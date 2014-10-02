package sim;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Semaphore;

import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.SimulationStateChangeListener;
import sim.app.episim.gui.EpisimGUIState;
import sim.app.episim.gui.EpisimSimulator;
import sim.app.episim.util.ClassLoaderChangeListener;
import sim.app.episim.util.GlobalClassLoader;
import sim.app.episim.util.SimulationTrigger;
import sim.engine.SimState;


public class SimStateServer implements SimulationStateChangeListener, ClassLoaderChangeListener{
	
	public enum EpisimSimulationState {PLAY, PAUSE, STOP, STEPWISE}
	
	private EpisimSimulationState state = EpisimSimulationState.STOP;
	private HashSet<SimulationStateChangeListener> simulationStateListeners;
	private static SimStateServer instance;
	
	private EpisimGUIState episimGUIState;
	
	private long simStepNumberAtStart = 0;
	
	private static Semaphore sem = new Semaphore(1);
	
	private HashSet<SimulationTrigger> registeredSimulationTrigger;
	
	private SimStateServer(){
		GlobalClassLoader.getInstance().addClassLoaderChangeListener(this);
		simulationStateListeners = new HashSet<SimulationStateChangeListener>();
		registeredSimulationTrigger = new HashSet<SimulationTrigger>();
	}
	
	public void registerSimulationTrigger(List<SimulationTrigger> trigger){
		for(SimulationTrigger t : trigger){
			registeredSimulationTrigger.add(t);
		}
	}
	
	public void executeSimulationTriggers(){
		for(SimulationTrigger trigger : registeredSimulationTrigger){
			if(trigger.isExecutable(getSimStepNumber()+1)){
				trigger.execute();
			}
		}
	}
	
	public EpisimGUIState getEpisimGUIState(){
		return episimGUIState;
	}
	
	public void setEpisimGUIState(EpisimGUIState guiState){
		this.episimGUIState = guiState;
		this.simStepNumberAtStart = 0;
	}
		
	public static SimStateServer getInstance(){
		if(instance==null){
			try{
	         sem.acquire();
	         instance = new SimStateServer();				
				sem.release();
         }
         catch (InterruptedException e){
	        ExceptionDisplayer.getInstance().displayException(e);
         }
				
		}
		return instance; 
	}
	
	public EpisimSimulationState getEpisimSimulationState(){ return this.state; }

	public void simulationWasPaused() {		
		if(state == EpisimSimulationState.PLAY){
			state = EpisimSimulationState.PAUSE;
			
		}
		else if(state == EpisimSimulationState.PAUSE || state == EpisimSimulationState.STEPWISE){ 
			state = EpisimSimulationState.PLAY;
			
		}
		else if(state== EpisimSimulationState.STOP){
			state =EpisimSimulationState.STEPWISE;
		}
		for(SimulationStateChangeListener actListener: simulationStateListeners) actListener.simulationWasPaused();
   }

	public void setSimStatetoPause(){ state= EpisimSimulationState.PAUSE;}
	
	public void simulationWasStarted() {
		if(state == EpisimSimulationState.STOP)state = EpisimSimulationState.PLAY;
		else if(state == EpisimSimulationState.PAUSE) state = EpisimSimulationState.STEPWISE;
		for(SimulationStateChangeListener actListener: simulationStateListeners) actListener.simulationWasStarted();
   }
	
	public long getSimStepNumber(){
		if(episimGUIState!=null){
			return (simStepNumberAtStart+episimGUIState.state.schedule.getSteps());
		}
		return simStepNumberAtStart;
	}
	
	public void setSimStepNumberAtStart(long simStepNumberAtStart){
		this.simStepNumberAtStart = simStepNumberAtStart;
	}

	public void simulationWasStopped() {
	   state = EpisimSimulationState.STOP;
	   for(SimulationStateChangeListener actListener: simulationStateListeners) actListener.simulationWasStopped();
   }
	
	public void addSimulationStateChangeListener(SimulationStateChangeListener listener){
		this.simulationStateListeners.add(listener);
	}
	public void removeSimulationStateChangeListener(SimulationStateChangeListener listener){
		this.simulationStateListeners.remove(listener);
	}
	public void reloadCurrentlyLoadedModel(){
		for(SimulationStateChangeListener listener:simulationStateListeners){
			if(listener instanceof EpisimSimulator){
				((EpisimSimulator)listener).reloadCurrentlyLoadedModel();
			}
		}
	}
	
	public void removeAllSimulationTrigger(){
		this.registeredSimulationTrigger.clear();
	}
	
   public void classLoaderHasChanged() {
	   instance = null;	   
   }	
	
}
