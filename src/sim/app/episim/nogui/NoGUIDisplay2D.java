package sim.app.episim.nogui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolTip;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import episiminterfaces.EpisimSimulationDisplay;
import sim.SimStateServer;
import sim.app.episim.EpisimProperties;
import sim.app.episim.EpisimExceptionHandler;
import sim.app.episim.ModeServer;
import sim.app.episim.gui.EpisimGUIState;
import sim.app.episim.gui.ExtendedFileChooser;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.util.EpisimMovieMaker;
import sim.app.episim.util.Names;
import sim.app.episim.util.Scale;
import sim.display.Console;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.display.IconLoaderHack;
import sim.display.Manipulating2D;
import sim.display.Prefs;
import sim.display.SimApplet;


import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.FieldPortrayal2D;
import sim.portrayal.LocationWrapper;
import sim.portrayal.Portrayal;
import sim.portrayal.SimplePortrayal2D;
import sim.util.Bag;
import sim.util.gui.Utilities;
import sim.util.gui.LabelledList;
import sim.util.gui.MovieMaker;
import sim.util.gui.NumberTextField;
import sim.util.media.PDFEncoder;
import sim.util.media.PNGEncoder;




public class NoGUIDisplay2D extends JComponent implements Steppable, EpisimSimulationDisplay, Manipulating2D{
   private EpisimGUIState epiSimulation = null;
	
   protected boolean precise = false;
	private boolean moviePathSet = false;
	private EpisimMovieMaker episimMovieMaker;
	private boolean automatedPNGSnapshotsEnabled = false;
   public String DEFAULT_PREFERENCES_KEY = "Display2D";
   String preferencesKey = DEFAULT_PREFERENCES_KEY;  // default 
	private Method cellColoringGetterMethod;
	private Method cellColoringSetterMethod;
	private NumberTextField cellColoringField =null;
	private EpisimCellBehavioralModelGlobalParameters globalCBMParameters;	
	
	
	
   /** If you have more than one Display2D in your simulation and you want them to have
       different preferences, set each to a different key value.    The default value is DEFAULT_PREFERENCES_KEY.
       You may not have a key which ends in a forward slash (/) when trimmed  
       Key may be set to null (the default).   */
   public void setPreferencesKey(String s)
       {
       if (s.trim().endsWith("/"))
           throw new RuntimeException("Key ends with '/', which is not allowed");
       else preferencesKey = s;
       }
   public String getPreferencesKey() { return preferencesKey; }
	
   /** Option pane */
   public class OptionPane extends JPanel
   {
      // buffer stuff
      int buffering;
      
      JRadioButton useNoBuffer = new JRadioButton("By Drawing Separate Rectangles");
      JRadioButton useBuffer = new JRadioButton("Using a Stretched Image");
      JRadioButton useDefault = new JRadioButton("Let the Program Decide How");
      ButtonGroup usageGroup = new ButtonGroup();
      
      JCheckBox antialias = new JCheckBox("Antialias Graphics");
      JCheckBox alphaInterpolation = new JCheckBox("Better Transparency");
      JCheckBox interpolation = new JCheckBox("Bilinear Interpolation of Images");
      JCheckBox tooltips = new JCheckBox("Tool Tips");
      
      JButton systemPreferences = new JButton("MASON");
      JButton appPreferences = new JButton("Simulation");
              
      NumberTextField xOffsetField = new NumberTextField(0,1,50)
          {
          public double newValue(final double val)
              {
              double scale = getScale();
              insideDisplay.xOffset = val / scale;
              NoGUIDisplay2D.this.repaint();  // redraw the inside display
              return insideDisplay.xOffset * scale;
              }
          };
          
      NumberTextField yOffsetField = new NumberTextField(0,1,50)
          {
          public double newValue(final double val)
              {
              double scale = getScale();
              insideDisplay.yOffset = val / scale;
              NoGUIDisplay2D.this.repaint();  // redraw the inside display
              return insideDisplay.yOffset * scale;
              }
          };

      ActionListener listener = null;
              
      OptionPane(String title)
          {
          this.setLayout(new BorderLayout());
          useDefault.setSelected(true);
          useNoBuffer.setToolTipText("<html>When not using transparency on Windows/XWindows,<br>this method is often (but not always) faster</html>");
          usageGroup.add(useNoBuffer);
          usageGroup.add(useBuffer);
          useBuffer.setToolTipText("<html>When using transparency, <i>or</i> when on a Mac,<br>this method is usually faster, but may require more<br>memory (especially on Windows/XWindows) --<br>increasing heap size can help performance.</html>");
          usageGroup.add(useDefault);
          
          JPanel p2 = new JPanel();
          
          Box b = new Box(BoxLayout.Y_AXIS);
          b.add(useNoBuffer);
          b.add(useBuffer);
          b.add(useDefault);
          JPanel p = new JPanel();
          p.setLayout(new BorderLayout());
          p.setBorder(new javax.swing.border.TitledBorder("Draw Grids of Rectangles..."));
          p.add(b,BorderLayout.CENTER);
          p2.setLayout(new BorderLayout());
          p2.add(p,BorderLayout.NORTH);

          LabelledList l = new LabelledList("Offset in Pixels");
          l.addLabelled("X Offset", xOffsetField);
          l.addLabelled("Y Offset", yOffsetField);
          p2.add(l,BorderLayout.CENTER);
          this.add(p2,BorderLayout.NORTH);

          b = new Box(BoxLayout.Y_AXIS);
          b.add(antialias);
          b.add(interpolation);
          b.add(alphaInterpolation);
          b.add(tooltips);
          p = new JPanel();
          p.setLayout(new BorderLayout());
          p.setBorder(new javax.swing.border.TitledBorder("Graphics Features"));
          p.add(b,BorderLayout.CENTER);
          this.add(p,BorderLayout.CENTER);
          
          listener = new ActionListener()
              {
              public void actionPerformed(ActionEvent e)
                  {
                  useTooltips = tooltips.isSelected();
                  if (useDefault.isSelected())
                      buffering = FieldPortrayal2D.DEFAULT;
                  else if (useBuffer.isSelected())
                      buffering = FieldPortrayal2D.USE_BUFFER;
                  else buffering = FieldPortrayal2D.DONT_USE_BUFFER;
                  insideDisplay.setupHints(antialias.isSelected(), alphaInterpolation.isSelected(), interpolation.isSelected());
                  NoGUIDisplay2D.this.repaint();  // redraw the inside display
                  }
              };
          useNoBuffer.addActionListener(listener);
          useBuffer.addActionListener(listener);
          useDefault.addActionListener(listener);
          antialias.addActionListener(listener);
          alphaInterpolation.addActionListener(listener);
          interpolation.addActionListener(listener);
          tooltips.addActionListener(listener);

          // add preferences
                      
          b = new Box(BoxLayout.X_AXIS);
          b.add(new JLabel(" Save as Defaults for "));
          b.add(appPreferences);
          b.add(systemPreferences);
          this.add(b, BorderLayout.SOUTH);

          systemPreferences.putClientProperty( "JComponent.sizeVariant", "mini" );
          systemPreferences.putClientProperty( "JButton.buttonType", "bevel" );
          systemPreferences.addActionListener(new ActionListener()
              {
              public void actionPerformed(ActionEvent e)
                  {
                  String key = getPreferencesKey();
                  savePreferences(Prefs.getGlobalPreferences(key));
                                      
                  // if we're setting the system preferences, remove the local preferences to avoid confusion
                  Prefs.removeAppPreferences(simulation, key);
                  }
              });
                      
          appPreferences.putClientProperty( "JComponent.sizeVariant", "mini" );
          appPreferences.putClientProperty( "JButton.buttonType", "bevel" );
          appPreferences.addActionListener(new ActionListener()
              {
              public void actionPerformed(ActionEvent e)
                  {
                  String key = getPreferencesKey();
                  savePreferences(Prefs.getAppPreferences(simulation, key));
                  }
              });

     /*     setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
          setResizable(false);
          pack();*/

          }
              
      /** Saves the Option Pane Preferences to a given Preferences Node */
      void savePreferences(Preferences prefs)
          {
          try
              {
              prefs.putInt(DRAW_GRIDS_KEY,
                  useNoBuffer.isSelected() ? 0 : 
                  useBuffer.isSelected() ? 1 : 2);
              prefs.putDouble(X_OFFSET_KEY, xOffsetField.getValue());
              prefs.putDouble(Y_OFFSET_KEY, yOffsetField.getValue());
              prefs.putBoolean(ANTIALIAS_KEY, antialias.isSelected());
              prefs.putBoolean(BETTER_TRANSPARENCY_KEY, alphaInterpolation.isSelected());
              prefs.putBoolean(INTERPOLATION_KEY, interpolation.isSelected());
              prefs.putBoolean(TOOLTIPS_KEY, tooltips.isSelected());
                                                      
           //   if (!Prefs.save(prefs)) Utilities.inform ("Preferences Cannot be Saved", "Your Java system can't save preferences.  Perhaps this is an applet?", this);
              }
          catch (java.security.AccessControlException e) { } // it must be an applet
          }
                      
                      
      static final String DRAW_GRIDS_KEY = "Draw Grids";
      static final String X_OFFSET_KEY = "X Offset";
      static final String Y_OFFSET_KEY = "Y Offset";
      static final String ANTIALIAS_KEY = "Antialias";
      static final String BETTER_TRANSPARENCY_KEY = "Better Transparency";
      static final String INTERPOLATION_KEY = "Bilinear Interpolation";
      static final String TOOLTIPS_KEY = "Tool Tips";
              
      /** Resets the Option Pane Preferences by loading from the preference database */
      void resetToPreferences()
          {
          try
              {
              Preferences systemPrefs = Prefs.getGlobalPreferences(getPreferencesKey());
              Preferences appPrefs = Prefs.getAppPreferences(simulation, getPreferencesKey());
              int val = appPrefs.getInt(DRAW_GRIDS_KEY, 
                  systemPrefs.getInt(DRAW_GRIDS_KEY,
                      useNoBuffer.isSelected() ? 0 : 
                      useBuffer.isSelected() ? 1 : 2));
              if (val == 0) useNoBuffer.setSelected(true);
              else if (val == 1) useBuffer.setSelected(true);
              else // (val == 0) 
                  useDefault.setSelected(true);
              xOffsetField.setValue(xOffsetField.newValue(appPrefs.getDouble(X_OFFSET_KEY,
                          systemPrefs.getDouble(X_OFFSET_KEY, 0))));
              yOffsetField.setValue(yOffsetField.newValue(appPrefs.getDouble(Y_OFFSET_KEY,
                          systemPrefs.getDouble(Y_OFFSET_KEY, 0))));
              antialias.setSelected(appPrefs.getBoolean(ANTIALIAS_KEY,
                      systemPrefs.getBoolean(ANTIALIAS_KEY, false)));
              alphaInterpolation.setSelected(appPrefs.getBoolean(BETTER_TRANSPARENCY_KEY,
                      systemPrefs.getBoolean(BETTER_TRANSPARENCY_KEY, false)));
              interpolation.setSelected(appPrefs.getBoolean(INTERPOLATION_KEY,
                      systemPrefs.getBoolean(INTERPOLATION_KEY, false)));
              tooltips.setSelected(appPrefs.getBoolean(TOOLTIPS_KEY,
                      systemPrefs.getBoolean(TOOLTIPS_KEY, false)));
              // trigger resets by calling the listener.  Don't bother with an event
              listener.actionPerformed(null);
              }
          catch (java.security.AccessControlException e) { } // it must be an applet
          }
   }
   
