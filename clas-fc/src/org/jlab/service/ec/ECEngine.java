/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.service.ec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jlab.clas.detector.DetectorCollection;
import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.base.GeometryFactory;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.base.Detector;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.evio.EvioFactory;
/**
 *
 * @author gavalian
 */
public class ECEngine extends ReconstructionEngine {

    Detector ecDetector = null;
    
    public ECEngine(){
        super("EC","gavalian","1.0");
    }
        
    @Override
    public boolean processDataEvent(DataEvent de) {
           
        List<ECStrip>  ecStrips = ECCommon.initEC(de, ecDetector, this.getConstantsManager(), 2);        
        System.out.println(" STRIPS SIZE = " + ecStrips.size());
        for(ECStrip strip : ecStrips) System.out.println(strip);
        List<ECPeak> ecPeaksALL = ECCommon.createPeaks(ecStrips);
        List<ECPeak>    ecPeaks = ECCommon.processPeaks(ecPeaksALL);
        int       peaksOriginal = ecPeaks.size();
        System.out.println(" ORIGINAL PEAKS  SIZE = " + ecPeaksALL.size());
        for(ECPeak p : ecPeaksALL){ System.out.println(p);}
        
        ECPeakAnalysis.splitPeaks(ecPeaks);
        int peaksOriginalSplit = ecPeaks.size();
        System.out.println(String.format("SPLIT PROCEDURE %8d %8d",peaksOriginal,peaksOriginalSplit));
               
        for(ECPeak p : ecPeaks){
            p.redoPeakLine();
//            System.out.println(p);
        }
                
        List<ECCluster>  cPCAL  = ECCommon.createClusters(ecPeaks,1);
        List<ECCluster>  cECIN  = ECCommon.createClusters(ecPeaks,4);
        List<ECCluster>  cECOUT = ECCommon.createClusters(ecPeaks,7);
        
        List<ECCluster>     cEC = new ArrayList<ECCluster>();
        
        cEC.addAll(cPCAL);
        cEC.addAll(cECIN);
        cEC.addAll(cECOUT);
        
        ECCommon.shareClustersEnergy(cEC);  
        
        System.out.println("\n\n\n\n\nEC CLUSTERS SIZE = " + cEC.size());
        if(cEC.size()==2){
            for(ECCluster c : cEC){            
                 System.out.println(c);
            }
        }
        
        writeBanks(de,ecStrips,ecPeaks,cEC);
        
        //for(ECPeak p : ecPeaks){ System.out.println(p);}
        /*
        for(ECPeak peak : ecPeaks){
            //peak.redoPeakLine();
            if(peak.getMultiplicity()==4){
                System.out.println(peak);
                int stripSplit = peak.getSplitStrip();                
                if(stripSplit>0){
                    List<ECPeak>  twoPeaks = peak.splitPeak(stripSplit);
                    for(ECPeak p : twoPeaks){
                        System.out.println("\t SPLIT PEAK  = " + p);                        
                    }
                }
            }
        }*/
        return true;
    }
    
