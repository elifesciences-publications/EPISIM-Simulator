package sim.app.episim.gui;


import java.awt.BorderLayout;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;

import javax.swing.JComponent;
import javax.swing.JInternalFrame;

import sim.display.Display2DHack;
import sim.display.GUIState;
import sim.display3d.CapturingCanvas3D;
import sim.display3d.Display3DHack;
import sim.portrayal.FieldPortrayal2D;
import sim.portrayal.Portrayal;
import sim.portrayal3d.FieldPortrayal3D;
import episiminterfaces.EpisimSimulationDisplay;


public class EpisimDisplay3D implements EpisimSimulationDisplay{
	
	protected GUIState simulation;
	private EpisimSimulationDisplay simulationDisplay;
	
	public EpisimDisplay3D(final double width, final double height, GUIState simulation){		
		
		simulationDisplay = new Display3DHack(width, height, simulation);
		this.simulation = simulation;
		
	}
	
	public void detatchAll(){
		((Display3DHack)simulationDisplay).detatchAll();
	}
	
	public double getDisplayScale(){
		return simulationDisplay.getDisplayScale();
	}
	
	/**
	 * Creates Internalframe instead of a JFrame
	 */
	public JInternalFrame createInternalFrame()
    {
   
    return ((Display3DHack)simulationDisplay).createInternalFrame();
    }
	
	
	public void attach(FieldPortrayal3D portrayal, String name, Rectangle2D.Double bounds, boolean visible){
		simulationDisplay.attach(portrayal, name, bounds, visible);
	}
	
	public void attach(FieldPortrayal3D portrayal, String name){
		simulationDisplay.attach(portrayal, name);
	}
	
	public boolean isPortrayalVisible(String name){
		return simulationDisplay.isPortrayalVisible(name);
	}
	
	public void reset(){
		simulationDisplay.reset();
	}
	public void repaint(){
		simulationDisplay.repaint();
	}
	
	public void setBackdrop(Paint c) {
		simulationDisplay.setBackdrop(c);
	}
	
	public void changePortrayal(String name, FieldPortrayal2D protrayal){
		((Display2DHack) simulationDisplay).changePortrayal(name, protrayal);
	}
	
	public CapturingCanvas3D getInsideDisplay(){
		return ((Display3DHack) simulationDisplay).getInsideDisplay();
	}
	
   public void quit() {	 
	   simulationDisplay.quit();
   }

	public void attach(Portrayal portrayal, String name, Double bounds, boolean visible) {
		simulationDisplay.attach(portrayal, name, bounds, visible);	   
   }

   public void attach(Portrayal portrayal, String name) {
   	simulationDisplay.attach(portrayal, name);
   }
   
   public void translate(double dx, double dy, double dz){
   	((Display3DHack)simulationDisplay).translate(dx, dy, dz);
   }
   
   public void scale(double scale){
   	((Display3DHack)simulationDisplay).scale(scale);
   }
}
