/* Author: Cole Smith */
/* Adapted from vmetcpserver client written by Sergey Boyarinov */
/* This version uses CrateMsgClient protocol and DiagGuiServer running on each ROC */
/* For FADC250 scalers it is required FADC250_TET>0 and FADC250_ALLCH_PED are loaded */

#include "guifc.h"
#include "TriggerBoardRegs.h"	//Not required 
#include "ttfc.h"
#include "CrateMsgClient.h"

ClassImp(Dsc2Dlg);

Float_t  zmin=0.,zmax=2.;           // Stripchart slider initial settings
unsigned int ksec=5,kdet=3,kcrt=0,kdbg=0;  // Initial radio button settings
Int_t ifirst;                       // initialized in Dsc2Dlg

UInt_t scal1[2][14][16];               // Scaler readout in slot,chan
UInt_t scal2[6][2][68];             // Scaler readout in sector,det,pmt

Int_t map[14]={3,4,5,6,7,8,9,10,13,14,15,16,17,18}; //index into translation table ttfc.h

Double_t ttt;
Float_t  bintime;
Float_t  norm=0.;                       // clck/ref ref=Group 1 Ref Scaler
Float_t clck[2]={125000000.,488281.25}; // Scaler clock DSC2,FADC
int scaler_update_period[2]={1000,1000}; // milliseconds

Int_t idet;                // idet=0,1,2 (ECAL,PCAL,FTOF)
Int_t ndsc[3]={14,12,12};  // Number of DSC2 slots for idet=0,1,2
Int_t nlay[3]={6,3,2};     // Number of layers for idet=0,1,2
Int_t nlr[3]={1,1,2};      // Number of subdivisions for idet=0,1,2
Int_t nsg[3]={2,1,1};      // Number of subgraphs for nlay
Int_t npmt[3][6]={{36,36,36,36,36,36},{68,62,62,0,0,0},{62,62,23,23,0,0}}; // Number of pmts for each nlay*nlr

char hostname[80];         // Name of crate 
const char *det[] = {"tdc","adc","ecal","pcal","ftof","1","2","3","4","5","6"}; //used to construct hostname
const char *mod[] = {"DSC2","FADC","DSC2/FADC"};
const char *udet[] = {"ECAL","PCAL","FTOF"};

CrateMsgClient *fc_crate[2];
int fc_crate_slots[2][22];
				    
const char *filetypes[] = { "All files",     "*",
                            "ROOT files",    "*.root",
                            "ROOT macros",   "*.C",
                            0,               0 };

TileFrame::TileFrame(const TGWindow *p) : TGCompositeFrame(p, 10, 10, kHorizontalFrame, GetWhitePixel())
{
   // Create tile view container. Used to show colormap.
   fCanvas = 0;
   SetLayoutManager(new TGTileLayout(this, 8));

   // Handle only buttons 4 and 5 used by the wheel mouse to scroll
   gVirtualX->GrabButton(fId, kButton4, kAnyModifier,
                         kButtonPressMask | kButtonReleaseMask,
                         kNone, kNone);
   gVirtualX->GrabButton(fId, kButton5, kAnyModifier,
                         kButtonPressMask | kButtonReleaseMask,
                         kNone, kNone);
}

Bool_t TileFrame::HandleButton(Event_t *event)
{
  // Handle wheel mouse to scroll.
   Int_t page = 0;
   if (event->fCode == kButton4 || event->fCode == kButton5) {
      if (!fCanvas) return kTRUE;
      if (fCanvas->GetContainer()->GetHeight())
         page = Int_t(Float_t(fCanvas->GetViewPort()->GetHeight() *
                              fCanvas->GetViewPort()->GetHeight()) /
                              fCanvas->GetContainer()->GetHeight());
   }

   if (event->fCode == kButton4) {
      //scroll up
      Int_t newpos = fCanvas->GetVsbPosition() - page;
      if (newpos < 0) newpos = 0;
      fCanvas->SetVsbPosition(newpos);
      return kTRUE;
   }
   if (event->fCode == kButton5) {
      // scroll down
      Int_t newpos = fCanvas->GetVsbPosition() + page;
      fCanvas->SetVsbPosition(newpos);
      return kTRUE;
   }
   return kTRUE;
}

