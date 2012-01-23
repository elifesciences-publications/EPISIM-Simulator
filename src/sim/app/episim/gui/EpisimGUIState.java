package sim.app.episim.gui;

import sim.SimStateServer;
import sim.engine.*;
import sim.field.grid.SparseGrid3D;
import sim.app.episim.EpisimProperties;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.UniversalCell;
import sim.app.episim.CellInspector;
import sim.app.episim.SimulationStateChangeListener;
import sim.app.episim.datamonitoring.charts.ChartController;
import sim.app.episim.datamonitoring.charts.ChartSetChangeListener;
import sim.app.episim.datamonitoring.charts.DefaultCharts;


import sim.app.episim.model.controller.CellBehavioralModelController;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters;
import sim.app.episim.model.visualization.EpisimDrawInfo;
import sim.app.episim.model.visualization.UniversalCellPortrayal2D;
import sim.app.episim.tissue.Epidermis;
import sim.app.episim.tissue.TissueBorder;
import sim.app.episim.tissue.TissueController;
import sim.app.episim.tissue.TissueServer;
import sim.app.episim.tissue.TissueType;
import sim.app.episim.visualization.BasementMembranePortrayal2D;
import sim.app.episim.visualization.EpisimSimulationBoxPortrayal3D;
import sim.app.episim.visualization.GridPortrayal2D;
import sim.app.episim.visualization.RulerPortrayal2D;
import sim.app.episim.visualization.WoundPortrayal2D;
import sim.display.*;
import sim.portrayal.continuous.*;
import sim.portrayal.grid.ObjectGridPortrayal2D;
import sim.portrayal3d.FieldPortrayal3D;
import sim.portrayal3d.grid.ObjectGridPortrayal3D;
import sim.portrayal3d.grid.SparseGridPortrayal3D;
import sim.portrayal3d.simple.WireFrameBoxPortrayal3D;
import sim.portrayal.*;
import sim.util.Double2D;

import javax.swing.*;

import java.awt.*;


import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;


import javax.swing.JFrame;
import javax.swing.plaf.basic.BasicInternalFrameUI;

import org.jfree.chart.*; // ChartPanel;

import episiminterfaces.EpisimBiomechanicalModelGlobalParameters.ModelDimensionality;
import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.EpisimPortrayal;
import episiminterfaces.EpisimSimulationDisplay;


public class EpisimGUIState extends GUIState implements ChartSetChangeListener{

	private EpisimDisplay2D display2D;
	
	private EpisimDisplay3D display3D;

	private JInternalFrame displayFrame;

	private Component mainComponent;
	

	private final int INTERNALFRAMECOLS = 3;

	private final String SIMULATIONFRAME = "Simframe";
	private final String CONTROLLERFRAME = "controllerFrame";
	private final String CHARTFRAME = "chartFrame";
	
	private JDesktopPane desktop;

	private EpisimConsole console;

	public final double INITIALZOOMFACTOR;
	public final double EPIDISPLAYWIDTH;
	public final double EPIDISPLAYHEIGHT;
	
	public  final int DISPLAY_BORDER_TOP = 60;
	public  final int DISPLAY_BORDER_LEFT = 45;
	public  final int DISPLAY_BORDER_BOTTOM = 40;
	public  final int DISPLAY_BORDER_RIGHT = 60;
	private final double MAXHEIGHTFACTOR = 1;
	
	private boolean workaroundPauseWasPressed = false;	
	private boolean pausedBecauseOfMainFrameResize = false;
	
	private final int STATUSBARHEIGHT = 25;	
	public static final double EPIDISPLAYSTANDARDWIDTH = 800;
	public static final double EPIDISPLAYSTANDARDHEIGHT = 500;
	
	
	private final int DEFAULTCHARTWIDTH = 400;
	private final int DEFAULTCHARTHEIGHT = 350;
	
	
	
	private boolean resizeButtonIsActionSource = false;
	
	
	
	private boolean activateDrawing = false;
	
	private ArrayList<SimulationStateChangeListener> simulationStateListeners;
	
	private boolean autoArrangeWindows = true;	
	
	FieldPortrayal2D cellPortrayal2D;
	FieldPortrayal3D cellPortrayal3D;
	BasementMembranePortrayal2D basementPortrayal;
	WoundPortrayal2D woundPortrayal;
	RulerPortrayal2D rulerPortrayal;
	GridPortrayal2D gridPortrayal;	
	
	public EpisimGUIState(JFrame mainFrame){			
		this(new Epidermis(System.currentTimeMillis()), mainFrame, false);
	}
	public EpisimGUIState(JPanel mainPanel){	
		this(new Epidermis(System.currentTimeMillis()), (Component)mainPanel, false);
	}

