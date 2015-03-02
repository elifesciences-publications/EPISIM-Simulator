package episimmcc.centerbased.adhesion;

import java.lang.reflect.Field;
import java.util.ArrayList;

import javax.vecmath.Point2d;

import sim.app.episim.EpisimExceptionHandler;
import sim.app.episim.model.UniversalCell;
import sim.app.episim.model.biomechanics.centerbased2D.oldmodel.wound.AdhesiveCenterBased2DModel;
import sim.app.episim.model.biomechanics.centerbased2D.oldmodel.wound.AdhesiveCenterBased2DModelGP;
import sim.app.episim.model.biomechanics.latticebased2D.LatticeBased2DModelGP;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters;
import sim.app.episim.persistence.SimulationStateData;
import sim.app.episim.tissueimport.StandardMembrane;
import sim.app.episim.tissueimport.TissueController;
import sim.app.episim.visualization.ContinuousUniversalCellPortrayal2D;
import sim.app.episim.visualization.UniversalCellPortrayal2D;
import sim.util.Double2D;
import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import episiminterfaces.EpisimCellType;
import episiminterfaces.EpisimDifferentiationLevel;
import episiminterfaces.EpisimPortrayal;
import episimmcc.centerbased.CenterBasedMechModelInit;


public class AdhesiveCenterBasedMechModelInit extends BiomechanicalModelInitializer {

	SimulationStateData simulationStateData = null;

