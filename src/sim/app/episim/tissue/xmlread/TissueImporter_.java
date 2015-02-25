package sim.app.episim.tissue.xmlread;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import sim.app.episim.EpisimExceptionHandler;
import sim.app.episim.tissue.ImportedTissue_;
import sim.app.episim.tissue.TissueRotator_;

public class TissueImporter_ {

	// -------------------------------------------------------------------------------------------------------------------
	// Available XML Elements in File
	// -------------------------------------------------------------------------------------------------------------------

	private static final String TISSUEPARAMS = "TissueParams";
	private static final String IMAGE = "Image";
	private static final String EPIDERMIS = "Epidermis";
	private static final String MARKERPROFILE = "markerProfile";
	private static final String SURFACE = "Surface";
	private static final String BASALLAMINA = "BasalLamina";
	private static final String CELLS = "Cells";
	private static final String NUCLEI = "Nuclei";
	private static final String CELL = "Cell";
	private static final String NUCLEUS = "Nucleus";
	private static final String RESOLUTIONNM = "ResolutionNM";
	private static final String HEIGHT = "Height";
	private static final String WIDTH = "Width";
	private static final String TISSUEID = "TissueID";
	private static final String MEANTHICKNESS = "MeanThickness";
	private static final String MAXTHICKNESS = "MaxThickness";
	private static final String ORIENTATION = "Orientation";
	private static final String PIXEL = "Pixel";
	private static final String MARKERPROFILEX = "markerProfileX";
	private static final String MARKERPROFILEY = "markerProfileY";
	private static final String DIST2BLABS = "Dist2BlAbs";
	private static final String DIST2BLNORM = "Dist2BlNorm";
	private static final String LAYER = "Layer";
	private static final String AREA = "Area";
	private static final String PERIMETER = "Perimeter";
	private static final String CENTER = "Center";
	private static final String MAJORAXISLENGTH = "MajorAxisLength";
	private static final String RATIOAXIS = "ratioAxis";
	private static final String ECCENTRICITY = "Eccentricity";
	private static final String ORIENTATIONX = "OrientationX";
	private static final String SOLIDITY = "Solidity";
	private static final String RATIOLEN = "RatioLen";
	private static final String ROUNDNESS = "Roundness";
	private static final String NNUCLEI = "nNuclei";
	private static final String NUCLEIID = "nucleiID";
	private static final String NNEIGHBOUR = "nNeighbour";
	private static final String NEIGHBOURID = "neighbourID";
	private static final String RATIONUC2CP = "ratioNuc2Cp";
	private static final String NUCDENSITY = "nucDensity";
	private static final String MEANINT = "MeanInt";
	private static final String MEDIANINT = "MedianInt";
	private static final String QUANT75INT = "Quant75Int";
	private static final String CELLINTR = "cellIntR";
	private static final String CELLINTG = "cellIntG";
	private static final String CELLINTB = "cellIntB";
	private static final String MEMINTR = "memIntR";
	private static final String MEMINTG = "memIntG";
	private static final String MEMINTB = "memIntB";
	private static final String NUCINTR = "nucIntR";
	private static final String NUCINTG = "nucIntG";
	private static final String NUCINTB = "nucIntB";
	private static final String CYTINTR = "cytIntR";
	private static final String CYTINTG = "cytIntG";
	private static final String CYTINTB = "cytIntB";
	private static final String ORIENTATIONBL = "OrientationBL";
	private static final String MINORAXISLENGTH = "MinorAxisLength";
	private static final String DISTBLABS = "DistBlAbs";

	private static final String NUCLEUSID = "nucleiID";
	private static final String DIST2BL = "Dist2BlAbs";
	private static final String N_NEIGHBOUR = "nNeighbour";
	private static final String NEIGHBOUR_IDS = "neighbourID";
	private static final String ORIENTATION_X = "OrientationX";
	private static final String RESOLUTION_IN_NM = "ResolutionNM";
	private static final String TISSUE = "TissueParams";
	private static final String TISSUE_IMAGE_ID = "TissueID";
	private static final String X = "X";
	private static final String Y = "Y";
	// -------------------------------------------------------------------------------------------------------------------

	private static TissueImporter_ instance;

