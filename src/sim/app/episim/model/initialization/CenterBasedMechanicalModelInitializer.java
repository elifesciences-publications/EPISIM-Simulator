package sim.app.episim.model.initialization;

import java.io.File;
import java.util.ArrayList;

import sim.app.episim.AbstractCell;
import sim.app.episim.UniversalCell;
import sim.app.episim.datamonitoring.GlobalStatistics;
import sim.app.episim.model.biomechanics.centerbased.CenterBasedMechanicalModelGlobalParameters;
import sim.app.episim.model.biomechanics.centerbased.CenterBasedMechanicalModel;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.tissue.TissueController;
import sim.util.Double2D;


public class CenterBasedMechanicalModelInitializer extends BiomechanicalModelInitializer {
	
	
	public CenterBasedMechanicalModelInitializer(){
		super();
	}
	
	public CenterBasedMechanicalModelInitializer(File modelInitializationFile){
		super(modelInitializationFile);
	}	

	private final double depthFrac(double y) // wie tief ist in prozent die uebergebene y-position relativ zu rete tiefe
	{
	     return (y-TissueController.getInstance().getTissueBorder().getUndulationBaseLine())/ModelController.getInstance().getBioMechanicalModelController().getEpisimBioMechanicalModelGlobalParameters().getBasalAmplitude_µm();                
	}	
	
   protected ArrayList<UniversalCell> buildStandardInitialCellEnsemble() {
   	
   	ArrayList<UniversalCell> standardCellEnsemble = new ArrayList<UniversalCell>();
   	
   	CenterBasedMechanicalModelGlobalParameters biomechanicalModelGlobalParameters = (CenterBasedMechanicalModelGlobalParameters) ModelController.getInstance().getBioMechanicalModelController().getEpisimBioMechanicalModelGlobalParameters();   	
   	
   	Double2D lastloc = new Double2D(2, TissueController.getInstance().getTissueBorder().lowerBound(2));
		for(double x = 2; x <= TissueController.getInstance().getTissueBorder().getWidth(); x += 2){
			Double2D newloc = new Double2D(x, TissueController.getInstance().getTissueBorder().lowerBound(x));
			double distance = newloc.distance(lastloc);

			if((depthFrac(newloc.y) > biomechanicalModelGlobalParameters.getSeedMinDepth_frac() && (!biomechanicalModelGlobalParameters.getSeedReverse()))
					|| (depthFrac(newloc.y) < biomechanicalModelGlobalParameters.getSeedMinDepth_frac() && biomechanicalModelGlobalParameters.getSeedReverse()))
				if(distance > biomechanicalModelGlobalParameters.getBasalDensity_µm()){
					
					UniversalCell stemCell = new UniversalCell(AbstractCell.getNextCellId(),-1, null);					
					((CenterBasedMechanicalModel) stemCell.getEpisimBioMechanicalModelObject()).getCellEllipseObject().setXY(((int)newloc.x), ((int)newloc.y));
					TissueController.getInstance().getActEpidermalTissue().getCellContinous2D().setObjectLocation(stemCell, newloc);
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

	
	

}
