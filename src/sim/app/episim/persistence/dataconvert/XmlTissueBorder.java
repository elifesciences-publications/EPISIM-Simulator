package sim.app.episim.persistence.dataconvert;

import java.util.HashMap;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sim.app.episim.model.sbml.SBMLModelState;
import sim.app.episim.persistence.ExportException;
import sim.app.episim.persistence.XmlFile;
import sim.app.episim.tissue.ImportedTissue;
import sim.app.episim.tissue.TissueBorder;
import sim.app.episim.tissue.TissueController;

public class XmlTissueBorder extends XmlObject<TissueBorder> {

	private static final String TISSUE_TYPE = "tissuetype";
	public static final String NOMEMBRANE = "nomembrane";
	public static final String STANDARD_MEMBRANE = "standardmembrane";
	public static final String IMPORTED_TISSUE = "importedtissue";
	private String tissueType = STANDARD_MEMBRANE;
	private XmlImportedTissue importedTissue;

	public XmlTissueBorder(TissueBorder obj) throws ExportException {
		super(obj);
	}

	public XmlTissueBorder(Node objectNode) {
		super(objectNode);
	}

	@Override
	protected void exportSubXmlObjectsFromParameters() throws ExportException {
		super.exportSubXmlObjectsFromParameters();
		this.importedTissue = new XmlImportedTissue(TissueController.getInstance().getTissueBorder().getImportedTissue());
	}

	@Override
	public Element toXMLNode(String nodeName, XmlFile xmlFile) throws ExportException {
		Element node = super.toXMLNode(nodeName, xmlFile);
		tissueType = IMPORTED_TISSUE;

		if (getObject().isStandardMembraneLoaded())
			tissueType = STANDARD_MEMBRANE;
		if (getObject().isNoMembraneLoaded())
			tissueType = NOMEMBRANE;

		node.setAttribute(TISSUE_TYPE, tissueType);
		if (importedTissue != null && tissueType.equals(IMPORTED_TISSUE))
			node.appendChild(importedTissue.toXMLNode(IMPORTED_TISSUE, xmlFile));
		return node;
	}

	public String getTissueType() {
		return get(TISSUE_TYPE).toString();
	}

	@Override
	protected void importParametersFromXml(Class<?> clazz) {
		super.importParametersFromXml(clazz);

		Node tissTypeNode = getObjectNode().getAttributes().getNamedItem(
				TISSUE_TYPE);
		if (tissTypeNode != null)
			tissueType = tissTypeNode.getNodeValue();

		NodeList nl = getObjectNode().getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node.getNodeName().equalsIgnoreCase(IMPORTED_TISSUE))
				importedTissue = new XmlImportedTissue(node);
		}
	}

	@Override
	public TissueBorder copyValuesToTarget(TissueBorder target) {
		super.copyValuesToTarget(target);
		if (tissueType.equals(NOMEMBRANE))
			target.loadNoMembrane();
		else if (tissueType.equals(STANDARD_MEMBRANE))
			target.loadStandardMembrane();
		else if (tissueType.equals(IMPORTED_TISSUE) && importedTissue != null) {
			target.setImportedTissue(importedTissue
					.copyValuesToTarget(new ImportedTissue()));

		}
		return target;
	}

}
