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

import sim.app.episim.util.ObjectManipulations;

import episiminterfaces.monitoring.EpisimChartSeries;
import episiminterfaces.monitoring.EpisimDataExportColumn;
import episiminterfaces.monitoring.EpisimDataExportDefinition;


public class EpisimDataExportImpl implements EpisimDataExportDefinition {
	
	private final long id;
	private Map<Long, EpisimDataExportColumn> columnMap;
	private int exportFrequency = 100;
	private String name;
	
	private File csvFilePath;
	
	private boolean isDirty = false;
	
	
	
	public EpisimDataExportImpl(long id){
		this.id = id;
		this.columnMap = new HashMap<Long, EpisimDataExportColumn>();
		
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

	public File getCSVFilePath() { return this.csvFilePath; }

	public void setCSVFilePath(File path) { this.csvFilePath = path; }

	public boolean isDirty() {
	   
	   return isDirty;
   }

	public void setIsDirty(boolean value) {

	   isDirty = value;
	   
   }
	
	public Set<Class<?>> getAllRequiredClasses() {
		Set<Class<?>> allRequiredClasses = new HashSet<Class<?>>();
		for(EpisimDataExportColumn col : this.columnMap.values()){
			allRequiredClasses.addAll(col.getRequiredClasses());
		}
	   return allRequiredClasses;
   }
	
	public Set<String> getAllRequiredClassesNameSet() {
		Set<String> allRequiredClasses = new HashSet<String>();
		for(EpisimDataExportColumn col : this.columnMap.values()){
			if(col instanceof EpisimDataExportColumnImpl){
				allRequiredClasses.addAll(((EpisimDataExportColumnImpl) col).getRequiredClassesNameSet());
			}
		}
		return allRequiredClasses;
	}
	
	public EpisimDataExportDefinition clone(){
		EpisimDataExportDefinition newExport = ObjectManipulations.cloneObject(this);
		for(EpisimDataExportColumn oldColumn : this.getEpisimDataExportColumns()){
			newExport.removeEpisimDataExportColumn(oldColumn.getId());
			newExport.addEpisimDataExportColumn(oldColumn.clone());
		}
		return newExport;
	}
}
