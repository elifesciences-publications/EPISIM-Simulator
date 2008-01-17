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
import sim.app.episim.model.*;

import sim.app.episim.ExceptionDisplayer;

/**
 * A class loader for loading jar files, both local and remote.
 */
class ModelJarClassLoader extends URLClassLoader {
    private URL url;

   
    private Class modelClass;
    private Class parametersClass;
    
    /**
     * Creates a new JarClassLoader for the specified url.
     *
     * @param url the url of the jar file
     */
    public ModelJarClassLoader(URL url) {
        super(new URL[] { url });
        this.url = url;
        
        try{
	      this.modelClass = loadClass(getClassName(new Attributes.Name("Model-Class")));
     
	      this.parametersClass = loadClass(getClassName(new Attributes.Name("Parameters-Class")));
        }
        catch (ClassNotFoundException e){
      	  ExceptionDisplayer.getInstance().displayException(e);
        }
        catch (IOException e){
      	  ExceptionDisplayer.getInstance().displayException(e);
        }
    }
    
    public boolean isDiffModel(){
   	 
   	 
   	if(modelClass != null && implementsInterface(modelClass, EpisimCellDiffModel.class)
   			&&	parametersClass != null && implementsInterface(parametersClass, EpisimCellDiffModelGlobalParameters.class)) return true;
   	return false;
    }
    
    public boolean isMechnicalModel(){
   	 if(modelClass != null && implementsInterface(modelClass, EpisimMechanicalModel.class)
    			&&	parametersClass != null && implementsInterface(parametersClass,EpisimMechanicalModelGlobalParameters.class)) return true;
    	return false;
    }
    
    private boolean implementsInterface(Class implementingClass, Class interfaceClass){
   	 Set<String> interfaceNameSet = new HashSet<String>();
   	 for(Class actClass: implementingClass.getInterfaces()) interfaceNameSet.add(actClass.getName());
   		
   	 
   	
   	 
   	 return interfaceNameSet.contains(interfaceClass.getSimpleName());
    }
    
    public <T extends Object> Class<T> getModelClass(Class<T> modelInterface){
   	if(implementsInterface(modelClass, modelInterface)) return modelClass;
   	else return null;
    }
    public <T extends Object> Class<T> getGlobalParametersClass(Class<T> globalParametersInterface){
   	 if(implementsInterface(parametersClass, globalParametersInterface)) return parametersClass;
    	else return null;
    }

   
    
    
    private String getClassName(Attributes.Name attrName)throws IOException{
   	 URL u = new URL("jar", "", url + "!/");
       JarURLConnection uc = (JarURLConnection)u.openConnection();
       Attributes attr = uc.getMainAttributes();
       return attr != null ? attr.getValue(attrName) : null;
    }
     
}