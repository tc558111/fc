/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.fcmon.tools;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javax.swing.JPanel;

import org.clas.fcmon.cc.CCPixels;
import org.clas.fcmon.detector.view.DetectorPane2D;
import org.clas.fcmon.detector.view.DetectorShape2D;
import org.clas.fcmon.ftof.FTOFPixels;
import org.jlab.clas.detector.DetectorCollection;
import org.jlab.detector.base.DetectorDescriptor;
import org.root.attr.ColorPalette;

/**
 *
 * @author lcsmith
 */
public class FCDetector {
    
    ColorPalette palette = new ColorPalette();

    private String                 appName = null;
    
    public ECPixels[]                ecPix = null;  
    public CCPixels                  ccPix = null; 
    public FTOFPixels[]            ftofPix = null; 
    public MonitorApp                  app = null;
    public DetectorMonitor             mon = null;
    public TreeMap<String,JPanel>  rbPanes = new TreeMap<String,JPanel>();
    public TreeMap<String,Integer>  bStore = new TreeMap<String,Integer>();
    
    public DetectorCollection<TreeMap<Integer,Object>> Lmap_a = new  DetectorCollection<TreeMap<Integer,Object>>();
    public DetectorCollection<TreeMap<Integer,Object>> Lmap_t = new  DetectorCollection<TreeMap<Integer,Object>>();
    
    public int is,layer,ic;
    public int panel,opt,io,of,lay,l1,l2;
    
    int          omap = 0;
    int         ilmap = 0;    
    int     inProcess = 0;
    int     nStrips[] = new int[6];
    double PCMon_zmin = 0;
    double PCMon_zmax = 0;
    
    public FCDetector(ECPixels[] ecPix) {
        this.ecPix = ecPix;     
    }
    
    public FCDetector(CCPixels ccPix) {
        this.ccPix = ccPix;     
    }
    
    public FCDetector(FTOFPixels[] ftofPix) {
        this.ftofPix = ftofPix;     
    }
    
    public FCDetector(String name, ECPixels[] ecPix) {
        this.appName = name;
        this.ecPix = ecPix;  
        this.nStrips[0] = ecPix[0].pc_nstr[0];
        this.nStrips[1] = ecPix[0].pc_nstr[1];
        this.nStrips[2] = ecPix[0].pc_nstr[2];
        this.nStrips[3] = ecPix[0].pc_nstr[0];
        this.nStrips[4] = ecPix[0].pc_nstr[1];
        this.nStrips[5] = ecPix[0].pc_nstr[2];
    }
    
    public FCDetector(String name, CCPixels ccPix) {
        this.appName = name;
        this.ccPix = ccPix;   
        this.nStrips[0] = ccPix.cc_nstr[0];
        this.nStrips[1] = ccPix.cc_nstr[1];
    }
    
    public FCDetector(String name, FTOFPixels[] ftofPix) {
        this.appName = name;
        this.ftofPix = ftofPix;   
        this.nStrips[0] = ftofPix[0].nstr;
        this.nStrips[1] = ftofPix[1].nstr;
        this.nStrips[2] = ftofPix[2].nstr;
    }
    
    public void setApplicationClass(MonitorApp app) {
        this.app = app;
        app.getDetectorView().addFCDetectorListeners(this);
    }
    
    public void setMonitoringClass(DetectorMonitor mon) {
        this.mon = mon;
    }    
    
    public void addLMaps(String name, DetectorCollection<TreeMap<Integer,Object>> map) {
        this.Lmap_a=map;
    }
  
    public void getDetIndices(DetectorDescriptor dd) {
        is    = dd.getSector();
        layer = dd.getLayer();
        ic    = dd.getComponent();   
                
        panel = omap;
//        io    = ilmap+1;
//        if (ilmap==0) of = (io-1)*3;
        of    = 0;
        lay   = 0;
        opt   = 0;
        
        if (panel==1) opt = 1;
        if (layer<4)  lay = layer+of;
        if (layer==4) lay = layer+2+io;
        if (panel==9) lay = panel+io-1;
        if (panel>10) lay = panel+of;
        
        l1 = of+1;
        l2 = of+4;  
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
    
    public void mapButtonAction(String group, String name, int key) {
        this.bStore = app.getDetectorView().bStore;
        if (!bStore.containsKey(group)) {
            bStore.put(group,key);
        }else{
            bStore.replace(group,key);
        }
        omap = key;
        app.getDetectorView().update();     
    }
    
    public void viewButtonAction(String group, String name, int key) {
        this.bStore  = app.getDetectorView().bStore;
        this.rbPanes = app.getDetectorView().rbPanes;
        if(group=="LAY") {
            app.currentView = name;
            name = name+Integer.toString(ilmap);
            app.getDetectorView().getView().setLayerState(name, true);
            if (key<4) {rbPanes.get("PMT").setVisible(true);rbPanes.get("PIX").setVisible(false);omap=bStore.get("PMT");}       
            if (key>3) {rbPanes.get("PIX").setVisible(true);rbPanes.get("PMT").setVisible(false);omap=bStore.get("PIX");}
        }
        if(group=="DET") {
            ilmap = key;            
            name = app.currentView+Integer.toString(ilmap);  
            app.getDetectorView().getView().setLayerState(name, true);
        }       
        app.getDetectorView().update();        
    }     
    
    public void update(DetectorShape2D shape) {
        
        DetectorDescriptor dd = shape.getDescriptor();
        this.getDetIndices(dd);
        layer = lay;
        
        double colorfraction=1;
        
        inProcess = (int) mon.getGlob().get("inProcess"); // Get process status
        
        // Update shape color map depending on process status and layer
        // layers 1-6 reserved for strip views, layers >7 for pixel views
        // Lmap_a stores live colormap of detector shape elements
        
        if (inProcess==0){ // Assign default colors upon starting GUI (before event processing)
             if(layer<7) colorfraction = (double)ic/nStrips[ilmap]; 
            if(layer>=7) colorfraction = getcolor((TreeMap<Integer, Object>) Lmap_a.get(0,0,0), ic);  
        }
        if (inProcess>0){      
                         colorfraction = getcolor((TreeMap<Integer, Object>) Lmap_a.get(is,layer,ilmap), ic);
        }
        
        if (colorfraction<0.05) colorfraction = 0.05;
        Color col = palette.getRange(colorfraction);
        shape.setColor(col.getRed(),col.getGreen(),col.getBlue());

    }
    
    public double getcolor(TreeMap<Integer,Object> map, int component) {
        
        double color=0;
        double smax=4000.;
        
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
            if ( app.isSingleEvent()) color=(double)(Math.log10(z)-pixMin*Math.log10(rmin))/(pixMax*Math.log10(smax)-pixMin*Math.log10(rmin));
        }
        
        // Set color bar min,max
        app.getDetectorView().getView().zmax = pixMax*rmax;
        app.getDetectorView().getView().zmin = pixMin*rmin;
        
        if (color>1)   color=1;
        if (color<=0)  color=0.;

        return color;
    }    
}
