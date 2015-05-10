#include <stdlib.h>

#include <TROOT.h>
#include <TApplication.h>
#include <TVirtualX.h>
#include <TVirtualPadEditor.h>
#include <TGResourcePool.h>
#include <TGListBox.h>
#include <TGListTree.h>
#include <TGFSContainer.h>
#include <TGClient.h>
#include <TGFrame.h>
#include <TGIcon.h>
#include <TGLabel.h>
#include <TGButton.h>
#include <TGButtonGroup.h>
#include <TGTextEntry.h>
#include <TGNumberEntry.h>
#include <TGMsgBox.h>
#include <TGMenu.h>
#include <TGCanvas.h>
#include <TGComboBox.h>
#include <TGTab.h>
#include <TGDoubleSlider.h>
#include <TGFileDialog.h>
#include <TGTextEdit.h>
#include <TGShutter.h>
#include <TGProgressBar.h>
#include <TGColorSelect.h>
#include <TRootEmbeddedCanvas.h>
#include <TCanvas.h>
#include <TColor.h>
#include <TH1.h>
#include <TH2.h>
#include <TRandom.h>
#include <TStyle.h>
#include <TSystem.h>
#include <TSystemDirectory.h>
#include <TEnv.h>
#include <TFile.h>
#include <TKey.h>
#include <TGDockableFrame.h>
#include <TGFontDialog.h>
#include <TPolyLine.h>
#include <TRootCanvas.h>
#include <TText.h>
#include <TStopwatch.h>
#include <TDatime.h>

#include "TriggerBoardRegs.h"
#include "guipcal.h"

#include "vmeclient.h"
#include "libtcp.h"
#include "libdb.h"

#include "scope.h"
#include "ttfc.h"

VMEClient *tcpvme; //sergey: global for now, will find appropriate place later

Double_t ttt;
Float_t  bintime;
Float_t  norm=0.;
int ksec=4,kdet=8;
Int_t  idet,ifirst;

UInt_t scal1[12][16],scal2[6][2][68];
Int_t map[12]={3,4,5,6,7,8,9,10,13,14,15,16};
Int_t dsc2map[3][14]={{3,4,5,6,7,8,9,10,13,14,15,16,17,18},
		      {3,4,5,6,7,8,9,10,13,14,15,16,0,0},
		      {2,4,5,7,8,10,12,14,15,17,18,20,0,0}};
Int_t ndsc[3]={14,12,12};
Int_t nlay[3]={6,3,2};
Int_t nlr[3]={1,1,2};
Int_t npmt[3][6]={{36,36,36,36,36,36},{68,62,62,0,0,0},{62,62,23,23,0,0}};

UInt_t addr[3][14];

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

const char *filetypes[] = { "All files",     "*",
                            "ROOT files",    "*.root",
                            "ROOT macros",   "*.C",
                            0,               0 };

/********************************/
/* MyTimer class implementation */

MyTimer::MyTimer(PCALMainFrame *m, Long_t ms) : TTimer(ms, kTRUE)
{
  fPCALMainFrame = m;
  gSystem->AddTimer(this);
}

Bool_t MyTimer::Notify()
{
  // This function will be called in case of timeout INSTEAD OF
  // standard Notify() function from TTimer class

  if(fPCALMainFrame->fDsc2Dlg)    fPCALMainFrame->fDsc2Dlg->ReadVME();
  if(fPCALMainFrame->fDelaysDlg)  fPCALMainFrame->fDelaysDlg->ReadVME();
  this->Reset();

  return kTRUE;
}
//------------------------------------------------------------------------------



/**********************************/
/* TileFrame class implementation */

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



/**************************************/
/* PCALMainFrame class implementation */

