package org.jlab.ecmon.ui;

import org.jlab.geom.prim.Path3D;
import org.jlab.clas.detector.BankType;
import org.jlab.clas.detector.DetectorBankEntry;
import org.jlab.clas.detector.DetectorCollection;
import org.jlab.clas.detector.DetectorDescriptor;
import org.jlab.clas.detector.DetectorType;
import org.jlab.clas.tools.utils.DataUtils;
import org.jlab.clas12.calib.DetectorShape2D;
import org.jlab.clas12.detector.EventDecoder;
import org.jlab.clas12.detector.FADCConfig;
import org.jlab.clas12.detector.FADCConfigLoader;
import org.root.func.F1D;
import org.root.group.TDirectory;
import org.root.histogram.*;
import org.root.pad.EmbeddedCanvas;
import org.root.attr.ColorPalette;
import org.root.attr.TStyle;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.Arrays;

import javax.swing.SwingUtilities;

import org.jlab.evio.clas12.*;
import org.jlab.data.io.DataEvent;
import org.jlab.ecmon.utils.*;

public class ECMon extends DetectorMonitor {
	
	public static ECMon monitor;
	public static MonitorApp app;
	
	public EventDecoder     decoder = new EventDecoder();
	FADCConfigLoader          fadc  = new FADCConfigLoader();
	TDirectory         mondirectory = new TDirectory(); 	
	ColorPalette            palette = new ColorPalette();
	
	DetectorCollection<CalibrationData> collection = new DetectorCollection<CalibrationData>();    
	
	double ec_xpix[][][] = new double[3][1296][7];
	double ec_ypix[][][] = new double[3][1296][7];
	double ec_cthpix[]   = new double[1296];
	int pixmap[][]       = new int[3][1296];
	int inProcess        = 0; //0=init 1=processing 2=end-of-run 3=post-run
	boolean inMC         = false; //true=MC false=DATA
	int thr              = 20;
	String monpath       = "/Users/colesmith/COATJAVA";
	String monfile       = "mondirectory";
	String labadc[] 	 = {"monitor/pcal/adc","monitor/ecinner/adc","monitor/ecouter/adc"}; 
	String labtdc[]    	 = {"monitor/pcal/tdc","monitor/ecinner/tdc","monitor/ecouter/tdc"}; 
	String labped[] 	 = {"monitor/pcal/ped","monitor/ecinner/ped","monitor/ecouter/ped"}; 
	String labatt[]    	 = {"monitor/pcal/att","monitor/ecinner/att","monitor/ecouter/att"}; 
	String labpmt[] 	 = {"monitor/pcal/pmt","monitor/ecinner/pmt","monitor/ecouter/pmt"}; 
	int hid;
	int tid       		 = 100000;
	int cid       		 = 10000;
	int lid       		 = 100;
			
    public TreeMap<Integer,H1D>      ECAL_ADCPIX  = new TreeMap<Integer,H1D>();
    public TreeMap<Integer,H1D>      ECAL_ADCPIX2 = new TreeMap<Integer,H1D>();
    public TreeMap<Integer,H1D>      ECAL_TDCPIX  = new TreeMap<Integer,H1D>();
    public TreeMap<Integer,H1D>      ECAL_PIXA    = new TreeMap<Integer,H1D>();
    public TreeMap<Integer,H1D>      ECAL_PIXA2   = new TreeMap<Integer,H1D>();
    public TreeMap<Integer,H1D>      ECAL_PIXT    = new TreeMap<Integer,H1D>();
    public TreeMap<Integer,H1D>      ECAL_PIXAS   = new TreeMap<Integer,H1D>();
    public TreeMap<Integer,H1D>      ECAL_PIXTS   = new TreeMap<Integer,H1D>();
    public TreeMap<Integer,H1D>      ECAL_PIXASUM = new TreeMap<Integer,H1D>();
    public TreeMap<Integer,H1D>      ECAL_PIXTSUM = new TreeMap<Integer,H1D>();
    public TreeMap<Integer,H1D>      ECAL_EVTPIXA = new TreeMap<Integer,H1D>();
    public TreeMap<Integer,H1D>      ECAL_EVTPIXT = new TreeMap<Integer,H1D>();
    public TreeMap<Integer,H2D>      ECAL_ADC     = new TreeMap<Integer,H2D>();
    public TreeMap<Integer,H2D>      ECAL_TDC     = new TreeMap<Integer,H2D>();
    public TreeMap<Integer,H2D>      ECAL_ADC_PIX = new TreeMap<Integer,H2D>();
    public TreeMap<Integer,H2D>      ECAL_TDC_PIX = new TreeMap<Integer,H2D>();
    public TreeMap<Integer,H2D>      ECAL_APIX    = new TreeMap<Integer,H2D>();
    public TreeMap<Integer,H2D>      ECAL_TPIX    = new TreeMap<Integer,H2D>();
    public TreeMap<Integer,Object>   LAYMAP       = new TreeMap<Integer,Object>();
    
	public ECMon(String[] args) {
		super("ECMON","1.0","lcsmith");
		fadc.load("/test/fc/fadc",10,"default");
		if(args.length == 1) thr = Integer.parseInt(args[0]);		
		if(args.length == 2) monpath = args[1];		
		System.out.println("Threshold= "+thr);
		System.out.println("monpath= "+monpath);
		
	}
	
	public void init() {
		inProcess=0;
		initHistograms();
		collection.clear();
		
	}
	
	public void close() {
		this.mondirectory.write(monpath+"/"+monfile);
		System.out.println("Writing out histograms");
		
	}
	
    public TDirectory getDir(){
        return this.mondirectory;
        
    }	
    
