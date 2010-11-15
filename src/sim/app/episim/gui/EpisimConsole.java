package sim.app.episim.gui;



import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Insets;
import java.awt.MenuBar;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import episiminterfaces.SimulationConsole;

import sim.SimStateServer;
import sim.app.episim.EpisimProperties;
import sim.app.episim.SimulationStateChangeListener;
import sim.app.episim.datamonitoring.charts.ChartController;
import sim.app.episim.datamonitoring.charts.DefaultCharts;
import sim.app.episim.model.CellBehavioralModelController;
import sim.app.episim.model.MiscalleneousGlobalParameters;
import sim.app.episim.model.ModelController;
import sim.app.episim.nogui.NoGUIConsole;
import sim.app.episim.snapshot.SnapshotWriter;
import sim.app.episim.util.Names;
import sim.display.Console;
import sim.display.ConsoleHack;
import sim.display.GUIState;
import sim.portrayal.SimpleInspector;
import sim.util.gui.NumberTextField;
import sim.util.gui.PropertyField;

public class EpisimConsole implements ActionListener, SimulationStateChangeListener{
	private Container controllerContainer;
	
	private KeyListener keyListener;

	private FocusAdapter focusAdapter;
	private JButton resetButton;
	private List<JButton> refreshButtons;
	private JButton snapshotButton;
	private final static String RESETTEXT = "Reset";
	private boolean reloadedSnapshot = false;
	private ArrayList<SnapshotRestartListener> snapshotRestartListener;
	
	private boolean wasStartedOnce = false;
	
	private SimulationConsole console = null;
	private boolean guiMode;
	private boolean consoleInput = false;
	
