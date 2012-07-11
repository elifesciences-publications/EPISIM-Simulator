package sim.app.episim.model.visualization;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import episiminterfaces.EpisimCellBehavioralModel;



import sim.SimStateServer;
import sim.SimStateServer.EpisimSimulationState;
import sim.app.episim.AbstractCell;
import sim.app.episim.SimulationStateChangeListener;
import sim.app.episim.UniversalCell;
import sim.app.episim.gui.EpisimGUIState;
import sim.app.episim.gui.EpisimGUIState.SimulationDisplayProperties;
import sim.app.episim.model.biomechanics.hexagonbased.AbstractHexagonBasedMechanicalModel;
import sim.app.episim.model.biomechanics.hexagonbased.AbstractHexagonBasedMechanicalModelGP;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.tissue.TissueController;
import sim.app.episim.util.CellEllipseIntersectionCalculationRegistry;
import sim.app.episim.util.EllipseIntersectionCalculatorAndClipper;
import sim.app.episim.util.GenericBag;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.simple.HexagonalPortrayal2DHack;
import sim.util.Double2D;
import sim.util.Int2D;
import sim.util.MutableInt2D;


public class HexagonalCellPortrayal2D extends HexagonalPortrayal2DHack implements SimulationStateChangeListener{
	
	   
	
	 private static Color standardCellColor = new Color(255,210,210);
	 private static Color standardMembraneColor = new Color(150, 0, 0);
	
	 private static final double DELTA = 1;
	 private HexagonalCellGridPortrayal2D cellGridPortrayal;
	 
	 private HashMap<Long, DrawInfo2D> drawInfoRegistry;
	 private HashMap<Long, Long> simStepTimeStampRegistry;
	 
	 public HexagonalCellPortrayal2D(HexagonalCellGridPortrayal2D cellGridPortrayal) { this(cellGridPortrayal,standardCellColor,1.0,true); }
    public HexagonalCellPortrayal2D(HexagonalCellGridPortrayal2D cellGridPortrayal,Paint paint)  { this(cellGridPortrayal,paint,1.0,true); }
    public HexagonalCellPortrayal2D(HexagonalCellGridPortrayal2D cellGridPortrayal, double scale) { this(cellGridPortrayal,standardCellColor,scale,true); }
    public HexagonalCellPortrayal2D(HexagonalCellGridPortrayal2D cellGridPortrayal, boolean filled) { this(cellGridPortrayal,standardCellColor,1.0,filled); }
    public HexagonalCellPortrayal2D(HexagonalCellGridPortrayal2D cellGridPortrayal, Paint paint, double scale)  { this(cellGridPortrayal,paint,scale,true); }
    public HexagonalCellPortrayal2D(HexagonalCellGridPortrayal2D cellGridPortrayal, Paint paint, boolean filled)  { this(cellGridPortrayal,paint,1.0,filled); }
    public HexagonalCellPortrayal2D(HexagonalCellGridPortrayal2D cellGridPortrayal, double scale, boolean filled)  { this(cellGridPortrayal,standardCellColor,scale,filled); }
    
    
    private static long actSimStepNo = -1; 
    
    public HexagonalCellPortrayal2D(HexagonalCellGridPortrayal2D cellGridPortrayal, Paint paint, double scale, boolean filled)
    {
   	 super(paint, scale, filled);
   	 this.cellGridPortrayal = cellGridPortrayal;
   	 drawInfoRegistry = new HashMap<Long, DrawInfo2D>();
   	 simStepTimeStampRegistry = new HashMap<Long, Long>();
   	 SimStateServer.getInstance().addSimulationStateChangeListener(this);
    }
    
   
    
