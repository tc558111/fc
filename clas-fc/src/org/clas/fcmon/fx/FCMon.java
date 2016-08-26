/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.fcmon.fx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import org.clas.fcmon.tools.PCPixels;
import org.jlab.clas.detector.DetectorType;
import org.jlab.clas12.calib.DetectorShape2D;
import org.jlab.geom.fx.*;
import org.jlab.geom.prim.Path3D;
import org.jlab.geom.prim.Point3D;
 

/**
 *
 * @author gavalian
 */
public class FCMon extends Application {
    
    DetectorMesh2DCanvas  canvas = null;
    PCPixels pcPix;
    int bcolor = 120;
;    @Override
    public void start(Stage primaryStage) throws Exception {
        BorderPane  root = new BorderPane();
        canvas = new DetectorMesh2DCanvas("FC");
        this.addShapes();
        DetectorTabView view = new DetectorTabView(1250,1250);
        view.addView(canvas);
        root.setCenter(view);
        
        
        Scene scene = new Scene(root, 250, 250, Color.rgb(255,255,255));
        
//scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> canvas.keyPressed());
        //canvas.widthProperty().bind(scene.widthProperty());
        //canvas.heightProperty().bind(scene.heightProperty());
        // create a canvas node
        
        primaryStage.setScene(scene);
        primaryStage.show();
    }

	public DetectorMesh2D getPixel(int sector, int layer, int pixel) {
		DetectorMesh2D mesh = new DetectorMesh2D();	
		mesh = new DetectorMesh2D(1,sector,layer,pixel);	          
		Path3D shapePath = mesh.getPath();    
		float pixmax = pcPix.pixels.getNumPixels();
		for(int j = 0; j < pcPix.pc_nvrt[pixel]; j++){
			shapePath.addPoint(pcPix.pc_xpix[j][pixel][sector],pcPix.pc_ypix[j][pixel][sector],0.0);
		} 
		mesh.setFillColor((int)(150*pixel/pixmax),bcolor,(int)(255*pixel/pixmax));
		return mesh;
	}  
	
    public void addShapes(){
    	
    	pcPix = new PCPixels("PCAL"); bcolor=200;
        DetectorMesh2DLayer layer1 = new DetectorMesh2DLayer("PCAL");
		for(int ip=0; ip<pcPix.pixels.getNumPixels() ; ip++)  layer1.addMesh(getPixel(1,4,ip));
        
		pcPix = new PCPixels("ECin"); bcolor=240;
        DetectorMesh2DLayer layer2 = new DetectorMesh2DLayer("EC");
		for(int ip=0; ip<pcPix.pixels.getNumPixels() ; ip++)  layer2.addMesh(getPixel(1,4,ip));

        this.canvas.addLayer(layer2);
        this.canvas.addLayer(layer1);

        
        this.canvas.setOnSelect(new DetectorEventHandler<DetectorMesh2D>(){
            @Override
            public void handle(DetectorMesh2D event) {
                System.out.print(" guess what a click was detected --> " );
                event.show();
            }
        });
    }
    
    public static void main(String[] args){
        launch();
    }
}
