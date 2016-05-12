package org.clas.fcmon.tools;

import org.clas.containers.FTHashCollection;
import org.root.histogram.H1D;
import org.root.histogram.H2D;

public class Pixel {

    private FTHashCollection<Integer> pixelStrips = null;
    private FTHashCollection<int[]>   stripPixels = null;
    private FTHashCollection<H1D>        pixelH1D = null;
    private FTHashCollection<H2D>        pixelH2D = null;
    
    public Pixel() {
    	this.pixelStrips = new FTHashCollection(3);
    	this.stripPixels = new FTHashCollection(1);
    	this.pixelH1D    = new FTHashCollection(1);
    	this.pixelH2D    = new FTHashCollection(1);
    }
    
    public void addStrips(int pixel, int u, int v, int w) {
    	pixelStrips.add(pixel,u,v,w); 
    	int[] str = {u,v,w};
    	stripPixels.add(str, pixel);
    }
    
    public void addH1D(H1D h1, int pixel) {
    	pixelH1D.add(h1, pixel);   	
    }
    
    public void addH2D(H2D h2, int pixel) {
    	pixelH2D.add(h2, pixel);   	
    }
    
    public static void main(String[] args) {
    	int[] str ; int pixel;
    	Pixel pix = new Pixel();
    	pix.addStrips(10, 1, 2, 3);
    	pix.addStrips(12, 11, 2, 3);
    	pix.addStrips(11, 21, 2, 3);
    	pix.addStrips(130, 11, 1, 1);
    	pix.addStrips(120, 31, 22, 3);
    	pix.addStrips(2133, 11, 12, 3);
    	pix.addStrips(2111, 31, 2, 33);
    	pix.addStrips(44, 21, 12, 13);
    	pix.addStrips(45, 7, 2, 3);
    	pix.addStrips(66, 8, 2, 3);
    	pix.pixelStrips.show();
    	pixel = pix.pixelStrips.getItem(1,2,3);    System.out.println(pixel);
    	pixel = pix.pixelStrips.getItem(11,12,3);  System.out.println(pixel);
    	pixel = pix.pixelStrips.getItem(21,12,13); System.out.println(pixel);
    	pixel = pix.pixelStrips.getItem(8,2,3);    System.out.println(pixel);
    	pixel = pix.pixelStrips.getItem(31,22,3);  System.out.println(pixel);
    	str   = pix.stripPixels.getItem(130) ;     System.out.println(str[0]+" "+str[1]+" "+str[2]); 
    	str   = pix.stripPixels.getItem(120) ;     System.out.println(str[0]+" "+str[1]+" "+str[2]); 
    	str   = pix.stripPixels.getItem(2133);     System.out.println(str[0]+" "+str[1]+" "+str[2]);
    }

}
