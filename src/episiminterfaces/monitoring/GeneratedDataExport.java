package episiminterfaces.monitoring;

import sim.app.episim.datamonitoring.dataexport.io.DataExportCSVWriter;
import sim.app.episim.util.EnhancedSteppable;


public interface GeneratedDataExport {
	EnhancedSteppable getSteppable();	
	DataExportCSVWriter getCSVWriter();
}
