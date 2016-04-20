package org.jlab.ecmon.ui;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

import javax.swing.SwingUtilities;

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
import org.jlab.ecmon.utils.CCPixels;
import org.jlab.ecmon.utils.DetectorMonitor;
import org.jlab.ecmon.utils.DetectorShapeView2D;
import org.jlab.ecmon.utils.MonitorApp;
import org.jlab.evio.clas12.EvioDataEvent;
import org.jlab.geom.prim.Path3D;
import org.root.attr.ColorPalette;
import org.root.basic.EmbeddedCanvas;
import org.root.func.F1D;
import org.root.group.TDirectory;
import org.root.histogram.H1D;
import org.root.histogram.H2D;

public class CCMon extends DetectorMonitor {
	
	public static MonitorApp app;
		
	EventDecoder            decoder = new EventDecoder();
	FADCConfigLoader          fadc  = new FADCConfigLoader();
	FADCFitter              fitter  = new FADCFitter();
	DatabaseConstantProvider   ccdb = new DatabaseConstantProvider(12,"default");
	TDirectory         mondirectory = new TDirectory(); 	
	ColorPalette            palette = new ColorPalette();
	CCPixels                  ccPix = new CCPixels();
	MyArrays               myarrays = new MyArrays();

	int inProcess        = 0; //0=init 1=processing 2=end-of-run 3=post-run
	boolean inMC         = false; //true=MC false=DATA
	int thrcc            = 20;
	int   tet            = 0;
	
	DetectorCollection<H1D> H1_CCa_Sevd = new DetectorCollection<H1D>();
	DetectorCollection<H1D> H1_CCt_Sevd = new DetectorCollection<H1D>();
	DetectorCollection<H2D> H2_CCa_Hist = new DetectorCollection<H2D>();
	DetectorCollection<H2D> H2_CCt_Hist = new DetectorCollection<H2D>();
	DetectorCollection<H2D> H2_CCa_Sevd = new DetectorCollection<H2D>();
	
	DetectorCollection<TreeMap<Integer,Object>> Lmap_a = new DetectorCollection<TreeMap<Integer,Object>>();	 
	
	public CCMon(String[] args) {
		super("CCMON", "1.0", "lcsmith");
		fadc.load("/daq/fadc/ltcc",10,"default");
	}