	public EpisimGUIState(SimState state, JPanel mainPanel, boolean reloadSnapshot){
		this(new Epidermis(System.currentTimeMillis()), (Component)mainPanel, reloadSnapshot);
	}	
	public EpisimGUIState(SimState state, Component mainComp, boolean reloadSnapshot){		
		super(state);
		
		double zoomFactorHeight = EPIDISPLAYSTANDARDHEIGHT / TissueController.getInstance().getTissueBorder().getHeightInPixels();
		double zoomFactorWidth = EPIDISPLAYSTANDARDWIDTH / TissueController.getInstance().getTissueBorder().getWidthInPixels();
		
		INITIALZOOMFACTOR = zoomFactorWidth < zoomFactorHeight ? zoomFactorWidth : zoomFactorHeight;
		
		EPIDISPLAYWIDTH = TissueController.getInstance().getTissueBorder().getWidthInPixels() * INITIALZOOMFACTOR;
		EPIDISPLAYHEIGHT = TissueController.getInstance().getTissueBorder().getHeightInPixels() * INITIALZOOMFACTOR;
		SimStateServer.getInstance().setEpisimGUIState(this);
		if(state instanceof TissueType) TissueController.getInstance().registerTissue(((TissueType) state));
		simulationStateListeners = new ArrayList<SimulationStateChangeListener>();
		ChartController.getInstance().registerChartSetChangeListener(this);
		this.mainComponent = mainComp;		
		this.setConsole(new EpisimConsole(this, reloadSnapshot));		
	}
	
	
	
	public Component getMainGUIComponent(){
		return this.mainComponent;
	}
	
	
	public Inspector getCellBehavioralModelInspector() {
		EpisimCellBehavioralModelGlobalParameters cbmModel = ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters();
		if(cbmModel == null)
			return null;
		Inspector i = new EpisimSimpleInspector(cbmModel, this);
		i.setVolatile(false);
		return i;
	}

	public Inspector getBiomechnicalModelInspector() {

		EpisimBiomechanicalModelGlobalParameters mechModel = ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		if(mechModel == null)
			return null;
		Inspector i = new EpisimSimpleInspector(mechModel, this);
		i.setVolatile(false);
		return i;
	}
	
	public Inspector getMiscalleneousInspector() {

		Object miscalleneous = MiscalleneousGlobalParameters.instance();
		if(miscalleneous == null)
			return null;
		Inspector i = new EpisimSimpleInspector(miscalleneous, this);
		i.setVolatile(false);
		return i;
	}
	

	public void setConsole(EpisimConsole cons) {

		console = cons;

		final JInternalFrame controllerFrame = new JInternalFrame("Tissue Simulation Controller", true, false, true, true);

		controllerFrame.setContentPane(cons.getControllerContainer());
		controllerFrame.setResizable(true);
		controllerFrame.setVisible(true);
		controllerFrame.setName(CONTROLLERFRAME);
		controllerFrame.setFrameIcon(null);
		desktop.add(controllerFrame);
		arrangeElements(desktop, false);
		
		
		console.addActionListenersAndResetButtons();

	}
	
	public void closeConsole(){
		console.doClose();
		if(mainComponent instanceof JFrame)controller.unregisterFrame((JFrame)mainComponent); // unregister previous frame
	}
	
	
	
	
	public static String getName() {

		return "Tissue Simulation - Controller";
	}

	public void start() {

		super.start();
		setupPortrayals();
		

	}
	
	public void setMaxSimulationSteps(long steps){
		if(console != null) console.setWhenShouldEnd(steps);
	}
	
	public void startSimulation(){
		if(console != null) console.pressPlay();
	}

	public void load(SimState state) {

		super.load(state);		
		setupPortrayals();
	}
	
	

