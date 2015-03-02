package sim.app.episim.visualization;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import javax.media.j3d.TransformGroup;

import episiminterfaces.EpisimBiomechanicalModel;
import episiminterfaces.EpisimPortrayal;
import sim.app.episim.model.AbstractCell;
import sim.app.episim.model.biomechanics.CellBoundaries;
import sim.app.episim.model.biomechanics.centerbased3D.newmodel.CenterBased3DModel;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters.MiscalleneousGlobalParameters3D;
import sim.app.episim.tissueimport.TissueController;
import sim.app.episim.util.GenericBag;
import sim.app.episim.util.Loop;
import sim.app.episim.util.Loop.Each;
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
	
	private static boolean tissueCrossSectionDirty = true;
	
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
	
	public static void setTissueCrossSectionDirty(){
		tissueCrossSectionDirty = true;
	}
	
	public Object getField()
   {
		
		if(tissueCrossSectionDirty)setField(createInt2DField());	
		return this.field;
   }
	
	
	
	private IntGrid2D createInt2DField(){
		
		double actCrossSectionTranslationCoordinate = 0;
		ModelSceneCrossSectionMode actCrossSectionMode = ModelSceneCrossSectionMode.X_Y_PLANE;
		if(((Display3DHack)getCurrentDisplay()) != null){
			actCrossSectionMode = ((Display3DHack)getCurrentDisplay()).getModelSceneCrossSectionMode();
			actCrossSectionTranslationCoordinate = ((Display3DHack)getCurrentDisplay()).getActModelSceneCrossSectionCoordinate();
		}
		
		final double positionInMikrometer = actCrossSectionTranslationCoordinate;
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
		
		
		
		final GenericBag<AbstractCell> allCells = TissueController.getInstance().getActEpidermalTissue().getAllCells();
		if(actCrossSectionMode != ModelSceneCrossSectionMode.DISABLED){
			if(actCrossSectionMode == ModelSceneCrossSectionMode.X_Y_PLANE){
				this.renewTilePortrayal((float)(1f/factorXY), (float)(1f/factorXY));
				field2D = new IntGrid2D(Math.round((float)(width*factorXY)), Math.round((float)(height*factorXY)));
				initializeFieldWithColor(field2D);
			}
			else if(actCrossSectionMode == ModelSceneCrossSectionMode.X_Z_PLANE){
				this.renewTilePortrayal((float)(1f/factorXZ), (float)(1f/factorXZ));
				field2D = new IntGrid2D(Math.round((float)(width*factorXZ)), Math.round((float)(length*factorXZ)));
				initializeFieldWithColor(field2D);
			}
			else if(actCrossSectionMode == ModelSceneCrossSectionMode.Y_Z_PLANE){
				this.renewTilePortrayal((float)(1f/factorYZ), (float)(1f/factorYZ));
				field2D = new IntGrid2D(Math.round((float)(length*factorYZ)), Math.round((float)(height*factorYZ)));
				initializeFieldWithColor(field2D);
			}
			
			final ModelSceneCrossSectionMode actCrossSectionModeFinal=actCrossSectionMode;
			final IntGrid2D field2DFinal= field2D;
		//	long start = System.currentTimeMillis();
			final ArrayList<AbstractCell> cellsInCrossSection = new ArrayList<AbstractCell>();
			final ArrayList<CellBoundaries> cellsBoundariesInCrossSection = new ArrayList<CellBoundaries>();
			for(int i = 0; i < allCells.size(); i++){				
				EpisimBiomechanicalModel bm = allCells.get(i).getEpisimBioMechanicalModelObject();
				CellBoundaries boundariesCell =bm.getCellBoundariesInMikron(0);
				if(actCrossSectionModeFinal == ModelSceneCrossSectionMode.X_Y_PLANE){					
					if(positionInMikrometer >= (boundariesCell.getMinZInMikron()*0.9) && positionInMikrometer <= (boundariesCell.getMaxZInMikron()*1.1)){
						cellsInCrossSection.add(allCells.get(i));
						cellsBoundariesInCrossSection.add(boundariesCell);
					}
				}
				else if(actCrossSectionModeFinal == ModelSceneCrossSectionMode.X_Z_PLANE){
					if(positionInMikrometer >= (boundariesCell.getMinYInMikron()*0.9) && positionInMikrometer <= (boundariesCell.getMaxYInMikron()*1.1)){
						cellsInCrossSection.add(allCells.get(i));
						cellsBoundariesInCrossSection.add(boundariesCell);
					}				
				}
				else if(actCrossSectionModeFinal == ModelSceneCrossSectionMode.Y_Z_PLANE){
					if(positionInMikrometer >= (boundariesCell.getMinXInMikron()*0.9) && positionInMikrometer <= (boundariesCell.getMaxXInMikron()*1.1)){
						cellsInCrossSection.add(allCells.get(i));
						cellsBoundariesInCrossSection.add(boundariesCell);
					}
				}
				
			}
			//for(int i = 0; i < cellsInCrossSection.size(); i++){
			if(cellsInCrossSection.size()>0){
			Loop.withIndex(0, cellsInCrossSection.size(), new Loop.Each() {
				public void run(int i) {
		
				EpisimBiomechanicalModel bm = cellsInCrossSection.get(i).getEpisimBioMechanicalModelObject();
				CellBoundaries boundariesCell =cellsBoundariesInCrossSection.get(i);
				CellBoundaries boundariesNucleus=null;
				if(bm instanceof CenterBased3DModel){
					boundariesNucleus =((CenterBased3DModel)bm).getNucleusBoundariesInMikron(0);					
				}
				if(actCrossSectionModeFinal == ModelSceneCrossSectionMode.X_Y_PLANE){					
					if(positionInMikrometer >= (boundariesCell.getMinZInMikron()*0.9) && positionInMikrometer <= (boundariesCell.getMaxZInMikron()*1.1)){
						if(boundariesCell!= null) boundariesCell.getXYCrosssection(positionInMikrometer, field2DFinal, cellsInCrossSection.get(i).getCellColoring());
						if(boundariesNucleus!= null) boundariesNucleus.getXYCrosssection(positionInMikrometer, field2DFinal, new Color(140,140,240));
					}
				}
				else if(actCrossSectionModeFinal == ModelSceneCrossSectionMode.X_Z_PLANE){
					if(positionInMikrometer >= (boundariesCell.getMinYInMikron()*0.9) && positionInMikrometer <= (boundariesCell.getMaxYInMikron()*1.1)){
						if(boundariesCell != null) boundariesCell.getXZCrosssection(positionInMikrometer, field2DFinal, cellsInCrossSection.get(i).getCellColoring());
						if(boundariesNucleus != null) boundariesNucleus.getXZCrosssection(positionInMikrometer, field2DFinal, new Color(140,140,240));
					}				
				}
				else if(actCrossSectionModeFinal == ModelSceneCrossSectionMode.Y_Z_PLANE){
					if(positionInMikrometer >= (boundariesCell.getMinXInMikron()*0.9) && positionInMikrometer <= (boundariesCell.getMaxXInMikron()*1.1)){
						if(boundariesCell!= null) boundariesCell.getYZCrosssection(positionInMikrometer, field2DFinal, cellsInCrossSection.get(i).getCellColoring());						
						if(boundariesNucleus!= null)	boundariesNucleus.getYZCrosssection(positionInMikrometer, field2DFinal, new Color(140,140,240));
					}
				}
				}
			});	
			}
			//long end = System.currentTimeMillis();
			//System.out.println("Cross Section Calculation Time in ms:"+(end-start));
		//	start=System.currentTimeMillis();
			addTransparentColor(field2D);
	//		end = System.currentTimeMillis();
		//	System.out.println("Add Transparent Color Calculation Time in ms:"+(end-start));
		}
		else {
			field2D = new IntGrid2D(Math.round((float)(width*factorXY)), Math.round((float)(height*factorXY)));
			addTransparentColorForWholeField(field2D);
		}
		tissueCrossSectionDirty=false;
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
