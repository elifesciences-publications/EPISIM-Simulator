package sim.app.episim.persistence.dataconvert;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import episiminterfaces.EpisimBiomechanicalModel;

import sim.app.episim.persistence.XmlFile;

public class XmlEpisimBiomechanicalModel extends
		XmlObject<EpisimBiomechanicalModel> {

	Node node;

	public XmlEpisimBiomechanicalModel(
			EpisimBiomechanicalModel episimBiomechanicalModel) {
		super(episimBiomechanicalModel);
	}

	public XmlEpisimBiomechanicalModel(Node subNode) {
		super(subNode);
		this.node = subNode;
	}

	boolean set(String parameterName, Object value) {
		return false;
	}

}
