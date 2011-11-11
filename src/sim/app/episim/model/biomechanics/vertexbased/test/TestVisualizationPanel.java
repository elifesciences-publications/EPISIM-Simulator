package sim.app.episim.model.biomechanics.vertexbased.test;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Iterator;


import javax.swing.JPanel;

import sim.app.episim.model.biomechanics.vertexbased.util.ColorRegistry;







public class TestVisualizationPanel extends JPanel {
	
	public interface TestVisualizationPanelPaintListener{ 
		void paintWasCalled(Graphics2D graphics);
		void paintToMovieBufferWasCalled(Graphics2D graphics);
	}
	
	private HashSet<TestVisualizationPanelPaintListener> paintListener;
	/** Image buffer for doing buffered draws, mostly for screenshots etc. */
   BufferedImage buffer = null;
 
   
	public TestVisualizationPanel(){
		super();
		paintListener = new HashSet<TestVisualizationPanelPaintListener>();
		
	}
	
	private void notifyAllPaintListeners(Graphics g){
		for(TestVisualizationPanelPaintListener listener : paintListener) listener.paintWasCalled((Graphics2D) g);
	}
	
	private void notifyAllPaintToMovieBufferListeners(Graphics g){
		for(TestVisualizationPanelPaintListener listener : paintListener) listener.paintToMovieBufferWasCalled((Graphics2D) g);
	}
	
	public void addTestVisualizationPanelPaintListener(TestVisualizationPanelPaintListener listener){
		paintListener.add(listener);
	}
	
	public void removeTestVisualizationPanelPaintListener(TestVisualizationPanelPaintListener listener){
		paintListener.remove(listener);
	}
	
	public void paint(Graphics g){
		super.paint(g);
		notifyAllPaintListeners(g);
	}
	
	public BufferedImage paint(boolean buffered, boolean shared)
   {
   
      
       BufferedImage result = null;
      
       result= paintBuffered();
       if (!shared) buffer = null; // kill it so paintBuffered(graphics,clip) makes a new one next time
       if (result != null) result.flush();  // just in case
       return result;
      
   }
	
	 public BufferedImage paintBuffered()
    {
	    // make buffer big enough
	    double ww = this.getWidth();
	    double hh = this.getHeight();
	    if (buffer==null || (buffer.getWidth(null)) != ww || (buffer.getHeight(null)) != hh)
	        // note < would be more efficient than != but
	        // it would create incorrect-sized images for snapshots,
	        // and it's more memory wasteful anyway
	        {
	       // buffer = getGraphicsConfiguration().createCompatibleImage((int)ww,(int)hh);
	        buffer =  new BufferedImage((int)ww,(int)hh, BufferedImage.TYPE_INT_RGB);
	        }
	    
	    // draw into the buffer
	    Graphics2D g = (Graphics2D)(buffer.getGraphics());
	    g.setColor(ColorRegistry.BACKGROUND_COLOR);
	    g.fillRect(0,0,buffer.getWidth(null),buffer.getHeight(null));
	    notifyAllPaintToMovieBufferListeners(g);
	    
	    g.dispose();  // because we got it with getGraphics(), we're responsible for it
	    
	   
	    return buffer;
    }
	 

	 
	 
	

}
