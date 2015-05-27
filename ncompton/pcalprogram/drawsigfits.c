void drawgraph()
{
    int i;
    int j = 1;
    int k = 0;
    int strip1 = 68;
    int strip2 = 60;
    char name[100];
    double max;
    TH1F *usigfits[62];
    TCanvas *c[7];
    c[1] = new TCanvas();
    c[0] = new TCanvas();
    c[0]->Divide(3,2);
    //input file
	TFile *f = new TFile("UFits.root","READ");
    gDirectory->cd("pass0");
    gDirectory->cd("SigFits");


    c[1]->cd();
    sprintf(name,"proj%02d%02dpass0",strip1,strip2); //ustrip w strip
    usigfits[0] = (TH1F *)gDirectory->Get(name);
    max = usigfits[0]->GetBinContent(usigfits[0]->GetMaximumBin());
    usigfits[0]->SetMaximum(max*1.1);
    usigfits[0]->Draw();

    gDirectory->cd("../../pass1/SigFits");
    c[0]->cd(2);
    sprintf(name,"proj%02d%02dpass1",strip1,strip2); //ustrip w strip
    usigfits[1] = (TH1F *)gDirectory->Get(name);
    usigfits[1]->SetMaximum(max*1.1);
    usigfits[1]->Draw();

    gDirectory->cd("../../pass2/SigFits");
    c[0]->cd(3);
    sprintf(name,"proj%02d%02dpass2",strip1,strip2); //ustrip w strip
    usigfits[2] = (TH1F *)gDirectory->Get(name);
    usigfits[2]->SetMaximum(max*1.1);
    usigfits[2]->Draw();

    gDirectory->cd("../../pass3/SigFits");
    c[0]->cd(4);
    sprintf(name,"proj%02d%02dpass3",strip1,strip2); //ustrip w strip
    usigfits[3] = (TH1F *)gDirectory->Get(name);
    usigfits[3]->SetMaximum(max*1.1);
    usigfits[3]->Draw();

    gDirectory->cd("../../pass4/SigFits");
    c[0]->cd(5);
    sprintf(name,"proj%02d%02dpass4",strip1,strip2); //ustrip w strip
    usigfits[4] = (TH1F *)gDirectory->Get(name);
    usigfits[4]->SetMaximum(max*1.1);
    usigfits[4]->Draw();

    gDirectory->cd("../../pass5/SigFits");
    c[0]->cd(6);
    sprintf(name,"proj%02d%02dpass5",strip1,strip2); //ustrip w strip
    usigfits[5] = (TH1F *)gDirectory->Get(name);
    usigfits[5]->SetMaximum(max*1.1);
    usigfits[5]->Draw();

    /*
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
    */

}
