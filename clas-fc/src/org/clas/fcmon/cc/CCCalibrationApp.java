package org.clas.fcmon.cc;

import java.util.ArrayList;
import java.util.List;

import org.clas.fcmon.tools.FCApplication;
import org.jlab.detector.calib.tasks.CalibrationEngine;
import org.jlab.detector.calib.utils.CalibrationConstants;

public class CCCalibrationApp extends FCApplication {
    
    ArrayList<CalibrationConstants> list = new ArrayList<CalibrationConstants>();
    
    public CCCalibrationApp(String name , CCPixels ccPix) {
        super(name, ccPix);       
     } 
    
    public void init() {
        doCalib calib = new doCalib();
        calib.init();
     }
    
    private class doCalib extends CalibrationEngine {
        
        doCalib(){
            CalibrationConstants gain = new CalibrationConstants(3,"Mean/F:Error/I:Sigma/F:Serror/F");
            for(int i = 0; i < 18; i++){
                gain.addEntry(1,1,i+1);
            }
            
            gain.setDoubleValue(0.2, "Mean", 1,1,1);
            gain.setDoubleValue(0.3, "Mean", 1,1,2);
            gain.setDoubleValue(0.4, "Mean", 1,1,3);
            gain.setDoubleValue(0.5, "Mean", 1,1,4);
            gain.setDoubleValue(0.6, "Mean", 1,1,5);
            
            gain.setIntValue(4, "Error", 1,1,4);
            
            list.add(gain);

        }
        
        public void init() {
            setCalibPane(this);
        }
        
        public List<CalibrationConstants>  getCalibrationConstants(){
            return list;
        }       
        
        
    }
}
