      real function fc()

      include ?

      integer nh(9,6),nic(3,6),nnic(3,6),ilic(6)
      integer strr(68,9,6),adcr(68,9,6),tdcr(68,9,6)
      integer maxstr(3),hid,hidd,nn(2)
      integer rs1,rs2,rs3
      real    rs(9),ad(9),td(9),uvw(3,6)
      logical good_lay(9)
      logical good_uw(3),good_vu(3),good_wv(3)
      logical good_uwt(3),good_vut(3),good_wvt(3)
      logical good_uwtt(3),good_vutt(3),good_wvtt(3)
      logical good_uwtp(3),good_vutp(3),good_wvtp(3)
      logical good_uvw(3),good_pixel(3,6)

      integer rscutuw(3),rscutvu(3),rscutwv(3),rsw(3)
      integer adcutuw(3),adcutvu(3),adcutwv(3)

      data rscutuw/60,35,35/  
      data rscutvu/67,35,35/
      data rscutwv/67,35,35/
      data rsw/1,2,2/
      data adcutuw/70,5,5/
      data adcutvu/70,5,5/
      data adcutwv/70,5,5/

      data ilic/1,2,3,1,2,3/
      data tid/100000/
      data cid/10000/
      data lid/100/
      data nn/0,0/

      vector srange
      vector icrange
      vector thresh
      vector adccal
      vector tdccal
      vector pixcut

c Loop limits for calorimeter type (PCAL:1 ECAL:2-3)

      ic1=icrange(1)
      ic2=icrange(2)

c Loop limits for sector number

      is1=srange(1)
      is2=srange(2)

c FTOF Monitor Histos

      call fill_ftof(664,665)

c is: sector index (1-6)
c il: layer index (PCAL:1-3 ECAL:4-9)
c ip: pmt index (PCAL: 1-68,62,62 ECAL:1-36)

c Init: Zero out arrays   

      do is=is1,is2
        do ic=ic1,ic2
          uvw(ic,is)=0
          nic(ic,is)=0
          nnic(ic,is)=0
        enddo
        do il=1,9
          nh(il,is)=0
          do ip=1,68
            strr(ip,il,is)=0.
            adcr(ip,il,is)=0.
            tdcr(ip,il,is)=0.
          enddo
        enddo
      enddo
      
c Loop: Get PC hits and fill arrays      

      if (ic1.eq.1) then
      ic = 1
  
      do i=1,npc
        is  = secpc(i)
        ip  = strippc(i)
        il  = layerpc(i)
        adc = adcpc(i)*adccal(ic)
        tdc = tdcpc(i)*tdccal(ic)
        hid = 1e7*is + 10*tid + ic*cid + il*lid
        call hf2(hid,adc,float(ip),1.)
        hid = 1e7*is + 11*tid + ic*cid + il*lid
        call hf2(hid,tdc,float(ip),1.)
        nnic(ic,is)       = nnic(ic,is)+1
        if (adc.gt.thresh(ic)) then
          nh(il,is)       = nh(il,is)+1
          nic(ic,is)      = nic(ic,is)+1
          inh             = nh(il,is)
          adcr(inh,il,is) = adc
          tdcr(inh,il,is) = tdc
          strr(inh,il,is) = ip
          uvw(ic,is) = uvw(ic,is) + uvw_dist_pc(ip,il)
        endif
      enddo

      endif

c Loop: Get EC hits and fill arrays      

      if (ic1.gt.1) then

      do i=1,nec
        is  = secec(i)
        ip  = stripec(i)
        ill = layerec(i)
        ic  = 2 
        if(ill.gt.3) ic=3
        il  = ilic(ill)
        iv  = ill+3
        adc = adcec(i)*adccal(ic)
        tdc = tdcec(i)*tdccal(ic)
        hid = 1e7*is + 10*tid + ic*cid + il*lid
        call hf2(hid,adc,float(ip),1.)
        hid = 1e7*is + 11*tid + ic*cid + il*lid
        call hf2(hid,tdc,float(ip),1.)
        nnic(ic,is)       = nnic(ic,is)+1
        if (adc.gt.thresh(ic)) then
          nh(iv,is)       = nh(iv,is)+1
          nic(ic,is)      = nic(ic,is)+1
          inh             = nh(iv,is)
          adcr(inh,iv,is) = adc
          tdcr(inh,iv,is) = tdc
          strr(inh,iv,is) = ip
          uvw(ic,is) = uvw(ic,is) + uvw_dist_ec(ip)
        endif
      enddo

      endif

