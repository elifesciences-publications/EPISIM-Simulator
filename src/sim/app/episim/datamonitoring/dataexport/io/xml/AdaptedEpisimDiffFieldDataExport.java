package sim.app.episim.datamonitoring.dataexport.io.xml;

import java.io.File;


public class AdaptedEpisimDiffFieldDataExport implements java.io.Serializable{

	private long id;
	private String name;
	private String description;
	private String diffusionFieldName;
	private File csvFilePath;
	private int dataExportFrequncyInSimulationSteps = 100;
	private boolean isDirty = false;
	
	public AdaptedEpisimDiffFieldDataExport(){}

	
   public long getId() {
   
   	return id;
   }

	
   public void setId(long id) {
   
   	this.id = id;
   }

	
   public String getName() {
   
   	return name;
   }

	
   public void setName(String name) {
   
   	this.name = name;
   }

	
   public String getDescription() {
   
   	return description;
   }

	
   public void setDescription(String description) {
   
   	this.description = description;
   }

	
   public String getDiffusionFieldName() {
   
   	return diffusionFieldName;
   }

	
   public void setDiffusionFieldName(String diffusionFieldName) {
   
   	this.diffusionFieldName = diffusionFieldName;
   }

	
   public File getCsvFilePath() {
   
   	return csvFilePath;
   }

	
   public void setCsvFilePath(File csvFilePath) {
   
   	this.csvFilePath = csvFilePath;
   }

	
   public int getDataExportFrequncyInSimulationSteps() {
   
   	return dataExportFrequncyInSimulationSteps;
   }

	
   public void setDataExportFrequncyInSimulationSteps(int dataExportFrequncyInSimulationSteps) {
   
   	this.dataExportFrequncyInSimulationSteps = dataExportFrequncyInSimulationSteps;
   }
	
   public boolean isDirty() {
   
   	return isDirty;
   }
	
   public void setDirty(boolean isDirty) {
   
   	this.isDirty = isDirty;
   }
	
	
}
