/* et2rzn.c */
/* Cole Smith */
/* Hacked from original code evio_sectorhist.c by Sergey Boiarinov (JLAB) */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <malloc.h>
#include <math.h>
 
#include "evio.h"
#include "et.h"
#include "etserg.h"
#include "evnlink.h"
#include "ttfc.h"
#include "tabread.h"
 
#undef DEBUG_SEARCH

#undef DEBUG
 
#define NWPAWC 20000000 /* Length of the PAWC common block. */
#define LREC 8191
#define SIZ 8176

struct {
  float hmemor[NWPAWC];
} pawc_;

int quest_[100];

#define max1A 23
#define max1B 64
#define maxPC 192
#define maxEC 216
#define maxEC2 50
#define maxPC2 50
#define maxSC  20

int    nSC1A;
int  secSC1A[max1A];
int   idSC1A[max1A];
int   tSC1AL[max1A];
int   tSC1AR[max1A];
int   aSC1AL[max1A];
int   aSC1AR[max1A];

int   nSC1B;
int secSC1B[max1B];
int  idSC1B[max1B];
int  tSC1BL[max1B];
int  tSC1BR[max1B];
int  aSC1BL[max1B];
int  aSC1BR[max1B];

int      nPC;
int    secPC[maxPC];
int  layerPC[maxPC];
int  stripPC[maxPC];
int    tdcPC[maxPC];
int    adcPC[maxPC];

int      nEC;
int    secEC[maxEC];
int  layerEC[maxEC];
int  stripEC[maxEC];
int    tdcEC[maxEC];
int    adcEC[maxEC];

int      nEC2;
int   slotEC[maxEC2];
int   chanEC[maxEC2];
float  pedEC[maxEC2];
int     t0EC[maxEC2];
int   adcEC2[maxEC2][100];

int      nPC2;
int   slotPC[maxPC2];
int   chanPC[maxPC2];
float  pedPC[maxPC2];
int     t0PC[maxPC2];
int   adcPC2[maxPC2][100];

int      nSC;
int     idSC[maxSC];
int   slotSC[maxSC];
int   chanSC[maxSC];
float  pedSC[maxSC];
int     t0SC[maxSC];
int    adcSC[maxSC][100];

int nECi,nECo;

static int nslabs[3]={62,23,5};

/* translated data */

#define NHITSSC 100
static int nadc[3][2][62], adc[3][2][62][NHITSSC];
static int ntdc[3][2][62], tdc[3][2][62][NHITSSC];
#define NHITSPC 192
static int npadc[3][68], padc[3][68][NHITSPC];
static int nptdc[3][68], ptdc[3][68][NHITSPC];
static int npl[3],adcrp[3][68],strrp[3][68];
#define NHITSEC 216
static int neadc[6][36], eadc[6][36][NHITSEC];
static int netdc[6][36], etdc[6][36][NHITSEC];
static int nel[6],adcre[6][36],strre[6][36];

#define ABS(x) ((x) < 0 ? -(x) : (x))

#define NCHAN 256

#define TDCRES 41.66667
#define TDCLSB 24
#define tid 100000
#define iid 10000

