package org.clas.fcmon.tools;

public class CCPixels {
	
	public double cc_xpix[][][] = new double[4][36][7];
	public double cc_ypix[][][] = new double[4][36][7];
	
	public CCPixels() {
		this.ccpixdef();
		this.ccpixrot();
	}
	
	public void ccpixdef() {
	      System.out.println("ccpixdef():");
		  
		       double   k;
		       double   y_inc=19.0;
		       double   x_inc=0;

		       double[] ccgeom={ 
		    		   65.018,
		    		   77.891,
		    		   90.532,
		    		   102.924,
		    		   115.056,
		    		   126.914,
		    		   138.487,
		    		   149.764,
		    		   160.734,
		    		   171.388,
		    		   183.967,
		    		   196.047,
		    		   209.663,
		    		   222.546,
		    		   234.684,
		    		   246.064,
		    		   256.680,
		    		   266.527};
		       
		       for(int i=0 ; i<18 ; i++){
		    	   x_inc = 0.7*ccgeom[i];
		    	   cc_xpix[0][18+i][6]=-x_inc;
		    	   cc_xpix[1][18+i][6]=0.;
		    	   cc_xpix[2][18+i][6]=0.;
		    	   cc_xpix[3][18+i][6]=-x_inc;
		    	   k = -i*y_inc-100.;	    	   
		    	   cc_ypix[0][18+i][6]=k;
		    	   cc_ypix[1][18+i][6]=k;
		    	   cc_ypix[2][18+i][6]=k-y_inc;
		    	   cc_ypix[3][18+i][6]=k-y_inc;
		       }
		       for(int i=0 ; i<18 ; i++){
		    	   x_inc = 0.7*ccgeom[i];
		    	   cc_xpix[0][i][6]=0.;
		    	   cc_xpix[1][i][6]=x_inc;
		    	   cc_xpix[2][i][6]=x_inc;
		    	   cc_xpix[3][i][6]=0.;
		    	   k = -i*y_inc-100.;	    	   
		    	   cc_ypix[0][i][6]=k;
		    	   cc_ypix[1][i][6]=k;
		    	   cc_ypix[2][i][6]=k-y_inc;
		    	   cc_ypix[3][i][6]=k-y_inc;
		       }
	}
		       
    public void ccpixrot() {
		  System.out.println("ccpixrot():");
    	
	       double[] theta={270.0,330.0,30.0,90.0,150.0,210.0};
    	
	       for(int is=0; is<6; is++) {
	    	   double thet=theta[is]*3.14159/180.;
	    	   for (int ipix=0; ipix<36; ipix++) {
	    		   for (int k=0;k<4;k++){
	    			   cc_xpix[k][ipix][is]= -(cc_xpix[k][ipix][6]*Math.cos(thet)+cc_ypix[k][ipix][6]*Math.sin(thet));
	    			   cc_ypix[k][ipix][is]=  -cc_xpix[k][ipix][6]*Math.sin(thet)+cc_ypix[k][ipix][6]*Math.cos(thet);
	    		   }
	    	   }
	       }	    	
    }

}