FCMainFrame::FCMainFrame(const TGWindow *p, UInt_t w, UInt_t h) : TGMainFrame(p, w, h)
{
   fDsc2Dlg   = NULL;
   fDelaysDlg = NULL;

   SetCleanup(kDeepCleanup);

   fMenuDock     = new TGDockableFrame(this); 
   AddFrame(fMenuDock, new TGLayoutHints(kLHintsExpandX, 0, 0, 1, 0)); 
   fMenuDock->SetWindowName("GUIPCAL Menu"); 

   fMenuBarLayout     = new TGLayoutHints(kLHintsTop | kLHintsExpandX);
   fMenuBarItemLayout = new TGLayoutHints(kLHintsTop | kLHintsLeft, 0, 4, 0, 0);
   fMenuBarHelpLayout = new TGLayoutHints(kLHintsTop | kLHintsRight);

   fMenuFile = new TGPopupMenu(fClient->GetRoot());
   fMenuFile->AddEntry("&Open...", M_FILE_OPEN);
   fMenuFile->AddEntry("&Save", M_FILE_SAVE);
   fMenuFile->AddEntry("S&ave as...", M_FILE_SAVEAS);
   fMenuFile->AddEntry("&Close", -1);
   fMenuFile->AddSeparator();
   fMenuFile->AddEntry("&Print", M_FILE_PRINT);
   fMenuFile->AddEntry("P&rint setup...", M_FILE_PRINTSETUP);
   fMenuFile->AddSeparator();
   fMenuFile->AddEntry("E&xit", M_FILE_EXIT);

   fMenuFile->DisableEntry(M_FILE_SAVEAS);
   fMenuFile->HideEntry(M_FILE_PRINT);
   
   fMenuMC = new TGPopupMenu(fClient->GetRoot());
   fMenuMC->AddLabel("Monitoring and Control");
   fMenuMC->AddSeparator();
   fMenuMC->AddEntry("&Scalers", M_DSC2);
     
   fMenuView = new TGPopupMenu(gClient->GetRoot());
   fMenuView->AddEntry("&Dock", M_VIEW_DOCK);
   fMenuView->AddEntry("&Undock", M_VIEW_UNDOCK);
   fMenuView->AddSeparator();
   fMenuView->AddEntry("Enable U&ndock", M_VIEW_ENBL_DOCK);
   fMenuView->AddEntry("Enable &Hide", M_VIEW_ENBL_HIDE);
   fMenuView->DisableEntry(M_VIEW_DOCK);

   fMenuDock->EnableUndock(kTRUE);
   fMenuDock->EnableHide(kTRUE);
   fMenuView->CheckEntry(M_VIEW_ENBL_DOCK);
   fMenuView->CheckEntry(M_VIEW_ENBL_HIDE);

   fMenuHelp = new TGPopupMenu(fClient->GetRoot());
   fMenuHelp->AddEntry("&Contents", M_HELP_CONTENTS);
   fMenuHelp->AddEntry("&Search...", M_HELP_SEARCH);
   fMenuHelp->AddSeparator();
   fMenuHelp->AddEntry("&About", M_HELP_ABOUT);

   // Menu button messages are handled by the main frame (i.e. "this") ProcessMessage() method.
   fMenuFile->Associate(this);
   fMenuMC->Associate(this);
   fMenuView->Associate(this);
   fMenuHelp->Associate(this);

   fMenuBar = new TGMenuBar(fMenuDock, 1, 1, kHorizontalFrame);
   fMenuBar->AddPopup("&File",    fMenuFile, fMenuBarItemLayout);
   fMenuBar->AddPopup("&Monitor", fMenuMC,   fMenuBarItemLayout);
   fMenuBar->AddPopup("&View",    fMenuView, fMenuBarItemLayout);
   fMenuBar->AddPopup("&Help",    fMenuHelp, fMenuBarHelpLayout);

   fMenuDock->AddFrame(fMenuBar, fMenuBarLayout);

   fL10 = new TGLayoutHints(kLHintsTop|kLHintsCenterX|kLHintsExpandX,2,2,2,2);
   fL0  = new TGLayoutHints(kLHintsLeft|kLHintsCenterY|kLHintsExpandY,5,5,5,5);
   fL1  = new TGLayoutHints(kLHintsLeft|kLHintsCenterX|kLHintsExpandX,5,5,5,5);

   fControlFrame = new TGHorizontalFrame(this,200,200);
   AddFrame(fControlFrame, fL10);
 
   fButtonGroup1 = new TGVButtonGroup(fControlFrame,"Sector");
   fRadiob1[0]   = new TGRadioButton(fButtonGroup1, "S1",5);
   fRadiob1[1]   = new TGRadioButton(fButtonGroup1, "S2",6);
   fRadiob1[2]   = new TGRadioButton(fButtonGroup1, "S3",7);
   fRadiob1[3]   = new TGRadioButton(fButtonGroup1, "S4",8);
   fRadiob1[4]   = new TGRadioButton(fButtonGroup1, "S5",9);
   fRadiob1[5]   = new TGRadioButton(fButtonGroup1, "S6",10);

   fButtonGroup2 = new TGVButtonGroup(fControlFrame,"Detector");
   fRadiob2[0]   = new TGRadioButton(fButtonGroup2, "ECAL",2);
   fRadiob2[1]   = new TGRadioButton(fButtonGroup2, "PCAL",3);
   fRadiob2[2]   = new TGRadioButton(fButtonGroup2, "FTOF",4);
   
   fButtonGroup3 = new TGVButtonGroup(fControlFrame,"Crate");
   fRadiob3[0]   = new TGRadioButton(fButtonGroup3, "DSC2",0);
   fRadiob3[1]   = new TGRadioButton(fButtonGroup3, "FADC",1);
   fRadiob3[2]   = new TGRadioButton(fButtonGroup3, "BOTH",11);
   
   fRadiob1[0]->SetOn();
   fRadiob2[1]->SetOn();
   fRadiob3[0]->SetOn();
   
   fButtonGroup1->Show();
   fButtonGroup2->Show();
   fButtonGroup3->Show();
   
   fControlFrame->AddFrame(fButtonGroup1, new TGLayoutHints(kLHintsCenterX|kLHintsCenterY,
                                                  1, 1, 1, 1));
   fControlFrame->AddFrame(fButtonGroup2, new TGLayoutHints(kLHintsTop|kLHintsCenterX,
                                                  1, 1, 1, 1));
   fControlFrame->AddFrame(fButtonGroup3, new TGLayoutHints(kLHintsTop|kLHintsCenterX,
                                                  1, 1, 1, 1));
   
   for (int i=0;i<6;i++) {fRadiob1[i]->Associate(this);}
   for (int i=0;i<3;i++) {fRadiob2[i]->Associate(this);}
   for (int i=0;i<3;i++) {fRadiob3[i]->Associate(this);}

   fStatusBar1 = new TGStatusBar(this,50,10);
   AddFrame(fStatusBar1,fL10);
   fStatusBar1->SetText("Choose Sector,Detector,Crate");
   
   SetWindowName("FCMON");
   MapSubwindows(); 
   Resize(GetDefaultSize()); 
   MapWindow();
}

