#include <iostream>
#include <fstream>
#include <string.h>
#include "TH1F.h"
#include "TH2F.h"
#include "TH3F.h"
#include "TFile.h"
#include "TTree.h"
//#include "TSpectrum.h"
#include "TDirectory.h"
#include "TObject.h"
#include <math.h>
#include "fitU.c"
#include "fitV.c"
#include "fitW.c"

#define nPc 192
#define USIZE 68
#define VSIZE 62
#define WSIZE 62

using namespace std;

void fitU(int Pass);
void fitV(int Pass);
void import(int bin[USIZE][VSIZE][WSIZE]);
void importgaus(double ucent[USIZE][WSIZE],double usig[USIZE][WSIZE],double vcent[VSIZE][USIZE],double vsig[VSIZE][USIZE],double wcent[WSIZE][USIZE],double wsig[WSIZE][USIZE]);
bool withingaus(double Ucent,double Usig,double Vcent,double Vsig,double Wcent,double Wsig, double adU, double adV, double adW,int rsU,int rsV,int rsW);
bool withingaus2(double Cent,double Sig, double Adc, int Strip, int Hit);
bool withingaus3(double Cent,double Sig, double Adc, int Strip, int Hit);
void GetFiberULengths(double array1[], double array2[], double array3[]);
void GetFiberVLengths(double array1[], double array2[], double array3[]);
void GetFiberWLengths(double array1[], double array2[], double array3[]);
Double_t uvw_dist(Int_t strip, Int_t layer);
void importatten(double u0[],double uA[],double uB[],double v0[],double vA[],double vB[],double w0[],double wA[],double wB[]);
void importgains(double ugain[], double vgain[], double wgain[]);
void combinegains(const double ugaini[], const double vgaini[], const double wgaini[], const double ugainf[], const double vgainf[], const double wgainf[]);
void adjustcoefficients();

