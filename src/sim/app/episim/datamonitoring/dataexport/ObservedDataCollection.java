package sim.app.episim.datamonitoring.dataexport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import episimexceptions.MethodNotImplementedException;


public class ObservedDataCollection<T>{
	
	private interface ListenerAction<T>{
		void performListenerAction(ValueMapListener<T> listener);
	}
	
	public enum ObservedDataCollectionType {ONEDIMTYPE, TWODIMTYPE}
	private HashMap<T, T> map = null;
	private ArrayList<T> list = null;
	
	private Set<ValueMapListener<T>> valueMapListenerSet;
	private ObservedDataCollectionType type;
	
	public ObservedDataCollection(ObservedDataCollectionType _type){
		this.type = _type;
		if(this.type == ObservedDataCollectionType.ONEDIMTYPE) list = new ArrayList<T>();
		else if(this.type == ObservedDataCollectionType.TWODIMTYPE) map = new HashMap<T,T>();
		valueMapListenerSet = new HashSet<ValueMapListener<T>>();
	}

	public ObservedDataCollectionType getType(){ return this.type;}
	
	public void clear() {
	   map.clear();
   }

	

	public void add(final T value1, final T value2) {
		if(this.type == ObservedDataCollectionType.TWODIMTYPE){		
			notifyAllListeners(new ListenerAction<T>(){
	
				public void performListenerAction(ValueMapListener<T> listener) {
	
		         listener.valueAdded(value1, value2);
		         
	         }});
			
			
		   map.put(value1, value2);
		}
		else throw new MethodNotImplementedException("Oberserved Data Collection is of 1 dim type. Please use method add(final T value) instead!");
   }
	public void add(final T value) {
		if(this.type == ObservedDataCollectionType.ONEDIMTYPE){		
			notifyAllListeners(new ListenerAction<T>(){
	
				public void performListenerAction(ValueMapListener<T> listener) {
	
		         listener.valueAdded(value);
		         
	         }});
			
			
		   list.add(value);
		}
		else throw new MethodNotImplementedException("Oberserved Data Collection is of 2 dim type. Please use method add(final T value1, final T value2) instead!");
   }
	


	public int size() {   
		if(this.type == ObservedDataCollectionType.ONEDIMTYPE) return list.size();
		else if(this.type == ObservedDataCollectionType.TWODIMTYPE) return map.size();
		
		return 0;
   }

	
	
	
	public void addValueMapListener(ValueMapListener<T> valueMapListener){
		this.valueMapListenerSet.add(valueMapListener);
	}
	
	private void notifyAllListeners(ListenerAction<T> listenerAction){
		for(ValueMapListener<T> valueMapListener: valueMapListenerSet) listenerAction.performListenerAction(valueMapListener);
	}


	 
	
	

}
