package org.clas.fcmon.ec;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.clas.fcmon.detector.view.DetectorShape2D;
import org.clas.fcmon.tools.CalibrationData;
import org.clas.fcmon.tools.FCApplication;
import org.jlab.clas.detector.DetectorCollection;
import org.jlab.clas.detector.DetectorType;
import org.jlab.clasrec.utils.DatabaseConstantProvider;
import org.jlab.detector.base.DetectorDescriptor;
import org.jlab.detector.calib.utils.CalibrationConstants;
import org.jlab.detector.calib.utils.CalibrationConstantsListener;
import org.jlab.detector.calib.utils.CalibrationConstantsView;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.groot.math.F1D;
import org.jlab.rec.ecn.ECCommon;

public class ECCalibrationApp extends FCApplication implements CalibrationConstantsListener,ChangeListener {
    
    JPanel                    engineView = new JPanel();
    EmbeddedCanvas             leftCanvas = new EmbeddedCanvas();
    EmbeddedCanvas            rightCanvas = new EmbeddedCanvas();
    CalibrationConstantsView      ccview = new CalibrationConstantsView();
    ArrayList<CalibrationConstants> list = new ArrayList<CalibrationConstants>();

    public ECCalibrationEngine[] engines = {
            new ECAttenEventListener(),
            new ECGainEventListener(),
            new ECStatusEventListener()
    };

    public final int  ATTEN  = 0;
    public final int   GAIN  = 1;
    public final int STATUS  = 2;
    
    String[] names = {"/calibration/ec/atten","/calibration/ec/gain","/calibration/ec/status"};

    String selectedDir = names[ATTEN];
       
    int selectedSector = 1;
    int selectedLayer = 1;
    int selectedPaddle = 1;
    
    public ECCalibrationApp(String name , ECPixels[] ecPix) {
        super(name, ecPix);       
     } 
    
    public void init(int is1, int is2) {
        for (int i=0; i<engines.length; i++) engines[i].init(is1,is2); 
    }   
    
    public JPanel getCalibPane() {        
        engineView.setLayout(new BorderLayout());
        JSplitPane enginePane = new JSplitPane(JSplitPane.VERTICAL_SPLIT); 
        JSplitPane   viewPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT); 
        ccview.getTabbedPane().addChangeListener(this);
        
        for (int i=0; i < engines.length; i++) {
            ccview.addConstants(engines[i].getCalibrationConstants().get(0),this);
        }   

