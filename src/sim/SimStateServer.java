package sim;

import java.util.ArrayList;
import java.util.HashSet;

import sim.app.episim.SimulationStateChangeListener;
import sim.app.episim.gui.EpisimGUIState;
import sim.engine.SimState;


public class SimStateServer implements SimulationStateChangeListener{
	
	public enum EpisimSimulationState {PLAY, PAUSE, STOP, STEPWISE}
	
	private EpisimSimulationState state = EpisimSimulationState.STOP;
	private HashSet<SimulationStateChangeListener> simulationStateListeners;
	private static final SimStateServer instance = new SimStateServer();
	
	private EpisimGUIState episimGUIState;
	
	private long simStepNumberAtStart = 0;
	
	private SimStateServer(){
		simulationStateListeners = new HashSet<SimulationStateChangeListener>();
	}
	
	public EpisimGUIState getEpisimGUIState(){
		return episimGUIState;
	}
	
	public void setEpisimGUIState(EpisimGUIState guiState){
		this.episimGUIState = guiState;
		this.simStepNumberAtStart = 0;
	}
		
	public static SimStateServer getInstance(){ return instance; }
	
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
	
		
	
	
}
