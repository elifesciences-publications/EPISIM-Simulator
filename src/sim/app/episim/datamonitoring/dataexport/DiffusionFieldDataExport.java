package sim.app.episim.datamonitoring.dataexport;

import episiminterfaces.monitoring.EpisimDiffFieldDataExport;
import sim.app.episim.EpisimProperties;
import sim.app.episim.datamonitoring.dataexport.io.DiffusionFieldDataExportCSVWriter;
import sim.app.episim.util.EnhancedSteppable;
import sim.engine.SimState;


public class DiffusionFieldDataExport {
	
	private EpisimDiffFieldDataExport dataExportConfig;
	private DiffusionFieldDataExportCSVWriter csvWriter;
	public DiffusionFieldDataExport(EpisimDiffFieldDataExport dataExportConfig){
		this.dataExportConfig = dataExportConfig;
		csvWriter = new DiffusionFieldDataExportCSVWriter(dataExportConfig.getCSVFilePath(), dataExportConfig.getDiffusionFieldName(), dataExportConfig.getName(), dataExportConfig.getDescription().replace("\n", ";\n"));
	}
	
	public EnhancedSteppable getSteppable(){
		return new EnhancedSteppable(){			
         public void step(SimState state) {
	         csvWriter.writeDiffusionFieldToDisk();
         }
		
         public double getInterval() {    
         	
         	
							return EpisimProperties.getProperty(EpisimProperties.SIMULATOR_DATAEXPORTUPDATEFREQ)== null
									|| Integer.parseInt(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_DATAEXPORTUPDATEFREQ)) <= 0 ? dataExportConfig.getDataExportFrequncyInSimulationSteps() :
										Integer.parseInt(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_DATAEXPORTUPDATEFREQ));
			
				    
         }			
		};
	}
	
	public DiffusionFieldDataExportCSVWriter getCSVWriter(){
		return csvWriter;
	}
	
	
	
}
