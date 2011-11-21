package sim.app.episim.persistence.dataconvert;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import episimbiomechanics.EpisimModelConnector;
import episiminterfaces.EpisimCellBehavioralModel;
import episiminterfaces.EpisimSbmlModelConnector;

import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.sbml.SbmlModelConnector;
import sim.app.episim.persistence.XmlFile;

public class XmlEpisimCellBehavioralModel extends
		XmlObject<EpisimCellBehavioralModel> {

	private static final String EPISIMSBMLMODELCONNECTOR = "episimSbmlModelConnector";

	public XmlEpisimCellBehavioralModel(
			EpisimCellBehavioralModel episimCellBehavioralModel) {
		super(episimCellBehavioralModel);
	}

	public XmlEpisimCellBehavioralModel(Node subNode) {
		super(subNode);

	}

	@Override
	protected void exportSubXmlObjectsFromParameters() {
		super.exportSubXmlObjectsFromParameters();
		Object subObj = getParameters().get(EPISIMSBMLMODELCONNECTOR);
		if (subObj instanceof SbmlModelConnector) {
			addSubXmlObject(EPISIMSBMLMODELCONNECTOR,
					new XmlSbmlModelConnector((SbmlModelConnector) subObj));
		}

		getObject().getEpisimSbmlModelConnector();
	}

	@Override
	protected void importParametersFromXml(Class<?> clazz) {
		super.importParametersFromXml(clazz);
		NodeList nl = getObjectNode().getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node.getNodeName().equalsIgnoreCase(EPISIMSBMLMODELCONNECTOR))
				addSubXmlObject(EPISIMSBMLMODELCONNECTOR,
						new XmlSbmlModelConnector(node));
		}
	}
	
	@Override
	public EpisimCellBehavioralModel copyValuesToTarget(
			EpisimCellBehavioralModel target) {
		EpisimCellBehavioralModel cellModel = super.copyValuesToTarget(target);
		
		XmlObject<?> xmlObj = getSubXmlObjects().get(EPISIMSBMLMODELCONNECTOR);
		
		if(xmlObj != null && xmlObj instanceof XmlSbmlModelConnector){
			XmlSbmlModelConnector connector = (XmlSbmlModelConnector)xmlObj;
			connector.copyValuesToTarget((SbmlModelConnector) cellModel.getEpisimSbmlModelConnector());
		}
		
		return cellModel;
	}

}
