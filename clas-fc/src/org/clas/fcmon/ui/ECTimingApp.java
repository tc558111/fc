package org.clas.fcmon.ui;

import org.clas.fcmon.tools.CalibrationData;
import org.clas.fcmon.tools.ECPixels;
import org.clas.fcmon.tools.FCApplication;
import org.jlab.clas.detector.DetectorCollection;
import org.jlab.clas.detector.DetectorDescriptor;
import org.root.basic.EmbeddedCanvas;
import org.root.func.F1D;
import org.root.histogram.H1D;
import org.root.histogram.H2D;

public class ECTimingApp extends FCApplication {
	
   public ECTimingApp(ECPixels[] ecPix, DetectorCollection<CalibrationData> collection) {
      super(ecPix,collection);	
   }

   public void updateCanvas(DetectorDescriptor dd, EmbeddedCanvas canvas) {
		
      H1D h;
      String otab[][]={{"U Strips","V Strips","W Strips"},{"U Strips","V Strips","W Strips"},{"U Strips","V Strips","W Strips"}};
			 	
      this.getDetIndices(dd);
		
      int la = dd.getLayer();
      int ip = dd.getComponent();		
      int ic = io;
		
      canvas.divide(3,2);

      for(int il=1;il<4;il++){
         H2D hpix = hmap2.get("H2_PCt_Hist").get(is+1,il+(io-1)*3,4);
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
}
