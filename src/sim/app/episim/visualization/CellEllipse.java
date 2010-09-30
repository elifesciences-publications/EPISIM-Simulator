package sim.app.episim.visualization;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import sim.app.episim.biomechanics.Vertex;
import sim.app.episim.util.CellEllipseIntersectionCalculationRegistry;
import sim.app.episim.util.EllipseIntersectionCalculatorAndClipper.XYPoints;
import sim.portrayal.DrawInfo2D;


public class CellEllipse extends AbstractCellEllipse{
	
		private int[] neighbouringCellIDs = null;
		private double solidity;
			
		private NucleusEllipse nucleus = null;
		
		private Color fillColor = null;
		
		public CellEllipse(long id, int x, int y, int majorAxis, int minorAxis, Color c){
			this(id, x, y, majorAxis, minorAxis, 0, 0, 0, 0, 0, 0, 0, null, c);
		}
		
		public CellEllipse(long id, int x, int y, int majorAxis, int minorAxis, int height, int width, int orientationInDegrees, double area, double perimeter, double solidity, double distanceToBL, int[] neighbouringCellIDs, Color c){
			super(id, x, y, majorAxis, minorAxis, height, width, orientationInDegrees, area, perimeter, distanceToBL, c);
		
			this.solidity = solidity;
			
			this.neighbouringCellIDs = neighbouringCellIDs;
			CellEllipseIntersectionCalculationRegistry.getInstance().registerCellEllipse(this);
		}
	
		public void setXY(int x, int y){
			if(this.nucleus != null){
			  int deltaX = x - this.getX();
			  int deltaY = y - this.getY();
				  
			  int newX =this.getNucleus().getX() + deltaX;
			  int newY = this.getNucleus().getY() + deltaY;
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
     
      
      private int[] getNeighbouringCellIDs(){return this.neighbouringCellIDs; }
      
      private int getNumberOfNeighbours(){ return this.neighbouringCellIDs != null ? this.neighbouringCellIDs.length :0; }
      
      
      
      
		
		
}

