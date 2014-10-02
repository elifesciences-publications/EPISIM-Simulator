package sim.app.episim.util;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import episimexceptions.SimulationTriggerException;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.controller.ModelParameterModifier;
import sim.app.episim.util.SimulationTrigger.TriggerType;


public class SimulationTriggerFileReader {
	private final static String EPISIM_TRIGGER = "episimtrigger";
	private final static String TRIGGER = "trigger";
	private final static String TYPE = "type";
	private final static String TYPE_BM = "bm";
	private final static String TYPE_CBM = "cbm";
	private final static String PARAM_NAME = "paramname";
	private final static String PARAM_VALUE = "paramval";
	private final static String SIM_STEP = "simstep";
	
	private File triggerFile;
	public SimulationTriggerFileReader(File triggerFile){
		this.triggerFile = triggerFile;
	}
	
	private class XMLDefaultHandler extends DefaultHandler {

		private ArrayList<SimulationTrigger> simulationTrigger;
		public XMLDefaultHandler() {			
			simulationTrigger = new ArrayList<SimulationTrigger>();
		}
		 
		

		public void startDocument() throws SAXException {

		}

		public void startElement(String namespaceURI, String localName,
				String qName, Attributes attr) {

			if (qName.equalsIgnoreCase(TRIGGER)){
				if(attr.getValue(TYPE) != null && attr.getValue(SIM_STEP) != null && attr.getValue(PARAM_NAME) != null && attr.getValue(PARAM_VALUE) != null){
					if(attr.getValue(TYPE).equals(TYPE_BM)||attr.getValue(TYPE).equals(TYPE_CBM)){
						
						String parameterName = attr.getValue(PARAM_NAME);
						
						TriggerType triggerType=null;
						if(attr.getValue(TYPE).equals(TYPE_BM)) triggerType = TriggerType.BM;
						if(attr.getValue(TYPE).equals(TYPE_CBM)) triggerType = TriggerType.CBM;
						
						long simStep = Long.MIN_VALUE;
						double doubleValue = Double.NaN;
						boolean booleanValue=false;
						boolean booleanValueSet = false;
						
						try{ simStep = Long.parseLong(attr.getValue(SIM_STEP)); }catch(NumberFormatException e){ return;}
						
						try{
							doubleValue = Double.parseDouble(attr.getValue(PARAM_VALUE));
						}
						catch(NumberFormatException e){ 
								if(attr.getValue(PARAM_VALUE).equalsIgnoreCase("true") || attr.getValue(PARAM_VALUE).equalsIgnoreCase("false")){
									booleanValue = Boolean.parseBoolean(attr.getValue(PARAM_VALUE));
									booleanValueSet = true;
								}								
						}
						if(simStep > 0 && triggerType != null){
							SimulationTrigger trigger=null;
							if(booleanValueSet){
								trigger = new SimulationTrigger(triggerType, simStep, parameterName,booleanValue);
							}
							else if(!Double.isNaN(doubleValue)){
								trigger = new SimulationTrigger(triggerType, simStep, parameterName,doubleValue);
							}
							if(trigger != null && doesParameterExist(trigger)) this.simulationTrigger.add(trigger);
						}
						
					}
				}
			}						
		}
		
		public void endDocument() {

		}

		public void endElement(String namespaceURI, String localName, String qName) {

		}
		public ArrayList<SimulationTrigger> getSimulationTrigger(){ return this.simulationTrigger; }
		
	
	}
	
	
	public ArrayList<SimulationTrigger> getSimulationTrigger() throws SimulationTriggerException{
		ArrayList<SimulationTrigger> simulationTrigger = loadSimulationTrigger(triggerFile);
		checkSimulationTriggerConsistency(simulationTrigger);
		return simulationTrigger;
	}
	
	private ArrayList<SimulationTrigger> loadSimulationTrigger(File file) throws SimulationTriggerException{      
		try{		
			SAXParserFactory spf = SAXParserFactory.newInstance();
			spf.setNamespaceAware(true);
			SAXParser sp = spf.newSAXParser();
			XMLDefaultHandler defaultHandler = new XMLDefaultHandler();
			sp.parse(file, defaultHandler);
			return defaultHandler.getSimulationTrigger();
		}
      catch (ParserConfigurationException e){
      	ExceptionDisplayer.getInstance().displayException(e);
      	throw new SimulationTriggerException(e.getMessage());
      }
      catch (SAXException e){
      	ExceptionDisplayer.getInstance().displayException(e);
      	 throw new SimulationTriggerException(e.getMessage());
      }
      catch (IOException e){
      	ExceptionDisplayer.getInstance().displayException(e);
      	throw new SimulationTriggerException(e.getMessage());
      }		
	}
	
	private boolean doesParameterExist(SimulationTrigger trigger){
		ModelParameterModifier parameterModifier = new ModelParameterModifier();	
	   if(trigger.getTriggerType()==TriggerType.CBM){
	   	EpisimCellBehavioralModelGlobalParameters globalBehave = ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters();
	  		return parameterModifier.doesParameterExist(globalBehave, trigger.getGlobalParameterName());
	   }
	   else if(trigger.getTriggerType()==TriggerType.BM){
	   		EpisimBiomechanicalModelGlobalParameters globalMech = ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
	   		return parameterModifier.doesParameterExist(globalMech, trigger.getGlobalParameterName());
	   }		
   	return false;
	}
	
	private void checkSimulationTriggerConsistency(List<SimulationTrigger> simulationTrigger) throws SimulationTriggerException{
		HashMap<String, HashSet<Long>> triggerMap = new HashMap<String, HashSet<Long>>();
		for(SimulationTrigger trigger : simulationTrigger){
			if(triggerMap.containsKey(trigger.getGlobalParameterName())){
				long simStep = trigger.getSimStep();
				if(triggerMap.get(trigger.getGlobalParameterName()).contains(simStep)) throw new SimulationTriggerException("TRIGGER ERROR: Found two different triggers for ONE parameter at the SAME simulation step");
				else triggerMap.get(trigger.getGlobalParameterName()).add(simStep);
			}
			else{
				HashSet<Long> timePointsSet = new HashSet<Long>();
				timePointsSet.add(trigger.getSimStep());
				triggerMap.put(trigger.getGlobalParameterName(), timePointsSet);
			}
		}
	}

}