	/*
	 * public static Image loadImage(String filename) { return new
	 * ImageIcon(BackImageClass.class.getResource(filename)).getImage(); }
	 */
	public void setupPortrayals() {
		if(ModelController.getInstance().getModelDimensionality()== ModelDimensionality.TWO_DIMENSIONAL){
			setupPortrayals2D();
		}
		if(ModelController.getInstance().getModelDimensionality()== ModelDimensionality.THREE_DIMENSIONAL){
			setupPortrayals3D();
		}	
	}
	
	
	private void setupPortrayals2D(){
	
		// obstacle portrayal needs no setup
		
		basementPortrayal = new BasementMembranePortrayal2D();
		woundPortrayal = new WoundPortrayal2D();
		rulerPortrayal = new RulerPortrayal2D();
		gridPortrayal = new GridPortrayal2D();
		
		
		
		
		 EpisimPortrayal cellPortrayal = ModelController.getInstance().getCellPortrayal();
		
		
		
	
		 cellPortrayal2D = (FieldPortrayal2D)cellPortrayal;
		
		
		
		
		display2D.detatchAll();
		
		display2D.attach(basementPortrayal, basementPortrayal.getPortrayalName(), basementPortrayal.getViewPortRectangle(), true);
		EpisimPortrayal[] portrayals = ModelController.getInstance().getAdditionalPortrayalsCellBackground();
		for(int i = 0; i < portrayals.length; i++) display2D.attach((FieldPortrayal2D)portrayals[i], portrayals[i].getPortrayalName(), portrayals[i].getViewPortRectangle(), true);
		display2D.attach(cellPortrayal2D, cellPortrayal.getPortrayalName(), cellPortrayal.getViewPortRectangle(), true);
		portrayals = ModelController.getInstance().getAdditionalPortrayalsCellForeground();
		for(int i = 0; i < portrayals.length; i++) display2D.attach((FieldPortrayal2D)portrayals[i], portrayals[i].getPortrayalName(), portrayals[i].getViewPortRectangle(), true);
		portrayals = ModelController.getInstance().getExtraCellularDiffusionPortrayals();
		for(int i = 0; i < portrayals.length; i++) display2D.attach((FieldPortrayal2D)portrayals[i], portrayals[i].getPortrayalName(), portrayals[i].getViewPortRectangle(), false);
		
		
		display2D.attach(woundPortrayal, woundPortrayal.getPortrayalName(), woundPortrayal.getViewPortRectangle(), true);
		display2D.attach(rulerPortrayal, rulerPortrayal.getPortrayalName(), rulerPortrayal.getViewPortRectangle(), true);
		display2D.attach(gridPortrayal, gridPortrayal.getPortrayalName(), gridPortrayal.getViewPortRectangle(), true);
		
		
		// reschedule the displayer
		display2D.reset();

		// redraw the display
		display2D.repaint();
	}
	
	private void setupPortrayals3D(){
		
		
		EpisimPortrayal cellPortrayal = ModelController.getInstance().getCellPortrayal();
		
		
		
		
		cellPortrayal3D = (FieldPortrayal3D) cellPortrayal;
		
		
		display3D.detatchAll();
		
		double height = TissueController.getInstance().getTissueBorder().getHeightInMikron();
		double width =  TissueController.getInstance().getTissueBorder().getWidthInMikron();
		double length =  TissueController.getInstance().getTissueBorder().getLengthInMikron();
		display3D.attach( new EpisimSimulationBoxPortrayal3D(0,0,0, width, height, length), "Simulation Box");
		
		
		
		EpisimPortrayal[] portrayals = ModelController.getInstance().getAdditionalPortrayalsCellBackground();
		for(int i = 0; i < portrayals.length; i++)display3D.attach((FieldPortrayal3D)portrayals[i], portrayals[i].getPortrayalName(), portrayals[i].getViewPortRectangle(), true);
		
		display3D.attach(cellPortrayal3D, cellPortrayal.getPortrayalName(), cellPortrayal.getViewPortRectangle(), true);
		portrayals = ModelController.getInstance().getAdditionalPortrayalsCellForeground();
		for(int i = 0; i < portrayals.length; i++) display3D.attach((FieldPortrayal3D)portrayals[i], portrayals[i].getPortrayalName(), portrayals[i].getViewPortRectangle(), true);
		portrayals = ModelController.getInstance().getExtraCellularDiffusionPortrayals();
		for(int i = 0; i < portrayals.length; i++) display3D.attach((FieldPortrayal3D)portrayals[i], portrayals[i].getPortrayalName(), portrayals[i].getViewPortRectangle(), false);
		
	
	// reschedule the displayer
      display3D.reset();
              
      // redraw the display
      display3D.createSceneGraph();
	}
	
	
	
	
	
