package sim.app.episim.datamonitoring.steppables;

import java.util.Set;

import sim.app.episim.util.Names;
import sim.engine.SimState;
import episiminterfaces.EpisimChart;
import episiminterfaces.EpisimChartSeries;


public abstract class SteppableCodeFactory {
	
	private static StringBuffer steppableCode;
	
	private static GradientSteppableBuilder gradientBuilder = new GradientSteppableBuilder();
	private static CommonSteppableBuilder commonBuilder = new CommonSteppableBuilder(); 
	
	//returns something like new EnhancedSteppable(){...}
	public synchronized static String getEnhancedSteppableSourceCodeforChart(EpisimChart chart){
		boolean gradientSeriesFound = false;
		steppableCode = new StringBuffer();
		steppableCode.append("new EnhancedSteppable(){\n");
		
		steppableCode.append("public void step(SimState state){\n");
		if(!chart.getBaselineExpression()[1].equals(Names.GRADBASELINE)
				&&!chart.getBaselineExpression() [1].startsWith(Names.BUILDGRADIENTHANDLER)){
			steppableCode.append("double baseLineResult = " + chart.getBaselineExpression() [1]+";\n");
		}
		for(EpisimChartSeries actSeries: chart.getEpisimChartSeries()){
			if(actSeries.getExpression()[1].startsWith(Names.BUILDGRADIENTHANDLER)) gradientSeriesFound = true;
			else steppableCode.append(actSeries.getExpression()[1]);
		}
		if(gradientSeriesFound) steppableCode.append("CalculationController.getInstance().calculateGradients();\n");
		steppableCode.append("}\n");
		steppableCode.append("public double getInterval(){\n");
		steppableCode.append("return " + chart.getChartUpdatingFrequency() + ";\n");
		steppableCode.append("}\n");
		steppableCode.append("}\n");
		
		return steppableCode.toString();
	}
	
	public static String getCalculationHandlerAnMethodCallForExpression(String expression, Set<Class<?>> requiredClasses){
		
		if(expression.startsWith(Names.BUILDGRADIENTHANDLER)){
			return gradientBuilder.buildCalculationHandler(expression.substring(Names.BUILDGRADIENTHANDLER.length()), requiredClasses);
		}
		else if(expression.startsWith(Names.BUILDACMVHANDLER)){
			return "CalculationController.getInstance().calculateACMV("+commonBuilder.buildCalculationHandler(expression.substring(Names.BUILDGRADIENTHANDLER.length())+")", requiredClasses);
		}
		
		
		return "";
	}
	
	public static void appendGradientCalucationHandlerRegistration(EpisimChart chart, StringBuffer source){
		gradientBuilder.appendGradientCalucationHandlerRegistration(chart,source);
	}
	
	

}
