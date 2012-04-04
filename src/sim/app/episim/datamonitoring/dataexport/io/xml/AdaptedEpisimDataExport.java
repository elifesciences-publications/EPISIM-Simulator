package sim.app.episim.datamonitoring.dataexport.io.xml;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import episiminterfaces.monitoring.EpisimDataExportColumn;


public class AdaptedEpisimDataExport implements java.io.Serializable{
	
	private long id;
	private List<EpisimDataExportColumn> dataExportColumns;
	private int exportFrequency = 100;
	private String name;
	private String description="";
	
	private File csvFilePath;
	
	private boolean isDirty = false;
	
	public AdaptedEpisimDataExport(){}

	
   public long getId() {
   
   	return id;
   }

	
   public void setId(long id) {
   
   	this.id = id;
   }

   @XmlElement
	@XmlJavaTypeAdapter(EpisimDataExportColumnAdapter.class)
   public List<EpisimDataExportColumn> getDataExportColumns() {
   
   	return dataExportColumns;
   }

	
   public void setDataExportColumns(List<EpisimDataExportColumn> dataExportColumns) {
   
   	this.dataExportColumns = dataExportColumns;
   }

	
   public int getExportFrequency() {
   
   	return exportFrequency;
   }

	
   public void setExportFrequency(int exportFrequency) {
   
   	this.exportFrequency = exportFrequency;
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

	
   public File getCsvFilePath() {
   
   	return csvFilePath;
   }

	
   public void setCsvFilePath(File csvFilePath) {
   
   	this.csvFilePath = csvFilePath;
   }

	
   public boolean isDirty() {
   
   	return isDirty;
   }

	
   public void setDirty(boolean isDirty) {
   
   	this.isDirty = isDirty;
   }

}
