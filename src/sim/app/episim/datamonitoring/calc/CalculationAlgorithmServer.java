package sim.app.episim.datamonitoring.calc;


import java.io.ObjectStreamClass;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import sim.app.episim.CellType;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.util.ClassLoaderChangeListener;
import sim.app.episim.util.GenericBag;
import sim.app.episim.util.GlobalClassLoader;
import sim.app.episim.util.ResultSet;
import episiminterfaces.calc.CalculationAlgorithm;
import episiminterfaces.calc.CalculationAlgorithmDescriptor;
import episiminterfaces.calc.CalculationHandler;
import episiminterfaces.calc.EntityChangeEvent;
import episiminterfaces.calc.SingleCellObserver;
import episiminterfaces.calc.SingleCellObserverAlgorithm;

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
            alg = null;
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
	
	public CalculationAlgorithmDescriptor getCalculationAlgorithmDescriptor(int id){
		return this.calculationAlgorithmsMap.get(id).getCalculationAlgorithmDescriptor(id);
	}
	
	public boolean isDataManagerRegistrationAtCalculationAlgorithmRequired(int algorithmID){
		if(this.calculationAlgorithmsMap.containsKey(algorithmID)){
			if(this.calculationAlgorithmsMap.get(algorithmID) instanceof SingleCellObserverAlgorithm){
				return true;
			}
		}
		return false;
	}


	public void registerCellsAtCalculationAlgorithms(GenericBag<CellType> allCells){
		for(CalculationAlgorithm alg: this.calculationAlgorithmsMap.values()) alg.registerCells(allCells);
	}
	
	public void registerDataManagerAtCalculationAlgorithm(int calculationAlgorithmID, long[] associatedCalculationHandlerIds, final CalculationDataManager<Double> dataManager){
		if(this.calculationAlgorithmsMap.containsKey(calculationAlgorithmID)){
			if(this.calculationAlgorithmsMap.get(calculationAlgorithmID) instanceof SingleCellObserverAlgorithm){
				SingleCellObserverAlgorithm alg = (SingleCellObserverAlgorithm)this.calculationAlgorithmsMap.get(calculationAlgorithmID);
				alg.addSingleCellObserver(associatedCalculationHandlerIds, new SingleCellObserver(){
					public void observedCellHasChanged() {      
	               dataManager.observedEntityHasChanged(new EntityChangeEvent(){public EntityChangeEventType getEventType() { return EntityChangeEventType.CELLCHANGE; }});
               }
				});
			}
		}
	}
	
	public void calculateValues(CalculationHandler handler, ResultSet<Double> results){
		if(handler != null && calculationAlgorithmsMap.containsKey(handler.getCalculationAlgorithmID()) && results != null){
			CalculationAlgorithm algorithm = this.calculationAlgorithmsMap.get(handler.getCalculationAlgorithmID());
			algorithm.calculate(handler, results);
		}
		else{
			if(handler == null) throw new IllegalArgumentException("CalculationHandler was null!");
			else if(!calculationAlgorithmsMap.containsKey(handler.getCalculationAlgorithmID())) throw new IllegalArgumentException("CalculationAlgorithm with ID " + handler.getCalculationAlgorithmID() + " is not available!");
			else if(results == null) throw new IllegalArgumentException("ResultSet was null!");
		}
	}
	
	
	
	public void classLoaderHasChanged() {
		
		buildCalculationAlgorithmsMap(CalculationAlgorithmsLoader.getInstance().loadCalculationAlgorithms());
	   
	   
   }
	
	public void sendRestartSimulationMessageToCalculationAlgorithms(){
		for(CalculationAlgorithm alg : this.calculationAlgorithmsMap.values()) alg.restartSimulation();
	}
	
	public void sendResetMessageToCalculationAlgorithms(){
		for(CalculationAlgorithm alg : this.calculationAlgorithmsMap.values()) alg.reset();
	}
	
	

}
