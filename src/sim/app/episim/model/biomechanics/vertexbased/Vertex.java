package sim.app.episim.model.biomechanics.vertexbased;

import java.util.ArrayList;
import java.util.HashSet;

import sim.app.episim.model.biomechanics.vertexbased.VertexChangeEvent.VertexChangeEventType;


public class Vertex implements java.io.Serializable{
	
	/**
    * 
    */
   private static final long serialVersionUID = 5030387288513842644L;

	private static int nextid=1;
	
	private final int id;
	
	private double x;
	private double y;
	
	private double x_new;
	private double y_new;
	
	private ArrayList<VertexChangeListener> changeListener;
	
	private boolean isNew = false;
		
	private boolean mergeVertex = false;
	
	private boolean estimatedVertex = false;
	
	private boolean wasDeleted = false;
	
	private boolean wasAlreadyCalculated = false;

	
	public Vertex(double x, double y){
		id = nextid++;
		this.x = x;
		this.y = y;
		this.x_new = x;
		this.y_new = y;
		changeListener = new ArrayList<VertexChangeListener>();
	}
	
	public Vertex(int x, int y){
		this((double) x, (double)y);
	}
	
	public Vertex(Vertex v)
   {
       this(v.x, v.y);
   }
	
	public int getId(){ return this.id; }
	
	public boolean isNew(){ return isNew;}
	public void setIsNew(boolean isNew){this.isNew = isNew;}
	
	
	
	public Vertex relTo(Vertex v)
   {
        return new Vertex(x-v.x, y-v.y);
   }
	
	public Vertex relToNormalized(Vertex v)
   {
      double x_new = x-v.x;
      double y_new = y-v.y;
      
      double normFactor = Math.sqrt(Math.pow(x_new, 2)+Math.pow(y_new, 2));
		
		return new Vertex(x_new/normFactor, y_new/normFactor);
   }

    public void makeRelTo(Vertex v)
    {
        x-=v.x;
        y-=v.y;
        notifyAllListeners(VertexChangeEventType.VERTEXMOVED);
    }


    public Vertex moved(double x0, double y0)
    {
        return new Vertex(x+x0, y+y0);
    }
    
    public void scalarMult(double scalar){
   	 x *= scalar;
   	 y *= scalar;
   	 notifyAllListeners(VertexChangeEventType.VERTEXMOVED);
    }

    public Vertex reversed()
    {
        return new Vertex(-x, -y);
    }


    public boolean isLower(Vertex v)
    {
        return y<v.y || y==v.y && x<v.x;
    }

    /**
     * Manhattan Distanz
     * @return
     */
    public double mdist()   // Manhattan-Distanz
    {
        return Math.abs(x)+Math.abs(y);
    }
    
    /**
     * Euclidean Distance
     * @return
     */
    public double edist(){ //Euklidische-Distanz
   	 return Math.sqrt((Math.pow(x, 2)+ Math.pow(y, 2)));
    }
    /**
     * Manhattan Distanz
     * @return
     */
    public double mdist(Vertex v)
    {
        return relTo(v).mdist();
    }

    /**
     * Euclidean Distance
     * @return
     */
    public double edist(Vertex v){ //Euklidische-Distanz
   	 return relTo(v).edist();
    }
    
    public boolean isFurther(Vertex v)
    {
        return mdist()>v.mdist();
    }


    public boolean isBetween(Vertex v0, Vertex v1)
    {
        return v0.mdist(v1)>=mdist(v0)+mdist(v1);
    }


    public double cross(Vertex v)
    {
        return x*v.y-v.x*y;
    }


    public boolean isLess(Vertex v)
    {
        double f=cross(v);
        return f>0 || f==0 && isFurther(v);
    }


    public double area2(Vertex v0, Vertex v1)
    {
        return v0.relTo(this).cross(v1.relTo(this));
    }

    public boolean isConvex(Vertex v0, Vertex v1)
    {
        double f=area2(v0, v1);
        return f<0 || f==0 && !isBetween(v0, v1);
    }

	public VertexChangeListener[] getVertexChangeListener(){
		return this.changeListener.toArray(new VertexChangeListener[changeListener.size()]);
	}
	
	public void addVertexChangeListener(VertexChangeListener listener){
		changeListener.add(listener);
	}
	
	public boolean isVertexOfCellPolygon(CellPolygon pol){
		for(VertexChangeListener listener: this.changeListener){
			if(listener instanceof CellPolygon){
				CellPolygon otherPol = (CellPolygon) listener;
				if(pol.getId() == otherPol.getId()) return  true;
			}
		}
		return false;
	}
	
	public void removeVertexChangeListener(VertexChangeListener listener){
		changeListener.remove(listener);
	}
	
	public void setDoubleX(double x){
		if(x != this.x){
			this.x = x;
			notifyAllListeners(VertexChangeEventType.VERTEXMOVED);
		}
	}
	public double getDoubleX(){ return x; }
	public void setDoubleY(double y){ 
		if(y != this.y){
			this.y = y;
			notifyAllListeners(VertexChangeEventType.VERTEXMOVED);
		}
		
	}
	public double getDoubleY(){ return y; }
	
