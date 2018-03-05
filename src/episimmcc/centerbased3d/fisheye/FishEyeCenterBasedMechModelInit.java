package episimmcc.centerbased3d.fisheye;

/*
 * Sets model initial conditions
 * * Initial cell position (calculated with icosahedral mesh)
 * * Differentiation levels
 * * Initial parameter values
 */

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;

import javax.vecmath.Point3d;

import sim.app.episim.EpisimExceptionHandler;
import sim.app.episim.model.AbstractCell;
import sim.app.episim.model.DummyCell;
import sim.app.episim.model.UniversalCell;
import sim.app.episim.model.biomechanics.centerbased3d.fisheye.FishEyeCenterBased3DModel;
import sim.app.episim.model.biomechanics.centerbased3d.fisheye.FishEyeCenterBased3DModelGP;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.controller.TissueController;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters.MiscalleneousGlobalParameters3D;
import sim.app.episim.persistence.SimulationStateData;
import sim.app.episim.util.GenericBag;
import sim.app.episim.util.Icosahedron;
import sim.app.episim.visualization.threedim.ContinuousCellFieldPortrayal3D;
import sim.util.Double3D;
import episiminterfaces.EpisimBiomechanicalModel;
import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import episiminterfaces.EpisimDifferentiationLevel;
import episiminterfaces.EpisimPortrayal;


public class FishEyeCenterBasedMechModelInit extends BiomechanicalModelInitializer {

	// Fields
	SimulationStateData simulationStateData = null;
	private static double CELL_WIDTH=0;
	private static double CELL_HEIGHT=0;
	private static double CELL_LENGTH=0;

	// Constructor
	public FishEyeCenterBasedMechModelInit() {
		super();
		TissueController.getInstance().getTissueBorder().loadNoMembrane();
		MiscalleneousGlobalParameters param = MiscalleneousGlobalParameters.getInstance();
		if(param instanceof MiscalleneousGlobalParameters3D){
			((MiscalleneousGlobalParameters3D)param).setStandardMembrane_2_Dim_Gauss(false);
			((MiscalleneousGlobalParameters3D)param).setOptimizedGraphics(false);
		}
	}

	// When loading from saved snapshot
	public FishEyeCenterBasedMechModelInit(SimulationStateData simulationStateData) {
		super(simulationStateData);
		this.simulationStateData = simulationStateData;
	}
	
