package episimbiomechanics.centerbased.newversion.chemotaxis;

import java.lang.reflect.Field;
import java.util.ArrayList;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.UniversalCell;
import sim.app.episim.model.biomechanics.centerbased.newversion.CenterBasedMechanicalModel;
import sim.app.episim.model.biomechanics.centerbased.newversion.chemotaxis.CenterBasedChemotaxisMechanicalModelGP;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import sim.app.episim.model.visualization.ContinuousUniversalCellPortrayal2D;
import sim.app.episim.model.visualization.UniversalCellPortrayal2D;
import sim.app.episim.persistence.SimulationStateData;
import sim.app.episim.tissue.TissueController;
import sim.util.Double2D;
import ec.util.MersenneTwisterFast;
import episiminterfaces.EpisimCellBehavioralModel;
import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import episiminterfaces.EpisimCellType;
import episiminterfaces.EpisimDifferentiationLevel;
import episiminterfaces.EpisimPortrayal;

public class CenterBasedMechModelInit extends BiomechanicalModelInitializer {

	SimulationStateData simulationStateData = null;
	MersenneTwisterFast random; 
	
	public CenterBasedMechModelInit() {
		super();
		TissueController.getInstance().getTissueBorder().loadNoMembrane();
		random = new MersenneTwisterFast(System.currentTimeMillis());
		CenterBasedChemotaxisMechanicalModelGP mechModelGP = (CenterBasedChemotaxisMechanicalModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		setInitialGlobalParametersValues(mechModelGP);
	}

	public CenterBasedMechModelInit(SimulationStateData simulationStateData) {
		super(simulationStateData);
		this.simulationStateData = simulationStateData;
		TissueController.getInstance().getTissueBorder().loadNoMembrane();
		random = new MersenneTwisterFast(System.currentTimeMillis());
	}

	

	protected ArrayList<UniversalCell> buildStandardInitialCellEnsemble() {
		double CELL_WIDTH=0;
		double CELL_HEIGHT=0;
		EpisimCellBehavioralModelGlobalParameters cbGP = ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters();		
		try{
	      Field field = cbGP.getClass().getDeclaredField("WIDTH_DEFAULT");
	      CELL_WIDTH = field.getDouble(cbGP);
	      
	      field = cbGP.getClass().getDeclaredField("HEIGHT_DEFAULT");
	      CELL_HEIGHT = field.getDouble(cbGP);   
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

		CenterBasedChemotaxisMechanicalModelGP mechModelGP = (CenterBasedChemotaxisMechanicalModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
			
		double width = mechModelGP.getWidthInMikron();
		double height = mechModelGP.getHeightInMikron();
		final double cellDistanceFact = 0.95;
		final double secretoryCellColumns = 4;
		double tCellDensity = mechModelGP.getTCellDensity();
		for (double y = (CELL_HEIGHT/2); y <= (height-(CELL_HEIGHT/2)); y += (CELL_HEIGHT*cellDistanceFact)) {
			for (double x = (CELL_WIDTH/2); x < (width-(CELL_WIDTH/2)); x += (CELL_WIDTH*cellDistanceFact)) {
				UniversalCell cell = new UniversalCell(null, null, true);
				CenterBasedMechanicalModel mechModel = ((CenterBasedMechanicalModel) cell.getEpisimBioMechanicalModelObject());
				Double2D cellPos =new Double2D(x, y);//mechModel.calculateLowerBoundaryPositionForCell(new Point2d(newloc.x, newloc.y));
				mechModel.setCellWidth(CELL_WIDTH);
				mechModel.setCellHeight(CELL_HEIGHT);	
				mechModel.setStandardCellWidth(CELL_WIDTH);
				mechModel.setStandardCellHeight(CELL_HEIGHT);
				
				mechModel.getCellEllipseObject().setXY(cellPos.x, cellPos.y);
				mechModel.getCellEllipseObject().setMajorAxisAndMinorAxis(CELL_WIDTH, CELL_HEIGHT);
				mechModel.setCellLocationInCellField(cellPos);
				
				EpisimCellBehavioralModel cbm=  cell.getEpisimCellBehavioralModelObject();
				EpisimCellBehavioralModelGlobalParameters globalCbmParam=  ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters();
				EpisimCellType[] cellTypes = globalCbmParam.getAvailableCellTypes();
				if(cellTypes.length >=3){
					if((x >= ((width/2)-(secretoryCellColumns/2d)*(CELL_WIDTH*cellDistanceFact)) && x <= ((width/2)+(secretoryCellColumns/2d)*(CELL_WIDTH*cellDistanceFact)))
					 &&(y >= ((height/2)-(secretoryCellColumns/2d)*(CELL_HEIGHT*cellDistanceFact)) && y <= ((height/2)+(secretoryCellColumns/2d)*(CELL_HEIGHT*cellDistanceFact)))){
						cbm.setCellType(cellTypes[1]);
					}
					else{						
						double rn =random.nextDouble();
						if(rn < tCellDensity){
							cbm.setCellType(cellTypes[2]);
						}
						else cbm.setCellType(cellTypes[0]);
					}					
				}
				else cbm.setCellType(cellTypes[0]);
				
				standardCellEnsemble.add(cell);	
			}
		}
		return standardCellEnsemble;
	}
	
	private void setInitialGlobalParametersValues(CenterBasedChemotaxisMechanicalModelGP mechModelGP){
		mechModelGP.setContinousDiffusionInXDirection(false);
		mechModelGP.setContinousDiffusionInYDirection(false);
		mechModelGP.setChemotaxisEnabled(true);
		mechModelGP.setRandomness(0.1);
		mechModelGP.setWidthInMikron(200);
		mechModelGP.setHeightInMikron(200);
	}

	protected ArrayList<UniversalCell> buildInitialCellEnsemble() {
		ArrayList<UniversalCell> loadedCells = super.buildInitialCellEnsemble();

		for (UniversalCell uCell : loadedCells) {				
			CenterBasedMechanicalModel centerBasedModel = (CenterBasedMechanicalModel) uCell.getEpisimBioMechanicalModelObject();
			centerBasedModel.getCellEllipseObject().setXY(centerBasedModel.getCellLocationInCellField().x, centerBasedModel.getCellLocationInCellField().y);
			centerBasedModel.getCellEllipseObject().setMajorAxisAndMinorAxis(centerBasedModel.getCellWidth(), centerBasedModel.getCellHeight());
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
