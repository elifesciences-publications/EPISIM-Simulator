package calculationalgorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import sim.app.episim.EpisimExceptionHandler;
import sim.app.episim.SimStateServer;
import sim.app.episim.datamonitoring.GlobalStatistics;
import sim.app.episim.model.AbstractCell;
import sim.app.episim.model.controller.TissueController;
import sim.app.episim.util.ResultSet;
import calculationalgorithms.common.AbstractCommonCalculationAlgorithm;
import episimexceptions.CellNotValidException;
import episiminterfaces.CellDeathListener;
import episiminterfaces.calc.CalculationAlgorithm;
import episiminterfaces.calc.CalculationAlgorithmDescriptor;
import episiminterfaces.calc.CalculationHandler;
import episiminterfaces.calc.CalculationAlgorithm.CalculationAlgorithmType;
import episiminterfaces.calc.marker.TissueObserver;


public class CellDeathCounterUnconditioned extends AbstractCommonCalculationAlgorithm implements CalculationAlgorithm, CellDeathListener{
	
	public static final String ABSOLUTECELLNUMBER = "calculate absolute cell number";
	
	private Map<Long, TissueObserver> observers;
	private List<AbstractCell> cellsDiedSinceLastCalculation;
	
	private Map<Long, Integer> absoluteCellNumbers;
	private int relativeNumberOfCellsDied =0;
	
	
	private long actSimStep = 0;
	
	
	public  CellDeathCounterUnconditioned(){
		observers = new HashMap<Long, TissueObserver>();
		cellsDiedSinceLastCalculation = new ArrayList<AbstractCell>();
		absoluteCellNumbers = new HashMap<Long, Integer>();
		
		GlobalStatistics.getInstance().addCellDeathListenerCalculationAlgorithm(this);
	}
	
	
	public CalculationAlgorithmDescriptor getCalculationAlgorithmDescriptor(int id) {
		final int _id = id;
	   
	   return new CalculationAlgorithmDescriptor(){

			public String getDescription() {	         
	         return "This algorithm calculates the number of cells that have died (absolute or since last check).";
         }

			public int getID() { return _id; }

			public String getName() { return "Cell Death Counter Unconditioned"; }

			public CalculationAlgorithmType getType() { return CalculationAlgorithmType.ONEDIMRESULT; }

			public boolean hasCondition() { return false; }
			
			public boolean hasMathematicalExpression() { return false; }
			
			public Map<String, Class<?>> getParameters() {
				Map<String, Class<?>> params = new LinkedHashMap<String, Class<?>>();
				params.put(CellDeathCounterUnconditioned.ABSOLUTECELLNUMBER, Boolean.TYPE);		        
	         return params;
         }
	   };
	}

	public void reset() {

		observers.clear();
		cellsDiedSinceLastCalculation.clear();
		absoluteCellNumbers = new HashMap<Long, Integer>();
		relativeNumberOfCellsDied = 0;
		actSimStep = 0;
   }

	public void restartSimulation() {

		cellsDiedSinceLastCalculation.clear();
		absoluteCellNumbers = new HashMap<Long, Integer>();
		relativeNumberOfCellsDied = 0;
		actSimStep = 0;
   }
	
	
   protected void setAbsoluteNumberOfCellsDied(long handlerId, int absoluteNumberOfCellsDied) {
	  this.absoluteCellNumbers.put(handlerId, absoluteNumberOfCellsDied);
   }
	
   
   protected int getAbsoluteNumberOfCellsDied(long handlerId) {
	   return this.absoluteCellNumbers.containsKey(handlerId) ? this.absoluteCellNumbers.get(handlerId) : 0;
   }
   
   protected long getActSimStep(){ return this.actSimStep; }
   protected void setActSimStep(long simStep){ this.actSimStep = simStep; }
	

	public void calculate(CalculationHandler handler, ResultSet<Double> results) {		
			if(SimStateServer.getInstance().getSimStepNumber() > getActSimStep()){
				setActSimStep(SimStateServer.getInstance().getSimStepNumber());
				this.relativeNumberOfCellsDied =getCellsDiedSinceLastCalculation().size();
				getCellsDiedSinceLastCalculation().clear();	
			}		
			boolean calculateAbsoluteNumber = (Boolean) handler.getParameters().get(CellDeathCounterUnconditioned.ABSOLUTECELLNUMBER);			
			setAbsoluteNumberOfCellsDied(handler.getID(),getAbsoluteNumberOfCellsDied(handler.getID()) + relativeNumberOfCellsDied);
			if(calculateAbsoluteNumber) results.add1DValue((double)getAbsoluteNumberOfCellsDied(handler.getID()));
			else results.add1DValue((double)relativeNumberOfCellsDied);			   
   }


	protected List<AbstractCell> getCellsDiedSinceLastCalculation(){
		return this.cellsDiedSinceLastCalculation;
	}
	
   public void cellIsDead(AbstractCell cell){
	   this.cellsDiedSinceLastCalculation.add(cell);
   }
	
	
	
}