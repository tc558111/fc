/* guipcal.h */

#define NU 68
#define NV 62
#define NW 62

enum ETestCommandIdentifiers {
   M_FILE_OPEN,
   M_FILE_SAVE,
   M_FILE_SAVEAS,
   M_FILE_PRINT,
   M_FILE_PRINTSETUP,
   M_FILE_EXIT,

   M_REGISTERS,
   M_DELAYS,
   M_DSC2,
   M_SCOPE_ASCII,
   M_SCOPE_CANVAS,

   M_VIEW_ENBL_DOCK,
   M_VIEW_ENBL_HIDE,
   M_VIEW_DOCK,
   M_VIEW_UNDOCK,

   M_HELP_CONTENTS,
   M_HELP_SEARCH,
   M_HELP_ABOUT
};


class Dsc2Dlg;
class DelaysDlg;
class ScopeDlg;
class MyTimer;

class TileFrame : public TGCompositeFrame {

private:
   TGCanvas *fCanvas;

public:
   TileFrame(const TGWindow *p);
   virtual ~TileFrame() { }

   void SetCanvas(TGCanvas *canvas) { fCanvas = canvas; }
   Bool_t HandleButton(Event_t *event);
};

class IDList {

private:
   Int_t nID;   // creates unique widget's IDs

public:
   IDList() : nID(0) {}
   ~IDList() {}
   Int_t GetUnID(void) { return ++nID; }
};

class FCMainFrame : public TGMainFrame {

private:

   TGDockableFrame    *fMenuDock;
   TGCompositeFrame   *fStatusFrame;
   TGCanvas           *fCanvasWindow;
   TGHorizontalFrame  *fControlFrame, *fActionFrame;
   TileFrame          *fContainer;
   TGTextEntry        *fTestText;
   TGButton           *fTestButton;
   TGColorSelect      *fColorSel;

   TGMenuBar          *fMenuBar;
   TGPopupMenu        *fMenuFile, *fMenuPCAL, *fMenuView, *fMenuHelp;
   TGPopupMenu        *fCascadeMenu, *fCascade1Menu, *fCascade2Menu;
   TGPopupMenu        *fMenuNew1, *fMenuNew2;
   TGLayoutHints      *fMenuBarLayout, *fMenuBarItemLayout, *fMenuBarHelpLayout;
   TGLayoutHints      *fL10, *fL0, *fL1;
   TGCompositeFrame   *fHor1;
   TGTextButton       *fExit;
   TGGroupFrame       *fGframe;
   TGNumberEntry      *fNumber;
   TGLabel            *fLabel;

   TGGroupFrame       *fF6;
   TGLayoutHints      *fL3;

   TGRadioButton      *fRadiob1[6],*fRadiob2[3],*fRadiob3[2]; 
   TGVButtonGroup     *fButtonGroup1,*fButtonGroup2,*fButtonGroup3;
   TGTextButton       *fWindowRatesButton;
   TGTextButton       *fWindowExitButton;

   TGTextButton *btConnect, *btDisconnect;

   IDList              IDs;           // Widget IDs generator
   MyTimer             *tt;

public:

   Dsc2Dlg            *fDsc2Dlg;
   DelaysDlg          *fDelaysDlg;
   ScopeDlg           *fScopeDlg;

   FCMainFrame(const TGWindow *p, UInt_t w, UInt_t h);
   virtual ~FCMainFrame();

   virtual void CloseWindow();
   virtual Bool_t ProcessMessage(Long_t msg, Long_t parm1, Long_t);
   virtual void connect_to_server();

   int get_crate_map();
   void ClearDsc2Dlg() {fDsc2Dlg = NULL;}
   void ClearDelaysDlg() {fDelaysDlg = NULL;}

   //void ClearScopeDlg() {fScopeDlg = NULL;}  // sergey: crash here !!!

};

class Dsc2Dlg : public TGTransientFrame {

private:

