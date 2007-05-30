package sim.app.episim.visualization;
import sim.app.episim.BioChemicalModelController;
import sim.app.episim.GrahamPoint;
import sim.app.episim.GrahamScan;
import sim.app.episim.KCyteClass;
import sim.portrayal.*;
import sim.util.*;
import java.util.Comparator;
import java.awt.*;
import java.awt.geom.*;

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
    public Paint paint;

    private int[] xPoints = new int[20];
    private int[] yPoints = new int[20];
    java.awt.Color myFrameColor = Color.white; //new Color(Red, Green, Blue);
     
    private BioChemicalModelController modelController;
    public boolean drawFrame = true;

    public KeratinocytePortrayal2D() {
   	 
   	 this(Color.gray,false); 
   	 modelController = BioChemicalModelController.getInstance();
   	 
    }
    public KeratinocytePortrayal2D(Paint paint)  { 
   	 
   	 this(paint,true); 
   	 modelController = BioChemicalModelController.getInstance();
   	 
    }
    public KeratinocytePortrayal2D(boolean drawFrame) { 
   	 this(Color.gray,drawFrame); 
   	 modelController = BioChemicalModelController.getInstance();
   	 }
    public KeratinocytePortrayal2D(Paint paint, boolean drawFrame)  { 
   	 modelController = BioChemicalModelController.getInstance();
   	 this.paint = paint; 
   	 this.drawFrame = drawFrame; 
   	 }
        
       
    
    // assumes the graphics already has its color set
    public void draw(Object object, Graphics2D graphics, DrawInfo2D info)
        {            

            
   	
   	      int wloc=5;
            int hloc=5;
            int xsL=0,xsR=0;

            int xform[]=new int[50];
            int yform[]=new int[50];  
            GrahamPoint convexhull[]=new GrahamPoint[50];            
            GrahamPoint newvoronoihull[]=new GrahamPoint[50];            
            int ResNumPoints=0;
            int formCount=0;
                        
            //boolean rahmen=false;            
            boolean drawVoronoi=false;
            boolean showNucleus=false;
            if (object instanceof KCyteClass)
            {                
                final KCyteClass kc=((KCyteClass)object);
                kc.LastDrawInfoAssigned=true;
                kc.LastDrawInfoX=info.draw.x;
                kc.LastDrawInfoY=info.draw.y;
                if (kc.inNirvana) return;       
               
                // get Agent data
                int id=kc.identity;
                int numFlockers=kc.theEpidermis.allocatedKCytes;
                int keratinoType=kc.KeratinoType;                                
                int typeColor=modelController.getIntField("typeColor_1to9");                
                int ownCol=(kc.ownColor)*kc.theEpidermis.individualColor;
                int maxAge= BioChemicalModelController.getInstance().getIntField("maxCellAge_t"); 
                wloc = kc.KeratinoWidth;                                
                hloc = kc.KeratinoHeight;  

                drawVoronoi=((typeColor==8) || (typeColor==9));
                if (drawVoronoi)
                {
                    drawVoronoi=false; // until here to draw a voronoi is only a wish, not test, if it can be done
                    formCount=0;
                    int i=0;
                    if (kc.formCount>0) // any neighbors counted in KCyteClass
                    {
                        for (i=0; i<kc.formCount; ++i)
                        {
                            if ((kc.NeighborDrawInfoX[i]==0) || (kc.NeighborDrawInfoY[i]==0)) continue;
                            int dx=(int) kc.NeighborDrawInfoX[i] - (int) info.draw.x;
                            int dy=(int) kc.NeighborDrawInfoY[i] - (int) info.draw.y;
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
                            ResNumPoints=GrahamScan.computeHull(convexhull, formCount);
                            for (i=0;i<ResNumPoints;i++)
                            {
                                // last tringle connects last and first point of convexhull
                                int p1,p2; // index of points
                                if (i==ResNumPoints-1) { p1=i; p2=0;}
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
                                double optDist=kc.gInitialKeratinoHeight*2; // factor 3 is much more 
                                if (actdist<optDist)
                                {
                                    dx=(actdist>0)?(optDist+0.1)/actdist*dx:0;    // increase dx by factor optDist/actdist
                                    dy=(actdist>0)?(optDist+0.1)/actdist*dy:0;    // increase dy by factor optDist/actdist            
                                }
                                double newx=info.draw.x+dx;
                                double newy=info.draw.y+dy;
                                // Build Average of new and old shape when both have the same number of vertexes, i.e. nodes
                                if (ResNumPoints==kc.voronoihullvertexes)
                                {
                                    newx=(newx*0.05+kc.voronoihull[i].x*0.95);
                                    newy=(newy*0.05+kc.voronoihull[i].y*0.95);
                                    kc.voronoihull[i]=new GrahamPoint(i, newx, newy);
                                }
                                else
                                    kc.voronoihull[i]=new GrahamPoint(i, newx, newy);
                            }                            
                            kc.voronoihullvertexes=ResNumPoints;
                            if (kc.voronoihullvertexes>4) 
                            {
                                drawVoronoi=true;
                                ++kc.VoronoiStable;  // Voronoi was possible
                            }

                        } // not enough points for Voronoi
                        else
                            {
                                --kc.VoronoiStable;  // Voronoi was not possible
                                drawVoronoi=false;
                            }
                    } // any neighbors counted in KCyteClass
                } // Voronoi should be drawn
                // Determine color
                
                //
                // set shape
                //
                
                if(keratinoType == modelController.getGlobalIntConstant("KTYPE_STEM")){ 
               	 showNucleus=true; 
               	 drawFrame=true;
               	 } // dunkelblau // mittels word zeichnen einfach zu finden
                else if(keratinoType == modelController.getGlobalIntConstant("KTYPE_TA")){ 
               	 showNucleus=true; 
               	 drawFrame=true; 
                }                             
                else if(keratinoType == modelController.getGlobalIntConstant("KTYPE_SPINOSUM")){ 
               	 showNucleus=true; 
               	 drawFrame=true;  
                }
                else if(keratinoType == modelController.getGlobalIntConstant("KTYPE_LATESPINOSUM")){ 
               	 showNucleus=true; 
               	 drawFrame=true; 
                }
                else if(keratinoType == modelController.getGlobalIntConstant("KTYPE_GRANULOSUM")){ 
               	 drawFrame=false; 
               	 drawVoronoi=false; 
               	 showNucleus=false; 
               }
                else if(keratinoType == modelController.getGlobalIntConstant("KTYPE_NONUCLEUS")){ 
               	 drawFrame=true; 
               	 drawVoronoi=false; 
               	 showNucleus=false;
                }
                        
                if ((kc.isMembraneCell) || (kc.isOuterCell)) drawVoronoi=false;
                //
                // set colors
                //
                myFrameColor=new Color(200, 165, 200);                
                int Colorvalue=0;  
                int Red=255;         
                int Green=0;
                int Blue=0;
                if ((typeColor<1) || (typeColor>9)) { typeColor=1; modelController.setIntField("typeColor_1to9", 1); }
                
                if ((typeColor==1) || (typeColor==2) || (typeColor==8))  // Cell type coloring
                    {
                        
                  	   if(keratinoType == modelController.getGlobalIntConstant("KTYPE_STEM")){ 
                  	   	Red=0x46; 
                  	   	Green=0x72; 
                  	   	Blue=0xBE;  
                  	   } // dunkelblau // mittels word zeichnen einfach zu finden
                  	   else if(keratinoType == modelController.getGlobalIntConstant("KTYPE_TA")){ 
                  	   	Red=148; 
                  	   	Green=167; 
                  	   	Blue=214;  
                  	   }                             
                  	   else if(keratinoType == modelController.getGlobalIntConstant("KTYPE_SPINOSUM")){ 
                  	   	Red=0xE1; 
                  	   	Green=0x6B; 
                  	   	Blue=0xF6; 
                  	   }
                  	   else if(keratinoType == modelController.getGlobalIntConstant("KTYPE_LATESPINOSUM")){ 
                  	   	Red=0xC1; 
                  	   	Green=0x4B; 
                  	   	Blue=0xE6;
                  	   }
                  	   else if(keratinoType == modelController.getGlobalIntConstant("KTYPE_GRANULOSUM")){ 
                  	   	Red=204; 
                  	   	Green=0; 
                  	   	Blue=102; 
                  	   }
                  	   else if(keratinoType == modelController.getGlobalIntConstant("KTYPE_NONUCLEUS")){ 
                  	   	Red=198; 
                  	   	Green=148; 
                  	   	Blue=60; 
                  	   }
                        
                        if ((kc.isOuterCell) && (typeColor==2))
                            {   Red=0xF3; Green=0xBE; Blue=0x4E; }        
                        if ((kc.isMembraneCell) && (typeColor==2))
                            {   Red=0xF3; Green=0xFF; Blue=0x4E; }                        
                   }
                  if (typeColor==3) // Age coloring
                    {
                        Colorvalue= (int) (250-250*kc.KeratinoAge/maxAge);
                        Red=255;
                        Green=Colorvalue;                        
                        Blue=Colorvalue;
                        if (keratinoType==modelController.getGlobalIntConstant("KTYPE_STEM"))
                        { Red=148; Green=167; Blue=214; } // stem cells do not age
                        myFrameColor=Color.black;
                    }
                  if ((typeColor==4) || (typeColor==9))  // Calcium coloring
                    {
                        Colorvalue= (int) (255* (1-((kc.ownSigExternalCalcium+kc.ownSigInternalCalcium) / modelController.getDoubleField("calSaturation"))));
                        Red=Colorvalue;         
                        Green=Colorvalue;
                        Blue=255;
                        myFrameColor=Color.black;
                        if (typeColor==9) showNucleus=false;
                        drawFrame=false; // immer frame zeichnen
                    }
                  if (typeColor==5)  // Lamella coloring
                    {
                        Colorvalue= (int) (255* (1-(kc.ownSigLamella / modelController.getDoubleField("lamellaSaturation"))));
                        Red=Colorvalue;         
                        Green=255;
                        Blue=Colorvalue;
                        myFrameColor=Color.black;
                    }
                  if (typeColor==6)  // Lipid coloring
                    {
                        if (kc.ownSigLipids>=modelController.getDoubleField("minSigLipidsBarrier"))
                        { Red=0xCB; Green=0x2F; Blue=0x9F; }
                        else 
                        { Red=0xAF; Green=0xCB; Blue=0x97; }
                        myFrameColor=Color.black;
                    }
                  if (typeColor==7)  // ion transport activitiy
                    {                     
                        Colorvalue= (int) (255* (1-(kc.hasGivenIons / 10)));
                        Red=Colorvalue;
                        Green=255;
                        Blue=Colorvalue;
                        myFrameColor=Color.black;
                    }                

                // Limit the colors to 255
                Green=(Green>255)?255:((Green<0)?0:Green);
                Red=(Red>255)?255:((Red<0)?0:Red);
                Blue=(Blue>255)?255:((Blue<0)?0:Blue);                
                java.awt.Color myColor = new Color(Red, Green, Blue);
                //java.awt.Color myColor = new Color (255-ownCol, Colorvalue+ownCol, Colorvalue+ownCol);

                
                GeneralPath cellPath;
                GeneralPath nucleusPath;
                if (drawVoronoi) // it was possible to draw it five times then it may be stable
                {                   
                    cellPath = createVoronoiPath( info, wloc, hloc, kc.voronoihull, kc.voronoihullvertexes);
                    graphics.setPaint(myColor);
                    graphics.fill(cellPath);                
                }
                else {
                    cellPath = createGeneralPath( info, wloc, hloc);
                    graphics.setPaint(myColor);
                    graphics.fill(cellPath);
                }
                if( drawFrame )
                {
                    graphics.setColor( myFrameColor);
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
                System.out.println(" NOFLO");
            }
        
        //if (rahmen)
        //{
///            graphics.setColor( Color.red );
//            graphics.draw(new Rectangle(xsL, 90, xsR-xsL, 10));
//            //graphics.draw3DRect(xsL, 90, xsR-xsL, 10, true);
//        }
        }

    protected GeneralPath generalPath = new GeneralPath();

    /** 
     * Creates a general path for the bounding hexagon 
     */
    public GeneralPath createGeneralPath( DrawInfo2D info, int w, int h )
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
            generalPath.moveTo( xPoints[0], yPoints[0] );
            for( int i = 1 ; i < 6 ; i++ )
                generalPath.lineTo( xPoints[i], yPoints[i] );
            generalPath.closePath();
            }
        return generalPath;
        }

        /** Creates a general path for the bounding hexagon */
    GeneralPath createVoronoiPath( DrawInfo2D info, int w, int h, GrahamPoint[] pL, int formCount )
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
                    generalPath.moveTo( xPoints[0], yPoints[0] );
                    for( int i = 1 ; i < 6 ; i++ )
                        generalPath.lineTo( xPoints[i], yPoints[i] );
                    generalPath.closePath();
                }
            }
        return generalPath;
        }
    
    /** If drawing area intersects selected area, add last portrayed object to the bag */
    public boolean hitObject(Object object, DrawInfo2D range)
        {
            int w=1;
            int h=1;
            if (object instanceof KCyteClass)
            {
                KCyteClass kc=((KCyteClass)object);
                int KeratinoAge=kc.KeratinoAge;                
                w=5;
                h=5-(int)KeratinoAge/60;
                h=(h<1 ? 1:h);
            };                    
            
        GeneralPath generalPath = createGeneralPath( range, w, h );
        Area area = new Area( generalPath );
        return ( area.intersects( range.clip.x, range.clip.y, range.clip.width, range.clip.height ) );
        }
    }
