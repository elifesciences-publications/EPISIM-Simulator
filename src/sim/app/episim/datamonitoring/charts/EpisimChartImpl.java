package sim.app.episim.datamonitoring.charts;


import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import episiminterfaces.calc.CalculationAlgorithmConfigurator;
import episiminterfaces.monitoring.EpisimChart;
import episiminterfaces.monitoring.EpisimChartSeries;
import sim.app.episim.util.ObjectManipulations;

public class EpisimChartImpl implements EpisimChart, java.io.Serializable{
	
	private long id;
	private String title = "";
	private String xLabel = "";
	private String yLabel = "";
	private boolean antiAliasingEnabled = false;
	private boolean legendVisible = false;
	private boolean pngPrintingEnabled = false;
	private int pngPrintingFrequency = 100;
	private int chartUpdatingFrequency = 100;
	private boolean xAxisLogarithmic = false;
	private boolean yAxisLogarithmic = false;
	
	
	private File pngPrintingPath = null;
	
	private CalculationAlgorithmConfigurator baselineCalculationAlgorithmConfigurator = null;
	
	private boolean isDirty = false;
	
	private Map<Long, EpisimChartSeries> seriesMap;
	
	
	private transient Set<Class<?>> requiredClassesForBaseline;
	private Set<String> requiredClassesForBaselineNameSet;
	
	public EpisimChartImpl(long id){
		this.id = id;
		
		this.seriesMap = new HashMap<Long, EpisimChartSeries>();			
		this.requiredClassesForBaseline = new HashSet<Class<?>>();
		this.requiredClassesForBaselineNameSet = new HashSet<String>();
	}
	
	public long getId(){
		return this.id;
	}
		
		
	public int getPNGPrintingFrequency() {
		
		return pngPrintingFrequency;
	}
	public int getChartUpdatingFrequency() {
		
		return chartUpdatingFrequency;
	}
	public String getTitle() {

		return title;
	}
	public String getXLabel() {

		return xLabel;
	}
	public String getYLabel() {

		return yLabel;
	}
	public boolean isAntialiasingEnabled() {

		return antiAliasingEnabled;
	}
	public boolean isLegendVisible() {

		return legendVisible;
	}
	public boolean isPNGPrintingEnabled() {

		
		return pngPrintingEnabled;
	}
	public void setAntialiasingEnabled(boolean val) {

		this.antiAliasingEnabled = val;
		
	}
	public void setLegendVisible(boolean val) {

		this.legendVisible = val;
		
	}
	public void setPNGPrintingEnabled(boolean val) {

		this.pngPrintingEnabled = val;
		
	}
	public void setPNGPrintingFrequency(int frequency) {

		this.pngPrintingFrequency = frequency;
		
	}
	public void setChartUpdatingFrequency(int frequency) {

		this.chartUpdatingFrequency = frequency;
		
	}
	public void setTitle(String title) {

		if(title != null && !title.trim().equals("")) this.title = title;
		
	}
	public void setXLabel(String label) {

		this.xLabel = label;
		
	}
	public void setYLabel(String label) {

		this.yLabel = label;
		
	}
	
	public CalculationAlgorithmConfigurator getBaselineCalculationAlgorithmConfigurator() {
	
		return baselineCalculationAlgorithmConfigurator;
	}
	
	public void setBaselineCalculationAlgorithmConfigurator(CalculationAlgorithmConfigurator val) {
	
		this.baselineCalculationAlgorithmConfigurator = val;
	}

	
	

	public void addEpisimChartSeries(EpisimChartSeries chartSeries) {

		this.seriesMap.put(chartSeries.getId(), chartSeries);
		
	}

