package org.jlab.ecmon.utils;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.jlab.geom.gui.DetectorShape3D;
import org.jlab.geom.gui.DetectorShape3DStore;
import org.jlab.geom.gui.IDetectorComponentSelection;
import org.jlab.geom.gui.IDetectorShapeIntensity;

/**
 *
 * @author gavalian
 */
public class DetectorShape3DPanel extends JPanel implements MouseListener , MouseMotionListener {
    
    private DetectorShape3DStore shapeStore = new DetectorShape3DStore();
    public  IDetectorComponentSelection  selectionListener = null;
    public  Boolean MOUSEOVER_CALLBACK = true;
    private DetectorShape3D cui_save = null;
    private DetectorShape3D cui_old = null;

    static final int FPS_INIT = 2;

    int junk;
    int delay;
    Timer timer;
 
    public DetectorShape3DPanel(){
        super();
        this.setPreferredSize(new Dimension(300,300));
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        
        delay = 1000 / FPS_INIT;
        updateGUIAction action = new updateGUIAction();
        timer = new Timer(delay,action);  
    }
    
    public void start(int fps){
    	timer.stop();
    	delay = 1000 / fps;
    	timer.setDelay(delay);
        timer.start();
    }
    
    public void stop(){
        timer.stop();
    }
    
    public DetectorShape3DStore  getStore(){ return this.shapeStore;}
    
    public void setSelectionListener(IDetectorComponentSelection listener){
        this.selectionListener = listener;
    }
    
    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        int xsize = this.getSize().width;
        int ysize = this.getSize().height;
        Graphics2D g2d = (Graphics2D) g;
        this.shapeStore.draw2D(g2d, 0, 0, xsize, ysize);
    }
    
    public void setColorIntensity(IDetectorShapeIntensity intens){
        this.shapeStore.setIntensityMap(intens);
    }
    
    private class updateGUIAction implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            updateGUI();
        }
    }
    
    public void updateGUI(){
        if(cui_save!=null){
        this.repaint();
            if(this.selectionListener!=null){
               this.selectionListener.detectorSelected(cui_save.SECTOR,cui_save.LAYER,cui_save.COMPONENT);
        }
        }
    }
    
    public void mouseClicked(MouseEvent e) {
    
    }

    public void mousePressed(MouseEvent e) {
        
    }

    public void mouseReleased(MouseEvent e) {
        
    }

    public void mouseEntered(MouseEvent e) {
        
    }

    public void mouseExited(MouseEvent e) {
 
    }

    public void mouseDragged(MouseEvent e) {
        
    }
    
    public void mouseMoved(MouseEvent e) {
        
        if(this.MOUSEOVER_CALLBACK==true){
            this.shapeStore.reaset();
            DetectorShape3D cui = this.shapeStore.getSelectedShape(e.getX(),e.getY(),
                                  this.getSize().width, this.getSize().height);
            if (cui!=cui_old) {
                cui_old = cui;
                junk = 33;
                this.repaint();
                if(cui!=null){
                    if(this.selectionListener!=null){
                       this.selectionListener.detectorSelected(cui.SECTOR,cui.LAYER,cui.COMPONENT);
                    cui_save = cui;
                    }
                }
            }
        }
    }
    
}
