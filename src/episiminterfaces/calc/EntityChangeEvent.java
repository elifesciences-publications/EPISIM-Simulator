package episiminterfaces.calc;


public interface EntityChangeEvent {
	
	public enum EntityChangeEventType {CELLCHANGE, SIMULATIONSTEPCHANGE}
	
	EntityChangeEventType getEventType();

}
