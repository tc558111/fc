package org.clas.fcmon.ec;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.clas.fcmon.tools.FCApplication; 
import org.jlab.detector.base.DetectorDescriptor;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.groot.math.F1D;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F; 

public class ECSingleEventApp extends FCApplication {

    JPanel                     engineView = new JPanel();
    EmbeddedCanvas                      l = new EmbeddedCanvas();
    EmbeddedCanvas                      r = new EmbeddedCanvas();  

   public ECSingleEventApp(String name, ECPixels[] ecPix) {
      super(name,ecPix);		
   }
   
   public JPanel getCalibPane() {        
       engineView.setLayout(new BorderLayout());
       JSplitPane   viewPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT); 
       viewPane.setLeftComponent(l);
       viewPane.setRightComponent(r);
       viewPane.setResizeWeight(0.5);
       engineView.add(viewPane);
       return engineView;       
   }  
   
   public void updateCanvas(DetectorDescriptor dd) {
	       
      H1F h,h1,h2;
      
      String otab[][]={{"U ","V ","W "},
              {" U Inner ","V Inner ","W Inner "},
              {" U Outer ","V Outer ","W Outer "}};

      String dtab[]={"PCAL ","EC Inner ","EC Outer "};
      
      int ilm = ilmap;
        
      double   zmax =  (double) mon.getGlob().get("PCMon_zmax");
      
      this.getDetIndices(dd);
		
      l.divide(3,6); r.divide(1,3);
      l.setAxisFontSize(14);
      l.setStatBoxFontSize(12);
      
      int ii=0;
      int jj=0;
      
	  for(ilm=0; ilm<3; ilm++) {
      for(int il=1;il<4;il++) {
         l.cd(ii); 
         l.getPad(ii).getAxisX().setRange(0.,ecPix[ilm].ec_nstr[il-1]+1);
         l.getPad(ii).getAxisY().setRange(0.,1.2*zmax*app.displayControl.pixMax);
         h = ecPix[ilm].strips.hmap1.get("H1_Stra_Sevd").get(is,il,0); 
         h.setTitleX(otab[ilm][il-1]+"PMT"); 
         h.setTitleY(" ");
         if (il==1) h.setTitleY("Strip Energy (MeV)"); 
         h.setFillColor(4);
         l.draw(h);
         ii++;
      }
      for(int il=1;il<4; il++) {
          l.cd(ii);
          l.getPad(ii).getAxisX().setRange(0.,40.);
          h  = ecPix[ilm].strips.hmap2.get("H2_a_Hist").get(is,4,0).sliceY(il-1) ; h.setTitleX(otab[ilm][il-1]+"Peak Energy (MeV)"); h.setFillColor(0);
          h1 = ecPix[ilm].strips.hmap2.get("H2_a_Hist").get(is,5,0).sliceY(il-1) ; h1.setFillColor(34);
          h2 = ecPix[ilm].strips.hmap2.get("H2_a_Hist").get(is,6,0).sliceY(il-1) ; h2.setFillColor(32);
          l.draw(h); 
          l.draw(h1,"same"); l.draw(h2,"same");
          ii++;
      }
          r.cd(jj);
          h = ecPix[ilm].strips.hmap2.get("H2_a_Hist").get(is,4,0).sliceY((int)3) ; h.setTitleX(dtab[ilm]+"Cluster Energy (MeV)"); h.setFillColor(2);
          r.draw(h);
          jj++;
   
	  }
      
      l.repaint(); r.repaint();
   }
   
}
