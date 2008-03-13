package sim.app.episim.datamonitoring.charts.build;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import binloc.ProjectLocator;

import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.model.ModelController;
import sim.app.episim.util.Names;
import episiminterfaces.EpisimChartSet;
import episiminterfaces.EpisimChart;

public class ChartCompiler {
	private ChartSourceBuilder chartSourceBuilder;
	private FactorySourceBuilder factorySourceBuilder;
	private final String TMPPATH = System.getProperty("java.io.tmpdir", "temp")+ "episimcharts"+ System.getProperty("file.separator");
	private File factoryFile = null;
	private List<File> chartFiles = null;
	public ChartCompiler(){
		
		this.chartSourceBuilder = new ChartSourceBuilder();
		this.factorySourceBuilder = new FactorySourceBuilder();
	}
	
	
	private void makeTempDir(){
		File dataDirectory = new File(TMPPATH);
		File packageDirectory = new File(TMPPATH+Names.GENERATEDCHARTSPACKAGENAME+ System.getProperty("file.separator"));
		if(!dataDirectory.exists()) dataDirectory.mkdir();
		if(!packageDirectory.exists()) packageDirectory.mkdir();
	}
	
	public void compileEpisimChartSet(EpisimChartSet chartSet){
		makeTempDir();
		List<File> javaFiles = buildChartJavaFiles(chartSet);
		javaFiles.add(buildFactoryJavaFile(chartSet));
		javaFiles =compileJavaFiles(javaFiles);
		
		for(File actFile:javaFiles){
			if(actFile.getName().startsWith(Names.EPISIMCHARTSETFACTORYNAME)){
				javaFiles.remove(actFile);
				this.chartFiles = javaFiles;
				this.factoryFile = actFile;
				break;
			}
		}
		
	}
	
	private List<File> buildChartJavaFiles(EpisimChartSet chartSet){
		FileOutputStream fileOut = null;
		List<File> javaFiles = new ArrayList<File>();
		File javaFile = null;
		for(EpisimChart actChart: chartSet.getEpisimCharts()){
			try{
				javaFile = new File(TMPPATH+Names.GENERATEDCHARTSPACKAGENAME+ System.getProperty("file.separator")+
						Names.convertVariableToClass(Names.cleanString(actChart.getTitle())+actChart.getId())+".java");
	         fileOut = new FileOutputStream(javaFile);
         }
         catch (FileNotFoundException e){
	        ExceptionDisplayer.getInstance().displayException(e);
         }
			try{
	         fileOut.write(chartSourceBuilder.buildEpisimChartSource(actChart).getBytes("UTF-8"));
	         fileOut.flush();
	         fileOut.close();
         }
         catch (UnsupportedEncodingException e){
         	ExceptionDisplayer.getInstance().displayException(e);
         }
         catch (IOException e){
         	ExceptionDisplayer.getInstance().displayException(e);
         }
         javaFiles.add(javaFile);
         
		}
		return javaFiles;
	}
	
	private File buildFactoryJavaFile(EpisimChartSet chartSet){
		FileOutputStream fileOut = null;
		File javaFile = null;
		
			try{
				javaFile = new File(TMPPATH+Names.cleanString(Names.EPISIMCHARTSETFACTORYNAME)+".java");
	         fileOut = new FileOutputStream(javaFile);
         }
         catch (FileNotFoundException e){
	        ExceptionDisplayer.getInstance().displayException(e);
         }
			try{
	         fileOut.write(factorySourceBuilder.buildEpisimFactorySource(chartSet).getBytes("UTF-8"));
	         fileOut.flush();
	         fileOut.close();
         }
         catch (UnsupportedEncodingException e){
         	ExceptionDisplayer.getInstance().displayException(e);
         }
         catch (IOException e){
         	ExceptionDisplayer.getInstance().displayException(e);
         }
         return javaFile;
	}
	private File compileJavaFile(File javaFile){
		List<File> tmpList = new ArrayList<File>();
		tmpList.add(javaFile);
		tmpList = compileJavaFiles(tmpList);
		if(tmpList.size()>0) return tmpList.get(0);
		return null;
	}
	
	private List<File> compileJavaFiles(List<File> javaFiles){
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
					binPath.getAbsolutePath()+";"
					+libPath.getAbsolutePath()+System.getProperty("file.separator")+"jcommon-1.0.12.jar"+";"
					+libPath.getAbsolutePath()+System.getProperty("file.separator")+"jfreechart-1.0.9.jar"+";"
					+libPath.getAbsolutePath()+System.getProperty("file.separator")+"jfreechart-1.0.9-experimental.jar"+";"});
			compiler.getTask(null, fileManager, null, options, null, compilationUnits).call();
			fileManager.close();
		} catch (Exception e) {
			ExceptionDisplayer.getInstance().displayException(e);

		}
		
		return classFiles;
	}
	
	
	private File convertBinPathToLibPath(String path){
		
		
		if(path.endsWith(System.getProperty("file.separator"))) path = path.substring(0, path.length()-1);
		int i = path.length()-1;
		for(;path.charAt(i)!= System.getProperty("file.separator").charAt(0); i--);
		
		return (new File((path.substring(0, i)+System.getProperty("file.separator")+"lib")));
	}
		
	public File getFactoryFile(){ return this.factoryFile; }
	
	public List<File> getChartFiles(){ return this.chartFiles; }
	
	public void deleteTempData(){
		File tempData = new File(this.TMPPATH);
		if(tempData.exists() && tempData.isDirectory()){
			deleteDir(tempData);
		}
	}
	private void deleteDir(File dir) {

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
