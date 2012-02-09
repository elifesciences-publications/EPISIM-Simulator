package sim.display;


import java.awt.Insets;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;


import javax.swing.JInternalFrame;

import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import episiminterfaces.SimulationConsole;


import sim.app.episim.gui.EpisimGUIState;
import sim.app.episim.gui.EpisimTextOut;

import sim.app.episim.util.Names;

import sim.portrayal.Inspector;


public class ConsoleHack extends Console implements SimulationConsole{
	JScrollPane cellbehavioralModelInspectorScrollPane;
	JScrollPane biomechanicalModelInspectorScrollPane;
	JScrollPane miscalleneousInspectorScrollPane;
	JScrollPane episimTextOutInspectorScrollPane;
	
	
	public ConsoleHack(final GUIState simulation){
		super(simulation);
		
		super.getContentPane().setName(Names.CONSOLE_MAIN_CONTAINER);
		
	}
	
	
	public JMenuBar getTheMenuBar(){
		return menuBar;
	}
	
	public Vector getFrameList(){ return this.frameList;}
	
	public JList getFrameListDisplay(){ return this.frameListDisplay;}
// we presume this isn't being called from the model thread.
   public void refresh()
       {
       // updates the displays.
       final Enumeration e = frameList.elements();
       while(e.hasMoreElements())
           ((JInternalFrame)(e.nextElement())).getContentPane().repaint();

       // updates the inspectors
       Iterator i = allInspectors.keySet().iterator();
       while(i.hasNext())
           {
           Inspector c = (Inspector)(i.next());
           if (c!=null)  // this is a WeakHashMap, so the keys can be null if garbage collected
               {
               if (c.isVolatile())
                   {
                   c.updateInspector();
                   c.repaint();
                   }
               }
           }
           
       // finally, update ourselves
       if (modelInspector!=null && modelInspector.isVolatile()) 
           {
           modelInspector.updateInspector();
           modelInspector.repaint();
           }
       getContentPane().repaint();
       }
   	
   public void setPlayState(int state){
   	super.setPlayState(state);
   }
   
   public synchronized void pressPlay()
   {   	
   	 super.pressPlay();
   }
   
 
   
   //Override to be able to deploy multiple model Inspectors
	void buildModelInspector() {
		EpisimGUIState epiGUIState;
		if(simulation != null && simulation instanceof EpisimGUIState){
			epiGUIState = (EpisimGUIState) simulation;
		
			
			deployInspector(epiGUIState.getCellBehavioralModelInspector(), this.cellbehavioralModelInspectorScrollPane, Names.BIOCHEM_MODEL);
		
			deployInspector(epiGUIState.getBiomechnicalModelInspector(), this.biomechanicalModelInspectorScrollPane, Names.MECH_MODEL);
			deployInspector(epiGUIState.getMiscalleneousInspector(), this.miscalleneousInspectorScrollPane, Names.MISCALLENEOUS);
			deployInspector(EpisimTextOut.getEpisimTextOut().getEpisimTextOutPanel(), this.episimTextOutInspectorScrollPane, Names.EPISIM_TEXTOUT);
			
		}
		else
			super.buildModelInspector();
	}
   
	private void deployInspector(Inspector inspector, JScrollPane pane, String alternativeName){
		// remove existing tab if it's there
		if(pane != null) tabPane.remove(pane);
		if(inspector != null){
			String name = inspector.getName();
			if(name == null || name.length() == 0)	name = alternativeName;
			pane = new JScrollPane(inspector) {

				Insets insets = new Insets(0, 0, 0, 0); // MacOS X adds a border

				public Insets getInsets() {

					return insets;
				}
			};
			pane.getViewport().setBackground(new JPanel().getBackground()); // UIManager.getColor("window"));  // make nice stripes on MacOS X
			tabPane.addTab(name, pane);
		}
		tabPane.revalidate();
		
	}
	private void deployInspector(JPanel panel, JScrollPane pane, String alternativeName){
		// remove existing tab if it's there
		if(pane != null) tabPane.remove(pane);
		if(panel != null){
			String name = panel.getName();
			if(name == null || name.length() == 0)	name = alternativeName;
			pane = new JScrollPane(panel) {

				Insets insets = new Insets(0, 0, 0, 0); // MacOS X adds a border

				public Insets getInsets() {

					return insets;
				}
			};
			pane.getViewport().setBackground(new JPanel().getBackground()); // UIManager.getColor("window"));  // make nice stripes on MacOS X
			tabPane.addTab(name, pane);
		}
		tabPane.revalidate();
		
	}
	
	public void disableConsoleButtons(){
		this.pauseButton.setEnabled(false);
		this.playButton.setEnabled(false);
		this.stopButton.setEnabled(false);
	}
	
	public void enableConsoleButtons(){
		this.pauseButton.setEnabled(true);
		this.playButton.setEnabled(true);
		this.stopButton.setEnabled(true);
	}
   
   
	public GUIState getSimulation(){
		 return simulation;
	}
   
}
