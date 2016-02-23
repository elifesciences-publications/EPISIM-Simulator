package sim.app.episim.util;

import java.io.ObjectStreamClass;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import sim.app.episim.model.AbstractCell;
import sim.app.episim.model.AbtractTissue;
import sim.app.episim.model.controller.ModelController;
import episimexceptions.ModelCompatibilityException;
import episiminterfaces.monitoring.EpisimChart;
import episiminterfaces.monitoring.EpisimChartSet;
import episiminterfaces.monitoring.EpisimDataExportDefinition;
import episiminterfaces.monitoring.EpisimDataExportDefinitionSet;


public class CompatibilityChecker {
	
	private Map<String, Long> classNameHashValueMap;
	
	public CompatibilityChecker(){
		classNameHashValueMap = new HashMap<String, Long>();		
	}
	
	
	public void checkEpisimChartSetForCompatibility(EpisimChartSet chartSet, AbtractTissue actTissue) throws ModelCompatibilityException{
		classNameHashValueMap.clear();
		if(chartSet == null) throw new IllegalArgumentException("Chart-Set for Compatibility-Check must not be null!");
		for(EpisimChart actChart: chartSet.getEpisimCharts()){
			for(Class<?> actClass : actChart.getAllRequiredClasses()){
				classNameHashValueMap.put(actClass.getCanonicalName(), 
						ObjectStreamClass.lookup(actClass).getSerialVersionUID());
			}
		}		
		checkCellBehavioralAndMechanicalModelClasses();		
	}
	
	public void checkEpisimDataExportDefinitionSetForCompatibility(EpisimDataExportDefinitionSet exportDefinitionSet, AbtractTissue actTissue) throws ModelCompatibilityException{
		classNameHashValueMap.clear();
		if(exportDefinitionSet == null) throw new IllegalArgumentException("Data-Export-Definition-Set for Compatibility-Check must not be null!");
		
		Set<Class<?>> requiredClasses = new HashSet<Class<?>>();
		
		for(EpisimDataExportDefinition exp : exportDefinitionSet.getEpisimDataExportDefinitions()){
			requiredClasses.addAll(exp.getAllRequiredClasses());
		}		
		for(Class<?> actClass : requiredClasses){
			classNameHashValueMap.put(actClass.getCanonicalName(), ObjectStreamClass.lookup(actClass).getSerialVersionUID());
		}				
		checkCellBehavioralAndMechanicalModelClasses();	
	}	
	
	private void checkCellBehavioralAndMechanicalModelClasses() throws ModelCompatibilityException{
		checkForCompatibility(ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getClass());
		checkForCompatibility(ModelController.getInstance().getCellBehavioralModelController().getNewEpisimCellBehavioralModelObject().getClass());
		checkForCompatibility(ModelController.getInstance().getBioMechanicalModelController().getNewEpisimBioMechanicalModelObject(null, null).getClass());
		checkForCompatibility(ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().getClass());
	}
	
	private void checkForCompatibility(Class<?> actClass) throws ModelCompatibilityException{		
		if(classNameHashValueMap.keySet().contains(actClass.getCanonicalName())){
			long registeredValue = classNameHashValueMap.get(actClass.getCanonicalName());
			long actualValue = ObjectStreamClass.lookup(actClass).getSerialVersionUID();
			//System.out.print("Soll: "+ registeredValue + "  Ist: "+ actualValue);
			if(registeredValue != actualValue) throw new ModelCompatibilityException("Class " + actClass.getCanonicalName() + " is not compatible!");
		}		
	}
}
