package org.clas.fcmon.tools;

import java.awt.BorderLayout;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JSplitPane;

import math.geom2d.*;
import math.geom2d.polygon.Polygon2D;
import math.geom2d.polygon.Polygons2D;
import math.geom2d.polygon.SimplePolygon2D;

import org.jlab.clas.detector.DetectorType;
import org.jlab.clas12.calib.DetectorShape2D;
import org.jlab.clas12.calib.DetectorShapeTabView;
import org.jlab.clas12.calib.DetectorShapeView2D;
import org.jlab.clasrec.utils.CLASGeometryLoader;
import org.jlab.geom.component.ScintillatorPaddle;
import org.jlab.geom.detector.ec.ECDetector;
import org.jlab.geom.detector.ec.ECLayer;
import org.jlab.geom.prim.Point3D;
import org.root.basic.EmbeddedCanvas;

public class ECDrawDB {
	
	private double length;
	private double angle;
	private double anglewidth;
	private double slightshift;
	
	private double[] xrotation = new double [6];
	private double[] yrotation = new double [6];
	
	private static double[][][][] xPoint = new double [6][3][68][4];
	private static double[][][][] yPoint = new double [6][3][68][4];

	public ECDrawDB() {
		initVert();
		length = 4.5;
		angle = 62.8941;
		anglewidth = length/Math.sin(Math.toRadians(angle));
		slightshift = length/Math.tan(Math.toRadians(angle));
	}
	
	
	//collects all possible pixels into a DetectorShapeView2D
	public DetectorShapeView2D drawAllPixels(int sector)
	{
		DetectorShapeView2D pixelmap= new DetectorShapeView2D("PCAL Pixels");
            for(int uPaddle = 0; uPaddle < 68; uPaddle++){
            	for(int vPaddle = 0; vPaddle < 62; vPaddle++){
            		for(int wPaddle = 0; wPaddle < 62; wPaddle++){
            			if(isValidPixel(sector, uPaddle, vPaddle, wPaddle))
            				pixelmap.addShape(getPixelShape(sector, uPaddle, vPaddle, wPaddle));
            		}
            	}
            }
            return pixelmap;
	}
	
	
	//collects all possible UW intersections into a DetectorShapeView2D
	public DetectorShapeView2D drawUW(int sector)
	{
		DetectorShapeView2D UWmap= new DetectorShapeView2D("PCAL UW");
	    	for(int uPaddle = 0; uPaddle < 68; uPaddle++){
	            for(int wPaddle = 0; wPaddle < 62; wPaddle++){
	            	if(isValidOverlap(sector, "u", uPaddle, "w", wPaddle))
	            		UWmap.addShape(getOverlapShape(sector, "u", uPaddle, "w", wPaddle));
	            }
	          
	    	}
	        return UWmap;
	}
	
	//collects all possible UW intersections into a DetectorShapeView2D
	public DetectorShapeView2D drawWU(int sector)
	{
		DetectorShapeView2D WUmap= new DetectorShapeView2D("PCAL WU");
		   	for(int uPaddle = 0; uPaddle < 68; uPaddle++){
		   		for(int wPaddle = 0; wPaddle < 62; wPaddle++){
		            if(isValidOverlap(sector, "w", wPaddle, "u", uPaddle))
		            	WUmap.addShape(getOverlapShape(sector, "w", wPaddle, "u", uPaddle));
		        }
		          
		   	}
		    return WUmap;
	}
	
	//collects all possible UW intersections into a DetectorShapeView2D
	public DetectorShapeView2D drawVU(int sector)
	{
		DetectorShapeView2D UVmap= new DetectorShapeView2D("PCAL UV");
		   	for(int uPaddle = 0; uPaddle < 68; uPaddle++){
		   		for(int vPaddle = 0; vPaddle < 62; vPaddle++){
		            if(isValidOverlap(sector, "v", vPaddle, "u", uPaddle))
		            	UVmap.addShape(getOverlapShape(sector, "v", vPaddle, "u", uPaddle));
		        }
		          
		   	}
		    return UVmap;
	}
	
	
	
	//collects all U strips into a DetectorShapeView2D
	public DetectorShapeView2D drawUStrips(int sector)
	{
		DetectorShapeView2D Umap= new DetectorShapeView2D("PCAL U Strips");
		   	for(int uPaddle = 0; uPaddle < 68; uPaddle++){
		   		//System.out.println(uPaddle);
		   		Umap.addShape(getStripShape(sector, "u", uPaddle));
		   	}
		    return Umap;
	}
	
	//collects all V strips into a DetectorShapeView2D
	public DetectorShapeView2D drawVStrips(int sector)
	{
		DetectorShapeView2D Vmap= new DetectorShapeView2D("PCAL V Strips");
		   	for(int vPaddle = 0; vPaddle < 62; vPaddle++){
		   		//System.out.println(vPaddle);
		   		Vmap.addShape(getStripShape(sector, "v", vPaddle));
		   	}
		    return Vmap;
	}

	//collects all W strips into a DetectorShapeView2D
	public DetectorShapeView2D drawWStrips(int sector)
	{
		DetectorShapeView2D Wmap= new DetectorShapeView2D("PCAL W Strips");
		   	for(int wPaddle = 0; wPaddle < 62; wPaddle++){
		   		//System.out.println(wPaddle);
		   		Wmap.addShape(getStripShape(sector, "w", wPaddle));
		   	}
		    return Wmap;
	}
	

	
	//calls getPixelVerticies
	//uses those 3 verticies to make a shape
	public DetectorShape2D getPixelShape(int sector, int uPaddle, int vPaddle, int wPaddle){
		
		Object[] obj = getPixelVerticies(sector, uPaddle, vPaddle, wPaddle);
		int numpoints = (int)obj[0];
		double[] x = new double[numpoints];
		double[] y = new double[numpoints];
		System.arraycopy( (double[])obj[1], 0, x, 0, numpoints);
		System.arraycopy( (double[])obj[2], 0, y, 0, numpoints);
	
        
        DetectorShape2D  pixel = new DetectorShape2D(DetectorType.PCAL,sector,2,uPaddle * 10000 + vPaddle * 100 + wPaddle);
    	pixel.getShapePath().clear(); 
        if(numpoints > 2) 
        {
        	for(int i = 0; i < numpoints; ++i){ 
        		pixel.getShapePath().addPoint(x[i],  y[i],  0.0); 
        	} 
           
            /*
            if(paddle%2==0){
                shape.setColor(180, 255, 180);
            } else {
                shape.setColor(180, 180, 255);
            }
            */
        	return pixel;
        }
        else
        	return null;
				
	}

