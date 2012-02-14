package sim.app.episim.datamonitoring.charts.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import episimexceptions.CompilationFailedException;
import episiminterfaces.monitoring.EpisimChart;
import episiminterfaces.monitoring.EpisimChartSet;


import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.datamonitoring.charts.build.ChartCompiler;
import sim.app.episim.datamonitoring.charts.build.ChartSourceBuilder;
import sim.app.episim.datamonitoring.charts.build.ChartSetFactorySourceBuilder;
import sim.app.episim.gui.EpisimSimulator;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.util.Names;


public class ECSFileWriter {
	
	private File path;
	
	public ECSFileWriter(File path){
		this.path = path;
		
		
	}
	
	public void createChartSetArchive(EpisimChartSet chartSet) throws CompilationFailedException {
				
				JarOutputStream jarOut=null;
				Manifest manifest;
				
								
					try {
									
							ChartCompiler chartCompiler = new ChartCompiler();
							chartCompiler.compileEpisimChartSet(chartSet);
						
							// Adding MANIFEST:MF
							StringBuffer sBuffer = new StringBuffer();

							sBuffer.append("Manifest-Version: 1.0\n");
							sBuffer.append("Created-By: "+EpisimSimulator.versionID+" (Episim - Uni Heidelberg)\n");
							sBuffer.append("Factory-Class: "+ Names.EPISIM_CHARTSET_FACTORYNAME +"\n");
							
							
		
							ByteArrayInputStream byteIn = new ByteArrayInputStream(sBuffer.toString().getBytes("UTF-8"));

							manifest = new Manifest(byteIn);

							jarOut = new JarOutputStream(new FileOutputStream(path), manifest);
							jarOut.setLevel(1);
							
							
							
							
							FileInputStream fileIn;
							for(File actChartFile: chartCompiler.getChartFiles()){
								jarOut.putNextEntry(new JarEntry(Names.GENERATED_CHARTS_PACKAGENAME+ "/"+actChartFile.getName()));
								fileIn = new FileInputStream(actChartFile);
								byte[] bytes = new byte[1024];
								int available = 0;
								while ((available = fileIn.read(bytes)) > 0) {
									jarOut.write(bytes, 0, available);
								}
								jarOut.flush();
								fileIn.close();
							}							
						
							for(File factoryFile : chartCompiler.getFactoryFiles()){
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
							ObjectOutputStream objOut = new ObjectOutputStream(byteOut);
							objOut.writeObject(chartSet);
							objOut.flush();
							objOut.close();
							byteOut.close();
							jarOut.putNextEntry(new JarEntry(Names.EPISIM_CHARTSET_FILENAME));
							jarOut.write(byteOut.toByteArray());
							jarOut.flush();
		
							jarOut.finish();
							jarOut.close();
						//TODO: Enable / Disable erasure of temp data	
						chartCompiler.deleteTempData();
							
						} catch (IOException e) {
							ExceptionDisplayer.getInstance()
									.displayException(e);

						}
		
						
	    
	}
}
