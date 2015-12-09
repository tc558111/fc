package org.jlab.ecmon.ui;

import org.jlab.geom.gui.DetectorShape3D;
import org.jlab.io.decode.AbsDetectorTranslationTable;
import org.jlab.clas.detector.DetectorRawData;
import org.jlab.clas.detector.DetectorType;
import org.jlab.clas.tools.utils.FileUtils;
import org.jlab.clas.tools.utils.ResourcesUtils;
import org.jlab.clasrec.utils.*;

import org.root.pad.*;
import org.root.histogram.*;
import org.root.attr.ColorPalette;
import org.root.attr.TStyle;

import java.awt.Color;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.TreeMap;
import java.util.Arrays;

import javax.swing.SwingUtilities;

import org.jlab.evio.clas12.*;
import org.jlab.clasrec.main.*;
import org.jlab.evio.decode.EvioEventDecoder;
import org.jlab.ecmon.utils.*;

public class ECMonv1 extends DetectorMonitoring {
	
	public static ECMonv1 monitor;
	public static DetectorBrowserApp app;
	EvioEventDecoder           decoder = new EvioEventDecoder();
	ColorPalette palette = new ColorPalette();
	
	double ec_xpix[][][] = new double[3][1296][7];
	double ec_ypix[][][] = new double[3][1296][7];
	double ec_cthpix[] = new double[1296];
	int pixmap[][]     = new int[3][1296];
	int inProcess      = 0;
	int thr = 20;
	
    public TreeMap<Integer,H1D>      ECAL_ADCPIX  = new TreeMap<Integer,H1D>();
    public TreeMap<Integer,H1D>      ECAL_TDCPIX  = new TreeMap<Integer,H1D>();
    public TreeMap<Integer,H1D>      ECAL_PIXA    = new TreeMap<Integer,H1D>();
    public TreeMap<Integer,H1D>      ECAL_PIXT    = new TreeMap<Integer,H1D>();
    public TreeMap<Integer,H1D>      ECAL_PIXAS   = new TreeMap<Integer,H1D>();
    public TreeMap<Integer,H1D>      ECAL_PIXTS   = new TreeMap<Integer,H1D>();
    public TreeMap<Integer,H1D>      ECAL_PIXASUM = new TreeMap<Integer,H1D>();
    public TreeMap<Integer,H1D>      ECAL_PIXTSUM = new TreeMap<Integer,H1D>();
    public TreeMap<Integer,H1D>      ECAL_EVTPIX  = new TreeMap<Integer,H1D>();
    public TreeMap<Integer,H2D>      ECAL_ADC     = new TreeMap<Integer,H2D>();
    public TreeMap<Integer,H2D>      ECAL_TDC     = new TreeMap<Integer,H2D>();
    public TreeMap<Integer,H2D>      ECAL_ADC_PIX = new TreeMap<Integer,H2D>();
    public TreeMap<Integer,H2D>      ECAL_TDC_PIX = new TreeMap<Integer,H2D>();
    public TreeMap<Integer,H2D>      ECAL_APIX    = new TreeMap<Integer,H2D>();
    public TreeMap<Integer,H2D>      ECAL_TPIX    = new TreeMap<Integer,H2D>();
    public TreeMap<Integer,Object>   LAYMAP       = new TreeMap<Integer,Object>();

	public ECMonv1() {
		super("ECMON","1.0","lcsmith");
		initTT();
	}
	
	public void initTT() {
		
        String directory = ResourcesUtils.getResourceDir("CLAS12DIR", "etc/bankdefs/translation");        
        List<String> tables = FileUtils.getFilesInDir(directory);
        
        for(int loop = 0; loop < tables.size(); loop++){
            
            AbsDetectorTranslationTable  trTable = new AbsDetectorTranslationTable();
            Path tablePath = Paths.get(tables.get(loop));
            
            String filename    = tablePath.getFileName().toString();
            String system_name = filename.substring(0, filename.indexOf("."));
            System.out.println("LOADING FILE : " + tablePath.getFileName() + " SYSTEM = " + system_name);
            
            trTable.readFile(tables.get(loop));
            trTable.setName(system_name);   
            
            decoder.addTranslationTable(trTable);
            
            if(system_name.compareTo("FTOF1A")==0){
                trTable.setDetectorType(DetectorType.FTOF);
            }
            
            if(system_name.compareTo("FTOF1B")==0){
                trTable.setDetectorType(DetectorType.FTOF);
            }
            
            if(system_name.compareTo("ECAL")==0){
                trTable.setDetectorType(DetectorType.EC);
            }
            if(system_name.compareTo("PCAL")==0){
                trTable.setDetectorType(DetectorType.EC);
            }
        }
	}

