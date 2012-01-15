package sim.app.episim.model.diffusion;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import sim.app.episim.model.biomechanics.CellBoundaries;
import sim.app.episim.util.EnhancedSteppable;
import sim.engine.SimState;
import sim.field.grid.DoubleGrid3D;
import sim.util.DoubleBag;
import episiminterfaces.EpisimDiffusionFieldConfiguration;


public class ExtraCellularDiffusionField3D implements EnhancedSteppable, ExtraCellularDiffusionField{
	
	private DoubleGrid3D extraCellularField;
	
	private double sizeXInMikron;
	private double sizeYInMikron;
	private double sizeZInMikron;
	 
	private boolean toroidalX;
	private boolean toroidalY;
	private boolean toroidalZ;
	private EpisimDiffusionFieldConfiguration fieldConfiguration;
	private ForwardEulerDiffusionReaction fEulerDiffReact;
	
	public ExtraCellularDiffusionField3D(EpisimDiffusionFieldConfiguration fieldConfiguration, double sizeXInMikron, double sizeYInMikron, double sizeZInMikron, boolean toroidalX, boolean toroidalY, boolean toroidalZ){
		this.sizeXInMikron = sizeXInMikron;
		this.sizeYInMikron = sizeYInMikron;
		this.sizeZInMikron = sizeZInMikron;
		this.toroidalX = toroidalX;
		this.toroidalY = toroidalY;
		this.toroidalZ = toroidalZ;
		this.fieldConfiguration = fieldConfiguration;
		
		int sizeX = (int)Math.floor(sizeXInMikron / fieldConfiguration.getLatticeSiteSizeInMikron());
		int sizeY = (int)Math.floor(sizeYInMikron / fieldConfiguration.getLatticeSiteSizeInMikron());
		int sizeZ = (int)Math.floor(sizeZInMikron / fieldConfiguration.getLatticeSiteSizeInMikron());
		
		this.extraCellularField = new DoubleGrid3D(sizeX, sizeY, sizeZ);
		fEulerDiffReact = new ForwardEulerDiffusionReaction(this);
	}	
	
   public String getName() {
	   return fieldConfiguration.getDiffusionFieldName();
   }	
  	
   public DoubleGrid3D getExtraCellularField() {   
   	return extraCellularField;
   }
	
   public void setExtraCellularField(DoubleGrid3D extraCellularField) {   
   	this.extraCellularField = extraCellularField;
   }   
   
   public EpisimDiffusionFieldConfiguration getFieldConfiguration() {
	   return fieldConfiguration;
   }
	
   public void step(SimState state) {
   	for(int i = 0; i < fieldConfiguration.getNumberOfIterationsPerCBMSimStep(); i++){
   		fEulerDiffReact.updateExtraCellularField();
   	}
   }
   
   public double getConcentration(double xInMikron, double yInMikron, double zInMikron){
   	int x = mikronToIntPos(xInMikron);
   	int y = mikronToIntPos(yInMikron);
   	int z = mikronToIntPos(zInMikron);
   	if(isToroidalX() && isToroidalY() && isToroidalZ()){
   		return extraCellularField.get(extraCellularField.stx(x), extraCellularField.sty(y), extraCellularField.stz(z));
   	}
   	else if(!isToroidalX() && isToroidalY() && isToroidalZ()){
   		if(x < extraCellularField.getWidth()){   
      		return extraCellularField.get(x, extraCellularField.sty(y), extraCellularField.stz(z));
      	}
      	else return 0;
   	}
   	else if(isToroidalX() && !isToroidalY() && isToroidalZ()){
   		if(y < extraCellularField.getHeight()){   
      		return extraCellularField.get(extraCellularField.stx(x), y, extraCellularField.stz(z));
      	}
      	else return 0;
   	}
   	else if(isToroidalX() && isToroidalY() && !isToroidalZ()){
   		if(z < extraCellularField.getLength()){   
      		return extraCellularField.get(extraCellularField.stx(x), extraCellularField.sty(y), z);
      	}
      	else return 0;
   	}
   	else if(!isToroidalX() && !isToroidalY() && isToroidalZ()){
   		if(x < extraCellularField.getWidth() && y < extraCellularField.getHeight()){   
      		return extraCellularField.get(x, y, extraCellularField.stz(z));
      	}
      	else return 0;
   	}   	
   	else if(!isToroidalX() && isToroidalY() && !isToroidalZ()){
   		if(x < extraCellularField.getWidth() && z < extraCellularField.getLength()){   
      		return extraCellularField.get(x, extraCellularField.sty(y), z);
      	}
      	else return 0;
   	}
   	else if(isToroidalX() && !isToroidalY() && !isToroidalZ()){
   		if(y < extraCellularField.getHeight() && z < extraCellularField.getLength()){   
      		return extraCellularField.get(extraCellularField.stx(x), y, z);
      	}
      	else return 0;
   	}   	
   	else if(!isToroidalX() && !isToroidalY() && !isToroidalZ()){
   		if(x < extraCellularField.getWidth() && y < extraCellularField.getHeight() && z < extraCellularField.getLength()){   
      		return extraCellularField.get(x, y, z);
      	}
      	else return 0;
   	}
   	return 0;
   }
   
