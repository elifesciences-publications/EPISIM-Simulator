package sim.app.episim.tissue;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.snapshot.SnapshotObject;
import sim.app.episim.snapshot.SnapshotReader;
import sim.app.episim.util.TissueRotator;
import sim.app.episim.visualization.CellEllipse;


public class TissueImporter {
	
	
	//-------------------------------------------------------------------------------------------------------------------
	// Available XML Elements in File
	//-------------------------------------------------------------------------------------------------------------------
	
	private static final String AREA = "Area";
	private static final String BASALLAMINA = "BasalLamina";
	private static final String CELL = "Cell";  
	private static final String CELLID = "CellID";
	private static final String CELLS = "Cells";
	private static final String CENTROID  = "Centroid";
	private static final String DIST2BL = "Dist2BL";
	private static final String EPIDERMIS  = "Epidermis";
	private static final String IMAGE = "Image";
	private static final String HEIGHT = "Height";
	private static final String MAJORAXISLENGTH = "MajorAxisLength";
	private static final String MAXTHICKNESS = "MaxThickness";
	private static final String MEANTHICKNESS = "MeanThickness";
	private static final String MINORAXISLENGTH = "MinorAxisLength";
	private static final String NUCLEI = "Nuclei";
	private static final String NUCLEUS = "Nucleus";
	private static final String ORIENTATION  = "Orientation";
	private static final String PIXEL = "Pixel";
	private static final String RESOLUTION = "Resolution";
	private static final String SOLIDITY = "Solidity";
	private static final String SURFACE = "Surface";
	private static final String TISSUE = "TissueParams";
	private static final String WIDTH = "Width";
	private static final String X = "X";
	private static final String Y = "Y";
	//-------------------------------------------------------------------------------------------------------------------
	
	
	
	
	private static TissueImporter instance;
	
	
	private ImportedTissue actImportedTissue;

	private double scalingFactor = 1;
	
	private Node nuclei = null;
	
	private ArrayList<CellEllipse> importedCells;
	
	private double surfaceOrientation = 0;
	
	protected TissueImporter(){
		
	} 
		
	
	private void reset(){
		scalingFactor = 1;
		surfaceOrientation = 0;
		nuclei = null;
		actImportedTissue = new ImportedTissue();
		importedCells = new ArrayList<CellEllipse>();
	}
	
	public ImportedTissue loadTissue(File path){
		
		
		if(path != null ){
			reset();
			
			loadXML(path);
		   TissueRotator rotator = new TissueRotator();
		   rotator.rotateTissue(actImportedTissue, surfaceOrientation);
			return actImportedTissue;
			
		}
		  
		
		else{
			ExceptionDisplayer.getInstance().displayException(new NullPointerException("Tissue Importer: Filepath was null!"));
			return null;
		}
	}
	
	private void addValue(String value){
	/*	if(scannerState.equals(BASALLAMINA)){
			String[] strings = value.split("\t");
			if(strings.length == 2){
				basalLayerPoints.add(new Point2D.Double(Double.parseDouble(strings[0]), Double.parseDouble(strings[1])));
		
			} 
		}
		else if(scannerState.equals(TISSUEDESCRIPTION)) tissueDescription = value;
		else if(scannerState.equals(IMAGEID)) imageid = value;
		else if(scannerState.equals(RESOLUTION)){
			resolution = Double.parseDouble(value);
		}
		else if(scannerState.equals(SURFACE)){
			String[] strings = value.split("\t");
			if(strings.length == 2){
				surfacePoints.add(new Point2D.Double(Double.parseDouble(strings[0]), Double.parseDouble(strings[1])));
		
			} 
		}
		else if(scannerState.equals(FULLCONTOUR)){
			//Ignore
		}
		else if(scannerState.equals(EPIDERMALWIDTH)){
			epidermalWidth = Double.parseDouble(value);
		}
		else if(scannerState.equals(MEANEPIDERMALTHICKNESS)){ 
			meanEpidermalThickness = Double.parseDouble(value);
		}
		else if(scannerState.equals(MAXIMUMEPIDERMALTHICKNESS)){ 
			maximumEpidermalThickness = Double.parseDouble(value);
		}
		*/
		
	}
	private void loadXML(File file){
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = null;
      Document document = null;
      try{
	      builder = factory.newDocumentBuilder();
         document = builder.parse(file);
      }
	 	catch (ParserConfigurationException e){
	      ExceptionDisplayer.getInstance().displayException(e);
	 	}
      catch (SAXException e){
      	ExceptionDisplayer.getInstance().displayException(e);
      }
      catch (IOException e){
      	ExceptionDisplayer.getInstance().displayException(e);
      }
      
      if(document != null)processRootElement(document.getDocumentElement());
	}
	
	
	private void processRootElement(Element elem){
		boolean imageTagFound = false;
		if(elem.getNodeName().equals(TISSUE)) {
			NodeList rootChildren = elem.getChildNodes();
			
			for(int i= 0; i < rootChildren.getLength() && !imageTagFound; i++){
				Node actNode = rootChildren.item(i);
				if(actNode.getNodeName().equals(IMAGE)){
					imageTagFound = true;
					processImageElement(actNode);
				}
			}
			for(int i= 0; i < rootChildren.getLength(); i++){
				Node actNode = rootChildren.item(i);
				
				if(actNode.getNodeName().equals(IMAGE)&&!imageTagFound) processImageElement(actNode);
				else if(actNode.getNodeName().equals(EPIDERMIS)) processEpidermisElement(actNode);
				else if(actNode.getNodeName().equals(BASALLAMINA)) processBasallaminaElement(actNode);
				else if(actNode.getNodeName().equals(SURFACE)) processSurfaceElement(actNode);
				else if(actNode.getNodeName().equals(CELLS)) processCellsElement(actNode);
				else if(actNode.getNodeName().equals(NUCLEI)) processNucleiElement(actNode);
				
			}
		}
	}
	
