package org.clas.fcmon.jroot;

import net.blackruffy.root.*;
//import static net.blackruffy.root.JRoot.*;
import static net.blackruffy.root.JRoot.newTFile;
import static net.blackruffy.root.JRoot.TFile;
import static net.blackruffy.root.JRoot.TTree;
import static net.blackruffy.root.Pointer.*;
import static java.lang.System.*;
import static java.lang.String.format;

public class ReadPcal {

  public static void main( String[] args ) throws Exception {

    TFile file = newTFile("/Users/colesmith/rootfiles/pcal_4294.root", "READ");
    TTree tree = TTree(file.get("h10"));

    long nev = tree.getEntries();
    
    Pointer     pnpc = allocate(4); tree.setBranchAddress("npc", pnpc);
    Pointer playerpc = allocate(4); tree.setBranchAddress("layerpc",playerpc);
    Pointer pstrippc = allocate(4); tree.setBranchAddress("strippc",pstrippc);
    Pointer   padcpc = allocate(4); tree.setBranchAddress("Adcpc",padcpc);
    
    for( long ev=0; ev<100; ev++ ) {
      tree.getEntry(ev);
      int        npc =     pnpc.getIntValue();
      byte[]  layerpc = playerpc.getByteArray(npc);
      byte[]  strippc = pstrippc.getByteArray(npc);
       int[]    adcpc =   padcpc.getUInt16Array(npc);
 
      out.printf("%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,\n",
		 ev,npc,layerpc[0],layerpc[1],layerpc[2],
		        strippc[0],strippc[1],strippc[2],
		          adcpc[0],adcpc[1],adcpc[2]);
  }
  tree.delete();
  file.close();
  }

}