FCMainFrame::~FCMainFrame()
{
   delete fMenuFile;
   delete fMenuMC;
   delete fMenuView;
   delete fMenuHelp;
   delete fContainer;
}

Bool_t FCMainFrame::ProcessMessage(Long_t msg, Long_t parm1, Long_t)
{
   switch (GET_MSG(msg)) {
   case kC_COMMAND:
     switch (GET_SUBMSG(msg)) {
     case kCM_RADIOBUTTON:
       if (parm1<2)          {kcrt=parm1; kdbg=0;}
       if (parm1>1&&parm1<5 ) kdet=parm1;
       if (parm1>4&&parm1<11) ksec=parm1;
       if (parm1==11)         kdbg=1;
     case kCM_MENUSELECT:
       break;
     case kCM_MENU:
       switch (parm1) {
       case M_FILE_OPEN:
	 {
	   static TString dir(".");
	   TGFileInfo fi;
	   fi.fFileTypes = filetypes;
	   fi.fIniDir    = StrDup(dir);
	   new TGFileDialog(fClient->GetRoot(), this, kFDOpen, &fi);
	   printf("Open file: %s (dir: %s)\n", fi.fFilename,fi.fIniDir);
	   dir = fi.fIniDir;
	 }
	 break;
       case M_FILE_SAVE:
	 printf("M_FILE_SAVE\n");
	 break;
       case M_FILE_PRINT:
	 printf("M_FILE_PRINT\n");
	 printf("Hiding itself, select \"Print Setup...\" to enable again\n");
	 fMenuFile->HideEntry(M_FILE_PRINT);
	 break;
       case M_FILE_PRINTSETUP:
	 printf("M_FILE_PRINTSETUP\n");
	 printf("Enabling \"Print\"\n");
	 fMenuFile->EnableEntry(M_FILE_PRINT);
	 break;
       case M_FILE_EXIT:
	 CloseWindow();   // this also terminates theApp
	 break;
       case M_DSC2:
	 if (kdbg==0) connect_to_server();
	 if (kdbg==1) {kcrt=0;connect_to_server();kcrt=1;connect_to_server();}
	 fDsc2Dlg = new Dsc2Dlg(fClient->GetRoot(), this, 600, 300);
	 break;
       case M_VIEW_ENBL_DOCK:
	 fMenuDock->EnableUndock(!fMenuDock->EnableUndock());
	 if (fMenuDock->EnableUndock()) {
	   fMenuView->CheckEntry(M_VIEW_ENBL_DOCK);
	   fMenuView->EnableEntry(M_VIEW_UNDOCK);
	 } else {
	   fMenuView->UnCheckEntry(M_VIEW_ENBL_DOCK);
	   fMenuView->DisableEntry(M_VIEW_UNDOCK);
	 }
	 break;
       case M_VIEW_ENBL_HIDE:
	 fMenuDock->EnableHide(!fMenuDock->EnableHide());
	 if (fMenuDock->EnableHide()) {
	   fMenuView->CheckEntry(M_VIEW_ENBL_HIDE);
	 } else {
	   fMenuView->UnCheckEntry(M_VIEW_ENBL_HIDE);
	 }
	 break;
       case M_VIEW_DOCK:
	 fMenuDock->DockContainer();
	 fMenuView->EnableEntry(M_VIEW_UNDOCK);
	 fMenuView->DisableEntry(M_VIEW_DOCK);
	 break;
       case M_VIEW_UNDOCK:
	 fMenuDock->UndockContainer();
	 fMenuView->EnableEntry(M_VIEW_DOCK);
	 fMenuView->DisableEntry(M_VIEW_UNDOCK);
	 break;
       default:
	 break;
       }
     default:
       break;
     }
   default:
     break;
   }

   if(fMenuDock->IsUndocked()) {
     fMenuView->EnableEntry(M_VIEW_DOCK);
     fMenuView->DisableEntry(M_VIEW_UNDOCK);
   } else {
     fMenuView->EnableEntry(M_VIEW_UNDOCK);
     fMenuView->DisableEntry(M_VIEW_DOCK);
   }
   return kTRUE;
}

