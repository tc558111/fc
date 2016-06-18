package org.clas.fcmon.ui;

import org.clas.fcmon.tools.ECPixels;
import org.clas.fcmon.tools.FCApplication;
import org.jlab.clas.detector.DetectorDescriptor;
import org.root.basic.EmbeddedCanvas;
import org.root.histogram.H2D;

public class ECRawHistosApp extends FCApplication {
	
   public ECRawHistosApp(String name, ECPixels[] ecPix) {
      super(name,ecPix);	
   }
	
   public void updateCanvas(DetectorDescriptor dd) {

      EmbeddedCanvas canvas = this.getCanvas(this.getName()); 
       
      H2D h2;
      int detID = (int) mon.getGlob().get("detID");
		
      String ytab[]={"U Inner Strip","V Inner Strip","W Inner Strip","U Outer Strip","V Outer Strip","W Outer Strip"};
      String xtaba[]={"U Inner ADC","V Inner ADC","W Inner ADC","U Outer ADC","V Outer ADC","W Outer ADC"};
      String xtabt[]={"U Inner TDC","V Inner TDC","W Inner TDC","U Outer TDC","V Outer TDC","W Outer TDC"};
      String iolab[]={" ","Inner ","Outer "};
		
      this.getDetIndices(dd);
		
      if (layer>3) return;
	    
         layer = lay;
			
         canvas.divide(3,3);		
         canvas.setAxisFontSize(14);
         canvas.setAxisTitleFontSize(14);
         canvas.setTitleFontSize(14);
         canvas.setStatBoxFontSize(12);
		
         for (int il=0; il<3 ; il++) {
            h2 = hmap2.get("H2_PC_Stat").get(is+1,detID+(io-1),il) ; h2.setYTitle(iolab[detID+(io-1)]+"View"); h2.setXTitle("Strip") ; 
            canvas.cd(il); canvas.setLogZ(); canvas.draw(h2);
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
