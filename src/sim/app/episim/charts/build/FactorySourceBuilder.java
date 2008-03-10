package sim.app.episim.charts.build;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jfree.chart.ChartPanel;

import sim.app.episim.CellType;
import sim.app.episim.util.Names;
import sim.engine.Steppable;

import episimexceptions.MissingObjectsException;
import episimfactories.AbstractChartSetFactory;
import episiminterfaces.EpisimCellDiffModel;
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
		appendRegisterMethod();
		appendCheckForMissingObjectsMethod();
		appendGetChartPanelsMethod();
		appendGetSteppablesOfChartsMethod();
		appendRegisterRequiredObjectsAtChartsMethod();
		appendEnd();
		
		return factorySource.toString();
	}
	
	public void appendHeader(){
		
		for(Class<?> actClass : this.requiredClasses){
			if(actClass.getCanonicalName().contains(".")) this.factorySource.append("import "+ actClass.getCanonicalName()+";\n");
		}
		this.factorySource.append("import java.util.*;\n");
		this.factorySource.append("import episiminterfaces.*;\n");
		this.factorySource.append("import episimexceptions.*;\n");
		this.factorySource.append("import episimfactories.*;\n");
		this.factorySource.append("import sim.util.Bag;\n");
		this.factorySource.append("import sim.field.continuous.*;\n");
		this.factorySource.append("import sim.engine.Steppable;\n");
		this.factorySource.append("import generatedcharts.*;\n");
		this.factorySource.append("import org.jfree.chart.ChartPanel;\n");
		this.factorySource.append("import sim.util.Bag;\n");
		this.factorySource.append("import sim.field.continuous.*;\n");
		this.factorySource.append("public class "+ Names.EPISIMCHARTSETFACTORYNAME+" extends AbstractChartSetFactory{\n");
	}
	
	public void appendDataFields(){
		for(Class<?> actClass : this.requiredClasses){
			if(!EpisimCellDiffModel.class.isAssignableFrom(actClass) && !CellType.class.isAssignableFrom(actClass)){
				this.factorySource.append("  private "+ actClass.getSimpleName()+ " "+Names.convertClassToVariable(actClass.getSimpleName())+ ";\n");
			}
		}
		for(EpisimChart actChart:actChartSet.getEpisimCharts()){
			this.factorySource.append("  private "+ Names.convertVariableToClass(Names.cleanString(actChart.getTitle())+ actChart.getId()) +
					" " + Names.convertClassToVariable(Names.cleanString(actChart.getTitle())+ actChart.getId())+";\n");
		}
		this.factorySource.append("  private List<GeneratedCharts> allChartsOfTheSet;\n");
		this.factorySource.append("  private Continuous2D cellContinuous;\n");
		this.factorySource.append("  private Bag allCells;\n");
		
	}
	
	public void appendConstructor(){
		this.factorySource.append("public EpisimChartSetFactory(){\n");
		this.factorySource.append("  this.allChartsOfTheSet = new ArrayList<GeneratedChart>();\n");
		for(EpisimChart actChart:actChartSet.getEpisimCharts()){
			this.factorySource.append("  this."+ Names.convertClassToVariable(Names.cleanString(actChart.getTitle())+ actChart.getId()) +
					" = new " + Names.convertVariableToClass(Names.cleanString(actChart.getTitle())+ actChart.getId())+"();\n");
			this.factorySource.append("  this.allChartsOfTheSet.add(this."+ Names.convertClassToVariable(Names.cleanString(actChart.getTitle())+ actChart.getId())+");\n");
		}
		this.factorySource.append("}\n");
	}
	
	private void appendRegisterMethod(){
		this.factorySource.append("public void registerNecessaryObjects(Object[] objects) throws MissingObjectsException{\n");
		this.factorySource.append("  if(objects == null) throw new IllegalArgumentsException(\"Objects-Array with Objects to be regisered for charting must not be null\");\n");
		this.factorySource.append("  for(Object actObject: objects){\n");
		this.factorySource.append("    if(actObject instanceof Continuous2D) this.cellContinuous = (Continuous2D) actObject;\n");
		this.factorySource.append("    else if(actObject instanceof Bag) this.allCells = (Bag) actObject;\n");
		for(Class<?> actClass : this.requiredClasses){
			if(!EpisimCellDiffModel.class.isAssignableFrom(actClass) && !CellType.class.isAssignableFrom(actClass)){
				this.factorySource.append("    else if(actObject instanceof "+actClass.getSimpleName()+") this."+Names.convertClassToVariable(actClass.getSimpleName())+" = ("+actClass.getSimpleName()+") actObject;\n"); 
			}
		}
		this.factorySource.append("  }\n");
		this.factorySource.append("  checkForMissingObjects();\n");
		this.factorySource.append("  registerRequiredObjectsAtCharts();\n");
		this.factorySource.append("}\n");
	}
	
	private void appendCheckForMissingObjectsMethod(){
		this.factorySource.append("private void checkForMissingObjects() throws MissingObjectsException {\n");
		this.factorySource.append("  boolean objectsMissing = false;\n");
		this.factorySource.append("  if(this.cellContinuous == null) objectsMissing = true;\n");
		this.factorySource.append("  if(this.allCells == null) objectsMissing = true;\n");
		for(Class<?> actClass : this.requiredClasses){
			if(!EpisimCellDiffModel.class.isAssignableFrom(actClass) && !CellType.class.isAssignableFrom(actClass)){
				this.factorySource.append("  if(this."+Names.convertClassToVariable(actClass.getSimpleName())+" == null) objectsMissing = true;\n"); 
			}
		}
		this.factorySource.append("  if(objectsMissing) throw new MissingObjectsException(\"Some of the required Objects for Charting are not registered."
				+" Please call again the registerNecessaryObjects-Method to register them!\");\n");
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
		this.factorySource.append("public abstract List<Steppable> getSteppablesOfCharts(){\n");
		this.factorySource.append("  List<Steppable> chartSteppables = new ArrayList<Steppable>();\n");
		this.factorySource.append("  for(GeneratedChart actChart : allChartsOfTheSet){\n");
		this.factorySource.append("    chartSteppables.add(actChart.getSteppable());\n");
		this.factorySource.append("  }\n");
		this.factorySource.append("  return chartSteppables;\n");
		this.factorySource.append("}\n");
	}
	
	public void appendEnd(){
				
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
