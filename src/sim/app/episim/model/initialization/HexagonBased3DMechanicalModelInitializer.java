package sim.app.episim.model.initialization;

import java.util.ArrayList;

import episiminterfaces.EpisimCellType;
import episiminterfaces.EpisimPortrayal;
import sim.app.episim.UniversalCell;

import sim.app.episim.model.biomechanics.hexagonbased.HexagonBasedMechanicalModel;
import sim.app.episim.model.biomechanics.hexagonbased.HexagonBasedMechanicalModelGP;
import sim.app.episim.model.biomechanics.hexagonbased3d.HexagonBased3DMechanicalModel;
import sim.app.episim.model.biomechanics.hexagonbased3d.HexagonBased3DMechanicalModelGP;
import sim.app.episim.model.biomechanics.hexagonbased3d.HexagonalCellField3D;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.persistence.SimulationStateData;
import sim.app.episim.tissue.TissueController;
import sim.field.grid.ObjectGrid2D;

import sim.util.Double2D;
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
				
		int width = (int) (HexagonBased3DMechanicalModelGP.number_of_columns);
		int height = (int) (HexagonBased3DMechanicalModelGP.number_of_rows);
		int length= (int) (HexagonBased3DMechanicalModelGP.number_of_columns);
		int delta = (int)HexagonBased3DMechanicalModelGP.number_of_initially_occupied_layers/2;
		for(int z = ((length/2)-delta); z < ((length/2)+delta); z++){
			for(int y = ((height/2)-delta) ;y < ((height/2)+delta); y++){			
				for(int x = ((width/2)-delta); x < ((width/2)+delta); x++){
					UniversalCell cell = new UniversalCell(null, null);
					((HexagonBased3DMechanicalModel) cell.getEpisimBioMechanicalModelObject()).setCellLocationInCellField(new Double3D(x, y, z));
				
					standardCellEnsemble.add(cell);
				}
			}
		}
		addSekretionCellColony(standardCellEnsemble);
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
   
   private void addSekretionCellColony(ArrayList<UniversalCell> standardCellEnsemble){
	
		EpisimCellType[] cellTypes =ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getAvailableCellTypes();	
		
		int width = (int) (HexagonBased3DMechanicalModelGP.number_of_columns);
		
		int length= (int) (HexagonBased3DMechanicalModelGP.number_of_columns);
		int delta = (int)HexagonBased3DMechanicalModelGP.number_of_initially_occupied_layers/2;
		for(int z = ((length/2)-delta); z < ((length/2)+delta); z++){
			for(int y = 0 ;y < 2; y++){			
				for(int x = ((width/2)-delta); x < ((width/2)+delta); x++){
					UniversalCell cell = new UniversalCell(null, null);
					((HexagonBased3DMechanicalModel) cell.getEpisimBioMechanicalModelObject()).setCellLocationInCellField(new Double3D(x, y, z));
					if(cellTypes.length >1) cell.getEpisimCellBehavioralModelObject().setCellType(cellTypes[1]);
					standardCellEnsemble.add(cell);
				}
			}
		}
   }
   
}
