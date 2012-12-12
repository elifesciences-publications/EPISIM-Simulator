package episimbiomechanics.hexagonbased2d.singlesurface;

import java.awt.Color;
import java.util.ArrayList;

import sim.app.episim.CellInspector;
import sim.app.episim.UniversalCell;
import sim.app.episim.model.biomechanics.hexagonbased.singlesurface.HexagonBasedMechanicalModelSingleSurface;
import sim.app.episim.model.biomechanics.hexagonbased.singlesurface.HexagonBasedMechanicalModelSingleSurfaceGP;
import sim.app.episim.model.biomechanics.hexagonbased3d.HexagonBased3DMechanicalModel;
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
import sim.util.Double3D;
import episiminterfaces.EpisimCellType;
import episiminterfaces.EpisimPortrayal;


public class HexagonBased2DMechModelSingleSurfaceInit extends BiomechanicalModelInitializer {
	
	public HexagonBased2DMechModelSingleSurfaceInit(){
		super();
		TissueController.getInstance().getTissueBorder().loadNoMembrane();
		MiscalleneousGlobalParameters.getInstance().setTypeColor(4);
	}
	
	public HexagonBased2DMechModelSingleSurfaceInit(SimulationStateData simulationStateData){
		super(simulationStateData);		
	}

	
	protected ArrayList<UniversalCell> buildStandardInitialCellEnsemble() {
		ArrayList<UniversalCell> standardCellEnsemble = new ArrayList<UniversalCell>();
		HexagonBasedMechanicalModelSingleSurfaceGP globalParameters = (HexagonBasedMechanicalModelSingleSurfaceGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		int width = (int)globalParameters.getNumber_of_columns();
		int height = (int)globalParameters.getNumber_of_rows();
		EpisimCellType[] cellTypes =ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getAvailableCellTypes();
		
		
		int delta = (int)globalParameters.getNumber_of_initially_occupied_columns()/2;
		for(int y = 0; y < delta; y++){
			for(int x = ((width/2)-delta); x < ((width/2)+delta); x++){			
				UniversalCell cell = new UniversalCell(null, null, true);
				((HexagonBasedMechanicalModelSingleSurface) cell.getEpisimBioMechanicalModelObject()).setCellLocationInCellField(new Double2D(x, y));
				if(cellTypes.length >1) cell.getEpisimCellBehavioralModelObject().setCellType(cellTypes[1]);
				standardCellEnsemble.add(cell);
			}
		}		
		for(int y = height-1; y > ((height-1)-delta); y--){
			for(int x = ((width/2)-delta); x < ((width/2)+delta); x++){	
				UniversalCell cell = new UniversalCell(null, null, true);
				((HexagonBasedMechanicalModelSingleSurface) cell.getEpisimBioMechanicalModelObject()).setCellLocationInCellField(new Double2D(x, y));
				if(cellTypes.length >1) cell.getEpisimCellBehavioralModelObject().setCellType(cellTypes[1]);
				standardCellEnsemble.add(cell);
			}
		}	
		
		if(globalParameters.getAddSecretingCellColony())addSekretionCellColony(standardCellEnsemble);
		return standardCellEnsemble;
	}
	
	private void addSekretionCellColony(ArrayList<UniversalCell> standardCellEnsemble){
		HexagonBasedMechanicalModelSingleSurfaceGP globalParameters = (HexagonBasedMechanicalModelSingleSurfaceGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		int width = (int) (globalParameters.getNumber_of_columns());
		int height = (int) globalParameters.getNumber_of_rows();
		//globalParameters.setNumber_of_initially_occupied_columns(globalParameters.getNumber_of_columns()/4);
		EpisimCellType[] cellTypes =ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getAvailableCellTypes();
		int delta = (int)globalParameters.getNumber_of_initially_occupied_columns()/4;
		delta = delta==0 ? 1 : delta;		
		for(int y = ((height/2)-delta);y < ((height/2)+delta); y++){			
			for(int x = ((width/2)-delta); x < ((width/2)+delta); x++){
				UniversalCell cell = new UniversalCell(null, null, true);
				((HexagonBasedMechanicalModelSingleSurface) cell.getEpisimBioMechanicalModelObject()).setCellLocationInCellField(new Double2D(x, y));
				if(cellTypes.length >1) cell.getEpisimCellBehavioralModelObject().setCellType(cellTypes[2]);
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