	public EpisimConsole(final GUIState simulation, boolean reloadSnapshot){
		consoleInput =  (EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CONSOLE_INPUT_PROP) != null 
				&& EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CONSOLE_INPUT_PROP).equals(EpisimProperties.ON_CONSOLE_INPUT_VAL));
		guiMode = ((EpisimProperties.getProperty(EpisimProperties.SIMULATOR_GUI_PROP) != null 
				&& EpisimProperties.getProperty(EpisimProperties.SIMULATOR_GUI_PROP).equals(EpisimProperties.ON_SIMULATOR_GUI_VAL) && consoleInput) 
				|| (EpisimProperties.getProperty(EpisimProperties.SIMULATOR_GUI_PROP)== null));	
		
		if(simulation instanceof EpidermisGUIState)((EpidermisGUIState)simulation).addSimulationStateChangeListener(this);
		 if(guiMode){
			 console = new ConsoleHack(simulation){
				 public synchronized void pressPlay(){
				   	if(!reloadedSnapshot){
				   		
				   		EpisimTextOut.getEpisimTextOut().clear();
				      	
				   		((EpidermisGUIState)console.getSimulation()).clearWoundPortrayalDraw();
				   		
				   	}
				   	else if(wasStartedOnce && reloadedSnapshot){
				   		notifyAllSnapshotRestartListeners();
				   		return;
				   	}
				   	
				   	wasStartedOnce = true;  
				   	((EpidermisGUIState)console.getSimulation()).simulationWasStarted();
				   	 super.pressPlay(reloadedSnapshot);
				   	   	
				   	 	   	
				   }
				   
				   
				   
				   public synchronized void pressStop(){
				   
				   	((EpidermisGUIState)console.getSimulation()).simulationWasStopped();
				   	
				      	
				   	super.pressStop();
				   }
				   
				   public synchronized void pressPause(){
				      
				   	((EpidermisGUIState)console.getSimulation()).simulationWasPaused();
				   	
				      	
				   	super.pressPause();
				   }
			 };
			 controllerContainer = getControllerContainer(((ConsoleHack)console).getContentPane());
		 }
		 else{ 
			 console = new NoGUIConsole(simulation);
			 controllerContainer = getControllerContainer((NoGUIConsole) console);
		 }
		 
		
		 refreshButtons = new ArrayList<JButton>();
		 snapshotRestartListener = new ArrayList<SnapshotRestartListener>();
		 
		 
		changeDisplaysTab();
		
		 keyListener = new KeyListener()
      {
      public void keyReleased(KeyEvent keyEvent) { }
      public void keyTyped(KeyEvent keyEvent) { }
      public void keyPressed(KeyEvent keyEvent) 
      {
          if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER)
              {
        	  
              if(keyEvent.getSource() instanceof JTextField){
              String name =((JTextField) keyEvent.getSource()).getName();
              //	ModelController.getInstance().getCellBehavioralModelController().reloadValue(());
              	if(name.equals("TypeColor")) clickRefreshButtons();
              }
              }
          }
      };
      
      focusAdapter = new FocusAdapter()
      {
      public void focusLost ( FocusEvent e )
          {
     	 if(e.getSource() instanceof JTextField){
     		 String name =((JTextField) e.getSource()).getName();
     		// ModelController.getInstance().getCellBehavioralModelController().reloadValue((name=));
         	if(name.equals("TypeColor")) clickRefreshButtons();
         }
          }
      };
		
      //Liste der Frames überschreiben
      
      console.getFrameListDisplay().setCellRenderer(new ListCellRenderer()
          {
          // this ListCellRenderer will show the frame titles in black if they're
          // visible, and show them as gray if they're hidden.  You can add frames
          // to this list by calling the registerFrame() method.
          protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
          public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
              {
              JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
             
              JInternalFrame frame = (JInternalFrame) value;
              
              if (frame.isVisible())
                  renderer.setForeground(Color.black);
              else
                  renderer.setForeground(Color.gray);
             
              renderer.setText(frame.getTitle());
             
              return renderer;
              }
          });
		
      snapshotButton = new JButton();
      snapshotButton.setContentAreaFilled( false );
      snapshotButton.setBorderPainted( false );
      snapshotButton.setFocusPainted( true );
      snapshotButton.addMouseListener(new MouseAdapter(){
			public void mouseEntered(MouseEvent e) {
 			    snapshotButton.setContentAreaFilled( true );
		       snapshotButton.setBorderPainted( true );
				
			}
			public void mouseExited(MouseEvent e) {

				 snapshotButton.setContentAreaFilled( false );
		       snapshotButton.setBorderPainted( false );
				
			}
      });
      snapshotButton.setIcon(new ImageIcon(ImageLoader.class.getResource("Camera.png")));
      snapshotButton.setPressedIcon(new ImageIcon(ImageLoader.class.getResource("CameraPressed.png")));
      snapshotButton.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));
     
      snapshotButton.setEnabled(false);
      
      snapshotButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				simulation.state.preCheckpoint();
				boolean snapshotPathWasNull = false;
				if(getPlayState() != ConsoleHack.PS_PAUSED && getPlayState() == ConsoleHack.PS_PLAYING) pressPause();
							 
				SnapshotWriter.getInstance().writeSnapshot();
				if(getPlayState() == ConsoleHack.PS_PAUSED && getPlayState() != ConsoleHack.PS_PLAYING)pressPause(); 
				simulation.state.postCheckpoint();
			}
     	 
      });
     addSnapshotButton();
     
	}
	
	
	private Container getControllerContainer(Container con){
		if(con.getName() != null && con.getName().equals(Names.CONSOLE_MAIN_CONTAINER)) return con;
		else{
		 for(Component comp : con.getComponents()){
			 if(comp.getName() != null && comp.getName().equals(Names.CONSOLE_MAIN_CONTAINER) && comp instanceof Container) return (Container)comp;
			 else if(comp instanceof Container) return getControllerContainer(((Container)comp));
		 }
		 return null;
		}
		
	}

   /** Simulations can call this to add a frame to be listed in the "Display list" of the console */
   public synchronized boolean registerFrame(JInternalFrame frame)
   {
   	console.getFrameList().add(frame);
   	console.getFrameListDisplay().setListData(console.getFrameList());
   	return true;
   }
   
   public synchronized boolean deregisterAllFrames()
   {
   	console.getFrameList().clear();
   	console.getFrameListDisplay().setListData(console.getFrameList());
   	return true;
   }
	
	
	public Container getControllerContainer(){
		return controllerContainer;
	}
	
	private void changeDisplaysTab(){
		Object obj=null;
		JTabbedPane tabPane;
		ArrayList<JButton> buttonsDisplayTab = new ArrayList<JButton>();
		for(int i = 0; i<controllerContainer.getComponentCount(); i++){
			if((obj=controllerContainer.getComponent(i)) instanceof JTabbedPane) break;
		}
		
		for(int i=0; i< (tabPane= ((JTabbedPane) obj)).getComponentCount(); i++){
			if (tabPane.getTitleAt(i).equals("Displays")){
				
				Component  comp = tabPane.getComponentAt(i);
				
					if(comp instanceof Container){
						getButtons(((Container)comp), buttonsDisplayTab);
					}
				
			}
		}
		Iterator<JButton> iter = buttonsDisplayTab.iterator();
		JButton button = null;
		while(iter.hasNext()){
			button = iter.next();
			ActionListener [] listeners = button.getActionListeners();
			for(ActionListener listener: listeners){
				button.removeActionListener(listener);
			}
			button.addActionListener(this);
		}
		
	}
	
	
	public void addActionListenersAndResetButtons(){
		Object obj=null;
		JTabbedPane tabPane;
		for(int i = 0; i<controllerContainer.getComponentCount(); i++){
			if((obj=controllerContainer.getComponent(i)) instanceof JTabbedPane) break;
			
		}
		for(int i=0; i< (tabPane= ((JTabbedPane) obj)).getTabCount(); i++){
			if (tabPane.getTitleAt(i).equals(Names.BIOCHEM_MODEL) || tabPane.getTitleAt(i).equals(Names.MECH_MODEL) || tabPane.getTitleAt(i).equals(Names.MISCALLENEOUS)){
				String actionString = null;
				if (tabPane.getTitleAt(i).equals(Names.BIOCHEM_MODEL)) actionString = Names.BIOCHEM_MODEL;
				else if (tabPane.getTitleAt(i).equals(Names.MECH_MODEL)) actionString = Names.MECH_MODEL;
				else if (tabPane.getTitleAt(i).equals(Names.MISCALLENEOUS)) actionString = Names.MISCALLENEOUS;
				Component comp = tabPane.getComponentAt(i); 
				if(comp instanceof Container){ 
					SimpleInspector inspector = findSimpleInspector(((Container)comp));
					if(inspector != null){
					
						addActionListenersToTextFields(inspector);
						Component pan;
						if((pan= inspector.header) instanceof JPanel){ 
						addResetButton(((JPanel) pan), actionString);								
						}
					}
				}
			}
		}
	}
	
	private SimpleInspector findSimpleInspector(Container cont){
		
		for(int i = 0; i<cont.getComponentCount(); i++){
		if(cont.getComponent(i) instanceof Container &&
				!(cont.getComponent(i) instanceof SimpleInspector)){ 
			return findSimpleInspector(((Container)cont.getComponent(i)));
			
		}
		else if(cont.getComponent(i) instanceof SimpleInspector) return (SimpleInspector) cont.getComponent(i);
		}
		return null;
	}
	
	private void getButtons(Container root, ArrayList<JButton> buttons){
		Component [] comps = root.getComponents();
		for(Component comp: comps){
			if(comp instanceof JButton) buttons.add(((JButton) comp));
			else if(comp instanceof Container) getButtons(((Container) comp), buttons);
		}
	}
	
	
	private void addActionListenersToTextFields(SimpleInspector comp) {
		if(comp.startField != null){
			for(Component compo : comp.startField.getComponents()){
	
				if(compo instanceof NumberTextField){
					((NumberTextField) compo).bellyButton.removeActionListener(this);
					((NumberTextField) compo).downButton.removeActionListener(this);
					((NumberTextField) compo).upButton.removeActionListener(this);
					((NumberTextField) compo).bellyButton.addActionListener(this);
					((NumberTextField) compo).downButton.addActionListener(this);
					((NumberTextField) compo).upButton.addActionListener(this);
	
				}
			}
		}
		PropertyField field = null;
		if(comp.members != null){
			for(int n = 0; n < comp.members.length; n++){
				field = comp.members[n];
				if(field != null && field.valField != null){
					field.valField.addKeyListener(keyListener);
					field.valField.setName(comp.properties.getName(n));
					field.valField.addFocusListener(focusAdapter);
				}
			}
		}

	}
		
	private void addResetButton(JPanel inspectorHeader, String actionString){
		JPanel buttonPanel = new JPanel(new BorderLayout(10, 0));
		JButton refreshButton = null;
		Component [] comps = inspectorHeader.getComponents();
		resetButton = new JButton(RESETTEXT);
		resetButton.setActionCommand(actionString);
		for(Component comp :comps){
			if(comp instanceof JButton){
				
				inspectorHeader.remove(comp);
				refreshButton = (JButton) comp;
				
				
			}
			else if(comp.getName()!= null && comp.getName().equals("ResetRefreshPanel")){
				inspectorHeader.remove(comp);
				
				for(Component comp2: ((JPanel)comp).getComponents()){
				 if(comp2 instanceof JButton && ((JButton)comp2).getText() != null
						 &&!((JButton)comp2).getText().equals(RESETTEXT)) 
					 refreshButton = (JButton) comp2;
				 
				}
				
			}
			
		}
		refreshButtons.add(refreshButton);
		buttonPanel.add(refreshButton, BorderLayout.WEST);
		buttonPanel.add(resetButton, BorderLayout.EAST);
		buttonPanel.setName("ResetRefreshPanel");
		resetButton.addActionListener(this);
		
		inspectorHeader.add(buttonPanel, BorderLayout.WEST);
	}
	
	public void actionPerformed(ActionEvent e) {

		if(e.getSource() instanceof JButton){
			JButton pressedButton = (JButton) e.getSource();
			if(pressedButton.getText().equalsIgnoreCase("Show")){
				pressShow();
			}
			else if(pressedButton.getText().equalsIgnoreCase("Show All")){
				pressShowAll();
			}
			else if(pressedButton.getText().equalsIgnoreCase("Hide")){
				pressHide();
			}
			else if(pressedButton.getText().equalsIgnoreCase("Hide All")){
				pressHideAll();
			}
			else if(pressedButton.getText().equalsIgnoreCase(RESETTEXT)){
				
				if(console.getPlayState() == ConsoleHack.PS_PLAYING) console.pressPause();
				if(pressedButton.getActionCommand() != null){
					if(pressedButton.getActionCommand().equals(Names.BIOCHEM_MODEL))ModelController.getInstance().getCellBehavioralModelController().resetInitialGlobalValues();
					else if(pressedButton.getActionCommand().equals(Names.MECH_MODEL))ModelController.getInstance().getBioMechanicalModelController().resetInitialGlobalValues();
					else if(pressedButton.getActionCommand().equals(Names.MISCALLENEOUS))MiscalleneousGlobalParameters.instance().resetInitialGlobalValues();
				}
				this.clickRefreshButtons();
				if(console.getPlayState() == ConsoleHack.PS_PAUSED)console.pressPause();
				
			}
			else{

			SwingUtilities.invokeLater(new Runnable() {

				public void run() {

					addActionListenersAndResetButtons();
				}
			});
			}
		}
	}
	
	
	
	
	
	
