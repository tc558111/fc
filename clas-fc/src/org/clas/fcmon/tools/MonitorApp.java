package org.clas.fcmon.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
 
import java.util.TreeMap;
 
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.root.basic.EmbeddedCanvas;
import org.clas.fcmon.detector.view.DetectorPane2D;
import org.clas.fcmon.tools.DetectorShapeTabView;
 
/*
 * @author gavalian
 * Revised by L. C. Smith in Sep-Nov 2015 for development of ECMon.java.
 */

@SuppressWarnings("serial")
public class MonitorApp extends JFrame implements ActionListener {
    
    DetectorPane2D detectorView;  
    int      selectedTabIndex = 0;  
    String   selectedTabName  = " ";  
	
    JTabbedPane      canvasTabbedPane;
    JSplitPane             vSplitPane; 
    JSplitPane	           hSplitPane;
	
    JPanel  canvasPane = null;
    JPanel  buttonPane = null;
    
    TreeMap<String,EmbeddedCanvas>  paneCanvas = new TreeMap<String,EmbeddedCanvas>();
	
    JPanel  controlsPanel0 = null;        
    JPanel  controlsPanel1 = null;
    JPanel  controlsPanel2 = null;
    JPanel  controlsPanel3 = null;
	
    EventControl              eventControl = null;    
    public DisplayControl   displayControl = null;	
    public Mode7Emulation   mode7Emulation = null;
    
    public String currentView = null;
    public int  detectorIndex = 0;
    public boolean doEpics = false;
    public String hipoPath = null;
    
//    Miscellaneous    extra = new Miscellaneous();
       
    DetectorMonitor   monitoringClass = null;
    
    public MonitorApp(String name, int xsize, int ysize) {
        super(name);
        this.setPreferredSize(new Dimension(xsize, ysize));
    }
    
    public void init(){
        this.addChangeListener();
        this.pack();
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    public void setPluginClass(DetectorMonitor mon) {
    	this.monitoringClass = mon;
    }
    
    public void getEnv() {        
        String   ostype = System.getenv("OSTYPE");             
        if (ostype=="Linux") {
            String hostname = System.getenv("HOSTNAME");
            if(hostname.substring(0,3)=="clon") {
              System.out.println("Running on "+hostname);
              doEpics = true;
              hipoPath = "/home/lcsmith";
            }
        } else {
            System.out.println("Running on "+ostype);
            doEpics = false;
            hipoPath  = "/Users/colesmith";
        }
    }    
    
    public void makeGUI(){

        this.setLayout(new BorderLayout());   
    	
        this.detectorView       = new DetectorPane2D();
        this.canvasPane         = new JPanel();
        this.canvasTabbedPane   = new JTabbedPane();	
        this.buttonPane         = new JPanel();
		
// Canvas buttons
		
        buttonPane.setLayout(new FlowLayout());
        JButton resetBtn = new JButton("Clear Histos");
        resetBtn.addActionListener(this);
        buttonPane.add(resetBtn);	
        
        JButton saveBtn = new JButton("Save Histos");
        saveBtn.addActionListener(this);
        buttonPane.add(saveBtn);	
        
        JButton loadBtn = new JButton("Load Histos");
        loadBtn.addActionListener(this);
        buttonPane.add(loadBtn);    
        
// Control Panels
		
        this.controlsPanel0 = new JPanel(new GridBagLayout());
		
        this.controlsPanel1 = new JPanel();
        this.controlsPanel1.setBorder(BorderFactory.createTitledBorder("Event Control"));
		
        this.controlsPanel2 = new JPanel();
        this.controlsPanel2.setBorder(BorderFactory.createTitledBorder("Display Control"));
		
        this.controlsPanel3 = new JPanel();
        this.controlsPanel3.setBorder(BorderFactory.createTitledBorder("Mode 7 Emulation"));

        eventControl   = new EventControl();   this.controlsPanel1.add(eventControl);
        displayControl = new DisplayControl(); this.controlsPanel2.add(displayControl);
        mode7Emulation = new Mode7Emulation(); this.controlsPanel3.add(mode7Emulation);
      
        eventControl.setPluginClass(this.monitoringClass,this.detectorView);
        displayControl.setPluginClass(this.detectorView);
        mode7Emulation.setPluginClass(this.detectorView);
        
    	this.setJMenuBar(new FCMenuBar(eventControl));
		
        this.controlsPanel0.setBackground(Color.LIGHT_GRAY);
        this.controlsPanel1.setBackground(Color.LIGHT_GRAY);
        this.controlsPanel2.setBackground(Color.LIGHT_GRAY);
        this.controlsPanel3.setBackground(Color.LIGHT_GRAY);
		
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 0.5;
		
        c.gridx=0 ; c.gridy=0 ; this.controlsPanel0.add(this.controlsPanel1,c);
        c.gridx=0 ; c.gridy=1 ; this.controlsPanel0.add(this.controlsPanel2,c);
        c.gridx=0 ; c.gridy=2 ; this.controlsPanel0.add(this.controlsPanel3,c);
        		
// Basic GUI layout
        
        this.hSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,detectorView,controlsPanel0);		
        this.hSplitPane.setDividerLocation(600);  
        this.hSplitPane.setResizeWeight(1.0);
		
        canvasPane.setLayout(new BorderLayout());
        this.canvasPane.add(canvasTabbedPane,BorderLayout.CENTER);
        this.canvasPane.add(buttonPane,BorderLayout.PAGE_END);
        
        this.vSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,hSplitPane,canvasPane);			
        this.vSplitPane.setDividerLocation(600);  

        this.add(this.vSplitPane,BorderLayout.CENTER);
    }
    
 /*   
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
*/  
    public void addCanvas(String name){
        EmbeddedCanvas canvas = new EmbeddedCanvas();
        this.paneCanvas.put(name, canvas);
        this.canvasTabbedPane.addTab(name,canvas);
    }
    
    public void addCanvas(String name, EmbeddedCanvas canvas){         
        this.canvasTabbedPane.addTab(name,canvas);
    }
    
    public void addFrame(String name, JPanel frame) {
        this.canvasTabbedPane.addTab(name, frame);
    }
    
    public EmbeddedCanvas getCanvas(String name){
        return this.paneCanvas.get(name);
    }  
    
    public DetectorPane2D getDetectorView(){
        return this.detectorView;
    }  
    
    public JPanel getControlPanel(){
        return this.controlsPanel1;
    }  
    
    public Boolean isSingleEvent(){
    	return eventControl.isSingleEvent;
    }

    public int getSelectedTabIndex(){
        return this.selectedTabIndex;
    }
    
    public String getSelectedTabName(){
        return this.selectedTabName;
    }
    
    public void setSelectedTab(int index) {
        this.canvasTabbedPane.setSelectedComponent(this.canvasTabbedPane.getComponent(index));
        System.out.println("Selected Tab is "+this.canvasTabbedPane.getTitleAt(index));
    }
    
    public void addChangeListener() {    
      canvasTabbedPane.addChangeListener(new ChangeListener() {
         public void stateChanged(ChangeEvent e) {
         if (e.getSource() instanceof JTabbedPane) {
           JTabbedPane pane = (JTabbedPane) e.getSource();
           selectedTabIndex = pane.getSelectedIndex();
           selectedTabName  = (String) pane.getTitleAt(selectedTabIndex);
         }
         }
      });
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand().compareTo("Clear Histos")==0) monitoringClass.reset();
        if(e.getActionCommand().compareTo("Save Histos")==0)  monitoringClass.saveToFile();
        if(e.getActionCommand().compareTo("Load Histos")==0)  monitoringClass.readHipoFile();
    }      
}
