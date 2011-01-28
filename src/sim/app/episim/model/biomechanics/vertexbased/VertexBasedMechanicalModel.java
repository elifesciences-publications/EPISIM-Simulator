package sim.app.episim.model.biomechanics.vertexbased;

import java.awt.Polygon;
import java.io.File;

import sim.app.episim.AbstractCell;
import sim.app.episim.model.biomechanics.AbstractMechanicalModel;
import sim.app.episim.model.initialization.AbstractBiomechanicalModelInitializer;
import sim.app.episim.model.initialization.VertexBasedMechanicalModelInitializer;
import sim.app.episim.util.GenericBag;
import sim.portrayal.DrawInfo2D;
import sim.util.Double2D;
import episimbiomechanics.EpisimModelConnector;


public class VertexBasedMechanicalModel extends AbstractMechanicalModel{
	
	
	public VertexBasedMechanicalModel(AbstractCell cell){
		super(cell);
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

	public Polygon getPolygonCell() {

	   // TODO Auto-generated method stub
	   return null;
   }

	public Polygon getPolygonCell(DrawInfo2D info) {

	   // TODO Auto-generated method stub
	   return null;
   }

	public Polygon getPolygonNucleus() {

	   // TODO Auto-generated method stub
	   return null;
   }

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

	public double getX() {

	   // TODO Auto-generated method stub
	   return 0;
   }

	public double getY() {

	   // TODO Auto-generated method stub
	   return 0;
   }

	public double getZ() {

	   // TODO Auto-generated method stub
	   return 0;
   }
	
	public AbstractBiomechanicalModelInitializer getBiomechanicalModelInitializer(){
		return new VertexBasedMechanicalModelInitializer();
	}
	
   public AbstractBiomechanicalModelInitializer getBiomechanicalModelInitializer(File modelInitializationFile) {	  
	   return new VertexBasedMechanicalModelInitializer(modelInitializationFile);
   }

}
