/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;

import org.jlab.clas12.calib.IDetectorListener;
import org.jlab.detector.base.DetectorDescriptor;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.decode.DetectorDataDgtz;
import org.jlab.groot.base.ColorPalette;
import org.jlab.groot.graphics.GraphicsAxis;
import org.jlab.groot.math.Dimension1D;
import org.jlab.groot.math.Dimension2D;
import org.jlab.utils.groups.IndexedList;

/**
 *
 * @author gavalian
 * Modified by lcsmith (July 2016)
 */
public class DetectorView2D extends JPanel implements MouseMotionListener {
    
    Map<String,DetectorViewLayer2D>  viewLayers = new LinkedHashMap<String,DetectorViewLayer2D>();
    Dimension2D                      viewBounds = new Dimension2D();
    List<String>                 viewLayerNames = new ArrayList<String>();
    ViewWorld                             world = new ViewWorld();
    DetectorShape2D                 activeShape = null;
    Color                       backgroundColor = Color.GRAY;
    GraphicsAxis                    colorAxis   = new GraphicsAxis();
    List<DetectorListener>    detectorListeners = new ArrayList<DetectorListener>();
    
    // lcs: Don't paint unless new shape entered
    int selectedShape = 1;
    int selectedShapeSave = -1;
    
    private boolean       isMouseMotionListener = true;
    
    public DetectorView2D(){        
        super();
        this.setSize(new Dimension(500,500));
        this.setMinimumSize(new Dimension(200,200));
        addListeners();
    }
    
    private void addListeners(){
        if(this.isMouseMotionListener==true){
            this.addMouseMotionListener(this);
        }
    }
    
    public void addDetectorListener(DetectorListener lt){
        this.detectorListeners.add(lt);
    }  
    
    public void fill(List<DetectorDataDgtz> data, String options){
        for(Map.Entry<String,DetectorViewLayer2D> entry : this.viewLayers.entrySet()){
            entry.getValue().fill(data, options);
        }
    }
    
    @Override
    public void paint(Graphics g){ 

//        Long st = System.currentTimeMillis();
        Graphics2D g2d = (Graphics2D) g;
//        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  lcs: Too slow (~130 ms for PCAL)
//                             RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                             RenderingHints.VALUE_TEXT_ANTIALIAS_ON);      
        
        int w = this.getSize().width;
        int h = this.getSize().height;
        
        g2d.setColor(backgroundColor);
        g2d.fillRect(0, 0, w, h);
        
        this.viewBounds.getDimension(0).setMinMax(0, w);
        this.viewBounds.getDimension(1).setMinMax(0, h);
        //g2d.setColor(Color.red);
        //g2d.drawRect(10,10,w-20,h-20);
       // long startTime = System.currentTimeMillis();
        this.drawLayers(g2d);
        //System.out.println("initDetector() time= "+(System.currentTimeMillis()-startTime));
        this.colorAxis.setVertical(true);
        this.colorAxis.setAxisType(GraphicsAxis.AXISTYPE_COLOR);
        this.colorAxis.setDimension(h-20,h-120);
        this.colorAxis.setRange(0.0, 10.0);
        this.colorAxis.drawAxis(g2d, 10, h-20);
        
    }
    
    public List<String> getLayerNames(){ return this.viewLayerNames;}
    
    public void drawLayers(Graphics2D g2d){
        
        int w = this.getSize().width;
        int h = this.getSize().height;
        
        Dimension2D  commonDimension = new Dimension2D();        
        
        int n = 0; //lcs: set dimension for first view only
        
        for(Map.Entry<String,DetectorViewLayer2D> entry : this.viewLayers.entrySet()){
           // System.out.println("[Drawing] ---> layer : " + entry.getKey() + " " + 
           //                                                entry.getValue().getBounds());
            if (n==0) {
            commonDimension.copy(entry.getValue().getBounds());
            commonDimension.getDimension(0).addPadding(0.1);
            commonDimension.getDimension(1).addPadding(0.1);
            
            //ViewWorld  world = new ViewWorld();
            world.setWorld(viewBounds);
            world.setView(commonDimension);
            n++;
            }
            
            //world.show();
            if(entry.getValue().isActive()==true){
                entry.getValue().drawLayer(g2d, world);
            }
        }
    }
    
    public void changeBackground(Color bkg){
        System.out.println("background color change ");
        this.backgroundColor = bkg;
    }
    
    public void removeLayer(String name){
        if(this.viewLayers.containsKey(name)==true){
            this.viewLayers.remove(name);
            this.viewLayerNames.remove(name);
        }
    }
    
