package sim.app.episim.model.visualization;

import java.awt.Color;
import java.awt.geom.Rectangle2D;

import javax.media.j3d.TransformGroup;

import episiminterfaces.EpisimPortrayal;
import sim.app.episim.AbstractCell;
import sim.app.episim.model.biomechanics.CellBoundaries;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters.MiscalleneousGlobalParameters3D;
import sim.app.episim.tissue.TissueController;
import sim.app.episim.util.GenericBag;
import sim.display3d.Display3DHack.ModelSceneCrossSectionMode;
import sim.display3d.Display3DHack;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.DoubleGrid3D;
import sim.field.grid.IntGrid2D;
import sim.portrayal3d.grid.ValueGrid2DPortrayal3DHack;
import sim.util.gui.ColorMap;
import sim.util.gui.SimpleColorMap;


public class TissueCrossSectionPortrayal3D extends ValueGrid2DPortrayal3DHack implements EpisimPortrayal{
	
	private static final String NAME = "Tissue Cross Section";
	
	
	
	private ModelSceneCrossSectionMode lastSelectedCrossSectionMode = ModelSceneCrossSectionMode.X_Y_PLANE;
	private double lastCrossSectionTranslationCoordinate = 0;
	
	private Color standardColor = Color.BLACK.brighter();
	private boolean optimizedGraphicsActivated =false;
	public TissueCrossSectionPortrayal3D(){
		super(NAME, 1.0f, 1.0f);
		MiscalleneousGlobalParameters param = MiscalleneousGlobalParameters.getInstance();
      
      if(param instanceof MiscalleneousGlobalParameters3D && ((MiscalleneousGlobalParameters3D)param).getOptimizedGraphics()){	
			optimizedGraphicsActivated = true;
			standardColor = Color.BLACK;
			
		}
		setField(createInt2DField());
		this.setMap(buildColorMap());
		doCrossSectionPlaneTransformation();
	}
	
	
	public Object getField()
   {
		
		setField(createInt2DField());	
		return this.field;
   }
	
	
	
	private IntGrid2D createInt2DField(){
		
		double actCrossSectionTranslationCoordinate = 0;
		ModelSceneCrossSectionMode actCrossSectionMode = ModelSceneCrossSectionMode.X_Y_PLANE;
		if(((Display3DHack)getCurrentDisplay()) != null){
			actCrossSectionMode = ((Display3DHack)getCurrentDisplay()).getModelSceneCrossSectionMode();
			actCrossSectionTranslationCoordinate = ((Display3DHack)getCurrentDisplay()).getActModelSceneCrossSectionCoordinate();
		}
		
		double positionInMikrometer = actCrossSectionTranslationCoordinate;
		double factorXY = TissueController.getInstance().getTissueBorder().get3DTissueCrosssectionXYResolutionFactor();
		double factorXZ = TissueController.getInstance().getTissueBorder().get3DTissueCrosssectionXZResolutionFactor();
		double factorYZ = TissueController.getInstance().getTissueBorder().get3DTissueCrosssectionYZResolutionFactor();
		if(optimizedGraphicsActivated){
			factorXY*=2;
			factorXZ*=2;
			factorYZ*=2;
		}
		double height = TissueController.getInstance().getTissueBorder().getHeightInMikron(); 
		double width = TissueController.getInstance().getTissueBorder().getWidthInMikron(); 
		double length = TissueController.getInstance().getTissueBorder().getLengthInMikron(); 
		IntGrid2D field2D = null;
		GenericBag<AbstractCell> allCells = TissueController.getInstance().getActEpidermalTissue().getAllCells();
		if(actCrossSectionMode == ModelSceneCrossSectionMode.X_Y_PLANE){
			this.renewTilePortrayal((float)(1f/factorXY), (float)(1f/factorXY));
			field2D = new IntGrid2D(Math.round((float)(width*factorXY)), Math.round((float)(height*factorXY)));
			initializeFieldWithColor(field2D);
			for(int i = 0; i < allCells.size(); i++){
				CellBoundaries boundaries =allCells.get(i).getEpisimBioMechanicalModelObject().getCellBoundariesInMikron(0);
				if(boundaries!= null) boundaries.getXYCrosssection(positionInMikrometer, field2D, allCells.get(i).getCellColoring());
			}
			addTransparentColor(field2D);
		}
		if(actCrossSectionMode == ModelSceneCrossSectionMode.X_Z_PLANE){
			this.renewTilePortrayal((float)(1f/factorXZ), (float)(1f/factorXZ));
			field2D = new IntGrid2D(Math.round((float)(width*factorXZ)), Math.round((float)(length*factorXZ)));
			initializeFieldWithColor(field2D);
			for(int i = 0; i < allCells.size(); i++){
				CellBoundaries boundaries =allCells.get(i).getEpisimBioMechanicalModelObject().getCellBoundariesInMikron(0);
				if(boundaries!= null) boundaries.getXZCrosssection(positionInMikrometer, field2D, allCells.get(i).getCellColoring());
			}
			addTransparentColor(field2D);
		}
		if(actCrossSectionMode == ModelSceneCrossSectionMode.Y_Z_PLANE){
			this.renewTilePortrayal((float)(1f/factorYZ), (float)(1f/factorYZ));
			field2D = new IntGrid2D(Math.round((float)(length*factorYZ)), Math.round((float)(height*factorYZ)));
			initializeFieldWithColor(field2D);
			
			for(int i = 0; i < allCells.size(); i++){
				CellBoundaries boundaries =allCells.get(i).getEpisimBioMechanicalModelObject().getCellBoundariesInMikron(0);
				if(boundaries!= null) boundaries.getYZCrosssection(positionInMikrometer, field2D, allCells.get(i).getCellColoring());
			}
			addTransparentColor(field2D);
		}
		if(actCrossSectionMode == ModelSceneCrossSectionMode.DISABLED){
			field2D = new IntGrid2D(Math.round((float)(width*factorXY)), Math.round((float)(height*factorXY)));
			addTransparentColorForWholeField(field2D);
		}
		
		return field2D;
	}
	
