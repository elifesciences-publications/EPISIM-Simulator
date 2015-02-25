package calculationalgorithms;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import sim.app.episim.EpisimExceptionHandler;
import sim.app.episim.SimStateServer;
import sim.app.episim.model.AbstractCell;
import sim.app.episim.util.ResultSet;
import episimexceptions.CellNotValidException;
import episiminterfaces.calc.CalculationAlgorithm;
import episiminterfaces.calc.CalculationAlgorithmDescriptor;
import episiminterfaces.calc.CalculationHandler;
import episiminterfaces.calc.CalculationAlgorithm.CalculationAlgorithmType;


public class CellDeathCounterConditioned extends CellDeathCounterUnconditioned{
	
	private ArrayList<AbstractCell> copyOfDeadCellList;
	
	public CellDeathCounterConditioned(){
		super();
		copyOfDeadCellList= new ArrayList<AbstractCell>();
	}
	
	public CalculationAlgorithmDescriptor getCalculationAlgorithmDescriptor(int id) {
		final int _id = id;
	   
	   return new CalculationAlgorithmDescriptor(){

			public String getDescription() {	         
	         return "This algorithm calculates the number of cells that have died (absolute or since last check) and hold the defined condition .";
         }

			public int getID() { return _id; }

			public String getName() { return "Cell Death Counter Conditioned"; }

			public CalculationAlgorithmType getType() { return CalculationAlgorithmType.ONEDIMRESULT; }

			public boolean hasCondition() { return true; }
			
			public boolean hasMathematicalExpression() { return false; }
			
			public Map<String, Class<?>> getParameters() {
				Map<String, Class<?>> params = new LinkedHashMap<String, Class<?>>();
				params.put(CellDeathCounterUnconditioned.ABSOLUTECELLNUMBER, Boolean.TYPE);		        
	         return params;
         }
	   };
	}
	
	public void reset() {
		super.reset();
		copyOfDeadCellList= new ArrayList<AbstractCell>();
   }

	public void restartSimulation() {
		super.restartSimulation();
		copyOfDeadCellList= new ArrayList<AbstractCell>();
   }
	
	
	
	public void calculate(CalculationHandler handler, ResultSet<Double> results) {
		try{
			if(SimStateServer.getInstance().getSimStepNumber() > getActSimStep()){
				setActSimStep(SimStateServer.getInstance().getSimStepNumber());
				copyOfDeadCellList.clear();
				copyOfDeadCellList.addAll(getCellsDiedSinceLastCalculation());
				getCellsDiedSinceLastCalculation().clear();	
			}		
		
			boolean calculateAbsoluteNumber = (Boolean) handler.getParameters().get(CellDeathCounterUnconditioned.ABSOLUTECELLNUMBER);
			
			int relativeCellNumber = 0;
			for(AbstractCell actCell: copyOfDeadCellList){
				if(handler.getRequiredCellType() == null || handler.getRequiredCellType().isAssignableFrom(actCell.getClass())){
					if(handler.conditionFulfilled(actCell)) relativeCellNumber++;
				}
			}
			
			setAbsoluteNumberOfCellsDied(handler.getID(), getAbsoluteNumberOfCellsDied(handler.getID()) + relativeCellNumber);
			
			if(calculateAbsoluteNumber) results.add1DValue((double)getAbsoluteNumberOfCellsDied(handler.getID()));
			else results.add1DValue((double)relativeCellNumber);
		}
		catch(CellNotValidException ex){
			EpisimExceptionHandler.getInstance().displayException(ex);
		}		   
   }

}
