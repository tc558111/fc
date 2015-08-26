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

public class PCALMonitoring extends DetectorMonitoring {

    public AbsDetectorTranslationTable ttPCAL = new AbsDetectorTranslationTable("PCAL",900);
    public TreeMap<Integer,H2D>      PCAL_ADC = new TreeMap<Integer,H2D>();
    public TreeMap<Integer,H2D>      PCAL_TDC = new TreeMap<Integer,H2D>();
    public EvioRawEventDecoder        decoder = new EvioRawEventDecoder();
    
    public PCALMonitoring(){
        super("EC","1.0","someone");
    }

    @Override
    public void init() {
    	   ttPCAL.readFile("/Users/colesmith/COATJAVA/PCAL.table");
       	   for (int lay=1 ; lay<4 ; lay++) {
    	       PCAL_ADC.put(lay, new H2D("ADC_LAYER_"+lay,68,1.0,69.0,50,0.0,300.0));
    	       PCAL_TDC.put(lay, new H2D("TDC_LAYER_"+lay,68,1.0,69.0,200,0.0,1000.0));
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

      if(event.hasBank("PCAL::dgtz")==true){

    		EvioDataBank bank = (EvioDataBank) event.getBank("PCAL::dgtz");
    		int nrows = bank.rows();
    		for(int loop = 0; loop < nrows; loop++){
    			int id  = bank.getInt("strip",loop);
    			int adc = bank.getInt("ADC",loop);
			    int tdc = bank.getInt("TDC",loop);
			    int lay = bank.getInt("view",loop);
    			PCAL_ADC.get(lay).fill(id,adc);
			    PCAL_TDC.get(lay).fill(id,tdc);
    		}
    		
      }

	ArrayList<RawDataEntry>  rawEntries       = decoder.getDataEntries(event);
	ArrayList<RawDataTDC>    tdcEntries       = (ArrayList<RawDataTDC>) decoder.getTDCEntries(event);
    ArrayList<RawDataEntry>  rawDecodedPCAL   = (ArrayList<RawDataEntry>) decoder.getDecodedData(rawEntries,ttPCAL);

//	for(RawDataTDC entry : tdcEntries){
//	  System.out.println(entry);
//	}

        for(RawDataEntry entry : rawDecodedPCAL) {
        	short[] rawpulse = entry.getRawPulse();
        	int ped          = entry.getIntegral(4,12)/8;
        	int adc          = entry.getIntegral(25,60)-ped*35;
        	int slot         = entry.getSlot();
        	int chan         = entry.getChannel();
        	int crate        = entry.getCrate();
        	int sector       = entry.getSector();
        	int lay          = entry.getLayer();
        	int  id          = entry.getComponent();
        	int tdc          = entry.getTDC();
        	PCAL_ADC.get(lay).fill(id,adc*0.2);
        	PCAL_TDC.get(lay).fill(id,tdc*24/1000.0);
        	
        }
        
    }

	public void drawComponent(int sector, int layer, int component, EmbeddedCanvas canvas){
		canvas.divide(2,3);
		int ll=layer+1;
		H1D h1 = PCAL_ADC.get(ll).projectionX();
		H1D h2 = PCAL_TDC.get(ll).projectionX();
		H1D h3 = PCAL_ADC.get(ll).projectionY();
		H1D h4 = PCAL_TDC.get(ll).projectionY();
		H1D h5 = PCAL_ADC.get(ll).sliceX(component);
		H1D h6 = PCAL_TDC.get(ll).sliceX(component);
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
   	  PCALMonitoring pcal = new PCALMonitoring();
      DetectorViewApp app = new DetectorViewApp(pcal);
   }
}



