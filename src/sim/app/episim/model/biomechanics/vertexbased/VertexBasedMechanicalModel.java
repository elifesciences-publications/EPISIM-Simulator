package sim.app.episim.model.biomechanics.vertexbased;

import java.awt.Polygon;
import java.io.File;
import java.util.HashSet;

import sim.SimStateServer;
import sim.app.episim.AbstractCell;
import sim.app.episim.model.biomechanics.AbstractMechanicalModel;
import sim.app.episim.model.biomechanics.centerbased.CenterBasedMechanicalModelGlobalParameters;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import sim.app.episim.model.initialization.VertexBasedMechanicalModelInitializer;
import sim.app.episim.tissue.TissueController;
import sim.app.episim.util.GenericBag;
import sim.portrayal.DrawInfo2D;
import sim.util.Double2D;
import episimbiomechanics.EpisimModelConnector;
import episimbiomechanics.vertexbased.EpisimVertexBasedModelConnector;
import episimexceptions.GlobalParameterException;
import episiminterfaces.CellPolygonProliferationSuccessListener;
import episiminterfaces.EpisimDifferentiationLevel;
import episiminterfaces.monitoring.CannotBeMonitored;


public class VertexBasedMechanicalModel extends AbstractMechanicalModel implements CellPolygonProliferationSuccessListener{
	
	private CellPolygon cellPolygon;	
	private EpisimVertexBasedModelConnector modelConnector;	
	
	private Double2D newPosition;
	private Double2D oldPosition;
	
	private DrawInfo2D lastDrawInfo = null;	
	
	private long currentSimStepNo = 0;
	
	
	
	public VertexBasedMechanicalModel(AbstractCell cell){
		super(cell);
		if(cell!= null){
			cellPolygon = CellPolygonRegistry.getNewCellPolygon(cell.getMotherId());
			cellPolygon.addProliferationAndApoptosisListener(this);
		}
	}
	
	public void setEpisimModelConnector(EpisimModelConnector modelConnector){
		
		if(modelConnector instanceof EpisimVertexBasedModelConnector){
   		this.modelConnector = (EpisimVertexBasedModelConnector) modelConnector;
   	}
   	else throw new IllegalArgumentException("Episim Model Connector must be of type: EpisimVertexBasedModelConnector");	   
   
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
	public Polygon getPolygonCell() {	   
		return getPolygonCell(null);
   }
	
	@CannotBeMonitored
	public Polygon getPolygonCell(DrawInfo2D info) {
		lastDrawInfo = info;
		
		Vertex cellCenter = this.cellPolygon.getCellCenter();
			
			
			if(lastDrawInfo != null){
				double scale = info.draw.height > info.draw.width ? info.draw.height : info.draw.width ;
				double deltaX = (info.clip.x)+(scale-1)*cellCenter.getDoubleX();
				double deltaY = (info.clip.y)+(scale-1)*cellCenter.getDoubleY();
				return VertexBasedModelController.getInstance().getCellCanvas().getDrawablePolygon(cellPolygon, (deltaX), (deltaY));
			}
	   return VertexBasedModelController.getInstance().getCellCanvas().getDrawablePolygon(cellPolygon, 0, 0);
   }
	
	@CannotBeMonitored
	public Polygon getPolygonNucleus(){		
	   return getPolygonNucleus(null);
   }
	
	@CannotBeMonitored
	public Polygon getPolygonNucleus(DrawInfo2D info) {
	  //TODO Auto-generated method stub
		return null;
   }

	public boolean nextToOuterCell(){
	   // TODO Auto-generated method stub
	   return false;
   }

	public boolean isMembraneCell() {	   
		// TODO Auto-generated method stub
	   return false;
   }

	public GenericBag<AbstractCell> getNeighbouringCells(){
	  return getRealNeighbours();
   }

	public void newSimStep(long simStepNumber){
		currentSimStepNo = simStepNumber;
		Vertex cellCenter = cellPolygon.getCellCenter();
		oldPosition = new Double2D(cellCenter.getDoubleX(), cellCenter.getDoubleY());
		
	//	if(!modelConnector.getIsProliferating()) cellPolygon.setPreferredArea(modelConnector.getPrefCellArea());		
		if(modelConnector.getIsProliferating() && !cellPolygon.isProliferating()){ 
			cellPolygon.proliferate();
			if(getCell().getEpisimCellBehavioralModelObject().getDiffLevel().ordinal() == EpisimDifferentiationLevel.EARLYSPICELL){
				System.out.println("Ich sollte nicht proliferieren");
			}
		}
		
		cellPolygon.step(simStepNumber);
		
		cellCenter = cellPolygon.getCellCenter();
		newPosition = new Double2D(cellCenter.getDoubleX(), cellCenter.getDoubleY());
		
		if(cellPolygon.canDivide() || CellPolygonRegistry.isWaitingForCellProliferation(getCell().getID())){
			System.out.println("Hallo, ich kann mich teilen! ID: "+getCell().getID());
		}		
		modelConnector.setCellDivisionPossible(cellPolygon.canDivide() || CellPolygonRegistry.isWaitingForCellProliferation(getCell().getID()));
		
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
	public BiomechanicalModelInitializer getBiomechanicalModelInitializer(){ return new VertexBasedMechanicalModelInitializer(); }	
   public BiomechanicalModelInitializer getBiomechanicalModelInitializer(File modelInitializationFile){ return new VertexBasedMechanicalModelInitializer(modelInitializationFile); }

	public void proliferationCompleted(CellPolygon oldCell, CellPolygon newCell) {
		
		EpisimDifferentiationLevel diffLevel = getCell().getEpisimCellBehavioralModelObject().getDiffLevel();
		if(diffLevel.ordinal() == EpisimDifferentiationLevel.STEMCELL || diffLevel.ordinal() == EpisimDifferentiationLevel.TACELL){
			double distanceOld = VertexBasedModelController.getInstance().getCellPolygonCalculator().getDistanceToBasalLayer(TissueController.getInstance().getTissueBorder(), oldCell.getCellCenter(), false);
			double distanceNew = VertexBasedModelController.getInstance().getCellPolygonCalculator().getDistanceToBasalLayer(TissueController.getInstance().getTissueBorder(), newCell.getCellCenter(), false);
			if(distanceOld > distanceNew){
				CellPolygonRegistry.registerNewCellPolygon(getCell().getID(), this.cellPolygon);
				this.cellPolygon = newCell;
			}
			else CellPolygonRegistry.registerNewCellPolygon(getCell().getID(), newCell);		
		}		
		else CellPolygonRegistry.registerNewCellPolygon(getCell().getID(), newCell);		
		System.out.println("Proliferation Completed!");	   
   }

	public void apoptosisCompleted(CellPolygon pol) {
	   // TODO Auto-generated method stub	   
   }

}
