package sim.app.episim.model.biomechanics.vertexbased;

import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;

import sim.SimStateServer;
import sim.app.episim.AbstractCell;
import sim.app.episim.gui.EpisimGUIState;
import sim.app.episim.gui.EpisimGUIState.SimulationDisplayProperties;
import sim.app.episim.model.biomechanics.AbstractMechanical2DModel;
import sim.app.episim.model.biomechanics.AbstractMechanicalModel;
import sim.app.episim.model.biomechanics.CellBoundaries;
import sim.app.episim.model.biomechanics.Episim2DCellShape;
import sim.app.episim.model.biomechanics.centerbased.CenterBasedMechanicalModelGP;
import sim.app.episim.model.biomechanics.vertexbased.geom.CellPolygon;
import sim.app.episim.model.biomechanics.vertexbased.geom.Vertex;
import sim.app.episim.model.biomechanics.vertexbased.util.CellPolygonRegistry;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import sim.app.episim.model.initialization.VertexBasedMechanicalModelInitializer;
import sim.app.episim.model.visualization.EpisimDrawInfo;
import sim.app.episim.tissue.TissueController;
import sim.app.episim.util.GenericBag;
import sim.field.continuous.Continuous2D;
import sim.portrayal.DrawInfo2D;
import sim.util.Double2D;
import episimbiomechanics.EpisimModelConnector;
import episimbiomechanics.vertexbased.EpisimVertexBasedModelConnector;
import episimexceptions.GlobalParameterException;
import episiminterfaces.CellPolygonProliferationSuccessListener;
import episiminterfaces.EpisimCellShape;
import episiminterfaces.EpisimDifferentiationLevel;
import episiminterfaces.NoExport;
import episiminterfaces.monitoring.CannotBeMonitored;


public class VertexBasedMechanicalModel extends AbstractMechanical2DModel implements CellPolygonProliferationSuccessListener{
	
	private CellPolygon cellPolygon;	
	private EpisimVertexBasedModelConnector modelConnector;	
	
	private Double2D newPosition;
	private Double2D oldPosition;
	
	private DrawInfo2D lastDrawInfo = null;	
	
	private long currentSimStepNo = 0;
	
	private DrawInfo2D lastDrawInfo2D;
	private static Continuous2D cellField;	
   
	public VertexBasedMechanicalModel(){
   	this(null);
   }
	
	public VertexBasedMechanicalModel(AbstractCell cell){
		super(cell);
		if(cellField == null){
			cellField = new Continuous2D(ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().getNeighborhood_mikron() / 1.5, 
		   		ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().getWidthInMikron(), 
		   		ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().getHeightInMikron());
		}
		if(cell!= null){
			cellPolygon = CellPolygonRegistry.getNewCellPolygon(cell.getMotherId());
			if(cellPolygon!=null){
				cellPolygon.addProliferationAndApoptosisListener(this);
				cellField.setObjectLocation(cell, new Double2D(this.getX(), this.getY()));
			}
		}
	}
	
	public void setLastDrawInfo2D(DrawInfo2D info){
   	this.lastDrawInfo2D = info;
   }
	
	public void setEpisimModelConnector(EpisimModelConnector modelConnector){
		
		if(modelConnector instanceof EpisimVertexBasedModelConnector){
   		this.modelConnector = (EpisimVertexBasedModelConnector) modelConnector;
   	}
   	else throw new IllegalArgumentException("Episim Model Connector must be of type: EpisimVertexBasedModelConnector"); 
	}
	public EpisimModelConnector getEpisimModelConnector(){
	  return this.modelConnector;
	}

	public GenericBag<AbstractCell> getRealNeighbours() {
		
		GenericBag<AbstractCell> neighbours = new GenericBag<AbstractCell>();		
		HashSet<Integer> cellPolygonIds = new HashSet<Integer>();
		
		for(CellPolygon cellPol : this.cellPolygon.getNeighbourPolygons()){
			cellPolygonIds.add(cellPol.getId());
		}
		
		GenericBag<AbstractCell> allCells = TissueController.getInstance().getActEpidermalTissue().getAllCells();
		
		for(AbstractCell cell : allCells){
			
			if(cell.getEpisimBioMechanicalModelObject() != null && cell.getEpisimBioMechanicalModelObject() instanceof VertexBasedMechanicalModel){
				
				if(cellPolygonIds.contains(((VertexBasedMechanicalModel)cell.getEpisimBioMechanicalModelObject()).cellPolygon.getId())) neighbours.add(cell);
				
			}
			
		}
		
	   return neighbours;
   }