    public void addLayer(String name){
        if(this.viewLayers.containsKey(name)==true){
            
        } else {
            this.viewLayers.put(name, new DetectorViewLayer2D());
            this.viewLayerNames.add(name);
        }
    }
    
    public void addShape(String layer, DetectorShape2D shape){
        if(this.viewLayers.containsKey(layer)==false){
            addLayer(layer);
        }        
        this.viewLayers.get(layer).addShape(shape);
    }

    public boolean isLayerActive(String layer){
        return this.viewLayers.get(layer).isActive();
    }
    
    public void setHitMap(boolean flag){
        for(String layer : this.viewLayerNames){
            this.viewLayers.get(layer).setShowHitMap(flag);
        }
    }
    
    public void  setLayerActive(String layer, boolean flag){
        viewLayers.get(layer).setActive(flag);
    }
    
    public void setDetectorListener(String layer, DetectorListener dl) {
        viewLayers.get(layer).addDetectorListener(dl);
    }  
    
    @Override
    public void mouseDragged(MouseEvent e) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        //System.out.println("mouse moved = " + e.getX() + " " + e.getY());
        int index = -1;
        if(this.isMouseMotionListener==true) {
            double x = world.getViewX(e.getX());
            double y = world.getViewY(e.getY());
            DetectorShape2D selection = null;
            for(String layer : this.viewLayerNames){
                if(viewLayers.get(layer).isActive()==true){
                    this.viewLayers.get(layer).resetSelection();
                    selection = this.viewLayers.get(layer).getShapeByXY( x,y);
                    if(selection!=null) {
                        index = selection.hashCode();
                        this.viewLayers.get(layer).setSelected(selection);
                        break;
                    }
                }
            } 

            if(selection!=null&&index!=this.selectedShapeSave){
                if(activeShape!=null){
                    //System.out.println(" compare = " + activeShape.getDescriptor().compare(selection.getDescriptor()));
                    //System.out.println(" active shape = " + selection.getDescriptor());
                }
               // System.out.println(" SELECTION = " + selection.getDescriptor());
                this.selectedShape = index;
                this.selectedShapeSave = index;
                activeShape = selection;
                for(DetectorListener lt : this.detectorListeners) lt.processShape(activeShape);
                repaint();
           }
        }
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    /**
     * Layer class to keep shapes in. it computes it's boundaries automatically;
     */
    public static class DetectorViewLayer2D {
        
        private IndexedList<DetectorShape2D>   shapes = null;
        private String                      layerName = "Layer";
        private Dimension2D                boundaries = new Dimension2D(); 
        private int                      layerOpacity = 255;
        private DetectorDescriptor selectedDescriptor = new DetectorDescriptor();
        private boolean                 isLayerActive = true;
        private Dimension1D                 axisRange = new Dimension1D();
        private boolean                    showHitMap = false;
        private int                           opacity = 255;
        private ColorPalette                  palette = new ColorPalette();
        
        List<DetectorListener>    detectorListeners = new ArrayList<DetectorListener>();
        
        public DetectorViewLayer2D() {
            shapes = new IndexedList<DetectorShape2D>(4);
        }
        
        public void addDetectorListener(DetectorListener lt){
            this.detectorListeners.add(lt);
        }
        
        /**
         * adding a shape to the layer.
         * @param shape
         * @return 
         */
        public DetectorViewLayer2D addShape(DetectorShape2D shape){
            
            int  type       = shape.getDescriptor().getType().getDetectorId();
            int  sector     = shape.getDescriptor().getSector();
            int  layer      = shape.getDescriptor().getLayer();
            int  component  = shape.getDescriptor().getComponent();
            
            if(shapes.getMap().isEmpty()){
                boundaries.set(
                        shape.getShapePath().point(0).x(),
                        shape.getShapePath().point(0).x(),
                        shape.getShapePath().point(0).y(),
                        shape.getShapePath().point(0).y()
                        );
            }
            
            int npoints = shape.getShapePath().size();
            for(int i = 0; i < npoints; i++){
                boundaries.grow(
                        shape.getShapePath().point(i).x(),
                        shape.getShapePath().point(i).y()
                );
            }
            //boundaries.getDimension(0).addPadding(0.1);
            //boundaries.getDimension(1).addPadding(0.1);
            
            this.shapes.add(shape, type,sector,layer,component);
            return this;
        }
           
