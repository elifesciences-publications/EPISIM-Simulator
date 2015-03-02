package sim.app.episim.visualization.twodim;




import sim.app.episim.SimStateServer;
import sim.app.episim.gui.EpisimGUIState;
import sim.app.episim.gui.EpisimGUIState.SimulationDisplayProperties;
import sim.app.episim.visualization.EpisimDrawInfo;
import sim.portrayal.*;

import java.awt.*;
import java.awt.geom.*;

import episiminterfaces.EpisimPortrayal;



public class LatticePortrayal2D extends AbstractSpatialityScalePortrayal2D implements EpisimPortrayal{
	
		 private final String NAME = "Grid";	
	    
		 private static final float DOT = 2;
		 private static final float SPACE = 2;
		 
	    private double gridResolution = 5.0;
	    private double gridResolutionFact = 1;
	    public LatticePortrayal2D() {
	   	 super();
	   	 	   	 
	    }
	    
	        
	    Rectangle2D.Double oldDraw = null;  
	    
	    // assumes the graphics already has its color set
	public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
		if(getFirstInfo() == null){
			setFirstInfo(info); // is assigned during the first call of this method
			
		}
		setLastActualInfo(info);
		
		
		if(getLastActualInfo() != null && getLastActualInfo().clip != null){ 			
			drawGrid(graphics, info);			
		}
	}
	
	public String getPortrayalName() {
	   return NAME;
   }
	    
	private void drawGrid(Graphics2D graphics, DrawInfo2D info){
	   	 
		graphics.setColor(new Color(192, 192, 192, 150));
		 float[] dash = new float[]{ DOT, SPACE };
 		
		graphics.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND,0,dash,0));
		
			 
		double minX = getMinX(info);
		double maxX = getMaxX(info);
		double minY = getMinY(info);
		double maxY = getMaxY(info);			
		SimulationDisplayProperties props = guiState.getSimulationDisplayProperties(new EpisimDrawInfo<DrawInfo2D>(info));		
		double spaceBetweenSmallLinesX = props.displayScaleX*getResolutionInMikron();	
		double spaceBetweenSmallLinesY = props.displayScaleY*getResolutionInMikron();
		double factX =1, factY = 1;
		if(spaceBetweenSmallLinesX < MIN_PIXEL_RESOLUTION){					
			factX = MIN_PIXEL_RESOLUTION / spaceBetweenSmallLinesX;
			spaceBetweenSmallLinesX*=factX;
			spaceBetweenSmallLinesY*=factX;
		}
		if(spaceBetweenSmallLinesY < MIN_PIXEL_RESOLUTION){					
			factY = MIN_PIXEL_RESOLUTION / spaceBetweenSmallLinesY;
			spaceBetweenSmallLinesX*=factY;
			spaceBetweenSmallLinesY*=factY;
		}
		gridResolutionFact =(factX*factY);
		
		if(((getResolutionInMikron()*gridResolutionFact) % 5) != 0){
			double modul = (getResolutionInMikron()*gridResolutionFact)%5;
			modul = 5 - modul;
			modul /= getResolutionInMikron();
			gridResolutionFact+=modul;
		}
	
		spaceBetweenSmallLinesX = props.displayScaleX*getResolutionInMikron()*gridResolutionFact;
		spaceBetweenSmallLinesY = props.displayScaleY*getResolutionInMikron()*gridResolutionFact;
				
				
		graphics.setFont(new Font("Arial", Font.PLAIN, 10));
		
		//draw vertical lines
		for(double i = (minX+(spaceBetweenSmallLinesX*gridResolution)); i <= maxX; i += (spaceBetweenSmallLinesX*gridResolution))
			graphics.draw(new Line2D.Double(i, minY, i, maxY));
	
		//draw horizontal lines
		for(double i = (maxY-(spaceBetweenSmallLinesY*gridResolution)); i >= minY; i -= (spaceBetweenSmallLinesY*gridResolution))
			graphics.draw(new Line2D.Double(minX, i, maxX ,i));		
	}
	
	public Rectangle2D.Double getViewPortRectangle() {
 		EpisimGUIState guiState = SimStateServer.getInstance().getEpisimGUIState();	   
 	   if(guiState != null)return new Rectangle2D.Double(0,0,guiState.EPIDISPLAYWIDTH+(guiState.DISPLAY_BORDER_LEFT+guiState.DISPLAY_BORDER_RIGHT), guiState.EPIDISPLAYHEIGHT+(guiState.DISPLAY_BORDER_TOP+guiState.DISPLAY_BORDER_BOTTOM));
 	   else return new Rectangle2D.Double(0,0,0, 0);
    }
}