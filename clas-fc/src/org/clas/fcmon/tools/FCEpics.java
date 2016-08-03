package org.clas.fcmon.tools;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

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
import org.jlab.detector.base.DetectorDescriptor;
import org.jlab.utils.groups.IndexedList;
import org.root.basic.EmbeddedCanvas;

public class FCEpics  {
    
    private String  appName    = null;

    public MonitorApp      app = null;
    public DetectorMonitor mon = null;
    public Context     context = null;
    
    JPanel HVScalers = new JPanel();
    public EmbeddedCanvas scaler1DView = new EmbeddedCanvas();
    public EmbeddedCanvas scaler2DView = new EmbeddedCanvas();
    
    IndexedList<String>                             map = new IndexedList<String>(4);
    TreeMap<String,IndexedList<String>>           pvMap = new TreeMap<String,IndexedList<String>>();
    TreeMap<String,IndexedList<Channel<Double>>>  caMap = new TreeMap<String,IndexedList<Channel<Double>>>();
    String grps[] = {"HV","ADC","TDC"};
    
   
    String detector = null;
	
	public FCEpics(String name){
	    this.appName = name;
	    this.context = new Context();   
	}
	
    public void setApplicationClass(MonitorApp app) {
        this.app = app;
    }
    
    public void setMonitoringClass(DetectorMonitor mon) {
        this.mon = mon;
    }
    
    public String getName() {
        return this.appName;
    }
    
    public JPanel getScalerPane() {        
        HVScalers.setLayout(new BorderLayout());
        JSplitPane HVScalerPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);       
        HVScalerPane.setTopComponent(scaler1DView);
        HVScalerPane.setBottomComponent(scaler2DView);       
        HVScalerPane.setDividerLocation(0.5);
        HVScalers.add(HVScalerPane);
        return HVScalers;       
    } 
    
    public int connectCa(int grp, String action, int sector, int layer, int channel) {
        try {
        caMap.get(action).getItem(grp,sector,layer,channel).connectAsync().get();   
        }
        catch (InterruptedException e) {  
            return -1;
        }        
        catch (ExecutionException e) {  
            return -1;
        }
        return 1;
        
    }
    
    public double getCaValue(int grp, String action, int sector, int layer, int channel) {
        try {
        CompletableFuture<Double> ffd = caMap.get(action).getItem(grp,sector,layer,channel).getAsync();
        return ffd.get(); 
        }
        catch (InterruptedException e) {  
            return -1.0;
        }        
        catch (ExecutionException e) {  
            return -1.0;
        }   
    }
    
    public void setCaNames(int grp) {
        switch (grp) {
        case 0:
        setCaActionNames(grp,"vmon");
        setCaActionNames(grp,"imon");
        setCaActionNames(grp,"vset");     
        break;
        case 1: break;
        case 2: break;
        }
    }
    
    public void setCaActionNames(int grp, String action) {
        
        IndexedList<Channel<Double>> map = new IndexedList<Channel<Double>>(4);
        
        for (int is=1; is<7 ; is++) {
            for (int il=1; il<3 ; il++) {
                for (int ic=1; ic<19; ic++) {
                    String pv = getPvName(grp,action,is,il,ic);
                    map.add(context.createChannel(pv, Double.class),grp,is,il,ic);
                }
            }
        } 
        caMap.put(action,map);
    }
    
    public void setPvNames(String det, int grp) {
        switch (grp) {
            case 0:
            setPvActionNames(det,grp,"vmon");
            setPvActionNames(det,grp,"imon");
            setPvActionNames(det,grp,"vset");
            setPvActionNames(det,grp,"pwonoff"); 
            break;
            case 1: ; break;
            case 2: ; break;
        }
    }
    
    public void setPvActionNames(String det, int grp, String action) {
      
        IndexedList<String> map = new IndexedList<String>(4);
        
        for (int is=1; is<7 ; is++) {
            for (int il=1; il<3 ; il++) {
                for (int ic=1; ic<19; ic++) {
                    map.add(getPvString(det,grp,is,il,ic,action),grp,is,il,ic);
                }
            }
        }
        pvMap.put(action,map);
    }
    
    public String getPvName(int grp, String action, int sector, int layer, int channel) {
        switch (grp) {
        case 0:
        switch (action) {
        case    "vmon": return (String) pvMap.get(action).getItem(grp,sector,layer,channel);
        case    "imon": return (String) pvMap.get(action).getItem(grp,sector,layer,channel); 
        case    "vset": return (String) pvMap.get(action).getItem(grp,sector,layer,channel); 
        case "pwonoff": return (String) pvMap.get(action).getItem(grp,sector,layer,channel); 
        }
        break;
        case 1: break;
        case 2: break;
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
	
	public String getPvString(String det, int grp, int sector, int layer, int channel,String action) {
	    return "B_DET_"+det+"_"+grps[grp]+"_SEC"+sector+"_"+layerToString(det,layer)+"_E"+channelToString(channel)+":"+action;  
	}
	     
}
