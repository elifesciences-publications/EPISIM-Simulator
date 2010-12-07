package sim.app.episim.biomechanics;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import java.awt.image.BufferedImage;
import java.util.HashSet;


import javax.swing.JPanel;





public class TestVisualizationPanel extends JPanel {
	
	public interface TestVisualizationPanelPaintListener{ void paintWasCalled(Graphics2D graphics);}
	
	private HashSet<TestVisualizationPanelPaintListener> paintListener;
	/** Image buffer for doing buffered draws, mostly for screenshots etc. */
   BufferedImage buffer = null;
   /** Hints used to draw the buffered image to the screen */
   public RenderingHints bufferedHints;
   
	public TestVisualizationPanel(){
		super();
		paintListener = new HashSet<TestVisualizationPanelPaintListener>();
		setupHints(false,false,false);  // go for speed
	}
	
	private void notifyAllPaintListeners(Graphics g){
		for(TestVisualizationPanelPaintListener listener : paintListener) listener.paintWasCalled((Graphics2D) g);
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
	
	public BufferedImage paint(final Graphics graphics, boolean buffered, boolean shared)
   {
   
       {
       BufferedImage result = null;
      
       result= paintBuffered((Graphics2D)graphics);
       if (!shared) buffer = null; // kill it so paintBuffered(graphics,clip) makes a new one next time
       if (result != null) result.flush();  // just in case
       return result;
       }
   }
	
	 BufferedImage paintBuffered(final Graphics2D graphics)
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
    g.setColor(this.getBackground());
    g.fillRect(0,0,buffer.getWidth(null),buffer.getHeight(null));
    
    
    g.dispose();  // because we got it with getGraphics(), we're responsible for it
    
    // paint and return the buffer
    if (graphics!=null)
        {
        graphics.setRenderingHints(bufferedHints);
        graphics.drawImage(buffer,0,0,null);
        }
    return buffer;
    }
	 
	 public void setupHints(boolean antialias, boolean niceAlphaInterpolation, boolean niceInterpolation)
    {
    

    bufferedHints = new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);  // in general
    bufferedHints.put(RenderingHints.KEY_INTERPOLATION,
        niceInterpolation ? RenderingHints.VALUE_INTERPOLATION_BILINEAR :
        RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
    // similarly
    bufferedHints.put(RenderingHints.KEY_ANTIALIASING, 
        antialias ? RenderingHints.VALUE_ANTIALIAS_ON :
        RenderingHints.VALUE_ANTIALIAS_OFF);
    bufferedHints.put(RenderingHints.KEY_TEXT_ANTIALIASING,
        antialias ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON :
        RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
    bufferedHints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, 
        niceAlphaInterpolation ? 
        RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY :
        RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
    }
	

}
