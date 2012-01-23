package sim.app.episim.persistence.dataconvert;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import sim.app.episim.persistence.ExportException;
import sim.app.episim.persistence.XmlFile;
import sim.util.Double2D;
import sim.util.Double3D;

public class XmlDouble3D extends XmlObject<Double3D> {

	private static final String X_VALUE = "X";
	private static final String Y_VALUE = "Y";
	private static final String Z_VALUE = "Z";

	public XmlDouble3D(Double3D obj) throws ExportException {
		super(obj);
	}

	public XmlDouble3D(Node objectNode) {
		super(objectNode);
	}

	@Override
	protected void exportSubXmlObjectsFromParameters() {
	}

	@Override
	public Element toXMLNode(String nodeName, XmlFile xmlFile) {
		if (getObject() != null) {
			Element node = xmlFile.createElement(nodeName);
			node.setAttribute(X_VALUE, Double.toString(getObject().x));
			node.setAttribute(Y_VALUE, Double.toString(getObject().y));
			return node;
		} else
			return null;
	}

	@Override
	public void importParametersFromXml(Class<?> clazz) {
		if (getObjectNode() != null) {
			double x = 0, y = 0, z = 0;
			x = Double.parseDouble(getObjectNode().getAttributes()
					.getNamedItem(X_VALUE).getNodeValue());
			y = Double.parseDouble(getObjectNode().getAttributes()
					.getNamedItem(Y_VALUE).getNodeValue());
			z = Double.parseDouble(getObjectNode().getAttributes()
					.getNamedItem(Z_VALUE).getNodeValue());
			setObject(new Double3D(x, y, z));
		}
	}

	@Override
	public Double3D copyValuesToTarget(Double3D target) {
		return getObject();
	}

}
