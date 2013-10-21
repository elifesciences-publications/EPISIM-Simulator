package sim.display;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Graphics;
import java.awt.Paint;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import episiminterfaces.EpisimSimulationDisplay;

import sim.SimStateServer;
import sim.SimStateServer.EpisimSimulationState;
import sim.app.episim.EpisimProperties;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.ModeServer;
import sim.app.episim.gui.EpisimGUIState;
import sim.app.episim.gui.ExtendedFileChooser;
import sim.app.episim.gui.ImageLoader;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.util.EpisimMovieMaker;
import sim.app.episim.util.Names;
import sim.app.episim.util.Scale;
import sim.display.Display2D.FieldPortrayal2DHolder;
import sim.display.Display2D.InnerDisplay2D;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.FieldPortrayal;
import sim.portrayal.FieldPortrayal2D;
import sim.portrayal.Portrayal;
import sim.util.gui.MovieMaker;
import sim.util.gui.NumberTextField;
import sim.util.gui.Utilities;
import sim.util.media.PDFEncoder;
import sim.util.media.PNGEncoder;


public class Display2DHack extends Display2D implements EpisimSimulationDisplay{
	private EpisimGUIState epiSimulation = null;
	
	
	private boolean moviePathSet = false;
	private EpisimMovieMaker episimMovieMaker;
	private EpisimCellBehavioralModelGlobalParameters globalCBMParameters;
	private Method cellColoringGetterMethod;
	private Method cellColoringSetterMethod;
	
