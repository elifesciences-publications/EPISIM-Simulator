package sim.app.episim.persistence.dataconvert;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import episiminterfaces.EpisimCellBehavioralModel;

import sim.app.episim.persistence.XmlFile;

public class XmlEpisimCellBehavioralModel extends XmlObject {

	public XmlEpisimCellBehavioralModel(EpisimCellBehavioralModel episimCellBehavioralModel) {
		super(episimCellBehavioralModel);
	}

	public XmlEpisimCellBehavioralModel(Node subNode){
		super(subNode);
	}

	String get(String parameterName) {
		// TODO Auto-generated method stub
		return null;
	}

	boolean set(String parameterName, Object value) {
		// TODO Auto-generated method stub
		return false;
	}


}
