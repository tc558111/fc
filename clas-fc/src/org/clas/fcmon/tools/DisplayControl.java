package org.clas.fcmon.tools;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class DisplayControl extends JPanel {
    
    static final int FPS_MIN = 0;
    static final int FPS_MAX = 20;
    static final int FPS_INIT = 2;
    
    static final int PIX_MIN_LO   =  10;
    static final int PIX_MIN_HI   = 200;
    static final int PIX_MIN_INIT =  90;
    
    static final int PIX_MAX_LO    =    1;
    static final int PIX_MAX_HI    =  150;
    static final int PIX_MAX_INIT  =   80;
    
    public double pixMin = PIX_MIN_INIT*0.01;
    public double pixMax = PIX_MAX_INIT*0.01;
    
    JSlider framesPerSecond = new JSlider(JSlider.HORIZONTAL,FPS_MIN,FPS_MAX,FPS_INIT);
    JSlider  pixContrastMin = new JSlider(JSlider.HORIZONTAL,PIX_MIN_LO,PIX_MIN_HI,PIX_MIN_INIT);
    JSlider  pixContrastMax = new JSlider(JSlider.HORIZONTAL,PIX_MAX_LO,PIX_MAX_HI,PIX_MAX_INIT);
    
    DetectorShapeTabView detectorView;
    
	public void setPluginClass(DetectorShapeTabView detectorView) {    		 
		this.detectorView = detectorView;
	}
	
	public DisplayControl() {
		
		this.setBackground(Color.LIGHT_GRAY);
        this.add(framesPerSecond);
        this.add(pixContrastMin);
        this.add(pixContrastMax);	
        
        framesPerSecond.setBackground(Color.LIGHT_GRAY);
        framesPerSecond.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent f) {
                JSlider source = (JSlider)f.getSource();
                if (!source.getValueIsAdjusting()) {
                    int fps = (int)source.getValue(); 
                    //int delay = 0;
                    //if (fps!=0 ) delay = 1000 / fps;
                    //updateTimer.setDelay(delay);
                    detectorView.setFPS(fps);
                	//monitoringClass.analyze(1);
                    //getDetectorView().panel1.updateGUI();
                }
            }            	
        }
        );  
        
        framesPerSecond.setMajorTickSpacing(10);
        framesPerSecond.setMinorTickSpacing(1);
        framesPerSecond.setPaintTicks(true);
        framesPerSecond.setPaintLabels(true);
        framesPerSecond.setBorder(BorderFactory.createTitledBorder("FPS"));
        Font font = new Font("Serif", Font.ITALIC, 12);
        framesPerSecond.setFont(font);
        framesPerSecond.setPreferredSize(new Dimension(100,50));
        framesPerSecond.setVisible(true);
        
        pixContrastMin.setBackground(Color.LIGHT_GRAY);

        pixContrastMin.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent f) {
                JSlider source = (JSlider)f.getSource();                
                        pixMin = 0.01*source.getValue();  
                        detectorView.repaint();
            }            	
        }
        );  
        pixContrastMin.setPreferredSize(new Dimension(100,50));
        pixContrastMin.setBorder(BorderFactory.createTitledBorder("ZMIN"));
        
        pixContrastMax.setBackground(Color.LIGHT_GRAY);
        
        pixContrastMax.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent f) {
                JSlider source = (JSlider)f.getSource();                
                        pixMax = 0.01*source.getValue();  
                        detectorView.repaint();
            }            	
        }
        );  
        pixContrastMax.setPreferredSize(new Dimension(100,50));
        pixContrastMax.setBorder(BorderFactory.createTitledBorder("ZMAX"));		}
}
		
