package org.clas.fcmon.ec;

import static java.lang.System.out;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.TreeMap;




import org.clas.fcmon.tools.CalDrawDB;
//clas12
import org.clas.fcmon.tools.FADCFitter;
import org.clas.fcmon.tools.FCApplication;
import org.jlab.clas.detector.DetectorCollection;
import org.jlab.clas12.detector.FADCConfigLoader;
//import org.root.histogram.H1D;
//import org.root.histogram.H2D;

//groot
//import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.math.StatNumber;
//clas12rec
import org.jlab.detector.decode.CodaEventDecoder;
import org.jlab.detector.decode.DetectorDataDgtz;
import org.jlab.detector.decode.DetectorEventDecoder;
import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataBank;
import org.clas.fcmon.detector.view.DetectorShape2D;
import org.clas.fcmon.jroot.*;

public class ECReconstructionApp extends FCApplication {
    
   FADCConfigLoader fadc  = new FADCConfigLoader();
   FADCFitter     fitter  = new FADCFitter(1,15);
   String          mondet = null;
   Boolean           inMC = null;
   Boolean          inCRT = null;
   Boolean          doRec = null;
   Boolean          doEng = null;
   String          config = null;
   String BankType        = null;
   int              detID = 0;
   double pcx,pcy,pcz;
   
   double genuA[] = new double[68]; //
   double genuB[] = new double[68]; //
   double genuC[] = new double[68]; //
   
   double genvA[] = new double[62]; //
   double genvB[] = new double[62]; //
   double genvC[] = new double[62]; //
   
   double genwA[] = new double[62]; //
   double genwB[] = new double[62]; //
   double genwC[] = new double[62];
   
   CodaEventDecoder            newdecoder = new CodaEventDecoder();
   DetectorEventDecoder   detectorDecoder = new DetectorEventDecoder();
   List<DetectorDataDgtz>  detectorData   = new ArrayList<DetectorDataDgtz>();
 
   public DetectorCollection<TreeMap<Integer,Object>> Lmap_a = new DetectorCollection<TreeMap<Integer,Object>>();
   public DetectorCollection<TreeMap<Integer,Object>> Lmap_t = new DetectorCollection<TreeMap<Integer,Object>>();
   
   double[]                sed7=null,sed8=null;
   TreeMap<Integer,Object> map7=null,map8=null; 
   
   int nstr = ecPix[0].ec_nstr[0];
   int npix = ecPix[0].pixels.getNumPixels();  
   
   int nsa,nsb,tet,pedref;     
   short[] pulse = new short[100]; 
   
   DetectorType[] detNames = {DetectorType.PCAL, DetectorType.ECIN, DetectorType.ECOUT};
    
   public ECReconstructionApp(String name, ECPixels[] ecPix) {
       super(name,ecPix);
   }
   
   public void init() {
       System.out.println("ECReconstruction.init()");
       fadc.load("/daq/fadc/ec",10,"default");
       mondet =           (String) mon.getGlob().get("mondet");
       inMC   =          (Boolean) mon.getGlob().get("inMC");
       inCRT  =          (Boolean) mon.getGlob().get("inCRT");
       doRec  =          (Boolean) mon.getGlob().get("doRec");
       doEng  =          (Boolean) mon.getGlob().get("doEng");
       config =           (String) mon.getGlob().get("config");
   }
   
   public void clearHistograms() {
     
       for (int idet=0; idet<ecPix.length; idet++) {
           for (int is=1 ; is<7 ; is++) {
               ecPix[idet].strips.hmap2.get("H2_a_Hist").get(is,0,0).reset();
               ecPix[idet].strips.hmap2.get("H2_t_Hist").get(is,0,0).reset();
               for (int il=1 ; il<3 ; il++) {
                   ecPix[idet].strips.hmap2.get("H2_a_Hist").get(is,il,0).reset();
                   ecPix[idet].strips.hmap2.get("H2_a_Hist").get(is,il,3).reset();
                   ecPix[idet].strips.hmap2.get("H2_a_Hist").get(is,il,5).reset();
                   ecPix[idet].strips.hmap2.get("H2_t_Hist").get(is,il,0).reset();
               }
           }       
       } 
   }      
   
   public void getMode7(int cr, int sl, int ch) {    
      app.mode7Emulation.configMode7(cr,sl,ch);
      this.nsa    = app.mode7Emulation.nsa;
      this.nsb    = app.mode7Emulation.nsb;
      this.tet    = app.mode7Emulation.tet;
      this.pedref = app.mode7Emulation.pedref;
   }
   
   public void addEvent(DataEvent event) {
       
      if(inMC==true) {
          this.updateSimulatedData(event);
      } else {
          this.updateRealData(event);         
      }
      
      if (doRec||doEng) this.processECRec(event);
      
      if (app.isSingleEvent()) {
//         for (int idet=0; idet<ecPix.length; idet++) findPixels(idet);     // Process all pixels for SED
         for (int idet=0; idet<ecPix.length; idet++) processSED(idet);
      } else {
         for (int idet=0; idet<ecPix.length; idet++) processPixels(idet); // Process only single pixels 
 //         processCalib();  // Process only single pixels 
      }
   }
   
   public int getDet(int layer) {
       int[] il = {0,0,0,1,1,1,2,2,2}; // layer 1-3: PCAL 4-6: ECinner 7-9: ECouter  
       return il[layer-1];
    }
   
   public int getLay(int layer) {
       int[] il = {1,2,3,1,2,3,1,2,3}; // layer 1-3: PCAL 4-6: ECinner 7-9: ECouter  
       return il[layer-1];
    }
     
