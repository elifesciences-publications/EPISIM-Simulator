package sim.app.episim.persistence.dataconvert;

import java.util.HashMap;

import org.w3c.dom.Node;

public class XmlHashMap<T> extends XmlObject<HashMap<String,T>> {

	public XmlHashMap(HashMap<String, T> obj) {
		super(obj);
		// TODO Auto-generated constructor stub
	}
	
	public XmlHashMap(Node node) {
		super(node);
	}

}
