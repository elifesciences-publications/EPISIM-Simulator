package sim.app.episim.util;

import java.io.ObjectStreamClass;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import sim.app.episim.CellType;
import sim.app.episim.model.ModelController;
import sim.app.episim.tissue.TissueType;

import episimexceptions.ModelCompatibilityException;
import episiminterfaces.EpisimChart;
import episiminterfaces.EpisimChartSet;
import episiminterfaces.EpisimDataExportDefinition;
import episiminterfaces.EpisimDataExportDefinitionSet;


public class CompatibilityChecker {
	
	private Map<String, Long> classNameHashValueMap;
	
	public CompatibilityChecker(){
		classNameHashValueMap = new HashMap<String, Long>();		
	}
	
	
	public void checkEpisimChartSetForCompatibility(EpisimChartSet chartSet, TissueType actTissue) throws ModelCompatibilityException{
		classNameHashValueMap.clear();
		if(chartSet == null) throw new IllegalArgumentException("Chart-Set for Compatibility-Check must not be null!");
		for(EpisimChart actChart: chartSet.getEpisimCharts()){
			for(Class<?> actClass : actChart.getRequiredClasses()){
				classNameHashValueMap.put(actClass.getCanonicalName(), 
						ObjectStreamClass.lookup(actClass).getSerialVersionUID());
			}
		}
		
		checkCellDiffAndMechanicalModelClasses();
		checkTissueAndCellTypes(actTissue);			
	}
	
	public void checkEpisimDataExportDefinitionSetForCompatibility(EpisimDataExportDefinitionSet exportDefinitionSet, TissueType actTissue) throws ModelCompatibilityException{
		classNameHashValueMap.clear();
		if(exportDefinitionSet == null) throw new IllegalArgumentException("Data-Export-Definition-Set for Compatibility-Check must not be null!");
		
		Set<Class<?>> requiredClasses = new HashSet<Class<?>>();
		
		for(EpisimDataExportDefinition exp : exportDefinitionSet.getEpisimDataExportDefinitions()){
			requiredClasses.addAll(exp.getRequiredClasses());
		}
		
		for(Class<?> actClass : requiredClasses){
			classNameHashValueMap.put(actClass.getCanonicalName(), 
						ObjectStreamClass.lookup(actClass).getSerialVersionUID());
		}
				
		checkCellDiffAndMechanicalModelClasses();
		checkTissueAndCellTypes(actTissue);
	}
	
	 
	
	
	private void checkTissueAndCellTypes(TissueType actTissue) throws ModelCompatibilityException{
		
		checkForCompatibility(actTissue.getClass());
		
		for(Class<? extends CellType> actCellTypeClass: actTissue.getRegisteredCellTypes()){
			checkForCompatibility(actCellTypeClass);
		}
	}
	
	private void checkCellDiffAndMechanicalModelClasses() throws ModelCompatibilityException{
		
		checkForCompatibility(ModelController.getInstance().getBioChemicalModelController().getEpisimCellDiffModelGlobalParameters().getClass());
		checkForCompatibility(ModelController.getInstance().getBioChemicalModelController().getNewEpisimCellDiffModelObject().getClass());
		checkForCompatibility(ModelController.getInstance().getBioMechanicalModelController().getEpisimMechanicalModel().getClass());
		checkForCompatibility(ModelController.getInstance().getBioMechanicalModelController().getEpisimMechanicalModelGlobalParameters().getClass());
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
