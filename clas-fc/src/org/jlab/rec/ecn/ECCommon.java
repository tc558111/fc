/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.ecn;

import java.util.ArrayList;
import java.util.List;
import org.jlab.clas.detector.DetectorType;
import org.jlab.clas.physics.Particle;
import org.jlab.clas.physics.Vector3;
import org.jlab.clas12.physics.DetectorParticle;
import org.jlab.clas12.physics.DetectorResponse;
import org.jlab.geom.base.Detector;
import org.jlab.geom.base.Layer;
import org.jlab.geom.component.ScintillatorPaddle;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Vector3D;

/**
 *
 * @author gavalian
 */
public class ECCommon {
    
    public static double PCAL_SF  = 0.2;
    public static double ECIN_SF  = 0.2;
    public static double ECOUT_SF = 0.2;
    public static double PCAL_EC_MATCHING_CUT = 7.5;
    
    public static int[]    EC_VIEW_STRIPS = new int[]{68,62,62,36,36,36,36,36,36};
    
    public static int[]  stripThreshold = new int[3];
    public static int[]   peakThreshold = new int[3];    
    
    public static ECStrip  createStrip(int sector, int layer, int component, Detector det){
        try {
            int superlayer = (layer-1)/3;
            int clayer     = (layer-1)%3;
            Layer detLayer = det.getSector(sector-1).getSuperlayer(superlayer).getLayer(clayer);
            ECStrip  strip = new ECStrip(sector,layer,component);
            ScintillatorPaddle paddle = (ScintillatorPaddle) detLayer.getComponent(component-1);
            strip.getLine().copy(paddle.getLine());
            return strip;
        } catch (Exception e){
            System.out.println("ERROR CREATING STRIP : " + sector + " " + layer + " " + component);
        }
        return new ECStrip(sector,layer,component);
    }
    
    
    public static int getCalibrationIndex(int sector, int layer, int component){
        int sector_offset = 68 + 62 + 62 + 6 * 36;

        int layer_offset = 0;
        if(layer>0){
            for(int loop = 0; loop < layer-1;loop++) layer_offset += ECCommon.EC_VIEW_STRIPS[loop];
        }
        int index = (sector-1)*sector_offset + layer_offset + (component - 1);
        return index;
    }
    
    public static List<ECPeak>   getPeaks(int sector, int layer, List<ECPeak> peaks){
        List<ECPeak>  selected = new ArrayList<ECPeak>();
        for(ECPeak peak : peaks){
            if(peak.getDescriptor().getSector()==sector&&peak.getDescriptor().getLayer()==layer){
                selected.add(peak);
            }
        }
        return selected;
    }
    
    
    public static List<ECCluster>   createClusters(List<ECPeak>  peaks){
        List<ECCluster>   clusters = new ArrayList<ECCluster>();
        for(int p = 0; p < peaks.size(); p++){
            peaks.get(p).setOrder(p+1);
        }
        
        
        for(int sector = 1; sector <= 6; sector++){
            
            List<ECPeak>  pU = ECCommon.getPeaks(sector, 1, peaks);
            List<ECPeak>  pV = ECCommon.getPeaks(sector, 2, peaks);
            List<ECPeak>  pW = ECCommon.getPeaks(sector, 3, peaks);
            
            if(pU.size()>0&&pV.size()>0&&pW.size()>0){
                for(int bU = 0; bU < pU.size();bU++){
                    for(int bV = 0; bV < pV.size();bV++){
                        for(int bW = 0; bW < pW.size();bW++){
                            ECCluster cluster = new ECCluster(
                                    pU.get(bU),pV.get(bV),pW.get(bW));
                            
                            if(cluster.getHitPositionError()<10.0)
                                clusters.add(cluster);
                        }
                    }
                }
            }
            
            List<ECPeak>  pUi = ECCommon.getPeaks(sector, 4, peaks);
            List<ECPeak>  pVi = ECCommon.getPeaks(sector, 5, peaks);
            List<ECPeak>  pWi = ECCommon.getPeaks(sector, 6, peaks);
            
            if(pUi.size()>0&&pVi.size()>0&&pWi.size()>0){
                for(int bU = 0; bU < pUi.size();bU++){
                    for(int bV = 0; bV < pVi.size();bV++){
                        for(int bW = 0; bW < pWi.size();bW++){
                            ECCluster cluster = new ECCluster(
                                    pUi.get(bU),pVi.get(bV),pWi.get(bW));
                            
                            if(cluster.getHitPositionError()<10.0)
                                clusters.add(cluster);
                        }
                    }
                }
            }
            
            List<ECPeak>  pUo = ECCommon.getPeaks(sector, 7, peaks);
            List<ECPeak>  pVo = ECCommon.getPeaks(sector, 8, peaks);
            List<ECPeak>  pWo = ECCommon.getPeaks(sector, 9, peaks);
            
            if(pUo.size()>0&&pVo.size()>0&&pWo.size()>0){
                for(int bU = 0; bU < pUo.size();bU++){
                    for(int bV = 0; bV < pVo.size();bV++){
                        for(int bW = 0; bW < pWo.size();bW++){
                            ECCluster cluster = new ECCluster(
                                    pUo.get(bU),pVo.get(bV),pWo.get(bW));
                            
                            if(cluster.getHitPositionError()<10.0)
                                clusters.add(cluster);
                        }
                    }
                }
            }
            
        }
        
        
        return clusters;
    }
    
