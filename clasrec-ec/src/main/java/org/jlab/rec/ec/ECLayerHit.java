/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.rec.ec;

import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;

/**
 *
 * @author gavalian
 */
public class ECLayerHit implements Comparable {
    
    public int sector = 0;
    public int superlayer = 0;
    public int layer = 0;
    public int strip = 0;
    public int ADC = 0;
    public int TDC = 0;
    public double energy = 0.0;
    public double time   = 0.0;
    public Line3D  hitStrip = new Line3D();
    public double  distanceOnEdge;
    public Point3D xyzPosition = new Point3D();
    public double edgeDistance = 0.0;
    
    public ECLayerHit(int _sec, int _supl, int _l, int _comp, int _adc, int _tdc){
        sector = _sec;
        superlayer = _supl;
        layer = _l;
        strip = _comp;
        ADC = _adc;
        TDC = _tdc;
        energy = ((double) ADC)/10.0;
    }
    
    @Override
    public int compareTo(Object o) {
        ECLayerHit ob = (ECLayerHit) o;
        if(ob.sector < this.sector) return 1;
        if(ob.sector > this.sector) return -1;
        if(ob.superlayer < this.superlayer) return  1;
        if(ob.superlayer > this.superlayer) return -1;
        if(ob.layer < this.layer) return  1;
        if(ob.layer > this.layer) return -1;
        if(ob.strip<this.strip) return  1;
        if(ob.strip==this.strip) return 0;
        return -1;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append(String.format("ECL : %4d %4d %4d %4d %8d %8d %12.5f %12.5f %12.5f", 
                sector,superlayer,layer,strip,
                ADC,TDC,
                energy,time, distanceOnEdge
                ));
        str.append(String.format("   ( %12.5f %12.5f  %12.5f )",
                hitStrip.origin().x(),hitStrip.origin().y(),hitStrip.origin().z()));
        return str.toString();
    }
    
    public boolean isNeighbour(ECLayerHit hit){
        if(hit.sector==this.sector&&hit.superlayer==this.superlayer
                &&hit.layer==this.layer){
            if(Math.abs(hit.strip-this.strip)<=1) return true;
        }
        return false;
    }
    
    public Integer getHashCode(){
        return layer*10 + superlayer*100 + sector*1000;
    }
    

    public void setPosition(double x, double y, double z){
        xyzPosition.set(x, y, z);
    }
    
    public Point3D getPosition(){
        return xyzPosition;
    }
    
    public void setEdgeDistance(double dist){
        edgeDistance = dist;
    }
    
    public double getEdgeDistance(){
        return edgeDistance;
    }
}
