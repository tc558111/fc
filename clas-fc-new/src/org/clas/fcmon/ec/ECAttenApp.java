package org.clas.fcmon.ec;

import java.util.TreeMap;

import org.clas.fcmon.tools.CalibrationData;
import org.clas.fcmon.tools.FCApplication;
import org.jlab.clas.detector.DetectorCollection;
import org.jlab.detector.base.DetectorDescriptor;
import org.jlab.clas.detector.DetectorType;
import org.jlab.clasrec.utils.DatabaseConstantProvider;
import org.jlab.rec.ecn.ECCommon;

//groot
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.groot.math.F1D;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.data.H1F;

public class ECAttenApp extends FCApplication {
    
   EmbeddedCanvas c = this.getCanvas(this.getName()); 
    
   public ECAttenApp(String name , ECPixels[] ecPix) {
      super(name, ecPix);		
   }
   
   public void init() {
      
   }
		
   public void analyze(int idet, int is1, int is2, int il1, int il2) {
	   
      TreeMap<Integer, Object> map;
      CalibrationData fits = null; 	
      boolean doCalibration=false;
      int npix = ecPix[idet].pixels.getNumPixels();
      double  meanerr[] = new double[npix];
      boolean status[] = new boolean[npix];
		 		
      for (int is=is1 ; is<is2 ; is++) {
         for (int il=il1 ; il<il2 ; il++) {	
            //Extract raw arrays for error bar calculation
            float  cnts[] = ecPix[idet].pixels.hmap1.get("H1_a_Maps").get(is,7,0).getData();				
            float   adc[] = ecPix[idet].pixels.hmap1.get("H1_a_Maps").get(is,il,1).getData();
            float adcsq[] = ecPix[idet].pixels.hmap1.get("H1_a_Maps").get(is,il,3).getData();
            doCalibration = false;
				
            for (int ipix=0 ; ipix<npix ; ipix++) {
               meanerr[ipix]=0;
               if (cnts[ipix]>1) {
                  meanerr[ipix]=Math.sqrt((adcsq[ipix]-adc[ipix]*adc[ipix]-8.3)/(cnts[ipix]-1)); //Sheppard's correction: c^2/12 c=10
                  doCalibration = true;
               }				
               if (cnts[ipix]==1) {
                  meanerr[ipix]=8.3;
                  doCalibration = true;
               }
                  status[ipix] = ecPix[idet].pixels.getPixelStatus(ipix+1);
            }
				
            map = (TreeMap<Integer, Object>) ecPix[idet].Lmap_a.get(is,il+10,0);
            float meanmap[] = (float[]) map.get(1);
            double distmap[] = (double[]) ecPix[idet].pixels.getDist(il);
				
            for (int ip=0 ; ip<ecPix[idet].ec_nstr[il-1] ; ip++) {
               if (doCalibration) {
                  fits = new CalibrationData(is+idet*10,il,ip);
                  fits.getDescriptor().setType(DetectorType.EC);
                  fits.addGraph(ecPix[idet].strips.getpixels(il,ip+1,cnts),
                                ecPix[idet].strips.getpixels(il,ip+1,distmap),
                                ecPix[idet].strips.getpixels(il,ip+1,meanmap),
                                ecPix[idet].strips.getpixels(il,ip+1,meanerr),
                                ecPix[idet].strips.getpixels(il,ip+1,status));
                  fits.analyze();
                  ecPix[idet].collection.add(fits.getDescriptor(),fits);
               }
            }
         }
      }
		
   }
	
