package sim.app.episim.model.biomechanics.vertexbased;

import java.util.ArrayList;

import sim.app.episim.AbstractCell;
import sim.app.episim.tissue.TissueController;
import sim.app.episim.tissue.TissueController.TissueRegistrationListener;
import sim.app.episim.tissue.TissueType;
import sim.app.episim.util.BagChangeEvent;
import sim.app.episim.util.BagChangeListener;
import sim.app.episim.util.GenericBag;


public class CellPolygonCalculationController implements TissueRegistrationListener, BagChangeListener<AbstractCell>{
	
	
	private static CellPolygonCalculationController instance;
	
	private CellPolygonCalculator calculator;
	
	private CellPolygonCalculationController(){
		calculator = new CellPolygonCalculator();
		newTissueWasRegistered();
	}
	
	/*
	 * This method is for testing purposes only, please don't use it
	 */
	public void setCellPolygonArrayInCalculator(CellPolygon[] cellPolygons){
		this.calculator.setCellPolygons(cellPolygons);
	}
	
	
	public static synchronized CellPolygonCalculationController getInstance(){
		if(instance==null) instance = new CellPolygonCalculationController();
		return instance;
	}
	
	public CellPolygonCalculator getCellPolygonCalculator(){ return this.calculator; }
	
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
		}	   
   }

	public void bagHasChanged(BagChangeEvent<AbstractCell> event) {
	   refreshCellPolygonArrayInCalculator();	   
   }
}
