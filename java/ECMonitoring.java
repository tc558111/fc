package org.jlab.mon;

import org.jlab.geom.gui.*;
import org.jlab.clasrec.utils.*;
import org.jlab.clasrec.ui.*;
import org.root.pad.*;
import org.root.histogram.*;
import org.root.attr.ColorPalette;

import java.awt.Color;
import java.util.TreeMap;
import java.util.Arrays;

import org.jlab.evio.clas12.*;
import org.jlab.clasrec.main.*;
		
public class ECMonitoring extends DetectorMonitoring {
	
	double ec_xpix[][] = new double[3][1296];
	double ec_ypix[][] = new double[3][1296];
	double ec_cthpix[] = new double[1296];
	int pixmap[][]     = new int[3][1296];
	int inProcess      = 0;
	
	public static ECMonitoring monitor;
	public static DetectorBrowserApp app;
	
	ColorPalette palette = new ColorPalette();
	
    public TreeMap<Integer,H1D>      ECAL_ADCPIX  = new TreeMap<Integer,H1D>();
    public TreeMap<Integer,H1D>      ECAL_TDCPIX  = new TreeMap<Integer,H1D>();
    public TreeMap<Integer,H1D>      ECAL_EVTPIX  = new TreeMap<Integer,H1D>();
    public TreeMap<Integer,H2D>      ECAL_ADC     = new TreeMap<Integer,H2D>();
    public TreeMap<Integer,H2D>      ECAL_TDC     = new TreeMap<Integer,H2D>();
    public TreeMap<Integer,H2D>      ECAL_ADC_PIX = new TreeMap<Integer,H2D>();
    public TreeMap<Integer,H2D>      ECAL_TDC_PIX = new TreeMap<Integer,H2D>();
    public TreeMap<Integer,Object>   LAYMAP       = new TreeMap<Integer,Object>();

	public ECMonitoring() {
		super("ECMON","1.0","lcsmith");
	}

	@Override

	public void init() {
		for (int lay=1 ; lay<4 ; lay++) {
			ECAL_ADC.put(lay, new H2D("ADC_LAYER_"+lay,100,0.0,200.0,36,1.0,37.0));
    	    ECAL_TDC.put(lay, new H2D("TDC_LAYER_"+lay,100,0.0,1000.0,36,1.0,37.0));  
			ECAL_ADC_PIX.put(lay, new H2D("ADC_PIX_LAYER_"+lay,100,0.0,200.0,36,1.0,37.0));
    	    ECAL_TDC_PIX.put(lay, new H2D("TDC_PIX_LAYER_"+lay,100,0.0,1000.0,36,1.0,37.0));  
    		ECAL_ADCPIX.put(lay, new H1D("ADC_PIX"+lay,1296,1.0,1297.0));
    		ECAL_TDCPIX.put(lay, new H1D("ADC_PIX"+lay,1296,1.0,1297.0));
    		ECAL_EVTPIX.put(lay, new H1D("ADC_PIX"+lay,1296,1.0,1297.0));
    		System.out.println("init():lay="+lay);
		}
	}
	
