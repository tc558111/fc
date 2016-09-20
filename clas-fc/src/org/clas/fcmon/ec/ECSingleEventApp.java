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
    EmbeddedCanvas                     ru = new EmbeddedCanvas();  
    EmbeddedCanvas                     rd = new EmbeddedCanvas();  

   public ECSingleEventApp(String name, ECPixels[] ecPix) {
      super(name,ecPix);		
   }
   
   public JPanel getCalibPane() {        
       engineView.setLayout(new BorderLayout());
       JSplitPane   hPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT); 
       JSplitPane   vPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT); 
       vPane.setTopComponent(ru);
       vPane.setBottomComponent(rd);
       hPane.setLeftComponent(l);
       hPane.setRightComponent(vPane);
       hPane.setResizeWeight(0.5);
       vPane.setResizeWeight(0.5);
       engineView.add(hPane);
       return engineView;       
   }  
   
   public void updateCanvas(DetectorDescriptor dd) {
	       
      H1F h,h1,h2;
      H2F hh;
      
      String otab[][]={{"U ","V ","W "},
              {" U Inner ","V Inner ","W Inner "},
              {" U Outer ","V Outer ","W Outer "}};

      String dtab[]={"PCAL ","EC Inner ","EC Outer "};
      
      int ilm = ilmap;
        
      double   zmax = (double) mon.getGlob().get("PCMon_zmax");
      String config = (String) mon.getGlob().get("config");
      
      this.getDetIndices(dd);
		
      l.divide(3,6); ru.divide(3,2);rd.divide(3,2);
      l.setAxisFontSize(14);
      l.setStatBoxFontSize(12);
      
      int ii=0;
      int jj=0;
      int kk=0;
      
	  for(ilm=0; ilm<3; ilm++) {
      for(int il=1;il<4;il++) {
         F1D f1 = new F1D("p0","[a]",0.,ecPix[ilm].ec_nstr[il-1]+1); 
         f1.setParameter(0,0.1*ecPix[ilm].getStripThr(config,il));
         f1.setLineColor(4);
         l.cd(ii); 
         l.getPad(ii).getAxisX().setRange(0.,ecPix[ilm].ec_nstr[il-1]+1);
         l.getPad(ii).getAxisY().setRange(0.,1.2*zmax*app.displayControl.pixMax);
         h1 = ecPix[ilm].strips.hmap1.get("H1_Stra_Sevd").get(is,il,1); 
         h2 = ecPix[ilm].strips.hmap1.get("H1_Stra_Sevd").get(is,il,0); 
         h1.setTitleX(otab[ilm][il-1]+"PMT"); h1.setTitleY(" ");
         if (il==1) h1.setTitleY("Strip Energy (MeV)"); 
         h1.setFillColor(0); l.draw(h1);
         h2.setFillColor(4); l.draw(h2,"same"); 
         l.draw(f1,"same");
         ii++;
      }
	  }
	  for(ilm=0; ilm<3; ilm++) {
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
	  }
	  
	  for(ilm=0; ilm<3; ilm++) {
          ru.cd(jj); 
          h = ecPix[ilm].strips.hmap2.get("H2_a_Hist").get(is,4,0).sliceY((int)3) ; h.setTitleX(dtab[ilm]+"Cluster Energy (MeV)"); h.setFillColor(2);          
          h.setOptStat(Integer.parseInt("1100")); ru.draw(h);
          jj++;   
	  }
	  
      rd.cd(0); h = ecPix[0].strips.hmap2.get("H2_a_Hist").get(is,8,0).sliceY(0) ; h.setTitleX("PCAL Cluster X - GEMC X (cm)"); h.setFillColor(2); 
      h.setOptStat(Integer.parseInt("1100")); h.setTitle(" "); rd.draw(h);
      rd.cd(1); h = ecPix[0].strips.hmap2.get("H2_a_Hist").get(is,8,0).sliceY(1) ; h.setTitleX("PCAL Cluster Y - GEMC Y (cm)"); h.setFillColor(2);
      h.setOptStat(Integer.parseInt("1100")); h.setTitle(" "); rd.draw(h);
      rd.cd(2); h = ecPix[0].strips.hmap2.get("H2_a_Hist").get(is,8,0).sliceY(2) ; h.setTitleX("PCAL Cluster Z - GEMC Z (cm)"); h.setFillColor(2); 
      h.setOptStat(Integer.parseInt("1100")); h.setTitle(" "); rd.draw(h);
	  
      l.repaint(); ru.repaint(); rd.repaint();
   }
   
}
