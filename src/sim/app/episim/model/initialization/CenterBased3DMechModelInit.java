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

	private final double depthFrac(double y) // wie tief ist in prozent die
												// uebergebene y-position
												// relativ zu rete tiefe
	{
		return (y + TissueController.getInstance().getTissueBorder().getHeightInMikron() - TissueController.getInstance().getTissueBorder().getUndulationBaseLine())
				/ ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().getBasalAmplitude_mikron();
	}

	protected ArrayList<UniversalCell> buildStandardInitialCellEnsemble() {

		ArrayList<UniversalCell> standardCellEnsemble = new ArrayList<UniversalCell>();

		CenterBased3DMechanicalModelGP mechModelGP = (CenterBased3DMechanicalModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();

		Double3D lastloc = new Double3D(2, TissueController.getInstance().getTissueBorder().lowerBoundInMikron(2,0,0), 10);
		
		for (double x = 2; x <= TissueController.getInstance().getTissueBorder().getWidthInPixels(); x += 2) {
			Double3D newLoc = new Double3D(x, TissueController.getInstance().getTissueBorder().lowerBoundInMikron(x,0), 10);
			double distance = newLoc.distance(lastloc);

			if ((depthFrac(newLoc.y) > mechModelGP.getSeedMinDepth_frac())
					|| (depthFrac(newLoc.y) < mechModelGP.getSeedMinDepth_frac()))
				if (distance > mechModelGP.getBasalDensity_mikron()) {				
					UniversalCell stemCell = new UniversalCell(null, null);
					CenterBased3DMechanicalModel mechModel=((CenterBased3DMechanicalModel) stemCell.getEpisimBioMechanicalModelObject());
					Point3d corrLoc = mechModel.calculateLowerBoundaryPositionForCell(new Point3d(newLoc.x, newLoc.y, newLoc.z));
					((CenterBased3DMechanicalModel) stemCell.getEpisimBioMechanicalModelObject()).setCellLocationInCellField(new Double3D(corrLoc.x, corrLoc.y, corrLoc.z));
					standardCellEnsemble.add(stemCell);

					lastloc = newLoc;

					GlobalStatistics.getInstance().inkrementActualNumberStemCells();
					GlobalStatistics.getInstance().inkrementActualNumberKCytes();
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
