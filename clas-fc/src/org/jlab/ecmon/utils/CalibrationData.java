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
    
    private List<GraphErrors>  rawgraphs  = new ArrayList<GraphErrors>();
    private List<GraphErrors>  fitgraphs  = new ArrayList<GraphErrors>();
    private List<F1D>          functions  = new ArrayList<F1D>();
    private List<Double>             chi2 = new ArrayList<Double>(); 
    private int dataSize; 
    private int fitSize;
    
    public CalibrationData(int sector, int layer, int component){
        this.desc.setSectorLayerComponent(sector, layer, component);
    }	
	
    public DetectorDescriptor getDescriptor(){ return this.desc;}
    
    public void addGraph(double[] data, double[] error){
    	
    	GraphErrors graph;
		String otab[]={"U Inner Strip","V Inner Strip","W Inner Strip","U Outer Strip","V Outer Strip","W Outer Strip"};
		
		dataSize = data.length;
		int n, min=2 , max=dataSize-2;
		
		if (dataSize==1) {min=0 ; max=1;}
		if (dataSize==3) {min=1 ; max=2;}
		if (dataSize==5) {min=1 ; max=dataSize-1;}
		if (dataSize==7) {min=1 ; max=dataSize-1;}
		
        double[] xpraw  = new double[dataSize];
        double[] ypraw  = new double[dataSize]; 
        double[] xprawe = new double[dataSize];
        double[] yprawe = new double[dataSize]; 
 
        // For raw graph
        n=0;
        for(int loop = 0; loop < data.length; loop++) {
        	if(loop>=min&&loop<max&&data[loop]>20) n++;        		
    		xpraw[loop]  = loop; 
    		xprawe[loop] = 0.;
    		ypraw[loop]  = data[loop];
    		yprawe[loop] = Math.max(0.1,error[loop]); 
    		//if(getDescriptor().getSector()==1){
    		//System.out.println("loop,il,ic,ypraw,ypawre = "+loop+" "+getDescriptor().getLayer()+" "+getDescriptor().getComponent()+" "+ypraw[loop]+" "+yprawe[loop]);
    		//}
        }
        
        double[] xpfit  = new double[n];
        double[] ypfit  = new double[n]; 
        double[] xpfite = new double[n];
        double[] ypfite = new double[n];  
        
        fitSize = n;
        // For fit graph
        n=0;
        for(int loop = 0; loop < data.length; loop++){
        	if(loop>=min&&loop<max&&data[loop]>20) {      		
         		xpfit[n]  = xpraw[loop]; 
        		xpfite[n] = xprawe[loop];
        		ypfit[n]  = ypraw[loop];
        		ypfite[n] = yprawe[loop];
        		n++;
        	}
        }

        graph = new GraphErrors(xpfit,ypfit,xpfite,ypfite);   
        graph.setXTitle("Pixel Number");
        graph.setYTitle("Mean ADC");
        graph.setMarkerStyle(2);
        graph.setMarkerSize(8);
        
        int sector=getDescriptor().getSector()+1;
        int   view=getDescriptor().getLayer();
        int  strip=getDescriptor().getComponent()+1;
        
        graph.setTitle("EXP FIT: Sector "+sector+" "+otab[view-1]+""+strip);        
        this.fitgraphs.add(graph);
        
        graph = new GraphErrors(xpraw,ypraw,xprawe,yprawe);   
        graph.setXTitle("Pixel Number");
        graph.setYTitle("Mean ADC");
        graph.setMarkerStyle(2);  
        graph.setMarkerSize(6); 
        graph.setMarkerColor(4);
        
        graph.setTitle("EXP FIT: Sector "+sector+" "+otab[view-1]+" "+strip);        
        this.rawgraphs.add(graph);
        
        F1D f1 = new F1D("exp",0,max);
        this.functions.add(f1);
    }
    
    public void analyze(){
    	DataFitter.FITPRINTOUT=false;
        for(int loop = 0; loop < this.fitgraphs.size(); loop++){
            F1D func = this.functions.get(loop);
            func.setParameter(0,0.);
            func.setParameter(1,0.);
            double [] dataY=this.fitgraphs.get(loop).getDataY().getArray();
            if (dataY.length>0) {
            	int imax = Math.min(2,dataY.length-1);
            	double p0try = dataY[imax] ; double p0min = p0try-30. ; double p0max=p0try+30.;
            	func.setParameter(0, p0try);
            	func.setParameter(1,-0.0144);
            	func.setParLimits(0,p0min,p0max);
            	func.setParLimits(1,-0.5,-0.00001);
            	func.setLineColor(2);
 
            	this.fitgraphs.get(loop).fit(this.functions.get(loop),"REQ");	//Fit data
            	double yfit,ydat,yerr,ch=0;
            	if (fitSize>3) {
            		for (int i=0 ; i<fitSize ; i++) {
            			yfit = func.eval(this.fitgraphs.get(loop).getDataX(i));
            			ydat = this.fitgraphs.get(loop).getDataY(i);
            			yerr = this.fitgraphs.get(loop).getErrorY(i);
            			ch   = ch + Math.pow((ydat-yfit)/yerr,2);
            		}
            		ch = ch/(fitSize-func.getNParams()-1);
            	}
            	this.chi2.add(ch);
            }
        }
        
    }
    
    public GraphErrors  getFitGraph(int index){return this.fitgraphs.get(index);}
    public GraphErrors  getRawGraph(int index){return this.rawgraphs.get(index);}
    public F1D          getFunc(int index) {return this.functions.get(index);}
    public double       getChi2(int index) {if (this.chi2.isEmpty()==false) return this.chi2.get(index); else return 0.;}
}
