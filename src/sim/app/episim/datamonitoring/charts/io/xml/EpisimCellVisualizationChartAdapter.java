package sim.app.episim.datamonitoring.charts.io.xml;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import sim.app.episim.datamonitoring.charts.EpisimCellVisualizationChartImpl;

import sim.app.episim.datamonitoring.charts.io.ECSFileReader;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.util.GlobalClassLoader;
import episimexceptions.ModelCompatibilityException;
import episiminterfaces.monitoring.EpisimCellVisualizationChart;

public class EpisimCellVisualizationChartAdapter extends XmlAdapter<AdaptedEpisimCellVisualizationChart, EpisimCellVisualizationChart> implements java.io.Serializable{

   public EpisimCellVisualizationChart unmarshal(AdaptedEpisimCellVisualizationChart v) throws Exception {
   	EpisimCellVisualizationChart episimChart = new EpisimCellVisualizationChartImpl(v.getId());   	
   	episimChart.setCellColoringConfigurator(v.getCellColoringConfigurator());
   	episimChart.setDefaultColoring(v.getDefaultColoring());
   	episimChart.setCellProjectionPlane(v.getCellProjectionPlane());
   	episimChart.setChartUpdatingFrequency(v.getChartUpdatingFrequency());
   	episimChart.setIsDirty(v.isDirty());
   	episimChart.setMaxXMikron(v.getMaxXMikron());
   	episimChart.setMaxYMikron(v.getMaxYMikron());
   	episimChart.setMaxZMikron(v.getMaxZMikron());
   	episimChart.setMinXMikron(v.getMinXMikron());
   	episimChart.setMinYMikron(v.getMinYMikron());
   	episimChart.setMinZMikron(v.getMinZMikron());
   	episimChart.setPNGPrintingEnabled(v.isPngPrintingEnabled());
   	episimChart.setPNGPrintingFrequency(v.getPngPrintingFrequency());
   	episimChart.setPNGPrintingPath(v.getPngPrintingPath());
   	episimChart.setPNGPrintingPath(v.getPngPrintingPath());
   	try{
   		addRequiredClassesToChart(v, episimChart);
   	}
   	catch(ClassNotFoundException e){
   		throw new ModelCompatibilityException("Actually Loaded Model is not Compatible with Chart-Set!");
   	}
   	episimChart.setTitle(v.getTitle());
   	episimChart.setXLabel(v.getXLabel());
   	episimChart.setYLabel(v.getYLabel());
   	   	
	   return episimChart;
   }
   
   private void addRequiredClassesToChart(AdaptedEpisimCellVisualizationChart adaptedChart, EpisimCellVisualizationChart chart) throws ClassNotFoundException{
   	
		if(chart instanceof EpisimCellVisualizationChartImpl){
			EpisimCellVisualizationChartImpl chartImpl = (EpisimCellVisualizationChartImpl) chart;
			HashMap<String, Class<?>> requiredClasses = new HashMap<String, Class<?>>();
			Set<String> requiredClassesNameSet = adaptedChart.getRequiredClassesNameSet();
			if(requiredClassesNameSet != null && !requiredClassesNameSet.isEmpty()){
				for(String actClassName : requiredClassesNameSet){
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
	            		ECSFileReader.foundDirtyChartSeriesDuringImport = true;
	            	}
	            	else if(actClassName.contains(".Parameters_") && !(actClassName.endsWith("DiffLevel") || actClassName.endsWith("CellType"))){
	            		requiredClasses.put(actClassName, ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getClass());
	            		chartImpl.setIsDirty(true);
	            		ECSFileReader.foundDirtyChartSeriesDuringImport = true;
	            	}
	            	else if(actClassName.contains(".Parameters_") && actClassName.endsWith("DiffLevel")){
	            		requiredClasses.put(actClassName, ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getAvailableDifferentiationLevels()[0].getClass());
	            		chartImpl.setIsDirty(true);
	            		ECSFileReader.foundDirtyChartSeriesDuringImport = true;
	            	}
	            	else if(actClassName.contains(".Parameters_") && actClassName.endsWith("CellType")){
	            		requiredClasses.put(actClassName, ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getAvailableCellTypes()[0].getClass());
	            		chartImpl.setIsDirty(true);
	            		ECSFileReader.foundDirtyChartSeriesDuringImport = true;
	            	}
	            	else{
	            		throw e;
	            	}
	            }
				}
			}
			HashSet<Class<?>> requiredClassesSet = new HashSet<Class<?>>();
			if(requiredClassesNameSet != null && !requiredClassesNameSet.isEmpty()){
				for(String actClass: requiredClassesNameSet){
					if(requiredClasses.containsKey(actClass))requiredClassesSet.add(requiredClasses.get(actClass));
				}				
			}
			chartImpl.setRequiredClasses(requiredClassesSet);
		}
	} 

   public AdaptedEpisimCellVisualizationChart marshal(EpisimCellVisualizationChart v) throws Exception {
   	AdaptedEpisimCellVisualizationChart episimChart = new AdaptedEpisimCellVisualizationChart();
   	episimChart.setDefaultColoring(v.getDefaultColoring());
   	episimChart.setCellColoringConfigurator(v.getCellColoringConfigurator());
   	episimChart.setCellProjectionPlane(v.getCellProjectionPlane());
   	episimChart.setChartUpdatingFrequency(v.getChartUpdatingFrequency());
   	episimChart.setDirty(v.isDirty());
   	episimChart.setId(v.getId());
   	episimChart.setMaxXMikron(v.getMaxXMikron());
   	episimChart.setMaxYMikron(v.getMaxYMikron());
   	episimChart.setMaxZMikron(v.getMaxZMikron());
   	episimChart.setMinXMikron(v.getMinXMikron());
   	episimChart.setMinYMikron(v.getMinYMikron());
   	episimChart.setMinZMikron(v.getMinZMikron());
   	episimChart.setPngPrintingEnabled(v.isPNGPrintingEnabled());
   	episimChart.setPngPrintingFrequency(v.getPNGPrintingFrequency());
   	episimChart.setPngPrintingPath(v.getPNGPrintingPath());
   	episimChart.setRequiredClassesNameSet(((EpisimCellVisualizationChartImpl)v).getRequiredClassesNameSet());
   	episimChart.setTitle(v.getTitle());
   	episimChart.setXLabel(v.getXLabel());
   	episimChart.setYLabel(v.getYLabel());
   	return episimChart;
   }

}