void Rawhistos(const double ugaini[], const double vgaini[], const double wgaini[], const int Pass = 0)
{

    string filetype;
    string folder = "pass";
    folder = folder + (char)(Pass + 48);

    if(Pass == 0)
    {
        filetype = "recreate";
    }
    else
    {
        filetype = "update";
    }
    
    TFile outFile("Histo.root",filetype.c_str());
    outFile.mkdir(folder.c_str());
    outFile.cd(folder.c_str());
    gDirectory->mkdir("Setup");
    gDirectory->cd("Setup");
    
	//program variables
	Int_t i, j;
    Int_t percent = 0;
    Int_t oldpercent = 0;

	Int_t good_u, good_v, good_w;
	Int_t good_uv, good_uw, good_vw;
	Int_t good_uwt, good_wut, good_vwt;
	Int_t good_uwtt, good_wutt, good_vwtt;
	Int_t good_uwtc, good_uwttb, good_uwttc;
	Int_t good_uvw;
	Int_t good;
	
	Int_t is, il, inh, nh[4][6];
	Double_t adc, adccal[4][69], tdc, tdcpc[193], pcped[69][4][7], thresh, uvw;
    Double_t tdcsum;
	Double_t strr[69][4][7], adcr[69][4][7],tdcr[69][4][7];

	Int_t rsu, rsv, rsw, adu, adv, adw, tdu, tdv, tdw;
	Int_t bin[USIZE][VSIZE][WSIZE];
    Double_t ucent[USIZE][WSIZE], usig[USIZE][WSIZE];
    Double_t vcent[VSIZE][USIZE], vsig[VSIZE][USIZE];
    Double_t wcent[WSIZE][USIZE], wsig[WSIZE][USIZE];

    //variable used in modified versions
    Double_t udist = 0.0;
    Double_t vwdist = 0.0;
    Double_t vdist = 0.0;
    Double_t wdist = 0.0;
    Double_t Iuv,Iuw,Ivw,Imax;
    Double_t Iu,Iv,Iw, Isum, Isum1, Isum2, Imin;
    Double_t fibU[68],fibV[62],fibW[62];
    Double_t p0U[68],p0V[62],p0W[62];
    Double_t p1U[68],p1V[62],p1W[62];
    Double_t width = 5.055;
    Int_t pass;
    Int_t select;
    Int_t ueff, veff, weff;

    //light attenuation variables
    Double_t u0[USIZE], uA[USIZE], uB[USIZE];
    Double_t v0[VSIZE], vA[VSIZE], vB[VSIZE];
    Double_t w0[WSIZE], wA[WSIZE], wB[WSIZE];

    //Gains
    double ugain[USIZE];
    double vgain[VSIZE];
    double wgain[WSIZE];
    
	// variables controling entry loop
	Int_t nentries, e, exists;

	//Branch variables
	Int_t npc,dt1,dt2;
	UChar_t secpc[nPc],layerpc[nPc],strippc[nPc];
	Int_t Tdcpc[nPc];
	UShort_t Adcpc[nPc];

	//Histograms
	TH1F *HDalitz = new TH1F("Dalitz","Distance Calculation",600,1.5,2.5);
	TH1F *HDalitzMCut = new TH1F("DalitzMCut","Distance Calculation With Multiplicity",600,1.5,2.5);
	TH3F *Hadcstrip = new TH3F("AdcValues","x = ADC, y = strip, z = layer",1000,0.0,1000,68,1.0,69.0,3,1.0,4.0);
	TH3F *Htdcstrip = new TH3F("TdcValues","x = TDC * 4, y = strip, z = layer",500,0.0,0.2,68,1.0,69.0,3,1.0,4.0);

    TH1F *Htdcsum = new TH1F("SumTdcValues","x = TDC * 4",200,6000,16000);

	TH2F *HnumhitsUV = new TH2F("numhitsUV","x = u-strip, y = v-strip",68,1.0,69.0,62,1.0,63.0);
	TH2F *HnumhitsUVcomp = new TH2F("numhitsUVcomp","x = u-strip, y = v-strip",68,1.0,69.0,62,1.0,63.0);
	TH2F *HnumhitsUVadu = new TH2F("numhitsUVadu","x = u-strip, y = v-strip",68,1.0,69.0,62,1.0,63.0);

	TH2F *HnumhitsUW = new TH2F("numhitsUW","x = u-strip, y = w-strip",68,1.0,69.0,62,1.0,63.0);
	TH2F *HnumhitsUWadu = new TH2F("numhitsUWadu","x = u-strip, y = w-strip",68,1.0,69.0,62,1.0,63.0);

	TH2F *HnumhitsVW = new TH2F("numhitsVW","x = v-strip, y = w-strip",62,1.0,63.0,62,1.0,63.0);
	TH2F *HnumhitsVWadv = new TH2F("numhitsVWadv","x = v-strip, y = w-strip",62,1.0,63.0,62,1.0,63.0);

	TH2F *HnumhitsWV = new TH2F("numhitsWV","x = w-strip, y = v-strip",62,1.0,63.0,62,1.0,63.0);
	TH2F *HnumhitsWVadw = new TH2F("numhitsWVadw","x = w-strip, y = v-strip",62,1.0,63.0,62,1.0,63.0);

	TH2F *HaduU = new TH2F("HaduU","x = adu, y = u-strip",500,0.0,1000,68,1.0,69.0);
	TH2F *HadvV = new TH2F("HadvV","x = adv, y = v-strip",500,0.0,1000,62,1.0,63.0);
	TH2F *HadwW = new TH2F("HadwW","x = adw, y = w-strip",500,0.0,1000,62,1.0,63.0);

	TH2F *HaduUMCut = new TH2F("HaduUMCut","x = adu, y = u-strip",500,0.0,1000,68,1.0,69.0);
	TH2F *HadvVMCut = new TH2F("HadvVMCut","x = adv, y = v-strip",500,0.0,1000,62,1.0,63.0);
	TH2F *HadwWMCut = new TH2F("HadwWMCut","x = adw, y = w-strip",500,0.0,1000,62,1.0,63.0);

	TH2F *HaduUMTCut = new TH2F("HaduUMTCut","x = adu, y = u-strip",500,0.0,1000,68,1.0,69.0);
	TH2F *HadvVMTCut = new TH2F("HadvVMTCut","x = adv, y = v-strip",500,0.0,1000,62,1.0,63.0);
	TH2F *HadwWMTCut = new TH2F("HadwWMTCut","x = adw, y = w-strip",500,0.0,1000,62,1.0,63.0);

	TH2F *HADUvsRSV = new TH2F("HADUvsRSV","x = v-strip, y = adu",62,1.0,63.0,500,0.0,1000);
	TH2F *HADVvsRSU = new TH2F("HADVvsRSU","x = u-strip, y = adv",68,1.0,69.0,500,0.0,1000);
	TH2F *HADUvsRSW = new TH2F("HADUvsRSW","x = w-strip, y = adu",62,1.0,63.0,500,0.0,1000);
	TH2F *HADWvsRSU = new TH2F("HADWvsRSU","x = u-strip, y = adw",68,1.0,69.0,500,0.0,1000);
	TH2F *HADVvsRSW = new TH2F("HADVvsRSW","x = w-strip, y = adv",62,1.0,63.0,500,0.0,1000);
	TH2F *HADWvsRSV = new TH2F("HADWvsRSV","x = v-strip, y = adw",62,1.0,63.0,500,0.0,1000);


	TH2F *HADUvsRSVu10 = new TH2F("HADUvsRSVu10","x = v-strip, y = adu",62,1.0,63.0,500,0.0,1000);
	TH2F *HADUvsRSVu40 = new TH2F("HADUvsRSVu40","x = v-strip, y = adu",62,1.0,63.0,500,0.0,1000);
	TH2F *HADUvsRSVu60 = new TH2F("HADUvsRSVu60","x = v-strip, y = adu",62,1.0,63.0,500,0.0,1000);
	TH1F *HADVB = new TH1F("HADVB","adv at ustrip=60 and adu < 200",500,0.0,1000);
	TH1F *HADVS = new TH1F("HADVS","adv at ustrip=60 and adu > 200",500,0.0,1000);
    
	TH3F *HADUvsRSVustrips = new TH3F("HADUvsRSVustrips","x = v-strip, y = adu, z = u-strip",62,0.5,62.5,300,0.0,1000,68,0.5,68.5);
	TH3F *HADUvsRSVustripsB = new TH3F("HADUvsRSVustripsB","x = v-strip, y = adu, z = u-strip",62,0.5,62.5,300,0.0,1000,68,0.5,68.5);


	TH3F *HADUvsRSWustrips = new TH3F("HADUvsRSWustrips","x = w-strip, y = adu, z = u-strip",62,0.5,62.5,300,0.0,1000,68,0.5,68.5);
	TH3F *HADUvsRSWustripsB = new TH3F("HADUvsRSWustripsB","x = w-strip, y = adu, z = u-strip",62,0.5,62.5,300,0.0,1000,68,0.5,68.5);
    
    //u strip calibration
	TH3F *HADUvsRSWustrips200 = new TH3F("HADUvsRSWustrips200","x = w-strip, y = adu, z = u-strip",62,0.5,62.5,125,0.0,1000,68,0.5,68.5);
	TH3F *HADUvsRSWustripsB200 = new TH3F("HADUvsRSWustripsB200","x = w-strip, y = adu, z = u-strip",62,0.5,62.5,200,0.0,1000,68,0.5,68.5);


    //v strip calibration
	TH3F *HADVvsRSUvstrips200 = new TH3F("HADVvsRSUvstrips200","x = u-strip, y = adv, z = v-strip",68,0.5,68.5,125,0.0,1000,62,0.5,62.5);
	TH3F *HADVvsRSUvstripsB200 = new TH3F("HADVvsRSUvstripsB200","x = u-strip, y = adv, z = v-strip",68,0.5,68.5,200,0.0,1000,62,0.5,62.5);


	TH3F *HADVvsRSWvstrips200 = new TH3F("HADVvsRSWvstrips200","x = w-strip, y = adv, z = v-strip",62,0.5,62.5,200,0.0,1000,62,0.5,62.5);
	TH3F *HADVvsRSWvstripsB200 = new TH3F("HADVvsRSWvstripsB200","x = w-strip, y = adv, z = v-strip",62,0.5,62.5,200,0.0,1000,62,0.5,62.5);

    //w strip calibration
	TH3F *HADWvsRSUwstrips200 = new TH3F("HADWvsRSUwstrips200","x = u-strip, y = adw, z = w-strip",68,0.5,68.5,125,0.0,1000,62,0.5,62.5);
	TH3F *HADWvsRSUwstripsB200 = new TH3F("HADWvsRSUwstripsB200","x = u-strip, y = adw, z = w-strip",68,0.5,68.5,200,0.0,1000,62,0.5,62.5);


	TH3F *HADWvsRSVwstrips200 = new TH3F("HADWvsRSVwstrips200","x = v-strip, y = adw, z = w-strip",62,0.5,62.5,200,0.0,1000,62,0.5,62.5);
	TH3F *HADWvsRSVwstripsB200 = new TH3F("HAWUvsRSVwstripsB200","x = v-strip, y = adw, z = w-strip",62,0.5,62.5,200,0.0,1000,62,0.5,62.5);

    //new test
    TH1F *IntSum1 = new TH1F("IntSum1", "Sum of initial intensities",800,0.0,3000);
    TH1F *IntSum2 = new TH1F("IntSum2", "Sum of initial intensities",800,0.0,3000);
    TH1F *IntUCut = new TH1F("IntUCut", "Initial intensities",800,0.0,3000);
    TH1F *IntVCut = new TH1F("IntVCut", "Initial intensities",800,0.0,3000);
    TH1F *IntWCut = new TH1F("IntWCut", "Initial intensities",800,0.0,3000);
    TH1F *IntSumCut1A = new TH1F("IntSumCut1A", "Sum of initial intensities",800,0.0,3000);
    TH1F *IntSumCut1 = new TH1F("IntSumCut1", "Sum of initial intensities",800,0.0,3000);
    TH1F *IntSumCut2 = new TH1F("IntSumCut2", "Sum of initial intensities",800,0.0,3000);
    TH2F *DiffIntensity = new TH2F("diffI", "x = variables, y = differences",4,0,4,2000,-1000,1000);
    TH3F *HADUvsRSWustripsTEST = new TH3F("HADUvsRSWustripsTEST","x = w-strip, y = adu, z = u-strip",62,0.5,62.5,250,0.0,2000.0,68,0.5,68.5);
    TH3F *HADVvsRSUvstripsTEST = new TH3F("HADVvsRSUvstripsTEST","x = u-strip, y = adv, z = v-strip",68,0.5,68.5,250,0.0,2000.0,62,0.5,62.5);
    TH3F *HADWvsRSUwstripsTEST = new TH3F("HADWvsRSUwstripsTEST","x = u-strip, y = adw, z = w-strip",68,0.5,68.5,250,0.0,2000.0,62,0.5,62.5);


    TH3F *HADUvsRSWustripsTEST50 = new TH3F("HADUvsRSWustripsTEST50","x = w-strip, y = adu, z = u-strip",62,0.5,62.5,100,0.0,1000.0,68,0.5,68.5);
    TH3F *HADUvsRSWustripsTEST100 = new TH3F("HADUvsRSWustripsTEST100","x = w-strip, y = adu, z = u-strip",62,0.5,62.5,150,0.0,1000.0,68,0.5,68.5);
    TH3F *HADUvsRSWustripsTEST150 = new TH3F("HADUvsRSWustripsTEST150","x = w-strip, y = adu, z = u-strip",62,0.5,62.5,175,0.0,1000.0,68,0.5,68.5);
    TH3F *HADUvsRSWustripsTEST200 = new TH3F("HADUvsRSWustripsTEST200","x = w-strip, y = adu, z = u-strip",62,0.5,62.5,200,0.0,1000.0,68,0.5,68.5);
    TH3F *HADUvsRSWustripsTEST250 = new TH3F("HADUvsRSWustripsTEST250","x = w-strip, y = adu, z = u-strip",62,0.5,62.5,225,0.0,1000.0,68,0.5,68.5);
    
/////////////////////// Main Program //////////////////////////////////////////// 

	//Intake pcped values from .vec file
	ifstream pedfile;
    pedfile.open ("pcped.vec");

	for(j = 1; j <=3; ++j)
	{
		for(i = 1; i <=68; ++i)
		{
			pedfile >> pcped[i][j][1];
		}
	}
	pedfile.close();

	//takes values in from file
	//these values indicate an exclusive hit
	import(bin);

    //Import gaussian features
    importgaus(ucent,usig,vcent,vsig,wcent,wsig);

    //Import Gains
    importgains(ugain, vgain, wgain);

    //Import Light attenuation features
    //This is fit to I(x)=I_0 exp(A * x) + B
    //Convert by
    //   adc =
    if(Pass > 0)
    {
        importatten(u0, uA, uB, v0, vA, vB, w0, wA, wB);
    } 
    
    //Import Fiber Lengths
    GetFiberULengths(fibU,p0U,p1U);  
    GetFiberVLengths(fibV,p0V,p1V);
    GetFiberWLengths(fibW,p0W,p1W);
    
	//open data file
	TFile *f = new TFile("pcal_4416.root");

	//exists = f->Contains("h10;4");
	exists = 1;
	if(exists)
	{
		TTree *p1 = (TTree*)f->Get("h10;4");
		p1->SetBranchAddress("npc", &npc);
		p1->SetBranchAddress("secpc", secpc); //6 sectors total data is eust from one
		p1->SetBranchAddress("layerpc", layerpc); //u,v,w  (1,3)
		p1->SetBranchAddress("strippc", strippc); // strip number (1,68)
		p1->SetBranchAddress("Tdcpc", Tdcpc);
		p1->SetBranchAddress("Adcpc", Adcpc);
		p1->SetBranchAddress("Dt1",&dt1);
		p1->SetBranchAddress("Dt2",&dt2);

        nentries = (Int_t)p1->GetEntries();
        for (e=0;e<nentries;e++) 
        { 
			p1->GetEntry(e);	//Loop over entries
			
//initialize arrays
			for(i = 0; i <=3; ++i)           //                u,v,w   1-6
			{                                //                 1-3    1-6
				nh[i][1] = 0;                //number of hits [layer][sector]
				for(j = 0; j <= 68; ++j)
				{
					strr[j][i][1] = 0;   //strip [num hits][layer][sector]	
					adcr[j][i][1] = 0;    //adc [num hits][layer][sector]
                    adccal[i][j] = 1.0;
				}	
				
			}
			uvw = 0.0;
			thresh = 0.0;
            tdcsum = 0.0;

//initial gains
            for(j = 1; j <= 68; ++j)
            {
                adccal[1][j] = ugaini[j-1]; //u
            }
            for(j = 1; j <= 62; ++j)
            {
                adccal[2][j] = vgaini[j-1]; //v
            }
            for(j = 1; j <= 62; ++j)
            {
                adccal[3][j] = wgaini[j-1]; //w
            }	
			

//Subtract ADC pedestals, test threshold, get hits, fill arrays
			for(i = 0; i <npc; ++i)                               //loop over all PMTs fired
			{
				is = (Int_t)strippc[i];                           // strip num of current PMT, 1-68
				il = (Int_t)layerpc[i];                           // layer num of current PMT, 1-3
                
				adc = adccal[il][is] * (Adcpc[i] - pcped[is][il][1]);  //integral of pulse with adjustment
				tdc = Tdcpc[i];
				if(adc > thresh)                             //if energy is greater than the threshold
				{
					nh[il][1] = nh[il][1] + 1;              //add one valid hit to num hits
					inh = nh[il][1];                        //make each hit have a unique id
					adcr[inh][il][1] = adc;		            //energy recorded by PMT
					tdcr[inh][il][1] = tdc * 4;             //times 4?				
					strr[inh][il][1] = is;   		        //strip number associated with that PMT
	
					//histo results for each PMT
					Hadcstrip->Fill(adc,(Float_t)is,(Float_t)il);	    //adc vs strip vs layer
					// https://clasweb.jlab.org/wiki/images/9/92/Pcal-mip-u.gif
					Htdcstrip->Fill(tdc,(Float_t)is,(Float_t)il);	    //tdc vs strip vs layer
					// ???

										
				}
				uvw = uvw + uvw_dist(is,il);  //add distances
                tdcsum += tdc;	
			}
            Htdcsum->Fill(tdcsum);
				
			//histo distances to see distribution
			HDalitz->Fill(uvw); 
			 //  https://clasweb.jlab.org/wiki/index.php/File:Uvw.gif

		
//Multiplicity tests
			good_u = (nh[1][1] == 1);
			good_v = (nh[2][1] == 1);
			good_w = (nh[3][1] == 1);

//Store strip numbers and ADC values for events with good multiplicity
			if(good_u) rsu = strr[1][1][1];
			if(good_v) rsv = strr[1][2][1];
			if(good_w) rsw = strr[1][3][1];
			if(good_u) adu = adcr[1][1][1];
			if(good_v) adv = adcr[1][2][1];
			if(good_w) adw = adcr[1][3][1];
			if(good_u) tdu = tdcr[1][1][1];
			if(good_v) tdv = tdcr[1][2][1];
			if(good_w) tdw = tdcr[1][3][1];
			
			good_uv = (good_u && good_v);
			good_uw = (good_u && good_w);
			good_vw = (good_v && good_w);

			good_uvw = (good_u && good_v && good_w);

//Next Longest Strip
			good_uwt = (good_uw && (rsw == 61));
			good_wut = (good_uw && (rsu == 67));
			good_vwt = (good_uv && (rsu == 67));

//Next Longest Strip trigger threshold (default = 70)
			good_uwtt = (good_uwt && adw > 70.0);
			good_wutt = (good_wut && adu > 70.0);
			good_vwtt = (good_vwt && adu > 70.0);	

//Histogram Dalitz calculation after multiplicity cuts
			if(good_uvw)
			{
				HDalitzMCut->Fill(uvw);

//Histo U vs V, U vs W, V vs W (used for detector map)

				for(i = 1; i <= nh[1][1]; ++i)   // loop over hits in u layer
				{
					for(j = 1; j <= nh[2][1]; ++j) // loop over hits in v layer
					{
						if(bin[(int)strr[i][1][1]-1][(int)strr[j][2][1]-1][(int)strr[1][3][1]-1] == 1)
						HnumhitsUVcomp->Fill((Float_t)strr[i][1][1],(Float_t)strr[j][2][1]);
						HnumhitsUV->Fill((Float_t)strr[i][1][1],(Float_t)strr[j][2][1]);
						// UV strip histo
						// https://clasweb.jlab.org/wiki/index.php/File:Pcal-evnt-uv-grid.gif
						HnumhitsUVadu->Fill((Float_t)strr[i][1][1],(Float_t)strr[j][2][1], adu);
						// UV strip histo weighted
					}
					for(j = 1; j <= nh[3][1]; ++j) // loop over hits in w layer
					{
						HnumhitsUW->Fill((Float_t)strr[i][1][1],(Float_t)strr[j][3][1]);
						// UW strip histo
						HnumhitsUWadu->Fill((Float_t)strr[i][1][1],(Float_t)strr[j][3][1], adu);
						// UW strip histo weighted
					}
				}

				for(i = 1; i <= nh[2][1]; ++i) // loop over hits in v layer
				{
					for(j = 1; j <= nh[3][1]; ++j) // loop over hits in w layer
					{
						HnumhitsVW->Fill((Float_t)strr[i][2][1],(Float_t)strr[j][3][1]);
						// VW strip histo
						HnumhitsVWadv->Fill((Float_t)strr[i][2][1],(Float_t)strr[j][3][1], adv);
						// VW strip histo weighted

						HnumhitsWV->Fill((Float_t)strr[j][3][1],(Float_t)strr[i][2][1]);
						// WV strip histo
						HnumhitsWVadw->Fill((Float_t)strr[j][3][1],(Float_t)strr[i][2][1], adw);
						// WV strip histo weighted
					}
				}


//Histo Plots using next longest strip trigger.
//These should have MIP peaks visible and will be fitted to obtain PMT gains.
				if(good_uwt) HaduU->Fill(adu,rsu);
				if(good_vwt) HadvV->Fill(adv,rsv);
				if(good_wut) HadwW->Fill(adw,rsw);

				if(good_uvw) //one hit in each layer
				{
					if(good_uwt) HaduUMCut->Fill(adu,rsu);
					if(good_vwt) HadvVMCut->Fill(adv,rsv);
					if(good_wut) HadwWMCut->Fill(adw,rsw);

					if(good_uwtt) HaduUMTCut->Fill(adu,rsu);
					if(good_vwtt) HadvVMTCut->Fill(adv,rsv);
					if(good_wutt) HadwWMTCut->Fill(adw,rsw);
					
				}


//Histo Attenuation plots (ADC vs strip number).
//These will be fit to obtain attenuation lengths of strips.
//Geometry will be used to convert strip number to distance in cm.
				if(good_uv) //one hit in u and v layer
				{

					if(adv > 60.0) HADUvsRSV->Fill(rsv,adu);

					if(rsu == 10) HADUvsRSVu10->Fill(rsv,adu);
					if(rsu == 40) HADUvsRSVu40->Fill(rsv,adu);
					if(rsu == 60) HADUvsRSVu60->Fill(rsv,adu);
					if(rsu == 60 && (adu < 150 || adw < 150) && bin[rsu - 1][rsv - 1][rsw - 1] == 1) 
						HADVB->Fill(adv);
					if(rsu == 60 && (adu > 150 && adw > 150) && bin[rsu - 1][rsv - 1][rsw - 1] == 1) 
						HADVS->Fill(adv);


                        
					if(adv > 60.0)
                    {
                        HADUvsRSVustrips->Fill(rsv,adu,rsu);
                    }
					if(adw > 60.0)
                    {
                        HADUvsRSWustrips->Fill(rsw,adu,rsu);
                    }
					if(bin[rsu - 1][rsv - 1][rsw - 1] == 1) 
					{
						HADUvsRSVustripsB->Fill(rsv,adu,rsu);
						HADUvsRSWustripsB->Fill(rsw,adu,rsu);
					}


					if(adu > 60.0)
                    {

                        //v strip calibration REAL
                        HADVvsRSUvstrips200->Fill(rsu,adv,rsv);
                        if(bin[rsu - 1][rsv - 1][rsw - 1] == 1) 
                            HADVvsRSUvstripsB200->Fill(rsu,adv,rsv);
                            
                        HADVvsRSU->Fill(rsu,adv);
                    }
				}
				if(good_uw) //one hit in u and w layer
				{

                    //u calibration REAL
					if(adw > 60.0) 
						HADUvsRSWustrips200->Fill(rsw,adu,rsu);
					if(adw > 60.0 && bin[rsu - 1][rsv - 1][rsw - 1] == 1) 
						HADUvsRSWustripsB200->Fill(rsw,adu,rsu);


					if(adw > 60.0) HADUvsRSW->Fill(rsw,adu);
					//if(adw > 60.0) HADUvsRSV->Fill(1.0-rrs3,adu);
					if(adu > 60.0)
                    {
                        HADWvsRSU->Fill(rsu,adw);

                        //w strip calibration REAL
                        HADWvsRSUwstrips200->Fill(rsu,adw,rsw);
                        if(bin[rsu - 1][rsv - 1][rsw - 1] == 1) 
                            HADWvsRSUwstripsB200->Fill(rsu,adw,rsw);
                            
                        HADVvsRSU->Fill(rsu,adv);
                    }
				}
				if(good_vw) //one hit in v and w layer
				{    
					if(adw > 60.0)
                    {
                        
                        //v strip calibration
                        HADVvsRSWvstrips200->Fill(rsw,adv,rsv);
                        if(bin[rsu - 1][rsv - 1][rsw - 1] == 1) 
                            HADVvsRSWvstripsB200->Fill(rsw,adv,rsv);
                            
                        HADVvsRSW->Fill(rsw,adv);
                    }
					if(adv > 60.0)
                    {
                        HADWvsRSV->Fill(rsv,adw);
                        
                        //w strip calibration
                        HADWvsRSVwstrips200->Fill(rsv,adw,rsw);
                        if(bin[rsu - 1][rsv - 1][rsw - 1] == 1) 
                            HADWvsRSVwstripsB200->Fill(rsv,adw,rsw);
                    }
				}

            //New Test
/******************************************************************/
                if(Pass > 0)
                {
                    if(rsw <= 15)
                    {
                        //converts to 77 strips
                        udist = 2.0 * (Double_t)rsw - 1.0;
                    }
                    else if(rsw > 15)
                    {
                        //converts to 77 strips
                        udist = (30.0 + ((Double_t)rsw - 15.0)) - 0.5;
                    }
                    udist = fabs(udist - 77.0) * width;


                    if(rsu <= 52)
                    {
                        //converts to 84 strips
                        vwdist = (Double_t)rsu - 0.5;
                    }
                    else if(rsu > 52)
                    {
                        //converts to 84 strips
                        vwdist = (52.0 + 2.0*((Double_t)rsu - 52.0)) - 1.0;
                    }
                    vwdist = fabs(vwdist - 84.0) * width;

/*
                    //////////////// Add in fiber Length ///////////////
                    
                    udist = udist * width + fibU[rsu - 1];
                    vdist = vwdist * width + fibV[rsv - 1];
                    wdist = vwdist * width + fibW[rsw - 1];
                    
                    //////////////// End fiber Length Addition //////////
*/

                    ////////////////Correct for Light Att. //////////////
                    if(u0[rsu - 1] == 0) u0[rsu - 1] = 650.0;
                    if(v0[rsv - 1] == 0) v0[rsv - 1] = 650.0;
                    if(w0[rsw - 1] == 0) w0[rsw - 1] = 650.0;
                    if(ugain[rsu - 1] == 0) ugain[rsu - 1] = 650.0;
                    if(vgain[rsv - 1] == 0) vgain[rsv - 1] = 650.0;
                    if(wgain[rsw - 1] == 0) wgain[rsw - 1] = 650.0;
                    
                    Iu = adu - u0[rsu - 1] * exp(uA[rsu - 1] * udist)  + u0[rsu - 1];
                    Iv = adv - v0[rsv - 1] * exp(vA[rsv - 1] * vwdist) + v0[rsv - 1];
                    Iw = adw - w0[rsw - 1] * exp(wA[rsw - 1] * vwdist) + w0[rsw - 1];

                    //Iu *= ugain[rsu - 1];
                   // Iv *= vgain[rsv - 1];
                    //Iw *= wgain[rsw - 1];
                    
                    Isum2 = Iu + Iv + Iw;
                    IntSum2->Fill(Isum2);

                    Iu = ((adu - uB[rsu - 1])/exp(uA[rsu - 1] * udist)) + uB[rsu - 1];
                    Iv = ((adv - vB[rsv - 1])/exp(vA[rsv - 1] * vwdist)) + vB[rsv - 1];
                    Iw = ((adw - wB[rsw - 1])/exp(wA[rsw - 1] * vwdist)) + wB[rsw - 1];

                    //Iu *= ugain[rsu - 1];
                    //Iv *= vgain[rsv - 1];
                    //Iw *= wgain[rsw - 1];
                    Isum1 = Iu + Iv + Iw;
                    IntSum1->Fill(Isum1);                
                    /////////////End Correct for Light Att. /////////////
               

                    //get neighboring values for longest u strips
                    if((Pass ==1 || Pass ==2) && rsu == 68)
                        ueff = rsu - 2;
                    else
                        ueff = rsu - 1;

                    //get neighboring values for longest v strips
                    if((Pass ==1 || Pass ==2) && rsv == 62)
                        veff = rsv - 2;
                    else
                        veff = rsv - 1;

                    //get neighboring values for longest w strips
                    if((Pass ==1 || Pass ==2) && rsw == 62)
                        weff = rsw - 2;
                    else
                        weff = rsw - 1;

                    //Pass 1 makes fitting the last strip possible
                    if(Pass ==1 && (bin[rsu - 1][rsv - 1][rsw - 1] == 1 || bin[rsu - 1][rsv - 1][rsw - 1] == 2))
                    {
                        select = 0;
                        //test for u strip adc value, rsu == 68 is special case
                        if(withingaus3(ucent[ueff][weff], usig[ueff][weff],adu,rsu, bin[rsu - 1][rsv - 1][rsw - 1]))
                        {
                            ++select;
                           //cout << "a";
                        }
                        //test for v strip adc value, rsv == 62 is special case
                        if(withingaus3(vcent[veff][ueff], vsig[veff][ueff],adv,rsv, bin[rsu - 1][rsv - 1][rsw - 1]))
                        {
                            ++select;
                            //cout << "b";
                        }
                        //test for w strip adc value, rsw == 62 is special case
                        if(withingaus3(wcent[weff][ueff], wsig[weff][ueff],adw,rsw, bin[rsu - 1][rsv - 1][rsw - 1]))
                        {
                            ++select;
                           // cout << "c";
                        }

                        //cout << "   " << select << endl;

                        //apply cut
                        if(select == 3)
                        {
                            IntUCut->Fill(Iu);
                            IntVCut->Fill(Iv);
                            IntWCut->Fill(Iw);
                            IntSumCut1->Fill(Isum1);
                            IntSumCut2->Fill(Isum2);
                            HADUvsRSWustripsTEST->Fill(rsw,adu,rsu);
                            HADVvsRSUvstripsTEST->Fill(rsu,adv,rsv);
                            HADWvsRSUwstripsTEST->Fill(rsu,adw,rsw);
                        }
                    }
                    //Pass 2 cuts based on Attenuation Fits in cluding last strip...
                    //Low numbered strips still difficult
                    else if(Pass == 2 && (bin[rsu - 1][rsv - 1][rsw - 1] == 1 || bin[rsu - 1][rsv - 1][rsw - 1] == 2))
                    {
                        select = 0;
                        /////////////////////////Cut u /////////////////////
                        if(rsu > 10)
                        {
                            //calculate the adu value expected
                            Iu = u0[rsu - 1]*exp(uA[rsu - 1] * udist) + uB[rsu - 1];
                            //I0u = ((adu - uB[rsu - 1])/exp(uA[rsu - 1] * udist)) + uB[rsu - 1];
                            //cut on +-100
                            Imin = Iu - 50.0;
                            Imax = Iu + 50.0;
        
                        }
                        else
                        {
                            //calculate the adu value expected
                            Iu = u0[60]*exp(uA[60] * udist) + uB[60];
                            //I0u = ((adu - uB[rsu - 1])/exp(uA[rsu - 1] * udist)) + uB[rsu - 1];
                            //cut on +-100
                            Imin = Iu - 50.0;
                            Imax = Iu + 50.0;
                        }

                        //Apply cut
                        //if centroid is unreasonable
                        if(ucent[rsu - 1][rsw - 1] < Imin || ucent[rsu - 1][rsw - 1] > Imax)
                        {
                            if(adu > Imin && adu < Imax) 
                                ++select;
                        }
                        //if centroid is reasonable
                        else
                        {

                            
                            if(withingaus2(ucent[rsu - 1][rsw - 1], usig[rsu - 1][rsw - 1],adu,rsu, bin[rsu - 1][rsv - 1][rsw - 1])) 
                                ++select;
                            /*
                            if(rsu == 67)
                            {
                                cout << "rsu:     " << rsu << endl;
                                cout << "rsw:     " << rsw << endl;
                                cout << "udist:   " << udist << endl;
                                cout << "Iu:      " << Iu << endl;
                                cout << "Imin:    " << Imin << endl;
                                cout << "adu:     " << adu << endl;
                                cout << "ucent:   " << ucent[ueff][weff] << endl;
                                cout << "Imax:    " << Imax << endl;
                                cout << "select:  " << select << endl;
                                cout << endl;
                                cout << endl;
                                cout << endl;
                            }
                            */                    
                        }

                        /////////////////////////Cut v /////////////////////
                        if(rsv > 10)
                        {
                            //calculate the adu value expected
                            Iv = v0[rsv - 1]*exp(vA[rsv - 1] * vwdist) + vB[rsv - 1];
                            //I0u = ((adu - uB[rsu - 1])/exp(uA[rsu - 1] * udist)) + uB[rsu - 1];
                            //cut on +-100
                            Imin = Iv - 50.0;
                            Imax = Iv + 50.0;
                        }
                        else
                        {
                            //calculate the adu value expected
                            Iv = v0[59]*exp(vA[59] * vwdist) + vB[59];
                            //I0u = ((adu - uB[rsu - 1])/exp(uA[rsu - 1] * udist)) + uB[rsu - 1];
                            //cut on +-100
                            Imin = Iv - 50.0;
                            Imax = Iv + 50.0;
                        }

                        //Apply cut
                        //if centroid is unreasonable
                        if(vcent[rsv - 1][rsu -1] < Imin || vcent[rsv - 1][rsu -1] > Imax)
                        {
                            if(adv > Imin && adv < Imax) 
                                ++select;
                        }
                        //if centroid is reasonable
                        else
                        {
                            if(withingaus2(vcent[rsv - 1][rsu -1], vsig[rsv - 1][rsu -1],adv,rsv, bin[rsu - 1][rsv - 1][rsw - 1])) 
                                ++select;                            
                        }

                        /////////////////////////Cut w /////////////////////
                        if(rsw > 10)
                        {
                            //calculate the adu value expected
                            Iw = w0[rsw - 1]*exp(wA[rsw - 1] * vwdist) + wB[rsw - 1];
                            //I0u = ((adu - uB[rsu - 1])/exp(uA[rsu - 1] * udist)) + uB[rsu - 1];
                            //cut on +-100
                            Imin = Iw - 50.0;
                            Imax = Iw + 50.0;
                        }
                        else
                        {
                            //calculate the adu value expected
                            Iw = w0[59]*exp(wA[59] * vwdist) + wB[59];
                            //I0u = ((adu - uB[rsu - 1])/exp(uA[rsu - 1] * udist)) + uB[rsu - 1];
                            //cut on +-100
                            Imin = Iw - 50.0;
                            Imax = Iw + 50.0;
                        }

                        //Apply cut
                        //if centroid is unreasonable
                        if(wcent[rsw - 1][rsu -1] < Imin || wcent[rsw - 1][rsu -1] > Imax)
                        {
                            if(adw > Imin && adw < Imax) 
                                ++select;
                        }
                        //if centroid is reasonable
                        else
                        {
                            if(withingaus2(wcent[rsw - 1][rsu -1], wsig[rsw - 1][rsu -1],adw,rsw, bin[rsu - 1][rsv - 1][rsw - 1])) 
                                ++select;                            
                        }
                            
                        //apply cut
                        if(select == 3)
                        {
                            IntUCut->Fill(Iu);
                            IntVCut->Fill(Iv);
                            IntWCut->Fill(Iw);
                            IntSumCut1->Fill(Isum1);
                            IntSumCut2->Fill(Isum2);
                            HADUvsRSWustripsTEST->Fill(rsw,adu,rsu);
                            HADVvsRSUvstripsTEST->Fill(rsu,adv,rsv);
                            HADWvsRSUwstripsTEST->Fill(rsu,adw,rsw);
                        }
                    }
                    //Pass 3 fine tuning
                    else if((Pass == 3 || Pass == 4 || Pass == 5) && (bin[rsu - 1][rsv - 1][rsw - 1] == 1))// || bin[rsu - 1][rsv - 1][rsw - 1] == 2))
                    {
                        select = 0;
                        
                        //test for u strip adc value, rsu == 68 is special case
                        if(withingaus3(ucent[ueff][weff], usig[ueff][weff],adu,rsu, bin[rsu - 1][rsv - 1][rsw - 1]))
                        {
                            ++select;
                           //cout << "a";
                        }
                        //test for v strip adc value, rsv == 62 is special case
                        if(withingaus3(vcent[veff][ueff], vsig[veff][ueff],adv,rsv, bin[rsu - 1][rsv - 1][rsw - 1]))
                        {
                            ++select;
                            //cout << "b";
                        }
                        //test for w strip adc value, rsw == 62 is special case
                        if(withingaus3(wcent[weff][ueff], wsig[weff][ueff],adw,rsw, bin[rsu - 1][rsv - 1][rsw - 1]))
                        {
                            ++select;
                           // cout << "c";
                        }
                        
                        //cout << "   " << select << endl;
                        if(select == 3) IntSumCut1A->Fill(Isum1);
                        if(Isum1 > 1300 && Isum1 < 2700) ++select;
                        double maxdiff, avdiff;
                        avdiff = (Iu+Iv+Iw)/3.0;
                        maxdiff = Iw - avdiff;
                        if(fabs(Iu-avdiff) > fabs(maxdiff)) maxdiff = Iu-avdiff;
                        if(fabs(Iv-avdiff) > fabs(maxdiff)) maxdiff = Iv-avdiff;
                        DiffIntensity->Fill(0.5,Iu-Iv);
                        DiffIntensity->Fill(1.5,Iu-Iw);
                        DiffIntensity->Fill(2.5,Iv-Iw);
                        DiffIntensity->Fill(3.5,maxdiff);
                        //if(fabs(maxdiff) < 300) ++select;
                        //apply cut
                        if(select == 4)
                        {
                            IntUCut->Fill(Iu);
                            IntVCut->Fill(Iv);
                            IntWCut->Fill(Iw);
                            IntSumCut1->Fill(Isum1);
                            IntSumCut2->Fill(Isum2);
                            HADUvsRSWustripsTEST->Fill(rsw,adu,rsu);
                            HADUvsRSWustripsTEST50->Fill(rsw,adu,rsu);
                            HADUvsRSWustripsTEST100->Fill(rsw,adu,rsu);
                            HADUvsRSWustripsTEST150->Fill(rsw,adu,rsu);
                            HADUvsRSWustripsTEST200->Fill(rsw,adu,rsu);
                            HADUvsRSWustripsTEST250->Fill(rsw,adu,rsu);
                            HADVvsRSUvstripsTEST->Fill(rsu,adv,rsv);
                            HADWvsRSUwstripsTEST->Fill(rsu,adw,rsw);
                        }
                    }
                    //Pass 3 Cuts again on the Gaussian signal
                    //Hopefully lower numbered strips are better restricted at this point.
                    else if(Pass > 5)
                    {
                        //if(fabs(Imax) < 120.0 && (bin[rsu - 1][rsv - 1][rsw - 1] == 1 || bin[rsu - 1][rsv - 1][rsw - 1] == 2) && Isum > 1200)
                        if(withingaus(ucent[rsu - 1][rsw - 1],usig[rsu - 1][rsw - 1],vcent[rsv - 1][rsu - 1],vsig[rsv - 1][rsu - 1],wcent[rsw - 1][rsu - 1],wsig[rsw - 1][rsu - 1],adu,adv,adw,rsu,rsv,rsw))
                        {
                            IntUCut->Fill(Iu);
                            IntVCut->Fill(Iv);
                            IntWCut->Fill(Iw);
                            IntSumCut1->Fill(Isum1);
                            IntSumCut2->Fill(Isum2);
                            HADUvsRSWustripsTEST->Fill(rsw,adu,rsu);
                            HADVvsRSUvstripsTEST->Fill(rsu,adv,rsv);
                            HADWvsRSUwstripsTEST->Fill(rsu,adw,rsw);
                        }
                    }
                
                }
/******************************************************************/
            //End New test

            } //end if for good_uvw
			/*
			nn[1] = nn[1] + 1;
			if(nn[1] > 100000)
			{
				nn[2] = nn[2] + 100000;
				cout << "NEVENTS =   " << nn[2];
				nn[1] = 0;
			}
			*/

            percent = (Int_t)(((Double_t)e * 100.0)/nentries);
            if((percent != oldpercent) && (percent % 5 == 0))
            {
                oldpercent = percent;
                cout << "Processing..." << percent << "% complete" << endl;
            }


		} // end loop over all entries
	} //end if clause over h10 tree
	else
		cout << "Tree not found" << endl;


	outFile.Write(); // write to the output file
	outFile.Close(); // close the output file

}


