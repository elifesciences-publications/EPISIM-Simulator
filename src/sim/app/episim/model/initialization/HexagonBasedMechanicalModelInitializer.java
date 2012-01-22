package sim.app.episim.model.initialization;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;

import episiminterfaces.EpisimCellType;
import episiminterfaces.EpisimPortrayal;

import sim.app.episim.AbstractCell;
import sim.app.episim.CellInspector;
import sim.app.episim.UniversalCell;
import sim.app.episim.gui.EpisimGUIState;
import sim.app.episim.model.biomechanics.hexagonbased.HexagonBasedMechanicalModel;
import sim.app.episim.model.biomechanics.hexagonbased.HexagonBasedMechanicalModelGP;
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
		HexagonBasedMechanicalModelGP globalParameters = (HexagonBasedMechanicalModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		int width = (int)HexagonBasedMechanicalModelGP.number_of_initially_occupied_columns;
		int height = (int)HexagonBasedMechanicalModelGP.number_of_rows;
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				UniversalCell cell = new UniversalCell(null, null);
				((HexagonBasedMechanicalModel) cell.getEpisimBioMechanicalModelObject()).setCellLocationInCellField(new Double2D(x, y));
				//((ObjectGrid2D) ModelController.getInstance().getBioMechanicalModelController().getCellField()).field[x][y] = cell;
				standardCellEnsemble.add(cell);
			}
		}	
		addSekretionCellColony(standardCellEnsemble);
		return standardCellEnsemble;
	}
	
	private void addSekretionCellColony(ArrayList<UniversalCell> standardCellEnsemble){
		HexagonBasedMechanicalModelGP globalParameters = (HexagonBasedMechanicalModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		int width = 6;
		int height = 6;
		int startX = (int)HexagonBasedMechanicalModelGP.number_of_columns-10;
		int startY = (int)((HexagonBasedMechanicalModelGP.number_of_rows/2)-(height/2));
		EpisimCellType[] cellTypes =ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getAvailableCellTypes();
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				UniversalCell cell = new UniversalCell(null, null);
				((HexagonBasedMechanicalModel) cell.getEpisimBioMechanicalModelObject()).setCellLocationInCellField(new Double2D(startX+x, startY+y));
				((ObjectGrid2D) ModelController.getInstance().getBioMechanicalModelController().getCellField()).field[startX+x][startY+y] = cell;
				
				if(cellTypes.length >1) cell.getEpisimCellBehavioralModelObject().setCellType(cellTypes[1]);
				standardCellEnsemble.add(cell);
			}
		}	
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
		HexagonBasedMechanicalModelGP globalParameters = (HexagonBasedMechanicalModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		BorderlinePortrayal initialWoundEdge = new BorderlinePortrayal("Initial Wound Edge", Color.WHITE, HexagonBasedMechanicalModelGP.initialPositionWoundEdge_Mikron, 0, 
            HexagonBasedMechanicalModelGP.initialPositionWoundEdge_Mikron, globalParameters.getHeightInMikron());
		globalParameters.setInitialWoundEdgeBorderlineConfig(initialWoundEdge.getBorderlineConfig());
		BorderlinePortrayal actualWoundEdge = new BorderlinePortrayal("Actual Averaged Wound Edge", Color.RED, HexagonBasedMechanicalModelGP.initialPositionWoundEdge_Mikron, 0, 
            HexagonBasedMechanicalModelGP.initialPositionWoundEdge_Mikron, globalParameters.getHeightInMikron());
		globalParameters.setActualWoundEdgeBorderlineConfig(actualWoundEdge.getBorderlineConfig());
		return new EpisimPortrayal[]{initialWoundEdge, actualWoundEdge};
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
