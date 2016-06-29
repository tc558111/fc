package org.clas.fcmon.ui;

 
import org.clas.fcmon.tools.ECPixels;
import org.clas.fcmon.tools.FCApplication;
import org.jlab.clas.detector.DetectorDescriptor;
import org.root.basic.EmbeddedCanvas;
import org.root.func.F1D;
import org.root.histogram.H1D;
import org.root.histogram.H2D;

public class ECPedestalApp extends FCApplication {
	
   public ECPedestalApp(String name, ECPixels[] ecPix) {
      super(name, ecPix);		
   }

   public void updateCanvas(DetectorDescriptor dd) {

       EmbeddedCanvas canvas = this.getCanvas(this.getName()); 
		
      H1D h;
      Boolean inMC = (Boolean) mon.getGlob().get("inMC");
		
      if (inMC) return;
		
      String otab[][]={{"U Strips","V Strips","W Strips"},{"U Inner Strips","V Inner Strips","W Inner Strips"},{"U Outer Strips","V Outer Strips","W Outer Strips"}};
		
      this.getDetIndices(dd);	

      int la = dd.getLayer();
      int ip = dd.getComponent();		
      int ic = io;
		
      canvas.divide(3,2);
		
      for(int il=1;il<4;il++){
         H2D hpix = ecPix[0].strips.hmap2.get("H2_Peds_Hist").get(is+1,il+(io-1)*3,0);
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
}
