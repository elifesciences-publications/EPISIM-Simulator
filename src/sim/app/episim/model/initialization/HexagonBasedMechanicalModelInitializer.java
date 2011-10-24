package sim.app.episim.model.initialization;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;

import episiminterfaces.EpisimPortrayal;

import sim.app.episim.AbstractCell;
import sim.app.episim.CellInspector;
import sim.app.episim.UniversalCell;
import sim.app.episim.gui.EpisimGUIState;
import sim.app.episim.model.biomechanics.hexagonbased.HexagonBasedMechanicalModel;
import sim.app.episim.model.biomechanics.hexagonbased.HexagonBasedMechanicalModelGlobalParameters;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.visualization.BorderlinePortrayal;
import sim.app.episim.model.visualization.HexagonalCellGridPortrayal2D;
import sim.app.episim.model.visualization.UniversalCellPortrayal2D;
import sim.app.episim.persistence.SimulationStateData;
import sim.app.episim.tissue.TissueController;
import sim.app.episim.visualization.SurfacePortrayal;
import sim.display.GUIState;
import sim.field.grid.ObjectGrid2D;
import sim.portrayal.Inspector;
import sim.portrayal.LocationWrapper;
import sim.portrayal.Portrayal;
import sim.util.Double2D;


public class HexagonBasedMechanicalModelInitializer extends BiomechanicalModelInitializer {
	
	public HexagonBasedMechanicalModelInitializer(){
		super();
		TissueController.getInstance().getTissueBorder().loadNoMembrane();
	}
	
	public HexagonBasedMechanicalModelInitializer(SimulationStateData simulationStateData){
		super(simulationStateData);		
	}

	
	protected ArrayList<UniversalCell> buildStandardInitialCellEnsemble() {
		ArrayList<UniversalCell> standardCellEnsemble = new ArrayList<UniversalCell>();
		HexagonBasedMechanicalModelGlobalParameters globalParameters = (HexagonBasedMechanicalModelGlobalParameters) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		
		
		
		int width = (int)(((HexagonBasedMechanicalModelGlobalParameters.initialPositionWoundEdge_Mikron)/globalParameters.getCellDiameter_mikron()));
		int height = (int)((globalParameters.getHeightInMikron()/globalParameters.getCellDiameter_mikron()));
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				UniversalCell cell = new UniversalCell(null, null, null);
				((HexagonBasedMechanicalModel) cell.getEpisimBioMechanicalModelObject()).setCellLocationInCellField(new Double2D(x, y));
				((ObjectGrid2D) ModelController.getInstance().getBioMechanicalModelController().getCellField()).field[x][y] = cell;
				standardCellEnsemble.add(cell);
			}
		}	
		return standardCellEnsemble;
	}

	
	protected void initializeCellEnsembleBasedOnRandomAgeDistribution(ArrayList<UniversalCell> cellEnsemble) {

		// is not needed int this model
	}

	protected EpisimPortrayal getCellPortrayal() {
			   
		HexagonalCellGridPortrayal2D portrayal =  new HexagonalCellGridPortrayal2D(java.awt.Color.lightGray){
			public Inspector getInspector(LocationWrapper wrapper, GUIState state) {
			// make the inspector
				return new CellInspector(super.getInspector(wrapper, state), wrapper, state);
			}
		};		
		return portrayal;
   }

	
	protected EpisimPortrayal[] getAdditionalPortrayalsCellForeground() {
		HexagonBasedMechanicalModelGlobalParameters globalParameters = (HexagonBasedMechanicalModelGlobalParameters) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		BorderlinePortrayal initialWoundEdge = new BorderlinePortrayal("Initial Wound Edge", Color.WHITE, HexagonBasedMechanicalModelGlobalParameters.initialPositionWoundEdge_Mikron, 0, 
            HexagonBasedMechanicalModelGlobalParameters.initialPositionWoundEdge_Mikron, globalParameters.getHeightInMikron());
		globalParameters.setInitialWoundEdgeBorderlineConfig(initialWoundEdge.getBorderlineConfig());
		return new EpisimPortrayal[]{initialWoundEdge};
	}
	
	protected EpisimPortrayal[] getAdditionalPortrayalsCellBackground() {		
		return  new EpisimPortrayal[0];
		/*
		 * new EpisimPortrayal[]{new SurfacePortrayal("Surface A",Color.MAGENTA,
		 * 0, 0, 500, SurfacePortrayal.MAX_HEIGHT_FLAG), new
		 * SurfacePortrayal("Surface B",Color.ORANGE, 500, 0,
		 * TissueController.getInstance
		 * ().getTissueBorder().getWidthInMikron()-500,
		 * SurfacePortrayal.MAX_HEIGHT_FLAG)};
		 */
	}

}
