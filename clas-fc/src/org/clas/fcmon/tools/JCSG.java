package org.clas.fcmon.tools;

import org.jlab.clasrec.utils.DataBaseLoader;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.base.GeometryFactory;
import eu.mihosoft.vrl.v3d.Vector3d;
import org.jlab.detector.geant4.v2.*;
import org.jlab.geometry.utils.*;
import org.jlab.detector.volume.*;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.component.ScintillatorPaddle;
import org.jlab.geom.detector.ec.ECDetector;
import org.jlab.geom.detector.ec.ECFactory;
import org.jlab.geom.detector.ec.ECLayer;
import org.jlab.geom.prim.Point3D;
import org.jlab.clasrec.utils.DataBaseLoader;

public class JCSG {
    
    ConstantProvider    cp1 = GeometryFactory.getConstants(DetectorType.EC,1,"default");
    ConstantProvider    cp2 = GeometryFactory.getConstants(DetectorType.EC,2,"default");
   
    ECDetector    detector = new ECFactory().createDetectorCLAS(cp2);
    String name[]={"PCAL","ECin","ECout"};
    int istrip[]={1,1};
    public JCSG() {};
    
    private void doECVert(int unit) {
        
        ECLayer  ecLayer;
        Point3D point1 = new Point3D();
//        int[] vertices = {0,4,5,1};
        int[] vertices = {4,0,5,1,7,3,6,2};
        int[] numstrips = new int[3];
        double[][][][] xPoint = new double [6][3][68][8];
        double[][][][] yPoint = new double [6][3][68][8];
        int suplay = unit; //PCAL ==0, ECinner ==1, ECouter==2 
        
        System.out.println(cp2.getDouble("/geometry/ec/ec/dist2tgt",0));
        System.out.println("CoatJava "+name[unit]+":");
        
        for(int sector = 0; sector < 1; ++sector) {
            for(int l = 0; l<1; l++) {      
                ecLayer = detector.getSector(sector).getSuperlayer(suplay).getLayer(l);
                numstrips[l] = ecLayer.getNumComponents();
                int n = 1;
                for(ScintillatorPaddle paddle1 : ecLayer.getAllComponents()) {
                    if (n==istrip[unit]) {
                    for(int j=0; j<8 ; j++) {
                        point1.copy(paddle1.getVolumePoint(vertices[j]));
//                        point1.copy(paddle1.getVolumePoint(j));
//                        point1.rotateZ(sector * Math.PI/3.0);
//                        point1.translateXYZ(333.1042, 0.0, 0.0);
                        System.out.println(point1.x()+" "+point1.y()+" "+point1.z());
                        xPoint[sector][l][n][j] =  point1.x();
                        yPoint[sector][l][n][j] = -point1.y(); // why minus sign?
                    }
                    }
                    n++;
                }
            }
        }
    }
    
    private void doJCSGVert() {
        
        G4Trap vol = new G4Trap("vol", 1,0,0,1,1,1,0,1,1,1,0);
        for(int i=0;i<8;i++)
            System.out.println(vol.getVertex(i));
        
        PCALGeant4Factory pcal = new PCALGeant4Factory(cp1);
        ECGeant4Factory     ec = new ECGeant4Factory(cp1);

        System.out.println("JCSG PCAL:");
        G4Trap pcalPadVol = pcal.getPaddle(1,1,istrip[0]);
        for(int i=0;i<8;i++) System.out.println(pcalPadVol.getVertex(i));

        System.out.println("JCSG EC:");
        G4Trap ecPadVol = ec.getPaddle(1,1,istrip[1]);
        for(int i=0;i<8;i++) System.out.println(ecPadVol.getVertex(i));
        
    }
    
    public static void main(String[] args){
    
    JCSG jcsg = new JCSG();
    
    jcsg.doJCSGVert();    
    jcsg.doECVert(0);
    jcsg.doECVert(1);
    
    }
    
}
