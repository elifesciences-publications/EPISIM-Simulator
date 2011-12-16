package sim.app.episim.datamonitoring.dataexport;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import sim.app.episim.util.ObjectManipulations;

import episiminterfaces.monitoring.EpisimDataExportDefinition;
import episiminterfaces.monitoring.EpisimDataExportDefinitionSet;
import episiminterfaces.monitoring.EpisimDiffFieldDataExport;


public class EpisimDataExportDefinitionSetImpl implements EpisimDataExportDefinitionSet, java.io.Serializable{
	
	private ArrayList<EpisimDataExportDefinition> episimDataExportDefinitions;
	private ArrayList<EpisimDiffFieldDataExport> episimDiffFieldDataExportDefinitions;
	
	private String name;
	
	private File path;
	
	public EpisimDataExportDefinitionSetImpl(){
		episimDataExportDefinitions = new ArrayList<EpisimDataExportDefinition>();
		episimDiffFieldDataExportDefinitions = new ArrayList<EpisimDiffFieldDataExport>();
		name = "";
	}
	
	public void addEpisimDataExportDefinition(EpisimDataExportDefinition dataExportDefinition) {
		this.episimDataExportDefinitions.add(dataExportDefinition);		
	}
	public void addEpisimDataExportDefinition(EpisimDiffFieldDataExport dataExportDefinition) {
		this.episimDiffFieldDataExportDefinitions.add(dataExportDefinition);
	}	

	public EpisimDataExportDefinition getEpisimDataExportDefinition(long id) {
		for(EpisimDataExportDefinition expDef: episimDataExportDefinitions){
		  if(expDef.getId() == id){ 
			 return expDef;
		  }
		}
		return null;
	}
	public EpisimDiffFieldDataExport getEpisimDiffFieldDataExportDefinition(long id) {
		for(EpisimDiffFieldDataExport expDef: episimDiffFieldDataExportDefinitions){
		  if(expDef.getId() == id){ 
			 return expDef;
		  }
		}
		return null;
	}	

	public List<EpisimDataExportDefinition> getEpisimDataExportDefinitions(){
		return episimDataExportDefinitions;
	}
	public List<EpisimDiffFieldDataExport> getEpisimDiffFieldDataExportDefinitions() {
		 return episimDiffFieldDataExportDefinitions;
	}

	public void removeEpisimDataExportDefinition(long id){
		episimDataExportDefinitions.remove(getEpisimDataExportDefinition(id));		
	}
	public void removeEpisimDiffFieldDataExportDefinition(long id) {
		episimDiffFieldDataExportDefinitions.remove(getEpisimDiffFieldDataExportDefinition(id));		
	}

	

	public void updateDataExportDefinition(EpisimDataExportDefinition dataExportDefinition) {
		EpisimDataExportDefinition episimDefOld =  getEpisimDataExportDefinition(dataExportDefinition.getId());
		int index = episimDataExportDefinitions.indexOf(episimDefOld);
		episimDataExportDefinitions.remove(index);
		episimDataExportDefinitions.add(index, dataExportDefinition);		
	}
	public void updateDataExportDefinition(EpisimDiffFieldDataExport dataExportDefinition) {
		EpisimDiffFieldDataExport episimDefOld =  getEpisimDiffFieldDataExportDefinition(dataExportDefinition.getId());
		int index = episimDiffFieldDataExportDefinitions.indexOf(episimDefOld);
		episimDiffFieldDataExportDefinitions.remove(index);
		episimDiffFieldDataExportDefinitions.add(index, dataExportDefinition);		
	}

	public String getName() {	   
	   return name;
   }
	public void setName(String name) {
	 this.name = name;	   
   }

	public File getPath() {	   
	   return path;
   }
	public void setPath(File path) {
	   this.path = path;	   
   }

	public boolean isOneOfTheDataExportDefinitionsDirty() {
		for(EpisimDataExportDefinition def : this.episimDataExportDefinitions){
			if(def.isDirty()) return true;
		}
		for(EpisimDiffFieldDataExport def : this.episimDiffFieldDataExportDefinitions){
			if(def.isDirty()) return true;
		}
	   return false;
   }

	public EpisimDataExportDefinitionSet clone(){
		EpisimDataExportDefinitionSet newDefinitionSet = ObjectManipulations.cloneObject(this);
		for(EpisimDataExportDefinition oldDefinition : this.getEpisimDataExportDefinitions()){
			newDefinitionSet.removeEpisimDataExportDefinition(oldDefinition.getId());
			newDefinitionSet.addEpisimDataExportDefinition(oldDefinition.clone());
		}
		for(EpisimDiffFieldDataExport oldDefinition : this.getEpisimDiffFieldDataExportDefinitions()){
			newDefinitionSet.removeEpisimDiffFieldDataExportDefinition(oldDefinition.getId());
			newDefinitionSet.addEpisimDataExportDefinition(oldDefinition.clone());
		}		
		return newDefinitionSet;
	}
	

	

	
	
	
	
	
	
}
