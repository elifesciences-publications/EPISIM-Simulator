package sim.app.episim.model.biomechanics.hexagonbased3d;


import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.media.j3d.Shape3D;
import javax.media.j3d.TransformGroup;

import ec.util.MersenneTwisterFast;
import episimbiomechanics.EpisimModelConnector;

import episimbiomechanics.hexagonbased3d.EpisimHexagonBased3DModelConnector;
import episiminterfaces.EpisimCellShape;
import episiminterfaces.NoExport;
import episiminterfaces.monitoring.CannotBeMonitored;
import sim.app.episim.AbstractCell;
import sim.app.episim.model.biomechanics.AbstractMechanical3DModel;

import sim.app.episim.model.biomechanics.CellBoundaries;

import sim.app.episim.model.biomechanics.Episim3DCellShape;

import sim.app.episim.model.biomechanics.hexagonbased.HexagonBasedMechanicalModelGP;
import sim.app.episim.model.controller.ModelController;

import sim.app.episim.model.diffusion.ExtraCellularDiffusionField3D;
import sim.app.episim.model.visualization.EpisimDrawInfo;
import sim.app.episim.tissue.TissueController;
import sim.app.episim.util.GenericBag;
import sim.util.Bag;
import sim.util.Double3D;
import sim.util.Int3D;
import sim.util.IntBag;


public class HexagonBased3DMechanicalModel extends AbstractMechanical3DModel {

	private EpisimHexagonBased3DModelConnector modelConnector;
	
	private static HexagonalCellField3D cellField;
	
	private Int3D fieldLocation = null;
	
	private Int3D spreadingLocation = null;
	
	private static MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
	
	private static final int UPPER_PROBABILITY_LIMIT = (int) Math.pow(10, 7);
	
	private HexagonBased3DMechanicalModelGP globalParameters;
	
	public HexagonBased3DMechanicalModel(){
		this(null);
	}
	
	public HexagonBased3DMechanicalModel(AbstractCell cell){
		super(cell);
		globalParameters = (HexagonBased3DMechanicalModelGP)ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
	
		if(cellField == null){
	   	
	   	int width = (int)HexagonBased3DMechanicalModelGP.number_of_columns;
	   	int length = (int)HexagonBased3DMechanicalModelGP.number_of_columns;
	   	int height = (int)HexagonBased3DMechanicalModelGP.number_of_rows;
	   	cellField = new HexagonalCellField3D(width, height, length);
	   }
	   if(cell!= null){
		   AbstractCell motherCell = cell.getMotherCell();
		   
		   if(motherCell != null && motherCell.getID() != cell.getID()){
		   	HexagonBased3DMechanicalModel motherCellMechModel = (HexagonBased3DMechanicalModel) motherCell.getEpisimBioMechanicalModelObject();
		   	if(motherCellMechModel.spreadingLocation != null){
		   		cellField.setFieldLocationOfObject(motherCellMechModel.spreadingLocation, cell);
		   		fieldLocation = new Int3D(motherCellMechModel.spreadingLocation.x,motherCellMechModel.spreadingLocation.y, motherCellMechModel.spreadingLocation.z);
		   		motherCellMechModel.spreadingLocation = null;
		   		motherCellMechModel.modelConnector.setIsSpreading(false);
		   	}		   	
		   }	   
	   }
	}
	public void setEpisimModelConnector(EpisimModelConnector modelConnector){
	   	if(modelConnector instanceof EpisimHexagonBased3DModelConnector){
	   		this.modelConnector = (EpisimHexagonBased3DModelConnector) modelConnector;
	   	}
	   	else throw new IllegalArgumentException("Episim Model Connector must be of type: EpisimHexagonBased3DModelConnector");
	 }
	 public EpisimModelConnector getEpisimModelConnector(){
	   	return this.modelConnector;
	 }
	 
