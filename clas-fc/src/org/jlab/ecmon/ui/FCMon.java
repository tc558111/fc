package org.jlab.ecmon.ui;

import javax.swing.SwingUtilities;

import org.jlab.clasrec.main.DetectorMonitoring;
import org.jlab.clasrec.rec.CLASMonitoring;
import org.jlab.clasrec.utils.ServiceConfiguration;
import org.root.histogram.*;
import org.jlab.ecmon.utils.MonitorApp;
import org.jlab.evio.clas12.EvioDataBank;
import org.jlab.evio.clas12.EvioDataEvent;
import org.root.group.TBrowser;
import org.root.group.TDirectory;

public class FCMon extends DetectorMonitoring {
	
	public String laba[] = {"monitor/pcal/adc","monitor/ecinner/adc","monitor/ecouter/adc"}; 
	public String labt[] = {"monitor/pcal/tdc","monitor/ecinner/tdc","monitor/ecouter/tdc"}; 
	 
	String monpath       = System.getenv("COATJAVA");
	String monfile       = "fcmon";	
	
		public FCMon(){
			super("FCMON","1.0","lcsmith");
		}

	@Override
	public void analyze() {
 
	}

	@Override
	public void configure(ServiceConfiguration arg0) {
		
	}
	
	public void close() {
		String file=monpath+"/"+monfile;
		getDir().write(file);
		System.out.println("Writing out histograms to "+file);
		
	} 
	@Override
	public void init() {
		
		//Histogram ID convention
		//hid=is*1e7 + tag*1e5 + ic*1e4 + uv*1e2 + pmt
		//is=1-6 tag=0-99 ic=0-2 uv=12,13,23 pmt=1-68
		
		int    nbn1[] = {68,36,36}; 
		double nbn2[] = {69.0,37.0,37.0}; 
		
		int hid;
		int tid       = 100000;
		int cid       = 10000;
		int lid       = 100;
		int is        = 2;
		int iss       = (int) (is*1e7);

	    TDirectory calADC[] = new TDirectory[3];
	    TDirectory calTDC[] = new TDirectory[3];
	    
    	for (int ic=0 ; ic<3 ; ic++) {  //ic=0,1,2 -> PCAL,ECALinner,ECALouter
    		calADC[ic] = new TDirectory(laba[ic]);
    		calTDC[ic] = new TDirectory(labt[ic]);
 
    		hid=iss+11*tid+ic*cid; //Dalitz test
    		calADC[ic].add(new H1D("A"+hid,50,0.,3.0));
    		
    		hid=iss+50*tid+ic*cid; //Light attenuation vs crossing strip
    		for (int ip=1 ; ip<nbn1[ic]+1 ; ip++) {    		 
    			calADC[ic].add(new H2D("A"+(int)(hid+21*lid+ip),nbn1[ic],1.0,nbn2[ic],30,0.0,200.0));
    			calADC[ic].add(new H2D("A"+(int)(hid+12*lid+ip),nbn1[ic],1.0,nbn2[ic],30,0.0,200.0));     
    			calADC[ic].add(new H2D("A"+(int)(hid+31*lid+ip),nbn1[ic],1.0,nbn2[ic],30,0.0,200.0));    	 
    			calADC[ic].add(new H2D("A"+(int)(hid+13*lid+ip),nbn1[ic],1.0,nbn2[ic],30,0.0,200.0));    	 
    			calADC[ic].add(new H2D("A"+(int)(hid+32*lid+ip),nbn1[ic],1.0,nbn2[ic],30,0.0,200.0));    		 
    			calADC[ic].add(new H2D("A"+(int)(hid+23*lid+ip),nbn1[ic],1.0,nbn2[ic],30,0.0,200.0));	
    			}
    		hid=iss+60*tid+ic*cid; //Time Difference vs crossing strip
    		for (int ip=1 ; ip<nbn1[ic]+1 ; ip++) {    		 
    			calTDC[ic].add(new H2D("T"+(int)(hid+21*lid+ip),nbn1[ic],1.0,nbn2[ic],80,-40.0,40.0));
    			calTDC[ic].add(new H2D("T"+(int)(hid+12*lid+ip),nbn1[ic],1.0,nbn2[ic],80,-40.0,40.0));     
    			calTDC[ic].add(new H2D("T"+(int)(hid+31*lid+ip),nbn1[ic],1.0,nbn2[ic],80,-40.0,40.0));    	 
    			calTDC[ic].add(new H2D("T"+(int)(hid+13*lid+ip),nbn1[ic],1.0,nbn2[ic],80,-40.0,40.0));    	 
    			calTDC[ic].add(new H2D("T"+(int)(hid+32*lid+ip),nbn1[ic],1.0,nbn2[ic],80,-40.0,40.0));    		 
    			calTDC[ic].add(new H2D("T"+(int)(hid+23*lid+ip),nbn1[ic],1.0,nbn2[ic],80,-40.0,40.0));	
    			}
    		hid=iss+40*tid+ic*cid; //Detector map
    		for (int il=1 ; il<4 ; il++) {    	 
    			calADC[ic].add(new H2D("A"+(int)(hid+12*lid+il),nbn1[ic],1.0,nbn2[ic],nbn1[ic],1.0,nbn2[ic]));     
    			calADC[ic].add(new H2D("A"+(int)(hid+13*lid+il),nbn1[ic],1.0,nbn2[ic],nbn1[ic],1.0,nbn2[ic]));    	 
    			calADC[ic].add(new H2D("A"+(int)(hid+23*lid+il),nbn1[ic],1.0,nbn2[ic],nbn1[ic],1.0,nbn2[ic]));    		 
    			calADC[ic].add(new H2D("A"+(int)(hid+32*lid+il),nbn1[ic],1.0,nbn2[ic],nbn1[ic],1.0,nbn2[ic]));
    			}
    		hid=iss+ic*cid; //FADC MIP vs strip 
    		for (int il=1 ; il<4 ; il++) {
    			calADC[ic].add(new H2D("A"+(int)(hid+10*tid+il*lid),50,0.0,200.0,nbn1[ic],1.0,nbn2[ic])); 
    			calTDC[ic].add(new H2D("T"+(int)(hid+10*tid+il*lid),70,1330.0,1420.0,nbn1[ic],1.0,nbn2[ic]));     		 
    			calADC[ic].add(new H2D("A"+(int)(hid+15*tid+il*lid),50,0.0,200.0,nbn1[ic],1.0,nbn2[ic]));     		 
    			calADC[ic].add(new H2D("A"+(int)(hid+20*tid+il*lid),50,0.0,200.0,nbn1[ic],1.0,nbn2[ic]));     		 
    			calADC[ic].add(new H2D("A"+(int)(hid+21*tid+il*lid),50,0.0,200.0,nbn1[ic],1.0,nbn2[ic]));     		 
    			calADC[ic].add(new H2D("A"+(int)(hid+22*tid+il*lid),50,0.0,200.0,nbn1[ic],1.0,nbn2[ic])); 
    			}
    		getDir().addDirectory(calADC[ic]);
    		getDir().addDirectory(calTDC[ic]);
    		}
	}
	
