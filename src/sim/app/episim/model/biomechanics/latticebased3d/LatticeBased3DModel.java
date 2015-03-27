package sim.app.episim.model.biomechanics.latticebased3d;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;







import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;








import ec.util.MersenneTwisterFast;
import episiminterfaces.EpisimCellShape;
import episiminterfaces.NoExport;
import episiminterfaces.monitoring.CannotBeMonitored;
import episimmcc.EpisimModelConnector;
import episimmcc.latticebased3d.EpisimLatticeBased3DMC;
import sim.app.episim.model.AbstractCell;
import sim.app.episim.model.UniversalCell;
import sim.app.episim.model.biomechanics.AbstractBiomechanical3DModel;
import sim.app.episim.model.biomechanics.Ellipsoid;
import sim.app.episim.model.biomechanics.CellBoundaries;
import sim.app.episim.model.biomechanics.Episim3DCellShape;


import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.diffusion.ExtraCellularDiffusionField3D;
import sim.app.episim.util.GenericBag;
import sim.app.episim.visualization.EpisimDrawInfo;
import sim.engine.SimState;
import sim.util.Bag;
import sim.util.Double3D;
import sim.util.Int3D;
import sim.util.IntBag;


public class LatticeBased3DModel extends AbstractBiomechanical3DModel {

	private EpisimLatticeBased3DMC modelConnector;
	
	private static LatticeCellField3D cellField;
	
	private Int3D fieldLocation = null;
	
	private Int3D spreadingLocation = null;
	
	private static MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
	
	private static final int UPPER_PROBABILITY_LIMIT = (int) Math.pow(10, 7);
	
	private LatticeBased3DModelGP globalParameters;
	
	
	
	private double standardCellRadius = 0.5;
	public LatticeBased3DModel(){
		this(null);
	}
	
