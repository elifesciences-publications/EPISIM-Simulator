package episiminterfaces.monitoring;

import java.io.File;
import java.util.List;
import java.util.Set;



public interface EpisimDataExportDefinition extends java.io.Serializable, Cloneable{
	
	long getId();
	String getName();
	String getDescription();
	int getDataExportFrequncyInSimulationSteps();
	List<EpisimDataExportColumn> getEpisimDataExportColumns();
	EpisimDataExportColumn getEpisimDataExportColumn(long id);
	Set<Class<?>> getAllRequiredClasses();
	
	File getCSVFilePath();
	
	
	void addEpisimDataExportColumn(EpisimDataExportColumn column);
	void setName(String val);
	void setDescription(String val);
	void setDataExportFrequncyInSimulationSteps(int val);
	void setCSVFilePath(File path);
	
	void removeAllEpisimDataExportColumns();
	void removeEpisimDataExportColumn(long id);
	
	void setIsDirty(boolean value);
	boolean isDirty();
	
	EpisimDataExportDefinition clone();

}
