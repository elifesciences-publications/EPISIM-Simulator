package sim.app.episim.tissue;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.geom.Point2D;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import sim.app.episim.model.visualization.CellEllipse;
import sim.app.episim.model.visualization.NucleusEllipse;
import sim.app.episim.util.ClassLoaderChangeListener;
import sim.app.episim.util.GlobalClassLoader;



public class TissueImporter{
	
	
//-------------------------------------------------------------------------------------------------------------------
// Available XML Elements in File
//-------------------------------------------------------------------------------------------------------------------
	
	private static final String AREA = "Area";
	private static final String BASALLAMINA = "BasalLamina";
	private static final String CELL = "Cell";  
	private static final String NUCLEUSID = "nucleiID";
	private static final String CELLS = "Cells";
	private static final String CENTER  = "Center";
	private static final String DIST2BL = "Dist2BlAbs";
	private static final String EPIDERMIS  = "Epidermis";
	private static final String IMAGE = "Image";
	private static final String HEIGHT = "Height";
	private static final String MAJORAXISLENGTH = "MajorAxisLength";
	private static final String MAXTHICKNESS = "MaxThickness";
	private static final String MEANTHICKNESS = "MeanThickness";
	private static final String MINORAXISLENGTH = "MinorAxisLength";
	private static final String NUCLEI = "Nuclei";
	private static final String NUCLEUS = "Nucleus";
	private static final String N_NEIGHBOUR = "nNeighbour";
	private static final String NEIGHBOUR_IDS = "neighbourID";
	private static final String ORIENTATION  = "Orientation";
	private static final String ORIENTATION_X  = "OrientationX";
	private static final String PERIMETER  = "Perimeter";
	private static final String PIXEL = "Pixel";
	private static final String RESOLUTION_IN_NM = "ResolutionNM";
	private static final String SOLIDITY = "Solidity";
	private static final String SURFACE = "Surface";
	private static final String TISSUE = "TissueParams";
	private static final String TISSUE_IMAGE_ID = "TissueID";
	private static final String WIDTH = "Width";
	private static final String X = "X";
	private static final String Y = "Y";
//-------------------------------------------------------------------------------------------------------------------
	
	
	
	
	
	
	private ImportedTissue actImportedTissue;

	private double scalingFactor = 1;
	
	private Node cellsNode = null;
	
	private ArrayList<NucleusEllipse> importedNuclei;
	private ArrayList<CellEllipse> importedCellEllipses;
	
	private double surfaceOrientation = 0;
	
	
	public static final double ELLIPSE_AXIS_LENGHT_CORR_FACTOR = 1.2;
	
	protected TissueImporter(){
		
	} 
		
	
	private void reset(){
		scalingFactor = 1;
		surfaceOrientation = 0;
		cellsNode = null;
		actImportedTissue = new ImportedTissue();
		importedNuclei = new ArrayList<NucleusEllipse>();
		importedCellEllipses = new ArrayList<CellEllipse>();
	}
	
