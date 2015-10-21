/* et2rzh.c */
/* Cole Smith */
/* Hacked from original code evio_sectorhist.c by Sergey Boiarinov (JLAB) */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <malloc.h>
#include <math.h>
#include <time.h>

#include "evio.h"
#include "et.h"
#include "etserg.h"
#include "evnlink.h"
#include "ttfc.h"
#include "tabread.h"

#undef DEBUG_SEARCH

#undef DEBUG
 
#define NWPAWC 20000000 /* Length of the PAWC common block. */
#define LREC 1024
#define SIZ 8176

struct {
  float hmemor[NWPAWC];
} pawc_;

int quest_[100];

static int nslabs[3]={62,23,5};

/* translated data */

#define NHITSSC 100
static int nadc[3][2][62], adc[3][2][62][NHITSSC];
static int ntdc[3][2][62], tdc[3][2][62][NHITSSC];
#define NHITSPC 192
static int npadc[3][68], padc[3][68][NHITSPC];
static int nptdc[3][68], ptdc[3][68][NHITSPC];
static int npl[3],adcr[3][68],strr[3][68];
#define NHITSEC 216
static int neadc[6][36], eadc[6][36][NHITSEC];
static int netdc[6][36], etdc[6][36][NHITSEC];

#define ABS(x) ((x) < 0 ? -(x) : (x))

#define NCHAN 256 /*how many channels to draw*/

#define TDCRES 41.66667
#define TDCLSB 24
#define tid 100000
#define iid 10000
  
char titnew[128],titold[128],dtnew[128],dtold[128];
int pparms[10]={1,5,20000,-1,3,10,10,100,0,0};
void plot1d(int datasaved[][16], int *baseline, int nsamples);
void utilSetupGraf1(void);
void utilGenColormap(int map);
int tabread(int n, float arr[n][n], char fname[]);