	 @NoExport
	 public GenericBag<AbstractCell> getRealNeighbours(){
		 return getRealNeighbours(globalParameters.getUseContinuousSpace());
	 }
	 @NoExport
	 private GenericBag<AbstractCell> getRealNeighbours(boolean continuous) {
			
		IntBag xPos = new IntBag();
		IntBag yPos = new IntBag();
		IntBag zPos = new IntBag();
		Bag neighbouringCellsBag = new Bag();
		cellField.getNeighborsMaxDistance(fieldLocation.x, fieldLocation.y, fieldLocation.z, 1, continuous, neighbouringCellsBag, xPos, yPos, zPos);
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
		  	zPos.clear();
		  	neighbouringCellsBag.clear();
		  	cellField.getNeighborsMaxDistance(spreadingLocation.x, spreadingLocation.y, spreadingLocation.z, 1, globalParameters.getUseContinuousSpace(), neighbouringCellsBag, xPos, yPos, zPos);
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
		
		if(modelConnector.getIsSpreading() &&isSpreadingPossible()){
			if(spreadingLocation == null){ 
				spread();
			}
			
		}
		else if(modelConnector.getIsSpreading() && !isSpreadingPossible()){
			if(spreadingLocation==null)modelConnector.setIsSpreading(false);
		}
		
			
		if(modelConnector.getIsRelaxing()){
			relax();
		}
		
		
		if(modelConnector.getIsRetracting() && spreadingLocation!=null){
			 retract();
		}
		
		if(modelConnector.getIsProliferating()){
			modelConnector.setIsSpreading(false);
			modelConnector.setIsProliferating(false);
		}		
	}
	
	private int rectractingProbabilityFactorBasedOnNeighbourhood(ArrayList<AbstractCell> neighbourToPull, Int3D locationToBeLeft, Int3D locationToBeKept){
		ArrayList<AbstractCell> neighboursToBeLost = new ArrayList<AbstractCell>();
		IntBag xPos = new IntBag();
		IntBag yPos = new IntBag();
		IntBag zPos = new IntBag();
		Bag neighbouringCellsBag = new Bag();
	   cellField.getNeighborsMaxDistance(locationToBeLeft.x, locationToBeLeft.y, locationToBeLeft.z, 1, false, neighbouringCellsBag, xPos, yPos, zPos);
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
	   zPos.clear();
	   cellField.getNeighborsMaxDistance(locationToBeKept.x, locationToBeKept.y, locationToBeKept.z, 1, false, neighbouringCellsBag, xPos, yPos, zPos);
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
				if(((HexagonBased3DMechanicalModel)neighboursToBeLost.get(i).getEpisimBioMechanicalModelObject()).isSpreading()) spreadingNeighbours.add(neighboursToBeLost.get(i));
				else nonSpreadingNeighbours.add(neighboursToBeLost.get(i));
			}
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
	
	private Double3D getLocationInMikron(Int3D location){
		double x=-1, y =-1, z=-1;
		if(location !=null){
			double locX = (double) location.x;
			double locY = (double) location.y;
			double locZ = (double) location.z;
			x = HexagonBasedMechanicalModelGP.outer_hexagonal_radius + (locX)*(2d*HexagonBasedMechanicalModelGP.outer_hexagonal_radius);
			y = HexagonBasedMechanicalModelGP.outer_hexagonal_radius + (locY)*(2d*HexagonBasedMechanicalModelGP.outer_hexagonal_radius);
			z = HexagonBasedMechanicalModelGP.outer_hexagonal_radius + (locZ)*(2d*HexagonBasedMechanicalModelGP.outer_hexagonal_radius);
			
		}
		return new Double3D(x, y, z);
	}
	@NoExport
	public Double3D getLocationInMikron(){
		return getLocationInMikron(fieldLocation);
	}
	
	@NoExport
	public Double3D getSpreadingLocationInMikron(){
		return getLocationInMikron(spreadingLocation);
	}
	
	private void spread(){
   	IntBag xPos = new IntBag();
		IntBag yPos = new IntBag();
		IntBag zPos = new IntBag();
	   ArrayList<Integer> spreadingLocationIndices = getPossibleSpreadingLocationIndices(xPos, yPos, zPos);
	 
	   if(!spreadingLocationIndices.isEmpty()){
		   int spreadingLocationIndex = getRandomSpreadingLocationIndex(spreadingLocationIndices, xPos, yPos, zPos);
		   this.spreadingLocation = new Int3D(xPos.get(spreadingLocationIndex), yPos.get(spreadingLocationIndex), zPos.get(spreadingLocationIndex));
		   cellField.setSpreadingLocationOfObject(spreadingLocation, getCell());
	   }
   }
	
