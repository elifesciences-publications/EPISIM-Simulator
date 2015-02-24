package sim.app.episim.tissue;



import sim.SimStateServer;
import sim.app.episim.AbstractCell;
import sim.app.episim.EpisimProperties;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.ModeServer;
import sim.app.episim.UniversalCell;
import sim.app.episim.datamonitoring.GlobalStatistics;
import sim.app.episim.datamonitoring.calc.CalculationAlgorithmServer;
import sim.app.episim.datamonitoring.charts.ChartController;
import sim.app.episim.datamonitoring.charts.DefaultCharts;
import sim.app.episim.datamonitoring.dataexport.DataExportController;
import sim.app.episim.gui.EpisimProgressWindow;
import sim.app.episim.gui.EpisimProgressWindow.EpisimProgressWindowCallback;
import sim.app.episim.model.biomechanics.AbstractMechanical2DModel;
import sim.app.episim.model.biomechanics.AbstractMechanicalModel;
import sim.app.episim.model.biomechanics.centerbased.CenterBasedMechanicalModel;
import sim.app.episim.model.biomechanics.centerbased.CenterBasedMechanicalModelGP;
import sim.app.episim.model.biomechanics.vertexbased.calc.CellPolygonCalculator;
import sim.app.episim.model.biomechanics.vertexbased.geom.CellPolygon;
import sim.app.episim.model.biomechanics.vertexbased.geom.CellPolygonNetworkBuilder;
import sim.app.episim.model.controller.BiomechanicalModelController;
import sim.app.episim.model.controller.CellBehavioralModelController;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.diffusion.ExtraCellularDiffusionField;
import sim.app.episim.model.diffusion.ExtraCellularDiffusionField2D;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters;
import sim.app.episim.persistence.SimulationStateFile;
import sim.app.episim.util.CellEllipseIntersectionCalculationRegistry;
import sim.app.episim.util.EnhancedSteppable;
import sim.app.episim.util.GenericBag;
import sim.app.episim.util.TysonRungeCuttaCalculator;
import sim.engine.*;
import sim.engine.SimStateHack.TimeSteps;
import sim.util.*;
import sim.field.continuous.*;

//Charts
import org.jfree.chart.JFreeChart;














//PDF Writer + ELSE
import java.awt.*; 
import java.awt.geom.*; 
//PDF Writer
import java.io.*;       
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Semaphore;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.lowagie.text.*;  
import com.lowagie.text.pdf.*;  

import episimexceptions.MissingObjectsException;
import episiminterfaces.CellDeathListener;
import episiminterfaces.EpisimBiomechanicalModel;
import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import episiminterfaces.EpisimCellType;
import episiminterfaces.EpisimDifferentiationLevel;
import episiminterfaces.monitoring.CannotBeMonitored;

public class UniversalTissue extends TissueType implements CellDeathListener
{

//	---------------------------------------------------------------------------------------------------------------------------------------------------
// CONSTANTS
//--------------------------------------------------------------------------------------------------------------------------------------------------- 
		
	public final String NAME ="Tissue";
	
//---------------------------------------------------------------------------------------------------------------------------------------------------
// VARIABLES
//--------------------------------------------------------------------------------------------------------------------------------------------------- 
	

	
	// Percentage
	
	private transient List<EnhancedSteppable> chartSteppables = null;
	private transient List<EnhancedSteppable> chartPNGWriterSteppables = null;
	
	private transient List<EnhancedSteppable> dataExportSteppables = null;
	
	private transient double globalColorMode = 1;
	
	
	
//---------------------------------------------------------------------------------------------------------------------------------------------------
//--------------------------------------------------------------------------------------------------------------------------------------------------- 
	 
 /** Creates a EpidermisClass simulation with the given random number seed. */
 public UniversalTissue(long seed)
 {
     super(seed);               
     ChartController.getInstance().setChartMonitoredTissue(this);
     DataExportController.getInstance().setDataExportMonitoredTissue(this);
     ChartController.getInstance().registerChartSetChangeListener(this);
     DataExportController.getInstance().registerDataExportChangeListener(this);		
 }
 
