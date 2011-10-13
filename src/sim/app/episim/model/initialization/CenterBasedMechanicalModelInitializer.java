package sim.app.episim.model.initialization;

import java.io.File;
import java.util.ArrayList;

import episiminterfaces.EpisimPortrayal;

import sim.app.episim.AbstractCell;
import sim.app.episim.CellInspector;
import sim.app.episim.UniversalCell;
import sim.app.episim.datamonitoring.GlobalStatistics;
import sim.app.episim.model.biomechanics.centerbased.CenterBasedMechanicalModelGlobalParameters;
import sim.app.episim.model.biomechanics.centerbased.CenterBasedMechanicalModel;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.visualization.UniversalCellPortrayal2D;
import sim.app.episim.tissue.TissueController;
import sim.app.episim.gui.EpisimGUIState;
import sim.display.GUIState;
import sim.portrayal.Inspector;
import sim.portrayal.LocationWrapper;
import sim.portrayal.Portrayal;
import sim.util.Double2D;


public class CenterBasedMechanicalModelInitializer extends BiomechanicalModelInitializer {
	
	
	public CenterBasedMechanicalModelInitializer(){
		super();
		TissueController.getInstance().getTissueBorder().loadStandardMembrane();
	}
	
	public CenterBasedMechanicalModelInitializer(File modelInitializationFile){
		super(modelInitializationFile);
	}	

	private final double depthFrac(double y) // wie tief ist in prozent die uebergebene y-position relativ zu rete tiefe
	{
	     return (y-TissueController.getInstance().getTissueBorder().getUndulationBaseLine())/ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().getBasalAmplitude_mikron();                
	}	
	
   protected ArrayList<UniversalCell> buildStandardInitialCellEnsemble() {
   	
   	ArrayList<UniversalCell> standardCellEnsemble = new ArrayList<UniversalCell>();
   	
   	CenterBasedMechanicalModelGlobalParameters biomechanicalModelGlobalParameters = (CenterBasedMechanicalModelGlobalParameters) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();   	
   	
   	Double2D lastloc = new Double2D(2, TissueController.getInstance().getTissueBorder().lowerBoundInMikron(2));
		for(double x = 2; x <= TissueController.getInstance().getTissueBorder().getWidthInPixels(); x += 2){
			Double2D newloc = new Double2D(x, TissueController.getInstance().getTissueBorder().lowerBoundInMikron(x));
			double distance = newloc.distance(lastloc);

			if((depthFrac(newloc.y) > biomechanicalModelGlobalParameters.getSeedMinDepth_frac() && (!biomechanicalModelGlobalParameters.getSeedReverse()))
					|| (depthFrac(newloc.y) < biomechanicalModelGlobalParameters.getSeedMinDepth_frac() && biomechanicalModelGlobalParameters.getSeedReverse()))
				if(distance > biomechanicalModelGlobalParameters.getBasalDensity_mikron()){
					
					UniversalCell stemCell = new UniversalCell(null, null, null);					
					((CenterBasedMechanicalModel) stemCell.getEpisimBioMechanicalModelObject()).getCellEllipseObject().setXY(((int)newloc.x), ((int)newloc.y));
					((CenterBasedMechanicalModel) stemCell.getEpisimBioMechanicalModelObject()).setCellLocationInCellField(newloc);
					standardCellEnsemble.add(stemCell);
					
					lastloc = newloc;
					
					GlobalStatistics.getInstance().inkrementActualNumberStemCells();
					GlobalStatistics.getInstance().inkrementActualNumberKCytes();
				}
		}
		return standardCellEnsemble;
   }

	//TODO: Initialisierungsmethode implementieren
   protected ArrayList<UniversalCell> buildInitialCellEnsemble(File file){	  
	   return new ArrayList<UniversalCell>();
   }

	
   protected void initializeCellEnsembleBasedOnRandomAgeDistribution(ArrayList<UniversalCell> cellEnsemble) {

	   //This method has to be implemented but has nothing to do in this model
	   
   }

	protected EpisimPortrayal getCellPortrayal() {
	
	   
	   return new UniversalCellPortrayal2D(java.awt.Color.lightGray){

			public Inspector getInspector(LocationWrapper wrapper, GUIState state) {
			// make the inspector
				return new CellInspector(super.getInspector(wrapper, state), wrapper, state);
			}
		};
   }
}
