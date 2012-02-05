package sim.app.episim.model.initialization;

import java.util.ArrayList;

import sim.app.episim.UniversalCell;
import sim.app.episim.datamonitoring.GlobalStatistics;
import sim.app.episim.model.biomechanics.centerbased.CenterBasedMechanicalModel;
import sim.app.episim.model.biomechanics.centerbased.CenterBasedMechanicalModelGP;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.visualization.ContinuousUniversalCellPortrayal2D;
import sim.app.episim.model.visualization.UniversalCellPortrayal2D;
import sim.app.episim.persistence.SimulationStateData;
import sim.app.episim.tissue.TissueController;
import sim.util.Double2D;
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

		CenterBasedMechanicalModelGP mechModelGP = (CenterBasedMechanicalModelGP) ModelController
				.getInstance().getEpisimBioMechanicalModelGlobalParameters();

		Double2D lastloc = new Double2D(2, TissueController.getInstance().getTissueBorder().lowerBoundInMikron(2,0));
		
		for (double x = 2; x <= TissueController.getInstance().getTissueBorder().getWidthInPixels(); x += 2) {
			Double2D newloc = new Double2D(x, TissueController.getInstance().getTissueBorder().lowerBoundInMikron(x,0));
			double distance = newloc.distance(lastloc);

			if ((depthFrac(newloc.y) > mechModelGP.getSeedMinDepth_frac())
					|| (depthFrac(newloc.y) < mechModelGP.getSeedMinDepth_frac()))
				if (distance > mechModelGP.getBasalDensity_mikron()) {
				
					UniversalCell stemCell = new UniversalCell(null, null);
					((CenterBasedMechanicalModel) stemCell.getEpisimBioMechanicalModelObject()).getCellEllipseObject().setXY(
							((int) newloc.x), ((int)  (newloc.y)));
					((CenterBasedMechanicalModel) stemCell.getEpisimBioMechanicalModelObject())
							.setCellLocationInCellField(newloc);
					standardCellEnsemble.add(stemCell);

					lastloc = newloc;

					GlobalStatistics.getInstance().inkrementActualNumberStemCells();
					GlobalStatistics.getInstance().inkrementActualNumberKCytes();
				}
		}
		return standardCellEnsemble;
	}

	protected ArrayList<UniversalCell> buildInitialCellEnsemble() {
		ArrayList<UniversalCell> loadedCells = super.buildInitialCellEnsemble();

		for (UniversalCell uCell : loadedCells) {				
			CenterBasedMechanicalModel centerBasedModel = (CenterBasedMechanicalModel) uCell.getEpisimBioMechanicalModelObject();
			centerBasedModel.getCellEllipseObject().setXY((int) centerBasedModel.getCellLocationInCellField().x, (int) centerBasedModel.getCellLocationInCellField().y);
		}
		return loadedCells;
	}

	
	protected void initializeCellEnsembleBasedOnRandomAgeDistribution(ArrayList<UniversalCell> cellEnsemble) {

		// This method has to be implemented but has nothing to do in this model

	}

	protected EpisimPortrayal getCellPortrayal() {
		UniversalCellPortrayal2D cellPortrayal = new UniversalCellPortrayal2D(java.awt.Color.lightGray);
		ContinuousUniversalCellPortrayal2D continousPortrayal = new ContinuousUniversalCellPortrayal2D();
		continousPortrayal.setPortrayalForClass(UniversalCell.class, cellPortrayal);
		continousPortrayal.setField(ModelController.getInstance().getBioMechanicalModelController().getCellField());
		return continousPortrayal;
	}

	protected EpisimPortrayal[] getAdditionalPortrayalsCellForeground() {
		return new EpisimPortrayal[0];
	}

	protected EpisimPortrayal[] getAdditionalPortrayalsCellBackground() {
		return new EpisimPortrayal[0];
	}
}