 public void checkMemory(){
	 // Memory Management
    if (getAllCells().size()>getAllCells().size()-50) // for safety -50
        getAllCells().resize(getAllCells().size()+500); // alloc 500 in advance
 }
 
 
 private void seedInitiallyAvailableCells(){
		final ArrayList<UniversalCell> initialCellEnsemble = new ArrayList<UniversalCell>();
		if(ModeServer.guiMode() && ModelController.getInstance().isStoredSimStateLoaded()){
			if(SimStateServer.getInstance().getEpisimGUIState()!= null 
					&& SimStateServer.getInstance().getEpisimGUIState().getMainGUIComponent() != null
					&& SimStateServer.getInstance().getEpisimGUIState().getMainGUIComponent() instanceof Frame
					&& !(EpisimProperties.getProperty(EpisimProperties.SIMULATION_AUTOSTART_AND_STOP_PROP) != null &&EpisimProperties.getProperty(EpisimProperties.SIMULATION_AUTOSTART_AND_STOP_PROP).equals(EpisimProperties.ON))
					){	
				JOptionPane.showMessageDialog((Frame)SimStateServer.getInstance().getEpisimGUIState().getMainGUIComponent(), 
						"Starting the simulation requires retrieval of the tissue simulation snapshot you loaded!\nDuring this time EPISIM Simulator is not responding.\nSimulation starts automatically after successful processing of the tissue simulation snapshot.", 
						"Simulation Start", JOptionPane.INFORMATION_MESSAGE);
									
			}
		}
	
		initialCellEnsemble.addAll(ModelController.getInstance().getInitialCellEnsemble());
		for(UniversalCell cell : initialCellEnsemble){
			 if(!ModeServer.useMonteCarloSteps()){					 
				Stoppable stoppable = schedule.scheduleRepeating(cell, SchedulePriority.CELLS.getPriority(), 1);
				cell.setStoppable(stoppable);
			 }
		 }
	}
		
	private void oneMonteCarloSimStep(SimState state){
		int numberOfCellsAtStart = getAllCells().size();
		if(numberOfCellsAtStart >0){
			//System.out.println("------------Number of Cells at Start: " + numberOfCellsAtStart);
			for(int i = 0; i < numberOfCellsAtStart; i++){
				int actualNumberOfCells = getAllCells().size();
				getAllCells().get(random.nextInt(actualNumberOfCells)).step(state);
			}
		}
	}
	
 
	private EnhancedSteppable getMonteCarloStepSteppable(){
		EnhancedSteppable steppable = new EnhancedSteppable(){

			
         public void step(SimState state) {
	         oneMonteCarloSimStep(state);
	         
         }

         public double getInterval() {
	         return 1;
         }
			
		};
		return steppable;
	}
	
 
 
