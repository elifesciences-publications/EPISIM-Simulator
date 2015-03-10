package episimmcc.centerbased2d.oldmodel;

import java.io.File;
import java.util.ArrayList;

import javax.vecmath.Point2d;

import org.w3c.dom.DOMException;

import episiminterfaces.EpisimPortrayal;
import sim.app.episim.datamonitoring.GlobalStatistics;
import sim.app.episim.model.AbstractCell;
import sim.app.episim.model.UniversalCell;
import sim.app.episim.model.biomechanics.centerbased2d.oldmodel.CenterBased2DModel;
import sim.app.episim.model.biomechanics.centerbased2d.oldmodel.CenterBased2DModelGP;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.controller.TissueController;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import sim.app.episim.persistence.SimulationStateData;
import sim.app.episim.persistence.dataconvert.XmlEpisimBiomechanicalModel;
import sim.app.episim.persistence.dataconvert.XmlUniversalCell;
import sim.app.episim.util.CellInspector;
import sim.app.episim.visualization.twodim.ContinousCellPortrayal2D;
import sim.app.episim.visualization.twodim.ContinuousCellFieldPortrayal2D;
import sim.app.episim.gui.EpisimGUIState;
import sim.display.GUIState;
import sim.portrayal.Inspector;
import sim.portrayal.LocationWrapper;
import sim.portrayal.Portrayal;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.util.Double2D;

public class CenterBasedMechModelInit extends BiomechanicalModelInitializer {

	SimulationStateData simulationStateData = null;

	public CenterBasedMechModelInit() {
		super();
		TissueController.getInstance().getTissueBorder().loadStandardMembrane();
	}

	public CenterBasedMechModelInit(SimulationStateData simulationStateData) {
		super(simulationStateData);
		this.simulationStateData = simulationStateData;
	}

	private final double depthFrac(double y)// depth of the position in the rete ridge in percent
	{
		
		CenterBased2DModelGP mechModelGP = (CenterBased2DModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		
		double depthPosition = mechModelGP.getBasalAmplitude_mikron()-y;
		
		return depthPosition < 0 ? 0: (depthPosition/mechModelGP.getBasalAmplitude_mikron());
	}

	protected ArrayList<UniversalCell> buildStandardInitialCellEnsemble() {

		ArrayList<UniversalCell> standardCellEnsemble = new ArrayList<UniversalCell>();

		CenterBased2DModelGP mechModelGP = (CenterBased2DModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		
		Double2D lastloc = new Double2D(0, TissueController.getInstance().getTissueBorder().lowerBoundInMikron(0,0));
		boolean firstCell = true;
		for (double x = 0; x <= TissueController.getInstance().getTissueBorder().getWidthInMikron(); x += 1) {
			Double2D newloc = new Double2D(x, TissueController.getInstance().getTissueBorder().lowerBoundInMikron(x,0));
			double distance = newloc.distance(lastloc);

			if (depthFrac(newloc.y) > mechModelGP.getSeedMinDepth_frac() || mechModelGP.getSeedMinDepth_frac() == 0){
				if (distance > mechModelGP.getBasalDensity_mikron() || firstCell) {
				
					UniversalCell stemCell = new UniversalCell(null, null, true);
					CenterBased2DModel mechModel = ((CenterBased2DModel) stemCell.getEpisimBioMechanicalModelObject());
					Point2d corrPos =new Point2d(newloc.x, newloc.y);//mechModel.calculateLowerBoundaryPositionForCell(new Point2d(newloc.x, newloc.y));
					
					mechModel.getCellEllipseObject().setXY(corrPos.x, corrPos.y);
					mechModel.setCellLocationInCellField(new Double2D(corrPos.x, corrPos.y));
					standardCellEnsemble.add(stemCell);

					lastloc = newloc;
					firstCell=false;
				}
			}
		}
		return standardCellEnsemble;
	}

	protected ArrayList<UniversalCell> buildInitialCellEnsemble() {
		ArrayList<UniversalCell> loadedCells = super.buildInitialCellEnsemble();

		for (UniversalCell uCell : loadedCells) {				
			CenterBased2DModel centerBasedModel = (CenterBased2DModel) uCell.getEpisimBioMechanicalModelObject();
			centerBasedModel.getCellEllipseObject().setXY(centerBasedModel.getCellLocationInCellField().x, centerBasedModel.getCellLocationInCellField().y);
		}
		return loadedCells;
	}

	
	protected void initializeCellEnsembleBasedOnRandomAgeDistribution(ArrayList<UniversalCell> cellEnsemble) {

		// This method has to be implemented but has nothing to do in this model

	}

	protected EpisimPortrayal getCellPortrayal() {
		ContinousCellPortrayal2D cellPortrayal = new ContinousCellPortrayal2D(java.awt.Color.lightGray);
		ContinuousCellFieldPortrayal2D continousPortrayal = new ContinuousCellFieldPortrayal2D();
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
