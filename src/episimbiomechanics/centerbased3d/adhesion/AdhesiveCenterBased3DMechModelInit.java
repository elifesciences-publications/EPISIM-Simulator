package episimbiomechanics.centerbased3d.adhesion;

import java.lang.reflect.Field;
import java.util.ArrayList;

import javax.vecmath.Point2d;

import sim.app.episim.EpisimExceptionHandler;
import sim.app.episim.model.UniversalCell;
import sim.app.episim.model.biomechanics.centerbased3Dr.oldmodel.wound.AdhesiveCenterBased3DModel;
import sim.app.episim.model.biomechanics.centerbased3Dr.oldmodel.wound.AdhesiveCenterBased3DModelGP;
import sim.app.episim.model.biomechanics.latticebased2D.LatticeBased2DModelGP;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters;
import sim.app.episim.persistence.SimulationStateData;
import sim.app.episim.tissue.StandardMembrane;
import sim.app.episim.tissue.TissueController;
import sim.app.episim.visualization.ContinuousUniversalCellPortrayal2D;
import sim.app.episim.visualization.ContinuousUniversalCellPortrayal3D;
import sim.app.episim.visualization.UniversalCellPortrayal2D;
import sim.util.Double2D;
import sim.util.Double3D;
import episimbiomechanics.centerbased.CenterBasedMechModelInit;
import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import episiminterfaces.EpisimCellType;
import episiminterfaces.EpisimDifferentiationLevel;
import episiminterfaces.EpisimPortrayal;


public class AdhesiveCenterBased3DMechModelInit extends BiomechanicalModelInitializer {

	SimulationStateData simulationStateData = null;