	private ImportedTissueData actImportedTissue;

	private double scalingFactor = 1;

	private Node cellsNode = null;

	private ArrayList<ImportedNucleusData> importedNuclei;
	private ArrayList<ImportedCellData> importedCells;

	private double surfaceOrientation = 0;

	public static final double ELLIPSE_AXIS_LENGHT_CORR_FACTOR = 1.2;

	public static void main(String[] args) {
		TissueImporter_ ti = new TissueImporter_();
		ti.loadTissue(new File("test.xml"));
	}

	protected TissueImporter_() {

	}

	public static TissueImporter_ getInstance() {
		if (instance == null)
			instance = new TissueImporter_();
		return instance;
	}

	private void reset() {
		scalingFactor = 1;
		surfaceOrientation = 0;
		cellsNode = null;
		actImportedTissue = new ImportedTissueData();
		importedNuclei = new ArrayList<ImportedNucleusData>();
		importedCells = new ArrayList<ImportedCellData>();
	}

	public ImportedTissue_ loadTissue(File path) {
		ImportedTissue_ tissue;
		if (path != null) {
			reset();

			loadXML(path);
			tissue = new ImportedTissue_(actImportedTissue);
			TissueRotator_ rotator = new TissueRotator_();
			rotator.rotateTissue(tissue, surfaceOrientation);
			return tissue;

		}

		else {
			EpisimExceptionHandler.getInstance().displayException(
					new NullPointerException(
							"Tissue Importer: Filepath was null!"));
			return null;
		}
	}

	private void addValue(String value) {
		/*
		 * if(scannerState.equals(BASALLAMINA)){ String[] strings =
		 * value.split("\t"); if(strings.length == 2){ basalLayerPoints.add(new
		 * Point2D.Double(Double.parseDouble(strings[0]),
		 * Double.parseDouble(strings[1])));
		 * 
		 * } } else if(scannerState.equals(TISSUEDESCRIPTION)) tissueDescription
		 * = value; else if(scannerState.equals(IMAGEID)) imageid = value; else
		 * if(scannerState.equals(RESOLUTION)){ resolution =
		 * Double.parseDouble(value); } else if(scannerState.equals(SURFACE)){
		 * String[] strings = value.split("\t"); if(strings.length == 2){
		 * surfacePoints.add(new Point2D.Double(Double.parseDouble(strings[0]),
		 * Double.parseDouble(strings[1])));
		 * 
		 * } } else if(scannerState.equals(FULLCONTOUR)){ //Ignore } else
		 * if(scannerState.equals(EPIDERMALWIDTH)){ epidermalWidth =
		 * Double.parseDouble(value); } else
		 * if(scannerState.equals(MEANEPIDERMALTHICKNESS)){
		 * meanEpidermalThickness = Double.parseDouble(value); } else
		 * if(scannerState.equals(MAXIMUMEPIDERMALTHICKNESS)){
		 * maximumEpidermalThickness = Double.parseDouble(value); }
		 */

	}

	private void loadXML(File file) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		Document document = null;
		try {
			builder = factory.newDocumentBuilder();
			document = builder.parse(file);
		} catch (ParserConfigurationException e) {
			EpisimExceptionHandler.getInstance().displayException(e);
		} catch (SAXException e) {
			EpisimExceptionHandler.getInstance().displayException(e);
		} catch (IOException e) {
			EpisimExceptionHandler.getInstance().displayException(e);
		}