   /** Removes all mouse listeners, mouse motion listeners, and Key listeners from this component.  Mostly used for kiosk mode stuff -- see the Howto */
   public void removeListeners()
   {
       // moved to the Display2D ot be at the same level as handleEvent
       insideDisplay.removeListeners();
   }
   public double getDisplayScale(){
   	return getScale();
   }
   
   /** The object which actually does all the drawing.  Perhaps we should move this out. */
   public class InnerDisplay2D extends JComponent
       {
       /** Image buffer for doing buffered draws, mostly for screenshots etc. */
       BufferedImage buffer = null;

       /** The width of the display when the scale is 1.0 */
       public double width;
       /** The height of the display when the scale is 1.0 */
       public double height;
       /** x offset */
       public double xOffset;
       /** y offset */
       public double yOffset;
       
       /** @deprecated Use Display2D.removeListeners instead. */
       public void removeListeners()
           {
           MouseListener[] mls = (MouseListener[])(getListeners(MouseListener.class));
           for(int x = 0 ; x < mls.length; x++)
               { removeMouseListener(mls[x]); }
           MouseMotionListener[] mmls = (MouseMotionListener[])(getListeners(MouseMotionListener.class));
           for(int x = 0 ; x < mmls.length; x++)
               { removeMouseMotionListener(mmls[x]); }
           KeyListener[] kls = (KeyListener[])(getListeners(KeyListener.class));
           for(int x = 0 ; x < kls.length; x++)
               { removeKeyListener(kls[x]); }
           }
                       
       /** Creates an InnerDisplay2D with the provided width and height. */
       public InnerDisplay2D(double width, double height)
       {
           this.width = width;
           this.height = height;
           setupHints(false,false,false);  // go for speed
       }
       
       /** Overloaded to return (width * scale, height * scale) */
       public Dimension getPreferredSize() 
           { return new Dimension((int)(width*getScale()),(int)(height*getScale())); }
           
       /** Overloaded to return (width * scale, height * scale) */
       public Dimension getMinimumSize() 
           { return getPreferredSize();  }

       /** Overloaded to return (width * scale, height * scale) */
       public Dimension getMaximumsize()
           { return getPreferredSize(); }
       
       /** Paints a movie, by drawing to a buffer, then
           encoding the buffer to disk, then optionally 
           writing the buffer to the provided Graphics2D. 
           If the Graphics2D is null, it's just written out to disk.
           This method will only write to disk when "appropriate", that is,
           if the current schedule has advanced to the point that a new
           frame is supposed to be outputted (given the frame rate of the
           movie).  In any rate, it'll write to the Graphic2D if
           provided. */
       public void paintToMovie(Graphics g)
           {
           // although presently paintToMovie is called solely from paintComponent,
           // which already has synchronized on the schedule, we do this anyway for
           // good measure.  See stopMovie() and startMovie() for why synchronization
           // is important.
           synchronized(NoGUIDisplay2D.this.simulation.state.schedule)
               {
               // only paint if it's appropriate
               long steps = NoGUIDisplay2D.this.simulation.state.schedule.getSteps();
               if (steps > lastEncodedSteps &&
                   NoGUIDisplay2D.this.simulation.state.schedule.getTime() < Schedule.AFTER_SIMULATION)
                   {
                   NoGUIDisplay2D.this.movieMaker.add(paint(g,true,false));
                   lastEncodedSteps = steps;
                   }
               else paint(g,false,false);
               }
           }
       
       /** Hints used to draw objects to the screen or to a buffer */
       public RenderingHints unbufferedHints;
       /** Hints used to draw the buffered image to the screen */
       public RenderingHints bufferedHints;
       
       /** The default method for setting up the given hints.
       By default they suggest that Java2D emphasize efficiency over prettiness.*/
   public void setupHints(boolean antialias, boolean niceAlphaInterpolation, boolean niceInterpolation)
       {
       unbufferedHints = new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);  // in general
       unbufferedHints.put(RenderingHints.KEY_INTERPOLATION,
           niceInterpolation ? RenderingHints.VALUE_INTERPOLATION_BILINEAR :
           RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
       // dunno what to do here about antialiasing on MacOS X
       // -- if it's on, then circles can get drawn as squares (see woims demo at 1.5 scale)
       // -- but if it's off, then stuff gets antialiased in pictures but not on a screenshot.
       // My inclination is to leave it off. 
       unbufferedHints.put(RenderingHints.KEY_ANTIALIASING, 
           antialias ? RenderingHints.VALUE_ANTIALIAS_ON :
           RenderingHints.VALUE_ANTIALIAS_OFF);
       unbufferedHints.put(RenderingHints.KEY_TEXT_ANTIALIASING,
           antialias ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON :
           RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
       unbufferedHints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, 
           niceAlphaInterpolation ? 
           RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY :
           RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);

       bufferedHints = new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);  // in general
       bufferedHints.put(RenderingHints.KEY_INTERPOLATION,
           niceInterpolation ? RenderingHints.VALUE_INTERPOLATION_BILINEAR :
           RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
       // similarly
       bufferedHints.put(RenderingHints.KEY_ANTIALIASING, 
           antialias ? RenderingHints.VALUE_ANTIALIAS_ON :
           RenderingHints.VALUE_ANTIALIAS_OFF);
       bufferedHints.put(RenderingHints.KEY_TEXT_ANTIALIASING,
           antialias ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON :
           RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
       bufferedHints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, 
           niceAlphaInterpolation ? 
           RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY :
           RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
       }
       
       java.lang.ref.WeakReference toolTip = new java.lang.ref.WeakReference(null);
       public JToolTip createToolTip()
           {
           JToolTip tip = super.createToolTip();
           toolTip = new java.lang.ref.WeakReference(tip);
           return tip;
           }
               
       protected MouseEvent lastToolTipEvent = null;
       // could be called from the model thread OR the swing thread -- must be careful
       public String getToolTipText(MouseEvent event)
           {
           if (useTooltips)
               {
               lastToolTipEvent = event;
               final Point point = event.getPoint();
               return createToolTipText( new Rectangle2D.Double(point.x,point.y,1,1), NoGUIDisplay2D.this.simulation);
               }
           else return null;
           }
       
       String lastToolTipText = null;
       // this will be called from the model thread most likely, so we need
       // to make sure that we actually do the updating in an invokeLater...
       public void updateToolTips()
           {
           if (useTooltips && lastToolTipEvent != null)
               {
               final String s = getToolTipText(lastToolTipEvent);
               if (s==null || !s.equals(lastToolTipText))
                   {
                   // ah, we need to update.
                   SwingUtilities.invokeLater(new Runnable() 
                       {
                       public void run() 
                           {
                           setToolTipText(lastToolTipText = s);
                           JToolTip tip = (JToolTip)(toolTip.get());
                           if (tip!=null && tip.getComponent()==InnerDisplay2D.this) tip.setTipText(s);
                           }
                       });
                   }
               }
           }
       static final int MAX_TOOLTIP_LINES = 10;
       public String createToolTipText( Rectangle2D.Double rect, final GUIState simulation )
           {
           String s = "<html><font face=\"" +
               getFont().getFamily() + "\" size=\"-1\">";
           Bag[] hitObjects = objectsHitBy(rect);
           int count = 0;
           for(int x=0;x<hitObjects.length;x++)
               {
               FieldPortrayal2DHolder p = (FieldPortrayal2DHolder)(portrayals.get(x));
               for( int i = 0 ; i < hitObjects[x].numObjs ; i++ )
                   {
                   if (count > 0) s += "<br>";
                   if (count >= MAX_TOOLTIP_LINES) { return s + "...<i>etc.</i></font></html>"; }
                   count++;
                   String status = p.portrayal.getStatus((LocationWrapper) (hitObjects[x].objs[i]));
                   if (status != null) s += status;  // might return null, sort of meaning "leave me alone"
                   }
               }
           if (count==0) return null;
           s += "</font></html>";
           return s;
           }
       
       boolean paintLock = false;
       /** Swing's equivalent of paint(Graphics g).   Called by repaint().  In turn calls
           paintComponent(g,false);   You should not call this method directly.  Instead you probably want to
           call paintComponent(Graphics, buffer).  */
       public synchronized void paintComponent(final Graphics g)
           {
           // Swing gets overreaching when scaling in.  This allows us to temporarily refuse to paint so we don't do so multiple times.
           if (paintLock) return; 
           // I'm likely being updated due to scrolling, so change rect
           if (SwingUtilities.isEventDispatchThread())
               setViewRect(port.getViewRect());
           paintComponent(g,false);
           }
       
       /** The top-level repainting method.  If we're writing to a movie, we do a paintToMovie
       (which also does a buffered paint to the screen) else we do an ordinary paint.
       <tt>buffer</tt> determines if we do our ordinary paints buffered or not.
       @deprecated use paintComponent() or paint(...) */
       public void paintComponent(Graphics g, boolean buffer)
           {
           synchronized(NoGUIDisplay2D.this.simulation.state.schedule)  // for time()
               {
               if (movieMaker!=null)  // we're writing a movie
                   insideDisplay.paintToMovie(g);
               else paint(g,buffer,true);
               }
           }
           
           
       /** Computes the expected clip for drawing. */
       Rectangle2D computeClip()
           {
           // This line is in case we're drawing from something other than the main
           // event loop.  In this situation, we need to get the view rect and figure
           // our clip region accordingly.  But port.getViewRect() is a problem because it's
           // calling magic synchronized code inside it, and so can deadlock if Swing is
           // locked (which it likely is if it's ALSO calling paintComponent and we beat
           // it to it).  So we maintain the last rect here using our own magic getViewRect
           // function (see explanations below)
           Rectangle2D clip = getViewRect();
           
           // centering
           double scale = getScale();
           int origindx = 0;
           int origindy = 0;
           if (clip.getWidth() > (width * scale))
               origindx = (int)((clip.getWidth() - width*scale)/2);
           if (clip.getHeight() > (height*scale))
               origindy = (int)((clip.getHeight() - height*scale)/2);
               
           if (isClipping())               
               {
               Dimension s = getPreferredSize();
               clip = clip.createIntersection(new Rectangle2D.Double(origindx,origindy,s.width,s.height));
               }
           return clip;
           }


       /** Paints an image to the screen either buffered or unbuffered.  If buffered, the
           buffer is returned for your convenience.   If SHARED is true, then the buffer
           is shared internally, so if
           you want to hold onto it without it changing, you'll need to make a copy.  If SHARED
           is false, then the buffer is given to you to do as you like and a new buffer is
           created next time (less efficient).
           This method is called from Swing
           when the window is resized or scrolled or whatnot, but it's also called as
           a result of the underlying model thread advancing a step so we can see
           what happened.  In the second case, the method could conceivably be
           called both from the Swing event thread (if making a movie, or if in MacOS X)
           or from the underlying model thread (if in Windows or X Windows -- to change
           this, see Display2D.step).  This is important because this if we're being called
           from the Swing event thread (repaint/movie/MacOS X), we must ensure that we're
           not updating the model at the same time the model is changing, and to do that
           we lock on the simulation's schedule so we know the model is done with step()
           methods and nothing's moving.  But because we're blocking on the schedule,
           a deadlock opportunity arises.  For example, what if some foolish person writes
           a step() method in the model which calls some blocking item in the Swing event
           thread?  Then they're blocking waiting for the Swing event thread to come
           available again, but the event thread is blocking because we're waiting for them
           to get out of the model.  A similar problem could arise if in implementing a
           portrayal's draw method the user decides to call some blocking item in Swing,
           because this paint() method could conceivably be called from the model itself
           (in Windows/XWindows), and it's possible that at the very same time the user
           has pressed the stop button or done some operation in Swing which is blocking
           waiting for the schedule.  Long story short, try not to call a blocking method
           on Swing from inside this method. */
       public BufferedImage paint(final Graphics graphics, boolean buffered, boolean shared)
           {
           synchronized(NoGUIDisplay2D.this.simulation.state.schedule)
               {
               BufferedImage result = null;
               Rectangle2D clip = computeClip();
               if (!buffered)
                   paintUnbuffered((Graphics2D)graphics,clip);
               else
                   result= paintBuffered((Graphics2D)graphics,clip);
               if (!shared) buffer = null; // kill it so paintBuffered(graphics,clip) makes a new one next time
               if (result != null) result.flush();  // just in case
               return result;
               }
           }
                
       /** Draws the image into a buffer, then IF graphics is not null,
           draws the resulting buffer to the graphics.  Returns the buffer,
           which is shared internally, so you need to copy out of it as soon as possible. */
       BufferedImage paintBuffered(final Graphics2D graphics, final Rectangle2D clip)
           {
           // make buffer big enough
           double ww = clip.getWidth();
           double hh = clip.getHeight();
           if (buffer==null || (buffer.getWidth(null)) != ww || (buffer.getHeight(null)) != hh)
               // note < would be more efficient than != but
               // it would create incorrect-sized images for snapshots,
               // and it's more memory wasteful anyway
               {
              // buffer = getGraphicsConfiguration().createCompatibleImage((int)ww,(int)hh);
               buffer =  new BufferedImage((int)ww,(int)hh, BufferedImage.TYPE_INT_RGB);
               }
           
           // draw into the buffer
           Graphics2D g = (Graphics2D)(buffer.getGraphics());
           g.setColor(port.getBackground());
           g.fillRect(0,0,buffer.getWidth(null),buffer.getHeight(null));
           g.translate(-(int)clip.getX(),-(int)clip.getY());
           paintUnbuffered(g,clip);
           g.dispose();  // because we got it with getGraphics(), we're responsible for it
           
           // paint and return the buffer
           if (graphics!=null)
               {
               graphics.setRenderingHints(bufferedHints);
               graphics.drawImage(buffer,(int)(clip.getX()),(int)(clip.getY()),null);
               }
           return buffer;
           }
       
       /** Paints an image unbuffered inside the provided clip. Not synchronized.
           You should probably call paintComponent() instead. */
       void paintUnbuffered(Graphics2D g, Rectangle2D clip)
           {
           if (g==null) return;
           
           g.setRenderingHints(unbufferedHints);

           // dunno if we want this
           if (isClipping()) g.setClip(clip);
           if (clip.getWidth()!=0 && clip.getHeight()!=0)
               {
               // presently not scaled
               if (backdrop!=null)
                   {
                   g.setPaint(backdrop);
                   g.fillRect((int)clip.getX(),(int)clip.getY(),(int)clip.getWidth(),(int)clip.getHeight());
                   }
               
              
               
               Iterator iter = portrayals.iterator();
               while (iter.hasNext())
               {
               FieldPortrayal2DHolder p = (FieldPortrayal2DHolder)(iter.next());
               if (p.visible)
                   {
                   // set buffering if necessary
                   int buf = p.portrayal.getBuffering();
                   p.portrayal.setBuffering(optionPane.buffering);
                   
                   // MacOS X 10.3 Panther has a bug which resets the clip, YUCK
                   g.setClip(g.getClip());
                   
                   // do the drawing
                   p.portrayal.draw(p.portrayal.getField(), // I could have passed null in here too
                       g, getDrawInfo2D(p, clip));
                   
                   // reset the buffering if necessary
                   p.portrayal.setBuffering(buf);
                   }
               }
               }
           }

       /*  TO FIX A SUBTLE BUG.  Can't call getViewRect() to get the proper
           clipping rect, because getViewRect calls some unknown synchronized gunk
           further up in Swing; thus if I'm in Windoze and splat to the screen from my own
           thread, and at the same time the Swing thread is trying to draw me, we
           have a problem -- it grabs the unknown gunk lock, I synchronize on this,
           then I try to call getViewRect on the port and lock, and we're both hung.
           if we do redrawing via repaint() like in MacOS X, then we're fine, but if
           we do it a-la X or Windows, this bug rears its ugly head.  So we get the
           most recent viewRect and set it here and keep it for when the redraw needs
           it, so it doesn't have to ask for it from Swing (which could be locked). */        

       Rectangle viewRect = new Rectangle(0,0,0,0);  // no access except via synchronization
       
       /** Lock for the viewRect above.  Don't want to lock on the Display2D itself. */
       final Object viewRectLock = new Object();
       
       /** Gets the last viewRect */
       Rectangle getViewRect()
           {
           synchronized(viewRectLock)
               {
               return new Rectangle(viewRect);
               }
           }
       
       /** Sets the viewRect to a new value */
       void setViewRect(Rectangle rect)
           {
           synchronized(viewRectLock)
               {
               viewRect = new Rectangle(rect);
               }
           }
       }    
       
