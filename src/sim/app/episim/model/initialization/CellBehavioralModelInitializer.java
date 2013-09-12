package sim.app.episim.model.initialization;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;

import ec.util.MersenneTwisterFast;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.EpisimCellBehavioralModel;
import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import episiminterfaces.EpisimCellType;
import episiminterfaces.EpisimDifferentiationLevel;

import sim.app.episim.EpisimLogger;
import sim.app.episim.EpisimProperties;
import sim.app.episim.UniversalCell;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters;
import sim.app.episim.model.sbml.SbmlModelConnector;
import sim.app.episim.persistence.SimulationStateData;
import sim.app.episim.persistence.dataconvert.XmlEpisimCellBehavioralModel;
import sim.app.episim.tissue.TissueController;
import sim.app.episim.util.TysonRungeCuttaCalculator;

public class CellBehavioralModelInitializer {

	private SimulationStateData simulationStateData;

	public CellBehavioralModelInitializer() {
		this(null);
	}

	public CellBehavioralModelInitializer(SimulationStateData simStateData) {
		this.simulationStateData = simStateData;
	}

	protected void initializeCellEnsemble(ArrayList<UniversalCell> cellEnsemble) {
		if (this.simulationStateData == null)
			initializeCellEnsembleWithStandardValues(cellEnsemble);
		else
			initializeCellEnsembleWithFileValues(cellEnsemble);
	}

	private void initializeCellEnsembleWithStandardValues(ArrayList<UniversalCell> cellEnsemble) {
		boolean randomAgeInit = EpisimProperties.getProperty(EpisimProperties.SIMULATOR_RANDOM_CELL_AGE_INIT) != null &&
				EpisimProperties.getProperty(EpisimProperties.SIMULATOR_RANDOM_CELL_AGE_INIT).equals(EpisimProperties.ON);
		MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
		int cellCyclePos =0;		
		int cellCycleStem = 1;
		for (UniversalCell actCell : cellEnsemble) {
			if(randomAgeInit){
				Object result = null;
				try {
					Method m = ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getClass().getMethod("getCellCycleStem", new Class<?>[]{});
					result =m.invoke(ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters(), new Object[0]);
					
				} catch (Exception e1) {
					try {
						Method m = ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getClass().getMethod("getCellCycle", new Class<?>[]{});
						result =m.invoke(ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters(), new Object[0]);
						
					} catch (Exception e2) {
						try {
							Method m = ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getClass().getMethod("getCellCycleBurst", new Class<?>[]{});
							result =m.invoke(ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters(), new Object[0]);
							
						} catch (Exception e3) {}
					}
				}
				if(result != null){
					if(result instanceof Integer){
						cellCycleStem = ((Integer) result).intValue();
 					}
					else if(result instanceof Double){
						cellCycleStem = (int)((Double) result).doubleValue();
					}
				}
				if(cellCycleStem ==0)cellCycleStem=1;
				
				cellCyclePos = random.nextInt(cellCycleStem);
				// assign random age
				actCell.getEpisimCellBehavioralModelObject().setAge((double) (cellCyclePos));// somewhere in the stemcellcycle
				
				if (actCell.getEpisimCellBehavioralModelObject().getEpisimSbmlModelConnector() != null
						&& actCell.getEpisimCellBehavioralModelObject().getEpisimSbmlModelConnector() instanceof SbmlModelConnector) {
					((SbmlModelConnector) actCell.getEpisimCellBehavioralModelObject().getEpisimSbmlModelConnector())
							.initializeSBMLModelsWithCellAge(cellCyclePos);
				}
			}
			boolean tysonCellCycleAvailable = false;
			try {
				Method m = ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getClass().getMethod("getT_k6", new Class<?>[]{});
				m = ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getClass().getMethod("getT_k4", new Class<?>[]{});
				tysonCellCycleAvailable = true;
			} catch (NoSuchMethodException e) {
				tysonCellCycleAvailable = false;
			}

			if (tysonCellCycleAvailable &&randomAgeInit){ 
				boolean available =TysonRungeCuttaCalculator.assignRandomCellcyleState(actCell.getEpisimCellBehavioralModelObject(), cellCyclePos); // on
				if(!available){
					EpisimLogger.getInstance().logInfo("Tyson Cell Cycle not available!");
				}
			}
			if(actCell.getEpisimCellBehavioralModelObject().getDiffLevel()==null){
				actCell.assignDefaultDiffLevel();
			}
			if(actCell.getEpisimCellBehavioralModelObject().getCellType()==null){
				actCell.assignDefaultCellType();
			}
			actCell.getEpisimCellBehavioralModelObject().setIsAlive(true);
		}
	}
	
	protected void initializeSampleCell(UniversalCell sampleCell){
		EpisimCellBehavioralModel cellBehave = sampleCell.getEpisimCellBehavioralModelObject();
		if (simulationStateData.getAlreadyLoadedXmlCellNewID(sampleCell.getID()) != null) {
			XmlEpisimCellBehavioralModel xCellBehave = simulationStateData.getAlreadyLoadedXmlCellNewID(sampleCell.getID())
					.getEpisimCellBehavioralModel();
			xCellBehave.copyValuesToTarget(cellBehave);
			cellBehave.setId(sampleCell.getID());
		}
	}
	
	protected void initializeCellEnsembleWithFileValues(ArrayList<UniversalCell> cellEnsemble) {
		EpisimCellBehavioralModelGlobalParameters globalBehave = ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters();
		if(simulationStateData.getEpisimCellBehavioralModelGlobalParameters() != null)simulationStateData.getEpisimCellBehavioralModelGlobalParameters().copyValuesToTarget(globalBehave);
		for (UniversalCell actCell : cellEnsemble) {
			EpisimCellBehavioralModel cellBehave = actCell.getEpisimCellBehavioralModelObject();
			if (simulationStateData.getAlreadyLoadedXmlCellNewID(actCell.getID()) != null) {
				XmlEpisimCellBehavioralModel xCellBehave = simulationStateData.getAlreadyLoadedXmlCellNewID(actCell.getID())
						.getEpisimCellBehavioralModel();
				xCellBehave.copyValuesToTarget(cellBehave);
				cellBehave.setId(actCell.getID());
			}
		}
		
	}
}
