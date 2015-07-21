#include <iostream>
#include <fstream>
#include <string.h>
#include <math.h>
#include "TCanvas.h"
#include "TF1.h"
#include "TH1.h"
#include "TH3.h"
#include "TGraphErrors.h"
#include "TStyle.h"
#include "TSpectrum.h"
#include "TNamed.h"


using namespace std;

void Initialize(double array[], const int size);
void GetFiberLengths(double array[], const int size, const char uplet);
void DrawChiSquare(int count,double chiarray[], const int strip, const int passnum);
void FitWithExpGaus(TH1F *fithisto, double par[], double parE[], const int fitnum, double *Rchisquare);
void FitWithGaus(TH1F *fithisto, double par[], double parE[], const int fitnum, double *Rchisquare);

void CalcDistinStrips(const char stripletter, const int crossstrip, double x[], double xE[], const int pointnum);

void CalcDistance(double xdistance[], double xdistanceE[],const double distperstrip, const int numpoints, const char stripletter);

void SetFitLimits(double *xlow, double *xhigh, const double xdist[], const int numpoints);
void CalcFiberLengths(const char lowlet, const int ustrip, double avfiberlength[], const double fiblength[], double xdist[], const int numpoints);

void FitAttenuation(const int numpoints, const int xlow, const int xhigh, double parfit[], double parfitE[], TGraphErrors *graphatt, TF1 *totalatt, const char fitname[], double *redchisq);

