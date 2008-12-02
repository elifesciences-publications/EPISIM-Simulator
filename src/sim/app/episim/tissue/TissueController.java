package sim.app.episim.tissue;

import java.io.File;
import java.util.ArrayList;

import sim.app.episim.visualization.*;


public class TissueController {
	
	
	
	private static TissueController instance;
	
	private TissueImporter importer;
	
	private ImportedTissue actImportedTissue;
	
	
	
	private TissueController(){
		importer = new TissueImporter();
	}
	
	
	public static synchronized TissueController getInstance(){
		if(instance == null) instance = new TissueController();
		return instance;
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
			if(this.actImportedTissue != null) TissueBorder.getInstance().setImportedBasementMembrane(actImportedTissue);
		}
		
	}

}