   FCMainFrame         *fMain;
   TGCompositeFrame    *fFrame1, *fF1, *fF2, *fF3, *fF4, *fF50, *fF51, *fF50b, *fF51b, *fF3b;
   TGHorizontalFrame   *fF4b;
   TGGroupFrame        *fF6[12], *fF7;
   TGButton            *fOkButton, *fCancelButton, *fStartB, *fStopB;
   TGButton            *fBtn1, *fBtn2, *fChk1, *fChk2, *fChk3, *fRad1, *fRad2;
   TGDoubleHSlider     *fHSlid;
   TGPictureButton     *fPicBut1;
   TGCheckButton       *fCheck1;
   TGCheckButton       *fCheckMulti;
   TGListBox           *fListBox;
   TGComboBox          *fCombo;
   TGTab               *fTab;
   TGTextEntry         *fTxt1, *fTxt2;
   TGLayoutHints       *fL1, *fL2, *fL3, *fL4, *fL5;
   TRootEmbeddedCanvas *fE1[6], *fE2[6];
   Int_t                fFirstEntry;
   Int_t                fLastEntry;
   Bool_t               fShowRates;
 
   TGTextBuffer        *tbufsc[12][16];
   TGTextEntry         *tentsc[12][16];
   TH1F                *fHP1[68];
   TH2F                *fHP2[68];

   UInt_t ref[16];

   void FillHistos();
   void MakeHistos();
   
   Int_t HistAccumulate;
   Int_t SetZlog, SetYlog;

public:
   Dsc2Dlg(const TGWindow *p, FCMainFrame *main, UInt_t w, UInt_t h,
               UInt_t options = kVerticalFrame);
   virtual ~Dsc2Dlg();

   virtual void CloseWindow();
   virtual Bool_t ProcessMessage(Long_t msg, Long_t parm1, Long_t parm2);

   virtual void ReadVME();
   virtual void UpdateGUI();

   virtual void HandleButton();
   virtual void HandleMotion();

   void DoSlider();

};

class DelaysDlg : public TGTransientFrame {

private:
   FCMainFrame       *fMain;
   TGCompositeFrame    *fFrame1, *fF1, *fF2, *fF3, *fF4, *fF5;
   TGGroupFrame        *fF6, *fF7;
   TGButton            *fOkButton, *fCancelButton, *fStartB, *fStopB;
   TGButton            *fBtn1, *fBtn2, *fChk1, *fChk2, *fChk3, *fRad1, *fRad2;
   TGPictureButton     *fPicBut1;
   TGCheckButton       *fCheck1;
   TGCheckButton       *fCheckMulti;
   TGListBox           *fListBox;
   TGComboBox          *fCombo;
   TGTab               *fTab;
   TGTextEntry         *fTxt1, *fTxt2;
   TGLayoutHints       *fL1, *fL2, *fL3, *fL4;
   TRootEmbeddedCanvas *fEc1, *fEc2;
   Int_t                fFirstEntry;
   Int_t                fLastEntry;

   TGNumberEntry *tnumU1[68], *tnumV1[62], *tnumW1[62];

   UInt_t U1[68], V1[62], W1[62];
   UInt_t U1GUI[68], V1GUI[62], W1GUI[62];

public:
   DelaysDlg(const TGWindow *p, FCMainFrame *main, UInt_t w, UInt_t h,
               UInt_t options = kVerticalFrame);
   virtual ~DelaysDlg();
   virtual void CloseWindow();
   virtual Bool_t ProcessMessage(Long_t msg, Long_t parm1, Long_t parm2);

   virtual void ReadVME();
   virtual void WriteVME();
   virtual Bool_t ReadGUI();
   virtual Bool_t UpdateGUI();
};


class RegistersDlg : public TGTransientFrame {

private:
   TGVerticalFrame      *fF1;
   TGVerticalFrame      *fF2;
   TGHorizontalFrame    *fF[13];
   TGLayoutHints        *fL1;
   TGLayoutHints        *fL2;
   TGLayoutHints        *fL3;
   TGLabel              *fLabel[13];
   TGNumberEntry        *fNumericEntries[13];
   TGCheckButton        *fLowerLimit;
   TGCheckButton        *fUpperLimit;
   TGNumberEntry        *fLimits[2];
   TGCheckButton        *fPositive;
   TGCheckButton        *fNonNegative;
   TGButton             *fSetButton;
   TGButton             *fExitButton;

   static const char *const numlabel[13];
   static const Double_t numinit[13];

public:
   RegistersDlg(const TGWindow *p, const TGWindow *main);
   virtual ~RegistersDlg();
   virtual void CloseWindow();

   void SetLimits();
   virtual Bool_t ProcessMessage(Long_t msg, Long_t parm1, Long_t);
};


class MyTimer : public TTimer
{
private:
  FCMainFrame   *fFCMainFrame;   //display to which this timer belongs

public:
  MyTimer(FCMainFrame *m, Long_t ms);
  Bool_t  Notify();
};
