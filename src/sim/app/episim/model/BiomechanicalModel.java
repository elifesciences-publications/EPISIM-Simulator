package sim.app.episim.model;


import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import sim.app.episim.Epidermis;
import sim.app.episim.KCyte;




public class BiomechanicalModel implements java.io.Serializable{
	
	

	private Class modelClass;
	
	private Object actModelObject;
	private Object resetModelObject;
	
	
	
	public BiomechanicalModel(Class modelClass)throws InstantiationException, IllegalAccessException{
		this.modelClass = modelClass;
		actModelObject = modelClass.newInstance();
		resetModelObject = modelClass.newInstance();
	}
	
	
	public void initModel() throws InstantiationException, IllegalAccessException{
		
	}
	
	
	
	public EpisimMechanicalModel getEpisimMechanicalModel() {
		return new EpisimModel();
	}
	
	public EpisimMechanicalModelGlobalParameters getEpisimMechanicalModelGlobalParameters() {
		return new EpisimModel();
	}
	
  
   
   
}