      real function fcpix()

      include ?

      integer maxstrec(6),nhec(6,6),strec(36,6,6)
      integer maxstrpc(3),nhpc(3,6),strpc(68,3,6)
      integer nn(2)
      real rsec(6),rspc(3)
      logical good_lay(6)
      logical good_uv,good_uw,good_vw
      logical good_uvw_pc,good_pix_pc
      logical good_uvw_ec,good_pix_ec

      data thrpc/30/
      data threc/250/
      data maxstrpc/68,62,62/
      data maxstrec/36,36,36,36,36,36/
      data adccal/0.35/
      data tid/100000/
      data iid/10000/
      data ilmaxec/6/
      data ilmaxpc/3/
      data iomax/1/
      data io/1/
      data nn/0,0/

c is: sector index
c il: layer index
c ip: pmt index

c Init: Clear PC arrays   
      do is=1,6
      do il=1,ilmaxpc
        nhpc(il,is)=0
        do ip=1,maxstrpc(il)
          strpc(ip,il,is)=0.
        enddo
      enddo
      enddo

c Init: Clear EC arrays   
      do is=1,6
      do il=1,ilmaxec
        nhec(il,is)=0
        do ip=1,maxstrec(il)
          strec(ip,il,is)=0.
        enddo
      enddo
      enddo

      uvw = 0.
      
c Loop: Get PC hits and fill arrays          
      do i=1,npc
        is  = secpc(i)
        ip  = strippc(i)
        il  = layerpc(i)
        adc = adccal*adcpc(i)
        if (adc.gt.thrpc) then
          nhpc(il,is) = nhpc(il,is)+1
          inh         = nhpc(il,is)
          strpc(inh,il,is) = ip
          uvw = uvw + uvw_dist_pc(ip,il)
        endif
      enddo

c Histo: Dalitz calculation
      call hf1(777,uvw,1.)
      
c Loop: Sector 5 hardwired for now
      is = 5
      
c Logic: Limit multiplicity to 1 hit per view         
      do il=1,ilmaxpc
        good_lay(il)=nhpc(il,is).eq.1
        if (good_lay(il)) rspc(il)=strpc(1,il,is)
      enddo

c Logic: M=2 cuts
      good_uv = good_lay(1).and.good_lay(2)
      good_uw = good_lay(1).and.good_lay(3)
      good_vw = good_lay(2).and.good_lay(3)

c Logic: M=3 cut
      good_uvw_pc = good_uv.and.good_lay(3)

c Logic: Dalitz cut
      good_pix_pc = abs(uvw-2.0).lt.0.1

c Histo: Dalitz distribution with M=3 cut
      if (good_uvw_pc) call hf1(778,uvw,1.)



c Loop: Get EC hits and fill arrays      
      uvw = 0.

      do i=1,nec
        is  = secec(i)
        ip  = stripec(i)
        il  = layerec(i)
        adc = adcec(i)
        if (adc.gt.threc) then
          nhec(il,is)= nhec(il,is)+1
          inh        = nhec(il,is)
          strec(inh,il,is) = ip
          uvw = uvw + uvw_dist_ec(ip)
        endif
      enddo

c Histo: Dalitz calculation
      call hf1(779,uvw,1.)

c Logic: Limit multiplicity to 1 hit per view         
      do il=1,ilmaxec
        good_lay(il)=nhec(il,is).eq.1
        if (good_lay(il)) rsec(il)=strec(1,il,is)
      enddo

c Logic: M=2 cuts
      good_uv = good_lay(1).and.good_lay(2)
      good_uw = good_lay(1).and.good_lay(3)
      good_vw = good_lay(2).and.good_lay(3)

c Logic: M=3 cut
      good_uvw_ec = good_uv.and.good_lay(3)

c Logic: Dalitz cut
      good_pix_ec = abs(uvw-2.0).lt.0.1

c Histo: Dalitz distribution with M=3 cut
      if (good_uvw_ec) call hf1(780,uvw,1.)

