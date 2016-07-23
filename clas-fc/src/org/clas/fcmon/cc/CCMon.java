package org.clas.fcmon.cc;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

import javax.swing.SwingUtilities;

import org.clas.fcmon.tools.CCPixels;
import org.clas.fcmon.tools.DetectorMonitor;
import org.clas.fcmon.tools.DetectorShapeView2D;
import org.clas.fcmon.tools.MonitorApp;
import org.clas.tools.Miscellaneous;
import org.jlab.clas.detector.BankType;
import org.jlab.clas.detector.DetectorBankEntry;
import org.jlab.clas.detector.DetectorCollection;
import org.jlab.clas.detector.DetectorDescriptor;
import org.jlab.clas.detector.DetectorType;
import org.jlab.clas12.calib.DetectorShape2D;
import org.jlab.clas12.detector.EventDecoder;
import org.jlab.clas12.detector.FADCConfig;
import org.jlab.clas12.detector.FADCConfigLoader;
import org.jlab.clasrec.utils.DatabaseConstantProvider;
import org.jlab.data.io.DataEvent;
import org.jlab.evio.clas12.EvioDataEvent;
import org.jlab.geom.prim.Path3D;
import org.root.attr.ColorPalette;
import org.root.basic.EmbeddedCanvas;
import org.root.func.F1D;
import org.root.group.TDirectory;
import org.root.histogram.H1D;
import org.root.histogram.H2D;

public class CCMon extends DetectorMonitor {
	
    static MonitorApp           app = new MonitorApp("LTCCMon",1800,950);		
	EventDecoder            decoder = new EventDecoder();
	FADCConfigLoader          fadc  = new FADCConfigLoader();
	FADCFitter              fitter  = new FADCFitter();
	DatabaseConstantProvider   ccdb = new DatabaseConstantProvider(12,"default");
	TDirectory         mondirectory = new TDirectory(); 	
	ColorPalette            palette = new ColorPalette();
	CCPixels                  ccPix = new CCPixels();
	MyArrays               myarrays = new MyArrays();
	Miscellaneous             extra = new Miscellaneous();

	int inProcess          = 0; //0=init 1=processing 2=end-of-run 3=post-run
	boolean inMC           = false; //true=MC false=DATA
	int thrcc              = 20;
	int nsa,nsb,tet,p1,p2,pedref  = 0;
	int ipsave             = 0;
	
	DetectorCollection<H1D> H1_CCa_Sevd = new DetectorCollection<H1D>();
	DetectorCollection<H1D> H1_CCt_Sevd = new DetectorCollection<H1D>();
	DetectorCollection<H2D> H2_CCa_Hist = new DetectorCollection<H2D>();
	DetectorCollection<H2D> H2_CCt_Hist = new DetectorCollection<H2D>();
	DetectorCollection<H2D> H2_CCa_Sevd = new DetectorCollection<H2D>();
	
	DetectorCollection<TreeMap<Integer,Object>> Lmap_a = new DetectorCollection<TreeMap<Integer,Object>>();	 
	
	TreeMap<String,Object> glob = new TreeMap<String,Object>();
	   
	public CCMon(String[] args) {
		super("CCMON", "1.0", "lcsmith");
		fadc.load("/daq/fadc/ltcc",10,"default");
	}

