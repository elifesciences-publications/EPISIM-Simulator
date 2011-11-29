package sim.app.episim.model.initialization;

import java.io.File;
import java.util.ArrayList;

import ec.util.MersenneTwisterFast;
import episiminterfaces.EpisimPortrayal;

import sim.app.episim.AbstractCell;
import sim.app.episim.CellInspector;
import sim.app.episim.UniversalCell;
import sim.app.episim.datamonitoring.GlobalStatistics;
import sim.app.episim.gui.EpisimGUIState;
import sim.app.episim.model.biomechanics.vertexbased.VertexBasedMechanicalModel;
import sim.app.episim.model.biomechanics.vertexbased.VertexBasedModelController;
import sim.app.episim.model.biomechanics.vertexbased.geom.CellPolygon;
import sim.app.episim.model.biomechanics.vertexbased.geom.CellPolygonNetworkBuilder;
import sim.app.episim.model.biomechanics.vertexbased.geom.Vertex;
import sim.app.episim.model.biomechanics.vertexbased.util.CellPolygonRegistry;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.visualization.UniversalCellPortrayal2D;
import sim.app.episim.persistence.SimulationStateData;
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
		TissueController.getInstance().getTissueBorder().setBasalPeriodInMikron(550);
		TissueController.getInstance().getTissueBorder().setStartXOfStandardMembraneInMikron(0);
		TissueController.getInstance().getTissueBorder().setUndulationBaseLineInMikron(190);
		TissueController.getInstance().getTissueBorder().loadStandardMembrane();
		
		random = new MersenneTwisterFast(System.currentTimeMillis());
	}
	
	public VertexBasedMechanicalModelInitializer(SimulationStateData simulationStateData){
		super(simulationStateData);
	}
	
	protected ArrayList<UniversalCell> buildStandardInitialCellEnsemble() {
		
		ArrayList<UniversalCell> standardCellEnsemble = new ArrayList<UniversalCell>();
   	CellPolygon[] polygons = CellPolygonNetworkBuilder.getStandardMembraneCellArray();
   	for(CellPolygon actCellPolygon : polygons){   		  		
   		UniversalCell stemCell = new UniversalCell(null, null);
   		VertexBasedMechanicalModel mechModel = (VertexBasedMechanicalModel)stemCell.getEpisimBioMechanicalModelObject();
   		mechModel.initializeWithCellPolygon(actCellPolygon);
   		Vertex cellCenter = actCellPolygon.getCellCenter();
			Double2D cellLoc = new Double2D(cellCenter.getDoubleX(), cellCenter.getDoubleY());
			mechModel.setCellLocationInCellField(cellLoc);
			standardCellEnsemble.add(stemCell);			
			GlobalStatistics.getInstance().inkrementActualNumberStemCells();
			GlobalStatistics.getInstance().inkrementActualNumberKCytes();
   	}
   	VertexBasedModelController.getInstance().bagHasChanged(null);
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
	
	protected EpisimPortrayal getCellPortrayal() {
			   
	UniversalCellPortrayal2D portrayal = new UniversalCellPortrayal2D(java.awt.Color.lightGray);
		
		return portrayal;
   }

	protected EpisimPortrayal[] getAdditionalPortrayalsCellForeground() {		
		return new EpisimPortrayal[0];
	}
	protected EpisimPortrayal[] getAdditionalPortrayalsCellBackground() {		
		return new EpisimPortrayal[0];
	}
	
	
	
}