	private void processImageElement(Node node){
		double height = 1;
		double width = 1;
		NodeList imageChildren = node.getChildNodes();
		for(int i = 0; i < imageChildren.getLength(); i++){
			Node actNode = imageChildren.item(i);
			if(actNode.getNodeName().equals(RESOLUTION)){
				NamedNodeMap attr = actNode.getAttributes();
			   this.actImportedTissue.setResolutionInMicrometerPerPixel(Double.parseDouble(attr.getNamedItem("value").getNodeValue()));
			}
			else if(actNode.getNodeName().equals(HEIGHT)){
				NamedNodeMap attr = actNode.getAttributes();
			   height = Double.parseDouble(attr.getNamedItem("value").getNodeValue());
			}
			else if(actNode.getNodeName().equals(WIDTH)){
				NamedNodeMap attr = actNode.getAttributes();
			   width = Double.parseDouble(attr.getNamedItem("value").getNodeValue());
			}
		}
		this.scalingFactor = calculateScalingFactor(height, width);
		this.actImportedTissue.setScalingFactor(this.scalingFactor);
		this.actImportedTissue.setEpidermalHeight(height*this.scalingFactor);
		this.actImportedTissue.setEpidermalWidth(width*this.scalingFactor);
		this.actImportedTissue.setResolutionInMicrometerPerPixel(this.actImportedTissue.getResolutionInMicrometerPerPixel()/this.scalingFactor);
	}
	
	private void processEpidermisElement(Node node){
		NodeList imageChildren = node.getChildNodes();
		for(int i = 0; i < imageChildren.getLength(); i++){
			Node actNode = imageChildren.item(i);
			if(actNode.getNodeName().equals(MEANTHICKNESS)){
				NamedNodeMap attr = actNode.getAttributes();
			   this.actImportedTissue.setMeanEpidermalThickness(Double.parseDouble(attr.getNamedItem("value").getNodeValue())*this.scalingFactor);
			}
			else if(actNode.getNodeName().equals(MAXTHICKNESS)){
				NamedNodeMap attr = actNode.getAttributes();
				this.actImportedTissue.setMaximumEpidermalThickness(Double.parseDouble(attr.getNamedItem("value").getNodeValue())*this.scalingFactor);
			}
		}
	}
	
	private void processBasallaminaElement(Node node){
		ArrayList<Point2D> basallaminaPoints = new ArrayList<Point2D>();
		addAllPointsXY(node.getChildNodes(), basallaminaPoints);
		this.actImportedTissue.setBasalLayerPoints(basallaminaPoints);
	}
	
	private void processSurfaceElement(Node node){
		ArrayList<Point2D> surfacePoints = new ArrayList<Point2D>();
		addAllPointsXY(node.getChildNodes(), surfacePoints);
		this.actImportedTissue.setSurfacePoints(surfacePoints);
	}
	
