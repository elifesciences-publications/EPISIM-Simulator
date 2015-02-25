package sim.app.episim.persistence;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import sim.app.episim.EpisimExceptionHandler;

public class XmlNodeConverter extends XmlFile {

	private File converted;

	public static void main(String[] args) {
		try {
			XmlNodeConverter xmlFile = new XmlNodeConverter(new File(
					"TestImageBasalLayer.xml"));
			NodeList rootNL = xmlFile.getRoot().getChildNodes();
			for (int i = 0; i < rootNL.getLength(); i++) {
				Node node = rootNL.item(i);
				if (node.getNodeName().equals("BasalLamina")) {
					Node importedTissueNode = xmlFile
							.createElement("importedtissue");

					NodeList basalLaminaNL = node.getChildNodes();
					for (int j = 0; j < basalLaminaNL.getLength(); j++) {
						Node basalLaminaNode = basalLaminaNL.item(j);
						if (basalLaminaNode.getNodeName().equals("Pixel")) {
							String x = "x", y = "y";
							NodeList PixelNL = basalLaminaNode.getChildNodes();
							for (int k = 0; k < PixelNL.getLength(); k++) {
								Node PixelNode = PixelNL.item(k);
								if (PixelNode.getNodeName().equals("X")) {
									x = PixelNode.getAttributes()
											.getNamedItem("value")
											.getNodeValue();
								} else if (PixelNode.getNodeName().equals("Y")) {
									y = PixelNode.getAttributes()
											.getNamedItem("value")
											.getNodeValue();
								}
							}
							Element pointElement = xmlFile
									.createElement("point");
							pointElement.setAttribute("x", x);
							pointElement.setAttribute("y", y);
							importedTissueNode.appendChild(pointElement);

						}
					}
					xmlFile.getRoot().appendChild(importedTissueNode);
					xmlFile.getRoot().removeChild(node);
				}
			}
			xmlFile.save();

		} catch (SAXException e){			
			EpisimExceptionHandler.getInstance().displayException(e);
		} catch (IOException e) {
			EpisimExceptionHandler.getInstance().displayException(e);
		} catch (ParserConfigurationException e) {
			EpisimExceptionHandler.getInstance().displayException(e);
		}
	}

	public XmlNodeConverter(File path) throws SAXException, IOException,
			ParserConfigurationException {
		super(path);
		converted = new File(path.getAbsolutePath() + "conv.xml");
	}

	protected void save() {
		super.save(converted);
	}

	public XmlNodeConverter(String rootNodeName)
			throws ParserConfigurationException, SAXException {
		super(rootNodeName);
	}

}
