package sim.app.episim.devBasalLayer;


import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class BasementMembraneDev {
	private static int basalY=80;          // y coordinate at which undulations start, the base line    
	private static int basalPeriod=70;      // width of an undulation at the foot
	
	private ArrayList<Point2D> membranePoints;
	
	private HashMap<Double, HashSet<Double>> organizedXPoints;
	
	private GeneralPath polygon;
	
	private GeneralPath drawPolygon;
	
	private static final int THRESHHOLD = 5;
	
	private static  BasementMembraneDev instance;
	
	
	
	private BasementMembraneDev(){
		membranePoints = new ArrayList<Point2D>();
		polygon = new GeneralPath();
		organizedXPoints = new HashMap<Double, HashSet<Double>>();
	}
	
	
	private static  double width =140-2;
	
	
	
	public static double getWidth(){
		return width;
	}
	
	public static double lowerBound(double x)
	 {
	    return -1;
	    
	 }
	public void loadBasementMembrane(File path) {

		if(path != null){

			List<Point2D> tmpMembranePoints = BasalLayerReader.getInstance().loadBasalLayer(path);
			if(!(tmpMembranePoints instanceof ArrayList)){
				ArrayList<Point2D> membranePoints = new ArrayList<Point2D>();
				membranePoints.addAll(tmpMembranePoints);
				this.membranePoints = membranePoints;
			}
			else
				this.membranePoints = (ArrayList<Point2D>) tmpMembranePoints;
			if(this.membranePoints.size() > 0){

				polygon = new GeneralPath();
				polygon.moveTo(this.membranePoints.get(0).getX(), this.membranePoints.get(0).getY());
				for(int i = 0; i < this.membranePoints.size(); i++){

					polygon.lineTo(this.membranePoints.get(i).getX(), this.membranePoints.get(i).getY());

				}
				drawPolygon = (GeneralPath)polygon.clone();
				polygon.lineTo(polygon.getBounds().getMinX(), polygon.getBounds().getMinY());
				polygon.closePath();
				
				organizePoints();
			}
		}

	}
	
	public static synchronized BasementMembraneDev getInstance(){
		if(instance == null) instance =  new BasementMembraneDev();
		return instance;
	}
	
	public GeneralPath getBasementMembraneDrawPolygon(){
		return drawPolygon;
		
	}
	
	public boolean isOverBasalLayer(Point2D point){
		if(polygon != null && (polygon.contains(point) ||
				(point.getX() >= polygon.getBounds().getMinX() && point.getX()<= polygon.getBounds().getMaxX()
						&& point.getY() >=0 && point.getY() <= polygon.getBounds().getMinY())))return true;
		else return false;
	}
		      
	private void organizePoints(){
		for(Point2D actPoint: membranePoints){
			if(organizedXPoints.containsKey(actPoint.getX()))
				organizedXPoints.get(actPoint.getX()).add(actPoint.getY());
			else{
				HashSet<Double> tmp =new HashSet<Double>();
				tmp.add(actPoint.getY());
				organizedXPoints.put(actPoint.getX(), tmp);
			}
		}
		
	}
		
	public Set<Double> getYCoordinateForXCoordinate(double x){
		
		if(organizedXPoints.containsKey(x)) return organizedXPoints.get(x);
		else return new HashSet<Double>();
	}

	
}