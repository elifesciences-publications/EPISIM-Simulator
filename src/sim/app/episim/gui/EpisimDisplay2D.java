package sim.app.episim.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.plaf.basic.BasicInternalFrameUI;

import episiminterfaces.EpisimSimulationDisplay;

import sim.app.episim.EpisimProperties;
import sim.app.episim.ModeServer;
import sim.app.episim.nogui.NoGUIDisplay2D;
import sim.display.Console;
import sim.display.Display2D;
import sim.display.Display2DHack;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.portrayal.FieldPortrayal2D;
import sim.portrayal.Portrayal;

public class EpisimDisplay2D implements EpisimSimulationDisplay{
	protected GUIState simulation;
	private EpisimSimulationDisplay simulationDisplay;
	
	public EpisimDisplay2D(final double width, final double height, GUIState simulation){
		
		
		if(ModeServer.guiMode())simulationDisplay = new Display2DHack(width, height, simulation);
		else simulationDisplay = new NoGUIDisplay2D(width, height, simulation);
		this.simulation = simulation;
		
	}
	
	
	public boolean isValid(){
		if(simulationDisplay instanceof Display2DHack) return((Display2DHack)simulationDisplay).isValid();
		else if(simulationDisplay instanceof NoGUIDisplay2D)return((NoGUIDisplay2D)simulationDisplay).isValid();
		return false;
	}
	
	public void detatchAll(){
		if(simulationDisplay instanceof Display2DHack)((Display2DHack)simulationDisplay).detatchAll();
		else if(simulationDisplay instanceof NoGUIDisplay2D)((NoGUIDisplay2D)simulationDisplay).detatchAll();
	}
	
	public double getDisplayScale(){
		return simulationDisplay.getDisplayScale();
	}
	
	
	/**
	 * Creates Internalframe instead of a JFrame
	 */
	public JInternalFrame createInternalFrame()
    {
    JInternalFrame frame = new JInternalFrame()
        {
        public void dispose()
            {
            simulationDisplay.quit();       // shut down the movies
            super.dispose();
            }
        };
        
    frame.setResizable(true);
    
    
         
                            
    frame.getContentPane().setLayout(new BorderLayout());
    frame.getContentPane().add(((JComponent)simulationDisplay),BorderLayout.CENTER);
    
    frame.setTitle(simulation.getName()  + " Display");
   
    
   
    frame.setMaximizable(false);
    frame.pack();
    return frame;
    }
	
	public void attach(FieldPortrayal2D portrayal, String name, Rectangle2D.Double bounds, boolean visible){
		simulationDisplay.attach(portrayal, name, bounds, visible);
	}
	
	public void attach(FieldPortrayal2D portrayal, String name){
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
		if(ModeServer.guiMode()) ((Display2DHack) simulationDisplay).changePortrayal(name, protrayal);
		else ((NoGUIDisplay2D) simulationDisplay).changePortrayal(name, protrayal);
	}
	
	public JComponent getInsideDisplay(){
		if(ModeServer.guiMode()) return ((Display2DHack) simulationDisplay).insideDisplay;
		else return ((NoGUIDisplay2D) simulationDisplay).insideDisplay;
	}
	
	 public void paintComponentInInnerDisplay(Graphics g, boolean buffer){
		 if(ModeServer.guiMode()) ((Display2DHack) simulationDisplay).insideDisplay.paintComponent(g, buffer);
		 else ((NoGUIDisplay2D) simulationDisplay).insideDisplay.paintComponent(g, buffer);
		 
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
	
	
}
