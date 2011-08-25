package sim.app.episim.persistence;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import sim.app.episim.AbstractCell;
import sim.app.episim.persistence.SimulationStateData.CellObjectData;

public class SimulationStateFile extends XmlFile {

	public static void main(String[] args) {
		try {
			new SimulationStateFile();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private SimulationStateData simulationData = null;

	private static final String ROOT_NAME = "data_set";
	private static final String DATA_SOURCE = "data_source";
	private static final String GLOBALS = "globals";
	private static final String CELL_LIST = "cell_list";
	private static final String CELL_ID = "cell_id";
	private static final String GLOBAL_VARIABLES = "global_variables";
	private static final String MultiCellXML_VERSION = "MultiCellXML_version";

	private Element rootNode = null;
	private Node data_source = null; // TODO initialisieren
	private Node globals = null;
	private Node cell_list = null;
	private Node global_variables = null;

	public SimulationStateFile(File path) throws SAXException, IOException,
			ParserConfigurationException {
		super(path);
		rootNode = getRoot();
		if (!rootNode.getNodeName().equals(ROOT_NAME))
			throw new IOException("Wrong file format: "
					+ path.getAbsolutePath());
		NodeList levelOneChildren = rootNode.getChildNodes();
		for (int i = 0; i < levelOneChildren.getLength(); i++) {
			Node cNode = levelOneChildren.item(i);
			if (cNode.getNodeName().equals(DATA_SOURCE)) {
				data_source = cNode;
			} else if (cNode.getNodeName().equals(GLOBALS)) {
				globals = cNode;
			} else if (cNode.getNodeName().equals(CELL_LIST)) {
				cell_list = cNode;
			} else if (cNode.getNodeName().equals(GLOBAL_VARIABLES)) {
				global_variables = cNode;
			}
		}
	}

	public SimulationStateFile() throws ParserConfigurationException,
			SAXException {
		super(ROOT_NAME);
		rootNode = getRoot();
		rootNode.setAttribute(MultiCellXML_VERSION, "1.0");
		// initialize level One Nodes
		data_source = createElement(DATA_SOURCE);
		rootNode.appendChild(data_source);
		globals = createElement(GLOBALS);
		rootNode.appendChild(globals);
		cell_list = createElement(CELL_LIST);
		rootNode.appendChild(cell_list);
		global_variables = createElement(GLOBAL_VARIABLES);
		rootNode.appendChild(global_variables);
	}

	private void loadData() {

	}

	private Element convertObjectToNode(String ElementName, Object obj) {
		Element node = createElement(ElementName);
		if (obj.getClass().isPrimitive()) {
			node.setTextContent(obj + "");
		} else if (obj instanceof Collection) {
			Collection listObj = (Collection) obj;
			if (listObj.size() > 0) {
				node.setAttribute("list", listObj.toArray()[0].getClass()
						.getName());
				for (Object listElement : listObj) {
					node.appendChild(convertObjectToNode("listElement",
							listElement));
				}

			}
		} else if (obj instanceof AbstractCell) {
			node.setAttribute("type", CELL_ID);
			node.setTextContent(((AbstractCell) obj).getID() + "");
		} else {
			node.setTextContent(obj.toString());
		}
		return node;
	}

	public void saveData(File path) {
		SimulationStateData.getInstance().updateData();
		for (CellObjectData cell : SimulationStateData.getInstance().cells) {
			
			Element cellElement = createElement("cell");
			for (String parameter : cell.cellData.keySet()) {
				Object o = cell.cellData.get(parameter);
				if (o != null)
					cellElement.appendChild(convertObjectToNode(parameter, o));
			}
			
			Element bioMechanicalModelObjectDataElement = createElement("bioMechanicalModel");
			for (String parameter : cell.bioMechanicalModelObjectData.keySet()) {
				Object o = cell.cellData.get(parameter);
				if (o != null)
					bioMechanicalModelObjectDataElement.appendChild(convertObjectToNode(parameter, o));
			}
			
			Element cellBehavioralModelObjectElement = createElement("cellBehavioralModel");
			for (String parameter : cell.cellBehavioralModelObjectData.keySet()) {
				Object o = cell.cellData.get(parameter);
				if (o != null)
					cellBehavioralModelObjectElement.appendChild(convertObjectToNode(parameter, o));
			}
			
			cellElement.appendChild(cellBehavioralModelObjectElement);
			cellElement.appendChild(bioMechanicalModelObjectDataElement);
			
			cell_list.appendChild(cellElement);

		}
		save(path);
	}
}
