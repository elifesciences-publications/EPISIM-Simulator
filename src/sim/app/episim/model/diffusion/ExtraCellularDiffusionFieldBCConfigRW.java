package sim.app.episim.model.diffusion;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import static java.nio.file.StandardCopyOption.*;
import java.nio.file.CopyOption;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import sim.app.episim.persistence.XmlFile;
import sim.app.episim.persistence.dataconvert.XmlDoubleGrid2D;
import sim.app.episim.persistence.dataconvert.XmlExtracellularDiffusionFieldBCConfig2D;
import sim.app.episim.persistence.dataconvert.XmlExtracellularDiffusionFieldBCConfig3D;


public class ExtraCellularDiffusionFieldBCConfigRW {
	private static int counter = 0;
	private static final String BCCONFIGS = "diffusionFieldBCConfigurations";
	private static final String BCCONFIG = "diffusionFieldBCConfig";
	private static final String NAME = "name";
	private static final String BCCONFIGSFILE="ecdfBCConfigs.xml";
	
	private File modelFile;
	
	public ExtraCellularDiffusionFieldBCConfigRW(File modelFile){
		this.modelFile = modelFile;
	}
	
	public void saveBCConfigs(Map<String, ExtracellularDiffusionFieldBCConfig2D> bcConfigs) throws ParserConfigurationException, SAXException, IOException{
		XmlFile xmlFile = new XmlFile(BCCONFIGS);		
		Element root = xmlFile.getRoot();		
		if(bcConfigs != null && bcConfigs.size() > 0){
			if(bcConfigs.get(0) instanceof ExtracellularDiffusionFieldBCConfig3D){
				for(String ecdfName : bcConfigs.keySet()){
					ExtracellularDiffusionFieldBCConfig3D bcConfig = (ExtracellularDiffusionFieldBCConfig3D) bcConfigs.get(ecdfName);
					Element ecdfConfigElement = new XmlExtracellularDiffusionFieldBCConfig3D(bcConfig).toXMLNode(BCCONFIG, xmlFile);					
					ecdfConfigElement.setAttribute(NAME, ecdfName);				
					root.appendChild(ecdfConfigElement);					
				}
			}
			else{
				for(String ecdfName : bcConfigs.keySet()){
					ExtracellularDiffusionFieldBCConfig2D bcConfig = bcConfigs.get(ecdfName);
					Element ecdfConfigElement = new XmlExtracellularDiffusionFieldBCConfig2D(bcConfig).toXMLNode(BCCONFIG, xmlFile);					
					ecdfConfigElement.setAttribute(NAME, ecdfName);				
					root.appendChild(ecdfConfigElement);					
				}
			}
		}
		String userTmpDir = System.getProperty("java.io.tmpdir", "temp");
		if(!userTmpDir.endsWith(System.getProperty("file.separator"))) userTmpDir = userTmpDir.concat(System.getProperty("file.separator"));
		File bcConfigsFile = new File(userTmpDir+ System.getProperty("file.separator")+BCCONFIGSFILE);
		xmlFile.save(bcConfigsFile);
		addFileToExistingJar(modelFile, bcConfigsFile);
		bcConfigsFile.delete();
	}
	
	public Map<String, ExtracellularDiffusionFieldBCConfig2D> readBCConfigs(Map<String, ExtracellularDiffusionFieldBCConfig2D> bcConfigs) throws FileNotFoundException, IOException, SAXException, ParserConfigurationException{
		 	 
		 JarFile jarFile = new JarFile(modelFile);
		 JarEntry entry = jarFile.getJarEntry(BCCONFIGSFILE);
		
		 if(entry != null){
			 File tempFile = File.createTempFile(BCCONFIGSFILE, ""+System.currentTimeMillis()+counter);
			 counter++;
			 InputStream stream = jarFile.getInputStream(entry);
			 Files.copy(stream, FileSystems.getDefault().getPath(tempFile.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
			 XmlFile xmlFile = new XmlFile(tempFile);
			 Element root = xmlFile.getRoot();
			 
			 NodeList childNodes = root.getChildNodes();
			 HashMap<String, Node> nodeMap = new HashMap<String, Node>();
			 for(int i = 0; i < childNodes.getLength();i++ ){
				 Node actChild = childNodes.item(i);
				 if(actChild.getNodeName().equals(BCCONFIG)){
					 nodeMap.put(actChild.getAttributes().getNamedItem(NAME).getNodeValue(), actChild);
				 }
			 }		 
			 if(bcConfigs != null && bcConfigs.size() > 0){
				if(bcConfigs.get(0) instanceof ExtracellularDiffusionFieldBCConfig3D){
					for(String ecdfName : bcConfigs.keySet()){
						ExtracellularDiffusionFieldBCConfig3D bcConfig = (ExtracellularDiffusionFieldBCConfig3D) bcConfigs.get(ecdfName);
						if(nodeMap.containsKey(ecdfName)){
							new XmlExtracellularDiffusionFieldBCConfig3D(nodeMap.get(ecdfName)).copyValuesToTarget(bcConfig);
						}				
					}
				}
				else{
					for(String ecdfName : bcConfigs.keySet()){
						ExtracellularDiffusionFieldBCConfig2D bcConfig = bcConfigs.get(ecdfName);
						if(nodeMap.containsKey(ecdfName)){
							new XmlExtracellularDiffusionFieldBCConfig2D(nodeMap.get(ecdfName)).copyValuesToTarget(bcConfig);
						}				
					}
				}
			}
		 }		 
		 return bcConfigs;
	}
	
	
	
	
	private void addFileToExistingJar(File jarFile, File newFile) throws IOException {
	        // get a temp file
	    File tempFile = File.createTempFile(jarFile.getName(), ""+System.currentTimeMillis()+counter);
	    counter++;
	    Files.copy(FileSystems.getDefault().getPath(jarFile.getAbsolutePath()), new FileOutputStream(tempFile));    
	    
	  
	    byte[] buf = new byte[1024];
	
	    JarInputStream jarIn = new JarInputStream(new FileInputStream(tempFile));
	    JarOutputStream jarOut = new JarOutputStream(new FileOutputStream(jarFile), jarIn.getManifest());
	    JarEntry entry = jarIn.getNextJarEntry();
	    while (entry != null) {
	        String name = entry.getName();
	        // Add JAR entry to output stream.
	        if(!name.endsWith(BCCONFIGSFILE)){
	      	  jarOut.putNextEntry(new JarEntry(name));
		            // Transfer bytes from the JAR file to the output file
		        int len;
		        while ((len = jarIn.read(buf)) > 0) {
		           jarOut.write(buf, 0, len);
		        }
	        }
	        entry = jarIn.getNextJarEntry();
	    }
	    // Close the streams        
	    jarIn.close();
	    // Compress the files
	    InputStream in = new FileInputStream(newFile);
	        // Add JAR entry to output stream.
	    jarOut.putNextEntry(new JarEntry(newFile.getName()));
	    // Transfer bytes from the file to the ZIP file
	    int len;
	    while ((len = in.read(buf)) > 0) {
	       jarOut.write(buf, 0, len);
	    }
	    // Complete the entry
	    jarOut.closeEntry();
	    in.close();	   
	    // Complete the JAR file
	    jarOut.close();
	    tempFile.delete();
	}
}