Double_t uvw_dist(Int_t strip, Int_t layer)
{
	Double_t uvw;
	
	//numbers have to do with strip widths
	//u after 52 strip number corresponds to 2 strips
		// 68 + 16 = 84
		// 68 - 16 = 52
	//v,w: the first 15 strip numbers correspond to 2 strips
		// 62 + 15 = 77
		// 15 * 2 = 30
	//Good pixel occurs at 2.0 +/- error

	if(layer == 1 && strip <= 52)
		uvw = strip/84.0;
	if(layer == 1 && strip > 52)
		uvw = (52.0 + (strip - 52.0)*2.0)/84.0;
	if(layer == 2 && strip <= 15)
		uvw = 2.0 * strip/77.0;
	if(layer == 2 && strip > 15)
		uvw = (30.0 + (strip - 15.0))/77.0;
	if(layer == 3 && strip <= 15)
		uvw = 2.0 * strip/77.0;
	if(layer == 3 && strip > 15)
		uvw = (30.0 + (strip - 15.0))/77.0;

	return(uvw);
}


void import(int bin[USIZE][VSIZE][WSIZE])
{
	ifstream hitfile ("hitmatrix.txt");

	int i,j,k, dummy;
	for(i = 0; i<USIZE ; ++i)
	{
		for(j = 0; j<VSIZE ; ++j)
		{
			for(k = 0; k<WSIZE ; ++k)
			{
				hitfile >> dummy;
				bin[i][j][k] = (int)dummy;
			}
		}
	}
	
	hitfile.close();
}

