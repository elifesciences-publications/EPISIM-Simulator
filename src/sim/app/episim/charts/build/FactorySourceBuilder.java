package sim.app.episim.charts.build;

import java.util.HashSet;
import java.util.Set;

import sim.app.episim.util.Names;

import episimfactories.AbstractChartSetFactory;
import episiminterfaces.EpisimChart;
import episiminterfaces.EpisimChartSet;

public class FactorySourceBuilder {
	
	private StringBuffer factorySource;
	
	private EpisimChartSet actChartSet;
	private Set<Class<?>> requiredClasses;
	public FactorySourceBuilder(){
		this.requiredClasses = new HashSet<Class<?>>();
		this.factorySource = new StringBuffer();
		
	}
	
	public String buildEpisimFactorySource(EpisimChartSet episimChartSet){
		if(episimChartSet == null) throw new IllegalArgumentException("Chart-Set mustn't be null");
		this.actChartSet = episimChartSet;
		
		for(EpisimChart actChart: episimChartSet.getEpisimCharts()){
			for(Class<?> actClass:actChart.getRequiredClasses()) this.requiredClasses.add(actClass);
		}
		
		appendHeader();
		appendDataFields();
		appendConstructor();
		appendEnd();
		
		return factorySource.toString();
	}
	
	public void appendHeader(){
		
		for(Class<?> actClass : this.requiredClasses){
			if(actClass.getCanonicalName().contains(".")) this.factorySource.append("import "+ actClass.getCanonicalName()+";\n");
		}
		this.factorySource.append("import java.util.*;\n");
		this.factorySource.append("import sim.util.Bag;\n");
		this.factorySource.append("import sim.field.continuous.*;\n");
		this.factorySource.append("import generatedcharts.*;\n");
		this.factorySource.append("public class "+ Names.EPISIMCHARTSETFACTORYNAME+" extends AbstractChartSetFactory{\n");
	}
	
	public void appendDataFields(){
		for(Class<?> actClass : this.requiredClasses)
			this.factorySource.append("  private "+ actClass.getSimpleName()+ " "+Names.convertClassToVariable(actClass.getSimpleName())+ ";\n");
		
		this.factorySource.append("private List<GeneratedCharts> allChartsOfTheSet;\n");
		
	}
	
	public void appendConstructor(){
		this.factorySource.append("public EpisimChartSetFactory(){\n");
		this.factorySource.append("allChartsOfTheSet = new ArrayList<GeneratedChart>();\n");
		for(EpisimChart actChart:actChartSet.getEpisimCharts()){
			this.factorySource.append("this.allChartsOfTheSet.add(new "+Names.cleanString(actChart.getTitle())+ actChart.getId()+"());\n");
		}
		this.factorySource.append("}\n");
	}
	
	public void appendEnd(){
				
		this.factorySource.append("}\n");
	}

}
