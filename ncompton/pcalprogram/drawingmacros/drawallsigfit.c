void drawsigfitcomp()
{
    int i;
    int j = 0;
    int k = 0;
    int strip1 = 5;
    int strip2 = 60;
    int pass = 5;
    char name[100];
    double max;
    TH1F *usigfits[68];
    TCanvas *c;
    
    c = new TCanvas();
    c->Divide(3,3);
    //input file
	TFile *f = new TFile("UFits.root","READ");
    gDirectory->cd("pass5");
    gDirectory->cd("SigFits");

    j = 0;
    for(i = 62; i > 53; --i)
    {
        c->cd(j+1);
        sprintf(name,"proj%02d%02dpass%d",strip1,i,pass); //ustrip w strip
        usigfits[j] = (TH1F *)gDirectory->Get(name);
        //max = usigfits[0]->GetBinContent(usigfits[0]->GetMaximumBin());
        //usigfits[0]->SetMaximum(max*1.1);
        sprintf(name,"Raw Signal (Pass %d, W-Strip %d, U-Strip %d);ADC Readout;Counts",pass,strip1,i);
        usigfits[j]->SetTitle(name);
        //usigfits[j]->GetYaxis()->SetLAbelSize(0.05);
        usigfits[j]->Draw();
        ++j;
    }

}