	//calls getOverlapVerticies
	//uses those 3 verticies to make a shape
	public DetectorShape2D getOverlapShape(int sector, String strip1, int paddle1, String strip2, int paddle2){
			
		int uPaddle = -1;
		int vPaddle = -1;
		int wPaddle = -1;
		if((strip1 == "u" || strip1 == "U"))
		{
			uPaddle = paddle1;
		}
		if((strip2 == "u" || strip2 == "U"))
		{
			uPaddle = paddle2;
		}
		if((strip1 == "v" || strip1 == "V"))
		{
			vPaddle = paddle1;
		}
		if((strip2 == "v" || strip2 == "V"))
		{
			vPaddle = paddle2;
		}
		if((strip1 == "w" || strip1 == "W"))
		{
			wPaddle = paddle1;
		}
		if((strip2 == "w" || strip2 == "W"))
		{
			wPaddle = paddle2;
		}
		
		Object[] obj = getOverlapVerticies(sector, strip1, paddle1, strip2, paddle2);
		int numpoints = (int)obj[0];
		double[] x = new double[numpoints];
		double[] y = new double[numpoints];
		System.arraycopy( (double[])obj[1], 0, x, 0, numpoints);
		System.arraycopy( (double[])obj[2], 0, y, 0, numpoints);
			
	      	
		DetectorShape2D  overlapShape;
		
		if(numpoints > 2)
		{
		    if(uPaddle == paddle1 && wPaddle == paddle2)
		    	overlapShape = new DetectorShape2D(DetectorType.PCAL,sector,3,uPaddle * 100 + wPaddle);
		    else if(uPaddle == paddle1 && vPaddle == paddle2)
	    		overlapShape = new DetectorShape2D(DetectorType.PCAL,sector,4,uPaddle * 100 + vPaddle);
		    else if(vPaddle == paddle1 && uPaddle == paddle2)
	    		overlapShape = new DetectorShape2D(DetectorType.PCAL,sector,4,uPaddle * 100 + vPaddle);
		    else if(wPaddle == paddle1 && uPaddle == paddle2)
		    	overlapShape = new DetectorShape2D(DetectorType.PCAL,sector,5,uPaddle * 100 + wPaddle);
		    else
		    	overlapShape = new DetectorShape2D(DetectorType.PCAL,sector,6,vPaddle * 100 + wPaddle);

	    
		    overlapShape.getShapePath().clear(); 

	        for(int i = 0; i < numpoints; ++i){ 
	        	overlapShape.getShapePath().addPoint(x[i],  y[i],  0.0); 
	        	//System.out.println("i: " + i + " x: " + x[i] + " y: " + y[i]);
	        } 

	           
	         /*
	         if(paddle%2==0){
	             shape.setColor(180, 255, 180);
	         } else {
	             shape.setColor(180, 180, 255);
	         }
	         */
	        return overlapShape;
	     }
		 else
		 {
			System.out.println("Not a valid overlap shape");
			return null;
		 }
	    		
	}
	
	
	//calls getOverlapVerticies
	//uses those 3 verticies to make a shape
	public DetectorShape2D getStripShape(int sector, String strip1, int paddle1){
			
		int uPaddle = -1;
		int vPaddle = -1;
		int wPaddle = -1;
		if((strip1 == "u" || strip1 == "U"))
		{
			uPaddle = paddle1;
		}
		if((strip1 == "v" || strip1 == "V"))
		{
			vPaddle = paddle1;
		}
		if((strip1 == "w" || strip1 == "W"))
		{
			wPaddle = paddle1;
		}

		
		Object[] obj = getStripVerticies(sector, strip1, paddle1);
		int numpoints = (int)obj[0];
		//System.out.println("Strip let: " + strip1 + "Strip num: " + paddle1 + " Numpoints: " + numpoints);
		double[] x = new double[numpoints];
		double[] y = new double[numpoints];
		System.arraycopy( (double[])obj[1], 0, x, 0, numpoints);
		System.arraycopy( (double[])obj[2], 0, y, 0, numpoints);
			
		/*
	    for(int i = 0; i< numpoints; ++i)
	    {
	        y[i] -= 400.0;
	    }
	    */

	      
	        	
		DetectorShape2D  stripShape;
	    if(uPaddle == paddle1)
	    	stripShape = new DetectorShape2D(DetectorType.PCAL,sector,7,uPaddle);
	    else if(vPaddle == paddle1)
	    	stripShape = new DetectorShape2D(DetectorType.PCAL,sector,8,vPaddle);
	    else if(wPaddle == paddle1)
	    	stripShape = new DetectorShape2D(DetectorType.PCAL,sector,9,wPaddle);
	    else
	    {
	    	stripShape = new DetectorShape2D();
	    	System.out.println("Either " + strip1 + " is not a valid strip letter, or " + paddle1 + " is not a valid number.");
	    }
	    
	    stripShape.getShapePath().clear(); 
	    if(numpoints > 2) 
	    {
	        for(int i = 0; i < numpoints; ++i){ 
	        	stripShape.getShapePath().addPoint(x[i],  y[i],  0.0); 
	        } 
	           
	         /*
	         if(paddle%2==0){
	             shape.setColor(180, 255, 180);
	         } else {
	             shape.setColor(180, 180, 255);
	         }
	         */
	     }
	    return stripShape;		
	}
	
	

	
	//calls getPixelVerticies to check
	//that at least 3 points exist,
	// if so it is marked as true, else false
	public Boolean isValidPixel(int sector, int uPaddle, int vPaddle, int wPaddle){
		Object[] obj = getPixelVerticies(sector, uPaddle, vPaddle, wPaddle);
		int numpoints = (int)obj[0];
		
        if(numpoints > 2) 
        	return true;
        else
        	return false;		
	}
	
