package org.clas.fcmon.ec;

import org.clas.fcmon.cc.CCHvApp;
import org.clas.fcmon.cc.CCScalersApp;
import org.clas.fcmon.detector.view.DetectorShape2D;
import org.clas.fcmon.tools.*;

//clas12
import org.jlab.clas.detector.DetectorCollection;
import org.jlab.clas12.detector.FADCConfigLoader;
import org.jlab.clasrec.utils.DatabaseConstantProvider;
import org.root.histogram.*;

//clas12rec
import org.jlab.detector.base.DetectorDescriptor;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.base.DataEvent;

import org.jlab.rec.ecn.ECDetectorReconstruction;
 
import java.util.TreeMap;

public class ECMon extends DetectorMonitor {
	
    static MonitorApp          app = new MonitorApp("ECMon",1800,950);	
 
    ECDetector                ecDet = null;
    ECAttenApp              ecAtten = null;
    ECMode1App              ecMode1 = null;
    ECOccupancyApp      ecOccupancy = null;
    ECSingleEventApp  ecSingleEvent = null;
    ECRawHistosApp      ecRawHistos = null;
    ECPedestalApp        ecPedestal = null;
    ECTimingApp            ecTiming = null;
    ECReconstructionApp     ecRecon = null;
    ECScalersApp          ecScalers = null;
    ECHvApp                    ecHv = null; 
    
    ECDetectorReconstruction  ecRec = null;
   
    DatabaseConstantProvider ccdb   = null;
    FADCConfigLoader          fadc  = new FADCConfigLoader();

    CalDrawDB                ecDB[] = new CalDrawDB[2]; 
    ECPixels                ecPix[] = new ECPixels[2];
   
    public int inProcess            = 0;     //0=init 1=processing 2=end-of-run 3=post-run
    public boolean inMC             = false; //true=MC false=DATA
    int    detID                    = 0;
    double PCMon_zmin               = 0;
    double PCMon_zmax               = 0;
    int is1                         = 3 ;
    int is2                         = 4 ;
   
    int nsa,nsb,tet,p1,p2,pedref    = 0;
   
    String mondet                   = "PCAL";
    int    moncalrun                = 0;

    DetectorCollection<H2D> H2_PCa_Hist = new DetectorCollection<H2D>();
    DetectorCollection<H2D> H2_PCt_Hist = new DetectorCollection<H2D>();
    DetectorCollection<H1D> H1_PCa_Maps = new DetectorCollection<H1D>();
    DetectorCollection<H1D> H1_PCt_Maps = new DetectorCollection<H1D>();
    DetectorCollection<H1D> H1_PCa_Sevd = new DetectorCollection<H1D>();
    DetectorCollection<H1D> H1_PCt_Sevd = new DetectorCollection<H1D>();
   
    DetectorCollection<H2D> H2_PCa_Sevd = new DetectorCollection<H2D>();
    DetectorCollection<H2D> H2_PC_Stat  = new DetectorCollection<H2D>();
   
    TreeMap<String,Object> glob = new TreeMap<String,Object>();
   
    public ECMon(String det) {
        super("ECMON","1.0","lcsmith");
        mondet = det;
        if (mondet=="PCAL") {detID = 0 ; moncalrun=12 ; ecDB[0] = new CalDrawDB("PCAL")  ; ecPix[0] = new ECPixels("PCAL");}
        if (mondet=="EC")   {detID = 1 ; moncalrun=12 ; ecDB[0] = new CalDrawDB("ECin")  ; ecPix[0] = new ECPixels("ECin");  
		                                                ecDB[1] = new CalDrawDB("ECout") ; ecPix[1] = new ECPixels("ECout");}
        ccdb = new DatabaseConstantProvider(moncalrun,"default");
        ccdb.loadTable("/calibration/ec/attenuation");
        ccdb.disconnect();
    }
	
