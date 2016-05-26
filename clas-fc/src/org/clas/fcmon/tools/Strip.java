package org.clas.fcmon.tools;

 
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jlab.clas.detector.DetectorCollection;
import org.root.histogram.H1D;
import org.root.histogram.H2D;

public class Strip {

	public  DetectorCollection<List<Integer>>  pixNmbr = null;
	public  DetectorCollection<List<Integer>>  pixDist = null;
    private DetectorCollection<H1D>           stripH1D = null;
    private DetectorCollection<H2D>           stripH2D = null;
    
    public Strip() {
    	this.pixNmbr   = new DetectorCollection<List<Integer>>();
    	this.pixDist   = new DetectorCollection<List<Integer>>();
    	this.stripH1D  = new DetectorCollection<H1D>();
    	this.stripH2D  = new DetectorCollection<H2D>();
    }
    
    public void addPixels(int sector, int layer, int component, int pixel) {
    	if (!pixNmbr.hasEntry(sector, layer, component)) pixNmbr.add(sector,layer,component,new ArrayList<Integer>());
    	this.pixNmbr.get(sector,layer,component).add(pixel);
    }
    
    public void addPixDist(int sector, int layer, int component, int dist) {
    	if (!pixDist.hasEntry(sector, layer, component)) pixDist.add(sector,layer,component,new ArrayList<Integer>());
    	this.pixDist.get(sector,layer,component).add(dist);
    }
    
    public void addH1D(int sector, int layer, int component, H1D h1) {
    	stripH1D.add(sector,layer,component,h1);   	
    }
    
    public void addH2D(int sector, int layer, int component, H2D h2) {
    	stripH2D.add(sector,layer,component,h2);   	
    }
    
    public int getpixel(int sector, int layer, int component, int index) {
    	return pixNmbr.get(sector,layer,component).get(index);
    }
    
    public void putpixels(int layer, int strip, int val, double[] in) {
    	Integer[] dum = this.getPixels(0,layer,strip); 
    	for (int j=0; j<dum.length; j++){
    		int pixel = dum[j];
    		if (in[pixel-1]==0) in[pixel-1] = val;
    	}
    } 
    
    public double[] getpixels(int layer, int strip, double[] in) {
    	Integer[] pixe = this.getPixels(0,layer,strip); 
    	double[] out = new double[pixe.length]; 
    	for (int j=0; j<pixe.length; j++)  out[j] = in[pixe[j]-1];
    	return out;
    }
   
    public Integer[] getPixels(int sector, int layer, int component) {
    	Integer list[] = new Integer[pixNmbr.get(sector,layer,component).size()];
    	return pixNmbr.get(sector,layer,component).toArray(list);
    }
    
    public void getSortedPixels(int sector, int layer, int component) {
    	List<Integer> pix1 = new ArrayList<Integer>(pixNmbr.get(sector,layer,component).size());
    	List<Integer> pix2 = new ArrayList<Integer>(pixDist.get(sector,layer,component).size());
    	Integer     list[] = new Integer[pixNmbr.get(sector,layer,component).size()];
    	pix1 = pixNmbr.get(sector,layer,component);
    	pix2 = pixDist.get(sector,layer,component);
    	concurrentSort(pix2,pix1);
    }
    
	public static <T extends Comparable<T>> void concurrentSort( final List<T> key, List<?>... lists){
        // Do validation
        if(key == null || lists == null)
        	throw new NullPointerException("key cannot be null.");
        
        for(List<?> list : lists)
        	if(list.size() != key.size())
        		throw new IllegalArgumentException("all lists must be the same size");
        
        // Lists are size 0 or 1, nothing to sort
        if(key.size() < 2)
        	return;
        
        // Create a List of indices
		List<Integer> indices = new ArrayList<Integer>();
		for(int i = 0; i < key.size(); i++)
			indices.add(i);

        // Sort the indices list based on the key
		Collections.sort(indices, new Comparator<Integer>(){
			@Override public int compare(Integer i, Integer j) {
				return key.get(i).compareTo(key.get(j));
			}
		});
		
		Map<Integer, Integer> swapMap = new HashMap<Integer, Integer>(indices.size());
		List<Integer> swapFrom = new ArrayList<Integer>(indices.size()),
					  swapTo   = new ArrayList<Integer>(indices.size());

        // create a mapping that allows sorting of the List by N swaps.
		for(int i = 0; i < key.size(); i++){
			int k = indices.get(i);
			while(i != k && swapMap.containsKey(k))
				k = swapMap.get(k);
			
			swapFrom.add(i);
			swapTo.add(k);
			swapMap.put(i, k);
		}
		
        // use the swap order to sort each list by swapping elements
		for(List<?> list : lists)
			for(int i = 0; i < list.size(); i++)
				Collections.swap(list, swapFrom.get(i), swapTo.get(i));
		}

}