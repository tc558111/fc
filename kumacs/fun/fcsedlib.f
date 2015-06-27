c Fill vectors for single event display (SED)      
      
      real function pcsed()
      
      include ?
      
      vector sedstr
      vector sedlay
      vector sedadc
      vector sedcol
      vector seduvw
      vector s1astr
      vector s1bstr
      vector secstr
      vector seclay
      vector secadc
      vector seccol

      logical bad
      
      do i=1,100
        sedstr(i)=0
        sedlay(i)=0
        sedadc(i)=0
        sedcol(i)=0
        s1astr(i)=0
        s1bstr(i)=0
        secstr(i)=0
        seclay(i)=0
        secadc(i)=0
        seccol(i)=0
      enddo
      
      nsum=0
      uvw=0
      
      n=0
      do i=1,npc
      is=strippc(i)
      il=layerpc(i)
      uvw=uvw+uvw_dist(is,il)
      adc=adcpc(i)
      if (adc.gt.20) then
      n=n+1
      sedstr(n)=is
      sedlay(n)=il
      sedadc(n)=adc
      sedcol(n)=getcolor(adc,0.,14.5,2)
      print *, 'Layer=',sedlay(n),' Strip=',sedstr(n),' ADC=',adc
      nsum=nsum+sedstr(n)
      endif
      enddo
      print *, 'U+V+W=',nsum,' UVW=',uvw
      print *, ' '
      seduvw(1)=uvw
      call hf1(777,uvw,1.)

      n=0
      do i=1,nsc1a
      tdcl=float(tsc1al(i))/1e3
      tdcr=float(tsc1ar(i))/1e3
      tdiff=tdcl-tdcr
      n=n+1
      s1astr(n)=idsc1a(i)
      enddo

      n=0
      do i=1,nsc1b
      tdcl=float(tsc1bl(i))/1e3
      tdcr=float(tsc1br(i))/1e3
      tdiff=tdcl-tdcr
      n=n+1
      s1bstr(n)=idsc1b(i)
      enddo
      
      n=0
      do i=1,nec
      is=stripec(i)
      il=layerec(i)
      adc=adcec(i)
      if (adc.gt.20) then
      n=n+1
      secstr(n)=is
      seclay(n)=il
      secadc(n)=adc
      seccol(n)=getcolor(adc,0.,14.5,2)
      endif
      enddo
      
      end
      
      real function uvw_dist(is,il)
      
      if (il.eq.1.and.is.le.52) uvw=is/84.
      if (il.eq.1.and.is.gt.52) uvw=(52+(is-52)*2)/84.
      if (il.eq.2.and.is.le.15) uvw=2*is/77.
      if (il.eq.2.and.is.gt.15) uvw=(30+(is-15))/77.
      if (il.eq.3.and.is.le.15) uvw=2*is/77.
      if (il.eq.3.and.is.gt.15) uvw=(30+(is-15))/77.
      
      uvw_dist = uvw
      
      end
      
      real function getcolor(val,rmin,rmax,opt)
      
      real val,rmin,rmax
      integer opt
      
      vector nlev
      
      if (val.eq.0) then
        getcolor=9
        return
      endif
      
      if (opt.eq.1) then
        getcolor = ifix((val-rmin)/(rmax-rmin)*nlev(1)+8)
      elseif (opt.eq.2) then
        getcolor = ifix((log(val)-rmin)/(rmax-rmin)*nlev(1)+8)
      endif
      end
