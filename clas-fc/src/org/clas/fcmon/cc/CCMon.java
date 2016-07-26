package org.clas.fcmon.cc;

import java.util.TreeMap;

import org.clas.fcmon.detector.view.DetectorShape2D;
import org.clas.fcmon.tools.*;

//clas12
import org.jlab.clas.detector.DetectorCollection;
import org.jlab.clas12.detector.FADCConfigLoader;
import org.jlab.clasrec.utils.DatabaseConstantProvider;
import org.root.basic.EmbeddedCanvas;
import org.root.func.F1D;
import org.root.histogram.H1D;
import org.root.histogram.H2D;

//clas12rec
import org.jlab.detector.base.DetectorDescriptor;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.base.DataEvent;

public class CCMon extends DetectorMonitor {
	
    static MonitorApp           app = new MonitorApp("LTCCMon",1800,950);	
    
    CCDetector                ccDet = null;
    CCReconstructionApp     ccRecon = null;
    CCOccupancyApp      ccOccupancy = null;
    CCMode1App              ccMode1 = null;
    CCPedestalApp        ccPedestal = null;
    CCSummaryApp          ccSummary = null;
    
	DatabaseConstantProvider   ccdb = new DatabaseConstantProvider(12,"default");
    FADCConfigLoader          fadc  = new FADCConfigLoader();
	
	CCPixels                  ccPix = new CCPixels();
	
	int inProcess                   = 0;     //0=init 1=processing 2=end-of-run 3=post-run
	boolean inMC                    = false; //true=MC false=DATA
	int ipsave                      = 0;
    int    detID                    = 0;
    double PCMon_zmin               = 0;
    double PCMon_zmax               = 0;
    int is1                         = 0;
    int is2                         = 1;	
    
    int thrcc                       = 20;
    int nsa,nsb,tet,p1,p2,pedref    = 0;
    
    String mondet                   = "LTCC";
    
	DetectorCollection<H1D> H1_CCa_Sevd = new DetectorCollection<H1D>();
	DetectorCollection<H1D> H1_CCt_Sevd = new DetectorCollection<H1D>();
	DetectorCollection<H2D> H2_CCa_Hist = new DetectorCollection<H2D>();
	DetectorCollection<H2D> H2_CCt_Hist = new DetectorCollection<H2D>();
	DetectorCollection<H2D> H2_CCa_Sevd = new DetectorCollection<H2D>();
	
	DetectorCollection<TreeMap<Integer,Object>> Lmap_a = new DetectorCollection<TreeMap<Integer,Object>>();	 
	
	TreeMap<String,Object> glob = new TreeMap<String,Object>();
	   
	public CCMon(String det) {
		super("CCMON", "1.0", "lcsmith");
	}

	public static void main(String[] args){		
        String det = "LTCC";
		CCMon monitor = new CCMon(det);		
		app.setPluginClass(monitor);
		app.makeGUI();
        app.mode7Emulation.init("/daq/fadc/ltcc",1, 18, 12);
        monitor.makeApps();
        monitor.addCanvas();
        monitor.init();
		monitor.initDetector();
        app.init();
        monitor.ccDet.initButtons();
	}
	
	public void initDetector() {
        ccDet = new CCDetector("CCDet",ccPix);
        ccDet.setMonitoringClass(this);
        ccDet.setApplicationClass(app);
        ccDet.init(is1,is2);
        ccDet.addLMaps("Lmap_a", ccRecon.Lmap_a); 	    
	}
	
	public void makeApps() {
        System.out.println("makeApps()"); 
        ccRecon = new CCReconstructionApp("CCREC",ccPix);        
        ccRecon.setMonitoringClass(this);
        ccRecon.setApplicationClass(app);	
        
        ccMode1 = new CCMode1App("Mode1",ccPix);        
        ccMode1.setMonitoringClass(this);
        ccMode1.setApplicationClass(app);   
        
        ccOccupancy = new CCOccupancyApp("Occupancy",ccPix);        
        ccOccupancy.setMonitoringClass(this);
        ccOccupancy.setApplicationClass(app);           
        
        ccPedestal = new CCPedestalApp("Pedestal",ccPix);        
        ccPedestal.setMonitoringClass(this);
        ccPedestal.setApplicationClass(app);       
        
        ccSummary = new CCSummaryApp("Summary",ccPix);        
        ccSummary.setMonitoringClass(this);
        ccSummary.setApplicationClass(app);               
	}
	
    public void addCanvas() {
        System.out.println("addCanvas()"); 
        app.addCanvas(ccMode1.getName(),     ccMode1.getCanvas());
        app.addCanvas(ccOccupancy.getName(), ccOccupancy.getCanvas());          
        app.addCanvas(ccPedestal.getName(),  ccPedestal.getCanvas());
        app.addCanvas(ccSummary.getName(),   ccSummary.getCanvas());        
    }
    
    public void init( ) {       
        System.out.println("init()");   
        initGlob();
        initApps();
        ccPix.initHistograms();
    }

    public void initApps() {
        System.out.println("initApps()");
        ccRecon.init();
    }
    
    public void initGlob() {
        System.out.println("initGlob()");
        putGlob("inProcess", inProcess);
        putGlob("detID", detID);
        putGlob("inMC", inMC);
        putGlob("nsa", nsa);
        putGlob("nsb", nsb);
        putGlob("tet", tet);        
        putGlob("ccdb", ccdb);
        putGlob("PCMon_zmin", PCMon_zmin);
        putGlob("PCMon_zmax", PCMon_zmax);
        putGlob("fadc",fadc);
        putGlob("mondet",mondet);
        putGlob("is1",is1);
        putGlob("is2",is2);
    }
    
    @Override
	public TreeMap<String,Object> getGlob(){
		return this.glob;
	}	
    
    @Override
    public void putGlob(String name, Object obj){
        glob.put(name,obj);
    }  
    
	@Override
	public void reset() {
		ccRecon.clearHistograms();
	}
	
	@Override
	public void close() {
		
	}	
	
	@Override
	public void saveToFile() {
		
	}
	
	@Override
	public void dataEventAction(DataEvent de) {
	    ccRecon.addEvent((EvioDataEvent) de);	
	}

	@Override
	public void update(DetectorShape2D shape) {
        putGlob("inProcess", inProcess);
        ccDet.update(shape);
	}
		
	@Override
	public void analyze(int process) {
        this.inProcess = process; glob.put("inProcess", process);
		if (process==1||process==2) ccRecon.makeMaps();	
	}

    @Override
    public void processShape(DetectorShape2D shape) {       
        DetectorDescriptor dd = shape.getDescriptor();
        this.analyze(inProcess);
        
        switch (app.getSelectedTabName()) {
        case "Mode1":             ccMode1.updateCanvas(dd); break;
        case "Occupancy":     ccOccupancy.updateCanvas(dd); break;
        case "Pedestal":       ccPedestal.updateCanvas(dd); break;
        case "Summary":         ccSummary.updateCanvas(dd); 
        }      
                 
    }

    @Override
    public void resetEventListener() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void timerUpdate() {
        // TODO Auto-generated method stub
        
    }
}
