package sim.app.episim.model.visualization;
import sim.SimStateServer;
import sim.SimStateServer.EpisimSimulationState;
import sim.app.episim.AbstractCell;
import sim.app.episim.CellInspector;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.UniversalCell;
import sim.app.episim.gui.EpisimGUIState;
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
                AbstractMechanicalModel mechModel = (AbstractMechanicalModel)universalCell.getEpisimBioMechanicalModelObject();
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
			                
			                Color fillColor = getFillColor(universalCell);
			                Shape cellPolygon;
			                Shape nucleusPolygon;               
			                cellPolygon = universalCell.getEpisimBioMechanicalModelObject().getPolygonCell(info);
			                if(mechModel.getX() != 0 || mechModel.getY() != 0){
				                graphics.setPaint(fillColor);
				                graphics.fill(cellPolygon);				                
				                if(drawFrame)
				                {
				                  graphics.setColor(getContourColor(universalCell));
				                  graphics.draw(cellPolygon);
				                }
			                
					                //TODO: Nucleus ein- und ausschalten
					              if(showNucleus)
					              {
					                  java.awt.Color nucleusColor = new Color(140,140,240); //(Red, Green, Blue); 
					                  nucleusPolygon= universalCell.getEpisimBioMechanicalModelObject().getPolygonNucleus(info);
					                  if(nucleusPolygon != null){
						                  graphics.setPaint(nucleusColor);  
						                  graphics.fill(nucleusPolygon);
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
		    AbstractMechanicalModel mechModel = (AbstractMechanicalModel)universalCell.getEpisimBioMechanicalModelObject();
			((CenterBasedMechanicalModel) universalCell.getEpisimBioMechanicalModelObject()).calculateClippedCell(SimStateServer.getInstance().getSimStepNumber());
			CellEllipse cellEllipseObject = ((CenterBasedMechanicalModel) universalCell.getEpisimBioMechanicalModelObject()).getCellEllipseObject();
			
			if(SimStateServer.getInstance().getEpisimSimulationState() == EpisimSimulationState.PAUSE || SimStateServer.getInstance().getEpisimSimulationState() == EpisimSimulationState.STOP){ 
				cellEllipseObject.translateCell(new DrawInfo2D(info.gui, info.fieldPortrayal, new Rectangle2D.Double(info.draw.x, info.draw.y, info.draw.width, info.draw.height),
		             		 new Rectangle2D.Double(info.clip.x, info.clip.y, info.clip.width, info.clip.height)));        
	      }	      	  
	    	graphics.setPaint(getFillColor(universalCell));
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
	      	 cellEllipseObject.setLastDrawInfo2D(new DrawInfo2D(info.gui, info.fieldPortrayal, new Rectangle2D.Double(info.draw.x, info.draw.y, info.draw.width, info.draw.height),
		             		 new Rectangle2D.Double(info.clip.x, info.clip.y, info.clip.width, info.clip.height)), false);
	       }         
		}
	}
		

   
   private Color getFillColor(UniversalCell kcyte){
   	int keratinoType=kcyte.getEpisimCellBehavioralModelObject().getDiffLevel().ordinal();                                
      int coloringType=MiscalleneousGlobalParameters.instance().getTypeColor();
   	//
      // set colors
      //
                    
      int calculatedColorValue=0;  
     
      int red=255;         
      int green=0;
      int blue=0;
            
      if ((coloringType==1) || (coloringType==2))  // Cell type coloring
      {              
        	   if(keratinoType == EpisimDifferentiationLevel.STEMCELL){red=0x46; green=0x72; blue=0xBE;} 
        	   else if(keratinoType == EpisimDifferentiationLevel.TACELL){red=148; green=167; blue=214;}                             
        	   else if(keratinoType == EpisimDifferentiationLevel.EARLYSPICELL){red=0xE1; green=0x6B; blue=0xF6;}
        	   else if(keratinoType == EpisimDifferentiationLevel.LATESPICELL){red=0xC1; green=0x4B; blue=0xE6;}
        	   else if(keratinoType == EpisimDifferentiationLevel.GRANUCELL){red=204; green=0; blue=102;}
        	  
              
            if((kcyte.getIsOuterCell()) && (coloringType==2)){red=0xF3; green=0xBE; blue=0x4E;}        
            boolean isMembraneCell = false;
            if(kcyte.getEpisimBioMechanicalModelObject() instanceof CenterBasedMechanicalModel) isMembraneCell=((CenterBasedMechanicalModel)kcyte.getEpisimBioMechanicalModelObject()).isMembraneCell();
            if(kcyte.getEpisimBioMechanicalModelObject() instanceof VertexBasedMechanicalModel) isMembraneCell=((VertexBasedMechanicalModel)kcyte.getEpisimBioMechanicalModelObject()).isMembraneCell();
            if(isMembraneCell && (coloringType==2)){red=0xF3; green=0xFF; blue=0x4E;}                        
       }
       if (coloringType==3) // Age coloring
       {              
      	 Method m=null;
      	 double maxAge =0;
          try{
	          m = kcyte.getEpisimCellBehavioralModelObject().getClass().getMethod("_getMaxAge", new Class<?>[0]);
	          maxAge= (Double) m.invoke(kcyte.getEpisimCellBehavioralModelObject(), new Object[0]);
          }
          catch (Exception e){
	          ExceptionDisplayer.getInstance().displayException(e);
          }
          
      	 calculatedColorValue= (int) (250-250*kcyte.getEpisimCellBehavioralModelObject().getAge()/maxAge);
          red=255;
          green=calculatedColorValue;                        
          blue=calculatedColorValue;
          if(keratinoType== EpisimDifferentiationLevel.STEMCELL){ red=148; green=167; blue=214; } // stem cells do not age
       }
      
       if(coloringType==4){ //Colors are calculated in the cellbehavioral model
         red=kcyte.getEpisimCellBehavioralModelObject().getColorR();
         green=kcyte.getEpisimCellBehavioralModelObject().getColorG();
         blue=kcyte.getEpisimCellBehavioralModelObject().getColorB();
       }
        
      // Limit the colors to 255
      green=(green>255)?255:((green<0)?0:green);
      red=(red>255)?255:((red<0)?0:red);
      blue=(blue>255)?255:((blue<0)?0:blue);
      
      if(kcyte.getIsTracked()) return Color.RED;
      return new Color(red, green, blue);
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
       Shape pol = ((UniversalCell)object).getEpisimBioMechanicalModelObject().getPolygonCell();      	 
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