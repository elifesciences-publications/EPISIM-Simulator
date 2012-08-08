package sim.app.episim.model.visualization;

import java.awt.Color;
import java.awt.geom.Rectangle2D;

import javax.media.j3d.TransformGroup;

import sim.app.episim.model.diffusion.ExtraCellularDiffusionField;
import sim.app.episim.model.diffusion.ExtraCellularDiffusionField2D;
import sim.app.episim.model.diffusion.ExtraCellularDiffusionField3D;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters;
import sim.app.episim.util.DiffusionColorGradient;
import sim.portrayal3d.grid.ValueGridPortrayal3D;
import sim.portrayal3d.grid.ValueGridPortrayal3DHack;
import sim.util.gui.SimpleColorMap;


public class ExtraCellularDiffusionPortrayal3D extends ValueGridPortrayal3DHack implements ExtraCellularDiffusionPortrayal{
	
	private String name;
	
	private ExtraCellularDiffusionField3D extraCellularDiffusionField;
	
	private Color[] legendLookUpTable;
	
	private double minValue=0;
	private double maxValue=0;
	
	public ExtraCellularDiffusionPortrayal3D(ExtraCellularDiffusionField diffusionField){
		super(diffusionField.getName(), (float)diffusionField.getFieldConfiguration().getLatticeSiteSizeInMikron());
		this.name = diffusionField.getName();
		if(diffusionField instanceof ExtraCellularDiffusionField3D)this.extraCellularDiffusionField = (ExtraCellularDiffusionField3D)diffusionField;
		else throw new IllegalArgumentException("diffusionField must be of type ExtraCellularDiffusionField3D");
		this.setField(this.extraCellularDiffusionField.getExtraCellularField());
	   this.setMap(buildColorMap());
	   
	}
	
	 public TransformGroup createModel()
    {
		 minValue = extraCellularDiffusionField.getFieldConfiguration().getMinimumConcentration();
	    maxValue = extraCellularDiffusionField.getFieldConfiguration().getMaximumConcentration() < Double.POSITIVE_INFINITY 
	   							? extraCellularDiffusionField.getFieldConfiguration().getMaximumConcentration()
	   							: extraCellularDiffusionField.getExtraCellularField().max();		 
		 
		this.setMap(buildColorMap());	
		
		 TransformGroup modelTG = super.createModel();
		return modelTG;
    }
	 
	 public void updateModel(TransformGroup modelTG)
    {
		 minValue = extraCellularDiffusionField.getFieldConfiguration().getMinimumConcentration();
	    double newMaxValue = extraCellularDiffusionField.getFieldConfiguration().getMaximumConcentration() < Double.POSITIVE_INFINITY 
	   							? extraCellularDiffusionField.getFieldConfiguration().getMaximumConcentration()
	   							: extraCellularDiffusionField.getExtraCellularField().max();
		 
	   if(newMaxValue > maxValue) this.maxValue = newMaxValue;
		
		this.setMap(buildColorMap());	
		 super.updateModel(modelTG);
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
	
   public ExtraCellularDiffusionField getExtraCellularDiffusionField() {
	   
	   return this.extraCellularDiffusionField;
   }
   public void setExtraCellularDiffusionField(ExtraCellularDiffusionField extraCellularDiffusionField) {
  	 if(extraCellularDiffusionField == null) this.extraCellularDiffusionField = null;
      else if(extraCellularDiffusionField instanceof ExtraCellularDiffusionField3D)this.extraCellularDiffusionField = (ExtraCellularDiffusionField3D)extraCellularDiffusionField;
	   if(extraCellularDiffusionField != null){
	   	this.name = extraCellularDiffusionField.getName();
	   	this.setField(this.extraCellularDiffusionField.getExtraCellularField());
	   }
  }

}