void FCMainFrame::CloseWindow()
{
  // Got close message for this MainFrame. Terminate the application
  // or returns from the TApplication event loop (depending on the
  // argument specified in TApplication::Run()).

  gApplication->Terminate(0);
}

void FCMainFrame::connect_to_server()
{
  sprintf(hostname,"%s%s%s",det[kcrt],det[kdet],det[ksec]);
  printf("Trying to connect to >%s<\n",hostname);
  fc_crate[kcrt] = new CrateMsgClient(hostname,6102);
  idet=kdet-2;
  TString hname = gSystem->BaseName(hostname);
  if(fc_crate[kcrt]->IsValid())  {get_crate_map();fStatusBar1->SetText("Connected to "+hname+" ");}
  if(!fc_crate[kcrt]->IsValid()) {fStatusBar1->SetText("Connection failed...test mode"); printf("Connection failed!\n");}
}

int FCMainFrame::get_crate_map()
{
  unsigned int *cmap;
  int len,nslots;

  if(!fc_crate[kcrt]->GetCrateMap(&cmap, &len)) return -4;
  if(len > 22) return -5;
  nslots=0;
  for(int slot = 0; slot < len; slot++)
    {
      if (cmap[slot]==kcrt) {fc_crate_slots[kcrt][nslots] = slot;nslots++;}
      printf("host %s slot %d, type %d\n", hostname, slot, cmap[slot]);
    }
  delete [] cmap;
      
  printf("Found %d %s slots for %s\n",nslots,mod[kcrt],hostname);

  return nslots;
}

/***********************************/
/* Dsc2Dlg class implementation */

