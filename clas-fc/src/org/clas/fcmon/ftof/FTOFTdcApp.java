package org.clas.fcmon.ftof;

import org.clas.fcmon.tools.FCApplication;
import org.jlab.clas.detector.DetectorCollection;
import org.jlab.detector.base.DetectorDescriptor;
import org.root.basic.EmbeddedCanvas;
import org.root.func.F1D;
import org.root.histogram.H1D;
import org.root.histogram.H2D;

public class FTOFTdcApp extends FCApplication {

    int ics;
    
    public FTOFTdcApp(String name, FTOFPixels[] ftofPix) {
        super(name,ftofPix);    
     }
    
    public void updateCanvas(DetectorDescriptor dd) {
        
        EmbeddedCanvas canvas = this.getCanvas(this.getName()); 
        this.getDetIndices(dd); 
        int lr = layer;
    
        int col0=0,col1=4,col2=2;
        
        H1D h1a,h1b,h1c,copy1=null,copy2=null;  
         
        canvas.divide(3,3);
        canvas.setAxisFontSize(14);
        canvas.setAxisTitleFontSize(14);
        canvas.setTitleFontSize(14);
        canvas.setStatBoxFontSize(12);
        
        int ilm = ilmap;
        double nstr = ftofPix[ilm].nstr;
        
        H2D h2a = ftofPix[ilm].strips.hmap2.get("H2_t_Hist").get(is,1,0); h2a.setYTitle("Sector "+is+" Left PMT")  ; h2a.setXTitle("Left PMT TDC");
        H2D h2b = ftofPix[ilm].strips.hmap2.get("H2_t_Hist").get(is,2,0); h2b.setYTitle("Sector "+is+" Right PMT") ; h2b.setXTitle("Right PMT TDC");
        H2D h2c = ftofPix[ilm].strips.hmap2.get("H2_t_Hist").get(is,0,0); h2c.setYTitle("Sector "+is+" PADDLE")    ; h2c.setXTitle("TDIF");
        canvas.cd(0); canvas.getPad().setAxisRange(1300.,1410.,1.,nstr+1) ; canvas.setLogZ(); canvas.draw(h2a); 
        canvas.cd(1); canvas.getPad().setAxisRange(1300.,1410.,1.,nstr+1) ; canvas.setLogZ(); canvas.draw(h2b); 
        canvas.cd(2); canvas.getPad().setAxisRange(-35.,35.,1.,nstr+1)    ; canvas.setLogZ(); canvas.draw(h2c); 
        
        F1D f1 = new F1D("p0",0.,4000.); f1.setParameter(0,ic+1);
        F1D f2 = new F1D("p0",0.,4000.); f2.setParameter(0,ic+2);
        canvas.cd(lr-1);        
        f1.setLineColor(2); canvas.draw(f1,"same"); 
        f2.setLineColor(2); canvas.draw(f2,"same");
            f1 = new F1D("p0",-35.,35.); f1.setParameter(0,ic+1);
            f2 = new F1D("p0",-35.,35.); f2.setParameter(0,ic+2);
        canvas.cd(2);        
        f1.setLineColor(2); canvas.draw(f1,"same"); 
        f2.setLineColor(2); canvas.draw(f2,"same");
        
        h1a = ftofPix[ilm].strips.hmap2.get("H2_t_Hist").get(is,1,0).projectionY(); h1a.setXTitle("Sector "+is+" Left PMT");   h1a.setFillColor(col0);  
        h1b = ftofPix[ilm].strips.hmap2.get("H2_t_Hist").get(is,2,0).projectionY(); h1b.setXTitle("Sector "+is+" Right PMT" ); h1b.setFillColor(col0);  
        h1c = ftofPix[ilm].strips.hmap2.get("H2_t_Hist").get(is,0,0).projectionY(); h1c.setXTitle("Sector "+is+" PADDLE" );    h1c.setFillColor(col1);  
        if (lr==1) {h1a.setFillColor(col1); copy1=h1a.histClone("Copy"); copy1.reset(); copy1.setBinContent(ic, h1a.getBinContent(ic)); copy1.setFillColor(col2);}
        if (lr==2) {h1b.setFillColor(col1); copy1=h1b.histClone("Copy"); copy1.reset(); copy1.setBinContent(ic, h1b.getBinContent(ic)); copy1.setFillColor(col2);}
                                            copy2=h1c.histClone("Copy"); copy2.reset(); copy2.setBinContent(ic, h1c.getBinContent(ic)); copy2.setFillColor(col2);
        canvas.cd(3); ; canvas.draw(h1a); if (lr==1) canvas.draw(copy1,"same");
        canvas.cd(4); ; canvas.draw(h1b); if (lr==2) canvas.draw(copy1,"same");
        canvas.cd(5); ; canvas.draw(h1c);            canvas.draw(copy2,"same");
        
        h1a = ftofPix[ilm].strips.hmap2.get("H2_t_Hist").get(is,1,0).sliceY(ics); h1a.setXTitle("Left PMT "+(ic+1)+" TDC");   h1a.setFillColor(col0);  
        h1b = ftofPix[ilm].strips.hmap2.get("H2_t_Hist").get(is,2,0).sliceY(ics); h1b.setXTitle("Right PMT "+(ic+1)+" TDC" ); h1b.setFillColor(col0);  
        h1c = ftofPix[ilm].strips.hmap2.get("H2_t_Hist").get(is,0,0).sliceY(ics); h1c.setXTitle("TDIF PADDLE "+(ic+1));       h1c.setFillColor(col2);  
        
        if (lr==1) h1a.setFillColor(col2);
        if (lr==2) h1b.setFillColor(col2);
        canvas.cd(6); ; h1a.setTitle(""); canvas.draw(h1a);  
        canvas.cd(7); ; h1b.setTitle(""); canvas.draw(h1b); 
        canvas.cd(8); ; h1c.setTitle(""); canvas.draw(h1c);      
        
        ics=ic;
        
    }


}