void fit(const int Pass = 0, const char striplet = 'x')
{

    char lowlet, uplet;
    if(striplet == 'x')
    {
        cout << "Passing an inappropriate strip label!";
        return;
    }
    else
    {
        lowlet = tolower(striplet);
        uplet = toupper(striplet);
    }
    string filetype;
    string histfolder = "pass";
    string fitfolder = "pass";

    char stringnum[3];
    sprintf(stringnum,"%1d",Pass);
    
    histfolder = histfolder + stringnum[0]; //where to look from
    fitfolder = fitfolder + stringnum[0]; //where to write to
        
    //cout << histfolder << endl;

    if(Pass == 0)
    {
        filetype = "recreate";
    }
    else
    {
        filetype = "update";
    }

    //u,v,w specific
    char gethistname[50];
    char crossstrip = 'W';
    int num_in_layer = 68;
    int num_in_crosslayer = 62;
    if(lowlet == 'u')
    {
        crossstrip = 'W';
        num_in_layer = 68;
        num_in_crosslayer = 62;
    }
    else if(lowlet == 'v')
    {
        crossstrip = 'U';
        num_in_layer = 62;
        num_in_crosslayer = 68;
    }
    else if(lowlet == 'w')
    {
        crossstrip = 'U';
        num_in_layer = 62;
        num_in_crosslayer = 68;
    }

	double par[6], parE[6];
	double parfit[4], parfitE[4];
	double x[68], xE[68], y[68], yE[68];
	double fib[69];
	double fiblen[69 + 16];
    TGraphErrors *graph[69];
    TF1 *total2[69];
	//double swidth = 4.5;
	double width = 5.055;
	int wstrip;
    //int i, j;
    int count, ustrip;
	int nentries = 0;
	char name[30];
	char canname[30];
    char fitname[30];
    char graphname[30];
    double xlow, xhigh;

	//bin study
    int gcount = 0;
	double xsquare[68 * 62];
    //double ndf;

    //fit study
	double x2square[69];
    int fitcount = 0;

	//statistical test
	//double functionval;
	//double diffsquare;
	//double chisquare;
    double redchisquare, redchisq2;

    //bincontent function
    //double bincontent;
    //double bincontentold, lowestx;
    //int bin, maxbin;

    //declare 3d histo
    TH3F *inhist;

	TCanvas *canfit[69];
	TCanvas *cangr[69];
    //TCanvas *canallatt = new TCanvas("ustrip","U - Attenuation");
    //canallatt->Divide(8,9);

    //input file
	TFile *f = new TFile("outputfiles/Histo.root");
    f->cd(histfolder.c_str());
    gDirectory->cd("Setup");


    if(Pass == 0)
    {
        sprintf(gethistname, "HAD%cvsRS%c%cstrips200", uplet, crossstrip, lowlet);
        inhist = (TH3F*)gDirectory->Get(gethistname);
    }
    /*
    else if(Pass == 6)
    {
        inhist = (TH3F*)gDirectory->Get("HADUvsRSWustripsTEST50");
    }
    else if(Pass == 7)
    {
        inhist = (TH3F*)gDirectory->Get("HADUvsRSWustripsTEST100");
    }
    else if(Pass == 8)
    {
        inhist = (TH3F*)gDirectory->Get("HADUvsRSWustripsTEST150");
    }
    else if(Pass == 9)
    {
        inhist = (TH3F*)gDirectory->Get("HADUvsRSWustripsTEST200");
    }
    */
    else
    {
        sprintf(gethistname, "HAD%cvsRS%c%cstripsTEST", uplet, crossstrip, lowlet);
        inhist = (TH3F*)gDirectory->Get(gethistname);
    }

    //output file
    char fitfilename[50];
    sprintf(fitfilename, "outputfiles/%cFits.root", uplet);
    string cppfitfilename = fitfilename;
    TFile fitsfile(cppfitfilename.c_str(),filetype.c_str());
    fitsfile.mkdir(fitfolder.c_str());
    fitsfile.cd(fitfolder.c_str());
    gDirectory->mkdir("SigFits");
    gDirectory->mkdir("AtFits");
    gDirectory->cd("SigFits");
    
	Initialize(fib,69);
	Initialize(fiblen,69+15);
	GetFiberLengths(fiblen,69+15,uplet);

    char genfilename[100];
    
    sprintf(genfilename, "outputfiles/%ccentroid.txt", uplet);
	ofstream centroids (genfilename);

    //sprintf(genfilename, "%cparameters.txt", uplet);
	//ofstream Par_file (genfilename);

    sprintf(genfilename, "outputfiles/%cAtten.txt", uplet);
    ofstream Atten (genfilename); // strip, I0, A, B

    sprintf(genfilename, "outputfiles/%cGains.dat", uplet);
    ofstream Gains (genfilename); // x = 0 value
    
	Initialize(parfit,3);
	Initialize(parfitE,3);
	parfit[0] = 6.5;
	parfit[1] = -0.005;
	parfit[2] = 180.0;


	for(ustrip = num_in_layer; ustrip > 0; --ustrip)
	{
		count = 0;
        Initialize(x,68);
        Initialize(xE,68);
        Initialize(y,68);
        Initialize(yE,68);
		sprintf(name,"%cstrip fits: %02d",lowlet, ustrip);
		canfit[ustrip] = new TCanvas(name,name);
		canfit[ustrip]->Divide(8,9);


        //cout << "On this strip: "<< ustrip << endl;
		for(wstrip = num_in_crosslayer; wstrip > 0; --wstrip)
		{
			canfit[ustrip]->cd(wstrip);
			sprintf(name, "proj%02d%02dpass%1d", ustrip, wstrip,Pass);
			TH1F *histo = (TH1F *)inhist->ProjectionY(name,wstrip,wstrip,ustrip,ustrip);
            
					     //3D histo           counts vs adu, w-strip, u-strip
			histo->Draw();

			nentries = (int)histo->GetEntries();
			if(nentries > 10 && (histo->GetBinContent(histo->GetMaximumBin()) > 3))
			{                
/*************** Fit based on pass number *************************/
                if(Pass == 0)
                {
                    //Fit raw signal
                    //Background plus gaussian      
                    FitWithExpGaus(histo, par, parE, count, &redchisquare);

                    //if(0.5 * par[3] > parE[3] && 0.5 * par[4] > parE[4])
                    if(fabs(par[2]) > parE[2] && fabs(par[3]) > parE[3] && fabs(par[4]) > parE[4])
                    {
                        if(par[3] > 90.0 && par[3] < 1000.0)
                        {
                            CalcDistinStrips(lowlet, wstrip, x, xE, count);
                            //x[count] = wstrip;
                            y[count] = par[3];
                            yE[count] = parE[3];
                            ++count;
                            
                            xsquare[gcount] = redchisquare;
                            ++gcount;

                        } //check gaussian centroid
                        centroids << ustrip << "  " << wstrip << "  " << par[3] << "   " << par[4] << endl;
                    } //check parameter errors
                    else
                    {
                        centroids << ustrip << "  " << wstrip << "  " << 0.0 << "   " << 0.0 << endl;
                    }
                }//Pass == 0
                else if(Pass >= 1)
                {
                    //Fit raw signal, hopefully gaussian like
                    FitWithGaus(histo, par, parE, count, &redchisquare);

                    if(par[0] > parE[0] && par[1] > parE[1] && par[2] > parE[2])
                    {
                        CalcDistinStrips(lowlet, wstrip, x, xE, count);
                        //x[count] = wstrip;
                        y[count] = par[1];
                        yE[count] = parE[1];
                        ++count;
                        
                        xsquare[gcount] = redchisquare;
                        ++gcount;
                    
                        centroids << ustrip << "  " << wstrip << "  " << par[1] << "   " << par[2] << endl;
                    } //check parameter errors
                    else
                    {
                        centroids << ustrip << "  " << wstrip << "  " << 0.0 << "   " << 0.0 << endl;
                    }
                }//Pass > 0
/************************* end fit of histograms ***************/
			} //nentries
            else
            {
                
                //histo->~TH1F();
            }
		}//w strip
        canfit[ustrip]->~TCanvas();

        //cout << "Finished looping over W cross strips. " <<endl;

        //Change distance from strips from edge to cm from edge
        CalcDistance(x, xE, width, count, lowlet);

        
        // arguements
        //u/v/w, strip number, logical strips, physical strips, dist, number of points
        //CalcFiberLengths(lowlet, ustrip, fib, fiblen, x, count);


        //initialize limits
        xlow = 0.0;
        xhigh = 0.0;
        //Set limits for attenuation fit
        SetFitLimits(&xlow, &xhigh, x, count);

        // set names for canvases, graphs, and fits for attenuation
        sprintf(canname,"%cstrip: %d", lowlet, ustrip);
        sprintf(graphname,"%cstrip%02dpass%1d", lowlet,ustrip,Pass);
        cangr[ustrip] = new TCanvas(canname,canname);
        sprintf(fitname,"%cfit%02dpass%1d", lowlet, ustrip, Pass);

        //plot all fits on one canvas
        // canallatt->cd(ustrip);

        //cout << "About to fit exponential for attenuatons." << endl;
		if(count > 0)
		{
            //cout << "Count > 4" << endl;
			graph[ustrip] = new TGraphErrors(count, x, y, xE, yE);
			graph[ustrip]->SetTitle(canname);
            graph[ustrip]->SetName(graphname);
			graph[ustrip]->SetMarkerStyle(8);
			graph[ustrip]->SetMarkerSize(0.3);
            graph[ustrip]->GetXaxis()->SetLimits(0.0, xhigh + 5.0*width);
			graph[ustrip]->Draw("APE");

            //cout << "About to go into the function Fit." << endl;
            FitAttenuation(count, xlow, xhigh, parfit, parfitE, graph[ustrip], total2[ustrip], fitname, &redchisq2);

            //cout << "Function Passed!" << endl;
            
            //Par_file << ustrip << "  &  " << parfit[1] << "  &  " <<  parfit[2]  << "  hline " << endl;
            // strip, I0, A, B
            Atten << ustrip << "    " << parfit[0] << "    " << parfit[1] << "    " << parfit[2] << endl;
            Gains << ustrip << "    " << 650.0/(parfit[0] + parfit[2]) << endl;

            //cout << "chisquarecalculation." << endl;
            
            x2square[fitcount] = redchisq2;
            ++fitcount;


            //cout << "after chisquare" <<endl;
            gDirectory->cd("../AtFits");
            gDirectory->Append(graph[ustrip]);
            gDirectory->cd("../SigFits");

        }
        else
        {
            cout << "No Gaussian Fit achieved. Layer: " << lowlet << " Stip #: " << ustrip << endl;
            Atten << ustrip << "    " << 0.0 << "    " << 0.0 << "    " << 0.0 << endl;
            //Par_file << ustrip << "  &  " << 0.0 << "  &  " <<  0.0  << "  hline " << endl;
            Gains << ustrip << "    " << 1.0 << endl;
        }
        
        cangr[ustrip]->~TCanvas();
        //total2[ustrip]->~TF1();
        //graph[ustrip]->~TGraphErrors();
        //cout << "Got through ustrips!" << endl;



	} //u strip
    Atten.close();
	//Par_file.close();
    Gains.close();
    centroids.close();


    //cout << "Closed some files and now passing into draw routine" << endl;
    DrawChiSquare(gcount, xsquare,1,Pass);
    DrawChiSquare(fitcount, x2square,10,Pass);

    //cout << "About to write and close UFit.root" << endl;
    fitsfile.Write(); // write to the output file
	fitsfile.Close(); // close the output file
    //cout << "About to write and close Hiso.root" << endl;
    f->Close();

    //cout << "The error should've occured by now." << endl;

}

