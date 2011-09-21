package sim.app.episim.model.initialization;

import java.io.File;
import java.util.ArrayList;

import ec.util.MersenneTwisterFast;

import sim.app.episim.AbstractCell;
import sim.app.episim.CellInspector;
import sim.app.episim.UniversalCell;
import sim.app.episim.datamonitoring.GlobalStatistics;
import sim.app.episim.gui.EpidermisGUIState;
import sim.app.episim.model.biomechanics.vertexbased.CellPolygon;
import sim.app.episim.model.biomechanics.vertexbased.CellPolygonNetworkBuilder;
import sim.app.episim.model.biomechanics.vertexbased.CellPolygonRegistry;
import sim.app.episim.model.biomechanics.vertexbased.Vertex;
import sim.app.episim.model.biomechanics.vertexbased.VertexBasedMechanicalModel;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.visualization.UniversalCellPortrayal2D;
import sim.app.episim.tissue.TissueController;
import sim.display.GUIState;
import sim.portrayal.Inspector;
import sim.portrayal.LocationWrapper;
import sim.portrayal.Portrayal;
import sim.util.Double2D;

public class VertexBasedMechanicalModelInitializer extends BiomechanicalModelInitializer {
	
	private MersenneTwisterFast random;
	
	public VertexBasedMechanicalModelInitializer(){
		super();
		TissueController.getInstance().getTissueBorder().setBasalPeriod(550);
		TissueController.getInstance().getTissueBorder().setStartXOfStandardMembrane(0);
		TissueController.getInstance().getTissueBorder().setUndulationBaseLine(190);
		TissueController.getInstance().getTissueBorder().loadStandardMembrane();
		TissueController.getInstance().getTissueBorder().setNumberOfPixelsPerMicrometer(2);
		random = new MersenneTwisterFast(System.currentTimeMillis());
	}
	
	public VertexBasedMechanicalModelInitializer(File modelInitializationFile){
		super(modelInitializationFile);
	}
	
	protected ArrayList<UniversalCell> buildStandardInitialCellEnsemble() {
		
		ArrayList<UniversalCell> standardCellEnsemble = new ArrayList<UniversalCell>();
   	CellPolygon[] polygons = CellPolygonNetworkBuilder.getStandardMembraneCellArray();
   	for(CellPolygon actCellPolygon : polygons){
   		long id  = AbstractCell.getNextCellId();
   		CellPolygonRegistry.registerNewCellPolygon(id, actCellPolygon);   		
   		UniversalCell stemCell = new UniversalCell(id,id, null, null);  		
   		Vertex cellCenter = actCellPolygon.getCellCenter();
			Double2D cellLoc = new Double2D(cellCenter.getDoubleX(), cellCenter.getDoubleY());
			TissueController.getInstance().getActEpidermalTissue().getCellContinous2D().setObjectLocation(stemCell, cellLoc);
			standardCellEnsemble.add(stemCell);			
			GlobalStatistics.getInstance().inkrementActualNumberStemCells();
			GlobalStatistics.getInstance().inkrementActualNumberKCytes();
   	}  	
   	return standardCellEnsemble;
	}
	
	protected void initializeCellEnsembleBasedOnRandomAgeDistribution(ArrayList<UniversalCell> cellEnsemble){
		
		for(UniversalCell cell : cellEnsemble){
			double age = cell.getEpisimCellBehavioralModelObject().getAge();
			VertexBasedMechanicalModel model = (VertexBasedMechanicalModel) cell.getEpisimBioMechanicalModelObject();
			CellPolygonNetworkBuilder.setCellPolygonSizeAccordingToAge(age, model.getCellPolygon());
		}
				
	}	
	
	//TODO: implement this Method as soon as an initialization file can be used
	protected ArrayList<UniversalCell> buildInitialCellEnsemble(File file) {
		return new ArrayList<UniversalCell>();
	}
	
	protected Portrayal getCellPortrayal() {
		double zoomFactorHeight = EpidermisGUIState.EPIDISPLAYSTANDARDHEIGHT / TissueController.getInstance().getTissueBorder().getHeight();
		double zoomFactorWidth = EpidermisGUIState.EPIDISPLAYSTANDARDWIDTH / TissueController.getInstance().getTissueBorder().getWidth();
		
		double initialZoomFactor = zoomFactorWidth < zoomFactorHeight ? zoomFactorWidth : zoomFactorHeight;
		
		double displayWidth = TissueController.getInstance().getTissueBorder().getWidth() * initialZoomFactor;
		double displayHeight = TissueController.getInstance().getTissueBorder().getHeight() * initialZoomFactor;
	   
	   return new UniversalCellPortrayal2D(java.awt.Color.lightGray, initialZoomFactor, 
	   		displayWidth + (2*EpidermisGUIState.DISPLAYBORDER), displayHeight + (2*EpidermisGUIState.DISPLAYBORDER), EpidermisGUIState.DISPLAYBORDER){

			public Inspector getInspector(LocationWrapper wrapper, GUIState state) {
			// make the inspector
				return new CellInspector(super.getInspector(wrapper, state), wrapper, state);
			}
		};
   }

	
	
	
	
}
