package episimmcc.centerbased2d.newmodel.roottip;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;

import sim.app.episim.EpisimExceptionHandler;
import sim.app.episim.model.UniversalCell;
import sim.app.episim.model.biomechanics.centerbased2d.roottip.RootTipCenterBased2DModel;
import sim.app.episim.model.biomechanics.centerbased2d.roottip.RootTipCenterBased2DModelGP;
import sim.app.episim.model.biomechanics.centerbased3d.fisheye.FishEyeCenterBased3DModel;
import sim.app.episim.model.biomechanics.centerbased3d.fisheye.FishEyeCenterBased3DModelGP;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.controller.TissueController;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters.MiscalleneousGlobalParameters3D;
import sim.app.episim.persistence.SimulationStateData;
import sim.app.episim.visualization.threedim.ContinuousCellFieldPortrayal3D;
import sim.app.episim.visualization.twodim.ContinousCellPortrayal2D;
import sim.app.episim.visualization.twodim.ContinuousCellFieldPortrayal2D;
import sim.util.Double2D;
import episiminterfaces.EpisimBiomechanicalModel;
import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import episiminterfaces.EpisimPortrayal;


public class RootTipCenterBasedMechModelInit extends BiomechanicalModelInitializer {

	// Fields
	SimulationStateData simulationStateData = null;
	private static double CELL_WIDTH  = 0;
	private static double CELL_HEIGHT = 0;

	// Constructor for new simulation
	public RootTipCenterBasedMechModelInit() {
		super();
		// Do not load standard tissue membrane
		TissueController.getInstance().getTissueBorder().loadNoMembrane();
		MiscalleneousGlobalParameters param = MiscalleneousGlobalParameters.getInstance();
		if(param instanceof MiscalleneousGlobalParameters3D){
			((MiscalleneousGlobalParameters3D)param).setStandardMembrane_2_Dim_Gauss(false);
			((MiscalleneousGlobalParameters3D)param).setOptimizedGraphics(false);
		}
	}

	// Constructor for loading from saved snapshot
	public RootTipCenterBasedMechModelInit(SimulationStateData simulationStateData) {
		super(simulationStateData);
		this.simulationStateData = simulationStateData;
	}

