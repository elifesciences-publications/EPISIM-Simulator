package sim.app.episim.model.biomechanics.hexagonbased.singlesurface;


import sim.app.episim.model.biomechanics.hexagonbased.AbstractHexagonBasedMechanicalModelGP;
import episiminterfaces.NoUserModification;
import episiminterfaces.NoExport;


public class HexagonBasedMechanicalModelGP extends AbstractHexagonBasedMechanicalModelGP {
	
	
	private double celldiameter_mikron = 50;
	private double inner_hexagonal_radius = ((celldiameter_mikron/2d)/2d)*Math.sqrt(3d);
	private double outer_hexagonal_radius = (celldiameter_mikron/2d);	
	
	private double numberOfPixelsPerMicrometer = 1;
	
	
	private double number_of_columns =54;
	private double number_of_rows =47;
	
	private int initialCellDensityInPercent = 100;	
	
	private double neighborhood_mikron = 2d*inner_hexagonal_radius;
	
	private boolean useContinuousSpace = false;
	private boolean useCellCellInteractionEnergy = true;
	
	
	private boolean chemotaxisEnabled = true;
	
   public double getNumber_of_columns() {
   	
	   return number_of_columns;
   }
   
   public void setNumber_of_columns(double number_of_columns) {

	   this.number_of_columns = (int)number_of_columns;
	   if((this.number_of_columns %2) > 0) this.number_of_columns++;
   }
   
   public double getNumber_of_rows() {
	   return number_of_rows;
   }
   
   public void setNumber_of_rows(double number_of_rows) {

	   this.number_of_rows = (int)number_of_rows;
	   if((this.number_of_rows %2) > 0) this.number_of_rows++;
   }
   	
	public double getNeighborhood_mikron() {
	   return this.neighborhood_mikron;
   }

	public void setNeighborhood_mikron(double val) {
	   this.neighborhood_mikron = val;	   
   }
	@NoUserModification
	@NoExport
	public int getBasalOpening_mikron() {
		//not needed in first version
	   return 0;
   }
	@NoUserModification
	@NoExport
	public void setBasalOpening_mikron(int val) {
		//not needed in first version
   }
	
	@NoUserModification
	@NoExport
	public int getBasalAmplitude_mikron() {
		//not needed in first version
	   return 0;
   }
	@NoUserModification
	@NoExport
	public void setBasalAmplitude_mikron(int val) {
		//not needed in first version
   }
	@NoUserModification
	@NoExport
   public int getBasalPeriod_mikron() {
		//not needed in first version
	   return 0;
   }
	@NoUserModification
	@NoExport
   public void setBasalPeriod_mikron(int val) {
		//not needed in first version   
   }
	@NoUserModification
	@NoExport
   public int getBasalYDelta_mikron() {
		//not needed in first version
	   return 0;
   }
	@NoUserModification
	@NoExport
   public void setBasalYDelta_mikron(int val) {
		//not needed in first version
   }
	
	@NoExport
	public void setWidthInMikron(double val) {
		if(val > 0){
			setNumber_of_columns(1+ ((val-celldiameter_mikron)/(1.5*outer_hexagonal_radius)));
		}
	}
	
	@NoUserModification
	@NoExport
	public double getWidthInMikron() {
		return celldiameter_mikron + (number_of_columns-1d)*1.5*outer_hexagonal_radius;
   }	
	

	public void setCellDiameterInMikron(double val) {
		if(val > 0){
			double widthInMikron = getWidthInMikron();
			double heightInMikron = getHeightInMikron();
					
			this.celldiameter_mikron = val;
			inner_hexagonal_radius = ((celldiameter_mikron/2d)/2d)*Math.sqrt(3d);
			outer_hexagonal_radius = (celldiameter_mikron/2d);
			setNumber_of_columns(1 + ((widthInMikron-celldiameter_mikron)/(1.5*outer_hexagonal_radius)));
			setNumber_of_rows(heightInMikron/(2d*inner_hexagonal_radius));
		}
	}
	
	@NoUserModification
	public double getCellDiameterInMikron() {
		return celldiameter_mikron ;
   }
			
	@NoExport
	public void setHeightInMikron(double val) {
		if(val > 0){
			setNumber_of_rows(val/(2d*inner_hexagonal_radius));
		}		
   }
		
	@NoUserModification
	@NoExport
	public double getHeightInMikron(){
		return number_of_rows*2d*inner_hexagonal_radius;
   }
	
	@NoUserModification
	public double getLengthInMikron() {
	   	//not needed in 2D model
	   	return 0;
	}
	@NoUserModification
	public void setLengthInMikron(double val) {   
	   	//not needed in 2D model
	}
	
	
	
	
		
   
	public void setNumberOfPixelsPerMicrometer(double val) {
	   this.numberOfPixelsPerMicrometer	= val;   
   }
	
	public boolean getUseContinuousSpace(){
		return this.useContinuousSpace;
	}
	
	public void setUseContinuousSpace(boolean useContinuousSpace){
		this.useContinuousSpace=useContinuousSpace;
	}
	
	public boolean getUseCellCellInteractionEnergy(){
		return this.useCellCellInteractionEnergy;
	}
	
	public void setUseCellCellInteractionEnergy(boolean useCellCellInteractionEnergy){
		this.useCellCellInteractionEnergy = useCellCellInteractionEnergy;
	}
	
	@NoUserModification
	public double getNumberOfPixelsPerMicrometer() {
	   return this.numberOfPixelsPerMicrometer;
   }
	
	public boolean isChemotaxisEnabled() {
   
   	return chemotaxisEnabled;
   }

	
   public void setChemotaxisEnabled(boolean chemotaxisEnabled) {
   
   	this.chemotaxisEnabled = chemotaxisEnabled;
   }
   
   @NoUserModification
	public boolean areDiffusionFieldsContinousInZDirection() {	   
	   return false;
   }

	@NoUserModification
   public ModelDimensionality getModelDimensionality() {	   
	   return ModelDimensionality.TWO_DIMENSIONAL;
   }

	
   public int getInitialCellDensityInPercent() {
   
   	return initialCellDensityInPercent;
   }

	
   public void setInitialCellDensityInPercent(int initialCellDensityInPercent) {
   
   	this.initialCellDensityInPercent = initialCellDensityInPercent;
   }   

   @NoUserModification
   public double getInner_hexagonal_radius() {
   
   	return inner_hexagonal_radius;
   }

   @NoUserModification
   public double getOuter_hexagonal_radius() {
   
   	return outer_hexagonal_radius;
   }
   
   @NoUserModification
   public void setInner_hexagonal_radius(double val) {
   
   	inner_hexagonal_radius=val;
   }

   @NoUserModification
   public void setOuter_hexagonal_radius(double val) {
   
   	outer_hexagonal_radius = val;
   }

}

