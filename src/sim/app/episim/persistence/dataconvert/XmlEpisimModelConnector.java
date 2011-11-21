package sim.app.episim.persistence.dataconvert;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import episimbiomechanics.EpisimModelConnector;

public class XmlEpisimModelConnector extends XmlObject<EpisimModelConnector> {

	public XmlEpisimModelConnector(EpisimModelConnector obj) {
		super(obj);
		// TODO Auto-generated constructor stub
	}

	public XmlEpisimModelConnector(Node obj) {
		super(obj);
		// TODO Auto-generated constructor stub
	}
	
//	@Override
//	protected void exportSubXmlObjectsFromParameters() {
//		super.exportSubXmlObjectsFromParameters();
//		addSubXmlObject(EPISIMBIOMECHANICALMODEL,
//				new XmlEpisimBiomechanicalModel(getObject()
//						.getEpisimBioMechanicalModelObject()));
//		addSubXmlObject(EPISIMCELLBEHAVIORALMODEL,
//				new XmlEpisimBiomechanicalModel(getObject()
//						.getEpisimBioMechanicalModelObject()));
//	}
//
//	@Override
//	protected void importParametersFromXml(Class<?> clazz) {
//		super.importParametersFromXml(clazz);
//		NodeList nl = getObjectNode().getChildNodes();
//		for (int i = 0; i < nl.getLength(); i++) {
//			Node node = nl.item(i);
//			if(node.getNodeName().equalsIgnoreCase(EPISIMBIOMECHANICALMODEL))
//				addSubXmlObject(EPISIMBIOMECHANICALMODEL, new XmlEpisimBiomechanicalModel(node));
//			if(node.getNodeName().equalsIgnoreCase(EPISIMCELLBEHAVIORALMODEL))
//				addSubXmlObject(EPISIMCELLBEHAVIORALMODEL, new XmlEpisimCellBehavioralModel(node));
//		}
//		// addSubXmlObject(EPISIMBIOMECHANICALMODEL, subXmlObject)
//	}

}
