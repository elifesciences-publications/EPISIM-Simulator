package sim.app.episim.model.initialization;

import java.io.File;
import java.util.ArrayList;

import sim.app.episim.CellInspector;
import sim.app.episim.UniversalCell;
import sim.app.episim.gui.EpidermisGUIState;
import sim.app.episim.model.visualization.HexagonalCellGridPortrayal2D;
import sim.app.episim.model.visualization.UniversalCellPortrayal2D;
import sim.app.episim.tissue.TissueController;
import sim.display.GUIState;
import sim.portrayal.Inspector;
import sim.portrayal.LocationWrapper;
import sim.portrayal.Portrayal;


public class HexagonBasedMechanicalModelInitializer extends BiomechanicalModelInitializer {
	
	public HexagonBasedMechanicalModelInitializer(){
		super();
		TissueController.getInstance().getTissueBorder().loadNoMembrane();
	}
	public HexagonBasedMechanicalModelInitializer(File modelInitializationFile){
		super(modelInitializationFile);
		
	}

	@Override
	protected ArrayList<UniversalCell> buildStandardInitialCellEnsemble() {

		
		return new ArrayList<UniversalCell>();
	}

	
	protected void initializeCellEnsembleBasedOnRandomAgeDistribution(ArrayList<UniversalCell> cellEnsemble) {

		// is not needed here
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
	   
		HexagonalCellGridPortrayal2D portrayal =  new HexagonalCellGridPortrayal2D(){

			public Inspector getInspector(LocationWrapper wrapper, GUIState state) {
			// make the inspector
				return new CellInspector(super.getInspector(wrapper, state), wrapper, state);
			}
		};		
		return portrayal;
   }

}