   public void updateRealData(DataEvent event){

      int adc,npk,ped;
      double tdc=0,tdcf=0;
      String AdcType ;
      
      List<DetectorDataDgtz>  dataSet = newdecoder.getDataEntries((EvioDataEvent) event);
      
      detectorDecoder.translate(dataSet);   
      detectorDecoder.fitPulses(dataSet);
      this.detectorData.clear();
      this.detectorData.addAll(dataSet);
      
      clear(0); clear(1); clear(2);

      int ilay=0;
      int idet=-1;
      
      for (DetectorDataDgtz strip : detectorData) {
         if(strip.getDescriptor().getType().getName()=="EC") {
            adc=npk=ped=pedref=0 ; tdc=tdcf=0;
            int icr = strip.getDescriptor().getCrate(); 
            int isl = strip.getDescriptor().getSlot(); 
            int ich = strip.getDescriptor().getChannel(); 
            int is  = strip.getDescriptor().getSector();
            int il  = strip.getDescriptor().getLayer(); // 1-3: PCAL 4-9: ECAL
            int ip  = strip.getDescriptor().getComponent();
            int iord= strip.getDescriptor().getOrder(); 
            
            idet = getDet(il);
            ilay = getLay(il);
            
            app.currentCrate = icr;
            app.currentSlot  = isl;
            app.currentChan  = ich;
 
            if (idet>-1) {
                            
            if (strip.getTDCSize()>0) {
                tdc = strip.getTDCData(0).getTime()*24./1000.;
            }
            
            if (strip.getADCSize()>0) {     
                
               AdcType = strip.getADCData(0).getPulseSize()>0 ? "ADCPULSE":"ADCFPGA";
               
               if(AdcType=="ADCFPGA") { // FADC MODE 7
                  adc = strip.getADCData(0).getIntegral();
                  ped = strip.getADCData(0).getPedestal();
                  npk = strip.getADCData(0).getHeight();
                 tdcf = strip.getADCData(0).getTime();            
                  getMode7(icr,isl,ich);
                  if (app.mode7Emulation.User_pedref==0) adc = (adc-ped*(this.nsa+this.nsb))/10;
                  if (app.mode7Emulation.User_pedref==1) adc = (adc-this.pedref*(this.nsa+this.nsb))/10;
               }   
               
               if (AdcType=="ADCPULSE") { // FADC MODE 1
                  for (int i=0 ; i<strip.getADCData(0).getPulseSize();i++) {               
                     pulse[i] = (short) strip.getADCData(0).getPulseValue(i);
                  }
                  getMode7(icr,isl,ich);
                  if (app.mode7Emulation.User_pedref==0) fitter.fit(this.nsa,this.nsb,this.tet,0,pulse);                  
                  if (app.mode7Emulation.User_pedref==1) fitter.fit(this.nsa,this.nsb,this.tet,pedref,pulse);                    
                  adc = fitter.adc/10;
                  ped = fitter.pedsum;
                  for (int i=0 ; i< pulse.length ; i++) {
                     ecPix[idet].strips.hmap2.get("H2_Mode1_Hist").get(is,ilay,0).fill(i,ip,pulse[i]-this.pedref);
                     if (app.isSingleEvent()) {
                        ecPix[idet].strips.hmap2.get("H2_Mode1_Sevd").get(is,ilay,0).fill(i,ip,pulse[i]-this.pedref);
                        int w1 = fitter.t0-this.nsb ; int w2 = fitter.t0+this.nsa;
                        if (fitter.adc>0&&i>=w1&&i<=w2) ecPix[idet].strips.hmap2.get("H2_Mode1_Sevd").get(is,ilay,1).fill(i,ip,pulse[i]-this.pedref);                     
                     }
                  }
               }               
               if (ped>0) ecPix[idet].strips.hmap2.get("H2_Peds_Hist").get(is,ilay,0).fill(this.pedref-ped, ip);
             }           
             fill(idet, is, ilay, ip, adc, tdc, tdcf);    
            }
         }
      }
   }
   
   public void updateSimulatedData(DataEvent event) {
       
      float tdcmax=2000000;
      double tmax = 30;
      int adc, tdcc, fac, detlen=0;
      double mc_t=0.,tdc=0,tdcf=0;
      boolean goodstrip = true;
      
      String det[] = {"PCAL","EC"}; // EC.xml banknames
      
      if (ecPix.length>1)  {detlen=det.length; clear(0); clear(1); clear(2);} 
      if (ecPix.length==1) {detlen=1;          clear(0);}
      
//      System.out.println(" ");
      
      
      for (int idet=0; idet<detlen; idet++) {
          
          fac = (inCRT==true) ? 6:1;
          
          if(event.hasBank(det[idet]+"::true")==true) {
              EvioDataBank bank = (EvioDataBank) event.getBank(det[idet]+"::true");
              for(int i=0; i < bank.rows(); i++) {
                  if(idet==0) {
                     double pcX = bank.getDouble("avgX",i);
                     double pcY = bank.getDouble("avgY",i);
                     double pcZ = bank.getDouble("avgZ",i);
                     double pcT = bank.getDouble("avgT",i);
                     if(pcT<tmax){pcx=pcX; pcy=pcY; pcz=pcZ ; tmax = pcT;}
                  }
              }
              if ((doRec||doEng)&&idet==0) System.out.println("PCAL x,y,z,t="+pcx+" "+pcy+" "+pcz+" "+tmax);
          }
          
          inMC = true; mon.putGlob("inMC",true); 
          
          if(event.hasBank(det[idet]+"::dgtz")==true) {            
              EvioDataBank bank = (EvioDataBank) event.getBank(det[idet]+"::dgtz");
              
              for(int i = 0; i < bank.rows(); i++){
                  float dum = (float)bank.getInt("TDC",i);
                  if (dum<tdcmax) tdcmax=dum; //Find and save longest hit time
              }
              
              for(int i = 0; i < bank.rows(); i++){
                  int is  = bank.getInt("sector",i);
                  int ip  = bank.getInt("strip",i);
                  int ic  = bank.getInt("stack",i);     
                  int il  = bank.getInt("view",i);  
                      adc = bank.getInt("ADC",i)/fac;
                     tdcc = bank.getInt("TDC",i);
                     tdcf = tdcc;
                  if (idet>0&&ic==1) idet=1;
                  if (idet>0&&ic==2) idet=2;
                  //System.out.println("Sector "+is+" Stack "+ic+" View "+il+" Strip "+ip+" Det "+idet+" ADC "+adc);
                  goodstrip= true;
                  if(inCRT&&il==2&&ip==53) goodstrip=false;
                  tdc = ((float)tdcc-tdcmax+1364000)/1000; 
                  if (goodstrip) fill(idet, is, il, ip, adc, tdc, tdcf); 
              }
          }
      }  
      //System.out.println(" ");
   }
    
