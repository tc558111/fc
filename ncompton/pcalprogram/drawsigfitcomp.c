void drawsigfitcomp()
{
    int i;
    int j = 1;
    int k = 0;
    int strip1 = 67;
    int strip2 = 60;
    int pass;
    char name[100];
    double max0,max1,max2,max3,max4,max5;
    TH1F *usigfits[62];
    TCanvas *c[7];
    
    c[0] = new TCanvas();
    c[0]->Divide(3,2);
    //input file
	TFile *f = new TFile("UFits.root","READ");


    //Pass 0
    pass = 0;
    gDirectory->cd("pass0");
    gDirectory->cd("SigFits");
    c[0]->cd(1);
    sprintf(name,"proj%02d%02dpass%d",5,strip2,pass); //ustrip w strip
    usigfits[0] = (TH1F *)gDirectory->Get(name);
    max0 = usigfits[0]->GetBinContent(usigfits[0]->GetMaximumBin());
    usigfits[0]->SetMaximum(max0*1.1);
    sprintf(name,"Raw Signal (Pass %d, U-Strip %d, W-Strip %d);ADC Readout;Counts",pass,5,strip2);
    usigfits[0]->SetTitle(name);
    usigfits[0]->Draw();

    //gDirectory->cd("../../pass1/SigFits");
    c[0]->cd(2);
    sprintf(name,"proj%02d%02dpass%d",15,strip2,pass); //ustrip w strip
    usigfits[1] = (TH1F *)gDirectory->Get(name);
    max1 = usigfits[1]->GetBinContent(usigfits[1]->GetMaximumBin());
    usigfits[1]->SetMaximum(max1*1.1);
    sprintf(name,"Raw Signal (Pass %d, U-Strip %d, W-Strip %d);ADC Readout;Counts",pass,15,strip2);
    usigfits[1]->SetTitle(name);
    usigfits[1]->Draw();

    //gDirectory->cd("../../pass2/SigFits");
    c[0]->cd(3);
    sprintf(name,"proj%02d%02dpass%d",30,strip2,pass); //ustrip w strip
    usigfits[2] = (TH1F *)gDirectory->Get(name);
    max2 = usigfits[2]->GetBinContent(usigfits[2]->GetMaximumBin());
    usigfits[2]->SetMaximum(max2*1.1);
    sprintf(name,"Raw Signal (Pass %d, U-Strip %d, W-Strip %d);ADC Readout;Counts",pass,30,strip2);
    usigfits[2]->SetTitle(name);
    usigfits[2]->Draw();

    //gDirectory->cd("../../pass3/SigFits");
    c[0]->cd(4);
    sprintf(name,"proj%02d%02dpass%d",45,strip2,pass); //ustrip w strip
    usigfits[3] = (TH1F *)gDirectory->Get(name);
    max3 = usigfits[3]->GetBinContent(usigfits[3]->GetMaximumBin());
    usigfits[3]->SetMaximum(max3*1.1);
    sprintf(name,"Raw Signal (Pass %d, U-Strip %d, W-Strip %d);ADC Readout;Counts",pass,45,strip2);
    usigfits[3]->SetTitle(name);
    usigfits[3]->Draw();

    //gDirectory->cd("../../pass4/SigFits");
    c[0]->cd(5);
    sprintf(name,"proj%02d%02dpass%d",67,strip2,pass); //ustrip w strip
    usigfits[4] = (TH1F *)gDirectory->Get(name);
    max4 = usigfits[4]->GetBinContent(usigfits[4]->GetMaximumBin());
    usigfits[4]->SetMaximum(max4*1.1);
    sprintf(name,"Raw Signal (Pass %d, U-Strip %d, W-Strip %d);ADC Readout;Counts",pass,67,strip2);
    usigfits[4]->SetTitle(name);
    usigfits[4]->Draw();

    //gDirectory->cd("../../pass5/SigFits");
    c[0]->cd(6);
    sprintf(name,"proj%02d%02dpass%d",68,strip2,pass); //ustrip w strip
    usigfits[5] = (TH1F *)gDirectory->Get(name);
    max5 = usigfits[5]->GetBinContent(usigfits[5]->GetMaximumBin());
    usigfits[5]->SetMaximum(max5*1.1);
    sprintf(name,"Raw Signal (Pass %d, U-Strip %d, W-Strip %d);ADC Readout;Counts",pass,68,strip2);
    usigfits[5]->SetTitle(name);
    usigfits[5]->Draw();


    //Pass1
    pass = 1;
    c[1] = new TCanvas();
    c[1]->Divide(3,2);

    gDirectory->cd("../../pass1/SigFits");
    c[1]->cd(1);
    sprintf(name,"proj%02d%02dpass%d",5,strip2,pass); //ustrip w strip
    usigfits[10] = (TH1F *)gDirectory->Get(name);
    usigfits[10]->SetMaximum(max0*1.1);
    sprintf(name,"Raw Signal (Pass %d, U-Strip %d, W-Strip %d);ADC Readout;Counts",pass,5,strip2);
    usigfits[10]->SetTitle(name);
    usigfits[10]->Draw();

    //gDirectory->cd("../../pass1/SigFits");
    c[1]->cd(2);
    sprintf(name,"proj%02d%02dpass%d",15,strip2,pass); //ustrip w strip
    usigfits[11] = (TH1F *)gDirectory->Get(name);
    usigfits[11]->SetMaximum(max1*1.1);
    sprintf(name,"Raw Signal (Pass %d, U-Strip %d, W-Strip %d);ADC Readout;Counts",pass,15,strip2);
    usigfits[11]->SetTitle(name);
    usigfits[11]->Draw();

    //gDirectory->cd("../../pass2/SigFits");
    c[1]->cd(3);
    sprintf(name,"proj%02d%02dpass%d",30,strip2,pass); //ustrip w strip
    usigfits[12] = (TH1F *)gDirectory->Get(name);
    usigfits[12]->SetMaximum(max2*1.1);
    sprintf(name,"Raw Signal (Pass %d, U-Strip %d, W-Strip %d);ADC Readout;Counts",pass,30,strip2);
    usigfits[12]->SetTitle(name);
    usigfits[12]->Draw();

    //gDirectory->cd("../../pass3/SigFits");
    c[1]->cd(4);
    sprintf(name,"proj%02d%02dpass%d",45,strip2,pass); //ustrip w strip
    usigfits[13] = (TH1F *)gDirectory->Get(name);
    usigfits[13]->SetMaximum(max3*1.1);
    sprintf(name,"Raw Signal (Pass %d, U-Strip %d, W-Strip %d);ADC Readout;Counts",pass,45,strip2);
    usigfits[13]->SetTitle(name);
    usigfits[13]->Draw();

    //gDirectory->cd("../../pass4/SigFits");
    c[1]->cd(5);
    sprintf(name,"proj%02d%02dpass%d",67,strip2,pass); //ustrip w strip
    usigfits[14] = (TH1F *)gDirectory->Get(name);
    usigfits[14]->SetMaximum(max4*1.1);
    sprintf(name,"Raw Signal (Pass %d, U-Strip %d, W-Strip %d);ADC Readout;Counts",pass,67,strip2);
    usigfits[14]->SetTitle(name);
    usigfits[14]->Draw();

    //gDirectory->cd("../../pass5/SigFits");
    c[1]->cd(6);
    sprintf(name,"proj%02d%02dpass%d",68,strip2,pass); //ustrip w strip
    usigfits[15] = (TH1F *)gDirectory->Get(name);
    usigfits[15]->SetMaximum(max5*1.1);
    sprintf(name,"Raw Signal (Pass %d, U-Strip %d, W-Strip %d);ADC Readout;Counts",pass,68,strip2);
    usigfits[15]->SetTitle(name);
    usigfits[15]->Draw();


    
    //Pass2
    pass = 2;
    c[2] = new TCanvas();
    c[2]->Divide(3,2);

    gDirectory->cd("../../pass2/SigFits");
    c[2]->cd(1);
    sprintf(name,"proj%02d%02dpass%d",5,strip2,pass); //ustrip w strip
    usigfits[20] = (TH1F *)gDirectory->Get(name);
    usigfits[20]->SetMaximum(max0*1.1);
    sprintf(name,"Raw Signal (Pass %d, U-Strip %d, W-Strip %d);ADC Readout;Counts",pass,5,strip2);
    usigfits[20]->SetTitle(name);
    usigfits[20]->Draw();

    //gDirectory->cd("../../pass1/SigFits");
    c[2]->cd(2);
    sprintf(name,"proj%02d%02dpass%d",15,strip2,pass); //ustrip w strip
    usigfits[21] = (TH1F *)gDirectory->Get(name);
    usigfits[21]->SetMaximum(max1*1.1);
    sprintf(name,"Raw Signal (Pass %d, U-Strip %d, W-Strip %d);ADC Readout;Counts",pass,15,strip2);
    usigfits[21]->SetTitle(name);
    usigfits[21]->Draw();

    //gDirectory->cd("../../pass2/SigFits");
    c[2]->cd(3);
    sprintf(name,"proj%02d%02dpass%d",30,strip2,pass); //ustrip w strip
    usigfits[22] = (TH1F *)gDirectory->Get(name);
    usigfits[22]->SetMaximum(max2*1.1);
    sprintf(name,"Raw Signal (Pass %d, U-Strip %d, W-Strip %d);ADC Readout;Counts",pass,30,strip2);
    usigfits[22]->SetTitle(name);
    usigfits[22]->Draw();

    //gDirectory->cd("../../pass3/SigFits");
    c[2]->cd(4);
    sprintf(name,"proj%02d%02dpass%d",45,strip2,pass); //ustrip w strip
    usigfits[23] = (TH1F *)gDirectory->Get(name);
    usigfits[23]->SetMaximum(max3*1.1);
    sprintf(name,"Raw Signal (Pass %d, U-Strip %d, W-Strip %d);ADC Readout;Counts",pass,45,strip2);
    usigfits[23]->SetTitle(name);
    usigfits[23]->Draw();

    //gDirectory->cd("../../pass4/SigFits");
    c[2]->cd(5);
    sprintf(name,"proj%02d%02dpass%d",67,strip2,pass); //ustrip w strip
    usigfits[24] = (TH1F *)gDirectory->Get(name);
    usigfits[24]->SetMaximum(max4*1.1);
    sprintf(name,"Raw Signal (Pass %d, U-Strip %d, W-Strip %d);ADC Readout;Counts",pass,67,strip2);
    usigfits[24]->SetTitle(name);
    usigfits[24]->Draw();

    //gDirectory->cd("../../pass5/SigFits");
    c[2]->cd(6);
    sprintf(name,"proj%02d%02dpass%d",68,strip2,pass); //ustrip w strip
    usigfits[25] = (TH1F *)gDirectory->Get(name);
    usigfits[25]->SetMaximum(max5*1.1);
    sprintf(name,"Raw Signal (Pass %d, U-Strip %d, W-Strip %d);ADC Readout;Counts",pass,68,strip2);
    usigfits[25]->SetTitle(name);
    usigfits[25]->Draw();



    
    //Pass3
    pass = 3;
    c[3] = new TCanvas();
    c[3]->Divide(3,2);

    gDirectory->cd("../../pass3/SigFits");
    c[3]->cd(1);
    sprintf(name,"proj%02d%02dpass%d",5,strip2,pass); //ustrip w strip
    usigfits[30] = (TH1F *)gDirectory->Get(name);
    usigfits[30]->SetMaximum(max0*1.1);
    sprintf(name,"Raw Signal (Pass %d, U-Strip %d, W-Strip %d);ADC Readout;Counts",pass,5,strip2);
    usigfits[30]->SetTitle(name);
    usigfits[30]->Draw();

    //gDirectory->cd("../../pass1/SigFits");
    c[3]->cd(2);
    sprintf(name,"proj%02d%02dpass%d",15,strip2,pass); //ustrip w strip
    usigfits[31] = (TH1F *)gDirectory->Get(name);
    usigfits[31]->SetMaximum(max1*1.1);
    sprintf(name,"Raw Signal (Pass %d, U-Strip %d, W-Strip %d);ADC Readout;Counts",pass,15,strip2);
    usigfits[31]->SetTitle(name);
    usigfits[31]->Draw();

    //gDirectory->cd("../../pass2/SigFits");
    c[3]->cd(3);
    sprintf(name,"proj%02d%02dpass%d",30,strip2,pass); //ustrip w strip
    usigfits[32] = (TH1F *)gDirectory->Get(name);
    usigfits[32]->SetMaximum(max2*1.1);
    sprintf(name,"Raw Signal (Pass %d, U-Strip %d, W-Strip %d);ADC Readout;Counts",pass,30,strip2);
    usigfits[32]->SetTitle(name);
    usigfits[32]->Draw();

    //gDirectory->cd("../../pass3/SigFits");
    c[3]->cd(4);
    sprintf(name,"proj%02d%02dpass%d",45,strip2,pass); //ustrip w strip
    usigfits[33] = (TH1F *)gDirectory->Get(name);
    usigfits[33]->SetMaximum(max3*1.1);
    sprintf(name,"Raw Signal (Pass %d, U-Strip %d, W-Strip %d);ADC Readout;Counts",pass,45,strip2);
    usigfits[33]->SetTitle(name);
    usigfits[33]->Draw();

    //gDirectory->cd("../../pass4/SigFits");
    c[3]->cd(5);
    sprintf(name,"proj%02d%02dpass%d",67,strip2,pass); //ustrip w strip
    usigfits[34] = (TH1F *)gDirectory->Get(name);
    usigfits[34]->SetMaximum(max4*1.1);
    sprintf(name,"Raw Signal (Pass %d, U-Strip %d, W-Strip %d);ADC Readout;Counts",pass,67,strip2);
    usigfits[34]->SetTitle(name);
    usigfits[34]->Draw();

    //gDirectory->cd("../../pass5/SigFits");
    c[3]->cd(6);
    sprintf(name,"proj%02d%02dpass%d",68,strip2,pass); //ustrip w strip
    usigfits[35] = (TH1F *)gDirectory->Get(name);
    usigfits[35]->SetMaximum(max5*1.1);
    sprintf(name,"Raw Signal (Pass %d, U-Strip %d, W-Strip %d);ADC Readout;Counts",pass,68,strip2);
    usigfits[35]->SetTitle(name);
    usigfits[35]->Draw();




    
    //Pass4
    pass = 4;
    c[4] = new TCanvas();
    c[4]->Divide(3,2);

    gDirectory->cd("../../pass4/SigFits");
    c[4]->cd(1);
    sprintf(name,"proj%02d%02dpass%d",5,strip2,pass); //ustrip w strip
    usigfits[40] = (TH1F *)gDirectory->Get(name);
    usigfits[40]->SetMaximum(max0*1.1);
    sprintf(name,"Raw Signal (Pass %d, U-Strip %d, W-Strip %d);ADC Readout;Counts",pass,5,strip2);
    usigfits[40]->SetTitle(name);
    usigfits[40]->Draw();

    //gDirectory->cd("../../pass1/SigFits");
    c[4]->cd(2);
    sprintf(name,"proj%02d%02dpass%d",15,strip2,pass); //ustrip w strip
    usigfits[41] = (TH1F *)gDirectory->Get(name);
    usigfits[41]->SetMaximum(max1*1.1);
    sprintf(name,"Raw Signal (Pass %d, U-Strip %d, W-Strip %d);ADC Readout;Counts",pass,15,strip2);
    usigfits[41]->SetTitle(name);
    usigfits[41]->Draw();

    //gDirectory->cd("../../pass2/SigFits");
    c[4]->cd(3);
    sprintf(name,"proj%02d%02dpass%d",30,strip2,pass); //ustrip w strip
    usigfits[42] = (TH1F *)gDirectory->Get(name);
    usigfits[42]->SetMaximum(max2*1.1);
    sprintf(name,"Raw Signal (Pass %d, U-Strip %d, W-Strip %d);ADC Readout;Counts",pass,30,strip2);
    usigfits[42]->SetTitle(name);
    usigfits[42]->Draw();

    //gDirectory->cd("../../pass3/SigFits");
    c[4]->cd(4);
    sprintf(name,"proj%02d%02dpass%d",45,strip2,pass); //ustrip w strip
    usigfits[43] = (TH1F *)gDirectory->Get(name);
    usigfits[43]->SetMaximum(max3*1.1);
    sprintf(name,"Raw Signal (Pass %d, U-Strip %d, W-Strip %d);ADC Readout;Counts",pass,45,strip2);
    usigfits[43]->SetTitle(name);
    usigfits[43]->Draw();

    //gDirectory->cd("../../pass4/SigFits");
    c[4]->cd(5);
    sprintf(name,"proj%02d%02dpass%d",67,strip2,pass); //ustrip w strip
    usigfits[44] = (TH1F *)gDirectory->Get(name);
    usigfits[44]->SetMaximum(max4*1.1);
    sprintf(name,"Raw Signal (Pass %d, U-Strip %d, W-Strip %d);ADC Readout;Counts",pass,67,strip2);
    usigfits[44]->SetTitle(name);
    usigfits[44]->Draw();

    //gDirectory->cd("../../pass5/SigFits");
    c[4]->cd(6);
    sprintf(name,"proj%02d%02dpass%d",68,strip2,pass); //ustrip w strip
    usigfits[45] = (TH1F *)gDirectory->Get(name);
    usigfits[45]->SetMaximum(max5*1.1);
    sprintf(name,"Raw Signal (Pass %d, U-Strip %d, W-Strip %d);ADC Readout;Counts",pass,68,strip2);
    usigfits[45]->SetTitle(name);
    usigfits[45]->Draw();





    
    //Pass5
    pass = 5;
    c[5] = new TCanvas();
    c[5]->Divide(3,2);

    gDirectory->cd("../../pass5/SigFits");
    c[5]->cd(1);
    sprintf(name,"proj%02d%02dpass%d",5,strip2,pass); //ustrip w strip
    usigfits[50] = (TH1F *)gDirectory->Get(name);
    usigfits[50]->SetMaximum(max0*1.1);
    sprintf(name,"Raw Signal (Pass %d, U-Strip %d, W-Strip %d);ADC Readout;Counts",pass,5,strip2);
    usigfits[50]->SetTitle(name);
    usigfits[50]->Draw();

    //gDirectory->cd("../../pass1/SigFits");
    c[5]->cd(2);
    sprintf(name,"proj%02d%02dpass%d",15,strip2,pass); //ustrip w strip
    usigfits[51] = (TH1F *)gDirectory->Get(name);
    usigfits[51]->SetMaximum(max1*1.1);
    sprintf(name,"Raw Signal (Pass %d, U-Strip %d, W-Strip %d);ADC Readout;Counts",pass,15,strip2);
    usigfits[51]->SetTitle(name);
    usigfits[51]->Draw();

    //gDirectory->cd("../../pass2/SigFits");
    c[5]->cd(3);
    sprintf(name,"proj%02d%02dpass%d",30,strip2,pass); //ustrip w strip
    usigfits[52] = (TH1F *)gDirectory->Get(name);
    usigfits[52]->SetMaximum(max2*1.1);
    sprintf(name,"Raw Signal (Pass %d, U-Strip %d, W-Strip %d);ADC Readout;Counts",pass,30,strip2);
    usigfits[52]->SetTitle(name);
    usigfits[52]->Draw();

    //gDirectory->cd("../../pass3/SigFits");
    c[5]->cd(4);
    sprintf(name,"proj%02d%02dpass%d",45,strip2,pass); //ustrip w strip
    usigfits[53] = (TH1F *)gDirectory->Get(name);
    usigfits[53]->SetMaximum(max3*1.1);
    sprintf(name,"Raw Signal (Pass %d, U-Strip %d, W-Strip %d);ADC Readout;Counts",pass,45,strip2);
    usigfits[53]->SetTitle(name);
    usigfits[53]->Draw();

    //gDirectory->cd("../../pass4/SigFits");
    c[5]->cd(5);
    sprintf(name,"proj%02d%02dpass%d",67,strip2,pass); //ustrip w strip
    usigfits[54] = (TH1F *)gDirectory->Get(name);
    usigfits[54]->SetMaximum(max4*1.1);
    sprintf(name,"Raw Signal (Pass %d, U-Strip %d, W-Strip %d);ADC Readout;Counts",pass,67,strip2);
    usigfits[54]->SetTitle(name);
    usigfits[54]->Draw();

    //gDirectory->cd("../../pass5/SigFits");
    c[5]->cd(6);
    sprintf(name,"proj%02d%02dpass%d",68,strip2,pass); //ustrip w strip
    usigfits[55] = (TH1F *)gDirectory->Get(name);
    usigfits[55]->SetMaximum(max5*1.1);
    sprintf(name,"Raw Signal (Pass %d, U-Strip %d, W-Strip %d);ADC Readout;Counts",pass,68,strip2);
    usigfits[55]->SetTitle(name);
    usigfits[55]->Draw();

}
