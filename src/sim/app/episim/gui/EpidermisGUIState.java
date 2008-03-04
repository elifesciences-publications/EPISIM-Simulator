package sim.app.episim.gui;

import sim.engine.*;
import sim.app.episim.Epidermis;
import sim.app.episim.KCyte;
import sim.app.episim.KCyteInspector;
import sim.app.episim.charts.DefaultCharts;
import sim.app.episim.model.BioChemicalModelController;
import sim.app.episim.model.ModelController;
import sim.app.episim.visualization.BasementMembranePortrayal2D;
import sim.app.episim.visualization.KeratinocytePortrayal2D;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Rectangle2D;


import javax.swing.JFrame;
import javax.swing.plaf.basic.BasicInternalFrameUI;

import org.jfree.chart.*; // ChartPanel;

import episiminterfaces.EpisimCellDiffModelGlobalParameters;
import episiminterfaces.EpisimMechanicalModelGlobalParameters;


public class EpidermisGUIState extends GUIState{

	public EpiDisplay2D display;

	public JInternalFrame displayFrame;

	private JFrame mainFrame;

	private final int INTERNALFRAMECOLS = 2;

	private final String SIMULATIONFRAME = "Simframe";

	private final String CONTROLLERFRAME = "controllerFrame";

	private final String CHARTFRAME = "chartFrame";

	private JDesktopPane desktop;

	private EpiConsole console;

	private final double EPIDISPLAYWIDTH = 750;
	private final double EPIDISPLAYHEIGHT = 700;
	
	private final BasementMembranePortrayal2D basementPortrayalDraw;
	
	private boolean resizeButtonIsActionSource = false;
	
	private final WoundPortrayal2D woundPortrayalDraw;
	
	private boolean activateDrawing = false;
	
	
	
	
	
	ContinuousPortrayal2D epiPortrayal = new ContinuousPortrayal2D();
	ContinuousPortrayal2D basementPortrayal = new ContinuousPortrayal2D();
	ContinuousPortrayal2D woundPortrayal = new ContinuousPortrayal2D();
	
	
	public EpidermisGUIState(JFrame mainFrame) {
		this(new Epidermis(System.currentTimeMillis()), mainFrame, false);
	}

