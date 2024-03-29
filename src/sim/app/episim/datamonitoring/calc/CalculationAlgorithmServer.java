package sim.app.episim.datamonitoring.calc;


import java.io.ObjectStreamClass;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

import calculationalgorithms.common.AbstractCommonCalculationAlgorithm;
import sim.app.episim.EpisimExceptionHandler;
import sim.app.episim.model.AbstractCell;
import sim.app.episim.util.ClassLoaderChangeListener;
import sim.app.episim.util.GenericBag;
import sim.app.episim.util.GlobalClassLoader;
import sim.app.episim.util.ResultSet;
import episiminterfaces.calc.CalculationAlgorithm;
import episiminterfaces.calc.CalculationAlgorithmDescriptor;
import episiminterfaces.calc.CalculationHandler;
import episiminterfaces.calc.EntityChangeEvent;
import episiminterfaces.calc.marker.CallMeEverySimStep;
import episiminterfaces.calc.marker.SingleCellObserver;
import episiminterfaces.calc.marker.SingleCellObserverAlgorithm;
import episiminterfaces.calc.marker.TissueObserver;
import episiminterfaces.calc.marker.TissueObserverAlgorithm;

public class CalculationAlgorithmServer implements ClassLoaderChangeListener{
	
	private static CalculationAlgorithmServer instance;
	
	private Map<Integer, CalculationAlgorithm> calculationAlgorithmsMap;
	private Set<CalculationAlgorithm> toBeCalledAtEverySimulationStep;
	private static Semaphore sem = new Semaphore(1);
	
	private CalculationAlgorithmServer(){
		GlobalClassLoader.getInstance().addClassLoaderChangeListener(this);
		toBeCalledAtEverySimulationStep = new HashSet<CalculationAlgorithm>();
		buildCalculationAlgorithmsMap(CalculationAlgorithmsLoader.getInstance().loadCalculationAlgorithms());
		
	}
	
	public static CalculationAlgorithmServer getInstance(){
		if(instance==null){
			try{
	         sem.acquire();
	         instance = new CalculationAlgorithmServer();				
				sem.release();
         }
         catch (InterruptedException e){
	        EpisimExceptionHandler.getInstance().displayException(e);
         }
				
		}
		return instance; 
	}
	
	private void buildCalculationAlgorithmsMap(List<Class<?>> loadedAlgorithms){
		calculationAlgorithmsMap = new HashMap<Integer, CalculationAlgorithm>();
		if(loadedAlgorithms != null){
			CalculationAlgorithm alg = null;
			for(Class<?> actClass : loadedAlgorithms){
				try{
	            alg = (CalculationAlgorithm) actClass.newInstance();
            }
            catch (InstantiationException e){
	            EpisimExceptionHandler.getInstance().displayException(e);
            }
            catch (IllegalAccessException e){
            	EpisimExceptionHandler.getInstance().displayException(e);
            }
            int id = actClass.getCanonicalName().hashCode();  
            if(alg != null){ 
            	calculationAlgorithmsMap.put(id, alg);
            	if(alg instanceof CallMeEverySimStep) toBeCalledAtEverySimulationStep.add(alg);
            }
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
			if(this.calculationAlgorithmsMap.get(algorithmID) instanceof SingleCellObserverAlgorithm
					|| this.calculationAlgorithmsMap.get(algorithmID) instanceof TissueObserverAlgorithm){
				return true;
			}
		}
		return false;
	}


	public void registerCellsAtCalculationAlgorithms(GenericBag<AbstractCell> allCells){
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
			if(this.calculationAlgorithmsMap.get(calculationAlgorithmID) instanceof TissueObserverAlgorithm){
				TissueObserverAlgorithm alg = (TissueObserverAlgorithm)this.calculationAlgorithmsMap.get(calculationAlgorithmID);
				alg.addTissueObserver(associatedCalculationHandlerIds, new TissueObserver(){
					public void observedTissueHasChanged() {      
	               dataManager.observedEntityHasChanged(new EntityChangeEvent(){public EntityChangeEventType getEventType() { return EntityChangeEventType.SIMULATIONSTEPCHANGE; }});
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
	   instance = null;
   }
	
	public void callMeEverySimulationStep(){
		for(CalculationAlgorithm alg : this.toBeCalledAtEverySimulationStep){
			alg.newSimStep();
		}
	}
	
	public void sendRestartSimulationMessageToCalculationAlgorithms(){
		for(CalculationAlgorithm alg : this.calculationAlgorithmsMap.values()) alg.restartSimulation();
	}
	
	public void sendResetMessageToCalculationAlgorithms(){
		for(CalculationAlgorithm alg : this.calculationAlgorithmsMap.values()) alg.reset();
	}
	
	

}