    public void writeBanks(DataEvent de, 
                           List<ECStrip>   strips, 
                           List<ECPeak>    peaks, 
                           List<ECCluster> clusters) {

        EvioDataBank bankS = (EvioDataBank) EvioFactory.createBank("ECDetector::hits", strips.size());
        for(int h = 0; h < strips.size(); h++){
            bankS.setInt("sector",h, strips.get(h).getDescriptor().getSector());
            bankS.setInt("layer",h,strips.get(h).getDescriptor().getLayer());
            bankS.setInt("strip",h,strips.get(h).getDescriptor().getComponent());
            bankS.setInt("peakid",h,strips.get(h).getPeakId());
            bankS.setDouble("energy", h, strips.get(h).getEnergy());
            bankS.setDouble("time", h, strips.get(h).getTime());                
        }
        
        EvioDataBank  bankP =  (EvioDataBank) EvioFactory.createBank("ECDetector::peaks", peaks.size());
        for(int p = 0; p < peaks.size(); p++){
            bankP.setInt("sector",p,peaks.get(p).getDescriptor().getSector());
            bankP.setInt("layer", p,peaks.get(p).getDescriptor().getLayer());
            bankP.setDouble("Xo", p,peaks.get(p).getLine().origin().x());
            bankP.setDouble("Yo", p,peaks.get(p).getLine().origin().y());
            bankP.setDouble("Zo", p,peaks.get(p).getLine().origin().z());
            bankP.setDouble("Xe", p,peaks.get(p).getLine().end().x());
            bankP.setDouble("Ye", p,peaks.get(p).getLine().end().y());
            bankP.setDouble("Ze", p,peaks.get(p).getLine().end().z());
            bankP.setDouble("energy",p,peaks.get(p).getEnergy());
            bankP.setDouble("time",p,peaks.get(p).getTime());
        }
        
        EvioDataBank  bankC =  (EvioDataBank) EvioFactory.createBank("ECDetector::clusters", clusters.size());
        for(int c = 0; c < clusters.size(); c++){
            bankC.setInt("sector", c, clusters.get(c).clusterPeaks.get(0).getDescriptor().getSector());
            bankC.setInt("layer", c, clusters.get(c).clusterPeaks.get(0).getDescriptor().getLayer());
            bankC.setDouble("energy", c, clusters.get(c).getEnergy());
            bankC.setDouble("time", c, clusters.get(c).getTime());
            bankC.setInt("uid", c, clusters.get(c).UVIEW_ID);
            bankC.setInt("vid", c, clusters.get(c).VVIEW_ID);
            bankC.setInt("wid", c, clusters.get(c).WVIEW_ID);
            bankC.setDouble("X", c, clusters.get(c).getHitPosition().x());
            bankC.setDouble("Y", c, clusters.get(c).getHitPosition().y());
            bankC.setDouble("Z", c, clusters.get(c).getHitPosition().z());
            bankC.setDouble("dX", c, clusters.get(c).getHitPositionError());
            bankC.setDouble("dY", c, clusters.get(c).getHitPositionError());
            bankC.setDouble("dZ", c, clusters.get(c).getHitPositionError());
            bankC.setDouble("widthU", c, clusters.get(c).getPeak(0).getMultiplicity());
            bankC.setDouble("widthV", c, clusters.get(c).getPeak(1).getMultiplicity());
            bankC.setDouble("widthW", c, clusters.get(c).getPeak(2).getMultiplicity());
            bankC.setInt("coordU", c, clusters.get(c).getPeak(0).getCoord());
            bankC.setInt("coordV", c, clusters.get(c).getPeak(1).getCoord());
            bankC.setInt("coordW", c, clusters.get(c).getPeak(2).getCoord());
        }
       
        EvioDataBank  bankD =  (EvioDataBank) EvioFactory.createBank("ECDetector::calib", clusters.size());
        for(int c = 0; c < clusters.size(); c++){
            bankD.setInt("sector", c, clusters.get(c).clusterPeaks.get(0).getDescriptor().getSector());
            bankD.setInt("layer", c, clusters.get(c).clusterPeaks.get(0).getDescriptor().getLayer());
            bankD.setDouble("energy", c, clusters.get(c).getEnergy());
            bankD.setDouble("rawEU", c, clusters.get(c).getRawEnergy(0));
            bankD.setDouble("rawEV", c, clusters.get(c).getRawEnergy(1));
            bankD.setDouble("rawEW", c, clusters.get(c).getRawEnergy(2));
            bankD.setDouble("recEU", c, clusters.get(c).getEnergy(0));
            bankD.setDouble("recEV", c, clusters.get(c).getEnergy(1));
            bankD.setDouble("recEW", c, clusters.get(c).getEnergy(2));            
        }
        
        de.appendBanks(bankS,bankP,bankC,bankD);        
       
    }
    
    public void setStripThresholds(int thr0, int thr1, int thr2) {
        System.out.println("ECEngine: Strip ADC thresholds = "+thr0+" "+thr1+" "+thr2);
        ECCommon.stripThreshold[0] = thr0;
        ECCommon.stripThreshold[1] = thr1;
        ECCommon.stripThreshold[2] = thr2;
    }
    
    public void setPeakThresholds(int thr0, int thr1, int thr2) {
        System.out.println("ECEngine: Peak ADC thresholds = "+thr0+" "+thr1+" "+thr2);
        ECCommon.peakThreshold[0] = thr0;
        ECCommon.peakThreshold[1] = thr1;
        ECCommon.peakThreshold[2] = thr2;
    }   
    
    public void setClusterErrors(float err0, float err1, float err2) {
        System.out.println("ECEngine: Cluster Errors = "+err0+" "+err1+" "+err2);
        ECCommon.clusterError[0] = err0;
        ECCommon.clusterError[1] = err1;
        ECCommon.clusterError[2] = err2;
    }
    
    public DetectorCollection<H1F>  getHist() {
        return ECCommon.H1_ecEng;
    }
    
    @Override
    public boolean init() {
        String[]  ecTables = new String[]{
            "/calibration/ec/attenuation", 
            "/calibration/ec/gain", 
        };
               
        requireConstants(Arrays.asList(ecTables));
        
        ecDetector =  GeometryFactory.getDetector(DetectorType.EC);
        ECCommon.initHistos();
        return true;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
