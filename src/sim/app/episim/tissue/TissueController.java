package sim.app.episim.tissue;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

import episimexceptions.NoEpidermalTissueAvailableException;

import sim.app.episim.model.visualization.CellEllipse;
import sim.app.episim.util.ClassLoaderChangeListener;
import sim.app.episim.util.GlobalClassLoader;
import sim.app.episim.visualization.*;


public class TissueController implements ClassLoaderChangeListener{
	
	
	
	private static TissueController instance;
	
	private TissueImporter importer;
	
	public interface TissueRegistrationListener{ public void newTissueWasRegistered();}
	
	private HashSet<TissueRegistrationListener> tissueRegistrationListener = new HashSet<TissueRegistrationListener>();
	
	private TissueController(){
		GlobalClassLoader.getInstance().addClassLoaderChangeListener(this);
		importer = new TissueImporter();
	}	

	
	public Epidermis getActEpidermalTissue() throws NoEpidermalTissueAvailableException{
		return TissueServer.getInstance().getActEpidermalTissue();
	}
	
	public TissueType getActTissue(){
		return TissueServer.getInstance().getActTissue();
	}
	
	public void registerTissue(TissueType tissue){
		TissueServer.getInstance().registerTissue(tissue);
		notifyAllTissueRegistrationListener();
	}
	
	public static synchronized TissueController getInstance(){
		if(instance == null) instance = new TissueController();
		return instance;
	}
	
		
	public boolean isTissueLoaded(){
		return (TissueBorder.getInstance().getImportedTissue() != null);
	}
	
	public ArrayList<CellEllipse> getImportedCells(){
		if(TissueBorder.getInstance().getImportedTissue() != null) return TissueBorder.getInstance().getImportedTissue().getCells();
		
		return null;
	}
	
	public TissueBorder getTissueBorder(){
		return TissueBorder.getInstance();
	}
	
	public void loadTissue(File file, boolean tissueVisualizationMode) throws IllegalArgumentException{
		if(file == null) throw new IllegalArgumentException(this.getClass().getName()+": File must not be null");
		else{
			ImportedTissue actImportedTissue = importer.loadTissue(file);
			if(actImportedTissue != null) TissueBorder.getInstance().setImportedTissue(actImportedTissue, tissueVisualizationMode);
		}		
	}
	
	public ImportedTissue getActImportedTissue(){  return TissueBorder.getInstance().getImportedTissue(); }
	
	public void addTissueRegistrationListener(TissueRegistrationListener listener){
		this.tissueRegistrationListener.add(listener);
	}
	
	public void removeTissueRegistrationListener(TissueRegistrationListener listener){
		this.tissueRegistrationListener.remove(listener);
	}
	
	private void notifyAllTissueRegistrationListener(){
		for(TissueRegistrationListener listener : this.tissueRegistrationListener){
			listener.newTissueWasRegistered();
		}
	}
	
	public void resetTissueSettings(){
		tissueRegistrationListener.clear();
		importer = new TissueImporter();
		TissueBorder.getInstance().resetTissueBorderSettings();
	}

   public void classLoaderHasChanged() {
		instance = null;
   }
}
