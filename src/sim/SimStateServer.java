package sim;

import sim.app.episim.SimulationStateChangeListener;


public class SimStateServer implements SimulationStateChangeListener{
	
	public enum SimState {PLAY, PAUSE, STOP, STEPWISE}
	
	private SimState state = SimState.STOP;
	
	private static final SimStateServer instance = new SimStateServer();
	
	private SimStateServer(){}
	
	public static SimStateServer getInstance(){ return instance; }
	
	public SimState getSimState(){ return this.state; }

	public void simulationWasPaused() {		
		if(state == SimState.PLAY){
			state = SimState.PAUSE;
			
		}
		else if(state == SimState.PAUSE || state == SimState.STEPWISE){ 
			state = SimState.PLAY;
			
		}
   }

	public void setSimStatetoPause(){ state= SimState.PAUSE;}
	
	public void simulationWasStarted() {
		if(state == SimState.STOP)state = SimState.PLAY;
		else if(state == SimState.PAUSE) state = SimState.STEPWISE;
		
   }

	public void simulationWasStopped() {
	   state = SimState.STOP;
	   
   }
	
}
