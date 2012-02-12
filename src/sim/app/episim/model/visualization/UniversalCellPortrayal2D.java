package sim.app.episim.model.visualization;
import sim.SimStateServer;
import sim.SimStateServer.EpisimSimulationState;
import sim.app.episim.AbstractCell;
import sim.app.episim.CellInspector;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.UniversalCell;
import sim.app.episim.gui.EpisimGUIState;
import sim.app.episim.gui.EpisimGUIState.SimulationDisplayProperties;
import sim.app.episim.model.biomechanics.AbstractMechanical2DModel;
import sim.app.episim.model.biomechanics.AbstractMechanicalModel;
import sim.app.episim.model.biomechanics.centerbased.CenterBasedMechanicalModel;
import sim.app.episim.model.biomechanics.centerbased.CenterBasedMechanicalModelGP;
import sim.app.episim.model.biomechanics.vertexbased.VertexBasedMechanicalModel;
import sim.app.episim.model.biomechanics.vertexbased.calc.CellPolygonCalculator;
import sim.app.episim.model.biomechanics.vertexbased.geom.CellPolygon;
import sim.app.episim.model.biomechanics.vertexbased.geom.Vertex;
import sim.app.episim.model.controller.BiomechanicalModelController;
import sim.app.episim.model.controller.CellBehavioralModelController;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters;
import sim.app.episim.tissue.TissueController;
import sim.app.episim.util.CellEllipseIntersectionCalculationRegistry;
import sim.app.episim.util.EllipseIntersectionCalculatorAndClipper;
import sim.app.episim.util.Scale;
import sim.display.GUIState;
import sim.portrayal.*;

import java.awt.*;
import java.awt.geom.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import episiminterfaces.EpisimCellShape;
import episiminterfaces.EpisimDifferentiationLevel;
import episiminterfaces.EpisimPortrayal;




public class UniversalCellPortrayal2D extends SimplePortrayal2D implements EpisimPortrayal
{
    
		private final String NAME = "Epidermis";	
	 private Paint paint;  
    private CellBehavioralModelController cBModelController;   
    private boolean drawFrame = true;
    private double implicitScale;
    
    private static long actSimStep;
    
    private final double INITIALWIDTH;
    private final double INITIALHEIGHT;
    
   
    private EpisimGUIState guiState;
    
    public UniversalCellPortrayal2D() {   	 
   	 this(Color.gray, false);   	 
    }    
    public UniversalCellPortrayal2D(Paint paint){   	 
   	 this(paint, true);  	 
    }    
      
    public UniversalCellPortrayal2D(Paint paint, boolean drawFrame)  {   	
   	cBModelController = ModelController.getInstance().getCellBehavioralModelController();
   	guiState = SimStateServer.getInstance().getEpisimGUIState();
   	
   	if(guiState != null){
   		this.implicitScale = guiState.INITIALZOOMFACTOR;
   		
   		this.INITIALWIDTH = guiState.EPIDISPLAYWIDTH + guiState.DISPLAY_BORDER_LEFT+guiState.DISPLAY_BORDER_RIGHT;
   		this.INITIALHEIGHT = guiState.EPIDISPLAYHEIGHT + guiState.DISPLAY_BORDER_BOTTOM+guiState.DISPLAY_BORDER_TOP;
   	}
   	else{
   		this.INITIALHEIGHT=0;
   		this.INITIALWIDTH=0;
   	}
   	this.paint = paint; 
   	this.drawFrame = drawFrame;  
   	
    }    
    
    public String getPortrayalName() {
 	   return NAME;
    }
    
 	public Inspector getInspector(LocationWrapper wrapper, GUIState state) {
		// make the inspector
		return new CellInspector(super.getInspector(wrapper, state), wrapper, state);
	}
    
