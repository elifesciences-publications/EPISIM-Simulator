package sim.app.episim.model.biomechanics.vertexbased;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.util.EnhancedSteppable;
import sim.engine.SimState;


public class GlobalBiomechanicalStatistics implements EnhancedSteppable{	
	
	/**
    * 
    */
   private static final long serialVersionUID = 6206654878533414475L;
   
   
   public enum GBSValue{
   	
   	SIM_STEP_NUMBER("Sim Step No.", Integer.TYPE, false),
   	T1_TRANSITION_NUMBER("T1 Transition No.", Integer.TYPE, true),
   	T2_TRANSITION_NUMBER("T2 Transition No.", Integer.TYPE, true),
   	T3_TRANSITION_NUMBER("T3 Transition No.", Integer.TYPE, true),
   	T3A_TRANSITION_NUMBER("T3A Transition No.", Integer.TYPE, true),
   	T3B_TRANSITION_NUMBER("T3B Transition No.", Integer.TYPE, true),
   	T3C_TRANSITION_NUMBER("T3C Transition No.", Integer.TYPE, true),
   	VERTEX_TOO_CLOSE_TO_EDGE("Vertex too close to edge", Integer.TYPE, true),
   	INTRUDER_VERTEX_NO("No. Intruder Vertices", Integer.TYPE, true),
   	VERTICES_MERGED("Vertices Merged", Integer.TYPE, true),
   	PREF_AREA_OVERHEAD("Pref. Area Overhead", Double.TYPE, true),
   	AVG_CELL_REL_AREA_DEVIATION("Avg. rel. Cell Area Deviation", Double.TYPE, true);
   	
   	private String name;
   	private Class<?> type;
   	private boolean cycleValue;
   	
   	private GBSValue(String name, Class<?> type, boolean cycleValue){
   		this.name = name;
   		this.type = type;
   		this.cycleValue = cycleValue;
   	}
   	
   	public String toString(){ return name; }
   	public boolean isCycleValue(){ return cycleValue; }
   	public boolean isIntValue(){ return Integer.TYPE.isAssignableFrom(type); }
   	public boolean isDoubleValue(){ return Double.TYPE.isAssignableFrom(type); }
   }
   
   private HashMap<GBSValue, Double> globalStatistics;

	
	private static final GlobalBiomechanicalStatistics instance = new GlobalBiomechanicalStatistics();
	
	private GlobalBiomechanicalStatistics(){		
		
		globalStatistics = new HashMap<GBSValue, Double>();
		initializeGlobalStatisticsMap();
	}	
	
	private void initializeGlobalStatisticsMap(){		
		for(GBSValue actValue : GBSValue.values()){								
			globalStatistics.put(actValue, 0d);			
		}		
	}
	
	public static GlobalBiomechanicalStatistics getInstance(){ return instance; }
	

	public void step(SimState state) {	   
	   this.globalStatistics.put(GBSValue.SIM_STEP_NUMBER, (this.globalStatistics.get(GBSValue.SIM_STEP_NUMBER)+1));	   
   }
	
	public String getCSVFileColumnHeader(){
		
		VertexBasedMechanicalModelGlobalParameters globalParameters=null;		
		
		globalParameters = (VertexBasedMechanicalModelGlobalParameters) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		
		
		StringBuffer buffer = new StringBuffer();
		buffer.append("Used Model Parameters:\n");
		buffer.append(globalParameters.getStatisticsHeaderString());
		buffer.append("\n");
		for(GBSValue actValue : GBSValue.values()) buffer.append(actValue.toString()+";");
		buffer.append("\n");
		return buffer.toString();
	}
	
	private void calculateAverageRelativeAreaDeviation(CellPolygon[] cells){
		double counter = 0;
		double cummulativeDifferenceInPercent = 0;
		for(CellPolygon cell : cells){
			if(!cell.isProliferating()){
				counter++;
				cummulativeDifferenceInPercent += ((Math.abs(cell.getCurrentArea()-cell.getPreferredArea())/cell.getCurrentArea())*100);
			}
		}	
 	   this.globalStatistics.put(GBSValue.AVG_CELL_REL_AREA_DEVIATION,(cummulativeDifferenceInPercent/counter));
	}
	
	private void resetCycleValues(){
		for(GBSValue actValue : GBSValue.values()){ 
			if(actValue.isCycleValue()){
				this.globalStatistics.put(actValue, 0d);
			}
		}
	}
	
	public double get(GBSValue gbsValue){ return this.globalStatistics.get(gbsValue); }
	public void set(GBSValue gbsValue, double value){ this.globalStatistics.put(gbsValue, value); }
	
	public String getCSVFileData(CellPolygon[] cells){
		calculateAverageRelativeAreaDeviation(cells);		
		StringBuffer buffer = new StringBuffer();
		for(GBSValue actValue : GBSValue.values()){ 
			if(actValue.isDoubleValue())buffer.append(this.globalStatistics.get(actValue)+";");
			else if(actValue.isIntValue())buffer.append(((int)this.globalStatistics.get(actValue).doubleValue())+";");
		}
		buffer.append("\n");
		resetCycleValues();
		return buffer.toString();
	}
	

	public double getInterval() {

	  return 1;
   }

}
