void drawchisquared()
{
    int i;
    int j = 1;
    int k = 0;
    int strip1 = 67;
    int strip2 = 60;
    int pass = 5;
    char name[100];
    double max;
    TH1F *usigfits[62];
    TCanvas *c[7];
    
    c[0] = new TCanvas();
    c[0]->Divide(2,5);
    //c[1] = new TCanvas();
    //c[1]->Divide(2,5);
    //input file
	TFile *f = new TFile("UFits.root","READ");
    gDirectory->cd("pass5");
    gDirectory->cd("SigFits");


    //pass 5
    c[0]->cd(5);
    sprintf(name,"diststrip01pass%d",5); //ustrip w strip
    usigfits[0] = (TH1F *)gDirectory->Get(name);
    max = usigfits[0]->GetBinContent(usigfits[0]->GetMaximumBin());
    //usigfits[0]->SetMaximum(max*1.1);
    sprintf(name,"Chi Squared Distribution (%3d bins);#Chi_{#nu};Counts",125);
    usigfits[0]->SetTitle(name);
    usigfits[0]->Draw();


    c[0]->cd(6);
    sprintf(name,"diststrip10pass%d",5); //ustrip w strip
    usigfits[0] = (TH1F *)gDirectory->Get(name);
    max = usigfits[0]->GetBinContent(usigfits[0]->GetMaximumBin());
    //usigfits[0]->SetMaximum(max*1.1);
    sprintf(name,"Chi Squared Distribution (%3d bins);#Chi_{#nu};Counts",125);
    usigfits[0]->SetTitle(name);
    usigfits[0]->Draw();

    //pass 6
    gDirectory->cd("../../pass6/SigFits");
    c[0]->cd(1);
    sprintf(name,"diststrip01pass%d",6); //ustrip w strip
    usigfits[1] = (TH1F *)gDirectory->Get(name);
    //usigfits[1]->SetMaximum(max*1.1);
    sprintf(name,"Chi Squared Distribution (%3d bins);#Chi_{#nu};Counts",50);
    usigfits[1]->SetTitle(name);
    usigfits[1]->Draw();


    c[0]->cd(2);
    sprintf(name,"diststrip10pass%d",6); //ustrip w strip
    usigfits[1] = (TH1F *)gDirectory->Get(name);
    //usigfits[1]->SetMaximum(max*1.1);
    sprintf(name,"Chi Squared Distribution (%3d bins);#Chi_{#nu};Counts",50);
    usigfits[1]->SetTitle(name);
    usigfits[1]->Draw();

    //pass 7
    gDirectory->cd("../../pass7/SigFits");
    c[0]->cd(3);
    sprintf(name,"diststrip01pass%d",7); //ustrip w strip
    usigfits[2] = (TH1F *)gDirectory->Get(name);
    //usigfits[2]->SetMaximum(max*1.1);
    sprintf(name,"Chi Squared Distribution (%3d bins);#Chi_{#nu};Counts",100);
    usigfits[2]->SetTitle(name);
    usigfits[2]->Draw();

    c[0]->cd(4);
    sprintf(name,"diststrip10pass%d",7); //ustrip w strip
    usigfits[2] = (TH1F *)gDirectory->Get(name);
    //usigfits[2]->SetMaximum(max*1.1);
    sprintf(name,"Chi Squared Distribution (%3d bins);#Chi_{#nu};Counts",100);
    usigfits[2]->SetTitle(name);
    usigfits[2]->Draw();

    //pass 8
    gDirectory->cd("../../pass8/SigFits");
    c[0]->cd(7);
    sprintf(name,"diststrip01pass%d",8); //ustrip w strip
    usigfits[3] = (TH1F *)gDirectory->Get(name);
    //usigfits[3]->SetMaximum(max*1.1);
    sprintf(name,"Chi Squared Distribution (%3d bins);#Chi_{#nu};Counts",150);
    usigfits[3]->SetTitle(name);
    usigfits[3]->Draw();

    c[0]->cd(8);
    sprintf(name,"diststrip10pass%d",8); //ustrip w strip
    usigfits[3] = (TH1F *)gDirectory->Get(name);
    //usigfits[3]->SetMaximum(max*1.1);
    sprintf(name,"Chi Squared Distribution (%3d bins);#Chi_{#nu};Counts",150);
    usigfits[3]->SetTitle(name);
    usigfits[3]->Draw();

    //pass 9
    gDirectory->cd("../../pass9/SigFits");
    c[0]->cd(9);
    sprintf(name,"diststrip01pass%d",9); //ustrip w strip
    usigfits[4] = (TH1F *)gDirectory->Get(name);
    //usigfits[4]->SetMaximum(max*1.1);
    sprintf(name,"Chi Squared Distribution (%3d bins);#Chi_{#nu};Counts",200);
    usigfits[4]->SetTitle(name);
    usigfits[4]->Draw();

    c[0]->cd(10);
    sprintf(name,"diststrip10pass%d",9); //ustrip w strip
    usigfits[4] = (TH1F *)gDirectory->Get(name);
    //usigfits[4]->SetMaximum(max*1.1);
    sprintf(name,"Chi Squared Distribution (%3d bins);#Chi_{#nu};Counts",200);
    usigfits[4]->SetTitle(name);
    usigfits[4]->Draw();




}
