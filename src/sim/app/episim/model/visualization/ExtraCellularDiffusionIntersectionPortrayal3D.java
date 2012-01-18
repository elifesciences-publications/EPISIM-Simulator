package sim.app.episim.model.visualization;

import java.awt.Color;
import java.awt.geom.Rectangle2D;


import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Vector3d;

import sim.app.episim.model.diffusion.ExtraCellularDiffusionField;
import sim.app.episim.model.diffusion.ExtraCellularDiffusionField3D;
import sim.app.episim.util.DiffusionColorGradient;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.DoubleGrid3D;
import sim.field.grid.Grid2D;
import sim.portrayal3d.grid.ValueGrid2DPortrayal3DHack;
import sim.portrayal3d.grid.quad.ValueGridCellInfo;
import sim.util.gui.SimpleColorMap;


public class ExtraCellularDiffusionIntersectionPortrayal3D extends ValueGrid2DPortrayal3DHack implements ExtraCellularDiffusionPortrayal{
	
	private String name;
	
	private ExtraCellularDiffusionField3D extraCellularDiffusionField;
	
	private Color[] legendLookUpTable;
	
	private double minValue=0;
	private double maxValue=0;
	
	public ExtraCellularDiffusionIntersectionPortrayal3D(ExtraCellularDiffusionField diffusionField){
		super(diffusionField.getName(), (float)diffusionField.getFieldConfiguration().getLatticeSiteSizeInMikron());
		this.name = diffusionField.getName();
		if(diffusionField instanceof ExtraCellularDiffusionField3D)this.extraCellularDiffusionField = (ExtraCellularDiffusionField3D)diffusionField;
		else throw new IllegalArgumentException("diffusionField must be of type ExtraCellularDiffusionField3D");
		setField(createDouble2DField(this.extraCellularDiffusionField.getExtraCellularField()));
		this.setMap(buildColorMap());
		this.rotateX(90);
	}
	
	
	public Object getField()
   {
	
		setField(createDouble2DField(this.extraCellularDiffusionField.getExtraCellularField()));
		return this.field;
   }
	
	
	
	private DoubleGrid2D createDouble2DField(DoubleGrid3D field3D){
	
		DoubleGrid2D field2D = new DoubleGrid2D(field3D.getWidth(), field3D.getHeight());
		int z = field3D.getLength()/2;
		for(int y = 0; y < field3D.getHeight(); y++){
			for(int x = 0; x < field3D.getWidth(); x++){
				field2D.field[x][y] = field3D.field[x][y][z];
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
		 
		 Transform3D transform = new Transform3D();
			/*	 modelTG.getTransform(transform);
				 Vector3d translationOld = new Vector3d(0,0,0);
				 transform.get(translationOld);
				 Vector3d additionalTranslation = new Vector3d(10,0,0);
				 translationOld.add(additionalTranslation);
				 transform.setTranslation(translationOld);*/

		
		 
		 //this.translate(100, 0, 0);
			
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
		 Transform3D transform = new Transform3D();
	/*	 modelTG.getTransform(transform);
		 Vector3d translationOld = new Vector3d(0,0,0);
		 transform.get(translationOld);
		 Vector3d additionalTranslation = new Vector3d(10,0,0);
		 translationOld.add(additionalTranslation);
		 transform.setTranslation(translationOld);*/
		
		// Vector3d additionalTranslation = new Vector3d(100,0,0);
		// transform.setTranslation(additionalTranslation);
		 //modelTG.setTransform(transform);
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
}
