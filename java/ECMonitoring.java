package org.jlab.mon.clasmon_fc;

import org.jlab.geom.gui.*;
import org.jlab.clasrec.utils.*;
import org.jlab.clasrec.ui.*;
import org.root.pad.*;
import org.root.histogram.*;
import org.root.attr.ColorPalette;

import java.awt.Color;
import java.util.TreeMap;

import org.jlab.evio.clas12.*;
import org.jlab.clasrec.main.*;
		
public class ECMonitoring extends DetectorMonitoring {
	
	double ec_xpix[][] = new double[3][1296];
	double ec_ypix[][] = new double[3][1296];
	int[][] pts73 = {{1,2,3,1},{2,3,1,2},{3,1,2,3}};
	int[][] pts74 = {{1,2,1},{3,2,3},{3,2,3},{1,2,1}};
	int inProcess=0;
	
	ColorPalette palette = new ColorPalette();
	
    public TreeMap<Integer,H2D>      ECAL_ADC = new TreeMap<Integer,H2D>();
    public TreeMap<Integer,H2D>      ECAL_TDC = new TreeMap<Integer,H2D>();

	public ECMonitoring() {
		
		super("ECMON","1.0","lcsmith");
		initec();
		
	}

	@Override

	public void init() {
		palette.set(2);
		for (int lay=1 ; lay<4 ; lay++) {
			ECAL_ADC.put(lay, new H2D("ADC_LAYER_"+lay,100,0.0,200.0,36,1.0,37.0));
    	    ECAL_TDC.put(lay, new H2D("TDC_LAYER_"+lay,100,0.0,1000.0,36,1.0,37.0));
    	    
		}
	}

