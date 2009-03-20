package sim.app.episim.visualization;
import sim.app.episim.GrahamPoint;
import sim.app.episim.GrahamScan;
import sim.app.episim.KCyte;
import sim.app.episim.model.BioChemicalModelController;
import sim.app.episim.model.BioMechanicalModelController;
import sim.app.episim.model.MiscalleneousGlobalParameters;
import sim.app.episim.model.ModelController;
import sim.portrayal.*;
import sim.util.*;
import java.util.Comparator;
import java.awt.*;
import java.awt.geom.*;

import episiminterfaces.EpisimCellDiffModelGlobalParameters;

//import sim.app.ngflock.*;
//import sim.app.episim1.*;
/**
   A simple portrayal for 2D visualization of hexagons. It extends the SimplePortrayal2D and
   it manages the drawing and hit-testing for hexagonal shapes. If the DrawInfo2D parameter
   received by draw and hitObject functions is an instance of HexaDrawInfo2D, better information
   is extracted and used to make everthing look better. Otherwise, hexagons may be created from
   information stored in simple DrawInfo2D objects, but overlapping or extra empty spaces may be
   observed (especially when increasing the scale).
*/

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
            boolean drawVoronoi=false;
            boolean showNucleus=false;
            if (object instanceof KCyte)
            {                
                final KCyte kcyte=((KCyte)object);
                kcyte.setLastDrawInfoAssigned(true);
                kcyte.setLastDrawInfoX(info.draw.x);
                kcyte.setLastDrawInfoY(info.draw.y);
                int keratinoType=kcyte.getEpisimCellDiffModelObject().getDifferentiation();                                
                int colorType=MiscalleneousGlobalParameters.instance().getTypeColor();              
                
                drawVoronoi=((colorType==8) || (colorType==9));
                showNucleus=(colorType!=9);
                if (drawVoronoi)
                {
                   drawVoronoi= calculateVoronoi(kcyte, info);
                } // Voronoi should be drawn
                // Determine color
                
                //
                // set shape
                //
                
                if(keratinoType == EpisimCellDiffModelGlobalParameters.STEMCELL
               	 || keratinoType == EpisimCellDiffModelGlobalParameters.TACELL
               	 || keratinoType == EpisimCellDiffModelGlobalParameters.EARLYSPICELL
               	 || keratinoType == EpisimCellDiffModelGlobalParameters.LATESPICELL){ 
               	 showNucleus=true; 
               	 drawFrame=true;
                } 
                else if(keratinoType == EpisimCellDiffModelGlobalParameters.GRANUCELL){ 
               	 drawFrame=false; 
               	 drawVoronoi=false; 
               	 showNucleus=false; 
               }
                else if(keratinoType == EpisimCellDiffModelGlobalParameters.KTYPE_NONUCLEUS){ 
               	 drawFrame=true; 
               	 drawVoronoi=false; 
               	 showNucleus=false;
                }
                        
                if ((kcyte.isMembraneCell()) || (kcyte.isOuterCell())) drawVoronoi=false;
                
                //getColors
                Color fillColor = getFillColor(kcyte);
                GeneralPath cellPath;
                GeneralPath nucleusPath;
                if (drawVoronoi) // it was possible to draw it five times then it may be stable
                {                   
                    cellPath = createVoronoiPath( info, kcyte.getKeratinoWidth(), kcyte.getKeratinoHeight(), kcyte.getVoronoihull(), kcyte.getVoronoihullvertexes());
                    graphics.setPaint(fillColor);
                    graphics.fill(cellPath);                
                }
                else {
                    cellPath = createGeneralPath(info, kcyte.getKeratinoWidth(), kcyte.getKeratinoHeight());
                    graphics.setPaint(fillColor);
                    graphics.fill(cellPath);
                }
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
            } else 
            { 
                graphics.setPaint(paint);  
                System.out.println("NOFLO");
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

        /** Creates a general path for the bounding hexagon */
   private GeneralPath createVoronoiPath( DrawInfo2D info, int w, int h, GrahamPoint[] pL, int formCount )
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
                if (formCount>4)
                {
                    xPoints[0]=(int) pL[0].x;
                    yPoints[0]=(int) pL[0].y;
                    generalPath.moveTo( xPoints[0], yPoints[0] );
                    for (int i=1; ((i<formCount) && (formCount<=19)); ++i)
                    {
                        xPoints[i]=(int) pL[i].x;
                        yPoints[i]=(int) pL[i].y;
                        generalPath.lineTo( xPoints[i], yPoints[i] );
                    }
                    generalPath.closePath();
                }
                else
                {
                                   
               	 calculateHexagonPoints(info,w,h);
                                 
                    generalPath.moveTo( xPoints[0], yPoints[0] );
                    for( int i = 1 ; i < 6 ; i++)
                        generalPath.lineTo(xPoints[i], yPoints[i]);
                    generalPath.closePath();
                }
            }
        return generalPath;
        }
    
    /** If drawing area intersects selected area, add last portrayed object to the bag */
    /*   public boolean hitObject(Object object, DrawInfo2D range)
        {
            int w=1;
            int h=1;
            if (object instanceof KCyte)
            {
                KCyte kcyte=((KCyte)object);
                int keratinoAge=kcyte.getEpisimCellDiffModelObject().getAge();                
                w=5;
                h=5-(int)keratinoAge/60;
                h=(h<1 ? 1:h);
            };                    
            
        GeneralPath generalPath = createGeneralPath( range, w, h );
        Area area = new Area( generalPath );
        return ( area.intersects( range.clip.x, range.clip.y, range.clip.width, range.clip.height ) );
        }
    */
   
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
      int maxAge= kcyte.getEpisimCellDiffModelObject().getMaxAge();
      int red=255;         
      int green=0;
      int blue=0;
      if ((coloringType<1) || (coloringType>10)) { 
     	 coloringType=1; 
     	 MiscalleneousGlobalParameters.instance().setTypeColor(1);
      }
      
      if ((coloringType==1) || (coloringType==2) || (coloringType==8))  // Cell type coloring
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
       if ((coloringType==4) || (coloringType==9))  // Calcium coloring
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
        if(coloringType==10){ //Colors are calculated in the biochemical model
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
   
   private boolean calculateVoronoi(KCyte kcyte, DrawInfo2D info){
   	GrahamPoint convexhull[]=new GrahamPoint[50];            
      
      int resNumPoints=0;
      int formCount=0;
   	boolean drawVoronoi=false; // until here to draw a voronoi is only a wish, not test, if it can be done
      formCount=0;
      int i=0;
      if (kcyte.getFormCount()>0) // any neighbors counted in KCyteClass
      {
          for (i=0; i<kcyte.getFormCount(); ++i)
          {
              if ((kcyte.getNeighborDrawInfoX()[i]==0) || (kcyte.getNeighborDrawInfoY()[i]==0)) continue;
              int dx=(int) kcyte.getNeighborDrawInfoX()[i] - (int) info.draw.x;
              int dy=(int) kcyte.getNeighborDrawInfoY()[i] - (int) info.draw.y;
              if ((dx>-80) && (dx<80) && (dy>-80) && (dy<80))                            
              {
              double xconvex= (int)info.draw.x +(int)(dx/2); //+(int)dx/2;
              double yconvex= (int)info.draw.y +(int)(dy/2); //+(int)dy/2;
              convexhull[formCount]=new GrahamPoint(i,xconvex,yconvex); // i statt 0
              ++formCount;                            
              }
          }

          if (formCount>4)
          {
              resNumPoints=GrahamScan.computeHull(convexhull, formCount);
              for (i=0;i<resNumPoints;i++)
              {
                  // last tringle connects last and first point of convexhull
                  int p1,p2; // index of points
                  if (i==resNumPoints-1){p1=i; p2=0;}
                  else {p1=i; p2=i+1; }
                  double xnewleft=(convexhull[p1].x-info.draw.x)*2+info.draw.x;    // point of tringle in upper left corner
                  double ynewleft=(convexhull[p1].y-info.draw.y)*2+info.draw.y;
                  double xnewright=(convexhull[p2].x-info.draw.x)*2+info.draw.x; // point of tringle in upper right corner
                  double ynewright=(convexhull[p2].y-info.draw.y)*2+info.draw.y;
                  double xcenter=(xnewleft+xnewright+info.draw.x)/3;  // center of triangle
                  double ycenter=(ynewleft+ynewright+info.draw.y)/3;
                  double dx=xcenter-info.draw.x;  // distance is in pixels not in KCyte Dimensions ! Pixels are much more
                  double dy=ycenter-info.draw.y;
                  double actdist=Math.sqrt(dx*dx+dy*dy);
                  double optDist=kcyte.GINITIALKERATINOHEIGHT*2; // factor 3 is much more 
                  if (actdist<optDist)
                  {
                      dx=(actdist>0)?(optDist+0.1)/actdist*dx:0;    // increase dx by factor optDist/actdist
                      dy=(actdist>0)?(optDist+0.1)/actdist*dy:0;    // increase dy by factor optDist/actdist            
                  }
                  double newx=info.draw.x+dx;
                  double newy=info.draw.y+dy;
                  // Build Average of new and old shape when both have the same number of vertexes, i.e. nodes
                  if (resNumPoints==kcyte.getVoronoihullvertexes())
                  {
                      newx=(newx*0.05+kcyte.getVoronoihull()[i].x*0.95);
                      newy=(newy*0.05+kcyte.getVoronoihull()[i].y*0.95);
                      kcyte.getVoronoihull()[i]=new GrahamPoint(i, newx, newy);
                  }
                  else
                      kcyte.getVoronoihull()[i]=new GrahamPoint(i, newx, newy);
              }                            
              kcyte.setVoronoihullvertexes(resNumPoints);
              if (kcyte.getVoronoihullvertexes()>4) 
              {
                  drawVoronoi=true;
                 kcyte.incrementVoronoiStable();  // Voronoi was possible
              }

          } // not enough points for Voronoi
          else
              {
                  kcyte.decrementVoronoiStable();  // Voronoi was not possible
                  drawVoronoi=false;
              }
      } // any neighbors counted in KCyteClass   
      return drawVoronoi;
   }
   
   
   
   
}