void importgaus(double ucent[USIZE][WSIZE],double usig[USIZE][WSIZE],double vcent[VSIZE][USIZE],double vsig[VSIZE][USIZE],double wcent[WSIZE][USIZE],double wsig[WSIZE][USIZE])
{
	int unum, wnum, vnum, ijunk, i, j;
    double djunk;

    //initialize u centroid and sigma
    for(i = 0; i < USIZE; ++i)
    {
        for(j=0;j<WSIZE;++j)
        {
            ucent[i][j]=0.0;
            usig[i][j]=0.0;
        }
    }

    //initialize v centroid and sigma
    for(i = 0; i < VSIZE; ++i)
    {
        for(j=0;j<USIZE;++j)
        {
            vcent[i][j]=0.0;
            vsig[i][j]=0.0;
        }
    }

    //initialize w centroid and sigma
    for(i = 0; i < WSIZE; ++i)
    {
        for(j=0;j<USIZE;++j)
        {
            wcent[i][j]=0.0;
            wsig[i][j]=0.0;
        }
    }

    ifstream ufile("Ucentroid.txt");
    while(ufile >> unum)
    {
        if(unum != 0)
        {
            ufile >> wnum;
            ufile >> ucent[unum - 1][wnum - 1];
            ufile >> usig[unum - 1][wnum - 1];
        }
        else
        {
            ufile >> ijunk;
            ufile >> djunk;
            ufile >> djunk;
        }            
    }
	ufile.close();

    ifstream vfile("Vcentroid.txt");
    while(vfile >> vnum)
    {
        if(vnum != 0)
        {
            vfile >> unum;
            vfile >> vcent[vnum - 1][unum - 1];
            vfile >> vsig[vnum - 1][unum - 1];
        }
        else
        {
            vfile >> ijunk;
            vfile >> djunk;
            vfile >> djunk;
        }            
    }
	vfile.close();

    ifstream wfile("Wcentroid.txt");
    while(wfile >> wnum)
    {
        if(wnum != 0)
        {
            wfile >> unum;
            wfile >> wcent[wnum - 1][unum - 1];
            wfile >> wsig[wnum - 1][unum - 1];
        }
        else
        {
            wfile >> ijunk;
            wfile >> djunk;
            wfile >> djunk;
        }            
    }
	wfile.close();
}

