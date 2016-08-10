package org.clas.fcmon.ftof;

import java.util.TreeMap;

import org.clas.fcmon.detector.view.DetectorShape2D;
import org.clas.fcmon.tools.*;

//clas12
import org.jlab.clas.detector.DetectorCollection;
import org.jlab.clas12.detector.FADCConfigLoader;
import org.jlab.clasrec.utils.DatabaseConstantProvider;
import org.root.histogram.H1D;
import org.root.histogram.H2D;

//clas12rec
import org.jlab.detector.base.DetectorDescriptor;
//import org.jlab.groot.data.H1F;
//import org.jlab.groot.data.H2F;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.base.DataEvent;

public class FTOFMon extends DetectorMonitor {
	
    static MonitorApp             app = new MonitorApp("FTOFMon",1800,950);	
    FADCConfigLoader            fadc  = new FADCConfigLoader();
    DatabaseConstantProvider     ccdb = new DatabaseConstantProvider(12,"default");
    FTOFPixels              ftofPix[] = new FTOFPixels[3];
  
    FTOFDetector              ftofDet = null;    
    FTOFReconstructionApp   ftofRecon = null;
    FTOFMode1App            ftofMode1 = null;
    FTOFOccupancyApp    ftofOccupancy = null;
    FTOFPedestalApp      ftofPedestal = null;
    FTOFSpeApp                ftofSpe = null;    
    FTOFCalibrationApp      ftofCalib = null;
    FTOFScalersApp        ftofScalers = null;
    FTOFHvApp                  ftofHv = null;
        
    String                      myEnv = "home";
    boolean                   doEpics = true;
    String                   hipoPath = null;
    public boolean               inMC = false; //true=MC false=DATA
    public int              inProcess = 0;     //0=init 1=processing 2=end-of-run 3=post-run
    int                         detID = 0;
    int                           is1 = 1 ;
    int                           is2 = 7 ;  
    int nsa,nsb,tet,p1,p2,pedref      = 0;
    
    String mondet                     = "FTOF";
    
    DetectorCollection<H1D> H1_a_Sevd = new DetectorCollection<H1D>();
    DetectorCollection<H1D> H1_t_Sevd = new DetectorCollection<H1D>();
    DetectorCollection<H2D> H2_a_Hist = new DetectorCollection<H2D>();
    DetectorCollection<H2D> H2_t_Hist = new DetectorCollection<H2D>();
    DetectorCollection<H2D> H2_a_Sevd = new DetectorCollection<H2D>();
	
    DetectorCollection<TreeMap<Integer,Object>> Lmap_a = new DetectorCollection<TreeMap<Integer,Object>>();	 
	
    TreeMap<String,Object> glob = new TreeMap<String,Object>();
	   
    public FTOFMon(String det) {
        super("FTOFMON", "1.0", "lcsmith");
        mondet = det;
        if (mondet=="FTOF") ftofPix[0] = new FTOFPixels("PANEL1A");
        setEnv();
    }

    public static void main(String[] args){		
        String det = "FTOF";
        FTOFMon monitor = new FTOFMon(det);	
        app.setPluginClass(monitor);
        app.makeGUI();
        app.mode7Emulation.init("/daq/fadc/ftof",5, 3, 1);
        monitor.initGlob();
        monitor.makeApps();
        monitor.addCanvas();
        monitor.init();
        monitor.initDetector();
        app.init();
        monitor.ftofDet.initButtons();
    }
    
    public void setEnv() {
        if (myEnv=="hallb") {
            doEpics = true;
           hipoPath = "/home/lcsmith";
        } else {
            doEpics = false;
          hipoPath  = "/Users/colesmith";
        }
    }
    
    public void initDetector() {
        ftofDet = new FTOFDetector("FTOFDet",ftofPix);
        ftofDet.setMonitoringClass(this);
        ftofDet.setApplicationClass(app);
        ftofDet.init(is1,is2);
        ftofDet.addLMaps("Lmap_a", ftofRecon.Lmap_a); 	    
    }
	
