package sim.app.episim.datamonitoring.steppables;

import java.io.File;
import java.util.Map;
import java.util.Set;

import org.jfree.chart.JFreeChart;

import sim.app.episim.datamonitoring.charts.build.ChartSourceBuilder;
import sim.app.episim.datamonitoring.charts.io.PNGPrinter;
import sim.app.episim.util.Names;
import sim.engine.SimState;
import episiminterfaces.monitoring.EpisimChart;
import episiminterfaces.monitoring.EpisimChartSeries;
import episiminterfaces.monitoring.EpisimDataExportColumn;
import episiminterfaces.monitoring.EpisimDataExportDefinition;


public abstract class SteppableCodeFactory {
	
	private static StringBuffer steppableCode;
	
	
	
	//returns something like new EnhancedSteppable(){...}
	public synchronized static String getEnhancedSteppableSourceCode(String nameOfCallBackList, double updatingFrequency){
				
		steppableCode = new StringBuffer();
		steppableCode.append("new EnhancedSteppable(){\n");
		
		steppableCode.append("public void step(SimState state){\n");
		
		steppableCode.append("for(CalculationCallBack callBack: "+ nameOfCallBackList + ") callBack.calculate();");		
		
		steppableCode.append("}\n");
		steppableCode.append("public double getInterval(){\n");
		steppableCode.append("return " + updatingFrequency + ";\n");
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
	
	
		
	
	

}
