package org.clas.fcmon.ftof;

import org.clas.fcmon.tools.FCApplication;
import org.jlab.detector.base.DetectorDescriptor;
import org.root.basic.EmbeddedCanvas;
import org.root.func.F1D;
import org.root.histogram.H1D;
import org.root.histogram.H2D;

public class FTOFOccupancyApp extends FCApplication {
    
    public FTOFOccupancyApp(String name, FTOFPixels[] ftofPix) {
        super(name,ftofPix);    
     }
    
    public void updateCanvas(DetectorDescriptor dd) {
        
        EmbeddedCanvas canvas = this.getCanvas(this.getName()); 
        this.getDetIndices(dd); 
        int lr = layer;
    
        int col0=0,col1=4,col2=2;
        
        H1D h1;  
        String otab[]={" Left "," Right "};
        String lab4[]={" ADC"," TDC"};      
        String xlab,ylab;
        
        canvas.divide(2,3);
        canvas.setAxisFontSize(14);
        canvas.setAxisTitleFontSize(14);
        canvas.setTitleFontSize(14);
        canvas.setStatBoxFontSize(12);
        
        double nstr = ftofPix[0].nstr;
        
        H2D h2a = ftofPix[0].strips.hmap2.get("H2_a_Hist").get(is,1,0); h2a.setYTitle(otab[0]+"PMTs") ; h2a.setXTitle(otab[0]+"PMT"+lab4[0]);
        H2D h2b = ftofPix[0].strips.hmap2.get("H2_a_Hist").get(is,2,0); h2b.setYTitle(otab[1]+"PMTs") ; h2b.setXTitle(otab[1]+"PMT"+lab4[0]);
        canvas.cd(0); canvas.getPad().setAxisRange(0.,2000.,1.,nstr+1) ; canvas.setLogZ(); canvas.draw(h2a); 
        canvas.cd(1); canvas.getPad().setAxisRange(0.,2000.,1.,nstr+1) ; canvas.setLogZ(); canvas.draw(h2b); 
        
        canvas.cd(lr-1);
        
        F1D f1 = new F1D("p0",0.,2000.); f1.setParameter(0,ic+1);
        F1D f2 = new F1D("p0",0.,2000.); f2.setParameter(0,ic+2);
        f1.setLineColor(2); canvas.draw(f1,"same"); 
        f2.setLineColor(2); canvas.draw(f2,"same");
        
        for(int il=1;il<3;il++){
            xlab = "Sector "+is+otab[il-1]+"PMTs";
            canvas.cd(il+1); h1 = ftofPix[0].strips.hmap2.get("H2_a_Hist").get(is,il,0).projectionY(); h1.setXTitle(xlab); h1.setFillColor(col0); canvas.draw(h1);
            }   
        
        canvas.cd(lr+1); h1 = ftofPix[0].strips.hmap2.get("H2_a_Hist").get(is,lr,0).projectionY(); h1.setFillColor(col1); canvas.draw(h1,"same");
        H1D copy = h1.histClone("Copy"); copy.reset() ; 
        copy.setBinContent(ic, h1.getBinContent(ic)); copy.setFillColor(col2); canvas.draw(copy,"same");
        
        for(int il=1;il<3;il++) {
            String alab = otab[il-1]+"PMT "+11+lab4[0]; String tlab = otab[il-1]+(ic+1)+lab4[1];
            if(lr!=il) {canvas.cd(il+3); h1 = ftofPix[0].strips.hmap2.get("H2_a_Hist").get(is,il,0).sliceY(11); h1.setXTitle(alab); h1.setTitle(""); h1.setFillColor(col0); canvas.draw(h1,"S");}
            //if(lr!=il) {canvas.cd(il+3); h = H2_CCt_Hist.get(is+1,il,0).sliceY(22); h.setXTitle(tlab); h.setTitle(""); h.setFillColor(col0); canvas.draw(h);}
        }
        String alab = otab[lr-1]+"PMT "+(ic+1)+lab4[0]; String tlab = otab[lr-1]+(ic+1)+lab4[1];
        canvas.cd(lr+3); h1 = ftofPix[0].strips.hmap2.get("H2_a_Hist").get(is,lr,0).sliceY(ic);h1.setXTitle(alab); h1.setTitle(""); h1.setFillColor(col2); canvas.draw(h1,"S");
        //canvas.cd(lr+3); h = H2_CCt_Hist.get(is+1,lr,0).sliceY(ip+1);h.setXTitle(tlab); h.setTitle(""); h.setFillColor(col2); canvas.draw(h);     
    }


}
