package sim.app.episim.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import sim.app.episim.ExceptionDisplayer;


public abstract class ObjectManipulations {
	public static final String GETPREFIX = "get";
	public static final String SETPREFIX = "set";
	public ObjectManipulations(){}
	
	public static <T> T  cloneObject(T object){
		
		try{
			
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			
			ObjectOutputStream objOut = new ObjectOutputStream(byteOut);
			
			objOut.writeObject(object);
			
			ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
			ObjectInputStream objIn = ObjectStreamFactory.getObjectInputStreamForInputStream(byteIn);
			Object result = objIn.readObject();
			
			if(result.getClass().isAssignableFrom(object.getClass())) return (T) result;
			
		}
		catch(Exception e){
			ExceptionDisplayer.getInstance().displayException(e);
		}
		
		return null;
	}
	public static <T> void resetInitialGlobalValues(T object, T resetObject){
	   try{	
	   	List<Method> getMethods = getMethods(GETPREFIX, object);
	   	List<Method> setMethods = getMethods(SETPREFIX, resetObject);
	   	
	   	Iterator<Method> iterSet = setMethods.iterator();
	   	Method setM = null;
	   	Method getM = null;
	   	while(iterSet.hasNext()){
	   		setM = iterSet.next();
	   		Iterator<Method> iterGet = getMethods.iterator();
	      	while(iterGet.hasNext()){
	      		getM = iterGet.next();
	      		if(setM.getName().substring(SETPREFIX.length()).equals(getM.getName().substring(GETPREFIX.length()))){
	      			setM.invoke(object, new Object[]{(getM.invoke(resetObject, (new Object[]{})))});
	      		}
	      	}
	   	}
	   	}
	   	catch(Exception e){
	   		ExceptionDisplayer.getInstance().displayException(e);
	   	}
	}
	private static List<Method> getMethods(String prefix, Object object) throws SecurityException, NoSuchMethodException{
		  List<Method> methods = new ArrayList<Method>();
	   	
	   	for(Method m :object.getClass().getMethods()){
	   		if(m.getName().startsWith(prefix)) methods.add(m);
	   	}
	   	
	   	return methods;
	   
	}
}
