package sim.app.episim.tissue;



import sim.SimStateServer;
import sim.app.episim.AbstractCell;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.ModeServer;
import sim.app.episim.UniversalCell;
import sim.app.episim.datamonitoring.GlobalStatistics;
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

import com.lowagie.text.*;  
import com.lowagie.text.pdf.*;  

import episimexceptions.MissingObjectsException;
import episiminterfaces.CellDeathListener;
import episiminterfaces.EpisimBiomechanicalModel;
import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import episiminterfaces.EpisimCellType;
import episiminterfaces.EpisimDifferentiationLevel;
import episiminterfaces.monitoring.CannotBeMonitored;

public class Epidermis extends TissueType implements CellDeathListener
{

//	---------------------------------------------------------------------------------------------------------------------------------------------------
// CONSTANTS
//--------------------------------------------------------------------------------------------------------------------------------------------------- 
		
	public final String NAME ="Epidermis";
	
//---------------------------------------------------------------------------------------------------------------------------------------------------
// VARIABLES
//--------------------------------------------------------------------------------------------------------------------------------------------------- 
	

	
	// Percentage
	
	private transient List<EnhancedSteppable> chartSteppables = null;
	
	private transient List<EnhancedSteppable> dataExportSteppables = null;
	
	
	
	
	
//---------------------------------------------------------------------------------------------------------------------------------------------------
//--------------------------------------------------------------------------------------------------------------------------------------------------- 
	 
 /** Creates a EpidermisClass simulation with the given random number seed. */
 public Epidermis(long seed)
 {
     super(seed);
          
     EpisimCellType[] cellTypes = ModelController.getInstance().getCellBehavioralModelController().getAvailableCellTypes();
     if(cellTypes!= null){
   	  for(EpisimCellType epiType : cellTypes) this.registerCellType(epiType, UniversalCell.class); // Currently the same class is used for all modeled cell types
     }
     
     ChartController.getInstance().setChartMonitoredTissue(this);
     DataExportController.getInstance().setDataExportMonitoredTissue(this);
     ChartController.getInstance().registerChartSetChangeListener(this);
     DataExportController.getInstance().registerDataExportChangeListener(this);		
 }
 
 public void checkMemory(){
	 // Memory Management
    if (GlobalStatistics.getInstance().getActualNumberKCytes()>getAllCells().size()-50) // for safety -50
        getAllCells().resize(getAllCells().size()+500); // alloc 500 in advance
 }
 
 
 void printChartToPDF( JFreeChart chart, int width, int height, String fileName )
 {
     // call: printChartToPDF( EpidermisClass.createChart(), 500, 500, "test.pdf" );
	 try
	     {
	     Document document = new Document(new com.lowagie.text.Rectangle(width,height));
	     PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fileName));
	     document.addAuthor("EPISIM Simulator");
	     document.open();
	     PdfContentByte cb = writer.getDirectContent();
	     PdfTemplate tp = cb.createTemplate(width, height); 
	     Graphics2D g2 = tp.createGraphics(width, height, new DefaultFontMapper());
	     Rectangle2D rectangle2D = new Rectangle2D.Double(0, 0, width, height); 
	     chart.draw(g2, rectangle2D);
	     g2.dispose();
	     cb.addTemplate(tp, 0, 0);
	     document.close();
	     writer.close();
	     }
	 catch( Exception e )
	     {
	     e.printStackTrace();
	     }
 }
 
 
	private boolean seedingHasFinished = false;
 	private void seedInitiallyAvailableCells(){
		final ArrayList<UniversalCell> initialCellEnsemble = new ArrayList<UniversalCell>();
		if(ModeServer.guiMode() && ModelController.getInstance().isStoredSimStateLoaded()){
			if(SimStateServer.getInstance().getEpisimGUIState()!= null 
					&& SimStateServer.getInstance().getEpisimGUIState().getMainGUIComponent() != null
					&& SimStateServer.getInstance().getEpisimGUIState().getMainGUIComponent() instanceof Frame){				
				EpisimProgressWindowCallback cb = new EpisimProgressWindowCallback() {											
					public void taskHasFinished() {
						for(UniversalCell cell : initialCellEnsemble){
							 if(!ModeServer.useMonteCarloSteps()){
								 
									Stoppable stoppable = schedule.scheduleRepeating(cell, SchedulePriority.CELLS.getPriority(), 1);
									cell.setStoppable(stoppable);
							 }
						 }
						seedingHasFinished = true;
					}
					public void executeTask() {
						initialCellEnsemble.addAll(ModelController.getInstance().getInitialCellEnsemble());						
					}
				};
				seedingHasFinished = false;
				EpisimProgressWindow.showProgressWindowForTask((Frame)SimStateServer.getInstance().getEpisimGUIState().getMainGUIComponent(), "Load initial simulation state...", cb);
				while(!seedingHasFinished){
					/* wait, Thread sleep important, otherwise deadlock */ 
					try{
	            Thread.sleep(50);
					}
					catch (InterruptedException e){
		            ExceptionDisplayer.getInstance().displayException(e);
					} 
					}
			}
		}
		else{
			initialCellEnsemble.addAll(ModelController.getInstance().getInitialCellEnsemble());
			for(UniversalCell cell : initialCellEnsemble){
				 if(!ModeServer.useMonteCarloSteps()){
					 
						Stoppable stoppable = schedule.scheduleRepeating(cell, SchedulePriority.CELLS.getPriority(), 1);
						cell.setStoppable(stoppable);
				 }
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
		
		if(this.dataExportSteppables != null){
			for(EnhancedSteppable steppable: this.dataExportSteppables){
				if(steppable.getInterval() >1)schedule.scheduleOnce(0,SchedulePriority.DATAMONITORING.getPriority(), steppable);
		   	schedule.scheduleRepeating(steppable, SchedulePriority.DATAMONITORING.getPriority(), steppable.getInterval());
		   }
		}
		
		GlobalStatistics.getInstance().reset(true);
			     
	   ModelController.getInstance().getBioMechanicalModelController().resetCellField();
	   getAllCells().clear();
	   seedInitiallyAvailableCells();
	   
	   if(ModeServer.useMonteCarloSteps()){
	     EnhancedSteppable mcSteppable = getMonteCarloStepSteppable();
	     schedule.scheduleRepeating(mcSteppable, SchedulePriority.CELLS.getPriority(), mcSteppable.getInterval());
	   }
	   ExtraCellularDiffusionField[] fields = ModelController.getInstance().getExtraCellularDiffusionController().getAllExtraCellularDiffusionFields(new ExtraCellularDiffusionField[ModelController.getInstance().getExtraCellularDiffusionController().getNumberOfFields()]);
	   for(ExtraCellularDiffusionField field : fields){	   	
	   	schedule.scheduleRepeating(field, field.getInterval());
	   }
	     
	   EnhancedSteppable globalStatisticsSteppable = GlobalStatistics.getInstance().getUpdateSteppable(getAllCells());
	   schedule.scheduleRepeating(globalStatisticsSteppable, SchedulePriority.STATISTICS.getPriority(), globalStatisticsSteppable.getInterval());
        
 }

	public void removeCells(GeneralPath path){
		ModelController.getInstance().getBioMechanicalModelController().removeCellsInWoundArea(path);		
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