	public List<EpisimChartSeries> getEpisimChartSeries() {
					
		List<EpisimChartSeries> result = new ArrayList<EpisimChartSeries>();
		result.addAll(this.seriesMap.values());
		Collections.sort(result, new Comparator<EpisimChartSeries>(){

			public int compare(EpisimChartSeries o1, EpisimChartSeries o2) {
				if(o1 != null && o2 != null){
					if(o1.getId() < o2.getId()) return -1;
					else if(o1.getId() > o2.getId()) return +1;
    
					else return 0;
				}
				return 0;
         }

		});
		return result;
	}
	
	public EpisimChartSeries getEpisimChartSeries(long id){
		return this.seriesMap.get(id);
	}

	public void removeChartSeries(long id) {

		this.seriesMap.remove(id);
		
	}

	public File getPNGPrintingPath() {

	 
	   return this.pngPrintingPath;
   }

	public void setPNGPrintingPath(File path) {

	  this.pngPrintingPath = path;
	   
   }

	
   public boolean isDirty() {
   
   	return isDirty;
   }

	
   public void setIsDirty(boolean isDirty) {
   
   	this.isDirty = isDirty;
   }

	public boolean isXAxisLogarithmic() {

		
		return this.xAxisLogarithmic;
	}

	public boolean isYAxisLogarithmic() {

		
		return this.yAxisLogarithmic;
	}

	public void setXAxisLogarithmic(boolean val) {

		this.xAxisLogarithmic = val;
		
	}

	public void setYAxisLogarithmic(boolean val) {

		this.yAxisLogarithmic = val;
		
	}

	public Set<Class<?>> getAllRequiredClasses() {
		 Set<Class<?>> allRequiredClasses = new HashSet<Class<?>>();
		 if(this.requiredClassesForBaseline != null) allRequiredClasses.addAll(requiredClassesForBaseline);
		 for(EpisimChartSeries series : this.seriesMap.values()) allRequiredClasses.addAll(series.getRequiredClasses());
	    return allRequiredClasses;
   }
	
	public Set<String> getAllRequiredClassesNameSet() {
		 Set<String> allRequiredClasses = new HashSet<String>();
		 if(this.requiredClassesForBaselineNameSet != null) allRequiredClasses.addAll(requiredClassesForBaselineNameSet);
		 for(EpisimChartSeries series : this.seriesMap.values()){
			 if(series instanceof EpisimChartSeriesImpl){
				 allRequiredClasses.addAll(((EpisimChartSeriesImpl)series).getRequiredClassesNameSet());
			 }
		 }
	    return allRequiredClasses;
  }

	public Set<Class<?>> getRequiredClassesForBaseline(){
		if(this.requiredClassesForBaseline == null) this.requiredClassesForBaseline = new HashSet<Class<?>>();
	   return ObjectManipulations.cloneObject(requiredClassesForBaseline);
   }
	
	
	
	public Set<String> getRequiredClassesForBaselineNameSet(){	   
	   return ObjectManipulations.cloneObject(this.requiredClassesForBaselineNameSet);
   }

	public void setRequiredClassesForBaseline(Set<Class<?>> requiredClasses) {
		if(requiredClasses != null){
			this.requiredClassesForBaseline = requiredClasses;
			this.requiredClassesForBaselineNameSet.clear();
			for(Class<?> actClass : this.requiredClassesForBaseline){
				this.requiredClassesForBaselineNameSet.add(actClass.getName());
			}
		}
		else{
			this.requiredClassesForBaseline = new HashSet<Class<?>>();
			this.requiredClassesForBaselineNameSet = new HashSet<String>();
		}
   }
	public EpisimChart clone(){
		EpisimChart newChart = ObjectManipulations.cloneObject(this);
		HashSet<Class<?>> newRequiredClasses = new HashSet<Class<?>>();
		newRequiredClasses.addAll(this.requiredClassesForBaseline);
		newChart.setRequiredClassesForBaseline(newRequiredClasses);		
		for(EpisimChartSeries oldSeries : this.getEpisimChartSeries()){
			newChart.removeChartSeries(oldSeries.getId());
			newChart.addEpisimChartSeries(oldSeries.clone());
		}		
		
		return newChart;
	}
	
}
