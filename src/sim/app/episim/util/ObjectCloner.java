package sim.app.episim.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.net.URL;
import java.net.URLClassLoader;

import sim.app.episim.ExceptionDisplayer;


public class ObjectCloner<T> {
	
	public ObjectCloner(){}
	
	public static <T> T  cloneObject(T object){
		
		try{
			
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			
			ObjectOutputStream objOut = new ObjectOutputStream(byteOut);
			
			objOut.writeObject(object);
			
			ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
			ObjectInputStream objIn = new ObjectInputStream(byteIn){

				protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {

					try{
						return super.resolveClass(desc);
					}
					catch (ClassNotFoundException ex){
						String name = desc.getName();
						Class cl = Class.forName(name, false, new URLClassLoader(new URL[] { jarFile.toURI().toURL() },
						      ClassLoader.getSystemClassLoader()));
						if(cl != null){
							return cl;
						}
						else{
							throw ex;
						}
					}
				}
			};
			Object result = objIn.readObject();
			
			if(result.getClass().isAssignableFrom(object.getClass())) return (T) result;
			
		}
		catch(Exception e){
			ExceptionDisplayer.getInstance().displayException(e);
		}
		
		return null;
	}
}
