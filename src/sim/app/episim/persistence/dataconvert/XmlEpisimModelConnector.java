package sim.app.episim.persistence.dataconvert;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import episimbiomechanics.EpisimModelConnector;

public class XmlEpisimModelConnector extends XmlObject<EpisimModelConnector> {

	public XmlEpisimModelConnector(EpisimModelConnector obj) {
		super(obj);
	}

	public XmlEpisimModelConnector(Node obj) {
		super(obj);
	}
	
}
