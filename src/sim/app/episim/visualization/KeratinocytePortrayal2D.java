package sim.app.episim.visualization;
import sim.SimStateServer;
import sim.SimStateServer.SimState;
import sim.app.episim.CellType;
import sim.app.episim.KCyte;
import sim.app.episim.biomechanics.Calculators;
import sim.app.episim.biomechanics.CellPolygon;
import sim.app.episim.biomechanics.Vertex;
import sim.app.episim.model.BioChemicalModelController;
import sim.app.episim.model.BioMechanicalModelController;
import sim.app.episim.model.MiscalleneousGlobalParameters;
import sim.app.episim.model.ModelController;
import sim.app.episim.util.CellEllipseIntersectionCalculationRegistry;
import sim.app.episim.util.EllipseIntersectionCalculatorAndClipper;
import sim.portrayal.*;
import java.awt.*;
import java.awt.geom.*;

import episiminterfaces.EpisimCellDiffModelGlobalParameters;




public class KeratinocytePortrayal2D extends SimplePortrayal2D
    {
    private Paint paint;

    private int[] xPoints = new int[20];
    private int[] yPoints = new int[20];
   
      
    private ModelController modelController;
    private BioChemicalModelController biochemModelController;
    private BioMechanicalModelController biomechModelController;
    private boolean drawFrame = true;
    
   
    
    public KeratinocytePortrayal2D() {
   	 
   	 this(Color.gray,false); 
   	
   	 
    }
    public KeratinocytePortrayal2D(Paint paint)  { 
   	 
   	 this(paint,true); 
   	 
   	 
    }
    public KeratinocytePortrayal2D(boolean drawFrame) { 
   	 this(Color.gray,drawFrame); 
   	
   	 }
    public KeratinocytePortrayal2D(Paint paint, boolean drawFrame)  { 
   	 modelController = ModelController.getInstance();
   	 biomechModelController = modelController.getBioMechanicalModelController();
   	 biochemModelController = modelController.getBioChemicalModelController();
   	 this.paint = paint; 
   	 this.drawFrame = drawFrame;
   	 
    }
        
       
    
    // assumes the graphics already has its color set
    public void draw(Object object, Graphics2D graphics, DrawInfo2D info)
    {                         
            //boolean rahmen=false;            
          
            boolean showNucleus=true;
            if (object instanceof KCyte)
            {                
                final KCyte kcyte=((KCyte)object);
               if(SimStateServer.getInstance().getSimState() == SimState.PAUSE || SimStateServer.getInstance().getSimState() == SimState.STOP){ 
    		         kcyte.getCellEllipseObject().translateCell(new DrawInfo2D(new Rectangle2D.Double(info.draw.x, info.draw.y, info.draw.width, info.draw.height),
    		             		 new Rectangle2D.Double(info.clip.x, info.clip.y, info.clip.width, info.clip.height)));
    		        
                }
                
               
                int keratinoType=kcyte.getEpisimCellDiffModelObject().getDifferentiation();                                
                int colorType=MiscalleneousGlobalParameters.instance().getTypeColor();
                
                
                if(keratinoType == EpisimCellDiffModelGlobalParameters.STEMCELL
                  	 || keratinoType == EpisimCellDiffModelGlobalParameters.TACELL
                  	 || keratinoType == EpisimCellDiffModelGlobalParameters.EARLYSPICELL
                  	 || keratinoType == EpisimCellDiffModelGlobalParameters.LATESPICELL){ 
                  	 showNucleus=true; 
                  	 drawFrame=true;
                   } 
                else if(keratinoType == EpisimCellDiffModelGlobalParameters.KTYPE_NONUCLEUS 
                  		 || keratinoType == EpisimCellDiffModelGlobalParameters.GRANUCELL){ 
                  	 drawFrame=true;                  	 
                  	 showNucleus=false;
                }
                
                
              if(colorType < 8){
                     
                //
                // set shape
                //
                
                                
                //getColors
                Color fillColor = getFillColor(kcyte);
                GeneralPath cellPath;
                GeneralPath nucleusPath;
               
                    cellPath = createGeneralPath(info, kcyte.getKeratinoWidth(), kcyte.getKeratinoHeight());
                    graphics.setPaint(fillColor);
                    graphics.fill(cellPath);
                
                if(drawFrame)
                {
                    graphics.setColor(getContourColor(kcyte));
                    graphics.draw(cellPath);
                }
                if (showNucleus)
                {
                    java.awt.Color nucleusColor = new Color(140,140,240); //(Red, Green, Blue); 
                    nucleusPath= createGeneralPath( info, 2, 2);
                    graphics.setPaint(nucleusColor);  
                    graphics.fill(nucleusPath);
                }
                
              }
              else if(colorType == 8){
            	              	  
            	  graphics.setPaint(getFillColor(kcyte));            	     	  
            	  graphics.fill(kcyte.getCellEllipseObject().getClippedEllipse());
            	  if(drawFrame){
	            	  graphics.setPaint(getContourColor(kcyte));
	            	  graphics.draw(kcyte.getCellEllipseObject().getClippedEllipse());
            	  
            	  }
            	 if(showNucleus){
            		  Color nucleusColor = new Color(140,140,240); //(Red, Green, Blue); 
	                 graphics.setPaint(nucleusColor);
	            	  final double NUCLEUSRAD = 0.75;
	                 graphics.fill(new Ellipse2D.Double(kcyte.getCellEllipseObject().getX()-NUCLEUSRAD*info.draw.width, kcyte.getCellEllipseObject().getY()- NUCLEUSRAD*info.draw.height,2*NUCLEUSRAD*info.draw.width, 2*NUCLEUSRAD*info.draw.height));
            	 }
              }
              
              else if(colorType == 10){
            	 
            	CellPolygon  cellPol = CellEllipseIntersectionCalculationRegistry.getInstance().getCellPolygonByCellEllipseId(kcyte.getCellEllipseObject().getId());
            	Vertex[] vertices = null;
        			if(cellPol != null && (vertices = cellPol.getVertices()) != null){
        				drawCellPolygon(graphics, cellPol);
        				
        				for(Vertex v : vertices){
        					/*if(v != null){
        						if(v.isWasDeleted())drawPoint(graphics, v.getIntX(), v.getIntY(), 5, Color.BLACK);
        						else if(v.isEstimatedVertex()) drawPoint(graphics, v.getIntX(), v.getIntY(), 5, Color.MAGENTA);
        						else if(v.isMergeVertex()) drawPoint(graphics, v.getIntX(), v.getIntY(), 5, Color.YELLOW);
        						else drawPoint(graphics, v.getIntX(), v.getIntY(), 5, Color.RED);
        					}*/
        					
        					drawPoint(graphics, v.getIntX(), v.getIntY(), 3, new Color(99,37,35));
        				}
        				
        			}
              }
              
              
             //must be set at the very end of the paint method
              
            if(SimStateServer.getInstance().getSimState() == SimState.PLAY){ 
		         kcyte.getCellEllipseObject().setLastDrawInfo2D(new DrawInfo2D(new Rectangle2D.Double(info.draw.x, info.draw.y, info.draw.width, info.draw.height),
		             		 new Rectangle2D.Double(info.clip.x, info.clip.y, info.clip.width, info.clip.height)), false);
            }
              
            } else { 
                graphics.setPaint(paint);          
            }
        }

    protected GeneralPath generalPath = new GeneralPath();
    
    
   
    
    

    /** 
     * Creates a general path for the bounding hexagon 
     */
    private GeneralPath createGeneralPath( DrawInfo2D info, int w, int h )
        {
        generalPath.reset();
        
   	  
        // we are doing a simple draw, so we ignore the info.clip
        if( info instanceof HexaDrawInfo2D )
         {
      	  
            final HexaDrawInfo2D temp = (HexaDrawInfo2D)info;
            generalPath.moveTo( temp.xPoints[0], temp.yPoints[0] );
            
            for( int i = 1 ; i < 6 ; i++ )
                generalPath.lineTo( temp.xPoints[i], temp.yPoints[i] );
            generalPath.closePath();
        }
        else
            {
      	  	calculateHexagonPoints(info,w,h);
            generalPath.moveTo(xPoints[0], yPoints[0] );
            for( int i = 1 ; i < 6 ; i++ ) generalPath.lineTo( xPoints[i], yPoints[i] );
            generalPath.closePath();
            }
        return generalPath;
        }

     
    
    /** If drawing area intersects selected area, add last portrayed object to the bag */
       
   private void calculateHexagonPoints(DrawInfo2D info, int w, int h){
   	 xPoints[0] = (int)(info.draw.x+info.draw.width/2.0*w);
       yPoints[0] = (int)(info.draw.y);
       xPoints[1] = (int)(info.draw.x+info.draw.width/4.0*w);
       yPoints[1] = (int)(info.draw.y-info.draw.height/2.0*h);
       xPoints[2] = (int)(info.draw.x-info.draw.width/4.0*w);
       yPoints[2] = (int)(info.draw.y-info.draw.height/2.0*h);
       xPoints[3] = (int)(info.draw.x-info.draw.width/2.0*w);
       yPoints[3] = (int)(info.draw.y);
       xPoints[4] = (int)(info.draw.x-info.draw.width/4.0*w);
       yPoints[4] = (int)(info.draw.y+info.draw.height/2.0*h);
       xPoints[5] = (int)(info.draw.x+info.draw.width/4.0*w);
       yPoints[5] = (int)(info.draw.y+info.draw.height/2.0*h);
   }
   
   
   private Color getFillColor(KCyte kcyte){
   	int keratinoType=kcyte.getEpisimCellDiffModelObject().getDifferentiation();                                
      int coloringType=MiscalleneousGlobalParameters.instance().getTypeColor();
   	//
      // set colors
      //
                    
      int calculatedColorValue=0;  
      double maxAge= kcyte.getEpisimCellDiffModelObject().getMaxAge();
      int red=255;         
      int green=0;
      int blue=0;
            
      if ((coloringType==1) || (coloringType==2) || (coloringType==8) || (coloringType ==10))  // Cell type coloring
      {              
        	   if(keratinoType == EpisimCellDiffModelGlobalParameters.STEMCELL){red=0x46; green=0x72; blue=0xBE;} 
        	   else if(keratinoType == EpisimCellDiffModelGlobalParameters.TACELL){red=148; green=167; blue=214;}                             
        	   else if(keratinoType == EpisimCellDiffModelGlobalParameters.EARLYSPICELL){red=0xE1; green=0x6B; blue=0xF6;}
        	   else if(keratinoType == EpisimCellDiffModelGlobalParameters.LATESPICELL){red=0xC1; green=0x4B; blue=0xE6;}
        	   else if(keratinoType == EpisimCellDiffModelGlobalParameters.GRANUCELL){red=204; green=0; blue=102;}
        	   else if(keratinoType == EpisimCellDiffModelGlobalParameters.KTYPE_NONUCLEUS){red=198; green=148; blue=60;}
              
            if((kcyte.isOuterCell()) && (coloringType==2)){red=0xF3; green=0xBE; blue=0x4E;}        
            if((kcyte.isMembraneCell()) && (coloringType==2)){red=0xF3; green=0xFF; blue=0x4E;}                        
       }
       if (coloringType==3) // Age coloring
       {
              calculatedColorValue= (int) (250-250*kcyte.getEpisimCellDiffModelObject().getAge()/maxAge);
              red=255;
              green=calculatedColorValue;                        
              blue=calculatedColorValue;
              if (keratinoType== EpisimCellDiffModelGlobalParameters.STEMCELL){ red=148; green=167; blue=214; } // stem cells do not age
       }
       if ((coloringType==4))  // Calcium coloring
       {
              calculatedColorValue= (int) (255* (1-((kcyte.getEpisimCellDiffModelObject().getCa()) / kcyte.getEpisimCellDiffModelObject().getMaxCa())));
              red=calculatedColorValue;         
              green=calculatedColorValue;
              blue=255;
                   
              drawFrame=false; // immer frame zeichnen
       }
       if (coloringType==5)  // Lamella coloring
       {
              calculatedColorValue= (int) (255* (1-(kcyte.getEpisimCellDiffModelObject().getLam() / kcyte.getEpisimCellDiffModelObject().getMaxLam())));
              red=calculatedColorValue;         
              green=255;
              blue=calculatedColorValue;
              
        }
        if (coloringType==6)  // Lipid coloring
        {
              if (kcyte.getEpisimCellDiffModelObject().getLip()>=biochemModelController.getEpisimCellDiffModelGlobalParameters().getMinSigLipidsBarrier()){red=0xCB; green=0x2F; blue=0x9F; }
              else{ red=0xAF; green=0xCB; blue=0x97; }
        }
        if (coloringType==7)  // ion transport activitiy
        {                     
              calculatedColorValue= (int) (255* (1-(kcyte.getHasGivenIons() / 10)));
              red=calculatedColorValue;
              green=255;
              blue=calculatedColorValue;
        }
        if(coloringType==9){ //Colors are calculated in the biochemical model
        	  red=kcyte.getEpisimCellDiffModelObject().getColorR();
           green=kcyte.getEpisimCellDiffModelObject().getColorG();
           blue=kcyte.getEpisimCellDiffModelObject().getColorB();
        }

      // Limit the colors to 255
      green=(green>255)?255:((green<0)?0:green);
      red=(red>255)?255:((red<0)?0:red);
      blue=(blue>255)?255:((blue<0)?0:blue);
      
      if(kcyte.isTracked()) return Color.RED;
      return new Color(red, green, blue);
   }
   
   private Color getContourColor(KCyte kcyte){
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
       int w=1;
       int h=1;
       if (object instanceof KCyte)
       {
           KCyte kc=((KCyte)object);
                        
           w=5;
           h=5-(int)kc.getEpisimCellDiffModelObject().getAge()/60;
           h=(h<1 ? 1:h);
       };                    
	       
	   GeneralPath generalPath = createGeneralPath( range, w, h );
	   Area area = new Area( generalPath );
	   return ( area.intersects( range.clip.x, range.clip.y, range.clip.width, range.clip.height ) );
   }

   private void drawCellPolygon(Graphics2D g, CellPolygon cell){
		if(cell != null){
			
			Polygon p = new Polygon();			
			Vertex[] sortedVertices = cell.getSortedVerticesUsingTravellingSalesmanSimulatedAnnealing();
		
			for(Vertex v : sortedVertices){	
				p.addPoint(v.getIntX(), v.getIntY());
				
			}
					
			Color oldColor = g.getColor();
			//g.setColor(cell.getFillColor());
			g.setColor(Color.PINK);
			g.fillPolygon(p);
			g.setColor(new Color(99,37,35));
			g.drawPolygon(p);
			g.setColor(oldColor);
			
			//drawVertex(g,Calculators.getCellCenter(cell),false);
		}
	}
   
}