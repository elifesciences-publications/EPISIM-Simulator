package sim.app.episim.datamonitoring.dataexport;

import java.util.ArrayList;
import java.util.List;

import episiminterfaces.monitoring.EpisimDiffFieldDataExport;

import sim.app.episim.util.EnhancedSteppable;


public class DiffusionFieldDataExportFactory {
	
	private List<EnhancedSteppable> diffFieldDataExportSteppables;
	private List<DiffusionFieldDataExport> diffFieldDataExports;	
	
	public DiffusionFieldDataExportFactory(List<EpisimDiffFieldDataExport> diffFieldDataExportsDefs){
		diffFieldDataExportSteppables = new ArrayList<EnhancedSteppable>();
		diffFieldDataExports = new ArrayList<DiffusionFieldDataExport>();
		buildDataExportsAndSteppables(diffFieldDataExportsDefs);
	}
	
	private void buildDataExportsAndSteppables(List<EpisimDiffFieldDataExport> diffFieldDataExportsDefs){
		if(diffFieldDataExportsDefs != null && !diffFieldDataExportsDefs.isEmpty()){
			for(EpisimDiffFieldDataExport dataExportDef : diffFieldDataExportsDefs){
				DiffusionFieldDataExport dataExport = new DiffusionFieldDataExport(dataExportDef);
				diffFieldDataExportSteppables.add(dataExport.getSteppable());
				diffFieldDataExports.add(dataExport);
			}			
		}
	}	
	
   public List<DiffusionFieldDataExport> getDiffFieldDataExports(){
	   return diffFieldDataExports;
   }   
   public List<EnhancedSteppable> getDiffFieldDataExportSteppables(){
	   return diffFieldDataExportSteppables;
   }
   
}
