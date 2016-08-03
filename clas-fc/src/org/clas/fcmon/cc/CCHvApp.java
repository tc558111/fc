package org.clas.fcmon.cc;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.Timer;

import org.clas.fcmon.tools.FCEpics;
import org.jlab.clas.detector.DetectorCollection;
import org.jlab.detector.base.DetectorDescriptor;
import org.root.basic.EmbeddedCanvas;
import org.root.histogram.H1D;
import org.root.histogram.H2D;

public class CCHvApp extends FCEpics {
   
    DetectorCollection<H1D> H1_HV = new DetectorCollection<H1D>();
    DetectorCollection<H2D> H2_HV = new DetectorCollection<H2D>();
    DetectorCollection<LinkedList<Double>> fifo1 = new DetectorCollection<LinkedList<Double>>();
    DetectorCollection<LinkedList<Double>> fifo2 = new DetectorCollection<LinkedList<Double>>();
    DetectorCollection<LinkedList<Double>> fifo3 = new DetectorCollection<LinkedList<Double>>();
    
    Timer timer = null;
    int delay;
    
    CCHvApp(String name) {
        super(name);
    }
    
    public void init() {
        setPvNames("LTCC",0);
        setCaNames(0);
        initHistos();
        initFifos();
        fillFifos();
        fillHistos();
        updateGUIAction action = new updateGUIAction();
        delay = 6000;
        this.timer = new Timer(delay,action);  
        this.timer.setDelay(delay);
        this.timer.start();
    }
    
