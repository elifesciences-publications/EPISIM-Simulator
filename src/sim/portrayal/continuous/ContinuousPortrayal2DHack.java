package sim.portrayal.continuous;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import sim.field.continuous.Continuous2D;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.Portrayal;
import sim.portrayal.SimplePortrayal2D;
import sim.util.Bag;
import sim.util.Double2D;


public class ContinuousPortrayal2DHack extends ContinuousPortrayal2D {
	
	 protected void hitOrDraw(Graphics2D graphics, DrawInfo2D info, Bag putInHere)
    {
    final Continuous2D field = (Continuous2D)this.field;
    if (field==null) return;
            
    boolean objectSelected = !selectedWrappers.isEmpty();
            
//    Rectangle2D.Double cliprect = (Rectangle2D.Double)(info.draw.createIntersection(info.clip));

    final double xScale = info.draw.width / field.width;
    final double yScale = info.draw.height / field.height;
    final int startx = (int)Math.floor((info.clip.x - info.draw.x) / xScale);
    final int starty = (int)Math.floor((info.clip.y - info.draw.y) / yScale);
    int endx = /*startx +*/ (int)Math.floor((info.clip.x - info.draw.x + info.clip.width) / xScale) + /*2*/ 1;  // with rounding, width be as much as 1 off
    int endy = /*starty +*/ (int)Math.floor((info.clip.y - info.draw.y + info.clip.height) / yScale) + /*2*/ 1;  // with rounding, height be as much as 1 off

//    final Rectangle clip = (graphics==null ? null : graphics.getClipBounds());

    DrawInfo2D newinfo = new DrawInfo2D(info.gui, info.fieldPortrayal, new Rectangle2D.Double(0,0, xScale, yScale),
        info.clip);  // we don't do further clipping 
    newinfo.fieldPortrayal = this;

    // hit/draw the objects one by one -- perhaps for large numbers of objects it would
    // be smarter to grab the objects out of the buckets that specifically are inside
    // our range...
    Bag objects = field.getAllObjects();
    final double discretizationOverlap = field.discretization;
    for(int x=0;x<objects.numObjs;x++)
        {
        Object object = (objects.objs[x]);
        Double2D objectLoc = field.getObjectLocation(object);
                    
        if (displayingToroidally)
            objectLoc = new Double2D(field.tx(objectLoc.x), field.tx(objectLoc.y));
                                            
        for(int i = 0; i < toroidalX.length; i++) 
            {
            Double2D loc = null;
            if (i == 0)
                loc = objectLoc;
            else if (displayingToroidally)  // and i > 0
                loc = new Double2D(objectLoc.x + field.width * toroidalX[i],
                    objectLoc.y + field.height * toroidalY[i]);
            else
                break; // no toroidal function
           
         
                Portrayal p = getPortrayalForObject(object);
                if (!(p instanceof SimplePortrayal2D))
                    throw new RuntimeException("Unexpected Portrayal " + p + " for object " + 
                        objects.objs[x] + " -- expected a SimplePortrayal2D");
                SimplePortrayal2D portrayal = (SimplePortrayal2D) p;
                                    
                newinfo.draw.x = (info.draw.x + (xScale) * loc.x);
                newinfo.draw.y = (info.draw.y + (yScale) * loc.y);

                newinfo.location = loc;

                final Object portrayedObject = object;
                if (graphics == null)
                    {
                    if (portrayal.hitObject(portrayedObject, newinfo))
                        putInHere.add(getWrapper(portrayedObject));
                    }
                else
                    {
                    // MacOS X 10.3 Panther has a bug which resets the clip, YUCK
                    //                    graphics.setClip(clip);
                    newinfo.selected = (objectSelected &&  // there's something there
                        selectedWrappers.get(portrayedObject) != null); 
                    /* {
                       LocationWrapper wrapper = (LocationWrapper)(selectedWrappers.get(portrayedObject));
                       portrayal.setSelected(wrapper,true);
                       portrayal.draw(portrayedObject, graphics, newinfo);
                       portrayal.setSelected(wrapper,false);
                       }
                       else */ portrayal.draw(portrayedObject, graphics, newinfo);
                    }
                
                }
            }
        
                    
    // finally draw the frame
    if (frame != null && graphics != null)
        {
        graphics.setPaint(frame);
        Rectangle2D rect = new Rectangle2D.Double(info.draw.x - 1, info.draw.y - 1, info.draw.width + 1, info.draw.height + 1);
        graphics.draw(rect);
        }
    }

}