    public void makeApps() {
        System.out.println("makeApps()"); 
        ftofRecon = new FTOFReconstructionApp("FTOFREC",ftofPix);        
        ftofRecon.setMonitoringClass(this);
        ftofRecon.setApplicationClass(app);	
        
        ftofMode1 = new FTOFMode1App("Mode1",ftofPix);        
        ftofMode1.setMonitoringClass(this);
        ftofMode1.setApplicationClass(app);   
        
        ftofOccupancy = new FTOFOccupancyApp("Occupancy",ftofPix);        
        ftofOccupancy.setMonitoringClass(this);
        ftofOccupancy.setApplicationClass(app);           
        
        ftofPedestal = new FTOFPedestalApp("Pedestal",ftofPix);        
        ftofPedestal.setMonitoringClass(this);
        ftofPedestal.setApplicationClass(app);       
        
        ftofSpe = new FTOFSpeApp("SPE",ftofPix);        
        ftofSpe.setMonitoringClass(this);
        ftofSpe.setApplicationClass(app);  
        
        ftofCalib = new FTOFCalibrationApp("Calibration", ftofPix);
        ftofCalib.setMonitoringClass(this);
        ftofCalib.setApplicationClass(app);  
        ftofCalib.init(is1,is2);
        
        ftofHv = new FTOFHvApp("HV","LTCC");
        ftofHv.setMonitoringClass(this);
        ftofHv.setApplicationClass(app);  
        
        ftofScalers = new FTOFScalersApp("Scalers","LTCC");
        ftofScalers.setMonitoringClass(this);
        ftofScalers.setApplicationClass(app);  
    }
	
    public void addCanvas() {
        System.out.println("addCanvas()"); 
        app.addCanvas(ftofMode1.getName(),         ftofMode1.getCanvas());
        app.addCanvas(ftofOccupancy.getName(), ftofOccupancy.getCanvas());          
        app.addCanvas(ftofPedestal.getName(),   ftofPedestal.getCanvas());
        app.addCanvas(ftofSpe.getName(),             ftofSpe.getCanvas()); 
        app.addFrame(ftofCalib.getName(),          ftofCalib.getCalibPane());
        app.addFrame(ftofHv.getName(),                ftofHv.getScalerPane());
        app.addFrame(ftofScalers.getName(),      ftofScalers.getScalerPane());
    }
    
    public void init( ) {       
        System.out.println("init()");   
        initApps();
        ftofPix[0].initHistograms(" ");
        H2_a_Hist = ftofPix[0].strips.hmap2.get("H2_a_Hist");
        H2_t_Hist = ftofPix[0].strips.hmap2.get("H2_t_Hist");
    }

    public void initApps() {
        System.out.println("initApps()");
        ftofRecon.init();
        if (doEpics) {
          ftofHv.init(is1,is2);        
          ftofScalers.init(is1,is2); 
        }
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
//        putGlob("zmin", zmin);
//        putGlob("zmax", zmax);
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
        ftofRecon.clearHistograms();
    }
	
    @Override
    public void close() {	
    }	
	
    @Override
    public void dataEventAction(DataEvent de) {
        ftofRecon.addEvent((EvioDataEvent) de);	
    }

    @Override
    public void update(DetectorShape2D shape) {
        putGlob("inProcess", inProcess);
        ftofDet.update(shape);
        ftofCalib.updateDetectorView(shape);
    }
		
    @Override
    public void analyze(int process) {
        this.inProcess = process; glob.put("inProcess", process);
        if (process==1||process==2) {
            ftofRecon.makeMaps();	
            ftofCalib.engines[0].analyze();
        }
    }

    @Override
    public void processShape(DetectorShape2D shape) {       
        DetectorDescriptor dd = shape.getDescriptor();
        this.analyze(inProcess);        
        switch (app.getSelectedTabName()) {
        case "Mode1":             ftofMode1.updateCanvas(dd); break;
        case "Occupancy":     ftofOccupancy.updateCanvas(dd); break;
        case "Pedestal":       ftofPedestal.updateCanvas(dd); break;
        case "SPE":                 ftofSpe.updateCanvas(dd); break; 
        case "HV":                   ftofHv.updateCanvas(dd); break;
        case "Scalers":         ftofScalers.updateCanvas(dd);
        }                       
    }

    @Override
    public void resetEventListener() {
    }

    @Override
    public void timerUpdate() {
    }
    
    @Override
    public void readHipoFile() {        
        String hipoFileName = hipoPath+"/"+mondet+".hipo";
        System.out.println("Loading Histograms from "+hipoFileName);
        ftofPix[0].initHistograms(hipoFileName);
        ftofOccupancy.analyze();
        inProcess = 2;          
    }
    
    @Override
    public void saveToFile() {
        String hipoFileName = hipoPath+"/"+mondet+".hipo";
        System.out.println("Saving Histograms to "+hipoFileName);
        HipoFile histofile = new HipoFile(hipoFileName);
        histofile.addToMap("H2_a_Hist", this.H2_a_Hist);
        histofile.addToMap("H2_t_Hist", this.H2_t_Hist);
        histofile.writeHipoFile(hipoFileName);
        histofile.browseFile(hipoFileName);     
    }
    
}
