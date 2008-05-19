package sim.app.episim.datamonitoring.dataexport;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import episiminterfaces.EpisimDataExportDefinition;
import episiminterfaces.EpisimDataExportDefinitionSet;


public class EpisimDataExportDefinitionSetImpl implements EpisimDataExportDefinitionSet, java.io.Serializable{
	
	private List<EpisimDataExportDefinition> episimDataExportDefinitions;
	
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

		removeEpisimDataExportDefinition(dataExportDefinition.getId());
		addEpisimDataExportDefinition(dataExportDefinition);
		
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


	
	
	
	
	
}
