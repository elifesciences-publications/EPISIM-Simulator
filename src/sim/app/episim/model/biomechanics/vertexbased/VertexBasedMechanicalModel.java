package sim.app.episim.model.biomechanics.vertexbased;

import java.awt.Polygon;
import java.io.File;

import sim.SimStateServer;
import sim.app.episim.AbstractCell;
import sim.app.episim.model.biomechanics.AbstractMechanicalModel;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import sim.app.episim.model.initialization.VertexBasedMechanicalModelInitializer;
import sim.app.episim.util.GenericBag;
import sim.portrayal.DrawInfo2D;
import sim.util.Double2D;
import episimbiomechanics.EpisimModelConnector;
import episiminterfaces.monitoring.CannotBeMonitored;


public class VertexBasedMechanicalModel extends AbstractMechanicalModel{
	
	private CellPolygon cellPolygon;
	
	public VertexBasedMechanicalModel(AbstractCell cell){
		super(cell);
		cellPolygon = CellPolygonRegistry.getNewCellPolygon(cell.getMotherId(), cell.getActSimState().schedule.getSteps());		
	}
	
	

	public void setEpisimModelConnector(EpisimModelConnector modelConnector) {

	   // TODO Auto-generated method stub
	   
   }

	public GenericBag<AbstractCell> getRealNeighbours() {

	   // TODO Auto-generated method stub
	   return null;
   }

	public Double2D getNewPosition() {

	   // TODO Auto-generated method stub
	   return null;
   }

	public Double2D getOldPosition() {

	   // TODO Auto-generated method stub
	   return null;
   }
	@CannotBeMonitored
	public Polygon getPolygonCell() {	   
		return getPolygonCell(null);
   }
	
	@CannotBeMonitored
	public Polygon getPolygonCell(DrawInfo2D info) {

	   // TODO Auto-generated method stub
	   return null;
   }
	@CannotBeMonitored
	public Polygon getPolygonNucleus(){		
	   return getPolygonNucleus(null);
   }
	@CannotBeMonitored
	public Polygon getPolygonNucleus(DrawInfo2D info) {

	   // TODO Auto-generated method stub
	   return null;
   }

	public boolean nextToOuterCell() {

	   // TODO Auto-generated method stub
	   return false;
   }

	public boolean isMembraneCell() {

	   // TODO Auto-generated method stub
	   return false;
   }

	public GenericBag<AbstractCell> getNeighbouringCells() {

	   // TODO Auto-generated method stub
	   return null;
   }

	public void newSimStep(long simStepNumber) {

	   // TODO Auto-generated method stub
	   
   }
	
	@CannotBeMonitored
	public double getX() {
	   // TODO Auto-generated method stub
	   return 0;
   }
	
	@CannotBeMonitored
	public double getY() {
	   // TODO Auto-generated method stub
	   return 0;
   }
	
	@CannotBeMonitored
	public double getZ() { return 0; }
	
	public CellPolygon getCellPolygon(){
		return cellPolygon;
	}
		
	public BiomechanicalModelInitializer getBiomechanicalModelInitializer(){
		return new VertexBasedMechanicalModelInitializer();
	}
	
   public BiomechanicalModelInitializer getBiomechanicalModelInitializer(File modelInitializationFile) {	  
	   return new VertexBasedMechanicalModelInitializer(modelInitializationFile);
   }

}
