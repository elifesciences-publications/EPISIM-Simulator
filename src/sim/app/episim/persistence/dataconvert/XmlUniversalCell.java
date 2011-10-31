package sim.app.episim.persistence.dataconvert;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sim.app.episim.UniversalCell;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.persistence.XmlFile;
import sim.app.episim.util.GlobalClassLoader;

public class XmlUniversalCell extends XmlObject{
	
	XmlEpisimBiomechanicalModel episimBiomechanicalModel;
	XmlEpisimCellBehavioralModel episimCellBehavioralModel;

	public XmlUniversalCell(Object cell) {
		super(cell);
		UniversalCell uniCell = (UniversalCell)cell;
		episimBiomechanicalModel = new XmlEpisimBiomechanicalModel(uniCell.getEpisimBioMechanicalModelObject());
		episimCellBehavioralModel = new XmlEpisimCellBehavioralModel(uniCell.getEpisimCellBehavioralModelObject());
	}
	
	public XmlUniversalCell(Node universalCellNode) throws ClassNotFoundException {
		super(universalCellNode);
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

	boolean set(String parameterName, Object value) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public long getId(){
		return (Long)get("iD");
	}

	@Override
	public Node toXMLNode(String nodeName, XmlFile xmlFile) {
		Node cellNode = super.toXMLNode(nodeName, xmlFile);
		cellNode.appendChild(episimBiomechanicalModel.toXMLNode("episimBiomechanicalModel",xmlFile));
		cellNode.appendChild(episimCellBehavioralModel.toXMLNode("episimCellBehavioralModel",xmlFile));
		return cellNode;
	}
	
	public void importParametersFromXml() throws ClassNotFoundException, DOMException {
		super.importParametersFromXml(UniversalCell.class);
		episimBiomechanicalModel.importParametersFromXml();
		episimCellBehavioralModel.importParametersFromXml();
	}

	public XmlEpisimBiomechanicalModel getEpisimBiomechanicalModel() {
		return episimBiomechanicalModel;
	}

	public XmlEpisimCellBehavioralModel getEpisimCellBehavioralModel() {
		return episimCellBehavioralModel;
	}
	

}
