package sim.app.episim.model.initialization;

import java.io.File;
import java.util.ArrayList;

import episiminterfaces.EpisimPortrayal;

import sim.app.episim.UniversalCell;
import sim.portrayal.Portrayal;


public abstract class BiomechanicalModelInitializer {
	
	private File modelInitializationFile;
	
	public BiomechanicalModelInitializer(){
		this(null);
	}
	
	public BiomechanicalModelInitializer(File file){
		this.modelInitializationFile = file;
	}
	
	protected ArrayList<UniversalCell> getInitialCellEnsemble(){
		if(this.modelInitializationFile == null) return buildStandardInitialCellEnsemble();
		else return buildInitialCellEnsemble(modelInitializationFile);
	}
	
	protected abstract ArrayList<UniversalCell> buildStandardInitialCellEnsemble();
	
	protected abstract void initializeCellEnsembleBasedOnRandomAgeDistribution(ArrayList<UniversalCell> cellEnsemble);
	
	protected abstract ArrayList<UniversalCell> buildInitialCellEnsemble(File file);

	/**
	 * Get component for visualizing the cells
	 * 
	 */
	protected abstract EpisimPortrayal getCellPortrayal();
	
	/**
	 * Other visualizing components
	 * 
	 */
	protected abstract EpisimPortrayal[] getAdditionalPortrayalsCellForeground();
	
	/**
	 * Other visualizing components
	 * 
	 */
	protected abstract EpisimPortrayal[] getAdditionalPortrayalsCellBackground();
	
   protected File getModelInitializationFile(){   
   	return modelInitializationFile;
   }

}
