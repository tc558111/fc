package org.clas.fcmon.tools;

import java.awt.BorderLayout;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JSplitPane;

import org.jlab.clas.detector.DetectorDescriptor;
import org.jlab.clas12.calib.DetectorShape2D;

import math.geom2d.polygon.SimplePolygon2D;

public class PCPixels {
	
	CalDrawDB         pcalDB  = null;
	DetectorShape2D    shape  = new DetectorShape2D();
 	DetectorShapeView2D Umap  = new DetectorShapeView2D("PCAL U");
 	DetectorShapeView2D Vmap  = new DetectorShapeView2D("PCAL V");
 	DetectorShapeView2D Wmap  = new DetectorShapeView2D("PCAL W");
	DetectorShapeTabView view = new DetectorShapeTabView();
	public Pixel      pixels  = new Pixel();
	public Strip      strips  = new Strip();
	
	PrintWriter    writer = null;

	public double pc_xpix[][][]   = new double[8][6800][7];
	public double pc_ypix[][][]   = new double[8][6800][7];
	public double pc_xstr[][][][] = new double[8][68][3][7];
	public double pc_ystr[][][][] = new double[8][68][3][7];
	
	public PCPixels() {
	  System.out.println("Initializing pixels");	
	  this.pcalDB = new CalDrawDB("PCAL");
	  this.pcstrDB();
      this.pcpixDB();
      System.out.println("PCPixels is done");
	}	
	
	public static void main(String[] args) {
		PCPixels pix = new PCPixels();
	}
	
	public void pcstrDB() {
		
		int pixel = 0;
		int[]     str = {68,62,62};
		String[] sstr = {"u","v","w"};
		
	 	for(int sector = 0; sector < 1; sector++) {
	        System.out.println("pcstrDB: Processing Sector "+sector);
	 		for(int layer=0; layer<3 ; layer++) {
	 			for(int strip = 0; strip < str[layer] ; strip++) {
	 				shape = pcalDB.getStripShape(sector, sstr[layer], strip);	            
	        		for(int i = 0; i < shape.getShapePath().size(); ++i) {
	                	pc_xstr[i][strip][layer][6] = shape.getShapePath().point(i).x();
	                	pc_ystr[i][strip][layer][6] = shape.getShapePath().point(i).y();
	        		}
	 			}
	 		}
    	}		
				
	}
	
	public void pcpixDB() {
		
		int pixel=0;
   	 	DetectorShapeView2D UWmap= new DetectorShapeView2D("PCAL Pixel");
		
		for(int sector = 0; sector < 1; sector++)
    	{
        System.out.println("pcpixDB: Processing Sector "+sector);
    	for(int uStrip = 0; uStrip < 68; uStrip++) {	 
    		for(int vStrip = 0; vStrip < 62; vStrip++) {    			 
	            for(int wStrip = 0; wStrip < 62; wStrip++) {         	 
	            	if(pcalDB.isValidPixel(sector, uStrip, vStrip, wStrip)) {
	            		shape = pcalDB.getPixelShape(sector, uStrip, vStrip, wStrip);
	            		pixel++;
	            		pixels.addStrips(pixel, uStrip, vStrip, wStrip);
	            		strips.addPixels(sector, 1, uStrip, pixel);
	            		strips.addPixels(sector, 2, vStrip, pixel);
	            		strips.addPixels(sector, 3, wStrip, pixel);
	            		for(int i = 0; i < shape.getShapePath().size(); ++i) {
	            			pc_xpix[i][pixel-1][6] = shape.getShapePath().point(i).x(); 
	            			pc_ypix[i][pixel-1][6] = shape.getShapePath().point(i).y();
	            			double x = shape.getShapePath().point(i).x();
			            	double y = shape.getShapePath().point(i).y();
        					shape.getShapePath().point(i).set(x, y, 0.0);
	            			//System.out.println("i,u,v,w,pix,x,y= "+i+" "+uStrip+" "+vStrip+" "+wStrip+" "+pixel);
        				} 
	            		shape.setColor(130,(int)(255*vStrip/62.),(int)(255*wStrip/62.));
	            		UWmap.addShape(shape);
	            	}
	            }
            }
    	}
    	}		
    	view.addDetectorLayer(UWmap);
    	JFrame hi = new JFrame();
		hi.setLayout(new BorderLayout());
	    hi.add(view,BorderLayout.CENTER);
	    hi.pack();
	    hi.setVisible(true);		
	}
	
    public void pcpixrot() {
    	System.out.println("pcpixrot():");	
    	double[] theta={0.0,60.0,120.0,180.0,240.0,300.0};
	    	
		for(int is=0; is<6; is++) {
			double thet=theta[is]*3.14159/180.;
			
			for (int lay=0; lay<3 ; lay++) {
				for (int istr=0; istr<68; istr++) {
					for (int k=0;k<4;k++){
						pc_xstr[k][istr][lay][is]= -(pc_xstr[k][istr][lay][6]*Math.cos(thet)+pc_ystr[k][istr][lay][6]*Math.sin(thet));
						pc_ystr[k][istr][lay][is]=  -pc_xstr[k][istr][lay][6]*Math.sin(thet)+pc_ystr[k][istr][lay][6]*Math.cos(thet);
					}
				}
			}
		    	   		    	   
			for (int ipix=0; ipix<6765; ipix++) {
				for (int k=0;k<3;k++) {
					pc_xpix[k][ipix][is]= -(pc_xpix[k][ipix][6]*Math.cos(thet)+pc_ypix[k][ipix][6]*Math.sin(thet));
					pc_ypix[k][ipix][is]=  -pc_xpix[k][ipix][6]*Math.sin(thet)+pc_ypix[k][ipix][6]*Math.cos(thet);
				}
			}	
		}	    	
	 }
				
     public float uvw_dalitz(int ic, int il, int ip) {
			float uvw=0;
			switch (ic) {
			case 0: //PCAL
				if (il==1&&ip<=52) uvw=(float)ip/84;
				if (il==1&&ip>52)  uvw=(float)(52+(ip-52)*2)/84;
				if (il==2&&ip<=15) uvw=(float) 2*ip/77;
				if (il==2&&ip>15)  uvw=(float)(30+(ip-15))/77;
				if (il==3&&ip<=15) uvw=(float) 2*ip/77;
				if (il==3&&ip>15)  uvw=(float)(30+(ip-15))/77;
				break;
			case 1: //ECALinner
				uvw=(float)ip/36;
				break;
			case 2: //ECALouter
				uvw=(float)ip/36;
				break;
			}
			return uvw;
     }
	
}
