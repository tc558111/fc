
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

using namespace std;

void GetFiberLengthsW(double array[], const int size);


void fitW(int Pass = 0)
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
    
	double par[6], parE[6];
	double parfit[4], parfitE[4];
	double x[68], xE[68], y[68], yE[68];
	double fib[63];
	double fiblen[63 + 15];
	double swidth = 4.5;
	double width = 5.055;
	int wstrip, i, j,count, ustrip;
	int effective;
	int nentries = 0;
	char name[20];
	char canname[15];
    char graphname[15];
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

	TCanvas *canfit[63];
	TCanvas *cangr[63];
    //TCanvas *canallatt = new TCanvas("wstrip","W - Attenuation");
    //canallatt->Divide(8,8);
    
    //input file
	TFile *f = new TFile("Histo.root");
    f->cd(folder.c_str());
    gDirectory->cd("Setup");

    if(Pass == 0)
	inhist = (TH3F*)gDirectory->Get("HADWvsRSUwstrips200");
    else
	inhist = (TH3F*)gDirectory->Get("HADWvsRSUwstripsTEST");

    //output file
    TFile fitsfile("WFits.root",filetype.c_str());
    fitsfile.mkdir(folder.c_str());
    fitsfile.cd(folder.c_str());
    gDirectory->mkdir("SigFits");
    gDirectory->mkdir("AtFits");
    gDirectory->cd("SigFits");
    
	// x = u-strip, y = adw, z = w-strip
	Initialize(fib,63);
	Initialize(fiblen,63+15);
	GetFiberLengthsW(fiblen,63+15);

	ofstream centroids ("Wcentroid.txt");
	ofstream UPar_file ("Wparameters.txt");
    ofstream WAtten ("WAtten.txt"); // strip, I0, A, B
    ofstream WGains ("WGains.dat"); // x = 0 value
	Initialize(parfit,3);
	Initialize(parfitE,3);
	parfit[0] = 6.5;
	parfit[1] = -0.005;
	parfit[2] = 180.0;
	for(wstrip = 62; wstrip > 0; --wstrip)
	{
		count = 0;
        Initialize(x,68);
        Initialize(xE,68);
        Initialize(y,68);
        Initialize(yE,68);
		sprintf(name,"wstrip fits: %02d",wstrip);
		canfit[wstrip] = new TCanvas(name,name);
		canfit[wstrip]->Divide(8,9);

		for(ustrip = 68; ustrip > 0; --ustrip)
		{
			canfit[wstrip]->cd(ustrip);
			sprintf(name, "proj%02d%02dpass%1d", wstrip, ustrip,Pass);
			TH1F *histo = (TH1F *)inhist->ProjectionY(name,ustrip,ustrip,wstrip,wstrip);
					     //3D histo           counts vs adu, u-strip, w-strip
			histo->Draw();
		
			nentries = (int)histo->GetEntries();
			if(nentries > 10 && (histo->GetBinContent(histo->GetMaximumBin()) > 3))
			{

				if(count == 0)
				{
					par[0] = 4.5;
					par[1] = -0.005;
					par[2] = 170.0;
					par[3] = 200.0;
					par[4] = 80.0;
				}

                // set up some initial guess values and min parameters
                lowestx = 0.0;
                bincontentold = 0.0;
                bincontent = 1.0;
                bin = 1;
                maxbin = histo->FindBin(900.0);
                while((bincontentold < bincontent || bincontent == 0.0) && bin < maxbin)
                {
                    bincontentold = bincontent;
                    bincontent = histo->GetBinContent(bin);
                    ++bin;
                }
                lowestx = histo->GetBinLowEdge(bin);
                
/*************** Fit based on pass number *************************/
                if(Pass == 0)
                {
                    TF1 *exp1 = new TF1("exp1","expo",lowestx,950.0);
                    exp1->SetParameter(0,par[0]);
                    exp1->SetParameter(1,par[1]);
                    histo->Fit(exp1,"RQ");   //rewriting fit here...
                    exp1->GetParameters(&par[0]);


                    TF1 *gssn2 = new TF1("gssn2","[0]*exp(-0.5*((x - [1])^2/[2]^2))",lowestx,800.0);
                    //gaussian: par[0] * e ^ -((x + par[1])^2 / 2.0 * par[2]^2)
                    gssn2->SetParameter(0,par[2]);
                    gssn2->SetParameter(1,par[3]);
                    gssn2->SetParameter(2,par[4]);
                    gssn2->SetParLimits(0,0.0,10000.0);
                    gssn2->SetParLimits(1,200.0,800.0);
                    gssn2->SetParLimits(2,10.0,300.0);
                    histo->Fit(gssn2,"QBR+"); // "R" is for custom settings fit
                    gssn2->GetParameters(&par[2]);


                    TF1 *total = new TF1("total","[0]*exp([1]*x) + [2]*exp(-0.5*((x - [3])^2/[4]^2))",lowestx,900.0);
                    total->SetLineColor(2);
                    total->SetParameters(par);
                    total->SetParameter(0,log(par[0]));
                    total->SetParLimits(3,200.0,900.0);
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
                            if(ustrip <= 52)
                            {
                                //converts to 84 strips
                                x[count] = ustrip - 0.5;
                                xE[count] = 1.0/2.0;
                            }
                            else if(ustrip > 52)
                            {
                                //converts to 84 strips
                                x[count] = (52.0 + 2.0*(ustrip - 52.0)) - 1.0;
                                xE[count] = 1.0;
                            }
                            //x[count] = wstrip;
                            y[count] = par[3];
                            yE[count] = parE[3];
                            ++count;
                            xsquare[gcount] = chisquare/ndf;
                            ++gcount;
                        } //check gaussian centroid
                        centroids << wstrip << "  " << ustrip << "  " << par[3] << "   " << par[4] << endl;
                    } //check parameter errors
                    else
                    {
                        centroids << ustrip << "  " << wstrip << "  " << 0.0 << "   " << 0.0 << endl;
                    }
                    
                }//Pass == 0
                else if(Pass > 0)
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
                        if(par[1] > 80.0)// && par[1] < 1000.0)
                        {
                            if(ustrip <= 52)
                            {
                                //converts to 84 strips
                                x[count] = ustrip - 0.5;
                                xE[count] = 1.0/2.0;
                            }
                            else if(ustrip > 52)
                            {
                                //converts to 84 strips
                                x[count] = (52.0 + 2.0*(ustrip - 52.0)) - 1.0;
                                xE[count] = 1.0;
                            }
                            //x[count] = wstrip;
                            y[count] = par[1];
                            yE[count] = parE[1];
                            ++count;
                            xsquare[gcount] = chisquare/ndf;
                            ++gcount;
                        } //check gaussian centroid
                        centroids << wstrip << "  " << ustrip << "  " << par[1] << "   " << par[2] << endl;
                    } //check parameter errors
                    else
                    {
                        centroids << ustrip << "  " << wstrip << "  " << 0.0 << "   " << 0.0 << endl;
                    }
                }//Pass > 0