	public void setIntX(int x){ setDoubleX((double)x); }
	public int getIntX(){ return (int)Math.round(getDoubleX()); }
	public void setIntY(int y){ setDoubleY((double)y); }
	public int getIntY(){ return (int)Math.round(getDoubleY()); }

	
	private void notifyAllListeners(VertexChangeEventType type){
		VertexChangeEvent event = new VertexChangeEvent(this, type);
		ArrayList<VertexChangeListener> changeListenerCopy = new ArrayList<VertexChangeListener>();
		changeListenerCopy.addAll(changeListener);
		for(VertexChangeListener listener : changeListenerCopy) listener.handleVertexChangeEvent(event);
	}
	
	private void notifyAllListeners(VertexChangeEventType type, Vertex newVertex){
		VertexChangeEvent event = new VertexChangeEvent(this, newVertex, type);
		ArrayList<VertexChangeListener> changeListenerCopy = new ArrayList<VertexChangeListener>();
		changeListenerCopy.addAll(changeListener);
		for(VertexChangeListener listener : changeListenerCopy) listener.handleVertexChangeEvent(event);
	}
	
	/*
	 * Vertex will be removed from all Listening Polygons
	 */
	public void delete(){
		this.wasDeleted = true;
		notifyAllListeners(VertexChangeEventType.VERTEXDELETED);
	}
	
	public void replaceVertex(Vertex newVertex){
		this.wasDeleted = true;
		notifyAllListeners(VertexChangeEventType.VERTEXREPLACED, newVertex);
	}
	
	public String toString(){
		return "("+ x+", "+y+")";
	}
	

   public int hashCode() {

	   final int prime = 31;
	   int result = 1;
	   result = prime * result + id;
	   return result;
   }
   

   public boolean equals(Object obj) {

	   if(this == obj)
		   return true;
	   if(obj == null)
		   return false;
	   if(getClass() != obj.getClass())
		   return false;
	   Vertex other = (Vertex) obj;
	   if(id != other.id)
		   return false;
	   return true;
   }

	
   public int getNumberOfCellsJoiningThisVertex(){
   	int numberOfCellPolygons = 0;
   	for(VertexChangeListener listener :changeListener){
   		if(listener instanceof CellPolygon) numberOfCellPolygons++;
   	}
   	return numberOfCellPolygons;
   }
   
   public CellPolygon[] getCellsJoiningThisVertex(){
   	HashSet<CellPolygon> polygons = new HashSet<CellPolygon>();
   	for(VertexChangeListener listener :changeListener){
   		if(listener instanceof CellPolygon) polygons.add((CellPolygon) listener);
   	}
   	CellPolygon[] polArray = new CellPolygon[polygons.size()];
   	return polygons.toArray(polArray);
   }
   
   
   public Vertex[] getAllOtherVerticesConnectedToThisVertex(){
   	HashSet<Vertex> connectedVertices = new HashSet<Vertex>();
   	CellPolygon[] cellPolygons = getCellsJoiningThisVertex();
   	for(CellPolygon pol :cellPolygons){
   		Vertex[] vertices = pol.getSortedVertices();
   		int index = getIndexOfThisVertexInVertexArray(vertices);
   		if(index >= 0){
   			connectedVertices.add(vertices[mod(index-1, vertices.length)]);
   			connectedVertices.add(vertices[mod(index+1, vertices.length)]);
   		}
   	}
   	return connectedVertices.toArray(new Vertex[connectedVertices.size()]);   	
   }
   private int getIndexOfThisVertexInVertexArray(Vertex[] vertices){
   	for(int i = 0; i < vertices.length; i++){
   		if(vertices[i] != null && vertices[i].getId() == this.getId()) return i;
   	}
   	return -1;
   }
   
   private int mod(double value, double base){
		return value%base < 0 ? (int)((value%base)+base) : (int)(value%base);
	}
   

	
   public boolean isMergeVertex() {
   
   	return mergeVertex;
   }

	
   public void setMergeVertex(boolean mergeVertex) {
   
   	this.mergeVertex = mergeVertex;
   }
	

	public boolean isWasDeleted() {
	
		return wasDeleted;
	}

	
   public boolean isEstimatedVertex() {
   
   	return estimatedVertex;
   }

	
   public void setEstimatedVertex(boolean estimatedVertex) {
   
   	this.estimatedVertex = estimatedVertex;
   }
   

	public boolean isWasAlreadyCalculated() {
	
		return wasAlreadyCalculated;
	}

	
	public void setWasAlreadyCalculated(boolean wasAlreadyCalculated) {
	
		this.wasAlreadyCalculated = wasAlreadyCalculated;
	}

	
	public void setNewX(double x_new) {
	
		this.x_new = x_new;
	}

	
	public void setNewY(double y_new) {
	
		this.y_new = y_new;
	}
	
	
	public double getNewX() {
		
		return this.x_new; 
	}

	
	public double getNewY() {
	
		return this.y_new;
	}
	
	public void resetCalculationStatus(){
		this.wasAlreadyCalculated = false;
	}
	
	public void commitNewValues(){
		this.x = this.x_new;
		this.y = this.y_new;
		notifyAllListeners(VertexChangeEventType.VERTEXMOVED);
	}


}