	public float uvw_dalitz(int ic, int ip, int il) {
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
	@Override
	public void processEvent(EvioDataEvent event) {
		
		int nh[][]         = new int[6][9];
		int strr[][][]     = new int[6][9][68]; 
		int adcr[][][]     = new int[6][9][68];
		float tdcr[][][]   = new float[6][9][68];
		int rs[]           = new int[9];
		int ad[]           = new int[9];
		float td[]         = new float[9];
		boolean good_lay[] = new boolean[9]; 
		boolean good_uv[]  = new boolean[3];
		boolean good_uw[]  = new boolean[3];
		boolean good_vw[]  = new boolean[3];
		boolean good_uvw[] = new boolean[3];		
		boolean good_uwt[] = new boolean[3];
		boolean good_vwt[] = new boolean[3];
		boolean good_wut[] = new boolean[3];
		boolean good_uwtt[]= new boolean[3];
		boolean good_vwtt[]= new boolean[3];
		boolean good_wutt[]= new boolean[3];
		int rscutuw[]      = {60,35,35};
		int rscutvw[]      = {67,35,35};
		int rscutwu[]      = {67,35,35};
		int rsw[]          = {0,1,1};
		int adcutuw[]      = {70,5,5};
		int adcutvw[]      = {70,5,5};
		int adcutwu[]      = {70,5,5};
		
		int tid            = 100000;
		int cid            = 10000;
		int lid            = 100;		

		int thr            = 15;
		int iis            = 2;	//Sector 5 hardwired for now
		
		int hid,hidd;
		
		H2D hadc,htdc;
		H1D hpix;
		
		for (int is=0 ; is<6 ; is++) {
			for (int il=0 ; il<9 ; il++) {
				nh[is][il] = 0;
				for (int ip=0 ; ip<68 ; ip++) {
					strr[is][il][ip] = 0;
					adcr[is][il][ip] = 0;
					tdcr[is][il][ip] = 0;
				}
			}
		}
		double mc_t=0.0;
		if(event.hasBank("PCAL::true")==true){
			EvioDataBank bank = (EvioDataBank) event.getBank("PCAL::true");
			int nrows = bank.rows();
			for(int i=0; i < nrows; i++){
				mc_t = bank.getDouble("avgT",i);
			}	
		}
		if(event.hasBank("PCAL::dgtz")==true){
			int ic=0;	// ic=0,1,2 -> PCAL,ECinner,ECouter
			float uvw=0;
			float tdcmax=100000;
            EvioDataBank bank = (EvioDataBank) event.getBank("PCAL::dgtz");
            
            for(int i = 0; i < bank.rows(); i++){
            	float tdc = (float)bank.getInt("TDC",i)-(float)(mc_t)*1000;
            	if (tdc<tdcmax) tdcmax=tdc;
            }
            
            for(int i = 0; i < bank.rows(); i++){
            	int is  = bank.getInt("sector",i);
            	int ip  = bank.getInt("strip",i);
            	int il  = bank.getInt("view",i);
            	int adc = bank.getInt("ADC",i);
            	float tdc =(float)(bank.getInt("TDC",i));
            	tdc=((tdc-(float)mc_t*1000)-tdcmax+1340000)/1000;
            	
                if(is==iis){
            	   if (adc>thr) {
            	     nh[is-1][il-1]++;
            	     int inh = nh[is-1][il-1];
            	     adcr[is-1][il-1][inh-1] = adc;
            	     tdcr[is-1][il-1][inh-1] = tdc;
            	     strr[is-1][il-1][inh-1] = ip;
            	     uvw=uvw+uvw_dalitz(ic,ip,il);
            	   }
            	   
               	   hid = (int) (1e7*is+10*tid+ic*cid+il*lid);
            	   hadc = (H2D) getDir().getDirectory(laba[ic]).getObject("A"+hid);
            	   htdc = (H2D) getDir().getDirectory(labt[ic]).getObject("T"+hid);
                   hadc.fill(adc,ip);
                   htdc.fill(tdc,ip);
            	   hid = (int) (1e7*is+11*tid+ic*cid);
            	   hpix = (H1D) getDir().getDirectory(laba[ic]).getObject("A"+hid);
                   hpix.fill(uvw);
               }
            }
         }		
		
		mc_t=0.0;
		if(event.hasBank("EC::true")==true){
			EvioDataBank bank = (EvioDataBank) event.getBank("EC::true");
			int nrows = bank.rows();
			for(int i=0; i < nrows; i++){
				mc_t = bank.getDouble("avgT",i);
			}	
		}
		
		if(event.hasBank("EC::dgtz")==true){
        	float uvw=0;
            float tdcmax=100000;
            EvioDataBank bank = (EvioDataBank) event.getBank("EC::dgtz");
            
            for(int i = 0; i < bank.rows(); i++){
            	float tdc = (float)bank.getInt("TDC",i)-(float)mc_t*1000;
            	if (tdc<tdcmax) tdcmax=tdc;
            }
            
            for(int i = 0; i < bank.rows(); i++){
            	int  is = bank.getInt("sector",i);
            	int  ip = bank.getInt("strip",i);
             	int  ic = bank.getInt("stack",i);	 
            	int  il = bank.getInt("view",i);
            	int adc = bank.getInt("ADC",i);
            	int tdc = bank.getInt("TDC",i);
            	
            	float tdcc=(((float)tdc-(float)mc_t*1000)-tdcmax+1340000)/1000;
            	
            	int  iv = ic*3+il;
                
                if(is==iis){
            	   if (adc>thr) {
            	     nh[is-1][iv-1]++;
            	     int inh = nh[is-1][iv-1];
            	     adcr[is-1][iv-1][inh-1] = adc;
            	     tdcr[is-1][iv-1][inh-1] = tdcc;
            	     strr[is-1][iv-1][inh-1] = ip;
            	     uvw=uvw+uvw_dalitz(ic,ip,il);
            	   }
            	   
                   hid = (int) (1e7*is+10*tid+ic*cid+il*lid);
            	   hadc = (H2D) getDir().getDirectory(laba[ic]).getObject("A"+hid);
            	   htdc = (H2D) getDir().getDirectory(labt[ic]).getObject("T"+hid);
                   hadc.fill(adc,ip);
                   htdc.fill(tdcc,ip);
            	   hid = (int) (1e7*is+11*tid+ic*cid);
            	   hpix = (H1D) getDir().getDirectory(laba[ic]).getObject("A"+hid);
                   hpix.fill(uvw);
               }
            }
         }
        
        // Logic: Limit multiplicity to 1 hit per view
        
        for (int il=0 ; il<9 ; il++){
        	good_lay[il]=nh[iis-1][il]==1;
        	if (good_lay[il]) {
        		rs[il]=strr[iis-1][il][0];
        		ad[il]=adcr[iis-1][il][0];
        		td[il]=tdcr[iis-1][il][0];
        	}
        }
        
        // Logic: Good two-view and three-view multiplicity (m2,m3 cut)
        
        for (int ic=0 ; ic<3; ic++){
        	good_uv[ic]   = good_lay[0+ic*3]&good_lay[1+ic*3];
        	good_uw[ic]   = good_lay[0+ic*3]&good_lay[2+ic*3];
        	good_vw[ic]   = good_lay[1+ic*3]&good_lay[2+ic*3];
        	good_uvw[ic]  =      good_uv[ic]&good_lay[2+ic*3];
        	
        	good_uwt[ic]  =  good_uw[ic]&rs[2+ic*3]==rscutuw[ic];
        	good_vwt[ic]  =  good_uv[ic]&rs[0+ic*3]==rscutvw[ic];
        	good_wut[ic]  =  good_uw[ic]&rs[rsw[ic]+ic*3]==rscutwu[ic];
        	good_uwtt[ic] = good_uwt[ic]&ad[2+ic*3]>adcutuw[ic];
        	good_vwtt[ic] = good_vwt[ic]&ad[0+ic*3]>adcutvw[ic];
        	good_wutt[ic] = good_wut[ic]&ad[rsw[ic]+ic*3]>adcutwu[ic];        	
        }  
        
        // Histo: Check plots using trigger condition (here u.v coincidence) (TAG=15)
        
        hid  = (int) (1e7*iis);
        
        for (int ic=0 ; ic<3 ; ic++){
        	if (good_uv[ic]) {
        		for (int il=1 ; il<4 ; il++) {
        			hidd = hid+15*tid+ic*cid+il*lid;
        			hadc = (H2D) getDir().getDirectory(laba[ic]).getObject("A"+hidd);
        			int ill=ic*3+il-1;
        			hadc.fill(ad[ill],rs[ill]);
        		}
        	}
        }
        
        // Histo: MIP plots using m2 and s cuts (TAG=20)
 
        H2D hadca[] = new H2D[3];
        
        for (int ic=0 ; ic<3 ; ic++){ 	
        	for (int il=1 ; il<4 ; il++) {
        		hidd = hid+20*tid+ic*cid+il*lid;   
        		hadca[il-1] = (H2D) getDir().getDirectory(laba[ic]).getObject("A"+hidd);
        	}
        	if(good_uwt[ic]) hadca[0].fill(ad[ic*3+0],rs[ic*3+0]);
            if(good_vwt[ic]) hadca[1].fill(ad[ic*3+1],rs[ic*3+1]);
            if(good_wut[ic]) hadca[2].fill(ad[ic*3+2],rs[ic*3+2]);
        } 
        
        // Histo: MIP plots using Dalitz m3 cut (TAG=21) and s cut (TAG=22)
        
        for (int ic=0 ; ic<3 ; ic++) {
        	if (good_uvw[ic]) {
            	for (int il=1 ; il<4 ; il++) {
            		hidd = hid+21*tid+ic*cid+il*lid;   
            		hadca[il-1] = (H2D) getDir().getDirectory(laba[ic]).getObject("A"+hidd);
            	}
                if(good_uwt[ic]) hadca[0].fill(ad[ic*3+0],rs[ic*3+0]);
                if(good_vwt[ic]) hadca[1].fill(ad[ic*3+1],rs[ic*3+1]);
                if(good_wut[ic]) hadca[2].fill(ad[ic*3+2],rs[ic*3+2]);
            	for (int il=1 ; il<4 ; il++) {
            		hidd = hid+22*tid+ic*cid+il*lid;   
            		hadca[il-1] = (H2D) getDir().getDirectory(laba[ic]).getObject("A"+hidd);
            	}
                if(good_uwtt[ic]) hadca[0].fill(ad[ic*3+0],rs[ic*3+0]);
                if(good_vwtt[ic]) hadca[1].fill(ad[ic*3+1],rs[ic*3+1]);
                if(good_wutt[ic]) hadca[2].fill(ad[ic*3+2],rs[ic*3+2]);
                
        // Histo: U vs V, U vs W, V vs W (used for detector map)
                
                double rs1=rs[ic*3+0] ; double rs2=rs[ic*3+1] ; double rs3=rs[ic*3+2];
                double ad1=ad[ic*3+0] ; double ad2=ad[ic*3+1] ; double ad3=ad[ic*3+2];
                double td1=td[ic*3+0] ; double td2=td[ic*3+1] ; double td3=td[ic*3+2];
                
                hidd = hid+40*tid+ic*cid;
                
        		H2D hadc121 = (H2D) getDir().getDirectory(laba[ic]).getObject("A"+(int)(hidd+12*lid+1));
        		H2D hadc122 = (H2D) getDir().getDirectory(laba[ic]).getObject("A"+(int)(hidd+12*lid+2));
        		H2D hadc131 = (H2D) getDir().getDirectory(laba[ic]).getObject("A"+(int)(hidd+13*lid+1));
        		H2D hadc132 = (H2D) getDir().getDirectory(laba[ic]).getObject("A"+(int)(hidd+13*lid+2));
        		H2D hadc133 = (H2D) getDir().getDirectory(laba[ic]).getObject("A"+(int)(hidd+13*lid+3));
        		H2D hadc231 = (H2D) getDir().getDirectory(laba[ic]).getObject("A"+(int)(hidd+23*lid+1));
        		H2D hadc232 = (H2D) getDir().getDirectory(laba[ic]).getObject("A"+(int)(hidd+23*lid+2));
        		H2D hadc321 = (H2D) getDir().getDirectory(laba[ic]).getObject("A"+(int)(hidd+32*lid+1));
        		H2D hadc322 = (H2D) getDir().getDirectory(laba[ic]).getObject("A"+(int)(hidd+32*lid+2));
                      
                                
                for (int i=0 ; i<nh[iis-1][ic*3+0] ; i++) {
                	double ris1=strr[iis-1][ic*3+0][i];
                	for (int k=0 ; k<nh[iis-1][ic*3+1] ; k++) {
                		double ris2=strr[iis-1][ic*3+1][k];
                		hadc121.fill(ris1,ris2,1.0);
                		hadc122.fill(ris1,ris2,ad1);
                	}
                	for (int k=0 ; k<nh[iis-1][ic*3+2] ; k++) {
                		double ris3=strr[iis-1][ic*3+2][k];
                		hadc131.fill(ris1,ris3,1.0);
                		hadc132.fill(ris1,ris3,ad1);
                		hadc133.fill(ris1,ris3,td3-td1);
                	}
                }
                
                for (int i=0 ; i<nh[iis-1][ic*3+1] ; i++) {
                	double ris2=strr[iis-1][ic*3+1][i];
                	for (int k=0 ; k<nh[iis-1][ic*3+2] ; k++) {
                		double ris3=strr[iis-1][ic*3+2][k];
                		hadc231.fill(ris2,ris3,1.0);
                		hadc232.fill(ris2,ris3,ad2);
                		hadc321.fill(ris3,ris2,1.0);
                		hadc322.fill(ris3,ris2,ad3);                		
                	}
                }
                
                
        // Histo: Attenuation lengths from ADC vs strip (TAG=50)
                
                hidd=hid+50*tid+ic*cid;
                
        		H2D hadc21 = (H2D) getDir().getDirectory(laba[ic]).getObject("A"+(int)(hidd+21*lid+rs1));
        		H2D hadc12 = (H2D) getDir().getDirectory(laba[ic]).getObject("A"+(int)(hidd+12*lid+rs2));
        		H2D hadc31 = (H2D) getDir().getDirectory(laba[ic]).getObject("A"+(int)(hidd+31*lid+rs1));
        		H2D hadc13 = (H2D) getDir().getDirectory(laba[ic]).getObject("A"+(int)(hidd+13*lid+rs3));
        		H2D hadc32 = (H2D) getDir().getDirectory(laba[ic]).getObject("A"+(int)(hidd+32*lid+rs2));
        		H2D hadc23 = (H2D) getDir().getDirectory(laba[ic]).getObject("A"+(int)(hidd+23*lid+rs3));
               
                if (good_uv[ic]) {
                	if(ad2>10) hadc21.fill(rs2,ad1); // U MIP VS V STRIP
                	if(ad1>10) hadc12.fill(rs1,ad2); // V MIP VS U STRIP
                }
                if (good_uw[ic]) {
                	if(ad3>10) hadc31.fill(rs3,ad1); // U MIP VS W STRIP
                	if(ad1>10) hadc13.fill(rs1,ad3); // W MIP VS U STRIP
                }
                if (good_vw[ic]) {
                	if(ad3>10) hadc32.fill(rs3,ad2); // V MIP VS W STRIP
                	if(ad2>10) hadc23.fill(rs2,ad3); // W MIP VS V STRIP
                }
                
        // Histo: Attenuation lengths from ADC vs strip (TAG=60)
                
                hidd=hid+60*tid+ic*cid;
                
        		H2D htdc21 = (H2D) getDir().getDirectory(labt[ic]).getObject("T"+(int)(hidd+21*lid+rs1));
        		H2D htdc12 = (H2D) getDir().getDirectory(labt[ic]).getObject("T"+(int)(hidd+12*lid+rs2));
        		H2D htdc31 = (H2D) getDir().getDirectory(labt[ic]).getObject("T"+(int)(hidd+31*lid+rs1));
        		H2D htdc13 = (H2D) getDir().getDirectory(labt[ic]).getObject("T"+(int)(hidd+13*lid+rs3));
        		H2D htdc32 = (H2D) getDir().getDirectory(labt[ic]).getObject("T"+(int)(hidd+32*lid+rs2));
        		H2D htdc23 = (H2D) getDir().getDirectory(labt[ic]).getObject("T"+(int)(hidd+23*lid+rs3));
               
                if (good_uv[ic]) {
                	if(ad2>5) htdc21.fill(rs2,td1-td2); // U MIP VS V STRIP
                	if(ad1>5) htdc12.fill(rs1,td2-td1); // V MIP VS U STRIP
                }
                if (good_uw[ic]) {
                	if(ad3>5) htdc31.fill(rs3,td1-td3); // U MIP VS W STRIP
                	if(ad1>5) htdc13.fill(rs1,td3-td1); // W MIP VS U STRIP
                }
                if (good_vw[ic]) {
                	if(ad3>5) htdc32.fill(rs3,td2-td3); // V MIP VS W STRIP
                	if(ad2>5) htdc23.fill(rs2,td3-td2); // W MIP VS V STRIP
                }                
                
      // Histo: Time Walk (Delta t vs ADC) (TAG=61)
                
                
        	}
        }
    }
	public static void main(String[] args){
		   FCMon calib = new FCMon();		   
		    SwingUtilities.invokeLater(new Runnable() {
		    	public void run() {
		 		   calib.init();
				   CLASMonitoring monitor = new CLASMonitoring("/Users/colesmith/COATJAVA/dat/fc-muon-500k-s2-noatt.evio", calib);
				   monitor.process();
				   calib.analyze();
				   calib.close();
				   TBrowser browser = new TBrowser(calib.getDir()); 
		    	}
		    });		   
		}
}
