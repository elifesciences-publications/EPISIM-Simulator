package sim.app.episim.model.biomechanics.vertexbased2D;

import java.util.ArrayList;

import sim.app.episim.model.AbstractCell;
import sim.app.episim.model.biomechanics.vertexbased2D.calc.CellPolygonCalculator;
import sim.app.episim.model.biomechanics.vertexbased2D.geom.CellPolygon;
import sim.app.episim.model.biomechanics.vertexbased2D.geom.ContinuousVertexField;
import sim.app.episim.model.biomechanics.vertexbased2D.util.CellCanvas;
import sim.app.episim.tissueimport.TissueController;
import sim.app.episim.tissueimport.TissueType;
import sim.app.episim.tissueimport.TissueController.TissueRegistrationListener;
import sim.app.episim.util.BagChangeEvent;
import sim.app.episim.util.BagChangeListener;
import sim.app.episim.util.ClassLoaderChangeListener;
import sim.app.episim.util.GenericBag;
import sim.app.episim.util.GlobalClassLoader;


public class VertexBasedModelController implements TissueRegistrationListener, BagChangeListener<AbstractCell>, ClassLoaderChangeListener{
	
	
	private static VertexBasedModelController instance;
	
	private CellPolygonCalculator calculator;
	
	private CellCanvas cellCanvas;
	
	private VertexBasedModelController(){
		GlobalClassLoader.getInstance().addClassLoaderChangeListener(this);
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
			if(cell.getEpisimBioMechanicalModelObject() instanceof VertexBasedModel){
				cellPolygons.add(((VertexBasedModel)cell.getEpisimBioMechanicalModelObject()).getCellPolygon());
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
			
			cellCanvas = new CellCanvas(0, 0, (int)TissueController.getInstance().getTissueBorder().getWidthInMikron(), 
						(int)TissueController.getInstance().getTissueBorder().getHeightInMikron());
			ContinuousVertexField.initializeContinousVertexField((int)TissueController.getInstance().getTissueBorder().getWidthInMikron(), 
						(int)TissueController.getInstance().getTissueBorder().getHeightInMikron()); 
				
		}	   
   }

	public void bagHasChanged(BagChangeEvent<AbstractCell> event){
	   refreshCellPolygonArrayInCalculator();	   
   }
	
   public void classLoaderHasChanged() {
		instance = null;
   }
}