        viewPane.setLeftComponent(leftCanvas);
        viewPane.setRightComponent(rightCanvas);
        viewPane.setResizeWeight(0.5);
        enginePane.setTopComponent(viewPane);
        enginePane.setBottomComponent(ccview);       
        enginePane.setResizeWeight(0.8);
        engineView.add(enginePane);
        return engineView;       
    }  
    
    public ECCalibrationEngine getSelectedEngine() {
        
        ECCalibrationEngine engine = engines[ATTEN];

        if (selectedDir == names[ATTEN]) {
            engine = engines[ATTEN];
        } else if (selectedDir == names[GAIN]) {
            engine = engines[GAIN];
        } else if (selectedDir == names[STATUS]) {
            engine = engines[STATUS];
        } 
        return engine;
    }
    
    public class ECAttenEventListener extends ECCalibrationEngine {
        
        public final int[][] PARAM = {{100,100,100,100,100,100,160,160,160},
                                      {360,360,360,360,360,360,360,360,360},
                                      {50,50,50,50,50,50,50,50,50}};
        public final int[][] DELTA =  {{10,10,10,10,10,10,16,16,16},
                                       {60,60,60,60,60,60,60,60,60},
                                       {50,50,50,50,50,50,50,50,50}};
        
        int is1,is2;
        
        ECAttenEventListener(){};
        
        public void init(int is1, int is2) {
            
            System.out.println("ECCalibrationApp:ECAttenEventListener.init");
            this.is1=is1;
            this.is2=is2;
            
            calib = new CalibrationConstants(3,"A/F:Aerr/F:B/F:Berr/F:C/F:Cerr/F");
            calib.setName("/calibration/ec/atten");
            calib.setPrecision(3);
            int kk=0;
            for (int k=0; k<3; k++) {
            for (int i=0; i<9; i++) {  
                calib.addConstraint(kk+3,PARAM[k][i]-DELTA[k][i], 
                                         PARAM[k][i]+DELTA[k][i], 1, i+1);
                kk=kk+2;
            }
            }
            
            for(int is=is1; is<is2; is++) {                
                for(int idet=0; idet<ecPix.length; idet++) {
                    for (int il=0; il<3 ; il++) {
                        int layer = il+idet*3;
                        for(int ip = 0; ip < ecPix[idet].ec_nstr[il]; ip++) {
                            calib.addEntry(is, layer+1, ip+1);
                            calib.setDoubleValue(0., "A",    is, layer+1, ip+1);
                            calib.setDoubleValue(0., "Aerr", is, layer+1, ip+1);
                            calib.setDoubleValue(0., "B",    is, layer+1, ip+1);
                            calib.setDoubleValue(0., "Berr", is, layer+1, ip+1);
                            calib.setDoubleValue(0., "C",    is, layer+1, ip+1);
                            calib.setDoubleValue(0., "Cerr", is, layer+1, ip+1);
                        }
                    }
                }
            }
            
            list.add(calib);         
        }
        
        @Override
        public void analyze() {
            for (int sector = is1; sector < is2; sector++) {
                for (int layer = 1; layer < 4; layer++) {
                    for (int paddle = 1; paddle<NUM_PADDLES[layer-1]+1; paddle++) {
                        fit(sector, layer, paddle, 0.0, 0.0);
                    }
                }
            }
            calib.fireTableDataChanged();
        }
        
        @Override
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
                  float  cnts[] = ecPix[idet].pixels.hmap1.get("H1_a_Maps").get(is,il,4).getData();                
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
                  float  meanmap[] = (float[]) map.get(1);
                  double distmap[] = (double[]) ecPix[idet].pixels.getDist(il);
                      
                  for (int ip=0 ; ip<ecPix[idet].ec_nstr[il-1] ; ip++) {
                     if (doCalibration) {
                        fits = new CalibrationData(is+idet*10,il,ip+1);
                        fits.getDescriptor().setType(DetectorType.EC);
                        fits.addGraph(ecPix[idet].strips.getpixels(il,ip+1,cnts),
                                      ecPix[idet].strips.getpixels(il,ip+1,distmap),
                                      ecPix[idet].strips.getpixels(il,ip+1,meanmap),
                                      ecPix[idet].strips.getpixels(il,ip+1,meanerr),
                                      ecPix[idet].strips.getpixels(il,ip+1,status));
                        fits.analyze();
                        calib.addEntry(is, il+idet*3, ip+1);
                        calib.setDoubleValue(fits.getFunc(0).parameter(0).value(), "A",    is, il+idet*3, ip+1);
                        calib.setDoubleValue(fits.getFunc(0).parameter(1).value(), "B",    is, il+idet*3, ip+1);
                        calib.setDoubleValue(fits.getFunc(0).parameter(2).value(), "C",    is, il+idet*3, ip+1);
                        calib.setDoubleValue(fits.getFunc(0).parameter(0).error(), "Aerr", is, il+idet*3, ip+1);
                        calib.setDoubleValue(fits.getFunc(0).parameter(1).error(), "Berr", is, il+idet*3, ip+1);
                        calib.setDoubleValue(fits.getFunc(0).parameter(2).error(), "Cerr", is, il+idet*3, ip+1);
                        ecPix[idet].collection.add(fits.getDescriptor(),fits);
                     }
                  }
               }
            }
              
         }  
        
        @Override
        public void fit(int sector, int layer, int paddle, double minRange, double maxRange){ 
           double mean = ftofPix[layer-1].strips.hmap2.get("H2_a_Hist").get(sector,0,0).sliceY(paddle-1).getMean();
           calib.addEntry(sector, layer, paddle);
           calib.setDoubleValue(mean, "B", sector, layer, paddle);
           calib.setDoubleValue(mean, "B", sector, layer, paddle);
        }
        
        public double getTestChannel(int sector, int layer, int paddle) {
            return calib.getDoubleValue("B", sector, layer, paddle);
        }
        
        @Override
        public boolean isGoodPaddle(int sector, int layer, int paddle) {

            return (getTestChannel(sector,layer,paddle) >=PARAM[1][layer-1]-DELTA[1][layer-1]  &&
                    getTestChannel(sector,layer,paddle) <=PARAM[1][layer-1]+DELTA[1][layer-1] );

        }    
              
        @Override
        public void drawPlots(int is, int layer, int ic, EmbeddedCanvas cl, EmbeddedCanvas cr) {
            
            DetectorCollection<CalibrationData> fit = ecPix[ilmap].collection;
            DatabaseConstantProvider ccdb = (DatabaseConstantProvider) mon.getGlob().get("ccdb");
            DetectorCollection<H2F>  dc2a = ecPix[ilmap].strips.hmap2.get("H2_a_Hist");    
            
            Boolean   isPix = false;
            Boolean   isStr = false;
            H1F      pixADC = null;
            int          il = 0;
            int    pixStrip = 0;
            int        nstr = ecPix[0].ec_nstr[0];
            int   inProcess =     (int) mon.getGlob().get("inProcess");
            Boolean    inMC = (Boolean) mon.getGlob().get("inMC");
              
            double[] xp     = new double[nstr];
            double[] xpe    = new double[nstr];
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
                      
            isPix = layer > 10;
            isStr = layer <  7;  
            il    = layer;
            pixStrip = ic+1;
            
            if (isStr) cl.divide(1,1);
            if (isPix) cl.divide(1,2);
                       cr.divide(1,3); 
                            
            if (isPix) {
               float meanmap[] = (float[]) ecPix[ilmap].Lmap_a.get(is, layer, 0).get(1);
               il = layer-10;
               xpix[0] = ecPix[ilmap].pixels.getDist(il, ic+1);
               ypix[0] = meanmap[ic];
               xerr[0] = 0.;
               yerr[0] = 0.;
               pixStrip = ecPix[ilmap].pixels.getStrip(il,ic+1);
                 pixADC = dc2a.get(is,il,2).sliceY(ic) ; pixADC.setFillColor(2);
            }
              
            if (isStr||isPix) {
               if (inProcess>0) {
                     nstr = ecPix[ilmap].ec_nstr[il-1];
                     if (inProcess==1)  {analyze(ilmap,is,is+1,il,il+1);}
                     if (fit.hasEntry(is+ilmap*10, il, pixStrip)) {
                                          
                     for (int ip=0; ip<nstr ; ip++) {
                        double gain  = fit.get(is+ilmap*10,il,ip+1).getFunc(0).parameter(0).value();
                        double gaine = fit.get(is+ilmap*10,il,ip+1).getFunc(0).parameter(0).error();    
                        double att   = fit.get(is+ilmap*10,il,ip+1).getFunc(0).parameter(1).value();
                        double atte  = fit.get(is+ilmap*10,il,ip+1).getFunc(0).parameter(1).error();
                        double chi2  = fit.get(is+ilmap*10,il,ip+1).getChi2(0);
                        int index = ECCommon.getCalibrationIndex(is,il+ilmap*3,ip+1);
                        double attdb = ccdb.getDouble("/calibration/ec/attenuation/B",index);
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
                  
                     gainGraph.getAttributes().setMarkerStyle(1);   
                     gainGraph.getAttributes().setMarkerSize(3);   
                     gainGraph.getAttributes().setMarkerColor(2);
                     gainGraph.getAttributes().setFillStyle(1);
                     gainGraph.getAttributes().setLineWidth(1);;
                     gainGraph.getAttributes().setLineColor(2);
                      attGraph.getAttributes().setMarkerStyle(1);    
                      attGraph.getAttributes().setMarkerSize(3);    
                      attGraph.getAttributes().setMarkerColor(2);
                      attGraph.getAttributes().setLineWidth(1);;
                      attGraph.getAttributes().setLineColor(2);
                    attdbGraph.getAttributes().setMarkerStyle(1);  
                    attdbGraph.getAttributes().setMarkerSize(3);  
                    attdbGraph.getAttributes().setFillStyle(1);
                    attdbGraph.getAttributes().setMarkerColor(1);
                    attdbGraph.getAttributes().setLineWidth(1);;
                     chi2Graph.getAttributes().setMarkerStyle(1);   
                     chi2Graph.getAttributes().setFillStyle(2);                       
                     chi2Graph.getAttributes().setMarkerSize(3);   
                     chi2Graph.getAttributes().setMarkerColor(2);
                      pixGraph.getAttributes().setMarkerStyle(1); 
                      pixGraph.getAttributes().setFillStyle(1);                       
                      pixGraph.getAttributes().setMarkerSize(3);    
                      pixGraph.getAttributes().setMarkerColor(2); 
                       
                     gainGraph.getAttributes().setTitleX(otab[ilmap][il-1]) ;  
                     gainGraph.getAttributes().setTitleY("PMT GAIN")  ; 
                     gainGraph.getAttributes().setTitle(" ");
                    attdbGraph.getAttributes().setTitleX(otab[ilmap][il-1]) ; 
                    attdbGraph.getAttributes().setTitleY("ATTENUATION (CM)") ; 
                    attdbGraph.getAttributes().setTitle(" ");
                     chi2Graph.getAttributes().setTitleX(otab[ilmap][il-1]) ;  
                     chi2Graph.getAttributes().setTitleY("REDUCED CHI^2"); 
                     chi2Graph.getAttributes().setTitle(" ");
                      
                     F1D f1 = new F1D("p0","[a]",0,nstr+1); f1.setParameter(0,mip[ilmap]); f1.setLineWidth(1);
                               
                     double ymax=200; if(!inMC) ymax=350;
                     cl.cd(0);
                     cl.getPad(0).getAxisX().setRange(0.,400.);cl.getPad(0).getAxisY().setRange(0.,ymax); 
                     cl.getPad(0).setTitle("Sector "+is+otab[ilmap][il-1]+pixStrip+" - Fit to pixels");
                     cl.draw(fit.get(is+ilmap*10,il,pixStrip).getRawGraph(0));                 
                     if(fit.get(is+ilmap*10,il,pixStrip).getFitGraph(0).getDataSize(0)>0) 
                     cl.draw(fit.get(is+ilmap*10,il,pixStrip).getFitGraph(0),"same");                      
                     cl.draw(fit.get(is+ilmap*10,il,pixStrip).getFunc(0),"same");

                     if (isPix) {                
                         cl.draw(pixGraph,"same");                     
                         pixADC.setTitleX("Sector "+is+otab[ilmap][il-1]+pixStrip+" Pixel "+(ic+1)+" ADC");
                         cl.cd(1); pixADC.setOptStat(Integer.parseInt("110")); pixADC.setTitle(""); cl.draw(pixADC);
                     }
                      
                     double xmax = ecPix[ilmap].ec_nstr[il-1]+1;
                     cr.cd(0); cr.getPad(0).getAxisX().setRange(0.,xmax);cr.getPad(0).getAxisY().setRange(0.,4.);
                     cr.draw(chi2Graph) ; 
                     cr.cd(1); cr.getPad(1).getAxisX().setRange(0.,xmax);cr.getPad(1).getAxisY().setRange(0.,ymax);
                     cr.draw(gainGraph) ; cr.draw(f1,"same"); 
                     cr.cd(2); cr.getPad(2).getAxisX().setRange(0.,xmax);cr.getPad(2).getAxisY().setRange(0.,600.);
                     cr.draw(attdbGraph); cr.draw(attGraph,"same");   
                    
                     cl.repaint();
                     cr.repaint();
                      
                  }
               }
            }
            calib.fireTableDataChanged();
        }

    }
    
    public class ECGainEventListener extends ECCalibrationEngine {
        
        public final int[]      PARAM = {100,100,100,100,100,100,160,160,160};
        public final int        DELTA = 10;        
        
        int is1,is2;
        
        ECGainEventListener(){};
        
        public void init(int is1, int is2) {
            
            System.out.println("ECCalibrationApp:ECGainEventListener.init");
            this.is1=is1;
            this.is2=is2;
            
            calib = new CalibrationConstants(3,"gain/F");
            calib.setName("/calibration/ec/gain");
            calib.setPrecision(3);

            for (int i=0; i<9; i++) { 
                calib.addConstraint(3, PARAM[i]-DELTA, 
                                       PARAM[i]+DELTA, 1, i+1);
            }
            
            list.add(calib);         
        } 
        
        @Override
        public void analyze() {
            for (int sector = is1; sector < is2; sector++) {
                for (int layer = 1; layer < 4; layer++) {
                    for (int paddle = 1; paddle<NUM_PADDLES[layer-1]+1; paddle++) {
                        fit(sector, layer, paddle, 0.0, 0.0);
                    }
                }
            }
            calib.fireTableDataChanged();
        }
        
        @Override
        public void fit(int sector, int layer, int paddle, double minRange, double maxRange){ 
           double mean = ftofPix[layer-1].strips.hmap2.get("H2_a_Hist").get(sector,0,0).sliceY(paddle-1).getMean();
           calib.addEntry(sector, layer, paddle);
           calib.setDoubleValue(mean, "mipa_left", sector, layer, paddle);
           calib.setDoubleValue(mean, "mipa_right", sector, layer, paddle);
        }
        
        public double getTestChannel(int sector, int layer, int paddle) {
            return calib.getDoubleValue("mipa_left", sector, layer, paddle);
        }
        
        @Override
        public boolean isGoodPaddle(int sector, int layer, int paddle) {
            return (getTestChannel(sector,layer,paddle) >=PARAM[layer-1]-DELTA  &&
                    getTestChannel(sector,layer,paddle) <=PARAM[layer-1]+DELTA );
        }   
        
        public void drawPlots(int is, int il, int ic, EmbeddedCanvas cl, EmbeddedCanvas cr) {
        }

    }
    
    private class ECStatusEventListener extends ECCalibrationEngine {
        
        public final int[]   PARAM = {0,0,0,0,0,0,0,0,0};
        public final int     DELTA = 1;
        
        ECStatusEventListener(){};
        
        public void init(int is1, int is2){
            
            System.out.println("ECCalibrationApp:ECStatusEventListener.init");
            calib = new CalibrationConstants(3,"status/I");
            calib.setName("/calibration/ec/status");
            calib.setPrecision(3);
            
            for (int i=0; i<9; i++) {
                calib.addConstraint(3, PARAM[i]-DELTA, 
                                       PARAM[i]+DELTA, 1, i+1);
            }
 
/*            
            for(int is=is1; is<is2; is++) {                
                for(int il=1; il<3; il++) {
                    for(int ip = 1; ip < 19; ip++) {
                        calib.addEntry(is,il,ip);
                        calib.setIntValue(0,"status",is,il,ip);
                    }
                }
            }
            */
            list.add(calib);
        }
        
        public void drawPlots(int is, int il, int ic, EmbeddedCanvas cl, EmbeddedCanvas cr) {
        }
    }
    
    public void analyze(int idet, int is1, int is2, int il1, int il2) {    
        
        for (int i=0; i< engines.length; i++) {
            engines[i].analyze(idet,is1,is2,il1,il2);
        }
    }    
   
    public void updateDetectorView(DetectorShape2D shape) {
        
        ECCalibrationEngine engine = getSelectedEngine();
        DetectorDescriptor dd = shape.getDescriptor();
        this.getDetIndices(dd);
        layer = lay;
        if (this.omap==3) {
           if(engine.isGoodPaddle(is, layer-1, ic)) {
               shape.setColor(101, 200, 59);
           } else {
               shape.setColor(225, 75, 60);
           }
        }
    }
        
    public void constantsEvent(CalibrationConstants cc, int col, int row) {

        String str_sector    = (String) cc.getValueAt(row, 0);
        String str_layer     = (String) cc.getValueAt(row, 1);
        String str_component = (String) cc.getValueAt(row, 2);
            
        if (cc.getName() != selectedDir) {
            selectedDir = cc.getName();
        }
            
        selectedSector = Integer.parseInt(str_sector);
        selectedLayer  = Integer.parseInt(str_layer);
        selectedPaddle = Integer.parseInt(str_component);

    }
    
    public void updateCanvas(DetectorDescriptor dd) {
        
        ECCalibrationEngine engine = getSelectedEngine();
        this.getDetIndices(dd);
        engine.drawPlots(is,lay,ic,leftCanvas,rightCanvas);
        
    }
        
        /*
        public void updateCanvas() {

            IndexedList<DataGroup> group = getSelectedEngine().getDataGroup();
            
            if(group.hasItem(selectedSector,selectedLayer,selectedPaddle)==true){
                DataGroup dataGroup = group.getItem(selectedSector,selectedLayer,selectedPaddle);
                this.canvas.draw(dataGroup);
                this.canvas.update();
            } else {
                System.out.println(" ERROR: can not find the data group");
            }
       
        }   
*/   
    public void stateChanged(ChangeEvent e) {
        int i = ccview.getTabbedPane().getSelectedIndex();
        String tabTitle = ccview.getTabbedPane().getTitleAt(i);
        if (tabTitle != selectedDir) {
            selectedDir = tabTitle;
        }
    }
 
}
