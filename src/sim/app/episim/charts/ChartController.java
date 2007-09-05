package sim.app.episim.charts;

import java.awt.Frame;
import java.util.*;

import java.util.concurrent.ConcurrentHashMap;
import java.io.*;

import sim.app.episim.charts.parser.*;
public class ChartController {
	
	private static ChartController instance = null;
	
	private List <ChartMonitoredClass> chartMonitoredClasses;
	
	private ConcurrentHashMap<String, ChartMonitoredCellType> chartMonitoredCellTypes;
	
	private ConcurrentHashMap<String, ChartMonitoredTissue> chartMonitoredTissues;
	
	
	private ChartController(){
		chartMonitoredClasses = new ArrayList<ChartMonitoredClass>();
		chartMonitoredCellTypes = new ConcurrentHashMap<String, ChartMonitoredCellType>();
		chartMonitoredTissues = new ConcurrentHashMap<String, ChartMonitoredTissue>();
	}
	
	
	public synchronized static ChartController getInstance(){
		if(instance == null) instance = new ChartController();
		
		return instance;
	}
	
	public void registerCelltypeForChartMonitoring(ChartMonitoredCellType celltype){
		if(!chartMonitoredCellTypes.containsKey(celltype.getName()))
				chartMonitoredCellTypes.put(celltype.getName(), celltype);
		
	}
   
	public void registerTissueForChartMonitoring(ChartMonitoredTissue tissue){
	if(!chartMonitoredTissues.containsKey(tissue.getTissueName())){
			chartMonitoredTissues.put(tissue.getTissueName(), tissue);
			for(ChartMonitoredCellType actCellType: tissue.getChartMonitoredCellTypes()) registerCelltypeForChartMonitoring(actCellType);
	}
	}
	
	public void showChartCreationWizard(Frame parent){
		ChartCreationWizard wizard = new ChartCreationWizard(parent, "Chart-Creation-Wizard", true);
		
		if(chartMonitoredCellTypes.size()!=0) wizard.showCellTypes(chartMonitoredCellTypes);
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