	public ImportedTissue loadTissue(File path){
		
		
		if(path != null ){
			reset();
			
			loadXML(path);
		   TissueRotator rotator = new TissueRotator();
		   rotator.rotateTissue(actImportedTissue, surfaceOrientation);
		   TissueFilter filter = new TissueFilter(new File(path.getAbsolutePath().substring(0, path.getAbsolutePath().length()-4)+"_Filter-Results.txt"));
		   filter.filterTissue(actImportedTissue);
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
			if(actNode.getNodeName().equals(RESOLUTION_IN_NM)){
				NamedNodeMap attr = actNode.getAttributes();
			   this.actImportedTissue.setResolutionInMicrometerPerPixel(Double.parseDouble(attr.getNamedItem("value").getNodeValue())/1000);
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
			else if(actNode.getNodeName().equals(TISSUE_IMAGE_ID)){
				NamedNodeMap attr = actNode.getAttributes();
				this.actImportedTissue.setTissueImageID(attr.getNamedItem("value").getNodeValue());
			}
			else if(actNode.getNodeName().equals(ORIENTATION)){				
				surfaceOrientation = Double.parseDouble(actNode.getAttributes().getNamedItem("value").getNodeValue());
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
		if(this.importedNuclei.size() > 0){ 
			processCellOrNucleiData(node.getChildNodes(), true);
			this.actImportedTissue.setCells(importedCellEllipses);
		}
		else this.cellsNode = node;
		
	}
	
	
	private void processCellOrNucleiData(NodeList cellsOrNuclei, boolean isCells){
		double majorAxis=0, minorAxis=0, height=0, width=0, solidity=0, distanceToBL=0,centerX=0, centerY=0, perimeter=0;
		int ID = 0, area=0, orientation=0, nucleusID = 0;
		BufferedWriter bout = null;
		if(isCells){
		/*	try{
	         bout = new BufferedWriter(new FileWriter(new File("d:/cellAreaAxis.csv")));
	         bout.write("major; minor; area;\n");
         }
         catch (IOException e){
	         
	         e.printStackTrace();
         }*/
			
		}
		
		
		for(int i = 0; i < cellsOrNuclei.getLength(); i++){
			int numberOfNeighbours = 0;
			Node actNode = cellsOrNuclei.item(i);
			Node neighboursNode = null;
			int[] neighbourCellIds = null;
			nucleusID = 0;
			if(actNode.getNodeName() != null && (actNode.getNodeName().equals(CELL)||actNode.getNodeName().equals(NUCLEUS))){
				
				ID = Integer.parseInt(actNode.getAttributes().getNamedItem("id").getNodeValue());
					
				
				
				NodeList children = actNode.getChildNodes();
				for(int n = 0; n < children.getLength(); n++){
					Node actChildNode = children.item(n);
					if(actChildNode.getNodeName().equals(AREA)) area= Integer.parseInt(actChildNode.getAttributes().getNamedItem("value").getNodeValue());
					else if(actChildNode.getNodeName().equals(ORIENTATION_X)) orientation= Integer.parseInt(actChildNode.getAttributes().getNamedItem("value").getNodeValue());
					else if(actChildNode.getNodeName().equals(PERIMETER)) perimeter= Double.parseDouble(actChildNode.getAttributes().getNamedItem("value").getNodeValue());
					else if(actChildNode.getNodeName().equals(CENTER)){ 
						centerX = Double.parseDouble(actChildNode.getAttributes().getNamedItem("value1").getNodeValue())*this.scalingFactor;
						centerY = Double.parseDouble(actChildNode.getAttributes().getNamedItem("value2").getNodeValue())*this.scalingFactor;
					}
					else if(actChildNode.getNodeName().equals(MAJORAXISLENGTH)){ 
						majorAxis= Double.parseDouble(actChildNode.getAttributes().getNamedItem("value").getNodeValue())*this.scalingFactor;
						if(isCells) majorAxis *= ELLIPSE_AXIS_LENGHT_CORR_FACTOR;
					}
					else if(actChildNode.getNodeName().equals(MINORAXISLENGTH)){ 
						minorAxis= Double.parseDouble(actChildNode.getAttributes().getNamedItem("value").getNodeValue())*this.scalingFactor;
						if(isCells) minorAxis *= ELLIPSE_AXIS_LENGHT_CORR_FACTOR;
					}
					else if(actChildNode.getNodeName().equals(HEIGHT)) height= Double.parseDouble(actChildNode.getAttributes().getNamedItem("value").getNodeValue())*this.scalingFactor;
					else if(actChildNode.getNodeName().equals(WIDTH)) width= Double.parseDouble(actChildNode.getAttributes().getNamedItem("value").getNodeValue())*this.scalingFactor;
					else if(actChildNode.getNodeName().equals(SOLIDITY)) solidity= Double.parseDouble(actChildNode.getAttributes().getNamedItem("value").getNodeValue());
					else if(actChildNode.getNodeName().equals(DIST2BL)) distanceToBL= Double.parseDouble(actChildNode.getAttributes().getNamedItem("value").getNodeValue())*this.scalingFactor;
					else if(actChildNode.getNodeName().equals(N_NEIGHBOUR)){ 
						numberOfNeighbours = Integer.parseInt(actChildNode.getAttributes().getNamedItem("value").getNodeValue());
						if(neighboursNode != null) neighbourCellIds =processNeighboursNode(neighboursNode, numberOfNeighbours);
					}
					else if(actChildNode.getNodeName().equals(NEIGHBOUR_IDS)){
						neighboursNode = actChildNode;
						if(numberOfNeighbours > 0) neighbourCellIds = processNeighboursNode(neighboursNode, numberOfNeighbours);
					}
					else if(isCells && actChildNode.getNodeName().equals(NUCLEUSID) && actChildNode.getAttributes().getNamedItem("value") != null){ 
						nucleusID= Integer.parseInt(actChildNode.getAttributes().getNamedItem("value").getNodeValue());
					}
				}
				
				if(isCells){
					/*if(height > width){
						double tmp = height;
						height = width;
						width = tmp;
					}*/
					
				/*	try{
	            //   bout.write(majorAxis+"; "+minorAxis+"; "+area+";\n");
               }
               catch (IOException e){
	              
	               e.printStackTrace();
               }*/
					
					CellEllipse actCell =new CellEllipse(ID, (int) centerX, (int) centerY, (int) majorAxis, (int)minorAxis, (int)height, (int)width, orientation,area, perimeter, solidity, distanceToBL, neighbourCellIds, Color.WHITE);
					NucleusEllipse nucleus =  null;
					if(nucleusID > 0) nucleus = this.importedNuclei.get(nucleusID -1);
					if(nucleus != null) actCell.setNucleus(nucleus);
					this.importedCellEllipses.add(actCell);
				}
				else{					
				this.importedNuclei.add(new NucleusEllipse(ID, (int) centerX, (int) centerY,(int) majorAxis, (int)minorAxis,(int) height, (int)width, orientation, area, perimeter, distanceToBL, Color.RED));
				}
			}
		}
		if(bout != null){
		/*	try{
	        bout.close();
         }
         catch (IOException e){
	         
	         e.printStackTrace();
         }*/
		}
	}
	
	private int[] processNeighboursNode(Node node, int numberOfNeighbours){
		int[] neighbouringCellIDs = new int[numberOfNeighbours];
		if(numberOfNeighbours > 1){
			for(int i = 1; i <= numberOfNeighbours; i++){
				neighbouringCellIDs[i-1] = Integer.parseInt(node.getAttributes().getNamedItem("value"+i).getNodeValue());
			}
		}
		else if(numberOfNeighbours == 1)neighbouringCellIDs[0] = Integer.parseInt(node.getAttributes().getNamedItem("value").getNodeValue());
		return neighbouringCellIDs;
	}
	
	private void processNucleiElement(Node node){
		processCellOrNucleiData(node.getChildNodes(), false);
		if(this.cellsNode != null){ 
			processCellOrNucleiData(this.cellsNode.getChildNodes(), true);
			this.actImportedTissue.setCells(importedCellEllipses);
		}
	}
	
	private void addAllPointsXY(NodeList pointNodes, List<Point2D> pointList){
		double x=0,y=0;
		HashSet<String> alreadyAddedCoordinates = new HashSet<String>();
		
		for(int i = 0; i < pointNodes.getLength(); i++){
			Node actNode = pointNodes.item(i);
			if(actNode.getNodeName().equals(PIXEL)){
				NodeList points =actNode.getChildNodes();
				for(int n = 0; n < points.getLength(); n++){
					if(points.item(n).getNodeName().equals(X)) x = Double.parseDouble(points.item(n).getAttributes().getNamedItem("value").getNodeValue())*this.scalingFactor;
					else if(points.item(n).getNodeName().equals(Y)) y = Double.parseDouble(points.item(n).getAttributes().getNamedItem("value").getNodeValue())*this.scalingFactor;
				}
				String keyString = x+";"+y;
				if(!alreadyAddedCoordinates.contains(keyString)){
					pointList.add(new Point2D.Double(x, y));
					alreadyAddedCoordinates.add(keyString);
				}
			}
		
		}
	}
		
	private double calculateScalingFactor(double height, double width){
		final double WIDTHFACT = 0.8;
		final double HEIGHTFACT = 0.8;
	//	return 1;
		
		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		if(height > width) return (screenDim.getHeight()*HEIGHTFACT)/height;
		else if(height < width) return (screenDim.getWidth()*WIDTHFACT)/width;
		else{
			if((screenDim.getHeight()*HEIGHTFACT) > (screenDim.getWidth()*WIDTHFACT)) return (screenDim.getWidth()*WIDTHFACT)/width;
			else return (screenDim.getHeight()*HEIGHTFACT)/height;
		}
	}
}