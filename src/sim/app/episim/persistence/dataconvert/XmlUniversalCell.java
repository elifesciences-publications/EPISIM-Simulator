package sim.app.episim.persistence.dataconvert;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sim.app.episim.AbstractCell;
import sim.app.episim.UniversalCell;
import sim.app.episim.persistence.ExportException;
import sim.app.episim.persistence.XmlFile;

public class XmlUniversalCell extends XmlObject<AbstractCell> {
	private static final String EPISIMBIOMECHANICALMODEL = "episimBiomechanicalModel";
	private static final String EPISIMCELLBEHAVIORALMODEL = "episimCellBehavioralModel";

	public XmlUniversalCell(AbstractCell cell) throws ExportException {
		this((UniversalCell) cell);
	}

	public XmlUniversalCell(UniversalCell cell) throws ExportException {
		super(cell);
	}

	public XmlUniversalCell(Node universalCellNode) {
		super(universalCellNode);
		NodeList nl = universalCellNode.getChildNodes();
	}

	public long getId() {
		Object id = get("iD");
		if (id instanceof String)
			return Long.parseLong((String) id);
		else
			return (Long) id;
	}

	public long getMotherId() {
		Object id = get("motherId");
		if(id != null){
			if (id instanceof String)
				return Long.parseLong((String) id);
			else
				return (Long) id;
		}
		else return Long.MIN_VALUE;
	}
	
	@Override
	public Element toXMLNode(String nodeName, XmlFile xmlFile) throws ExportException {
		Element xmlNode = super.toXMLNode(nodeName, xmlFile);
		XmlFile.sortChildNodes(xmlNode, new String[]{EPISIMCELLBEHAVIORALMODEL, EPISIMBIOMECHANICALMODEL});
		return xmlNode;
	}

	@Override
	protected void exportSubXmlObjectsFromParameters() throws ExportException {
		super.exportSubXmlObjectsFromParameters();
		addSubXmlObject(EPISIMBIOMECHANICALMODEL,
				new XmlEpisimBiomechanicalModel(getObject()
						.getEpisimBioMechanicalModelObject()));
		addSubXmlObject(EPISIMCELLBEHAVIORALMODEL,
				new XmlEpisimCellBehavioralModel(getObject()
						.getEpisimCellBehavioralModelObject()));
	}

	@Override
	protected void importParametersFromXml(Class<?> clazz) {
		super.importParametersFromXml(clazz);
		NodeList nl = getObjectNode().getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node.getNodeName().equalsIgnoreCase(EPISIMBIOMECHANICALMODEL))
				addSubXmlObject(EPISIMBIOMECHANICALMODEL,
						new XmlEpisimBiomechanicalModel(node));
			if (node.getNodeName().equalsIgnoreCase(EPISIMCELLBEHAVIORALMODEL))
				addSubXmlObject(EPISIMCELLBEHAVIORALMODEL,
						new XmlEpisimCellBehavioralModel(node));
		}
	}

	public XmlEpisimBiomechanicalModel getEpisimBiomechanicalModel() {
		return (XmlEpisimBiomechanicalModel) getSubXmlObjects().get(
				EPISIMBIOMECHANICALMODEL);
	}

	public XmlEpisimCellBehavioralModel getEpisimCellBehavioralModel() {
		return (XmlEpisimCellBehavioralModel) getSubXmlObjects().get(
				EPISIMCELLBEHAVIORALMODEL);
	}

}
