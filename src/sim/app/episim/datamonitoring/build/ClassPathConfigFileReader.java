package sim.app.episim.datamonitoring.build;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.gui.EpisimTextOut;
import binloc.ProjectLocator;


public class ClassPathConfigFileReader {
	
	private static final String LIB = "lib";
	private static final String FACTORY = "episimfactories";
	private static final String INTERFACE = "episiminterfaces";
	private static final String EXCEPTION = "episimexceptions";
	private static final String SIM = "sim";
	private static final String CONFIGFILENAME = "classpathconfig.xml";
	
	private String libClasspath;
	private String binClasspath;
	
	public ClassPathConfigFileReader() {
		libClasspath="";
		binClasspath="";
		loadXML();
	}
	
	private class XMLDefaultHandler extends DefaultHandler {

		private Set<String> libs;
		private Set<String> bins;

		public XMLDefaultHandler() {
			
			libs = new HashSet<String>();
			bins = new HashSet<String>();

		}
		 
		

		public void startDocument() throws SAXException {

		}

		public void startElement(String namespaceURI, String localName,
				String qName, Attributes attr) {

			if (qName.equalsIgnoreCase("lib")){
				if(attr.getValue("path") != null) libs.add(attr.getValue("path"));				
			} 
			if(qName.equalsIgnoreCase("bin")){
				if(attr.getValue("path") != null) bins.add(attr.getValue("path"));
			}
		}
		
		public void endDocument() {

		}

		public void endElement(String namespaceURI, String localName, String qName) {

		}
		public Set<String> getLibs(){ return this.libs; }
		public Set<String> getBins(){ return this.bins; }
	
	}
	
	
	private void loadXML(){
      
		try{
			File configFile = new File(getLibPath().getAbsolutePath().concat(System.getProperty("file.separator")).concat(CONFIGFILENAME));
			SAXParserFactory spf = SAXParserFactory.newInstance();
			spf.setNamespaceAware(true);
			SAXParser sp = spf.newSAXParser();
			XMLDefaultHandler defaultHandler = new XMLDefaultHandler();
			sp.parse(configFile, defaultHandler);
			this.binClasspath = "";
			this.libClasspath = "";			
	      this.binClasspath= buildClassPath(getBinPath(), defaultHandler.getBins());      
	      this.libClasspath= buildClassPath(getLibPath(), defaultHandler.getLibs());
		}
      catch (URISyntaxException e){
	      ExceptionDisplayer.getInstance().displayException(e);
      }
      catch (ParserConfigurationException e){
      	ExceptionDisplayer.getInstance().displayException(e);
      }
      catch (SAXException e){
      	ExceptionDisplayer.getInstance().displayException(e);
      }
      catch (IOException e){
      	ExceptionDisplayer.getInstance().displayException(e);
      }
	}
	
	
	
	private String buildClassPath(File path, Set<String> paths){
		String classpath = "";
		for(String str: paths){
			classpath = classpath.concat(path.getAbsolutePath()).concat(System.getProperty("file.separator")).concat(str).concat(getClasspathSeparatorChar());
		}
      return classpath;
	}
	
	public File getBinPath() throws URISyntaxException{
		return ProjectLocator.getBinPath();
	}
	
	public File getLibPath() throws URISyntaxException{
		return ProjectLocator.getPathOf(LIB);
	}
	
	



	
   public String getLibClasspath() {
   	   
   	return libClasspath;
   }



	
   public String getBinClasspath() {
   	
   	return binClasspath;
   }
	
	public String getClasspathSeparatorChar(){
		if(System.getProperty("os.name").toLowerCase().contains("windows")) return ";";
		if(System.getProperty("os.name").toLowerCase().contains("linux")|| System.getProperty("os.name").toLowerCase().contains("mac")) return ":";
		
		return ";";
	}

}
