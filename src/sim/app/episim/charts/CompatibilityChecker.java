package sim.app.episim.charts;

import java.io.ObjectStreamClass;
import java.util.HashMap;
import java.util.Map;

import sim.app.episim.CellType;
import sim.app.episim.model.ModelController;
import sim.app.episim.tissue.TissueType;

import episimexceptions.ModelCompatibilityException;
import episiminterfaces.EpisimChart;
import episiminterfaces.EpisimChartSet;


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
		
		//Check loaded Model-Classes
		checkForCompatibility(ModelController.getInstance().getBioChemicalModelController().getEpisimCellDiffModelGlobalParameters().getClass());
		checkForCompatibility(ModelController.getInstance().getBioChemicalModelController().getNewEpisimCellDiffModelObject().getClass());
		checkForCompatibility(ModelController.getInstance().getBioMechanicalModelController().getEpisimMechanicalModel().getClass());
		checkForCompatibility(ModelController.getInstance().getBioMechanicalModelController().getEpisimMechanicalModelGlobalParameters().getClass());
		
		//Check Tissue-Type and Cell-Types
		checkForCompatibility(actTissue.getClass());
		
		for(Class<? extends CellType> actCellTypeClass: actTissue.getRegiseredCellTypes()){
			checkForCompatibility(actCellTypeClass);
		}
		
		
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