// ///////////////////// SHOW/HIDE DISPLAY BUTTON FUNCTIONS

   /** Called when the "show" button is pressed in the Displays window */
   synchronized void pressShow()
   {
       Object[] vals = (Object[]) (console.getFrameListDisplay().getSelectedValues());
       for (int x = 0; x < vals.length; x++)
           {
           ((JInternalFrame) (vals[x])).toFront();
           ((JInternalFrame) (vals[x])).setVisible(true);
           }
       console.getFrameListDisplay().repaint();
   }

   /** Called when the "show all" button is pressed in the Displays window */
   synchronized void pressShowAll()
   {
       Object[] vals = (Object[]) (console.getFrameList().toArray());
       for (int x = 0; x < vals.length; x++)
           {
           ((JInternalFrame) (vals[x])).toFront();
           ((JInternalFrame) (vals[x])).setVisible(true);
           }
       console.getFrameListDisplay().repaint();
   }

   /** Called when the "hide" button is pressed in the Displays window */
   synchronized void pressHide()
   {
       Object[] vals = (Object[]) (console.getFrameListDisplay().getSelectedValues());
       for (int x = 0; x < vals.length; x++)
           {
           ((JInternalFrame) (vals[x])).setVisible(false);
           }
       console.getFrameListDisplay().repaint();
   }

   /** Called when the "hide all" button is pressed in the Displays window */
   synchronized void pressHideAll()
   {
       Object[] vals = (Object[]) (console.getFrameList().toArray());
       for (int x = 0; x < vals.length; x++)
           {
           ((JInternalFrame) (vals[x])).setVisible(false);
           }
       console.getFrameListDisplay().repaint();
   }
	
 
   
   public synchronized void pressPlay(){
   	
	   	if(!reloadedSnapshot){
	   		
	   		EpisimTextOut.getEpisimTextOut().clear();
	      	
	   		((EpidermisGUIState)console.getSimulation()).clearWoundPortrayalDraw();
	   		
	   	}
	   	else if(wasStartedOnce && this.reloadedSnapshot){
	   		notifyAllSnapshotRestartListeners();
	   		return;
	   	}
	   	
	   	wasStartedOnce = true;  
	   	((EpidermisGUIState)console.getSimulation()).simulationWasStarted();
	    snapshotButton.setEnabled(true);	
   	 console.pressPlay(reloadedSnapshot);
   	   	
   	 	   	
   }
   
   
   
   public synchronized void pressStop(){
   	if(console instanceof NoGUIConsole){
   		((EpidermisGUIState)console.getSimulation()).simulationWasStopped();
   	}
   	snapshotButton.setEnabled(false);
      	
   	console.pressStop();
   }
   
   public synchronized void pressPause(){
   	if(console instanceof NoGUIConsole){
   		((EpidermisGUIState)console.getSimulation()).simulationWasPaused();
   	
   	}
   	console.pressPause();
   }
   
   public int getPlayState(){ return console.getPlayState(); }
   
   public void doClose(){
   	console.doClose();
   }
   
   public void setWhenShouldEnd(long val){
   	console.setWhenShouldEnd(val);
   }
   
   private void addSnapshotButton(){
   	Container mainContainer = null;
   	if(guiMode) mainContainer = ((ConsoleHack)console).getContentPane();
   	else mainContainer = (NoGUIConsole) console;
   	
   	
   	if(mainContainer.getLayout() instanceof BorderLayout){
   		final Component comp =((BorderLayout) mainContainer.getLayout()).getLayoutComponent(BorderLayout.SOUTH);
   		if(comp != null && comp instanceof Box){
   			((Box) comp).add(snapshotButton, 3);
   			((JButton)((Box) comp).getComponent(0)).addMouseListener(new MouseAdapter(){
   				public void mouseEntered(MouseEvent e) {
   					((JButton)((Box) comp).getComponent(0)).setContentAreaFilled( true );
   					((JButton)((Box) comp).getComponent(0)).setBorderPainted( true );
   					
   				}
   				public void mouseExited(MouseEvent e) {

   					((JButton)((Box) comp).getComponent(0)).setContentAreaFilled( false );
   					((JButton)((Box) comp).getComponent(0)).setBorderPainted( false );
   					
   				}
   	       });
   			((JButton)((Box) comp).getComponent(1)).addMouseListener(new MouseAdapter(){
   				public void mouseEntered(MouseEvent e) {
   					((JButton)((Box) comp).getComponent(1)).setContentAreaFilled( true );
   			       ((JButton)((Box) comp).getComponent(1)).setBorderPainted( true );
   					
   				}
   				public void mouseExited(MouseEvent e) {

   					((JButton)((Box) comp).getComponent(1)).setContentAreaFilled( false );
   					((JButton)((Box) comp).getComponent(1)).setBorderPainted( false );
   					
   				}
   	       });
   			((JButton)((Box) comp).getComponent(2)).addMouseListener(new MouseAdapter(){
   				public void mouseEntered(MouseEvent e) {
   					((JButton)((Box) comp).getComponent(2)).setContentAreaFilled( true );
   					((JButton)((Box) comp).getComponent(2)).setBorderPainted( true );
   					
   				}
   				public void mouseExited(MouseEvent e) {

   					((JButton)((Box) comp).getComponent(2)).setContentAreaFilled( false );
   					((JButton)((Box) comp).getComponent(2)).setBorderPainted( false );
   					
   				}
   	       });
   		}
   	}
   }

   private void clickRefreshButtons(){
   	for(JButton refreshButton: refreshButtons) refreshButton.doClick();
   }
	
	public void setReloadedSnapshot(boolean reloadedSnapshot) {
	
		this.reloadedSnapshot = reloadedSnapshot;
	}
	public void addSnapshotRestartListener(SnapshotRestartListener listener){
		this.snapshotRestartListener.add(listener);
	}
	private void notifyAllSnapshotRestartListeners(){
		for(SnapshotRestartListener listener : this.snapshotRestartListener){
			listener.snapShotRestart();
		}
	}


	public void simulationWasStarted() {

	   snapshotButton.setEnabled(true);
	   
   }


	public void simulationWasPaused() {

	   // Do nothing
	   
   }


	public void simulationWasStopped() {

	  snapshotButton.setEnabled(false);
	   
   }
}
