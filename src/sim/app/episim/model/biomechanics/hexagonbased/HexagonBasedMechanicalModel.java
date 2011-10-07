package sim.app.episim.model.biomechanics.hexagonbased;

import java.awt.Color;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ec.util.MersenneTwisterFast;
import episimbiomechanics.EpisimModelConnector;


import episimbiomechanics.hexagonbased.EpisimHexagonBasedModelConnector;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.EpisimDifferentiationLevel;
import episiminterfaces.monitoring.CannotBeMonitored;
import sim.app.episim.AbstractCell;
import sim.app.episim.model.biomechanics.AbstractMechanicalModel;
import sim.app.episim.model.biomechanics.centerbased.CenterBasedMechanicalModel;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import sim.app.episim.model.initialization.HexagonBasedMechanicalModelInitializer;
import sim.app.episim.model.visualization.CellEllipse;
import sim.app.episim.tissue.TissueController;
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
	
	private static MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
	
	private CellEllipse cellEllipse;
	
	private DrawInfo2D lastDrawInfo2D;
	
	private static final boolean IS_TOROIDAL = false;
	
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
	   if(cell!= null){
		   AbstractCell motherCell = cell.getMotherCell();
		   
		   if(motherCell != null && motherCell.getID() != cell.getID()){
		   	HexagonBasedMechanicalModel motherCellMechModel = (HexagonBasedMechanicalModel) motherCell.getEpisimBioMechanicalModelObject();
		   	if(motherCellMechModel.spreadingLocation != null){
		   		cellField.field[motherCellMechModel.spreadingLocation.x][motherCellMechModel.spreadingLocation.y] = cell;
		   		fieldLocation = new Int2D(motherCellMechModel.spreadingLocation.x,motherCellMechModel.spreadingLocation.y);
		   		motherCellMechModel.spreadingLocation = null;
		   		motherCellMechModel.modelConnector.setIsSpreading(false);
		   	}		   	
		   }	   
	   }
	   
	   if(cell != null && getCellEllipse() == null && cell.getEpisimCellBehavioralModelObject() != null){
			cellEllipse = new CellEllipse(cell.getID(), (int)getX(), (int)getY(), 1, 1, Color.BLUE);    
		}
	   lastDrawInfo2D = new DrawInfo2D(new Rectangle2D.Double(0, 0, 0, 0),
		 new Rectangle2D.Double(0, 0, 0, 0));
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
	   cellField.getNeighborsHexagonalDistance(fieldLocation.x, fieldLocation.y, 1, IS_TOROIDAL, neighbouringCellsBag, xPos, yPos);
		GenericBag<AbstractCell> neighbouringCells = new GenericBag<AbstractCell>();
		HashSet<Long> neighbouringCellIDs = new HashSet<Long>();
	   for(Object obj : neighbouringCellsBag.objs){
			if(obj != null && obj instanceof AbstractCell && obj != this.getCell()){	
				AbstractCell cell = (AbstractCell)obj;
				
				if(!neighbouringCellIDs.contains(cell.getID())){
					neighbouringCellIDs.add(cell.getID());
					neighbouringCells.add(cell);
				}
			}
		}
	   if(spreadingLocation!=null){
	   	xPos.clear();
	   	yPos.clear();
	   	neighbouringCellsBag.clear();
	   	cellField.getNeighborsHexagonalDistance(spreadingLocation.x, spreadingLocation.y, 1, IS_TOROIDAL, neighbouringCellsBag, xPos, yPos);
	   	for(Object obj : neighbouringCellsBag.objs){
				if(obj != null && obj instanceof AbstractCell && obj != this.getCell()){				
					AbstractCell cell = (AbstractCell)obj;
					if(!neighbouringCellIDs.contains(cell.getID())){
						neighbouringCellIDs.add(cell.getID());
						neighbouringCells.add(cell);
					}
				}
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
			
		if(modelConnector.getIsSpreading() && isSpreadingPossible()){
			if(spreadingLocation == null){ 
				spread();
			}
			
		}
		else if(modelConnector.getIsSpreading() && !isSpreadingPossible()){
			if(spreadingLocation==null)modelConnector.setIsSpreading(false);
		}
		
		if(modelConnector.getIsRetracting() && spreadingLocation!=null){
			modelConnector.setIsSpreading(false);
			modelConnector.setIsRetracting(false);
			cellField.field[fieldLocation.x][fieldLocation.y] = null;
			cellField.field[spreadingLocation.x][spreadingLocation.y] = getCell();
			fieldLocation = spreadingLocation;
			spreadingLocation = null;
		}
		if(modelConnector.getIsRetracting() && spreadingLocation==null){
			System.out.println("Retracting but no location");
		}
		if(modelConnector.getIsProliferating()){
			modelConnector.setIsSpreading(false);
			modelConnector.setIsProliferating(false);
		}		
		this.getCellEllipse().setLastDrawInfo2D(getCellEllipse().getLastDrawInfo2D(), true);
		modelConnector.setIsSpreadingPossible(isSpreadingPossible());
	}
	
	public boolean isSpreading(){ return this.spreadingLocation != null; }
	
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
   	if(spreadingLocation != null){
   		cellField.field[spreadingLocation.x][spreadingLocation.y] = null;
   	}
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
   
   public Int2D getCellFieldDimensions(){
   	return new Int2D(cellField.getWidth(), cellField.getHeight());
   }
   
   protected void setReloadedCellField(Object cellField) {
   	if(cellField instanceof ObjectGrid2D){
   		HexagonBasedMechanicalModel.cellField = (ObjectGrid2D) cellField;
   	}
   }
   
   private void spread(){
   	IntBag xPos = new IntBag();
		IntBag yPos = new IntBag();
	   ArrayList<Integer> spreadingLocationIndices = getPossibleSpreadingLocationIndices(xPos, yPos);
	 
	   if(!spreadingLocationIndices.isEmpty()){
		   int spreadingLocationIndex = spreadingLocationIndices.get(random.nextInt(spreadingLocationIndices.size()));
		   this.spreadingLocation = new Int2D(xPos.get(spreadingLocationIndex), yPos.get(spreadingLocationIndex));
		   cellField.set(this.spreadingLocation.x, this.spreadingLocation.y, getCell());
	   }
   }
   
   public Int2D getSpreadingLocation(){
   	return this.spreadingLocation;
   }
   
   private boolean isSpreadingPossible(){
   	return !getPossibleSpreadingLocationIndices(new IntBag(),new IntBag()).isEmpty();
   }
   private ArrayList<Integer> getPossibleSpreadingLocationIndices(IntBag xPos, IntBag yPos){
   	
		Bag neighbouringCellsBag = new Bag();
	   if(fieldLocation != null)cellField.getNeighborsHexagonalDistance(fieldLocation.x, fieldLocation.y, 1, IS_TOROIDAL, neighbouringCellsBag, xPos, yPos);
	   
	   ArrayList<Integer> spreadingLocationIndices = new ArrayList<Integer>();
	   for(int i = 0; i < neighbouringCellsBag.size(); i++){
	   	if(neighbouringCellsBag.get(i)== null) spreadingLocationIndices.add(i);	   		   
	   }
	   return spreadingLocationIndices;
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
   
   public DrawInfo2D getLastDrawInfo2D(){
   	return this.lastDrawInfo2D;
   }
   
   public void setLastDrawInfo2D(DrawInfo2D info){
   	this.lastDrawInfo2D = info;
   }

  
   
	protected void removeCellsInWoundArea(GeneralPath woundArea) {
		Iterator<AbstractCell> iter = TissueController.getInstance().getActEpidermalTissue().getAllCells().iterator();
		HashSet<AbstractCell> deathCellSet = new HashSet<AbstractCell>();	
			
			while(iter.hasNext()){
				AbstractCell cell = iter.next();
				if(cell.getEpisimBioMechanicalModelObject() instanceof HexagonBasedMechanicalModel){
					HexagonBasedMechanicalModel mechModel = (HexagonBasedMechanicalModel) cell.getEpisimBioMechanicalModelObject();
					if(woundArea.contains(mechModel.getLastDrawInfo2D().draw.x, mechModel.getLastDrawInfo2D().draw.y)){  
						deathCellSet.add(cell);
					}
					
				}
			}
			for(AbstractCell cell :deathCellSet) cell.killCell();
   }
}
