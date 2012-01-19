package sim.app.episim.model.visualization;

import java.awt.Color;
import java.awt.geom.Rectangle2D;


import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Vector3d;

import sim.app.episim.model.controller.ExtraCellularDiffusionController;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.controller.ExtraCellularDiffusionController.DiffusionFieldCrossSectionMode;
import sim.app.episim.model.diffusion.ExtraCellularDiffusionField;
import sim.app.episim.model.diffusion.ExtraCellularDiffusionField3D;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters;
import sim.app.episim.util.DiffusionColorGradient;
import sim.display3d.Display3DHack;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.DoubleGrid3D;
import sim.field.grid.Grid2D;
import sim.portrayal3d.grid.ValueGrid2DPortrayal3DHack;
import sim.portrayal3d.grid.quad.ValueGridCellInfo;
import sim.util.gui.SimpleColorMap;


public class ExtraCellularDiffusionCrossSectionPortrayal3D extends ValueGrid2DPortrayal3DHack implements ExtraCellularDiffusionPortrayal{
	
	private String name;
	
	private ExtraCellularDiffusionField3D extraCellularDiffusionField;
	
	private Color[] legendLookUpTable;
	
	private double minValue=0;
	private double maxValue=0;
	
	private DiffusionFieldCrossSectionMode lastSelectedCrossSectionMode = DiffusionFieldCrossSectionMode.X_Y_PLANE;
	private double lastCrossSectionTranslationCoordinate = 0;
	
	public ExtraCellularDiffusionCrossSectionPortrayal3D(ExtraCellularDiffusionField diffusionField){
		super(diffusionField.getName(), (float)diffusionField.getFieldConfiguration().getLatticeSiteSizeInMikron());
		this.name = diffusionField.getName();
		if(diffusionField instanceof ExtraCellularDiffusionField3D)this.extraCellularDiffusionField = (ExtraCellularDiffusionField3D)diffusionField;
		else throw new IllegalArgumentException("diffusionField must be of type ExtraCellularDiffusionField3D");
		setField(createDouble2DField(this.extraCellularDiffusionField.getExtraCellularField()));
		this.setMap(buildColorMap());
		doCrossSectionPlaneTransformation();
	}
	
	
	public Object getField()
   {
	
		setField(createDouble2DField(this.extraCellularDiffusionField.getExtraCellularField()));
		return this.field;
   }
	
	
	
	private DoubleGrid2D createDouble2DField(DoubleGrid3D field3D){
		ExtraCellularDiffusionController controller = ModelController.getInstance().getExtraCellularDiffusionController();
		DiffusionFieldCrossSectionMode actCrossSectionMode = controller.getSelectedDiffusionFieldCrossSectionMode();
		double actCrossSectionTranslationCoordinate = controller.getDiffusionFieldCrossSectionCoordinate();
		double fieldRes = this.extraCellularDiffusionField.getFieldConfiguration().getLatticeSiteSizeInMikron();
		int constantArrayIndex = Math.round((float)(actCrossSectionTranslationCoordinate/fieldRes));
		DoubleGrid2D field2D = null;
		if(actCrossSectionMode == DiffusionFieldCrossSectionMode.X_Y_PLANE){
			field2D = new DoubleGrid2D(field3D.getWidth(), field3D.getHeight());
			constantArrayIndex = constantArrayIndex >= field3D.getLength() ? field3D.getLength()-1 : constantArrayIndex;
			for(int y = 0; y < field3D.getHeight(); y++){
				for(int x = 0; x < field3D.getWidth(); x++){
					field2D.field[x][y] = field3D.field[x][y][constantArrayIndex];
				}
			}
		}
		if(actCrossSectionMode == DiffusionFieldCrossSectionMode.X_Z_PLANE){
			field2D = new DoubleGrid2D(field3D.getWidth(), field3D.getLength());
			constantArrayIndex = constantArrayIndex >= field3D.getHeight() ? field3D.getHeight()-1 : constantArrayIndex;
			for(int z = 0; z < field3D.getLength(); z++){
				for(int x = 0; x < field3D.getWidth(); x++){
					field2D.field[x][z] = field3D.field[x][constantArrayIndex][z];
				}
			}
		}
		if(actCrossSectionMode == DiffusionFieldCrossSectionMode.Y_Z_PLANE){
			field2D = new DoubleGrid2D(field3D.getLength(), field3D.getHeight());
			constantArrayIndex = constantArrayIndex >= field3D.getWidth() ? field3D.getWidth()-1 : constantArrayIndex;
			for(int y = 0; y < field3D.getLength(); y++){
				for(int z = 0; z < field3D.getHeight(); z++){
					field2D.field[z][y] = field3D.field[constantArrayIndex][y][z];
				}
			}
		}
		return field2D;
	}
	