	private int getRandomSpreadingLocationIndex(ArrayList<Integer> spreadingLocationIndices, IntBag xPos, IntBag yPos, IntBag zPos){
		if(globalParameters.isChemotaxisEnabled()){
			String chemotacticFieldName = modelConnector.getChemotacticField();
			if(chemotacticFieldName != null && !chemotacticFieldName.trim().isEmpty()){
				ExtraCellularDiffusionField3D ecDiffField =  (ExtraCellularDiffusionField3D)ModelController.getInstance().getExtraCellularDiffusionController().getExtraCellularDiffusionField(chemotacticFieldName);
				if(ecDiffField != null){
					double[] concentrations = new double[spreadingLocationIndices.size()];
					for(int i = 0; i < spreadingLocationIndices.size();i++){
						Double3D locInMikron = getLocationInMikron(new Int3D(xPos.get(spreadingLocationIndices.get(i)), yPos.get(spreadingLocationIndices.get(i)), zPos.get(spreadingLocationIndices.get(i))));
						concentrations[i] = ecDiffField.getTotalConcentrationInArea(getEmptyLatticeCellBoundary(locInMikron.x, locInMikron.y, locInMikron.z));
					}
					int choosenIndex = getSpreadingLocationIndexNumberBasedOnNeighbouringConcentrations(ecDiffField, concentrations);
					if(choosenIndex >= 0) return spreadingLocationIndices.get(choosenIndex);
				}
			}
		}
		return spreadingLocationIndices.get(random.nextInt(spreadingLocationIndices.size()));
	}
	