   private int mikronToIntPos(double posInMikron){
   	return (int)(posInMikron/ fieldConfiguration.getLatticeSiteSizeInMikron());
   }
   
   private boolean shouldReturnZero(int x, int y, int z){
   	if(!toroidalX && !toroidalY && !toroidalZ && (x==0 || y==0 || z==0 || x ==(extraCellularField.getWidth()-1) || y ==(extraCellularField.getHeight()-1) || z ==(extraCellularField.getLength()-1))){
   		return true;
   	} 	
   	else if(!toroidalX && !toroidalY && (x==0 || y==0 || x ==(extraCellularField.getWidth()-1) || y ==(extraCellularField.getHeight()-1))){
   		return true;
   	}
   	else if(!toroidalY && !toroidalZ && (y==0 || z==0 || y ==(extraCellularField.getHeight()-1) || z ==(extraCellularField.getLength()-1))){
   		return true;
   	}
   	else if(!toroidalX && !toroidalZ && (x==0 || z==0 || x ==(extraCellularField.getWidth()-1) || z ==(extraCellularField.getLength()-1))){
   		return true;
   	}   	
   	else if(!toroidalX && (x==0 || x ==(extraCellularField.getWidth()-1))){
   		return true;
   	}
   	else if(!toroidalY && (y==0 || y ==(extraCellularField.getHeight()-1))){
   		return true;
   	}
   	else if(!toroidalZ && (z==0 || z ==(extraCellularField.getLength()-1))){
   		return true;
   	}
   	
   	return false;
   }
   
