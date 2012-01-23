package sim.app.episim.persistence.dataconvert;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import sim.app.episim.persistence.ExportException;
import sim.app.episim.persistence.XmlFile;
import sim.util.Double2D;
import sim.util.Int2D;

public class XmlInt2D extends XmlObject<Int2D> {

	private static final String X_VALUE = "X";
	private static final String Y_VALUE = "Y";

	public XmlInt2D(Int2D obj) throws ExportException {
		super(obj);
	}

	public XmlInt2D(Node objectNode) {
		super(objectNode);
	}

	@Override
	protected void exportSubXmlObjectsFromParameters() {
	}

	@Override
	public Element toXMLNode(String nodeName, XmlFile xmlFile) {
		if (getObject() != null) {
			Element node = xmlFile.createElement(nodeName);
			node.setAttribute(X_VALUE, Integer.toString(getObject().x));
			node.setAttribute(Y_VALUE, Integer.toString(getObject().y));
			return node;
		} else
			return null;
	}

	@Override
	public void importParametersFromXml(Class<?> clazz) {
		if(getObjectNode() != null){
			int x = 0, y = 0;
			x = Integer.parseInt(getObjectNode().getAttributes().getNamedItem(X_VALUE).getNodeValue());
			y = Integer.parseInt(getObjectNode().getAttributes().getNamedItem(Y_VALUE).getNodeValue());
		 setObject(new Int2D(x,y));
		}
	}

	@Override
	public Int2D copyValuesToTarget(Int2D target) {
		return getObject();
	}

}
