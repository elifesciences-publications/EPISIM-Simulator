package sim.app.episim.model;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import episimbiomechanics.EpisimModelIntegrator;
import episimexceptions.ModelCompatibilityException;
import episiminterfaces.EpisimCellBehavioralModel;
import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import episiminterfaces.EpisimCellType;
import episiminterfaces.EpisimDifferentiationLevel;

import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.UniversalCell;
import sim.app.episim.snapshot.SnapshotListener;
import sim.app.episim.snapshot.SnapshotObject;
import sim.app.episim.snapshot.SnapshotWriter;
import sim.app.episim.tissue.Epidermis;




public class CellBehavioralModel implements java.io.Serializable, SnapshotListener{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7116866406451878698L;

	private Class<EpisimCellBehavioralModel> cellBehavioralModelClass;
	
	private EpisimCellBehavioralModelGlobalParameters globalParametersObject;

		
	
	public CellBehavioralModel(Class<EpisimCellBehavioralModel> cellBehavioralModelClass, Object globalParametersObject) throws ModelCompatibilityException{
		this.cellBehavioralModelClass = cellBehavioralModelClass;
		
		
		if(globalParametersObject != null && globalParametersObject instanceof EpisimCellBehavioralModelGlobalParameters){
	        this.globalParametersObject= (EpisimCellBehavioralModelGlobalParameters)globalParametersObject;
	        SnapshotWriter.getInstance().addSnapshotListener(this);
	        
		}
      
		else throw new ModelCompatibilityException("No compatible EpisimCellBehavioralModelGlobalParameters-Object!");
		
		
        
	}
	
	
	private void setReloadedGlobalParametersObject(EpisimCellBehavioralModelGlobalParameters globalParametersObject){
		this.globalParametersObject = globalParametersObject;
	}
		
	public EpisimCellBehavioralModel getNewEpisimCellBehavioralModelObject() {
		EpisimCellBehavioralModel cellBehavioralModel = null;
		if(this.cellBehavioralModelClass !=null)
	      try{
	         cellBehavioralModel = this.cellBehavioralModelClass.newInstance();
	         cellBehavioralModel.setEpisimModelIntegrator((EpisimModelIntegrator)ModelController.getInstance().getBioMechanicalModelController().getEpisimMechanicalModel());
	         return cellBehavioralModel;
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
	
	public EpisimCellBehavioralModelGlobalParameters getEpisimCellBehavioralModelGlobalParameters(){
		
		return this.globalParametersObject;
	}
	
	
		
   
	public void reloadCellBehavioralModelGlobalParametersObject(EpisimCellBehavioralModelGlobalParameters parametersObject){
		if(this.globalParametersObject != null)this.globalParametersObject.setSnapshotValues(parametersObject);
	}
     
  
  public void resetInitialGlobalValues(){
	  if(globalParametersObject !=null) globalParametersObject.resetInitialGlobalValues();
  }


public List<SnapshotObject> collectSnapshotObjects() {
	List<SnapshotObject> list = new ArrayList<SnapshotObject>();
	list.add(new SnapshotObject(SnapshotObject.CELLBEHAVIORALMODELGLOBALPARAMETERS, this.globalParametersObject));
	return list;
}
   
   
   
  }
   
   
   
  
   
   

