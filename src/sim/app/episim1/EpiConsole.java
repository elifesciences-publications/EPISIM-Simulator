package sim.app.episim1;



import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Iterator;

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
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

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
	private JButton refreshButton;
	private JButton snapshotButton;
	private final static String RESETTEXT = "Reset";
	private boolean reloadedSnapshot = false;
	public EpiConsole(final GUIState simulation, boolean reloadSnapshot){
		super(simulation);
		 
		 controllerContainer = super.getContentPane();
		  
		
		 
		 
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
               	String name;
               	BioChemicalModelController.getInstance().reloadValue((name=((JTextField) keyEvent.getSource()).getName()));
               	if(name.equals("TypeColor_1to9") && refreshButton !=null) refreshButton.doClick();
               }
               }
           }
       };
       
       focusAdapter = new FocusAdapter()
       {
       public void focusLost ( FocusEvent e )
           {
      	 if(e.getSource() instanceof JTextField){
      		 String name;
          	BioChemicalModelController.getInstance().reloadValue((name=((JTextField) e.getSource()).getName()));
          	if(name.equals("TypeColor_1to9") && refreshButton !=null) refreshButton.doClick();
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
       snapshotButton.setIcon(new ImageIcon(ImageLoader.class.getResource("Camera.png")));
       snapshotButton.setPressedIcon(new ImageIcon(ImageLoader.class.getResource("CameraPressed.png")));
       snapshotButton.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));
       if(SnapshotWriter.getInstance().getSnapshotPath() == null){ 
      	 snapshotButton.setEnabled(false);
       }
       snapshotButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				if(getPlayState() != PS_PAUSED && getPlayState() == PS_PLAYING) pressPause();  
				SnapshotWriter.getInstance().writeSnapshot();
				//if(getPlayState() == PS_PAUSED && getPlayState() != PS_PLAYING)pressPause();  
			}
      	 
       });
       addSnapshotButton();
		
	}
	

   /** Simulations can call this to add a frame to be listed in the "Display list" of the console */
   public synchronized boolean registerFrame(JInternalFrame frame)
   {
   	getFrameList().add(frame);
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
	
	
	public void addActionListeners(){
		Object obj=null;
		JTabbedPane tabPane;
		for(int i = 0; i<controllerContainer.getComponentCount(); i++){
			if((obj=controllerContainer.getComponent(i)) instanceof JTabbedPane) break;
			
		}
		for(int i=0; i< (tabPane= ((JTabbedPane) obj)).getTabCount(); i++){
			if (tabPane.getTitleAt(i).equals("Model")){ 
				
				Component comp = tabPane.getComponentAt(i); 
				
					if(comp instanceof Container) getTextFields(((Container)comp)); 
			
				
			}
		}
	}
	
	private void getButtons(Container root, ArrayList<JButton> buttons){
		Component [] comps = root.getComponents();
		for(Component comp: comps){
			if(comp instanceof JButton) buttons.add(((JButton) comp));
			else if(comp instanceof Container) getButtons(((Container) comp), buttons);
		}
	}
	
	
	private void getTextFields(Container comp){
		
		
		for(int i = 0; i<comp.getComponentCount(); i++){
			
			
			if(comp.getComponent(i) instanceof Container &&
					!(comp.getComponent(i) instanceof SimpleInspector)) 
				getTextFields(((Container)comp.getComponent(i)));
			else if(comp.getComponent(i) instanceof SimpleInspector){
	//hier wird der Button für reset eingefügt
				
				Component pan;
				if((pan=(((SimpleInspector)comp.getComponent(i)).header)) instanceof JPanel){ 
					addResetButton(((JPanel) pan));
					
				}
				
				for(Component compo:((SimpleInspector) comp.getComponent(i)).startField.getComponents()){
					
					if(compo instanceof NumberTextField){
						((NumberTextField)compo).bellyButton.removeActionListener(this);
						((NumberTextField)compo).downButton.removeActionListener(this);
						((NumberTextField)compo).upButton.removeActionListener(this);
						((NumberTextField)compo).bellyButton.addActionListener(this);
						((NumberTextField)compo).downButton.addActionListener(this);
						((NumberTextField)compo).upButton.addActionListener(this);
						
					}
				}
				PropertyField field = null;
				for(int n =0; n< ((SimpleInspector) comp.getComponent(i)).members.length; n++){
					field =((SimpleInspector) comp.getComponent(i)).members[n];
					if(field != null && field.valField != null){
						field.valField.addKeyListener(keyListener);
						field.valField.setName(((SimpleInspector) comp.getComponent(i)).properties.getName(n));
						field.valField.addFocusListener(focusAdapter);
					}
				}
			}
		}
		
		
		
	}
	
	private void addResetButton(JPanel inspectorHeader){
		JPanel buttonPanel = new JPanel(new BorderLayout(10, 0));
		
		Component [] comps = inspectorHeader.getComponents();
		resetButton = new JButton(RESETTEXT);
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
		buttonPanel.add(refreshButton, BorderLayout.WEST);
		buttonPanel.add(resetButton, BorderLayout.EAST);
		buttonPanel.setName("ResetRefreshPanel");
		resetButton.addActionListener(this);
		
		inspectorHeader.add(buttonPanel, BorderLayout.WEST);
	}
	
	public void actionPerformed(ActionEvent e) {

		if(e.getSource() instanceof JButton){
			
			if(((JButton) e.getSource()).getText().equalsIgnoreCase("Show")){
				pressShow();
			}
			else if(((JButton) e.getSource()).getText().equalsIgnoreCase("Show All")){
				pressShowAll();
			}
			else if(((JButton) e.getSource()).getText().equalsIgnoreCase("Hide")){
				pressHide();
			}
			else if(((JButton) e.getSource()).getText().equalsIgnoreCase("Hide All")){
				pressHideAll();
			}
			else if(((JButton) e.getSource()).getText().equalsIgnoreCase(RESETTEXT)){
				if(getPlayState() == PS_PLAYING) super.pressPause();
				BioChemicalModelController.getInstance().resetInitialGloabalValues();
				refreshButton.doClick();
				if(getPlayState() == PS_PAUSED)super.pressPause();
				
			}
			else{

			SwingUtilities.invokeLater(new Runnable() {

				public void run() {

					addActionListeners();
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
	if(!reloadedSnapshot)EpiSimCharts.getInstance().clearSeries();
   	
	((EpidermisWithUIClass)this.simulation).clearWoundPortrayalDraw();
   	
   	super.pressPlay(reloadedSnapshot);
   }
   
   private void addSnapshotButton(){
   	if(getContentPane().getLayout() instanceof BorderLayout){
   		Component comp =((BorderLayout) getContentPane().getLayout()).getLayoutComponent(BorderLayout.SOUTH);
   		if(comp != null && comp instanceof Box){
   			((Box) comp).add(snapshotButton, 3);
   		}
   	}
   }


	
	public void setReloadedSnapshot(boolean reloadedSnapshot) {
	
		this.reloadedSnapshot = reloadedSnapshot;
	}
   
}