   public void processECRec(DataEvent event) {
       
       if (app.isSingleEvent()) {
           for (int i=0; i<3; i++) app.getDetectorView().getView().removeLayer("L"+i);
           for (int i=0; i<3; i++) app.getDetectorView().getView().addLayer("L"+i);
       }
       
      int ipp = 0;
      
      if(event.hasBank("ECDetector::peaks")){
         EvioDataBank bank = (EvioDataBank) event.getBank("ECDetector::peaks");
         for(int i=0; i < bank.rows(); i++) {
            int   is  = bank.getInt("sector",i);
            int   il  = bank.getInt("layer",i);
            double en = bank.getDouble("energy",i);
            ecPix[getDet(il)].strips.hmap2.get("H2_a_Hist").get(is,4,0).fill(en*1e3,getLay(il),1.);  
            if (app.isSingleEvent()) {
                double xo = bank.getDouble("Xo",i);
                double yo = bank.getDouble("Yo",i);
                double zo = bank.getDouble("Zo",i);
                double xe = bank.getDouble("Xe",i);
                double ye = bank.getDouble("Ye",i);
                double ze = bank.getDouble("Ze",i);
                Line3D xyz = new Line3D(-xo,yo,zo,-xe,ye,ze);
                xyz.rotateZ(Math.toRadians(60*(is-1)));
                xyz.rotateY(Math.toRadians(25));
                xyz.translateXYZ(-333.1042, 0.0, 0.0);
                xyz.rotateZ(Math.toRadians(-60*(is-1)));
                Point3D orig = xyz.origin();
                Point3D  end = xyz.end();
                orig.setY(-orig.y()); end.setY(-end.y());                        
                DetectorShape2D shape = new DetectorShape2D(detNames[getDet(il)],is,ipp++,0); 
                shape.getShapePath().addPoint(orig.x(),orig.y(),0.);
                shape.getShapePath().addPoint(end.x(),end.y(),0.);
                app.getDetectorView().getView().addShape("L"+getDet(il), shape);
                double[] dum = {orig.x(),orig.y(),end.x(),end.y()};
                ecPix[getDet(il)].peakXY.get(is).add(dum);
//                System.out.println("sector,layer="+is+" "+il);  
//                System.out.println(orig.x()+" "+orig.y()+" "+orig.z());
//                System.out.println(end.x()+" "+end.y()+" "+end.z());
//                System.out.println(" ");
            }
           
//             double[] dum = {orig.x(),-orig.y(),orig.z(),end.x(),-end.y(),end.z()};
//            if (app.isSingleEvent()) ecPix[getDet(il)].peakXY.get(is).add(dum);
//
//            System.out.println("sector,layer="+is+" "+il);  
//            System.out.println("Xo,Yo,Zo= "+xo+" "+yo+" "+zo);
//           System.out.println("Xe,Ye,Ze= "+xe+" "+ye+" "+ze);
//            System.out.println("energy="+en);  
//            System.out.println(" ");
         }
        
      } 
      
      if(event.hasBank("ECDetector::clusters")){
          EvioDataBank bank = (EvioDataBank) event.getBank("ECDetector::clusters");
          int sign[] = {-1,1,1}; double esum=0;
          for(int i=0; i < bank.rows(); i++) {
             int is = bank.getInt("sector",i);
             int il = bank.getInt("layer",i);
             double energy = bank.getDouble("energy",i);
             double      X = bank.getDouble("X",i);
             double      Y = bank.getDouble("Y",i);
             double      Z = bank.getDouble("Z",i);
             double    pcR = Math.sqrt(X*X+Y*Y+Z*Z);
             double pcThet = Math.asin(Math.sqrt(X*X+Y*Y)/pcR)*180/3.14159;
             double    mcR = Math.sqrt(pcx*pcx+pcy*pcy+pcz*pcz);
             double mcThet = Math.asin(Math.sqrt(pcx*pcx+pcy*pcy)/mcR)*180/3.14159;
            System.out.println("Cluster: "+X+" "+Y+" "+Z);
            System.out.println("Cluster-target:"+pcR);
            System.out.println("GEMC-target:"+0.1*mcR);
            System.out.println("Cluster thet: "+pcThet);
            System.out.println("GEMC thet: "+mcThet);
            System.out.println(" ");
            if(getDet(il)==0) {
                ecPix[getDet(il)].strips.hmap2.get("H2_a_Hist").get(is,8,0).fill(0.1*pcx-X,1.);
                ecPix[getDet(il)].strips.hmap2.get("H2_a_Hist").get(is,8,0).fill(0.1*pcy-Y,2.);
                ecPix[getDet(il)].strips.hmap2.get("H2_a_Hist").get(is,8,0).fill(0.1*pcz-Z,3.);
            }
            ecPix[getDet(il)].strips.hmap2.get("H2_a_Hist").get(is,9,0).fill(25.0-mcThet,1.);
            ecPix[getDet(il)].strips.hmap2.get("H2_a_Hist").get(is,9,0).fill(pcThet-mcThet,2.);
            //Display uses tilted coordinates.  Must transform X,Y,Z from CLAS into tilted.
             Point3D xyz = new Point3D(-X,Y,Z);
             xyz.rotateZ(Math.toRadians(60*(is-1)));
             xyz.rotateY(Math.toRadians(25));
             xyz.translateXYZ(-333.1042, 0.0, 0.0);
             xyz.rotateZ(Math.toRadians(-60*(is-1)));
             double[] dum  = {xyz.x(),-xyz.y()}; 
//             System.out.println("sector,layer="+is+" "+il);  
//           System.out.println("Cluster: "+dum[0]+" "+dum[1]+" "+xyz.z());
//           System.out.println("Cluster: "+X+" "+Y+" "+Z);
             if (app.isSingleEvent()) ecPix[getDet(il)].clusterXY.get(is).add(dum);
             ecPix[getDet(il)].strips.hmap2.get("H2_a_Hist").get(is,4,0).fill(energy*1e3,4,1.); 
             ecPix[getDet(il)].strips.hmap2.get("H2_a_Hist").get(is,4,0).fill(energy*1e3,4,1.); 
             esum=esum+energy*1e3;
          }
         ecPix[0].strips.hmap2.get("H2_a_Hist").get(2,4,0).fill(esum,5,1.);            
      }
      
      if(event.hasBank("ECDetector::calib")){
          double raw[] = new double[3];
          double rec[] = new double[3];
          EvioDataBank bank = (EvioDataBank) event.getBank("ECDetector::calib");
          for(int i=0; i < bank.rows(); i++) {
             int is = bank.getInt("sector",i);
             int il = bank.getInt("layer",i);
             raw[0] = bank.getDouble("rawEU",i);
             raw[1] = bank.getDouble("rawEV",i);
             raw[2] = bank.getDouble("rawEW",i);
             rec[0] = bank.getDouble("recEU",i);
             rec[1] = bank.getDouble("recEV",i);
             rec[2] = bank.getDouble("recEW",i);
             
             for (int k=1; k<4; k++) ecPix[getDet(il)].strips.hmap2.get("H2_a_Hist").get(is,5,0).fill(raw[k-1]*1e3,k,1.);            
             for (int k=1; k<4; k++) ecPix[getDet(il)].strips.hmap2.get("H2_a_Hist").get(is,6,0).fill(rec[k-1]*1e3,k,1.);            
//             System.out.println("sector,layer ="+is+" "+il);  
//             System.out.println("X,Y,Z,energy="+X+" "+Y+" "+Z+" "+energy);  
//             System.out.println(" ");
          }
      }    
        
 
   }
   
