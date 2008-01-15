package sim.app.episim.model;

import java.util.concurrent.Semaphore;

import sim.app.episim.ExceptionDisplayer;
public class ModelController implements java.io.Serializable{
	
	private static Semaphore sem = new Semaphore (1);
	
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
	
	public EpisimCellStateModelGlobalParameters getEpisimStateModelGlobalParameters(){
	
		return BioChemicalModelController.getInstance().getEpisimStateModelGlobalParameters();
	}
	public EpisimMechanicalModelGlobalParameters getEpisimMechanicalModelGlobalParameters(){
		
		return BioMechanicalModelController.getInstance().getEpisimMechanicalModelGlobalParameters();
	}
	
	public EpisimCellStateModel getNewEpisimStateModelObject(){
		
		return BioChemicalModelController.getInstance().getNewEpisimStateModelObject();
	}

	public EpisimMechanicalModel getMechanicalModel(){
		
		return BioMechanicalModelController.getInstance().getEpisimMechanicalModel();
	}
	
}
