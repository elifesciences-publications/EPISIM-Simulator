package sim.app.episim.model.initialization;

import java.io.File;
import java.util.ArrayList;

import sim.app.episim.AbstractCell;
import sim.app.episim.CellInspector;
import sim.app.episim.UniversalCell;
import sim.app.episim.gui.EpidermisGUIState;
import sim.app.episim.model.biomechanics.hexagonbased.HexagonBasedMechanicalModel;
import sim.app.episim.model.biomechanics.hexagonbased.HexagonBasedMechanicalModelGlobalParameters;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.visualization.HexagonalCellGridPortrayal2D;
import sim.app.episim.model.visualization.UniversalCellPortrayal2D;
import sim.app.episim.tissue.TissueController;
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
	
	public HexagonBasedMechanicalModelInitializer(File modelInitializationFile){
		super(modelInitializationFile);		
	}

	
	protected ArrayList<UniversalCell> buildStandardInitialCellEnsemble() {
		ArrayList<UniversalCell> standardCellEnsemble = new ArrayList<UniversalCell>();
		HexagonBasedMechanicalModelGlobalParameters globalParameters = (HexagonBasedMechanicalModelGlobalParameters) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		
		
		
		int width = (int)((globalParameters.getWidthInMikron()/globalParameters.getCellDiameter_mikron())*0.2);
		int height = (int)((globalParameters.getHeightInMikron()/globalParameters.getCellDiameter_mikron()));
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				UniversalCell cell = new UniversalCell(AbstractCell.getNextCellId(),null, null, null);
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

	@Override
	protected ArrayList<UniversalCell> buildInitialCellEnsemble(File file) {

		// TODO Auto-generated method stub has to be implemented
		return new ArrayList<UniversalCell>();
	}

	protected Portrayal getCellPortrayal() {
		double zoomFactorHeight = EpidermisGUIState.EPIDISPLAYSTANDARDHEIGHT / TissueController.getInstance().getTissueBorder().getHeightInPixels();
		double zoomFactorWidth = EpidermisGUIState.EPIDISPLAYSTANDARDWIDTH / TissueController.getInstance().getTissueBorder().getWidthInPixels();
		
		double initialZoomFactor = zoomFactorWidth < zoomFactorHeight ? zoomFactorWidth : zoomFactorHeight;
		
		double displayWidth = TissueController.getInstance().getTissueBorder().getWidthInPixels() * initialZoomFactor;
		double displayHeight = TissueController.getInstance().getTissueBorder().getHeightInPixels() * initialZoomFactor;
	   
		HexagonalCellGridPortrayal2D portrayal =  new HexagonalCellGridPortrayal2D(java.awt.Color.lightGray, initialZoomFactor, 
	   		displayWidth + (2*EpidermisGUIState.DISPLAYBORDER), displayHeight + (2*EpidermisGUIState.DISPLAYBORDER), EpidermisGUIState.DISPLAYBORDER){
			public Inspector getInspector(LocationWrapper wrapper, GUIState state) {
			// make the inspector
				return new CellInspector(super.getInspector(wrapper, state), wrapper, state);
			}
		};		
		return portrayal;
   }

}
