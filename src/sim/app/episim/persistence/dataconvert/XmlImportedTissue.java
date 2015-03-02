package sim.app.episim.persistence.dataconvert;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sim.app.episim.persistence.ExportException;
import sim.app.episim.persistence.XmlFile;
import sim.app.episim.tissueimport.ImportedTissue;

public class XmlImportedTissue extends XmlObject<ImportedTissue> {

	private static final String POINT = "point";
	private static final String Y = "y";
	private static final String X = "x";

	private ArrayList<Point2D> basalLayer = new ArrayList<Point2D>();

	public XmlImportedTissue(ImportedTissue obj) throws ExportException {
		super(obj);
	}

	public XmlImportedTissue(Node objectNode) {
		super(objectNode);
	}

	@Override
	protected void exportSubXmlObjectsFromParameters() {
	}

	@Override
	public Element toXMLNode(String nodeName, XmlFile xmlFile) {
		if(getObject()==null)return null;
		Element node = xmlFile.createElement(nodeName);
		for (Point2D point : getObject().getBasalLayerPoints()) {
			Element pointElement = xmlFile.createElement(POINT);
			pointElement.setAttribute(X, Double.toString(point.getX()));
			pointElement.setAttribute(Y, Double.toString(point.getY()));
			node.appendChild(pointElement);
		}
		return node;
	}

	@Override
	protected void importParametersFromXml(Class<?> clazz) {
		super.importParametersFromXml(clazz);
		basalLayer = new ArrayList<Point2D>();
		NodeList nl = getObjectNode().getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node.getNodeName().equalsIgnoreCase(POINT)) {
				Node xNode = node.getAttributes().getNamedItem(X);
				Node yNode = node.getAttributes().getNamedItem(Y);
				if (xNode != null && yNode != null) {
					basalLayer
							.add(new Point2D.Double((Double) parse(
									xNode.getNodeValue(), Double.TYPE),
									(Double) parse(yNode.getNodeValue(),
											Double.TYPE)));
				}
			}
		}
	}

	@Override
	public ImportedTissue copyValuesToTarget(ImportedTissue target) {
		super.copyValuesToTarget(target);
		//importParametersFromXml(null);
		target.setBasalLayerPoints(basalLayer);
		return target;
	}

}
