  package org.clas.fcmon.tools;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

//import org.clas.tools.FTParameter;
import org.jlab.clas.detector.DetectorCollection;
import org.jlab.detector.base.DetectorDescriptor;
import org.jlab.evio.clas12.EvioDataBank;
import org.root.basic.EmbeddedCanvas;
import org.root.histogram.H1D;
import org.root.histogram.H2D;

public class FCApplication implements ActionListener  {
	
    private String                                 appName    = null;
    private List<EmbeddedCanvas>                   canvases   = new ArrayList<EmbeddedCanvas>();
    private JPanel                                 radioPane  = new JPanel();
    private List<String>                           fields     = new ArrayList<String>();
    private List<FCParameter>                      parameters = new ArrayList<FCParameter>();
    
	public ECPixels[]                                   ecPix = new ECPixels[2];
	public DetectorCollection<TreeMap<Integer,Object>> Lmap_a = new  DetectorCollection<TreeMap<Integer,Object>>();
	public TreeMap<String, DetectorCollection<H1D>>     hmap1 = new TreeMap<String, DetectorCollection<H1D>>();
	public TreeMap<String, DetectorCollection<H2D>>     hmap2 = new TreeMap<String, DetectorCollection<H2D>>();
	 
	public MonitorApp      app = null;
	public DetectorMonitor mon = null;
	
	public int is,layer,ic;
	public int panel,io,of,lay,l1,l2;

    private String             buttonSelect;
    private int                buttonIndex;
    private String             canvasSelect;
    private int                canvasIndex;
    
    public FCApplication(ECPixels[] ecPix) {
        this.ecPix = ecPix;     
    }
    
    public FCApplication(String name, ECPixels[] ecPix) {
        this.appName = name;
        this.ecPix = ecPix;   
        this.addCanvas(name);
    }
	
    public String getName() {
        return this.appName;
    }
    
    public void setName(String name) {
        this.appName = name;
    }	
    
	public void getDetIndices(DetectorDescriptor dd) {
        is    = dd.getSector();
        layer = dd.getLayer();
        ic    = dd.getComponent(); 	 
                
        panel = app.getDetectorView().omap;
        io    = app.getDetectorView().ilmap;
        of    = (io-1)*3;
        lay   = 0;
        
        if (layer<4)  lay = layer+of;
        if (layer==4) lay = layer+2+io;
        if (panel==9) lay = panel+io-1;
        if (panel>10) lay = panel+of;
        
        l1 = of+1;
        l2 = of+4;  
	}
	
	public void addH1DMaps(String name, DetectorCollection<H1D> map) {
		this.hmap1.put(name,map);
	}
	
	public void addH2DMaps(String name, DetectorCollection<H2D> map) {
		this.hmap2.put(name,map);
	}
	
	public void addLMaps(String name, DetectorCollection<TreeMap<Integer,Object>> map) {
		this.Lmap_a=map;
	}
	
	public void setMonitoringClass(MonitorApp app) {
		this.app = app;
	}
	
	public void setApplicationClass(DetectorMonitor mon) {
		this.mon = mon;
	}
	
	public void process(EvioDataBank bank) {
	    
	}
	
	public void analyze() {
	}
	
	public void analyze(int is1, int is2, int il1, int il2, int ip1, int ip2) {
	}
	
	public void updateCanvas(DetectorDescriptor dd, EmbeddedCanvas canvas) {		
	}
	
    public final void addCanvas(String name) {
        EmbeddedCanvas c = new EmbeddedCanvas();
        this.canvases.add(c);
        this.canvases.get(this.canvases.size()-1).setName(name);
    }
    
    public EmbeddedCanvas getCanvas(int index) {
        return this.canvases.get(index);
    }
    
    public EmbeddedCanvas getCanvas(String name) {
        int index=0;
        for(int i=0; i<this.canvases.size(); i++) {
            if(this.canvases.get(i).getName() == name) {
                index=i;
                break;
            }
        }
        return this.canvases.get(index);
    }
    
    public void setRadioButtons() {
        this.radioPane.setLayout(new FlowLayout());
        ButtonGroup bG = new ButtonGroup();
        for (String field : this.fields) {
//            System.out.println(field);
            String item = field;
            // add buttons named as "fields" to the button group and panel
            JRadioButton b = new JRadioButton(item);
            if(bG.getButtonCount()==0) b.setSelected(true);
            b.addActionListener(this);
            this.radioPane.add(b);
            bG.add(b);
        }
    }  
    
    public void actionPerformed(ActionEvent e) {
//      System.out.println(this.getName() + " application radio button set to: " + e.getActionCommand());
      buttonSelect=e.getActionCommand();
      for(int i=0; i<this.fields.size(); i++) {
          if(buttonSelect == this.fields.get(i)) {
              buttonIndex=i;
              break;
          }
      }
    }
    
    public void setRadioPane(JPanel radioPane) {
        this.radioPane = radioPane;
    }

    public JPanel getRadioPane() {
        return radioPane;
    }
    
    public String getButtonSelect() {
        return buttonSelect;
    }
    
    public int getButtonIndex() {
        return buttonIndex;
    }
    
    public String getCanvasSelect() {
        if(canvasSelect == null) {
            canvasIndex  = 0;
            canvasSelect = this.canvases.get(0).getName();
        }
        return canvasSelect;
    }
    
    public void setCanvasSelect(String name) {
        canvasIndex  = 0;
        canvasSelect = this.canvases.get(0).getName();
        for(int i=0; i<canvases.size(); i++) {
            if(canvases.get(i).getName() == name) {
                canvasIndex = i;
                canvasSelect = name;
                break;
            }
        }
    }
    
    public void setCanvasIndex(int index) {
        if(index>=0 && index < this.canvases.size()) {
            canvasIndex  = index;
            canvasSelect = this.canvases.get(index).getName();
        }
        else {
            canvasIndex  = 0;
            canvasSelect = this.canvases.get(0).getName();
        }
    }

}
