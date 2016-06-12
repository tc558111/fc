package org.clas.fcmon.ui;

import java.util.TreeMap;

import org.clas.fcmon.tools.CalibrationData;
import org.clas.fcmon.tools.DetectorMonitor;
import org.clas.fcmon.tools.ECPixels;
import org.clas.fcmon.tools.FCApplication;
import org.clas.fcmon.tools.MonitorApp;
import org.jlab.clas.detector.DetectorCollection;
import org.jlab.clas.detector.DetectorDescriptor;
import org.root.basic.EmbeddedCanvas;
import org.root.histogram.H1D;
import org.root.histogram.H2D;

public class ECRawHistosApp extends FCApplication {
	
	public ECRawHistosApp(ECPixels[] ecPix, DetectorCollection<CalibrationData> collection) {
		super(ecPix,collection);	
	}
	
	public void updateCanvas(DetectorDescriptor dd, EmbeddedCanvas canvas) {
	
		int detID = (int) mon.getGlob().get("detID");
		
		String ytab[]={"U Inner Strip","V Inner Strip","W Inner Strip","U Outer Strip","V Outer Strip","W Outer Strip"};
		String xtaba[]={"U Inner ADC","V Inner ADC","W Inner ADC","U Outer ADC","V Outer ADC","W Outer ADC"};
		String xtabt[]={"U Inner TDC","V Inner TDC","W Inner TDC","U Outer TDC","V Outer TDC","W Outer TDC"};
		String iolab[]={" "+"Inner ","Outer "};
		
		int is    = dd.getSector();
		int layer = dd.getLayer();
		int ic    = dd.getComponent();
	
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
			h2 = hmap2.get("H2_PC_Stat").get(is+1,detID+(io-1),il) ; h2.setYTitle(iolab[detID+(io-1)]+"View"); h2.setXTitle("Strip") ; canvas.cd(il-of); canvas.setLogZ(); canvas.draw(h2);
		}
		
		for (int il=l1; il<l2; il++) {
			h2 = hmap2.get("H2_PCa_Hist").get(is+1,il,0); h2.setYTitle(ytab[il-1]) ; h2.setXTitle(xtaba[il-1]);
			canvas.cd(il-of-1+3) ; canvas.setLogZ(); canvas.draw(h2); 
		}
		
		for (int il=l1; il<l2; il++) {
			h2 = hmap2.get("H2_PCt_Hist").get(is+1,il,0); h2.setYTitle(ytab[il-1]) ; h2.setXTitle(xtabt[il-1]);
			canvas.cd(il-of-1+6) ; canvas.setLogZ(); canvas.draw(h2); 
		}
				
	}
	
	
}
