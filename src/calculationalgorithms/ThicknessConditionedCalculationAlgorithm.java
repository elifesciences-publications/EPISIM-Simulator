package calculationalgorithms;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import sim.app.episim.EpisimExceptionHandler;
import sim.app.episim.model.AbstractCell;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.util.ResultSet;
import calculationalgorithms.common.AbstractCommonCalculationAlgorithm;
import episimexceptions.CellNotValidException;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters.ModelDimensionality;
import episiminterfaces.calc.CalculationAlgorithm;
import episiminterfaces.calc.CalculationAlgorithmDescriptor;
import episiminterfaces.calc.CalculationHandler;
import episiminterfaces.calc.CalculationAlgorithm.CalculationAlgorithmType;


public class ThicknessConditionedCalculationAlgorithm extends AbstractCommonCalculationAlgorithm implements CalculationAlgorithm{
	
	public ThicknessConditionedCalculationAlgorithm(){}
	
	
	public CalculationAlgorithmDescriptor getCalculationAlgorithmDescriptor(int id) {
		final int _id = id;
	   
	   return new CalculationAlgorithmDescriptor(){

			public String getDescription() {	         
	         return "This algorithms calculates the thickness of a cell layer. First, the subset of cells meeting the defined condition is determined. Second, the cell in this subset with the minimum y- coordinated (minY) and the cell with the maximum y-coordinate (maxY) is determined. Based on these two y-Coordinates the thickness = maxY - minY is calculated.";
         }

			public int getID() { return _id; }

			public String getName() { return "Thickness Calculator Conditioned"; }

			public CalculationAlgorithmType getType() { return CalculationAlgorithmType.ONEDIMRESULT; }

			public boolean hasCondition() { return true; }
			public boolean hasMathematicalExpression() { return false; }
			
			public Map<String, Class<?>> getParameters(){				
				Map<String, Class<?>> params = new LinkedHashMap<String, Class<?>>();
				return params;
         }
	   };
	}

	public void reset() {}

	public void restartSimulation() {}

	public void calculate(CalculationHandler handler, ResultSet<Double> results) {
		double minY = Double.POSITIVE_INFINITY;
		AbstractCell minCell = null;
		ArrayList<AbstractCell> cellSubsetMeetingCondition = new ArrayList<AbstractCell>();
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
		if(thickness >=0) results.add1DValue(thickness);
		else results.add1DValue(0d);	   
   }	
	
}