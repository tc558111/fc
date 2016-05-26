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
import org.jlab.clasrec.utils.DataBaseLoader;
import org.jlab.geom.detector.ec.ECDetector;
import org.jlab.geom.detector.ec.ECFactory;
import org.jlab.geom.detector.ec.ECLayer;
import org.jlab.geom.prim.Path3D;

import math.geom2d.polygon.Polygons2D;
import math.geom2d.polygon.SimplePolygon2D;

public class PCPixels {
	
	CalDrawDB         pcalDB  = null;
	ECLayer           ecLayer = null;
	ECDetector       detector = null;
	DetectorShape2D    shape  = new DetectorShape2D();
	public Pixel      pixels  = new Pixel();
	public Strip      strips  = new Strip();
	PrintWriter        writer = null;

	public double pc_xpix[][][]   = new double[10][6916][7];
	public double pc_ypix[][][]   = new double[10][6916][7];
	public double pc_xstr[][][][] = new double[8][68][3][7];
	public double pc_ystr[][][][] = new double[8][68][3][7];
	public double pc_cmap[]       = new double[6916];
	public double pc_zmap[]       = new double[6916];
	public    int pc_nvrt[]       = new int[6916];
	public    int pc_nstr[]       = new int[3];
	
	int sup=0;
	
	public PCPixels(String det) {
		
	  detector  = new ECFactory().createDetectorTilted(DataBaseLoader.getGeometryConstants(DetectorType.EC, 10, "default"));
	  if (det=="PCAL")  sup=0;
	  if (det=="ECin")  sup=1;
	  if (det=="ECout") sup=2;
	  for (int suplay=sup ; suplay<sup+1; suplay++) {
  	    for (int layer=0; layer<3; layer++) {
  		  ecLayer = detector.getSector(0).getSuperlayer(suplay).getLayer(layer);
  		  pc_nstr[layer] = ecLayer.getAllComponents().size();
  	    }
  	  }
	  this.pcalDB = new CalDrawDB("PCAL");
	  this.pcGetStripsDB();
	  this.pcGetPixelsDB();
      this.pcpixrot();
//      this.testStrips();
//      this.testPixels();
      System.out.println("PCPixels is done");
	}	
	
	public static void main(String[] args) {
		PCPixels pix = new PCPixels("PCAL");
	}
	
	public void pcGetStripsDB() {
		
		System.out.println("PCPixels:pcGetStripsDB()");	
		
	 	for(int sector = 0; sector < 1; sector++) {
	        System.out.println("pcGetStripsDB: Processing Sector "+sector);
	 		for(int layer=0; layer<3 ; layer++) {
	 			for(int strip = 0; strip < pc_nstr[layer] ; strip++) {
		 			shape = pcalDB.getStripShape(sector, layer, strip);	            
	        		for(int i = 0; i < shape.getShapePath().size(); ++i) {
	                	pc_xstr[i][strip][layer][6] = shape.getShapePath().point(i).x();
	                	pc_ystr[i][strip][layer][6] = shape.getShapePath().point(i).y();
	        		}
	 			}
	 		}
    	}						
	}
	
	public void pcGetPixelsDB() {
		
        System.out.println("PCPixels:pcGetPixelsDB()");
		
		DetectorShape2D shape = new DetectorShape2D();

		for(int sector=0; sector<1 ; sector++) {
    	int pixel = 0; double maxPixArea=0;
    	for(int uStrip = 0; uStrip < pc_nstr[0]; uStrip++) {	 
    		for(int vStrip = 0; vStrip < pc_nstr[1]; vStrip++) {    			 
	            for(int wStrip = 0; wStrip < pc_nstr[2]; wStrip++) {  
	            	shape = pcalDB.getPixelShape(0, uStrip, vStrip, wStrip);
	              	if(shape!=null) {	
	            		pixel++;
	            	    double [] xtemp2 = new double [shape.getShapePath().size()];
	            		double [] ytemp2 = new double [shape.getShapePath().size()];
	            		for(int i = 0; i < shape.getShapePath().size(); ++i) {
	            			xtemp2[i] = shape.getShapePath().point(i).x();
	            			ytemp2[i] = shape.getShapePath().point(i).y();
		                	pc_xpix[i][pixel-1][6] = xtemp2[i];
		                	pc_ypix[i][pixel-1][6] = ytemp2[i];
	            		}
	            		shape.setColor(130,(int)(255*vStrip/pc_nstr[1]),(int)(255*wStrip/pc_nstr[2]));	            		
	            		pc_cmap[pixel-1] = 255*pixel/6916;
	            		pc_zmap[pixel-1] = 1.0;
	            		pc_nvrt[pixel-1] = shape.getShapePath().size();
	            		pixels.addTriplets(pixel, uStrip+1, vStrip+1, wStrip+1);
	            		pixels.addShape(shape, sector, pixel);
	            		double uDist = pcalDB.getUPixelDistance(uStrip, vStrip, wStrip);
	            		double vDist = pcalDB.getVPixelDistance(uStrip, vStrip, wStrip);
	            		double wDist = pcalDB.getWPixelDistance(uStrip, vStrip, wStrip);
	            	    strips.addPixels(sector, 1, uStrip+1, pixel);
	            		strips.addPixels(sector, 2, vStrip+1, pixel);
	            		strips.addPixels(sector, 3, wStrip+1, pixel);
	            	    strips.addPixDist(sector, 1, uStrip+1, (int) (uDist*100));
	            		strips.addPixDist(sector, 2, vStrip+1, (int) (vDist*100));
	            		strips.addPixDist(sector, 3, wStrip+1, (int) (wDist*100));
		        		pixels.addPixDist(uDist,vDist,wDist,pixel);	
		        		SimplePolygon2D pol1 = new SimplePolygon2D(xtemp2,ytemp2);
		        		pixels.addArea(pol1.area(),sector,pixel);
		        		if (pol1.area()>maxPixArea) maxPixArea = pol1.area();
	            	}
	            }
    		}
    	}
    	// Sort pixels in each strip according to distance from edge
    	for (int lay=0; lay<3 ; lay++ ){
    		System.out.println("PCPixels: Sorting pixels in layer "+lay);
    		for(int strip = 0; strip < pc_nstr[lay]; strip++) {
    			strips.getSortedPixels(0, lay+1, strip+1);
    		}
    	}
    	
   	
    	pixels.setMaxPixelArea(maxPixArea);
    	}
	}
	
