package sim.app.episim.gui;

import sim.SimStateServer;
import sim.engine.*;
import sim.app.episim.Epidermis;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.KCyte;
import sim.app.episim.KCyteInspector;
import sim.app.episim.SimulationStateChangeListener;
import sim.app.episim.TissueServer;
import sim.app.episim.datamonitoring.charts.ChartController;
import sim.app.episim.datamonitoring.charts.ChartSetChangeListener;
import sim.app.episim.datamonitoring.charts.DefaultCharts;

import sim.app.episim.devBasalLayer.BasementMembranePortrayal2DDev;
import sim.app.episim.devBasalLayer.EpidermisDev;
import sim.app.episim.model.BioChemicalModelController;
import sim.app.episim.model.MiscalleneousGlobalParameters;
import sim.app.episim.model.ModelController;
import sim.app.episim.tissue.TissueBorder;
import sim.app.episim.tissue.TissueController;
import sim.app.episim.tissue.TissueType;
import sim.app.episim.visualization.BasementMembranePortrayal2D;
import sim.app.episim.visualization.GridPortrayal2D;
import sim.app.episim.visualization.KeratinocytePortrayal2D;
import sim.app.episim.visualization.RulerPortrayal2D;
import sim.app.episim.visualization.WoundPortrayal2D;
import sim.display.*;
import sim.portrayal.continuous.*;
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

import episiminterfaces.EpisimCellDiffModelGlobalParameters;
import episiminterfaces.EpisimMechanicalModelGlobalParameters;


public class EpidermisGUIState extends GUIState implements ChartSetChangeListener{

	public EpiDisplay2D display;

	public JInternalFrame displayFrame;

	private JFrame mainFrame;

	private final int INTERNALFRAMECOLS = 2;

	private final String SIMULATIONFRAME = "Simframe";
	private final String CONTROLLERFRAME = "controllerFrame";
	private final String CHARTFRAME = "chartFrame";
	
	private final String EPIDERMISNAME = "Epidermis";
	private final String BASEMENTMEMBRANENAME = "Basement Membrane";
	private final String WOUNDNAME = "Wound Region";
	private final String RULERNAME = "Ruler";
	private final String GRIDNAME = "Grid";

	private JDesktopPane desktop;

	private EpiConsole console;

	private static final double INITIALZOOMFACTOR = 5;
	private final double EPIDISPLAYWIDTH = TissueController.getInstance().getTissueBorder().getWidth() * INITIALZOOMFACTOR;
	private final double EPIDISPLAYHEIGHT = TissueController.getInstance().getTissueBorder().getHeight()* INITIALZOOMFACTOR;
	
	private static final int DISPLAYBORDER = 40;
	
	private final double MAXHEIGHTFACTOR = 1;
	
	private boolean workaroundPauseWasPressed = false;
	
	private boolean pausedBecauseOfMainFrameResize = false;
	
	private final int STATUSBARHEIGHT = 15;
	
	/*
	private final double EPIDISPLAYWIDTH = 750;
	private final double EPIDISPLAYHEIGHT = 700;
	*/
	private final BasementMembranePortrayal2D basementPortrayalDraw;
	private  RulerPortrayal2D rulerPortrayalDraw;
	private  GridPortrayal2D gridPortrayalDraw;
	
	private boolean resizeButtonIsActionSource = false;
	
	private final WoundPortrayal2D woundPortrayalDraw;
	
	private boolean activateDrawing = false;
	
	private ArrayList<SimulationStateChangeListener> simulationStateListeners;
	
	
	
	ContinuousPortrayal2D epiPortrayal = new ContinuousPortrayal2D();
	ContinuousPortrayal2D basementPortrayal = new ContinuousPortrayal2D();
	ContinuousPortrayal2D woundPortrayal = new ContinuousPortrayal2D();
	ContinuousPortrayal2D rulerPortrayal = new ContinuousPortrayal2D();
	ContinuousPortrayal2D gridPortrayal = new ContinuousPortrayal2D();
	
	
	public EpidermisGUIState(JFrame mainFrame) {		
		
		this(new Epidermis(System.currentTimeMillis()), mainFrame, false);
	}

	
	
