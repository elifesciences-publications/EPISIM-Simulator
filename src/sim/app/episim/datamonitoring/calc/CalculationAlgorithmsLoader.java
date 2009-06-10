package sim.app.episim.datamonitoring.calc;

import java.io.File;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;


import episiminterfaces.calc.CalculationAlgorithm;

import binloc.ProjectLocator;
import sim.app.episim.ExceptionDisplayer;

import sim.app.episim.util.GlobalClassLoader;


public class CalculationAlgorithmsLoader{
	
	private static final String PACKAGENAME = "calculationalgorithms";
	private static final CalculationAlgorithmsLoader instance = new CalculationAlgorithmsLoader();
	private File packagePath = null;
	
	private CalculationAlgorithmsLoader(){
		
		try{
	      packagePath = new File(ProjectLocator.class.getResource("../"+PACKAGENAME+"/").toURI());
      }
      catch (URISyntaxException e){
	      ExceptionDisplayer.getInstance().displayException(e);
      }
	}
	
	public static CalculationAlgorithmsLoader getInstance(){ return instance; }
	
	public List<Class<?>> loadCalculationAlgorithms(){
		
		return getCalculationAlgorithmClasses();
		
	}

	
	
	
	private List<Class<?>> getCalculationAlgorithmClasses(){
		LinkedList<Class<?>> algorithms = new LinkedList<Class<?>>();
		if(packagePath != null){
			try{
	         GlobalClassLoader.getInstance().registerURL(packagePath.toURI().toURL());
              
				for(File file: getClassFiles()){					
		           Class<?> loadedClass = GlobalClassLoader.getInstance().loadClass(PACKAGENAME+"."+file.getName().substring(0, (file.getName().length()-".class".length())));
		           if(CalculationAlgorithm.class.isAssignableFrom(loadedClass)){
		         	   algorithms.add(loadedClass);
		           }
				}
			}
         catch (ClassNotFoundException e){
            ExceptionDisplayer.getInstance().displayException(e);
         }
         catch (MalformedURLException e){
         	ExceptionDisplayer.getInstance().displayException(e);
         }
		}
		return algorithms;
	}
	
	
	private List<File> getClassFiles(){
		LinkedList<File> foundClassFiles = new LinkedList<File>();
		
		
      if(packagePath != null && packagePath.isDirectory()){
      	for(File file : packagePath.listFiles()){
      		if(file != null && file.isFile() && file.getName() != null && file.getName().endsWith(".class")){
      			foundClassFiles.add(file);
      		}
      	}
      }
      return foundClassFiles;
	}
	
	
	

}