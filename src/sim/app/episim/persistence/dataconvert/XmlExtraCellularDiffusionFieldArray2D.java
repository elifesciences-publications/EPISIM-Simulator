package sim.app.episim.persistence.dataconvert;

import java.util.HashMap;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sim.app.episim.model.diffusion.ExtraCellularDiffusionField2D;
import sim.app.episim.persistence.ExportException;
import sim.app.episim.persistence.XmlFile;

public class XmlExtraCellularDiffusionFieldArray2D extends
		XmlObject<ExtraCellularDiffusionField2D[]> {

	private static final String GRID2D = "grid2d";
	private static final String NAME = "name";

	public XmlExtraCellularDiffusionFieldArray2D(ExtraCellularDiffusionField2D[] obj) throws ExportException {
		super(obj);
	}

	public XmlExtraCellularDiffusionFieldArray2D(Node objectNode) {
		super(objectNode);
	}
	
	@Override
	protected void exportSubXmlObjectsFromParameters() {
	}
	
	@Override
	public Element toXMLNode(String nodeName, XmlFile xmlFile) throws ExportException {
		Element arrayElement = null;
		if(getObject()!=null){
			arrayElement = xmlFile.createElement(nodeName);
			for(ExtraCellularDiffusionField2D ecdf : getObject()){
				Element ecdfElement = new XmlDoubleGrid2D(ecdf.getExtraCellularField()).toXMLNode(GRID2D, xmlFile);
				ecdfElement.setAttribute(NAME, ecdf.getName());
				arrayElement.appendChild(ecdfElement);
			}
		} else throw new ExportException(getClass().getSimpleName()+" - "+"Can't Export -> Object=null");
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
	public ExtraCellularDiffusionField2D[] copyValuesToTarget(
			ExtraCellularDiffusionField2D[] target) {
		importParametersFromXml(null);
		for(ExtraCellularDiffusionField2D ecdf : target){
			XmlDoubleGrid2D xmlGrid = grids.get(ecdf.getName());
			if(xmlGrid!= null){
				xmlGrid.copyValuesToTarget(ecdf.getExtraCellularField());
			}
		}
		return target;
	}

}
