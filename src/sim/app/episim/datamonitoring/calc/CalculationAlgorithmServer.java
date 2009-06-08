package sim.app.episim.datamonitoring.calc;


import java.io.ObjectStreamClass;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.util.ClassLoaderChangeListener;
import sim.app.episim.util.GlobalClassLoader;
import episiminterfaces.calc.CalculationAlgorithm;
import episiminterfaces.calc.CalculationAlgorithmDescriptor;

public class CalculationAlgorithmServer implements ClassLoaderChangeListener{
	
	private static final CalculationAlgorithmServer instance = new CalculationAlgorithmServer();
	
	private Map<Integer, CalculationAlgorithm> calculationAlgorithmsMap;
	
	
	private CalculationAlgorithmServer(){
		GlobalClassLoader.getInstance().addClassLoaderChangeListener(this);
		buildCalculationAlgorithmsMap(CalculationAlgorithmsLoader.getInstance().loadCalculationAlgorithms());
	}
	
	public static CalculationAlgorithmServer getInstance(){ return instance; }
	
	private void buildCalculationAlgorithmsMap(List<Class<?>> loadedAlgorithms){
		calculationAlgorithmsMap = new HashMap<Integer, CalculationAlgorithm>();
		if(loadedAlgorithms != null){
			CalculationAlgorithm alg = null;
			for(Class<?> actClass : loadedAlgorithms){
				try{
	            alg = (CalculationAlgorithm) actClass.newInstance();
            }
            catch (InstantiationException e){
	            ExceptionDisplayer.getInstance().displayException(e);
            }
            catch (IllegalAccessException e){
            	ExceptionDisplayer.getInstance().displayException(e);
            }
            int id = actClass.getCanonicalName().hashCode();  
            if(alg != null) calculationAlgorithmsMap.put(id, alg);
			}
		}
		
	}
	
	public List<CalculationAlgorithmDescriptor> getCalculationAlgorithmDescriptors(){
		LinkedList<CalculationAlgorithmDescriptor> descriptors = new LinkedList<CalculationAlgorithmDescriptor>();
		for(int id : this.calculationAlgorithmsMap.keySet()){
			descriptors.add(this.calculationAlgorithmsMap.get(id).getCalculationAlgorithmDescriptor(id));
		}		
		return descriptors;
	}
	
	
	public void classLoaderHasChanged() {
		
		buildCalculationAlgorithmsMap(CalculationAlgorithmsLoader.getInstance().loadCalculationAlgorithms());
	   
	   
   }
	
	

}
