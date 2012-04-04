package sim.app.episim.datamonitoring.dataexport.io.xml;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import sim.app.episim.datamonitoring.dataexport.EpisimDiffFieldDataExportImpl;

import episiminterfaces.monitoring.EpisimDiffFieldDataExport;


public class EpisimDiffFieldDataExportAdapter extends XmlAdapter<AdaptedEpisimDiffFieldDataExport, EpisimDiffFieldDataExport> implements java.io.Serializable{

   public EpisimDiffFieldDataExport unmarshal(AdaptedEpisimDiffFieldDataExport v) throws Exception {
   	EpisimDiffFieldDataExportImpl dataExport = new EpisimDiffFieldDataExportImpl(v.getId());
   	dataExport.setCSVFilePath(v.getCsvFilePath());
   	dataExport.setDataExportFrequencyInSimulationSteps(v.getDataExportFrequncyInSimulationSteps());
   	dataExport.setDescription(v.getDescription());
   	dataExport.setDiffusionFieldName(v.getDiffusionFieldName());
   	dataExport.setIsDirty(v.isDirty());
   	dataExport.setName(v.getName());
   	
	   return dataExport;
   }

	
   public AdaptedEpisimDiffFieldDataExport marshal(EpisimDiffFieldDataExport v) throws Exception {

		AdaptedEpisimDiffFieldDataExport dataExport = new AdaptedEpisimDiffFieldDataExport();
		dataExport.setCsvFilePath(v.getCSVFilePath());
		dataExport.setDataExportFrequncyInSimulationSteps(v.getDataExportFrequncyInSimulationSteps());
		dataExport.setDescription(v.getDescription());
		dataExport.setDiffusionFieldName(v.getDiffusionFieldName());
		dataExport.setDirty(v.isDirty());
		dataExport.setId(v.getId());
		dataExport.setName(v.getName());
	   return dataExport;
   }

}
