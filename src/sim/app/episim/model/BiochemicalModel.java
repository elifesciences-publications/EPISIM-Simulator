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
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.KCyte;




public class BiochemicalModel implements java.io.Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7116866406451878698L;

	private Class<EpisimCellDiffModel> cellDiffModelClass;
	private Class<EpisimCellDiffModelGlobalParameters> globalParametersClass;
	private EpisimCellDiffModelGlobalParameters globalParametersObject;
	private EpisimCellDiffModelGlobalParameters globalParametersResetObject;
	
	
	
	
	
	public BiochemicalModel(Class<EpisimCellDiffModel> cellDiffModelClass, Class<EpisimCellDiffModelGlobalParameters> globalParametersClass){
		this.cellDiffModelClass = cellDiffModelClass;
		this.globalParametersClass = globalParametersClass;
		if(globalParametersClass != null)
	      try{
	         this.globalParametersObject= (EpisimCellDiffModelGlobalParameters)globalParametersClass.getMethod("getInstance", null).invoke(null, null);
	         this.globalParametersResetObject = (EpisimCellDiffModelGlobalParameters)globalParametersClass.getMethod("getInstance", null).invoke(null, null);
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
	
   
   
  
   
   private Method getMethod(Class theclass, String prefix, String name, Class [] parameters) throws SecurityException, NoSuchMethodException{
   	
   	if(name != null && name.length() >0)
   	return theclass.getMethod((prefix+ name.substring(0,1).toUpperCase() + 
   			name.substring(1, name.length())), parameters);
   	else
   		return theclass.getMethod(prefix, parameters);
   }
   
  private List<Method> getMethods(String prefix, Object object) throws SecurityException, NoSuchMethodException{
	  List<Method> methods = new ArrayList<Method>();
   	
   	for(Method m :object.getClass().getMethods()){
   		if(m.getName().startsWith(prefix)) methods.add(m);
   	}
   	
   	return methods;
   
   }
	
   
   
   private Object getField(String name) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException
   {
   	Method method = getMethod(this.globalParametersClass,"get", name.trim(), null);
		if(method == null) throw new NoSuchFieldException();
		else{
			
			return method.invoke(this.globalParametersObject, ((Object[])null));
		}
   }
   
   private Object setField(String name, Class[] parameters, Object value) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException
   {
   	Method method = getMethod(this.globalParametersClass, "set" , name.trim() , parameters);
   	Object[] arguments = new Object[]{value};
		if(method == null) throw new NoSuchFieldException();
		else{
			
			return method.invoke(this.globalParametersObject, arguments);
		}
   }
   /*
   public void differentiate(KCyte  kCyte, Epidermis theEpidermis, boolean pBarrierMember) throws SecurityException, NoSuchMethodException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, InvocationTargetException{
   	Method method = getMethod("differentiate", "", new Class[]{KCyte.class, Epidermis.class, Boolean.TYPE});
   	Object[] arguments = new Object[]{kCyte, theEpidermis, pBarrierMember};
	
			method.invoke(actModelObject, arguments);

	}
   
   public void loadCacheValues(Map<String, Object> cache) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException{
   	Method [] methods = actModelObject.getClass().getMethods();
   	String name = "";
   	for(Method method: methods){
   		if( method.getName().startsWith("get")){
   			name = method.getName().trim().substring(3, 4).toLowerCase()+ method.getName().trim().substring(4, method.getName().length());
   			cache.put(name, method.invoke(actModelObject, ((Object[])null)));
   		}
   	}
   	Field[] fields = actModelObject.getClass().getFields();
   	for(int i = 0; i < fields.length-1; i++){
   	
   		 cache.put(fields[i].getName(), fields[i].get(modelClass));
   	}
   	
   }*/
   
   
   public void resetInitialGlobalValues() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException{
   	
   	List<Method> getMethods = getMethods("get", this.globalParametersResetObject);
   	List<Method> setMethods = getMethods("set", this.globalParametersObject);
   	
   	Iterator<Method> iterSet = setMethods.iterator();
   	Method setM = null;
   	Method getM = null;
   	while(iterSet.hasNext()){
   		setM = iterSet.next();
   		Iterator<Method> iterGet = getMethods.iterator();
      	while(iterGet.hasNext()){
      		getM = iterGet.next();
      		if(setM.getName().endsWith(getM.getName().substring(3))){
      			setM.invoke(this.globalParametersObject, new Object[]{(getM.invoke(this.globalParametersResetObject, (new Object[]{})))});
      		}
      	}
   		
   	}
  }
   
   
   
  
   
   
}
