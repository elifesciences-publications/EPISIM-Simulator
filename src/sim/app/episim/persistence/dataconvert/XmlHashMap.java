package sim.app.episim.persistence.dataconvert;

import java.util.HashMap;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sim.app.episim.persistence.ExportException;
import sim.app.episim.persistence.XmlFile;

/*
 * TODO Importieren funktioniert nicht!
 */
public class XmlHashMap<K,V> extends XmlObject<HashMap<K, V>> {

	private HashMap<XmlPrimitive, XmlObject<V>> entryMap;
	private static final String KEY = "key";
	private static final String VALUE = "value";
	private static final String ENTRY = "entry";

	public XmlHashMap(HashMap<K, V> obj) throws ExportException {
		super(obj);
	}

	public XmlHashMap(Node objectNode) {
		super(objectNode);
	}

	@Override
	protected void exportSubXmlObjectsFromParameters() throws ExportException {
		entryMap = new HashMap<XmlPrimitive, XmlObject<V>>();
		for (K key : getObject().keySet()) {
			entryMap.put(new XmlPrimitive(key), new XmlObject<V>(getObject()
					.get(key)));
		}
	}

	@Override
	public Element toXMLNode(String nodeName, XmlFile xmlFile)
			throws ExportException {
		if (entryMap != null && entryMap.size() > 0) {
			Element node = xmlFile.createElement(nodeName);
			for (XmlPrimitive key : entryMap.keySet()) {
				Element entryNode = xmlFile.createElement(ENTRY);
				Element keyNode = key.toXMLNode(KEY, xmlFile);
				Element valueNode = entryMap.get(key).toXMLNode(VALUE, xmlFile);
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
					entryMap.put(new XmlPrimitive(keyNode), new XmlObject<V>(
							valueNode));
				}
			}
		}
	}

	@Override
	public HashMap<K, V> copyValuesToTarget(HashMap<K, V> target) {
		importParametersFromXml(null);
		HashMap<K, V> ret = target;
		if (target == null) {
			ret = new HashMap<K, V>();

		}
		for (XmlPrimitive xmlKey : entryMap.keySet()) {
			ret.put((K)xmlKey.copyValuesToTarget(null), entryMap.get(xmlKey)
					.copyValuesToTarget(null));
		}

		return ret;
	}

}
