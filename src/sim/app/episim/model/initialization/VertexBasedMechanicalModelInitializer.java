package sim.app.episim.model.initialization;

import java.io.File;
import java.util.ArrayList;

import sim.app.episim.AbstractCell;
import sim.app.episim.UniversalCell;
import sim.app.episim.datamonitoring.GlobalStatistics;
import sim.app.episim.model.biomechanics.centerbased.CenterBasedMechanicalModel;
import sim.app.episim.model.biomechanics.centerbased.CenterBasedMechanicalModelGlobalParameters;
import sim.app.episim.model.biomechanics.vertexbased.CellPolygon;
import sim.app.episim.model.biomechanics.vertexbased.CellPolygonNetworkBuilder;
import sim.app.episim.model.biomechanics.vertexbased.CellPolygonRegistry;
import sim.app.episim.model.biomechanics.vertexbased.Vertex;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.tissue.TissueController;
import sim.engine.Schedule;
import sim.util.Double2D;


public class VertexBasedMechanicalModelInitializer extends BiomechanicalModelInitializer {
	
	public VertexBasedMechanicalModelInitializer(){
		super();
		TissueController.getInstance().getTissueBorder().setBasalPeriod(550);
		TissueController.getInstance().getTissueBorder().setStartXOfStandardMembrane(0);
		TissueController.getInstance().getTissueBorder().setUndulationBaseLine(190);
		TissueController.getInstance().getTissueBorder().loadStandardMembrane();
		TissueController.getInstance().getTissueBorder().setNumberOfPixelsPerMicrometer(2);
	}
	
	public VertexBasedMechanicalModelInitializer(File modelInitializationFile){
		super(modelInitializationFile);
	}
	
	protected ArrayList<UniversalCell> buildStandardInitialCellEnsemble() {
		
		ArrayList<UniversalCell> standardCellEnsemble = new ArrayList<UniversalCell>();
   	CellPolygon[] polygons = CellPolygonNetworkBuilder.getStandardMembraneCellArray();
   	for(CellPolygon actCellPolygon : polygons){
   		long id  = AbstractCell.getNextCellId();
   		CellPolygonRegistry.registerNewCellPolygon(id, 0, actCellPolygon);
   		
   		UniversalCell stemCell = new UniversalCell(id,id, null);		
   		Vertex cellCenter = actCellPolygon.getCellCenter();
			Double2D cellLoc = new Double2D(cellCenter.getDoubleX(), cellCenter.getDoubleY());
			TissueController.getInstance().getActEpidermalTissue().getCellContinous2D().setObjectLocation(stemCell, cellLoc);
			standardCellEnsemble.add(stemCell);			
			GlobalStatistics.getInstance().inkrementActualNumberStemCells();
			GlobalStatistics.getInstance().inkrementActualNumberKCytes();
   	}  	
   	return standardCellEnsemble;
	}

	//TODO: implement this Method as soon as an initialization file can be used
	protected ArrayList<UniversalCell> buildInitialCellEnsemble(File file) {
		return new ArrayList<UniversalCell>();
	}
}
