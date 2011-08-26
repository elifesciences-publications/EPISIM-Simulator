package sim.app.episim.util;

import java.awt.geom.Point2D;



public class SimulatedAnnealingForOrderingPoints {

	 public static final double DELTA = 0.99;
	 public static final double STARTING_TEMPERATURE = 10;
	
	 private Point2D[] points;

	  /**
	   * The current temperature.
	   */
	  private double temperature;

	  /**
	   * The length of the current path.
	   */
	  private double pathlength;

	  /**
	   * The length of the best path.
	   */
	  private double minimallength;

	  /**
	   * The current order of vertices.
	   */
	  private int order[];

	  /**
	   * The best order of vertices.
	   */
	  private int minimalorder[];

	  /**
	   * Constructor
	   *
	   * @param the vertices to be optimized
	   */
	  public SimulatedAnnealingForOrderingPoints(Point2D[] points)
	  {
	    this.points = points;
	    order = new int[points.length];
	    minimalorder = new int[points.length];
	  }

	  /**
	   * Called to determine if annealing should take place.
	   *
	   * @param d The distance.
	   * @return True if annealing should take place.
	   */
	  private boolean anneal(double d)
	  {
	    if (temperature < 1.0E-4) {
	      if (d > 0.0)
	        return true;
	      else
	        return false;
	    }
	    if (Math.random() < Math.exp(d / temperature))
	      return true;
	    else
	      return false;
	  }



	  /**
	   * Used to ensure that the passed in integer is within thr city range.
	   *
	   * @param i A vertex index.
	   * @return A vertex index that will be less than VERTEX_COUNT
	   */
	  private int mod(int i)
	  {
	    return i % points.length;
	  }

	  /**
	   * Run as a background thread. This method is called to
	   * perform the simulated annealing.
	   */
	  public Point2D[] sortVertices()
	  {
	    int cycle=1;
	    int sameCount = 0;
	    temperature = STARTING_TEMPERATURE;

	    initorder(order);
	    initorder(minimalorder);

	    pathlength = length();
	    minimallength = pathlength;


	    while (sameCount<50) {
	      
	     

	      // make adjustments to vertex order(annealing)
	      for (int j2 = 0; j2 < points.length * points.length; j2++) {
	        int i1 = (int)Math.floor((double)points.length * Math.random());
	        int j1 = (int)Math.floor((double)points.length * Math.random());
	        double d = getError(i1, i1 + 1) + getError(j1, j1 + 1) - getError(i1, j1) - getError(i1 + 1, j1 + 1);
	        if (anneal(d)) {
	          if (j1 < i1) {
	            int k1 = i1;
	            i1 = j1;
	            j1 = k1;
	          }
	          for (; j1 > i1; j1--) {
	            int i2 = order[i1 + 1];
	            order[i1 + 1] = order[j1];
	            order[j1] = i2;
	            i1++;
	          }
	        }
	      }

	      // See if this improved anything
	      pathlength = length();
	      if (pathlength < minimallength) {
	        minimallength = pathlength;
	        for (int k2 = 0; k2 < points.length; k2++)
	          minimalorder[k2] = order[k2];
	        sameCount=0;
	      } else
	        sameCount++;
	      temperature = DELTA * temperature;
	      cycle++;
	    }

	    // we're done
	    //System.out.println("Solution found after " + cycle + " cycles." );
	    
	    return getSortedVertexArray();
	  }

	  /**
	   * Return the length of the current path through
	   * the vertices.
	   *
	   * @return The length of the current path through the vertices.
	   */
	  private double length()
	  {
	    double d = 0.0;
	    for (int i = 1; i <= points.length; i++)
	      d += getError(i, i - 1);
	    return d;
	  }

	  /**
	   * Set the specified array to have a list of the vertices in
	   * order.
	   *
	   * @param an An array to hold the vertices.
	   */
	  private void initorder(int[] an)
	  {
	    for (int i = 0; i < points.length; i++)
	      an[i] = i;
	  }
	  
	  private double getError(int i, int j)
	  {

	    int c1 = order[i % points.length];
	    int c2 = order[j % points.length];
	    return points[c1].distance(points[c2]);
	  }
	  
	  private Point2D[] getSortedVertexArray(){
		  
		  Point2D[] sortedVertices = new Point2D[points.length];
		  
		  for(int i = 0; i < points.length; i++){
			  sortedVertices[i] = points[minimalorder[i]];
		  }
		  return sortedVertices;
	  }

	  
	}
