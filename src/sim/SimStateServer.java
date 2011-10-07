package sim;

import java.util.ArrayList;
import java.util.HashSet;

import sim.app.episim.SimulationStateChangeListener;


public class SimStateServer implements SimulationStateChangeListener{
	
	public enum SimState {PLAY, PAUSE, STOP, STEPWISE}
	
	private SimState state = SimState.STOP;
	private HashSet<SimulationStateChangeListener> simulationStateListeners;
	private static final SimStateServer instance = new SimStateServer();
	
	private SimStateServer(){
		simulationStateListeners = new HashSet<SimulationStateChangeListener>();
	}
	
	public static SimStateServer getInstance(){ return instance; }
	
	public SimState getSimState(){ return this.state; }

	public void simulationWasPaused() {		
		if(state == SimState.PLAY){
			state = SimState.PAUSE;
			
		}
		else if(state == SimState.PAUSE || state == SimState.STEPWISE){ 
			state = SimState.PLAY;
			
		}
		else if(state== SimState.STOP){
			state =SimState.STEPWISE;
		}
		for(SimulationStateChangeListener actListener: simulationStateListeners) actListener.simulationWasPaused();
   }

	public void setSimStatetoPause(){ state= SimState.PAUSE;}
	
	public void simulationWasStarted() {
		if(state == SimState.STOP)state = SimState.PLAY;
		else if(state == SimState.PAUSE) state = SimState.STEPWISE;
		for(SimulationStateChangeListener actListener: simulationStateListeners) actListener.simulationWasStarted();
   }

	public void simulationWasStopped() {
	   state = SimState.STOP;
	   for(SimulationStateChangeListener actListener: simulationStateListeners) actListener.simulationWasStopped();
   }
	
	public void addSimulationStateChangeListener(SimulationStateChangeListener listener){
		this.simulationStateListeners.add(listener);
	}
	public void removeSimulationStateChangeListener(SimulationStateChangeListener listener){
		this.simulationStateListeners.remove(listener);
	}
	
		
	
	
}
