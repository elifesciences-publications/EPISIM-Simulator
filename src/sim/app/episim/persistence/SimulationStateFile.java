package sim.app.episim.persistence;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import sim.app.episim.UniversalCell;

public class SimulationStateFile extends XmlFile {

	private static final String ROOT_NAME = "data_set";
	private static final String DATA_SOURCE = "data_source";
	private static final String GLOBALS = "globals";
	private static final String CELL_LIST = "cell_list";
	private static final String CELL_ID = "cell_id";
	private static final String GLOBAL_VARIABLES = "global_variables";
	private static final String MultiCellXML_VERSION = "MultiCellXML_version";
	private static final String CELLCONTINUOUS = "cellContinuous";
	private static final String CELLS = "cells";
	private static final String DELTAINFO = "deltaInfo";
	private static final String EPISIMBIOMECHANICALMODELGLOBALPARAMETERS = "episimBioMechanicalModelGlobalParameters";
	private static final String EPISIMCELLBEHAVIORALMODELGLOBALPARAMETERS = "episimCellBehavioralModelGlobalParameters";
	private static final String MISCALLENEOUSGLOBALPARAMETERS = "miscalleneousGlobalParameters";
	private static final String TIMESTEPS = "timeSteps";
	private static final String WOUNDREGIONCOORDINATES = "woundRegionCoordinates";

	private Element rootNode = null;
	private ConvertObjectXML converter = new ConvertObjectXML(this);

	public SimulationStateFile(File path) throws SAXException, IOException, ParserConfigurationException {
		super(path);
		rootNode = getRoot();
		if (!rootNode.getNodeName().equals(ROOT_NAME))
			throw new IOException("Wrong file format: " + path.getAbsolutePath());
	}

	public SimulationStateFile() throws ParserConfigurationException, SAXException {
		super(ROOT_NAME);
		rootNode = getRoot();
		rootNode.setAttribute(MultiCellXML_VERSION, "1.0");
	}

	public void loadData() {
		NodeList nodes = getRoot().getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node actNode = nodes.item(i);
			if (actNode.getNodeName().equals(CELLS)) {
				ArrayList<UniversalCell> cells = null;
				cells=converter.xMLToObject(actNode, ArrayList.class);
				SimulationStateData.getInstance().setCells(cells);
			} 
//			else if (actNode.getNodeName().equals(CELLCONTINUOUS)) {
//				SimulationStateData.getInstance().setCellContinuous((Continuous2D) unknown);
//			} else if (actNode.getNodeName().equals(DELTAINFO)) {
//				SimulationStateData.getInstance().setDeltaInfo((Rectangle2D.Double[]) unknown);
//			} else if (actNode.getNodeName().equals(EPISIMBIOMECHANICALMODELGLOBALPARAMETERS)) {
//				SimulationStateData.getInstance().setEpisimBioMechanicalModelGlobalParameters((EpisimBiomechanicalModelGlobalParameters) unknown);
//			} else if (actNode.getNodeName().equals(EPISIMCELLBEHAVIORALMODELGLOBALPARAMETERS)) {
//				SimulationStateData.getInstance().setEpisimCellBehavioralModelGlobalParameters((EpisimCellBehavioralModelGlobalParameters) unknown);
//			} else if (actNode.getNodeName().equals(MISCALLENEOUSGLOBALPARAMETERS)) {
//				SimulationStateData.getInstance().setMiscalleneousGlobalParameters((MiscalleneousGlobalParameters) unknown);
//			} else if (actNode.getNodeName().equals(TIMESTEPS)) {
//				SimulationStateData.getInstance().setTimeSteps((TimeSteps) unknown);
//			} else if (actNode.getNodeName().equals(WOUNDREGIONCOORDINATES)) {
//				SimulationStateData.getInstance().setWoundRegionCoordinates((List<Double2D>) unknown);
//			}
		}
	}

	public void saveData(File path) {
		SimulationStateData.getInstance().updateData();
		getRoot().appendChild(converter.objectToXML(SimulationStateData.getInstance().getCells(),CELLS));
		
		getRoot().appendChild(converter.objectToXML(SimulationStateData.getInstance().getCellContinuous(),CELLCONTINUOUS));
		
		getRoot().appendChild(converter.objectToXML(SimulationStateData.getInstance().getDeltaInfo(),DELTAINFO));
		
		getRoot().appendChild(converter.objectToXML(SimulationStateData.getInstance().getEpisimBioMechanicalModelGlobalParameters(),EPISIMBIOMECHANICALMODELGLOBALPARAMETERS));
		
		getRoot().appendChild(converter.objectToXML(SimulationStateData.getInstance().getEpisimCellBehavioralModelGlobalParameters(),EPISIMCELLBEHAVIORALMODELGLOBALPARAMETERS));
		
		getRoot().appendChild(converter.objectToXML(SimulationStateData.getInstance().getMiscalleneousGlobalParameters(),MISCALLENEOUSGLOBALPARAMETERS));
		
		getRoot().appendChild(converter.objectToXML(SimulationStateData.getInstance().getTimeSteps(),TIMESTEPS));
		
		getRoot().appendChild(converter.objectToXML(SimulationStateData.getInstance().getWoundRegionCoordinates(),WOUNDREGIONCOORDINATES));
		
		save(path);
	}
	

}
