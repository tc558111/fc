package org.jlab.ecmon.utils;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.root.pad.EmbeddedCanvas;

public class CanvasViewPanel extends JPanel {
	
    private		JTabbedPane tabbedPane;
    
    public CanvasViewPanel(){
        super();
        this.setLayout(new BorderLayout());
        this.initComponents();
    }	
    
    private void initComponents(){
        tabbedPane = new JTabbedPane();
        this.add(tabbedPane,BorderLayout.CENTER);
    }
    
    public void addCanvasLayer(String name, EmbeddedCanvas panel){
        tabbedPane.addTab(name, panel);
    }
}
