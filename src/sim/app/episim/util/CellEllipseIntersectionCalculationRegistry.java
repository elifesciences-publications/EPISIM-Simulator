package sim.app.episim.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import sim.app.episim.SimulationStateChangeListener;


public class CellEllipseIntersectionCalculationRegistry implements SimulationStateChangeListener {
	
	private long actSimulationStep = 0;
	
	private Set<String> alreadyCalculatedCells;
	
	
	
	private CellEllipseIntersectionCalculationRegistry(){
		alreadyCalculatedCells = new HashSet<String>();		
	}
	
	private static CellEllipseIntersectionCalculationRegistry instance = new CellEllipseIntersectionCalculationRegistry();
	
	public static CellEllipseIntersectionCalculationRegistry getInstance(){ return instance; }
	
	public void addCellEllipseIntersectionCalculation(long idCell1, long idCell2){
		this.alreadyCalculatedCells.add(buildStringId(idCell1, idCell2));
		this.alreadyCalculatedCells.add(buildStringId(idCell2, idCell1));
	}
	
	public boolean isAreadyCalculated(long idCell1, long idCell2, long actSimStep){
		if(actSimStep > actSimulationStep){
			actSimulationStep = actSimStep;
			alreadyCalculatedCells.clear();
		}
		else if(actSimStep < actSimulationStep){
			throw new IllegalStateException("The current Sim Step is: " + actSimulationStep + " The submitted Sim Step to calculate was: " + actSimStep);
		}
		
		return alreadyCalculatedCells.contains(buildStringId(idCell1, idCell2)) || alreadyCalculatedCells.contains(buildStringId(idCell2, idCell1));
				
	}
	
	private String buildStringId(long cellId1, long cellId2){
		StringBuffer stringBuilder = new StringBuffer();
		stringBuilder.append(cellId1);
		stringBuilder.append(";");
		stringBuilder.append(cellId2);
		
		return stringBuilder.toString(); 	
	}
	
	public void reset(){
		actSimulationStep = 0;
		this.alreadyCalculatedCells.clear();
	}

	public void simulationWasStarted() {

	   reset();
	   
   }

	public void simulationWasStopped() {

	   reset();
	   
   }
	
	public void simulationWasPaused(){}

}
