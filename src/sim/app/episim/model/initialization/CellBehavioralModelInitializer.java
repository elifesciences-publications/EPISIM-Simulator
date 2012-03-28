package sim.app.episim.model.initialization;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;

import ec.util.MersenneTwisterFast;
import episiminterfaces.EpisimCellBehavioralModel;
import episiminterfaces.EpisimCellType;
import episiminterfaces.EpisimDifferentiationLevel;

import sim.app.episim.UniversalCell;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.sbml.SbmlModelConnector;
import sim.app.episim.persistence.SimulationStateData;
import sim.app.episim.persistence.dataconvert.XmlEpisimCellBehavioralModel;
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
		MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
		for (UniversalCell actCell : cellEnsemble) {
			int cellCyclePos = random.nextInt(ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters()
					.getCellCycleStem());

			// assign random age
		/*	actCell.getEpisimCellBehavioralModelObject().setAge((double) (cellCyclePos));// somewhere
																							// in
																							// the
																							// stemcellcycle
			if (actCell.getEpisimCellBehavioralModelObject().getEpisimSbmlModelConnector() != null
					&& actCell.getEpisimCellBehavioralModelObject().getEpisimSbmlModelConnector() instanceof SbmlModelConnector) {
				((SbmlModelConnector) actCell.getEpisimCellBehavioralModelObject().getEpisimSbmlModelConnector())
						.initializeSBMLModelsWithCellAge(cellCyclePos);
			}*/
			boolean tysonCellCycleAvailable = false;
			try {
				Method m = ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getClass()
						.getMethod("getK6", new Class<?>[] {});
				m = ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getClass()
						.getMethod("getK4", new Class<?>[] {});
				tysonCellCycleAvailable = true;
			} catch (NoSuchMethodException e) {
				tysonCellCycleAvailable = false;
			}

			if (tysonCellCycleAvailable)
				TysonRungeCuttaCalculator.assignRandomCellcyleState(actCell.getEpisimCellBehavioralModelObject(), cellCyclePos); // on
			if(actCell.getEpisimCellBehavioralModelObject().getDiffLevel()==null){
				actCell.getEpisimCellBehavioralModelObject().setDiffLevel(
						ModelController.getInstance().getCellBehavioralModelController()
								.getDifferentiationLevelForOrdinal(EpisimDifferentiationLevel.STEMCELL));
			}
			if(actCell.getEpisimCellBehavioralModelObject().getCellType()==null){
				actCell.getEpisimCellBehavioralModelObject().setCellType(
					ModelController.getInstance().getCellBehavioralModelController()
							.getCellTypeForOrdinal(EpisimCellType.KERATINOCYTE));
			}
			actCell.getEpisimCellBehavioralModelObject().setIsAlive(true);
		}
	}

	protected void initializeCellEnsembleWithFileValues(ArrayList<UniversalCell> cellEnsemble) {
		for (UniversalCell actCell : cellEnsemble) {
			EpisimCellBehavioralModel cellBehave = actCell.getEpisimCellBehavioralModelObject();
			if (simulationStateData.getAlreadyLoadedXmlCellNewID(actCell.getID()) != null) {
				XmlEpisimCellBehavioralModel xCellBehave = simulationStateData.getAlreadyLoadedXmlCellNewID(actCell.getID())
						.getEpisimCellBehavioralModel();
				xCellBehave.copyValuesToTarget(cellBehave);

			}
		}
		
	}
}
