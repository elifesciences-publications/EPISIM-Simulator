package episimfactories;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.swing.JPanel;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.jfree.chart.ChartPanel;

import episimexceptions.MissingObjectsException;
import episimexceptions.ModelCompatibilityException;
import episiminterfaces.monitoring.EpisimCellVisualizationChart;
import episiminterfaces.monitoring.EpisimChart;
import episiminterfaces.monitoring.EpisimChartSeries;
import episiminterfaces.monitoring.EpisimChartSet;
import sim.app.episim.EpisimExceptionHandler;
import sim.app.episim.datamonitoring.charts.EpisimCellVisualizationChartImpl;
import sim.app.episim.datamonitoring.charts.EpisimChartImpl;
import sim.app.episim.datamonitoring.charts.EpisimChartSeriesImpl;
import sim.app.episim.model.AbstractCell;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.util.EnhancedSteppable;
import sim.app.episim.util.GenericBag;
import sim.app.episim.util.GlobalClassLoader;
import sim.app.episim.util.Names;
import sim.app.episim.util.ObjectStreamFactory;
import sim.engine.Steppable;
import sim.field.continuous.Continuous2D;


public abstract class AbstractChartSetFactory {

	
	public static EpisimChartSet getEpisimChartSetBasedOnXml(InputStream stream) throws ModelCompatibilityException{
		JAXBContext jc = null;
		Unmarshaller u = null;
		EpisimChartSet chartSet = null;
      try{
	      jc = JAXBContext.newInstance(sim.app.episim.datamonitoring.charts.EpisimChartSetImpl.class);
	      u = jc.createUnmarshaller();
	      Object o = u.unmarshal(stream);
	      if(o instanceof EpisimChartSet){
	      	chartSet = (EpisimChartSet) o;
	      }
      }
      catch (JAXBException e){
	      EpisimExceptionHandler.getInstance().displayException(e);
      }
      if(chartSet != null){
      	for(EpisimChart chart :chartSet.getEpisimCharts()){ 
				try{
	            checkChartDirtyStatus(chart);
            }
            catch (ClassNotFoundException e){
            	throw new ModelCompatibilityException("Actually Loaded Model is not Compatible with Chart-Set!");
            }
			}
      	for(EpisimCellVisualizationChart chart :chartSet.getEpisimCellVisualizationCharts()){ 
				try{
	            checkChartDirtyStatus(chart);
            }
            catch (ClassNotFoundException e){
            	throw new ModelCompatibilityException("Actually Loaded Model is not Compatible with Chart-Set!");
            }
			}
      }
		return chartSet; 
	}
	
	
	public static EpisimChartSet getEpisimChartSet(InputStream stream) throws ModelCompatibilityException{
		
			ObjectInputStream objIn =ObjectStreamFactory.getObjectInputStreamForInputStream(stream);
			
			Object result = null;
			try{
				result = objIn.readObject();
				stream.close();
				objIn.close();
				
			}
			catch (IOException e){				
				if(e instanceof InvalidClassException) throw new ModelCompatibilityException("Actually Loaded Model is not Compatible with Chart-Set!");
				else{
					EpisimExceptionHandler.getInstance().displayException(e);
				}
			}
			catch(ClassNotFoundException e){
				throw new ModelCompatibilityException("Actually Loaded Model is not Compatible with Chart-Set!");
			}
			catch(NoClassDefFoundError e){
				throw new ModelCompatibilityException("Actually Loaded Model is not Compatible with Chart-Set!");				
			}
			if(result != null && result instanceof EpisimChartSet){
				try{
					EpisimChartSet chartSet = (EpisimChartSet) result;
					
					for(EpisimChart chart :chartSet.getEpisimCharts()){ 
						addRequiredClassesToChart(chart);
						chart.setIsDirty(true);
					}
					for(EpisimCellVisualizationChart chart :chartSet.getEpisimCellVisualizationCharts()){ 
						addRequiredClassesToChart(chart);
						chart.setIsDirty(true);
					}
					return chartSet;
				}
				catch(ClassNotFoundException e){
					throw new ModelCompatibilityException("Actually Loaded Model is not Compatible with Chart-Set!");
				}
			}
		
		
		return null;
	}
	
