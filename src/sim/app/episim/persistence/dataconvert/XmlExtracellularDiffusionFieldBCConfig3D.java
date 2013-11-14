package sim.app.episim.persistence.dataconvert;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sim.app.episim.model.diffusion.ExtracellularDiffusionFieldBCConfig2D;
import sim.app.episim.model.diffusion.ExtracellularDiffusionFieldBCConfig3D;
import sim.app.episim.model.diffusion.ExtracellularDiffusionFieldBCConfig2D.BoundaryCondition;
import sim.app.episim.persistence.XmlFile;


public class XmlExtracellularDiffusionFieldBCConfig3D extends XmlObject<ExtracellularDiffusionFieldBCConfig3D>{
	
	private static String X_AXIS_BC ="xAxisBC";
	private static String Y_AXIS_BC ="yAxisBC";
	private static String Z_AXIS_BC ="zAxisBC";
	private static String BOUNDARY_CONDITION ="boundaryCondition";	
	private static String CONSTANT_VALUE ="constantValue";
	private static String CONSTANT_FLOW ="constantFlow";
	
	public XmlExtracellularDiffusionFieldBCConfig3D(ExtracellularDiffusionFieldBCConfig3D obj){
		super(obj);
	}
	
	public XmlExtracellularDiffusionFieldBCConfig3D(Node objectNode) {
		super(objectNode);
	}
	
	@Override
	protected void exportSubXmlObjectsFromParameters() {
	}
	
	@Override
	public Element toXMLNode(String nodeName, XmlFile xmlFile) {
		
		Element configNode = xmlFile.createElement(nodeName);
		
		BoundaryCondition bc =  getObject().getBoundaryConditionX();
		Element xAxisElement = xmlFile.createElement(X_AXIS_BC);
		configNode.appendChild(xAxisElement);
		xAxisElement.setAttribute(BOUNDARY_CONDITION, bc.name());
		if(bc==BoundaryCondition.DIRICHLET) xAxisElement.setAttribute(CONSTANT_VALUE, ""+getObject().getConstantValueX());
		else if(bc==BoundaryCondition.NEUMANN) xAxisElement.setAttribute(CONSTANT_FLOW, ""+getObject().getConstantFlowX());
		
		bc =  getObject().getBoundaryConditionY();
		Element yAxisElement = xmlFile.createElement(Y_AXIS_BC);
		configNode.appendChild(yAxisElement);
		yAxisElement.setAttribute(BOUNDARY_CONDITION, bc.name());
		if(bc==BoundaryCondition.DIRICHLET) yAxisElement.setAttribute(CONSTANT_VALUE, ""+getObject().getConstantValueY());
		else if(bc==BoundaryCondition.NEUMANN) yAxisElement.setAttribute(CONSTANT_FLOW, ""+getObject().getConstantFlowY());
		
		bc =  getObject().getBoundaryConditionZ();
		Element zAxisElement = xmlFile.createElement(Z_AXIS_BC);
		configNode.appendChild(zAxisElement);
		zAxisElement.setAttribute(BOUNDARY_CONDITION, bc.name());
		if(bc==BoundaryCondition.DIRICHLET) zAxisElement.setAttribute(CONSTANT_VALUE, ""+getObject().getConstantValueZ());
		else if(bc==BoundaryCondition.NEUMANN) zAxisElement.setAttribute(CONSTANT_FLOW, ""+getObject().getConstantFlowZ());		
		
		return configNode;
	}
	
