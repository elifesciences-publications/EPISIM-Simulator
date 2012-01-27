package episiminterfaces;

import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import sim.portrayal.Portrayal;

public interface EpisimSimulationDisplay {
 void quit();
 void attach(Portrayal portrayal, String name, Rectangle2D.Double bounds, boolean visible);
 void attach(Portrayal portrayal, String name );
 boolean isPortrayalVisible(String name);
 boolean isValid();
 void reset();
 void repaint();
 void setBackdrop(Paint c);
 double getDisplayScale();
  
}
