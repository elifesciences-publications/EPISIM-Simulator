package calculationalgorithms;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import sim.app.episim.AbstractCell;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.gui.EpisimTextOut;

import episimexceptions.CellNotValidException;
import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import episiminterfaces.EpisimDifferentiationLevel;
import episiminterfaces.calc.CalculationAlgorithmDescriptor;
import episiminterfaces.calc.CalculationHandler;
import episiminterfaces.calc.CalculationAlgorithm.CalculationAlgorithmType;


public class OneCellConditionedCalculationAlgorithm extends OneCellCalculationAlgorithm {
	
	
	
	
	
	protected AbstractCell getNewCellForTracking(CalculationHandler handler){
		
		
		Class<? extends AbstractCell> requiredClass = handler.getRequiredCellType();
		try{
			if(requiredClass == null){
				for(AbstractCell actCell : this.allCells){
					if(actCell.isTracked() && handler.conditionFulfilled(actCell)) return actCell;
				}
				
				for(AbstractCell actCell : this.allCells){
					if(actCell.getEpisimCellBehavioralModelObject().getIsAlive() == true 
							&& actCell.getEpisimCellBehavioralModelObject().getDiffLevel().ordinal() != EpisimDifferentiationLevel.STEMCELL 
							&& handler.conditionFulfilled(actCell)) return actCell;
				}
			}
			else{
					
				for(AbstractCell actCell : this.allCells){
					if(actCell.isTracked() && requiredClass.isAssignableFrom(actCell.getClass()) && handler.conditionFulfilled(actCell)) return actCell;
				}
				
				for(AbstractCell actCell : this.allCells){
					if(actCell.getEpisimCellBehavioralModelObject().getIsAlive() == true && requiredClass.isAssignableFrom(actCell.getClass())
							&& actCell.getEpisimCellBehavioralModelObject().getDiffLevel().ordinal() != EpisimDifferentiationLevel.STEMCELL 
							&& handler.conditionFulfilled(actCell)) return actCell;
				}
			}
		}
		catch(CellNotValidException e){
			ExceptionDisplayer.getInstance().displayException(e);
		}
		EpisimTextOut.getEpisimTextOut().println("No Cell found which matches the specified oberservation requirements.", Color.BLACK);
		return null;
	}
	
	
	
	

	
	
	
	public CalculationAlgorithmDescriptor getCalculationAlgorithmDescriptor(int id) {

		final int _id = id;
	   
	   return new CalculationAlgorithmDescriptor(){

			public String getDescription() {				
	         return "This algorithm allows to observe a single cell. For oberservation a cell is selected which fulfils the specified condition.";
         }

			public int getID() { return _id; }

			public String getName() { return "Single Cell Oberserver Conditioned"; }

			public CalculationAlgorithmType getType(){ return CalculationAlgorithmType.ONEDIMRESULT; }

			public boolean hasCondition() { return true; }

			public Map<String, Class<?>> getParameters() {
				Map<String, Class<?>> params = new HashMap<String, Class<?>>();
				
	         return params;
         }	   	
	   };
   }

}
