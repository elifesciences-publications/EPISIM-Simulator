package sim.app.episim.datamonitoring.dataexport.build;


import java.util.List;

import sim.app.episim.CellType;
import sim.app.episim.datamonitoring.build.AbstractCommonFactorySourceBuilder;
import sim.app.episim.util.Names;
import episiminterfaces.EpisimCellDiffModel;
import episiminterfaces.EpisimDataExportDefinitionSet;
import episiminterfaces.GeneratedDataExport;

import episiminterfaces.EpisimDataExportDefinition;

public class DataExportFactorySourceBuilder  extends AbstractCommonFactorySourceBuilder{
	
	
	
	private EpisimDataExportDefinitionSet actdataExportDefinitionSet;
	
	public DataExportFactorySourceBuilder(){
		super();
		
	}
	
	public String buildEpisimFactorySource(EpisimDataExportDefinitionSet dataExportDefinitionSet){
		if(dataExportDefinitionSet == null) throw new IllegalArgumentException("Data-Export-Definition mustn't be null");
		this.actdataExportDefinitionSet = dataExportDefinitionSet;
		
		for(EpisimDataExportDefinition dataExportDefinition: dataExportDefinitionSet.getEpisimDataExportDefinitions()){
			for(Class<?> actClass:dataExportDefinition.getRequiredClasses()) this.requiredClasses.add(actClass);
		}
		
		
		appendHeader();
		appendDataFields();
		appendConstructor();
		appendRegisterMethod();
		appendCheckForMissingObjectsMethod();
		appendGetGeneratedDataExportsMethod();
		appendGetSteppableOfDataExportMethod();
		appendRegisterRequiredObjectsMethod();
		appendEnd();
		
		return factorySource.toString();
	}
	
	public void appendHeader(){
		super.appendHeader();
		this.factorySource.append("import "+Names.GENERATEDDATAEXPORTPACKAGENAME+".*;\n");

		this.factorySource.append("public class "+ Names.EPISIMDATAEXPORTFACTORYNAME+" extends AbstractDataExportFactory{\n");
	}
	
	public void appendDataFields(){
		
		super.appendDataFields();
		for(EpisimDataExportDefinition actdataExportDefinition: actdataExportDefinitionSet.getEpisimDataExportDefinitions()){
			this.factorySource.append("  private "+ Names.convertVariableToClass(Names.cleanString(actdataExportDefinition.getName())+ actdataExportDefinition.getId()) +
						" " + Names.convertClassToVariable(Names.cleanString(actdataExportDefinition.getName())+ actdataExportDefinition.getId())+";\n");
		}
		this.factorySource.append("  private List<GeneratedDataExport> allDataExports;\n");
			
	}
	
	public void appendConstructor(){
		this.factorySource.append("public "+ Names.EPISIMDATAEXPORTFACTORYNAME+"(){\n");
		this.factorySource.append("  this.allDataExports = new ArrayList<GeneratedDataExport>();\n");
		for(EpisimDataExportDefinition actdataExportDefinition: actdataExportDefinitionSet.getEpisimDataExportDefinitions()){
			this.factorySource.append("  this."+ Names.convertClassToVariable(Names.cleanString(actdataExportDefinition.getName())+ actdataExportDefinition.getId()) +
						" = new " + Names.convertVariableToClass(Names.cleanString(actdataExportDefinition.getName())+ actdataExportDefinition.getId())+"();\n");
			this.factorySource.append("  this.allDataExports.add(this."+ Names.convertClassToVariable(Names.cleanString(actdataExportDefinition.getName())+ actdataExportDefinition.getId())+");\n");
		}	
		this.factorySource.append("}\n");
	}
	
	private void appendGetSteppableOfDataExportMethod(){
		this.factorySource.append("public List<EnhancedSteppable> getSteppablesOfDataExports(){\n");
		this.factorySource.append("  List<EnhancedSteppable> dataExportSteppables = new ArrayList<EnhancedSteppable>();\n");
		this.factorySource.append("  for(GeneratedDataExport actExport : allDataExports){\n");
		this.factorySource.append("    dataExportSteppables.add(actExport.getSteppable());\n");
		this.factorySource.append("  }\n");
		this.factorySource.append("  return dataExportSteppables;\n");
		this.factorySource.append("}\n");
	}
	
	private void appendGetGeneratedDataExportsMethod(){
		this.factorySource.append("public List<GeneratedDataExport> getDataExports(){\n");
		this.factorySource.append("  return allDataExports;\n");
		this.factorySource.append("}\n");
	}
	private void appendRegisterRequiredObjectsMethod(){
		this.factorySource.append("private void registerRequiredObjects(){\n");
		for(EpisimDataExportDefinition actdataExportDefinition: actdataExportDefinitionSet.getEpisimDataExportDefinitions()){
			this.factorySource.append("  this."+ Names.convertClassToVariable(Names.cleanString(actdataExportDefinition.getName())+ actdataExportDefinition.getId()) +
						".registerRequiredObjects(");
			for(Class<?> actClass: actdataExportDefinition.getRequiredClasses()){
					if(!EpisimCellDiffModel.class.isAssignableFrom(actClass) && !CellType.class.isAssignableFrom(actClass)){
						this.factorySource.append(Names.convertClassToVariable(actClass.getSimpleName())+", ");
					}
			}
			this.factorySource.append("allCells, cellContinuous);\n");
		}
		this.factorySource.append("}\n");
	}
	
	
}