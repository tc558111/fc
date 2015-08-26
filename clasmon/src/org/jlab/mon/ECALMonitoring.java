package org.jlab.mon;

import org.jlab.evio.clas12.*;
import org.jlab.clasrec.main.*;
import org.jlab.clas12.raw.*;
import org.jlab.io.decode.*;
import org.jlab.clasrec.utils.*;
import org.jlab.clasrec.ui.*;
import org.root.pad.*;
import org.root.histogram.*;
import java.util.ArrayList;
import java.util.TreeMap;

public class ECALMonitoring extends DetectorMonitoring {

    public AbsDetectorTranslationTable ttECAL = new AbsDetectorTranslationTable("ECAL",900);
    public TreeMap<Integer,H2D>      ECAL_ADC = new TreeMap<Integer,H2D>();
    public TreeMap<Integer,H2D>      ECAL_TDC = new TreeMap<Integer,H2D>();
    public EvioRawEventDecoder        decoder = new EvioRawEventDecoder();
    
    public ECALMonitoring(){
        super("ECi","1.0","someone");
    }

    @Override
    public void init() {
    	   ttECAL.readFile("/Users/colesmith/COATJAVA/ECAL.table");
       	   for (int lay=1 ; lay<4 ; lay++) {
    	       ECAL_ADC.put(lay, new H2D("ADC_LAYER_"+lay,36,1.0,37.0,50,0.0,3000.0));
    	       ECAL_TDC.put(lay, new H2D("TDC_LAYER_"+lay,36,1.0,37.0,100,800.0,2500.0));
	       }
    }

    @Override
    public void configure(ServiceConfiguration sc) {
        
    }

    @Override
    public void analyze() {
        
    }

    @Override
    public void processEvent(EvioDataEvent event) {

      if(event.hasBank("EC::dgtz")==true){

    		EvioDataBank bank = (EvioDataBank) event.getBank("EC::dgtz");
    		int nrows = bank.rows();
    		for(int loop = 0; loop < nrows; loop++){
    			int id  = bank.getInt("strip",loop);
    			int adc = bank.getInt("ADC",loop);
			    int tdc = bank.getInt("TDC",loop);
			    int lay = bank.getInt("view",loop);
    			ECAL_ADC.get(lay).fill(id,adc);
			    ECAL_TDC.get(lay).fill(id,tdc/1000);
    		}
    		
      }

	ArrayList<RawDataEntry>  rawEntries       = decoder.getDataEntries(event);
	ArrayList<RawDataTDC>    tdcEntries       = (ArrayList<RawDataTDC>) decoder.getTDCEntries(event);
    ArrayList<RawDataEntry>  rawDecodedECAL   = (ArrayList<RawDataEntry>) decoder.getDecodedData(rawEntries,ttECAL);
    ArrayList<RawDataTDC>    tdcDecodedECAL   = (ArrayList<RawDataTDC>) decoder.getDecodedTDCData(tdcEntries,ttECAL);

//	for(RawDataTDC entry : tdcEntries){
//	  System.out.println(entry);
//	}

        for(RawDataEntry entry : rawDecodedECAL) {
        	short[] rawpulse = entry.getRawPulse();
        	int ped          = entry.getIntegral(4,12)/8;
        	int adc          = entry.getIntegral(25,60)-ped*35;
        	int slot         = entry.getSlot();
        	int chan         = entry.getChannel();
        	int crate        = entry.getCrate();
        	int sector       = entry.getSector();
        	int lay          = entry.getLayer();
        	int  id          = entry.getComponent();
        	ECAL_ADC.get(lay).fill(id,adc);	
        }
        
        for(RawDataTDC entry : tdcDecodedECAL) {
        	int crate        = entry.getCrate();
        	int slot         = entry.getSlot();
        	int chan         = entry.getChannel();
        	int lay          = entry.getLayer();
        	int sector       = entry.getSector();
        	int  id          = entry.getComponent();
        	int  tdc         = entry.getTDC();
        	System.out.println("crate,slot,chan,sector,id,tdc= "+crate+" "+slot+" "+chan+" "+" "+sector+" "+id+" "+tdc);
        	ECAL_TDC.get(lay).fill(id,tdc/1000);  	
        }

        
    }

	public void drawComponent(int sector, int layer, int component, EmbeddedCanvas canvas){
		canvas.divide(2,3);
		int ll=layer+1;
		H1D h1 = ECAL_ADC.get(ll).projectionX();
		H1D h2 = ECAL_TDC.get(ll).projectionX();
		H1D h3 = ECAL_ADC.get(ll).projectionY();
		H1D h4 = ECAL_TDC.get(ll).projectionY();
		H1D h5 = ECAL_ADC.get(ll).sliceX(component);
		H1D h6 = ECAL_TDC.get(ll).sliceX(component);
		h1.setTitle("ADC HIT DISTRIBUTION");
		h2.setTitle("TDC HIT DISTRIBUTION");
		h3.setTitle("ADC DISTRIBUTION");
		h4.setTitle("TDC DISTRIBUTION");
		h5.setTitle("LAYER "+ll+" STRIP " + component);
		h6.setTitle("LAYER "+ll+" STRIP " + component);
		h1.setXTitle("STRIP NUMBER")    ; h2.setXTitle("STRIP NUMBER");
		h3.setXTitle("ADC CHAN. NO.")   ; h4.setXTitle("TDC CHAN. NO.");
		h5.setXTitle("ADC CHAN. NO.")   ; h6.setXTitle("TDC CHAN. NO.");
		canvas.cd(0) ; h1.setFillColor(2) ; canvas.draw(h1);
		canvas.cd(1) ; h2.setFillColor(3) ; canvas.draw(h2);
		canvas.cd(2) ; h3.setFillColor(2) ; canvas.draw(h3);
		canvas.cd(3) ; h4.setFillColor(3) ; canvas.draw(h4);
		canvas.cd(4) ; h5.setFillColor(2) ; canvas.draw(h5);
		canvas.cd(5) ; h6.setFillColor(3) ; canvas.draw(h6);
	}


   public static void main(String[] args){
   	  ECALMonitoring pcal = new ECALMonitoring();
      DetectorViewApp app = new DetectorViewApp(pcal);
   }
}



