package org.jlab.eccal;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.TreeMap;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jlab.clas.detector.ConstantsTable;
import org.jlab.clas.detector.ConstantsTablePanel;
import org.jlab.clas.detector.DetectorCollection;
import org.jlab.clas.detector.DetectorDescriptor;
import org.jlab.clas.detector.DetectorType;
import org.jlab.clas.detector.IConstantsTableListener;
import org.jlab.clas12.calib.CalibrationPane;
import org.jlab.clas12.calib.DetectorDatasetPane;
import org.jlab.clas12.calib.DetectorShape2D;
import org.jlab.clas12.calib.DetectorShapeView2D;
import org.jlab.clas12.calib.IDetectorListener;
import org.jlab.clasrec.main.DataEventProcessorDialog;
import org.jlab.clasrec.main.DataEventProcessorThread;
import org.jlab.clasrec.main.IDataEventProcessor;
import org.jlab.data.io.DataEvent;
import org.root.attr.TStyle;
import org.root.histogram.H1D;
import org.root.basic.EmbeddedCanvas;
import org.root.pad.TBookCanvas;

/**
 *
 * @author gavalian
 */
public class ECCal implements IDetectorListener,
        IConstantsTableListener,ActionListener, IDataEventProcessor {
    
    private EmbeddedCanvas            canvas = new EmbeddedCanvas();    
    private CalibrationPane           calibPane = new CalibrationPane();
    private DetectorCollection<H1D>   tdcHistograms = new DetectorCollection<H1D>();
    
    private ConstantsTable        constantsTable = null;
    private ConstantsTablePanel   constantsTablePanel = null;
    private TreeMap<Integer,H1D>  histograms = new TreeMap<Integer,H1D>();
    
	double ec_xpix[][] = new double[3][1296];
	double ec_ypix[][] = new double[3][1296];
	double ec_cthpix[] = new double[1296];
	int pixmap[][]     = new int[3][1296];    
	
    public ECCal(){
        this.initDetector();
        this.init();
        
        TStyle.setFrameFillColor(235, 255, 245);
        TStyle.setFrameBackgroundColor(245, 255, 245);
    }
    
    public JPanel getView(){ return this.calibPane;}
    
    public void init(){
        this.calibPane.getCanvasPane().add(canvas);        
        this.constantsTable = new ConstantsTable(DetectorType.LTCC,
                new String[]{"Peak Mean","Mean Error","Peak Sigma","Sigma Error"});
                
        this.tdcHistograms.setName("LTCC_TDC");
        for(int sector = 0; sector < 6; sector++){
            for(int region = 0; region < 8; region++){
                this.constantsTable.addEntry(sector, region, 0);
                this.tdcHistograms.add(sector, region, 0, 
                        new H1D("TDC_" + sector + "_"+region,
                                "TDC (SECTOR " + sector + " REGION " + region + ")", 
                                200,0.0,4000.0));
            }
        }
        
        
        
        this.constantsTablePanel = new ConstantsTablePanel(this.constantsTable);
        this.constantsTablePanel.addListener(this);        
        this.calibPane.getTablePane().add(this.constantsTablePanel);
        
        JButton buttonFit = new JButton("Fit");
        buttonFit.addActionListener(this);
        
        JButton buttonProc = new JButton("Process");
        buttonProc.addActionListener(this);
        
        JButton buttonProcEt  = new JButton("Process ET");
        buttonProcEt.addActionListener(this);
        
        JButton buttonTDC = new JButton("Show TDC");
        buttonTDC.addActionListener(this);
        
        
        this.calibPane.getBottonPane().add(buttonFit);
        this.calibPane.getBottonPane().add(buttonProc);
        this.calibPane.getBottonPane().add(buttonProcEt);
        this.calibPane.getBottonPane().add(buttonTDC);

    }
	public void ecpixdef() {
	      System.out.println("ecpixdef():");
		       int jmax,pixel,m;
		       double xtmp,ytmp,tmp;
		       double   y_inc=10.0;
		       double   x_inc=5.31;
		       double[] xstrt={0.0, -5.31, 5.31};
		       double[] ystrt={0.0, -10.0,-10.0};
		       double[] xtrans={0.0,0.0,0.0};
		       double[] ytrans={0.0,0.0,0.0};
		       double[]  yflip={-20.0,0.0,0.0};
	       
		       for(int u=1; u<37; u++) {
		           jmax = 2*u-1;
		           pixel= u*(u-1)-u;
		           tmp=y_inc*(u-1);
		           ytrans[0]=tmp;
		           ytrans[1]=tmp;
		           ytrans[2]=tmp;
		           for (int j=1; j<jmax+1; j=j+2) {
		               m=-u+j;
		               pixel=pixel+2;
		               tmp=x_inc*m;
		               xtrans[0]=tmp;
		               xtrans[1]=tmp;
		               xtrans[2]=tmp;
		               for (int k=0;k<3;k++) {
		                   xtmp=(xstrt[k]+xtrans[k]);
		                   ytmp=(ystrt[k]-ytrans[k]);
		                   ec_xpix[k][pixel-1]=xtmp;
		                   ec_ypix[k][pixel-1]=ytmp;
		                   if (u!=36) {
		                       ytmp=ytmp+yflip[k];
		                       ec_xpix[k][pixel+2*u-1]=xtmp;
		                       ec_ypix[k][pixel+2*u-1]=ytmp;
		                   }     
		               }
		           }
		       }
		}
		
		public void ecpixang() {
		  System.out.println("ecpixang():");
			double x,y,r,angle,r0=510.3;
			int jmax,m,pixel,sign;
			double[] off={3.453,6.907};
			
			pixel=0;
			
			for (int u=1;u<37;u++){
				jmax = 2*u-1;
				for (int j=1;j<jmax+1;j++){
					m = -u+j;
					pixel = pixel +1;
					sign=j%2;
					x = (18-u)*10.36-off[sign]+3.453;
					y = m*5.305;
					r = Math.sqrt(x*x+y*y);
					angle = Math.atan(r/r0);
					ec_cthpix[pixel-1]=1./Math.cos(angle);
				}
						
			}	
		}
		
		public void ecpixmap() {
	      System.out.println("ecpixmap():");
			int pixel;
			for (int u=1 ; u<37 ; u++){
				int jmax = 2*u-1;
				int v=36 ; int w=36-u+1 ; int uvw=73 ; int nj=0;
				for (int j=1; j<jmax+1 ; j++) {
					if (nj==2) {v--; nj=0;}
					w=uvw-u-v;
					pixel=pix(u,v,w);
					pixmap[0][pixel-1]=u;
					pixmap[1][pixel-1]=v;
					pixmap[2][pixel-1]=w;
					switch (uvw) {
					case 73: uvw=74;
					break;
					case 74: uvw=73;
					break;
					}
					nj++;
				}
			}
		}
		
		public int pix(int u, int v, int w) {
			return u*(u-1)+v-w+1;
		}
		
    public void initDetector(){
        
        DetectorShapeView2D view = new DetectorShapeView2D("EC");
        for(int sector = 0; sector < 6; sector++){
            for(int region = 0; region < 3; region++){
                for(int half = 0; half < 4; half++){
                    double arcStart = 280 + region*60;
                    double arcEnd   = 280 + (region+1)*60;
                    double midAngle = sector*60;
                    double rotation = Math.toRadians(midAngle);
                    DetectorShape2D  shape = new DetectorShape2D(DetectorType.EC,sector,region,half);
                    switch (half) {
                    case 0: shape.createArc(arcStart, arcEnd, midAngle-25, midAngle-16.6); 
                    break;
                    case 1: shape.createArc(arcStart, arcEnd, midAngle-16.6, midAngle+16.6);
                    break;
                    case 2: shape.createArc(arcStart, arcEnd, midAngle+16.6, midAngle+25);
                    }
                    if(region%2==0){
                        shape.setColor(180, 180, 255);
                    } else {
                        shape.setColor(180, 255, 180);
                    }
                    view.addShape(shape);
                }
            }
        }
        view.addDetectorListener(this);
        this.calibPane.getDetectorView().addDetectorLayer(view);
    }
    /**
     * This method comes from detector listener interface.
     * @param dd 
     */
    public void detectorSelected(DetectorDescriptor dd) {
        System.out.println(" DETECTOR SELECTED " + dd.toString());
    }

    public void update(DetectorShape2D dsd) {
        
    }
    
    public void entrySelected(int i, int i1, int i2) {
        System.out.println(" ENTRY SELECTED FROM TABLE = " + i + i1 + i2);
    }
    
   

    public void actionPerformed(ActionEvent e) {
        System.out.println("ACTION PERFORMED : " + e.getActionCommand());
        if(e.getActionCommand().compareTo("Fit")==0){
            System.out.println("---> I think you want me to fit something.");
        }
        
        if(e.getActionCommand().compareTo("Process")==0){
            
            System.out.println("---> I think you want me to fit something.");
            DataEventProcessorDialog.runProcess(this);
            // This commented section is used to run on specific file in background mode
            /*String filename = "/Users/gavalian/Work/Software/Release-8.0/COATJAVA/coatjava/../etaPXSection_0_recon.evio";
            DataEventProcessorThread thread = new DataEventProcessorThread();
            thread.setFileName(filename);
            thread.addProcessor(this);
            thread.start();*/
        }
        
        
        if(e.getActionCommand().compareTo("Process ET")==0){
            //System.out.println("---> I think you want me to fit something.");
            //DataEventProcessorDialog.runProcess(this);
            
            DataEventProcessorThread thread = new DataEventProcessorThread();
            thread.setFileName("adcecal2:/tmp/et_sys_clasprod2");
            thread.addProcessor(this);
            thread.start();
        }
        
        if(e.getActionCommand().compareTo("Show TDC")==0){
            DetectorDatasetPane.showDialog(this.tdcHistograms);            
        }
        
    }
        
     public static void main(String[] args){
        JFrame frame = new JFrame();
        frame.setSize(1200, 700);
        ECCal calib = new ECCal();
        frame.add(calib.getView());
        frame.pack();
        frame.setVisible(true);
    }

    public void process(DataEvent de) {
        System.out.println("running LTCC code");
        //if(de.hasBank("LTCC")==true){
        //}
    }
}
