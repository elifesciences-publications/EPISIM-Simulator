package sim.app.episim.tissue;

import java.io.File;
import java.util.ArrayList;

import episimexceptions.NoEpidermalTissueAvailableException;

import sim.app.episim.visualization.*;


public class TissueController {
	
	
	
	private static TissueController instance;
	
	private TissueImporter importer;
	
	private ImportedTissue actImportedTissue;
	
	
	
	private TissueController(){
		importer = new TissueImporter();
	}
	
	public Epidermis getActEpidermalTissue() throws NoEpidermalTissueAvailableException{
		return TissueServer.getInstance().getActEpidermalTissue();
	}
	
	public void registerTissue(TissueType tissue){
		TissueServer.getInstance().registerTissue(tissue);
	}
	
	public static synchronized TissueController getInstance(){
		if(instance == null) instance = new TissueController();
		return instance;
	}
	
	public int getTissueWidth(){
		if(actImportedTissue != null) return (int) actImportedTissue.getEpidermalWidth();
		
		return 0;
	}
	
	public int getTissueHeight(){
		if(actImportedTissue != null) return (int) actImportedTissue.getEpidermalHeight();
		
		return 0;
	}
	
	public boolean isTissueLoaded(){
		return (this.actImportedTissue != null);
	}
	
	public ArrayList<CellEllipse> getImportedCells(){
		if(this.actImportedTissue != null) return this.actImportedTissue.getCells();
		
		return null;
	}
	
	public TissueBorder getTissueBorder(){
		return TissueBorder.getInstance();
	}
	
	public void loadTissue(File file) throws IllegalArgumentException{
		if(file == null) throw new IllegalArgumentException(this.getClass().getName()+": File must not be null");
		else{
			actImportedTissue = importer.loadTissue(file);
			if(this.actImportedTissue != null) TissueBorder.getInstance().setImportedTissueBorder(actImportedTissue);
		}
		
	}

}
