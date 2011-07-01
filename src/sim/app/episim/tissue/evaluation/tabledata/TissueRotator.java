package sim.app.episim.tissue.evaluation.tabledata;

import java.awt.geom.Point2D;

import sim.app.episim.tissue.xmlread.ImportedTissueData;

public class TissueRotator {
	
	
	public void rotateTissue(Tissue tissue, double angleInDegrees) {
     
     
/*      int width  = (int)tissue.getEpidermalWidth();
      int height = (int)tissue.getEpidermalHeight();

      double angle = Math.toRadians(360-angleInDegrees);
      double sin = Math.sin(angle);
      double cos = Math.cos(angle);
      double i0 = 0.5 * (width  - 1);     // point to rotate about
      double j0 = 0.5 * (height - 1);     // center of image

      
      // rotation
     for(CellEllipse cell: tissue.getCells()){
              double a = cell.getX() - i0;
              double b = cell.getY() - j0;
              int newX = (int) (+a * cos - b * sin + i0);
              int newY = (int) (+a * sin + b * cos + j0);
              cell.rotateCellEllipseInRadians(angle);
              cell.setXY(newX, newY);
             
              
     }
     
     double widthA = Math.abs(Math.cos(angle) * width);
     double widthB = Math.abs(Math.sin(angle) * width);
     double heightA = Math.abs(Math.cos(angle) * height);
     double heightB = Math.abs(Math.sin(angle) * height);
     
    tissue.setEpidermalWidth((int)(widthA + heightB));
    tissue.setEpidermalHeight((int)(widthB + heightA));*/
    
    xyShift(tissue);
  }
	
	private void xyShift(Tissue tissue){
		double minX = Double.POSITIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;
		
		double maxRadius = Double.NEGATIVE_INFINITY;
		
		for(Cell cell : tissue.getCells()){
			if(cell.getX() < minX) minX = cell.getX();
			if(cell.getX()> maxX) maxX = cell.getX();
			if(cell.getY() < minY) minY = cell.getY();
			if(cell.getY()> maxY) maxY = cell.getY();
			
			if(cell.getMajorAxis() > maxRadius) maxRadius = cell.getMajorAxis();
			if(cell.getMinorAxis() > maxRadius) maxRadius = cell.getMinorAxis();
		}
		
		/*double xShift = 0;
		double yShift = 0;
		
		if(minX < 0) xShift = Math.abs(minX);
		if(minY < 0) yShift = Math.abs(minY);
		
		double marginXShift = (tissue.getEpidermalWidth() - ((maxX-minX)/2));
		double marginYShift = (tissue.getEpidermalHeight() -(maxY-minY));
		System.out.println("xShift: "+ xShift);
		System.out.println("MarginXShift: "+ marginXShift);
		System.out.println("yShift: "+ yShift);
		System.out.println("MarginYShift: "+ marginYShift);*/
		
		for(Cell cell : tissue.getCells()){
			cell.setXY((int)(cell.getX()-minX+ (maxRadius/2)), (int)(cell.getY()-minY+(maxRadius/2)));
			
		}
		
		for(Point2D p : tissue.getSurfacePoints()){
			p.setLocation((p.getX()-minX+ (maxRadius/2)), (p.getY()-minY+(maxRadius/2)));
		}
		for(Point2D p : tissue.getBasalLayerPoints()){
			p.setLocation((p.getX()-minX+ (maxRadius/2)), (p.getY()-minY+(maxRadius/2)));
		}
		
		
		double calculatedHeight = (maxY - minY)+maxRadius*2;
		if(tissue.getEpidermalHeight() > calculatedHeight) tissue.setEpidermalHeight(calculatedHeight);
	}

}
