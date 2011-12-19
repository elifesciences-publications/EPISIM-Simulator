package sim.app.episim.persistence;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import sim.app.episim.ExceptionDisplayer;

public class XmlFile {

	private Document document = null;

	public XmlFile(File path) throws SAXException, IOException,
			ParserConfigurationException {
		readFile(path);
		if (document.getFirstChild() == null)
			throw new IOException("File " + path.getAbsolutePath()
					+ " has no Data");
	}

	public XmlFile(String rootNodeName) throws ParserConfigurationException,
			SAXException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		document = builder.newDocument();
		if (rootNodeName == null || rootNodeName.equals(""))
			throw new SAXException("rootNode can't be 'null' or empty");
		Element root = document.createElement(rootNodeName);
		document.appendChild(root);
	}

	private boolean readFile(File path) throws SAXException, IOException,
			ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		builder = factory.newDocumentBuilder();
		document = builder.parse(path);

		return document != null;
	}

	public void save(File path) {
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

	public Element createElement(String name) {
		return document.createElement(name);
	}

	public Element getRoot() {
		return document.getDocumentElement();
	}

	public static void sortChildNodes(Node node, String[] nodeOrder) {

		List<Node> nodes = new ArrayList<Node>();
		NodeList childNodeList = node.getChildNodes();
		if (childNodeList.getLength() > 0 && nodeOrder != null) {
			for (int i = 0; i < childNodeList.getLength(); i++) {
				Node tNode = childNodeList.item(i);
				// Remove empty text nodes
				if ((!(tNode instanceof Text))
						|| (tNode instanceof Text && ((Text) tNode)
								.getTextContent().trim().length() > 1)) {
					nodes.add(tNode);
				}
			}
			
			for(String nodeRegEX : nodeOrder){
				ArrayList<Node> matches = new ArrayList<Node>();
				for(Node actNode : nodes){
					if(actNode.getNodeName().matches(nodeRegEX)){
						matches.add(actNode);
						node.appendChild(actNode);
					}
				}
			}
		}

	}
}
