/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.ecmon.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import org.jlab.clas.detector.DetectorDescriptor;
import org.jlab.clas.detector.DetectorType;
import org.jlab.clas12.calib.DetectorShape2D;
//import org.jlab.clas12.calib.DetectorShapeView2D;

import org.jlab.evio.clas12.EvioDataEvent;
//import org.root.pad.EmbeddedCanvas;
import org.root.basic.EmbeddedCanvas;
/**
 *
 * @author gavalian
 */
public abstract class DetectorCalibration {
    
    DetectorType  type = DetectorType.UNDEFINED;
    String        moduleName    = "UNKNOWN";
    String        moduleAuthor  = "UNKNOWN";
    String        moduleVersion = "UNKNOWN";
    
    TreeMap<String, DetectorShapeView2D >  detectors = new TreeMap<String, 
            DetectorShapeView2D>();
    
    public DetectorCalibration(String name, String author, String version){
        this.moduleName = name;
        this.moduleAuthor = author;
        this.moduleVersion = version;
        //this.initDummyGeometry();
    }
    
    public  void setType(DetectorType t){
        this.type = t;
    }
    
    public  void initGeometry(DetectorType type){
        if(type==DetectorType.CTOF){
           this.initGeometryCTOF();
        }
    }
    
    public abstract void processEvent(EvioDataEvent event);

    public void processCalibration(){
        System.out.println("[processCalibration] THIS IS A METHOD FROM SUPER CLASS and DOES NOTHING");
    }
    
    public void writeOutput(){
        System.out.println("[writeOutput] THIS IS A METHOD FROM SUPER CLASS and DOES NOTHING");
    }
    
    
    public  void  updateView(EvioDataEvent event){
        
    }
    
    public  void  update(DetectorShape2D  shape){
        //System.out.println("UPDATING SHAPE COLORS");
        if(shape.getDescriptor().getComponent()%2==0){
            shape.setColor(100,200,100);
        } else {
            shape.setColor(100,100,200);
        }
    }
    
    public String[] getOptions(){
        return new String[]{"default","ocupancy","gain","fits"};
    }
    
    private void initGeometryCTOF(){
        
        for(int loop = 0 ; loop < 48; loop++){
                DetectorShape2D shape = new DetectorShape2D();
                double length = 20.0;
                shape.createBarXY(20, length);
                shape.getShapePath().translateXYZ(220.0, 0.0, 0.0);
                shape.getShapePath().rotateZ(loop*Math.toRadians(-360.0/(48.0)));
                //System.out.println("COMPONENT # " + loop);
                //System.out.println(shape.getShapePath());
                shape.getDescriptor().setType(org.jlab.clas.detector.DetectorType.CTOF);
                shape.getDescriptor().setSectorLayerComponent(0,0,loop);
                this.addShape("CTOF", shape);
        }
    }
    
    public void init(){
        
    }
    
    public void initGeometryFTOF(){
        int[] paddles   = new int[]{65,23,5};
        double[] minLength = new double[]{23.0,34.0,40.0};
        String[] names     = new String[]{"FTOF1A","FTOF1B","FTOF2"};
    }
    
    private void initDummyGeometry(){

            for(int loop = 0 ; loop < 48; loop++){
                DetectorShape2D shape = new DetectorShape2D();
                double length = 20.0;
                shape.createBarXY(20, length);
                shape.getShapePath().translateXYZ(220.0, 0.0, 0.0);
                shape.getShapePath().rotateZ(loop*Math.toRadians(-360.0/(48.0)));
                //System.out.println("COMPONENT # " + loop);
                //System.out.println(shape.getShapePath());
                shape.getDescriptor().setType(org.jlab.clas.detector.DetectorType.CTOF);
                shape.getDescriptor().setSectorLayerComponent(0,0,loop);
                this.addShape("CTOF", shape);
            }
            
    }
    
    public void addShape(String name, DetectorShape2D shape){
        if(detectors.containsKey(name)==false){
            detectors.put(name, new DetectorShapeView2D(shape.getDescriptor().getType().getName()));
        }
        detectors.get(name).addShape(shape);
    }
    
    public TreeMap<String, DetectorShapeView2D> getDetectorList(){
        return this.detectors;
    }
    
    public List<DetectorShapeView2D> getDetectorShapes(){
        List<DetectorShapeView2D>  detectorList = new ArrayList<DetectorShapeView2D>();
        
        if(this.type==DetectorType.FTOF){
            DetectorShapeView2D  view1A = new DetectorShapeView2D("FTOF1A");
            for(int sector = 0; sector < 6; sector++){
                for(int paddle = 0; paddle < 23; paddle++){
                    DetectorShape2D  shape = new DetectorShape2D();
                    shape.getDescriptor().setType(DetectorType.FTOF1A);
                    shape.getDescriptor().setSectorLayerComponent(sector, 0, paddle);
                    shape.createBarXY(18, 80 + paddle*20);
                    shape.getShapePath().translateXYZ(120+20*paddle, 0, 0);
                    shape.getShapePath().rotateZ(Math.toRadians(sector*60.0));
                    if(paddle%2==0){
                        shape.setColor(50, 200, 100);
                    } else {
                        shape.setColor(50, 100, 200);
                    }
                    view1A.addShape(shape);
                }
            }
            detectorList.add(view1A);
            DetectorShapeView2D  view1B = new DetectorShapeView2D("FTOF1B");
            for(int sector = 0; sector < 6; sector++){
                for(int paddle = 0; paddle < 64; paddle++){
                    DetectorShape2D  shape = new DetectorShape2D();
                    shape.getDescriptor().setType(DetectorType.FTOF1B);
                    shape.getDescriptor().setSectorLayerComponent(sector, 1, paddle);
                    shape.createBarXY(18, 80 + paddle*20);
                    shape.getShapePath().translateXYZ(120+20*paddle, 0, 0);
                    shape.getShapePath().rotateZ(Math.toRadians(sector*60.0));
                    if(paddle%2==0){
                        shape.setColor(50, 200, 100);
                    } else {
                        shape.setColor(50, 100, 200);
                    }
                    view1B.addShape(shape);
                }
            }
            detectorList.add(view1B);
            DetectorShapeView2D  view2 = new DetectorShapeView2D("FTOF2");
            for(int sector = 0; sector < 6; sector++){
                for(int paddle = 0; paddle < 5; paddle++){
                    DetectorShape2D  shape = new DetectorShape2D();
                    shape.getDescriptor().setType(DetectorType.FTOF2);
                    shape.getDescriptor().setSectorLayerComponent(sector, 2, paddle);
                    shape.createBarXY(18, 80 + paddle*20);
                    shape.getShapePath().translateXYZ(120+20*paddle, 0, 0);
                    shape.getShapePath().rotateZ(Math.toRadians(sector*60.0));
                    if(paddle%2==0){
                        shape.setColor(50, 200, 100);
                    } else {
                        shape.setColor(50, 100, 200);
                    }
                    view2.addShape(shape);
                }
            }
            detectorList.add(view2);
            
        }
        
        return detectorList;
    }
    
    public void draw(EmbeddedCanvas canvas, DetectorDescriptor desc, String option){
        System.out.println("DRAWING ON CANVAS : " + desc.toString() );
        System.out.println("\t OPTIONS =      : " + option );
        canvas.divide(2, 2);
    }
}
