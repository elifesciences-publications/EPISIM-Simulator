package episimmcc;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import episiminterfaces.EpisimBiomechanicalModel;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.NoExport;

import sim.app.episim.EpisimExceptionHandler;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import sim.app.episim.util.GlobalClassLoader;
import binloc.ProjectLocator;




public abstract class EpisimModelConnector implements java.io.Serializable{
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface Hidden{}	
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface Pairwise{}
	
	private static final String PACKAGENAME = "episimmcc";
	private static File packagePath = null;
	
	@Hidden
	@NoExport
	protected abstract String getIdForInternalUse();
	
	@Hidden
	@NoExport
	public final String getBiomechanicalModelId(){
		String finalId = "";
		finalId = finalId.concat(getIdForInternalUse());
		finalId = finalId.concat("_");
		finalId = finalId.concat(this.getClass().getCanonicalName());
		return finalId;
	}
	
	/**
	 * This method should be overridden in subclasses in case that pairwise parameters are defined
	 */
	@NoExport
	public void resetPairwiseParameters(){}
	
	@Hidden
	@NoExport
	public abstract String getBiomechanicalModelName();
	
	@Hidden
	@NoExport
	public boolean isEpidermisDemoModel(){ return false; }
	
	@Hidden
	@NoExport
	public abstract Class<? extends EpisimBiomechanicalModel> getEpisimBioMechanicalModelClass();
	
	@Hidden
	@NoExport
	public abstract Class<? extends BiomechanicalModelInitializer> getEpisimBioMechanicalModelInitializerClass();
	
	@Hidden
	@NoExport
	public abstract Class<? extends EpisimBiomechanicalModelGlobalParameters> getEpisimBioMechanicalModelGlobalParametersClass();
	
	@Hidden
	@NoExport
	public static ArrayList<Class<? extends EpisimModelConnector>> getAvailableModelConnectors(){
		
		try{
	      packagePath = new File(ProjectLocator.class.getResource("../"+PACKAGENAME+"/").toURI());
      }
      catch (URISyntaxException e){
	      EpisimExceptionHandler.getInstance().displayException(e);
      }
      if(packagePath != null){
      	
      	try{
	         GlobalClassLoader.getInstance().registerURL(packagePath.toURI().toURL());
            List<File> classFiles = new ArrayList<File>();
            ArrayList<Class<? extends EpisimModelConnector>> resultList = new ArrayList<Class<? extends EpisimModelConnector>>();
            getClassFiles(packagePath, classFiles);  
				for(File file: classFiles){
					  String fullFileName = getFullClassName(file);
					  if(fullFileName !=null){
			           Class<?> loadedClass = GlobalClassLoader.getInstance().loadClass(fullFileName.substring(0, (fullFileName.length()-".class".length())));
			           if(EpisimModelConnector.class.isAssignableFrom(loadedClass) && loadedClass!= EpisimModelConnector.class){
			         	   resultList.add((Class<? extends EpisimModelConnector>)loadedClass);
			           }
					  }
				}
				return resultList;
			}
         catch (ClassNotFoundException e){
            EpisimExceptionHandler.getInstance().displayException(e);
         }
         catch (MalformedURLException e){
         	EpisimExceptionHandler.getInstance().displayException(e);
         }
      	
      	
      }      
      return null;
	}
	
	private static String getFullClassName(File file){
		if(file != null){
			String resultString ="";
			
			try{
				String path = file.getCanonicalPath();
				path = path.replace(System.getProperty("file.separator"),"/");
				String[] nameParts = path.split("/");
				boolean concatenationStarted = false;
				for(String namePart: nameParts){
					if(namePart!= null && namePart.length()>0){
						if(namePart.equals(PACKAGENAME)){
							concatenationStarted = true;
							resultString = resultString.concat(namePart);
						}
						else if(concatenationStarted){
							resultString = resultString.concat(".");
							resultString = resultString.concat(namePart);
						}
					}
				}
				return resultString;				
			}
			catch (IOException e){
				EpisimExceptionHandler.getInstance().displayException(e);
			}
			
			
		}			
		return null;
	}
	
	private static void getClassFiles(File rootPath, List<File> classFiles){		
      if(rootPath != null && rootPath.isDirectory()){
      	for(File file : rootPath.listFiles()){
      		if(file != null && file.isFile() && file.getName() != null && file.getName().endsWith(".class")){
      			classFiles.add(file);
      		}
      		else if(file != null && file.isDirectory()) getClassFiles(file, classFiles);
      	}
      }      
	}
}
