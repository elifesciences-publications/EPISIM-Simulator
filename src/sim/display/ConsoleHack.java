package sim.display;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Toolkit;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import episiminterfaces.SimulationConsole;

import sim.app.episim.devBasalLayer.EpidermisUIDev;
import sim.app.episim.gui.EpidermisGUIState;
import sim.app.episim.gui.EpisimTextOut;
import sim.app.episim.gui.ImageLoader;
import sim.app.episim.util.Names;
import sim.engine.SimState;
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
   
   public synchronized void pressPlay(boolean reloadSnapshot)
   {
   	
   	 super.pressPlay();
   }
   
   //this method is a copy
 /*  public static void showAbout()
   {
   	if (aboutFrame == null)
      {
      // construct the frame
      
      aboutFrame = new JFrame("About MASON");
      JPanel p = new JPanel();  // 1.3.1 only has borders for JComponents, not Boxes
      p.setBorder(BorderFactory.createEmptyBorder(25,30,30,30));
      Box b = new Box(BoxLayout.Y_AXIS);
      p.add(b,BorderLayout.CENTER);
      aboutFrame.getContentPane().add(p,BorderLayout.CENTER);
      aboutFrame.setResizable(false);
      Font small = new Font("Dialog",0,9);

      // start dumping in text
      JLabel j = new JLabel("MASON");
      j.setFont(new Font("Serif",0,36));
      b.add(j);
              
      java.text.NumberFormat n = java.text.NumberFormat.getInstance();
      n.setMinimumFractionDigits(0);
      j = new JLabel("Version " + n.format(SimState.version()));
      b.add(j);
      JLabel spacer = new JLabel(" ");
      spacer.setFont(new Font("Dialog",0,6));
      b.add(spacer);

      j = new JLabel("Co-created by George Mason University's");
      b.add(j);
      j = new JLabel("Evolutionary Computation Laboratory and");
      b.add(j);
      j = new JLabel("Center for Social Complexity");
      b.add(j);

      spacer = new JLabel(" ");
      spacer.setFont(new Font("Dialog",0,6));
      b.add(spacer);
      
      j = new JLabel("http://cs.gmu.edu/~eclab/projects/mason/");
      b.add(j);

      spacer = new JLabel(" ");
      spacer.setFont(new Font("Dialog",0,6));
      b.add(spacer);

      j = new JLabel("Major contributors include Sean Luke,");
      b.add(j);
      j = new JLabel("Gabriel Catalin Balan, Liviu Panait,");
      b.add(j);
      j = new JLabel("Claudio Cioffi-Revilla, Sean Paus,");
      b.add(j);
      j = new JLabel("Keith Sullivan, and Daniel Kuebrich.");
      b.add(j);
          
      spacer = new JLabel(" ");
      spacer.setFont(new Font("Dialog",0,6));
      b.add(spacer);
                  
      j = new JLabel("MASON is (c) 2005-2011 Sean Luke and George Mason University,");
      j.setFont(small);
      b.add(j);

      j = new JLabel("with various elements copyrighted by the above contributors.");
      j.setFont(small);
      b.add(j);

      j = new JLabel("PNGEncoder is (c) 2000 J. David Eisenberg.  MovieEncoder,", JLabel.LEFT);
      j.setFont(small);
      b.add(j);
      
      j = new JLabel("SelectionBehavior, and WireFrameBoxPortrayal3D are partly", JLabel.LEFT);
      j.setFont(small);
      b.add(j);
      
      j = new JLabel("(c) 1996 Sun Microsystems.  MersenneTwisterFast is partly", JLabel.LEFT);
      j.setFont(small);
      b.add(j);

      j = new JLabel("(c) 1993 Michael Lecuyer.  CapturingCanvas3D is based in", JLabel.LEFT);
      j.setFont(small);
      b.add(j);
  
      j = new JLabel("part on code by Peter Kunszt.", JLabel.LEFT);
      j.setFont(small);
      b.add(j);
      aboutFrame.pack();
      }
      
  // if not on screen right now, move to center of screen
  if (!aboutFrame.isVisible())
      {
      Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
      d.width -= aboutFrame.getWidth();
      d.height -= aboutFrame.getHeight();
      d.width /= 2;
      d.height /= 2;
      if (d.width < 0) d.width = 0;
      if (d.height < 0) d.height = 0;
      aboutFrame.setLocation(d.width,d.height);
      }
  
  // show it!
  aboutFrame.setVisible(true);
   }*/
   
   //Override to be able to deploy multiple model Inspectors
	void buildModelInspector() {
		EpidermisGUIState epiGUIState;
		if(simulation != null && simulation instanceof EpidermisGUIState){
			epiGUIState = (EpidermisGUIState) simulation;
		
			
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
   
   
	public GUIState getSimulation(){
		 return simulation;
	}
   
}
