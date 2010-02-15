package sim.app.episim.biomechanics;

import java.util.ArrayList;

import sim.app.episim.biomechanics.VertexChangeEvent.VertexChangeEventType;


public class Vertex implements java.io.Serializable{
	
	private static int nextid=1;
	
	private final int id;
	
	private double x;
	private double y;
	
	private ArrayList<VertexChangeListener> changeListener;
	
	public Vertex(double x, double y){
		id = nextid++;
		this.x = x;
		this.y = y;
		changeListener = new ArrayList<VertexChangeListener>();
	}
	
	public Vertex(int x, int y){
		this((double) x, (double)y);
	}	
	
	public int getId(){ return this.id; }
	
	public void addVertexChangeListener(VertexChangeListener listener){
		changeListener.add(listener);
	}
	
	public void removeVertexChangeListener(VertexChangeListener listener){
		changeListener.remove(listener);
	}
	
	public void setDoubleX(double x){ this.x = x; }
	public double getDoubleX(){ return x; }
	public void setDoubleY(double y){ this.y = y; }
	public double getDoubleY(){ return y; }
	
	public void setIntX(int x){ setDoubleX((double)x); }
	public int getIntX(){ return (int) getDoubleX(); }
	public void setIntY(int y){ setDoubleY((double)y); }
	public int getIntY(){ return (int)getDoubleY(); }

	
	private void notifyAllListeners(VertexChangeEventType type){
		VertexChangeEvent event = new VertexChangeEvent(this, type);
		for(VertexChangeListener listener : changeListener) listener.handleVertexChangeEvent(event);
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
	
	

}
