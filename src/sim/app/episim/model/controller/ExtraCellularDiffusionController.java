package sim.app.episim.model.controller;

import java.util.HashMap;

import episiminterfaces.EpisimDiffusionFieldConfiguration;

import sim.app.episim.model.diffusion.ExtraCellularDiffusionField;
import sim.app.episim.model.initialization.ExtraCellularDiffusionInitializer;
import sim.app.episim.persistence.SimulationStateData;
import sim.app.episim.tissue.TissueController;


public class ExtraCellularDiffusionController {
	
	
	
	private static ExtraCellularDiffusionController instance = new ExtraCellularDiffusionController();
	
	private HashMap<String, ExtraCellularDiffusionField> extraCellularFieldMap;
	
	private EpisimDiffusionFieldConfiguration[] episimExtraCellularDiffusionFieldsConfigurations = new EpisimDiffusionFieldConfiguration[]{ new EpisimDiffusionFieldConfiguration(){
      public String getDiffusionFieldName() {	     
	      return "TestDiffusionField";
      }

		
      public double getDiffusionCoefficient() {

	      // TODO Auto-generated method stub
	      return 0.00000000000002;
      }

		
      public double getLatticeSiteSizeInMikron() {

	      // TODO Auto-generated method stub
	      return 1;
      }

		
      public double getDegradationRate() {

	      // TODO Auto-generated method stub
	      return 0;
      }

		
      public int getNumberOfIterationsPerCBMSimStep() {

	      // TODO Auto-generated method stub
	      return 1;
      }

		
      public double getDeltaTimeInSecondsPerIteration() {	      
	      return 1;
      }

		
      public double getMaximumConcentration() {	     
	      return 255;
      }

		
      public double getMinimumConcentration() {	      
	      return 0;
      }


		public double getDefaultConcentration() {

			return 0;
		}}};
	
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
	
	public ExtraCellularDiffusionField[] getAllExtraCellularDiffusionFields(){
		return extraCellularFieldMap.values().toArray(new ExtraCellularDiffusionField[extraCellularFieldMap.size()]);
	}
	
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
	
	

}
