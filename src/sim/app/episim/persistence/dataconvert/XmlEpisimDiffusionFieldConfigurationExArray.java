package sim.app.episim.persistence.dataconvert;

import java.util.HashMap;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import episiminterfaces.EpisimDiffusionFieldConfiguration;
import episiminterfaces.EpisimDiffusionFieldConfigurationEx;

import sim.app.episim.model.diffusion.ExtraCellularDiffusionField2D;
import sim.app.episim.model.sbml.SBMLModelConnector;
import sim.app.episim.persistence.ExportException;
import sim.app.episim.persistence.XmlFile;


public class XmlEpisimDiffusionFieldConfigurationExArray extends XmlObject<EpisimDiffusionFieldConfiguration[]> {

	private static final String DIFFCONFIGS = "episimDiffusionFieldConfiguration";
	private static final String NAME = "name";

	public XmlEpisimDiffusionFieldConfigurationExArray(EpisimDiffusionFieldConfiguration[] obj) throws ExportException {
		super(obj);
	}

	public XmlEpisimDiffusionFieldConfigurationExArray(Node objectNode) {
		super(objectNode);
	}

	@Override
	protected void exportSubXmlObjectsFromParameters() throws ExportException {
		for(EpisimDiffusionFieldConfiguration config : getObject()){
			if(config instanceof EpisimDiffusionFieldConfigurationEx){
				addSubXmlObject(DIFFCONFIGS, new XmlEpisimDiffusionFieldConfigurationEx((EpisimDiffusionFieldConfigurationEx)config));
			}
		}
	}
	
	@Override
	public Element toXMLNode(String nodeName, XmlFile xmlFile) throws ExportException {
		Element arrayElement = null;
		if(getObject()!=null){
			arrayElement = xmlFile.createElement(nodeName);
			for(EpisimDiffusionFieldConfiguration config : getObject()){
				Element ecdfElement = new XmlEpisimDiffusionFieldConfigurationEx((EpisimDiffusionFieldConfigurationEx)config).toXMLNode(DIFFCONFIGS, xmlFile);
				ecdfElement.setAttribute(NAME, config.getDiffusionFieldName());
				arrayElement.appendChild(ecdfElement);
			}
		} else throw new ExportException(getClass().getSimpleName()+" - "+"Can't Export -> Object=null");
		return arrayElement;
	}
	
	private HashMap<String, XmlEpisimDiffusionFieldConfigurationEx> configs;
	
	@Override
	protected void importParametersFromXml(Class<?> clazz) {
		configs = new HashMap<String, XmlEpisimDiffusionFieldConfigurationEx>();
		NodeList configsNL = getObjectNode().getChildNodes();
		for(int index = 0 ; index < configsNL.getLength(); index++){
			Node configNode = configsNL.item(index);
			if(configNode.getNodeName().equals(DIFFCONFIGS)){
				Node nameNode = configNode.getAttributes().getNamedItem(NAME);
				if(nameNode!=null){
					XmlEpisimDiffusionFieldConfigurationEx xmlConfig = new XmlEpisimDiffusionFieldConfigurationEx(configNode);
					configs.put(nameNode.getNodeValue(), xmlConfig);
				}
			}
		}
	}
	
	@Override
	public EpisimDiffusionFieldConfiguration[] copyValuesToTarget(EpisimDiffusionFieldConfiguration[] target) {
		importParametersFromXml(null);
		if(target == null){
			target=new EpisimDiffusionFieldConfiguration[configs.size()];
			int index =0;
			for(String configName : configs.keySet()){
				XmlEpisimDiffusionFieldConfigurationEx xmlConfig = null;
				xmlConfig =configs.get(configName);
				if(xmlConfig!= null){
					target[index]=xmlConfig.copyValuesToTarget((EpisimDiffusionFieldConfigurationEx)null);
				}
				index++;
			}
		}
		else{
			for(EpisimDiffusionFieldConfiguration config : target){
				XmlEpisimDiffusionFieldConfigurationEx xmlConfig = null;
				xmlConfig =configs.get(config.getDiffusionFieldName());
				if(xmlConfig!= null && config instanceof EpisimDiffusionFieldConfigurationEx){
					xmlConfig.copyValuesToTarget((EpisimDiffusionFieldConfigurationEx)config);
				}
			}
		}
		return target;
	}

}
