package sim.app.episim.datamonitoring.dataexport.build;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.datamonitoring.build.AbstractCommonCompiler;
import sim.app.episim.util.Names;
import binloc.ProjectLocator;
import episiminterfaces.monitoring.EpisimDataExportDefinition;
import episiminterfaces.monitoring.EpisimDataExportDefinitionSet;

public class DataExportCompiler extends AbstractCommonCompiler {
	private DataExportSourceBuilder dataExportSourceBuilder;
	private DataExportFactorySourceBuilder factorySourceBuilder;
	private final String TMPPATH = System.getProperty("java.io.tmpdir", "temp")+ "episimdataexport"+ System.getProperty("file.separator");
	private List<File> factoryFiles = null;
	private List<File> dataExportFiles = null;
	
	public DataExportCompiler(){
		
		this.dataExportSourceBuilder = new DataExportSourceBuilder();
		this.factorySourceBuilder = new DataExportFactorySourceBuilder();
	}
	
	
	private void makeTempDir(){
		File dataDirectory = new File(TMPPATH);
		File packageDirectory = new File(TMPPATH+Names.GENERATEDDATAEXPORTPACKAGENAME+ System.getProperty("file.separator"));
		if(!dataDirectory.exists()) dataDirectory.mkdir();
		if(!packageDirectory.exists()) packageDirectory.mkdir();
	}
	
	public void compileEpisimDataExportDefinitionSet(EpisimDataExportDefinitionSet dataExportDefinitionSet){
		makeTempDir();
		
		List<File> javaFiles;
		
		javaFiles = buildDataExportJavaFiles(dataExportDefinitionSet);
		javaFiles.add(buildFactoryJavaFile(dataExportDefinitionSet));
		javaFiles.addAll(compileJavaFiles(javaFiles));
		this.factoryFiles = new ArrayList<File>();
		List<File> listCopy = new ArrayList<File>();
		listCopy.addAll(javaFiles);
		for(File actFile:listCopy){
			if(actFile.getName().startsWith(Names.EPISIMDATAEXPORTFACTORYNAME)){
				javaFiles.remove(actFile);				
				this.factoryFiles.add(actFile);				
			}
		}
		this.dataExportFiles = javaFiles;
		
	}
	
	private List<File> buildDataExportJavaFiles(EpisimDataExportDefinitionSet dataExportDefinitionSet) {

		FileOutputStream fileOut = null;
		File javaFile = null;
		List<File> javaFiles = new ArrayList<File>();
		for(EpisimDataExportDefinition dataExportDefinition:dataExportDefinitionSet.getEpisimDataExportDefinitions()){
			try{
				javaFile = new File(TMPPATH + Names.GENERATEDDATAEXPORTPACKAGENAME + System.getProperty("file.separator")
				      + Names.convertVariableToClass(Names.cleanString(dataExportDefinition.getName())
				      + dataExportDefinition.getId()) + ".java");
				fileOut = new FileOutputStream(javaFile);
			}
			catch (FileNotFoundException e){
				ExceptionDisplayer.getInstance().displayException(e);
			}
			try{
				fileOut.write(dataExportSourceBuilder.buildEpisimDataExportSource(dataExportDefinition).getBytes("UTF-8"));
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
	
	private File buildFactoryJavaFile(EpisimDataExportDefinitionSet dataExportDefinitionSet){
		FileOutputStream fileOut = null;
		File javaFile = null;
		
			try{
				javaFile = new File(TMPPATH+Names.cleanString(Names.EPISIMDATAEXPORTFACTORYNAME)+".java");
	         fileOut = new FileOutputStream(javaFile);
         }
         catch (FileNotFoundException e){
	        ExceptionDisplayer.getInstance().displayException(e);
         }
			try{
	         fileOut.write(factorySourceBuilder.buildEpisimFactorySource(dataExportDefinitionSet).getBytes("UTF-8"));
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
	
	public List<File> getDataExportSetFiles(){ return this.dataExportFiles; }
	
   protected String getTmpPath() { return this.TMPPATH; }
	
}