	@Override

	public void init() {
		initHist();
	}
		
    public void initHist(){
		for (int is=0;is<6;is++) {
			ECAL_EVTPIX.put(is,  new H1D("EVTPIX"+is,1296,1.0,1297.0));
			ECAL_PIXASUM.put(is, new H1D("A_PIXSUM"+is,1296,1.0,1297.0));
			ECAL_PIXTSUM.put(is, new H1D("T_PIXSUM"+is,1296,1.0,1297.0));
			ECAL_PIXAS.put(is,   new H1D("A_PIXAS"+is,1296,1.0,1297.0));
			ECAL_PIXTS.put(is,   new H1D("T_PIXTS"+is,1296,1.0,1297.0));
 
			for (int lay=1 ; lay<4 ; lay++) {
				int iss=is*10+lay;
				ECAL_ADC.put(iss,     new H2D("ADC_LAYER_"+iss,100,0.0,200.0,36,1.0,37.0));
				ECAL_TDC.put(iss,     new H2D("TDC_LAYER_"+iss,100,1330.0,1370.0,36,1.0,37.0));  
				ECAL_ADC_PIX.put(iss, new H2D("ADC_PIX_LAYER_"+iss,100,0.0,200.0,36,1.0,37.0));
				ECAL_TDC_PIX.put(iss, new H2D("TDC_PIX_LAYER_"+iss,100,1330.0,1370.0,36,1.0,37.0));  
				ECAL_ADCPIX.put(iss,  new H1D("ADC_PIX"+iss,1296,1.0,1297.0));
				ECAL_PIXA.put(iss,    new H1D("T_PIX"+iss,1296,1.0,1297.0));
				ECAL_PIXT.put(iss,    new H1D("A_PIX"+iss,1296,1.0,1297.0));
				ECAL_TDCPIX.put(iss,  new H1D("TDC_PIX"+iss,1296,1.0,1297.0));
				ECAL_APIX.put(iss,    new H2D("APIX"+iss,1296,1.0,1297.0,20,0.0,200.0));
				ECAL_TPIX.put(iss,    new H2D("TPIX"+iss,1296,1.0,1297.0,40,1330.0,1370.0));
			}
		}
	}
	
	public void initEcGui(int is1, int is2, String[] args) {
		System.out.println("initecgui():");
		palette.set(3);
		ecpixdef();
		ecpixang();
		ecpixmap();
		
		if(args.length > 0) thr = Integer.parseInt(args[0]);
		System.out.println("Threshold="+thr);
		
		LAYMAP.put(7, toTreeMap(ec_cthpix));
		
		long startTime = System.currentTimeMillis();
		
		for(int is=is1; is<is2; is++) {
		    for(int ip=0; ip<36 ; ip++)   app.addDetectorShape("EC U", getStrip(is,1,ip));
		    for(int ip=0; ip<36 ; ip++)   app.addDetectorShape("EC V", getStrip(is,2,ip));
		    for(int ip=0; ip<36 ; ip++)   app.addDetectorShape("EC W", getStrip(is,3,ip));
		    for(int ip=0; ip<1296 ; ip++) app.addDetectorShape("ECP",   getPixel(is,7,ip));
		    for(int ip=0; ip<1296 ; ip++) app.addDetectorShape("ECPA",  getPixel(is,8,ip));
		    for(int ip=0; ip<1296 ; ip++) app.addDetectorShape("ECPA U",getPixel(is,9,ip));
		    for(int ip=0; ip<1296 ; ip++) app.addDetectorShape("ECPA V",getPixel(is,10,ip));
		    for(int ip=0; ip<1296 ; ip++) app.addDetectorShape("ECPA W",getPixel(is,11,ip));
		    for(int ip=0; ip<1296 ; ip++) app.addDetectorShape("ECPT",  getPixel(is,12,ip));
		    for(int ip=0; ip<1296 ; ip++) app.addDetectorShape("ECPT U",getPixel(is,13,ip));
		    for(int ip=0; ip<1296 ; ip++) app.addDetectorShape("ECPT V",getPixel(is,14,ip));
		    for(int ip=0; ip<1296 ; ip++) app.addDetectorShape("ECPT W",getPixel(is,15,ip));
		}
	    
		long stopTime = System.currentTimeMillis();
		long funTime = stopTime-startTime;
		System.out.println("initEcGui time= "+funTime);
	}