int tabread(int n, float arr[][n], char fname[]);


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
  int fragment, sector, detector; 
  int nr,sec,strip,nl,ncol,nrow,i,j, k, ii,jj,kk,l,l1,l2,ichan,nn,mm,iev,nbytes,ind1;
  char title[128], *ch;
  char HBOOKfilename[256], PEDfilename[256], chrunnum[32];
  int runnum;
  int nwpawc,lun,lrec,istat,icycle,idn,idnt,nbins,nxbins,nx2bins,nybins,nbins1,igood,offset;
  float x1,x2,y1,y2,x22,ww,tmpx,tmpy,ttt,ref;
  static int doevio=0;
  static int dofadc=0;

  if(argc == 1) {printf("Usage: et2rzn <evio_filename> <runno>  <sector> <maxevents> <doevio> <dofadc>\n"); exit(1);}

  /* check if events come from ET system */
  if(!strncmp(argv[1],"/tmp/et_sys_",12))
  {
    /* check if file exist */
    FILE *fd;
    fd = fopen(argv[1],"r");
    if(fd!=NULL)
	{
      fclose(fd);
      strncpy(et_name,argv[1],ET_FILENAME_LENGTH);
      printf("attach to ET system >%s<\n",et_name);
	}
    else
	{
      printf("ET system >%s< does not exist - exit\n",argv[1]);
      exit(0);
	}
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
  quest_[9]=65000;

  nwpawc = NWPAWC;
  hlimit_(&nwpawc);

  lun = 11;
  lrec = LREC;

  runnum = atoi(argv[2]);
  sec = atoi(argv[3]);
  int maxev = atoi(argv[4]);
  if(argc>5) doevio = atoi(argv[5]);
  if(argc>6) dofadc = atoi(argv[6]);
  
  sprintf(HBOOKfilename,"forcar-s%d-%d.rzn",sec,runnum);

  hropen_(&lun,"NTP",HBOOKfilename,"N",&lrec,&istat,3L,strlen(HBOOKfilename),1);
  hbset_("BSIZE",&siz,&istat,5L);

  if(istat)
    {
   printf("\aError: cannot open RZ file %s for writing.\n", HBOOKfilename);fflush(stdout);
   exit(0);
    }
  else
    {
      printf("RZ file >%s< opened for writing %d events, istat = %d\n\n", HBOOKfilename, maxev, istat);fflush(stdout);
    }

   idnt = 10;
   hbnt_(&idnt,"FCAR"," ",4L,1L);
   hbname_(&idnt," ",0,"$clear",1L,6L);
      
   hbname_(&idnt,"SC1A",&nSC1A,"nSC1A[0,23]:I",4L,13L);
   hbname_(&idnt,"SC1A",&secSC1A,"secSC1A(nSC1A)[1,6]:I",4L,21L);
   hbname_(&idnt,"SC1A",&idSC1A,"idSC1A(nSC1A)[1,23]:I",4L,21L);
   hbname_(&idnt,"SC1A",&tSC1AL,"tSC1AL(nSC1A):I",4L,15L);
   hbname_(&idnt,"SC1A",&tSC1AR,"tSC1AR(nSC1A):I",4L,15L);
   hbname_(&idnt,"SC1A",&aSC1AL,"aSC1AL(nSC1A):I",4L,15L);
   hbname_(&idnt,"SC1A",&aSC1AR,"aSC1AR(nSC1A):I",4L,15L);

   hbname_(&idnt,"SC1B",&nSC1B,"nSC1B[0,62]:I",4L,13L);
   hbname_(&idnt,"SC1B",&secSC1B,"secSC1B(nSC1B)[1,6]:I",4L,21L);
   hbname_(&idnt,"SC1B",&idSC1B,"idSC1B(nSC1B)[1,62]:I",4L,21L);
   hbname_(&idnt,"SC1B",&tSC1BL,"tSC1BL(nSC1B):I",4L,15L);
   hbname_(&idnt,"SC1B",&tSC1BR,"tSC1BR(nSC1B):I",4L,15L);
   hbname_(&idnt,"SC1B",&aSC1BL,"aSC1BL(nSC1B):I",4L,15L);
   hbname_(&idnt,"SC1B",&aSC1BR,"aSC1BR(nSC1B):I",4L,15L);

   hbname_(&idnt,"PCAL",&nPC,"nPC[0,192]:I",4L,12L);
   hbname_(&idnt,"PCAL",&secPC,"secPC(nPC)[1,6]:I",4L,17L);
   hbname_(&idnt,"PCAL",&layerPC,"layerPC(nPC)[1,3]:I",4L,18L);
   hbname_(&idnt,"PCAL",&stripPC,"stripPC(nPC)[1,68]:I",4L,20L);
   hbname_(&idnt,"PCAL",&tdcPC,"TDCPC(nPC):I",4L,12L);
   hbname_(&idnt,"PCAL",&adcPC,"ADCPC(nPC):I",4L,12L);
   
   hbname_(&idnt,"ECAL",&nEC,"nEC[0,216]:I",4L,12L);
   hbname_(&idnt,"ECAL",&secEC,"secEC(nEC)[1,6]:I",4L,17L);
   hbname_(&idnt,"ECAL",&layerEC,"layerEC(nEC)[1,6]:I",4L,18L);
   hbname_(&idnt,"ECAL",&stripEC,"stripEC(nEC)[1,36]:I",4L,20L);
   hbname_(&idnt,"ECAL",&tdcEC,"TDCEC(nEC):I",4L,12L);
   hbname_(&idnt,"ECAL",&adcEC,"ADCEC(nEC):I",4L,12L);

   if (dofadc) 
   {
   hbname_(&idnt,"FADC",&nEC2,"nEC2[0,50]:I",4L,12L);
   hbname_(&idnt,"FADC",&slotEC,"slotEC(nEC2)[0,18]:I",4L,20L);
   hbname_(&idnt,"FADC",&chanEC,"chanEC(nEC2)[0,15]:I",4L,20L);
   hbname_(&idnt,"FADC",&pedEC,"pedEC(nEC2)",4L,11L);
   hbname_(&idnt,"FADC",&t0EC,"t0EC(nEC2)[0,100]:I",4L,19L);
   hbname_(&idnt,"FADC",&adcEC2,"RAWEC(100,nEC2):I",4L,17L);

   hbname_(&idnt,"FADC",&nPC2,"nPC2[0,50]:I",4L,12L);
   hbname_(&idnt,"FADC",&slotPC,"slotPC(nPC2)[0,18]:I",4L,20L);
   hbname_(&idnt,"FADC",&chanPC,"chanPC(nPC2)[0,15]:I",4L,20L);
   hbname_(&idnt,"FADC",&pedPC,"pedPC(nPC2)",4L,11L);
   hbname_(&idnt,"FADC",&t0PC,"t0PC(nPC2)[0,100]:I",4L,19L);
   hbname_(&idnt,"FADC",&adcPC2,"RAWPC(100,nPC2):I",4L,17L);

   hbname_(&idnt,"FADC",&nSC,"nSC[0,20]:I",4L,11L);
   hbname_(&idnt,"FADC",&idSC,"idSC(nSC)[0,1]:I",4L,16L);
   hbname_(&idnt,"FADC",&slotSC,"slotSC(nSC)[0,18]:I",4L,19L);
   hbname_(&idnt,"FADC",&chanSC,"chanSC(nSC)[0,15]:I",4L,19L);
   hbname_(&idnt,"FADC",&pedSC,"pedSC(nSC)",4L,10L);
   hbname_(&idnt,"FADC",&t0SC,"t0SC(nSC)[0,100]:I",4L,18L);
   hbname_(&idnt,"FADC",&adcSC,"RAWSC(100,nSC):I",4L,16L);
   }
   
/* OPEN EVIO OUTPUT FILE */

   if (doevio) 
     {
       sprintf(filename,"forcar-s%d-%d.evio",sec,runnum);
       printf("Opening evio file >%s<\n",filename);
       status = evOpen(filename,"w",&handler);
       printf("evOpen status=%d\n",status);
       if(status!=0)
	 {
	   printf("evOpen error %d - exit\n",status);
	   exit;
	 }  
     }

/* NSA+NSB to calculate pedestals*/
/* From $CLON_PARMS/fadc250/adc*mode3.cnf */

  int nsat[3]={12,12,12};   /* ns */
  //int nsbt[3]={76,156,60};  /* runs 170,171 */
  int nsbt[3]={60,156,44};
  int nsa[3] ={3,3,3};      /* samples */
  int nsb[3] ={15,39,11};
  
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

  /*  
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
  */  


/* EVENT LOOP */

   iev = 0;
   nfile = 0;

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
           /* closing HBOOK file */
	    idn = 0;
	    printf("befor hrout_\n");fflush(stdout);
	    hrout_(&idn,&icycle," ",1);
	    printf("after hrout_\n");fflush(stdout);
	    hrend_("NTP", 3L);
	    printf("after hrend_\n");fflush(stdout);
	    if (doevio) 
	      {
		printf("evClose after %d events\n",iev);fflush(stdout);
		evClose(handler);
	      }
	    exit(0);
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

	if(use_et)
	{
      int handle1;
      if(iet >= nevents)
	  {
        printf("ERROR: iev=%d, nevents=%d\n",iet,nevents);
        exit(0);
	  }
      et_event_getlength(pe[iet], &len); /*get event length from et*/
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

   /* Clear ECAL,PCAL,FTOF counters */

    for(ii=0; ii<6; ii++)
    {
      for(kk=0; kk<36; kk++)
        {
        neadc[ii][kk] = 0;
        netdc[ii][kk] = 0;
	strre[ii][kk] = 0;
	}
      nel[ii]=0;
    }

    for(ii=0; ii<3; ii++)
    {
      for(kk=0; kk<68; kk++)
        {
        npadc[ii][kk] = 0;
        nptdc[ii][kk] = 0;
        strrp[ii][kk] = 0;
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

    nEC2 = nPC2 = nSC = 0;
    tdcref = 0;

    /*TDCs*/
    for(fragment=1; fragment<=36; fragment++)
      {
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

      tdcl = tdcr = 0;

      while(b08<end)
	{
        GET32(word);
        slot = (word>>27)&0x1F;
        edge = (word>>26)&0x1;
	chan = (word>>19)&0x7F;
        val = (word&0x3FFFF)*TDCLSB;
        if(slot==16 && chan==63)
	  {
          if(tdcref==0){tdcref=val;}
	  }
        if(detector==1 || detector==4)
	  {
          ii = tdclayerecal[slot][chan]-1;
	  kk = tdcstripecal[slot][chan]-1;
          etdc[ii][kk][netdc[ii][kk]] = val;
          strre[ii][nel[ii]]=kk+1;nel[ii]++;
	  netdc[ii][kk]++;
	  }
        if(detector==2 || detector==5)
	  {
          ii = tdclayerpcal[slot][chan]-1;
	  kk = tdcstrippcal[slot][chan]-1;
          ptdc[ii][kk][nptdc[ii][kk]] = val;
          strrp[ii][npl[ii]]=kk+1;npl[ii]++;
	  nptdc[ii][kk]++;
	  }
        if(detector==3 || detector==0)
	  {
          ii = tdclayerftof[slot][chan] - 1;
          jj =    tdclrftof[slot][chan] - 1;
          kk =  tdcslabftof[slot][chan] - 1;
          if(ii>=0)
	    {
            tdc[ii][jj][kk][ntdc[ii][jj][kk]] = val;
            ntdc[ii][jj][kk] ++;
	    }
#ifdef DEBUG
        printf("TDC[0x%08x]:  slot=%2d  chan=%3d  edge=%1d  tdc=%5d(%f)  (hist_id=%d)\n",
			   word,slot,chan,edge,val,(((float)val)/1000.),idn);
#endif
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
      int ndata0[22], data0[21][8];
      int sum, channel, summing_in_progress, mmsum, mmt0;
      float baseline;
      int datasaved[1000];

#ifdef DEBUG
      printf("ind1=%d, nbytes=%d\n",ind1,nbytes);
#endif
      b08 = (unsigned char *) &bufptr[ind1];
      end = b08 + nbytes;
#ifdef DEBUG
      printf("ind1=%d, nbytes=%d (from 0x%08x to 0x%08x)\n",ind1,nbytes,b08,end);
#endif

      sector   = (fragment-1)/6 + 1;
      detector = (fragment/2)%6 + 1;

      edet = -1;
      if(detector==1 || detector==4) edet=0;
      if(detector==2 || detector==5) edet=1;
      if(detector==3 || detector==6) edet=2;

      while(b08<end)
        {
#ifdef DEBUG
        printf("begin while: b08=0x%08x\n",b08);
#endif
        GET8(slot);
        GET32(trig);
        GET64(time);
        GET32(nchan);
#ifdef DEBUG
        printf("slot=%d, trig=%d, time=%lld nchan=%d\n",slot,trig,time,nchan);
#endif
        for(nn=0; nn<nchan; nn++)
	  {
          GET8(chan);
          GET32(nsamples);
#ifdef DEBUG
          printf("  chan=%d, nsamples=%d\n",chan,nsamples);
#endif
          baseline = sum = summing_in_progress = mmsum = mmt0 = 0;
          for(mm=0; mm<nsamples; mm++)
	    {
	    GET16(data);
            datasaved[mm] = data;
            if(mm<35) baseline += data;
            if(mm==35)
	      {
              baseline = baseline / 35;
	      //#ifdef DEBUG
              //printf("slot=%d chan=%d baseline=%f\n",slot,chan,baseline);
	      //#endif
	      }
            if(mm>35 && mm<100)
              {
              if(summing_in_progress==0 && data>(baseline+10))
		{
                summing_in_progress = 1;
                for (ii=1;ii<(nsa[edet]+1);ii++) sum += (datasaved[mm-ii]-baseline);
                mmsum=nsa[edet];
                mmt0=mm;
		}
	      if(summing_in_progress>0 && mmsum>(nsa[edet]+nsb[edet]))
		{
		summing_in_progress = -1;
                //printf("det=%d sum=%d tsa+tsb=%d\n",edet,sum,nsa[edet]+nsb[edet]);
		}
              if(summing_in_progress>0 && data<baseline)
	        {
                //summing_in_progress = -1;
		}
              if(summing_in_progress>0)
	        {
                sum += (datasaved[mm]-baseline);
		mmsum++;
		}
	      }
	    }

	  /* fill raw adc pulse hist only if there was a pulse */

	      if(edet==0)
		{
		ii = adclayerecal[slot][chan]-1;
		kk = adcstripecal[slot][chan]-1;
		if(ii>=0 && sum>0)
		  {
		  eadc[ii][kk][neadc[ii][kk]] = sum;
		  neadc[ii][kk]++;
                 
		  if (dofadc)
		    {
		    if (nEC2>=0&&nEC2<maxEC2) 
		    {
		    slotEC[nEC2]=slot;
		    chanEC[nEC2]=chan;
		    pedEC[nEC2]=baseline;
		    t0EC[nEC2]=mmt0;
		    for(jj=0;jj<100;jj++) adcEC2[nEC2][jj]=datasaved[jj];
		    nEC2++;
		    }
		    }
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

		  if (dofadc)
		    {
                    if (nPC2<maxPC2) 
		    {
		    slotPC[nPC2]=slot;
		    chanPC[nPC2]=chan;
		    pedPC[nPC2]=baseline;
		    t0PC[nPC2]=mmt0;
		    for(jj=0;jj<100;jj++) adcPC2[nPC2][jj]=datasaved[jj];
		    nPC2++;
		    }
		    }
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

		  if (dofadc) 
		    {
                    if (nSC<maxSC) 
		    {
		    slotSC[nSC]=slot;
		    chanSC[nSC]=chan;
		    pedSC[nSC]=baseline;
		    idSC[nSC]=ii;
		    t0SC[nSC]=mmt0;
		    for(jj=0;jj<100;jj++) adcSC[nSC][jj]=datasaved[jj];
		    nSC++;
		    }
		    }
		  }
		}
	      
            } 
#ifdef DEBUG
        printf("end loop: b08=0x%08x\n",b08);
#endif
        }   
     }
	
    /* ADC pulsed mode bank */
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

#ifdef DEBUG
        printf("slot=%d, trig=%d, time=%lld nchan=%d\n",slot,trig,time,nchan);
#endif
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
#ifdef DEBUG
          printf("  chan=%d, npulses=%d\n",chan,npulses);
#endif
          for(mm=0; mm<npulses; mm++)
	    {
            b16 = (unsigned short *)b32;
            pulse_time = (*b16++)>>6;
            b32 = (unsigned int *)b16;
            pulse_integral = *b32++;

#ifdef DEBUG
            printf(" b32=0x%08x:  pulse_time=%d pulse_integral=%d\n",b32,pulse_time,pulse_integral);
#endif	

	      if(edet==0)
		{
		sum  = (float)pulse_integral-tabecal[slot][chan]*(nsa[edet]+nsb[edet]);
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
		  }
		}
              if(edet==2) 
		{
                ii = adclayerftof[slot][chan] - 1;
                jj =    adclrftof[slot][chan] - 1;
                kk =  adcslabftof[slot][chan] - 1;
		sum  = (float)pulse_integral-tabftof[slot][chan]*(nsa[edet]+nsb[edet]);
	        if(ii>=0 && sum>0)
		  {
                  adc[ii][jj][kk][nadc[ii][jj][kk]] = sum;
                  nadc[ii][jj][kk] ++;
		  }
		}	      
	    }
	  }
        b08 = (unsigned char *)b32;
#ifdef DEBUG
        printf("end loop: b08=0x%08x\n",b08);
#endif
      }
    }

    } /*fragment*/

    /* PCAL M=3 CUT */

    int il,good_lay[6],ad[6],rs[6],good_12[6],good_a12[6],good_uw,good_vu,good_wv,good_uvw[3];

    for (il=0; il<3 ; il++)
      {
	good_lay[il]=npl[il]==1;
        if (good_lay[il]){rs[il]=strrp[il][0];}
      }

    good_uvw[0]= good_lay[0] && good_lay[1] && good_lay[2];

    if (good_uvw[0]) {printf("PCAL pixel event: u,v,w= %d %d %d\n",rs[0],rs[1],rs[2]);}

    /* EC Inner M=3 CUT */

    for (il=0; il<3 ; il++)
      {
	good_lay[il]=nel[il]==1;
        if (good_lay[il]){rs[il]=strre[il][0];}
      }


    good_uvw[1] = good_lay[0] && good_lay[1] && good_lay[2];
    if (good_uvw[1]) {printf("ECinner pixel event: u,v,w= %d %d %d\n",rs[0],rs[1],rs[2]);}

    /* EC Outer M=3 CUT */

    for (il=3; il<6 ; il++)
      {
	good_lay[il]=nel[il]==1;
        if (good_lay[il]){rs[il]=strre[il][0];}
      }

    good_uvw[2] = good_lay[0] && good_lay[1] && good_lay[2];
    if (good_uvw[2]) {printf("ECouter pixel event: u,v,w= %d %d %d\n",rs[3],rs[4],rs[5]);}

    /*
    good_12[0] = good_uw && rs[2]==61;
    good_12[1] = good_vu && rs[0]==67;
    good_12[2] = good_wv && rs[1]==67;
    
    good_a12[0] = good_12[0] && ad[2]>70;
    good_a12[1] = good_12[1] && ad[0]>70;
    good_a12[2] = good_12[2] && ad[1]>70;
    
    */
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
    /* Fill columnwise ntuple */
    
    nEC=0;nECi=0;nECo=0;
    for(ii=0;ii<6;ii++)
      {
      for(kk=0; kk<36; kk++) 
        { 
      if(neadc[ii][kk]>=1)
	  {
	    secEC[nEC]=sec;
	  layerEC[nEC]=ii+1;
          stripEC[nEC]=kk+1;
            tdcEC[nEC]=etdc[ii][kk][0];
            adcEC[nEC]=eadc[ii][kk][0];
          nEC++;
	  if (layerEC[nEC]<4) {nECi++;}
	  if (layerEC[nEC]>3) {nECo++;}
	  }
	}
      }

    nPC=0;
    for(ii=0;ii<3;ii++)
      {
      for(kk=0; kk<68; kk++) /* counter # */
        { 
      if(npadc[ii][kk]>=1)
	  {
	    secPC[nPC]=sec;
	  layerPC[nPC]=ii+1;
          stripPC[nPC]=kk+1;
            tdcPC[nPC]=ptdc[ii][kk][0];
            adcPC[nPC]=padc[ii][kk][0];
          nPC++;
	  }
	}
      }

    ii=1;
    nSC1A=0;
    for(kk=0; kk<23; kk++) /* counter # */
    { 
      if(ntdc[ii][0][kk]==1 || ntdc[ii][1][kk]==1 || nadc[ii][0][kk]==1 || nadc[ii][1][kk]==1 )
	{
	  secSC1A[nSC1A]=sec;
	  idSC1A[nSC1A]=kk+1;
          tSC1AL[nSC1A]=tdc[ii][0][kk][0];
          tSC1AR[nSC1A]=tdc[ii][1][kk][0];
          aSC1AL[nSC1A]=adc[ii][0][kk][0];
          aSC1AR[nSC1A]=adc[ii][1][kk][0];
          nSC1A++;
	}
    }

    ii=0;
    nSC1B=0;
    for(kk=0; kk<62; kk++) /* counter # */
    { 
      if(ntdc[ii][0][kk]==1 || ntdc[ii][1][kk]==1 || nadc[ii][0][kk]==1 || nadc[ii][1][kk]==1 )
	{
	  secSC1B[nSC1B]=sec;
	  idSC1B[nSC1B]=kk+1;
          tSC1BL[nSC1B]=tdc[ii][0][kk][0];
          tSC1BR[nSC1B]=tdc[ii][1][kk][0];
          aSC1BL[nSC1B]=adc[ii][0][kk][0];
          aSC1BR[nSC1B]=adc[ii][1][kk][0];
          nSC1B++;
	}
    }
    //    if( nEC==3 || nPC==3 || nSC1A==1 || nSC1B==1 ) hfnt_(&idnt);	
    //if( nEC>=6 || nPC>=3 || (nSC1A==1 && nSC1B==1 )) {iev++ ; hfnt_(&idnt);}
    //if( good_uvw[0] || good_uvw[1] || good_uvw[2] || nSC1A==1 || nSC1B==1) {hfnt_(&idnt);}

    if(nECi==3 || nECo==3 || nPC==3 || nSC1A==1 || nSC1B==1) 
      {hfnt_(&idnt);
	if (doevio)
	  {
	    status=evWrite(handler,bufptr);
	    if(status!=0) printf("evWrite returns %d\n",status);
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
  hrend_("NTP", 3L);
  printf("after hrend_\n");fflush(stdout);
  if (doevio) 
    {
      printf("evClose after %d events\n",iev);fflush(stdout);
      evClose(handler);
    }

  exit(0);
}
