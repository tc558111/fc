/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.rec.ec;

import java.util.ArrayList;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;

/**
 *
 * @author gavalian
 */
public class ECPeakCluster {
    private final ArrayList<ECLayerPeak> ecLayerPeaks = new ArrayList<ECLayerPeak>();
    private double energy = 0.0;
    public  Point3D  hitPosition = new Point3D();
    public  Point3D  hitPositionError = new Point3D();    
    public  int  sector = 0;
    public  int  superlayer = 0;
    
    public ECPeakCluster(int _s, int _sl,ECLayerPeak peakU, ECLayerPeak peakV, ECLayerPeak peakW){
        sector = _s;
        superlayer = _sl;
        ecLayerPeaks.add(peakU);
        ecLayerPeaks.add(peakV);
        ecLayerPeaks.add(peakW);
    }
    
    public void redo(){
        Line3D  uline = ecLayerPeaks.get(0).getLine();
        Line3D  vline = ecLayerPeaks.get(1).getLine();
        Line3D  wline = ecLayerPeaks.get(2).getLine();
        Line3D  vwLine = vline.distance(wline);
        Line3D  uvwLine = uline.distance(vwLine.midpoint());
        Point3D hitCoord = uvwLine.midpoint();
        hitPosition.set(hitCoord.x(), hitCoord.y(),hitCoord.z());
        double xdiff = hitPosition.x()-uvwLine.origin().x();
        double ydiff = hitPosition.y()-uvwLine.origin().y();
        double zdiff = hitPosition.z()-uvwLine.origin().z();
        hitPositionError.set(
                Math.sqrt(xdiff*xdiff),
                Math.sqrt(ydiff*ydiff),
                Math.sqrt(zdiff*zdiff)
                );
        
        energy = 0.0;
        energy += ecLayerPeaks.get(0).getEnergy();
        energy += ecLayerPeaks.get(1).getEnergy();
        energy += ecLayerPeaks.get(2).getEnergy();
    }
    
    public double getEnergy(){ return energy;}
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        /*
        str.append(String.format("PEAK CLUSTER S = %d SL = %d Energy = %8.2f POS (%8.2f %8.2f %8.2f) ERR (%8.2f %8.2f %8.2f) \n",
                sector, superlayer,this.getEnergy(),
                hitPosition.x(),hitPosition.y(),hitPosition.z(),
                hitPositionError.x(),hitPositionError.y(),hitPositionError.z()
                ));*/
        str.append(String.format("[%3d %3d %3d %3d ] %8.3f %8.3f ", 
                2,sector,superlayer,0,0.0,this.getEnergy()));
        str.append(String.format("(%8.3f %8.3f %8.3f) (%8.3f %8.3f %8.3f)", 
                hitPosition.x(),hitPosition.y(),hitPosition.z(),
                hitPositionError.x(),hitPositionError.y(),hitPositionError.z()));
        /*
        for(ECLayerPeak peak : ecLayerPeaks){
            //str.append("\t");
            str.append(peak.toString());
            str.append("\n");
        }*/
        return str.toString();
    }
}
