package sim.app.episim.persistence.dataconvert;

import java.util.HashMap;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sim.app.episim.persistence.ExportException;
import sim.app.episim.persistence.XmlFile;

public abstract class XmlHashMap<V> extends XmlObject<HashMap<Object, V>> {

	private HashMap<Node, Node> entryNodeMap;
	protected static final String KEY = "key";
	protected static final String VALUE = "value";
	private static final String ENTRY = "entry";

	public XmlHashMap(HashMap<Object, V> obj) throws ExportException {
		super(obj);
	}

	public XmlHashMap(Node objectNode) {
		super(objectNode);
	}

	@Override
	protected void exportSubXmlObjectsFromParameters() throws ExportException {
	}

	@Override
	public Element toXMLNode(String nodeName, XmlFile xmlFile)
			throws ExportException {
		if (entryNodeMap != null && entryNodeMap.size() > 0) {
			Element node = xmlFile.createElement(nodeName);
			for (Node keyNode : entryNodeMap.keySet()) {
				Element entryNode = xmlFile.createElement(ENTRY);
				Node valueNode = entryNodeMap.get(keyNode);
				if (keyNode != null) {
					entryNode.appendChild(keyNode);
					entryNode.appendChild(valueNode);
					node.appendChild(entryNode);
				}
			}

			return node;
		} else
			return null;
	}

	@Override
	protected void importParametersFromXml(Class<?> clazz) {
		NodeList hashMapNL = getObjectNode().getChildNodes();
		for (int i = 0; i < hashMapNL.getLength(); i++) {
			Node entryNode = hashMapNL.item(i);
			if (entryNode.getNodeName().equalsIgnoreCase(ENTRY)) {
				NodeList entryNL = entryNode.getChildNodes();
				Node keyNode = null, valueNode = null;
				for (int e = 0; e < hashMapNL.getLength(); e++) {
					Node node = hashMapNL.item(e);
					if (node.getNodeName().equalsIgnoreCase(KEY)) {
						keyNode = node;
					} else if (node.getNodeName().equalsIgnoreCase(VALUE)) {
						valueNode = node;
					}
				}
				if (keyNode != null && valueNode != null) {
					entryNodeMap.put(keyNode, valueNode);
				}
			}
		}
	}

	public HashMap<Node, Node> getEntryNodeMap() {
		return entryNodeMap;
	}

	public void addEntryNode(Node key, Node value) {
		this.entryNodeMap.put(key, value);
	}

//	@Override
//	public HashMap<Object, V> copyValuesToTarget(HashMap<Object, V> target) {
////		importParametersFromXml(null);
////		HashMap<Object, V> ret = target;
////		if (target == null) {
////			ret = new HashMap<Object, V>();
////		}
////		for (XmlPrimitive xmlKey : entryMap.keySet()) {
////			ret.put(xmlKey.copyValuesToTarget(null), entryMap.get(xmlKey)
////					.copyValuesToTarget(null));
////		}
////
////		return ret;
//	}

}