	//calls getPixelVerticies to check
	//that at least 3 points exist,
	// if so it is marked as true, else false
	public Boolean isValidOverlap(int sector, String strip1, int paddle1, String strip2, int paddle2){
		Object[] obj = getOverlapVerticies(sector, strip1, paddle1, strip2, paddle2);
		int numpoints = (int)obj[0];
		//System.out.println("Blah!" + numpoints);
		//System.out.println("numpoints: " + numpoints);
        if(numpoints > 2) 
        	return true;
        else
        	return false;		
	}

	
	//returns an Object array of size 3
	//first element is the number of verticies (n) (int)
	//second element is an array x-coordinates (double[]) of size n
	//third element is an array y-coordinates (double[]) of size n
	//                                     0-5         0-67         0-61          0-61
	public Object[] getPixelVerticies(int sector, int uPaddle, int vPaddle, int wPaddle){
		
		if(isValidOverlap(sector, "u", uPaddle,"w",wPaddle) && isValidOverlap(sector, "u", uPaddle,"v",vPaddle) && isValidOverlap(sector, "v", vPaddle,"w",wPaddle))
		{
		//DetectorShape2D shape1 = getOverlapShape(sector, "u", uPaddle,"w",wPaddle);
		//DetectorShape2D shape2 = getOverlapShape(sector, "v", vPaddle,"w",wPaddle);
		//DetectorShape2D shape3 = getOverlapShape(sector, "u", uPaddle,"v",vPaddle);
		//if(shape1 != null && shape2 != null && shape3 != null)
		//{
			Object[] obj = getVerticies(getOverlapShape(sector, "u", uPaddle,"w",wPaddle),getStripShape(sector, "v",vPaddle));
			
			int numpoints = (int)obj[0];
			//System.out.println("Strip let: " + strip1 + "Strip num: " + paddle1 + " Numpoints: " + numpoints);
			double[] x = new double[numpoints];
			double[] y = new double[numpoints];
			System.arraycopy( (double[])obj[1], 0, x, 0, numpoints);
			System.arraycopy( (double[])obj[2], 0, y, 0, numpoints);
			return(new Object[]{numpoints, x, y});
			/*
			Object[] obj2 = sortVerticies(numpoints, x, y);
			
			int nPoints = (int)obj2[0];
			//System.out.println("Strip let: " + strip1 + "Strip num: " + paddle1 + " Numpoints: " + numpoints);
			double[] xnew = new double[nPoints];
			double[] ynew = new double[nPoints];
			System.arraycopy( (double[])obj2[1], 0, xnew, 0, nPoints);
			System.arraycopy( (double[])obj2[2], 0, ynew, 0, nPoints);
			
			return(new Object[]{nPoints, xnew, ynew});
			*/
		}
		else
		{
			return(new Object[]{0, 0.0, 0.0});
		}
		
	}
	
	
	//returns an Object array of size 3
	//first element is the number of verticies (n) (int)
	//second element is an array x-coordinates (double[]) of size n
	//third element is an array y-coordinates (double[]) of size n
	public Object[] getOverlapVerticies(int sector, String strip1, int paddle1, String strip2, int paddle2){
	
		Object[] obj = getVerticies(getStripShape(sector, strip1, paddle1),getStripShape(sector, strip2, paddle2));
		
		int numpoints = (int)obj[0];
		//System.out.println("Strip let: " + strip1 + "Strip num: " + paddle1 + " Numpoints: " + numpoints);
		double[] x = new double[numpoints];
		double[] y = new double[numpoints];
		System.arraycopy( (double[])obj[1], 0, x, 0, numpoints);
		System.arraycopy( (double[])obj[2], 0, y, 0, numpoints);
		
		return(new Object[]{numpoints, x, y});
		/*
		Object[] obj2 = sortVerticies(numpoints, x, y);
		
		int nPoints = (int)obj2[0];
		//System.out.println("Strip let: " + strip1 + "Strip num: " + paddle1 + " Numpoints: " + numpoints);
		double[] xnew = new double[nPoints];
		double[] ynew = new double[nPoints];
		System.arraycopy( (double[])obj2[1], 0, xnew, 0, nPoints);
		System.arraycopy( (double[])obj2[2], 0, ynew, 0, nPoints);

		return(new Object[]{nPoints, xnew, ynew});
		*/
	}
	

	public Object[] getStripVerticies(int sector, String strip1, int paddle1){
		int numpoints = 4;
		int l = 0;
		
		if((strip1 == "u" || strip1 == "U"))
		{
			l = 0;
		}
		if((strip1 == "v" || strip1 == "V"))
		{
			l = 1;
		}
		if((strip1 == "w" || strip1 == "W"))
		{
			l = 2;
		}
        
        return(new Object[]{numpoints, xPoint[sector][l][paddle1], yPoint[sector][l][paddle1]});
	}
	
	//estimates the shape center by calculating average x, y, z
	//from all verticies in the shape
	public double[] getShapeCenter(DetectorShape2D shape){
		double[] center = new double[3];
		int numpoints = shape.getShapePath().size();
		Point3D[] points = new Point3D[numpoints];
		Point3D centerp;
		for(int i = 0; i < numpoints; ++i)
		{
			points[i] = shape.getShapePath().point(i);
		}
		centerp = Point3D.average(points);
		center[0] = centerp.x();
		center[1] = centerp.y();
		center[2] = centerp.z();
		
		return center;
	}
	
	//assuming PMT is right at edge of PCAL
	//there is actually tens of centimeters of fibers between.
	public double[] getPMTLocation(String strip1, int paddle1){
		double[] center = new double[3];
		
		int uPaddle = -1;
		int vPaddle = -1;
		int wPaddle = -1;
		
        double[] xyz1 = new double[3];
        double[] xyz2 = new double[3];
        double[] xyz3 = new double[3];
        Point3D dist = new Point3D();
		
		
		if((strip1 == "u" || strip1 == "U"))
		{
			uPaddle = paddle1;
		}
		if((strip1 == "v" || strip1 == "V"))
		{
			vPaddle = paddle1;
		}
		if((strip1 == "w" || strip1 == "W"))
		{
			wPaddle = paddle1;
		}
		
		//case 1: U strip
		if(uPaddle != -1)
		{ 
			if(paddle1 != 0)
			{
				xyz1 = getShapeCenter(getOverlapShape(0, "u", paddle1, "w", 35)); //last strip
				xyz2 = getShapeCenter(getOverlapShape(0, "u", paddle1, "w", 34)); //second to last
			}
			else
			{
				xyz1 = getShapeCenter(getOverlapShape(0, "u", paddle1, "w", 35)); //last strip
				
				xyz2 = getShapeCenter(getOverlapShape(0, "u", 1, "w", 34)); //second to last
				xyz3 = getShapeCenter(getOverlapShape(0, "u", 1, "w", 35)); //second to last
				
				xyz2[0] = xyz1[0] + xyz2[0]-xyz3[0];
				xyz2[1] = xyz1[1] + xyz2[1]-xyz3[1];
				xyz2[2] = xyz1[2] + xyz2[2]-xyz3[2];
			}
			for(int i = 0; i < 3; ++i)
			{
				xyz1[i] = xyz1[i] + (xyz1[i] - xyz2[i])/2.0;
			}
			dist.set(xyz1[0],xyz1[1],xyz1[2]);		                
		}
		//case 2: V strip
		else if(vPaddle != -1)
		{ 
			if(paddle1 != 0)
			{
				xyz1 = getShapeCenter(getOverlapShape(0, "v", paddle1, "u", 35)); //last strip
				xyz2 = getShapeCenter(getOverlapShape(0, "v", paddle1, "u", 34)); //second to last
			}
			else
			{
				xyz1 = getShapeCenter(getOverlapShape(0, "v", paddle1, "u", 35)); //last strip
				
				xyz2 = getShapeCenter(getOverlapShape(0, "v", 1, "u", 34)); //second to last
				xyz3 = getShapeCenter(getOverlapShape(0, "v", 1, "u", 35)); //second to last
				
				xyz2[0] = xyz1[0] + xyz2[0]-xyz3[0];
				xyz2[1] = xyz1[1] + xyz2[1]-xyz3[1];
				xyz2[2] = xyz1[2] + xyz2[2]-xyz3[2];
			}
			for(int i = 0; i < 3; ++i)
			{
				xyz1[i] = xyz1[i] + (xyz1[i] - xyz2[i])/2.0;
			}
			dist.set(xyz1[0],xyz1[1],xyz1[2]);	
            
		}
		//case 3: W strip
		else if(wPaddle != -1)
		{ 
			if(paddle1 != 0)
			{
				xyz1 = getShapeCenter(getOverlapShape(0, "w", paddle1, "v", 35)); //last strip
				xyz2 = getShapeCenter(getOverlapShape(0, "w", paddle1, "v", 34)); //second to last
			}
			else
			{
				xyz1 = getShapeCenter(getOverlapShape(0, "w", paddle1, "v", 35)); //last strip
				
				xyz2 = getShapeCenter(getOverlapShape(0, "w", 1, "v", 34)); //second to last
				xyz3 = getShapeCenter(getOverlapShape(0, "w", 1, "v", 35)); //second to last
				
				xyz2[0] = xyz1[0] + xyz2[0]-xyz3[0];
				xyz2[1] = xyz1[1] + xyz2[1]-xyz3[1];
				xyz2[2] = xyz1[2] + xyz2[2]-xyz3[2];
			}
			for(int i = 0; i < 3; ++i)
			{
				xyz1[i] = xyz1[i] + (xyz1[i] - xyz2[i])/2.0;
			}
			dist.set(xyz1[0],xyz1[1],xyz1[2]);	

		}
		
		center[0] = dist.x();
		center[1] = dist.y();
		center[2] = 0.0;
		
		return center;
	}
	
