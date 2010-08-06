package sim.app.episim.devBasalLayer;



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

import sim.app.episim.model.CellBehavioralModelController;
import sim.app.episim.snapshot.SnapshotWriter;
import sim.display.Console;
import sim.display.ConsoleHack;
import sim.display.GUIState;
import sim.portrayal.SimpleInspector;
import sim.util.gui.NumberTextField;
import sim.util.gui.PropertyField;

public class EpiConsoleDev extends ConsoleHack{
	private Container controllerContainer;
	
	private KeyListener keyListener;

	private FocusAdapter focusAdapter;
	private JButton resetButton;
	private JButton refreshButton;
	private JButton snapshotButton;
	private final static String RESETTEXT = "Reset";
	private boolean reloadedSnapshot = false;
	public EpiConsoleDev(final GUIState simulation, boolean reloadSnapshot){
		super(simulation);
		 
		 controllerContainer = super.getContentPane();
		  
		
		 
		 
		
		
		
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
	
   	
   	super.pressPlay(false);
   }
   
      
}