   /** Holds all the relevant information for a given FieldPortrayal. */
   class FieldPortrayal2DHolder
       {
       /** The translation and scale of the FieldPortrayal.  Presently this
           is always 0,0 translation and 1.0 scale, but we'll allow the
           user to change this soon. */
       public Rectangle2D.Double bounds;
       /** The portrayal proper */
       public FieldPortrayal2D portrayal;
       /** The name of the portrayal, as shown in the Layers menu on the Display2D window */
       public String name;
       /** The menu item of the portrayal, in the Layers menu. */
       public JCheckBoxMenuItem menuItem;
       /** Whether we should draw the portrayal on updates */
       public boolean visible;
       /** Returns the portrayal's name in the Layers menu */
       public String toString() { return name; }
       /** Creates a menu item which selects or unselects the portrayal for drawing. */
       public FieldPortrayal2DHolder(FieldPortrayal2D p, String n, Rectangle2D.Double bounds, boolean visible)
           {
           this.bounds = bounds;
           portrayal=p; 
           name=n;
           this.visible = visible;
           menuItem = new JCheckBoxMenuItem(name,visible);
           menuItem.addActionListener(new ActionListener()
               {
               public void actionPerformed(ActionEvent e)
                   {
                   FieldPortrayal2DHolder.this.visible = menuItem.isSelected(); 
                   repaint();
                   }
               });
           }
       }


   // Windows draws faster by handling our own double buffering into our own Java-created
   // buffered image.  MacOS X draws faster by doing a repaint and letting the OS handle
   // the buffering.  Dunno about X Windows, haven't checked, I presume it's same situation
   // as Windows as MacOS X is doing lots of odd hard-coding underneath.

   /** Set to true if we're running on a Mac */
   public static final boolean isMacOSX = isMacOSX();
                                   
   /** Set to true if we're running on Windows */
   public static final boolean isWindows = isWindows();

   /** Set to the version number */
   public static final String javaVersion = getVersion();

   static boolean isMacOSX() 
       {
       try  // we'll try to get certain properties if the security permits it
           {
           return (System.getProperty("mrj.version") != null);  // Apple's official approach
           }
       catch (Throwable e) { return false; }  // Non-Mac Web browsers will fail here
       }

   static boolean isWindows() 
       {
       try  // we'll try to get certain properties if the security permits it
           {
           return !isMacOSX() && (System.getProperty("os.name").startsWith("Win"));
           }
       catch (Throwable e) { return false; }
       }

   static String getVersion()
       {
       try
           {
           return System.getProperty("java.version");
           }
       catch (Throwable e) { return "unknown"; }
       }


   /** Sets various MacOS X features */
   static 
       {
       // use heavyweight tooltips -- otherwise they get obscured by the Canvas3D
       // [this appears to be ignored by MacOS X Java 1.4.1 and 1.4.2.  A bug? ]
       ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);

       // Use Quaqua if it exists
       try
           {
           System.setProperty( "Quaqua.TabbedPane.design","auto" );  // UI Manager Properties docs differ
           System.setProperty( "Quaqua.visualMargin","1,1,1,1" );
           UIManager.put("Panel.opaque", Boolean.TRUE);
           UIManager.setLookAndFeel((String)(Class.forName("ch.randelshofer.quaqua.QuaquaManager").
                   getMethod("getLookAndFeelClassName",(Class[])null).invoke(null,(Object[])null)));
           } 
       catch (Exception e) { /* e.printStackTrace(); */ }

       try  // now we try to set certain properties if the security permits it
           {
           // turn on hardware acceleration on MacOS X.  As of September 2003, 1.3.1
           // turns this off by default, which makes 1.3.1 half the speed (and draws
           // objects wrong to boot).
           System.setProperty("com.apple.hwaccel","true");  // probably settable as an applet.  D'oh! Looks like it's ignored.
           System.setProperty("apple.awt.graphics.UseQuartz","true");  // counter the awful effect in OS X's Sun Renderer
           // the following are likely not settable
           // macOS X 1.4.1 java doesn't show the grow box.  We force it here.
           System.setProperty("apple.awt.showGrowBox","true");
           // we set this so that macos x application packages appear as files
           // and not as directories in the file viewer.  Note that this is the 
           // 1.3.1 version -- Apple gives us an obnoxious warning in 1.4.1 when
           // we call forth an open/save panel saying we should now use
           // apple.awt.use-file-dialog-packages instead, as if 1.3.1 isn't also
           // in common use...
           System.setProperty("com.apple.macos.use-file-dialog-packages","true");
           }
       catch (Exception e) { }
       }
   
   /** Returns icons for a given filename, such as "Layers.png". A utility function. */
   static ImageIcon iconFor(String name)
       {
       return new ImageIcon(IconLoaderHack.class.getResource(name));
       }
   
   public static final ImageIcon LAYERS_ICON = iconFor("Layers.png");
   public static final ImageIcon LAYERS_ICON_P = iconFor("LayersPressed.png");
   public static final ImageIcon REFRESH_ICON = iconFor("Reload.png");
   public static final ImageIcon REFRESH_ICON_P = iconFor("ReloadPressed.png");
   public static final ImageIcon MOVIE_ON_ICON = iconFor("MovieOn.png");
   public static final ImageIcon MOVIE_ON_ICON_P = iconFor("MovieOnPressed.png");
   public static final ImageIcon MOVIE_OFF_ICON = iconFor("MovieOff.png");
   public static final ImageIcon MOVIE_OFF_ICON_P = iconFor("MovieOffPressed.png");
   public static final ImageIcon CAMERA_ICON = iconFor("Camera.png");
   public static final ImageIcon CAMERA_ICON_P = iconFor("CameraPressed.png");
   public static final ImageIcon OPTIONS_ICON = iconFor("Options.png");
   public static final ImageIcon OPTIONS_ICON_P = iconFor("OptionsPressed.png");
   
