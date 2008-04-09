package sim.app.episim.model;

import java.util.concurrent.Semaphore;

import episiminterfaces.EpisimCellDiffModel;
import episiminterfaces.EpisimCellDiffModelGlobalParameters;
import episiminterfaces.EpisimMechanicalModel;
import episiminterfaces.EpisimMechanicalModelGlobalParameters;

import sim.app.episim.ExceptionDisplayer;
public class ModelController implements java.io.Serializable{
	
	private static Semaphore sem = new Semaphore (1);
	
	private boolean modelOpened = false;
	
	private boolean simulationStartedOnce = false;
	
	private static ModelController instance;
	private ModelController(){}
	
	
	public static ModelController getInstance(){
		
		try{
		   sem.acquire();
	      if(instance == null) instance = new ModelController();
			sem.release();
		}
      catch (InterruptedException e){
	      ExceptionDisplayer.getInstance().displayException(e);
      }
		return instance;
	}
	
	public EpisimCellDiffModelGlobalParameters getEpisimCellDiffModelGlobalParameters(){
	
		return BioChemicalModelController.getInstance().getEpisimCellDiffModelGlobalParameters();
	}
	public EpisimMechanicalModelGlobalParameters getEpisimMechanicalModelGlobalParameters(){
		
		return BioMechanicalModelController.getInstance().getEpisimMechanicalModelGlobalParameters();
	}
	
	public EpisimCellDiffModel getNewEpisimStateModelObject(){
		
		return BioChemicalModelController.getInstance().getNewEpisimCellDiffModelObject();
	}

	public EpisimMechanicalModel getMechanicalModel(){
		
		return BioMechanicalModelController.getInstance().getEpisimMechanicalModel();
	}
	
	public BioMechanicalModelController getBioMechanicalModelController(){ return BioMechanicalModelController.getInstance();}
	public BioChemicalModelController getBioChemicalModelController() { return BioChemicalModelController.getInstance();}

	
   public boolean isModelOpened() {
   
   	return modelOpened;
   }

	
   public void setModelOpened(boolean modelOpened) {
   
   	this.modelOpened = modelOpened;
   }
   
   public void setSimulationStartedOnce(boolean val){ this.simulationStartedOnce = val;}
   
   public boolean isSimulationStartedOnce(){ return this.simulationStartedOnce;}
	
}
