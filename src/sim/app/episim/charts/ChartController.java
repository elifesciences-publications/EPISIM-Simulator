package sim.app.episim.charts;

import java.awt.Frame;
import java.util.*;

import java.util.concurrent.ConcurrentHashMap;
import java.io.*;

import sim.app.episim.CellType;
import sim.app.episim.TissueType;

import sim.app.episim.charts.parser.*;
import sim.app.episim.util.TissueCellDataFieldsInspector;
public class ChartController {
	
	private static ChartController instance = null;
	
	
	
	private long nextChartId = 0;
	
	private TissueType chartMonitoredTissue;
	private EpisimChartSet actLoadedChartSet;
	private Set<String> markerPrefixes;
	private Set<Class<?>> validDataTypes;
	
	private ChartController(){
		
		markerPrefixes = new HashSet<String>();
		validDataTypes = new HashSet<Class<?>>();
		
		markerPrefixes.add("get");
		markerPrefixes.add("is");
		
		validDataTypes.add(Integer.TYPE);
		validDataTypes.add(Short.TYPE);
		validDataTypes.add(Byte.TYPE);
		validDataTypes.add(Long.TYPE);
		validDataTypes.add(Float.TYPE);
		validDataTypes.add(Double.TYPE);
		
	}
	
	public long getNextChartId(){
	
		return System.currentTimeMillis() + (this.nextChartId++);
	}
		
	public synchronized static ChartController getInstance(){
		if(instance == null) instance = new ChartController();
		
		return instance;
	}
	   
	public void setChartMonitoredTissue(TissueType tissue){
		this.chartMonitoredTissue = tissue;
	}
	
	protected EpisimChart showChartCreationWizard(Frame parent){
		ChartCreationWizard wizard = new ChartCreationWizard(parent, "Chart-Creation-Wizard", true, 
		new TissueCellDataFieldsInspector(this.chartMonitoredTissue, this.markerPrefixes, this.validDataTypes));
		
		if(this.chartMonitoredTissue != null) 
			wizard.showWizard();
		return wizard.getEpisimChart();
	}
	
	public void showChartSetDialog(Frame parent){
		ChartSetDialog dialog = new ChartSetDialog(parent, "Episim-Chart-Set", true);
		
		if(this.chartMonitoredTissue != null){ 
			if(this.actLoadedChartSet == null){
				this.actLoadedChartSet = new EpisimChartSetImpl();
				
			}
			dialog.showChartSet(actLoadedChartSet);
		}
	}
	
	public String checkChartExpression(String expression, Set<String> varNameSet) throws ParseException,TokenMgrError{
		
		String result = "";
		
	   if(expression != null && varNameSet != null){
		 StringReader sr = new java.io.StringReader(expression);
	    Reader r = new java.io.BufferedReader( sr );
	    ChartExpressionChecker parser = new ChartExpressionChecker(r);
	    result = parser.Start(varNameSet);
	    
	   }
		return result;
	}

}