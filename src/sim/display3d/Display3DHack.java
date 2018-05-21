package sim.display3d;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
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
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.prefs.Preferences;

import javax.media.j3d.Alpha;
import javax.media.j3d.AmbientLight;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.GraphicsConfigTemplate3D;
import javax.media.j3d.ModelClip;
import javax.media.j3d.Node;
import javax.media.j3d.PointLight;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.QuadArray;
import javax.media.j3d.RotationInterpolator;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Switch;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TriangleStripArray;
import javax.media.j3d.View;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameListener;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;

import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import com.sun.j3d.utils.behaviors.vp.WandViewBehavior.ResetViewListener;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.geometry.Text2D;
import com.sun.j3d.utils.universe.SimpleUniverse;

import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import episiminterfaces.EpisimSimulationDisplay;
import sim.app.episim.EpisimProperties;
import sim.app.episim.EpisimExceptionHandler;
import sim.app.episim.ModeServer;
import sim.app.episim.SimStateServer;
import sim.app.episim.datamonitoring.dataexport.TissueSnapshotDataExportDialog;
import sim.app.episim.gui.EpisimDisplay3D;
import sim.app.episim.gui.EpisimGUIState;
import sim.app.episim.gui.EpisimProgressWindow;
import sim.app.episim.gui.ExtendedFileChooser;
import sim.app.episim.gui.ImageLoader;
import sim.app.episim.gui.NumberInputDialog;
import sim.app.episim.gui.EpisimProgressWindow.EpisimProgressWindowCallback;
import sim.app.episim.model.controller.ExtraCellularDiffusionController;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.controller.TissueController;
import sim.app.episim.model.controller.ExtraCellularDiffusionController.DiffusionFieldCrossSectionMode;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters.MiscalleneousGlobalParameters3D;
import sim.app.episim.util.EpisimMovieMaker;
import sim.app.episim.util.Names;
import sim.app.episim.visualization.threedim.ContinuousCellFieldPortrayal3D;
import sim.app.episim.visualization.threedim.BasementMembranePortrayal3D;
import sim.app.episim.visualization.threedim.Optimized3DVisualization;
import sim.app.episim.visualization.threedim.TissueCrossSectionPortrayal3D;
import sim.display.Display2D;
import sim.display.Display2DHack;
import sim.display.GUIState;
import sim.display.Prefs;
import sim.display.SimApplet;
import sim.display3d.Display3D.LocalWindowListener;
import sim.display3d.Display3D.Portrayal3DHolder;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.continuous.Continuous3D;
import sim.portrayal.FieldPortrayal;
import sim.portrayal.FieldPortrayal2D;
import sim.portrayal.Portrayal;
import sim.portrayal3d.FieldPortrayal3D;
import sim.portrayal3d.Portrayal3D;
import sim.portrayal3d.continuous.ContinuousPortrayal3D;
import sim.util.Double3D;
import sim.util.gui.LabelledList;
import sim.util.gui.NumberTextField;
import sim.util.gui.Utilities;
import sim.util.media.PNGEncoder;



public class Display3DHack extends Display3D implements EpisimSimulationDisplay
{
	
	//---------------------------------------------------------------------------------------------------------------------------------------------
	// TODO: IMPORTANT delete class OptionPane in class Display3D otherwise headless mode in computer cluster environment does not work !!!
	//----------------------------------------------------------------------------------------------------------------------------------------------
	
	private static final double TRANSLATION_FACTOR = 0.1;
	private static final double ROTATION_FACTOR = 0.5;
	private NumberTextField cellColoringField = null;
	
	public enum ModelSceneCrossSectionMode
	{
		DISABLED("Disabled"),
		X_Y_PLANE("X-Y-Plane"),
		X_Z_PLANE("X-Z-Plane"),
		Y_Z_PLANE("Y-Z-Plane");
		
		private String name;
		
		ModelSceneCrossSectionMode(String name)
		{
			this.name = name;		
		}
		
		public String toString(){ return this.name; }
		
	}
	
	private ModelSceneCrossSectionMode modelSceneCrossSectionMode = ModelSceneCrossSectionMode.DISABLED;
	
	private double actModelSceneCrossSectionCoordinate = 0;
	
	private EpisimGUIState epiSimulation = null;
	
	private boolean moviePathSet = false;
	private EpisimMovieMaker episimMovieMaker;
	
	private double initialDisplayScale = 1;
	
	private double diffusionFieldOpacity = 1;
	private double modelSceneOpacity = 1;
	
	private ModelClip modelClip;
	
	private EpisimCellBehavioralModelGlobalParameters globalCBMParameters;
	private Method cellColoringGetterMethod;
	private Method cellColoringSetterMethod;
	private boolean optimizedGraphicsActivated = false;
	private boolean automatedPNGSnapshotsEnabled = false;
	
	private Steppable displayRotationSteppable;
	
