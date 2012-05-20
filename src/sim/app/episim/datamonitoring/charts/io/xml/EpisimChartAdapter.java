package sim.app.episim.datamonitoring.charts.io.xml;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import episimexceptions.ModelCompatibilityException;
import episiminterfaces.monitoring.EpisimChart;
import episiminterfaces.monitoring.EpisimChartSeries;

import sim.app.episim.datamonitoring.charts.EpisimChartImpl;
import sim.app.episim.datamonitoring.charts.EpisimChartSeriesImpl;
import sim.app.episim.datamonitoring.charts.io.ECSFileReader;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.util.GlobalClassLoader;


public class EpisimChartAdapter extends XmlAdapter<AdaptedEpisimChart, EpisimChart> implements java.io.Serializable{

   public EpisimChart unmarshal(AdaptedEpisimChart v) throws Exception {
   	EpisimChart episimChart = new EpisimChartImpl(v.getId());
   	episimChart.setAntialiasingEnabled(v.isAntiAliasingEnabled());
   	episimChart.setBaselineCalculationAlgorithmConfigurator(v.getBaselineCalculationAlgorithmConfigurator());
   	episimChart.setChartUpdatingFrequency(v.getChartUpdatingFrequency());
   	episimChart.setIsDirty(v.isDirty());
   	episimChart.setLegendVisible(v.isLegendVisible());
   	episimChart.setPNGPrintingEnabled(v.isPngPrintingEnabled());
   	episimChart.setPNGPrintingFrequency(v.getPngPrintingFrequency());
   	episimChart.setPNGPrintingPath(v.getPngPrintingPath());
   	try{
   		addRequiredBaselineClassesToChart(v, episimChart);
   	}
   	catch(ClassNotFoundException e){
   		throw new ModelCompatibilityException("Actually Loaded Model is not Compatible with Chart-Set!");
   	}
   	episimChart.setTitle(v.getTitle());
   	episimChart.setXAxisLogarithmic(v.isxAxisLogarithmic());
   	episimChart.setXLabel(v.getxLabel());
   	episimChart.setYAxisLogarithmic(v.isyAxisLogarithmic());
   	episimChart.setYLabel(v.getyLabel());
   	for(EpisimChartSeries series : v.getEpisimChartSeries()) episimChart.addEpisimChartSeries(series);
   	
	   return episimChart;
   }
   
   private void addRequiredBaselineClassesToChart(AdaptedEpisimChart adaptedChart, EpisimChart chart) throws ClassNotFoundException{
   	
		if(chart instanceof EpisimChartImpl){
			EpisimChartImpl chartImpl = (EpisimChartImpl) chart;
			HashMap<String, Class<?>> requiredClasses = new HashMap<String, Class<?>>();
			Set<String> requiredBaselineClasses = adaptedChart.getRequiredClassesForBaselineNameSet();
			if(requiredBaselineClasses != null && !requiredBaselineClasses.isEmpty()){
				for(String actClassName : requiredBaselineClasses){
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
			if(requiredBaselineClasses != null && !requiredBaselineClasses.isEmpty()){
				for(String actClass: requiredBaselineClasses){
					if(requiredClasses.containsKey(actClass))requiredClassesSet.add(requiredClasses.get(actClass));
				}				
			}
			chartImpl.setRequiredClassesForBaseline(requiredClassesSet);
		}
	}
   
   
   
   

   public AdaptedEpisimChart marshal(EpisimChart v) throws Exception {
   	AdaptedEpisimChart episimChart = new AdaptedEpisimChart();
   	episimChart.setId(v.getId());
   	episimChart.setAntiAliasingEnabled(v.isAntialiasingEnabled());
   	episimChart.setBaselineCalculationAlgorithmConfigurator(v.getBaselineCalculationAlgorithmConfigurator());
   	episimChart.setChartUpdatingFrequency(v.getChartUpdatingFrequency());
   	episimChart.setDirty(v.isDirty());
   	episimChart.setLegendVisible(v.isLegendVisible());
   	episimChart.setPngPrintingEnabled(v.isPNGPrintingEnabled());
   	episimChart.setPngPrintingFrequency(v.getPNGPrintingFrequency());
   	episimChart.setPngPrintingPath(v.getPNGPrintingPath());
   	episimChart.setRequiredClassesForBaselineNameSet(((EpisimChartImpl)v).getRequiredClassesForBaselineNameSet());
   	episimChart.setTitle(v.getTitle());
   	episimChart.setxAxisLogarithmic(v.isXAxisLogarithmic());
   	episimChart.setxLabel(v.getXLabel());
   	episimChart.setyAxisLogarithmic(v.isYAxisLogarithmic());
   	episimChart.setyLabel(v.getYLabel());
   	episimChart.setEpisimChartSeries(v.getEpisimChartSeries());
   	return episimChart;
   }

}