   private class toLocal {
       
       void toLocal(int is, Line3D line) {
           line.rotateZ(Math.toRadians(60*(is-1)));
           line.rotateY(Math.toRadians(25));
           line.translateXYZ(-333.1042, 0.0, 0.0);
           line.rotateZ(Math.toRadians(-60*(is-1)));           
       }
       
       void toLocal(int is, Point3D point) {
           point.rotateZ(Math.toRadians(60*(is-1)));
           point.rotateY(Math.toRadians(25));
           point.translateXYZ(-333.1042, 0.0, 0.0);
           point.rotateZ(Math.toRadians(-60*(is-1)));                      
       }
       
   }
   
   public void clear(int idet) {
            
      for (int is=0 ; is<6 ; is++) {     
          ecPix[idet].uvwa[is] = 0;
          ecPix[idet].uvwt[is] = 0;
          ecPix[idet].mpix[is] = 0;
          ecPix[idet].esum[is] = 0;         
          for (int il=0 ; il<3 ; il++) {
             ecPix[idet].nha[is][il] = 0;
             ecPix[idet].nht[is][il] = 0;
             for (int ip=0 ; ip<ecPix[idet].ec_nstr[il] ; ip++) {
                 ecPix[idet].strra[is][il][ip]    = 0;
                 ecPix[idet].strrt[is][il][ip]    = 0;
                 ecPix[idet].adcr[is][il][ip]     = 0;
                 ecPix[idet].ftdcr[is][il][ip]    = 0;
                 ecPix[idet].tdcr[is][il][ip]     = 0;
             }
          }               
      }       
            
      if (app.isSingleEvent()) {
         for (int is=0 ; is<6 ; is++) {
            ecPix[idet].clusterXY.get(is+1).clear();
            for (int il=0 ; il<3 ; il++) {
                ecPix[idet].strips.hmap1.get("H1_Stra_Sevd").get(is+1,il+1,0).reset();
                ecPix[idet].strips.hmap1.get("H1_Stra_Sevd").get(is+1,il+1,1).reset();
                ecPix[idet].strips.hmap2.get("H2_Mode1_Sevd").get(is+1,il+1,0).reset();
                ecPix[idet].strips.hmap2.get("H2_Mode1_Sevd").get(is+1,il+1,1).reset();
                for (int ip=0; ip<ecPix[idet].pixels.getNumPixels() ; ip++) {
                    ecPix[idet].ecadcpix[is][il][ip] = 0;                
                }
                
            }
            for (int ip=0; ip<ecPix[idet].pixels.getNumPixels() ; ip++) {
                ecPix[idet].ecpixel[is][ip] = 0;                
                ecPix[idet].ecsumpix[is][ip] = 0;
            }
            for (int il=0 ; il<1 ; il++) {
               ecPix[idet].strips.hmap1.get("H1_Pixa_Sevd").get(is+1,il+1,0).reset();
            }
         }
      }           
   }
        
   public void fill(int idet, int is, int il, int ip, int adc, double tdc, double tdcf) {

       if(tdc>1200&&tdc<1500){
           ecPix[idet].uvwt[is-1]=ecPix[idet].uvwt[is-1]+ecPix[idet].uvw_dalitz(idet,il,ip); //Dalitz tdc 
           ecPix[idet].nht[is-1][il-1]++; int inh = ecPix[idet].nht[is-1][il-1];
           if (inh>nstr) inh=nstr;
           ecPix[idet].tdcr[is-1][il-1][inh-1] = tdc;
           ecPix[idet].strrt[is-1][il-1][inh-1] = ip;                  
           ecPix[idet].strips.hmap2.get("H2_t_Hist").get(is,il,0).fill(tdc,ip,1.);
           ecPix[idet].strips.hmap2.get("H2_PC_Stat").get(is,0,2).fill(ip,il,tdc);
       }
       
       ecPix[idet].strips.hmap1.get("H1_Stra_Sevd").get(is,il,1).fill(ip,0.1*adc);
       
       if(adc>ecPix[idet].getStripThr(config,il)){
           ecPix[idet].uvwa[is-1]=ecPix[idet].uvwa[is-1]+ecPix[idet].uvw_dalitz(idet,il,ip); //Dalitz adc
           ecPix[idet].nha[is-1][il-1]++; int inh = ecPix[idet].nha[is-1][il-1];
           if (inh>nstr) inh=nstr;
           ecPix[idet].adcr[is-1][il-1][inh-1] = adc;
           ecPix[idet].ftdcr[is-1][il-1][inh-1] = tdcf;
           ecPix[idet].strra[is-1][il-1][inh-1] = ip;
           ecPix[idet].strips.hmap2.get("H2_a_Hist").get(is,il,0).fill(adc,ip,1.);  
           ecPix[idet].strips.hmap2.get("H2_PC_Stat").get(is,0,0).fill(ip,il,1.);
           ecPix[idet].strips.hmap2.get("H2_PC_Stat").get(is,0,1).fill(ip,il,adc);
       }   
   }
   
