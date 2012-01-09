package sim.app.episim.persistence.dataconvert;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sim.app.episim.persistence.ExportException;

import episimbiomechanics.EpisimModelConnector;

public class XmlEpisimModelConnector extends XmlObject<EpisimModelConnector> {

	public XmlEpisimModelConnector(EpisimModelConnector obj) throws ExportException {
		super(obj);
	}

	public XmlEpisimModelConnector(Node obj) {
		super(obj);
	}
	
}
