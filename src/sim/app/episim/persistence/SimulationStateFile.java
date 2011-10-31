package sim.app.episim.persistence;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import sim.app.episim.AbstractCell;
import sim.app.episim.persistence.dataconvert.XmlUniversalCell;

public class SimulationStateFile extends XmlFile {

	private static final String ROOT_NAME = "data_set";
	private static final String CELLBEHAVIORALMODEL_FILE = "model_file";
	private static final String MultiCellXML_VERSION = "MultiCellXML_version";
	private static final String CELLS = "cells";

	private Element rootNode = null;

	public SimulationStateFile(File path) throws SAXException, IOException, ParserConfigurationException {
		super(path);
		rootNode = getRoot();
		if (!rootNode.getNodeName().equals(ROOT_NAME))
			throw new IOException("Wrong file format: " + path.getAbsolutePath());

	}

	public SimulationStateFile() throws ParserConfigurationException, SAXException {
		super(ROOT_NAME);
		rootNode = getRoot();
		rootNode.setAttribute(MultiCellXML_VERSION, "1.0");
	}

	public SimulationStateData loadData() {
		Node behaviorFile = getRoot().getElementsByTagName(CELLBEHAVIORALMODEL_FILE).item(0);
		SimulationStateData simStateData = new SimulationStateData();
//		simStateData.setLoadedModelFile(behaviorFile.getTextContent());
		simStateData.setLoadedModelFile(behaviorFile.getAttributes().getNamedItem("modelfile").getNodeValue());

		NodeList nodes = getRoot().getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {

			if (nodes.item(i).getNodeName().equalsIgnoreCase(CELLS)) {
				NodeList cellNodes = nodes.item(i).getChildNodes();

				for (int j = 0; j < cellNodes.getLength(); j++) {
					XmlUniversalCell xmlCell;
					Node cellNode = cellNodes.item(j);
					if (cellNode.getNodeName().equalsIgnoreCase("cell"))
						try {
							xmlCell = new XmlUniversalCell(cellNode);
							simStateData.addCell(xmlCell);
						} catch (ClassNotFoundException e) {
						}

				}

			}

		}
		return simStateData;
	}

	public void saveData(File path) {

		SimulationStateData simStateData = new SimulationStateData();
		simStateData.updateData();

		Element modelFileElement = createElement(CELLBEHAVIORALMODEL_FILE);
//		modelFileElement.setTextContent(simStateData.getLoadedModelFile().getAbsolutePath());
		modelFileElement.setAttribute("modelfile", simStateData.getLoadedModelFile().getAbsolutePath());
		getRoot().appendChild(modelFileElement);

		getRoot().appendChild(cellListToXML(simStateData.getCells(), CELLS));
		//
		// getRoot().appendChild(converter.objectToXML(SimulationStateData.getInstance().getCellContinuous(),CELLCONTINUOUS));
		//
		// getRoot().appendChild(converter.objectToXML(SimulationStateData.getInstance().getDeltaInfo(),DELTAINFO));
		//
		// getRoot().appendChild(converter.objectToXML(SimulationStateData.getInstance().getEpisimBioMechanicalModelGlobalParameters(),EPISIMBIOMECHANICALMODELGLOBALPARAMETERS));
		//
		// getRoot().appendChild(converter.objectToXML(SimulationStateData.getInstance().getEpisimCellBehavioralModelGlobalParameters(),EPISIMCELLBEHAVIORALMODELGLOBALPARAMETERS));
		//
		// getRoot().appendChild(converter.objectToXML(SimulationStateData.getInstance().getMiscalleneousGlobalParameters(),MISCALLENEOUSGLOBALPARAMETERS));
		//
		// getRoot().appendChild(converter.objectToXML(SimulationStateData.getInstance().getTimeSteps(),TIMESTEPS));
		//
		// getRoot().appendChild(converter.objectToXML(SimulationStateData.getInstance().getWoundRegionCoordinates(),WOUNDREGIONCOORDINATES));
		save(path);
	}

	public Node cellListToXML(ArrayList<XmlUniversalCell> cells, String nodeName) {
		Element cellsNode = createElement(nodeName);
		for (XmlUniversalCell xCell : cells) {
			cellsNode.appendChild(xCell.toXMLNode("cell", this));
		}
		return cellsNode;
	}

	private Element convertObjectToNode(String ElementName, Object obj) {
		Element node = createElement(ElementName);
		if (obj.getClass().isPrimitive()) {
			node.setTextContent(obj + "");
		} else if (obj instanceof Collection) {
			Collection listObj = (Collection) obj;
			if (listObj.size() > 0) {
				node.setAttribute("list", listObj.toArray()[0].getClass().getName());
				for (Object listElement : listObj) {
					node.appendChild(convertObjectToNode("listElement", listElement));
				}

			}
		} else if (obj instanceof AbstractCell) {
			node.setAttribute("type", CELLS);
			node.setTextContent(((AbstractCell) obj).getID() + "");
		} else {
			node.setTextContent(obj.toString());
		}
		return node;
	}

}