    public static void main(String[] args){
        String det = "PCAL";
        ECMon monitor = new ECMon(det);		
        app.setPluginClass(monitor);
        app.makeGUI();
        app.mode7Emulation.init("/daq/fadc/ec",1, 3, 1);
        monitor.makeApps();
        monitor.addCanvas();
        monitor.init();
        monitor.initDetector();
        app.init();
        monitor.ecDet.initButtons();
    }
	
    public void initDetector() {
        ecDet = new ECDetector("ECDet",ecPix);
        ecDet.setMonitoringClass(this);
        ecDet.setApplicationClass(app);
        ecDet.init(is1,is2);
        ecDet.addLMaps("Lmap_a", ecRecon.Lmap_a); 
    }
    
    public void makeApps() {
        System.out.println("makeApps()");
        
        ecRecon = new ECReconstructionApp("ECREC",ecPix);        
        ecRecon.setMonitoringClass(this);
        ecRecon.setApplicationClass(app);
        
        ecAtten = new ECAttenApp("Attenuation",ecPix);  
        ecAtten.setMonitoringClass(this);
        ecAtten.setApplicationClass(app);
        
        ecMode1 = new ECMode1App("Mode1",ecPix);
        ecMode1.setMonitoringClass(this);
        ecMode1.setApplicationClass(app);
        
        ecSingleEvent = new ECSingleEventApp("SingleEvent",ecPix);
        ecSingleEvent.setMonitoringClass(this);
        ecSingleEvent.setApplicationClass(app);
        
        ecRawHistos = new ECRawHistosApp("RawHistos",ecPix);
        ecRawHistos.setMonitoringClass(this);
        ecRawHistos.setApplicationClass(app);
        
        ecPedestal = new ECPedestalApp("Pedestal",ecPix);       
        ecPedestal.setMonitoringClass(this);
        ecPedestal.setApplicationClass(app);
        
        ecTiming = new ECTimingApp("Timing",ecPix);     
        ecTiming.setMonitoringClass(this);
        ecTiming.setApplicationClass(app);
        
        ecOccupancy = new ECOccupancyApp("Occupancy",ecPix);        
        ecOccupancy.setMonitoringClass(this);
        ecOccupancy.setApplicationClass(app);    
        
        ecHv = new ECHvApp("HV",mondet);
        ecHv.setMonitoringClass(this);
        ecHv.setApplicationClass(app);  
        
        ecScalers = new ECScalersApp("Scalers",mondet);
        ecScalers.setMonitoringClass(this);
        ecScalers.setApplicationClass(app);     
        
    }
    
    public void addCanvas() {
        System.out.println("addCanvas()");   
        app.addCanvas(ecMode1.getName(),      ecMode1.getCanvas());
        app.addCanvas(ecSingleEvent.getName(),ecSingleEvent.getCanvas());
        app.addCanvas(ecOccupancy.getName(),  ecOccupancy.getCanvas());
        app.addCanvas(ecAtten.getName(),      ecAtten.getCanvas());
        app.addCanvas(ecPedestal.getName(),   ecPedestal.getCanvas());
        app.addCanvas(ecTiming.getName(),     ecTiming.getCanvas());
        app.addCanvas(ecRawHistos.getName(),  ecRawHistos.getCanvas());          
        app.addCanvas(ecRecon.getName(),      ecRecon.getCanvas());          
        app.addFrame(ecHv.getName(),          ecHv.getScalerPane());
        app.addFrame(ecScalers.getName(),     ecScalers.getScalerPane());        
    }
	
    public void init( ) {	    
        System.out.println("init()");	
        initGlob();
        initApps();
        ecPix[0].initHistograms();
        H2_PCa_Hist = ecPix[0].strips.hmap2.get("H2_PCa_Hist");
        H2_PCt_Hist = ecPix[0].strips.hmap2.get("H2_PCt_Hist");
        H1_PCa_Maps = ecPix[0].pixels.hmap1.get("H1_PCa_Maps");
        H1_PCt_Maps = ecPix[0].pixels.hmap1.get("H1_PCt_Maps");
    }

