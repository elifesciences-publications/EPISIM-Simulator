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
	private static final String BCCONFIG = "diffusionFieldBCConfig";
	private static final String EXTRACELLULARDIFFUSIONFIELD2D = "extraCellularDiffusionField2D";		
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
				Element fieldElement = xmlFile.createElement(EXTRACELLULARDIFFUSIONFIELD2D);
				Element ecdfConfigElement = new XmlExtracellularDiffusionFieldBCConfig2D(ecdf.getFieldBCConfig()).toXMLNode(BCCONFIG, xmlFile);
				Element ecdfElement = new XmlDoubleGrid2D(ecdf.getExtraCellularField()).toXMLNode(GRID2D, xmlFile);
				fieldElement.setAttribute(NAME, ecdf.getName());				
				fieldElement.appendChild(ecdfConfigElement);
				fieldElement.appendChild(ecdfElement);
				arrayElement.appendChild(fieldElement);
			}
		} else throw new ExportException(getClass().getSimpleName()+" - "+"Can't Export -> Object=null");
		return arrayElement;
	}
	
	private HashMap<String, XmlDoubleGrid2D> grids;
	private HashMap<String, XmlExtracellularDiffusionFieldBCConfig2D> bcConfigs;
	
	@Override
	protected void importParametersFromXml(Class<?> clazz) {
		grids = new HashMap<String, XmlDoubleGrid2D>();
		bcConfigs= new HashMap<String, XmlExtracellularDiffusionFieldBCConfig2D>();
		NodeList fieldsNL = getObjectNode().getChildNodes();
		for(int index = 0 ; index < fieldsNL.getLength(); index++){
			Node fieldNode = fieldsNL.item(index);
			if(fieldNode.getNodeName().equals(EXTRACELLULARDIFFUSIONFIELD2D)){
				NodeList childrenNL = fieldNode.getChildNodes();
				Node nameNode = fieldNode.getAttributes().getNamedItem(NAME);
				for(int i = 0; i < childrenNL.getLength(); i++){
					Node childNode = childrenNL.item(i);					
					if(childNode.getNodeName().equals(GRID2D)){					
						if(nameNode!=null){
							XmlDoubleGrid2D xmlGrid = new XmlDoubleGrid2D(childNode);
							grids.put(nameNode.getNodeValue(), xmlGrid);
						}
					}
					if(childNode.getNodeName().equals(BCCONFIG)){					
						if(nameNode!=null){
							XmlExtracellularDiffusionFieldBCConfig2D xmlBCConfig = new XmlExtracellularDiffusionFieldBCConfig2D(childNode);
							bcConfigs.put(nameNode.getNodeValue(), xmlBCConfig);
						}
					}
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
			XmlExtracellularDiffusionFieldBCConfig2D xmlBCConfig = bcConfigs.get(ecdf.getName());
			if(xmlBCConfig!= null){
				xmlBCConfig.copyValuesToTarget(ecdf.getFieldBCConfig());
			}
		}
		return target;
	}

}
