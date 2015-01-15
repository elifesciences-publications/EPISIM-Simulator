package episimbiomechanics.hexagonbased2d.singlesurface.tumormodel.chemokine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import sim.app.episim.CellInspector;
import sim.app.episim.UniversalCell;
import sim.app.episim.model.biomechanics.hexagonbased.HexagonBasedMechanicalModel;
import sim.app.episim.model.biomechanics.hexagonbased.tumor.chemokine.HexagonBasedMechanicalModelCytokineTumorGP;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters;
import sim.app.episim.model.visualization.HexagonalCellGridPortrayal2D;
import sim.app.episim.persistence.SimulationStateData;
import sim.app.episim.tissue.TissueController;
import sim.display.GUIState;
import sim.portrayal.Inspector;
import sim.portrayal.LocationWrapper;
import sim.util.Double2D;
import sim.util.Int2D;
import ec.util.MersenneTwisterFast;
import episiminterfaces.EpisimCellType;
import episiminterfaces.EpisimPortrayal;


public class EpisimChemokineTumorModelInit extends BiomechanicalModelInitializer {
	
	MersenneTwisterFast random; 
	
	public EpisimChemokineTumorModelInit(){
		super();
		TissueController.getInstance().getTissueBorder().loadNoMembrane();
		
		random = new MersenneTwisterFast(System.currentTimeMillis());
		HexagonBasedMechanicalModelCytokineTumorGP globalParameters = (HexagonBasedMechanicalModelCytokineTumorGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();		
		setInitialGlobalParametersValues(globalParameters);
	}
	
	public EpisimChemokineTumorModelInit(SimulationStateData simulationStateData){
		super(simulationStateData);
		TissueController.getInstance().getTissueBorder().loadNoMembrane();
		
		random = new MersenneTwisterFast(System.currentTimeMillis());
		HexagonBasedMechanicalModelCytokineTumorGP globalParameters = (HexagonBasedMechanicalModelCytokineTumorGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();		
		setInitialGlobalParametersValues(globalParameters);
	}

	
	protected ArrayList<UniversalCell> buildStandardInitialCellEnsemble() {
		ArrayList<UniversalCell> standardCellEnsemble = new ArrayList<UniversalCell>();
		HexagonBasedMechanicalModelCytokineTumorGP globalParameters = (HexagonBasedMechanicalModelCytokineTumorGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();

		int width = (int)globalParameters.getNumber_of_columns();
		double sectionWidth = Math.floor(((double)width) / 3d);
		int height = (int)globalParameters.getNumber_of_rows();
		double cellDensity = globalParameters.getInitialCellDensityInPercent() / 100d;
		double al_sec_density = globalParameters.getAL_SecretionCellDensityInPerc() / 100d;
		double im_sec_density = globalParameters.getIM_SecretionCellDensityInPerc() / 100d;
		double lm_sec_density = globalParameters.getLM_SecretionCellDensityInPerc() / 100d;
		EpisimCellType[] cellTypes =ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getAvailableCellTypes();
		
		
		double numberOfCellsAL = height*sectionWidth;
		double numberOfCellsIM = height*sectionWidth;
		double numberOfCellsLM = height*(width-2*sectionWidth);
		

		
		int numberOfScretoryCellsAL =(int)(numberOfCellsAL*al_sec_density);
		int numberOfScretoryCellsIM =(int)(numberOfCellsIM*im_sec_density);
		int numberOfScretoryCellsLM =(int)(numberOfCellsLM*lm_sec_density);
		
		
		
		Set<Int2D> secretoryPositionsAL = new HashSet<Int2D>();
		Set<Int2D> secretoryPositionsIM = new HashSet<Int2D>();
		Set<Int2D> secretoryPositionsLM = new HashSet<Int2D>();
		
		MersenneTwisterFast rand = new MersenneTwisterFast(globalParameters.getRandomSequenceSeed());
		
	
		while((secretoryPositionsAL.size()+secretoryPositionsIM.size()+secretoryPositionsLM.size())<
				(numberOfScretoryCellsAL+numberOfScretoryCellsIM+numberOfScretoryCellsLM)){
			int x = rand.nextInt(width);
			int y = rand.nextInt(height);
			Int2D cellPos = new Int2D(x, y);
			if(x < sectionWidth && secretoryPositionsAL.size() < numberOfScretoryCellsAL && !secretoryPositionsAL.contains(cellPos)){ // AL Cells
				secretoryPositionsAL.add(cellPos);
			}
			else if(x >= sectionWidth && x < (2*sectionWidth) && secretoryPositionsIM.size() < numberOfScretoryCellsIM && !secretoryPositionsIM.contains(cellPos)){ // IM Cells
				secretoryPositionsIM.add(cellPos);
			}
			else if(x >= (2*sectionWidth) && x < width && secretoryPositionsLM.size() < numberOfScretoryCellsLM && !secretoryPositionsLM.contains(cellPos)){ // LM Cells
				secretoryPositionsLM.add(cellPos);
			}			
		}
		
		// AL Secretory Cells
		for(Int2D cellPos:secretoryPositionsAL){
			UniversalCell cell = new UniversalCell(null, null, true);
			((HexagonBasedMechanicalModel) cell.getEpisimBioMechanicalModelObject()).setCellLocationInCellField(new Double2D(cellPos.x, cellPos.y));
			if(cellTypes.length >=2) cell.getEpisimCellBehavioralModelObject().setCellType(cellTypes[1]);
			standardCellEnsemble.add(cell);
		}
		// IM Secretory Cells
		for(Int2D cellPos:secretoryPositionsIM){
			UniversalCell cell = new UniversalCell(null, null, true);
			((HexagonBasedMechanicalModel) cell.getEpisimBioMechanicalModelObject()).setCellLocationInCellField(new Double2D(cellPos.x, cellPos.y));
			if(cellTypes.length >=3) cell.getEpisimCellBehavioralModelObject().setCellType(cellTypes[2]);
			standardCellEnsemble.add(cell);
		}
		// LM Secretory Cells
		for(Int2D cellPos:secretoryPositionsLM){
			UniversalCell cell = new UniversalCell(null, null, true);
			((HexagonBasedMechanicalModel) cell.getEpisimBioMechanicalModelObject()).setCellLocationInCellField(new Double2D(cellPos.x, cellPos.y));
			if(cellTypes.length >=4) cell.getEpisimCellBehavioralModelObject().setCellType(cellTypes[3]);
			standardCellEnsemble.add(cell);
		}
		
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				Int2D cellPos = new Int2D(x,y);
				if(!secretoryPositionsAL.contains(cellPos)&&!secretoryPositionsIM.contains(cellPos)&&!secretoryPositionsLM.contains(cellPos)){
					UniversalCell cell = new UniversalCell(null, null, true);
					((HexagonBasedMechanicalModel) cell.getEpisimBioMechanicalModelObject()).setCellLocationInCellField(new Double2D(cellPos.x, cellPos.y));
					cell.getEpisimCellBehavioralModelObject().setCellType(cellTypes[0]);
					standardCellEnsemble.add(cell);
				}				
			}
		}		
		return standardCellEnsemble;
	}
	
	
	private void setInitialGlobalParametersValues(HexagonBasedMechanicalModelCytokineTumorGP globalParameters){
		globalParameters.setCellDiameterInMikron(20);
		globalParameters.setWidthInMikron(1000);
		globalParameters.setHeightInMikron(1000);
		globalParameters.setUseCellCellInteractionEnergy(false);
		globalParameters.setInitialCellDensityInPercent(100);
		globalParameters.setAL_SecretionCellDensityInPerc(33);
		globalParameters.setIM_SecretionCellDensityInPerc(33);
		globalParameters.setLM_SecretionCellDensityInPerc(33);
		//ModelController.getInstance().getExtraCellularDiffusionController().getDiffusionModelGlobalParameters().setBoundaryConditionX(BoundaryCondition.NEUMANN);
		//ModelController.getInstance().getExtraCellularDiffusionController().getDiffusionModelGlobalParameters().setConstantFlowX(4*Math.pow(10, -23));
		//ModelController.getInstance().getExtraCellularDiffusionController().getDiffusionModelGlobalParameters().setBoundaryConditionY(BoundaryCondition.NEUMANN);
		//ModelController.getInstance().getExtraCellularDiffusionController().getDiffusionModelGlobalParameters().setConstantFlowY(4*Math.pow(10, -23));
		
	}
		
	
	protected void initializeCellEnsembleBasedOnRandomAgeDistribution(ArrayList<UniversalCell> cellEnsemble) {

		// is not needed in this model
	}

	protected EpisimPortrayal getCellPortrayal() {			   
		HexagonalCellGridPortrayal2D portrayal =  new HexagonalCellGridPortrayal2D(java.awt.Color.lightGray){
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