	public Double2D getNewPosition(){
	   return newPosition;
   }

	public Double2D getOldPosition(){
	   return oldPosition;
   }
	
	@CannotBeMonitored
	public EpisimCellShape<Shape> getPolygonCell() {	   
		return getPolygonCell(null);
   }
	
	@CannotBeMonitored
	public EpisimCellShape<Shape> getPolygonCell(EpisimDrawInfo<DrawInfo2D> info) {
		lastDrawInfo = info.getDrawInfo();		
		Vertex cellCenter = this.cellPolygon.getCellCenter();		
			
		if(lastDrawInfo != null){
			
			double scale = info.getDrawInfo().draw.height > info.getDrawInfo().draw.width ? info.getDrawInfo().draw.height : info.getDrawInfo().draw.width;
			
			SimulationDisplayProperties props = SimStateServer.getInstance().getEpisimGUIState().getSimulationDisplayProperties(info);
			
			double deltaX = props.offsetX+(props.displayScaleX-1)*cellCenter.getDoubleX();
			double deltaY = props.offsetY+(props.displayScaleY-1)*cellCenter.getDoubleY();
			
			
			
			Shape pol = VertexBasedModelController.getInstance().getCellCanvas().getDrawablePolygon(cellPolygon, (deltaX), (deltaY));
			AffineTransform transform = new AffineTransform();
	       transform.scale(props.displayScaleX, props.displayScaleY);
	       
	       GeneralPath path = new GeneralPath(pol);
      	 Rectangle2D rectBefore = path.getBounds2D();
      	 path.transform(transform);
      	
      	 Rectangle2D rectAfter = path.getBounds2D();
      	 double xShift = (rectBefore.getCenterX() -rectAfter.getCenterX());
      	 double yShift = (rectBefore.getCenterY()-rectAfter.getCenterY());
      	 
      	 transform = new AffineTransform();
      	 
      
      	
      	
      	 transform.translate(xShift, yShift);
      	 path.transform(transform);
	       
	       
	       
	        
			return new Episim2DCellShape(path);
			
		}
	   return new Episim2DCellShape(VertexBasedModelController.getInstance().getCellCanvas().getDrawablePolygon(cellPolygon, 0, 0));
   }
	
	@CannotBeMonitored
	public EpisimCellShape<Shape> getPolygonNucleus(){		
	   return getPolygonNucleus(null);
   }
	
	@CannotBeMonitored
	public EpisimCellShape<Shape> getPolygonNucleus(EpisimDrawInfo<DrawInfo2D> info) {
	  //TODO Auto-generated method stub
		return null;
   }

	public boolean nextToOuterCell(){
	   // TODO Auto-generated method stub
	   return false;
   }

	public boolean isMembraneCell() {	   
		
	   return cellPolygon.hasContactToBasalLayer();
   }

	public GenericBag<AbstractCell> getNeighbouringCells(){
	  return getRealNeighbours();
   }
	
	private HashMap<Long, Integer> waitingCellsMap = new HashMap<Long, Integer>();
	
