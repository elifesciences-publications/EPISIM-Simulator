package sim.app.episim.model.initialization;

import java.io.File;
import java.util.ArrayList;

import sim.app.episim.UniversalCell;


public abstract class AbstractBiomechanicalModelInitializer {
	
	private File modelInitializationFile;
	
	public AbstractBiomechanicalModelInitializer(){
		this(null);
	}
	
	public AbstractBiomechanicalModelInitializer(File file){
		this.modelInitializationFile = file;
	}
	
	protected ArrayList<UniversalCell> getInitialCellEnsemble(){
		if(this.modelInitializationFile == null) return buildStandardInitialCellEnsemble();
		else return buildInitialCellEnsemble(modelInitializationFile);
	}
	
	protected abstract ArrayList<UniversalCell> buildStandardInitialCellEnsemble();
	
	protected abstract ArrayList<UniversalCell> buildInitialCellEnsemble(File file);

	
   protected File getModelInitializationFile(){   
   	return modelInitializationFile;
   }

}
