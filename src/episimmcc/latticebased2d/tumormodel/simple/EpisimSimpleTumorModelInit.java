package episimmcc.latticebased2d.tumormodel.simple;

import java.util.ArrayList;

import sim.app.episim.model.UniversalCell;
import sim.app.episim.model.biomechanics.latticebased2D.LatticeBased2DModel;
import sim.app.episim.model.biomechanics.latticebased2D.tumor.simple.LatticeBased2DModelSimpleTumorGP;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.controller.TissueController;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters;
import sim.app.episim.persistence.SimulationStateData;
import sim.app.episim.util.CellInspector;
import sim.app.episim.visualization.HexagonalCellGridPortrayal2D;
import sim.display.GUIState;
import sim.field.grid.ObjectGrid2D;
import sim.portrayal.Inspector;
import sim.portrayal.LocationWrapper;
import sim.util.Double2D;
import ec.util.MersenneTwisterFast;
import episiminterfaces.EpisimCellType;
import episiminterfaces.EpisimPortrayal;


public class EpisimSimpleTumorModelInit extends BiomechanicalModelInitializer {
	
	MersenneTwisterFast random; 
	
	public EpisimSimpleTumorModelInit(){
		super();
		TissueController.getInstance().getTissueBorder().loadNoMembrane();
		
		random = new MersenneTwisterFast(System.currentTimeMillis());
		LatticeBased2DModelSimpleTumorGP globalParameters = (LatticeBased2DModelSimpleTumorGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();		
		setInitialGlobalParametersValues(globalParameters);
	}
	
	public EpisimSimpleTumorModelInit(SimulationStateData simulationStateData){
		super(simulationStateData);
		random = new MersenneTwisterFast(System.currentTimeMillis());
	}
	
	protected ArrayList<UniversalCell> buildStandardInitialCellEnsemble() {
		ArrayList<UniversalCell> standardCellEnsemble = new ArrayList<UniversalCell>();
		LatticeBased2DModelSimpleTumorGP globalParameters = (LatticeBased2DModelSimpleTumorGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		
		
		
		int width = (int)globalParameters.getNumber_of_initially_occupied_columns();
		int height = (int)globalParameters.getNumber_of_rows();
		double cellDensity = globalParameters.getInitialCellDensityInPercent() / 100d;
		EpisimCellType[] cellTypes =ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getAvailableCellTypes();
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				double rn =random.nextDouble();
				if(rn < cellDensity){
					UniversalCell cell = new UniversalCell(null, null, true);
					((LatticeBased2DModel) cell.getEpisimBioMechanicalModelObject()).setCellLocationInCellField(new Double2D(x, y));
					if(cellTypes.length >=2) cell.getEpisimCellBehavioralModelObject().setCellType(cellTypes[0]);
					standardCellEnsemble.add(cell);
				}
			}
		}	
		addSekretionCellColony(standardCellEnsemble);
		return standardCellEnsemble;
	}
	
	
	private void setInitialGlobalParametersValues(LatticeBased2DModelSimpleTumorGP globalParameters){
		globalParameters.setCellDiameterInMikron(25);
		globalParameters.setNumber_of_initially_occupied_columns(4);
		globalParameters.setNumber_of_columns(54);
		globalParameters.setNumber_of_rows(92);
		globalParameters.setUseCellCellInteractionEnergy(false);		
		globalParameters.setInitialCellDensityInPercent(100);
	}
	private void addSekretionCellColony(ArrayList<UniversalCell> standardCellEnsemble){
		LatticeBased2DModelSimpleTumorGP globalParameters = (LatticeBased2DModelSimpleTumorGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		float xPos = (float)(globalParameters.getNumber_of_columns() /2d);
		int height = (int)globalParameters.getNumber_of_rows();
		EpisimCellType[] cellTypes =ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getAvailableCellTypes();
		double cellDensity = globalParameters.getInitialSecretionCellDensityInPercent() / 100d;
		double sineModulationFactor = (4*Math.PI/height);
		for(int y = 0; y < height; y++){
			double rn =random.nextDouble();
			if(rn < cellDensity){
				float delta = (float)(2d*Math.sin(sineModulationFactor*y));
				int x = Math.round(xPos+delta);
				UniversalCell cell = new UniversalCell(null, null, true);
				((LatticeBased2DModel) cell.getEpisimBioMechanicalModelObject()).setCellLocationInCellField(new Double2D(x, y));
				((ObjectGrid2D) ModelController.getInstance().getBioMechanicalModelController().getCellField()).field[x][y] = cell;
				
				if(cellTypes.length >=2) cell.getEpisimCellBehavioralModelObject().setCellType(cellTypes[1]);
				standardCellEnsemble.add(cell);
			}
		}	
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
