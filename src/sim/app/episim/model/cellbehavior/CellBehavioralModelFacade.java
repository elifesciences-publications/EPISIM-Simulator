package sim.app.episim.model.cellbehavior;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import episimexceptions.ModelCompatibilityException;
import episiminterfaces.EpisimCellBehavioralModel;
import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import episiminterfaces.EpisimCellType;
import episiminterfaces.EpisimDifferentiationLevel;
import episimmcc.EpisimModelConnector;
import sim.app.episim.EpisimExceptionHandler;
import sim.app.episim.model.UniversalCell;
import sim.app.episim.model.initialization.CellBehavioralModelInitializer;
import sim.app.episim.persistence.SimulationStateData;
import sim.app.episim.tissueimport.UniversalTissue;




public class CellBehavioralModelFacade implements java.io.Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7116866406451878698L;

	private Class<EpisimCellBehavioralModel> cellBehavioralModelClass;
	
	private EpisimCellBehavioralModelGlobalParameters globalParametersObject;

	public enum StandardCellType{ 
		KERATINOCYTE("CT_Keratinocyte");
	 	private String name = "";
	 	private StandardCellType(String name){ this.name = name;}
	 	public String toString(){ return name;}
	}
	public enum StandardDiffLevel{ 
		STEMCELL("DL_StemCell"),
		TACELL("DL_TaCell"),		
		GRANUCELL("DL_GranuCell");		
 	private String name = "";
 	private StandardDiffLevel(String name){ this.name = name;}
 	public String toString(){ return name;}
}
	
	public CellBehavioralModelFacade(Class<EpisimCellBehavioralModel> cellBehavioralModelClass, Object globalParametersObject) throws ModelCompatibilityException{
		this.cellBehavioralModelClass = cellBehavioralModelClass;
		
		
		if(globalParametersObject != null && globalParametersObject instanceof EpisimCellBehavioralModelGlobalParameters){
	        this.globalParametersObject= (EpisimCellBehavioralModelGlobalParameters)globalParametersObject;	        
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
	         return cellBehavioralModel;
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
	
	public EpisimCellBehavioralModelGlobalParameters getEpisimCellBehavioralModelGlobalParameters(){		
		return this.globalParametersObject;
	}
	
  
  public void resetInitialGlobalValues(){
	  if(globalParametersObject !=null) globalParametersObject.resetInitialGlobalValues();
  }	
  public CellBehavioralModelInitializer getCellBehavioralModelInitializer(){
   	return new CellBehavioralModelInitializer();
  }   
  public CellBehavioralModelInitializer getCellBehavioralModelInitializer(SimulationStateData simStateData){
   	return new CellBehavioralModelInitializer(simStateData);
  }
   
}
   
   
   
  
   
   

