package sim.portrayal.continuous;
import sim.portrayal.*;
import sim.portrayal.simple.*;
import sim.field.continuous.*;
import sim.util.*;
import java.awt.*;
import java.awt.geom.*;

/**
   Portrays Continuous2D fields.  When asked to portray objects, this field computes the buckets
   covered by the requested region, then includes an additional boundary of two buckets in each
   direction just in case objects leak over the boundary region.
*/

public class ContinuousPortrayal2D extends FieldPortrayal2D
    {
    // a grey oval.  You should provide your own protrayals...
    SimplePortrayal2D defaultPortrayal = new OvalPortrayal2D();

    public void setField(Object field)
        {
        dirtyField = true;
        if (field instanceof Continuous2D) this.field = field;
        else throw new RuntimeException("Invalid field for ContinuousPortrayal2D: " + field);
        }
        
    public Portrayal getDefaultPortrayal()
        {
        return defaultPortrayal;
        }
        
    protected void hitOrDraw(Graphics2D graphics, DrawInfo2D info, Bag putInHere)
        {
        final Continuous2D field = (Continuous2D)this.field;
        if (field==null) return;

        Rectangle2D.Double cliprect = (Rectangle2D.Double)(info.draw.createIntersection(info.clip));

        final double xScale = info.draw.width / field.width;
        final double yScale = info.draw.height / field.height;
        final int startx = (int)((info.clip.x - info.draw.x) / xScale);
        final int starty = (int)((info.clip.y - info.draw.y) / yScale);
        int endx = /*startx +*/ (int)((info.clip.x - info.draw.x + info.clip.width) / xScale) + /*2*/ 1;  // with rounding, width be as much as 1 off
        int endy = /*starty +*/ (int)((info.clip.y - info.draw.y + info.clip.height) / yScale) + /*2*/ 1;  // with rounding, height be as much as 1 off

        final Rectangle clip = (graphics==null ? null : graphics.getClipBounds());

        DrawInfo2D newinfo = new DrawInfo2D(new Rectangle2D.Double(0,0, xScale, yScale),
                                            info.clip);  // we don't do further clipping 

        // hit/draw the objects one by one -- perhaps for large numbers of objects it would
        // be smarter to grab the objects out of the buckets that specifically are inside
        // our range...
        Bag objects = field.getAllObjects();
        final double discretizationOverlap = field.discretization;
        for(int x=0;x<objects.numObjs;x++)
            {
            Double2D loc = field.getObjectLocation(objects.objs[x]);

            // here we only hit/draw the object if it's within our range.  However objects
            // might leak over to other places, so I dunno...  I give them the benefit
            // of the doubt that they might be three times the size they oughta be, hence the -2 and +2's
            if (loc.x >= startx - discretizationOverlap && loc.x < endx + discretizationOverlap &&
                loc.y >= starty - discretizationOverlap && loc.y < endy + discretizationOverlap)
                {
                Portrayal p = getPortrayalForObject(objects.objs[x]);
                if (!(p instanceof SimplePortrayal2D))
                    throw new RuntimeException("Unexpected Portrayal " + p + " for object " + 
                                               objects.objs[x] + " -- expected a SimplePortrayal2D");
                SimplePortrayal2D portrayal = (SimplePortrayal2D) p;
                
                newinfo.draw.x = (info.draw.x + (xScale) * loc.x);
                newinfo.draw.y = (info.draw.y + (yScale) * loc.y);

                final Object portrayedObject = objects.objs[x];
                if (graphics == null)
                    {
                    if (portrayal.hitObject(portrayedObject, newinfo))
                        putInHere.add(getWrapper(portrayedObject));
                    }
                else
                    {
                    // MacOS X 10.3 Panther has a bug which resets the clip, YUCK
                    //                    graphics.setClip(clip);
                    portrayal.draw(portrayedObject, graphics, newinfo);
                    }
                }
            }
        }

    public LocationWrapper getWrapper(final Object obj)
        {
        final Continuous2D field = (Continuous2D)this.field;
        return new LocationWrapper( obj, null , this)  // don't care about location
            {
            public Object getLocation()
                {
                if (field==null) return null;
                else return field.getObjectLocation(object);
                }
                
            public String getLocationName()
                {
                Object loc = getLocation();
                if (loc == null) return "Gone";
                return ((Double2D)loc).toCoordinates();
                }
            };
        }
    }
    
    
