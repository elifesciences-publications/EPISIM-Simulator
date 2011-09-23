package sim.app.episim.model.visualization;
import sim.SimStateServer;
import sim.SimStateServer.SimState;
import sim.app.episim.AbstractCell;
import sim.app.episim.UniversalCell;
import sim.app.episim.model.biomechanics.centerbased.CenterBasedMechanicalModel;
import sim.app.episim.model.biomechanics.centerbased.CenterBasedMechanicalModelGlobalParameters;
import sim.app.episim.model.biomechanics.vertexbased.CellPolygonCalculator;
import sim.app.episim.model.biomechanics.vertexbased.CellPolygon;
import sim.app.episim.model.biomechanics.vertexbased.Vertex;
import sim.app.episim.model.controller.BiomechanicalModelController;
import sim.app.episim.model.controller.CellBehavioralModelController;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters;
import sim.app.episim.util.CellEllipseIntersectionCalculationRegistry;
import sim.app.episim.util.EllipseIntersectionCalculatorAndClipper;
import sim.app.episim.util.Scale;
import sim.portrayal.*;
import java.awt.*;
import java.awt.geom.*;

import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import episiminterfaces.EpisimDifferentiationLevel;




public class UniversalCellPortrayal2D extends SimplePortrayal2D
{
    private Paint paint;  
    private CellBehavioralModelController cBModelController;   
    private boolean drawFrame = true;
    private double implicitScale;
    
    private static long actSimStep;
    
    private final double INITIALWIDTH;
    private final double INITIALHEIGHT;
    
    private int border = 0;
    
    public UniversalCellPortrayal2D(double implicitScale) {   	 
   	 this(Color.gray, false, implicitScale, 0, 0, 0);   	 
    }    
    public UniversalCellPortrayal2D(Paint paint, double implicitScale, double width, double height, int border){   	 
   	 this(paint, true, implicitScale, width, height, border);  	 
    }    
      
