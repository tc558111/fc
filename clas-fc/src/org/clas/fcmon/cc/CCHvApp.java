package org.clas.fcmon.cc;

import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.clas.fcmon.tools.FCHv;
import org.epics.ca.Channel;
import org.epics.ca.Listener;
import org.epics.ca.Monitor;
import org.epics.ca.data.Alarm;
import org.epics.ca.data.Control;
import org.epics.ca.data.Graphic;
import org.epics.ca.data.Timestamped;
import org.jlab.utils.groups.IndexedList;

public class CCHvApp extends FCHv {
	
    CCHvApp(String name) {
        super(name);
    }
    
    public void init() {
        setPvNames("LTCC");
        setCaNames();
    }
    
    public void getPvNames()  throws InterruptedException, ExecutionException {
        for (int is=1; is<7 ; is++) {
            for (int il=1; il<3 ; il++) {
                for (int ic=1; ic<19; ic++) {
                  System.out.println("Sector= "+is+" Layer= "+il+" PMT= "+ic+" vset="+getCaValue("vset",is,il,ic)); 
                }
            }
        }
    }

}
