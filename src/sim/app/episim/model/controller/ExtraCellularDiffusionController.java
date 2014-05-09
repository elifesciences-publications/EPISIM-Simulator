package sim.app.episim.model.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import episiminterfaces.EpisimBiomechanicalModelGlobalParameters.ModelDimensionality;
import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import episiminterfaces.EpisimDiffusionFieldConfiguration;
import sim.app.episim.EpisimProperties;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.model.diffusion.ExtraCellularDiffusionField;
import sim.app.episim.model.diffusion.ExtraCellularDiffusionField2D;
import sim.app.episim.model.diffusion.ExtraCellularDiffusionFieldBCConfigRW;
import sim.app.episim.model.diffusion.ExtracellularDiffusionFieldBCConfig2D;
import sim.app.episim.model.diffusion.ExtracellularDiffusionFieldBCConfig3D;
import sim.app.episim.model.diffusion.TestDiffusionFieldConfiguration;
import sim.app.episim.model.initialization.ExtraCellularDiffusionInitializer;
import sim.app.episim.persistence.SimulationStateData;
import sim.app.episim.tissue.TissueController;
import sim.app.episim.util.ClassLoaderChangeListener;
import sim.app.episim.util.EnhancedSteppable;
import sim.app.episim.util.GlobalClassLoader;
import sim.engine.SimState;
import sim.engine.Steppable;


public class ExtraCellularDiffusionController implements ClassLoaderChangeListener{
	
	public enum DiffusionFieldCrossSectionMode{		
		X_Y_PLANE("X-Y-Plane"),
		X_Z_PLANE("X-Z-Plane"),
		Y_Z_PLANE("Y-Z-Plane");
		
		private String name;
		DiffusionFieldCrossSectionMode(String name){
			this.name = name;
		
		}		
		public String toString(){ return this.name; }
	}
	
	private DiffusionFieldCrossSectionMode selectedDiffusionFieldCrossSectionMode = DiffusionFieldCrossSectionMode.X_Y_PLANE;
	
	private double diffusionFieldCrossSectionCoordinateInMikron = 0;
	
	
	private static ExtraCellularDiffusionController instance= new ExtraCellularDiffusionController();
	
	private HashMap<String, ExtraCellularDiffusionField> extraCellularFieldMap;
	private HashMap<String, ExtracellularDiffusionFieldBCConfig2D> extraCellularFieldBCConfigMap;
	
	private static Semaphore sem = new Semaphore(1);
	
	private ExtraCellularDiffusionController(){
		GlobalClassLoader.getInstance().addClassLoaderChangeListener(this);
		extraCellularFieldMap = new HashMap<String, ExtraCellularDiffusionField>();
		extraCellularFieldBCConfigMap = new HashMap<String, ExtracellularDiffusionFieldBCConfig2D>();
		buildExtraCellularFieldBCConfigMap();
		
	}	
	
	private void buildExtraCellularFieldBCConfigMap(){
		EpisimDiffusionFieldConfiguration[] episimExtraCellularDiffusionFieldsConfigurations =getEpisimDiffusionFieldConfigurations();
		if(episimExtraCellularDiffusionFieldsConfigurations!= null){
			extraCellularFieldBCConfigMap.clear();
			for(EpisimDiffusionFieldConfiguration config: episimExtraCellularDiffusionFieldsConfigurations){
				if(ModelController.getInstance().getModelDimensionality() == ModelDimensionality.TWO_DIMENSIONAL){
					extraCellularFieldBCConfigMap.put(config.getDiffusionFieldName(), new ExtracellularDiffusionFieldBCConfig2D());
				}
				else if(ModelController.getInstance().getModelDimensionality() == ModelDimensionality.THREE_DIMENSIONAL){
					extraCellularFieldBCConfigMap.put(config.getDiffusionFieldName(), new ExtracellularDiffusionFieldBCConfig3D());
				}
			}
			ExtraCellularDiffusionFieldBCConfigRW configRW = new ExtraCellularDiffusionFieldBCConfigRW(ModelController.getInstance().getCellBehavioralModelController().getActLoadedModelFile());
			try{
	          configRW.readBCConfigs(getExtraCellularFieldBCConfigurationsMap());
	      }
	      catch (Exception e){
	      	ExceptionDisplayer.getInstance().displayException(e);
	      }
		}
	}
	