	public static void main(String[] args){
		
		CCMon monitor = new CCMon(args);
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				app = new MonitorApp(2000,600);
				app.setPluginClass(monitor);
				app.addCanvas("Mode1");
				app.addCanvas("SingleEvent");
				app.addCanvas("Occupancy");			 
				app.addCanvas("Pedestals");
				app.addChangeListener();
				monitor.init();
				monitor.initDetector(0,6);
				}
			});
		}
	
	@Override
	public void init() {
	  inProcess = 0;
	  initHistograms();
	}
	
	@Override
	public void close() {
		
	}	
	
	public void initHistograms() {
	
		for (int is=1; is<7 ; is++) {
			for (int il=1 ; il<3 ; il++){
				H2_CCa_Hist.add(is, il, 0, new H2D("CCa_Hist_Raw_"+il, 100,   0., 2000.,  18, 1., 19.));
				H2_CCt_Hist.add(is, il, 0, new H2D("CCt_Hist_Raw_"+il, 100,1330., 1370.,   1, 1., 19.));
				H2_CCa_Hist.add(is, il, 3, new H2D("CCa_Hist_PED_"+il,  60, -30.,  30.,   18, 1., 19.)); 
				H2_CCa_Hist.add(is, il, 5, new H2D("CCa_Hist_FADC_"+il,100,   0., 100.,   18, 1., 19.));
				H1_CCa_Sevd.add(is, il, 0, new H1D("ECa_Sed_"+il,       18,   1.,  19.));
				H2_CCa_Sevd.add(is, il, 0, new H2D("CCa_Sed_FADC_"+il, 100,   0., 100.,   18, 1., 19.));
				H2_CCa_Sevd.add(is, il, 1, new H2D("CCa_Sed_FADC_"+il, 100,   0., 100.,   18, 1., 19.));
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
	
	private class FADCFitter {
		
		int tet,nsb,nsa,p1,p2;
		int mmsum,summing_in_progress;
		public int ped;
		public int adc;
		public int  t0;
		
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
					  t0 = mm;
					  for (int ii=1; ii<this.nsb+1;ii++) adc+=(pulse[mm-ii]-ped);
					  mmsum=this.nsb;
					}
					if(summing_in_progress>0 && mmsum>(this.nsa+this.nsb)) summing_in_progress=-1;
					if(summing_in_progress>0) {adc+=(pulse[mm]-ped); mmsum++;}
				}
			}
		}
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
					nha[is][il]  = 0;
					nht[is][il]  = 0;
					for (int ip=0 ; ip<18 ; ip++) {
						strra[is][il][ip] = 0;
						strrt[is][il][ip] = 0;
						 adcr[is][il][ip] = 0;
						 tdcr[is][il][ip] = 0;
					}
				}
			}
			
			if (app.isSingleEvent) {
				for (int is=0 ; is<6 ; is++) {
					for (int il=1 ; il<2 ; il++) {
						 H1_CCa_Sevd.get(is+1,il,0).reset();
						 H2_CCa_Sevd.get(is+1,il,0).reset();
						 H2_CCa_Sevd.get(is+1,il,1).reset();
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
	           for (int il=1; il<3; il++ ){;
	        	   for (int n=1 ; n<nha[is][il-1]+1 ; n++) {
	        		   int ip=strra[is][il-1][n-1]; int ad=adcr[is][il-1][n-1];
	        		   H1_CCa_Sevd.get(is+1,il,0).fill(ip,ad);
	        	   }
	           }
			}			
		}

	}
	
	@Override
	public void processEvent(DataEvent de) {
		
		EvioDataEvent event = (EvioDataEvent) de;
		int adc=0,ped=0,t0=0,nsa,nsb,tet,pedref=0,tdc=0,tdcf=0;
		
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
//  			  System.out.println("crate,slot,chan:"+icr+" "+isl+" "+ich);
//  			  System.out.println("sector,layer,pmt,order"+is+" "+il+" "+ip+" "+io);
            	
            	if(strip.getType()==BankType.ADCPULSE) { // FADC MODE 1
            		FADCConfig config=fadc.getMap().get(icr,isl,ich);
         		   nsa = (int) config.getNSA();
         		   nsb = (int) config.getNSB();
         		   tet = (int) config.getTET();
         		   tet = 60;
         		   this.tet = tet;
         		pedref = (int) config.getPedestal();
            		fitter.setParams(tet,nsb,nsa,1,15);
            		short[] pulse = (short[]) strip.getDataObject();
            		fitter.fit(pulse);
            		adc = fitter.adc;
            		ped = fitter.ped;
            		 t0 = fitter.t0;
            		for (int i=0 ; i< pulse.length ; i++) {
            			                       H2_CCa_Hist.get(is,io,5).fill(i,ip,pulse[i]-pedref);
            			if (app.isSingleEvent) {
            				H2_CCa_Sevd.get(is,io,0).fill(i,ip,pulse[i]-pedref);
            				if (i>=(t0-nsb)&&i<=(t0+nsa)) H2_CCa_Sevd.get(is,io,1).fill(i,ip,pulse[i]-pedref);
            			}
            		}
            	}
            	
              	H2_CCa_Hist.get(is,io,3).fill(pedref-ped, ip);
			    this.myarrays.fill(is, io, ip, adc, tdc, tdcf);		  
            }
            
		}
		
		if (app.isSingleEvent) this.myarrays.processSED();
		
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
	
	@Override
	public void analyze(int process) {
		this.inProcess = process;
		if (process==1||process==2) this.analyzeOccupancy();
		
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
		
		for (int is=1;is<7;is++) {
			for (int il=1 ; il<3 ; il++) {
				Lmap_a.add(is,il,0, toTreeMap(H2_CCa_Hist.get(is,il,0).projectionY().getData()));    //Strip View ADC 
				if (app.isSingleEvent) Lmap_a.add(is,il,0,  toTreeMap(H1_CCa_Sevd.get(is,il,0).getData())); 			
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
		  this.canvasOccupancy(desc,   app.getCanvas("Occupancy"));
		  break;
		case 3:
		  this.canvasPedestal(desc,    app.getCanvas("Pedestals"));	
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
		
		F1D f1 = new F1D("p0",0.,100.); f1.setParameter(0,this.tet);
		f1.setLineColor(2);
		
	    for(int ip=0;ip<18;ip++){
	    	canvas.cd(ip); canvas.getPad().setAxisRange(0.,100.,-15.,app.pixMax);
	        h = H2_CCa_Sevd.get(is+1,lr,0).sliceY(ip); h.setXTitle("Samples (4 ns)"); h.setYTitle("Counts");
	    	h.setTitle("Sector "+(is+1)+otab[lr-1]+(ip+1)); h.setFillColor(4); canvas.draw(h);
	        h = H2_CCa_Sevd.get(is+1,lr,1).sliceY(ip); h.setFillColor(2); canvas.draw(h,"same");
	        canvas.draw(f1,"same");
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
    		            h=hpix.sliceY(12); h.setFillColor(4); h.setTitle(""); h.setXTitle("Sector "+(is+1)+otab[il-1]+12)    ; canvas.draw(h);
    	    if(lr==il) {h=hpix.sliceY(ip); h.setFillColor(2); h.setTitle(""); h.setXTitle("Sector "+(is+1)+otab[il-1]+(ip+1)); canvas.draw(h);}
	    }			
	}
	public void canvasOccupancy(DetectorDescriptor desc, EmbeddedCanvas canvas) {
		
		int is = desc.getSector();
		int lr = desc.getLayer();
		int ip = desc.getComponent();
		
		int col0=0,col1=4,col2=2;
		
		H1D h;
		String otab[]={" Left PMT "," Right PMT "};
		String lab4[]={" ADC"," TDC"};		
		
		canvas.divide(2,2);
		canvas.setAxisFontSize(14);
		canvas.setAxisTitleFontSize(14);
		canvas.setTitleFontSize(14);
		canvas.setStatBoxFontSize(12);
		
		for(int il=1;il<3;il++){
			String xlab = "Sector "+(is+1)+otab[il-1]+"PMTs";
			canvas.cd(il-1); h = H2_CCa_Hist.get(is+1,il,0).projectionY(); h.setXTitle(xlab); h.setFillColor(col0); canvas.draw(h,"S");
			}	
		
		canvas.cd(lr-1); h = H2_CCa_Hist.get(is+1,lr,0).projectionY(); h.setFillColor(col1); canvas.draw(h,"same");
		H1D copy = h.histClone("Copy"); copy.reset() ; 
		copy.setBinContent(ip, h.getBinContent(ip)); copy.setFillColor(col2); canvas.draw(copy,"same");
		
		for(int il=1;il<3;il++) {
			String alab = otab[il-1]+(ip+1)+lab4[0]; String tlab = otab[il-1]+(ip+1)+lab4[1];
			if(lr!=il) {canvas.cd(il+1); h = H2_CCa_Hist.get(is+1,il,0).sliceY(11); h.setXTitle(alab); h.setTitle(""); h.setFillColor(col0); canvas.draw(h);}
			//if(lr!=il) {canvas.cd(il+3); h = H2_CCt_Hist.get(is+1,il,0).sliceY(22); h.setXTitle(tlab); h.setTitle(""); h.setFillColor(col0); canvas.draw(h);}
		}
		String alab = otab[lr-1]+(ip+1)+lab4[0]; String tlab = otab[lr-1]+(ip+1)+lab4[1];
		canvas.cd(lr+1); h = H2_CCa_Hist.get(is+1,lr,0).sliceY(ip);h.setXTitle(alab); h.setTitle(""); h.setFillColor(col2); canvas.draw(h);
		//canvas.cd(lr+3); h = H2_CCt_Hist.get(is+1,lr,0).sliceY(ip+1);h.setXTitle(tlab); h.setTitle(""); h.setFillColor(col2); canvas.draw(h);	 	
	}
}
