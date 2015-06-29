import org.jlab.geom.detector.ftof.*;
import org.jlab.geom.base.*;
import org.jlab.geom.*;
import org.jlab.geom.gui.*;
import org.jlab.geom.prim.*;
import org.jlab.clasrec.utils.*;
import org.jlab.clasrec.ui.*;
import org.jlab.geom.prim.*;
import javax.swing.JFrame;
import java.awt.BorderLayout;
import org.root.pad.*;
import org.root.histogram.*;
import org.root.func.*;
import java.lang.Math;
import java.awt.Color;
import org.jlab.evio.clas12.*;
import org.jlab.clasrec.main.*;

public class ECMonitoring extends DetectorMonitoring {

       def ec_xpix = new Object[3][1296];
       def ec_ypix = new Object[3][1296];
       H2D H_ADC = null;

public ECMonitoring(){

       super("ECMON","1.0","someone");
       initec();

}

@Override

public void init() {

       H_ADC = new H2D("HISTOGRAM",420,0,420,200,0.0,5000.0);

}

public void initec() {

       int jmax,pixel,m,xtmp,ytmp;
       double tmp;
       double   y_inc=10.0;
       double   x_inc=5.31;
       double[] xstrt=[0.0, -5.31, 5.31];
       double[] ystrt=[0.0, -10.0,-10.0];
       double[] xtrans=[0.0,0.0,0.0];
       double[] ytrans=[0.0,0.0,0.0];
       int[]    yflip=[-20,0,0];
        
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
                   xtmp=(int)(xstrt[k]+xtrans[k]+300);
                   ytmp=(int)(ystrt[k]-ytrans[k]-50);
                   ec_xpix[k][pixel-1]=xtmp;
                   ec_ypix[k][pixel-1]=-ytmp;
                   if (u!=36) {
                       ytmp=ytmp+yflip[k];
                       ec_xpix[k][pixel+2*u-1]=xtmp;
                       ec_ypix[k][pixel+2*u-1]=-ytmp;
                   }     
               }
           }
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
	EvioDataBank bank = event.getBank("EC::dgtz");
       	int nrows = bank.rows();
	for(int loop = 0; loop < nrows; loop++){
	//int id = 12*bank.getInt("idx",loop)*bank.getInt("idy",loop);
        //int adc = bank.getInt("ADC",loop);
        //H_ADC.fill(id,adc);
	}

	}
}

public Color getColor(int sector, int layer, int component){

       int r,g,b;
       if(layer==1) {
        r = 255*component/1296;
	b = 255-r;
	g = r+b;
	}
	if(layer==2) {
	 b = 255*component/1296;
	 r = 255-b;
	 g = r+b;
	}
	return new Color(r,g,b);

}

void drawComponent(int sector, int layer, int component, EmbeddedCanvas canvas){
     H1D  hist1 = new H1D("hist1",200,0.0,24.0);
     hist1.setFillColor(3);
     hist1.setTitle("SECTOR " + sector + " LAYER " + layer + " PIXEL " + component);
     for(int loop =0; loop < 12000; loop++){
     	     hist1.fill(Math.random()*24.0);
     }

     canvas.divide(5,2);
     canvas.cd(0);
     canvas.draw(hist1);
     canvas.cd(1);
     canvas.draw(hist1);
     canvas.cd(2);
     canvas.draw(hist1);

}

public DetectorShape3D getPixel(int sector, int layer, int pixel){

    double[] xc= new double[3];
    double[] yc= new double[3];

    for(int loop = 0; loop < 3; loop++){
    	    xc[loop] = ec_xpix[loop][pixel];
    	    yc[loop] = ec_ypix[loop][pixel];
    }

    DetectorShape3D shape = new DetectorShape3D();
    shape.SECTOR = sector;
    shape.LAYER  = layer;
    shape.COMPONENT = pixel;
    shape.setPoints(xc,yc);
    return shape;

}

public static void main(String[] args){

    ECMonitoring       monitor = new ECMonitoring();
    DetectorBrowserApp     app = new DetectorBrowserApp();

    for(int ip=0; ip<1296 ; ip++){
       DetectorShape3D shape = monitor.getPixel(1,1,ip);
       app.addDetectorShape("EC Inner",shape);
       //System.out.println(shape);
    }

    for(int ip=0; ip<1296 ; ip++){
       DetectorShape3D shape = monitor.getPixel(1,2,ip);
       app.addDetectorShape("EC Outer",shape);
       //System.out.println(shape);
    }

    //app.setHistogramDraw(calib);
    app.setPluginClass(monitor);
    app.updateDetectorView();

   }
   
}
