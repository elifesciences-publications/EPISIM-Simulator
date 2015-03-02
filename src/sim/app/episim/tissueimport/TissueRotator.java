package sim.app.episim.tissueimport;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import sim.app.episim.visualization.CellEllipse;

public class TissueRotator {
	
	
	public void rotateTissue(ImportedTissue tissue, double angleInDegrees) {
     
     
    double width  = tissue.getEpidermalWidth();
     double height = tissue.getEpidermalHeight();
     angleInDegrees = 90 < angleInDegrees ? (180-angleInDegrees) : (360-angleInDegrees);
     double angle = Math.toRadians(angleInDegrees);
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
       cell.rotateCellEllipseInRadians(cell.getOrientationInRadians()+ angle);
       cell.setXY(newX, newY);         
     }
    for(Point2D p : tissue.getSurfacePoints()){
   	  double a = p.getX() - i0;
        double b = p.getY() - j0;
        double newX = (+a * cos - b * sin + i0);
        double newY = (+a * sin + b * cos + j0);
		  p.setLocation(newX, newY);
    }
    for(Point2D p : tissue.getBasalLayerPoints()){
		double a = p.getX() - i0;
      double b = p.getY() - j0;
      double newX = (+a * cos - b * sin + i0);
      double newY = (+a * sin + b * cos + j0);
      p.setLocation(newX, newY);			
	}
    
     
     
     double widthA = Math.abs(Math.cos(angle) * width);
     double widthB = Math.abs(Math.sin(angle) * width);
     double heightA = Math.abs(Math.cos(angle) * height);
     double heightB = Math.abs(Math.sin(angle) * height);
     
    tissue.setEpidermalWidth((int)(widthA + heightB));
    tissue.setEpidermalHeight((int)(widthB + heightA));
    
    xyShift(tissue);
  }
	
	private void xyShift(ImportedTissue tissue){
		double minX = Double.POSITIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;
		final double margin = 25;
	
		
		for(CellEllipse cell : tissue.getCells()){
			if(cell.getX() < minX) minX = cell.getX();
			if(cell.getX()> maxX) maxX = cell.getX();
			if(cell.getY() < minY) minY = cell.getY();
			if(cell.getY()> maxY) maxY = cell.getY();
			
			
		}
		ArrayList<Point2D> points = new ArrayList<Point2D>();
		points.addAll(tissue.getSurfacePoints());
		points.addAll(tissue.getBasalLayerPoints());
		for(Point2D p : points){
			if(p.getX() < minX) minX = p.getX();
			if(p.getX()> maxX) maxX = p.getX();
			if(p.getY() < minY) minY = p.getY();
			if(p.getY()> maxY) maxY = p.getY();
		}
		double calculatedHeight = (maxY - minY);
		if(tissue.getEpidermalHeight() > calculatedHeight) tissue.setEpidermalHeight(calculatedHeight);
		
		minX-=margin;
		minY-=margin;
		
		
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
		
		for(CellEllipse cell : tissue.getCells()){			
			cell.setXY((int)(cell.getX()-minX), (int)(cell.getY()-minY));			
		}
		
		for(Point2D p : tissue.getSurfacePoints()){
			
			p.setLocation((p.getX()-minX), (p.getY()-minY));
		}
		for(Point2D p : tissue.getBasalLayerPoints()){
			p.setLocation((p.getX()-minX), (p.getY()-minY));
		}
		
		
	
	}

}