	public Display3DHack(double width, double height, GUIState simulation) 
	{
		super(width, height, simulation);
		
		if(EpisimProperties.getProperty(EpisimProperties.SIMULATION_PNG_PATH) != null && EpisimProperties.getProperty(EpisimProperties.SIMULATION_PNG_PRINT_FREQUENCY)!=null)
		{
			File pngSnaphotPath = new File(EpisimProperties.getProperty(EpisimProperties.SIMULATION_PNG_PATH));
			
			if(pngSnaphotPath.exists() && pngSnaphotPath.isDirectory())
			{
				automatedPNGSnapshotsEnabled=true;
			}
		}
		
		MiscalleneousGlobalParameters param = MiscalleneousGlobalParameters.getInstance();
		
		if(param instanceof MiscalleneousGlobalParameters3D && ((MiscalleneousGlobalParameters3D)param).getOptimizedGraphics())
		{	
			optimizedGraphicsActivated = true;
		}
		
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
		
		if(moviePathSet)
		{ 
			movieButton.setEnabled(false);
			simulation.scheduleAtStart(new Steppable()   // to stop movie when simulation is stopped
			{
				public void step(SimState state) 
				{   
					SwingUtilities.invokeLater(new Runnable()
					{
						public void run() 
						{
							startMovie();								
						}
					}); 
				} 
			});
		}
		
		if(simulation instanceof EpisimGUIState) epiSimulation = (EpisimGUIState) simulation;
		
		//no unnecessary Entry: Show Console in the popup
		if(popup != null && popup.getComponentCount()>1)
		{
			popup.remove(0);
			popup.remove(0);
		}
		for(Component comp :header.getComponents())
		{
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
        
        if(ModeServer.guiMode())
        {
        	optionPane = new OptionPane3D(epiSimulation.getMainGUIComponent(), "3D Options");
        	displayRotationSteppable = new Steppable()
        	{
        		public void step(SimState state)
        		{
        			if(optionPane != null) optionPane.updateDisplayRotationSpeed(state);
        		}
        	};
        }
    
      	setRotationToPropertyValues();
      	
      	findCellColoringMethods();
      	 
      	if(this.cellColoringGetterMethod != null && this.cellColoringSetterMethod != null)
      	{
      		// Add the "scale" field
          	Object defaultValue = null;
          	try
          	{
          		defaultValue = this.cellColoringGetterMethod.invoke(this.globalCBMParameters, new Object[0]);
          	}
          	catch (Exception e1)
          	{
          		EpisimExceptionHandler.getInstance().displayException(e1);
          	}
             
          	double defaultVal = 0;
          	
          	if(defaultValue != null)
          	{
          		defaultVal = defaultValue instanceof Integer ? (double)((Integer)defaultValue).intValue() : defaultValue instanceof Double ? ((Double)defaultValue).doubleValue() :0;
          	}
    	    
          	// Add the "Cell Coloring" field
          	cellColoringField = new NumberTextField("     Cell Coloring: ", defaultVal, 1, 1)
          	{
          		public double newValue(double newValue)
          		{
          			if (newValue > 0)
          			{
          				if(Integer.TYPE.isAssignableFrom(cellColoringGetterMethod.getReturnType()))
          				{
          					int val = (int) newValue;
          					try
          					{
          						cellColoringSetterMethod.invoke(globalCBMParameters, new Object[]{val});
          					}
          					catch (Exception e)
          					{
          						EpisimExceptionHandler.getInstance().displayException(e);
          					}
          				}
          				else
          				{
          					try
          					{
          						cellColoringSetterMethod.invoke(globalCBMParameters, new Object[]{newValue});
          					}
          					catch (Exception e)
          					{
          						EpisimExceptionHandler.getInstance().displayException(e);
          					}
          				}
          				currentValue = newValue;
          				return newValue;
          			}             
          			return currentValue;
          		}
          	};
          	cellColoringField.setToolTipText("Change Cell Coloring Mode");
          	header.add(cellColoringField);
      	}
	}
	
	public void export3Dmodel(String format) 
	{
		// TODO get this to work on cross-sections too
		
		if (format == "obj") 
		{
			// Save file dialog
	        String objfile = "EPISIM_3D_Model.obj";
	        String matfile = "_material.mtl";
	        JFileChooser savefile = new JFileChooser();
	        savefile.setSelectedFile(new File(objfile));
	        int sf = savefile.showSaveDialog(null);
	        
	        File obj = savefile.getSelectedFile();
	        File mat = new File(savefile.getSelectedFile().getPath().toString() + matfile);
	        
			ArrayList<String> content = new ArrayList<>();
			ArrayList<String> material = new ArrayList<>();
			StringBuffer vertex_string = new StringBuffer();
    		content.add("#EPISIM 3D Model Export");
    		content.add("mtllib " + savefile.getSelectedFile().getPath().toString() + matfile);
    		
			// Loop through portrayal
			Iterator iter = portrayals.iterator();
	        while(iter.hasNext())
	        {
	        	Portrayal3DHolder ph = (Portrayal3DHolder)iter.next();
	        	
	        	// Get model of all cells
	        	if(ph.portrayal instanceof ContinuousCellFieldPortrayal3D) 
	        	{	
	        		ContinuousCellFieldPortrayal3D test = (ContinuousCellFieldPortrayal3D) ph.portrayal;

	        		// Get model transform group
	        		TransformGroup tg = (TransformGroup)portrayalSwitch.getChild(ph.subgraphIndex);
	        		int count1 = 0;
	        		
	        		// Get internal transform group
	        		while (count1 < tg.numChildren())
	        		{	
	        			TransformGroup tgchild = (TransformGroup) tg.getChild(count1);
	        			count1 = count1 +1;
	        			int count2 = 0;
	        			
	        			// Internal transform group --> contains a branchgroup FOR EACH cell
	        			while (count2 < tgchild.numChildren())
	            		{
	 					    content.add("o solid sphere_" + String.valueOf(count2) ); // Log name of sphere
	 					    
	        				BranchGroup bg = (BranchGroup) tgchild.getChild(count2);
	        				Enumeration<Node> en = bg.getAllChildren();
	        				
	        				// Get cell coordinates
	        				Point3d location = new Point3d();
	             	        bg.getBounds().getCenter(location);
	        				
	        				// Each branchgroup contains 1 (ONE) other transform group
	        				while (en.hasMoreElements()) 
	        				{
	            				TransformGroup bgtg = (TransformGroup) en.nextElement();
	            				int count3 = 0;
	            				
	            				// Get the scale vector to scale coordinates for ellipsoid shapes
	            				Transform3D trans = new Transform3D();
            					bgtg.getTransform(trans);
	    	        			Vector3d scalevec3d = new Vector3d();
	    	        			trans.getScale(scalevec3d);
	            				
	            				// It's transform groups all the way down! --> 1 (ONE) element
	            				while (count3 < bgtg.numChildren()) 
	            				{
	            					TransformGroup bgtg2 = (TransformGroup) bgtg.getChild(count3);
	            					count3 = count3 + 1;
	            					int count4 = 0;
		            						    	        			
	            					// Finally, the geometry! --> 4 (FOUR) elements per cell --> 4 concentric shells
	            					while (count4 < bgtg2.numChildren())
	            					{
	            						Sphere geom = (Sphere) bgtg2.getChild(count4);
	            						count4 = count4 + 1;
	            	        			
	            						if (geom.getShape() instanceof Shape3D)
	            	        			{
	            	        				Shape3D modelshape = (Shape3D)geom.getChild(0);	            	        				
	            	        				
	            	        				// Get the color associated to the shape
	            	        				Color3f rawcolor = new Color3f();
	            	        				Color3f ambcolor = new Color3f();
	            	        				Color3f emicolor = new Color3f();
	            	        				Color3f difcolor = new Color3f();
	            	        				Color3f specolor = new Color3f();
	            	        				
	            	        				if (modelshape.getAppearance().getColoringAttributes() != null) // skip if the color is not set
	            	        				{
	            	        					modelshape.getAppearance().getColoringAttributes().getColor(rawcolor);
	            	        					
	            	        					material.add("newmtl sphere_" + String.valueOf(count2) + "_" + String.valueOf(count4));
	            	        					material.add("Kd " + String.valueOf(rawcolor.x) + " " + String.valueOf(rawcolor.y) + " " + String.valueOf(rawcolor.z) );
	            	        				}
	            	        				else if (modelshape.getAppearance().getMaterial() != null) // skip if the material is not set
	            	        				{
	            	        					modelshape.getAppearance().getMaterial().getDiffuseColor(difcolor);
	            	        					modelshape.getAppearance().getMaterial().getAmbientColor(ambcolor);
	            	        					modelshape.getAppearance().getMaterial().getEmissiveColor(emicolor);
	            	        					modelshape.getAppearance().getMaterial().getSpecularColor(specolor);
	            	        					Float ns = modelshape.getAppearance().getMaterial().getShininess();
	            	        					
	            	        					material.add("newmtl sphere_" + String.valueOf(count2) + "_" + String.valueOf(count4));
	            	        					material.add("Kd " + String.valueOf(difcolor.x) + " " + String.valueOf(difcolor.y) + " " + String.valueOf(difcolor.z) );
	            	        					material.add("Ka " + String.valueOf(ambcolor.x) + " " + String.valueOf(ambcolor.y) + " " + String.valueOf(ambcolor.z) );
	            	        					material.add("Ke " + String.valueOf(emicolor.x) + " " + String.valueOf(emicolor.y) + " " + String.valueOf(emicolor.z) );
	            	        					material.add("Ke " + String.valueOf(specolor.x) + " " + String.valueOf(specolor.y) + " " + String.valueOf(specolor.z) );
	            	        					material.add("Ns " + String.valueOf(ns));
	            	        				}
	            	        				
	            	        				if (modelshape.getGeometry() instanceof TriangleStripArray) 
	            	        				{
	            	        					TriangleStripArray ta = (TriangleStripArray)modelshape.getGeometry();
	        	            					
	            	        					/** 
	            	        					 * In a TriangleStripArray, each successive 3 vertices form one triangle.
	            	        					 * For example, a TriangleStripArray with 2 triangles will contain 4 vertices,
	            	        					 * where vertices with index [0,1,2] define the first triangle, 
	            	        					 * and vertices with index [1,2,3] define the second triangle.
	            	        					 **/
	            	            				for(int iv = 0; iv < ta.getVertexCount()-2; iv++ ) // Loop through all vertices
	            	            				{  
	            	            					double [] coordv = new double [3];

													for (int ic = 0; ic < 3; ic++) // Loop through this and next two vertices
													{
		            	            				    ta.getCoordinate(iv+ic, coordv);

		               	        						vertex_string.setLength(0); // Clear stringbuffer
		               	        					    vertex_string.append("v");
		               	        					    
		               	        					    // vertex coordinates
		               	        					    vertex_string.append(" " + (String.valueOf( (location.x + scalevec3d.x*coordv[0]) )));
		               	        					    vertex_string.append(" " + (String.valueOf( (location.y + scalevec3d.y*coordv[1]) )));
		               	        					    vertex_string.append(" " + (String.valueOf( (location.z + scalevec3d.z*coordv[2]) )));
		               	        					    content.add(vertex_string.toString());
													}

													content.add("usemtl sphere_" + String.valueOf(count2) + "_" + String.valueOf(count4));
													content.add("f -3 -2 -1");
													
	            	            				} //next vertex
	            	        				}
	            	        			}
	            					}
	            				}
	        				}
	        				
	        				count2 = count2 + 1;
	            		}
	        		}      		
	        	}

	        	// Get also basement membrane mesh!
	        	else if (ph.portrayal instanceof BasementMembranePortrayal3D)
	        	{
	        		// Get transform group
	        		TransformGroup tg = (TransformGroup)portrayalSwitch.getChild(ph.subgraphIndex);
	        		int count1 = 0;
					
	        		// Transform group contains one (1) shape3D, which is the basement membrane
	        		while (count1 < tg.numChildren())
	        		{
	        			Shape3D modelshape = (Shape3D) tg.getChild(count1);
	        			
		        		if (modelshape.getGeometry() instanceof QuadArray) 
        				{
		        			QuadArray qa = (QuadArray)modelshape.getGeometry();
		        			
		        			/**
		        			 * In a QuadArray, each 4 consecutive vertices define a rectangle.
		        			 * Vertices are not shared between rectangles, unlike in a TriangleStripArray.
		        			 * Therefore, the total number of vertices is divisible by 4.
		        			 * The rectangles are also not necessarily connected, 
		        			 * so each rectangle should be treated as a separate model for exporting.
		        			 */
		        			
		        			int count2 = 0;
		        			
		        			// Get the color associated to the shape
	        				Color3f rawcolor = new Color3f();
	        				Color3f ambcolor = new Color3f();
	        				Color3f emicolor = new Color3f();
	        				Color3f difcolor = new Color3f();
	        				Color3f specolor = new Color3f();
	        				
	        				if (modelshape.getAppearance().getColoringAttributes() != null) // skip if the color is not set
	        				{
	        					modelshape.getAppearance().getColoringAttributes().getColor(rawcolor);
	        					
	        					material.add("newmtl membrane_" + String.valueOf(count1));
	        					material.add("Kd " + String.valueOf(rawcolor.x) + " " + String.valueOf(rawcolor.y) + " " + String.valueOf(rawcolor.z) );
	        				}
	        				else if (modelshape.getAppearance().getMaterial() != null) // skip if the material is not set
	        				{
	        					modelshape.getAppearance().getMaterial().getDiffuseColor(difcolor);
	        					modelshape.getAppearance().getMaterial().getAmbientColor(ambcolor);
	        					modelshape.getAppearance().getMaterial().getEmissiveColor(emicolor);
	        					modelshape.getAppearance().getMaterial().getSpecularColor(specolor);
	        					Float ns = modelshape.getAppearance().getMaterial().getShininess();
	        					
	        					material.add("newmtl membrane_" + String.valueOf(count1));
	        					material.add("Kd " + String.valueOf(difcolor.x) + " " + String.valueOf(difcolor.y) + " " + String.valueOf(difcolor.z) );
	        					material.add("Ka " + String.valueOf(ambcolor.x) + " " + String.valueOf(ambcolor.y) + " " + String.valueOf(ambcolor.z) );
	        					material.add("Ke " + String.valueOf(emicolor.x) + " " + String.valueOf(emicolor.y) + " " + String.valueOf(emicolor.z) );
	        					material.add("Ke " + String.valueOf(specolor.x) + " " + String.valueOf(specolor.y) + " " + String.valueOf(specolor.z) );
	        					material.add("Ns " + String.valueOf(ns));
	        				}
		        			
		        			content.add("o solid membrane_" + String.valueOf(count2) ); // Log name
		        			
		        			for(int iv = 0; iv < qa.getVertexCount(); iv+=1 ) // Loop through all vertices
            				{
            					double [] coordv = new double [3];
	        						
        						// Get initial vertex
        						qa.getCoordinate(iv, coordv); // no return value (void)

        						vertex_string.setLength(0); // Clear stringbuffer
        					    vertex_string.append("v");
        					    
        					    // vertex coordinates
        					    vertex_string.append(" " + (String.valueOf(coordv[0])));
        					    vertex_string.append(" " + (String.valueOf(coordv[1])));
        					    vertex_string.append(" " + (String.valueOf(coordv[2])));
        					    content.add(vertex_string.toString());
        					    
        					    if ((iv+1)%4 == 0) 
        					    {
        					    	content.add("usemtl membrane_" + String.valueOf(count1) );
        					    	content.add("f -4 -3 -2 -1");
        					    	count2 = count2 + 1;
        					    }
        					}
        				}
	    				count1 = count1 + 1;
	        		}
	        	}
	        }
	        
	        // Save the file
	        if(sf == JFileChooser.APPROVE_OPTION)
	        {
	            try 
	            {
	            	FileWriter writer = new FileWriter(obj);
			        BufferedWriter bw = new BufferedWriter(writer);
			        for(String str: content) 
			        {
			            bw.write(str);
			            bw.write("\n");
			        }
			        bw.close();
			        
			        FileWriter writer2 = new FileWriter(mat);
			        BufferedWriter bw2 = new BufferedWriter(writer2);
			        for(String str: material) 
			        {
			            bw2.write(str);
			            bw2.write("\n");
			        }
			        bw2.close();
			        
	                JOptionPane.showMessageDialog(null, "File has been saved","File Saved",JOptionPane.INFORMATION_MESSAGE);
	            }
	            catch (IOException e) 
	            {
	                e.printStackTrace();
	            }
	        }
	        else if(sf == JFileChooser.CANCEL_OPTION)
	        {
	            JOptionPane.showMessageDialog(null, "File save has been canceled");
	        }
		}
		
		else // default to stl file format
		{
			ArrayList<String> content = new ArrayList<>();
			StringBuffer vertex_string = new StringBuffer();
			// Loop through portrayal
			Iterator iter = portrayals.iterator();
	        while(iter.hasNext())
	        {
	        	Portrayal3DHolder ph = (Portrayal3DHolder)iter.next();
	        	

	        	// Get model of all cells
	        	if(ph.portrayal instanceof ContinuousCellFieldPortrayal3D) 
	        	{	
	        		ContinuousCellFieldPortrayal3D test = (ContinuousCellFieldPortrayal3D) ph.portrayal;

	        		// Get model transform group
	        		TransformGroup tg = (TransformGroup)portrayalSwitch.getChild(ph.subgraphIndex);
	        		int count1 = 0;
	        		
	        		// Get internal transform group
	        		while (count1 < tg.numChildren())
	        		{	
	        			TransformGroup tgchild = (TransformGroup) tg.getChild(count1);
	        			count1 = count1 +1;
	        			int count2 = 0;
	        			
	        			// Internal transform group --> contains a branchgroup FOR EACH cell
	        			while (count2 <tgchild.numChildren())
	            		{
	 					    content.add("solid sphere_" + String.valueOf(count2) ); // Log name of sphere
	 					    
	        				BranchGroup bg = (BranchGroup) tgchild.getChild(count2);
	        				Enumeration<Node> en = bg.getAllChildren();
	        				
	        				// Get cell coordinates
	        				Point3d location = new Point3d();
	             	        bg.getBounds().getCenter(location);
	        				
	        				// Each branchgroup contains 1 (ONE) other transform group
	        				while (en.hasMoreElements()) 
	        				{
	            				TransformGroup bgtg = (TransformGroup) en.nextElement();
	            				int count3 = 0;
	            				
	            				// Get the scale vector to scale coordinates for ellipsoid shapes
	            				Transform3D trans = new Transform3D();
            					bgtg.getTransform(trans);
	    	        			Vector3d scalevec3d = new Vector3d();
	    	        			trans.getScale(scalevec3d);
	            				
	            				// It's transform groups all the way down! --> 1 (ONE) element
	            				while (count3 < bgtg.numChildren()) 
	            				{
	            					TransformGroup bgtg2 = (TransformGroup) bgtg.getChild(count3);
	            					count3 = count3 + 1;
	            					int count4 = 0;
		            						    	        			
	            					// Finally, the geometry! --> 4 (FOUR) elements per cell --> 4 concentric shells
	            					while (count4 < bgtg2.numChildren())
	            					{
	            						Sphere geom = (Sphere) bgtg2.getChild(count4);
	            						count4 = count4 + 1;
	            	        			
	            						if (geom.getShape() instanceof Shape3D)
	            	        			{
	            	        				Shape3D modelshape = (Shape3D)geom.getChild(0);
	            	        				
	            	        				if (modelshape.getGeometry() instanceof TriangleStripArray) 
	            	        				{
	            	        					TriangleStripArray ta = (TriangleStripArray)modelshape.getGeometry();
	        	            					
	            	        					/** 
	            	        					 * In a TriangleStripArray, each successive 3 vertices form one triangle.
	            	        					 * For example, a TriangleStripArray with 2 triangles will contain 4 vertices,
	            	        					 * where vertices with index [0,1,2] define the first triangle, 
	            	        					 * and vertices with index [1,2,3] define the second triangle.
	            	        					 **/
	            	            				for(int iv = 0; iv < ta.getVertexCount()-2; iv++ ) // Loop through all vertices
	            	            				{  
	               	        					    // Get normal from cross product, as ta.getNormal gives a null pointer exception

	               	        					    // Would have been easier if the following worked:
	               	        					    // float[] norm1 = null;
													// ta.getNormal(iv, norm1);
													// System.out.println(norm1);
	            	            					
	            	            					// Three vertices define one facet of a triangle.
	        	            						// Use cross product to find normal and orientation of vertices w.r.t. to each other.
	            	            					double [] v1 = new double [3];
	            	            					double [] v2 = new double [3];
	            	            					double [] v3 = new double [3];
	               	        					    ta.getCoordinate(iv+0, v1);
	               	        					    ta.getCoordinate(iv+1, v2);
	               	        					    ta.getCoordinate(iv+2, v3);
	               	        					    
	               	        					    Vector3d v11 = new Vector3d(v1);
	               	        					    Vector3d v22 = new Vector3d(v2);
	               	        					    Vector3d v33 = new Vector3d(v3);
	               	        					    Vector3d normal = new Vector3d();
	               	        					    v22.sub(v11);
													normal.cross(v22,v33);
													normal.normalize();
													
													if (iv > 0) 
													{ 
														content.add("    endloop");
														content.add("endfacet");
													}
													
													content.add("facet normal " + String.valueOf(normal.x) + " " + String.valueOf(normal.y) + " " + String.valueOf(normal.z));
	               	        						content.add("    outer loop");
	               	        						
	            	            					double [] coordv = new double [3];

													for (int ic = 0; ic < 3; ic++) // Loop through this and next two vertices
													{
		            	            				    ta.getCoordinate(iv+ic, coordv); // no return value (void)

		               	        						vertex_string.setLength(0); // Clear stringbuffer
		               	        					    vertex_string.append("        vertex");
		               	        					    
		               	        					    // vertex coordinates
		               	        					    vertex_string.append(" " + (String.valueOf( (location.x + scalevec3d.x*coordv[0]) )));
		               	        					    vertex_string.append(" " + (String.valueOf( (location.y + scalevec3d.y*coordv[1]) )));
		               	        					    vertex_string.append(" " + (String.valueOf( (location.z + scalevec3d.z*coordv[2]) )));
		               	        					    content.add(vertex_string.toString());
													}
	            	            				} //next vertex
	            	        				}
	            	        			}
	            					}
	            				}
	        				}
	        				
	        				content.add("endsolid sphere_" + String.valueOf(count2));
	        				content.add("\n");
	        				count2 = count2 + 1;
	            		}
	        		}      		
	        	}

	        	// Get also basement membrane mesh!
	        	else if (ph.portrayal instanceof BasementMembranePortrayal3D)
	        	{
	        		// Get transform group
	        		TransformGroup tg = (TransformGroup)portrayalSwitch.getChild(ph.subgraphIndex);
	        		int count1 = 0;
					
	        		// Transform group contains one (1) shape3D, which is the basement membrane
	        		while (count1 < tg.numChildren())
	        		{
	        			Shape3D modelshape = (Shape3D) tg.getChild(count1);
		        		
		        		if (modelshape.getGeometry() instanceof QuadArray) 
        				{
		        			QuadArray qa = (QuadArray)modelshape.getGeometry();
		        			
		        			/**
		        			 * In a QuadArray, each 4 consecutive vertices define a rectangle.
		        			 * Vertices are not shared between rectangles, unlike in a TriangleStripArray.
		        			 * Therefore, the total number of vertices is divisible by 4.
		        			 * The rectangles are also not necessarily connected, 
		        			 * so each rectangle should be treated as a separate model for exporting.
		        			 * Moreover, each rectangle needs to be split into two triangles, with vertices:
		        			 * [n, n+1, n+2] and [n, n+2, n+3]. The normals are the same for both triangles.
		        			 */
		        			
		        			int count2 = 0;
		        			
		        			for(int iv = 0; iv < qa.getVertexCount(); iv+=4 ) // Loop through all vertices
            				{
		    					content.add("solid membrane_" + String.valueOf(count2) ); // Log name
		    					
		        				// Would have been easier if the following worked:
		        				// float[] norm1 = null;
								// ta.getNormal(iv, norm1);
		    					
		    					// Get normal from cross product, as ta.getNormal gives a null pointer exception
        						// Use cross product to find normal and orientation of vertices w.r.t. to each other.
            					double [] v1 = new double [3];
            					double [] v2 = new double [3];
            					double [] v3 = new double [3];
            					qa.getCoordinate(iv+0, v1);
	        					qa.getCoordinate(iv+1, v2);
	        					qa.getCoordinate(iv+2, v3);
	        					    
	        					Vector3d v11 = new Vector3d(v1);
	        					Vector3d v22 = new Vector3d(v2);
	        					Vector3d v33 = new Vector3d(v3);
	        					Vector3d normal = new Vector3d();
	        					v22.sub(v11);
								normal.cross(v22,v33);
								normal.normalize();
		
            					double [] coordv = new double [3];
            					
            					/*
            					 * 1st subtriangle: [n, n+1, n+2]
            					 * 2nd subtriangle: [n, n+2, n+3]
            					 */
            					for (int subtriangle = 0; subtriangle < 2; subtriangle++) // subdivide rectangle into two triangles
            					{
            						content.add("facet normal " + String.valueOf(normal.x) + " " + String.valueOf(normal.y) + " " + String.valueOf(normal.z));
	        						content.add("    outer loop");
	        						
            						// Get initial vertex
            						qa.getCoordinate(iv, coordv); // no return value (void)

   	        						vertex_string.setLength(0); // Clear stringbuffer
   	        					    vertex_string.append("        vertex");
   	        					    
   	        					    // vertex coordinates
   	        					    vertex_string.append(" " + (String.valueOf(coordv[0])));
   	        					    vertex_string.append(" " + (String.valueOf(coordv[1])));
   	        					    vertex_string.append(" " + (String.valueOf(coordv[2])));
   	        					    content.add(vertex_string.toString());
   	        					    
   	        					    
            						for (int ic = 1; ic < 3; ic++) // Loop through next two vertices
    								{
                						qa.getCoordinate(iv+ic+subtriangle, coordv); // no return value (void)

       	        					    vertex_string.setLength(0); // Clear stringbuffer
       	        					    vertex_string.append("        vertex");
       	        					    
       	        					    // vertex coordinates
       	        					    vertex_string.append(" " + (String.valueOf(coordv[0])));
       	        					    vertex_string.append(" " + (String.valueOf(coordv[1])));
       	        					    vertex_string.append(" " + (String.valueOf(coordv[2])));
       	        					    content.add(vertex_string.toString());
    								}
            						
									content.add("    endloop");
									content.add("endfacet");
            					}
            					
			    				content.add("endsolid membrane_" + String.valueOf(count2));
			    				content.add("\n");
			    				count2 = count2 + 1;
            				} // next vertex
        				}
	    				count1 = count1 + 1;
	        		}
	        	}
	        }
	        
	        // Save file dialog
	        String stlfile = "EPISIM_3D_Model.stl";
	        JFileChooser savefile = new JFileChooser();
	        savefile.setSelectedFile(new File(stlfile));
	        int sf = savefile.showSaveDialog(null);
	        if(sf == JFileChooser.APPROVE_OPTION)
	        {
	            try 
	            {
	    	        
	    	        File stl = savefile.getSelectedFile();
	    	        
	            	FileWriter writer = new FileWriter(stl);
			        BufferedWriter bw = new BufferedWriter(writer);
			        for(String str: content) 
			        {
			            bw.write(str);
			            bw.write("\n");
			        }
			        bw.close();
			        
	                JOptionPane.showMessageDialog(null, "File has been saved","File Saved",JOptionPane.INFORMATION_MESSAGE);
	            }
	            catch (IOException e) 
	            {
	                e.printStackTrace();
	            }
	        }
	        else if(sf == JFileChooser.CANCEL_OPTION)
	        {
	            JOptionPane.showMessageDialog(null, "File save has been canceled");
	        }
		}
	}
	
	public void changeCellColoringMode(double val)
	{
		if(cellColoringField!=null)
		{
			cellColoringField.newValue(val);
			cellColoringField.setValue(val);
		}		
	}
	
	public void takeSnapshot()
	{
		if (SimApplet.isApplet)
		{
			Object[] options = {"Oops"};
			JOptionPane.showOptionDialog(
					this, "You cannot save snapshots from an applet.",
					"MASON Applet Restriction",
					JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE,
					null, options, options[0]);
			return;
		}

	   // start the image
	   canvas.beginCapturing(false);
   
	   // NOW pop up the save window
	   FileDialog fd = new FileDialog(getFrame(), 
	       "Save Snapshot as 24-bit PNG...", 
	       FileDialog.SAVE);
	   
	   if(!automatedPNGSnapshotsEnabled)
	   {
		   fd.setFile("Untitled.png");
		   fd.setVisible(true);
	   }
	   
	   if (fd.getFile()!=null ||automatedPNGSnapshotsEnabled)
	       try
           {
	           File snapShotFile = automatedPNGSnapshotsEnabled ? EpisimProperties.getFileForPathOfAProperty(EpisimProperties.SIMULATION_PNG_PATH, "EPISIM_Visualization_Snapshot", ".png")
	         		  : new File(fd.getDirectory(), Utilities.ensureFileEndsWith(fd.getFile(),".png"));
	//           PNGEncoder tmpEncoder = new PNGEncoder(image, false,PNGEncoder.FILTER_NONE,9);
	           BufferedImage image = canvas.getLastImage();
	           PNGEncoder tmpEncoder = new PNGEncoder(image, false,PNGEncoder.FILTER_NONE,9);
	           OutputStream stream = new BufferedOutputStream(new FileOutputStream(snapShotFile));
	           stream.write(tmpEncoder.pngEncode());
	           stream.close();
	           image.flush();  // just in case -- OS X bug?
           }
	       catch (FileNotFoundException e) { } // fail
	       catch (IOException e) { /* could happen on close? */} // fail
	}
		
	boolean imageRenderingCompleted = false;
		
	public void renderCrossSectionStack(final double deltaInMikron, final double userDefStartValueInMikron, final ModelSceneCrossSectionMode mode)
	{
	   if (SimApplet.isApplet)
	   {
		   Object[] options = {"Oops"};
		   JOptionPane.showOptionDialog(this, "You cannot save snapshots from an applet.", "MASON Applet Restriction", JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
		   return;
	   }
	
	   // NOW pop up the save window
	   final ExtendedFileChooser fd = new ExtendedFileChooser("png");
	   fd.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);   
	   int fdResult = fd.showSaveDialog(getFrame());
	   
	   if (fdResult == JFileChooser.APPROVE_OPTION && fd.getSelectedFile()!= null && fd.getSelectedFile().isDirectory() && mode != null)
	   {
		   EpisimProgressWindowCallback cb = new EpisimProgressWindowCallback() 
		   {					
			   public void taskHasFinished() 
			   {
				   ((Frame)((EpisimGUIState) simulation).getMainGUIComponent()).setEnabled(true);
				   epiSimulation.pressWorkaroundSimulationPlay();						
			   }
					
			   @Override
			   public void executeTask() 
			   {
				   epiSimulation.pressWorkaroundSimulationPause();
				   ((Frame)((EpisimGUIState) simulation).getMainGUIComponent()).setEnabled(false);
				   	
				   double scale = getScale(); 
				   Transform3D autoSpinTrans =  new Transform3D();
				   autoSpinTransformGroup.getTransform(autoSpinTrans);
				      
				   Transform3D autoSpinBackgroundTrans =  new Transform3D();
				   autoSpinBackgroundTransformGroup.getTransform(autoSpinBackgroundTrans);
				      
				   Transform3D viewPlatformTrans =  new Transform3D();
				   universe.getViewingPlatform().getViewPlatformTransform().getTransform(viewPlatformTrans);	      
				      
				   setDisplaySettingsForCrossSections(mode);
				   	
				   double width = TissueController.getInstance().getTissueBorder().getWidthInMikron();
				   double height = TissueController.getInstance().getTissueBorder().getHeightInMikron();
				   double length = TissueController.getInstance().getTissueBorder().getLengthInMikron();
				   	
				   double originalModelSceneCrossSectionCoordinate = actModelSceneCrossSectionCoordinate;
				   ModelSceneCrossSectionMode originalModelSceneCrossSectionMode = modelSceneCrossSectionMode;
				   	
				   Vector4d[] planes = new Vector4d[]{new Vector4d(), new Vector4d(), new Vector4d(), new Vector4d(), new Vector4d(), new Vector4d()};
				   modelClip.getPlanes(planes);
				   	
				   boolean[] enables = new boolean[6];
				   modelClip.getEnables(enables);
				   	
				   double startPosInMikron = 0;
				   
				   if(mode == ModelSceneCrossSectionMode.Y_Z_PLANE)startPosInMikron=width;
				   else if(mode == ModelSceneCrossSectionMode.X_Y_PLANE)startPosInMikron=height;
				   else if(mode == ModelSceneCrossSectionMode.X_Z_PLANE)startPosInMikron=length;
						
				   startPosInMikron = startPosInMikron < userDefStartValueInMikron ? startPosInMikron : userDefStartValueInMikron;
				   long waitingTimeInMs = 1000;
				   
				   if(EpisimProperties.getProperty(EpisimProperties.DISPLAY_3D_CROSSSECTION_STACK_WAITMS) != null)
				   {
					   try
					   {
						   long waitingTime = Long.parseLong(EpisimProperties.getProperty(EpisimProperties.DISPLAY_3D_CROSSSECTION_STACK_WAITMS));
						   waitingTimeInMs = waitingTime;
					   }
					   catch(NumberFormatException e)
					   {
				   			/*Do nothing*/
					   }
				   }
				   			
				   for(double posInMikron =startPosInMikron; posInMikron >= 0; posInMikron -= deltaInMikron)
				   {
					   setCrossSectionPlane(posInMikron, mode);
					   //take into account the display delay
					   try
					   {
						   Thread.sleep(waitingTimeInMs);
					   }
					   catch (InterruptedException e1)
					   {
						   EpisimExceptionHandler.getInstance().displayException(e1);
					   }
				   		
					   canvas.beginCapturing(false);			   
				   	  	
					   try   
				   	   {
						   File snapShotFile =  EpisimProperties.getFileForDirectoryPath(fd.getSelectedFile().getAbsolutePath(), "EPISIM_Visualization_Cross_Section_"+posInMikron, ".png", Long.MAX_VALUE);
						   BufferedImage image = canvas.getLastImage();
				   			           
						   PNGEncoder tmpEncoder = new PNGEncoder(image, false, PNGEncoder.FILTER_NONE, 9);
						   OutputStream stream = new BufferedOutputStream(new FileOutputStream(snapShotFile));
						   stream.write(tmpEncoder.pngEncode());
						   stream.close();
						   image.flush();  // just in case -- OS X bug?	   			          
				   	   }
				   	   catch (FileNotFoundException e) { EpisimExceptionHandler.getInstance().displayException(e); } // fail
				   	   catch (IOException e) { EpisimExceptionHandler.getInstance().displayException(e); } // fail
				   }
				   	
				   	canvas.stopRenderer();
				   	universe.getViewingPlatform().setNominalViewingTransform(); // reset translations/rotations  
				   	autoSpinTransformGroup.setTransform(autoSpinTrans);
				   	autoSpinBackgroundTransformGroup.setTransform(autoSpinBackgroundTrans);
				   	universe.getViewingPlatform().getViewPlatformTransform().setTransform(viewPlatformTrans);
				   	modelClip.setPlanes(planes);
				   	modelClip.setEnables(enables);
				   	// scaleField.setValue(scale);
				   	// setScale(scale);
					   
				   	actModelSceneCrossSectionCoordinate=originalModelSceneCrossSectionCoordinate;
				   	modelSceneCrossSectionMode=originalModelSceneCrossSectionMode;
				      
				   	canvas.startRenderer();	   	
				   	updateSceneGraph(true);
			   }
		   };
		   EpisimProgressWindow.showProgressWindowForTask(((Frame)((EpisimGUIState) simulation).getMainGUIComponent()), "Rendering Tissue Cross Section Stack", cb, true);
	   }
	}
	
	private void setCrossSectionPlane(double posInMikron, ModelSceneCrossSectionMode mode)
	{
		posInMikron = posInMikron < 0 ? 0 : posInMikron; 
		int modeOrdinal = mode.ordinal();
		modelSceneCrossSectionMode = mode;
		
		if(mode == ModelSceneCrossSectionMode.DISABLED)
		{			
			modelClip.setEnables(new boolean[]{false, false, false, false, false, false});
		}
		else
		{
			if(modelSceneCrossSectionMode != ModelSceneCrossSectionMode.DISABLED)
			{
				modelClip.setEnable(modelSceneCrossSectionMode.ordinal()-1, false);
			}
			
			if(mode != ModelSceneCrossSectionMode.DISABLED)
			{								
				modelClip.setEnable(mode.ordinal()-1, true);
			}
			
			Vector4d planePosition = new Vector4d();
			modelClip.getPlane(modeOrdinal-1, planePosition);
			
			double result = 0;
			
			if(mode == ModelSceneCrossSectionMode.X_Y_PLANE || mode == ModelSceneCrossSectionMode.DISABLED)
			{
				double length = TissueController.getInstance().getTissueBorder().getLengthInMikron();
				posInMikron = posInMikron > length ? length: posInMikron;  
				result =-1*posInMikron;
				actModelSceneCrossSectionCoordinate = -1*result;
				TissueCrossSectionPortrayal3D.setTissueCrossSectionDirty();
			}
			
			else if(mode == ModelSceneCrossSectionMode.X_Z_PLANE){
				double height = TissueController.getInstance().getTissueBorder().getHeightInMikron();
				posInMikron = posInMikron > height ? height: posInMikron;
				result =-1*posInMikron;
				actModelSceneCrossSectionCoordinate = -1*result;
				TissueCrossSectionPortrayal3D.setTissueCrossSectionDirty();
			}
			
			else if(mode == ModelSceneCrossSectionMode.Y_Z_PLANE)
			{
				double width = TissueController.getInstance().getTissueBorder().getWidthInMikron();
				posInMikron = posInMikron > width ? width: posInMikron;
				result =-1*posInMikron;
				actModelSceneCrossSectionCoordinate = -1*result;
				TissueCrossSectionPortrayal3D.setTissueCrossSectionDirty();
			}
			
			planePosition.w = result;
		
			modelClip.setPlane(modeOrdinal-1, planePosition);										
			modelClip.setEnable(modeOrdinal-1, true);
		}
		updateSceneGraph(true);	
	}
	
	public ModelClip getModelClip() 
	{
		return modelClip;
	}
	
	public void stopRenderer()
	{
		canvas.stopCapturing();
		if(ModeServer.guiMode()) canvas.stopRenderer();
	}
	
	public double getDisplayScale()
	{
		return this.getScale();
	}
	
	public double getDiffusionFieldOpacity() 
	{
		return diffusionFieldOpacity;
	}
   
	public double getModelSceneOpacity() 
	{
		return modelSceneOpacity;
	}
	
	public double getInitialDisplayScale() 
	{
		return initialDisplayScale;
	}   
   
	public void setInitialDisplayScale(double initialDisplayScale) 
	{
		if(initialDisplayScale > 0)  this.initialDisplayScale = initialDisplayScale;
	}	
	
	public void setPortrayalVisible(String name, boolean visible)
	{
		Portrayal3DHolder holder =getPortrayalHolder(name);
		if(holder != null)
		{
			holder.visible = visible;
			holder.menuItem.setSelected(visible);
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
	 
	public JInternalFrame createInternalFrame()
	{
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
	      
	   canvas.getView().setSceneAntialiasingEnable(true); 
	   frame.setResizable(true);
	   frame.setIconifiable(!automatedPNGSnapshotsEnabled);
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

	 private void findCellColoringMethods()
	 {
		 //cellColoringMode
		 if(ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters() != null)
		 {
			 globalCBMParameters = ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters();
			 Method[] methods = globalCBMParameters.getClass().getMethods();
			 for(Method m : methods)
			 {
				 if(m.getName().startsWith("get") 
						&& (m.getName().trim().toLowerCase().contains(Names.CELL_COLORING_MODE_NAME_I)
						  || m.getName().trim().toLowerCase().contains(Names.CELL_COLORING_MODE_NAME_II)
						  || m.getName().trim().toLowerCase().contains(Names.CELL_COLORING_MODE_NAME_III)))
				 {
					 this.cellColoringGetterMethod = m;
					 break;
				 }
			 }
			 if(this.cellColoringGetterMethod != null)
			 {
				 String setterName = "s"+this.cellColoringGetterMethod.getName().trim().substring(1);
				 for(Method m : methods)
				 {
					 if(m.getName().equals(setterName))
					 {
						 this.cellColoringSetterMethod=m;
						 break;
					 }
				 }
			 }			
		 }		
	}
	 
	public boolean isPortrayalVisible(String name)
	{
		Portrayal3DHolder holder =getPortrayalHolder(name);		
		if(holder != null) return holder.visible;
		else return false;
	}
	
	private Portrayal3DHolder getPortrayalHolder(String name)
	{
		Portrayal3DHolder holder;
		for(Object obj :portrayals)
		{
			if(obj instanceof Portrayal3DHolder)
			{
				if((holder =(Portrayal3DHolder)obj).name.equals(name)) return holder;
			}
		}
		return null;
	}
	
	public void changePortrayal(String name, Portrayal3D portrayal)
	{
		Portrayal3DHolder holder = getPortrayalHolder(name);
		if(holder != null)
		{
			holder.portrayal = portrayal;
		}
	}
		
	public CapturingCanvas3D getInsideDisplay() 
	{
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
			GraphicsConfiguration gc = SimpleUniverse.getPreferredConfiguration();
			MiscalleneousGlobalParameters param = MiscalleneousGlobalParameters.getInstance();
		   
			if(param instanceof MiscalleneousGlobalParameters3D && ((MiscalleneousGlobalParameters3D)param).getOptimizedGraphics())
			{	
				optimizedGraphicsActivated = true;
			}
	   				
			GraphicsConfigTemplate3D gct3D = new GraphicsConfigTemplate3D();
			gct3D.setSceneAntialiasing(GraphicsConfigTemplate3D.REQUIRED);
			gc = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getBestConfiguration(gct3D); 
	   		
			canvas = new CapturingCanvas3DHack(gc);
	       
			add(canvas, BorderLayout.CENTER);
	   	
			universe = new SimpleUniverse(canvas);
			universe.getViewingPlatform().setNominalViewingTransform();  //take the viewing point a step back
	  
			// set up light switch elements
			lightSwitch = new Switch(Switch.CHILD_MASK);
			lightSwitch.setCapability(Switch.ALLOW_SWITCH_WRITE);
			lightSwitch.setCapability(Switch.ALLOW_CHILDREN_READ);
			lightSwitch.setCapability(Switch.ALLOW_CHILDREN_EXTEND);
	                   
			lightSwitchMask.set(SPOTLIGHT_INDEX);    // turn spotlight on
	       
	      	if(optimizedGraphicsActivated) lightSwitchMask.set(AMBIENT_LIGHT_INDEX);
	      	else lightSwitchMask.clear(AMBIENT_LIGHT_INDEX);    // turn ambient light off 
	       
	      	lightSwitch.setChildMask(lightSwitchMask);
	       
	      	PointLight pl = optimizedGraphicsActivated ? new PointLight(new Color3f(1.0f, 1.5f, 1.5f), new Point3f(0f,0f,0f), new Point3f(1f,0f,0f))
	      		 :  new PointLight(new Color3f(1f,1f,1f), new Point3f(0f,0f,0f), new Point3f(1f,0f,0f));
	       
	      	pl.setInfluencingBounds(new BoundingSphere(new Point3d(0,0,0), Double.POSITIVE_INFINITY));
	      	lightSwitch.addChild(pl);
	       
	      	AmbientLight al = optimizedGraphicsActivated ? new AmbientLight(new Color3f(0.3f, 0.3f, 0.3f))
	      		 :new AmbientLight(new Color3f(1f,1f,1f));
	       
	      	al.setInfluencingBounds(new BoundingSphere(new Point3d(0,0,0), Double.POSITIVE_INFINITY));
	      	lightSwitch.addChild(al);
	                   
	      	viewRoot = new BranchGroup();
	      	viewRoot.addChild(lightSwitch);
	      	universe.getViewingPlatform().getViewPlatformTransform().addChild(viewRoot); 
		}
		else // reset the canvas
		{
			// detaches the root and the selection behavior from the universe.
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
	   	BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), Double.POSITIVE_INFINITY);
	   	mSelectBehavior =  new SelectionBehavior(canvas, root, bounds, simulation);
	   	mSelectBehavior.setSelectsAll(selectionAll, inspectionAll);
	   	mSelectBehavior.setEnable(selectBehCheckBox.isSelected());
	
	   	toolTipBehavior = new ToolTipBehavior(canvas, root, bounds);	   		
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
	   	mOrbitBehavior.setRotXFactor(orbitRotateXCheckBox.isSelected() ? ROTATION_FACTOR : 0.0);
	   	mOrbitBehavior.setRotYFactor(orbitRotateYCheckBox.isSelected() ? ROTATION_FACTOR : 0.0);
	   	mOrbitBehavior.setTranslateEnable(true);
	   	mOrbitBehavior.setTransXFactor(orbitTranslateXCheckBox.isSelected() ? TRANSLATION_FACTOR : 0.0);
	   	mOrbitBehavior.setTransYFactor(orbitTranslateYCheckBox.isSelected() ? TRANSLATION_FACTOR : 0.0);
	   	mOrbitBehavior.setZoomEnable(orbitZoomCheckBox.isSelected());
	   	mOrbitBehavior.setZoomFactor(0.1);
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
	
	public void updateSceneGraph(boolean waitForRenderer)
	{
		// we synchronize here so stopMovie() and startMovie() can
		// prevent us from adding images if necessary.
		if(moviePathSet)
		{
			if (canvas==null) return;  // hasn't been created yet
	        
	        if (dirty && waitForRenderer) { createSceneGraph(); return; }
	    
	        //canvas.stopRenderer();
	                
	        boolean changes = false;
	        Iterator iter = portrayals.iterator();
	        
	        moveBogusMover();
	        while(iter.hasNext())
            {
	            Portrayal3DHolder ph = (Portrayal3DHolder)iter.next();
	            if(portrayalSwitchMask.get(ph.subgraphIndex))
	            {
	                // update model ONLY on what is actually on screen. 
	                ph.portrayal.setCurrentDisplay(this);
	                ph.portrayal.getModel(
	                    (ph.portrayal instanceof FieldPortrayal3D)? ((FieldPortrayal3D)ph.portrayal).getField(): null,
	                    (TransformGroup)portrayalSwitch.getChild(ph.subgraphIndex));
	                changes = true;
	               
	            }
            }
	      
	        //canvas.startRenderer();
	                
	        waitForRenderer &= changes; 
	        if(!waitForRenderer)
	            return;
	            
	        synchronized(canvas)
            {
	        	try
                {
	        		if (!Thread.currentThread().isInterrupted())
	                    // couldn't there be a race condition here?  -- Sean
	                    canvas.wait(0);
                }
	            catch(InterruptedException ex)
                {
	            	try
                    {
	                    Thread.currentThread().interrupt();
                    }
	                catch (SecurityException ex2) { } // some stupid browsers -- *cough* IE -- don't like interrupts
                }
            }
			
	        if(episimMovieMaker!=null)
	        {
	        	synchronized(Display3DHack.this.simulation.state.schedule)
	        	{
	        		// if waitForRenderer is false, does the movie maker ever get updated?  -- Sean
	        		episimMovieMaker.add(canvas.getLastImage());
	        	}
	        }
		}
		else super.updateSceneGraph(waitForRenderer);
	}

	public void destroySceneGraph()
    {
		// unhook the root from the universe so we can reuse the universe (Hmmmm....)
	    mSelectBehavior.detach();
	    root.detach();
	    universe.getLocale().removeBranchGraph(root);
	    if(!canvas.isOffScreen())canvas.stopRenderer();
    }
	
	private void appendClippingPlanes(Bounds bounds)
	{
		modelClip.setCapability(ModelClip.ALLOW_PARENT_READ);
		modelClip.setCapability(ModelClip.ALLOW_ENABLE_READ);
		modelClip.setCapability(ModelClip.ALLOW_ENABLE_WRITE);
		modelClip.setCapability(ModelClip.ALLOW_PLANE_READ);
		modelClip.setCapability(ModelClip.ALLOW_PLANE_WRITE);
		modelClip.setInfluencingBounds(bounds);  
		boolean enables[] = {false, false, false, false, false, false}; 
		modelClip.setEnables(enables);  
		modelClip.setPlane(0, new Vector4d(0, 0, 1, -50));
		modelClip.setPlane(1, new Vector4d(0, 1, 0, -50));
		modelClip.setPlane(2, new Vector4d(1, 0, 0, -50));
		//modelClip.addScope(portrayalSwitch);
		//modelClip.setPlane(1, new Vector4d(0,1,0,-0.1));
		globalModelTransformGroup.addChild(modelClip);
	}
	
	public Frame getFrame()
    {
	    Component c = this;
	    while(c.getParent() != null)
	        c = c.getParent();
	    return c instanceof Frame ? (Frame)c : null;
    }
	
	public void startMovie()
	{
		if(moviePathSet)
		{
			if(ModeServer.guiMode())
			{
				if (episimMovieMaker != null) return;
				
				synchronized(Display3DHack.this.simulation.state.schedule)
				{
					if (episimMovieMaker!=null) return;  // already running
	
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
			else
			{
				System.err.println("WARNING: No movie generation in console mode possible!");
			}
		}
		else super.startMovie();
	}
	
	public void stopMovie()
	{
		if(moviePathSet)
		{
			synchronized(Display3DHack.this.simulation.state.schedule)
		   {
		       if (episimMovieMaker == null) return;  // already stopped
		       canvas.stopCapturing();
		       
		       if (!episimMovieMaker.stop())
		       {		           
		           EpisimExceptionHandler.getInstance().displayException(new Exception("Your movie did not write to disk\ndue to a spurious JMF movie generation bug."));		             
		       }
		       episimMovieMaker = null;		       
		   }
		}
		else super.stopMovie();
	}
	
	public void step(final SimState state)
	{
		if(moviePathSet)
		{
			if (shouldUpdate() &&
                (canvas.isShowing()    // only draw if we can be seen
                || episimMovieMaker != null ))      // OR draw to a movie even if we can't be seen
        	{
				updateSceneGraph(true);
        	}
		}
		else
		{
			super.step(state);
		}
   }	
	
   public void attach(Portrayal portrayal, String name, Rectangle2D.Double bounds, boolean visible) 
   {
	   if(portrayal instanceof Portrayal3D){
		   super.attach((Portrayal3D) portrayal, name, visible);
	   }
   }
	
   public void attach(Portrayal portrayal, String name) 
   {
	   if(portrayal instanceof Portrayal3D)
	   {
		   super.attach((Portrayal3D) portrayal, name);
	   }   
   }
   
   public void attach(Portrayal portrayal, String name, boolean visible)
   {
	   if(portrayal instanceof Portrayal3D)
	   {
		   super.attach((Portrayal3D) portrayal, name, visible);
	   }   
   }
   
   public void setBackdrop(Paint c)
   {
	   if(c instanceof Color)
	   {
		   super.setBackdrop((Color)c);
	   }
   }
   
   private void setRotationToPropertyValues()
   {
	   double rotX=0;
	   double rotY=0;
	   double rotZ=0;
	   double rotPerSec=0;
	   
	   try
	   {
		   if(EpisimProperties.getProperty(EpisimProperties.DISPLAY_3D_ROTATION_X) != null)
		   {
			   rotX = Double.parseDouble(EpisimProperties.getProperty(EpisimProperties.DISPLAY_3D_ROTATION_X));
		   }
	   }
	   catch(NumberFormatException e){/*ignore this exception and do nothing*/}
	   
	   try
	   {
		   if(EpisimProperties.getProperty(EpisimProperties.DISPLAY_3D_ROTATION_Y) != null)
		   {
			   rotY = Double.parseDouble(EpisimProperties.getProperty(EpisimProperties.DISPLAY_3D_ROTATION_Y));
		   }
	   }
	   catch(NumberFormatException e){/*ignore this exception and do nothing*/}
   	
	   try
	   {
		   if(EpisimProperties.getProperty(EpisimProperties.DISPLAY_3D_ROTATION_Z) != null)
		   {
			   rotZ = Double.parseDouble(EpisimProperties.getProperty(EpisimProperties.DISPLAY_3D_ROTATION_Z));
		   }
	   }
	   catch(NumberFormatException e){/*ignore this exception and do nothing*/}
   	
	   try
	   {
		   if(EpisimProperties.getProperty(EpisimProperties.DISPLAY_3D_ROTATION_PERSECOND) != null)
		   {
			   rotPerSec = Double.parseDouble(EpisimProperties.getProperty(EpisimProperties.DISPLAY_3D_ROTATION_PERSECOND));
		   }
	   }
	   catch(NumberFormatException e){/*ignore this exception and do nothing*/}
   	
	   if(rotX!=0 || rotY != 0 || rotZ != 0)
	   {
		   autoSpin.setTransformAxis(getTransformForAxis(rotX, rotY, rotZ));
		   // spin background too
		   autoSpinBackground.setTransformAxis(getTransformForAxis(rotX, rotY, rotZ));
		   if (rotPerSec == 0 ||(rotX == 0 && rotY == 0 && rotZ==0))
			   setSpinningEnabled(false);
		   else setSpinningEnabled(true);
         
		   rotAxis_X.setValue(rotX);
		   rotAxis_Y.setValue(rotY);
		   rotAxis_Z.setValue(rotZ);
	   }
	   
	   if(rotPerSec!=0)
	   {
		   long mSecsPerRot = (rotPerSec == 0 ? 1 /* don't care */ : (long)(1000 / rotPerSec));
          
		   autoSpin.getAlpha().setIncreasingAlphaDuration(mSecsPerRot);
		   // spin background too
		   autoSpinBackground.getAlpha().setIncreasingAlphaDuration(mSecsPerRot);
		   if (rotPerSec == 0 ||(rotX == 0 && rotY == 0 && rotZ==0))
			   setSpinningEnabled(false);
		   else setSpinningEnabled(true);   		
		   spinDuration.setValue(rotPerSec);
	   }
   }
   
   private void startVisualizationAnimation(final long duration)
   {
	   Runnable r = new Runnable() 
	   {
		   @Override
		   public void run() 
		   {
			   double oldRotAxisXValue = rotAxis_X.getValue();
			   double oldRotAxisYValue = rotAxis_Y.getValue();
			   double oldRotAxisZValue = rotAxis_Z.getValue();
			   long startTime = System.currentTimeMillis()+100;

			   autoSpin.setTransformAxis(getTransformForAxis(0, 1, 0));
			   autoSpinBackground.setTransformAxis(getTransformForAxis(0, 1, 0));         
			   autoSpin.getAlpha().setIncreasingAlphaDuration(duration);
			   autoSpinBackground.getAlpha().setIncreasingAlphaDuration(duration);
		        
			   autoSpin.setEnable(true);
			   autoSpinBackground.setEnable(true);
		        
			   autoSpin.getAlpha().setLoopCount(0);         
			   autoSpinBackground.getAlpha().setLoopCount(0);
			   autoSpin.getAlpha().setStartTime(startTime);
			   autoSpinBackground.getAlpha().setStartTime(startTime);
			   autoSpin.getAlpha().pause();
			   autoSpinBackground.getAlpha().pause();
			   autoSpin.getAlpha().setLoopCount(1); 
			   autoSpinBackground.getAlpha().setLoopCount(1);
			   autoSpin.getAlpha().resume(startTime);
			   autoSpinBackground.getAlpha().resume(startTime);
			   rotAxis_X.setValue(0);
			   rotAxis_Y.setValue(1);
			   rotAxis_Z.setValue(0);
		        
			   do
			   {
				   updateSceneGraph(true);
				   try
				   {
					   Thread.sleep(100);
				   }
				   catch (InterruptedException e)
				   {
                  	 EpisimExceptionHandler.getInstance().displayException(e);
				   }
			   }
			   while(!autoSpin.getAlpha().finished());
		    
			   startTime = System.currentTimeMillis()+100;
			   autoSpin.setTransformAxis(getTransformForAxis(1, 0, 0));
			   autoSpinBackground.setTransformAxis(getTransformForAxis(1, 0, 0));         
			   autoSpin.getAlpha().setIncreasingAlphaDuration(duration);
			   autoSpinBackground.getAlpha().setIncreasingAlphaDuration(duration);
		        
			   autoSpin.setEnable(true);
			   autoSpinBackground.setEnable(true);
		        
			   autoSpin.getAlpha().setLoopCount(0);         
			   autoSpinBackground.getAlpha().setLoopCount(0);
			   autoSpin.getAlpha().setStartTime(startTime);
			   autoSpinBackground.getAlpha().setStartTime(startTime);
			   autoSpin.getAlpha().pause();
			   autoSpinBackground.getAlpha().pause();
			   autoSpin.getAlpha().setLoopCount(1); 
			   autoSpinBackground.getAlpha().setLoopCount(1);
			   autoSpin.getAlpha().resume(startTime);
			   autoSpinBackground.getAlpha().resume(startTime);
			   rotAxis_X.setValue(1);
			   rotAxis_Y.setValue(0);
			   rotAxis_Z.setValue(0);
		       
			   do
		       {
				   updateSceneGraph(true);
				   try
				   {
					   Thread.sleep(100);
				   }
				   catch (InterruptedException e)
				   {
					   EpisimExceptionHandler.getInstance().displayException(e);
				   }
		       } 
			   while(!autoSpin.getAlpha().finished());
		        
			   setSpinningEnabled(false);
			   for(int i=0; i < 100; i++)
			   {
				   updateSceneGraph(true);
				   try
				   {
					   Thread.sleep(100);
				   }
				   catch (InterruptedException e)
				   {
					   EpisimExceptionHandler.getInstance().displayException(e);
				   }
			   }
			  	
			   autoSpin.setTransformAxis(getTransformForAxis(oldRotAxisXValue, oldRotAxisYValue, oldRotAxisZValue));
			   autoSpinBackground.setTransformAxis(getTransformForAxis(oldRotAxisXValue, oldRotAxisYValue, oldRotAxisZValue));         
			   autoSpin.getAlpha().setIncreasingAlphaDuration(duration);
			   autoSpinBackground.getAlpha().setIncreasingAlphaDuration(duration);
        
			   autoSpin.setEnable(true);
			   autoSpinBackground.setEnable(true);
			   rotAxis_X.setValue(oldRotAxisXValue);
			   rotAxis_Y.setValue(oldRotAxisYValue);
			   rotAxis_Z.setValue(oldRotAxisZValue);
			   spinDuration.newValue(spinDuration.getValue());
		   }
	   };
	   Thread animationThread = new Thread(r);
	   animationThread.start();
	}
   
	private void resetDisplaySettings()
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
   
	private void setDisplaySettingsForCrossSections(ModelSceneCrossSectionMode mode)
	{
		canvas.stopRenderer();
		// reset scale field
		scaleField.setValue(1);
		setScale(1);
                          
		universe.getViewingPlatform().setNominalViewingTransform(); // reset translations/rotations
		setScale(1.5);
		Transform3D trans = new Transform3D();
		if(mode == ModelSceneCrossSectionMode.X_Z_PLANE) trans.rotX(Math.PI*0.5);
		else if(mode == ModelSceneCrossSectionMode.Y_Z_PLANE) trans.rotY(Math.PI*1.5);
     
		autoSpinTransformGroup.setTransform(trans);
		autoSpinBackgroundTransformGroup.setTransform(trans);
		canvas.startRenderer();
		updateSceneGraph(true);
	}
    
	public double getActModelSceneCrossSectionCoordinate()
	{
		return actModelSceneCrossSectionCoordinate;
	}
	
	public ModelSceneCrossSectionMode getModelSceneCrossSectionMode()
	{
		return modelSceneCrossSectionMode;
	}

	public Steppable getDisplayRotationSteppable()
	{
		return displayRotationSteppable;
	}

	public class OptionPane3D extends JDialog
	{
		private JComboBox diffFieldPlaneCombo;
	   	
	   	private JSlider diffFieldPlaneSlider;
	   	private JLabel diffFieldPlaneSliderLabel;
	   	private JSlider diffFieldOpacitySlider;
	   	private JLabel diffFieldOpacitySliderLabel;
	   	private int lastDiffFieldPlaneSliderPosition = 0;
	   	private int lastDiffFieldOpacitySliderPosition = 100;
	   	
	   	private JComboBox modelScenePlaneCombo;
	   	private JSlider modelScenePlaneSlider;
	   	private JLabel modelScenePlaneSliderLabel;
	   	private JSlider modelSceneOpacitySlider;
	   	private JLabel modelSceneOpacitySliderLabel;
	   	private int lastModelScenePlaneSliderPosition = 0;
	   	private int lastModelSceneOpacitySliderPosition = 100;  
	   	private Box modelSceneCrossectionPanel = null;
	   	private Box diffCrossectionPanel = null;
	   	private JButton sceneAnimationButton;
	   	private JButton takeCrossSectionImageStackButton;
	   	private double rotationPerSimStep = 0;    
	   	private JCheckBox enableRotationPerSimStep;
	   	private NumberTextField rotationPerSimStepDuration;
	   	
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

	   		Box resetAndAnimationBox = new Box(BoxLayout.X_AXIS);
	   		resetAndAnimationBox.setBorder(new javax.swing.border.TitledBorder("Viewpoint"));
	   		JButton resetButton = new JButton("Reset Viewpoint");
	   		resetButton.setToolTipText("Resets display to original rotation, translation, and zoom.");
	   		resetAndAnimationBox.add(resetButton);
	   		resetAndAnimationBox.add(Box.createHorizontalStrut(25));
	   		resetButton.addActionListener(new ActionListener()
	   		{
	   			public void actionPerformed(ActionEvent e)
	   			{
	   				resetDisplaySettings();
	   			} 
	   		});
	   		
	   		JButton Export3DButton = new JButton("Export 3D Model");
	   		Export3DButton.setToolTipText("Export 3D Model");
	   		resetAndAnimationBox.add(Export3DButton);
	   		resetAndAnimationBox.add(Box.createHorizontalStrut(25));
	   		Export3DButton.addActionListener(new ActionListener() 
	   		{
	   			public void actionPerformed(ActionEvent e)
	   			{
	   				JFrame frame = new JFrame();
	   				Object[] options = {"Wavefront obj + mtl",
                            "Stl",
                            "Cancel"};
	   				int n = JOptionPane.showOptionDialog(frame,
                            "Choose file type for 3D model export",
                            "3D model export",
                            JOptionPane.YES_NO_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            options,
                            options[2]);
	   				if (n == JOptionPane.YES_OPTION) 
	   				{
	   					export3Dmodel("obj");
                    } 
	   				else if (n == JOptionPane.NO_OPTION) 
	   				{
	   					export3Dmodel("stl");
                    }
	   			} 
	   		});
       
	   		boolean crossSectionStackEnabled = EpisimProperties.getProperty(EpisimProperties.DISPLAY_3D_CROSSSECTION_STACK) != null 
      		 										&& EpisimProperties.getProperty(EpisimProperties.DISPLAY_3D_CROSSSECTION_STACK).equalsIgnoreCase(EpisimProperties.ON);
	       sceneAnimationButton = new JButton("Animate Scene");
	       sceneAnimationButton.setToolTipText("Animates (rotates) the scene that is visualized.");
	       sceneAnimationButton.addActionListener(new ActionListener() 
	       {
	    	   public void actionPerformed(ActionEvent e) 
	    	   {
	    		   Double choice = NumberInputDialog.showDialog((Frame)Display3DHack.OptionPane3D.this.getParent(), "Enter a value", "Rot per sec",1d);
	    		   
	    		   if(choice != null)
	    		   {
	    			   long mSecsPerRot = (choice.doubleValue() == 0 ? 1 /* don't care */ : (long)(1000 / choice.doubleValue()));
	    			   startVisualizationAnimation(mSecsPerRot);
	    		   }
	    	   }
	       });
	       
	       resetAndAnimationBox.add(sceneAnimationButton);
	       if(crossSectionStackEnabled)resetAndAnimationBox.add(Box.createHorizontalStrut(25));
	       else resetAndAnimationBox.add(Box.createGlue());
		   takeCrossSectionImageStackButton = new JButton("Cross Section Stack");
		   takeCrossSectionImageStackButton.setToolTipText("Renders a stack of cross sections through the tissue and stores the images at the selected path.");
		   takeCrossSectionImageStackButton.addActionListener(new ActionListener() 
		   {
			   public void actionPerformed(ActionEvent e) 
			   {
				   Double deltaMikron = NumberInputDialog.showDialog((Frame)Display3DHack.OptionPane3D.this.getParent(), "Enter a value", "Cross section delta in mikron",1d);
				   Double startMikron = NumberInputDialog.showDialog((Frame)Display3DHack.OptionPane3D.this.getParent(), "Enter a value", "Start value in mikron",100d);
				   
				   if(deltaMikron != null && startMikron != null)
				   {
					   double deltaInMikron = (deltaMikron.doubleValue() <= 0 ? 1 /* don't care */ : deltaMikron.doubleValue());
					   double startInMikron = (startMikron.doubleValue() <= 0 ? 0 /* don't care */ : startMikron.doubleValue());
	      		
					   ModelSceneCrossSectionMode[] modes = new ModelSceneCrossSectionMode[]{ModelSceneCrossSectionMode.X_Y_PLANE,ModelSceneCrossSectionMode.X_Z_PLANE,ModelSceneCrossSectionMode.Y_Z_PLANE};
					   ModelSceneCrossSectionMode selectedMode = (ModelSceneCrossSectionMode) JOptionPane.showInputDialog((Frame)Display3DHack.OptionPane3D.this.getParent(),
							   OptionPane3D.MODEL_SCENE_CROSSSECTION_PLANE,
							   "Select Mode",
							   JOptionPane.PLAIN_MESSAGE,
							   null,
							   modes,
							   ModelSceneCrossSectionMode.Y_Z_PLANE);	      		
					   if(selectedMode != null) renderCrossSectionStack(deltaInMikron, startInMikron, selectedMode);
				   }
			   }
		   });
		   
		   if(crossSectionStackEnabled)
		   {
			   resetAndAnimationBox.add(takeCrossSectionImageStackButton);
			   resetAndAnimationBox.add(Box.createGlue());
		   }
		   
		   orbitRotateXCheckBox.addItemListener(new ItemListener()
           {
			   public void itemStateChanged(ItemEvent e)
			   {
				   if (mOrbitBehavior!=null) mOrbitBehavior.setRotXFactor(orbitRotateXCheckBox.isSelected() ? ROTATION_FACTOR : 0.0); 
			   }
           });
		   
		   orbitRotateYCheckBox.addItemListener(new ItemListener()
           {
			   public void itemStateChanged(ItemEvent e)
               {
				   if (mOrbitBehavior!=null) mOrbitBehavior.setRotYFactor(orbitRotateYCheckBox.isSelected() ? ROTATION_FACTOR : 0.0); 
               }
           });
		   
		   orbitTranslateXCheckBox.addItemListener(new ItemListener()
           {
			   public void itemStateChanged(ItemEvent e)
               {
				   if (mOrbitBehavior!=null) mOrbitBehavior.setTransXFactor(orbitTranslateXCheckBox.isSelected() ? TRANSLATION_FACTOR: 0.0); 
               }
           });
		   
		   orbitTranslateYCheckBox.addItemListener(new ItemListener()
           {
			   public void itemStateChanged(ItemEvent e)
               {
				   if (mOrbitBehavior!=null) mOrbitBehavior.setTransYFactor(orbitTranslateYCheckBox.isSelected() ? TRANSLATION_FACTOR : 0.0);
               }
           });
		   
		   orbitZoomCheckBox.addItemListener(new ItemListener()
           {
			   public void itemStateChanged(ItemEvent e)
               {
				   if (mOrbitBehavior!=null) mOrbitBehavior.setZoomEnable(orbitZoomCheckBox.isSelected());
               }
           });   
		   
		   selectBehCheckBox.addItemListener(new ItemListener()
           {
			   public void itemStateChanged(ItemEvent e)
               {
				   if (mSelectBehavior!=null) mSelectBehavior.setEnable(selectBehCheckBox.isSelected());
               }
           });         
       
		   rotationPerSimStepDuration = new NumberTextField(null, 0, 1, 0.02) // 0, true)
		   {
			   public double newValue(double newValue)
			   {
				   rotationPerSimStep= newValue>=0? newValue : 0;
				   return rotationPerSimStep;  // rounding errors ignored...
			   }
		   };
		   
		   rotationPerSimStepDuration.setEnabled(false);
		   enableRotationPerSimStep = new JCheckBox();
		   enableRotationPerSimStep.setSelected(false);
		   enableRotationPerSimStep.addActionListener(new ActionListener() 
		   {
			   public void actionPerformed(ActionEvent e) 
			   {		
				   spinDuration.setEnabled(!enableRotationPerSimStep.isSelected());
				   rotationPerSimStepDuration.setEnabled(enableRotationPerSimStep.isSelected());
			   }
		   });
    
		   // Auto-Orbiting
		   LabelledList rotatePanel = new LabelledList("Auto-Rotate About <X,Y,Z> Axis");
	       rotatePanel.addLabelled("X", rotAxis_X);
	       rotatePanel.addLabelled("Y", rotAxis_Y);
	       rotatePanel.addLabelled("Z", rotAxis_Z);
	       rotatePanel.addLabelled("Rotations/Sec", spinDuration);
	       rotatePanel.addLabelled("Enable Rotations/Sim Step", enableRotationPerSimStep);
	       rotatePanel.addLabelled("Rotations/Sim Step", rotationPerSimStepDuration);
                                   
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
	       
	       diffCrossectionPanel = new Box(BoxLayout.Y_AXIS);
	       JPanel mainPanel = new JPanel(new BorderLayout(10,10));
	       mainPanel.setBorder(new javax.swing.border.TitledBorder("Diffusion Field Cross-Section Plane"));
	       JPanel planeComboPanel = new JPanel(new BorderLayout(10,10));
	       JLabel comboBoxLabel = new JLabel(OptionPane3D.DF_CROSSSECTION_PLANE);
	       planeComboPanel.add(comboBoxLabel, BorderLayout.WEST);
	       diffFieldPlaneCombo = new JComboBox(ExtraCellularDiffusionController.DiffusionFieldCrossSectionMode.values());
	       diffFieldPlaneCombo.setSelectedIndex(0);
	       
	       diffFieldPlaneCombo.addItemListener(new ItemListener()
	       {			
	    	   public void itemStateChanged(ItemEvent e) 
	    	   {
	    		   if(e.getStateChange() ==ItemEvent.SELECTED)
	    		   {
						DiffusionFieldCrossSectionMode selectedComboItem = (DiffusionFieldCrossSectionMode) diffFieldPlaneCombo.getSelectedItem();
						ModelController.getInstance().getExtraCellularDiffusionController().setSelectedDiffusionFieldCrossSectionMode(selectedComboItem);
						double result =0;
						double fact =  ((double) diffFieldPlaneSlider.getValue())/100d;
						
						if(selectedComboItem == DiffusionFieldCrossSectionMode.X_Y_PLANE)
						{
							double length = TissueController.getInstance().getTissueBorder().getLengthInMikron();
							result =length*fact;
						}
						else if(selectedComboItem == DiffusionFieldCrossSectionMode.X_Z_PLANE)
						{
							double height = TissueController.getInstance().getTissueBorder().getHeightInMikron();
							result =height*fact;
						}
						else if(selectedComboItem == DiffusionFieldCrossSectionMode.Y_Z_PLANE)
						{
							double width = TissueController.getInstance().getTissueBorder().getWidthInMikron();
							result =width*fact;
						}
						
						ModelController.getInstance().getExtraCellularDiffusionController().setDiffusionFieldCrossSectionCoordinate(result);
						diffFieldPlaneSliderLabel.setText(Math.round(result) + " µm");
						SwingUtilities.invokeLater(new Runnable(){ public void run(){ updateSceneGraph(true);}});
	    		   }
	    	   }
	       });
	       
	       planeComboPanel.add(diffFieldPlaneCombo, BorderLayout.CENTER);
	      
	       JPanel planeSilderPanel = new JPanel(new BorderLayout(10,10));
	       diffFieldPlaneSlider = new JSlider(JSlider.HORIZONTAL,0,100,0);
	       diffFieldPlaneSlider.setMajorTickSpacing(1);
	       diffFieldPlaneSlider.setMinorTickSpacing(1);      
	       diffFieldPlaneSlider.setPaintLabels(false);       
	       diffFieldPlaneSliderLabel = new JLabel(diffFieldPlaneSlider.getValue()+ " µm");
	       
	       diffFieldPlaneSlider.addChangeListener(new ChangeListener()
	       {
				public void stateChanged(ChangeEvent e) 
				{
					if(lastDiffFieldPlaneSliderPosition != diffFieldPlaneSlider.getValue())
					{
						lastDiffFieldPlaneSliderPosition = diffFieldPlaneSlider.getValue();
						double fact =  ((double) diffFieldPlaneSlider.getValue())/100d;
						DiffusionFieldCrossSectionMode selectedComboItem = (DiffusionFieldCrossSectionMode) diffFieldPlaneCombo.getSelectedItem();
						double result = 0;
						
						if(selectedComboItem == DiffusionFieldCrossSectionMode.X_Y_PLANE)
						{
							double length = TissueController.getInstance().getTissueBorder().getLengthInMikron();
							result =length*fact;
						}
						else if(selectedComboItem == DiffusionFieldCrossSectionMode.X_Z_PLANE)
						{
							double height = TissueController.getInstance().getTissueBorder().getHeightInMikron();
							result =height*fact;
						}
						else if(selectedComboItem == DiffusionFieldCrossSectionMode.Y_Z_PLANE)
						{
							double width = TissueController.getInstance().getTissueBorder().getWidthInMikron();
							result =width*fact;
						}
						
						ModelController.getInstance().getExtraCellularDiffusionController().setDiffusionFieldCrossSectionCoordinate(result);
					
						diffFieldPlaneSliderLabel.setText(Math.round(result) + " µm");
						SwingUtilities.invokeLater(new Runnable(){ public void run(){ updateSceneGraph(true);}});
					}
				}
	       });
	       
	       planeSilderPanel.add(new JLabel("Position on Axis: "), BorderLayout.WEST);
	       planeSilderPanel.add(diffFieldPlaneSlider, BorderLayout.CENTER);
	       planeSilderPanel.add(diffFieldPlaneSliderLabel, BorderLayout.EAST);
	       JPanel opacitySilderPanel = null;
	       
	       if(EpisimProperties.getProperty(EpisimProperties.DISPLAY_DIFFUSION_FIELD_3DVISUALIZATION) == null
							|| !EpisimProperties.getProperty(EpisimProperties.DISPLAY_DIFFUSION_FIELD_3DVISUALIZATION).toLowerCase().equals(EpisimProperties.DISPLAY_DF_3DVISUALIZATION_BLOCK_MODE))
	       { 
	    	   opacitySilderPanel = new JPanel(new BorderLayout(10,10));
		       diffFieldOpacitySlider = new JSlider(JSlider.HORIZONTAL,0,100,100);
		       diffFieldOpacitySlider.setMajorTickSpacing(1);
		       diffFieldOpacitySlider.setMinorTickSpacing(1);      
		       diffFieldOpacitySlider.setPaintLabels(false);       
		       diffFieldOpacitySliderLabel = new JLabel(diffFieldOpacitySlider.getValue()+ "%");
		       
		       diffFieldOpacitySlider.addChangeListener(new ChangeListener()
		       {
		    	   public void stateChanged(ChangeEvent e) 
		    	   {
		    		   if(lastDiffFieldOpacitySliderPosition != diffFieldOpacitySlider.getValue())
		    		   {
		    			   lastDiffFieldOpacitySliderPosition = diffFieldOpacitySlider.getValue();
		    			   diffusionFieldOpacity= ((double) diffFieldOpacitySlider.getValue())/100d;
							
		    			   diffFieldOpacitySliderLabel.setText(diffFieldOpacitySlider.getValue() + "%");
		    			   SwingUtilities.invokeLater(new Runnable(){ public void run(){ updateSceneGraph(false);}});
		    		   }
		    	   }
		       });
		       
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
	       
	       modelSceneCrossectionPanel = new Box(BoxLayout.Y_AXIS);
	       JPanel modelSceneMainPanel = new JPanel(new BorderLayout(10,10));
	       modelSceneMainPanel.setBorder(new javax.swing.border.TitledBorder("Model View"));
	       JPanel modelScenePlaneComboPanel = new JPanel(new BorderLayout(10,10));
	       JLabel modelSceneComboBoxLabel = new JLabel(OptionPane3D.MODEL_SCENE_CROSSSECTION_PLANE);
	       modelScenePlaneComboPanel.add(modelSceneComboBoxLabel, BorderLayout.WEST);
	       modelScenePlaneCombo = new JComboBox(ModelSceneCrossSectionMode.values());
	       modelScenePlaneCombo.setSelectedIndex(0);
	       
	       final JLabel modelScenePlaneSliderLabel2 =new JLabel("Position on Axis: ");
	       
	       modelScenePlaneCombo.addItemListener(new ItemListener()
	       {			
	    	   public void itemStateChanged(ItemEvent e)
	    	   {
	    		   if(e.getStateChange() ==ItemEvent.SELECTED)
	    		   {
	    			   ModelSceneCrossSectionMode mode = (ModelSceneCrossSectionMode)modelScenePlaneCombo.getSelectedItem();
	    			   int modeOrdinal = mode.ordinal();
	    			   if(mode == ModelSceneCrossSectionMode.DISABLED)
	    			   {
	    				   TissueCrossSectionPortrayal3D.setTissueCrossSectionDirty();
	    				   modelScenePlaneSlider.setEnabled(false);
	    				   modelScenePlaneSliderLabel.setEnabled(false);
	    				   modelScenePlaneSliderLabel2.setEnabled(false);
	    				   modelClip.setEnables(new boolean[]{false, false, false, false, false, false});
	    			   }
	    			   else
	    			   {
	    				   modelScenePlaneSlider.setEnabled(true);
	    				   modelScenePlaneSliderLabel.setEnabled(true);
	    				   modelScenePlaneSliderLabel2.setEnabled(true);
							
	    				   if(modelSceneCrossSectionMode != ModelSceneCrossSectionMode.DISABLED)
	    				   {
	    					   modelClip.setEnable(modelSceneCrossSectionMode.ordinal()-1, false);
	    				   }
	    				   
	    				   if(mode != ModelSceneCrossSectionMode.DISABLED)
	    				   {								
	    					   modelClip.setEnable(mode.ordinal()-1, true);
	    				   }
	    				   
	    				   Vector4d planePosition = new Vector4d();
	    				   modelClip.getPlane(modeOrdinal-1, planePosition);
	    				   double result = 0;
	    				   double fact =  ((double) modelScenePlaneSlider.getValue())/100d;

	    				   if(mode == ModelSceneCrossSectionMode.X_Y_PLANE || mode == ModelSceneCrossSectionMode.DISABLED)
	    				   {
	    					   double length = TissueController.getInstance().getTissueBorder().getLengthInMikron();
	    					   result =-1*length*fact;
	    					   actModelSceneCrossSectionCoordinate = -1*result;
	    					   TissueCrossSectionPortrayal3D.setTissueCrossSectionDirty();
	    				   }
	    				   else if(mode == ModelSceneCrossSectionMode.X_Z_PLANE)
	    				   {
	    					   double height = TissueController.getInstance().getTissueBorder().getHeightInMikron();
	    					   result =-1*height*fact;
	    					   actModelSceneCrossSectionCoordinate = -1*result;
	    					   TissueCrossSectionPortrayal3D.setTissueCrossSectionDirty();
	    				   }
	    				   else if(mode == ModelSceneCrossSectionMode.Y_Z_PLANE)
	    				   {
	    					   double width = TissueController.getInstance().getTissueBorder().getWidthInMikron();
	    					   result =-1*width*fact;
	    					   actModelSceneCrossSectionCoordinate = -1*result;
	    					   TissueCrossSectionPortrayal3D.setTissueCrossSectionDirty();
	    				   }
							
	    				   planePosition.w = result;
	    				   modelScenePlaneSliderLabel.setText(Math.round(-1*result) + " µm");
	    				   modelClip.setPlane(modeOrdinal-1, planePosition);										
	    				   modelClip.setEnable(modeOrdinal-1, true);
	    			   }
	    			   modelSceneCrossSectionMode = (ModelSceneCrossSectionMode)modelScenePlaneCombo.getSelectedItem();
	    			   SwingUtilities.invokeLater(new Runnable(){ public void run(){ updateSceneGraph(true);}});
	    		   }
	    	   }
	       });
	       
	       modelScenePlaneComboPanel.add(modelScenePlaneCombo, BorderLayout.CENTER);
	      
	       JPanel modelScenePlaneSilderPanel = new JPanel(new BorderLayout(10,10));
	       modelScenePlaneSlider = new JSlider(JSlider.HORIZONTAL,0,100,100);
	       modelScenePlaneSlider.setMajorTickSpacing(1);
	       modelScenePlaneSlider.setMinorTickSpacing(1);      
	       modelScenePlaneSlider.setPaintLabels(false);
	       modelScenePlaneSlider.setEnabled(false);
	       modelScenePlaneSliderLabel = new JLabel(TissueController.getInstance().getTissueBorder().getLengthInMikron()+ " µm");
	       modelScenePlaneSliderLabel.setEnabled(false);	      
	       modelScenePlaneSliderLabel2.setEnabled(false);
	       
	       modelScenePlaneSlider.addChangeListener(new ChangeListener()
	       {
	    	   public void stateChanged(ChangeEvent e) 
	    	   {
	    		   if(lastModelScenePlaneSliderPosition != modelScenePlaneSlider.getValue())
	    		   {
	    			   lastModelScenePlaneSliderPosition = modelScenePlaneSlider.getValue();
	    			   double fact =  ((double) modelScenePlaneSlider.getValue())/100d;
	    			   ModelSceneCrossSectionMode selectedComboItem = (ModelSceneCrossSectionMode) modelScenePlaneCombo.getSelectedItem();
	    			   double result = 0;

	    			   if(selectedComboItem == ModelSceneCrossSectionMode.X_Y_PLANE)
	    			   {
	    				   double length = TissueController.getInstance().getTissueBorder().getLengthInMikron();
	    				   result =-1*length*fact;
	    				   TissueCrossSectionPortrayal3D.setTissueCrossSectionDirty();
	    			   }
	    			   else if(selectedComboItem == ModelSceneCrossSectionMode.X_Z_PLANE)
	    			   {
	    				   double height = TissueController.getInstance().getTissueBorder().getHeightInMikron();
	    				   result =-1*height*fact;
	    				   TissueCrossSectionPortrayal3D.setTissueCrossSectionDirty();
	    			   }
	    			   else if(selectedComboItem == ModelSceneCrossSectionMode.Y_Z_PLANE)
	    			   {
	    				   double width = TissueController.getInstance().getTissueBorder().getWidthInMikron();
	    				   result =-1*width*fact;
	    				   TissueCrossSectionPortrayal3D.setTissueCrossSectionDirty();
	    			   }
	    			   
	    			   Vector4d planePosition = new Vector4d();
	    			   modelClip.getPlane(selectedComboItem.ordinal()-1, planePosition);
	    			   planePosition.w = result;
	    			   actModelSceneCrossSectionCoordinate = -1*result;
	    			   modelClip.setPlane(selectedComboItem.ordinal()-1, planePosition);
	    			   modelScenePlaneSliderLabel.setText(Math.round(-1*result) + " µm");
	    			   SwingUtilities.invokeLater(new Runnable(){ public void run(){ updateSceneGraph(true);}});
	    		   }
	    	   }
	       });
	       
	       modelScenePlaneSilderPanel.add(modelScenePlaneSliderLabel2, BorderLayout.WEST);
	       modelScenePlaneSilderPanel.add(modelScenePlaneSlider, BorderLayout.CENTER);
	       modelScenePlaneSilderPanel.add(modelScenePlaneSliderLabel, BorderLayout.EAST);
	       JPanel modelSceneOpacitySilderPanel = null;
		 
	       modelSceneOpacitySilderPanel = new JPanel(new BorderLayout(10,10));
	       modelSceneOpacitySlider = new JSlider(JSlider.HORIZONTAL,0,100,100);
	       modelSceneOpacitySlider.setMajorTickSpacing(1);
	       modelSceneOpacitySlider.setMinorTickSpacing(1);      
	       modelSceneOpacitySlider.setPaintLabels(false);       
	       modelSceneOpacitySliderLabel = new JLabel(modelSceneOpacitySlider.getValue()+ "%");
	       
	       modelSceneOpacitySlider.addChangeListener(new ChangeListener()
	       {
	    	   public void stateChanged(ChangeEvent e)
	    	   {
	    		   if(lastDiffFieldOpacitySliderPosition != modelSceneOpacitySlider.getValue())
	    		   {
	    			   lastDiffFieldOpacitySliderPosition = modelSceneOpacitySlider.getValue();
	    			   modelSceneOpacity= ((double) modelSceneOpacitySlider.getValue())/100d;
	    			   modelSceneOpacitySliderLabel.setText(modelSceneOpacitySlider.getValue() + "%");
	    			   SwingUtilities.invokeLater(new Runnable(){ public void run(){ updateSceneGraph(false);}});
	    		   }
	    	   }
	       });
	       
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
	       showAmbientLightCheckBox.setSelected(optimizedGraphicsActivated);
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
	       optionsPanel.add(resetAndAnimationBox);
	       //optionsPanel.add(viewPanel);
	       
	       JScrollPane scroll = new JScrollPane(optionsPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	       getContentPane().add(scroll);                  
                   
	       // add preferences  
	       pack();
	       setIconImage(new ImageIcon(ImageLoader.class.getResource("icon.gif")).getImage());
	       centerMe(this);
	       setResizable(true);
	   	} 
   	
	   	public void setVisible(boolean val)
	   	{
	   		if(this.diffCrossectionPanel != null)
	   		{
	   			if(ModelController.getInstance().getExtraCellularDiffusionController().getNumberOfFields() > 0)
	   			{
	   				diffCrossectionPanel.setVisible(true);
	   			}
	   			else
	   			{
	   				diffCrossectionPanel.setVisible(false);
	   			}
	   		}
	   		super.setVisible(val);
	   	}
	
	   	private void centerMe(JDialog frame)
	   	{
			if(frame != null)
			{
				Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
				frame.setLocation(((int)((screenDim.getWidth()/2)-(frame.getPreferredSize().getWidth()/2))), 
				((int)((screenDim.getHeight()/2)-(frame.getPreferredSize().getHeight()/2))));
			}
		}
	   	
	   	private long lastTimeStamp = System.currentTimeMillis();
	   	private long lastSimStep = -1;
	   	boolean spinningEnabled = false;
	   	
	   	public void updateDisplayRotationSpeed(SimState state)
	   	{
	   		/*	 long mSecsPerRot = 0;
			if(enableRotationPerSimStep.isSelected()){
				if(lastSimStep > state.schedule.getSteps()){
		   			 lastTimeStamp = System.currentTimeMillis();
		   		 }
		   		 lastSimStep = state.schedule.getSteps();
		   		 long currentTimeStamp = System.currentTimeMillis();
		   		 long timePerSimStep = currentTimeStamp - lastTimeStamp;
		   		 lastTimeStamp = currentTimeStamp;
		   		 timePerSimStep= timePerSimStep <=0 ? 1: timePerSimStep;
		   		    		 
		   		 if(rotationPerSimStep == 0 ||
		  	           (rotAxis_X.getValue() == 0 && rotAxis_Y.getValue() == 0 && rotAxis_Z.getValue()==0)){
		   			 setSpinningEnabled(false);
		   			 spinningEnabled=false;
		   		 }
		   		 else{
		   			 mSecsPerRot =(long)(timePerSimStep/rotationPerSimStep);
		   			 autoSpin.getAlpha().setIncreasingAlphaDuration(mSecsPerRot);
		   	       // spin background too
		   	       autoSpinBackground.getAlpha().setIncreasingAlphaDuration(mSecsPerRot);   	       
		   			 if(!spinningEnabled)setSpinningEnabled(true);
		   			 spinningEnabled=true;
		   		 }
		   	 }
		   	 else{
		   		 
		   		 double value = spinDuration.getValue();
			   	 mSecsPerRot = (value == 0 ? 1  : (long)(1000 / value));	       
			       autoSpin.getAlpha().setIncreasingAlphaDuration(mSecsPerRot);
			       // spin background too
			       autoSpinBackground.getAlpha().setIncreasingAlphaDuration(mSecsPerRot);
			       if (value == 0 ||
			           (rotAxis_X.getValue() == 0 && rotAxis_Y.getValue() == 0 && rotAxis_Z.getValue()==0))
			           setSpinningEnabled(false);
			       else setSpinningEnabled(true);
		   	 }*/
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

	public boolean isAutomatedPNGSnapshotsEnabled() 
	{
		return automatedPNGSnapshotsEnabled;
	}
}