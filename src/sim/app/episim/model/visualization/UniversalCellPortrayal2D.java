package sim.app.episim.model.visualization;
import sim.SimStateServer;
import sim.SimStateServer.EpisimSimulationState;

import sim.app.episim.CellInspector;

import sim.app.episim.UniversalCell;
import sim.app.episim.gui.EpisimGUIState;
import sim.app.episim.gui.EpisimGUIState.SimulationDisplayProperties;
import sim.app.episim.model.biomechanics.AbstractMechanical2DModel;

import sim.app.episim.model.biomechanics.centerbased.adhesion.old.AdhesiveCenterBasedMechanicalModel;
import sim.app.episim.model.biomechanics.centerbased.adhesion.old.AdhesiveCenterBasedMechanicalModelGP;

import sim.app.episim.model.controller.CellBehavioralModelController;
import sim.app.episim.model.controller.ModelController;

import sim.app.episim.util.Scale;
import sim.display.GUIState;
import sim.portrayal.*;

import java.awt.*;
import java.awt.geom.*;
import episiminterfaces.EpisimCellShape;
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
    
    private Color nucleusColor = new Color(78,191,250); //(Red, Green, Blue); 
   
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
                final UniversalCell universalCell = ((UniversalCell)object);
                AbstractMechanical2DModel mechModel = (AbstractMechanical2DModel) universalCell.getEpisimBioMechanicalModelObject();
         		 mechModel.setLastDrawInfo2D(new DrawInfo2D(info.gui, info.fieldPortrayal, new Rectangle2D.Double(info.draw.x, info.draw.y, info.draw.width, info.draw.height),
                		 new Rectangle2D.Double(info.clip.x, info.clip.y, info.clip.width, info.clip.height))); 
                if(ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters() instanceof sim.app.episim.model.biomechanics.centerbased.CenterBasedMechanicalModelGP){
               	 drawCellEllipses = ((sim.app.episim.model.biomechanics.centerbased.CenterBasedMechanicalModelGP)ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters()).isDrawCellsAsEllipses();
               	 centerBasedModel = true;
                }
                else if(ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters() instanceof AdhesiveCenterBasedMechanicalModelGP){
               	 drawCellEllipses = ((AdhesiveCenterBasedMechanicalModelGP)ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters()).isDrawCellsAsEllipses();
               	 centerBasedModel = true;
                }
                else if(ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters() instanceof sim.app.episim.model.biomechanics.centerbased.newversion.CenterBasedMechanicalModelGP){
               	 drawCellEllipses = ((sim.app.episim.model.biomechanics.centerbased.newversion.CenterBasedMechanicalModelGP)ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters()).isDrawCellsAsEllipses();
               	 centerBasedModel = true;
                }
                drawFrame=true;
               
                
                EpisimCellShape<Shape> shape = mechModel.getPolygonNucleus(new EpisimDrawInfo<DrawInfo2D>(info));
                showNucleus = (shape != null && shape.getCellShape() != null);
               if(!drawCellEllipses){           
		                                                          
			                
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
			                
					                
					              
					             java.awt.Color nucleusColor = new Color(140,140,240); //(Red, Green, Blue); 
					            
					             if(showNucleus){					                  
							          graphics.setPaint(nucleusColor);  
							          graphics.fill(shape.getCellShape());						                  
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
		if(universalCell.getEpisimBioMechanicalModelObject() instanceof sim.app.episim.model.biomechanics.centerbased.CenterBasedMechanicalModel
				|| universalCell.getEpisimBioMechanicalModelObject() instanceof sim.app.episim.model.biomechanics.centerbased.newversion.CenterBasedMechanicalModel
				|| universalCell.getEpisimBioMechanicalModelObject() instanceof AdhesiveCenterBasedMechanicalModel){
		    AbstractMechanical2DModel mechModel = (AbstractMechanical2DModel)universalCell.getEpisimBioMechanicalModelObject();
		    CellEllipse cellEllipseObject = null;
		    if(universalCell.getEpisimBioMechanicalModelObject() instanceof sim.app.episim.model.biomechanics.centerbased.CenterBasedMechanicalModel){
				((sim.app.episim.model.biomechanics.centerbased.CenterBasedMechanicalModel) universalCell.getEpisimBioMechanicalModelObject()).calculateClippedCell(SimStateServer.getInstance().getSimStepNumber());
					cellEllipseObject = ((sim.app.episim.model.biomechanics.centerbased.CenterBasedMechanicalModel) universalCell.getEpisimBioMechanicalModelObject()).getCellEllipseObject();
		    }
		    if(universalCell.getEpisimBioMechanicalModelObject() instanceof sim.app.episim.model.biomechanics.centerbased.newversion.CenterBasedMechanicalModel){
					((sim.app.episim.model.biomechanics.centerbased.newversion.CenterBasedMechanicalModel) universalCell.getEpisimBioMechanicalModelObject()).calculateClippedCell(SimStateServer.getInstance().getSimStepNumber());
					cellEllipseObject = ((sim.app.episim.model.biomechanics.centerbased.newversion.CenterBasedMechanicalModel) universalCell.getEpisimBioMechanicalModelObject()).getCellEllipseObject();
			 }
		    if(universalCell.getEpisimBioMechanicalModelObject() instanceof AdhesiveCenterBasedMechanicalModel){
					((AdhesiveCenterBasedMechanicalModel) universalCell.getEpisimBioMechanicalModelObject()).calculateClippedCell(SimStateServer.getInstance().getSimStepNumber());
					cellEllipseObject = ((AdhesiveCenterBasedMechanicalModel) universalCell.getEpisimBioMechanicalModelObject()).getCellEllipseObject();
			 }
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
		    		
		         graphics.setPaint(nucleusColor);
		       	final double NUCLEUSRAD = 0.75;
		       	double width = cellEllipseObject.getMajorAxis()*0.3;
		       	double height = cellEllipseObject.getMinorAxis()*0.3;		
		         graphics.fill(new Ellipse2D.Double(cellEllipseObject.getX()-(width/2), cellEllipseObject.getY()-(height/2), width, height));
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
      myFrameColor=new Color(200, 165, 200);   	
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