	public LatticeBased3DModel(AbstractCell cell){
		super(cell);
		globalParameters = (LatticeBased3DModelGP)ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		standardCellRadius = LatticeBased3DModelGP.hexagonal_radius;
		if(cellField == null){
	   	
	   	int width = globalParameters.getNumber_of_columns();
	   	int length = globalParameters.getNumber_of_columns();
	   	int height = globalParameters.getNumber_of_rows();
	   	cellField = new LatticeCellField3D(width, height, length);
	   }
	   if(cell!= null){
		   AbstractCell motherCell = cell.getMotherCell();
		   
		   if(motherCell != null && motherCell.getID() != cell.getID()){
		   	LatticeBased3DModel motherCellMechModel = (LatticeBased3DModel) motherCell.getEpisimBioMechanicalModelObject();
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
	   	if(modelConnector instanceof EpisimLatticeBased3DMC){
	   		this.modelConnector = (EpisimLatticeBased3DMC) modelConnector;
	   	}
	   	else throw new IllegalArgumentException("Episim Model Connector must be of type: EpisimHexagonBased3DModelConnector");
	 }
	 public EpisimModelConnector getEpisimModelConnector(){
	   	return this.modelConnector;
	 }
	 
	 @NoExport
	 public GenericBag<AbstractCell> getDirectNeighbours(){
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
				
		if(modelConnector.getIsRetracting() && spreadingLocation!=null){
			 retract();
		}
		
		if(modelConnector.getIsProliferating()){
			modelConnector.setIsSpreading(false);
			modelConnector.setIsProliferating(false);
		}
		modelConnector.setX(getX());
		modelConnector.setY(getY());
		modelConnector.setZ(getZ());
		
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
				if(((LatticeBased3DModel)neighboursToBeLost.get(i).getEpisimBioMechanicalModelObject()).isSpreading()) spreadingNeighbours.add(neighboursToBeLost.get(i));
				else nonSpreadingNeighbours.add(neighboursToBeLost.get(i));
			}
		}		
		double factorAllNeighbours = Math.pow(Math.exp(-1d*modelConnector.getCellCellInteractionEnergy()), numberOfNeighbours);
		double factorAllMinusOneNeighbour = Math.pow(Math.exp(-1d*modelConnector.getCellCellInteractionEnergy()), (numberOfNeighbours-1));
		
		
		factorAllNeighbours *= (double)UPPER_PROBABILITY_LIMIT;
		factorAllMinusOneNeighbour *= (double)UPPER_PROBABILITY_LIMIT;		
	
		
		int factorAllNeighboursInt = (int)factorAllNeighbours;
		int factorAllMinusOneNeighbourInt = 0;
		checkNonSpreadingNeighbourListForIntersectionConflicts(nonSpreadingNeighbours, locationToBeLeft);
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
	
	private boolean wouldThisCellBeIsolated(Int3D loc){
		Bag neighbouringCellsBag = new Bag();
		IntBag xPos = new IntBag();
		IntBag yPos = new IntBag();
		IntBag zPos = new IntBag();
		
		
		
		cellField.getNeighborsMaxDistance(loc.x, loc.y, loc.z, 1, globalParameters.getUseContinuousSpace(), neighbouringCellsBag, xPos, yPos, zPos);
		xPos.clear();
		yPos.clear();
		zPos.clear();
		
		for(int i = 0; i < neighbouringCellsBag.size(); i++){
			AbstractCell actOldNeighbour = (AbstractCell) neighbouringCellsBag.get(i);
			if(actOldNeighbour!= null && actOldNeighbour != getCell())return false;
	   }
		return true;
	}
	
	private boolean wouldACellBeIsolated(Int3D loc){
		Bag oldLocNeighbouringCellsBag = new Bag();
		
		IntBag xPos = new IntBag();
		IntBag yPos = new IntBag();
		IntBag zPos = new IntBag();	
		cellField.getNeighborsMaxDistance(loc.x, loc.y, loc.z, 1, globalParameters.getUseContinuousSpace(), oldLocNeighbouringCellsBag, xPos, yPos, zPos);
		for(int i = 0; i < xPos.size(); i++){
			Object cell = cellField.get(xPos.get(i), yPos.get(i), zPos.get(i));
			if(cell != null && cell != getCell()){
				Bag neighbouringCellsBag = new Bag();
				cellField.getNeighborsMaxDistance(xPos.get(i), yPos.get(i), zPos.get(i), 1, globalParameters.getUseContinuousSpace(), neighbouringCellsBag, new IntBag(), new IntBag(), new IntBag());
				boolean neighbourFound = false;
				for(Object neighbourCell : neighbouringCellsBag){
					if(neighbourCell!= null &&neighbourCell != getCell() && neighbourCell != cell){
						neighbourFound = true;
						break;
					}
				}
				if(!neighbourFound){
					return true;			
				}
			}
		}
		return false;
	}
	
	private boolean hasNeighbourhoodIntersection(){
		Bag oldLocNeighbouringCellsBag = new Bag();
		Bag newLocNeighbouringCellsBag = new Bag();
		
		IntBag xPos = new IntBag();
		IntBag yPos = new IntBag();
		IntBag zPos = new IntBag();	
		cellField.getNeighborsMaxDistance(fieldLocation.x, fieldLocation.y, fieldLocation.z, 1, globalParameters.getUseContinuousSpace(), oldLocNeighbouringCellsBag, xPos, yPos, zPos);
		cellField.getNeighborsMaxDistance(spreadingLocation.x, spreadingLocation.y, spreadingLocation.z, 1, globalParameters.getUseContinuousSpace(), newLocNeighbouringCellsBag, xPos, yPos, zPos);
		
		for(Object cellOld: oldLocNeighbouringCellsBag){
			if(cellOld != null && cellOld != getCell()){
				for(Object cellNew : oldLocNeighbouringCellsBag){
					if(cellNew != null && cellNew != getCell() && cellNew == cellOld) return true;
				}
			}
		}
		return false;
	}
	
	private void checkNonSpreadingNeighbourListForIntersectionConflicts(ArrayList<AbstractCell> nonSpreadingNeighbours, Int3D locationToBeLeft){
		AbstractCell[] nonSpreadingNeighboursArray = nonSpreadingNeighbours.toArray(new AbstractCell[nonSpreadingNeighbours.size()]);
		for(int n = 0; n < nonSpreadingNeighboursArray.length; n++){
			AbstractCell neighbour = nonSpreadingNeighboursArray[n];
			LatticeBased3DModel neighbourMechModel = (LatticeBased3DModel) neighbour.getEpisimBioMechanicalModelObject();
			Bag neighbouringCellsBag = new Bag();
			cellField.getNeighborsMaxDistance(neighbourMechModel.getFieldLocation().x, neighbourMechModel.getFieldLocation().y, neighbourMechModel.getFieldLocation().z, 3, globalParameters.getUseContinuousSpace(), neighbouringCellsBag, new IntBag(), new IntBag(), new IntBag());
			Double3D fieldLocMikron = neighbourMechModel.getLocationInMikron();
			Double3D spreadingLocMikron = getLocationInMikron(neighbourMechModel.correctToroidalSpreadingCoordinatesInMikronForEllipseDrawing(locationToBeLeft));
			Line3D line= new Line3D(new Vector3d(fieldLocMikron.x, fieldLocMikron.y, fieldLocMikron.z), 
					  new Vector3d(spreadingLocMikron.x, spreadingLocMikron.y, spreadingLocMikron.z));
			 for(int i = 0; i < neighbouringCellsBag.size(); i++){
			   	if(neighbouringCellsBag.get(i)!= null && neighbouringCellsBag.get(i) != getCell() && neighbouringCellsBag.get(i) != neighbour){
			   		AbstractCell cell = (AbstractCell) neighbouringCellsBag.get(i);
			   		LatticeBased3DModel mechModel = (LatticeBased3DModel) cell.getEpisimBioMechanicalModelObject();
			   		Line3D otherLine = mechModel.getSpreadingLine();
			   		if(otherLine != null){
			   			if(line.lineLineIntersect(otherLine, LatticeBased3DModelGP.hexagonal_radius*0.5)){
			   				nonSpreadingNeighbours.remove(neighbour);
			   			}
			   		}
			   	}
			 }
		}

	}
	
	private Double3D getLocationInMikron(Int3D location){
		double x=-1, y =-1, z=-1;
		if(location !=null){
			double locX = (double) location.x;
			double locY = (double) location.y;
			double locZ = (double) location.z;
			x = LatticeBased3DModelGP.hexagonal_radius + (locX)*(2d*LatticeBased3DModelGP.hexagonal_radius);
			y = LatticeBased3DModelGP.hexagonal_radius + (locY)*(2d*LatticeBased3DModelGP.hexagonal_radius);
			z = LatticeBased3DModelGP.hexagonal_radius + (locZ)*(2d*LatticeBased3DModelGP.hexagonal_radius);
			
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
		   cellField.setSpreadingLocationOfObject(fieldLocation, spreadingLocation, getCell());
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
						concentrations[i] = ecDiffField.getAverageConcentrationInArea(getEmptyLatticeCellBoundary(locInMikron.x, locInMikron.y, locInMikron.z, ecDiffField.getFieldConfiguration().getLatticeSiteSizeInMikron()));
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
		final double lambda = modelConnector.getLambdaChem();
		double localConcentration = ecDiffField.getAverageConcentrationInArea(getCellBoundariesInMikron(ecDiffField.getFieldConfiguration().getLatticeSiteSizeInMikron()));
		double[] normalizedConcentrations = new double[concentrations.length];
		for(int i = 0; i < concentrations.length; i++){
			double gradient = lambda*(concentrations[i]-localConcentration);
			normalizedConcentrations[i]= gradient > 0 ? ((gradient/c_max)+(1.0d/((double)concentrations.length))) : (1.0d/((double)concentrations.length));
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
		if(modelConnector.getIsRetracting()){
					
		
			int randomNumber = random.nextInt(UPPER_PROBABILITY_LIMIT);
						
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
			boolean isolatedCellSpreadingLoc = wouldACellBeIsolated(spreadingLocation);
			boolean isolatedCellFieldLoc = wouldACellBeIsolated(fieldLocation);
			boolean wouldBeIsolatedSpreadingLoc = wouldThisCellBeIsolated(spreadingLocation);
			boolean wouldBeIsolatedFieldLoc = wouldThisCellBeIsolated(fieldLocation);
			boolean hasNeighbourhoodIntersection = hasNeighbourhoodIntersection();
			if(globalParameters.getStickToCellColony() && (!hasNeighbourhoodIntersection ||(wouldBeIsolatedSpreadingLoc^wouldBeIsolatedFieldLoc))){
				boolean retracting = false;
				if(!hasNeighbourhoodIntersection && !isolatedCellSpreadingLoc && !wouldBeIsolatedFieldLoc){
					//System.out.println("Intersection Field");
					cellField.setFieldLocationOfObject(fieldLocation, getCell());
					cellField.setSpreadingLocationOfObject(fieldLocation, spreadingLocation, null);
					if(!neighbourToPullB.isEmpty()) pullNeighbour(neighbourToPullB.get(0), spreadingLocation);
					retracting=true;
				}
				else if(!hasNeighbourhoodIntersection && !isolatedCellFieldLoc && wouldBeIsolatedFieldLoc){
					//System.out.println("A Cell at spreading");
					cellField.setFieldLocationOfObject(fieldLocation, null);
					cellField.setFieldLocationOfObject(spreadingLocation, getCell());
					if(!neighbourToPullA.isEmpty()) pullNeighbour(neighbourToPullA.get(0), fieldLocation);
					fieldLocation = spreadingLocation;
					retracting=true;
				}
				else if(wouldBeIsolatedSpreadingLoc){
					//System.out.println("Isolated spreading");
					cellField.setFieldLocationOfObject(fieldLocation, getCell());
					cellField.setSpreadingLocationOfObject(fieldLocation, spreadingLocation, null);
					if(!neighbourToPullB.isEmpty()) pullNeighbour(neighbourToPullB.get(0), spreadingLocation);
					retracting=true;
				}
				else if(wouldBeIsolatedFieldLoc){
					//System.out.println("Isolated Field");
					cellField.setFieldLocationOfObject(fieldLocation, null);
					cellField.setFieldLocationOfObject(spreadingLocation, getCell());
					if(!neighbourToPullA.isEmpty()) pullNeighbour(neighbourToPullA.get(0), fieldLocation);
					fieldLocation = spreadingLocation;
					retracting=true;
				}
				if(retracting){
					retraction = true;
					spreadingLocation = null;
					modelConnector.setIsSpreading(false);
				}
				else{
					modelConnector.setIsRetracting(false);
					return;
				}				
			}	
			else{
				if(globalParameters.isChemotaxisEnabled()){
					String chemotacticFieldName = modelConnector.getChemotacticField();
					if(chemotacticFieldName != null && !chemotacticFieldName.trim().isEmpty()){
						ExtraCellularDiffusionField3D ecDiffField =  (ExtraCellularDiffusionField3D)ModelController.getInstance().getExtraCellularDiffusionController().getExtraCellularDiffusionField(chemotacticFieldName);
						if(ecDiffField != null){					
							double c_max = ecDiffField.getFieldConfiguration().getMaximumConcentration() < Double.POSITIVE_INFINITY 
							                                                                            ? ecDiffField.getFieldConfiguration().getMaximumConcentration()
							                                                                            : ecDiffField.getMaxConcentrationInField();
				         double c_fieldPos = ecDiffField.getAverageConcentrationInArea(getEmptyLatticeCellBoundary(getLocationInMikron().x, getLocationInMikron().y, getLocationInMikron().z, ecDiffField.getFieldConfiguration().getLatticeSiteSizeInMikron()));
				         double c_spreadingPos = ecDiffField.getAverageConcentrationInArea(getEmptyLatticeCellBoundary(getSpreadingLocationInMikron().x, getSpreadingLocationInMikron().y, getSpreadingLocationInMikron().z, ecDiffField.getFieldConfiguration().getLatticeSiteSizeInMikron()));  
							  
							normGradient = (modelConnector.getLambdaChem() *(c_spreadingPos-c_fieldPos))/c_max;
							
							if(normGradient < 0) normGradient = 0;
						}
					}		
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
						cellField.setSpreadingLocationOfObject(fieldLocation, spreadingLocation, null);
						if(!neighbourToPullB.isEmpty()) pullNeighbour(neighbourToPullB.get(0), spreadingLocation);
						retraction = true;
				}		
				
				if(retraction){
					spreadingLocation = null;
					modelConnector.setIsSpreading(false);
					
				}
			}
		}
		modelConnector.setIsRetracting(false);		
	}
	private void pullNeighbour(AbstractCell neighbour, Int3D locationToPull){
		LatticeBased3DModel mechModel = (LatticeBased3DModel) neighbour.getEpisimBioMechanicalModelObject();
		mechModel.spreadingLocation = locationToPull;
		cellField.setFieldLocationOfObject(locationToPull, neighbour);
		mechModel.modelConnector.setIsSpreading(true);		
	}
	
	private boolean isSpreadingPossible(){
	   return !getPossibleSpreadingLocationIndices(new IntBag(), new IntBag(), new IntBag()).isEmpty();
	}
	
	private ArrayList<Integer> getPossibleSpreadingLocationIndices(IntBag xPos, IntBag yPos, IntBag zPos){   	
		
	   if(fieldLocation != null)cellField.getNeighborLocationsMaxDistance(fieldLocation.x, fieldLocation.y, fieldLocation.z, 1, globalParameters.getUseContinuousSpace(),  xPos, yPos, zPos);
	   
	   ArrayList<Integer> spreadingLocationIndices = new ArrayList<Integer>();
	   for(int i = 0; i < xPos.size(); i++){
	   	if(cellField.get(xPos.objs[i],yPos.objs[i], zPos.objs[i])== null){
	   		if(!hasIntersectionWithNeighbours(new Int3D(xPos.get(i), yPos.get(i), zPos.get(i)))){
		   		spreadingLocationIndices.add(i);
	   		}	   		
	   	}
	   }
	   return spreadingLocationIndices;
   }
	
	private boolean hasIntersectionWithNeighbours(Int3D spreadingLoc){
		Bag neighbouringCellsBag = new Bag();
		cellField.getNeighborsMaxDistance(fieldLocation.x, fieldLocation.y, fieldLocation.z, 3, globalParameters.getUseContinuousSpace(), neighbouringCellsBag, new IntBag(), new IntBag(), new IntBag());
		Double3D fieldLocMikron = getLocationInMikron();
		Double3D spreadingLocMikron = getLocationInMikron(correctToroidalSpreadingCoordinatesInMikronForEllipseDrawing(spreadingLoc));
		Line3D line= new Line3D(new Vector3d(fieldLocMikron.x, fieldLocMikron.y, fieldLocMikron.z), 
				  new Vector3d(spreadingLocMikron.x, spreadingLocMikron.y, spreadingLocMikron.z));
		 for(int i = 0; i < neighbouringCellsBag.size(); i++){
		   	if(neighbouringCellsBag.get(i)!= null && neighbouringCellsBag.get(i) != getCell()){
		   		AbstractCell cell = (AbstractCell) neighbouringCellsBag.get(i);
		   		LatticeBased3DModel mechModel = (LatticeBased3DModel) cell.getEpisimBioMechanicalModelObject();
		   		Line3D otherLine = mechModel.getSpreadingLine();
		   		if(otherLine != null){
		   			if(line.lineLineIntersect(otherLine, LatticeBased3DModelGP.hexagonal_radius*0.5)) return true;
		   		}
		   	}
		 }
		 return false;
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
	protected void resetCellField() {	   
   	cellField.clear();
   	int width = globalParameters.getNumber_of_columns();
   	int length = globalParameters.getNumber_of_columns();
   	int height = globalParameters.getNumber_of_rows();
   	cellField = new LatticeCellField3D(width, height, length);
	}
	
	@NoExport
	public Line3D getSpreadingLine(){
		if(isSpreading()){
			Double3D fieldLocMikron = getLocationInMikron();
			Double3D spreadingLocMikron = getLocationInMikron(correctToroidalSpreadingCoordinatesInMikronForEllipseDrawing());
			return new Line3D(new Vector3d(fieldLocMikron.x, fieldLocMikron.y, fieldLocMikron.z), 
					  new Vector3d(spreadingLocMikron.x, spreadingLocMikron.y, spreadingLocMikron.z));
		}
		return null;
	}

	public void removeCellFromCellField() {
		cellField.setFieldLocationOfObject(fieldLocation, null);
		if(spreadingLocation != null){
			cellField.setSpreadingLocationOfObject(fieldLocation, spreadingLocation, null);
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
   protected void newGlobalSimStep(long simStepNumber, SimState state){ /* NOT NEEDED IN THIS MODEL */ }
   
   protected void newSimStepGloballyFinished(long simStepNumber, SimState state) {
   }
   
   private CellBoundaries getEmptyLatticeCellBoundary(double xInMikron, double yInMikron, double zInMikron, double sizeDelta){
   	Vector3d minVector = new Vector3d((xInMikron-standardCellRadius),
													 (yInMikron-standardCellRadius),
													 (zInMikron-standardCellRadius));

   	Vector3d maxVector = new Vector3d((xInMikron+standardCellRadius),
				  									 (yInMikron+standardCellRadius),
				  									 (zInMikron+standardCellRadius));
   	
   	 Transform3D trans = new Transform3D();
   	 trans.setTranslation(new Vector3d(xInMikron, yInMikron, zInMikron));
	 	 return new CellBoundaries(new Ellipsoid(trans, (standardCellRadius+sizeDelta)), minVector, maxVector);
   } 
   
   @CannotBeMonitored
   @NoExport
   public CellBoundaries getCellBoundariesInMikron(double sizeDelta) {
	 	  Double3D fieldLocMikron = getLocationInMikron(getFieldLocation());
	 	  Vector3d minVector= null;
	 	  Vector3d maxVector= null;
	 	  if(isSpreading()){
	 		  Double3D spreadingLocMikron = getLocationInMikron(correctToroidalSpreadingCoordinatesInMikronForEllipseDrawing());
	   	   
	 		  minVector = new Vector3d(fieldLocMikron.x < spreadingLocMikron.x ? (fieldLocMikron.x-standardCellRadius) : (spreadingLocMikron.x-standardCellRadius),
	 	   								   fieldLocMikron.y < spreadingLocMikron.y ? (fieldLocMikron.y-standardCellRadius) : (spreadingLocMikron.y-standardCellRadius),
	 	   								   fieldLocMikron.z < spreadingLocMikron.z ? (fieldLocMikron.z-standardCellRadius) : (spreadingLocMikron.z-standardCellRadius));
	 	     
	 		  maxVector = new Vector3d(fieldLocMikron.x > spreadingLocMikron.x ? (fieldLocMikron.x+standardCellRadius) : (spreadingLocMikron.x+standardCellRadius),
												fieldLocMikron.y > spreadingLocMikron.y ? (fieldLocMikron.y+standardCellRadius) : (spreadingLocMikron.y+standardCellRadius),
												fieldLocMikron.z > spreadingLocMikron.z ? (fieldLocMikron.z+standardCellRadius) : (spreadingLocMikron.z+standardCellRadius));
	 	  }
	 	  else{
	 		 minVector = new Vector3d((fieldLocMikron.x-standardCellRadius),
					   						(fieldLocMikron.y-standardCellRadius),
					   						(fieldLocMikron.z-standardCellRadius));

	 		 maxVector = new Vector3d((fieldLocMikron.x+standardCellRadius),
											  (fieldLocMikron.y+standardCellRadius),
											  (fieldLocMikron.z+standardCellRadius));
	 	  } 	 
	 	 Transform3D trans = new Transform3D();	 	
	 	 addSpreadingCellRotationAndTranslation(trans);
	 	 addCellTranslation(trans);		   	
	 	 return new CellBoundaries(new Ellipsoid(trans, (standardCellRadius+sizeDelta)), minVector, maxVector);	   
   }
   
   public Int3D correctToroidalSpreadingCoordinatesInMikronForEllipseDrawing(){
   	return correctToroidalSpreadingCoordinatesInMikronForEllipseDrawing(getSpreadingLocation());
   }
   
   public Int3D correctToroidalSpreadingCoordinatesInMikronForEllipseDrawing(Int3D potentialSpreadingLocation){
      
   	Int3D loc1 = getFieldLocation();
   	Int3D loc2 = potentialSpreadingLocation;
  	 	int x = loc2.x;
  	 	int y = loc2.y;
  	 	int z = loc2.z;
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
    	return new Int3D(x,y,z);
   } 
   
   public void addCellTranslation(Transform3D mainTrans){		
   	
		Int3D location = getFieldLocation();
		Transform3D translationTrans = new Transform3D();
		translationTrans.setTranslation(new Vector3f((float)(standardCellRadius + (2f*location.x*standardCellRadius)),
	      		 (float)(standardCellRadius + (2f*location.y*standardCellRadius)),
	      		 (float)(standardCellRadius + (2f*location.z*standardCellRadius))));	
		mainTrans.mul(translationTrans, mainTrans);        
   }

	public void addSpreadingCellRotationAndTranslation(Transform3D mainTrans){		
		
		if(isSpreading()){
			
			double transX=0,transY=0, transZ=0;
			double rotX=0,rotY=0,rotZ=0;
			double scaleX=1, scaleY=1, scaleZ=1;
			
			Int3D fieldLoc = getFieldLocation();
			Int3D spreadingLoc = correctToroidalSpreadingCoordinatesInMikronForEllipseDrawing();
			final double extensionFactor = 0.8;
			if(fieldLoc.x==spreadingLoc.x && fieldLoc.y == spreadingLoc.y && fieldLoc.z != spreadingLoc.z){
				scaleZ=2;
				scaleX-=(1/scaleZ)*0.5;
				scaleY-=(1/scaleZ)*0.5;
				if(spreadingLoc.z < fieldLoc.z) transZ = -1*standardCellRadius;
				if(spreadingLoc.z > fieldLoc.z) transZ = standardCellRadius;
			}
			else if(fieldLoc.x==spreadingLoc.x && fieldLoc.y != spreadingLoc.y && fieldLoc.z == spreadingLoc.z){
				scaleY=2;
				scaleX-=(1/scaleY)*0.5;
				scaleZ-=(1/scaleY)*0.5;
				if(spreadingLoc.y < fieldLoc.y) transY = -1*standardCellRadius;
				if(spreadingLoc.y > fieldLoc.y) transY = standardCellRadius;
			}
			else if(fieldLoc.x!=spreadingLoc.x && fieldLoc.y == spreadingLoc.y && fieldLoc.z == spreadingLoc.z){
				scaleX=2;
				scaleY-=(1/scaleX)*0.5;
				scaleZ-=(1/scaleX)*0.5;
				if(spreadingLoc.x < fieldLoc.x) transX = -1*standardCellRadius;
				if(spreadingLoc.x > fieldLoc.x) transX = standardCellRadius;
			}
			else if(fieldLoc.x==spreadingLoc.x && fieldLoc.y != spreadingLoc.y && fieldLoc.z != spreadingLoc.z){
				scaleZ=Math.sqrt(8)*extensionFactor;
				scaleX-=(1/scaleZ)*0.5;
				scaleY-=(1/scaleZ)*0.5;
				if(spreadingLoc.z < fieldLoc.z){
					transZ = -1*standardCellRadius;
				}
				if(spreadingLoc.z > fieldLoc.z){
					transZ = standardCellRadius;
				}
				if(spreadingLoc.y < fieldLoc.y){
					transY = -1*standardCellRadius;
				}
				if(spreadingLoc.y > fieldLoc.y){
					transY = standardCellRadius;
				}
				if((spreadingLoc.y > fieldLoc.y && spreadingLoc.z > fieldLoc.z)
						|| (spreadingLoc.y < fieldLoc.y && spreadingLoc.z < fieldLoc.z)){
					rotX=-1*45;
				}
				if((spreadingLoc.y < fieldLoc.y && spreadingLoc.z > fieldLoc.z)
						|| (spreadingLoc.y > fieldLoc.y && spreadingLoc.z < fieldLoc.z)){
					rotX=45;
				}				
			}
			else if(fieldLoc.x!=spreadingLoc.x && fieldLoc.y == spreadingLoc.y && fieldLoc.z != spreadingLoc.z){
				scaleX=Math.sqrt(8)*extensionFactor;
				scaleY-=(1/scaleX)*0.5;
				scaleZ-=(1/scaleX)*0.5;
				if(spreadingLoc.z < fieldLoc.z){
					transZ = -1*standardCellRadius;
				}
				if(spreadingLoc.z > fieldLoc.z){
					transZ = standardCellRadius;
				}
				if(spreadingLoc.x < fieldLoc.x){
					transX = -1*standardCellRadius;
				}
				if(spreadingLoc.x > fieldLoc.x){
					transX = standardCellRadius;
				}
				if((spreadingLoc.x > fieldLoc.x && spreadingLoc.z > fieldLoc.z)
						|| (spreadingLoc.x < fieldLoc.x && spreadingLoc.z < fieldLoc.z)){
					rotY=-1*45;
				}
				if((spreadingLoc.x < fieldLoc.x && spreadingLoc.z > fieldLoc.z)
						|| (spreadingLoc.x > fieldLoc.x && spreadingLoc.z < fieldLoc.z)){
					rotY=45;
				}				
			}
			else if(fieldLoc.x!=spreadingLoc.x && fieldLoc.y != spreadingLoc.y && fieldLoc.z == spreadingLoc.z){
				scaleY=Math.sqrt(8)*extensionFactor;
				scaleX-=(1/scaleY)*0.5;
				scaleZ-=(1/scaleY)*0.5;
				if(spreadingLoc.y < fieldLoc.y){
					transY = -1*standardCellRadius;
				}
				if(spreadingLoc.y > fieldLoc.y){
					transY = standardCellRadius;
				}				
				if(spreadingLoc.x < fieldLoc.x){
					transX = -1*standardCellRadius;
				}
				if(spreadingLoc.x > fieldLoc.x){
					transX = standardCellRadius;
				}
				if((spreadingLoc.x > fieldLoc.x && spreadingLoc.y > fieldLoc.y)
						|| (spreadingLoc.x < fieldLoc.x && spreadingLoc.y < fieldLoc.y)){
					rotZ=-1*45;
				}
				if((spreadingLoc.x < fieldLoc.x && spreadingLoc.y > fieldLoc.y)
						|| (spreadingLoc.x > fieldLoc.x && spreadingLoc.y < fieldLoc.y)){
					rotZ=45;
				}				
			}
			else if(fieldLoc.x!=spreadingLoc.x && fieldLoc.y != spreadingLoc.y && fieldLoc.z != spreadingLoc.z){
				scaleX=Math.sqrt(12)*extensionFactor;
				scaleY-=(1/scaleX)*0.5;
				scaleZ-=(1/scaleX)*0.5;
				if(spreadingLoc.z < fieldLoc.z){
					transZ = -1*standardCellRadius;
				}
				if(spreadingLoc.z > fieldLoc.z){
					transZ = standardCellRadius;
				}
				if(spreadingLoc.y < fieldLoc.y){
					transY = -1*standardCellRadius;
				}
				if(spreadingLoc.y > fieldLoc.y){
					transY = standardCellRadius;
				}				
				if(spreadingLoc.x < fieldLoc.x){
					transX = -1*standardCellRadius;
				}
				if(spreadingLoc.x > fieldLoc.x){
					transX = standardCellRadius;
				}
				transX*=extensionFactor;
				transY*=extensionFactor;
				transZ*=extensionFactor;
				if((spreadingLoc.x < fieldLoc.x && spreadingLoc.y > fieldLoc.y && spreadingLoc.z > fieldLoc.z)
						|| (spreadingLoc.x > fieldLoc.x && spreadingLoc.y < fieldLoc.y && spreadingLoc.z < fieldLoc.z)){
					rotY=45;
					rotZ=-1*45;
				}
				if((spreadingLoc.x < fieldLoc.x && spreadingLoc.y > fieldLoc.y && spreadingLoc.z < fieldLoc.z)
						|| (spreadingLoc.x > fieldLoc.x && spreadingLoc.y < fieldLoc.y && spreadingLoc.z > fieldLoc.z)){
					rotY=-1*45;
					rotZ=-1*45;				
				}
				if((spreadingLoc.x > fieldLoc.x && spreadingLoc.y > fieldLoc.y && spreadingLoc.z < fieldLoc.z)
						|| (spreadingLoc.x < fieldLoc.x && spreadingLoc.y < fieldLoc.y && spreadingLoc.z > fieldLoc.z)){
					rotY=45;
					rotZ=45;
				}
				if((spreadingLoc.x > fieldLoc.x && spreadingLoc.y > fieldLoc.y && spreadingLoc.z > fieldLoc.z)
						|| (spreadingLoc.x < fieldLoc.x && spreadingLoc.y < fieldLoc.y && spreadingLoc.z < fieldLoc.z)){
					rotY=-1*45;
					rotZ=45;
				}				
			}		
			
			Transform3D translationTrans = new Transform3D();	
			translationTrans.setTranslation(new Vector3d(transX, transY, transZ));
			Transform3D scaleTrans = new Transform3D();
			scaleTrans.setScale(new Vector3d(scaleX,scaleY,scaleZ));
	   
			Transform3D rotationTrans1 = new Transform3D();
			rotationTrans1.rotX(Math.toRadians(rotX));
			Transform3D rotationTrans2 = new Transform3D();
			rotationTrans2.rotY(Math.toRadians(rotY));
			Transform3D rotationTrans3 = new Transform3D();
			rotationTrans3.rotZ(Math.toRadians(rotZ));			
			
			mainTrans.mul(scaleTrans, mainTrans);
			mainTrans.mul(rotationTrans1, mainTrans);
			mainTrans.mul(rotationTrans2, mainTrans);
			mainTrans.mul(rotationTrans3, mainTrans);
			mainTrans.mul(translationTrans, mainTrans);   
		}
	}

}
