package sim.app.episim.model.diffusion;

import ec.util.MersenneTwisterFast;
import episiminterfaces.EpisimDiffusionFieldConfiguration;
import sim.app.episim.util.EnhancedSteppable;
import sim.engine.SimState;
import sim.field.grid.DoubleGrid2D;


public class ExtraCellularDiffusionField implements EnhancedSteppable{
	
	private DoubleGrid2D extraCellularField;
	
	private double widthInMikron;
	private double heightInMikron;
	 
	private boolean toroidal;
	private EpisimDiffusionFieldConfiguration fieldConfiguration;
	private ForwardEulerDiffusionReaction fEulerDiffReact;
	
	public ExtraCellularDiffusionField(EpisimDiffusionFieldConfiguration fieldConfiguration, double widthInMikron, double heightInMikron, boolean toroidal){
		this.widthInMikron = widthInMikron;
		this.heightInMikron = heightInMikron;
		this.toroidal = toroidal;
		this.fieldConfiguration = fieldConfiguration;
		
		int width = (int)Math.floor(widthInMikron / fieldConfiguration.getLatticeSiteSizeInMikron());
		int height = (int)Math.floor(heightInMikron / fieldConfiguration.getLatticeSiteSizeInMikron());
		
		this.extraCellularField = new DoubleGrid2D(width, height);
		fEulerDiffReact = new ForwardEulerDiffusionReaction(this);
	}	
	
   public String getName() {
	   return fieldConfiguration.getDiffusionFieldName();
   }	
  	
   public DoubleGrid2D getExtraCellularField() {   
   	return extraCellularField;
   }
	
   public void setExtraCellularField(DoubleGrid2D extraCellularField) {   
   	this.extraCellularField = extraCellularField;
   }
	
   public double getWidthInMikron() {   
   	return widthInMikron;
   }
	
   public void setWidthInMikron(double widthInMikron) {   
   	this.widthInMikron = widthInMikron;
   }
	
   public double getHeightInMikron() {   
   	return heightInMikron;
   }
	
   public void setHeightInMikron(double heightInMikron) {   
   	this.heightInMikron = heightInMikron;
   }
   
	
   public boolean isToroidal() {   
   	return toroidal;
   }
	
   public void setToroidal(boolean toroidal) {   
   	this.toroidal = toroidal;
   }
   
   
   public EpisimDiffusionFieldConfiguration getFieldConfiguration() {
	   return fieldConfiguration;
   }
	
   public void step(SimState state) {
   	for(int i = 0; i < fieldConfiguration.getNumberOfIterationsPerCBMSimStep(); i++){
   		fEulerDiffReact.updateExtraCellularField();
   	}
   }
   
   public double getConcentration(double xInMikron, double yInMikron){
   	int x = (int)(xInMikron/ fieldConfiguration.getLatticeSiteSizeInMikron());
   	int y = (int)(yInMikron/ fieldConfiguration.getLatticeSiteSizeInMikron());
   	if(x < extraCellularField.getWidth() && y < extraCellularField.getHeight()){   
   		return extraCellularField.get(x, y);
   	}
   	else return 0;
   }
   public double getInterval() {
	   return 1;
   }
   
}
