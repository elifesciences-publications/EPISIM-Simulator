package sim.app.episim.persistence.dataconvert;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import episimbiomechanics.EpisimModelConnector;
import episiminterfaces.EpisimBiomechanicalModel;

import sim.app.episim.model.biomechanics.AbstractMechanical2DModel;
import sim.app.episim.model.biomechanics.AbstractMechanical3DModel;
import sim.app.episim.persistence.ExportException;

public class XmlEpisimBiomechanicalModel extends
		XmlObject<EpisimBiomechanicalModel> {

	private static final String EPISIMMODELCONNECTOR = "episimModelConnector";

	public XmlEpisimBiomechanicalModel(
			EpisimBiomechanicalModel episimBiomechanicalModel) throws ExportException {
		super(episimBiomechanicalModel);
	}

	public XmlEpisimBiomechanicalModel(Node subNode) {
		super(subNode);
	}

	@Override
	protected void exportSubXmlObjectsFromParameters() throws ExportException {
		super.exportSubXmlObjectsFromParameters();
		Object subObj = getParameters().get(EPISIMMODELCONNECTOR);
		if (subObj instanceof EpisimModelConnector) {
			addSubXmlObject(EPISIMMODELCONNECTOR, new XmlEpisimModelConnector(
					(EpisimModelConnector) subObj));
		} 
	}

	@Override
	protected void importParametersFromXml(Class<?> clazz) {
		super.importParametersFromXml(clazz);
		NodeList nl = getObjectNode().getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node.getNodeName().equalsIgnoreCase(EPISIMMODELCONNECTOR))
				addSubXmlObject(EPISIMMODELCONNECTOR,
						new XmlEpisimModelConnector(node));
		}
	}

	@Override
	public EpisimBiomechanicalModel copyValuesToTarget(
			EpisimBiomechanicalModel target) {
		AbstractMechanical2DModel mechModel2D = null;
		AbstractMechanical3DModel mechModel3D = null;

		if (target instanceof AbstractMechanical2DModel) {
			mechModel2D = (AbstractMechanical2DModel) super.copyValuesToTarget(target);
			
			XmlObject<?> xmlObj = getSubXmlObjects().get(EPISIMMODELCONNECTOR);
			
			if(xmlObj != null && xmlObj instanceof XmlEpisimModelConnector){
			XmlEpisimModelConnector connector = (XmlEpisimModelConnector) xmlObj;
			connector.copyValuesToTarget(mechModel2D.getEpisimModelConnector());
			return mechModel2D;
			}
		}
		else 	if (target instanceof AbstractMechanical3DModel) {
			mechModel3D = (AbstractMechanical3DModel) super.copyValuesToTarget(target);
			
			XmlObject<?> xmlObj = getSubXmlObjects().get(EPISIMMODELCONNECTOR);
			
			if(xmlObj != null && xmlObj instanceof XmlEpisimModelConnector){
			XmlEpisimModelConnector connector = (XmlEpisimModelConnector) xmlObj;
			connector.copyValuesToTarget(mechModel3D.getEpisimModelConnector());
			return mechModel3D;
			}
		}
		return target;
		
	}
}