	public void ecpixdef() {
      System.out.println("ecpixdef():");
	       int jmax,pixel,m;
	       double xtmp,ytmp,tmp;
	       double   y_inc=10.0;
	       double   x_inc=5.31;
	       double[] xstrt={0.0, -5.31, 5.31};
	       double[] ystrt={0.0, -10.0,-10.0};
	       double[] xtrans={0.0,0.0,0.0};
	       double[] ytrans={0.0,0.0,0.0};
	       double[]  yflip={-20.0,0.0,0.0};
	       double[] theta={270.0,330.0,30.0,90.0,150.0,210.0};
       
	       for(int u=1; u<37; u++) {
	           jmax = 2*u-1;
	           pixel= u*(u-1)-u;
	           tmp=y_inc*(u-1);
	           ytrans[0]=tmp;
	           ytrans[1]=tmp;
	           ytrans[2]=tmp;
	           for (int j=1; j<jmax+1; j=j+2) {
	               m=-u+j;
	               pixel=pixel+2;
	               tmp=x_inc*m;
	               xtrans[0]=tmp;
	               xtrans[1]=tmp;
	               xtrans[2]=tmp;
	               for (int k=0;k<3;k++) {
	                   xtmp=(xstrt[k]+xtrans[k]);
	                   ytmp=(ystrt[k]-ytrans[k]);
	                   ec_xpix[k][pixel-1][6]=xtmp;
	                   ec_ypix[k][pixel-1][6]=ytmp;
	                   if (u!=36) {
	                       ytmp=ytmp+yflip[k];
	                       ec_xpix[k][pixel+2*u-1][6]=xtmp;
	                       ec_ypix[k][pixel+2*u-1][6]=ytmp;
	                   }     
	               }
	           }
	       }
	       for(int is=0; is<6; is++) {
	    	   double thet=theta[is]*3.14159/180.;
	    	   for (int ipix=0; ipix<1296; ipix++) {
	    		   for (int k=0;k<3;k++){
	    			   ec_xpix[k][ipix][is]= -(ec_xpix[k][ipix][6]*Math.cos(thet)+ec_ypix[k][ipix][6]*Math.sin(thet));
	    			   ec_ypix[k][ipix][is]=  -ec_xpix[k][ipix][6]*Math.sin(thet)+ec_ypix[k][ipix][6]*Math.cos(thet);
	    		   }
	    	   }
	       }
	   
	}
	
	public void ecpixang() {
	  System.out.println("ecpixang():");
		double x,y,r,angle,r0=510.3;
		int jmax,m,pixel,sign;
		double[] off={3.453,6.907};
		
		pixel=0;
		
		for (int u=1;u<37;u++){
			jmax = 2*u-1;
			for (int j=1;j<jmax+1;j++){
				m = -u+j;
				pixel = pixel +1;
				sign=j%2;
				x = (18-u)*10.36-off[sign]+3.453;
				y = m*5.305;
				r = Math.sqrt(x*x+y*y);
				angle = Math.atan(r/r0);
				ec_cthpix[pixel-1]=1./Math.cos(angle);
			}
					
		}	
	}
	
	public void ecpixmap() {
      System.out.println("ecpixmap():");
		int pixel;
		for (int u=1 ; u<37 ; u++){
			int jmax = 2*u-1;
			int v=36 ; int w=36-u+1 ; int uvw=73 ; int nj=0;
			for (int j=1; j<jmax+1 ; j++) {
				if (nj==2) {v--; nj=0;}
				w=uvw-u-v;
				pixel=pix(u,v,w);
				pixmap[0][pixel-1]=u;
				pixmap[1][pixel-1]=v;
				pixmap[2][pixel-1]=w;
				switch (uvw) {
				case 73: uvw=74;
				break;
				case 74: uvw=73;
				break;
				}
				nj++;
			}
		}
	}

	
	public int pix(int u, int v, int w) {
		return u*(u-1)+v-w+1;
	}
	
