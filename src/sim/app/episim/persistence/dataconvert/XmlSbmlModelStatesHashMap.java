package sim.app.episim.persistence.dataconvert;

import java.util.HashMap;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sim.app.episim.model.sbml.SBMLModelState;
import sim.app.episim.persistence.ExportException;
import sim.app.episim.persistence.XmlFile;

public class XmlSbmlModelStatesHashMap extends
		XmlObject<HashMap<String, SBMLModelState>> {

	private static final String KEY = "filename";
	private static final String ENTRY = "modelState";

	private HashMap<String, XmlSBMLModelState> xmlObjectMap;

	public XmlSbmlModelStatesHashMap(HashMap<String, SBMLModelState> obj) throws ExportException {
		super(obj);
	}

	public XmlSbmlModelStatesHashMap(Node node) {
		super(node);
	}

	@Override
	protected void exportSubXmlObjectsFromParameters() throws ExportException {
		xmlObjectMap = new HashMap<String, XmlSBMLModelState>();
		for (String key : getObject().keySet()) {
			xmlObjectMap.put(key, new XmlSBMLModelState(getObject().get(key)));
		}
	}

	@Override
	public Element toXMLNode(String nodeName, XmlFile xmlFile) {
		if (xmlObjectMap != null && xmlObjectMap.size() > 0) {
			Element node = xmlFile.createElement(nodeName);
			for (String key : xmlObjectMap.keySet()) {
				Element subNode = xmlObjectMap.get(key).toXMLNode(ENTRY,
						xmlFile);
				subNode.setAttribute(KEY, key);
				if (subNode != null)
					node.appendChild(subNode);
			}

			return node;
		} else
			return null;
	}

	@Override
	protected void importParametersFromXml(Class<?> clazz) {
		xmlObjectMap = new HashMap<String, XmlSBMLModelState>();
		NodeList nl = getObjectNode().getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node.getNodeName().equalsIgnoreCase(ENTRY)) {
				Node keyNode = node.getAttributes().getNamedItem(KEY);
				if (keyNode != null) {
					String key = keyNode.getNodeValue();
					xmlObjectMap.put(key, new XmlSBMLModelState(node));
				}
			}
		}
	}

	@Override
	public HashMap<String, SBMLModelState> copyValuesToTarget(
			HashMap<String, SBMLModelState> target) {
		importParametersFromXml(null);
		for(String modelStateKey : target.keySet()){
			XmlSBMLModelState xmlModelState = xmlObjectMap.get(modelStateKey);
			SBMLModelState modelState = target.get(modelStateKey);
			if(xmlModelState != null && modelState != null){
				xmlModelState.copyValuesToTarget(modelState);
			}
		}
		
		return target;
	}

}