	private static void addRequiredClassesToChart(EpisimChart chart) throws ClassNotFoundException{
	
		if(chart instanceof EpisimChartImpl){
			EpisimChartImpl chartImpl = (EpisimChartImpl) chart;
			HashMap<String, Class<?>> requiredClasses = new HashMap<String, Class<?>>();
			for(String actClassName :chartImpl.getAllRequiredClassesNameSet()){
				try{
	            Class<?> actClass = Class.forName(actClassName, true, GlobalClassLoader.getInstance());
	            if(actClass!= null){
	            	requiredClasses.put(actClassName, actClass);
	            }
	            
            }
            catch (ClassNotFoundException e){
            	if(actClassName.contains(".Cell_")){
            		requiredClasses.put(actClassName, ModelController.getInstance().getNewEpisimCellBehavioralModelObject().getClass());
            		chartImpl.setIsDirty(true);
            	}
            	else if(actClassName.contains(".Parameters_") && !(actClassName.endsWith("DiffLevel") || actClassName.endsWith("CellType"))){
            		requiredClasses.put(actClassName, ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getClass());
            		chartImpl.setIsDirty(true);
            	}
            	else if(actClassName.contains(".Parameters_") && actClassName.endsWith("DiffLevel")){
            		requiredClasses.put(actClassName, ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getAvailableDifferentiationLevels()[0].getClass());
            		chartImpl.setIsDirty(true);
            	}
            	else if(actClassName.contains(".Parameters_") && actClassName.endsWith("CellType")){
            		requiredClasses.put(actClassName, ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getAvailableCellTypes()[0].getClass());
            		chartImpl.setIsDirty(true);
            	}
            	else{
            		throw e;
            	}
            }
			}
			HashSet<Class<?>> requiredClassesSet = new HashSet<Class<?>>();
			for(String actClass: chartImpl.getRequiredClassesForBaselineNameSet()){
				if(requiredClasses.containsKey(actClass))requiredClassesSet.add(requiredClasses.get(actClass));
			}
			chartImpl.setRequiredClassesForBaseline(requiredClassesSet);
			
			for(EpisimChartSeries series : chart.getEpisimChartSeries()){
				if(series instanceof EpisimChartSeriesImpl){
					EpisimChartSeriesImpl seriesImpl = (EpisimChartSeriesImpl) series;
					requiredClassesSet = new HashSet<Class<?>>();
					for(String actClass : seriesImpl.getRequiredClassesNameSet()){
						if(requiredClasses.containsKey(actClass))requiredClassesSet.add(requiredClasses.get(actClass));
					}
					seriesImpl.setRequiredClasses(requiredClassesSet);
				}
			}
		}
	}
	private static void addRequiredClassesToChart(EpisimCellVisualizationChart chart) throws ClassNotFoundException{
		
		if(chart instanceof EpisimCellVisualizationChartImpl){
			EpisimCellVisualizationChartImpl chartImpl = (EpisimCellVisualizationChartImpl) chart;
			HashMap<String, Class<?>> requiredClasses = new HashMap<String, Class<?>>();
			for(String actClassName :chartImpl.getRequiredClassesNameSet()){
				try{
	            Class<?> actClass = Class.forName(actClassName, true, GlobalClassLoader.getInstance());
	            if(actClass!= null){
	            	requiredClasses.put(actClassName, actClass);
	            }
	            
            }
            catch (ClassNotFoundException e){
            	if(actClassName.contains(".Cell_")){
            		requiredClasses.put(actClassName, ModelController.getInstance().getNewEpisimCellBehavioralModelObject().getClass());
            		chartImpl.setIsDirty(true);
            	}
            	else if(actClassName.contains(".Parameters_") && !(actClassName.endsWith("DiffLevel") || actClassName.endsWith("CellType"))){
            		requiredClasses.put(actClassName, ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getClass());
            		chartImpl.setIsDirty(true);
            	}
            	else if(actClassName.contains(".Parameters_") && actClassName.endsWith("DiffLevel")){
            		requiredClasses.put(actClassName, ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getAvailableDifferentiationLevels()[0].getClass());
            		chartImpl.setIsDirty(true);
            	}
            	else if(actClassName.contains(".Parameters_") && actClassName.endsWith("CellType")){
            		requiredClasses.put(actClassName, ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getAvailableCellTypes()[0].getClass());
            		chartImpl.setIsDirty(true);
            	}
            	else{
            		throw e;
            	}
            }
			}
			HashSet<Class<?>> requiredClassesSet = new HashSet<Class<?>>();
			for(String actClass: chartImpl.getRequiredClassesNameSet()){
				if(requiredClasses.containsKey(actClass))requiredClassesSet.add(requiredClasses.get(actClass));
			}
			chartImpl.setRequiredClasses(requiredClassesSet);		
		}
	}	
	
