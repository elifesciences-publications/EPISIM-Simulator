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

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.plaf.basic.BasicInternalFrameUI;

import episiminterfaces.SimulationDisplay;

import sim.app.episim.EpisimProperties;
import sim.app.episim.nogui.NoGUIDisplay2D;
import sim.display.Console;
import sim.display.Display2D;
import sim.display.Display2DHack;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.portrayal.FieldPortrayal2D;

public class EpiDisplay2D {
	protected GUIState simulation;
	private SimulationDisplay simulationDisplay;
	private boolean guiMode = true;
	private boolean consoleInput = false;
	public EpiDisplay2D(final double width, final double height, GUIState simulation, long interval){
		consoleInput =  (EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CONSOLE_INPUT_PROP) != null 
				&& EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CONSOLE_INPUT_PROP).equals(EpisimProperties.ON_CONSOLE_INPUT_VAL));
		guiMode = ((EpisimProperties.getProperty(EpisimProperties.SIMULATOR_GUI_PROP) != null 
				&& EpisimProperties.getProperty(EpisimProperties.SIMULATOR_GUI_PROP).equals(EpisimProperties.ON_SIMULATOR_GUI_VAL) && consoleInput) 
				|| (EpisimProperties.getProperty(EpisimProperties.SIMULATOR_GUI_PROP)== null));
		
		if(guiMode)simulationDisplay = new Display2DHack(width, height, simulation, interval);
		else simulationDisplay = new NoGUIDisplay2D(width, height, simulation, interval);
		this.simulation = simulation;
		
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
	
	public JComponent getInsideDisplay(){
		if(guiMode) return ((Display2DHack) simulationDisplay).insideDisplay;
		else return ((NoGUIDisplay2D) simulationDisplay).insideDisplay;
	}
	
	 public void paintComponentInInnerDisplay(Graphics g, boolean buffer){
		 if(guiMode) ((Display2DHack) simulationDisplay).insideDisplay.paintComponent(g, buffer);
		 else ((NoGUIDisplay2D) simulationDisplay).insideDisplay.paintComponent(g, buffer);
		 
	 }
	
	
}