  public void processSED(int idet) {
      for (int is=0; is<6; is++) {
          float[] sed7 = ecPix[idet].strips.hmap1.get("H1_Pixa_Sevd").get(is+1,1,0).getData();
          for (int il=1; il<4; il++ ){               
              for (int n=1 ; n<ecPix[idet].nha[is][il-1]+1 ; n++) {
                  int ip=ecPix[idet].strra[is][il-1][n-1]; float ad= (float) 0.1*ecPix[idet].adcr[is][il-1][n-1];
                  ecPix[idet].strips.hmap1.get("H1_Stra_Sevd").get(is+1,il,0).fill(ip,ad);
                  ecPix[idet].strips.putpixels(il,ip,ad,sed7);
              }
          }
          for (int i=0; i<sed7.length; i++) {
              ecPix[idet].strips.hmap1.get("H1_Pixa_Sevd").get(is+1,1,0).setBinContent(i, sed7[i]);  
          }
      }  
  }
  
   public void findPixels(int idet) {

       int u,v,w,ii;

       for (int is=0 ; is<6 ; is++) { // Loop over sectors
           for (int i=0; i<ecPix[idet].nha[is][0]; i++) { // Loop over U strips
               u=ecPix[idet].strra[is][0][i];
               for (int j=0; j<ecPix[idet].nha[is][1]; j++) { // Loop over V strips
                   v=ecPix[idet].strra[is][1][j];
                   for (int k=0; k<ecPix[idet].nha[is][2]; k++){ // Loop over W strips
                       w=ecPix[idet].strra[is][2][k];
                       int dalitz = u+v+w;
                       if (dalitz==73||dalitz==74) { // Dalitz test
                           ecPix[idet].mpix[is]++;      ii = ecPix[idet].mpix[is]-1;
                           ecPix[idet].ecadcpix[is][0][ii] = ecPix[idet].adcr[is][0][i];
                           ecPix[idet].ecadcpix[is][1][ii] = ecPix[idet].adcr[is][1][i];
                           ecPix[idet].ecadcpix[is][2][ii] = ecPix[idet].adcr[is][2][i];

                           ecPix[idet].ecsumpix[is][ii] = ecPix[idet].ecadcpix[is][0][ii]
                                                         +ecPix[idet].ecadcpix[is][1][ii]
                                                         +ecPix[idet].ecadcpix[is][2][ii];
                           ecPix[idet].esum[is]         = ecPix[idet].esum[is] + ecPix[idet].ecsumpix[is][ii];
                           ecPix[idet].ecpixel[is][ii]  = ecPix[idet].pixels.getPixelNumber(u,v,w);
                           ecPix[idet].strips.hmap1.get("H1_Pixa_Sevd").get(is+1,1,0).fill(ecPix[idet].ecpixel[is][ii],ecPix[idet].esum[is]); 
                       }
                   }
               }
           }
       }
           //              if (is==1){
           //                  System.out.println("is,inner nhit="+is+" "+nha[is][3]+","+nha[is][4]+","+nha[is][5]);
           //                  System.out.println("is,outer nhit="+is+" "+nha[is][6]+","+nha[is][7]+","+nha[is][8]);
           //                  System.out.println("mpix,ecpix="+mpix[is][0]+","+mpix[is][1]+","+ecpixel[is][0][0]+","+ecpixel[is][1][0]);
           //                  System.out.println(" ");
           //              } 
   }

        
   public void processPixels(int idet) {
	   
	   int stripnum = 0;
	   	int ijunk;
	   	int counter = 0;
	       Scanner scanner;
	       //generted values
	       try 
			{
	       	scanner = new Scanner(new File("/home/chetry/PCAL/TayaPCAL/src/org/jlab/calib/AttenCoeffm5run4294.dat"));
				while(scanner.hasNextInt())
				{
					if(counter < 68)
					{
						ijunk = scanner.nextInt();
						stripnum = scanner.nextInt();
						//umaxX[stripnum - 1] = scanner.nextDouble();
						genuA[stripnum - 1] = scanner.nextDouble();
						genuB[stripnum - 1] = scanner.nextDouble();
						genuC[stripnum - 1] = scanner.nextDouble();
						ijunk = scanner.nextInt();
					}
					else if(counter < 130)
					{
						ijunk = scanner.nextInt();
						stripnum = scanner.nextInt();
						//vmaxX[stripnum - 1] = scanner.nextDouble();
						genvA[stripnum - 1] = scanner.nextDouble();
						genvB[stripnum - 1] = scanner.nextDouble();
						genvC[stripnum - 1] = scanner.nextDouble();
						ijunk = scanner.nextInt();
					}
					else
					{
						ijunk = scanner.nextInt();
						stripnum = scanner.nextInt();
						//wmaxX[stripnum - 1] = scanner.nextDouble();
						genwA[stripnum - 1] = scanner.nextDouble();
						genwB[stripnum - 1] = scanner.nextDouble();
						genwC[stripnum - 1] = scanner.nextDouble();
						ijunk = scanner.nextInt();
					}
					++counter;
					
				}
				scanner.close();
			}
			catch (FileNotFoundException e1) 
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

       boolean good_ua, good_va, good_wa, good_uvwa;
       boolean[] good_pix = {false,false,false};
       boolean good_ut, good_vt, good_wt, good_uvwt;
       boolean good_dalitz=false, good_pixel;
       int pixel;
       int tpixel;
  	

       TreeMap<Integer, Object> map= (TreeMap<Integer, Object>) ecPix[idet].Lmap_a.get(0,0,1); 
       float pixelLength[] = (float[]) map.get(1);
       
       for (int is=0 ; is<6 ; is++) {      

               good_ua = ecPix[idet].nha[is][0]==1;
               good_va = ecPix[idet].nha[is][1]==1;
               good_wa = ecPix[idet].nha[is][2]==1;
               
               good_uvwa = good_ua && good_va && good_wa; //Multiplicity test (NU=NV=NW=1)
               
               //here the cut is made!!
               good_pix[0] = good_ua && ecPix[idet].adcr[is][1][0] > 35 && ecPix[idet].adcr[is][2][0] > 35;
               good_pix[1] = good_va && ecPix[idet].adcr[is][0][0] > 35 && ecPix[idet].adcr[is][2][0] > 35; 
               good_pix[2] = good_wa && ecPix[idet].adcr[is][0][0] > 35 && ecPix[idet].adcr[is][1][0] > 35;  

               if (idet>0)   good_dalitz = (ecPix[idet].uvwa[is]-2.0)>0.02 && (ecPix[idet].uvwa[is]-2.0)<0.056; //EC               
               if (idet==0 ) good_dalitz = Math.abs(ecPix[idet].uvwa[is]-2.0)<0.1; //PCAL
               
               
               
               pixel = ecPix[idet].pixels.getPixelNumber(ecPix[idet].strra[is][0][0],ecPix[idet].strra[is][1][0],ecPix[idet].strra[is][2][0]);
               good_pixel = pixel!=0;

                              ecPix[idet].strips.hmap2.get("H2_PC_Stat").get(is+1,0,3).fill(ecPix[idet].uvwa[is]-2.0,1.,1.);
               if (good_uvwa) ecPix[idet].strips.hmap2.get("H2_PC_Stat").get(is+1,0,3).fill(ecPix[idet].uvwa[is]-2.0,2.,1.);
               
               /* 
                * 
                * ecPix[idet].adcr[is][0][0] == adu
                * ecPix[idet].adcr[is][1][0] == adv
                * ecPix[idet].adcr[is][2][0] == adw
                * 
                * ecPix[idet].strra[is][0][0] == uStripNum
                * ecPix[idet].strra[is][1][0] == vStripNum
                * ecPix[idet].strra[is][2][0] == wStripNum
                * 
                * 
                *  */

               /*if (is == 2 && idet == 0 && good_pixel){
            	   tpixel = ecPix[idet].pixels.getPixelNumber(ecPix[idet].strra[is][0][0],ecPix[idet].strra[is][1][0],ecPix[idet].strra[is][2][0]);
                System.out.println(
                		"uStrip = " + (ecPix[idet].strra[is][0][0]-1) + "      " + 
                		"vStrip = " + (ecPix[idet].strra[is][1][0]-1) + "      " +
                		"wStrip = " + (ecPix[idet].strra[is][2][0]-1)
                		);
                System.out.println(
                		"u distance = " + ecPix[idet].pixels.getUdist(tpixel) + "      " +
                		"v distance = " + ecPix[idet].pixels.getVdist(tpixel) + "      " +
                		"w distance = " + ecPix[idet].pixels.getWdist(tpixel)
                		);
               }*/

               if (idet == 0 && is == 2 && good_pixel)
               {
            	   tpixel = ecPix[idet].pixels.getPixelNumber(ecPix[idet].strra[is][0][0],ecPix[idet].strra[is][1][0],ecPix[idet].strra[is][2][0]);
            	   
	               if(Math.abs(ecPix[idet].adcr[is][1][0] - (genvA[ecPix[idet].strra[is][1][0] -1]*Math.exp(genvB[ecPix[idet].strra[is][1][0] - 1] * 
	            		   ecPix[idet].calDB.getVPixelDistance(ecPix[idet].strra[is][0][0], ecPix[idet].strra[is][1][0], ecPix[idet].strra[is][2][0]) + 
	            		   genvC[ecPix[idet].strra[is][1][0] - 1]))) < 100.0
	   	           && Math.abs(ecPix[idet].adcr[is][2][0] - (genwA[ecPix[idet].strra[is][2][0] -1]*Math.exp(genwB[ecPix[idet].strra[is][2][0] - 1] *  
	   	        		   ecPix[idet].calDB.getWPixelDistance(ecPix[idet].strra[is][0][0], ecPix[idet].strra[is][1][0], ecPix[idet].strra[is][2][0]) + 
	   	        		   genwC[ecPix[idet].strra[is][2][0] - 1]))) < 100.0)
	            	   ijunk = 1;
	               else
	            	   good_pix[0] = false;
	               
	               if(Math.abs(ecPix[idet].adcr[is][0][0] - (genuA[ecPix[idet].strra[is][0][0] -1]*Math.exp(genuB[ecPix[idet].strra[is][0][0] - 1] *  
	            		   ecPix[idet].calDB.getUPixelDistance(ecPix[idet].strra[is][0][0], ecPix[idet].strra[is][1][0], ecPix[idet].strra[is][2][0]) + 
	            		   genuC[ecPix[idet].strra[is][0][0] - 1]))) < 100.0
	    	   	   && Math.abs(ecPix[idet].adcr[is][2][0] - (genwA[ecPix[idet].strra[is][2][0] -1]*Math.exp(genwB[ecPix[idet].strra[is][2][0] - 1] *  
	    	   			   ecPix[idet].calDB.getWPixelDistance(ecPix[idet].strra[is][0][0], ecPix[idet].strra[is][1][0], ecPix[idet].strra[is][2][0]) + 
	    	   			   genwC[ecPix[idet].strra[is][2][0] - 1]))) < 100.0)
	            	   ijunk = 1;
	               else
	            	   good_pix[1] = false;
	               
	               if(Math.abs(ecPix[idet].adcr[is][0][0] - (genuA[ecPix[idet].strra[is][0][0] -1]*Math.exp(genuB[ecPix[idet].strra[is][0][0] - 1] *  
	            		   ecPix[idet].calDB.getUPixelDistance(ecPix[idet].strra[is][0][0], ecPix[idet].strra[is][1][0], ecPix[idet].strra[is][2][0]) + 
	            		   genuC[ecPix[idet].strra[is][0][0] - 1]))) < 100.0
	    	   	   && Math.abs(ecPix[idet].adcr[is][1][0] - (genvA[ecPix[idet].strra[is][1][0] -1]*Math.exp(genvB[ecPix[idet].strra[is][1][0] - 1] *  
	    	   			   ecPix[idet].calDB.getVPixelDistance(ecPix[idet].strra[is][0][0], ecPix[idet].strra[is][1][0], ecPix[idet].strra[is][2][0]) + 
	    	   			   genwC[ecPix[idet].strra[is][1][0] - 1]))) < 100.0 )
	            	   ijunk = 1;
	               else
	            	   good_pix[2] = false;
               }
   	          
            
               
               
               if (good_dalitz && good_pixel && good_uvwa && idet == 0) { 
                   
                   double area = ecPix[idet].pixels.getZoneNormalizedArea(pixel);
                   int    zone = ecPix[idet].pixels.getZone(pixel);
                   
                   ecPix[idet].strips.hmap2.get("H2_PC_Stat").get(is+1,0,4).fill(area,zone,1.);
                   ecPix[idet].pixels.hmap1.get("H1_a_Maps").get(is+1,7,0).fill(pixel,1.0); // Events per pixel
                   ecPix[idet].pixels.hmap1.get("H1_a_Maps").get(is+1,7,3).fill(pixel,1.0/ecPix[idet].pixels.getNormalizedArea(pixel)); //Normalized to pixel area

                   for (int il=1; il<4 ; il++){
                       double adcc = ecPix[idet].adcr[is][il-1][0]/pixelLength[pixel-1];
                       if (good_pix[il-1]) {
                         ecPix[idet].pixels.hmap1.get("H1_a_Maps").get(is+1,il,4).fill(pixel,1.0); // Events per pixel
                         ecPix[idet].strips.hmap2.get("H2_a_Hist").get(is+1,il,1).fill(adcc,ecPix[idet].strra[is][il-1][0],1.0) ;
                         ecPix[idet].strips.hmap2.get("H2_a_Hist").get(is+1,il,2).fill(adcc,pixel,1.0);                        
                         ecPix[idet].pixels.hmap1.get("H1_a_Maps").get(is+1,7,1).fill(pixel,adcc);
                         ecPix[idet].pixels.hmap1.get("H1_a_Maps").get(is+1,il,0).fill(pixel,adcc);
                         ecPix[idet].pixels.hmap1.get("H1_a_Maps").get(is+1,il,2).fill(pixel,Math.pow(adcc,2));
                       }
                   }
               }  
               
               good_ut = ecPix[idet].nht[is][0]==1;
               good_vt = ecPix[idet].nht[is][1]==1;
               good_wt = ecPix[idet].nht[is][2]==1;
               
               good_uvwt = good_ut && good_vt && good_wt; //Multiplicity test (NU=NV=NW=1)                 
               
               if (idet>0)  good_dalitz = (ecPix[idet].uvwt[is]-2.0)>0.02 && (ecPix[idet].uvwt[is]-2.0)<0.056; //EC               
               if (idet==0) good_dalitz = Math.abs(ecPix[idet].uvwt[is]-2.0)<0.1; //PCAL
               
               pixel  = ecPix[idet].pixels.getPixelNumber(ecPix[idet].strrt[is][0][0],ecPix[idet].strrt[is][1][0],ecPix[idet].strrt[is][2][0]);
               good_pixel  = pixel!=0;

               if (good_uvwt && good_dalitz && good_pixel) { 
                   ecPix[idet].pixels.hmap1.get("H1_t_Maps").get(is+1,7,0).fill(pixel,1.0);
                   ecPix[idet].pixels.hmap1.get("H1_t_Maps").get(is+1,7,3).fill(pixel,1.0/ecPix[idet].pixels.getNormalizedArea(pixel)); //Normalized to pixel area
                   for (int il=1; il<4 ; il++){
                       ecPix[idet].strips.hmap2.get("H2_t_Hist").get(is+1,il,1).fill(ecPix[idet].tdcr[is][il-1][0],ecPix[idet].strrt[is][il-1][0],1.0) ;
                       ecPix[idet].strips.hmap2.get("H2_t_Hist").get(is+1,il,2).fill(ecPix[idet].tdcr[is][il-1][0],pixel,1.0);                       
                       ecPix[idet].pixels.hmap1.get("H1_t_Maps").get(is+1,7,1).fill(pixel,ecPix[idet].tdcr[is][il-1][0]);
                       ecPix[idet].pixels.hmap1.get("H1_t_Maps").get(is+1,il,0).fill(pixel,ecPix[idet].tdcr[is][il-1][0]);
                   }
               }   
       }  
       
   }
  
