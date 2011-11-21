package sim.app.episim.persistence.dataconvert;

import java.lang.reflect.Method;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sim.app.episim.AbstractCell;
import sim.app.episim.UniversalCell;
import sim.app.episim.persistence.XmlFile;
import sim.util.Double2D;

public class XmlUniversalCell extends XmlObject<AbstractCell> {
	private static final String EPISIMBIOMECHANICALMODEL = "episimBiomechanicalModel";
	private static final String EPISIMCELLBEHAVIORALMODEL = "episimCellBehavioralModel";

	public XmlUniversalCell(AbstractCell cell) {
		this((UniversalCell) cell);
	}

	public XmlUniversalCell(UniversalCell cell) {
		super(cell);
	}

	public XmlUniversalCell(Node universalCellNode)
			throws ClassNotFoundException {
		super(universalCellNode);
		NodeList nl = universalCellNode.getChildNodes();
		// for (int i = 0; i < nl.getLength(); i++) {
		// Node subNode = nl.item(i);
		// if (subNode.getNodeName().equals("episimBiomechanicalModel")) {
		// episimBiomechanicalModel = new XmlEpisimBiomechanicalModel(
		// subNode);
		// } else if (subNode.getNodeName()
		// .equals("episimCellBehavioralModel")) {
		// episimCellBehavioralModel = new XmlEpisimCellBehavioralModel(
		// subNode);
		// }
		// }
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
		if (id instanceof String)
			return Long.parseLong((String) id);
		else
			return (Long) id;
	}

	@Override
	protected void exportSubXmlObjectsFromParameters() {
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
