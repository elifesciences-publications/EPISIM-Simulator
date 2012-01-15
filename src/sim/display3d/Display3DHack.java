package sim.display3d;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Paint;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;

import episiminterfaces.EpisimSimulationDisplay;
import sim.app.episim.EpisimProperties;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.ModeServer;
import sim.app.episim.gui.EpisimGUIState;
import sim.app.episim.gui.ImageLoader;
import sim.app.episim.util.EpisimMovieMaker;
import sim.display.Display2D;
import sim.display.Display2DHack;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.engine.Steppable;

import sim.portrayal.FieldPortrayal;
import sim.portrayal.FieldPortrayal2D;
import sim.portrayal.Portrayal;
import sim.portrayal3d.FieldPortrayal3D;
import sim.portrayal3d.Portrayal3D;
import sim.util.gui.NumberTextField;


public class Display3DHack extends Display3D implements EpisimSimulationDisplay{
	
	//---------------------------------------------------------------------------------------------------------------------------------------------
	// TODO: IMPORTANT delete class OptionPane in class Display3D otherwise headless mode in computer cluster environment does not work !!!
	//----------------------------------------------------------------------------------------------------------------------------------------------
	
	
	
	
	
	
	
	
	private EpisimGUIState epiSimulation = null;
	
	private boolean moviePathSet = false;
	private EpisimMovieMaker episimMovieMaker;
	
	public Display3DHack(double width, double height, GUIState simulation) {

		super(width, height, simulation);
		optionButton.setVisible(false);
		moviePathSet = EpisimProperties.getProperty(EpisimProperties.MOVIE_PATH_PROP) != null;
		if(moviePathSet && ModeServer.consoleInput()){ 
			movieButton.setEnabled(false);
			 simulation.scheduleAtStart(new Steppable()   // to stop movie when simulation is stopped
          {
          public void step(SimState state) { startMovie(); }
          });
		}
		
		
		if(simulation instanceof EpisimGUIState) epiSimulation = (EpisimGUIState) simulation;
		
		
		//no unnecessary Entry: Show Console in the popup
		if(popup != null && popup.getComponentCount()>1){
			popup.remove(0);
			popup.remove(0);
		}
		for(Component comp :header.getComponents()){
			if(comp instanceof NumberTextField
				|| comp instanceof JComboBox) header.remove(comp);
		} 
		
		NumberTextField  scaleField = new NumberTextField("  Scale: ", 1.0, true)
        {
        public double newValue(double newValue)
            {
            if (newValue <= 0.0) newValue = currentValue;
            epiSimulation.workaroundConsolePause();
            setScale(newValue);
            epiSimulation.workaroundConsolePlay();
            return newValue;
            }
        };
        scaleField.setToolTipText("Magnifies the scene.  Not the same as zooming (see the options panel)");
        scaleField.setBorder(BorderFactory.createEmptyBorder(0,0,0,2));
        header.add(scaleField);		
	}
	
	
	public double getDisplayScale(){
		 return this.getScale();
	}
	public void setPortrayalVisible(String name, boolean visible){
		Portrayal3DHolder holder =getPortrayalHolder(name);
		if(holder != null){
			holder.visible = visible;
			canvas.repaint();
		}
		
	}
	 public ArrayList detatchAll()
    {
	    ArrayList old = portrayals;
	    popup.removeAll();
	    
	    portrayals = new ArrayList();
	    return old;
    }

	
	public boolean isPortrayalVisible(String name){
		Portrayal3DHolder holder =getPortrayalHolder(name);
		if(holder != null) return holder.visible;
		else return false;
	}
	
	private Portrayal3DHolder getPortrayalHolder(String name){
		Portrayal3DHolder holder;
		for(Object obj :portrayals){
			if(obj instanceof Portrayal3DHolder){
				if((holder =(Portrayal3DHolder)obj).name.equals(name)) return holder;
			}
		}
		return null;
	}
	
	public void changePortrayal(String name, Portrayal3D portrayal){
		Portrayal3DHolder holder = getPortrayalHolder(name);
		if(holder != null){
			holder.portrayal = portrayal;
		}
	}
		
	public CapturingCanvas3D getInsideDisplay() {
      return canvas;
   }
	
	public void startMovie()
   {
		if(ModeServer.consoleInput() && moviePathSet){
			synchronized(Display3DHack.this.simulation.state.schedule)
	      {
				 if (episimMovieMaker != null) return;  // already running
		       episimMovieMaker = new EpisimMovieMaker(getFrame());
		       
		       canvas.beginCapturing(false);  // emit a single picture to get the image sizes
	          final BufferedImage typicalImage = canvas.getLastImage();
	                    
	          if (!episimMovieMaker.start(typicalImage))
	         	 episimMovieMaker = null;  // fail
	          else
             {
	             canvas.beginCapturing(true);
	             simulation.scheduleAtEnd(new Steppable(){   // to stop movie when simulation is stopped
	                 public void step(SimState state) { stopMovie(); }
	             });
             }
                             
         typicalImage.flush();  // just in case -- bug in OS X
	      }
		}
		else super.startMovie();
   }
	public void stopMovie()
   {
		if(ModeServer.consoleInput() && moviePathSet){
		   synchronized(Display3DHack.this.simulation.state.schedule)
		   {
		       if (episimMovieMaker == null) return;  // already stopped
		       canvas.stopCapturing();
		       if (!episimMovieMaker.stop())
		       {		           
		           ExceptionDisplayer.getInstance().displayException(new Exception("Your movie did not write to disk\ndue to a spurious JMF movie generation bug."));		             
		       }
		       episimMovieMaker = null;		       
		   }
		}
		else super.stopMovie();
   }
	
	public void step(final SimState state)
   {
		if(ModeServer.consoleInput() && moviePathSet){
			 if (shouldUpdate() &&
                (canvas.isShowing()    // only draw if we can be seen
                || episimMovieMaker != null ))      // OR draw to a movie even if we can't be seen
            {
            updateSceneGraph(true);
            }
		}
		else{
			super.step(state);
		}
   }	
	
   public void attach(Portrayal portrayal, String name, Rectangle2D.Double bounds, boolean visible) {
   	if(portrayal instanceof Portrayal3D){
   		super.attach((Portrayal3D) portrayal, name, visible);
   	}
   }
	
   public void attach(Portrayal portrayal, String name) {
   	if(portrayal instanceof Portrayal3D){
   		super.attach((Portrayal3D) portrayal, name);
   	}   
   }
	
   public void setBackdrop(Paint c) {
   	if(c instanceof Color){
   		super.setBackdrop((Color)c);
   	}
   } 

}
