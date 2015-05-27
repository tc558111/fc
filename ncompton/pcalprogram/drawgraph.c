void drawgraph()
{
    int i;
    int j = 1;
    int k = 0;
    char name[100];
    double max;
    TGraphErrors *ufits[68];
    TCanvas *c[8];
    
    
    //input file
	TFile *f = new TFile("UFits.root","READ");
    gDirectory->cd("pass5");
    gDirectory->cd("AtFits");

    c[0] = new TCanvas();
    c[0]->Divide(3,3,0.0,0.0);
    for(i = 68; i > 0; --i)
    {

        if(j > 9)
        {
            ++k;
            j = 1;
            c[k] = new TCanvas();
            if(i == 5) c[k]->Divide(3,2,0.0,0.0);
            else c[k]->Divide(3,3,0.0,0.0);
        }
        
        c[k]->cd(j);
        sprintf(name,"ustrip%02dpass5",i);
        ufits[i] = (TGraphErrors *)gDirectory->Get(name);
        if(i == 68) max = 398.0;
        if(i == 59) max = 348.0;
        if(i == 50) max = 273.0;
        if(i == 41) max = 223.0;
        if(i == 32) max = 198.0;
        if(i == 23) max = 148.0;
        if(i == 14) max = 98.0;
        if(i == 5) max = 48.0;
        ufits[i]->GetXaxis()->SetLimits(0.0, max);
        ufits[i]->GetYaxis()->SetRangeUser(0.0, 850.0);
        ufits[i]->GetYaxis()->SetLabelSize(0.065);
        ufits[i]->GetXaxis()->SetLabelSize(0.065);
        ufits[i]->Draw("APE");
        ++j;
    }

}