   /**
    * 
    * @param xInMikron
    * @param yInMikron
    *	@param concentrationToBeAdded
    * @return the concentration that was in fact added
    */
   public double addConcentration(double xInMikron, double yInMikron, double zInMikron, double concentrationToBeAdded){
   	int x = mikronToIntPos(xInMikron);
   	int y = mikronToIntPos(yInMikron);
   	int z = mikronToIntPos(zInMikron);
   	if(shouldReturnZero(x,y,z)) return 0;
   	double concentration = extraCellularField.get(extraCellularField.stx(x), extraCellularField.sty(y), extraCellularField.stz(z));
   	if((concentration+concentrationToBeAdded) > this.fieldConfiguration.getMaximumConcentration()){
   		concentrationToBeAdded = this.fieldConfiguration.getMaximumConcentration() - concentration;
   	}
   	extraCellularField.set(extraCellularField.stx(x), extraCellularField.sty(y), extraCellularField.stz(z),(concentration+concentrationToBeAdded));
   	return concentrationToBeAdded;
   }
   /**
    * 
    * @param xInMikron
    * @param yInMikron
    *	@param concentrationToBeRemoved
    * @return the concentration that was in fact removed
    */
   public double removeConcentration(double xInMikron, double yInMikron, double zInMikron, double concentrationToBeRemoved){
   	int x = mikronToIntPos(xInMikron);
   	int y = mikronToIntPos(yInMikron);
   	int z = mikronToIntPos(zInMikron);
   	if(shouldReturnZero(x,y,z)) return 0;
   	double concentration = extraCellularField.get(extraCellularField.stx(x), extraCellularField.sty(y), extraCellularField.stz(z));
   	if((concentration-concentrationToBeRemoved) < this.fieldConfiguration.getMinimumConcentration()){
   		concentrationToBeRemoved = concentration - this.fieldConfiguration.getMinimumConcentration();
   	}
   	extraCellularField.set(extraCellularField.stx(x), extraCellularField.sty(y), extraCellularField.stz(z), (concentration-concentrationToBeRemoved));
   	return concentrationToBeRemoved;
   }
   
   
   public double getTotalLocalFieldRemainingCapacity(CellBoundaries cellBoundaries, DoubleBag xPos, DoubleBag yPos, DoubleBag zPos){
   	xPos.clear();
   	yPos.clear();
   	zPos.clear();
   	double maxConcentration = getFieldConfiguration().getMaximumConcentration();
   	if(maxConcentration < Double.POSITIVE_INFINITY){
   		
   		double remainingCapacity = 0;
   		
   		double fieldRes = getFieldConfiguration().getLatticeSiteSizeInMikron();
   		
   		double startX = (isToroidalX() ||  (cellBoundaries.getMinXInMikron() >= fieldRes)) ? cellBoundaries.getMinXInMikron() :fieldRes;
   		double stopX = (isToroidalX() || (cellBoundaries.getMaxXInMikron() <= (getSizeXInMikron()-fieldRes)))?cellBoundaries.getMaxXInMikron():(getSizeXInMikron()-fieldRes);
   		
   		double startY = (isToroidalY() ||  (cellBoundaries.getMinYInMikron() >= fieldRes)) ? cellBoundaries.getMinYInMikron() :fieldRes;
   		double stopY = (isToroidalY() || (cellBoundaries.getMaxYInMikron() <= (getSizeYInMikron()-fieldRes)))?cellBoundaries.getMaxYInMikron():(getSizeYInMikron()-fieldRes);
   		
   		double startZ = (isToroidalZ() ||  (cellBoundaries.getMinZInMikron() >= fieldRes)) ? cellBoundaries.getMinZInMikron() :fieldRes;
   		double stopZ = (isToroidalZ() || (cellBoundaries.getMaxZInMikron() <= (getSizeZInMikron()-fieldRes)))?cellBoundaries.getMaxZInMikron():(getSizeZInMikron()-fieldRes);
   		
   		for(double z = startZ; z <= stopZ;){
	   		for(double y = startY; y <= stopY;){
	   			for(double x = startX; x <= stopX;){
	      			if(cellBoundaries.contains(x, y, z)){
	      				xPos.add(x);
	      				yPos.add(y);
	      				zPos.add(z);
	      				remainingCapacity += (maxConcentration-getConcentration(x, y, z));
	      			}
	      			x+=fieldRes;
	      		}
	   			y+=fieldRes;
	   		}
	   		z+=fieldRes;
   		}
   		return remainingCapacity;
   	}
   	else return Double.POSITIVE_INFINITY;
   }
   public double getTotalLocalFreeFieldConcentration(CellBoundaries cellBoundaries, DoubleBag xPos, DoubleBag yPos, DoubleBag zPos){
   	xPos.clear();
   	yPos.clear();
   	zPos.clear();
   	double minConcentration = getFieldConfiguration().getMinimumConcentration();
   	if(minConcentration > Double.NEGATIVE_INFINITY){
   		double totalFreeCapacity = 0;
   		double fieldRes = getFieldConfiguration().getLatticeSiteSizeInMikron();
   		
   		double startX = (isToroidalX() ||  (cellBoundaries.getMinXInMikron() >= fieldRes)) ? cellBoundaries.getMinXInMikron() :fieldRes;
   		double stopX = (isToroidalX() || (cellBoundaries.getMaxXInMikron() <= (getSizeXInMikron()-fieldRes)))?cellBoundaries.getMaxXInMikron():(getSizeXInMikron()-fieldRes);
   		
   		double startY = (isToroidalY() ||  (cellBoundaries.getMinYInMikron() >= fieldRes)) ? cellBoundaries.getMinYInMikron() :fieldRes;
   		double stopY = (isToroidalY() || (cellBoundaries.getMaxYInMikron() <= (getSizeYInMikron()-fieldRes)))?cellBoundaries.getMaxYInMikron():(getSizeYInMikron()-fieldRes);
   		
   		double startZ = (isToroidalZ() ||  (cellBoundaries.getMinZInMikron() >= fieldRes)) ? cellBoundaries.getMinZInMikron() :fieldRes;
   		double stopZ = (isToroidalZ() || (cellBoundaries.getMaxZInMikron() <= (getSizeZInMikron()-fieldRes)))?cellBoundaries.getMaxZInMikron():(getSizeZInMikron()-fieldRes);
   		
   		for(double z = startZ; z <= stopZ;){
	   		for(double y = startY; y <= stopY;){
	   			for(double x = startX; x <= stopX;){
	   				if(cellBoundaries.contains(x, y, z)){
	      				xPos.add(x);
	      				yPos.add(y);
	      				zPos.add(z);
	      				totalFreeCapacity += (getConcentration(x, y, z)-minConcentration);
	      			}
	      			x+=fieldRes;
	      		}
	   			y+=fieldRes;
	   		}
	   		z+=fieldRes;
   		}
   		return totalFreeCapacity;
   	}
   	else return Double.POSITIVE_INFINITY;
   }
   
