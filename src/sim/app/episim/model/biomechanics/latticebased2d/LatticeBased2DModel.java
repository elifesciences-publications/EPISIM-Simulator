package sim.app.episim.model.biomechanics.latticebased2d;


import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import ec.util.MersenneTwisterFast;
import episiminterfaces.EpisimCellShape;
import episiminterfaces.NoExport;
import episiminterfaces.monitoring.CannotBeMonitored;
import episimmcc.EpisimModelConnector;
import episimmcc.latticebased2d.EpisimLatticeBased2DMC;
import sim.app.episim.model.AbstractCell;
import sim.app.episim.model.biomechanics.AbstractBiomechanical2DModel;
import sim.app.episim.model.biomechanics.CellBoundaries;
import sim.app.episim.model.biomechanics.Episim2DCellShape;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.controller.TissueController;
import sim.app.episim.model.diffusion.ExtraCellularDiffusionField2D;
import sim.app.episim.util.GenericBag;
import sim.app.episim.visualization.EpisimDrawInfo;
import sim.engine.SimState;
import sim.field.grid.Grid2D;
import sim.field.grid.ObjectGrid2D;
import sim.portrayal.DrawInfo2D;
import sim.util.Bag;
import sim.util.Double2D;
import sim.util.Int2D;
import sim.util.IntBag;



public class LatticeBased2DModel extends AbstractLatticeBased2DModel {
	
	private EpisimLatticeBased2DMC modelConnector;
	
	private static ObjectGrid2D cellField;
	
	private Int2D fieldLocation = null;	
	
	private Int2D spreadingLocation = null;
	
	private static MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
	
	private DrawInfo2D lastDrawInfo2D;
		
	private boolean isAtWoundEdge=false;
	
	private LatticeBased2DModelGP globalParameters;
	
	private static final int UPPER_PROBABILITY_LIMIT = (int) Math.pow(10, 7);
	
	public LatticeBased2DModel(){
		this(null);	
	}

	public LatticeBased2DModel(AbstractCell cell) {
	   super(cell);
	   globalParameters = (LatticeBased2DModelGP)ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
	  
	   if(cellField == null){
	   	
	   	int width = (int)globalParameters.getNumber_of_columns();
	   	int height = (int)globalParameters.getNumber_of_rows();
	   	cellField = new ObjectGrid2D(width, height);
	   }
	   if(cell!= null){
		   AbstractCell motherCell = cell.getMotherCell();
		   
		   if(motherCell != null && motherCell.getID() != cell.getID()){
		   	LatticeBased2DModel motherCellMechModel = (LatticeBased2DModel) motherCell.getEpisimBioMechanicalModelObject();
		   	if(motherCellMechModel.spreadingLocation != null){
		   		cellField.field[motherCellMechModel.spreadingLocation.x][motherCellMechModel.spreadingLocation.y] = cell;
		   		fieldLocation = new Int2D(motherCellMechModel.spreadingLocation.x,motherCellMechModel.spreadingLocation.y);
		   		motherCellMechModel.spreadingLocation = null;
		   		motherCellMechModel.modelConnector.setIsSpreading(false);
		   	}
		   	
		   }	   
	   }
	  	  
	   lastDrawInfo2D = new DrawInfo2D(null, null, new Rectangle2D.Double(0, 0, 0, 0),
		 new Rectangle2D.Double(0, 0, 0, 0));
   }

