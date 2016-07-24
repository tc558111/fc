package org.clas.fcmon.ec;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.clas.fcmon.detector.view.DetectorShape2D;
import org.clas.fcmon.detector.view.DetectorPane2D;
import org.clas.fcmon.tools.ECPixels;
import org.clas.fcmon.tools.FCApplication;
import org.jlab.detector.base.DetectorDescriptor;
import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Path3D;
import org.root.attr.ColorPalette;

public class ECDetector extends FCApplication{
    
    ColorPalette palette = new ColorPalette();
    
    String        mondet = null;
    int        inProcess = 0;
    double    PCMon_zmin = 0;
    double    PCMon_zmax = 0;
    
    public ECDetector(String name , ECPixels[] ecPix) {
        super(name, ecPix);       
     }  
    
    public void init(int is1, int is2) {
        mondet = (String) mon.getGlob().get("mondet");
        initDetector(is1,is2);
   }
    
    public void addButtons(String group, String store, String arg) {
        List<String> name = new ArrayList<String>();
        List<Integer> key = new ArrayList<Integer>(); 
        String[] items = arg.split("\\.");
        for (int i=0; i<items.length; i=i+2) {
            name.add(items[i]);
             key.add(Integer.parseInt(items[i+1]));
        }   
        if (store=="View") app.getDetectorView().addViewStore(group, name, key);
        if (store=="Map")  app.getDetectorView().addMapStore(group, name, key);
    }
    
    public void initViewButtons(int groupIndex, int nameIndex) {
        DetectorPane2D.buttonMap map = app.getDetectorView().getViewButtonMap(groupIndex,nameIndex);
        map.b.setSelected(true);        
        viewButtonAction(map.group,map.name,map.key);
     }
    
    public void initMapButtons(int groupIndex, int nameIndex) {
        DetectorPane2D.buttonMap map = app.getDetectorView().getMapButtonMap(groupIndex,nameIndex);
        map.b.setSelected(true);        
        mapButtonAction(map.group,map.name,map.key);
     }    
    
    public void initButtons() {
        
        System.out.println("initButtons()");
        
        initMapButtons(0, 0);
        initMapButtons(1, 0);
        initViewButtons(0, 0);
        initViewButtons(1, 3);
        app.getDetectorView().setFPS(10);
        app.setSelectedTab(2); 
        
    }
    
    public void initDetector(int is1, int is2) {
        
        System.out.println("initDetector()");
                
         for(int is=is1; is<is2; is++) {
            for(int ip=0; ip<ecPix[0].pc_nstr[0] ; ip++)             app.getDetectorView().getView().addShape("U",getStrip(is,1,ip));
            for(int ip=0; ip<ecPix[0].pc_nstr[1] ; ip++)             app.getDetectorView().getView().addShape("V",getStrip(is,2,ip));
            for(int ip=0; ip<ecPix[0].pc_nstr[2] ; ip++)             app.getDetectorView().getView().addShape("W",getStrip(is,3,ip));           
            for(int ip=0; ip<ecPix[0].pixels.getNumPixels() ; ip++)  app.getDetectorView().getView().addShape("PIX",getPixel(is,4,ip));
        }
        
         app.getDetectorView().getView().addDetectorListener(mon);
         
         for(String layer : app.getDetectorView().getView().getLayerNames()){
            app.getDetectorView().getView().setDetectorListener(layer,mon);
         }
         
         addButtons("DET","View","PC.1.ECi.1.ECo.2");
         addButtons("LAY","View","U.1.V.2.W.3.PIX.4");
         addButtons("UVW","Map","EVT.0.ADC.0.TDC.0");
         addButtons("PIX","Map","EVT.0.NEVT.1.ADC U.11.ADC V.12.ADC W.13.ADC U+V+W.9");
     
         app.getDetectorView().addMapButtons();
         app.getDetectorView().addViewButtons();
         
    }
    
    public DetectorShape2D getPixel(int sector, int layer, int pixel){

        DetectorShape2D shape = new DetectorShape2D();
        
        if (mondet=="PCAL") shape = new DetectorShape2D(DetectorType.PCAL,sector,layer,pixel);      
        if (mondet=="EC")   shape = new DetectorShape2D(DetectorType.EC,sector,layer,pixel);    
        
        Path3D shapePath = shape.getShapePath();
        
        for(int j = 0; j < ecPix[0].pc_nvrt[pixel]; j++){
            shapePath.addPoint(ecPix[0].pc_xpix[j][pixel][sector],ecPix[0].pc_ypix[j][pixel][sector],0.0);
        }
        
        shape.addDetectorListener(mon);
        return shape;
    }
    
    public DetectorShape2D getStrip(int sector, int layer, int str) {

        DetectorShape2D shape = new DetectorShape2D();
        
        if (mondet=="PCAL") shape = new DetectorShape2D(DetectorType.PCAL,sector,layer,str);        
        if (mondet=="EC")   shape = new DetectorShape2D(DetectorType.EC,sector,layer,str);  
        
        Path3D shapePath = shape.getShapePath();
        
        for(int j = 0; j <4; j++){
            shapePath.addPoint(ecPix[0].pc_xstr[j][str][layer-1][sector],ecPix[0].pc_ystr[j][str][layer-1][sector],0.0);
        }   
        
        shape.addDetectorListener(mon);
        return shape;
    }
    
    public void update(DetectorShape2D shape) {
        
        DetectorDescriptor dd = shape.getDescriptor();
        this.getDetIndices(dd);

        layer = lay;
        
        double colorfraction=1;
        
        inProcess = (int) mon.getGlob().get("inProcess");

        if (inProcess==0){ // Assign default colors upon starting GUI (before event processing)
            if(layer<7) colorfraction = (double)ic/36;
            if(layer>=7) colorfraction = getcolor((TreeMap<Integer, Object>) Lmap_a.get(0,0,0), ic);
        }
        if (inProcess>0){             // Use Lmap_a to get colors of components while processing data
                         colorfraction = getcolor((TreeMap<Integer, Object>) Lmap_a.get(is+1,layer,opt), ic);
        }
        if (colorfraction<0.05) colorfraction = 0.05;
        
        Color col = palette.getRange(colorfraction);
        shape.setColor(col.getRed(),col.getGreen(),col.getBlue());

    }
    
    public double getcolor(TreeMap<Integer,Object> map, int component) {
        
        double color=0;
        
        double val[] =(double[]) map.get(1); 
        double rmin  =(double)   map.get(2);
        double rmax  =(double)   map.get(3);
        double z=val[component];
        
        if (z==0) return 0;
        
        PCMon_zmax = rmax*1.2; mon.getGlob().put("PCMon_zmax", PCMon_zmax);
        
        if (inProcess==0)  color=(double)(z-rmin)/(rmax-rmin);
        double pixMin = app.displayControl.pixMin ; double pixMax = app.displayControl.pixMax;
        if (inProcess!=0) {
            if (!app.isSingleEvent()) color=(double)(Math.log10(z)-pixMin*Math.log10(rmin))/(pixMax*Math.log10(rmax)-pixMin*Math.log10(rmin));
            if ( app.isSingleEvent()) color=(double)(Math.log10(z)-pixMin*Math.log10(rmin))/(pixMax*Math.log10(4000.)-pixMin*Math.log10(rmin));
        }
        
        app.getDetectorView().getView().zmax = pixMax*rmax;
        app.getDetectorView().getView().zmin = pixMin*rmin;
        
        if (color>1)   color=1;
        if (color<=0)  color=0.;

        return color;
    }
    
}
