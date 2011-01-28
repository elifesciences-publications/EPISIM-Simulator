package sim.app.episim.model.initialization;

import java.io.File;
import java.util.ArrayList;

import sim.app.episim.UniversalCell;


public class VertexBasedMechanicalModelInitializer extends AbstractBiomechanicalModelInitializer {
	
	public VertexBasedMechanicalModelInitializer(){
		super();
	}
	
	public VertexBasedMechanicalModelInitializer(File modelInitializationFile){
		super(modelInitializationFile);
	}

	@Override
	protected ArrayList<UniversalCell> buildStandardInitialCellEnsemble() {

		// TODO: Implement this method
		return new ArrayList<UniversalCell>();
	}

	@Override
	protected ArrayList<UniversalCell> buildInitialCellEnsemble(File file) {

		// TODO: Implement this method
		return new ArrayList<UniversalCell>();
	}

}
