/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.rec.ecn;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;

import org.jlab.clara.base.ClaraUtil;
import org.jlab.clara.engine.Engine;
import org.jlab.clara.engine.EngineData;
import org.jlab.clara.engine.EngineDataType;
import org.jlab.clara.engine.EngineStatus;
import org.jlab.clas.detector.DetectorType;
import org.jlab.clasrec.data.Clas12Types;
import org.jlab.clasrec.utils.DataBaseLoader;
import org.jlab.clasrec.utils.DatabaseConstantProvider;
import org.jlab.clasrec.utils.ServiceConfiguration;
//import org.jlab.evio.clas12.EvioDataEvent;
//import org.jlab.evio.clas12.EvioFactory;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.base.Detector;
import org.jlab.geom.detector.dc.DCFactory;
import org.jlab.geom.detector.ec.ECFactory;
import org.jlab.geom.detector.ft.FTCALFactory;
import org.jlab.geom.detector.ftof.FTOFFactory;

//clas12rec
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.evio.EvioFactory;
/**
 *
 * @author gavalian
 */

public abstract class DetectorReconstruction implements Engine {
    
    private String serviceName     = "undefined";
    private String serviceVersion  = "1.0";
    private String serviceAuthor  = "undefined";
    private String serviceDescription = "No description";
    
    private final String mainModuleName     = "[CLASREC-MAIN] ====>>>>> ";
    
    private final HashMap<String,Detector>  detectorGeometry = new HashMap<String,Detector>();
    private final ServiceConfiguration serviceConfig = new ServiceConfiguration();
    
    private Integer serviceDebugLevel   = 0;
    
    private Integer geometryRunNumber      = 10;
    private String  geometryVariation      = "default";
    private Integer calibrationRunNumber   = 10;
    private String  calibrationVariation   = "default";

    private TreeMap<String,DatabaseConstantProvider>  calibrationConstants = 
            new TreeMap<String,DatabaseConstantProvider>();
    
    
    public abstract void processEvent(EvioDataEvent event);
    public abstract void init();
    public abstract void configure(ServiceConfiguration c);
    
    /**
     * Returns the debug level.
     * @return debug level
     */
    public int debugLevel(){
        return this.serviceDebugLevel;
    }
    /**
     * Set the debug level for the service. 
     * @param level debug level
     */
    public void setDebugLevel(int level){
        this.serviceDebugLevel = level;
        if(level<0)  serviceDebugLevel = 0;
        if(level>10) serviceDebugLevel = 10;
    }

    public void setCalibrationVariation(String var){
        this.calibrationVariation = var;
        System.out.println("[DET-INIT] ["+this.getName()+"] ====> "
        + "set calibration variation " + var);
    }
    
    public void setCalibrationRun(int run){
        this.calibrationRunNumber = run;
        System.out.println("[DET-INIT] ["+this.getName()+"] ====> "
        + "set calibration run # " + run);
    }
    
    public void setGeometryVariation(String var){
        this.geometryVariation = var;
        System.out.println("[DET-INIT] ["+this.getName()+"] ====> "
        + "set geometry variation " + var);
    }
    
    public void setGeometryRun(int run){
        this.geometryRunNumber = run;
        System.out.println("[DET-INIT] ["+this.getName()+"] ====> "
        + "set geometry run # " + run);
    }
    /**
     * Returns a constant provider for calibration table requested.
     * In order for the table to be loaded in the init() method one must
     * call requireCalibration(calib_table_name).
     * @param name
     * @return 
     */
    public ConstantProvider getConstants(String name){
        if(this.calibrationConstants.containsKey(name)==true){
            return (ConstantProvider) this.calibrationConstants.get(name);
        }
        return null;
    }
    /**
     * Returns geometry object for detector "geom". To use this function
     * first the geometry for given detector has to be loaded through
     * requireGeometry(geomname)
     * @param geom detector name.
     * @return 
     */
    public Detector getGeometry(String geom){
        if(detectorGeometry.containsKey(geom)==false){
            System.err.println(mainModuleName + "ERROR : requested geometry "
            + " for detector " + geom + " is not loaded.");
            System.err.println(mainModuleName + "ERROR : use requireGeometry(\""
                    + geom + "\") function first");
            return null;
        }
        return detectorGeometry.get(geom);
    }
    /**
     * Loads geometry package for given detector into local map. To access 
     * it use getGeometry(geomname)
     * @param geometryPackage  geometry package name ("EC","FTOF","DC","FTCAL" etc.)
     */
    public void requireGeometry(String geometryPackage){
        
        if(geometryPackage.compareTo("DC::Tilted")==0){
            DCFactory factory = new DCFactory();
            ConstantProvider  data = DataBaseLoader.getDetectorConstants(DetectorType.DC);
            Detector geomFTOF = factory.createDetectorTilted(data);
            detectorGeometry.put("DC::Tilted", geomFTOF);
            System.err.println(mainModuleName + "geometry for detector " +
                    geometryPackage + " is loaded...");
            return;
        }
        
        if(geometryPackage.compareTo("DC")==0){
            DCFactory factory = new DCFactory();
            ConstantProvider  data = DataBaseLoader.getDetectorConstants(DetectorType.DC);
            Detector geomFTOF = factory.createDetectorCLAS(data);
            detectorGeometry.put("DC", geomFTOF);
            System.err.println(mainModuleName + "geometry for detector " +
                    geometryPackage + " is loaded...");
            return;
        }
        
        if(geometryPackage.compareTo("FTOF")==0){
            FTOFFactory factory = new FTOFFactory();
            ConstantProvider  data = DataBaseLoader.getDetectorConstants(DetectorType.FTOF);
            Detector geomFTOF = factory.createDetectorCLAS(data);
            detectorGeometry.put("FTOF", geomFTOF);
            System.err.println(mainModuleName + "geometry for detector " +
                    geometryPackage + " is loaded...");
            return;
        }
        
        if(geometryPackage.compareTo("FTCAL")==0){
            FTCALFactory factory = new FTCALFactory();
            ConstantProvider  data = DataBaseLoader.getDetectorConstants(DetectorType.FTCAL);
            Detector geomFTCAL = factory.createDetectorCLAS(data);
            detectorGeometry.put("FTCAL", geomFTCAL);
            System.err.println(mainModuleName + "geometry for detector " +
                    geometryPackage + " is loaded...");
            return;
        }
        
        if(geometryPackage.compareTo("EC")==0){
            ECFactory factory = new ECFactory();
            ConstantProvider  data = DataBaseLoader.getDetectorConstants(DetectorType.EC);
            Detector geomEC = factory.createDetectorCLAS(data);
            detectorGeometry.put("EC", geomEC);
            System.err.println(mainModuleName + "geometry for detector " +
                    geometryPackage + " is loaded...");
            return;
        }
        
        System.err.println(mainModuleName + "WARRNING : geometry package with name "
        + geometryPackage + " does not exist..");
    }
    
