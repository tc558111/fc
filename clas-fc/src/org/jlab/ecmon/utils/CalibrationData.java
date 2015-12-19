package org.jlab.ecmon.utils;

import java.util.ArrayList;
import java.util.List;

import org.jlab.clas.detector.DetectorDescriptor;
import org.root.histogram.GraphErrors;
import org.root.fitter.DataFitter;
import org.root.func.F1D;

public class CalibrationData {
	
    DetectorDescriptor desc = new DetectorDescriptor();
    
    private List<GraphErrors>  graphs     = new ArrayList<GraphErrors>();
    private List<F1D>          functions  = new ArrayList<F1D>();
	
    public CalibrationData(int sector, int layer, int component){
        this.desc.setSectorLayerComponent(sector, layer, component);
    }	
	
    public DetectorDescriptor getDescriptor(){ return this.desc;}
    
    public void addGraph(double[] data, double[] error){
        double[] xp  = new double[data.length];
        double[] yp  = new double[data.length]; 
        double[] xpe = new double[data.length];
        double[] ype = new double[data.length]; 
        for(int loop = 0; loop < data.length; loop++){
            xp[loop]  = loop; 
            xpe[loop] = 0.;
            yp[loop]  = data[loop];
            ype[loop] = error[loop];
        }

        GraphErrors  graph = new GraphErrors(xp,yp,xpe,ype);   
        graph.setXTitle("Pixel Number");
        graph.setYTitle("Mean ADC");	
        this.graphs.add(graph);
        F1D f1 = new F1D("exp",2,data.length-2);
        this.functions.add(f1);
    }
    
    public void analyze(){
    	DataFitter.FITPRINTOUT=false;
        for(int loop = 0; loop < this.graphs.size(); loop++){
            F1D func = this.functions.get(loop);
            //double [] dataY=this.graphs.get(loop).getDataY().getArray();
            //double [] dataX=this.graphs.get(loop).getDataX().getArray();
            func.setParameter(0, 100.0);
            func.setParameter(1,-0.016);
            func.setParLimits(0,80.,140.);
            func.setParLimits(1,-0.03,-0.014);
            this.graphs.get(loop).fit(this.functions.get(loop));
        }
        
    }
    
    public GraphErrors  getGraph(int index){ return this.graphs.get(index);}
    public F1D          getFunc(int index){ return this.functions.get(index);}
    
}