	public Display2DHack(double width, double height, GUIState simulation) {

		super(width, height, simulation);
		optionButton.setVisible(false);
		for(MouseMotionListener listener :insideDisplay.getMouseMotionListeners())insideDisplay.removeMouseMotionListener(listener);
		for(MouseListener listener :insideDisplay.getMouseListeners())insideDisplay.removeMouseListener(listener);

		insideDisplay.addMouseListener(new MouseAdapter()
      {
      public void mouseClicked(MouseEvent e) 
          {
          if (handleMouseEvent(e)) { repaint(); return; }
          else
              {
              // we only care about mouse button 1.  Perhaps in the future we may eliminate some key modifiers as well
              int modifiers = e.getModifiers();
              if ((modifiers & e.BUTTON1_MASK) == e.BUTTON1_MASK)
                  {
                  final Point point = e.getPoint();
                  if( e.getClickCount() == 2 )
                      createInspectors( new Rectangle2D.Double( point.x, point.y, 1, 1 ),
                          Display2DHack.this.simulation );
                  if (e.getClickCount() == 1 || e.getClickCount() == 2)  // in both situations
                      performSelection( new Rectangle2D.Double( point.x, point.y, 1, 1 ));
                  repaint();
                  }
              }
          }
      
      // clear tool-tip updates
      public void mouseExited(MouseEvent e)
          {
          insideDisplay.lastToolTipEvent = null;  // do this no matter what
     //     if (handleMouseEvent(e)) { repaint(); return; }
          }
      });
		
		
		
		
		
		moviePathSet = EpisimProperties.getProperty(EpisimProperties.MOVIE_PATH_PROP) != null;
		
		
		if(moviePathSet && ModeServer.consoleInput()){ 
			movieButton.setEnabled(false);
			 insideDisplay = new EpisimInnerDisplay2D(width,height); 
			
			 display = new JScrollPane(insideDisplay,
		            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
		            JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		        display.setMinimumSize(new Dimension(0,0));
		        display.setBorder(null);
		        display.getHorizontalScrollBar().setBorder(null);
		        display.getVerticalScrollBar().setBorder(null);
		        port = display.getViewport();
		        insideDisplay.setViewRect(port.getViewRect());
		        insideDisplay.setOpaque(true);  // radically increases speed in OS X, maybe others
		        // Bug in Panther causes this color to be wrong, ARGH
//		        port.setBackground(UIManager.getColor("window"));  // make the nice stripes on MacOS X
		        insideDisplay.setBackground(UIManager.getColor("Panel.background"));
		        display.setBackground(UIManager.getColor("Panel.background")); // this is the one that has any affect
		        port.setBackground(UIManager.getColor("Panel.background"));
		        
		       
		        insideDisplay.addMouseListener(new MouseAdapter()
	            {
	            public void mouseClicked(MouseEvent e) 
	                {
	                if (handleMouseEvent(e)) { repaint(); return; }
	                else
	                    {
	                    // we only care about mouse button 1.  Perhaps in the future we may eliminate some key modifiers as well
	                    int modifiers = e.getModifiers();
	                    if ((modifiers & e.BUTTON1_MASK) == e.BUTTON1_MASK)
	                        {
	                        final Point point = e.getPoint();
	                        if( e.getClickCount() == 2 )
	                            createInspectors( new Rectangle2D.Double( point.x, point.y, 1, 1 ),
	                                Display2DHack.this.simulation );
	                        if (e.getClickCount() == 1 || e.getClickCount() == 2)  // in both situations
	                            performSelection( new Rectangle2D.Double( point.x, point.y, 1, 1 ));
	                        repaint();
	                        }
	                    }
	                }
	            
	            // clear tool-tip updates
	            public void mouseExited(MouseEvent e)
	                {
	                insideDisplay.lastToolTipEvent = null;  // do this no matter what
	             //   if (handleMouseEvent(e)) { repaint(); return; }
	                }

	           
	            });
	                
	        
	                
	                
	        // can't add this because Java thinks I no longer want to scroll
	        // the window via the scroll wheel, oops.  
	        /*
	          insideDisplay.addMouseWheelListener(new MouseWheelListener()
	          {
	          public void mouseWheelMoved(MouseWheelEvent e)
	          {
	          if (handleMouseEvent(e)) { repaint(); return; }
	          }
	          });
	        */

	        insideDisplay.setToolTipText("Display");  // sacrificial	      

		         // so it gets repainted first hopefully
		        add(display,BorderLayout.CENTER);

		        
		     // set ourselves up to quit when stopped
		        simulation.scheduleAtStart(new Steppable()   // to stop movie when simulation is stopped
		            {
		            public void step(SimState state) { SwingUtilities.invokeLater(new Runnable(){
							public void run() {
								startMovie();								
							}}); }
		            });
			
		}
		
		
		if(simulation instanceof EpisimGUIState) epiSimulation = (EpisimGUIState) simulation;
		
		optionPane.setIconImage(new ImageIcon(ImageLoader.class.getResource("icon.gif")).getImage());
		optionPane.addWindowListener(new WindowAdapter(){
			 public void windowOpened(WindowEvent e){
				 optionPane.setLocation(
						 ((int)(display.getLocation().x + (display.getWidth()/2) -(optionPane.getWidth()/2))),
						 ((int)(display.getLocation().y + (display.getHeight()/2) -(optionPane.getHeight()/2)))
				 );			 
				 }
		});
			
		
		
		//no unnecessary Entry: Show Console in the popup
		if(popup != null && popup.getComponentCount()>1){
			popup.remove(0);
			popup.remove(0);
		}
		
		for(Component comp :header.getComponents()){
			if(comp instanceof NumberTextField
				|| comp instanceof JComboBox) header.remove(comp);
		} 
	    // add the scale field
      NumberTextField scaleField = new NumberTextField("  Scale: ", 1.0, true)
          {
          public double newValue(double newValue)
              {
              if (newValue <= 0.0) newValue = currentValue;
              epiSimulation.pressWorkaroundSimulationPause();
              setScale(newValue);
              Scale.displayScale = getScale();
              port.setView(insideDisplay);
              //optionPane.xOffsetField.add *= (newValue / currentValue);
              optionPane.xOffsetField.setValue(insideDisplay.xOffset * newValue);
              //optionPane.yOffsetField.add *= (newValue / currentValue);
              optionPane.yOffsetField.setValue(insideDisplay.yOffset * newValue);
              epiSimulation.pressWorkaroundSimulationPlay();
              return newValue;
              }
          };
      scaleField.setToolTipText("Zoom in and out");
      header.add(scaleField);
      
      findCellColoringMethods();
      if(this.cellColoringGetterMethod != null && this.cellColoringSetterMethod != null){
      	
	      // add the scale field
      	Object defaultValue = null;
      	try{
	         defaultValue = this.cellColoringGetterMethod.invoke(this.globalCBMParameters, new Object[0]);
         }
         catch (Exception e1){
	         ExceptionDisplayer.getInstance().displayException(e1);
         }
         double defaultVal = 0;
         if(defaultValue != null){
         	defaultVal = defaultValue instanceof Integer ? (double)((Integer)defaultValue).intValue() : defaultValue instanceof Double ? ((Double)defaultValue).doubleValue() :0;
         }
	      NumberTextField cellColoringField = new NumberTextField("     Cell Coloring: ", defaultVal, true)
	      {
	          public double newValue(double newValue)
	          {
	              if (newValue > 0){
	            	  if(Integer.TYPE.isAssignableFrom(cellColoringGetterMethod.getReturnType())){
	            		  int val = (int) newValue;
	            		  try{
	                     cellColoringSetterMethod.invoke(globalCBMParameters, new Object[]{val});
	            		  }
	            		  catch (Exception e){
                     	 ExceptionDisplayer.getInstance().displayException(e);
	            		  }
	            	  }
	            	  else{
	            		  
	            		  try{
	                     cellColoringSetterMethod.invoke(globalCBMParameters, new Object[]{newValue});
	            		  }
	            		  catch (Exception e){
                     	 ExceptionDisplayer.getInstance().displayException(e);
	            		  }
	            	  }
	            	  currentValue = newValue;
	            	  return newValue;
	              }             
	              return currentValue;
	          }
	      };
	      scaleField.setToolTipText("Change Cell Coloring Mode");
	      header.add(cellColoringField);
      }
           
	}
	
	private void findCellColoringMethods(){
		//cellColoringMode
		if(ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters() != null){
			globalCBMParameters = ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters();
			Method[] methods =globalCBMParameters.getClass().getMethods();
			for(Method m : methods){
				if(m.getName().startsWith("get") 
						&& (m.getName().trim().toLowerCase().contains(Names.CELL_COLORING_MODE_NAME_I)
						  || m.getName().trim().toLowerCase().contains(Names.CELL_COLORING_MODE_NAME_II)
						  || m.getName().trim().toLowerCase().contains(Names.CELL_COLORING_MODE_NAME_III))){
						this.cellColoringGetterMethod = m;
						break;
				}
			}
			if(this.cellColoringGetterMethod != null){
				String setterName = "s"+this.cellColoringGetterMethod.getName().trim().substring(1);
				for(Method m : methods){
					if(m.getName().equals(setterName)){
						this.cellColoringSetterMethod=m;
						break;
					}
				}
			}			
		}		
	}
	
	
	
	public double getDisplayScale(){
		 return this.getScale();
	}
	public void setPortrayalVisible(String name, boolean visible){
		FieldPortrayal2DHolder holder =getPortrayalHolder(name);
		if(holder != null){
			holder.visible = visible;
			insideDisplay.repaint();
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
	public void changePortrayal(String name, FieldPortrayal2D portrayal){
		FieldPortrayal2DHolder holder = getPortrayalHolder(name);
		if(holder != null){
			holder.portrayal = portrayal;
		}
	}
	
	
	public void startMovie()
   {
		if(ModeServer.consoleInput() && moviePathSet){
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
	
	 public void takeSnapshot()
    {
			
		 SimStateServer.getInstance().getEpisimGUIState().pressWorkaroundSimulationPause(); 

        // snap the shot FIRST
        Graphics g = insideDisplay.getGraphics();
        BufferedImage img = insideDisplay.paint(g,true,false);  // notice we're painting to a non-shared buffer
      
                            
        g.dispose();  // because we got it with getGraphics(), we're responsible for it
                    
        // Ask what kind of thing we want to save?
        final int CANCEL_BUTTON = 0;
        final int PNG_BUTTON = 1;
        final int SVG_BUTTON = 2;
        int result = PNG_BUTTON;  //  default
        Object[] options = { "Cancel", "Save to PNG ", "Save to SVG"};
            result = JOptionPane.showOptionDialog(getFrame(), "Save window snapshot to what kind of file format?", "Save Format", 
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);
       
                    
        if (result == PNG_BUTTON) 
            {
            // NOW pop up the save window
            FileDialog fd = new FileDialog(getFrame(), 
                "Save Snapshot as 24-bit PNG...", FileDialog.SAVE);
            fd.setFile("Untitled.png");
            fd.setVisible(true);
            if (fd.getFile()!=null) try
                                        {
                                        OutputStream stream = new BufferedOutputStream(new FileOutputStream(
                                                new File(fd.getDirectory(), Utilities.ensureFileEndsWith(fd.getFile(),".png"))));
                                        PNGEncoder tmpEncoder = new
                                            PNGEncoder(img, false,PNGEncoder.FILTER_NONE,9);
                                        stream.write(tmpEncoder.pngEncode());
                                        stream.close();
                                        }
                catch (Exception e) { e.printStackTrace(); }
            }
        else if (result == SVG_BUTTON)
            {
           ExtendedFileChooser fd = new ExtendedFileChooser("svg");
           
            
            if (fd.showSaveDialog(getFrame())==ExtendedFileChooser.APPROVE_OPTION){ 
            	try
               {
                                        boolean oldprecise = precise;
                                        precise = true;
                                       
                                       saveSVGImage(port, fd.getSelectedFile());
                                        precise = oldprecise;
                 }
                catch (Exception e) { e.printStackTrace(); }
            }
        else // (result == 0)  // Cancel
            {
            // don't bother
            }
        }
     	
       
				SimStateServer.getInstance().getEpisimGUIState().pressWorkaroundSimulationPlay();
			
	}
    
    
   private void saveSVGImage(Component comp, File file) throws IOException{
   	if(comp != null && file != null){
   		if(!file.getAbsolutePath().toLowerCase().endsWith(".svg")){
   			file = new File(file.getAbsolutePath()+".svg");
   		}
   		DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
   	     // Create an instance of org.w3c.dom.Document.
   	     String svgNS = "http://www.w3.org/2000/svg";
   	     Document document = domImpl.createDocument(svgNS, "svg", null);

   	     // Create an instance of the SVG Generator.
   	     SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
   	     
   	     comp.paint(svgGenerator);
   	     
   	     boolean useCSS = true; // we want to use CSS style attributes
   	     FileOutputStream fileOut = new FileOutputStream(file);
   	     Writer out = new OutputStreamWriter(fileOut, "UTF-8");
   	     svgGenerator.stream(out, useCSS);
   	     fileOut.flush();
   	     fileOut.close();
   	     svgGenerator.dispose();
   	}
   }
    
	
	public void stopMovie()
   {
		if(ModeServer.consoleInput() && moviePathSet){
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
		if(ModeServer.consoleInput() && moviePathSet){
			
	      
			if (shouldUpdate())       // time to update!
         {
	         if (insideDisplay.isShowing() && 
	             (getFrame().getExtendedState() & java.awt.Frame.ICONIFIED) == 0)   // not minimized on the Mac
	             {
	         	if(isMacOSX){
	         		insideDisplay.repaint();
	         	}
	         	else{
		         	Graphics g = insideDisplay.getGraphics();
		            insideDisplay.paintComponent(g,true);
		            g.dispose();
	         	}
	             }
	         else if (episimMovieMaker != null)  // we're not being displayed but we still need to output to a movie
	         {
	             insideDisplay.paintToMovie(null);
	         }
	         insideDisplay.updateToolTips();
        }
     }
	  else{
		  if (shouldUpdate())       // time to update!
        {
        if (insideDisplay.isShowing() && 
            (getFrame().getExtendedState() & java.awt.Frame.ICONIFIED) == 0)   // not minimized on the Mac
            {
        	if(isMacOSX){
        		insideDisplay.repaint();
        	}
        	else{
	         	Graphics g = insideDisplay.getGraphics();
	            insideDisplay.paintComponent(g, true);
	            g.dispose();
        	}
         }
        else if (movieMaker != null)  // we're not being displayed but we still need to output to a movie
            {
            insideDisplay.paintToMovie(null);
            }
        insideDisplay.updateToolTips();
        }

     }
   }
	
	public static boolean isMacOSX(){
		return Display2D.isMacOSX();
	}
	
	
	public class EpisimInnerDisplay2D extends InnerDisplay2D{
		public EpisimInnerDisplay2D(double width, double height){
			super(width, height);
		}
		public void paintToMovie(Graphics g)
      {
      synchronized(Display2DHack.this.simulation.state.schedule)
          {
          // only paint if it's appropriate
          long steps = Display2DHack.this.simulation.state.schedule.getSteps();
          if (steps > lastEncodedSteps &&
         		 shouldUpdate() &&
              Display2DHack.this.simulation.state.schedule.time() < Schedule.AFTER_SIMULATION)
              {
         	
         	 Display2DHack.this.episimMovieMaker.add(paint(g,true,false));
              lastEncodedSteps = steps;
              }
          else paint(g,false,false);
          }
      }
		 public void paintComponent(Graphics g, boolean buffer)
       {
       synchronized(Display2DHack.this.simulation.state.schedule)  // for time()
           {
           if (episimMovieMaker!=null)  // we're writing a movie
               insideDisplay.paintToMovie(g);
           else paint(g,buffer,true);
           }
       }
	}
	public InnerDisplay2D getInsideDisplay() {
      return insideDisplay;
   }


   public void attach(Portrayal portrayal, String name, Rectangle2D.Double bounds, boolean visible) {
   	if(portrayal instanceof FieldPortrayal2D){
   		super.attach((FieldPortrayal2D) portrayal, name, bounds, visible);
   	}
   }
	
   public void attach(Portrayal portrayal, String name) {
   	if(portrayal instanceof FieldPortrayal2D){
   		super.attach((FieldPortrayal2D) portrayal, name);
   	}   
   } 
}
