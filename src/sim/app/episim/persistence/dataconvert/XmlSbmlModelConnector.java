package sim.app.episim.persistence.dataconvert;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sim.app.episim.model.sbml.SBMLModelState;
import sim.app.episim.model.sbml.SbmlModelConnector;

public class XmlSbmlModelConnector extends XmlObject<SbmlModelConnector> {
	
	private static final String SBMLMODELSTATES = "sbmlModelStates";

	public XmlSbmlModelConnector(SbmlModelConnector obj) {
		super(obj);
		// TODO Auto-generated constructor stub
	}

	public XmlSbmlModelConnector(Node objectNode) {
		super(objectNode);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void exportSubXmlObjectsFromParameters() {
//		addSubXmlObject(SBMLMODELSTATES,new XmlHashMap<SBMLModelState>(getObject().));
	}

	@Override
	protected void importParametersFromXml(Class<?> clazz) {
		// super.importParametersFromXml(clazz);
		// NodeList nl = getObjectNode().getChildNodes();
		// for (int i = 0; i < nl.getLength(); i++) {
		// Node node = nl.item(i);
		// if(node.getNodeName().equalsIgnoreCase(EPISIMBIOMECHANICALMODEL))
		// addSubXmlObject(EPISIMBIOMECHANICALMODEL, new
		// XmlEpisimBiomechanicalModel(node));
		// if(node.getNodeName().equalsIgnoreCase(EPISIMCELLBEHAVIORALMODEL))
		// addSubXmlObject(EPISIMCELLBEHAVIORALMODEL, new
		// XmlEpisimCellBehavioralModel(node));
		// }
		// addSubXmlObject(EPISIMBIOMECHANICALMODEL, subXmlObject)
	}

}
