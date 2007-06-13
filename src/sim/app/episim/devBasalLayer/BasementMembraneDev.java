package sim.app.episim.devBasalLayer;


import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class BasementMembraneDev {
	private static int basalY=80;          // y coordinate at which undulations start, the base line    
	private static int basalPeriod=70;      // width of an undulation at the foot
	
	private ArrayList<Point2D> membranePoints;
	
	
	private GeneralPath polygon;
	
	
	private static final int THRESHHOLD = 5;
	
	private static  BasementMembraneDev instance;
	
	
	
	private BasementMembraneDev(){
		membranePoints = new ArrayList<Point2D>();
		
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
				polygon.closePath();
			}
		}

	}
	
	public static synchronized BasementMembraneDev getInstance(){
		if(instance == null) instance =  new BasementMembraneDev();
		return instance;
	}
	public List<Point2D> getBasementMembranePoints(){
		return membranePoints;
		
	}
	
	public boolean isOverBasalLayer(Point2D point){
		if(polygon != null && polygon.contains(point))return true;
		else return false;
	}
		      
		
		

	
}