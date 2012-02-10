package sim.app.episim.model.initialization;

import java.util.ArrayList;

import javax.vecmath.Point3d;

import sim.app.episim.UniversalCell;
import sim.app.episim.datamonitoring.GlobalStatistics;
import sim.app.episim.model.biomechanics.centerbased3d.CenterBased3DMechanicalModel;

import sim.app.episim.model.biomechanics.centerbased3d.CenterBased3DMechanicalModelGP;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.visualization.ContinuousUniversalCellPortrayal3D;
import sim.app.episim.persistence.SimulationStateData;
import sim.app.episim.tissue.TissueController;
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
		return buildOneLayerStandardInitialCellEnsemble();
		//return buildMultiLayerStandardInitialCellEnsemble();
		
	}
	
	private ArrayList<UniversalCell> buildMultiLayerStandardInitialCellEnsemble(){
		ArrayList<UniversalCell> standardCellEnsemble = new ArrayList<UniversalCell>();
		CenterBased3DMechanicalModelGP mechModelGP = (CenterBased3DMechanicalModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		
		
		double stopZ = TissueController.getInstance().getTissueBorder().getLengthInMikron()-(mechModelGP.getBasalDensity_mikron()/2);
		double incrementZ = mechModelGP.getBasalDensity_mikron();
		
		int stemCellRowCounter = 0;
		Double3D lastloc = new Double3D(0, TissueController.getInstance().getTissueBorder().lowerBoundInMikron(0,0,0), 0);			
		boolean firstCell = true;
			
			double startX = 0;
			
			for (double x = startX; x <= TissueController.getInstance().getTissueBorder().getWidthInMikron(); x += 1){
			
				Double3D newLoc = new Double3D(x, TissueController.getInstance().getTissueBorder().lowerBoundInMikron(x,0,0), 0);
				double distance = newLoc.distance(lastloc);
	
				if (depthFrac(newLoc.y) > mechModelGP.getSeedMinDepth_frac()){
					if (distance >= (mechModelGP.getBasalDensity_mikron()/2) || firstCell) {
						double z = (stemCellRowCounter%2)==0 ? (mechModelGP.getBasalDensity_mikron()/2) : (mechModelGP.getBasalDensity_mikron());
						lastloc = newLoc;						
						stemCellRowCounter++;
						firstCell=false;
						for(; z <= stopZ; z+=incrementZ){
							
							newLoc = new Double3D(x, TissueController.getInstance().getTissueBorder().lowerBoundInMikron(x,0,z), z);
							
							UniversalCell stemCell = new UniversalCell(null, null);
							CenterBased3DMechanicalModel mechModel=((CenterBased3DMechanicalModel) stemCell.getEpisimBioMechanicalModelObject());
							Point3d corrLoc = mechModel.calculateLowerBoundaryPositionForCell(new Point3d(newLoc.x, newLoc.y, newLoc.z));
							((CenterBased3DMechanicalModel) stemCell.getEpisimBioMechanicalModelObject()).setCellLocationInCellField(new Double3D(corrLoc.x, corrLoc.y, corrLoc.z));
							standardCellEnsemble.add(stemCell);							
							
							GlobalStatistics.getInstance().inkrementActualNumberStemCells();
							GlobalStatistics.getInstance().inkrementActualNumberKCytes();
						}
						
					}
				}
			}
		
		return standardCellEnsemble;
	}
	
	private ArrayList<UniversalCell> buildOneLayerStandardInitialCellEnsemble(){
		ArrayList<UniversalCell> standardCellEnsemble = new ArrayList<UniversalCell>();
		CenterBased3DMechanicalModelGP mechModelGP = (CenterBased3DMechanicalModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		
		
		
		
		
		Double3D lastloc = new Double3D(0, TissueController.getInstance().getTissueBorder().lowerBoundInMikron(0,0,0), 2);			
		boolean firstCell = true;
			
			double startX = 0;
			
			for (double x = startX; x <= TissueController.getInstance().getTissueBorder().getWidthInMikron(); x += 1){
			
				Double3D newLoc = new Double3D(x, TissueController.getInstance().getTissueBorder().lowerBoundInMikron(x,0,0), 0);
				double distance = newLoc.distance(lastloc);
	
				if (depthFrac(newLoc.y) > mechModelGP.getSeedMinDepth_frac()){
					if (distance >= (mechModelGP.getBasalDensity_mikron()) || firstCell) {
						
						lastloc = newLoc;						
						
						firstCell=false;
					
							
							newLoc = new Double3D(x, TissueController.getInstance().getTissueBorder().lowerBoundInMikron(x,0,2), 2);
							
							UniversalCell stemCell = new UniversalCell(null, null);
							CenterBased3DMechanicalModel mechModel=((CenterBased3DMechanicalModel) stemCell.getEpisimBioMechanicalModelObject());
							Point3d corrLoc = mechModel.calculateLowerBoundaryPositionForCell(new Point3d(newLoc.x, newLoc.y, newLoc.z));
							((CenterBased3DMechanicalModel) stemCell.getEpisimBioMechanicalModelObject()).setCellLocationInCellField(new Double3D(corrLoc.x, corrLoc.y, corrLoc.z));
							standardCellEnsemble.add(stemCell);							
							
							GlobalStatistics.getInstance().inkrementActualNumberStemCells();
							GlobalStatistics.getInstance().inkrementActualNumberKCytes();						
						
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
		ContinuousUniversalCellPortrayal3D continuousPortrayal = new ContinuousUniversalCellPortrayal3D();
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
