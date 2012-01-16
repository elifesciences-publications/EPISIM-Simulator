package sim.app.episim.persistence.dataconvert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sim.app.episim.model.sbml.SBMLModelEntity;
import sim.app.episim.model.sbml.SBMLModelState;
import sim.app.episim.persistence.ExportException;
import sim.app.episim.persistence.ImportLog;
import sim.app.episim.persistence.XmlFile;

public class XmlSBMLModelState extends XmlObject<SBMLModelState> {

	private static final String NAME = "name";
	private static final String VALUE = "value";
	private static final String CONCENTRATION = "concentration";

	private static final String SPECIES_LIST = "speciesList";
	private static final String PARAMETERS_LIST = "parametersList";
	private static final String REACTIONS_LIST = "reactionsList";

	private static final String SPECIES = "species";
	private static final String PARAMETER = "parameter";
	private static final String REACTION = "reaction";

	private List<SBMLModelEntity> speciesSubObjects;
	private List<SBMLModelEntity> parametersSubObjects;
	private List<SBMLModelEntity> reactionsSubObjects;

	public XmlSBMLModelState(Node objectNode) {
		super(objectNode);
	}

	public XmlSBMLModelState(SBMLModelState modelState) throws ExportException {
		super(modelState);
	}

	@Override
	protected void exportSubXmlObjectsFromParameters() {
		speciesSubObjects = new ArrayList<SBMLModelEntity>();
		parametersSubObjects = new ArrayList<SBMLModelEntity>();
		reactionsSubObjects = new ArrayList<SBMLModelEntity>();

		for (SBMLModelEntity reaction : getObject().getSpeciesValues()) {
			speciesSubObjects.add((reaction));
		}
		for (SBMLModelEntity parameter : getObject().getParameterValues()) {
			parametersSubObjects.add((parameter));
		}
		for (SBMLModelEntity species : getObject().getReactionValues()) {
			reactionsSubObjects.add((species));
		}
	}

	@Override
	public Element toXMLNode(String nodeName, XmlFile xmlFile) {
		Element node = xmlFile.createElement(nodeName);
		Element speciesNode = createListElement(SPECIES_LIST, SPECIES,
				speciesSubObjects, xmlFile);
		Element reactionsNode = createListElement(PARAMETERS_LIST, PARAMETER,
				parametersSubObjects, xmlFile);
		Element parametersNode = createListElement(REACTIONS_LIST, REACTION,
				reactionsSubObjects, xmlFile);
		if (speciesNode != null)
			node.appendChild(speciesNode);
		if (parametersNode != null)
			node.appendChild(parametersNode);
		if (reactionsNode != null)
			node.appendChild(reactionsNode);
		return node;

	}

	private Element createListElement(String nodeName, String subNodeName,
			List<SBMLModelEntity> objectList, XmlFile xmlFile) {
		if (objectList != null && objectList.size() > 0) {

			Element listNode = xmlFile.createElement(nodeName);
			for (SBMLModelEntity entity : objectList) {
				Element subNode = xmlFile.createElement(subNodeName);
				subNode.setAttribute(NAME, entity.name);
				subNode.setAttribute(VALUE, Double.toString(entity.value));
				subNode.setAttribute(CONCENTRATION,
						Double.toString(entity.concentration));
				if (subNode != null)
					listNode.appendChild(subNode);
			}

			return listNode;
		}
		return null;
	}

	@Override
	protected void importParametersFromXml(Class<?> clazz) {
	}

	private SBMLModelEntity convertNodeToSBMLModelEntity(Node entityNode) {
		String name = "NONAME!";
		double value = 0;
		double concentration = 0;
		Node nameNode = entityNode.getAttributes().getNamedItem(NAME);
		Node valueNode = entityNode.getAttributes().getNamedItem(VALUE);
		Node concentrationNode = entityNode.getAttributes().getNamedItem(
				CONCENTRATION);
		if (nameNode != null)
			name = nameNode.getNodeValue();
		if (valueNode != null)
			value = Double.parseDouble(valueNode.getNodeValue());
		if (concentrationNode != null)
			concentration = Double
					.parseDouble(concentrationNode.getNodeValue());
		return new SBMLModelEntity(name, value, concentration);
	}

	private void convertNodeToTarget(Node entityListNode, SBMLModelState target) {
		NodeList nl = entityListNode.getChildNodes();
		ImportLog.success(entityListNode);
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			
			if (node.getNodeName().equalsIgnoreCase(SPECIES)) {
				ImportLog.success(node);
				target.addSpeciesValue(convertNodeToSBMLModelEntity(node));
			} else if (node.getNodeName().equalsIgnoreCase(REACTION)) {
				ImportLog.success(node);
				target.addReactionValue(convertNodeToSBMLModelEntity(node));
			} else if (node.getNodeName().equalsIgnoreCase(PARAMETER)) {
				ImportLog.success(node);
				target.addParameterValue(convertNodeToSBMLModelEntity(node));
			}
		}
	}

	@Override
	public SBMLModelState copyValuesToTarget(SBMLModelState target) {
		List<SBMLModelEntity> entityList = new ArrayList<SBMLModelEntity>();
		NodeList nl = getObjectNode().getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node.getNodeName().equalsIgnoreCase(SPECIES_LIST)
					|| node.getNodeName().equalsIgnoreCase(REACTIONS_LIST)
					|| node.getNodeName().equalsIgnoreCase(PARAMETERS_LIST)) {
				convertNodeToTarget(node, target);
			}
		}
		return target;
	}
}
