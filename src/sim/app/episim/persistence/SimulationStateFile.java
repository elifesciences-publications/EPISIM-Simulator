package sim.app.episim.persistence;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import sim.app.episim.gui.EpisimSimulator;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters;
import sim.app.episim.persistence.dataconvert.XmlExtraCellularDiffusionFieldArray2D;
import sim.app.episim.persistence.dataconvert.XmlExtraCellularDiffusionFieldArray3D;
import sim.app.episim.persistence.dataconvert.XmlObject;
import sim.app.episim.persistence.dataconvert.XmlTissueBorder;
import sim.app.episim.persistence.dataconvert.XmlUniversalCell;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters.ModelDimensionality;
import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;

public class SimulationStateFile extends XmlFile {

	private static final String ROOT_NAME = "data_set";
	private static final String CELLBEHAVIORALMODEL_FILE = "model_file";
	private static final String MultiCellXML_VERSION = "MultiCellXML_version";
	private static final String CELLS = "cells";
	private static final String CELL = "cell";
	private static final String MODELFILE = "modelfile";
	private static final String SIMSTEP = "simstep";
	private static final String EPISIMBIOMECHANICALMODELGLOBALPARAMETERS = "episimbiomechanicalmodelglobalparameters";
	private static final String EPISIMCELLBEHAVIORALMODELGLOBALPARAMETERS = "episimcellbehavioralmodelglobalparameters";
	private static final String MISCALLENEOUSGLOBALPARAMETERS = "miscalleneousglobalparameters";
	public static final String FILEEXTENSION = "xml";
	private static final String VALUE = "value";
	private static final String EPISIM_TISSUE_SIMULATION_HEADER = "episim_tissue_simulation_header";
	private static final String EPISIM_VERSION = "version";
	private static final String EXPORT_DATE = "exportdate";
	private static final String TISSUE_BORDER = "tissueborder";
	private static final String EXTRACELLULARDIFFUSIONFIELDARRAY2D = "ExtraCellularDiffusionFields2D";
	private static final String EXTRACELLULARDIFFUSIONFIELDARRAY3D = "ExtraCellularDiffusionFields3D";

	private static File tissueExportPath;

	private Element rootNode = null;

	public SimulationStateFile(File path) throws SAXException, IOException,
			ParserConfigurationException {
		super(path);
		rootNode = getRoot();
		if (!rootNode.getNodeName().equals(ROOT_NAME))
			throw new IOException("Wrong file format: "
					+ path.getAbsolutePath());

	}

	public SimulationStateFile() throws ParserConfigurationException,
			SAXException {
		super(ROOT_NAME);
		rootNode = getRoot();
		rootNode.setAttribute(MultiCellXML_VERSION, "1.0");
	}

	public static File getTissueExportPath() {
		return tissueExportPath;
	}

	public static void setTissueExportPath(File tissueExportPath) {
		SimulationStateFile.tissueExportPath = tissueExportPath;
	}

	public SimulationStateData loadData() {
		Node simulationHeader = getRoot().getElementsByTagName(
				EPISIM_TISSUE_SIMULATION_HEADER).item(0);
		SimulationStateData simStateData = new SimulationStateData();

		if (simulationHeader != null) {
			NodeList nodes = simulationHeader.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				if (nodes.item(i).getNodeName()
						.equalsIgnoreCase(CELLBEHAVIORALMODEL_FILE)) {
					simStateData.setLoadedModelFile(nodes.item(i)
							.getAttributes().getNamedItem(MODELFILE)
							.getNodeValue());
				} else if (nodes.item(i).getNodeName()
						.equalsIgnoreCase(SIMSTEP)) {
					String simstepString = nodes.item(i).getAttributes()
							.getNamedItem(VALUE).getNodeValue();
					if (simstepString != null)
						simStateData.setSimStepNumber(Long
								.parseLong(simstepString));
				}
			}

		}

		NodeList nodes = getRoot().getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			ImportLog.nodeRead(nodes.item(i));

			if (nodes.item(i).getNodeName().equalsIgnoreCase(CELLS)) {
				NodeList cellNodes = nodes.item(i).getChildNodes();

				for (int j = 0; j < cellNodes.getLength(); j++) {
					XmlUniversalCell xmlCell;
					Node cellNode = cellNodes.item(j);
					if (cellNode.getNodeName().equalsIgnoreCase(CELL)) {

						xmlCell = new XmlUniversalCell(cellNode);
						simStateData.addCell(xmlCell);
					}

				}

			}
			if (nodes.item(i).getNodeName()
					.equalsIgnoreCase(EPISIMBIOMECHANICALMODELGLOBALPARAMETERS)) {
				simStateData
						.setEpisimBioMechanicalModelGlobalParameters(new XmlObject<EpisimBiomechanicalModelGlobalParameters>(
								nodes.item(i)));
			}