Dsc2Dlg::Dsc2Dlg(const TGWindow *p, FCMainFrame *main,
					   UInt_t w, UInt_t h, UInt_t options) : TGTransientFrame(p, main, w, h, options)
{
  fMain = main; // remember mainframe
   SetCleanup(kDeepCleanup);

   fL1 = new TGLayoutHints(kLHintsTop    | kLHintsLeft | kLHintsExpandX,2, 2, 2, 2);
   fL2 = new TGLayoutHints(kLHintsBottom | kLHintsRight, 2, 2, 5, 1);
   fL3 = new TGLayoutHints(kLHintsTop    | kLHintsLeft, 5, 5, 5, 5);
   fL4 = new TGLayoutHints(kLHintsTop    | kLHintsLeft | kLHintsExpandX | kLHintsExpandY, 5, 5, 5, 5);
   fL5 = new TGLayoutHints(kLHintsBottom | kLHintsExpandX | kLHintsExpandY, 2, 2, 5, 1);
   fL10 = new TGLayoutHints(kLHintsTop|kLHintsCenterX|kLHintsExpandX,2,2,2,2);
   
   AddFrame(fFrame1=new TGHorizontalFrame(this, 200, 20, kFixedWidth), fL2);
   fFrame1->AddFrame(fCancelButton=new TGTextButton(fFrame1, "&Disconnect", 1), fL1);
   fFrame1->Resize(150, fCancelButton->GetDefaultHeight());

   fCancelButton->Associate(this);

   TGCompositeFrame *tf;
   fTab = new TGTab(this, 300, 300);

   /* Slots Tab */

   tf = fTab->AddTab("Slots");
   tf->SetLayoutManager(new TGMatrixLayout(tf, 0, ndsc[idet]/2, 1));

   char sln[100],buff1[100];
   int nsl,jj;

   for (nsl=0;nsl<ndsc[idet];nsl++) 
     {
       if(kdbg==0) sprintf(sln,"Slot %d",fc_crate_slots[kcrt][nsl]);
       if(kdbg==1) sprintf(sln,"Slot %d/%d",fc_crate_slots[0][nsl],fc_crate_slots[1][nsl]);
       fF6[nsl] = new TGGroupFrame(tf,sln, kVerticalFrame);
       fF6[nsl]->SetLayoutManager(new TGMatrixLayout(fF6[nsl], 0, 2, 5));
       fF6[nsl]->SetTitlePos(TGGroupFrame::kLeft);
       tf->AddFrame(fF6[nsl], fL3);
       for(jj=0;jj<16;jj++)
	 {
	   tbufsc[nsl][jj] = new TGTextBuffer(10); tbufsc[nsl][jj]->AddText(0,"0");
	   tentsc[nsl][jj] = new TGTextEntry(fF6[nsl], tbufsc[nsl][jj]);
	   tentsc[nsl][jj]->Resize(80, tentsc[nsl][jj]->GetDefaultHeight()); // 1st arg: the number of pixels
	   tentsc[nsl][jj]->SetEnabled(kFALSE);
	   tentsc[nsl][jj]->SetFont("-adobe-courier-bold-r-*-*-12-*-*-*-*-*-iso8859-1");
	   sprintf(buff1,"   Ch.%d",jj);fF6[nsl]->AddFrame(new TGLabel(fF6[nsl], new TGHotString(buff1)));
	   fF6[nsl]->AddFrame(tentsc[nsl][jj]);
	 }
       fF6[nsl]->Resize();
     }

   /* Rates Tab */

   fShowRates = kTRUE; SetZlog = kTRUE; SetYlog = kTRUE; SetIO = kTRUE;
   
   for (int ii=0; ii<68 ; ii++) {fHP1[ii]=0 ; fHP2[ii]=0;}

   tf = fTab->AddTab("Rates");

   fF3 = new TGCompositeFrame(tf, 60, 20, kHorizontalFrame);
   fF3->AddFrame(fChk1 = new TGCheckButton(fF3, "A&ccumulate", 71), fL3);
   fF3->AddFrame(fChk3 = new TGCheckButton(fF3, "Y&Log", 73), fL3);
   fChk1->Associate(this);
   fChk3->Associate(this);
   fChk3->SetOn();
   if (idet==0) {
     fF3->AddFrame(fChk4 = new TGCheckButton(fF3, "Inner/Outer",74),fL3);
     fChk4->Associate(this);
     fChk4->SetOn();
   }
   tf->AddFrame(fF3,fL3);

   switch (idet) {
   case 0:
   fL4  = new TGLayoutHints(kLHintsTop    | kLHintsLeft | kLHintsExpandX | kLHintsExpandY, 5, 5, 5, 5);
   fF50 = new TGCompositeFrame(tf, 60, 60, kVerticalFrame);
   fF50->AddFrame(fE1[0] = new TRootEmbeddedCanvas("ec1", fF50, 100, 100), fL4);
   fF50->AddFrame(fE1[1] = new TRootEmbeddedCanvas("ec2", fF50, 100, 100), fL4);
   fF50->AddFrame(fE1[2] = new TRootEmbeddedCanvas("ec3", fF50, 100, 100), fL4);
   tf->AddFrame(fF50,fL4);
   break;
   case 1:
   fL4  = new TGLayoutHints(kLHintsTop    | kLHintsLeft | kLHintsExpandX | kLHintsExpandY, 5, 5, 5, 5);
   fF50 = new TGCompositeFrame(tf, 60, 60, kVerticalFrame);
   fF50->AddFrame(fE1[0] = new TRootEmbeddedCanvas("ec1", fF50, 100, 100), fL4);
   fF50->AddFrame(fE1[1] = new TRootEmbeddedCanvas("ec2", fF50, 100, 100), fL4);
   fF50->AddFrame(fE1[2] = new TRootEmbeddedCanvas("ec3", fF50, 100, 100), fL4);
   tf->AddFrame(fF50,fL4);
   break;
   case 2:
   fL4  = new TGLayoutHints(kLHintsExpandX | kLHintsExpandY);
   fF50 = new TGCompositeFrame(tf, 60, 60, kVerticalFrame);
   fF51 = new TGCompositeFrame(tf, 60, 60, kVerticalFrame);
   fF50->AddFrame(fE1[0] = new TRootEmbeddedCanvas("ec1", fF50, 100, 100), fL4);
   fF50->AddFrame(fE1[1] = new TRootEmbeddedCanvas("ec2", fF50, 100, 100), fL4);
   fF51->AddFrame(fE1[2] = new TRootEmbeddedCanvas("ec3", fF51, 100, 100), fL4);
   fF51->AddFrame(fE1[3] = new TRootEmbeddedCanvas("ec4", fF51, 100, 100), fL4);
   tf->AddFrame(fF50,fL4);tf->AddFrame(fF51,fL4);
   break;
   }

   int nplt=nlay[idet]*nlr[idet]/nsg[idet];
   
   for (int ii=0; ii<nplt ; ii++) {fE1[ii]->GetCanvas()->SetBorderMode(0);}

   /* Stripcharts Tab */

   tf = fTab->AddTab("Stripcharts");

   fF3b = new TGCompositeFrame(tf,120, 60, kHorizontalFrame);
   fF3b->AddFrame(fChk2  = new TGCheckButton(fF3b, "Z&Log", 72), fL3);
   fF3b->AddFrame(fHSlid = new TGDoubleHSlider(fF3b,100, kDoubleScaleBoth, 74),fL3);
   fHSlid->SetRange(0.0,7.0);
   fHSlid->SetPosition(0.0,2.0);
   fHSlid->Connect("PositionChanged()","Dsc2Dlg",this,"DoSlider()");
   fChk2->Associate(this);
   fChk2->SetOn();
   fHSlid->Associate(this);
   if (idet==0) {
     fF3b->AddFrame(fChk5 = new TGCheckButton(fF3b, "Inner/Outer",75),fL3);
     fChk5->Associate(this);
     fChk5->SetOn();
   }

   tf->AddFrame(fF3b,fL3);

   switch (idet) {
   case 0:
   fL4 = new TGLayoutHints(kLHintsTop    | kLHintsLeft | kLHintsExpandX | kLHintsExpandY, 5, 5, 5, 5);
   fF50b = new TGCompositeFrame(tf, 60, 60, kVerticalFrame);
   fF50b->AddFrame(fE2[0] = new TRootEmbeddedCanvas("ec1", fF50b, 100, 100), fL4);
   fF50b->AddFrame(fE2[1] = new TRootEmbeddedCanvas("ec2", fF50b, 100, 100), fL4);
   fF50b->AddFrame(fE2[2] = new TRootEmbeddedCanvas("ec3", fF50b, 100, 100), fL4);
   tf->AddFrame(fF50b,fL4);
   break;
   case 1:
   fL4 = new TGLayoutHints(kLHintsTop    | kLHintsLeft | kLHintsExpandX | kLHintsExpandY, 5, 5, 5, 5);
   fF50b = new TGCompositeFrame(tf, 60, 60, kVerticalFrame);
   fF50b->AddFrame(fE2[0] = new TRootEmbeddedCanvas("ec1", fF50b, 100, 100), fL4);
   fF50b->AddFrame(fE2[1] = new TRootEmbeddedCanvas("ec2", fF50b, 100, 100), fL4);
   fF50b->AddFrame(fE2[2] = new TRootEmbeddedCanvas("ec3", fF50b, 100, 100), fL4);
   tf->AddFrame(fF50b,fL4);
   break;
   case 2:
   fL4 = new TGLayoutHints(kLHintsExpandX | kLHintsExpandY);
   fF50b = new TGCompositeFrame(tf, 60, 60, kVerticalFrame);
   fF51b = new TGCompositeFrame(tf, 60, 60, kVerticalFrame);
   fF50b->AddFrame(fE2[0] = new TRootEmbeddedCanvas("ec1", fF50b, 100, 100), fL4);
   fF50b->AddFrame(fE2[1] = new TRootEmbeddedCanvas("ec2", fF50b, 100, 100), fL4);
   fF51b->AddFrame(fE2[2] = new TRootEmbeddedCanvas("ec3", fF51b, 100, 100), fL4);
   fF51b->AddFrame(fE2[3] = new TRootEmbeddedCanvas("ec4", fF51b, 100, 100), fL4);
   tf->AddFrame(fF50b,fL4);tf->AddFrame(fF51b,fL4);
   break;
   }

   for (int ii=0; ii<nplt ; ii++) {fE2[ii]->GetCanvas()->SetBorderMode(0);}

   AddFrame(fTab, fL5);
   Int_t parts[]={50,10};
   fStatusBar2 = new TGStatusBar(this,180,20);
   fStatusBar2->SetParts(parts,2);
   AddFrame(fStatusBar2,fL10);
   
   MapSubwindows();
   Resize();                    
   CenterOnParent();            
   SetWindowName("Scalers");
   MapWindow();
   
   HistAccumulate = 0;  ifirst=3;
   TTimer::SingleShot(scaler_update_period[idet],"Dsc2Dlg",this,"refresh_scalers()");
}