	public void initHistograms() {
		
		int    nbn1[] = {68,36,36}; 
		double nbn2[] = {69.0,37.0,37.0}; 
		
		int is        = 2;
		int iss       = (int) (is*1e7);
		
	    TDirectory monADC[] = new TDirectory[3];
	    TDirectory monTDC[] = new TDirectory[3];
	    TDirectory monPED[] = new TDirectory[3];
	    TDirectory monPMT[] = new TDirectory[3];
	    TDirectory monATT[] = new TDirectory[3];
	    
		for (int ic=1 ; ic<3 ; ic++) {  //ic=0,1,2 -> PCAL,ECALinner,ECALouter
			
			monADC[ic] = new TDirectory(labadc[ic]);
			monTDC[ic] = new TDirectory(labtdc[ic]); 
			monPED[ic] = new TDirectory(labped[ic]);
			monPMT[ic] = new TDirectory(labpmt[ic]); 
			monATT[ic] = new TDirectory(labatt[ic]);
    			
    		hid=iss+ic*cid;  
    		for (int il=1 ; il<4 ; il++) {
    			monADC[ic].add(new H2D("ADC"+(int)(hid+10*tid+il*lid),50,0.0,200.0,nbn1[ic],0.0,nbn2[ic])); 
    			monPED[ic].add(new H2D("PED"+(int)(hid+10*tid+il*lid),20,-10.,10.0,nbn1[ic],0.0,nbn2[ic])); 
    			monTDC[ic].add(new H2D("TDC"+(int)(hid+10*tid+il*lid),60,-15.0,15.0,nbn1[ic],0.0,nbn2[ic]));     		 
    			monTDC[ic].add(new H2D("TDC"+(int)(hid+11*tid+il*lid),60,-15.0,15.0,nbn1[ic],0.0,nbn2[ic]));     		 
     		}    	
    		getDir().addDirectory(monADC[ic]);
    		getDir().addDirectory(monPED[ic]);
    		getDir().addDirectory(monTDC[ic]);
    	}
		
		for (is=0;is<6;is++) {			
			for (int lay=1 ; lay<7 ; lay++) {
				iss=is*10+lay;
				ECAL_ADC.put(iss,     new H2D("ADC_LAYER_"+iss,100,0.0,200.0,36,1.0,37.0));
				ECAL_TDC.put(iss,     new H2D("TDC_LAYER_"+iss,100,1330.0,1370.0,36,1.0,37.0));  
				ECAL_ADC_PIX.put(iss, new H2D("ADC_PIX_LAYER_"+iss,100,0.0,200.0,36,1.0,37.0));
				ECAL_TDC_PIX.put(iss, new H2D("TDC_PIX_LAYER_"+iss,100,1330.0,1370.0,36,1.0,37.0));  
				ECAL_ADCPIX.put(iss,  new H1D("ADC_PIX"+iss,1296,1.0,1297.0));
				ECAL_ADCPIX2.put(iss, new H1D("ADC_PIX2"+iss,1296,1.0,1297.0));
				ECAL_PIXA.put(iss,    new H1D("A_PIX"+iss,1296,1.0,1297.0));
				ECAL_PIXA2.put(iss,   new H1D("A_PIX2"+iss,1296,1.0,1297.0));
				ECAL_PIXT.put(iss,    new H1D("T_PIX"+iss,1296,1.0,1297.0));
				ECAL_TDCPIX.put(iss,  new H1D("TDC_PIX"+iss,1296,1.0,1297.0));
				ECAL_APIX.put(iss,    new H2D("APIX"+iss,1296,1.0,1297.0,30,0.0,250.0));
				ECAL_TPIX.put(iss,    new H2D("TPIX"+iss,1296,1.0,1297.0,40,1330.0,1370.0));
			}
			for (int lay=7; lay<9; lay++) {
				iss=is*10+lay;
				ECAL_EVTPIXA.put(iss, new H1D("EVTPIXA"+iss,1296,1.0,1297.0));
				ECAL_EVTPIXT.put(iss, new H1D("EVTPIXT"+iss,1296,1.0,1297.0));
				ECAL_PIXASUM.put(iss, new H1D("A_PIXSUM"+iss,1296,1.0,1297.0));
				ECAL_PIXTSUM.put(iss, new H1D("T_PIXSUM"+iss,1296,1.0,1297.0));
				ECAL_PIXAS.put(iss,   new H1D("A_PIXAS"+iss,1296,1.0,1297.0));
				ECAL_PIXTS.put(iss,   new H1D("T_PIXTS"+iss,1296,1.0,1297.0));
			}
		}
		
	}
	
	public void initDetector(int is1, int is2) {
		
		System.out.println("initecgui():");
		palette.set(3);
		ecpixdef();
		ecpixang();
		ecpixmap();
		
		LAYMAP.put(0, toTreeMap(ec_cthpix));
						
		List<String> b1 = new ArrayList<String>();  b1.add("Inner") ; b1.add("Outer");
		List<String> b2 = new ArrayList<String>();  b2.add("EVT")   ; b2.add("ADC")   ; b2.add("TDC");
		List<String> b3 = new ArrayList<String>();  b3.add("EVT")   ; b3.add("ADC U") ; b3.add("ADC V"); b3.add("ADC W"); b3.add("ADC U+V+W");
		
		List<List<String>> bg1 = new ArrayList<List<String>>(); bg1.add(b1) ; bg1.add(b2);
		List<List<String>> bg2 = new ArrayList<List<String>>(); bg2.add(b1) ; bg2.add(b3);
		
		DetectorShapeView2D  dv1 = new DetectorShapeView2D("EC U")   ; dv1.addRB(bg1);
		DetectorShapeView2D  dv2 = new DetectorShapeView2D("EC V")   ; dv2.addRB(bg1);
		DetectorShapeView2D  dv3 = new DetectorShapeView2D("EC W")   ; dv3.addRB(bg1);
		DetectorShapeView2D  dv4 = new DetectorShapeView2D("EC PIX") ; dv4.addRB(bg2);
		
		long startTime = System.currentTimeMillis();
		
		for(int is=is1; is<is2; is++) {
			for(int ip=0; ip<36 ; ip++)    dv1.addShape(getStrip(is,1,ip));
			for(int ip=0; ip<36 ; ip++)    dv2.addShape(getStrip(is,2,ip));
			for(int ip=0; ip<36 ; ip++)    dv3.addShape(getStrip(is,3,ip));		    
			for(int ip=0; ip<1296 ; ip++)  dv4.addShape(getPixel(is,4,ip));
		}
		
		long stopTime = System.currentTimeMillis();
		long funTime = stopTime-startTime;
		System.out.println("initgui time= "+funTime);
		
		app.view.addDetectorLayer(dv1);
		app.view.addDetectorLayer(dv2);
		app.view.addDetectorLayer(dv3);
		app.view.addDetectorLayer(dv4);
		
		app.view.addDetectorListener(this);
		
	}
	
