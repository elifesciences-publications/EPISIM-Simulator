package sim.app.episim.persistence.dataconvert;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import sim.app.episim.persistence.XmlFile;
import sim.util.Double2D;

public class XmlDouble2D extends XmlObject<Double2D> {

	private static final String X_VALUE = "X";
	private static final String Y_VALUE = "Y";

	public XmlDouble2D(Double2D obj) {
		super(obj);
	}

	public XmlDouble2D(Node objectNode) {
		super(objectNode);
	}

	@Override
	protected void exportSubXmlObjectsFromParameters() {
	}

	@Override
	public Node toXMLNode(String nodeName, XmlFile xmlFile) {
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
		if(getObjectNode() != null){
			double x = 0, y = 0;
			x = Double.parseDouble(getObjectNode().getAttributes().getNamedItem(X_VALUE).getNodeValue());
			y = Double.parseDouble(getObjectNode().getAttributes().getNamedItem(Y_VALUE).getNodeValue());
		 setObject(new Double2D(x,y));
		}
	}

	@Override
	public Double2D copyValuesToTarget(Double2D target) {
		return getObject();
	}

}
