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
import episiminterfaces.EpisimDataExportDefinition;

public class DataExportCompiler extends AbstractCommonCompiler {
	private DataExportSourceBuilder dataExportSourceBuilder;
	private DataExportFactorySourceBuilder factorySourceBuilder;
	private final String TMPPATH = System.getProperty("java.io.tmpdir", "temp")+ "episimdataexport"+ System.getProperty("file.separator");
	private File factoryFile = null;
	private File dataExportFile = null;
	
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
	
	public void compileEpisimDataExportDefinition(EpisimDataExportDefinition dataExportDefinition){
		makeTempDir();
		File libPath = null;
		List<File> javaFiles = new ArrayList<File>();
		try{
	      File binPath = new File(ProjectLocator.class.getResource("../").toURI());
	      libPath = convertBinPathToLibPath(binPath.getAbsolutePath());
      }
      catch (URISyntaxException e){
      	ExceptionDisplayer.getInstance()
			.displayException(e);
      }
		javaFiles.add(buildDataExportJavaFile(dataExportDefinition));
		javaFiles.add(buildFactoryJavaFile(dataExportDefinition));
		javaFiles =compileJavaFiles(javaFiles,"");
		
		for(File actFile:javaFiles){
			if(actFile.getName().startsWith(Names.EPISIMDATAEXPORTFACTORYNAME)){
				javaFiles.remove(actFile);
				this.dataExportFile = javaFiles.get(0);
				this.factoryFile = actFile;
				break;
			}
		}
		
	}
	
	private File buildDataExportJavaFile(EpisimDataExportDefinition dataExportDefinition) {

		FileOutputStream fileOut = null;
		File javaFile = null;

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

		return javaFile;
	}
	
	private File buildFactoryJavaFile(EpisimDataExportDefinition dataExportDefinition){
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
	         fileOut.write(factorySourceBuilder.buildEpisimFactorySource(dataExportDefinition).getBytes("UTF-8"));
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
	
	public File getFactoryFile(){ return this.factoryFile; }
	
	public File getDataExportFile(){ return this.dataExportFile; }
	
   protected String getTmpPath() { return this.TMPPATH; }
	
}
