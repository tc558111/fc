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
import java.util.TreeMap;

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
 * @version Modified by lcsmith for use with ECMon
 */
public class DetectorPane2D extends JPanel implements ActionListener {
    
    JPanel              viewPane = null;
    JPanel               mapPane = null;
    JPanel          checkBoxPane = null;
    List<JCheckBox> checkButtons = new ArrayList<JCheckBox>();
    DetectorView2D        view2D = new DetectorView2D();
    
    TreeMap<String,JPanel>   rbPanes = new TreeMap<String,JPanel>();
    
    List<List<buttonMap>> viewStore = new ArrayList<List<buttonMap>>();
    List<List<buttonMap>>  mapStore = new ArrayList<List<buttonMap>>();
    TreeMap<String,Integer>  bStore = new TreeMap<String,Integer>();
    buttonMap         lastMapButton = null;
    
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
         mapPane = new JPanel();
    checkBoxPane = new JPanel();
        viewPane.setBorder(BorderFactory.createSoftBevelBorder(SoftBevelBorder.LOWERED));
        this.add( mapPane,BorderLayout.PAGE_START);
        this.add(  view2D,BorderLayout.CENTER);
        this.add(viewPane,BorderLayout.PAGE_END);        
    }
    
   private class buttonMap {
       String group;
       String name;
       int key;          
       buttonMap(String group, String name, int key) {
           this.group = group;
           this.name = name;
           this.key  = key;
       }
   }
    
    public void addViewStore(String group, List<String>name, List<Integer>index) {
        List<buttonMap> store = new ArrayList<buttonMap>();
        for(int i=0; i<name.size() ; i++) store.add(new buttonMap(group, name.get(i),index.get(i)));
        viewStore.add(store);
    }
    
    public void addMapStore(String group, List<String>name, List<Integer>index) {
        List<buttonMap> store = new ArrayList<buttonMap>();
        for(int i=0; i<name.size() ; i++) store.add(new buttonMap(group, name.get(i),index.get(i)));       
        mapStore.add(store);
    }
    
    public void addMapButtons() {
        for(List<buttonMap> bg: mapStore) { 
            ButtonGroup bG = new ButtonGroup();
            String bGname = null;
            for(buttonMap bn: bg) {
                bGname = bn.group;
                if(!rbPanes.containsKey(bGname)) rbPanes.put(bGname, new JPanel());
                JRadioButton b = new JRadioButton(bn.name);  
                b.setBackground(Color.LIGHT_GRAY);
                if(bG.getButtonCount()==0) b.setSelected(true); 
                b.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                       mapButtonAction(bn.group,bn.name,bn.key);
                    }
                });
                this.rbPanes.get(bGname).add(b); bG.add(b);
            }
            this.mapPane.add(rbPanes.get(bGname));
            mapButtonAction("UVW","EVT",0);
            mapButtonAction("PIX","EVT",0);
        } 
    }
    
    public void addViewButtons() {
        for(List<buttonMap> bg: viewStore) {
            ButtonGroup bG = new ButtonGroup();
            for(buttonMap bn: bg) {
                JRadioButton b = new JRadioButton(bn.name);  
                b.setBackground(Color.LIGHT_GRAY);
                if(bG.getButtonCount()==0) b.setSelected(true); 
                b.addActionListener(new ActionListener() {
                   public void actionPerformed(ActionEvent e) {
                      viewButtonAction(bn.group,bn.name,bn.key);
                   }
                });               
                this.viewPane.add(b); bG.add(b);
            }
        } 
    }  
    
    public void mapButtonAction(String group, String name, int key) {
        lastMapButton = new buttonMap(group,name,key);
        if (!bStore.containsKey(group)) {
            bStore.put(group,key);
        }else{
            bStore.replace(group,key);
        }
        omap = key;
        update();     
    }
    
    public void viewButtonAction(String group, String name, int key) {
        if(group=="LAY") {
            view2D.setLayerActive(name, true);
            if (key<4) {rbPanes.get("UVW").setVisible(true);rbPanes.get("PIX").setVisible(false);omap=bStore.get("UVW");}       
            if (key>3) {rbPanes.get("PIX").setVisible(true);rbPanes.get("UVW").setVisible(false);omap=bStore.get("PIX");}
        }
        if(group=="DET") ilmap = key;
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
       // this.mapPane.add(hitMap);
    }
    
    public DetectorView2D  getView(){
        return this.view2D;
    }
    
    public void update(){
        this.getView().repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub
        
    }
    
}
