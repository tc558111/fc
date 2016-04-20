package org.jlab.ecmon.utils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.io.File;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger; 

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.root.basic.EmbeddedCanvas;
import org.jlab.evio.clas12.EvioDataEvent;
import org.jlab.evio.clas12.EvioSource;
import org.jlab.evio.clas12.EvioETSource;
import org.jlab.clas.tools.benchmark.BenchmarkTimer;
import org.jlab.clas12.basic.IDetectorProcessor;
import org.jlab.ecmon.utils.DetectorShapeTabView;
import org.jlab.clas.tools.utils.*;
import org.jlab.hipo.*;

/**
 * @author gavalian
 * Revised by L. C. Smith in Sep-Nov 2015 for development of ECMon.java.
 */
@SuppressWarnings("serial")
public class MonitorApp extends JFrame implements ActionListener,ChangeListener {
    
    EvioSource                       evReader;      
    boolean         isRegularFileOpen = false;
    private File                  file = null;
    private String            filename = null;
    
    EvioETSource                     etReader;
	String         ethost=null,etfile = null;
    private Boolean isEtFileOpen      = false;   
    private Boolean isRemote          = false;
    
    private DetectorShapeTabView detectorView;  
	private int   selectedTabIndex    = 0;  
	
    private JTabbedPane      canvasTabbedPane;
    private JSplitPane             vSplitPane; 
	private JSplitPane	           hSplitPane;
	
    private TreeMap<String,EmbeddedCanvas>  paneCanvas = new TreeMap<String,EmbeddedCanvas>();
	
	private JPanel  controlsPanel0 = null;
    
	private JPanel controlsPanel1  = null;
    private JLabel   statusLabel   = null;
    private JLabel     fileLabel   = null;
    
	private JPanel  controlsPanel2 = null;
    private JButton  buttonPrev    = null;
    private JButton  buttonNext    = null;
    private JButton  buttonNextFFW = null;
    private JButton  buttonStop    = null;
    private JSpinner spinnerDelay  = null;	
    	
	private JPanel  controlsPanel3 = null;
	
    private java.util.Timer    processTimer  = null;
    private javax.swing.Timer   updateTimer  = null;
    private Integer    updateDelay = 0;
    private int        threadDelay = 0;
    
    private JSlider  framesPerSecond;
    static final int FPS_MIN = 0;
    static final int FPS_MAX = 20;
    static final int FPS_INIT = 2;
    
    private JSlider  pixContrastMin;
    static final int PIX_MIN_LO   =  1;
    static final int PIX_MIN_HI   = 20;
    static final int PIX_MIN_INIT =  1;
    
    private JSlider  pixContrastMax;
    static final int PIX_MAX_LO    =   10;
    static final int PIX_MAX_HI    = 2000;
    static final int PIX_MAX_INIT  =   20;
    
    private volatile boolean running;
    public boolean isSingleEvent=false;
    public double pixMin = PIX_MIN_INIT;
    public double pixMax = PIX_MAX_INIT;
    
//    private ProcessEvio processEvio;
    private IDetectorProcessor processorClass = null;
    private DetectorMonitor   monitoringClass = null;
    
    public MonitorApp(int xsize, int ysize){
        super("BrowserApp");
        this.setSize(xsize, ysize);
        this.initMenuBar();
        this.initComponents();
        //this.initTimer();
        this.pack();
        this.setVisible(true);
    }
    
    public void setPluginClass(DetectorMonitor mon) {
    	this.monitoringClass = mon;
    	this.processorClass  = mon;
    }
    
    private void initTimer(){
        updateDelay = 1000 / FPS_INIT;
        updateGUIAction action = new updateGUIAction();
        this.updateTimer = new javax.swing.Timer(updateDelay,action);  
        this.updateTimer.start();
    }
     