    private class updateGUIAction implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            fillFifos();
            fillHistos();
        }
    } 
    
    public void initHistos() {
        for (int is=1; is<7 ; is++) {
            for (int il=1 ; il<3 ; il++){
                H1_HV.add(is, il, 0, new H1D("LTCC_HV_vset"+is+"_"+il, 18,1.,19.));                
                H1_HV.add(is, il, 1, new H1D("LTCC_HV_vmon"+is+"_"+il, 18,1.,19.));                
                H1_HV.add(is, il, 2, new H1D("LTCC_HV_imon"+is+"_"+il, 18,1.,19.));                
                H2_HV.add(is, il, 0, new H2D("LTCC_HV_vset"+is+"_"+il, 18,1.,19.,30,0.,30.));                
                H2_HV.add(is, il, 1, new H2D("LTCC_HV_vmon"+is+"_"+il, 18,1.,19.,30,0.,30.));                
                H2_HV.add(is, il, 2, new H2D("LTCC_HV_imon"+is+"_"+il, 18,1.,19.,30,0.,30.));                
            }
        }
    }
        
    public void initFifos() {
        for (int is=1; is<7 ; is++) {
            for (int il=1; il<3 ; il++) {
                for (int ic=1; ic<19; ic++) {
                    fifo1.add(is, il, ic,new LinkedList<Double>());
                    fifo2.add(is, il, ic,new LinkedList<Double>());
                    fifo3.add(is, il, ic,new LinkedList<Double>());
                    connectCa(0,"vset",is,il,ic);
                    connectCa(0,"vmon",is,il,ic);
                    connectCa(0,"imon",is,il,ic);
                }
            }
        }
    }
    
    public void fillFifos() {
        
        long startTime = System.currentTimeMillis();
        for (int is=1; is<7 ; is++) {
            for (int il=1; il<3 ; il++) {
                for (int ic=1; ic<19; ic++) {
                    //long startTime = System.currentTimeMillis();
                    fifo1.get(is, il, ic).add(getCaValue(0,"vset",is, il, ic));
                    fifo2.get(is, il, ic).add(getCaValue(0,"vmon",is, il, ic));
                    fifo3.get(is, il, ic).add(getCaValue(0,"imon",is, il, ic));
                    //System.out.println("is,il,ic,time= "+is+" "+il+" "+ic+" "+(System.currentTimeMillis()-startTime));
                }
            }
         }
        System.out.println("time= "+(System.currentTimeMillis()-startTime));
        
    }

    public void fillHistos() {
        
        for (int is=1; is<7 ; is++) {
            for (int il=1; il<3 ; il++) {
                H1_HV.get(is, il, 0).reset();
                H1_HV.get(is, il, 1).reset();
                H1_HV.get(is, il, 2).reset();
                for (int ic=1; ic<19; ic++) {
                    //long startTime = System.currentTimeMillis();
                    H1_HV.get(is, il, 0).fill(ic,fifo1.get(is, il, ic).getLast());
                    H1_HV.get(is, il, 1).fill(ic,fifo2.get(is, il, ic).getLast());
                    H1_HV.get(is, il, 2).fill(ic,fifo3.get(is, il, ic).getLast());
                    Double ts1[] = new Double[fifo1.get(is, il, ic).size()];
                    fifo1.get(is, il, ic).toArray(ts1);
                    Double ts2[] = new Double[fifo2.get(is, il, ic).size()];
                    fifo2.get(is, il, ic).toArray(ts2);
                    Double ts3[] = new Double[fifo3.get(is, il, ic).size()];
                    fifo3.get(is, il, ic).toArray(ts3);
                    for (int it=0; it<ts1.length; it++) {
                        H2_HV.get(is, il, 0).fill(ic,it,ts1[it]);
                        H2_HV.get(is, il, 1).fill(ic,it,ts2[it]);
                        H2_HV.get(is, il, 2).fill(ic,it,ts3[it]);
                    }
                    //System.out.println("is,il,ic,time= "+is+" "+il+" "+ic+" "+(System.currentTimeMillis()-startTime));
                }
            }
        }
        
    }
    
    public void dothis() {
        
        LinkedList<Integer> fifo = new LinkedList<Integer>();
        int intoWalk = 21;

        for (int i=1; i<=20; i++)
            fifo.add(i);

        fifo.removeFirst();
        fifo.add(intoWalk);

        for (Integer fifoItem : fifo)
            System.out.println(fifoItem);        
        
    }
    
    public void updateCanvas(DetectorDescriptor dd) {
        
        update1DScalers(dd,scaler1DView);   
        update2DScalers(dd,scaler2DView);
        
    }
    
    public void update1DScalers(DetectorDescriptor dd, EmbeddedCanvas canvas) {
        
        H1D h = new H1D();
        H1D c = new H1D();
        
        int is = dd.getSector()+1;
        int lr = dd.getLayer();
        int ip = dd.getComponent();   
        
        canvas.divide(4, 1);
        
        h = H1_HV.get(is, 1, 0); h.setXTitle("Sector "+is+" Left PMT"); h.setYTitle("VOLTS");
        h.setFillColor(32); canvas.cd(0); canvas.draw(h);
        h = H1_HV.get(is, 2, 0); h.setXTitle("Sector "+is+" Right PMT"); h.setYTitle("VOLTS");
        h.setFillColor(32); canvas.cd(1);    canvas.draw(h);

        h = H1_HV.get(is, 1, 2); h.setXTitle("Sector "+is+" Left PMT"); h.setYTitle("MICROAMPS");
        h.setFillColor(32); canvas.cd(2); canvas.draw(h);
        h = H1_HV.get(is, 2, 2); h.setXTitle("Sector "+is+" Right PMT"); h.setYTitle("MICROAMPS");
        h.setFillColor(32); canvas.cd(3); canvas.draw(h);
        
        c = H1_HV.get(is, lr, 0).histClone("Copy"); c.reset() ; 
        c.setBinContent(ip, H1_HV.get(is, lr, 0).getBinContent(ip));
        c.setFillColor(2);  canvas.cd(lr-1); canvas.draw(c,"same");
        
        c = H1_HV.get(is, lr, 2).histClone("Copy"); c.reset() ; 
        c.setBinContent(ip, H1_HV.get(is, lr, 2).getBinContent(ip));
        c.setFillColor(2);  canvas.cd(lr-1+2); canvas.draw(c,"same");
               
    }
    
    public void update2DScalers(DetectorDescriptor dd, EmbeddedCanvas canvas) {
        
        H2D h = new H2D();
        
        int is = dd.getSector()+1;
        int lr = dd.getLayer();
        int ip = dd.getComponent();   
        
        canvas.divide(4, 1);
        
        h = H2_HV.get(is, 1, 0); h.setXTitle("Sector "+is+" Left PMT"); h.setYTitle("TIME");
        canvas.cd(0); canvas.draw(h);
        h = H2_HV.get(is, 2, 0); h.setXTitle("Sector "+is+" Right PMT"); h.setYTitle("TIME");
        canvas.cd(1);    canvas.draw(h);

        h = H2_HV.get(is, 1, 2); h.setXTitle("Sector "+is+" Left PMT"); h.setYTitle("TIME");
        canvas.cd(2); canvas.draw(h);
        h = H2_HV.get(is, 2, 2); h.setXTitle("Sector "+is+" Right PMT"); h.setYTitle("TIME");
        canvas.cd(3); canvas.draw(h);
        
    }
    
}