	private void addTransparentColor(IntGrid2D field){
		boolean cellColoringFound = false;
		for(int y = 0; y < field.getHeight() && !cellColoringFound; y++){
			for(int x = 0; !cellColoringFound && x < field.getWidth(); x++){
				cellColoringFound = field.field[x][y] != standardColor.getRGB();
			}
			for(int x = 0; !cellColoringFound && x < field.getWidth(); x++){
				field.field[x][y]= Color.BLACK.getRGB();
			}
		}
		cellColoringFound = false;
		for(int y = field.getHeight()-1; y >=0 && !cellColoringFound; y--){
			for(int x = 0; !cellColoringFound && x < field.getWidth(); x++){
				cellColoringFound = field.field[x][y] != standardColor.getRGB();
			}
			for(int x = 0; !cellColoringFound && x < field.getWidth(); x++){
				field.field[x][y]= Color.BLACK.getRGB();
			}
		}
	}
	private void addTransparentColorForWholeField(IntGrid2D field){
		for(int y = 0; y < field.getHeight(); y++){
			for(int x = 0; x < field.getWidth(); x++){
				field.field[x][y]= Color.BLACK.getRGB();
			}
		}
	}
	
	private void initializeFieldWithColor(IntGrid2D field){
		for(int y = 0; y < field.getHeight(); y++){
			for(int x = 0; x < field.getWidth(); x++){
				field.field[x][y]= standardColor.getRGB();
			}
		}
	}
	
	public TransformGroup createModel()
   {
		
		 IntGrid2D field = (IntGrid2D) getField();
		 setField(field);		 
		 this.setMap(buildColorMap());
		 TransformGroup modelTG = super.createModel();
		 doCrossSectionPlaneTransformation();
		 this.setTransparency(((Display3DHack)getCurrentDisplay()).getModelSceneOpacity());
		 return modelTG;
    }
	 
	 public void updateModel(TransformGroup modelTG)
    {
		
		 IntGrid2D field = (IntGrid2D) getField();
		 setField(field);
		
		 
		 this.setMap(buildColorMap());
		 super.updateModel(modelTG);
		 doCrossSectionPlaneTransformation();
    }
	 