   public static final Object[] REDRAW_OPTIONS = new Object[] { "Steps/Redraw", "Model Secs/Redraw", "Real Secs/Redraw", "Always Redraw", "Never Redraw" };

   
   /** Use tool tips? */
   boolean useTooltips;

   /** The last steps for a frame that was painted to the screen.  Keeping this
       variable around enables our movie maker to ensure that it doesn't write
       a frame twice to its movie stream. */
   long lastEncodedSteps = -1;  // because we want to encode the start of the simulation prior to any steps.  That's step 0.
   /** Our movie maker, if one is running, else null. */
   MovieMaker movieMaker;

   /** The 2D display inside the scroll view.  Does the actual drawing of the simulation. */
   public InnerDisplay2D insideDisplay;

   /** Our option pane */
   public OptionPane optionPane = new OptionPane("");
   
   /** The list of portrayals the insideDisplay draws.  Each element in this list is a Portrayal2DHolder. */
   ArrayList portrayals = new ArrayList();
   /** The scroll view which holds the insideDisplay. */
   public JScrollPane display;
   /** The scroll view's viewport. */
   JViewport port;
   /** The stoppable for the repeat object which redraws the Display2D in the schedule. */
   Stoppable stopper;
   /** The simulation proper. */
   GUIState simulation;
   /** The component bar at the top of the Display2D. */
   public Box header;
   /** The popup layers menu */
   public JPopupMenu popup;
   /** The button which pops up the layers menu */
   public JToggleButton layersbutton;  // for popup
   /** The refresh menu */
   public JPopupMenu refreshPopup;
   /** The button which pops up the refresh menu */
   public JToggleButton refreshbutton;  // for popup
   /** The button which starts or stops a movie */
   public JButton movieButton;
   /** The button which snaps a screenshot */
   public JButton snapshotButton;
   /** The button which pops up the option pane */
   public JButton optionButton;
   /** The field for scaling values */
   public NumberTextField scaleField;
   /** The field for skipping frames */
   public NumberTextField skipField;
   /** The combo box for skipping frames */
   public JComboBox skipBox;
   /** The frame which holds the skip controls */
   public JPanel skipFrame;
       
   /** Scale (zoom value).  1.0 is 1:1.  2.0 is zoomed in 2 times.  Etc. */
   double scale = 1.0;
   final Object scaleLock = new Object();  // scale lock
   /** Sets the scale (the zoom value) of the Display2D */
   public void setScale(double val) { synchronized (scaleLock)  { if (val > 0.0) scale = val; } }
   /** Returns the scale (the zoom value) of the Display2D */
   public double getScale() { synchronized (scaleLock) { return scale; } }

   /** How many steps are skipped before the display updates itself.  */
 //  long interval = 1;
   //Object intervalLock = new Object();  // interval lock
   /** Sets how many steps are skipped before the display updates itself. */
  // public void setInterval(long i) { synchronized(intervalLock) { if (i > 0) interval = i; } }
   /** Gets how many steps are skipped before the display updates itself. */
  // public long getInterval() { synchronized(intervalLock) { return interval; } }
   
   /** Whether or not we're clipping */
   boolean clipping = true;
   /** Returns true if the Display2D is clipping the drawing area to the user-specified
       height and width */
   public boolean isClipping() { return clipping; }
   /** Sets the Display2D to clip or to not clip to the user-specified height and width when drawing */
   public void setClipping(boolean val) { clipping = val; }
       
   /** Backdrop color or other paint.  This is the color/paint that the simulation is whitewashed with prior to
       the portrayals redrawing themselves.  This differs from the scroll view's BACKGROUND
       color, which is the color of any area that the simulation doesn't draw on. */
   Paint backdrop = Color.white;  // default.  It'll get changed.
   /** Specify the backdrop color or other paint.  The backdrop is the region behind where the simulation 
       actually draws.  If set to null, no color/paint is used -- and indeed the background you're drawing on
       is not defined.  Only set to null if you know you're filling the entire background with something else
       anyway. */
   public void setBackdrop(Paint c) { backdrop = c; }
   /** Returns the backdrop color or paint.  The backdrop is the region behind where the simulation actually draws.
       If set to null, no color/paint is used. */
   public Paint getBackdrop() { return backdrop; }
       
   /** Quits the Display2D.  Okay, so finalize is evil and we're not supposed to rely on it.
       We're not.  But it's an additional cargo-cult programming measure just in case. */
   protected void finalize() throws Throwable
       {
       super.finalize();
       quit();
       }
   
   /** Quits the Display2D.  Called by the Display2D's frame if the Display2D made the frame itself.
       Also called by finalize().  Otherwise you should call this method before destroying the Display2D. */
   public void quit()
       {
       if (stopper!=null) stopper.stop();
       stopper = null;
       stopMovie();
       }
       
   /** Resets the Display2D so it reschedules itself and clears out all selections.  This is useful when reusing the Display2D. */
   public void reset()
   {
   // now reschedule myself
   if (stopper!=null) stopper.stop();
   try { stopper = simulation.scheduleRepeatingImmediatelyAfter(this); }
   catch (IllegalArgumentException e) { } // if the simulation is over, we can't schedule.  Don't worry about it.

   clearSelections();
   }
   
   /** Attaches a portrayal to the Display2D, along with the provided human-readable name for the portrayal.
       The portrayal will be attached with an origin at (0,0) and a width and height equal to the Display2D's
       default width and height. 
       Portrayals are drawn on-screen in the order that they are attached; thus the "top-most" portrayal
       will be the last one attached. */
   public void attach(Portrayal portrayal, String name )
       {
       attach(portrayal, name, true);
       }

   /** Attaches a portrayal to the Display2D, along with the provided human-readable name for the portrayal.
       The portrayal's attached origin, width and height is given in the bounds rectangle. 
       Portrayals are drawn on-screen in the order that they are attached; thus the "top-most" portrayal
       will be the last one attached.*/
   public void attach(Portrayal portrayal, String name, Rectangle2D.Double bounds )
       {
       attach(portrayal, name, bounds, true);
       }
       
   /** Attaches a portrayal to the Display2D, along with the provided
       human-readable name for the portrayal.  The portrayal will be attached
       with an origin at (0,0) and a width and height equal to the Display2D's
       default width and height.  The portrayal may be set to initially 
       visible or not visible.  Portrayals are drawn
       on-screen in the order that they are attached; thus the "top-most" portrayal
       will be the last one attached.*/
   public void attach(Portrayal portrayal, String name, boolean visible )
       {
       attach(portrayal, name, 0, 0, visible);
       }

   /** Attaches a portrayal to the Display2D, along with the provided human-readable name for the portrayal.
       The portrayal's attached origin is given with the coordinates provided.
       The width and height will be equal to the Display2D's default width and height (display2D.insideDisplay.width and
       display2D.insideDisplay.height respectively). To put the origin at the
       center of the display, you can set the x and y coordinates to display2D.insideDisplay.width/2, display2D.insideDisplay.height/2).
       The portrayal may be set to initially visible or not visible.  Portrayals are drawn 
       on-screen in the order that they are attached; thus the "top-most" portrayal
       will be the last one attached. 
   */
   public void attach(Portrayal portrayal, String name, double x, double y, boolean visible)
       {
       attach(portrayal, name, new Rectangle2D.Double(x,y,insideDisplay.width, insideDisplay.height), visible);
       }


   /** Attaches a portrayal to the Display2D, along with the provided 
       human-readable name for the portrayal.  The portrayal's attached 
       origin, width and height is given in the bounds rectangle.  The portrayal
       may be set to initially visible or not visible.  Portrayals are drawn 
       on-screen in the order that they are attached; thus the "top-most" portrayal
       will be the last one attached.  */
   public void attach(Portrayal portrayal, String name, 
       Rectangle2D.Double bounds, boolean visible )
       {
	   	if(portrayal instanceof FieldPortrayal2D){
	       FieldPortrayal2DHolder p = new FieldPortrayal2DHolder((FieldPortrayal2D)portrayal,name,bounds,visible);
	       portrayals.add(p);
	       popup.add(p.menuItem);
	   	}
       }
               
   /** A convenience function: creates a popup menu item of the given name which, when selected, will display the
       given inspector in the Console.  Used rarely, typically for per-field Inspectors. */
   public void attach(final sim.portrayal.Inspector inspector, final String name)
       {
       JMenuItem consoleMenu = new JMenuItem("Show " + name);
       popup.add(consoleMenu);
       consoleMenu.addActionListener(new ActionListener()
           {
           public void actionPerformed(ActionEvent e)
               {
               Bag inspectors = new Bag();
               inspectors.add(inspector);
               Bag names = new Bag();
               names.add(name);
               simulation.controller.setInspectors(inspectors,names);
               }
           });
       }
   
   public void createConsoleMenu()
       {
       if (simulation != null && simulation.controller != null &&
           simulation.controller instanceof NoGUIConsole)
           {
           final NoGUIConsole c = (NoGUIConsole)(simulation.controller);
           JMenuItem consoleMenu = new JMenuItem("Show Console");
           popup.add(consoleMenu);
           consoleMenu.addActionListener(new ActionListener()
               {
               public void actionPerformed(ActionEvent e)
                   {
                   c.setVisible(true);
                   }
               });
           }
       popup.addSeparator();
       }
       
   /** Detatches all portrayals from the Display2D. */
   public ArrayList detatchAll()
       {
       ArrayList old = portrayals;
       popup.removeAll();
       createConsoleMenu();
       portrayals = new ArrayList();
       return old;
       }
   
   
   /** Creates a Display2D with the provided width and height for its portrayal region, 
   attached to the provided simulation.   The interval is ignored.
           
   @deprecated
*/
   public NoGUIDisplay2D(final double width, final double height, GUIState simulation, long interval)
   {
   this(width, height, simulation);
   }
       
   /** Creates a Display2D with the provided width and height for its portrayal region, 
       attached to the provided simulation, and displaying itself with the given interval (which must be > 0). */
  
