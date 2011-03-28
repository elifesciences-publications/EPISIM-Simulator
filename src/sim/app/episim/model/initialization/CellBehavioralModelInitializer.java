package sim.app.episim.model.initialization;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;

import ec.util.MersenneTwisterFast;
import episiminterfaces.EpisimCellType;
import episiminterfaces.EpisimDifferentiationLevel;

import sim.app.episim.UniversalCell;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.util.TysonRungeCuttaCalculator;


public class CellBehavioralModelInitializer {
	
	private File modeInitializationFile;
	
	public CellBehavioralModelInitializer(){
		this(null);
	}
	
	public CellBehavioralModelInitializer(File file){
		this.modeInitializationFile = file;
	}
	
	protected void initializeCellEnsemble(ArrayList<UniversalCell> cellEnsemble){
		if(this.modeInitializationFile == null) initializeCellEnsembleWithStandardValues(cellEnsemble);
		else initializeCellEnsembleWithFileValues(cellEnsemble);
	}
	
	private void initializeCellEnsembleWithStandardValues(ArrayList<UniversalCell> cellEnsemble){
		MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
		for(UniversalCell actCell : cellEnsemble){
			int cellCyclePos = random.nextInt(ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getCellCycleStem());
			
			//assign random age
			actCell.getEpisimCellBehavioralModelObject().setAge((double)(cellCyclePos));// somewhere in the stemcellcycle
			
			boolean tysonCellCycleAvailable = false;
			try{
				 Method m = ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getClass().getMethod("getK6", (Class<?>)null);
				 m = ModelController.getInstance().getCellBehavioralModelController().getClass().getMethod("getK4", (Class<?>)null);
				 tysonCellCycleAvailable = true;
			}
			catch(NoSuchMethodException e){ tysonCellCycleAvailable = false; }
			
			
			if(tysonCellCycleAvailable) TysonRungeCuttaCalculator.assignRandomCellcyleState(actCell.getEpisimCellBehavioralModelObject(), cellCyclePos);																																		// on
																																					
			actCell.getEpisimCellBehavioralModelObject().setDiffLevel(ModelController.getInstance().getCellBehavioralModelController().getDifferentiationLevelForOrdinal(EpisimDifferentiationLevel.STEMCELL));
			actCell.getEpisimCellBehavioralModelObject().setCellType(ModelController.getInstance().getCellBehavioralModelController().getCellTypeForOrdinal(EpisimCellType.KERATINOCYTE));
			actCell.getEpisimCellBehavioralModelObject().setIsAlive(true);			
		}
	}
	
	private void initializeCellEnsembleWithFileValues(ArrayList<UniversalCell> cellEnsemble){
		//TODO:implement this method
	}

}
