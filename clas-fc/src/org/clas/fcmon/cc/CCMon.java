package org.clas.fcmon.cc;

import java.util.TreeMap;

import org.clas.fcmon.detector.view.DetectorShape2D;
import org.clas.fcmon.tools.*;

//clas12
import org.jlab.clas.detector.DetectorCollection;
import org.jlab.clas12.detector.FADCConfigLoader;
import org.jlab.clasrec.utils.DatabaseConstantProvider;
//import org.root.histogram.H1D;
//import org.root.histogram.H2D;

//clas12rec
import org.jlab.detector.base.DetectorDescriptor;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.base.DataEvent;

public class CCMon extends DetectorMonitor {
	
    static MonitorApp           app = new MonitorApp("LTCCMon",1800,950);	
    
    CCDetector                ccDet = null;
    
    CCReconstructionApp     ccRecon = null;
    CCMode1App              ccMode1 = null;
    CCOccupancyApp      ccOccupancy = null;
    CCPedestalApp        ccPedestal = null;
    CCSpeApp                  ccSpe = null;    
    CCCalibrationApp        ccCalib = null;
    
    CCPixels                  ccPix = new CCPixels();
	
    DatabaseConstantProvider   ccdb = new DatabaseConstantProvider(12,"default");
    FADCConfigLoader          fadc  = new FADCConfigLoader();
    	
    int inProcess                   = 0;     //0=init 1=processing 2=end-of-run 3=post-run
    boolean inMC                    = false; //true=MC false=DATA
    int ipsave                      = 0;
    int    detID                    = 0;
    double zmin                     = 0;
    double zmax                     = 0;
    int is1                         = 0;
    int is2                         = 6;	
    
    int nsa,nsb,tet,p1,p2,pedref    = 0;
    
    String mondet                   = "LTCC";
    
    DetectorCollection<H1F> H1_CCa_Sevd = new DetectorCollection<H1F>();
    DetectorCollection<H1F> H1_CCt_Sevd = new DetectorCollection<H1F>();
    DetectorCollection<H2F> H2_CCa_Hist = new DetectorCollection<H2F>();
    DetectorCollection<H2F> H2_CCt_Hist = new DetectorCollection<H2F>();
    DetectorCollection<H2F> H2_CCa_Sevd = new DetectorCollection<H2F>();
	
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
        monitor.initGlob();
        monitor.makeApps();
        monitor.init();
        monitor.addCanvas();
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
        
        ccSpe = new CCSpeApp("SPE",ccPix);        
        ccSpe.setMonitoringClass(this);
        ccSpe.setApplicationClass(app);  
        
        ccCalib = new CCCalibrationApp("Calibration", ccPix);
        ccCalib.setMonitoringClass(this);
        ccCalib.setApplicationClass(app);  
        ccCalib.init(is1,is2);
    }
	
    public void addCanvas() {
        System.out.println("addCanvas()"); 
        app.addCanvas(ccMode1.getName(),     ccMode1.getCanvas());
        app.addCanvas(ccOccupancy.getName(), ccOccupancy.getCanvas());          
        app.addCanvas(ccPedestal.getName(),  ccPedestal.getCanvas());
        app.addCanvas(ccSpe.getName(),       ccSpe.getCanvas()); 
        app.addFrame(ccCalib.getName(),      ccCalib.getCalibPane());
    }
    
    public void init( ) {       
        System.out.println("init()");   
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
        putGlob("zmin", zmin);
        putGlob("zmax", zmax);
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
        ccCalib.updateDetectorView(shape);
    }
		
    @Override
    public void analyze(int process) {
        this.inProcess = process; glob.put("inProcess", process);
        if (process==1||process==2) {
            ccRecon.makeMaps();	
            ccCalib.engines[0].analyze();
        }
    }

    @Override
    public void processShape(DetectorShape2D shape) {       
        DetectorDescriptor dd = shape.getDescriptor();
        this.analyze(inProcess);        
        switch (app.getSelectedTabName()) {
        case "Mode1":             ccMode1.updateCanvas(dd); break;
        case "Occupancy":     ccOccupancy.updateCanvas(dd); break;
        case "Pedestal":       ccPedestal.updateCanvas(dd); break;
        case "SPE":                 ccSpe.updateCanvas(dd); 
        }                       
    }

    @Override
    public void resetEventListener() {
    }

    @Override
    public void timerUpdate() {
    }
    
}
