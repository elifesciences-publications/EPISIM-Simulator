package calculationalgorithms;

import java.util.HashMap;
import java.util.Map;

import sim.app.episim.AbstractCell;
import sim.app.episim.util.ResultSet;

import episimexceptions.CellNotValidException;
import episiminterfaces.calc.CalculationAlgorithmDescriptor;
import episiminterfaces.calc.CalculationHandler;
import episiminterfaces.calc.CalculationAlgorithm.CalculationAlgorithmType;


public class CellPositionTrackerConditioned {}

/*extends OneCellConditionedCalculationAlgorithm{
	public static final String X = "X coordinate";
	public static final String Y = "Y coordinate";
	public static final String Z = "Z coordinate";
	
	public CalculationAlgorithmDescriptor getCalculationAlgorithmDescriptor(int id) {

		final int _id = id;
	   
		
		
	   return new CalculationAlgorithmDescriptor(){

			public String getDescription() {				
	         return "This algorithm allows to track the position of a single cell. For oberservation a cell is selected which fulfils the specified condition. Please select exactly two coordinates for tracking. Per default and in case of wrong configuration X and Y are tracked.";
         }

			public int getID() { return _id; }

			public String getName() { return "Single Cell Position Tracker Conditioned"; }

			public CalculationAlgorithmType getType(){ return CalculationAlgorithmType.TWODIMRESULT; }

			public boolean hasCondition() { return true; }
			public boolean hasMathematicalExpression() { return false; }

			public Map<String, Class<?>> getParameters() {
				Map<String, Class<?>> params = new HashMap<String, Class<?>>();
				params.put(OneCellCalculationAlgorithm.CELLSEARCHINGSIMSTEPINTERVAL, Integer.TYPE);
				params.put(CellPositionTrackerConditioned.X, Boolean.TYPE);
				params.put(CellPositionTrackerConditioned.Y, Boolean.TYPE);
				params.put(CellPositionTrackerConditioned.Z, Boolean.TYPE);
	         return params;
         }	   	
	   };
   }
	
	public void calculate(CalculationHandler handler, ResultSet<Double> results) {
		if(results.getTimeStep() != getLastTimeStep()){
			setLastTimeStep(results.getTimeStep());
			incrementCounter();
		}
		try{
				checkTrackedCells(handler);
				AbstractCell trackedCell=null;
				
				boolean trackX = (Boolean) handler.getParameters().get(CellPositionTrackerConditioned.X);
				boolean trackY = (Boolean) handler.getParameters().get(CellPositionTrackerConditioned.Y);
				boolean trackZ = (Boolean) handler.getParameters().get(CellPositionTrackerConditioned.Z);
				
				if((!trackX && !trackY && !trackZ) || (trackX && trackY && trackZ)
						|| (trackX && !trackY && !trackZ)
						|| (!trackX && trackY && !trackZ)
						|| (!trackX && !trackY && trackZ)){
					trackX = true;
					trackY = true;
					trackZ = false;
				}
								
				trackedCell = this.trackedCells.get(handlerIdStringIdMap.get(handler.getID()));
				if(trackedCell != null){
					double val1 = 0;
					double val2 = 0;
					
					val1 = trackX ? trackedCell.getEpisimBioMechanicalModelObject().getX() : trackedCell.getEpisimBioMechanicalModelObject().getY();
					val2 = (trackY && !trackZ && trackX) ? trackedCell.getEpisimBioMechanicalModelObject().getY() : trackedCell.getEpisimBioMechanicalModelObject().getZ();
					results.add2DValue(val1, val2);
				}	
			}
			catch (CellNotValidException e){
				System.out.println("This exception should never occur: " + e.getClass().toString() + " - "+ e.getMessage());
			} 
   }

}*/