	public float uvw_dalitz(int ic, int ip, int il) {
		float uvw=0;
		switch (ic) {
		case 0: //PCAL
			if (il==1&&ip<=52) uvw=(float)ip/84;
			if (il==1&&ip>52)  uvw=(float)(52+(ip-52)*2)/84;
			if (il==2&&ip<=15) uvw=(float) 2*ip/77;
			if (il==2&&ip>15)  uvw=(float)(30+(ip-15))/77;
			if (il==3&&ip<=15) uvw=(float) 2*ip/77;
			if (il==3&&ip>15)  uvw=(float)(30+(ip-15))/77;
			break;
		case 1: //ECALinner
			uvw=(float)ip/36;
			break;
		case 2: //ECALouter
			uvw=(float)ip/36;
			break;
		}
		return uvw;
	}
	

	public DetectorShape3D getPixel(int sector, int layer, int pixel){

	    double[] xc= new double[3];
	    double[] yc= new double[3];

	    for(int j = 0; j < 3; j++){
	    	xc[j] = ec_xpix[j][pixel][sector];
	    	yc[j] = ec_ypix[j][pixel][sector];
	    }

	    DetectorShape3D shape = new DetectorShape3D();
	    shape.SECTOR = sector;
	    shape.LAYER  = layer;
	    shape.COMPONENT = pixel;
	    shape.setPoints(xc,yc);
	    return shape;

	}
	
	public DetectorShape3D getStrip(int sector, int layer, int str) {

		int ipix=1,pix1=1,pix2=1;
	    double[] xc   = new double[5];
	    double[] yc   = new double[5];
		int[][] pts73 = {{1,2,3,1},{2,3,1,2},{3,1,2,3}};
		int[][] pts74 = {{1,2,1},{3,2,3},{3,2,3},{1,2,1}};
	    
	    int strip=str+1;
	    
	    switch(layer) {
	    
	    case 1:
	    	pix1 = pix(strip,37-strip,36);
	    	pix2 = pix(strip,36,37-strip);
	    	break;
	    case 2:
	    	pix1 = pix(36,strip,37-strip);
	    	pix2 = pix(37-strip,strip,36);
	    	break;
	    case 3:
	    	pix1 = pix(37-strip,36,strip);
	    	pix2 = pix(36,37-strip,strip);
	    	break;
	    	
	    }

	    for(int j = 0; j < 4; j++){
	    	if (j<=1) ipix = pix1;
	    	if (j>1)  ipix = pix2;
	    	xc[j] = ec_xpix[pts73[layer-1][j]-1][ipix-1][sector];
	    	yc[j] = ec_ypix[pts73[layer-1][j]-1][ipix-1][sector];
	    	//System.out.println(sector+" "+str+" "+xc[0]+" "+xc[1]+" "+xc[2]+" "+xc[3]);
	    	//System.out.println(sector+" "+str+" "+yc[0]+" "+yc[1]+" "+yc[2]+" "+yc[3]);	    	
	    }
	    xc[4]=xc[0];
	    yc[4]=yc[0];
	    
	    DetectorShape3D shape = new DetectorShape3D();
	    shape.SECTOR = sector;
	    shape.LAYER  = layer;
	    shape.COMPONENT = str;
	    shape.setPoints(xc,yc);
	    return shape;

	}
	
	@Override

	public void configure(ServiceConfiguration sc) {

	}

	@Override

