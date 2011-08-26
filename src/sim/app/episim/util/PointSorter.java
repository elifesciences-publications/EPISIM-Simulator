package sim.app.episim.util;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.Comparator;


public class PointSorter {
	
	private FPoint[] points;
	
	public PointSorter(Point2D[] points){
		Arrays.sort(points, new Comparator<Point2D>(){
			public int compare(Point2D o1, Point2D o2) {	         
	         return (new Double(o1.getX()).compareTo(new Double(o2.getX())));
         }});
		this.points = new FPoint[points.length];
		for(int i = 0; i < points.length; i++) this.points[i] = new FPoint(points[i]);
	}
	
	/*private void swap(int i, int j){
		Point2D tmp = points[i];
		points[i] = points[j];
		points[j] = tmp;
	}
	
	
	private int bubbleSort(){
	
		int swaps = 0;
		for(int i = 0; i < (points.length-1); i++){
			double distance1 = Double.POSITIVE_INFINITY, distance2 = Double.POSITIVE_INFINITY;
			int position = 0;
			if(i==0){
				distance1 = points[i].distance(points[(i+1)%points.length]);
				distance2 = points[i].distance(points[(i+2)%points.length]);
			}
			else if(i==(points.length-1)){
				//do nothing
			}
			else{
				distance1 = points[i].distance(points[(i+1)%points.length])+points[i].distance(points[(i-1)%points.length]);
				distance2 = points[i].distance(points[(i+1)%points.length])+points[i].distance(points[(i+2)%points.length]);
			}
			if(distance2 < distance1){
				swaps++;			
				swap(i, ((i+1) % points.length));
			}					
		}
		return swaps;
	}*/
	
	
	public Point2D[] getSortedPoints(){
		FPoint[] newPoints = new FPoint[points.length];
		
		newPoints[0] = getStartPoint();
		for(int i = 1; i < points.length; i++) newPoints[i] = getNextPoint(newPoints[i-1]);
		
		return newPoints;
	}
	
	
	
	 
	   private FPoint getStartPoint() {
	      /*double yB = 0;
	        FPoint pB = null;
	        for (final FPoint point : points) {
	            if (!point.isVisited() && point.y > yB) {
	                yB = point.y;
	                pB = point;
	            }
	        }
	        if (pB != null) {
	            pB.setVisited(true);
	        }
	        return pB;*/
	   	points[0].setVisited(true);
	   	return points[0];
	  }
	 
	  private FPoint getNextPoint(final FPoint p1) {
	        if(p1 == null){
	            throw new IllegalArgumentException("Argument must not be null");
	        }
	        FPoint pN = null;
	        double distanceN = Double.MAX_VALUE;
	        for (final FPoint point : points) {
	            if (!point.isVisited()) {
	                double distance = point.distance(p1);
	                if (distance < distanceN) {
	                    distanceN = distance;
	                    pN = point;
	                }
	            }
	        }
	        if (pN != null) {
	            pN.setVisited(true);
	        }
	        return pN;
	 }
	 
	
		 
	private class FPoint extends Point2D.Double {	 
	    private boolean visted;
	 
	    public FPoint(Point2D point) {   	 
	        super(point.getX(), point.getY());
	    }
	 
	    public boolean isVisited() {
	        return visted;
	    }
	 
	    public void setVisited(final boolean visited) {
	        this.visted = visited;
	    }
	}	

}
