package sim.display3d;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import java.util.prefs.Preferences;

import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.Transform3D;
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
	
	
	private EpisimGUIState epiSimulation = null;
	
	private boolean moviePathSet = false;
	private EpisimMovieMaker episimMovieMaker;
	
	private double initialDisplayScale = 1;
	
	private double diffusionFieldOpacity = 1;
	
	
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
            epiSimulation.workaroundConsolePause();
            setScale(newValue);
            epiSimulation.workaroundConsolePlay();
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
	   frame.getContentPane().add(this,BorderLayout.CENTER);
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
   	
   	private JComboBox<DiffusionFieldCrossSectionMode> planeCombo;
   	private JSlider planeSlider;
   	private JLabel planeSliderLabel;
   	private JSlider opacitySlider;
   	private JLabel opacitySliderLabel;
   	private int lastPlaneSliderPosition = 0;
   	private int lastOpacitySliderPosition = 100;
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
	       JLabel comboBoxLabel = new JLabel(OptionPane3D.CROSSSECTION_PLANE);
	       planeComboPanel.add(comboBoxLabel, BorderLayout.WEST);
	       planeCombo = new JComboBox<DiffusionFieldCrossSectionMode>(ExtraCellularDiffusionController.DiffusionFieldCrossSectionMode.values());
	       planeCombo.setSelectedIndex(0);
	       
	       planeCombo.addItemListener(new ItemListener(){			
				public void itemStateChanged(ItemEvent e) {
					if(e.getStateChange() ==ItemEvent.SELECTED){
						ModelController.getInstance().getExtraCellularDiffusionController().setSelectedDiffusionFieldCrossSectionMode((DiffusionFieldCrossSectionMode)planeCombo.getSelectedItem());
					
						updateSceneGraph(true);
					}
					
				}});
	       planeComboPanel.add(planeCombo, BorderLayout.CENTER);
	      
	       JPanel planeSilderPanel = new JPanel(new BorderLayout(10,10));
	       planeSlider = new JSlider(JSlider.HORIZONTAL,0,100,0);
	       planeSlider.setMajorTickSpacing(1);
	       planeSlider.setMinorTickSpacing(1);      
	       planeSlider.setPaintLabels(false);       
	       planeSliderLabel = new JLabel(planeSlider.getValue()+ " �m");       
	       planeSlider.addChangeListener(new ChangeListener(){
				public void stateChanged(ChangeEvent e) {
					if(lastPlaneSliderPosition != planeSlider.getValue()){
						lastPlaneSliderPosition = planeSlider.getValue();
						double fact =  ((double) planeSlider.getValue())/100d;
						DiffusionFieldCrossSectionMode selectedComboItem = (DiffusionFieldCrossSectionMode) planeCombo.getSelectedItem();
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
					
						planeSliderLabel.setText(result + " �m");
						 SwingUtilities.invokeLater(new Runnable(){ public void run(){ updateSceneGraph(true);}});
						
					}
				
					
				}});
	       planeSilderPanel.add(new JLabel("Position on Axis: "), BorderLayout.WEST);
	       planeSilderPanel.add(planeSlider, BorderLayout.CENTER);
	       planeSilderPanel.add(planeSliderLabel, BorderLayout.EAST);
	       JPanel opacitySilderPanel = null;
		  if(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_DIFFUSION_FIELD_3DVISUALIZATION) == null
							|| !EpisimProperties.getProperty(EpisimProperties.SIMULATOR_DIFFUSION_FIELD_3DVISUALIZATION).toLowerCase().equals(EpisimProperties.SIMULATOR_DF_3DVISUALIZATION_BLOCK_MODE)){ 
		       opacitySilderPanel = new JPanel(new BorderLayout(10,10));
		       opacitySlider = new JSlider(JSlider.HORIZONTAL,0,100,100);
		       opacitySlider.setMajorTickSpacing(1);
		       opacitySlider.setMinorTickSpacing(1);      
		       opacitySlider.setPaintLabels(false);       
		       opacitySliderLabel = new JLabel(opacitySlider.getValue()+ "%");       
		       opacitySlider.addChangeListener(new ChangeListener(){
					public void stateChanged(ChangeEvent e) {
						if(lastOpacitySliderPosition != opacitySlider.getValue()){
							lastOpacitySliderPosition = opacitySlider.getValue();
							diffusionFieldOpacity= ((double) opacitySlider.getValue())/100d;
							
							opacitySliderLabel.setText(opacitySlider.getValue() + "%");
							 SwingUtilities.invokeLater(new Runnable(){ public void run(){ updateSceneGraph(false);}});
						}
					
						
					}});
		       opacitySilderPanel.add(new JLabel("Diffusion Field Opacity: "), BorderLayout.WEST);
		       opacitySilderPanel.add(opacitySlider, BorderLayout.CENTER);
		       opacitySilderPanel.add(opacitySliderLabel, BorderLayout.EAST);
		   }
	       
	       JPanel gridPanel = new JPanel(new GridLayout(opacitySilderPanel != null ? 3: 2,1,10,10));
	       gridPanel.add(planeComboPanel);
	       gridPanel.add(planeSilderPanel);
	       if(opacitySilderPanel != null)gridPanel.add(opacitySilderPanel);
	       mainPanel.add(gridPanel, BorderLayout.NORTH);       
	       diffCrossectionPanel.add(mainPanel);
       
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
   static final String CROSSSECTION_PLANE="DF Cross-Section-Mode";        
  
   }

//must be after all other declared widgets because its constructor relies on them existing
private OptionPane3D  optionPane;

}
