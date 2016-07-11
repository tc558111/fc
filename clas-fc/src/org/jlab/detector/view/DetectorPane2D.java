/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;

/**
 *
 * @author gavalian
 */
public class DetectorPane2D extends JPanel implements ActionListener {
    
    JPanel             viewPane = null;
    JPanel              mapPane = null;
    DetectorView2D       view2D = new DetectorView2D();
    List<JCheckBox>       checkButtons = new ArrayList<JCheckBox>();
    JPanel                checkBoxPane = null;
    public int ilmap=1;
    public int  omap=0;    
    
    public DetectorPane2D(){
        super();
        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createSoftBevelBorder(SoftBevelBorder.RAISED));
        initUI();
    }
    
    public final void initUI(){
        viewPane = new JPanel();
        mapPane  = new JPanel();
        viewPane.setBorder(BorderFactory.createSoftBevelBorder(SoftBevelBorder.LOWERED));
        this.add(mapPane,BorderLayout.PAGE_START);
        this.add(view2D,BorderLayout.CENTER);
        this.add(viewPane,BorderLayout.PAGE_END);        
        this.checkBoxPane = new JPanel();
    }
    
    public void addRB(List<List<String>> buttons){
        for(List<String> bn : buttons){
            ButtonGroup bG = new ButtonGroup();
            for(int i=0; i< bn.size(); i++){
                JRadioButton b = new JRadioButton(bn.get(i));
                b.setBackground(Color.LIGHT_GRAY);
                b.addActionListener(this);
                b.setActionCommand(bn.get(i));
                if (i==0) b.setSelected(true);
                this.mapPane.add(b); bG.add(b);
            }
        }           
    } 
    
    public void actionPerformed(ActionEvent e) {
        switch(e.getActionCommand() ) {
        case "Inner":
            ilmap=1;
            break;
        case "Outer":
            ilmap=2;
            break;
        case "EVT":
            omap=0;
            break;
        case "NEVT":
            omap=1;
            break;
        case "ADC":
            omap=0;
            break;
        case "TDC":
            omap=0;
            break;
        case "ADC U":
            omap=11;
            break;
        case "ADC V":
            omap=12;
            break;
        case "ADC W":
            omap=13;
            break;
        case "ADC U+V+W":
            omap=9;
            break;
        }
        update();
    }  
    
    public void updateBox(){
        
        this.checkButtons.clear();
        this.checkBoxPane.removeAll();
        
        for(String name : this.view2D.getLayerNames()){
            JCheckBox  cb = new JCheckBox(name);
            cb.setSelected(true);
            
            cb.addItemListener(new ItemListener(){
                @Override
                public void itemStateChanged(ItemEvent e) {
                    JCheckBox box = (JCheckBox) e.getItem();
                    //System.out.println("changed " + box.getActionCommand());
                    if(box.isSelected()==false){
                        view2D.setLayerActive(box.getActionCommand(), false);
                        view2D.repaint();
                    } else {
                        view2D.setLayerActive(box.getActionCommand(), true);
                        view2D.repaint();
                    }
                }
            });
            
            System.out.println(" adding check box " + name);
            checkBoxPane.add(cb);
        }
        this.viewPane.add(checkBoxPane);
        System.out.println(" check box created");
        
        JCheckBox  hitMap = new JCheckBox("Hit Map");
        hitMap.addItemListener(new ItemListener(){
                @Override
                public void itemStateChanged(ItemEvent e) {
                    JCheckBox box = (JCheckBox) e.getItem();
                    if(box.isSelected()==false){
                        view2D.setHitMap(false);
                    } else { view2D.setHitMap(true); } 
                }
        }
        );
        this.mapPane.add(hitMap);
    }
    
    public DetectorView2D  getView(){
        return this.view2D;
    }
    
    public void update(){
        this.getView().repaint();
    }
    
}
