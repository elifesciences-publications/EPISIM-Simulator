package sim.app.episim.util;


public abstract class Names {
	private static String [] charactersToRemove = new String[]{";", "/", "\\", ",", ".", ":", "?", "!", "_", "-", "<", ">", "&", "%", "+", "*", " "};
	public static final String BIOCHEMMODEL ="Biochemical-Model";
	public static final String MECHMODEL ="Biomechanical-Model";
	public static final String EPISIMCHARTSETFILENAME ="EpisimChartSet.dat";
	public static final String EPISIMCELLDIFFMODELVALUE ="celldiffmodel";
	public static final String GENERATEDCHARTSPACKAGENAME = "generatedcharts";
	
	public static final String EPISIMCHARTSETFACTORYNAME ="EpisimChartSetFactory";
	
	
	
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

}
