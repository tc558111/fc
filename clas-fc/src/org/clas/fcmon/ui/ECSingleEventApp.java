package org.clas.fcmon.ui;

import java.util.TreeMap;

import org.clas.fcmon.tools.CalibrationData;
import org.clas.fcmon.tools.DetectorMonitor;
import org.clas.fcmon.tools.ECPixels;
import org.clas.fcmon.tools.MonitorApp;
import org.jlab.clas.detector.DetectorCollection;
import org.jlab.clas.detector.DetectorDescriptor;
import org.root.basic.EmbeddedCanvas;
import org.root.histogram.H1D;
import org.root.histogram.H2D;

public class ECSingleEventApp {
	ECPixels[]                                   ecPix = new ECPixels[2];
	DetectorCollection<CalibrationData>     collection = new DetectorCollection<CalibrationData>();  
	DetectorCollection<TreeMap<Integer,Object>> Lmap_a = new  DetectorCollection<TreeMap<Integer,Object>>();
	TreeMap<String, DetectorCollection<H1D>>     hmap1 = new TreeMap<String, DetectorCollection<H1D>>();
	TreeMap<String, DetectorCollection<H2D>>     hmap2 = new TreeMap<String, DetectorCollection<H2D>>();
	
	MonitorApp      app = null;
	DetectorMonitor mon = null;
	
	public ECSingleEventApp(ECPixels[] ecPix, DetectorCollection<CalibrationData> collection) {
		this.ecPix = ecPix;
		this.collection = collection;		
	}
	
	public void addH1DMaps(String name, DetectorCollection map) {
		this.hmap1.put(name,map);
	}
	public void addH2DMaps(String name, DetectorCollection map) {
		this.hmap2.put(name,map);
	}
	public void addLMaps(String name, DetectorCollection map) {
		this.Lmap_a=map;
	}
	public void setMonitoringClass(MonitorApp app) {
		this.app = app;
	}
	public void setApplicationClass(DetectorMonitor mon) {
		this.mon = mon;
	}
	
	
	public void canvas(DetectorDescriptor dd, EmbeddedCanvas canvas) {
		
        double   zmax =  (double) mon.getGlob().get("PCMon_zmax");
        
		int is    = dd.getSector();
		int layer = dd.getLayer();
		int ic    = dd.getComponent();
		
		int panel = app.getDetectorView().panel1.omap;
		int io    = app.getDetectorView().panel1.ilmap;
		int of    = (io-1)*3;
		int lay=0;
		
		if (layer<4)  lay = layer+of;
		if (layer==4) lay = layer+2+io;
		if (panel==9) lay = panel+io-1;
		if (panel>10) lay = panel+of;
		
		layer = lay;
		int l1 = of+1;
		int l2 = of+4;	
		
		int l,col0=0,col1=0,col2=0,strip=0,pixel=0;
		String otab[]={"U Inner Strips","V Inner Strips","W Inner Strips","U Outer Strips","V Outer Strips","W Outer Strips"};
		String lab1[]={"U ","V ","W "}, lab2[]={"Inner ","Outer "}, lab3[]={"Strip ","Pixel "},lab4[]={" ADC"," TDC"};
		H1D h;
		canvas.divide(1,3);
		canvas.setAxisFontSize(14);
		canvas.setTitleFontSize(14);
		canvas.setStatBoxFontSize(12);
		
	    //TStyle.setStatBoxFont(TStyle.getStatBoxFontName(),12);
	    //TStyle.setAxisFont(TStyle.getAxisFontName(),8);
	    		
		if (layer<7)  {col0=0 ; col1=4; col2=2;strip=ic+1;}
		if (layer>=7) {col0=4 ; col1=4; col2=2;pixel=ic+1;}
    		
	    for(int il=1;il<4;il++){
	    	canvas.cd(il-1); canvas.getPad().setAxisRange(-1.,ecPix[0].pc_nstr[il-1]+1,0.,zmax*app.displayControl.pixMax);
	    	h = hmap1.get("H1_PCa_Sevd").get(is+1,il+of,0); h.setXTitle(otab[il-1+of]); h.setFillColor(col0); canvas.draw(h);
	    }
	}	
}
