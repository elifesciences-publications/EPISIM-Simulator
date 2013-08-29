package episimbiomechanics.hexagonbased2d.singlesurface.tumormodel.chemokine;

import java.util.ArrayList;

import sim.app.episim.CellInspector;
import sim.app.episim.UniversalCell;
import sim.app.episim.model.biomechanics.hexagonbased.singlesurface.HexagonBasedMechanicalModel;
import sim.app.episim.model.biomechanics.hexagonbased.singlesurface.tumor.chemokine.HexagonBasedMechanicalModelCytokineTumorGP;
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
import ec.util.MersenneTwisterFast;
import episiminterfaces.EpisimCellType;
import episiminterfaces.EpisimPortrayal;


public class EpisimChemokineTumorModelInit extends BiomechanicalModelInitializer {
	
	MersenneTwisterFast random; 
	
	public EpisimChemokineTumorModelInit(){
		super();
		TissueController.getInstance().getTissueBorder().loadNoMembrane();
		MiscalleneousGlobalParameters.getInstance().setTypeColor(4);
		random = new MersenneTwisterFast(System.currentTimeMillis());
		HexagonBasedMechanicalModelCytokineTumorGP globalParameters = (HexagonBasedMechanicalModelCytokineTumorGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();		
		setInitialGlobalParametersValues(globalParameters);
	}
	
	public EpisimChemokineTumorModelInit(SimulationStateData simulationStateData){
		super(simulationStateData);
		random = new MersenneTwisterFast(System.currentTimeMillis());
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
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				double rn =random.nextDouble();
				double rn_secr = random.nextDouble();
				if(rn < cellDensity){
					UniversalCell cell = new UniversalCell(null, null, true);
					((HexagonBasedMechanicalModel) cell.getEpisimBioMechanicalModelObject()).setCellLocationInCellField(new Double2D(x, y));					
					if(x < sectionWidth){ // AL Cells
						if(rn_secr < al_sec_density){
							if(cellTypes.length >=3) cell.getEpisimCellBehavioralModelObject().setCellType(cellTypes[2]);
						}
						else{
							if(cellTypes.length >=2) cell.getEpisimCellBehavioralModelObject().setCellType(cellTypes[1]);
						}						
					}				
					else if(x < (2*sectionWidth)){ // IM Cells
						if(rn_secr < im_sec_density){
							if(cellTypes.length >=5) cell.getEpisimCellBehavioralModelObject().setCellType(cellTypes[4]);
						}
						else{
							if(cellTypes.length >=4) cell.getEpisimCellBehavioralModelObject().setCellType(cellTypes[3]);
						}
					}
					else if(x < width){ // IM Cells
						if(rn_secr < lm_sec_density){
							if(cellTypes.length >=7) cell.getEpisimCellBehavioralModelObject().setCellType(cellTypes[6]);
						}
						else{
							if(cellTypes.length >=6) cell.getEpisimCellBehavioralModelObject().setCellType(cellTypes[5]);
						}
					}					
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