		if (document != null)
			processRootElement(document.getDocumentElement());
	}

	private void processRootElement(Element elem) {
		boolean imageTagFound = false;
		if (elem.getNodeName().equals(TISSUEPARAMS)) {
			NodeList rootChildren = elem.getChildNodes();

			for (int i = 0; i < rootChildren.getLength() && !imageTagFound; i++) {
				Node actNode = rootChildren.item(i);
				if (actNode.getNodeName().equals(IMAGE)) {
					imageTagFound = true;
					processImageElement(actNode);
				}
			}
			for (int i = 0; i < rootChildren.getLength(); i++) {
				Node actNode = rootChildren.item(i);

				if (actNode.getNodeName().equals(IMAGE) && !imageTagFound)
					processImageElement(actNode);
				else if (actNode.getNodeName().equals(EPIDERMIS))
					processEpidermisElement(actNode);
				else if (actNode.getNodeName().equals(BASALLAMINA))
					processBasallaminaElement(actNode);
				else if (actNode.getNodeName().equals(SURFACE))
					processSurfaceElement(actNode);
				else if (actNode.getNodeName().equals(CELLS))
					processCellsElement(actNode);
				else if (actNode.getNodeName().equals(NUCLEI))
					processNucleiElement(actNode);

			}
		}
	}

	private void processImageElement(Node node) {
		double height = 1;
		double width = 1;
		NodeList imageChildren = node.getChildNodes();
		for (int i = 0; i < imageChildren.getLength(); i++) {
			Node actNode = imageChildren.item(i);
			if (actNode.getNodeName().equals(RESOLUTION_IN_NM)) {
				NamedNodeMap attr = actNode.getAttributes();
				this.actImportedTissue
						.setResolutionInMicrometerPerPixel(Double
								.parseDouble(attr.getNamedItem("value")
										.getNodeValue()) / 1000);
			} else if (actNode.getNodeName().equals(HEIGHT)) {
				NamedNodeMap attr = actNode.getAttributes();
				height = Double.parseDouble(attr.getNamedItem("value")
						.getNodeValue());
			} else if (actNode.getNodeName().equals(WIDTH)) {
				NamedNodeMap attr = actNode.getAttributes();
				width = Double.parseDouble(attr.getNamedItem("value")
						.getNodeValue());
			}
		}
		this.scalingFactor = calculateScalingFactor(height, width);
		this.actImportedTissue.setScalingFactor(this.scalingFactor);
		this.actImportedTissue.setEpidermalHeight(height * this.scalingFactor);
		this.actImportedTissue.setEpidermalWidth(width * this.scalingFactor);
		this.actImportedTissue
				.setResolutionInMicrometerPerPixel(this.actImportedTissue
						.getResolutionInMicrometerPerPixel()
						/ this.scalingFactor);
	}

	private void processEpidermisElement(Node node) {
		NodeList imageChildren = node.getChildNodes();
		for (int i = 0; i < imageChildren.getLength(); i++) {
			Node actNode = imageChildren.item(i);
			if (actNode.getNodeName().equals(MEANTHICKNESS)) {
				NamedNodeMap attr = actNode.getAttributes();
				this.actImportedTissue.setMeanEpidermalThickness(Double
						.parseDouble(attr.getNamedItem("value").getNodeValue())
						* this.scalingFactor);
			} else if (actNode.getNodeName().equals(MAXTHICKNESS)) {
				NamedNodeMap attr = actNode.getAttributes();
				this.actImportedTissue.setMaximumEpidermalThickness(Double
						.parseDouble(attr.getNamedItem("value").getNodeValue())
						* this.scalingFactor);
			} else if (actNode.getNodeName().equals(TISSUE_IMAGE_ID)) {
				NamedNodeMap attr = actNode.getAttributes();
				this.actImportedTissue.setTissueImageID(attr.getNamedItem(
						"value").getNodeValue());
			} else if (actNode.getNodeName().equals(ORIENTATION)) {
				surfaceOrientation = Double.parseDouble(actNode.getAttributes()
						.getNamedItem("value").getNodeValue());
			}
		}
	}

	private void processBasallaminaElement(Node node) {
		ArrayList<Point2D> basallaminaPoints = new ArrayList<Point2D>();
		addAllPointsXY(node.getChildNodes(), basallaminaPoints);
		this.actImportedTissue.setBasalLayerPoints(basallaminaPoints);
	}

	private void processSurfaceElement(Node node) {
		ArrayList<Point2D> surfacePoints = new ArrayList<Point2D>();
		addAllPointsXY(node.getChildNodes(), surfacePoints);
		this.actImportedTissue.setSurfacePoints(surfacePoints);
	}

	private void processCellsElement(Node node) {
		if (this.importedNuclei.size() > 0) {
			processCellData(node.getChildNodes());
			this.actImportedTissue.setCells(importedCells);
		} else
			this.cellsNode = node;

	}

	private void processCellData(NodeList cells) {
		for (int i = 0; i < cells.getLength(); i++) {
			Node actNode = cells.item(i);
			if (actNode.getNodeName() != null
					&& actNode.getNodeName().equals(CELL)) {
				long ID = Long.parseLong(actNode.getAttributes()
						.getNamedItem("id").getNodeValue());
				ImportedCellData cell = new ImportedCellData(ID);

				NodeList children = actNode.getChildNodes();
				for (int n = 0; n < children.getLength(); n++) {
					Node actChildNode = children.item(n);
					cell.setAttribute(actChildNode.getNodeName(),
							generateAttValueMap(actChildNode.getAttributes()));
				}
				this.importedCells.add(cell);
			}
		}
	}

	private void processNucleiData(NodeList Nuclei) {

		for (int i = 0; i < Nuclei.getLength(); i++) {

			Node actNode = Nuclei.item(i);
			if (actNode.getNodeName() != null
					&& actNode.getNodeName().equals(NUCLEUS)) {
				long ID = Long.parseLong(actNode.getAttributes()
						.getNamedItem("id").getNodeValue());
				ImportedNucleusData nucleus = new ImportedNucleusData(ID);

				NodeList children = actNode.getChildNodes();
				for (int n = 0; n < children.getLength(); n++) {
					Node actChildNode = children.item(n);
					try {
						nucleus.setAttribute(actChildNode.getNodeName(),
								generateAttValueMap(actChildNode
										.getAttributes()));
					} catch (NumberFormatException e) {
						System.out.println("skipping Node: "+ID+" because of attribute: "+actChildNode.getNodeName());
						nucleus = null;
						break;
					}
				}
				if(nucleus!=null)
				this.importedNuclei.add(nucleus);
			}

		}
	}

	private HashMap<String, String> generateAttValueMap(NamedNodeMap attributes) {
		HashMap<String, String> attValMap = new HashMap<String, String>();
		if (attributes != null)
			for (int n = 0; n < attributes.getLength(); n++) {
				Node attribute = attributes.item(n);
				attValMap
						.put(attribute.getNodeName(), attribute.getNodeValue());
			}
		return attValMap;

	}

	private void processNucleiElement(Node node) {
		processNucleiData(node.getChildNodes());
		if (this.cellsNode != null) {
			processCellData(this.cellsNode.getChildNodes());
			this.actImportedTissue.setCells(importedCells);
		}
		this.actImportedTissue.setNuclei(importedNuclei);
	}

	private void addAllPointsXY(NodeList pointNodes, List<Point2D> pointList) {
		double x = 0, y = 0;
		HashSet<String> alreadyAddedCoordinates = new HashSet<String>();

		for (int i = 0; i < pointNodes.getLength(); i++) {
			Node actNode = pointNodes.item(i);
			if (actNode.getNodeName().equals(PIXEL)) {
				NodeList points = actNode.getChildNodes();
				for (int n = 0; n < points.getLength(); n++) {
					if (points.item(n).getNodeName().equals(X))
						x = Double.parseDouble(points.item(n).getAttributes()
								.getNamedItem("value").getNodeValue());
					else if (points.item(n).getNodeName().equals(Y))
						y = Double.parseDouble(points.item(n).getAttributes()
								.getNamedItem("value").getNodeValue());
				}
				String keyString = x + ";" + y;
				if (!alreadyAddedCoordinates.contains(keyString)) {
					pointList.add(new Point2D.Double(x, y));
					alreadyAddedCoordinates.add(keyString);
				}
			}

		}
	}
	
	private double calculateScalingFactor(double height, double width) {
		final double WIDTHFACT = 0.6;
		final double HEIGHTFACT = 0.8;
		return 1;
		/*
		 * Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		 * if(height > width) return (screenDim.getHeight()*HEIGHTFACT)/height;
		 * else if(height < width) return
		 * (screenDim.getWidth()*WIDTHFACT)/width; else{
		 * if((screenDim.getHeight()*HEIGHTFACT) >
		 * (screenDim.getWidth()*WIDTHFACT)) return
		 * (screenDim.getWidth()*WIDTHFACT)/width; else return
		 * (screenDim.getHeight()*HEIGHTFACT)/height; }
		 */
	}

}