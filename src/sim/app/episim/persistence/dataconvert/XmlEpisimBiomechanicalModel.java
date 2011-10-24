package sim.app.episim.persistence.dataconvert;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import episiminterfaces.EpisimBiomechanicalModel;

import sim.app.episim.persistence.XmlFile;

public class XmlEpisimBiomechanicalModel extends XmlObject {

	public XmlEpisimBiomechanicalModel(EpisimBiomechanicalModel episimBiomechanicalModel) {
		super(episimBiomechanicalModel);
	}

	public XmlEpisimBiomechanicalModel(Node subNode){
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

//	@Override
//	Node toXMLNode(XmlFile xmlFile) {
//		Element modelNode = xmlFile.createElement("episimBiomechanicalModel");
//		for(String s : getParameters().keySet()){
//			Element parameterNode = xmlFile.createElement(s);
//			System.out.println(s);
//			parameterNode.setTextContent(getParameters().get(s).toString());
//			modelNode.appendChild(parameterNode);
//		}
//		return modelNode;
//	}

}
