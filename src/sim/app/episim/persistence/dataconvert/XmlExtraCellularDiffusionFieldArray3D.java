package sim.app.episim.persistence.dataconvert;

import java.util.HashMap;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sim.app.episim.model.diffusion.ExtraCellularDiffusionField3D;
import sim.app.episim.persistence.ExportException;
import sim.app.episim.persistence.XmlFile;

public class XmlExtraCellularDiffusionFieldArray3D extends XmlObject<ExtraCellularDiffusionField3D[]> {

	private static final String GRID3D = "grid3d";
	private static final String NAME = "name";

	public XmlExtraCellularDiffusionFieldArray3D(ExtraCellularDiffusionField3D[] obj) throws ExportException {
		super(obj);
	}

	public XmlExtraCellularDiffusionFieldArray3D(Node objectNode) {
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
			for(ExtraCellularDiffusionField3D ecdf : getObject()){
				Element ecdfElement = new XmlDoubleGrid3D(ecdf.getExtraCellularField()).toXMLNode(GRID3D, xmlFile);
				ecdfElement.setAttribute(NAME, ecdf.getName());
				arrayElement.appendChild(ecdfElement);
			}
		} else throw new ExportException(getClass().getSimpleName()+" - "+"Can't Export -> Object=null");
		return arrayElement;
	}
	
	private HashMap<String, XmlDoubleGrid3D> grids;
	
	@Override
	protected void importParametersFromXml(Class<?> clazz) {
		grids = new HashMap<String, XmlDoubleGrid3D>();
		NodeList gridsNL = getObjectNode().getChildNodes();
		for(int index = 0 ; index < gridsNL.getLength(); index++){
			Node gridNode = gridsNL.item(index);
			if(gridNode.getNodeName().equals(GRID3D)){
				Node nameNode = gridNode.getAttributes().getNamedItem(NAME);
				if(nameNode!=null){
					XmlDoubleGrid3D xmlGrid = new XmlDoubleGrid3D(gridNode);
					grids.put(nameNode.getNodeValue(), xmlGrid);
				}
			}
		}
	}
	
	@Override
	public ExtraCellularDiffusionField3D[] copyValuesToTarget(
			ExtraCellularDiffusionField3D[] target) {
		importParametersFromXml(null);
		for(ExtraCellularDiffusionField3D ecdf : target){
			XmlDoubleGrid3D xmlGrid = grids.get(ecdf.getName());
			if(xmlGrid!= null){
				xmlGrid.copyValuesToTarget(ecdf.getExtraCellularField());
			}
		}
		return target;
	}
	
}
