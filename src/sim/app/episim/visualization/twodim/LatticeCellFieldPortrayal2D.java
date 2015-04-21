package sim.app.episim.visualization.twodim;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;

import episiminterfaces.EpisimPortrayal;
import sim.app.episim.SimStateServer;
import sim.app.episim.gui.EpisimGUIState;
import sim.app.episim.model.controller.CellBehavioralModelController;
import sim.app.episim.model.controller.ModelController;
import sim.field.grid.Grid2D;
import sim.field.grid.ObjectGrid2D;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.Portrayal;
import sim.portrayal.SimplePortrayal2D;
import sim.portrayal.grid.HexaObjectGridPortrayal2D;
import sim.portrayal.grid.HexaObjectGridPortrayal2DHack;
import sim.portrayal.simple.HexagonalPortrayal2D;
import sim.util.Bag;
import sim.util.Double2D;
import sim.util.Int2D;


public class LatticeCellFieldPortrayal2D extends HexaObjectGridPortrayal2DHack implements EpisimPortrayal{
	private static final String NAME = "Corneal Epithelial Cells";
	private LatticeCellPortrayal2D defaultHexPortrayal;
	
	 private Paint paint;  
       
   
	
	
	public LatticeCellFieldPortrayal2D(){
		this(Color.BLACK); 
	}
	
	 
      
    public LatticeCellFieldPortrayal2D(Paint paint){ 
   	super();   	
   	this.paint = paint;   
   	
	  	defaultHexPortrayal = new LatticeCellPortrayal2D(this, false);
    }    
	
	
	 public Portrayal getDefaultPortrayal()
    {
		 return defaultHexPortrayal;
    }
	 
	 public String getPortrayalName() {
	 	   return NAME;
	 }



	public Rectangle2D.Double getViewPortRectangle() {
		EpisimGUIState guiState = SimStateServer.getInstance().getEpisimGUIState();	   
	   if(guiState != null)return new Rectangle2D.Double(guiState.DISPLAY_BORDER_LEFT,guiState.DISPLAY_BORDER_TOP,guiState.getEpiDisplayWidth(), guiState.getEpiDisplayHeight());
	   else return new Rectangle2D.Double(0,0,0, 0);
   }
	