c Loop: Loop over sectors       
      
      do is=is1,is2
      hid=1e7*is

      call hf1(hid+1,float(nnic(1,is)),1.)
      call hf1(hid+2,float(nnic(2,is)),1.)
      call hf1(hid+3,float(nnic(3,is)),1.)
      call hf1(hid+4,float(nic(1,is)),1.)
      call hf1(hid+5,float(nic(2,is)),1.)
      call hf1(hid+6,float(nic(3,is)),1.)

      call hf2(hid+7,float(nnic(2,is)),float(nnic(3,is)),1.)
      call hf2(hid+8,float(nic(2,is)),float(nic(3,is)),1.)

c Logic: Avoid single PMT triggers

      if (nic(1,is).gt.1.or.
     1    nic(2,is).gt.1.or.
     1    nic(3,is).gt.1) then

c Logic: Limit multiplicity to 1 hit per view   
      
      do il=1,9
        good_lay(il)=nh(il,is).eq.1
        if (good_lay(il)) then
          rs(il)=strr(1,il,is)
          ad(il)=adcr(1,il,is)
          td(il)=tdcr(1,il,is)
        endif
      enddo

      do ic=ic1,ic2
      icm=ic-1

c Logic: Dalitz calculation

      good_pixel(ic,is) = abs(uvw(ic,is)-2.0).lt.pixcut(ic)
      
c Histo: Dalitz calculation

      call hf1(700+ic,uvw(ic,is),1.)

c Logic: Good two-view and three view multiplicity (m2,m3 cut)

      good_uw(ic)  = good_lay(1+icm*3).and.good_lay(3+icm*3)
      good_vu(ic)  = good_lay(2+icm*3).and.good_lay(1+icm*3)
      good_wv(ic)  = good_lay(3+icm*3).and.good_lay(rsw(ic)+icm*3)
      good_uvw(ic) =       good_vu(ic).and.good_lay(3+icm*3)

c Monitor: Fill FTOF monitor histos if good_uv(1)

      if (good_vu(ic)) call fill_ftof(666,667)

c Logic: Next longest strip (s cut) and strip/threshold (st cut)            

      good_uwt(ic)  =  good_uw(ic).and.rs(3+icm*3).eq.rscutuw(ic)
      good_vut(ic)  =  good_vu(ic).and.rs(1+icm*3).eq.rscutvu(ic)
      good_wvt(ic)  =  good_wv(ic).and.rs(rsw(ic)+icm*3).eq.rscutwv(ic)
      good_uwtt(ic) = good_uwt(ic).and.ad(3+icm*3).gt.adcutuw(ic)
      good_vutt(ic) = good_vut(ic).and.ad(1+icm*3).gt.adcutvu(ic)
      good_wvtt(ic) = good_wvt(ic).and.ad(rsw(ic)+icm*3).gt.adcutwv(ic)
      good_uwtp(ic) = good_uwt(ic).and.good_pixel(ic,is)
      good_vutp(ic) = good_vut(ic).and.good_pixel(ic,is)
      good_wvtp(ic) = good_wvt(ic).and.good_pixel(ic,is)

      enddo

c Histo: MIP plots using trigger condition (here u.v coincidence) (TAG=15)
c        and pixel cut (TAG=16)

      do ic=ic1,ic2
      icm=ic-1
      do il=1,3
        ill=icm*3+il
        hidd = hid+16*tid+ic*cid+il*lid
        if (good_pixel(ic,is)) call hf2(hidd,ad(ill),rs(ill),1.) 
        hidd = hid+15*tid+ic*cid+il*lid
        if       (good_vu(ic)) call hf2(hidd,ad(ill),rs(ill),1.)
      enddo
      enddo
      
c Histo: MIP plots using m2 and s cuts (TAG=20)

      do ic=ic1,ic2
      icm=ic-1
      hidd = hid+20*tid+ic*cid

      if(good_uwt(ic)) call hf2(hidd+1*lid,ad(1+icm*3),rs(1+icm*3),1.)
      if(good_vut(ic)) call hf2(hidd+2*lid,ad(2+icm*3),rs(2+icm*3),1.)
      if(good_wvt(ic)) call hf2(hidd+3*lid,ad(3+icm*3),rs(3+icm*3),1.)

c Histo: MIP plots using Dalitz (m3 cut) and s cut (TAG=21)

      if (good_uvw(ic)) then
      
      hidd = hid+21*tid+ic*cid
      if(good_uwt(ic)) call hf2(hidd+1*lid,ad(1+icm*3),rs(1+icm*3),1.)
      if(good_vut(ic)) call hf2(hidd+2*lid,ad(2+icm*3),rs(2+icm*3),1.)
      if(good_wvt(ic)) call hf2(hidd+3*lid,ad(3+icm*3),rs(3+icm*3),1.)
      
