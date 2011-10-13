package sim.app.episim.visualization;

import sim.SimStateServer;
import sim.app.episim.gui.EpisimGUIState;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.tissue.TissueController;
import sim.app.episim.util.Scale;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.SimplePortrayal2D;
import sim.portrayal.continuous.ContinuousPortrayal2D;


public abstract class AbstractSpatialityScalePortrayal2D extends ContinuousPortrayal2D {
	
	private double width;
   private double height;
   protected final double INITIALWIDTH;
   protected final double INITIALHEIGHT;
  
   private DrawInfo2D lastActualInfo;
   private DrawInfo2D firstInfo;
   	   
   private int border;
   private int specificOffset;
   protected final int OFFSET = 0; //distance ruler <->tissue
   
   private double implicitScale;
   
   private double resolution = 5;
	
	 public AbstractSpatialityScalePortrayal2D() {
   	 
		 EpisimGUIState guiState = SimStateServer.getInstance().getEpisimGUIState();
		 
		 if(guiState != null){
			 this.width =  guiState.EPIDISPLAYWIDTH + (2*guiState.DISPLAYBORDER);
	   	 this.height = guiState.EPIDISPLAYHEIGHT + (2*guiState.DISPLAYBORDER);
	   	 this.INITIALWIDTH = width;
	   	 this.INITIALHEIGHT = height;
	   	
	   	 this.border = guiState.DISPLAYBORDER;
	   	 this.implicitScale = guiState.INITIALZOOMFACTOR;
		 }else{
			 this.INITIALWIDTH=0;
			 this.INITIALHEIGHT=0;
		 }
   	 
   	 this.specificOffset = (border - OFFSET) < 0 ? 0 : (border - OFFSET);
   	 
   	 double heightResolution = Math.round(ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().getHeightInMikron() * 0.02);
	  	 double widthResolution = Math.round(ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().getWidthInMikron() * 0.02);
	  	 this.resolution = heightResolution > widthResolution ? heightResolution : widthResolution;
   	 
    }
	 
	 protected double getMinX(DrawInfo2D info){
		 return lastActualInfo.clip.getMinX() -getDeltaX() + (specificOffset*getScaleFactorOfTheDisplay());
	 }
	 
	 protected double getMaxX(DrawInfo2D info){
		 return lastActualInfo.clip.getMinX()+width-getDeltaX()- (specificOffset*getScaleFactorOfTheDisplay());
	 }
	 protected double getMinY(DrawInfo2D info){
		 return lastActualInfo.clip.getMinY()-getDeltaY()+ (specificOffset*getScaleFactorOfTheDisplay());
	 }
	 protected double getMaxY(DrawInfo2D info){
	  return lastActualInfo.clip.getMinY()+height-getDeltaY()-(specificOffset*getScaleFactorOfTheDisplay());
	}
	protected double getDeltaX(){
   	 if((lastActualInfo.clip.width+1)< width){
   		 return lastActualInfo.clip.getMinX();   		 
   	 }
   	 else return 0;
   }
    
	protected double getDeltaY(){   	 
	    if((lastActualInfo.clip.height+1) < height){
	   	 return lastActualInfo.clip.getMinY();
	    }
	    else return 0;
   }
   protected double getScaledNumberOfPixelPerMicrometer(DrawInfo2D info){
		return TissueController.getInstance().getTissueBorder().getNumberOfPixelsPerMicrometer()*implicitScale*getScaleFactorOfTheDisplay();
	}

	protected double getScaleFactorOfTheDisplay(){
		return Scale.displayScale;
	}	 	
   protected double getWidth() {   
   	return width;
   }
	
   protected void setWidth(double width) {
   
   	this.width = width;
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

	
   protected int getBorder() {
   
   	return border;
   }

	
   protected void setBorder(int border) {
   
   	this.border = border;
   }

	
   protected double getImplicitScale() {
   
   	return implicitScale;
   }

	
   protected void setImplicitScale(double implicitScale) {
   
   	this.implicitScale = implicitScale;
   }

	
   protected double getResolutionInMikron(){
   	
   	return this.resolution;
   }
   
   protected void setResolution(double resolution) {
   
   	this.resolution = resolution;
   }


	
   protected int getSpecificOffset() {
   
   	return specificOffset;
   }


	
   protected void setSpecificOffset(int specificOffset) {
   
   	this.specificOffset = specificOffset;
   }


	
   protected double getHeight() {
   
   	return height;
   }


	
   protected void setHeight(double height) {
   
   	this.height = height;
   }

}
