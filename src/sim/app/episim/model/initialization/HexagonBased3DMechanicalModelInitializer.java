package sim.app.episim.model.initialization;

import java.util.ArrayList;

import episiminterfaces.EpisimPortrayal;
import sim.app.episim.UniversalCell;
import sim.app.episim.model.biomechanics.hexagonbased3d.HexagonBased3DMechanicalModelGP;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.visualization.HexagonalCellGridPortrayal3D;
import sim.app.episim.persistence.SimulationStateData;
import sim.app.episim.tissue.TissueController;


public class HexagonBased3DMechanicalModelInitializer extends BiomechanicalModelInitializer {
	private HexagonBased3DMechanicalModelGP globalParameters;
	public HexagonBased3DMechanicalModelInitializer(){
		super();
		TissueController.getInstance().getTissueBorder().loadNoMembrane();
		globalParameters = (HexagonBased3DMechanicalModelGP)ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
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
   	
	   return new HexagonalCellGridPortrayal3D(2*HexagonBased3DMechanicalModelGP.outer_hexagonal_radius);
   }

	
   protected EpisimPortrayal[] getAdditionalPortrayalsCellForeground() {

	   // TODO Auto-generated method stub
	   return new EpisimPortrayal[0];
   }

	
   protected EpisimPortrayal[] getAdditionalPortrayalsCellBackground() {
	   
	   return new EpisimPortrayal[0];
   }
}
