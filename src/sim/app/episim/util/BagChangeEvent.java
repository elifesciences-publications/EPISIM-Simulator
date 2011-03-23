package sim.app.episim.util;

import java.util.Collection;


public class BagChangeEvent<T> {
	
	public enum BagChangeEventType{ ADD_EVENT, REMOVE_EVENT, REPLACE_EVENT; }
	
	private BagChangeEventType eventType;
	
	private Collection<? extends T> eventSourceObjects;
	
	public BagChangeEvent(BagChangeEventType eventType){
		this(eventType, null);
	}
	
	public BagChangeEvent(BagChangeEventType eventType, Collection<? extends T> eventSourceObjects){
		this.eventType = eventType;
		this.eventSourceObjects = eventSourceObjects;
	}
	
	public BagChangeEventType getEventType(){ return this.eventType; }

	public Collection<? extends T> getEventSourceObjects(){ return eventSourceObjects; }
}
