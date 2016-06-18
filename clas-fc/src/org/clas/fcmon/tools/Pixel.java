package org.clas.fcmon.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.clas.containers.FTHashCollection;
import org.jlab.clas.detector.DetectorCollection;
import org.jlab.clas12.calib.DetectorShape2D;
import org.root.histogram.H1D;
import org.root.histogram.H2D;

public class Pixel {

    FTHashCollection<Integer>        pixelStrips = null;
    FTHashCollection<Boolean>        pixelStatus = null;
    FTHashCollection<int[]>          stripPixels = null;
    FTHashCollection<DetectorShape2D> pixelShape = null;
    FTHashCollection<Double>           pixelArea = null;
    FTHashCollection<Double>          pixelUDist = null;
    FTHashCollection<Double>          pixelVDist = null;
    FTHashCollection<Double>          pixelWDist = null;
    
    DetectorCollection<List<H1D>>       pixelH1D = null;
    DetectorCollection<List<H2D>>       pixelH2D = null;
    
    public TreeMap<String, DetectorCollection<H1D>>   hmap1 = null; 
    public TreeMap<String, DetectorCollection<H2D>>   hmap2 = null; 
    
	double[] theta={0.0,60.0,120.0,180.0,240.0,300.0};
    double maxPixelArea = 0;
    
    public Pixel() {
    	this.pixelStrips = new FTHashCollection<Integer>(3);
    	this.pixelStatus = new FTHashCollection<Boolean>(1);
    	this.stripPixels = new FTHashCollection<int[]>(1);
    	this.pixelShape  = new FTHashCollection<DetectorShape2D>(2);
    	this.pixelArea   = new FTHashCollection<Double>(2);
    	this.pixelUDist  = new FTHashCollection<Double>(1);
    	this.pixelVDist  = new FTHashCollection<Double>(1);
    	this.pixelWDist  = new FTHashCollection<Double>(1);
        this.pixelH1D    = new DetectorCollection<List<H1D>>();
        this.pixelH2D    = new DetectorCollection<List<H2D>>();
        this.hmap1       = new TreeMap<String, DetectorCollection<H1D>>();
        this.hmap2       = new TreeMap<String, DetectorCollection<H2D>>();
    }
    
    public void addTriplets(int pixel, int u, int v, int w) {
    	pixelStrips.add(pixel,u,v,w); 
    	int[] str = {u,v,w};
    	stripPixels.add(str, pixel);
    }
    
    public void addShape(DetectorShape2D shape, int sector, int pixel){
    	pixelShape.add(shape, sector, pixel);
    }
    
    public void addArea(double area, int sector, int pixel){
    	pixelArea.add(area, sector, pixel);
    }
    
    public DetectorShape2D rotatePixel(int sector, int pixel) {
    	DetectorShape2D rotshape = this.getShape(0,pixel);
		double thet=theta[sector]*3.14159/180.;
		double ct=Math.cos(thet) ; double st=Math.sin(thet);
		for(int i = 0; i < this.getShape(0,pixel).getShapePath().size(); ++i) {
			double x = this.getShape(0,pixel).getShapePath().point(i).x();
			double y = this.getShape(0,pixel).getShapePath().point(i).y();
			double xrot = -(x*ct+y*st);
			double yrot =  -x*st+y*ct;
			rotshape.getShapePath().point(i).set(xrot, yrot, 0.0);
		}
		return rotshape;
    }
    
    public void addH1D(int sector, int layer, int component, H1D h1) {
        if(!pixelH1D.hasEntry(sector,layer,component)) pixelH1D.add(sector,layer,component,new ArrayList<H1D>());
        pixelH1D.get(sector,layer,component).add(h1);       
    }
    
    public void addH2D(int sector, int layer, int component, H2D h2) {
        if(!pixelH2D.hasEntry(sector,layer,component)) pixelH2D.add(sector,layer,component,new ArrayList<H2D>());
        pixelH2D.get(sector,layer,component).add(h2);       
    }    
    