    // assumes the graphics already has its color set
    public void draw(Object object, Graphics2D graphics, DrawInfo2D info)
    {      
            boolean showNucleus=true;
            boolean drawCellEllipses = false;
            boolean centerBasedModel = false;
            if (object instanceof UniversalCell)
            {                
                final UniversalCell universalCell=((UniversalCell)object);
                AbstractMechanical2DModel mechModel = (AbstractMechanical2DModel)universalCell.getEpisimBioMechanicalModelObject();
         		 mechModel.setLastDrawInfo2D(new DrawInfo2D(info.gui, info.fieldPortrayal, new Rectangle2D.Double(info.draw.x, info.draw.y, info.draw.width, info.draw.height),
                		 new Rectangle2D.Double(info.clip.x, info.clip.y, info.clip.width, info.clip.height))); 
                if(ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters() instanceof CenterBasedMechanicalModelGP){
               	 drawCellEllipses = ((CenterBasedMechanicalModelGP)ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters()).isDrawCellsAsEllipses();
               	 centerBasedModel = true;
                }
                int keratinoType=universalCell.getEpisimCellBehavioralModelObject().getDiffLevel().ordinal();                                
                int colorType=MiscalleneousGlobalParameters.instance().getTypeColor();               
                if(keratinoType == EpisimDifferentiationLevel.STEMCELL
                  	 || keratinoType == EpisimDifferentiationLevel.TACELL
                  	 || keratinoType == EpisimDifferentiationLevel.EARLYSPICELL
                  	 || keratinoType == EpisimDifferentiationLevel.LATESPICELL){ 
                  	 showNucleus=true; 
                  	 drawFrame=true;
                } 
                else if(keratinoType == EpisimDifferentiationLevel.GRANUCELL){ 
                  	 drawFrame=true;                  	 
                  	 showNucleus=false;
                }          
                
               if(!drawCellEllipses){           
		                if(colorType < 5){                                               
			                
			                Color fillColor = universalCell.getCellColoring();
			                Shape cellPolygon;
			                Shape nucleusPolygon;               
			                cellPolygon = mechModel.getPolygonCell(new EpisimDrawInfo<DrawInfo2D>(info)).getCellShape();
			                if(mechModel.getX() != 0 || mechModel.getY() != 0){
				                graphics.setPaint(fillColor);
				                graphics.fill(cellPolygon);				                
				                if(drawFrame)
				                {
				                  graphics.setColor(getContourColor(universalCell));
				                  graphics.draw(cellPolygon);
				                }
			                
					                
					              if(showNucleus)
					              {
					                java.awt.Color nucleusColor = new Color(140,140,240); //(Red, Green, Blue); 
					                 EpisimCellShape<Shape> shape = mechModel.getPolygonNucleus(new EpisimDrawInfo<DrawInfo2D>(info));
					                 if(shape != null && shape.getCellShape() != null){					                  
							                  graphics.setPaint(nucleusColor);  
							                  graphics.fill(shape.getCellShape());						                  
					                 }
					              }
			                }
		                
		              } 
               }
               else{
               	doCenterBasedModelEllipseDrawing(graphics, info, universalCell, showNucleus);
               }
              
              }
              else { 
            	  graphics.setPaint(paint);          
              }       
   }
   
   private double getScaleFactorOfTheDisplay(){
 		return Scale.displayScale;
 	}

