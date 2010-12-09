package sim.app.episim.devBasalLayer;


import sim.engine.*;

import sim.app.episim.gui.EpiDisplay2D;
import sim.app.episim.model.controller.CellBehavioralModelController;
import sim.app.episim.tissue.Epidermis;
import sim.app.episim.tissue.TissueBorder;
import sim.app.episim.tissue.TissueController;
import sim.app.episim.visualization.BasementMembranePortrayal2D;
import sim.app.episim.visualization.GridPortrayal2D;
import sim.app.episim.visualization.RulerPortrayal2D;

import sim.display.*;
import sim.portrayal.continuous.*;


import javax.swing.*;

import java.awt.*;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;



import javax.swing.JFrame;
import javax.swing.plaf.basic.BasicInternalFrameUI;



public class EpidermisUIDev extends GUIState{

	private EpiDisplay2D display;

	public JInternalFrame displayFrame;

	private JFrame mainFrame;

	private final int INTERNALFRAMECOLS = 2;

	private final String SIMULATIONFRAME = "Simframe";

	private final String CONTROLLERFRAME = "controllerFrame";

	private final String CHARTFRAME = "chartFrame";
	
	private final String BASEMENTMEMBRANENAME = "Basement Membrane";
	private final String RULERNAME = "Ruler";
	private final String GRIDNAME = "Grid";

	private JDesktopPane desktop;

	private EpiConsoleDev console;
	
	private static final double INITIALZOOMFACTOR = 2;
	private final double EPIDISPLAYWIDTH = TissueController.getInstance().getTissueBorder().getWidth() * INITIALZOOMFACTOR;
	private final double EPIDISPLAYHEIGHT = TissueController.getInstance().getTissueBorder().getHeight()* INITIALZOOMFACTOR;
	
	private  BasementMembranePortrayal2D basementPortrayalDraw;
	private  RulerPortrayal2D rulerPortrayalDraw;
	private  GridPortrayal2D gridPortrayalDraw;
	
	private boolean resizeButtonIsActionSource = false;
	
	private boolean movingCellPoint = false;
	
	private static final int DISPLAYBORDER = 40;
	
	public Object getSimulationInspectedObject() {

		return new Object();
	} // non-volatile

	
	ContinuousPortrayal2D basementPortrayal = new ContinuousPortrayal2D();
	ContinuousPortrayal2D rulerPortrayal = new ContinuousPortrayal2D();
	ContinuousPortrayal2D gridPortrayal = new ContinuousPortrayal2D();

	
	
	public EpidermisUIDev(JFrame mainFrame) {
		
		this(new EpidermisDev(System.currentTimeMillis()), mainFrame, false);
		
	}

	public EpidermisUIDev(SimState state, JFrame mainFrame, boolean reloadSnapshot) {
		
		super(state);
		this.mainFrame = mainFrame;
		this.setConsole(new EpiConsoleDev(this, false));
		basementPortrayalDraw =new BasementMembranePortrayal2D(EPIDISPLAYWIDTH+ (2*DISPLAYBORDER), EPIDISPLAYHEIGHT+ (2*DISPLAYBORDER), DISPLAYBORDER);
		rulerPortrayalDraw =new RulerPortrayal2D(EPIDISPLAYWIDTH + (2*DISPLAYBORDER), EPIDISPLAYHEIGHT+ (2*DISPLAYBORDER), DISPLAYBORDER, INITIALZOOMFACTOR);
		gridPortrayalDraw =new GridPortrayal2D(EPIDISPLAYWIDTH + (2*DISPLAYBORDER), EPIDISPLAYHEIGHT+ (2*DISPLAYBORDER), DISPLAYBORDER, INITIALZOOMFACTOR);
		setupPortrayals();
	}