	public AdhesiveCenterBasedMechModelInit() {
		super();		
		AdhesiveCenterBased2DModelGP globalParameters = (AdhesiveCenterBased2DModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		TissueController.getInstance().getTissueBorder().loadStandardMembrane(globalParameters.getBasalMembraneDiscrSteps(), globalParameters.getBasalMembraneContactTimeThreshold());
		setInitialGlobalParametersValues(globalParameters);
	}

	public AdhesiveCenterBasedMechModelInit(SimulationStateData simulationStateData) {
		super(simulationStateData);
		this.simulationStateData = simulationStateData;
	}

	
	protected ArrayList<UniversalCell> buildStandardInitialCellEnsemble() {
		double BASAL_CELL_WIDTH=0;
		double BASAL_CELL_HEIGHT=0;
		double SUPRABASAL_CELL_WIDTH=0;
		double SUPRABASAL_CELL_HEIGHT=0;
		AdhesiveCenterBased2DModelGP globalParameters = (AdhesiveCenterBased2DModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		TissueController.getInstance().getTissueBorder().loadStandardMembrane(globalParameters.getBasalMembraneDiscrSteps(), globalParameters.getBasalMembraneContactTimeThreshold());
		
		EpisimCellBehavioralModelGlobalParameters cbGP = ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters();
		AdhesiveCenterBased2DModelGP mechModelGP = (AdhesiveCenterBased2DModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		
		try{
	      Field field = cbGP.getClass().getDeclaredField("BASAL_CELL_WIDTH");
	      BASAL_CELL_WIDTH = field.getDouble(cbGP);
	      
	      field = cbGP.getClass().getDeclaredField("BASAL_CELL_HEIGHT");
	      BASAL_CELL_HEIGHT = field.getDouble(cbGP);
	      
	      field = cbGP.getClass().getDeclaredField("SUPRABASAL_CELL_WIDTH");
	      SUPRABASAL_CELL_WIDTH = field.getDouble(cbGP);
	      
	      field = cbGP.getClass().getDeclaredField("SUPRABASAL_CELL_HEIGHT");
	      SUPRABASAL_CELL_HEIGHT = field.getDouble(cbGP);
	      
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
		
		
		//seed basal layer left side
		boolean firstCell = true;
		double yZeroLine = TissueController.getInstance().getTissueBorder().lowerBoundInMikron(0,0);
		for (double x = (0.5*BASAL_CELL_WIDTH); x <= mechModelGP.getInitCellCoveredDistInMikron(); x += BASAL_CELL_WIDTH) {
			Double2D newloc = new Double2D(x, yZeroLine+ (BASAL_CELL_HEIGHT/2));				
			UniversalCell cell = new UniversalCell(null, null, true);
			AdhesiveCenterBased2DModel mechModel = ((AdhesiveCenterBased2DModel) cell.getEpisimBioMechanicalModelObject());
			Point2d corrPos =new Point2d(newloc.x, newloc.y);//mechModel.calculateLowerBoundaryPositionForCell(new Point2d(newloc.x, newloc.y));
			mechModel.setKeratinoWidth(BASAL_CELL_WIDTH);
			mechModel.setKeratinoHeight(BASAL_CELL_HEIGHT);	
			mechModel.setStandardCellWidth(BASAL_CELL_WIDTH);
			mechModel.setStandardCellHeight(BASAL_CELL_HEIGHT);	
			mechModel.getCellEllipseObject().setXY(corrPos.x, corrPos.y);
			mechModel.setCellLocationInCellField(new Double2D(corrPos.x, corrPos.y));
			standardCellEnsemble.add(cell);
			
			cell.getEpisimCellBehavioralModelObject().setCellType(cellTypes[0]);
			if(firstCell){
				cell.getEpisimCellBehavioralModelObject().setDiffLevel(diffLevels[0]);
			}
			else{
				if(diffLevels.length>1)cell.getEpisimCellBehavioralModelObject().setDiffLevel(diffLevels[1]);
			}
			firstCell=false;			
		}
		
		//set basal contact time to threshold
		StandardMembrane membrane = TissueController.getInstance().getTissueBorder().getStandardMembrane();
		if(membrane != null && membrane.isDiscretizedMembrane()){
			for (double x = 0; x < mechModelGP.getInitCellCoveredDistInMikron(); x += 1){
				membrane.setContactTimeForReferenceCoordinate2D(new Double2D(x, membrane.lowerBoundInMikron(x, 0)), mechModelGP.getBasalMembraneContactTimeThreshold());
			}
		}
		
		
		
		
		//seed basal layer right side
				firstCell = true;
				yZeroLine = TissueController.getInstance().getTissueBorder().lowerBoundInMikron(0,0);
				for (double x = (mechModelGP.getWidthInMikron()-(0.5*BASAL_CELL_WIDTH)); x >= (mechModelGP.getWidthInMikron()- mechModelGP.getInitCellCoveredDistInMikron()); x -= BASAL_CELL_WIDTH) {
					Double2D newloc = new Double2D(x, yZeroLine+ (BASAL_CELL_HEIGHT/2));				
					UniversalCell cell = new UniversalCell(null, null, true);
					AdhesiveCenterBased2DModel mechModel = ((AdhesiveCenterBased2DModel) cell.getEpisimBioMechanicalModelObject());
					Point2d corrPos =new Point2d(newloc.x, newloc.y);//mechModel.calculateLowerBoundaryPositionForCell(new Point2d(newloc.x, newloc.y));
					mechModel.setKeratinoWidth(BASAL_CELL_WIDTH);
					mechModel.setKeratinoHeight(BASAL_CELL_HEIGHT);
					mechModel.setStandardCellWidth(BASAL_CELL_WIDTH);
					mechModel.setStandardCellHeight(BASAL_CELL_HEIGHT);
					mechModel.getCellEllipseObject().setXY(corrPos.x, corrPos.y);
					mechModel.setCellLocationInCellField(new Double2D(corrPos.x, corrPos.y));
					standardCellEnsemble.add(cell);
					
					cell.getEpisimCellBehavioralModelObject().setCellType(cellTypes[0]);
					if(firstCell){
						cell.getEpisimCellBehavioralModelObject().setDiffLevel(diffLevels[0]);
						mechModel.setDividesToTheLeft(true);
					}
					else{
						if(diffLevels.length>1)cell.getEpisimCellBehavioralModelObject().setDiffLevel(diffLevels[1]);
					}
					firstCell=false;			
				}
				
				//set basal contact time to threshold
				membrane = TissueController.getInstance().getTissueBorder().getStandardMembrane();
				if(membrane != null && membrane.isDiscretizedMembrane()){
					for (double x = mechModelGP.getWidthInMikron(); x > (mechModelGP.getWidthInMikron()-mechModelGP.getInitCellCoveredDistInMikron()); x -= 1){
						membrane.setContactTimeForReferenceCoordinate2D(new Double2D(x, membrane.lowerBoundInMikron(x, 0)), mechModelGP.getBasalMembraneContactTimeThreshold());
					}
				}
		
		
		
		
		
		//seed suprabasal layers left side
		boolean firstSuprabasalLayer = true;
		for (double y = 0; y < 3 ; y++) {
			firstCell = true;
			for (double x = (0.5*SUPRABASAL_CELL_WIDTH); x <= mechModelGP.getInitCellCoveredDistInMikron(); x += SUPRABASAL_CELL_WIDTH) {
				Double2D newloc = new Double2D(x, yZeroLine+BASAL_CELL_HEIGHT + (SUPRABASAL_CELL_HEIGHT/2d) +(y*SUPRABASAL_CELL_HEIGHT));				
				UniversalCell cell = new UniversalCell(null, null, true);
				AdhesiveCenterBased2DModel mechModel = ((AdhesiveCenterBased2DModel) cell.getEpisimBioMechanicalModelObject());
				Point2d corrPos =new Point2d(newloc.x, newloc.y);//mechModel.calculateLowerBoundaryPositionForCell(new Point2d(newloc.x, newloc.y));
				mechModel.setKeratinoWidth(SUPRABASAL_CELL_WIDTH);
				mechModel.setKeratinoHeight(SUPRABASAL_CELL_HEIGHT);
				mechModel.setStandardCellWidth(BASAL_CELL_WIDTH);
				mechModel.setStandardCellHeight(BASAL_CELL_HEIGHT);
				mechModel.getCellEllipseObject().setXY(corrPos.x, corrPos.y);
				mechModel.setCellLocationInCellField(new Double2D(corrPos.x, corrPos.y));
				standardCellEnsemble.add(cell);
				
				cell.getEpisimCellBehavioralModelObject().setCellType(cellTypes[0]);
				if(firstCell&&firstSuprabasalLayer) mechModel.setHasFixedPosition(true);
				if(diffLevels.length>2)cell.getEpisimCellBehavioralModelObject().setDiffLevel(diffLevels[2]);
				firstCell=false;
			}
			firstSuprabasalLayer = false;
		}
		
		//seed suprabasal layers right side
				firstSuprabasalLayer = true;
				for (double y = 0; y < 3 ; y++) {
					firstCell = true;
					for (double x = (mechModelGP.getWidthInMikron()-(0.5*SUPRABASAL_CELL_WIDTH)); x >= (mechModelGP.getWidthInMikron()-mechModelGP.getInitCellCoveredDistInMikron()); x -= SUPRABASAL_CELL_WIDTH) {
						Double2D newloc = new Double2D(x, yZeroLine+BASAL_CELL_HEIGHT + (SUPRABASAL_CELL_HEIGHT/2d) +(y*SUPRABASAL_CELL_HEIGHT));				
						UniversalCell cell = new UniversalCell(null, null, true);
						AdhesiveCenterBased2DModel mechModel = ((AdhesiveCenterBased2DModel) cell.getEpisimBioMechanicalModelObject());
						Point2d corrPos =new Point2d(newloc.x, newloc.y);//mechModel.calculateLowerBoundaryPositionForCell(new Point2d(newloc.x, newloc.y));
						mechModel.setKeratinoWidth(SUPRABASAL_CELL_WIDTH);
						mechModel.setKeratinoHeight(SUPRABASAL_CELL_HEIGHT);
						mechModel.setStandardCellWidth(BASAL_CELL_WIDTH);
						mechModel.setStandardCellHeight(BASAL_CELL_HEIGHT);
						mechModel.getCellEllipseObject().setXY(corrPos.x, corrPos.y);
						mechModel.setCellLocationInCellField(new Double2D(corrPos.x, corrPos.y));
						standardCellEnsemble.add(cell);
						
						cell.getEpisimCellBehavioralModelObject().setCellType(cellTypes[0]);
						if(firstCell&&firstSuprabasalLayer) mechModel.setHasFixedPosition(true);
						if(diffLevels.length>2)cell.getEpisimCellBehavioralModelObject().setDiffLevel(diffLevels[2]);
						firstCell=false;
					}
					firstSuprabasalLayer = false;
				}
		
		return standardCellEnsemble;
	}

	protected ArrayList<UniversalCell> buildInitialCellEnsemble() {
		ArrayList<UniversalCell> loadedCells = super.buildInitialCellEnsemble();

		for (UniversalCell uCell : loadedCells) {				
			AdhesiveCenterBased2DModel centerBasedModel = (AdhesiveCenterBased2DModel) uCell.getEpisimBioMechanicalModelObject();
			centerBasedModel.getCellEllipseObject().setXY(centerBasedModel.getCellLocationInCellField().x, centerBasedModel.getCellLocationInCellField().y);
		}
		return loadedCells;
	}

	
	protected void initializeCellEnsembleBasedOnRandomAgeDistribution(ArrayList<UniversalCell> cellEnsemble) {

		// This method has to be implemented but has nothing to do in this model

	}

	private void setInitialGlobalParametersValues(AdhesiveCenterBased2DModelGP globalParameters){
		
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
