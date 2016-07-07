package org.clas.fcmon.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

import org.clas.fcmon.tools.ECPixels;
import org.clas.fcmon.tools.FADCFitter;
import org.clas.fcmon.tools.FCApplication;
import org.jlab.clas.detector.BankType;
import org.jlab.clas.detector.DetectorBankEntry;
import org.jlab.clas.detector.DetectorCollection;
import org.jlab.clas.tools.utils.DataUtils;
import org.jlab.clas12.detector.EventDecoder;
import org.jlab.clas12.detector.FADCConfig;
import org.jlab.clas12.detector.FADCConfigLoader;
import org.jlab.detector.decode.CodaEventDecoder;
import org.jlab.detector.decode.DetectorDataDgtz;
import org.jlab.detector.decode.DetectorEventDecoder;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.evio.clas12.EvioDataBank;
//import org.jlab.evio.clas12.EvioDataEvent;
import org.root.histogram.H1D;
import org.root.histogram.H2D;

public class ECReconstructionApp extends FCApplication {
    
   FADCConfigLoader fadc  = new FADCConfigLoader();
   String          mondet ;
   Boolean           inMC ;
   int              detID ;
   FADCFitter     fitter  = new FADCFitter(1,15);
   EventDecoder   decoder = new EventDecoder();
   String BankType        ;
   CodaEventDecoder            newdecoder = new CodaEventDecoder();
   DetectorEventDecoder   detectorDecoder = new DetectorEventDecoder();
   List<DetectorDataDgtz>  detectorData   = new ArrayList<DetectorDataDgtz>();
   
   DetectorCollection<H2D> H2_PCa_Hist; 
   DetectorCollection<H2D> H2_PCt_Hist;  
   DetectorCollection<H2D> H2_PCa_Sevd;  
   DetectorCollection<H2D> H2_PC_Stat;  
   DetectorCollection<H1D> H1_PCa_Maps;  
   DetectorCollection<H1D> H1_PCt_Maps;  
   DetectorCollection<H1D> H1_Stra_Sevd;  
   DetectorCollection<H1D> H1_Pixa_Sevd;  
 
   public DetectorCollection<TreeMap<Integer,Object>> Lmap_a = new DetectorCollection<TreeMap<Integer,Object>>();
   public DetectorCollection<TreeMap<Integer,Object>> Lmap_t = new DetectorCollection<TreeMap<Integer,Object>>();
   
   double[]                sed7=null,sed8=null;
   TreeMap<Integer,Object> map7=null,map8=null; 
   
   int nstr = ecPix[0].pc_nstr[0];
   int npix = ecPix[0].pixels.getNumPixels();
   
   int        nha[][] = new    int[6][9];
   int        nht[][] = new    int[6][9];
   int    strra[][][] = new    int[6][9][nstr]; 
   int    strrt[][][] = new    int[6][9][nstr]; 
   int     adcr[][][] = new    int[6][9][nstr];
   double ftdcr[][][] = new double[6][9][nstr];
   double  tdcr[][][] = new double[6][9][nstr];
   double    uvwa[][] = new double[6][9];
   double    uvwt[][] = new double[6][9];
   int       mpix[][] = new    int[6][3];
   int       esum[][] = new    int[6][3];
   int ecadcpix[][][] = new    int[6][9][npix];
   int ecsumpix[][][] = new    int[6][9][npix];
   int  ecpixel[][][] = new    int[6][9][npix];   
   
   int nsa,nsb,tet,pedref;     
   int thr[] = {15,15,20};
   short[] pulse = new short[100]; 
    
   public ECReconstructionApp(String name, ECPixels[] ecPix) {
       super(name,ecPix);
       // TODO Auto-generated constructor stub
   }
   
   public void init() {
       fadc.load("/daq/fadc/ec",10,"default");
       mondet =           (String) mon.getGlob().get("mondet");
       inMC   =          (Boolean) mon.getGlob().get("inMC");
       detID  =              (int) mon.getGlob().get("detID");     
   }
        
