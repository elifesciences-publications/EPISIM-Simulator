package sim.app.episim.model.biomechanics.centerbased3d.fisheye;

import sim.util.Double3D;


public class DummyCell {
	
	private Double3D cellPosition = new Double3D(0d,0d,0d);
	private double cellWidth = 0;
	private double cellHeight = 0;
	private double cellLength = 0;
	
	public DummyCell(Double3D position, double cellWidth, double cellHeight, double cellLength){
		this.cellPosition = position;
		this.cellWidth = cellWidth; 
		this.cellHeight = cellHeight;
		this.cellLength = cellLength;
	}

	
   public Double3D getCellPosition() {
   
   	return cellPosition;
   }
   
   public void setCellPosition(Double3D pos) {
      
   	this.cellPosition= pos != null? pos : cellPosition;
   }

	
   public double getCellWidth() {
   
   	return cellWidth;
   }

	
   public double getCellHeight() {
   
   	return cellHeight;
   }

	
   public double getCellLength() {
   
   	return cellLength;
   }
}
