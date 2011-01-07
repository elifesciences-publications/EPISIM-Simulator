package sim.app.episim.model.controller;



import java.util.ArrayList;
import java.util.List;

import episimbiomechanics.EpisimModelConnector;
import episimbiomechanics.centerbased.EpisimCenterBasedModelConnector;
import episiminterfaces.EpisimBioMechanicalModel;
import episiminterfaces.EpisimBioMechanicalModelGlobalParameters;

import sim.app.episim.AbstractCell;
import sim.app.episim.model.biomechanics.centerbased.CenterBasedMechanicalModel;
import sim.app.episim.model.biomechanics.centerbased.CenterBasedMechanicalModelGlobalParameters;
import sim.app.episim.snapshot.SnapshotListener;
import sim.app.episim.snapshot.SnapshotObject;
import sim.app.episim.snapshot.SnapshotWriter;
import sim.app.episim.util.ObjectManipulations;




public class BiomechanicalModel implements java.io.Serializable, SnapshotListener{
		
	private static final long serialVersionUID = 512640154196012852L;
	private CenterBasedMechanicalModelGlobalParameters actParametersObject;
	private EpisimBioMechanicalModelGlobalParameters resetParametersObject;
	
	
	
	public BiomechanicalModel(){		
		actParametersObject = new CenterBasedMechanicalModelGlobalParameters();
		resetParametersObject = new CenterBasedMechanicalModelGlobalParameters();
		SnapshotWriter.getInstance().addSnapshotListener(this);		
	}	
	
	public void reloadMechanicalModelGlobalParametersObject(EpisimBioMechanicalModelGlobalParameters parametersObject){
		this.resetParametersObject = parametersObject;
		ObjectManipulations.resetInitialGlobalValues(actParametersObject, resetParametersObject);
	}		
	
	
	public EpisimBioMechanicalModel getEpisimNewMechanicalModelObject() {
		return new CenterBasedMechanicalModel();
	}
	
	public EpisimBioMechanicalModel getNewEpisimMechanicalModelObject(AbstractCell cell) {
		return new CenterBasedMechanicalModel(cell);
	}
	
	public EpisimModelConnector getEpisimModelConnector() {
		return new EpisimCenterBasedModelConnector();
	}
	
	public EpisimBioMechanicalModelGlobalParameters getEpisimMechanicalModelGlobalParameters() {
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