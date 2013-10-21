package sim.app.episim.util;

import episiminterfaces.calc.CalculationAlgorithm;
import episiminterfaces.monitoring.EpisimChartSeries;


public abstract class Names {
	private static String [] charactersToRemove = new String[]{";", "/", "\\", ",", ".", ":", "?", "!", "_", "-", "<", ">", "&", "%", "+", "*", " " ,"}","{",")","(", "|","'","´","`","°","~","§","ß", "\""};
	public static final String BIOCHEM_MODEL ="Cell-Behavioral-Model";
	public static final String EPISIM_TEXTOUT ="Episim Text Output";
	public static final String MECH_MODEL ="Biomechanical-Model";
	public static final String MISCALLENEOUS = "Miscalleneous";
	public static final String EPISIM_CHARTSET_FILENAME ="EpisimChartSet.dat";
	public static final String EPISIM_CHARTSET_XML_FILENAME ="EpisimChartSet.xml";
	public static final String EPISIM_DATAEXPORT_FILENAME ="EpisimDataExport.dat";
	public static final String EPISIM_DATAEXPORT_XML_FILENAME ="EpisimDataExport.xml";
	public static final String EPISIM_CELLBEHAVIORAL_MODEL_VALUE ="cellbehavioralmodel";
	public static final String GENERATED_CHARTS_PACKAGENAME = "generatedcharts";
	public static final String GENERATED_DATAEXPORT_PACKAGENAME = "generateddataexports";
	public static final String CELLBEHAVIORAL_MODEL ="_CBM";
	public static final String EPISIM_CHARTSET_FACTORYNAME ="EpisimChartSetFactory";
	public static final String EPISIM_DATAEXPORT_FACTORYNAME ="EpisimDataExportFactory";
	
	public static final String CELL_COLORING_MODE_NAME_I ="cellcoloringmode";
	public static final String CELL_COLORING_MODE_NAME_II ="cellcoloring";
	public static final String CELL_COLORING_MODE_NAME_III ="coloringmode";
	
	
	public static final String NUMBER_PREFIX = "n_";
	public static final String BOOLEAN_PREFIX = "b_";
	public static final String CELLTYPE_PREFIX = "c_";
	public static final String DIFFLEVEL_PREFIX = "d_";
	public static final int PREFIX_LENGTH = 2;
	
	
	public static final String BUILD_GRADIENT_HANDLER = "buildGradientHandler_";
	public static final String BUILD_ACMV_HANDLER = "buildAllCellsMeanValueHandler_";
	public static final String BUILD_CELL_HANDLER = "buildOneCellTrackingHandler_";
	
	public static final String GENERATED_GRADIENT_FUNCTIONNAME = "gradients";
	
	
	public static final String DATAEXPORT_FILETYPE = ".ede";
	public static final String CHARTSET_FILETYPE = ".ecs";
	public static final String MODEL_FILETYPE = ".jar";
	
	public static final String CONSOLE_MAIN_CONTAINER = "ConsoleMainContainer";
	
	
	public static final String CALCULATION_CALLBACK_LIST = "calculationCallbacks";
	
	
	public static final String TRUE_RETURNING_METHOD = "getThisMethodReturnsAlwaysTrue";
	
	public static String cleanString(String str){
		
		str = str.trim();
		for(String character: charactersToRemove){
			str = str.replace(character, "");
		}
		
		return str;
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
