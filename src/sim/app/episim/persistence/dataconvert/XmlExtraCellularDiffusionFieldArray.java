package sim.app.episim.persistence.dataconvert;

import java.util.HashMap;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sim.app.episim.model.diffusion.ExtraCellularDiffusionField;
import sim.app.episim.persistence.XmlFile;
import sim.field.grid.DoubleGrid2D;

public class XmlExtraCellularDiffusionFieldArray extends
		XmlObject<ExtraCellularDiffusionField[]> {

	private static final String GRID2D = "grid2d";
	private static final String NAME = "name";

	public XmlExtraCellularDiffusionFieldArray(ExtraCellularDiffusionField[] obj) {
		super(obj);
	}

	public XmlExtraCellularDiffusionFieldArray(Node objectNode) {
		super(objectNode);
	}
	
	@Override
	protected void exportSubXmlObjectsFromParameters() {
	}
	
	@Override
	public Element toXMLNode(String nodeName, XmlFile xmlFile) {
		Element arrayElement = null;
		if(getObject()!=null){
			arrayElement = xmlFile.createElement(nodeName);
			for(ExtraCellularDiffusionField ecdf : getObject()){
				Element ecdfElement = new XmlDoubleGrid2D(ecdf.getExtraCellularField()).toXMLNode(GRID2D, xmlFile);
				ecdfElement.setAttribute(NAME, ecdf.getName());
				arrayElement.appendChild(ecdfElement);
			}
		}
		return arrayElement;
	}
	
	private HashMap<String, XmlDoubleGrid2D> grids;
	
	@Override
	protected void importParametersFromXml(Class<?> clazz) {
		grids = new HashMap<String, XmlDoubleGrid2D>();
		NodeList gridsNL = getObjectNode().getChildNodes();
		for(int index = 0 ; index < gridsNL.getLength(); index++){
			Node gridNode = gridsNL.item(index);
			if(gridNode.getNodeName().equals(GRID2D)){
				Node nameNode = gridNode.getAttributes().getNamedItem(NAME);
				if(nameNode!=null){
					XmlDoubleGrid2D xmlGrid = new XmlDoubleGrid2D(gridNode);
					grids.put(nameNode.getNodeValue(), xmlGrid);
				}
			}
		}
	}
	
	@Override
	public ExtraCellularDiffusionField[] copyValuesToTarget(
			ExtraCellularDiffusionField[] target) {
		importParametersFromXml(null);
		for(ExtraCellularDiffusionField ecdf : target){
			XmlDoubleGrid2D xmlGrid = grids.get(ecdf.getName());
			if(xmlGrid!= null){
				xmlGrid.copyValuesToTarget(ecdf.getExtraCellularField());
			}
		}
		return target;
	}

}
