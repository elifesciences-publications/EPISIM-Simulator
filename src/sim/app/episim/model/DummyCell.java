package sim.app.episim.model;

import sim.util.Double2D;

/*
 * Sets dummy cell position
 */


import sim.util.Double3D;


public class DummyCell {
	
	private Double3D cellPosition   = new Double3D(0d,0d,0d);
	private Double2D cellPosition2D = new Double2D(0d,0d);
	private double cellWidth 		= 0;
	private double cellHeight		= 0;
	private double cellLength 		= 0;
	
	public DummyCell(Double3D position, double cellWidth, double cellHeight, double cellLength){
		this.cellPosition = position;
		this.cellWidth 	  = cellWidth; 
		this.cellHeight   = cellHeight;
		this.cellLength   = cellLength;
	}
	
	public DummyCell(Double2D position, double cellWidth, double cellHeight){
		this.cellPosition2D = position;
		this.cellWidth 	    = cellWidth; 
		this.cellHeight     = cellHeight;
	}
	
   public Double3D getCellPosition() {
   
   	return cellPosition;
   }

   public Double2D getCellPosition2D() {
   
   	return cellPosition2D;
   }
   
   public void setCellPosition(Double3D pos) {
      
   	this.cellPosition = pos != null? pos : cellPosition;
   }

   public void setCellPosition(Double2D pos) {
      
   	this.cellPosition2D = pos != null? pos : cellPosition2D;
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
