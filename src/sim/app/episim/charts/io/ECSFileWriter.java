package sim.app.episim.charts.io;

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

import episiminterfaces.EpisimChart;
import episiminterfaces.EpisimChartSet;


import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.charts.build.ChartCompiler;
import sim.app.episim.charts.build.ChartSourceBuilder;
import sim.app.episim.charts.build.FactorySourceBuilder;
import sim.app.episim.util.Names;


public class ECSFileWriter {
	
	private File path;
	
	public ECSFileWriter(File path){
		this.path = path;
		
		
	}
	
	public void createChartSetArchive(EpisimChartSet chartSet) {
				
				JarOutputStream jarOut=null;
				Manifest manifest;
				
								
					try {
					
						
							
							
							// Adding MANIFEST:MF

							StringBuffer sBuffer = new StringBuffer();

							sBuffer.append("Manifest-Version: 1.0\n");
							sBuffer.append("Created-By: 1.1 (Episim - Uni Heidelberg)\n");
							sBuffer.append("Factory-Class: "+ Names.EPISIMCHARTSETFACTORYNAME +"\n");
							
							
		
							ByteArrayInputStream byteIn = new ByteArrayInputStream(sBuffer.toString().getBytes("UTF-8"));

							manifest = new Manifest(byteIn);

							jarOut = new JarOutputStream(new FileOutputStream(path), manifest);
							jarOut.setLevel(1);
							
							ChartCompiler chartCompiler = new ChartCompiler();
							chartCompiler.compileEpisimChartSet(chartSet);
							
							
							FileInputStream fileIn;
							for(File actChartFile: chartCompiler.getChartFiles()){
								jarOut.putNextEntry(new JarEntry(Names.GENERATEDCHARTSPACKAGENAME+ "/"+actChartFile.getName()));
								fileIn = new FileInputStream(actChartFile);
								byte[] bytes = new byte[1024];
								int available = 0;
								while ((available = fileIn.read(bytes)) > 0) {
									jarOut.write(bytes, 0, available);
								}
								jarOut.flush();
								fileIn.close();
							}							
						
							
							jarOut.putNextEntry(new JarEntry(chartCompiler.getFactoryFile().getName()));
							fileIn = new FileInputStream(chartCompiler.getFactoryFile());
							byte[] bytes = new byte[1024];
							int available = 0;
							while ((available = fileIn.read(bytes)) > 0) {
								jarOut.write(bytes, 0, available);
							}
							fileIn.close();
							jarOut.flush();	
							
							
							
							ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
							ObjectOutputStream objOut = new ObjectOutputStream(byteOut);
							objOut.writeObject(chartSet);
							objOut.flush();
							objOut.close();
							byteOut.close();
							jarOut.putNextEntry(new JarEntry(Names.EPISIMCHARTSETFILENAME));
							jarOut.write(byteOut.toByteArray());
							jarOut.flush();
		
							jarOut.finish();
							jarOut.close();
							
							chartCompiler.deleteTempData();
							
						} catch (Exception e) {
							ExceptionDisplayer.getInstance()
									.displayException(e);

						}
		
						
	    
	}
}