	 public void setEpisimModelConnector(EpisimModelConnector modelConnector){
	   	if(modelConnector instanceof EpisimLatticeBased2DMC){
	   		this.modelConnector = (EpisimLatticeBased2DMC) modelConnector;
	   	}
	   	else throw new IllegalArgumentException("Episim Model Connector must be of type: EpisimHexagonBasedModelConnector");
	 }
	 public EpisimModelConnector getEpisimModelConnector(){
	   	return this.modelConnector;
	 }
	 @NoExport
	 public GenericBag<AbstractCell> getDirectNeighbours(){
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
	
	public boolean isMembraneCell() {

		return false;
	}

	public void newSimStep(long simStepNumber) {
			
		if(modelConnector.getIsSpreading()&&isSpreadingPossible()){
			if(spreadingLocation == null){ 
				spread();
			}
			
		}
		else if(modelConnector.getIsSpreading() && !isSpreadingPossible()){
			if(spreadingLocation==null)modelConnector.setIsSpreading(false);
		}		
		
		if(modelConnector.getIsRetracting() && spreadingLocation!=null){
			 retract();
		}
		
		if(modelConnector.getIsProliferating()){
			modelConnector.setIsSpreading(false);
			modelConnector.setIsProliferating(false);
		}
		modelConnector.setX(getX());
		modelConnector.setY(getY());
	}
	
	private int rectractingProbabilityFactorBasedOnNeighbourhood(ArrayList<AbstractCell> neighbourToPull, Int2D locationToBeLeft, Int2D locationToBeKept){
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
			if(neighboursToBeLost.get(i).getEpisimCellBehavioralModelObject().getCellType() == getCell().getEpisimCellBehavioralModelObject().getCellType()){
				if(((LatticeBased2DModel)neighboursToBeLost.get(i).getEpisimBioMechanicalModelObject()).isSpreading()) spreadingNeighbours.add(neighboursToBeLost.get(i));
				else nonSpreadingNeighbours.add(neighboursToBeLost.get(i));
			}
		}		
		double factorAllNeighbours = Math.pow(Math.exp(-1d*modelConnector.getCellCellInteractionEnergy()), numberOfNeighbours);
		double factorAllMinusOneNeighbour = Math.pow(Math.exp(-1d*modelConnector.getCellCellInteractionEnergy()), (numberOfNeighbours-1));
		
		
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
	
	private Double2D getLocationInMikron(Int2D location){
		double x=-1, y =-1;
		
		if(location !=null){
			double locX = (double) location.x;
			double locY = (double) location.y;
			x = globalParameters.getOuter_hexagonal_radius() + (locX)*(1.5d*globalParameters.getOuter_hexagonal_radius());
			
			if((location.x%2) ==0){
				y = globalParameters.getInner_hexagonal_radius()+ (locY)*(2d*globalParameters.getInner_hexagonal_radius());
			}
			else{
				y = (locY+1d)*(2*globalParameters.getInner_hexagonal_radius());
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
	
	private void spread(){
   	IntBag xPos = new IntBag();
		IntBag yPos = new IntBag();
	   ArrayList<Integer> spreadingLocationIndices = getPossibleSpreadingLocationIndices(xPos, yPos);
	 
	   if(!spreadingLocationIndices.isEmpty()){
		   int spreadingLocationIndex = getRandomSpreadingLocationIndex(spreadingLocationIndices, xPos, yPos);
		   this.spreadingLocation = new Int2D(xPos.get(spreadingLocationIndex), yPos.get(spreadingLocationIndex));
		   cellField.set(this.spreadingLocation.x, this.spreadingLocation.y, getCell());
	   }
   }
	
	private int getRandomSpreadingLocationIndex(ArrayList<Integer> spreadingLocationIndices, IntBag xPos, IntBag yPos){
		if(globalParameters.isChemotaxisEnabled()){
			String chemotacticFieldName = modelConnector.getChemotacticField();
			if(chemotacticFieldName != null && !chemotacticFieldName.trim().isEmpty()){
				ExtraCellularDiffusionField2D ecDiffField =  (ExtraCellularDiffusionField2D)ModelController.getInstance().getExtraCellularDiffusionController().getExtraCellularDiffusionField(chemotacticFieldName);
				if(ecDiffField != null){
					double[] concentrations = new double[spreadingLocationIndices.size()];
					for(int i = 0; i < spreadingLocationIndices.size();i++){
						Double2D locInMikron = getLocationInMikron(new Int2D(xPos.get(spreadingLocationIndices.get(i)), yPos.get(spreadingLocationIndices.get(i))));
						concentrations[i] = ecDiffField.getAverageConcentrationInArea(getEmptyLatticeCellBoundary(locInMikron.x, locInMikron.y), true);
					}
					int choosenIndex = getSpreadingLocationIndexNumberBasedOnNeighbouringConcentrations(ecDiffField, concentrations);
					if(choosenIndex >= 0) return spreadingLocationIndices.get(choosenIndex);
				}
			}
		}
		return spreadingLocationIndices.get(random.nextInt(spreadingLocationIndices.size()));
	}
	
	private int getSpreadingLocationIndexNumberBasedOnNeighbouringConcentrations(ExtraCellularDiffusionField2D ecDiffField, double[] concentrations){
		double c_max = ecDiffField.getFieldConfiguration().getMaximumConcentration() < Double.POSITIVE_INFINITY 
																																				  ? ecDiffField.getFieldConfiguration().getMaximumConcentration()
																																				  : ecDiffField.getMaxConcentrationInField();
		final double lambda = modelConnector.getLambdaChem();
		double localConcentration = ecDiffField.getAverageConcentrationInArea(getCellBoundariesInMikron(0), true);
		double[] normalizedConcentrations = new double[concentrations.length];
		for(int i = 0; i < concentrations.length; i++){
			double gradient = lambda*(concentrations[i]-localConcentration);
			normalizedConcentrations[i]= gradient > 0 ? ((gradient/c_max)+(1.0d/((double)concentrations.length))) : (1.0d/(((double)concentrations.length)));
		}
		
		double sumNormalizedConcentrations = 0;
	
		for(int i = 0; i < normalizedConcentrations.length; i++){
			if(normalizedConcentrations[i] > 0)sumNormalizedConcentrations+=normalizedConcentrations[i];
		}
		HashMap<Integer, Integer> probabilityArrayIndexToConcentrationArrayIndexMap = new HashMap<Integer, Integer>();
		double[] probabilityArray=null;
		if(sumNormalizedConcentrations > 0){
			probabilityArray = new double[normalizedConcentrations.length];
			int actProbabIndex = 0;
			for(int i = 0; i < probabilityArray.length; i++){
				if(normalizedConcentrations[i] > 0){
					normalizedConcentrations[i] /= sumNormalizedConcentrations;
					probabilityArray[actProbabIndex] = actProbabIndex > 0 ? (probabilityArray[actProbabIndex-1]+normalizedConcentrations[i]):normalizedConcentrations[i];
					probabilityArrayIndexToConcentrationArrayIndexMap.put(actProbabIndex, i);
					actProbabIndex++;					
				}
			}
		}
		
		double randomNumber = random.nextDouble();
		
		if(probabilityArray != null){
			randomNumber = random.nextDouble();
			for(int i = 0; i < probabilityArray.length; i++){
				if(i == 0){
					if(randomNumber >= 0 && randomNumber < probabilityArray[i]) return probabilityArrayIndexToConcentrationArrayIndexMap.get(i);
				}
				else{
					if(randomNumber >= probabilityArray[i-1] && randomNumber < probabilityArray[i]) return probabilityArrayIndexToConcentrationArrayIndexMap.get(i);
				}
			}
		}	
		return -1;
	}
	
	private void retract(){
		
		 
		boolean retraction = false;
		double normGradient = Double.NEGATIVE_INFINITY;
		if(globalParameters.isChemotaxisEnabled()){
			String chemotacticFieldName = modelConnector.getChemotacticField();
			if(chemotacticFieldName != null && !chemotacticFieldName.trim().isEmpty()){
				ExtraCellularDiffusionField2D ecDiffField =  (ExtraCellularDiffusionField2D)ModelController.getInstance().getExtraCellularDiffusionController().getExtraCellularDiffusionField(chemotacticFieldName);
				if(ecDiffField != null){					
					double c_max = ecDiffField.getFieldConfiguration().getMaximumConcentration() < Double.POSITIVE_INFINITY 
					                                                                            ? ecDiffField.getFieldConfiguration().getMaximumConcentration()
					                                                                            : ecDiffField.getMaxConcentrationInField();
		         double c_fieldPos = ecDiffField.getAverageConcentrationInArea(getEmptyLatticeCellBoundary(getLocationInMikron().x, getLocationInMikron().y), true);
		         double c_spreadingPos = ecDiffField.getAverageConcentrationInArea(getEmptyLatticeCellBoundary(getSpreadingLocationInMikron().x, getSpreadingLocationInMikron().y), true);  
					  
					normGradient = (modelConnector.getLambdaChem() *(c_spreadingPos-c_fieldPos))/c_max;
					
					if(normGradient < 0) normGradient = 0;
				}
			}		
		}
		
	
		int randomNumber = random.nextInt(UPPER_PROBABILITY_LIMIT);
		if(modelConnector.getIsRetracting()){
			
			ArrayList<AbstractCell> neighbourToPullA = new ArrayList<AbstractCell>();
			ArrayList<AbstractCell> neighbourToPullB = new ArrayList<AbstractCell>();
			
			int probabilityA = 0;
			int probabilityB = 0;
			if(globalParameters.getUseCellCellInteractionEnergy()){
				probabilityA = rectractingProbabilityFactorBasedOnNeighbourhood(neighbourToPullA, fieldLocation, spreadingLocation);
				probabilityB = rectractingProbabilityFactorBasedOnNeighbourhood(neighbourToPullB, spreadingLocation, fieldLocation);
			}
			else{
				probabilityA = UPPER_PROBABILITY_LIMIT/2;
				probabilityB = UPPER_PROBABILITY_LIMIT/2;
			}
			
			if(normGradient > 0){
				int probabGradient = (int) (normGradient*UPPER_PROBABILITY_LIMIT);			
				probabilityA += probabGradient;
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
				modelConnector.setIsSpreading(false);
			}
		}
		modelConnector.setIsRetracting(false);
	}
	
	private void pullNeighbour(AbstractCell neighbour, Int2D locationToPull){
		LatticeBased2DModel mechModel = (LatticeBased2DModel) neighbour.getEpisimBioMechanicalModelObject();
		mechModel.spreadingLocation = locationToPull;
		cellField.field[locationToPull.x][locationToPull.y] = neighbour;
		mechModel.modelConnector.setIsSpreading(true);
		
	}
	
   
   private boolean isSpreadingPossible(){
   	return !getPossibleSpreadingLocationIndices(new IntBag(), new IntBag()).isEmpty();
   }
   private ArrayList<Integer> getPossibleSpreadingLocationIndices(IntBag xPos, IntBag yPos){   	
		
	   if(fieldLocation != null)cellField.getHexagonalLocations(fieldLocation.x, fieldLocation.y, 1, globalParameters.getUseContinuousSpace()? Grid2D.TOROIDAL:Grid2D.BOUNDED, false, xPos, yPos);
	   
	   ArrayList<Integer> spreadingLocationIndices = new ArrayList<Integer>();
	   for(int i = 0; i < xPos.size(); i++){
	   	if(cellField.field[xPos.objs[i]][yPos.objs[i]]== null){
	   			spreadingLocationIndices.add(i);	   		  		   
	   	}
	   }
	   return spreadingLocationIndices;
   }
   
	public boolean isSpreading(){ return this.spreadingLocation != null; }
	
	public Int2D getSpreadingLocation(){
	  	return this.spreadingLocation;
	}
	public void setSpreadingLocation(Int2D spreadingLocation){
	  	this.spreadingLocation = spreadingLocation;
	}
	@NoExport
	private Int2D getFieldLocation(){
	  	return this.fieldLocation;
	}
	public void setFieldLocation(Int2D fieldLocation){
	  	this.fieldLocation = fieldLocation;
	}	
	@NoExport
	public double getX() {
		return fieldLocation != null ? getLocationInMikron(fieldLocation).x : -1;
	}
	@NoExport
	public double getY() {
		return fieldLocation != null ? getLocationInMikron(fieldLocation).y : -1;
	}
	@NoExport
	public double getZ() {
		return 0;
	}
	
	protected void clearCellField() {	   
	   	cellField.clear();
	   	cellField = null;
   }
	protected void resetCellField() {	   
		clearCellField();
		int width = (int)globalParameters.getNumber_of_columns();
   	int height = (int)globalParameters.getNumber_of_rows();
   	cellField = new ObjectGrid2D(width, height);
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
   @NoExport
   protected Object getCellField() {	  
	   return cellField;
   }
   
   @NoExport
   public Int2D getCellFieldDimensions(){
   	return new Int2D(cellField.getWidth(), cellField.getHeight());
   }
   
  
   
      
   
   //--------------------------------------------------------------------------------------------------------------------------------------------------------------
   // NOT YET NEEDED METHODS
   //--------------------------------------------------------------------------------------------------------------------------------------------------------------
   @CannotBeMonitored
   @NoExport
   public EpisimCellShape<Shape> getPolygonCell() {
		//not yet needed
		return new Episim2DCellShape<Polygon>(new Polygon());
	}
   @CannotBeMonitored
   @NoExport
	public EpisimCellShape<Shape> getPolygonCell(EpisimDrawInfo<DrawInfo2D> info) {
		//not yet needed
		return new Episim2DCellShape<Polygon>(new Polygon());
	}
   @CannotBeMonitored
   @NoExport
	public EpisimCellShape<Shape> getPolygonNucleus() {
		//not yet needed
		return new Episim2DCellShape<Polygon>(new Polygon());
	}
   @CannotBeMonitored
   @NoExport
	public EpisimCellShape<Shape> getPolygonNucleus(EpisimDrawInfo<DrawInfo2D> info) {
		//not yet needed
		return new Episim2DCellShape<Polygon>(new Polygon());
	}
   
   @NoExport
   public DrawInfo2D getLastDrawInfo2D(){
   	return this.lastDrawInfo2D;
   }
   
   @NoExport
   public void setLastDrawInfo2D(DrawInfo2D info){
   	this.lastDrawInfo2D = info;
   }

  
   
	
	
	@NoExport
	public boolean getIsAtWoundEdge(){ return this.isAtWoundEdge; }

	protected void newGlobalSimStep(long simStepNumber, SimState state){ /* NOT NEEDED IN THIS MODEL */ }
	
   protected void newSimStepGloballyFinished(long simStepNumber, SimState state) {   	
   	
   }
   
   private CellBoundaries getEmptyLatticeCellBoundary(double xInMikron, double yInMikron){
     	double heightInMikron = TissueController.getInstance().getTissueBorder().getHeightInMikron();
    	double width = 0;
    	double height = 0;
   	double radiusOuter = globalParameters.getOuter_hexagonal_radius();
   	double radiusInner = globalParameters.getInner_hexagonal_radius();
   	
   	yInMikron = heightInMikron - yInMikron;
 		width = 2* radiusOuter;
 		height = 2 * radiusInner;
 		xInMikron-=(width/2d);
 		yInMikron-=(height/2d);
   	return new CellBoundaries(new Ellipse2D.Double(xInMikron,yInMikron,height,width));
   }
   
   /**
    * Parameter sizeDelta is ignored
    */
   @CannotBeMonitored
   @NoExport   
   public CellBoundaries getCellBoundariesInMikron(double sizeDelta) {
		Double2D fieldLoc =getLocationInMikron();
    	Double2D spreadingLoc =getSpreadingLocationInMikron();
    	double heightInMikron = TissueController.getInstance().getTissueBorder().getHeightInMikron();
    	double x = 0;
    	double y = 0;
   	double width = 0;
    	double height = 0;
   	double radiusOuter = globalParameters.getOuter_hexagonal_radius();
   	double radiusInner = globalParameters.getInner_hexagonal_radius();
	   if(isSpreading()){   		
    		 
    		spreadingLoc = correctToroidalSpreadingCoordinatesInMikronForEllipseDrawing();		
    		
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
	    		//heightDelta = height*0.1*-1d;
	    	}
	    	
	    	if((fieldLoc.x <spreadingLoc.x && fieldLoc.y <spreadingLoc.y)
	    			||(fieldLoc.x > spreadingLoc.x && fieldLoc.y > spreadingLoc.y)){ 
	    		rotationInDegrees = 155;
	    	//	heightDelta = height*0.1;
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
	   	
	    	return new CellBoundaries(shape);
		   	
	   }
	   else{
	   	x = fieldLoc.x;
	 		y = heightInMikron - fieldLoc.y;
	 		width = 2 * radiusOuter;
	 		height = 2 * radiusInner;
	 		x-=(width/2d);
	 		y-=(height/2d);
	   	return new CellBoundaries(new Ellipse2D.Double(x,y,height,width));
	   }
	   
   }
   
   
   public Double2D correctToroidalSpreadingCoordinatesInMikronForEllipseDrawing(){
   
  	 Int2D loc1 = getFieldLocation();
	 Int2D loc2 = getSpreadingLocation();
	 int x = loc2.x;
	 int y = loc2.y;
  	 if(Math.abs(loc1.x-loc2.x) > 1){
  		 if(loc1.x > loc2.x){
  			 x = cellField.getWidth() - loc2.x;
  		 }
  		 else{
  			 x = loc2.x - cellField.getWidth();
  		 }
  	 }
  	 if(Math.abs(loc1.y-loc2.y) > 1){
  		 if(loc1.y > loc2.y){
  			 y = cellField.getHeight() - loc2.y;
  		 }
  		 else{
  			 y = loc2.y - cellField.getHeight();
  		 }
  	 }	
  	 return getLocationInMikron(new Int2D(x,y));
   }  
}
