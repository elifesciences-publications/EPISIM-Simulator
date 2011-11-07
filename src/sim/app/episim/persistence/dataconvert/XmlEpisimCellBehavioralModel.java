package sim.app.episim.persistence.dataconvert;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import episiminterfaces.EpisimCellBehavioralModel;

import sim.app.episim.model.controller.ModelController;
import sim.app.episim.persistence.XmlFile;

public class XmlEpisimCellBehavioralModel extends XmlObject<EpisimCellBehavioralModel> {

	public XmlEpisimCellBehavioralModel(EpisimCellBehavioralModel episimCellBehavioralModel) {
		super(episimCellBehavioralModel);
	}

	public XmlEpisimCellBehavioralModel(Node subNode){
		super(subNode);
	}

	public void importParametersFromXml() {
		super.importParametersFromXml(ModelController.getInstance().getNewEpisimCellBehavioralModelObject().getClass());
		
	}
	
}