void FitAttenuation(const int numpoints, const int xlow, const int xhigh, double parfit[], double parfitE[], TGraphErrors *graphatt, TF1 *totalatt, const char fitname[], double *redchisq)
{

    int i;
    
        if(numpoints > 4)
		{

			TF1 *exp = new TF1("exp","[0]*exp([1]*x)", xlow, xhigh);
			exp->SetParameters(parfit);
			graphatt->Fit(exp,"QMR");   //fit with exponential
			exp->GetParameters(&parfit[0]);
				//f(x) = exp([0] + [1]*x)

            /*
			TF1 *exp2 = new TF1("exp2","[0]*exp(([1] + [2])*x + [1]*[3])",xlow,xhigh);
			//exp2->SetParameters(parfit);
            exp2->SetParameter(0,600.0);
            exp2->SetParameter(1,-0.0005);
            exp2->SetParameter(2,-0.0001);
            exp2->FixParameter(3,fib[ustrip]);
			graph->Fit(exp2,"BMR");   //fit with exponential
			exp2->GetParameters(&parfit[0]);
				//f(x) = exp([0] + [1]*x)
            */                

			//exponential with constant fit
			totalatt = new TF1(fitname, "[0]*exp([1]*x) + [2]",xlow,xhigh);
			totalatt->SetParameters(parfit);
            totalatt->SetParameter(0,600.0);
            totalatt->SetParameter(1,-0.005);
            totalatt->SetParameter(2,200.0);
            totalatt->SetParLimits(0,200.0,900.0);
            totalatt->SetParLimits(1,-0.009,-0.0005);
            totalatt->SetParLimits(2,0.0,700.0);
			graphatt->Fit(totalatt,"QMBR");  //fit with exp plus const 
			totalatt->GetParameters(&parfit[0]);
			for(i = 0; i < 3; ++i)
			{
				parfitE[i] = totalatt->GetParError(i);
			}

            exp->~TF1();
      
		} //fitting
        else if(numpoints == 4)
        {
			//exponential with constant fit
			totalatt = new TF1(fitname, "[0]*exp([1]*x)",xlow,xhigh);
			totalatt->SetParameters(parfit);
            totalatt->SetParameter(0,600.0);
            totalatt->SetParameter(1,-0.005);
            //totalatt->SetParameter(2,0.0);
            totalatt->SetParLimits(0,200.0,900.0);
            totalatt->SetParLimits(1,-0.009,-0.0005);
            //totalatt->SetParLimits(2,0.0,0.0);
			graphatt->Fit(totalatt,"QMBR");  //fit with exp plus const 
			totalatt->GetParameters(&parfit[0]);
			for(i = 0; i < 2; ++i)
			{
				parfitE[i] = totalatt->GetParError(i);
			}

            // A             B               C
            //parfit[0] = ;
            //parfit[1] = ;
            parfit[2] = 0.0; 
        }
        else if(numpoints > 0)
        {
            //exponential with constant fit
			totalatt = new TF1(fitname, "[0]",xlow,xhigh);
            totalatt->SetParameter(0,650.0);
            totalatt->SetParLimits(0,100.0,1000.0);
			graphatt->Fit(totalatt,"QMBR");  //fit with exp plus const 
			totalatt->GetParameters(&parfit[0]);
			for(i = 0; i < 1; ++i)
			{
				parfitE[i] = totalatt->GetParError(i);
			}

            // A             B               C
            //parfit[0] = ;
            parfit[1] = 0.0;
            parfit[2] = 0.0; 

        }
        else
        {
            cout << "No Gaussian Fit achieved. " << endl;
        }
        if((double)totalatt->GetNDF() > 0.0)
        {
            *redchisq = totalatt->GetChisquare()/(double)totalatt->GetNDF();
        }
        else
        {
            *redchisq = 0.0;
        }
            

}