	// Implementation of abstract method buildStandardInitialCellEnsemble in BiomechanicalModelInitializer
	protected ArrayList<UniversalCell> buildStandardInitialCellEnsemble() {
			
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

		// Get the biomechanical global parameters
		FishEyeCenterBased3DModelGP mechModelGP = (FishEyeCenterBased3DModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		mechModelGP.setInnerEyeRadius(mechModelGP.getInitialInnerEyeRadius());
		
		Point3d fishEyeCenter = mechModelGP.getInnerEyeCenter();
		double cellSize = Math.max(CELL_WIDTH, CELL_HEIGHT);
		cellSize = Math.max(cellSize, CELL_LENGTH);
		
		
		HashSet<Point3d> existingCoordinates = new HashSet<Point3d>();
		
		//	double radius2 = radius*Math.cos((Math.PI/2d - (theta/2d)));
		//	double angleIncrement2 = Math.acos(((Math.pow(radius2, 2)+Math.pow(radius2, 2)-Math.pow(cellSize, 2))/(2d*radius2*radius2)));
		
		Point3d previousPoint = null;
		
	/*		An attempt to use spherical coordinates to calculate vertices?
	 * 
	   	for(double phi = 0; phi <= Math.PI ; phi+=angleIncrement){				
				
				for(double theta =  Math.PI; theta >= Math.PI/2d; theta-=angleIncrement){
				
				double z = radius*Math.sin(theta)*Math.cos(phi);
				double x = radius*Math.sin(theta)*Math.sin(phi);
				double y = radius*Math.cos(theta);
	*/
		
		double radius    = mechModelGP.getInitialInnerEyeRadius();
		// generate icosahedral mesh and subdivide it x times
		Icosahedron ico  = new Icosahedron(5); // 5
		int ignoredCells = 0;

		// Set allowed initial cell number to given initial radius and density:		
		double cellradius     = cellSize/2d;
		double tol_overlap    = 1 - mechModelGP.getLinearToExpMaxOverlap_perc();
		double hemispherearea = 2*Math.PI*Math.pow(radius,2);
		double cellarea       = Math.PI*Math.pow(cellradius*(1-tol_overlap),2);
		int num_cells_fit  	 = (int) Math.ceil(hemispherearea/cellarea);
		System.out.println("Cells that fit:" + num_cells_fit);
		//System.out.println(ico.getVertexList().size());
		
		int cell_counter = 0;
		
	   for (int i = 0; i < ico.getVertexList().size(); i+=3 ) {
	         // Get icosahedral mesh vertex coordinates (normalized) and blow them up to initial eye radius
				double x = ico.getVertexList().get(i)   * radius;
				double y = ico.getVertexList().get(i+1) * radius;
				double z = ico.getVertexList().get(i+2) * radius;
				
				// Translate coordinates to be centered around fishEyeCenter
				// By default fishEyeCenter.x is at 50, so no cell should sit at x = 50, i.e. the cell's x coordinate is > 50
				Point3d newPos = new Point3d(fishEyeCenter.x + x, fishEyeCenter.y + y, fishEyeCenter.z + z);
				
				// Check if this new position already exists, if its x coordinate is > 50 and if 
				// the previous point is null or is at a distance greater than a cell from the new point
				if(!existingCoordinates.contains(newPos) && newPos.x >= fishEyeCenter.x && (previousPoint == null || previousPoint.distance(newPos)>=cellSize)){
					
					if (cell_counter <= num_cells_fit) {
						cell_counter += 1;
						
						//if(previousPoint==null ||(previousPoint.distance(newPos))>=cellSize){
						UniversalCell stemCell = new UniversalCell(null, null, true);
						FishEyeCenterBased3DModel mechModel=((FishEyeCenterBased3DModel) stemCell.getEpisimBioMechanicalModelObject());					
							
						mechModel.setCellWidth(CELL_WIDTH);
						mechModel.setCellHeight(CELL_HEIGHT);
						mechModel.setCellLength(CELL_LENGTH);
						mechModel.setStandardCellWidth(CELL_WIDTH);
						mechModel.setStandardCellHeight(CELL_HEIGHT);
						mechModel.setStandardCellLength(CELL_LENGTH);
							
						existingCoordinates.add(newPos);
						previousPoint = newPos;
						mechModel.setPositionRespectingBounds(newPos, CELL_WIDTH/2d, CELL_HEIGHT/2d, CELL_LENGTH/2d, mechModelGP.getOptDistanceToBMScalingFactor(), true);			
						standardCellEnsemble.add(stemCell);					
					}
					else ignoredCells++;
				}
				else ignoredCells++;
				
		//	}
		}
		initializeBiomechanics(standardCellEnsemble);
		setDiffLevels(standardCellEnsemble, cellSize);
		FishEyeCenterBased3DModel.setDummyCellSize(cellSize);
		
		System.out.println("No of cells: " + standardCellEnsemble.size()+ "    Cells Ignored: " + ignoredCells);
		return standardCellEnsemble;
	}
	
	private void setDiffLevels(ArrayList<UniversalCell> standardCellEnsemble, double cellSize){
		
		FishEyeCenterBased3DModelGP mechModelGP = (FishEyeCenterBased3DModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		EpisimDifferentiationLevel[] diffLevels = ModelController.getInstance().getCellBehavioralModelController().getAvailableDifferentiationLevels();
		
		double prolifBeltSize = mechModelGP.getProlifCompWidthMikron();
		double radius 			 = mechModelGP.getInitialInnerEyeRadius();
		
		// The following is used to calculate the x interval within which initialized cells are assigned to diffLevel[1] (proliferative)
		// The angle subtended by the proliferative belt can be calculated for the 2D case due to symmetry
		// The proliferative belt spans the distance as measured by a ruler (projected distance)
		double angleIncrement = Math.PI/2d - Math.acos(prolifBeltSize/radius);

		// xDelta is the arc given by the angle
		double xDelta 			 = radius * angleIncrement;
		
		// Set differentiation levels of cells
		for(int i=0; i < standardCellEnsemble.size(); i++){
			
			UniversalCell actCell 				= standardCellEnsemble.get(i);
			EpisimBiomechanicalModel biomech = actCell.getEpisimBioMechanicalModelObject();
			
			if(biomech instanceof FishEyeCenterBased3DModel){
				if(diffLevels.length>2){
					if((biomech.getX()+(cellSize/2d)) <= (mechModelGP.getInnerEyeCenter().x + xDelta)){
						actCell.getEpisimCellBehavioralModelObject().setDiffLevel(diffLevels[1]);
					}
					else{
						actCell.getEpisimCellBehavioralModelObject().setDiffLevel(diffLevels[2]);
					}
				}
			}
		}
	}
	
	// This part is the initial relaxation of the model, before the proper simulation starts
	private void initializeBiomechanics(ArrayList<UniversalCell> standardCellEnsemble){
		
		EpisimBiomechanicalModel biomech 		 = ModelController.getInstance().getBioMechanicalModelController().getNewEpisimBioMechanicalModelObject(null, null);
		FishEyeCenterBased3DModelGP mechModelGP = (FishEyeCenterBased3DModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		
		if(biomech instanceof FishEyeCenterBased3DModel){
			
			FishEyeCenterBased3DModel cbBioMech = (FishEyeCenterBased3DModel) biomech;			
			double cumulativeMigrationDist      = 0;
			
			do{
				cbBioMech.initialisationGlobalSimStep();
				cumulativeMigrationDist = getCumulativeMigrationDistance(standardCellEnsemble);
				//System.out.println("Average migration:" + cumulativeMigrationDist/standardCellEnsemble.size());
			}
			while(standardCellEnsemble.size() > 0 && ((cumulativeMigrationDist / standardCellEnsemble.size()) > mechModelGP.getMinAverageMigrationMikron()));
			
		}
	}
	
	// Get migration of cells during relaxation
	private double getCumulativeMigrationDistance(ArrayList<UniversalCell> standardCellEnsemble){
		
		double cumulativeMigrationDistance = 0;
		
		for(int i = 0; i < standardCellEnsemble.size(); i++){
			FishEyeCenterBased3DModel mechModel = ((FishEyeCenterBased3DModel) standardCellEnsemble.get(i).getEpisimBioMechanicalModelObject());
			cumulativeMigrationDistance += mechModel.getMigrationDistPerSimStep();
			
		}
		
		return cumulativeMigrationDistance;
	}
	
 	// Set cell dimensions
	protected ArrayList<UniversalCell> buildInitialCellEnsemble() {
		
		ArrayList<UniversalCell> loadedCells 			  = super.buildInitialCellEnsemble();
		EpisimCellBehavioralModelGlobalParameters cbGP = ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters();		
		
		try{
	      Field field = cbGP.getClass().getDeclaredField("WIDTH_DEFAULT");
	      CELL_WIDTH  = field.getDouble(cbGP);
	      
	      field 		= cbGP.getClass().getDeclaredField("HEIGHT_DEFAULT");
	      CELL_HEIGHT = field.getDouble(cbGP);
	      
	      field 		= cbGP.getClass().getDeclaredField("LENGTH_DEFAULT");
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
		
		double cellSize = Math.max(CELL_WIDTH, CELL_HEIGHT);
		cellSize 		= Math.max(cellSize, CELL_LENGTH);
		FishEyeCenterBased3DModel.setDummyCellSize(cellSize);
		
		return loadedCells;
	}

	
	protected void initializeCellEnsembleBasedOnRandomAgeDistribution(ArrayList<UniversalCell> cellEnsemble){
		// This method has to be implemented but has nothing to do in this model
	}

	protected EpisimPortrayal getCellPortrayal() {
		ContinuousCellFieldPortrayal3D continuousPortrayal = new ContinuousCellFieldPortrayal3D("Fish Eye");
		continuousPortrayal.setField(ModelController.getInstance().getBioMechanicalModelController().getCellField());
		return continuousPortrayal;
	}

	protected EpisimPortrayal[] getAdditionalPortrayalsCellForeground() {
		ContinuousCellFieldPortrayal3D continuousPortrayal = new ContinuousCellFieldPortrayal3D("Dummy Cells");
		continuousPortrayal.setField(FishEyeCenterBased3DModel.getDummyCellField());
		
		return new EpisimPortrayal[]{continuousPortrayal};
	}

	protected EpisimPortrayal[] getAdditionalPortrayalsCellBackground() {
		return new EpisimPortrayal[0];
	}

}
