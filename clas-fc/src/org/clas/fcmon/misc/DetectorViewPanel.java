package org.clas.fcmon.misc;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
/**
 *
 * @author gavalian
 * Revised by L. C. Smith in Sep-Nov 2015 for development of ECMon.java (now ECMonv1.java)
 */
public class DetectorViewPanel extends JPanel {
    
    private		JTabbedPane tabbedPane;
    private DetectorShape3DPanel panel1=null;
    private DetectorShape3DPanel panel2=null;
    private JSlider framesPerSecond;
    static final int FPS_MIN = 0;
    static final int FPS_MAX = 20;
    static final int FPS_INIT = 2;
    int fps=FPS_INIT;
    boolean frozen = false;
    
    public DetectorViewPanel(){
        super();
        this.setLayout(new BorderLayout());
        this.initComponents();
    }
    
    private void initComponents(){
        tabbedPane = new JTabbedPane();
        tabbedPane.addChangeListener(new ChangeListener() {
        	   public void stateChanged(ChangeEvent e) {
        	        JTabbedPane tabbedPane = (JTabbedPane) e.getSource();
        	        panel1 = (DetectorShape3DPanel) tabbedPane.getSelectedComponent();
        	        panel1.start(fps);
        	        if (panel2!=null) panel2.stop();
        	        panel2=panel1;
        	    }
        });
        this.add(tabbedPane,BorderLayout.CENTER);
        
        framesPerSecond = new JSlider(JSlider.HORIZONTAL,FPS_MIN,FPS_MAX,FPS_INIT);
        framesPerSecond.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent f) {
                JSlider source = (JSlider)f.getSource();
                if (!source.getValueIsAdjusting()) {
                    fps = (int)source.getValue();
                    if (fps == 0) panel1.stop();
                    if (fps > 0)  panel1.start(fps);
                }
            }            	
        }
        );        		
        framesPerSecond.setMajorTickSpacing(10);
        framesPerSecond.setMinorTickSpacing(1);
        framesPerSecond.setPaintTicks(true);
        framesPerSecond.setPaintLabels(true);
        Font font = new Font("Serif", Font.ITALIC, 15);
        framesPerSecond.setFont(font);
        framesPerSecond.setSize(200,100);
        framesPerSecond.setVisible(true);
        this.add(framesPerSecond,BorderLayout.SOUTH);
    } 
    
    public void addDetectorLayer(String name, DetectorShape3DPanel panel){
        tabbedPane.addTab( name, panel);
    }

}
