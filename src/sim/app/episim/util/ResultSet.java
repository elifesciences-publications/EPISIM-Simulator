package sim.app.episim.util;

import java.util.*;
public class ResultSet <T>{
	
	public enum ResultSetType { ONEDIMRESULTS, TWODIMRESULTS, MULTIDIMRESULTS };
	
	private ResultSetType resultType;
	
	private ArrayList <Vector<T>> results = null;
	
	
	private long timeStep;
	
	public ResultSet(ResultSetType type){
		resultType = type;
		results = new ArrayList<Vector<T>>();
	}
	public void addMultiDimValue(Vector<T> columnVector){
		if(resultType == ResultSetType.MULTIDIMRESULTS){			
			results.add(columnVector);
		}		
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
		if(results != null)return ObjectManipulations.cloneObject(results.get(index));
		return null;
	}
	
	public void clear(){
		 if(results != null) results.clear();
	}
	 
	public boolean isEmpty(){
		 if(results != null){
				 return results.isEmpty();
		 }
		 return true;
	}
	 
	 public void setTimeStep(long timeStep){
		 this.timeStep = timeStep;
	 }
	 public long getTimeStep(){
		 return this.timeStep;
	 }
	 
	 public int size(){
		 if(results != null){
				 return results.size();
		 }
		 return 0;
	 }
	
	 public ResultSetType getResultSetType(){ return this.resultType;}
	

}
