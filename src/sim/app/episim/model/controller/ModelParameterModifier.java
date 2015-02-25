package sim.app.episim.model.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import episimexceptions.GlobalParameterException;

import sim.app.episim.EpisimExceptionHandler;

public class ModelParameterModifier {
	
	
	
	public ModelParameterModifier(){}
	
	public void setGlobalModelPropertiesToValuesInPropertiesFile(Object globalModelPropertiesObj, File propertiesFile){
		
		Properties props = getPropertiesFromFile(propertiesFile);
		Map<String, Method> methodMap = buildMethodMapWithoutSetterPrefix(globalModelPropertiesObj);
		setAllProperties(globalModelPropertiesObj, methodMap, props);		
	}	
	
	private Properties getPropertiesFromFile(File propertiesFile){
		Properties properties = new Properties();
		FileInputStream stream;
      try{
	      stream = new FileInputStream(propertiesFile);
         properties.load(stream);
         stream.close();
      }
      catch (IOException e1){
	      EpisimExceptionHandler.getInstance().displayException(new GlobalParameterException("Could not read Global Properties File: "+ propertiesFile.getAbsolutePath() + "Detailed Exception: "+e1.getMessage()));
      }
      return properties;
	}
	
	private void setAllProperties(Object globalModelPropertiesObj, Map<String, Method> methodMap, Properties properties){
		Map<String, String> lowerCasePropertiesKeyMap = new HashMap<String, String>();
		for(Object obj: properties.keySet()){ 
			String str = null;
			if(obj instanceof String){
				str = (String) obj;
				lowerCasePropertiesKeyMap.put(str.toLowerCase(), str);
			}
		}
		for(String propName : methodMap.keySet()){
			if(lowerCasePropertiesKeyMap.containsKey(propName)){
				String val = properties.getProperty(lowerCasePropertiesKeyMap.get(propName));
				callSetterMethod(methodMap.get(propName), val, globalModelPropertiesObj);
			}
		}
		
	}
	
	public boolean doesParameterExist(Object globalModelPropertiesObj, String name){
		Map<String, Method> methodMap = buildMethodMapWithoutSetterPrefix(globalModelPropertiesObj);
		return methodMap.containsKey(name.toLowerCase());
	}
	public Class<?> getParameterDatatype(Object globalModelPropertiesObj, String name){
		Map<String, Method> methodMap = buildMethodMapWithoutSetterPrefix(globalModelPropertiesObj);
		if(methodMap.containsKey(name.toLowerCase())){
			Class<?>[] paramType = methodMap.get(name.toLowerCase()).getParameterTypes();
			return paramType[0];
		}		
		return null;
	}
	
	public void setParameterValue(Object globalModelPropertiesObj, String name, String value){
		Map<String, Method> methodMap = buildMethodMapWithoutSetterPrefix(globalModelPropertiesObj);
		if(methodMap.containsKey(name.toLowerCase())){
			Method m = methodMap.get(name.toLowerCase());
			callSetterMethod(m, value, globalModelPropertiesObj);
		}
	}
	
	
	private void callSetterMethod(Method m, String val, Object globalModelPropertiesObj){
		Class<?>[] paramType = m.getParameterTypes();
		
		try{
			if(Byte.TYPE.isAssignableFrom(paramType[0])){
				 double doubleVal = Double.parseDouble(val);
				 m.invoke(globalModelPropertiesObj, new Object[]{((byte)doubleVal)}); 
			}
			else if(Double.TYPE.isAssignableFrom(paramType[0])){				
		       m.invoke(globalModelPropertiesObj, new Object[]{Double.parseDouble(val)});   
			}
			else if(Float.TYPE.isAssignableFrom(paramType[0])){
				 m.invoke(globalModelPropertiesObj, new Object[]{Float.parseFloat(val)}); 
			}
			else if(Integer.TYPE.isAssignableFrom(paramType[0])){
				double doubleVal = Double.parseDouble(val);
				m.invoke(globalModelPropertiesObj, new Object[]{((int)doubleVal)}); 
			}
			else if(Long.TYPE.isAssignableFrom(paramType[0])){				
				 m.invoke(globalModelPropertiesObj, new Object[]{Long.parseLong(val)}); 
			}
			else if(Short.TYPE.isAssignableFrom(paramType[0])){
				 double doubleVal = Double.parseDouble(val);
				 m.invoke(globalModelPropertiesObj, new Object[]{((short)doubleVal)}); 
			}
			else if(Boolean.TYPE.isAssignableFrom(paramType[0])){
				 m.invoke(globalModelPropertiesObj, new Object[]{Boolean.parseBoolean(val)}); 
			}
			else if(String.class.isAssignableFrom(paramType[0])){
				 m.invoke(globalModelPropertiesObj, new Object[]{val}); 
			}
		}
		catch(Exception e){
			EpisimExceptionHandler.getInstance().displayException(new GlobalParameterException("The Value of a Global Parameter could not be set! Method-Name: " +m.getName() + " Value: "+ val + "Detailed Error Message: " + e.getMessage()));
		}
	}
	
	
	private Map<String, Method> buildMethodMapWithoutSetterPrefix(Object globalModelPropertiesObj){
		List<Method> methods = getAllSetterMethods(globalModelPropertiesObj);
		Map<String, Method> methodMap = new HashMap<String, Method>();
		for(Method m : methods) methodMap.put(m.getName().toLowerCase().substring(3), m);
		return methodMap;
	}
	
	private List<Method> getAllSetterMethods(Object globalModelPropertiesObj){
		Method[] methods = globalModelPropertiesObj.getClass().getMethods();
		List<Method> setterMethods = new ArrayList<Method>();
		for(Method m : methods){
			if(m.getName().toLowerCase().startsWith("set")) setterMethods.add(m);
		}		
		return setterMethods;
	}

}
