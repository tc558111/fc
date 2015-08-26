package org.jlab.mon;
import java.util.ArrayList;
import org.jlab.evio.clas12.EvioDataBank;

public class ECALHit implements Comparable<ECALHit> {
	
	private int _sector;
	private int _strip;
	private int _stack;
	private int _ADC;
	private int _TDC;

	public ECALHit(int i, int sector, int strip, int stack, int ADC, int TDC) {
		this._sector = sector;
		this._strip  = strip ;
		this._stack  = stack;
		this._ADC    = ADC;
		this._TDC    = TDC;
	}
	
	public static ArrayList<ECALHit> getRawHits(EvioDataBank bank) {
		int[] sector = bank.getInt("sector");
		int[] strip  = bank.getInt("strip");
		int[] stack  = bank.getInt("stack");
		int[] ADC    = bank.getInt("ADC");
		int[] TDC    = bank.getInt("TDC");
		
		int size = sector.length;
		ArrayList<ECALHit> hits = new ArrayList<ECALHit>();
		
		for(int i = 0 ; i<size ; i++) {
			ECALHit hit = new ECALHit(i,sector[i],strip[i],stack[i],ADC[i],TDC[i]);
			hits.add(hit);
		}
		return hits;
	
	}

	public int compareTo(ECALHit o) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int get_sector() {
		return _sector;
	}

	public void set_sector(int _sector) {
		this._sector = _sector;
	}

	public int get_strip() {
		return _strip;
	}

	public void set_strip(int _strip) {
		this._strip = _strip;
	}

	public int get_stack() {
		return _stack;
	}

	public void set_stack(int _stack) {
		this._stack = _stack;
	}

	public int get_ADC() {
		return _ADC;
	}

	public void set_ADC(int _ADC) {
		this._ADC = _ADC;
	}

	public int get_TDC() {
		return _TDC;
	}

	public void set_TDC(int _TDC) {
		this._TDC = _TDC;
	}

}
