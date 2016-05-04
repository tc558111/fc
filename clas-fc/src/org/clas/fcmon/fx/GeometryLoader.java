/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.fcmon.fx;

import java.util.List;
import javafx.scene.shape.MeshView;
import org.jlab.clasrec.utils.DataBaseLoader;
import org.jlab.detector.geant4.BSTGeant4Factory;
import org.jlab.detector.geant4.ECGeant4Factory;
import org.jlab.detector.geant4.FTOFGeant4Factory;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.component.ScintillatorMesh;
import org.jlab.geom.detector.ftof.FTOFDetectorMesh;
import org.jlab.geom.detector.ftof.FTOFFactory;
import org.jlab.geom.geant.Geant4Basic;

/**
 *
 * @author gavalian
 */
public class GeometryLoader {
    
        
    public static MeshStore  getGeometry(String name, int sector, int layer){
        MeshStore  store = new MeshStore();
        if(name.compareTo("FTOF")==0){
             ConstantProvider  cp = DataBaseLoader.getConstantsFTOF();
             FTOFGeant4Factory  factory = new FTOFGeant4Factory(cp);
             //Geant4Basic  mVolume    = factory.createSector(cp,sector,layer);
             //store.init(mVolume);
        }
        
        if(name.compareTo("BST")==0){
            ConstantProvider  cp = DataBaseLoader.getConstantsBST();
            BSTGeant4Factory  factory = new BSTGeant4Factory();
            Geant4Basic  mVolume = factory.createDetector(cp);
            store.init(mVolume);
        }
        return store;
    }
    
    public static MeshStore  getGeometry(String name){
        MeshStore  store = new MeshStore();
        if(name.compareTo("BST")==0){
            ConstantProvider  cp = DataBaseLoader.getConstantsBST();
            BSTGeant4Factory  factory = new BSTGeant4Factory();
            Geant4Basic  mVolume = factory.createDetector(cp);
            store.init(mVolume);
        }
        if(name.compareTo("EC")==0){
            ECGeant4Factory  factory = new ECGeant4Factory();
            Geant4Basic mVolume = factory.getPCAL();
            store.init(mVolume);
        }
        return store;
    }
    
    public static MeshStore  getGeometryGemc(){
        MeshStore  store = new MeshStore();
        ConstantProvider  cp = DataBaseLoader.getConstantsFTOF();
        FTOFFactory factory = new FTOFFactory();
        FTOFDetectorMesh detector = factory.getDetectorGeant4(cp);

        int counter = 1;
        for(int sector = 1; sector <= 6; sector++){
            for(int superlayer = 1; superlayer <=3; superlayer++){
                List<ScintillatorMesh>  sci = detector.getSector(sector).getSuperlayer(superlayer).getLayer(1).getAllComponents();
                for(ScintillatorMesh m : sci){                    
                    MeshView mesh = m.getVolumeMesh().getMeshView();
                    
                    //store.getMap().put("sector_" + sector + "_mesh_"+counter, mesh);
                    store.addMesh("FTOF_S_" + sector + "_SL_"+ superlayer + "P_"+counter, 
                            mesh,superlayer+1);
                    counter++;
                }
            }
        }
        return store;
    }
    
}
