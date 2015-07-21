void drawattfitcomp()
{
    int i;
    int j = 1;
    int k = 0;
    int strip1 = 67;
    int strip2 = 61;
    int pass;
    char name[100];
    double max, maxx;
    TGraphErrors *usigfits[62];
    TCanvas *c[7];

    //pass 0
    pass = 0;
    c[0] = new TCanvas();
    c[0]->Divide(3,2);
    //input file
	TFile *f = new TFile("UFits.root","READ");
    gDirectory->cd("pass0");
    gDirectory->cd("AtFits");


    c[0]->cd(1);
    sprintf(name,"ustrip%02dpass%d",5, pass); //ustrip w strip
    usigfits[0] = (TGraphErrors *)gDirectory->Get(name);
    max = 700.0;
    maxx = 400.0;
    sprintf(name,"Attenuation (Pass %d, U-Strip %d);Distance (cm); Gaussian Centroid (ADC)",pass,5);
    usigfits[0]->SetTitle(name);
    usigfits[0]->SetMaximum(max*1.1);
    usigfits[0]->SetMinimum(0.0);
    usigfits[0]->Draw("APE");

    //gDirectory->cd("../../pass1/AtFits");
    c[0]->cd(2);
    sprintf(name,"ustrip%02dpass%d",15, pass); //ustrip w strip
    usigfits[1] = (TGraphErrors *)gDirectory->Get(name);
    sprintf(name,"Attenuation (Pass %d, U-Strip %d);Distance (cm); Gaussian Centroid (ADC)",pass,15);
    usigfits[1]->SetTitle(name);
    usigfits[1]->SetMaximum(max*1.1);
    usigfits[1]->SetMinimum(0.0);
    usigfits[1]->Draw("APE");

    //gDirectory->cd("../../pass2/AtFits");
    c[0]->cd(3);
    sprintf(name,"ustrip%02dpass%d",30, pass); //ustrip w strip
    usigfits[2] = (TGraphErrors *)gDirectory->Get(name);
    sprintf(name,"Attenuation (Pass %d, U-Strip %d);Distance (cm); Gaussian Centroid (ADC)",pass,30);
    usigfits[2]->SetTitle(name);
    usigfits[2]->SetMaximum(max*1.1);
    usigfits[2]->SetMinimum(0.0);
    usigfits[2]->Draw("APE");

    //gDirectory->cd("../../pass3/AtFits");
    c[0]->cd(4);
    sprintf(name,"ustrip%02dpass%d",45, pass); //ustrip w strip
    usigfits[3] = (TGraphErrors *)gDirectory->Get(name);
    sprintf(name,"Attenuation (Pass %d, U-Strip %d);Distance (cm); Gaussian Centroid (ADC)",pass,45);
    usigfits[3]->SetTitle(name);
    usigfits[3]->SetMaximum(max*1.1);
    usigfits[3]->SetMinimum(0.0);
    usigfits[3]->Draw("APE");

    //gDirectory->cd("../../pass4/AtFits");
    c[0]->cd(5);
    sprintf(name,"ustrip%02dpass%d",67, pass); //ustrip w strip
    usigfits[4] = (TGraphErrors *)gDirectory->Get(name);
    sprintf(name,"Attenuation (Pass %d, U-Strip %d);Distance (cm); Gaussian Centroid (ADC)",pass,67);
    usigfits[4]->SetTitle(name);
    usigfits[4]->SetMaximum(max*1.1);
    usigfits[4]->SetMinimum(0.0);
    usigfits[4]->Draw("APE");

    //gDirectory->cd("../../pass5/AtFits");
    c[0]->cd(6);
    sprintf(name,"ustrip%02dpass%d",68, pass); //ustrip w strip
    usigfits[5] = (TGraphErrors *)gDirectory->Get(name);
    sprintf(name,"Attenuation (Pass %d, U-Strip %d);Distance (cm); Gaussian Centroid (ADC)",pass,68);
    usigfits[5]->SetTitle(name);
    usigfits[5]->SetMaximum(max*1.1);
    usigfits[5]->SetMinimum(0.0);
    usigfits[5]->Draw("APE");


    //pass 1
    pass = 1;
    c[1] = new TCanvas();
    c[1]->Divide(3,2);


    gDirectory->cd("../../pass1/AtFits");
    c[1]->cd(1);
    sprintf(name,"ustrip%02dpass%d",5, pass); //ustrip w strip
    usigfits[10] = (TGraphErrors *)gDirectory->Get(name);
    max = 700.0;
    maxx = 400.0;
    sprintf(name,"Attenuation (Pass %d, U-Strip %d);Distance (cm); Gaussian Centroid (ADC)",pass,5);
    usigfits[10]->SetTitle(name);
    usigfits[10]->SetMaximum(max*1.1);
    usigfits[10]->SetMinimum(0.0);
    usigfits[10]->Draw("APE");

    //gDirectory->cd("../../pass1/AtFits");
    c[1]->cd(2);
    sprintf(name,"ustrip%02dpass%d",15, pass); //ustrip w strip
    usigfits[11] = (TGraphErrors *)gDirectory->Get(name);
    sprintf(name,"Attenuation (Pass %d, U-Strip %d);Distance (cm); Gaussian Centroid (ADC)",pass,15);
    usigfits[11]->SetTitle(name);
    usigfits[11]->SetMaximum(max*1.1);
    usigfits[11]->SetMinimum(0.0);
    usigfits[11]->Draw("APE");

    //gDirectory->cd("../../pass2/AtFits");
    c[1]->cd(3);
    sprintf(name,"ustrip%02dpass%d",30, pass); //ustrip w strip
    usigfits[12] = (TGraphErrors *)gDirectory->Get(name);
    sprintf(name,"Attenuation (Pass %d, U-Strip %d);Distance (cm); Gaussian Centroid (ADC)",pass,30);
    usigfits[12]->SetTitle(name);
    usigfits[12]->SetMaximum(max*1.1);
    usigfits[12]->SetMinimum(0.0);
    usigfits[12]->Draw("APE");

    //gDirectory->cd("../../pass3/AtFits");
    c[1]->cd(4);
    sprintf(name,"ustrip%02dpass%d",45, pass); //ustrip w strip
    usigfits[13] = (TGraphErrors *)gDirectory->Get(name);
    sprintf(name,"Attenuation (Pass %d, U-Strip %d);Distance (cm); Gaussian Centroid (ADC)",pass,45);
    usigfits[13]->SetTitle(name);
    usigfits[13]->SetMaximum(max*1.1);
    usigfits[13]->SetMinimum(0.0);
    usigfits[13]->Draw("APE");

    //gDirectory->cd("../../pass4/AtFits");
    c[1]->cd(5);
    sprintf(name,"ustrip%02dpass%d",67, pass); //ustrip w strip
    usigfits[14] = (TGraphErrors *)gDirectory->Get(name);
    sprintf(name,"Attenuation (Pass %d, U-Strip %d);Distance (cm); Gaussian Centroid (ADC)",pass,67);
    usigfits[14]->SetTitle(name);
    usigfits[14]->SetMaximum(max*1.1);
    usigfits[14]->SetMinimum(0.0);
    usigfits[14]->Draw("APE");

    //gDirectory->cd("../../pass5/AtFits");
    c[1]->cd(6);
    sprintf(name,"ustrip%02dpass%d",68, pass); //ustrip w strip
    usigfits[15] = (TGraphErrors *)gDirectory->Get(name);
    sprintf(name,"Attenuation (Pass %d, U-Strip %d);Distance (cm); Gaussian Centroid (ADC)",pass,68);
    usigfits[15]->SetTitle(name);
    usigfits[15]->SetMaximum(max*1.1);
    usigfits[15]->SetMinimum(0.0);
    usigfits[15]->Draw("APE");

    

    //pass 2
    pass = 2;
    c[2] = new TCanvas();
    c[2]->Divide(3,2);


    gDirectory->cd("../../pass2/AtFits");
    c[2]->cd(1);
    sprintf(name,"ustrip%02dpass%d",5, pass); //ustrip w strip
    usigfits[20] = (TGraphErrors *)gDirectory->Get(name);
    max = 700.0;
    maxx = 400.0;
    sprintf(name,"Attenuation (Pass %d, U-Strip %d);Distance (cm); Gaussian Centroid (ADC)",pass,5);
    usigfits[20]->SetTitle(name);
    usigfits[20]->SetMaximum(max*1.1);
    usigfits[20]->SetMinimum(0.0);
    usigfits[20]->Draw("APE");

    //gDirectory->cd("../../pass1/AtFits");
    c[2]->cd(2);
    sprintf(name,"ustrip%02dpass%d",15, pass); //ustrip w strip
    usigfits[21] = (TGraphErrors *)gDirectory->Get(name);
    sprintf(name,"Attenuation (Pass %d, U-Strip %d);Distance (cm); Gaussian Centroid (ADC)",pass,15);
    usigfits[21]->SetTitle(name);
    usigfits[21]->SetMaximum(max*1.1);
    usigfits[21]->SetMinimum(0.0);
    usigfits[21]->Draw("APE");

    //gDirectory->cd("../../pass2/AtFits");
    c[2]->cd(3);
    sprintf(name,"ustrip%02dpass%d",30, pass); //ustrip w strip
    usigfits[22] = (TGraphErrors *)gDirectory->Get(name);
    sprintf(name,"Attenuation (Pass %d, U-Strip %d);Distance (cm); Gaussian Centroid (ADC)",pass,30);
    usigfits[22]->SetTitle(name);
    usigfits[22]->SetMaximum(max*1.1);
    usigfits[22]->SetMinimum(0.0);
    usigfits[22]->Draw("APE");

    //gDirectory->cd("../../pass3/AtFits");
    c[2]->cd(4);
    sprintf(name,"ustrip%02dpass%d",45, pass); //ustrip w strip
    usigfits[23] = (TGraphErrors *)gDirectory->Get(name);
    sprintf(name,"Attenuation (Pass %d, U-Strip %d);Distance (cm); Gaussian Centroid (ADC)",pass,45);
    usigfits[23]->SetTitle(name);
    usigfits[23]->SetMaximum(max*1.1);
    usigfits[23]->SetMinimum(0.0);
    usigfits[23]->Draw("APE");

    //gDirectory->cd("../../pass4/AtFits");
    c[2]->cd(5);
    sprintf(name,"ustrip%02dpass%d",67, pass); //ustrip w strip
    usigfits[24] = (TGraphErrors *)gDirectory->Get(name);
    sprintf(name,"Attenuation (Pass %d, U-Strip %d);Distance (cm); Gaussian Centroid (ADC)",pass,67);
    usigfits[24]->SetTitle(name);
    usigfits[24]->SetMaximum(max*1.1);
    usigfits[24]->SetMinimum(0.0);
    usigfits[24]->Draw("APE");

    //gDirectory->cd("../../pass5/AtFits");
    c[2]->cd(6);
    sprintf(name,"ustrip%02dpass%d",68, pass); //ustrip w strip
    usigfits[25] = (TGraphErrors *)gDirectory->Get(name);
    sprintf(name,"Attenuation (Pass %d, U-Strip %d);Distance (cm); Gaussian Centroid (ADC)",pass,68);
    usigfits[25]->SetTitle(name);
    usigfits[25]->SetMaximum(max*1.1);
    usigfits[25]->SetMinimum(0.0);
    usigfits[25]->Draw("APE");




    

    //pass 3
    pass = 3;
    c[3] = new TCanvas();
    c[3]->Divide(3,2);


    gDirectory->cd("../../pass3/AtFits");
    c[3]->cd(1);
    sprintf(name,"ustrip%02dpass%d",5, pass); //ustrip w strip
    usigfits[30] = (TGraphErrors *)gDirectory->Get(name);
    max = 700.0;
    maxx = 400.0;
    sprintf(name,"Attenuation (Pass %d, U-Strip %d);Distance (cm); Gaussian Centroid (ADC)",pass,5);
    usigfits[30]->SetTitle(name);
    usigfits[30]->SetMaximum(max*1.1);
    usigfits[30]->SetMinimum(0.0);
    usigfits[30]->Draw("APE");

    //gDirectory->cd("../../pass1/AtFits");
    c[3]->cd(2);
    sprintf(name,"ustrip%02dpass%d",15, pass); //ustrip w strip
    usigfits[31] = (TGraphErrors *)gDirectory->Get(name);
    sprintf(name,"Attenuation (Pass %d, U-Strip %d);Distance (cm); Gaussian Centroid (ADC)",pass,15);
    usigfits[31]->SetTitle(name);
    usigfits[31]->SetMaximum(max*1.1);
    usigfits[31]->SetMinimum(0.0);
    usigfits[31]->Draw("APE");

    //gDirectory->cd("../../pass2/AtFits");
    c[3]->cd(3);
    sprintf(name,"ustrip%02dpass%d",30, pass); //ustrip w strip
    usigfits[32] = (TGraphErrors *)gDirectory->Get(name);
    sprintf(name,"Attenuation (Pass %d, U-Strip %d);Distance (cm); Gaussian Centroid (ADC)",pass,30);
    usigfits[32]->SetTitle(name);
    usigfits[32]->SetMaximum(max*1.1);
    usigfits[32]->SetMinimum(0.0);
    usigfits[32]->Draw("APE");

    //gDirectory->cd("../../pass3/AtFits");
    c[3]->cd(4);
    sprintf(name,"ustrip%02dpass%d",45, pass); //ustrip w strip
    usigfits[33] = (TGraphErrors *)gDirectory->Get(name);
    sprintf(name,"Attenuation (Pass %d, U-Strip %d);Distance (cm); Gaussian Centroid (ADC)",pass,45);
    usigfits[33]->SetTitle(name);
    usigfits[33]->SetMaximum(max*1.1);
    usigfits[33]->SetMinimum(0.0);
    usigfits[33]->Draw("APE");

    //gDirectory->cd("../../pass4/AtFits");
    c[3]->cd(5);
    sprintf(name,"ustrip%02dpass%d",67, pass); //ustrip w strip
    usigfits[34] = (TGraphErrors *)gDirectory->Get(name);
    sprintf(name,"Attenuation (Pass %d, U-Strip %d);Distance (cm); Gaussian Centroid (ADC)",pass,67);
    usigfits[34]->SetTitle(name);
    usigfits[34]->SetMaximum(max*1.1);
    usigfits[34]->SetMinimum(0.0);
    usigfits[34]->Draw("APE");

    //gDirectory->cd("../../pass5/AtFits");
    c[3]->cd(6);
    sprintf(name,"ustrip%02dpass%d",68, pass); //ustrip w strip
    usigfits[35] = (TGraphErrors *)gDirectory->Get(name);
    sprintf(name,"Attenuation (Pass %d, U-Strip %d);Distance (cm); Gaussian Centroid (ADC)",pass,68);
    usigfits[35]->SetTitle(name);
    usigfits[35]->SetMaximum(max*1.1);
    usigfits[35]->SetMinimum(0.0);
    usigfits[35]->Draw("APE");



    

    //pass 4
    pass = 4;
    c[4] = new TCanvas();
    c[4]->Divide(3,2);


    gDirectory->cd("../../pass4/AtFits");
    c[4]->cd(1);
    sprintf(name,"ustrip%02dpass%d",5, pass); //ustrip w strip
    usigfits[40] = (TGraphErrors *)gDirectory->Get(name);
    max = 700.0;
    maxx = 400.0;
    sprintf(name,"Attenuation (Pass %d, U-Strip %d);Distance (cm); Gaussian Centroid (ADC)",pass,5);
    usigfits[40]->SetTitle(name);
    usigfits[40]->SetMaximum(max*1.1);
    usigfits[40]->SetMinimum(0.0);
    usigfits[40]->Draw("APE");

    //gDirectory->cd("../../pass1/AtFits");
    c[4]->cd(2);
    sprintf(name,"ustrip%02dpass%d",15, pass); //ustrip w strip
    usigfits[41] = (TGraphErrors *)gDirectory->Get(name);
    sprintf(name,"Attenuation (Pass %d, U-Strip %d);Distance (cm); Gaussian Centroid (ADC)",pass,15);
    usigfits[41]->SetTitle(name);
    usigfits[41]->SetMaximum(max*1.1);
    usigfits[41]->SetMinimum(0.0);
    usigfits[41]->Draw("APE");

    //gDirectory->cd("../../pass2/AtFits");
    c[4]->cd(3);
    sprintf(name,"ustrip%02dpass%d",30, pass); //ustrip w strip
    usigfits[42] = (TGraphErrors *)gDirectory->Get(name);
    sprintf(name,"Attenuation (Pass %d, U-Strip %d);Distance (cm); Gaussian Centroid (ADC)",pass,30);
    usigfits[42]->SetTitle(name);
    usigfits[42]->SetMaximum(max*1.1);
    usigfits[42]->SetMinimum(0.0);
    usigfits[42]->Draw("APE");

    //gDirectory->cd("../../pass3/AtFits");
    c[4]->cd(4);
    sprintf(name,"ustrip%02dpass%d",45, pass); //ustrip w strip
    usigfits[43] = (TGraphErrors *)gDirectory->Get(name);
    sprintf(name,"Attenuation (Pass %d, U-Strip %d);Distance (cm); Gaussian Centroid (ADC)",pass,45);
    usigfits[43]->SetTitle(name);
    usigfits[43]->SetMaximum(max*1.1);
    usigfits[43]->SetMinimum(0.0);
    usigfits[43]->Draw("APE");

    //gDirectory->cd("../../pass4/AtFits");
    c[4]->cd(5);
    sprintf(name,"ustrip%02dpass%d",67, pass); //ustrip w strip
    usigfits[44] = (TGraphErrors *)gDirectory->Get(name);
    sprintf(name,"Attenuation (Pass %d, U-Strip %d);Distance (cm); Gaussian Centroid (ADC)",pass,67);
    usigfits[44]->SetTitle(name);
    usigfits[44]->SetMaximum(max*1.1);
    usigfits[44]->SetMinimum(0.0);
    usigfits[44]->Draw("APE");

    //gDirectory->cd("../../pass5/AtFits");
    c[4]->cd(6);
    sprintf(name,"ustrip%02dpass%d",68, pass); //ustrip w strip
    usigfits[45] = (TGraphErrors *)gDirectory->Get(name);
    sprintf(name,"Attenuation (Pass %d, U-Strip %d);Distance (cm); Gaussian Centroid (ADC)",pass,68);
    usigfits[45]->SetTitle(name);
    usigfits[45]->SetMaximum(max*1.1);
    usigfits[45]->SetMinimum(0.0);
    usigfits[45]->Draw("APE");



    

    //pass 5
    pass = 5;
    c[5] = new TCanvas();
    c[5]->Divide(3,2);


    gDirectory->cd("../../pass5/AtFits");
    c[5]->cd(1);
    sprintf(name,"ustrip%02dpass%d",5, pass); //ustrip w strip
    usigfits[50] = (TGraphErrors *)gDirectory->Get(name);
    max = 700.0;
    maxx = 400.0;
    sprintf(name,"Attenuation (Pass %d, U-Strip %d);Distance (cm); Gaussian Centroid (ADC)",pass,5);
    usigfits[50]->SetTitle(name);
    usigfits[50]->SetMaximum(max*1.1);
    usigfits[50]->SetMinimum(0.0);
    usigfits[50]->Draw("APE");

    //gDirectory->cd("../../pass1/AtFits");
    c[5]->cd(2);
    sprintf(name,"ustrip%02dpass%d",15, pass); //ustrip w strip
    usigfits[51] = (TGraphErrors *)gDirectory->Get(name);
    sprintf(name,"Attenuation (Pass %d, U-Strip %d);Distance (cm); Gaussian Centroid (ADC)",pass,15);
    usigfits[51]->SetTitle(name);
    usigfits[51]->SetMaximum(max*1.1);
    usigfits[51]->SetMinimum(0.0);
    usigfits[51]->Draw("APE");

    //gDirectory->cd("../../pass2/AtFits");
    c[5]->cd(3);
    sprintf(name,"ustrip%02dpass%d",30, pass); //ustrip w strip
    usigfits[52] = (TGraphErrors *)gDirectory->Get(name);
    sprintf(name,"Attenuation (Pass %d, U-Strip %d);Distance (cm); Gaussian Centroid (ADC)",pass,30);
    usigfits[52]->SetTitle(name);
    usigfits[52]->SetMaximum(max*1.1);
    usigfits[52]->SetMinimum(0.0);
    usigfits[52]->Draw("APE");

    //gDirectory->cd("../../pass3/AtFits");
    c[5]->cd(4);
    sprintf(name,"ustrip%02dpass%d",45, pass); //ustrip w strip
    usigfits[53] = (TGraphErrors *)gDirectory->Get(name);
    sprintf(name,"Attenuation (Pass %d, U-Strip %d);Distance (cm); Gaussian Centroid (ADC)",pass,45);
    usigfits[53]->SetTitle(name);
    usigfits[53]->SetMaximum(max*1.1);
    usigfits[53]->SetMinimum(0.0);
    usigfits[53]->Draw("APE");

    //gDirectory->cd("../../pass4/AtFits");
    c[5]->cd(5);
    sprintf(name,"ustrip%02dpass%d",67, pass); //ustrip w strip
    usigfits[54] = (TGraphErrors *)gDirectory->Get(name);
    sprintf(name,"Attenuation (Pass %d, U-Strip %d);Distance (cm); Gaussian Centroid (ADC)",pass,67);
    usigfits[54]->SetTitle(name);
    usigfits[54]->SetMaximum(max*1.1);
    usigfits[54]->SetMinimum(0.0);
    usigfits[54]->Draw("APE");

    //gDirectory->cd("../../pass5/AtFits");
    c[5]->cd(6);
    sprintf(name,"ustrip%02dpass%d",68, pass); //ustrip w strip
    usigfits[55] = (TGraphErrors *)gDirectory->Get(name);
    sprintf(name,"Attenuation (Pass %d, U-Strip %d);Distance (cm); Gaussian Centroid (ADC)",pass,68);
    usigfits[55]->SetTitle(name);
    usigfits[55]->SetMaximum(max*1.1);
    usigfits[55]->SetMinimum(0.0);
    usigfits[55]->Draw("APE");

}
