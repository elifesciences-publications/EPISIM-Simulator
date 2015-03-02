package episimmcc.centerbased3d.fisheye;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;

import javax.vecmath.Point3d;

import sim.app.episim.EpisimExceptionHandler;
import sim.app.episim.model.AbstractCell;
import sim.app.episim.model.UniversalCell;
import sim.app.episim.model.biomechanics.centerbased3D.fisheye.DummyCell;
import sim.app.episim.model.biomechanics.centerbased3D.fisheye.FishEyeCenterBased3DModel;
import sim.app.episim.model.biomechanics.centerbased3D.fisheye.FishEyeCenterBased3DModelGP;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters.MiscalleneousGlobalParameters3D;
import sim.app.episim.persistence.SimulationStateData;
import sim.app.episim.tissue.TissueController;
import sim.app.episim.util.GenericBag;
import sim.app.episim.util.Icosahedron;
import sim.app.episim.visualization.ContinuousUniversalCellPortrayal3D;
import sim.util.Double3D;
import episiminterfaces.EpisimBiomechanicalModel;
import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import episiminterfaces.EpisimDifferentiationLevel;
import episiminterfaces.EpisimPortrayal;


public class FishEyeCenterBasedMechModelInit extends BiomechanicalModelInitializer {

	SimulationStateData simulationStateData = null;
	private static double CELL_WIDTH=0;
	private static double CELL_HEIGHT=0;
	private static double CELL_LENGTH=0;

	public FishEyeCenterBasedMechModelInit() {
		super();
		TissueController.getInstance().getTissueBorder().loadNoMembrane();
		MiscalleneousGlobalParameters param = MiscalleneousGlobalParameters.getInstance();
		if(param instanceof MiscalleneousGlobalParameters3D){
			((MiscalleneousGlobalParameters3D)param).setStandardMembrane_2_Dim_Gauss(false);
			((MiscalleneousGlobalParameters3D)param).setOptimizedGraphics(false);
		}
	}

	public FishEyeCenterBasedMechModelInit(SimulationStateData simulationStateData) {
		super(simulationStateData);
		this.simulationStateData = simulationStateData;
	}

	private final double depthFrac(double y)// depth of the position in the rete ridge in percent
	{
		double depthPosition = ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().getBasalAmplitude_mikron()-y;		
		return depthPosition < 0 ? 0: (depthPosition/ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().getBasalAmplitude_mikron());
	}
	
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

		FishEyeCenterBased3DModelGP mechModelGP = (FishEyeCenterBased3DModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		mechModelGP.setInnerEyeRadius(mechModelGP.getInitialInnerEyeRadius());
		
		Point3d fishEyeCenter = mechModelGP.getInnerEyeCenter();
		double cellSize = Math.max(CELL_WIDTH, CELL_HEIGHT);
		cellSize = Math.max(cellSize, CELL_LENGTH);
		
		
		HashSet<Point3d> existingCoordinates = new HashSet<Point3d>();
		
		//	double radius2 = radius*Math.cos((Math.PI/2d - (theta/2d)));
		//	double angleIncrement2 = Math.acos(((Math.pow(radius2, 2)+Math.pow(radius2, 2)-Math.pow(cellSize, 2))/(2d*radius2*radius2)));
		Point3d previousPoint =null;
	/*		for(double phi = 0; phi <= Math.PI ; phi+=angleIncrement){				
				
				for(double theta =  Math.PI; theta >= Math.PI/2d; theta-=angleIncrement){
				
				double z = radius*Math.sin(theta)*Math.cos(phi);
				double x = radius*Math.sin(theta)*Math.sin(phi);
				double y = radius*Math.cos(theta);*/
			double radius = mechModelGP.getInitialInnerEyeRadius();
			Icosahedron ico = new Icosahedron(5);
			int ignoredCells = 0;
		 for (int i = 0; i < ico.getVertexList().size(); i+=3 ) {
				 double x = ico.getVertexList().get(i) * radius;
				 double y = ico.getVertexList().get(i+1) * radius;
				 double z = ico.getVertexList().get(i+2) * radius;
				
				 Point3d newPos = new Point3d(fishEyeCenter.x + x, fishEyeCenter.y + y, fishEyeCenter.z + z);
				if(!existingCoordinates.contains(newPos) && newPos.x >= fishEyeCenter.x && (previousPoint == null || previousPoint.distance(newPos)>=cellSize)){
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
				//	}
				}
				else ignoredCells++;
				
		//	}
		}
		initializeBiomechanics(standardCellEnsemble);
		setDiffLevels(standardCellEnsemble, cellSize);
		FishEyeCenterBased3DModel.setDummyCellSize(cellSize);
		 
		System.out.println("No of stem cells: " + standardCellEnsemble.size()+ "    Cells Ingnored: " + ignoredCells);
		return standardCellEnsemble;
	}
	
