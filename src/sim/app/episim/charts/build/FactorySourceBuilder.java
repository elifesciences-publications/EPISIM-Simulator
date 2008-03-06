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
		
		appendEnd();
		
		return factorySource.toString();
	}
	
	public void appendHeader(){
		
		for(Class<?> actClass : this.requiredClasses){
			if(actClass.getCanonicalName().contains(".")) this.factorySource.append("import "+ actClass.getCanonicalName()+";\n");
		}
		this.factorySource.append("public class "+ Names.EPISIMCHARTSETFACTORYNAME+" extends AbstractChartSetFactory{\n");
	}
	
	public void appendEnd(){
				
		this.factorySource.append("}\n");
	}

}