PCALMainFrame::PCALMainFrame(const TGWindow *p, UInt_t w, UInt_t h, char *host) : TGMainFrame(p, w, h)
{

   // create VME communication
   tcpvme     = new VMEClient();
   //   strcpy(hostname,host);
   sprintf(hostname,"tdcpcal5");


   fDsc2Dlg   = NULL;
   fDelaysDlg = NULL;

   // Create main frame. A TGMainFrame is a top level window.

   // use hierarchical cleaning
   SetCleanup(kDeepCleanup);

   // Create menubar and popup menus. The hint objects are used to place
   // and group the different menu widgets with respect to each other.
   fMenuDock     = new TGDockableFrame(this); // create menu dock (upper horizontal bar for 'File' etc)
   AddFrame(fMenuDock, new TGLayoutHints(kLHintsExpandX, 0, 0, 1, 0)); // add menu dock to the main window
   fMenuDock->SetWindowName("GUIPCAL Menu"); 

   // create several layouts 
   fMenuBarLayout     = new TGLayoutHints(kLHintsTop | kLHintsExpandX);
   fMenuBarItemLayout = new TGLayoutHints(kLHintsTop | kLHintsLeft, 0, 4, 0, 0);
   fMenuBarHelpLayout = new TGLayoutHints(kLHintsTop | kLHintsRight);

   // menu item 'File'
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
   

   // menu item 'DISPLAY'
   fMenuPCAL = new TGPopupMenu(fClient->GetRoot());
   fMenuPCAL->AddLabel("Monitoring and Control");
   fMenuPCAL->AddSeparator();
   fMenuPCAL->AddEntry("&Dsc2", M_DSC2);
   fMenuPCAL->AddEntry("&Scope (ASCII)", M_SCOPE_ASCII);
   fMenuPCAL->AddEntry("&Scope (Canvas)", M_SCOPE_CANVAS);

   // menu item 'View'
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

   // 'menu item 'Help'
   fMenuHelp = new TGPopupMenu(fClient->GetRoot());
   fMenuHelp->AddEntry("&Contents", M_HELP_CONTENTS);
   fMenuHelp->AddEntry("&Search...", M_HELP_SEARCH);
   fMenuHelp->AddSeparator();
   fMenuHelp->AddEntry("&About", M_HELP_ABOUT);

   // Menu button messages are handled by the main frame (i.e. "this") ProcessMessage() method.
   fMenuFile->Associate(this);
   fMenuPCAL->Associate(this);
   fMenuView->Associate(this);
   fMenuHelp->Associate(this);

   // create menu bar and actually add created above menus to it
   fMenuBar = new TGMenuBar(fMenuDock, 1, 1, kHorizontalFrame);
   fMenuBar->AddPopup("&File",    fMenuFile, fMenuBarItemLayout);
   fMenuBar->AddPopup("&Monitor", fMenuPCAL, fMenuBarItemLayout);
   fMenuBar->AddPopup("&View",    fMenuView, fMenuBarItemLayout);
   fMenuBar->AddPopup("&Help",    fMenuHelp, fMenuBarHelpLayout);

   // add menu bar to the dock
   fMenuDock->AddFrame(fMenuBar, fMenuBarLayout);

   //main frame is control frame

   fL10 = new TGLayoutHints(kLHintsTop|kLHintsCenterX|kLHintsExpandX,2,2,2,2);
   fL0  = new TGLayoutHints(kLHintsLeft|kLHintsCenterY|kLHintsExpandY,5,5,5,5);
   fL1  = new TGLayoutHints(kLHintsLeft|kLHintsCenterX|kLHintsExpandX,5,5,5,5);

   fControlFrame = new TGHorizontalFrame(this,200,200);
   AddFrame(fControlFrame, fL10);
 
   fButtonGroup1 = new TGVButtonGroup(fControlFrame,"Sector");
   fRadiob1[0]   = new TGRadioButton(fButtonGroup1, "S1",0);
   fRadiob1[1]   = new TGRadioButton(fButtonGroup1, "S2",1);
   fRadiob1[2]   = new TGRadioButton(fButtonGroup1, "S3",2);
   fRadiob1[3]   = new TGRadioButton(fButtonGroup1, "S4",3);
   fRadiob1[4]   = new TGRadioButton(fButtonGroup1, "S5",4);
   fRadiob1[5]   = new TGRadioButton(fButtonGroup1, "S6",5);

   fButtonGroup2 = new TGVButtonGroup(fControlFrame,"Detector");
   fRadiob2[0]   = new TGRadioButton(fButtonGroup2, "ECAL",6);
   fRadiob2[1]   = new TGRadioButton(fButtonGroup2, "PCAL",7);
   fRadiob2[2]   = new TGRadioButton(fButtonGroup2, "FTOF",8);
   fRadiob1[4]->SetOn();
   fRadiob2[2]->SetOn();

   fButtonGroup1->Show();
   fButtonGroup2->Show();
   
   fControlFrame->AddFrame(fButtonGroup1, new TGLayoutHints(kLHintsCenterX|kLHintsCenterY,
                                                  1, 1, 1, 1));
   fControlFrame->AddFrame(fButtonGroup2, new TGLayoutHints(kLHintsTop|kLHintsCenterX,
                                                  1, 1, 1, 1));
   for (int i=0;i<6;i++) {fRadiob1[i]->Associate(this);}
   for (int i=0;i<3;i++) {fRadiob2[i]->Associate(this);}

   fActionFrame = new TGHorizontalFrame(this,100,50);
   AddFrame(fActionFrame,fL10);
   fActionFrame->AddFrame(btConnect    = new TGTextButton(fActionFrame, "Connect", 11),fL1);
   fActionFrame->AddFrame(btDisconnect = new TGTextButton(fActionFrame,"Disconnect", 12),fL1);
   btDisconnect->SetEnabled(kFALSE);
   btConnect->Associate(this);
   btDisconnect->Associate(this);

   SetWindowName("FCMON");
   MapSubwindows();  // force drawing of all created subwindows; actual drawing is done by MapWindow()
   Resize(GetDefaultSize());   // resize to default size
   MapWindow(); // actual window drawing

   //   fButtonGroup1->SetState(kTRUE);

   Print();

   tt = new MyTimer(this, 1000);
}

