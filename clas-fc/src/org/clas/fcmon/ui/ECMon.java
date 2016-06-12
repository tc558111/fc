package org.clas.fcmon.ui;

import org.clas.fcmon.tools.*;
import org.jlab.geom.prim.Path3D;
import org.jlab.rec.ecn.ECCommon;
import org.jlab.clas.detector.BankType;
import org.jlab.clas.detector.DetectorBankEntry;
import org.jlab.clas.detector.DetectorCollection;
import org.jlab.clas.detector.DetectorDescriptor;
import org.jlab.clas.detector.DetectorType;
import org.jlab.clas.tools.utils.DataUtils;
import org.jlab.clas12.calib.DetectorShape2D;
import org.jlab.clas12.detector.EventDecoder;
import org.jlab.clas12.detector.FADCConfig;
import org.jlab.clas12.detector.FADCConfigLoader;
import org.jlab.clasrec.utils.DatabaseConstantProvider;
import org.root.func.F1D;
import org.root.group.TDirectory;
import org.root.histogram.*;
import org.root.basic.EmbeddedCanvas;
import org.root.basic.EmbeddedPad;
import org.root.basic.GraphicsAxisNumber;
import org.root.basic.DataSetFrame;
//import org.root.pad.TEmbeddedCanvas;
import org.root.attr.ColorPalette;
import org.root.attr.TStyle;

import java.awt.Color;
import java.awt.GridLayout;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Arrays;

import javax.swing.SwingUtilities;

import org.jlab.evio.clas12.*;
import org.jlab.data.io.DataEvent;

public class ECMon extends DetectorMonitor {
	
   static MonitorApp           app = new MonitorApp("ECMon",1800,950);	
   ECMon monitor = null;
   ECAttenApp             ecAtten  = null;
   ECMode1App             ecMode1  = null;
   ECOccupancyApp      ecOccupancy = null;
   ECSingleEventApp  ecSingleEvent = null;
   ECRawHistosApp      ecRawHistos = null;
   ECPedestalApp        ecPedestal = null;
   ECTimingApp            ecTiming = null;
   EventDecoder            decoder = new EventDecoder();
   FADCConfigLoader          fadc  = new FADCConfigLoader();
   FADCFitter              fitter  = new FADCFitter(1,15);
   TDirectory         mondirectory = new TDirectory(); 	
   ColorPalette            palette = new ColorPalette();
   DatabaseConstantProvider ccdb   = null;
   
   CalDrawDB[]                ecDB = new CalDrawDB[2];  
   ECPixels[]                ecPix = new ECPixels[2];

   MyArrays               myarrays ;
   
   TreeMap<Integer,Object> map7=null,map8=null; 
   double[]                sed7=null,sed8=null;
   
   public int inProcess        = 0; //0=init 1=processing 2=end-of-run 3=post-run
   public boolean inMC         = false; //true=MC false=DATA
   int thr[]            = {15,15,20};
   int nsa,nsb,tet,p1,p2,pedref  = 0;
   double PCMon_zmax    = 0;
   String monpath       = System.getenv("COATJAVA");
   String monfile       = "mondirectory"; 
   String mondet        = "PCAL";
   int    moncalrun     = 0;
   int    detID         = 0;
   int tid       		 = 100000;
   int cid       		 = 10000;
   int lid       		 = 100;
      
   DetectorCollection<CalibrationData> collection = new DetectorCollection<CalibrationData>();  
	
   DetectorCollection<H2D> H2_PCa_Hist = new DetectorCollection<H2D>();
   DetectorCollection<H2D> H2_PCt_Hist = new DetectorCollection<H2D>();
   DetectorCollection<H1D> H1_PCa_Hist = new DetectorCollection<H1D>();
   DetectorCollection<H1D> H1_PCt_Hist = new DetectorCollection<H1D>();
   DetectorCollection<H1D> H1_PCa_Maps = new DetectorCollection<H1D>();
   DetectorCollection<H1D> H1_PCt_Maps = new DetectorCollection<H1D>();
   DetectorCollection<H1D> H1_PCa_Sevd = new DetectorCollection<H1D>();
   DetectorCollection<H1D> H1_PCt_Sevd = new DetectorCollection<H1D>();
   DetectorCollection<H2D> H2_PCa_Sevd = new DetectorCollection<H2D>();
   DetectorCollection<H2D> H2_PC_Stat  = new DetectorCollection<H2D>();

   DetectorCollection<TreeMap<Integer,Object>> Lmap_a = new DetectorCollection<TreeMap<Integer,Object>>();
   DetectorCollection<TreeMap<Integer,Object>> Lmap_t = new DetectorCollection<TreeMap<Integer,Object>>();

   TreeMap<String,Object> glob = new TreeMap<String,Object>();
   
	public ECMon(String det) {
		super("ECMON","1.0","lcsmith");
		mondet = det;
		if (mondet=="PCAL") {detID = 0 ; moncalrun=2  ; ecDB[0] = new CalDrawDB("PCAL")  ; ecPix[0] = new ECPixels("PCAL");}
		if (mondet=="EC")   {detID = 1 ; moncalrun=12 ; ecDB[0] = new CalDrawDB("ECin")  ; ecPix[0] = new ECPixels("ECin");  
		                                                ecDB[1] = new CalDrawDB("ECout") ; ecPix[1] = new ECPixels("ECout");}
		myarrays = new MyArrays();
		fadc.load("/daq/fadc/ec",10,"default");
		ccdb = new DatabaseConstantProvider(moncalrun,"default");
		ccdb.loadTable("/calibration/ec/attenuation");
		ccdb.disconnect();
	}
	
