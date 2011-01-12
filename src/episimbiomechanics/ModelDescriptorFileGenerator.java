package episimbiomechanics;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

import episimbiomechanics.EpisimModelConnector.Hidden;

import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.gui.ExtendedFileChooser;
import sim.app.episim.util.GlobalClassLoader;
import binloc.ProjectLocator;


public class ModelDescriptorFileGenerator {
	
	private static final String PACKAGENAME = "episimbiomechanics";
	private File packagePath = null;
	public ModelDescriptorFileGenerator(){}
	
	private ArrayList<Class<? extends EpisimModelConnector>> getAvailableModelConnectors(){
		
		try{
	      packagePath = new File(ProjectLocator.class.getResource("../"+PACKAGENAME+"/").toURI());
      }
      catch (URISyntaxException e){
	      ExceptionDisplayer.getInstance().displayException(e);
      }
      if(packagePath != null){
      	
      	try{
	         GlobalClassLoader.getInstance().registerURL(packagePath.toURI().toURL());
            List<File> classFiles = new ArrayList<File>();
            ArrayList<Class<? extends EpisimModelConnector>> resultList = new ArrayList<Class<? extends EpisimModelConnector>>();
            getClassFiles(packagePath, classFiles);  
				for(File file: classFiles){
					  String fullFileName = getFullClassName(file);
					  if(fullFileName !=null){
			           Class<?> loadedClass = GlobalClassLoader.getInstance().loadClass(fullFileName.substring(0, (fullFileName.length()-".class".length())));
			           if(EpisimModelConnector.class.isAssignableFrom(loadedClass) && loadedClass!= EpisimModelConnector.class){
			         	   resultList.add((Class<? extends EpisimModelConnector>)loadedClass);
			           }
					  }
				}
				return resultList;
			}
         catch (ClassNotFoundException e){
            ExceptionDisplayer.getInstance().displayException(e);
         }
         catch (MalformedURLException e){
         	ExceptionDisplayer.getInstance().displayException(e);
         }
      	
      	
      }      
      return null;
	}
	
	private String getFullClassName(File file){
		if(file != null){
			String resultString ="";
			
			try{
				String path = file.getCanonicalPath();
				path = path.replace(System.getProperty("file.separator"),"/");
				String[] nameParts = path.split("/");
				boolean concatenationStarted = false;
				for(String namePart: nameParts){
					if(namePart!= null && namePart.length()>0){
						if(namePart.equals(PACKAGENAME)){
							concatenationStarted = true;
							resultString = resultString.concat(namePart);
						}
						else if(concatenationStarted){
							resultString = resultString.concat(".");
							resultString = resultString.concat(namePart);
						}
					}
				}
				return resultString;				
			}
			catch (IOException e){
				ExceptionDisplayer.getInstance().displayException(e);
			}
			
			
		}			
		return null;
	}
	
	private void getClassFiles(File rootPath, List<File> classFiles){		
      if(rootPath != null && rootPath.isDirectory()){
      	for(File file : rootPath.listFiles()){
      		if(file != null && file.isFile() && file.getName() != null && file.getName().endsWith(".class")){
      			classFiles.add(file);
      		}
      		else if(file != null && file.isDirectory()) getClassFiles(file, classFiles);
      	}
      }      
	}
	
	
	
	
	public void start(){
		ArrayList<Class<? extends EpisimModelConnector>> modelConnector = getAvailableModelConnectors();
		Class<? extends EpisimModelConnector> selectedModelConnector = null;
		if(modelConnector != null){
			selectedModelConnector = (Class<? extends EpisimModelConnector>)JOptionPane.showInputDialog(null, "Please select the Episim Model Connector Class", "Model Descriptor File Generator", JOptionPane.PLAIN_MESSAGE, null, modelConnector.toArray(), modelConnector.toArray()[0]);
		}
		if(selectedModelConnector != null){
			Document document = null;
			try{
				document = generateModelDescriptorDocument(selectedModelConnector);
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
					ExtendedFileChooser chooser = new ExtendedFileChooser(".xml");
					chooser.setCurrentDirectory(new File(path));
					chooser.setDialogTitle("Select Directory");
					chooser.setFileSelectionMode(ExtendedFileChooser.DIRECTORIES_ONLY);
					if(chooser.showSaveDialog(null)== ExtendedFileChooser.APPROVE_OPTION){
						File file = chooser.getSelectedFile();
						write(document, file.getCanonicalPath()+"/ModelDescriptor.xml");
					}
				}
				catch (IOException e){
					ExceptionDisplayer.getInstance().displayException(e);
				}
			}
		}
	}
	
	private Document generateModelDescriptorDocument(Class<? extends EpisimModelConnector> modelConnectorClass) throws DOMException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException, ParserConfigurationException{
		if(modelConnectorClass != null){
			Method[] methods = modelConnectorClass.getMethods();
			
			EpisimModelConnector actConnector = modelConnectorClass.newInstance();
			
			
			
			HashMap<String, Method> getterMethods = new HashMap<String, Method>();
			HashMap<String, Method> setterMethods = new HashMap<String, Method>();
			for(Method m : methods){
				if(m.getName().startsWith("get") && m.getAnnotation(Hidden.class) == null)getterMethods.put(m.getName().substring(3), m);				
				else if(m.getName().startsWith("set") && m.getAnnotation(Hidden.class) == null) setterMethods.put(m.getName().substring(3), m);
			}
			
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			
			DocumentBuilder builder = factory.newDocumentBuilder();
			
			Document document = builder.newDocument();
			document.setXmlVersion("1.0");
			
			Element rootElement = document.createElement("modeldescriptor");
			document.appendChild(rootElement);
			
			Element modelIdElement = document.createElement("modelid");
			modelIdElement.setAttribute("id", actConnector.getBiomechanicalModelId());
			rootElement.appendChild(modelIdElement);
			
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
		}
		
		
		return null;
	}
	
	private boolean isValidReturnType(Class<?> returnType){ return getReturnTypeString(returnType) != null; }
	
	public static void main(String[] args){
		ModelDescriptorFileGenerator generator = new ModelDescriptorFileGenerator();
		generator.start();		
	}

}