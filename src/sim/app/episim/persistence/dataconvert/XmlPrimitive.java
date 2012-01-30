package sim.app.episim.persistence.dataconvert;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import sim.app.episim.persistence.ExportException;
import sim.app.episim.persistence.XmlFile;

public class XmlPrimitive extends XmlObject<Object> {

	private Object value;
	private static final String VALUE = "value";

	public XmlPrimitive(Object obj) throws ExportException {
		super(obj);
		value = obj;
	}

	public XmlPrimitive(Node objectNode) {
		super(objectNode);
	}

	@Override
	protected void exportSubXmlObjectsFromParameters() {

	}

	@Override
	public Element toXMLNode(String nodeName, XmlFile xmlFile) {
		if (value != null) {
			Element node = xmlFile.createElement(nodeName);
			node.setAttribute(VALUE, value.toString());
			return node; //TODO null Exportieren?
		} else
			return null;

	}

	@Override
	public void importParametersFromXml(Class<?> clazz) {
		if (getObjectNode() != null) {
			Node valueNode = getObjectNode().getAttributes()
					.getNamedItem(VALUE);
			if (valueNode != null)
				setObject(parse(valueNode.getNodeValue(), clazz));
		}
	}

	@Override
	public Object copyValuesToTarget(Object target) {
		return getObject();
	}

}