	  static final double HEXAGONAL_RATIO = 2/Math.sqrt(3);
/**
 * Here we just inverse the y-check as height is inverted in our portrayal, the rest of the implementation stays the same
 */
	 protected void hitOrDraw(Graphics2D graphics, DrawInfo2D info, Bag putInHere)
    {
    final ObjectGrid2D field = (ObjectGrid2D) this.field;
    if (field==null) return;
    
    // Scale graphics to desired shape -- according to p. 90 of Java2D book,
    // this will change the line widths etc. as well.  Maybe that's not what we
    // want.
    
    // first question: determine the range in which we need to draw.
    // We assume that we will fill exactly the info.draw rectangle.
    // We can do the item below because we're an expensive operation ourselves
    
    final int maxX = field.getWidth();
    final int maxY = field.getHeight();
    if (maxX == 0 || maxY == 0) return;
    
    final double divideByX = ((maxX%2==0)?(3.0*maxX/2.0+0.5):(3.0*maxX/2.0+2.0));
    final double divideByY = (1.0+2.0*maxY);

    final double xScale = info.draw.width / divideByX;
    final double yScale = info.draw.height / divideByY;
    int startx = (int)(((info.clip.x - info.draw.x)/xScale-0.5)/1.5)-2;
    int starty = (int)((info.clip.y - info.draw.y)/(yScale*2.0))-2;
    int endx = /*startx +*/ (int)(((info.clip.x - info.draw.x + info.clip.width)/xScale-0.5)/1.5) + 4;  // with rounding, width be as much as 1 off
    int endy = /*starty +*/ (int)((info.clip.y - info.draw.y + info.clip.height)/(yScale*2.0)) + 4;  // with rounding, height be as much as 1 off

//    double precomputedWidth = -1;  // see discussion further below
//    double precomputedHeight = -1;  // see discussion further below

    //
    //
    // CAUTION!
    //
    // At some point we should triple check the math for rounding such
    // that the margins are drawn properly
    //
    //

    // Horizontal hexagons are staggered.  This complicates computations.  Thus
    // if  you have a M x N grid scaled to SCALE, then
    // your height is (N + 0.5) * SCALE
    // and your width is ((M - 1) * (3/4) + 1) * HEXAGONAL_RATIO * SCALE
    // we invert these calculations here to compute the rough width and height
    // for the newinfo here.  Additionally, because the original screen sizes were likely
    // converted from floats to ints, there's a round down there, so we round up to
    // compensate.  This usually results in nice circles.

//    final Rectangle clip = (graphics==null ? null : graphics.getClipBounds());

    DrawInfo2D newinfo = new DrawInfo2D(info.gui, info.fieldPortrayal, new Rectangle2D.Double(0,0, 
            Math.ceil(info.draw.width / (HEXAGONAL_RATIO * ((maxX - 1) * 3.0 / 4.0 + 1))),
            Math.ceil(info.draw.height / (maxY + 0.5))),
        info.clip/*, xPoints, yPoints*/);  // we don't do further clipping 
    newinfo.precise = info.precise;
    newinfo.fieldPortrayal = this;
    newinfo.location = locationToPass;

    if( startx < 0 ) startx = 0;
    if( starty < 0 ) starty = 0;
    if (endx > maxX) endx = maxX;
    if (endy > maxY) endy = maxY;

    for(int y=(maxY-endy);y<(maxY-starty);y++)
        for(int x=startx;x<endx;x++)
            {
            Object obj = field.field[x][y];
            Portrayal p = getPortrayalForObject(obj);
            if (!(p instanceof SimplePortrayal2D))
                throw new RuntimeException("Unexpected Portrayal " + p + " for object " + 
                    obj + " -- expected a SimplePortrayal2D");
            SimplePortrayal2D portrayal = (SimplePortrayal2D) p;
            
            getxyCHack( x, y, xScale, yScale, info.draw.x, info.draw.y, getXyC() );
            getxyCHack( field.ulx(x,y), field.uly(x,y), xScale, yScale, info.draw.x, info.draw.y, getXyC_ul() );
            getxyCHack( field.upx(x,y), field.upy(x,y), xScale, yScale, info.draw.x, info.draw.y, getXyC_up() );
            getxyCHack( field.urx(x,y), field.ury(x,y), xScale, yScale, info.draw.x, info.draw.y, getXyC_ur() );

            getXPoints()[0] = (int)(getXyC_ur()[0]-0.5*xScale);
            //yPoints[0] = (int)(xyC_ur[1]+yScale);
            //xPoints[1] = (int)(xyC_up[0]+0.5*xScale);
            getYPoints()[1] = (int)(getXyC_up()[1]+yScale);
            //xPoints[2] = (int)(xyC_up[0]-0.5*xScale);
            //yPoints[2] = (int)(xyC_up[1]+yScale);
            getXPoints()[3] = (int)(getXyC_ul()[0]+0.5*xScale);
            //yPoints[3] = (int)(xyC_ul[1]+yScale);
            //xPoints[4] = (int)(xyC[0]-0.5*xScale);
            getYPoints()[4] = (int)(getXyC()[1]+yScale);
            //xPoints[5] = (int)(xyC[0]+0.5*xScale);
            //yPoints[5] = (int)(xyC[1]+yScale);

            // compute the width of the object -- we tried computing the EXACT width each time, but
            // it results in weird-shaped circles etc, so instead we precomputed a standard width
            // and height, and just compute the x values here.
            newinfo.draw.x = getXPoints()[3];
            newinfo.draw.y = getYPoints()[1];
            
            // adjust drawX and drawY to center
            newinfo.draw.x +=(getXPoints()[0]-getXPoints()[3]) / 2.0;
            newinfo.draw.y += (getYPoints()[4]-getYPoints()[1]) / 2.0;
        
            locationToPass.x = x;
            locationToPass.y = y;
            
            if (graphics == null)
                {
                if (portrayal.hitObject(obj, newinfo))
                    putInHere.add(getWrapper(obj, new Int2D(x,y)));
                }
            else
                {
                // MacOS X 10.3 Panther has a bug which resets the clip, YUCK
                //                    graphics.setClip(clip);
                portrayal.draw(obj, graphics, newinfo);
                }
            }
    }
	
	
}