	public void analyze() {
		  
		for (int is=0;is<6;is++) {
	    ECAL_ADCPIX.get(is*10+1).divide(ECAL_EVTPIX.get(is),ECAL_PIXA.get(is*10+1));
	    ECAL_ADCPIX.get(is*10+2).divide(ECAL_EVTPIX.get(is),ECAL_PIXA.get(is*10+2));
	    ECAL_ADCPIX.get(is*10+3).divide(ECAL_EVTPIX.get(is),ECAL_PIXA.get(is*10+3));
	    ECAL_TDCPIX.get(is*10+1).divide(ECAL_EVTPIX.get(is),ECAL_PIXT.get(is*10+1));
	    ECAL_TDCPIX.get(is*10+2).divide(ECAL_EVTPIX.get(is),ECAL_PIXT.get(is*10+2));
	    ECAL_TDCPIX.get(is*10+3).divide(ECAL_EVTPIX.get(is),ECAL_PIXT.get(is*10+3));
	    ECAL_PIXASUM.get(is).divide(ECAL_EVTPIX.get(is),ECAL_PIXAS.get(is));
	    ECAL_PIXTSUM.get(is).divide(ECAL_EVTPIX.get(is),ECAL_PIXTS.get(is));
	    	
	    LAYMAP.put(1+is*20, toTreeMap(ECAL_ADC.get(is*10+1).projectionY().getData()));
	    LAYMAP.put(2+is*20, toTreeMap(ECAL_ADC.get(is*10+2).projectionY().getData()));
	    LAYMAP.put(3+is*20, toTreeMap(ECAL_ADC.get(is*10+3).projectionY().getData()));
	    LAYMAP.put(7+is*20, toTreeMap(ECAL_EVTPIX.get(is).getData()));
	    LAYMAP.put(8+is*20, toTreeMap(ECAL_PIXAS.get(is).getData()));
	    LAYMAP.put(9+is*20, toTreeMap(ECAL_PIXA.get(is*10+1).getData()));
	    LAYMAP.put(10+is*20,toTreeMap(ECAL_PIXA.get(is*10+2).getData()));
	    LAYMAP.put(11+is*20,toTreeMap(ECAL_PIXA.get(is*10+3).getData()));
	    LAYMAP.put(12+is*20,toTreeMap(ECAL_PIXTS.get(is).getData()));
	    LAYMAP.put(13+is*20,toTreeMap(ECAL_PIXT.get(is*10+1).getData()));
	    LAYMAP.put(14+is*20,toTreeMap(ECAL_PIXT.get(is*10+2).getData()));
	    LAYMAP.put(15+is*20,toTreeMap(ECAL_PIXT.get(is*10+3).getData()));	     
		}
	}
	
	public TreeMap<Integer, Object> toTreeMap(double dat[]) {
        TreeMap<Integer, Object> hcontainer = new TreeMap<Integer, Object>();
        hcontainer.put(1, dat);
        double[] b = Arrays.copyOf(dat, dat.length);
        Arrays.sort(b);
        double min = b[0]; double max=b[b.length-1];
        if (min<=0) min=0.0;
        hcontainer.put(2, min);
        hcontainer.put(3, max);
        return hcontainer;
	}
	
	@Override

