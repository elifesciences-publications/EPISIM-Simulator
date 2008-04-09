package sim.app.episim.datamonitoring.charts.build;

import sim.app.episim.CellType;
import sim.app.episim.datamonitoring.build.AbstractCommonFactorySourceBuilder;
import sim.app.episim.util.Names;
import episiminterfaces.EpisimCellDiffModel;
import episiminterfaces.EpisimChart;
import episiminterfaces.EpisimChartSet;

public class ChartSetFactorySourceBuilder  extends AbstractCommonFactorySourceBuilder{
	
	
	
	private EpisimChartSet actChartSet;
	
	public ChartSetFactorySourceBuilder(){
		super();
		
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
		appendRegisterMethod();
		appendCheckForMissingObjectsMethod();
		appendGetChartPanelsMethod();
		appendGetSteppablesOfChartsMethod();
		appendRegisterRequiredObjectsAtChartsMethod();
		appendEnd();
		
		return factorySource.toString();
	}
	
	public void appendHeader(){
		super.appendHeader();
		this.factorySource.append("import "+Names.GENERATEDCHARTSPACKAGENAME+".*;\n");
		this.factorySource.append("import org.jfree.chart.ChartPanel;\n");
		this.factorySource.append("public class "+ Names.EPISIMCHARTSETFACTORYNAME+" extends AbstractChartSetFactory{\n");
	}
	
	public void appendDataFields(){
		
		super.appendDataFields();
		for(EpisimChart actChart:actChartSet.getEpisimCharts()){
			this.factorySource.append("  private "+ Names.convertVariableToClass(Names.cleanString(actChart.getTitle())+ actChart.getId()) +
					" " + Names.convertClassToVariable(Names.cleanString(actChart.getTitle())+ actChart.getId())+";\n");
		}
		this.factorySource.append("  private List<GeneratedChart> allChartsOfTheSet;\n");
			
	}
	
	public void appendConstructor(){
		this.factorySource.append("public "+ Names.EPISIMCHARTSETFACTORYNAME+"(){\n");
		this.factorySource.append("  this.allChartsOfTheSet = new ArrayList<GeneratedChart>();\n");
		for(EpisimChart actChart:actChartSet.getEpisimCharts()){
			this.factorySource.append("  this."+ Names.convertClassToVariable(Names.cleanString(actChart.getTitle())+ actChart.getId()) +
					" = new " + Names.convertVariableToClass(Names.cleanString(actChart.getTitle())+ actChart.getId())+"();\n");
			this.factorySource.append("  this.allChartsOfTheSet.add(this."+ Names.convertClassToVariable(Names.cleanString(actChart.getTitle())+ actChart.getId())+");\n");
		}
		this.factorySource.append("}\n");
	}
	
		
	
	private void appendGetChartPanelsMethod(){
		this.factorySource.append("public List<ChartPanel> getChartPanels(){\n");
		this.factorySource.append("  List<ChartPanel> chartPanels = new ArrayList<ChartPanel>();\n");
		this.factorySource.append("  for(GeneratedChart actChart : allChartsOfTheSet){\n");
		this.factorySource.append("    chartPanels.add(actChart.getChartPanel());\n");
		this.factorySource.append("  }\n");
		this.factorySource.append("  return chartPanels;\n");
		this.factorySource.append("}\n");
	}
	
	private void appendGetSteppablesOfChartsMethod(){
		this.factorySource.append("public List<EnhancedSteppable> getSteppablesOfCharts(){\n");
		this.factorySource.append("  List<EnhancedSteppable> chartSteppables = new ArrayList<EnhancedSteppable>();\n");
		this.factorySource.append("  for(GeneratedChart actChart : allChartsOfTheSet){\n");
		this.factorySource.append("    chartSteppables.add(actChart.getSteppable());\n");
		this.factorySource.append("    if(actChart.getPNGSteppable() != null) chartSteppables.add(actChart.getPNGSteppable());\n");
		this.factorySource.append("  }\n");
		this.factorySource.append("  return chartSteppables;\n");
		this.factorySource.append("}\n");
	}
	
	
	
	private void appendRegisterRequiredObjectsAtChartsMethod(){
		this.factorySource.append("private void registerRequiredObjectsAtCharts(){\n");
		for(EpisimChart actChart:actChartSet.getEpisimCharts()){
			this.factorySource.append("  this."+ Names.convertClassToVariable(Names.cleanString(actChart.getTitle())+ actChart.getId()) +
					".registerRequiredObjects(");
			for(Class<?> actClass: actChart.getRequiredClasses()){
				if(!EpisimCellDiffModel.class.isAssignableFrom(actClass) && !CellType.class.isAssignableFrom(actClass)){
					this.factorySource.append(Names.convertClassToVariable(actClass.getSimpleName())+", ");
				}
			}
			this.factorySource.append("allCells, cellContinuous);\n");
		}
		this.factorySource.append("}\n");
	}
	
	
}
