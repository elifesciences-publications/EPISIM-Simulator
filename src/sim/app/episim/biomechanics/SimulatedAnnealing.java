package sim.app.episim.biomechanics;


public class SimulatedAnnealing {

	 public static final double DELTA = 0.99;
	 public static final double STARTING_TEMPERATURE = 10;
	
	 private Vertex[] vertices;

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
	  SimulatedAnnealing(Vertex[] vertices)
	  {
	    this.vertices = vertices;
	    order = new int[vertices.length];
	    minimalorder = new int[vertices.length];
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
	    return i % vertices.length;
	  }

	  /**
	   * Run as a background thread. This method is called to
	   * perform the simulated annealing.
	   */
	  public Vertex[] sortVertices()
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
	      for (int j2 = 0; j2 < vertices.length * vertices.length; j2++) {
	        int i1 = (int)Math.floor((double)vertices.length * Math.random());
	        int j1 = (int)Math.floor((double)vertices.length * Math.random());
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
	        for (int k2 = 0; k2 < vertices.length; k2++)
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
	    for (int i = 1; i <= vertices.length; i++)
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
	    for (int i = 0; i < vertices.length; i++)
	      an[i] = i;
	  }
	  
	  private double getError(int i, int j)
	  {

	    int c1 = order[i % vertices.length];
	    int c2 = order[j % vertices.length];
	    return vertices[c1].edist(vertices[c2]);
	  }
	  
	  private Vertex[] getSortedVertexArray(){
		  
		  Vertex[] sortedVertices = new Vertex[vertices.length];
		  
		  for(int i = 0; i < vertices.length; i++){
			  sortedVertices[i] = vertices[minimalorder[i]];
		  }
		  return sortedVertices;
	  }

	  
	}