    public UniversalCellPortrayal2D(Paint paint, boolean drawFrame, double implicitScale, double width, double height, int border)  {   	
   	cBModelController = ModelController.getInstance().getCellBehavioralModelController();
   	this.paint = paint; 
   	this.drawFrame = drawFrame;  
   	this.implicitScale = implicitScale;
   	 this.INITIALWIDTH = ((int)width);
	  	 this.INITIALHEIGHT = ((int)height);
	  	 this.border = border;
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
                
                if(ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters() instanceof CenterBasedMechanicalModelGlobalParameters){
               	 drawCellEllipses = ((CenterBasedMechanicalModelGlobalParameters)ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters()).isDrawCellsAsEllipses();
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
		                if(colorType < 9){                                               
			                
			                Color fillColor = getFillColor(universalCell);
			                Polygon cellPolygon;
			                Polygon nucleusPolygon;               
			                cellPolygon = universalCell.getEpisimBioMechanicalModelObject().getPolygonCell(info);
			                if(!centerBasedModel){
			               	 AffineTransform transform = new AffineTransform();
			               	 double scaleFactor = getScaleFactorOfTheDisplay()*implicitScale;
			               	 transform.scale(scaleFactor, scaleFactor);
			               	 GeneralPath path = new GeneralPath(cellPolygon);
			               	 Rectangle2D rectBefore = path.getBounds2D();
			               	 path.transform(transform);
			               	 
			               	 Rectangle2D rectAfter = path.getBounds2D();
			               	 double xShift = (rectBefore.getCenterX() -rectAfter.getCenterX());
			               	 double yShift = (rectBefore.getCenterY()-rectAfter.getCenterY());
			               	 
			               	 transform = new AffineTransform();
			               	 
			               	 if((INITIALHEIGHT*getScaleFactorOfTheDisplay()) > (info.clip.height+1)){
			               		 yShift -= info.clip.y;
			               	 }
			               	 
			               	 if((INITIALWIDTH*getScaleFactorOfTheDisplay()) > (info.clip.width+1)){
			               		 xShift -= info.clip.x;
			               	 }
			               	 
			               	 transform.translate(xShift+(border*getScaleFactorOfTheDisplay()), yShift+(border*getScaleFactorOfTheDisplay()));
			               	 path.transform(transform);
			               	
			               	 graphics.setPaint(fillColor);
				                graphics.fill(path);				                
				              
				                if(drawFrame){
				                  graphics.setColor(getContourColor(universalCell));
				                  graphics.draw(path);
				                }
			                }
			                else{
				                graphics.setPaint(fillColor);
				                graphics.fillPolygon(cellPolygon);				                
				                if(drawFrame)
				                {
				                  graphics.setColor(getContourColor(universalCell));
				                  graphics.drawPolygon(cellPolygon);
				                }
			                }
			                
			                //TODO: Nucleus ein- und ausschalten
			              if(showNucleus)
			              {
			                  java.awt.Color nucleusColor = new Color(140,140,240); //(Red, Green, Blue); 
			                  nucleusPolygon= universalCell.getEpisimBioMechanicalModelObject().getPolygonNucleus(info);
			                  graphics.setPaint(nucleusColor);  
			                  graphics.fillPolygon(nucleusPolygon);
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
		if(universalCell.getEpisimBioMechanicalModelObject() instanceof CenterBasedMechanicalModel && universalCell.getActSimState()!= null){
			
			((CenterBasedMechanicalModel) universalCell.getEpisimBioMechanicalModelObject()).calculateClippedCell(universalCell.getActSimState().schedule.getSteps());
			CellEllipse cellEllipseObject = ((CenterBasedMechanicalModel) universalCell.getEpisimBioMechanicalModelObject()).getCellEllipseObject();
			
			if(SimStateServer.getInstance().getSimState() == SimState.PAUSE || SimStateServer.getInstance().getSimState() == SimState.STOP){ 
				cellEllipseObject.translateCell(new DrawInfo2D(new Rectangle2D.Double(info.draw.x, info.draw.y, info.draw.width, info.draw.height),
		             		 new Rectangle2D.Double(info.clip.x, info.clip.y, info.clip.width, info.clip.height)));        
	      }	      	  
	    	graphics.setPaint(getFillColor(universalCell));    
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
	    	//must be set at the very end of the paint method
	       
	       if(SimStateServer.getInstance().getSimState() == SimState.PLAY){ 
	      	 cellEllipseObject.setLastDrawInfo2D(new DrawInfo2D(new Rectangle2D.Double(info.draw.x, info.draw.y, info.draw.width, info.draw.height),
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
      double maxAge= kcyte.getEpisimCellBehavioralModelObject().getMaxAge();
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
        	  
              
            if((kcyte.isOuterCell()) && (coloringType==2)){red=0xF3; green=0xBE; blue=0x4E;}        
            if((kcyte.getEpisimBioMechanicalModelObject().isMembraneCell()) && (coloringType==2)){red=0xF3; green=0xFF; blue=0x4E;}                        
       }
       if (coloringType==3) // Age coloring
       {
              calculatedColorValue= (int) (250-250*kcyte.getEpisimCellBehavioralModelObject().getAge()/maxAge);
              red=255;
              green=calculatedColorValue;                        
              blue=calculatedColorValue;
              if (keratinoType== EpisimDifferentiationLevel.STEMCELL){ red=148; green=167; blue=214; } // stem cells do not age
       }
       if ((coloringType==4))  // Calcium coloring
       {
              calculatedColorValue= (int) (255* (1-((kcyte.getEpisimCellBehavioralModelObject().getCa()) / kcyte.getEpisimCellBehavioralModelObject().getMaxCa())));
              red=calculatedColorValue;         
              green=calculatedColorValue;
              blue=255;
                   
              drawFrame=false; // immer frame zeichnen
       }
       if (coloringType==5)  // Lamella coloring
       {
              calculatedColorValue= (int) (255* (1-(kcyte.getEpisimCellBehavioralModelObject().getLam() / kcyte.getEpisimCellBehavioralModelObject().getMaxLam())));
              red=calculatedColorValue;         
              green=255;
              blue=calculatedColorValue;
              
        }
        if (coloringType==6)  // Lipid coloring
        {
              if (kcyte.getEpisimCellBehavioralModelObject().getLip()>=ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getMinSigLipidsBarrier()){red=0xCB; green=0x2F; blue=0x9F; }
              else{ red=0xAF; green=0xCB; blue=0x97; }
        }
        if (coloringType==7)  // ion transport activitiy
        {                     
              calculatedColorValue= (int) (255* (1-(kcyte.getHasGivenIons() / 10)));
              red=calculatedColorValue;
              green=255;
              blue=calculatedColorValue;
        }
        if(coloringType==8){ //Colors are calculated in the cellbehavioral model
        	  red=kcyte.getEpisimCellBehavioralModelObject().getColorR();
           green=kcyte.getEpisimCellBehavioralModelObject().getColorG();
           blue=kcyte.getEpisimCellBehavioralModelObject().getColorB();
        }

      // Limit the colors to 255
      green=(green>255)?255:((green<0)?0:green);
      red=(red>255)?255:((red<0)?0:red);
      blue=(blue>255)?255:((blue<0)?0:blue);
      
      if(kcyte.isTracked()) return Color.RED;
      return new Color(red, green, blue);
   }
   
   private Color getContourColor(UniversalCell kcyte){
   	Color myFrameColor = Color.white; //new Color(Red, Green, Blue);   	                               
      int coloringType=MiscalleneousGlobalParameters.instance().getTypeColor();
   	myFrameColor=new Color(200, 165, 200);                
   	if (coloringType==3 || coloringType==4 || coloringType==5 || coloringType==6 || coloringType==7 || coloringType==9) // Age coloring
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
       Polygon pol = ((UniversalCell)object).getEpisimBioMechanicalModelObject().getPolygonCell();      	 
       return ( pol.intersects( range.clip.x, range.clip.y, range.clip.width, range.clip.height));
      }
	   return false; 
   }
   
   
}