 public void start() {

		super.start();
		ChartController.getInstance().newSimulationRun();
		DataExportController.getInstance().newSimulationRun();
		
		if(this.chartSteppables != null){
			for(EnhancedSteppable steppable: this.chartSteppables){
				if(steppable.getInterval() >1)schedule.scheduleOnce(0,SchedulePriority.DATAMONITORING.getPriority(), steppable);
		   	schedule.scheduleRepeating(steppable, SchedulePriority.DATAMONITORING.getPriority(), steppable.getInterval());
		   }
		}
		if(this.chartPNGWriterSteppables != null){
			for(EnhancedSteppable steppable: this.chartPNGWriterSteppables){
				if(steppable.getInterval() >1)schedule.scheduleOnce(0,SchedulePriority.PNGWRITING.getPriority(), steppable);
		   	schedule.scheduleRepeating(steppable, SchedulePriority.PNGWRITING.getPriority(), steppable.getInterval());
		   }
		}
		schedule.scheduleRepeating(new Steppable(){			
         public void step(SimState state) {
	         CalculationAlgorithmServer.getInstance().callMeEverySimulationStep();
         }},SchedulePriority.STATISTICS.getPriority(), 1);
		
		if(this.dataExportSteppables != null){
			for(EnhancedSteppable steppable: this.dataExportSteppables){
				//if(steppable.getInterval() >1)
					schedule.scheduleOnce(0,SchedulePriority.DATAMONITORING.getPriority(), steppable);
		   	schedule.scheduleRepeating(steppable, SchedulePriority.DATAMONITORING.getPriority(), steppable.getInterval());
		   }
		}
		if(SimulationStateFile.getTissueExportPath() != null && EpisimProperties.getProperty(EpisimProperties.SIMULATION_SNAPSHOT_SAVE_FREQUENCY) !=null)
		{
			EnhancedSteppable steppable = getTissueSimulationSnaphshotSaveSteppable();
			schedule.scheduleRepeating(steppable, SchedulePriority.DATAMONITORING.getPriority(), steppable.getInterval());			
		}
		if(EpisimProperties.getProperty(EpisimProperties.SIMULATION_PNG_PATH) != null && EpisimProperties.getProperty(EpisimProperties.SIMULATION_PNG_PRINT_FREQUENCY)!=null){
			File pngSnaphotPath = new File(EpisimProperties.getProperty(EpisimProperties.SIMULATION_PNG_PATH));
			if(pngSnaphotPath.exists() && pngSnaphotPath.isDirectory()){
				EnhancedSteppable steppable = getTissueVisualizationSnaphshotSaveSteppable();
				schedule.scheduleRepeating(steppable, SchedulePriority.PNGWRITING.getPriority(), steppable.getInterval());
			}
		}
		
		if((EpisimProperties.getProperty(EpisimProperties.DISPLAY_COLORMODE_FREQ) != null
				&& EpisimProperties.getProperty(EpisimProperties.DISPLAY_COLORMODE_MIN) != null
				&& EpisimProperties.getProperty(EpisimProperties.DISPLAY_COLORMODE_MAX) != null)){
				EnhancedSteppable steppable = getTissueVisualizationColorChangeSteppable();
				schedule.scheduleRepeating(steppable, SchedulePriority.OTHER.getPriority(), steppable.getInterval());
		}
		
		
		
		GlobalStatistics.getInstance().reset(true);
			     
	   ModelController.getInstance().getBioMechanicalModelController().resetCellField();
	   getAllCells().clear();
	   seedInitiallyAvailableCells();
	   
	   if(ModeServer.useMonteCarloSteps()){
	     EnhancedSteppable mcSteppable = getMonteCarloStepSteppable();
	     schedule.scheduleRepeating(mcSteppable, SchedulePriority.CELLS.getPriority(), mcSteppable.getInterval());
	   }
	   
	   boolean parallelizationOn = EpisimProperties.getProperty(EpisimProperties.SIMULATION_PARALLELIZATION) == null || EpisimProperties.getProperty(EpisimProperties.SIMULATION_PARALLELIZATION).equalsIgnoreCase(EpisimProperties.ON);
	   /*
	    * TODO: Parallelized Diff Field Sim Version
	    */
	   if(ModelController.getInstance().getExtraCellularDiffusionController().getNumberOfFields()>0){
	   	if(parallelizationOn){
			   EnhancedSteppable diffFieldsSimSteppable = ModelController.getInstance().getExtraCellularDiffusionController().getDiffusionFieldsSimulationSteppable();
			   schedule.scheduleRepeating(diffFieldsSimSteppable, SchedulePriority.EXTRACELLULARFIELD.getPriority(),diffFieldsSimSteppable.getInterval());
	   	}
	   	else{
	   		ExtraCellularDiffusionField[] fields = ModelController.getInstance().getExtraCellularDiffusionController().getAllExtraCellularDiffusionFields(new ExtraCellularDiffusionField[ModelController.getInstance().getExtraCellularDiffusionController().getNumberOfFields()]);
	 		   for(ExtraCellularDiffusionField field : fields){	   	
	 		   	schedule.scheduleRepeating(field, SchedulePriority.EXTRACELLULARFIELD.getPriority(),field.getInterval());
	 		   }
	   	}
		   
	   }
	   
	   EnhancedSteppable globalStatisticsSteppable = GlobalStatistics.getInstance().getUpdateSteppable(getAllCells());
	   schedule.scheduleRepeating(globalStatisticsSteppable, SchedulePriority.STATISTICS.getPriority(), globalStatisticsSteppable.getInterval());
	 
        
 }

	public void removeCells(GeneralPath path){
		ModelController.getInstance().getBioMechanicalModelController().removeCellsInWoundArea(path);		
	}
	
