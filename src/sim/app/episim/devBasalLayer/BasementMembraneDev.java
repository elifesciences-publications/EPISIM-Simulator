package sim.app.episim.devBasalLayer;


import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class BasementMembraneDev {
	private static int basalY=80;          // y coordinate at which undulations start, the base line    
	private static int basalPeriod=70;      // width of an undulation at the foot
	
	private List<Point2D> membranePoints;
	
	private static final int LOOKBACKSIZE = 3;
	
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
	public void loadBasementMembrane(File path){
		if(path != null)membranePoints = BasalLayerReader.getInstance().loadBasalLayer(path);
	}
	public static synchronized BasementMembraneDev getInstance(){
		if(instance == null) instance =  new BasementMembraneDev();
		return instance;
	}
	public List<Point2D> getFilteredBasementMembranePoints(){
		return membranePoints;
	}
	
}