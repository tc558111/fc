/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.ecmon.utils;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import org.jlab.clas.detector.BankType;
import org.jlab.clas.detector.DetectorBankEntry;
import org.jlab.clas.detector.DetectorCollection;
import org.jlab.clas12.basic.IDetectorProcessor;
import org.jlab.clas12.detector.EventDecoder;
import org.jlab.clasrec.main.DetectorEventProcessorPane;
import org.jlab.data.io.DataEvent;
import org.jlab.evio.clas12.EvioDataEvent;
import org.root.histogram.H1D;
import org.root.pad.EmbeddedCanvas;

/**
 *
 * @author gavalian
 */
public class EventDecoderViewer extends JPanel implements IDetectorProcessor,MouseListener {
    JTree  dataTree = null;
    EmbeddedCanvas  canvas = new EmbeddedCanvas();
    DetectorEventProcessorPane evPane = new DetectorEventProcessorPane();
    DetectorCollection<H1D>    adcCollection = new DetectorCollection<H1D>();
    EventDecoder                decoder       = new EventDecoder();
    
    public EventDecoderViewer(){
        super();
        this.setLayout(new BorderLayout());
        JSplitPane splitPane = new JSplitPane();
        dataTree = new JTree();
        splitPane.setLeftComponent(dataTree);
        splitPane.setRightComponent(canvas);
        this.add(splitPane,BorderLayout.CENTER);
        this.add(evPane,BorderLayout.PAGE_END);
        this.dataTree.addMouseListener(this);
        this.adcCollection.setName("FADC");
        this.evPane.addProcessor(this);
    }
    
    public void processEvent(DataEvent event) {
        this.adcCollection.clear();
        this.decoder.decode((EvioDataEvent) event);
        List<DetectorBankEntry> counters =  decoder.getDataEntries();
        for(DetectorBankEntry entry : counters){
            if(entry.getType()==BankType.ADCPULSE){
                H1D h1 = EventDecoder.getADCPulse(entry);
                
                this.adcCollection.add(
                        entry.getDescriptor().getCrate(),
                        entry.getDescriptor().getSlot(),
                        entry.getDescriptor().getChannel()
                        , h1);
                //System.out.println(entry);
            }
        }
        
        this.dataTree.setModel(this.adcCollection.getTreeModel());
        //this.adcCollection.show();
        List<DetectorBankEntry> cntALL =  decoder.getDataEntries();
        for(DetectorBankEntry cnt : cntALL){
            System.out.println(cnt);
        }

    }
    
    public static void main(String[] args){
        
        EventDecoderViewer viewer = new EventDecoderViewer();
        JFrame frame = new JFrame();
        frame.add(viewer);
        frame.pack();
        frame.setVisible(true);
    }

    public void mouseClicked(MouseEvent e) {
        
    }

    public void mousePressed(MouseEvent e) {
        if(e.getClickCount()==2){
            TreePath selPath = dataTree.getPathForLocation(e.getX(), e.getY());
            Object[] paths = (Object[]) selPath.getPath();
            if(paths.length==4){
                System.out.println(" 4 clicked");
                System.out.println(paths[1].toString() + " " + paths[2].toString() + " " + paths[3].toString());
                int crate = Integer.parseInt(paths[1].toString());
                int slot  = Integer.parseInt(paths[2].toString());
                int chann = Integer.parseInt(paths[3].toString());
                if(this.adcCollection.hasEntry(crate,slot, chann)){
                    canvas.divide(1, 1);
                    canvas.cd(0);
                    canvas.draw(this.adcCollection.get(crate,slot, chann));
                }
                    
            } 
            System.out.println(" Double click detected");
        }
    }

    public void mouseReleased(MouseEvent e) {
        
    }

    public void mouseEntered(MouseEvent e) {
        
    }

    public void mouseExited(MouseEvent e) {
        
    }

    
}
