package sim.app.episim.model.biomechanics.vertexbased2Dr.geom;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;

import sim.app.episim.model.biomechanics.vertexbased2Dr.geom.VertexChangeEvent.VertexChangeEventType;



public class Vertex implements java.io.Serializable{
	
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
	
	private long lastCalculationSimStepNo = -1;

	private Color vertexColor;
	
	private boolean isAttachedToBasalLayer = false;
	
	private boolean isIntruderVertex = false;
	
	public Vertex(double x, double y){
		this(x, y, true);
	}
	
	public Vertex(double x, double y, boolean transformContinuousFieldLocation){
		id = nextid++;
		this.x = transformContinuousFieldLocation ? ContinuousVertexField.getInstance().getXLocationInField(x) : x;
		this.y = transformContinuousFieldLocation ? ContinuousVertexField.getInstance().getYLocationInField(y) : y;
		this.x_new = this.x;
		this.y_new = this.y;
		changeListener = new ArrayList<VertexChangeListener>();
		vertexColor = Color.BLUE;
	}
	
	public Vertex(int x, int y){
		this((double) x, (double)y);
	}
		
	public int getId(){ return this.id; }
	
	
		   
    /**
     * Manhattan Distanz
     * 
     * @return
     */
    public double mdist(Vertex v)   // Manhattan-Distanz
    {
        return Math.abs(ContinuousVertexField.getInstance().dxMinAbs(this, v))+Math.abs(ContinuousVertexField.getInstance().dyMinAbs(this, v));
    }
    
    /**
     * Euclidean Distance
     * @return
     */
    public double edist(Vertex v){ //Euklidische-Distanz
   	 return ContinuousVertexField.getInstance().getMinEuclideanDistance(this, v);
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
			this.x = ContinuousVertexField.getInstance().getXLocationInField(x);
			notifyAllListeners(VertexChangeEventType.VERTEXMOVED);
		}
	}
	public double getDoubleX(){ return x; }
	public void setDoubleY(double y){ 
		if(y != this.y){
			this.y = ContinuousVertexField.getInstance().getYLocationInField(y);
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
		return "(" + getDoubleX() + ", " + getDoubleY() +")";
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
   

	public boolean isWasAlreadyCalculated(long simStepNo){	
		return this.lastCalculationSimStepNo == simStepNo;
	}

	
	public void setWasAlreadyCalculated(long simStepNo){	
		this.lastCalculationSimStepNo = simStepNo;
	}

	
	public void setNewX(double x_new) {
	
		this.x_new = ContinuousVertexField.getInstance().getXLocationInField(x_new);
	}

	
	public void setNewY(double y_new) {
	
		this.y_new = ContinuousVertexField.getInstance().getYLocationInField(y_new);
	}
	
	
	public double getNewX() {
		
		return this.x_new; 
	}

	
	public double getNewY() {
	
		return this.y_new;
	}
	
		
	public void commitNewValues(){
		this.x = this.x_new;
		this.y = this.y_new;
		notifyAllListeners(VertexChangeEventType.VERTEXMOVED);
	}

	
   public Color getVertexColor() {
   
   	return vertexColor;
   }

	
   public void setVertexColor(Color vertexColor) {
   
   	this.vertexColor = vertexColor;
   }

	
   public boolean isIntruderVertex() {
   
   	return isIntruderVertex;
   }

	
   public void setIntruderVertex(boolean isIntruderVertex) {
   
   	this.isIntruderVertex = isIntruderVertex;
   }
   public boolean isNew(){ return isNew;}
	public void setIsNew(boolean isNew){this.isNew = isNew;}
	
	public boolean isAttachedToBasalLayer(){ return isAttachedToBasalLayer; }
	public void setIsAttachedToBasalLayer(boolean val){ this.isAttachedToBasalLayer = val; }


}