	public TransformGroup createModel()
   {
		 
		 DoubleGrid2D field = (DoubleGrid2D) getField();
		 setField(field);
		 minValue = extraCellularDiffusionField.getFieldConfiguration().getMinimumConcentration();
	    maxValue = extraCellularDiffusionField.getFieldConfiguration().getMaximumConcentration() < Double.POSITIVE_INFINITY 
	   							? extraCellularDiffusionField.getFieldConfiguration().getMaximumConcentration()
	   							: field.max();		
		 
		 this.setMap(buildColorMap());
		 TransformGroup modelTG = super.createModel();
		 doCrossSectionPlaneTransformation();
		 this.setTransparency(((Display3DHack)getCurrentDisplay()).getDiffusionFieldOpacity());
		 return modelTG;
    }
	 
	 public void updateModel(TransformGroup modelTG)
    {
		 DoubleGrid2D field = (DoubleGrid2D) getField();
		 setField(field);
		 minValue = extraCellularDiffusionField.getFieldConfiguration().getMinimumConcentration();
	    maxValue = extraCellularDiffusionField.getFieldConfiguration().getMaximumConcentration() < Double.POSITIVE_INFINITY 
	   							? extraCellularDiffusionField.getFieldConfiguration().getMaximumConcentration()
	   							: field.max();		
		 
		 this.setMap(buildColorMap());
		 super.updateModel(modelTG);
		 doCrossSectionPlaneTransformation();
    }
	 
	 private SimpleColorMap buildColorMap(){   	
	   	Color[] colorTable = DiffusionColorGradient.createMultiGradient(COLOR_STEPS, (int)(maxValue-minValue+1));
	      legendLookUpTable = new Color[colorTable.length];
	   	for(int i = 0; i < colorTable.length; i++){
	   		legendLookUpTable[i]= colorTable[i];
	      	colorTable[i] = new Color(colorTable[i].getRed(), colorTable[i].getGreen(), colorTable[i].getBlue(),25); 
	      }
	   	
	   	return new SimpleColorMap(colorTable);
	 }

	 public String getPortrayalName() {	  
		   return this.name;
	 }

	 public Rectangle2D.Double getViewPortRectangle() {
		   return new Rectangle2D.Double(0d, 0d, 0d, 0d);
	 }


	 public void setExtraCellularDiffusionField(ExtraCellularDiffusionField extraCellularDiffusionField) {
	  	 if(extraCellularDiffusionField == null) this.extraCellularDiffusionField = null;
	      else if(extraCellularDiffusionField instanceof ExtraCellularDiffusionField3D)this.extraCellularDiffusionField = (ExtraCellularDiffusionField3D)extraCellularDiffusionField;
		   if(extraCellularDiffusionField != null){
		   	this.name = extraCellularDiffusionField.getName();
		   }
	 }
	 public ExtraCellularDiffusionField getExtraCellularDiffusionField() {
		   
		   return this.extraCellularDiffusionField;
	 }
	 
