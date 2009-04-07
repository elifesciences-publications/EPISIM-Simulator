package sim.app.episim.datamonitoring.build;

import java.io.File;
import java.io.FileFilter;
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

import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.model.ModelController;
import binloc.ProjectLocator;


public abstract class AbstractCommonCompiler {
	
	
	
	
	protected File compileJavaFile(File javaFile){
		List<File> tmpList = new ArrayList<File>();
		tmpList.add(javaFile);
		tmpList = compileJavaFiles(tmpList);
		if(tmpList.size()>0) return tmpList.get(0);
		return null;
	}
	
	protected List<File> compileJavaFiles(List<File> javaFiles){
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
			
			options = Arrays.asList(new String[] { "-cp", 
					ModelController.getInstance().getBioChemicalModelController().getActLoadedModelFile().getAbsolutePath()+configReader.getClasspathSeparatorChar()+
					configReader.getBinPath().getAbsolutePath()+configReader.getClasspathSeparatorChar()+ configReader.getBinClasspath()+configReader.getLibClasspath()});
			compiler.getTask(null, fileManager, null, options, null, compilationUnits).call();
			fileManager.close();
		} catch (Exception e) {
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
