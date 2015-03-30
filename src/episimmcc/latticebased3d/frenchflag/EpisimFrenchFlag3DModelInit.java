package episimmcc.latticebased3d.frenchflag;

import java.util.ArrayList;

import sim.app.episim.model.UniversalCell;
import sim.app.episim.model.biomechanics.latticebased2d.LatticeBased2DModel;
import sim.app.episim.model.biomechanics.latticebased2d.LatticeBased2DModelGP;
import sim.app.episim.model.biomechanics.latticebased2d.tumor.chemokine.LatticeBased2DModelCytokineTumorGP;
import sim.app.episim.model.biomechanics.latticebased3d.LatticeBased3DModel;
import sim.app.episim.model.biomechanics.latticebased3d.LatticeBased3DModelGP;
import sim.app.episim.model.biomechanics.latticebased3d.LatticeCellField3D;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.controller.TissueController;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import sim.app.episim.persistence.SimulationStateData;
import sim.app.episim.util.CellInspector;
import sim.app.episim.visualization.threedim.LatticeCellFieldPortrayal3D;
import sim.app.episim.visualization.twodim.LatticeCellFieldPortrayal2D;
import sim.display.GUIState;
import sim.portrayal.Inspector;
import sim.portrayal.LocationWrapper;
import sim.util.Double2D;
import sim.util.Double3D;
import sim.util.Int2D;
import sim.util.Int3D;
import ec.util.MersenneTwisterFast;
import episiminterfaces.EpisimCellType;
import episiminterfaces.EpisimPortrayal;


public class EpisimFrenchFlag3DModelInit extends BiomechanicalModelInitializer {
	
	MersenneTwisterFast random; 
	
	public EpisimFrenchFlag3DModelInit(){
		super();
		TissueController.getInstance().getTissueBorder().loadNoMembrane();
		
		random = new MersenneTwisterFast(System.currentTimeMillis());
		LatticeBased3DModelGP globalParameters = (LatticeBased3DModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();		
		setInitialGlobalParametersValues(globalParameters);
	}
	
	public EpisimFrenchFlag3DModelInit(SimulationStateData simulationStateData){
		super(simulationStateData);
		TissueController.getInstance().getTissueBorder().loadNoMembrane();		
		random = new MersenneTwisterFast(System.currentTimeMillis());
		LatticeBased3DModelGP globalParameters = (LatticeBased3DModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();		
		setInitialGlobalParametersValues(globalParameters);
	}

	
	protected ArrayList<UniversalCell> buildStandardInitialCellEnsemble() {
		ArrayList<UniversalCell> standardCellEnsemble = new ArrayList<UniversalCell>();
		LatticeBased3DModelGP globalParameters = (LatticeBased3DModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();

		int width = (int)globalParameters.getNumber_of_columns();

		int height = (int)globalParameters.getNumber_of_rows();
		
		int length = (int)globalParameters.getNumber_of_layers();
		
		
		EpisimCellType[] cellTypes =ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getAvailableCellTypes();
		
		for(int z = 0; z < length; z++){
			for(int y = 0; y < height; y++){
				for(int x = 0; x < width; x++){
					
					Int3D cellPos = new Int3D(x,y, z);				
					UniversalCell cell = new UniversalCell(null, null, true);
					((LatticeBased3DModel) cell.getEpisimBioMechanicalModelObject()).setCellLocationInCellField(new Double3D(cellPos.x, cellPos.y, cellPos.z));
					
					if(x==0 && cellTypes.length>1) cell.getEpisimCellBehavioralModelObject().setCellType(cellTypes[1]);
					else if(x==(width-1) && cellTypes.length>2) cell.getEpisimCellBehavioralModelObject().setCellType(cellTypes[2]);
					else cell.getEpisimCellBehavioralModelObject().setCellType(cellTypes[0]);
					standardCellEnsemble.add(cell);								
				}
			}
		}
		return standardCellEnsemble;
	}
	
	
	private void setInitialGlobalParametersValues(LatticeBased3DModelGP globalParameters){
		globalParameters.setCellDiameterInMikron(10);
		globalParameters.setWidthInMikron(150);
		globalParameters.setHeightInMikron(150);
		globalParameters.setLengthInMikron(150);
		globalParameters.setUseCellCellInteractionEnergy(false);
		
		
	//	ModelController.getInstance().getExtraCellularDiffusionController()..getExtraCellularDiffusionInitializer()..getDiffusionModelGlobalParameters().setBoundaryConditionX(BoundaryCondition.NEUMANN);
	//	ModelController.getInstance().getExtraCellularDiffusionController().getDiffusionModelGlobalParameters().setConstantFlowX(4*Math.pow(10, -23));
	//	ModelController.getInstance().getExtraCellularDiffusionController().getDiffusionModelGlobalParameters().setBoundaryConditionY(BoundaryCondition.NEUMANN);
	//	ModelController.getInstance().getExtraCellularDiffusionController().getDiffusionModelGlobalParameters().setConstantFlowY(4*Math.pow(10, -23));
		
	}
		
	
	protected void initializeCellEnsembleBasedOnRandomAgeDistribution(ArrayList<UniversalCell> cellEnsemble) {

		// is not needed in this model
	}

	protected EpisimPortrayal getCellPortrayal() {			   
		Object cellField = ModelController.getInstance().getBioMechanicalModelController().getCellField();
	   if(cellField instanceof LatticeCellField3D){
   	 return ((LatticeCellField3D) cellField).getCellFieldPortrayal();
	   }
	   return null;
   }

	
	protected EpisimPortrayal[] getAdditionalPortrayalsCellForeground() {
		return new EpisimPortrayal[0];
	}
	
	protected EpisimPortrayal[] getAdditionalPortrayalsCellBackground() {		
		return  new EpisimPortrayal[0];
	}
}