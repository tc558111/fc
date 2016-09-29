/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.ecn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
//import org.jlab.evio.clas12.EvioDataBank;
//import org.jlab.evio.clas12.EvioDataEvent;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.base.Detector;

//clas12rec
import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataEvent;

/**
 *
 * @author gavalian
 */
public class ECStore {
    
    List<ECStrip>    ecStrips    = new ArrayList<ECStrip>();
    List<ECPeak>     ecPeaks     = null;
    List<ECCluster>  ecClusters  = null;
    int                   ind[]  = {0,0,0,1,1,1,2,2,2}; 
    
    public ECStore(){
        
    }
    
    public void processGemc(EvioDataEvent event, Detector  geom, ConstantProvider  calib){
        this.initStripsGemc(event, geom, calib);
        Collections.sort(ecStrips);
        this.ecPeaks    = ECCommon.createPeaks(ecStrips);
        this.ecClusters = ECCommon.createClusters(ecPeaks);
    }
    
    
    public void initStripsGemc(EvioDataEvent event, Detector  geom, ConstantProvider  calib){
        
        ecStrips.clear();
        
        //************************************************************
        // Load data from PCAL banks
        
        EvioDataBank bankPCAL = (EvioDataBank) event.getBank("PCAL::dgtz");
        //if(bank==null) return;
        if(bankPCAL!=null){
            int nrowsEC = bankPCAL.rows();
            for(int row = 0; row < nrowsEC; row++){
                int sector = bankPCAL.getInt("sector", row);
                //int layer  = bankPCAL.getInt("stack", row)*3 + bankPCAL.getInt("view", row);
                int layer  = bankPCAL.getInt("view", row);                
                int comp   = bankPCAL.getInt("strip", row);
                ECStrip strip = ECCommon.createStrip(sector,layer,comp,geom);
            
                strip.setADC(bankPCAL.getInt("ADC", row));
                strip.setTDC(bankPCAL.getInt("TDC", row));
                
                int index = ECCommon.getCalibrationIndex(sector, layer, comp);
                
                strip.setAttenuation(
                        calib.getDouble("/calibration/ec/attenuation/A", index),
                        calib.getDouble("/calibration/ec/attenuation/B", index),
                        calib.getDouble("/calibration/ec/attenuation/C", index)
                );
                
                strip.setGain(calib.getDouble("/calibration/ec/gain/gain", index));
                if(strip.getADC()>ECCommon.stripThreshold[ind[layer-1]]) ecStrips.add(strip);                       
            }
            
        }
        
        //************************************************************
        // Load data from PCAL banks
        
        EvioDataBank bankEC = (EvioDataBank) event.getBank("EC::dgtz");
        //if(bank==null) return;
        if(bankEC!=null){
            int nrowsEC = bankEC.rows();
            for(int row = 0; row < nrowsEC; row++){
                int sector = bankEC.getInt("sector", row);
                int layer  = bankEC.getInt("stack", row)*3 + bankEC.getInt("view", row);
                int comp   = bankEC.getInt("strip", row);
                ECStrip strip = ECCommon.createStrip(sector,layer,comp,geom);
            
                strip.setADC(bankEC.getInt("ADC", row));
                strip.setTDC(bankEC.getInt("TDC", row));
                
                int index = ECCommon.getCalibrationIndex(sector, layer, comp);
                
                strip.setAttenuation(
                        calib.getDouble("/calibration/ec/attenuation/A", index),
                        calib.getDouble("/calibration/ec/attenuation/B", index),
                        calib.getDouble("/calibration/ec/attenuation/C", index)
                );
                
                strip.setGain(calib.getDouble("/calibration/ec/gain/gain", index));
                if(strip.getADC()>ECCommon.stripThreshold[ind[layer-1]]) ecStrips.add(strip);                       
            }
            
        }        
    }
    
    public List<ECStrip>     getStrips(){return this.ecStrips;}
    public List<ECPeak>       getPeaks(){return this.ecPeaks;}
    public List<ECCluster> getClusters(){return this.ecClusters;}
    
    public void           showStrips(){
        System.out.println("************************  STRIPS  *********************");
        for(ECStrip strip : this.ecStrips){
            System.out.println(strip);
        }
    }
    
    public void           showPeaks(){
        System.out.println("************************  PEAKS  *********************");
        for(ECPeak peak : this.ecPeaks){
            System.out.println(peak);
        }
    }
    
    public void           showClusters(){
        System.out.println("************************  CLUSTERS  *********************");
        for(ECCluster cluster : this.ecClusters){
            System.out.println(cluster);
        }
    }
}