	public void simulateASingleDataExtractionStepForDataExport(){
		DataExportController.getInstance().newSimulationRun();
		SimStateServer.getInstance().simulationWasStarted();
		ModelController.getInstance().getInitialCellEnsemble();
		if(this.dataExportSteppables != null){
			for(EnhancedSteppable steppable: this.dataExportSteppables){
				steppable.step(this);				
		   }
		}
		SimStateServer.getInstance().simulationWasStopped();
	}


	private EnhancedSteppable getTissueSimulationSnaphshotSaveSteppable(){
		final double frequency = Double.parseDouble(EpisimProperties.getProperty(EpisimProperties.SIMULATION_SNAPSHOT_SAVE_FREQUENCY));
		EnhancedSteppable steppable = new EnhancedSteppable() {			
			public void step(SimState state) {
				if(SimStateServer.getInstance().getEpisimGUIState() != null){
					SimStateServer.getInstance().getEpisimGUIState().saveTissueSimulationSnapshot(true);
				}				
			}			
			public double getInterval() {				
				return frequency;
			}
		};
		return steppable;
	}

	private EnhancedSteppable getTissueVisualizationSnaphshotSaveSteppable(){
		final double frequency = Double.parseDouble(EpisimProperties.getProperty(EpisimProperties.SIMULATION_PNG_PRINT_FREQUENCY));
		EnhancedSteppable steppable = new EnhancedSteppable() {
			
			
			public void step(SimState state) {
				if(SimStateServer.getInstance().getEpisimGUIState() != null){
					final boolean pngSequenceMode = EpisimProperties.getProperty(EpisimProperties.SIMULATION_PNG_PRINT_SEQUENCE)!=null;
					final double pngSeqLength= EpisimProperties.getProperty(EpisimProperties.SIMULATION_PNG_PRINT_SEQUENCE)!=null
							? Double.parseDouble(EpisimProperties.getProperty(EpisimProperties.SIMULATION_PNG_PRINT_SEQUENCE))
									:1.0d;
					final double incr = EpisimProperties.getProperty(EpisimProperties.DISPLAY_COLORMODE_INCR)!=null
									? Double.parseDouble(EpisimProperties.getProperty(EpisimProperties.DISPLAY_COLORMODE_INCR))
											:1.0d;
					final long printDelayInMs= EpisimProperties.getProperty(EpisimProperties.SIMULATION_PNG_PRINT_DELAY_IN_MS)!=null
							? Long.parseLong(EpisimProperties.getProperty(EpisimProperties.SIMULATION_PNG_PRINT_DELAY_IN_MS))
									:1000l;
							
							
					if(pngSequenceMode){
							globalColorMode+=incr;
							SimStateServer.getInstance().getEpisimGUIState().changeCellColoringMode(globalColorMode);							
					}
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {							
							SimStateServer.getInstance().getEpisimGUIState().takeVisualizationSnapshot();							
						}
					});					
					if(pngSequenceMode){								
								for(int i = 1; i < pngSeqLength; i++){
									final int n = i;
									schedule.scheduleOnceIn(i,
											new Steppable(){
												public void step(SimState state) {
													if(n==(pngSeqLength-1)) globalColorMode=1;
													else{
														globalColorMode+=incr;																
													}
													SimStateServer.getInstance().getEpisimGUIState().changeCellColoringMode(globalColorMode);
													
													SwingUtilities.invokeLater(new Runnable() {
														public void run() {
															try{
	                                             Thread.sleep(printDelayInMs);
                                             }
                                             catch (InterruptedException e){
	                                           ExceptionDisplayer.getInstance().displayException(e); /*  Race Conditions Workaround */
                                             }
															SimStateServer.getInstance().getEpisimGUIState().takeVisualizationSnapshot();
													
														}
													});
				                        }}
												, SchedulePriority.PNGWRITING.getPriority());
								}
							}
				}				
			}			
			
