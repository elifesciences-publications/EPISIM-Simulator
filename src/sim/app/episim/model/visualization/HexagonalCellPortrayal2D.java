package sim.app.episim.model.visualization;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.HashSet;

import episiminterfaces.EpisimCellBehavioralModel;



import sim.SimStateServer;
import sim.SimStateServer.SimState;
import sim.app.episim.AbstractCell;
import sim.app.episim.SimulationStateChangeListener;
import sim.app.episim.UniversalCell;
import sim.app.episim.model.biomechanics.hexagonbased.HexagonBasedMechanicalModel;
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
   		 
   		 long simStepNo = -1;
   		 if(cell.getActSimState() != null &&  cell.getActSimState().schedule != null) actSimStepNo = cell.getActSimState().schedule.getSteps();
   		 
   		  		 
   		 
   		 filled = true;
   		 double width = (info.draw.width+DELTA)*scale;
	       double height = (info.draw.height+DELTA)*scale;
	       double x = info.draw.x;
		    double y = info.draw.y;
	       
   		 HexagonBasedMechanicalModel mechModel = (HexagonBasedMechanicalModel)cell.getEpisimBioMechanicalModelObject();
   		 mechModel.setLastDrawInfo2D(new DrawInfo2D(new Rectangle2D.Double(info.draw.x, info.draw.y, width, height),
          		 new Rectangle2D.Double(info.clip.x, info.clip.y, info.clip.width, info.clip.height)));
   		 
   		 if(mechModel.isSpreading()){
	 	      	if(this.drawInfoRegistry.containsKey(cell.getID()) 
	 	      			&& this.simStepTimeStampRegistry.get(cell.getID()) == actSimStepNo){
	 	      		DrawInfo2D secondInfo = this.drawInfoRegistry.get(cell.getID());
	 	      		
	 	      		     		
	 	      		
	 	      		this.drawInfoRegistry.remove(cell.getID());
	 	      		this.simStepTimeStampRegistry.remove(cell.getID());
	 	      		
	 	      		x = info.draw.x < secondInfo.draw.x ? (info.draw.x -(((info.draw.width+DELTA)*scale)/2)): (secondInfo.draw.x -(((secondInfo.draw.width+DELTA)*scale)/2));
			      	y = info.draw.y < secondInfo.draw.y ? (info.draw.y -(((info.draw.height+DELTA)*scale)/2)): (secondInfo.draw.y -(((secondInfo.draw.height+DELTA)*scale)/2));
			      		      	
			      	height = (Math.abs(info.draw.y-secondInfo.draw.y)+info.draw.height+DELTA)*scale;
			      	width = (Math.abs(info.draw.x-secondInfo.draw.x)+info.draw.width+DELTA)*scale;
			      
			      	double rotationInDegrees = 0;
			      	
			      	
			      	boolean swap = false;
			      	MutableInt2D location = (MutableInt2D)info.location;
			      	if(mechModel.getSpreadingLocation().x == location.x && mechModel.getSpreadingLocation().y == location.y){
			      		DrawInfo2D temp = secondInfo;
			      		secondInfo = info;
			      		info = temp;
			      		swap = true;
			      	} 	 			      	
			      	if((info.draw.x <secondInfo.draw.x && info.draw.y >secondInfo.draw.y)
			      			||(info.draw.x > secondInfo.draw.x && info.draw.y < secondInfo.draw.y)) rotationInDegrees = 155;
			      	
			      	if((info.draw.x <secondInfo.draw.x && info.draw.y <secondInfo.draw.y)
			      			||(info.draw.x > secondInfo.draw.x && info.draw.y > secondInfo.draw.y)) rotationInDegrees = 25;
			      	
			      	if(swap){
			      		DrawInfo2D temp = secondInfo;
			      		secondInfo = info;
			      		info = temp;
			      	} 
			      	
			      	double ellipseHeight = ((info.draw.height+DELTA)*scale)*0.85;
			      	if(rotationInDegrees != 0){
			      		AffineTransform tansform = new AffineTransform();
			      		double rotateX = x + (width/2);
			      		double rotateY = y + (height/2);
			      		
			      		y += (height/2);
				      	y -= (ellipseHeight/2);
			      		tansform.setToRotation(Math.toRadians(rotationInDegrees), rotateX, rotateY);
			      		//
			      		shape = tansform.createTransformedShape(new Ellipse2D.Double(x, y, ((info.draw.height+DELTA)*scale)*2, ellipseHeight));
			      	}
			      	else shape = new Ellipse2D.Double(x+(Math.abs(ellipseHeight-width)/2), y, ellipseHeight, height);
	 	      	}
	 	      	else{
	 	      		
	 	      		if((Math.abs(mechModel.getX()-mechModel.getSpreadingLocation().x) + Math.abs(mechModel.getY()-mechModel.getSpreadingLocation().y))<=2){   		
	 	  		      		this.drawInfoRegistry.put(cell.getID(), new DrawInfo2D(new Rectangle2D.Double(info.draw.x, info.draw.y, info.draw.width, info.draw.height),		      
	 	  			          		 new Rectangle2D.Double(info.clip.x, info.clip.y, info.clip.width, info.clip.height)));
	 	  		      	   this.drawInfoRegistry.get(cell.getID()).location = info.location;
	 	  		      		this.simStepTimeStampRegistry.put(cell.getID(), actSimStepNo);
	 	  		      		shape = null;
	 	      		}
	 	      		else{
	 	      			shape = new Ellipse2D.Double(info.draw.x-(width/2), info.draw.y-(height/2), width, height);
	 	      			MutableInt2D location = (MutableInt2D)info.location;
	 	      			boolean isSpreadingLocation = (location.x == mechModel.getSpreadingLocation().x && location.y == mechModel.getSpreadingLocation().y);
	 	      			
	 	      			
	 	      			
	 	      			EpisimCellBehavioralModel cbm = cell.getEpisimCellBehavioralModelObject();
	 		   		 	graphics.setPaint(new Color(cbm.getColorR(), cbm.getColorG(), cbm.getColorB()));
	 					   if (filled)
	 					   {	        
	 					   	graphics.fill(shape);
	 					   }
	 					   graphics.setPaint(standardMembraneColor);
	 					   graphics.setStroke(stroke);	      
	 					   graphics.draw(shape);
	 	      			
	 	      		}
	 	      	}
 	      	}
	 	      else{
		   	 /*CellEllipse cellEllipse = mechModel.getCellEllipse();  			 
	   			cellEllipse.setLastDrawInfo2D(new DrawInfo2D(new Rectangle2D.Double(info.draw.x, info.draw.y, width, height),
	             		 new Rectangle2D.Double(info.clip.x, info.clip.y, info.clip.width, info.clip.height)), true);*/
	   			
	   			 
			       shape = new Ellipse2D.Double(info.draw.x-(width/2), info.draw.y-(height/2), width, height);	
	 	      }
   		 if(shape!=null){
	   		 	EpisimCellBehavioralModel cbm = cell.getEpisimCellBehavioralModelObject();
	   		 	graphics.setPaint(new Color(cbm.getColorR(), cbm.getColorG(), cbm.getColorB()));
				   if (filled)
				   {	        
				   	graphics.fill(shape);
				   }
				   graphics.setPaint(standardMembraneColor);
				   graphics.setStroke(stroke);	      
				   graphics.draw(shape);
   		 }
	   	}	   	
    }
    
    private boolean isSpreadingLocation(DrawInfo2D info, HexagonBasedMechanicalModel mechModel){
   	 if(mechModel.isSpreading()){
   		 MutableInt2D loc = (MutableInt2D)info.location;
   		 Int2D spreadingLoc = mechModel.getSpreadingLocation();
   		 return loc.x == spreadingLoc.x && loc.y == spreadingLoc.y;
   	 }
   	 return false;
    }
    
    private void doEllipseDrawing(Graphics2D graphics, DrawInfo2D info, UniversalCell universalCell){
 		if(universalCell.getEpisimBioMechanicalModelObject() instanceof HexagonBasedMechanicalModel && universalCell.getActSimState()!= null){
 			
 			HexagonBasedMechanicalModel mechModel = (HexagonBasedMechanicalModel) universalCell.getEpisimBioMechanicalModelObject();
 			
 			double width = (info.draw.width+DELTA)*scale;
	      double height = (info.draw.height+DELTA)*scale;  
	      
	      double x = info.draw.x;
	      double y = info.draw.y;
	      boolean toroidalSpreading = false;
	      if(mechModel.isSpreading()){
	      	if(this.drawInfoRegistry.containsKey(universalCell.getID()) 
	      			&& this.simStepTimeStampRegistry.get(universalCell.getID()) == actSimStepNo){
	      		DrawInfo2D secondInfo = this.drawInfoRegistry.get(universalCell.getID());
	      		this.drawInfoRegistry.remove(universalCell.getID());
	      		this.simStepTimeStampRegistry.remove(universalCell.getID());
	      	//	x = ((info.draw.x +(width/2))+(secondInfo.draw.x +(secondInfo.draw.width/2)))/2;
	      	//	y = ((info.draw.y +(height/2))+(secondInfo.draw.y +(secondInfo.draw.height/2)))/2;
	      		x = info.draw.x <secondInfo.draw.x ? info.draw.x : secondInfo.draw.x;
		      	y = info.draw.x <secondInfo.draw.y ? info.draw.y : secondInfo.draw.y;
		      	
		      	double rotationInDegrees = 0;
		      	if((info.draw.x <secondInfo.draw.x && info.draw.y <secondInfo.draw.y)
		      			||(info.draw.x > secondInfo.draw.x && info.draw.y > secondInfo.draw.y)) rotationInDegrees = 45;
		      	if((info.draw.x <secondInfo.draw.x && info.draw.y >secondInfo.draw.y)
		      			||(info.draw.x > secondInfo.draw.x && info.draw.y < secondInfo.draw.y)) rotationInDegrees = 135;
		      	
	      		//width += secondInfo.draw.width;
	      		mechModel.getCellEllipse().setMajorAxis(2);
	      		mechModel.getCellEllipse().setXY((int)((mechModel.getX()+mechModel.getSpreadingLocation().x)/2), 
	      													(int)((mechModel.getY()+mechModel.getSpreadingLocation().y)/2));
	      		mechModel.getCellEllipse().rotateCellEllipseInDegrees(rotationInDegrees);
	      		toroidalSpreading = false;
	      	}
	      	else{
	      		//Otherwise toroidal Side Change ==> draw two ellipses
	      //		if((Math.abs(mechModel.getX()-mechModel.getSpreadingLocation().x) + Math.abs(mechModel.getY()-mechModel.getSpreadingLocation().y))<=2){   		
		      /*		this.drawInfoRegistry.put(universalCell.getID(), new DrawInfo2D(new Rectangle2D.Double(x, y, width, height),		      
			          		 new Rectangle2D.Double(info.clip.x, info.clip.y, info.clip.width, info.clip.height)));
		      		this.simStepTimeStampRegistry.put(universalCell.getID(), actSimStepNo);*/
		      		toroidalSpreading = true;
		      		//return;
		      		
	      /*		}
	      		else{
	      			toroidalSpreading = true;
	      		}*/
	      		
	      	}
	      }
 			
	      
	      if(SimStateServer.getInstance().getSimState() == SimState.PLAY || SimStateServer.getInstance().getSimState() == SimState.STEPWISE){
	      	boolean translate = (mechModel.getCellEllipse().getLastDrawInfo2D().clip.x != info.clip.x
	      			           || mechModel.getCellEllipse().getLastDrawInfo2D().clip.y != info.clip.y
	      			           || mechModel.getCellEllipse().getLastDrawInfo2D().clip.width != info.clip.width
	      			           || mechModel.getCellEllipse().getLastDrawInfo2D().clip.height != info.clip.height);
	      	
		      if(info != null){
		      	if(translate || toroidalSpreading){
		      		mechModel.getCellEllipse().translateCell(new DrawInfo2D(new Rectangle2D.Double(x, y, width, height),
		             		 new Rectangle2D.Double(info.clip.x, info.clip.y, info.clip.width, info.clip.height))); 
		      	}
		      	else{
		      		mechModel.getCellEllipse().setLastDrawInfo2D(new DrawInfo2D(new Rectangle2D.Double(x, y, width, height),		      
	          		 new Rectangle2D.Double(info.clip.x, info.clip.y, info.clip.width, info.clip.height)), false);
		      	}
		      }
	      }
	     /* else if(SimStateServer.getInstance().getSimState() == SimState.PAUSE || SimStateServer.getInstance().getSimState() == SimState.STOP){ 
 				mechModel.getCellEllipse().setLastDrawInfo2D(new DrawInfo2D(new Rectangle2D.Double(info.draw.x, info.draw.y, width, height),
	          		 new Rectangle2D.Double(info.clip.x, info.clip.y, info.clip.width, info.clip.height)), true);        
 	      }*/
	      calculateClippedCell(universalCell.getActSimState().schedule.getSteps(), mechModel);
 			
	      
 			if(SimStateServer.getInstance().getSimState() == SimState.PAUSE || SimStateServer.getInstance().getSimState() == SimState.STOP){ 
 				 mechModel.getCellEllipse().translateCell(new DrawInfo2D(new Rectangle2D.Double(x, y, width, height),
 		             		 new Rectangle2D.Double(info.clip.x, info.clip.y, info.clip.width, info.clip.height)));        
 	      }	    	  
 	    	
 			EpisimCellBehavioralModel cbm = universalCell.getEpisimCellBehavioralModelObject();
 			graphics.setPaint(new Color(cbm.getColorR(), cbm.getColorG(), cbm.getColorB()));    
 	    	Area clippedEllipse = mechModel.getCellEllipse().getClippedEllipse();
 	    	if(clippedEllipse != null){
 	    		 graphics.fill(clippedEllipse);
 	       	  
 	          graphics.setPaint(standardMembraneColor);
 	          graphics.draw(clippedEllipse);	       	  
 	      }   
 		}
 	}
    
    private void calculateClippedCell(long simstepNumber, HexagonBasedMechanicalModel mechModel){
     	 
     	CellEllipse cellEllipseCell = mechModel.getCellEllipse();
     	GenericBag<AbstractCell> realNeighbours = mechModel.getRealNeighbours();     	 
     	if(realNeighbours != null && realNeighbours.size() > 0 && cellEllipseCell.getLastDrawInfo2D()!= null){
     		for(AbstractCell neighbouringCell : realNeighbours){
     			
     			HexagonBasedMechanicalModel biomechModelNeighbour = (HexagonBasedMechanicalModel) neighbouringCell.getEpisimBioMechanicalModelObject();
 	 	   		 if(!CellEllipseIntersectionCalculationRegistry.getInstance().isAreadyCalculated(cellEllipseCell.getId(), biomechModelNeighbour.getCellEllipse().getId(), simstepNumber)
 	 	   				 || mechModel.isSpreading()){
 	 	   			 CellEllipseIntersectionCalculationRegistry.getInstance().addCellEllipseIntersectionCalculation(cellEllipseCell.getId(),biomechModelNeighbour.getCellEllipse().getId());
 	 	   			 EllipseIntersectionCalculatorAndClipper.getClippedEllipsesAndXYPoints(cellEllipseCell, biomechModelNeighbour.getCellEllipse());
 	 	   		 }
  	   		 
  	   	 }
     	 }    	
    }
    
    private Double2D calculateSpreadingCellCenter(DrawInfo2D info, HexagonBasedMechanicalModel mechModel){
   	 double x = info.draw.x;
   	 double y = info.draw.y;
   	 
   	 double spreadingX = (x/(mechModel.getX()+1))*(mechModel.getSpreadingLocation().x+1);
   	 double spreadingY = (y/(mechModel.getY()+1))*(mechModel.getSpreadingLocation().y+1);
   	 Point2D.Double loc = cellGridPortrayal.getLocationPosition(mechModel.getSpreadingLocation(), info);
   	 
   	return new Double2D(((spreadingX + x)/2), ((spreadingY + y)/2));    	 
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
