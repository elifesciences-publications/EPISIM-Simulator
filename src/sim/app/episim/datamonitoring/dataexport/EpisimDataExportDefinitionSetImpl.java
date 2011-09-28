package sim.app.episim.datamonitoring.dataexport;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import sim.app.episim.util.ObjectManipulations;

import episiminterfaces.monitoring.EpisimDataExportDefinition;
import episiminterfaces.monitoring.EpisimDataExportDefinitionSet;


public class EpisimDataExportDefinitionSetImpl implements EpisimDataExportDefinitionSet, java.io.Serializable{
	
	private ArrayList<EpisimDataExportDefinition> episimDataExportDefinitions;
	
	private String name;
	
	private File path;
	
	public EpisimDataExportDefinitionSetImpl(){
		episimDataExportDefinitions = new ArrayList<EpisimDataExportDefinition>();
		name = "";
	}
	
	public void addEpisimDataExportDefinition(EpisimDataExportDefinition dataExportDefinition) {

		this.episimDataExportDefinitions.add(dataExportDefinition);
		
	}

	public EpisimDataExportDefinition getEpisimDataExportDefinition(long id) {
		  for(EpisimDataExportDefinition expDef: episimDataExportDefinitions){
			  if(expDef.getId() == id){ 
				 return expDef;
			  }
		  }
		   return null;
	}

	public List<EpisimDataExportDefinition> getEpisimDataExportDefinitions() {

		return episimDataExportDefinitions;
	}



	public void removeEpisimDataExportDefinition(long id) {

		episimDataExportDefinitions.remove(getEpisimDataExportDefinition(id));
		
	}

	

	public void updateDataExportDefinition(EpisimDataExportDefinition dataExportDefinition) {

		EpisimDataExportDefinition episimDefOld =  getEpisimDataExportDefinition(dataExportDefinition.getId());
		int index = episimDataExportDefinitions.indexOf(episimDefOld);
		episimDataExportDefinitions.remove(index);
		episimDataExportDefinitions.add(index, dataExportDefinition);
		
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
	   return false;
   }

	public EpisimDataExportDefinitionSet clone(){
		EpisimDataExportDefinitionSet newDefinitionSet = ObjectManipulations.cloneObject(this);
		for(EpisimDataExportDefinition oldDefinition : this.getEpisimDataExportDefinitions()){
			newDefinitionSet.removeEpisimDataExportDefinition(oldDefinition.getId());
			newDefinitionSet.addEpisimDataExportDefinition(oldDefinition.clone());
		}
		
		return newDefinitionSet;
	}
	
	
	
	
	
}
