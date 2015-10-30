/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clasrec.ui;

//import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import org.jlab.clasrec.loader.ClasPluginChooseDialog;
import org.jlab.clasrec.main.DetectorMonitoring;
import org.jlab.geom.gui.DetectorShape3D;
import org.jlab.geom.gui.DetectorShape3DPanel;
import org.jlab.geom.gui.DetectorShape3DStore;
import org.jlab.geom.gui.DetectorViewPanel;
import org.jlab.geom.gui.IDetectorComponentSelection;
import org.jlab.geom.gui.IDetectorShapeIntensity;
import org.root.pad.EmbeddedCanvas;
import org.jlab.evio.clas12.EvioDataChain;
import org.jlab.evio.clas12.EvioDataEvent;
import org.jlab.evio.clas12.EvioSource;
/**
 *
 * @author gavalian
 */
public class DetectorBrowserApp extends JFrame implements IDetectorComponentSelection,ActionListener {
    
    private JSplitPane splitPane;
    private EmbeddedCanvas canvas;
    private DetectorViewPanel detectorView;
    private TreeMap<String,DetectorShape3DStore>  shapeTree = new TreeMap<String,DetectorShape3DStore>();
    private IDetectorHistogramDraw histogramDrawer = null;
    private DetectorMonitoring     monitoringClass = null;
    
    private JPanel bottom;
    private JButton startButton;
    private volatile boolean running;
    private ProcessFile processFile;
    private File file;
    private EvioSource reader;
    
    public DetectorBrowserApp(){
        super("DetectorBrowserApp");
        this.initMenuBar();
        this.initComponents();
        this.setSize(900, 700);
        this.pack();
        this.setVisible(true);
    }
    
    public void setPluginClass(DetectorMonitoring monitor){
        this.histogramDrawer = monitor;
        this.monitoringClass = monitor;
    }
    
    public void addDetector(DetectorShape3DStore store){
        this.shapeTree.put(store.getName(), store);
    }
    
    public void addDetectorShape(String name, DetectorShape3D shape){
        if(this.shapeTree.containsKey(name)==false){
            DetectorShape3DStore store = new DetectorShape3DStore();
            store.setName(name);
            this.shapeTree.put(name, store);
        }
        this.shapeTree.get(name).addShape(shape);
        //System.out.println("ADDING SHAPE");
    }
    
    
    public void setIntensityMap(String name, IDetectorShapeIntensity imap){
        if(this.shapeTree.containsKey(name)==true){
            this.shapeTree.get(name).setIntensityMap(imap);
            System.out.println("Intensity Map changed for "+name);
        }
    }
    
    public void setHistogramDraw(IDetectorHistogramDraw hd){
        this.histogramDrawer = hd;
    }
    
    public void updateDetectorView(){
        //this.detectorView.removeAll();
        for(Map.Entry<String,DetectorShape3DStore> entry : this.shapeTree.entrySet()){
            DetectorShape3DPanel panel = new DetectorShape3DPanel();
            panel.setSelectionListener(this);
            panel.getStore().getShapes().addAll( entry.getValue().getShapes());
            if(entry.getValue().getIntensityMap()!=null){
                panel.getStore().setIntensityMap(entry.getValue().getIntensityMap());
            }
            if(this.monitoringClass != null){
                panel.getStore().setIntensityMap(this.monitoringClass);
            }
            panel.setName(entry.getKey());
            this.detectorView.addDetectorLayer(entry.getKey(), panel);
        }
        this.repaint();
    }
    
