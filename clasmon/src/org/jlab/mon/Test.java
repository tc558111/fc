package org.jlab.mon;

import org.root.pad.*;
import org.root.histogram.*;

public class Test {;

	public Test() {
    H1D h1 = new H1D("h1","TEST",200,0.0,14.0);
    h1.setXTitle("X");
    h1.setYTitle("Y");
    h1.setTitle("Title");
    ;
	TCanvas c1 = new TCanvas("c1","TEST",900,800,1,1);
	c1.cd(0);
	c1.draw(h1,"*");
	}
	
	public static void main(String[] args){
		   Test test = new Test();
		}
}
