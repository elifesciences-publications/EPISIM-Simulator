package sim.app.episim.util;


public abstract class Names {
	private static String [] charactersToRemove = new String[]{";", "/", "\\", ",", ".", ":", "?", "!", "_", "-", "<", ">", "&", "%", "+", "*", " "};
	public static final String BIOCHEMMODEL ="Biochemical-Model";
	public static final String MECHMODEL ="Biomechanical-Model";
	public static final String EPISIMCHARTSETFILENAME ="EpisimChartSet.dat";
	public static final String EPISIMCELLDIFFMODELVALUE ="celldiffmodel";
	
	public static final String EPISIMCHARTSETFACTORYNAME ="EpisimChartSetFactory";
	
	
	
	public static String cleanString(String str){
		
		str = str.trim();
		for(String character: charactersToRemove){
			str = str.replace(character, "");
		}
		
		return str;
	}

}
