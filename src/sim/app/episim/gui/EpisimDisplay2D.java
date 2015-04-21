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
	protected EpisimGUIState simulation;
	private EpisimSimulationDisplay simulationDisplay;
	boolean autoVisualizationSnaphotEnabled = false;
	private JInternalFrame internalFrame;
	
	public EpisimDisplay2D(EpisimGUIState simulation){
		double width = simulation.getEpiDisplayWidth()+(EpisimGUIState.DISPLAY_BORDER_LEFT+EpisimGUIState.DISPLAY_BORDER_RIGHT);
		double height = simulation.getEpiDisplayHeight()+(EpisimGUIState.DISPLAY_BORDER_TOP+EpisimGUIState.DISPLAY_BORDER_BOTTOM);
		
		if(ModeServer.guiMode()){
			simulationDisplay = new Display2DHack(width, height, simulation);
			autoVisualizationSnaphotEnabled = ((Display2DHack)simulationDisplay).isAutomatedPNGSnapshotsEnabled(); 
		}
		else{
			simulationDisplay = new NoGUIDisplay2D(width, height, simulation);
			autoVisualizationSnaphotEnabled = ((NoGUIDisplay2D)simulationDisplay).isAutomatedPNGSnapshotsEnabled(); 
		}
		this.simulation = simulation;
		
	}
	
	 public void takeSnapshot(){
		 if(ModeServer.guiMode())((Display2DHack)simulationDisplay).takeSnapshot();
		 else((NoGUIDisplay2D)simulationDisplay).takeSnapshot();
	 }
	 public void changeCellColoringMode(double val){
		 if(ModeServer.guiMode())((Display2DHack)simulationDisplay).changeCellColoringMode(val);
		 else((NoGUIDisplay2D)simulationDisplay).changeCellColoringMode(val);
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
	
	public void renewSimulationDisplay(){
		
		double width = simulation.getEpiDisplayWidth()+(EpisimGUIState.DISPLAY_BORDER_LEFT+EpisimGUIState.DISPLAY_BORDER_RIGHT);
		double height = simulation.getEpiDisplayHeight()+(EpisimGUIState.DISPLAY_BORDER_TOP+EpisimGUIState.DISPLAY_BORDER_BOTTOM);
		
		internalFrame.getContentPane().remove(((JComponent)simulationDisplay));
		if(ModeServer.guiMode()){
			simulationDisplay = new Display2DHack(width, height, simulation);
			autoVisualizationSnaphotEnabled = ((Display2DHack)simulationDisplay).isAutomatedPNGSnapshotsEnabled(); 
		}
		else{
			simulationDisplay = new NoGUIDisplay2D(width, height, simulation);
			autoVisualizationSnaphotEnabled = ((NoGUIDisplay2D)simulationDisplay).isAutomatedPNGSnapshotsEnabled(); 
		}	
		
		internalFrame.getContentPane().add(((JComponent)simulationDisplay),BorderLayout.CENTER);
		
	   internalFrame.setTitle(simulation.getName() + " Display");
	   internalFrame.validate(); 
	}
	
	/**
	 * Creates Internalframe instead of a JFrame
	 */
	public JInternalFrame getInternalFrame()
    {
	    internalFrame = new JInternalFrame()
	        {
	        public void dispose()
	            {
	            simulationDisplay.quit();       // shut down the movies
	            super.dispose();
	            }
	        };
	        
	    internalFrame.setResizable(true);         
	                            
	    internalFrame.getContentPane().setLayout(new BorderLayout());
	    internalFrame.getContentPane().add(((JComponent)simulationDisplay),BorderLayout.CENTER);
	    
	    internalFrame.setTitle(simulation.getName() + " Display");   
	    
	    internalFrame.setIconifiable(!autoVisualizationSnaphotEnabled);
	    internalFrame.setMaximizable(false);
	    internalFrame.pack();
	    return internalFrame;
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