   public void makeMaps(int idet) {

       DetectorCollection<H2F> H2_a_Hist    = new DetectorCollection<H2F>() ; 
       DetectorCollection<H2F> H2_t_Hist    = new DetectorCollection<H2F>() ; 
       DetectorCollection<H1F> H1_Stra_Sevd = new DetectorCollection<H1F>() ;
       DetectorCollection<H1F> H1_Pixa_Sevd = new DetectorCollection<H1F>() ; 
       DetectorCollection<H1F> H1_a_Maps    = new DetectorCollection<H1F>() ;
       DetectorCollection<H1F> H1_t_Maps    = new DetectorCollection<H1F>() ;
      
       H2_a_Hist    = ecPix[idet].strips.hmap2.get("H2_a_Hist");
       H2_t_Hist    = ecPix[idet].strips.hmap2.get("H2_t_Hist");
       H1_Stra_Sevd = ecPix[idet].strips.hmap1.get("H1_Stra_Sevd");
       H1_Pixa_Sevd = ecPix[idet].strips.hmap1.get("H1_Pixa_Sevd");
       H1_a_Maps    = ecPix[idet].pixels.hmap1.get("H1_a_Maps");
       H1_t_Maps    = ecPix[idet].pixels.hmap1.get("H1_t_Maps");

       // Layer assignments:
       // il=1-3 (U,V,W strips) il=7 (Inner Pixels) il=8 (Outer Pixels)
         
        for (int is=1;is<7;is++) {
           for (int il=1 ; il<4 ; il++) {
               divide(H1_a_Maps.get(is,il,0),H1_a_Maps.get(is,il,4),H1_a_Maps.get(is,il,1)); //Normalize Raw View ADC   to Events
               divide(H1_a_Maps.get(is,il,2),H1_a_Maps.get(is,il,4),H1_a_Maps.get(is,il,3)); //Normalize Raw View ADC^2 to Events
               divide(H1_t_Maps.get(is,il,0),H1_t_Maps.get(is,il,0),H1_t_Maps.get(is,il,1)); //Normalize Raw View TDC   to Events
               ecPix[idet].Lmap_a.add(is,il,   0, toTreeMap(H2_a_Hist.get(is,il,0).projectionY().getData()));  //Strip View ADC  
               ecPix[idet].Lmap_a.add(is,il+10,0, toTreeMap(H1_a_Maps.get(is,il,1).getData()));                //Pixel View ADC 
               ecPix[idet].Lmap_t.add(is,il,   0, toTreeMap(H2_t_Hist.get(is,il,0).projectionY().getData()));  //Strip View TDC  
               ecPix[idet].Lmap_t.add(is,il,   1, toTreeMap(H1_t_Maps.get(is,il,1).getData()));                //Pixel View TDC  
           }
           
           divide(H1_a_Maps.get(is, 7, 1),H1_a_Maps.get(is, 7, 0),H1_a_Maps.get(is, 7, 2)); // Normalize Raw ADC Sum to Events
           divide(H1_t_Maps.get(is, 7, 1),H1_t_Maps.get(is, 7, 0),H1_t_Maps.get(is, 7, 2)); // Normalize Raw TDC Sum to Events 
           ecPix[idet].Lmap_a.add(is, 7,0, toTreeMap(H1_a_Maps.get(is,7,0).getData())); //Pixel Events  
           ecPix[idet].Lmap_a.add(is, 7,1, toTreeMap(H1_a_Maps.get(is,7,3).getData())); //Pixel Events Normalized  
           ecPix[idet].Lmap_a.add(is, 9,0, toTreeMap(H1_a_Maps.get(is,7,2).getData())); //Pixel U+V+W ADC     
           ecPix[idet].Lmap_t.add(is, 7,2, toTreeMap(H1_t_Maps.get(is,7,2).getData())); //Pixel U+V+W TDC  
           if (app.isSingleEvent()){
               for (int il=1 ; il<4 ; il++) ecPix[idet].Lmap_a.add(is,il,0,   toTreeMap(H1_Stra_Sevd.get(is,il,0).getData())); 
               for (int il=1 ; il<2 ; il++) ecPix[idet].Lmap_a.add(is,il+6,0, toTreeMap(H1_Pixa_Sevd.get(is,il,0).getData())); 
           }
       }
     
   }