	public AdhesiveCenterBased3DMechModelInit() {
		super();		
		AdhesiveCenterBased3DModelGP globalParameters = (AdhesiveCenterBased3DModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		TissueController.getInstance().getTissueBorder().loadStandardMembrane(globalParameters.getBasalMembraneDiscrSteps(), globalParameters.getBasalMembraneDiscrSteps(), globalParameters.getBasalMembraneContactTimeThreshold());
		setInitialGlobalParametersValues(globalParameters);
	}

	public AdhesiveCenterBased3DMechModelInit(SimulationStateData simulationStateData) {
		super(simulationStateData);
		this.simulationStateData = simulationStateData;
	}

	
	protected ArrayList<UniversalCell> buildStandardInitialCellEnsemble() {
		double BASAL_CELL_WIDTH=0;
		double BASAL_CELL_HEIGHT=0;
		double BASAL_CELL_LENGTH=0;
		double SUPRABASAL_CELL_WIDTH=0;
		double SUPRABASAL_CELL_HEIGHT=0;
		double SUPRABASAL_CELL_LENGTH=0;
		
		AdhesiveCenterBased3DModelGP globalParameters = (AdhesiveCenterBased3DModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		TissueController.getInstance().getTissueBorder().loadStandardMembrane(globalParameters.getBasalMembraneDiscrSteps(), globalParameters.getBasalMembraneContactTimeThreshold());
		
		EpisimCellBehavioralModelGlobalParameters cbGP = ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters();
		AdhesiveCenterBased3DModelGP mechModelGP = (AdhesiveCenterBased3DModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		
		try{
	      Field field = cbGP.getClass().getDeclaredField("BASAL_CELL_WIDTH");
	      BASAL_CELL_WIDTH = field.getDouble(cbGP);
	      
	      field = cbGP.getClass().getDeclaredField("BASAL_CELL_HEIGHT");
	      BASAL_CELL_HEIGHT = field.getDouble(cbGP);
	      
	      field = cbGP.getClass().getDeclaredField("BASAL_CELL_LENGTH");
	      BASAL_CELL_LENGTH = field.getDouble(cbGP);
	      
	      field = cbGP.getClass().getDeclaredField("SUPRABASAL_CELL_WIDTH");
	      SUPRABASAL_CELL_WIDTH = field.getDouble(cbGP);
	      
	      field = cbGP.getClass().getDeclaredField("SUPRABASAL_CELL_HEIGHT");
	      SUPRABASAL_CELL_HEIGHT = field.getDouble(cbGP);
	      
	      field = cbGP.getClass().getDeclaredField("SUPRABASAL_CELL_LENGTH");
	      SUPRABASAL_CELL_LENGTH = field.getDouble(cbGP);
	      
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

		ArrayList<UniversalCell> standardCellEnsemble = new ArrayList<UniversalCell>();

		EpisimCellType[] cellTypes = ModelController.getInstance().getCellBehavioralModelController().getAvailableCellTypes();
		EpisimDifferentiationLevel[] diffLevels = ModelController.getInstance().getCellBehavioralModelController().getAvailableDifferentiationLevels();
		
		double circleRadius = ((mechModelGP.getWidthInMikron()-BASAL_CELL_WIDTH)/2d);
		Double2D circleCenter = new Double2D(mechModelGP.getWidthInMikron()/2d, mechModelGP.getLengthInMikron()/2d);
		
	
		
		//seed basal layer
		boolean firstCell = true;
		double yZeroLine = TissueController.getInstance().getTissueBorder().lowerBoundInMikron(0,0);
		
		for(double actCircleRadius = circleRadius; (circleRadius-actCircleRadius)<globalParameters.getInitCellCoveredDistInMikron();actCircleRadius-=BASAL_CELL_WIDTH){
			double angleIncrement = 2*Math.asin((BASAL_CELL_WIDTH)/(2*actCircleRadius));
			double angleIncremtentDegrees = Math.toDegrees(angleIncrement);
			/*while((360%angleIncremtentDegrees)!=0 && angleIncremtentDegrees<360){
				angleIncremtentDegrees++;
			}		*/	
			for (double alpha= 0; alpha < 360; alpha += angleIncremtentDegrees){
				
					Double3D newloc = new Double3D((circleCenter.x+ (actCircleRadius*Math.cos(Math.toRadians(alpha)))), yZeroLine+ (BASAL_CELL_HEIGHT/2), (circleCenter.y+ (actCircleRadius*Math.sin(Math.toRadians(alpha)))));
					UniversalCell cell = new UniversalCell(null, null, true);					
					
					AdhesiveCenterBased3DModel mechModel = ((AdhesiveCenterBased3DModel) cell.getEpisimBioMechanicalModelObject());					
					mechModel.setKeratinoWidth(BASAL_CELL_WIDTH);
					mechModel.setKeratinoHeight(BASAL_CELL_HEIGHT);
					mechModel.setKeratinoLength(BASAL_CELL_LENGTH);
					
					mechModel.setStandardCellWidth(BASAL_CELL_WIDTH);
					mechModel.setStandardCellHeight(BASAL_CELL_HEIGHT);
					mechModel.setStandardCellLength(BASAL_CELL_LENGTH);
					
					mechModel.setCellLocationInCellField(newloc);
					standardCellEnsemble.add(cell);					
					cell.getEpisimCellBehavioralModelObject().setCellType(cellTypes[0]);					
					
					if(firstCell){
						cell.getEpisimCellBehavioralModelObject().setDiffLevel(diffLevels[0]);
						
					}
					else{
						if(diffLevels.length>1){
							cell.getEpisimCellBehavioralModelObject().setDiffLevel(diffLevels[1]);							
						}
					}
					
			}
			firstCell=false;
		}
		
		//set basal contact time to threshold
		StandardMembrane membrane = TissueController.getInstance().getTissueBorder().getStandardMembrane();
		if(membrane != null && membrane.isDiscretizedMembrane()){
			for(double actCircleRadius = circleRadius; (circleRadius-actCircleRadius)<globalParameters.getInitCellCoveredDistInMikron();actCircleRadius-=1){
				double angleIncrement = 2*Math.asin(1/(2*actCircleRadius));
				double angleIncremtentDegrees = Math.toDegrees(angleIncrement);
			/*	while((360%angleIncremtentDegrees)!=0 && angleIncremtentDegrees<360){
					angleIncremtentDegrees++;
				}	*/		
				for (double alpha= 0; alpha < 360; alpha += angleIncremtentDegrees){
					Double3D newloc = new Double3D((circleCenter.x+ (actCircleRadius*Math.cos(Math.toRadians(alpha)))), 0, (circleCenter.y+ (actCircleRadius*Math.sin(Math.toRadians(alpha)))));
					membrane.setContactTimeForReferenceCoordinate3D(new Double3D(newloc.x, membrane.lowerBoundInMikron(newloc.x, newloc.z),newloc.z), mechModelGP.getBasalMembraneContactTimeThreshold());
				}
			}
		}
		
		//seed suprabasal layers 
		boolean firstSuprabasalLayer = true;
		for (double y = 0; y < 3 ; y++) {
			firstCell = true;
			for(double actCircleRadius = circleRadius; (circleRadius-actCircleRadius)<=globalParameters.getInitCellCoveredDistInMikron();actCircleRadius-=SUPRABASAL_CELL_WIDTH){
				double angleIncrement = 2*Math.asin(SUPRABASAL_CELL_WIDTH/(2*actCircleRadius));
				double angleIncremtentDegrees = Math.toDegrees(angleIncrement);
			//	while((360%angleIncremtentDegrees)!=0 && angleIncremtentDegrees<360){
			//		angleIncremtentDegrees++;
			//	}			
				for (double alpha= 0; alpha < 360; alpha += angleIncremtentDegrees){
					
						Double3D newloc = new Double3D((circleCenter.x+ (actCircleRadius*Math.cos(Math.toRadians(alpha)))), 
								 								  (yZeroLine+BASAL_CELL_HEIGHT + (SUPRABASAL_CELL_HEIGHT/2d) +(y*SUPRABASAL_CELL_HEIGHT)), 
								 								  (circleCenter.y+ (actCircleRadius*Math.sin(Math.toRadians(alpha)))));
						UniversalCell cell = new UniversalCell(null, null, true);					
						
						AdhesiveCenterBased3DModel mechModel = ((AdhesiveCenterBased3DModel) cell.getEpisimBioMechanicalModelObject());					
						mechModel.setKeratinoWidth(SUPRABASAL_CELL_WIDTH);
						mechModel.setKeratinoHeight(SUPRABASAL_CELL_HEIGHT);
						mechModel.setKeratinoLength(SUPRABASAL_CELL_LENGTH);
						mechModel.setStandardCellWidth(BASAL_CELL_WIDTH);
						mechModel.setStandardCellHeight(BASAL_CELL_HEIGHT);
						mechModel.setStandardCellLength(BASAL_CELL_LENGTH);
						mechModel.setCellLocationInCellField(newloc);
						standardCellEnsemble.add(cell);					
						cell.getEpisimCellBehavioralModelObject().setCellType(cellTypes[0]);					
						
						
						if(diffLevels.length>2)cell.getEpisimCellBehavioralModelObject().setDiffLevel(diffLevels[2]);
						if(firstCell&&firstSuprabasalLayer) mechModel.setHasFixedPosition(true);						
				}
				firstCell=false;
			}			
			firstSuprabasalLayer = false;
		}
		
		return standardCellEnsemble;
	}

	protected ArrayList<UniversalCell> buildInitialCellEnsemble() {
		ArrayList<UniversalCell> loadedCells = super.buildInitialCellEnsemble();

		for (UniversalCell uCell : loadedCells) {				
			AdhesiveCenterBased3DModel centerBasedModel = (AdhesiveCenterBased3DModel) uCell.getEpisimBioMechanicalModelObject();
		}
		return loadedCells;
	}

	
	protected void initializeCellEnsembleBasedOnRandomAgeDistribution(ArrayList<UniversalCell> cellEnsemble) {

		// This method has to be implemented but has nothing to do in this model

	}

	private void setInitialGlobalParametersValues(AdhesiveCenterBased3DModelGP globalParameters){
		
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

