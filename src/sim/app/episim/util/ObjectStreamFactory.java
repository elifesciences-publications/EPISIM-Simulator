package sim.app.episim.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;

import sim.app.episim.EpisimExceptionHandler;
import sim.app.episim.model.controller.CellBehavioralModelController;
import sim.app.episim.model.controller.ModelController;


public abstract class ObjectStreamFactory {

	
	public static ObjectInputStream getObjectInputStreamForInputStream(InputStream inputStream){
		
		final File cellBehavioralModelJarFile = ModelController.getInstance().getCellBehavioralModelController().getActLoadedModelFile();
		ObjectInputStream objIn = null;
		try{
			if(cellBehavioralModelJarFile != null){
				objIn = new ObjectInputStream(inputStream){
		
					protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
		
						try{
							return super.resolveClass(desc);
						}
						catch (ClassNotFoundException ex){
							String name = desc.getName();
							Class cl = Class.forName(name, false, GlobalClassLoader.getInstance());
							if(cl != null){
								return cl;
							}
							else{
								throw ex;
							}
						}
					}
				};
			}
			
			else objIn = new ObjectInputStream(inputStream);
		}
		catch(Exception e){
			EpisimExceptionHandler.getInstance().displayException(e);
		}
		
		return objIn;
	}
	
	public static ObjectOutputStream getObjectOutputStreamForInputStream(OutputStream outputStream){
		ObjectOutputStream objOut = null;
		try{
			objOut = new ObjectOutputStream(outputStream);
		}
		catch (IOException e){
			EpisimExceptionHandler.getInstance().displayException(e);
		}
		return objOut;
	}
}