Dsc2Dlg::~Dsc2Dlg()
{
}

Bool_t Dsc2Dlg::ProcessMessage(Long_t msg, Long_t parm1, Long_t)
{
   switch (GET_MSG(msg)) {
      case kC_COMMAND:

         switch (GET_SUBMSG(msg)) {
            case kCM_BUTTON:
               switch(parm1) {
                  case 1:
                     CloseWindow();
                     break;
                 default:
                     break;
               }
               break;
			   
            case kCM_CHECKBUTTON:
               switch (parm1) {
                  case 71:
                     HistAccumulate = fChk1->GetState();
                     break;
	          case 72:
		     SetZlog = fChk2->GetState();
		     break;
	          case 73:
		     SetYlog = fChk3->GetState();
		     break;
	       case 74:
		 SetIO = fChk4->GetState();
		 break;
	       case 75:
		 SetIO = fChk5->GetState();
		 break;
               default:
                 break;
               }
               break;
			   
            case kCM_TAB:
	      switch(parm1) {
	      case 1: fShowRates = kTRUE  ; fShowStripChart = kFALSE ; break;
	      case 2: fShowRates = kFALSE ; fShowStripChart = kTRUE  ; break;
            default:
               break;
         }
         break;
	 }
      default:
         break;
   }
   return kTRUE;
}

void Dsc2Dlg::CloseWindow()
{
  fShowRates = kFALSE; fShowStripChart = kFALSE;         // Stop filling histos
  fCancelButton->SetState(kButtonDisabled);                  // no double-clicks
  if (TVirtualPadEditor::GetPadEditor(kFALSE) != 0)      // close Ged editor
      TVirtualPadEditor::Terminate();
  DeleteWindow();
  if (kdbg==0) disconnect_from_server();
  if (kdbg==1) {kcrt=0 ; disconnect_from_server() ; kcrt=1 ; disconnect_from_server();}
  norm=-1.; DeleteHistos();
  fMain->ClearDsc2Dlg();                                 // clear pointer to ourself
  fMain->fStatusBar1->SetText("Choose Sector,Detector,Crate");
}

void Dsc2Dlg::disconnect_from_server()
{
  sprintf(hostname,"%s%s%s",det[kcrt],det[kdet],det[ksec]);
  printf("Closing connection to %s\n",hostname);
  if(fc_crate[kcrt]->IsValid()) fc_crate[kcrt]->Close();
}

int Dsc2Dlg::refresh_scalers()
{
  if (norm==-1.) {norm=0.;return 0;}
  if (ifirst>0)  ifirst--;
  
  if (kdbg==0) ReadVME();
  if (kdbg==1) {kcrt=0;ReadVME();kcrt=1;ReadVME();}
  
  if (ifirst==1&&kdbg==0) MakeHistos();

  if (ifirst==0)
    {
      refresh_statusbar();
      UpdateGUI();
      if (kdbg==0) FillHistos();
      if (kdbg==0) DrawHistos();
    }
  
  TTimer::SingleShot(scaler_update_period[kcrt],"Dsc2Dlg",this,"refresh_scalers()");
  return 0;
}

