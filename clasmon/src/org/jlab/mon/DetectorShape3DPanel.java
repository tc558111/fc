/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.geom.gui;

/*
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ChangeListener;
import java.awt.event.ChangeEvent;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.JSlider;
import java.awt.*;
 */
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 *
 * @author gavalian
 */
public class DetectorShape3DPanel extends JPanel implements MouseListener , MouseMotionListener, ChangeListener {
    
    private DetectorShape3DStore shapeStore = new DetectorShape3DStore();
    public  IDetectorComponentSelection  selectionListener = null;
    public  Boolean MOUSEOVER_CALLBACK = true;
    private DetectorShape3D cui_save = null;
    private DetectorShape3D cui_old = null;
    private JSlider framesPerSecond;
    static final int FPS_MIN = 0;
    static final int FPS_MAX = 20;
    static final int FPS_INIT = 2;

    int delay;
    Timer timer;
    boolean frozen = false;
 
    public DetectorShape3DPanel(){
        super();
        this.setPreferredSize(new Dimension(300,300));
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        
        delay = 1000 / FPS_INIT;
        updateGUIAction action = new updateGUIAction();
        timer = new Timer(delay,action);
        
     // JSlider code copied from SliderDemo.java from Oracle Documentation
        framesPerSecond = new JSlider(JSlider.HORIZONTAL,FPS_MIN,FPS_MAX,FPS_INIT);
        framesPerSecond.addChangeListener(this);
        framesPerSecond.setMajorTickSpacing(10);
        framesPerSecond.setMinorTickSpacing(1);
        framesPerSecond.setPaintTicks(true);
        framesPerSecond.setPaintLabels(true);
        framesPerSecond.setBorder(
                                  BorderFactory.createEmptyBorder(0,0,10,0));
        Font font = new Font("Serif", Font.ITALIC, 15);
        framesPerSecond.setFont(font);
        this.setLayout(new FlowLayout(FlowLayout.LEFT,50,500));
        framesPerSecond.setSize(200,200);
        framesPerSecond.setVisible(true);
        this.add(framesPerSecond);
    }
    
    /** Listen to the slider. */
    public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider)e.getSource();
        if (!source.getValueIsAdjusting()) {
            int fps = (int)source.getValue();
            if (fps == 0) {
                if (!frozen) stop();
            } else {
                delay = 1000 / fps;
                timer.setDelay(delay);
                timer.setInitialDelay(delay * 10);
                if (frozen) start();
            }
        }
    }
    
    public void start(){
        timer.start();
        frozen = false;
    }
    
    public void stop(){
        timer.stop();
        frozen = true;
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
    
    // Action for Timer to update plots without mouse motion
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
            if (cui!=cui_old) { //Don't call if mouse has not moved outside of object
                cui_old = cui;
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