void importatten(double u0[],double uA[],double uB[],double v0[],double vA[],double vB[],double w0[],double wA[],double wB[])
{

    int num;

    ifstream ufile("UAtten.txt");
    while(ufile >> num)
    {
        if(num != 0)
        {
            num = num -1;
            ufile >> u0[num];
            ufile >> uA[num];
            ufile >> uB[num];
        }
        else
        {
            cout << "There is something wrong with the file WAtten.txt." << endl;
        }            
    }
	ufile.close();

    ifstream vfile("VAtten.txt");
    while(vfile >> num)
    {
        if(num != 0)
        {
            num = num -1;
            vfile >> v0[num];
            vfile >> vA[num];
            vfile >> vB[num];
        }
        else
        {
            cout << "There is something wrong with the file WAtten.txt." << endl;
        }            
    }
	vfile.close();
    
    ifstream wfile("WAtten.txt");
    while(wfile >> num)
    {
        if(num != 0)
        {
            num = num -1;
            wfile >> w0[num];
            wfile >> wA[num];
            wfile >> wB[num];
        }
        else
        {
            cout << "There is something wrong with the file WAtten.txt." << endl;
        }            
    }
	wfile.close();
}


void importgains(double ugain[], double vgain[], double wgain[])
{

    int num;

    ifstream ufile("UGains.dat");
    while(ufile >> num)
    {
        if(num != 0)
        {
            num = num -1;
            ufile >> ugain[num];
        }
        else
        {
            cout << "There is something wrong with the file UGains.dat." << endl;
        }            
    }
	ufile.close();

    ifstream vfile("VGains.dat");
    while(vfile >> num)
    {
        if(num != 0)
        {
            num = num -1;
            vfile >> vgain[num];
        }
        else
        {
            cout << "There is something wrong with the file Vgains.dat." << endl;
        }            
    }
	vfile.close();
    
    ifstream wfile("WGains.dat");
    while(wfile >> num)
    {
        if(num != 0)
        {
            num = num -1;
            wfile >> wgain[num];
        }
        else
        {
            cout << "There is something wrong with the file WGains.dat." << endl;
        }            
    }
	wfile.close();
}

