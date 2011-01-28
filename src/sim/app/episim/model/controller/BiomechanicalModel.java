package sim.app.episim.model.controller;



import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import episimbiomechanics.EpisimModelConnector;
import episimexceptions.ModelCompatibilityException;
import episiminterfaces.EpisimBioMechanicalModel;
import episiminterfaces.EpisimBioMechanicalModelGlobalParameters;


import sim.app.episim.AbstractCell;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.UniversalCell;
import sim.app.episim.model.initialization.AbstractBiomechanicalModelInitializer;
import sim.app.episim.snapshot.SnapshotListener;
import sim.app.episim.snapshot.SnapshotObject;
import sim.app.episim.snapshot.SnapshotWriter;
import sim.app.episim.util.ObjectManipulations;




public class BiomechanicalModel implements java.io.Serializable, SnapshotListener{
		
	private static final long serialVersionUID = 512640154196012852L;
	private EpisimBioMechanicalModelGlobalParameters actParametersObject;
	private EpisimBioMechanicalModelGlobalParameters resetParametersObject;
	private Class<? extends EpisimBioMechanicalModel> biomechanicalModelClass;
	private Class<? extends EpisimModelConnector> modelConnectorClass;	
	
	
	public BiomechanicalModel(Class<? extends EpisimBioMechanicalModel> biomechanicalModelClass, Class<? extends EpisimModelConnector> modelConnectorClass,
			EpisimBioMechanicalModelGlobalParameters actParametersObject) throws ModelCompatibilityException{
		this.biomechanicalModelClass = biomechanicalModelClass;
		this.modelConnectorClass = modelConnectorClass;
		

		if(actParametersObject != null){
	        this.actParametersObject= actParametersObject;
	        this.resetParametersObject = ObjectManipulations.cloneObject(actParametersObject);
	        SnapshotWriter.getInstance().addSnapshotListener(this);
	        
		}      
		else throw new ModelCompatibilityException("No compatible EpisimCellBehavioralModelGlobalParameters-Object!");	
	}	
	
	public void reloadMechanicalModelGlobalParametersObject(EpisimBioMechanicalModelGlobalParameters parametersObject){
		this.resetParametersObject = parametersObject;
		ObjectManipulations.resetInitialGlobalValues(actParametersObject, resetParametersObject);
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
	public void test(String... strings){
		
	}
	
	public EpisimBioMechanicalModel getNewEpisimBiomechanicalModelObject(AbstractCell cell) {
		EpisimBioMechanicalModel biomechanicalModel = null;
		if(this.biomechanicalModelClass !=null)
	      try{
	      	Constructor<? extends EpisimBioMechanicalModel> constructor = this.biomechanicalModelClass.getConstructor(new Class[]{AbstractCell.class});
	      	biomechanicalModel = constructor.newInstance(new Object[]{cell});
	         return biomechanicalModel;
         }
         catch (InstantiationException e){
         	ExceptionDisplayer.getInstance().displayException(e);
         	return null;
         }
         catch (IllegalAccessException e){
         	ExceptionDisplayer.getInstance().displayException(e);
         	return null;
         }
         catch (SecurityException e){
         	ExceptionDisplayer.getInstance().displayException(e);
         	return null;
         }
         catch (NoSuchMethodException e){
         	ExceptionDisplayer.getInstance().displayException(e);
         	return null;
         }
         catch (IllegalArgumentException e){
         	ExceptionDisplayer.getInstance().displayException(e);
         	return null;
         }
         catch (InvocationTargetException e){
         	ExceptionDisplayer.getInstance().displayException(e);
         	return null;
         }
		
		return null;
	}
	
	public EpisimModelConnector getNewEpisimModelConnector() {
		EpisimModelConnector modelConnector = null;
		if(this.modelConnectorClass !=null)
	      try{
	      	modelConnector = this.modelConnectorClass.newInstance();
	         return modelConnector;
         }
         catch (InstantiationException e){
         	ExceptionDisplayer.getInstance().displayException(e);
         	return null;
         }
         catch (IllegalAccessException e){
         	ExceptionDisplayer.getInstance().displayException(e);
         	return null;
         }
		
		return null;
	}
	
	
	
	public AbstractBiomechanicalModelInitializer getBiomechanicalModelInitializer(){		
		return getNewEpisimBiomechanicalModelObject(null).getBiomechanicalModelInitializer();
	}
	
	public AbstractBiomechanicalModelInitializer getBiomechanicalModelInitializer(File modelInitializationFile){		
		return getNewEpisimBiomechanicalModelObject(null).getBiomechanicalModelInitializer(modelInitializationFile);
	}
}