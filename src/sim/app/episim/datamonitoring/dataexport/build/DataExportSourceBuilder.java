package sim.app.episim.datamonitoring.dataexport.build;


import java.io.File;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.datamonitoring.build.AbstractCommonSourceBuilder;
import sim.app.episim.datamonitoring.calc.CalculationAlgorithmServer;
import sim.app.episim.datamonitoring.steppables.SteppableCodeFactory;
import sim.app.episim.util.Names;
import episiminterfaces.calc.CalculationAlgorithmConfigurator;
import episiminterfaces.calc.CalculationAlgorithm.CalculationAlgorithmType;
import episiminterfaces.monitoring.EpisimChartSeries;
import episiminterfaces.monitoring.EpisimDataExportColumn;
import episiminterfaces.monitoring.EpisimDataExportDefinition;




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
		appendGetCSVWriter();
		appendRegisterObjectsMethod(episimDataExportDefinition.getAllRequiredClasses());
		appendEnd();
		
		return generatedSourceCode.toString();
	}
	
	private void appendHeader(){
		
		generatedSourceCode.append("package "+ Names.GENERATEDDATAEXPORTPACKAGENAME +";\n");
		generatedSourceCode.append("import episiminterfaces.*;\n");
		generatedSourceCode.append("import episiminterfaces.calc.*;\n");
		generatedSourceCode.append("import episiminterfaces.monitoring.*;\n");
		generatedSourceCode.append("import episimexceptions.*;\n");
		generatedSourceCode.append("import episimfactories.*;\n");
		generatedSourceCode.append("import sim.app.episim.util.EnhancedSteppable;\n");
		generatedSourceCode.append("import sim.engine.Steppable;\n");
		generatedSourceCode.append("import sim.app.episim.util.GenericBag;\n");
		generatedSourceCode.append("import sim.app.episim.CellType;\n");
		generatedSourceCode.append("import sim.field.continuous.*;\n");
		generatedSourceCode.append("import sim.app.episim.datamonitoring.calc.*;\n");
		generatedSourceCode.append("import sim.app.episim.datamonitoring.dataexport.ObservedDataCollection;\n");
		generatedSourceCode.append("import sim.app.episim.datamonitoring.dataexport.ObservedDataCollection.ObservedDataCollectionType;\n");
		generatedSourceCode.append("import sim.engine.SimState;\n");
		generatedSourceCode.append("import sim.app.episim.datamonitoring.dataexport.io.*;\n");
		generatedSourceCode.append("import java.io.*;\n");
		for(Class<?> actClass: this.actDataExportDefinition.getAllRequiredClasses()){
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
		   	CalculationAlgorithmType type = CalculationAlgorithmServer.getInstance().getCalculationAlgorithmDescriptor(actColumn.getCalculationAlgorithmConfigurator().getCalculationAlgorithmID()).getType();
		   	if(type == CalculationAlgorithmType.ONEDIMDATASERIESRESULT || type == CalculationAlgorithmType.ONEDIMRESULT ||type == CalculationAlgorithmType.HISTOGRAMRESULT){
			   	generatedSourceCode.append("  private ObservedDataCollection<Double> "+Names.convertClassToVariable(Names.cleanString(actColumn.getName())+actColumn.getId())+
			   			" = new ObservedDataCollection<Double>(ObservedDataCollectionType.ONEDIMTYPE);\n");
		   	}
		   	else{
		   		generatedSourceCode.append("  private ObservedDataCollection<Double> "+Names.convertClassToVariable(Names.cleanString(actColumn.getName())+actColumn.getId())+
	   			" = new ObservedDataCollection<Double>(ObservedDataCollectionType.TWODIMTYPE);\n");
		   	}
		   }
		  
		   for(Class<?> actClass : this.actDataExportDefinition.getAllRequiredClasses())
				this.generatedSourceCode.append("  private "+ Names.convertVariableToClass(actClass.getSimpleName())+ " "
						+Names.convertClassToVariable(actClass.getSimpleName())+ ";\n");
	}
	
	private void appendConstructor(){
		generatedSourceCode.append("public " +Names.convertVariableToClass(Names.cleanString(this.actDataExportDefinition.getName())+ this.actDataExportDefinition.getId())+"(){\n");
		
		
	      generatedSourceCode.append("  dataExportCSVWriter = new DataExportCSVWriter(new File(\""+ this.actDataExportDefinition.getCSVFilePath().getPath().replace(File.separatorChar, '/')+"\"), \""+ getColumnNamesString()+"\");\n");
   
	      
	      long counter = 1;
			
			Map <Long, Long> columnCalculationHandlerIDs = new HashMap<Long, Long>();
			
			for(EpisimDataExportColumn column: actDataExportDefinition.getEpisimDataExportColumns()){
				
				columnCalculationHandlerIDs.put(column.getId(), (System.currentTimeMillis()+ counter));
				counter++;
			}
	      
		
		appendHandlerRegistration(columnCalculationHandlerIDs);			
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
	
	private void appendGetCSVWriter(){
		generatedSourceCode.append("public DataExportCSVWriter getCSVWriter(){\n");
		generatedSourceCode.append("   return dataExportCSVWriter;\n");
		generatedSourceCode.append("}\n");
	}
	
	private void appendSteppable(){
		
		generatedSourceCode.append("steppable = "+SteppableCodeFactory.getEnhancedSteppableSourceCode(Names.CALCULATIONCALLBACKLIST, this.actDataExportDefinition.getDataExportFrequncyInSimulationSteps())+";\n");
	
	}
	
	private void appendHandlerRegistration(Map<Long, Long> calculationHandlerIDs){
		
		for(EpisimDataExportColumn actColumn: this.actDataExportDefinition.getEpisimDataExportColumns()){			
			generatedSourceCode.append(Names.CALCULATIONCALLBACKLIST+".add(");
			generatedSourceCode.append("CalculationController.getInstance().registerAtCalculationAlgorithm(");
			generatedSourceCode.append(buildCalculationHandler(calculationHandlerIDs.get(actColumn.getId()), 
					                                             Long.MIN_VALUE, false, actColumn.getCalculationAlgorithmConfigurator(), 
					                                             actColumn.getRequiredClasses()));
			generatedSourceCode.append(", "+Names.convertClassToVariable(Names.cleanString(actColumn.getName())+actColumn.getId())+"));\n");				
		}
	}
	
	private void appendDataMapsRegistration(){
		for(EpisimDataExportColumn actColumn : this.actDataExportDefinition.getEpisimDataExportColumns()){
			generatedSourceCode.append("dataExportCSVWriter.registerObservedDataCollection(" + actColumn.getId()+"l, "+
					Names.convertClassToVariable(Names.cleanString(actColumn.getName())+actColumn.getId())+");\n");
		}
		
	}
	
	
		

}
