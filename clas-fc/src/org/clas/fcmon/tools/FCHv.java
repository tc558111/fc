package org.clas.fcmon.tools;

import java.util.Arrays;
import java.util.Date;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.epics.ca.Channel;
import org.epics.ca.Context;
import org.epics.ca.Listener;
import org.epics.ca.Monitor;
import org.epics.ca.Status;
import org.epics.ca.data.Alarm;
import org.epics.ca.data.Control;
import org.epics.ca.data.Graphic;
import org.epics.ca.data.GraphicEnum;
import org.epics.ca.data.Timestamped;
import org.jlab.utils.groups.IndexedList;

public class FCHv {
    
    public MonitorApp      app = null;
    public DetectorMonitor mon = null;
    public Context     context = null;
    
    IndexedList<String>                            map = new IndexedList<String>(3);
    TreeMap<String,IndexedList<String>>          pvMap = new TreeMap<String,IndexedList<String>>();
    TreeMap<String,IndexedList<Channel<Double>>> caMap = new TreeMap<String,IndexedList<Channel<Double>>>();
   
    String detector = null;
	
	public FCHv(String det){
	    this.detector = det;
	    this.context = new Context();   
	}
	
    public void setApplicationClass(MonitorApp app) {
        this.app = app;
    }
    
    public void setMonitoringClass(DetectorMonitor mon) {
        this.mon = mon;
    }
    
    public double getCaValue(String action, int sector, int layer, int channel) throws InterruptedException, ExecutionException {
        caMap.get(action).getItem(sector,layer,channel).connectAsync().get();
        CompletableFuture<Double> ffd = caMap.get(action).getItem(sector,layer,channel).getAsync();
        return ffd.get(); 
    }
    
    public void setCaNames() {
        setCaActionNames("vmon");
        setCaActionNames("imon");
        setCaActionNames("vset");        
    }
    
    public void setCaActionNames(String action) {
        
        IndexedList<Channel<Double>> map = new IndexedList<Channel<Double>>(3);
        
        for (int is=1; is<7 ; is++) {
            for (int il=1; il<3 ; il++) {
                for (int ic=1; ic<19; ic++) {
                    String pv = getPvName(action,is,il,ic);
                    map.add(context.createChannel(pv, Double.class),is,il,ic);
                }
            }
        } 
        caMap.put(action,map);
    }
    
    public void setPvNames(String det) {
        setPvActionNames(det,"vmon");
        setPvActionNames(det,"imon");
        setPvActionNames(det,"vset");
        setPvActionNames(det,"pwonoff");
    }
    
    public void setPvActionNames(String det, String action) {
      
        IndexedList<String> map = new IndexedList<String>(3);
        
        for (int is=1; is<7 ; is++) {
            for (int il=1; il<3 ; il++) {
                for (int ic=1; ic<19; ic++) {
                    map.add(getPvString(det,is,il,ic,action),is,il,ic);
                }
            }
        }
        pvMap.put(action,map);
    }
    
    public String getPvName(String action, int sector, int layer, int channel) {
        switch (action) {
        case    "vmon": return (String) pvMap.get(action).getItem(sector,layer,channel);
        case    "imon": return (String) pvMap.get(action).getItem(sector,layer,channel); 
        case    "vset": return (String) pvMap.get(action).getItem(sector,layer,channel); 
        case "pwonoff": return (String) pvMap.get(action).getItem(sector,layer,channel); 
        }
        return "Invalid action";
    }
    
	public String layerToString(String det, int layer) {
	    switch (det) {
	    case "LTCC": return (layer<2 ? "L":"R");
	    }
	    return "Invalid Detector";
	}
	
	public String channelToString(int channel) {
	    return (channel<10 ? "0"+Integer.toString(channel):Integer.toString(channel));
	}
	
	public String getPvString(String det, int sector, int layer, int channel,String action) {
	    return "B_DET_"+det+"_HV_SEC"+sector+"_"+layerToString(det,layer)+"_E"+channelToString(channel)+":"+action;  
	}

}