	private JInternalFrame buildEpisimDisplay2D(){
		display2D = new EpisimDisplay2D(EPIDISPLAYWIDTH+(DISPLAY_BORDER_LEFT+DISPLAY_BORDER_RIGHT), EPIDISPLAYHEIGHT+(DISPLAY_BORDER_TOP+DISPLAY_BORDER_BOTTOM), this);
		//display.setClipping(false);
		Color myBack = new Color(0xE0, 0xCB, 0xF6);
		display2D.setBackdrop(Color.BLACK);		
		display2D.getInsideDisplay().addMouseListener(new MouseAdapter(){			
			
			
				public void mouseClicked(MouseEvent e){
					if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2){
						if(console.getPlayState() != Console.PS_PAUSED && console.getPlayState() == Console.PS_PLAYING)console.pressPause();
					}
				}
			
				public void mousePressed(MouseEvent e) {
	
					if(e.getButton() == MouseEvent.BUTTON3){
						if(console.getPlayState() != Console.PS_PAUSED && console.getPlayState() == Console.PS_PLAYING)console.pressPause();
						if(woundPortrayal != null){
							woundPortrayal.clearWoundRegionCoordinates();
							woundPortrayal.closeWoundRegionPath(false);
						}
						activateDrawing = true;
					}
					
				}
				public void mouseReleased(MouseEvent e) {
	
					if(e.getButton() == MouseEvent.BUTTON3){
					//	if(console.getPlayState() == Console.PS_PAUSED)console.pressPause();
						if(woundPortrayal != null){
							woundPortrayal.closeWoundRegionPath(true);
							((Epidermis) state).removeCells(woundPortrayal.getWoundRegion());
							activateDrawing = false;
						}
					}
					
				}
	
				public void mouseEntered(MouseEvent e){
					if(rulerPortrayal != null && display2D.isPortrayalVisible(rulerPortrayal.getPortrayalName())){
						
							rulerPortrayal.setCrosshairsVisible(true);
							rulerPortrayal.setActMousePosition(new Point2D.Double(e.getX(), e.getY()));
						
					}
				}
				public void mouseExited(MouseEvent e){
					if(rulerPortrayal != null && display2D.isPortrayalVisible(rulerPortrayal.getPortrayalName())) rulerPortrayal.setCrosshairsVisible(false);
				}

			});
		display2D.getInsideDisplay().addMouseMotionListener(new MouseMotionAdapter(){
			public void mouseDragged(MouseEvent e){
				
				if(activateDrawing){
					if(woundPortrayal != null){
						woundPortrayal.addMouseCoordinate(new Double2D(e.getX(), e.getY()));
						redrawDisplayForDrawing2DWoundingArea();
					}
				}
				if(rulerPortrayal != null && display2D.isPortrayalVisible(rulerPortrayal.getPortrayalName())) rulerPortrayal.setActMousePosition(new Point2D.Double(e.getX(), e.getY()));
				if(rulerPortrayal != null && display2D.isPortrayalVisible(rulerPortrayal.getPortrayalName())&& (console.getPlayState()==Console.PS_PAUSED
						||console.getPlayState()==Console.PS_STOPPED) && ModelController.getInstance().isSimulationStartedOnce()) redrawDisplayForDrawing2DWoundingArea();	
				
			}
			public void mouseMoved(MouseEvent e){
				if(rulerPortrayal != null && display2D.isPortrayalVisible(rulerPortrayal.getPortrayalName())) rulerPortrayal.setActMousePosition(new Point2D.Double(e.getX(), e.getY()));
				if(rulerPortrayal != null && display2D.isPortrayalVisible(rulerPortrayal.getPortrayalName())&&(console.getPlayState()==Console.PS_PAUSED
						|| console.getPlayState()==Console.PS_STOPPED) && ModelController.getInstance().isSimulationStartedOnce()) redrawDisplayForDrawing2DWoundingArea();			
			} 
		});
		return display2D.createInternalFrame();
	}
	
	private JInternalFrame buildEpisimDisplay3D(){
		display3D = new EpisimDisplay3D(EPIDISPLAYWIDTH, EPIDISPLAYHEIGHT, this);
		
		//display.setClipping(false);
		Color myBack = new Color(0xE0, 0xCB, 0xF6);
		display3D.setBackdrop(Color.BLACK);
		
		display3D.getInsideDisplay().addMouseListener(new MouseAdapter(){			
			public void mouseClicked(MouseEvent e){
				if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2){
					if(console.getPlayState() != Console.PS_PAUSED && console.getPlayState() == Console.PS_PLAYING)console.pressPause();
				}
			}
		});
		
		double height = TissueController.getInstance().getTissueBorder().getHeightInMikron();
		double width =  TissueController.getInstance().getTissueBorder().getWidthInMikron();
		double length = TissueController.getInstance().getTissueBorder().getLengthInMikron();
		
		display3D.translate(-.5*width,-.5*height,-0.5*length);
		double initialScale = 0.5/Math.max(width, Math.max(height, length));
		display3D.setInitialDisplayScale(initialScale);
		display3D.scale(initialScale);
       
      
		return display3D.createInternalFrame();
	}
	
	
	
	
	void addInternalFrames(Controller c) {

		desktop = new JDesktopPane();
		
		desktop.setBackground(Color.LIGHT_GRAY);
		// --------------------------------------------------------------------------
		// Internal Frame for Simulation Display
		// --------------------------------------------------------------------------

		
		

		if(ModelController.getInstance().getModelDimensionality()== ModelDimensionality.TWO_DIMENSIONAL){
			displayFrame = buildEpisimDisplay2D();
		}
		if(ModelController.getInstance().getModelDimensionality()== ModelDimensionality.THREE_DIMENSIONAL){
			displayFrame = buildEpisimDisplay3D();
		}
		
		
		maximizeWorkaround(displayFrame);
		
	
		
	//-----------------------------------------------------------------------------------------------------------------	
	//	Necessary for Maximize Workaround
	//-----------------------------------------------------------------------------------------------------------------	
		
		mainComponent.addComponentListener(new ComponentAdapter(){
			public void componentResized(ComponentEvent e) {
	       if(console != null && console.getPlayState() == Console.PS_PLAYING){		      
		      	 console.pressPause();
		      	 pausedBecauseOfMainFrameResize = true;
		      	Thread t = new Thread(new Runnable(){

						public void run() {
	                  try{
	                     Thread.sleep(500);
	                     checkForRestartAfterMainFrameResize();
                     }
                     catch (InterruptedException e){
	                    ExceptionDisplayer.getInstance().displayException(e);
                     }	                  
                  }});
		      	t.start();
	       }
	       else if(console != null && console.getPlayState() == Console.PS_PAUSED){
	      	 if(SimStateServer.getInstance().getEpisimSimulationState() == SimStateServer.EpisimSimulationState.STEPWISE){
	      		 SimStateServer.getInstance().setSimStatetoPause();
	      		 mainComponent.validate();
	      		 mainComponent.repaint();
					
	      	 }
			 }
         }
		});

		mainComponent.addMouseMotionListener(new MouseMotionAdapter(){ public void mouseMoved(MouseEvent e) {checkForRestartAfterMainFrameResize();}});
		desktop.addMouseMotionListener(new MouseMotionAdapter(){	public void mouseMoved(MouseEvent e) {checkForRestartAfterMainFrameResize(); }});
		mainComponent.addMouseListener(new MouseAdapter(){
			public void mouseEntered(MouseEvent e) { checkForRestartAfterMainFrameResize(); }
			public void mouseExited(MouseEvent e) { checkForRestartAfterMainFrameResize(); }
		});
		desktop.addMouseListener(new MouseAdapter(){
			public void mouseEntered(MouseEvent e) { checkForRestartAfterMainFrameResize(); }
			public void mouseExited(MouseEvent e) { checkForRestartAfterMainFrameResize(); }
		});
		
		
		
		
		displayFrame.addComponentListener(new ComponentAdapter(){
			 public void componentResized (ComponentEvent e) 
          {
    	      if(console != null && console.getPlayState() == Console.PS_PAUSED && resizeButtonIsActionSource){
					 console.pressPause();
					 resizeButtonIsActionSource = false;
				 }
    	     else if(console != null && console.getPlayState() == Console.PS_PAUSED){
    	   	 if(SimStateServer.getInstance().getEpisimSimulationState() == SimStateServer.EpisimSimulationState.STEPWISE){
    	   		SimStateServer.getInstance().setSimStatetoPause();
    	   		 displayFrame.validate();
    	   		displayFrame.repaint();
					
	      	 }
				}
		    }
		});
		
		//-------------------------------------------------------------------------------------------------------------------------------------------------------------
		displayFrame.setTitle("Tissue Visualization");
		displayFrame.setName(SIMULATIONFRAME);
		displayFrame.setMaximizable(true);
		displayFrame.setIconifiable(true);
		displayFrame.setResizable(true);
		displayFrame.setVisible(true);
		displayFrame.setFrameIcon(null);
		
		desktop.add(displayFrame);

		
		
		
		desktop.putClientProperty("JDesktopPane.dragMode", "outline");
		
		
		
	
		final JScrollPane desktopScroll = new JScrollPane(desktop);
		
		
		if(mainComponent instanceof JFrame){
			desktop.setPreferredSize(new Dimension(
				(int)(((JFrame)mainComponent).getContentPane().getWidth()-desktopScroll.getVerticalScrollBar().getPreferredSize().getWidth()),
				(int)(((JFrame)mainComponent).getContentPane().getHeight()-desktopScroll.getHorizontalScrollBar().getPreferredSize().getHeight()-STATUSBARHEIGHT)));
	   
		}
		else if(mainComponent instanceof JPanel){
			desktop.setPreferredSize(new Dimension(
					(int)(((JPanel)mainComponent).getWidth()-desktopScroll.getVerticalScrollBar().getPreferredSize().getWidth()),
					(int)(((JPanel)mainComponent).getHeight()-desktopScroll.getHorizontalScrollBar().getPreferredSize().getHeight()-STATUSBARHEIGHT)));
		}
		
		desktop.setSize(desktop.getPreferredSize());
		
		arrangeElements(desktop, false);
		if(mainComponent instanceof JFrame){
			((JFrame)mainComponent).getContentPane().add(desktopScroll, BorderLayout.CENTER);		
		}
		else if(mainComponent instanceof JPanel){
			((JPanel)mainComponent).add(desktopScroll, BorderLayout.CENTER);
		}
				
		if(mainComponent instanceof JFrame) ((JFrame)mainComponent).pack();
		
		mainComponent.addComponentListener(new ComponentAdapter(){
			public void componentResized(ComponentEvent comp) {
				
							
				if(comp.getSource() instanceof JFrame){
					
					if(desktop != null){ 
					
						
						if(mainComponent instanceof JFrame){
							desktop.setPreferredSize(new Dimension(
										(int)(((JFrame)mainComponent).getContentPane().getWidth()-desktopScroll.getVerticalScrollBar().getPreferredSize().getWidth()),
										(int)(((JFrame)mainComponent).getContentPane().getHeight()-desktopScroll.getHorizontalScrollBar().getPreferredSize().getHeight())));
						}
						else if(mainComponent instanceof JPanel){
							desktop.setPreferredSize(new Dimension(
									(int)(((JPanel)mainComponent).getWidth()-desktopScroll.getVerticalScrollBar().getPreferredSize().getWidth()),
									(int)(((JPanel)mainComponent).getHeight()-desktopScroll.getHorizontalScrollBar().getPreferredSize().getHeight())));
					}
						if(autoArrangeWindows)arrangeElements(desktop, true);
					}
			

				}
				
				
			}
		});		
		registerInternalFrames(desktop, console);
	
	}
	
	private void checkForRestartAfterMainFrameResize(){
		if(console != null && console.getPlayState() == Console.PS_PAUSED && pausedBecauseOfMainFrameResize){
		     
     	 console.pressPause();
     	 pausedBecauseOfMainFrameResize = false;
		 }
	}
	
	private void registerInternalFrames(JDesktopPane desktop, EpisimConsole c){
		Component[] comps = desktop.getComponents();
		for(Component comp: comps){
			if(comp instanceof JInternalFrame &&
					((JInternalFrame) comp).getName().equals(CHARTFRAME)){
				c.registerFrame(((JInternalFrame)comp));
			}
		}
	}
	
	private void redrawDisplayForDrawing2DWoundingArea(){
		if(display2D.getInsideDisplay().getWidth() > 0 && display2D.getInsideDisplay().getHeight() > 0){	
	  	 Graphics g = display2D.getInsideDisplay().getGraphics();
	  	 display2D.paintComponentInInnerDisplay(g,true);
	  	 g.dispose();
		}
	}

	public void init(Controller c) {

		super.init(c);
		
		addInternalFrames(c);
	}

	public void quit() {
		console.pressStop();
		if(display3D != null) display3D.stopRenderer();
		super.quit();
			
		if(displayFrame != null)
			displayFrame.dispose();
		displayFrame = null;
		
		desktop.removeAll();
		desktop.validate();
		
		display2D = null;
		display3D = null;
	}

	private JInternalFrame getChartInternalFrame(JPanel chartPanel, String title) {

		JInternalFrame chartFrame = new JInternalFrame(title, true, false, true, true);
		chartFrame.setResizable(true);
		chartFrame.setIconifiable(false);
		chartFrame.getContentPane().setLayout(new BorderLayout());
		chartFrame.getContentPane().add(chartPanel, BorderLayout.CENTER);
		chartFrame.setName(CHARTFRAME);
		
		chartFrame.setFrameIcon(null);
		chartFrame.setVisible(true);
		
		return chartFrame;
	}

	/**
	 * Ordnet die Internal Frames in der übergebenen Komponente an
	 * 
	 * @param comp
	 *           Komponente in der die Fenster angeordnet werden sollen
	 */
	private void arrangeElements(JComponent comp, boolean resizeArrangement) {
			
		Dimension mainFrameDim; 
		mainFrameDim= comp.getPreferredSize();
		if(resizeArrangement){
			mainFrameDim.height = mainFrameDim.height - STATUSBARHEIGHT;
			comp.setPreferredSize(mainFrameDim);
		}
		
		
		
		int corrNumber = comp.getComponentCount() - 2;
		int remainder = 0;
		
		int numberOfColumns =1;
		
		if(corrNumber > 0 && corrNumber <= 2) numberOfColumns = 2;
		else if(corrNumber > 2) numberOfColumns = INTERNALFRAMECOLS;
		if((remainder = corrNumber % numberOfColumns) != 0)
			corrNumber += (numberOfColumns - remainder);
		int framesPerCol =2; 
		if(numberOfColumns >= 2)framesPerCol =  corrNumber / (numberOfColumns - 1);

		int xDeltaSim = 0; 
		if(numberOfColumns == 1)xDeltaSim= ((int) mainFrameDim.getWidth());
		if(numberOfColumns == 2)xDeltaSim= ((int) (mainFrameDim.getWidth()*2) / 3);
		if(numberOfColumns > 2)xDeltaSim= ((int) mainFrameDim.getWidth()) / 2;

		int yDeltaSim = ((int) mainFrameDim.getHeight())*3 / 4;
		int xDeltaChart = 0;
		if(((int) mainFrameDim.getWidth()  - xDeltaSim) > 0 && numberOfColumns >1)
			xDeltaChart= ((int) mainFrameDim.getWidth() - xDeltaSim) / (numberOfColumns-1);
		int yDeltaChart = 0; 
		if(framesPerCol > 0 && ((int) mainFrameDim.getHeight())>0) yDeltaChart =((int) mainFrameDim.getHeight()) / framesPerCol;
		
		if(yDeltaChart > MAXHEIGHTFACTOR * xDeltaChart) yDeltaChart = (int)(MAXHEIGHTFACTOR * xDeltaChart);
		
		Component actComp;
		int xCompCount = 0;
		int yCompCount = 0;
		for(int i = 0; i < comp.getComponentCount(); i++){
			actComp = comp.getComponent(i);
			if(actComp != null && actComp instanceof JInternalFrame){
				if(((JInternalFrame) actComp).getName().equals(SIMULATIONFRAME)){
					((JInternalFrame) actComp).setPreferredSize(new Dimension(xDeltaSim, yDeltaSim));
					((JInternalFrame) actComp).setLocation(0, 0);
				}
				else if(((JInternalFrame) actComp).getName().equals(CHARTFRAME)){
					((JInternalFrame) actComp).setPreferredSize(new Dimension(xDeltaChart, yDeltaChart));
					((JInternalFrame) actComp).setLocation(xDeltaSim + xCompCount * xDeltaChart,  yCompCount
							* yDeltaChart);
					if(xCompCount < numberOfColumns-2) xCompCount ++;
					else{
						xCompCount = 0;
						yCompCount++;
					}

				}
				else if(((JInternalFrame) actComp).getName().equals(CONTROLLERFRAME)){
					((JInternalFrame) actComp).setPreferredSize(new Dimension(xDeltaSim, ((int) mainFrameDim.getHeight() - yDeltaSim)));
					((JInternalFrame) actComp).setLocation(0, yDeltaSim);
							

				}
				((JInternalFrame) actComp).pack();
			}

		}

	}
	
	public void removeAllChartInternalFrames(){
		 removeAllChartInternalFrames(desktop);
	}
	
	private void removeAllChartInternalFrames(JComponent comp){
		for(Component actComp : comp.getComponents()){
		
			if(actComp != null && actComp instanceof JInternalFrame){
				if(((JInternalFrame) actComp).getName().equals(CHARTFRAME)) comp.remove(actComp);
			}
		}
		comp.validate();
		comp.repaint();
	}

	private void maximizeWorkaround(JInternalFrame frame){
		
		 JComponent title = ((BasicInternalFrameUI)frame.getUI()).getNorthPane();
	    final Component[] comps =title.getComponents();
	     
	     for(int i = 0; i < comps.length; i++){
	    	 final int n = i;
	    	 if(comps[i] instanceof JButton && i == 2){
	    		 ((JButton) comps[i]).addActionListener(new ActionListener(){

	 				public void actionPerformed(ActionEvent e) {

	 					if(console.getPlayState() != Console.PS_PAUSED && console.getPlayState() == Console.PS_PLAYING){
	 						console.pressPause();
	 						resizeButtonIsActionSource = true;
	 						
	 					}
	 					else if(console.getPlayState() == Console.PS_PAUSED){
	 						SimStateServer.getInstance().setSimStatetoPause();
	 					}
	 				}
	    			 
	    		 });
	    	 }
	    }
	}
	
	
	
	public void pressWorkaroundSimulationPause(){
		if(console.getPlayState() != Console.PS_PAUSED && console.getPlayState() == Console.PS_PLAYING){
				console.pressPause();
				workaroundPauseWasPressed = true;
		}
	}
	
	public void pressWorkaroundSimulationPlay(){
		if(console.getPlayState() == Console.PS_PAUSED && console.getPlayState() != Console.PS_STOPPED && workaroundPauseWasPressed){
			console.pressPause();
			workaroundPauseWasPressed = false;
		}
	}
	
	public void clearWoundPortrayalDraw(){
		if(woundPortrayal != null){
			woundPortrayal.clearWoundRegionCoordinates();
			woundPortrayal.closeWoundRegionPath(false);
		}
		
	}	
	
	public EpisimSimulationDisplay getDisplay() {
	   
   	if(display2D != null) return display2D;
   	if(display3D != null) return display3D;   	
   	return null;
   }
	
	public void simulationWasStarted(){
		ModelController.getInstance().setSimulationStartedOnce(true);
		for(SimulationStateChangeListener actListener: simulationStateListeners) actListener.simulationWasStarted();
	}
	
	public void simulationWasStopped(){
		for(SimulationStateChangeListener actListener: simulationStateListeners) actListener.simulationWasStopped();
	}
	
	public void simulationWasPaused(){
		for(SimulationStateChangeListener actListener: simulationStateListeners) actListener.simulationWasPaused();
	}
	
	public void addSimulationStateChangeListener(SimulationStateChangeListener listener){
		this.simulationStateListeners.add(listener);
	}

	public void chartSetHasChanged() {
		if(console != null){ 
			console.deregisterAllFrames();
			removeAllChartInternalFrames(desktop);
			int offset = 10;
			int index = 2;
			
			ArrayList<JPanel> chartPanels = new ArrayList<JPanel>();
			chartPanels.addAll(ChartController.getInstance().getChartPanelsofActLoadedChartSet());
			chartPanels.addAll(ChartController.getInstance().getDiffusionChartPanelsofActLoadedChartSet());
			for(JPanel actPanel : chartPanels){
				String title ="";
				
				if(actPanel instanceof ChartPanel){
				  title = ((ChartPanel)actPanel).getChart().getTitle().getText();
				}
				else{
					title = actPanel.getName();
				}
				
				JInternalFrame frame = getChartInternalFrame(actPanel, title);
				
				if(!autoArrangeWindows){
					desktop.add(frame, index++);
					frame.setLocation(offset, offset);
					frame.setPreferredSize(new Dimension(DEFAULTCHARTWIDTH, DEFAULTCHARTHEIGHT));
					frame.pack();
					frame.toFront();
					offset+=10;
				}
				else desktop.add(frame);				
			}
		   if(autoArrangeWindows)
		   	arrangeElements(desktop, false);
		   else desktop.validate();
		   registerInternalFrames(desktop, console);
		}
   }
	
	public void setAutoArrangeWindows(boolean autoArrangeWindows){
		this.autoArrangeWindows = autoArrangeWindows;
		if(autoArrangeWindows) arrangeElements(desktop, false);
	}

	public SimulationDisplayProperties getSimulationDisplayProperties(EpisimDrawInfo<DrawInfo2D> episimInfo){
		DrawInfo2D info = episimInfo.getDrawInfo();
		double displayScale = getDisplay().getDisplayScale();
		double scaleX = (EPIDISPLAYWIDTH / TissueController.getInstance().getTissueBorder().getWidthInMikron());
		double scaleY = (EPIDISPLAYHEIGHT / TissueController.getInstance().getTissueBorder().getHeightInMikron());
		scaleX*=displayScale;
		scaleY*=displayScale;
		
		
		double offsetX = DISPLAY_BORDER_LEFT*displayScale;
		double offsetY = DISPLAY_BORDER_TOP*displayScale;
		
		double differenceX = 0;
		double differenceY = 0;
		
		
		double startX =0; 
		double startY =0; 
		if(info != null){
			differenceX = (info.clip.width-(EPIDISPLAYWIDTH*displayScale));
			differenceY = (info.clip.height-(EPIDISPLAYHEIGHT*displayScale));
			startX =differenceX >= 0 ? info.clip.x:0;
			startY =differenceY >= 0 ? info.clip.y:0;
		}
		offsetX+=startX;
		offsetY+=startY;
		
		return new SimulationDisplayProperties(scaleX, scaleY, offsetX, offsetY);
	}
	
	public class SimulationDisplayProperties{
		public final double displayScaleX;
		public final double displayScaleY; 
		public final double offsetX; 
		public final double offsetY;
		
		private SimulationDisplayProperties(double displayScaleX, double displayScaleY, double offsetX, double offsetY){
			this.displayScaleX = displayScaleX;
			this.displayScaleY = displayScaleY;
			this.offsetX = offsetX;
			this.offsetY = offsetY;
		}
	}
	
	

}