int main(int argc, char **argv)
{
  FILE *fd = NULL;
  int bco1[256], bco2[256], bco3[256], bco4[256], nbco1, nbco2, nbco3, nbco4, diff, diff1, diff2;
  int nfile, nevents, etstart, etstop, len;
  char filename[1024];
  int handler, status, ifpga, nchannels, tdcref;
  unsigned long long *b64, timestamp, timestamp_old;
  unsigned int *b32;
  unsigned short *b16;
  unsigned char *b08;
  int trig,chan,fpga,apv,hybrid;
  int i1, type, timestamp_flag;
  float f1,f2;
  unsigned int word;
  int iet, maxevents;
  int fragment, sector, detector; /* 1<=fragment<=36; 1<=sector<=6; detector: 1-ecal, 2-pcal, 3-ftof */

  time_t rawtime;
  struct tm *info;
  char tbuf[80];

  time(&rawtime);
  info=localtime(&rawtime);
  printf("Current local time and date: %s",asctime(info));  

  /*arg-slot(from 0), fun-histnum*/
  /*3,4,5,6,7,8,9,10,13,14,15,16*/
  int adcslot2hist[22] = {0,0,0,1,2,3,4,5,6,7,8,0,0,9,10,11,12,0,0,0,0,0};
  /*3,6,9,13,16*/
  int tdcslot2hist[22] = {0,0,0,1,0,0,2,0,0,3,0,0,0,4,0,0,5,0,0,6,0,0};

  int nr,sec,strip,nl,ncol,nrow,i,j, k, ii,jj,kk,l,l1,l2,ichan,nn,mm,iev,nbytes,ind1;
  char title[128], *ch;
  char HBOOKfilename[256], PEDfilename[256], chrunnum[32];
  int runnum;

  int nwpawc,lun,lrec,istat,icycle,idn,idn1,idn2,nbins,nxbins,nx2bins,nybins,nbins1,igood,offset;
  float x1,x2,y1,y2,x22,ww,tmpx,tmpy,ttt,ref;
  int doplt;

  if(argc == 1)
  {
    printf("Usage: et2rzh <input> <runno> <sector> <maxevents> <det> <slot> <thrsh> <nskp> <ymax> \n");
    printf("Purpose: Creates histo file 'forcar_s<sector>_<runno>.rzh' for use with fcMon.kumac \n");
    printf("Purpose: Also use as realtime monitor of FADC data in raw mode \n");
    printf("<input>=Enter et or evio filename \n");
    printf("<det>=0,1,2 for ecal,pcal,ftof \n");
    printf("<slot>=FADC slot <3-10,13-16 (FTOF,PCAL) 13-18 (ECAL)>\n");
    printf("<thrsh>=For at least one channel per slot pulse sum must exceed <thrsh> \n");
    printf("<nskp>=Skip <nskp> events before plotting \n");
    printf("<ymax>=Max y scale of FADC vs. samples \n");
    exit(1);
  }

  doplt=argc>5;

  /* check if events come from ET system */
  
  if(!strncmp(argv[1],"et",2))
  {
    sprintf(et_name,"/tmp/et_sys_clasprod%d",atoi(argv[3]));
    FILE *fd;
    fd = fopen(et_name,"r");
    if(fd!=NULL)
	{
      fclose(fd);
      printf("attach to ET system >%s<\n",et_name);
	}
    else
	{
      printf("ET system >%s< does not exist - exit\n",argv[1]);
      exit(0);
	}
	/*
    if (!et_alive(et_sys))
    {
      printf("ERROR: not attached to ET system\n");
      et_reinit = 1;
      exit(0);
    }
	*/
    if(et_initialize() != 0)
    {
      printf("ERROR: cannot initalize ET system\n");
      exit(0);
    }

    done = 0;
    use_et = 1;
  }


  if(use_et)
  {
    runnum = 1; /* temporary fake it, must extract from et */
    printf("run number is %d\n",runnum);
  }
  else
  {
    strcpy(chrunnum,argv[1]);
    ch = strchr(chrunnum,'0');
    ch[6] = '\0';
    runnum = atoi(ch);
    printf("run number is %s (%d)\n",ch,runnum);
  }

  /* HBOOK STUFF */

  int siz=SIZ;
  int kwkid=4;

  quest_[9]=65000;

  nwpawc = NWPAWC;
  hlimit_(&nwpawc);

  if(doplt) {hplint_(&kwkid); x1=30.; x2=18. ; hplsiz_(&x1,&x2," ",1L);utilSetupGraf1();}

  lun = 11;
  lrec = LREC;

  int maxev,pdet,pslt,thrsh=0,nskp;
  float ymax;

  runnum = pparms[0] = atoi(argv[2]);
  sec    = pparms[1] = atoi(argv[3]);
  maxev  = pparms[2] = atoi(argv[4]);
  pslt   = pparms[4];
  thrsh  = pparms[5];
  nskp   = pparms[6];
  ymax   = pparms[7];

  if(doplt)  {pdet  = pparms[3] = atoi(argv[5]);}
  if(argc>6) {pslt  = pparms[4] = atoi(argv[6]);}
  if(argc>7) {thrsh = pparms[5] = atoi(argv[7]);}
  if(argc>8) {nskp  = pparms[6] = atoi(argv[8]);}
  if(argc>9) {ymax  = pparms[7] = atof(argv[9]);}

  /* NSA+NSB to calculate pedestals*/
  /* From $CLON_PARMS/fadc250/adc*mode3.cnf */

  int nsat[3]={12,12,12};   /* ns */
  //int nsbt[3]={76,156,60};  /* runs 170,171 */
  int nsbt[3]={36,156,44};
  int nsa[3] ={3,3,3};      /* samples */
  int nsb[3] ={9,39,11};
  
  /* PEDESTAL TABLES */

  float tabecal[22][16];
  float tabpcal[22][16];
  float tabftof[22][16];

  int pedrun[6]={169,0,0,0,0,0};

  int columns=16;

  sprintf(PEDfilename,"cal/ped/ecal/forcar-s%d-%d-ecal.ped",sec,pedrun[sec-1]);
  tabread(columns,tabecal,PEDfilename);
  sprintf(PEDfilename,"cal/ped/pcal/forcar-s%d-%d-pcal.ped",sec,pedrun[sec-1]);
  tabread(columns,tabpcal,PEDfilename);
  sprintf(PEDfilename,"cal/ped/ftof/forcar-s%d-%d-ftof.ped",sec,pedrun[sec-1]);
  tabread(columns,tabftof,PEDfilename);

  
  for(i=0;i<22;i++){
    for(j=0;j<16;j++)
      printf("%lf%s",tabecal[i][j], j < columns-1 ? "\t" : "\n");
    
  }

  for(i=0;i<22;i++){
    for(j=0;j<16;j++)
      printf("%lf%s",tabpcal[i][j], j < columns-1 ? "\t" : "\n");
    
  }  

  for(i=0;i<22;i++){
    for(j=0;j<16;j++)
      printf("%lf%s",tabftof[i][j], j < columns-1 ? "\t" : "\n");
    
  }
  

  sprintf(HBOOKfilename,"forcar-s%d-%d.rzh",sec,runnum);

  hropen_(&lun,"FTOF",HBOOKfilename,"N",&lrec,&istat,strlen("FTOF"),strlen(HBOOKfilename),1);

  if(istat)
    {
    printf("\aError: cannot open RZ file %s for writing.\n", HBOOKfilename);fflush(stdout);
    exit(0);
    }
  else
    {
    printf("RZ file >%s< opened for writing, istat = %d\n\n", HBOOKfilename, istat);fflush(stdout);
    }


  /**************/
  /* histograms

   id = fragment*100000 + slot*1000 + channel
        detector*100000 + layer*100 + channel;

  where:
    fragment: 1-36
    detector: 1-ecal, 2-pcal, 3-ftof


   TDC for every slot/channel (1D) 5*32 idn=100/200/300/400/500 
   ADC pedestals for every slot/channel (1D) 16*10 idn=580/630/680/../1030 
   ADC for every slot/channel (1D) 16*10 idn=1100/1200/../2000 

   TDC L and R for every layer/slab (1D) 23*6 idn=2100/2200/2300/2400/2500/2600 
   ADC L and R for every layer/slab (1D) 23*6 idn= 3100/3200/3300/3400/3500/3600 
   TDCL vs TDCR for every layer/slab (2D) 23*3 vs 23*3 idn=4100/4200/4300 
   ADCL vs ADCR for every layer/slab (2D) 23*3 vs 23*3 idn=5100/5200/5300 
   SQRT(ADCL * ADCR) for every layer/slab (1D) 23*3 idn=6100/6200/6300 
   LN(ADCR/ADCL) for every layer/slab (1D) 23*3 idn=7100/7200/7300 

  */

  int iff;
  iff=sec*6-5;
  int hid = 1e7*sec;

  /* REALTIME FADC PLOTS */

  if(doplt)
    {
      //char dlab[3][4]={{'E','C','A','L'},{'P','C','A','L'},{'F','T','O','F'}};
      if (pdet==0) {sprintf(title,"RUN %d   SECTOR %d   DETECTOR ECAL  SLOT %d   THRSH %d   NSKIP %d",runnum,sec,pslt,thrsh,nskp);}
      if (pdet==1) {sprintf(title,"RUN %d   SECTOR %d   DETECTOR PCAL  SLOT %d   THRSH %d   NSKIP %d",runnum,sec,pslt,thrsh,nskp);}
      if (pdet==2) {sprintf(title,"RUN %d   SECTOR %d   DETECTOR FTOF  SLOT %d   THRSH %d   NSKIP %d",runnum,sec,pslt,thrsh,nskp);}
      ii=1 ; istxci_(&ii) ; htitle_(title,strlen(title));
      float ymin=-5.;
      if (ymax<=30){ymin=-ymax;}
      if (ymax<0) {ymax=-ymax;ymin=1.;i1=1;hplopt_("LOGY",&i1,4L);}

      sprintf(title,"FADC RAW TIME");
      nbins=100; x1=0.; x2=100.; ww=0.;
      for (ii=0;ii<16;ii++)
	{
	  sprintf(title,"CHANNEL %d",ii);
	  idn1=700+ii; hbook1_(&idn1,title,&nbins,&x1,&x2,&ww,strlen(title));
	  idn2=800+ii; hbook1_(&idn2,title,&nbins,&x1,&x2,&ww,strlen(title));
	  hmaxim_(&idn1,&ymax); hminim_(&idn1,&ymin);
	  hmaxim_(&idn2,&ymax); hminim_(&idn2,&ymin);
	}
    }


  /* TDC for every slot/channel (1D) */

  nxbins=500;
  x1 = 800.;
  x2 = 2700.;
  nybins=128;
  y1 = 0.;
  y2 = 128.;
  ww = 0.;
  for(ii=3; ii<=16; ii++)
  {
    idn = (iff+1)*100000 + ii*1000;
    sprintf(title,"ecal tdc slot %02d",ii);
    hbook2_(&idn,title,&nxbins,&x1,&x2,&nybins,&y1,&y2,&ww,strlen(title));
    idn = (iff+3)*100000 + ii*1000;
    sprintf(title,"pcal tdc slot %02d",ii);
    hbook2_(&idn,title,&nxbins,&x1,&x2,&nybins,&y1,&y2,&ww,strlen(title));
    idn = (iff+5)*100000 + ii*1000;
    sprintf(title,"ftof tdc slot %02d",ii);
    hbook2_(&idn,title,&nxbins,&x1,&x2,&nybins,&y1,&y2,&ww,strlen(title));
  }

  idn=11;
  sprintf(title,"tdc_all");
  hbook1_(&idn,title,&nbins,&x1,&x2,&ww,strlen(title));

  nbins=1000;
  x1 = 0.;
  x2 = 1000.;
  ww = 0.;
  idn=12;
  sprintf(title,"tdc ref ns");
  hbook1_(&idn,title,&nbins,&x1,&x2,&ww,strlen(title));

  /* ADC pulses for every slot/channel (1D) */
  nxbins=100;
  x1 = 0.;
  x2 = 100.;
  nybins=16;
  y1 = 0.;
  y2 = 16.;
  ww = 0.;
  for(ii=3; ii<=20; ii++)
  {
    idn = 10000000+(iff)*100000 + ii*1000;
    sprintf(title,"ecal adcraw slot %02d",ii);
    hbook2_(&idn,title,&nxbins,&x1,&x2,&nybins,&y1,&y2,&ww,strlen(title));
    idn = 10000000+(iff+2)*100000 + ii*1000;
    sprintf(title,"pcal adcraw slot %02d",ii);
    hbook2_(&idn,title,&nxbins,&x1,&x2,&nybins,&y1,&y2,&ww,strlen(title));
    idn = 10000000+(iff+4)*100000 + ii*1000;
    sprintf(title,"ftof adcraw slot %02d",ii);
    hbook2_(&idn,title,&nxbins,&x1,&x2,&nybins,&y1,&y2,&ww,strlen(title));
  }

  /* ADC pedestals for every slot/channel (1D) */
  nxbins=300;
  x1 = 0.;
  x2 = 300.;
  nybins=16;
  y1 = 0.;
  y2 =  16.;
  ww = 0.;
  for(ii=3; ii<=20; ii++)
  {
    idn = 20000000+(iff)*100000 + ii*1000;
    sprintf(title,"ecal pedestal slot %02d",ii);
    hbook2_(&idn,title,&nxbins,&x1,&x2,&nybins,&y1,&y2,&ww,strlen(title));
    idn = 20000000+(iff+2)*100000 + ii*1000;
    sprintf(title,"pcal pedestal slot %02d",ii);
    hbook2_(&idn,title,&nxbins,&x1,&x2,&nybins,&y1,&y2,&ww,strlen(title));
    idn = 20000000+(iff+4)*100000 + ii*1000;
    sprintf(title,"ftof pedestal slot %02d",ii);
    hbook2_(&idn,title,&nxbins,&x1,&x2,&nybins,&y1,&y2,&ww,strlen(title));
  }

  idn=13;
  sprintf(title,"adc_all");
  hbook1_(&idn,title,&nbins,&x1,&x2,&ww,strlen(title));


  /* ADC spectra for every slot/channel (1D) */
  nxbins=100;
  x1 = 0.;
  x2 = 2500.;
  nx2bins=200;
  x22 = 8000;
  nybins=16;
  y1 = 0.;
  y2 =  16.;
  ww = 0.;
  for(ii=3; ii<=20; ii++)
  {
    idn = 30000000+(iff)*100000 + ii*1000;
    sprintf(title,"ecal adc slot %02d",ii);
    hbook2_(&idn,title,&nxbins,&x1,&x2,&nybins,&y1,&y2,&ww,strlen(title));
    idn = 30000000+(iff+2)*100000 + ii*1000;
    sprintf(title,"pcal adc slot %02d",ii);
    hbook2_(&idn,title,&nxbins,&x1,&x2,&nybins,&y1,&y2,&ww,strlen(title));
    idn = 30000000+(iff+4)*100000 + ii*1000;
    sprintf(title,"ftof adc slot %02d",ii);
    nx2bins=200;
    x22=8000.;
    hbook2_(&idn,title,&nx2bins,&x1,&x22,&nybins,&y1,&y2,&ww,strlen(title));
  }

  /* ADC for every PCAL layer/slab (2D) 68*3 idn=22000/22200/22300 */
  nxbins=100;
  x1 = 0.;
  x2 = 2000.;
  nybins=68;
  y1=1.;
  y2=69.;
  ww = 0.;
  for(ii=1; ii<=3; ii++)
  {
    idn=21800+200*ii;
    if (ii==1) sprintf(title,"PCAL U ADC");
    if (ii==2) sprintf(title,"PCAL V ADC");
    if (ii==3) sprintf(title,"PCAL W ADC");
    hbook2_(&idn,title,&nxbins,&x1,&x2,&nybins,&y1,&y2,&ww,strlen(title));
    if (ii==1) sprintf(title,"PCAL U ADC PIXEL");
    if (ii==2) sprintf(title,"PCAL V ADC PIXEL");
    if (ii==3) sprintf(title,"PCAL W ADC PIXEL");
    idn=hid+20*tid+iid+100*ii;
    hbook2_(&idn,title,&nxbins,&x1,&x2,&nybins,&y1,&y2,&ww,strlen(title));
    idn=hid+21*tid+iid+100*ii;
    hbook2_(&idn,title,&nxbins,&x1,&x2,&nybins,&y1,&y2,&ww,strlen(title));
    idn=hid+22*tid+iid+100*ii;
    hbook2_(&idn,title,&nxbins,&x1,&x2,&nybins,&y1,&y2,&ww,strlen(title));
  }

  /* TDC for every PCAL layer/slab (2D) 68*3 idn=26000/26200/26300 */
  nxbins=100;
  x1 = 800.;
  x2 = 2700.;
  nybins=68;
  y1=1.;
  y2=69.;
  ww = 0.;
  for(ii=1; ii<=3; ii++)
  {
    idn=25800+200*ii;
    if (ii==1) sprintf(title,"PCAL U TDC");
    if (ii==2) sprintf(title,"PCAL V TDC");
    if (ii==3) sprintf(title,"PCAL W TDC");
    hbook2_(&idn,title,&nxbins,&x1,&x2,&nybins,&y1,&y2,&ww,strlen(title));
  }

  /* ADC for every ECAL layer/slab (2D) 36*6 idn=32000/32200/32300... */
  nxbins=100;
  x1 = 0.;
  x2 = 2000.;
  nybins=36;
  y1=1.;
  y2=37.;
  ww = 0.;
  for(ii=1; ii<=6; ii++)
  {
    idn=31800+200*ii;
    if (ii==1) sprintf(title,"ECAL UI ADC");
    if (ii==2) sprintf(title,"ECAL VI ADC");
    if (ii==3) sprintf(title,"ECAL WI ADC");
    if (ii==4) sprintf(title,"ECAL UO ADC");
    if (ii==5) sprintf(title,"ECAL VO ADC");
    if (ii==6) sprintf(title,"ECAL WO ADC");
    hbook2_(&idn,title,&nxbins,&x1,&x2,&nybins,&y1,&y2,&ww,strlen(title));
  }
  
  /* TDC for every ECAL layer/slab (2D) 36*6 idn=36000/36200/36300... */
  nxbins=100;
  x1 = 800.;
  x2 = 2700.;
  nybins=36;
  y1=1.;
  y2=37.;
  ww = 0.;
  for(ii=1; ii<=6; ii++)
  {
    idn=35800+200*ii;
    if (ii==1) sprintf(title,"ECAL UI TDC");
    if (ii==2) sprintf(title,"ECAL VI TDC");
    if (ii==3) sprintf(title,"ECAL WI TDC");
    if (ii==4) sprintf(title,"ECAL UO TDC");
    if (ii==5) sprintf(title,"ECAL VO TDC");
    if (ii==6) sprintf(title,"ECAL WO TDC");
    hbook2_(&idn,title,&nxbins,&x1,&x2,&nybins,&y1,&y2,&ww,strlen(title));
  }

  /* TDC L and R for every layer/slab (1D) 23*6 idn=12100/12200/ 12300/12400/ 12500/12600 */
  nxbins=100;
  x1 = 800.;
  x2 = 2700.;
  nybins=62;
  y1=1.;
  y2=63.;
  ww = 0.;
  for(ii=1; ii<=3; ii++)
  {
    nybins=nslabs[ii-1];
    y2=(float)nybins+1;
    for(jj=1; jj<=2; jj++)
    {
     idn=11800+200*ii+100*jj;
     if (ii==1 && jj==1) sprintf(title,"FTOF1B LTDC");
     if (ii==1 && jj==2) sprintf(title,"FTOF1B RTDC");
     if (ii==2 && jj==1) sprintf(title,"FTOF1A LTDC");
     if (ii==2 && jj==2) sprintf(title,"FTOF1A RTDC");
     if (ii==3 && jj==1) sprintf(title,"FTOF2 LTDC");
     if (ii==3 && jj==2) sprintf(title,"FTOF2 RTDC");
     hbook2_(&idn,title,&nxbins,&x1,&x2,&nybins,&y1,&y2,&ww,strlen(title));
    }
  }

  /* ADC L and R for every layer/slab (1D) 23*6 idn= 13100/13200/ 13300/13400 /13500/13600 */
  nxbins=100;
  x1 = 0.;
  x2 = 8000.;
  nybins=62;
  y1=1.;
  y2=63.;
  ww = 0.;
  for(ii=1; ii<=3; ii++)
  {
    nybins=nslabs[ii-1];
    y2=(float)nybins+1;
    for(jj=1; jj<=2; jj++)
    {
     idn=12800+200*ii+100*jj;
     if (ii==1 && jj==1) {sprintf(title,"FTOF1B LADC");x2=9000;}
     if (ii==1 && jj==2) {sprintf(title,"FTOF1B RADC");x2=9000;}
     if (ii==2 && jj==1) {sprintf(title,"FTOF1A LADC");x2=4000;}
     if (ii==2 && jj==2) {sprintf(title,"FTOF1A RADC");x2=4000;}
     if (ii==3 && jj==1) sprintf(title,"FTOF2 LADC");
     if (ii==3 && jj==2) sprintf(title,"FTOF2 RADC");
     hbook2_(&idn,title,&nxbins,&x1,&x2,&nybins,&y1,&y2,&ww,strlen(title));
    }
  }

  /* TDCL vs TDCR for every layer/slab (2D) 23*3 vs 23*3 idn=14100/14200/14300 */
  nxbins=40;
  nybins=40;
  x1 = 800.;
  x2 = 1200.;
  y1 = 800.;
  y2 = 1200.;
  ww = 0.;
  for(ii=1; ii<=3; ii++)
  {
    for(kk=1; kk<=62; kk++)
    {
      idn = 14000+100*ii+kk;
      sprintf(title,"tdcLR%02d%02d",ii,kk);
      hbook2_(&idn,title,&nxbins,&x1,&x2,&nybins,&y1,&y2,&ww,strlen(title));
    }
  }

  /* TDCL minus TDCR for every layer/slab (2D) 23*3 vs 23*3 idn=14400/ 14500/ 14600 */
  nxbins=50;
  x1 = -25.;
  x2 =  25.;
  nybins=62;
  y1=1.;
  y2=63.;
  ww = 0.;
  for(ii=1; ii<=3; ii++)
  {
    nybins=nslabs[ii-1];
    y2=(float)nybins+1;
    idn=14300+100*ii;
    if (ii==1) sprintf(title,"FTOF1B TDC L-R");
    if (ii==2) sprintf(title,"FTOF1A TDC L-R");
    if (ii==3) sprintf(title,"FTOF2  TDC L-R");
    hbook2_(&idn,title,&nxbins,&x1,&x2,&nybins,&y1,&y2,&ww,strlen(title));
  }

  /* ADCL vs ADCR for every layer/slab (2D) 23*3 vs 23*3 idn=5100/5200/5300 */
  nxbins=40;
  nybins=40;
  x1 = 0.;
  x2 = 8000.;
  y1 = 0.;
  y2 = 8000.;
  ww = 0.;
  for(ii=1; ii<=3; ii++)
  {
    for(kk=1; kk<=62; kk++)
    {
      idn = 15000+100*ii+kk;
      sprintf(title,"adcLR%02d%02d",ii,kk);
      hbook2_(&idn,title,&nxbins,&x1,&x2,&nybins,&y1,&y2,&ww,strlen(title));
    }
  }

  /* SQRT(ADCL * ADCR) for every layer/slab (1D) 23*3 idn=6100/6200/6300 */
  nxbins=60;
  x1 = 0.;
  x2 = 6000.;
  nybins=62;
  y1=1.;
  y2=63.;
  ww = 0.;
  for(ii=1; ii<=3; ii++)
  {
    nybins=nslabs[ii-1];
    y2=(float)nybins+1;
    if (ii==1) {sprintf(title,"FTOF1B ADC SQRT(LR)"); x2=10000;}
    if (ii==2) {sprintf(title,"FTOF1A ADC SQRT(LR)"); x2=3000;}
    if (ii==3) sprintf(title,"FTOF2  ADC SQRT(LR)");
    idn=16000+100*ii;
    hbook2_(&idn,title,&nxbins,&x1,&x2,&nybins,&y1,&y2,&ww,strlen(title));
    idn=16000+100*ii+1;
    hbook2_(&idn,title,&nxbins,&x1,&x2,&nybins,&y1,&y2,&ww,strlen(title));
  }


  /* LN(ADCR/ADCL) for every layer/slab (1D) 23*3 idn=7100/7200/7300 */
  nxbins=15;
  x1 = -3.;
  x2 =  3.;
  nybins=62;
  y1=1.;
  y2=63.;
  ww = 0.;
  for(ii=1; ii<=3; ii++)
  {
    nybins=nslabs[ii-1];
    y2=(float)nybins+1;
    idn=17000+100*ii;
    if (ii==1) sprintf(title,"FTOF1B ADC LOG(L/R)");
    if (ii==2) sprintf(title,"FTOF1A ADC LOG(L/R)");
    if (ii==3) sprintf(title,"FTOF2  ADC LOG(L/R)");
    hbook2_(&idn,title,&nxbins,&x1,&x2,&nybins,&y1,&y2,&ww,strlen(title));

  }

iev = 0;
nfile = 0;
 int nread=0,trig1,trig2;

while(1)
{

  if(use_et)
  {
    status = et_events_get(et_sys, et_attach, pe, ET_SLEEP,
                            NULL, ET_EVENT_ARRAY_SIZE, &nevents);
    /* if no events or error ... */
    if ((nevents < 1) || (status != ET_OK))
    {
      /* if status == ET_ERROR_EMPTY or ET_ERROR_BUSY, no reinit is necessary */
      
      /* will wake up with ET_ERROR_WAKEUP only in threaded code */
      if (status == ET_ERROR_WAKEUP)
      {
        printf("status = ET_ERROR_WAKEUP\n");
      }
      else if (status == ET_ERROR_DEAD)
      {
        printf("status = ET_ERROR_DEAD\n");
        et_reinit = 1;
      }
      else if (status == ET_ERROR)
      {
        printf("error in et_events_get, status = ET_ERROR \n");
        et_reinit = 1;
      }
      done = 1;
    }
    else /* if we got events */
    {
      /* by default (no control events) write everything */
      etstart = 0;
      etstop  = nevents - 1;
      
      /* if we got control event(s) */
      if (gotControlEvent(pe, nevents))
      {
        /* scan for prestart and end events */
        for (i=0; i<nevents; i++)
        {
	      if (pe[i]->control[0] == prestartEvent)
          {
	        printf("Got Prestart Event!!\n");
	        /* look for first prestart */
	        if (PrestartCount == 0)
            {
	          /* ignore events before first prestart */
	          etstart = i;
	          if (i != 0)
              {
	            printf("ignoring %d events before prestart\n",i);
	          }
	        }
            PrestartCount++;
	      }
	      else if (pe[i]->control[0] == endEvent)
          {
	        /* ignore events after last end event & quit */
            printf("Got End event\n");
            etstop = i;
	        done = 1;
	      }
        }
      }
	}
    maxevents = iev + etstop; 
    iet = 0;
  }
  else
  {
    sprintf(filename,"%s.%d",argv[1],nfile++);
    printf("opening data file >%s<\n",filename);
    status = evOpen(filename,"r",&handler);
    printf("status=%d\n",status);
    if(status!=0)
    {
      printf("evOpen error %d - exit\n",status);
      break;
    }
    maxevents = maxev;
  }

  timestamp_old = 0;

  while(iev<maxevents)
  {
    iev ++;

    if(!(iev%1000)) printf("\n\n\nEvent %d\n\n",iev);
    //    if(!(iev%1000)) {printf("Updating histogram file");hcdir_("//PAWC"," ",6L,1L);hrput_(0,HBOOKfilename,"T",strlen(HBOOKfilename),1);}


#ifdef DEBUG
    printf("\n\n\nEvent %d\n\n",iev);
#endif

	if(use_et)
	{
      int handle1;
      if(iet >= nevents)
	  {
        printf("ERROR: iev=%d, nevents=%d\n",iet,nevents);
        exit(0);
	  }
      et_event_getlength(pe[iet], &len); /*get event length from et*/
	  /*printf("len1=%d\n",len);*/

      /*copy event from et to the temporary buffer
      memcpy((char *)buf, (char *)pe[iet]->pdata, len);
      bufptr = &buf[8];
	  */
      bufptr = (unsigned int *)pe[iet]->pdata;
      bufptr += 8;

	  /*
	  printf("buf: 0x%08x 0x%08x 0x%08x 0x%08x 0x%08x 0x%08x 0x%08x 0x%08x 0x%08x 0x%08x\n",
			 bufptr[0],bufptr[1],bufptr[2],bufptr[3],bufptr[4],
			 bufptr[5],bufptr[6],bufptr[7],bufptr[8],bufptr[9]);
	  */
goto a123;
      status = evOpenBuffer(pe[iet]->pdata, MAXBUF, "w", &handle1); /*open 'buffer' in et*/
      if(status!=0) printf("evOpenBuffer returns %d\n",status);
      status = evWrite(handle1, buf); /*write event to the 'buffer'*/
      if(status!=0) printf("evWrite returns %d\n",status);
      evGetBufferLength(handle1,&len); /*get 'buffer' length*/
	  /*printf("len2=%d\n",len);*/
      status = evClose(handle1); /*close 'buffer'*/
      if(status!=0) printf("evClose returns %d\n",status);
      et_event_setlength(pe[iet], len); /*update event length in et*/
a123:

      iet ++;
      nread ++;
    }
    else
    {
      status = evRead(handler, buf, MAXBUF);
      if(status < 0)
	  {
	    if(status==EOF)
	    {
          printf("evRead: end of file after %d events - exit\n",iev);
          break;
	    }
	    else
	    {
          printf("evRead error=%d after %d events - exit\n",status,iev);
          break;
	    }
      }
      bufptr = buf;
    }

    /*if(iev < 3) continue;*/ /*skip first 2 events*/

   /* Clear ECAL,PCAL,FTOF counters */

    for(ii=0; ii<6; ii++)
    {
      for(kk=0; kk<36; kk++)
        {
        neadc[ii][kk] = 0;
        netdc[ii][kk] = 0;
	}
    }

    for(ii=0; ii<3; ii++)
    {
      for(kk=0; kk<68; kk++)
        {
        npadc[ii][kk] = 0;
        nptdc[ii][kk] = 0;
         strr[ii][kk] = 0;
	}
      npl[ii] = 0;
    }

    for(ii=0; ii<3; ii++)
    {
      for(jj=0; jj<2; jj++)
      {
        for(kk=0; kk<62; kk++)
        {
        nadc[ii][jj][kk] = 0;
        ntdc[ii][jj][kk] = 0;
	}
      }
    }

    tdcref = 0;

    for(fragment=1; fragment<=36; fragment++)
      {

    /*TDCs*/
    if((ind1 = evNlink(bufptr,  fragment, 0xe107,  0, &nbytes)) > 0)
      {
      int half,chip,chan,bco,val,chan1,edge,nw,tdcl,tdcr;
      unsigned char *end, *start;
      unsigned int tmp;
      float tmpx0, tmpx2, dcrbref;
      unsigned int temp[6];
      unsigned sample[6];
      int slot;
      int ndata0[22], data0[21][8];
      int baseline, sum, channel, ch1;
#ifdef DEBUG
      printf("ind1=%d, nbytes=%d\n",ind1,nbytes);fflush(stdout);
#endif
      start = b08 = (unsigned char *) &bufptr[ind1];
      end = b08 + nbytes;
#ifdef DEBUG
      printf("ind1=%d, nbytes=%d (from 0x%08x to 0x%08x)\n",ind1,nbytes,b08,end);fflush(stdout);
#endif
      sector   = (fragment-1)/6 + 1;
      detector = (fragment/2)%6;
      //printf("TDC: fragment=%d, sector=%d, detector=%d\n",fragment,sector,detector);

      tdcl = tdcr = 0;

      while(b08<end)
	{
        GET32(word);
        slot = (word>>27)&0x1F;
        edge = (word>>26)&0x1;
	chan = (word>>19)&0x7F;
        val = (word&0x3FFFF)*TDCLSB;

        tmpx = ((float)val)/1000.;
        tmpy = (float)chan;
        ww = 1.;
        idn = fragment*100000 + 1000*slot;
        /*printf("idn0=%d\n",idn);*/
        hf2_(&idn,&tmpx,&tmpy,&ww);
	/*if(slot==16) printf("-- %d(%d) %d %d(%f)\n",slot,tdcslot2hist[slot],chan,val,tmpx);*/
        if(slot==16 && chan==63)
	  {
          if(tdcref==0)
	    {
            tdcref=val;
            /*printf("tdcref=%d\n",tdcref);*/
            tmpx = ((float)tdcref)/1000.;
            ww = 1.;
            idn = 12;
            hf1_(&idn,&tmpx,&ww);
	    }
          else
	    {
            /*printf("ERROR: double tdcref = %d, already defined as %d\n",val,tdcref)*/;
	    }
	  }
        if(detector==1 || detector==4)
	  {
          ii = tdclayerecal[slot][chan]-1;
	  kk = tdcstripecal[slot][chan]-1;
          etdc[ii][kk][netdc[ii][kk]] = val;
	  netdc[ii][kk]++;
	  }
	  if(detector==2 || detector==5)
	  {
          ii = tdclayerpcal[slot][chan]-1;
	  kk = tdcstrippcal[slot][chan]-1;
          ptdc[ii][kk][nptdc[ii][kk]] = val;
	  nptdc[ii][kk]++;
	  }
	  if(detector==3 || detector==0)
	  {
          ii = tdclayerftof[slot][chan] - 1;
          jj =    tdclrftof[slot][chan] - 1;
          kk =  tdcslabftof[slot][chan] - 1;
          if(ii>=0)
	  {
	  /*
	  printf("1=> %d %d %d\n",ii,jj,kk);fflush(stdout);
	  printf("1==> %d\n",ntdc[ii][jj][kk]);fflush(stdout);
	  */
          tdc[ii][jj][kk][ntdc[ii][jj][kk]] = val;
          ntdc[ii][jj][kk]++;
	  }

#ifdef DEBUG
	 
        printf("TDC[0x%08x]:  slot=%2d  chan=%3d  edge=%1d  tdc=%5d(%f)  (hist_id=%d)\n",
			   word,slot,chan,edge,val,(((float)val)/1000.),idn);
#endif
          idn = 11;
	  hf1_(&idn,&tmpx,&ww);
	  }
	}
      }

    int edet;

    /* ADC raw mode bank */
    if((ind1 = evNlink(bufptr, fragment, 0xe101, 0, &nbytes)) > 0)
      {
      unsigned char *end;
      unsigned long long time;
      int crate,slot,trig,nchan,chan,nsamples,notvalid,edge,val,data,count,ncol1,nrow1;
      int oldslot = 100;
      int thr=15;
      int goplt;
      int ndata0[22], data0[21][8];
      int baseline, pedestal[16], pedplt[16], sum, channel, summing_in_progress, mmsum;
      int datasaved[100][16];

#ifdef DEBUG
      printf("ind1=%d, nbytes=%d\n",ind1,nbytes);
#endif
      b08 = (unsigned char *) &bufptr[ind1];
      end = b08 + nbytes;
#ifdef DEBUG
      printf("ind1=%d, nbytes=%d (from 0x%08x to 0x%08x)\n",ind1,nbytes,b08,end);
#endif

      sector = (fragment-1)/6 + 1;
      detector = (fragment/2)%6 + 1;

      edet = -1;
      if(detector==1 || detector==4) edet=0;
      if(detector==2 || detector==5) edet=1;
      if(detector==3 || detector==6) edet=2;
      //printf("ADC: fragment=%d, sector=%d, detector=%d\n",fragment,sector,detector);

      while(b08<end)
        {
#ifdef DEBUG
        printf("begin while: b08=0x%08x\n",b08);
#endif
        GET8(slot);
        GET32(trig);
        GET64(time);
        GET32(nchan);
        if (nread==5)    {trig1=trig;}
        if (nread==1005) {trig2=trig; pparms[9]=1e3*1000/(trig2-trig1) ; nread=0;}

#ifdef DEBUG
        printf("slot=%d, trig=%d, time=%lld nchan=%d\n",slot,trig,time,nchan);
#endif
        goplt = 0;
        for(nn=0; nn<nchan; nn++)
	  {
          GET8(chan);
          /*chan++;*/
          GET32(nsamples);
#ifdef DEBUG
          printf("  chan=%d, nsamples=%d\n",chan,nsamples);
#endif
          baseline = sum = summing_in_progress = mmsum = 0;
          for(mm=0; mm<nsamples; mm++)
	    {
	    GET16(data);
            datasaved[mm][nn] = data;
	    /*printf("mm=%d data=%d\n",mm,data);*/
            if(mm<10) baseline += data;
            if(mm==10)
	      {
              baseline = baseline / 10;
#ifdef DEBUG
              printf("slot=%d chan=%d baseline=%d\n",slot,chan,baseline);
#endif
	      }
            if(mm>15 && mm<100)
              {
              if(summing_in_progress==0 && data>(baseline+10))
		{
                /*printf("open summing at mm=%d\n",mm);*/
                summing_in_progress = 1;
                for (ii=1;ii++;ii<nsa[edet]+1) 
		  {
		  sum += (datasaved[mm-ii][nn]-baseline);
		  }
		mmsum=nsa[edet];
		}
	      if(summing_in_progress>0 && mmsum==(nsa[edet]+nsb[edet]))
		{
		summing_in_progress = -1;
		}
              if(summing_in_progress>0 && data<baseline)
	        {
                /*printf("close summing at mm=%d, sum=%d\n",mm,sum);*/
                //summing_in_progress = -1;
		}

              if(summing_in_progress>0)
	        {
                sum += (datasaved[mm][nn]-baseline);
                mmsum++;
                //printf("sum=%d (mm=%d)\n",sum,mm);
		}
	      }
	    }

	  /* fill raw adc pulse hist only if there was a pulse */
            if(sum>0)
	      {
              for(mm=0; mm<nsamples; mm++)
	        {
                  tmpx = (float)mm+0.5;
                  tmpy = (float)chan;
                  ww = (float)datasaved[mm][nn]-baseline;
                  idn = 10000000 + fragment*100000 + 1000*slot;
                  hf2_(&idn,&tmpx,&tmpy,&ww);   
		}
	      }

	      ww = 1.;
            /* adc pedestal */
              tmpx = (float)baseline;
              tmpy = (float)chan;
              idn = 20000000 + fragment*100000 + 1000*slot;
              hf2_(&idn,&tmpx,&tmpy,&ww);

            /* adc spectra */
              tmpx = (float)sum;
              tmpy = (float)chan;
              idn = 30000000 + fragment*100000 + 1000*slot;
              if(sum>0) hf2_(&idn,&tmpx,&tmpy,&ww);

	      if(edet==0)
		{
		ii = adclayerecal[slot][chan]-1;
		kk = adcstripecal[slot][chan]-1;
		if(ii>=0 && sum>0)
		  {
		  eadc[ii][kk][neadc[ii][kk]] = sum;
		  neadc[ii][kk]++;
		  }
		}
	      if(edet==1)
		{
		ii = adclayerpcal[slot][chan]-1;
		kk = adcstrippcal[slot][chan]-1;
		if(ii>=0 && sum>0)
		  {
		  padc[ii][kk][npadc[ii][kk]] = sum;
		  npadc[ii][kk]++;
                  if (sum>100) {strr[ii][npl[ii]]=kk+1;adcr[ii][npl[ii]]=sum;npl[ii]++;}
		  }
		}
              if(edet==2) 
		{
                ii = adclayerftof[slot][chan] - 1;
                jj =    adclrftof[slot][chan] - 1;
                kk =  adcslabftof[slot][chan] - 1;
	        if(ii>=0 && sum>0)
		  {
                  adc[ii][jj][kk][nadc[ii][jj][kk]] = sum;
                  nadc[ii][jj][kk] ++;
		  }
		}    	  

	    /* Plot FADC samples for each channel */

	    if (sum>=thrsh) {goplt=1;}
	    if(slot==pslt&&edet==pdet&&iev<10) {pedplt[nn]=baseline;}
	  }

        pparms[8]=trig;
       	if(doplt&&goplt==1&&edet==pdet&&slot==pslt){plot1d(datasaved,pedplt,100);} 
#ifdef DEBUG
        printf("end loop: b08=0x%08x\n",b08);
#endif
        }   
     }
	
    /* Adc pulsed mode bank */
   
    if((ind1 = evNlink(bufptr, fragment, 0xe103, 0, &nbytes)) > 0)
    {
      unsigned short pulse_time;
      unsigned int pulse_integral;
      unsigned char *end;
      unsigned long long time;
      int crate,slot,trig,nchan,chan,npulses,notvalid,edge,data,count,ncol1,nrow1;
      int oldslot = 100;
      int ndata0[22], data0[21][8];
      int baseline, sum, channel;

      b08 = (unsigned char *) &bufptr[ind1];
      b16 = (unsigned short *) &bufptr[ind1];
      b32 = (unsigned int *) &bufptr[ind1];

      end = b08 + nbytes;
#ifdef DEBUG
      printf("ind1=%d, nbytes=%d (from 0x%08x to 0x%08x)\n",ind1,nbytes,b32,end);
#endif
      while(b08<end)
      {
#ifdef DEBUG
        printf("begin while: b08=0x%08x\n",b08);
#endif
        b08 = (unsigned char *)b32;
        slot = *b08 ++;

        b32 = (unsigned int *)b08;
        trig = *b32++;

        b64 = (unsigned long long *)b32;
        time = *b64++;

        b32 = (unsigned int *)b64;
        nchan = *b32++;

	//#ifdef DEBUG
        //printf("slot=%d, trig=%d, time=%lld nchan=%d\n",slot,trig,time,nchan);
	//#endif

        sector   = (fragment-1)/6 + 1;
        detector = (fragment/2)%6 + 1;

        edet = -1;
        if(detector==1 || detector==4) edet=0;
        if(detector==2 || detector==5) edet=1;
        if(detector==3 || detector==6) edet=2;

        for(nn=0; nn<nchan; nn++)
	    {
            b08 = (unsigned char *)b32;
            chan = (*b08 ++) /*+ 1*/;
            b32 = (unsigned int *)b08;
            npulses = *b32++;

	    //#ifdef DEBUG
	    //printf("detector=%d\n",detector);
	    //printf("  chan=%d, npulses=%d\n",chan,npulses);
	  //#endif

          for(mm=0; mm<npulses; mm++)
	      {
              b16 = (unsigned short *)b32;
              pulse_time = (*b16++)>>6;
              b32 = (unsigned int *)b16;
              pulse_integral = *b32++;

	      //#ifdef DEBUG
	      //printf(" b32=0x%08x:  pulse_time=%d pulse_integral=%d\n",b32,pulse_time,pulse_integral);
	    //#endif		

	        if(edet==0)
		  {
		  sum  = (float)pulse_integral-tabecal[slot][chan]*(nsa[edet]+nsb[edet]);
                  //printf("slot,chan,adc,ped=%d %d %d %f\n",slot,chan,sum,tabecal[slot][chan]);
		  ii = adclayerecal[slot][chan]-1;
		  kk = adcstripecal[slot][chan]-1;
		  if(ii>=0 && sum>0)
		    {
		    eadc[ii][kk][neadc[ii][kk]] = sum;
		    neadc[ii][kk]++;
		    }	
		  }
	        if(edet==1)
		  {
		  sum  = (float)pulse_integral-tabpcal[slot][chan]*(nsa[edet]+nsb[edet]);
		  ii = adclayerpcal[slot][chan]-1;
		  kk = adcstrippcal[slot][chan]-1;
		  if(ii>=0 && sum>0)
		    {
		    padc[ii][kk][npadc[ii][kk]] = sum;
		    npadc[ii][kk]++;
                    if (sum>100) {strr[ii][npl[ii]]=kk+1;adcr[ii][npl[ii]]=sum;npl[ii]++;}
		    }
		  }
                if(edet==2) 
		  {
		  sum  = (float)pulse_integral-tabftof[slot][chan]*(nsa[edet]+nsb[edet]);
                  ii = adclayerftof[slot][chan] - 1;
                  jj =    adclrftof[slot][chan] - 1;
                  kk =  adcslabftof[slot][chan] - 1;
	          if(ii>=0 && sum>0)
		    {
                    adc[ii][jj][kk][nadc[ii][jj][kk]] = sum;
                    nadc[ii][jj][kk] ++;
		    }
		  }

                tmpx = (float)sum;
                tmpy = (float)chan;                
                idn = 30000000 + fragment*100000 + 1000*slot;
                if(sum>0) hf2_(&idn,&tmpx,&tmpy,&ww);	 

	      /*printf("slot %d chan %d -> idn=%d tmpx=%f\n",slot,chan,idn,tmpx);*/

	      }
	    }

        b08 = (unsigned char *)b32;
#ifdef DEBUG
        printf("end loop: b08=0x%08x\n",b08);
#endif
      }
    }

    } /*fragment*/

    /* PCAL PIXEL CUT */

    int il,good_lay[3],ad[3],rs[3],good_12[3],good_a12[3],good_uw,good_vu,good_wv,good_uvw;

    for (il=0; il<3 ; il++)
      {
	good_lay[il]=npl[il]==1;
        if (good_lay[il]){rs[il]=strr[il][0];ad[il]=adcr[il][0];}
      }

    good_uw=good_lay[0] && good_lay[2];
    good_vu=good_lay[1] && good_lay[0];
    good_wv=good_lay[2] && good_lay[1];

    good_uvw = good_lay[0] && good_lay[1] && good_lay[2];
    if (good_uvw) {printf("Pixel event: u,v,w= %d %d %d adc= %d %d %d \n",rs[0],rs[1],rs[2],ad[0],ad[1],ad[2]);}

    good_12[0] = good_uw && rs[2]==61;
    good_12[1] = good_vu && rs[0]==67;
    good_12[2] = good_wv && rs[1]==67;
    
    good_a12[0] = good_12[0] && ad[2]>70;
    good_a12[1] = good_12[1] && ad[0]>70;
    good_a12[2] = good_12[2] && ad[1]>70;
    
    /* PCAL HISTOS */

    for (ii=0; ii<3 ; ii++)
      {
      tmpx = (float)ad[ii];
      tmpy = (float)rs[ii];
      ww=1;
      if (good_uvw)
        {
        idn = hid+20*tid+iid+100*(ii+1);
        hf2_(&idn,&tmpx,&tmpy,&ww);      
        idn = hid+21*tid+iid+100*(ii+1);
        if (good_12[ii]) {hf2_(&idn,&tmpx,&tmpy,&ww);}
        idn = hid+22*tid+iid+100*(ii+1);
        if (good_a12[ii]) {hf2_(&idn,&tmpx,&tmpy,&ww);}
	}
      }

    for(ii=0; ii<3; ii++)
      {
	for(kk=0;kk<68;kk++)
	  {
	    for(nn=0;nn<npadc[ii][kk];nn++)
	      {
		ww = 1;
		tmpx = ((float)padc[ii][kk][nn]);
		tmpy = (float)kk+1+0.5;
		idn  = 21800+200*(ii+1);
                hf2_(&idn,&tmpx,&tmpy,&ww);
	      }
	  }
      }

    for(ii=0; ii<3; ii++)
      {
	for(kk=0;kk<68;kk++)
	  {
	    for(nn=0;nn<nptdc[ii][kk];nn++)
	      {
		ww = 1;
		tmpx = ((float)ptdc[ii][kk][nn])/1000.;
		tmpy = (float)kk+1+0.5;
		idn  = 25800+200*(ii+1);
                hf2_(&idn,&tmpx,&tmpy,&ww);
	      }
	  }
      }

    /* ECAL HISTOS */

    for(ii=0; ii<6; ii++)
      {
	for(kk=0;kk<36 ;kk++)
	  {
	    for(nn=0;nn<neadc[ii][kk];nn++)
	      {
		ww = 1;
		tmpx = ((float)eadc[ii][kk][nn]);
		tmpy = (float)kk+1+0.5;
		idn  = 31800+200*(ii+1);
                hf2_(&idn,&tmpx,&tmpy,&ww);
	      }
	  }
      }

    for(ii=0; ii<6; ii++)
      {
	for(kk=0;kk<36 ;kk++)
	  {
	    for(nn=0;nn<netdc[ii][kk];nn++)
	      {
		ww = 1;
		tmpx = ((float)etdc[ii][kk][nn])/1000.;
		tmpy = (float)kk+1+0.5;
		idn  = 35800+200*(ii+1);
                hf2_(&idn,&tmpx,&tmpy,&ww);
	      }
	  }
      }



    /* FTOF HISTOS */

    /* correct TDCs using reference signal */

/*
    if(tdcref==0)
      {
      printf("ERROR: iev=%d -> there is no ref signal\n",iev);
      }
    else
      {
      for(ii=0; ii<6; ii++)
	{
        for(jj=0; jj<2; jj++)
	  {
          for(kk=0; kk<16; kk++)
	    {
            if(ntdc[ii][jj][kk]>1)
	      {
              ntdc[ii][jj][kk]=1;
	      }
            for(nn=0; nn<ntdc[ii][jj][kk]; nn++)
	      {
              tdc[ii][jj][kk][nn] = tdc[ii][jj][kk][nn] + 250000 - tdcref;
	      }
	    }
	  }
	}
      }
 */

    for(ii=0; ii<3; ii++)
      {
      for(kk=0; kk<62; kk++)
	{

        for(jj=0; jj<2; jj++)
	  {
          /* TDC L and R for every layer/slab (1D) 23*6 idn=2100/2200/2300/2400/2500/2600 */
          for(nn=0; nn<ntdc[ii][jj][kk]; nn++)
	    {
            ww = 1.;
            tmpx = ((float)tdc[ii][jj][kk][nn])/1000.;
            tmpy = (float)kk+1+0.5;
            idn = 11800+200*(ii+1)+100*(jj+1);
            hf2_(&idn,&tmpx,&tmpy,&ww);
	    }
          /* ADC L and R for every layer/slab (1D) 23*6 idn=3100/3200/3300/3400/3500/3600 */
          for(nn=0; nn<nadc[ii][jj][kk]; nn++)
	    {
            ww = 1.;
            tmpx = (float)adc[ii][jj][kk][nn];
            tmpy = (float)kk+1+0.5;
            idn = 12800+200*(ii+1)+100*(jj+1);
            hf2_(&idn,&tmpx,&tmpy,&ww);
	    }
	  }

        /* TDCL vs TDCR for every layer/slab (2D) 23*3 vs 23*3 idn=4100/4200/4300 */
        for(nn=0; nn<ntdc[ii][0][kk]; nn++)
	  {
          for(mm=0; mm<ntdc[ii][1][kk]; mm++)
	    {
            ww = 1.;
            tmpx = ((float)tdc[ii][0][kk][nn])/1000.;
	    tmpy = ((float)tdc[ii][1][kk][mm])/1000.;
            idn = 14000+100*(ii+1)+kk+1;
			/*printf("idn=%d tmpx=%f tmpy=%f\n",idn,tmpx,tmpy);*/
            hf2_(&idn,&tmpx,&tmpy,&ww);
            tmpx = ((float)tdc[ii][0][kk][nn]-(float)tdc[ii][1][kk][nn])/1000.;
            tmpy = (float)kk+1+0.5;
            idn = 14300+100*(ii+1);
            hf2_(&idn,&tmpx,&tmpy,&ww);
	    }
	  }
        /* ADCL vs ADCR for every layer/slab (2D) 23*3 vs 23*3 idn=5100/5200/5300 */
        for(nn=0; nn<nadc[ii][0][kk]; nn++)
	  {
          for(mm=0; mm<nadc[ii][1][kk]; mm++)
	    {
            ww = 1.;
            tmpx = (float)adc[ii][0][kk][nn];
            tmpy = (float)adc[ii][1][kk][mm];
            idn = 15000+100*(ii+1)+kk+1;
			/*printf("idn=%d tmpx=%f tmpy=%f\n",idn,tmpx,tmpy);*/
            hf2_(&idn,&tmpx,&tmpy,&ww);
	    }
	  }
        /* SQRT(ADCL * ADCR) for every layer/slab (1D) 23*3 idn=6100/6200/6300 */
        for(nn=0; nn<nadc[ii][0][kk]; nn++)
	  {
          for(mm=0; mm<nadc[ii][1][kk]; mm++)
	    {  
            ww = 1.;
            tmpx = sqrt ( ((float)adc[ii][0][kk][nn]) * ((float)adc[ii][1][kk][mm]) );
            tmpy = (float)kk+1+0.5;
            idn = 16000+100*(ii+1);
	    if(adc[ii][0][kk][nn]>100&&adc[ii][1][kk][mm]>100)
	      {
			  /*
			  if(idn==6222)
			    printf("GOD: idn=%d adcl=%d adcr=%d sqrt=%f\n",idn,adc[ii][0][kk][nn],adc[ii][1][kk][mm],tmpx);
			  if(idn==6223)
			    printf("   BAD: idn=%d adcl=%d adcr=%d sqrt=%f\n",idn,adc[ii][0][kk][nn],adc[ii][1][kk][mm],tmpx);
			  */
		{hf2_(&idn,&tmpx,&tmpy,&ww);}
                idn=idn+1;
		if (good_uvw) {hf2_(&idn,&tmpx,&tmpy,&ww);}
	      }
	    }
	  }
        /* LN(ADCL/ADCR) for every layer/slab (1D) 23*3 idn=7100/7200/7300 */
        for(nn=0; nn<nadc[ii][0][kk]; nn++)
	  {
          for(mm=0; mm<nadc[ii][1][kk]; mm++)
	    {
            ww = 1.;
	    if(adc[ii][0][kk][nn]>0 && adc[ii][1][kk][mm]>0)
	      {
              tmpx = logf ( ((float)adc[ii][1][kk][nn]) / ((float)adc[ii][0][kk][mm]) ); /* R/L !!! */
              tmpy = (float)kk+1+0.5;
              idn = 17000+100*(ii+1);
			  /*printf("idn=%d adcl=%d adcr=%d ln=%f\n",idn,adc[ii][0][kk][nn],adc[ii][1][kk][mm],tmpx);*/
              hf2_(&idn,&tmpx,&tmpy,&ww);
	      }
	    }
	  }
	}
      }
  }



  if(use_et)
    {
    /* put et events back into system */
    status = et_events_put(et_sys, et_attach, pe, nevents);            
    if (status != ET_OK)
      {
      printf("error in et_events_put, status = %i \n",status);
      et_reinit = 1;
      done = 1;
      }	
    }
  else
    {
    printf("evClose after %d events\n",iev);fflush(stdout);
    evClose(handler);
    }

  if(iev>=maxev) break;

} /*while*/


  /* closing HBOOK file */
  idn = 0;
  printf("befor hrout_\n");fflush(stdout);
  hrout_(&idn,&icycle," ",1);
  printf("after hrout_\n");fflush(stdout);
  hrend_("FTOF", 4);
  printf("after hrend_\n");fflush(stdout);

  exit(0);
}

