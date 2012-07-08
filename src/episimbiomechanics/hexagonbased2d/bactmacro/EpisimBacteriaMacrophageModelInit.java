package episimbiomechanics.hexagonbased2d.bactmacro;

import java.util.ArrayList;

import sim.app.episim.CellInspector;
import sim.app.episim.UniversalCell;
import sim.app.episim.model.biomechanics.hexagonbased.HexagonBasedMechanicalModel;
import sim.app.episim.model.biomechanics.hexagonbased.HexagonBasedMechanicalModelGP;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters;
import sim.app.episim.model.visualization.HexagonalCellGridPortrayal2D;
import sim.app.episim.persistence.SimulationStateData;
import sim.app.episim.tissue.TissueController;
import sim.display.GUIState;
import sim.field.grid.ObjectGrid2D;
import sim.portrayal.Inspector;
import sim.portrayal.LocationWrapper;
import sim.util.Double2D;
import ec.util.MersenneTwisterFast;
import episiminterfaces.EpisimCellType;
import episiminterfaces.EpisimPortrayal;


public class EpisimBacteriaMacrophageModelInit  extends BiomechanicalModelInitializer {
	
	MersenneTwisterFast random; 
	
	public EpisimBacteriaMacrophageModelInit(){
		super();
		TissueController.getInstance().getTissueBorder().loadNoMembrane();
		MiscalleneousGlobalParameters.getInstance().setTypeColor(4);
		random = new MersenneTwisterFast(System.currentTimeMillis());
		HexagonBasedMechanicalModelGP globalParameters = (HexagonBasedMechanicalModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();		
		setInitialGlobalParametersValues(globalParameters);
	}
	
	public EpisimBacteriaMacrophageModelInit(SimulationStateData simulationStateData){
		super(simulationStateData);
		random = new MersenneTwisterFast(System.currentTimeMillis());
	}

	
	protected ArrayList<UniversalCell> buildStandardInitialCellEnsemble() {
		ArrayList<UniversalCell> standardCellEnsemble = new ArrayList<UniversalCell>();
		addMakrophageColony(standardCellEnsemble);
		addBacteriaColony(standardCellEnsemble);
		return standardCellEnsemble;
	}
	
	
	private void setInitialGlobalParametersValues(HexagonBasedMechanicalModelGP globalParameters){
		globalParameters.setCellDiameterMikron(20);
		globalParameters.setWidthInMikron(300);
		globalParameters.setHeightInMikron(300);
		globalParameters.setNumber_of_initially_occupied_columns(0);
		globalParameters.setInitialPositionWoundEdge_Mikron(Double.POSITIVE_INFINITY);
		globalParameters.setInitialCellDensityInPercent(20);		
	}
	
	private void addMakrophageColony(ArrayList<UniversalCell> standardCellEnsemble){
		HexagonBasedMechanicalModelGP globalParameters = (HexagonBasedMechanicalModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		int xPos = 0;//(float)(globalParameters.getNumber_of_columns() /2d);
		int yPos = (int)(globalParameters.getNumber_of_rows() /2d);	
		EpisimCellType[] cellTypes =ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getAvailableCellTypes();
		double cellDensity = globalParameters.getInitialSecretionCellDensityInPercent() / 100d;
		double rn =random.nextDouble();
			//if(rn < cellDensity){
				
				
		UniversalCell cell = new UniversalCell(null, null);
		((HexagonBasedMechanicalModel) cell.getEpisimBioMechanicalModelObject()).setCellLocationInCellField(new Double2D(xPos, yPos));
		((ObjectGrid2D) ModelController.getInstance().getBioMechanicalModelController().getCellField()).field[xPos][yPos] = cell;
			
		if(cellTypes.length >=3) cell.getEpisimCellBehavioralModelObject().setCellType(cellTypes[1]);
		standardCellEnsemble.add(cell);			
		
	}
	private void addBacteriaColony(ArrayList<UniversalCell> standardCellEnsemble){
		HexagonBasedMechanicalModelGP globalParameters = (HexagonBasedMechanicalModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		int xPos = (int)(globalParameters.getNumber_of_columns() - 1);
		int yPos = (int)(globalParameters.getNumber_of_rows() /2d);
	
		EpisimCellType[] cellTypes =ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getAvailableCellTypes();
		double cellDensity = globalParameters.getInitialSecretionCellDensityInPercent() / 100d;
		double rn =random.nextDouble();
			//if(rn < cellDensity){
				
				
		UniversalCell cell = new UniversalCell(null, null);
		((HexagonBasedMechanicalModel) cell.getEpisimBioMechanicalModelObject()).setCellLocationInCellField(new Double2D(xPos, yPos));
		((ObjectGrid2D) ModelController.getInstance().getBioMechanicalModelController().getCellField()).field[xPos][yPos] = cell;
			
		if(cellTypes.length >=3) cell.getEpisimCellBehavioralModelObject().setCellType(cellTypes[2]);
		standardCellEnsemble.add(cell);			
		
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