	public void processEvent(EvioDataEvent event) {
		
		int nh[][]         = new int[6][9];
		int strr[][][]     = new int[6][9][68]; 
		int adcr[][][]     = new int[6][9][68];
		float tdcr[][][]   = new float[6][9][68];
		
		int inh;
		float uvw = 0;
		
		for (int is=0 ; is<6 ; is++) {
			for (int il=0 ; il<9 ; il++) {
				nh[is][il]  = 0;
				for (int ip=0 ; ip<68 ; ip++) {
					strr[is][il][ip] = 0;
					adcr[is][il][ip] = 0;
					tdcr[is][il][ip] = 0;
				}
			}
		}
		
		inProcess=1;
		double mc_t=0.0;
		float tdcmax=100000;
		boolean debug=false;

        int   adc=0;
        float tdc=0;
        
		// Process raw banks
		
		if(event.hasBank("EC::true")!=true) {	
					
        List<DetectorRawData> rawdata = decoder.getDataEntries(event);

        if (debug) {
    		event.getHandler().list();
    		for(DetectorRawData data : rawdata){
    			System.out.println(data);
    		}
        }

        decoder.decode(rawdata);
		
        if (debug) {
        	for(DetectorRawData data : rawdata){
        		System.out.println(data);
        	}
        }
        List<DetectorRawData>  selectionECAL = decoder.getDetectorData(rawdata,DetectorType.EC);       
        
        for(DetectorRawData data : selectionECAL){
        	int is  = data.getDescriptor().getSector();
        	int il  = data.getDescriptor().getLayer();
        	int ip  = data.getDescriptor().getComponent();
        	if (data.getMode()==10) tdc = (float) data.getTDC()*24/1000;
        	if (data.getMode()==1)  adc = (int)   data.getSignal(0,30,35,60)/10;
        				        	
			if(il<4){
				int ic=1;
				if (adc>thr) {
					//System.out.println("sector,layer,pmt,adc,tdc= "+is+" "+il+" "+ip+" "+adc+" "+tdc);
	   	            int  iv = ic*3+il;
	          	    nh[is-1][iv-1]++;
	          	    inh = nh[is-1][iv-1];
	          	    adcr[is-1][iv-1][inh-1] = adc;
	          	    tdcr[is-1][iv-1][inh-1] = tdc;
	          	    strr[is-1][iv-1][inh-1] = ip;
	          	    uvw=uvw+uvw_dalitz(ic,ip,il); //Dalitz test
	             	int iss = (is-1)*10+il;
	   		    	ECAL_ADC.get(iss).fill(adc,ip,1.0);
		   		    ECAL_TDC.get(iss).fill(tdc,ip,1.0);
				}
			}
		}
		}   	
		// Process MC banks
		
		if(event.hasBank("EC::true")==true){
			EvioDataBank bank  = (EvioDataBank) event.getBank("EC::true");
			int nrows = bank.rows();
			for(int i=0; i < nrows; i++){
				mc_t = bank.getDouble("avgT",i);
			}	
		}
					
		if(event.hasBank("EC::dgtz")==true){
			
			EvioDataBank bank = (EvioDataBank) event.getBank("EC::dgtz");
			
			for(int i = 0; i < bank.rows(); i++){
				float dum = (float)bank.getInt("TDC",i)-(float)mc_t*1000;
				if (dum<tdcmax) tdcmax=dum;
			}	    		
	        
	    uvw=0;
	
	    int nrows = bank.rows();
		for(int i = 0; i < nrows; i++){
        	int is  = bank.getInt("sector",i);
			int ip  = bank.getInt("strip",i);
         	int ic  = bank.getInt("stack",i);	 
		    int il  = bank.getInt("view",i);  
			    adc = bank.getInt("ADC",i);
        	    tdc = bank.getInt("TDC",i);
		     
        	float tdcc=(((float)tdc-(float)mc_t*1000)-tdcmax+1340000)/1000;
		   	
        	int  iv = ic*3+il;
        	
		    if(ic==1){
         	   if (adc>thr) {
          	     nh[is-1][iv-1]++;
          	     inh = nh[is-1][iv-1];
          	     adcr[is-1][iv-1][inh-1] = adc;
          	     tdcr[is-1][iv-1][inh-1] = tdcc;
          	     strr[is-1][iv-1][inh-1] = ip;
          	     uvw=uvw+uvw_dalitz(ic,ip,il); //Dalitz test
          	   }
         	int iss = (is-1)*10+il;
   		    ECAL_ADC.get(iss).fill(adc,ip,1.0);
   		    ECAL_TDC.get(iss).fill(tdcc,ip,1.0);
		    }
		    
		    }
		}
		
		for (int is=0 ; is<6 ; is++) {		
			
			boolean good_u = nh[is][3]==1;
			boolean good_v = nh[is][4]==1;
			boolean good_w = nh[is][5]==1;
			boolean good_uvw = good_u&&good_v&&good_w; //Multiplicity test (NU=NV=NW=1)
		    //if (is==1) System.out.println("is,uvw,nh= "+is+" "+uvw+" "+nh[is][3]+" "+nh[is][4]+" "+nh[is][5]);
			if (good_uvw&&Math.abs(uvw-2.0)<0.2) {
			    //System.out.println("is,uvw,nh= "+is+" "+uvw+" "+nh[is][3]+" "+nh[is][4]+" "+nh[is][5]);
				int pixel=pix(strr[is][3][0],strr[is][4][0],strr[is][5][0]);
				ECAL_EVTPIX.get(is).fill(pixel,1.0);
				for (int il=1; il<4 ; il++){
					int iss = is*10+il;
					ECAL_ADC_PIX.get(iss).fill(adcr[is][il+2][0],strr[is][il+2][0],1.0);
					ECAL_TDC_PIX.get(iss).fill(tdcr[is][il+2][0],strr[is][il+2][0],1.0);
					ECAL_PIXASUM.get(is).fill(pixel,adcr[is][il+2][0]);
					ECAL_PIXTSUM.get(is).fill(pixel,tdcr[is][il+2][0]);
					ECAL_ADCPIX.get(iss).fill(pixel,adcr[is][il+2][0]);
					ECAL_TDCPIX.get(iss).fill(pixel,tdcr[is][il+2][0]);
					ECAL_APIX.get(iss).fill(pixel,adcr[is][il+2][0],1.0);
					ECAL_TPIX.get(iss).fill(pixel,tdcr[is][il+2][0],1.0);
				}
			}
			
		}

	}
 
