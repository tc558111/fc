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
import org.root.basic.GraphicsAxisNumber;
import org.root.basic.DataSetFrame;
//import org.root.pad.TEmbeddedCanvas;
import org.root.attr.ColorPalette;
import org.root.attr.TStyle;

import java.awt.Color;
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
	
   public static MonitorApp app;
	
   EventDecoder            decoder = new EventDecoder();
   FADCConfigLoader          fadc  = new FADCConfigLoader();
   FADCFitter              fitter  = new FADCFitter();
   DatabaseConstantProvider   ccdb = new DatabaseConstantProvider(12,"default");
   TDirectory         mondirectory = new TDirectory(); 	
   ColorPalette            palette = new ColorPalette();
   ECPixels                  ecPix = new ECPixels();
   MyArrays               myarrays = new MyArrays();
   
   TreeMap<Integer,Object> map7=null,map8=null; 
   double[]                sed7=null,sed8=null;
   
   int inProcess        = 0; //0=init 1=processing 2=end-of-run 3=post-run
   boolean inMC         = false; //true=MC false=DATA
   int thr[]            = {15,20};
   String monpath       = System.getenv("COATJAVA");
   String monfile       = "mondirectory"; 
   
   int tid       		 = 100000;
   int cid       		 = 10000;
   int lid       		 = 100;
      
   DetectorCollection<CalibrationData> collection = new DetectorCollection<CalibrationData>();  
	
   DetectorCollection<H2D> H2_ECa_Hist = new DetectorCollection<H2D>();
   DetectorCollection<H2D> H2_ECt_Hist = new DetectorCollection<H2D>();
   DetectorCollection<H1D> H1_ECa_Hist = new DetectorCollection<H1D>();
   DetectorCollection<H1D> H1_ECt_Hist = new DetectorCollection<H1D>();
   DetectorCollection<H1D> H1_ECa_Maps = new DetectorCollection<H1D>();
   DetectorCollection<H1D> H1_ECt_Maps = new DetectorCollection<H1D>();
   DetectorCollection<H1D> H1_ECa_Sevd = new DetectorCollection<H1D>();
   DetectorCollection<H1D> H1_ECt_Sevd = new DetectorCollection<H1D>();
   DetectorCollection<H2D> H2_ECa_Sevd = new DetectorCollection<H2D>();

   DetectorCollection<TreeMap<Integer,Object>> Lmap_a = new DetectorCollection<TreeMap<Integer,Object>>();
   DetectorCollection<TreeMap<Integer,Object>> Lmap_t = new DetectorCollection<TreeMap<Integer,Object>>();
	
	public ECMon(String[] args) {
		super("FCMON","1.0","lcsmith");
		fadc.load("/daq/fadc/ec",10,"default");
		ccdb.loadTable("/calibration/ec/attenuation");
		ccdb.disconnect();
		if(args.length == 1) monpath = args[0];		
		System.out.println("monpath= "+monpath);		
	}
	
	public static void main(String[] args){
		
		ECMon monitor = new ECMon(args);
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				app = new MonitorApp("ECMon",1800,900);
				app.setPluginClass(monitor);
				app.addCanvas("Mode1");
				app.addCanvas("SingleEvent");
				app.addCanvas("Occupancy");
				app.addCanvas("Attenuation");
				app.addCanvas("Pedestals");
				app.addCanvas("Timing");
				monitor.init();
				monitor.initDetector(0,6);
				}
			});
	}
	
	public void init() {
		inProcess=0;
		initHistograms();
		collection.clear();	
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
		
		for (int is=1; is<7 ; is++) {
			for (int il=1 ; il<7 ; il++){
				// For Histos
				H2_ECa_Hist.add(is, il, 0, new H2D("ECa_Hist_Raw_"+il, 100,   0., 200.,  36, 1.,  37.));
				H2_ECt_Hist.add(is, il, 0, new H2D("ECt_Hist_Raw_"+il, 100,1330.,1370.,  36, 1.,  37.));
				H2_ECa_Hist.add(is, il, 1, new H2D("ECa_Hist_Pix_"+il, 100,   0., 200.,  36, 1.,  37.));
				H2_ECt_Hist.add(is, il, 1, new H2D("ECt_Hist_Pix_"+il, 100,1330.,1370.,  36, 1.,  37.));
				H2_ECa_Hist.add(is, il, 2, new H2D("ECa_Hist_Pix_"+il,  25,   0., 250.,1296, 1.,1297.));
				H2_ECt_Hist.add(is, il, 2, new H2D("ECt_Hist_Pix_"+il,  40,1330.,1370.,1296, 1.,1297.));
				H2_ECa_Hist.add(is, il, 3, new H2D("ECa_Hist_PED_"+il,  20, -10.,  10.,  36, 1.,  37.)); 
				H2_ECt_Hist.add(is, il, 3, new H2D("ECa_Hist_TDIF_"+il, 60, -15.,  15.,  36, 1.,  37.)); 
				H2_ECt_Hist.add(is, il, 4, new H2D("ECa_Hist_TDIF_"+il, 60, -15.,  15.,  36, 1.,  37.)); 
				H2_ECa_Hist.add(is, il, 5, new H2D("ECa_Hist_FADC_"+il,100,   0., 100.,  36, 1.,  37.));
				// For Layer Maps
				H1_ECa_Maps.add(is, il, 0, new H1D("ECa_Maps_ADCPIX_"+il, 1296,  1.,1297.));
				H1_ECa_Maps.add(is, il, 1, new H1D("ECa_Maps_PIXA_"+il,   1296,  1.,1297.));
				H1_ECa_Maps.add(is, il, 2, new H1D("ECa_Maps_ADCPIX2_"+il,1296,  1.,1297.));
				H1_ECa_Maps.add(is, il, 3, new H1D("ECa_Maps_PIXA2_"+il,  1296,  1.,1297.));
				H1_ECt_Maps.add(is, il, 0, new H1D("ECt_Maps_TDCPIX_"+il, 1296,  1.,1297.));	
				H1_ECt_Maps.add(is, il, 1, new H1D("ECt_Maps_PIXT_"+il,   1296,  1.,1297.));	
				// For Single Events
				H1_ECa_Sevd.add(is, il, 0, new H1D("ECa_Sed_"+il, 36,  1.,37.));
				H1_ECt_Sevd.add(is, il, 0, new H1D("ECt_Sed_"+il, 36,  1.,37.));
				H2_ECa_Sevd.add(is, il, 0, new H2D("ECa_Sed_FADC_"+il,100, 0., 100., 36, 1., 37.));
			}
			for (int il=7 ; il<9 ; il++){
				// For Non-Layer Maps
				H1_ECa_Maps.add(is, il, 0, new H1D("ECa_Maps_EVTPIXA_"+il, 1296,  1.,1297.));
				H1_ECt_Maps.add(is, il, 0, new H1D("ECt_Maps_EVTPIXT_"+il, 1296,  1.,1297.));	
				H1_ECa_Maps.add(is, il, 1, new H1D("ECa_Maps_PIXASUM_"+il, 1296,  1.,1297.));
				H1_ECt_Maps.add(is, il, 1, new H1D("ECt_Maps_PIXTSUM_"+il, 1296,  1.,1297.));	
				H1_ECa_Maps.add(is, il, 2, new H1D("ECa_Maps_PIXAS_"+il,   1296,  1.,1297.));
				H1_ECt_Maps.add(is, il, 2, new H1D("ECt_Maps_PIXTS_"+il,   1296,  1.,1297.));
				// For Single Events
				H1_ECa_Sevd.add(is, il, 0, new H1D("ECa_Sed_"+il, 1296,  1.,1297.));
				H1_ECt_Sevd.add(is, il, 0, new H1D("ECt_Sed_"+il, 1296,  1.,1297.));
			}
		}
	}
	
	public void initDetector(int is1, int is2) {
		
		System.out.println("initecgui():");
		
		Lmap_a.add(0,0,0, toTreeMap(ecPix.ec_cthpix));
						
		List<String> b1 = new ArrayList<String>();  b1.add("Inner") ; b1.add("Outer");
		List<String> b2 = new ArrayList<String>();  b2.add("EVT")   ; b2.add("ADC")   ; b2.add("TDC");
		List<String> b3 = new ArrayList<String>();  b3.add("EVT")   ; b3.add("ADC U") ; b3.add("ADC V"); b3.add("ADC W"); b3.add("ADC U+V+W");
		
		List<List<String>> bg1 = new ArrayList<List<String>>(); bg1.add(b1) ; bg1.add(b2);
		List<List<String>> bg2 = new ArrayList<List<String>>(); bg2.add(b1) ; bg2.add(b3);
		
		DetectorShapeView2D  dv1 = new DetectorShapeView2D("EC U")   ; dv1.addRB(bg1);
		DetectorShapeView2D  dv2 = new DetectorShapeView2D("EC V")   ; dv2.addRB(bg1);
		DetectorShapeView2D  dv3 = new DetectorShapeView2D("EC W")   ; dv3.addRB(bg1);
		DetectorShapeView2D  dv4 = new DetectorShapeView2D("EC PIX") ; dv4.addRB(bg2);
		
		long startTime = System.currentTimeMillis();
		
		for(int is=is1; is<is2; is++) {
			for(int ip=0; ip<36 ; ip++)    dv1.addShape(getStrip(is,1,ip));
			for(int ip=0; ip<36 ; ip++)    dv2.addShape(getStrip(is,2,ip));
			for(int ip=0; ip<36 ; ip++)    dv3.addShape(getStrip(is,3,ip));		    
			for(int ip=0; ip<1296 ; ip++)  dv4.addShape(getPixel(is,4,ip));
		}
		
		System.out.println("initgui time= "+(System.currentTimeMillis()-startTime));
		
		app.getDetectorView().addDetectorLayer(dv1);
		app.getDetectorView().addDetectorLayer(dv2);
		app.getDetectorView().addDetectorLayer(dv3);
		app.getDetectorView().addDetectorLayer(dv4);
		
		app.getDetectorView().addDetectorListener(this);
		
	}
	
	public DetectorShape2D getPixel(int sector, int layer, int pixel){

	    DetectorShape2D shape = new DetectorShape2D(DetectorType.ECIN,sector,layer,pixel);	    
	    Path3D shapePath = shape.getShapePath();
	    
	    for(int j = 0; j < 3; j++){
	    	shapePath.addPoint(ecPix.ec_xpix[j][pixel][sector],ecPix.ec_ypix[j][pixel][sector],0.0);
	    }
	    return shape;
	}
	
	public DetectorShape2D getStrip(int sector, int layer, int str) {

	    DetectorShape2D shape = new DetectorShape2D(DetectorType.ECIN,sector,layer,str);	    
	    Path3D shapePath = shape.getShapePath();
	    
		int ipix=1,pix1=1,pix2=1;
	    double[] xc   = new double[5];
	    double[] yc   = new double[5];
		int[][] pts73 = {{1,2,3,1},{2,3,1,2},{3,1,2,3}};
		int[][] pts74 = {{1,2,1},{3,2,3},{3,2,3},{1,2,1}};
	    
	    int strip=str+1;
	    
	    switch(layer) {	    
	    case 1:
	    	pix1 = ecPix.pix(strip,37-strip,36);
	    	pix2 = ecPix.pix(strip,36,37-strip);
	    	break;
	    case 2:
	    	pix1 = ecPix.pix(36,strip,37-strip);
	    	pix2 = ecPix.pix(37-strip,strip,36);
	    	break;
	    case 3:
	    	pix1 = ecPix.pix(37-strip,36,strip);
	    	pix2 = ecPix.pix(36,37-strip,strip);
	    	break;	    	
	    }

	    for(int j = 0; j < 4; j++){
	    	if (j<=1) ipix = pix1;
	    	if (j>1)  ipix = pix2;
	    	xc[j] = ecPix.ec_xpix[pts73[layer-1][j]-1][ipix-1][sector];
	    	yc[j] = ecPix.ec_ypix[pts73[layer-1][j]-1][ipix-1][sector];
	    	//System.out.println(sector+" "+str+" "+xc[0]+" "+xc[1]+" "+xc[2]+" "+xc[3]);
	    	//System.out.println(sector+" "+str+" "+yc[0]+" "+yc[1]+" "+yc[2]+" "+yc[3]);
	    }
	    xc[4]=xc[0]; yc[4]=yc[0];
	    
	    for(int j=0; j < 5; j++) shapePath.addPoint(xc[j],yc[j],0.0);
	
	    return shape; 
	}
	
	private class MyArrays {
		
		int        nha[][] = new    int[6][9];
		int        nht[][] = new    int[6][9];
		int    strra[][][] = new    int[6][9][68]; 
		int    strrt[][][] = new    int[6][9][68]; 
		int     adcr[][][] = new    int[6][9][68];
		double ftdcr[][][] = new double[6][9][68];
		double  tdcr[][][] = new double[6][9][68];
		double    uvwa[][] = new double[6][9];
		double    uvwt[][] = new double[6][9];
		int       mpix[][] = new    int[6][3];
		int       esum[][] = new    int[6][3];
		int ecadcpix[][][] = new    int[6][9][1296];
		int ecsumpix[][][] = new    int[6][9][1296];
		int  ecpixel[][][] = new    int[6][9][1296];
		
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
					for (int ip=0 ; ip<68 ; ip++) {
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
			
			if (app.isSingleEvent) {
				for (int is=0 ; is<6 ; is++) {
					for (int il=1 ; il<9 ; il++) {
						         H1_ECa_Sevd.get(is+1,il,0).reset();
						if(il<7) H2_ECa_Sevd.get(is+1,il,0).reset();
					}
				}
			}
			
		}
		
		public void fill(int is, int il, int ip, int adc, double tdc, double tdcf) {
			
			int ic=1; if (il>3) ic=2 ;
			int  iv = il+3;
			if(tdc>1200&&tdc<1500){
				uvwt[is-1][ic]=uvwt[is-1][il]+ecPix.uvw_dalitz(ic,il,ip); //Dalitz test
	          	 nht[is-1][iv-1]++; int inh = nht[is-1][iv-1];
	            tdcr[is-1][iv-1][inh-1] = tdc;
	           strrt[is-1][iv-1][inh-1] = ip;
	          	  H2_ECt_Hist.get(is,il,0).fill(tdc,ip,1.0);
	          	  }
	   	    if(adc>thr[ic-1]){
	   	    	uvwa[is-1][ic]=uvwa[is-1][ic]+ecPix.uvw_dalitz(ic,il,ip); //Dalitz test
	          	 nha[is-1][iv-1]++; int inh = nha[is-1][iv-1];
	            adcr[is-1][iv-1][inh-1] = adc;
	           ftdcr[is-1][iv-1][inh-1] = tdcf;
	           strra[is-1][iv-1][inh-1] = ip;
	          	  H2_ECa_Hist.get(is,il,0).fill(adc,ip,1.0);
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
								     ecpixel[is][io][ii] = ecPix.pix(u,v,w); 
								     H1_ECa_Sevd.get(is+1,io+7,0).fill(ecpixel[is][io][ii],esum[is][io]);								}
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
				map7 = new TreeMap<Integer,Object>(H1_ECa_Sevd.get(is+1, 7, 0).toTreeMap());
				map8 = new TreeMap<Integer,Object>(H1_ECa_Sevd.get(is+1, 8, 0).toTreeMap());
				sed7 = (double[]) map7.get(5); sed8 = (double[]) map8.get(5);	
	           for (int il=1; il<7; il++ ){
	        	   int iv = il+3;
	        	   for (int n=1 ; n<nha[is][iv-1]+1 ; n++) {
	        		   int ip=strra[is][iv-1][n-1]; int ad=adcr[is][iv-1][n-1];
	        		   H1_ECa_Sevd.get(is+1,il,0).fill(ip,ad);
	        		   if(il<4) ecPix.putpixels(il,ip,ad,sed7);
	        		   if(il>3) ecPix.putpixels(il,ip,ad,sed8);
	        	   }
	           }
	           map7.put(5,sed7); map8.put(5,sed8);
	           H1_ECa_Sevd.get(is+1,7,0).fromTreeMap(map7);
	           H1_ECa_Sevd.get(is+1,8,0).fromTreeMap(map8);
			}					
		}
		
		public void processPixels() {
			
		boolean good_ua, good_va, good_wa, good_uvwa;
		boolean good_ut, good_vt, good_wt, good_uvwt;
		boolean good_uvwt_save=false;
		int iic,l1,l2;
		TreeMap<Integer, Object> map= (TreeMap<Integer, Object>) Lmap_a.get(0,0,0);
		double pixelLength[] = (double[]) map.get(1);
		
		for (int is=0 ; is<6 ; is++) {		
			for (int ic=1; ic<3 ; ic++) {
				iic=ic*3; l1=iic-2; l2=iic+1;
				
				good_ua = nha[is][iic+0]==1;
				good_va = nha[is][iic+1]==1;
				good_wa = nha[is][iic+2]==1;
				good_ut = nht[is][iic+0]==1;
				good_vt = nht[is][iic+1]==1;
				good_wt = nht[is][iic+2]==1;
			
				good_uvwa = good_ua && good_va && good_wa; //Multiplicity test (NU=NV=NW=1)
				good_uvwt = good_ut && good_vt && good_wt; //Multiplicity test (NU=NV=NW=1)		   			

				if (good_uvwa && (uvwa[is][ic]-2.0)>0.02 && (uvwa[is][ic]-2.0)<0.056) { 
					int pixela=ecPix.pix(strra[is][iic+0][0],strra[is][iic+1][0],strra[is][iic+2][0]);
					H1_ECa_Maps.get(is+1,ic+6,0).fill(pixela,1.0);
					
					for (int il=l1; il<l2 ; il++){
						double adcc = adcr[is][il+2][0]/pixelLength[pixela-1];
						
						H2_ECa_Hist.get(is+1,il,1).fill(adcc,strra[is][il+2][0],1.0) ;
						H2_ECa_Hist.get(is+1,il,2).fill(adcc,pixela,1.0);						
						H1_ECa_Maps.get(is+1,ic+6,1).fill(pixela,adcc);
						H1_ECa_Maps.get(is+1,il,0).fill(pixela,adcc);
						H1_ECa_Maps.get(is+1,il,2).fill(pixela,Math.pow(adcc,2));

						if (good_uvwt) {
							if(l1==1) good_uvwt_save = good_uvwt;
							if(l1==4 && good_uvwt_save){
								double dtiff1 =  tdcr[is][il-1][0] -  tdcr[is][il+2][0];
								double dtiff2 = ftdcr[is][il-1][0] - ftdcr[is][il+2][0];
								H2_ECt_Hist.get(is+1,il-3,3).fill(dtiff1, strrt[is][il+2][0]);
								H2_ECt_Hist.get(is+1,il-3,4).fill(dtiff2, strrt[is][il+2][0]);
								H2_ECt_Hist.get(is+1,il  ,3).fill(dtiff1, strrt[is][il+2][0]);
								H2_ECt_Hist.get(is+1,il  ,4).fill(dtiff2, strrt[is][il+2][0]);
							}
						}
					}
				}	
			
				if (good_uvwt && (uvwt[is][ic]-2.0)>0.02 && (uvwt[is][ic]-2.0)<0.056) { 
					int pixelt=ecPix.pix(strrt[is][iic+0][0],strrt[is][iic+1][0],strrt[is][iic+2][0]);
					H1_ECt_Maps.get(is+1,ic+6,0).fill(pixelt,1.0);
					for (int il=l1; il<l2 ; il++){
						H2_ECt_Hist.get(is+1,il,1).fill(tdcr[is][il+2][0],strrt[is][il+2][0],1.0) ;
						H2_ECt_Hist.get(is+1,il,2).fill(tdcr[is][il+2][0],pixelt,1.0);						
						H1_ECt_Maps.get(is+1,ic+6,1).fill(pixelt,tdcr[is][il+2][0]);
						H1_ECt_Maps.get(is+1,il,0).fill(pixelt,tdcr[is][il+2][0]);
					}
				}	
			}
		}	
		}
	}
		
	@Override
	public void processEvent(DataEvent de) {
		
		EvioDataEvent event = (EvioDataEvent) de;
		   	 		
		float tdcmax=100000;
		boolean debug=false;
		int adc,ped,nsb=0,nsa=0,tet=0,npk=0,pedref=0,timf=0,timc=0;
		double mc_t=0.,tdc=0,tdcf=0;
 				
		if(event.hasBank("EC::true")!=true) {
			thr[0]=15 ; thr[1]=20;
		
			this.myarrays.clear();
			if (debug) event.getHandler().list();	
					
    		decoder.decode(event);
            List<DetectorBankEntry> strips = decoder.getDataEntries("EC");

            for(DetectorBankEntry strip : strips) {
                adc=ped=pedref=npk=timf=timc=0 ; tdc=tdcf=0;
             	int is  = strip.getDescriptor().getSector();
            	int il  = strip.getDescriptor().getLayer();
            	int ip  = strip.getDescriptor().getComponent();
            	int iord= strip.getDescriptor().getOrder();
            	int icr = strip.getDescriptor().getCrate(); 
            	int isl = strip.getDescriptor().getSlot(); 
            	int ich = strip.getDescriptor().getChannel(); 
            	
            	if(strip.getType()!=BankType.TDC) { // FADC MODE 7 or 1
            		FADCConfig config=fadc.getMap().get(icr,isl,ich);
            		   nsa = (int) config.getNSA();
            		   nsb = (int) config.getNSB();
            		   tet = (int) config.getTET();
            		pedref = (int) config.getPedestal();
            	} else {
            		int[] tdcc = (int[]) strip.getDataObject(); 
            		tdc = tdcc[0]*24./1000.;        
            	}
            	
            	if(strip.getType()==BankType.ADCFPGA) { // FADC MODE 7
            		int[] adcc= (int[]) strip.getDataObject();
            		ped = adcc[2];
            		npk = adcc[3];
            		adc = (adcc[1]-ped*(nsa+nsb))/10;
            		timf = DataUtils.getInteger(adcc[0],0,5);
            		timc = DataUtils.getInteger(adcc[0],6,14);
            		tdcf = timc*4.+timf*0.0625;
             	}
            	
            	if(strip.getType()==BankType.ADCPULSE) { // FADC MODE 1
            		fitter.setParams(tet,nsb,nsa,1,15);
            		short[] pulse = (short[]) strip.getDataObject();
            		fitter.fit(pulse);
            		adc = fitter.adc/10;
            		ped = fitter.ped;
            		for (int i=0 ; i< pulse.length ; i++) {
            			                       H2_ECa_Hist.get(is,il,5).fill(i,ip,pulse[i]-pedref);
            			if (app.isSingleEvent) H2_ECa_Sevd.get(is,il,0).fill(i,ip,pulse[i]-pedref);
            		}
            	}  
            	
              	H2_ECa_Hist.get(is,il,3).fill(pedref-ped, ip);
			    this.myarrays.fill(is, il, ip, adc, tdc, tdcf);		  
			    
			  //System.out.println("crate,slot,chan:"+icr+" "+isl+" "+ich);
			  //System.out.println("sector,layer,pmt,order"+is+" "+il+" "+ip+" "+iord);
			  //System.out.println("  nchan,tdc,adc,ped,pedref,nsb,nsa: "+npk+" "+tdc+" "+adc+" "+ped+" "+pedref+" "+nsb+" "+nsa);
			  //System.out.println("  tdc,timc,timf: "+tdc+" "+timc+" "+timf);
			              	
            }
        
		} 
		
		if(event.hasBank("EC::true")==true){
			EvioDataBank bank  = (EvioDataBank) event.getBank("EC::true");
			int nrows = bank.rows();
			for(int i=0; i < nrows; i++){
				mc_t = bank.getDouble("avgT",i);
			}	
		}
					
		if(event.hasBank("EC::dgtz")==true){
			
			inMC = true; thr[0]=thr[1]=5;
			this.myarrays.clear();
			
			EvioDataBank bank = (EvioDataBank) event.getBank("EC::dgtz");
			
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
       	           tdc = (((float)tdcc-(float)mc_t*1000)-tdcmax+1340000)/1000;		   	        	
        	      if(ic==1||ic==2) this.myarrays.fill(is, il+(ic-1)*3, ip, adc, tdc, tdcf);		      	   	   		    	
		    }
		}
				
		if (app.isSingleEvent) {
			this.myarrays.findPixels();	    // Process all pixels for SED
			this.myarrays.processSED();
		} else {
			this.myarrays.processPixels();	// Process only single pixels 
		}
	}
	
	private class FADCFitter {
		
		int tet,nsb,nsa,p1,p2;
		int mmsum,summing_in_progress;
		public int ped;
		public int adc;
		
		public FADCFitter() {	
		}
		
		public FADCFitter(int tet, int tsb, int tsa, int p1, int p2) {
			this.setParams(tet,tsb,tsa,p1,p2);
		}
		
		public final void setParams(int tet, int nsb, int nsa, int p1, int p2) {
			this.tet = tet;
			this.nsb = nsb;
			this.nsa = nsa;	
			this.p1  = p1;
			this.p2  = p2;
		}
		
		public void fit(short[] pulse) {
			ped=0;adc=0;mmsum=0;summing_in_progress=0;
			for (int mm=0; mm<pulse.length; mm++) {
				if(mm>p1 && mm<=p2)  ped+=pulse[mm];
				if(mm==p2)           ped=ped/(p2-p1);
				if(mm>p2 && mm<100) {
					if ((summing_in_progress==0) && pulse[mm]>ped+this.tet) {
					  summing_in_progress=1;
					  for (int ii=1; ii<this.nsb+1;ii++) adc+=(pulse[mm-ii]-ped);
					  mmsum=this.nsb;
					}
					if(summing_in_progress>0 && mmsum>(this.nsa+this.nsb)) summing_in_progress=-1;
					if(summing_in_progress>0) {adc+=(pulse[mm]-ped); mmsum++;}
				}
			}
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
			             colorfraction = getcolor((TreeMap<Integer, Object>) Lmap_a.get(is+1,layer,0), component);
		}
		if (colorfraction<0.05) colorfraction = 0.05;
		
		Color col = palette.getRange(colorfraction);
		shape.setColor(col.getRed(),col.getGreen(),col.getBlue());
	}

	public double getcolor(TreeMap<Integer,Object> map, int component) {
		double color=9;
		int opt=1;
		double val[] =(double[]) map.get(1); 
		double rmin  =(double)   map.get(2);
		double rmax  =(double)   map.get(3);
		double z=val[component];
		
		if (z==0) color=9;
		
		if (  inProcess==0)  color=(double)(z-rmin)/(rmax-rmin);
		if (!(inProcess==0)) color=(double)(Math.log10(z)-Math.log10(app.pixMin))/(Math.log10(app.pixMax)-Math.log10(app.pixMin));
		
		//System.out.println(z+" "+rmin+" "+" "+rmax+" "+color);
		if (color>1)   color=1;
		if (color<=0)  color=0.;

		return color;
	}
	
	public TreeMap<Integer, Object> toTreeMap(double dat[]) {
        TreeMap<Integer, Object> hcontainer = new TreeMap<Integer, Object>();
        hcontainer.put(1, dat);
        double[] b = Arrays.copyOf(dat, dat.length);
//        double min=100000,max=0;
//        for (int i =0 ; i < b.length; i++){
//        	if (b[i] !=0 && b[i] < min) min=b[i];
//        	if (b[i] !=0 && b[i] > max) max=b[i];
//        }
        Arrays.sort(b);
        double min = b[0]; double max=b[b.length-1];
        if (min<=0) min=0.0;
        hcontainer.put(2, min);
        hcontainer.put(3, max);
        return hcontainer;        
	}
		
	public void analyzeOccupancy() {
		
		// il=1-3 (U,V,W Inner strips) il=4-6 (U,V,W Outer Strips) il=7 (Inner Pixels) il=8 (Outer Pixels)
		
		for (int is=1;is<7;is++) {
			for (int il=1 ; il<7 ; il++) {
				int ill ; if (il<4) ill=7 ; else ill=8 ;
				H1_ECa_Maps.get(is,il,0).divide(H1_ECa_Maps.get(is,ill,0),H1_ECa_Maps.get(is,il,1)); //Normalize Raw View Energy Sum to Events
				H1_ECt_Maps.get(is,il,0).divide(H1_ECt_Maps.get(is,ill,0),H1_ECt_Maps.get(is,il,1)); //Normalize Raw View Timing Sum to Events
				H1_ECa_Maps.get(is,il,2).divide(H1_ECa_Maps.get(is,ill,0),H1_ECa_Maps.get(is,il,3)); //Normalize Raw ADC^2 Sum to Events
				Lmap_a.add(is,il,0, toTreeMap(H2_ECa_Hist.get(is,il,0).projectionY().getData()));    //Strip View ADC  
				Lmap_a.add(is,il+10,0, toTreeMap(H1_ECa_Maps.get(is,il,1).getData()));               //Pixel View ADC 
				Lmap_t.add(is,il,0, toTreeMap(H2_ECt_Hist.get(is,il,0).projectionY().getData()));    //Strip View TDC  
				Lmap_t.add(is,il,1, toTreeMap(H1_ECt_Maps.get(is,il,1).getData()));                  //Pixel View TDC  
			}		    
			for (int il=7; il<9; il++) {	
				H1_ECa_Maps.get(is, il, 1).divide(H1_ECa_Maps.get(is, il, 0),H1_ECa_Maps.get(is, il, 2)); // Normalize Raw Energy Sum to Events
				H1_ECt_Maps.get(is, il, 1).divide(H1_ECt_Maps.get(is, il, 0),H1_ECt_Maps.get(is, il, 2)); // Normalize Raw Timing Sum to Events
			}
	    	Lmap_a.add(is, 7,0, toTreeMap(H1_ECa_Maps.get(is,7,0).getData())); //Pixel Events Inner  
	    	Lmap_a.add(is, 8,0, toTreeMap(H1_ECa_Maps.get(is,8,0).getData())); //Pixel Events Outer  
		    Lmap_a.add(is, 9,0, toTreeMap(H1_ECa_Maps.get(is,7,2).getData())); //Pixel U+V+W Inner Energy     
		    Lmap_a.add(is,10,0, toTreeMap(H1_ECa_Maps.get(is,8,2).getData())); //Pixel U+V+W Outer Energy    
		    Lmap_t.add(is, 7,2, toTreeMap(H1_ECt_Maps.get(is,7,2).getData())); //Pixel U+V+W Inner Time  
		    Lmap_t.add(is, 8,2, toTreeMap(H1_ECt_Maps.get(is,8,2).getData())); //Pixel U+V+W Outer Time 
			if (app.isSingleEvent){
				for (int il=1 ; il<9 ; il++) Lmap_a.add(is,il,0,  toTreeMap(H1_ECa_Sevd.get(is,il,0).getData())); 
			}
		}
		
	}	
	public void analyzeAttenuation(int is1, int is2, int il1, int il2, int ip1, int ip2) {
		
		TreeMap<Integer, Object> map;
		CalibrationData fits ; 	
		boolean doCalibration=false;
		double meanerr[] = new double[1296];
		 
		
		for (int is=is1 ; is<is2 ; is++) {
			for (int il=il1 ; il<il2 ; il++) {
				int ill ; if (il<4) ill=7 ; else ill=8;
				double cnts[]  = H1_ECa_Maps.get(is+1,ill,0).getData();
				
				double adc[]   = H1_ECa_Maps.get(is+1,il,1).getData();
				double adcsq[] = H1_ECa_Maps.get(is+1,il,3).getData();
				doCalibration = false;
				for (int ipix=0 ; ipix<1296 ; ipix++){
					meanerr[ipix]=0;
					//if (is==1) System.out.println("il,ipix,cnts,adc = "+il+" "+ipix+" "+cnts[ipix]+" "+adc[ipix]);
					if (cnts[ipix]>1) {
						meanerr[ipix]=Math.sqrt((adcsq[ipix]-adc[ipix]*adc[ipix]-8.3)/(cnts[ipix]-1)); //Sheppard's correction: c^2/12 c=10
						doCalibration = true;
					}
//					if (cnts[ipix]==2) {
//						meanerr[ipix]=20.;
//						doCalibration = true;
//					}					
					if (cnts[ipix]==1) {
						meanerr[ipix]=8.3;
						doCalibration = true;
					}
					  
				}
				
				map = (TreeMap<Integer, Object>) Lmap_a.get(is+1,il+10,0);
				double meanmap[] = (double[]) map.get(1);
				
				for (int ip=ip1 ; ip<ip2 ; ip++){
					if (doCalibration){
						fits = new CalibrationData(is,il,ip);
						fits.getDescriptor().setType(DetectorType.EC);
						fits.addGraph(ecPix.getpixels(il,ip+1,meanmap),ecPix.getpixels(il,ip+1,meanerr));
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
		  this.canvasMode1(desc, app.getCanvas("Mode1"));
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
		}
	}
	
	public void analyze(int process) {
		
		this.inProcess = process;
		if (process==1)  this.analyzeOccupancy();	 //Don't analyze until event counter sets process flag
		if (process==2)  this.analyzeOccupancy();	 //Final analysis for end of run 		
	}
	
	public void canvasMode1(DetectorDescriptor desc, EmbeddedCanvas canvas) {
		
		int is    = desc.getSector();
		int layer = desc.getLayer();
		int ic    = desc.getComponent();
		
		if (layer>6) return;
		
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
		
		canvas.divide(6,6);
		canvas.setAxisFontSize(14);
		canvas.setTitleFontSize(14);
		
		H1D h = new H1D() ; 
		String otab[]={"U Inner Strip","V Inner Strip","W Inner Strip","U Outer Strip","V Outer Strip","W Outer Strip"};
		
	    for(int ip=0;ip<36;ip++){
	    	canvas.cd(ip); canvas.getPad().setAxisRange(0.,100.,-15.,400.);
	        h = H2_ECa_Sevd.get(is+1,layer,0).sliceY(ip); h.setXTitle("Sample (4 ns)"); h.setYTitle("Counts");
	    	h.setTitle(otab[layer-1]+" "+(ip+1)); h.setFillColor(4); canvas.draw(h);
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
		//TStyle.setOptStat
		canvas.divide(6,3);
		canvas.setAxisFontSize(14);
		canvas.setTitleFontSize(14);
		canvas.setStatBoxFontSize(12);
		
	    //TStyle.setStatBoxFont(TStyle.getStatBoxFontName(),12);
	    //TStyle.setAxisFont(TStyle.getAxisFontName(),8);
	    		
		if (layer<7)  {col0=0 ; col1=4; col2=2;strip=ic+1;}
		if (layer>=7) {col0=4 ; col1=4; col2=2;pixel=ic+1;}
    
	    for(int il=1;il<7;il++){
	    	canvas.cd(il-1); canvas.getPad().setAxisRange(-1.,37.,0.,300.);
	    	h = H1_ECa_Sevd.get(is+1,il,0); h.setXTitle(otab[il-1]); h.setFillColor(col0); canvas.draw(h);
	    }
	}
	
	public void canvasPedestal(DetectorDescriptor desc, EmbeddedCanvas canvas) {
		
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
	    	H2D hpix = H2_ECa_Hist.get(is,il+(io-1)*3,3);
    		hpix.setXTitle("PED (Ref-Measured)") ; hpix.setYTitle(otab[ic][il-1]);
    	 
    		canvas.cd(il-1); canvas.setLogZ(); canvas.draw(hpix);
    		
    		if(la==il) {
    			F1D f1 = new F1D("p0",-10.,10.); f1.setParameter(0,ip);
    			F1D f2 = new F1D("p0",-10.,10.); f2.setParameter(0,ip+1);
    			f1.setLineColor(2); canvas.draw(f1,"same"); 
    			f2.setLineColor(2); canvas.draw(f2,"same");
    		}
    		
			//TStyle.setOptStat(true);
    		canvas.cd(il-1+3); h=hpix.sliceY(22) ; h.setFillColor(4) ; h.setTitle(""); h.setXTitle("STRIP "+22) ; canvas.draw(h);
    	    if(la==il) {h=hpix.sliceY(ip); h.setFillColor(2); h.setTitle(""); h.setXTitle("STRIP "+(ip+1));canvas.draw(h);}
			//TStyle.setOptStat(false);
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
			H2D hpix = H2_ECt_Hist.get(is,il+(io-1)*3,4);
    		hpix.setXTitle("TDIF (Inner-Outer)") ; hpix.setYTitle(otab[ic][il-1]);
    		canvas.cd(il-1); canvas.setLogZ(); canvas.draw(hpix);
    		if(la==il) {
    			F1D f1 = new F1D("p0",-15.,15.); f1.setParameter(0,ip);
    			F1D f2 = new F1D("p0",-15.,15.); f2.setParameter(0,ip+1);
    			f1.setLineColor(2); canvas.draw(f1,"same"); 
    			f2.setLineColor(2); canvas.draw(f2,"same");
    		}
			//TStyle.setOptStat(true);
    		canvas.cd(il-1+3); h=hpix.sliceY(22) ; h.setFillColor(4) ; h.setTitle(""); h.setXTitle("STRIP "+22) ; canvas.draw(h);
    	    if(la==il) {h=hpix.sliceY(ip); h.setFillColor(2); h.setTitle(""); h.setXTitle("STRIP "+(ip+1)); canvas.draw(h);}
			//TStyle.setOptStat(false);
	    }	
	}	
	
	public void canvasAttenuation(DetectorDescriptor desc, EmbeddedCanvas canvas) {
		
	    double[] xp     = new double[36];
	    double[] xpe    = new double[36];
	    double[] yp     = new double[36]; 
	    double[] vgain  = new double[36];
	    double[] vgaine = new double[36]; 
	    double[] vatt   = new double[36];
	    double[] vatte  = new double[36]; 
	    double[] vattdb = new double[36];
	    double[] vattdbe= new double[36];
	    double[] vchi2  = new double[36];
	    double[] vchi2e = new double[36]; 
	    double[] mip    = {100.,160.};
	    
		String otab[]={"U Inner Strips","V Inner Strips","W Inner Strips","U Outer Strips","V Outer Strips","W Outer Strips"};
		double pixwidth[]={5.35,5.92,5.92,5.55,6.15,6.15};
	    
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
		//System.out.println("layer,panel,io,of,l1,l2= "+layer+" "+panel+" "+io+" "+of+" "+l1+" "+l2);
        if (inProcess==2) {this.analyzeAttenuation(0,6,1,7,0,36);inProcess=3;}
		 
		if (layer<7||(layer>10&&layer<17)) {
			if (inProcess>0) {
				if (layer>10) {layer=layer-10; lay=layer;int component = ecPix.pixmap[layer-1-of][ic]; ic=component-1;}
			    if (inProcess==1)  {this.analyzeAttenuation(is,is+1,layer,layer+1,0,36);}
				if (collection.hasEntry(is, layer, ic)) {
									
				for (int ip=0; ip<36 ; ip++) {
					double gain  =  collection.get(1,layer,ip).getFunc(0).parameter(0).value();
					double gaine =  collection.get(1,layer,ip).getFunc(0).parameter(0).error();	
					double att   = -collection.get(1,layer,ip).getFunc(0).parameter(1).value();
					double atte  =  collection.get(1,layer,ip).getFunc(0).parameter(1).error();
					double chi2  =  collection.get(1,layer,ip).getChi2(0);
					int index = ECCommon.getCalibrationIndex(is+1,layer+3,ip+1);
					double attdb = ccdb.getDouble("/calibration/ec/attenuation/B",index);
					if (att>0) att=1./att; else att=0 ; 
					atte = Math.min(30,att*att*atte);
					xp[ip]=ip ; xpe[ip]=0. ; 
					if (gain>0) gaine = Math.min(30,gaine); vgain[ip]  = gain; vgaine[ip] = gaine;
		             vatt[ip] = Math.min(80, att)*pixwidth[lay-1] ; vatte[ip]=atte*pixwidth[lay-1];
		           vattdb[ip] = attdb; vattdbe[ip] = 0.;
		            vchi2[ip] = Math.min(4, chi2) ; vchi2e[ip]=0.;   
				}
				
	            GraphErrors   gainGraph = new GraphErrors(xp,vgain,xpe,vgaine);
	            GraphErrors    attGraph = new GraphErrors(xp,vatt,xpe,vatte);
	            GraphErrors  attdbGraph = new GraphErrors(xp,vattdb,xpe,vattdbe);
	            GraphErrors   chi2Graph = new GraphErrors(xp,vchi2,xpe,vchi2e);
	             
	            gainGraph.setMarkerStyle(2);   gainGraph.setMarkerSize(6);   gainGraph.setMarkerColor(2);
	             attGraph.setMarkerStyle(2);    attGraph.setMarkerSize(6);    attGraph.setMarkerColor(2);
	           attdbGraph.setMarkerStyle(2);  attdbGraph.setMarkerSize(7);  attdbGraph.setMarkerColor(1);
	            chi2Graph.setMarkerStyle(2);   chi2Graph.setMarkerSize(6);   chi2Graph.setMarkerColor(2);
	            gainGraph.setXTitle(otab[lay-1]) ; gainGraph.setYTitle("PMT GAIN")         ; gainGraph.setTitle(" ");
	             attGraph.setXTitle(otab[lay-1]) ;  attGraph.setYTitle("ATTENUATION (CM)") ;  attGraph.setTitle(" ");
		        chi2Graph.setXTitle(otab[lay-1]) ; chi2Graph.setYTitle("REDUCED CHI^2")    ; chi2Graph.setTitle(" ");
		        
	            F1D f1 = new F1D("p0",0,37); f1.setParameter(0,mip[io-1]); f1.setLineStyle(2);
		        
	            double ymax=200; if(!inMC) ymax=350;
				canvas.cd(0);canvas.getPad().setAxisRange("Y",0.,ymax);
				canvas.draw(collection.get(is,layer,ic).getRawGraph(0));
				canvas.draw(collection.get(is,layer,ic).getFitGraph(0),"same");
				canvas.draw(collection.get(is,layer,ic).getFunc(0),"same");
				
				canvas.cd(1);           canvas.getPad().setAxisRange(-1.,37.,0.,4.)   ; canvas.draw(chi2Graph); 
	            canvas.cd(2); if(!inMC) canvas.getPad().setAxisRange(-1.,37.,0.,400.) ; canvas.draw(gainGraph); canvas.draw(f1,"same"); 
	            canvas.cd(3);           canvas.getPad().setAxisRange(-1.,37.,0.,600.) ; canvas.draw(attGraph);  canvas.draw(attdbGraph,"same");                                                  
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
			canvas.cd(il-1-of); h = H2_ECa_Hist.get(is+1,il,0).projectionY(); h.setXTitle(otab); h.setFillColor(col0); canvas.draw(h);
			}
		
		l=layer;
		
		if (layer<7) {
			canvas.cd(l-1-of); h = H2_ECa_Hist.get(is+1,l,0).projectionY(); h.setFillColor(col1); canvas.draw(h,"same");
			H1D copy = h.histClone("Copy"); copy.reset() ; 
			copy.setBinContent(ic, h.getBinContent(ic)); copy.setFillColor(col2); canvas.draw(copy,"same");
			for(int il=l1;il<l2;il++) {
				String alab = lab1[il-1-of]+lab2[io-1]+lab3[0]+strip+lab4[0];String tlab = lab1[il-1-of]+lab2[io-1]+lab3[0]+strip+lab4[1];
				if(layer!=il) {canvas.cd(il+2-of); h = H2_ECa_Hist.get(is+1,il,0).sliceY(22); h.setXTitle(alab); h.setTitle(""); h.setFillColor(col0); canvas.draw(h);}
				if(layer!=il) {canvas.cd(il+5-of); h = H2_ECt_Hist.get(is+1,il,0).sliceY(22); h.setXTitle(tlab); h.setTitle(""); h.setFillColor(col0); canvas.draw(h);}
				}
			String alab = lab1[l-1-of]+lab2[io-1]+lab3[0]+strip+lab4[0];String tlab = lab1[l-1-of]+lab2[io-1]+lab3[0]+strip+lab4[1];
			canvas.cd(l+2-of); h = H2_ECa_Hist.get(is+1,l,0).sliceY(ic);h.setXTitle(alab); h.setTitle(""); h.setFillColor(col2); canvas.draw(h);
			canvas.cd(l+5-of); h = H2_ECt_Hist.get(is+1,l,0).sliceY(ic);h.setXTitle(tlab); h.setTitle(""); h.setFillColor(col2); canvas.draw(h);
			}
		
		if (layer==7||layer==8) {
			for(int il=l1;il<l2;il++) {
				canvas.cd(il-1-of); h = H2_ECa_Hist.get(is+1,il,0).projectionY();
				H1D copy = h.histClone("Copy");
				strip = ecPix.pixmap[il-1-of][ic];
				copy.reset() ; copy.setBinContent(ic, h.getBinContent(ic));
				copy.setFillColor(col2); canvas.draw(copy,"same");	    		 
				String alab = lab1[il-1-of]+lab2[io-1]+lab3[0]+strip+lab4[0];String tlab = lab1[il-1-of]+lab2[io-1]+lab3[0]+strip+lab4[1];
				canvas.cd(il+2-of) ; h = H2_ECa_Hist.get(is+1,il,0).sliceY(strip-1); h.setXTitle(alab); h.setTitle("");h.setFillColor(col2); canvas.draw(h);
				canvas.cd(il+5-of) ; h = H2_ECt_Hist.get(is+1,il,0).sliceY(strip-1); h.setXTitle(tlab); h.setTitle("");h.setFillColor(col2); canvas.draw(h);
				}
			}
		
		if (layer>8) {
			for(int il=l1;il<l2;il++) {
				canvas.cd(il-1-of); h = H2_ECa_Hist.get(is+1,il,1).projectionY();
				H1D copy = h.histClone("Copy");
				strip = ecPix.pixmap[il-1-of][ic];
				copy.reset() ; copy.setBinContent(strip-1, h.getBinContent(strip-1));
				copy.setFillColor(col2); canvas.draw(copy,"same");	
				String alab1 = lab1[il-1-of]+lab2[io-1]+lab3[0]+strip+lab4[0];String tlab1 = lab1[il-1-of]+lab2[io-1]+lab3[0]+strip+lab4[1];
				String alab2 = lab1[il-1-of]+lab2[io-1]+lab3[1]+pixel+lab4[0];String tlab2 = lab1[il-1-of]+lab2[io-1]+lab3[1]+pixel+lab4[1];
				if (layer<17) {
					canvas.cd(il+2-of) ; h = H2_ECa_Hist.get(is+1,il,1).sliceY(ecPix.pixmap[il-1-of][ic]-1); h.setXTitle(alab1); h.setTitle("");h.setFillColor(col2); canvas.draw(h);
					canvas.cd(il+5-of) ; h = H2_ECa_Hist.get(is+1,il,2).sliceY(ic)                   ; h.setXTitle(alab2); h.setTitle("");h.setFillColor(col2); canvas.draw(h);
					}
				if (layer>16&&layer<22) {
					canvas.cd(il+2-of) ; h = H2_ECt_Hist.get(is+1,il,1).sliceY(ecPix.pixmap[il-1-of][ic]-1); h.setXTitle(tlab1); h.setTitle("");h.setFillColor(col2); canvas.draw(h);
					canvas.cd(il+5-of) ; h = H2_ECt_Hist.get(is+1,il,2).sliceY(ic)                   ; h.setXTitle(tlab2); h.setTitle("");h.setFillColor(col2); canvas.draw(h);
					}
				}  	 
			}
		
	}
	
}
