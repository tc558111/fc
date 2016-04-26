package org.clas.fcmon.misc;

import java.util.List;

import org.jlab.clas.detector.BankType;
import org.jlab.clas.detector.DetectorBankEntry;
import org.jlab.clas.detector.DetectorType;
import org.jlab.clas.tools.utils.DataUtils;
import org.jlab.clas12.detector.DetectorCounter;
import org.jlab.clas12.detector.EventDecoder;
import org.jlab.clas12.detector.FADCConfig;
import org.jlab.clas12.detector.FADCConfigLoader;
import org.jlab.clas12.detector.RawEventDecoder;
import org.jlab.evio.clas12.EvioDataEvent;
import org.jlab.evio.clas12.EvioSource;
import org.root.histogram.H1D;
import org.root.histogram.H2D;
import org.root.pad.TGCanvas;

public class FTOFReader {
		
	FADCConfigLoader          fadcLoader  = new FADCConfigLoader();
	H2D hADCPADDLE,hTDCPADDLE;
	
	FTOFReader(){				
		}
		
		public void plot(int panel) {
	      String plt[] = {"A","B"};
	      int nsab[] = {9,19};
		  int icounter = 0;
          String input = "/Users/colesmith/coatjava/dat/sector2_000257_mode7.evio";
	      EvioSource  reader = new EvioSource();
	      reader.open(input);
	      EventDecoder decoder = new EventDecoder();
	      H1D hADC = new H1D("hADC",60,0.0,5000.0);
	      H1D hTDC = new H1D("hTDC",100,-2000.0,2000.0);
	      if (panel==1) {
	        hADCPADDLE = new H2D("hADCPADDLE",60,    0.0,5000.0,62,0.5,62.5);
	        hTDCPADDLE = new H2D("hTDCPADDLE",40,-1000.0,1000.0,62,0.5,62.5);
	      }else{
	        hADCPADDLE = new H2D("hADCPADDLE",60,    0.0,3000.0,23,0.5,23.5);
	        hTDCPADDLE = new H2D("hTDCPADDLE",40,-1000.0,1000.0,23,0.5,23.5);
	      }
	      hADC.setLineWidth(2);
	      hADC.setFillColor(3);
	      hTDC.setLineWidth(2);
	      hTDC.setFillColor(6);

	      hADC.setXTitle("SQRT(ADCL*ADCR)");
	      hTDC.setXTitle("TDCL-TDCR");
	      hADCPADDLE.setYTitle("FTOF1"+plt[panel]+" PADDLE #");
	      hTDCPADDLE.setYTitle("FTOF1"+plt[panel]+" PADDLE #");
	      hADCPADDLE.setXTitle("SQRT(ADCL*ADCR)");
	      hTDCPADDLE.setXTitle("TDCL-TDCR");

	      while(reader.hasEvent()){
	        icounter++;
	        EvioDataEvent event = (EvioDataEvent) reader.getNextEvent();	        
	        decoder.decode(event);
	        
	        List<DetectorBankEntry> strips;
	        
            if (panel==1){	
              strips = decoder.getDataEntries("FTOF1B");
            }else{
              strips = decoder.getDataEntries("FTOF1A");
            }	
            
            int adcL=0,adcR=0,tdcL=0,tdcR=0;
            int adc,ped,nsb=0,nsa=0,npk=0,timf=0,timc=0;
            double pedref=0.,mc_t=0.,tdc=0,tdcf=0;
            //System.out.println(" ");
            for(DetectorBankEntry strip : strips) {
                adc=ped=npk=timf=timc=0 ; pedref=0.;
            	//System.out.println(strip);
            	int is  = strip.getDescriptor().getSector();
            	int il  = strip.getDescriptor().getLayer();
            	int ip  = strip.getDescriptor().getComponent();
            	int iord= strip.getDescriptor().getOrder();
            	int icr = strip.getDescriptor().getCrate(); 
            	int isl = strip.getDescriptor().getSlot(); 
            	int ich = strip.getDescriptor().getChannel(); 
            	
            	if(strip.getType()==BankType.TDC) {
            		int[] tdcc = (int[]) strip.getDataObject();
            		tdc = tdcc[0]*24./1000.;
            		//if(is==2) {System.out.println("pmt,order,tdc= "+ip+" "+iord+" "+tdcc[0]);}
            	}
            	if(strip.getType()==BankType.ADCFPGA) {
            		int[] adcc= (int[]) strip.getDataObject();
            		ped = adcc[2];
            		npk = adcc[3];
            		adc = (adcc[1]-ped*nsab[panel]);
            		timf = DataUtils.getInteger(adcc[0],0,5);
            		timc = DataUtils.getInteger(adcc[0],6,14);
            		tdcf = timc*4.+timf*0.0625;
            		FADCConfig config=fadcLoader.getMap().get(icr,isl,ich);
            		//nsa = config.getNSA();
            		//nsb = config.getNSB();
            		//pedref = config.getPedestal();
            		//if(is==2) {System.out.println("pmt,order,adc= "+ip+" "+iord+" "+adc);}            		
            	}            	
                //System.out.println("crate,slot,chan:"+icr+" "+isl+" "+ich);
    			//System.out.println("sector,layer,pmt,order"+is+" "+il+" "+ip+" "+iord);
    			//System.out.println("  nchan,tdc,adc,ped,pedref,nsb,nsa: "+npk+" "+tdc+" "+adc+" "+ped+" "+pedref+" "+nsb+" "+nsa);
            }
            
            	
	        List<DetectorCounter> banks;
            if (panel==1){	
              banks = decoder.getDetectorCounters(DetectorType.FTOF1B);
            }else{
              banks = decoder.getDetectorCounters(DetectorType.FTOF1A);
            }
	        for(DetectorCounter bank : banks){
	          if(bank.getChannels().size()==2){
//	             if(bank.isMultiHit()==false){
	               // isMultihit() method returns false when
	            	 
	               if (bank.getChannels().get(0).getADC().size()==1&&
	                   bank.getChannels().get(1).getADC().size()==1&&
	                   bank.getChannels().get(0).getTDC().size()>0&&
	                   bank.getChannels().get(1).getTDC().size()>0)
	               {
	               // it checks if each channel has one ADC and one TDC.
	               int aadcL = bank.getChannels().get(0).getADC().get(0);
	               int aadcR = bank.getChannels().get(1).getADC().get(0);
	               int ttdcL = bank.getChannels().get(0).getTDC().get(0);
	               int ttdcR = bank.getChannels().get(1).getTDC().get(0);
                   hADC.fill(Math.sqrt(aadcL*aadcR));
                   hTDC.fill(ttdcL-ttdcR);	     
	               int paddle = bank.getDescriptor().getComponent();
	               //System.out.println("paddle,adcL,R,tdcL,R= "+paddle+" "+aadcL+" "+aadcR+" "+ttdcL+" "+ttdcR);
                   hADCPADDLE.fill(Math.sqrt(aadcL*aadcR),paddle);
	               hTDCPADDLE.fill(ttdcL-ttdcR,paddle);
	               }
//	             }
	          }
	        }
	        
	        
	      }
	      TGCanvas c1 = new TGCanvas("c1","FTOF",1200,800,2,2);
	      c1.cd(0);
	      c1.draw(hADC);
	      c1.cd(1);
	      c1.draw(hTDC);
	      c1.cd(2);
	      c1.setLogZ();
	      c1.draw(hADCPADDLE);
	      c1.cd(3);
	      c1.setLogZ();
	      c1.draw(hTDCPADDLE);
		}
	
	public static void main(String[] args) {		
		FTOFReader tof = new FTOFReader();
		tof.plot(0);
		tof.plot(1);
	}
	
}
