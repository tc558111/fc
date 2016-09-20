/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.ecn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jlab.clas.detector.DetectorType;
import org.jlab.clas.physics.LorentzVector;
import org.jlab.clas12.detector.DetectorCounter;
import org.jlab.clas12.detector.EventDecoder;
import org.jlab.clas12.physics.DetectorParticle;
//import org.jlab.clasrec.main.DetectorReconstruction;
import org.jlab.clasrec.utils.ServiceConfiguration;
//import org.jlab.evio.clas12.EvioDataBank;
//import org.jlab.evio.clas12.EvioDataEvent;
//import org.jlab.evio.clas12.EvioFactory;
import org.root.histogram.H1D;

//clas12rec
import org.jlab.detector.decode.DetectorDataDgtz;
import org.jlab.detector.decode.DetectorEventDecoder;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.evio.EvioFactory;
/**
 *
 * @author gavalian
 */
public class ECDetectorReconstruction extends DetectorReconstruction {
    
//    EventDecoder          decoder = new EventDecoder();
    DetectorEventDecoder   detectorDecoder = new DetectorEventDecoder();
    List<DetectorDataDgtz>  detectorData   = new ArrayList<DetectorDataDgtz>();
    List<ECStrip>                recStrips = new ArrayList<ECStrip>();
    int[]                     stripThreshold = new int[3];
    int[]                      peakThreshold = new int[3];
    
    public ECDetectorReconstruction(){
        super("ECREC","gavalian","1.0");
    }        

    /*
    public void initStripsFromRaw(EvioDataEvent de){
         EvioDataEvent  event = (EvioDataEvent) de;
        decoder.decode(event);
        
        System.out.println("********************   EVENT   **********************");
        List<DetectorCounter>  counters   = decoder.getDetectorCounters(DetectorType.PCAL);
        List<DetectorCounter>  countersEC = decoder.getDetectorCounters(DetectorType.EC);
        List<ECStrip>  strips = new ArrayList<ECStrip>();
        
        for(DetectorCounter cnt : counters){
            //System.out.println(cnt);
            if(cnt.getChannels().size()==1){
                if(cnt.getChannels().get(0).getADC().size()>0){
                    /*ECStrip strip = new ECStrip(cnt.getDescriptor().getSector(),
                            cnt.getDescriptor().getLayer(), cnt.getDescriptor().getComponent());
                    
                    ECStrip strip = ECCommon.createStrip(cnt.getDescriptor().getSector(),
                            cnt.getDescriptor().getLayer(), cnt.getDescriptor().getComponent()
                            , this.getGeometry("EC"));
                    strip.setADC(cnt.getChannels().get(0).getADC().get(0));
                    //strip.setTDC(cnt.getChannels().get(0).getTDC().get(0));
                    strips.add(strip);
                }
            }
        }
        
        for(DetectorCounter cnt : countersEC){
            //System.out.println(cnt);
            if(cnt.getChannels().size()==1){
                if(cnt.getChannels().get(0).getADC().size()>0){
                    
                    ECStrip strip = ECCommon.createStrip(cnt.getDescriptor().getSector(),
                            cnt.getDescriptor().getLayer()+3, cnt.getDescriptor().getComponent()
                            , this.getGeometry("EC"));
                    strip.setADC(cnt.getChannels().get(0).getADC().get(0));
                    //strip.setTDC(cnt.getChannels().get(0).getTDC().get(0));
                    strips.add(strip);
                }
            }
        }
    }
*/           
    
    @Override
    public void processEvent(EvioDataEvent de) {
        ECStore               ecStore = new ECStore();
        if(de.hasBank("PCAL::dgtz")==true || de.hasBank("EC::dgtz")==true){
            ecStore.processGemc(de, this.getGeometry("EC"), this.getConstants("ECCALIB"));
            
            //if(this.debugLevel()>0)
            //ecStore.showStrips();
            //ecStore.showPeaks();
            //ecStore.showClusters();
        
            List<ECStrip>  strips = ecStore.getStrips();
            EvioDataBank bankS = (EvioDataBank) EvioFactory.createBank("ECDetector::hits", strips.size());
            for(int h = 0; h < strips.size(); h++){
                bankS.setInt("sector",h, strips.get(h).getDescriptor().getSector());
                bankS.setInt("layer",h,strips.get(h).getDescriptor().getLayer());
                bankS.setInt("strip",h,strips.get(h).getDescriptor().getComponent());
                bankS.setInt("peakid",h,strips.get(h).getPeakId());
                bankS.setDouble("energy", h, strips.get(h).getEnergy());
                bankS.setDouble("time", h, strips.get(h).getTime());                
            }
            
            List<ECPeak>  peaks = ecStore.getPeaks();
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
            
            List<ECCluster>  clusters = ecStore.getClusters();
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
        
        
        /*
        if(de.hasBank("GenPart::true")){
            EvioDataBank genBank = (EvioDataBank) de.getBank("GenPart::true");
            genBank.show();
        }*/
    }
    
    public void setStripThresholds(int thr0, int thr1, int thr2) {
        System.out.println("ECDetectorReconstruction: Strip ADC thresholds = "+thr0+" "+thr1+" "+thr2);
        ECCommon.stripThreshold[0] = thr0;
        ECCommon.stripThreshold[1] = thr1;
        ECCommon.stripThreshold[2] = thr2;
    }
    
    public void setPeakThresholds(int thr0, int thr1, int thr2) {
        System.out.println("ECDetectorReconstruction: Peak ADC thresholds = "+thr0+" "+thr1+" "+thr2);
        ECCommon.peakThreshold[0] = thr0;
        ECCommon.peakThreshold[1] = thr1;
        ECCommon.peakThreshold[2] = thr2;
    }
    
    @Override
    public void init() {
        this.requireGeometry("EC");
        this.setCalibrationRun(2);
        this.requireCalibration("ECCALIB", 
                "/calibration/ec/attenuation",
                "/calibration/ec/gain",
                "/calibration/ec/timing",
                "/calibration/ec/pedestal"
                );        
    }

    @Override
    public void configure(ServiceConfiguration sc) {
        
    }
        
}
