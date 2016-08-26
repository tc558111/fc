/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.fcmon.fx;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import org.jlab.containers.HashCollection;
import org.jlab.geom.geant.Geant4Basic;
import org.jlab.geom.geant.Geant4Mesh;

/**
 *
 * @author gavalian
 */
public class MeshStore {
    
    String    storeName  = "";
    MeshView  motherMesh = null;
    
    Map<String,MeshView>  meshStore = new TreeMap<String,MeshView>();
    HashCollection<MeshView>  meshCollection = new HashCollection<MeshView>();
    
    private   Boolean     isVisible = true;
    private  Map<Integer,Material>   materials = new HashMap<Integer,Material>();
    
    public MeshStore(){
        
        PhongMaterial matb = new PhongMaterial();
        matb.setDiffuseColor(new Color(0.2,0.2,0.9,1.0));
        matb.setSpecularColor(new Color(0.2,0.2,0.9,1.0));
        materials.put(4, matb);
        PhongMaterial matr = new PhongMaterial();
        matr.setDiffuseColor(new Color(0.9,0.2,0.2,1.0));
        matr.setSpecularColor(new Color(0.9,0.2,0.2,1.0));
        materials.put(2, matr);
        PhongMaterial matg = new PhongMaterial();
        matg.setDiffuseColor(new Color(0.2,0.9,0.2,1.0));
        matg.setSpecularColor(new Color(0.2,0.9,0.2,1.0));
        materials.put(3, matg);
        PhongMaterial matbt = new PhongMaterial();
        matbt.setDiffuseColor(new Color(0.2,0.2,0.9,0.1));
        matbt.setSpecularColor(new Color(0.2,0.2,0.9,0.1));
        materials.put(14, matbt);
        PhongMaterial matrt = new PhongMaterial();
        matrt.setDiffuseColor(new Color(0.9,0.2,0.2,0.1));
        matrt.setSpecularColor(new Color(0.9,0.2,0.2,0.1));
        materials.put(12, matrt);
        PhongMaterial matgt = new PhongMaterial();
        matgt.setDiffuseColor(new Color(0.2,0.9,0.2,0.1));
        matgt.setSpecularColor(new Color(0.2,0.9,0.2,0.1));
        materials.put(13, matgt);
        
    }
    
    public String getName(){
        return this.storeName;
    }
    
    
    public Map<Integer,Material> getMaterials(){
        return this.materials;
    }
    
    public void init(Geant4Basic volume){
        this.storeName = volume.getName();
        List<MeshView>  meshes = Geant4Mesh.getMesh(volume);
        this.meshStore.clear();
        int counter = 0;
        for(MeshView mesh : meshes){
            this.meshStore.put("mesh_" + counter, mesh);
            counter++;
        }
    }
    
    public Map<String,MeshView>  getMap(){return this.meshStore;}
    
    public void addMesh(String name, MeshView view, int color){
        view.setMaterial(this.materials.get(color));
        this.meshStore.put(name, view);
    }
    
    public void setVisible(Boolean flag){
        for(Map.Entry<String,MeshView> entry : this.meshStore.entrySet()){
            entry.getValue().setVisible(flag);
        }
    }
    
    public TreeItem  getMeshTree(){
        //TreeItem<String>  rootItem = new TreeItem<String>(this.storeName);
        CheckBoxTreeItem<String> rootItem = new CheckBoxTreeItem<String>(this.storeName);
        rootItem.setIndependent(true);
        rootItem.setSelected(true);
        rootItem.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                //System.out.println("Things change " + newValue);
                setVisible(newValue);
            }

            });
        for(Map.Entry<String,MeshView> entry : this.meshStore.entrySet()){
            
            TreeItem<String>  item = new TreeItem<String>(entry.getKey());
            rootItem.getChildren().add(item);
        }
        return rootItem;
    }
    
    
    public void setMaterial(double red, double green , double blue, double alpha){
        PhongMaterial mat = new PhongMaterial();
        mat.setDiffuseColor(new Color(red,green,blue,alpha));
        mat.setSpecularColor(new Color(red*0.5,green*0.5,blue*0.5,alpha));
        mat.setSpecularPower(0.5);
        for(Map.Entry<String,MeshView> entry : this.meshStore.entrySet()){
            entry.getValue().setMaterial(mat);
        }
    }
}
