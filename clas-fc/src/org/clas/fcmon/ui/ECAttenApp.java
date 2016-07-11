package org.clas.fcmon.ui;

import java.util.TreeMap;

import org.clas.fcmon.tools.CalibrationData;
import org.clas.fcmon.tools.ECPixels;
import org.clas.fcmon.tools.FCApplication;
import org.jlab.clas.detector.DetectorCollection;
import org.jlab.detector.base.DetectorDescriptor;
import org.jlab.clas.detector.DetectorType;
import org.jlab.clasrec.utils.DatabaseConstantProvider;
import org.jlab.rec.ecn.ECCommon;
import org.root.basic.EmbeddedCanvas;
import org.root.func.F1D;
import org.root.histogram.GraphErrors;
import org.root.histogram.H1D;

public class ECAttenApp extends FCApplication {
    
   DetectorCollection<CalibrationData> collection = new DetectorCollection<CalibrationData>();  
    
   public ECAttenApp(String name , ECPixels[] ecPix) {
      super(name, ecPix);		
   }
   
   public DetectorCollection<CalibrationData> getCalibration() {
      return this.collection;
   }
   
   public void init() {
       this.collection.clear();
   }
		
   public void analyze(int is1, int is2, int il1, int il2, int ip1, int ip2) {
	   
      TreeMap<Integer, Object> map;
      CalibrationData fits ; 	
      boolean doCalibration=false;
      int npix = ecPix[0].pixels.getNumPixels();
      double  meanerr[] = new double[npix];
      boolean status[] = new boolean[npix];
		 		
      for (int is=is1 ; is<is2 ; is++) {
         for (int il=il1 ; il<il2 ; il++) {
            int ill,iill ; if (il<4) {ill=7 ; iill=il;} else {ill=8 ; iill=il-3;}	
            //Extract raw arrays for error bar calculation
            double cnts[]  = ecPix[0].pixels.hmap1.get("H1_PCa_Maps").get(is+1,ill,0).getData();				
            double adc[]   = ecPix[0].pixels.hmap1.get("H1_PCa_Maps").get(is+1,il,1).getData();
            double adcsq[] = ecPix[0].pixels.hmap1.get("H1_PCa_Maps").get(is+1,il,3).getData();
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
                  status[ipix] = ecPix[ill-7].pixels.getPixelStatus(ipix+1);
            }
				
            map = (TreeMap<Integer, Object>) Lmap_a.get(is+1,il+10,0);
            double meanmap[] = (double[]) map.get(1);
            double distmap[] = (double[]) ecPix[ill-7].pixels.getDist(iill);
				
            for (int ip=ip1 ; ip<ip2 ; ip++) {
               if (doCalibration) {
                  fits = new CalibrationData(is,il,ip);
                  fits.getDescriptor().setType(DetectorType.PCAL);
                  fits.addGraph(ecPix[ill-7].strips.getpixels(iill,ip+1,cnts),
                                ecPix[ill-7].strips.getpixels(iill,ip+1,distmap),
                                ecPix[ill-7].strips.getpixels(iill,ip+1,meanmap),
                                ecPix[ill-7].strips.getpixels(iill,ip+1,meanerr),
                                ecPix[ill-7].strips.getpixels(iill,ip+1,status));
                  fits.analyze();
                  collection.add(fits.getDescriptor(),fits);
               }
            }
         }
      }
		
   }
	
   public void updateCanvas(DetectorDescriptor dd) {
		
      EmbeddedCanvas canvas = this.getCanvas(this.getName()); 
      
      H1D mipADC = null;
      int nstr = ecPix[0].pc_nstr[0];
		
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
      double[] mip    = {100.,160.};
      double[] xpix   = new double[1];
      double[] ypix   = new double[1];
      double[] xerr   = new double[1];
      double[] yerr   = new double[1];
	    
      String otab[]={"U Inner Strips","V Inner Strips","W Inner Strips","U Outer Strips","V Outer Strips","W Outer Strips"};
		
      this.getDetIndices(dd);
	  layer = lay ; 	
      canvas.divide(2,2); 
		
      int inProcess =     (int) mon.getGlob().get("inProcess");
      int     detID =     (int) mon.getGlob().get("detID");
      Boolean  inMC = (Boolean) mon.getGlob().get("inMC");
      DatabaseConstantProvider ccdb = (DatabaseConstantProvider) mon.getGlob().get("ccdb");
        
      if (layer>10) {
         double meanmap[] = (double[]) Lmap_a.get(is+1, layer, 0).get(1);
         xpix[0] = ecPix[0].pixels.getDist(layer-10-of, ic+1);
         ypix[0] = meanmap[ic];
         xerr[0] = 0.;
         yerr[0] = 0.;
         mipADC = ecPix[0].strips.hmap2.get("H2_PCa_Hist").get(is+1,layer-10,2).sliceY(ic) ;
         //System.out.println("Pixel="+(ic+1)+" Mean1= "+ypix[0]+" Mean2= "+mipADC.getMean());
      }
        
      if (layer<7||(layer>10&&layer<17)) {
         if (inProcess>0) {
            if (layer>10) {layer=layer-10; lay=layer;int component = ecPix[0].pixels.getStrip(lay-of,ic+1); ic=component-1;}
               nstr = ecPix[0].pc_nstr[layer-of-1];
               if (inProcess==1)  {analyze(is,is+1,layer,layer+1,0,nstr);}
               if (collection.hasEntry(is, layer, ic)) {
									
               for (int ip=0; ip<nstr ; ip++) {
                  double gain  =  collection.get(1,layer,ip).getFunc(0).parameter(0).value();
                  double gaine =  collection.get(1,layer,ip).getFunc(0).parameter(0).error();	
                  double att   =  collection.get(1,layer,ip).getFunc(0).parameter(1).value();
                  double atte  =  collection.get(1,layer,ip).getFunc(0).parameter(1).error();
                  double chi2  =  collection.get(1,layer,ip).getChi2(0);
                  int index = ECCommon.getCalibrationIndex(is+1,layer+detID*3,ip+1);
                  double attdb = ccdb.getDouble("/calibration/ec/attenuation/B",index);
                  if (att!=0) att=-1./att; else att=0 ; 
                     atte = att*att*atte;
                     xp[ip] = ip+1 ;     xpe[ip] = 0.; 
                  vgain[ip] = gain ;  vgaine[ip] = gaine;
                   vatt[ip] = att  ;   vatte[ip] = atte;
                 vattdb[ip] = attdb; vattdbe[ip] = 0.;
                  vchi2[ip] = Math.min(4, chi2) ; vchi2e[ip]=0.;   
               }
				
               GraphErrors   gainGraph = new GraphErrors(xp,vgain,xpe,vgaine);
               GraphErrors    attGraph = new GraphErrors(xp,vatt,xpe,vatte);
               GraphErrors  attdbGraph = new GraphErrors(xp,vattdb,xpe,vattdbe);
               GraphErrors   chi2Graph = new GraphErrors(xp,vchi2,xpe,vchi2e);
               GraphErrors    pixGraph = new GraphErrors(xpix,ypix,xerr,yerr);
	             
               gainGraph.setMarkerStyle(2);   gainGraph.setMarkerSize(6);   gainGraph.setMarkerColor(2);
                attGraph.setMarkerStyle(2);    attGraph.setMarkerSize(6);    attGraph.setMarkerColor(2);
              attdbGraph.setMarkerStyle(2);  attdbGraph.setMarkerSize(7);  attdbGraph.setMarkerColor(1);
               chi2Graph.setMarkerStyle(2);   chi2Graph.setMarkerSize(6);   chi2Graph.setMarkerColor(2);
                pixGraph.setMarkerStyle(2);    pixGraph.setMarkerSize(6);    pixGraph.setFillColor(1); 
	             
               gainGraph.setXTitle(otab[lay-1]) ;  gainGraph.setYTitle("PMT GAIN")         ; gainGraph.setTitle(" ");
              attdbGraph.setXTitle(otab[lay-1]) ; attdbGraph.setYTitle("ATTENUATION (CM)") ; attdbGraph.setTitle(" ");
               chi2Graph.setXTitle(otab[lay-1]) ;  chi2Graph.setYTitle("REDUCED CHI^2")    ; chi2Graph.setTitle(" ");
		        
               F1D f1 = new F1D("p0",0,nstr+1); f1.setParameter(0,mip[io-1]); f1.setLineStyle(2);
	            		        
               double ymax=200; if(!inMC) ymax=350;
               canvas.cd(0);canvas.getPad().setAxisRange("Y",0.,ymax);
               canvas.draw(collection.get(is,layer,ic).getRawGraph(0));
               canvas.draw(collection.get(is,layer,ic).getFitGraph(0),"same");
               canvas.draw(collection.get(is,layer,ic).getFunc(0),"same");
               canvas.draw(pixGraph,"same");
				
               double xmax = ecPix[0].pc_nstr[0]+1;
               canvas.cd(1);           canvas.getPad().setAxisRange(0.,xmax,0.,4.)   ; canvas.draw(chi2Graph) ; 
               canvas.cd(2); if(!inMC) canvas.getPad().setAxisRange(0.,xmax,0.,400.) ; canvas.draw(gainGraph) ; canvas.draw(f1,"same"); 
               canvas.cd(3);           canvas.getPad().setAxisRange(0.,xmax,0.,600.) ; canvas.draw(attdbGraph); canvas.draw(attGraph,"same");   
	            
            }
         }
      }
   }
	
}