PCALMainFrame::~PCALMainFrame()
{
   // Delete all created widgets.

   delete fMenuFile;
   delete fMenuPCAL;
   delete fMenuView;
   delete fMenuHelp;
   delete fContainer;
}

void PCALMainFrame::CloseWindow()
{
   // Got close message for this MainFrame. Terminate the application
   // or returns from the TApplication event loop (depending on the
   // argument specified in TApplication::Run()).

   gApplication->Terminate(0);
}

Bool_t PCALMainFrame::ProcessMessage(Long_t msg, Long_t parm1, Long_t)
{
   // Handle messages send to the PCALMainFrame object. E.g. all menu button
   // messages.
   const char *det[] = {"1","2","3","4","5","6","ecal","pcal","ftof"};
   UInt_t board_addr[2];
   board_addr[0] = PCAL_BOARD_ADDRESS_1;
   if(PCAL_BOARD_ADDRESS_1 != PCAL_BOARD_ADDRESS_2) board_addr[1] = PCAL_BOARD_ADDRESS_2;
   else                                             board_addr[1] = 0;

   char SignalNames0[SCOPE_CHANNELCOUNT][20];
   char SignalNames1[SCOPE_CHANNELCOUNT][20];
   int i, sig;

   sig = 0;
   for(i=1; i<=12; i++) sprintf(SignalNames0[sig++],"TRIG%02d", i);
   for(i=1; i<=NU; i++) sprintf(SignalNames0[sig++],"U__%02d", i);
   for(i=1; i<=NV; i++) sprintf(SignalNames0[sig++],"V__%02d", i);
   while(sig<SCOPE_CHANNELCOUNT) sprintf(SignalNames0[sig++],"unused");
   printf("sig0=%d\n",sig);

   sig = 0;
   for(i=1; i<=NW; i++) sprintf(SignalNames1[sig++],"W__%02d", i);
   while(sig<SCOPE_CHANNELCOUNT) sprintf(SignalNames1[sig++],"unused");
   printf("sig1=%d\n",sig);


   switch (GET_MSG(msg)) {
   case kC_COMMAND:
     switch (GET_SUBMSG(msg)) {
     case kCM_RADIOBUTTON:
       if (parm1<6) ksec=parm1;
       if (parm1>5&&parm1<9) kdet=parm1;
       sprintf(hostname,"tdc%s%s",det[kdet],det[ksec]);
       printf("hostname=%s\n",hostname);
       idet=kdet-6;
       printf("idet=%d\n",idet);
     case kCM_BUTTON:

     if(parm1 == 11) // Connect
       {
	 printf("Connect reached\n");
	 Bool_t res = tcpvme->ConnectVME(hostname,0);
	 if(res)
	   {
	     btConnect->SetEnabled(kFALSE);
	     btDisconnect->SetEnabled(kTRUE);
	   }
	 if(fDsc2Dlg)
	   {
	     fDsc2Dlg->ReadVME();
	     fDsc2Dlg->UpdateGUI();
	   }
       }
     else if(parm1 == 12) // Disconnect
       {
	 printf("Disconnect reached\n");
	 Bool_t res = tcpvme->DisconnectVME();
	 if(res)
	   {
	     btConnect->SetEnabled(kTRUE);
	     btDisconnect->SetEnabled(kFALSE);
	   }
       }
     break;

     case kCM_MENUSELECT:
       //printf("Pointer over menu entry, id=%ld\n", parm1);
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
                        printf("Open file: %s (dir: %s)\n", fi.fFilename,
                               fi.fIniDir);
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
                     fDsc2Dlg = new Dsc2Dlg(fClient->GetRoot(), this, 600, 300);
                     break;

                  case M_SCOPE_ASCII:
					fScopeDlg = new ScopeDlg(fClient->GetRoot(), this, 800, 800, board_addr, SignalNames0, SignalNames1, 0);
                     break;

                  case M_SCOPE_CANVAS:
					fScopeDlg = new ScopeDlg(fClient->GetRoot(), this, 800, 800, board_addr, SignalNames0, SignalNames1, 1);
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

   if (fMenuDock->IsUndocked()) {
      fMenuView->EnableEntry(M_VIEW_DOCK);
      fMenuView->DisableEntry(M_VIEW_UNDOCK);
   } else {
      fMenuView->EnableEntry(M_VIEW_UNDOCK);
      fMenuView->DisableEntry(M_VIEW_DOCK);
   }

   return kTRUE;
}

/***********************************/
/* Dsc2Dlg class implementation */

Dsc2Dlg::Dsc2Dlg(const TGWindow *p, PCALMainFrame *main,
					   UInt_t w, UInt_t h, UInt_t options) : TGTransientFrame(p, main, w, h, options)
{

   fMain = main; // remember mainframe
   SetCleanup(kDeepCleanup);

   fL1 = new TGLayoutHints(kLHintsTop    | kLHintsLeft | kLHintsExpandX,2, 2, 2, 2);
   fL2 = new TGLayoutHints(kLHintsBottom | kLHintsRight, 2, 2, 5, 1);
   fL3 = new TGLayoutHints(kLHintsTop    | kLHintsLeft, 5, 5, 5, 5);
   fL4 = new TGLayoutHints(kLHintsTop    | kLHintsLeft | kLHintsExpandX | kLHintsExpandY, 5, 5, 5, 5);
   fL5 = new TGLayoutHints(kLHintsBottom | kLHintsExpandX | kLHintsExpandY, 2, 2, 5, 1);

   /* Ok Cancel Buttons */
   AddFrame(fFrame1=new TGHorizontalFrame(this, 60, 20, kFixedWidth), fL2);
   fFrame1->AddFrame(fOkButton=new TGTextButton(fFrame1, "&Ok", 1),fL1);
   fFrame1->AddFrame(fCancelButton=new TGTextButton(fFrame1, "&Cancel", 2), fL1);
   fFrame1->Resize(150, fOkButton->GetDefaultHeight());

   fOkButton->Associate(this);
   fCancelButton->Associate(this);

   TGCompositeFrame *tf;
   fTab = new TGTab(this, 300, 300);

   /* Scalers Tab */

   tf = fTab->AddTab("Scalers");
   tf->SetLayoutManager(new TGMatrixLayout(tf, 0, ndsc[idet]/2, 1));

   char sln[100],buff1[100];
   int nsl,jj,imap;

   for (nsl=0;nsl<ndsc[idet];nsl++) 
     {
       imap=dsc2map[idet][nsl];
       sprintf(sln,"Slot %d",imap); fF6[nsl] = new TGGroupFrame(tf,sln, kVerticalFrame);
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

   fShowRates = kTRUE; SetZlog = kTRUE; SetYlog = kTRUE;
   for (int ii=0; ii<68 ; ii++) {fHP1[ii]=0 ; fHP2[ii]=0;}

   tf = fTab->AddTab("Rates");

   fF3 = new TGCompositeFrame(tf, 60, 20, kHorizontalFrame);
   fF3->AddFrame(fChk1 = new TGCheckButton(fF3, "A&ccumulate", 71), fL3);
   fF3->AddFrame(fChk3 = new TGCheckButton(fF3, "Y&Log", 73), fL3);
   fChk1->Associate(this);
   fChk3->Associate(this);
   fChk3->SetOn();
   tf->AddFrame(fF3,fL3);

   switch (idet) {
   case 0:
   fL4  = new TGLayoutHints(kLHintsTop    | kLHintsLeft | kLHintsExpandX | kLHintsExpandY, 5, 5, 5, 5);
   fF50 = new TGCompositeFrame(tf, 60, 60, kHorizontalFrame);
   fF51 = new TGCompositeFrame(tf, 60, 60, kHorizontalFrame);
   fF50->AddFrame(fE1[0] = new TRootEmbeddedCanvas("ec1", fF50, 100, 100), fL4);
   fF50->AddFrame(fE1[1] = new TRootEmbeddedCanvas("ec2", fF50, 100, 100), fL4);
   fF50->AddFrame(fE1[2] = new TRootEmbeddedCanvas("ec3", fF50, 100, 100), fL4);
   fF51->AddFrame(fE1[3] = new TRootEmbeddedCanvas("ec4", fF51, 100, 100), fL4);
   fF51->AddFrame(fE1[4] = new TRootEmbeddedCanvas("ec5", fF51, 100, 100), fL4);
   fF51->AddFrame(fE1[5] = new TRootEmbeddedCanvas("ec6", fF51, 100, 100), fL4);
   tf->AddFrame(fF50,fL4);tf->AddFrame(fF51,fL4);
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

   int nplot=nlay[idet]*nlr[idet];
   for (int ii=0; ii<nplot ; ii++) {fE1[ii]->GetCanvas()->SetBorderMode(0);}

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
   tf->AddFrame(fF3b,fL3);

   switch (idet) {
   case 0:
   fL4 = new TGLayoutHints(kLHintsTop    | kLHintsLeft | kLHintsExpandX | kLHintsExpandY, 5, 5, 5, 5);
   fF50b = new TGCompositeFrame(tf, 60, 60, kVerticalFrame);
   fF51b = new TGCompositeFrame(tf, 60, 60, kVerticalFrame);
   fF50b->AddFrame(fE2[0] = new TRootEmbeddedCanvas("ec1", fF50b, 100, 100), fL4);
   fF50b->AddFrame(fE2[1] = new TRootEmbeddedCanvas("ec2", fF50b, 100, 100), fL4);
   fF50b->AddFrame(fE2[2] = new TRootEmbeddedCanvas("ec3", fF50b, 100, 100), fL4);
   fF51b->AddFrame(fE2[3] = new TRootEmbeddedCanvas("ec4", fF51b, 100, 100), fL4);
   fF51b->AddFrame(fE2[4] = new TRootEmbeddedCanvas("ec5", fF51b, 100, 100), fL4);
   fF51b->AddFrame(fE2[5] = new TRootEmbeddedCanvas("ec6", fF51b, 100, 100), fL4);
   tf->AddFrame(fF50b,fL4);tf->AddFrame(fF51b,fL4);
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

   for (int ii=0; ii<nplot ; ii++) {fE2[ii]->GetCanvas()->SetBorderMode(0);}

   AddFrame(fTab, fL5);

   MapSubwindows();
   Resize();                    
   CenterOnParent();            
   SetWindowName("Dialog");
   MapWindow();
   //fClient->WaitFor(this);    // otherwise canvas contextmenu does not work
   HistAccumulate = 0;

   if(tcpvme->m_bConnected) {Init() ; ReadVME(); UpdateGUI();}

}

void Dsc2Dlg::DoSlider()
{
  char buf[32];
  Float_t zmin,zmax;
  zmin=fHSlid->GetMinPosition();
  zmax=fHSlid->GetMaxPosition();
  printf("zmin,zmax=%f,%f\n",zmin,zmax);

  //  sprintf(buf,"%.3f", fHSlid->GetMinPosition());
}

void Dsc2Dlg::HandleButton()
{
}

void Dsc2Dlg::HandleMotion()
{
}

Dsc2Dlg::~Dsc2Dlg()
{
   // Delete Dsc2Dlg widgets.

}

void Dsc2Dlg::MakeHistos()
{
  Char_t tit[10];  
  Int_t nplot,np;

  gROOT->SetStyle("Plain");
  gStyle->SetPalette(53);
  gStyle->SetLabelSize(0.06,"Z") ; gStyle->SetLabelSize(0.05,"Y") ; gStyle->SetLabelSize(0.05,"X");

  TStopwatch sw ; sw.Start();
  TDatime dtime ; gStyle->SetTimeOffset(dtime.Convert());
  bintime=1./norm;
  nplot=nlay[idet]*nlr[idet];

  for(np=0; np<nplot; np++)
    {
      if (!fHP1[np]){
	sprintf(tit,"hp1%d",np);
	fHP1[np] = new TH1F(tit,htit1[idet][np],npmt[idet][np],1.,((Float_t)npmt[idet][np]+1.));
	fHP1[np]->SetFillColor(kRed);fHP1[np]->SetStats(kFALSE);
        fHP1[np]->SetNdivisions(npmt[idet][np]); fHP1[np]->GetYaxis()->SetTickLength(0.01);
      }
      if (fHP2[np]) delete fHP2[np];sprintf(tit,"hp2%d",np);
      fHP2[np] = new TH2F(tit,htit2[idet][np],100,1.,101.*bintime,npmt[idet][np],1.,((Float_t)npmt[idet][np]+1.));
      fHP2[np]->SetStats(kFALSE);fHP2[np]->GetXaxis()->SetTimeDisplay(1); 
      fHP2[np]->GetZaxis()->SetRangeUser(1.,1500.); fHP2[np]->GetYaxis()->SetTitle(htit3[idet][np]);
      fHP2[np]->GetYaxis()->SetNdivisions(npmt[idet][np]/2);fHP2[np]->GetYaxis()->SetTickLength(0.01);
      fHP2[np]->GetYaxis()->SetTitleSize(0.06);fHP2[np]->GetYaxis()->SetTitleOffset(0.5); 
      fHP2[np]->GetYaxis()->CenterTitle();
    }
}

void Dsc2Dlg::FillHistos()
{
   // Fill histograms till user clicks "Stop Filling" button.
  TCanvas *c[6];
  Int_t nplot,np;

  nplot = nlay[idet]*nlr[idet];

  if (ifirst>0) {MakeHistos();ttt=0.;}

  if(fShowRates)  {for (np=0 ; np<nplot ; np++) {c[np] = fE1[np]->GetCanvas(); c[np]->SetLogy(SetYlog);}}
  if(!fShowRates) {for (np=0 ; np<nplot ; np++) {c[np] = fE2[np]->GetCanvas(); c[np]->SetLogy(0); c[np]->SetLogz(SetZlog);}}

  Int_t ii,jj,kk;
  Double_t xx,ww;

  if(fShowRates && !HistAccumulate) {for (np=0 ; np<nplot ; np++) {fHP1[np]->Reset();}}

  ttt=ttt+bintime;
  printf("Here tt=%f\n",ttt);
  
  if (ttt>bintime*100) {MakeHistos();ttt=bintime;}

  np=0;
  for(ii=0; ii<nlay[idet] ; ii++)
    {
      for(jj=0; jj<nlr[idet]; jj++)
	{
	  printf("ii,,jj,np=%d,%d,%d\n",ii,jj,np);
	  for(kk=0; kk<npmt[idet][np] ; kk++)
	    {
	      xx = (Double_t)kk+1; ww = (Double_t)scal2[ii][jj][kk];
	      fHP1[np]->Fill(xx,ww);
	      fHP2[np]->Fill(ttt,xx,ww);
	    }
	  np++;
	}
    }

  for(np=0; np<nplot ; np++)
    {
      c[np]->cd(); if(fShowRates) {fHP1[np]->Draw();}else{fHP2[np]->Draw("colz");}
      c[np]->Modified(); c[np]->Update();
    }

//gSystem->ProcessEvents();  // handle GUI events

}

void Dsc2Dlg::CloseWindow()
{
   // Called when window is closed (via the window manager or not).
   // Let's stop histogram filling...
   fShowRates = kFALSE;
   // Add protection against double-clicks
   fOkButton->SetState(kButtonDisabled);
   fCancelButton->SetState(kButtonDisabled);
   // ... and close the Ged editor if it was activated.
   if (TVirtualPadEditor::GetPadEditor(kFALSE) != 0)
      TVirtualPadEditor::Terminate();
   DeleteWindow();

   fMain->ClearDsc2Dlg(); // clear pointer to ourself, so MainFrame will stop reading scalers from VME

}


Bool_t Dsc2Dlg::ProcessMessage(Long_t msg, Long_t parm1, Long_t)
{
  //Int_t status;

   // Process messages coming from widgets associated with the dialog.

   switch (GET_MSG(msg)) {
      case kC_COMMAND:

         switch (GET_SUBMSG(msg)) {
            case kCM_BUTTON:
               switch(parm1) {
                  case 1:
                  case 2:
                     printf("\nTerminating dialog: %s pressed\n",
                            (parm1 == 1) ? "OK" : "Cancel");
                     CloseWindow();
                     break;
                 default:
                     break;
               }
               break;
			   /*
            case kCM_RADIOBUTTON:
               switch (parm1) {
                  case 81:
		     printf("RADIOBUTTON 81: %d\n",fRad1->GetState());
                     if(fRad1->GetState()) fRad1->SetState(kButtonUp);
                     else fRad1->SetState(kButtonDown);
                     break;
                  case 82:
                     fRad1->SetState(kButtonUp);
                     break; 
               }
               break;
			   */
			   
            case kCM_CHECKBUTTON:
               switch (parm1) {
                  case 71:
		     printf("CHECKBUTTON 71\n");
                     HistAccumulate = fChk1->GetState();
		     printf("CHECKBUTTON 71: status=%d\n",HistAccumulate); 
                     /*fListBox->SetMultipleSelections(fCheck1->GetState());*/
                     break;
	          case 72:
		     printf("CHECKBUTTON 72\n");
		     SetZlog = fChk2->GetState();
		     break;
	          case 73:
		     printf("CHECKBUTTON 73\n");
		     SetYlog = fChk3->GetState();
		     break;
                  default:
                     break;
               }
               break;
			   
            case kCM_TAB:
	      switch(parm1) {
	      case 1: fShowRates = kTRUE  ; break;
	      case 2: fShowRates = kFALSE ; break;
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

void Dsc2Dlg::Init()
{
  Int_t ii;

  for(ii=0; ii<4; ii++) addr[0][ii]    = ECAL_DSC2_ADDRESS_1 + ii*0x00080000;
  for(ii=0; ii<4; ii++) addr[0][ii+4]  = ECAL_DSC2_ADDRESS_2 + ii*0x00080000;
  for(ii=0; ii<4; ii++) addr[0][ii+8]  = ECAL_DSC2_ADDRESS_3 + ii*0x00080000;
  for(ii=0; ii<2; ii++) addr[0][ii+12] = ECAL_DSC2_ADDRESS_4 + ii*0x00080000;

  for(ii=0; ii<4; ii++) addr[1][ii]    = PCAL_DSC2_ADDRESS_1 + ii*0x00080000;
  for(ii=0; ii<4; ii++) addr[1][ii+4]  = PCAL_DSC2_ADDRESS_2 + ii*0x00080000;
  for(ii=0; ii<4; ii++) addr[1][ii+8]  = PCAL_DSC2_ADDRESS_3 + ii*0x00080000;

  addr[2][0]  = FTOF_DSC2_ADDRESS_SLOT2;
  addr[2][1]  = FTOF_DSC2_ADDRESS_SLOT4;
  addr[2][2]  = FTOF_DSC2_ADDRESS_SLOT5;
  addr[2][3]  = FTOF_DSC2_ADDRESS_SLOT7;
  addr[2][4]  = FTOF_DSC2_ADDRESS_SLOT8;
  addr[2][5]  = FTOF_DSC2_ADDRESS_SLOT10;
  addr[2][6]  = FTOF_DSC2_ADDRESS_SLOT12;
  addr[2][7]  = FTOF_DSC2_ADDRESS_SLOT14;
  addr[2][8]  = FTOF_DSC2_ADDRESS_SLOT15;
  addr[2][9]  = FTOF_DSC2_ADDRESS_SLOT17;
  addr[2][10] = FTOF_DSC2_ADDRESS_SLOT18;
  addr[2][11] = FTOF_DSC2_ADDRESS_SLOT20; 
}

void Dsc2Dlg::ReadVME()
{
  Int_t ii, jj, i=0, j=0, k=0;

  if (norm==0.) ifirst=3;
  if (ifirst>0) ifirst--;

  if(tcpvme->m_bConnected)
  {
    for(ii=0; ii<ndsc[idet]; ii++) tcpvme->VMEWrite32(addr[idet][ii] + DSC2_SCALER_GATE, 0x0004, FALSE);
    for(ii=0; ii<ndsc[idet]; ii++) tcpvme->VMEWrite32(addr[idet][ii] + DSC2_SCALER_LATCH,0x0000, FALSE);
    for(ii=0; ii<ndsc[idet]; ii++) tcpvme->VMERead32(addr[idet][ii]  + DSC2_SCALER_REF, &ref[ii], FALSE);

    if(ref[0]>0)
      {
	for(ii=0; ii<ndsc[idet]; ii++) 
	  {
	    norm = 125000000./((Float_t)ref[ii]);
	    for(jj=0; jj<16; jj++) {
	      tcpvme->VMERead32(addr[idet][ii]+DSC2_SCALER_BASE+jj*4,&scal1[ii][jj],FALSE);
	      switch (idet){
	      case 0: i=adclayerecal[map[ii]][jj]-1 ; j=0                        ; k=adcstripecal[map[ii]][jj]-1 ;break;
	      case 1: i=adclayerpcal[map[ii]][jj]-1 ; j=0                        ; k=adcstrippcal[map[ii]][jj]-1 ;break;
	      case 2: i=adclayerftof[map[ii]][jj]-1 ; j=adclrftof[map[ii]][jj]-1 ; k=adcslabftof[map[ii]][jj]-1  ;break;
	      }
	      scal1[ii][jj]=(Int_t)(((Float_t)scal1[ii][jj])*norm) ; scal2[i][j][k]=scal1[ii][jj];
	    }
	  }
      }
    UpdateGUI();
    FillHistos();
  }

}

void Dsc2Dlg::UpdateGUI()
{
   Int_t ii,jj;
   Char_t str[10];
   for(ii=0; ii<12; ii++) {for(jj=0; jj<16; jj++){sprintf(str,"%8d",scal1[ii][jj]);tentsc[ii][jj]->SetText(str);}}
}

/****************/
/* Main program */

int
main(int argc, char **argv)
{
   TApplication theApp("App", &argc, argv);

   if(gROOT->IsBatch())
   {
     fprintf(stderr, "%s: cannot run in batch mode\n", argv[0]);
     return(1);
   }

   if(argc != 2)
   {
     fprintf(stderr, "error: have to specify hostname\n");
     return(1);     
   }

   printf("Trying to connect to >%s<\n",argv[1]);

   PCALMainFrame mainWindow(gClient->GetRoot(), 200, 200, argv[1]);

   theApp.Run();

   return 0;
}
