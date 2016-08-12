package org.clas.fcmon.ftof;

import org.clas.fcmon.tools.FCApplication;
import org.jlab.detector.base.DetectorDescriptor;
import org.root.basic.EmbeddedCanvas;
import org.root.func.F1D;
import org.root.histogram.H1D;
import org.root.histogram.H2D;

public class FTOFPedestalApp extends FCApplication {

    public FTOFPedestalApp(String name, FTOFPixels[] ftofPix) {
        super(name,ftofPix);    
     }
    
    public void updateCanvas(DetectorDescriptor dd) {
        
        EmbeddedCanvas canvas = this.getCanvas(this.getName()); 
        this.getDetIndices(dd);   
        int  lr = layer;
        int ilm = ilmap;
        
        int col2=2,col4=4,col0=0;
        double nstr = ftofPix[ilm].nstr;
        
        H1D h;
        String otab[]={" Left PMT "," Right PMT "};
        
        canvas.divide(2,2);
        canvas.setAxisFontSize(14);
        canvas.setTitleFontSize(14);
        canvas.setAxisTitleFontSize(14);
        
        for(int il=1;il<3;il++){
            H2D hpix = ftofPix[ilm].strips.hmap2.get("H2_a_Hist").get(is,il,3);
            hpix.setXTitle("PED (Ref-Measured)") ; hpix.setYTitle(otab[il-1]);
         
            canvas.cd(il-1); canvas.getPad().setAxisRange(-30.,30.,1.,nstr+1) ; canvas.setLogZ(); canvas.draw(hpix);
            
            if(lr==il) {
                F1D f1 = new F1D("p0",-30.,30.); f1.setParameter(0,ic+1);
                F1D f2 = new F1D("p0",-30.,30.); f2.setParameter(0,ic+2);
                f1.setLineColor(2); canvas.draw(f1,"same"); 
                f2.setLineColor(2); canvas.draw(f2,"same");
            }
            
            canvas.cd(il-1+2);
                        h=hpix.sliceY(2);  h.setFillColor(4); h.setTitle(""); h.setXTitle("Sector "+is+otab[il-1]+2)     ; canvas.draw(h,"S");
            if(lr==il) {h=hpix.sliceY(ic); h.setFillColor(2); h.setTitle(""); h.setXTitle("Sector "+is+otab[il-1]+(ic+1)); canvas.draw(h,"S");}
        }           
    }
    
}