void CalcDistinStrips(const char stripletter, const int crossstrip, double x[], double xE[], const int pointnum)
{
    if(stripletter == 'u')
    {
        if(crossstrip <= 15)
        {
            //converts to 77 strips
            x[pointnum] = 2.0* crossstrip - 1.0;
            xE[pointnum] = 1.0;
        }
        else if(crossstrip > 15)
        {
            //converts to 77 strips
            x[pointnum] = (30.0 + (crossstrip - 15.0)) - 0.5;
            xE[pointnum] = 1.0/2.0;
        }
    }
    else if(stripletter == 'v' || stripletter == 'w')
    {
        if(crossstrip <= 52)
        {
            //converts to 84 strips
            x[pointnum] = crossstrip - 0.5;
            xE[pointnum] = 1.0/2.0;
            }
            else if(crossstrip > 52)
            {
                //converts to 84 strips
                x[pointnum] = (52.0 + 2.0*(crossstrip - 52.0)) - 1.0;
                xE[pointnum] = 1.0;
            }
    }
}

void SetFitLimits(double *xlow, double *xhigh, const double xdist[], const int numpoints)
{
    int lowindex, highindex;

    if(numpoints == 0)
    {
        cout << "No signal fits available." << endl;
    }
    else if(numpoints == 1)
    {
        *xlow = xdist[0]/2.0;
        *xhigh = xdist[0] + xdist[0]/2.0;
    }
    else if(numpoints > 1)
    {
        //just in case the strips are looped through backwards
        if(xdist[0] < xdist[1])
        {
            lowindex = 0;
            highindex = numpoints - 1;
        }
        else
        {
            lowindex = numpoints - 1;
            highindex = 0;
        }

        //decide limits case by case
        if(numpoints == 2)
        {
            *xlow = xdist[lowindex]/2.0;
            *xhigh = xdist[highindex] + xdist[lowindex]/2.0;
        }
        else if(numpoints > 2 && numpoints < 8)
        {
            //exclude end points
            *xlow = xdist[lowindex] + xdist[lowindex]/2.0;
            *xhigh = xdist[highindex] - xdist[lowindex]/2.0;
        }
        else
        {
            //exclude two points on both sides
            *xlow = 2.0 * xdist[lowindex] + xdist[lowindex]/2.0;
            *xhigh = xdist[highindex] - 2.0 * xdist[lowindex] - xdist[lowindex]/2.0;
        }
    }
}

