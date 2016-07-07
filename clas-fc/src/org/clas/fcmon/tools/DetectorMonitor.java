package org.clas.fcmon.tools;

import java.util.TreeMap;

import org.jlab.clas12.basic.IDetectorProcessor;
import org.jlab.clas12.calib.IDetectorListener;
import org.jlab.data.io.DataEvent;
import org.jlab.io.task.IDataEventListener;

public abstract class DetectorMonitor implements IDataEventListener, IDetectorListener{
	
    private String moduleName      = "DetectorMonitor";
    private String moduleVersion   = "0.5";
    private String moduleAuthor    = "lcsmith";
    
    public DetectorMonitor(String name, String version, String author){
        this.moduleName     = name;
        this.moduleVersion  = version;
        this.moduleAuthor   = author;
    }
    
    public abstract void init();
    public abstract void initDetector(int s1, int s2);
    public abstract void analyze(int inProcess);
    public abstract void close();
    public abstract void reset();
    public abstract void saveToFile();
    public abstract TreeMap<String,Object> getGlob();
    public abstract void putGlob(String name, Object obj);
    public String getName(){ return moduleName;}

    public void dataEventAction(DataEvent de) {
        // TODO Auto-generated method stub       
    }
	
}
