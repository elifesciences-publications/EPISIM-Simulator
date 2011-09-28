package sim.app.episim.model.biomechanics.hexagonbased;

import java.awt.Polygon;
import java.io.File;

import episimbiomechanics.EpisimModelConnector;


import episimbiomechanics.hexagonbased.EpisimHexagonBasedModelConnector;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import sim.app.episim.AbstractCell;
import sim.app.episim.model.biomechanics.AbstractMechanicalModel;
import sim.app.episim.model.biomechanics.centerbased.CenterBasedMechanicalModel;
import sim.app.episim.model.controller.ModelController;
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
	
	private int fieldPosX;
	private int fieldPosY;
	
	public HexagonBasedMechanicalModel(){
		this(null);	
	}

	public HexagonBasedMechanicalModel(AbstractCell cell) {
	   super(cell);
	   if(cellField == null){
	   	HexagonBasedMechanicalModelGlobalParameters globalParameters = (HexagonBasedMechanicalModelGlobalParameters)ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
	   	int width = (int)(globalParameters.getWidthInMikron() / globalParameters.getCellDiameter_mikron());
	   	int height = (int)(globalParameters.getHeightInMikron() / globalParameters.getCellDiameter_mikron());
	   	cellField = new ObjectGrid2D(width, height);
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
		return new GenericBag<AbstractCell>();
	}

	

	public Polygon getPolygonCell() {

		//not yet needed
		return new Polygon();
	}

	public Polygon getPolygonCell(DrawInfo2D info) {

		//not yet needed
		return new Polygon();
	}

	public Polygon getPolygonNucleus() {

		//not yet needed
		return new Polygon();
	}

	public Polygon getPolygonNucleus(DrawInfo2D info) {

		//not yet needed
		return new Polygon();
	}
	
	public boolean isMembraneCell() {

		return false;
	}

	public void newSimStep(long simStepNumber) {

		// TODO Auto-generated method stub

	}

	public double getX() {
		
		return fieldPosX;
	}

	public double getY() {		
		return fieldPosY;
	}

	public double getZ() {
		return 0;
	}

	protected void clearCellField() {
	   
	   	cellField.clear();
	   
   }
   public void removeCellFromCellField() {
   	cellField.field[fieldPosX][fieldPosY] = null;
   }
	
   /*
    * Be Careful with this method, existing cells at the location will be overwritten...
    */
   public void setCellLocationInCellField(Double2D location){
   	removeCellFromCellField();
   	cellField.field[cellField.tx((int)location.x)][cellField.ty((int)location.y)] = getCell();
   }
	
   public Double2D getCellLocationInCellField() {	   
	   return new Double2D((double) fieldPosX, (double) fieldPosY);
   }

   protected Object getCellField() {	  
	   return cellField;
   }
   
   protected void setReloadedCellField(Object cellField) {
   	if(cellField instanceof ObjectGrid2D){
   		HexagonBasedMechanicalModel.cellField = (ObjectGrid2D) cellField;
   	}
   }

}
