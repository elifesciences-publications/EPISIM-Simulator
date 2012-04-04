package sim.app.episim.datamonitoring.dataexport.io.xml;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import sim.app.episim.datamonitoring.dataexport.EpisimDataExportImpl;

import episiminterfaces.monitoring.EpisimDataExportColumn;
import episiminterfaces.monitoring.EpisimDataExportDefinition;


public class EpisimDataExportAdapter extends XmlAdapter<AdaptedEpisimDataExport, EpisimDataExportDefinition> implements java.io.Serializable{

	
   public EpisimDataExportDefinition unmarshal(AdaptedEpisimDataExport v) throws Exception {
   	EpisimDataExportImpl dataExport = new EpisimDataExportImpl(v.getId());
	   dataExport.setCSVFilePath(v.getCsvFilePath());
	   dataExport.setDataExportFrequncyInSimulationSteps(v.getExportFrequency());
	   dataExport.setDescription(v.getDescription());
	   dataExport.setIsDirty(v.isDirty());
	   dataExport.setName(v.getName());
	   for(EpisimDataExportColumn col : v.getDataExportColumns()){
	   	dataExport.addEpisimDataExportColumn(col);
	   }
	   return dataExport;
   }

	
   public AdaptedEpisimDataExport marshal(EpisimDataExportDefinition v) throws Exception {
   	AdaptedEpisimDataExport dataExport = new AdaptedEpisimDataExport();
   	dataExport.setCsvFilePath(v.getCSVFilePath());
   	dataExport.setDataExportColumns(v.getEpisimDataExportColumns());
   	dataExport.setDescription(v.getDescription());
   	dataExport.setDirty(v.isDirty());
   	dataExport.setExportFrequency(v.getDataExportFrequncyInSimulationSteps());
   	dataExport.setId(v.getId());
   	dataExport.setName(v.getName());
	   return dataExport;
   }

}
