package org.jlab.ecmon.utils;


import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.root.pad.EmbeddedCanvas;
import org.jlab.evio.clas12.EvioDataEvent;
import org.jlab.evio.clas12.EvioSource;
import org.jlab.evio.clas12.EvioETSource;
import org.jlab.clas.tools.benchmark.BenchmarkTimer;
import org.jlab.clas12.basic.IDetectorProcessor;
import org.jlab.ecmon.utils.DetectorShapeTabView;

/**
 *
 * @author gavalian
 * Revised by L. C. Smith in Sep-Nov 2015 for development of ECMon.java (now ECMonv2.java).
 */
@SuppressWarnings("serial")
public class MonitorApp extends JFrame implements ActionListener {
    
	public DetectorShapeTabView         view;  
	public CanvasViewPanel        canvasView;
	public EmbeddedCanvas             canvas0,canvas1,canvas2,canvas3;   
    
    private JPanel bottom;
    private JButton startButton;
    private JSlider framesPerSecond;
    
    static final int FPS_MIN = 0;
    static final int FPS_MAX = 20;
    static final int FPS_INIT = 2;
    
    private volatile boolean running;
    private ProcessEvio processEvio;
    private File file;
    private String filename;
    private IDetectorProcessor processorClass = null;
    private DetectorMonitor   monitoringClass = null;
    
    private Boolean isRemote = false;

    boolean frozen = false;
    
    public MonitorApp(){
        super("BrowserApp");
        this.initMenuBar();
        this.initComponents();
        this.setSize(900, 700);
        this.pack();
        this.setVisible(true);
    }
    
    public void setPluginClass(DetectorMonitor mon) {
    	this.monitoringClass = mon;
    	this.processorClass  = mon;
    }
    
    private void initComponents(){

		canvas0 = new EmbeddedCanvas(800,400,3,1);
		canvas1 = new EmbeddedCanvas(800,400,2,2);
		canvas2 = new EmbeddedCanvas(800,400,3,2);
		canvas3 = new EmbeddedCanvas(800,400,3,2);
		view   = new DetectorShapeTabView();
		canvasView = new CanvasViewPanel();
		canvasView.addCanvasLayer("Occupancy",canvas0);
		canvasView.addCanvasLayer("Attenuation",canvas1);
		canvasView.addCanvasLayer("Pedestals",canvas2);
		canvasView.addCanvasLayer("Timing",canvas3);
		
    	setLayout(new BorderLayout(3,3));
    	
		JSplitPane splitPane = new JSplitPane();
        splitPane.setSize(900, 700);
        splitPane.setPreferredSize(new Dimension(1200,900));
        splitPane.setDividerLocation(900);	
		splitPane.setLeftComponent(view);
		splitPane.setRightComponent(canvasView);
		this.add(splitPane,BorderLayout.CENTER);	
		
		bottom = new JPanel();
		bottom.setLayout(new FlowLayout());
		
		startButton = new JButton();
		startButton.setText("Load EVIO file");
		startButton.setEnabled(false);		
		startButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if (running)
					stop();
				else
					start();
			}
		});
		
        framesPerSecond = new JSlider(JSlider.HORIZONTAL,FPS_MIN,FPS_MAX,FPS_INIT);
        framesPerSecond.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent f) {
                JSlider source = (JSlider)f.getSource();
                if (!source.getValueIsAdjusting()) {
                    int fps = (int)source.getValue();
                    view.setFPS(fps);
                }
            }            	
        }
        );    
        framesPerSecond.setMajorTickSpacing(10);
        framesPerSecond.setMinorTickSpacing(1);
        framesPerSecond.setPaintTicks(true);
        framesPerSecond.setPaintLabels(true);
        framesPerSecond.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
        Font font = new Font("Serif", Font.ITALIC, 12);
        framesPerSecond.setFont(font);
        framesPerSecond.setSize(200,200);
        framesPerSecond.setVisible(true);
        
		bottom.setBackground(Color.LIGHT_GRAY);
		bottom.add(startButton);
        bottom.add(framesPerSecond);
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
        
        JMenuItem ET_open = new JMenuItem("Attach to ET");
        ET_open.addActionListener(this);
        file.add(ET_open);
        
        JMenuItem save_histos= new JMenuItem("Save Histos");
        save_histos.addActionListener(this);
        file.add(save_histos);
        
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
                filename = file.getAbsolutePath();
                isRemote = false;
		        startButton.setText("Start");
		        startButton.setEnabled(true);
            } else {
                System.out.println("Open command cancelled by user." );
            }
        }
        
        if(e.getActionCommand().compareTo("Attach to ET")==0){
            isRemote = true;
            filename = "";
            startButton.setText("Start");
            startButton.setEnabled(true);
        }
        
        if(e.getActionCommand().compareTo("Save Histos")==0){
        	monitoringClass.close();
        }
        
    }

    private void start() {
	startButton.setEnabled(false);
	startButton.setText("Running");
	processEvio = new ProcessEvio(filename,isRemote);
	try {
	    processEvio.setPriority(Thread.currentThread().getPriority()-1);
	}
	catch (Exception e) {
	}
	running = true;
	processEvio.start();
    }
	
    private void stop() {
	startButton.setEnabled(false);
	running = false;
	processEvio  = null;
    }
    
    private class ProcessEvio extends Thread {
        
        Boolean connectEtRing = false;
        String filename;
        private String ethost="adcecal2";
        private String etfile="/tmp/et_sys_clasprod2";
        
        public ProcessEvio(String filename, Boolean connectEtRing) {
            this.connectEtRing = connectEtRing;
            this.filename = filename;
        }
        
        private void runOnLocalFile(){
		
            monitoringClass.init();
            EvioSource reader = new EvioSource();
            reader.open(filename);
            
            try{
                int counter = 0;
                while(reader.hasEvent()){
                    counter++;
                    if( counter>100&&counter%500==0) monitoringClass.analyze(1);
                    EvioDataEvent event = (EvioDataEvent) reader.getNextEvent();
                    try {
                        processorClass.processEvent(event);
                    } catch (Exception e){
                    	e.printStackTrace(System.out);
                    }
                }
                monitoringClass.analyze(2);
                if (!running) return;
                System.out.println("DONE PROCESSING FILE");
            }
            finally {
                startButton.setText("Finished");
                running = false;
                reader.close();
                monitoringClass.close();
            }
        }
        
        private void runOnET(){

            monitoringClass.init();
            EvioETSource reader = new EvioETSource(ethost);
            reader.open(etfile);
	    
            while(true){		
                try {
                    Thread.sleep(20);
                } catch (InterruptedException ex) {
                    Logger.getLogger(MonitorApp.class.getName()).log(Level.SEVERE, null, ex);
                }
                BenchmarkTimer  timer = new BenchmarkTimer("ET");
                timer.resume();
                reader.loadEvents();
                int counter = 0;
                while(reader.hasEvent()==true){
                    counter++;
                    if( counter>100&&counter%500==0) monitoringClass.analyze(1);
                    EvioDataEvent  event = (EvioDataEvent) reader.getNextEvent();
                    try {
                        processorClass.processEvent(event);
                    } catch (Exception e){
                    	e.printStackTrace(System.out);
                    }
                    counter++;
                }
                timer.pause();
                monitoringClass.analyze(1);
                //System.out.println("--> processed events #  " + counter);
                //System.out.println("--> " + timer.toString());
                //monitoringClass.close();
            }
        }
        
        @Override
        public void run() {
            if(this.connectEtRing==false) this.runOnLocalFile();
            if(this.connectEtRing==true)  this.runOnET();
        }
    }
    
}
