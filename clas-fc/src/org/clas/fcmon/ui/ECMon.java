package org.clas.fcmon.ui;

import org.clas.fcmon.tools.*;
import org.clas.tools.HipoFile;
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
	
   EventDecoder            decoder = new EventDecoder();
   FADCConfigLoader          fadc  = new FADCConfigLoader();
   FADCFitter              fitter  = new FADCFitter();
   TDirectory         mondirectory = new TDirectory(); 	
   ColorPalette            palette = new ColorPalette();

   CalDrawDB[]                ecDB = new CalDrawDB[2];  
   ECPixels[]                ecPix = new ECPixels[2];

   MyArrays               myarrays ;
   DatabaseConstantProvider   ccdb ;
   
   TreeMap<Integer,Object> map7=null,map8=null; 
   double[]                sed7=null,sed8=null;
   
   int inProcess        = 0; //0=init 1=processing 2=end-of-run 3=post-run
   boolean inMC         = false; //true=MC false=DATA
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
		monpath=".";		
		System.out.println("monpath= "+monpath);		
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
				monitor.init();
				monitor.initDetector(1,2);
				}
			});
	}
	
	public void init() {
		inProcess=0;
		inMC=false;
		initHistograms();
		  configMode7(1,3,1);
		  app.mode7Emulation.tet.setText(Integer.toString(this.tet));
	   	  app.mode7Emulation.nsa.setText(Integer.toString(this.nsa));
	   	  app.mode7Emulation.nsb.setText(Integer.toString(this.nsb));
	   	  collection.clear();	
	}
	
	public void saveToFile() {
		System.out.println("Saving hipofile");
		String hipoFileName = "/Users/colesmith/junk.hipo";
        HipoFile histofile = new HipoFile(hipoFileName);
        histofile.addToMap("Energy_histo", this.H2_PCa_Hist);
        histofile.writeHipoFile(hipoFileName);
        histofile.browsFile(hipoFileName);		
	}
	
	public void reset() {
		
	}
	
	public void close() {
		String file=monpath+"/"+monfile;
		this.mondirectory.write(file);
		System.out.println("Writing out histograms to "+file);		
	} 
	
    public TDirectory getDir(){
        return this.mondirectory;       
    }	
    
	public void initHistograms() {
		
		int nstr = ecPix[0].pc_nstr[0]            ; double nend = nstr+1;  
		int npix = ecPix[0].pixels.getNumPixels() ; double pend = npix+1;
		
		for (int is=1; is<7 ; is++) {
			for (int il=1 ; il<7 ; il++){
				// For Histos
				H2_PCa_Hist.add(is, il, 0, new H2D("PCa_Hist_Raw_"+il, 100,   0., 200.,  nstr, 1., nend));
				H2_PCt_Hist.add(is, il, 0, new H2D("PCt_Hist_Raw_"+il, 100,1330.,1370.,  nstr, 1., nend));
				H2_PCa_Hist.add(is, il, 1, new H2D("PCa_Hist_Pix_"+il, 100,   0., 200.,  nstr, 1., nend));
				H2_PCt_Hist.add(is, il, 1, new H2D("PCt_Hist_Pix_"+il, 100,1330.,1370.,  nstr, 1., nend));
				H2_PCa_Hist.add(is, il, 2, new H2D("PCa_Hist_Pix_"+il,  25,   0., 250.,  npix, 1., pend));
				H2_PCt_Hist.add(is, il, 2, new H2D("PCt_Hist_Pix_"+il,  40,1330.,1370.,  npix, 1., pend));
				H2_PCa_Hist.add(is, il, 3, new H2D("PCa_Hist_PED_"+il,  20, -10.,  10.,  nstr, 1., nend)); 
				H2_PCt_Hist.add(is, il, 3, new H2D("PCa_Hist_TDIF_"+il, 60, -15.,  15.,  nstr, 1., nend)); 
				H2_PCt_Hist.add(is, il, 4, new H2D("PCa_Hist_TDIF_"+il, 60, -15.,  15.,  nstr, 1., nend)); 
				H2_PCa_Hist.add(is, il, 5, new H2D("PCa_Hist_FADC_"+il,100,   0., 100.,  nstr, 1., nend));
				// For Layer Maps
				H1_PCa_Maps.add(is, il, 0, new H1D("PCa_Maps_ADCPIX_"+il, npix,  1., pend));
				H1_PCa_Maps.add(is, il, 1, new H1D("PCa_Maps_PIXA_"+il,   npix,  1., pend));
				H1_PCa_Maps.add(is, il, 2, new H1D("PCa_Maps_ADCPIX2_"+il,npix,  1., pend));
				H1_PCa_Maps.add(is, il, 3, new H1D("PCa_Maps_PIXA2_"+il,  npix,  1., pend));
				H1_PCt_Maps.add(is, il, 0, new H1D("PCt_Maps_TDCPIX_"+il, npix,  1., pend));	
				H1_PCt_Maps.add(is, il, 1, new H1D("PCt_Maps_PIXT_"+il,   npix,  1., pend));	
				// For Single Events
				H1_PCa_Sevd.add(is, il, 0, new H1D("PCa_Sed_"+il, nstr,  1., nend));
				H1_PCt_Sevd.add(is, il, 0, new H1D("PCt_Sed_"+il, nstr,  1., nend));
				H2_PCa_Sevd.add(is, il, 0, new H2D("PCa_Sed_FADC_"+il,100, 0., 100., nstr, 1., nend));
				H2_PCa_Sevd.add(is, il, 1, new H2D("PCa_Sed_FADC_"+il,100, 0., 100., nstr, 1., nend));
			}
			for (int il=7 ; il<9 ; il++){
				// For Non-Layer Maps
				H1_PCa_Maps.add(is, il, 0, new H1D("PCa_Maps_EVTPIXA_"+il,  npix, 1., pend));
				H1_PCt_Maps.add(is, il, 0, new H1D("PCt_Maps_EVTPIXT_"+il,  npix, 1., pend));	
				H1_PCa_Maps.add(is, il, 1, new H1D("PCa_Maps_PIXASUM_"+il,  npix, 1., pend));
				H1_PCt_Maps.add(is, il, 1, new H1D("PCt_Maps_PIXTSUM_"+il,  npix, 1., pend));	
				H1_PCa_Maps.add(is, il, 2, new H1D("PCa_Maps_PIXAS_"+il,    npix, 1., pend));
				H1_PCt_Maps.add(is, il, 2, new H1D("PCt_Maps_PIXTS_"+il,    npix, 1., pend));
				H1_PCa_Maps.add(is, il, 3, new H1D("PCa_Maps_NEVTPIXA_"+il, npix, 1., pend));
				H1_PCt_Maps.add(is, il, 3, new H1D("PCt_Maps_NEVTPIXT_"+il, npix, 1., pend));	
				// For Single Events
				H1_PCa_Sevd.add(is, il, 0, new H1D("PCa_Sed_"+il, npix,  1., pend));
				H1_PCt_Sevd.add(is, il, 0, new H1D("PCt_Sed_"+il, npix,  1., pend));
			}
			for (int il=0 ; il<3 ; il++) {
				H2_PC_Stat.add(is, il, 0, new H2D("PC_Stat_EVTS_"+il, nstr, 1., nend,  3, 1., 4.));				
				H2_PC_Stat.add(is, il, 1, new H2D("PC_Stat_ADC_"+il,  nstr, 1., nend,  3, 1., 4.));				
				H2_PC_Stat.add(is, il, 2, new H2D("PC_Stat_TDC_"+il,  nstr, 1., nend,  3, 1., 4.));				
			}
		}
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
	}
	
	private class FADCFitter {
		
		int p1=1,p2=15;
		int mmsum,summing_in_progress;
		int t0,adc,ped,pedsum;
		
		public FADCFitter() {	
		}
		
		public void fit(int nsa, int nsb, int tet, short[] pulse) {
			pedsum=0;adc=0;mmsum=0;summing_in_progress=0;
			for (int mm=0; mm<pulse.length; mm++) {
				if(mm>p1 && mm<=p2)  pedsum+=pulse[mm];
				if(mm==p2)           pedsum=pedsum/(p2-p1);
				if (app.mode7Emulation.User_pedref==0) ped=pedsum;
				if (app.mode7Emulation.User_pedref==1) ped=pedref;
				if(mm>p2) {
					if ((summing_in_progress==0) && pulse[mm]>ped+tet) {
					  summing_in_progress=1;
					  t0 = mm;
					  for (int ii=1; ii<nsb+1;ii++) adc+=(pulse[mm-ii]-ped);
					  mmsum=nsb;
					}
					if(summing_in_progress>0 && mmsum>(nsa+nsb)) summing_in_progress=-1;
					if(summing_in_progress>0) {adc+=(pulse[mm]-ped); mmsum++;}
				}
			}
		}
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
             		this.configMode7(icr,isl,ich);
            		int[] adcc= (int[]) strip.getDataObject();
            		ped = adcc[2];
            		npk = adcc[3];
            		adc = (adcc[1]-ped*(this.nsa+this.nsb))/10;
            		timf = DataUtils.getInteger(adcc[0],0,5);
            		timc = DataUtils.getInteger(adcc[0],6,14);
            		tdcf = timc*4.+timf*0.0625;
             	}
            	
            	if(strip.getType()==BankType.ADCPULSE) { // FADC MODE 1
            		short[] pulse = (short[]) strip.getDataObject();
             		this.configMode7(icr,isl,ich);
            		fitter.fit(this.nsa,this.nsb,this.tet,pulse);            		
            		adc = fitter.adc/10;
            		ped = fitter.ped;
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
			
			inMC = true; thr[0]=thr[1]=5;
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
		
		PCMon_zmax = rmax*1.2;
		
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
		this.inProcess = process;
		if (process==1 || process==2)  this.analyzeOccupancy();	 
	}
	
	public void analyzeOccupancy() {
		
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
	public void analyzeAttenuation(int is1, int is2, int il1, int il2, int ip1, int ip2) {
		
		TreeMap<Integer, Object> map;
		CalibrationData fits ; 	
		boolean doCalibration=false;
		int npix = ecPix[0].pixels.getNumPixels();
		double  meanerr[] = new double[npix];
		boolean status[] = new boolean[npix];
		 		
		for (int is=is1 ; is<is2 ; is++) {
			for (int il=il1 ; il<il2 ; il++) {
				int ill,iill ; if (il<4) {ill=7 ; iill=il;} else {ill=8 ; iill=il-3;}
				
				//Extract raw arrays for error bar calculation
				double cnts[]  = H1_PCa_Maps.get(is+1,ill,0).getData();				
				double adc[]   = H1_PCa_Maps.get(is+1,il,1).getData();
				double adcsq[] = H1_PCa_Maps.get(is+1,il,3).getData();
				doCalibration = false;
				
				for (int ipix=0 ; ipix<npix ; ipix++){
					meanerr[ipix]=0;
					if (cnts[ipix]>1) {
						meanerr[ipix]=Math.sqrt((adcsq[ipix]-adc[ipix]*adc[ipix]-8.3)/(cnts[ipix]-1)); //Sheppard's correction: c^2/12 c=10
						doCalibration = true;
					}				
					if (cnts[ipix]==1) {
						meanerr[ipix]=8.3;
						doCalibration = true;
					}
					status[ipix] = ecPix[ill-7].pixels.getPixelStatus(ipix+1);
				}
				
				map = (TreeMap<Integer, Object>) Lmap_a.get(is+1,il+10,0);
				double meanmap[] = (double[]) map.get(1);
				double distmap[] = (double[]) ecPix[ill-7].pixels.getDist(iill);
				
				for (int ip=ip1 ; ip<ip2 ; ip++){
					if (doCalibration){
						fits = new CalibrationData(is,il,ip);
						fits.getDescriptor().setType(DetectorType.PCAL);
						fits.addGraph(ecPix[ill-7].strips.getpixels(iill,ip+1,cnts),
								      ecPix[ill-7].strips.getpixels(iill,ip+1,distmap),
								      ecPix[ill-7].strips.getpixels(iill,ip+1,meanmap),
								      ecPix[ill-7].strips.getpixels(iill,ip+1,meanerr),
								      ecPix[ill-7].strips.getpixels(iill,ip+1,status));
						fits.analyze();
						collection.add(fits.getDescriptor(),fits);
					}
				}
			}
		}
		
	}
	
	public void detectorSelected(DetectorDescriptor desc) {
		
		this.analyze(inProcess);
		switch (app.getSelectedTabIndex()) {
		case 0:
		  this.canvasMode1(desc,       app.getCanvas("Mode1"));
		  break;
		case 1:
		  this.canvasSingleEvent(desc, app.getCanvas("SingleEvent"));
		  break;
		case 2:
		  this.canvasOccupancy(desc,   app.getCanvas("Occupancy"));
		  break;
		case 3:
		  this.canvasAttenuation(desc, app.getCanvas("Attenuation"));
		  break;
		case 4:
		  this.canvasPedestal(desc,    app.getCanvas("Pedestals"));	
		  break;
		case 5:
		  this.canvasTiming(desc,      app.getCanvas("Timing"));	
		  break;
		case 6:
          this.canvasRawHistos(desc,   app.getCanvas("RawHistos"));	
		}
	}
	
	public void canvasRawHistos(DetectorDescriptor desc, EmbeddedCanvas canvas) {
		
		String ytab[]={"U Inner Strip","V Inner Strip","W Inner Strip","U Outer Strip","V Outer Strip","W Outer Strip"};
		String xtaba[]={"U Inner ADC","V Inner ADC","W Inner ADC","U Outer ADC","V Outer ADC","W Outer ADC"};
		String xtabt[]={"U Inner TDC","V Inner TDC","W Inner TDC","U Outer TDC","V Outer TDC","W Outer TDC"};
		String iolab[]={" "+"Inner ","Outer "};
		
		int is    = desc.getSector();
		int layer = desc.getLayer();
		int ic    = desc.getComponent();
	
		if (layer>3) return;
		
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
		
		canvas.divide(3,3);
		
		H2D h2 = new H2D() ; 
		
		canvas.setAxisFontSize(14);
		canvas.setAxisTitleFontSize(14);
		canvas.setTitleFontSize(14);
		canvas.setStatBoxFontSize(12);
		
		for (int il=0; il<3 ; il++) {
			h2 = H2_PC_Stat.get(is+1,detID+(io-1),il) ; h2.setYTitle(iolab[detID+(io-1)]+"View"); h2.setXTitle("Strip") ; canvas.cd(il-of); canvas.setLogZ(); canvas.draw(h2);
		}
		
		for (int il=l1; il<l2; il++) {
			h2 = H2_PCa_Hist.get(is+1,il,0); h2.setYTitle(ytab[il-1]) ; h2.setXTitle(xtaba[il-1]);
			canvas.cd(il-of-1+3) ; canvas.setLogZ(); canvas.draw(h2); 
		}
		
		for (int il=l1; il<l2; il++) {
			h2 = H2_PCt_Hist.get(is+1,il,0); h2.setYTitle(ytab[il-1]) ; h2.setXTitle(xtabt[il-1]);
			canvas.cd(il-of-1+6) ; canvas.setLogZ(); canvas.draw(h2); 
		}
				
	}

	public void canvasMode1(DetectorDescriptor desc, EmbeddedCanvas canvas) {
		
		if (inMC) return;
		
		int is    = desc.getSector();
		int layer = desc.getLayer();
		int ic    = desc.getComponent();
	
		if (layer>3) return;
		
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
		
		switch (mondet) {
		case "PCAL":
			if (layer==1) canvas.divide(9,8);
			if (layer>1)  canvas.divide(9,7);
			break;
		case "EC":
			canvas.divide(6,6);
		}
		
		canvas.setAxisFontSize(14);
		canvas.setTitleFontSize(14);
		
		H1D h = new H1D() ; 
		String otab[]={"U Inner Strip","V Inner Strip","W Inner Strip","U Outer Strip","V Outer Strip","W Outer Strip"};
		
		if (app.mode7Emulation.User_tet>0)  this.tet=app.mode7Emulation.User_tet;
		if (app.mode7Emulation.User_tet==0) this.tet=app.mode7Emulation.CCDB_tet;
		
		F1D f1 = new F1D("p0",0.,100.); f1.setParameter(0,this.tet);
		f1.setLineColor(2);
		F1D f2 = new F1D("p0",0.,100.); f2.setParameter(0,app.mode7Emulation.CCDB_tet);
		f2.setLineColor(4);f2.setLineStyle(2);	
		
	    for(int ip=0;ip<ecPix[io-1].pc_nstr[layer-of-1];ip++){
	    	canvas.cd(ip); canvas.getPad().setAxisRange(0.,100.,-15.,PCMon_zmax*app.displayControl.pixMax);
	        h = H2_PCa_Sevd.get(is+1,layer,0).sliceY(ip); h.setXTitle("Sample (4 ns)"); h.setYTitle("Counts");
	    	h.setTitle(otab[layer-1]+" "+(ip+1)); h.setFillColor(4); canvas.draw(h);
	        h = H2_PCa_Sevd.get(is+1,layer,1).sliceY(ip); h.setFillColor(2); canvas.draw(h,"same");
	        canvas.draw(f1,"same");canvas.draw(f2,"same");
	    }		
	}
	
	public void canvasSingleEvent(DetectorDescriptor desc, EmbeddedCanvas canvas) {
		
		int is    = desc.getSector();
		int layer = desc.getLayer();
		int ic    = desc.getComponent();
		
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
	    	canvas.cd(il-1); canvas.getPad().setAxisRange(-1.,ecPix[0].pc_nstr[il-1]+1,0.,PCMon_zmax*app.displayControl.pixMax);
	    	h = H1_PCa_Sevd.get(is+1,il+of,0); h.setXTitle(otab[il-1+of]); h.setFillColor(col0); canvas.draw(h);
	    }
	}
	
	public void canvasPedestal(DetectorDescriptor desc, EmbeddedCanvas canvas) {
		
		if (inMC) return;
		
		String otab[][]={{"U Strips","V Strips","W Strips"},{"U Inner Strips","V Inner Strips","W Inner Strips"},{"U Outer Strips","V Outer Strips","W Outer Strips"}};
			 		
		int is = desc.getSector()+1;
		int la = desc.getLayer();
		int ip = desc.getComponent();
		
		int panel = app.getDetectorView().panel1.omap;
		int io    = app.getDetectorView().panel1.ilmap;
		int ic    = io;
		int col2=2,col4=4,col0=0;
		
		H1D h;
		canvas.divide(3,2);
		
	    for(int il=1;il<4;il++){
	    	H2D hpix = H2_PCa_Hist.get(is,il+(io-1)*3,3);
    		hpix.setXTitle("PED (Ref-Measured)") ; hpix.setYTitle(otab[ic][il-1]);
    	 
    		canvas.cd(il-1); canvas.setLogZ(); canvas.draw(hpix);
    		
    		if(la==il) {
    			F1D f1 = new F1D("p0",-10.,10.); f1.setParameter(0,ip);
    			F1D f2 = new F1D("p0",-10.,10.); f2.setParameter(0,ip+1);
    			f1.setLineColor(2); canvas.draw(f1,"same"); 
    			f2.setLineColor(2); canvas.draw(f2,"same");
    		}
    		canvas.cd(il-1+3); 
    		            h=hpix.sliceY(22) ; h.setFillColor(4) ; h.setTitle("") ; h.setXTitle("STRIP "+22)     ; canvas.draw(h);
    	    if(la==il) {h=hpix.sliceY(ip) ; h.setFillColor(2) ; h.setTitle("") ; h.setXTitle("STRIP "+(ip+1)) ; canvas.draw(h,"S");}
	    }			
	}
	public void canvasTiming(DetectorDescriptor desc, EmbeddedCanvas canvas) {
		
		String otab[][]={{"U Strips","V Strips","W Strips"},{"U Strips","V Strips","W Strips"},{"U Strips","V Strips","W Strips"}};
			 		
		int is = desc.getSector()+1;
		int la = desc.getLayer();
		int ip = desc.getComponent();
		
		int panel = app.getDetectorView().panel1.omap;
		int io    = app.getDetectorView().panel1.ilmap;
		int ic    = io;
		int col2=2,col4=4,col0=0;
		
		H1D h;
		canvas.divide(3,2);

	    for(int il=1;il<4;il++){
			H2D hpix = H2_PCt_Hist.get(is,il+(io-1)*3,4);
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
	
	public void canvasAttenuation(DetectorDescriptor desc, EmbeddedCanvas canvas) {
		
		H1D mipADC = null;
		int nstr = ecPix[0].pc_nstr[0];
		
	    double[] xp     = new double[nstr];
	    double[] xpe    = new double[nstr];
	    double[] yp     = new double[nstr]; 
	    double[] vgain  = new double[nstr];
	    double[] vgaine = new double[nstr]; 
	    double[] vatt   = new double[nstr];
	    double[] vatte  = new double[nstr]; 
	    double[] vattdb = new double[nstr];
	    double[] vattdbe= new double[nstr];
	    double[] vchi2  = new double[nstr];
	    double[] vchi2e = new double[nstr]; 
	    double[] mip    = {100.,160.};
    	double[] xpix   = new double[1];
    	double[] ypix   = new double[1];
    	double[] xerr   = new double[1];
    	double[] yerr   = new double[1];
	    
		String otab[]={"U Inner Strips","V Inner Strips","W Inner Strips","U Outer Strips","V Outer Strips","W Outer Strips"};
		//double pixwidth[]={5.35,5.92,5.92,5.55,6.15,6.15};
	    double pixwidth[]={1.,1.,1.,1.,1.,1.};
	    
	    int is        = desc.getSector();
		int layer     = desc.getLayer();
		int ic        = desc.getComponent();
		
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
				
		canvas.divide(2,2); 

		//System.out.println("layer,panel,io,nstr,of,l1,l2= "+layer+" "+panel+" "+io+" "+nstr+" "+of+" "+l1+" "+l2);
        if (inProcess==2) {
        	              for (int ll=0; ll<3 ; ll++) this.analyzeAttenuation(1,2,ll+1,ll+2,0,ecPix[0].pc_nstr[ll]);
        	if (detID==1) for (int ll=0; ll<3 ; ll++) this.analyzeAttenuation(1,2,ll+4,ll+5,0,ecPix[1].pc_nstr[ll]);
        	inProcess=3;
        }
        
        if (layer>10) {
        	double meanmap[] = (double[]) Lmap_a.get(is+1, layer, 0).get(1);
        	xpix[0] = ecPix[0].pixels.getDist(layer-10-of, ic+1);
		    ypix[0] = meanmap[ic];
		    xerr[0] = 0.;
		    yerr[0] = 0.;
		    mipADC = H2_PCa_Hist.get(is+1,layer-10,2).sliceY(ic) ;
		    //System.out.println("Pixel="+(ic+1)+" Mean1= "+ypix[0]+" Mean2= "+mipADC.getMean());
        }
        
		if (layer<7||(layer>10&&layer<17)) {
			if (inProcess>0) {
				if (layer>10) {layer=layer-10; lay=layer;int component = ecPix[0].pixels.getStrip(lay-of,ic+1); ic=component-1;}
				nstr = ecPix[0].pc_nstr[layer-of-1];
			    if (inProcess==1)  {this.analyzeAttenuation(is,is+1,layer,layer+1,0,nstr);}
				if (collection.hasEntry(is, layer, ic)) {
									
				for (int ip=0; ip<nstr ; ip++) {
					double gain  =  collection.get(1,layer,ip).getFunc(0).parameter(0).value();
					double gaine =  collection.get(1,layer,ip).getFunc(0).parameter(0).error();	
					double att   =  collection.get(1,layer,ip).getFunc(0).parameter(1).value();
					double atte  =  collection.get(1,layer,ip).getFunc(0).parameter(1).error();
					double chi2  =  collection.get(1,layer,ip).getChi2(0);
					int index = ECCommon.getCalibrationIndex(is+1,layer+detID*3,ip+1);
					double attdb = ccdb.getDouble("/calibration/ec/attenuation/B",index);
					if (att!=0) att=-1./att; else att=0 ; 
					atte = att*att*atte;
					   xp[ip] =ip    ;     xpe[ip] = 0.; 
					vgain[ip] = gain ;  vgaine[ip] = gaine;
		             vatt[ip] = att  ;   vatte[ip] = atte;
		           vattdb[ip] = attdb; vattdbe[ip] = 0.;
		            vchi2[ip] = Math.min(4, chi2) ; vchi2e[ip]=0.;   
				}
				
	            GraphErrors   gainGraph = new GraphErrors(xp,vgain,xpe,vgaine);
	            GraphErrors    attGraph = new GraphErrors(xp,vatt,xpe,vatte);
	            GraphErrors  attdbGraph = new GraphErrors(xp,vattdb,xpe,vattdbe);
	            GraphErrors   chi2Graph = new GraphErrors(xp,vchi2,xpe,vchi2e);
	            GraphErrors    pixGraph = new GraphErrors(xpix,ypix,xerr,yerr);
	             
	            gainGraph.setMarkerStyle(2);   gainGraph.setMarkerSize(6);   gainGraph.setMarkerColor(2);
	             attGraph.setMarkerStyle(2);    attGraph.setMarkerSize(6);    attGraph.setMarkerColor(2);
	           attdbGraph.setMarkerStyle(2);  attdbGraph.setMarkerSize(7);  attdbGraph.setMarkerColor(1);
	            chi2Graph.setMarkerStyle(2);   chi2Graph.setMarkerSize(6);   chi2Graph.setMarkerColor(2);
	             pixGraph.setMarkerStyle(2);    pixGraph.setMarkerSize(6);    pixGraph.setFillColor(1); 
	             
	            gainGraph.setXTitle(otab[lay-1]) ;  gainGraph.setYTitle("PMT GAIN")         ; gainGraph.setTitle(" ");
	           attdbGraph.setXTitle(otab[lay-1]) ; attdbGraph.setYTitle("ATTENUATION (CM)") ; attdbGraph.setTitle(" ");
		        chi2Graph.setXTitle(otab[lay-1]) ; chi2Graph.setYTitle("REDUCED CHI^2")    ; chi2Graph.setTitle(" ");
		        
	            F1D f1 = new F1D("p0",0,nstr+1); f1.setParameter(0,mip[io-1]); f1.setLineStyle(2);
	            		        
	            double ymax=200; if(!inMC) ymax=350;
				canvas.cd(0);canvas.getPad().setAxisRange("Y",0.,ymax);
				canvas.draw(collection.get(is,layer,ic).getRawGraph(0));
				canvas.draw(collection.get(is,layer,ic).getFitGraph(0),"same");
				canvas.draw(collection.get(is,layer,ic).getFunc(0),"same");
				canvas.draw(pixGraph,"same");
				
				double xmax = ecPix[0].pc_nstr[0]+1;
				canvas.cd(1);           canvas.getPad().setAxisRange(-1.,xmax,0.,4.)   ; canvas.draw(chi2Graph) ; 
	            canvas.cd(2); if(!inMC) canvas.getPad().setAxisRange(-1.,xmax,0.,400.) ; canvas.draw(gainGraph) ; canvas.draw(f1,"same"); 
	            canvas.cd(3);           canvas.getPad().setAxisRange(-1.,xmax,0.,600.) ; canvas.draw(attdbGraph); canvas.draw(attGraph,"same");            
				}
			}
		}
	}

	
	public void canvasOccupancy(DetectorDescriptor desc, EmbeddedCanvas canvas) {
				
		int is    = desc.getSector();
		int layer = desc.getLayer();
		int ic    = desc.getComponent();
		
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
		
		//System.out.println("layer,panel,io,of,l1,l2= "+layer+" "+panel+" "+io+" "+of+" "+l1+" "+l2);
		
		int l,col0=0,col1=0,col2=0,strip=0,pixel=0;
		//String otab[]={"U INNER STRIPS","V INNER STRIPS","W INNER STRIPS","U OUTER STRIPS","V OUTER STRIPS","W OUTER STRIPS"};
		String lab1[]={"U ","V ","W "}, lab2[]={"Inner ","Outer "}, lab3[]={"Strip ","Pixel "},lab4[]={" ADC"," TDC"};
		H1D h;
		//TStyle.setOptStat
		canvas.divide(3,3);
		canvas.setAxisFontSize(14);
		canvas.setTitleFontSize(14);
		canvas.setStatBoxFontSize(12);
		
	    //TStyle.setStatBoxFont(TStyle.getStatBoxFontName(),12);
	    //TStyle.setAxisFont(TStyle.getAxisFontName(),8);
	    		
		if (layer<7)  {col0=0 ; col1=4; col2=2;strip=ic+1;}
		if (layer>=7) {col0=4 ; col1=4; col2=2;pixel=ic+1;}
    
		for(int il=l1;il<l2;il++){
			String otab = lab1[il-1-of]+lab2[io-1]+"Strips";
			canvas.cd(il-1-of); h = H2_PCa_Hist.get(is+1,il,0).projectionY(); h.setXTitle(otab); h.setFillColor(col0); canvas.draw(h);
			}
		
		l=layer;
		
		if (layer<7) {
			canvas.cd(l-1-of); h = H2_PCa_Hist.get(is+1,l,0).projectionY(); h.setFillColor(col1); canvas.draw(h,"same");
			H1D copy = h.histClone("Copy"); copy.reset() ; 
			copy.setBinContent(ic, h.getBinContent(ic)); copy.setFillColor(col2); canvas.draw(copy,"same");
			for(int il=l1;il<l2;il++) {
				String alab = lab1[il-1-of]+lab2[io-1]+lab3[0]+strip+lab4[0];String tlab = lab1[il-1-of]+lab2[io-1]+lab3[0]+strip+lab4[1];
				if(layer!=il) {canvas.cd(il+2-of); h = H2_PCa_Hist.get(is+1,il,0).sliceY(22); h.setXTitle(alab); h.setTitle(""); h.setFillColor(col0); canvas.draw(h);}
				if(layer!=il) {canvas.cd(il+5-of); h = H2_PCt_Hist.get(is+1,il,0).sliceY(22); h.setXTitle(tlab); h.setTitle(""); h.setFillColor(col0); canvas.draw(h);}
				}
			String alab = lab1[l-1-of]+lab2[io-1]+lab3[0]+strip+lab4[0];String tlab = lab1[l-1-of]+lab2[io-1]+lab3[0]+strip+lab4[1];
			canvas.cd(l+2-of); h = H2_PCa_Hist.get(is+1,l,0).sliceY(ic);h.setXTitle(alab); h.setTitle(""); h.setFillColor(col2); canvas.draw(h,"S");
			canvas.cd(l+5-of); h = H2_PCt_Hist.get(is+1,l,0).sliceY(ic);h.setXTitle(tlab); h.setTitle(""); h.setFillColor(col2); canvas.draw(h,"S");
			}
		
		if (layer==7||layer==8) {
			for(int il=l1;il<l2;il++) {
				canvas.cd(il-1-of); h = H2_PCa_Hist.get(is+1,il,0).projectionY();
				H1D copy = h.histClone("Copy");
				
				strip = ecPix[0].pixels.getStrip(il-of,ic+1);
				copy.reset() ; copy.setBinContent(ic, h.getBinContent(ic));
				copy.setFillColor(col2); canvas.draw(copy,"same");	    		 
				String alab = lab1[il-1-of]+lab2[io-1]+lab3[0]+strip+lab4[0];String tlab = lab1[il-1-of]+lab2[io-1]+lab3[0]+strip+lab4[1];
				canvas.cd(il+2-of) ; h = H2_PCa_Hist.get(is+1,il,0).sliceY(strip-1); h.setXTitle(alab); h.setTitle("");h.setFillColor(col2); canvas.draw(h,"S");
				canvas.cd(il+5-of) ; h = H2_PCt_Hist.get(is+1,il,0).sliceY(strip-1); h.setXTitle(tlab); h.setTitle("");h.setFillColor(col2); canvas.draw(h,"S");
				}
			}
		
		if (layer>8) {
			for(int il=l1;il<l2;il++) {
				canvas.cd(il-1-of); h = H2_PCa_Hist.get(is+1,il,1).projectionY();
				H1D copy = h.histClone("Copy");
				strip = ecPix[0].pixels.getStrip(il-of,ic+1);
				copy.reset() ; copy.setBinContent(strip-1, h.getBinContent(strip-1));
				copy.setFillColor(col2); canvas.draw(copy,"same");	
				String alab1 = lab1[il-1-of]+lab2[io-1]+lab3[0]+strip+lab4[0];String tlab1 = lab1[il-1-of]+lab2[io-1]+lab3[0]+strip+lab4[1];
				String alab2 = lab1[il-1-of]+lab2[io-1]+lab3[1]+pixel+lab4[0];String tlab2 = lab1[il-1-of]+lab2[io-1]+lab3[1]+pixel+lab4[1];
				if (layer<17) {
					canvas.cd(il+2-of) ; h = H2_PCa_Hist.get(is+1,il,1).sliceY(strip-1); h.setXTitle(alab1); h.setTitle("");h.setFillColor(col2); canvas.draw(h,"S");
					canvas.cd(il+5-of) ; h = H2_PCa_Hist.get(is+1,il,2).sliceY(ic)     ; h.setXTitle(alab2); h.setTitle("");h.setFillColor(col2); canvas.draw(h,"S");
					}
				if (layer>16&&layer<22) {
					canvas.cd(il+2-of) ; h = H2_PCt_Hist.get(is+1,il,1).sliceY(strip-1); h.setXTitle(tlab1); h.setTitle("");h.setFillColor(col2); canvas.draw(h);
					canvas.cd(il+5-of) ; h = H2_PCt_Hist.get(is+1,il,2).sliceY(ic)     ; h.setXTitle(tlab2); h.setTitle("");h.setFillColor(col2); canvas.draw(h);
					}
				}  	 
			}
		
	}
	
}