        public int                  getOpacity()                {return opacity;}
        public boolean              isActive()                  {return this.isLayerActive;}
        public DetectorViewLayer2D  setActive(boolean flag)     {isLayerActive = flag;return this;}
        public DetectorViewLayer2D  setOpacity(int op)          {this.opacity = op;return this;}
        public DetectorViewLayer2D  setShowHitMap(boolean flag) {this.showHitMap = flag;return this;}      
        public String               getName()                   {return this.layerName;}
        
        public final DetectorViewLayer2D setName(String name){
            this.layerName = name;
            return this;
        }
        
        public void setSelected(DetectorShape2D shape){
            this.selectedDescriptor.copy(shape.getDescriptor());
        }
        
        public void resetSelection(){
            this.selectedDescriptor.setCrateSlotChannel(0, 0, 0);
            this.selectedDescriptor.setSectorLayerComponent(0, 0, 0);
            this.selectedDescriptor.setType(DetectorType.UNDEFINED);
        }
        
        public DetectorShape2D  getShapeByXY(double x, double y){
            for(Map.Entry<Long,DetectorShape2D>  shape : shapes.getMap().entrySet()){
                if(shape.getValue().isContained(x, y)==true) return shape.getValue();
            }
            return null;
        }
        
        public Dimension1D  getAxisRange(){
            int counter = 0;
            for(Map.Entry<Long,DetectorShape2D>  shape : shapes.getMap().entrySet()){
                if(counter==0) axisRange.setMinMax(shape.getValue().getCounter(), shape.getValue().getCounter());
                axisRange.grow(shape.getValue().getCounter());
                //if(shape.getValue().isContained(x, y)==true) return shape.getValue();
            }
            return this.axisRange;
        }
        
        public Dimension2D  getBounds(){
            return this.boundaries;
        }
        /**
         * updating the detector shapes with the data from detector Bank.
         * @param detectorData
         * @param options 
         */
        public void fill(List<DetectorDataDgtz> detectorData, String options){            
            boolean doReset = true;
            if(options.contains("same")==true) doReset = false;
            for(Map.Entry<Long,DetectorShape2D>  shape : shapes.getMap().entrySet()){
                if(doReset==true){ shape.getValue().reset(); }                
                DetectorDescriptor dm = shape.getValue().getDescriptor();
                
                for(int d = 0 ; d < detectorData.size(); d++){                     
                    DetectorDescriptor dd = detectorData.get(d).getDescriptor();
                    if(dd.getType()==dm.getType()&dd.getSector()==dm.getSector()&
                            dd.getLayer()==dm.getLayer()&dd.getComponent()==dm.getComponent()
                            ){                            
                        //System.out.println("COLORING COMPONENT " + shape.getValue().getDescriptor());
                        int cv = shape.getValue().getCounter();
                        shape.getValue().setCounter(cv+1);
                    }
                }
            }
        }
        
        public void drawLayer(Graphics2D g2d, ViewWorld world){
            //System.out.println(" WORLD      = " + d2d);
            //System.out.println(" Layer Boundaries = " + this.boundaries);
            
            //if(this.showHitMap==true){
            //    this.getAxisRange();
            //}
            int counterZero = 0;
            int counterOne  = 0;
            
            for(Map.Entry<Long,DetectorShape2D> entry : this.shapes.getMap().entrySet()){
                DetectorShape2D shape = entry.getValue();
                for(DetectorListener lt : this.detectorListeners) lt.update(shape);
                Color shapeColor = shape.getSwingColorWithAlpha(this.opacity);
                
                //System.out.println(" drawing shape ----> " + entry.getKey());
                //double x = world.getPointX(shape.getShapePath().point(0).x());
                //double y = world.getPointY(shape.getShapePath().point(0).y());
                //g2d.drawOval( (int) x, (int) y,5,5);
                
                if(this.showHitMap==true){
                    if(shape.getCounter()>0){
                        counterOne++;
                    } else {
                        counterZero++;
                    }
                    //Color mapColor = ;//ColorPalette.gaxisRange.getMax();
                    shapeColor = palette.getColor3D(shape.getCounter(),axisRange.getMax(), false);
                    
                    //System.out.println(" AXIS MAX = " + axisRange.getMax() + "  VALUE = " + shape.getCounter());
                }

                if(this.selectedDescriptor.compare(shape.getDescriptor())==true){
                    shape.drawShape(g2d, world, Color.red, Color.black);
                } else {                 
                    shape.drawShape(g2d, world, shapeColor, Color.black);                        
                }
                
                //if(this.showHitMap==true){
                   // System.out.println("Counters Zero = " + counterZero + "  One = "
                   //         + counterOne);
                //}
            }
        }
        
    }
}
