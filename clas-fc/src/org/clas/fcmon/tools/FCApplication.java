package org.clas.fcmon.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.clas.tools.NoGridCanvas;
import org.jlab.clas.detector.DetectorCollection;
import org.jlab.clas.detector.DetectorDescriptor;
import org.jlab.evio.clas12.EvioDataBank;
import org.root.basic.EmbeddedCanvas;
import org.root.histogram.H1D;
import org.root.histogram.H2D;

public class FCApplication {
	
    private String                                 appName    = null;
    private List<EmbeddedCanvas>                   canvases   = new ArrayList<EmbeddedCanvas>();
    
	public ECPixels[]                                   ecPix = new ECPixels[2];
	public DetectorCollection<TreeMap<Integer,Object>> Lmap_a = new  DetectorCollection<TreeMap<Integer,Object>>();
	public TreeMap<String, DetectorCollection<H1D>>     hmap1 = new TreeMap<String, DetectorCollection<H1D>>();
	public TreeMap<String, DetectorCollection<H2D>>     hmap2 = new TreeMap<String, DetectorCollection<H2D>>();
	 
	public MonitorApp      app = null;
	public DetectorMonitor mon = null;
	
	public int is,layer,ic;
	public int panel,io,of,lay,l1,l2;
	
    public FCApplication(ECPixels[] ecPix) {
        this.ecPix = ecPix;     
    }
    
    public FCApplication(String name, ECPixels[] ecPix) {
        this.appName = name;
        this.ecPix = ecPix;   
        this.addCanvas(name);
    }
	
    public String getName() {
        return this.appName;
    }
    
    public void setName(String name) {
        this.appName = name;
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
	
	public void addH1DMaps(String name, DetectorCollection<H1D> map) {
		this.hmap1.put(name,map);
	}
	
	public void addH2DMaps(String name, DetectorCollection<H2D> map) {
		this.hmap2.put(name,map);
	}
	
	public void addLMaps(String name, DetectorCollection<TreeMap<Integer,Object>> map) {
		this.Lmap_a=map;
	}
	
	public void setMonitoringClass(MonitorApp app) {
		this.app = app;
	}
	
	public void setApplicationClass(DetectorMonitor mon) {
		this.mon = mon;
	}
	
	public void process(EvioDataBank bank) {
	    
	}
	
	public void analyze() {
	}
	
	public void analyze(int is1, int is2, int il1, int il2, int ip1, int ip2) {
	}
	
	public void updateCanvas(DetectorDescriptor dd, EmbeddedCanvas canvas) {		
	}
	
    public final void addCanvas(String name) {
        EmbeddedCanvas c = new EmbeddedCanvas();
        this.canvases.add(c);
        this.canvases.get(this.canvases.size()-1).setName(name);
    }
    
    public EmbeddedCanvas getCanvas(int index) {
        return this.canvases.get(index);
    }
    
    public EmbeddedCanvas getCanvas(String name) {
        int index=0;
        for(int i=0; i<this.canvases.size(); i++) {
            if(this.canvases.get(i).getName() == name) {
                index=i;
                break;
            }
        }
        return this.canvases.get(index);
    }
}
