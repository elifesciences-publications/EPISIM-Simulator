package sim.app.episim.model.biomechanics.hexagonbased;

import java.awt.Polygon;
import java.io.File;

import episimbiomechanics.EpisimModelConnector;


import episimbiomechanics.hexagonbased.EpisimHexagonBasedModelConnector;
import sim.app.episim.AbstractCell;
import sim.app.episim.model.biomechanics.AbstractMechanicalModel;
import sim.app.episim.model.biomechanics.centerbased.CenterBasedMechanicalModel;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import sim.app.episim.model.initialization.HexagonBasedMechanicalModelInitializer;
import sim.app.episim.util.GenericBag;
import sim.field.continuous.Continuous2D;
import sim.field.grid.ObjectGrid2D;
import sim.portrayal.DrawInfo2D;
import sim.util.Double2D;


public class HexagonBasedMechanicalModel extends AbstractMechanicalModel {
	
	private EpisimHexagonBasedModelConnector modelConnector;
	
	private static ObjectGrid2D cellField;
	
	public HexagonBasedMechanicalModel(){
		this(null);	
	}

	public HexagonBasedMechanicalModel(AbstractCell cell) {
	   super(cell);
	   if(cellField == null){
	   	cellField = new ObjectGrid2D(100, 100);
	   }
   }

	 public void setEpisimModelConnector(EpisimModelConnector modelConnector){
	   	if(modelConnector instanceof EpisimHexagonBasedModelConnector){
	   		this.modelConnector = (EpisimHexagonBasedModelConnector) modelConnector;
	   	}
	   	else throw new IllegalArgumentException("Episim Model Connector must be of type: EpisimHexagonBasedModelConnector");
	   }

	public GenericBag<AbstractCell> getRealNeighbours() {

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

	protected void clearCellField() {
	   
	   	cellField.clear();
	   
   }
   public void removeCellFromCellField() {
   	cellField.getNeighborsMaxDistance(0, 0, 0, isMembraneCell(), null, null)
	   cellField.remove(this.getCell());
   }
	
   public void setCellLocationInCellField(Double2D location){
	   cellField.setObjectLocation(this.getCell(), location);
   }
	
   public Double2D getCellLocationInCellField() {	   
	   return cellField.getObjectLocation(getCell());
   }

   protected Object getCellField() {	  
	   return cellField;
   }
   
   protected void setReloadedCellField(Object cellField) {
   	if(cellField instanceof Continuous2D){
   		CenterBasedMechanicalModel.cellField = (Continuous2D) cellField;
   	}
   }

}