	 private void doCrossSectionPlaneTransformation(){
		 ExtraCellularDiffusionController controller = ModelController.getInstance().getExtraCellularDiffusionController();
		 DiffusionFieldCrossSectionMode actCrossSectionMode = controller.getSelectedDiffusionFieldCrossSectionMode();
		 double actCrossSectionTranslationCoordinate = controller.getDiffusionFieldCrossSectionCoordinate();
		 
		
		 if(actCrossSectionMode != lastSelectedCrossSectionMode || actCrossSectionTranslationCoordinate != lastCrossSectionTranslationCoordinate){
			 double translation = (actCrossSectionTranslationCoordinate-lastCrossSectionTranslationCoordinate);
			 if(actCrossSectionMode == DiffusionFieldCrossSectionMode.X_Y_PLANE){
				 this.translate(0,0,translation);
			 }
			 if(actCrossSectionMode == DiffusionFieldCrossSectionMode.X_Z_PLANE){
				 this.translate(0, translation, 0);
			 }
			 if(actCrossSectionMode == DiffusionFieldCrossSectionMode.Y_Z_PLANE){
				 this.translate(translation,0 ,0);
			 }		 	
		 	 lastCrossSectionTranslationCoordinate=actCrossSectionTranslationCoordinate;
		 }
		 if(actCrossSectionMode != lastSelectedCrossSectionMode){
			 lastCrossSectionTranslationCoordinate=controller.getDiffusionFieldCrossSectionCoordinate();
			 if(actCrossSectionMode == DiffusionFieldCrossSectionMode.X_Y_PLANE){
				 if(lastSelectedCrossSectionMode == DiffusionFieldCrossSectionMode.X_Z_PLANE){					
					 this.translate(0, -1*lastCrossSectionTranslationCoordinate, 0);
					 this.rotateX(-90);
					 this.translate(0, 0, lastCrossSectionTranslationCoordinate);
				 }
				 if(lastSelectedCrossSectionMode == DiffusionFieldCrossSectionMode.Y_Z_PLANE){
					 this.translate(-1*lastCrossSectionTranslationCoordinate,0, 0);
					 this.rotateY(90);
					 this.translate(0, 0,lastCrossSectionTranslationCoordinate);
				 }
			 }
			 if(actCrossSectionMode == DiffusionFieldCrossSectionMode.X_Z_PLANE){
				 if(lastSelectedCrossSectionMode == DiffusionFieldCrossSectionMode.X_Y_PLANE){
					 this.translate(0, 0, -1*lastCrossSectionTranslationCoordinate);
					 this.rotateX(90);
					 this.translate(0, lastCrossSectionTranslationCoordinate, 0);
				 }
				 if(lastSelectedCrossSectionMode == DiffusionFieldCrossSectionMode.Y_Z_PLANE){
					 this.translate(-1*lastCrossSectionTranslationCoordinate, 0, 0);
					 this.rotateY(90);
					 this.rotateX(90);
					 this.translate(0, lastCrossSectionTranslationCoordinate, 0);
				 }
			 }		
			 if(actCrossSectionMode == DiffusionFieldCrossSectionMode.Y_Z_PLANE){
				 if(lastSelectedCrossSectionMode == DiffusionFieldCrossSectionMode.X_Y_PLANE){
					 this.translate(0, 0, -1*lastCrossSectionTranslationCoordinate);
					 this.rotateY(-90);
					 this.translate(lastCrossSectionTranslationCoordinate, 0, 0);
				 }
				 if(lastSelectedCrossSectionMode == DiffusionFieldCrossSectionMode.X_Z_PLANE){
					 this.translate(0, -1*lastCrossSectionTranslationCoordinate,0);
					 this.rotateX(-90);
					 this.rotateY(-90);
					 this.translate(lastCrossSectionTranslationCoordinate, 0, 0);
				 }
			 }
			 lastSelectedCrossSectionMode=actCrossSectionMode;
		 }
	 }
	 
}