void CalcDistance(double xdistance[], double xdistanceE[], const double distperstrip, const int numpoints, const char stripletter)
{
    int i;

    if(stripletter == 'u')
    {
        //convert strip number to distance
        for(i = 0; i < numpoints; ++i)
        {
            xdistance[i] = fabs(xdistance[i] - 77.0) * distperstrip;
            xdistanceE[i] = xdistanceE[i] * distperstrip;
        }
    }
    else if(stripletter == 'v' || stripletter == 'w')
    {
        //convert strip number to distance
        for(i = 0; i < numpoints; ++i)
        {
            xdistance[i] = fabs(xdistance[i] - 84.0) * distperstrip;
            xdistanceE[i] = xdistanceE[i] * distperstrip;
        }
    }

}

void CalcFiberLengths(const char lowlet, const int ustrip, double avfiberlength[], const double fiblength[], double xdist[], const int numpoints)
{
        int effective, i;

        if(lowlet == 'u')
        {
            //calculate fiber lengths
            if(ustrip <= 52)
            {
                avfiberlength[ustrip] = fiblength[ustrip];
            }
            else	
            {
                effective = (ustrip - 52) * 2 + 52; 
                    //converts to 84 strips
                avfiberlength[ustrip] = fiblength[effective] + fiblength[effective - 1]; 
                    //add the last 15 strips into pairs	
                avfiberlength[ustrip] = avfiberlength[ustrip]/2.0;
                    //takes average fiber length
            }
        }
        else if(lowlet == 'v')
        {
            //calculate fiber lengths
            if(ustrip <= 15)
            {
                effective = (ustrip  * 2);
                //converts to 77 strips
                avfiberlength[ustrip] = fiblength[effective] + fiblength[effective - 1];
                //takes average fiber length
                avfiberlength[ustrip] = avfiberlength[ustrip]/2.0;
            }
            else	
            {
                //add the first 15 strips into pairs	
                effective = ustrip + 15;
                avfiberlength[ustrip] = fiblength[effective];
            }
        }
        else if(lowlet == 'w')
        {
            //calculate fiber lengths
            if(ustrip <= 15)
            {
                effective = (ustrip  * 2);
                //converts to 77 strips
                avfiberlength[ustrip] = fiblength[effective] + fiblength[effective - 1];
                //takes average fiber length
                avfiberlength[ustrip] = avfiberlength[ustrip]/2.0;
            }
            else	
            {
                //add the first 15 strips into pairs	
                effective = ustrip + 15;
                avfiberlength[ustrip] = fiblength[effective];
            }
        }

        //calculated fiberlength on this particular strip
        //add in fiber lengths for all distances
        for(i = 0; i < numpoints; ++i)
		{
			xdist[i] = xdist[i] + avfiberlength[ustrip];
		}
        

}