	public DetectorShape2D getPixel(int sector, int layer, int pixel){

	    DetectorShape2D shape = new DetectorShape2D(DetectorType.ECIN,sector,layer,pixel);
	    Path3D      shapePath = new Path3D();
	    
	    shapePath = shape.getShapePath();
	    
	    for(int j = 0; j < 3; j++){
	    	shapePath.addPoint(ec_xpix[j][pixel][sector],ec_ypix[j][pixel][sector],0.0);
	    }
	    return shape;
	}
	
	public DetectorShape2D getStrip(int sector, int layer, int str) {

	    DetectorShape2D shape = new DetectorShape2D(DetectorType.ECIN,sector,layer,str);
	    Path3D shapePath = new Path3D();
	    
	    shapePath = shape.getShapePath();
	    
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
	    xc[4]=xc[0]; yc[4]=yc[0];
	    
	    for(int j=0; j < 5; j++) shapePath.addPoint(xc[j],yc[j],0.0);
	
	    return shape;

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
	
	public double[] getpixels(int view, int strip, double[] in){
		int numpix,a,b,c,sum,pixel=1;
		numpix = 2*strip-1;
		a = strip;
		b = 37-a;
		c = 36;
		
		double[] out = new double[numpix];
		for (int j=0; j<numpix ; j++) {
			if (view==1) pixel=a*(a-1)+b-c+1;
			if (view==2) pixel=c*(c-1)+a-b+1;
			if (view==3) pixel=b*(b-1)+c-a+1;
			if (view==4) pixel=a*(a-1)+b-c+1;
			if (view==5) pixel=c*(c-1)+a-b+1;
			if (view==6) pixel=b*(b-1)+c-a+1;
			out[j] = in[pixel-1];
			sum = a+b+c;
			if(sum==73) b=b+1;
			if(sum==74) c=c-1;
		}
		return out;
		
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

	public void processEvent(DataEvent de) {
		
		EvioDataEvent event = (EvioDataEvent) de;
		
		int nha[][]      = new int[6][9];
		int nht[][]      = new int[6][9];
		int strra[][][]  = new int[6][9][68]; 
		int strrt[][][]  = new int[6][9][68]; 
		int adcr[][][]   = new int[6][9][68];
		double ftdcr[][][] = new double[6][9][68];
		double tdcr[][][]  = new double[6][9][68];
		double uvwa[][]    = new double[6][9];
		double uvwt[][]    = new double[6][9];
		
		int inh;
		
		for (int is=0 ; is<6 ; is++) {
			for (int il=0 ; il<9 ; il++) {
				nha[is][il]  = 0;
				nht[is][il]  = 0;
				uvwa[is][il] = 0;
				uvwt[is][il] = 0;
				for (int ip=0 ; ip<68 ; ip++) {
					strra[is][il][ip] = 0;
					strrt[is][il][ip] = 0;
					 adcr[is][il][ip] = 0;
					ftdcr[is][il][ip] = 0;
					 tdcr[is][il][ip] = 0;
				}
			}
			
		}
		
		float tdcmax=100000;
		boolean debug=false;
		int adc,ped,npk=0,pedref=0,timf=0,timc=0;
		double mc_t=0.,tdc=0,tdcf=0;
        H2D hpix;
				
		if(event.hasBank("EC::true")!=true) {
		
		if (debug) event.getHandler().list();	
					
    		decoder.decode(event);
            List<DetectorBankEntry> strips = decoder.getDataEntries("EC");
            
            for(DetectorBankEntry strip : strips) {
                adc=ped=pedref=npk=timf=timc=0 ; tdc=tdcf=0;
            	//System.out.println(strip);
            	int is  = strip.getDescriptor().getSector();
            	int il  = strip.getDescriptor().getLayer();
            	int ip  = strip.getDescriptor().getComponent();
            	int icr = strip.getDescriptor().getCrate(); 
            	int isl = strip.getDescriptor().getSlot(); 
            	int ich = strip.getDescriptor().getChannel(); 
            	
            	if(strip.getType()==BankType.TDC) {
            		int[] tdcc = (int[]) strip.getDataObject();
            		tdc = tdcc[0]*24./1000.;
            	}
            	if(strip.getType()==BankType.ADCFPGA) {
            		int[] adcc= (int[]) strip.getDataObject();
            		ped = adcc[2];
            		npk = adcc[3];
            		adc = (adcc[1]-ped*18)/10;
            		timf = DataUtils.getInteger(adcc[0],0,5);
            		timc = DataUtils.getInteger(adcc[0],6,14);
            		tdcf = timc*4.+timf*0.0625;
            		FADCConfig config=fadc.getMap().get(icr,isl,ich);
            		pedref = (int) config.getPedestal();
            	}
                    	
			//System.out.println("sector,layer,pmt:"+is+" "+il+" "+ip);
			//System.out.println("  nchan,adc,ped,pedref: "+npk+" "+adc+" "+ped+" "+pedref);
			//System.out.println("  tdc,timc,timf: "+tdc+" "+timc+" "+timf);
	            	
			int ic = 1;
			if (il>3) {ic=2 ; il=il-3;}
		
			if(ic==1||ic==2){
	   	        int  iv = ic*3+il;
	            int iss = (is-1)*10+il+(ic-1)*3;
	   	        if(tdc>1200&&tdc<1500){
		          uvwt[is-1][ic]=uvwt[is-1][il]+uvw_dalitz(ic,ip,il); //Dalitz test
	          	  nht[is-1][iv-1]++;
	          	  inh = nht[is-1][iv-1];
	          	   tdcr[is-1][iv-1][inh-1] = tdc;
	          	  strrt[is-1][iv-1][inh-1] = ip;
	          	  ECAL_TDC.get(iss).fill(tdc,ip,1.0);
	   	        }
	   	        if(adc>thr){
	          	  uvwa[is-1][ic]=uvwa[is-1][ic]+uvw_dalitz(ic,ip,il); //Dalitz test
	          	  nha[is-1][iv-1]++;
	          	  inh = nha[is-1][iv-1];
	          	   adcr[is-1][iv-1][inh-1] = adc;
	          	  ftdcr[is-1][iv-1][inh-1] = tdcf;
	          	  strra[is-1][iv-1][inh-1] = ip;
	   		      ECAL_ADC.get(iss).fill(adc,ip,1.0);
	   		      hid = (int) (1e7*is+10*tid+ic*cid+il*lid);
	   		      hpix = (H2D) getDir().getDirectory(labadc[ic]).getObject("ADC"+hid); hpix.fill(adc,ip);
	   		      hid = (int) (1e7*is+10*tid+ic*cid+il*lid);
	   		      hpix = (H2D) getDir().getDirectory(labped[ic]).getObject("PED"+hid); hpix.fill(pedref-ped,ip);
	   	        }
			}
		}
        
		} 
		
		
		
		if(event.hasBank("EC::true")==true){
			EvioDataBank bank  = (EvioDataBank) event.getBank("EC::true");
			int nrows = bank.rows();
			for(int i=0; i < nrows; i++){
				mc_t = bank.getDouble("avgT",i);
			}	
		}
					
		if(event.hasBank("EC::dgtz")==true){
			
			inMC = true;	// Processing MC banks
			
			int tdcc;
			EvioDataBank bank = (EvioDataBank) event.getBank("EC::dgtz");
			
			for(int i = 0; i < bank.rows(); i++){
				float dum = (float)bank.getInt("TDC",i)-(float)mc_t*1000;
				if (dum<tdcmax) tdcmax=dum;
			}	    		
	        
	    for(int i = 0; i < bank.rows(); i++){
	    	int is  = bank.getInt("sector",i);
        	int ip  = bank.getInt("strip",i);
			int ic  = bank.getInt("stack",i);	 
         	int il  = bank.getInt("view",i);  
			    adc = bank.getInt("ADC",i);
        	   tdcc = bank.getInt("TDC",i);
        	   tdcf = tdcc;
			//System.out.println("sector,layer,stack,pmt,adc,tdc= "+is+" "+il+" "+ic+" "+ip+" "+adc+" "+tdcc);
		     
        	tdc=(((float)tdcc-(float)mc_t*1000)-tdcmax+1340000)/1000;
		   	        	
		    if(ic==1||ic==2){
	        	int  iv = ic*3+il;
	         	int iss = (is-1)*10+il+(ic-1)*3;
	   	        if(tdc>0){
		          uvwt[is-1][ic]=uvwt[is-1][ic]+uvw_dalitz(ic,ip,il); //Dalitz test
		          nht[is-1][iv-1]++;
		          inh = nht[is-1][iv-1];
		           tdcr[is-1][iv-1][inh-1] = tdc;
		          strrt[is-1][iv-1][inh-1] = ip;
		          ECAL_TDC.get(iss).fill(tdc,ip,1.0);
		   	    }
		   	    if(adc>thr){
		          uvwa[is-1][ic]=uvwa[is-1][ic]+uvw_dalitz(ic,ip,il); //Dalitz test
		          nha[is-1][iv-1]++;
		          inh = nha[is-1][iv-1];
		           adcr[is-1][iv-1][inh-1] = adc;
		          ftdcr[is-1][iv-1][inh-1] = tdcf;
		          strra[is-1][iv-1][inh-1] = ip;
		          if (is==2){
		   		  ECAL_ADC.get(iss).fill(adc,ip,1.0);
	   		      hid = (int) (1e7*is+10*tid+ic*cid+il*lid);
	   		      hpix = (H2D) getDir().getDirectory(labadc[ic]).getObject("ADC"+hid); hpix.fill(adc,ip);
		          }
		   	    }		    	
		    }
		}
		}
		
		//Process pixel data
		boolean good_ua, good_va, good_wa, good_uvwa;
		boolean good_ut, good_vt, good_wt, good_uvwt;
		boolean good_uvwt_save=false;
		int iic,l1,l2,ist;
		TreeMap<Integer, Object> map= (TreeMap<Integer, Object>) LAYMAP.get(0);
		double pixelLength[] = (double[]) map.get(1);
		
		for (int is=0 ; is<6 ; is++) {		
			for (int ic=1; ic<3 ; ic++) {
				iic=ic*3; l1=iic-2; l2=iic+1; ist=is*10+ic+6;
				
				good_ua = nha[is][iic+0]==1;
				good_va = nha[is][iic+1]==1;
				good_wa = nha[is][iic+2]==1;
				good_ut = nht[is][iic+0]==1;
				good_vt = nht[is][iic+1]==1;
				good_wt = nht[is][iic+2]==1;
			
				good_uvwa = good_ua && good_va && good_wa; //Multiplicity test (NU=NV=NW=1)
				good_uvwt = good_ut && good_vt && good_wt; //Multiplicity test (NU=NV=NW=1)		   			

				if (good_uvwa && (uvwa[is][ic]-2.0)>0.02 && (uvwa[is][ic]-2.0)<0.056) { 
					int pixela=pix(strra[is][iic+0][0],strra[is][iic+1][0],strra[is][iic+2][0]);
					ECAL_EVTPIXA.get(ist).fill(pixela,1.0);
					
					for (int il=l1; il<l2 ; il++){
						int iss = is*10+il;
						double adcc = adcr[is][il+2][0]/pixelLength[pixela-1];
						//double adcc = adcr[is][il+2][0];
						ECAL_ADC_PIX.get(iss).fill(adcc,strra[is][il+2][0],1.0);
						ECAL_PIXASUM.get(ist).fill(pixela,adcc);
						ECAL_ADCPIX.get(iss).fill(pixela,adcc);
						ECAL_ADCPIX2.get(iss).fill(pixela,Math.pow(adcc,2));
						ECAL_APIX.get(iss).fill(pixela,adcc,1.0);
						if (good_uvwt&&is==1) {
							if(l1==1) good_uvwt_save = good_uvwt;
							if(l1==4 && good_uvwt_save){
								double dtiff1=tdcr[is][il-1][0]-tdcr[is][il+2][0];
								double dtiff2=ftdcr[is][il-1][0]-ftdcr[is][il+2][0];
							//System.out.println("il A:ip T:ip "+il+" "+strra[is][il+2][0]+" "+strrt[is][il+2][0]);
							//System.out.println("ftdc,tdc,diff1,diff2="+ftdcr[is][il+2][0]+" "+tdcr[is][il+2][0]+" "+dtiff1+" "+dtiff2);
							//System.out.println(" ");
							int iil=il ; if (iil>3) iil=iil-3;
				   		    hid = (int) (1e7*(is+1)+10*tid+ic*cid+iil*lid);
				   		    hpix = (H2D) getDir().getDirectory(labtdc[ic]).getObject("TDC"+hid);
				   		    hpix.fill(dtiff1,strra[is][il+2][0]);
				   		    hid = (int) (1e7*(is+1)+11*tid+ic*cid+iil*lid);
				   		    hpix = (H2D) getDir().getDirectory(labtdc[ic]).getObject("TDC"+hid);
				   		    hpix.fill(dtiff2,strra[is][il+2][0]);
							}
						}
					}
				}	
			
				if (good_uvwt && (uvwt[is][ic]-2.0)>0.02 && (uvwt[is][ic]-2.0)<0.056) { 
					int pixelt=pix(strrt[is][iic+0][0],strrt[is][iic+1][0],strrt[is][iic+2][0]);
					ECAL_EVTPIXT.get(ist).fill(pixelt,1.0);
					for (int il=l1; il<l2 ; il++){
						int iss = is*10+il;
						ECAL_TDC_PIX.get(iss).fill(tdcr[is][il+2][0],strrt[is][il+2][0],1.0);
						ECAL_PIXTSUM.get(ist).fill(pixelt,tdcr[is][il+2][0]);
						ECAL_TDCPIX.get(iss).fill(pixelt,tdcr[is][il+2][0]);
						ECAL_TPIX.get(iss).fill(pixelt,tdcr[is][il+2][0],1.0);
					}
				}	
			}
		}
	}
 
	public void update(DetectorShape2D shape) {
		
		int is        = shape.getDescriptor().getSector();
		int layer     = shape.getDescriptor().getLayer();
		int component = shape.getDescriptor().getComponent();
		
		int panel = app.view.panel1.omap;	
		int io    = app.view.panel1.ilmap;
		int of    = (io-1)*3;
		int lay=0;
		
		if (layer<4)  lay = layer+of;
		if (layer==4) lay = layer+2+io;
		if (panel==9) lay = panel+io-1;
		if (panel>10) lay = panel+of;
		
		layer = lay;
		
		double colorfraction=1;
		
		if (inProcess==0){ // Assign default colors upon starting GUI (before event processing)
			if(layer<7) colorfraction = (double)component/36;
			if(layer>=7) colorfraction = getcolor((TreeMap<Integer, Object>) LAYMAP.get(0), component);
		} else {   		  // Use LAYMAP to get colors of components while processing data
			             colorfraction = getcolor((TreeMap<Integer, Object>) LAYMAP.get(is*50+layer), component);
		}
		if (colorfraction<0.05) colorfraction = 0.05;
		
		Color col = palette.getRange(colorfraction);
		shape.setColor(col.getRed(),col.getGreen(),col.getBlue());
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
		 
		if (color>1)   color=1;
		if (color<=0)  color=0.;

		return color;
	}
	
	public void analyzeOccupancy() {
	
		int is10,ist,is50;
		
		for (int is=0;is<6;is++) {
			is10=is*10; is50=is*50;
			for (int il=1 ; il<7 ; il++) {
				if (il<4) ist=is10+7 ; else ist=is10+8 ;
				ECAL_ADCPIX.get(is10+il).divide(ECAL_EVTPIXA.get(ist),ECAL_PIXA.get(is10+il));
				ECAL_TDCPIX.get(is10+il).divide(ECAL_EVTPIXT.get(ist),ECAL_PIXT.get(is10+il));
				LAYMAP.put(il+is50,    toTreeMap(ECAL_ADC.get(is10+il).projectionY().getData()));  
				LAYMAP.put(il+10+is50, toTreeMap(ECAL_PIXA.get(is10+il).getData()));  
				LAYMAP.put(il+18+is50, toTreeMap(ECAL_PIXT.get(is10+il).getData()));  
				ECAL_ADCPIX2.get(is10+il).divide(ECAL_EVTPIXA.get(ist),ECAL_PIXA2.get(is10+il));
			}		    
			for (int il=7; il<9; il++) {			
				ECAL_PIXASUM.get(is10+il).divide(ECAL_EVTPIXA.get(is10+il),ECAL_PIXAS.get(is10+il));
				ECAL_PIXTSUM.get(is10+il).divide(ECAL_EVTPIXT.get(is10+il),ECAL_PIXTS.get(is10+il));
			}
	    	LAYMAP.put(7+is50,  toTreeMap(ECAL_EVTPIXA.get(is10+7).getData())); 
	    	LAYMAP.put(8+is50,  toTreeMap(ECAL_EVTPIXA.get(is10+8).getData())); 
		    LAYMAP.put(9+is50,  toTreeMap(ECAL_PIXAS.get(is10+7).getData()));    
		    LAYMAP.put(10+is50, toTreeMap(ECAL_PIXAS.get(is10+8).getData()));    
		    LAYMAP.put(17+is50, toTreeMap(ECAL_PIXTS.get(is10+7).getData()));
		    LAYMAP.put(18+is50, toTreeMap(ECAL_PIXTS.get(is10+8).getData()));
		}
	}
	
	public void analyzeAttenuation(int is1, int is2, int il1, int il2, int ip1, int ip2) {
		
		TreeMap<Integer, Object> map;
		CalibrationData fits ; 	
		boolean doCalibration=false;
		double meanerr[] = new double[1296];
		int ist;
		
		for (int is=is1 ; is<is2 ; is++) {
			for (int il=il1 ; il<il2 ; il++) {
				if (il<4) ist=is*10+7 ; else ist=is*10+8 ;
				double cnts[]  = ECAL_EVTPIXA.get(ist).getData();
				double adc[]   = ECAL_PIXA.get(is*10+il).getData();
				double adcsq[] = ECAL_PIXA2.get(is*10+il).getData();
				doCalibration = false;
				for (int ipix=0 ; ipix<1296 ; ipix++){
					meanerr[ipix]=0;
					//if (is==1) System.out.println("il,ipix,cnts,adc = "+il+" "+ipix+" "+cnts[ipix]+" "+adc[ipix]);
					if (cnts[ipix]>2) {
						meanerr[ipix]=Math.sqrt((adcsq[ipix]-adc[ipix]*adc[ipix])/(cnts[ipix]-1));
						doCalibration = true;
					}
					if (cnts[ipix]==2) {
						meanerr[ipix]=20.;
						doCalibration = true;
					}					
					if (cnts[ipix]==1) {
						meanerr[ipix]=50.;
						doCalibration = true;
					}
					  
				}
				
				map = (TreeMap<Integer, Object>)  LAYMAP.get(is*50+10+il);
				double meanmap[] = (double[]) map.get(1);
				
				for (int ip=ip1 ; ip<ip2 ; ip++){
					if (doCalibration){
						fits = new CalibrationData(is,il,ip);
						fits.getDescriptor().setType(DetectorType.EC);
						fits.addGraph(this.getpixels(il,ip+1,meanmap),this.getpixels(il,ip+1,meanerr));
						fits.analyze();
						collection.add(fits.getDescriptor(),fits);
					}
				}
			}
		}
		
	}
	
	public void detectorSelected(DetectorDescriptor desc) {
		
		this.analyze(inProcess);
		this.canvasOccupancy(desc, app.canvas0);
		this.canvasAttenuation(desc, app.canvas1);	
		this.canvasPedestal(desc, app.canvas2);	
		this.canvasTiming(desc, app.canvas3);	
	}
	
	public void analyze(int process) {
		
		this.inProcess = process;
		if (process==1)  this.analyzeOccupancy();	 //Don't analyze until event counter sets process flag
		if (process==2)  this.analyzeOccupancy();	 //Final analysis for end of run 		
	}
	
	public void canvasPedestal(DetectorDescriptor desc, EmbeddedCanvas canvas) {
		
		String otab[][]={{"U STRIPS","V STRIPS","W STRIPS"},{"Ui STRIPS","Vi STRIPS","Wi STRIPS"},{"Uo STRIPS","Vo STRIPS","Wo STRIPS"}};
			 		
		int is = desc.getSector()+1;
		int la = desc.getLayer();
		int ip = desc.getComponent();
		
		int panel = app.view.panel1.omap;
		int io    = app.view.panel1.ilmap;
		int ic    = io;
		int col2=2,col4=4,col0=0;
		H1D h;
		
		if (is==2) {
	    for(int il=1;il<4;il++){
    		int hid = (int) (1e7*is+10*tid+ic*cid+il*lid);
    		H2D hpix = (H2D) getDir().getDirectory(labped[ic]).getObject("PED"+hid); 
    		hpix.setXTitle("PED (Ref-Measured)") ; hpix.setYTitle(otab[ic][il-1]);
    		canvas.cd(il-1); canvas.setLogZ(); canvas.draw(hpix);
    		if(la==il) {
    			F1D f1 = new F1D("p0",-10.,10.); f1.setParameter(0,ip);
    			F1D f2 = new F1D("p0",-10.,10.); f2.setParameter(0,ip+1);
    			f1.setLineColor(2); canvas.draw(f1,"same"); 
    			f2.setLineColor(2); canvas.draw(f2,"same");

    		}
    		canvas.cd(il-1+3); h=hpix.sliceY(22) ; h.setFillColor(4) ; canvas.draw(h);
    	    if(la==il) {h=hpix.sliceY(ip); h.setFillColor(2) ; canvas.draw(h);}
	    }
	    
		}
			
	}
	public void canvasTiming(DetectorDescriptor desc, EmbeddedCanvas canvas) {
		
		String otab[][]={{"U STRIPS","V STRIPS","W STRIPS"},{"Ui STRIPS","Vi STRIPS","Wi STRIPS"},{"Uo STRIPS","Vo STRIPS","Wo STRIPS"}};
			 		
		int is = desc.getSector()+1;
		int la = desc.getLayer();
		int ip = desc.getComponent();
		
		int panel = app.view.panel1.omap;
		int io    = app.view.panel1.ilmap;
		int ic    = io;
		int col2=2,col4=4,col0=0;
		H1D h;
		
		if (is==2) {
	    for(int il=1;il<4;il++){
    		int hid = (int) (1e7*is+11*tid+ic*cid+il*lid);
    		H2D hpix = (H2D) getDir().getDirectory(labtdc[ic]).getObject("TDC"+hid); 
    		hpix.setXTitle("TDIF (Inner-Outer)") ; hpix.setYTitle(otab[ic][il-1]);
    		canvas.cd(il-1); canvas.setLogZ(); canvas.draw(hpix);
    		if(la==il) {
    			F1D f1 = new F1D("p0",-15.,15.); f1.setParameter(0,ip);
    			F1D f2 = new F1D("p0",-15.,15.); f2.setParameter(0,ip+1);
    			f1.setLineColor(2); canvas.draw(f1,"same"); 
    			f2.setLineColor(2); canvas.draw(f2,"same");

    		}
    		canvas.cd(il-1+3); h=hpix.sliceY(22) ; h.setFillColor(4) ; canvas.draw(h);
    	    if(la==il) {h=hpix.sliceY(ip); h.setFillColor(2) ; canvas.draw(h);}
	    }
	    
		}
			
	}	
	
	public void canvasAttenuation(DetectorDescriptor desc, EmbeddedCanvas canvas) {
		
	    double[] xp     = new double[36];
	    double[] xpe    = new double[36];
	    double[] yp     = new double[36]; 
	    double[] vgain  = new double[36];
	    double[] vgaine = new double[36]; 
	    double[] vatt   = new double[36];
	    double[] vatte  = new double[36]; 
	    double[] vchi2  = new double[36];
	    double[] vchi2e = new double[36]; 
	    double[] mip    = {100.,160.};
	    
		String otab[]={"Ui STRIPS","Vi STRIPS","Wi STRIPS","Uo STRIPS","Vo STRIPS","Wo STRIPS"};
	    
	    int is        = desc.getSector();
		int layer     = desc.getLayer();
		int component = desc.getComponent();
		
		int panel = app.view.panel1.omap;
		int io    = app.view.panel1.ilmap;
		int of    = (io-1)*3;
		int lay=0;
		
		if (layer<4)  lay = layer+of;
		if (layer==4) lay = layer+2+io;
		if (panel==9) lay = panel+io-1;
		if (panel>10) lay = panel+of;
		
		layer = lay;
		
		//		GraphErrors gainGraph, attGraph;
        if (inProcess==2) {this.analyzeAttenuation(0,6,1,7,0,36);inProcess=3;}
		 
		if (layer<7) {
			if (inProcess>0) {
			    if (inProcess==1)  {this.analyzeAttenuation(is,is+1,layer,layer+1,0,36);}
				canvas.divide(2,2);

				canvas.cd(0);
				if (collection.hasEntry(is, layer, component)) {
					
				canvas.draw(collection.get(is,layer,component).getRawGraph(0));
				canvas.draw(collection.get(is,layer,component).getFitGraph(0),"same");
				canvas.draw(collection.get(is,layer,component).getFunc(0),"same");
				
				for (int ip=0; ip<36 ; ip++) {
					double gain  = collection.get(1,layer,ip).getFunc(0).getParameter(0);
					double gaine = collection.get(1,layer,ip).getFunc(0).parameter(0).error();	
					double att   = -collection.get(1,layer,ip).getFunc(0).getParameter(1);
					double atte  = collection.get(1,layer,ip).getFunc(0).parameter(1).error();
					double chi2  = collection.get(1,layer,ip).getChi2(0);
					
					if (att>0) att=1./att; else att=0 ; 
					atte = Math.min(30,att*att*atte);
					xp[ip]=ip ; xpe[ip]=0. ; 
					if (gain>0) gaine = Math.min(30,gaine); vgain[ip]  = gain; vgaine[ip] = gaine;
		            vatt[ip] = Math.min(80, att) ; vatte[ip]=atte;
		            vchi2[ip] = Math.min(4, chi2) ; vchi2e[ip]=0.;   
				}
				
	            GraphErrors gainGraph = new GraphErrors(xp,vgain,xpe,vgaine);
	            GraphErrors  attGraph = new GraphErrors(xp,vatt,xpe,vatte);
	            GraphErrors chi2Graph = new GraphErrors(xp,vchi2,xpe,vchi2e);
	             
	            gainGraph.setMarkerStyle(2); gainGraph.setMarkerSize(6); gainGraph.setMarkerColor(2);
	             attGraph.setMarkerStyle(2);  attGraph.setMarkerSize(6);  attGraph.setMarkerColor(2);
	            chi2Graph.setMarkerStyle(2); chi2Graph.setMarkerSize(6); chi2Graph.setMarkerColor(2);
	            gainGraph.setXTitle(otab[lay-1]) ; gainGraph.setYTitle("PMT GAIN")      ; gainGraph.setTitle(" ");
	             attGraph.setXTitle(otab[lay-1]) ;  attGraph.setYTitle("ATTENUATION")   ;  attGraph.setTitle(" ");
		        chi2Graph.setXTitle(otab[lay-1]) ; chi2Graph.setYTitle("REDUCED CHI^2") ; chi2Graph.setTitle(" ");
		        
	            F1D f1 = new F1D("p0",0,37); f1.setParameter(0,mip[io-1]); f1.setLineStyle(2);
		        
	            canvas.cd(1);           canvas.getPad().setAxisRange(-1.,37.,0.,4.)   ; canvas.draw(chi2Graph); 
	            canvas.cd(2); if(!inMC) canvas.getPad().setAxisRange(-1.,37.,0.,400.) ; canvas.draw(gainGraph); canvas.draw(f1,"same"); 
	            canvas.cd(3);           canvas.getPad().setAxisRange(-1.,37.,0.,100.) ; canvas.draw(attGraph); 
	            
				}
			}
		}
	}
	
	public void canvasOccupancy(DetectorDescriptor desc, EmbeddedCanvas canvas) {
				
		int is        = desc.getSector();
		int layer     = desc.getLayer();
		int component = desc.getComponent();
		
		int panel = app.view.panel1.omap;
		int io    = app.view.panel1.ilmap;
		int of    = (io-1)*3;
		int lay=0;
		
		if (layer<4)  lay = layer+of;
		if (layer==4) lay = layer+2+io;
		if (panel==9) lay = panel+io-1;
		if (panel>10) lay = panel+of;
		
		layer = lay;
		int l1 = of+1;
		int l2 = of+4;
		
		//System.out.println("layer,panel,io,of,l1,l2= "+layer+" "+panel+" "+io+" "+of+" "+l1+" "+l2);
		
		int l,col0=0,col1=0,col2=0,strip=0,pixel=0;
		String alab[]={"Ui ADC","Vi ADC","Wi ADC","Uo ADC","Vo ADC","Wo ADC"};
		String tlab[]={"Ui TDC","Vi TDC","Wi TDC","Uo TDC","Vo TDC","Wo TDC"};
		String otab[]={"Ui STRIPS","Vi STRIPS","Wi STRIPS","Uo STRIPS","Vo STRIPS","Wo STRIPS"};
		H1D h;
		//TStyle.setOptStat(false);
	    TStyle.setStatBoxFont(TStyle.getStatBoxFontName(),12);
	    		
		if (layer<7)  {col0=0 ; col1=4; col2=2;strip=component;}
		if (layer>=7) {col0=4 ; col1=4; col2=2;pixel=component;}
		
	    canvas.divide(3,3);
	    
	    for(int il=l1;il<l2;il++){
	    	canvas.cd(il-1-of); h = ECAL_ADC.get(is*10+il).projectionY(); h.setXTitle(otab[il-1]); h.setFillColor(col0); canvas.draw(h);
	    }
	    
	    l=layer;
	    
	    if (layer<7) {
	    	 canvas.cd(l-1-of); h = ECAL_ADC.get(is*10+l).projectionY(); h.setFillColor(col1); canvas.draw(h,"same");
	         H1D copy = h.histClone("Copy"); copy.reset() ; 
	         copy.setBinContent(strip, h.getBinContent(strip)); copy.setFillColor(col2); canvas.draw(copy,"same");
	         for(int il=l1;il<l2;il++) {
			     if(layer!=il) {canvas.cd(il+2-of); h = ECAL_ADC.get(is*10+il).sliceY(22); h.setXTitle(alab[il-1]); h.setFillColor(col0); canvas.draw(h);}
				 if(layer!=il) {canvas.cd(il+5-of); h = ECAL_TDC.get(is*10+il).sliceY(22); h.setXTitle(tlab[il-1]); h.setFillColor(col0); canvas.draw(h);}
	         }
			 canvas.cd(l+2-of); h = ECAL_ADC.get(is*10+l).sliceY(strip);h.setXTitle(alab[l-1]); h.setFillColor(col2); canvas.draw(h);
			 canvas.cd(l+5-of); h = ECAL_TDC.get(is*10+l).sliceY(strip);h.setXTitle(tlab[l-1]); h.setFillColor(col2); canvas.draw(h);
	    }
	    
	    if (layer==7||layer==8) {
	    	for(int il=l1;il<l2;il++) {
	    		canvas.cd(il-1-of); h = ECAL_ADC.get(is*10+il).projectionY();
	    		H1D copy = h.histClone("Copy");
	    		copy.reset() ; copy.setBinContent(pixmap[il-1-of][pixel]-1, h.getBinContent(pixmap[il-1-of][pixel]-1));
	    		copy.setFillColor(col2); canvas.draw(copy,"same");	    		 
	    		canvas.cd(il+2-of) ; h = ECAL_ADC.get(is*10+il).sliceY(pixmap[il-1-of][pixel]-1); h.setXTitle(alab[il-1]); h.setFillColor(col2); canvas.draw(h);
	    		canvas.cd(il+5-of) ; h = ECAL_TDC.get(is*10+il).sliceY(pixmap[il-1-of][pixel]-1); h.setXTitle(tlab[il-1]); h.setFillColor(col2); canvas.draw(h);
	    	}
	    }
	    
		if (layer>8) {
			for(int il=l1;il<l2;il++) {
				canvas.cd(il-1-of); h = ECAL_ADC_PIX.get(is*10+il).projectionY();
	 		    H1D copy = h.histClone("Copy");
			    copy.reset() ; copy.setBinContent(pixmap[il-1-of][pixel]-1, h.getBinContent(pixmap[il-1-of][pixel]-1));
			    copy.setFillColor(col2); canvas.draw(copy,"same");	
			    if (layer<17) {
	    		 canvas.cd(il+2-of) ; h = ECAL_ADC_PIX.get(is*10+il).sliceY(pixmap[il-1-of][pixel]-1); h.setXTitle(alab[il-1]); h.setFillColor(col2); canvas.draw(h);
	    		 canvas.cd(il+5-of) ; h =    ECAL_APIX.get(is*10+il).sliceX(pixel)                   ; h.setXTitle(alab[il-1]); h.setFillColor(col2); canvas.draw(h);
			    }
			    if (layer>16&&layer<22) {
	    		 canvas.cd(il+2-of) ; h = ECAL_TDC_PIX.get(is*10+il).sliceY(pixmap[il-1-of][pixel]-1); h.setXTitle(tlab[il-1]); h.setFillColor(col2); canvas.draw(h);
	    		 canvas.cd(il+5-of) ; h =    ECAL_TPIX.get(is*10+il).sliceX(pixel)                   ; h.setXTitle(tlab[il-1]); h.setFillColor(col2); canvas.draw(h);
			    }
			}  	 
	     }
   
	}
	
	public static void main(String[] args){
		
		monitor = new ECMon(args);
		
	    SwingUtilities.invokeLater(new Runnable() {
	    	public void run() {
	    		app = new MonitorApp();
	    		app.setPluginClass(monitor);	    		
	    		monitor.init();
	    		monitor.initDetector(0,6);
	    	}
	    });
	}

}
