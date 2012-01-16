package sim.app.episim.model.initialization;

import java.util.ArrayList;

import episiminterfaces.EpisimPortrayal;
import sim.app.episim.UniversalCell;
import sim.app.episim.persistence.SimulationStateData;
import sim.app.episim.tissue.TissueController;


public class HexagonBased3DMechanicalModelInitializer extends BiomechanicalModelInitializer {
	public HexagonBased3DMechanicalModelInitializer(){
		super();
		TissueController.getInstance().getTissueBorder().loadNoMembrane();
	}
	
	public HexagonBased3DMechanicalModelInitializer(SimulationStateData simulationStateData){
		super(simulationStateData);		
	}

	@Override
   protected ArrayList<UniversalCell> buildStandardInitialCellEnsemble() {

	   // TODO Auto-generated method stub
	   return null;
   }

	@Override
   protected void initializeCellEnsembleBasedOnRandomAgeDistribution(ArrayList<UniversalCell> cellEnsemble) {

	   // TODO Auto-generated method stub
	   
   }

	@Override
   protected EpisimPortrayal getCellPortrayal() {

	   // TODO Auto-generated method stub
	   return null;
   }

	@Override
   protected EpisimPortrayal[] getAdditionalPortrayalsCellForeground() {

	   // TODO Auto-generated method stub
	   return null;
   }

	@Override
   protected EpisimPortrayal[] getAdditionalPortrayalsCellBackground() {

	   // TODO Auto-generated method stub
	   return null;
   }
}
