package org.clas.fcmon.ec;

import org.clas.fcmon.tools.ECPixels;
import org.clas.fcmon.tools.FCApplication;
import org.jlab.detector.base.DetectorDescriptor;
import org.root.basic.EmbeddedCanvas;
import org.root.func.F1D;
import org.root.histogram.H1D;
 

public class ECMode1App extends FCApplication  {
	
   public ECMode1App(String name, ECPixels[] ecPix) {
      super(name,ecPix);	
   }
   
   public void updateCanvas(DetectorDescriptor dd) {
		
      EmbeddedCanvas canvas = this.getCanvas(this.getName()); 
      int inProcess =     (int) mon.getGlob().get("inProcess"); 
      
      if (inProcess==0) return;
        
      Boolean  inMC = (Boolean) mon.getGlob().get("inMC");
      int     detID =     (int) mon.getGlob().get("detID");
      double   zmax =  (double) mon.getGlob().get("PCMon_zmax");
      
      if (inMC) return;
		
      this.getDetIndices(dd);
	     
      if (layer>3) return;
      layer = lay ; 
		
      if (detID==0) {
         if (layer==1) canvas.divide(9,8);
         if (layer>1)  canvas.divide(9,7);
       }
      
      if (detID>0) canvas.divide(6,6);
		
      canvas.setAxisFontSize(14);
      canvas.setTitleFontSize(14);
		
      int tet = app.mode7Emulation.tet;
      
      if (app.mode7Emulation.User_tet>0)  tet=app.mode7Emulation.User_tet;
      if (app.mode7Emulation.User_tet==0) tet=app.mode7Emulation.CCDB_tet;
      
      F1D f1 = new F1D("p0",0.,100.); f1.setParameter(0,tet);
      f1.setLineColor(2);
      F1D f2 = new F1D("p0",0.,100.); f2.setParameter(0,app.mode7Emulation.CCDB_tet);
      f2.setLineColor(4); f2.setLineStyle(2);	
		
      String otab[]={"U Inner Strip","V Inner Strip","W Inner Strip","U Outer Strip","V Outer Strip","W Outer Strip"};
      
      for(int ip=0;ip<ecPix[io-1].pc_nstr[layer-of-1];ip++){
         canvas.cd(ip); canvas.getPad().setAxisRange(0.,100.,-15.,zmax*app.displayControl.pixMax); //Vertical scale set by ZMAX slider
         H1D h1 = ecPix[0].strips.hmap2.get("H2_Mode1_Sevd").get(is+1,layer,0).sliceY(ip); 
         H1D h2 = ecPix[0].strips.hmap2.get("H2_Mode1_Sevd").get(is+1,layer,1).sliceY(ip); 
         h1.setXTitle("Sample (4 ns)"); h1.setYTitle("Counts"); h1.setTitle(""); h1.setTitle(otab[layer-1]+" "+(ip+1));
         h1.setFillColor(4); canvas.draw(h1); 
         h2.setFillColor(2); canvas.draw(h2,"same"); 
         canvas.draw(f1,"same"); 
         canvas.draw(f2,"same");
      }
      
   }
}
