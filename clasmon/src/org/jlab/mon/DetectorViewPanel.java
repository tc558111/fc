/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.geom.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
/**
 *
 * @author gavalian
 * Revised by L. C. Smith in Sep-Nov 2015 for development of ECMon.java (now ECMonv1.java)
 */
public class DetectorViewPanel extends JPanel implements ChangeListener{
    
    private		JTabbedPane tabbedPane;
    private DetectorShape3DPanel panel1=null;
    private DetectorShape3DPanel panel2=null;
    
    public DetectorViewPanel(){
        super();
        //this.setPreferredSize(new Dimension(600,600));
        this.setLayout(new BorderLayout());
        this.initComponents();
    }
    
    private void initComponents(){
        tabbedPane = new JTabbedPane();
        this.add(tabbedPane,BorderLayout.CENTER);
        tabbedPane.addChangeListener(this);
    }
    
    public void stateChanged(ChangeEvent e) {
        JTabbedPane tabbedPane = (JTabbedPane) e.getSource();
        panel1 = (DetectorShape3DPanel) tabbedPane.getSelectedComponent();
        panel1.start();
        if (panel2!=null) panel2.stop();
        panel2=panel1;
    }
    
    public void addDetectorLayer(String name, DetectorLayerPanel panel){
        tabbedPane.addTab( name, panel);
    }
    
    public void addDetectorLayer(String name, DetectorShape3DPanel panel){
        tabbedPane.addTab( name, panel);
    }
}