void combinegains(const double ugaini[], const double vgaini[], const double wgaini[], const double ugainf[], const double vgainf[], const double wgainf[])
{

    int i;

    ofstream ufile("UGains.dat");
    for(i = 68; i > 0; --i)
    {
        if(ugaini[i-1] * ugainf[i-1] > 0.001 && ugaini[i-1] * ugainf[i-1] < 5.0)
            ufile << i << "   " << ugaini[i-1] * ugainf[i-1] << endl;
        else
            ufile << i << "   " << 1.0 << endl;           
    }
	ufile.close();

    ofstream vfile("VGains.dat");
    for(i = 62; i > 0; --i)
    {
        if(vgaini[i-1] * vgainf[i-1] > 0.001 && vgaini[i-1] * vgainf[i-1] < 5.0)
            vfile << i << "   " << vgaini[i-1] * vgainf[i-1] << endl;
        else
            vfile << i << "   " << 1.0 << endl;           
    }
	vfile.close();
    
    ofstream wfile("WGains.dat");
    for(i = 62; i > 0; --i)
    {
        if(wgaini[i-1] * wgainf[i-1] > 0.001 && wgaini[i-1] * wgainf[i-1] < 5.0)
            wfile << i << "   " << wgaini[i-1] * wgainf[i-1] << endl;
        else
            wfile << i << "   " << 1.0 << endl;            
    }
	wfile.close();
}