	public ExtracellularDiffusionFieldBCConfig2D getExtraCellularFieldBCConfiguration(String fieldName){
		if(fieldName != null && this.extraCellularFieldBCConfigMap != null && this.extraCellularFieldBCConfigMap.containsKey(fieldName)){
			return this.extraCellularFieldBCConfigMap.get(fieldName);
		}
		return null;
	}
	
	public HashMap<String, ExtracellularDiffusionFieldBCConfig2D> getExtraCellularFieldBCConfigurationsMap(){
		return this.extraCellularFieldBCConfigMap;		
	}
	
	public int getNumberOfEpisimExtraCellularDiffusionFieldConfigurations(){
		EpisimDiffusionFieldConfiguration[] episimExtraCellularDiffusionFieldsConfigurations =getEpisimDiffusionFieldConfigurations();
		return episimExtraCellularDiffusionFieldsConfigurations != null ? episimExtraCellularDiffusionFieldsConfigurations.length: 0;
	}
	
	public EpisimDiffusionFieldConfiguration[] getEpisimExtraCellularDiffusionFieldsConfigurations(){
		return getEpisimDiffusionFieldConfigurations();
	}
	
	public EpisimDiffusionFieldConfiguration getEpisimExtraCellularDiffusionFieldsConfiguration(String fieldName){
		EpisimDiffusionFieldConfiguration[] episimExtraCellularDiffusionFieldsConfigurations =getEpisimDiffusionFieldConfigurations();
		for(int i = 0; i < episimExtraCellularDiffusionFieldsConfigurations.length; i++){
			if(episimExtraCellularDiffusionFieldsConfigurations[i].getDiffusionFieldName().equals(fieldName)){
				return episimExtraCellularDiffusionFieldsConfigurations[i];
			}
		}
		return null;
	}
	
	public <T extends ExtraCellularDiffusionField> T[] getAllExtraCellularDiffusionFields(T[] fieldArray){
		if(fieldArray != null && fieldArray.length == getNumberOfFields()) return extraCellularFieldMap.values().toArray(fieldArray);
		else throw new IllegalArgumentException("fieldArray is null or size does not fit");
	}
	
	public int getNumberOfFields(){ 
		EpisimDiffusionFieldConfiguration[] episimExtraCellularDiffusionFieldsConfigurations =getEpisimDiffusionFieldConfigurations();
		return episimExtraCellularDiffusionFieldsConfigurations != null ? episimExtraCellularDiffusionFieldsConfigurations.length: 0;
	} 
	
	public ExtraCellularDiffusionField getExtraCellularDiffusionField(String name){
		return this.extraCellularFieldMap.get(name);
	}
	
	public void setExtraCellularFieldMap(HashMap<String, ExtraCellularDiffusionField> extraCellularFieldMap){
		this.extraCellularFieldMap = extraCellularFieldMap;
	}
	
	
	protected static ExtraCellularDiffusionController getInstance(){
		if(instance==null){
			try{
	         sem.acquire();
	         instance = new ExtraCellularDiffusionController();				
				sem.release();
         }
         catch (InterruptedException e){
	        ExceptionDisplayer.getInstance().displayException(e);
         }
				
		}
		return instance;
	}
	
	public ExtraCellularDiffusionInitializer getExtraCellularDiffusionInitializer(){
		return new ExtraCellularDiffusionInitializer();
	}
	public ExtraCellularDiffusionInitializer getExtraCellularDiffusionInitializer(SimulationStateData simulationStateData){
		return new ExtraCellularDiffusionInitializer(simulationStateData);
	}
	
	private EpisimDiffusionFieldConfiguration[] getEpisimDiffusionFieldConfigurations(){
		if(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_DIFFUSION_FIELD_TESTMODE)!= null &&
				EpisimProperties.getProperty(EpisimProperties.SIMULATOR_DIFFUSION_FIELD_TESTMODE).equals(EpisimProperties.ON)){
			return new EpisimDiffusionFieldConfiguration[]{new TestDiffusionFieldConfiguration()};
		}
		else{
			EpisimCellBehavioralModelGlobalParameters globalParameters = ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters();
			if(globalParameters != null){
				return globalParameters.getAllExtraCellularDiffusionFieldConfigurations();
			}
		}
	
