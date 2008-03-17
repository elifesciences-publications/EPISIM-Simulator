package sim.app.episim.datamonitoring.dataexport.build;


import sim.app.episim.datamonitoring.build.AbstractCommonSourceBuilder;
import sim.app.episim.util.Names;
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
		generatedSourceCode.append("import sim.util.Bag;\n");
		generatedSourceCode.append("import sim.field.continuous.*;\n");
		for(Class<?> actClass: this.actDataExportDefinition.getRequiredClasses()){
			generatedSourceCode.append("import " + actClass.getCanonicalName()+";\n");	
		}
		
		generatedSourceCode.append("public class " +Names.convertVariableToClass(Names.cleanString(this.actDataExportDefinition.getName())+ this.actDataExportDefinition.getId())
				+" implements GeneratedDataExport{\n");
	
	}
	
	private void appendDataFields(){
				   
		   
		   generatedSourceCode.append("  private Continuous2D cellContinuous;\n");
		   generatedSourceCode.append("  private Bag allCells;\n");
		 
		  
		   for(Class<?> actClass : this.actDataExportDefinition.getRequiredClasses())
				this.generatedSourceCode.append("  private "+ Names.convertVariableToClass(actClass.getSimpleName())+ " "
						+Names.convertClassToVariable(actClass.getSimpleName())+ ";\n");
	}
	
	private void appendConstructor(){
		generatedSourceCode.append("public " +Names.convertVariableToClass(Names.cleanString(this.actDataExportDefinition.getName())+ this.actDataExportDefinition.getId())+"(){\n");
		
		
		
		generatedSourceCode.append("}\n");
	}
	
	
	
	
	
	
		

}
