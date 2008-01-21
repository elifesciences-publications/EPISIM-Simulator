package sim.app.episim.model;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import episiminterfaces.EpisimCellDiffModel;
import episiminterfaces.EpisimCellDiffModelGlobalParameters;

import sim.app.episim.Epidermis;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.KCyte;




public class BiochemicalModel implements java.io.Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7116866406451878698L;

	private Class<EpisimCellDiffModel> cellDiffModelClass;
	
	private EpisimCellDiffModelGlobalParameters globalParametersObject;

	
	
	
	
	
	public BiochemicalModel(Class<EpisimCellDiffModel> cellDiffModelClass, Object globalParametersObject){
		this.cellDiffModelClass = cellDiffModelClass;
		
		try{
		if(globalParametersObject != null && globalParametersObject instanceof EpisimCellDiffModelGlobalParameters)
	        this.globalParametersObject= (EpisimCellDiffModelGlobalParameters)globalParametersObject;
      
		else throw new Exception("No compatible EpisimCellDiffModelGlobalParameters_Object!!!");
		}
		catch (Exception e){
	       ExceptionDisplayer.getInstance().displayException(e);
      }
        
	}
	
	
	
		
	public EpisimCellDiffModel getNewEpisimCellDiffModelObject() {
		if(this.cellDiffModelClass !=null)
	      try{
	         return this.cellDiffModelClass.newInstance();
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
	
   
   
  
   
  
   
  
   
   
  
  public void resetInitialGlobalValues(){
	  if(globalParametersObject !=null) globalParametersObject.resetInitialGlobalValues();
  }
   
   
   
  }
   
   
   
  
   
   

