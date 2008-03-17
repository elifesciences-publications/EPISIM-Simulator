package sim.app.episim.datamonitoring.build;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.model.ModelController;
import binloc.ProjectLocator;


public abstract class AbstractCommonCompiler {
	
	protected File compileJavaFile(File javaFile, String extendedClassPath){
		List<File> tmpList = new ArrayList<File>();
		tmpList.add(javaFile);
		tmpList = compileJavaFiles(tmpList, extendedClassPath);
		if(tmpList.size()>0) return tmpList.get(0);
		return null;
	}
	
	protected List<File> compileJavaFiles(List<File> javaFiles, String extendedClassPath){
		File binPath = null;
		File libPath = null;
		//Binary location of the simulation environment and the charting libraries
		try{
	      binPath = new File(ProjectLocator.class.getResource("../").toURI());
	      libPath = convertBinPathToLibPath(binPath.getAbsolutePath());
      }
      catch (URISyntaxException e){
      	ExceptionDisplayer.getInstance()
			.displayException(e);
      }
		
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
					ModelController.getInstance().getBioChemicalModelController().getActLoadedModelFile().getAbsolutePath()+";"+
					binPath.getAbsolutePath()+";"+ extendedClassPath});
			compiler.getTask(null, fileManager, null, options, null, compilationUnits).call();
			fileManager.close();
		} catch (Exception e) {
			ExceptionDisplayer.getInstance().displayException(e);

		}
		
		return classFiles;
	}
	
	
	protected File convertBinPathToLibPath(String path){
		
		
		if(path.endsWith(System.getProperty("file.separator"))) path = path.substring(0, path.length()-1);
		int i = path.length()-1;
		for(;path.charAt(i)!= System.getProperty("file.separator").charAt(0); i--);
		
		return (new File((path.substring(0, i)+System.getProperty("file.separator")+"lib")));
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
