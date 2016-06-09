/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.fcmon.tools;

import static java.lang.Integer.parseInt;
import java.util.ArrayList;
import java.util.TreeMap;
import org.jlab.clas.detector.DetectorCollection;
import org.root.func.F1D;
import org.root.group.TDirectory;
import org.root.histogram.GraphErrors;
import org.root.histogram.H1D;
import org.root.histogram.H2D;

/**
 *
 * @author fanchini
 * @modified lcsmith
 */
public class FCCalibrationData {
    HipoFile hipofile;
    
    public void getFile(String filename){
        HipoFile file = new HipoFile(filename);
        this.hipofile = file;
    }
    
    public DetectorCollection getCollection(String dirname){
    	DetectorCollection DC = new DetectorCollection();
    	ArrayList arrayl = this.hipofile.getArrayList(dirname);
    	for (Object obj : arrayl) {
    		String obj_label = getObjlabel(obj);
    		DC.add(getIndex(obj_label,"_s"),getIndex(obj_label,"_l"),getIndex(obj_label,"_c"),obj);
    	}    
    return DC;
}    
    
    
    private String getObjlabel(Object obj){
        String obj_label ="";
        H1D h_1d; 
        H2D h_2d;
        F1D f_1d; 
        GraphErrors g_err;
        if(obj instanceof H1D){
                h_1d = (H1D) obj;
                obj_label = h_1d.getName();
        }
        if(obj instanceof H2D){
                h_2d = (H2D) obj;
                obj_label = h_2d.getName();
        }
        if(obj instanceof F1D){
                f_1d = (F1D) obj;
                obj_label = f_1d.getName();
        }
        if(obj instanceof GraphErrors){
                g_err = (GraphErrors) obj;
                obj_label = g_err.getName();
        }
        return obj_label;
    } 

    private int getIndex(String name, String lab){
    	int i1=name.indexOf(lab)+2;
        return parseInt(name.substring(i1,i1+1));
    }    

}