	public EpidermisGUIState(SimState state, JFrame mainFrame, boolean reloadSnapshot) {
		
		super(state);
		this.mainFrame = mainFrame;
		this.setConsole(new EpiConsole(this, reloadSnapshot));
		basementPortrayalDraw =new BasementMembranePortrayal2D(EPIDISPLAYWIDTH, EPIDISPLAYHEIGHT);
		woundPortrayalDraw = new WoundPortrayal2D(EPIDISPLAYWIDTH, EPIDISPLAYHEIGHT);
		
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
	

	public void setConsole(EpiConsole cons) {

		console = cons;

		final JInternalFrame controllerFrame = new JInternalFrame("EpiSimulation-Controller", true, false, true, true);

		controllerFrame.setContentPane(cons.getControllerContainer());
		
		controllerFrame.setResizable(true);
		controllerFrame.setVisible(true);
		controllerFrame.setName(CONTROLLERFRAME);
		controllerFrame.setFrameIcon(null);
		desktop.add(controllerFrame);
		arrangeElements(desktop);
		
		
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
		// make the flockers random colors and four times their normal size
		// (prettier)
		java.awt.Color myColor = java.awt.Color.lightGray;

		
		epiPortrayal.setPortrayalForClass(KCyte.class, new KeratinocytePortrayal2D(myColor) {

			public Inspector getInspector(LocationWrapper wrapper, GUIState state) {

				// make the inspector
				return new KCyteInspector(super.getInspector(wrapper, state), wrapper, state);
			}
		});
		
		basementPortrayal.setPortrayalForAll(basementPortrayalDraw);
		woundPortrayal.setPortrayalForAll(woundPortrayalDraw);
		
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

		display = new EpiDisplay2D(EPIDISPLAYWIDTH, EPIDISPLAYHEIGHT, this, 1);
		//display.setClipping(false);
		Color myBack = new Color(0xE0, 0xCB, 0xF6);
		display.setBackdrop(Color.BLACK);
	
		
		display.attach(basementPortrayal, "basementMembrane");
		
		display.attach(epiPortrayal, "epidermis");
		display.attach(woundPortrayal, "wound");
		
		
		display.insideDisplay.addMouseListener(new MouseAdapter(){			

			public void mousePressed(MouseEvent e) {

				if(e.getButton() == MouseEvent.BUTTON3){
					if(console.getPlayState() != console.PS_PAUSED && console.getPlayState() == console.PS_PLAYING)console.pressPause();
					woundPortrayalDraw.clearWoundRegionCoordinates();
					woundPortrayalDraw.closeWoundRegionPath(false);
					activateDrawing = true;
				}
				
			}
			public void mouseReleased(MouseEvent e) {

				if(e.getButton() == MouseEvent.BUTTON3){
					if(console.getPlayState() == console.PS_PAUSED)console.pressPause();
					woundPortrayalDraw.closeWoundRegionPath(true);
					((Epidermis) state).removeCells(woundPortrayalDraw.getWoundRegion());
					activateDrawing = false;
				}
				
			}

			});
		display.insideDisplay.addMouseMotionListener(new MouseMotionAdapter(){
			public void mouseDragged(MouseEvent e){
				
				if(activateDrawing){
					woundPortrayalDraw.addMouseCoordinate(new Double2D(e.getX(), e.getY()));
					display.insideDisplay.repaint();
				}
				
			}
		});
		
		
		
		// display.setBackdrop(Color.white);
		

		displayFrame = display.createInternalFrame();
		maximizeWorkaround(displayFrame);
		displayFrame.addComponentListener(new ComponentAdapter(){
			 public void componentResized (ComponentEvent e) 
          {
    	      
				 if(console.getPlayState() == console.PS_PAUSED && resizeButtonIsActionSource)console.pressPause();
				 resizeButtonIsActionSource = false;
          }
		});
		displayFrame.setTitle("Epidermis Simulation v1.1");
		displayFrame.setName(SIMULATIONFRAME);
		displayFrame.setMaximizable(true);
		displayFrame.setIconifiable(true);
		displayFrame.setResizable(true);
		displayFrame.setVisible(true);
		displayFrame.setFrameIcon(null);
		
		desktop.add(displayFrame);

		// --------------------------------------------------------------------------
		// Internal Frames for the different available charts
		// --------------------------------------------------------------------------
		
		// Chart Kinetics
		
		desktop.add(getChartInternalFrame(DefaultCharts.getInstance().getKineticsChart(), "Kinetics Statistics"));

		// Num Cells Chart
		
		desktop.add(getChartInternalFrame(DefaultCharts.getInstance().getNumCellsChart(), "Cell Type Statistics"));

		// Epidermis Barrier Dist chart
		desktop.add(getChartInternalFrame(DefaultCharts.getInstance().getBarrierChart(), "Barrier Statistics"));

		// Apoptosis Chart
		desktop.add(getChartInternalFrame(DefaultCharts.getInstance().getApoptosisChart(), "Apoptosis Statistics"));

		// Particel Celltype chart
		desktop.add(getChartInternalFrame(DefaultCharts.getInstance().getParticleCellTypeChart(), "Particles per Cell Type"));

		// Particel Dist chart
		desktop.add(getChartInternalFrame(DefaultCharts.getInstance().getParticleDistribution(), "Particles per Depth"));

		// age dist chart
		desktop.add(getChartInternalFrame(DefaultCharts.getInstance().getAgeDistribution(), "Age Statistics"));
		
		//performance chart
		desktop.add(getChartInternalFrame(DefaultCharts.getInstance().getPerformanceChart(), "Performance"));
		
		desktop.putClientProperty("JDesktopPane.dragMode", "outline");
		
		//desktop.setPreferredSize(new Dimension((int) java.awt.Toolkit.getDefaultToolkit().getScreenSize().getWidth(),
			//	(int) java.awt.Toolkit.getDefaultToolkit().getScreenSize().getHeight() - 30));
		
	
		JScrollPane desktopScroll = new JScrollPane(desktop);
		
		desktop.setPreferredSize(new Dimension(
				(int)(mainFrame.getContentPane().getWidth()-desktopScroll.getVerticalScrollBar().getPreferredSize().getWidth()),
				(int)(mainFrame.getContentPane().getHeight()-desktopScroll.getHorizontalScrollBar().getPreferredSize().getHeight())));
	   desktop.setSize(desktop.getPreferredSize());
		
		
		arrangeElements(desktop);
		
		mainFrame.getContentPane().add(desktopScroll, BorderLayout.CENTER);
		
		
		
		
		mainFrame.pack();
		final JDesktopPane desktopFinal = desktop;
		mainFrame.addComponentListener(new ComponentAdapter(){
			public void componentResized(ComponentEvent comp) {
				
				Object obj;
				if((obj = comp.getSource()) instanceof JFrame){
					
					JFrame frame = (JFrame) obj;
					if(desktopFinal != null) arrangeElements(desktopFinal);
			

				}

			}
		});
		
		registerInternalFrames(desktop, ((EpiConsole)c));
		
		
	}
	
	private void registerInternalFrames(JDesktopPane desktop, EpiConsole c){
		Component[] comps = desktop.getComponents();
		for(Component comp: comps){
			if(comp instanceof JInternalFrame &&
					((JInternalFrame) comp).getName().equals(CHARTFRAME))c.registerFrame(((JInternalFrame)comp));
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

	private JInternalFrame getChartInternalFrame(JFreeChart chart, String title) {

		ChartPanel chartPanel = new ChartPanel(chart);

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
	private void arrangeElements(JComponent comp) {
		final int RANDUNTEN = 0;	
		Dimension screenDim; 
		screenDim= comp.getPreferredSize();
		comp.setPreferredSize(screenDim);
		
		comp.getComponentCount();
		int corrNumber = comp.getComponentCount() - 2;
		int remainder = 0;
		if((remainder = corrNumber % INTERNALFRAMECOLS) != 0)
			corrNumber += (INTERNALFRAMECOLS - remainder);
		int framesPerCol = corrNumber / INTERNALFRAMECOLS;

		int xDeltaSim = ((int) screenDim.getWidth()) / 2;

		int yDeltaSim = ((int) screenDim.getHeight() - RANDUNTEN)*2 / 3;
		int xDeltaChart = 0;
		if(((int) screenDim.getWidth()  - xDeltaSim) > 0 && INTERNALFRAMECOLS >0)
			xDeltaChart= ((int) screenDim.getWidth() - xDeltaSim) / INTERNALFRAMECOLS;
		int yDeltaChart = 0; 
		if(framesPerCol > 0 && ((int) screenDim.getHeight() - RANDUNTEN)>0) yDeltaChart =((int) screenDim.getHeight() - RANDUNTEN) / framesPerCol;

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
					((JInternalFrame) actComp).setPreferredSize(new Dimension(xDeltaSim, ((int) screenDim.getHeight() - RANDUNTEN - yDeltaSim)));
					((JInternalFrame) actComp).setLocation(0, yDeltaSim);
							

				}
				((JInternalFrame) actComp).pack();
			}

		}

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
	 					
	 				}
	    			 
	    		 });
	    	 }
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
	

}
