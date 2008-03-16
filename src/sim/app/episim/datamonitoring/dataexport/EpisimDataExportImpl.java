package sim.app.episim.datamonitoring.dataexport;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import episiminterfaces.EpisimChartSeries;
import episiminterfaces.EpisimDataExport;
import episiminterfaces.EpisimDataExportColumn;


public class EpisimDataExportImpl implements EpisimDataExport {
	
	private final long id;
	private Map<Long, EpisimDataExportColumn> columnMap;
	private int exportFrequency = 1;
	private String name;
	
	private File csvFilePath;
	private File dataExportPath;
	
	private Set<Class<?>> requiredClasses;
	
	public EpisimDataExportImpl(long id){
		this.id = id;
		this.columnMap = new HashMap<Long, EpisimDataExportColumn>();
		requiredClasses = new HashSet<Class<?>>();
	}

	public void addEpisimDataExportColumn(EpisimDataExportColumn column) {

	  if(column != null) this.columnMap.put(column.getId(), column);
	   
   }

	public int getDataExportFrequncyInSimulationSteps() { return this.exportFrequency; }

	public EpisimDataExportColumn getEpisimDataExportColumn(long id) { return this.columnMap.get(id); }

	public List<EpisimDataExportColumn> getEpisimDataExportColumns() { 
		List<EpisimDataExportColumn> values = new ArrayList<EpisimDataExportColumn>();
		values.addAll(this.columnMap.values());
		Collections.sort(values, new Comparator<EpisimDataExportColumn>(){

			public int compare(EpisimDataExportColumn o1, EpisimDataExportColumn o2) {
				if(o1 != null && o2 != null){
					if(o1.getId() < o2.getId()) return -1;
					else if(o1.getId() > o2.getId()) return +1;
    
					else return 0;
				}
				return 0;
         }

		});
		return values;
   }

	public long getId() { return this.id; }

	public String getName() { return this.name; }
   
	public void removeAllEpisimDataExportColumns() {  this.columnMap.clear(); }	
	
	public void removeEpisimDataExportColumn(long id) {this.columnMap.remove(id); }

	public void setDataExportFrequncyInSimulationSteps(int val){if(val>0) this.exportFrequency = val;}

	public void setName(String val) {if(val != null && !val.trim().equals("")) this.name = val; }

	public void addRequiredClass(Class<?> requiredClass) { this.requiredClasses.add(requiredClass); }

	public Set<Class<?>> getRequiredClasses() { return this.requiredClasses; }

	public File getCSVFilePath() { return this.csvFilePath; }

	public void setCSVFilePath(File path) { this.csvFilePath = path; }

	public File getExportDefinitionPath() { return this.dataExportPath; 	}

	public void setExportDefinitionPath(File path) { this.dataExportPath = path; }

}
