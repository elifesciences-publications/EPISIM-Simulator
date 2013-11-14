package sim.app.episim.persistence.dataconvert;

import java.util.HashMap;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sim.app.episim.model.diffusion.ExtraCellularDiffusionField3D;
import sim.app.episim.model.diffusion.ExtracellularDiffusionFieldBCConfig3D;
import sim.app.episim.persistence.ExportException;
import sim.app.episim.persistence.XmlFile;

public class XmlExtraCellularDiffusionFieldArray3D extends XmlObject<ExtraCellularDiffusionField3D[]> {

	private static final String BCCONFIG = "diffusionFieldBCConfig";
	private static final String EXTRACELLULARDIFFUSIONFIELD3D = "extraCellularDiffusionField3D";	
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
				Element fieldElement = xmlFile.createElement(EXTRACELLULARDIFFUSIONFIELD3D);
				Element ecdfConfigElement = new XmlExtracellularDiffusionFieldBCConfig3D((ExtracellularDiffusionFieldBCConfig3D)ecdf.getFieldBCConfig()).toXMLNode(BCCONFIG, xmlFile);
				Element ecdfElement = new XmlDoubleGrid3D(ecdf.getExtraCellularField()).toXMLNode(GRID3D, xmlFile);
				fieldElement.setAttribute(NAME, ecdf.getName());				
				fieldElement.appendChild(ecdfConfigElement);
				fieldElement.appendChild(ecdfElement);
				arrayElement.appendChild(fieldElement);
			}
		} else throw new ExportException(getClass().getSimpleName()+" - "+"Can't Export -> Object=null");
		return arrayElement;
	}
	
	private HashMap<String, XmlDoubleGrid3D> grids;
	private HashMap<String, XmlExtracellularDiffusionFieldBCConfig3D> bcConfigs;
	
	@Override
	protected void importParametersFromXml(Class<?> clazz) {
		grids = new HashMap<String, XmlDoubleGrid3D>();
		bcConfigs= new HashMap<String, XmlExtracellularDiffusionFieldBCConfig3D>();
		NodeList fieldsNL = getObjectNode().getChildNodes();
		for(int index = 0 ; index < fieldsNL.getLength(); index++){
			Node fieldNode = fieldsNL.item(index);
			if(fieldNode.getNodeName().equals(EXTRACELLULARDIFFUSIONFIELD3D)){
				NodeList childrenNL = fieldNode.getChildNodes();
				Node nameNode = fieldNode.getAttributes().getNamedItem(NAME);
				for(int i = 0; i < childrenNL.getLength(); i++){
					Node childNode = childrenNL.item(i);					
					if(childNode.getNodeName().equals(GRID3D)){					
						if(nameNode!=null){
							XmlDoubleGrid3D xmlGrid = new XmlDoubleGrid3D(childNode);
							grids.put(nameNode.getNodeValue(), xmlGrid);
						}
					}
					if(childNode.getNodeName().equals(BCCONFIG)){					
						if(nameNode!=null){
							XmlExtracellularDiffusionFieldBCConfig3D xmlBCConfig = new XmlExtracellularDiffusionFieldBCConfig3D(childNode);
							bcConfigs.put(nameNode.getNodeValue(), xmlBCConfig);
						}
					}
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
			XmlExtracellularDiffusionFieldBCConfig3D xmlBCConfig = bcConfigs.get(ecdf.getName());
			if(xmlBCConfig!= null){
				xmlBCConfig.copyValuesToTarget((ExtracellularDiffusionFieldBCConfig3D)ecdf.getFieldBCConfig());
			}
		}
		return target;
	}
	
}