void plot1d(int datasaved[][16], int *baseline, int nsamples)
{
  int higz1,ww,mm,nn,hsed,hdum;
  int id,i1,i2,i3,i4,istart=1;

  static int ifirst=1;
  static int nnn=0;
  float vsed[100],a1,a2;

  int runnum = pparms[0];
  int sec    = pparms[1];
  int iff    = sec*6-5;
  int pdet   = pparms[3];
  int pslt   = pparms[4];
  int thrsh  = pparms[5];
  int nskp   = pparms[6];
  int evnt   = pparms[8];
  int lt     = pparms[9];

  int fslot[3][14]={3,4,5,6,7,8,9,10,13,14,15,16,17,18,
		    3,4,5,6,7,8,9,10,13,14,15,16,0,0,
		    3,4,5,6,7,8,9,10,13,14,15,16,0,0};
  int nslot[3]={14,12,12};
  int zon[2][16]={1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,
  		  1,5,9,13,2,6,10,14,3,7,11,15,4,8,12,16};

  nnn++;
  hdum=800;   
  int iz=1;
  int idn=1e7+(iff+2*pdet)*1e5;
  
  if (iz==0) {i1=8;i2=2;}
  if (iz==1) {i1=4;i2=4;}

  if (ifirst==0 && nnn==nskp) 
    {
      higz1=-1;hplset_("HCOL",&higz1,4L); higz1=0;
      for(nn=0;nn<16;nn++){hdum=800+nn;i3=nn+1;hplzon_(&i1,&i2,&i3,"S",1L); hplot_(&hdum,"H"," ",&higz1,2L,1L);hdelet_(&hdum);}
      //a1=0.3 ; igset_("CHHE",&a1,4L);
      //higz1=1 ; iselnt_(&higz1); a1=2.; a2=5.; i4=2 ; istxci_(&i4) ; itx_(&a1,&a2,titold,strlen(titold)); i4=1 ; istxci_(&i4);
      igterm_();
    }
                                       
  higz1=1;hplset_("HCOL",&higz1,4L);
  for(nn=0; nn<16; nn++){for(mm=0; mm<nsamples; mm++){vsed[mm]=(float)datasaved[mm][nn]-baseline[nn];} hsed=700+nn ; hpak_(&hsed,&vsed);}

  if (ifirst==1) 
    {
      higz1=0;hplzon_(&i1,&i2,&istart," ",1L); a1=100 ; hplset_("YVAL",&a1,4L); 
      if (iz==0)
	{
      for(hsed=700;hsed<701;hsed++){higz1=0;hdum=hsed+100;hplot_(&hsed,"H"," ",&higz1,1L,1L);hcopy_(&hsed,&hdum," ",1L); a1=100;hplset_("XVAL",&a1,4L);}
      for(hsed=701;hsed<708;hsed++){higz1=0;hdum=hsed+100;hplot_(&hsed,"H"," ",&higz1,1L,1L);hcopy_(&hsed,&hdum," ",1L);}a1=0  ;hplset_("XVAL",&a1,4L);
      for(hsed=708;hsed<709;hsed++){higz1=0;hdum=hsed+100;hplot_(&hsed,"H"," ",&higz1,1L,1L);hcopy_(&hsed,&hdum," ",1L); a1=100;hplset_("XVAL",&a1,4L);}
      for(hsed=709;hsed<716;hsed++){higz1=0;hdum=hsed+100;hplot_(&hsed,"H"," ",&higz1,1L,1L);hcopy_(&hsed,&hdum," ",1L);}
	}
      if (iz==1)
	{
      for(nn=0;nn<1;nn++)  {hsed=700+nn;hdum=hsed+100;hplot_(&hsed,"H"," ",&higz1,1L,1L);hcopy_(&hsed,&hdum," ",1L); a1=100;hplset_("XVAL",&a1,4L);}
      for(nn=1;nn<4;nn++)  {hsed=700+nn;hdum=hsed+100;hplot_(&hsed,"H"," ",&higz1,1L,1L);hcopy_(&hsed,&hdum," ",1L);}a1=0  ;hplset_("XVAL",&a1,4L);
      for(nn=4;nn<5;nn++)  {hsed=700+nn;hdum=hsed+100;hplot_(&hsed,"H"," ",&higz1,1L,1L);hcopy_(&hsed,&hdum," ",1L); a1=100;hplset_("XVAL",&a1,4L);}
      for(nn=5;nn<8;nn++)  {hsed=700+nn;hdum=hsed+100;hplot_(&hsed,"H"," ",&higz1,1L,1L);hcopy_(&hsed,&hdum," ",1L);}a1=0  ;hplset_("XVAL",&a1,4L);
      for(nn=8;nn<9;nn++)  {hsed=700+nn;hdum=hsed+100;hplot_(&hsed,"H"," ",&higz1,1L,1L);hcopy_(&hsed,&hdum," ",1L); a1=100;hplset_("XVAL",&a1,4L);}
      for(nn=9;nn<12;nn++) {hsed=700+nn;hdum=hsed+100;hplot_(&hsed,"H"," ",&higz1,1L,1L);hcopy_(&hsed,&hdum," ",1L);}a1=0  ;hplset_("XVAL",&a1,4L);
      for(nn=12;nn<13;nn++){hsed=700+nn;hdum=hsed+100;hplot_(&hsed,"H"," ",&higz1,1L,1L);hcopy_(&hsed,&hdum," ",1L); a1=100;hplset_("XVAL",&a1,4L);}
      for(nn=13;nn<15;nn++){hsed=700+nn;hdum=hsed+100;hplot_(&hsed,"H"," ",&higz1,1L,1L);hcopy_(&hsed,&hdum," ",1L);}
	}
      //      for(id=0;id<nslot[pdet];id++){higz1=0;hsed=idn+fslot[pdet][id]*1e3; hplot_(&hsed,"COL"," ",&higz1,3L,1L);}
      //for(id=0;id<3;id++){higz1=0;hsed=idn+fslot[pdet][id]*1e3; hplot_(&hsed,"COL"," ",&higz1,3L,1L);}
      //utilGenColormap(1) 
      igterm_();
      ifirst=0;
    }

  if (nnn==nskp) 
    {
      higz1=0;
      for(nn=0;nn<16;nn++){hsed=700+nn;hdum=hsed+100;i3=nn+1;hplzon_(&i1,&i2,&i3,"S",1L); hplot_(&hsed,"H"," ",&higz1,1L,1L);hcopy_(&hsed,&hdum," ",1L);}
      //for(id=0;id<nslot[pdet];id++){higz1=0;hsed=idn+fslot[pdet][id]*1e3; hplot_(&hsed,"COL"," ",&higz1,3L,1L);}
      //for(id=0;id<3;id++){higz1=0;hsed=idn+fslot[pdet][id]*1e3; hplot_(&hsed,"COL"," ",&higz1,3L,1L);}
      //utilGenColormap(1) 
      //strcpy(titold,title);
      higz1=1 ; iselnt_(&higz1);
      a1=0.3; igset_("CHHE",&a1,4L); a1=2. ; a2=6.5 ;
      sprintf(titnew," EVENT %d",evnt); sprintf(dtnew,"LIVET %.1f",lt/10.);
      i4=0 ; istxci_(&i4) ; a1=23. ; a2=16.5 ; itx_(&a1,&a2,titold,strlen(titold)); a1=23. ; a2=17.0 ; itx_(&a1,&a2,dtold,strlen(dtold));
      i4=1 ; istxci_(&i4) ; a1=23. ; a2=16.5 ; itx_(&a1,&a2,titnew,strlen(titnew)); a1=23. ; a2=17.0 ; itx_(&a1,&a2,dtnew,strlen(dtnew));
      sprintf(titold," EVENT %d",evnt); sprintf(dtold,"LIVET %.1f",lt/10.);
      nnn=0;
      igterm_();
    }
}