	public EpidermisGUIState(SimState state, JFrame mainFrame, boolean reloadSnapshot) {
		
		super(state);
		if(state instanceof TissueType) TissueServer.getInstance().registerTissue(((TissueType) state));
		simulationStateListeners = new ArrayList<SimulationStateChangeListener>();
		ChartController.getInstance().registerChartSetChangeListener(this);
		this.mainFrame = mainFrame;
		this.setConsole(new EpiConsole(this, reloadSnapshot));
		basementPortrayalDraw =new BasementMembranePortrayal2D(EPIDISPLAYWIDTH+(2*DISPLAYBORDER), EPIDISPLAYHEIGHT+(2*DISPLAYBORDER), DISPLAYBORDER);
		woundPortrayalDraw = new WoundPortrayal2D(EPIDISPLAYWIDTH+(2*DISPLAYBORDER), EPIDISPLAYHEIGHT+(2*DISPLAYBORDER));
		rulerPortrayalDraw =new RulerPortrayal2D(EPIDISPLAYWIDTH + (2*DISPLAYBORDER), EPIDISPLAYHEIGHT+ (2*DISPLAYBORDER), DISPLAYBORDER, INITIALZOOMFACTOR);
		gridPortrayalDraw =new GridPortrayal2D(EPIDISPLAYWIDTH + (2*DISPLAYBORDER), EPIDISPLAYHEIGHT+ (2*DISPLAYBORDER), DISPLAYBORDER, INITIALZOOMFACTOR);
		
	}
	
	
	
	public Inspector getBiochemicalModelInspector() {

		EpisimCellDiffModelGlobalParameters chemModel = ModelController.getInstance().getEpisimCellDiffModelGlobalParameters();
		if(chemModel == null)
			return null;
		Inspector i = new SimpleInspector(chemModel, this);
		i.setVolatile(false);
		return i;
	}

	public Inspector getBiomechnicalModelInspector() {

		EpisimMechanicalModelGlobalParameters mechModel = ModelController.getInstance().getEpisimMechanicalModelGlobalParameters();
		if(mechModel == null)
			return null;
		Inspector i = new SimpleInspector(mechModel, this);
		i.setVolatile(false);
		return i;
	}
	
	public Inspector getMiscalleneousInspector() {

		Object miscalleneous = MiscalleneousGlobalParameters.instance();
		if(miscalleneous == null)
			return null;
		Inspector i = new SimpleInspector(miscalleneous, this);
		i.setVolatile(false);
		return i;
	}
	

	public void setConsole(EpiConsole cons) {

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
		controller.unregisterFrame(mainFrame); // unregister previous frame
	}
	
	
	
	
	public static String getName() {

		return "Epidermis Simulation  - Controller";
	}

