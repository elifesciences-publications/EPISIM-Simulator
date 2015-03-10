package episimmcc.centerbased2d.newmodel.psoriasis;

import java.lang.reflect.Field;
import java.util.ArrayList;

import javax.vecmath.Point2d;

import sim.app.episim.EpisimExceptionHandler;
import sim.app.episim.model.UniversalCell;
import sim.app.episim.model.biomechanics.centerbased2d.newmodel.CenterBased2DModel;
import sim.app.episim.model.biomechanics.centerbased2d.newmodel.CenterBased2DModelGP;
import sim.app.episim.model.biomechanics.centerbased2d.newmodel.psoriasis.PsoriasisCenterBased2DModelGP;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.controller.TissueController;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters;
import sim.app.episim.persistence.SimulationStateData;
import sim.app.episim.visualization.twodim.ContinousCellPortrayal2D;
import sim.app.episim.visualization.twodim.ContinuousCellFieldPortrayal2D;
import sim.util.Double2D;
import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import episiminterfaces.EpisimPortrayal;


public class CenterBasedMechModelInit extends BiomechanicalModelInitializer {

	SimulationStateData simulationStateData = null;

	public CenterBasedMechModelInit() {
		super();
		TissueController.getInstance().getTissueBorder().loadStandardMembrane();
		PsoriasisCenterBased2DModelGP mechModelGP = (PsoriasisCenterBased2DModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		mechModelGP.setHeightInMikron(325);
	}

	public CenterBasedMechModelInit(SimulationStateData simulationStateData) {
		super(simulationStateData);
		this.simulationStateData = simulationStateData;		
	}

	private final double depthFrac(double y, double stemCellHeight)// depth of the position in the rete ridge in percent
	{
		PsoriasisCenterBased2DModelGP mechModelGP = (PsoriasisCenterBased2DModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		double yShift = mechModelGP.getBasalYDelta_mikron()-(stemCellHeight/2d);
		
		yShift = yShift < 0 ? 0 : yShift;
		
		double amplitude = mechModelGP.getBasalAmplitude_mikron();
		double depthPosition = amplitude-(y-yShift);
		
		return depthPosition < 0 ? 0: (depthPosition/amplitude);
	}

	protected ArrayList<UniversalCell> buildStandardInitialCellEnsemble() {
		double STEM_CELL_WIDTH=0;
		double STEM_CELL_HEIGHT=0;
		double STEM_CELL_LENGTH=0;
		
		EpisimCellBehavioralModelGlobalParameters cbGP = ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters();		
		try{
	      Field field = cbGP.getClass().getDeclaredField("WIDTH_DEFAULT");
	      STEM_CELL_WIDTH = field.getDouble(cbGP);
	      
	      field = cbGP.getClass().getDeclaredField("HEIGHT_DEFAULT");
	      STEM_CELL_HEIGHT = field.getDouble(cbGP);
	      
	      field = cbGP.getClass().getDeclaredField("LENGTH_DEFAULT");
	      STEM_CELL_LENGTH = field.getDouble(cbGP);
      }
      catch (NoSuchFieldException e){
      	EpisimExceptionHandler.getInstance().displayException(e);
      }
      catch (SecurityException e){
      	EpisimExceptionHandler.getInstance().displayException(e);
      }
      catch (IllegalArgumentException e){
      	EpisimExceptionHandler.getInstance().displayException(e);
      }
      catch (IllegalAccessException e){
      	EpisimExceptionHandler.getInstance().displayException(e);
      }	
		initGlobalParameters(STEM_CELL_HEIGHT);

		ArrayList<UniversalCell> standardCellEnsemble = new ArrayList<UniversalCell>();

		PsoriasisCenterBased2DModelGP mechModelGP = (PsoriasisCenterBased2DModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		
		Double2D lastloc = new Double2D(0, TissueController.getInstance().getTissueBorder().lowerBoundInMikron(0,0));
		boolean firstCell = true;
		for (double x = 0; x <= TissueController.getInstance().getTissueBorder().getWidthInMikron(); x += 1) {
			Double2D newloc = new Double2D(x, TissueController.getInstance().getTissueBorder().lowerBoundInMikron(x,0));
			double distance = newloc.distance(lastloc);

			if (depthFrac(newloc.y, STEM_CELL_HEIGHT) > mechModelGP.getSeedMinDepth_frac() || mechModelGP.getSeedMinDepth_frac() == 0){
				if (distance > mechModelGP.getBasalDensity_mikron() || firstCell) {
				
					UniversalCell stemCell = new UniversalCell(null, null, true);
					CenterBased2DModel mechModel = ((CenterBased2DModel) stemCell.getEpisimBioMechanicalModelObject());
					Point2d corrPos =new Point2d(newloc.x, newloc.y);//mechModel.calculateLowerBoundaryPositionForCell(new Point2d(newloc.x, newloc.y));
					mechModel.setCellWidth(STEM_CELL_WIDTH);
					mechModel.setCellHeight(STEM_CELL_HEIGHT);
					mechModel.setCellLength(STEM_CELL_LENGTH);
					mechModel.setStandardCellWidth(STEM_CELL_WIDTH);
					mechModel.setStandardCellHeight(STEM_CELL_HEIGHT);
					mechModel.getCellEllipseObject().setXY(corrPos.x, corrPos.y);
					mechModel.getCellEllipseObject().setMajorAxisAndMinorAxis(STEM_CELL_WIDTH, STEM_CELL_HEIGHT);
					mechModel.setCellLocationInCellField(new Double2D(corrPos.x, corrPos.y));
					standardCellEnsemble.add(stemCell);

					lastloc = newloc;
					firstCell=false;
				}
			}
		}
		return standardCellEnsemble;
	}
	
	private void initGlobalParameters(double stemCellHeight){
		PsoriasisCenterBased2DModelGP mechModelGP = (PsoriasisCenterBased2DModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		mechModelGP.setHeightInMikron(325);
		mechModelGP.setBasalAmplitude_mikron(mechModelGP.getInitBasalAmplitude_mikron());
		mechModelGP.setBasalYDelta_mikron(mechModelGP.getMaxBasalAmplitude_mikron()-mechModelGP.getBasalAmplitude_mikron() +(stemCellHeight/2));
		
	}

	protected ArrayList<UniversalCell> buildInitialCellEnsemble() {
		ArrayList<UniversalCell> loadedCells = super.buildInitialCellEnsemble();

		for (UniversalCell uCell : loadedCells) {				
			CenterBased2DModel centerBasedModel = (CenterBased2DModel) uCell.getEpisimBioMechanicalModelObject();
			centerBasedModel.getCellEllipseObject().setXY(centerBasedModel.getX(), centerBasedModel.getY());
			centerBasedModel.getCellEllipseObject().setMajorAxisAndMinorAxis(centerBasedModel.getCellWidth(), centerBasedModel.getCellHeight());
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
