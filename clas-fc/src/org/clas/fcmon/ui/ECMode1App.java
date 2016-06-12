package org.clas.fcmon.ui;

import java.util.TreeMap;

import org.clas.fcmon.tools.CalibrationData;
import org.clas.fcmon.tools.DetectorMonitor;
import org.clas.fcmon.tools.ECPixels;
import org.clas.fcmon.tools.FCApplication;
import org.clas.fcmon.tools.MonitorApp;
import org.jlab.clas.detector.DetectorCollection;
import org.jlab.clas.detector.DetectorDescriptor;
import org.jlab.clas.detector.DetectorType;
import org.jlab.rec.ecn.ECCommon;
import org.root.basic.EmbeddedCanvas;
import org.root.func.F1D;
import org.root.histogram.GraphErrors;
import org.root.histogram.H1D;
import org.root.histogram.H2D;

public class ECMode1App extends FCApplication  {
	
	public ECMode1App(ECPixels[] ecPix, DetectorCollection<CalibrationData> collection) {
		super(ecPix,collection);		
	}

	public void updateCanvas(DetectorDescriptor dd, EmbeddedCanvas canvas) {
		
        int inProcess =     (int) mon.getGlob().get("inProcess");
        if (inProcess==0) return;
        
		Boolean  inMC = (Boolean) mon.getGlob().get("inMC");
        int     detID =     (int) mon.getGlob().get("detID");
        int       tet =     (int) mon.getGlob().get("tet");
        double   zmax =  (double) mon.getGlob().get("PCMon_zmax");
        
		if (inMC) return;
		
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
		

		if (detID==0) {
			if (layer==1) canvas.divide(9,8);
			if (layer>1)  canvas.divide(9,7);
		}
		if (detID>0) canvas.divide(6,6);
		
		canvas.setAxisFontSize(14);
		canvas.setTitleFontSize(14);
		
		H1D h = new H1D() ; 
		String otab[]={"U Inner Strip","V Inner Strip","W Inner Strip","U Outer Strip","V Outer Strip","W Outer Strip"};
		
		if (app.mode7Emulation.User_tet>0)  tet=app.mode7Emulation.User_tet;
		if (app.mode7Emulation.User_tet==0) tet=app.mode7Emulation.CCDB_tet;
		
		F1D f1 = new F1D("p0",0.,100.); f1.setParameter(0,tet);
		f1.setLineColor(2);
		F1D f2 = new F1D("p0",0.,100.); f2.setParameter(0,app.mode7Emulation.CCDB_tet);
		f2.setLineColor(4);f2.setLineStyle(2);	
		
	    for(int ip=0;ip<ecPix[io-1].pc_nstr[layer-of-1];ip++){
	    	canvas.cd(ip); canvas.getPad().setAxisRange(0.,100.,-15.,zmax*app.displayControl.pixMax);
	        h = hmap2.get("H2_PCa_Sevd").get(is+1,layer,0).sliceY(ip); h.setXTitle("Sample (4 ns)"); h.setYTitle("Counts");
	    	h.setTitle(otab[layer-1]+" "+(ip+1)); h.setFillColor(4); canvas.draw(h);
	        h = hmap2.get("H2_PCa_Sevd").get(is+1,layer,1).sliceY(ip); h.setFillColor(2); canvas.draw(h,"same");
	        canvas.draw(f1,"same");canvas.draw(f2,"same");
	    }		
	}
	
}