    public static List<ECPeak>  createPeaks(List<ECStrip> stripList){
        List<ECPeak>  peakList = new ArrayList<ECPeak>();
        if(stripList.size()>1){
            ECPeak  firstPeak = new ECPeak(stripList.get(0));
            peakList.add(firstPeak);
            for(int loop = 1; loop < stripList.size(); loop++){
                boolean stripAdded = false;
                
                for(ECPeak  peak : peakList){
                    if(peak.addStrip(stripList.get(loop))==true){
                        stripAdded = true;
                    } 
                }
                if(stripAdded==false){
                    ECPeak  newPeak = new ECPeak(stripList.get(loop));
                    peakList.add(newPeak);
                }
            }        
        }
        for(int loop = 0; loop < peakList.size(); loop++){
            peakList.get(loop).setPeakId(loop+1);
        }
        return peakList;
    }
    
    public static Particle  createParticle(ECCluster pcal, ECCluster ecin, ECCluster ecout){
        Line3D  dir = new Line3D(0.0,0.0,0.0,
                pcal.getHitPosition().x(),pcal.getHitPosition().y(),pcal.getHitPosition().z());
        double energy = (pcal.getEnergy()/ECCommon.PCAL_SF + 
                ecin.getEnergy()/ECCommon.ECIN_SF + ecout.getEnergy()/ECCommon.ECOUT_SF);
        double quality = 0;
        
        Line3D  iecin  = dir.distance(ecin.getHitPosition());
        Line3D  iecout = dir.distance(ecout.getHitPosition());
        
        Vector3D uvec = dir.direction();
        uvec.unit();
        Particle part = new Particle(22,uvec.x()*energy, 
                uvec.y()*energy,
                uvec.z()*energy,
                0.0,0.0,0.0);
        part.setProperty("match", (iecin.length() + iecout.length()));
        return part;
    }
    
