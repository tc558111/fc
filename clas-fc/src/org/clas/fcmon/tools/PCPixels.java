package org.clas.fcmon.tools;

import java.awt.BorderLayout;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JSplitPane;

import org.jlab.clas.detector.DetectorDescriptor;
import org.jlab.clas.detector.DetectorType;
import org.jlab.clas12.calib.DetectorShape2D;
import org.jlab.geom.prim.Path3D;

import math.geom2d.polygon.Polygons2D;
import math.geom2d.polygon.SimplePolygon2D;

public class PCPixels {
	
	CalDrawDB         pcalDB  = null;
	DetectorShape2D    shape  = new DetectorShape2D();
	DetectorShapeTabView view = new DetectorShapeTabView();
	public Pixel      pixels  = new Pixel();
	public Strip      strips  = new Strip();
	
	PrintWriter    writer = null;

	public double pc_xpix[][][]   = new double[10][6916][7];
	public double pc_ypix[][][]   = new double[10][6916][7];
	public double pc_xstr[][][][] = new double[8][68][3][7];
	public double pc_ystr[][][][] = new double[8][68][3][7];
	public double pc_cmap[]       = new double[6916];
	public double pc_zmap[]       = new double[6916];
	public    int pc_nvrt[][]     = new int[6916][7];
	
	public PCPixels() {
	  System.out.println("Initializing pixels");	
	  this.pcalDB = new CalDrawDB("PCAL");
	  this.pcGetStripsDB();
	  this.pcGetPixelsDB();
      this.pcpixrot();
//      this.testStrips();
//      this.testPixels();
      System.out.println("PCPixels is done");
	}	
	
	public static void main(String[] args) {
		PCPixels pix = new PCPixels();
	}
	
	public void pcGetStripsDB() {
		
		int[]     str = {68,62,62};
		String[] sstr = {"u","v","w"};
		
	 	for(int sector = 0; sector < 1; sector++) {
	        System.out.println("pcGetStripsDB: Processing Sector "+sector);
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
	
	public void pcGetPixelsDB() {
		
		DetectorShape2D shape = new DetectorShape2D();
		
        System.out.println("pcGetPixelsDB: Processing pixels ");
    	for(int sector=0; sector<1 ; sector++) {
    	int pixel = 0;
    	for(int uStrip = 0; uStrip < 68; uStrip++) {	 
    		for(int vStrip = 0; vStrip < 62; vStrip++) {    			 
	            for(int wStrip = 0; wStrip < 62; wStrip++) {  
	            	shape = pcalDB.getPixelShape(0, uStrip, vStrip, wStrip);
	              	if(shape!=null) {	
	            		shape.setColor(130,(int)(255*vStrip/62.),(int)(255*wStrip/62.));	            		
	            		pixel++;
	            		pixels.addTriplets(pixel, uStrip+1, vStrip+1, wStrip+1);
	            		pixels.addShape(shape, sector, pixel);
	            	    strips.addPixels(sector, 1, uStrip+1, pixel);
	            		strips.addPixels(sector, 2, vStrip+1, pixel);
	            		strips.addPixels(sector, 3, wStrip+1, pixel);
	            		pc_cmap[pixel-1] = 255*pixel/6916;
	            		pc_zmap[pixel-1] = 1.0;
	            		pc_nvrt[pixel-1][6] = (int) shape.getShapePath().size();
	            		for(int i = 0; i < shape.getShapePath().size(); ++i) {
	            			double x = shape.getShapePath().point(i).x();
			            	double y = shape.getShapePath().point(i).y();
		                	pc_xpix[i][pixel-1][6] = x;
		                	pc_ypix[i][pixel-1][6] = y;
	            			//System.out.println("i,u,v,w,pix,x,y= "+i+" "+(uStrip+1)+" "+(vStrip+1)+" "+(wStrip+1)+" "+pixel+" "+x+" "+y);
	            		}
	            	}
	            }
    		}
    	}
    	}
	}
	
	public void testPixels() {
		
   	 	DetectorShapeView2D UWmap= new DetectorShapeView2D("PCAL Pixel");
   	 	
		this.pcGetPixelsDB();
		this.pcpixrot();
    	
    	for (int sector=0; sector<6; sector++) {
    		for (int ipix=0; ipix<6916; ipix++) {
		        DetectorShape2D  pixx = new DetectorShape2D();
		    	pixx.getShapePath().clear(); 
    			for (int k=0;k<pc_nvrt[ipix][6];k++) {
    		    	double xrot = pc_xpix[k][ipix][sector]; double yrot = pc_ypix[k][ipix][sector];
            		pixx.getShapePath().addPoint(xrot,yrot,0.0); 
    			}
    			int pixel=ipix+1;
        		int u=pixels.getStrip(1,pixel); int v=pixels.getStrip(2,pixel); int w=pixels.getStrip(3,pixel);
        		pixx.setColor(130,(int)(255*v/62.),(int)(255*w/62.));
        		             		
        		UWmap.addShape(pixx);    			
    		}
    	}
   
    	view.addDetectorLayer(UWmap);
    	JFrame hi = new JFrame();
		hi.setLayout(new BorderLayout());
	    hi.add(view,BorderLayout.CENTER);
	    hi.pack();
	    hi.setVisible(true);
	    
	}
	public void testStrips() {
		
   	 	DetectorShapeView2D UWmap= new DetectorShapeView2D("PCAL Pixel");
   	 	
		this.pcGetStripsDB();
		this.pcpixrot();
    	
    	for (int sector=0; sector<6; sector++) {
    		for (int istr=0; istr<62; istr++) {
		        DetectorShape2D  strp = new DetectorShape2D();
		    	strp.getShapePath().clear(); 
    			for (int k=0;k<4;k++) {
    		    	double xrot = pc_xstr[k][istr][1][sector]; double yrot = pc_ystr[k][istr][1][sector];
            		strp.getShapePath().addPoint(xrot,yrot,0.0); 
    			}
        		strp.setColor(130,(int)(255*istr/68.),30);
        		             		
        		UWmap.addShape(strp);    			
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
    		double ct=Math.cos(thet) ; double st=Math.sin(thet);
			
			for (int lay=0; lay<3 ; lay++) {
				for (int istr=0; istr<68; istr++) {
					for (int k=0;k<4;k++){
						pc_xstr[k][istr][lay][is]= -(pc_xstr[k][istr][lay][6]*Math.cos(thet)+pc_ystr[k][istr][lay][6]*Math.sin(thet));
						pc_ystr[k][istr][lay][is]=  -pc_xstr[k][istr][lay][6]*Math.sin(thet)+pc_ystr[k][istr][lay][6]*Math.cos(thet);
					}
				}
			}
		    	   		    	   
			for (int ipix=0; ipix<6916; ipix++) {
				for (int k=0;k<pc_nvrt[ipix][6];k++) {
                	double x = pc_xpix[k][ipix][6] ;  
                	double y = pc_ypix[k][ipix][6] ;
        			double xrot = -(x*ct+y*st);
        			double yrot =  -x*st+y*ct;
					pc_xpix[k][ipix][is]= xrot;
					pc_ypix[k][ipix][is]= yrot;
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
