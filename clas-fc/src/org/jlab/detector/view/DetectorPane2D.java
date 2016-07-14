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
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.SoftBevelBorder;

/**
 *
 * @author gavalian
 * @version Modified by lcsmith for use with ECMon
 */
public class DetectorPane2D extends JPanel {
    
    JPanel               mapPane = null;
    JPanel              viewPane = null;
    DetectorView2D        view2D = new DetectorView2D();
    
    TreeMap<String,JPanel>  rbPanes = new TreeMap<String,JPanel>();
    
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
        makeGUI();
    }
    
    public final void makeGUI(){
         mapPane = new JPanel();
        viewPane = new JPanel();  
        viewPane.setBorder(BorderFactory.createSoftBevelBorder(SoftBevelBorder.LOWERED));
        this.add( mapPane,BorderLayout.PAGE_START);
        this.add(  view2D,BorderLayout.CENTER);
        this.add(viewPane,BorderLayout.PAGE_END);        
    }
       
    public void initViewButtons(int groupIndex, int nameIndex) {
        buttonMap map = viewStore.get(groupIndex).get(nameIndex);
        map.b.setSelected(true);        
        this.viewButtonAction(map.group,map.name,map.key);
     }
    
    public void initMapButtons(int groupIndex, int nameIndex) {
        buttonMap map = mapStore.get(groupIndex).get(nameIndex);
        map.b.setSelected(true);        
        this.mapButtonAction(map.group,map.name,map.key);
     }
    
    private class buttonMap {
       String group;
       String name;
       int key;       
       JRadioButton b;
       buttonMap() {}
       buttonMap(String group, String name, int key) {
           this.group = group;
            this.name = name;
             this.key = key;
               this.b = new JRadioButton(this.name);
       }
   }
    
    public void addViewStore(String group, List<String> name, List<Integer> key) {
        List<buttonMap> store = new ArrayList<buttonMap>();
        for(int i=0; i<name.size() ; i++) store.add(new buttonMap(group, name.get(i), key.get(i)));
        viewStore.add(store);
    }
    
    public void addMapStore(String group, List<String> name, List<Integer> key) {
        List<buttonMap> store = new ArrayList<buttonMap>();
        for(int i=0; i<name.size() ; i++) store.add(new buttonMap(group, name.get(i), key.get(i)));       
        mapStore.add(store);
    }
    
    public void addMapButtons() {
        for(List<buttonMap> bg: mapStore) { 
            ButtonGroup bG = new ButtonGroup();
            String bGname = null;
            for(buttonMap bn: bg) {
                bGname = bn.group;
                if(!rbPanes.containsKey(bGname)) rbPanes.put(bGname, new JPanel());
                bn.b.setBackground(Color.LIGHT_GRAY);
                bn.b.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                       mapButtonAction(bn.group,bn.name,bn.key);
                    }
                });
                this.rbPanes.get(bGname).add(bn.b); bG.add(bn.b);
            }
            this.mapPane.add(rbPanes.get(bGname));
        } 
    }
    
    public void addViewButtons() {
        for(List<buttonMap> bg: viewStore) {
            ButtonGroup bG = new ButtonGroup();
            for(buttonMap bn: bg) {
                bn.b.setBackground(Color.LIGHT_GRAY);
                bn.b.addActionListener(new ActionListener() {
                   public void actionPerformed(ActionEvent e) {
                      viewButtonAction(bn.group,bn.name,bn.key);
                   }
                });               
                this.viewPane.add(bn.b); bG.add(bn.b);
            }
        } 
    }  
    
    public void mapButtonAction(String group, String name, int key) {
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
            view2D.setLayerState(name, true);
            if (key<4) {rbPanes.get("UVW").setVisible(true);rbPanes.get("PIX").setVisible(false);omap=bStore.get("UVW");}       
            if (key>3) {rbPanes.get("PIX").setVisible(true);rbPanes.get("UVW").setVisible(false);omap=bStore.get("PIX");}
        }
        if(group=="DET") ilmap = key;
        update();        
    }
    
    public void setFPS(int fps){
        if (fps==0) getView().stop();
        if (fps>0)  getView().start(fps);
      }
    
    public DetectorView2D  getView(){
        return this.view2D;
    }
    
    public void update(){
        this.getView().repaint();
    }
    
}
