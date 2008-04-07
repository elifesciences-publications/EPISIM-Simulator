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
import episiminterfaces.CalculationHandler;
import episiminterfaces.EpisimCellDiffModel;
import episiminterfaces.EpisimChart;
import episiminterfaces.EpisimChartSeries;


public class GradientSteppableBuilder extends CommonSteppableBuilder{
	
	
	
	protected GradientSteppableBuilder(){
		
	}
	
	public void appendGradientCalucationHandlerRegistration(EpisimChart chart, StringBuffer source){
		for(EpisimChartSeries actSeries: chart.getEpisimChartSeries()){
			if(actSeries.getExpression()[1].startsWith(Names.BUILDGRADIENTHANDLER)) {
				source.append("CalculationController.getInstance().registerForGradientCalculationGradient(");
				source.append(actSeries.getExpression()[1].substring(Names.BUILDGRADIENTHANDLER.length())+", ");
				source.append(Names.convertClassToVariable(Names.cleanString(actSeries.getName())+actSeries.getId())+");\n");
			}
		}
	}
	
	
	

}
