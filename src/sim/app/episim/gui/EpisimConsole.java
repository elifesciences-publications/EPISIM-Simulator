package sim.app.episim.gui;



import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
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
import java.io.IOException;
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
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import episimexceptions.CompilationFailedException;
import episiminterfaces.SimulationConsole;

import sim.SimStateServer;
import sim.app.episim.EpisimProperties;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.ModeServer;
import sim.app.episim.SimulationStateChangeListener;
import sim.app.episim.datamonitoring.charts.ChartController;
import sim.app.episim.datamonitoring.charts.DefaultCharts;
import sim.app.episim.datamonitoring.dataexport.DataExportController;
import sim.app.episim.gui.EpisimProgressWindow.EpisimProgressWindowCallback;
import sim.app.episim.model.controller.CellBehavioralModelController;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters;
import sim.app.episim.nogui.NoGUIConsole;
import sim.app.episim.persistence.SimulationStateFile;
import sim.app.episim.util.Names;
import sim.display.Console;
import sim.display.ConsoleHack;
import sim.display.GUIState;
import sim.portrayal.SimpleInspector;
import sim.portrayal.SimpleInspectorHack;
import sim.util.gui.NumberTextField;
import sim.util.gui.PropertyField;

public class EpisimConsole implements ActionListener, SimulationStateChangeListener{
	private Container controllerContainer;
	
	private KeyListener keyListener;

	private FocusAdapter focusAdapter;
	private JButton resetButton;
	private List<JButton> refreshButtons;
	private JButton tissueExportButton;
	private JButton reloadModelButton;
	private final static String RESETTEXT = "Reset";	
	private SimulationConsole console = null;
	private Component mainGUIComponent = null; 
	
	private EpisimGUIState episimGUIState;
	
	private EpisimProgressWindow progressWindow;
	
