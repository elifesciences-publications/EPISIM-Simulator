package sim.app.episim.tissue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import sim.app.episim.CellType;
import sim.app.episim.EpisimProperties;
import sim.app.episim.datamonitoring.charts.ChartSetChangeListener;
import sim.app.episim.datamonitoring.dataexport.DataExportChangeListener;
import sim.app.episim.snapshot.SnapshotListener;
import sim.app.episim.snapshot.SnapshotObject;
import sim.app.episim.util.GenericBag;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.SimStateHack;
import sim.engine.Steppable;
import sim.engine.SimStateHack.TimeSteps;


public abstract class TissueType extends SimStateHack implements java.io.Serializable, ChartSetChangeListener, DataExportChangeListener, SnapshotListener{
	
	
	public enum SchedulePriority{		
		
		CELLS(1), TISSUE(2), STATISTICS(3), DATAMONITORING(4), OTHER(5);
		
		private int priority;
		
		private SchedulePriority(int priority){ this.priority = priority;}
		public int getPriority(){ return priority;}		
	}
	
	
	private List<Class<? extends CellType>> registeredCellTypes;
	private boolean guiMode = true;
	private boolean consoleInput = false;
	private GenericBag<CellType> allCells=new GenericBag<CellType>(3000); //all cells will be stored in this bag
	private boolean reloadedSnapshot = false;
	private TimeSteps timeStepsAfterSnapshotReload = null;
	
	public TissueType(long seed){ 
		super(new ec.util.MersenneTwisterFast(seed), new Schedule());
		registeredCellTypes = new ArrayList<Class<? extends CellType>>();
		consoleInput =  (EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CONSOLE_INPUT_PROP) != null 
					&& EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CONSOLE_INPUT_PROP).equals(EpisimProperties.ON_CONSOLE_INPUT_VAL));
	     
	   guiMode = ((EpisimProperties.getProperty(EpisimProperties.SIMULATOR_GUI_PROP) != null 
					&& EpisimProperties.getProperty(EpisimProperties.SIMULATOR_GUI_PROP).equals(EpisimProperties.ON_SIMULATOR_GUI_VAL) && consoleInput) 
					|| (EpisimProperties.getProperty(EpisimProperties.SIMULATOR_GUI_PROP)== null));
	   
	}
	
	protected boolean isGUIMode(){ return guiMode;}
	
	
	public abstract String getTissueName();
	
	
	public abstract List<Method> getParameters();
	
	public abstract List<Field> getContants();
	
	public List <Class<? extends CellType>> getRegisteredCellTypes(){
		return this.registeredCellTypes;
	}
	
	public void registerCellType(Class<? extends CellType> celltype){
		this.registeredCellTypes.add(celltype);
	}
	
	public GenericBag<CellType> getAllCells() {	return allCells; }
	
	public List<SnapshotObject> collectSnapshotObjects() {
		
		List<SnapshotObject> list = new LinkedList<SnapshotObject>();
		Iterator<CellType> iter = getAllCells().iterator();
		
		while(iter.hasNext()){
			list.add(new SnapshotObject(SnapshotObject.CELL, iter.next()));
		}
		
		list.add(new SnapshotObject(SnapshotObject.TIMESTEPS, new TimeSteps(schedule.getTime(), schedule.getSteps())));
		return list;
	}  
	
	public void addSnapshotLoadedCells(List<CellType> cells) { this.allCells.addAll(cells); }
	
	public void setReloadedSnapshot(boolean reloadedSnapshot) {	this.reloadedSnapshot = reloadedSnapshot; }
	
	protected boolean isReloadedSnapshot(){ return this.reloadedSnapshot; }
	
	public void setSnapshotTimeSteps(TimeSteps timeSteps){
		this.timeStepsAfterSnapshotReload = timeSteps;
	}
	
	public void cellIsDead(CellType cell) {
		this.allCells.remove(cell);		
	}
	
	 public void start() {

			super.start(timeStepsAfterSnapshotReload);
			
			if(!isGUIMode()){
		   	  Steppable consoleOutputSteppable = new Steppable(){

					public void step(SimState state) {

			         System.out.print("\r");	         
			         System.out.print("Simulation Step " + (state.schedule.getSteps()+1));
			         if(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_MAX_SIMULATION_STEPS_PROP) != null){
							long steps = Long.parseLong(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_MAX_SIMULATION_STEPS_PROP));
							System.out.print(" of " + steps);
							if((state.schedule.getSteps()+1) == steps){
								System.out.println("\n------------Simulation Stopped------------");
							}
			         }
		         }
		   		  
		   	  };
		   	  schedule.scheduleRepeating(consoleOutputSteppable, SchedulePriority.OTHER.getPriority(), 1);
		     }
	 }
	 

}
