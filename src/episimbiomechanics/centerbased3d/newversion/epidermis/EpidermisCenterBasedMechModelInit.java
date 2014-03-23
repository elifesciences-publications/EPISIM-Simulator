package episimbiomechanics.centerbased3d.newversion.epidermis;

import java.lang.reflect.Field;
import java.util.ArrayList;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;

import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.UniversalCell;
import sim.app.episim.model.biomechanics.centerbased3d.newversion.CenterBased3DMechanicalModel;
import sim.app.episim.model.biomechanics.centerbased3d.newversion.CenterBased3DMechanicalModelGP;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import sim.app.episim.model.visualization.ContinuousUniversalCellPortrayal2D;
import sim.app.episim.model.visualization.ContinuousUniversalCellPortrayal3D;
import sim.app.episim.model.visualization.UniversalCellPortrayal2D;
import sim.app.episim.persistence.SimulationStateData;
import sim.app.episim.tissue.TissueController;
import sim.util.Double2D;
import sim.util.Double3D;
import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import episiminterfaces.EpisimPortrayal;


public class EpidermisCenterBasedMechModelInit extends BiomechanicalModelInitializer {

	SimulationStateData simulationStateData = null;
	private static double STEM_CELL_WIDTH=0;
	private static double STEM_CELL_HEIGHT=0;
	private static double STEM_CELL_LENGTH=0;

	public EpidermisCenterBasedMechModelInit() {
		super();
		TissueController.getInstance().getTissueBorder().loadStandardMembrane();
	}

	public EpidermisCenterBasedMechModelInit(SimulationStateData simulationStateData) {
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

		CenterBased3DMechanicalModelGP mechModelGP = (CenterBased3DMechanicalModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		
		
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
	
	private boolean checkIfCellHasToBeAdded(CenterBased3DMechanicalModelGP mechModelGP, ArrayList<UniversalCell> standardCellEnsemble, double x, double z){
		Double3D newLoc = new Double3D(x, TissueController.getInstance().getTissueBorder().lowerBoundInMikron(x,0,z), z);
		boolean cellAdded = false;
		double requiredDistance = mechModelGP.getBasalDensity_mikron()/2d;
		if (depthFrac(newLoc.y) > mechModelGP.getSeedMinDepth_frac()  || mechModelGP.getSeedMinDepth_frac() == 0){					
			
			if(CenterBased3DMechanicalModel.getAllCellsWithinDistance(newLoc, requiredDistance).isEmpty()){				   
					cellAdded = true;		
					UniversalCell stemCell = new UniversalCell(null, null, true);
					CenterBased3DMechanicalModel mechModel=((CenterBased3DMechanicalModel) stemCell.getEpisimBioMechanicalModelObject());
					Point3d corrLoc = mechModel.calculateLowerBoundaryPositionForCell(new Point3d(newLoc.x, newLoc.y, newLoc.z));
					mechModel.setCellWidth(STEM_CELL_WIDTH);
					mechModel.setCellHeight(STEM_CELL_HEIGHT);
					mechModel.setCellLength(STEM_CELL_LENGTH);	
					mechModel.setCellLocationInCellField(new Double3D(corrLoc.x, corrLoc.y, corrLoc.z));
					standardCellEnsemble.add(stemCell);	
			}						
		}
		return cellAdded;
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