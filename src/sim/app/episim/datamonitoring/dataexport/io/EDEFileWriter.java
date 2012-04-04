package sim.app.episim.datamonitoring.dataexport.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.datamonitoring.charts.build.ChartCompiler;
import sim.app.episim.datamonitoring.dataexport.build.DataExportCompiler;
import sim.app.episim.gui.EpisimSimulator;
import sim.app.episim.util.Names;
import episimexceptions.CompilationFailedException;
import episiminterfaces.monitoring.EpisimDataExportDefinition;
import episiminterfaces.monitoring.EpisimDataExportDefinitionSet;


public class EDEFileWriter {
	
	private File path;
	
	public EDEFileWriter(File path){
		this.path = path;
		
		
	}
	
	public void createDataExportDefinitionSetArchive(EpisimDataExportDefinitionSet dataExportSet) throws CompilationFailedException, JAXBException {
				
				JarOutputStream jarOut=null;
				Manifest manifest;
				
								
					try {
						DataExportCompiler dataExportCompiler = new DataExportCompiler();
						dataExportCompiler.compileEpisimDataExportDefinitionSet(dataExportSet);									
							
							// Adding MANIFEST:MF

							StringBuffer sBuffer = new StringBuffer();

							sBuffer.append("Manifest-Version: 1.0\n");
							sBuffer.append("Created-By: "+EpisimSimulator.versionID+" (Episim - Uni Heidelberg)\n");
							sBuffer.append("Factory-Class: "+ Names.EPISIM_DATAEXPORT_FACTORYNAME +"\n");
							
							
		
							ByteArrayInputStream byteIn = new ByteArrayInputStream(sBuffer.toString().getBytes("UTF-8"));

							manifest = new Manifest(byteIn);

							jarOut = new JarOutputStream(new FileOutputStream(path), manifest);
							jarOut.setLevel(1);
						
							
							
							
							FileInputStream fileIn;
							List<File> dataExportFiles = dataExportCompiler.getDataExportSetFiles();
							for(File dataExportFile : dataExportFiles){
								jarOut.putNextEntry(new JarEntry(Names.GENERATED_DATAEXPORT_PACKAGENAME+ "/"+dataExportFile.getName()));
								fileIn = new FileInputStream(dataExportFile);
								byte[] bytes = new byte[1024];
								int available = 0;
								while ((available = fileIn.read(bytes)) > 0) {
									jarOut.write(bytes, 0, available);
								}
								jarOut.flush();
								fileIn.close();
							}							
						
							for(File factoryFile : dataExportCompiler.getFactoryFiles()){
								jarOut.putNextEntry(new JarEntry(factoryFile.getName()));
								fileIn = new FileInputStream(factoryFile);
								byte[] bytes = new byte[1024];
								int available = 0;
								while ((available = fileIn.read(bytes)) > 0) {
									jarOut.write(bytes, 0, available);
								}
								fileIn.close();
								jarOut.flush();	
							}
														
							ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
							JAXBContext jc = JAXBContext.newInstance(sim.app.episim.datamonitoring.dataexport.EpisimDataExportDefinitionSetImpl.class);
							Marshaller m = jc.createMarshaller();
						   m.marshal(dataExportSet, byteOut);
							byteOut.close();
							jarOut.putNextEntry(new JarEntry(Names.EPISIM_DATAEXPORT_XML_FILENAME));
							jarOut.write(byteOut.toByteArray());
							
							
							
							/*byteOut = new ByteArrayOutputStream();
							ObjectOutputStream objOut = new ObjectOutputStream(byteOut);
							objOut.writeObject(dataExportSet);
							objOut.flush();
							objOut.close();
							byteOut.close();
							jarOut.putNextEntry(new JarEntry(Names.EPISIM_DATAEXPORT_FILENAME));
							jarOut.write(byteOut.toByteArray());*/
							
							
							jarOut.flush();
		
							jarOut.finish();
							jarOut.close();
							//TODO: Enable / Disable deletion of tempory data
							dataExportCompiler.deleteTempData();
							
						} catch (IOException e) {
							ExceptionDisplayer.getInstance()
									.displayException(e);

						}
		
						
	    
	}
}