	// Implementation of abstract method buildStandardInitialCellEnsemble in BiomechanicalModelInitializer
	protected ArrayList<UniversalCell> buildStandardInitialCellEnsemble() {
		
		EpisimCellBehavioralModelGlobalParameters cbGP = ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters();		
		try
		{
			Field field = cbGP.getClass().getDeclaredField("WIDTH_DEFAULT");
			CELL_WIDTH = field.getDouble(cbGP);
			
			field = cbGP.getClass().getDeclaredField("HEIGHT_DEFAULT");
			CELL_HEIGHT = field.getDouble(cbGP);
	      
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
		
		// Get the biomechanical global parameters
		RootTipCenterBased2DModelGP mechModelGP = (RootTipCenterBased2DModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();

		double rootdome = mechModelGP.getRootdome();
		double rootrad  = mechModelGP.getRootradius();
		double xstart = mechModelGP.getXbound();
		double ystart = mechModelGP.getYbound();

		// Number of initial cell rows and columns
		int cellrows = 9;
		int cellcols = 11;
		
		// Distances for placing cells
		double xdelta = (2*rootrad)/cellcols;
		double ydelta = (rootdome + rootrad*2)/(cellcols+1);
		
		// Create cells at predefined positions

    	int sub = (cellcols/2) - 1; // Used to make narrowing tip
    	
	    for (int row = 0; row < cellrows; row+=1 ) {
	    	
	    	for (int col = sub; col < cellcols-sub; col+=1) {

				Double2D newPos;
				UniversalCell newcell = new UniversalCell(null, null, true);
				RootTipCenterBased2DModel mechModel = ((RootTipCenterBased2DModel) newcell.getEpisimBioMechanicalModelObject());	
		    	
		    	newPos = new Double2D(xstart + xdelta*col, ystart + ydelta*row);
				mechModel.setCellWidth(CELL_WIDTH);
				mechModel.setCellHeight(CELL_HEIGHT);
				mechModel.setStandardCellWidth(CELL_WIDTH);
				mechModel.setStandardCellHeight(CELL_HEIGHT);
				
				mechModel.setCellLocationInCellField(newPos);
					
				standardCellEnsemble.add(newcell);
	    	}
	    	
	    	if (sub > 0) {
	    		sub -= 1;
	    	}

		}

		//RootTipCenterBased2DModel.setDummyCellSize(CELL_WIDTH);
		//initializeBiomechanics(standardCellEnsemble);
		return standardCellEnsemble;
	}

	protected ArrayList<UniversalCell> buildInitialCellEnsemble() {
		ArrayList<UniversalCell> loadedCells = super.buildInitialCellEnsemble();

		for (UniversalCell uCell : loadedCells) {				
			RootTipCenterBased2DModel centerBasedModel = (RootTipCenterBased2DModel) uCell.getEpisimBioMechanicalModelObject();
			centerBasedModel.getCellEllipseObject().setXY(centerBasedModel.getX(), centerBasedModel.getY());
			centerBasedModel.getCellEllipseObject().setMajorAxisAndMinorAxis(centerBasedModel.getCellWidth(), centerBasedModel.getCellHeight());
		}
		return loadedCells;
	}

	// This part is the initial relaxation of the model, before the proper simulation starts
	private void initializeBiomechanics(ArrayList<UniversalCell> standardCellEnsemble){

		EpisimBiomechanicalModel biomech 		= ModelController.getInstance().getBioMechanicalModelController().getNewEpisimBioMechanicalModelObject(null, null);
		RootTipCenterBased2DModelGP mechModelGP = (RootTipCenterBased2DModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();

		if(biomech instanceof RootTipCenterBased2DModel){
			
			RootTipCenterBased2DModel cbBioMech = (RootTipCenterBased2DModel) biomech;			
			double cumulativeMigrationDist      = 0;
			
			do{
				cbBioMech.initialisationGlobalSimStep();
				cumulativeMigrationDist = getCumulativeMigrationDistance(standardCellEnsemble);
			}
			while(standardCellEnsemble.size() > 0 && ((cumulativeMigrationDist / standardCellEnsemble.size()) > mechModelGP.getMinAverageMigrationMikron()));

		}
	}
	
	// Get migration of cells during relaxation
	private double getCumulativeMigrationDistance(ArrayList<UniversalCell> standardCellEnsemble){
		double cumulativeMigrationDistance = 0;
		
		for(int i = 0; i < standardCellEnsemble.size(); i++){
			RootTipCenterBased2DModel mechModel = ((RootTipCenterBased2DModel) standardCellEnsemble.get(i).getEpisimBioMechanicalModelObject());
			cumulativeMigrationDistance += mechModel.getMigrationDistPerSimStep();
			
		}
		
		return cumulativeMigrationDistance;
	}
	
	protected void initializeCellEnsembleBasedOnRandomAgeDistribution(ArrayList<UniversalCell> cellEnsemble) {
		// This method has to be implemented but has nothing to do in this model
	}

	// Graphical initialization of cells
	protected EpisimPortrayal getCellPortrayal() {
		ContinousCellPortrayal2D cellPortrayal = new ContinousCellPortrayal2D(java.awt.Color.lightGray);
		ContinuousCellFieldPortrayal2D continousPortrayal = new ContinuousCellFieldPortrayal2D();
		continousPortrayal.setPortrayalName("Root Tip Cells");
		continousPortrayal.setPortrayalForClass(UniversalCell.class, cellPortrayal);
		continousPortrayal.setField(ModelController.getInstance().getBioMechanicalModelController().getCellField());
		return continousPortrayal;
	}

	protected EpisimPortrayal[] getAdditionalPortrayalsCellForeground() {
		//ContinousCellPortrayal2D cellPortrayal = new ContinousCellPortrayal2D(java.awt.Color.lightGray);
		//ContinuousCellFieldPortrayal2D continuousPortrayal = new ContinuousCellFieldPortrayal2D();
		//continuousPortrayal.setPortrayalName("Dummy Cells");
		//continuousPortrayal.setPortrayalForClass(UniversalCell.class, cellPortrayal);
		//continuousPortrayal.setField(RootTipCenterBased2DModel.getDummyCellField());
		//return new EpisimPortrayal[]{continuousPortrayal};
 		return new EpisimPortrayal[0];
	}

	protected EpisimPortrayal[] getAdditionalPortrayalsCellBackground() {
		return new EpisimPortrayal[0];
	}
}