c Histo: MIP plots using Dalitz (m3 cut) and st cut (TAG=22)

      hidd = hid+22*tid+ic*cid
      if(good_uwtt(ic)) call hf2(hidd+1*lid,ad(1+icm*3),rs(1+icm*3),1.)
      if(good_vutt(ic)) call hf2(hidd+2*lid,ad(2+icm*3),rs(2+icm*3),1.)
      if(good_wvtt(ic)) call hf2(hidd+3*lid,ad(3+icm*3),rs(3+icm*3),1.)
      
c Histo: U vs V, U vs W, V vs W (used for detector map) (TAG=40) 
    
      hidd = hid+40*tid+ic*cid
 
      do i=1,nh(1+icm*3,is)
        ris1=strr(i,1+icm*3,is)
      do k=1,nh(2+icm*3,is)
        ris2=strr(k,2+icm*3,is)
        call hf2(hidd+12*lid+1,ris1,ris2,1.)             !uv
        call hf2(hidd+12*lid+2,ris1,ris2,ad(1+icm*3))    !uv
      enddo
      do k=1,nh(3+icm*3,is)
        ris3=strr(k,3+icm*3,is)
        call hf2(hidd+13*lid+1,ris1,ris3,1.)             !uw
        call hf2(hidd+13*lid+2,ris1,ris3,ad(1+icm*3))    !uw
        call hf2(hidd+13*lid+3,ris1,ris3,td(3+icm*3)-td(1+icm*3))
      enddo
      enddo
      
      do i=1,nh(2+icm*3,is)
        ris2=strr(i,2+icm*3,is)
      do k=1,nh(3+icm*3,is)
        ris3=strr(k,3+icm*3,is)
        call hf2(hidd+23*lid+1,ris2,ris3,1.)             !vw
        call hf2(hidd+23*lid+2,ris2,ris3,ad(2+icm*3))    !vw
        call hf2(hidd+32*lid+1,ris3,ris2,1.)             !wv
        call hf2(hidd+32*lid+2,ris3,ris2,ad(3+icm*3))    !wv
      enddo
      enddo

c Histo: Attenuation lengths (ADC vs strip) (TAG=50)
      
      hidd  = hid+50*tid+ic*cid   
      
      rs1=rs(1+icm*3)
      rs2=rs(2+icm*3)
      rs3=rs(3+icm*3)
      ad1=ad(1+icm*3)
      ad2=ad(2+icm*3)
      ad3=ad(3+icm*3)
      
      if(good_vu(ic)) then
         if(ad2.gt.5)call hf2(hidd+21*lid+rs1,float(rs2),ad1,1.)
         if(ad1.gt.5)call hf2(hidd+12*lid+rs2,float(rs1),ad2,1.)
      endif
      if(good_uw(ic)) then
         if(ad3.gt.5)call hf2(hidd+31*lid+rs1,float(rs3),ad1,1.)
         if(ad1.gt.5)call hf2(hidd+13*lid+rs3,float(rs1),ad3,1.)
      endif
      if(good_wv(ic)) then
         if(ad3.gt.5)call hf2(hidd+32*lid+rs2,float(rs3),ad2,1.)
         if(ad2.gt.5)call hf2(hidd+23*lid+rs3,float(rs2),ad3,1.)
      endif  
      
      endif

      enddo

      endif

      enddo

100   nn(1)=nn(1)+1
      if (nn(1).gt.100000) then
        nn(2)=nn(2)+100000
        print *, 'NEVENTS=',nn(2)
        nn(1)=0
      endif

      return
      end

      subroutine fill_ftof(id1,id2)

      include ?

c FTOF geom. mean plots for stability checks

      do i=1,nsc1a
        al=asc1al(i)
        ar=asc1ar(i)
        gm=sqrt(al*ar)
        call hf2(id1,gm,float(idsc1a(i)),1.)
      enddo

      do i=1,nsc1b
        al=asc1bl(i)
        ar=asc1br(i)
        gm=sqrt(al*ar)
        call hf2(id2,gm,float(idsc1b(i)),1.)
      enddo

      return
      end

      real function uvw_dist_pc(ip,il)
      
      if (il.eq.1.and.ip.le.52) uvw=ip/84.
      if (il.eq.1.and.ip.gt.52) uvw=(52+(ip-52)*2)/84.
      if (il.eq.2.and.ip.le.15) uvw=2*ip/77.
      if (il.eq.2.and.ip.gt.15) uvw=(30+(ip-15))/77.
      if (il.eq.3.and.ip.le.15) uvw=2*ip/77.
      if (il.eq.3.and.ip.gt.15) uvw=(30+(ip-15))/77.
      
      uvw_dist_pc = uvw
      
      end
      
      real function uvw_dist_ec(ip)
      
      uvw_dist_ec=ip/36.
      
      end

