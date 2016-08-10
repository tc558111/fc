package org.clas.fcmon.ftof;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.Timer;

import org.clas.fcmon.tools.FCEpics;
import org.jlab.clas.detector.DetectorCollection;
import org.jlab.detector.base.DetectorDescriptor;
import org.root.basic.EmbeddedCanvas;
import org.root.histogram.H1D;
import org.root.histogram.H2D;

public class FTOFHvApp extends FCEpics {
   
    DetectorCollection<H1D> H1_HV = new DetectorCollection<H1D>();
    DetectorCollection<H2D> H2_HV = new DetectorCollection<H2D>();
    DetectorCollection<LinkedList<Double>> fifo1 = new DetectorCollection<LinkedList<Double>>();
    DetectorCollection<LinkedList<Double>> fifo2 = new DetectorCollection<LinkedList<Double>>();
    DetectorCollection<LinkedList<Double>> fifo3 = new DetectorCollection<LinkedList<Double>>();
    
    updateGUIAction action = new updateGUIAction();
    
    Timer timer = null;
    int delay=2000;
    int nfifo=0, nmax=120;
    int isCurrentSector;
    int isCurrentLayer;
    
    FTOFHvApp(String name, String det) {
        super(name, det);
    }
    
    public void init(int is1, int is2) {
        this.is1=is1;
        this.is2=is2;
        setPvNames(this.detName,0);
        setCaNames(this.detName,0);
        sectorSelected=is1;
        layerSelected=1;
        channelSelected=1;
        initHistos();
        initFifos();
        fillFifos();
        fillHistos();
        this.timer = new Timer(delay,action);  
        this.timer.setDelay(delay);
        this.timer.start();
    }
    
    private class updateGUIAction implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            fillFifos();
            fillHistos();
            update1DScalers(scaler1DView,1);   
            update2DScalers(scaler2DView,1);        }
    } 
    
    public void initHistos() {       
        for (int is=is1; is<is2 ; is++) {
            for (int il=1 ; il<layMap.get(detName).length+1 ; il++){
                int nb=nlayMap.get(detName)[il-1]; int mx=nb+1;
                H1_HV.add(is, il, 0, new H1D("HV_vset"+is+"_"+il, nb,1,mx));                
                H1_HV.add(is, il, 1, new H1D("HV_vmon"+is+"_"+il, nb,1,mx));                
                H1_HV.add(is, il, 2, new H1D("HV_imon"+is+"_"+il, nb,1,mx));                
                H2_HV.add(is, il, 0, new H2D("HV_vset"+is+"_"+il, nb,1,mx,nmax,0,nmax));                
                H2_HV.add(is, il, 1, new H2D("HV_vmon"+is+"_"+il, nb,1,mx,nmax,0,nmax));                
                H2_HV.add(is, il, 2, new H2D("HV_imon"+is+"_"+il, nb,1,mx,nmax,0,nmax));                
            }
        }
    }
        
    public void initFifos() {
        for (int is=is1; is<is2 ; is++) {
            for (int il=1; il<layMap.get(detName).length+1 ; il++) {
                for (int ic=1; ic<nlayMap.get(detName)[il-1]+1; ic++) {
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
        
        //long startTime = System.currentTimeMillis();
        nfifo++;
        for (int is=is1; is<is2 ; is++) {
            for (int il=1; il<layMap.get(detName).length+1 ; il++) {
                for (int ic=1; ic<nlayMap.get(detName)[il-1]+1; ic++) {
                    if(nfifo>nmax) {
                        fifo1.get(is, il, ic).removeFirst();
                        fifo2.get(is, il, ic).removeFirst();
                        fifo3.get(is, il, ic).removeFirst();
                    }
                    fifo1.get(is, il, ic).add(getCaValue(0,"vset",is, il, ic));
                    fifo2.get(is, il, ic).add(getCaValue(0,"vmon",is, il, ic));
                    fifo3.get(is, il, ic).add(getCaValue(0,"imon",is, il, ic));
                }
            }
         }
       // System.out.println("time= "+(System.currentTimeMillis()-startTime));
        
    }

    public void fillHistos() {
        
        for (int is=is1; is<is2 ; is++) {
            for (int il=1; il<layMap.get(detName).length+1 ; il++) {
                H1_HV.get(is, il, 0).reset(); H2_HV.get(is, il, 0).reset();
                H1_HV.get(is, il, 1).reset(); H2_HV.get(is, il, 1).reset();
                H1_HV.get(is, il, 2).reset(); H2_HV.get(is, il, 2).reset();
                for (int ic=1; ic<nlayMap.get(detName)[il-1]+1; ic++) {                    
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
                }
            }
        }
        
    }
    
    public void updateCanvas(DetectorDescriptor dd) {
        
        sectorSelected  = dd.getSector();
        layerSelected   = dd.getLayer();
        channelSelected = dd.getComponent(); 
        
        update1DScalers(scaler1DView,0);   
        update2DScalers(scaler2DView,0);
        
        isCurrentSector = sectorSelected;
        isCurrentLayer  = layerSelected;
    }
    
    public void update1DScalers(EmbeddedCanvas canvas, int flag) {
        
        H1D h = new H1D();
        H1D c = new H1D();
        
        int is = sectorSelected;
        int lr = layerSelected;
        int ip = channelSelected; 
        
        if (lr==0||lr>layMap.get(detName).length) return;
        
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
    
    public void update2DScalers(EmbeddedCanvas canvas, int flag) {
        
        H2D h = new H2D();
        
        int is = sectorSelected;
        int lr = layerSelected;
                
        if (lr==0||lr>layMap.get(detName).length) return;
        
        //Don't redraw unless timer fires or new sector selected
        if (flag==0&&(is==isCurrentSector)) return;  
        
        canvas.divide(4, 1);
        
        h = H2_HV.get(is, 1, 0); h.setXTitle("Sector "+is+" Left PMT"); h.setYTitle("TIME");
        canvas.cd(0); canvas.draw(h);
        h = H2_HV.get(is, 2, 0); h.setXTitle("Sector "+is+" Right PMT"); h.setYTitle("TIME");
        canvas.cd(1);    canvas.draw(h);

        h = H2_HV.get(is, 1, 2); h.setXTitle("Sector "+is+" Left PMT"); h.setYTitle("TIME");
        canvas.cd(2); canvas.draw(h);
        h = H2_HV.get(is, 2, 2); h.setXTitle("Sector "+is+" Right PMT"); h.setYTitle("TIME");
        canvas.cd(3); canvas.draw(h);
        
        isCurrentSector = is;
        
    }
    
    
}
