package episimbiomechanics.centerbased.newversion;

import java.lang.reflect.Field;
import java.util.ArrayList;

import javax.vecmath.Point2d;

import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.UniversalCell;
import sim.app.episim.model.biomechanics.centerbased.newversion.CenterBasedMechanicalModel;
import sim.app.episim.model.biomechanics.centerbased.newversion.CenterBasedMechanicalModelGP;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import sim.app.episim.model.visualization.ContinuousUniversalCellPortrayal2D;
import sim.app.episim.model.visualization.UniversalCellPortrayal2D;
import sim.app.episim.persistence.SimulationStateData;
import sim.app.episim.tissue.TissueController;
import sim.util.Double2D;
import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import episiminterfaces.EpisimPortrayal;


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
		double depthPosition = ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().getBasalAmplitude_mikron()-y;
		
		return depthPosition < 0 ? 0: (depthPosition/ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().getBasalAmplitude_mikron());
	}

	protected ArrayList<UniversalCell> buildStandardInitialCellEnsemble() {
		double STEM_CELL_WIDTH=0;
		double STEM_CELL_HEIGHT=0;
		EpisimCellBehavioralModelGlobalParameters cbGP = ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters();		
		try{
	      Field field = cbGP.getClass().getDeclaredField("WIDTH_DEFAULT");
	      STEM_CELL_WIDTH = field.getDouble(cbGP);
	      
	      field = cbGP.getClass().getDeclaredField("HEIGHT_DEFAULT");
	      STEM_CELL_HEIGHT = field.getDouble(cbGP);   
      }
      catch (NoSuchFieldException e){
      	ExceptionDisplayer.getInstance().displayException(e);
      }
      catch (SecurityException e){
      	ExceptionDisplayer.getInstance().displayException(e);
      }
      catch (IllegalArgumentException e){
      	ExceptionDisplayer.getInstance().displayException(e);
      }
      catch (IllegalAccessException e){
      	ExceptionDisplayer.getInstance().displayException(e);
      }	
		

		ArrayList<UniversalCell> standardCellEnsemble = new ArrayList<UniversalCell>();

		CenterBasedMechanicalModelGP mechModelGP = (CenterBasedMechanicalModelGP) ModelController
				.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		
		Double2D lastloc = new Double2D(0, TissueController.getInstance().getTissueBorder().lowerBoundInMikron(0,0));
		boolean firstCell = true;
		for (double x = 0; x <= TissueController.getInstance().getTissueBorder().getWidthInMikron(); x += 1) {
			Double2D newloc = new Double2D(x, TissueController.getInstance().getTissueBorder().lowerBoundInMikron(x,0));
			double distance = newloc.distance(lastloc);

			if (depthFrac(newloc.y) > mechModelGP.getSeedMinDepth_frac() || mechModelGP.getSeedMinDepth_frac() == 0){
				if (distance > mechModelGP.getBasalDensity_mikron() || firstCell) {
				
					UniversalCell stemCell = new UniversalCell(null, null, true);
					CenterBasedMechanicalModel mechModel = ((CenterBasedMechanicalModel) stemCell.getEpisimBioMechanicalModelObject());
					Point2d corrPos =new Point2d(newloc.x, newloc.y);//mechModel.calculateLowerBoundaryPositionForCell(new Point2d(newloc.x, newloc.y));
					mechModel.setKeratinoWidth(STEM_CELL_WIDTH);
					mechModel.setKeratinoHeight(STEM_CELL_HEIGHT);	
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
			CenterBasedMechanicalModel centerBasedModel = (CenterBasedMechanicalModel) uCell.getEpisimBioMechanicalModelObject();
			centerBasedModel.getCellEllipseObject().setXY(centerBasedModel.getCellLocationInCellField().x, centerBasedModel.getCellLocationInCellField().y);
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
