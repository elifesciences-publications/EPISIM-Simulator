package sim.app.episim.model.biomechanics.hexagonbased;

import java.awt.Color;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
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
import sim.app.episim.model.diffusion.ExtraCellularDiffusionField;
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
import sim.util.MutableInt2D;


public class HexagonBasedMechanicalModel extends AbstractMechanicalModel {
	
	private EpisimHexagonBasedModelConnector modelConnector;
	
	private static ObjectGrid2D cellField;
	
	private Int2D fieldLocation = null;	
	
	private Int2D spreadingLocation = null;
	
	private static MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
	
	private CellEllipse cellEllipse;
	
	private DrawInfo2D lastDrawInfo2D;
	
	private static final boolean IS_TOROIDAL = false;
	
	private boolean isAtWoundEdge=false;
	
	private HexagonBasedMechanicalModelGlobalParameters globalParameters;
	
	private static final int UPPER_PROBABILITY_LIMIT = (int) Math.pow(10, 7);
	
	public HexagonBasedMechanicalModel(){
		this(null);	
	}

	public HexagonBasedMechanicalModel(AbstractCell cell) {
	   super(cell);
	   globalParameters = (HexagonBasedMechanicalModelGlobalParameters)ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
	  
	   if(cellField == null){
	   	
	   	int width = (int)HexagonBasedMechanicalModelGlobalParameters.number_of_columns;
	   	int height = (int)HexagonBasedMechanicalModelGlobalParameters.number_of_rows;
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
		   		motherCellMechModel.modelConnector.setIsSpreadingRGD(false);
		   		motherCellMechModel.modelConnector.setIsSpreadingFN(false);
		   	}		   	
		   }	   
	   }
	   
	   if(cell != null && getCellEllipse() == null && cell.getEpisimCellBehavioralModelObject() != null){
			cellEllipse = new CellEllipse(cell.getID(), (int)getX(), (int)getY(), 1, 1, Color.BLUE);    
		}
	   lastDrawInfo2D = new DrawInfo2D(null, null, new Rectangle2D.Double(0, 0, 0, 0),
		 new Rectangle2D.Double(0, 0, 0, 0));
   }

	 public void setEpisimModelConnector(EpisimModelConnector modelConnector){
	   	if(modelConnector instanceof EpisimHexagonBasedModelConnector){
	   		this.modelConnector = (EpisimHexagonBasedModelConnector) modelConnector;
	   	}
	   	else throw new IllegalArgumentException("Episim Model Connector must be of type: EpisimHexagonBasedModelConnector");
	 }
	 public EpisimModelConnector getEpisimModelConnector(){
	   	return this.modelConnector;
	 }
	 
	 public GenericBag<AbstractCell> getRealNeighbours(){
		 return getRealNeighbours(globalParameters.getUseContinuousSpace());
	 }

	private GenericBag<AbstractCell> getRealNeighbours(boolean continuous) {
		
		IntBag xPos = new IntBag();
		IntBag yPos = new IntBag();
		Bag neighbouringCellsBag = new Bag();
	   cellField.getNeighborsHexagonalDistance(fieldLocation.x, fieldLocation.y, 1, continuous, neighbouringCellsBag, xPos, yPos);
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
	   	cellField.getNeighborsHexagonalDistance(spreadingLocation.x, spreadingLocation.y, 1, globalParameters.getUseContinuousSpace(), neighbouringCellsBag, xPos, yPos);
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
			
		if(modelConnector.getIsSpreadingFN() &&isSpreadingOnFNPossible()){
			if(spreadingLocation == null){ 
				spread(false);
			}
			
		}
		else if(modelConnector.getIsSpreadingFN() && !isSpreadingOnFNPossible()){
			if(spreadingLocation==null || isLocationOnTestSurface(spreadingLocation))modelConnector.setIsSpreadingFN(false);
		}
		
		if(modelConnector.getIsSpreadingRGD() && isSpreadingOnRGDPossible()){
			if(spreadingLocation == null){ 
				spread(true);
			}
			
		}
		else if(modelConnector.getIsSpreadingRGD() && !isSpreadingOnRGDPossible()){
			if(spreadingLocation==null || !isLocationOnTestSurface(spreadingLocation))modelConnector.setIsSpreadingRGD(false);
		}
		
		if(modelConnector.getIsRelaxing()){
			relax();
		}
		
		
		if((modelConnector.getIsRetractingToRGD() || modelConnector.getIsRetractingToFN()) && spreadingLocation!=null){
			 retract();
		}
		
		if(modelConnector.getIsProliferating()){
			modelConnector.setIsSpreadingRGD(false);
			modelConnector.setIsSpreadingFN(false);
			modelConnector.setIsProliferating(false);
		}		
		this.getCellEllipse().setLastDrawInfo2D(getCellEllipse().getLastDrawInfo2D(), true);		
		checkIfCellIsAtWoundEdge();
		modelConnector.setIsOnTestSurface(isLocationOnTestSurface(fieldLocation));
		modelConnector.setIsAtSurfaceBorder(isLocationAtSurfaceBorder(fieldLocation));
	}
	
	private int rectractingProbabilityBasedOnNeighbourhood(ArrayList<AbstractCell> neighbourToPull, Int2D locationToBeLeft, Int2D locationToBeKept){
		ArrayList<AbstractCell> neighboursToBeLost = new ArrayList<AbstractCell>();
		IntBag xPos = new IntBag();
		IntBag yPos = new IntBag();
		Bag neighbouringCellsBag = new Bag();
	   cellField.getNeighborsHexagonalDistance(locationToBeLeft.x, locationToBeLeft.y, 1, false, neighbouringCellsBag, xPos, yPos);
		HashSet<Long> neighbouringCellIDs = new HashSet<Long>();
	   for(Object obj : neighbouringCellsBag.objs){
			if(obj != null && obj instanceof AbstractCell && obj != this.getCell()){	
				AbstractCell cell = (AbstractCell)obj;				
				if(!neighbouringCellIDs.contains(cell.getID())){
					neighbouringCellIDs.add(cell.getID());
					neighboursToBeLost.add(cell);
				}
			}
		}
	   neighbouringCellsBag.clear();
	   xPos.clear();
	   yPos.clear();
	   cellField.getNeighborsHexagonalDistance(locationToBeKept.x, locationToBeKept.y, 1, false, neighbouringCellsBag, xPos, yPos);
	   for(Object obj : neighbouringCellsBag.objs){
			if(obj != null && obj instanceof AbstractCell && obj != this.getCell()){	
				AbstractCell cell = (AbstractCell)obj;				
				if(neighbouringCellIDs.contains(cell.getID())){
					neighbouringCellIDs.remove(cell.getID());
					neighboursToBeLost.remove(cell);
				}
			}
		}
		double numberOfNeighbours = neighboursToBeLost.size();
	
		ArrayList<AbstractCell> nonSpreadingNeighbours = new ArrayList<AbstractCell>();
		ArrayList<AbstractCell> spreadingNeighbours = new ArrayList<AbstractCell>();
		for(int i = 0; i < numberOfNeighbours; i++){
			if(((HexagonBasedMechanicalModel)neighboursToBeLost.get(i).getEpisimBioMechanicalModelObject()).isSpreading()) spreadingNeighbours.add(neighboursToBeLost.get(i));
			else nonSpreadingNeighbours.add(neighboursToBeLost.get(i));
		}		
		double factorAllNeighbours = Math.pow(Math.exp(-0.7d), numberOfNeighbours);
		double factorAllMinusOneNeighbour = Math.pow(Math.exp(-0.7d), (numberOfNeighbours-1));
		
		
		factorAllNeighbours *= (double)UPPER_PROBABILITY_LIMIT;
		factorAllMinusOneNeighbour *= (double)UPPER_PROBABILITY_LIMIT;		
	
		
		int factorAllNeighboursInt = (int)factorAllNeighbours;
		int factorAllMinusOneNeighbourInt = 0;
		if(!nonSpreadingNeighbours.isEmpty()){
			factorAllMinusOneNeighbourInt = (int)factorAllMinusOneNeighbour;
		}
		else return factorAllNeighboursInt;
		
		if(random.nextBoolean()){
			return factorAllNeighboursInt;
		}
		else{
			if(nonSpreadingNeighbours.size() > 1){
				neighbourToPull.add(nonSpreadingNeighbours.get(random.nextInt(nonSpreadingNeighbours.size())));
			}
			else{
				neighbourToPull.add(nonSpreadingNeighbours.get(0));
			}
			return factorAllMinusOneNeighbourInt;
		}			
	}
	
	
	
	private boolean isLocationOnTestSurface(Int2D location){
		return (getLocationInMikron(location).x > HexagonBasedMechanicalModelGlobalParameters.initialPositionWoundEdge_Mikron);
	}
	
	private boolean isLocationAtSurfaceBorder(Int2D location){
		double xPosInMikron = getLocationInMikron(location).x ;
		double intervalMin =  HexagonBasedMechanicalModelGlobalParameters.initialPositionWoundEdge_Mikron - globalParameters.getCellDiameter_mikron();
		double intervalMax =  HexagonBasedMechanicalModelGlobalParameters.initialPositionWoundEdge_Mikron + globalParameters.getCellDiameter_mikron();
		return (xPosInMikron >= intervalMin && xPosInMikron <= intervalMax);
	}
	
	private void checkIfCellIsAtWoundEdge(){
		double xPosInMikron = getLocationInMikron(fieldLocation).x;
		
		int minNeighbourNumber = 5;//isSpreading() ? 6 : 5;
		
		isAtWoundEdge = (getRealNeighbours(true).size() < minNeighbourNumber && 
				(xPosInMikron >= HexagonBasedMechanicalModelGlobalParameters.initialPositionWoundEdge_Mikron || 
						(xPosInMikron + (1*globalParameters.getCellDiameter_mikron()))>= HexagonBasedMechanicalModelGlobalParameters.initialPositionWoundEdge_Mikron));
	}
	
	private Double2D getLocationInMikron(Int2D location){
		double x=-1, y =-1;
		
		if(location !=null){
			double locX = (double) location.x;
			double locY = (double) location.y;
			x = HexagonBasedMechanicalModelGlobalParameters.outer_hexagonal_radius + (locX)*(1.5d*HexagonBasedMechanicalModelGlobalParameters.outer_hexagonal_radius);
			
			if((location.x%2) ==0){
				y = HexagonBasedMechanicalModelGlobalParameters.inner_hexagonal_radius+ (locY)*(2d*HexagonBasedMechanicalModelGlobalParameters.inner_hexagonal_radius);
			}
			else{
				y = (locY+1d)*(2*HexagonBasedMechanicalModelGlobalParameters.inner_hexagonal_radius);
			}
		}
		return new Double2D(x, y);
	}
	
	public Double2D getLocationInMikron(){
		return getLocationInMikron(fieldLocation);
	}
	
	public Double2D getSpreadingLocationInMikron(){
		return getLocationInMikron(spreadingLocation);
	}
	
	private void spread(boolean onTestSurface){
   	IntBag xPos = new IntBag();
		IntBag yPos = new IntBag();
	   ArrayList<Integer> spreadingLocationIndices = getPossibleSpreadingLocationIndices(xPos, yPos, onTestSurface);
	 
	   if(!spreadingLocationIndices.isEmpty()){
		   int spreadingLocationIndex = getRandomSpreadingLocationIndex(spreadingLocationIndices);
		   this.spreadingLocation = new Int2D(xPos.get(spreadingLocationIndex), yPos.get(spreadingLocationIndex));
		   cellField.set(this.spreadingLocation.x, this.spreadingLocation.y, getCell());
	   }
   }
	
	private int getRandomSpreadingLocationIndex(ArrayList<Integer> spreadingLocationIndices){
		if(globalParameters.isChemotaxisEnabled()){
			String chemotacticFieldName = modelConnector.getChemotacticField();
			if(chemotacticFieldName != null && !chemotacticFieldName.trim().isEmpty()){
				ExtraCellularDiffusionField ecDiffField =  ModelController.getInstance().getExtraCellularDiffusionController().getExtraCellularDiffusionField(chemotacticFieldName);
				if(ecDiffField != null){
					//ecDiffField.getTotalLocalFreeFieldConcentration(getCellBoundariesInMikron(), null, null)
				}
			}
		}
		return spreadingLocationIndices.get(random.nextInt(spreadingLocationIndices.size()));
	}
	
	private void relax(){
		modelConnector.setIsRelaxing(false);
		if(spreadingLocation != null){
			 cellField.set(this.spreadingLocation.x, this.spreadingLocation.y, null);
		}
		spreadingLocation = null;
		modelConnector.setIsSpreadingFN(false);
		modelConnector.setIsSpreadingRGD(false);
	}
	
	private void retract(){
		
		 
		boolean retraction = false;
		
	
		int randomNumber = random.nextInt(UPPER_PROBABILITY_LIMIT);
		if((modelConnector.getIsRetractingToRGD()&& isLocationOnTestSurface(fieldLocation) && isLocationOnTestSurface(spreadingLocation))
			||(modelConnector.getIsRetractingToFN()&& !isLocationOnTestSurface(fieldLocation) && !isLocationOnTestSurface(spreadingLocation))){
			
			ArrayList<AbstractCell> neighbourToPullA = new ArrayList<AbstractCell>();
			ArrayList<AbstractCell> neighbourToPullB = new ArrayList<AbstractCell>();
			
			int probabilityA = 0;
			int probabilityB = 0;
			if(globalParameters.getUseCellCellInteractionEnergy()){
				probabilityA = rectractingProbabilityBasedOnNeighbourhood(neighbourToPullA, fieldLocation, spreadingLocation);
				probabilityB = rectractingProbabilityBasedOnNeighbourhood(neighbourToPullB, spreadingLocation, fieldLocation);
			}
			else{
				probabilityA = UPPER_PROBABILITY_LIMIT/2;
				probabilityB = UPPER_PROBABILITY_LIMIT/2;
			}
			
			if((probabilityA + probabilityB) > UPPER_PROBABILITY_LIMIT){
				 int sum = probabilityA + probabilityB;
				 sum /=UPPER_PROBABILITY_LIMIT;
				 probabilityA /= sum;
				 probabilityB /= sum;
			}				
			if(randomNumber < probabilityA){
					cellField.field[fieldLocation.x][fieldLocation.y] = null;
					cellField.field[spreadingLocation.x][spreadingLocation.y] = getCell();
					if(!neighbourToPullA.isEmpty()) pullNeighbour(neighbourToPullA.get(0), fieldLocation);
					fieldLocation = spreadingLocation;
					retraction = true;
			}
			else if(randomNumber >= probabilityA && randomNumber < (probabilityA + probabilityB)){
					cellField.field[fieldLocation.x][fieldLocation.y] = getCell();
					cellField.field[spreadingLocation.x][spreadingLocation.y] = null;
					if(!neighbourToPullB.isEmpty()) pullNeighbour(neighbourToPullB.get(0), spreadingLocation);
					retraction = true;
			}
		
			
			if(retraction){
				spreadingLocation = null;
				modelConnector.setIsSpreadingFN(false);
				modelConnector.setIsSpreadingRGD(false);
			}
		}
		else if((modelConnector.getIsRetractingToFN() && isLocationOnTestSurface(fieldLocation) && !isLocationOnTestSurface(spreadingLocation))
				||(modelConnector.getIsRetractingToRGD() && !isLocationOnTestSurface(fieldLocation) && isLocationOnTestSurface(spreadingLocation))){
			ArrayList<AbstractCell> neighbourToPull = new ArrayList<AbstractCell>();
			int probability = 0;
			if(globalParameters.getUseCellCellInteractionEnergy()){
				probability = rectractingProbabilityBasedOnNeighbourhood(neighbourToPull, fieldLocation, spreadingLocation);			
			}
			else{
				probability = UPPER_PROBABILITY_LIMIT;
			}
			if(randomNumber < probability){
				cellField.field[fieldLocation.x][fieldLocation.y] = null;
				cellField.field[spreadingLocation.x][spreadingLocation.y] = getCell();
				if(!neighbourToPull.isEmpty()) pullNeighbour(neighbourToPull.get(0), fieldLocation);
				fieldLocation = spreadingLocation;
				spreadingLocation = null;
				modelConnector.setIsSpreadingFN(false);
				modelConnector.setIsSpreadingRGD(false);
			}
			
		}
		else if((modelConnector.getIsRetractingToFN() && !isLocationOnTestSurface(fieldLocation) && isLocationOnTestSurface(spreadingLocation))
				||(modelConnector.getIsRetractingToRGD() && isLocationOnTestSurface(fieldLocation) && !isLocationOnTestSurface(spreadingLocation))){
			ArrayList<AbstractCell> neighbourToPull = new ArrayList<AbstractCell>();
			int probability = 0;
			if(globalParameters.getUseCellCellInteractionEnergy()){
				probability = rectractingProbabilityBasedOnNeighbourhood(neighbourToPull, spreadingLocation, fieldLocation);			
			}
			else{
				probability =UPPER_PROBABILITY_LIMIT;
			}
			if(randomNumber < probability){
				cellField.field[fieldLocation.x][fieldLocation.y] = getCell();
				cellField.field[spreadingLocation.x][spreadingLocation.y] = null;
				if(!neighbourToPull.isEmpty()) pullNeighbour(neighbourToPull.get(0), spreadingLocation);
				spreadingLocation = null;
				modelConnector.setIsSpreadingFN(false);
				modelConnector.setIsSpreadingRGD(false);
			}
			
		}
		modelConnector.setIsRetractingToFN(false);
		modelConnector.setIsRetractingToRGD(false);
	}
	
	private void pullNeighbour(AbstractCell neighbour, Int2D locationToPull){
		HexagonBasedMechanicalModel mechModel = (HexagonBasedMechanicalModel) neighbour.getEpisimBioMechanicalModelObject();
		mechModel.spreadingLocation = locationToPull;
		cellField.field[locationToPull.x][locationToPull.y] = neighbour;
		mechModel.modelConnector.setIsSpreadingFN(!isLocationOnTestSurface(locationToPull));
		mechModel.modelConnector.setIsSpreadingRGD(isLocationOnTestSurface(locationToPull));		
	}
	
   
   private boolean isSpreadingOnFNPossible(){
   	return !getPossibleSpreadingLocationIndices(new IntBag(), new IntBag(), false).isEmpty();
   }
   private boolean isSpreadingOnRGDPossible(){
   	return !getPossibleSpreadingLocationIndices(new IntBag(), new IntBag(), true).isEmpty();
   }
   private ArrayList<Integer> getPossibleSpreadingLocationIndices(IntBag xPos, IntBag yPos, boolean onTestSurface){   	
		Bag neighbouringCellsBag = new Bag();
	   if(fieldLocation != null)cellField.getNeighborsHexagonalDistance(fieldLocation.x, fieldLocation.y, 1, globalParameters.getUseContinuousSpace(), neighbouringCellsBag, xPos, yPos);
	   
	   ArrayList<Integer> spreadingLocationIndices = new ArrayList<Integer>();
	   for(int i = 0; i < neighbouringCellsBag.size(); i++){
	   	if(neighbouringCellsBag.get(i)== null){
	   		if(onTestSurface){
	   			if(isLocationOnTestSurface(new Int2D(xPos.get(i), yPos.get(i))))spreadingLocationIndices.add(i);
	   		}
	   		else {
	   			if(!isLocationOnTestSurface(new Int2D(xPos.get(i), yPos.get(i))))spreadingLocationIndices.add(i);
	   		}  		   
	   	}
	   }
	   return spreadingLocationIndices;
   }
   
	public boolean isSpreading(){ return this.spreadingLocation != null; }
	
	public Int2D getSpreadingLocation(){
	  	return this.spreadingLocation;
	}
	
	public Int2D getFieldLocation(){
	  	return this.fieldLocation;
	}
	
	public double getX() {
		return fieldLocation != null ? fieldLocation.x : -1;
	}
	
	public double getY() {
		return fieldLocation != null ? fieldLocation.y : -1;
	}

	public double getZ() {
		return 0;
	}
	
	public boolean getIsOnTestSurface(){
		return modelConnector.getIsOnTestSurface();
	}
	
	public boolean getIsAtSurfaceBorder(){
		return modelConnector.getIsAtSurfaceBorder();
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
	
	public boolean getIsAtWoundEdge(){ return this.isAtWoundEdge; }

	
   protected void newSimStepGloballyFinished(long simStepNumber) {
   	
   	Iterator<AbstractCell> iter = TissueController.getInstance().getActEpidermalTissue().getAllCells().iterator();
   	double kumulativeXPositionWoundEdge=0;
   	double woundEdgeCellCounter=0;
		ArrayList<Double> xPositions = new ArrayList<Double>();
		while(iter.hasNext()){
			AbstractCell cell = iter.next();
			if(cell.getEpisimBioMechanicalModelObject() instanceof HexagonBasedMechanicalModel){
				HexagonBasedMechanicalModel mechModel = (HexagonBasedMechanicalModel) cell.getEpisimBioMechanicalModelObject();
				if(mechModel.isAtWoundEdge && mechModel.getRealNeighbours().size() > 0){
					double xPos = mechModel.getLocationInMikron(mechModel.fieldLocation).x + HexagonBasedMechanicalModelGlobalParameters.outer_hexagonal_radius;
					xPositions.add(xPos);
					kumulativeXPositionWoundEdge += xPos;
					woundEdgeCellCounter++;
				}					
			}
		}
		Collections.sort(xPositions);
		int n = xPositions.size();
		if(n >0){
			double medianXPos = (n % 2 == 0)?((xPositions.get((n/2)-1)+xPositions.get((n/2)))/2):(xPositions.get(((n+1)/2)-1));
	   	double newXPosWoundEdge = kumulativeXPositionWoundEdge / woundEdgeCellCounter;			
			
	   	globalParameters.setPositionXWoundEdge_Mikron(newXPosWoundEdge);
	   	globalParameters.getActualWoundEdgeBorderlineConfig().x1_InMikron = newXPosWoundEdge;
	   	globalParameters.getActualWoundEdgeBorderlineConfig().x2_InMikron = newXPosWoundEdge;
		}
   }

   @CannotBeMonitored
   public Shape getCellBoundariesInMikron() {
		Double2D fieldLoc =getLocationInMikron();
    	Double2D spreadingLoc =getSpreadingLocationInMikron();
    	double heightInMikron = TissueController.getInstance().getTissueBorder().getHeightInMikron();
    	double x = 0;
    	double y = 0;
   	double width = 0;
    	double height = 0;
   	double radiusOuter = HexagonBasedMechanicalModelGlobalParameters.outer_hexagonal_radius;
   	double radiusInner = HexagonBasedMechanicalModelGlobalParameters.inner_hexagonal_radius;
	   if(isSpreading()){
	   	
	   	
    		
    		spreadingLoc = correctToroidalSpreadingCoordinatesInMikronForEllipseDrawing(fieldLoc, spreadingLoc);
    		  	
	   	
	   	x = ((fieldLoc.x+spreadingLoc.x)/2d);
	 		y = heightInMikron - ((fieldLoc.y+spreadingLoc.y)/2d);
	 		width = 4* radiusInner;
	 		height = 2 * radiusInner;
	 		x-=(width/2d);
	 		y-=(height/2d);
	   	
	   	
	   	double rotationInDegrees = 0;
	   	
	    	double heightDelta= 0;	
	    	Shape shape;
	    	if((fieldLoc.x <spreadingLoc.x && fieldLoc.y >spreadingLoc.y)
	    			||(fieldLoc.x > spreadingLoc.x && fieldLoc.y < spreadingLoc.y)){ 
	    		rotationInDegrees = 25;
	    		heightDelta = height*0.1*-1d;
	    	}
	    	
	    	if((fieldLoc.x <spreadingLoc.x && fieldLoc.y <spreadingLoc.y)
	    			||(fieldLoc.x > spreadingLoc.x && fieldLoc.y > spreadingLoc.y)){ 
	    		rotationInDegrees = 155;
	    		heightDelta = height*0.1;
	    	}
	   	if((fieldLoc.x == spreadingLoc.x && fieldLoc.y !=spreadingLoc.y)){ 
	   		rotationInDegrees = 90;
	   	}
	    
	    	if(rotationInDegrees != 0){
	    		AffineTransform transform = new AffineTransform();
	    		double rotateX = x + (width/2d);
	    		double rotateY = y + (height/2d);	    		
	    		
	    		transform.setToRotation(Math.toRadians(rotationInDegrees), rotateX, rotateY);	    		
	    		shape = transform.createTransformedShape(new Ellipse2D.Double(x, y, width, height));
	    		if(heightDelta != 0){	    			
	    			transform.setToTranslation(0, heightDelta);
	    			shape = transform.createTransformedShape(shape);
	    		}
	    	}
	    	else shape = new Ellipse2D.Double(x, y, width, height); 	
	   	return shape;
		   	
	   }
	   else{
	   	x = fieldLoc.x;
	 		y = heightInMikron - fieldLoc.y;
	 		width = 2* radiusOuter;
	 		height = 2 * radiusInner;
	 		x-=(width/2d);
	 		y-=(height/2d);
	   	return new Ellipse2D.Double(x,y,height,width);
	   }
	   
   }
   
   public Double2D correctToroidalSpreadingCoordinatesInMikronForEllipseDrawing(Double2D fieldLoc, Double2D spreadingLoc){
  	 double radiusOuter = HexagonBasedMechanicalModelGlobalParameters.outer_hexagonal_radius;
 	 double radiusInner = HexagonBasedMechanicalModelGlobalParameters.inner_hexagonal_radius;
	 double x1 = fieldLoc.x;
	 double y1 = fieldLoc.y;
	 double x2 = spreadingLoc.x;
	 double y2 = spreadingLoc.y;
 	 double distance = Math.sqrt(Math.pow(x1-x2,2)+Math.pow(y1-y2,2));
 	 if(distance > 3*radiusInner){
 		Int2D loc1 = getFieldLocation();
 		Int2D loc2 = getSpreadingLocation();
 		boolean upperLeftCorner = loc1.x==0 && loc1.y==0;
			boolean lowerLeftCorner = loc1.x ==0 && loc1.y == (getCellFieldDimensions().y-1);
			boolean upperRightCorner = loc1.x ==(getCellFieldDimensions().x-1) && loc1.y == 0;
			boolean lowerRightCorner = loc1.x ==(getCellFieldDimensions().x-1) && loc1.y ==(getCellFieldDimensions().y-1);
			
			boolean left = loc1.x==0 && !upperLeftCorner && !lowerLeftCorner;
			boolean right = loc1.x ==(getCellFieldDimensions().x-1) && !upperRightCorner && !lowerRightCorner;
			boolean bottom = loc1.y ==(getCellFieldDimensions().y-1) && !lowerRightCorner && !lowerLeftCorner;
			boolean top = loc1.y==0 && !upperLeftCorner && !upperRightCorner;
			
			boolean upperleftSide = (loc2.x > loc1.x && loc2.y > loc1.y && upperLeftCorner)
									  		|| (loc2.x < loc1.x && loc2.y > loc1.y && top)
									  		|| (loc2.x > loc1.x && loc2.y < loc1.y && left)
									  		|| (loc2.x < loc1.x && loc2.y < loc1.y && !right && !bottom && !lowerRightCorner);
			boolean upperrightSide = (loc2.x < loc1.x && loc2.y > loc1.y && upperRightCorner)
			                     	 || (loc2.x > loc1.x && loc2.y > loc1.y && top)
			                     	 || (loc2.x < loc1.x && loc2.y < loc1.y && right)
			                     	 || (loc2.x > loc1.x && loc2.y < loc1.y && !left && !lowerLeftCorner && !bottom);
			boolean lowerleftSide = (loc2.x > loc1.x && loc2.y < loc1.y && lowerLeftCorner)
       								|| (loc2.x < loc1.x && loc2.y < loc1.y && bottom)
       								|| (loc2.x > loc1.x && loc2.y > loc1.y && left)
       								|| (loc2.x < loc1.x && loc2.y > loc1.y && !top && !upperRightCorner && !right); 			
			boolean lowerrightSide = (loc2.x < loc1.x && loc2.y < loc1.y && lowerRightCorner)
										 || (loc2.x > loc1.x && loc2.y < loc1.y && bottom)
										 || (loc2.x < loc1.x && loc2.y > loc1.y && right)
										 || (loc2.x > loc1.x && loc2.y > loc1.y && !upperLeftCorner && !top && !left);
			
			boolean rightSide = (loc2.x < loc1.x && loc2.y == loc1.y && (right || lowerRightCorner ||upperRightCorner))
		  					 	  || (loc2.x > loc1.x && loc2.y == loc1.y && !(left || lowerLeftCorner ||upperLeftCorner));
			boolean leftSide = (loc2.x > loc1.x && loc2.y == loc1.y && (left || lowerLeftCorner ||upperLeftCorner))
	 	  						 || (loc2.x < loc1.x && loc2.y == loc1.y && !(right || lowerRightCorner ||upperRightCorner));
			boolean upSide = (loc2.x == loc1.x && loc2.y > loc1.y && (top || upperLeftCorner || upperRightCorner))
			 				  || (loc2.x == loc1.x && loc2.y < loc1.y && !(bottom || lowerLeftCorner || lowerRightCorner));
			boolean downSide = (loc2.x == loc1.x && loc2.y < loc1.y && (bottom || lowerLeftCorner || lowerRightCorner))
		  						 || (loc2.x == loc1.x && loc2.y > loc1.y && !(top || upperLeftCorner || upperRightCorner));
 		 
			if(upSide){
				y2 = y1-2*radiusInner;	
			}
			if(downSide){
				y2 = y1+2*radiusInner;						
			}
			if(leftSide){
				x2 = x1 - 2*radiusOuter;
			}
			if(rightSide){
				x2 = x1 + 2*radiusOuter;
			}
			if(upperleftSide){
				x2 = x1 - 1.5*radiusOuter;
				y2 = y1 - radiusInner;
			}
			if(upperrightSide){
				x2 = x1 + 1.5*radiusOuter;
				y2 = y1 - radiusInner;			
			}
			if(lowerrightSide){
				x2 = x1 + 1.5*radiusOuter;
				y2 = y1 + radiusInner;
			}
			if(lowerleftSide){
				x2 = x1 - 1.5*radiusOuter;
				y2 = y1 + radiusInner;
			}
 		 
 	 }
	 return new Double2D(x2,y2);
 }
}