	public void testPixels() {
		
		DetectorShapeTabView view = new DetectorShapeTabView();
   	 	DetectorShapeView2D UWmap= new DetectorShapeView2D("PCAL Pixel");
   	 	
		this.pcGetPixelsDB();
		this.pcpixrot();
    	
    	for (int sector=0; sector<6; sector++) {
    		for (int ipix=0; ipix<pixels.getNumPixels(); ipix++) {
		        DetectorShape2D  pixx = new DetectorShape2D();
		    	pixx.getShapePath().clear(); 
    			for (int k=0;k<pc_nvrt[ipix];k++) {
    		    	double xrot = pc_xpix[k][ipix][sector]; double yrot = pc_ypix[k][ipix][sector];
            		pixx.getShapePath().addPoint(xrot,yrot,0.0); 
    			}
    			int pixel=ipix+1;
        		int u=pixels.getStrip(1,pixel); int v=pixels.getStrip(2,pixel); int w=pixels.getStrip(3,pixel);
        		pixx.setColor(130,(int)(255*v/pc_nstr[1]),(int)(255*w/pc_nstr[2]));
        		             		
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
	public void testStrips(int lay) {
		
		DetectorShapeTabView view = new DetectorShapeTabView();
   	 	DetectorShapeView2D UWmap= new DetectorShapeView2D("PCAL Pixel");
   	 	
		this.pcGetStripsDB();
		this.pcpixrot();
    	
    	for (int sector=0; sector<6; sector++) {
    		for (int istr=0; istr<pc_nstr[lay]; istr++) {
		        DetectorShape2D  strp = new DetectorShape2D();
		    	strp.getShapePath().clear(); 
    			for (int k=0;k<4;k++) {
    		    	double xrot = pc_xstr[k][istr][lay][sector]; double yrot = pc_ystr[k][istr][lay][sector];
            		strp.getShapePath().addPoint(xrot,yrot,0.0); 
    			}
        		strp.setColor(130,(int)(255*istr/pc_nstr[lay]),30);
        		             		
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
    	System.out.println("PCPixels:pcpixrot():");	
    	double[] theta={0.0,60.0,120.0,180.0,240.0,300.0};
	    	
		for(int is=0; is<6; is++) {
			double thet=theta[is]*3.14159/180.;
    		double ct=Math.cos(thet) ; double st=Math.sin(thet);
			
			for (int lay=0; lay<3 ; lay++) {
				for (int istr=0; istr<pc_nstr[lay]; istr++) {
					for (int k=0;k<4;k++){
						pc_xstr[k][istr][lay][is]= -(pc_xstr[k][istr][lay][6]*ct+pc_ystr[k][istr][lay][6]*st);
						pc_ystr[k][istr][lay][is]=  -pc_xstr[k][istr][lay][6]*st+pc_ystr[k][istr][lay][6]*ct;
					}
				}
			}
		    	   		    	   
			for (int ipix=0; ipix<pixels.getNumPixels(); ipix++) {
				for (int k=0;k<pc_nvrt[ipix];k++) {
					pc_xpix[k][ipix][is]= -(pc_xpix[k][ipix][6]*ct+pc_ypix[k][ipix][6]*st); 
					pc_ypix[k][ipix][is]=  -pc_xpix[k][ipix][6]*st+pc_ypix[k][ipix][6]*ct;
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
