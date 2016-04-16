/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.rec.ec;

import java.util.ArrayList;
import org.jlab.geom.prim.Line3D;

/**
 *
 * @author gavalian
 */
public class ECLayerPeak {
    
    private final ArrayList<ECLayerHit>  peakHits = new ArrayList<ECLayerHit>();
    public int   sector = 0;
    public int   superlayer = 0;
    public int   layer      = 0;
    
    private double energy    = 0.0;
    private double time      = 0.0;
    private double width     = 0.0;
    private Line3D  peakLine = new Line3D();
        
    public ECLayerPeak(ECLayerHit hit){
        sector = hit.sector;
        superlayer = hit.superlayer;
        layer = hit.layer;
        peakHits.add(hit);
    }
    
    public boolean isInCluster(ECLayerHit hit){
        for(ECLayerHit components : peakHits){
            if(components.isNeighbour(hit)==true) return true;
        }
        return false;
    }
    
    public void add(ECLayerHit hit){
        peakHits.add(hit);
    }
    
    public void redo(){
        double summ_logw  = 0.0;
        double summ_log_xo = 0.0;
        double summ_log_yo = 0.0;
        double summ_log_zo = 0.0;
        double summ_log_xe = 0.0;
        double summ_log_ye = 0.0;
        double summ_log_ze = 0.0;
        double totalEnergy = 0.0;
        for(int loop = 0; loop < peakHits.size(); loop++){
            totalEnergy += peakHits.get(loop).energy;
            double log_e = Math.log(peakHits.get(loop).energy);
            summ_log_xo += log_e*peakHits.get(loop).hitStrip.origin().x();
            summ_log_yo += log_e*peakHits.get(loop).hitStrip.origin().y();
            summ_log_zo += log_e*peakHits.get(loop).hitStrip.origin().z();
            summ_log_xe += log_e*peakHits.get(loop).hitStrip.end().x();
            summ_log_ye += log_e*peakHits.get(loop).hitStrip.end().y();
            summ_log_ze += log_e*peakHits.get(loop).hitStrip.end().z();
            summ_logw   += log_e;
        }
        
        this.energy = totalEnergy;
        peakLine.set(
                summ_log_xo/summ_logw, 
                summ_log_yo/summ_logw,
                summ_log_zo/summ_logw,
                summ_log_xe/summ_logw, 
                summ_log_ye/summ_logw,
                summ_log_ze/summ_logw
                );
    }
    
    public double getWidth(){
        return width;
    }
    
    public double getEnergy(){
        return this.energy;
    }
    
    public double getTime(){
        return this.time;
    }
    
    public int getHashCode(){
        return (1000*sector + 100*superlayer + layer);
    }
    
    public Line3D getLine(){
        return peakLine;        
    }
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append(String.format("PEAK (hash=%9d) Energy = %12.3f (%8.2f %8.2f %8.2f) (%8.2f %8.2f %8.2f)\n",
                this.getHashCode(),
                this.getEnergy(),
                this.getLine().origin().x(),this.getLine().origin().y(),this.getLine().origin().z(),
                this.getLine().end().x(),this.getLine().end().y(),this.getLine().end().z()
                ));
        
        for(ECLayerHit component : peakHits){
            str.append("\t");
            str.append(component.toString());
            str.append("\n");
        }
        return str.toString();
    }
}
