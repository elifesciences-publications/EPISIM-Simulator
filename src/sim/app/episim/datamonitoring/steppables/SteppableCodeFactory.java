package sim.app.episim.datamonitoring.steppables;

import java.io.File;
import java.util.Set;

import org.jfree.chart.JFreeChart;

import sim.app.episim.datamonitoring.charts.build.ChartSourceBuilder;
import sim.app.episim.datamonitoring.charts.io.PNGPrinter;
import sim.app.episim.util.Names;
import sim.engine.SimState;
import episiminterfaces.EpisimChart;
import episiminterfaces.EpisimChartSeries;
import episiminterfaces.EpisimDataExportColumn;
import episiminterfaces.EpisimDataExportDefinition;


public abstract class SteppableCodeFactory {
	
	private static StringBuffer steppableCode;
	
	
	private static CommonSteppableBuilder commonBuilder = new CommonSteppableBuilder(); 
	
	//returns something like new EnhancedSteppable(){...}
	public synchronized static String getEnhancedSteppableSourceCodeforChart(EpisimChart chart){
		boolean gradientSeriesFound = false;
		boolean oneCellSeriesFound = false;
		steppableCode = new StringBuffer();
		steppableCode.append("new EnhancedSteppable(){\n");
		
		steppableCode.append("public void step(SimState state){\n");
		steppableCode.append("double baseLineResult = 0;\n");
		if(!chart.getBaselineExpression()[1].equals(Names.GRADBASELINE)
				&&!chart.getBaselineExpression() [1].startsWith(Names.BUILDGRADIENTHANDLER)
				&&!chart.getBaselineExpression() [1].startsWith(Names.BUILDCELLHANDLER)){
			steppableCode.append("baseLineResult = " + chart.getBaselineExpression() [1]+";\n");
		}
		else if(chart.getBaselineExpression() [1].startsWith(Names.BUILDCELLHANDLER)){
			steppableCode.append("baseLineResult = CalculationController.getInstance().calculateOneCellBaseLine("
					+ chart.getId() + "l, "
					+ chart.getBaselineExpression()[1].substring(Names.BUILDCELLHANDLER.length())+");\n");
		}
		for(EpisimChartSeries actSeries: chart.getEpisimChartSeries()){
			if(actSeries.getExpression()[1].startsWith(Names.BUILDGRADIENTHANDLER)) gradientSeriesFound = true;
			else if(actSeries.getExpression()[1].startsWith(Names.BUILDCELLHANDLER)) oneCellSeriesFound = true;
			else{ 
				steppableCode.append(Names.convertClassToVariable(Names.cleanString(actSeries.getName())+actSeries.getId())+
						".add(baseLineResult, "+ actSeries.getExpression()[1]+");\n");
			}
		}
		if(gradientSeriesFound) steppableCode.append("CalculationController.getInstance().calculateGradients();\n");
		if(oneCellSeriesFound) steppableCode.append("CalculationController.getInstance().calculateOneCell(baseLineResult);\n");
		steppableCode.append("}\n");
		steppableCode.append("public double getInterval(){\n");
		steppableCode.append("return " + chart.getChartUpdatingFrequency() + ";\n");
		steppableCode.append("}\n");
		steppableCode.append("}\n");
		
		return steppableCode.toString();
	}
	
	
	public synchronized static String getEnhancedSteppableSourceCodeforDataExport(EpisimDataExportDefinition exportDefinition){
		boolean gradientColumnFound = false;
		boolean oneCellColumnFound = false;
		steppableCode = new StringBuffer();
		steppableCode.append("new EnhancedSteppable(){\n");
		
		steppableCode.append("public void step(SimState state){\n");
		
		
		
		for(EpisimDataExportColumn actColumn: exportDefinition.getEpisimDataExportColumns()){
			if(actColumn.getCalculationExpression()[1].startsWith(Names.BUILDGRADIENTHANDLER)) gradientColumnFound = true;
			else if(actColumn.getCalculationExpression()[1].startsWith(Names.BUILDCELLHANDLER)) oneCellColumnFound = true;
			else{ 
				steppableCode.append(Names.convertClassToVariable(Names.cleanString(actColumn.getName())+actColumn.getId())+
						".add(baseLineResult, "+ actColumn.getCalculationExpression()[1]+");\n");
			}
		}
	
		//________________________________________________________
		
		// TODO: Vervollständigen
		//__________________________________________________________
		
		
		
		if(gradientColumnFound) steppableCode.append("CalculationController.getInstance().calculateGradients();\n");
		if(oneCellColumnFound) steppableCode.append("CalculationController.getInstance().calculateOneCell(baseLineResult);\n");
		steppableCode.append("}\n");
		steppableCode.append("public double getInterval(){\n");
		steppableCode.append("return " + exportDefinition.getDataExportFrequncyInSimulationSteps()+ ";\n");
		steppableCode.append("}\n");
		steppableCode.append("}\n");
		
		return steppableCode.toString();
	}
	
	
	
	
	
	public synchronized static String getEnhancedSteppableForPNGPrinting(EpisimChart chart){
		
		steppableCode = new StringBuffer();
		steppableCode.append("new EnhancedSteppable(){\n");
		
		steppableCode.append("public void step(SimState state){\n");
		steppableCode.append("  PNGPrinter.getInstance().printChartAsPng("+ chart.getId()+"l, "+
				                  "new File(\""+ chart.getPNGPrintingPath().getAbsolutePath().replace(File.separatorChar, '/')+"\"), "+
				                  "\""+ chart.getTitle()+"\", "+ChartSourceBuilder.CHARTDATAFIELDNAME+", state);\n");
		steppableCode.append("}\n");
		steppableCode.append("public double getInterval(){\n");
		steppableCode.append("return " + chart.getPNGPrintingFrequency() + ";\n");
		steppableCode.append("}\n");
		steppableCode.append("}\n");
		
		return steppableCode.toString();
		
	}
	
	public static String getCalculationHandlerAndMethodCallForExpression(String expression, Set<Class<?>> requiredClasses){
		
		if(expression.startsWith(Names.BUILDGRADIENTHANDLER)){
			return commonBuilder.buildCalculationHandler(expression.substring(Names.BUILDGRADIENTHANDLER.length()), requiredClasses);
		}
		else if(expression.startsWith(Names.BUILDACMVHANDLER)){
			return "CalculationController.getInstance().calculateACMV("+commonBuilder.buildCalculationHandler(expression.substring(Names.BUILDACMVHANDLER.length()), requiredClasses)+")";
		}
		else if(expression.startsWith(Names.BUILDCELLHANDLER)){
			return commonBuilder.buildCalculationHandler(expression.substring(Names.BUILDCELLHANDLER.length()), requiredClasses);
		}
		
		
		return "";
	}
	
	public static void appendGradientCalucationHandlerRegistration(EpisimChart chart, StringBuffer source){
		commonBuilder.appendCalucationHandlerRegistration(chart,source);
	}
	
	

}
