package sim.app.episim.datamonitoring.steppables;

import sim.app.episim.util.Names;
import sim.engine.SimState;
import episiminterfaces.EpisimChart;
import episiminterfaces.EpisimChartSeries;


public abstract class SteppableCodeFactory {
	
	private static StringBuffer steppableCode;
	
	private static GradientSteppableBuilder gradientBuilder = new GradientSteppableBuilder(); 
	
	//returns something like new EnhancedSteppable(){...}
	public synchronized static String getEnhancedSteppableSourceCodeforChart(EpisimChart chart){
		boolean gradientSeriesFound = false;
		steppableCode = new StringBuffer();
		steppableCode.append("new EnhancedSteppable(){\n");
		steppableCode.append(gradientBuilder.buildChartGradientsFunction(chart));
		steppableCode.append("public void step(SimState state){\n");
		for(EpisimChartSeries actSeries: chart.getEpisimChartSeries()){
			if(actSeries.getExpression()[1].startsWith(Names.BUILDGRADIENTHANDLER)) gradientSeriesFound = true;
				
		}
		if(gradientSeriesFound) steppableCode.append(Names.GENERATEDGRADIENTFUNCTIONNAME+"();\n");
		steppableCode.append("}\n");
		steppableCode.append("public double getInterval(){\n");
		steppableCode.append("return " + chart.getChartUpdatingFrequency() + ";\n");
		steppableCode.append("}\n");
		steppableCode.append("}\n");
		
		return steppableCode.toString();
	}
	
	
	
	

}
