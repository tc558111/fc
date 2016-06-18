package org.clas.fcmon.tools;

import java.awt.BorderLayout;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JSplitPane;

import org.jlab.clas.detector.DetectorCollection;
import org.jlab.clas.detector.DetectorDescriptor;
import org.jlab.clas.detector.DetectorType;
import org.jlab.clas12.calib.DetectorShape2D;
import org.jlab.clasrec.utils.DataBaseLoader;
import org.jlab.geom.detector.ec.ECDetector;
import org.jlab.geom.detector.ec.ECFactory;
import org.jlab.geom.detector.ec.ECLayer;
import org.jlab.geom.prim.Path3D;
import org.root.histogram.H1D;
import org.root.histogram.H2D;

import math.geom2d.polygon.Polygons2D;
import math.geom2d.polygon.SimplePolygon2D;

public class ECPixels {
	
	CalDrawDB          calDB  = null;
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
	public    int pc_nstr[]       = {36,36,36};
	
	int sup=0;
	
	public ECPixels(String det) {
		
	  detector  = new ECFactory().createDetectorTilted(DataBaseLoader.getGeometryConstants(DetectorType.EC, 10, "default"));
	  if (det=="PCAL")   sup=0;
	  if (det=="ECin")   sup=1;
	  if (det=="ECout")  sup=2;
	  for (int suplay=sup ; suplay<sup+1; suplay++) {
  	    for (int layer=0; layer<3; layer++) {
  		  ecLayer = detector.getSector(0).getSuperlayer(suplay).getLayer(layer);
  		  pc_nstr[layer] = ecLayer.getAllComponents().size();
  	    }
  	  }
	  this.calDB = new CalDrawDB(det);
	  this.GetStripsDB();
	  this.GetPixelsDB();
      this.pixrot();
//      this.testStrips();
//      this.testPixels();
      System.out.println("ECPixels is done");
	}	
	
	public static void main(String[] args) {
		ECPixels pix = new ECPixels("PCAL");
	}
	
	public void GetStripsDB() {
		
		System.out.println("ECPixels:GetStripsDB()");	
		
	 	for(int sector = 0; sector < 1; sector++) {
	        System.out.println("pcGetStripsDB: Processing Sector "+sector);
	 		for(int layer=0; layer<3 ; layer++) {
	 			for(int strip = 0; strip < pc_nstr[layer] ; strip++) {
		 			shape = calDB.getStripShape(sector, layer, strip);	            
	        		for(int i = 0; i < shape.getShapePath().size(); ++i) {
	                	pc_xstr[i][strip][layer][6] = shape.getShapePath().point(i).x();
	                	pc_ystr[i][strip][layer][6] = shape.getShapePath().point(i).y();
	        		}
	 			}
	 		}
    	}						
	}
	
