package sim.app.episim.datamonitoring.steppables;

import java.util.Set;

import sim.app.episim.util.Names;
import sim.engine.SimState;
import episiminterfaces.EpisimChart;
import episiminterfaces.EpisimChartSeries;


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