	public void newSimStep(long simStepNumber){
		currentSimStepNo = simStepNumber;
		Vertex cellCenter = cellPolygon.getCellCenter();
		oldPosition = new Double2D(cellCenter.getDoubleX(), cellCenter.getDoubleY());
		
	//	if(!modelConnector.getIsProliferating()) cellPolygon.setPreferredArea(modelConnector.getPrefCellArea());		
		if(modelConnector.getIsProliferating() && !cellPolygon.isProliferating()){			
			cellPolygon.proliferate();
		}
		if(cellPolygon.isProliferating() && getCell().getEpisimCellBehavioralModelObject().getDiffLevel().ordinal() == EpisimDifferentiationLevel.EARLYSPICELL){
			System.out.println(" --------------------------------- Ich sollte nicht proliferieren");
		
		}
		
		cellPolygon.step(simStepNumber);
		
		cellCenter = cellPolygon.getCellCenter();
		newPosition = new Double2D(cellCenter.getDoubleX(), cellCenter.getDoubleY());
		if(CellPolygonRegistry.isWaitingForCellProliferation(getCell().getID())){
			if(!waitingCellsMap.containsKey(getCell().getID())) waitingCellsMap.put(getCell().getID(), 1);
			else waitingCellsMap.put(getCell().getID(), (waitingCellsMap.get(getCell().getID())+1));
		}
		else{			
			waitingCellsMap.put(getCell().getID(), 0);		
		}
		if(CellPolygonRegistry.isWaitingForCellProliferation(getCell().getID()) 
				&& waitingCellsMap.containsKey(getCell().getID()) 
				&& waitingCellsMap.get(getCell().getID()) >= 2){			
		
			System.out.println("Hallo, ich warte! ID: "+getCell().getID());
		
		}		
		modelConnector.setCellDivisionPossible(cellPolygon.canDivide() || CellPolygonRegistry.isWaitingForCellProliferation(getCell().getID()));
		modelConnector.setIsMembrane(isMembraneCell());
		modelConnector.setIsSurface(!isMembraneCell() && cellPolygon.isSurfaceCell());
		modelConnector.setX(cellCenter.getDoubleX());
		modelConnector.setY(cellCenter.getDoubleY());
   }
	
	@CannotBeMonitored
	public double getX(){ return cellPolygon.getCellCenter().getDoubleX(); }	
	
	@CannotBeMonitored
	public double getY(){ return cellPolygon.getCellCenter().getDoubleY(); }	
	
	@CannotBeMonitored
	public double getZ(){ return 0; }
	
	public CellPolygon getCellPolygon(){ return cellPolygon; }		
	
	public void proliferationCompleted(CellPolygon oldCell, CellPolygon newCell){		
		EpisimDifferentiationLevel diffLevel = getCell().getEpisimCellBehavioralModelObject().getDiffLevel();
		if(diffLevel.ordinal() == EpisimDifferentiationLevel.STEMCELL || diffLevel.ordinal() == EpisimDifferentiationLevel.TACELL){
			double distanceOld = VertexBasedModelController.getInstance().getCellPolygonCalculator().getDistanceToBasalLayer(TissueController.getInstance().getTissueBorder(), oldCell.getCellCenter(), false);
			double distanceNew = VertexBasedModelController.getInstance().getCellPolygonCalculator().getDistanceToBasalLayer(TissueController.getInstance().getTissueBorder(), newCell.getCellCenter(), false);
			if(distanceOld > distanceNew){
				this.cellPolygon.removeProliferationAndApoptosisListener(this);
				CellPolygonRegistry.registerNewCellPolygon(getCell().getID(), this.cellPolygon);
				this.cellPolygon = newCell;
				newCell.addProliferationAndApoptosisListener(this);
			}
			else CellPolygonRegistry.registerNewCellPolygon(getCell().getID(), newCell);		
		}		
		else CellPolygonRegistry.registerNewCellPolygon(getCell().getID(), newCell);		
		
		System.out.println("Proliferation Completed!");	   
   }
	public void apoptosisCompleted(CellPolygon pol) {
	   // TODO Auto-generated method stub	   
   }

	public void maturationCompleted(CellPolygon pol) {
		// TODO Auto-generated method stub	   
   }
	
	/**
	 * This method should be used only by the model initializer!!!
	 * @param cellPolygon
	 */
	public void initializeWithCellPolygon(CellPolygon cellPolygon){
		this.cellPolygon = cellPolygon;
		this.cellPolygon.addProliferationAndApoptosisListener(this);
		cellField.setObjectLocation(this.getCell(), new Double2D(this.getX(), this.getY()));
	}
	
	protected void clearCellField() {
	   if(!cellField.getAllObjects().isEmpty()){
	   	cellField.clear();
	   }
   }

	
   public void removeCellFromCellField() {
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
   		VertexBasedMechanicalModel.cellField = (Continuous2D) cellField;
   	}
   }
	protected void removeCellsInWoundArea(GeneralPath woundArea) {

	   // TODO Wounding in Vertex Model is not yet supported
	   
   }
	
   protected void newSimStepGloballyFinished(long simStepNumber) {

	   //not needed
	   
   }

	@CannotBeMonitored
	@NoExport
   public CellBoundaries getCellBoundariesInMikron() {
	
	  return new CellBoundaries(getPolygonCell().getCellShape());
   }
 }
