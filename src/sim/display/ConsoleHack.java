package sim.display;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JInternalFrame;
import javax.swing.JList;

import sim.portrayal.Inspector;


public class ConsoleHack extends Console {
	
	public ConsoleHack(final GUIState simulation){
		super(simulation);
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

}