	private void setDiffLevels(ArrayList<UniversalCell> standardCellEnsemble, double cellSize){
		FishEyeCenterBased3DModelGP mechModelGP = (FishEyeCenterBased3DModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		EpisimDifferentiationLevel[] diffLevels = ModelController.getInstance().getCellBehavioralModelController().getAvailableDifferentiationLevels();
		double prolifBeltSize = mechModelGP.getProlifCompWidthMikron();
		double radius = mechModelGP.getInitialInnerEyeRadius();
		double angleIncrement = Math.acos(((Math.pow(radius, 2)+Math.pow(radius, 2)-Math.pow(prolifBeltSize, 2))/(2d*radius*radius)));
		double xDelta = radius*Math.sin(Math.PI/2d)*Math.sin(angleIncrement);
		for(int i=0; i < standardCellEnsemble.size(); i++){
			
			UniversalCell actCell = standardCellEnsemble.get(i);
			EpisimBiomechanicalModel biomech =  actCell.getEpisimBioMechanicalModelObject();			
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
	private void initializeBiomechanics(ArrayList<UniversalCell> standardCellEnsemble){
		EpisimBiomechanicalModel biomech =  ModelController.getInstance().getBioMechanicalModelController().getNewEpisimBioMechanicalModelObject(null);
		FishEyeCenterBased3DModelGP mechModelGP = (FishEyeCenterBased3DModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		if(biomech instanceof FishEyeCenterBased3DModel){
			FishEyeCenterBased3DModel cbBioMech = (FishEyeCenterBased3DModel) biomech;			
			double cumulativeMigrationDist=0;
			do{
				cbBioMech.initialisationGlobalSimStep();
				cumulativeMigrationDist = getCumulativeMigrationDistance(standardCellEnsemble);	
			}
			while(standardCellEnsemble.size() > 0 && ((cumulativeMigrationDist / standardCellEnsemble.size()) >mechModelGP.getInitMinAverageMigration()));
			
		}
	}
	
	private double getCumulativeMigrationDistance(ArrayList<UniversalCell> standardCellEnsemble){
		double cumulativeMigrationDistance = 0;
		for(int i = 0; i < standardCellEnsemble.size(); i++){
			FishEyeCenterBased3DModel mechModel = ((FishEyeCenterBased3DModel) standardCellEnsemble.get(i).getEpisimBioMechanicalModelObject());
			cumulativeMigrationDistance += mechModel.getMigrationDistPerSimStep();
		}
		return cumulativeMigrationDistance;
	}
	
	

	protected ArrayList<UniversalCell> buildInitialCellEnsemble() {
		ArrayList<UniversalCell> loadedCells = super.buildInitialCellEnsemble();	
		return loadedCells;
	}

	
	protected void initializeCellEnsembleBasedOnRandomAgeDistribution(ArrayList<UniversalCell> cellEnsemble){
		// This method has to be implemented but has nothing to do in this model
	}

	protected EpisimPortrayal getCellPortrayal() {
		ContinuousUniversalCellPortrayal3D continuousPortrayal = new ContinuousUniversalCellPortrayal3D("Fish Eye");
		continuousPortrayal.setField(ModelController.getInstance().getBioMechanicalModelController().getCellField());
		return continuousPortrayal;
	}

	protected EpisimPortrayal[] getAdditionalPortrayalsCellForeground() {
		ContinuousUniversalCellPortrayal3D continuousPortrayal = new ContinuousUniversalCellPortrayal3D("Dummy Cells");
		continuousPortrayal.setField(FishEyeCenterBased3DModel.getDummyCellField());
		
		return new EpisimPortrayal[]{continuousPortrayal};
	}

	protected EpisimPortrayal[] getAdditionalPortrayalsCellBackground() {
		return new EpisimPortrayal[0];
	}

}
