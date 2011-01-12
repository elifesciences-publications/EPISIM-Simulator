package episimbiomechanics.centerbased;

import episimbiomechanics.EpisimModelConnector;



public class EpisimCenterBasedModelConnector extends EpisimModelConnector {
	
	private static final String VERSION = "2010-05-13";
	private static final String NAME = "Center Based Biomechanical Model";
   
	private boolean hasCollision =false;
	private boolean isMembrane =false;
	private boolean isSurface = false;
	private double x;
	private double y;
	private double dx;
	private double dy;
	
	
	public String getBiomechanicalModelId(){
		return VERSION;
	}
	
	public String getBiomechanicalModelName(){
		return NAME;
	}
	
	public EpisimCenterBasedModelConnector(){}
	
	public boolean getHasCollision() {
	
		return hasCollision;
	}
	
	@Hidden
	public void setHasCollision(boolean hasCollision){
		this.hasCollision = hasCollision;
	}
	
	public boolean getIsMembrane() {
		
		return isMembrane;
	}
	
	@Hidden
	public void setIsMembrane(boolean isMembrane){
		this.isMembrane = isMembrane;
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

	
	public double getDx() {	
		return dx;
	}

	@Hidden
	public void setDx(double dx) {	
		this.dx = dx;
	}

	
	public double getDy() {	
		return dy;
	}

	@Hidden
	public void setDy(double dy) {	
		this.dy = dy;
	}
	
	public boolean getIsSurface(){
		return isSurface;
	}
	
	@Hidden
	public void setIsSurface(boolean isSurface){
		this.isSurface = isSurface;
	}
	
}