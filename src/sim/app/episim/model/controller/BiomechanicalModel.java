package sim.app.episim.model.controller;



import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import episimbiomechanics.EpisimModelConnector;
import episimexceptions.ModelCompatibilityException;
import episiminterfaces.EpisimBiomechanicalModel;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;


import sim.app.episim.AbstractCell;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.UniversalCell;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import sim.app.episim.snapshot.SnapshotListener;
import sim.app.episim.snapshot.SnapshotObject;
import sim.app.episim.snapshot.SnapshotWriter;
import sim.app.episim.util.ObjectManipulations;




public class BiomechanicalModel implements java.io.Serializable, SnapshotListener{
		
	private static final long serialVersionUID = 512640154196012852L;
	private EpisimBiomechanicalModelGlobalParameters actParametersObject;
	private EpisimBiomechanicalModelGlobalParameters resetParametersObject;
	private Class<? extends EpisimBiomechanicalModel> biomechanicalModelClass;
	private Class<? extends EpisimModelConnector> modelConnectorClass;	
	
	
	public BiomechanicalModel(Class<? extends EpisimBiomechanicalModel> biomechanicalModelClass, Class<? extends EpisimModelConnector> modelConnectorClass,
			EpisimBiomechanicalModelGlobalParameters actParametersObject) throws ModelCompatibilityException{
		
		this.biomechanicalModelClass = biomechanicalModelClass;
		this.modelConnectorClass = modelConnectorClass;
		
		if(actParametersObject != null){
	        this.actParametersObject = actParametersObject;
	        this.resetParametersObject = ObjectManipulations.cloneObject(actParametersObject);
	        SnapshotWriter.getInstance().addSnapshotListener(this);	        
		}      
		else throw new ModelCompatibilityException("No compatible EpisimCellBehavioralModelGlobalParameters-Object!");	
	}	
	
	public void reloadMechanicalModelGlobalParametersObject(EpisimBiomechanicalModelGlobalParameters parametersObject){
		this.resetParametersObject = parametersObject;
		ObjectManipulations.resetInitialGlobalValues(actParametersObject, resetParametersObject);
	}
	
	
	public EpisimBiomechanicalModelGlobalParameters getEpisimMechanicalModelGlobalParameters() {
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
		
	public EpisimBiomechanicalModel getNewEpisimBiomechanicalModelObject(AbstractCell cell) {
		EpisimBiomechanicalModel biomechanicalModel = null;
		if(this.biomechanicalModelClass !=null)
	      try{
	      	Constructor<? extends EpisimBiomechanicalModel> constructor = this.biomechanicalModelClass.getConstructor(new Class[]{AbstractCell.class});
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
	
	
	
	public BiomechanicalModelInitializer getBiomechanicalModelInitializer(){		
		return getNewEpisimBiomechanicalModelObject(null).getBiomechanicalModelInitializer();
	}
	
	public BiomechanicalModelInitializer getBiomechanicalModelInitializer(File modelInitializationFile){		
		return getNewEpisimBiomechanicalModelObject(null).getBiomechanicalModelInitializer(modelInitializationFile);
	}
}