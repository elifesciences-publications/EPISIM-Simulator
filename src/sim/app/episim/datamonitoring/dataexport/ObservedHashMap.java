package sim.app.episim.datamonitoring.dataexport;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class ObservedHashMap<K,V> implements Map<K,V>{
	
	private interface ListenerAction<K,V>{
		void performListenerAction(ValueMapListener<K,V> listener);
	}
	
	private HashMap<K, V> map;
	
	private Set<ValueMapListener<K,V>> valueMapListenerSet;
	
	public ObservedHashMap(){
		map = new HashMap<K,V>();
		valueMapListenerSet = new HashSet<ValueMapListener<K,V>>();
	}

	public void clear() {
	   map.clear();
   }

	public boolean containsKey(Object key) {
	   return map.containsKey(key);
   }

	public boolean containsValue(Object value) {
 
	   return map.containsValue(value);
   }

	public Set<Entry<K,V>> entrySet() {

	  
	   return map.entrySet();
   }

	public V get(Object key) {
	   return map.get(key);
   }

	public boolean isEmpty() {
  
	   return map.isEmpty();
   }

	public Set<K> keySet() {
	  
	   return map.keySet();
   }

	public V put(K key, V value) {
		final K key_f = key;
		final V value_f= value;
		
		notifyAllListeners(new ListenerAction<K,V>(){

			public void performListenerAction(ValueMapListener<K, V> listener) {

	         listener.valueAdded(key_f, value_f);
	         
         }});
		
		
	   return map.put(key, value);
   }

	public void putAll(Map<? extends K, ? extends V> m) {

	  map.putAll(m);
	   
   }

	public V remove(Object key) {
	   
		return map.remove(key);
   }

	public int size() {

	  
	   return map.size();
   }

	public Collection<V> values() {
	   return map.values();
   }
	
	
	public void addValueMapListener(ValueMapListener<K,V> valueMapListener){
		this.valueMapListenerSet.add(valueMapListener);
	}
	
	private void notifyAllListeners(ListenerAction<K,V> listenerAction){
		for(ValueMapListener<K,V> valueMapListener: valueMapListenerSet) listenerAction.performListenerAction(valueMapListener);
	}


	 
	
	

}
