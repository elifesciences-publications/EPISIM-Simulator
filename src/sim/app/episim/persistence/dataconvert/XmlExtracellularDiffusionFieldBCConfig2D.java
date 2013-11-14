package sim.app.episim.persistence.dataconvert;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sim.app.episim.model.diffusion.ExtracellularDiffusionFieldBCConfig2D;
import sim.app.episim.model.diffusion.ExtracellularDiffusionFieldBCConfig2D.BoundaryCondition;
import sim.app.episim.persistence.XmlFile;



public class XmlExtracellularDiffusionFieldBCConfig2D extends XmlObject<ExtracellularDiffusionFieldBCConfig2D>{
	
	private static String X_AXIS_BC ="xAxisBC";
	private static String Y_AXIS_BC ="yAxisBC";
	private static String BOUNDARY_CONDITION ="boundaryCondition";	
	private static String CONSTANT_VALUE ="constantValue";
	private static String CONSTANT_FLOW ="constantFlow";
	
	
	public XmlExtracellularDiffusionFieldBCConfig2D(ExtracellularDiffusionFieldBCConfig2D obj){
		super(obj);
	}
	
	public XmlExtracellularDiffusionFieldBCConfig2D(Node objectNode) {
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
		
		return configNode;
	}
	
	@Override
	protected void importParametersFromXml(Class<?> clazz) {
		ExtracellularDiffusionFieldBCConfig2D bcConfig = new ExtracellularDiffusionFieldBCConfig2D();
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
	public ExtracellularDiffusionFieldBCConfig2D copyValuesToTarget(ExtracellularDiffusionFieldBCConfig2D target) {
		importParametersFromXml(null);
		ExtracellularDiffusionFieldBCConfig2D loadedBC = getObject();
		target.setBoundaryConditionX(loadedBC.getBoundaryConditionX());
		target.setBoundaryConditionY(loadedBC.getBoundaryConditionY());
		target.setConstantValueX(loadedBC.getConstantValueX());
		target.setConstantValueY(loadedBC.getConstantValueY());
		target.setConstantFlowX(loadedBC.getConstantFlowX());
		target.setConstantFlowY(loadedBC.getConstantFlowY());
		return target;
	}
	
}
