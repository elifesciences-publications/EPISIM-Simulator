package sim.app.episim.visualization;

import sim.SimStateServer;
import sim.app.episim.gui.EpisimGUIState;
import sim.app.episim.gui.EpisimGUIState.SimulationDisplayProperties;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.visualization.EpisimDrawInfo;
import sim.app.episim.tissue.TissueController;
import sim.app.episim.util.Scale;
import sim.field.continuous.Continuous2D;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.SimplePortrayal2D;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.util.Double2D;


public abstract class AbstractSpatialityScalePortrayal2D extends ContinuousPortrayal2D {
	
	private double width;
   private double height;
   
  
   private DrawInfo2D lastActualInfo;
   private DrawInfo2D firstInfo;
   	   
  
     
   
   
   private double resolution = 10;
	
   protected double MIN_PIXEL_RESOLUTION = 10;
   
   protected EpisimGUIState guiState;
  
	 public AbstractSpatialityScalePortrayal2D() {
   	 
		 guiState = SimStateServer.getInstance().getEpisimGUIState();
		 
		 if(guiState != null){
			 this.width =  guiState.EPIDISPLAYWIDTH + guiState.DISPLAY_BORDER_LEFT+guiState.DISPLAY_BORDER_RIGHT;
	   	 this.height = guiState.EPIDISPLAYHEIGHT + guiState.DISPLAY_BORDER_TOP+guiState.DISPLAY_BORDER_BOTTOM;
	    	 
		 }
		 
		
	
	 
	 
   	    	 
   	 double heightResolution = Math.round(ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().getHeightInMikron() * 0.02);
	  	 double widthResolution = Math.round(ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().getWidthInMikron() * 0.02);
	  	 this.resolution = heightResolution > widthResolution ? heightResolution : widthResolution;
	  	 
	  	 Continuous2D field = new Continuous2D(TissueController.getInstance().getTissueBorder().getWidthInMikron() + 2, 
					TissueController.getInstance().getTissueBorder().getWidthInMikron() + 2, 
					TissueController.getInstance().getTissueBorder().getHeightInMikron());
	  	 
	  	 field.setObjectLocation("DummyObject", new Double2D(50, 50));
	  	 this.setField(field);
   	 
    }
	 
	 protected double getMinX(DrawInfo2D info){
		 return guiState.getSimulationDisplayProperties(new EpisimDrawInfo<DrawInfo2D>(info)).offsetX;
	 }
	 
	 protected double getMaxX(DrawInfo2D info){
		 return guiState.getSimulationDisplayProperties(new EpisimDrawInfo<DrawInfo2D>(info)).offsetX+getWidth()
		  																							-(guiState.DISPLAY_BORDER_RIGHT*guiState.getDisplay().getDisplayScale())
		  																							-(guiState.DISPLAY_BORDER_LEFT*guiState.getDisplay().getDisplayScale());
	 }
	 protected double getMinY(DrawInfo2D info){
		 return guiState.getSimulationDisplayProperties(new EpisimDrawInfo<DrawInfo2D>(info)).offsetY;
	 }
	 protected double getMaxY(DrawInfo2D info){
	  return guiState.getSimulationDisplayProperties(new EpisimDrawInfo<DrawInfo2D>(info)).offsetY+getHeight()
	  																									-(guiState.DISPLAY_BORDER_BOTTOM*guiState.getDisplay().getDisplayScale())
	  																									-(guiState.DISPLAY_BORDER_TOP*guiState.getDisplay().getDisplayScale());
	}
	  
   protected double getWidth() {   
   	return width*guiState.getDisplay().getDisplayScale();
   }
	
   

	
   protected DrawInfo2D getLastActualInfo() {
   
   	return lastActualInfo;
   }

	
   protected void setLastActualInfo(DrawInfo2D lastActualInfo) {
   
   	this.lastActualInfo = lastActualInfo;
   }

	
   protected DrawInfo2D getFirstInfo() {
   
   	return firstInfo;
   }

	
   protected void setFirstInfo(DrawInfo2D firstInfo) {
   
   	this.firstInfo = firstInfo;
   }
	
   
	
   protected double getResolutionInMikron(){
   	
   	return this.resolution;
   }
   
   protected void setResolution(double resolution) {
   
   	this.resolution = resolution;
   }
	
   protected double getHeight() {
   
   	return height*guiState.getDisplay().getDisplayScale();
   }
   
  

}
