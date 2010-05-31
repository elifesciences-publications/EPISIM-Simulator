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
import episiminterfaces.EpisimCellDiffModel;
import episiminterfaces.EpisimCellDiffModelGlobalParameters;

import sim.app.episim.Epidermis;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.KCyte;
import sim.app.episim.snapshot.SnapshotListener;
import sim.app.episim.snapshot.SnapshotObject;
import sim.app.episim.snapshot.SnapshotWriter;




public class BiochemicalModel implements java.io.Serializable, SnapshotListener{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7116866406451878698L;

	private Class<EpisimCellDiffModel> cellDiffModelClass;
	
	private EpisimCellDiffModelGlobalParameters globalParametersObject;

		
	
	public BiochemicalModel(Class<EpisimCellDiffModel> cellDiffModelClass, Object globalParametersObject) throws ModelCompatibilityException{
		this.cellDiffModelClass = cellDiffModelClass;
		
		
		if(globalParametersObject != null && globalParametersObject instanceof EpisimCellDiffModelGlobalParameters){
	        this.globalParametersObject= (EpisimCellDiffModelGlobalParameters)globalParametersObject;
	        SnapshotWriter.getInstance().addSnapshotListener(this);
	        
		}
      
		else throw new ModelCompatibilityException("No compatible EpisimCellDiffModelGlobalParameters-Object!");
		
		
        
	}
	
	
	private void setReloadedGlobalParametersObject(EpisimCellDiffModelGlobalParameters globalParametersObject){
		this.globalParametersObject = globalParametersObject;
	}
		
	public EpisimCellDiffModel getNewEpisimCellDiffModelObject() {
		EpisimCellDiffModel cellDiffModel = null;
		if(this.cellDiffModelClass !=null)
	      try{
	         cellDiffModel = this.cellDiffModelClass.newInstance();
	         cellDiffModel.setEpisimModelIntegrator((EpisimModelIntegrator)ModelController.getInstance().getBioMechanicalModelController().getEpisimMechanicalModel());
	         return cellDiffModel;
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
	
	public EpisimCellDiffModelGlobalParameters getEpisimCellDiffModelGlobalParameters(){
		
		return this.globalParametersObject;
	}
	
   
	public void reloadCellDiffModelGlobalParametersObject(EpisimCellDiffModelGlobalParameters parametersObject){
		if(this.globalParametersObject != null)this.globalParametersObject.setSnapshotValues(parametersObject);
	}
  
   
  
   
  
   
   
  
  public void resetInitialGlobalValues(){
	  if(globalParametersObject !=null) globalParametersObject.resetInitialGlobalValues();
  }


public List<SnapshotObject> collectSnapshotObjects() {
	List<SnapshotObject> list = new ArrayList<SnapshotObject>();
	list.add(new SnapshotObject(SnapshotObject.CELLDIFFMODELGLOBALPARAMETERS, this.globalParametersObject));
	return list;
}
   
   
   
  }
   
   
   
  
   
   

