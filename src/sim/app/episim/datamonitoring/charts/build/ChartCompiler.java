package sim.app.episim.datamonitoring.charts.build;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import binloc.ProjectLocator;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.datamonitoring.build.AbstractCommonCompiler;
import sim.app.episim.util.Names;
import episimexceptions.CompilationFailedException;
import episiminterfaces.monitoring.EpisimChart;
import episiminterfaces.monitoring.EpisimChartSet;

public class ChartCompiler extends AbstractCommonCompiler {
	private ChartSourceBuilder chartSourceBuilder;
	private ChartSetFactorySourceBuilder factorySourceBuilder;
	private final String TMPPATH;
	private List<File> factoryFiles = null;
	private List<File> chartFiles = null;
	public ChartCompiler(){
		String userTmpDir = System.getProperty("java.io.tmpdir", "temp");
		if(!userTmpDir.endsWith(System.getProperty("file.separator"))) userTmpDir = userTmpDir.concat(System.getProperty("file.separator"));
		TMPPATH= userTmpDir+ "episimcharts"+ System.getProperty("file.separator");
		this.chartSourceBuilder = new ChartSourceBuilder();
		this.factorySourceBuilder = new ChartSetFactorySourceBuilder();
	}
	
	
	private void makeTempDir(){
		File dataDirectory = new File(TMPPATH);
		File packageDirectory = new File(TMPPATH+Names.GENERATED_CHARTS_PACKAGENAME+ System.getProperty("file.separator"));
		if(!dataDirectory.exists()) dataDirectory.mkdir();
		if(!packageDirectory.exists()) packageDirectory.mkdir();
	}
	
	public void compileEpisimChartSet(EpisimChartSet chartSet) throws CompilationFailedException{
		makeTempDir();
		
		List<File> javaFiles = buildChartJavaFiles(chartSet);
		javaFiles.add(buildFactoryJavaFile(chartSet));
		javaFiles.addAll(compileJavaFiles(javaFiles));
		this.factoryFiles = new ArrayList<File>();
		List<File> listCopy = new ArrayList<File>();
		listCopy.addAll(javaFiles);
		for(File actFile:listCopy){
			if(actFile.getName().startsWith(Names.EPISIM_CHARTSET_FACTORYNAME)){
				javaFiles.remove(actFile);				
				this.factoryFiles.add(actFile);
			}
		}
		this.chartFiles = javaFiles;
		
	}
	
	private List<File> buildChartJavaFiles(EpisimChartSet chartSet){
		FileOutputStream fileOut = null;
		List<File> javaFiles = new ArrayList<File>();
		File javaFile = null;
		for(EpisimChart actChart: chartSet.getEpisimCharts()){
			try{
				javaFile = new File(TMPPATH+Names.GENERATED_CHARTS_PACKAGENAME+ System.getProperty("file.separator")+
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
				javaFile = new File(TMPPATH+Names.cleanString(Names.EPISIM_CHARTSET_FACTORYNAME)+".java");
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
	
	public List<File> getFactoryFiles(){ return this.factoryFiles; }
	
	public List<File> getChartFiles(){ return this.chartFiles; }
	
   protected String getTmpPath() { return this.TMPPATH; }
	
}
