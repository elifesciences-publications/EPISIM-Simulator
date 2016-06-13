package sim.app.episim.model;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters.ModelDimensionality;
import episiminterfaces.EpisimCellType;
import episiminterfaces.EpisimSimulationDisplay;
import episiminterfaces.monitoring.CannotBeMonitored;
import sim.app.episim.EpisimProperties;
import sim.app.episim.EpisimExceptionHandler;
import sim.app.episim.ModeServer;
import sim.app.episim.SimStateServer;
import sim.app.episim.datamonitoring.charts.ChartSetChangeListener;
import sim.app.episim.datamonitoring.dataexport.DataExportChangeListener;
import sim.app.episim.gui.EpisimDisplay3D;
import sim.app.episim.model.biomechanics.centerbased2d.newmodel.psoriasis.PsoriasisCenterBased2DModelGP;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.util.GenericBag;
import sim.app.episim.visualization.threedim.TissueCrossSectionPortrayal3D;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.SimStateHack;
import sim.engine.Steppable;
import sim.engine.SimStateHack.TimeSteps;


public abstract class AbtractTissue extends SimStateHack implements java.io.Serializable, ChartSetChangeListener, DataExportChangeListener{
	
	
	public enum SchedulePriority{		
		
		EXTRACELLULARFIELD(1),GLOBALBIOMECHANICS(2),CELLS(3), TISSUE(4), STATISTICS(5), DATAMONITORING(6),PNGWRITING(7), OTHER(8);
		
		private int priority;
		
		private SchedulePriority(int priority){ this.priority = priority;}
		public int getPriority(){ return priority;}		
	}
	
	
	private Map<EpisimCellType, Class<? extends AbstractCell>> registeredCellTypes;

	private GenericBag<AbstractCell> allCells=new GenericBag<AbstractCell>(3000); //all cells will be stored in this bag
	private TimeSteps timeStepsAfterSnapshotReload = null;
	
	public AbtractTissue(long seed){ 
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
	private long start = System.currentTimeMillis();
	 public void start() {

			super.start(timeStepsAfterSnapshotReload);
			
			schedule.scheduleRepeating(new Steppable(){
				public void step(SimState state) {
	            ModelController.getInstance().getBioMechanicalModelController().newSimStepGloballyFinished(SimStateServer.getInstance().getSimStepNumber(), state);
	            TissueCrossSectionPortrayal3D.setTissueCrossSectionDirty();
	          //  long end = System.currentTimeMillis();
	           // System.out.println("Time for sim step: "+ (end-start)+" ms");
	            //start = end;
	            EpisimBiomechanicalModelGlobalParameters globalParam = ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
	            if(globalParam instanceof PsoriasisCenterBased2DModelGP){
	            	((PsoriasisCenterBased2DModelGP)globalParam).newSimStep();
	            }
            }}, SchedulePriority.TISSUE.getPriority(), 1);
			
			schedule.scheduleRepeating(new Steppable(){
				public void step(SimState state) {
	           SimStateServer.getInstance().executeSimulationTriggers();
            }}, SchedulePriority.OTHER.getPriority(), 1);
			if(ModelController.getInstance().getModelDimensionality()==ModelDimensionality.THREE_DIMENSIONAL){
				EpisimSimulationDisplay display =SimStateServer.getInstance().getEpisimGUIState().getDisplay();
				if(display != null && display instanceof EpisimDisplay3D){
					final EpisimDisplay3D display3D = (EpisimDisplay3D) display;
					final Steppable rotationSteppable = display3D.getDisplayRotationSteppable();
					schedule.scheduleRepeating(new Steppable(){
						public void step(SimState state) {			           
							if(rotationSteppable != null)rotationSteppable.step(state);					
		            }}, SchedulePriority.OTHER.getPriority(), 1);
				}
			}
			schedule.scheduleRepeating(new Steppable(){
				public void step(SimState state) {
	            ModelController.getInstance().getBioMechanicalModelController().newGlobalSimStep(SimStateServer.getInstance().getSimStepNumber(), state);	            
            }}, SchedulePriority.GLOBALBIOMECHANICS.getPriority(), 1);
			
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
