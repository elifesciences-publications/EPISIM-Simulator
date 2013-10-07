package sim.app.episim.model.biomechanics;

import java.lang.reflect.Modifier;
import java.util.ArrayList;

import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;

import episimbiomechanics.EpisimModelConnector;
import episimexceptions.ModelCompatibilityException;
import episiminterfaces.EpisimBiomechanicalModel;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;


public class BiomechanicalModelLoader{
	
	private Class<? extends EpisimModelConnector> episimModelConnectorClass;
	private Class<? extends EpisimBiomechanicalModel> episimBiomechanicalModelClass;
	private Class<? extends BiomechanicalModelInitializer> biomechanicalModelInitializerClass;
	private EpisimBiomechanicalModelGlobalParameters episimBiomechnicalModelGlobalParametersObject;
	private String episimBiomechanicalModelName = "";
	private String episimBiomechanicalModelId = "";
	
	public BiomechanicalModelLoader(String modelConnectorId) throws ModelCompatibilityException{
		EpisimModelConnector modelConnector = findModelConnector(modelConnectorId);
		this.episimModelConnectorClass = modelConnector.getClass();
		this.episimBiomechanicalModelClass = modelConnector.getEpisimBioMechanicalModelClass();
		this.biomechanicalModelInitializerClass = modelConnector.getEpisimBioMechanicalModelInitializerClass();
		this.episimBiomechanicalModelName = modelConnector.getBiomechanicalModelName();
		this.episimBiomechanicalModelId = modelConnector.getBiomechanicalModelId();
		if(modelConnector.getEpisimBioMechanicalModelGlobalParametersClass() != null){
			try{
	         this.episimBiomechnicalModelGlobalParametersObject = modelConnector.getEpisimBioMechanicalModelGlobalParametersClass().newInstance();
         }
         catch (InstantiationException e){
         	ExceptionDisplayer.getInstance().displayException(e);
         }
         catch (IllegalAccessException e){
         	ExceptionDisplayer.getInstance().displayException(e);
         }
		}
	}
	
	private EpisimModelConnector findModelConnector(String modelConnectorId) throws ModelCompatibilityException{
		ArrayList<EpisimModelConnector> foundModelConnectors = new ArrayList<EpisimModelConnector>();
		
		for(Class<? extends EpisimModelConnector> actModelConnectorClass : EpisimModelConnector.getAvailableModelConnectors()){
			try{
			
				if(!Modifier.isAbstract(actModelConnectorClass.getModifiers())){
					EpisimModelConnector modelConnectorInstance = actModelConnectorClass.newInstance();
		         if(modelConnectorInstance.getBiomechanicalModelId().equals(modelConnectorId)) foundModelConnectors.add(modelConnectorInstance);
				}				
         }
         catch (InstantiationException e){
	         ExceptionDisplayer.getInstance().displayException(e);
         }
         catch (IllegalAccessException e){
         	 ExceptionDisplayer.getInstance().displayException(e);
         }
		}
		if(foundModelConnectors.size() <= 0) throw new ModelCompatibilityException("Found no Model Connector which is compatible with the loaded Cell Behavioral Model!");
		if(foundModelConnectors.size() > 1) throw new ModelCompatibilityException("Found MULTIPLE Model Connectors which are compatible with the loaded Cell Behavioral Model!");
		
		return foundModelConnectors.get(0);
	}

	
   public Class<? extends EpisimModelConnector> getEpisimModelConnectorClass() {
   
   	return episimModelConnectorClass;
   }

	
   public Class<? extends EpisimBiomechanicalModel> getEpisimBiomechanicalModelClass() {
   
   	return episimBiomechanicalModelClass;
   }
   
   public Class<? extends BiomechanicalModelInitializer> getBiomechanicalModelInitializerClass(){
   	return biomechanicalModelInitializerClass;
   }

	
   public String getEpisimBiomechanicalModelName() {
   
   	return episimBiomechanicalModelName;
   }

	
   public String getEpisimBiomechanicalModelId() {
   
   	return episimBiomechanicalModelId;
   }

	
   public EpisimBiomechanicalModelGlobalParameters getEpisimBiomechnicalModelGlobalParametersObject() {
   
   	return episimBiomechnicalModelGlobalParametersObject;
   }

}