void DrawChiSquare(int count,double chiarray[], const int strip, const int passnum)
{
    char name[100];
    sprintf(name,"canvas strip%02dpass%d",strip, passnum);
    TCanvas *canchi = new TCanvas(name,"global chisquare distribution");

    if(strip == 10)
    {
        sprintf(name,"diststrip%02dpass%d",strip, passnum);
        TH1D *chidist = new TH1D(name,"global chisquare distribution",50,0.0,3.0);
        chidist->FillN(count,chiarray,NULL);
        chidist->Draw();
    }
    else
    {
        sprintf(name,"diststrip%02dpass%d",strip, passnum);
        TH1D *chidist = new TH1D(name,"global chisquare distribution",100,0.0,3.0);
        chidist->FillN(count,chiarray,NULL);
        chidist->Draw();
    }
    canchi->~TCanvas();
    
}

void Initialize(double array[], const int size)
{
	int i;

	for(i = 0; i < size; ++i)
		array[i] = 0.0;

}

void GetFiberLengths(double array[], const int size, const char uplet)
{
	int i;
	int num;
    char genfilename[50];

    sprintf(genfilename, "inputfiles/FiberLengths%c.txt", uplet);
	ifstream fib_file (genfilename);
	for(i = 0; i < size; ++i)
	{
		fib_file >> num;
		fib_file >> array[num];
	}
	fib_file.close();

}

