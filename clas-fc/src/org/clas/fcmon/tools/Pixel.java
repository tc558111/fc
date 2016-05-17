package org.clas.fcmon.tools;

import org.clas.containers.FTHashCollection;
import org.jlab.clas12.calib.DetectorShape2D;
import org.root.histogram.H1D;
import org.root.histogram.H2D;

public class Pixel {

    FTHashCollection<Integer>        pixelStrips = null;
    FTHashCollection<int[]>          stripPixels = null;
    FTHashCollection<H1D>               pixelH1D = null;
    FTHashCollection<H2D>               pixelH2D = null;
    FTHashCollection<DetectorShape2D> pixelShape = null;
    
	double[] theta={0.0,60.0,120.0,180.0,240.0,300.0};
    
    public Pixel() {
    	this.pixelStrips = new FTHashCollection(3);
    	this.stripPixels = new FTHashCollection(1);
    	this.pixelH1D    = new FTHashCollection(1);
    	this.pixelH2D    = new FTHashCollection(1);
    	this.pixelShape  = new FTHashCollection(2);
    }
    
    public void addTriplets(int pixel, int u, int v, int w) {
    	pixelStrips.add(pixel,u,v,w); 
    	int[] str = {u,v,w};
    	stripPixels.add(str, pixel);
    }
    
    public void addShape(DetectorShape2D shape, int sector, int pixel){
    	pixelShape.add(shape, sector, pixel);
    }
    
    public DetectorShape2D getShape(int sector, int pixel) {
    	return pixelShape.getItem(sector,pixel);
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
    
    public int getNumPixels() {
    	return pixelShape.getMap().size();
    }
    
    public void addH1D(H1D h1, int pixel) {
    	pixelH1D.add(h1, pixel);   	
    }
    
    public void addH2D(H2D h2, int pixel) {
    	pixelH2D.add(h2, pixel);   	
    }
    
    public int getPixel(int u, int v, int w) {
    	int pixel=0;
    	if (pixelStrips.hasItem(u,v,w)) pixel = pixelStrips.getItem(u,v,w);
    	return pixel;
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
