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
void GetFiberLengthsU(double array[], const int size);
void DrawChiSquare(int count,double chiarray[], const int strip, const int passnum);

void fitU(int Pass = 0)
{

    string filetype;
    string histfolder = "pass";
    string fitfolder = "pass";
    if(Pass < 6)
        histfolder = histfolder + (char)(Pass + 48);
    else
        histfolder = histfolder + (char)(5 + 48);

    fitfolder = fitfolder + (char)(Pass + 48);
        
    cout << histfolder << endl;

    if(Pass == 0)
    {
        filetype = "recreate";
    }
    else
    {
        filetype = "update";
    }

	double par[6], parE[6];
	double parfit[4], parfitE[4];
	double x[62], xE[62], y[62], yE[62];
	double fib[69];
	double fiblen[69 + 16];
	double swidth = 4.5;
	double width = 5.055;
	int wstrip, i, j, count, ustrip;
	int effective;
	int nentries = 0;
	char name[20];
	char canname[25];
    char fitname[25];
    char graphname[25];
    double xlow, xhigh;

	//bin study
    int gcount = 0;
	double xsquare[68 * 62];
    double ndf;

    //fit study
	double x2square[69];
    int fitcount = 0;

	//statistical test
	double functionval;
	double diffsquare;
	double chisquare;

    //bincontent function
    double bincontent, bincontentold, lowestx;
    int bin, maxbin;

    //declare 3d histo
    TH3F *inhist;

	TCanvas *canfit[69];
	TCanvas *cangr[69];
    //TCanvas *canallatt = new TCanvas("ustrip","U - Attenuation");
    //canallatt->Divide(8,9);

    //input file
	TFile *f = new TFile("Histo.root");
    f->cd(histfolder.c_str());
    gDirectory->cd("Setup");

    if(Pass == 0)
    {
        inhist = (TH3F*)gDirectory->Get("HADUvsRSWustrips200");
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
    * */
    else
    {
        inhist = (TH3F*)gDirectory->Get("HADUvsRSWustripsTEST");
    }

    //output file
    TFile fitsfile("UFits.root",filetype.c_str());
    fitsfile.mkdir(fitfolder.c_str());
    fitsfile.cd(fitfolder.c_str());
    gDirectory->mkdir("SigFits");
    gDirectory->mkdir("AtFits");
    gDirectory->cd("SigFits");
    
	Initialize(fib,69);
	Initialize(fiblen,69+15);
	GetFiberLengthsU(fiblen,69+15);

	ofstream centroids ("Ucentroid.txt");
	ofstream UPar_file ("Uparameters.txt");
    ofstream UAtten ("UAtten.txt"); // strip, I0, A, B
    ofstream UGains ("UGains.dat"); // x = 0 value
	Initialize(parfit,3);
	Initialize(parfitE,3);
	parfit[0] = 6.5;
	parfit[1] = -0.005;
	parfit[2] = 180.0;


	for(ustrip = 68; ustrip > 0; --ustrip)
	{
		count = 0;
        Initialize(x,62);
        Initialize(xE,62);
        Initialize(y,62);
        Initialize(yE,62);
		sprintf(name,"ustrip fits: %02d",ustrip);
		canfit[ustrip] = new TCanvas(name,name);
		canfit[ustrip]->Divide(7,9);



		for(wstrip = 62; wstrip > 0; --wstrip)
		{
			canfit[ustrip]->cd(wstrip);
			sprintf(name, "proj%02d%02dpass%1d", ustrip, wstrip,Pass);
			TH1F *histo = (TH1F *)inhist->ProjectionY(name,wstrip,wstrip,ustrip,ustrip);
            
					     //3D histo           counts vs adu, w-strip, u-strip
			histo->Draw();

			nentries = (int)histo->GetEntries();
			if(nentries > 10 && (histo->GetBinContent(histo->GetMaximumBin()) > 3))
			{

				if(count == 0)
				{
					par[0] = 4.5;
					par[1] = -0.005;
					par[2] = histo->GetMaximum();
					par[3] = histo->GetMean();
					par[4] = 80.0;
				}

                // set up some initial guess values and min parameters
                lowestx = 0.0;
                bincontentold = 0.0;
                bincontent = 1.0;
                bin = 1;
                maxbin = histo->FindBin(900.0);
               
                while((bincontentold < bincontent || bincontent < 1.0) && bin < maxbin)
                {
                    bincontentold = bincontent;
                    bincontent = histo->GetBinContent(bin);
                    ++bin;
                }
                lowestx = histo->GetBinLowEdge(bin);
                
/*************** Fit based on pass number *************************/
                if(Pass == 0)
                {                    
                
                    TF1 *exp1 = new TF1("exp1","expo",lowestx,901.0);
                    exp1->SetParameter(0,par[0]);
                    exp1->SetParameter(1,par[1]);
                    histo->Fit(exp1,"RQ");   //rewriting fit here...
                    exp1->GetParameters(&par[0]);


                    TF1 *gssn2 = new TF1("gssn2","[0]*exp(-0.5*((x - [1])^2/[2]^2))",lowestx,901.0);
                    //gaussian: par[0] * e ^ -((x + par[1])^2 / 2.0 * par[2]^2)
                    gssn2->SetParameter(0,par[2]);
                    gssn2->SetParameter(1,par[3]);
                    //gssn2->SetParameter(1,xgaus);
                    gssn2->SetParameter(2,par[4]);
                    gssn2->SetParLimits(0,0.0,histo->GetBinContent(histo->GetMaximumBin()));
                    gssn2->SetParLimits(1,150.0,800.0);
                    gssn2->SetParLimits(2,10.0,300.0);
                    histo->Fit(gssn2,"QBR+"); // "R" is for custom settings fit
                    gssn2->GetParameters(&par[2]);


                    TF1 *total = new TF1("total","[0]*exp([1]*x) + [2]*exp(-0.5*((x - [3])^2/[4]^2))",lowestx,901.0);

                    total->SetLineColor(2);
                    total->SetParameters(par);
                    total->SetParameter(0,log(par[0]));
                    total->SetParLimits(3,150.0,800.0);
                    total->SetParLimits(4,10.0,500.0);
                    histo->Fit(total,"QBR");   //rewriting fit here...
                    total->GetParameters(&par[0]);
                    
                    gStyle->SetOptFit(1);
                    chisquare = (double)total->GetChisquare();
                    ndf = (double)total->GetNDF();

                    
                    for(j=0; j < 5; ++j)
                    {
                        parE[j] = total->GetParError(j);
                    }


                    //if(0.5 * par[3] > parE[3] && 0.5 * par[4] > parE[4])
                    if(fabs(par[2]) > parE[2] && fabs(par[3]) > parE[3] && fabs(par[4]) > parE[4])
                    {
                        if(par[3] > 100.0 && par[3] < 1000.0)
                        {
                            if(wstrip <= 15)
                            {
                                //converts to 77 strips
                                x[count] = 2.0* wstrip - 1.0;
                                xE[count] = 1.0;
                            }
                            else if(wstrip > 15)
                            {
                                //converts to 77 strips
                                x[count] = (30.0 + (wstrip - 15.0)) - 0.5;
                                xE[count] = 1.0/2.0;
                            }
                            //x[count] = wstrip;
                            y[count] = par[3];
                            yE[count] = parE[3];
                            ++count;
                            xsquare[gcount] = chisquare/ndf;
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
            
                    if(count == 0)
                    {
                        par[0] = histo->GetMaximum();
                        par[1] = histo->GetMean();
                        par[2] = 80.0;
                    }
                    TF1 *gssn2 = new TF1("gssn2","[0]*exp(-0.5*((x - [1])^2/[2]^2))",0.0,800.0);
                    //gaussian: par[0] * e ^ -((x + par[1])^2 / 2.0 * par[2]^2)
                    gssn2->SetParameters(par);
                    gssn2->SetParLimits(0,0.0,histo->GetMaximum() + 100.0);
                    gssn2->SetParLimits(1,lowestx,900.0);
                    gssn2->SetParLimits(2,20.0,300.0);
                    histo->Fit(gssn2,"QB"); // "R" is for custom settings fit
                    gssn2->GetParameters(&par[0]);


                    TF1 *total = new TF1("total","[0]*exp(-0.5*((x - [1])^2/[2]^2))",0.0,900.0);
                    total->SetLineColor(2);
                    total->SetParameters(par);
                    total->SetParLimits(2, 0.0, 200.0);
                    histo->Fit(total,"QB");   //rewriting fit here...
                    total->GetParameters(&par[0]);
                    
                    gStyle->SetOptFit(1);
                    chisquare = (double)total->GetChisquare();
                    ndf = (double)total->GetNDF();

                    for(j=0; j < 3; ++j)
                    {
                        parE[j] = total->GetParError(j);
                    }


                    if(par[0] > parE[0] && par[1] > parE[1] && par[2] > parE[2])
                    {
                        if(par[1] > 70.0)// && par[1] < 1000.0)
                        {
                            if(wstrip <= 15)
                            {
                                //converts to 77 strips
                                x[count] = 2.0* wstrip - 1.0;
                                xE[count] = 1.0;
                            }
                            else if(wstrip > 15)
                            {
                                //converts to 77 strips
                                x[count] = (30.0 + (wstrip - 15.0)) - 0.5;
                                xE[count] = 1.0/2.0;
                            }
                            //x[count] = wstrip;
                            y[count] = par[1];
                            yE[count] = parE[1];
                            ++count;
                            xsquare[gcount] = chisquare/ndf;
                            ++gcount;

                        } //check gaussian centroid
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



        //initialize limits
        xlow = 0.0;
        xhigh = 0.0;
        if(count > 1)
        {
            xlow = fabs(x[1] - 77.0) * width;
            xhigh = fabs(x[1] - 77.0) * width;
        }
        else 
        {
            xlow = 0.0;
            xhigh = 0.0;
        }
        
        //convert strip number to distance
		for(i = 0; i < count; ++i)
		{
			x[i] = fabs(x[i] - 77.0) * width;
            xE[i] = width * xE[i];

            //rule out first and last points
            if(i > 0 && i < (count - 1))
            {
                if(x[i] < xlow)
                    xlow = x[i];
                if(x[i] > xhigh)
                    xhigh= x[i];
            }
		}
        xlow = (xlow + x[0])/2.0;
        if(count > 6) xhigh = (xhigh + x[count-3])/2.0;
        else xhigh= (xhigh + x[count-1])/2.0;

        
        //////////////// Add in fiber Length ///////////////

        //calculate fiber lengths
		if(ustrip <= 52)
		{
			fib[ustrip] = fiblen[ustrip];
		}
		else	
		{
			effective = (ustrip - 52) * 2 + 52; 
				//converts to 84 strips
			fib[ustrip] = fiblen[effective] + fiblen[effective - 1]; 
				//add the last 15 strips into pairs	
			fib[ustrip] = fib[ustrip]/2.0;
				//takes average fiber length
		}
       
        //add in fiber lengths
        /*
        for(i = 0; i < count; ++i)
		{
			x[i] = x[i] + fib[ustrip];
		}
        */
        //////////////// End fiber Length Addition //////////

        
		if(count > 4)
		{
			sprintf(canname,"ustrip: %d",ustrip);
            sprintf(graphname,"ustrip%02dpass%1d",ustrip,Pass);
			cangr[ustrip] = new TCanvas(canname,canname);

            //plot all fits on one canvas
           // canallatt->cd(ustrip);

			TGraphErrors *graph = new TGraphErrors(count, x, y, xE, yE);
			graph->SetTitle(canname);
            graph->SetName(graphname);
			graph->SetMarkerStyle(8);
			graph->SetMarkerSize(0.3);
            graph->GetXaxis()->SetLimits(0.0, xhigh + 5.0*width);
			graph->Draw("APE");



			TF1 *exp = new TF1("exp","[0]*exp([1]*x)",xlow,xhigh);
			exp->SetParameters(parfit);
			graph->Fit(exp,"QMR");   //fit with exponential
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
            sprintf(fitname,"ufit%02dpass%1d",ustrip,Pass);
			TF1 *total2 = new TF1(fitname, "[0]*exp([1]*x) + [2]",xlow,xhigh);
			total2->SetParameters(parfit);
            total2->SetParameter(0,600.0);
            total2->SetParameter(1,-0.005);
            total2->SetParameter(2,200.0);
            total2->SetParLimits(0,200.0,900.0);
            total2->SetParLimits(1,-0.009,-0.0005);
            total2->SetParLimits(2,0.0,700.0);
			graph->Fit(total2,"QMBR");  //fit with exp plus const 
			total2->GetParameters(&parfit[0]);
			for(i = 0; i < 3; ++i)
			{
				parfitE[i] = total2->GetParError(i);
			}
            /*
            TF1 *copyfit = new TF1("copyfit", "[0]*exp([1]*x) + [2]",0.0, xhigh);
            copyfit->SetParameters(parfit);
            copyfit->SetLineStyle(2);
            copyfit->SetLineColor(4);
            copyfit->Draw("SAME");
            */
            
            UPar_file << ustrip << "  &  " << parfit[1] << "  &  " <<  parfit[2]  << "  hline " << endl;
            // strip, I0, A, B
            UAtten << ustrip << "    " << parfit[0] << "    " << parfit[1] << "    " << parfit[2] << endl;
            UGains << ustrip << "    " << 650.0/(parfit[0] + parfit[2]) << endl;
            
			/*
			TF1 *expA = new TF1("expA","[0]*exp([1]*x)",xlow,xhigh);
			expA->SetParameter(0,429.728);
			expA->SetParameter(1,-0.00771971);
			graph->Fit(expA,"MR");   //rewriting fit here...
			expA->GetParameters(&parfit[0]);

			TF1 *expB = new TF1("expB","[0]*exp([1]*x)",xlow,xhigh);
			expB->FixParameter(0,parfit[0]);
			expB->SetParameter(1,-0.0013452);
			graph->Fit(expB,"MBR+");   //rewriting fit here...
			parfit[2] = expB->GetParameter(1);
			*/
			
/*
			//two exponential fit
			//TF1 *total3 = new TF1("total3", "[0] * (exp([1]*x) + exp([2]*x)) / 2.0",xlow,xhigh);
            TF1 *total3 = new TF1("total3", "[0]*exp([1]*[3]) * (exp([1]*(x + [3])) + exp([2]*x)) / (exp([1]*[3]) + 1)",xlow,xhigh);
			total3->SetParameter(0,700.0);
			total3->SetParameter(1,-0.00076);
			total3->SetParameter(2,-0.00635);
            total3->FixParameter(3,fib[ustrip]);
			//total3->SetParameters(parfit);
			//total3->SetLineStyle(2);
			//total3->SetLineColor(3);
			graph->Fit(total3,"BMR");  //fit with exp1 + exp2
			total3->GetParameters(&parfit[0]);
			for(i = 0; i < 4; ++i)
			{
				parfitE[i] = total3->GetParError(i);
			}

            UPar_file << ustrip << endl;
			UPar_file << "Amplitude:      " << parfit[0]      << " +- " << parfitE[0]      << endl;
			UPar_file << "first slope:   " << parfit[1]      << " +- " << parfitE[1]      << endl;
			UPar_file << "second slope:       " << parfit[2]      << " +- " << parfitE[2]      << endl;
            UPar_file << "fiber length:       " << parfit[3]      << " +- " << parfitE[3]      << endl;
			UPar_file << endl;
*/
			/* statistical test
			chisquare = 0.0;
			for(i = 2; i < count-1; ++i)
			{
				functionval = exp(parfit[0] + parfit[1]*x[i]) + parfit[2];
				diffsquare = y[i] - functionval;
				diffsquare = pow(diffsquare, 2)/pow(yE[i],2);

				cout << diffsquare << endl;

				chisquare += diffsquare;
			}
			cout << chisquare << endl;
			*/

            x2square[fitcount] = total2->GetChisquare()/(double)total2->GetNDF();
            ++fitcount;
            gDirectory->cd("../AtFits");
            gDirectory->Append(graph);
            gDirectory->cd("../SigFits");
            cangr[ustrip]->~TCanvas();
            total2->~TF1();       
		} //fitting
        else if(count == 4)
        {

            sprintf(canname,"ustrip: %d",ustrip);
            sprintf(graphname,"ustrip%02dpass%1d",ustrip,Pass);
			cangr[ustrip] = new TCanvas(canname,canname);

            //plot all fits on one canvas
           // canallatt->cd(ustrip);

			TGraphErrors *graph = new TGraphErrors(count, x, y, xE, yE);
			graph->SetTitle(canname);
            graph->SetName(graphname);
			graph->SetMarkerStyle(8);
			graph->SetMarkerSize(0.3);
            graph->GetXaxis()->SetLimits(0.0, xhigh + 5.0*width);
			graph->Draw("APE");



			//exponential with constant fit
            sprintf(fitname,"ufit%02dpass%1d",ustrip,Pass);
			TF1 *total2 = new TF1(fitname, "[0]*exp([1]*x)",x[1],x[count-2]);
			total2->SetParameters(parfit);
            total2->SetParameter(0,600.0);
            total2->SetParameter(1,-0.005);
            //total2->SetParameter(2,0.0);
            total2->SetParLimits(0,200.0,900.0);
            total2->SetParLimits(1,-0.009,-0.0005);
            //total2->SetParLimits(2,0.0,0.0);
			graph->Fit(total2,"QMBR");  //fit with exp plus const 
			total2->GetParameters(&parfit[0]);
			for(i = 0; i < 2; ++i)
			{
				parfitE[i] = total2->GetParError(i);
			}

            UPar_file << ustrip << "  &  " << parfit[1] << "  &  " <<  0.0  << "  hline " << endl;
            // strip, I0, A, B
            UAtten << ustrip << "    " << parfit[0] << "    " << parfit[1] << "    " << 0.0 << endl;
            UGains << ustrip << "    " << 650.0/parfit[0] << endl;

            gDirectory->cd("../AtFits");
            gDirectory->Append(graph);
            gDirectory->cd("../SigFits");
            cangr[ustrip]->~TCanvas();
            total2->~TF1(); 
        }
        else if(count == 3)
        {

            sprintf(canname,"ustrip: %d",ustrip);
            sprintf(graphname,"ustrip%02dpass%1d",ustrip,Pass);
			cangr[ustrip] = new TCanvas(canname,canname);

            //plot all fits on one canvas
           // canallatt->cd(ustrip);

			TGraphErrors *graph = new TGraphErrors(count, x, y, xE, yE);
			graph->SetTitle(canname);
            graph->SetName(graphname);
			graph->SetMarkerStyle(8);
			graph->SetMarkerSize(0.3);
            graph->GetXaxis()->SetLimits(0.0, xhigh + 5.0*width);
			graph->Draw("APE");

            //exponential with constant fit
            sprintf(fitname,"ufit%02dpass%1d",ustrip,Pass);
			TF1 *total2 = new TF1(fitname, "[0]",(x[1] + x[0])/2.0,(x[1] + x[count-1])/2.0);
            total2->SetParameter(0,650.0);
            total2->SetParLimits(0,100.0,1000.0);
			graph->Fit(total2,"QMBR");  //fit with exp plus const 
			total2->GetParameters(&parfit[0]);
			for(i = 0; i < 1; ++i)
			{
				parfitE[i] = total2->GetParError(i);
			}

            

            UAtten << ustrip << "    " << parfit[0] << "    " << 0.0 << "    " << 0.0 << endl;
            UPar_file << ustrip << "  &  " << parfit[0] << "  &  " <<  0.0  << "  hline " << endl;
            if(parfit[0] < 450.0 || parfit[0] > 750.0) parfit[0] = 500.0;
            UGains << ustrip << "    " << 650.0/parfit[0] << endl;

            gDirectory->cd("../AtFits");
            gDirectory->Append(graph);
            gDirectory->cd("../SigFits");
            cangr[ustrip]->~TCanvas();
            total2->~TF1(); 
        }
        else if(count > 0)
        {

            sprintf(canname,"ustrip: %d",ustrip);
            sprintf(graphname,"ustrip%02dpass%1d",ustrip,Pass);
			cangr[ustrip] = new TCanvas(canname,canname);

            //plot all fits on one canvas
           // canallatt->cd(ustrip);

			TGraphErrors *graph = new TGraphErrors(count, x, y, xE, yE);
			graph->SetTitle(canname);
            graph->SetName(graphname);
			graph->SetMarkerStyle(8);
			graph->SetMarkerSize(0.3);
            graph->GetXaxis()->SetLimits(0.0, xhigh + 5.0*width);
			graph->Draw("APE");

            //exponential with constant fit
            sprintf(fitname,"ufit%02dpass%1d",ustrip,Pass);
			TF1 *total2 = new TF1(fitname, "[0]",x[0],x[count-1]);
            total2->SetParameter(0,650.0);
            total2->SetParLimits(0,100.0,1000.0);
			graph->Fit(total2,"QMBR");  //fit with exp plus const 
			total2->GetParameters(&parfit[0]);
			for(i = 0; i < 1; ++i)
			{
				parfitE[i] = total2->GetParError(i);
			}

            

            UAtten << ustrip << "    " << parfit[0] << "    " << 0.0 << "    " << 0.0 << endl;
            UPar_file << ustrip << "  &  " << parfit[0] << "  &  " <<  0.0  << "  hline " << endl;
            if(parfit[0] < 450.0 || parfit[0] > 750.0) parfit[0] = 500.0;
            UGains << ustrip << "    " << 650.0/parfit[0] << endl;

            gDirectory->cd("../AtFits");
            gDirectory->Append(graph);
            gDirectory->cd("../SigFits");
            cangr[ustrip]->~TCanvas();
            total2->~TF1(); 
        }
        else
        {
            UAtten << ustrip << "    " << 0.0 << "    " << 0.0 << "    " << 0.0 << endl;
            UPar_file << ustrip << "  &  " << 0.0 << "  &  " <<  0.0  << "  hline " << endl;
        }




	} //u strip
    UAtten.close();
	UPar_file.close();
    UGains.close();
    centroids.close();

    DrawChiSquare(gcount, xsquare,1,Pass);
    DrawChiSquare(fitcount, x2square,10,Pass);
    fitsfile.Write(); // write to the output file
	fitsfile.Close(); // close the output file
    f->Close();

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

void GetFiberLengthsU(double array[], const int size)
{
	int i;
	int num;
	
	ifstream fib_file ("FiberLengthsU.txt");
	for(i = 0; i < size; ++i)
	{
		fib_file >> num;
		fib_file >> array[num];
	}
	fib_file.close();

}