   public TreeMap<Integer, Object> toTreeMap(float dat[]) {
       TreeMap<Integer, Object> hcontainer = new TreeMap<Integer, Object>();
       hcontainer.put(1, dat);
       float[] b = Arrays.copyOf(dat, dat.length);
       double min=100000,max=0;
       for (int i =0 ; i < b.length; i++){
           if (b[i] !=0 && b[i] < min) min=b[i];
           if (b[i] !=0 && b[i] > max) max=b[i];
       }
       // Arrays.sort(b);
       // double min = b[0]; double max=b[b.length-1];
       if (min<=0) min=0.01;
       hcontainer.put(2, min);
       hcontainer.put(3, max);
       return hcontainer;        
   }

   public void divide(H1F h1, H1F h2, H1F h3){      
       if(h1.getXaxis().getNBins()!=h2.getXaxis().getNBins()){
           System.out.println("[H1D::divide] error : histograms have inconsistent bins");
           return;
       }       
       StatNumber   numer = new StatNumber();
       StatNumber   denom = new StatNumber();
       for(int bin = 0; bin < h1.getXaxis().getNBins(); bin++){
           numer.set(h1.getBinContent(bin), h1.getBinError(bin));
           denom.set(h2.getBinContent(bin), h2.getBinError(bin));
           numer.divide(denom);
           h3.setBinContent(bin, numer.number());
           h3.setBinError(bin, numer.error());
       }
   }
   
