package episimbiomechanics.centerbased3d;

import java.util.ArrayList;

import javax.vecmath.Point3d;

import sim.app.episim.datamonitoring.GlobalStatistics;
import sim.app.episim.model.UniversalCell;
import sim.app.episim.model.biomechanics.centerbased3D.oldmodel.CenterBased3DModel;
import sim.app.episim.model.biomechanics.centerbased3D.oldmodel.CenterBased3DModelGP;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import sim.app.episim.persistence.SimulationStateData;
import sim.app.episim.tissue.TissueController;
import sim.app.episim.visualization.ContinuousUniversalCellPortrayal3D;
import sim.util.Double3D;
import episiminterfaces.EpisimPortrayal;


public class CenterBased3DMechModelInit extends BiomechanicalModelInitializer {

	SimulationStateData simulationStateData = null;

	public CenterBased3DMechModelInit() {
		super();
		TissueController.getInstance().getTissueBorder().loadStandardMembrane();
	}

	public CenterBased3DMechModelInit(SimulationStateData simulationStateData) {
		super(simulationStateData);
		this.simulationStateData = simulationStateData;
	}

	private final double depthFrac(double y)// depth of the position in the rete ridge in percent
	{
		double depthPosition = ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().getBasalAmplitude_mikron()-y;
		
		return depthPosition < 0 ? 0: (depthPosition/ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().getBasalAmplitude_mikron());
	}

	protected ArrayList<UniversalCell> buildStandardInitialCellEnsemble() {
		//return buildOneLayerStandardInitialCellEnsemble();
		return buildMultiLayerStandardInitialCellEnsemble();
		
	}
	
	private ArrayList<UniversalCell> buildMultiLayerStandardInitialCellEnsemble(){
		ArrayList<UniversalCell> standardCellEnsemble = new ArrayList<UniversalCell>();
		CenterBased3DModelGP mechModelGP = (CenterBased3DModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		double stopZ = TissueController.getInstance().getTissueBorder().getLengthInMikron();
		double startZ = mechModelGP.getBasalDensity_mikron()/2;
		final double increment = 0.1;
			
			
		
			boolean regularOrder = true;
			double cellCounter = 0;
			double oldCellCounter = 0;
			for(double z = startZ; z <= stopZ ; z+=increment){				
				if(cellCounter > oldCellCounter){
					regularOrder = !regularOrder;
					oldCellCounter = cellCounter;
				}				
				if(regularOrder){
					for (double x = 0; x <= TissueController.getInstance().getTissueBorder().getWidthInMikron(); x += increment){			
						boolean cellAdded =checkIfCellHasToBeAdded(mechModelGP, standardCellEnsemble, x, z);
						if(cellAdded)cellCounter++;
					}
				}
				else{
					for (double x = TissueController.getInstance().getTissueBorder().getWidthInMikron(); x >= 0; x -= increment){			
						boolean cellAdded =checkIfCellHasToBeAdded(mechModelGP, standardCellEnsemble, x, z);
						if(cellAdded)cellCounter++;
					}
				}			
			}
			
		
		return standardCellEnsemble;
	}
	
	private boolean checkIfCellHasToBeAdded(CenterBased3DModelGP mechModelGP, ArrayList<UniversalCell> standardCellEnsemble, double x, double z){
		Double3D newLoc = new Double3D(x, TissueController.getInstance().getTissueBorder().lowerBoundInMikron(x,0,z), z);
		boolean cellAdded = false;
		double requiredDistance = mechModelGP.getBasalDensity_mikron()/2d;
		if (depthFrac(newLoc.y) > mechModelGP.getSeedMinDepth_frac()  || mechModelGP.getSeedMinDepth_frac() == 0){					
			
			if(CenterBased3DModel.getAllCellsWithinDistance(newLoc, requiredDistance).isEmpty()){				   
					cellAdded = true;		
					UniversalCell stemCell = new UniversalCell(null, null, true);
					CenterBased3DModel mechModel=((CenterBased3DModel) stemCell.getEpisimBioMechanicalModelObject());
					Point3d corrLoc = mechModel.calculateLowerBoundaryPositionForCell(new Point3d(newLoc.x, newLoc.y, newLoc.z));
					((CenterBased3DModel) stemCell.getEpisimBioMechanicalModelObject()).setCellLocationInCellField(new Double3D(corrLoc.x, corrLoc.y, corrLoc.z));
					standardCellEnsemble.add(stemCell);	
			}						
		}
		return cellAdded;
	}
	
	
	private ArrayList<UniversalCell> buildOneLayerStandardInitialCellEnsemble(){
		ArrayList<UniversalCell> standardCellEnsemble = new ArrayList<UniversalCell>();
		CenterBased3DModelGP mechModelGP = (CenterBased3DModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		
		
		
		
		
		Double3D lastloc = new Double3D(0, TissueController.getInstance().getTissueBorder().lowerBoundInMikron(0,0,0), 0);			
		boolean firstCell = true;
			
			double startX = 0;
			
			for (double x = startX; x <= TissueController.getInstance().getTissueBorder().getWidthInMikron(); x += 1){
			
				Double3D newLoc = new Double3D(x, TissueController.getInstance().getTissueBorder().lowerBoundInMikron(x,0,0), 0);
				double distance = newLoc.distance(lastloc);
	
				if (depthFrac(newLoc.y) > mechModelGP.getSeedMinDepth_frac()){
					if (distance >= (mechModelGP.getBasalDensity_mikron()) || firstCell) {
						
						lastloc = newLoc;						
						
						firstCell=false;
					
							
							newLoc = new Double3D(x, TissueController.getInstance().getTissueBorder().lowerBoundInMikron(x,0,2), 0);
							
							UniversalCell stemCell = new UniversalCell(null, null, true);
							CenterBased3DModel mechModel=((CenterBased3DModel) stemCell.getEpisimBioMechanicalModelObject());
							Point3d corrLoc = mechModel.calculateLowerBoundaryPositionForCell(new Point3d(newLoc.x, newLoc.y, newLoc.z));
							((CenterBased3DModel) stemCell.getEpisimBioMechanicalModelObject()).setCellLocationInCellField(new Double3D(corrLoc.x, corrLoc.y, corrLoc.z));
							standardCellEnsemble.add(stemCell);							
											
					}
				}
			}
		
		return standardCellEnsemble;
	}
	

	protected ArrayList<UniversalCell> buildInitialCellEnsemble() {
		ArrayList<UniversalCell> loadedCells = super.buildInitialCellEnsemble();	
		return loadedCells;
	}

	
	protected void initializeCellEnsembleBasedOnRandomAgeDistribution(ArrayList<UniversalCell> cellEnsemble) {

		// This method has to be implemented but has nothing to do in this model

	}

	protected EpisimPortrayal getCellPortrayal() {		
		ContinuousUniversalCellPortrayal3D continuousPortrayal = new ContinuousUniversalCellPortrayal3D("Epidermis");
		continuousPortrayal.setField(ModelController.getInstance().getBioMechanicalModelController().getCellField());
		return continuousPortrayal;
	}

	protected EpisimPortrayal[] getAdditionalPortrayalsCellForeground() {
		return new EpisimPortrayal[0];
	}

	protected EpisimPortrayal[] getAdditionalPortrayalsCellBackground() {
		return new EpisimPortrayal[0];
	}
}