	@Override
	protected void importParametersFromXml(Class<?> clazz) {
		ExtracellularDiffusionFieldBCConfig3D bcConfig = new ExtracellularDiffusionFieldBCConfig3D();
		if (getObjectNode() != null){
			NodeList childNodes = getObjectNode().getChildNodes();
			for(int i = 0; i < childNodes.getLength(); i++){
				Node actChildNode = childNodes.item(i);
				if(actChildNode.getNodeName() != null && actChildNode.getNodeName().equals(X_AXIS_BC)){
					bcConfig.setBoundaryConditionX(getBoundaryConditionForName(actChildNode.getAttributes().getNamedItem(BOUNDARY_CONDITION).getNodeValue()));
					if(bcConfig.getBoundaryConditionX()== BoundaryCondition.DIRICHLET){
						bcConfig.setConstantValueX(Double.parseDouble(actChildNode.getAttributes().getNamedItem(CONSTANT_VALUE).getNodeValue()));
					}
					else if(bcConfig.getBoundaryConditionX()== BoundaryCondition.NEUMANN){
						bcConfig.setConstantFlowX(Double.parseDouble(actChildNode.getAttributes().getNamedItem(CONSTANT_FLOW).getNodeValue()));
					}
				}
				if(actChildNode.getNodeName() != null && actChildNode.getNodeName().equals(Y_AXIS_BC)){
					bcConfig.setBoundaryConditionY(getBoundaryConditionForName(actChildNode.getAttributes().getNamedItem(BOUNDARY_CONDITION).getNodeValue()));
					if(bcConfig.getBoundaryConditionY()== BoundaryCondition.DIRICHLET){
						bcConfig.setConstantValueY(Double.parseDouble(actChildNode.getAttributes().getNamedItem(CONSTANT_VALUE).getNodeValue()));
					}
					else if(bcConfig.getBoundaryConditionY()== BoundaryCondition.NEUMANN){
						bcConfig.setConstantFlowY(Double.parseDouble(actChildNode.getAttributes().getNamedItem(CONSTANT_FLOW).getNodeValue()));
					}
				}
				if(actChildNode.getNodeName() != null && actChildNode.getNodeName().equals(Z_AXIS_BC)){
					bcConfig.setBoundaryConditionZ(getBoundaryConditionForName(actChildNode.getAttributes().getNamedItem(BOUNDARY_CONDITION).getNodeValue()));
					if(bcConfig.getBoundaryConditionZ()== BoundaryCondition.DIRICHLET){
						bcConfig.setConstantValueZ(Double.parseDouble(actChildNode.getAttributes().getNamedItem(CONSTANT_VALUE).getNodeValue()));
					}
					else if(bcConfig.getBoundaryConditionZ()== BoundaryCondition.NEUMANN){
						bcConfig.setConstantFlowZ(Double.parseDouble(actChildNode.getAttributes().getNamedItem(CONSTANT_FLOW).getNodeValue()));
					}
				}
			}
		}
		setObject(bcConfig);		
	}
	
	private BoundaryCondition getBoundaryConditionForName(String name){
		if(name.equalsIgnoreCase(BoundaryCondition.DIRICHLET.name())) return BoundaryCondition.DIRICHLET;
		else if(name.equalsIgnoreCase(BoundaryCondition.NEUMANN.name())) return BoundaryCondition.NEUMANN;
		else if(name.equalsIgnoreCase(BoundaryCondition.PERIODIC.name())) return BoundaryCondition.PERIODIC;
		return null;
	}
	
	@Override
	public ExtracellularDiffusionFieldBCConfig3D copyValuesToTarget(ExtracellularDiffusionFieldBCConfig3D target) {
		importParametersFromXml(null);
		ExtracellularDiffusionFieldBCConfig3D loadedBC = getObject();
		target.setBoundaryConditionX(loadedBC.getBoundaryConditionX());
		target.setBoundaryConditionY(loadedBC.getBoundaryConditionY());
		target.setBoundaryConditionZ(loadedBC.getBoundaryConditionZ());
		target.setConstantValueX(loadedBC.getConstantValueX());
		target.setConstantValueY(loadedBC.getConstantValueY());
		target.setConstantValueZ(loadedBC.getConstantValueZ());
		target.setConstantFlowX(loadedBC.getConstantFlowX());
		target.setConstantFlowY(loadedBC.getConstantFlowY());
		target.setConstantFlowZ(loadedBC.getConstantFlowZ());
		return target;
	}
	
}
