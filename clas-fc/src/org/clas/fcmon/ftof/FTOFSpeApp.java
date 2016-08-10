package org.clas.fcmon.ftof;

import org.clas.fcmon.tools.FCApplication;
import org.jlab.detector.base.DetectorDescriptor;
import org.root.basic.EmbeddedCanvas;
import org.root.histogram.H1D;

public class FTOFSpeApp extends FCApplication {

    public FTOFSpeApp(String name, FTOFPixels[] ftofPix) {
        super(name,ftofPix);    
     }

    public void updateCanvas(DetectorDescriptor dd) {
        
        EmbeddedCanvas canvas = this.getCanvas(this.getName()); 
        this.getDetIndices(dd);   
        int lr = layer;
        
        int il=1,col0=0,col1=4,col2=2;
        int nstr = ftofPix[0].nstr;
        
        H1D h;
        String alab;
        String otab[]={" Left PMT "," Right PMT "};
        String lab4[]={" ADC"," TDC"};      
        
        canvas.divide(4,6);
        canvas.setAxisFontSize(12);
        canvas.setAxisTitleFontSize(12);
        canvas.setTitleFontSize(14);
        canvas.setStatBoxFontSize(10);
        
        il = 1; 
        
        for(int iip=0;iip<nstr;iip++) {
            alab = otab[lr-1]+(iip+1)+lab4[0];
            canvas.cd(iip); h = ftofPix[0].strips.hmap2.get("H2_a_Hist").get(is,lr,0).sliceY(iip); 
            h.setXTitle(alab); h.setTitle(""); h.setFillColor(col1); canvas.draw(h,"S");
        }
/*
        il = 2;
        
        for(int iip=0;iip<nstr;iip++) {
            alab = otab[il-1]+(iip+1)+lab4[0];
            canvas.cd(nstr+iip); h = ccPix.strips.hmap2.get("H2_CCa_Hist").get(is+1,il,0).sliceY(iip); 
            h.setXTitle(alab); h.setTitle(""); h.setFillColor(col1); canvas.draw(h,"S");
        }       
        canvas.cd((lr-1)*nstr+ic); h = ccPix.strips.hmap2.get("H2_CCa_Hist").get(is+1,lr,0).sliceY(ic); 
        h.setTitle(""); h.setFillColor(col2); canvas.draw(h,"same");  
        */
        
        canvas.cd(ic); h = ftofPix[0].strips.hmap2.get("H2_a_Hist").get(is,lr,0).sliceY(ic); 
        h.setTitle(""); h.setFillColor(col2); canvas.draw(h,"same");    

    }
}
