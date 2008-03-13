package sim.app.episim.datamonitoring.dataexport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import episiminterfaces.EpisimDataExport;
import episiminterfaces.EpisimDataExportColumn;


public class EpisimDataExportImpl implements EpisimDataExport {
	
	private final long id;
	private Map<Long, EpisimDataExportColumn> columnMap;
	private int exportFrequency = 1;
	private String name;
	
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
		return values;
   }

	public long getId() { return this.id; }

	public String getName() { return this.name; }
   
	public void removeAllEpisimDataExportColumns() {  this.columnMap.clear(); }	
	
	public void removeEpisimDataExportColumn(long id) {this.columnMap.remove(id); }

	public void setDataExportFrequncyInSimulationSteps(int val){if(val>0) this.exportFrequency = val;}

	public void setName(String val) {if(name != null && !name.trim().equals("")) this.name = val; }

	public void addRequiredClass(Class<?> requiredClass) { this.requiredClasses.add(requiredClass); }

	public Set<Class<?>> getRequiredClasses() { return this.requiredClasses; }

}
