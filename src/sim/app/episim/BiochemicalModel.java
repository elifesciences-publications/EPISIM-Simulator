package sim.app.episim;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;




public class BiochemicalModel implements java.io.Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7116866406451878698L;

	private Class modelClass;
	
	private Object actModelObject;
	private Object resetModelObject;
	
	public BiochemicalModel(Class modelClass)throws InstantiationException, IllegalAccessException{
		this.modelClass = modelClass;
		actModelObject = modelClass.newInstance();
		resetModelObject = modelClass.newInstance();
	}
	
	
	public void initModel() throws InstantiationException, IllegalAccessException{
		//System.out.println("Init Model");
		//actModelObject = modelClass.newInstance();
	}
	
	public int getGlobalIntConstant(String name) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException{
		Object obj;
		if((obj=modelClass.getField(name.trim()).get(modelClass)) instanceof Integer) return ((Integer) obj).intValue();
		else return -1;
	}
	
	public Object getModelAsObject() {
		return actModelObject;
	}
	
   public int getIntField(String name)throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException{
		Object value = getField(name);
		if(value instanceof Integer) return ((Integer) value).intValue();
		else throw new NoSuchFieldException("Field not found!");
	}
	
   public double getDoubleField(String name)throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException{
   	Object value = getField(name);
		if(value instanceof Double) return ((Double) value).doubleValue();
		else throw new NoSuchFieldException("Field not found!");
	}
   public float getFloatField(String name)throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException{
		
   	Object value = getField(name);
		if(value instanceof Float) return ((Float) value).floatValue();
		else throw new NoSuchFieldException("Field not found!");
	}
   
   public boolean getBooleanField(String name)throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException{
   	Object value = getField(name);
		if(value instanceof Boolean) return ((Boolean) value).booleanValue();
		else throw new NoSuchFieldException("Field not found!");
   }
   
   public void setIntField(String name, int value)throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException{
		
   	setField( name, new Class[]{Integer.TYPE}, value);
	}
	
   public void setDoubleField(String name, double value)throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException{
   	setField( name,  new Class[]{Double.TYPE}, value);
		
	}
   public void setFloatField(String name, float value)throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException{
   	setField( name,  new Class[]{Float.TYPE}, value);
		
	}
   
   public void setBooleanField(String name, boolean value)throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException{
   	setField( name,  new Class[]{Boolean.TYPE}, value);
   }
   
   public double get2DDoubleArrayValue(String name, int pos1, int pos2) throws SecurityException, NoSuchMethodException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, InvocationTargetException{
   	
   	Method method = getMethod("gib", name, new Class[]{Integer.TYPE, Integer.TYPE});
   	Object[] arguments = new Object[]{pos1, pos2};
		if(method == null) throw new NoSuchFieldException();
		else{
			
			return ((Double) method.invoke(actModelObject, arguments)).doubleValue();
		}
   	
   }
   
   public void set2DDoubleArrayValue(String name, int pos1, int pos2, double val) throws SecurityException, NoSuchMethodException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, InvocationTargetException{
   	
   	Method method = getMethod("setze", name, new Class[]{Integer.TYPE, Integer.TYPE, Double.TYPE});
   	Object[] arguments = new Object[]{pos1, pos2};
		if(method == null) throw new NoSuchFieldException();
		else{
			
			method.invoke(actModelObject, arguments);
		}
   	
   }
   
   private Method getMethod(String prefix, String name, Class [] parameters) throws SecurityException, NoSuchMethodException{
   	
   	if(name != null && name.length() >0)
   	return actModelObject.getClass().getMethod((prefix+ name.substring(0,1).toUpperCase() + 
   			name.substring(1, name.length())), parameters);
   	else
   		return actModelObject.getClass().getMethod(prefix, parameters);
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
   	Method method = getMethod("get", name.trim(), null);
		if(method == null) throw new NoSuchFieldException();
		else{
			
			return method.invoke(actModelObject, ((Object[])null));
		}
   }
   
   private Object setField(String name, Class[] parameters, Object value) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException
   {
   	Method method = getMethod("set" , name.trim() , parameters);
   	Object[] arguments = new Object[]{value};
		if(method == null) throw new NoSuchFieldException();
		else{
			
			return method.invoke(actModelObject, arguments);
		}
   }
   
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
   	
   }
   
   
   public void resetInitialGlobalValues() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException{
   	
   	List<Method> getMethods = getMethods("get", resetModelObject);
   	List<Method> setMethods = getMethods("set", actModelObject);
   	
   	Iterator<Method> iterSet = setMethods.iterator();
   	Method setM = null;
   	Method getM = null;
   	while(iterSet.hasNext()){
   		setM = iterSet.next();
   		Iterator<Method> iterGet = getMethods.iterator();
      	while(iterGet.hasNext()){
      		getM = iterGet.next();
      		if(setM.getName().endsWith(getM.getName().substring(3))){
      			setM.invoke(actModelObject, new Object[]{(getM.invoke(resetModelObject, (new Object[]{})))});
      		}
      	}
   		
   	}
   	
   	
   	
   }
   
   
   
   public double [][] getAdhesionArray() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException{
   	Method method = getMethod("returnAdhesionArray", "", null);
   	
	
			return (double[][]) method.invoke(actModelObject, ((Object[])null));
   }
   
   
}
