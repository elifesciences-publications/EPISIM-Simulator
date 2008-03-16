package episiminterfaces;

import java.io.File;
import java.util.List;
import java.util.Set;


public interface EpisimDataExport extends java.io.Serializable{
	
	long getId();
	String getName();
	int getDataExportFrequncyInSimulationSteps();
	List<EpisimDataExportColumn> getEpisimDataExportColumns();
	EpisimDataExportColumn getEpisimDataExportColumn(long id);
	Set<Class<?>> getRequiredClasses();
	File getExportDefinitionPath();
	File getCSVFilePath();
	
	void addRequiredClass(Class<?> requiredClass);
	void addEpisimDataExportColumn(EpisimDataExportColumn column);
	void setName(String val);
	void setDataExportFrequncyInSimulationSteps(int val);
	void setExportDefinitionPath(File path);
	void setCSVFilePath(File path);
	
	void removeAllEpisimDataExportColumns();
	void removeEpisimDataExportColumn(long id);

}
