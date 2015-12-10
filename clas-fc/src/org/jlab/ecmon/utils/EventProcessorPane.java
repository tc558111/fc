package org.jlab.ecmon.utils;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.jlab.clas.tools.utils.FileUtils;
import org.jlab.clas12.basic.IDetectorProcessor;
import org.jlab.coda.et.exception.EtException;
import org.jlab.evio.clas12.EvioDataEvent;
import org.jlab.evio.clas12.EvioETSource;
import org.jlab.evio.clas12.EvioSource;

/**
 *
 * @author gavalian
 * @ Revised for use in ECMon by L.C. Smith
 */
public class EventProcessorPane extends JPanel implements ActionListener {
    
    List<IDetectorProcessor>  processorList = new ArrayList<IDetectorProcessor>();
    EvioSource                reader        = new EvioSource();
    EvioETSource              etReader      = null;
    boolean                   isRegularFileOpen = false;
    boolean                   isEtFileOpen      = false;
    JButton                   buttonPrev    = null;
    JButton                   buttonNext    = null;
    JButton                   buttonNextFFW = null;
    JButton                   buttonNextFFFW = null;
    JButton                   buttonStop    = null;
    JLabel                    statusLabel   = null;
    Timer                     processTimer  = null;
    private  Integer          threadDelay   = 200;
    
    public EventProcessorPane(){
        super();
        this.setLayout(new FlowLayout());
        //this.setBorder();
        buttonPrev = new JButton("<");
        buttonPrev.addActionListener(this);
        buttonNext = new JButton(">");
        buttonNext.addActionListener(this);
        
        buttonNextFFW = new JButton(">>");
        buttonNextFFW.addActionListener(this);
        
        buttonNextFFFW = new JButton(">>>");
        buttonNextFFFW.addActionListener(this);
        
        buttonStop  = new JButton("||");
        buttonStop.addActionListener(this);
        
        buttonNext.setEnabled(false);
        buttonPrev.setEnabled(false);
        buttonStop.setEnabled(false);
        buttonNextFFW.setEnabled(false);
        
        JButton  buttonOpen = new JButton("File");
        buttonOpen.addActionListener(this);
        JButton  buttonEt = new JButton("Et");
        buttonEt.addActionListener(this);
        
        this.add(buttonOpen);
        this.add(buttonEt);
        this.add(Box.createHorizontalStrut(30));
        this.add(buttonPrev);
        this.add(buttonNext);
        this.add(buttonNextFFW);
        this.add(buttonNextFFFW);
        this.add(buttonStop);
        this.add(Box.createHorizontalStrut(30));
        
        statusLabel = new JLabel("No Opened File");
        this.add(statusLabel);
    }
    
    public void addProcessor(IDetectorProcessor proc){
        this.processorList.add(proc);
    }
    
    private void chooseEtFile(){
    	String etfilename = "/tmp/et_sys_clasprod2";
        System.out.println(" Opening Et file : " + etfilename);
        if(etfilename!=null){
            try {
                this.etReader = new EvioETSource("adcecal2");
                this.etReader.open(etfilename);
            } catch(Exception e){
                System.out.println("Error opening ET file : " + etfilename);
                this.etReader = null;
            } finally {
                this.isEtFileOpen = true;
                this.isRegularFileOpen = false;
                this.reader.close();
                this.etReader.loadEvents();
                this.buttonNext.setEnabled(true);
                this.buttonPrev.setEnabled(false);
                this.buttonNextFFW.setEnabled(true);
                this.buttonStop.setEnabled(false);
            }
        }
    }
    
