package sim.app.episim.datamonitoring.build;

import java.awt.Color;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import episimexceptions.CompilationFailedException;

import sim.app.episim.EpisimProperties;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.gui.EpisimTextOut;
import sim.app.episim.model.controller.ModelController;
import binloc.ProjectLocator;


public abstract class AbstractCommonCompiler {
	
	
	
	
	protected File compileJavaFile(File javaFile) throws CompilationFailedException{
		List<File> tmpList = new ArrayList<File>();
		tmpList.add(javaFile);
		tmpList = compileJavaFiles(tmpList);
		if(tmpList.size()>0) return tmpList.get(0);
		return null;
	}
	
	protected List<File> compileJavaFiles(List<File> javaFiles) throws CompilationFailedException{
		ClassPathConfigFileReader configReader = new ClassPathConfigFileReader();
		
		List<File> classFiles = new ArrayList<File>();
		JavaCompiler compiler;
		StandardJavaFileManager fileManager;
		Iterable<? extends JavaFileObject> compilationUnits;
		Iterable<String> options;

		// Preparing Class-File-Objects
		for (File src : javaFiles) {
			File tmp = new File(src.getAbsolutePath()
					.substring(0,
							src.getAbsolutePath().length() - 4)
					+ "class");
			classFiles.add(tmp);
		}
		
		//Preparing Compiler
		compiler = ToolProvider.getSystemJavaCompiler();
		fileManager = compiler.getStandardFileManager(null, null, null);
		compilationUnits = fileManager.getJavaFileObjectsFromFiles(javaFiles);
		try {
			String simulatorJarName =	EpisimProperties.getProperty(EpisimProperties.EPISIMBUILD_JARNAME_PROP);
			String pathToEpisimSimulatorBinaries = null;
			if(simulatorJarName != null && new File(configReader.getBinPath().getAbsolutePath()+System.getProperty("file.separator")+ simulatorJarName).exists()) 
				pathToEpisimSimulatorBinaries = configReader.getBinPath().getAbsolutePath()+ System.getProperty("file.separator")+ simulatorJarName;
			else pathToEpisimSimulatorBinaries = configReader.getBinPath().getAbsolutePath();
			
			EpisimTextOut.getEpisimTextOut().println(ModelController.getInstance().getCellBehavioralModelController().getActLoadedModelFile().getAbsolutePath()+configReader.getClasspathSeparatorChar()+
					pathToEpisimSimulatorBinaries+configReader.getClasspathSeparatorChar()+ configReader.getBinClasspath()+configReader.getLibClasspath(),Color.BLUE);
			
			
				options = Arrays.asList(new String[] { "-cp", 
					ModelController.getInstance().getCellBehavioralModelController().getActLoadedModelFile().getAbsolutePath()+configReader.getClasspathSeparatorChar()+
					pathToEpisimSimulatorBinaries+configReader.getClasspathSeparatorChar()+ configReader.getBinClasspath()+configReader.getLibClasspath()
				,"-source","1.6"
				,"-target","1.6"
				,"-encoding", "UTF-8"
				});
			boolean success = compiler.getTask(null, fileManager, null, options, null, compilationUnits).call();
			if(!success) throw new CompilationFailedException("The compilation of the generated Charts or Data Exports was not successful! Probably a required class is missing.");
			fileManager.close();
		} catch (IOException e) {
			ExceptionDisplayer.getInstance().displayException(e);

		}
      catch (URISyntaxException e){
      	ExceptionDisplayer.getInstance().displayException(e);
      }
		
		checkForMissingClassFiles(classFiles);
		
		return classFiles;
	}
	
	private void checkForMissingClassFiles(List<File> classFiles){
		Set<String> nameSet = new HashSet<String>();
		List<File> newFiles = new LinkedList<File>();
		for(File file : classFiles) nameSet.add(file.getAbsolutePath());
		for(File file : classFiles){	
			
			if(file.getParentFile() != null){		
				for(File actFile: file.getParentFile().listFiles(new FileFilter(){
					public boolean accept(File pathname) {return pathname.getAbsolutePath().endsWith(".class");}})){
				 if(!nameSet.contains(actFile.getAbsolutePath())){
					 nameSet.add(actFile.getAbsolutePath());
					 newFiles.add(actFile);					
				 }
				}
			}
		}
		classFiles.addAll(newFiles);		
	}
	
	
	
	
	
	
	public void deleteTempData(){
		File tempData = new File(getTmpPath());
		if(tempData.exists() && tempData.isDirectory()){
			deleteDir(tempData);
		}
	}
	
	protected abstract String getTmpPath();
	
	protected void deleteDir(File dir) {

	   File[] files = dir.listFiles();
	   if (files != null) {
	      for (int i = 0; i < files.length; i++) {
	         if (files[i].isDirectory()) {
	            deleteDir(files[i]);
	         }
	         else {
	            files[i].delete(); 
	         }
	      }
	      dir.delete();
	   }
	}

}