	//get attenuation distance
	public double getUPixelDistance(int uPaddle, int vPaddle, int wPaddle){
		double distance = 0;
		double[] shapecenter = new double[3];
		double[] PMTloc = new double[3];
		
		
		shapecenter = getShapeCenter(getPixelShape(0,uPaddle,vPaddle,wPaddle));
		PMTloc = getPMTLocation("u", uPaddle);
		
		distance = Math.sqrt(Math.pow(shapecenter[0] - PMTloc[0],2) + Math.pow(shapecenter[1] - PMTloc[1],2));
		//distance = Math.sqrt(Math.pow(shapecenter[0] - PMTloc[0],2) + Math.pow(shapecenter[1] - PMTloc[1],2) + Math.pow(shapecenter[2] - PMTloc[2],2));
		
		return distance;
	}
	
	//get attenuation distance
	public double getVPixelDistance(int uPaddle, int vPaddle, int wPaddle){
		double distance = 0;
		double[] shapecenter = new double[3];
		double[] PMTloc = new double[3];
		
		
		shapecenter = getShapeCenter(getPixelShape(0,uPaddle,vPaddle,wPaddle));
		PMTloc = getPMTLocation("v", vPaddle);
		
		distance = Math.sqrt(Math.pow(shapecenter[0] - PMTloc[0],2) + Math.pow(shapecenter[1] - PMTloc[1],2));
		//distance = Math.sqrt(Math.pow(shapecenter[0] - PMTloc[0],2) + Math.pow(shapecenter[1] - PMTloc[1],2) + Math.pow(shapecenter[2] - PMTloc[2],2));
		
		return distance;
	}
	
	//get attenuation distance
	public double getWPixelDistance(int uPaddle, int vPaddle, int wPaddle){
		double distance = 0;
		double[] shapecenter = new double[3];
		double[] PMTloc = new double[3];
		
		
		shapecenter = getShapeCenter(getPixelShape(0,uPaddle,vPaddle,wPaddle));
		PMTloc = getPMTLocation("w", wPaddle);
		
		distance = Math.sqrt(Math.pow(shapecenter[0] - PMTloc[0],2) + Math.pow(shapecenter[1] - PMTloc[1],2));
		//distance = Math.sqrt(Math.pow(shapecenter[0] - PMTloc[0],2) + Math.pow(shapecenter[1] - PMTloc[1],2) + Math.pow(shapecenter[2] - PMTloc[2],2));
		
		return distance;
	}
	

	
	//get attenuation distance
	//                              main strip let,    main num, cross strip let, cross num
	//                                      "u"        0-67         "w"             0-61
	public double getOverlapDistance(String strip1, int paddle1, String strip2, int paddle2){
		double distance = 0;
		double[] shapecenter = new double[3];
		double[] PMTloc = new double[3];
		
		String s1 = "u";
		String s2 = "w";
		if(strip1.contains("u") || strip1.contains("U")) s1 = "u";
		if(strip1.contains("v") || strip1.contains("V")) s1 = "v";
		if(strip1.contains("w") || strip1.contains("W")) s1 = "w";
		if(strip2.contains("u") || strip2.contains("U")) s2 = "u";
		if(strip2.contains("v") || strip2.contains("V")) s2 = "v";
		if(strip2.contains("w") || strip2.contains("W")) s2 = "w";

		System.arraycopy( (double[])getShapeCenter(getOverlapShape(0, s1, paddle1, s2, paddle2)), 0, shapecenter, 0, 3);
		System.arraycopy( (double[])getPMTLocation(s1, paddle1), 0, PMTloc, 0, 3);
		
		distance = Math.sqrt(Math.pow(shapecenter[0] - PMTloc[0],2) + Math.pow(shapecenter[1] - PMTloc[1],2) + Math.pow(shapecenter[2] - PMTloc[2],2));
		
		return distance;
	}
	
	
	
  	//crossstrip needs to be 1-62 or 1-68 not 0
	//returns a strip number in element 0
	// and half the strip bin 0.5 for singles and 1 for doubles in element 1
    public double[] CalcDistinStrips(char stripletter, int crossstrip)
    {
  	  double x=0;
  	  double xE = 0.0;
        if(stripletter == 'u' || stripletter == 'U')
        {
            if(crossstrip <= 15)
            {
                //converts to 77 strips
                x = 2.0* crossstrip - 1.0;
                xE = 1.0;
            }
            else if(crossstrip > 15)
            {
                //converts to 77 strips
                x = (30.0 + (crossstrip - 15.0)) - 0.5;
                xE = 1.0/2.0;
            }
        }
        else if(stripletter == 'v' || stripletter == 'w' || stripletter == 'V' || stripletter == 'W')
        {
            if(crossstrip <= 52)
            {
                //converts to 84 strips
                x = crossstrip - 0.5;
                xE = 1.0/2.0;
                }
                else if(crossstrip > 52)
                {
                    //converts to 84 strips
                    x = (52.0 + 2.0*(crossstrip - 52.0)) - 1.0;
                    xE = 1.0;
                }
        }
        return new double[] {x, xE};
    }
	
