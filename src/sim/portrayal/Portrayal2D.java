package sim.portrayal;
import java.awt.*;

/**
   The basic 2D portrayal interface.  It adds the draw method in the 2D context.
*/

public interface Portrayal2D extends Portrayal
    {
    /** Draw a portrayed object centered at the origin in info, and
        with the given scaling factors.  draw(...) will not be called
        until portray(obj) has been called on a Portrayal2D at least
        once.  But you should have some default null drawing capability
        just in case. */
    public void draw(Object object, Graphics2D graphics, DrawInfo2D info);
    }