void Dsc2Dlg::refresh_statusbar()
{
  TDatime datime;
  TString stat1,stat2;

  stat1=TString::Format("FCMON: SECTOR %s %s %s",det[ksec],udet[idet],mod[kcrt+kdbg]);
  stat2=TString::Format("%d/%d/%d %.2d:%.2d:%.2d",
		       datime.GetMonth(),datime.GetDay(),datime.GetYear(),
		       datime.GetHour(),datime.GetMinute(),datime.GetSecond());
  set_status_text(stat1,0);
  set_status_text(stat2,1);
}

void Dsc2Dlg::set_status_text(const char *txt, Int_t pi)
{
  fStatusBar2->SetText(txt,pi);
}

void Dsc2Dlg::DoSlider()
{
  zmin=fHSlid->GetMinPosition();
  zmax=fHSlid->GetMaxPosition(); 
  DrawHistos();
}

void Dsc2Dlg::ReadVME()
{
  Int_t ii, jj, i=0, j=0, k=0;
  UInt_t ref[16];
  unsigned int *buf;
  int len,slot,off[2][2]={{68,16},{51,0}};
  norm = 1.0;
  
  for(ii=0; ii<ndsc[idet]; ii++) 
    {
      slot=fc_crate_slots[kcrt][ii];
      ref[ii]=clck[kcrt];
      if(fc_crate[kcrt]->IsValid()) {fc_crate[kcrt]->ScalerReadBoard(slot, &buf, &len); ref[ii]=buf[off[0][kcrt]];}
      norm = clck[kcrt]/((Float_t)ref[ii]);
      for(jj=0; jj<16; jj++)
        {
          scal1[kcrt][ii][jj]=jj;
          if(fc_crate[kcrt]->IsValid()) scal1[kcrt][ii][jj]=buf[off[1][kcrt]+jj];
          switch (idet){
          case 0: i=adclayerecal[map[ii]][jj]-1 ; j=0                        ; k=adcstripecal[map[ii]][jj]-1 ;break;
          case 1: i=adclayerpcal[map[ii]][jj]-1 ; j=0                        ; k=adcstrippcal[map[ii]][jj]-1 ;break;
          case 2: i=adclayerftof[map[ii]][jj]-1 ; j=adclrftof[map[ii]][jj]-1 ; k=adcslabftof[map[ii]][jj]-1  ;break;
          }
          scal1[kcrt][ii][jj]=(Int_t)(((Float_t)scal1[kcrt][ii][jj])*norm) ; scal2[i][j][k]=scal1[kcrt][ii][jj];
        }
      if(fc_crate[kcrt]->IsValid()) delete [] buf;
    }
}

void Dsc2Dlg::MakeHistos()
{
  printf("MakeHistos():Making histos for idet=%d\n",idet);

  Char_t tit[10];  
  Int_t nplot,np;

  const char *htit1[3][6]={{"ECAL Ui SCALERS vs STRIP","ECAL Vi SCALERS vs STRIP","ECAL Wi SCALERS vs STRIP",
			    "ECAL Uo SCALERS vs STRIP","ECAL Vo SCALERS vs STRIP","ECAL Wo SCALERS vs STRIP"},
			   {"PCAL U  SCALERS vs STRIP","PCAL V  SCALERS vs STRIP","PCAL W  SCALERS vs STRIP",
			    " "," "," "},
			   {"FTOF1B LEFT SCALERS vs BAR","FTOF1B RIGHT SCALERS vs BAR",
			    "FTOF1A LEFT SCALERS vs BAR","FTOF1A RIGHT SCALERS vs BAR"," "," "}};

  const char *htit2[3][6]={{"ECAL Ui SCALERS vs TIME","ECAL Vi SCALERS vs TIME","ECAL Wi SCALERS vs TIME",
			    "ECAL Uo SCALERS vs TIME","ECAL Vo SCALERS vs TIME","ECAL Wo SCALERS vs TIME"},
			   {"PCAL U  SCALERS vs TIME","PCAL V  SCALERS vs TIME","PCAL W  SCALERS vs TIME",
			    " "," "," "},
			   {"FTOF1B LEFT SCALERS vs TIME","FTOF1B RIGHT SCALERS vs TIME",
			    "FTOF1A LEFT SCALERS vs TIME","FTOF1A RIGHT SCALERS vs TIME"," "," "}};

  const char *htit3[3][6]={{"EC INNER U STRIP NUMBER","EC INNER V STRIP NUMBER","EC INNER W STRIP NUMBER",
			    "EC OUTER V STRIP NUMBER","EC OUTER V STRIP NUMBER","EC OUTER W STRIP NUMBER"},
			   {"PCAL U STRIP NUMBER","PCAL V STRIP NUMBER","PCAL W STRIP NUMBER"," "," "," "},
			   {"FTOF1B LEFT BAR","FTOF1B RIGHT BAR","FTOF1A LEFT BAR","FTOF1A RIGHT BAR"," "," "}};
 
  gROOT->SetStyle("Plain");
  gStyle->SetPalette(53);
  gStyle->SetLabelSize(0.06,"Z") ; gStyle->SetLabelSize(0.05,"Y") ; gStyle->SetLabelSize(0.05,"X");

  TStopwatch sw ; sw.Start();
  TDatime dtime ; gStyle->SetTimeOffset(dtime.Convert());
  bintime=1./norm;
  nplot=nlay[idet]*nlr[idet];

  for(np=0; np<nplot; np++)
    {
      sprintf(tit,"hp1%d",np);
      fHP1[np] = new TH1F(tit,htit1[idet][np],npmt[idet][np],1.,((Float_t)npmt[idet][np]+1.));
      fHP1[np]->SetFillColor(kRed);fHP1[np]->SetStats(kFALSE);
      fHP1[np]->SetNdivisions(npmt[idet][np]); fHP1[np]->GetYaxis()->SetTickLength(0.01);
      fHP1[np]->SetMinimum(0.5);
      sprintf(tit,"hp2%d",np);
      fHP2[np] = new TH2F(tit,htit2[idet][np],100,1.,101.*bintime,npmt[idet][np],1.,((Float_t)npmt[idet][np]+1.));
      fHP2[np]->SetStats(kFALSE);fHP2[np]->GetXaxis()->SetTimeDisplay(1); 
      fHP2[np]->GetZaxis()->SetRangeUser(1.,1500.); fHP2[np]->GetYaxis()->SetTitle(htit3[idet][np]);
      fHP2[np]->GetYaxis()->SetNdivisions(npmt[idet][np]/2);fHP2[np]->GetYaxis()->SetTickLength(0.01);
      fHP2[np]->GetYaxis()->SetTitleSize(0.06);fHP2[np]->GetYaxis()->SetTitleOffset(0.5); 
      fHP2[np]->GetYaxis()->CenterTitle();
    }
}

