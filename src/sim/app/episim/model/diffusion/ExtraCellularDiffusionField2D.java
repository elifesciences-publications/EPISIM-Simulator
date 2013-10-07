package sim.app.episim.model.diffusion;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import javax.vecmath.Vector2d;

import ec.util.MersenneTwisterFast;
import episiminterfaces.EpisimDiffusionFieldConfiguration;
import sim.app.episim.model.biomechanics.CellBoundaries;
import sim.app.episim.util.EnhancedSteppable;
import sim.engine.SimState;
import sim.field.grid.DoubleGrid2D;
import sim.util.DoubleBag;


public class ExtraCellularDiffusionField2D implements ExtraCellularDiffusionField{
	
	private DoubleGrid2D extraCellularField;
	
	private double widthInMikron;
	private double heightInMikron;
	 
	private boolean toroidalX;
	private boolean toroidalY;
	private EpisimDiffusionFieldConfiguration fieldConfiguration;
	private ForwardEulerDiffusionReaction fEulerDiffReact;
	
	public ExtraCellularDiffusionField2D(EpisimDiffusionFieldConfiguration fieldConfiguration, double widthInMikron, double heightInMikron, boolean toroidalX, boolean toroidalY){
		this.widthInMikron = widthInMikron;
		this.heightInMikron = heightInMikron;
		this.toroidalX = toroidalX;
		this.toroidalY = toroidalY;
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
   
	
   public boolean isToroidalX() {   
   	return toroidalX;
   }
	
   public void setToroidalX(boolean toroidalX) {   
   	this.toroidalX = toroidalX;
   }
   
   public boolean isToroidalY() {   
   	return toroidalY;
   }
	
   public void setToroidal(boolean toroidalY) {   
   	this.toroidalY = toroidalY;
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
   	int x = mikronToIntPos(xInMikron);
   	int y = mikronToIntPos(yInMikron);
   	if(isToroidalX() && isToroidalY()){
   		return extraCellularField.get(extraCellularField.stx(x), extraCellularField.sty(y));
   	}
   	else if(!isToroidalX() && isToroidalY()){
   		if(x < extraCellularField.getWidth()){   
      		return extraCellularField.get(x, extraCellularField.sty(y));
      	}
      	else return 0;
   	}
   	else if(isToroidalX() && !isToroidalY()){
   		if(y < extraCellularField.getHeight()){   
      		return extraCellularField.get(extraCellularField.stx(x), y);
      	}
      	else return 0;
   	}
   	else if(!isToroidalX() && !isToroidalY()){
   		if(x < extraCellularField.getWidth() && y < extraCellularField.getHeight()){   
      		return extraCellularField.get(x, y);
      	}
      	else return 0;
   	}
   	return 0;
   }
   
   private int mikronToIntPos(double posInMikron){
   	return (int)(posInMikron/ fieldConfiguration.getLatticeSiteSizeInMikron());
   }
   
   private boolean shouldReturnZero(int x, int y){
   	if(!toroidalX && !toroidalY && (x==0 || y==0 || x ==(extraCellularField.getWidth()-1) || y ==(extraCellularField.getHeight()-1))){
   		return true;
   	}
   	else if(!toroidalX && (x==0 || x ==(extraCellularField.getWidth()-1))){
   		return true;
   	}
   	else if(!toroidalY && (y==0 ||y ==(extraCellularField.getHeight()-1))){
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
   public double addConcentration(double xInMikron, double yInMikron, double concentrationToBeAdded){
   	int x = mikronToIntPos(xInMikron);
   	int y = mikronToIntPos(yInMikron);
   	if(shouldReturnZero(x, y)) return 0;
   	double concentration = extraCellularField.get(extraCellularField.stx(x), extraCellularField.sty(y));
   	if((concentration+concentrationToBeAdded) > this.fieldConfiguration.getMaximumConcentration()){
   		concentrationToBeAdded = this.fieldConfiguration.getMaximumConcentration() - concentration;
   	}
   	extraCellularField.set(extraCellularField.stx(x), extraCellularField.sty(y),(concentration+concentrationToBeAdded));
   	return concentrationToBeAdded;
   }
   /**
    * 
    * @param xInMikron
    * @param yInMikron
    *	@param concentrationToBeRemoved
    * @return the concentration that was in fact removed
    */
   public double removeConcentration(double xInMikron, double yInMikron, double concentrationToBeRemoved){
   	int x = mikronToIntPos(xInMikron);
   	int y = mikronToIntPos(yInMikron);   	
   	if(shouldReturnZero(x, y)) return 0;
   	double concentration = extraCellularField.get(extraCellularField.stx(x), extraCellularField.sty(y));
   	if((concentration-concentrationToBeRemoved) < this.fieldConfiguration.getMinimumConcentration()){
   		concentrationToBeRemoved = concentration - this.fieldConfiguration.getMinimumConcentration();
   	}
   	extraCellularField.set(extraCellularField.stx(x), extraCellularField.sty(y),(concentration-concentrationToBeRemoved));
   	return concentrationToBeRemoved;
   }
   
  
   
   public double getInterval() {
	   return 1;
   }
   
   public double getTotalLocalFieldRemainingCapacity(CellBoundaries cellBoundaries, DoubleBag xPos, DoubleBag yPos){
   	xPos.clear();
   	yPos.clear();
   	
   	double maxConcentration = getFieldConfiguration().getMaximumConcentration();
   	
   		double remainingCapacity = 0;
   		
   		double fieldRes = getFieldConfiguration().getLatticeSiteSizeInMikron();
   		
   		double startX = getMinX(cellBoundaries);
   		double stopX = getMaxX(cellBoundaries);
   		
   		double startY = getMinY(cellBoundaries);
   		double stopY = getMaxY(cellBoundaries);
   		
   		for(double y = startY; y <= stopY;){
   			for(double x = startX; x <= stopX;){
      			if(cellBoundaries.contains(x, y)){
      				xPos.add(x);
      				yPos.add(y);
      				if(maxConcentration < Double.POSITIVE_INFINITY) remainingCapacity += (maxConcentration-getConcentration(x, y));
      			}
      			x+=fieldRes;
      		}
   			y+=fieldRes;
   		}
   		if(maxConcentration < Double.POSITIVE_INFINITY)return remainingCapacity;
   		else return Double.POSITIVE_INFINITY;   	
   }
   public double getTotalLocalFreeFieldConcentration(CellBoundaries cellBoundaries, DoubleBag xPos, DoubleBag yPos){
   	xPos.clear();
   	yPos.clear();
   	double minConcentration = getFieldConfiguration().getMinimumConcentration();
   	
   		double totalFreeCapacity = 0;
   		
   		double fieldRes = getFieldConfiguration().getLatticeSiteSizeInMikron();
   		
   		double startX = getMinX(cellBoundaries);
   		double stopX = getMaxX(cellBoundaries);
   		
   		double startY = getMinY(cellBoundaries);
   		double stopY = getMaxY(cellBoundaries);
   		
   		for(double y = startY; y <= stopY;){
   			for(double x = startX; x <= stopX;){
   				if(cellBoundaries.contains(x, y)){
      				xPos.add(x);
      				yPos.add(y);
      				if(minConcentration > Double.NEGATIVE_INFINITY)totalFreeCapacity += (getConcentration(x, y)-minConcentration);
      			}
      			x+=fieldRes;
      		}
   			y+=fieldRes;
   		}
   		if(minConcentration > Double.NEGATIVE_INFINITY)return totalFreeCapacity;
   		else return Double.POSITIVE_INFINITY;
   	
   	
   }
   
   public double getMaxConcentrationInField(){
   	return this.extraCellularField.max();
   }
   
   public double getTotalConcentrationInArea(CellBoundaries area){   	
   
   		double totalConcentration = 0;
   		
   		double fieldRes = getFieldConfiguration().getLatticeSiteSizeInMikron();
   		
   		double startX = getMinX(area);
   		double stopX = getMaxX(area);
   		
   		double startY = getMinY(area);
   		double stopY = getMaxY(area);
   		
   		for(double y = startY; y <= stopY;){
   			for(double x = startX; x <= stopX;){
   				if(area.contains(x, y)){
   					totalConcentration += getConcentration(x, y);
      			}
      			x+=fieldRes;
      		}
   			y+=fieldRes;
   		}
   		return totalConcentration;
   }
   
   public double getAverageConcentrationInArea(CellBoundaries area){   	
      
		double totalConcentration = 0;
		
		double fieldRes = getFieldConfiguration().getLatticeSiteSizeInMikron();
		
		double startX = getMinX(area);
		double stopX = getMaxX(area);
		
		double startY = getMinY(area);
		double stopY = getMaxY(area);
		
		double numberOfLatticeSites = 0;
		
		for(double y = startY; y <= stopY;){
			for(double x = startX; x <= stopX;){
				if(area.contains(x, y)){
					totalConcentration += getConcentration(x, y);
					numberOfLatticeSites+=1.0;
   			}
   			x+=fieldRes;
   		}
			y+=fieldRes;
		}
		return numberOfLatticeSites > 0 ? (totalConcentration/numberOfLatticeSites) : totalConcentration;
   }
   public Vector2d getChemotaxisVectorForCellBoundary(CellBoundaries area){		
		
		double fieldRes = getFieldConfiguration().getLatticeSiteSizeInMikron();
		
		double startX = getMinX(area);
		double stopX = getMaxX(area);
		
		double startY = getMinY(area);
		double stopY = getMaxY(area);
		
		double topLeftConcentration=0;
		double bottomLeftConcentration=0;
		double topRightConcentration=0;
		double bottomRightConcentration=0;
		
		double topLeftLatticeSites=0;
		double bottomLeftLatticeSites=0;
		double topRightLatticeSites=0;
		double bottomRightLatticeSites=0;
		
		double verticalReflectionAxis = startX+((stopX-startX)/2);
		double horizontalReflectionAxis = startY+((stopY-startY)/2);
		
		for(double y = startY; y <= stopY;){
			for(double x = startX; x <= stopX;){
				if(area.contains(x, y)){
					if(x < verticalReflectionAxis && y < horizontalReflectionAxis){
						topLeftConcentration += getConcentration(x, y);
						topLeftLatticeSites++;
					}
					else if(x < verticalReflectionAxis && y > horizontalReflectionAxis){
						bottomLeftConcentration += getConcentration(x, y);
						bottomLeftLatticeSites++;
					}
					else if(x > verticalReflectionAxis && y < horizontalReflectionAxis){
						topRightConcentration += getConcentration(x, y);
						topRightLatticeSites++;
					}
					else if(x > verticalReflectionAxis && y > horizontalReflectionAxis){
						bottomRightConcentration += getConcentration(x, y);
						bottomRightLatticeSites++;
					}
   			}
   			x+=fieldRes;
   		}
			y+=fieldRes;
		}
		
		topLeftLatticeSites = topLeftLatticeSites==0 ? 1 : topLeftLatticeSites;
		bottomLeftLatticeSites = bottomLeftLatticeSites==0 ? 1 : bottomLeftLatticeSites;
		topRightLatticeSites = topRightLatticeSites==0 ? 1 : topRightLatticeSites;
		bottomRightLatticeSites = bottomRightLatticeSites==0 ? 1: bottomRightLatticeSites;
		
		
		topLeftConcentration /= topLeftLatticeSites;
		bottomLeftConcentration /= bottomLeftLatticeSites;
		topRightConcentration /= topRightLatticeSites;
		bottomRightConcentration /= bottomRightLatticeSites;
		
		
		double dx = ((topRightConcentration+bottomRightConcentration)/2)-((topLeftConcentration+bottomLeftConcentration)/2);
		double dy = ((topLeftConcentration+topRightConcentration)/2)-((bottomLeftConcentration+bottomRightConcentration)/2);
		double c_max = getFieldConfiguration().getMaximumConcentration() < Double.POSITIVE_INFINITY 
				  ? getFieldConfiguration().getMaximumConcentration()
				  : getMaxConcentrationInField();
		dx /= c_max;
		dy /= c_max;
		return new Vector2d(dx, dy);
   }
	
   private double getMinX(CellBoundaries boundaries){
   	double fieldRes = getFieldConfiguration().getLatticeSiteSizeInMikron();
   	return (isToroidalX() ||  (boundaries.getMinXInMikron() >= fieldRes)) ? boundaries.getMinXInMikron() :fieldRes;
   }
   private double getMaxX(CellBoundaries boundaries){
   	double fieldRes = getFieldConfiguration().getLatticeSiteSizeInMikron();
   	return (isToroidalX() || (boundaries.getMaxXInMikron() <= (getWidthInMikron()-fieldRes)))?boundaries.getMaxXInMikron():(getWidthInMikron()-fieldRes);
   }
   private double getMinY(CellBoundaries boundaries){
   	double fieldRes = getFieldConfiguration().getLatticeSiteSizeInMikron();
   	return (isToroidalY() ||  (boundaries.getMinYInMikron() >= fieldRes)) ? boundaries.getMinYInMikron() :fieldRes;
   }
   private double getMaxY(CellBoundaries boundaries){
   	double fieldRes = getFieldConfiguration().getLatticeSiteSizeInMikron();
   	return (isToroidalY() || (boundaries.getMaxYInMikron() <= (getHeightInMikron()-fieldRes)))?boundaries.getMaxYInMikron():(getHeightInMikron()-fieldRes);
   }
   
   
   public void setToValue(double value) {	   
	   extraCellularField.setTo(value);
   }
   
}
