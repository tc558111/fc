package org.clas.fcmon.tools;

import java.util.TreeMap;

import org.jlab.clas.detector.DetectorCollection;
import org.jlab.clas.detector.DetectorDescriptor;
import org.root.basic.EmbeddedCanvas;
import org.root.histogram.H1D;
import org.root.histogram.H2D;

public class FCApplication {
	
	public ECPixels[]                                   ecPix = new ECPixels[2];
	public DetectorCollection<CalibrationData>     collection = new DetectorCollection<CalibrationData>();  
	public DetectorCollection<TreeMap<Integer,Object>> Lmap_a = new  DetectorCollection<TreeMap<Integer,Object>>();
	public TreeMap<String, DetectorCollection<H1D>>     hmap1 = new TreeMap<String, DetectorCollection<H1D>>();
	public TreeMap<String, DetectorCollection<H2D>>     hmap2 = new TreeMap<String, DetectorCollection<H2D>>();
	 
	public MonitorApp      app = null;
	public DetectorMonitor mon = null;
	
	public int is,layer,ic;
	public int panel,io,of,lay,l1,l2;
	
	public FCApplication(ECPixels[] ecPix, DetectorCollection<CalibrationData> collection) {
		this.ecPix = ecPix;
		this.collection = collection;		
	}
	
	public void getDetIndices(DetectorDescriptor dd) {
        is    = dd.getSector();
        layer = dd.getLayer();
        ic    = dd.getComponent(); 	 
                
        panel = app.getDetectorView().panel1.omap;
        io    = app.getDetectorView().panel1.ilmap;
        of    = (io-1)*3;
        lay   = 0;
        
        if (layer<4)  lay = layer+of;
        if (layer==4) lay = layer+2+io;
        if (panel==9) lay = panel+io-1;
        if (panel>10) lay = panel+of;
        
        l1 = of+1;
        l2 = of+4;  
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
	
	public void analyze() {
	}
	
	public void analyze(int is1, int is2, int il1, int il2, int ip1, int ip2) {
	}
	
	public void updateCanvas(DetectorDescriptor dd, EmbeddedCanvas canvas) {		
	}
}