   public double getMaxConcentrationInField(){
   	return this.extraCellularField.max();
   }
   
   public double getTotalConcentrationInArea(CellBoundaries area){   	
   
   		double totalConcentration = 0;
   		
   		double fieldRes = getFieldConfiguration().getLatticeSiteSizeInMikron();
   		
   		double startX = (isToroidalX() ||  (area.getMinXInMikron() >= fieldRes)) ? area.getMinXInMikron() :fieldRes;
   		double stopX = (isToroidalX() || (area.getMaxXInMikron() <= (getSizeXInMikron()-fieldRes)))?area.getMaxXInMikron():(getSizeXInMikron()-fieldRes);
   		
   		double startY = (isToroidalY() ||  (area.getMinYInMikron() >= fieldRes)) ? area.getMinYInMikron() :fieldRes;
   		double stopY = (isToroidalY() || (area.getMaxYInMikron() <= (getSizeYInMikron()-fieldRes)))?area.getMaxYInMikron():(getSizeYInMikron()-fieldRes);
   		
   		double startZ = (isToroidalZ() ||  (area.getMinZInMikron() >= fieldRes)) ? area.getMinZInMikron() :fieldRes;
   		double stopZ = (isToroidalZ() || (area.getMaxZInMikron() <= (getSizeZInMikron()-fieldRes)))?area.getMaxZInMikron():(getSizeZInMikron()-fieldRes);
   		
   		for(double z = startZ; z <= stopZ;){
	   		for(double y = startY; y <= stopY;){
	   			for(double x = startX; x <= stopX;){
	   				if(area.contains(x, y, z)){
	   					totalConcentration += getConcentration(x, y, z);
	      			}
	      			x+=fieldRes;
	      		}
	   			y+=fieldRes;
	   		}
	   		z+=fieldRes;
   		}
   		return totalConcentration;
   }
   
   public double getInterval() {
	   return 1;
   }

	
   public double getSizeXInMikron() {
   
   	return sizeXInMikron;
   }

	
   public void setSizeXInMikron(double sizeXInMikron) {
   
   	this.sizeXInMikron = sizeXInMikron;
   }

	
   public double getSizeYInMikron() {
   
   	return sizeYInMikron;
   }

	
   public void setSizeYInMikron(double sizeYInMikron) {
   
   	this.sizeYInMikron = sizeYInMikron;
   }

	
   public double getSizeZInMikron() {
   
   	return sizeZInMikron;
   }

	
   public void setSizeZInMikron(double sizeZInMikron) {
   
   	this.sizeZInMikron = sizeZInMikron;
   }

	
   public boolean isToroidalX() {
   
   	return toroidalX;
   }

	
   public void setToroidalX(boolean toroidalX) {
   
   	this.toroidalX = toroidalX;
   }

	
   public boolean isToroidalY() {
   
   	return toroidalY;
   }

	
   public void setToroidalY(boolean toroidalY) {
   
   	this.toroidalY = toroidalY;
   }

	
   public boolean isToroidalZ() {
   
   	return toroidalZ;
   }

	
   public void setToroidalZ(boolean toroidalZ) {
   
   	this.toroidalZ = toroidalZ;
   }
   
}