		return new EpisimDiffusionFieldConfiguration[0];
	}
	
	
	protected void newCellBehavioralModelLoaded(){
		extraCellularFieldMap = new HashMap<String, ExtraCellularDiffusionField>();
		extraCellularFieldBCConfigMap = new HashMap<String, ExtracellularDiffusionFieldBCConfig2D>();
		buildExtraCellularFieldBCConfigMap();
	}
	
	private int numberOfThreadsCompleted = 0;
/*	public EnhancedSteppable getDiffusionFieldsSimulationSteppable(){
		return new EnhancedSteppable() {
			
			final ExtraCellularDiffusionField[] fields = getAllExtraCellularDiffusionFields(new ExtraCellularDiffusionField[getNumberOfFields()]);
			final int numberOfFields = getNumberOfFields();
			public void step(SimState state) {
				numberOfThreadsCompleted = 0;
				final SimState stateFinal=state;
				for(int i = 0; i < fields.length; i++){
					final int number = i;
					Runnable r = new Runnable(){						
                  public void run() {
	                 fields[number].step(stateFinal);	
	                 increaseNumberOfThreadsCompleted();
                  }
						
					};
					Thread t = new Thread(r);
					t.start();
				}
				
				while(numberOfThreadsCompleted < numberOfFields){
					try{
	               Thread.sleep(1l);
	            }
               catch (InterruptedException e){
	               // TODO Auto-generated catch block
	               e.printStackTrace();
               }
				}
			}
			public double getInterval() { return 1; }
		};
	}*/
	private ExecutorService exec;
	public EnhancedSteppable getDiffusionFieldsSimulationSteppable(){
		if(exec != null && !exec.isShutdown()){
			exec.shutdown();			
		}
		//System.out.println("Available Processors: "+ Runtime.getRuntime().availableProcessors());
		exec = Executors.newFixedThreadPool(getNumberOfFields());
		return new EnhancedSteppable() {
			
			final ExtraCellularDiffusionField[] fields = getAllExtraCellularDiffusionFields(new ExtraCellularDiffusionField[getNumberOfFields()]);
			final int numberOfFields = getNumberOfFields();
			SimState stateGlobal = null;
			final ArrayList<Callable<Integer>> runnables = new ArrayList<Callable<Integer>>();
			{
				for(int i = 0; i < fields.length; i++){
					final int number = i;
					Callable<Integer> r = new Callable<Integer>(){						
                  public Integer call() {
	                 fields[number].step(stateGlobal);	
	                 increaseNumberOfThreadsCompleted();
	                 return new Integer(number);
                  }						
					};
					
					runnables.add(r);
				}
			}
			public void step(SimState state) {
				numberOfThreadsCompleted = 0;
				stateGlobal =state;
				List<Future<Integer>> results = null;
				try{
	           results = exec.invokeAll(runnables);
            }
            catch (InterruptedException e1){
            	ExceptionDisplayer.getInstance().displayException(e1);
            }
				while(numberOfThreadsCompleted < numberOfFields){
					try{
	               Thread.sleep(1l);
               }
               catch (InterruptedException e){
	              ExceptionDisplayer.getInstance().displayException(e);
               }
				}
			}
			public double getInterval() { return 1; }
		};		
	}
	
	private synchronized void increaseNumberOfThreadsCompleted(){
		numberOfThreadsCompleted++;
	}
	
	
	public DiffusionFieldCrossSectionMode getSelectedDiffusionFieldCrossSectionMode() {
	
		return selectedDiffusionFieldCrossSectionMode;
	}
	public void setSelectedDiffusionFieldCrossSectionMode(DiffusionFieldCrossSectionMode selectedDiffusionFieldCrossSectionMode) {
	
		this.selectedDiffusionFieldCrossSectionMode = selectedDiffusionFieldCrossSectionMode;
	}
	public double getDiffusionFieldCrossSectionCoordinate() {
	
		return diffusionFieldCrossSectionCoordinateInMikron;
	}	
	public void setDiffusionFieldCrossSectionCoordinate(double diffusionFieldCrossSectionCoordinate) {
	
		this.diffusionFieldCrossSectionCoordinateInMikron = diffusionFieldCrossSectionCoordinate;
	}	
	
	
   public void classLoaderHasChanged() {
		instance = null;		
   }
}
