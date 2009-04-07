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

import sim.app.episim.datamonitoring.charts.ChartController;
import sim.app.episim.datamonitoring.charts.DefaultCharts;
import sim.app.episim.model.BioChemicalModelController;
import sim.app.episim.model.MiscalleneousGlobalParameters;
import sim.app.episim.model.ModelController;
import sim.app.episim.snapshot.SnapshotWriter;
import sim.app.episim.util.Names;
import sim.display.Console;
import sim.display.ConsoleHack;
import sim.display.GUIState;
import sim.portrayal.SimpleInspector;
import sim.util.gui.NumberTextField;
import sim.util.gui.PropertyField;

public class EpiConsole extends ConsoleHack implements ActionListener{
	private Container controllerContainer;
	
	private KeyListener keyListener;

	private FocusAdapter focusAdapter;
	private JButton resetButton;
	private List<JButton> refreshButtons;
	private JButton snapshotButton;
	private final static String RESETTEXT = "Reset";
	private boolean reloadedSnapshot = false;
	public EpiConsole(final GUIState simulation, boolean reloadSnapshot){
		super(simulation);
		 
		 controllerContainer = getControllerContainer(super.getContentPane());
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
               //	ModelController.getInstance().getBioChemicalModelController().reloadValue(());
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
      		// ModelController.getInstance().getBioChemicalModelController().reloadValue((name=));
          	if(name.equals("TypeColor")) clickRefreshButtons();
          }
           }
       };
		
       //Liste der Frames überschreiben
       
       getFrameListDisplay().setCellRenderer(new ListCellRenderer()
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
       if(SnapshotWriter.getInstance().getSnapshotPath() == null){ 
      	 snapshotButton.setEnabled(false);
       }
       snapshotButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				simulation.state.preCheckpoint();
				//if(getPlayState() != PS_PAUSED && getPlayState() == PS_PLAYING) pressPause();  
				SnapshotWriter.getInstance().writeSnapshot();
				//if(getPlayState() == PS_PAUSED && getPlayState() != PS_PLAYING)pressPause(); 
				simulation.state.postCheckpoint();
			}
      	 
       });
   //   addSnapshotButton();
		
	}
	
	
	private Container getControllerContainer(Container con){
		if(con.getName() != null && con.getName().equals(Names.CONSOLEMAINCONTAINER)) return con;
		else{
		 for(Component comp : con.getComponents()){
			 if(comp.getName() != null && comp.getName().equals(Names.CONSOLEMAINCONTAINER) && comp instanceof Container) return (Container)comp;
			 else if(comp instanceof Container) return getControllerContainer(((Container)comp));
		 }
		 return null;
		}
		
	}

   /** Simulations can call this to add a frame to be listed in the "Display list" of the console */
   public synchronized boolean registerFrame(JInternalFrame frame)
   {
   	getFrameList().add(frame);
   	getFrameListDisplay().setListData(getFrameList());
   	return true;
   }
   
   public synchronized boolean deregisterAllFrames()
   {
   	getFrameList().clear();
   	getFrameListDisplay().setListData(getFrameList());
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
			if (tabPane.getTitleAt(i).equals(Names.BIOCHEMMODEL) || tabPane.getTitleAt(i).equals(Names.MECHMODEL) || tabPane.getTitleAt(i).equals(Names.MISCALLENEOUS)){
				String actionString = null;
				if (tabPane.getTitleAt(i).equals(Names.BIOCHEMMODEL)) actionString = Names.BIOCHEMMODEL;
				else if (tabPane.getTitleAt(i).equals(Names.MECHMODEL)) actionString = Names.MECHMODEL;
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
				
				if(getPlayState() == PS_PLAYING) super.pressPause();
				if(pressedButton.getActionCommand() != null){
					if(pressedButton.getActionCommand().equals(Names.BIOCHEMMODEL))ModelController.getInstance().getBioChemicalModelController().resetInitialGlobalValues();
					else if(pressedButton.getActionCommand().equals(Names.MECHMODEL))ModelController.getInstance().getBioMechanicalModelController().resetInitialGlobalValues();
					else if(pressedButton.getActionCommand().equals(Names.MISCALLENEOUS))MiscalleneousGlobalParameters.instance().resetInitialGlobalValues();
				}
				this.clickRefreshButtons();
				if(getPlayState() == PS_PAUSED)super.pressPause();
				
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
       Object[] vals = (Object[]) (getFrameListDisplay().getSelectedValues());
       for (int x = 0; x < vals.length; x++)
           {
           ((JInternalFrame) (vals[x])).toFront();
           ((JInternalFrame) (vals[x])).setVisible(true);
           }
       getFrameListDisplay().repaint();
       }

   /** Called when the "show all" button is pressed in the Displays window */
   synchronized void pressShowAll()
       {
       Object[] vals = (Object[]) (getFrameList().toArray());
       for (int x = 0; x < vals.length; x++)
           {
           ((JInternalFrame) (vals[x])).toFront();
           ((JInternalFrame) (vals[x])).setVisible(true);
           }
       getFrameListDisplay().repaint();
       }

   /** Called when the "hide" button is pressed in the Displays window */
   synchronized void pressHide()
       {
       Object[] vals = (Object[]) (getFrameListDisplay().getSelectedValues());
       for (int x = 0; x < vals.length; x++)
           {
           ((JInternalFrame) (vals[x])).setVisible(false);
           }
       	getFrameListDisplay().repaint();
       }

   /** Called when the "hide all" button is pressed in the Displays window */
   synchronized void pressHideAll()
       {
       Object[] vals = (Object[]) (getFrameList().toArray());
       for (int x = 0; x < vals.length; x++)
           {
           ((JInternalFrame) (vals[x])).setVisible(false);
           }
       getFrameListDisplay().repaint();
       }
	
   public synchronized void pressPlay(){
	if(!reloadedSnapshot){
		
	
   	
	((EpidermisGUIState)this.simulation).clearWoundPortrayalDraw();
	
	}
	((EpidermisGUIState)this.simulation).simulationWasStarted();
   	super.pressPlay(reloadedSnapshot);
   }
   public synchronized void pressStop(){
   
   	((EpidermisGUIState)this.simulation).simulationWasStopped();
   	
      	
      	super.pressStop();
      }
   
   private void addSnapshotButton(){
   	if(getContentPane().getLayout() instanceof BorderLayout){
   		final Component comp =((BorderLayout) getContentPane().getLayout()).getLayoutComponent(BorderLayout.SOUTH);
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
   
}
