package calculationalgorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import sim.app.episim.EpisimExceptionHandler;
import sim.app.episim.model.AbstractCell;
import sim.app.episim.util.ResultSet;
import calculationalgorithms.common.AbstractCommonCalculationAlgorithm;
import episimexceptions.CellNotValidException;
import episiminterfaces.calc.CalculationAlgorithm;
import episiminterfaces.calc.CalculationAlgorithmDescriptor;
import episiminterfaces.calc.CalculationHandler;
import episiminterfaces.calc.CalculationAlgorithm.CalculationAlgorithmType;
import episiminterfaces.calc.marker.TissueObserver;


public class ThicknessConditionedMovingAverageCalculationAlgorithm extends AbstractCommonCalculationAlgorithm implements CalculationAlgorithm{
	public static final String NO_OF_DATA_POINTS = "number of data points M";
	private ArrayList<Double> thicknesses;
	private Map<Long, TissueObserver> observers;
	public ThicknessConditionedMovingAverageCalculationAlgorithm(){
		observers = new HashMap<Long, TissueObserver>();
		thicknesses = new ArrayList<Double>();
	}
	
	
	public CalculationAlgorithmDescriptor getCalculationAlgorithmDescriptor(int id) {
		final int _id = id;
	   
	   return new CalculationAlgorithmDescriptor(){

			public String getDescription() {	         
	         return "This algorithms calculates the thickness of a cell layer. First, the subset of cells meeting the defined condition is determined. Second, the cell in this subset with the minimum y- coordinated (minY) and the cell with the maximum y-coordinate (maxY) is determined. Based on these two y-Coordinates the thickness = maxY - minY is calculated.\n"
	               +"The thickness is averaged over the last M of the n different chart update cycles:\n"
	         		+"mean_thickness=(thickness_n-M-1 + ...  + thickness_n)/M\n"
	               +"NOTE: Only thickness values greater than zero are included. The calculation of the moving average starts as soon as M data points have been calculated.";
         }

			public int getID() { return _id; }

			public String getName() { return "Thickness Calculator Conditioned with Moving Average"; }

			public CalculationAlgorithmType getType() { return CalculationAlgorithmType.ONEDIMRESULT; }

			public boolean hasCondition() { return true; }
			public boolean hasMathematicalExpression() { return false; }
			
			public Map<String, Class<?>> getParameters(){				
				Map<String, Class<?>> params = new LinkedHashMap<String, Class<?>>();
				params.put(ThicknessConditionedMovingAverageCalculationAlgorithm.NO_OF_DATA_POINTS, Integer.TYPE);
						        
	         return params;
         }
	   };
	}

	public void reset() {
		observers.clear();
		thicknesses.clear();
	}

	public void restartSimulation() {
		thicknesses.clear();
	}

	public void calculate(CalculationHandler handler, ResultSet<Double> results) {
		double minY = Double.POSITIVE_INFINITY;
		AbstractCell minCell = null;
		ArrayList<AbstractCell> cellSubsetMeetingCondition = new ArrayList<AbstractCell>();
		int numberOfDataPoints = (Integer) handler.getParameters().get(ThicknessConditionedMovingAverageCalculationAlgorithm.NO_OF_DATA_POINTS);
		if(numberOfDataPoints <= 0) numberOfDataPoints = 1;
		for(AbstractCell actCell : allCells){
			try{
				
				if(handler.conditionFulfilled(actCell)){
					cellSubsetMeetingCondition.add(actCell);
					double actY = actCell.getEpisimBioMechanicalModelObject().getY();
					if(actY < minY){
						minY = actY;
						minCell = actCell;
					}
				}
			}
			catch (CellNotValidException e){
				EpisimExceptionHandler.getInstance().displayException(e);
			}
		}
		double thickness = -1;
		if(minCell != null){
			double maxY = Double.NEGATIVE_INFINITY;
			for(AbstractCell actCell : cellSubsetMeetingCondition){
				double actY = actCell.getEpisimBioMechanicalModelObject().getY();
				if(actY > minY && actY > maxY){
					maxY = actY;
				}
			}
			if(maxY != Double.NEGATIVE_INFINITY){
				thickness = maxY - minY;
			}
			
		}
		if(thickness >=0) thicknesses.add(thickness);
		else thicknesses.add(0d);
		double sum = 0;
		
		int totalNumberOfDataPoints = thicknesses.size();
		if(totalNumberOfDataPoints >= numberOfDataPoints){
			for(int i =0; i < numberOfDataPoints && ((totalNumberOfDataPoints-i)-1)>=0; i++){
				sum += thicknesses.get((totalNumberOfDataPoints-1)-i);
			}
		}
		
		if(sum > 0)results.add1DValue((sum/((double)numberOfDataPoints)));		
		else results.add1DValue(0d);	   
   }	
	
}