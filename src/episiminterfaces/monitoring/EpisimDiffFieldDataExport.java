package episiminterfaces.monitoring;

import java.io.File;


public interface EpisimDiffFieldDataExport {

	long getId();
	
	void setName(String name);
	String getName();
	
	void setDiffusionFieldName(String name);
	String getDiffusionFieldName();
	
	void setCSVFilePath(File path);
	File getCSVFilePath();
	
	void setDataExportFrequencyInSimulationSteps(int frequency);
	int getDataExportFrequncyInSimulationSteps();

	void setIsDirty(boolean value);
	boolean isDirty();
	
	EpisimDiffFieldDataExport clone();
}
