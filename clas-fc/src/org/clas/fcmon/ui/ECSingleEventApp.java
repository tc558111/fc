package org.clas.fcmon.ui;

import org.clas.fcmon.tools.CalibrationData;
import org.clas.fcmon.tools.ECPixels;
import org.clas.fcmon.tools.FCApplication;
import org.jlab.clas.detector.DetectorCollection;
import org.jlab.clas.detector.DetectorDescriptor;
import org.root.basic.EmbeddedCanvas;
import org.root.histogram.H1D;
 

public class ECSingleEventApp extends FCApplication {

   public ECSingleEventApp(ECPixels[] ecPix, DetectorCollection<CalibrationData> collection) {
      super(ecPix,collection);		
   }
	
   public void updateCanvas(DetectorDescriptor dd, EmbeddedCanvas canvas) {
	    
      H1D h;
      int col0=0;
      String otab[]={"U Inner Strips","V Inner Strips","W Inner Strips","U Outer Strips","V Outer Strips","W Outer Strips"};
        
      double   zmax =  (double) mon.getGlob().get("PCMon_zmax");
        
      this.getDetIndices(dd);
      layer = lay;
		
      canvas.divide(1,3);
      canvas.setAxisFontSize(14);
      canvas.setTitleFontSize(14);
      canvas.setStatBoxFontSize(12);
		
      for(int il=1;il<4;il++){
         canvas.cd(il-1); canvas.getPad().setAxisRange(-1.,ecPix[0].pc_nstr[il-1]+1,0.,zmax*app.displayControl.pixMax);
         h = hmap1.get("H1_PCa_Sevd").get(is+1,il+of,0); h.setXTitle(otab[il-1+of]); h.setFillColor(col0); canvas.draw(h);
      }
   }
   
}