	public void start() {

		super.start();
		setupPortrayals();
		

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
		epiPortrayal.setField(theEpidermis.getCellContinous2D());
		basementPortrayal.setField(theEpidermis.getBasementContinous2D());
		woundPortrayal.setField(theEpidermis.getBasementContinous2D());
		rulerPortrayal.setField(theEpidermis.getRulerContinous2D());
		gridPortrayal.setField(theEpidermis.getGridContinous2D());
		// make the flockers random colors and four times their normal size
		// (prettier)
		java.awt.Color myColor = java.awt.Color.lightGray;

		
		epiPortrayal.setPortrayalForClass(KCyte.class, new KeratinocytePortrayal2D(myColor) {

			public Inspector getInspector(LocationWrapper wrapper, GUIState state) {
				System.out.println("Hallo Inspector");
				// make the inspector
				return new KCyteInspector(super.getInspector(wrapper, state), wrapper, state);
			}
		});
		
		basementPortrayal.setPortrayalForAll(basementPortrayalDraw);
		woundPortrayal.setPortrayalForAll(woundPortrayalDraw);
		rulerPortrayal.setPortrayalForAll(rulerPortrayalDraw);
		gridPortrayal.setPortrayalForAll(gridPortrayalDraw);
		
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

		display = new EpiDisplay2D(EPIDISPLAYWIDTH+ (2*DISPLAYBORDER), EPIDISPLAYHEIGHT+(2*DISPLAYBORDER), this, 1);
		//display.setClipping(false);
		Color myBack = new Color(0xE0, 0xCB, 0xF6);
		display.setBackdrop(Color.BLACK);
	
		
		
		display.attach(basementPortrayal, BASEMENTMEMBRANENAME, new Rectangle2D.Double(0,0,EPIDISPLAYWIDTH+(2*DISPLAYBORDER), EPIDISPLAYHEIGHT+(2*DISPLAYBORDER)), true);
		display.attach(epiPortrayal, EPIDERMISNAME, new Rectangle2D.Double(DISPLAYBORDER,DISPLAYBORDER,EPIDISPLAYWIDTH, EPIDISPLAYHEIGHT), true);
		display.attach(woundPortrayal, WOUNDNAME, new Rectangle2D.Double(DISPLAYBORDER,DISPLAYBORDER,EPIDISPLAYWIDTH, EPIDISPLAYHEIGHT), true);
		display.attach(rulerPortrayal, RULERNAME, new Rectangle2D.Double(0,0,EPIDISPLAYWIDTH+(2*DISPLAYBORDER), EPIDISPLAYHEIGHT+(2*DISPLAYBORDER)), true);
		display.attach(gridPortrayal, GRIDNAME, new Rectangle2D.Double(0,0,EPIDISPLAYWIDTH+(2*DISPLAYBORDER), EPIDISPLAYHEIGHT+(2*DISPLAYBORDER)), true);
		
		
		display.insideDisplay.addMouseListener(new MouseAdapter(){			
			
			
				public void mouseClicked(MouseEvent e){
					if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2){
						if(console.getPlayState() != console.PS_PAUSED && console.getPlayState() == console.PS_PLAYING)console.pressPause();
					}
				}
			
				public void mousePressed(MouseEvent e) {
	
					if(e.getButton() == MouseEvent.BUTTON3){
						if(console.getPlayState() != console.PS_PAUSED && console.getPlayState() == console.PS_PLAYING)console.pressPause();
						if(woundPortrayalDraw != null){
							woundPortrayalDraw.clearWoundRegionCoordinates();
							woundPortrayalDraw.closeWoundRegionPath(false);
						}
						activateDrawing = true;
					}
					
				}
				public void mouseReleased(MouseEvent e) {
	
					if(e.getButton() == MouseEvent.BUTTON3){
						if(console.getPlayState() == console.PS_PAUSED)console.pressPause();
						if(woundPortrayalDraw != null){
							woundPortrayalDraw.closeWoundRegionPath(true);
							((Epidermis) state).removeCells(woundPortrayalDraw.getWoundRegion());
							activateDrawing = false;
						}
					}
					
				}
	
				public void mouseEntered(MouseEvent e){
					if(display.isPortrayalVisible(RULERNAME)){
						if(rulerPortrayalDraw != null){
							rulerPortrayalDraw.setCrosshairsVisible(true);
							rulerPortrayalDraw.setActMousePosition(new Point2D.Double(e.getX(), e.getY()));
						}
					}
				}
				public void mouseExited(MouseEvent e){
					if(display.isPortrayalVisible(RULERNAME) && rulerPortrayalDraw != null) rulerPortrayalDraw.setCrosshairsVisible(false);
				}

			});
		display.insideDisplay.addMouseMotionListener(new MouseMotionAdapter(){
			public void mouseDragged(MouseEvent e){
				
				if(activateDrawing){
					if(woundPortrayalDraw != null){
						woundPortrayalDraw.addMouseCoordinate(new Double2D(e.getX(), e.getY()));
						redrawDisplay();
					}
				}
				if(display.isPortrayalVisible(RULERNAME)) rulerPortrayalDraw.setActMousePosition(new Point2D.Double(e.getX(), e.getY()));
				if(display.isPortrayalVisible(RULERNAME)&& (console.getPlayState()==Console.PS_PAUSED
						||console.getPlayState()==Console.PS_STOPPED) && ModelController.getInstance().isSimulationStartedOnce()) redrawDisplay();	
				
			}
			public void mouseMoved(MouseEvent e){
				if(display.isPortrayalVisible(RULERNAME)) rulerPortrayalDraw.setActMousePosition(new Point2D.Double(e.getX(), e.getY()));
				if(display.isPortrayalVisible(RULERNAME)&&(console.getPlayState()==Console.PS_PAUSED
						|| console.getPlayState()==Console.PS_STOPPED) && ModelController.getInstance().isSimulationStartedOnce()) redrawDisplay();			
			} 
		});
		

		
		