   public NoGUIDisplay2D(final double width, final double height, GUIState simulation)
       {
       //setInterval(interval);
       this.simulation = simulation;
       
       reset();  // must happen AFTER simulation and interval are assigned
       if(EpisimProperties.getProperty(EpisimProperties.SIMULATION_PNG_PATH) != null && EpisimProperties.getProperty(EpisimProperties.SIMULATION_PNG_PRINT_FREQUENCY)!=null){
 			File pngSnaphotPath = new File(EpisimProperties.getProperty(EpisimProperties.SIMULATION_PNG_PATH));
 			if(pngSnaphotPath.exists() && pngSnaphotPath.isDirectory()){
 				automatedPNGSnapshotsEnabled=true;
 			}
 		}
       final Color transparentBackground = new JPanel().getBackground();  // sacrificial JPanel

       // create the inner display and put it in a Scroll Panel
       insideDisplay = new InnerDisplay2D(width,height);
       display = new JScrollPane(insideDisplay,
           JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
           JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
       display.setPreferredSize(new Dimension((int)width, (int) height));
       display.setMinimumSize(new Dimension(0,0));
       display.setBorder(null);
       display.getHorizontalScrollBar().setBorder(null);
       display.getVerticalScrollBar().setBorder(null);
       port = display.getViewport();
       insideDisplay.setViewRect(new Rectangle(0,0, (int)width, (int)height));
       insideDisplay.setOpaque(true);  // radically increases speed in OS X, maybe others
       // Bug in Panther causes this color to be wrong, ARGH
//       port.setBackground(UIManager.getColor("window"));  // make the nice stripes on MacOS X
       insideDisplay.setBackground(UIManager.getColor("Panel.background"));
       display.setBackground(UIManager.getColor("Panel.background")); // this is the one that has any affect
       port.setBackground(UIManager.getColor("Panel.background"));
       
       // create the button bar at the top.
       header = new Box(BoxLayout.X_AXIS);

       //Create the popup menu.
       layersbutton = new JToggleButton(LAYERS_ICON);
       layersbutton.setPressedIcon(LAYERS_ICON_P);
       layersbutton.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
       layersbutton.setBorderPainted(false);
       layersbutton.setContentAreaFilled(false);
       layersbutton.setToolTipText("Show and hide different layers");
       
       header.add(layersbutton);
       popup = new JPopupMenu();
       popup.setLightWeightPopupEnabled(false);

       //Add listener to components that can bring up popup menus.
       layersbutton.addMouseListener(new MouseAdapter()
           {
           public void mousePressed(MouseEvent e)
               {
               popup.show(e.getComponent(),
                   0, //layersbutton.getLocation().x,
                   layersbutton.getSize().height);
               }
           public void mouseReleased(MouseEvent e)
               {
               layersbutton.setSelected(false);
               }
           });
       //Create the popup menu.
       refreshbutton = new JToggleButton(REFRESH_ICON);
       refreshbutton.setPressedIcon(REFRESH_ICON_P);
       refreshbutton.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
       refreshbutton.setBorderPainted(false);
       refreshbutton.setContentAreaFilled(false);
       refreshbutton.setToolTipText("Change How and When the Display Redraws Itself");
       
       header.add(refreshbutton);
       refreshPopup = new JPopupMenu();
       refreshPopup.setLightWeightPopupEnabled(false);

       //Add listener to components that can bring up popup menus.
       refreshbutton.addMouseListener(new MouseAdapter()
           {
           public void mousePressed(MouseEvent e)
               {
               rebuildRefreshPopup();
               refreshPopup.show(e.getComponent(),
                   0,
                   //refreshbutton.getLocation().x,
                   refreshbutton.getSize().height);
               }
           public void mouseReleased(MouseEvent e)
               {
               refreshbutton.setSelected(false);
               rebuildRefreshPopup();
               }
           });

       // add mouse listener for the inspectors
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
                               NoGUIDisplay2D.this.simulation );
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
               if (handleMouseEvent(e)) { repaint(); return; }
               }

           public void mouseEntered(MouseEvent e)
               {
               if (handleMouseEvent(e)) { repaint(); return; }
               }

           public void mousePressed(MouseEvent e)
               {
               if (handleMouseEvent(e)) { repaint(); return; }
               }

