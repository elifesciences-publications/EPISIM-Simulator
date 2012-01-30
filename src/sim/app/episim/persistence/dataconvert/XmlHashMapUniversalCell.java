package sim.app.episim.persistence.dataconvert;

import java.util.HashMap;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import sim.app.episim.UniversalCell;
import sim.app.episim.persistence.ExportException;
import sim.app.episim.persistence.XmlFile;

public class XmlHashMapUniversalCell extends XmlHashMap<UniversalCell> {

	public XmlHashMapUniversalCell(HashMap<Object, UniversalCell> obj)
			throws ExportException {
		super(obj);
	}

	public XmlHashMapUniversalCell(Node objectNode) {
		super(objectNode);
	}
	
	@Override
	public Element toXMLNode(String nodeName, XmlFile xmlFile)
			throws ExportException {
		for(Object o : getObject().keySet()){
			XmlPrimitive xmlKey = new XmlPrimitive(o);
			XmlUniversalCell xCellValue = new XmlUniversalCell(getObject().get(o));
			Node keyNode = xmlKey.toXMLNode(KEY, xmlFile);
			Node valueNode = xCellValue.toXMLNode(VALUE, xmlFile);
			addEntryNode(keyNode, valueNode);
		}
		return super.toXMLNode(nodeName, xmlFile);
	}

	@Override
	public HashMap<Object, UniversalCell> copyValuesToTarget(
			HashMap<Object, UniversalCell> target) {
		importParametersFromXml(null);
		HashMap<Object, UniversalCell> ret = new HashMap<Object, UniversalCell>();
		for(Node keyNode : getEntryNodeMap().keySet()){
			XmlPrimitive xmlKey = new XmlPrimitive(keyNode);
			XmlUniversalCell xCellValue = new XmlUniversalCell(getEntryNodeMap().get(keyNode));
			UniversalCell uCell = new UniversalCell();
			xCellValue.copyValuesToTarget(uCell);
			Object o = xmlKey.copyValuesToTarget(null);
			ret.put(o, uCell);
		}
		
		return ret;
	}

}
