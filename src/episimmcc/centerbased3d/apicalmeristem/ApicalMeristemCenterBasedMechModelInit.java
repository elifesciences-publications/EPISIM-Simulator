package episimmcc.centerbased3d.apicalmeristem;



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
import javax.vecmath.Vector3d;

	import sim.app.episim.EpisimExceptionHandler;
import sim.app.episim.model.AbstractCell;
import sim.app.episim.model.UniversalCell;
import sim.app.episim.model.biomechanics.centerbased3d.apicalmeristem.ApicalMeristemCenterBased3DModel;
import sim.app.episim.model.biomechanics.centerbased3d.apicalmeristem.ApicalMeristemCenterBased3DModelGP;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.controller.TissueController;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters.MiscalleneousGlobalParameters3D;
import sim.app.episim.persistence.SimulationStateData;
import sim.app.episim.util.GenericBag;
import sim.app.episim.util.Icosahedron;
import sim.app.episim.visualization.threedim.ContinuousCellFieldPortrayal3D;
import sim.field.continuous.Continuous3DExt;
import sim.util.Bag;
import sim.util.Double3D;
import ec.util.MersenneTwisterFast;
import episiminterfaces.EpisimBiomechanicalModel;
import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import episiminterfaces.EpisimCellType;
import episiminterfaces.EpisimDifferentiationLevel;
import episiminterfaces.EpisimPortrayal;
import episimmcc.EpisimModelConnector;


	public class ApicalMeristemCenterBasedMechModelInit extends BiomechanicalModelInitializer {

		// Fields
		SimulationStateData simulationStateData = null;
		private static double CELL_WIDTH=0;
		private static double CELL_HEIGHT=0;
		private static double CELL_LENGTH=0;
		private MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
		// Constructor
		public ApicalMeristemCenterBasedMechModelInit() {
			super();
			TissueController.getInstance().getTissueBorder().loadNoMembrane();
			MiscalleneousGlobalParameters param = MiscalleneousGlobalParameters.getInstance();
			if(param instanceof MiscalleneousGlobalParameters3D){
				((MiscalleneousGlobalParameters3D)param).setStandardMembrane_2_Dim_Gauss(false);
				((MiscalleneousGlobalParameters3D)param).setOptimizedGraphics(false);
			}
		}

		// When loading from saved snapshot
		public ApicalMeristemCenterBasedMechModelInit(SimulationStateData simulationStateData) {
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
			ApicalMeristemCenterBased3DModelGP mechModelGP = (ApicalMeristemCenterBased3DModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
					
			Point3d colonyCenter = mechModelGP.getCellColonyCenter();
			double cellSize = Math.max(CELL_WIDTH, CELL_HEIGHT);
			cellSize = Math.max(cellSize, CELL_LENGTH);		
					
			int overlapCells = 0;
			int invalidPositions = 0;
			
			double colonyRadius    = mechModelGP.getCellColonyRadius();
			double colonyHeight    = mechModelGP.getCellColonyHeight(); 
			mechModelGP.setL2MaxRadius(colonyRadius-cellSize);
			mechModelGP.setL3MaxRadius(colonyRadius-2*cellSize);
			
			
			Icosahedron ico  = new Icosahedron(6); // generate icosahedral mesh and subdivide it x times
			Continuous3DExt cellField = new Continuous3DExt(cellSize / 1.5, 
						  TissueController.getInstance().getTissueBorder().getWidthInMikron(), 
						  TissueController.getInstance().getTissueBorder().getHeightInMikron(),
						  TissueController.getInstance().getTissueBorder().getLengthInMikron());
			int numberOfVertices = ico.getVertexList().size();
			for(int i = 1; colonyRadius > 0 && colonyHeight > 0; i++){	
				
				// Set allowed initial cell number to given initial radius and density:		
				double cellradius     = cellSize/2d;
				double tol_overlap    = mechModelGP.getLinearToExpMaxOverlap_perc();
				double hemispherearea = 2*Math.PI*colonyRadius*colonyHeight;
				double cellarea       = Math.PI*Math.pow(cellradius*tol_overlap,2);
				double num_cells_fit  	 = (int) Math.ceil(hemispherearea/cellarea);
				System.out.println("Possible Cell Number:" + num_cells_fit);
				
				
				double cell_counter = 0;
				
				long timeStampLastCell = System.currentTimeMillis();
				while(cell_counter <= num_cells_fit && (System.currentTimeMillis()-timeStampLastCell) < 1000){
					
					double heightRadiusDelta = mechModelGP.getCellColonyRadius()-mechModelGP.getCellColonyHeight();
			
					int vertexNumber = (random.nextInt(numberOfVertices)/3)*3;
					
			       // Get icosahedral mesh vertex coordinates (normalized) and blow them up to initial eye radius
					double x = ico.getVertexList().get(vertexNumber)   * colonyRadius;
					double y = ico.getVertexList().get(vertexNumber+1) * colonyRadius;
					double z = ico.getVertexList().get(vertexNumber+2) * colonyRadius;
						
					Point3d newPos = new Point3d(colonyCenter.x + x, colonyCenter.y + y, colonyCenter.z + z);
						
					// Check if this new position already exists, if its x coordinate is > 50 and if 
					// the previous point is null or is at a distance greater than a cell from the new point
					if(newPos.x >= (colonyCenter.x+heightRadiusDelta)){// && (previousPoint == null || previousPoint.distance(newPos)>=cellSize)){ 
						Bag neighbors = cellField.getNeighborsWithinDistance(new Double3D(newPos.x, newPos.y, newPos.z), cellSize);
						boolean overlapFound = false;
							
						for(int n = 0; n < neighbors.size();n++){								
							UniversalCell cell = (UniversalCell) neighbors.get(n);								
							EpisimBiomechanicalModel bm = cell.getEpisimBioMechanicalModelObject();
							Point3d neighborPos = new Point3d(bm.getX(), bm.getY(), bm.getZ());								
							if(neighborPos.distance(newPos) < cellSize*0.5){									
								overlapFound = true;
							}
						}
						
						if(!overlapFound){						
							
							cell_counter += 1;
							
							UniversalCell stemCell = new UniversalCell(null, null, true);
							ApicalMeristemCenterBased3DModel mechModel=((ApicalMeristemCenterBased3DModel) stemCell.getEpisimBioMechanicalModelObject());					
								
							mechModel.setCellWidth(CELL_WIDTH);
							mechModel.setCellHeight(CELL_HEIGHT);
							mechModel.setCellLength(CELL_LENGTH);
							
							mechModel.setStandardCellWidth(CELL_WIDTH);
							mechModel.setStandardCellHeight(CELL_HEIGHT);
							mechModel.setStandardCellLength(CELL_LENGTH);
							mechModel.setPositionRespectingBounds(newPos, CELL_WIDTH/2d, CELL_HEIGHT/2d, CELL_LENGTH/2d, mechModelGP.getOptDistanceToBMScalingFactor(), true);	
							
							EpisimModelConnector mc = mechModel.getEpisimModelConnector();
							if(mc != null && mc instanceof EpisimApicalMeristemCenterBased3DMC){
								EpisimApicalMeristemCenterBased3DMC meristemMc = (EpisimApicalMeristemCenterBased3DMC) mc;
								if(i==1) meristemMc.setL1(true);
								else if(i==2) meristemMc.setL2(true);
								else meristemMc.setL3(true);
							}
							standardCellEnsemble.add(stemCell);	
							cellField.setObjectLocation(stemCell, new Double3D(mechModel.getX(), mechModel.getY(), mechModel.getZ()));						
							timeStampLastCell = System.currentTimeMillis();
						}
						else overlapCells++;
					}
					else invalidPositions++;	
						
				}
				colonyRadius -= cellSize;
				colonyHeight -= cellSize;
				System.out.println("Cell seeded: "+cell_counter);
			}
				
			//	}
			//}
			cellField.clear();
			initializeBiomechanics(standardCellEnsemble);
			setDiffLevels(standardCellEnsemble, cellSize);
			
			
			System.out.println("No of cells: " + standardCellEnsemble.size()+ "    Cells Ignored (Overlap): " + overlapCells+"    Invalid Positions: " + invalidPositions);
			return standardCellEnsemble;
		}
		
		
		
		private void setDiffLevels(ArrayList<UniversalCell> standardCellEnsemble, double cellSize){
			
			ApicalMeristemCenterBased3DModelGP mechModelGP = (ApicalMeristemCenterBased3DModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
			EpisimDifferentiationLevel[] diffLevels = ModelController.getInstance().getCellBehavioralModelController().getAvailableDifferentiationLevels();
			EpisimCellType[] cellTypes = ModelController.getInstance().getCellBehavioralModelController().getAvailableCellTypes();					
		
			
			// The following is used to calculate the x interval within which initialized cells are assigned to diffLevel[1] (proliferative)
			// The angle subtended by the proliferative belt can be calculated for the 2D case due to symmetry
			// The proliferative belt spans the distance as measured by a ruler	
			
			// Set differentiation levels of cells
			for(int i=0; i < standardCellEnsemble.size(); i++){
				
				UniversalCell actCell 				= standardCellEnsemble.get(i);
				if(cellTypes.length >= 2) actCell.getEpisimCellBehavioralModelObject().setCellType(cellTypes[1]);
				
				if(diffLevels.length > 2){
					if(((ApicalMeristemCenterBased3DModel)actCell.getEpisimBioMechanicalModelObject()).isWithinStemCellCone()){
							actCell.getEpisimCellBehavioralModelObject().setDiffLevel(diffLevels[1]);
					}
					else{
						actCell.getEpisimCellBehavioralModelObject().setDiffLevel(diffLevels[2]);
					}
				}
				
			}
		}
		
		// This part is the initial relaxation of the model, before the proper simulation starts
		private void initializeBiomechanics(ArrayList<UniversalCell> standardCellEnsemble){
			
			EpisimBiomechanicalModel biomech 		 = ModelController.getInstance().getBioMechanicalModelController().getNewEpisimBioMechanicalModelObject(null, null);
			ApicalMeristemCenterBased3DModelGP mechModelGP = (ApicalMeristemCenterBased3DModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
			
			if(biomech instanceof ApicalMeristemCenterBased3DModel){
				
				ApicalMeristemCenterBased3DModel cbBioMech = (ApicalMeristemCenterBased3DModel) biomech;			
				double cumulativeMigrationDist      = 0;
				
				do{
					cbBioMech.initialisationGlobalSimStep();
					cumulativeMigrationDist = getCumulativeMigrationDistance(standardCellEnsemble);
				//	System.out.println("Average migration:" + cumulativeMigrationDist/standardCellEnsemble.size());
				}
				while(standardCellEnsemble.size() > 0 && ((cumulativeMigrationDist / standardCellEnsemble.size()) > mechModelGP.getMinAverageMigrationMikron()));
				
			}
		}
		
		// Get migration of cells during relaxation
		private double getCumulativeMigrationDistance(ArrayList<UniversalCell> standardCellEnsemble){
			
			double cumulativeMigrationDistance = 0;
			
			for(int i = 0; i < standardCellEnsemble.size(); i++){
				ApicalMeristemCenterBased3DModel mechModel = ((ApicalMeristemCenterBased3DModel) standardCellEnsemble.get(i).getEpisimBioMechanicalModelObject());
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
			cellSize 		 = Math.max(cellSize, CELL_LENGTH);
						
			return loadedCells;
		}

		
		protected void initializeCellEnsembleBasedOnRandomAgeDistribution(ArrayList<UniversalCell> cellEnsemble){
			// This method has to be implemented but has nothing to do in this model
		}

		protected EpisimPortrayal getCellPortrayal() {
			ContinuousCellFieldPortrayal3D continuousPortrayal = new ContinuousCellFieldPortrayal3D("Apical Meristem");
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

