package sim.app.episim.model.biomechanics.hexagonbased;

import java.awt.Color;
import java.awt.Polygon;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.ArrayList;

import ec.util.MersenneTwisterFast;
import episimbiomechanics.EpisimModelConnector;


import episimbiomechanics.hexagonbased.EpisimHexagonBasedModelConnector;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.monitoring.CannotBeMonitored;
import sim.app.episim.AbstractCell;
import sim.app.episim.model.biomechanics.AbstractMechanicalModel;
import sim.app.episim.model.biomechanics.centerbased.CenterBasedMechanicalModel;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import sim.app.episim.model.initialization.HexagonBasedMechanicalModelInitializer;
import sim.app.episim.model.visualization.CellEllipse;
import sim.app.episim.util.GenericBag;
import sim.field.continuous.Continuous2D;
import sim.field.grid.ObjectGrid2D;
import sim.portrayal.DrawInfo2D;
import sim.util.Bag;
import sim.util.Double2D;
import sim.util.Int2D;
import sim.util.IntBag;


public class HexagonBasedMechanicalModel extends AbstractMechanicalModel {
	
	private EpisimHexagonBasedModelConnector modelConnector;
	
	private static ObjectGrid2D cellField;
	
	private Int2D fieldLocation = null;
	
	
	private Int2D spreadingLocation = null;
	
	
	private final int MAX_NEIGHBOUR_NUMBER = 6;
	
	private boolean isSpreading = false;
	
	private static MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
	
	private CellEllipse cellEllipse;
	
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
	   if(cell != null && getCellEllipse() == null && cell.getEpisimCellBehavioralModelObject() != null){
			cellEllipse = new CellEllipse(cell.getID(), (int)getX(), (int)getY(), 1, 1, Color.BLUE);    
		}
   }

	 public void setEpisimModelConnector(EpisimModelConnector modelConnector){
	   	if(modelConnector instanceof EpisimHexagonBasedModelConnector){
	   		this.modelConnector = (EpisimHexagonBasedModelConnector) modelConnector;
	   	}
	   	else throw new IllegalArgumentException("Episim Model Connector must be of type: EpisimHexagonBasedModelConnector");
	   }

	public GenericBag<AbstractCell> getRealNeighbours() {
		IntBag xPos = new IntBag();
		IntBag yPos = new IntBag();
		Bag neighbouringCellsBag = new Bag();
	   cellField.getNeighborsHexagonalDistance(fieldLocation.x, fieldLocation.y, 1, true, neighbouringCellsBag, xPos, yPos);
		GenericBag<AbstractCell> neighbouringCells = new GenericBag<AbstractCell>();
	   for(Object obj : neighbouringCellsBag.objs){
			if(obj != null && obj instanceof AbstractCell && obj != this.getCell()){				
				neighbouringCells.add((AbstractCell)obj);
			}
		}
	  
		return neighbouringCells;
	}

	@CannotBeMonitored
	public CellEllipse getCellEllipse(){
		return this.cellEllipse;
	}
	
	
	
	public boolean isMembraneCell() {

		return false;
	}

	public void newSimStep(long simStepNumber) {
		if(modelConnector.getIsSpreading() && getRealNeighbours().size() < MAX_NEIGHBOUR_NUMBER){
			if(spreadingLocation == null) spread();
			this.isSpreading = true;
		}
		else{
			modelConnector.setIsSpreading(false);
		}

	}
	
	public boolean isSpreading(){ return this.isSpreading; }
	
	public double getX() {
		return fieldLocation != null ? fieldLocation.x : -1;
	}
	
	public double getY() {
		return fieldLocation != null ? fieldLocation.y : -1;
	}

	public double getZ() {
		return 0;
	}

	protected void clearCellField() {
	   
	   	cellField.clear();
	   
   }
   public void removeCellFromCellField() {
   	cellField.field[fieldLocation.x][fieldLocation.y] = null;
   }
	
   /*
    * Be Careful with this method, existing cells at the location will be overwritten...
    */
   @CannotBeMonitored
   public void setCellLocationInCellField(Double2D location){
   	if(fieldLocation != null) removeCellFromCellField();
   	fieldLocation = new Int2D(cellField.tx((int)location.x), cellField.ty((int)location.y));
   	
   	cellField.field[fieldLocation.x][fieldLocation.y] = getCell();
   }
   @CannotBeMonitored
   public Double2D getCellLocationInCellField() {	   
	   return new Double2D(this.fieldLocation.x, this.fieldLocation.y);
   }
   @CannotBeMonitored
   protected Object getCellField() {	  
	   return cellField;
   }
   
   protected void setReloadedCellField(Object cellField) {
   	if(cellField instanceof ObjectGrid2D){
   		HexagonBasedMechanicalModel.cellField = (ObjectGrid2D) cellField;
   	}
   }
   
   private void spread(){
   	IntBag xPos = new IntBag();
		IntBag yPos = new IntBag();
		Bag neighbouringCellsBag = new Bag();
	   cellField.getNeighborsHexagonalDistance(fieldLocation.x, fieldLocation.y, 1, true, neighbouringCellsBag, xPos, yPos);
	   ArrayList<Integer> spreadingLocationIndices = new ArrayList<Integer>();
	   for(int i = 0; i < neighbouringCellsBag.size(); i++){
	   	if(neighbouringCellsBag.get(i)== null) spreadingLocationIndices.add(i);	   		   
	   }
	   int spreadingLocationIndex = spreadingLocationIndices.get(random.nextInt(spreadingLocationIndices.size()));
	   this.spreadingLocation = new Int2D(xPos.get(spreadingLocationIndex), yPos.get(spreadingLocationIndex));
	   cellField.set(this.spreadingLocation.x, this.spreadingLocation.y, getCell());
   }
   
   public Int2D getSpreadingLocation(){
   	return this.spreadingLocation;
   }
   
   private void calculateCellEllipse(long simstepNumber){
   	DrawInfo2D info = this.getCellEllipse().getLastDrawInfo2D();
		DrawInfo2D newInfo = null;
		if( info != null){
			newInfo = new DrawInfo2D(new Rectangle2D.Double(info.draw.x, info.draw.y, info.draw.width,info.draw.height), info.clip);
		//	newInfo.draw.x = ((newInfo.draw.x - newInfo.draw.width*getOldPosition().x) + newInfo.draw.width* getNewPosition().x);
		//	newInfo.draw.y = ((newInfo.draw.y - newInfo.draw.height*getOldPosition().y) + newInfo.draw.height*getNewPosition().y);
			this.getCellEllipse().setLastDrawInfo2D(newInfo, true);
		}  	   
  	   
  	  
   }
   
   
   //--------------------------------------------------------------------------------------------------------------------------------------------------------------
   // NOT YET NEEDED METHODS
   //--------------------------------------------------------------------------------------------------------------------------------------------------------------
   @CannotBeMonitored
   public Polygon getPolygonCell() {
		//not yet needed
		return new Polygon();
	}
   @CannotBeMonitored
	public Polygon getPolygonCell(DrawInfo2D info) {
		//not yet needed
		return new Polygon();
	}
   @CannotBeMonitored
	public Polygon getPolygonNucleus() {
		//not yet needed
		return new Polygon();
	}
   @CannotBeMonitored
	public Polygon getPolygonNucleus(DrawInfo2D info) {
		//not yet needed
		return new Polygon();
	}
}