/************************* end fit of histograms ***************/
			} //nentries
		}//u strip
        canfit[wstrip]->~TCanvas();

        //initialize limits
        xlow = 0.0;
        xhigh = 0.0;
        if(count > 1)
        {
            xlow = fabs(x[1] - 84.0) * width;
            xhigh = fabs(x[1] - 84.0) * width;
        }
        else 
        {
            xlow = 0.0;
            xhigh = 0.0;
        }

        //convert strip number to distance 
		for(i = 0; i < count; ++i)
		{
			x[i] = fabs(x[i] - 84.0)*width;
            xE[i] = xE[i] * width;
            
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
		if(wstrip <= 15)
		{
            effective = (wstrip  * 2);
            //converts to 77 strips
			fib[wstrip] = fiblen[effective] + fiblen[effective - 1];
            //takes average fiber length
			fib[wstrip] = fib[wstrip]/2.0;
		}
		else	
		{
            //add the first 15 strips into pairs	
            effective = wstrip + 15;
			fib[wstrip] = fiblen[effective];
		}
        
        //add in fiber lengths
        /*
        for(i = 0; i < count; ++i)
		{
			x[i] = x[i] + fib[wstrip];
		}
        */
        
        //////////////// End fiber Length Addition //////////

        
		if(count > 4)
		{
			sprintf(canname,"wstrip: %d",wstrip);
            sprintf(graphname,"wstrip%02dpass%1d",wstrip,Pass);
			cangr[wstrip] = new TCanvas(canname,canname);

            //plot all fits on one canvas
           //canallatt->cd(wstrip);

			TGraphErrors *graph = new TGraphErrors(count, x, y, xE, yE);
			graph->SetTitle(canname);
            graph->SetName(graphname);
			graph->SetMarkerStyle(8);
			graph->SetMarkerSize(0.3);
            graph->GetXaxis()->SetLimits(0.0, xhigh + 5.0*width);
			graph->Draw("APE");


			TF1 *exp = new TF1("exp","expo",xlow,xhigh);
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
			graph->Fit(exp2,"QBMR");   //fit with exponential
			exp2->GetParameters(&parfit[0]);
				//f(x) = exp([0] + [1]*x)
*/                

			//exponential with constant fit
			TF1 *total2 = new TF1("total2", "[0]*exp([1]*x) + [2]",xlow,xhigh);
			total2->SetParameters(parfit);
            total2->SetParameter(0,600.0);
            total2->SetParameter(1,-0.005);
            total2->SetParameter(2,200.0);
            total2->SetParLimits(0,200.0,900.0);
            total2->SetParLimits(1,-0.009,-0.0005);
            total2->SetParLimits(2,0.0,500.0);
			graph->Fit(total2,"QMBR");  //fit with exp plus const 
			total2->GetParameters(&parfit[0]);
			for(i = 0; i < 3; ++i)
			{
				parfitE[i] = total2->GetParError(i);
			}

            /*
            TF1 *copyfit = new TF1("copyfit", "[0]*exp([1]*x) + [2]",0.0,xhigh);
            copyfit->SetParameters(parfit);
            copyfit->SetLineStyle(2);
            copyfit->SetLineColor(4);
            copyfit->Draw("SAME");
            */
            
            UPar_file << wstrip << "  &  " << parfit[1] << "  &  " <<  parfit[2]  << "  hline " << endl;
            // strip, I0, A, B
            WAtten << wstrip << "    " << parfit[0] << "    " << parfit[1] << "    " << parfit[2] << endl;
            WGains << wstrip << "    " << 650.0/(parfit[0] + parfit[2]) << endl;
            
			/*
			TF1 *expA = new TF1("expA","[0]*exp([1]*x)",xlow,xhigh);
			expA->SetParameter(0,429.728);
			expA->SetParameter(1,-0.00771971);
			graph->Fit(expA,"QMR");   //rewriting fit here...
			expA->GetParameters(&parfit[0]);

			TF1 *expB = new TF1("expB","[0]*exp([1]*x)",xlow,xhigh);
			expB->FixParameter(0,parfit[0]);
			expB->SetParameter(1,-0.0013452);
			graph->Fit(expB,"QMBR+");   //rewriting fit here...
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
			graph->Fit(total3,"QBMR");  //fit with exp1 + exp2
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
            cangr[wstrip]->~TCanvas();
		} //fitting
        else if(count == 4)
        {

            sprintf(canname,"wstrip: %d",wstrip);
            sprintf(graphname,"wstrip%02dpass%1d",wstrip,Pass);
			cangr[wstrip] = new TCanvas(canname,canname);

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
			TF1 *total2 = new TF1("total2", "[0]*exp([1]*x)",x[1],x[count-2]);
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

            UPar_file << wstrip << "  &  " << parfit[1] << "  &  " <<  0.0  << "  hline " << endl;
            // strip, I0, A, B
            WAtten << wstrip << "    " << parfit[0] << "    " << parfit[1] << "    " << 0.0 << endl;
            WGains << wstrip << "    " << 650.0/parfit[0] << endl;

            gDirectory->cd("../AtFits");
            gDirectory->Append(graph);
            gDirectory->cd("../SigFits");
            cangr[wstrip]->~TCanvas();
        }
        else if(count == 3)
        {

            sprintf(canname,"wstrip: %d",wstrip);
            sprintf(graphname,"wstrip%02dpass%1d",wstrip,Pass);
			cangr[wstrip] = new TCanvas(canname,canname);

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
			TF1 *total2 = new TF1("total2", "[0]",(x[1] + x[0])/2.0,(x[1] + x[count-1])/2.0);
            total2->SetParameter(0,650.0);
            total2->SetParLimits(0,200.0,900.0);
			graph->Fit(total2,"QMBR");  //fit with exp plus const 
			total2->GetParameters(&parfit[0]);
			for(i = 0; i < 1; ++i)
			{
				parfitE[i] = total2->GetParError(i);
			}

            WAtten << wstrip << "    " << parfit[0] << "    " << 0.0 << "    " << 0.0 << endl;
            UPar_file << wstrip << "  &  " << parfit[0] << "  &  " <<  0.0  << "  hline " << endl;
            if(parfit[0] < 450.0 || parfit[0] > 750.0) parfit[0] = 500.0;
            WGains << wstrip << "    " << 650.0/parfit[0] << endl;


            gDirectory->cd("../AtFits");
            gDirectory->Append(graph);
            gDirectory->cd("../SigFits");
            cangr[wstrip]->~TCanvas();
        }
        else if(count > 0)
        {

            sprintf(canname,"wstrip: %d",wstrip);
            sprintf(graphname,"wstrip%02dpass%1d",wstrip,Pass);
			cangr[wstrip] = new TCanvas(canname,canname);

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
			TF1 *total2 = new TF1("total2", "[0]",x[0],x[count-1]);
            total2->SetParameter(0,650.0);
            total2->SetParLimits(0,200.0,900.0);
			graph->Fit(total2,"QMBR");  //fit with exp plus const 
			total2->GetParameters(&parfit[0]);
			for(i = 0; i < 1; ++i)
			{
				parfitE[i] = total2->GetParError(i);
			}

            WAtten << wstrip << "    " << parfit[0] << "    " << 0.0 << "    " << 0.0 << endl;
            UPar_file << wstrip << "  &  " << parfit[0] << "  &  " <<  0.0  << "  hline " << endl;
            if(parfit[0] < 450.0 || parfit[0] > 750.0) parfit[0] = 500.0;
            WGains << wstrip << "    " << 650.0/parfit[0] << endl;


            gDirectory->cd("../AtFits");
            gDirectory->Append(graph);
            gDirectory->cd("../SigFits");
            cangr[wstrip]->~TCanvas();
        }
        else
        {
            WAtten << wstrip << "    " << 0.0 << "    " << 0.0 << "    " << 0.0 << endl;
            UPar_file << wstrip << "  &  " << 0.0 << "  &  " <<  0.0  << "  hline " << endl;
        }



	} //w strip
    WAtten.close();
	UPar_file.close();
    WGains.close();
    centroids.close();
    
    DrawChiSquare(gcount, xsquare,3,Pass);
    DrawChiSquare(fitcount, x2square,30,Pass);
    fitsfile.Write(); // write to the output file
	fitsfile.Close(); // close the output file
    f->Close();

}

void GetFiberLengthsW(double array[], const int size)
{
	int i;
	int num;
	
	ifstream fib_file ("FiberLengthsW.txt");
	for(i = 0; i < size; ++i)
	{
		fib_file >> num;
		fib_file >> array[num];
	}
	fib_file.close();

}



