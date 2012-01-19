package sim.app.episim.model.controller;

import java.util.HashMap;

import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import episiminterfaces.EpisimDiffusionFieldConfiguration;

import sim.app.episim.EpisimProperties;
import sim.app.episim.model.diffusion.ExtraCellularDiffusionField;
import sim.app.episim.model.diffusion.ExtraCellularDiffusionField2D;
import sim.app.episim.model.diffusion.TestDiffusionFieldConfiguration;
import sim.app.episim.model.initialization.ExtraCellularDiffusionInitializer;
import sim.app.episim.persistence.SimulationStateData;
import sim.app.episim.tissue.TissueController;


public class ExtraCellularDiffusionController {
	
	public enum DiffusionFieldCrossSectionMode{		
		X_Y_PLANE("X-Y-Plane"),
		X_Z_PLANE("X-Z-Plane"),
		Y_Z_PLANE("Y-Z-Plane");
		
		private String name;
		DiffusionFieldCrossSectionMode(String name){
			this.name = name;
		
		}		
		public String toString(){ return this.name; }
	}
	
	private DiffusionFieldCrossSectionMode selectedDiffusionFieldCrossSectionMode = DiffusionFieldCrossSectionMode.X_Y_PLANE;
	
	private double diffusionFieldCrossSectionCoordinateInMikron = 0;
	
	
	private static ExtraCellularDiffusionController instance = new ExtraCellularDiffusionController();
	
	private HashMap<String, ExtraCellularDiffusionField> extraCellularFieldMap;
	
	private EpisimDiffusionFieldConfiguration[] episimExtraCellularDiffusionFieldsConfigurations;
	
	private ExtraCellularDiffusionController(){
		extraCellularFieldMap = new HashMap<String, ExtraCellularDiffusionField>();
	}	
	
	
	
	
	public int getNumberOfEpisimExtraCellularDiffusionFieldConfigurations(){
		return episimExtraCellularDiffusionFieldsConfigurations != null ? episimExtraCellularDiffusionFieldsConfigurations.length: 0;
	}
	
	public EpisimDiffusionFieldConfiguration[] getEpisimExtraCellularDiffusionFieldsConfigurations(){
		return episimExtraCellularDiffusionFieldsConfigurations;
	}
	
	public EpisimDiffusionFieldConfiguration getEpisimExtraCellularDiffusionFieldsConfiguration(String fieldName){
		for(int i = 0; i < this.episimExtraCellularDiffusionFieldsConfigurations.length; i++){
			if(this.episimExtraCellularDiffusionFieldsConfigurations[i].getDiffusionFieldName().equals(fieldName)){
				return this.episimExtraCellularDiffusionFieldsConfigurations[i];
			}
		}
		return null;
	}
	
	public <T extends ExtraCellularDiffusionField> T[] getAllExtraCellularDiffusionFields(T[] fieldArray){
		if(fieldArray != null && fieldArray.length == getNumberOfFields()) return extraCellularFieldMap.values().toArray(fieldArray);
		else throw new IllegalArgumentException("fieldArray is null or size does not fit");
	}
	
	public int getNumberOfFields(){ return this.extraCellularFieldMap.size(); } 
	
	public ExtraCellularDiffusionField getExtraCellularDiffusionField(String name){
		return this.extraCellularFieldMap.get(name);
	}
	
	public void setExtraCellularFieldMap(HashMap<String, ExtraCellularDiffusionField> extraCellularFieldMap){
		this.extraCellularFieldMap = extraCellularFieldMap;
	}
	
	
	protected static ExtraCellularDiffusionController getInstance(){
		return instance;
	}
	
	public ExtraCellularDiffusionInitializer getExtraCellularDiffusionInitializer(){
		return new ExtraCellularDiffusionInitializer();
	}
	public ExtraCellularDiffusionInitializer getExtraCellularDiffusionInitializer(SimulationStateData simulationStateData){
		return new ExtraCellularDiffusionInitializer(simulationStateData);
	}
	
	protected void newCellBehavioralModelLoaded(){
		extraCellularFieldMap = new HashMap<String, ExtraCellularDiffusionField>();
		if(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_DIFFUSION_FIELD_TESTMODE)!= null &&
				EpisimProperties.getProperty(EpisimProperties.SIMULATOR_DIFFUSION_FIELD_TESTMODE).equals(EpisimProperties.ON)){
			this.episimExtraCellularDiffusionFieldsConfigurations = new EpisimDiffusionFieldConfiguration[]{new TestDiffusionFieldConfiguration()};
		}
		else{
			EpisimCellBehavioralModelGlobalParameters globalParameters = ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters();
			if(globalParameters != null){
				this.episimExtraCellularDiffusionFieldsConfigurations = globalParameters.getAllExtraCellularDiffusionFieldConfigurations();
			}
		}
		if(this.episimExtraCellularDiffusionFieldsConfigurations == null)
			this.episimExtraCellularDiffusionFieldsConfigurations = new EpisimDiffusionFieldConfiguration[0];
	}




	
	public DiffusionFieldCrossSectionMode getSelectedDiffusionFieldCrossSectionMode() {
	
		return selectedDiffusionFieldCrossSectionMode;
	}
	public void setSelectedDiffusionFieldCrossSectionMode(DiffusionFieldCrossSectionMode selectedDiffusionFieldCrossSectionMode) {
	
		this.selectedDiffusionFieldCrossSectionMode = selectedDiffusionFieldCrossSectionMode;
	}
	public double getDiffusionFieldCrossSectionCoordinate() {
	
		return diffusionFieldCrossSectionCoordinateInMikron;
	}	
	public void setDiffusionFieldCrossSectionCoordinate(double diffusionFieldCrossSectionCoordinate) {
	
		this.diffusionFieldCrossSectionCoordinateInMikron = diffusionFieldCrossSectionCoordinate;
	}

}
