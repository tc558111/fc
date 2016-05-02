package org.clas.fcmon.tools;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

public class Mode7Emulation extends JPanel implements ActionListener,ItemListener {
	
	DetectorShapeTabView detectorView;
	
	ButtonGroup       bG3  = new ButtonGroup();
    JRadioButton      bG3a = new JRadioButton("CCDB"); 
	JRadioButton      bG3b = new JRadioButton("User");
	JCheckBox           cb = new JCheckBox("RefPeds");
    public JTextField  nsa = new JTextField(3); 
    public JTextField  nsb = new JTextField(3);
    public JTextField  tet = new JTextField(3);
    
    public int User_pedref=0;
    public int User_tet=0;
    public int User_nsa=0;
    public int User_nsb=0;
    public int CCDB_tet=0;
    public int CCDB_nsa=0;
    public int CCDB_nsb=0;	    
    public int useCCDB = 1;
	
	public void setPluginClass(DetectorShapeTabView detectorView) {    		 
		this.detectorView = detectorView;
	}
	
	public Mode7Emulation() {
		
		this.setBackground(Color.LIGHT_GRAY);
		
		this.add(bG3a); bG3a.setActionCommand("CCDB"); bG3a.addActionListener(this); bG3a.setSelected(true); 
		this.add(bG3b); bG3b.setActionCommand("User"); bG3b.addActionListener(this);
		this.add(new JLabel("TET")); this.add(tet); tet.setActionCommand("TET"); tet.addActionListener(this);
		this.add(new JLabel("NSB")); this.add(nsb); nsb.setActionCommand("NSB"); nsb.addActionListener(this);
		this.add(new JLabel("NSA")); this.add(nsa); nsa.setActionCommand("NSA"); nsa.addActionListener(this);
		this.add(cb); cb.addItemListener(this); cb.setSelected(false);
		
		bG3a.setBackground(Color.LIGHT_GRAY);
		bG3b.setBackground(Color.LIGHT_GRAY);
		  cb.setBackground(Color.LIGHT_GRAY);
		bG3.add(bG3a); bG3.add(bG3b);
	}

	@Override
	public void actionPerformed(ActionEvent e) {			
        if(e.getActionCommand().compareTo("TET")==0) {
        	this.useCCDB=0; bG3b.setSelected(true);
			User_tet = Integer.parseInt(tet.getText());
			detectorView.repaint();
        }		 
        if(e.getActionCommand().compareTo("NSA")==0) {
        	this.useCCDB=0; bG3b.setSelected(true);
			User_nsa = Integer.parseInt(nsa.getText());
        }		 
        if(e.getActionCommand().compareTo("NSB")==0) {
        	this.useCCDB=0; bG3b.setSelected(true);
			User_nsb = Integer.parseInt(nsb.getText());
        }		 
        if(e.getActionCommand().compareTo("CCDB")==0) {
        	this.useCCDB=1;	bG3a.setSelected(true);
        	tet.setText(Integer.toString(CCDB_tet));
        	nsa.setText(Integer.toString(CCDB_nsa));
        	nsb.setText(Integer.toString(CCDB_nsb));
        	User_tet=0;User_nsa=0;User_nsb=0;
        	detectorView.repaint();
        }
	}
 	
	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange()==ItemEvent.DESELECTED) User_pedref=0;
		if (e.getStateChange()==ItemEvent.SELECTED)   User_pedref=1;			
	}
}
