package sim.app.episim.persistence.dataconvert;

import org.w3c.dom.Node;

import sim.app.episim.persistence.ExportException;
import episimmcc.EpisimModelConnector;

public class XmlEpisimModelConnector extends XmlObject<EpisimModelConnector> {

	public XmlEpisimModelConnector(EpisimModelConnector obj) throws ExportException {
		super(obj);
	}

	public XmlEpisimModelConnector(Node obj) {
		super(obj);
	}
	
}