		// display.setBackdrop(Color.white);
		

		displayFrame = display.createInternalFrame();
		maximizeWorkaround(displayFrame);
		
	
		
	//-----------------------------------------------------------------------------------------------------------------	
	//	Necessary for Maximize Workaround
	//-----------------------------------------------------------------------------------------------------------------	
		
		mainFrame.addComponentListener(new ComponentAdapter(){
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
	       else if(console != null && console.getPlayState() == console.PS_PAUSED){
	      	 if(SimStateServer.getInstance().getSimState() == SimStateServer.SimState.STEPWISE){
	      		 SimStateServer.getInstance().setSimStatetoPause();
	      		 mainFrame.validate();
	      		 mainFrame.repaint();
					
	      	 }
			 }
         }
		});

		mainFrame.addMouseMotionListener(new MouseMotionAdapter(){ public void mouseMoved(MouseEvent e) {checkForRestartAfterMainFrameResize();}});
		desktop.addMouseMotionListener(new MouseMotionAdapter(){	public void mouseMoved(MouseEvent e) {checkForRestartAfterMainFrameResize(); }});
		mainFrame.addMouseListener(new MouseAdapter(){
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
    	      if(console != null && console.getPlayState() == console.PS_PAUSED && resizeButtonIsActionSource){
					 console.pressPause();
					 resizeButtonIsActionSource = false;
				 }
    	     else if(console != null && console.getPlayState() == console.PS_PAUSED){
    	   	 if(SimStateServer.getInstance().getSimState() == SimStateServer.SimState.STEPWISE){
    	   		SimStateServer.getInstance().setSimStatetoPause();
    	   		 displayFrame.validate();
    	   		displayFrame.repaint();
					
	      	 }
				}
		    }
		});
		
		//-------------------------------------------------------------------------------------------------------------------------------------------------------------
		displayFrame.setTitle("Epidermis Simulation v. 1.1");
		displayFrame.setName(SIMULATIONFRAME);
		displayFrame.setMaximizable(true);
		displayFrame.setIconifiable(true);
		displayFrame.setResizable(true);
		displayFrame.setVisible(true);
		displayFrame.setFrameIcon(null);
		
		desktop.add(displayFrame);

		
		
		
		desktop.putClientProperty("JDesktopPane.dragMode", "outline");
		
		
		
	
		final JScrollPane desktopScroll = new JScrollPane(desktop);
		
		
		
	desktop.setPreferredSize(new Dimension(
				(int)(mainFrame.getContentPane().getWidth()-desktopScroll.getVerticalScrollBar().getPreferredSize().getWidth()),
				(int)(mainFrame.getContentPane().getHeight()-desktopScroll.getHorizontalScrollBar().getPreferredSize().getHeight()-STATUSBARHEIGHT)));
	   
	  
		
		desktop.setSize(desktop.getPreferredSize());
		
		arrangeElements(desktop, false);
		
		mainFrame.getContentPane().add(desktopScroll, BorderLayout.CENTER);
		
		
		
		
		mainFrame.pack();
		
		mainFrame.addComponentListener(new ComponentAdapter(){
			public void componentResized(ComponentEvent comp) {
				
							
				if(comp.getSource() instanceof JFrame){
					
					if(desktop != null){ 
					
						
					
					desktop.setPreferredSize(new Dimension(
								(int)(mainFrame.getContentPane().getWidth()-desktopScroll.getVerticalScrollBar().getPreferredSize().getWidth()),
								(int)(mainFrame.getContentPane().getHeight()-desktopScroll.getHorizontalScrollBar().getPreferredSize().getHeight())));
						arrangeElements(desktop, true);
					}
			

				}
				
				
			}
		});
		
		
		
		registerInternalFrames(desktop, ((EpiConsole)c));
		
		
	}
	
	private void checkForRestartAfterMainFrameResize(){
		if(console != null && console.getPlayState() == Console.PS_PAUSED && pausedBecauseOfMainFrameResize){
		     
     	 console.pressPause();
     	 pausedBecauseOfMainFrameResize = false;
		 }
	}
	
	private void registerInternalFrames(JDesktopPane desktop, EpiConsole c){
		Component[] comps = desktop.getComponents();
		for(Component comp: comps){
			if(comp instanceof JInternalFrame &&
					((JInternalFrame) comp).getName().equals(CHARTFRAME))c.registerFrame(((JInternalFrame)comp));
		}
	}
	
	public void redrawDisplay(){
	if(display.insideDisplay.getWidth() > 0 && display.insideDisplay.getHeight() > 0){	
  	 Graphics g = display.insideDisplay.getGraphics();
  	 display.insideDisplay.paintComponent(g,true);
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
		
		
		comp.getComponentCount();
		int corrNumber = comp.getComponentCount() - 2;
		int remainder = 0;
		if((remainder = corrNumber % INTERNALFRAMECOLS) != 0)
			corrNumber += (INTERNALFRAMECOLS - remainder);
		int framesPerCol = corrNumber / INTERNALFRAMECOLS;

		int xDeltaSim = ((int) mainFrameDim.getWidth()) / 2;

		int yDeltaSim = ((int) mainFrameDim.getHeight())*2 / 3;
		int xDeltaChart = 0;
		if(((int) mainFrameDim.getWidth()  - xDeltaSim) > 0 && INTERNALFRAMECOLS >0)
			xDeltaChart= ((int) mainFrameDim.getWidth() - xDeltaSim) / INTERNALFRAMECOLS;
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
					if(xCompCount < INTERNALFRAMECOLS-1) xCompCount ++;
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

	 					if(console.getPlayState() != console.PS_PAUSED && console.getPlayState() == console.PS_PLAYING){
	 						console.pressPause();
	 						resizeButtonIsActionSource = true;
	 						
	 					}
	 					else if(console.getPlayState() == console.PS_PAUSED){
	 						SimStateServer.getInstance().setSimStatetoPause();
	 					}
	 				}
	    			 
	    		 });
	    	 }
	    }
	}
	
	
	
	public void workaroundConsolePause(){
		if(console.getPlayState() != console.PS_PAUSED && console.getPlayState() == console.PS_PLAYING){
				console.pressPause();
				workaroundPauseWasPressed = true;
		}
	}
	
	public void workaroundConsolePlay(){
		if(console.getPlayState() == console.PS_PAUSED && console.getPlayState() != console.PS_STOPPED){
			console.pressPause();
			workaroundPauseWasPressed = false;
		}
	}
	
	public void clearWoundPortrayalDraw(){
		
		woundPortrayalDraw.clearWoundRegionCoordinates();
		woundPortrayalDraw.closeWoundRegionPath(false);
		
	}
   
	public void setReloadedSnapshot(boolean reloaded){
		console.setReloadedSnapshot(reloaded);
	}

	
	public WoundPortrayal2D getWoundPortrayalDraw() {
	
		return woundPortrayalDraw;
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
	
	public void addSnapshotRestartListener(SnapshotRestartListener listener){
		if(this.console != null) console.addSnapshotRestartListener(listener); 
	}

	public void chartSetHasChanged() {
		if(console != null){ 
			console.deregisterAllFrames();
			removeAllChartInternalFrames(desktop);
			
			for(ChartPanel actPanel : ChartController.getInstance().getChartPanelsofActLoadedChartSet()){
				desktop.add(getChartInternalFrame(actPanel, actPanel.getChart().getTitle().getText()));
			}
		   arrangeElements(desktop, false);
		   registerInternalFrames(desktop, console);
		}
   }

	

	
	

}
