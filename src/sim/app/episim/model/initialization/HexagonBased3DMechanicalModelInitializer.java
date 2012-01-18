package sim.app.episim.model.initialization;

import java.util.ArrayList;

import episiminterfaces.EpisimPortrayal;
import sim.app.episim.UniversalCell;
import sim.app.episim.model.visualization.HexagonalCellGridPortrayal3D;
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


   protected ArrayList<UniversalCell> buildStandardInitialCellEnsemble() {
   	ArrayList<UniversalCell> standardCellEnsemble = new ArrayList<UniversalCell>();
   	return standardCellEnsemble;
   }

   protected void initializeCellEnsembleBasedOnRandomAgeDistribution(ArrayList<UniversalCell> cellEnsemble) {
		// is not needed int this model
   }

	
   protected EpisimPortrayal getCellPortrayal() {
	   return new HexagonalCellGridPortrayal3D();
   }

	
   protected EpisimPortrayal[] getAdditionalPortrayalsCellForeground() {

	   // TODO Auto-generated method stub
	   return new EpisimPortrayal[0];
   }

	
   protected EpisimPortrayal[] getAdditionalPortrayalsCellBackground() {
	   
	   return new EpisimPortrayal[0];
   }
}
