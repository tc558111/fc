package org.jlab.ecmon.utils;

import java.util.ArrayList;
import java.util.List;

import org.jlab.clas.detector.DetectorDescriptor;
import org.root.histogram.GraphErrors;
import org.root.data.DataSetXY;
import org.root.fitter.DataFitter;
import org.root.func.F1D;

public class CalibrationData {
	
    DetectorDescriptor desc = new DetectorDescriptor();
    
    private List<GraphErrors>  graphs     = new ArrayList<GraphErrors>();
    private List<F1D>          functions  = new ArrayList<F1D>();
    private List<Double>  chi2            = new ArrayList<Double>(); 
    private int dataSize; 
    
    public CalibrationData(int sector, int layer, int component){
        this.desc.setSectorLayerComponent(sector, layer, component);
    }	
	
    public DetectorDescriptor getDescriptor(){ return this.desc;}
    
    public void addGraph(double[] data, double[] error){
		String otab[]={"Ui Strip","Vi Strip","Wi Strip","Uo Strip","Vo Strip","Wo Strip"};
		
		dataSize = data.length;
		int min=2 , max=dataSize-2;
		if (dataSize==1) {min=0 ; max=dataSize;}
		if (dataSize==3) {min=0 ; max=dataSize;}
		if (dataSize==5) {min=0 ; max=dataSize;}
		if (dataSize==7) {min=1 ; max=dataSize-1;}
		int size = max-min;
        double[] xp  = new double[size];
        double[] yp  = new double[size]; 
        double[] xpe = new double[size];
        double[] ype = new double[size]; 
        int n=0;
        for(int loop = min; loop < max; loop++){
            xp[n]  = loop; 
            xpe[n] = 0.;
            yp[n]  = data[loop];
            ype[n] = error[loop];
            n++;
        }

        GraphErrors  graph = new GraphErrors(xp,yp,xpe,ype);   
        graph.setXTitle("Pixel Number");
        graph.setYTitle("Mean ADC");
        graph.setMarkerStyle(2);
        graph.setMarkerSize(8);
        int sector=getDescriptor().getSector()+1;
        int   view=getDescriptor().getLayer();
        int  strip=getDescriptor().getComponent()+1;
        graph.setTitle("EXP FIT: Sector "+sector+" "+otab[view-1]+""+strip);
        this.graphs.add(graph);
        F1D f1 = new F1D("exp",0,max);
        this.functions.add(f1);
    }
    
    public void analyze(){
    	DataFitter.FITPRINTOUT=false;
        for(int loop = 0; loop < this.graphs.size(); loop++){
            F1D func = this.functions.get(loop);
            func.setParameter(0,0.);
            func.setParameter(1,0.);
            double [] dataY=this.graphs.get(loop).getDataY().getArray();
            //double [] dataX=this.graphs.get(loop).getDataX().getArray();
            if (dataY.length>0) {
            	int imax = Math.min(2,dataY.length-1);
            	double p0try = dataY[imax] ; double p0min = p0try-30. ; double p0max=p0try+30.;
            	func.setParameter(0, p0try);
            	func.setParameter(1,-0.0144);
            	func.setParLimits(0,p0min,p0max);
            	//func.setParLimits(1,-0.03,-0.014);
            	func.setParLimits(1,-0.04,-0.001);
            	func.setLineColor(2);
            	this.graphs.get(loop).fit(this.functions.get(loop));
            	chi2.add(DataFitter.getChiSquareFunc(this.graphs.get(loop),func));
            	//chi2.add(func.getChiSquare(func.getDataSet(func.getDataSize())));
            }
        }
        
    }
    
    public GraphErrors  getGraph(int index){return this.graphs.get(index);}
    public F1D          getFunc(int index) {return this.functions.get(index);}
    public double       getChi2(int index) {return this.chi2.get(index);}
}
