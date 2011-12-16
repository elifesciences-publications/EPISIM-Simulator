package episiminterfaces.monitoring;

import java.io.File;
import java.util.List;


public interface EpisimDataExportDefinitionSet extends Cloneable{
	
	String getName();
	File getPath();
	List<EpisimDataExportDefinition> getEpisimDataExportDefinitions();
	List<EpisimDiffFieldDataExport> getEpisimDiffFieldDataExportDefinitions();
	
	
	void setName(String name);
	void setPath(File path);

	void addEpisimDataExportDefinition(EpisimDataExportDefinition dataExportDefinition);
	void updateDataExportDefinition(EpisimDataExportDefinition dataExportDefinition);
	
	void addEpisimDataExportDefinition(EpisimDiffFieldDataExport dataExportDefinition);
	void updateDataExportDefinition(EpisimDiffFieldDataExport dataExportDefinition);
	
	EpisimDataExportDefinition getEpisimDataExportDefinition(long id);
	EpisimDiffFieldDataExport getEpisimDiffFieldDataExportDefinition(long id);
	
	void removeEpisimDataExportDefinition(long id);
	void removeEpisimDiffFieldDataExportDefinition(long id);
	
	boolean isOneOfTheDataExportDefinitionsDirty();
	
	EpisimDataExportDefinitionSet clone();
	
}
