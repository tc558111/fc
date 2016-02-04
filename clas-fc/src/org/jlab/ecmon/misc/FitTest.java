package org.jlab.ecmon.misc;

import org.root.func.F1D;
import org.root.histogram.GraphErrors;
import org.root.pad.TGCanvas;

public class FitTest {
	String name;
    
    double[] x2 = {10.0, 20.0};
    double[] ex2 = {1.0, 1.0};
    double[] y2 = {10.0, 20.0};
    double[] ey2 = {1.0, 1.0};
    double[] x1 = {10.0};
    double[] ex1 = {1.0};
    double[] y1 = {10.0};
    double[] ey1 = {1.0};
    
    public FitTest() {
    	dofit();
}

	public void dofit() {
		
    F1D fitfunc1 = new F1D("p0",9.0,11.0);
    fitfunc1.setName("test_of_fit1");
    fitfunc1.setParameter(0, 10.0);

    F1D fitfunc2 = new F1D("p0",9.0,11.0);
    fitfunc2.setName("test_of_fit2");
    fitfunc2.setParameter(0, 10.0);

    //test 1
    TGCanvas test1 = new TGCanvas("test1", "test1", 500, 500,1,1);
    name = String.format("testgraph_%02d", 1);
    GraphErrors g1 = new GraphErrors(name,x2,y2,ex2,ey2); //creates graph with a name and 1 point
    g1.fit(fitfunc1);
    test1.draw(g1);
    test1.setAxisRange(0.0, 25.0, 0.0, 25.0);
    test1.draw(fitfunc1,"same");

    //test 2
    TGCanvas test2 = new TGCanvas("test2", "test2", 500, 500,1,1);
    name = String.format("testgraph_%02d", 2);
    GraphErrors g2 = new GraphErrors(name,x1,y1,ex1,ey1); //creates graph with a name and 2 points
    g2.fit(fitfunc2); 
    test2.draw(g2);
    test2.setAxisRange(0.0, 25.0, 0.0, 25.0);
    test2.draw(fitfunc2,"same");
    
	}
    
public static void main(String[] args){		
	FitTest dum = new FitTest();
}

}