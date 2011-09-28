package sim.app.episim.model.visualization;

import java.awt.Color;
import java.awt.Paint;

import sim.app.episim.model.controller.CellBehavioralModelController;
import sim.app.episim.model.controller.ModelController;
import sim.field.grid.Grid2D;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.Portrayal;
import sim.portrayal.grid.HexaObjectGridPortrayal2D;
import sim.portrayal.simple.HexagonalPortrayal2D;
import sim.util.Double2D;


public class HexagonalCellGridPortrayal2D extends HexaObjectGridPortrayal2D {
	private HexagonalCellPortrayal2D defaultHexPortrayal;
	
	 private Paint paint;  
       
    
    private double implicitScale;
    
    private static long actSimStep;
    
    private final double INITIALWIDTH;
    private final double INITIALHEIGHT;
    
    private int border = 0;
	
	
	
	public HexagonalCellGridPortrayal2D(){
		this(Color.BLACK, 1, 0, 0, 0); 
	}
	
	 public HexagonalCellGridPortrayal2D(double implicitScale) {   	 
   	 this(Color.BLACK, implicitScale, 0, 0, 0);   	 
    }  
      
    public HexagonalCellGridPortrayal2D(Paint paint, double implicitScale, double width, double height, int border){ 
   	super();   	
   	this.paint = paint;   
   	this.implicitScale = implicitScale;
   	this.INITIALWIDTH = width;
	  	this.INITIALHEIGHT = height;
	  	this.border = border;
	  	defaultHexPortrayal = new HexagonalCellPortrayal2D(false);
    }    
	
	
	 public Portrayal getDefaultPortrayal()
    {
		 return defaultHexPortrayal;
    }

}
