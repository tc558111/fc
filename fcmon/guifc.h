/* guifc.h */

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
#include <TPaveText.h>
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
#include <TTimer.h>
#include <TStopwatch.h>
#include <TDatime.h>
#include <TQObject.h>
#include <TGStatusBar.h>

#include <iostream>

#define NU 68
#define NV 62
#define NW 62

enum ETestCommandIdentifiers
  {
    M_FILE_OPEN,
    M_FILE_SAVE,
    M_FILE_SAVEAS,
    M_FILE_PRINT,
    M_FILE_PRINTSETUP,
    M_FILE_EXIT,
   
    M_DSC2,

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

class TileFrame : public TGCompositeFrame
{
 private:
  TGCanvas *fCanvas;

 public:
  TileFrame(const TGWindow *p);
  virtual ~TileFrame() { }

  void SetCanvas(TGCanvas *canvas) { fCanvas = canvas; }
  Bool_t HandleButton(Event_t *event);
};

class IDList
{
 private:
  Int_t nID;
 public:

 IDList() : nID(0) {}
  ~IDList() {}
  Int_t GetUnID(void) { return ++nID; }
};

class FCMainFrame : public TGMainFrame
{
 private:

  TGDockableFrame    *fMenuDock;
  TGHorizontalFrame  *fControlFrame, *fActionFrame;
  TileFrame          *fContainer;

  TGMenuBar          *fMenuBar;
  TGPopupMenu        *fMenuFile, *fMenuMC, *fMenuView, *fMenuHelp;
  TGLayoutHints      *fMenuBarLayout, *fMenuBarItemLayout, *fMenuBarHelpLayout;
  TGLayoutHints      *fL10, *fL0, *fL1;

  TGRadioButton      *fRadiob1[6],*fRadiob2[4],*fRadiob3[3]; 
  TGVButtonGroup     *fButtonGroup1,*fButtonGroup2,*fButtonGroup3;

  IDList              IDs;           // Widget IDs generator

 public:

  Dsc2Dlg            *fDsc2Dlg;
  DelaysDlg          *fDelaysDlg;
  ScopeDlg           *fScopeDlg;

  FCMainFrame(const TGWindow *p, UInt_t w, UInt_t h);
  virtual ~FCMainFrame();

  TGStatusBar        *fStatusBar1;
  virtual void CloseWindow();
  virtual Bool_t ProcessMessage(Long_t msg, Long_t parm1, Long_t);
  void connect_to_server();
  int get_crate_map();
  void ClearDsc2Dlg() {fDsc2Dlg = NULL;}

};

class Dsc2Dlg : public TGTransientFrame
{
 private:

  FCMainFrame         *fMain;
  TGCompositeFrame    *fFrame1, *fF1, *fF2, *fF3, *fF4, *fF50, *fF51, *fF50b, *fF51b, *fF3b;
  TGHorizontalFrame   *fF4b;
  TGGroupFrame        *fF6[12], *fF7;
  TGButton            *fChk1, *fChk2, *fChk3, *fChk4, *fChk5, *fCancelButton;
  TGDoubleHSlider     *fHSlid;
  TGTab               *fTab;
  TGLayoutHints       *fL1, *fL2, *fL3, *fL4, *fL5, *fL10;
  TGStatusBar         *fStatusBar2;
  TRootEmbeddedCanvas *fE1[6], *fE2[6];
  Bool_t               fShowRates, fShowStripChart;
 
  TGTextBuffer        *tbufsc[16][16];
  TGTextEntry         *tentsc[16][16];
  TH1F                *fHP1[68];
  TH2F                *fHP2[68];

  void MakeHistos();
  void FillHistos();
  void DrawHistos();
  void DeleteHistos();
   
  Int_t HistAccumulate;
  Int_t SetZlog, SetYlog, SetIO;

 public:
  Dsc2Dlg(const TGWindow *p, FCMainFrame *main, UInt_t w, UInt_t h,
               UInt_t options = kVerticalFrame);
  virtual ~Dsc2Dlg();

  virtual void CloseWindow();
  virtual Bool_t ProcessMessage(Long_t msg, Long_t parm1, Long_t parm2);

  virtual void ReadVME();
  virtual void UpdateGUI();
  virtual void disconnect_from_server();
   
  // slots
  int refresh_scalers();
  void refresh_statusbar();
  void set_status_text(const char *txt, Int_t pi);
  void DoSlider();       
  
  ClassDef(Dsc2Dlg,0) 

};

