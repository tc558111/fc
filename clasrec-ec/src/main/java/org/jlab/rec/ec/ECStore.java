/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.rec.ec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;
import org.jlab.evio.clas12.EvioDataBank;
import org.jlab.evio.clas12.EvioDataEvent;
import org.jlab.geom.base.Detector;
import java.util.Map;

/**
 *
 * @author gavalian
 */
public class ECStore {
    
    public ArrayList<ECLayerHit>  ecHits  = new ArrayList<ECLayerHit>();
    public ArrayList<ECLayerPeak> ecPeaks = new ArrayList<ECLayerPeak>();
    public ArrayList<ECPeakCluster> ecPeakClusters = new ArrayList<ECPeakCluster>();
    
    public TreeMap<Integer,ECLayerPeak> ecPeaksTree = 
            new TreeMap<Integer,ECLayerPeak>();
    /**
     * Read the event and initialize arrays with hits from detectors of PCAL 
     * and EC.
     * @param event
     * @param ecGeom 
     */
    public void initECHits(EvioDataEvent event, Detector ecGeom, Map<String,double[][][][]> CCDB){
        
        if(event.hasBank("PCAL::dgtz")==true){
            EvioDataBank bankPCAL = (EvioDataBank) event.getBank("PCAL::dgtz");
            int nrows = bankPCAL.rows();
            int[] sector = bankPCAL.getInt("sector");
            int[] stack  = bankPCAL.getInt("stack");
            int[] view   = bankPCAL.getInt("view");
            int[] strip  = bankPCAL.getInt("strip");
            int[] ADC    = bankPCAL.getInt("ADC");
            int[] TDC    = bankPCAL.getInt("TDC");
            
            for(int loop = 0; loop < nrows; loop ++){
                double gain=CCDB.get("gain")[sector[loop]-1][stack[loop]-1][view[loop]-1][strip[loop]-1];
                double  ped=CCDB.get("ped")[sector[loop]-1][stack[loop]-1][view[loop]-1][strip[loop]-1];
                ECLayerHit hit = ECUtilities.getLayerHit(ecGeom,
                        sector[loop]-1,stack[loop]-1,view[loop]-1,
                        strip[loop]-1,(int)(ADC[loop]*gain-ped),TDC[loop]);
                if((int)(ADC[loop]*gain-ped)>10.0)
                    ecHits.add(hit);
            }
        }
        
        
        EvioDataBank bankEC = (EvioDataBank) event.getBank("EC::dgtz");
        
        if(bankEC!=null){
            int nrowsEC = bankEC.rows();
            //System.err.println(">>>>>>>> EC ROWS = " + nrowsEC);
            
            int[] e_sector = bankEC.getInt("sector");
            int[] e_stack  = bankEC.getInt("stack");
            int[] e_view   = bankEC.getInt("view");
            int[] e_strip  = bankEC.getInt("strip");
            int[] e_ADC    = bankEC.getInt("ADC");
            int[] e_TDC    = bankEC.getInt("TDC");
            
            for(int loop = 0; loop < nrowsEC; loop ++){
                double gain=CCDB.get("gain")[e_sector[loop]-1][e_stack[loop]][e_view[loop]-1][e_strip[loop]-1];
                double  ped=CCDB.get("ped")[e_sector[loop]-1][e_stack[loop]][e_view[loop]-1][e_strip[loop]-1];
                ECLayerHit hit = ECUtilities.getLayerHit(ecGeom,
                        e_sector[loop]-1,e_stack[loop],e_view[loop]-1,
                        e_strip[loop]-1,(int)(e_ADC[loop]*gain-ped),e_TDC[loop]);
                if((int)(e_ADC[loop]*gain-ped)>10.0)
                    ecHits.add(hit);
            }
            
            Collections.sort(ecHits);
        }
    }
    
    /**
     * Initializes the array with layer peaks.
     */
    public void initECPeaks(){
        for(ECLayerHit hit : ecHits){
            if(ecPeaks.size()==0){
                ecPeaks.add(new ECLayerPeak(hit));
            } else {
                boolean hitAdded = false;
                for(ECLayerPeak peak : ecPeaks){
                    if(peak.isInCluster(hit)==true){
                        peak.add(hit);
                        hitAdded = true;
                    }
                }
                if(hitAdded==false) ecPeaks.add(new ECLayerPeak(hit));
            }            
        }
        
        for(ECLayerPeak peak : ecPeaks)
            peak.redo();
    }
    
    public void initECPeakClusters(){
        for(int s = 0 ; s < 6 ; s++){
            for(int sl = 0; sl < 3; sl ++){
                ArrayList<ECLayerPeak> peakU = this.getLayerPeaks(s, sl, 0);
                ArrayList<ECLayerPeak> peakV = this.getLayerPeaks(s, sl, 1);
                ArrayList<ECLayerPeak> peakW = this.getLayerPeaks(s, sl, 2);
                for(int uloop = 0; uloop < peakU.size(); uloop++)
                    for(int vloop = 0; vloop < peakV.size();vloop++)
                        for(int wloop = 0; wloop < peakW.size();wloop++){
                            ECPeakCluster cluster = new ECPeakCluster(s,sl,
                                    peakU.get(uloop),peakV.get(vloop),
                            peakW.get(wloop));
                            cluster.redo();
                            if(cluster.hitPositionError.x()<4.5&&
                                    cluster.hitPositionError.z()<2.0)
                                ecPeakClusters.add(cluster);
                        }
                
            }
        }
    }
    