   /*public void getAttenuationCoefficients()
   {
   	int stripnum = 0;
   	int ijunk;
   	int counter = 0;
       Scanner scanner;
       //generted values
       try 
		{
       	scanner = new Scanner(new File("/home/chetry/PCAL/TayaPCAL/src/org/jlab/calib/AttenCoeffm5run4294.dat"));
			while(scanner.hasNextInt())
			{
				if(counter < 68)
				{
					ijunk = scanner.nextInt();
					stripnum = scanner.nextInt();
					//umaxX[stripnum - 1] = scanner.nextDouble();
					genuA[stripnum - 1] = scanner.nextDouble();
					genuB[stripnum - 1] = scanner.nextDouble();
					genuC[stripnum - 1] = scanner.nextDouble();
					ijunk = scanner.nextInt();
				}
				else if(counter < 130)
				{
					ijunk = scanner.nextInt();
					stripnum = scanner.nextInt();
					//vmaxX[stripnum - 1] = scanner.nextDouble();
					genvA[stripnum - 1] = scanner.nextDouble();
					genvB[stripnum - 1] = scanner.nextDouble();
					genvC[stripnum - 1] = scanner.nextDouble();
					ijunk = scanner.nextInt();
				}
				else
				{
					ijunk = scanner.nextInt();
					stripnum = scanner.nextInt();
					//wmaxX[stripnum - 1] = scanner.nextDouble();
					genwA[stripnum - 1] = scanner.nextDouble();
					genwB[stripnum - 1] = scanner.nextDouble();
					genwC[stripnum - 1] = scanner.nextDouble();
					ijunk = scanner.nextInt();
				}
				++counter;
				
			}
			scanner.close();
		}
		catch (FileNotFoundException e1) 
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
   }
   */
}
    
    