	public void GetPixelsDB() {
		
        System.out.println("ECPixels:GetPixelsDB()");
		
		DetectorShape2D shape = new DetectorShape2D();

		for(int sector=0; sector<1 ; sector++) {
    	int pixel = 0; double maxPixArea=0;
    	for(int uStrip = 0; uStrip < pc_nstr[0]; uStrip++) {	 
    		for(int vStrip = 0; vStrip < pc_nstr[1]; vStrip++) {    			        		 			 
	            for(int wStrip = 0; wStrip < pc_nstr[2]; wStrip++) {  
	            	shape = calDB.getPixelShape(0, uStrip, vStrip, wStrip);
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
		        		SimplePolygon2D pol1 = new SimplePolygon2D(xtemp2,ytemp2);
	            		double uDist = calDB.getUPixelDistance(uStrip, vStrip, wStrip);
	            		double vDist = calDB.getVPixelDistance(uStrip, vStrip, wStrip);
	            		double wDist = calDB.getWPixelDistance(uStrip, vStrip, wStrip);
	            		shape.setColor(130,(int)(255*vStrip/pc_nstr[1]),(int)(255*wStrip/pc_nstr[2]));	            		
	            		pc_cmap[pixel-1] = 255*pixel/6916;
	            		pc_zmap[pixel-1] = 1.0;
	            		pc_nvrt[pixel-1] = shape.getShapePath().size();
	            		pixels.addTriplets(pixel, uStrip+1, vStrip+1, wStrip+1);
	            		pixels.addShape(shape, sector, pixel);
	            		pixels.setPixelStatus(calDB.isEdgePixel(uStrip,vStrip,wStrip), pixel);
	            	    strips.addPixels(sector, 1, uStrip+1, pixel);
	            		strips.addPixels(sector, 2, vStrip+1, pixel);
	            		strips.addPixels(sector, 3, wStrip+1, pixel);
	            	    strips.addPixDist(sector, 1, uStrip+1, (int) (uDist*100));
	            		strips.addPixDist(sector, 2, vStrip+1, (int) (vDist*100));
	            		strips.addPixDist(sector, 3, wStrip+1, (int) (wDist*100));
		        		pixels.addPixDist(uDist,vDist,wDist,pixel);	
		        		pixels.addArea(pol1.area(),sector,pixel);
		        		if (pol1.area()>maxPixArea) maxPixArea = pol1.area();
	            	}
	            }
    		}
    	}
    	// Sort pixels in each strip according to distance from edge
    	for (int lay=0; lay<3 ; lay++ ){
    		System.out.println("ECPixels: Sorting pixels in layer "+lay);
    		for(int strip = 0; strip < pc_nstr[lay]; strip++) {
    			strips.getSortedPixels(0, lay+1, strip+1);
    		}
    	}   	
    	pixels.setMaxPixelArea(maxPixArea);
    	}
	}
    public void initHistograms() {
        
        System.out.println("ECPixels:initHistograms()");
        
        DetectorCollection<H2D> H2_PCa_Hist = new DetectorCollection<H2D>();
        DetectorCollection<H2D> H2_PCt_Hist = new DetectorCollection<H2D>();
        DetectorCollection<H1D> H1_PCa_Maps = new DetectorCollection<H1D>();
        DetectorCollection<H1D> H1_PCt_Maps = new DetectorCollection<H1D>();
        DetectorCollection<H1D> H1_PCa_Sevd = new DetectorCollection<H1D>();
        DetectorCollection<H1D> H1_PCt_Sevd = new DetectorCollection<H1D>();
        DetectorCollection<H2D> H2_PCa_Sevd = new DetectorCollection<H2D>();
        DetectorCollection<H2D> H2_PC_Stat  = new DetectorCollection<H2D>();  
        
        int nstr = pc_nstr[0]            ; double nend = nstr+1;  
        int npix = pixels.getNumPixels() ; double pend = npix+1;
        
        for (int is=1; is<7 ; is++) {           
            for (int il=1 ; il<7 ; il++){               
                // For Histos
                String id="s"+Integer.toString(is)+"_l"+Integer.toString(il)+"_c";
                H2_PCa_Hist.add(is, il, 0, new H2D("a_raw_"+id+0, 100,   0., 200.,  nstr, 1., nend));
                H2_PCt_Hist.add(is, il, 0, new H2D("a_raw_"+id+0, 100,1330.,1370.,  nstr, 1., nend));
                H2_PCa_Hist.add(is, il, 1, new H2D("b_pix_"+id+1, 100,   0., 200.,  nstr, 1., nend));
                H2_PCt_Hist.add(is, il, 1, new H2D("b_pix_"+id+1, 100,1330.,1370.,  nstr, 1., nend));
                H2_PCa_Hist.add(is, il, 2, new H2D("c_pix_"+id+2,  25,   0., 250.,  npix, 1., pend));
                H2_PCt_Hist.add(is, il, 2, new H2D("c_pix_"+id+2,  40,1330.,1370.,  npix, 1., pend));
                H2_PCa_Hist.add(is, il, 3, new H2D("d_ped_"+id+3,  20, -10.,  10.,  nstr, 1., nend)); 
                H2_PCt_Hist.add(is, il, 3, new H2D("d_tdif_"+id+3, 60, -15.,  15.,  nstr, 1., nend)); 
                H2_PCt_Hist.add(is, il, 4, new H2D("e_tdif_"+id+4, 60, -15.,  15.,  nstr, 1., nend)); 
                H2_PCa_Hist.add(is, il, 5, new H2D("e_fadc_"+id+5,100,   0., 100.,  nstr, 1., nend));
                // For Layer Maps
                H1_PCa_Maps.add(is, il, 0, new H1D("a_adcpix_"+id+0, npix,  1., pend));
                H1_PCa_Maps.add(is, il, 1, new H1D("b_pixa_"+id+1,   npix,  1., pend));
                H1_PCa_Maps.add(is, il, 2, new H1D("c_adcpix2_"+id+2,npix,  1., pend));
                H1_PCa_Maps.add(is, il, 3, new H1D("d_pixa2_"+id+3,  npix,  1., pend));
                H1_PCt_Maps.add(is, il, 0, new H1D("a_tdcpix_"+id+0, npix,  1., pend)); 
                H1_PCt_Maps.add(is, il, 1, new H1D("b_pixt_"+id+1,   npix,  1., pend)); 
                // For Single Events
                H1_PCa_Sevd.add(is, il, 0, new H1D("a_sed_"+id+0, nstr,  1., nend));
                H1_PCt_Sevd.add(is, il, 0, new H1D("a_sed_"+id+0, nstr,  1., nend));
                H2_PCa_Sevd.add(is, il, 0, new H2D("b_sed_fadc_"+id+0,100, 0., 100., nstr, 1., nend));
                H2_PCa_Sevd.add(is, il, 1, new H2D("c_sed_fadc_"+id+1,100, 0., 100., nstr, 1., nend));
            }
            for (int il=7 ; il<9 ; il++){
                String id="s"+Integer.toString(is)+"_l"+Integer.toString(il)+"_c";
                // For Non-Layer Maps
                H1_PCa_Maps.add(is, il, 0, new H1D("a_evtpixa_"+id+0,  npix, 1., pend));
                H1_PCt_Maps.add(is, il, 0, new H1D("a_evtpixt_"+id+0,  npix, 1., pend));    
                H1_PCa_Maps.add(is, il, 1, new H1D("b_pixasum_"+id+1,  npix, 1., pend));
                H1_PCt_Maps.add(is, il, 1, new H1D("b_pixtsum_"+id+1,  npix, 1., pend));    
                H1_PCa_Maps.add(is, il, 2, new H1D("c_pixas_"+id+2,    npix, 1., pend));
                H1_PCt_Maps.add(is, il, 2, new H1D("c_pixat_"+id+2,    npix, 1., pend));
                H1_PCa_Maps.add(is, il, 3, new H1D("d_nevtpixa_"+id+3, npix, 1., pend));
                H1_PCt_Maps.add(is, il, 3, new H1D("d_nevtpixt_"+id+3, npix, 1., pend));    
                // For Single Events
                H1_PCa_Sevd.add(is, il, 0, new H1D("a_sed_"+id+0, npix,  1., pend));
                H1_PCt_Sevd.add(is, il, 0, new H1D("a_sed_"+id+0, npix,  1., pend));
            }
            for (int il=0 ; il<3 ; il++) {
                String id="s"+Integer.toString(is)+"_l"+Integer.toString(il)+"_c";
                H2_PC_Stat.add(is, il, 0, new H2D("a_evt_"+id+0, nstr, 1., nend,  3, 1., 4.));              
                H2_PC_Stat.add(is, il, 1, new H2D("b_adc_"+id+1, nstr, 1., nend,  3, 1., 4.));              
                H2_PC_Stat.add(is, il, 2, new H2D("c_tdc_"+id+2, nstr, 1., nend,  3, 1., 4.));              
            }
        } 
        
        strips.addH2DMap("H2_PCa_Hist", H2_PCa_Hist);
        strips.addH2DMap("H2_PCt_Hist", H2_PCt_Hist);
        pixels.addH1DMap("H1_PCa_Maps", H1_PCa_Maps);
        pixels.addH1DMap("H1_PCt_Maps", H1_PCt_Maps);
        strips.addH1DMap("H1_PCa_Sevd", H1_PCa_Sevd);
        strips.addH1DMap("H1_PCt_Sevd", H1_PCt_Sevd);
        strips.addH2DMap("H2_PCa_Sevd", H2_PCa_Sevd);
        strips.addH2DMap("H2_PC_Stat",  H2_PC_Stat);
   
    }	
	public void testPixels() {
		
		DetectorShapeTabView view = new DetectorShapeTabView();
   	 	DetectorShapeView2D UWmap= new DetectorShapeView2D("PCAL Pixel");
   	 	
		this.GetPixelsDB();
		this.pixrot();
    	
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
   	 	
		this.GetStripsDB();
		this.pixrot();
    	
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
    public void pixrot() {
    	System.out.println("ECPixels:pixrot():");	
    	double[] theta={0.0,60.0,120.0,180.0,240.0,300.0};
	    	
		for(int is=0; is<6; is++) {
			double thet=theta[is]*3.14159/180.;
    		double ct=Math.cos(thet) ; double st=Math.sin(thet);
			// Rotate strips
			for (int lay=0; lay<3 ; lay++) {
				for (int istr=0; istr<pc_nstr[lay]; istr++) {
					for (int k=0;k<4;k++){
						pc_xstr[k][istr][lay][is]= -(pc_xstr[k][istr][lay][6]*ct+pc_ystr[k][istr][lay][6]*st);
						pc_ystr[k][istr][lay][is]=  -pc_xstr[k][istr][lay][6]*st+pc_ystr[k][istr][lay][6]*ct;
					}
				}
			}
		    // Rotate pixels	   		    	   
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
