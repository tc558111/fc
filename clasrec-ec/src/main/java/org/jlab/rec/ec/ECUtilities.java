/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.rec.ec;

import org.jlab.geom.base.Detector;
import org.jlab.geom.base.Layer;
import org.jlab.geom.component.ScintillatorPaddle;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Shape3D;
import org.jlab.geom.prim.Transformation3D;

/**
 *
 * @author gavalian
 */
public class ECUtilities {
    
    public static double getTriangleHit(double up, double vp, double wp, double angle_rad){
        double luv = up - (1.0 - vp);
        double luw = up - (1.0 - wp);
        //System.err.println(" u = " + up + " v = " + vp + " w = " + wp
        //+ " luv = " + luv + " luw = " + luw + " luw = ");
        
        if(luv<0.0||luw<0.0) return 0.0;
        double lvw = 1 - up;
        //System.err.println(" u = " + up + " v = " + vp + " w = " + wp
        //+ " luv = " + luv + " luw = " + luw + " luw = " + lvw);

        double sin_alpha = Math.sin(angle_rad);
        double A   = 0.5*sin_alpha;
        double Ap  = 0.5*(up*up + vp*vp + wp*wp)*sin_alpha;
        double App = 0.5*(luv*luv + luw*luw + lvw*lvw)*sin_alpha;
        if(A!=0){
            return (Ap-App)/A; 
        }
        return 0.0;
    }
    
    public static ECLayerHit getLayerHit(Detector det, int sector, int superlayer, 
            int layer, int component, int ADC, int TDC){
        
        ECLayerHit hit = new ECLayerHit(sector,superlayer,layer,component,ADC,TDC);
        Layer eclayer = det.getSector(sector).getSuperlayer(superlayer).getLayer(layer);
        Shape3D boundary = eclayer.getBoundary();
        Point3D corner   = boundary.face(0).point(0);
        Point3D right    = boundary.face(0).point(1);
        Point3D left     = boundary.face(0).point(2);
        //System.err.println("SURFACE FOR sector " + sector +
        //"  superlayer = " + superlayer + " layer = " + layer );
        //System.err.println(boundary.toString());
        ScintillatorPaddle paddle = (ScintillatorPaddle) eclayer.getComponent(component);
        Line3D paddleLine = paddle.getLine();
        Transformation3D  toCLAS   = det.getSector(sector).getSuperlayer(superlayer).getLayer(layer).getTransformation();
        Transformation3D  fromCLAS = toCLAS.inverse();//det.getSector(sector).getSuperlayer(superlayer).getLayer(layer).getTransformation();
        //fromCLAS.inverse();
        //System.err.println(fromCLAS.toString());
        Line3D paddleLocal = new Line3D(paddleLine);
        
        fromCLAS.apply(paddleLocal);
        paddleLocal.origin().setZ(0.0);
        paddleLocal.end().setZ(0.0);
        toCLAS.apply(paddleLocal); 
        
        hit.hitStrip.set( 
                paddleLocal.origin().x(),
                paddleLocal.origin().y(),
                paddleLocal.origin().z(),
                paddleLocal.end().x(),
                paddleLocal.end().y(),
                paddleLocal.end().z()                
        );
        
        double distance = paddleLine.origin().distance(corner);
        if(layer==1) distance = paddleLine.origin().distance(right);
        if(layer==2) distance = paddleLine.origin().distance(left);
        //if(layer==0){
        //    distance = paddleLine.origin().distance(corner)/corner.distance(edge);
        //}
        hit.distanceOnEdge = distance/corner.distance(left);
        return hit;
    }
    
}
