package episimmcc.vertexbased;

import java.io.File;
import java.util.ArrayList;

import ec.util.MersenneTwisterFast;
import episiminterfaces.EpisimPortrayal;
import sim.app.episim.EpisimProperties;
import sim.app.episim.datamonitoring.GlobalStatistics;
import sim.app.episim.gui.EpisimGUIState;
import sim.app.episim.model.AbstractCell;
import sim.app.episim.model.UniversalCell;
import sim.app.episim.model.biomechanics.vertexbased2D.VertexBasedModel;
import sim.app.episim.model.biomechanics.vertexbased2D.VertexBasedModelGP;
import sim.app.episim.model.biomechanics.vertexbased2D.VertexBasedModelController;
import sim.app.episim.model.biomechanics.vertexbased2D.geom.CellPolygon;
import sim.app.episim.model.biomechanics.vertexbased2D.geom.CellPolygonNetworkBuilder;
import sim.app.episim.model.biomechanics.vertexbased2D.geom.Vertex;
import sim.app.episim.model.biomechanics.vertexbased2D.util.CellPolygonRegistry;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.controller.TissueController;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import sim.app.episim.persistence.SimulationStateData;
import sim.app.episim.util.CellInspector;
import sim.app.episim.visualization.twodim.ContinousCellPortrayal2D;
import sim.app.episim.visualization.twodim.ContinuousCellFieldPortrayal2D;
import sim.display.GUIState;
import sim.portrayal.Inspector;
import sim.portrayal.LocationWrapper;
import sim.portrayal.Portrayal;
import sim.util.Double2D;

public class VertexBasedMechModelInit extends BiomechanicalModelInitializer {
	
	private MersenneTwisterFast random;
	private VertexBasedModelGP globalParameters;
	public VertexBasedMechModelInit(){
		super();
		globalParameters = (VertexBasedModelGP)ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		TissueController.getInstance().getTissueBorder().setBasalPeriodInMikron(550);
		TissueController.getInstance().getTissueBorder().setStartXOfStandardMembraneInMikron(0);
		TissueController.getInstance().getTissueBorder().loadStandardMembrane();
		
		random = new MersenneTwisterFast(System.currentTimeMillis());
	}
	
	public VertexBasedMechModelInit(SimulationStateData simulationStateData){
		super(simulationStateData);
	}
	
	protected ArrayList<UniversalCell> buildStandardInitialCellEnsemble() {
		
		ArrayList<UniversalCell> standardCellEnsemble = new ArrayList<UniversalCell>();
   	CellPolygon[] polygons = CellPolygonNetworkBuilder.getStandardMembraneCellArray();
   	for(CellPolygon actCellPolygon : polygons){   		  		
   		UniversalCell stemCell = new UniversalCell(null, null, true);
   		VertexBasedModel mechModel = (VertexBasedModel)stemCell.getEpisimBioMechanicalModelObject();
   		mechModel.initializeWithCellPolygon(actCellPolygon);
   		Vertex cellCenter = actCellPolygon.getCellCenter();
			Double2D cellLoc = new Double2D(cellCenter.getDoubleX(), cellCenter.getDoubleY());
			mechModel.setCellLocationInCellField(cellLoc);
			standardCellEnsemble.add(stemCell);			
   	}
   	VertexBasedModelController.getInstance().bagHasChanged(null);
   	return standardCellEnsemble;
	}
	
	protected void initializeCellEnsembleBasedOnRandomAgeDistribution(ArrayList<UniversalCell> cellEnsemble){
		if( EpisimProperties.getProperty(EpisimProperties.MODEL_RANDOM_CELL_AGE_INIT) != null &&
				EpisimProperties.getProperty(EpisimProperties.MODEL_RANDOM_CELL_AGE_INIT).equals(EpisimProperties.ON)){
			for(UniversalCell cell : cellEnsemble){
				double age = cell.getEpisimCellBehavioralModelObject().getAge();
				VertexBasedModel model = (VertexBasedModel) cell.getEpisimBioMechanicalModelObject();
				CellPolygonNetworkBuilder.setCellPolygonSizeAccordingToAge(age, model.getCellPolygon());
			}
		}		
	}	
		
	protected EpisimPortrayal getCellPortrayal() {
			   
		ContinousCellPortrayal2D cellPortrayal = new ContinousCellPortrayal2D(java.awt.Color.lightGray);
		ContinuousCellFieldPortrayal2D continousPortrayal = new ContinuousCellFieldPortrayal2D();
		continousPortrayal.setPortrayalForClass(UniversalCell.class, cellPortrayal);
		continousPortrayal.setField(ModelController.getInstance().getBioMechanicalModelController().getCellField());
		
		return continousPortrayal;
   }

	protected EpisimPortrayal[] getAdditionalPortrayalsCellForeground() {		
		return new EpisimPortrayal[0];
	}
	protected EpisimPortrayal[] getAdditionalPortrayalsCellBackground() {		
		return new EpisimPortrayal[0];
	}
	
	
	
}
