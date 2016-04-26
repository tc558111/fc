package org.clas.fcmon.tools;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.clas.fcmon.tools.ECDrawDB;
import org.jlab.clas12.calib.DetectorShape2D;

public class ECPixels {
	
	ECDrawDB     pcaltest = new ECDrawDB();
	DetectorShape2D shape = new DetectorShape2D();
	PrintWriter    writer = null;

	public double ec_xpix[][][] = new double[15][1296][7];
	public double ec_ypix[][][] = new double[15][1296][7];
	public double ec_cthpix[]   = new double[1296];
	public int pixmap[][]       = new int[3][1296];	
	
	public ECPixels() {
		this.ecpixGet(1);
		this.ecpixrot();
		this.ecpixang();
		this.ecpixmap();
	}	
	
	public ECPixels(int opt) {
		this.ecpixGet(opt);
		this.ecpixrot();
		this.ecpixang();
		this.ecpixmap();
	}
	
	public void ecpixGet(int opt) {
		if (opt==1) ecpixdef(); //Calculate from combinatorics
		if (opt==2) ecpixDB();  //Calculate from Geometry DB and polygon overlap of strips
	}
	
	public void ecpixDB() {
		
		try 
		{
			writer = new PrintWriter("ecuvwpix.dat");
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
		
		for(int sector = 0; sector < 1; sector++)
    	{
			System.out.println("ecpixDB: Processing Sector "+sector);
    	for(int uPaddle = 0; uPaddle < 36; uPaddle++)
    	{
    		for(int vPaddle = 0; vPaddle < 36; vPaddle++)
            {
	            for(int wPaddle = 0; wPaddle < 36; wPaddle++)
	            {
	            	if(pcaltest.isValidPixel(sector, uPaddle, vPaddle, wPaddle))
	            	{
	            		shape = pcaltest.getPixelShape(sector, uPaddle, vPaddle, wPaddle);
	            		int num1 = uPaddle + 1;
	            		int num2 = vPaddle + 1;
	            		int num3 = wPaddle + 1;
	            		//total = pcaltest.getUPixelDistance(uPaddle, vPaddle, wPaddle) + pcaltest.getVPixelDistance(uPaddle, vPaddle, wPaddle) + pcaltest.getWPixelDistance(uPaddle, vPaddle, wPaddle);
	            		
	            		writer.println(num1  + "   " + num2 + "   " + num3 + "   " 
								+ pcaltest.getUPixelDistance(uPaddle, vPaddle, wPaddle) + "   " 
								+ pcaltest.getVPixelDistance(uPaddle, vPaddle, wPaddle) + "   "
								+ pcaltest.getWPixelDistance(uPaddle, vPaddle, wPaddle)); 
	            				//+ "   "	+ total);
	            		int pixel = pix(uPaddle+1,vPaddle+1,wPaddle+1);
	            		
	            		for(int i = 0; i < shape.getShapePath().size(); ++i)
        				{
	            			double x = shape.getShapePath().point(i).x();
	            			double y = shape.getShapePath().point(i).y();
	            			ec_xpix[i][pixel-1][6] = x; 
	            			ec_ypix[i][pixel-1][6] = y;
	            			System.out.println("i,u,v,w,pix,x,y= "+i+" "+uPaddle+" "+vPaddle+" "+wPaddle+" "+pixel+" "+x+" "+y);

//			            	if(uPaddle == 3 && vPaddle == 35 && wPaddle == 31) System.out.println("x: " + x + " y: " + y);
//        					shape.getShapePath().point(i).set(x, y, 0.0);
        				}
//	            		UWmap.addShape(shape);
	            	}
	            	//}
	            }
            }

    	}
    	}		
		
	}
	
	public void ecpixdef() {
	      System.out.println("ecpixdef():");
		       int jmax,pixel,m;
		       double xtmp,ytmp,tmp;
		       double   y_inc=10.0;
		       double   x_inc=5.31;
		       double[] xstrt={0.0, -5.31, 5.31};
		       double[] ystrt={0.0, -10.0,-10.0};
		       double[] xtrans={0.0,0.0,0.0};
		       double[] ytrans={0.0,0.0,0.0};
		       double[]  yflip={-20.0,0.0,0.0};
	       
		       for(int u=1; u<37; u++) {
		           jmax = 2*u-1;
		           pixel= u*(u-1)-u;
		           tmp=y_inc*(u-1);
		           ytrans[0]=tmp;
		           ytrans[1]=tmp;
		           ytrans[2]=tmp;
		           for (int j=1; j<jmax+1; j=j+2) {
		               m=-u+j;
		               pixel=pixel+2;
		               tmp=x_inc*m;
		               xtrans[0]=tmp;
		               xtrans[1]=tmp;
		               xtrans[2]=tmp;
		               for (int k=0;k<3;k++) {
		                   xtmp=(xstrt[k]+xtrans[k]);
		                   ytmp=(ystrt[k]-ytrans[k]);
		                   ec_xpix[k][pixel-1][6]=xtmp;
		                   ec_ypix[k][pixel-1][6]=ytmp;
		                   if (u!=36) {
		                       ytmp=ytmp+yflip[k];
		                       ec_xpix[k][pixel+2*u-1][6]=xtmp;
		                       ec_ypix[k][pixel+2*u-1][6]=ytmp;
		                   }     
		               }
		           }
		       }		       
		}
	
	    public void ecpixrot() {
			  System.out.println("ecpixrot():");
	    	
		       double[] theta={270.0,330.0,30.0,90.0,150.0,210.0};
	    	
		       for(int is=0; is<6; is++) {
		    	   double thet=theta[is]*3.14159/180.;
		    	   for (int ipix=0; ipix<1296; ipix++) {
		    		   for (int k=0;k<3;k++){
		    			   ec_xpix[k][ipix][is]= -(ec_xpix[k][ipix][6]*Math.cos(thet)+ec_ypix[k][ipix][6]*Math.sin(thet));
		    			   ec_ypix[k][ipix][is]=  -ec_xpix[k][ipix][6]*Math.sin(thet)+ec_ypix[k][ipix][6]*Math.cos(thet);
		    		   }
		    	   }
		       }	    	
	    }
		
		public void ecpixang() {
		  System.out.println("ecpixang():");
			double x,y,r,angle,r0=510.3;
			int jmax,m,pixel,sign;
			double[] off={3.453,6.907};
			
			pixel=0;
			
			for (int u=1;u<37;u++){
				jmax = 2*u-1;
				for (int j=1;j<jmax+1;j++){
					m = -u+j;
					pixel = pixel +1;
					sign=j%2;
					x = (18-u)*10.36-off[sign]+3.453;
					y = m*5.305;
					r = Math.sqrt(x*x+y*y);
					angle = Math.atan(r/r0);
					ec_cthpix[pixel-1]=1./Math.cos(angle);
				}
						
			}	
		}
		
		public void ecpixmap() {
	      System.out.println("ecpixmap():");
			int pixel;
			for (int u=1 ; u<37 ; u++){
				int jmax = 2*u-1;
				int v=36 ; int w=36-u+1 ; int uvw=73 ; int nj=0;
				for (int j=1; j<jmax+1 ; j++) {
					if (nj==2) {v--; nj=0;}
					w=uvw-u-v;
					pixel=pix(u,v,w);
					pixmap[0][pixel-1]=u;
					pixmap[1][pixel-1]=v;
					pixmap[2][pixel-1]=w;
					switch (uvw) {
					case 73: uvw=74;
					break;
					case 74: uvw=73;
					break;
					}
					nj++;
				}
			}
			
		}
		
		public double[] getpixels(int view, int strip, double[] in){
			int numpix,a,b,c,sum,pixel=1;
			numpix = 2*strip-1;
			a = strip;
			b = 37-a;
			c = 36;
			
			double[] out = new double[numpix];
			for (int j=0; j<numpix ; j++) {
				if (view==1) pixel=a*(a-1)+b-c+1;
				if (view==2) pixel=c*(c-1)+a-b+1;
				if (view==3) pixel=b*(b-1)+c-a+1;
				if (view==4) pixel=a*(a-1)+b-c+1;
				if (view==5) pixel=c*(c-1)+a-b+1;
				if (view==6) pixel=b*(b-1)+c-a+1;
				out[j] = in[pixel-1];
				sum = a+b+c;
				if(sum==73) b=b+1;
				if(sum==74) c=c-1;
			}
			return out;
			
		}
		public void putpixels(int view, int strip, int val, double[] in){
			int numpix,a,b,c,sum,pixel=1;
			numpix = 2*strip-1;
			a = strip;
			b = 37-a;
			c = 36;
			
			for (int j=0; j<numpix ; j++) {
				if (view==1) pixel=a*(a-1)+b-c+1;
				if (view==2) pixel=c*(c-1)+a-b+1;
				if (view==3) pixel=b*(b-1)+c-a+1;
				if (view==4) pixel=a*(a-1)+b-c+1;
				if (view==5) pixel=c*(c-1)+a-b+1;
				if (view==6) pixel=b*(b-1)+c-a+1;
				if (in[pixel-1]==0) in[pixel-1] = val;
				sum = a+b+c;
				if(sum==73) b=b+1;
				if(sum==74) c=c-1;
			}
			
		}	
		
		public int pix(int u, int v, int w) {
			return u*(u-1)+v-w+1;
			
		}
		
		public float uvw_dalitz(int ic, int il, int ip) {
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
		

}
