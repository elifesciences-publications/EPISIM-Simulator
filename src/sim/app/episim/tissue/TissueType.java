package sim.app.episim.tissue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import episiminterfaces.EpisimCellType;
import episiminterfaces.monitoring.CannotBeMonitored;

import sim.SimStateServer;
import sim.app.episim.AbstractCell;
import sim.app.episim.EpisimProperties;
import sim.app.episim.ModeServer;
import sim.app.episim.datamonitoring.charts.ChartSetChangeListener;
import sim.app.episim.datamonitoring.dataexport.DataExportChangeListener;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.util.GenericBag;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.SimStateHack;
import sim.engine.Steppable;
import sim.engine.SimStateHack.TimeSteps;


public abstract class TissueType extends SimStateHack implements java.io.Serializable, ChartSetChangeListener, DataExportChangeListener{
	
	
	public enum SchedulePriority{		
		
		EXTRACELLULARFIELD(1),CELLS(2), TISSUE(3), STATISTICS(4), DATAMONITORING(5), OTHER(6);
		
		private int priority;
		
		private SchedulePriority(int priority){ this.priority = priority;}
		public int getPriority(){ return priority;}		
	}
	
	
	private Map<EpisimCellType, Class<? extends AbstractCell>> registeredCellTypes;

	private GenericBag<AbstractCell> allCells=new GenericBag<AbstractCell>(3000); //all cells will be stored in this bag
	private TimeSteps timeStepsAfterSnapshotReload = null;
	
	public TissueType(long seed){ 
		super(new ec.util.MersenneTwisterFast(seed), new Schedule());
		registeredCellTypes = new HashMap<EpisimCellType, Class<? extends AbstractCell>>();
		
	   
	}
	
	
	
	
	public abstract String getTissueName();
	
	
	public abstract List<Method> getParameters();
	
	public abstract List<Field> getContants();	
	
	public GenericBag<AbstractCell> getAllCells() {	return allCells; }
	
	public AbstractCell getCell(long id){
		for(AbstractCell cell: allCells){
			if(cell.getID()==id) return cell;
		}
		return null;
	}
	
	public void setSnapshotTimeSteps(TimeSteps timeSteps){
		this.timeStepsAfterSnapshotReload = timeSteps;
	}
	
	public void cellIsDead(AbstractCell cell) {
		this.allCells.remove(cell);		
	}
	
	 public void start() {

			super.start(timeStepsAfterSnapshotReload);			
			schedule.scheduleRepeating(new Steppable(){

				public void step(SimState state) {
	            ModelController.getInstance().getBioMechanicalModelController().newSimStepGloballyFinished(SimStateServer.getInstance().getSimStepNumber(), state);
            }}, SchedulePriority.TISSUE.getPriority(), 1);
			
			if(!ModeServer.guiMode()){
		   	  Steppable consoleOutputSteppable = new Steppable(){

					public void step(SimState state) {

			         System.out.print("\r");	         
			         System.out.print("Simulation Step " + (SimStateServer.getInstance().getSimStepNumber()+1));
			         if(EpisimProperties.getProperty(EpisimProperties.SIMULATION_MAX_STEPS_PROP) != null){
							long steps = Long.parseLong(EpisimProperties.getProperty(EpisimProperties.SIMULATION_MAX_STEPS_PROP));
							System.out.print(" of " + steps);
							if((SimStateServer.getInstance().getSimStepNumber()+1) == steps){
								System.out.println("\n------------Simulation Stopped------------");
							}
			         }
		         }
		   		  
		   	  };
		   	  schedule.scheduleRepeating(consoleOutputSteppable, SchedulePriority.OTHER.getPriority(), 1);
		     }
	 }
	 

}