			if (nodes
					.item(i)
					.getNodeName()
					.equalsIgnoreCase(EPISIMCELLBEHAVIORALMODELGLOBALPARAMETERS)) {
				simStateData
						.setEpisimCellBehavioralModelGlobalParameters(new XmlObject<EpisimCellBehavioralModelGlobalParameters>(
								nodes.item(i)));

			}

			if (nodes.item(i).getNodeName().equalsIgnoreCase(TISSUE_BORDER)) {
				simStateData
						.setTissueBorder(new XmlTissueBorder(nodes.item(i)));

			}
			if (nodes.item(i).getNodeName()
					.equalsIgnoreCase(MISCALLENEOUSGLOBALPARAMETERS)) {
				simStateData
						.setMiscalleneousGlobalParameters(new XmlObject<MiscalleneousGlobalParameters>(
								nodes.item(i)));
			}

			if (nodes.item(i).getNodeName()
					.equalsIgnoreCase(EXTRACELLULARDIFFUSIONFIELDARRAY2D)) {
				simStateData
						.setExtraCellularDiffusionFieldArray2D(new XmlExtraCellularDiffusionFieldArray2D(
								nodes.item(i)));
			}
			if (nodes.item(i).getNodeName()
					.equalsIgnoreCase(EXTRACELLULARDIFFUSIONFIELDARRAY3D)) {
				simStateData
						.setExtraCellularDiffusionFieldArray3D(new XmlExtraCellularDiffusionFieldArray3D(
								nodes.item(i)));
			}
		}
		return simStateData;
	}

	public void saveData() {

		if (SimulationStateFile.tissueExportPath != null) {
			saveData(getFilePath(SimulationStateFile.tissueExportPath));
		}
	}

	private File getFilePath(File file) {
		File originalFile = file;
		if (file != null && file.exists()) {
			int i = 2;
			do {
				file = new File(
						originalFile
								.getAbsolutePath()
								.substring(
										0,
										(originalFile.getAbsolutePath()
												.length() - (SimulationStateFile.FILEEXTENSION
												.length() + 1)))
								+ "_"
								+ i
								+ "."
								+ SimulationStateFile.FILEEXTENSION);
				i++;
			} while (file.exists());
		}
		return file;
	}

	private void saveData(File path) {

		SimulationStateData simStateData = new SimulationStateData();
		simStateData.updateData();

		Element headerElement = createElement(EPISIM_TISSUE_SIMULATION_HEADER);

		headerElement
				.setAttribute(EPISIM_VERSION, EpisimSimulator.versionID);
		GregorianCalendar cal = new GregorianCalendar();
		headerElement.setAttribute(EXPORT_DATE, cal.getTime().toString());

		Element modelFileElement = createElement(CELLBEHAVIORALMODEL_FILE);
		modelFileElement.setAttribute(MODELFILE, simStateData
				.getLoadedModelFile().getAbsolutePath());
		headerElement.appendChild(modelFileElement);

		Element simStepElement = createElement(SIMSTEP);
		simStepElement.setAttribute(VALUE,
				Long.toString(simStateData.getSimStepNumber()));
		headerElement.appendChild(simStepElement);

		getRoot().appendChild(headerElement);

		getRoot().appendChild(
				simStateData.getTissueBorder().toXMLNode(TISSUE_BORDER, this));

		if (ModelController.getInstance().getModelDimensionality() == ModelDimensionality.TWO_DIMENSIONAL) {
			getRoot()
					.appendChild(
							simStateData
									.getExtraCellularDiffusionFieldArray2D()
									.toXMLNode(
											EXTRACELLULARDIFFUSIONFIELDARRAY2D,
											this));
		}

		if (ModelController.getInstance().getModelDimensionality() == ModelDimensionality.THREE_DIMENSIONAL) {
			getRoot()
					.appendChild(
							simStateData
									.getExtraCellularDiffusionFieldArray3D()
									.toXMLNode(
											EXTRACELLULARDIFFUSIONFIELDARRAY3D,
											this));
		}

		getRoot().appendChild(
				simStateData.getEpisimCellBehavioralModelGlobalParameters()
						.toXMLNode(EPISIMCELLBEHAVIORALMODELGLOBALPARAMETERS,
								this));

		getRoot().appendChild(
				simStateData.getEpisimBioMechanicalModelGlobalParameters()
						.toXMLNode(EPISIMBIOMECHANICALMODELGLOBALPARAMETERS,
								this));

		getRoot().appendChild(
				simStateData.getMiscalleneousGlobalParameters().toXMLNode(
						MISCALLENEOUSGLOBALPARAMETERS, this));

		getRoot().appendChild(cellListToXML(simStateData.getCells(), CELLS));

		save(path);
	}

	public Node cellListToXML(ArrayList<XmlUniversalCell> cells, String nodeName) {
		Element cellsNode = createElement(nodeName);
		for (XmlUniversalCell xCell : cells) {
			cellsNode.appendChild(xCell.toXMLNode(CELL, this));
		}
		return cellsNode;
	}

}
