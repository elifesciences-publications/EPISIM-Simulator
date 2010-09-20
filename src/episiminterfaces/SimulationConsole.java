package episiminterfaces;

import java.util.Vector;

import javax.swing.JList;

import sim.display.Controller;
import sim.display.GUIState;
import sim.display.Display2D.InnerDisplay2D;

/**
 * 
 * @author Thomas Suetterlin
 *
 *	Interface for the different available Console Implementations
 *
 */
public interface SimulationConsole extends Controller{
	
	Vector getFrameList();
	JList getFrameListDisplay();
	int getPlayState();
	void pressPause();
	void pressPlay();
	void pressStop();
	GUIState getSimulation();
	void pressPlay(boolean reloadSnapshot);
	void doClose();
	void setWhenShouldEnd(long val);
	

}
