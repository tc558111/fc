package org.clas.fcmon.cc;

import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.clas.fcmon.tools.FCEpics;
import org.epics.ca.Channel;
import org.epics.ca.Listener;
import org.epics.ca.Monitor;
import org.epics.ca.data.Alarm;
import org.epics.ca.data.Control;
import org.epics.ca.data.Graphic;
import org.epics.ca.data.Timestamped;
import org.jlab.detector.base.DetectorDescriptor;
import org.jlab.utils.groups.IndexedList;
import org.root.basic.EmbeddedCanvas;

public class CCHvApp extends FCEpics {
	
    CCHvApp(String name) {
        super(name);
    }
    
    public void init() {
        setPvNames("LTCC",0);
        setCaNames(0);
    }
    
    public void getPvNames()  throws InterruptedException, ExecutionException {
        for (int is=1; is<7 ; is++) {
            for (int il=1; il<3 ; il++) {
                for (int ic=1; ic<19; ic++) {
                  System.out.println("Sector= "+is+" Layer= "+il+" PMT= "+ic+" vset="+getCaValue(0,"vset",is,il,ic)); 
                }
            }
        }
    }
    
    public void updateCanvas(DetectorDescriptor dd) {
        
        EmbeddedCanvas canvas = this.getCanvas(this.getName()); 
        
        int is = dd.getSector();
        int lr = dd.getLayer();
        int ip = dd.getComponent();   
  
        
        
        
    }

}
