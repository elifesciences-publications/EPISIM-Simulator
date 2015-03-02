package episimmcc.latticebased2d.bactmacro;

import java.util.ArrayList;

import sim.app.episim.model.UniversalCell;
import sim.app.episim.model.biomechanics.latticebased2D.LatticeBased2DModel;
import sim.app.episim.model.biomechanics.latticebased2D.LatticeBased2DModelGP;
import sim.app.episim.model.biomechanics.latticebased2D.bact.LatticeBased2DModelBactGP;
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


public class EpisimBacteriaMacrophageModelInit  extends BiomechanicalModelInitializer {
	
	MersenneTwisterFast random; 
	
	public EpisimBacteriaMacrophageModelInit(){
		super();
		TissueController.getInstance().getTissueBorder().loadNoMembrane();
		random = new MersenneTwisterFast(System.currentTimeMillis());
		LatticeBased2DModelGP globalParameters = (LatticeBased2DModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();		
		setInitialGlobalParametersValues(globalParameters);
	}
	
	public EpisimBacteriaMacrophageModelInit(SimulationStateData simulationStateData){
		super(simulationStateData);
		random = new MersenneTwisterFast(System.currentTimeMillis());
	}

	
	protected ArrayList<UniversalCell> buildStandardInitialCellEnsemble() {
		ArrayList<UniversalCell> standardCellEnsemble = new ArrayList<UniversalCell>();
		LatticeBased2DModelBactGP globalParameters = (LatticeBased2DModelBactGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		if(globalParameters.getAddErythrocyteColony()){
			addErythrocyteColony(standardCellEnsemble);
		}
		addMakrophageColony(standardCellEnsemble);
		addBacteriaColony(standardCellEnsemble);
		return standardCellEnsemble;
	}
	
	
	private void setInitialGlobalParametersValues(LatticeBased2DModelGP globalParameters){
		globalParameters.setCellDiameterInMikron(20);
		globalParameters.setWidthInMikron(200);
		globalParameters.setHeightInMikron(200);
		globalParameters.setInitialCellDensityInPercent(25);		
		globalParameters.setUseCellCellInteractionEnergy(false);		
	}
	
	private void addMakrophageColony(ArrayList<UniversalCell> standardCellEnsemble){
		LatticeBased2DModelGP globalParameters = (LatticeBased2DModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		int xPos = 0;//(float)(globalParameters.getNumber_of_columns() /2d);
		int yPos = (int)(globalParameters.getNumber_of_rows() /2d);
		if(globalParameters.getWidthInMikron() >250){
			xPos = (int)((globalParameters.getNumber_of_columns() /2d)-7);
		}
		EpisimCellType[] cellTypes =ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getAvailableCellTypes();
		
				
				
		UniversalCell cell = new UniversalCell(null, null, true);
		((LatticeBased2DModel) cell.getEpisimBioMechanicalModelObject()).setCellLocationInCellField(new Double2D(xPos, yPos));
		((ObjectGrid2D) ModelController.getInstance().getBioMechanicalModelController().getCellField()).field[xPos][yPos] = cell;
		//System.out.println("Macrophage: "+((HexagonBasedMechanicalModelSingleSurface) cell.getEpisimBioMechanicalModelObject()).getLocationInMikron());	
		if(cellTypes.length >=2) cell.getEpisimCellBehavioralModelObject().setCellType(cellTypes[0]);
		standardCellEnsemble.add(cell);			
		
	}
	private void addBacteriaColony(ArrayList<UniversalCell> standardCellEnsemble){
		LatticeBased2DModelGP globalParameters = (LatticeBased2DModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		int xPos = (int)(globalParameters.getNumber_of_columns() - 1);
		int yPos = (int)(globalParameters.getNumber_of_rows() /2d);
		if(globalParameters.getWidthInMikron() >250){
			xPos = (int)((globalParameters.getNumber_of_columns() /2d)+6);
		}
		EpisimCellType[] cellTypes =ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getAvailableCellTypes();
	
				
				
		UniversalCell cell = new UniversalCell(null, null, true);
		((LatticeBased2DModel) cell.getEpisimBioMechanicalModelObject()).setCellLocationInCellField(new Double2D(xPos, yPos));
		((ObjectGrid2D) ModelController.getInstance().getBioMechanicalModelController().getCellField()).field[xPos][yPos] = cell;			
		if(cellTypes.length >=2) cell.getEpisimCellBehavioralModelObject().setCellType(cellTypes[1]);
		standardCellEnsemble.add(cell);			
		
	}
	
	private void addErythrocyteColony(ArrayList<UniversalCell> standardCellEnsemble){
		LatticeBased2DModelGP globalParameters = (LatticeBased2DModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		int noCols = (int)(globalParameters.getNumber_of_columns());
		int noRows = (int)(globalParameters.getNumber_of_rows());
		EpisimCellType[] cellTypes =ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getAvailableCellTypes();
		double cellDensity = globalParameters.getInitialCellDensityInPercent()/100d;
		int erythrocyteCounter = 0;
		for(int y = 0;y<noRows;y++){
			for(int x = 0;x<noCols;x++){
				double rn =random.nextDouble();
				if(rn < cellDensity){				
					UniversalCell cell = new UniversalCell(null, null, true);
					((LatticeBased2DModel) cell.getEpisimBioMechanicalModelObject()).setCellLocationInCellField(new Double2D(x, y));
					((ObjectGrid2D) ModelController.getInstance().getBioMechanicalModelController().getCellField()).field[x][y] = cell;
						
					if(cellTypes.length >=3) cell.getEpisimCellBehavioralModelObject().setCellType(cellTypes[2]);
					standardCellEnsemble.add(cell);
					erythrocyteCounter++;
				}
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
