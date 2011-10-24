package sim.app.episim.persistence;

import java.io.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;

import javax.xml.transform.dom.DOMSource;

import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import sim.app.episim.ExceptionDisplayer;

public class XmlFile {

	private Document document = null;


	public static void main(String[] args) {

	}

	protected XmlFile(File path) throws SAXException, IOException, ParserConfigurationException {
		readFile(path);
		if(document.getFirstChild() == null)
			throw new IOException("File "+path.getAbsolutePath()+" has no Data");
	}

	protected XmlFile(String rootNodeName) throws ParserConfigurationException, SAXException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		document = builder.newDocument();
		if(rootNodeName == null || rootNodeName.equals(""))
			throw new SAXException("rootNode can't be 'null' or empty");
		Element root = document.createElement(rootNodeName);
		document.appendChild(root);
	}

	private boolean readFile(File path) throws SAXException, IOException, ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		builder = factory.newDocumentBuilder();
		document = builder.parse(path);

		return document != null;
	}

	protected void save(File path) {
		DataOutputStream dos = null;
		try {
			dos = new DataOutputStream(new FileOutputStream(path));
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer();

			DOMSource source = new DOMSource(document);

			StreamResult result = new StreamResult(dos);
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(source, result);
			dos.close();

		} catch (TransformerException e) {
			ExceptionDisplayer.getInstance().displayException(e);
		} catch (FileNotFoundException e) {
			ExceptionDisplayer.getInstance().displayException(e);
		} catch (IOException e) {
			ExceptionDisplayer.getInstance().displayException(e);
		}
	}

	public Element createElement(String name){
		return document.createElement(name);
	}

	protected Element getRoot() {
		return document.getDocumentElement();
	}
}
