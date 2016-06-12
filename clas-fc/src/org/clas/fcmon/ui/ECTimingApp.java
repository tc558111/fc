package org.clas.fcmon.ui;

import java.util.TreeMap;

import org.clas.fcmon.tools.CalibrationData;
import org.clas.fcmon.tools.DetectorMonitor;
import org.clas.fcmon.tools.ECPixels;
import org.clas.fcmon.tools.MonitorApp;
import org.jlab.clas.detector.DetectorCollection;
import org.jlab.clas.detector.DetectorDescriptor;
import org.root.basic.EmbeddedCanvas;
import org.root.func.F1D;
import org.root.histogram.H1D;
import org.root.histogram.H2D;

public class ECTimingApp {
	
	ECPixels[]                                   ecPix = new ECPixels[2];
	DetectorCollection<CalibrationData>     collection = new DetectorCollection<CalibrationData>();  
	DetectorCollection<TreeMap<Integer,Object>> Lmap_a = new  DetectorCollection<TreeMap<Integer,Object>>();
	TreeMap<String, DetectorCollection<H1D>>     hmap1 = new TreeMap<String, DetectorCollection<H1D>>();
	TreeMap<String, DetectorCollection<H2D>>     hmap2 = new TreeMap<String, DetectorCollection<H2D>>();
	
	MonitorApp      app = null;
	DetectorMonitor mon = null;
	
	public ECTimingApp(ECPixels[] ecPix, DetectorCollection<CalibrationData> collection) {
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
		
		String otab[][]={{"U Strips","V Strips","W Strips"},{"U Strips","V Strips","W Strips"},{"U Strips","V Strips","W Strips"}};
			 		
		int is = dd.getSector()+1;
		int la = dd.getLayer();
		int ip = dd.getComponent();
		
		int panel = app.getDetectorView().panel1.omap;
		int io    = app.getDetectorView().panel1.ilmap;
		int ic    = io;
		int col2=2,col4=4,col0=0;
		
		H1D h;
		canvas.divide(3,2);

	    for(int il=1;il<4;il++){
			H2D hpix = hmap2.get("H2_PCt_Hist").get(is,il+(io-1)*3,4);
    		hpix.setXTitle("TDIF (Inner-Outer)") ; hpix.setYTitle(otab[ic][il-1]);
    		canvas.cd(il-1); canvas.setLogZ(); canvas.draw(hpix);
    		if(la==il) {
    			F1D f1 = new F1D("p0",-15.,15.); f1.setParameter(0,ip);
    			F1D f2 = new F1D("p0",-15.,15.); f2.setParameter(0,ip+1);
    			f1.setLineColor(2); canvas.draw(f1,"same"); 
    			f2.setLineColor(2); canvas.draw(f2,"same");
    		}
    		canvas.cd(il-1+3); 
    		            h=hpix.sliceY(22) ; h.setFillColor(4) ; h.setTitle("") ; h.setXTitle("STRIP "+22)     ; canvas.draw(h);
    	    if(la==il) {h=hpix.sliceY(ip) ; h.setFillColor(2) ; h.setTitle("") ; h.setXTitle("STRIP "+(ip+1)) ; canvas.draw(h,"S");}
	    }	
	}	
	
}
