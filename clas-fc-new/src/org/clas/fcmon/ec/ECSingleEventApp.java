package org.clas.fcmon.ec;

import org.clas.fcmon.tools.FCApplication; 
import org.jlab.detector.base.DetectorDescriptor;
//import org.root.basic.EmbeddedCanvas;
//import org.root.histogram.H1D;
//groot
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.groot.math.F1D;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F; 

public class ECSingleEventApp extends FCApplication {

   EmbeddedCanvas c = this.getCanvas(this.getName()); 

   public ECSingleEventApp(String name, ECPixels[] ecPix) {
      super(name,ecPix);		
   }
	
   public void updateCanvas(DetectorDescriptor dd) {
	       
      H1F h;
      String otab[][]={{" U PMT ","V PMT ","W PMT "},
              {" U Inner PMT","V Inner PMT","W Inner PMT"},
              {" U Outer PMT","V Outer PMT","W Outer PMT"}};

      int ilm = ilmap;
        
      double   zmax =  (double) mon.getGlob().get("PCMon_zmax");
      
      this.getDetIndices(dd);
      layer = lay;
		
      c.divide(3,3);
      c.setAxisFontSize(14);
      c.setStatBoxFontSize(12);
      
      int ii=0;
      
	  for(ilm=0; ilm<3; ilm++) {
      for(int il=1;il<4;il++) {
         c.cd(ii); 
         c.getPad(ii).getAxisX().setRange(0.,ecPix[ilm].ec_nstr[il-1]+1);
         c.getPad(ii).getAxisY().setRange(0.,1.2*zmax*app.displayControl.pixMax);
         h = ecPix[ilm].strips.hmap1.get("H1_Stra_Sevd").get(is,il,0); h.setTitleX(otab[ilm][il-1]); h.setFillColor(4);
         c.draw(h);
         ii++;
      }
	  }
      
      c.repaint();
   }
   
}
