package sim.display3d;

import java.awt.GraphicsConfiguration;

import javax.media.j3d.J3DGraphics2D;

import sim.SimStateServer;


public class CapturingCanvas3DHack extends CapturingCanvas3D {
	private J3DGraphics2D graphics;
	
	public CapturingCanvas3DHack(GraphicsConfiguration graphicsConfiguration) 
   {
		super(graphicsConfiguration);
		graphics = this.getGraphics2D();
   }

	public CapturingCanvas3DHack(GraphicsConfiguration graphicsConfiguration, boolean offScreen)
   {
		super(graphicsConfiguration, offScreen);
		graphics = this.getGraphics2D();
   }
	

	 public void postRender()
    {
	    super.postRender();
		 synchronized(this){ 
			 graphics.drawString("Simulation Step: " + SimStateServer.getInstance().getSimStepNumber(), 20, 20);
		  	 graphics.flush(true);
	    }
    }

}
