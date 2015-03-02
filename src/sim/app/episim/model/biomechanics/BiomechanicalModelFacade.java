package sim.app.episim.model.biomechanics;



import java.awt.geom.GeneralPath;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import episimexceptions.ModelCompatibilityException;
import episiminterfaces.EpisimBiomechanicalModel;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;


import episimmcc.EpisimModelConnector;
import sim.app.episim.EpisimExceptionHandler;
import sim.app.episim.model.AbstractCell;
import sim.app.episim.model.UniversalCell;
import sim.app.episim.model.controller.TissueController;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import sim.app.episim.persistence.SimulationStateData;
import sim.app.episim.util.ObjectManipulations;
import sim.engine.SimState;




public class BiomechanicalModelFacade implements java.io.Serializable{
		
	private static final long serialVersionUID = 512640154196012852L;
	private EpisimBiomechanicalModelGlobalParameters actParametersObject;
	private EpisimBiomechanicalModelGlobalParameters resetParametersObject;
	private Class<? extends EpisimBiomechanicalModel> biomechanicalModelClass;
	private Class<? extends EpisimModelConnector> modelConnectorClass;
	private Class<? extends BiomechanicalModelInitializer> biomechanicalModelInitializerClass;	
	
	
	public BiomechanicalModelFacade(Class<? extends EpisimBiomechanicalModel> biomechanicalModelClass, Class<? extends EpisimModelConnector> modelConnectorClass,
			EpisimBiomechanicalModelGlobalParameters actParametersObject, 
			Class<? extends BiomechanicalModelInitializer> biomechanicalModelInitializerClass) throws ModelCompatibilityException{
		
		this.biomechanicalModelClass = biomechanicalModelClass;
		this.modelConnectorClass = modelConnectorClass;
		this.biomechanicalModelInitializerClass = biomechanicalModelInitializerClass;
		
		if(actParametersObject != null){
	        this.actParametersObject = actParametersObject;
	        this.resetParametersObject = ObjectManipulations.cloneObject(actParametersObject);       
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
		
	public EpisimBiomechanicalModel getNewEpisimBiomechanicalModelObject(AbstractCell cell) {
		EpisimBiomechanicalModel biomechanicalModel = null;
		if(this.biomechanicalModelClass !=null)
	      try{
	      	if(cell != null){
		      	Constructor<? extends EpisimBiomechanicalModel> constructor = this.biomechanicalModelClass.getConstructor(new Class[]{AbstractCell.class});
		      	biomechanicalModel = constructor.newInstance(new Object[]{cell});
	      	}
	      	else{
	      		biomechanicalModel = this.biomechanicalModelClass.newInstance();
	      	}
	         return biomechanicalModel;
         }
         catch (InstantiationException e){
         	EpisimExceptionHandler.getInstance().displayException(e);
         	return null;
         }
         catch (IllegalAccessException e){
         	EpisimExceptionHandler.getInstance().displayException(e);
         	return null;
         }
         catch (SecurityException e){
         	EpisimExceptionHandler.getInstance().displayException(e);
         	return null;
         }
         catch (NoSuchMethodException e){
         	EpisimExceptionHandler.getInstance().displayException(e);
         	return null;
         }
         catch (IllegalArgumentException e){
         	EpisimExceptionHandler.getInstance().displayException(e);
         	return null;
         }
         catch (InvocationTargetException e){
         	EpisimExceptionHandler.getInstance().displayException(e);
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
         	EpisimExceptionHandler.getInstance().displayException(e);
         	return null;
         }
         catch (IllegalAccessException e){
         	EpisimExceptionHandler.getInstance().displayException(e);
         	return null;
         }
		
		return null;
	}
	
	
	
	public void clearCellField(){
		EpisimBiomechanicalModel biomechanicalModel = this.getNewEpisimBiomechanicalModelObject(null);
		if(biomechanicalModel instanceof AbstractBiomechanicalModel) ((AbstractBiomechanicalModel) biomechanicalModel).clearCellField();
	}
	
	
	public void resetCellField(){
		EpisimBiomechanicalModel biomechanicalModel = this.getNewEpisimBiomechanicalModelObject(null);
		if(biomechanicalModel instanceof AbstractBiomechanicalModel) ((AbstractBiomechanicalModel) biomechanicalModel).resetCellField();
	}
	
	public void removeCellsInWoundArea(GeneralPath woundArea){
		if(woundArea!=null){
			EpisimBiomechanicalModel biomechanicalModel = this.getNewEpisimBiomechanicalModelObject(null);
			if(biomechanicalModel instanceof AbstractBiomechanical2DModel){ 
				((AbstractBiomechanical2DModel) biomechanicalModel).removeCellsInWoundArea(woundArea);
			}
		}
	}	
	
	public void newSimStepGloballyFinished(long simStepNumber, SimState state){
		EpisimBiomechanicalModel biomechanicalModel = this.getNewEpisimBiomechanicalModelObject(null);
		if(biomechanicalModel instanceof AbstractBiomechanicalModel){
			((AbstractBiomechanicalModel) biomechanicalModel).newSimStepGloballyFinished(simStepNumber, state);
		}
	}
	public void newGlobalSimStep(long simStepNumber, SimState state){
		EpisimBiomechanicalModel biomechanicalModel = this.getNewEpisimBiomechanicalModelObject(null);
		if(biomechanicalModel instanceof AbstractBiomechanicalModel){
			((AbstractBiomechanicalModel) biomechanicalModel).newGlobalSimStep(simStepNumber, state);
		}
	}
	
	public Object getCellField(){
		EpisimBiomechanicalModel biomechanicalModel = this.getNewEpisimBiomechanicalModelObject(null);
		if(biomechanicalModel instanceof AbstractBiomechanicalModel){
			return ((AbstractBiomechanicalModel) biomechanicalModel).getCellField();
		}
		return null;
	}
	
	public BiomechanicalModelInitializer getBiomechanicalModelInitializer(){
		BiomechanicalModelInitializer modelInitializer = null;
		try{
	      modelInitializer = this.biomechanicalModelInitializerClass.newInstance();
      }
      catch (InstantiationException e){
	     EpisimExceptionHandler.getInstance().displayException(e);
      }
      catch (IllegalAccessException e){
      	EpisimExceptionHandler.getInstance().displayException(e);
      }
		return modelInitializer;
	}
	
	public BiomechanicalModelInitializer getBiomechanicalModelInitializer(SimulationStateData simStateData){
		BiomechanicalModelInitializer modelInitializer = null;
		Constructor<? extends BiomechanicalModelInitializer> constructor=null;
      try{
	      constructor = this.biomechanicalModelInitializerClass.getConstructor(new Class[]{SimulationStateData.class});
	      modelInitializer = constructor.newInstance(new Object[]{simStateData});
      }
      catch (SecurityException e){
      	EpisimExceptionHandler.getInstance().displayException(e);
      }
      catch (NoSuchMethodException e){
      	EpisimExceptionHandler.getInstance().displayException(e);
      }
      catch (IllegalArgumentException e){
      	EpisimExceptionHandler.getInstance().displayException(e);
      }
      catch (InstantiationException e){
      	EpisimExceptionHandler.getInstance().displayException(e);
      }
      catch (IllegalAccessException e){
      	EpisimExceptionHandler.getInstance().displayException(e);
      }
      catch (InvocationTargetException e){
      	EpisimExceptionHandler.getInstance().displayException(e);
      }		
		return modelInitializer;
	}
}