	public Color getColor(int is, int layer, int component) {
		
		double colorfraction=1;
		switch(inProcess) {
		case 0: // Assign default colors upon starting GUI (before event processing)
	    if(layer==1) colorfraction = (double)component/36;
	    if(layer==2) colorfraction = (double)component/36;
	    if(layer==3) colorfraction = (double)component/36;
		if(layer>=7) colorfraction = getcolor((TreeMap<Integer, Object>) LAYMAP.get(7), component); 
		break;
		case 1: // Use LAYMAP to get colors of components while processing data
		//System.out.println("is,layer,component="+is+" "+layer+" "+component);
		colorfraction = getcolor((TreeMap<Integer, Object>) LAYMAP.get(is*20+layer), component);
	    break;
		}
		if (colorfraction<0.05) colorfraction = 0.05;
		return palette.getRange(colorfraction);

	}

	public double getcolor(TreeMap<Integer, Object> map, int component) {
		double color=9;
		int opt=1;
		double val[] =(double[]) map.get(1); 
		double rmin  =(double)   map.get(2);
		double rmax  =(double)   map.get(3);
		//System.out.println("comp,rmin,rmax,val="+component+" "+" "+rmin+" "+rmax+" "+val[component]);
		double z=val[component];
		if (z==0) color=9;
		if (opt==1) color=(double)(z-rmin)/(rmax-rmin);
		if (opt==2) color=(double)(Math.log10(z)-Math.log10(rmin))/(Math.log10(rmax)-Math.log10(rmin));      
		 
		if (color>1) color=1;
        if (color<=0)  color=0.;

		return color;
	}
	