	private void processCellsElement(Node node){
		processCellOrNucleiData(node.getChildNodes(), true);
		if(this.nuclei != null) processCellOrNucleiData(this.nuclei.getChildNodes(), false);
		this.actImportedTissue.setCells(importedCells);
	}
	
	
	private void processCellOrNucleiData(NodeList cellsOrNuclei, boolean isCells){
		double majorAxis=0, minorAxis=0, height=0, width=0, solidity=0, distanceToBL=0,centroidX=0, centroidY=0;
		int cellID = 0, area=0, orientation=0;
		
		for(int i = 0; i < cellsOrNuclei.getLength(); i++){
			Node actNode = cellsOrNuclei.item(i);
			if(actNode.getNodeName() != null && (actNode.getNodeName().equals(CELL)||actNode.getNodeName().equals(NUCLEUS))){
				if(isCells){
					cellID = Integer.parseInt(actNode.getAttributes().getNamedItem("id").getNodeValue());
				}
				
				NodeList children = actNode.getChildNodes();
				for(int n = 0; n < children.getLength(); n++){
					Node actChildNode = children.item(n);
					if(actChildNode.getNodeName().equals(AREA)) area= Integer.parseInt(actChildNode.getAttributes().getNamedItem("value").getNodeValue());
					else if(actChildNode.getNodeName().equals(ORIENTATION)) orientation= Integer.parseInt(actChildNode.getAttributes().getNamedItem("value").getNodeValue());
					else if(actChildNode.getNodeName().equals(CENTROID)){ 
						centroidX = Double.parseDouble(actChildNode.getAttributes().getNamedItem("value1").getNodeValue())*this.scalingFactor;
						centroidY = Double.parseDouble(actChildNode.getAttributes().getNamedItem("value2").getNodeValue())*this.scalingFactor;
					}
					else if(actChildNode.getNodeName().equals(MAJORAXISLENGTH)) majorAxis= Double.parseDouble(actChildNode.getAttributes().getNamedItem("value").getNodeValue())*this.scalingFactor;
					else if(actChildNode.getNodeName().equals(MINORAXISLENGTH)) minorAxis= Double.parseDouble(actChildNode.getAttributes().getNamedItem("value").getNodeValue())*this.scalingFactor;
					else if(actChildNode.getNodeName().equals(HEIGHT)) height= Double.parseDouble(actChildNode.getAttributes().getNamedItem("value").getNodeValue())*this.scalingFactor;
					else if(actChildNode.getNodeName().equals(WIDTH)) width= Double.parseDouble(actChildNode.getAttributes().getNamedItem("value").getNodeValue())*this.scalingFactor;
					else if(actChildNode.getNodeName().equals(SOLIDITY)) solidity= Double.parseDouble(actChildNode.getAttributes().getNamedItem("value").getNodeValue());
					else if(actChildNode.getNodeName().equals(DIST2BL)) distanceToBL= Double.parseDouble(actChildNode.getAttributes().getNamedItem("value").getNodeValue())*this.scalingFactor;
					else if(!isCells && actChildNode.getNodeName().equals(CELLID)) cellID= Integer.parseInt(actChildNode.getAttributes().getNamedItem("value").getNodeValue());
				}
				
				if(isCells){
					this.importedCells.add(new CellEllipse(cellID, (int) centroidX, (int) centroidY, (int) majorAxis, (int)minorAxis, (int)height, (int)width, orientation,area, solidity, distanceToBL, new Color(cellID, cellID, cellID)));
				}
				else{
					CellEllipse cell =this.importedCells.get(cellID-1);
					if(cell!= null)cell.setNucleus(
							cell.new Nucleus(cellID, (int) centroidX, (int) centroidY,(int) majorAxis, (int)minorAxis,(int) height, (int)width, orientation, area, solidity, distanceToBL, Color.RED));
				}
			}
		}
		
		
		
	}
	
	
	
	
	
	private void processNucleiElement(Node node){
		if(this.importedCells.size() > 0) processCellOrNucleiData(node.getChildNodes(), false);
		else this.nuclei = node;
	}
	
	private void addAllPointsXY(NodeList pointNodes, List<Point2D> pointList){
		double x=0,y=0;
		for(int i = 0; i < pointNodes.getLength(); i++){
			Node actNode = pointNodes.item(i);
			if(actNode.getNodeName().equals(PIXEL)){
				NodeList points =actNode.getChildNodes();
				for(int n = 0; n < points.getLength(); n++){
					if(points.item(n).getNodeName().equals(X)) x = Double.parseDouble(points.item(n).getAttributes().getNamedItem("value").getNodeValue());
					else if(points.item(n).getNodeName().equals(Y)) y = Double.parseDouble(points.item(n).getAttributes().getNamedItem("value").getNodeValue());
				}
				pointList.add(new Point2D.Double(x, y));
			}
			else if(actNode.getNodeName().equals(ORIENTATION)){				
				surfaceOrientation = Double.parseDouble(actNode.getAttributes().getNamedItem("value").getNodeValue());
			}
		}
	}
		
	private double calculateScalingFactor(double height, double width){
		final double WIDTHFACT = 0.5;
		final double HEIGHTFACT = 0.7;
		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		if(height > width) return (screenDim.getHeight()*HEIGHTFACT)/height;
		else if(height < width) return (screenDim.getWidth()*WIDTHFACT)/width;
		else{
			if((screenDim.getHeight()*HEIGHTFACT) > (screenDim.getWidth()*WIDTHFACT)) return (screenDim.getWidth()*WIDTHFACT)/width;
			else return (screenDim.getHeight()*HEIGHTFACT)/height;
		}
	}
}