package sim.app.episim.persistence.dataconvert;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sim.app.episim.UniversalCell;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.persistence.XmlFile;
import sim.app.episim.util.GlobalClassLoader;

public class XmlUniversalCell extends XmlObject{
	
	XmlEpisimBiomechanicalModel episimBiomechanicalModel;
	XmlEpisimCellBehavioralModel episimCellBehavioralModel;

	public XmlUniversalCell(Object snapshotObject) {
		super(snapshotObject);
		UniversalCell uniCell = (UniversalCell)snapshotObject;
		episimBiomechanicalModel = new XmlEpisimBiomechanicalModel(uniCell.getEpisimBioMechanicalModelObject());
		episimCellBehavioralModel = new XmlEpisimCellBehavioralModel(uniCell.getEpisimCellBehavioralModelObject());
	}
	
	public XmlUniversalCell(Node universalCellNode) throws ClassNotFoundException {
		super(universalCellNode);
//		ModelController.getInstance().getNewEpisimCellBehavioralModelObject().getClass(); //TODO
		NodeList nl = universalCellNode.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node subNode = nl.item(i);
			if(subNode.getNodeName().equals("episimBiomechanicalModel")){
					episimBiomechanicalModel = new XmlEpisimBiomechanicalModel(subNode);
			} else if(subNode.getNodeName().equals("episimCellBehavioralModel")){
					episimCellBehavioralModel = new XmlEpisimCellBehavioralModel(subNode);
			}
		}
	}

	String get(String parameterName) {
		// TODO Auto-generated method stub
		return null;
	}

	boolean set(String parameterName, Object value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Node toXMLNode(String nodeName, XmlFile xmlFile) {
		Node cellNode = super.toXMLNode(nodeName, xmlFile);
		cellNode.appendChild(episimBiomechanicalModel.toXMLNode("episimBiomechanicalModel",xmlFile));
		cellNode.appendChild(episimCellBehavioralModel.toXMLNode("episimCellBehavioralModel",xmlFile));
		return cellNode;
	}
	
	@Override
	public void importParametersFromXml(Object obj) {
		super.importParametersFromXml(obj);
		episimBiomechanicalModel.importParametersFromXml(obj);
		episimCellBehavioralModel.importParametersFromXml(obj);
	}
	

}
