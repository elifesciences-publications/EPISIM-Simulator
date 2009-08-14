package sim.app.episim.util;

import java.util.*;
public class ResultSet <T>{
	
	public enum ResultSetType { ONEDIMRESULTS, TWODIMRESULTS };
	
	private ResultSetType resultType;
	
	private ArrayList <Vector<T>> results;
	
	private long timeStep;
	
	public ResultSet(ResultSetType type){
		resultType = type;
		results = new ArrayList<Vector<T>>();
	}
	
	public void add2DValue(T x, T y){
		if(resultType == ResultSetType.TWODIMRESULTS){
			Vector<T> v = new Vector<T>();
			v.add(x);
			v.add(y);
			results.add(v);
		}
		
	}
	public void add1DValue(T val){
		if(resultType == ResultSetType.ONEDIMRESULTS){
			Vector<T> v = new Vector<T>();
			v.add(val);
			results.add(v);
		}
	}
	
	public Vector<T> get(int index){
		return ObjectManipulations.cloneObject(results.get(index));
	}
	
	 public void clear(){
		 results.clear();		 
	 }
	 
	 public boolean isEmpty(){
		 return results.isEmpty();
	 }
	 
	 public void setTimeStep(long timeStep){
		 this.timeStep = timeStep;
	 }
	 public long getTimeStep(){
		 return this.timeStep;
	 }
	 
	 public int size(){
		 return results.size();
	 }
	
	
	

}
