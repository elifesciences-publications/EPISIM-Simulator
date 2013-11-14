package sim.app.episim.persistence.dataconvert;



import org.w3c.dom.Element;
import org.w3c.dom.Node;

import episiminterfaces.EpisimDiffusionFieldConfigurationEx;

import sim.app.episim.persistence.ExportException;
import sim.app.episim.persistence.XmlFile;

public class XmlEpisimDiffusionFieldConfigurationEx extends XmlObject<EpisimDiffusionFieldConfigurationEx> {

	private static final String DIFFUSIONFIELDNAME = "diffusionFieldName";
	private static final String DIFFUSIONCOEFFICIENT = "diffusionCoefficient";
	private static final String LATTICESITESIZEINMIKRON = "latticeSiteSizeInMikron";
	private static final String DEGRADATIONRATE = "degradationRate";
	private static final String NUMBEROFITERATIONSPERCBMSIMSTEP = "numberOfIterationsPerCBMSimStep";
	private static final String DELTATIMEINSECONDSPERITERATION = "deltaTimeInSecondsPerIteration";
	private static final String DEFAULTCONCENTRATION = "defaultConcentration";
	private static final String MAXIMUMCONCENTRATION = "maximumConcentration";
	private static final String MINIMUMCONCENTRATION = "minimumConcentration";
	

	public XmlEpisimDiffusionFieldConfigurationEx(Node objectNode) {
		super(objectNode);
	}

	public XmlEpisimDiffusionFieldConfigurationEx(EpisimDiffusionFieldConfigurationEx modelState) throws ExportException {
		super(modelState);
	}

	@Override
	protected void exportSubXmlObjectsFromParameters() {
		
	}

	@Override
	public Element toXMLNode(String nodeName, XmlFile xmlFile) {
		
		if (getObject() != null) {
			Element node = xmlFile.createElement(nodeName);
			node.setAttribute(DIFFUSIONFIELDNAME, getObject().getDiffusionFieldName());
			node.setAttribute(DIFFUSIONCOEFFICIENT, Double.toString(getObject().getDiffusionCoefficient()));
			node.setAttribute(LATTICESITESIZEINMIKRON, Double.toString(getObject().getLatticeSiteSizeInMikron()));
			node.setAttribute(DEGRADATIONRATE, Double.toString(getObject().getDegradationRate()));
			node.setAttribute(NUMBEROFITERATIONSPERCBMSIMSTEP, Integer.toString(getObject().getNumberOfIterationsPerCBMSimStep()));
			node.setAttribute(DELTATIMEINSECONDSPERITERATION, Double.toString(getObject().getDeltaTimeInSecondsPerIteration()));
			node.setAttribute(DEFAULTCONCENTRATION, Double.toString(getObject().getDefaultConcentration()));
			node.setAttribute(MAXIMUMCONCENTRATION, Double.toString(getObject().getMaximumConcentration()));
			node.setAttribute(MINIMUMCONCENTRATION, Double.toString(getObject().getMinimumConcentration()));
			return node;
		} else
			return null;

	}
	
	@Override
	protected void importParametersFromXml(Class<?> clazz){
		if(getObjectNode() != null){
			EpisimDiffusionFieldConfigurationEx config = new EpisimDiffusionFieldConfigurationEx() {
				
				private String diffusionFieldName=getObjectNode().getAttributes().getNamedItem(DIFFUSIONFIELDNAME).getNodeValue();
				private double diffusionCoefficient=Double.parseDouble(getObjectNode().getAttributes().getNamedItem(DIFFUSIONCOEFFICIENT).getNodeValue());
				private double latticeSiteSizeInMikron=Double.parseDouble(getObjectNode().getAttributes().getNamedItem(LATTICESITESIZEINMIKRON).getNodeValue());
				private double degradationRate=Double.parseDouble(getObjectNode().getAttributes().getNamedItem(DEGRADATIONRATE).getNodeValue());
				private int numberOfIterationsPerCBMSimStep=Integer.parseInt(getObjectNode().getAttributes().getNamedItem(NUMBEROFITERATIONSPERCBMSIMSTEP).getNodeValue());
				private double deltaTimeInSecondsPerIteration=Double.parseDouble(getObjectNode().getAttributes().getNamedItem(DELTATIMEINSECONDSPERITERATION).getNodeValue());
				private double defaultConcentration=Double.parseDouble(getObjectNode().getAttributes().getNamedItem(DEFAULTCONCENTRATION).getNodeValue());
				private double maximumConcentration=Double.parseDouble(getObjectNode().getAttributes().getNamedItem(MAXIMUMCONCENTRATION).getNodeValue());
				private double minimumConcentration=Double.parseDouble(getObjectNode().getAttributes().getNamedItem(MINIMUMCONCENTRATION).getNodeValue());
				
				public String getDiffusionFieldName() {
					return this.diffusionFieldName;
				}

				public double getDiffusionCoefficient() {
					return this.diffusionCoefficient;
				}

				public double getLatticeSiteSizeInMikron() {
					return this.latticeSiteSizeInMikron;
				}

				public double getDegradationRate() {
					return this.degradationRate;
				}

				public int getNumberOfIterationsPerCBMSimStep() {
					return this.numberOfIterationsPerCBMSimStep;
				}

				public double getDeltaTimeInSecondsPerIteration() {
					return this.deltaTimeInSecondsPerIteration;
				}

				public double getDefaultConcentration() {
					return this.defaultConcentration;
				}

				public double getMaximumConcentration() {
					return this.maximumConcentration;
				}

				public double getMinimumConcentration() {
					return this.minimumConcentration;
				}
				
				
				public void setDiffusionFieldName(String val) {
					this.diffusionFieldName = val;
				}

				public void setDiffusionCoefficient(double val) {
					this.diffusionCoefficient=val;
				}

				public void setLatticeSiteSizeInMikron(double val) {
					this.latticeSiteSizeInMikron=val;
				}

				public void setDegradationRate(double val) {
					this.degradationRate=val;
				}

				public void setNumberOfIterationsPerCBMSimStep(int val) {
					this.numberOfIterationsPerCBMSimStep=val;
				}

				public void setDeltaTimeInSecondsPerIteration(double val) {
					this.deltaTimeInSecondsPerIteration=val;
				}

				public void setDefaultConcentration(double val) {
					this.defaultConcentration=val;
				}

				public void setMaximumConcentration(double val) {
					this.maximumConcentration=val;
				}

				public void setMinimumConcentration(double val) {
					this.minimumConcentration=val;
				}
			};
			
			
		 setObject(config);
		}
	}

	
	

	@Override
	public EpisimDiffusionFieldConfigurationEx copyValuesToTarget(EpisimDiffusionFieldConfigurationEx target) {
		importParametersFromXml(null);
		return getObject();	
	}
}
