package sim.app.episim.tissue;

import java.io.File;


public class TissueController {
	
	
	
	private static TissueController instance;
	
	private TissueImporter importer;
	
	private TissueController(){
		importer = new TissueImporter();
	}
	
	
	public static synchronized TissueController getInstance(){
		if(instance == null) instance = new TissueController();
		return instance;
	}
	
	public TissueBorder getTissueBorder(){
		return TissueBorder.getInstance();
	}
	
	public void loadTissue(File file) throws IllegalArgumentException{
		if(file == null) throw new IllegalArgumentException(this.getClass().getName()+": File must not be null");
		else{
			importer.loadTissue(file);
		}
		
	}

}