    public static List<DetectorParticle>  getNeutralParticles(List<ECCluster> clusters){
        List<DetectorParticle> particles =  new ArrayList<DetectorParticle>();
        for(int sector = 0; sector < 6; sector++){
            
            List<ECCluster>  pcalClusters = ECCommon.getClusters(sector, 1, clusters);
            
            for(int cid = 0; cid < pcalClusters.size();cid++){
                
                ECCluster cluster = pcalClusters.get(cid);
                DetectorParticle particle = new DetectorParticle();
                
                
                DetectorResponse  response = new DetectorResponse();
                response.getDescriptor().setType(DetectorType.PCAL);
                response.getDescriptor().setSectorLayerComponent(sector, 1, 0);
                
                response.setPosition(cluster.getHitPosition().x(),
                                     cluster.getHitPosition().y(),
                                     cluster.getHitPosition().z());
                
                response.setEnergy(cluster.getEnergy());
                
                particle.addResponse(response);
                

                Line3D  vLine = new Line3D(0.0,0.0,0.0,cluster.getHitPosition().x(),
                                                       cluster.getHitPosition().y(),
                                                       cluster.getHitPosition().z());
                
                
                
                List<ECCluster>  ecinCL = ECCommon.getClusters(sector, 4, clusters);
                int bestIndex       = -1;
                double bestDistance = 100.0;
                for(int id = 0; id < ecinCL.size();id++){
                    double dist = vLine.distance(ecinCL.get(id).getHitPosition()).length();
                    if(dist<ECCommon.PCAL_EC_MATCHING_CUT&&dist<bestDistance){
                        bestDistance = dist;
                        bestIndex    = id;
                    }
                    //System.out.println(" PARTICLE : CID = " + cid +
                    //        "  ECIN ID = " + id + "   distance = " + dist);
                }
                
                if(bestIndex>=0){
                    DetectorResponse  respECIN = new DetectorResponse();
                    respECIN.getDescriptor().setType(DetectorType.EC);
                    respECIN.getDescriptor().setSectorLayerComponent(sector, 4,0);
                    respECIN.setEnergy(ecinCL.get(bestIndex).getEnergy());
                    respECIN.setPosition(
                            ecinCL.get(bestIndex).getHitPosition().x(),
                            ecinCL.get(bestIndex).getHitPosition().y(),
                            ecinCL.get(bestIndex).getHitPosition().z()                            
                    );
                    particle.addResponse(respECIN);
                }
                
                
                List<ECCluster>  ecoutCL = ECCommon.getClusters(sector, 7, clusters);
                bestIndex       = -1;
                bestDistance = 100.0;
                for(int id = 0; id < ecoutCL.size();id++){
                    double dist = vLine.distance(ecoutCL.get(id).getHitPosition()).length();
                    if(dist<ECCommon.PCAL_EC_MATCHING_CUT&&dist<bestDistance){
                        bestDistance = dist;
                        bestIndex    = id;
                    }
                    //System.out.println(" PARTICLE : CID = " + cid +
                    //        "  ECOUT ID = " + id + "   distance = " + dist);
                }
                
                if(bestIndex>=0){
                    DetectorResponse  respECIN = new DetectorResponse();
                    respECIN.getDescriptor().setType(DetectorType.EC);
                    respECIN.getDescriptor().setSectorLayerComponent(sector, 7,0);
                    respECIN.setEnergy(ecoutCL.get(bestIndex).getEnergy());
                    respECIN.setPosition(
                            ecoutCL.get(bestIndex).getHitPosition().x(),
                            ecoutCL.get(bestIndex).getHitPosition().y(),
                            ecoutCL.get(bestIndex).getHitPosition().z()                            
                    );
                    particle.addResponse(respECIN);
                }
                
                particle.vertex().setXYZ(0.0, 0.0, 0.0);
                particle.vector().setXYZ(cluster.getHitPosition().x(),
                        cluster.getHitPosition().y(),cluster.getHitPosition().z());
            

                Vector3D  vec = vLine.direction();
                vec.unit();
                double energy = 0.0;
                for(DetectorResponse res : particle.getDetectorResponses()){
                    energy += res.getEnergy();
                }
                vec.scale(energy/ECCommon.PCAL_SF);
                particle.vector().setXYZ(vec.x(),vec.y(), vec.z());
                particles.add(particle);
            }
            
        }
        
        for(DetectorParticle part : particles){
            System.out.println(part);
        }
        return particles;
    }
    
    public static List<ECCluster> getClusters(int sector, int stack, List<ECCluster> cl){
        List<ECCluster>  result = new ArrayList<ECCluster>();
        for(ECCluster cluster : cl){
            if(cluster.clusterPeaks.get(0).getDescriptor().getSector()==sector
                    &&cluster.clusterPeaks.get(0).getDescriptor().getLayer()==stack){
                result.add(cluster);
            }
        }
        return result;
    }
}