    //xdistance needs to be 1-77 or 1-84 not 0
    //meant for use with the output of CalcDistinStrips
    public double[] CalcDistance(char stripletter, double xdistance, double xdistanceE)
    {
        
        if(stripletter == 'u' || stripletter == 'U')
        {
            //convert strip number to distance
            xdistance = Math.abs(xdistance - 77.0) * anglewidth;
            xdistanceE = xdistanceE * anglewidth;
        }
        else if(stripletter == 'v' || stripletter == 'w' || stripletter == 'V' || stripletter == 'W')
        {
            //convert strip number to distance
            xdistance = Math.abs(xdistance - 84.0) * anglewidth;
            xdistanceE = xdistanceE * anglewidth;
        }
        return new double[] {xdistance, xdistanceE};
    }
    
    
	public float uvw_dalitz(int ic, int ip, int il) {
		float uvw=0;
		switch (ic) {
		case 0: //PCAL
			if (il==1&&ip<=52) uvw=(float)ip/84;
			if (il==1&&ip>52)  uvw=(float)(52+(ip-52)*2)/84;
			if (il==2&&ip<=15) uvw=(float) 2*ip/77;
			if (il==2&&ip>15)  uvw=(float)(30+(ip-15))/77;
			if (il==3&&ip<=15) uvw=(float) 2*ip/77;
			if (il==3&&ip>15)  uvw=(float)(30+(ip-15))/77;
			break;
		case 1: //ECALinner
			uvw=(float)ip/36;
			break;
		case 2: //ECALouter
			uvw=(float)ip/36;
			break;
		}
		return uvw;
		
	}
	
	
	private void initVert()
	{
		ScintillatorPaddle paddle;
		ECLayer  ecLayer;
		int sector = 0;
		int suplay = 2; //PCAL ==0  
		int lastcomponent = 35;
		Point3D tranpoint = new Point3D();
		Point3D tranpointA = new Point3D();
		Point3D tranpointB = new Point3D();
		Point3D tranpointC = new Point3D();
		Point3D tranpointD = new Point3D();
		Point3D point1 = new Point3D();
		Point3D point2 = new Point3D();
		Point3D point3 = new Point3D();
        ECDetector detector  = (ECDetector) CLASGeometryLoader.createDetector(DetectorType.EC, 10, "default", "local");
        for(sector = 0; sector < 6; ++sector)
        {
		        //                                          PCAL ==0        u,v,w
		        ecLayer = detector.getSector(sector).getSuperlayer(suplay).getLayer(0);
		
		        //get point 1
		        paddle = ecLayer.getComponent(0); //strip num
		        point1.copy(paddle.getVolumePoint(0)); //one of 8
		        //if(sector == 0) System.out.println("x: " + point1.x() + " y: " + point1.y() + " z: " + point1.z());
		            	
		        //get point 2
		        paddle = ecLayer.getComponent(lastcomponent);
		        point2.copy(paddle.getVolumePoint(0));
		        if(sector == 0) System.out.println("x: " + detector.getSector(sector).getSuperlayer(suplay).getLayer(0).getComponent(0).getVolumePoint(0).x() + " y: " + detector.getSector(sector).getSuperlayer(suplay).getLayer(0).getComponent(0).getVolumePoint(0).y() + " z: " + detector.getSector(sector).getSuperlayer(suplay).getLayer(0).getComponent(0).getVolumePoint(0).z());
		        if(sector == 0) System.out.println("x: " + detector.getSector(sector).getSuperlayer(suplay).getLayer(1).getComponent(0).getVolumePoint(0).x() + " y: " + detector.getSector(sector).getSuperlayer(suplay).getLayer(1).getComponent(0).getVolumePoint(0).y() + " z: " + detector.getSector(sector).getSuperlayer(suplay).getLayer(1).getComponent(0).getVolumePoint(0).z());
		        if(sector == 0) System.out.println("x: " + detector.getSector(sector).getSuperlayer(suplay).getLayer(2).getComponent(0).getVolumePoint(0).x() + " y: " + detector.getSector(sector).getSuperlayer(suplay).getLayer(2).getComponent(0).getVolumePoint(0).y() + " z: " + detector.getSector(sector).getSuperlayer(suplay).getLayer(2).getComponent(0).getVolumePoint(0).z());
		        
		        //get point 3
		        paddle = ecLayer.getComponent((lastcomponent - 1)/2);
		        point3.copy(paddle.getVolumePoint(4));
		        //System.out.println("x: " + point3.x() + " y: " + point3.y() + " z: " + point3.z());
		
		        
		        //calculate plane from 3 points
		        //Point3D ab = new Point3D(point2);
		        //Point3D ac = new Point3D(point3);
		
		        //ab.translateXYZ(-point1.x(),-point1.y(),-point1.z());
		        //ac.translateXYZ(-point1.x(),-point1.y(),-point1.z());
		        
		        double a = (point2.y() - point1.y())*(point3.z() - point1.z()) - (point3.y() - point1.y())*(point2.z() - point1.z());
		        double b = (point2.z() - point1.z())*(point3.x() - point1.x()) - (point3.z() - point1.z())*(point2.x() - point1.x());
		        double c = (point2.x() - point1.x())*(point3.y() - point1.y()) - (point3.x() - point1.x())*(point2.y() - point1.y());
		        double d = -(a * point1.x() + b * point1.y() + c * point1.z());
		        
		        //System.out.println("a: " + a + " b: " + b + " c: " + c + " d: " + d);
		        
		        //find x rotation
            	if(-b/c > 0)
            	{
            		xrotation[sector] = -Math.atan(-b/c);
            	}
            	else
            	{
            		xrotation[sector] = Math.atan(Math.abs(-b/c));
            	}
            	
            	if(Math.abs(xrotation[sector]) < 0.0000001) xrotation[sector] = 0.0;
            	else if(Math.abs(xrotation[sector]) > Math.PI && xrotation[sector] < 0.0) xrotation[sector] += Math.PI;
            	else if(Math.abs(xrotation[sector]) > Math.PI && xrotation[sector] > 0.0) xrotation[sector] -= Math.PI;
            	//System.out.println("x rot: " + xrotation[sector]);
            	
            	//apply x rotation
		        point1.rotateX(xrotation[sector]);
		        point2.rotateX(xrotation[sector]);
		        point3.rotateX(xrotation[sector]);
		        
		        //calculate 2nd plane from the rotated points
		        //ab = new Point3D(point2);
		        //ac = new Point3D(point3);
		
		        //ab.translateXYZ(-point1.x(),-point1.y(),-point1.z());
		        //ac.translateXYZ(-point1.x(),-point1.y(),-point1.z());
		        
		        a = (point2.y() - point1.y())*(point3.z() - point1.z()) - (point3.y() - point1.y())*(point2.z() - point1.z());
		        b = (point2.z() - point1.z())*(point3.x() - point1.x()) - (point3.z() - point1.z())*(point2.x() - point1.x());
		        c = (point2.x() - point1.x())*(point3.y() - point1.y()) - (point3.x() - point1.x())*(point2.y() - point1.y());
		        d = -(a * point1.x() + b * point1.y() + c * point1.z());
		        
		        //System.out.println("a: " + a + " b: " + b + " c: " + c + " d: " + d);
		        
		        //find y rotation
		        if(-c/a > 0)
            	{
            		yrotation[sector] = Math.PI/2.0 - Math.atan(-c/a);
            	}
            	else
            	{
            		yrotation[sector] = Math.PI/2.0 - Math.atan(Math.abs(-c/a));
            		yrotation[sector] *= -1.000;
            	}
		        
		        if(Math.abs(yrotation[sector]) < 0.0000001) yrotation[sector] = 0.0;
            	else if(Math.abs(yrotation[sector]) > Math.PI && yrotation[sector] < 0.0) yrotation[sector] += Math.PI;
            	else if(Math.abs(yrotation[sector]) > Math.PI && yrotation[sector] > 0.0) yrotation[sector] -= Math.PI;
		        //System.out.println("y rot: " + yrotation[sector]);
		        
		       
			     //find translation coordinates
			     tranpointA.copy(detector.getSector(sector).getSuperlayer(suplay).getLayer(0).getComponent(0).getVolumePoint(0));
			     tranpointB.copy(detector.getSector(sector).getSuperlayer(suplay).getLayer(0).getComponent(0).getVolumePoint(4));
			     tranpointC.copy(detector.getSector(sector).getSuperlayer(suplay).getLayer(0).getComponent(0).getVolumePoint(1));
			     tranpointD.copy(detector.getSector(sector).getSuperlayer(suplay).getLayer(0).getComponent(0).getVolumePoint(5));
			     point1.set((tranpointA.x()+tranpointB.x()+tranpointC.x()+tranpointD.x())/4.0,(tranpointA.y()+tranpointB.y()+tranpointC.y()+tranpointD.y())/4.0,(tranpointA.z()+tranpointB.z()+tranpointC.z()+tranpointD.z())/4.0);
			   	 tranpoint.copy(point1);
	             point1.rotateX(xrotation[sector]);
	             point1.rotateY(yrotation[sector]);
	             
			     //tranpoint.set(detector.getSector(sector).getSuperlayer(0).getLayer(0).getComponent(0).getVolumePoint(0).x(),detector.getSector(sector).getSuperlayer(0).getLayer(0).getComponent(0).getVolumePoint(0).y(), 0.0);
			     tranpoint.set(tranpoint.x() - point1.x(), tranpoint.y() - point1.y(), tranpoint.z() - point1.z());
			     
			     //System.out.println("x: " + tranpointA.x() + " y: " + tranpointA.y() + " z: " + tranpointA.z());
			     //System.out.println("x: " + tranpointB.x() + " y: " + tranpointB.y() + " z: " + tranpointB.z());
			     //System.out.println("x: " + point1.x() + " y: " + point1.y() + " z: " + point1.z());
			     //System.out.println("x: " + tranpoint.x() + " y: " + tranpoint.y() + " z: " + tranpoint.z());
			     //System.out.println("   ");
		   //apply both rotations to all geometry points
		    for(int l = 0; l <3; l++)
		    {
	        	//	                                       PCAL ==0           u,v,w
	        	ecLayer = detector.getSector(sector).getSuperlayer(suplay).getLayer(l);
	            for(ScintillatorPaddle paddle2 : ecLayer.getAllComponents())
	            {													//0-67
	            	point1.copy(paddle2.getVolumePoint(0));
	            	point1.rotateX(xrotation[sector]);
	            	point1.rotateY(yrotation[sector]);
	            	point1.translateXYZ(tranpoint.x(), tranpoint.y(),0.0);
	            	//point1.rotateZ(Math.PI/2.0);
	            	//if(paddle2.getComponentId() == 35 && l == 0)System.out.println("x: " + point1.x() + " y: " + point1.y() + " z: " + point1.z());
	            	//System.out.println("Component ID: " + paddle2.getComponentId());
	            	xPoint[sector][l][paddle2.getComponentId()][0] = point1.x();
	            	yPoint[sector][l][paddle2.getComponentId()][0] = point1.y();
	            	
	            	point1.copy(paddle2.getVolumePoint(4));
	            	point1.rotateX(xrotation[sector]);
	            	point1.rotateY(yrotation[sector]);
	            	point1.translateXYZ(tranpoint.x(), tranpoint.y(),0.0);
	            	//point1.rotateZ(Math.PI/2.0);
	            	//if(paddle2.getComponentId() == 35 && l == 0)System.out.println("x: " + point1.x() + " y: " + point1.y() + " z: " + point1.z());
	            	//System.out.println("Component ID: " + paddle2.getComponentId());
	            	xPoint[sector][l][paddle2.getComponentId()][1] = point1.x();
	            	yPoint[sector][l][paddle2.getComponentId()][1] = point1.y();
	            	
	            	point1.copy(paddle2.getVolumePoint(5));
	            	point1.rotateX(xrotation[sector]);
	            	point1.rotateY(yrotation[sector]);
	            	point1.translateXYZ(tranpoint.x(), tranpoint.y(),0.0);
	            	//point1.rotateZ(Math.PI/2.0);
	            	//if(paddle2.getComponentId() == 35 && l == 0) System.out.println("x: " + point1.x() + " y: " + point1.y() + " z: " + point1.z());
	            	//System.out.println("Component ID: " + paddle2.getComponentId());
	            	xPoint[sector][l][paddle2.getComponentId()][2] = point1.x();
	            	yPoint[sector][l][paddle2.getComponentId()][2] = point1.y();
	            	
	            	point1.copy(paddle2.getVolumePoint(1));
	            	point1.rotateX(xrotation[sector]);
	            	point1.rotateY(yrotation[sector]);
	            	point1.translateXYZ(tranpoint.x(), tranpoint.y(),0.0);
	            	//point1.rotateZ(Math.PI/2.0);
	            	//if(paddle2.getComponentId() == 35 && l == 0) System.out.println("x: " + point1.x() + " y: " + point1.y() + " z: " + point1.z());
	            	//System.out.println("Component ID: " + paddle2.getComponentId());
	            	xPoint[sector][l][paddle2.getComponentId()][3] = point1.x();
	            	yPoint[sector][l][paddle2.getComponentId()][3] = point1.y();
	            	
	            	
	            }
	            System.out.println("     ");
	        }
        }
	}
	
