package org.clas.fcmon.ui;


import org.clas.fcmon.tools.ECPixels;
import org.clas.fcmon.tools.FCApplication; 
import org.jlab.detector.base.DetectorDescriptor;
import org.root.basic.EmbeddedCanvas;
import org.root.histogram.H1D;
 

public class ECSingleEventApp extends FCApplication {

   public ECSingleEventApp(String name, ECPixels[] ecPix) {
      super(name,ecPix);		
   }
	
   public void updateCanvas(DetectorDescriptor dd) {
	    
      EmbeddedCanvas canvas = this.getCanvas(this.getName()); 
      
      H1D h;
      int col0=4;
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
         h = ecPix[0].strips.hmap1.get("H1_Stra_Sevd").get(is+1,il+of,0); h.setXTitle(otab[il-1+of]); h.setFillColor(col0); canvas.draw(h);
      }
   }
   
}
