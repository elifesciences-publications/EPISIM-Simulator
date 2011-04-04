package sim.app.episim.model.biomechanics.vertexbased;

import java.util.ArrayList;

import sim.app.episim.AbstractCell;
import sim.app.episim.tissue.TissueController;
import sim.app.episim.tissue.TissueController.TissueRegistrationListener;
import sim.app.episim.tissue.TissueType;
import sim.app.episim.util.BagChangeEvent;
import sim.app.episim.util.BagChangeListener;
import sim.app.episim.util.GenericBag;


public class VertexBasedModelController implements TissueRegistrationListener, BagChangeListener<AbstractCell>{
	
	
	private static VertexBasedModelController instance;
	
	private CellPolygonCalculator calculator;
	
	private CellCanvas cellCanvas;
	
	private VertexBasedModelController(){
		calculator = new CellPolygonCalculator();
		newTissueWasRegistered();
	}
	
	/*
	 * This method is for testing purposes only, please don't use it
	 */
	public void setCellPolygonArrayInCalculator(CellPolygon[] cellPolygons){
		this.calculator.setCellPolygons(cellPolygons);
	}
	
	
	public static synchronized VertexBasedModelController getInstance(){
		if(instance==null) instance = new VertexBasedModelController();
		return instance;
	}
	
	public CellPolygonCalculator getCellPolygonCalculator(){ return this.calculator; }	
	public CellCanvas getCellCanvas(){ return this.cellCanvas; }
	
	private CellPolygon[] getAllCellPolygons(GenericBag<AbstractCell> allCells){
		ArrayList<CellPolygon> cellPolygons = new ArrayList<CellPolygon>();		
		for(AbstractCell cell : allCells){
			if(cell.getEpisimBioMechanicalModelObject() instanceof VertexBasedMechanicalModel){
				cellPolygons.add(((VertexBasedMechanicalModel)cell.getEpisimBioMechanicalModelObject()).getCellPolygon());
			}
		}
		return cellPolygons.toArray(new CellPolygon[cellPolygons.size()]);
	}
	
	private void refreshCellPolygonArrayInCalculator(){
		TissueType tissue = TissueController.getInstance().getActTissue();
		if(tissue != null && tissue.getAllCells() != null){
			this.calculator.setCellPolygons(getAllCellPolygons(tissue.getAllCells()));
		}
	}	
	
	public void newTissueWasRegistered(){
		TissueType tissue = TissueController.getInstance().getActTissue();
		if(tissue != null && tissue.getAllCells() != null){
			tissue.getAllCells().addBagChangeListener(this);
			refreshCellPolygonArrayInCalculator();
			if(TissueController.getInstance().isTissueLoaded()){
				cellCanvas = new CellCanvas(0, 0, TissueController.getInstance().getImportedTissueWidth(), 
						TissueController.getInstance().getImportedTissueHeight());
				ContinuousVertexField.initializeContinousVertexField(TissueController.getInstance().getImportedTissueWidth(), 
						TissueController.getInstance().getImportedTissueHeight()); 
			}
			else{
				cellCanvas = new CellCanvas(0, 0, (int)TissueController.getInstance().getTissueBorder().getWidth(), 
						(int)TissueController.getInstance().getTissueBorder().getHeight());				
				ContinuousVertexField.initializeContinousVertexField((int)TissueController.getInstance().getTissueBorder().getWidth(), 
						(int)TissueController.getInstance().getTissueBorder().getHeight());
			}			
		}	   
   }

	public void bagHasChanged(BagChangeEvent<AbstractCell> event){
	   refreshCellPolygonArrayInCalculator();	   
   }
}