	public void initecgui() {
		System.out.println("initecgui():");
		palette.set(3);
		ecpixdef();
		ecpixang();
		ecpixmap();
		LAYMAP.put(7, toTreeMap(ec_cthpix));
		
	    for(int ip=0; ip<36 ; ip++){
	    	DetectorShape3D shape = monitor.getStrip(1,1,ip);
		    app.addDetectorShape("EC UI",shape);
		    }
	    
	    for(int ip=0; ip<36 ; ip++){
	    	DetectorShape3D shape = monitor.getStrip(1,2,ip);
		    app.addDetectorShape("EC VI",shape);
		    }
	    
	    for(int ip=0; ip<36 ; ip++){
	    	DetectorShape3D shape = monitor.getStrip(1,3,ip);
		    app.addDetectorShape("EC WI",shape);
		    }
	    
	    for(int ip=0; ip<1296 ; ip++){
	       DetectorShape3D shape = monitor.getPixel(1,7,ip);
	       app.addDetectorShape("ECP",shape);
	       }
	    
	    for(int ip=0; ip<1296 ; ip++){
		   DetectorShape3D shape = monitor.getPixel(1,8,ip);
		   app.addDetectorShape("ECP3A",shape);
		}
	    for(int ip=0; ip<1296 ; ip++){
		   DetectorShape3D shape = monitor.getPixel(1,9,ip);
		   app.addDetectorShape("ECP3UA",shape);
		}
	    for(int ip=0; ip<1296 ; ip++){
		   DetectorShape3D shape = monitor.getPixel(1,10,ip);
		   app.addDetectorShape("ECP3VA",shape);
		}
	    for(int ip=0; ip<1296 ; ip++){
		   DetectorShape3D shape = monitor.getPixel(1,11,ip);
		   app.addDetectorShape("ECP3WA",shape);
		}    
	    for(int ip=0; ip<1296 ; ip++){
		   DetectorShape3D shape = monitor.getPixel(1,12,ip);
		   app.addDetectorShape("ECP3T",shape);
		}
	    for(int ip=0; ip<1296 ; ip++){
		   DetectorShape3D shape = monitor.getPixel(1,13,ip);
		   app.addDetectorShape("ECP3UT",shape);
		}
	    for(int ip=0; ip<1296 ; ip++){
		   DetectorShape3D shape = monitor.getPixel(1,14,ip);
		   app.addDetectorShape("ECP3VT",shape);
		}
	    for(int ip=0; ip<1296 ; ip++){
		   DetectorShape3D shape = monitor.getPixel(1,15,ip);
		   app.addDetectorShape("ECP3WT",shape);
		}    
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
	                   ec_xpix[k][pixel-1]=xtmp;
	                   ec_ypix[k][pixel-1]=ytmp;
	                   if (u!=36) {
	                       ytmp=ytmp+yflip[k];
	                       ec_xpix[k][pixel+2*u-1]=xtmp;
	                       ec_ypix[k][pixel+2*u-1]=ytmp;
	                   }     
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

	public DetectorShape3D getPixel(int sector, int layer, int pixel){

	    double[] xc= new double[3];
	    double[] yc= new double[3];

	    for(int j = 0; j < 3; j++){
	    	    xc[j] = ec_xpix[j][pixel];
	    	    yc[j] = ec_ypix[j][pixel];
	    }

	    DetectorShape3D shape = new DetectorShape3D();
	    shape.SECTOR = sector;
	    shape.LAYER  = layer;
	    shape.COMPONENT = pixel;
	    shape.setPoints(xc,yc);
	    return shape;

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
	    	xc[j] = ec_xpix[pts73[layer-1][j]-1][ipix-1];
	    	yc[j] = ec_ypix[pts73[layer-1][j]-1][ipix-1];
	    	
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
	  System.out.println("Enter analyze():");
	    ECAL_ADCPIX.get(1).divide(ECAL_EVTPIX.get(1));
	    ECAL_ADCPIX.get(2).divide(ECAL_EVTPIX.get(2));
	    ECAL_ADCPIX.get(3).divide(ECAL_EVTPIX.get(3));
	    ECAL_TDCPIX.get(1).divide(ECAL_EVTPIX.get(1));
	    ECAL_TDCPIX.get(2).divide(ECAL_EVTPIX.get(2));
	    ECAL_TDCPIX.get(3).divide(ECAL_EVTPIX.get(3));
		
	    LAYMAP.put(1, toTreeMap(ECAL_ADC.get(1).projectionY().getData()));
	    LAYMAP.put(2, toTreeMap(ECAL_ADC.get(2).projectionY().getData()));
	    LAYMAP.put(3, toTreeMap(ECAL_ADC.get(3).projectionY().getData()));
	    LAYMAP.put(8, toTreeMap(ECAL_EVTPIX.get(1).getData()));
	    LAYMAP.put(9, toTreeMap(ECAL_ADCPIX.get(1).getData()));
	    LAYMAP.put(10,toTreeMap(ECAL_ADCPIX.get(2).getData()));
	    LAYMAP.put(11,toTreeMap(ECAL_ADCPIX.get(3).getData()));
	    LAYMAP.put(12,toTreeMap(ECAL_TDCPIX.get(1).getData()));
	    LAYMAP.put(13,toTreeMap(ECAL_TDCPIX.get(2).getData()));
	    LAYMAP.put(14,toTreeMap(ECAL_TDCPIX.get(3).getData()));
	  System.out.println("Leave analyze():");
	}
	
	public TreeMap<Integer, Object> toTreeMap(double dat[]) {
        TreeMap<Integer, Object> hcontainer = new TreeMap<Integer, Object>();
        hcontainer.put(1, dat);
        double[] b = Arrays.copyOf(dat, dat.length);
        Arrays.sort(b);
        double min = b[0]; double max=b[b.length-1];
        hcontainer.put(2, min);
        hcontainer.put(3, max);
        return hcontainer;
	}
	
	@Override

	public void processEvent(EvioDataEvent event) {
		
		int nh[][]         = new int[6][9];
		int strr[][][]     = new int[6][9][68]; 
		int adcr[][][]     = new int[6][9][68];
		int tdcr[][][]     = new int[6][9][68];
		
		int inh;
		int thr            = 15;
		int iis            = 5;	//Sector 5 hardwired for now
		float uvw          = 0;
		
		for (int is=0 ; is<6 ; is++) {
			for (int il=0 ; il<9 ; il++) {
				nh[is][il] = 0;
				for (int ip=0 ; ip<68 ; ip++) {
					strr[is][il][ip] = 0;
					adcr[is][il][ip] = 0;
					tdcr[is][il][ip] = 0;
				}
			}
		}
		
		inProcess=1;	
		if(event.hasBank("EC::dgtz")==true){
		uvw=0;
		EvioDataBank bank = (EvioDataBank) event.getBank("EC::dgtz");
	    int nrows = bank.rows();
		for(int i = 0; i < nrows; i++){
        	int is  = bank.getInt("sector",i);
			int ip  = bank.getInt("strip",i);
         	int ic  = bank.getInt("stack",i);	 
			int adc = bank.getInt("ADC",i);
		    int tdc = bank.getInt("TDC",i);
		    int il  = bank.getInt("view",i);  
		    
        	int  iv = ic*3+il;
        	
		    if(is==iis&&ic==1){
         	   if (adc>thr) {
          	     nh[is-1][iv-1]++;
          	     inh = nh[is-1][iv-1];
          	     adcr[is-1][iv-1][inh-1] = adc;
          	     tdcr[is-1][iv-1][inh-1] = tdc;
          	     strr[is-1][iv-1][inh-1] = ip;
          	     uvw=uvw+uvw_dalitz(ic,ip,il);
          	   }
   		    ECAL_ADC.get(il).fill(adc,ip,1.0);
   		    ECAL_TDC.get(il).fill(tdc,ip,1.0);
		    }
		    
		    }
		}
		
		boolean good_u = nh[iis-1][3]==1;
		boolean good_v = nh[iis-1][4]==1;
		boolean good_w = nh[iis-1][5]==1;
		boolean good_uvw = good_u&&good_v&&good_w;
		
		if (good_uvw&&Math.abs(uvw-2.0)<0.2) {
			int pixel=pix(strr[iis-1][3][0],strr[iis-1][4][0],strr[iis-1][5][0]);
			for (int il=1; il<4 ; il++){
				ECAL_ADC_PIX.get(il).fill(adcr[iis-1][il+2][0],strr[iis-1][il+2][0],1.0);
				ECAL_TDC_PIX.get(il).fill(tdcr[iis-1][il+2][0],strr[iis-1][il+2][0],1.0);
				ECAL_EVTPIX.get(il).fill(pixel,1.0);
				ECAL_ADCPIX.get(il).fill(pixel,adcr[iis-1][il+2][0]);
				ECAL_TDCPIX.get(il).fill(pixel,tdcr[iis-1][il+2][0]);
			}
	    }

	}
 
	public Color getColor(int sector, int layer, int component) {
		
		double colorfraction=1;
	   
		switch(inProcess) {
		case 0:
	    if(layer==1) colorfraction = (double)component/36;
	    if(layer==2) colorfraction = (double)component/36;
	    if(layer==3) colorfraction = (double)component/36;
		if(layer>=7) colorfraction = getcolor((TreeMap<Integer, Object>) LAYMAP.get(7), component); 
		break;
		case 1:
	    colorfraction = getcolor((TreeMap<Integer, Object>) LAYMAP.get(layer), component);
	    break;
		}
		
		//System.out.println("layer,component,color="+layer+" "+component+" "+colorfraction);
     
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
		if (opt==2) color=(double)(Math.log10(z)-rmin)/(rmax-rmin);      
		if (color>1) color=1;
        if (color<=0)  color=0;

		return color;
	}
	
	public void drawComponent(int sector, int layer, int component, EmbeddedCanvas canvas) {
		
		int l,col1=0,col2=0,strip=0,pixel=0;
		String alab[]={"U ADC","V ADC","W ADC"},tlab[]={"U TDC","V TDC","W TDC"};
		H1D u,v,w,h;
		
		if (inProcess==1) {
			
		 if (layer<4)  {col1=0; col2=0;strip=component-1;}
		 if (layer>=7) {col1=2; col2=4;pixel=component;}
		
	     canvas.divide(3,3);
	     l=1;canvas.cd(l-1); u = ECAL_ADC.get(l).projectionY(); u.setXTitle("U STRIPS"); u.setFillColor(col1); canvas.draw(u);
	     l=2;canvas.cd(l-1); v = ECAL_ADC.get(l).projectionY(); v.setXTitle("V STRIPS"); v.setFillColor(col1); canvas.draw(v);
	     l=3;canvas.cd(l-1); w = ECAL_ADC.get(l).projectionY(); w.setXTitle("W STRIPS"); w.setFillColor(col1); canvas.draw(w);
	     if (layer<4) {
	    	 l=layer;canvas.cd(l-1); h = ECAL_ADC.get(l).projectionY(); h.setFillColor(2); canvas.draw(h);
	         H1D copy = h.histClone("Copy");
	         copy.reset() ; copy.setBinContent(strip, h.getBinContent(strip));
	         copy.setFillColor(4); canvas.draw(copy);
	     }
	     if (layer==7) {
	    	 for(int il=0;il<3;il++) {
	    		 canvas.cd(il); h = ECAL_ADC.get(il+1).projectionY();
 		         H1D copy = h.histClone("Copy");
		         copy.reset() ; copy.setBinContent(pixmap[il][pixel]-1, h.getBinContent(pixmap[il][pixel]-1));
		         copy.setFillColor(4); canvas.draw(copy);	    		 
	    	 }
	     }
		     if (layer>7) {
		    	 for(int il=0;il<3;il++) {
		    		 canvas.cd(il); h = ECAL_ADC_PIX.get(il+1).projectionY();
	 		         H1D copy = h.histClone("Copy");
			         copy.reset() ; copy.setBinContent(pixmap[il][pixel]-1, h.getBinContent(pixmap[il][pixel]-1));
			         copy.setFillColor(4); canvas.draw(copy);		    		 
		    	 }  	 
	     }
	    
	     if (layer<4){
	     if(layer!=1) {l=1;canvas.cd(l+2); u = ECAL_ADC.get(l).sliceY(22);    u.setXTitle(alab[l-1]); u.setFillColor(col2); canvas.draw(u);}
	     if(layer!=2) {l=2;canvas.cd(l+2); v = ECAL_ADC.get(l).sliceY(22);    v.setXTitle(alab[l-1]); v.setFillColor(col2); canvas.draw(v);}
	     if(layer!=3) {l=3;canvas.cd(l+2); w = ECAL_ADC.get(l).sliceY(22);    w.setXTitle(alab[l-1]); w.setFillColor(col2); canvas.draw(w);}
	     l=layer;          canvas.cd(l+2); h = ECAL_ADC.get(l).sliceY(strip); h.setXTitle(alab[l-1]); h.setFillColor(4);    canvas.draw(h);
		 if(layer!=1) {l=1;canvas.cd(l+5); u = ECAL_TDC.get(l).sliceY(22);    u.setXTitle(tlab[l-1]); u.setFillColor(col2); canvas.draw(u);}
		 if(layer!=2) {l=2;canvas.cd(l+5); v = ECAL_TDC.get(l).sliceY(22);    v.setXTitle(tlab[l-1]); v.setFillColor(col2); canvas.draw(v);}
		 if(layer!=3) {l=3;canvas.cd(l+5); w = ECAL_TDC.get(l).sliceY(22);    w.setXTitle(tlab[l-1]); w.setFillColor(col2); canvas.draw(w);}
		 l=layer;          canvas.cd(l+5); h = ECAL_TDC.get(l).sliceY(strip); h.setXTitle(tlab[l-1]); h.setFillColor(4);    canvas.draw(h);
	     }
	     
	     if (layer==7){
	    	 for(int il=0;il<3;il++) {
	    		 canvas.cd(il+3) ; h = ECAL_ADC.get(il+1).sliceY(pixmap[il][pixel]-1); h.setXTitle(alab[il]); h.setFillColor(4); canvas.draw(h);
	    		 canvas.cd(il+6) ; h = ECAL_TDC.get(il+1).sliceY(pixmap[il][pixel]-1); h.setXTitle(tlab[il]); h.setFillColor(4); canvas.draw(h);
	    	 }
		 }
	     if (layer>7){
	    	 for(int il=0;il<3;il++) {
	    		 canvas.cd(il+3) ; h = ECAL_ADC_PIX.get(il+1).sliceY(pixmap[il][pixel]-1); h.setXTitle(alab[il]); h.setFillColor(4); canvas.draw(h);
	    		 canvas.cd(il+6) ; h = ECAL_TDC_PIX.get(il+1).sliceY(pixmap[il][pixel]-1); h.setXTitle(tlab[il]); h.setFillColor(4); canvas.draw(h);
	    	 }
		 }
		}
	}

	
	public static void main(String[] args){

	    monitor = new ECMonitoring();
	    app = new DetectorBrowserApp();
	    monitor.initecgui(); 
	    app.setPluginClass(monitor);
	    app.updateDetectorView();

	   }
}
