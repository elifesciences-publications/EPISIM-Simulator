package sim.app.episim.datamonitoring.dataexport.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.datamonitoring.charts.build.ChartCompiler;
import sim.app.episim.datamonitoring.dataexport.build.DataExportCompiler;
import sim.app.episim.util.Names;
import episiminterfaces.EpisimDataExportDefinition;
import episiminterfaces.EpisimDataExportDefinitionSet;


public class EDEFileWriter {
	
	private File path;
	
	public EDEFileWriter(File path){
		this.path = path;
		
		
	}
	
	public void createDataExportDefinitionSetArchive(EpisimDataExportDefinitionSet dataExportSet) {
				
				JarOutputStream jarOut=null;
				Manifest manifest;
				
								
					try {
																
							
							// Adding MANIFEST:MF

							StringBuffer sBuffer = new StringBuffer();

							sBuffer.append("Manifest-Version: 1.0\n");
							sBuffer.append("Created-By: 1.1 (Episim - Uni Heidelberg)\n");
							sBuffer.append("Factory-Class: "+ Names.EPISIMDATAEXPORTFACTORYNAME +"\n");
							
							
		
							ByteArrayInputStream byteIn = new ByteArrayInputStream(sBuffer.toString().getBytes("UTF-8"));

							manifest = new Manifest(byteIn);

							jarOut = new JarOutputStream(new FileOutputStream(path), manifest);
							jarOut.setLevel(1);
						
							DataExportCompiler dataExportCompiler = new DataExportCompiler();
							dataExportCompiler.compileEpisimDataExportDefinitionSet(dataExportSet);
							
							
							FileInputStream fileIn;
							List<File> dataExportFiles = dataExportCompiler.getDataExportSetFiles();
							for(File dataExportFile : dataExportFiles){
								jarOut.putNextEntry(new JarEntry(Names.GENERATEDDATAEXPORTPACKAGENAME+ "/"+dataExportFile.getName()));
								fileIn = new FileInputStream(dataExportFile);
								byte[] bytes = new byte[1024];
								int available = 0;
								while ((available = fileIn.read(bytes)) > 0) {
									jarOut.write(bytes, 0, available);
								}
								jarOut.flush();
								fileIn.close();
							}							
						
							
							jarOut.putNextEntry(new JarEntry(dataExportCompiler.getFactoryFile().getName()));
							fileIn = new FileInputStream(dataExportCompiler.getFactoryFile());
							byte[] bytes = new byte[1024];
							int available = 0;
							while ((available = fileIn.read(bytes)) > 0) {
								jarOut.write(bytes, 0, available);
							}
							fileIn.close();
							jarOut.flush();	
							
														
							ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
							ObjectOutputStream objOut = new ObjectOutputStream(byteOut);
							objOut.writeObject(dataExportSet);
							objOut.flush();
							objOut.close();
							byteOut.close();
							jarOut.putNextEntry(new JarEntry(Names.EPISIMDATAEXPORTFILENAME));
							jarOut.write(byteOut.toByteArray());
							jarOut.flush();
		
							jarOut.finish();
							jarOut.close();
							
							dataExportCompiler.deleteTempData();
							
						} catch (Exception e) {
							ExceptionDisplayer.getInstance()
									.displayException(e);

						}
		
						
	    
	}
}