void utilSetupGraf1(void)
{
  int i1,i2;
  float a1;
  int kwkid=1;

  //  i1=1;i2=1;hplzon_(&i1,&i2,&i1," ",1L);
  // hplnul_();
  //iclrwk_(&kwkid);

  a1=0.1;  hplset_("XWIN",&a1,4L);
  a1=0.4;  hplset_("YWIN",&a1,4L);
  a1=0.2;  hplset_("VSIZ",&a1,4L);
  a1=0.2;  hplset_("TSIZ",&a1,4L);
  a1=0.15; hplset_("YHTI",&a1,4L);
  i1=4  ;  hplset_("NDVX",&i1,4L);
  a1=0.5;  hplset_("YGTI",&a1,4L);
  i1=1  ;  hplopt_("LOGZ",&i1,4L);
  //i1=16  ; hplset_("NDVY",&i1,4L);
}


void utilSetupGraphics(void)
{
  float a1,a2,a3,a4;
  int i1,i2,i3,i4;
  int kwkid=1;

  
  i1=4;i2=4;hplzon_(&i1,&i2,&i1," ",1L);
  hplnul_();
  iclrwk_(&kwkid);

  i1=1;isfais_(&i1);
  i1=-60;hplset_("LFON",&i1,4L);
  i1=-60;hplset_("VFON",&i1,4L);
  i1=-60;hplset_("GFON",&i1,4L);
  i1=-60;hplset_("TFON",&i1,4L);
  a1=0.4;hplset_("GSIZ",&a1,4L);
  a1=0.8;hplset_("YGTI",&a1,4L);
  i1=-60;igset_("TXFP",&i1,4L);
  i1=1  ;igset_("FAIS",&i1,4L);
  i1=1  ;igset_("TXCI",&i1,4L);
  a1=0.3;igset_("LASI",&a1,4L);
  a1=1.3;igset_("CHHE",&a1,4L);
  i1=1  ;isfais_(&i1);

}

