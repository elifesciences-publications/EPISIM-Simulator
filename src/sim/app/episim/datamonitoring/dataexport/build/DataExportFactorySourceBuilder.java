package sim.app.episim.datamonitoring.dataexport.build;


import sim.app.episim.CellType;
import sim.app.episim.datamonitoring.build.AbstractCommonFactorySourceBuilder;
import sim.app.episim.util.Names;
import episiminterfaces.EpisimCellDiffModel;

import episiminterfaces.EpisimDataExportDefinition;

public class DataExportFactorySourceBuilder  extends AbstractCommonFactorySourceBuilder{
	
	
	
	private EpisimDataExportDefinition actdataExportDefinition;
	
	public DataExportFactorySourceBuilder(){
		super();
		
	}
	
	public String buildEpisimFactorySource(EpisimDataExportDefinition dataExportDefinition){
		if(dataExportDefinition == null) throw new IllegalArgumentException("Data-Export-Definition mustn't be null");
		this.actdataExportDefinition = dataExportDefinition;
		
		
		for(Class<?> actClass:dataExportDefinition.getRequiredClasses()) this.requiredClasses.add(actClass);
		
		
		appendHeader();
		appendDataFields();
		appendConstructor();
		appendRegisterMethod();
		appendCheckForMissingObjectsMethod();
		appendGetSteppableOfDataExportMethod();
		appendRegisterRequiredObjectsAtChartsMethod();
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
		
		this.factorySource.append("  private "+ Names.convertVariableToClass(Names.cleanString(actdataExportDefinition.getName())+ actdataExportDefinition.getId()) +
					" " + Names.convertClassToVariable(Names.cleanString(actdataExportDefinition.getName())+ actdataExportDefinition.getId())+";\n");
		
		this.factorySource.append("  private GeneratedDataExport generatedDataExport;\n");
			
	}
	
	public void appendConstructor(){
		this.factorySource.append("public "+ Names.EPISIMDATAEXPORTFACTORYNAME+"(){\n");
				
		this.factorySource.append("  this."+ Names.convertClassToVariable(Names.cleanString(actdataExportDefinition.getName())+ actdataExportDefinition.getId()) +
					" = new " + Names.convertVariableToClass(Names.cleanString(actdataExportDefinition.getName())+ actdataExportDefinition.getId())+"();\n");
			
		this.factorySource.append("}\n");
	}
	
		
	
	
	
	private void appendGetSteppableOfDataExportMethod(){
		this.factorySource.append("public EnhancedSteppable getSteppableOfDataExport(){\n");
		this.factorySource.append("  return generatedDataExport.getSteppable();\n");
		this.factorySource.append("}\n");
	}
	
	
	
	private void appendRegisterRequiredObjectsAtChartsMethod(){
		this.factorySource.append("private void registerRequiredObjectsAtCharts(){\n");
		
		this.factorySource.append("  this."+ Names.convertClassToVariable(Names.cleanString(actdataExportDefinition.getName())+ actdataExportDefinition.getId()) +
					".registerRequiredObjects(");
		for(Class<?> actClass: actdataExportDefinition.getRequiredClasses()){
				if(!EpisimCellDiffModel.class.isAssignableFrom(actClass) && !CellType.class.isAssignableFrom(actClass)){
					this.factorySource.append(Names.convertClassToVariable(actClass.getSimpleName())+", ");
				}
		}
		this.factorySource.append("allCells, cellContinuous);\n");
		this.factorySource.append("}\n");
	}
	
	
}
