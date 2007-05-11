package sim.portrayal.simple;
import sim.portrayal.*;
import java.awt.*;
import java.awt.geom.*;

/**
   A simple portrayal for 2D visualization of hexagons. It extends the SimplePortrayal2D and
   it manages the drawing and hit-testing for hexagonal shapes. If the DrawInfo2D parameter
   received by draw and hitObject functions is an instance of HexaDrawInfo2D, better information
   is extracted and used to make everthing look better. Otherwise, hexagons may be created from
   information stored in simple DrawInfo2D objects, but overlapping or extra empty spaces may be
   observed (especially when increasing the scale).
*/

public class HexagonalPortrayal2D extends SimplePortrayal2D
    {
    public Paint paint;

    private int[] xPoints = new int[6];
    private int[] yPoints = new int[6];

    public boolean drawFrame = true;

    public HexagonalPortrayal2D() { this(Color.gray,false); }
    public HexagonalPortrayal2D(Paint paint)  { this(paint,false); }
    public HexagonalPortrayal2D(boolean drawFrame) { this(Color.gray,drawFrame); }
    public HexagonalPortrayal2D(Paint paint, boolean drawFrame)  { this.paint = paint; this.drawFrame = drawFrame; }
        
    // assumes the graphics already has its color set
    public void draw(Object object, Graphics2D graphics, DrawInfo2D info)
        {
        graphics.setPaint(paint);
        GeneralPath generalPath = createGeneralPath( info );
        graphics.fill(generalPath);
        if( drawFrame )
            {
            graphics.setColor( Color.black );
            graphics.draw( generalPath);
            }
        }

    protected GeneralPath generalPath = new GeneralPath();

    /** Creates a general path for the bounding hexagon */
    GeneralPath createGeneralPath( DrawInfo2D info )
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
            xPoints[0] = (int)(info.draw.x+info.draw.width/2.0);
            yPoints[0] = (int)(info.draw.y);
            xPoints[1] = (int)(info.draw.x+info.draw.width/4.0);
            yPoints[1] = (int)(info.draw.y-info.draw.height/2.0);
            xPoints[2] = (int)(info.draw.x-info.draw.width/4.0);
            yPoints[2] = (int)(info.draw.y-info.draw.height/2.0);
            xPoints[3] = (int)(info.draw.x-info.draw.width/2.0);
            yPoints[3] = (int)(info.draw.y);
            xPoints[4] = (int)(info.draw.x-info.draw.width/4.0);
            yPoints[4] = (int)(info.draw.y+info.draw.height/2.0);
            xPoints[5] = (int)(info.draw.x+info.draw.width/4.0);
            yPoints[5] = (int)(info.draw.y+info.draw.height/2.0);
            generalPath.moveTo( xPoints[0], yPoints[0] );
            for( int i = 1 ; i < 6 ; i++ )
                generalPath.lineTo( xPoints[i], yPoints[i] );
            generalPath.closePath();
            }
        return generalPath;
        }

    /** If drawing area intersects selected area, add last portrayed object to the bag */
    public boolean hitObject(Object object, DrawInfo2D range)
        {
        GeneralPath generalPath = createGeneralPath( range );
        Area area = new Area( generalPath );
        return ( area.intersects( range.clip.x, range.clip.y, range.clip.width, range.clip.height ) );
        }
    }