    public EvioDataBank getBankHits(EvioDataEvent event){
        int nhits = ecHits.size();
        EvioDataBank bank = (EvioDataBank) event.getDictionary().createBank("ECRec::hits", nhits);
        for(int loop = 0; loop <nhits; loop++){
            bank.setInt("sector", loop,ecHits.get(loop).sector);
            bank.setInt("superlayer", loop,ecHits.get(loop).superlayer);
            bank.setInt("view", loop,ecHits.get(loop).layer);
            bank.setInt("strip",loop,ecHits.get(loop).strip);
            bank.setDouble("energy", loop,ecHits.get(loop).energy);
            bank.setDouble("time", loop,ecHits.get(loop).time);
        }
        return bank;
    }
    
    public EvioDataBank getBankPeaks(EvioDataEvent event){
        int nhits = ecPeaks.size();
        EvioDataBank bank = (EvioDataBank) event.getDictionary().createBank("ECRec::peaks", nhits);
        for(int loop = 0; loop <nhits; loop++){
            bank.setInt("sector", loop,ecPeaks.get(loop).sector);
            bank.setInt("superlayer", loop,ecPeaks.get(loop).superlayer);
            bank.setInt("view", loop,ecPeaks.get(loop).layer);
            bank.setDouble("energy", loop,ecPeaks.get(loop).getEnergy());
            bank.setDouble("time", loop,ecPeaks.get(loop).getTime());
            bank.setDouble("Xo", loop,ecPeaks.get(loop).getLine().origin().x());
            bank.setDouble("Yo", loop,ecPeaks.get(loop).getLine().origin().y());
            bank.setDouble("Zo", loop,ecPeaks.get(loop).getLine().origin().z());
            bank.setDouble("Xe", loop,ecPeaks.get(loop).getLine().end().x());
            bank.setDouble("Ye", loop,ecPeaks.get(loop).getLine().end().y());
            bank.setDouble("Ze", loop,ecPeaks.get(loop).getLine().end().z());
            bank.setDouble("width", loop,ecPeaks.get(loop).getWidth());            
        }
        return bank;
    }
    
    public EvioDataBank getBankClusters(EvioDataEvent event){
        int nhits = ecPeakClusters.size();
        EvioDataBank bank = (EvioDataBank) event.getDictionary().createBank("ECRec::clusters", nhits);
        for(int loop = 0; loop <nhits; loop++){
            bank.setInt("sector", loop,ecPeakClusters.get(loop).sector);
            bank.setInt("superlayer", loop,ecPeakClusters.get(loop).superlayer);
            bank.setDouble("energy", loop,ecPeakClusters.get(loop).getEnergy());
            bank.setDouble("time", loop, 0.0);
            bank.setDouble("X", loop,ecPeakClusters.get(loop).hitPosition.x());
            bank.setDouble("Y", loop,ecPeakClusters.get(loop).hitPosition.y());
            bank.setDouble("Z", loop,ecPeakClusters.get(loop).hitPosition.z());
            bank.setDouble("dX", loop,ecPeakClusters.get(loop).hitPositionError.x());
            bank.setDouble("dY", loop,ecPeakClusters.get(loop).hitPositionError.y());
            bank.setDouble("dZ", loop,ecPeakClusters.get(loop).hitPositionError.z());
        }
        return bank;
    }
    
    public ArrayList<ECLayerPeak>  getLayerPeaks(int sector, int superlayer, int layer){
        ArrayList<ECLayerPeak>  peaks = new ArrayList<ECLayerPeak>();
        for(ECLayerPeak peak : ecPeaks){
            if(peak.sector==sector && peak.superlayer==superlayer&&
                    peak.layer==layer) peaks.add(peak);
        }
        return peaks;
    }
    /**
     * Prints out information about peaks
     */
    public void showPeaks(){
        System.err.println(">>>>>>>>>>>>>>>>>>>> EC PEAKS ARRAY");
        for(ECLayerPeak peak : ecPeaks){
            System.err.println(peak.toString());
        }
    }
    /**
     * prints out information about individual hits
     */
    public void showHits(){
        System.err.println(">>>>>>>>>>>>>>>>>>>> EC HITS ARRAY");
        for(ECLayerHit hit : ecHits){
            System.err.println("\t" + hit.toString());
        }
    }

    public void showClusters(){
        System.err.println(">>>>>>>>>>>>>>>>>>>> EC CLUSTERS ARRAY");
        for(ECPeakCluster cluster : ecPeakClusters){
            System.err.println(cluster.toString());
        }
    }
    
}
