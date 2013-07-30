package episimbiomechanics;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

import episimbiomechanics.EpisimModelConnector.Hidden;

import sim.app.episim.EpisimProperties;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.gui.EpisimSimulator;
import sim.app.episim.gui.ExtendedFileChooser;
import sim.app.episim.gui.ImageLoader;
import sim.app.episim.util.GlobalClassLoader;
import binloc.ProjectLocator;


public class ModelDescriptorFileGenerator {
	
	
	public ModelDescriptorFileGenerator(){}
	
	public void start(){
		ArrayList<Class<? extends EpisimModelConnector>> modelConnector = EpisimModelConnector.getAvailableModelConnectors();
		Class<? extends EpisimModelConnector> selectedModelConnector = null;
		
		
		try{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e){
			
			ExceptionDisplayer.getInstance().displayException(e);
		}
		
		JFrame parentFrame = new JFrame();
		
		parentFrame.setIconImage(new ImageIcon(ImageLoader.class.getResource("icon.gif")).getImage());
		
		if(modelConnector != null){
			selectedModelConnector = (Class<? extends EpisimModelConnector>)JOptionPane.showInputDialog(parentFrame, "Please select the Episim Model Connector Class", "Model Descriptor File Generator", JOptionPane.PLAIN_MESSAGE, null, modelConnector.toArray(), modelConnector.toArray()[0]);
		}
		if(selectedModelConnector != null){
			String modelConnectorName = null;
			do{
			 modelConnectorName = JOptionPane.showInputDialog(parentFrame, "Please provide a name for this Model Connector", "Model Descriptor File Generator", JOptionPane.PLAIN_MESSAGE);
			}
			while(modelConnectorName == null || modelConnectorName.trim().isEmpty());
			Document document = null;
			try{
				document = generateModelDescriptorDocument(selectedModelConnector, modelConnectorName);
			}
			catch (Exception e){
				ExceptionDisplayer.getInstance().displayException(e);
			}
			if(document != null){
				try{
					
					String path = selectedModelConnector.getResource("./").getPath();
					path = path.replace(System.getProperty("file.separator"), "/");
					if(path.contains("/bin/")){
						path = path.replace("/bin/", "/src/");
					}
					File file = new File(path);
					if(file.isDirectory() && file.exists()) write(document, file.getCanonicalPath()+"/ModelDescriptor.xml");
					
					if(path.contains("/src/")){
						path = path.replace("/src/", "/bin/");
					}
					file = new File(path);
					if(file.isDirectory() && file.exists()) write(document, file.getCanonicalPath()+"/ModelDescriptor.xml");
				
					ExtendedFileChooser chooser = new ExtendedFileChooser(".jar");					
					chooser.setDialogTitle("Save Model Connector");				
					if(chooser.showSaveDialog(parentFrame) == ExtendedFileChooser.APPROVE_OPTION){
						writeJarFile(selectedModelConnector, chooser.getSelectedFile());
					}			
				}
				catch (IOException e){
					ExceptionDisplayer.getInstance().displayException(e);
				}
			}
		}
		parentFrame.dispose();
	}
	
	
	private void writeJarFile(Class<? extends EpisimModelConnector> modelConnectorClass,  File jarPath) throws IOException{
		JarOutputStream jarOut = null;
		
		Manifest manifest;
		

		StringBuffer sBuffer = new StringBuffer();

		sBuffer.append("Manifest-Version: 1.0\n");
		sBuffer.append("Created-By: "+EpisimSimulator.versionID+" (Episim - Uni Heidelberg)\n");
		

		
		ByteArrayInputStream byteIn = new ByteArrayInputStream(sBuffer
				.toString().getBytes("UTF-8"));

		manifest = new Manifest(byteIn);
		jarOut = new JarOutputStream(new FileOutputStream(jarPath), manifest);
		jarOut.setLevel(1);
		
		ArrayList<File> fileList = generateFileList(modelConnectorClass);		
		
		for (java.io.File f : fileList){	
			
			if(f.isDirectory()) continue;
			
			String name = f.getAbsolutePath().replace(java.io.File.separatorChar, '/');
		//	name = (name.endsWith(".xml") || name.endsWith(".sbml")) ? name.replace(sbmlDir.getAbsolutePath() + java.io.File.separatorChar, "sbml"+ java.io.File.separatorChar):name.replace(projectDir.getAbsolutePath() + java.io.File.separatorChar, "");
			
			if(!name.contains(this.getClass().getSimpleName()))jarOut.putNextEntry(new JarEntry(name.substring(name.indexOf("episimbiomechanics"))));

			FileInputStream fileInput = new FileInputStream(f);

			byte[] bytes = new byte[1024];
			int available;
			while ((available = fileInput.read(bytes)) > 0) {
				jarOut.write(bytes, 0, available);
			}
			jarOut.flush();
			fileInput.close();
		}
		jarOut.flush();
		jarOut.finish();
		jarOut.close();
	}
	
