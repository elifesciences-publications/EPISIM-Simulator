package sim.app.episim.devBasalLayer;

import java.awt.geom.Point2D;
import java.util.Comparator;


public class InterpolationPointComparator implements Comparator<Point2D>{

	public int compare(Point2D o1, Point2D o2) {

		if(o1.getX() < o2.getX()) return -1;
		else if(o1.getX() > o2.getX()) return 1;
		else if(o1.getX() == o2.getX()) return 0;
		return 0;
	}

}
