package org.jlab.ecmon.utils;

import org.root.group.TBrowser;
import org.root.group.TDirectory;
import org.root.histogram.H2D;

public class TDirTest {
	
	TDirectory         mondirectory = new TDirectory(); 	
	
	String monpath       = System.getenv("COATJAVA");
	String monfile       = "mondirectory";
	String labadc[] 	 = {"monitor/pcal/adc","monitor/ecinner/adc","monitor/ecouter/adc"}; 
	String labtdc[]    	 = {"monitor/pcal/tdc","monitor/ecinner/tdc","monitor/ecouter/tdc"}; 
	String labped[] 	 = {"monitor/pcal/ped","monitor/ecinner/ped","monitor/ecouter/ped"}; 
	String labatt[]    	 = {"monitor/pcal/att","monitor/ecinner/att","monitor/ecouter/att"}; 
	String labpmt[] 	 = {"monitor/pcal/pmt","monitor/ecinner/pmt","monitor/ecouter/pmt"}; 
	
	int hid;
	int tid       		 = 100000;
	int cid       		 = 10000;
	int lid       		 = 100;
	
	public TDirTest (String[] args) {
		
	}
	
	public TDirectory getDir(){
        return this.mondirectory;
        
    }	
	
	public void read() {
		String file=monpath+"/"+monfile+".0.evio";	
		TDirectory newdir = new TDirectory();
		newdir.readFile(file);
		TBrowser browser = new TBrowser(newdir);
	}
    
	public void write() {
		String file=monpath+"/"+monfile;
		System.out.println(this.mondirectory.toString());
		this.mondirectory.write(file);
		System.out.println("Writing out histograms to "+file);
		
	}    
        
	public void initHistograms() {
		
		int    nbn1[] = {68,36,36}; 
		double nbn2[] = {69.0,37.0,37.0}; 
		
		int is        = 2;
		int iss       = (int) (is*1e7);
		
	    TDirectory monADC[] = new TDirectory[3];
	    TDirectory monTDC[] = new TDirectory[3];
	    TDirectory monPED[] = new TDirectory[3];
	    TDirectory monPMT[] = new TDirectory[3];
	    TDirectory monATT[] = new TDirectory[3];
	    
		for (int ic=1 ; ic<3 ; ic++) {  //ic=0,1,2 -> PCAL,ECALinner,ECALouter
			
			monADC[ic] = new TDirectory(labadc[ic]);
			monTDC[ic] = new TDirectory(labtdc[ic]); 
			monPED[ic] = new TDirectory(labped[ic]);
			monPMT[ic] = new TDirectory(labpmt[ic]); 
			monATT[ic] = new TDirectory(labatt[ic]);
    			
    		hid=iss+ic*cid;  
    		
    		for (int il=1 ; il<4 ; il++) {
    			monADC[ic].add(new H2D("A"+(int)(hid+10*tid+il*lid),50,0.0,200.0,nbn1[ic],0.0,nbn2[ic])); 
    			monPED[ic].add(new H2D("PED"+(int)(hid+10*tid+il*lid),20,-10.,10.0,nbn1[ic],0.0,nbn2[ic])); 
    			monTDC[ic].add(new H2D("T"+(int)(hid+10*tid+il*lid),60,-15.0,15.0,nbn1[ic],0.0,nbn2[ic]));     		 
    			monTDC[ic].add(new H2D("T"+(int)(hid+11*tid+il*lid),60,-15.0,15.0,nbn1[ic],0.0,nbn2[ic]));     		 
     		}    	
    		getDir().addDirectory(monADC[ic]);
    		getDir().addDirectory(monPED[ic]);
     		getDir().addDirectory(monTDC[ic]);
     		getDir().addDirectory(monPMT[ic]);
    		getDir().addDirectory(monATT[ic]);
    		
		}
	}
	
	public static void main(String[] args){		
		TDirTest monitor = new TDirTest(args);    		
	    monitor.initHistograms();
	     
	    if (args.length==0) monitor.write();
	    if (args.length>0&&args[0].equals("Write")) monitor.write();
	    if (args.length>0&&args[0].equals("Read")) monitor.read();
	    
	}
}
