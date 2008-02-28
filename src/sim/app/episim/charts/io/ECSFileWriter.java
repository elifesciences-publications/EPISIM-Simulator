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


import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.charts.EpisimChartSet;
import sim.app.episim.charts.EpisimChartSetFactory;
import sim.app.episim.util.Names;


public class ECSFileWriter {
	
	private File path;
	public ECSFileWriter(File path){
		this.path = path;
	}
	
	public void createChartSetArchive(Class<EpisimChartSetFactory> chartSetFactoryClass, EpisimChartSet chartSet) {
				
				JarOutputStream jarOut=null;
				Manifest manifest;
				
								
					try {
					
							ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
							
							ObjectOutputStream objOut = new ObjectOutputStream(byteOut);
							
							objOut.writeObject(chartSet);
							
							
							// Adding MANIFEST:MF

							StringBuffer sBuffer = new StringBuffer();

							sBuffer.append("Manifest-Version: 1.0\n");
							sBuffer.append("Created-By: 1.1 (Episim - Uni Heidelberg)\n");
							sBuffer.append("Factory-Class: "+ chartSetFactoryClass.getCanonicalName().replace(".", System.getProperty("file.separator"))+"\n");
							
							
		
							ByteArrayInputStream byteIn = new ByteArrayInputStream(sBuffer.toString().getBytes("UTF-8"));

							manifest = new Manifest(byteIn);

							jarOut = new JarOutputStream(new FileOutputStream(path), manifest);
							
							
							
							List<File> fileList = new ArrayList<File>();
							
							jarOut.putNextEntry(new JarEntry(Names.EPISIMCHARTSETFILENAME));
							jarOut.write(byteOut.toByteArray());
							jarOut.flush();
							
							
							
							jarOut.putNextEntry(new JarEntry(chartSetFactoryClass.getCanonicalName().replace(".", System.getProperty("file.separator")) +".class"));
							
							InputStream factoryClassStream =chartSetFactoryClass.getResourceAsStream(chartSetFactoryClass.getSimpleName()+".class");
							
							byte[] bytes = new byte[1024];
							int available;
							while ((available = factoryClassStream.read(bytes)) > 0) {
								jarOut.write(bytes, 0, available);
							}
							
							jarOut.flush();
							
							
							
							jarOut.putNextEntry(new JarEntry(chartSetFactoryClass.getSuperclass().getCanonicalName().replace(".", System.getProperty("file.separator")) +".class"));
							InputStream superClassStream =chartSetFactoryClass.getSuperclass().getResourceAsStream(chartSetFactoryClass.getSuperclass().getSimpleName()+".class");
							bytes = new byte[1024];
							while ((available = superClassStream.read(bytes)) > 0) {
								jarOut.write(bytes, 0, available);
							}
							jarOut.flush();
					/*		
							
							for (File f : fileList) {

								if (f.isDirectory())
									continue;
								String name = f.getAbsolutePath();
								//name =name.replace(modelFolderFinal.getAbsolutePath() + System.getProperty("file.separator"), "");
								
								if (name.endsWith(".class")) {

									jarOut.putNextEntry(new JarEntry(name));
								} else
									continue;

								FileInputStream fileInput = new FileInputStream(f);

							   bytes = new byte[1024];
								 
								while ((available = fileInput.read(bytes)) > 0) {
									jarOut.write(bytes, 0, available);
								}
								jarOut.flush();
								fileInput.close();

							}*/
							//jarOut.flush();
							jarOut.finish();
							jarOut.close();
							
							
						} catch (Exception e) {
							ExceptionDisplayer.getInstance()
									.displayException(e);

						}
		
						
	    
	}
}
