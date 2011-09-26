package sim.app.episim.gui;

import sim.app.episim.util.EpisimProperties;
import sim.display.GUIState;
import sim.portrayal.SimpleInspectorHack;



public class EpisimSimpleInspector extends SimpleInspectorHack {

	public EpisimSimpleInspector(Object object, GUIState state) {

	   super(object, state);
	   this.properties = EpisimProperties.getProperties(object,true,true,false,true);
	   setFixedProperties(true);
	   generateProperties(0);
   }

}
