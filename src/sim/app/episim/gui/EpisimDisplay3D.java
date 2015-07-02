package sim.app.episim.gui;


import java.awt.BorderLayout;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;

import javax.media.j3d.Transform3D;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;

import sim.app.episim.ModeServer;
import sim.app.episim.SimStateServer;
import sim.display.Display2DHack;
import sim.display.GUIState;
import sim.display3d.CapturingCanvas3D;
import sim.display3d.Display3DHack;
import sim.engine.Steppable;
import sim.portrayal.FieldPortrayal2D;
import sim.portrayal.Portrayal;
import sim.portrayal3d.FieldPortrayal3D;
import episiminterfaces.EpisimSimulationDisplay;


public class EpisimDisplay3D implements EpisimSimulationDisplay{
	
	protected GUIState simulation;
	private Display3DHack simulationDisplay;
	
	public static final int DEFAULT_NO_GUI_WIDTH = 950;
	public static final int DEFAULT_NO_GUI_HEIGHT = 700;
	
	public EpisimDisplay3D(final double width, final double height, GUIState simulation){		
		
		if(ModeServer.guiMode()) simulationDisplay = new Display3DHack(width, height, simulation);
		else simulationDisplay = new Display3DHack(DEFAULT_NO_GUI_WIDTH, DEFAULT_NO_GUI_HEIGHT, simulation);
		this.simulation = simulation;
			
	}
	public void takeSnapshot()
	{
		if(ModeServer.guiMode())simulationDisplay.takeSnapshot();
	}
	public void changeCellColoringMode(double val){
		simulationDisplay.changeCellColoringMode(val);
	}
	public void detatchAll(){
		simulationDisplay.detatchAll();
	}
	
	public double getDisplayScale(){
		return simulationDisplay.getDisplayScale();
	}
	
	public boolean isValid(){ return simulationDisplay.isValid();}
	
	/**
	 * Creates Internalframe instead of a JFrame
	 */
	public JInternalFrame createInternalFrame()
    {
   
    return simulationDisplay.createInternalFrame();
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
	public void setPortrayalVisible(String name, boolean visible){
		 simulationDisplay.setPortrayalVisible(name, visible);
	}
	
	public void reset(){
		simulationDisplay.reset();
	}
	public void repaint(){
		simulationDisplay.repaint();
	}
	
	public void createSceneGraph(){
		simulationDisplay.createSceneGraph();
	}
	
	public void setBackdrop(Paint c) {
		simulationDisplay.setBackdrop(c);
	}
	
	public void changePortrayal(String name, FieldPortrayal3D protrayal){
		simulationDisplay.changePortrayal(name, protrayal);
	}
	
	public CapturingCanvas3D getInsideDisplay(){
		return simulationDisplay.getInsideDisplay();
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
   public void attach(Portrayal portrayal, String name, boolean visible) {
   	simulationDisplay.attach(portrayal, name, visible);
   }
   
   public void setInitialDisplayScale(double initialScale){ ((Display3DHack)simulationDisplay).setInitialDisplayScale(initialScale); }
   
   public double getInitialDisplayScale(double initialScale){ return ((Display3DHack)simulationDisplay).getInitialDisplayScale(); }
   
   public void translate(double dx, double dy, double dz){
   	simulationDisplay.translate(dx, dy, dz);
   }
   
   public void scale(double scale){
   	simulationDisplay.scale(scale);
   }
   
   public void resetDisplayTransformation(){
   	simulationDisplay.setTransform(new Transform3D());
   }
   
   public void stopRenderer(){
   	simulationDisplay.stopRenderer();
   }
   
   public Steppable getDisplayRotationSteppable(){
   	return simulationDisplay.getDisplayRotationSteppable();
   }
}
