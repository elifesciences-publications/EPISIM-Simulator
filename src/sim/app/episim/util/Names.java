package sim.app.episim.util;

import episiminterfaces.EpisimChartSeries;


public abstract class Names {
	private static String [] charactersToRemove = new String[]{";", "/", "\\", ",", ".", ":", "?", "!", "_", "-", "<", ">", "&", "%", "+", "*", " "};
	public static final String BIOCHEMMODEL ="Cell-Behavioral-Model";
	public static final String MECHMODEL ="Biomechanical-Model";
	public static final String MISCALLENEOUS = "Miscalleneous";
	public static final String EPISIMCHARTSETFILENAME ="EpisimChartSet.dat";
	public static final String EPISIMDATAEXPORTFILENAME ="EpisimDataExport.dat";
	public static final String EPISIMCELLDIFFMODELVALUE ="celldiffmodel";
	public static final String GENERATEDCHARTSPACKAGENAME = "generatedcharts";
	public static final String GENERATEDDATAEXPORTPACKAGENAME = "generateddataexports";
	public static final String CELLDIFFMODEL ="_CellDiffModel";
	public static final String EPISIMCHARTSETFACTORYNAME ="EpisimChartSetFactory";
	public static final String EPISIMDATAEXPORTFACTORYNAME ="EpisimDataExportFactory";
	
	public static final String BUILDGRADIENTHANDLER = "buildGradientHandler_";
	public static final String BUILDACMVHANDLER = "buildAllCellsMeanValueHandler_";
	public static final String BUILDCELLHANDLER = "buildOneCellTrackingHandler_";
	
	public static final String GENERATEDGRADIENTFUNCTIONNAME = "gradients";
	
	public static final String GRADBASELINE = "GRADBASELINE";
	
	public static final String CHARTBASELINEEXPRESSIONEDITORROLE = "ChartBaselineExpressionEditorRole";
	public static final String CHARTSERIESEXPRESSIONEDITORROLE = "ChartSeriesExpressionEditorRole";
	public static final String DATAEXPORTEXPRESSIONEDITORROLE = "DataExportExpressionEditorRole";
	
	public static final String DATAEXPORTFILETYPE = ".ede";
	public static final String CHARTSETFILETYPE = ".ecs";
	public static final String MODELFILETYPE = ".jar";
	
	public static final String CONSOLEMAINCONTAINER = "ConsoleMainContainer";
	
	public static String cleanString(String str){
		
		str = str.trim();
		for(String character: charactersToRemove){
			str = str.replace(character, "");
		}
		
		return str;
	}
	
	public static String insertIDIntoCalculationHandlerAndRemovePrefix(String handler, long id){
		String prefixString = "";
		if(handler.startsWith(BUILDACMVHANDLER)) prefixString = BUILDACMVHANDLER;
		else if(handler.startsWith(BUILDCELLHANDLER)) prefixString = BUILDCELLHANDLER;
		else if(handler.startsWith(BUILDGRADIENTHANDLER)) prefixString = BUILDGRADIENTHANDLER;
		
		handler = handler.substring(prefixString.length());
		
		prefixString = "new CalculationHandler(){\n";
		
		handler = handler.substring(prefixString.length());
		
		return prefixString.concat("{id="+id+"l;}\n").concat(handler);
	}
	
	public static String convertClassToVariable(String classname){
		return classname.substring(0,1).toLowerCase() + classname.substring(1);
	}
	
	public static String convertVariableToClass(String variablename){
		return variablename.substring(0,1).toUpperCase() + variablename.substring(1);
	}

	
	public static String getSeriesFunctionName(EpisimChartSeries series){
		return convertClassToVariable(cleanString(series.getName()+series.getId()));
	}
	
	
}
