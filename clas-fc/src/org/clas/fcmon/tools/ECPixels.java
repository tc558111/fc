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
	public Pixels      pixels  = new Pixels();
	public Strips      strips  = new Strips();
	public Pixel        pixel  = null;
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
//	  this.writeFPGALookupTable("/Users/colesmith/pcal_att376_DB.dat",376.,1); 
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
    	int pix = 0; double maxPixArea=0;
    	for(int uStrip = 0; uStrip < pc_nstr[0]; uStrip++) {	 
    		for(int vStrip = 0; vStrip < pc_nstr[1]; vStrip++) {    			        		 			 
	            for(int wStrip = 0; wStrip < pc_nstr[2]; wStrip++) {  
	            	shape = calDB.getPixelShape(0, uStrip, vStrip, wStrip);
	              	if(shape!=null) {	
	            		pix++;
	            	    double [] xtemp2 = new double [shape.getShapePath().size()];
	            		double [] ytemp2 = new double [shape.getShapePath().size()];
	            		for(int i = 0; i < shape.getShapePath().size(); ++i) {
	            			xtemp2[i] = shape.getShapePath().point(i).x();
	            			ytemp2[i] = shape.getShapePath().point(i).y();
		                	pc_xpix[i][pix-1][6] = xtemp2[i];
		                	pc_ypix[i][pix-1][6] = ytemp2[i];
	            		}
		        		SimplePolygon2D pol1 = new SimplePolygon2D(xtemp2,ytemp2);
	            		double uDist = calDB.getUPixelDistance(uStrip, vStrip, wStrip);
	            		double vDist = calDB.getVPixelDistance(uStrip, vStrip, wStrip);
	            		double wDist = calDB.getWPixelDistance(uStrip, vStrip, wStrip);
	            		shape.setColor(130,(int)(255*vStrip/pc_nstr[1]),(int)(255*wStrip/pc_nstr[2]));	            		
	            		pc_cmap[pix-1] = 255*pix/6916;
	            		pc_zmap[pix-1] = 1.0;
	            		pc_nvrt[pix-1] = shape.getShapePath().size();
                        pixel = new Pixel();
                        pixel.setIndex(pix);
	            		pixel.setShape(shape);
                        pixel.setArea(pol1.area());
                        pixel.setReadout(uStrip+1, vStrip+1, wStrip+1);
                        pixel.setReadoutDist(uDist,vDist,wDist);    
                        pixel.setStatus(calDB.isEdgePixel(uStrip,vStrip,wStrip));
                        pixels.addPixel(pixel,pix,uStrip+1,vStrip+1,wStrip+1);
	            	    strips.addPixel(sector, 1, uStrip+1, pix);
	            		strips.addPixel(sector, 2, vStrip+1, pix);
	            		strips.addPixel(sector, 3, wStrip+1, pix);
	            	    strips.addPixDist(sector, 1, uStrip+1, (int) (uDist*100));
	            		strips.addPixDist(sector, 2, vStrip+1, (int) (vDist*100));
	            		strips.addPixDist(sector, 3, wStrip+1, (int) (wDist*100));
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
	
    public void initHistograms(String hipoFile) {
        
        System.out.println("ECPixels:initHistograms()");
        
        DetectorCollection<H2D> H2_PCa_Hist = new DetectorCollection<H2D>();
        DetectorCollection<H2D> H2_PCt_Hist = new DetectorCollection<H2D>();
        DetectorCollection<H1D> H1_PCa_Maps = new DetectorCollection<H1D>();
        DetectorCollection<H1D> H1_PCt_Maps = new DetectorCollection<H1D>();
        DetectorCollection<H2D> H2_PC_Stat  = new DetectorCollection<H2D>();  
        DetectorCollection<H2D> H2_Peds_Hist = new DetectorCollection<H2D>();  
        DetectorCollection<H2D> H2_Tdif_Hist = new DetectorCollection<H2D>();  
        DetectorCollection<H2D> H2_Mode1_Hist = new DetectorCollection<H2D>();  
        DetectorCollection<H2D> H2_Mode1_Sevd = new DetectorCollection<H2D>();  
        DetectorCollection<H1D> H1_Stra_Sevd = new DetectorCollection<H1D>();
        DetectorCollection<H1D> H1_Strt_Sevd = new DetectorCollection<H1D>();
        DetectorCollection<H1D> H1_Pixa_Sevd = new DetectorCollection<H1D>();
        DetectorCollection<H1D> H1_Pixt_Sevd = new DetectorCollection<H1D>();
        
        int nstr = pc_nstr[0]            ; double nend = nstr+1;  
        int npix = pixels.getNumPixels() ; double pend = npix+1;
        
        for (int is=1; is<7 ; is++) {           
            for (int il=1 ; il<7 ; il++){               
                // For Occupancy Histos
                String id="s"+Integer.toString(is)+"_l"+Integer.toString(il)+"_c";
                H2_PCa_Hist.add(is, il, 0, new H2D("a_raw_"+id+0, 100,   0., 200.,  nstr, 1., nend));
                H2_PCt_Hist.add(is, il, 0, new H2D("a_raw_"+id+0, 100,1330.,1450.,  nstr, 1., nend));
                H2_PCa_Hist.add(is, il, 1, new H2D("b_pix_"+id+1, 100,   0., 200.,  nstr, 1., nend));
                H2_PCt_Hist.add(is, il, 1, new H2D("b_pix_"+id+1, 100,1330.,1450.,  nstr, 1., nend));
                H2_PCa_Hist.add(is, il, 2, new H2D("c_pix_"+id+2,  25,   0., 250.,  npix, 1., pend));
                H2_PCt_Hist.add(is, il, 2, new H2D("c_pix_"+id+2,  40,1330.,1450.,  npix, 1., pend));
                //For Pedestal Noise Histos
                H2_Peds_Hist.add(is, il, 0, new H2D("a_ped_"+id+0,  20, -10.,  10.,  nstr, 1., nend)); 
                //For Einner-Eouter FADC time difference Histos
                H2_Tdif_Hist.add(is, il, 0, new H2D("a_tdif_"+id+0, 60, -15.,  15.,  nstr, 1., nend)); 
                H2_Tdif_Hist.add(is, il, 1, new H2D("b_tdif_"+id+1, 60, -15.,  15.,  nstr, 1., nend)); 
                // For Mode1 Histos
                H2_Mode1_Hist.add(is, il, 0, new H2D("a_fadc_"+id+0,100,   0., 100.,  nstr, 1., nend));
                // For Layer Maps
                H1_PCa_Maps.add(is, il, 0, new H1D("a_adcpix_"+id+0, npix,  1., pend));
                H1_PCa_Maps.add(is, il, 1, new H1D("b_pixa_"+id+1,   npix,  1., pend));
                H1_PCa_Maps.add(is, il, 2, new H1D("c_adcpix2_"+id+2,npix,  1., pend));
                H1_PCa_Maps.add(is, il, 3, new H1D("d_pixa2_"+id+3,  npix,  1., pend));
                H1_PCt_Maps.add(is, il, 0, new H1D("a_tdcpix_"+id+0, npix,  1., pend)); 
                H1_PCt_Maps.add(is, il, 1, new H1D("b_pixt_"+id+1,   npix,  1., pend)); 
                // For Single Event Strip Occupancy
                H1_Stra_Sevd.add(is, il, 0, new H1D("a_str_"+id+0, nstr,  1., nend));
                H1_Strt_Sevd.add(is, il, 0, new H1D("a_str_"+id+0, nstr,  1., nend));
                // For Single Event fADC bins
                H2_Mode1_Sevd.add(is, il, 0, new H2D("a_sed_fadc_"+id+0,100, 0., 100., nstr, 1., nend));
                H2_Mode1_Sevd.add(is, il, 1, new H2D("b_sed_fadc_"+id+1,100, 0., 100., nstr, 1., nend));
            }
            for (int il=1 ; il<3 ; il++) {
                // For Single Event Pixel Occupancy
                String id="s"+Integer.toString(is)+"_l"+Integer.toString(il)+"_c";
                H1_Pixa_Sevd.add(is, il, 0, new H1D("a_pix_"+id+0, npix,  1., pend));
                H1_Pixt_Sevd.add(is, il, 0, new H1D("a_pix_"+id+0, npix,  1., pend));
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
            }
            for (int il=0 ; il<3 ; il++) {
                String id="s"+Integer.toString(is)+"_l"+Integer.toString(il)+"_c";
                H2_PC_Stat.add(is, il, 0, new H2D("a_evt_"+id+0, nstr, 1., nend,  3, 1., 4.));              
                H2_PC_Stat.add(is, il, 1, new H2D("b_adc_"+id+1, nstr, 1., nend,  3, 1., 4.));              
                H2_PC_Stat.add(is, il, 2, new H2D("c_tdc_"+id+2, nstr, 1., nend,  3, 1., 4.));              
            }
        }
        
        if(hipoFile!=" "){
            FCCalibrationData calib = new FCCalibrationData();
            calib.getFile(hipoFile);
            H2_PCa_Hist = calib.getCollection("H2_PCa_Hist");
            H1_PCa_Maps = calib.getCollection("H1_PCa_Maps");
            H2_PCt_Hist = calib.getCollection("H2_PCt_Hist");
            H1_PCt_Maps = calib.getCollection("H1_PCt_Maps");
        }   
        
        strips.addH2DMap("H2_PCa_Hist",  H2_PCa_Hist);
        strips.addH2DMap("H2_PCt_Hist",  H2_PCt_Hist);
        pixels.addH1DMap("H1_PCa_Maps",  H1_PCa_Maps);
        pixels.addH1DMap("H1_PCt_Maps",  H1_PCt_Maps);
        strips.addH1DMap("H1_Pixa_Sevd", H1_Pixa_Sevd);
        strips.addH1DMap("H1_Pixt_Sevd", H1_Pixt_Sevd);
        strips.addH1DMap("H1_Stra_Sevd", H1_Stra_Sevd);
        strips.addH1DMap("H1_Strt_Sevd", H1_Strt_Sevd);
        strips.addH2DMap("H2_PC_Stat",   H2_PC_Stat);
        strips.addH2DMap("H2_Peds_Hist", H2_Peds_Hist);
        strips.addH2DMap("H2_Tdif_Hist", H2_Tdif_Hist);
        strips.addH2DMap("H2_Mode1_Hist",H2_Mode1_Hist);
        strips.addH2DMap("H2_Mode1_Sevd",H2_Mode1_Sevd);
   
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

     public void writeFPGALookupTable(String filename, double atten, int opt) {
       Pixels newpix = new Pixels();
       int u,v,w,us,vs,ws;
       double dist_u,dist_v,dist_w,ua,va,wa;
       
       try {
          PrintWriter fout = new PrintWriter(filename);
          for(int i=0; i<pixels.getNumPixels() ; i++) {
             u=pixels.getStrip(1,i+1) ; v=pixels.getStrip(2,i+1); w=pixels.getStrip(3,i+1);
             dist_u = pixels.getDist(1,i+1); dist_v=pixels.getDist(2,i+1); dist_w=pixels.getDist(3,i+1);
             ua=Math.exp(-dist_u/atten); va=Math.exp(-dist_v/atten); wa=Math.exp(-dist_w/atten);
             String line = String.format("%1$d  %2$d  %3$d  %4$.3f %5$.3f %6$.3f",u,v,w,ua,va,wa); 
             fout.printf(line+"\n");
             if (opt==1) { // FPGA version allows for +/- 1 non-intersecting strips 
             us=u+1;
             if (us<69&&!pixels.pixelStrips.hasItem(us,v,w)&&!newpix.pixelStrips.hasItem(us,v,w)) {
                 line = String.format("%1$d  %2$d  %3$d  %4$.3f %5$.3f %6$.3f",us,v,w,ua,va,wa);fout.printf(line+"\n");
                 newpix.addPixel(i,us,v,w);                
             }
             vs=v+1;
             if (vs<63&&!pixels.pixelStrips.hasItem(u,vs,w)&&!newpix.pixelStrips.hasItem(u,vs,w)) {
                 line = String.format("%1$d  %2$d  %3$d  %4$.3f %5$.3f %6$.3f",u,vs,w,ua,va,wa);fout.printf(line+"\n");
                 newpix.addPixel(i,u,vs,w);                
             }
             ws=w+1;
             if (ws<63&&!pixels.pixelStrips.hasItem(u,v,ws)&&!newpix.pixelStrips.hasItem(u,v,ws)) {
                 line = String.format("%1$d  %2$d  %3$d  %4$.3f %5$.3f %6$.3f",u,v,ws,ua,va,wa);fout.printf(line+"\n");
                 newpix.addPixel(i,u,v,ws);                
             }
             us=u-1;
             if (us>0&&!pixels.pixelStrips.hasItem(us,v,w)&&!newpix.pixelStrips.hasItem(us,v,w)) {
                 line = String.format("%1$d  %2$d  %3$d  %4$.3f %5$.3f %6$.3f",us,v,w,ua,va,wa);fout.printf(line+"\n");
                 newpix.addPixel(i,us,v,w);                
             }
             vs=v-1;
             if (vs>0&&!pixels.pixelStrips.hasItem(u,vs,w)&&!newpix.pixelStrips.hasItem(u,vs,w)) {
                 line = String.format("%1$d  %2$d  %3$d  %4$.3f %5$.3f %6$.3f",u,vs,w,ua,va,wa);fout.printf(line+"\n");
                 newpix.addPixel(i,u,vs,w);                
             }
             ws=w-1;
             if (ws>0&&!pixels.pixelStrips.hasItem(u,v,ws)&&!newpix.pixelStrips.hasItem(u,v,ws)) {
                 line = String.format("%1$d  %2$d  %3$d  %4$.3f %5$.3f %6$.3f",u,v,ws,ua,va,wa);fout.printf(line+"\n");
                 newpix.addPixel(i,u,v,ws);                
             }
             }
          }       
       fout.close();
       }
     
       catch(FileNotFoundException ex){}       
    
     }

}