			public double getInterval() {				
				return frequency;
			}
		};
		return steppable;
	}
	
	private EnhancedSteppable getTissueVisualizationColorChangeSteppable(){
		final double min = EpisimProperties.getProperty(EpisimProperties.DISPLAY_COLORMODE_MIN) != null 
									? Double.parseDouble(EpisimProperties.getProperty(EpisimProperties.DISPLAY_COLORMODE_MIN))
											:1.0d;
		final double max = EpisimProperties.getProperty(EpisimProperties.DISPLAY_COLORMODE_MAX) != null
									? Double.parseDouble(EpisimProperties.getProperty(EpisimProperties.DISPLAY_COLORMODE_MAX))
											:1.0d;
		final double freq = EpisimProperties.getProperty(EpisimProperties.DISPLAY_COLORMODE_FREQ)!= null
									? Double.parseDouble(EpisimProperties.getProperty(EpisimProperties.DISPLAY_COLORMODE_FREQ))
											:1.0d;
		
		final double incr = EpisimProperties.getProperty(EpisimProperties.DISPLAY_COLORMODE_INCR)!=null
									? Double.parseDouble(EpisimProperties.getProperty(EpisimProperties.DISPLAY_COLORMODE_INCR))
											:1.0d;
		
	
		EnhancedSteppable steppable = new EnhancedSteppable() {
			private double val = 1.0;
			
			public void step(SimState state) {
				if(SimStateServer.getInstance().getEpisimGUIState() != null){
				
					if(val< min || val>=max) val=min;
						else{
							val+=incr;
						}
						SimStateServer.getInstance().getEpisimGUIState().changeCellColoringMode(val);				
				}				
			}			
			
			public double getInterval() {				
				return freq;
			}
		};
		return steppable;
	}
	
	
	
//---------------------------------------------------------------------------------------------------------------------------------------------------
//GETTER-METHODS
//--------------------------------------------------------------------------------------------------------------------------------------------------- 
	

	@CannotBeMonitored
	public String getTissueName() {return NAME;}
	
	
	
	
//---------------------------------------------------------------------------------------------------------------------------------------------------
//SETTER-METHODS
//--------------------------------------------------------------------------------------------------------------------------------------------------- 
		
		
		
	
	
	//	complex-Methods------------------------------------------------------------------------------------------------------------------
	
	
	public List<Method> getParameters() {
		List<Method> methods = new ArrayList<Method>();
		 
		for(Method m : ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getClass().getMethods()){
			if((m.getName().startsWith("get") || m.getName().startsWith("is"))&& m.getAnnotation(CannotBeMonitored.class)==null) methods.add(m);
		}
		for(Method m : ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().getClass().getMethods()){
			if((m.getName().startsWith("get") || m.getName().startsWith("is"))&& m.getAnnotation(CannotBeMonitored.class)==null) methods.add(m);
		}
		for(Method m : this.getClass().getMethods()){
			if((m.getName().startsWith("get") || m.getName().startsWith("is"))&& m.getAnnotation(CannotBeMonitored.class)==null) methods.add(m);
		}	   
		return methods;
	}
	
	public List<Field> getContants() {	
		List<Field> fields = new ArrayList<Field>();
		for(Field field : ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getClass().getFields()){
	   		if(!field.getDeclaringClass().isInterface()) fields.add(field);
		}
		for(Field field : ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().getClass().getFields()){
   		if(!field.getDeclaringClass().isInterface()) fields.add(field);
		}
		
		return fields;
	}



	public void chartSetHasChanged() {

		try{
			if(getAllCells() != null
					&& ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters() != null
					&& ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters() != null){
		      this.chartSteppables = ChartController.getInstance().getChartSteppablesOfActLoadedChartSet(getAllCells(), new Object[]{
		      		ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters(), 
		      		ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters(), 
		      		this});
		      this.chartPNGWriterSteppables = ChartController.getInstance().getPNGWriterSteppablesOfActLoadedChartSet();
		   }
      }
      catch (MissingObjectsException e){
	     ExceptionDisplayer.getInstance().displayException(e);
      }
		}



	public void cellIsDead(AbstractCell cell) {
		super.cellIsDead(cell);
	}
	
	


	public void dataExportHasChanged() {

	   try{
	   	if(getAllCells() != null 
					&& ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters() != null
					&& ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters() != null){
	      this.dataExportSteppables = DataExportController.getInstance().getDataExportSteppablesOfActLoadedDataExport(getAllCells(), new Object[]{
	      	ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters(), 
	      	ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters(), 
	      	  	this});
	   	}
      }
      catch (MissingObjectsException e){
      	 ExceptionDisplayer.getInstance().displayException(e);
      }
	   
   }
}