    public void actionPerformed(ActionEvent e) {
        
        if(e.getActionCommand().compareTo("Et")==0){
            this.chooseEtFile();
        }
        
        if(e.getActionCommand().compareTo("File")==0){
            String currentDir = System.getenv("PWD");
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

            if(currentDir!=null){
                fc.setCurrentDirectory(new File(currentDir));
            }
            int returnVal = fc.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                //This is where a real application would open the file.
               System.out.println("Opening: " + file.getAbsolutePath() + "." );
               //this.processFile(file.getAbsolutePath());
               this.reader.open(file.getAbsolutePath());
               this.buttonNext.setEnabled(true);
               this.buttonNextFFW.setEnabled(true);
               Integer current = this.reader.getCurrentIndex();
               Integer nevents = this.reader.getSize();
               
               this.statusLabel.setText("EVENTS IN FILE : " + nevents.toString() + "  CURRENT : " + current.toString());
               this.isRegularFileOpen = true;
               this.isEtFileOpen = false;
            } else {
                System.out.println("Open command cancelled by user." );
                //this.buttonClose.setEnabled(true);
                //this.progressBar.setValue(100);
            }
        }
        if(e.getActionCommand().compareTo("<")==0){
           
            if(reader.hasEvent()){
                if(reader.getCurrentIndex()>=2){
                    
                    EvioDataEvent event = (EvioDataEvent) reader.getPreviousEvent();
                    Integer current = this.reader.getCurrentIndex();
                    Integer nevents = this.reader.getSize();
                    this.statusLabel.setText("EVENTS IN FILE : " + nevents.toString() + "  CURRENT : " + current.toString());

                    if(reader.getCurrentIndex()==2){
                        this.buttonPrev.setEnabled(false);
                    }
                    if(event!=null){
                        for(IDetectorProcessor proc : this.processorList){
                            try {
                                proc.processEvent(event);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        if(e.getActionCommand().compareTo(">")==0){
            this.processNextEvent();
        }
        
        if(e.getActionCommand().compareTo(">>>")==0){
            this.buttonStop.setEnabled(true);
            this.buttonNext.setEnabled(false);
            this.buttonPrev.setEnabled(false);
            this.buttonNextFFW.setEnabled(false);
            this.buttonNextFFFW.setEnabled(false);
        	while(reader.hasEvent()) {
                EvioDataEvent event = (EvioDataEvent) reader.getNextEvent();
                Integer current = this.reader.getCurrentIndex();
                Integer nevents = this.reader.getSize();                
                this.statusLabel.setText("EVENTS IN FILE : " + nevents.toString() + "  CURRENT : " + current.toString());
                
                for(IDetectorProcessor proc : this.processorList){
                    try {
                        proc.processEvent(event);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
        	}
        	reader.close();
        }
        
        if(e.getActionCommand().compareTo(">>")==0){
            class CrunchifyReminder extends TimerTask {
		public void run() {
                    //System.out.println(" Timer processing next event");
                    processNextEvent();
		}

            }
            processTimer = new Timer();
            processTimer.schedule(new CrunchifyReminder(), 1, 1);
            this.buttonStop.setEnabled(true);
            this.buttonNext.setEnabled(false);
            this.buttonPrev.setEnabled(false);
            this.buttonNextFFW.setEnabled(false);
        }
        
        if(e.getActionCommand().compareTo("||")==0){
            if(this.processTimer!=null){
                this.processTimer.cancel();
                this.processTimer = null;
                this.buttonNextFFW.setEnabled(true);
                this.buttonStop.setEnabled(false);
                this.buttonNext.setEnabled(true);
                this.buttonPrev.setEnabled(true);
            }
        }
    }
    
    
    private void processNextEvent(){
        
        if(this.isEtFileOpen == true){
            if(this.etReader.hasEvent()==false){
                int maxTries = 20;
                int trycount = 0;
                this.etReader.clearEvents();
                while(trycount<maxTries&&this.etReader.getSize()<=0){
                    System.out.println("[Et-Ring::Thread] ---> reloading the data....");
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(EventProcessorPane.class.getName()).log(Level.SEVERE, null, ex);
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
                Integer current = this.etReader.getCurrentIndex();
                Integer nevents = this.etReader.getSize();
                
                this.statusLabel.setText("EVENTS IN FILE : " + nevents.toString() + "  CURRENT : " + current.toString());
                
                for(IDetectorProcessor proc : this.processorList){
                    try {
                        proc.processEvent(event);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
            return;
        }
        
        if(reader.hasEvent()){
            this.buttonPrev.setEnabled(true);
            EvioDataEvent event = (EvioDataEvent) reader.getNextEvent();
            Integer current = this.reader.getCurrentIndex();
            Integer nevents = this.reader.getSize();                
            this.statusLabel.setText("EVENTS IN FILE : " + nevents.toString() + "  CURRENT : " + current.toString());
            
            for(IDetectorProcessor proc : this.processorList){
                try {
                    proc.processEvent(event);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    
    public static void main(String[] args){
        JFrame fr = new JFrame();
        EventProcessorPane  pane = new EventProcessorPane();
        fr.add(pane);
        fr.pack();
        fr.setVisible(true);
    }
}