	public void initec() {

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

	@Override

	public void configure(ServiceConfiguration sc) {

	}

	@Override

	public void analyze() {

	}

	@Override

	public void processEvent(EvioDataEvent event) {
		inProcess=1;
		if(event.hasBank("EC::dgtz")==true){
		EvioDataBank bank = (EvioDataBank) event.getBank("EC::dgtz");
	    int nrows = bank.rows();
		for(int loop = 0; loop < nrows; loop++){
			int id  = bank.getInt("strip",loop);
			int adc = bank.getInt("ADC",loop);
		    int tdc = bank.getInt("TDC",loop);
		    int lay = bank.getInt("view",loop);
			ECAL_ADC.get(lay).fill(adc,id,1.0);
		    ECAL_TDC.get(lay).fill(tdc,id,1.0);
		}

		}
	}
/*	
	public getcolor(int val, float rmin, float rmax, int opt) {
		if (opt==1) {getcolor=int((val-rmin)/(rmax-rmin)*49+8);}
		if (opt==2) {getcolor=int((math.log10(val)-rmin)/(rmax-rmin)*49+8);}
		}
	return getcolor()
	}
*/
	public Color getColor(int sector, int layer, int component) {
		
		double colorfraction=1;
	   
		switch(inProcess) {
		case 0:
	    if(layer==1) colorfraction = (double)component/36;
	    if(layer==2) colorfraction = (double)component/36;
	    if(layer==3) colorfraction = (double)component/36;
		if(layer==7) colorfraction = (double)(component+1)/1296;
		break;
		case 1:
	    if(layer==1) colorfraction = (double)ECAL_ADC.get(layer).projectionY().getBinContent(component-1)/7e3;
	    if(layer==2) colorfraction = (double)ECAL_ADC.get(layer).projectionY().getBinContent(component-1)/1e4; 
	    if(layer==3) colorfraction = (double)ECAL_ADC.get(layer).projectionY().getBinContent(component-1)/1e4; 
		if(layer==7) colorfraction = (double)(component+1)/1296;
		break;
		}
        if (colorfraction>255) colorfraction=255;
        if (colorfraction<=0)  colorfraction=1;
        
		return palette.getRange(colorfraction);

	}

	public void drawComponent(int sector, int layer, int component, EmbeddedCanvas canvas){
		int l;
		H1D u,v,w,h;
		if (inProcess==1) {
	     canvas.divide(3,3);
	     l=1;canvas.cd(l-1); u = ECAL_ADC.get(l).projectionY(); u.setFillColor(0); canvas.draw(u);
	     l=2;canvas.cd(l-1); v = ECAL_ADC.get(l).projectionY(); v.setFillColor(0); canvas.draw(v);
	     l=3;canvas.cd(l-1); w = ECAL_ADC.get(l).projectionY(); w.setFillColor(0); canvas.draw(w);
	     l=layer;
	         canvas.cd(l-1); h = ECAL_ADC.get(l).projectionY(); h.setFillColor(2); canvas.draw(h);
	     H1D copy = h.histClone("Copy");
	     copy.reset() ; copy.setBinContent(component-1, h.getBinContent(component-1));
	     copy.setFillColor(4); canvas.draw(copy);
	     if(layer!=1) {l=1;canvas.cd(l+2); u = ECAL_ADC.get(l).sliceY(22); u.setFillColor(0); canvas.draw(u);}
	     if(layer!=2) {l=2;canvas.cd(l+2); v = ECAL_ADC.get(l).sliceY(22); v.setFillColor(0); canvas.draw(v);}
	     if(layer!=3) {l=3;canvas.cd(l+2); w = ECAL_ADC.get(l).sliceY(22); w.setFillColor(0); canvas.draw(w);}
	     l=layer;          canvas.cd(l+2); h = ECAL_ADC.get(l).sliceY(component-1); h.setFillColor(4); canvas.draw(h);
		 if(layer!=1) {l=1;canvas.cd(l+5); u = ECAL_TDC.get(l).sliceY(22); u.setFillColor(0); canvas.draw(u);}
		 if(layer!=2) {l=2;canvas.cd(l+5); v = ECAL_TDC.get(l).sliceY(22); v.setFillColor(0); canvas.draw(v);}
		 if(layer!=3) {l=3;canvas.cd(l+5); w = ECAL_TDC.get(l).sliceY(22); w.setFillColor(0); canvas.draw(w);}
		 l=layer;          canvas.cd(l+5); h = ECAL_TDC.get(l).sliceY(component-1); h.setFillColor(4); canvas.draw(h);	     
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
	
	public DetectorShape3D getStrip(int sector, int layer, int strip) {

		int ipix=1,pix1=1,pix2=1;
	    double[] xc= new double[5];
	    double[] yc= new double[5];
	    
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
	    shape.COMPONENT = strip;
	    shape.setPoints(xc,yc);
	    return shape;

	}
	public static void main(String[] args){

	    ECMonitoring       monitor = new ECMonitoring();
	    DetectorBrowserApp     app = new DetectorBrowserApp();
	    monitor.init();

	    for(int ip=1; ip<37 ; ip++){
	    	DetectorShape3D shape = monitor.getStrip(1,1,ip);
		    app.addDetectorShape("EC Inner U",shape);
		    }
	    
	    for(int ip=1; ip<37 ; ip++){
	    	DetectorShape3D shape = monitor.getStrip(1,2,ip);
		    app.addDetectorShape("EC Inner V",shape);
		    }
	    
	    for(int ip=1; ip<37 ; ip++){
	    	DetectorShape3D shape = monitor.getStrip(1,3,ip);
		    app.addDetectorShape("EC Inner W",shape);
		    }
	    
	    for(int ip=0; ip<1296 ; ip++){
	       DetectorShape3D shape = monitor.getPixel(1,7,ip);
	       app.addDetectorShape("EC Inner",shape);
	       }

	    for(int ip=0; ip<1296 ; ip++){
	       DetectorShape3D shape = monitor.getPixel(1,9,ip);
	       app.addDetectorShape("EC Outer",shape);
	       }

	    //app.setHistogramDraw(calib);
	    app.setPluginClass(monitor);
	    app.updateDetectorView();

	   }
}
