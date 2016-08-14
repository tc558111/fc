package org.clas.fcmon.ftof;

import java.util.TreeMap;

import org.clas.fcmon.tools.FCCalibrationData;
import org.clas.fcmon.tools.Strips;
import org.jlab.clas.detector.DetectorCollection;
import org.jlab.clasrec.utils.DatabaseConstantProvider;
import org.root.histogram.H1D;
import org.root.histogram.H2D;

public class FTOFPixels {
	
    public Strips          strips = new Strips();
    DatabaseConstantProvider ccdb = new DatabaseConstantProvider(1,"default");
    
    double ftof_xpix[][][] = new double[4][124][7];
    double ftof_ypix[][][] = new double[4][124][7];
    
    public    int     ftof_nstr[] = {23,62,5};
    
    int        nha[][] = new    int[6][2];
    int        nht[][] = new    int[6][2];
    int    strra[][][] = new    int[6][2][62]; 
    int    strrt[][][] = new    int[6][2][62]; 
    int     adcr[][][] = new    int[6][2][62];      
    double  tdcr[][][] = new double[6][2][62]; 
    
    int id;
	public int nstr;
	public String detName = null;
	
    public FTOFPixels(String det) {
        if (det=="PANEL1A") id=0;
        if (det=="PANEL1B") id=1;
        if (det=="PANEL2")  id=2;
        nstr = ftof_nstr[id];
        detName = det;
        pixdef();
        pixrot();
    }
	
    public void pixdef() {
        
        System.out.println("FTOFPixels.pixdef(): "+this.detName); 
        
        double geom[] = new double[nstr];
        double zoff[] = {50.,50.,420.};
        String table=null;
            
        switch (id) { 
        case 0: table = "/geometry/ftof/panel1a"; break;
        case 1: table = "/geometry/ftof/panel1b"; break;
        case 2: table = "/geometry/ftof/panel2";   
        }
        
        ccdb.loadTable(table+"/paddles");        
        for (int i=0; i<nstr; i++) {
            geom[i] = ccdb.getDouble(table+"/paddles/Length",i);
        }
        
        ccdb.loadTable(table+"/panel");
        double y_inc = ccdb.getDouble(table+"/panel/paddlewidth",0);	
        
        double   k;
        double   x_inc=0;
		       
        for(int i=0 ; i<nstr ; i++){
            x_inc = 0.5*geom[i];
            ftof_xpix[0][nstr+i][6]=-x_inc;
            ftof_xpix[1][nstr+i][6]=0.;
            ftof_xpix[2][nstr+i][6]=0.;
            ftof_xpix[3][nstr+i][6]=-x_inc;
            k = -i*y_inc-zoff[id];	    	   
            ftof_ypix[0][nstr+i][6]=k;
            ftof_ypix[1][nstr+i][6]=k;
            ftof_ypix[2][nstr+i][6]=k-y_inc;
            ftof_ypix[3][nstr+i][6]=k-y_inc;
        }
        for(int i=0 ; i<nstr ; i++){
            x_inc = 0.5*geom[i];
            ftof_xpix[0][i][6]=0.;
            ftof_xpix[1][i][6]=x_inc;
            ftof_xpix[2][i][6]=x_inc;
            ftof_xpix[3][i][6]=0.;
            k = -i*y_inc-zoff[id];	    	   
            ftof_ypix[0][i][6]=k;
            ftof_ypix[1][i][6]=k;
            ftof_ypix[2][i][6]=k-y_inc;
            ftof_ypix[3][i][6]=k-y_inc;
        }
	}
		       
    public void pixrot() {
        
        System.out.println("FTOFPixels.pixrot(): "+this.detName);
		
        double[] theta={270.0,330.0,30.0,90.0,150.0,210.0};

        for(int is=0; is<6; is++) {
            double thet=theta[is]*3.14159/180.;
            for (int ipix=0; ipix<2*nstr; ipix++) {
                for (int k=0;k<4;k++){
                    ftof_xpix[k][ipix][is]= -(ftof_xpix[k][ipix][6]*Math.cos(thet)+ftof_ypix[k][ipix][6]*Math.sin(thet));
                    ftof_ypix[k][ipix][is]=  -ftof_xpix[k][ipix][6]*Math.sin(thet)+ftof_ypix[k][ipix][6]*Math.cos(thet);
                }
            }
        }	    	
    }
    
    public void initHistograms(String hipoFile) {
        
        System.out.println("FTOFPixels.initHistograms(): "+this.detName);  
        
        double amax[]= {4000.,6000.,4000.};
        
        DetectorCollection<H1D> H1_a_Sevd = new DetectorCollection<H1D>();
        DetectorCollection<H1D> H1_t_Sevd = new DetectorCollection<H1D>();
        DetectorCollection<H2D> H2_a_Hist = new DetectorCollection<H2D>();
        DetectorCollection<H2D> H2_t_Hist = new DetectorCollection<H2D>();
        DetectorCollection<H2D> H2_a_Sevd = new DetectorCollection<H2D>();
        
        double nend = nstr+1;  
        
        for (int is=1; is<7 ; is++) {
            H2_a_Hist.add(is, 0, 0, new H2D("a_Hist_GM_",  100,   0., amax[id],nstr, 1., nend));
            H2_t_Hist.add(is, 0, 0, new H2D("t_Hist_TD_",  100, -35.,      35.,nstr, 1., nend));
            for (int il=1 ; il<3 ; il++){
                H2_a_Hist.add(is, il, 0, new H2D("a_Hist_Raw_"+il, 100,   0., 4000.,nstr, 1., nend));
                H2_t_Hist.add(is, il, 0, new H2D("t_Hist_Raw_"+il, 100,1330., 1370.,nstr, 1., nend));
                H2_a_Hist.add(is, il, 3, new H2D("a_Hist_PED_"+il,  40, -20.,  20., nstr, 1., nend)); 
                H2_a_Hist.add(is, il, 5, new H2D("a_Hist_FADC_"+il,100,   0., 100., nstr, 1., nend));
                H1_a_Sevd.add(is, il, 0, new H1D("a_Sed_"+il,                       nstr, 1., nend));
                H2_a_Sevd.add(is, il, 0, new H2D("a_Sed_FADC_"+il, 100,   0., 100., nstr, 1., nend));
                H2_a_Sevd.add(is, il, 1, new H2D("a_Sed_FADC_"+il, 100,   0., 100., nstr, 1., nend));
            }
        }       

        if(hipoFile!=" "){
            FCCalibrationData calib = new FCCalibrationData();
            calib.getFile(hipoFile);
            H2_a_Hist = calib.getCollection("H2_a_Hist");
            H2_t_Hist = calib.getCollection("H2_t_Hist");
        }         
        
        strips.addH1DMap("H1_a_Sevd",  H1_a_Sevd);
        strips.addH1DMap("H1_t_Sevd",  H1_t_Sevd);
        strips.addH2DMap("H2_a_Hist",  H2_a_Hist);
        strips.addH2DMap("H2_t_Hist",  H2_t_Hist);
        strips.addH2DMap("H2_a_Sevd",  H2_a_Sevd);
    } 
    
}