   public void getMode7(int cr, int sl, int ch) {    
      app.mode7Emulation.configMode7(cr,sl,ch);
      this.nsa    = app.mode7Emulation.nsa;
      this.nsb    = app.mode7Emulation.nsb;
      this.tet    = app.mode7Emulation.tet;
      this.pedref = app.mode7Emulation.pedref;
   }
   
   public void addEvent(EvioDataEvent event) {
      if(event.hasBank(mondet+"::true")!=true) {
         this.updateRealData(event);
      } else {
         this.updateSimulatedData(event);
      }
   }
   
   public void updateRealData(EvioDataEvent event){

      int adc,ped,npk;
      double tdc=0,tdcf=0;
      String AdcType ;
      
      List<DetectorDataDgtz>  dataSet = newdecoder.getDataEntries((EvioDataEvent) event);
      
      detectorDecoder.translate(dataSet);   
      detectorDecoder.fitPulses(dataSet);
      this.detectorData.clear();
      this.detectorData.addAll(dataSet);
      
      clear();
      
      for (DetectorDataDgtz strip : detectorData) {
         if(strip.getDescriptor().getType().getName()=="EC") {
            adc=ped=pedref=npk=0 ; tdc=tdcf=0;
            int icr = strip.getDescriptor().getCrate(); 
            int isl = strip.getDescriptor().getSlot(); 
            int ich = strip.getDescriptor().getChannel(); 
            int is  = strip.getDescriptor().getSector();
            int il  = strip.getDescriptor().getLayer(); // 1-3: PCAL 4-9: ECAL
            int ip  = strip.getDescriptor().getComponent();
            int iord= strip.getDescriptor().getOrder(); 
            
            if (il>3) {
            il = il-3;    
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
                     ecPix[0].strips.hmap2.get("H2_Mode1_Hist").get(is,il,0).fill(i,ip,pulse[i]-this.pedref);
                     if (app.isSingleEvent()) {
                        ecPix[0].strips.hmap2.get("H2_Mode1_Sevd").get(is,il,0).fill(i,ip,pulse[i]-this.pedref);
                        int w1 = fitter.t0-this.nsb ; int w2 = fitter.t0+this.nsa;
                        if (fitter.adc>0&&i>=w1&&i<=w2) ecPix[0].strips.hmap2.get("H2_Mode1_Sevd").get(is,il,1).fill(i,ip,pulse[i]-this.pedref);                     
                     }
                  }
               }   
            }
            
            if (strip.getTDCSize()>0) tdc = strip.getTDCData(0).getTime();
                    
            if (ped>0) ecPix[0].strips.hmap2.get("H2_Peds_Hist").get(is,il,0).fill(this.pedref-ped, ip);
            fill(is, il, ip, adc, tdc, tdcf);  
            }
         }
      }
   }
   
   public void updateSimulatedData(EvioDataEvent event) {
       
       float tdcmax=100000;
       boolean debug=false;
       int adc,ped,npk=0,timf=0,timc=0;
       double mc_t=0.,tdc=0,tdcf=0;
         
      // SIMULATED EVENT
      
      if(event.hasBank(mondet+"::true")==true){
         EvioDataBank bank  = (EvioDataBank) event.getBank(mondet+"::true");
         int nrows = bank.rows();
         for(int i=0; i < nrows; i++){
            mc_t = bank.getDouble("avgT",i);
         }   
      }
                
      if(event.hasBank(mondet+"::dgtz")==true){
        
         inMC = true; mon.putGlob("inMC",true); thr[0]=thr[1]=5;
         clear();
        
         EvioDataBank bank = (EvioDataBank) event.getBank(mondet+"::dgtz");
        
         for(int i = 0; i < bank.rows(); i++){
            float dum = (float)bank.getInt("TDC",i)-(float)mc_t*1000;
            if (dum<tdcmax) tdcmax=dum;
         }      
       
         for(int i = 0; i < bank.rows(); i++){
            int is  = bank.getInt("sector",i);
            int ip  = bank.getInt("strip",i);
            int ic  = bank.getInt("stack",i);     
            int il  = bank.getInt("view",i);  
                adc = bank.getInt("ADC",i);
           int tdcc = bank.getInt("TDC",i);
               tdcf = tdcc;
               //System.out.println("sector,strip,stack,view,ADC="+is+" "+ip+" "+ic+" "+il+" "+adc);
                tdc = (((float)tdcc-(float)mc_t*1000)-tdcmax+1340000)/1000;                      
           if(ic==1||ic==2) fill(is, il+(ic-1)*3, ip, adc, tdc, tdcf);                                 
         }       
       
       processECRec(event);

      }
            
      if (app.isSingleEvent()) {
         findPixels();     // Process all pixels for SED
         processSED();
      } else {
        processPixels();  // Process only single pixels 
      }
   }
    
   public void processECRec(EvioDataEvent event) {
        
      if(event.hasBank("ECDetector::hits")){
         EvioDataBank bank = (EvioDataBank) event.getBank("ECDetector::hits");
         for(int i=0; i < bank.rows(); i++) {
            int is  = bank.getInt("sector",i);
            int il  = bank.getInt("layer",i);
            int ip  = bank.getInt("strip",i);
            int id  = bank.getInt("peakID",i);
          double en = bank.getDouble("energy",i);
           // System.out.println("sector,layer,strip="+is+" "+il+" "+ip);  
           // System.out.println("peakid,energy="+id+" "+en+" ");  
           // System.out.println(" ");
         }
      }  
      if(event.hasBank("ECDetector::clusters")){
          EvioDataBank bank = (EvioDataBank) event.getBank("ECDetector::clusters");
          for(int i=0; i < bank.rows(); i++) {
             int is = bank.getInt("sector",i);
             int il = bank.getInt("layer",i);
             double energy = bank.getDouble("energy",i);
             double      X = bank.getDouble("X",i);
             double      Y = bank.getDouble("Y",i);
             double      Z = bank.getDouble("Z",i);
          
            // System.out.println("sector,layer ="+is+" "+il);  
            // System.out.println("X,Y,Z,energy="+X+" "+Y+" "+Z+" "+energy);  
            // System.out.println(" ");
          }
       }  
   }
   
   public void clear() {
            
      for (int is=0 ; is<6 ; is++) {
          
         for (int il=0 ; il<3 ; il++) {
            mpix[is][il] = 0;
            esum[is][il] = 0;
         }
         
         for (int il=0 ; il<9 ; il++) {
            nha[is][il]  = 0;
            nht[is][il]  = 0;
            uvwa[is][il] = 0;
            uvwt[is][il] = 0;
            for (int ip=0 ; ip<nstr ; ip++) {
               strra[is][il][ip] = 0;
               strrt[is][il][ip] = 0;
               adcr[is][il][ip] = 0;
               ftdcr[is][il][ip] = 0;
               tdcr[is][il][ip] = 0;
               ecadcpix[is][il][ip] = 0;
               ecpixel[is][il][ip] = 0;
            }
         }               
      }       
            
      if (app.isSingleEvent()) {
         for (int is=0 ; is<6 ; is++) {
            for (int il=1 ; il<7 ; il++) {
               ecPix[0].strips.hmap1.get("H1_Stra_Sevd").get(is+1,il,0).reset();
               ecPix[0].strips.hmap2.get("H2_Mode1_Sevd").get(is+1,il,0).reset();
               ecPix[0].strips.hmap2.get("H2_Mode1_Sevd").get(is+1,il,1).reset();
            }
            for (int il=1 ; il<3 ; il++) {
               ecPix[0].strips.hmap1.get("H1_Pixa_Sevd").get(is+1,il,0).reset();
            }
         }
      }           
   }
        
   public void fill(int is, int il, int ip, int adc, double tdc, double tdcf) {

       int ic=0,iil;

       if (mondet=="EC")    ic=1;  
       if (mondet=="PCAL")  ic=0;

       iil = il;
       if (il>3) {ic=2; iil=il-3;}

       int  iv = il+3;

       if(tdc>1200&&tdc<1500){
           uvwt[is-1][ic]=uvwt[is-1][il]+ecPix[0].uvw_dalitz(ic,il,ip); //Dalitz tdc 
           nht[is-1][iv-1]++; int inh = nht[is-1][iv-1];
           tdcr[is-1][iv-1][inh-1] = tdc;
           strrt[is-1][iv-1][inh-1] = ip;                  
           ecPix[0].strips.hmap2.get("H2_PCt_Hist").get(is,il,0).fill(tdc,ip,1.0);
           ecPix[0].strips.hmap2.get("H2_PC_Stat").get(is,ic,2).fill(ip,iil,tdc);
       }

       if(adc>thr[ic]){
           uvwa[is-1][ic]=uvwa[is-1][ic]+ecPix[0].uvw_dalitz(ic,il,ip); //Dalitz adc
           nha[is-1][iv-1]++; int inh = nha[is-1][iv-1];
           adcr[is-1][iv-1][inh-1] = adc;
           ftdcr[is-1][iv-1][inh-1] = tdcf;
           strra[is-1][iv-1][inh-1] = ip;
           ecPix[0].strips.hmap2.get("H2_PCa_Hist").get(is,il,0).fill(adc,ip,1.0);
           ecPix[0].strips.hmap2.get("H2_PC_Stat").get(is,ic,0).fill(ip,iil,1.);
           ecPix[0].strips.hmap2.get("H2_PC_Stat").get(is,ic,1).fill(ip,iil,adc);
       }   
   }
        
   public void findPixels() {

       int u,v,w,ii;

       for (int is=0 ; is<6 ; is++) { // Loop over sectors
           for (int io=0; io<2 ; io++) { // Loop over calorimeter layers 
               int off = 3*io;
               int off1 = off+3;
               int off2 = off+4;
               int off3 = off+5;
               for (int i=0; i<nha[is][off1]; i++) { // Loop over U strips
                   u=strra[is][off1][i];
                   for (int j=0; j<nha[is][off2]; j++) { // Loop over V strips
                       v=strra[is][off2][j];
                       for (int k=0; k<nha[is][off3]; k++){ // Loop over W strips
                           w=strra[is][off3][k];
                           int dalitz = u+v+w;
                           if (dalitz==73||dalitz==74) { // Dalitz test
                               mpix[is][io]++; ii = mpix[is][io]-1;
                               ecadcpix[is][off1][ii] = adcr[is][off1][i];
                               ecadcpix[is][off2][ii] = adcr[is][off2][i];
                               ecadcpix[is][off3][ii] = adcr[is][off3][i];

                               ecsumpix[is][io][ii] = ecadcpix[is][off1][ii]+ecadcpix[is][off2][ii]+ecadcpix[is][off3][ii];
                               esum[is][io]     = esum[is][io]+ecsumpix[is][io][ii];
                               ecpixel[is][io][ii] = ecPix[0].pixels.getPixelNumber(u,v,w);
                               ecPix[0].strips.hmap1.get("H1_Pixa_Sevd").get(is+1,io+1,0).fill(ecpixel[is][io][ii],esum[is][io]);                               }
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
   }
    
   public void processSED() {

       for (int is=0; is<6; is++) {
           map7 = new TreeMap<Integer,Object>(H1_Pixa_Sevd.get(is+1,1,0).toTreeMap());
           map8 = new TreeMap<Integer,Object>(H1_Pixa_Sevd.get(is+1,2,0).toTreeMap());
           sed7 = (double[]) map7.get(5); sed8 = (double[]) map8.get(5);   
           for (int il=1; il<7; il++ ){
               int iv = il+3;
               for (int n=1 ; n<nha[is][iv-1]+1 ; n++) {
                   int ip=strra[is][iv-1][n-1]; int ad=adcr[is][iv-1][n-1];
                   ecPix[0].strips.hmap1.get("H1_Stra_Sevd").get(is+1,il,0).fill(ip,ad);
                   if(il<4) ecPix[0].strips.putpixels(il,ip,ad,sed7);
                   if(detID==1&&il>3) ecPix[1].strips.putpixels(il-3,ip,ad,sed8);
               }
           }
           map7.put(5,sed7); map8.put(5,sed8);
           ecPix[0].strips.hmap1.get("H1_Pixa_Sevd").get(is+1,1,0).fromTreeMap(map7);
           ecPix[0].strips.hmap1.get("H1_Pixa_Sevd").get(is+1,2,0).fromTreeMap(map8);
       }                   
   }
        
   public void processPixels() {

       boolean good_ua, good_va, good_wa, good_uvwa;
       boolean good_ut, good_vt, good_wt, good_uvwt;
       boolean good_dalitz, good_pixel;
       boolean good_uvwt_save=false;
       int iic,l1,l2,icmax=2,icoff=0,pixel;
       TreeMap<Integer, Object> map= (TreeMap<Integer, Object>) Lmap_a.get(0,0,1); //PCAL
       double pixelLength[] = (double[]) map.get(1);

       if (mondet=="EC")   {icmax=3; icoff=0;}
       if (mondet=="PCAL") {icmax=2; icoff=1;}

       for (int is=0 ; is<6 ; is++) {      
           for (int ic=1; ic<icmax ; ic++) {  
               iic=ic*3; l1=iic-2; l2=iic+1;

               good_ua = nha[is][iic+0]==1;
               good_va = nha[is][iic+1]==1;
               good_wa = nha[is][iic+2]==1;
               good_ut = nht[is][iic+0]==1;
               good_vt = nht[is][iic+1]==1;
               good_wt = nht[is][iic+2]==1;

               good_uvwa = good_ua && good_va && good_wa; //Multiplicity test (NU=NV=NW=1)
               good_uvwt = good_ut && good_vt && good_wt; //Multiplicity test (NU=NV=NW=1)                 

               //              good_dalitz = uvwa[is][ic]-2.0)>0.02 && (uvwa[is][ic]-2.0)<0.056 //EC               
               good_dalitz = Math.abs(uvwa[is][ic-icoff]-2.0)<0.1; //PCAL
               pixel = ecPix[0].pixels.getPixelNumber(strra[is][iic+0][0],strra[is][iic+1][0],strra[is][iic+2][0]);
               good_pixel = pixel!=0;

               if (good_uvwa && good_dalitz && good_pixel) { 

                   ecPix[0].pixels.hmap1.get("H1_PCa_Maps").get(is+1,ic+6,0).fill(pixel,1.0);
                   ecPix[0].pixels.hmap1.get("H1_PCa_Maps").get(is+1,ic+6,3).fill(pixel,1.0/ecPix[0].pixels.getNormalizedArea(pixel)); //Normalized to pixel area

                   for (int il=l1; il<l2 ; il++){
                       double adcc = adcr[is][il+2][0]/pixelLength[pixel-1];

                       ecPix[0].strips.hmap2.get("H2_PCa_Hist").get(is+1,il,1).fill(adcc,strra[is][il+2][0],1.0) ;
                       ecPix[0].strips.hmap2.get("H2_PCa_Hist").get(is+1,il,2).fill(adcc,pixel,1.0);                        
                       ecPix[0].pixels.hmap1.get("H1_PCa_Maps").get(is+1,ic+6,1).fill(pixel,adcc);
                       ecPix[0].pixels.hmap1.get("H1_PCa_Maps").get(is+1,il,0).fill(pixel,adcc);
                       ecPix[0].pixels.hmap1.get("H1_PCa_Maps").get(is+1,il,2).fill(pixel,Math.pow(adcc,2));

                       if (good_uvwt) {
                           if(l1==1) good_uvwt_save = good_uvwt;
                           if(l1==4 && good_uvwt_save){
                               double dtiff1 =  tdcr[is][il-1][0] -  tdcr[is][il+2][0];
                               double dtiff2 = ftdcr[is][il-1][0] - ftdcr[is][il+2][0];
                               ecPix[0].strips.hmap2.get("H2_Tdif_Hist").get(is+1,il-3,0).fill(dtiff1, strrt[is][il+2][0]);
                               ecPix[0].strips.hmap2.get("H2_Tdif_Hist").get(is+1,il-3,1).fill(dtiff2, strrt[is][il+2][0]);
                               ecPix[0].strips.hmap2.get("H2_Tdif_Hist").get(is+1,il  ,0).fill(dtiff1, strrt[is][il+2][0]);
                               ecPix[0].strips.hmap2.get("H2_Tdif_Hist").get(is+1,il  ,1).fill(dtiff2, strrt[is][il+2][0]);
                           }
                       }
                   }
               }   
               //              good_dalitz = uvwt[is][ic]-2.0)>0.02 && (uvwt[is][ic]-2.0)<0.056 //EC               
               good_dalitz = Math.abs(uvwt[is][ic-icoff]-2.0)<0.1; //PCAL
               pixel  = ecPix[0].pixels.getPixelNumber(strrt[is][iic+0][0],strrt[is][iic+1][0],strrt[is][iic+2][0]);
               good_pixel  = pixel!=0;

               if (good_uvwt && good_dalitz && good_pixel) { 
                   ecPix[0].pixels.hmap1.get("H1_PCt_Maps").get(is+1,ic+6,0).fill(pixel,1.0);
                   ecPix[0].pixels.hmap1.get("H1_PCt_Maps").get(is+1,ic+6,3).fill(pixel,1.0/ecPix[0].pixels.getNormalizedArea(pixel)); //Normalized to pixel area
                   for (int il=l1; il<l2 ; il++){
                       ecPix[0].strips.hmap2.get("H2_PCt_Hist").get(is+1,il,1).fill(tdcr[is][il+2][0],strrt[is][il+2][0],1.0) ;
                       ecPix[0].strips.hmap2.get("H2_PCt_Hist").get(is+1,il,2).fill(tdcr[is][il+2][0],pixel,1.0);                       
                       ecPix[0].pixels.hmap1.get("H1_PCt_Maps").get(is+1,ic+6,1).fill(pixel,tdcr[is][il+2][0]);
                       ecPix[0].pixels.hmap1.get("H1_PCt_Maps").get(is+1,il,0).fill(pixel,tdcr[is][il+2][0]);
                   }
               }   
           }
       }   
   }

   public TreeMap<Integer, Object> toTreeMap(double dat[]) {
       TreeMap<Integer, Object> hcontainer = new TreeMap<Integer, Object>();
       hcontainer.put(1, dat);
       double[] b = Arrays.copyOf(dat, dat.length);
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

   public void makeMaps() {

       H2_PCa_Hist  = ecPix[0].strips.hmap2.get("H2_PCa_Hist");
       H2_PCt_Hist  = ecPix[0].strips.hmap2.get("H2_PCt_Hist");
       H1_Stra_Sevd = ecPix[0].strips.hmap1.get("H1_Stra_Sevd");
       H1_Pixa_Sevd = ecPix[0].strips.hmap1.get("H1_Pixa_Sevd");
       H1_PCa_Maps  = ecPix[0].pixels.hmap1.get("H1_PCa_Maps");
       H1_PCt_Maps  = ecPix[0].pixels.hmap1.get("H1_PCt_Maps");

       // il=1-3 (U,V,W Inner strips) il=4-6 (U,V,W Outer Strips) il=7 (Inner Pixels) il=8 (Outer Pixels)

       for (int is=1;is<7;is++) {
           for (int il=1 ; il<7 ; il++) {
               int ill ; if (il<4) ill=7 ; else ill=8 ;
               H1_PCa_Maps.get(is,il,0).divide(H1_PCa_Maps.get(is,ill,0),H1_PCa_Maps.get(is,il,1)); //Normalize Raw View Energy Sum to Events
               H1_PCt_Maps.get(is,il,0).divide(H1_PCt_Maps.get(is,ill,0),H1_PCt_Maps.get(is,il,1)); //Normalize Raw View Timing Sum to Events
               H1_PCa_Maps.get(is,il,2).divide(H1_PCa_Maps.get(is,ill,0),H1_PCa_Maps.get(is,il,3)); //Normalize Raw ADC^2 Sum to Events
               Lmap_a.add(is,il,0, toTreeMap(H2_PCa_Hist.get(is,il,0).projectionY().getData()));    //Strip View ADC  
               Lmap_a.add(is,il+10,0, toTreeMap(H1_PCa_Maps.get(is,il,1).getData()));               //Pixel View ADC 
               Lmap_t.add(is,il,0, toTreeMap(H2_PCt_Hist.get(is,il,0).projectionY().getData()));    //Strip View TDC  
               Lmap_t.add(is,il,1, toTreeMap(H1_PCt_Maps.get(is,il,1).getData()));                  //Pixel View TDC  
           }           
           for (int il=7; il<9; il++) {    
               H1_PCa_Maps.get(is, il, 1).divide(H1_PCa_Maps.get(is, il, 0),H1_PCa_Maps.get(is, il, 2)); // Normalize Raw Energy Sum to Events
               H1_PCt_Maps.get(is, il, 1).divide(H1_PCt_Maps.get(is, il, 0),H1_PCt_Maps.get(is, il, 2)); // Normalize Raw Timing Sum to Events
           }
           Lmap_a.add(is, 7,0, toTreeMap(H1_PCa_Maps.get(is,7,0).getData())); //Pixel Events Inner  
           Lmap_a.add(is, 8,0, toTreeMap(H1_PCa_Maps.get(is,8,0).getData())); //Pixel Events Outer  
           Lmap_a.add(is, 9,0, toTreeMap(H1_PCa_Maps.get(is,7,2).getData())); //Pixel U+V+W Inner Energy     
           Lmap_a.add(is,10,0, toTreeMap(H1_PCa_Maps.get(is,8,2).getData())); //Pixel U+V+W Outer Energy    
           Lmap_t.add(is, 7,2, toTreeMap(H1_PCt_Maps.get(is,7,2).getData())); //Pixel U+V+W Inner Time  
           Lmap_t.add(is, 8,2, toTreeMap(H1_PCt_Maps.get(is,8,2).getData())); //Pixel U+V+W Outer Time 
           Lmap_a.add(is, 7,1, toTreeMap(H1_PCa_Maps.get(is,7,3).getData())); //Pixel Events Inner Normalized  
           Lmap_a.add(is, 8,1, toTreeMap(H1_PCa_Maps.get(is,8,3).getData())); //Pixel Events Outer Normalized  
           if (app.isSingleEvent()){
               for (int il=1 ; il<7 ; il++) Lmap_a.add(is,il,0,   toTreeMap(H1_Stra_Sevd.get(is,il,0).getData())); 
               for (int il=1 ; il<3 ; il++) Lmap_a.add(is,il+6,0, toTreeMap(H1_Pixa_Sevd.get(is,il,0).getData())); 
           }
       }

   }
        
}
    
    