	 private ColorMap buildColorMap(){   	
	   	
	   	
	   	return new TissueCrossSectionColorMap(255);
	 }

	 public String getPortrayalName() {	  
		   return NAME;
	 }

	 public Rectangle2D.Double getViewPortRectangle() {
		   return new Rectangle2D.Double(0d, 0d, 0d, 0d);
	 }	
	 
	 private void doCrossSectionPlaneTransformation(){	
			
			if(((Display3DHack)getCurrentDisplay()) != null){
				 ModelSceneCrossSectionMode actCrossSectionMode = ((Display3DHack)getCurrentDisplay()).getModelSceneCrossSectionMode();
				 double actCrossSectionTranslationCoordinate = ((Display3DHack)getCurrentDisplay()).getActModelSceneCrossSectionCoordinate();
				 boolean retranslate = false;
				 
				 actCrossSectionMode = actCrossSectionMode != ModelSceneCrossSectionMode.DISABLED ? actCrossSectionMode : ModelSceneCrossSectionMode.X_Y_PLANE;
				
				 
				 if(actCrossSectionMode != lastSelectedCrossSectionMode){					 
					 if(actCrossSectionMode == ModelSceneCrossSectionMode.X_Y_PLANE || actCrossSectionMode == ModelSceneCrossSectionMode.DISABLED){
						 if(lastSelectedCrossSectionMode == ModelSceneCrossSectionMode.X_Z_PLANE){				
							 this.translate(0, -1*lastCrossSectionTranslationCoordinate, 0);
							 this.rotateX(-90);					 
						 }
						 if(lastSelectedCrossSectionMode == ModelSceneCrossSectionMode.Y_Z_PLANE){
							 this.translate(-1*lastCrossSectionTranslationCoordinate,0, 0);
							 this.rotateY(90);					 
						 }
					 }
					 if(actCrossSectionMode == ModelSceneCrossSectionMode.X_Z_PLANE){
						 if(lastSelectedCrossSectionMode == ModelSceneCrossSectionMode.X_Y_PLANE){
							 this.translate(0, 0, -1*lastCrossSectionTranslationCoordinate);
							 this.rotateX(90);					 
						 }
						 if(lastSelectedCrossSectionMode == ModelSceneCrossSectionMode.Y_Z_PLANE){
							 this.translate(-1*lastCrossSectionTranslationCoordinate, 0, 0);
							 this.rotateY(90);
							 this.rotateX(90);					
						 }
					 }		
					 if(actCrossSectionMode == ModelSceneCrossSectionMode.Y_Z_PLANE){
						 if(lastSelectedCrossSectionMode == ModelSceneCrossSectionMode.X_Y_PLANE){
							 this.translate(0, 0, -1*lastCrossSectionTranslationCoordinate);
							 this.rotateY(-90);					 
						 }
						 if(lastSelectedCrossSectionMode == ModelSceneCrossSectionMode.X_Z_PLANE){
							 this.translate(0, -1*lastCrossSectionTranslationCoordinate,0);
							 this.rotateX(-90);
							 this.rotateY(-90);					 
						 }
					 }
					 lastSelectedCrossSectionMode= actCrossSectionMode;
					 retranslate = true;
				 }
				 if(retranslate || actCrossSectionTranslationCoordinate != lastCrossSectionTranslationCoordinate){
					 double translation =  retranslate ? actCrossSectionTranslationCoordinate :(actCrossSectionTranslationCoordinate-lastCrossSectionTranslationCoordinate);
					 if(actCrossSectionMode == ModelSceneCrossSectionMode.X_Y_PLANE){
						 this.translate(0,0,translation);
					 }
					 if(actCrossSectionMode == ModelSceneCrossSectionMode.X_Z_PLANE){
						 this.translate(0, translation, 0);
					 }
					 if(actCrossSectionMode == ModelSceneCrossSectionMode.Y_Z_PLANE){
						 this.translate(translation,0 ,0);
					 }		 	
				 	 lastCrossSectionTranslationCoordinate=actCrossSectionTranslationCoordinate;
				 }
			}
	 }
	 
	 
	 
}
