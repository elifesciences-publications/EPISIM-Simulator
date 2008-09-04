package sim.app.episim.datamonitoring.dataexport.build;


import java.io.File;
import java.net.MalformedURLException;

import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.datamonitoring.build.AbstractCommonSourceBuilder;
import sim.app.episim.datamonitoring.steppables.SteppableCodeFactory;
import sim.app.episim.util.Names;
import episiminterfaces.EpisimChartSeries;
import episiminterfaces.EpisimDataExportColumn;
import episiminterfaces.EpisimDataExportDefinition;




public class DataExportSourceBuilder extends AbstractCommonSourceBuilder {
	
	
	
	
	private EpisimDataExportDefinition actDataExportDefinition;
	public DataExportSourceBuilder(){
		
	}
	
	public String buildEpisimDataExportSource(EpisimDataExportDefinition episimDataExportDefinition){
		if(episimDataExportDefinition ==  null) throw new IllegalArgumentException("Episim-Data-Export-Definition was null!");
		this.actDataExportDefinition = episimDataExportDefinition;
		generatedSourceCode = new StringBuffer();
		appendHeader();
		appendDataFields();
		appendConstructor();
		appendStandardMethods();
		appendNewSimulationRun();
		appendRegisterObjectsMethod(episimDataExportDefinition.getRequiredClasses());
		appendEnd();
		
		return generatedSourceCode.toString();
	}
	
	private void appendHeader(){
		
		generatedSourceCode.append("package "+ Names.GENERATEDDATAEXPORTPACKAGENAME +";\n");
		generatedSourceCode.append("import episiminterfaces.*;\n");
		generatedSourceCode.append("import episimexceptions.*;\n");
		generatedSourceCode.append("import episimfactories.*;\n");
		generatedSourceCode.append("import sim.app.episim.util.EnhancedSteppable;\n");
		generatedSourceCode.append("import sim.engine.Steppable;\n");
		generatedSourceCode.append("import sim.app.episim.util.GenericBag;\n");
		generatedSourceCode.append("import sim.app.episim.CellType;\n");
		generatedSourceCode.append("import sim.field.continuous.*;\n");
		generatedSourceCode.append("import sim.app.episim.datamonitoring.calc.*;\n");
		generatedSourceCode.append("import sim.app.episim.datamonitoring.dataexport.ObservedHashMap;\n");
		generatedSourceCode.append("import sim.engine.SimState;\n");
		generatedSourceCode.append("import sim.app.episim.datamonitoring.dataexport.io.*;\n");
		generatedSourceCode.append("import java.io.*;\n");
		for(Class<?> actClass: this.actDataExportDefinition.getRequiredClasses()){
			generatedSourceCode.append("import " + actClass.getCanonicalName()+";\n");	
		}
		
		generatedSourceCode.append("public class " +Names.convertVariableToClass(Names.cleanString(this.actDataExportDefinition.getName())+ this.actDataExportDefinition.getId())
				+" implements GeneratedDataExport{\n");
	
	}
	
	protected void appendDataFields(){
				   
		   super.appendDataFields();
		   generatedSourceCode.append("  private Continuous2D cellContinuous;\n");
		   generatedSourceCode.append("  private GenericBag<CellType> allCells;\n");
		   generatedSourceCode.append("  private DataExportCSVWriter dataExportCSVWriter;\n");
		   
		   for(EpisimDataExportColumn actColumn: this.actDataExportDefinition.getEpisimDataExportColumns()){
		   	generatedSourceCode.append("  private ObservedHashMap<Double, Double> "+Names.convertClassToVariable(Names.cleanString(actColumn.getName())+actColumn.getId())+
		   			" = new ObservedHashMap<Double, Double>();\n");
		   }
		  
		   for(Class<?> actClass : this.actDataExportDefinition.getRequiredClasses())
				this.generatedSourceCode.append("  private "+ Names.convertVariableToClass(actClass.getSimpleName())+ " "
						+Names.convertClassToVariable(actClass.getSimpleName())+ ";\n");
	}
	
	private void appendConstructor(){
		generatedSourceCode.append("public " +Names.convertVariableToClass(Names.cleanString(this.actDataExportDefinition.getName())+ this.actDataExportDefinition.getId())+"(){\n");
		
		
	      generatedSourceCode.append("  dataExportCSVWriter = new DataExportCSVWriter(new File(\""+ this.actDataExportDefinition.getCSVFilePath().getPath().replace(File.separatorChar, '/')+"\"), \""+ getColumnNamesString()+"\");\n");
   
		
		appendHandlerRegistration();			
		appendSteppable();
		appendDataMapsRegistration();
		
		generatedSourceCode.append("}\n");
	}
	
	private String getColumnNamesString(){
		String result = "";
		for(EpisimDataExportColumn actColumn : this.actDataExportDefinition.getEpisimDataExportColumns()){
			result = result.concat(actColumn.getName());
			result = result.concat(";");
		}
		result = result.concat("\\n");
		
		return result;
	}
	
	private void appendNewSimulationRun(){
		generatedSourceCode.append("public void newSimulationRun(){\n");
		generatedSourceCode.append("   dataExportCSVWriter.newSimulationRun();\n");
		generatedSourceCode.append("}\n");
	}
	
	private void appendSteppable(){
		
		generatedSourceCode.append("steppable = "+SteppableCodeFactory.getEnhancedSteppableSourceCodeforDataExport(actDataExportDefinition)+";\n");
	
	}
	
	private void appendHandlerRegistration(){
		SteppableCodeFactory.appendCalucationHandlerRegistration(actDataExportDefinition, generatedSourceCode);
	}
	
	private void appendDataMapsRegistration(){
		for(EpisimDataExportColumn actColumn : this.actDataExportDefinition.getEpisimDataExportColumns()){
			generatedSourceCode.append("dataExportCSVWriter.registerObservedHashMap(" + actColumn.getId()+"l, "+
					Names.convertClassToVariable(Names.cleanString(actColumn.getName())+actColumn.getId())+");\n");
		}
		
	}
	
	
		

}
