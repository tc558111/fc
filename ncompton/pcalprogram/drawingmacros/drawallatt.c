void drawgraph()
{
    int i;
    int j = 1;
    int k = 0;
    char name[100];
    double max;
    TGraphErrors *ufits[62];
    TCanvas *c;
    
    
    //input file
	TFile *f = new TFile("WFits.root","READ");
    gDirectory->cd("pass5");
    gDirectory->cd("AtFits");

    c = new TCanvas();
    c->Divide(7,9);
    for(i = 62; i > 0; --i)
    {
        j = 62 - i + 1;
        c->cd(j);
        sprintf(name,"wstrip%02dpass5",i);
        ufits[i] = (TGraphErrors *)gDirectory->Get(name);
        //max = 398.0;

        /*
        if(i == 68) max = 398.0;
        if(i == 59) max = 348.0;
        if(i == 50) max = 273.0;
        if(i == 41) max = 223.0;
        if(i == 32) max = 198.0;
        if(i == 23) max = 148.0;
        if(i == 14) max = 98.0;
        if(i == 5) max = 48.0;
        */
        if(i == 62) max = 450.0;
        if(i == 55) max = 400.0;
        if(i == 48) max = 380.0;
        if(i == 41) max = 320.0;
        if(i == 34) max = 280.0;
        if(i == 27) max = 250.0;
        if(i == 20) max = 210.0;
        if(i == 13) max = 160.0;
        if(i == 6) max = 80.0;
        
        ufits[i]->GetXaxis()->SetRangeUser(0.0, max);
        ufits[i]->GetYaxis()->SetRangeUser(0.0, 850.0);
        ufits[i]->GetYaxis()->SetLabelSize(0.065);
        ufits[i]->GetXaxis()->SetLabelSize(0.065);
        ufits[i]->Draw("APE");
    }

}
