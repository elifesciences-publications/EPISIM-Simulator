package sim.display;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JOptionPane;

import sim.app.episim.EpisimProperties;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.gui.EpidermisGUIState;
import sim.app.episim.util.EpisimMovieMaker;
import sim.app.episim.util.Scale;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.gui.MovieMaker;
import sim.util.gui.NumberTextField;


public class Display2DHack extends Display2D {
	private EpidermisGUIState epiSimulation = null;
	
	private boolean consoleMode = false;
	private boolean moviePathSet = false;
	private EpisimMovieMaker episimMovieMaker;
	
	public Display2DHack(double width, double height, GUIState simulation, long interval) {

		super(width, height, simulation, interval);
		
		moviePathSet = EpisimProperties.getProperty(EpisimProperties.MOVIE_PATH_PROP) != null;
		consoleMode = EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CONSOLEMODE_PROP).equals(EpisimProperties.OFF_CONSOLEMODE_VAL);
		
		if(moviePathSet && consoleMode) movieButton.setEnabled(false);
		
		
		if(simulation instanceof EpidermisGUIState) epiSimulation = (EpidermisGUIState) simulation;
		
		//no unnecessary Entry: Show Console in the popup
		if(popup != null && popup.getComponentCount()>1){
			popup.remove(0);
			popup.remove(0);
		}
		
		for(Component comp :header.getComponents()){
			if(comp instanceof NumberTextField) header.remove(comp);
		} 
	    // add the scale field
      NumberTextField scaleField = new NumberTextField("  Scale: ", 1.0, true)
          {
          public double newValue(double newValue)
              {
              if (newValue <= 0.0) newValue = currentValue;
              epiSimulation.workaroundConsolePause();
              setScale(newValue);
              Scale.displayScale = getScale();
              port.setView(insideDisplay);
              //optionPane.xOffsetField.add *= (newValue / currentValue);
              optionPane.xOffsetField.setValue(insideDisplay.xOffset * newValue);
              //optionPane.yOffsetField.add *= (newValue / currentValue);
              optionPane.yOffsetField.setValue(insideDisplay.yOffset * newValue);
              epiSimulation.workaroundConsolePlay();
              return newValue;
              }
          };
      scaleField.setToolTipText("Zoom in and out");
      header.add(scaleField);
      
      // add the interval (skip) field
      NumberTextField skipField = new NumberTextField("  Skip: ", 1, false)
          {
          public double newValue(double newValue)
              {
              int val = (int) newValue;
              if (val < 1) val = (int)currentValue;
                      
              // reset with a new interval
              setInterval(val);
              reset();
                      
              return val;
              }
          };
      skipField.setToolTipText("Specify the number of steps between screen updates");
      header.add(skipField);
      
   // set ourselves up to quit when stopped
      simulation.scheduleAtStart(new Steppable()   // to stop movie when simulation is stopped
          {
          public void step(SimState state) { startMovie(); }
          });
      
		
	}
	
	
	
	public void setPortrayalVisible(String name, boolean visible){
		FieldPortrayal2DHolder holder =getPortrayalHolder(name);
		if(holder != null){
			holder.visible = visible;
			insideDisplay.repaint();
		}
		
	}
	
	public boolean isPortrayalVisible(String name){
		FieldPortrayal2DHolder holder =getPortrayalHolder(name);
		if(holder != null) return holder.visible;
		else return false;
	}
	
	private FieldPortrayal2DHolder getPortrayalHolder(String name){
		FieldPortrayal2DHolder holder;
		for(Object obj :portrayals){
			if(obj instanceof FieldPortrayal2DHolder){
				if((holder =(FieldPortrayal2DHolder)obj).name.equals(name)) return holder;
			}
		}
		return null;
	}
	
	
	public void startMovie()
   {
		if(consoleMode && moviePathSet){
			synchronized(Display2DHack.this.simulation.state.schedule)
	       {
	       
	       if (episimMovieMaker != null) return;  // already running
	       episimMovieMaker = new EpisimMovieMaker(getFrame());
	       Graphics g = insideDisplay.getGraphics();
	       final BufferedImage typicalImage = insideDisplay.paint(g,true,false);
	       g.dispose();
	               
	       if (!episimMovieMaker.start(typicalImage))
	      	 episimMovieMaker = null;  // failed
	       else 
	           {
	         	
	           // start up simulation paused if necessary
	           final Console console = (Console)(simulation.controller);
	           if (console.getPlayState() == Console.PS_STOPPED)  // either after simulation or we just started the program
	               console.pressPause();
	           
	           lastEncodedSteps = -1;
	
	           // capture the currently shown frame (important if we just paused the simulation)
	           insideDisplay.paintToMovie(null);
	           
	           
	           // set ourselves up to quit when stopped
	           simulation.scheduleAtEnd(new Steppable()   // to stop movie when simulation is stopped
	               {
	               public void step(SimState state) { stopMovie(); }
	               });
	           }
	       }
	   }
		else super.startMovie();
   }
	
	public void stopMovie()
   {
		if(consoleMode && moviePathSet){
		   synchronized(Display2DHack.this.simulation.state.schedule)
		   {
		       if (episimMovieMaker == null) return;  // already stopped
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
		if(consoleMode && moviePathSet){
			long steps = simulation.state.schedule.getSteps();
	      
	      if (steps % getInterval() == 0   // time to update!
	          && (insideDisplay.isShowing()    // only draw if we can be seen
	              || episimMovieMaker !=null ))      // OR draw to a movie even if we can't be seen
	          {
	          if (isMacOSX && episimMovieMaker == null) 
	              {   // macos x should use other method for movie maker and off-screen buffers
	              insideDisplay.repaint();
	              }
	          else  // Windows or X Windows
	              {
	              Graphics g = insideDisplay.getGraphics();
	              insideDisplay.paintComponent(g,true);
	              g.dispose();
	              }
	          }
	      insideDisplay.updateToolTips();
		}
		else super.step(state);
   }
	
	
	 public void paintToMovie(Graphics g)
    {
		 if(consoleMode && moviePathSet){
			 synchronized(Display2DHack.this.simulation.state.schedule)
	        {
	        // only paint if it's appropriate
	        long steps = Display2DHack.this.simulation.state.schedule.getSteps();
	        if (steps > lastEncodedSteps &&
	            steps % getInterval() == 0 &&
	            Display2DHack.this.simulation.state.schedule.time() < Schedule.AFTER_SIMULATION)
	            {
	      	  Display2DHack.this.movieMaker.add(paint(g,true,false));
	            lastEncodedSteps = steps;
	            }
	        else paint(g,false,false);
	        }
		 }
		 else super.paintToMovie(g);
    }

}