	public static void main(String[] args){
		
		CCMon monitor = new CCMon(args);
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				app.setPluginClass(monitor);
				app.init();
				app.addCanvas("Mode1");
				app.addCanvas("SingleEvent");
				app.addCanvas("Occupancy");			 
				app.addCanvas("Pedestals");
				app.addCanvas("Summary");
				monitor.init();
				monitor.initDetector(0,6);
				}
			});
	}
	
	@Override
	public void init() {
	  inProcess = 0;
	  initHistograms();
	  configMode7(1,18,12);
	  app.mode7Emulation.ttet.setText(Integer.toString(this.tet));
   	  app.mode7Emulation.tnsa.setText(Integer.toString(this.nsa));
   	  app.mode7Emulation.tnsb.setText(Integer.toString(this.nsb));
	}
	
	public TreeMap<String,Object> getGlob(){
		return this.glob;
	}		
	@Override
	public void reset() {
		this.clearHistograms();
	}
	
	@Override
	public void close() {
		
	}	
	
	@Override
	public void saveToFile() {
		
	}
	
	public void initHistograms() {
	
		for (int is=1; is<7 ; is++) {
			for (int il=1 ; il<3 ; il++){
				H2_CCa_Hist.add(is, il, 0, new H2D("CCa_Hist_Raw_"+il, 100,   0., 2000.,  18, 1., 19.));
				H2_CCt_Hist.add(is, il, 0, new H2D("CCt_Hist_Raw_"+il, 100,1330., 1370.,   1, 1., 19.));
				H2_CCa_Hist.add(is, il, 3, new H2D("CCa_Hist_PED_"+il,  40, -20.,  20.,   18, 1., 19.)); 
				H2_CCa_Hist.add(is, il, 5, new H2D("CCa_Hist_FADC_"+il,100,   0., 100.,   18, 1., 19.));
				H1_CCa_Sevd.add(is, il, 0, new H1D("ECa_Sed_"+il,       18,   1.,  19.));
				H2_CCa_Sevd.add(is, il, 0, new H2D("CCa_Sed_FADC_"+il, 100,   0., 100.,   18, 1., 19.));
				H2_CCa_Sevd.add(is, il, 1, new H2D("CCa_Sed_FADC_"+il, 100,   0., 100.,   18, 1., 19.));
			}
		}		
	}

	
	public void clearHistograms() {
		
		for (int is=1 ; is<7 ; is++) {
			for (int il=1 ; il<3 ; il++) {
				 H2_CCa_Hist.get(is,il,0).reset();
				 H2_CCa_Hist.get(is,il,3).reset();
				 H2_CCa_Hist.get(is,il,5).reset();
			}
		}		
	}
	
	public void initDetector(int is1, int is2) {
		
        DetectorShapeView2D  dv1 = new DetectorShapeView2D("LTCC");
		
		for(int is=is1; is<is2; is++) {
			for(int ip=0; ip<18 ; ip++)    dv1.addShape(getMirror(is,1,ip));
			for(int ip=0; ip<18 ; ip++)    dv1.addShape(getMirror(is,2,ip));
		}		
		app.getDetectorView().addDetectorLayer(dv1);
		app.getDetectorView().addDetectorListener(this);
	}

	
	public DetectorShape2D getMirror(int sector, int layer, int mirror) {
		
	    DetectorShape2D shape = new DetectorShape2D(DetectorType.LTCC,sector,layer,mirror);	    
	    Path3D shapePath = shape.getShapePath();
	    
	    int off = (layer-1)*18;
	    
	    for(int j = 0; j < 4; j++){
	    	shapePath.addPoint(ccPix.cc_xpix[j][mirror+off][sector],ccPix.cc_ypix[j][mirror+off][sector],0.0);
	    }
	    return shape;		
	}
	
	private class MyArrays {
		
		int        nha[][] = new    int[6][2];
		int        nht[][] = new    int[6][2];
		int    strra[][][] = new    int[6][2][18]; 
		int    strrt[][][] = new    int[6][2][18]; 
		int     adcr[][][] = new    int[6][2][18];		
		double  tdcr[][][] = new double[6][2][18];	
		
		public MyArrays() {	
		}
		
		public void clear() {
			
			for (int is=0 ; is<6 ; is++) {
				for (int il=0 ; il<2 ; il++) {
					nha[is][il] = 0;
					nht[is][il] = 0;
					for (int ip=0 ; ip<18 ; ip++) {
						strra[is][il][ip] = 0;
						strrt[is][il][ip] = 0;
						 adcr[is][il][ip] = 0;
						 tdcr[is][il][ip] = 0;
					}
				}
			}
			
			if (app.isSingleEvent()) {
				for (int is=0 ; is<6 ; is++) {
					for (int il=0 ; il<2 ; il++) {
						 H1_CCa_Sevd.get(is+1,il+1,0).reset();
						 H2_CCa_Sevd.get(is+1,il+1,0).reset();
						 H2_CCa_Sevd.get(is+1,il+1,1).reset();
					}
				}
			}	
		}
		
		public void fill(int is, int il, int ip, int adc, double tdc, double tdcf) {
				
			if(tdc>1200&&tdc<1500){
	          	 nht[is-1][il-1]++; int inh = nht[is-1][il-1];
	            tdcr[is-1][il-1][inh-1] = tdc;
	           strrt[is-1][il-1][inh-1] = ip;
	          	  H2_CCt_Hist.get(is,il,0).fill(tdc,ip,1.0);
	          	  }
	   	    if(adc>thrcc){
	          	 nha[is-1][il-1]++; int inh = nha[is-1][il-1];
	            adcr[is-1][il-1][inh-1] = adc;
	           strra[is-1][il-1][inh-1] = ip;
	          	  H2_CCa_Hist.get(is,il,0).fill(adc,ip,1.0);
	          	  }	
		}
		
		public void processSED() {
			
			for (int is=0; is<6; is++) {
	           for (int il=0; il<2; il++ ){;
	        	   for (int n=0 ; n<nha[is][il] ; n++) {
	        		   int ip=strra[is][il][n]; int ad=adcr[is][il][n];
	        		   H1_CCa_Sevd.get(is+1,il+1,0).fill(ip,ad);
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
		int tdc=0,tdcf=0;
		
		if(event.hasBank("EC::true")!=true) {
			this.myarrays.clear();
			decoder.decode(event);
			
            List<DetectorBankEntry> strips = decoder.getDataEntries("LTCC");
            for(DetectorBankEntry strip : strips) {
            	int icr = strip.getDescriptor().getCrate(); 
            	int isl = strip.getDescriptor().getSlot(); 
            	int ich = strip.getDescriptor().getChannel(); 
             	int is  = strip.getDescriptor().getSector();
            	int il  = strip.getDescriptor().getLayer();
            	int ip  = strip.getDescriptor().getComponent();
            	int io  = strip.getDescriptor().getOrder()+1;            
            	
            	if(strip.getType()==BankType.ADCPULSE) { // FADC MODE 1
            		short[] pulse = (short[]) strip.getDataObject();
             		this.configMode7(icr,isl,ich);
            		fitter.fit(this.nsa,this.nsb,this.tet,pulse);
            		for (int i=0 ; i< pulse.length ; i++) {
            			H2_CCa_Hist.get(is,io,5).fill(i,ip,pulse[i]-this.pedref);
            			if (app.isSingleEvent()) {
            				H2_CCa_Sevd.get(is,io,0).fill(i,ip,pulse[i]-this.pedref);
            				int w1 = fitter.t0-this.nsb ; int w2 = fitter.t0+this.nsa;
            				if (fitter.adc>0&&i>=w1&&i<=w2) H2_CCa_Sevd.get(is,io,1).fill(i,ip,pulse[i]-this.pedref);
            			}
            		}
            	}           	
              	H2_CCa_Hist.get(is,io,3).fill(this.pedref-fitter.ped, ip);
			    this.myarrays.fill(is, io, ip, fitter.adc, tdc, tdcf);		  
            }            
		}		
		if (app.isSingleEvent()) this.myarrays.processSED();		
	}


	@Override
	public void update(DetectorShape2D shape) {
		
		int is        = shape.getDescriptor().getSector();
		int layer     = shape.getDescriptor().getLayer();
		int component = shape.getDescriptor().getComponent();
		
		double colorfraction=1;
		
		if (inProcess==0){ // Assign default colors upon starting GUI (before event processing)
			if(layer<7) colorfraction = (double)component/18;
		}
		if (inProcess>0){   		  // Use Lmap_a to get colors of components while processing data
			             colorfraction = getcolor((TreeMap<Integer, Object>) Lmap_a.get(is+1,layer,0), component);
		}
		if (colorfraction<0.05) colorfraction = 0.05;
		
		Color col = palette.getRange(colorfraction);
		shape.setColor(col.getRed(),col.getGreen(),col.getBlue());
		
	}
	
	public double getcolor(TreeMap<Integer,Object> map, int component) {
		
		double color = 0;
		
		double val[] =(double[]) map.get(1); 
		double rmin  =(double)   map.get(2);
		double rmax  =(double)   map.get(3);
		double z=val[component];
		
		if (z==0) return 0;
		
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
//        Arrays.sort(b);
//        double min = b[0]; double max=b[b.length-1];
        if (min<=0) min=0.01;
        hcontainer.put(2, min);
        hcontainer.put(3, max);
        return hcontainer;        
	}	
		
	@Override
	public void analyze(int process) {
		this.inProcess = process;
		if (process==1||process==2) this.analyzeOccupancy();
		
	}
	
	public void analyzeOccupancy() {
		
		for (int is=1;is<7;is++) {
			for (int il=1 ; il<3 ; il++) {
				if (!app.isSingleEvent()) Lmap_a.add(is,il,0, toTreeMap(H2_CCa_Hist.get(is,il,0).projectionY().getData())); //Strip View ADC 
				if  (app.isSingleEvent()) Lmap_a.add(is,il,0, toTreeMap(H1_CCa_Sevd.get(is,il,0).getData())); 			
			}
		}	
	}

	@Override
	public void detectorSelected(DetectorDescriptor desc) {
		
        this.analyze(inProcess);
        switch (app.getSelectedTabIndex()) {
        case 0:
          this.canvasMode1(desc, app.getCanvas("Mode1"));
          break;
        case 1:
		  //this.canvasSingleEvent(desc, app.getCanvas("SingleEvent"));
          break;
		case 2:
          this.canvasOccupancy(desc, app.getCanvas("Occupancy"));
          break;
		case 3:
          this.canvasPedestal(desc, app.getCanvas("Pedestals"));	
          break;
        case 4:
          this.canvasSummary(desc, app.getCanvas("Summary"));	
          break;
        }	 
    }
	
	public void canvasMode1(DetectorDescriptor desc, EmbeddedCanvas canvas) {
		
		int is = desc.getSector();
		int lr = desc.getLayer();
		int ic = desc.getComponent();
		
		canvas.divide(3,6);
		canvas.setAxisFontSize(14);
		canvas.setTitleFontSize(14);
		canvas.setAxisTitleFontSize(14);
		
		H1D h = new H1D() ; 
		String otab[]={" Left PMT "," Right PMT "};
		
		if (app.mode7Emulation.User_tet>0)  this.tet=app.mode7Emulation.User_tet;
		if (app.mode7Emulation.User_tet==0) this.tet=app.mode7Emulation.CCDB_tet;
		
		F1D f1 = new F1D("p0",0.,100.); f1.setParameter(0,this.tet);
		f1.setLineColor(2);
		F1D f2 = new F1D("p0",0.,100.); f2.setParameter(0,app.mode7Emulation.CCDB_tet);
		f2.setLineColor(4);f2.setLineStyle(2);
		
	    for(int ip=0;ip<18;ip++){
	    	canvas.cd(ip); canvas.getPad().setAxisRange(0.,100.,-15.,4000*app.displayControl.pixMax);
	        h = H2_CCa_Sevd.get(is+1,lr,0).sliceY(ip); h.setXTitle("Samples (4 ns)"); h.setYTitle("Counts");
	    	h.setTitle("Sector "+(is+1)+otab[lr-1]+(ip+1)); h.setFillColor(4); canvas.draw(h);
	        h = H2_CCa_Sevd.get(is+1,lr,1).sliceY(ip); h.setFillColor(2); canvas.draw(h,"same");
	        canvas.draw(f1,"same");canvas.draw(f2,"same");
	  	    }		
	}	
	
	public void canvasPedestal(DetectorDescriptor desc, EmbeddedCanvas canvas) {
		
		int is = desc.getSector();
		int lr = desc.getLayer();
		int ip = desc.getComponent();
		
		int col2=2,col4=4,col0=0;
		
		H1D h;
		String otab[]={" Left PMT "," Right PMT "};
 		
		canvas.divide(2,2);
		canvas.setAxisFontSize(14);
		canvas.setTitleFontSize(14);
		canvas.setAxisTitleFontSize(14);
		
	    for(int il=1;il<3;il++){
	    	H2D hpix = H2_CCa_Hist.get(is+1,il,3);
    		hpix.setXTitle("PED (Ref-Measured)") ; hpix.setYTitle(otab[il-1]);
    	 
    		canvas.cd(il-1); canvas.getPad().setAxisRange(-30.,30.,1.,19.) ; canvas.setLogZ(); canvas.draw(hpix);
    		
    		if(lr==il) {
    			F1D f1 = new F1D("p0",-30.,30.); f1.setParameter(0,ip+1);
    			F1D f2 = new F1D("p0",-30.,30.); f2.setParameter(0,ip+2);
    			f1.setLineColor(2); canvas.draw(f1,"same"); 
    			f2.setLineColor(2); canvas.draw(f2,"same");
    		}
    		
    		canvas.cd(il-1+2);
    		            h=hpix.sliceY(12); h.setFillColor(4); h.setTitle(""); h.setXTitle("Sector "+(is+1)+otab[il-1]+12)    ; canvas.draw(h,"S");
    	    if(lr==il) {h=hpix.sliceY(ip); h.setFillColor(2); h.setTitle(""); h.setXTitle("Sector "+(is+1)+otab[il-1]+(ip+1)); canvas.draw(h,"S");}
	    }			
	}
	
	public void canvasOccupancy(DetectorDescriptor desc, EmbeddedCanvas canvas) {
		
		int is = desc.getSector();
		int lr = desc.getLayer();
		int ip = desc.getComponent();
	
		int col0=0,col1=4,col2=2;
		
		H1D h1;  
		String otab[]={" Left "," Right "};
		String lab4[]={" ADC"," TDC"};		
		String xlab,ylab;
		
		canvas.divide(2,3);
		canvas.setAxisFontSize(14);
		canvas.setAxisTitleFontSize(14);
		canvas.setTitleFontSize(14);
		canvas.setStatBoxFontSize(12);
		
		H2D h2a = H2_CCa_Hist.get(is+1,1,0); h2a.setYTitle(otab[0]+"PMTs") ; h2a.setXTitle(otab[0]+"PMT"+lab4[0]);
		H2D h2b = H2_CCa_Hist.get(is+1,2,0); h2b.setYTitle(otab[1]+"PMTs") ; h2b.setXTitle(otab[1]+"PMT"+lab4[0]);
		canvas.cd(0); canvas.getPad().setAxisRange(0.,2000.,1.,19.) ; canvas.setLogZ(); canvas.draw(h2a); 
		canvas.cd(1); canvas.getPad().setAxisRange(0.,2000.,1.,19.) ; canvas.setLogZ(); canvas.draw(h2b); 
		
		canvas.cd(lr-1);
		
		F1D f1 = new F1D("p0",0.,2000.); f1.setParameter(0,ip+1);
		F1D f2 = new F1D("p0",0.,2000.); f2.setParameter(0,ip+2);
		f1.setLineColor(2); canvas.draw(f1,"same"); 
		f2.setLineColor(2); canvas.draw(f2,"same");
		
		for(int il=1;il<3;il++){
			xlab = "Sector "+(is+1)+otab[il-1]+"PMTs";
			canvas.cd(il+1); h1 = H2_CCa_Hist.get(is+1,il,0).projectionY(); h1.setXTitle(xlab); h1.setFillColor(col0); canvas.draw(h1);
			}	
		
		canvas.cd(lr+1); h1 = H2_CCa_Hist.get(is+1,lr,0).projectionY(); h1.setFillColor(col1); canvas.draw(h1,"same");
		H1D copy = h1.histClone("Copy"); copy.reset() ; 
		copy.setBinContent(ip, h1.getBinContent(ip)); copy.setFillColor(col2); canvas.draw(copy,"same");
		
		for(int il=1;il<3;il++) {
			String alab = otab[il-1]+"PMT "+11+lab4[0]; String tlab = otab[il-1]+(ip+1)+lab4[1];
			if(lr!=il) {canvas.cd(il+3); h1 = H2_CCa_Hist.get(is+1,il,0).sliceY(11); h1.setXTitle(alab); h1.setTitle(""); h1.setFillColor(col0); canvas.draw(h1,"S");}
			//if(lr!=il) {canvas.cd(il+3); h = H2_CCt_Hist.get(is+1,il,0).sliceY(22); h.setXTitle(tlab); h.setTitle(""); h.setFillColor(col0); canvas.draw(h);}
		}
		String alab = otab[lr-1]+"PMT "+(ip+1)+lab4[0]; String tlab = otab[lr-1]+(ip+1)+lab4[1];
		canvas.cd(lr+3); h1 = H2_CCa_Hist.get(is+1,lr,0).sliceY(ip);h1.setXTitle(alab); h1.setTitle(""); h1.setFillColor(col2); canvas.draw(h1,"S");
		//canvas.cd(lr+3); h = H2_CCt_Hist.get(is+1,lr,0).sliceY(ip+1);h.setXTitle(tlab); h.setTitle(""); h.setFillColor(col2); canvas.draw(h);	 	
	}
	
	public void canvasSummary(DetectorDescriptor desc, EmbeddedCanvas canvas) {
		
		int is = desc.getSector();
		int lr = desc.getLayer();
		int ip = desc.getComponent();
		
		int il=1,col0=0,col1=4,col2=2;
		
		H1D h;
		String alab;
		String otab[]={" Left PMT "," Right PMT "};
		String lab4[]={" ADC"," TDC"};		
		
		canvas.divide(6,6);
		canvas.setAxisFontSize(12);
		canvas.setAxisTitleFontSize(12);
		canvas.setTitleFontSize(14);
		canvas.setStatBoxFontSize(10);
		
		il = 1; 
		
		for(int iip=0;iip<18;iip++) {
			alab = otab[il-1]+(iip+1)+lab4[0];
			canvas.cd(iip); h = H2_CCa_Hist.get(is+1,il,0).sliceY(iip); h.setXTitle(alab); h.setTitle(""); h.setFillColor(col1); canvas.draw(h);
		}

		il = 2;
		
		for(int iip=0;iip<18;iip++) {
			alab = otab[il-1]+(iip+1)+lab4[0];
			canvas.cd(18+iip); h = H2_CCa_Hist.get(is+1,il,0).sliceY(iip); h.setXTitle(alab); h.setTitle(""); h.setFillColor(col1); canvas.draw(h);
		}
		
		canvas.cd((lr-1)*18+ip); h = H2_CCa_Hist.get(is+1,lr,0).sliceY(ip); h.setTitle(""); h.setFillColor(col2); canvas.draw(h,"same"); 	

	}

    @Override
    public void putGlob(String name, Object obj) {
        // TODO Auto-generated method stub
        
    }
}
