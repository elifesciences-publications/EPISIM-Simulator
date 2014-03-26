package episimbiomechanics.centerbased.newversion;

import sim.app.episim.AbstractCell;
import episimbiomechanics.EpisimModelConnector;
import episiminterfaces.NoExport;


public abstract class EpisimCenterBasedMC extends EpisimModelConnector {
	
	
   
	private boolean hasCollision =false;
	private boolean isBasal =false;
	private boolean isSurface = false;
	private double epidermalSurfaceRatio=0;
	private double x=0;
	private double y=0;
	private double width=0;
	private double height=0;
	private double length = 0;
	private int numberOfSecondsPerSimStep=1;
	
	private double adhesionBasalMembrane=0;
	
		
	
	public boolean getHasCollision() {
	
		return hasCollision;
	}
	
	@Hidden
	public void setHasCollision(boolean hasCollision){
		this.hasCollision = hasCollision;
	}
	
	public boolean getIsBasal() {
		
		return isBasal;
	}
	
	@Hidden
	public void setIsBasal(boolean isBasal){
		this.isBasal = isBasal;
	}

	
	public double getX() {	
		return x;
	}

	@Hidden
	public void setX(double x) {	
		this.x = x;
	}

	
	public double getY() {	
		return y;
	}

	@Hidden
	public void setY(double y) {	
		this.y = y;
	}
	
	public boolean getIsSurface(){
		return isSurface;
	}
	
	@Hidden
	public void setIsSurface(boolean isSurface){
		this.isSurface = isSurface;
	}
	
	public double getWidth() {	
		return width;
	}
	public void setWidth(double width) {	
		this.width = width;
	}
	
	public double getHeight() {	
		return height;
	}
	public void setHeight(double height) {	
		this.height = height;
	}
	
	public double getLength() {	
		return length;
	}
	public void setLength(double length) {	
		this.length = length;
	}
   
	@Hidden
	@NoExport
   public abstract double getAdhesionFactorForCell(AbstractCell cell);

	
   public int getNumberOfSecondsPerSimStep() {   
   	return numberOfSecondsPerSimStep;
   }

	
   public void setNumberOfSecondsPerSimStep(int numberOfSecondsPerSimStep) {   
   	this.numberOfSecondsPerSimStep = numberOfSecondsPerSimStep;
   }
   
   public double getAdhesionBasalMembrane() {      
   	return adhesionBasalMembrane;
   }	
   public void setAdhesionBasalMembrane(double adhesionBasalMembrane) {   
   	this.adhesionBasalMembrane = adhesionBasalMembrane;
   }

	
   public double getEpidermalSurfaceRatio() {
   
   	return epidermalSurfaceRatio;
   }

   @Hidden
   public void setEpidermalSurfaceRatio(double epidermalSurfaceRatio) {
   
   	this.epidermalSurfaceRatio = epidermalSurfaceRatio;
   }	
}
