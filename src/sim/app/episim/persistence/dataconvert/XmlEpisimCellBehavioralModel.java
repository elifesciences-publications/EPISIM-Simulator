package sim.app.episim.persistence.dataconvert;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import episiminterfaces.EpisimCellBehavioralModel;
import sim.app.episim.model.sbml.SBMLModelConnector;
import sim.app.episim.persistence.ExportException;

public class XmlEpisimCellBehavioralModel extends
		XmlObject<EpisimCellBehavioralModel> {

	private static final String EPISIMSBMLMODELCONNECTOR = "episimSbmlModelConnector";

	public XmlEpisimCellBehavioralModel(
			EpisimCellBehavioralModel episimCellBehavioralModel) throws ExportException {
		super(episimCellBehavioralModel);
	}

	public XmlEpisimCellBehavioralModel(Node subNode) {
		super(subNode);

	}

	@Override
	protected void exportSubXmlObjectsFromParameters() throws ExportException {
		super.exportSubXmlObjectsFromParameters();
		Object subObj = getParameters().get(EPISIMSBMLMODELCONNECTOR);
		if (subObj instanceof SBMLModelConnector) {
			addSubXmlObject(EPISIMSBMLMODELCONNECTOR,
					new XmlSbmlModelConnector((SBMLModelConnector) subObj));
		} else throw new ExportException(getClass().getSimpleName()+": Parameter Export went wrong");
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
			connector.copyValuesToTarget((SBMLModelConnector) cellModel.getEpisimSbmlModelConnector());
		}
		if(cellModel != null) cellModel.updateAllSbmlModelParameterValuesFromConnector();
		return cellModel;
	}

}