    private void initComponents(){

	setLayout(new BorderLayout(3,3));
	
        splitPane = new JSplitPane();
        splitPane.setSize(900, 700);
        splitPane.setPreferredSize(new Dimension(1200,900));
        splitPane.setDividerLocation(900);
        
        canvas       = new EmbeddedCanvas(800,400,3,1);
        detectorView = new DetectorViewPanel();     
        
        splitPane.setLeftComponent(this.detectorView);
        splitPane.setRightComponent(this.canvas);
        
        this.add(splitPane,BorderLayout.CENTER);
	
	bottom = new JPanel();
	bottom.setLayout(new FlowLayout());
	startButton = new JButton();
	startButton.setText("Load EVIO file");
	startButton.setEnabled(false);
 	bottom.setBackground(Color.LIGHT_GRAY);
	bottom.add(startButton);
	//add(bottom,BorderLayout.SOUTH);
	startButton.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e){
		    if (running)
			stop();
		    else
			start();
		}
	});
	this.add(bottom,BorderLayout.SOUTH);
    }
    
    private void initMenuBar(){
        JMenuBar menubar = new JMenuBar();
        JMenu    file = new JMenu("File");
        JMenu    plugins = new JMenu("Plugins");
        
        menubar.add(file);
        menubar.add(plugins);
        
        JMenuItem file_open = new JMenuItem("Load EVIO File");
        file_open.addActionListener(this);
        file.add(file_open);
        
        JMenuItem load_plugin = new JMenuItem("Load Plugin");
        load_plugin.addActionListener(this);
        plugins.add(load_plugin);
        
        
        this.setJMenuBar(menubar);
    }
    
    public void actionPerformed(ActionEvent e) {
        
        if(e.getActionCommand().compareTo("Load EVIO File")==0){
            final JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new javax.swing.filechooser.FileFilter(){
                public boolean accept(File f) {
                    return f.getName().toLowerCase().endsWith(".evio")
                            || f.isDirectory();
                }
                
                public String getDescription() {
                    return "EVIO CLAS data format";
                }
            });
            String currentDir = System.getenv("PWD");
            if(currentDir!=null){
                fc.setCurrentDirectory(new File(currentDir));
            }
            int returnVal = fc.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                file = fc.getSelectedFile();
		startButton.setText("Start");
		startButton.setEnabled(true);              
            } else {
                System.out.println("Open command cancelled by user." );
            }
        }
        
        if(e.getActionCommand().compareTo("Load Plugin")==0){
            ClasPluginChooseDialog dialog = new ClasPluginChooseDialog("");
            dialog.setModal(true);
            dialog.setVisible(true);
            this.monitoringClass = (DetectorMonitoring) dialog.getMonitoringClass();
            System.out.println("SELECTED PLUGIN FOR MONITORING : " + this.monitoringClass.getName());
            //this.initDetector(this.monitoringClass.getName());
        }
    }

    public void detectorSelected(int sectore, int layer, int component) {
        if(this.monitoringClass!=null){
            try {
                this.monitoringClass.drawComponent(sectore, layer, component, canvas);
            } catch (Exception e){
                System.out.println("Oppps ! problem with SECTOR/LAYER/COMPONENT "
                + sectore + " " + layer + "  " + component );
            }
        }

	if (this.histogramDrawer!=this.monitoringClass) {
        if(this.histogramDrawer!=null){
            try {
             this.histogramDrawer.drawComponent(sectore, layer, component, canvas);
            } catch (Exception e){
                System.out.println("Oppps ! problem with SECTOR/LAYER/COMPONENT "
                + sectore + " " + layer + "  " + component );
            }
        }
	}
    }

    private void start() {
	startButton.setEnabled(false);
	startButton.setText("Running");
	processFile = new ProcessFile(file.getAbsolutePath());
	try {
	    processFile.setPriority(Thread.currentThread().getPriority()-1);
	}
	catch (Exception e) {
	}
	running = true;
	processFile.start();
    }
	
    private void stop() {
	startButton.setEnabled(false);
	running = false;
	processFile  = null;
    }
    
    private class ProcessFile extends Thread {

	ProcessFile(String filename) {
	    reader = new EvioSource();
	    reader.open(filename);
	    monitoringClass.init();
	}

	public void run() {
	    try {    
	    int counter = 0;
	    while(reader.hasEvent()){
		counter++;
		EvioDataEvent event = (EvioDataEvent) reader.getNextEvent();
		try {
		    monitoringClass.processEvent(event);
		} catch (Exception e){
		    System.out.println("SOMETHING WRONG WITH THE EVENT");
		}
	    }
	    monitoringClass.analyze();
	    if (!running) return;
	    System.out.println("DONE PROCESSING FILE");
	    }
	    finally {
		startButton.setText("Finished");
		running = false;
	    }
	}
    }
}