void utilGenColormap(int map)
{
  int i,ici,kwkid=1;
  float cr,cg,cb;
  float color[49][4]={
     8, 0.50, 0.50, 0.50, 
     9, 0.60, 0.60, 0.60, 
    10, 0.00, 0.00, 0.30, 
    11, 0.00, 0.00, 0.33, 
    12, 0.00, 0.00, 0.36, 
    13, 0.00, 0.00, 0.39, 
    14, 0.00, 0.00, 0.42, 
    15, 0.00, 0.00, 0.45, 
    16, 0.00, 0.00, 0.48, 
    17, 0.00, 0.00, 0.52, 
    18, 0.00, 0.00, 0.56, 
    19, 0.00, 0.00, 0.60, 
    20, 0.00, 0.00, 0.64, 
    21, 0.00, 0.08, 0.68, 
    22, 0.00, 0.15, 0.68, 
    23, 0.00, 0.23, 0.70, 
    24, 0.00, 0.31, 0.70, 
    25, 0.00, 0.38, 0.70, 
    26, 0.00, 0.46, 0.70, 
    27, 0.00, 0.53, 0.64, 
    28, 0.00, 0.59, 0.56, 
    29, 0.00, 0.66, 0.48, 
    30, 0.00, 0.73, 0.40, 
    31, 0.00, 0.80, 0.33, 
    32, 0.00, 0.87, 0.00, 
    33, 0.17, 0.72, 0.00, 
    34, 0.33, 0.58, 0.00, 
    35, 0.50, 0.43, 0.00, 
    36, 0.67, 0.29, 0.00, 
    37, 0.83, 0.14, 0.00, 
    38, 1.00, 0.00, 0.00, 
    39, 1.00, 0.08, 0.00, 
    40, 1.00, 0.17, 0.00, 
    41, 1.00, 0.25, 0.00, 
    42, 1.00, 0.33, 0.00, 
    43, 1.00, 0.42, 0.00, 
    44, 1.00, 0.50, 0.00, 
    45, 1.00, 0.58, 0.00, 
    46, 1.00, 0.67, 0.00, 
    47, 1.00, 0.75, 0.00, 
    48, 1.00, 0.83, 0.00, 
    49, 1.00, 0.92, 0.00, 
    50, 1.00, 1.00, 0.00, 
    51, 1.00, 1.00, 0.17, 
    52, 1.00, 1.00, 0.33, 
    53, 1.00, 1.00, 0.50, 
    54, 1.00, 1.00, 0.67, 
    55, 1.00, 1.00, 0.83, 
    56, 1.00, 1.00, 1.00};


  for (i=0;i<49;i++)
    {
      ici=i+8;
      if (map==1) 
	{
	  cr=color[i][1];
	  cg=color[i][2];
	  cb=color[i][3];
	}
      if (map==2)
	{
	  cr=(float)i/48.;
	  cg=0.;
	  cb=0.;
	}
      if (map==3)
	{
	  cr=(float)i/48.;
	  cg=(float)i/48.;
	  cb=0.;
	}
      if (map==4)
	{
	  cr=0.;
	  cb=0.;
	  cg=(float)i/48.;
	}
      iscr_(&kwkid,&ici,&cr,&cg,&cb);
    }
}
