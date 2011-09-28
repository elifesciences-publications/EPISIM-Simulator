package episiminterfaces.monitoring;

import java.io.File;
import java.util.List;


public interface EpisimDataExportDefinitionSet extends Cloneable{
	
	String getName();
	File getPath();
	List<EpisimDataExportDefinition> getEpisimDataExportDefinitions();
	
	
	void setName(String name);
	void setPath(File path);

	void addEpisimDataExportDefinition(EpisimDataExportDefinition dataExportDefinition);
	void updateDataExportDefinition(EpisimDataExportDefinition dataExportDefinition);
	EpisimDataExportDefinition getEpisimDataExportDefinition(long id);
	void removeEpisimDataExportDefinition(long id);
	boolean isOneOfTheDataExportDefinitionsDirty();
	
	EpisimDataExportDefinitionSet clone();
	
}