    public void initApps() {
        System.out.println("initApps()");
        ecRecon.init();
        ecRecon.Lmap_a.add(0,0,0, ecRecon.toTreeMap(ecPix[0].pc_cmap));
        ecRecon.Lmap_a.add(0,0,1, ecRecon.toTreeMap(ecPix[0].pc_zmap)); 
        ecAtten.init(); 
        ecAtten.addLMaps("Lmap_a", ecRecon.Lmap_a); 
        ecHv.init(is1,is2);        
        ecScalers.init(is1,is2);        
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
    
    public void readHipoFile() {        
        FCCalibrationData calib = new FCCalibrationData();
        calib.getFile("/Users/colesmith/junk.hipo");
        H2_PCa_Hist = calib.getCollection("H2_PCa_Hist");
        H1_PCa_Maps = calib.getCollection("H1_PCa_Maps");
        H2_PCt_Hist = calib.getCollection("H2_PCt_Hist");
        H1_PCt_Maps = calib.getCollection("H1_PCt_Maps");
        ecOccupancy.analyze();
        inProcess = 2;          
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
    public void saveToFile() {
	    System.out.println("Saving hipofile");
		String hipoFileName = "/Users/colesmith/junk.hipo";
        HipoFile histofile = new HipoFile(hipoFileName);
        histofile.addToMap("H2_PCa_Hist", this.H2_PCa_Hist);
        histofile.addToMap("H1_PCa_Maps", this.H1_PCa_Maps);
        histofile.addToMap("H2_PCt_Hist", this.H2_PCt_Hist);
        histofile.addToMap("H1_PCt_Maps", this.H1_PCt_Maps);
        histofile.writeHipoFile(hipoFileName);
        histofile.browseFile(hipoFileName);		
    }

    @Override
    public void reset() {
		
    }
	
    @Override
    public void close() {
	    
    } 

    @Override
    public void dataEventAction(DataEvent de) {        
        ecRecon.addEvent((EvioDataEvent) de);
    }
    
    @Override    
    public void update(DetectorShape2D shape) {
        putGlob("inProcess", inProcess);
        ecDet.update(shape);
    }
    
	@Override
	public void analyze(int process) {		
		this.inProcess = process; glob.put("inProcess", process);
		switch (inProcess) {
			case 1: 
				ecRecon.makeMaps(); break;
			case 2: 
				ecRecon.makeMaps(); 
		                      for (int ll=0; ll<3 ; ll++) ecAtten.analyze(is1,is2,ll+1,ll+2,0,ecPix[0].pc_nstr[ll]);
		        if (detID==1) for (int ll=0; ll<3 ; ll++) ecAtten.analyze(is1,is2,ll+4,ll+5,0,ecPix[1].pc_nstr[ll]);
		        inProcess=3; glob.put("inProcess", process);
		        break;
		}
	}
	
    @Override
    public void processShape(DetectorShape2D shape) {		
        DetectorDescriptor dd = shape.getDescriptor();
        this.analyze(inProcess);
		
        switch (app.getSelectedTabName()) {
        case "Mode1":             ecMode1.updateCanvas(dd); break;
        case "SingleEvent": ecSingleEvent.updateCanvas(dd); break;
        case "Occupancy":     ecOccupancy.updateCanvas(dd); break;
        case "Attenuation":       ecAtten.updateCanvas(dd); break;
        case "Pedestal":       ecPedestal.updateCanvas(dd);	break;
        case "Timing":           ecTiming.updateCanvas(dd);	break;
        case "RawHistos":     ecRawHistos.updateCanvas(dd);	break;
        case "HV":                   ecHv.updateCanvas(dd); break;
        case "Scalers":         ecScalers.updateCanvas(dd);
        }				
    }

    @Override
    public void resetEventListener() {

    }

    @Override
    public void timerUpdate() {

    }
	
}
