package sim.app.episim.model.initialization;

import java.io.File;
import java.util.ArrayList;

import sim.app.episim.UniversalCell;
import sim.portrayal.Portrayal;


public class HexagonBasedMechanicalModelInitializer extends BiomechanicalModelInitializer {
	
	public HexagonBasedMechanicalModelInitializer(){
		super();
	}
	public HexagonBasedMechanicalModelInitializer(File modelInitializationFile){
		super(modelInitializationFile);
	}

	@Override
	protected ArrayList<UniversalCell> buildStandardInitialCellEnsemble() {

		// TODO Auto-generated method stub
		return null;
	}

	
	protected void initializeCellEnsembleBasedOnRandomAgeDistribution(ArrayList<UniversalCell> cellEnsemble) {

		// is not needed here
	}

	@Override
	protected ArrayList<UniversalCell> buildInitialCellEnsemble(File file) {

		// TODO Auto-generated method stub has to be implemented
		return new ArrayList<UniversalCell>();
	}

	@Override
	protected Portrayal getCellPortrayal() {
		
		return null;
	}

}