    public void addH1DMap(String name, DetectorCollection<H1D> map) {
        this.hmap1.put(name,map);
    }
    
    public void addH2DMap(String name, DetectorCollection<H2D> map) {
        this.hmap2.put(name,map);
    }   
    
    public void setMaxPixelArea(double area) {
    	this.maxPixelArea = area;
    }
    
    public void addPixDist(double udist, double vdist, double wdist, int pixel) {
    	pixelUDist.add(udist,pixel);
    	pixelVDist.add(vdist,pixel);
    	pixelWDist.add(wdist,pixel);
    }
    
    public void setPixelStatus(boolean status, int pixel) {
    	pixelStatus.add(status, pixel);
    }
    
    public boolean getPixelStatus(int pixel) {
    	return pixelStatus.getItem(pixel);
    }
    
    public double getUdist(int pixel) {
    	return pixelUDist.getItem(pixel);
    }
    
    public double getVdist(int pixel) {
    	return pixelVDist.getItem(pixel);
    }
    
    public double getWdist(int pixel) {
    	return pixelWDist.getItem(pixel);
    }
    
    public double[] getDist(int layer) {
    	double[] dist = new double[this.getNumPixels()];
    	for (int pixel=0; pixel<this.getNumPixels(); pixel++) {
      	  if (layer==1) dist[pixel] = this.getUdist(pixel+1);
    	  if (layer==2) dist[pixel] = this.getVdist(pixel+1);
    	  if (layer==3) dist[pixel] = this.getWdist(pixel+1);
    	}
    	return dist;
    }
    
    public double getDist(int layer, int pixel) {
    	double dist = 0;
    	if (layer==1) dist = this.getUdist(pixel);
  	  	if (layer==2) dist = this.getVdist(pixel);
  	  	if (layer==3) dist = this.getWdist(pixel);
  	  	return dist;
    }
    
    public int getPixel(int u, int v, int w) {
    	int pixel=0;
    	if (pixelStrips.hasItem(u,v,w)) pixel = pixelStrips.getItem(u,v,w);
    	return pixel;
    }
    
    public int getNumPixels() {
    	return pixelShape.getMap().size();
    }
    
    public DetectorShape2D getShape(int sector, int pixel) {
    	return pixelShape.getItem(sector,pixel);
    }
    
    public double getArea(int pixel) {
    	return pixelArea.getItem(0,pixel);
    }
    
    public double getNormalizedArea(int pixel) {
    	return pixelArea.getItem(0,pixel)/maxPixelArea;
    }
    
    public int[] getTriplets(int pixel) {
    	return stripPixels.getItem(pixel);		
    }
   
    public int getStrip(int layer, int pixel) {
    	int[] str; 
    	str = stripPixels.getItem(pixel);
    	return str[layer-1];	
    }
    
    public static void main(String[] args) {
    	int[] str ; int pixel;
    	Pixel pix = new Pixel();
    	pix.addTriplets(10, 1, 2, 3);
    	pix.addTriplets(12, 11, 12, 3);
    	pix.addTriplets(2133, 21, 2, 3);
    	pix.pixelStrips.show();
    	pixel = pix.pixelStrips.getItem(1,2,3);    System.out.println(pixel);
    	pixel = pix.pixelStrips.getItem(11,12,3);  System.out.println(pixel);
    	pixel = pix.pixelStrips.getItem(21,2,3);  System.out.println(pixel);
    	str   = pix.stripPixels.getItem(10) ;     System.out.println(str[0]+" "+str[1]+" "+str[2]); 
    	str   = pix.stripPixels.getItem(12) ;     System.out.println(str[0]+" "+str[1]+" "+str[2]); 
    	str   = pix.stripPixels.getItem(2133);     System.out.println(str[0]+" "+str[1]+" "+str[2]);
    	int ustr  = pix.getStrip(1,2133); int vstr=pix.getStrip(2, 2133); int wstr=pix.getStrip(3, 2133);
    	System.out.println("u,v,w="+ustr+" "+vstr+" "+wstr);
    	System.out.println(pix.pixelStrips.getMap().size());
    }

}