   public void updateCanvas(DetectorDescriptor dd) {
		
      H1F mipADC = null;
      int nstr = ecPix[0].ec_nstr[0];
		
      double[] xp     = new double[nstr];
      double[] xpe    = new double[nstr];
      double[] yp     = new double[nstr]; 
      double[] vgain  = new double[nstr];
      double[] vgaine = new double[nstr]; 
      double[] vatt   = new double[nstr];
      double[] vatte  = new double[nstr]; 
      double[] vattdb = new double[nstr];
      double[] vattdbe= new double[nstr];
      double[] vchi2  = new double[nstr];
      double[] vchi2e = new double[nstr]; 
      double[] mip    = {100.,100.,160.};
      double[] xpix   = new double[1];
      double[] ypix   = new double[1];
      double[] xerr   = new double[1];
      double[] yerr   = new double[1];
	    
      String otab[][]={{" U PMT "," V PMT "," W PMT "},
              {" U Inner PMT "," V Inner PMT "," W Inner PMT "},
              {" U Outer PMT "," V Outer PMT "," W Outer PMT "}};
		
      this.getDetIndices(dd);
	  layer = lay ; 
	  int ilm = ilmap;
	  
	  DetectorCollection<CalibrationData> calib = ecPix[ilm].collection;
	  
      c.divide(2,2); 
		
      int   inProcess =     (int) mon.getGlob().get("inProcess");
      Boolean    inMC = (Boolean) mon.getGlob().get("inMC");
      int         is1 =     (int) mon.getGlob().get("is1");      
      DatabaseConstantProvider ccdb = (DatabaseConstantProvider) mon.getGlob().get("ccdb");
        
      if (layer>10) {
         float meanmap[] = (float[]) ecPix[ilm].Lmap_a.get(is, layer, 0).get(1);
         xpix[0] = ecPix[ilm].pixels.getDist(layer-10, ic+1);
         ypix[0] = meanmap[ic];
         xerr[0] = 0.;
         yerr[0] = 0.;
         mipADC = ecPix[ilm].strips.hmap2.get("H2_a_Hist").get(is,layer-10,2).sliceY(ic) ;
      }
        
      if (layer<7||(layer>10&&layer<17)) {
         if (inProcess>0) {
            if (layer>10) {layer=layer-10; lay=layer;int component = ecPix[ilm].pixels.getStrip(lay,ic+1); ic=component-1;}
               nstr = ecPix[ilm].ec_nstr[layer-1];
               if (inProcess==1)  {analyze(ilm,is,is+1,layer,layer+1);}
               if (calib.hasEntry(is+ilm*10, layer, ic)) {
									
               for (int ip=0; ip<nstr ; ip++) {
                  double gain  = calib.get(is1+ilm*10,layer,ip).getFunc(0).parameter(0).value();
                  double gaine = calib.get(is1+ilm*10,layer,ip).getFunc(0).parameter(0).error();	
                  double att   = calib.get(is1+ilm*10,layer,ip).getFunc(0).parameter(1).value();
                  double atte  = calib.get(is1+ilm*10,layer,ip).getFunc(0).parameter(1).error();
                  double chi2  = calib.get(is1+ilm*10,layer,ip).getChi2(0);
                  int index = ECCommon.getCalibrationIndex(is,layer+ilm*3,ip+1);
                  double attdb = ccdb.getDouble("/calibration/ec/attenuation/B",index);
                  if (att!=0) att=-1./att; else att=0 ; 
                     atte = att*att*atte;
                     xp[ip] = ip+1 ;     xpe[ip] = 0.; 
                  vgain[ip] = gain ;  vgaine[ip] = gaine;
                   vatt[ip] = att  ;   vatte[ip] = atte;
                 vattdb[ip] = attdb; vattdbe[ip] = 0.;
                  vchi2[ip] = Math.min(4, chi2) ; vchi2e[ip]=0.;   
               }
				
               GraphErrors   gainGraph = new GraphErrors("gain",xp,vgain,xpe,vgaine);
               GraphErrors    attGraph = new GraphErrors("att",xp,vatt,xpe,vatte);
               GraphErrors  attdbGraph = new GraphErrors("attdb",xp,vattdb,xpe,vattdbe);
               GraphErrors   chi2Graph = new GraphErrors("chi2",xp,vchi2,xpe,vchi2e);
               GraphErrors    pixGraph = new GraphErrors("pix",xpix,ypix,xerr,yerr);
	             
               gainGraph.getAttributes().setMarkerStyle(2);   
               gainGraph.getAttributes().setMarkerSize(6);   
               gainGraph.getAttributes().setMarkerColor(2);
                attGraph.getAttributes().setMarkerStyle(2);    
                attGraph.getAttributes().setMarkerSize(6);    
                attGraph.getAttributes().setMarkerColor(2);
              attdbGraph.getAttributes().setMarkerStyle(2);  
              attdbGraph.getAttributes().setMarkerSize(7);  
              attdbGraph.getAttributes().setMarkerColor(1);
               chi2Graph.getAttributes().setMarkerStyle(2);   
               chi2Graph.getAttributes().setMarkerSize(6);   
               chi2Graph.getAttributes().setMarkerColor(2);
                pixGraph.getAttributes().setMarkerStyle(1);    
                pixGraph.getAttributes().setMarkerSize(6);    
                pixGraph.getAttributes().setMarkerColor(2); 
	             
               gainGraph.getAttributes().setTitleX(otab[ilm][lay-1]) ;  
               gainGraph.getAttributes().setTitleY("PMT GAIN")  ; 
               gainGraph.getAttributes().setTitle(" ");
              attdbGraph.getAttributes().setTitleX(otab[ilm][lay-1]) ; 
              attdbGraph.getAttributes().setTitleY("ATTENUATION (CM)") ; 
              attdbGraph.getAttributes().setTitle(" ");
               chi2Graph.getAttributes().setTitleX(otab[ilm][lay-1]) ;  
               chi2Graph.getAttributes().setTitleY("REDUCED CHI^2"); 
               chi2Graph.getAttributes().setTitle(" ");
		        
               F1D f1 = new F1D("p0","[a]",0,nstr+1); f1.setParameter(0,mip[ilm]); f1.setLineStyle(2);
	            		        
               double ymax=200; if(!inMC) ymax=350;
               c.cd(0);c.getPad(0).getAxisX().setRange(0.,400.);c.getPad(0).getAxisY().setRange(0.,ymax);
               c.draw(calib.get(is+ilm*10,layer,ic).getRawGraph(0));                 
               if(calib.get(is+ilm*10,layer,ic).getFitGraph(0).getDataSize(0)>0) 
               c.draw(calib.get(is+ilm*10,layer,ic).getFitGraph(0),"same");                             
               c.draw(calib.get(is+ilm*10,layer,ic).getFunc(0),"same");
               c.draw(pixGraph,"same");
				
               double xmax = ecPix[ilm].ec_nstr[layer-1]+1;
               c.cd(1); c.getPad(1).getAxisX().setRange(0.,xmax);c.getPad(1).getAxisY().setRange(0.,4.);
               c.draw(chi2Graph) ; 
               c.cd(2); c.getPad(2).getAxisX().setRange(0.,xmax);c.getPad(2).getAxisY().setRange(0.,ymax);
               c.draw(gainGraph) ; c.draw(f1,"same"); 
               c.cd(3); c.getPad(3).getAxisX().setRange(0.,xmax);c.getPad(3).getAxisY().setRange(0.,600.);
               c.draw(attdbGraph); c.draw(attGraph,"same");   
              
               c.repaint();
	            
            }
         }
      }
   }
   
	
}