void Dsc2Dlg::DeleteHistos()
{
  Int_t nplot,np; 
  nplot=nlay[idet]*nlr[idet];
  for(np=0; np<nplot; np++)
    {
      if (fHP1[np]) delete fHP1[np];
      if (fHP2[np]) delete fHP2[np];
    }
}

void Dsc2Dlg::UpdateGUI()
{
   Int_t ii,jj;
   Char_t str[10];
   if(kdbg==0) for(ii=0; ii<ndsc[idet]; ii++) {for(jj=0; jj<16; jj++){sprintf(str,"%8d",scal1[kcrt][ii][jj]);tentsc[ii][jj]->SetText(str);}}
   if(kdbg==1) for(ii=0; ii<ndsc[idet]; ii++) {for(jj=0; jj<16; jj++){sprintf(str,"%4d/%4d",scal1[0][ii][jj],scal1[1][ii][jj]);tentsc[ii][jj]->SetText(str);}}
}

void Dsc2Dlg::FillHistos()
{
  Int_t nplot,np;
  Int_t ii,jj,kk;
  Double_t xx,ww;

  nplot = nlay[idet]*nlr[idet];

  if(fShowRates && !HistAccumulate) {for (np=0 ; np<nplot ; np++) {fHP1[np]->Reset();}}

  ttt=ttt+bintime;
  printf("Here ttt=%f\n",ttt);
  
  if (ttt>bintime*100) {DeleteHistos();MakeHistos();ttt=bintime;}

  np=0;
  for(ii=0; ii<nlay[idet] ; ii++)
    {
      for(jj=0; jj<nlr[idet]; jj++)
	{
	  for(kk=0; kk<npmt[idet][np] ; kk++)
	    {
	      xx = (Double_t)kk+1; ww = (Double_t)scal2[ii][jj][kk];
	      fHP1[np]->Fill(xx,ww);
	      fHP2[np]->Fill(ttt,xx,ww);
	    }
	  np++;
	}
    }
}

void Dsc2Dlg::DrawHistos()
{
  TCanvas *c[6];
  Int_t nplt,np,ioff;
  
  nplt = nlay[idet]*nlr[idet]/nsg[idet];
  
  ioff=0;
  if(idet==0&&SetIO)  ioff=0; //EC inner
  if(idet==0&&!SetIO) ioff=3; //EC outer
  
  for(np=0; np<nplt ; np++)
    {
      if(fShowRates)      {c[np] = fE1[np]->GetCanvas(); c[np]->SetLogy(SetYlog);
	                   c[np]->cd(); fHP1[np+ioff]->Draw();}
      if(fShowStripChart) {c[np] = fE2[np]->GetCanvas(); c[np]->SetLogy(0); c[np]->SetLogz(SetZlog) ; 
	                           fHP2[np]->GetZaxis()->SetRangeUser(pow(10.,zmin),pow(10.,zmax));
				   //	                           fHP2[np]->GetZaxis()->SetRangeUser(zmin,zmax);
			   c[np]->cd(); fHP2[np+ioff]->Draw("colz");}
      
      c[np]->Modified(); c[np]->Update();
    }
}

int main(int argc, char **argv)
{
   TApplication theApp("App", &argc, argv);
   FCMainFrame mainWindow(gClient->GetRoot(), 300, 300);
   theApp.Run();
   return 0;
}