           public void mouseReleased(MouseEvent e)
               {
               if (handleMouseEvent(e)) { repaint(); return; }
               }
           });
               
       insideDisplay.addMouseMotionListener(new MouseMotionAdapter()
           {
           public void mouseDragged(MouseEvent e)
               {
               if (handleMouseEvent(e)) { repaint(); return; }
               }

           public void mouseMoved(MouseEvent e)
               {
               if (handleMouseEvent(e)) { repaint(); return; }
               }
           });
           
       insideDisplay.setToolTipText("Display");  // sacrificial
               
       // add the movie button
       movieButton = new JButton(MOVIE_OFF_ICON);
       movieButton.setPressedIcon(MOVIE_OFF_ICON_P);
       movieButton.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
       movieButton.setBorderPainted(false);
       movieButton.setContentAreaFilled(false);
       movieButton.setToolTipText("Create a Quicktime movie");
       movieButton.addActionListener(new ActionListener()
           {
           public void actionPerformed(ActionEvent e)
               {
               if (movieMaker==null)
                   {
                   startMovie();
                   }
               else 
                   {
                   stopMovie();
                   }
               }
           });
       header.add(movieButton);

       // add the snapshot button
       snapshotButton = new JButton(CAMERA_ICON);
       snapshotButton.setPressedIcon(CAMERA_ICON_P);
       snapshotButton.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
       snapshotButton.setBorderPainted(false);
       snapshotButton.setContentAreaFilled(false);
       snapshotButton.setToolTipText("Create a snapshot (as a PNG or PDF file)");
       snapshotButton.addActionListener(new ActionListener()
           {
           public void actionPerformed(ActionEvent e)
               {
               takeSnapshot();
               }
           });
       header.add(snapshotButton);
       
       // add the option button
       optionButton = new JButton(OPTIONS_ICON);
       optionButton.setPressedIcon(OPTIONS_ICON_P);
       optionButton.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));               
       optionButton.setBorderPainted(false);
       optionButton.setContentAreaFilled(false);
       optionButton.setToolTipText("Show the Option Pane");
       optionButton.addActionListener(new ActionListener()
           {
           public void actionPerformed(ActionEvent e)
               {
              // optionPane.setTitle(getFrame().getTitle() + " Options");
               //optionPane.pack();
               optionPane.setVisible(true);;
               }
           });
       header.add(optionButton);
       
    // add the scale field
       scaleField = new NumberTextField("  Scale: ", 1.0, true)
           {
           public double newValue(double newValue)
               {
               if (newValue <= 0.0) newValue = currentValue;

               // lock the paint lock so we don't try repainting until we request it.
               // JScrollView tries to jump the gun, making things flashy.
               insideDisplay.paintLock = true;
               
               // grab the original location
               Rectangle r = port.getViewRect();

               // scroll to keep the zoomed-in region centered -- this is prettier
               double centerx = r.x + r.width/2.0;
               double centery = r.y + r.height/2.0;
               centerx *= (newValue / (double) currentValue);
               centery *= (newValue / (double) currentValue);
               Point topleft = new Point((int)(centerx - r.width/2.0), (int)(centery - r.height/2.0));
               if (topleft.x < 0) topleft.x = 0;
               if (topleft.y < 0) topleft.y = 0;

               setScale(newValue);
               optionPane.xOffsetField.setValue(insideDisplay.xOffset * newValue);
               optionPane.yOffsetField.setValue(insideDisplay.yOffset * newValue);
               port.setView(insideDisplay);
               
               // now release the paint lock and repaint
               insideDisplay.paintLock = false;
               
               port.setViewPosition(topleft);
               NoGUIDisplay2D.this.repaint();
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
 	         EpisimExceptionHandler.getInstance().displayException(e1);
          }
          double defaultVal = 0;
          if(defaultValue != null){
          	defaultVal = defaultValue instanceof Integer ? (double)((Integer)defaultValue).intValue() : defaultValue instanceof Double ? ((Double)defaultValue).doubleValue() :0;
          }
 	      cellColoringField = new NumberTextField("     Cell Coloring: ", defaultVal, 1,1)
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
                      	 EpisimExceptionHandler.getInstance().displayException(e);
 	            		  }
 	            	  }
 	            	  else{
 	            		  
 	            		  try{
 	                     cellColoringSetterMethod.invoke(globalCBMParameters, new Object[]{newValue});
 	            		  }
 	            		  catch (Exception e){
                      	 EpisimExceptionHandler.getInstance().displayException(e);
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
       
       
       
       
       
       
       
       
       
       
       skipFrame = new JPanel();
       rebuildSkipFrame();

       // put everything together
       setLayout(new BorderLayout());
       add(header,BorderLayout.NORTH);  // so it gets repainted first hopefully
       add(display,BorderLayout.CENTER);

       createConsoleMenu();
       // update preferences
       optionPane.resetToPreferences();
       
       //-----------------------------------------------------------------------------------------------------------------------------------------------------------------
       // Added from Display2DHack
       //-----------------------------------------------------------------------------------------------------------------------------------------------------------------
       
    	moviePathSet = EpisimProperties.getProperty(EpisimProperties.MOVIE_PATH_PROP) != null;
    	
		
		
		if(moviePathSet){ 
			movieButton.setEnabled(false);
			 insideDisplay = new EpisimInnerDisplay2D(width,height);
			 insideDisplay.setViewRect(new Rectangle(0,0, (int)width, (int)height));
			 display = new JScrollPane(insideDisplay,
		            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
		            JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		        display.setMinimumSize(new Dimension(0,0));
		        display.setBorder(null);
		        display.getHorizontalScrollBar().setBorder(null);
		        display.getVerticalScrollBar().setBorder(null);
		        port = display.getViewport();
		        //insideDisplay.setViewRect(port.getViewRect());
		        insideDisplay.setOpaque(true);  // radically increases speed in OS X, maybe others
		        // Bug in Panther causes this color to be wrong, ARGH
//		        port.setBackground(UIManager.getColor("window"));  // make the nice stripes on MacOS X
		        insideDisplay.setBackground(UIManager.getColor("Panel.background"));
		        display.setBackground(UIManager.getColor("Panel.background")); // this is the one that has any affect
		        port.setBackground(UIManager.getColor("Panel.background"));
		        
		       
		        // add mouse listener for the inspectors
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
		                                NoGUIDisplay2D.this.simulation );
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
		                if (handleMouseEvent(e)) { repaint(); return; }
		                }

		            public void mouseEntered(MouseEvent e)
		                {
		                if (handleMouseEvent(e)) { repaint(); return; }
		                }

		            public void mousePressed(MouseEvent e)
		                {
		                if (handleMouseEvent(e)) { repaint(); return; }
		                }

		            public void mouseReleased(MouseEvent e)
		                {
		                if (handleMouseEvent(e)) { repaint(); return; }
		                }
		            });
		                
		        insideDisplay.addMouseMotionListener(new MouseMotionAdapter()
		            {
		            public void mouseDragged(MouseEvent e)
		                {
		                if (handleMouseEvent(e)) { repaint(); return; }
		                }

		            public void mouseMoved(MouseEvent e)
		                {
		                if (handleMouseEvent(e)) { repaint(); return; }
		                }
		            });
		            
		        insideDisplay.setToolTipText("Display");  // sacrificial		      

		         // so it gets repainted first hopefully
		        add(display,BorderLayout.CENTER);

		        
		     // set ourselves up to quit when stopped
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
			if(comp instanceof NumberTextField) header.remove(comp);
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
   public void changeCellColoringMode(double val){
		if(cellColoringField!=null){
			cellColoringField.newValue(val);
			cellColoringField.setValue(val);
		}		
	}

   /** Returns LocationWrappers for all the objects which fall within the coordinate rectangle specified by rect.  This 
       rectangle is in the coordinate system of the (InnerDisplay2D) component inside the scroll
       view of the Display2D class.  The return value is an array of Bags.  For each FieldPortrayal
       attached to the Display2D, one Bag is returned holding all the LocationWrappers for objects falling within the
       rectangle which are associated with that FieldPortrayal's portrayed field.  The order of
       the Bags in the array is the same as the order of the FieldPortrayals in the Display2D's
       <code>portrayals</code> list.
   */
   public Bag[] objectsHitBy( final Rectangle2D.Double rect )
       {
       Bag[] hitObjs = new Bag[portrayals.size()];
       Iterator iter = portrayals.iterator();
       int x=0;
               
       while (iter.hasNext())
           {
           hitObjs[x] = new Bag();
           FieldPortrayal2DHolder p = (FieldPortrayal2DHolder)(iter.next());
           if (p.visible)
               {
               p.portrayal.hitObjects(getDrawInfo2D(p, rect), hitObjs[x]);
               }
           x++;
           }
       return hitObjs;
       }
	   /** Returns LocationWrappers for all the objects which overlap with the point specified by 'point'.  This 
	   point is in the coordinate system of the (InnerDisplay2D) component inside the scroll
	   view of the Display2D class.  The return value is an array of Bags.  For each FieldPortrayal
	   attached to the Display2D, one Bag is returned holding all the LocationWrappers for objects overlapping with the point
	   which are associated with that FieldPortrayal's portrayed field.  The order of
	   the Bags in the array is the same as the order of the FieldPortrayals in the Display2D's
	   <code>portrayals</code> list.
	*/
   public Bag[] objectsHitBy( final Point2D point )
   {
   return objectsHitBy(new Rectangle2D.Double(point.getX(), point.getY(), 1, 1));
   }
       
   /** Constructs a DrawInfo2D for the given portrayal, or null if failed.  O(num portrayals). */
   public DrawInfo2D getDrawInfo2D(FieldPortrayal2D portrayal, Point2D point)
       {
       return getDrawInfo2D(portrayal, new java.awt.geom.Rectangle2D.Double( point.getX(), point.getY(), 1, 1 )); 
       }

   /** Constructs a DrawInfo2D for the given portrayal, or null if failed.  O(num portrayals). */
   public DrawInfo2D getDrawInfo2D(FieldPortrayal2D portrayal, Rectangle2D clip)
       {
       Iterator iter = portrayals.iterator();
       while(iter.hasNext())
           {
           FieldPortrayal2DHolder p = (FieldPortrayal2DHolder)(iter.next());
           if (p.portrayal == portrayal) { return getDrawInfo2D(p, clip); }
           }
       return null;
       }
       
       
   DrawInfo2D getDrawInfo2D(FieldPortrayal2DHolder holder, Rectangle2D clip)
   {
	   if (holder==null) return null;
	   
	   double scale = getScale();
	   // compute WHERE we need to draw
	   int origindx = 0;
	   int origindy = 0;
	
	   // offset according to user's specification
	   origindx += (int)(insideDisplay.xOffset*scale);
	   origindy += (int)(insideDisplay.yOffset*scale);
	
	   // for information on why we use getViewRect, see computeClip()
	   Rectangle2D fullComponent = insideDisplay.getViewRect();
	   if (fullComponent.getWidth() > (insideDisplay.width * scale))
	       origindx = (int)((fullComponent.getWidth() - insideDisplay.width*scale)/2);
	   if (fullComponent.getHeight() > (insideDisplay.height*scale))
	       origindy = (int)((fullComponent.getHeight() - insideDisplay.height*scale)/2);
	                           
	   Rectangle2D.Double region = new Rectangle2D.Double(
	       // we floor to an integer because we're dealing with exact pixels at this point
	       (int)(holder.bounds.x * scale) + origindx,
	       (int)(holder.bounds.y * scale) + origindy,
	       (int)(holder.bounds.width * scale),
	       (int)(holder.bounds.height * scale));
	   DrawInfo2D d2d = new DrawInfo2D(simulation, holder.portrayal, region, clip);
	   d2d.gui = simulation;
	   d2d.precise = precise;
	   return d2d;
   }
    
   /** */
   ArrayList selectedWrappers = new ArrayList();
   
   /** Selects the following object, deselecting other objects if so asked. */
   public void performSelection( LocationWrapper wrapper)
       {
       Bag b = new Bag();
       b.add(wrapper);
       performSelection(b);
       }
   
   public void clearSelections()
       {
       for(int x=0;x<selectedWrappers.size();x++)
           {
           LocationWrapper wrapper = ((LocationWrapper)(selectedWrappers.get(x)));
           wrapper.getFieldPortrayal().setSelected(wrapper, false);
           }
       selectedWrappers.clear();
       }
       
   public void performSelection( Bag locationWrappers )
       {
       clearSelections();
               
       if (locationWrappers == null) return;  // deselect everything
       
       // add new wrappers
       for(int x=0;x < locationWrappers.size(); x++)
           {
           LocationWrapper wrapper = ((LocationWrapper)(locationWrappers.get(x)));
           wrapper.getFieldPortrayal().setSelected(wrapper, true);
           selectedWrappers.add(wrapper);
           }
       }
       
   public void performSelection( final Rectangle2D.Double rect )
       {
       // gather objects hit and select them, and put in selectedObjects
       Bag[] hitObjects = objectsHitBy(rect);
       Bag collection = new Bag();
       for(int x=0;x<hitObjects.length;x++)
           collection.addAll(hitObjects[x]);
       performSelection(collection);
       }

   /** Determines the inspectors appropriate for the given selection region (rect), and sends
   them on to the Controller. */
   public void createInspectors( final Rectangle2D.Double rect, final GUIState simulation )
   {
	   Bag inspectors = new Bag();
	   Bag names = new Bag();
	   
	   Bag[] hitObjects = objectsHitBy(rect);
	   for(int x=0;x<hitObjects.length;x++)
	       {
	       FieldPortrayal2DHolder p = (FieldPortrayal2DHolder)(portrayals.get(x));
	       for( int i = 0 ; i < hitObjects[x].numObjs ; i++ )
	           {
	           LocationWrapper wrapper = (LocationWrapper) (hitObjects[x].objs[i]);
	           inspectors.add(p.portrayal.getInspector(wrapper,simulation));
	           names.add(p.portrayal.getName(wrapper));
	           }
	       }
	   simulation.controller.setInspectors(inspectors,names);
   }

   final static int SCROLL_BAR_SCROLL_RATIO = 10;

   /** Creates a frame holding the Display2D.  This is the best method to create the frame,
       rather than making a frame and putting the Display2D in it.  If you prefer the latter,
       then you need to handle two things.  First, when the frame is disposed, you need to
       call quit() on the Display2D.  Second, if you care about distribution to MacOS X
       Java 1.3.1, you need to call Utilities.doEnsuredRepaint(header) whenever the window is resized. */
   public JFrame createFrame()
       {
       JFrame frame = new JFrame()
           {
           public void dispose()
               {
               quit();       // shut down the movies
               super.dispose();
               }
           };
           
       frame.setResizable(true);
       
       // these bugs are tickled by our constant redraw requests.
       frame.addComponentListener(new ComponentAdapter()
           {
           // Bug in MacOS X Java 1.3.1 requires that we force a repaint.
           public void componentResized (ComponentEvent e) 
               {
               Utilities.doEnsuredRepaint(header);
               display.getHorizontalScrollBar().setUnitIncrement(display.getViewport().getWidth() / SCROLL_BAR_SCROLL_RATIO);
               display.getVerticalScrollBar().setUnitIncrement(display.getViewport().getHeight() / SCROLL_BAR_SCROLL_RATIO);
               }
           });
                               
       frame.getContentPane().setLayout(new BorderLayout());
       frame.getContentPane().add(this,BorderLayout.CENTER);
       
       frame.setTitle(GUIState.getName(simulation.getClass()) + " Display");
       frame.pack();
       return frame;
       }
   
   /** Returns the frame holding this Component.  If there is NO such frame, an error will
       be generated (probably a ClassCastException). */
   public Frame getFrame()
       {
       Component c = this;
       while(c.getParent() != null)
           c = c.getParent();
       return c instanceof Frame ? (Frame)c : null;
       }

   /** Takes a snapshot of the Display2D's currently displayed simulation.
       Ought only be done from the main event loop. */
       
   // Why are we using PNG?  For a couple of reasons.  First, GIF only supports 256 colors.  That had begun
   // to bite us.  Second, JPEG is good for photos but quite poor for screenshots which have lots of
   // straight lines and aliased stuff.  PNG is the RIGHT choice for what we need to do.  Unfortunately
   // it's not properly supported by old versions Internet Exploder, and so to make web-ready snapshots 
   // you may need to convert it.  :-(

   // We're using our own small PNG package (see sim.util.media) because the JAI isn't standard across
   // all platforms (notably 1.3.1) and at the time we made the call, it wasn't available on the Mac at all.

   private Object sacrificialObj = null;
   
   public final static int TYPE_PDF = 1;
   public final static int TYPE_PNG = 2;
   
   public void takeSnapshot(File file, int type) throws IOException
   {
   if (type == TYPE_PNG)
       {
       Graphics g = insideDisplay.getGraphics();
       BufferedImage img = insideDisplay.paint(g,true,false);
       g.dispose();
       OutputStream stream = new BufferedOutputStream(new FileOutputStream(file));
       PNGEncoder tmpEncoder = new PNGEncoder(img, false,PNGEncoder.FILTER_NONE,9);
       stream.write(tmpEncoder.pngEncode());
       stream.close();
       }
   else // type == TYPE_PDF
       {
       boolean oldprecise = precise;
       precise = true;
       PDFEncoder.generatePDF(port, file);
       precise = oldprecise;
       }
   }
   
   public void takeSnapshot()
   {
   	 SimStateServer.getInstance().getEpisimGUIState().pressWorkaroundSimulationPause(); 

       // snap the shot FIRST
       Graphics g = insideDisplay.getGraphics();
       BufferedImage img = insideDisplay.paint(g,true,false);  // notice we're painting to a non-shared buffer
     
                           
     if(g!=null)  g.dispose();  // because we got it with getGraphics(), we're responsible for it
                   
       // Ask what kind of thing we want to save?
       final int CANCEL_BUTTON = 0;
       final int PNG_BUTTON = 1;
       final int SVG_BUTTON = 2;
       int result = PNG_BUTTON;  //  default
       Object[] options = { "Cancel", "Save to PNG ", "Save to SVG"};
       if(!automatedPNGSnapshotsEnabled){
           result = JOptionPane.showOptionDialog(getFrame(), "Save window snapshot to what kind of file format?", "Save Format", 
               JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
               null, options, options[0]);
       }
      
                   
       if (result == PNG_BUTTON || automatedPNGSnapshotsEnabled) 
           {
           // NOW pop up the save window
           FileDialog fd = new FileDialog(getFrame(), 
               "Save Snapshot as 24-bit PNG...", FileDialog.SAVE);
           fd.setFile("Untitled.png");
          if(!automatedPNGSnapshotsEnabled) fd.setVisible(true);
           if (fd.getFile()!=null || automatedPNGSnapshotsEnabled) 
           	try
              {
                                       
           		 File pngFile= automatedPNGSnapshotsEnabled	? EpisimProperties.getFileForPathOfAProperty(EpisimProperties.SIMULATION_PNG_PATH, "EPISIM_Visualization_Snapshot", ".png")
									:  new File(fd.getDirectory(), Utilities.ensureFileEndsWith(fd.getFile(),".png"));
           		
           		
           		OutputStream stream = new BufferedOutputStream(new FileOutputStream(pngFile));
                                       PNGEncoder tmpEncoder = new
                                           PNGEncoder(img, false,PNGEncoder.FILTER_NONE,9);
                                       stream.write(tmpEncoder.pngEncode());
                                       stream.close();
                                       }
               catch (Exception e) { e.printStackTrace(); }
           }
       if (result == SVG_BUTTON ||(automatedPNGSnapshotsEnabled 
     		  && EpisimProperties.getProperty(EpisimProperties.IMAGE_SAVESVGCOPYOFPNG) != null 
     		  && EpisimProperties.getProperty(EpisimProperties.IMAGE_SAVESVGCOPYOFPNG).equals(EpisimProperties.ON)))
           {
     	  		ExtendedFileChooser fd = new ExtendedFileChooser("svg");
          
           
           if (automatedPNGSnapshotsEnabled || fd.showSaveDialog(getFrame())==ExtendedFileChooser.APPROVE_OPTION){ 
           	try
              {
                     boolean oldprecise = precise;
                     precise = true;
                     File svgFile= automatedPNGSnapshotsEnabled	? EpisimProperties.getFileForPathOfAProperty(EpisimProperties.SIMULATION_PNG_PATH, "EPISIM_Visualization_Snapshot", ".svg")
                    		 													: fd.getSelectedFile();
                     
                     saveSVGImage(port, svgFile);
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

   /** Starts a Quicktime movie on the given Display2D.  The size of the movie frame will be the size of
       the display at the time this method is called.  This method ought to be called from the main event loop.
       Most of the default movie formats provided will result in a gigantic movie, which you can
       re-encode using something smarter (like the Animation or Sorenson codecs) to put to a reasonable size.
       On the Mac, Quicktime Pro will do this quite elegantly. */
 /*  public void startMovie()
       {
       // we synchronize because movieMaker.add() could
       // get called, via paintToMovie(), from inside the model thread rather
       // than the Swing thread (see step(...) below).  This allows us to guarantee,
       // everywhere where movieMaker is set (to null or to new), that paintToMovie
       // isn't doing anything.
       synchronized(NoGUIDisplay2D.this.simulation.state.schedule)
           {
           // can't start a movie if we're in an applet
           if (SimApplet.isApplet)
               {
               Object[] options = {"Oops"};
               JOptionPane.showOptionDialog(
                   this, "You cannot create movies from an applet.",
                   "MASON Applet Restriction",
                   JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE,
                   null, options, options[0]);
               return;
               }
                       
           if (movieMaker != null) return;  // already running
           movieMaker = new MovieMaker(getFrame());
           Graphics g = insideDisplay.getGraphics();
           final BufferedImage typicalImage = insideDisplay.paint(g,true,false);
           g.dispose();
                   
           if (!movieMaker.start(typicalImage))
               movieMaker = null;  // failed
           else 
               {
               movieButton.setIcon(MOVIE_ON_ICON);
               movieButton.setPressedIcon(MOVIE_ON_ICON_P);

               // start up simulation paused if necessary
               final NoGUIConsole console = (NoGUIConsole)(simulation.controller);
               if (console.getPlayState() == NoGUIConsole.PS_STOPPED)  // either after simulation or we just started the program
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
       }*/
       

   /** Stops a Quicktime movie and cleans up, flushing the remaining frames out to disk. 
       This method ought to be called from the main event loop. */
  /* public void stopMovie()
       {
       // we synchronize because movieMaker.add() could
       // get called, via paintToMovie(), from inside the model thread rather
       // than the Swing thread (see step(...) below).  This allows us to guarantee,
       // everywhere where movieMaker is set (to null or to new), that paintToMovie
       // isn't doing anything.
       synchronized(NoGUIDisplay2D.this.simulation.state.schedule)
           {
           if (movieMaker == null) return;  // already stopped
           if (!movieMaker.stop())
               {
               Object[] options = {"Drat"};
               JOptionPane.showOptionDialog(
                   this, "Your movie did not write to disk\ndue to a spurious JMF movie generation bug.",
                   "JMF Movie Generation Bug",
                   JOptionPane.OK_OPTION, JOptionPane.WARNING_MESSAGE,
                   null, options, options[0]);
               }
           movieMaker = null;
           if (movieButton!=null)  // hasn't been destroyed yet
               {
               movieButton.setIcon(MOVIE_OFF_ICON);
               movieButton.setPressedIcon(MOVIE_OFF_ICON_P);
               }
           }
       }*/

   /** Steps the Display2D in the GUIState schedule.  If we're in MacOS X, this results in a repaint()
       request generated.  If we're in Windows or X Windows, this results in a direct call to
       paintComponent on the insideDisplay.  It's OS-dependent because different operating systems
       draw faster in different ways. */
  /* public void step(final SimState state)
   {
  	 long steps = simulation.state.schedule.getSteps();
      
      if (steps % getInterval() == 0   // time to update!
          && (insideDisplay.isShowing()    // only draw if we can be seen
              || movieMaker !=null ))      // OR draw to a movie even if we can't be seen
          {
          if (isMacOSX && movieMaker == null) 
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
      }*/
   /** Used internally and by Display3D to indicate */
   public final static int UPDATE_RULE_STEPS = 0;
   public final static int UPDATE_RULE_INTERNAL_TIME = 1;
   public final static int UPDATE_RULE_WALLCLOCK_TIME = 2;
   public final static int UPDATE_RULE_ALWAYS = 3;
   public final static int UPDATE_RULE_NEVER = 4;
   int updateRule = UPDATE_RULE_ALWAYS;
   long stepInterval = 1;
   double timeInterval = 0;
   long wallInterval = 0;
   long lastStep = -1;
   double lastTime = Schedule.BEFORE_SIMULATION;
   long lastWall = -1;  // the current time is around 1266514720569 so this should be fine (knock on wood)
       
   /** Returns whether it's time to update. */
   public boolean shouldUpdate()
       {
       boolean val = false;
               
       if (updateRule == UPDATE_RULE_ALWAYS)
           val = true;
       else if (updateRule == UPDATE_RULE_STEPS)
           {
           long step = simulation.state.schedule.getSteps();
           val = (lastStep < 0 || stepInterval == 0 || step - lastStep >= stepInterval || // clearly need to update
               lastStep % stepInterval >= step % stepInterval);  // on opposite sides of a tick
           if (val) lastStep = step;
           }
       else if (updateRule == UPDATE_RULE_WALLCLOCK_TIME)
           {
           long wall = System.currentTimeMillis();
           val = (lastWall == 0 || wallInterval == 0 || wall - lastWall >= wallInterval || // clearly need to update
               lastWall % wallInterval >= wall % wallInterval);  // on opposite sides of a tick
           if (val) lastWall = wall;
           }
       else if (updateRule == UPDATE_RULE_INTERNAL_TIME)
           {
           double time = simulation.state.schedule.getTime();
           val = (lastTime == 0 || timeInterval == 0 || time - lastTime >= timeInterval || // clearly need to update
               lastTime % timeInterval >= time % timeInterval);  // on opposite sides of a tick
           if (val) lastTime = time;
           }
       // else val = false;
               
       return val;
       }
   public boolean handleMouseEvent(MouseEvent event)
   {
   // first, let's propagate the event to selected objects
           
   Point2D.Double p = new Point2D.Double(event.getX(), event.getY());
   for(int x=0;x<selectedWrappers.size();x++)
       {
       LocationWrapper wrapper = ((LocationWrapper)(selectedWrappers.get(x)));
       FieldPortrayal2D f = (FieldPortrayal2D)(wrapper.getFieldPortrayal());
       Object obj = wrapper.getObject();
       SimplePortrayal2D portrayal = (SimplePortrayal2D)(f.getPortrayalForObject(obj));
       if (portrayal.handleMouseEvent(simulation, this, wrapper, event, getDrawInfo2D(f, p), SimplePortrayal2D.TYPE_SELECTED_OBJECT))
           {
           simulation.controller.refresh();
           return true;
           }
       }
                   
   // next, let' propagate the event to any objects which have been hit.
   // We go backwards through the bag list so top elements are selected first
           
   Bag[] hitObjects = objectsHitBy(p);
   for(int x=hitObjects.length - 1; x >= 0; x--)
       for(int i = 0; i < hitObjects[x].numObjs; i++)
           {
           LocationWrapper wrapper = (LocationWrapper)(hitObjects[x].objs[i]);
           FieldPortrayal2D f = (FieldPortrayal2D)(wrapper.getFieldPortrayal());
           Object obj = wrapper.getObject();
           SimplePortrayal2D portrayal = (SimplePortrayal2D)(f.getPortrayalForObject(obj));
           if (portrayal.handleMouseEvent(simulation, this, wrapper, event, getDrawInfo2D(f, p), SimplePortrayal2D.TYPE_HIT_OBJECT))
               {
               simulation.controller.refresh();
               return true;
               }
           }
                   
   // at this point, nobody consumed the event so we ignore it

   return false;
   }

   void rebuildSkipFrame()
   {
   skipFrame.removeAll();
   skipFrame.invalidate();
   skipFrame.repaint();
   skipFrame.setLayout(new BorderLayout());

   JPanel skipHeader = new JPanel();
   skipHeader.setLayout(new BorderLayout());
   skipFrame.add(skipHeader, BorderLayout.CENTER);
           
   // add the interval (skip) field
   skipBox = new JComboBox(REDRAW_OPTIONS);
   skipBox.setSelectedIndex(updateRule);
   ActionListener skipListener = new ActionListener()
       {
       public void actionPerformed(ActionEvent e)
           {
           updateRule = skipBox.getSelectedIndex();
           if (updateRule == UPDATE_RULE_ALWAYS || updateRule == UPDATE_RULE_NEVER)
               {
               skipField.getField().setText("");
               skipField.setEnabled(false);
               }
           else if (updateRule == UPDATE_RULE_STEPS)
               {
               skipField.setValue(stepInterval);
               skipField.setEnabled(true);
               }
           else if (updateRule == UPDATE_RULE_INTERNAL_TIME)
               {
               skipField.setValue(timeInterval);
               skipField.setEnabled(true);
               }
           else // UPDATE_RULE_WALLCLOCK_TIME
               {
               skipField.setValue((long)(wallInterval / 1000));
               skipField.setEnabled(true);
               }
           }
       };
   skipBox.addActionListener(skipListener);
           
   // I want right justified text.  This is an ugly way to do it
   skipBox.setRenderer(new DefaultListCellRenderer()
       {
       public Component getListCellRendererComponent(JList list, Object value, int index,  boolean isSelected,  boolean cellHasFocus)
           {
           // JLabel is the default
           JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
           label.setHorizontalAlignment(SwingConstants.RIGHT);
           return label;
           }
       });
                   
   skipHeader.add(skipBox, BorderLayout.WEST);


   skipField = new NumberTextField(null, 1, false)
       {
       public double newValue(double newValue)
           {
           double val;
           if (updateRule == UPDATE_RULE_ALWAYS || updateRule == UPDATE_RULE_NEVER)  // shouldn't have happened
               {
               val = 0;
               }
           else if (updateRule == UPDATE_RULE_STEPS)
               {
               val = (long) newValue;
               if (val < 1) val = stepInterval;
               stepInterval = (long) val;
               }
           else if (updateRule == UPDATE_RULE_WALLCLOCK_TIME)
               {
               val = newValue;
               if (val < 0) val = wallInterval / 1000;
               wallInterval = (long) (newValue * 1000);
               }
           else // if (updateRule == UPDATE_RULE_INTERNAL_TIME)
               {
               val = newValue;
               if (newValue < 0) newValue = timeInterval;
               timeInterval = val;
               }
                   
           // reset with a new interval
           reset();
                   
           return val;
           }
       };
   skipField.setToolTipText("Specify the interval between screen updates");
   skipField.getField().setColumns(10);
   skipHeader.add(skipField,BorderLayout.CENTER);
   skipHeader.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
   skipListener.actionPerformed(null);  // have it update the text field accordingly
   }

void rebuildRefreshPopup()
   {
   refreshPopup.removeAll();
   String s = "";
   switch(updateRule)
       {
       case UPDATE_RULE_STEPS:
           s = (stepInterval == 1 ? "Currently redrawing each model iteration" :
               "Currently redrawing every " + stepInterval +  " model iterations");
           break;
       case UPDATE_RULE_INTERNAL_TIME:
           s = (timeInterval == 1 ? "Currently redrawing each unit of model time" :
               "Currently redrawing every " + (timeInterval) +  " units of model time");
           break;
       case UPDATE_RULE_WALLCLOCK_TIME:
           s = (wallInterval == 1000 ? "Currently redrawing each second of real time" :
               "Currently redrawing every " + (wallInterval / 1000.0) +  " seconds of real time");
           break;
       case UPDATE_RULE_ALWAYS:
           s = "Currently redrawing each model iteration";
           break;
       case UPDATE_RULE_NEVER:
           s = "Currently never redrawing except when the window is redrawn";
           break;
       }
   JMenuItem m = new JMenuItem(s);
   m.setEnabled(false);
   refreshPopup.add(m);
           
   refreshPopup.addSeparator();

   m = new JMenuItem("Always Redraw");
   refreshPopup.add(m);
   m.addActionListener(new ActionListener()
       {
       public void actionPerformed(ActionEvent e)
           {
           updateRule = UPDATE_RULE_ALWAYS;
           rebuildSkipFrame();
           }
       });

   m = new JMenuItem("Never Redraw");
   refreshPopup.add(m);
   m.addActionListener(new ActionListener()
       {
       public void actionPerformed(ActionEvent e)
           {
           updateRule = UPDATE_RULE_NEVER;
           rebuildSkipFrame();
           }
       });

   m = new JMenuItem("Redraw once every 2 iterations");
   refreshPopup.add(m);
   m.addActionListener(new ActionListener()
       {
       public void actionPerformed(ActionEvent e)
           {
           updateRule = UPDATE_RULE_STEPS;
           stepInterval = 2;
           rebuildSkipFrame();
           }
       });

   m = new JMenuItem("Redraw once every 4 iterations");
   refreshPopup.add(m);
   m.addActionListener(new ActionListener()
       {
       public void actionPerformed(ActionEvent e)
           {
           updateRule = UPDATE_RULE_STEPS;
           stepInterval = 4;
           rebuildSkipFrame();
           }
       });

   m = new JMenuItem("Redraw once every 8 iterations");
   refreshPopup.add(m);
   m.addActionListener(new ActionListener()
       {
       public void actionPerformed(ActionEvent e)
           {
           updateRule = UPDATE_RULE_STEPS;
           stepInterval = 8;
           rebuildSkipFrame();
           }
       });

   m = new JMenuItem("Redraw once every 16 iterations");
   refreshPopup.add(m);
   m.addActionListener(new ActionListener()
       {
       public void actionPerformed(ActionEvent e)
           {
           updateRule = UPDATE_RULE_STEPS;
           stepInterval = 16;
           rebuildSkipFrame();
           }
       });
                   
   m = new JMenuItem("Redraw once every 32 iterations");
   refreshPopup.add(m);
   m.addActionListener(new ActionListener()
       {
       public void actionPerformed(ActionEvent e)
           {
           updateRule = UPDATE_RULE_STEPS;
           stepInterval = 16;
           rebuildSkipFrame();
           }
       });
                   
   refreshPopup.addSeparator();

   // add other menu items
   m = new JMenuItem("More Options...");
   refreshPopup.add(m);
   m.addActionListener(new ActionListener()
       {
       public void actionPerformed(ActionEvent e)
           {
           
           skipFrame.setVisible(true);
           }
       });

   refreshPopup.revalidate();
   }

   
   
   
   //-----------------------------------------------------------------------------------------------------------------------------------------------------------------
   // Added from Display2DHack
   //-----------------------------------------------------------------------------------------------------------------------------------------------------------------
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
		if(moviePathSet){
			synchronized(NoGUIDisplay2D.this.simulation.state.schedule)
	       {
	       
	       if (episimMovieMaker != null) return;  // already running
	       episimMovieMaker = new EpisimMovieMaker(getFrame());
	       Graphics g = insideDisplay.getGraphics();
	       final BufferedImage typicalImage = insideDisplay.paint(g,true,false);
	       if(g != null)g.dispose();
	               
	       if (!episimMovieMaker.start(typicalImage))
	      	 episimMovieMaker = null;  // failed
	       else 
	           {
	         	
	           // start up simulation paused if necessary
	           final NoGUIConsole console = (NoGUIConsole)(simulation.controller);
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
		else{
	       // we synchronize because movieMaker.add() could
	       // get called, via paintToMovie(), from inside the model thread rather
	       // than the Swing thread (see step(...) below).  This allows us to guarantee,
	       // everywhere where movieMaker is set (to null or to new), that paintToMovie
	       // isn't doing anything.
	       synchronized(NoGUIDisplay2D.this.simulation.state.schedule)
	           {
	           // can't start a movie if we're in an applet
	           if (SimApplet.isApplet)
	               {
	               Object[] options = {"Oops"};
	               JOptionPane.showOptionDialog(
	                   this, "You cannot create movies from an applet.",
	                   "MASON Applet Restriction",
	                   JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE,
	                   null, options, options[0]);
	               return;
	               }
	                       
	           if (movieMaker != null) return;  // already running
	           movieMaker = new MovieMaker(getFrame());
	           Graphics g = insideDisplay.getGraphics();
	           final BufferedImage typicalImage = insideDisplay.paint(g,true,false);
	           g.dispose();
	                   
	           if (!movieMaker.start(typicalImage))
	               movieMaker = null;  // failed
	           else 
	               {
	               movieButton.setIcon(MOVIE_ON_ICON);
	               movieButton.setPressedIcon(MOVIE_ON_ICON_P);

	               // start up simulation paused if necessary
	               final NoGUIConsole console = (NoGUIConsole)(simulation.controller);
	               if (console.getPlayState() == NoGUIConsole.PS_STOPPED)  // either after simulation or we just started the program
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
   }
	
	public void stopMovie()
   {
		if(moviePathSet){
		   synchronized(NoGUIDisplay2D.this.simulation.state.schedule)
		   {
		       if (episimMovieMaker == null) return;  // already stopped
		       if (!episimMovieMaker.stop())
		       {
		           
		           EpisimExceptionHandler.getInstance().displayException(new Exception("Your movie did not write to disk\ndue to a spurious JMF movie generation bug."));
		             
		       }
		       episimMovieMaker = null;		       
		   }
		}
		else{
		// we synchronize because movieMaker.add() could
	       // get called, via paintToMovie(), from inside the model thread rather
	       // than the Swing thread (see step(...) below).  This allows us to guarantee,
	       // everywhere where movieMaker is set (to null or to new), that paintToMovie
	       // isn't doing anything.
	       synchronized(NoGUIDisplay2D.this.simulation.state.schedule)
	           {
	           if (movieMaker == null) return;  // already stopped
	           if (!movieMaker.stop())
	               {
	               Object[] options = {"Drat"};
	               JOptionPane.showOptionDialog(
	                   this, "Your movie did not write to disk\ndue to a spurious JMF movie generation bug.",
	                   "JMF Movie Generation Bug",
	                   JOptionPane.OK_OPTION, JOptionPane.WARNING_MESSAGE,
	                   null, options, options[0]);
	               }
	           movieMaker = null;
	           if (movieButton!=null)  // hasn't been destroyed yet
	               {
	               movieButton.setIcon(MOVIE_OFF_ICON);
	               movieButton.setPressedIcon(MOVIE_OFF_ICON_P);
	               }
	           }
		}
   }
	
	public void step(final SimState state)
   {
		if(moviePathSet){
			long steps = simulation.state.schedule.getSteps();
	      
	      if ((insideDisplay.isShowing()    // only draw if we can be seen
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
	              if(g!= null)g.dispose();
	              }
	          }
	      insideDisplay.updateToolTips();
		}
		else{
			long steps = simulation.state.schedule.getSteps();
	      
	      if ((insideDisplay.isShowing()    // only draw if we can be seen
	              || movieMaker !=null ))      // OR draw to a movie even if we can't be seen
	          {
	          if (isMacOSX && movieMaker == null) 
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
   }
	
	
	
	public class EpisimInnerDisplay2D extends InnerDisplay2D{
		public EpisimInnerDisplay2D(double width, double height){
			super(width, height);
			viewRect = new Rectangle(0,0,(int)width, (int) height);
		}
		public void paintToMovie(Graphics g)
      {
      synchronized(NoGUIDisplay2D.this.simulation.state.schedule)
          {
          // only paint if it's appropriate
          long steps = NoGUIDisplay2D.this.simulation.state.schedule.getSteps();
          if (steps > lastEncodedSteps &&
                    NoGUIDisplay2D.this.simulation.state.schedule.getTime() < Schedule.AFTER_SIMULATION)
              {
         	 NoGUIDisplay2D.this.episimMovieMaker.add(paint(g,true,false));
              lastEncodedSteps = steps;
              }
          else paint(g,false,false);
          }
      }
		 public void paintComponent(Graphics g, boolean buffer)
       {
       synchronized(NoGUIDisplay2D.this.simulation.state.schedule)  // for time()
           {
           if (episimMovieMaker!=null)  // we're writing a movie
               insideDisplay.paintToMovie(g);
           else paint(g,buffer,true);
           }
       }
	}
	
	
	//--------------------------------------------------------------------------------------------------------------------------------------------------------------
	// ADDED METHODS
	
	
	public void changePortrayal(String name, FieldPortrayal2D portrayal){
		FieldPortrayal2DHolder holder = getPortrayalHolder(name);
		if(holder != null){
			holder.portrayal = portrayal;
		}
	}
	
	public boolean isAutomatedPNGSnapshotsEnabled() {
	   
   	return automatedPNGSnapshotsEnabled;
   } 
}

	

