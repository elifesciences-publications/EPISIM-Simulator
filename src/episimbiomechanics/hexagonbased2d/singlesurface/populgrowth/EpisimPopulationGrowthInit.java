package episimbiomechanics.hexagonbased2d.singlesurface.populgrowth;

import java.util.ArrayList;

import sim.app.episim.model.UniversalCell;
import sim.app.episim.model.biomechanics.latticebased2D.LatticeBased2DModel;
import sim.app.episim.model.biomechanics.latticebased2D.demo.LatticeBased2DModelDemoGP;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters;
import sim.app.episim.persistence.SimulationStateData;
import sim.app.episim.tissue.TissueController;
import sim.app.episim.util.CellInspector;
import sim.app.episim.visualization.HexagonalCellGridPortrayal2D;
import sim.display.GUIState;
import sim.portrayal.Inspector;
import sim.portrayal.LocationWrapper;
import sim.util.Double2D;
import episiminterfaces.EpisimCellType;
import episiminterfaces.EpisimPortrayal;


public class EpisimPopulationGrowthInit extends BiomechanicalModelInitializer {
	
	public EpisimPopulationGrowthInit(){
		super();
		TissueController.getInstance().getTissueBorder().loadNoMembrane();
		
	}
	
	public EpisimPopulationGrowthInit(SimulationStateData simulationStateData){
		super(simulationStateData);		
	}

	
	protected ArrayList<UniversalCell> buildStandardInitialCellEnsemble() {
		ArrayList<UniversalCell> standardCellEnsemble = new ArrayList<UniversalCell>();
		LatticeBased2DModelDemoGP globalParameters = (LatticeBased2DModelDemoGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		int width = (int) (globalParameters.getNumber_of_columns());
		int height = (int) globalParameters.getNumber_of_rows();
		EpisimCellType[] cellTypes =ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getAvailableCellTypes();
		int delta = (int)globalParameters.getNumber_of_initially_occupied_columns();
		delta = delta==0 ? 1 : delta;		
		for(int y = ((height/2)-delta);y < ((height/2)+delta); y++){			
			for(int x = ((width/2)-delta); x < ((width/2)+delta); x++){
				UniversalCell cell = new UniversalCell(null, null, true);
				((LatticeBased2DModel) cell.getEpisimBioMechanicalModelObject()).setCellLocationInCellField(new Double2D(x, y));
				if(cellTypes.length >0) cell.getEpisimCellBehavioralModelObject().setCellType(cellTypes[0]);
				standardCellEnsemble.add(cell);
			}
		}	
		return standardCellEnsemble;
	}
	
	
	protected void initializeCellEnsembleBasedOnRandomAgeDistribution(ArrayList<UniversalCell> cellEnsemble) {

		// is not needed in this model
	}

	protected EpisimPortrayal getCellPortrayal() {
			   
		HexagonalCellGridPortrayal2D portrayal =  new HexagonalCellGridPortrayal2D(java.awt.Color.lightGray){
			public Inspector getInspector(LocationWrapper wrapper, GUIState state) {
			// make the inspector
				return new CellInspector(super.getInspector(wrapper, state), wrapper, state);
			}
		};
		portrayal.setField(ModelController.getInstance().getBioMechanicalModelController().getCellField());
		return portrayal;
   }

	
	protected EpisimPortrayal[] getAdditionalPortrayalsCellForeground() {
		return new EpisimPortrayal[0];
	}
	
	protected EpisimPortrayal[] getAdditionalPortrayalsCellBackground() {		
		return  new EpisimPortrayal[0];
	}

}