	private static void checkChartDirtyStatus(EpisimChart chart) throws ClassNotFoundException{
		
		if(chart instanceof EpisimChartImpl){
			EpisimChartImpl chartImpl = (EpisimChartImpl) chart;		
			for(String actClassName :chartImpl.getAllRequiredClassesNameSet()){
				try{
	            Class<?> actClass = Class.forName(actClassName, true, GlobalClassLoader.getInstance());        
            }
            catch (ClassNotFoundException e){
            	if(actClassName.contains(".Cell_")){
            		chartImpl.setIsDirty(true);
            	}
            	else if(actClassName.contains(".Parameters_") && !(actClassName.endsWith("DiffLevel") || actClassName.endsWith("CellType"))){            		
            		chartImpl.setIsDirty(true);
            	}
            	else if(actClassName.contains(".Parameters_") && actClassName.endsWith("DiffLevel")){            		
            		chartImpl.setIsDirty(true);
            	}
            	else if(actClassName.contains(".Parameters_") && actClassName.endsWith("CellType")){            		
            		chartImpl.setIsDirty(true);
            	}
            	else{
            		throw e;
            	}
            }
			}			
		}
	}
	private static void checkChartDirtyStatus(EpisimCellVisualizationChart chart) throws ClassNotFoundException{
		
		if(chart instanceof EpisimCellVisualizationChartImpl){
			EpisimCellVisualizationChartImpl chartImpl = (EpisimCellVisualizationChartImpl) chart;
		
			for(String actClassName :chartImpl.getRequiredClassesNameSet()){
				try{
	            Class<?> actClass = Class.forName(actClassName, true, GlobalClassLoader.getInstance());         
            }
            catch (ClassNotFoundException e){
            	if(actClassName.contains(".Cell_")){
            		chartImpl.setIsDirty(true);
            	}
            	else if(actClassName.contains(".Parameters_") && !(actClassName.endsWith("DiffLevel") || actClassName.endsWith("CellType"))){            		
            		chartImpl.setIsDirty(true);
            	}
            	else if(actClassName.contains(".Parameters_") && actClassName.endsWith("DiffLevel")){            		
            		chartImpl.setIsDirty(true);
            	}
            	else if(actClassName.contains(".Parameters_") && actClassName.endsWith("CellType")){            		
            		chartImpl.setIsDirty(true);
            	}
            	else{
            		throw e;
            	}
            }
			}			
		}
	}
	
	public static String getEpisimChartSetBinaryName() {
		
		return Names.EPISIM_CHARTSET_FILENAME+"ds";
	}
	public abstract List<ChartPanel> getChartPanels();
   public abstract List<EnhancedSteppable> getSteppablesOfCharts();
   public abstract List<EnhancedSteppable> getSteppablesOfPNGWriters();
   
   public abstract void registerNecessaryObjects(GenericBag<AbstractCell> allCells, Object[] objects) throws MissingObjectsException;
}