	public Object[] getVerticies(DetectorShape2D shape1, DetectorShape2D shape2){
		int nPoints = 0;
		
		int vert1size = shape1.getShapePath().size();
		int vert2size = shape2.getShapePath().size();
		
		double[] x = new double[vert1size * vert2size];
		double[] y = new double[vert1size * vert2size];
		
		double[] xtemp1 = new double[vert1size];
		double[] ytemp1 = new double[vert1size];
		
		double[] xtemp2 = new double[vert2size];
		double[] ytemp2 = new double[vert2size];

		for(int i = 0; i < vert1size; ++i)
		{
			xtemp1[i] = shape1.getShapePath().point(i).x();
			ytemp1[i] = shape1.getShapePath().point(i).y();
		}
		
		
		for(int i = 0; i < vert2size; ++i)
		{
			xtemp2[i] = shape2.getShapePath().point(i).x();
			ytemp2[i] = shape2.getShapePath().point(i).y();
		}
		
		
		/////////////////////////////////////////////////////////////
		
		SimplePolygon2D pol1 = new SimplePolygon2D(xtemp1,ytemp1);
		SimplePolygon2D pol2 = new SimplePolygon2D(xtemp2,ytemp2);
		//System.out.println("area: " + Polygons2D.computeArea(pol1));

		Polygon2D pol3 = Polygons2D.intersection(pol1,pol2);
		
		
		nPoints = pol3.vertexNumber();
		for(int i = 0; i < pol3.vertexNumber(); ++i)
		{
			x[i] = pol3.vertex(i).getX();
			y[i] = pol3.vertex(i).getY();
			
			//System.out.println("x: " + pol3.vertex(i).getX() + " y: " + pol3.vertex(i).getY());
		}
		
		//if(nPoints > 2 && Polygons2D.computeArea(pol3) < 100.0)System.out.println("area: " + Polygons2D.computeArea(pol3));
		if(nPoints > 2 && Math.abs(Polygons2D.computeArea(pol3)) < 2.0)
		{
			nPoints = 0;
			//System.out.println("area: " + Polygons2D.computeArea(pol3));
		}
		//if(!pol3.contains(pol3.centroid())) nPoints = 0;
		
		/////////////////////////////////////////////////////////////////
		
		/////////////////////////////////////////////////////////////
		
	
		/*
		Path2D path1 = new Path2D.Double();
		path1.moveTo(xtemp1[0], ytemp1[0]);
		for(int i = 1; i < vert1size; ++i) {
		   path1.lineTo(xtemp1[i], ytemp1[i]);
		}
		path1.closePath();
		Shape pol1 = (Shape)path1;
		
		Path2D path2 = new Path2D.Double();
		path2.moveTo(xtemp2[0], ytemp2[0]);
		for(int i = 1; i < vert2size; ++i) {
		   path2.lineTo(xtemp2[i], ytemp2[i]);
		}
		path2.closePath();
		Shape pol2 = (Shape)path2;
		
		Area a1 = new Area(pol1);
		Area a2 = new Area(pol2);
		
		a1.intersect(a2);
		
		//a1.isPolygonal()
		nPoints = 0;
		double[] data = new double[6];
		if(a1.isEmpty())
			nPoints = 0;
		else
		{
			//pol1 = (Shape) a1;
			PathIterator pi = a1.getPathIterator(null);
			while(!pi.isDone())
			{
				if(pi.currentSegment(data) != pi.SEG_CLOSE)
				{
					//int type = pi.currentSegment(data);
					x[nPoints] = data[0];
					y[nPoints] = data[1];
					++nPoints;
				}
				pi.next();
			}
		}
		
		*/
		/////////////////////////////////////////////////////////////////
		/*
		int nPoints2 = 0;
		double[] x2 = new double[nPoints];
		double[] y2 = new double[nPoints];
		
		for(int i = 0; i < nPoints; ++i)
		{
			x2[nPoints2] = x[i];
			y2[nPoints2] = y[i];
			
			while(i != nPoints - 1 && Math.abs(x2[nPoints2] - x[i+1]) < 2.0 && Math.abs(y2[nPoints2] - y[i+1])< 2.0 && Math.sqrt(Math.pow(x2[nPoints2] - x[i+1],2) + Math.pow(y2[nPoints2] - y[i+1],2))< 2.0)
			{
				++i;
			}
			++nPoints2;

		}
		*/
		
		
		
		return(new Object[]{nPoints, x, y});
	}
	
