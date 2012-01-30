package sim.app.episim.persistence.dataconvert;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import sim.app.episim.persistence.ExportException;
import sim.app.episim.persistence.XmlFile;
import sim.util.Int3D;

public class XmlInt3D extends XmlObject<Int3D> {

	private static final String X_VALUE = "X";
	private static final String Y_VALUE = "Y";
	private static final String Z_VALUE = "Z";

	public XmlInt3D(Int3D obj) throws ExportException {
		super(obj);
	}

	public XmlInt3D(Node objectNode) {
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
			node.setAttribute(Z_VALUE, Integer.toString(getObject().z));
			return node;
		} else
			return null;
	}

	@Override
	public void importParametersFromXml(Class<?> clazz) {
		if(getObjectNode() != null){
			int x = 0, y = 0, z= 0;
			x = Integer.parseInt(getObjectNode().getAttributes().getNamedItem(X_VALUE).getNodeValue());
			y = Integer.parseInt(getObjectNode().getAttributes().getNamedItem(Y_VALUE).getNodeValue());
			z = Integer.parseInt(getObjectNode().getAttributes().getNamedItem(Z_VALUE).getNodeValue());
		 setObject(new Int3D(x,y,z));
		}
	}

	@Override
	public Int3D copyValuesToTarget(Int3D target) {
		return getObject();
	}

}