	private ArrayList<File>	generateFileList(Class<? extends EpisimModelConnector> modelConnectorClass){
		
		ArrayList<File> fileList = new ArrayList<File>();
		
		fileList.addAll(Arrays.asList(getAllClassFilesOfSamePackage(modelConnectorClass)));
		fileList.add(new File(modelConnectorClass.getResource("./").getPath()+"/ModelDescriptor.xml"));
		
		Class<?> superClass = modelConnectorClass.getSuperclass();
		while(EpisimModelConnector.class.isAssignableFrom(superClass)){
			
			fileList.addAll(Arrays.asList(getAllClassFilesOfSamePackage((Class<? extends EpisimModelConnector>) superClass)));
			superClass = superClass.getSuperclass();
		}		
		return fileList;		
	}
	
	private File[] getAllClassFilesOfSamePackage(Class<? extends EpisimModelConnector> modelConnectorClass){
		File modelConnectorClassDirectory = new File(modelConnectorClass.getResource("./").getPath());		
		File[] requiredClassFiles = modelConnectorClassDirectory.listFiles(new FileFilter(){		
         public boolean accept(File pathname) {
	         return pathname.getAbsolutePath().endsWith(".class");
         }});
		return requiredClassFiles;
	}
	
	
	private Document generateModelDescriptorDocument(Class<? extends EpisimModelConnector> modelConnectorClass, String visibleName) throws DOMException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException, ParserConfigurationException{
		if(modelConnectorClass != null){
			Method[] methods = modelConnectorClass.getDeclaredMethods();
			
			
			EpisimModelConnector actConnector = modelConnectorClass.newInstance();
			
			
			
			HashMap<String, Method> getterMethods = new HashMap<String, Method>();
			HashMap<String, Method> setterMethods = new HashMap<String, Method>();
			for(Method m : methods){
				if(m.getName().startsWith("get") && m.getAnnotation(Hidden.class) == null && m.getModifiers() == Modifier.PUBLIC)getterMethods.put(m.getName().substring(3), m);				
				else if(m.getName().startsWith("set") && m.getAnnotation(Hidden.class) == null && m.getModifiers() == Modifier.PUBLIC) setterMethods.put(m.getName().substring(3), m);
			}
			
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			
			DocumentBuilder builder = factory.newDocumentBuilder();
			
			Document document = builder.newDocument();
			//document.setXmlVersion("1.0");
			
			Element rootElement = document.createElement("modeldescriptor");
			document.appendChild(rootElement);
			
			Element modelIdElement = document.createElement("modelid");
			modelIdElement.setAttribute("id", actConnector.getBiomechanicalModelId());
			rootElement.appendChild(modelIdElement);
			
			Element visibleNameElement = document.createElement("visiblename");
			visibleNameElement.setAttribute("name", visibleName);
			rootElement.appendChild(visibleNameElement);
			
			
			Element modelNameElement = document.createElement("modelname");
			modelNameElement.setAttribute("name", actConnector.getBiomechanicalModelName());
			rootElement.appendChild(modelNameElement);
			
			Element modelConnectorClassElement = document.createElement("modelconnectorclass");
			modelConnectorClassElement.setAttribute("name", modelConnectorClass.getCanonicalName());
			rootElement.appendChild(modelConnectorClassElement);
			
			Element modelParametersElement = document.createElement("modelparameters");
			rootElement.appendChild(modelParametersElement);
			
			for(String m : getterMethods.keySet()){
				if(isValidReturnType(getterMethods.get(m).getReturnType())){
					Element parameterElement = document.createElement("parameter");
					modelParametersElement.appendChild(parameterElement);
					
					parameterElement.setAttribute("name", m.substring(0,1).toLowerCase()+m.substring(1));
					parameterElement.setAttribute("datatype", getReturnTypeString(getterMethods.get(m).getReturnType()));
					parameterElement.setAttribute("default", ""+getterMethods.get(m).invoke(actConnector, null));
					parameterElement.setAttribute("readonly",""+ !setterMethods.containsKey(m));
				}
			}
			return document;
		}
		return null;
	}
	
	private void write(Document doc, String outputFile) throws IOException {
		FileWriter fw = new FileWriter(outputFile, false);
		OutputFormat format = new OutputFormat(doc);
		format.setIndenting(true);
		format.setIndent(2);
		format.setLineSeparator("\r\n");
		format.setEncoding("UTF-8");
		XMLSerializer serializer = new XMLSerializer(fw, format);
		serializer.serialize(doc);
	}
	
	private String getReturnTypeString(Class<?> returnType){
		if(returnType!= null){
			
			if(Integer.TYPE.isAssignableFrom(returnType))return "int";
			else if(Boolean.TYPE.isAssignableFrom(returnType))return "boolean";
			else if(Double.TYPE.isAssignableFrom(returnType))return "double";		
			else if(String.class.isAssignableFrom(returnType))return "String";
		}
		
		
		return null;
	}
	
	private boolean isValidReturnType(Class<?> returnType){ return getReturnTypeString(returnType) != null; }
	
	public static void main(String[] args){
		ModelDescriptorFileGenerator generator = new ModelDescriptorFileGenerator();
		if(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_STANDARDFILEPATH)!= null){
			EpisimProperties.removeProperty(EpisimProperties.SIMULATOR_STANDARDFILEPATH);
		}
		generator.start();		
	}

}