bool withingaus(double Ucent,double Usig,double Vcent,double Vsig,double Wcent,double Wsig, double adU, double adV, double adW,int rsU,int rsV,int rsW)
{
    int yes = 0;
    double max, min;

    // restrict adc values for u strip
    if(rsU == 68)
    {
        max = fabs(Ucent) + fabs(2.0 * Usig);
        min = fabs(Ucent) - fabs(2.0 * Usig);
    }
    else
    {
        max = fabs(Ucent) + fabs(3.0 * Usig);
        min = fabs(Ucent) - fabs(3.0 * Usig);
    }
    if(adU > min && adU < max)
        ++yes;
        
    // restrict adc values for v strip
    if(rsV == 62)
    {
        max = fabs(Vcent) + fabs(2.0 * Vsig);
        min = fabs(Vcent) - fabs(2.0 * Vsig);
    }
    else
    {
        max = fabs(Vcent) + fabs(3.0 * Vsig);
        min = fabs(Vcent) - fabs(3.0 * Vsig);
    }
    if(adV > min && adV < max)
        ++yes;

    // restrict adc values for v strip
    if(rsW == 62)
    {
        max = fabs(Wcent) + fabs(2.0 * Wsig);
        min = fabs(Wcent) - fabs(2.0 * Wsig);
    }
    else
    {
        max = Wcent + fabs(3.0 * Wsig);
        min = Wcent - fabs(3.0 * Wsig);
    }
    if(adW > min && adW < max)
        ++yes;

    return (yes == 3);
}

