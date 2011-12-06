package sim.app.episim.gui;

import sim.SimStateServer;
import sim.engine.*;
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
import sim.app.episim.model.visualization.UniversalCellPortrayal2D;
import sim.app.episim.tissue.Epidermis;
import sim.app.episim.tissue.TissueBorder;
import sim.app.episim.tissue.TissueController;
import sim.app.episim.tissue.TissueServer;
import sim.app.episim.tissue.TissueType;
import sim.app.episim.visualization.BasementMembranePortrayal2D;
import sim.app.episim.visualization.GridPortrayal2D;
import sim.app.episim.visualization.RulerPortrayal2D;
import sim.app.episim.visualization.WoundPortrayal2D;
import sim.display.*;
import sim.portrayal.continuous.*;
import sim.portrayal.grid.ObjectGridPortrayal2D;
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

import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.EpisimPortrayal;


public class EpisimGUIState extends GUIState implements ChartSetChangeListener{

	public EpiDisplay2D display;

	public JInternalFrame displayFrame;

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
	
	public  final int DISPLAYBORDER = 40;	
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
	
	FieldPortrayal2D epiPortrayal;
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

		final JInternalFrame controllerFrame = new JInternalFrame("EpiSimulation-Controller", true, false, true, true);

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

		return "Epidermis Simulation  - Controller";
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

		Epidermis theEpidermis = (Epidermis) state;
		// obstacle portrayal needs no setup
		
		basementPortrayal = new BasementMembranePortrayal2D();
		woundPortrayal = new WoundPortrayal2D();
		rulerPortrayal = new RulerPortrayal2D();
		gridPortrayal = new GridPortrayal2D();
		
		
		
		
		EpisimPortrayal cellPortrayal = ModelController.getInstance().getCellPortrayal();
		
		
		
		if(cellPortrayal instanceof UniversalCellPortrayal2D){
			epiPortrayal = new ContinuousPortrayal2D();
			epiPortrayal.setPortrayalForClass(UniversalCell.class, (UniversalCellPortrayal2D)cellPortrayal);
		}
		else if(cellPortrayal instanceof ObjectGridPortrayal2D){
			epiPortrayal = (FieldPortrayal2D) cellPortrayal;
		}
		epiPortrayal.setField(ModelController.getInstance().getBioMechanicalModelController().getCellField());
		
		display.detatchAll();
		
		display.attach(basementPortrayal, basementPortrayal.getPortrayalName(), basementPortrayal.getViewPortRectangle(), true);
		EpisimPortrayal[] portrayals = ModelController.getInstance().getAdditionalPortrayalsCellBackground();
		for(int i = 0; i < portrayals.length; i++) display.attach((FieldPortrayal2D)portrayals[i], portrayals[i].getPortrayalName(), portrayals[i].getViewPortRectangle(), true);
		display.attach(epiPortrayal, cellPortrayal.getPortrayalName(), cellPortrayal.getViewPortRectangle(), true);
		portrayals = ModelController.getInstance().getAdditionalPortrayalsCellForeground();
		for(int i = 0; i < portrayals.length; i++) display.attach((FieldPortrayal2D)portrayals[i], portrayals[i].getPortrayalName(), portrayals[i].getViewPortRectangle(), true);
		display.attach(woundPortrayal, woundPortrayal.getPortrayalName(), woundPortrayal.getViewPortRectangle(), true);
		display.attach(rulerPortrayal, rulerPortrayal.getPortrayalName(), rulerPortrayal.getViewPortRectangle(), true);
		display.attach(gridPortrayal, gridPortrayal.getPortrayalName(), gridPortrayal.getViewPortRectangle(), true);
		
		
		// reschedule the displayer
		display.reset();