    private class updateGUIAction implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
           detectorView.repaint();
        }
    }    
    private void initComponents(){

    	this.setLayout(new BorderLayout());
    	
		this.detectorView       = new DetectorShapeTabView();
		this.canvasTabbedPane   = new JTabbedPane();	
		
		this.vSplitPane = new JSplitPane();				
		this.hSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
        this.hSplitPane.setSize(2000, 600);
        this.hSplitPane.setDividerLocation(800);
        
		this.controlsPanel0 = new JPanel(new GridBagLayout());
		
	    this.controlsPanel1 = new JPanel();
		this.controlsPanel1.setBorder(BorderFactory.createTitledBorder("Event Source"));
		this.controlsPanel1.setSize(500,100);
		
      	this.controlsPanel2 = new JPanel();
		this.controlsPanel2.setBorder(BorderFactory.createTitledBorder("Event Control"));
		this.controlsPanel2.setSize(500,100);
		
      	this.controlsPanel3 = new JPanel();
		this.controlsPanel3.setBorder(BorderFactory.createTitledBorder("Display Control"));
		this.controlsPanel3.setSize(500,100);
		
        fileLabel   = new JLabel("");
        statusLabel = new JLabel("No Opened File");
        
        buttonPrev = new JButton("<");
        buttonPrev.addActionListener(this);
        buttonNext = new JButton(">");
        buttonNext.addActionListener(this);
        
        buttonNextFFW = new JButton(">>");
        buttonNextFFW.addActionListener(this);
        
        buttonStop  = new JButton("||");
        buttonStop.addActionListener(this);
        
        buttonNext.setEnabled(false);
        buttonPrev.setEnabled(false);
        buttonStop.setEnabled(false);
        buttonNextFFW.setEnabled(false); 
        
        SpinnerModel model = new SpinnerNumberModel(0, //initial value
                                                    0, //min
                                                   10, //max
                                                 0.1); //step
        this.spinnerDelay = new JSpinner(model);
        this.spinnerDelay.addChangeListener(this);
        
        
        framesPerSecond = new JSlider(JSlider.HORIZONTAL,FPS_MIN,FPS_MAX,FPS_INIT);
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
//      framesPerSecond.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
        framesPerSecond.setBorder(BorderFactory.createTitledBorder("FPS"));
        Font font = new Font("Serif", Font.ITALIC, 12);
        framesPerSecond.setFont(font);
        framesPerSecond.setPreferredSize(new Dimension(100,50));
        framesPerSecond.setVisible(true);
        
        pixContrastMin = new JSlider(JSlider.HORIZONTAL,PIX_MIN_LO,PIX_MIN_HI,PIX_MIN_INIT);
        pixContrastMin.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent f) {
                JSlider source = (JSlider)f.getSource();                
                        pixMin = (int)source.getValue();  
                        detectorView.repaint();
            }            	
        }
        );  
        pixContrastMin.setPreferredSize(new Dimension(100,50));
        pixContrastMin.setBorder(BorderFactory.createTitledBorder("ZMIN"));
        
        pixContrastMax = new JSlider(JSlider.HORIZONTAL,PIX_MAX_LO,PIX_MAX_HI,PIX_MAX_INIT);
        pixContrastMax.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent f) {
                JSlider source = (JSlider)f.getSource();                
                        pixMax = (int)source.getValue();  
                        detectorView.repaint();
            }            	
        }
        );  
        pixContrastMax.setPreferredSize(new Dimension(100,50));
        pixContrastMax.setBorder(BorderFactory.createTitledBorder("ZMAX"));
        
        /*        
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
        this.controlsPanel2.add(startButton);
*/		
        this.controlsPanel1.add(fileLabel);	
        this.controlsPanel1.add(statusLabel);	
         
        this.controlsPanel2.add(buttonPrev);
        this.controlsPanel2.add(buttonNext);
        this.controlsPanel2.add(buttonNextFFW);
        this.controlsPanel2.add(buttonStop);        
        this.controlsPanel2.add(new JLabel("Delay (sec)"));
        this.controlsPanel2.add(this.spinnerDelay);
        
        this.controlsPanel3.add(this.framesPerSecond);
        this.controlsPanel3.add(this.pixContrastMin);
        this.controlsPanel3.add(this.pixContrastMax);
              
        this.controlsPanel0.setBackground(Color.LIGHT_GRAY);
		this.controlsPanel1.setBackground(Color.LIGHT_GRAY);
		this.controlsPanel2.setBackground(Color.LIGHT_GRAY);
		this.controlsPanel3.setBackground(Color.LIGHT_GRAY);
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 0.5;
        c.gridx=0 ; c.gridy=0 ; this.controlsPanel0.add(this.controlsPanel1,c);
        c.gridx=0 ; c.gridy=1 ; this.controlsPanel0.add(this.controlsPanel2,c);
        c.gridx=0 ; c.gridy=2 ; this.controlsPanel0.add(this.controlsPanel3,c);
       
		this.vSplitPane.setLeftComponent(this.hSplitPane);
		this.vSplitPane.setRightComponent(this.canvasTabbedPane);
		this.hSplitPane.setTopComponent(this.detectorView);				
		this.hSplitPane.setBottomComponent(this.controlsPanel0);

		this.add(this.vSplitPane,BorderLayout.CENTER);
    }
    public void addCanvas(String name){
        EmbeddedCanvas canvas = new EmbeddedCanvas();
        this.paneCanvas.put(name, canvas);
        this.canvasTabbedPane.addTab(name,canvas);
    }
    public EmbeddedCanvas getCanvas(String name){
        return this.paneCanvas.get(name);
    }    
    public DetectorShapeTabView  getDetectorView(){
        return this.detectorView;
    }    
    public JPanel getControlPanel(){
        return this.controlsPanel1;
    }        
    public int getSelectedTabIndex(){
    	return this.selectedTabIndex;
    }

    
    public void addChangeListener() {    
      canvasTabbedPane.addChangeListener(new ChangeListener() {
    	  public void stateChanged(ChangeEvent e) {
    		  if (e.getSource() instanceof JTabbedPane) {
      	        JTabbedPane pane = (JTabbedPane) e.getSource();
      	      selectedTabIndex = pane.getSelectedIndex();
      	      //selectedTabName  = (String) pane.getTitleAt(selectedTabIndex);
    		  }
      	    }
        });
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
        
        JMenu ET_open = new JMenu("Attach to ET");
        JMenuItem s1 = new JMenuItem("Sector 1") ; s1.addActionListener(this);ET_open.add(s1);
        JMenuItem s2 = new JMenuItem("Sector 2") ; s2.addActionListener(this);ET_open.add(s2);
        JMenuItem s3 = new JMenuItem("Sector 3") ; s3.addActionListener(this);ET_open.add(s3);
        JMenuItem s4 = new JMenuItem("Sector 4") ; s4.addActionListener(this);ET_open.add(s4);
        JMenuItem s5 = new JMenuItem("Sector 5") ; s5.addActionListener(this);ET_open.add(s5);
        JMenuItem s6 = new JMenuItem("Sector 6") ; s6.addActionListener(this);ET_open.add(s6);
        file.add(ET_open);
        
        JMenuItem save_histos= new JMenuItem("Save Histos");
        save_histos.addActionListener(this);
        file.add(save_histos);
        
        JMenuItem load_plugin = new JMenuItem("Load Plugin");
        load_plugin.addActionListener(this);
        plugins.add(load_plugin);
               
        this.setJMenuBar(menubar);
    }
    
    private void chooseEtFile() { 
    	if(etfile!=null){
    		try {
    			this.etReader = new EvioETSource(ethost);
    			this.etReader.open(etfile);
    			this.fileLabel.setText("FILE: "+ethost+"::"+etfile);
    		} catch(Exception e){
    			System.out.println("Error opening ET file : " + etfile);
    			this.fileLabel.setText(" ");
    			this.etReader = null;
    		} finally {
    			this.isSingleEvent = false;
    			this.isEtFileOpen  = true;
    			this.isRegularFileOpen = false;
    			this.etReader.close();
    			this.etReader.loadEvents();
    			this.buttonNext.setEnabled(true);
    			this.buttonPrev.setEnabled(false);
    			this.buttonNextFFW.setEnabled(true);
    			this.buttonStop.setEnabled(false);
    		}    
    	}
    }
   
    public void actionPerformed(ActionEvent e) {
        
    	if(e.getActionCommand().compareTo("Sector 1")==0) {ethost="adcecal1";etfile="/tmp/et_sys_clasprod1";}
    	if(e.getActionCommand().compareTo("Sector 2")==0) {ethost="adcecal2";etfile="/tmp/et_sys_clasprod2";}
    	if(e.getActionCommand().compareTo("Sector 3")==0) {ethost="adcecal3";etfile="/tmp/et_sys_clasprod3";}
    	if(e.getActionCommand().compareTo("Sector 4")==0) {ethost="adcecal4";etfile="/tmp/et_sys_clasprod4";}
    	if(e.getActionCommand().compareTo("Sector 5")==0) {ethost="adcecal5";etfile="/tmp/et_sys_clasprod5";}
    	if(e.getActionCommand().compareTo("Sector 6")==0) {ethost="adcecal6";etfile="/tmp/et_sys_clasprod6";}
    	
    	if(ethost!=null) {chooseEtFile(); isRemote=true ;}
    	
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
                isSingleEvent = false;
                //startButton.setText("Start");
                //startButton.setEnabled(true);
                monitoringClass.init();
                this.evReader = new EvioSource();
                this.evReader.open(filename);
                isRegularFileOpen = true;
                this.buttonNext.setEnabled(true);
                this.buttonNextFFW.setEnabled(true);
                Integer current = this.evReader.getCurrentIndex();
                Integer nevents = this.evReader.getSize();  
                this.fileLabel.setText("FILE: "+file.getName());
                this.statusLabel.setText("   EVENTS IN FILE : " + nevents.toString() + "  CURRENT : " + current.toString());
            } else {
                System.out.println("Open command cancelled by user." );
            }
        }
        if(e.getActionCommand().compareTo("<")==0){
            isSingleEvent = true;
            if(evReader.hasEvent()){
                if(evReader.getCurrentIndex()>=2){
                    
                    EvioDataEvent event = (EvioDataEvent) evReader.getPreviousEvent();
                    Integer current = this.evReader.getCurrentIndex();
                    Integer nevents = this.evReader.getSize();
                    this.statusLabel.setText("   EVENTS IN FILE : " + nevents.toString() + "  CURRENT : " + current.toString());

                    if(evReader.getCurrentIndex()==2){
                        this.buttonPrev.setEnabled(false);
                    }
                    if(event!=null){
           
                            try {
                                processorClass.processEvent(event);
                            	monitoringClass.analyze(1);
                                this.getDetectorView().panel1.updateGUI();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        
                    }
                }
            }
        }
        
        if(e.getActionCommand().compareTo(">")==0){
        	isSingleEvent = true;
        	this.processNextEvent();
        	this.buttonPrev.setEnabled(true);
        	monitoringClass.analyze(1);
            this.getDetectorView().panel1.updateGUI();
        }
        
        if(e.getActionCommand().compareTo(">>")==0){
        	isSingleEvent = false;
        	running = true;
            class CrunchifyReminder extends TimerTask {
            	public void run() {
            		for (int i=1 ; i<200 ; i++) {
                      processNextEvent();
            		}
            	}
            }
            this.processTimer = new java.util.Timer();
            this.processTimer.schedule(new CrunchifyReminder(),1,1);
            this.buttonStop.setEnabled(true);
            this.buttonNext.setEnabled(false);
            this.buttonPrev.setEnabled(false);
            this.buttonNextFFW.setEnabled(false);
        }
        
        if(e.getActionCommand().compareTo("||")==0){
        	killTimer();
            this.buttonNextFFW.setEnabled(true);
            this.buttonStop.setEnabled(false);
            this.buttonNext.setEnabled(true);
            this.buttonPrev.setEnabled(true);
        }
        
        if(e.getActionCommand().compareTo("Save Histos")==0){
        	monitoringClass.close();
        }
        
    }

    private void killTimer(){
        if(this.processTimer!=null){
           this.processTimer.cancel();
           this.processTimer = null;
        }   	
    }
    
    private void processNextEvent() {	
    	if (!running) return;
    	
        if(this.isEtFileOpen == true){
            if(this.etReader.hasEvent()==false){
                int maxTries = 20;
                int trycount = 0;
                this.etReader.clearEvents();
                while(trycount<maxTries&&this.etReader.getSize()<=0){
                    
                    System.out.println("[Et-Ring::Thread] ---> reloading the data....");
                    try {
                        Thread.sleep(this.threadDelay);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(MonitorApp.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                    this.etReader.loadEvents();
                    System.out.println("[Et-Ring::Thread] ---> reloaded events try = " + trycount
                    + "  event buffer size = " + this.etReader.getSize());
                    trycount++;
                }
                if(trycount==maxTries){
                    System.out.println("[Et-Ring::Thread] Tried reloading events unsuccesfully");
                }
            }
            
            if(this.etReader.hasEvent()==true){
                EvioDataEvent event = (EvioDataEvent) this.etReader.getNextEvent();
                int current = this.etReader.getCurrentIndex();
                int nevents = this.etReader.getSize();
                if( current>100&&current%500==0) monitoringClass.analyze(1);                
                this.statusLabel.setText("   EVENTS IN ET : " + nevents + "  CURRENT : " + current);
                                  
                    try {
                        processorClass.processEvent(event);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                
                    try {
                    	Thread.sleep(this.threadDelay);
                    } catch (InterruptedException ex) {
                    	Logger.getLogger(MonitorApp.class.getName()).log(Level.SEVERE, null, ex);
                    }
            }
            return;
        }
    	   	
    	if(evReader.hasEvent()){	 
    		EvioDataEvent event = (EvioDataEvent) evReader.getNextEvent();
    		int current = this.evReader.getCurrentIndex();
    		int nevents = this.evReader.getSize();                
    		if( current>100&&current%500==0) monitoringClass.analyze(1);
    		this.statusLabel.setText("   EVENTS IN FILE : " + nevents + "  CURRENT : " + current);
        
    		try {
                Thread.sleep(this.threadDelay);
                processorClass.processEvent(event);
    		} catch (Exception ex) {
    			ex.printStackTrace();
    		}
     
        } else {
          running = false;
    	  killTimer();
    	  evReader.close();
          this.buttonNextFFW.setEnabled(false);
          this.buttonStop.setEnabled(false);
          this.buttonNext.setEnabled(false);
          this.buttonPrev.setEnabled(false);        
    	  monitoringClass.analyze(2);
    	  monitoringClass.close();
          System.out.println("DONE PROCESSING FILE");
        }

    }    
/*    
    private void start() {

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
        
        @Override
        public void run() {
            if(this.connectEtRing==false) this.runOnLocalFile();
            if(this.connectEtRing==true)  this.runOnET();
        }
        
        private void runOnLocalFile(){
		
            monitoringClass.init();
            Integer nev = evReader.getSize();
            try{
                int counter = 0;
                for (int i=2 ; i < nev; i++) {
                //while(evReader.hasEvent()) {
                    counter++;
                    if( counter>100&&counter%500==0) monitoringClass.analyze(1);
                    EvioDataEvent event = (EvioDataEvent) evReader.getNextEvent();
                    Integer current = evReader.getCurrentIndex();                
                    statusLabel.setText("EVENTS IN FILE : " + nev.toString() + "  CURRENT : " + current.toString());

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
                //startButton.setText("Finished");
                running = false;
                //reader.close();
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

    }
*/
	@Override
	public void stateChanged(ChangeEvent e) {
        double delay = (double) this.spinnerDelay.getValue();
        this.threadDelay = (int) (delay*1000);
        isSingleEvent = false;
        if (delay!=0) isSingleEvent=true;     		
	}
    
}
