package sim.display;

import java.util.Vector;

import javax.swing.JList;


public class ConsoleHack extends Console {
	
	public ConsoleHack(final GUIState simulation){
		super(simulation);
	}
	
	public Vector getFrameList(){ return this.frameList;}
	
	public JList getFrameListDisplay(){ return this.frameListDisplay;}

}