    public void requireCalibration(String calibPackage){
        DatabaseConstantProvider  provider = new DatabaseConstantProvider(
                this.calibrationRunNumber,this.calibrationVariation);
        provider.loadTable(calibPackage);
        provider.disconnect();
        this.calibrationConstants.put(calibPackage, provider);
    }
    
    public void requireCalibration(String system,String... vars){
        DatabaseConstantProvider  provider = new DatabaseConstantProvider(
                this.calibrationRunNumber,this.calibrationVariation);
        for(String var : vars){
            provider.loadTable(var);
        }
        this.calibrationConstants.put(system, provider);
        provider.disconnect();
    }
    
    public DetectorReconstruction(String name, String author,String version){
        serviceName = name;
        serviceAuthor = author;
        serviceVersion = version;
    }
    
    public void setDescription(String desc){
        serviceDescription = desc;
    }
    
    @Override
    public EngineData configure(EngineData js) {        
        this.init();
        return js;
    }

    @Override
    public EngineData execute(EngineData input) {
        EngineData output = input;
 
        // Validate input type
        String mt = input.getMimeType();
        if (!mt.equalsIgnoreCase(Clas12Types.EVIO.mimeType())) {
            String msg = String.format("Wrong input type: %s", mt);
            output.setStatus(EngineStatus.ERROR);
            output.setDescription(msg);
            return output;
        }
        
        EvioDataEvent dataevent = null;
        try {
            ByteBuffer bb = (ByteBuffer) input.getData();
            byte[] buffer = bb.array();
            ByteOrder endianness = bb.order();
            dataevent = new EvioDataEvent(buffer, endianness, EvioFactory.getDictionary());
        } catch (/* EvioException */ Exception e) {
            // Actually, EvioDataEvent is not throwing any exception, but I think it should.
            // Why having an EvioDataEvent that failed to extract the event?
            String msg = String.format("Error reading input event%n%n%s", ClaraUtil.reportException(e));
            output.setStatus(EngineStatus.ERROR);
            output.setDescription(msg);
            return output;
        }
        try {
            this.processEvent(dataevent);
        } catch (Exception e) {
            String msg = String.format("Error processing input event%n%n%s", ClaraUtil.reportException(e));
            output.setStatus(EngineStatus.ERROR);
            output.setDescription(msg);
            return output;
        }
 
        // Save event.
        output.setData(Clas12Types.EVIO.mimeType(), dataevent.getEventBuffer());
 
        return output;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public EngineData executeGroup(Set<EngineData> inputs) {
        return null;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void destroy() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Deprecated
    public void destruct() {
        destroy();
    }

    public String getName() {
        return serviceName;
    }

    @Override
    public String getAuthor() {
        return serviceAuthor;
    }

    @Override
    public String getDescription() {
        return serviceDescription;
    }

    @Override
    public String getVersion() {
        return serviceVersion;
    }

    @Deprecated
    public String getLanguage() {
        return "java";
    }

    @Override
    public Set<EngineDataType> getInputDataTypes() {
        return ClaraUtil.buildDataTypes(Clas12Types.EVIO,
                                        Clas12Types.PROPERTY_LIST,
                                        EngineDataType.STRING);
    }

    @Override
    public Set<EngineDataType> getOutputDataTypes() {
        return ClaraUtil.buildDataTypes(Clas12Types.EVIO,
                                        Clas12Types.PROPERTY_LIST,
                                        EngineDataType.STRING);
    }

    @Override
    public Set<String> getStates() {
        return null;
    }

    @Override
    public void reset() {
        // nothing
    }
}
