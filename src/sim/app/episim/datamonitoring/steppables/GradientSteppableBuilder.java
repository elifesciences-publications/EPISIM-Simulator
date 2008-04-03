package sim.app.episim.datamonitoring.steppables;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jfree.data.xy.XYSeries;

import sim.app.episim.CellType;
import sim.app.episim.util.GenericBag;
import sim.app.episim.util.Names;
import sim.app.episim.util.Sorting;
import sim.field.continuous.Continuous2D;
import episiminterfaces.EpisimCellDiffModel;
import episiminterfaces.EpisimChart;
import episiminterfaces.EpisimChartSeries;


public class GradientSteppableBuilder {
	
	private StringBuffer source;
	
	protected GradientSteppableBuilder(){
		
	}
	
	public String buildChartGradientsFunction(EpisimChart chart){
		source = new StringBuffer();
		source.append("private void "+ Names.GENERATEDGRADIENTFUNCTIONNAME+"(){\n");
		for(EpisimChartSeries actSeries: chart.getEpisimChartSeries()){
			if(actSeries.getExpression()[1].startsWith(Names.BUILDGRADIENTHANDLER)) 
				source.append(Names.convertClassToVariable(Names.cleanString(actSeries.getName())+actSeries.getId()) +".clear();\n");
				source.append("Map<Double, Double> map"+Names.convertClassToVariable(Names.cleanString(actSeries.getName())+actSeries.getId()) + "= new HashMap<Double, Double>();\n");
		}
		source.append("for(Object actCell:allCells){\n");
		appendLocalVars(chart);
		appendAssignmentCheck("actCell", chart.getRequiredClasses());
		source.append("EpisimCellDiffModel cellDiff = ((CellType)actCell).getEpisimCellDiffModelObject();\n");
		appendAssignmentCheck("cellDiff", chart.getRequiredClasses());
		source.append("if(cellDiff.getX() >= GlobalStatistics.getInstance().getGradientMinX()"
				+ "&& cellDiff.getX() <= GlobalStatistics.getInstance().getGradientMaxX()"
				+ "&& cellDiff.getY() >= GlobalStatistics.getInstance().getGradientMinY()"
				+ "&& cellDiff.getY() <= GlobalStatistics.getInstance().getGradientMaxY()){\n");
		for(EpisimChartSeries actSeries: chart.getEpisimChartSeries()){
			if(actSeries.getExpression()[1].startsWith(Names.BUILDGRADIENTHANDLER)) 
				source.append("map"+Names.convertClassToVariable(Names.cleanString(actSeries.getName())+actSeries.getId())+".put(cellDiff.getY(), ("
						+actSeries.getExpression()[1].substring(Names.BUILDGRADIENTHANDLER.length())+"));\n");
		}
		source.append("}\n");
		source.append("}\n");
		
		for(EpisimChartSeries actSeries: chart.getEpisimChartSeries()){
			if(actSeries.getExpression()[1].startsWith(Names.BUILDGRADIENTHANDLER)) 
			source.append("Sorting.sortMapValuesIntoXYSeries(map"+Names.convertClassToVariable(Names.cleanString(actSeries.getName())+actSeries.getId())+","+ 
				Names.convertClassToVariable(Names.cleanString(actSeries.getName())+actSeries.getId())+");\n");
		}
	
		
		
		
	//	this.factorySource.append("allCells, cellContinuous);\n");
		
		
		source.append("}\n");
		return source.toString();
	}
	private void appendLocalVars(EpisimChart chart){
		for(Class<?> actClass: chart.getRequiredClasses()){
			if(EpisimCellDiffModel.class.isAssignableFrom(actClass) || CellType.class.isAssignableFrom(actClass))				
				source.append(actClass.getSimpleName()+ " " + Names.convertClassToVariable(actClass.getSimpleName())+" = null;\n");
		}
	}
	private void appendAssignmentCheck(String varName, Set<Class<?>> requiredClasses){
		boolean firstLoop = true;
		
		for(Class<?> actClass: requiredClasses){
			if(EpisimCellDiffModel.class.isAssignableFrom(actClass) || CellType.class.isAssignableFrom(actClass)){				
				
				if(firstLoop){
					source.append("if("+varName+ ".getClass().getName().equals(\""+ actClass.getName()+ "\")) " + 
							Names.convertClassToVariable(actClass.getSimpleName())+"= ("+ actClass.getSimpleName()+ ")"+varName+ ";\n");
					firstLoop = false;
					
				}
				else{
					source.append("else if("+varName+ ".getClass().isAssignableFrom("+ actClass.getSimpleName()+ ".class)) " + 
							Names.convertClassToVariable(actClass.getSimpleName())+"= ("+ actClass.getSimpleName()+ ")"+varName+ ";\n");
				}
				
			}
		}
	}

}
