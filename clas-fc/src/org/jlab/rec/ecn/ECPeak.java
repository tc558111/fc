/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.ecn;

import java.util.ArrayList;
import java.util.List;
import org.jlab.clas.detector.DetectorDescriptor;
import org.jlab.clas.detector.DetectorType;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;

/**
 *
 * @author gavalian
 */
public class ECPeak {
    
    private DetectorDescriptor  desc       = new DetectorDescriptor(DetectorType.EC);
    private List<ECStrip>       peakStrips = new ArrayList<ECStrip>();
    private Line3D              peakLine   = new Line3D();
    private int                 indexMaxStrip = -1;
    private int                 peakOrder     = -1;
    
    public ECPeak(ECStrip strip){
        this.desc.setSectorLayerComponent(strip.getDescriptor().getSector(), 
                strip.getDescriptor().getLayer(), 0);
        this.peakStrips.add(strip);
        this.peakLine.copy(strip.getLine());
        this.indexMaxStrip = 0;
    }
    
    public Line3D  getLine(){
        return this.peakLine;
    }
    
    public void setOrder(int order){ this.peakOrder = order; }
    public int  getOrder(){ return this.peakOrder;}
    
    public void setPeakId(int id){
        for(ECStrip strip : this.peakStrips){
            strip.setPeakId(id);
        }
    }
    public double getTime(){
        if(this.indexMaxStrip>0&&this.indexMaxStrip<this.peakStrips.size()-1){
            return this.peakStrips.get(indexMaxStrip).getTime();
        }
            return 0.0;
    }
    public double getEnergy(){
        double energy = 0.0;
        for(ECStrip strip : this.peakStrips){
            energy += strip.getEnergy();
        }
        return energy;
    }
    
    public double getEnergy(Point3D point){
         double energy = 0.0;
        for(ECStrip strip : this.peakStrips){
            energy += strip.getEnergy(point);
        }
        return energy;
    }
    
    
    public DetectorDescriptor getDescriptor(){
        return this.desc;
    }
    
    public boolean  addStrip(ECStrip strip){
        for(ECStrip s : this.peakStrips){
            if(s.isNeighbour(strip)){
                this.peakStrips.add(strip);
                if(strip.getEnergy()>peakStrips.get(indexMaxStrip).getEnergy()){
                    this.indexMaxStrip = this.peakStrips.size()-1;
                    this.peakLine.copy(strip.getLine());
                }
                return true;
            }
        }
        return false;
    }
    
    public int getMultiplicity(){
        return this.peakStrips.size();
    }
    
    public int getCoord(){
        double energy_summ = 0.0;
        double energy_norm = 0.0;
        for(ECStrip strip : this.peakStrips){
            energy_summ += strip.getEnergy();
	    int str = strip.getDescriptor().getComponent() - 1;
	    str = str*8+4;
            energy_norm += strip.getEnergy()*str;
        }        
        return (int) (energy_norm/energy_summ);
    }
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append(String.format("----> peak  ( %3d %3d )  ENERGY = %12.5f\n", 
                this.desc.getSector(),this.desc.getLayer(), this.getEnergy()));
        for(ECStrip strip : this.peakStrips){
            str.append("\t\t");
            str.append(strip.toString());
            str.append("\n");
        }

        return str.toString();
    }
}