	public static void main(String[] args){ 
		
		ECDrawDB pcaltest = new ECDrawDB();
		
		
		char stripLetter[] = {'u','v','w'};
		char stripLetter2[] = {'w','u','u'};
		String cstring1 = ""+stripLetter[0];//Character.toString(stripLetter[0]);
		String cstring2 = ""+stripLetter2[0];//Character.toString(stripLetter2[0]);
		int strip = 38;
		int crossStrip = 31;
		double x,y;
		
		//x: 360.9626941103118 y: 240.25231320773017 z: 625.7122498447577
		//x rot: 0.3838074126117121
		//y rot: -0.21291414121808772
		//Point3D testp = new Point3D(388.5459536110259, -192.47670631413644, 625.7122498447577);
		//testp.rotateX(0.3838074126117121);
		//testp.rotateY(-0.43633231299858166);
		//System.out.println("x: " + testp.x() + " y: " + testp.y() + " z: " + testp.z());
		
		
		//System.out.println("pad1: " + strip + " pad2: " + crossStrip);
		//double x = pcaltest.getOverlapDistance(cstring1,strip,cstring2,crossStrip);
		//System.out.println("x: " + x);
		
		
		//x = pcaltest.CalcDistinStrips('u',32)[0];
		//x = pcaltest.CalcDistance('u',x,0)[0];
		//System.out.println("x: " + x);
		
		
		EmbeddedCanvas canvas = new EmbeddedCanvas();
		
		DetectorShapeTabView  view   = new DetectorShapeTabView();
		
		
		//draw U strips
		
		/*
		DetectorShape2D shape = new DetectorShape2D();
	 	DetectorShapeView2D Umap= new DetectorShapeView2D("PCAL U");
	 	for(int sector = 0; sector < 6; sector++)
    	{
	 		for(int uPaddle = 0; uPaddle < 68; uPaddle++)
	 		{
	            shape = pcaltest.getStripShape(sector, "u", uPaddle);
	            for(int i = 0; i < shape.getShapePath().size(); ++i)
        		{
	            	
	            	//if(vPaddle == 0)System.out.println(shape.getShapePath().point(i).x());
	            	//if(vPaddle == 0)System.out.println(xPoint[sector][1][vPaddle][i]);
	            	
        			//if(vPaddle == 0)System.out.println(shape.getShapePath().point(i).y());
	            	
	            	x = shape.getShapePath().point(i).x();
	            	y = shape.getShapePath().point(i).y();
        			shape.getShapePath().point(i).set(x, y, 0.0);
        			//if(i == 0 && uPaddle == 67)System.out.println(shape.getShapePath().point(i).x());
        			
        		}
	            Umap.addShape(shape);


	 		}
    	}
	    view.addDetectorLayer(Umap);
		*/
		
		/*
		Object[] obj = pcaltest.getOverlapVerticies(2, "u", 67, "w", 42);
		int numpoints = (int)obj[0];
		double[] x = new double[numpoints];
		double[] y = new double[numpoints];
		System.arraycopy( (double[])obj[1], 0, x, 0, numpoints);
		System.arraycopy( (double[])obj[2], 0, y, 0, numpoints);
		System.out.println("numpoints: " + numpoints);
		*/
		
		
		//draw UW pane
		/*
	    DetectorShape2D shape = new DetectorShape2D();
	 	DetectorShapeView2D UWmap= new DetectorShapeView2D("PCAL UW");
	 	for(int sector = 0; sector < 1; sector++)
    	{
    	for(int uPaddle = 0; uPaddle < 68; uPaddle++)
    	{
    		for(int vPaddle = 0; vPaddle < 62; vPaddle++)
            {
	            //for(int wPaddle = 0; wPaddle < 62; wPaddle++)
	            //{
	            	if(pcaltest.isValidOverlap(sector, "u", uPaddle, "v", vPaddle))
	            	{
	            		
	            		System.out.println("u: " + uPaddle + " v: " + vPaddle);
	            		shape = pcaltest.getOverlapShape(sector, "u", uPaddle, "v", vPaddle);
	            		for(int i = 0; i < shape.getShapePath().size(); ++i)
        				{
	        				x = shape.getShapePath().point(i).x();// * 1000.0;
			            	y = shape.getShapePath().point(i).y();// * 1000.0;
			            	
			            	//if(sector == 0){ x += 302.0; y += 0.0;}
			            	//if(sector == 1){ x += 140.0; y += 260.0;}
			            	//if(sector == 2){ x += -140.0; y += 260.0;}
			            	//if(sector == 3){ x += -302.0; y += 0.0;}
			            	//if(sector == 4){ x += -140.0; y += -260.0;}
			            	//if(sector == 5){ x += 140.0; y += -260.0;}
			            	//x *= 1000.0;
			            	//y *= 1000.0;
			            	
        					shape.getShapePath().point(i).set(x, y, 0.0);
        					//if(i == 0 && vPaddle == 67 && wPaddle == 30)System.out.println(shape.getShapePath().point(i).x());
        				}
	            		UWmap.addShape(shape);
	            	//}
	            }
            }

    	}
    	}
	    view.addDetectorLayer(UWmap);
	    */
		
		
		
		//Draw pixels
		PrintWriter writer = null;
		try 
		{
			writer = new PrintWriter("uvwpix.dat");
		} 
		catch (FileNotFoundException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int num1, num2, num3;
		//double total;
		
		DetectorShape2D shape = new DetectorShape2D();
    	 	DetectorShapeView2D UWmap= new DetectorShapeView2D("PCAL Pixel");
    	 	for(int sector = 0; sector < 1; sector++)
	    	{
	    	for(int uPaddle = 0; uPaddle < 36; uPaddle++)
	    	{
	    		for(int vPaddle = 0; vPaddle < 36; vPaddle++)
	            {
		            for(int wPaddle = 0; wPaddle < 36; wPaddle++)
		            {
		            	//System.out.println("u: " + uPaddle + " v: " + vPaddle + " w: " + wPaddle);
		            	if(pcaltest.isValidPixel(sector, uPaddle, vPaddle, wPaddle))
		            	{
		            		//System.out.println("u: " + uPaddle + " v: " + vPaddle + " w: " + wPaddle);
		            		shape = pcaltest.getPixelShape(sector, uPaddle, vPaddle, wPaddle);
		            	//if(shape != null)
		            	//{
		            		
		            		num1 = uPaddle + 1;
		            		num2 = vPaddle + 1;
		            		num3 = wPaddle + 1;
		            		//total = pcaltest.getUPixelDistance(uPaddle, vPaddle, wPaddle) + pcaltest.getVPixelDistance(uPaddle, vPaddle, wPaddle) + pcaltest.getWPixelDistance(uPaddle, vPaddle, wPaddle);
		            		
		            		writer.println(num1  + "   " + num2 + "   " + num3 + "   " 
									+ pcaltest.getUPixelDistance(uPaddle, vPaddle, wPaddle) + "   " 
									+ pcaltest.getVPixelDistance(uPaddle, vPaddle, wPaddle) + "   "
									+ pcaltest.getWPixelDistance(uPaddle, vPaddle, wPaddle)); 
		            				//+ "   "	+ total);
		            		
		            		for(int i = 0; i < shape.getShapePath().size(); ++i)
	        				{
		            			x = shape.getShapePath().point(i).x();
				            	y = shape.getShapePath().point(i).y();
				            	if(uPaddle == 3 && vPaddle == 35 && wPaddle == 31) System.out.println("x: " + x + " y: " + y);
	        					shape.getShapePath().point(i).set(x, y, 0.0);
	        				}
		            		UWmap.addShape(shape);
		            	}
		            	//}
		            }
	            }

	    	}
	    	}
	    	view.addDetectorLayer(UWmap);
	    	
	    	writer.close();
	    	
	    	
	       // return UWmap;
	    	
	    	JFrame hi = new JFrame();
			hi.setLayout(new BorderLayout());
		    JSplitPane  splitPane = new JSplitPane();
		    splitPane.setLeftComponent(view);
		    splitPane.setRightComponent(canvas);
		    hi.add(splitPane,BorderLayout.CENTER);
		    hi.pack();
		    hi.setVisible(true);
	    	//canvas.add(view);
	    	//canvas1.draw(view);
	       // return UWmap;
    	 
	    	System.out.println("Done!");
	
	}
    
}
