package sim.app.episim.persistence.dataconvert;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import episiminterfaces.EpisimCellBehavioralModel;
import episiminterfaces.EpisimSbmlModelConnector;

import sim.app.episim.model.sbml.SBMLModelState;
import sim.app.episim.model.sbml.SbmlModelConnector;

public class XmlSbmlModelConnector extends XmlObject<SbmlModelConnector> {

	private static final String SBMLMODELSTATES = "sbmlModelStates";

	public XmlSbmlModelConnector(SbmlModelConnector obj) {
		super(obj);
	}

	public XmlSbmlModelConnector(Node objectNode) {
		super(objectNode);
	}

	@Override
	protected void exportSubXmlObjectsFromParameters() {
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
	public SbmlModelConnector copyValuesToTarget(SbmlModelConnector target) {
//		SbmlModelConnector modelConnector = super.copyValuesToTarget(target);
		
		XmlObject<?> xmlObj = getSubXmlObjects().get(SBMLMODELSTATES);
		
		if(xmlObj != null && xmlObj instanceof XmlSbmlModelStatesHashMap){
			XmlSbmlModelStatesHashMap statesMap = (XmlSbmlModelStatesHashMap)xmlObj;
			statesMap.copyValuesToTarget(target.getSBMLModelStateMap());
		}
		
		return target;
	}

}
