package sim.app.episim.model.diffusion;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;

import sim.app.episim.model.biomechanics.CellBoundaries;
import sim.app.episim.model.diffusion.ExtracellularDiffusionFieldBCConfig2D.BoundaryCondition;
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
	private ExtracellularDiffusionFieldBCConfig3D fieldBCConfig;
	private ForwardEulerDiffusionReaction fEulerDiffReact;
	
	public ExtraCellularDiffusionField3D(EpisimDiffusionFieldConfiguration fieldConfiguration,  ExtracellularDiffusionFieldBCConfig3D fieldBCConfig, double sizeXInMikron, double sizeYInMikron, double sizeZInMikron){
		this.sizeXInMikron = sizeXInMikron;
		this.sizeYInMikron = sizeYInMikron;
		this.sizeZInMikron = sizeZInMikron;
		this.toroidalX = fieldBCConfig.getBoundaryConditionX()==BoundaryCondition.PERIODIC;
		this.toroidalY = fieldBCConfig.getBoundaryConditionY()==BoundaryCondition.PERIODIC;
		this.toroidalZ = fieldBCConfig.getBoundaryConditionZ()==BoundaryCondition.PERIODIC;
		this.fieldConfiguration = fieldConfiguration;
		this.fieldBCConfig = fieldBCConfig;
		
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
   	
   		
   		double remainingCapacity = 0;
   		
   		double fieldRes = getFieldConfiguration().getLatticeSiteSizeInMikron();
   		
   		double startX = getMinX(cellBoundaries);
   		double stopX = getMaxX(cellBoundaries);   			
   		
   		double startY = getMinY(cellBoundaries);
   		double stopY = getMaxY(cellBoundaries);
   		
   		double startZ = getMinZ(cellBoundaries);
   		double stopZ = getMaxZ(cellBoundaries);
   		
   		for(double z = startZ; z <= stopZ;){
	   		for(double y = startY; y <= stopY;){
	   			for(double x = startX; x <= stopX;){
	      			if(cellBoundaries.contains(x, y, z)){
	      				xPos.add(x);
	      				yPos.add(y);
	      				zPos.add(z);
	      				if(maxConcentration < Double.POSITIVE_INFINITY)remainingCapacity += (maxConcentration-getConcentration(x, y, z));
	      			}
	      			x+=fieldRes;
	      		}
	   			y+=fieldRes;
	   		}
	   		z+=fieldRes;
   		}
   		if(maxConcentration < Double.POSITIVE_INFINITY)return remainingCapacity;
   		else return Double.POSITIVE_INFINITY;
   	
   	
   }
   public double getTotalLocalFreeFieldConcentration(CellBoundaries cellBoundaries, DoubleBag xPos, DoubleBag yPos, DoubleBag zPos){
   	xPos.clear();
   	yPos.clear();
   	zPos.clear();
   	double minConcentration = getFieldConfiguration().getMinimumConcentration();
   	
   		double totalFreeCapacity = 0;
   		double fieldRes = getFieldConfiguration().getLatticeSiteSizeInMikron();
   		
   		double startX = getMinX(cellBoundaries);
   		double stopX = getMaxX(cellBoundaries);
   		
   		double startY = getMinY(cellBoundaries);
   		double stopY = getMaxY(cellBoundaries);
   		
   		double startZ = getMinZ(cellBoundaries);
   		double stopZ = getMaxZ(cellBoundaries);
   		
   		for(double z = startZ; z <= stopZ;){
	   		for(double y = startY; y <= stopY;){
	   			for(double x = startX; x <= stopX;){
	   				if(cellBoundaries.contains(x, y, z)){
	      				xPos.add(x);
	      				yPos.add(y);
	      				zPos.add(z);
	      				if(minConcentration > Double.NEGATIVE_INFINITY) totalFreeCapacity += (getConcentration(x, y, z)-minConcentration);
	      			}
	      			x+=fieldRes;
	      		}
	   			y+=fieldRes;
	   		}
	   		z+=fieldRes;
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
   		
   		double startZ = getMinZ(area);
   		double stopZ = getMaxZ(area);
   		
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
   
   public double getAverageConcentrationInArea(CellBoundaries area){   	
      
		double totalConcentration = 0;
		
		double fieldRes = getFieldConfiguration().getLatticeSiteSizeInMikron();
		
		double startX = getMinX(area);
		double stopX = getMaxX(area);
		
		
		double startY = getMinY(area);
		double stopY = getMaxY(area);
		
		double startZ = getMinZ(area);
		double stopZ = getMaxZ(area);
		
		double numberOfLatticeSites = 0;
		
		for(double z = startZ; z <= stopZ;){
   		for(double y = startY; y <= stopY;){
   			for(double x = startX; x <= stopX;){
   				if(area.contains(x, y, z)){
   					totalConcentration += getConcentration(x, y, z);
   					numberOfLatticeSites+=1.0;
      			}
      			x+=fieldRes;
      		}
   			y+=fieldRes;
   		}
   		z+=fieldRes;
		}
		return numberOfLatticeSites > 0 ? (totalConcentration/numberOfLatticeSites) : totalConcentration;
}
   
   
   public Vector3d getChemotaxisVectorForCellBoundary(CellBoundaries area){		
		
		double fieldRes = getFieldConfiguration().getLatticeSiteSizeInMikron();
		
		double startX = getMinX(area);
		double stopX = getMaxX(area);
		
		double startY = getMinY(area);
		double stopY = getMaxY(area);
		
		double startZ = getMinZ(area);
		double stopZ = getMaxZ(area);
		
		double c_top_left_front=0;
		double c_top_left_back=0;
		double c_bottom_left_front=0;
		double c_bottom_left_back=0;
		double c_top_right_front=0;
		double c_top_right_back=0;
		double c_bottom_right_front=0;
		double c_bottom_right_back=0;
		
		double ls_top_left_front=0;
		double ls_top_left_back=0;
		double ls_bottom_left_front=0;
		double ls_bottom_left_back=0;
		double ls_top_right_front=0;
		double ls_top_right_back=0;
		double ls_bottom_right_front=0;
		double ls_bottom_right_back=0;
		
		double verticalReflectionAxis = startX+((stopX-startX)/2);
		double horizontalReflectionAxis = startY+((stopY-startY)/2);
		double middleReflectionAxis = startZ+((stopZ-startZ)/2);
		for(double z = startZ; z <= stopZ;){
			for(double y = startY; y <= stopY;){
				for(double x = startX; x <= stopX;){
					if(area.contains(x, y)){
						if(x < verticalReflectionAxis && y > horizontalReflectionAxis && z > middleReflectionAxis){
							c_top_left_front += getConcentration(x, y, z);
							ls_top_left_front++;
						}
						else if(x < verticalReflectionAxis && y < horizontalReflectionAxis && z > middleReflectionAxis){
							c_bottom_left_front += getConcentration(x, y, z);
							ls_bottom_left_front++;
						}
						else if(x < verticalReflectionAxis && y > horizontalReflectionAxis && z < middleReflectionAxis){
							c_top_left_back += getConcentration(x, y, z);
							ls_top_left_back++;
						}
						else if(x < verticalReflectionAxis && y < horizontalReflectionAxis && z < middleReflectionAxis){
							c_bottom_left_back += getConcentration(x, y, z);
							ls_bottom_left_back++;
						}						
						else if(x > verticalReflectionAxis && y > horizontalReflectionAxis && z > middleReflectionAxis){
							c_top_right_front += getConcentration(x, y, z);
							ls_top_right_front++;
						}
						else if(x > verticalReflectionAxis && y < horizontalReflectionAxis && z > middleReflectionAxis){
							c_bottom_right_front += getConcentration(x, y, z);
							ls_bottom_right_front++;
						}
						else if(x > verticalReflectionAxis && y > horizontalReflectionAxis && z < middleReflectionAxis){
							c_top_right_back += getConcentration(x, y, z);
							ls_top_right_back++;
						}
						else if(x > verticalReflectionAxis && y < horizontalReflectionAxis && z < middleReflectionAxis){
							c_bottom_right_back += getConcentration(x, y, z);
							ls_bottom_right_back++;
						}			
	   			}
	   			x+=fieldRes;
	   		}
				y+=fieldRes;
			}
			z+=fieldRes;
		}		
		
		c_top_left_front /= correctLatticeSiteNumber(ls_top_left_front);
		c_top_left_back /= correctLatticeSiteNumber(ls_top_left_back);
		c_top_right_front /= correctLatticeSiteNumber(ls_top_right_front);
		c_top_right_back /= correctLatticeSiteNumber(ls_top_right_back);
		
		c_bottom_left_front /= correctLatticeSiteNumber(ls_bottom_left_front);
		c_bottom_left_back /= correctLatticeSiteNumber(ls_bottom_left_back);
		c_bottom_right_front /= correctLatticeSiteNumber(ls_bottom_right_front);
		c_bottom_right_back /= correctLatticeSiteNumber(ls_bottom_right_back);		
		
		double dx = ((c_top_right_front+c_bottom_right_front+c_top_right_back+c_bottom_right_back)/4)-((c_top_left_front+c_bottom_left_front+c_top_left_back+c_bottom_left_back)/4);
		double dy = ((c_top_left_front+c_top_right_front+c_top_left_back+c_top_right_back)/4)-((c_bottom_left_front+c_bottom_right_front+c_bottom_left_back+c_bottom_right_back)/4);
		double dz = ((c_top_left_front+c_top_right_front+c_bottom_left_front+c_bottom_right_front)/4)-((c_top_left_back+c_top_right_back+c_bottom_left_back+c_bottom_right_back)/4);
		double c_max = getFieldConfiguration().getMaximumConcentration() < Double.POSITIVE_INFINITY 
				  ? getFieldConfiguration().getMaximumConcentration()
				  : getMaxConcentrationInField();
		dx /= c_max;
		dy /= c_max;
		dz /= c_max;
		return new Vector3d(dx, dy, dz);
   }
   private double correctLatticeSiteNumber(double number){
   	return number==0d?1d:number;
   }
	
   
   private double getMinX(CellBoundaries boundaries){
   	double fieldRes = getFieldConfiguration().getLatticeSiteSizeInMikron();
   	return (isToroidalX() ||  (boundaries.getMinXInMikron() >= fieldRes)) ? (boundaries.getMinXInMikron()-fieldRes) :fieldRes;
   }
   private double getMaxX(CellBoundaries boundaries){
   	double fieldRes = getFieldConfiguration().getLatticeSiteSizeInMikron();
   	return (isToroidalX() || (boundaries.getMaxXInMikron() <= (getSizeXInMikron()-fieldRes)))?(boundaries.getMaxXInMikron()+fieldRes):(getSizeXInMikron()-fieldRes);
   }
   private double getMinY(CellBoundaries boundaries){
   	double fieldRes = getFieldConfiguration().getLatticeSiteSizeInMikron();
   	return (isToroidalY() ||  (boundaries.getMinYInMikron() >= fieldRes)) ? (boundaries.getMinYInMikron()-fieldRes) :fieldRes;
   }
   private double getMaxY(CellBoundaries boundaries){
   	double fieldRes = getFieldConfiguration().getLatticeSiteSizeInMikron();
   	return (isToroidalY() || (boundaries.getMaxYInMikron() <= (getSizeYInMikron()-fieldRes)))?(boundaries.getMaxYInMikron()+fieldRes):(getSizeYInMikron()-fieldRes);
	}
   private double getMinZ(CellBoundaries boundaries){
   	double fieldRes = getFieldConfiguration().getLatticeSiteSizeInMikron();
   	return (isToroidalZ() ||  (boundaries.getMinZInMikron() >= fieldRes)) ? (boundaries.getMinZInMikron()-fieldRes) :fieldRes;
   }
   private double getMaxZ(CellBoundaries boundaries){
   	double fieldRes = getFieldConfiguration().getLatticeSiteSizeInMikron();
   	return (isToroidalZ() || (boundaries.getMaxZInMikron() <= (getSizeZInMikron()-fieldRes)))?(boundaries.getMaxZInMikron()+fieldRes):(getSizeZInMikron()-fieldRes);
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
   
   public void setToValue(double value) {	   
	   extraCellularField.setTo(value);
   }

	
   public ExtracellularDiffusionFieldBCConfig3D getFieldBCConfig3D() {
   
   	return fieldBCConfig;
   }
   public ExtracellularDiffusionFieldBCConfig2D getFieldBCConfig() {
      
   	return fieldBCConfig;
   }
}
