/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.rec.ec;

import java.util.ArrayList;

/**
 *
 * @author gavalian
 */
public class ECLayerCluster {
    private final ArrayList<ECLayerHit>  clusterHits = new ArrayList<ECLayerHit>();
    private final double ADC_TO_ENERGY = 0.1;
    private double position = 0.0;
    private int   largestHitIndex = -1;
    public ECLayerCluster(){
    
    }
    
    public ECLayerCluster(ECLayerHit hit){
        clusterHits.add(hit);
        largestHitIndex = 0;
    }
    
    public boolean isInCluster(ECLayerHit hit){
        for(ECLayerHit components : clusterHits){
            if(components.isNeighbour(hit)==true) return true;
        }
        return false;
    }
    
    public ECLayerHit getHighestHit(){
        return clusterHits.get(largestHitIndex);
    }
    
    public double getEnergy(){
        double energy = 0.0;
        for(ECLayerHit hit : clusterHits){
            energy += hit.ADC*ADC_TO_ENERGY;
        }
        return energy;
    }
    
    public void add(ECLayerHit hit){
        clusterHits.add(hit);
        if(largestHitIndex>=0){
            if(hit.ADC>clusterHits.get(largestHitIndex).ADC)
                largestHitIndex = clusterHits.size() - 1;
        }
    }
    
    public ArrayList<ECLayerHit> getClusterHits(){
        return clusterHits;
    }
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append(String.format("CLUSTER  Energy = %12.5f\n",this.getEnergy()));
        for(ECLayerHit component : clusterHits){
            str.append(component.toString());
            str.append("\n");
        }
        return str.toString();
    }
}