    public void draw(Object object, Graphics2D graphics, DrawInfo2D info)
    {
   	 if(object instanceof UniversalCell){   	 
   		 UniversalCell cell = (UniversalCell) object;   		 
   		
   		actSimStepNo = SimStateServer.getInstance().getSimStepNumber();  		  		 
   		 
   		filled = true;
   		AbstractHexagonBasedMechanicalModel mechModel = (AbstractHexagonBasedMechanicalModel)cell.getEpisimBioMechanicalModelObject();
   		AbstractHexagonBasedMechanicalModelGP globalParameters = (AbstractHexagonBasedMechanicalModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
   			
   		 
   		 if(mechModel.isSpreading()){
	 	      	if(this.drawInfoRegistry.containsKey(cell.getID()) 
	 	      			&& this.simStepTimeStampRegistry.get(cell.getID()) == actSimStepNo){
	 	      		
	 	      		Double2D fieldLoc = mechModel.getLocationInMikron();
	 	      		Double2D spreadingLoc =mechModel.getSpreadingLocationInMikron();	      		 
	 	      		spreadingLoc = mechModel.correctToroidalSpreadingCoordinatesInMikronForEllipseDrawing();	 	  	   	
	 	      		
	 	      		
	 	      		double[] coordinatesAndDimensions = new double[]{((fieldLoc.x+spreadingLoc.x)/2d),((fieldLoc.y+ spreadingLoc.y)/2d),
									globalParameters.getInner_hexagonal_radius()*4,
									globalParameters.getInner_hexagonal_radius()*2};
	 	      					coordinatesAndDimensions = correctCoordinatesAndDimensions(coordinatesAndDimensions, info);
	 	      		
	 	      		
	 	      		drawSpreadingCell(graphics, cell, coordinatesAndDimensions[0], coordinatesAndDimensions[1], coordinatesAndDimensions[2], coordinatesAndDimensions[3],fieldLoc, spreadingLoc);
	 	      	}
	 	      	else{ 	      				
	 	  		      	this.drawInfoRegistry.put(cell.getID(), new DrawInfo2D(info.gui, info.fieldPortrayal, new Rectangle2D.Double(info.draw.x, info.draw.y, info.draw.width, info.draw.height),		      
	 	  			        		 new Rectangle2D.Double(info.clip.x, info.clip.y, info.clip.width, info.clip.height)));
	 	  		         this.drawInfoRegistry.get(cell.getID()).location = info.location;
	 	  		      	this.simStepTimeStampRegistry.put(cell.getID(), actSimStepNo);
	 	  		      	shape = null;	 	      		
	 	      	}
 	      	}
	 	      else{	 		     
	 	      	double[] coordinatesAndDimensions = new double[]{mechModel.getLocationInMikron().x,mechModel.getLocationInMikron().y,
																					 	      			globalParameters.getOuter_hexagonal_radius()*2,
																					 	      			globalParameters.getInner_hexagonal_radius()*2};
	 	      	coordinatesAndDimensions = correctCoordinatesAndDimensions(coordinatesAndDimensions, info);
	 	      	 shape = new Ellipse2D.Double(coordinatesAndDimensions[0], coordinatesAndDimensions[1], coordinatesAndDimensions[2], coordinatesAndDimensions[3]);
	 	      }
	   		if(shape!=null){
		   	 	EpisimCellBehavioralModel cbm = cell.getEpisimCellBehavioralModelObject();
		   	/* 	if(((EpisimHexagonBasedModelConnector)mechModel.getEpisimModelConnector()).getIsAtSurfaceBorder()){
		   	 		graphics.setPaint(Color.GREEN);
		   	 	}*/
		   	 	if(cell.getIsTracked()){
		   	 		graphics.setPaint(Color.RED);
		   	 	}
		   	 	else{
		   	 		graphics.setPaint(cell.getCellColoring());
		   	   }
				   if (filled)
				   {	        
				   	graphics.fill(shape);
				   }
				   graphics.setPaint(standardMembraneColor);
				   graphics.setStroke(stroke == null ? getDefaultStroke() : stroke);	      
				   graphics.draw(shape);
				   
				   ModelController.getInstance().getCellBehavioralModelController().getActLoadedModelFile();
	   		}
	   	}	   	
    }
    
    
    
    
    private double[] correctCoordinatesAndDimensions(double[] coordinatesAndDimensions, DrawInfo2D info){
   		EpisimGUIState guiState = SimStateServer.getInstance().getEpisimGUIState();
 	 		SimulationDisplayProperties props = guiState.getSimulationDisplayProperties(new EpisimDrawInfo<DrawInfo2D>(info));
	 		coordinatesAndDimensions[0] *= props.displayScaleX;
 				
 			double heightInMikron = TissueController.getInstance().getTissueBorder().getHeightInMikron();
 			coordinatesAndDimensions[1] = heightInMikron - coordinatesAndDimensions[1];
 			coordinatesAndDimensions[1]*= props.displayScaleY;
 		
 			coordinatesAndDimensions[0] += props.offsetX;
 			coordinatesAndDimensions[1] += props.offsetY;
 			
 			coordinatesAndDimensions[2] *= props.displayScaleX;
 			coordinatesAndDimensions[3] *= props.displayScaleY;
       
 			coordinatesAndDimensions[0] -= (coordinatesAndDimensions[2]/2);
 			coordinatesAndDimensions[1] -= (coordinatesAndDimensions[3]/2); 
   	 
   	 return coordinatesAndDimensions;
    } 
    
    private void drawSpreadingCell(Graphics2D graphics, AbstractCell cell, double x, double y, double width, double height, Double2D fieldLoc, Double2D spreadingLoc){
   	
   	 this.drawInfoRegistry.remove(cell.getID());
  		 this.simStepTimeStampRegistry.remove(cell.getID());    
  		 double rotationInDegrees = 0;
    	
    	
    	
    			double heightDelta= 0;	      	
    	if((fieldLoc.x <spreadingLoc.x && fieldLoc.y >spreadingLoc.y)
    			||(fieldLoc.x > spreadingLoc.x && fieldLoc.y < spreadingLoc.y)){ 
    		rotationInDegrees = 25;
    		//heightDelta = height*0.1*-1d;
    	}
    	
    	if((fieldLoc.x <spreadingLoc.x && fieldLoc.y <spreadingLoc.y)
    			||(fieldLoc.x > spreadingLoc.x && fieldLoc.y > spreadingLoc.y)){ 
    		rotationInDegrees = 155;
    	//	heightDelta = height*0.1;
    	}
   	if((fieldLoc.x == spreadingLoc.x && fieldLoc.y !=spreadingLoc.y)){ 
   		rotationInDegrees = 90;
   	}
    
    	if(rotationInDegrees != 0){
    		AffineTransform transform = new AffineTransform();
    		double rotateX = x + (width/2d);
    		double rotateY = y + (height/2d);
    		
    		
    		transform.setToRotation(Math.toRadians(rotationInDegrees), rotateX, rotateY);
    		//
    		shape = transform.createTransformedShape(new Ellipse2D.Double(x, y, width, height));
    		if(heightDelta != 0){
    			
    			transform.setToTranslation(0, heightDelta);
    			shape = transform.createTransformedShape(shape);
    		}
    	}
    	else shape = new Ellipse2D.Double(x, y, width, height);
    }
    
   private void reset(){
   	actSimStepNo = -1;
   	this.drawInfoRegistry.clear();
   	this.simStepTimeStampRegistry.clear();
   }
    
   public void simulationWasStarted() {

	  reset();
	   
   }
	public void simulationWasPaused() {}
	public void simulationWasStopped() {
	   reset();
   }
}
