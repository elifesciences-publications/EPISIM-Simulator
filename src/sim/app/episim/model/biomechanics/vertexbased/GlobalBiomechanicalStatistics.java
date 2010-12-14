package sim.app.episim.model.biomechanics.vertexbased;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;

import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.util.EnhancedSteppable;
import sim.engine.SimState;


public class GlobalBiomechanicalStatistics implements EnhancedSteppable{
	
	
	/**
    * 
    */
   private static final long serialVersionUID = 6206654878533414475L;
   
	public static final String SIM_STEP_NUMBER_INT = "Sim Step No.";
	public static final String T1_TRANSITION_NUMBER_INT = "T1 Transition No.";
	public static final String T2_TRANSITION_NUMBER_INT = "T2 Transition No.";
	public static final String T3_TRANSITION_NUMBER_INT = "T3 Transition No.";
	
	
	private HashSet<String> intValueFields;
	private HashMap<String, Double> globalStatistics;
	
	private static final GlobalBiomechanicalStatistics instance = new GlobalBiomechanicalStatistics();
	
	private GlobalBiomechanicalStatistics(){
		
		intValueFields = new HashSet<String>();
		globalStatistics = new HashMap<String, Double>();
		buildIntValueFieldSet();		
		initializeGlobalStatisticsMap();
	}
	
	private void buildIntValueFieldSet(){
		Field [] fields = this.getClass().getDeclaredFields();
		for(Field actField : fields){
			if(actField.getName().endsWith("_INT")){
				try{
	            intValueFields.add((String)actField.get(this));
            }
            catch (IllegalArgumentException e){
	           ExceptionDisplayer.getInstance().displayException(e);
            }
            catch (IllegalAccessException e){
            	ExceptionDisplayer.getInstance().displayException(e);
            }
			}
		}
	}
	private void initializeGlobalStatisticsMap(){
		Field [] fields = this.getClass().getDeclaredFields();
		try{
			for(Field actField : fields){
				if(Modifier.isFinal(actField.getModifiers()) && actField.get(this) instanceof String){
					
						globalStatistics.put((String)actField.get(this), 0d);	           
	            
				}
			}
		}
      catch (IllegalArgumentException e){
        ExceptionDisplayer.getInstance().displayException(e);
      }
      catch (IllegalAccessException e){
      	ExceptionDisplayer.getInstance().displayException(e);
      }
	}
	
	public static GlobalBiomechanicalStatistics getInstance(){ return instance; }
	

	public void step(SimState state) {	   
	   
   }
	
	public String getCSVFileColumnHeader(){
		StringBuffer buffer = new StringBuffer();
		
		return "";
	}
	
	public String getCSVFileData(CellPolygon[] cells){
		return "";
	}
	

	public double getInterval() {

	  return 1;
   }

}
