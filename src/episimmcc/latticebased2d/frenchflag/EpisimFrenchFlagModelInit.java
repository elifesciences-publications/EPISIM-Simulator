package episimmcc.latticebased2d.frenchflag;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import sim.app.episim.model.UniversalCell;
import sim.app.episim.model.biomechanics.latticebased2d.LatticeBased2DModel;
import sim.app.episim.model.biomechanics.latticebased2d.LatticeBased2DModelGP;
import sim.app.episim.model.biomechanics.latticebased2d.tumor.chemokine.LatticeBased2DModelCytokineTumorGP;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.controller.TissueController;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import sim.app.episim.persistence.SimulationStateData;
import sim.app.episim.util.CellInspector;
import sim.app.episim.visualization.twodim.LatticeCellFieldPortrayal2D;
import sim.display.GUIState;
import sim.portrayal.Inspector;
import sim.portrayal.LocationWrapper;
import sim.util.Double2D;
import sim.util.Int2D;
import ec.util.MersenneTwisterFast;
import episiminterfaces.EpisimCellType;
import episiminterfaces.EpisimPortrayal;


public class EpisimFrenchFlagModelInit extends BiomechanicalModelInitializer {
	
	MersenneTwisterFast random; 
	
	public EpisimFrenchFlagModelInit(){
		super();
		TissueController.getInstance().getTissueBorder().loadNoMembrane();
		
		random = new MersenneTwisterFast(System.currentTimeMillis());
		LatticeBased2DModelGP globalParameters = (LatticeBased2DModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();		
		setInitialGlobalParametersValues(globalParameters);
	}
	
	public EpisimFrenchFlagModelInit(SimulationStateData simulationStateData){
		super(simulationStateData);
		TissueController.getInstance().getTissueBorder().loadNoMembrane();
		
		random = new MersenneTwisterFast(System.currentTimeMillis());
		LatticeBased2DModelCytokineTumorGP globalParameters = (LatticeBased2DModelCytokineTumorGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();		
		setInitialGlobalParametersValues(globalParameters);
	}

	
	protected ArrayList<UniversalCell> buildStandardInitialCellEnsemble() {
		ArrayList<UniversalCell> standardCellEnsemble = new ArrayList<UniversalCell>();
		LatticeBased2DModelGP globalParameters = (LatticeBased2DModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();

		int width = (int)globalParameters.getNumber_of_columns();

		int height = (int)globalParameters.getNumber_of_rows();
		double cellDensity = globalParameters.getInitialCellDensityInPercent() / 100d;
		
		EpisimCellType[] cellTypes =ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getAvailableCellTypes();
		

		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				
				Int2D cellPos = new Int2D(x,y);				
				UniversalCell cell = new UniversalCell(null, null, true);
				((LatticeBased2DModel) cell.getEpisimBioMechanicalModelObject()).setCellLocationInCellField(new Double2D(cellPos.x, cellPos.y));
				
				if(x==0 && cellTypes.length>1) cell.getEpisimCellBehavioralModelObject().setCellType(cellTypes[1]);
				else if(x==(width-1) && cellTypes.length>2) cell.getEpisimCellBehavioralModelObject().setCellType(cellTypes[2]);
				else cell.getEpisimCellBehavioralModelObject().setCellType(cellTypes[0]);
				standardCellEnsemble.add(cell);								
			}
		}		
		return standardCellEnsemble;
	}
	
	
	private void setInitialGlobalParametersValues(LatticeBased2DModelGP globalParameters){
		globalParameters.setCellDiameterInMikron(10);
		globalParameters.setWidthInMikron(500);
		globalParameters.setHeightInMikron(500);
		globalParameters.setUseCellCellInteractionEnergy(false);
		globalParameters.setInitialCellDensityInPercent(100);
		
	//	ModelController.getInstance().getExtraCellularDiffusionController()..getExtraCellularDiffusionInitializer()..getDiffusionModelGlobalParameters().setBoundaryConditionX(BoundaryCondition.NEUMANN);
	//	ModelController.getInstance().getExtraCellularDiffusionController().getDiffusionModelGlobalParameters().setConstantFlowX(4*Math.pow(10, -23));
	//	ModelController.getInstance().getExtraCellularDiffusionController().getDiffusionModelGlobalParameters().setBoundaryConditionY(BoundaryCondition.NEUMANN);
	//	ModelController.getInstance().getExtraCellularDiffusionController().getDiffusionModelGlobalParameters().setConstantFlowY(4*Math.pow(10, -23));
		
	}
		
	
	protected void initializeCellEnsembleBasedOnRandomAgeDistribution(ArrayList<UniversalCell> cellEnsemble) {

		// is not needed in this model
	}

	protected EpisimPortrayal getCellPortrayal() {			   
		LatticeCellFieldPortrayal2D portrayal =  new LatticeCellFieldPortrayal2D(java.awt.Color.lightGray){
			public Inspector getInspector(LocationWrapper wrapper, GUIState state) {
			// make the inspector
				return new CellInspector(super.getInspector(wrapper, state), wrapper, state);
			}
		};
		portrayal.setField(ModelController.getInstance().getBioMechanicalModelController().getCellField());
		return portrayal;
   }

	
	protected EpisimPortrayal[] getAdditionalPortrayalsCellForeground() {
		return new EpisimPortrayal[0];
	}
	
	protected EpisimPortrayal[] getAdditionalPortrayalsCellBackground() {		
		return  new EpisimPortrayal[0];
	}
}