package episimbiomechanics.centerbased3d.newversion.chemotaxis;

import java.lang.reflect.Field;
import java.util.ArrayList;

import sim.app.episim.EpisimExceptionHandler;
import sim.app.episim.model.UniversalCell;
import sim.app.episim.model.biomechanics.centerbased3d.newversion.CenterBased3DMechanicalModel;
import sim.app.episim.model.biomechanics.centerbased3d.newversion.chemotaxis.CenterBasedChemotaxis3DMechanicalModelGP;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import sim.app.episim.persistence.SimulationStateData;
import sim.app.episim.tissue.TissueController;
import sim.app.episim.visualization.ContinuousUniversalCellPortrayal3D;
import sim.util.Double3D;
import ec.util.MersenneTwisterFast;
import episiminterfaces.EpisimCellBehavioralModel;
import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import episiminterfaces.EpisimCellType;
import episiminterfaces.EpisimPortrayal;

public class ChemotaxisCenterBasedMechModelInit  extends BiomechanicalModelInitializer {

	SimulationStateData simulationStateData = null;
	MersenneTwisterFast random; 
	
	public ChemotaxisCenterBasedMechModelInit() {
		super();
		TissueController.getInstance().getTissueBorder().loadNoMembrane();
		random = new MersenneTwisterFast(System.currentTimeMillis());
		CenterBasedChemotaxis3DMechanicalModelGP mechModelGP = (CenterBasedChemotaxis3DMechanicalModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		setInitialGlobalParametersValues(mechModelGP);
	}

	public ChemotaxisCenterBasedMechModelInit(SimulationStateData simulationStateData) {
		super(simulationStateData);
		this.simulationStateData = simulationStateData;
		TissueController.getInstance().getTissueBorder().loadNoMembrane();
		random = new MersenneTwisterFast(System.currentTimeMillis());
	}

	

	protected ArrayList<UniversalCell> buildStandardInitialCellEnsemble() {
		double CELL_WIDTH=0;
		double CELL_HEIGHT=0;
		double CELL_LENGTH=0;
		EpisimCellBehavioralModelGlobalParameters cbGP = ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters();		
		try{
	      Field field = cbGP.getClass().getDeclaredField("WIDTH_DEFAULT");
	      CELL_WIDTH = field.getDouble(cbGP);
	      
	      field = cbGP.getClass().getDeclaredField("HEIGHT_DEFAULT");
	      CELL_HEIGHT = field.getDouble(cbGP);
	      
	      field = cbGP.getClass().getDeclaredField("LENGTH_DEFAULT");
	      CELL_LENGTH = field.getDouble(cbGP);   
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

		CenterBasedChemotaxis3DMechanicalModelGP mechModelGP = (CenterBasedChemotaxis3DMechanicalModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
			
		double width = mechModelGP.getWidthInMikron();
		double height = mechModelGP.getHeightInMikron();
		double length = mechModelGP.getLengthInMikron();
		final double cellDistanceFact = 0.95;
		final double secretoryCellColumns = 4;
		double tCellDensity = mechModelGP.getTCellDensity();
		for (double z = (CELL_LENGTH/2); z <= (length-(CELL_LENGTH/2)); z += (CELL_LENGTH*cellDistanceFact)) {
			for (double y = (CELL_HEIGHT/2); y <= (height-(CELL_HEIGHT/2)); y += (CELL_HEIGHT*cellDistanceFact)) {
				for (double x = (CELL_WIDTH/2); x < (width-(CELL_WIDTH/2)); x += (CELL_WIDTH*cellDistanceFact)) {
					UniversalCell cell = new UniversalCell(null, null, true);
					CenterBased3DMechanicalModel mechModel = ((CenterBased3DMechanicalModel) cell.getEpisimBioMechanicalModelObject());
					Double3D cellPos =new Double3D(x, y, z);//mechModel.calculateLowerBoundaryPositionForCell(new Point2d(newloc.x, newloc.y));
					mechModel.setCellWidth(CELL_WIDTH);
					mechModel.setCellHeight(CELL_HEIGHT);
					mechModel.setCellLength(CELL_LENGTH);
					mechModel.setStandardCellWidth(CELL_WIDTH);
					mechModel.setStandardCellHeight(CELL_HEIGHT);
					mechModel.setStandardCellLength(CELL_LENGTH);	
				
					mechModel.setCellLocationInCellField(cellPos);
					
					EpisimCellBehavioralModel cbm=  cell.getEpisimCellBehavioralModelObject();
					EpisimCellBehavioralModelGlobalParameters globalCbmParam=  ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters();
					EpisimCellType[] cellTypes = globalCbmParam.getAvailableCellTypes();
					if(cellTypes.length >=3){
						if((x >= ((width/2)-(secretoryCellColumns/2d)*(CELL_WIDTH*cellDistanceFact)) && x <= ((width/2)+(secretoryCellColumns/2d)*(CELL_WIDTH*cellDistanceFact)))
						 &&(y >= ((height/2)-(secretoryCellColumns/2d)*(CELL_HEIGHT*cellDistanceFact)) && y <= ((height/2)+(secretoryCellColumns/2d)*(CELL_HEIGHT*cellDistanceFact)))
						 &&(z >= ((length/2)-(secretoryCellColumns/2d)*(CELL_LENGTH*cellDistanceFact)) && z <= ((length/2)+(secretoryCellColumns/2d)*(CELL_LENGTH*cellDistanceFact)))){
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
		}
		return standardCellEnsemble;
	}
	
	private void setInitialGlobalParametersValues(CenterBasedChemotaxis3DMechanicalModelGP mechModelGP){
		mechModelGP.setContinousDiffusionInXDirection(false);
		mechModelGP.setContinousDiffusionInYDirection(false);
		mechModelGP.setContinousDiffusionInZDirection(false);
		mechModelGP.setChemotaxisEnabled(true);
		mechModelGP.setRandomness(0.1);
		mechModelGP.setWidthInMikron(200);
		mechModelGP.setHeightInMikron(200);
		mechModelGP.setLengthInMikron(200);
		mechModelGP.setNumberOfSecondsPerSimStep(1);
	}

	protected ArrayList<UniversalCell> buildInitialCellEnsemble() {
		ArrayList<UniversalCell> loadedCells = super.buildInitialCellEnsemble();

	/*	for (UniversalCell uCell : loadedCells) {				
			CenterBased3DMechanicalModel centerBasedModel = (CenterBased3DMechanicalModel) uCell.getEpisimBioMechanicalModelObject();
			
		}*/
		return loadedCells;
	}

	
	protected void initializeCellEnsembleBasedOnRandomAgeDistribution(ArrayList<UniversalCell> cellEnsemble) {

		// This method has to be implemented but has nothing to do in this model

	}

	protected EpisimPortrayal getCellPortrayal() {
		ContinuousUniversalCellPortrayal3D continuousPortrayal = new ContinuousUniversalCellPortrayal3D("Cells");
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
