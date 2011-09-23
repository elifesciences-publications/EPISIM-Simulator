package sim.app.episim.model.cellbehavior;


import java.net.URL;
import java.net.URLClassLoader;
import java.net.JarURLConnection;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.Attributes;
import java.io.IOException;

import episimexceptions.ModelCompatibilityException;
import episimfactories.AbstractEpisimCellBehavioralModelFactory;
import episiminterfaces.EpisimCellBehavioralModel;
import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import episiminterfaces.EpisimBiomechanicalModel;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import sim.app.episim.model.*;
import sim.app.episim.util.GlobalClassLoader;

import sim.app.episim.ExceptionDisplayer;

/**
 * A class loader for loading jar files, both local and remote.
 */
public class CellBehavioralModelJarClassLoader{
    private URL url;

   
    private Class factoryClass;
    private AbstractEpisimCellBehavioralModelFactory factory;
    
    /**
     * Creates a new JarClassLoader for the specified url.
     *
     * @param url the url of the jar file
    * @throws ModelCompatibilityException 
     */
    public CellBehavioralModelJarClassLoader(URL url) throws ModelCompatibilityException {
        
        this.url = url;
        
        
	      try{
	      	 GlobalClassLoader.getInstance().registerURL(url);
			     this.factoryClass = GlobalClassLoader.getInstance().loadClass(getClassName(new Attributes.Name("Factory-Class")));
	        	        
	      	
	         
         }
         catch (ClassNotFoundException e){
         	
         	throw new ModelCompatibilityException("No compatible EpisimCellBehavioralModelFactory found!");
         }
        
         catch (SecurityException e){
         	throw new ModelCompatibilityException("Error while Opening the Model Archive found!");
         }
         catch (IOException e){
         	throw new ModelCompatibilityException("Error while reading the Model Archive found!");
         }
	     
	      if(factoryClass != null && AbstractEpisimCellBehavioralModelFactory.class.isAssignableFrom(this.factoryClass)){
	      	try{
	            factory = (AbstractEpisimCellBehavioralModelFactory) factoryClass.newInstance();
            }
            catch (InstantiationException e){
            	throw new ModelCompatibilityException("Cannot instantiate Model-Factory!");
            }
            catch (IllegalAccessException e){
            	throw new ModelCompatibilityException("Cannot access Model-Factory!");
            }
	      }
	      else throw new ModelCompatibilityException("No compatible EpisimCellBehavioralModelFactory found!");
              
       
    }
    
    public boolean isDiffModel(){
   	 
   	 
   	if(factory != null && EpisimCellBehavioralModel.class.isAssignableFrom(factory.getEpisimCellBehavioralModelClass())
   			&& factory.getEpisimCellBehavioralModelGlobalParametersObject() != null) return true;
   				
   	return false;
    }
    
    public boolean isMechnicalModel(){
   	//TODO: Please implements when mechanical Model can be dynamicly loaded
    	return false;
    }
    
    private boolean implementsInterface(Class implementingClass, Class interfaceClass){
   	 Set<String> interfaceNameSet = new HashSet<String>();
   	 for(Class actClass: implementingClass.getInterfaces()) interfaceNameSet.add(actClass.getName());
   		
   	 
   	
   	 
   	 return interfaceNameSet.contains(interfaceClass.getSimpleName());
    }
    
    public <T extends Object> Class<T> getModelClass(Class<T> modelInterface){
   	if(modelInterface.isAssignableFrom(factory.getEpisimCellBehavioralModelClass())) return factory.getEpisimCellBehavioralModelClass();
   	else return null;
    }    
    
    public Object getGlobalParametersObject(){
   	 return factory.getEpisimCellBehavioralModelGlobalParametersObject();
    }   
    
    private String getClassName(Attributes.Name attrName)throws IOException{
   	 URL u = new URL("jar", "", url + "!/");
       JarURLConnection uc = (JarURLConnection)u.openConnection();
       uc.setDefaultUseCaches(false);
       Attributes attr = uc.getMainAttributes();
       return attr != null ? attr.getValue(attrName) : null;
    }
     
}