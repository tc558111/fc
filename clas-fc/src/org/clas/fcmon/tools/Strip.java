package org.clas.fcmon.tools;

 
import java.util.ArrayList;

import org.jlab.clas.detector.DetectorCollection;
import org.root.histogram.H1D;
import org.root.histogram.H2D;

public class Strip {

	public  DetectorCollection<ArrayList<Integer>>      pix = null;
    private DetectorCollection<H1D>                stripH1D = null;
    private DetectorCollection<H2D>                stripH2D = null;
    
    public Strip() {
    	this.pix       = new DetectorCollection<ArrayList<Integer>>();
    	this.stripH1D  = new DetectorCollection<H1D>();
    	this.stripH2D  = new DetectorCollection<H2D>();
    }
    
    public void addPixels(int sector, int layer, int component, int pixel) {
    	if (!pix.hasEntry(sector, layer, component)) pix.add(sector,layer,component,new ArrayList<Integer>());
    	this.pix.get(sector,layer,component).add(pixel);
    }
    
    public void addH1D(int sector, int layer, int component, H1D h1) {
    	stripH1D.add(sector,layer,component,h1);   	
    }
    
    public void addH2D(int sector, int layer, int component, H2D h2) {
    	stripH2D.add(sector,layer,component,h2);   	
    }
    
	public void putPixels(int view, int strip, int val, double[] in){
		int numpix,a,b,c,sum,pixel=1;
		numpix = 2*strip-1;
		a = strip;
		b = 37-a;
		c = 36;
		
		for (int j=0; j<numpix ; j++) {
			if (view==1) pixel=a*(a-1)+b-c+1;
			if (view==2) pixel=c*(c-1)+a-b+1;
			if (view==3) pixel=b*(b-1)+c-a+1;
			if (view==4) pixel=a*(a-1)+b-c+1;
			if (view==5) pixel=c*(c-1)+a-b+1;
			if (view==6) pixel=b*(b-1)+c-a+1;
			if (in[pixel-1]==0) in[pixel-1] = val;
			sum = a+b+c;
			if(sum==73) b=b+1;
			if(sum==74) c=c-1;
		}
		
	}	
	
    public void putpixels(int layer, int strip, int val, double[] in) {
    	Integer[] dum = this.getPixels(0,layer,strip); 
    	for (int j=0; j<dum.length; j++){
    		int pixel = dum[j];
    		if (in[pixel-1]==0) in[pixel-1] = val;
    	}
    } 
    
    public double[] getpixels(int layer, int strip, double[] in) {
    	Integer[] dum = this.getPixels(0,layer,strip); 
    	double[] out = new double[dum.length];
    	for (int j=0; j<dum.length; j++) {
    		int pixel = dum[j];
    		out[j] = in[pixel-1];
    	}
    	return out;
    }
   
    public Integer[] getPixels(int sector, int layer, int component) {
    	Integer list[] = new Integer[pix.get(sector,layer,component).size()];
    	return pix.get(sector,layer,component).toArray(list);
    }
}