	public EpisimConsole(final GUIState simulation, boolean reloadSnapshot){
		
		if(simulation instanceof EpisimGUIState){
			episimGUIState = ((EpisimGUIState)simulation);
			episimGUIState.addSimulationStateChangeListener(this);
			mainGUIComponent = episimGUIState.getMainGUIComponent();
		}
		 if(ModeServer.guiMode()){
			 console = new ConsoleHack(simulation){
				 public synchronized void pressPlay(){
					
				   	EpisimTextOut.getEpisimTextOut().clear();
				      ((EpisimGUIState)console.getSimulation()).clearWoundPortrayalDraw();
				   	((EpisimGUIState)console.getSimulation()).simulationWasStarted();
				    	EpisimConsole.this.disableConsoleButtons();
				   	super.pressPlay();
				   	EpisimConsole.this.enableConsoleButtons();
				   	EpisimConsole.this.reloadModelButton.setEnabled(false);
				   }			   
				   
				   public synchronized void pressStop(){
				   	
				   	((EpisimGUIState)console.getSimulation()).simulationWasStopped();
				   	EpisimConsole.this.disableConsoleButtons();
				      	
				   	super.pressStop();
				   	EpisimConsole.this.enableConsoleButtons();
				   	EpisimConsole.this.reloadModelButton.setEnabled(true);
				   }
				   
				   public synchronized void pressPause(){
				   
				   	((EpisimGUIState)console.getSimulation()).simulationWasPaused();
				   	EpisimConsole.this.disableConsoleButtons();
				      	
				   	super.pressPause();
				   	EpisimConsole.this.enableConsoleButtons();
				   	EpisimConsole.this.reloadModelButton.setEnabled(false);
				   }
			 };
			 controllerContainer = getControllerContainer(((ConsoleHack)console).getContentPane());
		 }
		 else{ 
			 console = new NoGUIConsole(simulation);
			 controllerContainer = getControllerContainer((NoGUIConsole) console);
		 }
		 
		 refreshButtons = new ArrayList<JButton>();	 
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
		
      tissueExportButton = new JButton();
      tissueExportButton.setContentAreaFilled( false );
      tissueExportButton.setBorderPainted( false );
      tissueExportButton.setFocusPainted( true );
      tissueExportButton.addMouseListener(new MouseAdapter(){
			public void mouseEntered(MouseEvent e) {
 			    tissueExportButton.setContentAreaFilled( true );
		       tissueExportButton.setBorderPainted( true );
				
			}
			public void mouseExited(MouseEvent e) {

				 tissueExportButton.setContentAreaFilled( false );
		       tissueExportButton.setBorderPainted( false );
				
			}
      });
      tissueExportButton.setIcon(new ImageIcon(ImageLoader.class.getResource("save-export.png")));
      tissueExportButton.setPressedIcon(new ImageIcon(ImageLoader.class.getResource("save-export-pressed.png")));
      tissueExportButton.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));
     
      tissueExportButton.setEnabled(false);
      
      tissueExportButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				simulation.state.preCheckpoint();
				
				episimGUIState.pressWorkaroundSimulationPause();
							 
				if(mainGUIComponent != null && mainGUIComponent instanceof JFrame && SimulationStateFile.getTissueExportPath() == null){					
					ExtendedFileChooser chooser = new ExtendedFileChooser(SimulationStateFile.FILEEXTENSION);
					if(ExtendedFileChooser.APPROVE_OPTION == chooser.showSaveDialog((JFrame)mainGUIComponent) && chooser.getSelectedFile() != null){
						SimulationStateFile.setTissueExportPath(chooser.getSelectedFile());	
						 if(ModeServer.guiMode()){
	                  try{
	                     ((JFrame)mainGUIComponent).setTitle(EpisimSimulator.getEpisimSimulatorTitle()+ "- Tissue-Export-Path: "+chooser.getSelectedFile().getCanonicalPath());
                     }
                     catch (IOException e1){
                     	 ExceptionDisplayer.getInstance().displayException(e1);
                     }
						 }
					}
				}				
			
				if(ModeServer.guiMode()){
					if(mainGUIComponent instanceof Frame){
					
						EpisimProgressWindowCallback cb = new EpisimProgressWindowCallback(){
							
							public void executeTask() {							
								saveSimulationState();					
		                }
							public void taskHasFinished(){								  			
						        episimGUIState.pressWorkaroundSimulationPlay(); 
								  simulation.state.postCheckpoint();
							}					
						};
						EpisimProgressWindow.showProgressWindowForTask((Frame)mainGUIComponent, "Writing simulation state to disk...", cb);						
					}
				}
				else{
					saveSimulationState();	
				}   		
			}
     	 
      });
      
      reloadModelButton = new JButton();
      reloadModelButton.setContentAreaFilled( false );
      reloadModelButton.setBorderPainted( false );
      reloadModelButton.setFocusPainted( true );
      reloadModelButton.addMouseListener(new MouseAdapter(){
			public void mouseEntered(MouseEvent e) {
				reloadModelButton.setContentAreaFilled( true );
				reloadModelButton.setBorderPainted( true );
				
			}
			public void mouseExited(MouseEvent e) {

				reloadModelButton.setContentAreaFilled( false );
				reloadModelButton.setBorderPainted( false );
				
			}
      });
      reloadModelButton.setIcon(new ImageIcon(ImageLoader.class.getResource("reload-model.png")));
      reloadModelButton.setPressedIcon(new ImageIcon(ImageLoader.class.getResource("reload-model-pressed.png")));
      reloadModelButton.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));
      reloadModelButton.addActionListener(new ActionListener(){
         public void actionPerformed(ActionEvent e) {
         	SimStateServer.getInstance().reloadCurrentlyLoadedModel();
         }});
      reloadModelButton.setEnabled(true);
      
      
      
     addTissueExportAndRelaodButton();
     
	}
	
	protected void disableConsoleButtons(){
		((ConsoleHack)this.console).disableConsoleButtons();
		tissueExportButton.setEnabled(false);
	}
	
	protected void enableConsoleButtons(){
		((ConsoleHack)this.console).enableConsoleButtons();
		tissueExportButton.setEnabled(true);
	}
	
	private void saveSimulationState(){
		  try{
			  (new SimulationStateFile()).saveData(false);							
		  }
        catch (ParserConfigurationException e1){
           ExceptionDisplayer.getInstance().displayException(e1);
        }
        catch (SAXException e1){
        	ExceptionDisplayer.getInstance().displayException(e1);
        }		
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
			if (tabPane.getTitleAt(i).equals(Names.BIOCHEM_MODEL) || tabPane.getTitleAt(i).equals(Names.MECH_MODEL) || tabPane.getTitleAt(i).equals(Names.MISCELLANEOUS)){
				String actionString = null;
				if (tabPane.getTitleAt(i).equals(Names.BIOCHEM_MODEL)) actionString = Names.BIOCHEM_MODEL;
				else if (tabPane.getTitleAt(i).equals(Names.MECH_MODEL)) actionString = Names.MECH_MODEL;
				else if (tabPane.getTitleAt(i).equals(Names.MISCELLANEOUS)) actionString = Names.MISCELLANEOUS;
				Component comp = tabPane.getComponentAt(i); 
				if(comp instanceof Container){ 
					SimpleInspectorHack inspector = findSimpleInspectorHack(((Container)comp));
					if(inspector != null){
					
						addActionListenersToTextFields(inspector);
						Component pan;
						if((pan= inspector.getHeader()) instanceof JPanel){ 
						addResetButton(((JPanel) pan), actionString);								
						}
					}
				}
			}
		}
	}
	
	private SimpleInspectorHack findSimpleInspectorHack(Container cont){
		
		for(int i = 0; i<cont.getComponentCount(); i++){
		if(cont.getComponent(i) instanceof Container &&
				!(cont.getComponent(i) instanceof SimpleInspectorHack)){ 
			return findSimpleInspectorHack(((Container)cont.getComponent(i)));
			
		}
		else if(cont.getComponent(i) instanceof SimpleInspectorHack) return (SimpleInspectorHack) cont.getComponent(i);
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
	
	
	private void addActionListenersToTextFields(SimpleInspectorHack comp) {
		
		if(comp.getStartField() != null){
			for(Component compo : comp.getStartField().getComponents()){
	
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
		if(comp.getMembers() != null){
			for(int n = 0; n < comp.getMembers().length; n++){
				field = comp.getMembers()[n];
				if(field != null && field.getField() != null){
					field.getField().addKeyListener(keyListener);
					field.getField().setName(comp.getProperties().getName(n));
					field.getField().addFocusListener(focusAdapter);
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
					else if(pressedButton.getActionCommand().equals(Names.MISCELLANEOUS))MiscalleneousGlobalParameters.getInstance().resetInitialGlobalValues();
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
   	
	   EpisimTextOut.getEpisimTextOut().clear();	      	
	   ((EpisimGUIState)console.getSimulation()).clearWoundPortrayalDraw();   	
	   	
	   ((EpisimGUIState)console.getSimulation()).simulationWasStarted();	  
   	console.pressPlay();
   	tissueExportButton.setEnabled(true);
   	reloadModelButton.setEnabled(false);
   }
   
   
   
   public synchronized void pressStop(){
   	if(console instanceof NoGUIConsole){
   		((EpisimGUIState)console.getSimulation()).simulationWasStopped();
   	}
   	tissueExportButton.setEnabled(false);      	
   	console.pressStop();
   	reloadModelButton.setEnabled(true);
   	
   }
   
   public synchronized void pressPause(){
   	if(console instanceof NoGUIConsole){
   		((EpisimGUIState)console.getSimulation()).simulationWasPaused();   	
   	}   	
   	console.pressPause();
   	tissueExportButton.setEnabled(true);
   	reloadModelButton.setEnabled(false);
   }
   
   public int getPlayState(){ return console.getPlayState(); }
   
   public void doClose(){
   	console.doClose();
   }
   
   public void setWhenShouldEnd(long val){
   	console.setWhenShouldEnd(val);
   }
   
   private void addTissueExportAndRelaodButton(){
   	Container mainContainer = null;
   	if(ModeServer.guiMode()) mainContainer = ((ConsoleHack)console).getContentPane();
   	else mainContainer = (NoGUIConsole) console;
   	
   	
   	if(mainContainer.getLayout() instanceof BorderLayout){
   		final Component comp =((BorderLayout) mainContainer.getLayout()).getLayoutComponent(BorderLayout.SOUTH);
   		if(comp != null && comp instanceof Box){
   			((Box) comp).add(reloadModelButton, 3);
   			((Box) comp).add(tissueExportButton, 4);
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
	
	


	public void simulationWasStarted() {

	   tissueExportButton.setEnabled(true);
	   reloadModelButton.setEnabled(false);
	   
   }


	public void simulationWasPaused() {

	   
		 tissueExportButton.setEnabled(true);
		 reloadModelButton.setEnabled(false);
   }


	public void simulationWasStopped() {

	  tissueExportButton.setEnabled(false);
	  reloadModelButton.setEnabled(true);
	   
   }
}