	private int getSpreadingLocationIndexNumberBasedOnNeighbouringConcentrations(ExtraCellularDiffusionField3D ecDiffField, double[] concentrations){
		double c_max = ecDiffField.getFieldConfiguration().getMaximumConcentration() < Double.POSITIVE_INFINITY 
																																				  ? ecDiffField.getFieldConfiguration().getMaximumConcentration()
																																				  : ecDiffField.getMaxConcentrationInField();
		final double lambda = globalParameters.getLambdaChem();
		double localConcentration = ecDiffField.getTotalConcentrationInArea(getCellBoundariesInMikron());
		double[] normalizedConcentrations = new double[concentrations.length];
		for(int i = 0; i < concentrations.length; i++){
			double gradient = lambda*(concentrations[i]-localConcentration);
			normalizedConcentrations[i]= gradient > 0 ? (gradient/c_max) : 0;
		}		
		double sumNormalizedConcentrations = 0;
		int numberOfZeroConcentrations = 0;
		for(int i = 0; i < normalizedConcentrations.length; i++){
			if(normalizedConcentrations[i] > 0)sumNormalizedConcentrations+=normalizedConcentrations[i];
			else numberOfZeroConcentrations++;
		}
		HashMap<Integer, Integer> probabilityArrayIndexToConcentrationArrayIndexMap = new HashMap<Integer, Integer>();
		double[] probabilityArray=null;
		if(sumNormalizedConcentrations > 0){
			probabilityArray = new double[normalizedConcentrations.length-numberOfZeroConcentrations];
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
		boolean selectConcentration = false;
		//This increases the influence of lambda
		if(sumNormalizedConcentrations >0 && sumNormalizedConcentrations < 1){
			selectConcentration = randomNumber < sumNormalizedConcentrations;
		}
		else if(sumNormalizedConcentrations >=1){
			selectConcentration = true;
		}		
		if(selectConcentration && probabilityArray != null){
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
	
	private void relax(){
		modelConnector.setIsRelaxing(false);
		if(spreadingLocation != null){
			 cellField.setSpreadingLocationOfObject(spreadingLocation, null);
		}
		spreadingLocation = null;
		modelConnector.setIsSpreading(false);
	}
	
	private void retract(){		
		 
		boolean retraction = false;
		double normGradient = Double.NEGATIVE_INFINITY;
		if(globalParameters.isChemotaxisEnabled()){
			String chemotacticFieldName = modelConnector.getChemotacticField();
			if(chemotacticFieldName != null && !chemotacticFieldName.trim().isEmpty()){
				ExtraCellularDiffusionField3D ecDiffField =  (ExtraCellularDiffusionField3D)ModelController.getInstance().getExtraCellularDiffusionController().getExtraCellularDiffusionField(chemotacticFieldName);
				if(ecDiffField != null){					
					double c_max = ecDiffField.getFieldConfiguration().getMaximumConcentration() < Double.POSITIVE_INFINITY 
					                                                                            ? ecDiffField.getFieldConfiguration().getMaximumConcentration()
					                                                                            : ecDiffField.getMaxConcentrationInField();
		         double c_fieldPos = ecDiffField.getTotalConcentrationInArea(getEmptyLatticeCellBoundary(getLocationInMikron().x, getLocationInMikron().y, getLocationInMikron().z));
		         double c_spreadingPos = ecDiffField.getTotalConcentrationInArea(getEmptyLatticeCellBoundary(getSpreadingLocationInMikron().x, getSpreadingLocationInMikron().y, getSpreadingLocationInMikron().z));  
					  
					normGradient = (globalParameters.getLambdaChem() *(c_spreadingPos-c_fieldPos))/c_max;
					
					if(normGradient > 1) normGradient = 1;
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
					cellField.setFieldLocationOfObject(fieldLocation, null);
					cellField.setFieldLocationOfObject(spreadingLocation, getCell());
					if(!neighbourToPullA.isEmpty()) pullNeighbour(neighbourToPullA.get(0), fieldLocation);
					fieldLocation = spreadingLocation;
					retraction = true;
			}
			else if(randomNumber >= probabilityA && randomNumber < (probabilityA + probabilityB)){
					cellField.setFieldLocationOfObject(fieldLocation, getCell());
					cellField.setSpreadingLocationOfObject(spreadingLocation, null);
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
	private void pullNeighbour(AbstractCell neighbour, Int3D locationToPull){
		HexagonBased3DMechanicalModel mechModel = (HexagonBased3DMechanicalModel) neighbour.getEpisimBioMechanicalModelObject();
		mechModel.spreadingLocation = locationToPull;
		cellField.setFieldLocationOfObject(locationToPull, neighbour);
		mechModel.modelConnector.setIsSpreading(true);		
	}
	
	private boolean isSpreadingPossible(){
	   return !getPossibleSpreadingLocationIndices(new IntBag(), new IntBag(), new IntBag()).isEmpty();
	}
	
	private ArrayList<Integer> getPossibleSpreadingLocationIndices(IntBag xPos, IntBag yPos, IntBag zPos){   	
		Bag neighbouringCellsBag = new Bag();
	   if(fieldLocation != null)cellField.getNeighborsMaxDistance(fieldLocation.x, fieldLocation.y, fieldLocation.z, 1, globalParameters.getUseContinuousSpace(), neighbouringCellsBag, xPos, yPos, zPos);
	   
	   ArrayList<Integer> spreadingLocationIndices = new ArrayList<Integer>();
	   for(int i = 0; i < neighbouringCellsBag.size(); i++){
	   	if(neighbouringCellsBag.get(i)== null){
	   		spreadingLocationIndices.add(i);	   		  		   
	   	}
	   }
	   return spreadingLocationIndices;
   }
	
	@NoExport
	public boolean isSpreading(){ return this.spreadingLocation != null; }
	
	public Int3D getSpreadingLocation(){
	  	return this.spreadingLocation;
	}
	public void setSpreadingLocation(Int3D spreadingLocation){
	  	this.spreadingLocation = spreadingLocation;
	}
	
	public Int3D getFieldLocation(){
	  	return this.fieldLocation;
	}
	public void setFieldLocation(Int3D fieldLocation){
	  	this.fieldLocation = fieldLocation;
	}
	
	@NoExport
	public double getX() {
		return fieldLocation != null ? fieldLocation.x : -1;
	}
	
	@NoExport
	public double getY() {
		return fieldLocation != null ? fieldLocation.y : -1;
	}
	@NoExport
	public double getZ() {
		return fieldLocation != null ? fieldLocation.z : -1;
	}
	
	protected void clearCellField() {	   
   	cellField.clear();
   
	}

	public void removeCellFromCellField() {
		cellField.setFieldLocationOfObject(fieldLocation, null);
		if(spreadingLocation != null){
			cellField.setSpreadingLocationOfObject(spreadingLocation, null);
		}
	}
	
	/*
    * Be Careful with this method, existing cells at the location will be overwritten...
    */
   @CannotBeMonitored
   @NoExport
   public void setCellLocationInCellField(Double3D location){
   	if(fieldLocation != null) removeCellFromCellField();
   	fieldLocation = new Int3D(cellField.tx((int)location.x), cellField.ty((int)location.y), cellField.tz((int)location.z));   	
   	cellField.setFieldLocationOfObject(fieldLocation, getCell());
   }
   
   @CannotBeMonitored
  
   public Double3D getCellLocationInCellField() {	   
	   return new Double3D(this.fieldLocation.x, this.fieldLocation.y, this.fieldLocation.z);
   }
   @CannotBeMonitored
   @NoExport
   protected Object getCellField() {	  
	   return cellField;
   }
   @NoExport
   public Int3D getCellFieldDimensions(){
   	return new Int3D(cellField.getWidth(), cellField.getHeight(), cellField.getLength());
   }
   
   
   
 //--------------------------------------------------------------------------------------------------------------------------------------------------------------
   // NOT YET NEEDED METHODS
   //--------------------------------------------------------------------------------------------------------------------------------------------------------------
   @CannotBeMonitored
   @NoExport
   public EpisimCellShape<Shape3D> getPolygonCell() {
		//not yet needed
		return new Episim3DCellShape<Shape3D>(new Shape3D());
	}
   @CannotBeMonitored
   @NoExport
	public EpisimCellShape<Shape3D> getPolygonCell(EpisimDrawInfo<TransformGroup> info) {
		//not yet needed
		return new Episim3DCellShape<Shape3D>(new Shape3D());
	}
   @CannotBeMonitored
   @NoExport
	public EpisimCellShape<Shape3D> getPolygonNucleus() {
		//not yet needed
		return new Episim3DCellShape<Shape3D>(new Shape3D());
	}
   @CannotBeMonitored
   @NoExport
	public EpisimCellShape<Shape3D> getPolygonNucleus(EpisimDrawInfo<TransformGroup> info) {
		//not yet needed
		return new Episim3DCellShape<Shape3D>(new Shape3D());
	}
   
   protected void newSimStepGloballyFinished(long simStepNumber) {
   	
   }
   
   private CellBoundaries getEmptyLatticeCellBoundary(double xInMikron, double yInMikron, double zInMikron){
   	//TODO: implement this method
     	double heightInMikron = TissueController.getInstance().getTissueBorder().getHeightInMikron();
    	double width = 0;
    	double height = 0;
   	double radiusOuter = HexagonBasedMechanicalModelGP.outer_hexagonal_radius;
   	double radiusInner = HexagonBasedMechanicalModelGP.inner_hexagonal_radius;
   	
   	yInMikron = heightInMikron - yInMikron;
 		width = 2* radiusOuter;
 		height = 2 * radiusInner;
 		xInMikron-=(width/2d);
 		yInMikron-=(height/2d);
   	return new CellBoundaries(new Ellipse2D.Double(xInMikron,yInMikron,height,width));
   }
   
   @CannotBeMonitored
   @NoExport
   public CellBoundaries getCellBoundariesInMikron() {
   	//TODO: implement this method
		Double3D fieldLoc =getLocationInMikron();
    	Double3D spreadingLoc =getSpreadingLocationInMikron();
    	double heightInMikron = TissueController.getInstance().getTissueBorder().getHeightInMikron();
    	double x = 0;
    	double y = 0;
   	double width = 0;
    	double height = 0;
   	double radiusOuter = HexagonBasedMechanicalModelGP.outer_hexagonal_radius;
   	double radiusInner = HexagonBasedMechanicalModelGP.inner_hexagonal_radius;
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
	   	
	    	return new CellBoundaries(shape);
		   	
	   }
	   else{
	   	x = fieldLoc.x;
	 		y = heightInMikron - fieldLoc.y;
	 		width = 2* radiusOuter;
	 		height = 2 * radiusInner;
	 		x-=(width/2d);
	 		y-=(height/2d);
	   	return new CellBoundaries(new Ellipse2D.Double(x,y,height,width));
	   }
	   
   }
   public Double3D correctToroidalSpreadingCoordinatesInMikronForEllipseDrawing(){
      
   	Int3D loc1 = getFieldLocation();
   	Int3D loc2 = getSpreadingLocation();
  	 	int x = loc2.x;
  	 	int y = loc2.y;
  	 	int z = loc2.y;
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
    	if(Math.abs(loc1.z-loc2.z) > 1){
   		 if(loc1.z > loc2.z){
   			 z = cellField.getLength() - loc2.z;
   		 }
   		 else{
   			 z = loc2.z - cellField.getLength();
   		 }
   	}	
    	return getLocationInMikron(new Int3D(x,y,z));
   } 

}