	public void setConsole(EpiConsoleDev cons) {

		console = cons;

		final JInternalFrame controllerFrame = new JInternalFrame("EpiSimulation-Controller", true, false, true, true);

		controllerFrame.setContentPane(cons.getControllerContainer());
		controllerFrame.setResizable(true);
		controllerFrame.setVisible(true);
		controllerFrame.setName(CONTROLLERFRAME);
		controllerFrame.setFrameIcon(null);
		desktop.add(controllerFrame);
		this.arrangeElements(desktop);
		
		
		

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

		EpidermisDev theEpidermis = (EpidermisDev) state;
		
		basementPortrayal.setField(theEpidermis.getBasementContinous2D());
		rulerPortrayal.setField(theEpidermis.getRulerContinous2D());
		gridPortrayal.setField(theEpidermis.getGridContinous2D());
		
		
		
		// make the flockers random colors and four times their normal size
		// (prettier)
		java.awt.Color myColor = java.awt.Color.lightGray;

		
		
		
		basementPortrayal.setPortrayalForAll(basementPortrayalDraw);
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

		display = new EpiDisplay2D(EPIDISPLAYWIDTH + (2*DISPLAYBORDER), EPIDISPLAYHEIGHT+(2*DISPLAYBORDER), this, 1);
		//display.setClipping(false);
		Color myBack = new Color(0xE0, 0xCB, 0xF6);
		display.setBackdrop(Color.BLACK);
	
		
		display.attach(basementPortrayal, BASEMENTMEMBRANENAME);
		display.attach(rulerPortrayal, RULERNAME);
		display.attach(gridPortrayal, GRIDNAME);
		
		
		
		
		
		display.getInsideDisplay().addMouseListener(new MouseAdapter(){			

			public void mousePressed(MouseEvent e) {

				if(e.getButton() == MouseEvent.BUTTON1){
					//if(console.getPlayState() != console.PS_PAUSED && console.getPlayState() == console.PS_PLAYING)console.pressPause();
					if(basementPortrayalDraw.getCellPoint(new Point2D.Double(e.getX(), e.getY())))
						movingCellPoint = true;
				}
				
			}
			public void mouseReleased(MouseEvent e) {

				if(e.getButton() == MouseEvent.BUTTON1){
					//if(console.getPlayState() == console.PS_PAUSED)console.pressPause();
				
					movingCellPoint = false;
					basementPortrayalDraw.setHitAndButtonPressed(false);
				}
				
			}
			
			public void mouseEntered(MouseEvent e){
				if(display.isPortrayalVisible(RULERNAME)){ 
					rulerPortrayalDraw.setCrosshairsVisible(true);
					rulerPortrayalDraw.setActMousePosition(new Point2D.Double(e.getX(), e.getY()));
				}
			}
			public void mouseExited(MouseEvent e){
				if(display.isPortrayalVisible(RULERNAME)) rulerPortrayalDraw.setCrosshairsVisible(false);
			}
			
			public void mouseClicked(MouseEvent e) {
				int result = -1;
				if(e.getButton() == MouseEvent.BUTTON1 && !movingCellPoint){
					//if(console.getPlayState() != console.PS_PAUSED && console.getPlayState() == console.PS_PLAYING) console.pressPause();
								
				   	basementPortrayalDraw.addCellPoint(new Point2D.Double(e.getX(), e.getY()));	
				   	
				  
				  //if(console.getPlayState() == console.PS_PAUSED)	console.pressPause();
					
				
				}
				
			}

			});
		display.getInsideDisplay().addMouseMotionListener(new MouseMotionAdapter(){
			public void mouseDragged(MouseEvent e){
				
				if(movingCellPoint){
					basementPortrayalDraw.getCellPoint(new Point2D.Double(e.getX(), e.getY()));
				}
				if(display.isPortrayalVisible(RULERNAME)) rulerPortrayalDraw.setActMousePosition(new Point2D.Double(e.getX(), e.getY()));
				if(console.getPlayState()==Console.PS_PAUSED
						||console.getPlayState()==Console.PS_STOPPED) redrawDisplay();	
			}
			public void mouseMoved(MouseEvent e){
				if(display.isPortrayalVisible(RULERNAME)) rulerPortrayalDraw.setActMousePosition(new Point2D.Double(e.getX(), e.getY()));
				if(console.getPlayState()==Console.PS_PAUSED
						||console.getPlayState()==Console.PS_STOPPED) redrawDisplay();			
			}
			
		});
		
		
		
		// display.setBackdrop(Color.white);
		

		displayFrame = display.createInternalFrame();
		maximizeWorkaround(displayFrame);
		displayFrame.addComponentListener(new ComponentAdapter(){
			 public void componentResized (ComponentEvent e) 
          {
    	      
				 if(console !=null && console.getPlayState() == console.PS_PAUSED && resizeButtonIsActionSource)console.pressPause();
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

		
		
		desktop.putClientProperty("JDesktopPane.dragMode", "outline");
		
		desktop.setPreferredSize(new Dimension((int) java.awt.Toolkit.getDefaultToolkit().getScreenSize().getWidth(),
				(int) java.awt.Toolkit.getDefaultToolkit().getScreenSize().getHeight() - 30));
		
		arrangeElements(desktop);
		
		mainFrame.getContentPane().add(desktop, BorderLayout.CENTER);
		
		mainFrame.pack();
		mainFrame.addComponentListener(new ComponentAdapter(){
			public void componentResized(ComponentEvent comp) {

				Object obj;
				if((obj = comp.getSource()) instanceof JFrame){
					JFrame frame = (JFrame) obj;
					if(frame.getContentPane().getComponentCount() > 0
							&& frame.getContentPane().getComponent(0) instanceof JDesktopPane){
						JDesktopPane tempPane = (JDesktopPane) frame.getContentPane().getComponent(0);
					//	tempPane.setPreferredSize(frame.getSize());
						arrangeElements(tempPane);
					}

				}

			}
		});
		
		registerInternalFrames(desktop, ((EpiConsoleDev)c));
		
		
	}
	
	private void registerInternalFrames(JDesktopPane desktop, EpiConsoleDev c){
		Component[] comps = desktop.getComponents();
		for(Component comp: comps){
			if(comp instanceof JInternalFrame &&
					((JInternalFrame) comp).getName().equals(CHARTFRAME))c.registerFrame(((JInternalFrame)comp));
		}
	}
	
	public void redrawDisplay(){
		
   	 Graphics g = display.getInsideDisplay().getGraphics();
   	 display.paintComponentInInnerDisplay(g,true);
   	 g.dispose();
   	 
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

	

	/**
	 * Ordnet die Internal Frames in der übergebenen Komponente an
	 * 
	 * @param comp
	 *           Komponente in der die Fenster angeordnet werden sollen
	 */
	private void arrangeElements(JComponent comp) {
		final int RANDUNTEN = 55;	
		Dimension screenDim = comp.getPreferredSize();
		comp.setPreferredSize(screenDim);
		comp.getComponentCount();
		int corrNumber = comp.getComponentCount() - 2;
		int remainder = 0;
		if((remainder = corrNumber % INTERNALFRAMECOLS) != 0)
			corrNumber += (INTERNALFRAMECOLS - remainder);
		int framesPerCol = corrNumber / INTERNALFRAMECOLS;

		int xDeltaSim = ((int) screenDim.getWidth() - 10) / 2;

		int yDeltaSim = ((int) screenDim.getHeight() - RANDUNTEN)*2 / 3;
		int xDeltaChart = 0;
		if(((int) screenDim.getWidth() - 10 - xDeltaSim) > 0 && INTERNALFRAMECOLS >0)
			xDeltaChart= ((int) screenDim.getWidth() - 10 - xDeltaSim) / INTERNALFRAMECOLS;
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

	
   public EpiDisplay2D getDisplay() {
   
   	return display;
   }

	
	

}