package sim.app.episim.util;


public interface ListenerAction <T extends Object>{

	void performAction(T changedObject);
	
}
