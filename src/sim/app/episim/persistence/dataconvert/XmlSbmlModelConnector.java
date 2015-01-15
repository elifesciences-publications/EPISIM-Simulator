package sim.app.episim.persistence.dataconvert;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sim.app.episim.model.sbml.SBMLModelConnector;
import sim.app.episim.persistence.ExportException;

public class XmlSbmlModelConnector extends XmlObject<SBMLModelConnector> {

	private static final String SBMLMODELSTATES = "sbmlModelStates";

	public XmlSbmlModelConnector(SBMLModelConnector obj) throws ExportException {
		super(obj);
	}

	public XmlSbmlModelConnector(Node objectNode) {
		super(objectNode);
	}

	@Override
	protected void exportSubXmlObjectsFromParameters() throws ExportException {
		addSubXmlObject(SBMLMODELSTATES, new XmlSbmlModelStatesHashMap(
				getObject().getSBMLModelStateMap()));
	}

	@Override
	protected void importParametersFromXml(Class<?> clazz) {
		NodeList nl = getObjectNode().getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node.getNodeName().equalsIgnoreCase(SBMLMODELSTATES))
				addSubXmlObject(SBMLMODELSTATES,
						new XmlSbmlModelStatesHashMap(node));
		}
	}
	
	@Override
	public SBMLModelConnector copyValuesToTarget(SBMLModelConnector target) {
		if(target == null)
			return null;
			
		importParametersFromXml(null);
		
		XmlObject<?> xmlObj = getSubXmlObjects().get(SBMLMODELSTATES);
		
		if(xmlObj != null && xmlObj instanceof XmlSbmlModelStatesHashMap){
			XmlSbmlModelStatesHashMap statesMap = (XmlSbmlModelStatesHashMap)xmlObj;
			statesMap.copyValuesToTarget(target.getSBMLModelStateMap());
		}
		
		return target;
	}

}
