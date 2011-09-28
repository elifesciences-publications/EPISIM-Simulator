package sim.app.episim.model.visualization;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;

import episiminterfaces.EpisimCellBehavioralModel;

import sim.app.episim.UniversalCell;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.simple.HexagonalPortrayal2DHack;


public class HexagonalCellPortrayal2D extends HexagonalPortrayal2DHack {
	
	 private static Color standardCellColor = new Color(255,210,210);	
	
	 public HexagonalCellPortrayal2D() { this(standardCellColor,1.0,true); }
    public HexagonalCellPortrayal2D(Paint paint)  { this(paint,1.0,true); }
    public HexagonalCellPortrayal2D(double scale) { this(standardCellColor,scale,true); }
    public HexagonalCellPortrayal2D(boolean filled) { this(standardCellColor,1.0,filled); }
    public HexagonalCellPortrayal2D(Paint paint, double scale)  { this(paint,scale,true); }
    public HexagonalCellPortrayal2D(Paint paint, boolean filled)  { this(paint,1.0,filled); }
    public HexagonalCellPortrayal2D(double scale, boolean filled)  { this(standardCellColor,scale,filled); }
    public HexagonalCellPortrayal2D(Paint paint, double scale, boolean filled)
    {
   	 super(paint, scale, filled);
    }
    
    public void draw(Object object, Graphics2D graphics, DrawInfo2D info)
    {
   	 if(object instanceof UniversalCell){   	 
   		 UniversalCell cell = (UniversalCell) object;
   		 EpisimCellBehavioralModel cbModel = cell.getEpisimCellBehavioralModelObject();
	   	 graphics.setPaint(new Color(cbModel.getColorR(), cbModel.getColorG(), cbModel.getColorB()));
	   
	       final double width = info.draw.width*scale;
	       final double height = info.draw.height*scale;
	       filled = true; 
	       if (getBufferedShape() == null || width != getBufferedWidth() || height != getBufferedHeight())
	       {
	          setBufferedWidth(width);
	          setBufferedHeight(height);
	          getTransform().setToScale(getBufferedWidth(), getBufferedHeight());
	          setBufferedShape(getTransform().createTransformedShape(shape));
	       }
	
	       // we are doing a simple draw, so we ignore the info.clip
	
	       // draw centered on the origin
	       getTransform().setToTranslation(info.draw.x,info.draw.y);
	       if (filled)
	       {
	            graphics.fill(getTransform().createTransformedShape(getBufferedShape()));
	       }
	       graphics.setPaint(new Color(150, 0, 0));
	       graphics.setStroke(stroke);
	       graphics.draw(getTransform().createTransformedShape(getBufferedShape()));
	     }       
    }
}
