package episiminterfaces;

import java.util.List;
import java.util.Set;


public interface EpisimDataExport extends java.io.Serializable{
	
	long getId();
	String getName();
	int getDataExportFrequncyInSimulationSteps();
	List<EpisimDataExportColumn> getEpisimDataExportColumns();
	EpisimDataExportColumn getEpisimDataExportColumn(long id);
	Set<Class<?>> getRequiredClasses();
	
	void addRequiredClass(Class<?> requiredClass);
	void addEpisimDataExportColumn(EpisimDataExportColumn column);
	void setName(String val);
	void setDataExportFrequncyInSimulationSteps(int val);
	
	void removeAllEpisimDataExportColumns();
	void removeEpisimDataExportColumn(long id);

}
