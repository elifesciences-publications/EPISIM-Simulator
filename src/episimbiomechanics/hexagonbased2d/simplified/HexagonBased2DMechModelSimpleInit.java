package episimbiomechanics.hexagonbased2d.simplified;

import java.awt.Color;
import java.util.ArrayList;

import sim.app.episim.CellInspector;
import sim.app.episim.UniversalCell;
import sim.app.episim.model.biomechanics.hexagonbased.HexagonBasedMechanicalModel;
import sim.app.episim.model.biomechanics.hexagonbased.HexagonBasedMechanicalModelGP;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters;
import sim.app.episim.model.visualization.BorderlinePortrayal;
import sim.app.episim.model.visualization.HexagonalCellGridPortrayal2D;
import sim.app.episim.persistence.SimulationStateData;
import sim.app.episim.tissue.TissueController;
import sim.display.GUIState;
import sim.field.grid.ObjectGrid2D;
import sim.portrayal.Inspector;
import sim.portrayal.LocationWrapper;
import sim.util.Double2D;
import episiminterfaces.EpisimCellType;
import episiminterfaces.EpisimPortrayal;


public class HexagonBased2DMechModelSimpleInit  extends BiomechanicalModelInitializer {
	
	public HexagonBased2DMechModelSimpleInit(){
		super();
		TissueController.getInstance().getTissueBorder().loadNoMembrane();
		MiscalleneousGlobalParameters.instance().setTypeColor(4);
	}
	
	public HexagonBased2DMechModelSimpleInit(SimulationStateData simulationStateData){
		super(simulationStateData);		
	}

	
	protected ArrayList<UniversalCell> buildStandardInitialCellEnsemble() {
		ArrayList<UniversalCell> standardCellEnsemble = new ArrayList<UniversalCell>();
		HexagonBasedMechanicalModelGP globalParameters = (HexagonBasedMechanicalModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		globalParameters.setInitialPositionWoundEdge_Mikron(Double.POSITIVE_INFINITY);
		int width = (int)globalParameters.getNumber_of_initially_occupied_columns();
		int height = (int)globalParameters.getNumber_of_rows();
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				UniversalCell cell = new UniversalCell(null, null);
				((HexagonBasedMechanicalModel) cell.getEpisimBioMechanicalModelObject()).setCellLocationInCellField(new Double2D(x, y));
				standardCellEnsemble.add(cell);
			}
		}	
		if(globalParameters.getAddSecretingCellColony())addSekretionCellColony(standardCellEnsemble);
		return standardCellEnsemble;
	}
	
	private void addSekretionCellColony(ArrayList<UniversalCell> standardCellEnsemble){
		HexagonBasedMechanicalModelGP globalParameters = (HexagonBasedMechanicalModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		int width = 6 < globalParameters.getNumber_of_columns() ? 6:(int)globalParameters.getNumber_of_columns();
		int height = 6 < globalParameters.getNumber_of_rows() ? 6:(int)globalParameters.getNumber_of_rows();
		int startX = (int)(globalParameters.getNumber_of_columns()-(globalParameters.getNumber_of_columns()*0.25));
		int startY = (int)((globalParameters.getNumber_of_rows()/2)-(height/2));
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
		return new EpisimPortrayal[0];
	}
	
	protected EpisimPortrayal[] getAdditionalPortrayalsCellBackground() {		
		return  new EpisimPortrayal[0];
	}

}
