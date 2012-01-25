package sim.display3d;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.prefs.Preferences;

import javax.media.j3d.Alpha;
import javax.media.j3d.AmbientLight;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.ModelClip;
import javax.media.j3d.PointLight;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.RotationInterpolator;
import javax.media.j3d.Switch;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.View;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameListener;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector4d;

import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import com.sun.j3d.utils.universe.SimpleUniverse;

import episiminterfaces.EpisimSimulationDisplay;
import sim.app.episim.EpisimProperties;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.ModeServer;
import sim.app.episim.gui.EpisimGUIState;
import sim.app.episim.gui.ImageLoader;
import sim.app.episim.model.controller.ExtraCellularDiffusionController;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.controller.ExtraCellularDiffusionController.DiffusionFieldCrossSectionMode;
import sim.app.episim.tissue.TissueController;
import sim.app.episim.util.EpisimMovieMaker;
import sim.display.Display2D;
import sim.display.Display2DHack;
import sim.display.GUIState;
import sim.display.Prefs;
import sim.display3d.Display3D.LocalWindowListener;
import sim.display3d.Display3D.Portrayal3DHolder;
import sim.engine.SimState;
import sim.engine.Steppable;

import sim.portrayal.FieldPortrayal;
import sim.portrayal.FieldPortrayal2D;
import sim.portrayal.Portrayal;
import sim.portrayal3d.FieldPortrayal3D;
import sim.portrayal3d.Portrayal3D;
import sim.util.gui.LabelledList;
import sim.util.gui.NumberTextField;
import sim.util.gui.Utilities;



public class Display3DHack extends Display3D implements EpisimSimulationDisplay{
	
	//---------------------------------------------------------------------------------------------------------------------------------------------
	// TODO: IMPORTANT delete class OptionPane in class Display3D otherwise headless mode in computer cluster environment does not work !!!
	//----------------------------------------------------------------------------------------------------------------------------------------------
	
	public enum ModelSceneCrossSectionMode{
		DISABLED("Disabled"),
		X_Y_PLANE("X-Y-Plane"),
		X_Z_PLANE("X-Z-Plane"),
		Y_Z_PLANE("Y-Z-Plane");
		
		private String name;
		ModelSceneCrossSectionMode(String name){
			this.name = name;
		
		}		
		public String toString(){ return this.name; }
	}
	
	private double modelSceneCrossSectionCoordinateInMikron = -1*TissueController.getInstance().getTissueBorder().getLengthInMikron();
	
	private ModelSceneCrossSectionMode modelSceneCrossSectionMode = ModelSceneCrossSectionMode.DISABLED;
	
	
	
	private EpisimGUIState epiSimulation = null;
	
	private boolean moviePathSet = false;
	private EpisimMovieMaker episimMovieMaker;
	
	private double initialDisplayScale = 1;
	
	private double diffusionFieldOpacity = 1;
	private double modelSceneOpacity = 1;
	