void FitWithExpGaus(TH1F *fithisto, double par[], double parE[], const int fitnum, double *Rchisquare)
{
    //bincontent function
    double bincontent, bincontentold, lowestx;
    int bin, maxbin, j;
    
    if(fitnum == 0)
    {
        par[0] = 4.5;
        par[1] = -0.005;
        par[2] = fithisto->GetMaximum();
        par[3] = fithisto->GetMean();
        par[4] = 80.0;
    }

    // set up some initial guess values and min parameters
    lowestx = 0.0;
    bincontentold = 0.0;
    bincontent = 1.0;
    bin = 1;
    maxbin = fithisto->FindBin(900.0);
               
    while((bincontentold < bincontent || bincontent < 1.0) && bin < maxbin)
    {
        bincontentold = bincontent;
        bincontent = fithisto->GetBinContent(bin);
        ++bin;
    }
    lowestx = fithisto->GetBinLowEdge(bin);
    if(lowestx > 500.0) lowestx = 150.0;

    TF1 *exp1 = new TF1("exp1","expo",lowestx,901.0);
    exp1->SetParameter(0,par[0]);
    exp1->SetParameter(1,par[1]);
    fithisto->Fit(exp1,"RQ");   //rewriting fit here...
    exp1->GetParameters(&par[0]);


    TF1 *gssn2 = new TF1("gssn2","[0]*exp(-0.5*((x - [1])^2/[2]^2))",lowestx,901.0);
    //gaussian: par[0] * e ^ -((x + par[1])^2 / 2.0 * par[2]^2)
    gssn2->SetParameter(0,par[2]);
    gssn2->SetParameter(1,par[3]);
    //gssn2->SetParameter(1,xgaus);
    gssn2->SetParameter(2,par[4]);
    gssn2->SetParLimits(0,0.0,fithisto->GetBinContent(fithisto->GetMaximumBin()));
    gssn2->SetParLimits(1,150.0,800.0);
    gssn2->SetParLimits(2,10.0,300.0);
    fithisto->Fit(gssn2,"QBR+"); // "R" is for custom settings fit
    gssn2->GetParameters(&par[2]);


    TF1 *total = new TF1("total","[0]*exp([1]*x) + [2]*exp(-0.5*((x - [3])^2/[4]^2))",lowestx,901.0);
    total->SetLineColor(2);
    total->SetParameters(par);
    total->SetParameter(0,1.5 * fithisto->GetMaximum());
    total->SetParLimits(3,150.0,800.0);
    total->SetParLimits(4,10.0,500.0);
    fithisto->Fit(total,"QBR");   //rewriting fit here...
    total->GetParameters(&par[0]);
                    
    gStyle->SetOptFit(1);

    if((double)total->GetNDF() > 0.0)
    {
        *Rchisquare = (double)total->GetChisquare()/(double)total->GetNDF();
    }
    else
    {
        *Rchisquare = 0.0;
    }

    for(j=0; j < 5; ++j)
    {
        parE[j] = total->GetParError(j);
    }
}

void FitWithGaus(TH1F *fithisto, double par[], double parE[], const int fitnum, double *Rchisquare)
{
    int j;

    if(fitnum == 0)
    {
        par[0] = fithisto->GetMaximum();
        par[1] = fithisto->GetMean();
        par[2] = 80.0;
    }

    // set up some initial guess values and min parameters
    TF1 *gssn2 = new TF1("gssn2","[0]*exp(-0.5*((x - [1])^2/[2]^2))",1.0,1999.0);
    //gaussian: par[0] * e ^ -((x + par[1])^2 / 2.0 * par[2]^2)
    //initial guess is the parameters from previous fit
    gssn2->SetParameters(par);

    if(par[0] > fithisto->GetMaximum()) par[0] = fithisto->GetMaximum();
    gssn2->SetParLimits(0,0.0,fithisto->GetMaximum() + 100.0);

    if(par[1] > 900.0) par[1] = 300.0;
    gssn2->SetParLimits(1,1.0,900.0);

    if(par[2] > 300.0) par[2] = 80.0;
    gssn2->SetParLimits(2,20.0,300.0);
    
    fithisto->Fit(gssn2,"QB"); // "R" is for custom settings fit
    gssn2->GetParameters(&par[0]);


    TF1 *total = new TF1("total","[0]*exp(-0.5*((x - [1])^2/[2]^2))",1.0,1999.0);
    total->SetLineColor(2);
    total->SetParameters(par);
    total->SetParLimits(2, 0.0, 200.0);
    fithisto->Fit(total,"QB");   //rewriting fit here...
    total->GetParameters(&par[0]);
                    
    gStyle->SetOptFit(1);

    if((double)total->GetNDF() > 0.0)
    {
        *Rchisquare = (double)total->GetChisquare()/(double)total->GetNDF();
    }
    else
    {
        *Rchisquare = 0.0;
    }

    for(j=0; j < 3; ++j)
    {
        parE[j] = total->GetParError(j);
    }
}



