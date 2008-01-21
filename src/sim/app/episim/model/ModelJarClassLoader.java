package sim.app.episim.model;


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

import episimfactories.AbstractEpisimCellDiffModelFactory;
import episiminterfaces.EpisimCellDiffModel;
import episiminterfaces.EpisimCellDiffModelGlobalParameters;
import episiminterfaces.EpisimMechanicalModel;
import episiminterfaces.EpisimMechanicalModelGlobalParameters;
import sim.app.episim.model.*;

import sim.app.episim.ExceptionDisplayer;

/**
 * A class loader for loading jar files, both local and remote.
 */
class ModelJarClassLoader extends URLClassLoader {
    private URL url;

   
    private Class factoryClass;
    private AbstractEpisimCellDiffModelFactory factory;
    
    /**
     * Creates a new JarClassLoader for the specified url.
     *
     * @param url the url of the jar file
     */
    public ModelJarClassLoader(URL url) {
        super(new URL[] { url });
        this.url = url;
        
        try{
	      this.factoryClass = loadClass(getClassName(new Attributes.Name("Factory-Class")));
	     
	      if(factoryClass != null && AbstractEpisimCellDiffModelFactory.class.isAssignableFrom(this.factoryClass)){
	      	factory = (AbstractEpisimCellDiffModelFactory) factoryClass.newInstance();
	      }
	      else throw new Exception("No compatible EpisimCellDiffModelFactory found!");
        }
        catch (Exception e){
      	  ExceptionDisplayer.getInstance().displayException(e);
        }
       
    }
    
    public boolean isDiffModel(){
   	 
   	 
   	if(factory != null && EpisimCellDiffModel.class.isAssignableFrom(factory.getEpisimCellDiffModelClass())
   			&&	factory.getEpisimCellDiffModelGlobalParametersObject() != null) return true;
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
   	if(modelInterface.isAssignableFrom(factory.getEpisimCellDiffModelClass())) return factory.getEpisimCellDiffModelClass();
   	else return null;
    }
    public Object getGlobalParametersObject(){
   	 return factory.getEpisimCellDiffModelGlobalParametersObject();
    }

   
    
    
    private String getClassName(Attributes.Name attrName)throws IOException{
   	 URL u = new URL("jar", "", url + "!/");
       JarURLConnection uc = (JarURLConnection)u.openConnection();
       Attributes attr = uc.getMainAttributes();
       return attr != null ? attr.getValue(attrName) : null;
    }
     
}