bool withingaus2(double Cent,double Sig, double Adc, int Strip, int Hit)
{
    int yes = 0;
    double max, min;

    max = fabs(Cent) + fabs(2.0 * Sig);
    min = fabs(Cent) - fabs(2.0 * Sig);
    if(Adc > min && Adc < max)
    {
        yes = 1;
    }
    else if(Hit != 0 && Cent == 0)
    {
        //valid pixel, but bad fit
        //most likely strips 1-10...
        yes = 1;
    }

    return(yes == 1);
}

bool withingaus3(double Cent,double Sig, double Adc, int Strip, int Hit)
{
    int yes = 0;
    double max, min;

    max = fabs(Cent) + fabs(3.0 * Sig);
    min = fabs(Cent) - fabs(3.0 * Sig);
    if(Adc > min && Adc < max)
    {
        yes = 1;
    }
    else if((Hit != 0 && Cent == 0))
    {
        //valid pixel, but bad fit
        //most likely strips 1-10...
        yes = 1;
    }
    //else if(Strip < 6 && (Adc > 200 || Cent == 0))
    //{
        ////valid pixel, but bad fit
        ////most likely strips 1-10...
        //yes = 1;
    //}
    

    return (yes == 1);
}

void GetFiberULengths(double array1[], double array2[], double array3[])
{
	int num;
    double junk;
	
	ifstream fib_file;
    fib_file.open ("Uparam.txt");
    while(fib_file >> num)
    {
        if(num != 0)
        {
            fib_file >> array1[num-1];
            fib_file >> array2[num-1];
            fib_file >> array3[num-1];
        }
        else
        {
            fib_file >> junk;
            fib_file >> junk;
            fib_file >> junk;
        }            
    }
	fib_file.close();
}

void GetFiberVLengths(double array1[], double array2[], double array3[])
{
	int num;
    double junk;
	
	ifstream fib_file;
    fib_file.open ("Vparam.txt");
    while(fib_file >> num)
    {
        if(num != 0)
        {
            fib_file >> array1[num-1];
            fib_file >> array2[num-1];
            fib_file >> array3[num-1];
        }
        else
        {
            fib_file >> junk;
            fib_file >> junk;
            fib_file >> junk;
        }            
    }
	fib_file.close();
}

void GetFiberWLengths(double array1[], double array2[], double array3[])
{
	int num;
    double junk;
	
	ifstream fib_file;
    fib_file.open ("Wparam.txt");
    while(fib_file >> num)
    {
        if(num != 0)
        {
            fib_file >> array1[num-1];
            fib_file >> array2[num-1];
            fib_file >> array3[num-1];
        }
        else
        {
            fib_file >> junk;
            fib_file >> junk;
            fib_file >> junk;
        }            
    }
	fib_file.close();
}

void adjustcoefficients()
{
  //Get final Gains
  double ugainf[USIZE];
  double vgainf[VSIZE];
  double wgainf[WSIZE];
  importgains(ugainf, vgainf, wgainf); //import new gains
  
    //Adjust u coefficients with gains
    int num;
    int ustrip[USIZE] = {1};
    double u0[USIZE] = {1.0};
    double uA[USIZE] = {0.0};
    double uB[USIZE] = {0.0};

    ifstream ufile("UAtten.txt");
    while(ufile >> num)
    {
        if(num != 0)
        {
            num = num -1;
            ustrip[num] = num + 1;
            ufile >> u0[num];
            ufile >> uA[num];
            ufile >> uB[num];
        }
        else
        {
            cout << "There is something wrong with the file WAtten.txt." << endl;
        }            
    }
	ufile.close();
    
    ofstream ufileout("UAtten.txt");
    for(num = 0; num < USIZE; ++num)
    {
        ufileout << ustrip[num] << "    ";
        ufileout << u0[num] * ugainf[num] << "    ";
        ufileout << uA[num] << "    ";
        ufileout << uB[num] * ugainf[num] << endl;          
    }
	ufileout.close();
    
    //Adjust v coefficients with gains
    int vstrip[VSIZE] = {1};
    double v0[VSIZE] = {1.0};
    double vA[VSIZE] = {0.0};
    double vB[VSIZE] = {0.0};
    ifstream vfile("VAtten.txt");
    while(vfile >> num)
    {
        if(num != 0)
        {
            num = num -1;
            vstrip[num] = num + 1;
            vfile >> v0[num];
            vfile >> vA[num];
            vfile >> vB[num];
        }
        else
        {
            cout << "There is something wrong with the file WAtten.txt." << endl;
        }            
    }
	vfile.close();

    ofstream vfileout("VAtten.txt");
    for(num = 0; num < VSIZE; ++num)
    {
        vfileout << vstrip[num] << "    ";
        vfileout << v0[num] * vgainf[num] << "    ";
        vfileout << vA[num] << "    ";
        vfileout << vB[num] * vgainf[num] << endl;          
    }
	vfileout.close();

    //Adjust w coefficients with gains
    int wstrip[WSIZE] = {1};
    double w0[WSIZE] = {1.0};
    double wA[WSIZE] = {0.0};
    double wB[WSIZE] = {0.0};    
    ifstream wfile("WAtten.txt");
    while(wfile >> num)
    {
        if(num != 0)
        {
            num = num -1;
            wfile >> w0[num];
            wfile >> wA[num];
            wfile >> wB[num];
        }
        else
        {
            cout << "There is something wrong with the file WAtten.txt." << endl;
        }            
    }
	wfile.close();

    ofstream wfileout("WAtten.txt");
    for(num = 0; num < WSIZE; ++num)
    {
        wfileout << wstrip[num] << "    ";
        wfileout << w0[num] * wgainf[num] << "    ";
        wfileout << wA[num] << "    ";
        wfileout << wB[num] * wgainf[num] << endl;          
    }
	wfileout.close();

}

# ifndef __CINT__
int main()
{
  int num_iter = 6;
  int pass = 0;


  //Get initial Gains
  double ugaini[USIZE];
  double vgaini[VSIZE];
  double wgaini[WSIZE];
  importgains(ugaini, vgaini, wgaini);
  //Get final Gains
  double ugainf[USIZE];
  double vgainf[VSIZE];
  double wgainf[WSIZE];
  /*
  int i;
  for(i = 0; i < USIZE; ++i)
  {
      ugaini[i] = 1.0;
  }
  for(i = 0; i < VSIZE; ++i)
  {
      vgaini[i] = 1.0;
      wgaini[i] = 1.0;
  }
  */
  //Get FiberLengths
  //Get Initial Gaussian Parameters
  //Get Initial Attenuation Fit Parameters
  //Get hit matrix

  for(pass = 0; pass < num_iter; ++pass)
  {
      if(pass > 2)
      {
          importgains(ugainf, vgainf, wgainf); //import new gains
          combinegains(ugaini, vgaini, wgaini, ugainf, vgainf, wgainf);//multiply into old ones
          importgains(ugaini, vgaini, wgaini); //import new gains
      }
      Rawhistos(ugaini, vgaini, wgaini, pass);
      cout << "Pass " << pass << " of the Histograms generated!" << endl;
      cout << endl;
      cout << endl;
      fitU(pass);
      cout << "Pass " << pass << " of the Fits to U strips completed." << endl;
      cout << endl;
      cout << endl;
      fitV(pass);
      cout << "Pass " << pass << " of the Fits to V strips completed." << endl;
      cout << endl;
      cout << endl;
      fitW(pass);
      cout << "Pass " << pass << " of the Fits to W strips completed." << endl;
      cout << endl;
      cout << endl;
      //combinefiles();
    }
    //fitU(6);
    //fitU(7);
    //fitU(8);
    //fitU(9);

    importgains(ugainf, vgainf, wgainf); //import new gains
    adjustcoefficients();
    combinegains(ugaini, vgaini, wgaini, ugainf, vgainf, wgainf);//multiply into old ones



  
  return 0;
}
# endif

