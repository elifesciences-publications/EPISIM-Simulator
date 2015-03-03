package sim.portrayal;


import javax.swing.Box;
import javax.swing.JPanel;

import sim.app.episim.util.EpisimInspectorProperties;
import sim.display.GUIState;
import sim.util.Interval;
import sim.util.Properties;
import sim.util.gui.PropertyField;


public class SimpleInspectorHack extends SimpleInspector {

	public SimpleInspectorHack(Object object, GUIState state) {
	   super(object, state);
   }
	
	public void setProperties(Properties properties){ this.properties = properties; }
	
	
	
	public void generateProperties(int start){ super.generateProperties(start); }
	
	public JPanel getHeader(){ return this.header; }
	
	public Box getStartField(){ return this.startField;}
	
	public PropertyField[] getMembers(){ return this.members;}
	
	public Properties getProperties(){ return this.properties; }
	
	 
	
}