		// redraw the display
		display.repaint();
	}
	
	

	void addInternalFrames(Controller c) {

		desktop = new JDesktopPane();
		
		desktop.setBackground(Color.LIGHT_GRAY);
		// --------------------------------------------------------------------------
		// Internal Frame for EpiSimlation Display
		// --------------------------------------------------------------------------

		display = new EpiDisplay2D(EPIDISPLAYWIDTH+ (2*DISPLAYBORDER), EPIDISPLAYHEIGHT+(2*DISPLAYBORDER), this);
		//display.setClipping(false);
		Color myBack = new Color(0xE0, 0xCB, 0xF6);
		display.setBackdrop(Color.BLACK);
	
		
		
	
		
		
		display.getInsideDisplay().addMouseListener(new MouseAdapter(){			
			
			
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
					if(rulerPortrayal != null && display.isPortrayalVisible(rulerPortrayal.getPortrayalName())){
						
							rulerPortrayal.setCrosshairsVisible(true);
							rulerPortrayal.setActMousePosition(new Point2D.Double(e.getX(), e.getY()));
						
					}
				}
				public void mouseExited(MouseEvent e){
					if(rulerPortrayal != null && display.isPortrayalVisible(rulerPortrayal.getPortrayalName())) rulerPortrayal.setCrosshairsVisible(false);
				}

			});
		display.getInsideDisplay().addMouseMotionListener(new MouseMotionAdapter(){
			public void mouseDragged(MouseEvent e){
				
				if(activateDrawing){
					if(woundPortrayal != null){
						woundPortrayal.addMouseCoordinate(new Double2D(e.getX(), e.getY()));
						redrawDisplay();
					}
				}
				if(rulerPortrayal != null && display.isPortrayalVisible(rulerPortrayal.getPortrayalName())) rulerPortrayal.setActMousePosition(new Point2D.Double(e.getX(), e.getY()));
				if(rulerPortrayal != null && display.isPortrayalVisible(rulerPortrayal.getPortrayalName())&& (console.getPlayState()==Console.PS_PAUSED
						||console.getPlayState()==Console.PS_STOPPED) && ModelController.getInstance().isSimulationStartedOnce()) redrawDisplay();	
				
			}
			public void mouseMoved(MouseEvent e){
				if(rulerPortrayal != null && display.isPortrayalVisible(rulerPortrayal.getPortrayalName())) rulerPortrayal.setActMousePosition(new Point2D.Double(e.getX(), e.getY()));
				if(rulerPortrayal != null && display.isPortrayalVisible(rulerPortrayal.getPortrayalName())&&(console.getPlayState()==Console.PS_PAUSED
						|| console.getPlayState()==Console.PS_STOPPED) && ModelController.getInstance().isSimulationStartedOnce()) redrawDisplay();			
			} 
		});
		

		
		
		// display.setBackdrop(Color.white);
		

		displayFrame = display.createInternalFrame();
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
		displayFrame.setTitle("Epidermis Simulation v. "+EpidermisSimulator.versionID);
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
	
	public void redrawDisplay(){
	if(display.getInsideDisplay().getWidth() > 0 && display.getInsideDisplay().getHeight() > 0){	
  	 Graphics g = display.getInsideDisplay().getGraphics();
  	 display.paintComponentInInnerDisplay(g,true);
  	 g.dispose();
	}
	}

	public void init(Controller c) {

		super.init(c);
		
		addInternalFrames(c);
		
		

	}

	public void quit() {
		console.pressStop();
		super.quit();
		
		if(displayFrame != null)
			displayFrame.dispose();
		displayFrame = null;
		
		desktop.removeAll();
		desktop.validate();

		display = null;
		
	}

	private JInternalFrame getChartInternalFrame(ChartPanel chartPanel, String title) {

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
	
	
	
	public void workaroundConsolePause(){
		if(console.getPlayState() != Console.PS_PAUSED && console.getPlayState() == Console.PS_PLAYING){
				console.pressPause();
				workaroundPauseWasPressed = true;
		}
	}
	
	public void workaroundConsolePlay(){
		if(console.getPlayState() == Console.PS_PAUSED && console.getPlayState() != Console.PS_STOPPED){
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
	
	public WoundPortrayal2D getWoundPortrayalDraw() {
	
		return woundPortrayal;
	}
	public EpiDisplay2D getDisplay() {
	   
   	return display;
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
			for(ChartPanel actPanel : ChartController.getInstance().getChartPanelsofActLoadedChartSet()){
				JInternalFrame  frame = getChartInternalFrame(actPanel, actPanel.getChart().getTitle().getText());
				
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

	

	
	

}
