package episimbiomechanics.vertexbased;

import episimbiomechanics.EpisimModelConnector;


public class EpisimVertexModelConnector extends EpisimModelConnector {

	private static final String VERSION = "2011-01-07";
	private static final String NAME = "Vertex Based Biomechanical Model";
   
	private double contractility =1500;
	private double apicalCellBondTension=15000;
	private double lateralCellBondTension=15000;
	private double basalCellBondTension=15000;
	private double elasticity =650;
	private boolean isProliferating = false;
	private boolean cellDivisionPossible = false;
	
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
	
	public EpisimVertexModelConnector(){}

	
	public double getContractility() {
	
		return contractility;
	}

	
	public void setContractility(double contractility) {
	
		this.contractility = contractility;
	}

	
	public boolean getCellDivisionPossible() {
	
		return cellDivisionPossible;
	}

	@Hidden
	public void setCellDivisionPossible(boolean cellDivisionPossible) {
	
		this.cellDivisionPossible = cellDivisionPossible;
	}

	
	public double getApicalCellBondTension() {
	
		return apicalCellBondTension;
	}

	
	public void setApicalCellBondTension(double apicalCellBondTension) {
	
		this.apicalCellBondTension = apicalCellBondTension;
	}

	
	public double getLateralCellBondTension() {
	
		return lateralCellBondTension;
	}

	
	public void setLateralCellBondTension(double lateralCellBondTension) {
	
		this.lateralCellBondTension = lateralCellBondTension;
	}

	
	public double getBasalCellBondTension() {
	
		return basalCellBondTension;
	}

	
	public void setBasalCellBondTension(double basalCellBondTension) {
	
		this.basalCellBondTension = basalCellBondTension;
	}

	
	public double getElasticity() {
	
		return elasticity;
	}

	
	public void setElasticity(double elasticity) {
	
		this.elasticity = elasticity;
	}

	
	public boolean getIsProliferating() {
	
		return isProliferating;
	}

	
	public void setIsProliferating(boolean isProliferating) {
	
		this.isProliferating = isProliferating;
	}

	
	public boolean getIsMembrane() {
	
		return isMembrane;
	}

	@Hidden
	public void setIsMembrane(boolean isMembrane) {
	
		this.isMembrane = isMembrane;
	}

	
	public boolean getIsSurface() {
	
		return isSurface;
	}

	@Hidden
	public void setIsSurface(boolean isSurface) {
	
		this.isSurface = isSurface;
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

}