c Loop: Get Panel 1a info

      do i=1,nsc1a
        is = secsc1a(i)
        ip = idsc1a(i)
        adcl=float(asc1al(i))
        adcr=float(asc1ar(i))
        tdcl=float(tsc1al(i))/1e3
        tdcr=float(tsc1ar(i))/1e3
        tdiff=tdcl-tdcr
        gmean=sqrt(adcl*adcr)
        call hf2(400,adcl,float(ip),1.)
        call hf2(401,adcr,float(ip),1.)
        call hf2(402,gmean,float(ip),1.)
        call hf2(403,tdiff,float(ip),1.)
        call hf2(4000+ip,rspc(2),tdiff,1.)
        call hf2(4100+ip,rspc(3),tdiff,1.)
        call hf2(4200+ip,rsec(2),tdiff,1.)
        call hf2(4300+ip,rsec(3),tdiff,1.)
        call hf2(411,rspc(1),float(ip),1.)
        call hf2(412,rspc(2),float(ip),1.)
        call hf2(413,rspc(3),float(ip),1.)
        call hf2(421,rsec(1),float(ip),1.)
        call hf2(422,rsec(2),float(ip),1.)
        call hf2(423,rsec(3),float(ip),1.)
        if (good_uvw_pc) then
        call hf2(500,adcl,float(ip),1.)
        call hf2(501,adcr,float(ip),1.)
        call hf2(502,gmean,float(ip),1.)
        call hf2(503,tdiff,float(ip),1.)
        call hf2(5000+ip,rspc(2),tdiff,1.)
        call hf2(5100+ip,rspc(3),tdiff,1.)
        call hf2(511,rspc(1),float(ip),1.)
        call hf2(512,rspc(2),float(ip),1.)
        call hf2(513,rspc(3),float(ip),1.)
        endif
        if (good_uvw_ec) then
        call hf2(521,rsec(1),float(ip),1.)
        call hf2(522,rsec(2),float(ip),1.)
        call hf2(523,rsec(3),float(ip),1.)
        call hf2(5200+ip,rsec(2),tdiff,1.)
        call hf2(5300+ip,rsec(3),tdiff,1.)
        endif
      enddo

c Loop: Get Panel 1b info

      do i=1,nsc1b
        is = secsc1b(i)
        ip = idsc1b(i)
        adcl=float(asc1bl(i))
        adcr=float(asc1br(i))
        tdcl=float(tsc1bl(i))/1e3
        tdcr=float(tsc1br(i))/1e3
        tdiff=tdcl-tdcr
        gmean=sqrt(adcl*adcr)
        call hf2(700,adcl,float(ip),1.)
        call hf2(701,adcr,float(ip),1.)
        call hf2(702,gmean,float(ip),1.)
        call hf2(703,tdiff,float(ip),1.)
        call hf2(7000+ip,rspc(2),tdiff,1.)
        call hf2(7100+ip,rspc(3),tdiff,1.)
        call hf2(7200+ip,rsec(2),tdiff,1.)
        call hf2(7300+ip,rsec(3),tdiff,1.)
        call hf2(711,rspc(1),float(ip),1.)
        call hf2(712,rspc(2),float(ip),1.)
        call hf2(713,rspc(3),float(ip),1.)
        call hf2(721,rsec(1),float(ip),1.)
        call hf2(722,rsec(2),float(ip),1.)
        call hf2(723,rsec(3),float(ip),1.)
        if (good_uvw_pc) then
        call hf2(800,adcl,float(ip),1.)
        call hf2(801,adcr,float(ip),1.)
        call hf2(802,gmean,float(ip),1.)
        call hf2(803,tdiff,float(ip),1.)
        call hf2(8000+ip,rspc(2),tdiff,1.)
        call hf2(8100+ip,rspc(3),tdiff,1.)
        call hf2(811,rspc(1),float(ip),1.)
        call hf2(812,rspc(2),float(ip),1.)
        call hf2(813,rspc(3),float(ip),1.)
        endif
        if (good_uvw_ec) then
        call hf2(821,rsec(1),float(ip),1.)
        call hf2(822,rsec(2),float(ip),1.)
        call hf2(823,rsec(3),float(ip),1.)
        call hf2(8200+ip,rsec(2),tdiff,1.)
        call hf2(8300+ip,rsec(3),tdiff,1.)
        endif
      enddo

c Loop: Get EC info

      do i=1,nec
        is = secec(i)
        ip = stripec(i)
        il = layerec(i)
        if (il.eq.1) call hf2(911,rspc(1),float(ip),1.)
        if (il.eq.2) call hf2(912,rspc(2),float(ip),1.)
        if (il.eq.3) call hf2(913,rspc(3),float(ip),1.)
        if (good_uvw_pc) then
        if (il.eq.1) call hf2(921,rspc(1),float(ip),1.)
        if (il.eq.2) call hf2(922,rspc(2),float(ip),1.)
        if (il.eq.3) call hf2(923,rspc(3),float(ip),1.)
        if (good_uvw_ec) then
        if (il.eq.1) call hf2(931,rspc(1),float(ip),1.)
        if (il.eq.2) call hf2(932,rspc(2),float(ip),1.)
        if (il.eq.3) call hf2(933,rspc(3),float(ip),1.)
        endif
        endif
      enddo

      nn(1)=nn(1)+1
      if (nn(1).gt.100000) then
        nn(2)=nn(2)+100000
        print *, 'NEVENTS=',nn(2)
        nn(1)=0
      endif

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
