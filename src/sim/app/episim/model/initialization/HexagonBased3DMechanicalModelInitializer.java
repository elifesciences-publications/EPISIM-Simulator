package sim.app.episim.model.initialization;

import java.util.ArrayList;

import episiminterfaces.EpisimPortrayal;
import sim.app.episim.UniversalCell;

import sim.app.episim.model.biomechanics.hexagonbased3d.HexagonBased3DMechanicalModel;
import sim.app.episim.model.biomechanics.hexagonbased3d.HexagonBased3DMechanicalModelGP;
import sim.app.episim.model.biomechanics.hexagonbased3d.HexagonalCellField3D;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.persistence.SimulationStateData;
import sim.app.episim.tissue.TissueController;

import sim.util.Double3D;


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
				
		int x = (int) (HexagonBased3DMechanicalModelGP.number_of_columns /2);
		int y = (int) (HexagonBased3DMechanicalModelGP.number_of_rows /2);
		int z = (int) (HexagonBased3DMechanicalModelGP.number_of_columns /2);
		UniversalCell cell = new UniversalCell(null, null);
		((HexagonBased3DMechanicalModel) cell.getEpisimBioMechanicalModelObject()).setCellLocationInCellField(new Double3D(0, 0, 0));
		standardCellEnsemble.add(cell);
   	return standardCellEnsemble;
   }

   protected void initializeCellEnsembleBasedOnRandomAgeDistribution(ArrayList<UniversalCell> cellEnsemble) {
		// is not needed int this model
   }

	
   protected EpisimPortrayal getCellPortrayal() {
   	Object cellField = ModelController.getInstance().getBioMechanicalModelController().getCellField();
	   if(cellField instanceof HexagonalCellField3D){
   	 return ((HexagonalCellField3D) cellField).getCellFieldPortrayal();
	   }
	   return null;
   }

	
   protected EpisimPortrayal[] getAdditionalPortrayalsCellForeground() {

	   // TODO Auto-generated method stub
	   return new EpisimPortrayal[0];
   }

	
   protected EpisimPortrayal[] getAdditionalPortrayalsCellBackground() {
	   
	   return new EpisimPortrayal[0];
   }
   
   
}
