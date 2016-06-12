package org.clas.fcmon.ui;

import java.util.TreeMap;

import org.clas.fcmon.tools.CalibrationData;
import org.clas.fcmon.tools.DetectorMonitor;
import org.clas.fcmon.tools.ECPixels;
import org.clas.fcmon.tools.MonitorApp;
import org.jlab.clas.detector.DetectorCollection;
import org.jlab.clas.detector.DetectorDescriptor;
import org.root.basic.EmbeddedCanvas;
import org.root.histogram.H1D;
import org.root.histogram.H2D;

public class ECOccupancyApp {
	
	ECPixels[]                                   ecPix = new ECPixels[2];
	DetectorCollection<CalibrationData>     collection = new DetectorCollection<CalibrationData>();  
	DetectorCollection<TreeMap<Integer,Object>> Lmap_a = new  DetectorCollection<TreeMap<Integer,Object>>();
	TreeMap<String, DetectorCollection<H1D>>     hmap1 = new TreeMap<String, DetectorCollection<H1D>>();
	TreeMap<String, DetectorCollection<H2D>>     hmap2 = new TreeMap<String, DetectorCollection<H2D>>();
	
	MonitorApp      app = null;
	DetectorMonitor mon = null;
	
	public ECOccupancyApp(ECPixels[] ecPix, DetectorCollection<CalibrationData> collection) {
		this.ecPix = ecPix;
		this.collection = collection;		
	}
	
	public void addH1DMaps(String name, DetectorCollection map) {
		this.hmap1.put(name,map);
	}
	public void addH2DMaps(String name, DetectorCollection map) {
		this.hmap2.put(name,map);
	}
	public void addLMaps(String name, DetectorCollection map) {
		this.Lmap_a=map;
	}
	public void setMonitoringClass(MonitorApp app) {
		this.app = app;
	}
	public void setApplicationClass(DetectorMonitor mon) {
		this.mon = mon;
	}
		
	public void canvas(DetectorDescriptor dd, EmbeddedCanvas canvas) {
		
		int is    = dd.getSector();
		int layer = dd.getLayer();
		int ic    = dd.getComponent();
		
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
		
		DetectorCollection<H2D> dc2a = hmap2.get("H2_PCa_Hist");
		DetectorCollection<H2D> dc2t = hmap2.get("H2_PCt_Hist");
    
		for(int il=l1;il<l2;il++){
			String otab = lab1[il-1-of]+lab2[io-1]+"Strips";
			canvas.cd(il-1-of); h = dc2a.get(is+1,il,0).projectionY(); h.setXTitle(otab); h.setFillColor(col0); canvas.draw(h);
			}
		
		l=layer;
		
		if (layer<7) {
			canvas.cd(l-1-of); h = dc2a.get(is+1,l,0).projectionY(); h.setFillColor(col1); canvas.draw(h,"same");
			H1D copy = h.histClone("Copy"); copy.reset() ; 
			copy.setBinContent(ic, h.getBinContent(ic)); copy.setFillColor(col2); canvas.draw(copy,"same");
			for(int il=l1;il<l2;il++) {
				String alab = lab1[il-1-of]+lab2[io-1]+lab3[0]+strip+lab4[0];String tlab = lab1[il-1-of]+lab2[io-1]+lab3[0]+strip+lab4[1];
				if(layer!=il) {canvas.cd(il+2-of); h = dc2a.get(is+1,il,0).sliceY(22); h.setXTitle(alab); h.setTitle(""); h.setFillColor(col0); canvas.draw(h);}
				if(layer!=il) {canvas.cd(il+5-of); h = dc2t.get(is+1,il,0).sliceY(22); h.setXTitle(tlab); h.setTitle(""); h.setFillColor(col0); canvas.draw(h);}
				}
			String alab = lab1[l-1-of]+lab2[io-1]+lab3[0]+strip+lab4[0];String tlab = lab1[l-1-of]+lab2[io-1]+lab3[0]+strip+lab4[1];
			canvas.cd(l+2-of); h = dc2a.get(is+1,l,0).sliceY(ic);h.setXTitle(alab); h.setTitle(""); h.setFillColor(col2); canvas.draw(h,"S");
			canvas.cd(l+5-of); h = dc2t.get(is+1,l,0).sliceY(ic);h.setXTitle(tlab); h.setTitle(""); h.setFillColor(col2); canvas.draw(h,"S");
			}
		
		if (layer==7||layer==8) {
			for(int il=l1;il<l2;il++) {
				canvas.cd(il-1-of); h = dc2a.get(is+1,il,0).projectionY();
				H1D copy = h.histClone("Copy");
				
				strip = ecPix[0].pixels.getStrip(il-of,ic+1);
				copy.reset() ; copy.setBinContent(ic, h.getBinContent(ic));
				copy.setFillColor(col2); canvas.draw(copy,"same");	    		 
				String alab = lab1[il-1-of]+lab2[io-1]+lab3[0]+strip+lab4[0];String tlab = lab1[il-1-of]+lab2[io-1]+lab3[0]+strip+lab4[1];
				canvas.cd(il+2-of) ; h = dc2a.get(is+1,il,0).sliceY(strip-1); h.setXTitle(alab); h.setTitle("");h.setFillColor(col2); canvas.draw(h,"S");
				canvas.cd(il+5-of) ; h = dc2t.get(is+1,il,0).sliceY(strip-1); h.setXTitle(tlab); h.setTitle("");h.setFillColor(col2); canvas.draw(h,"S");
				}
			}
		
		if (layer>8) {
			for(int il=l1;il<l2;il++) {
				canvas.cd(il-1-of); h = dc2a.get(is+1,il,1).projectionY();
				H1D copy = h.histClone("Copy");
				strip = ecPix[0].pixels.getStrip(il-of,ic+1);
				copy.reset() ; copy.setBinContent(strip-1, h.getBinContent(strip-1));
				copy.setFillColor(col2); canvas.draw(copy,"same");	
				String alab1 = lab1[il-1-of]+lab2[io-1]+lab3[0]+strip+lab4[0];String tlab1 = lab1[il-1-of]+lab2[io-1]+lab3[0]+strip+lab4[1];
				String alab2 = lab1[il-1-of]+lab2[io-1]+lab3[1]+pixel+lab4[0];String tlab2 = lab1[il-1-of]+lab2[io-1]+lab3[1]+pixel+lab4[1];
				if (layer<17) {
					canvas.cd(il+2-of) ; h = dc2a.get(is+1,il,1).sliceY(strip-1); h.setXTitle(alab1); h.setTitle("");h.setFillColor(col2); canvas.draw(h,"S");
					canvas.cd(il+5-of) ; h = dc2a.get(is+1,il,2).sliceY(ic)     ; h.setXTitle(alab2); h.setTitle("");h.setFillColor(col2); canvas.draw(h,"S");
					}
				if (layer>16&&layer<22) {
					canvas.cd(il+2-of) ; h = dc2t.get(is+1,il,1).sliceY(strip-1); h.setXTitle(tlab1); h.setTitle("");h.setFillColor(col2); canvas.draw(h);
					canvas.cd(il+5-of) ; h = dc2t.get(is+1,il,2).sliceY(ic)     ; h.setXTitle(tlab2); h.setTitle("");h.setFillColor(col2); canvas.draw(h);
					}
				}  	 
			}
		
	}
}
