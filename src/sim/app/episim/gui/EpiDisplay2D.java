package sim.app.episim.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.plaf.basic.BasicInternalFrameUI;

import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;

public class EpiDisplay2D extends Display2D {
	protected GUIState simulation;
	
	public EpiDisplay2D(final double width, final double height, GUIState simulation, long interval){
		super(width, height, simulation, interval);
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
            quit();       // shut down the movies
            super.dispose();
            }
        };
        
    frame.setResizable(true);
    
    
         
                            
    frame.getContentPane().setLayout(new BorderLayout());
    frame.getContentPane().add(this,BorderLayout.CENTER);
    
    frame.setTitle(simulation.getName()  + " Display");
   
    
   
    frame.setMaximizable(false);
    frame.pack();
    return frame;
    }
	
	
	
}
