package org.jlab.ecmon.utils;

import org.jlab.clas12.basic.IDetectorProcessor;
import org.jlab.clas12.calib.IDetectorListener;

public abstract class DetectorMonitor implements IDetectorProcessor, IDetectorListener{
	
    private String moduleName      = "DetectorMonitor";
    private String moduleVersion   = "0.5";
    private String moduleAuthor    = "lcsmith";
    
    public DetectorMonitor(String name, String version, String author){
        this.moduleName     = name;
        this.moduleVersion  = version;
        this.moduleAuthor   = author;
    }
    
    public abstract void init();
    public abstract void analyze(int inProcess);

    public String getName(){ return moduleName;}
	
}
