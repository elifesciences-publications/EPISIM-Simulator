package sim.app.episim.model.initialization;

import java.io.File;
import java.util.ArrayList;

import episiminterfaces.EpisimPortrayal;

import sim.app.episim.UniversalCell;
import sim.app.episim.model.controller.ModelController;
import sim.portrayal.Portrayal;


public class ModelInitialization {
	
	private File modelInitializationFile;
	
	private BiomechanicalModelInitializer biomechanicalModelInitializer;
	private CellBehavioralModelInitializer cellbehavioralModelInitializer;
	
	
	public ModelInitialization(){
		this(null);
	}
	
	public ModelInitialization(File modelInitializationFile){
		this.modelInitializationFile = modelInitializationFile;
		if(modelInitializationFile== null){
			this.biomechanicalModelInitializer = ModelController.getInstance().getBioMechanicalModelController().getBiomechanicalModelInitializer();
			this.cellbehavioralModelInitializer = ModelController.getInstance().getCellBehavioralModelController().getCellBehavioralModelInitializer();
		}
		else{
			this.biomechanicalModelInitializer = ModelController.getInstance().getBioMechanicalModelController().getBiomechanicalModelInitializer(modelInitializationFile);
			this.cellbehavioralModelInitializer = ModelController.getInstance().getCellBehavioralModelController().getCellBehavioralModelInitializer(modelInitializationFile);
		}
		if(this.cellbehavioralModelInitializer == null || this.biomechanicalModelInitializer == null) 
			throw new IllegalArgumentException("Neither the CellBehavioralModelInitializer nor the BiomechanicalModelInitializer must be null!");
	}
	
	public ArrayList<UniversalCell> getCells(){
	
		if(this.modelInitializationFile == null) return buildStandardCellList();
		
		//TODO: Implement Alternative using an initialization file
		return new ArrayList<UniversalCell>();
	}
	
	public EpisimPortrayal getCellPortrayal(){
		return biomechanicalModelInitializer.getCellPortrayal();
	}
	
	public EpisimPortrayal[] getAdditionalPortrayalsCellBackground(){
		return biomechanicalModelInitializer.getAdditionalPortrayalsCellBackground();
	}
	
	public EpisimPortrayal[] getAdditionalPortrayalsCellForeground(){
		return biomechanicalModelInitializer.getAdditionalPortrayalsCellForeground();
	}
	
	private ArrayList<UniversalCell> buildStandardCellList(){
		ArrayList<UniversalCell> initiallyExistingCells = this.biomechanicalModelInitializer.buildStandardInitialCellEnsemble();
		
		this.cellbehavioralModelInitializer.initializeCellEnsemble(initiallyExistingCells);
		this.biomechanicalModelInitializer.initializeCellEnsembleBasedOnRandomAgeDistribution(initiallyExistingCells);
		return initiallyExistingCells;
	}
	
	
	
	

}