	public static void main(String[] args){
		
		String det = "PCAL";
		ECMon monitor = new ECMon(det);
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				app.setPluginClass(monitor);
				app.init();
				app.addCanvas("Mode1");
				app.addCanvas("SingleEvent");
				app.addCanvas("Occupancy");
				app.addCanvas("Attenuation");
				app.addCanvas("Pedestals");
				app.addCanvas("Timing");
				app.addCanvas("RawHistos");
				monitor.init(monitor);
				monitor.initDetector(1,2);
				}
			});
	}
	
	public void init(DetectorMonitor monitor) {
		initGlob();
		initHistograms();
		configMode7(1,3,1);
		app.mode7Emulation.tet.setText(Integer.toString(this.tet));
		app.mode7Emulation.nsa.setText(Integer.toString(this.nsa));
		app.mode7Emulation.nsb.setText(Integer.toString(this.nsb));
		collection.clear();	
		
		ecAtten = new ECAttenApp(ecPix,collection);	
		ecAtten.setMonitoringClass(app);
		ecAtten.setApplicationClass(monitor);
		ecAtten.addH1DMaps("H1_PCa_Maps", H1_PCa_Maps);
		ecAtten.addH2DMaps("H2_PCa_Hist", H2_PCa_Hist);
		ecAtten.addLMaps("Lmap_a", Lmap_a);
		
		ecMode1 = new ECMode1App(ecPix,collection);
		ecMode1.setMonitoringClass(app);
		ecMode1.setApplicationClass(monitor);
		ecMode1.addH2DMaps("H2_PCa_Sevd", H2_PCa_Sevd);
		
		ecSingleEvent = new ECSingleEventApp(ecPix,collection);
		ecSingleEvent.setMonitoringClass(app);
		ecSingleEvent.setApplicationClass(monitor);
		ecSingleEvent.addH1DMaps("H1_PCa_Sevd", H1_PCa_Sevd);
		
		ecRawHistos = new ECRawHistosApp(ecPix,collection);
		ecRawHistos.setMonitoringClass(app);
		ecRawHistos.setApplicationClass(monitor);
		ecRawHistos.addH2DMaps("H2_PC_Stat", H2_PC_Stat);
		ecRawHistos.addH2DMaps("H2_PCa_Hist", H2_PCa_Hist);
		ecRawHistos.addH2DMaps("H2_PCt_Hist", H2_PCt_Hist);
		
		ecPedestal = new ECPedestalApp(ecPix,collection);		
		ecPedestal.setMonitoringClass(app);
		ecPedestal.setApplicationClass(monitor);
		ecPedestal.addH2DMaps("H2_PCa_Hist", H2_PCa_Hist);
		
		ecTiming = new ECTimingApp(ecPix,collection);		
		ecTiming.setMonitoringClass(app);
		ecTiming.setApplicationClass(monitor);
		ecTiming.addH2DMaps("H2_PCt_Hist", H2_PCt_Hist);
		
		ecOccupancy = new ECOccupancyApp(ecPix,collection);		
		ecOccupancy.setMonitoringClass(app);
		ecOccupancy.setApplicationClass(monitor);
		ecOccupancy.addH2DMaps("H2_PCa_Hist", H2_PCa_Hist);
		ecOccupancy.addH2DMaps("H2_PCt_Hist", H2_PCt_Hist);
	}
	
	public void initGlob() {
		glob.put("inProcess", inProcess);
		glob.put("detID", detID);
		glob.put("inMC", inMC);
		glob.put("nsa", nsa);
		glob.put("nsb", nsb);
		glob.put("tet", tet);		
		glob.put("ccdb", ccdb);
		glob.put("PCMon_zmax", PCMon_zmax);
	}
	
	public TreeMap<String,Object> getGlob(){
		return this.glob;
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
	
	public void initHistograms() {
		
		int nstr = ecPix[0].pc_nstr[0]            ; double nend = nstr+1;  
		int npix = ecPix[0].pixels.getNumPixels() ; double pend = npix+1;
		
		for (int is=1; is<7 ; is++) {
			for (int il=1 ; il<7 ; il++){
				// For Histos
				String id="s"+Integer.toString(is)+"_l"+Integer.toString(il)+"_c";
				H2_PCa_Hist.add(is, il, 0, new H2D("a_raw_"+id+0, 100,   0., 200.,  nstr, 1., nend));
				H2_PCt_Hist.add(is, il, 0, new H2D("a_raw_"+id+0, 100,1330.,1370.,  nstr, 1., nend));
				H2_PCa_Hist.add(is, il, 1, new H2D("b_pix_"+id+1, 100,   0., 200.,  nstr, 1., nend));
				H2_PCt_Hist.add(is, il, 1, new H2D("b_pix_"+id+1, 100,1330.,1370.,  nstr, 1., nend));
				H2_PCa_Hist.add(is, il, 2, new H2D("c_pix_"+id+2,  25,   0., 250.,  npix, 1., pend));
				H2_PCt_Hist.add(is, il, 2, new H2D("c_pix_"+id+2,  40,1330.,1370.,  npix, 1., pend));
				H2_PCa_Hist.add(is, il, 3, new H2D("d_ped_"+id+3,  20, -10.,  10.,  nstr, 1., nend)); 
				H2_PCt_Hist.add(is, il, 3, new H2D("d_tdif_"+id+3, 60, -15.,  15.,  nstr, 1., nend)); 
				H2_PCt_Hist.add(is, il, 4, new H2D("e_tdif_"+id+4, 60, -15.,  15.,  nstr, 1., nend)); 
				H2_PCa_Hist.add(is, il, 5, new H2D("e_fadc_"+id+5,100,   0., 100.,  nstr, 1., nend));
				// For Layer Maps
				H1_PCa_Maps.add(is, il, 0, new H1D("a_adcpix_"+id+0, npix,  1., pend));
				H1_PCa_Maps.add(is, il, 1, new H1D("b_pixa_"+id+1,   npix,  1., pend));
				H1_PCa_Maps.add(is, il, 2, new H1D("c_adcpix2_"+id+2,npix,  1., pend));
				H1_PCa_Maps.add(is, il, 3, new H1D("d_pixa2_"+id+3,  npix,  1., pend));
				H1_PCt_Maps.add(is, il, 0, new H1D("a_tdcpix_"+id+0, npix,  1., pend));	
				H1_PCt_Maps.add(is, il, 1, new H1D("b_pixt_"+id+1,   npix,  1., pend));	
				// For Single Events
				H1_PCa_Sevd.add(is, il, 0, new H1D("a_sed_"+id+0, nstr,  1., nend));
				H1_PCt_Sevd.add(is, il, 0, new H1D("a_sed_"+id+0, nstr,  1., nend));
				H2_PCa_Sevd.add(is, il, 0, new H2D("b_sed_fadc_"+id+0,100, 0., 100., nstr, 1., nend));
				H2_PCa_Sevd.add(is, il, 1, new H2D("c_sed_fadc_"+id+1,100, 0., 100., nstr, 1., nend));
			}
			for (int il=7 ; il<9 ; il++){
				String id="s"+Integer.toString(is)+"_l"+Integer.toString(il)+"_c";
				// For Non-Layer Maps
				H1_PCa_Maps.add(is, il, 0, new H1D("a_evtpixa_"+id+0,  npix, 1., pend));
				H1_PCt_Maps.add(is, il, 0, new H1D("a_evtpixt_"+id+0,  npix, 1., pend));	
				H1_PCa_Maps.add(is, il, 1, new H1D("b_pixasum_"+id+1,  npix, 1., pend));
				H1_PCt_Maps.add(is, il, 1, new H1D("b_pixtsum_"+id+1,  npix, 1., pend));	
				H1_PCa_Maps.add(is, il, 2, new H1D("c_pixas_"+id+2,    npix, 1., pend));
				H1_PCt_Maps.add(is, il, 2, new H1D("c_pixat_"+id+2,    npix, 1., pend));
				H1_PCa_Maps.add(is, il, 3, new H1D("d_nevtpixa_"+id+3, npix, 1., pend));
				H1_PCt_Maps.add(is, il, 3, new H1D("d_nevtpixt_"+id+3, npix, 1., pend));	
				// For Single Events
				H1_PCa_Sevd.add(is, il, 0, new H1D("a_sed_"+id+0, npix,  1., pend));
				H1_PCt_Sevd.add(is, il, 0, new H1D("a_sed_"+id+0, npix,  1., pend));
			}
			for (int il=0 ; il<3 ; il++) {
				String id="s"+Integer.toString(is)+"_l"+Integer.toString(il)+"_c";
				H2_PC_Stat.add(is, il, 0, new H2D("a_evts_"+id+0, nstr, 1., nend,  3, 1., 4.));				
				H2_PC_Stat.add(is, il, 1, new H2D("b_adc_"+id+1,  nstr, 1., nend,  3, 1., 4.));				
				H2_PC_Stat.add(is, il, 2, new H2D("c_tdc_"+id+2,  nstr, 1., nend,  3, 1., 4.));				
			}
		} 
		/*
		 FCCalibrationData calib = new FCCalibrationData();
		 calib.getFile("/Users/colesmith/junk.hipo");
		 H2_PCa_Hist = calib.getCollection("H2_PCa_Hist");
		 H1_PCa_Maps = calib.getCollection("H1_PCa_Maps");
		 H2_PCt_Hist = calib.getCollection("H2_PCt_Hist");
		 H1_PCt_Maps = calib.getCollection("H1_PCt_Maps");
		 analyzeOccupancy();
		 inProcess = 2;
		 */
		 
	}
	
	public void initDetector(int is1, int is2) {
		
		System.out.println("initpcgui():");
		
		Lmap_a.add(0,0,0, toTreeMap(ecPix[0].pc_cmap));
		Lmap_a.add(0,0,1, toTreeMap(ecPix[0].pc_zmap));
		
		List<String> b1 = new ArrayList<String>();  b1.add("Inner") ; b1.add("Outer");
		List<String> b2 = new ArrayList<String>();  b2.add("EVT")   ; b2.add("ADC")  ; b2.add("TDC");
		List<String> b3 = new ArrayList<String>();  b3.add("EVT")   ; b3.add("NEVT") ; b3.add("ADC U"); b3.add("ADC V"); b3.add("ADC W"); b3.add("ADC U+V+W");
		
		List<List<String>> bg1 = new ArrayList<List<String>>();  
		List<List<String>> bg2 = new ArrayList<List<String>>();   
		
		if (mondet=="PCAL") {bg1.add(b2) ; bg2.add(b3);}
		if (mondet=="EC")   {bg1.add(b1) ; bg1.add(b2); bg2.add(b1) ; bg2.add(b3);}
	
		DetectorShapeView2D  dv1 = new DetectorShapeView2D("U")   ; dv1.addRB(bg1);
		DetectorShapeView2D  dv2 = new DetectorShapeView2D("V")   ; dv2.addRB(bg1);
		DetectorShapeView2D  dv3 = new DetectorShapeView2D("W")   ; dv3.addRB(bg1);
		DetectorShapeView2D  dv4 = new DetectorShapeView2D("PIX") ; dv4.addRB(bg2);
		
		long startTime = System.currentTimeMillis();
		
		for(int is=is1; is<is2; is++) {
			for(int ip=0; ip<ecPix[0].pc_nstr[0] ; ip++)             dv1.addShape(getStrip(is,1,ip));
			for(int ip=0; ip<ecPix[0].pc_nstr[1] ; ip++)             dv2.addShape(getStrip(is,2,ip));
			for(int ip=0; ip<ecPix[0].pc_nstr[2] ; ip++)             dv3.addShape(getStrip(is,3,ip));		    
			for(int ip=0; ip<ecPix[0].pixels.getNumPixels() ; ip++)  dv4.addShape(getPixel(is,4,ip));
		}
		
		System.out.println("initgui time= "+(System.currentTimeMillis()-startTime));
		
		app.getDetectorView().addDetectorLayer(dv1);
		app.getDetectorView().addDetectorLayer(dv2);
		app.getDetectorView().addDetectorLayer(dv3);
		app.getDetectorView().addDetectorLayer(dv4);
		
		app.getDetectorView().addDetectorListener(this);
		
	}
	
	public DetectorShape2D getPixel(int sector, int layer, int pixel){

		DetectorShape2D shape = new DetectorShape2D();
		
	    if (mondet=="PCAL") shape = new DetectorShape2D(DetectorType.PCAL,sector,layer,pixel);	    
	    if (mondet=="EC")   shape = new DetectorShape2D(DetectorType.EC,sector,layer,pixel);	
	    
	    Path3D shapePath = shape.getShapePath();
	    
	    for(int j = 0; j < ecPix[0].pc_nvrt[pixel]; j++){
	    	shapePath.addPoint(ecPix[0].pc_xpix[j][pixel][sector],ecPix[0].pc_ypix[j][pixel][sector],0.0);
	    }
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
	    
	    return shape;
	}
	
	private class MyArrays {
		
		int nstr = ecPix[0].pc_nstr[0];
		int npix = ecPix[0].pixels.getNumPixels();
		
		int        nha[][] = new    int[6][9];
		int        nht[][] = new    int[6][9];
		int    strra[][][] = new    int[6][9][nstr]; 
		int    strrt[][][] = new    int[6][9][nstr]; 
		int     adcr[][][] = new    int[6][9][nstr];
		double ftdcr[][][] = new double[6][9][nstr];
		double  tdcr[][][] = new double[6][9][nstr];
		double    uvwa[][] = new double[6][9];
		double    uvwt[][] = new double[6][9];
		int       mpix[][] = new    int[6][3];
		int       esum[][] = new    int[6][3];
		int ecadcpix[][][] = new    int[6][9][npix];
		int ecsumpix[][][] = new    int[6][9][npix];
		int  ecpixel[][][] = new    int[6][9][npix];
		
		public MyArrays() {	
		}
		
		public void clear() {
			
			for (int is=0 ; is<6 ; is++) {
				for (int il=0 ; il<3 ; il++) {
					mpix[is][il] = 0;
					esum[is][il] = 0;
				}
				for (int il=0 ; il<9 ; il++) {
					nha[is][il]  = 0;
					nht[is][il]  = 0;
					uvwa[is][il] = 0;
					uvwt[is][il] = 0;
					for (int ip=0 ; ip<nstr ; ip++) {
						strra[is][il][ip] = 0;
						strrt[is][il][ip] = 0;
						 adcr[is][il][ip] = 0;
						ftdcr[is][il][ip] = 0;
						 tdcr[is][il][ip] = 0;
					 ecadcpix[is][il][ip] = 0;
					  ecpixel[is][il][ip] = 0;
					}
				}				
			}		
			
			if (app.isSingleEvent()) {
				for (int is=0 ; is<6 ; is++) {
					for (int il=1 ; il<9 ; il++) {
				        H1_PCa_Sevd.get(is+1,il,0).reset();
						if(il<7) {H2_PCa_Sevd.get(is+1,il,0).reset();H2_PCa_Sevd.get(is+1,il,1).reset();}
					}
				}
			}
			
		}
		
		public void fill(int is, int il, int ip, int adc, double tdc, double tdcf) {
			
			int ic=0,iil;
			  
			if (mondet=="EC")    ic=1;  
			if (mondet=="PCAL")  ic=0;
			
			iil = il;
			if (il>3) {ic=2; iil=il-3;}
			
			int  iv = il+3;
			
			if(tdc>1200&&tdc<1500){
				uvwt[is-1][ic]=uvwt[is-1][il]+ecPix[0].uvw_dalitz(ic,il,ip); //Dalitz tdc 
	          	 nht[is-1][iv-1]++; int inh = nht[is-1][iv-1];
	            tdcr[is-1][iv-1][inh-1] = tdc;
	           strrt[is-1][iv-1][inh-1] = ip;
	          	  H2_PCt_Hist.get(is,il,0).fill(tdc,ip,1.0);
	          	  //System.out.println(is+" "+ic+" "+ip+" "+iil+" "+tdc);
	          	  H2_PC_Stat.get(is,ic,2).fill(ip,iil,tdc);
	        }
	   	    if(adc>thr[ic]){
	   	    	uvwa[is-1][ic]=uvwa[is-1][ic]+ecPix[0].uvw_dalitz(ic,il,ip); //Dalitz adc
	          	 nha[is-1][iv-1]++; int inh = nha[is-1][iv-1];
	            adcr[is-1][iv-1][inh-1] = adc;
	           ftdcr[is-1][iv-1][inh-1] = tdcf;
	           strra[is-1][iv-1][inh-1] = ip;
	          	  H2_PCa_Hist.get(is,il,0).fill(adc,ip,1.0);
	          	  H2_PC_Stat.get(is,ic,0).fill(ip,iil,1.);
	          	  H2_PC_Stat.get(is,ic,1).fill(ip,iil,adc);
	        }	
		}
		
		public void findPixels() {
			
			int u,v,w,ii;
			
			for (int is=0 ; is<6 ; is++) { // Loop over sectors
				for (int io=0; io<2 ; io++) { // Loop over calorimeter layers 
					int off = 3*io;
					int off1 = off+3;
					int off2 = off+4;
					int off3 = off+5;
					for (int i=0; i<nha[is][off1]; i++) { // Loop over U strips
						u=strra[is][off1][i];
						for (int j=0; j<nha[is][off2]; j++) { // Loop over V strips
							v=strra[is][off2][j];
							for (int k=0; k<nha[is][off3]; k++){ // Loop over W strips
								w=strra[is][off3][k];
								int dalitz = u+v+w;
								if (dalitz==73||dalitz==74) { // Dalitz test
									mpix[is][io]++; ii = mpix[is][io]-1;
									ecadcpix[is][off1][ii] = adcr[is][off1][i];
									ecadcpix[is][off2][ii] = adcr[is][off2][i];
									ecadcpix[is][off3][ii] = adcr[is][off3][i];
									
									ecsumpix[is][io][ii] = ecadcpix[is][off1][ii]+ecadcpix[is][off2][ii]+ecadcpix[is][off3][ii];
									    esum[is][io]     = esum[is][io]+ecsumpix[is][io][ii];
								     ecpixel[is][io][ii] = ecPix[0].pixels.getPixel(u,v,w);
								     H1_PCa_Sevd.get(is+1,io+7,0).fill(ecpixel[is][io][ii],esum[is][io]);								}
							}
						}
					}
				}
//				if (is==1){
//					System.out.println("is,inner nhit="+is+" "+nha[is][3]+","+nha[is][4]+","+nha[is][5]);
//					System.out.println("is,outer nhit="+is+" "+nha[is][6]+","+nha[is][7]+","+nha[is][8]);
//					System.out.println("mpix,ecpix="+mpix[is][0]+","+mpix[is][1]+","+ecpixel[is][0][0]+","+ecpixel[is][1][0]);
//					System.out.println(" ");
//				}
			}
		}
	
		public void processSED() {
			
			for (int is=0; is<6; is++) {
				map7 = new TreeMap<Integer,Object>(H1_PCa_Sevd.get(is+1, 7, 0).toTreeMap());
				map8 = new TreeMap<Integer,Object>(H1_PCa_Sevd.get(is+1, 8, 0).toTreeMap());
				sed7 = (double[]) map7.get(5); sed8 = (double[]) map8.get(5);	
	           for (int il=1; il<7; il++ ){
	        	   int iv = il+3;
	        	   for (int n=1 ; n<nha[is][iv-1]+1 ; n++) {
	        		   int ip=strra[is][iv-1][n-1]; int ad=adcr[is][iv-1][n-1];
	        		   H1_PCa_Sevd.get(is+1,il,0).fill(ip,ad);
	        		             if(il<4) ecPix[0].strips.putpixels(il,ip,ad,sed7);
	        		   if(detID==1&&il>3) ecPix[1].strips.putpixels(il-3,ip,ad,sed8);
	        	   }
	           }
	           map7.put(5,sed7); map8.put(5,sed8);
	           H1_PCa_Sevd.get(is+1,7,0).fromTreeMap(map7);
	           H1_PCa_Sevd.get(is+1,8,0).fromTreeMap(map8);
			}					
		}
		
		public void processPixels() {
			
		boolean good_ua, good_va, good_wa, good_uvwa;
		boolean good_ut, good_vt, good_wt, good_uvwt;
		boolean good_dalitz, good_pixel;
		boolean good_uvwt_save=false;
		int iic,l1,l2,icmax=2,icoff=0,pixel;
		TreeMap<Integer, Object> map= (TreeMap<Integer, Object>) Lmap_a.get(0,0,1); //PCAL
		double pixelLength[] = (double[]) map.get(1);
		
		if (mondet=="EC")   {icmax=3; icoff=0;}
		if (mondet=="PCAL") {icmax=2; icoff=1;}
		
		for (int is=0 ; is<6 ; is++) {		
			for (int ic=1; ic<icmax ; ic++) {  
				iic=ic*3; l1=iic-2; l2=iic+1;
				
				good_ua = nha[is][iic+0]==1;
				good_va = nha[is][iic+1]==1;
				good_wa = nha[is][iic+2]==1;
				good_ut = nht[is][iic+0]==1;
				good_vt = nht[is][iic+1]==1;
				good_wt = nht[is][iic+2]==1;
			
				good_uvwa = good_ua && good_va && good_wa; //Multiplicity test (NU=NV=NW=1)
				good_uvwt = good_ut && good_vt && good_wt; //Multiplicity test (NU=NV=NW=1)		   			

//				good_dalitz = uvwa[is][ic]-2.0)>0.02 && (uvwa[is][ic]-2.0)<0.056 //EC 				
				good_dalitz = Math.abs(uvwa[is][ic-icoff]-2.0)<0.1; //PCAL
				      pixel = ecPix[0].pixels.getPixel(strra[is][iic+0][0],strra[is][iic+1][0],strra[is][iic+2][0]);
				 good_pixel = pixel!=0;
						
			    if (good_uvwa && good_dalitz && good_pixel) { 

					H1_PCa_Maps.get(is+1,ic+6,0).fill(pixel,1.0);
					H1_PCa_Maps.get(is+1,ic+6,3).fill(pixel,1.0/ecPix[0].pixels.getNormalizedArea(pixel)); //Normalized to pixel area
					
					for (int il=l1; il<l2 ; il++){
						double adcc = adcr[is][il+2][0]/pixelLength[pixel-1];
						
						H2_PCa_Hist.get(is+1,il,1).fill(adcc,strra[is][il+2][0],1.0) ;
						H2_PCa_Hist.get(is+1,il,2).fill(adcc,pixel,1.0);						
						H1_PCa_Maps.get(is+1,ic+6,1).fill(pixel,adcc);
						H1_PCa_Maps.get(is+1,il,0).fill(pixel,adcc);
						H1_PCa_Maps.get(is+1,il,2).fill(pixel,Math.pow(adcc,2));

						if (good_uvwt) {
							if(l1==1) good_uvwt_save = good_uvwt;
							if(l1==4 && good_uvwt_save){
								double dtiff1 =  tdcr[is][il-1][0] -  tdcr[is][il+2][0];
								double dtiff2 = ftdcr[is][il-1][0] - ftdcr[is][il+2][0];
								H2_PCt_Hist.get(is+1,il-3,3).fill(dtiff1, strrt[is][il+2][0]);
								H2_PCt_Hist.get(is+1,il-3,4).fill(dtiff2, strrt[is][il+2][0]);
								H2_PCt_Hist.get(is+1,il  ,3).fill(dtiff1, strrt[is][il+2][0]);
								H2_PCt_Hist.get(is+1,il  ,4).fill(dtiff2, strrt[is][il+2][0]);
							}
						}
					}
				}	
//				good_dalitz = uvwt[is][ic]-2.0)>0.02 && (uvwt[is][ic]-2.0)<0.056 //EC 				
				good_dalitz = Math.abs(uvwt[is][ic-icoff]-2.0)<0.1; //PCAL
				     pixel  = ecPix[0].pixels.getPixel(strrt[is][iic+0][0],strrt[is][iic+1][0],strrt[is][iic+2][0]);
				good_pixel  = pixel!=0;
			
				if (good_uvwt && good_dalitz && good_pixel) { 
					H1_PCt_Maps.get(is+1,ic+6,0).fill(pixel,1.0);
					H1_PCt_Maps.get(is+1,ic+6,3).fill(pixel,1.0/ecPix[0].pixels.getNormalizedArea(pixel)); //Normalized to pixel area
					for (int il=l1; il<l2 ; il++){
						H2_PCt_Hist.get(is+1,il,1).fill(tdcr[is][il+2][0],strrt[is][il+2][0],1.0) ;
						H2_PCt_Hist.get(is+1,il,2).fill(tdcr[is][il+2][0],pixel,1.0);						
						H1_PCt_Maps.get(is+1,ic+6,1).fill(pixel,tdcr[is][il+2][0]);
						H1_PCt_Maps.get(is+1,il,0).fill(pixel,tdcr[is][il+2][0]);
					}
				}	
			}
		}	
		}
	}
	public void configMode7(int cr, int sl, int ch) {
   		FADCConfig config=fadc.getMap().get(cr,sl,ch);
		   this.nsa    = (int) config.getNSA();
		   this.nsb    = (int) config.getNSB();
		   this.tet    = (int) config.getTET();
	       this.pedref = (int) config.getPedestal();
		   app.mode7Emulation.CCDB_tet=this.tet;
		   app.mode7Emulation.CCDB_nsa=this.nsa;
		   app.mode7Emulation.CCDB_nsb=this.nsb;
		   if (app.mode7Emulation.User_tet>0) this.tet=app.mode7Emulation.User_tet;
		   if (app.mode7Emulation.User_nsa>0) this.nsa=app.mode7Emulation.User_nsa;
		   if (app.mode7Emulation.User_nsb>0) this.nsb=app.mode7Emulation.User_nsb;
		   glob.put("nsa", this.nsa);
		   glob.put("nsb", this.nsb);
		   glob.put("tet", this.tet);
	}

	
	@Override
	public void processEvent(DataEvent de) {
		
		EvioDataEvent event = (EvioDataEvent) de;
		   	 		
		float tdcmax=100000;
		boolean debug=false;
		int adc,ped,npk=0,pedref=0,timf=0,timc=0;
		double mc_t=0.,tdc=0,tdcf=0;
 				
		if(event.hasBank(mondet+"::true")!=true) {
			thr[0]=15 ; thr[1]=20;
		
			this.myarrays.clear();
			if (debug) event.getHandler().list();	
					
    		decoder.decode(event);
            List<DetectorBankEntry> strips = decoder.getDataEntries(mondet);

            for(DetectorBankEntry strip : strips) {
                adc=ped=pedref=npk=timf=timc=0 ; tdc=tdcf=0;
            	int icr = strip.getDescriptor().getCrate(); 
            	int isl = strip.getDescriptor().getSlot(); 
            	int ich = strip.getDescriptor().getChannel(); 
             	int is  = strip.getDescriptor().getSector();
            	int il  = strip.getDescriptor().getLayer();
            	int ip  = strip.getDescriptor().getComponent();
            	int iord= strip.getDescriptor().getOrder();
            	
            	if(strip.getType()==BankType.TDC) { 
            		int[] tdcc = (int[]) strip.getDataObject(); 
            		tdc = tdcc[0]*24./1000.;        
            	}
            	
            	if(strip.getType()==BankType.ADCFPGA) { // FADC MODE 7
            		int[] adcc= (int[]) strip.getDataObject();
            		ped = adcc[2];
            		npk = adcc[3];
             		this.configMode7(icr,isl,ich);
            		if (app.mode7Emulation.User_pedref==0) adc = (adcc[1]-ped*(this.nsa+this.nsb))/10;
            		if (app.mode7Emulation.User_pedref==1) adc = (adcc[1]-this.pedref*(this.nsa+this.nsb))/10;
            		timf = DataUtils.getInteger(adcc[0],0,5);
            		timc = DataUtils.getInteger(adcc[0],6,14);
            		tdcf = timc*4.+timf*0.0625;
             	}
            	
            	if(strip.getType()==BankType.ADCPULSE) { // FADC MODE 1
            		short[] pulse = (short[]) strip.getDataObject();
             		this.configMode7(icr,isl,ich);
             		if (app.mode7Emulation.User_pedref==0) fitter.fit(this.nsa,this.nsb,this.tet,0,pulse);            		
             		if (app.mode7Emulation.User_pedref==1) fitter.fit(this.nsa,this.nsb,this.tet,this.pedref,pulse);            		
            		adc = fitter.adc/10;
            		ped = fitter.pedsum;
            		for (int i=0 ; i< pulse.length ; i++) {
            			H2_PCa_Hist.get(is,il,5).fill(i,ip,pulse[i]-this.pedref);
            			if (app.isSingleEvent()) {
            				H2_PCa_Sevd.get(is,il,0).fill(i,ip,pulse[i]-this.pedref);
            				int w1 = fitter.t0-this.nsb ; int w2 = fitter.t0+this.nsa;
            				if (fitter.adc>0&&i>=w1&&i<=w2) H2_PCa_Sevd.get(is,il,1).fill(i,ip,pulse[i]-this.pedref);            	      }
            		}
            	}  
            	
              	if (ped>0) H2_PCa_Hist.get(is,il,3).fill(this.pedref-ped, ip);
			    this.myarrays.fill(is, il, ip, adc, tdc, tdcf);		  
			    
			  //System.out.println("crate,slot,chan:"+icr+" "+isl+" "+ich);
			  //System.out.println("sector,layer,pmt,order"+is+" "+il+" "+ip+" "+iord);
			  //System.out.println("  nchan,tdc,adc,ped,pedref,nsb,nsa: "+npk+" "+tdc+" "+adc+" "+ped+" "+pedref+" "+nsb+" "+nsa);
			  //System.out.println("  tdc,timc,timf: "+tdc+" "+timc+" "+timf);
			              	
            }
        
		} 
		
		if(event.hasBank(mondet+"::true")==true){
			EvioDataBank bank  = (EvioDataBank) event.getBank(mondet+"::true");
			int nrows = bank.rows();
			for(int i=0; i < nrows; i++){
				mc_t = bank.getDouble("avgT",i);
			}	
		}
					
		if(event.hasBank(mondet+"::dgtz")==true){
			
			inMC = true; glob.put("inMC",true); thr[0]=thr[1]=5;
			this.myarrays.clear();
			
			EvioDataBank bank = (EvioDataBank) event.getBank(mondet+"::dgtz");
			
			for(int i = 0; i < bank.rows(); i++){
				float dum = (float)bank.getInt("TDC",i)-(float)mc_t*1000;
				if (dum<tdcmax) tdcmax=dum;
			}	   
		   
		   for(int i = 0; i < bank.rows(); i++){
			   int is  = bank.getInt("sector",i);
			   int ip  = bank.getInt("strip",i);
			   int ic  = bank.getInt("stack",i);	 
			   int il  = bank.getInt("view",i);  
			   	   adc = bank.getInt("ADC",i);
        	  int tdcc = bank.getInt("TDC",i);
        	      tdcf = tdcc;
        	      //System.out.println("sector,strip,stack,view,ADC="+is+" "+ip+" "+ic+" "+il+" "+adc);
       	           tdc = (((float)tdcc-(float)mc_t*1000)-tdcmax+1340000)/1000;		   	        	
        	      if(ic==1||ic==2) this.myarrays.fill(is, il+(ic-1)*3, ip, adc, tdc, tdcf);		      	   	   		    	
		    }
		}
				
		if (app.isSingleEvent()) {
			this.myarrays.findPixels();	    // Process all pixels for SED
			this.myarrays.processSED();
		} else {
			this.myarrays.processPixels();	// Process only single pixels 
		}
	}

	public void update(DetectorShape2D shape) {
		
		int is        = shape.getDescriptor().getSector();
		int layer     = shape.getDescriptor().getLayer();
		int component = shape.getDescriptor().getComponent();
		
		int panel = app.getDetectorView().panel1.omap;	
		int io    = app.getDetectorView().panel1.ilmap;
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
			if(layer>=7) colorfraction = getcolor((TreeMap<Integer, Object>) Lmap_a.get(0,0,0), component);
		}
		if (inProcess>0){   		  // Use Lmap_a to get colors of components while processing data
			             colorfraction = getcolor((TreeMap<Integer, Object>) Lmap_a.get(is+1,layer,opt), component);
		}
		if (colorfraction<0.05) colorfraction = 0.05;
		
		Color col = palette.getRange(colorfraction);
		shape.setColor(col.getRed(),col.getGreen(),col.getBlue());
	}

	public double getcolor(TreeMap<Integer,Object> map, int component) {
		
		double color=0;
		
		double val[] =(double[]) map.get(1); 
		double rmin  =(double)   map.get(2);
		double rmax  =(double)   map.get(3);
		double z=val[component];
		
		if (z==0) return 0;
		
		PCMon_zmax = rmax*1.2; glob.put("PCMon_zmax", PCMon_zmax);
;		
		if (inProcess==0)  color=(double)(z-rmin)/(rmax-rmin);
		double pixMin = app.displayControl.pixMin ; double pixMax = app.displayControl.pixMax;
		if (inProcess!=0) {
			if (!app.isSingleEvent()) color=(double)(Math.log10(z)-pixMin*Math.log10(rmin))/(pixMax*Math.log10(rmax)-pixMin*Math.log10(rmin));
			if ( app.isSingleEvent()) color=(double)(Math.log10(z)-pixMin*Math.log10(rmin))/(pixMax*Math.log10(4000.)-pixMin*Math.log10(rmin));
		}
		
		//System.out.println(z+" "+rmin+" "+" "+rmax+" "+color);
		if (color>1)   color=1;
		if (color<=0)  color=0.;

		return color;
	}
	
	public TreeMap<Integer, Object> toTreeMap(double dat[]) {
        TreeMap<Integer, Object> hcontainer = new TreeMap<Integer, Object>();
        hcontainer.put(1, dat);
        double[] b = Arrays.copyOf(dat, dat.length);
        double min=100000,max=0;
        for (int i =0 ; i < b.length; i++){
        	if (b[i] !=0 && b[i] < min) min=b[i];
        	if (b[i] !=0 && b[i] > max) max=b[i];
        }
       // Arrays.sort(b);
       // double min = b[0]; double max=b[b.length-1];
        if (min<=0) min=0.01;
        hcontainer.put(2, min);
        hcontainer.put(3, max);
        return hcontainer;        
	}
			
	@Override
	public void analyze(int process) {		
		this.inProcess = process; glob.put("inProcess", process);
		if (process==1 || process==2)  this.makeMaps();	 
	}

	public void makeMaps() {
		
		// il=1-3 (U,V,W Inner strips) il=4-6 (U,V,W Outer Strips) il=7 (Inner Pixels) il=8 (Outer Pixels)
		
		for (int is=1;is<7;is++) {
			for (int il=1 ; il<7 ; il++) {
				int ill ; if (il<4) ill=7 ; else ill=8 ;
				H1_PCa_Maps.get(is,il,0).divide(H1_PCa_Maps.get(is,ill,0),H1_PCa_Maps.get(is,il,1)); //Normalize Raw View Energy Sum to Events
				H1_PCt_Maps.get(is,il,0).divide(H1_PCt_Maps.get(is,ill,0),H1_PCt_Maps.get(is,il,1)); //Normalize Raw View Timing Sum to Events
				H1_PCa_Maps.get(is,il,2).divide(H1_PCa_Maps.get(is,ill,0),H1_PCa_Maps.get(is,il,3)); //Normalize Raw ADC^2 Sum to Events
				Lmap_a.add(is,il,0, toTreeMap(H2_PCa_Hist.get(is,il,0).projectionY().getData()));    //Strip View ADC  
				Lmap_a.add(is,il+10,0, toTreeMap(H1_PCa_Maps.get(is,il,1).getData()));               //Pixel View ADC 
				Lmap_t.add(is,il,0, toTreeMap(H2_PCt_Hist.get(is,il,0).projectionY().getData()));    //Strip View TDC  
				Lmap_t.add(is,il,1, toTreeMap(H1_PCt_Maps.get(is,il,1).getData()));                  //Pixel View TDC  
			}		    
			for (int il=7; il<9; il++) {	
				H1_PCa_Maps.get(is, il, 1).divide(H1_PCa_Maps.get(is, il, 0),H1_PCa_Maps.get(is, il, 2)); // Normalize Raw Energy Sum to Events
				H1_PCt_Maps.get(is, il, 1).divide(H1_PCt_Maps.get(is, il, 0),H1_PCt_Maps.get(is, il, 2)); // Normalize Raw Timing Sum to Events
			}
	    	Lmap_a.add(is, 7,0, toTreeMap(H1_PCa_Maps.get(is,7,0).getData())); //Pixel Events Inner  
	    	Lmap_a.add(is, 8,0, toTreeMap(H1_PCa_Maps.get(is,8,0).getData())); //Pixel Events Outer  
		    Lmap_a.add(is, 9,0, toTreeMap(H1_PCa_Maps.get(is,7,2).getData())); //Pixel U+V+W Inner Energy     
		    Lmap_a.add(is,10,0, toTreeMap(H1_PCa_Maps.get(is,8,2).getData())); //Pixel U+V+W Outer Energy    
		    Lmap_t.add(is, 7,2, toTreeMap(H1_PCt_Maps.get(is,7,2).getData())); //Pixel U+V+W Inner Time  
		    Lmap_t.add(is, 8,2, toTreeMap(H1_PCt_Maps.get(is,8,2).getData())); //Pixel U+V+W Outer Time 
	    	Lmap_a.add(is, 7,1, toTreeMap(H1_PCa_Maps.get(is,7,3).getData())); //Pixel Events Inner Normalized  
	    	Lmap_a.add(is, 8,1, toTreeMap(H1_PCa_Maps.get(is,8,3).getData())); //Pixel Events Outer Normalized  
			if (app.isSingleEvent()){
				for (int il=1 ; il<9 ; il++) Lmap_a.add(is,il,0,  toTreeMap(H1_PCa_Sevd.get(is,il,0).getData())); 
			}
		}
		
	}
	
	public void detectorSelected(DetectorDescriptor dd) {
		
		this.analyze(inProcess);
		
		switch (app.getSelectedTabIndex()) {
		case 0:
		  ecMode1.canvas(dd, app.getCanvas("Mode1"));
		  break;
		case 1:
		  ecSingleEvent.canvas(dd, app.getCanvas("SingleEvent"));
		  break;
		case 2:
		  ecOccupancy.canvas(dd, app.getCanvas("Occupancy"));
		  break;
		case 3:
		  ecAtten.canvas(dd, app.getCanvas("Attenuation"));
		  break;
		case 4:
		  ecPedestal.canvas(dd, app.getCanvas("Pedestals"));	
		  break;
		case 5:
		  ecTiming.canvas(dd, app.getCanvas("Timing"));	
		  break;
		case 6:
          ecRawHistos.canvas(dd, app.getCanvas("RawHistos"));	
		}
	}
	
}
