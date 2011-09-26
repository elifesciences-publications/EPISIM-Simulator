package sim.portrayal;

import sim.display.GUIState;


public class SimpleInspectorHack extends SimpleInspector {

	public SimpleInspectorHack(Object object, GUIState state) {
	   super(object, state);
   }
	
	public void setFixedProperties(boolean val){ fixedProperties = val; }
	
	public void generateProperties(int start){ super.generateProperties(start); }

}