	private void doCenterBasedModelEllipseDrawing(Graphics2D graphics, DrawInfo2D info, UniversalCell universalCell, boolean showNucleus){
		if(universalCell.getEpisimBioMechanicalModelObject() instanceof CenterBasedMechanicalModel){
		    AbstractMechanical2DModel mechModel = (AbstractMechanical2DModel)universalCell.getEpisimBioMechanicalModelObject();
			((CenterBasedMechanicalModel) universalCell.getEpisimBioMechanicalModelObject()).calculateClippedCell(SimStateServer.getInstance().getSimStepNumber());
			CellEllipse cellEllipseObject = ((CenterBasedMechanicalModel) universalCell.getEpisimBioMechanicalModelObject()).getCellEllipseObject();
			
			if(SimStateServer.getInstance().getEpisimSimulationState() == EpisimSimulationState.PAUSE || SimStateServer.getInstance().getEpisimSimulationState() == EpisimSimulationState.STOP){ 
				SimulationDisplayProperties props = guiState.getSimulationDisplayProperties(new EpisimDrawInfo<DrawInfo2D>(info));
				
				/*cellEllipseObject.translateCell(new DrawInfo2D(info.gui, info.fieldPortrayal, new Rectangle2D.Double(info.draw.x, (props.offsetY+props.displayScaleY*TissueController.getInstance().getTissueBorder().getHeightInPixels())-info.draw.y, props.displayScaleX, props.displayScaleY),
		             		 new Rectangle2D.Double(info.clip.x, info.clip.y, info.clip.width, info.clip.height)));        */
				 cellEllipseObject.setLastSimulationDisplayProps(props, false);
	      }	      	  
	    	graphics.setPaint(universalCell.getCellColoring());
	    	if(mechModel.getX() != 0 || mechModel.getY() != 0){
		    	Area clippedEllipse = cellEllipseObject.getClippedEllipse();
		    	if(clippedEllipse != null){
		    		  graphics.fill(clippedEllipse);
		       	  if(drawFrame){
		          	  graphics.setPaint(getContourColor(universalCell));
		          	  graphics.draw(clippedEllipse);	       	  
		       	  }
		    	}
		    	if(showNucleus){
		    		Color nucleusColor = new Color(140,140,240); //(Red, Green, Blue); 
		         graphics.setPaint(nucleusColor);
		       	final double NUCLEUSRAD = 0.75;
		         graphics.fill(new Ellipse2D.Double(cellEllipseObject.getX()-NUCLEUSRAD*info.draw.width, cellEllipseObject.getY()-NUCLEUSRAD*info.draw.height,2*NUCLEUSRAD*info.draw.width, 2*NUCLEUSRAD*info.draw.height));
		    	}
	    	}
	    	//must be set at the very end of the paint method
	       
	       if(SimStateServer.getInstance().getEpisimSimulationState() == EpisimSimulationState.PLAY || SimStateServer.getInstance().getEpisimSimulationState() == EpisimSimulationState.STEPWISE){ 
	      	 
	      	 SimulationDisplayProperties props = guiState.getSimulationDisplayProperties(new EpisimDrawInfo<DrawInfo2D>(info));
	      	 cellEllipseObject.setLastSimulationDisplayProps(props, false);
	       }         
		}
	}
		

   
  
   
   private Color getContourColor(UniversalCell kcyte){
   	Color myFrameColor = Color.white; //new Color(Red, Green, Blue);   	                               
      int coloringType=MiscalleneousGlobalParameters.instance().getTypeColor();
   	myFrameColor=new Color(200, 165, 200);                
   	if (coloringType==3 || coloringType==4 || coloringType==5 || coloringType==6 || coloringType==7) // Age coloring
   	{
   		myFrameColor=Color.black;
   	}
   	return myFrameColor;      
   }
   
   private void drawPoint(Graphics2D g, int x, int y, int size, Color c){
		if(x> 0 || y > 0){
			if(size % 2 != 0) size -= 1;
			Color oldColor = g.getColor();
			g.setColor(c);
			g.fillRect(x-(size/2), y-(size/2), size+1, size+1);
			g.setColor(oldColor);
		}
	} 

   public boolean hitObject(Object object, DrawInfo2D range)
   {       
      if (object instanceof UniversalCell){
      	 AbstractMechanical2DModel mechModel = (AbstractMechanical2DModel)((UniversalCell)object).getEpisimBioMechanicalModelObject();
      	 Shape pol = mechModel.getPolygonCell().getCellShape();      	 
       return ( pol.intersects( range.clip.x, range.clip.y, range.clip.width, range.clip.height));
      }
	   return false; 
   }
   
   public Rectangle2D.Double getViewPortRectangle() {
 		EpisimGUIState guiState = SimStateServer.getInstance().getEpisimGUIState();	   
 	   if(guiState != null)return new Rectangle2D.Double(guiState.DISPLAY_BORDER_LEFT,guiState.DISPLAY_BORDER_TOP,guiState.EPIDISPLAYWIDTH, guiState.EPIDISPLAYHEIGHT);
 	   else return new Rectangle2D.Double(0,0,0, 0);
   }
}