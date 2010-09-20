package episiminterfaces;

import java.awt.Paint;
import java.awt.geom.Rectangle2D;

import sim.display.Display2D.InnerDisplay2D;
import sim.portrayal.FieldPortrayal2D;




public interface SimulationDisplay {
 void quit();
 void attach(FieldPortrayal2D portrayal, String name, Rectangle2D.Double bounds, boolean visible);
 void attach(FieldPortrayal2D portrayal, String name );
 boolean isPortrayalVisible(String name);
 void reset();
 void repaint();
 void setBackdrop(Paint c);
  
}
