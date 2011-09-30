package sim.app.episim.model.visualization;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import episiminterfaces.EpisimCellBehavioralModel;

import sim.SimStateServer;
import sim.SimStateServer.SimState;
import sim.app.episim.AbstractCell;
import sim.app.episim.UniversalCell;
import sim.app.episim.model.biomechanics.centerbased.CenterBasedMechanicalModel;
import sim.app.episim.model.biomechanics.hexagonbased.HexagonBasedMechanicalModel;
import sim.app.episim.util.CellEllipseIntersectionCalculationRegistry;
import sim.app.episim.util.EllipseIntersectionCalculatorAndClipper;
import sim.app.episim.util.GenericBag;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.simple.HexagonalPortrayal2DHack;


public class HexagonalCellPortrayal2D extends HexagonalPortrayal2DHack {
	
	 private static Color standardCellColor = new Color(255,210,210);
	 private static Color standardMembraneColor = new Color(150, 0, 0);
	
	 private static final double DELTA = 3;
	 
	 public HexagonalCellPortrayal2D() { this(standardCellColor,1.0,true); }
    public HexagonalCellPortrayal2D(Paint paint)  { this(paint,1.0,true); }
    public HexagonalCellPortrayal2D(double scale) { this(standardCellColor,scale,true); }
    public HexagonalCellPortrayal2D(boolean filled) { this(standardCellColor,1.0,filled); }
    public HexagonalCellPortrayal2D(Paint paint, double scale)  { this(paint,scale,true); }
    public HexagonalCellPortrayal2D(Paint paint, boolean filled)  { this(paint,1.0,filled); }
    public HexagonalCellPortrayal2D(double scale, boolean filled)  { this(standardCellColor,scale,filled); }
    
    
    
    
    public HexagonalCellPortrayal2D(Paint paint, double scale, boolean filled)
    {
   	 super(paint, scale, filled);
    }
    
    public void draw(Object object, Graphics2D graphics, DrawInfo2D info)
    {
   	 if(object instanceof UniversalCell){   	 
   		 UniversalCell cell = (UniversalCell) object;
   		 EpisimCellBehavioralModel cbModel = cell.getEpisimCellBehavioralModelObject();
	   	   		 
   		 HexagonBasedMechanicalModel mechModel = (HexagonBasedMechanicalModel)cell.getEpisimBioMechanicalModelObject();
   		 if(mechModel.isSpreading()) graphics.setPaint(Color.BLUE.brighter());
   		 else graphics.setPaint(standardCellColor);
	   
   		 
   		 
	       final double width = (info.draw.width+DELTA)*scale;
	       final double height = (info.draw.height+DELTA)*scale;
	       
	       
	       
	       filled = true; 
	       mechModel.getCellEllipse().setXY((int)(info.draw.x-(width/2)), (int)(info.draw.y-(height/2)));
	       
	       shape = new Ellipse2D.Double(info.draw.x-(width/2), info.draw.y-(height/2), width, height); 
	       
	       
	       /*if(getBufferedShape() == null || width != getBufferedWidth() || height != getBufferedHeight())
	       {
	          setBufferedWidth(width);
	          setBufferedHeight(height);
	          getTransform().setToScale(getBufferedWidth(), getBufferedHeight());
	          setBufferedShape(getTransform().createTransformedShape(shape));
	       }
	
	       // we are doing a simple draw, so we ignore the info.clip
	
	       // draw centered on the origin*/
	      // getTransform().setToTranslation(info.draw.x,info.draw.y);
	       if (filled)
	       {
	           //graphics.fill(getTransform().createTransformedShape(getBufferedShape()));
	      	// graphics.fill(getTransform().createTransformedShape(shape));
	      	 graphics.fill(shape);
	       }
	       graphics.setPaint(standardMembraneColor);
	       graphics.setStroke(stroke);
	      // graphics.draw(getTransform().createTransformedShape(getBufferedShape()));
	       graphics.draw(shape);
	       
	     }       
    }
    
    private void doEllipseDrawing(Graphics2D graphics, DrawInfo2D info, UniversalCell universalCell, boolean showNucleus){
 		if(universalCell.getEpisimBioMechanicalModelObject() instanceof HexagonBasedMechanicalModel && universalCell.getActSimState()!= null){
 			HexagonBasedMechanicalModel mechModel = (HexagonBasedMechanicalModel) universalCell.getEpisimBioMechanicalModelObject();
 			calculateClippedCell(universalCell.getActSimState().schedule.getSteps(), mechModel);
 			CellEllipse cellEllipseObject = mechModel.getCellEllipse();
 			
 			if(SimStateServer.getInstance().getSimState() == SimState.PAUSE || SimStateServer.getInstance().getSimState() == SimState.STOP){ 
 				cellEllipseObject.translateCell(new DrawInfo2D(new Rectangle2D.Double(info.draw.x, info.draw.y, info.draw.width, info.draw.height),
 		             		 new Rectangle2D.Double(info.clip.x, info.clip.y, info.clip.width, info.clip.height)));        
 	      }	      	  
 	    	graphics.setPaint(standardCellColor);    
 	    	Area clippedEllipse = cellEllipseObject.getClippedEllipse();
 	    	if(clippedEllipse != null){
 	    		 graphics.fill(clippedEllipse);
 	       	  
 	          graphics.setPaint(standardMembraneColor);
 	          graphics.draw(clippedEllipse);	       	  
 	      }
 	    	
 	    	//must be set at the very end of the paint method
 	       
 	       if(SimStateServer.getInstance().getSimState() == SimState.PLAY){ 
 	      	 cellEllipseObject.setLastDrawInfo2D(new DrawInfo2D(new Rectangle2D.Double(info.draw.x, info.draw.y, info.draw.width, info.draw.height),
 		             		 new Rectangle2D.Double(info.clip.x, info.clip.y, info.clip.width, info.clip.height)), false);
 	       }         
 		}
 	}
    
    private void calculateClippedCell(long simstepNumber, HexagonBasedMechanicalModel mechModel){
     	 
     	CellEllipse cellEllipseCell = mechModel.getCellEllipse();
     	GenericBag<AbstractCell> realNeighbours = mechModel.getRealNeighbours();     	 
     	if(realNeighbours != null && realNeighbours.size() > 0 && cellEllipseCell.getLastDrawInfo2D()!= null){
     		for(AbstractCell neighbouringCell : realNeighbours){
     			if(neighbouringCell.getEpisimBioMechanicalModelObject() instanceof CenterBasedMechanicalModel){
     				CenterBasedMechanicalModel biomechModelNeighbour = (CenterBasedMechanicalModel) neighbouringCell.getEpisimBioMechanicalModelObject();
 	 	   		 if(!CellEllipseIntersectionCalculationRegistry.getInstance().isAreadyCalculated(cellEllipseCell.getId(), biomechModelNeighbour.getCellEllipseObject().getId(), simstepNumber)){
 	 	   			 CellEllipseIntersectionCalculationRegistry.getInstance().addCellEllipseIntersectionCalculation(cellEllipseCell.getId(),biomechModelNeighbour.getCellEllipseObject().getId());
 	 	   			 EllipseIntersectionCalculatorAndClipper.getClippedEllipsesAndXYPoints(cellEllipseCell, biomechModelNeighbour.getCellEllipseObject());
 	 	   		 }
  	   		 }
  	   	 }
     	 }    	
    }
}
