package sim.app.episim.model.visualization;

import java.awt.Color;



import sim.app.episim.util.CellEllipseIntersectionCalculationRegistry;


public class CellEllipse extends AbstractCellEllipse{
	
		private int[] neighbouringCellIDs = null;
		private double solidity;
			
		private NucleusEllipse nucleus = null;
		
		private Color fillColor = null;
		
		public CellEllipse(long id, double x, double y, double majorAxis, double minorAxis, Color c){
			this(id, x, y, majorAxis, minorAxis, 0, 0, 0, 0, 0, 0, 0, null, c);
		}
		
		public CellEllipse(long id, double x, double y, double majorAxis, double minorAxis, double height, double width, double orientationInDegrees, double area, double perimeter, double solidity, double distanceToBL, int[] neighbouringCellIDs, Color c){
			super(id, x, y, majorAxis, minorAxis, height, width, orientationInDegrees, area, perimeter, distanceToBL, c);
		
			this.solidity = solidity;
			
			this.neighbouringCellIDs = neighbouringCellIDs;
			CellEllipseIntersectionCalculationRegistry.getInstance().registerCellEllipse(this);
		}
	
		public void setXY(double x, double y){
			if(this.nucleus != null){
			  double deltaX = x - this.getX();
			  double deltaY = y - this.getY();
				  
			  double newX =this.getNucleus().getX() + deltaX;
			  double newY = this.getNucleus().getY() + deltaY;
			  this.getNucleus().setXY(newX, newY);			  
			  super.setXY(x, y);
			}
			else super.setXY(x, y);
		}
		
		
      
      public Color getFillColor() { 
      	int neighbourNo = CellEllipseIntersectionCalculationRegistry.getInstance().getNeighbourNumber(this.getId());
      	if(fillColor == null){
      		if(neighbourNo <= 3) return Color.WHITE;
      		else if(neighbourNo == 4) return Color.GREEN;
      		else if(neighbourNo == 5) return Color.YELLOW;
      		else if(neighbourNo == 6) return Color.GRAY;
      		else if(neighbourNo == 7) return Color.BLUE;
      		else if(neighbourNo == 8) return Color.RED;
      		else if(neighbourNo >= 9) return Color.PINK;
      	}
      	else return fillColor; 
      	
      	return null;
      }
		
      public void setFillColor(Color fillColor) { this.fillColor = fillColor; }
      
      public NucleusEllipse getNucleus() { return nucleus; }

      public void setNucleus(NucleusEllipse nucleus) { this.nucleus = nucleus; }
     
		
      public double getSolidity() { return solidity; }

		public void setSolidity(double solidity) { this.solidity = solidity; }		
     
      
      private int[] getNeighbouringCellIDs(){ return this.neighbouringCellIDs; }
      
      private int getNumberOfNeighbours(){ return this.neighbouringCellIDs != null ? this.neighbouringCellIDs.length :0; }
      
      
      
      
		
		
}

