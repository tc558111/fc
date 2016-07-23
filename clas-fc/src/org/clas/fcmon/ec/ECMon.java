package org.clas.fcmon.ec;

import org.clas.fcmon.detector.view.DetectorShape2D;
import org.clas.fcmon.tools.*;
import org.jlab.geom.prim.Path3D;

//clas12
import org.jlab.clas.detector.DetectorCollection;
import org.jlab.clas12.detector.FADCConfigLoader;
import org.jlab.clasrec.utils.DatabaseConstantProvider;
import org.root.histogram.*;
import org.root.attr.ColorPalette;

//clas12rec
import org.jlab.detector.base.DetectorDescriptor;
import org.jlab.detector.base.DetectorType;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.base.DataEvent;

import org.jlab.rec.ecn.ECDetectorReconstruction;
 
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class ECMon extends DetectorMonitor {
	
   static MonitorApp          app = new MonitorApp("ECMon",1800,950);	
 
   ECAttenApp             ecAtten  = null;
   ECMode1App             ecMode1  = null;
   ECOccupancyApp      ecOccupancy = null;
   ECSingleEventApp  ecSingleEvent = null;
   ECRawHistosApp      ecRawHistos = null;
   ECPedestalApp        ecPedestal = null;
   ECTimingApp            ecTiming = null;
   ECReconstructionApp     ecRecon = null;
   
   ECDetectorReconstruction  ecRec = new ECDetectorReconstruction();
   
   DatabaseConstantProvider ccdb   = null;
   FADCConfigLoader          fadc  = new FADCConfigLoader();

   ColorPalette            palette = new ColorPalette();
   
   CalDrawDB[]                ecDB = new CalDrawDB[2];  
   ECPixels[]                ecPix = new ECPixels[2];
   
   public int inProcess            = 0;     //0=init 1=processing 2=end-of-run 3=post-run
   public boolean inMC             = false; //true=MC false=DATA
   int    detID                    = 0;
   double PCMon_zmax               = 0;
   int is1                         = 2 ;
   int is2                         = 3 ;
   
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
	    app.mode7Emulation.init(1, 3, 1);
		monitor.makeApps(monitor);
		monitor.addCanvas();
		monitor.init();
		monitor.initDetector();
		app.init();
		monitor.initButtons();
		//monitor.ecRecon.updatePcalCrtData();
	}
    
    public void makeApps(DetectorMonitor monitor) {
        System.out.println("makeApps()");
        
        ecRecon = new ECReconstructionApp("ECREC",ecPix);        
        ecRecon.setMonitoringClass(app);
        ecRecon.setApplicationClass(monitor);
        
        ecAtten = new ECAttenApp("Attenuation",ecPix);  
        ecAtten.setMonitoringClass(app);
        ecAtten.setApplicationClass(monitor);
        
        ecMode1 = new ECMode1App("Mode1",ecPix);
        ecMode1.setMonitoringClass(app);
        ecMode1.setApplicationClass(monitor);
        
        ecSingleEvent = new ECSingleEventApp("SingleEvent",ecPix);
        ecSingleEvent.setMonitoringClass(app);
        ecSingleEvent.setApplicationClass(monitor);
        
        ecRawHistos = new ECRawHistosApp("RawHistos",ecPix);
        ecRawHistos.setMonitoringClass(app);
        ecRawHistos.setApplicationClass(monitor);
        
        ecPedestal = new ECPedestalApp("Pedestal",ecPix);       
        ecPedestal.setMonitoringClass(app);
        ecPedestal.setApplicationClass(monitor);
        
        ecTiming = new ECTimingApp("Timing",ecPix);     
        ecTiming.setMonitoringClass(app);
        ecTiming.setApplicationClass(monitor);
        
        ecOccupancy = new ECOccupancyApp("Occupancy",ecPix);        
        ecOccupancy.setMonitoringClass(app);
        ecOccupancy.setApplicationClass(monitor);
        
    }
    
	public void addCanvas() {
        System.out.println("addCanvas()");   
        app.addCanvas("Mode1",            ecMode1.getCanvas("Mode1"));
        app.addCanvas("SingleEvent",ecSingleEvent.getCanvas("SingleEvent"));
        app.addCanvas("Occupancy",    ecOccupancy.getCanvas("Occupancy"));
        app.addCanvas("Attenuation",      ecAtten.getCanvas("Attenuation"));
        app.addCanvas("Pedestal",      ecPedestal.getCanvas("Pedestal"));
        app.addCanvas("Timing",          ecTiming.getCanvas("Timing"));
        app.addCanvas("RawHistos",    ecRawHistos.getCanvas("RawHistos"));          
        app.addCanvas("ECREC",            ecRecon.getCanvas("ECREC"));          
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
        ecAtten.init(); 
        ecAtten.addLMaps("Lmap_a", ecRecon.Lmap_a); 
//        ecRec.init();
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
		putGlob("PCMon_zmax", PCMon_zmax);
		putGlob("fadc",fadc);
		putGlob("mondet",mondet);
		putGlob("is1",is1);
		putGlob("is2",is2);
	}
	
	public TreeMap<String,Object> getGlob(){
		return this.glob;
	}
	
    public void putGlob(String name, Object obj){
        glob.put(name,obj);
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

	public void reset() {
		
	}
	
	public void close() {
	    
	} 
	
	public void addButtons(String group, String store, String arg) {
	    List<String> name = new ArrayList<String>();
	    List<Integer> key = new ArrayList<Integer>(); 
	    String[] items = arg.split("\\.");
	    for (int i=0; i<items.length; i=i+2) {
	        name.add(items[i]);
	         key.add(Integer.parseInt(items[i+1]));
	    }	
	    if (store=="View") app.getDetectorView().addViewStore(group, name, key);
	    if (store=="Map")  app.getDetectorView().addMapStore(group, name, key);
	}

	
    public void initDetector() {
        
        System.out.println("initDetector()");
        
        ecRecon.Lmap_a.add(0,0,0, ecRecon.toTreeMap(ecPix[0].pc_cmap));
        ecRecon.Lmap_a.add(0,0,1, ecRecon.toTreeMap(ecPix[0].pc_zmap));
        
         for(int is=is1; is<is2; is++) {
            for(int ip=0; ip<ecPix[0].pc_nstr[0] ; ip++)             app.getDetectorView().getView().addShape("U",getStrip(is,1,ip));
            for(int ip=0; ip<ecPix[0].pc_nstr[1] ; ip++)             app.getDetectorView().getView().addShape("V",getStrip(is,2,ip));
            for(int ip=0; ip<ecPix[0].pc_nstr[2] ; ip++)             app.getDetectorView().getView().addShape("W",getStrip(is,3,ip));           
            for(int ip=0; ip<ecPix[0].pixels.getNumPixels() ; ip++)  app.getDetectorView().getView().addShape("PIX",getPixel(is,4,ip));
        }
        
         app.getDetectorView().getView().addDetectorListener(this);
         
         for(String layer : app.getDetectorView().getView().getLayerNames()){
            app.getDetectorView().getView().setDetectorListener(layer,this);
         }
         
         addButtons("DET","View","PC.1.ECi.1.ECo.2");
         addButtons("LAY","View","U.1.V.2.W.3.PIX.4");
         addButtons("UVW","Map","EVT.0.ADC.0.TDC.0");
         addButtons("PIX","Map","EVT.0.NEVT.1.ADC U.11.ADC V.12.ADC W.13.ADC U+V+W.9");
     
         app.getDetectorView().addMapButtons();
         app.getDetectorView().addViewButtons();
         
    }
    
    public void initButtons() {
        
        System.out.println("initButtons()");
        
        app.getDetectorView().initMapButtons(0, 0);
        app.getDetectorView().initMapButtons(1, 0);
        app.getDetectorView().initViewButtons(0, 0);
        app.getDetectorView().initViewButtons(1, 1);
        app.getDetectorView().setFPS(10);
        app.setSelectedTab(2); 
        
    }
    
    public DetectorShape2D getPixel(int sector, int layer, int pixel){

        DetectorShape2D shape = new DetectorShape2D();
        
        if (mondet=="PCAL") shape = new DetectorShape2D(DetectorType.PCAL,sector,layer,pixel);      
        if (mondet=="EC")   shape = new DetectorShape2D(DetectorType.EC,sector,layer,pixel);    
        
        Path3D shapePath = shape.getShapePath();
        
        for(int j = 0; j < ecPix[0].pc_nvrt[pixel]; j++){
            shapePath.addPoint(ecPix[0].pc_xpix[j][pixel][sector],ecPix[0].pc_ypix[j][pixel][sector],0.0);
        }
        
        shape.addDetectorListener(this);
        return shape;
    }
    
    public DetectorShape2D getStrip(int sector, int layer, int str) {

        DetectorShape2D shape = new DetectorShape2D();
        
        if (mondet=="PCAL") shape = new DetectorShape2D(DetectorType.PCAL,sector,layer,str);        
        if (mondet=="EC")   shape = new DetectorShape2D(DetectorType.EC,sector,layer,str);  
        
        Path3D shapePath = shape.getShapePath();
        
        for(int j = 0; j <4; j++){
            shapePath.addPoint(ecPix[0].pc_xstr[j][str][layer-1][sector],ecPix[0].pc_ystr[j][str][layer-1][sector],0.0);
        }   
        
        shape.addDetectorListener(this);
        return shape;
    }

    @Override
    public void dataEventAction(DataEvent de) {        
        EvioDataEvent event = (EvioDataEvent) de;
//        ecRec.processEvent(event);
        ecRecon.addEvent(event);
    }
    
 //   @Override
	public void update(DetectorShape2D shape) {
		
		int is        = shape.getDescriptor().getSector();
		int layer     = shape.getDescriptor().getLayer();
		int component = shape.getDescriptor().getComponent();
		
		int panel = app.getDetectorView().omap;	
		int io    = app.getDetectorView().ilmap;

		int of    = (io-1)*3;
		int lay=0;
		int opt=0;
		
		if (panel==1) opt = 1;
		if (layer<4)  lay = layer+of;
		if (layer==4) lay = layer+2+io;
		if (panel==9) lay = panel+io-1;
		if (panel>10) lay = panel+of;
		
		layer = lay;
		
		double colorfraction=1;
		
		if (inProcess==0){ // Assign default colors upon starting GUI (before event processing)
			if(layer<7) colorfraction = (double)component/36;
			if(layer>=7) colorfraction = getcolor((TreeMap<Integer, Object>) ecRecon.Lmap_a.get(0,0,0), component);
		}
		if (inProcess>0){   		  // Use Lmap_a to get colors of components while processing data
			             colorfraction = getcolor((TreeMap<Integer, Object>) ecRecon.Lmap_a.get(is+1,layer,opt), component);
		}
		if (colorfraction<0.05) colorfraction = 0.05;
		
		Color col = palette.getRange(colorfraction);
		shape.setColor(col.getRed(),col.getGreen(),col.getBlue());
		
		//viewLayers();
	}
	
	public void viewLayers() {
	   
	    double opac = app.displayControl.opac ;
	    
	    if(app.isSingleEvent()) {	  
	       System.out.println("opac="+opac);
	       if(opac>0.99){
	        for(String name : app.getDetectorView().getView().getLayerNames()){
	            app.getDetectorView().getView().setLayerActive(name);
	        }
	       }
	       if(opac<1.0) app.getDetectorView().getView().setOpacity("PIX",(int) (opac*255) ); ;
	    }
	}

	public double getcolor(TreeMap<Integer,Object> map, int component) {
		
		double color=0;
		
		double val[] =(double[]) map.get(1); 
		double rmin  =(double)   map.get(2);
		double rmax  =(double)   map.get(3);
		double z=val[component];
		
		if (z==0) return 0;
		
		PCMon_zmax = rmax*1.2; glob.put("PCMon_zmax", PCMon_zmax);
		
		if (inProcess==0)  color=(double)(z-rmin)/(rmax-rmin);
		double pixMin = app.displayControl.pixMin ; double pixMax = app.displayControl.pixMax;
		if (inProcess!=0) {
			if (!app.isSingleEvent()) color=(double)(Math.log10(z)-pixMin*Math.log10(rmin))/(pixMax*Math.log10(rmax)-pixMin*Math.log10(rmin));
			if ( app.isSingleEvent()) color=(double)(Math.log10(z)-pixMin*Math.log10(rmin))/(pixMax*Math.log10(4000.)-pixMin*Math.log10(rmin));
		}
		
		app.getDetectorView().getView().zmax = pixMax*rmax;
		app.getDetectorView().getView().zmin = pixMin*rmin;
		
		//System.out.println(z+" "+rmin+" "+" "+rmax+" "+color);
		if (color>1)   color=1;
		if (color<=0)  color=0.;

		return color;
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
		        inProcess=3;
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
		case "RawHistos":     ecRawHistos.updateCanvas(dd);	
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