	public void drawComponent(int is, int layer, int component, EmbeddedCanvas canvas) {
		
		int l,col0=0,col1=0,col2=0,strip=0,pixel=0;
		String alab[]={"U ADC","V ADC","W ADC"},tlab[]={"U TDC","V TDC","W TDC"};
		H1D u,v,w,h;
		//TStyle.setOptStat(false);
	    TStyle.setStatBoxFont(TStyle.getStatBoxFontName(),12);
	    
		if (inProcess==1) analyze(); //updates component colormap ; may not be thread-save 
		
		//System.out.println("is,layer,component="+is+" "+layer+" "+component);
			
		if (layer<4)  {col0=0 ; col1=4; col2=2;strip=component;}
		if (layer>=7) {col0=4 ; col1=4; col2=2;pixel=component;}
		
	    canvas.divide(3,3);
	    l=1;canvas.cd(l-1); u = ECAL_ADC.get(is*10+l).projectionY(); u.setXTitle("U STRIPS"); u.setFillColor(col0); canvas.draw(u);
	    l=2;canvas.cd(l-1); v = ECAL_ADC.get(is*10+l).projectionY(); v.setXTitle("V STRIPS"); v.setFillColor(col0); canvas.draw(v);
	    l=3;canvas.cd(l-1); w = ECAL_ADC.get(is*10+l).projectionY(); w.setXTitle("W STRIPS"); w.setFillColor(col0); canvas.draw(w);
	    
	    if (layer<4) {
	    	 l=layer;canvas.cd(l-1); h = ECAL_ADC.get(is*10+l).projectionY(); h.setFillColor(col1); canvas.draw(h,"same");
	         H1D copy = h.histClone("Copy");
	         copy.reset() ; copy.setBinContent(strip, h.getBinContent(strip));
	         copy.setFillColor(col2); canvas.draw(copy,"same");
	    }
	    
	    if (layer==7) {
	    	for(int il=1;il<4;il++) {
	    		canvas.cd(il-1); h = ECAL_ADC.get(is*10+il).projectionY();
	    		H1D copy = h.histClone("Copy");
	    		copy.reset() ; copy.setBinContent(pixmap[il-1][pixel]-1, h.getBinContent(pixmap[il-1][pixel]-1));
	    		copy.setFillColor(col2); canvas.draw(copy,"same");	    		 
	    	}
	    }
	    
		if (layer>7) {
			for(int il=1;il<4;il++) {
				canvas.cd(il-1); h = ECAL_ADC_PIX.get(is*10+il).projectionY();
	 		    H1D copy = h.histClone("Copy");
			    copy.reset() ; copy.setBinContent(pixmap[il-1][pixel]-1, h.getBinContent(pixmap[il-1][pixel]-1));
			    copy.setFillColor(col2); canvas.draw(copy,"same");		    		 
			}  	 
	     }
	    
	     if (layer<4){
	     if(layer!=1) {l=1;canvas.cd(l+2); u = ECAL_ADC.get(is*10+l).sliceY(22);   u.setXTitle(alab[l-1]); u.setFillColor(col0); canvas.draw(u);}
	     if(layer!=2) {l=2;canvas.cd(l+2); v = ECAL_ADC.get(is*10+l).sliceY(22);   v.setXTitle(alab[l-1]); v.setFillColor(col0); canvas.draw(v);}
	     if(layer!=3) {l=3;canvas.cd(l+2); w = ECAL_ADC.get(is*10+l).sliceY(22);   w.setXTitle(alab[l-1]); w.setFillColor(col0); canvas.draw(w);}
	     l=layer;          canvas.cd(l+2); h = ECAL_ADC.get(is*10+l).sliceY(strip);h.setXTitle(alab[l-1]); h.setFillColor(col2); canvas.draw(h);
		 if(layer!=1) {l=1;canvas.cd(l+5); u = ECAL_TDC.get(is*10+l).sliceY(22);   u.setXTitle(tlab[l-1]); u.setFillColor(col0); canvas.draw(u);}
		 if(layer!=2) {l=2;canvas.cd(l+5); v = ECAL_TDC.get(is*10+l).sliceY(22);   v.setXTitle(tlab[l-1]); v.setFillColor(col0); canvas.draw(v);}
		 if(layer!=3) {l=3;canvas.cd(l+5); w = ECAL_TDC.get(is*10+l).sliceY(22);   w.setXTitle(tlab[l-1]); w.setFillColor(col0); canvas.draw(w);}
		 l=layer;          canvas.cd(l+5); h = ECAL_TDC.get(is*10+l).sliceY(strip);h.setXTitle(tlab[l-1]); h.setFillColor(col2); canvas.draw(h);
	     }
	     
	     if (layer==7){
	    	 for(int il=1;il<4;il++) {
	    		 canvas.cd(il+2) ; h = ECAL_ADC.get(is*10+il).sliceY(pixmap[il-1][pixel]-1); h.setXTitle(alab[il-1]); h.setFillColor(col2); canvas.draw(h);
	    		 canvas.cd(il+5) ; h = ECAL_TDC.get(is*10+il).sliceY(pixmap[il-1][pixel]-1); h.setXTitle(tlab[il-1]); h.setFillColor(col2); canvas.draw(h);
	    	 }
		 }
	     if (layer>7&&layer<12){
	    	 for(int il=1;il<4;il++) {
	    		 canvas.cd(il+2) ; h = ECAL_ADC_PIX.get(is*10+il).sliceY(pixmap[il-1][pixel]-1); h.setXTitle(alab[il-1]); h.setFillColor(col2); canvas.draw(h);
	    		 canvas.cd(il+5) ; h = ECAL_APIX.get(is*10+il).sliceX(pixel)                   ; h.setXTitle(alab[il-1]); h.setFillColor(col2); canvas.draw(h);
	    	 }
		 }
	     if (layer>11&&layer<16){
	    	 for(int il=1;il<4;il++) {
	    		 canvas.cd(il+2) ; h = ECAL_TDC_PIX.get(is*10+il).sliceY(pixmap[il-1][pixel]-1); h.setXTitle(tlab[il-1]); h.setFillColor(col2); canvas.draw(h);
	    		 canvas.cd(il+5) ; h = ECAL_TPIX.get(is*10+il).sliceX(pixel)                   ; h.setXTitle(tlab[il-1]); h.setFillColor(col2); canvas.draw(h);
	    	 }
		 } 
		     
	}

	public static void main(String[] args){
		
		monitor = new ECMonv1();
		
	    SwingUtilities.invokeLater(new Runnable() {
	    	public void run() {
	    		app = new DetectorBrowserApp();
	    		monitor.init();
	    		monitor.initEcGui(0,6,args);
	    		app.setPluginClass(monitor);
	    		app.updateDetectorView();
	    	}
	    });
	}
}
