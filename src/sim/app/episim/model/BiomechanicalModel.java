package sim.app.episim.model;



import java.util.ArrayList;
import java.util.List;

import episiminterfaces.EpisimMechanicalModel;
import episiminterfaces.EpisimMechanicalModelGlobalParameters;

import sim.app.episim.snapshot.SnapshotListener;
import sim.app.episim.snapshot.SnapshotObject;
import sim.app.episim.snapshot.SnapshotWriter;
import sim.app.episim.util.ObjectManipulations;




public class BiomechanicalModel implements java.io.Serializable, SnapshotListener{
		
	/**
    * 
    */
   private static final long serialVersionUID = 512640154196012852L;
	private MechanicalModelGlobalParameters actParametersObject;
	private EpisimMechanicalModelGlobalParameters resetParametersObject;
	
	
	
	public BiomechanicalModel(){
		
		actParametersObject = new MechanicalModelGlobalParameters();
		resetParametersObject = new MechanicalModelGlobalParameters();
		SnapshotWriter.getInstance().addSnapshotListener(this);
		
	}
	
	
	public void reloadMechanicalModelGlobalParametersObject(EpisimMechanicalModelGlobalParameters parametersObject){
		this.resetParametersObject = parametersObject;
		ObjectManipulations.resetInitialGlobalValues(actParametersObject, resetParametersObject);
	}
	
	
	
	public EpisimMechanicalModel getEpisimMechanicalModel() {
		return new EpisimMechanicalModel(){};
	}
	
	public EpisimMechanicalModelGlobalParameters getEpisimMechanicalModelGlobalParameters() {
		return actParametersObject;
	}
	
	
	public void resetInitialGlobalValues(){
		ObjectManipulations.resetInitialGlobalValues(actParametersObject, resetParametersObject);
	}
	
	

	


	public List<SnapshotObject> collectSnapshotObjects() {
		List<SnapshotObject> list = new ArrayList<SnapshotObject>();
		list.add(new SnapshotObject(SnapshotObject.MECHANICALMODELGLOBALPARAMETERS, this.actParametersObject));
		return list;
	}
   
   
}