	private ModelClip modelClip;
	
	
	public Display3DHack(double width, double height, GUIState simulation) {

		super(width, height, simulation);
		optionButton.setVisible(true);
		  optionButton.addActionListener(new ActionListener()
        {
        public void actionPerformed(ActionEvent e)
            {
      	  		if(ModeServer.guiMode()){
      	  			optionPane.setVisible(true);
      	  		}
            }
        });
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
            epiSimulation.pressWorkaroundSimulationPause();
            setScale(newValue);
            epiSimulation.pressWorkaroundSimulationPlay();
            return newValue;
            }
        };
        scaleField.setToolTipText("Magnifies the scene.  Not the same as zooming (see the options panel)");
        scaleField.setBorder(BorderFactory.createEmptyBorder(0,0,0,2));
        header.add(scaleField);
        if(ModeServer.guiMode()){
      	  optionPane = new OptionPane3D(epiSimulation.getMainGUIComponent(), "3D Options");
        }
      
	}	
	
   public ModelClip getModelClip() {
	   return modelClip;
   }
	
	public void stopRenderer(){
		canvas.stopCapturing();
		canvas.stopRenderer();
	}
	
	public double getDisplayScale(){
		 return this.getScale();
	}
	
	
   public double getDiffusionFieldOpacity() {
	   return diffusionFieldOpacity;
   }
	
   public double getInitialDisplayScale() {
	   return initialDisplayScale;
   }   
   public void setInitialDisplayScale(double initialDisplayScale) {
   	if(initialDisplayScale > 0)  this.initialDisplayScale = initialDisplayScale;
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
        //createConsoleMenu();
        portrayals = new ArrayList();
        portrayalSwitchMask = null;
        subgraphCount = 0;
        dirty = true;
        return old;
    }
	 
	 public JInternalFrame createInternalFrame(){
		 JInternalFrame frame = new JInternalFrame()
	       {
	       boolean previouslyShown = false;
	       public void dispose()
	           {
	           quit();       // shut down the movies
	           super.dispose();
	           }
	
	       /** Java3D adds a window listener to the frame to determine when the window has been closed;
	           it stops the system as a result (on Linux).  This code removes this listener so the system can go on
	           unabated underneath. */ 
	       public void addInternalFrameListener(InternalFrameListener l) 
	           {
	           if ((new String("class javax.media.j3d.EventCatcher")).compareTo(l.getClass().toString()) == 0)
	              
	                 super.addInternalFrameListener(new InternalFrameAdapter(){});                          
	           
	           }
	
	           
	       /** A bug on MacOS X causes Canvas3Ds to not redisplay if their window is hidden and then reshown.
	           This code gets around it */
	       public void setVisible(boolean val)
	           {
	           super.setVisible(val);
	           // MacOS X Java prior to 1.4.2 update 1 isn't fixed by this, and indeed it just
	           // messes up on the first load of the window.  But previouslyShown at least provides
	           // the status quo for people with the older Java...
	           if (canvas != null && val && previouslyShown && sim.display.Display2D.isMacOSX)
	               {
	               SwingUtilities.invokeLater(new Runnable()
	                   {
	                   public void run()
	                       {
	                       Display3DHack.this.remove(canvas);
	                       Display3DHack.this.add(canvas, BorderLayout.CENTER);
	                       }
	                   });
	               }
	           if (val == true)
	               previouslyShown = true;
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
	           }
	       });
	
	   frame.getContentPane().setLayout(new BorderLayout());
	
	   
	   frame.getContentPane().add(this, BorderLayout.CENTER);
	   frame.getContentPane().setBackground(Color.yellow);
	  return frame;
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
	
	public void createSceneGraph()
   {
   dirty = false;  // reset dirty flag -- we're creating the scene graph
   
   // Recreate the graph -- destroy the existing one
   if (universe == null)   // make a canvas
       {
       //if (universe != null)
       //{ remove(canvas); revalidate(); }
   	if(ModeServer.guiMode()){
       canvas = new CapturingCanvas3D(SimpleUniverse.getPreferredConfiguration());
   	}
   	else{
         canvas = new CapturingCanvas3D(SimpleUniverse.getPreferredConfiguration(), true);
     	}
       
   	
   	add(canvas, BorderLayout.CENTER);
   	
       universe = new SimpleUniverse(canvas);
       universe.getViewingPlatform().setNominalViewingTransform();  //take the viewing point a step back
  
       // set up light switch elements
       lightSwitch = new Switch(Switch.CHILD_MASK);
       lightSwitch.setCapability(Switch.ALLOW_SWITCH_WRITE);
       lightSwitch.setCapability(Switch.ALLOW_CHILDREN_READ);
       lightSwitch.setCapability(Switch.ALLOW_CHILDREN_EXTEND);
                   
       lightSwitchMask.set(SPOTLIGHT_INDEX);    // turn spotlight on
       lightSwitchMask.clear(AMBIENT_LIGHT_INDEX);    // turn ambient light off 
       lightSwitch.setChildMask(lightSwitchMask);
       PointLight pl = new PointLight(new Color3f(1f,1f,1f),
           new Point3f(0f,0f,0f),
           new Point3f(1f,0f,0f));
       pl.setInfluencingBounds(new BoundingSphere(new Point3d(0,0,0), Double.POSITIVE_INFINITY));
       lightSwitch.addChild(pl);
       AmbientLight al = new AmbientLight(new Color3f(1f,1f,1f));
       al.setInfluencingBounds(new BoundingSphere(new Point3d(0,0,0), Double.POSITIVE_INFINITY));
       lightSwitch.addChild(al);
                   
       viewRoot = new BranchGroup();
       viewRoot.addChild(lightSwitch);
       universe.getViewingPlatform().getViewPlatformTransform().addChild(viewRoot);
       
       }
   else // reset the canvas
       {
       // detatches the root and the selection behavior from the universe.
       // we'll need to reattach those.  Everything else: the canvas and lights etc.,
       // will stay connected.
       destroySceneGraph();
       }
   
   // The root in our universe will be a branchgroup
   BranchGroup oldRoot = root;
   root = new BranchGroup();
   // in order to add/remove spinBehavior, I need these:
   root.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
   root.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
   root.setCapability(BranchGroup.ALLOW_BOUNDS_READ);
   // I need this one to delete the root when we reset the canvas above
   root.setCapability(BranchGroup.ALLOW_DETACH);

   // the root's child is a transform group (autoSpinTransformGroup), which can be spun around by the auto-spinner
   autoSpinTransformGroup = new TransformGroup();
   autoSpinTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);  // for spin behavior

   // autoSpinTransformGroup contains a switch to turn the various field portrayals on and off
   portrayalSwitch = new Switch(Switch.CHILD_MASK);
   portrayalSwitch.setCapability(Switch.ALLOW_SWITCH_WRITE);
   portrayalSwitch.setCapability(Switch.ALLOW_CHILDREN_READ);
   portrayalSwitch.setCapability(Switch.ALLOW_CHILDREN_EXTEND);

   // We sneakily include ANOTHER transform group to spin around with another spinner
   // (autoSpinBackground).  This lets us spin the background around with the elements in the universe
   autoSpinBackgroundTransformGroup = new TransformGroup();
   autoSpinBackgroundTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);  // for spin behavior
   
   modelClip = new ModelClip();
   // ADD THE MODEL
   // Add to the switch each subgraph: all the field portrayals plus the axes.
   portrayalSwitchMask = new BitSet(subgraphCount);
   int count = 0;
   Iterator iter = portrayals.iterator();
   while (iter.hasNext())
       {
       Portrayal3DHolder p3h = (Portrayal3DHolder)(iter.next());
       Portrayal3D p = p3h.portrayal;
       Object obj = (p instanceof FieldPortrayal3D)? ((FieldPortrayal3D)p).getField(): null;
       p.setCurrentDisplay(this);
       portrayalSwitch.addChild(p.getModel(obj,null));
       if (p3h.visible)
           portrayalSwitchMask.set(count);
       else
           portrayalSwitchMask.clear(count);
       count++;  // go to next position in visibility mask
       }
   portrayalSwitch.setChildMask(portrayalSwitchMask);

   // add inspection
   BoundingSphere bounds = new BoundingSphere(new Point3d(0.0,0.0,0.0), Double.POSITIVE_INFINITY);
   mSelectBehavior =  new SelectionBehavior(canvas, root, bounds, simulation);
   mSelectBehavior.setSelectsAll(selectionAll, inspectionAll);
   mSelectBehavior.setEnable(selectBehCheckBox.isSelected());

   toolTipBehavior = new ToolTipBehavior(canvas, root, bounds, simulation);
   toolTipBehavior.setEnable(true);
   toolTipBehavior.setCanShowToolTips(usingToolTips);
   
   // make autoSpinTransformGroup spinnable
   // note that Alpha's loop count is ZERO beacuse I want the spin behaivor turned off.
   // Don't forget to put a -1 instead if you want endless spinning. 
   if (autoSpin == null)  // haven't set it up yet
       {
       autoSpin = new RotationInterpolator(new Alpha(), autoSpinTransformGroup);
       autoSpin.getAlpha().setLoopCount(0); 
       autoSpin.setSchedulingBounds(bounds);

       // spin the background too
       autoSpinBackground = new RotationInterpolator(new Alpha(), autoSpinBackgroundTransformGroup);
       autoSpinBackground.getAlpha().setLoopCount(0); 
       autoSpinBackground.setSchedulingBounds(bounds);

       setSpinningEnabled(false);
       }
   else 
       {
       oldRoot.removeChild(autoSpin);  // so it can be added to the new root
       oldRoot.removeChild(autoSpinBackground);
       }

   // create the global model transform group
   rebuildGlobalModelTransformGroup();
   
   // set up auxillary elements
   rebuildAuxillarySwitch();
           
   // add the ability to rotate, translate, and zoom
   mOrbitBehavior = new OrbitBehavior(canvas, OrbitBehavior.REVERSE_ALL);
   mOrbitBehavior.setRotateEnable(true);
   mOrbitBehavior.setRotXFactor(orbitRotateXCheckBox.isSelected() ? 1.0 : 0.0);
   mOrbitBehavior.setRotYFactor(orbitRotateYCheckBox.isSelected() ? 1.0 : 0.0);
   mOrbitBehavior.setTranslateEnable(true);
   mOrbitBehavior.setTransXFactor(orbitTranslateXCheckBox.isSelected() ? 1.0 : 0.0);
   mOrbitBehavior.setTransYFactor(orbitTranslateYCheckBox.isSelected() ? 1.0 : 0.0);
   mOrbitBehavior.setZoomEnable(orbitZoomCheckBox.isSelected());
   mOrbitBehavior.setSchedulingBounds(bounds);
   universe.getViewingPlatform().setViewPlatformBehavior(mOrbitBehavior);
           
   // hook everything up
   globalModelTransformGroup.addChild(portrayalSwitch);
   autoSpinTransformGroup.addChild(globalModelTransformGroup);
   autoSpinTransformGroup.addChild(auxillarySwitch);

   root.addChild(autoSpin);
   root.addChild(autoSpinBackground);
   autoSpin.setTarget(autoSpinTransformGroup);  // reuse
   autoSpinBackground.setTarget(autoSpinBackgroundTransformGroup);  // reuse
   root.addChild(autoSpinTransformGroup);

   // define attributes -- at this point the optionsPanel has been created so it's okay
   setCullingMode(cullingMode);
   setRasterizationMode(rasterizationMode);
   appendClippingPlanes(bounds);
   // call our hook
   sceneGraphCreated();

   // add the universe
   universe.addBranchGraph(root);
   
   // fire it up
   canvas.startRenderer();
   
   //updateSceneGraph(movieMaker != null);  // force a paint into a movie frame if necessary
   }
	
	private void appendClippingPlanes(Bounds bounds){
	
		modelClip.setInfluencingBounds(bounds);  
		boolean enables[] = {true, false, false, false, false, false}; 
		modelClip.setEnables(enables);  
		modelClip.setPlane(0, new Vector4d(0, 0, 1, -50));
		//modelClip.addScope(portrayalSwitch);
		//modelClip.setPlane(1, new Vector4d(0,1,0,-0.1));
		globalModelTransformGroup.addChild(modelClip);  

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
   public class OptionPane3D extends JDialog
   {
   	
   	private JComboBox<DiffusionFieldCrossSectionMode> diffFieldPlaneCombo;
   	private JSlider diffFieldPlaneSlider;
   	private JLabel diffFieldPlaneSliderLabel;
   	private JSlider diffFieldOpacitySlider;
   	private JLabel diffFieldOpacitySliderLabel;
   	private int lastDiffFieldPlaneSliderPosition = 0;
   	private int lastDiffFieldOpacitySliderPosition = 100;
   	
   	private JComboBox<ModelSceneCrossSectionMode> modelScenePlaneCombo;
   	private JSlider modelScenePlaneSlider;
   	private JLabel modelScenePlaneSliderLabel;
   	private JSlider modelSceneOpacitySlider;
   	private JLabel modelSceneOpacitySliderLabel;
   	private int lastModelScenePlaneSliderPosition = 0;
   	private int lastModelSceneOpacitySliderPosition = 100;
   OptionPane3D(Component parent, String label)
       {
       super((JFrame)parent, label, false);
                   
       // set some tool tips
       orbitRotateXCheckBox.setToolTipText("Rotates the scene left or right. Drag the left mouse button.");
       orbitRotateYCheckBox.setToolTipText("Rotates the scene up or down. Drag the left mouse button.");
       orbitTranslateXCheckBox.setToolTipText("Move the scene left or right.  Drag the middle mouse button.");
       orbitTranslateYCheckBox.setToolTipText("Move the scene up or down.  Drag the middle mouse button.");
       orbitZoomCheckBox.setToolTipText("Moves the eye towards/away from scene.  Not the same as scaling.  Drag the right mouse button.");
       selectBehCheckBox.setToolTipText("Selects objects.  Double-click the left mouse button.");

       // Mouse Behaviors
       Box outerBehaviorsPanel = new Box(BoxLayout.X_AXIS);
       outerBehaviorsPanel.setBorder(new javax.swing.border.TitledBorder("Mouse Actions"));
                   
       // add rotateX, translateX, zoom, select to left panel
       Box leftBehaviors = new Box(BoxLayout.Y_AXIS);
       leftBehaviors.add(orbitRotateXCheckBox);
       orbitRotateXCheckBox.setSelected(true);
       leftBehaviors.add(orbitTranslateXCheckBox);
       orbitTranslateXCheckBox.setSelected(true);
       leftBehaviors.add(orbitZoomCheckBox);
       orbitZoomCheckBox.setSelected(true);
       leftBehaviors.add(Box.createGlue());

       // add rotateY, translateY, reset to right panel
       Box rightBehaviors = new Box(BoxLayout.Y_AXIS);
       rightBehaviors.add(orbitRotateYCheckBox);
       orbitRotateYCheckBox.setSelected(true);
       rightBehaviors.add(orbitTranslateYCheckBox);
       orbitTranslateYCheckBox.setSelected(true);
       rightBehaviors.add(selectBehCheckBox);
       selectBehCheckBox.setSelected(true);
       rightBehaviors.add(Box.createGlue());

       outerBehaviorsPanel.add(leftBehaviors);
       outerBehaviorsPanel.add(rightBehaviors);
       outerBehaviorsPanel.add(Box.createGlue());
                   
                   
       Box resetBox = new Box(BoxLayout.X_AXIS);
       resetBox.setBorder(new javax.swing.border.TitledBorder("Viewpoint"));
       JButton resetButton = new JButton("Reset Viewpoint");
       resetButton.setToolTipText("Resets display to original rotation, translation, and zoom.");
       resetBox.add(resetButton);
       resetBox.add(Box.createGlue());

       resetButton.addActionListener(new ActionListener()
           {
           public void actionPerformed(ActionEvent e)
               {
               canvas.stopRenderer();
               // reset scale field
               scaleField.setValue(1);
               setScale(1);
                                   
               universe.getViewingPlatform().setNominalViewingTransform(); // reset translations/rotations
               autoSpinTransformGroup.setTransform(new Transform3D());
               // reset background spin too
               autoSpinBackgroundTransformGroup.setTransform(new Transform3D());
               canvas.startRenderer();
               } 
           });
                   
       orbitRotateXCheckBox.addItemListener(new ItemListener()
           {
           public void itemStateChanged(ItemEvent e)
               {
               if (mOrbitBehavior!=null) mOrbitBehavior.setRotXFactor(orbitRotateXCheckBox.isSelected() ? 1.0 : 0.0); }
           });
       orbitRotateYCheckBox.addItemListener(new ItemListener()
           {
           public void itemStateChanged(ItemEvent e)
               {
               if (mOrbitBehavior!=null) mOrbitBehavior.setRotYFactor(orbitRotateYCheckBox.isSelected() ? 1.0 : 0.0); }
           });
       orbitTranslateXCheckBox.addItemListener(new ItemListener()
           {
           public void itemStateChanged(ItemEvent e)
               {
               if (mOrbitBehavior!=null) mOrbitBehavior.setTransXFactor(orbitTranslateXCheckBox.isSelected() ? 1.0 : 0.0); }
           });
       orbitTranslateYCheckBox.addItemListener(new ItemListener()
           {
           public void itemStateChanged(ItemEvent e)
               {
               if (mOrbitBehavior!=null) mOrbitBehavior.setTransYFactor(orbitTranslateYCheckBox.isSelected() ? 1.0 : 0.0); }
           });
       orbitZoomCheckBox.addItemListener(new ItemListener()
           {
           public void itemStateChanged(ItemEvent e)
               {       if (mOrbitBehavior!=null) mOrbitBehavior.setZoomEnable(orbitZoomCheckBox.isSelected()); }
           });         
       selectBehCheckBox.addItemListener(new ItemListener()
           {
           public void itemStateChanged(ItemEvent e)
               {       if (mSelectBehavior!=null) mSelectBehavior.setEnable(selectBehCheckBox.isSelected()); }
           });         

       // Auto-Orbiting
       LabelledList rotatePanel = new LabelledList("Auto-Rotate About <X,Y,Z> Axis");
       rotatePanel.addLabelled("X", rotAxis_X);
       rotatePanel.addLabelled("Y", rotAxis_Y);
       rotatePanel.addLabelled("Z", rotAxis_Z);
       rotatePanel.addLabelled("Rotations/Sec", spinDuration);
                                   
       Box polyPanel = new Box(BoxLayout.X_AXIS);
       polyPanel.setBorder(new javax.swing.border.TitledBorder("Polygon Attributes"));
       ButtonGroup polyLineGroup = new ButtonGroup();
       polyLineGroup.add(polyPoint);
       polyLineGroup.add(polyLine);
       polyLineGroup.add(polyFill);
       ButtonGroup polyCullingGroup = new ButtonGroup();
       polyCullingGroup.add(polyCullNone);
       polyCullingGroup.add(polyCullFront);
       polyCullingGroup.add(polyCullBack);
                                   
       Box polyLinebox = Box.createVerticalBox();
       polyLinebox.add(Box.createGlue());
       polyLinebox.add (new JLabel ("Draw Polygons As..."));
       polyLinebox.add (polyPoint);
       polyPoint.addActionListener(new ActionListener()
           { 
           public void actionPerformed(ActionEvent e) {setRasterizationMode(PolygonAttributes.POLYGON_POINT);} 
           });
       polyLinebox.add (polyLine);
       polyLine.addActionListener(new ActionListener()
           { 
           public void actionPerformed(ActionEvent e) {setRasterizationMode(PolygonAttributes.POLYGON_LINE);} 
           });
       polyLinebox.add (polyFill);
       polyFill.addActionListener(new ActionListener()
           { 
           public void actionPerformed(ActionEvent e) {setRasterizationMode(PolygonAttributes.POLYGON_FILL);} 
           });
       polyLinebox.add(Box.createGlue());
       polyLinebox.setBorder(new javax.swing.border.EmptyBorder(0,0,0,20));
       polyPanel.add(polyLinebox);
       Box polyCullbox = Box.createVerticalBox();
       polyCullbox.add(Box.createGlue());
       polyCullbox.add (new JLabel ("Draw Faces As..."));
       polyCullbox.add (polyCullNone);
       polyCullNone.addActionListener(new ActionListener()
           { 
           public void actionPerformed(ActionEvent e) {setCullingMode(PolygonAttributes.CULL_NONE);} 
           });
       polyCullbox.add (polyCullBack);
       polyCullBack.addActionListener(new ActionListener()
           { 
           public void actionPerformed(ActionEvent e) {setCullingMode(PolygonAttributes.CULL_BACK);} 
           });
       polyCullbox.add (polyCullFront);
       polyCullFront.addActionListener(new ActionListener()
           { 
           public void actionPerformed(ActionEvent e) {setCullingMode(PolygonAttributes.CULL_FRONT);} 
           });
       polyCullbox.add(Box.createGlue());
       polyCullbox.setBorder(new javax.swing.border.EmptyBorder(0,0,0,20));
       polyPanel.add(polyCullbox);
       polyPanel.add(Box.createGlue());
        
       Box diffCrossectionPanel = null;
      
      	 diffCrossectionPanel = new Box(BoxLayout.Y_AXIS);
      	 JPanel mainPanel = new JPanel(new BorderLayout(10,10));
	       mainPanel.setBorder(new javax.swing.border.TitledBorder("Diffusion Field Cross-Section Plane"));
	       JPanel planeComboPanel = new JPanel(new BorderLayout(10,10));
	       JLabel comboBoxLabel = new JLabel(OptionPane3D.DF_CROSSSECTION_PLANE);
	       planeComboPanel.add(comboBoxLabel, BorderLayout.WEST);
	       diffFieldPlaneCombo = new JComboBox<DiffusionFieldCrossSectionMode>(ExtraCellularDiffusionController.DiffusionFieldCrossSectionMode.values());
	       diffFieldPlaneCombo.setSelectedIndex(0);
	       
	       diffFieldPlaneCombo.addItemListener(new ItemListener(){			
				public void itemStateChanged(ItemEvent e) {
					if(e.getStateChange() ==ItemEvent.SELECTED){
						ModelController.getInstance().getExtraCellularDiffusionController().setSelectedDiffusionFieldCrossSectionMode((DiffusionFieldCrossSectionMode)diffFieldPlaneCombo.getSelectedItem());
					
						updateSceneGraph(true);
					}
					
				}});
	       planeComboPanel.add(diffFieldPlaneCombo, BorderLayout.CENTER);
	      
	       JPanel planeSilderPanel = new JPanel(new BorderLayout(10,10));
	       diffFieldPlaneSlider = new JSlider(JSlider.HORIZONTAL,0,100,0);
	       diffFieldPlaneSlider.setMajorTickSpacing(1);
	       diffFieldPlaneSlider.setMinorTickSpacing(1);      
	       diffFieldPlaneSlider.setPaintLabels(false);       
	       diffFieldPlaneSliderLabel = new JLabel(diffFieldPlaneSlider.getValue()+ " µm");       
	       diffFieldPlaneSlider.addChangeListener(new ChangeListener(){
				public void stateChanged(ChangeEvent e) {
					if(lastDiffFieldPlaneSliderPosition != diffFieldPlaneSlider.getValue()){
						lastDiffFieldPlaneSliderPosition = diffFieldPlaneSlider.getValue();
						double fact =  ((double) diffFieldPlaneSlider.getValue())/100d;
						DiffusionFieldCrossSectionMode selectedComboItem = (DiffusionFieldCrossSectionMode) diffFieldPlaneCombo.getSelectedItem();
						double result = 0;
						if(selectedComboItem == DiffusionFieldCrossSectionMode.X_Y_PLANE){
							double length = TissueController.getInstance().getTissueBorder().getLengthInMikron();
							result =length*fact;
						}
						else if(selectedComboItem == DiffusionFieldCrossSectionMode.X_Z_PLANE){
							double height = TissueController.getInstance().getTissueBorder().getHeightInMikron();
							result =height*fact;
						}
						else if(selectedComboItem == DiffusionFieldCrossSectionMode.Y_Z_PLANE){
							double width = TissueController.getInstance().getTissueBorder().getWidthInMikron();
							result =width*fact;
						}
						ModelController.getInstance().getExtraCellularDiffusionController().setDiffusionFieldCrossSectionCoordinate(result);
					
						diffFieldPlaneSliderLabel.setText(result + " µm");
						 SwingUtilities.invokeLater(new Runnable(){ public void run(){ updateSceneGraph(true);}});
						
					}
				
					
				}});
	       planeSilderPanel.add(new JLabel("Position on Axis: "), BorderLayout.WEST);
	       planeSilderPanel.add(diffFieldPlaneSlider, BorderLayout.CENTER);
	       planeSilderPanel.add(diffFieldPlaneSliderLabel, BorderLayout.EAST);
	       JPanel opacitySilderPanel = null;
		  if(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_DIFFUSION_FIELD_3DVISUALIZATION) == null
							|| !EpisimProperties.getProperty(EpisimProperties.SIMULATOR_DIFFUSION_FIELD_3DVISUALIZATION).toLowerCase().equals(EpisimProperties.SIMULATOR_DF_3DVISUALIZATION_BLOCK_MODE)){ 
		       opacitySilderPanel = new JPanel(new BorderLayout(10,10));
		       diffFieldOpacitySlider = new JSlider(JSlider.HORIZONTAL,0,100,100);
		       diffFieldOpacitySlider.setMajorTickSpacing(1);
		       diffFieldOpacitySlider.setMinorTickSpacing(1);      
		       diffFieldOpacitySlider.setPaintLabels(false);       
		       diffFieldOpacitySliderLabel = new JLabel(diffFieldOpacitySlider.getValue()+ "%");       
		       diffFieldOpacitySlider.addChangeListener(new ChangeListener(){
					public void stateChanged(ChangeEvent e) {
						if(lastDiffFieldOpacitySliderPosition != diffFieldOpacitySlider.getValue()){
							lastDiffFieldOpacitySliderPosition = diffFieldOpacitySlider.getValue();
							diffusionFieldOpacity= ((double) diffFieldOpacitySlider.getValue())/100d;
							
							diffFieldOpacitySliderLabel.setText(diffFieldOpacitySlider.getValue() + "%");
							 SwingUtilities.invokeLater(new Runnable(){ public void run(){ updateSceneGraph(false);}});
						}
					
						
					}});
		       opacitySilderPanel.add(new JLabel("Diffusion Field Opacity: "), BorderLayout.WEST);
		       opacitySilderPanel.add(diffFieldOpacitySlider, BorderLayout.CENTER);
		       opacitySilderPanel.add(diffFieldOpacitySliderLabel, BorderLayout.EAST);
		   }
	       
	       JPanel gridPanel = new JPanel(new GridLayout(opacitySilderPanel != null ? 3: 2,1,10,10));
	       gridPanel.add(planeComboPanel);
	       gridPanel.add(planeSilderPanel);
	       if(opacitySilderPanel != null)gridPanel.add(opacitySilderPanel);
	       mainPanel.add(gridPanel, BorderLayout.NORTH);       
	       diffCrossectionPanel.add(mainPanel);
	       
	       
	       Box modelSceneCrossectionPanel = null;
	       
	       modelSceneCrossectionPanel = new Box(BoxLayout.Y_AXIS);
      	 JPanel modelSceneMainPanel = new JPanel(new BorderLayout(10,10));
      	 modelSceneMainPanel.setBorder(new javax.swing.border.TitledBorder("Model View"));
	       JPanel modelScenePlaneComboPanel = new JPanel(new BorderLayout(10,10));
	       JLabel modelSceneComboBoxLabel = new JLabel(OptionPane3D.MODEL_SCENE_CROSSSECTION_PLANE);
	       modelScenePlaneComboPanel.add(modelSceneComboBoxLabel, BorderLayout.WEST);
	       modelScenePlaneCombo = new JComboBox<ModelSceneCrossSectionMode>(ModelSceneCrossSectionMode.values());
	       modelScenePlaneCombo.setSelectedIndex(0);
	       
	       modelScenePlaneCombo.addItemListener(new ItemListener(){			
				public void itemStateChanged(ItemEvent e) {
					if(e.getStateChange() ==ItemEvent.SELECTED){
						modelSceneCrossSectionMode = (ModelSceneCrossSectionMode)modelScenePlaneCombo.getSelectedItem();
					
						updateSceneGraph(true);
					}
					
				}});
	       modelScenePlaneComboPanel.add(modelScenePlaneCombo, BorderLayout.CENTER);
	      
	       JPanel modelScenePlaneSilderPanel = new JPanel(new BorderLayout(10,10));
	       modelScenePlaneSlider = new JSlider(JSlider.HORIZONTAL,0,100,0);
	       modelScenePlaneSlider.setMajorTickSpacing(1);
	       modelScenePlaneSlider.setMinorTickSpacing(1);      
	       modelScenePlaneSlider.setPaintLabels(false);       
	       modelScenePlaneSliderLabel = new JLabel(modelScenePlaneSlider.getValue()+ " µm");       
	       modelScenePlaneSlider.addChangeListener(new ChangeListener(){
				public void stateChanged(ChangeEvent e) {
					if(lastModelScenePlaneSliderPosition != modelScenePlaneSlider.getValue()){
						lastModelScenePlaneSliderPosition = modelScenePlaneSlider.getValue();
						double fact =  ((double) modelScenePlaneSlider.getValue())/100d;
					/*	DiffusionFieldCrossSectionMode selectedComboItem = (DiffusionFieldCrossSectionMode) diffFieldPlaneCombo.getSelectedItem();
						double result = 0;
						if(selectedComboItem == DiffusionFieldCrossSectionMode.X_Y_PLANE){
							double length = TissueController.getInstance().getTissueBorder().getLengthInMikron();
							result =length*fact;
						}
						else if(selectedComboItem == DiffusionFieldCrossSectionMode.X_Z_PLANE){
							double height = TissueController.getInstance().getTissueBorder().getHeightInMikron();
							result =height*fact;
						}
						else if(selectedComboItem == DiffusionFieldCrossSectionMode.Y_Z_PLANE){
							double width = TissueController.getInstance().getTissueBorder().getWidthInMikron();
							result =width*fact;
						}
						ModelController.getInstance().getExtraCellularDiffusionController().setDiffusionFieldCrossSectionCoordinate(result);*/
					
						//modelScenePlaneSliderLabel.setText(result + " µm");
						 SwingUtilities.invokeLater(new Runnable(){ public void run(){ updateSceneGraph(true);}});
						
					}
				
					
				}});
	       modelScenePlaneSilderPanel.add(new JLabel("Position on Axis: "), BorderLayout.WEST);
	       modelScenePlaneSilderPanel.add(modelScenePlaneSlider, BorderLayout.CENTER);
	       modelScenePlaneSilderPanel.add(modelScenePlaneSliderLabel, BorderLayout.EAST);
	       JPanel modelSceneOpacitySilderPanel = null;
		 
	       modelSceneOpacitySilderPanel = new JPanel(new BorderLayout(10,10));
	       modelSceneOpacitySlider = new JSlider(JSlider.HORIZONTAL,0,100,100);
	       modelSceneOpacitySlider.setMajorTickSpacing(1);
	       modelSceneOpacitySlider.setMinorTickSpacing(1);      
	       modelSceneOpacitySlider.setPaintLabels(false);       
	       modelSceneOpacitySliderLabel = new JLabel(modelSceneOpacitySlider.getValue()+ "%");       
	       modelSceneOpacitySlider.addChangeListener(new ChangeListener(){
					public void stateChanged(ChangeEvent e) {
						if(lastDiffFieldOpacitySliderPosition != modelSceneOpacitySlider.getValue()){
							lastDiffFieldOpacitySliderPosition = modelSceneOpacitySlider.getValue();
							modelSceneOpacity= ((double) modelSceneOpacitySlider.getValue())/100d;
							
							modelSceneOpacitySliderLabel.setText(modelSceneOpacitySlider.getValue() + "%");
							 SwingUtilities.invokeLater(new Runnable(){ public void run(){ updateSceneGraph(false);}});
						}
					
						
					}});
		       modelSceneOpacitySilderPanel.add(new JLabel("Model View Opacity: "), BorderLayout.WEST);
		       modelSceneOpacitySilderPanel.add(modelSceneOpacitySlider, BorderLayout.CENTER);
		       modelSceneOpacitySilderPanel.add(modelSceneOpacitySliderLabel, BorderLayout.EAST);
		   
	       
	       JPanel modelSceneGridPanel = new JPanel(new GridLayout(modelSceneOpacitySilderPanel != null ? 3: 2,1,10,10));
	       modelSceneGridPanel.add(modelScenePlaneComboPanel);
	       modelSceneGridPanel.add(modelScenePlaneSilderPanel);
	       modelSceneGridPanel.add(modelSceneOpacitySilderPanel);
	       modelSceneMainPanel.add(modelSceneGridPanel, BorderLayout.NORTH);       
	       modelSceneCrossectionPanel.add(modelSceneMainPanel);
	       
	       
       
       Box auxillaryPanel = new Box(BoxLayout.Y_AXIS);
       JPanel box = new JPanel(new FlowLayout(FlowLayout.LEFT, 10,10));
       auxillaryPanel.setBorder(new javax.swing.border.TitledBorder("Auxillary Elements"));
       box.add(showAxesCheckBox);
       showAxesCheckBox.addItemListener(new ItemListener()
           {
           public void itemStateChanged(ItemEvent e)
               {       
               toggleAxes();
               }
           });
       box.add(showBackgroundCheckBox);
       showBackgroundCheckBox.addItemListener(new ItemListener()
           {
           public void itemStateChanged(ItemEvent e)
               {       
               toggleBackdrop();
               }
           });
       box.add(tooltips);
       tooltips.addItemListener(new ItemListener()
           {
           public void itemStateChanged(ItemEvent e)
               {
               usingToolTips = tooltips.isSelected();
               if (toolTipBehavior != null)
                   toolTipBehavior.setCanShowToolTips(usingToolTips);
               }
           });
         
       box.add(showSpotlightCheckBox);
       showSpotlightCheckBox.setSelected(true);
       showSpotlightCheckBox.addItemListener(new ItemListener()
           {
           public void itemStateChanged(ItemEvent e)
               {       
               toggleSpotlight();
               }
           });

       box.add(showAmbientLightCheckBox);
       showAmbientLightCheckBox.addItemListener(new ItemListener()
           {
           public void itemStateChanged(ItemEvent e)
               {       
               toggleAmbientLight();
               }
           });
       box.add(Box.createGlue());
       auxillaryPanel.add(box);

       // set up initial design
                           
                   
       Box optionsPanel = new Box(BoxLayout.Y_AXIS);
       optionsPanel.add(outerBehaviorsPanel);
       optionsPanel.add(rotatePanel);
       if(modelSceneCrossectionPanel != null)optionsPanel.add(modelSceneCrossectionPanel);
       if(diffCrossectionPanel != null)optionsPanel.add(diffCrossectionPanel);       
       optionsPanel.add(auxillaryPanel);      
       optionsPanel.add(polyPanel);
       optionsPanel.add(resetBox);
       //optionsPanel.add(viewPanel);

       getContentPane().add(optionsPanel);
       
                   
                   
       // add preferences
                           
     
      
       pack();
       setIconImage(new ImageIcon(ImageLoader.class.getResource("icon.gif")).getImage());
       centerMe(this);
       setResizable(false);
     } 


   private void centerMe(JDialog frame){
		if(frame != null){
			Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
			frame.setLocation(((int)((screenDim.getWidth()/2)-(frame.getPreferredSize().getWidth()/2))), 
			((int)((screenDim.getHeight()/2)-(frame.getPreferredSize().getHeight()/2))));
		}
	}
   
                   
                   
   static final String ROTATE_LEFT_RIGHT_KEY = "Rotate Left Right";
   static final String TRANSLATE_LEFT_RIGHT_KEY = "Translate Left Right";
   static final String MOVE_TOWARDS_AWAY_KEY = "Move Towards Away";
   static final String ROTATE_UP_DOWN_KEY = "Rotate Up Down";
   static final String TRANSLATE_UP_DOWN_KEY = "Translate Up Down";
   static final String SELECT_KEY = "Select";
   static final String AUTO_ROTATE_X_KEY = "Auto Rotate X";
   static final String AUTO_ROTATE_Y_KEY = "Auto Rotate Y";
   static final String AUTO_ROTATE_Z_KEY = "Auto Rotate Z";
   static final String AUTO_ROTATE_RATE_KEY = "Auto Rotate Rate";
   static final String AXES_KEY = "Axes";
   static final String TOOLTIPS_KEY = "Tooltips";
   static final String SPOTLIGHT_KEY = "Spotlight";
   static final String AMBIENT_LIGHT_KEY = "Ambient Light";
   static final String BACKDROP_KEY = "Backdrop";
   static final String DRAW_POLYGONS_KEY = "Draw Polygons";
   static final String DRAW_FACES_KEY = "Draw Faces";
   static final String DF_CROSSSECTION_PLANE="DF Cross-Section-Mode";  
   static final String MODEL_SCENE_CROSSSECTION_PLANE="Model View Cross-Section-Mode";
  
   }

//must be after all other declared widgets because its constructor relies on them existing
private OptionPane3D  optionPane;

}
