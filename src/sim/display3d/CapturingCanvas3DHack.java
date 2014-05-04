package sim.display3d;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.GraphicsConfiguration;
import java.awt.geom.Rectangle2D;

import javax.media.j3d.J3DGraphics2D;

import sim.SimStateServer;
import sim.SimStateServer.EpisimSimulationState;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters.MiscalleneousGlobalParameters3D;
import sim.app.episim.visualization.threedim.Optimized3DVisualization;



public class CapturingCanvas3DHack extends CapturingCanvas3D {
	
	private J3DGraphics2D graphics;
	private final int LABEL_MARGIN_TOP=30;
	
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
			 
			if(SimStateServer.getInstance().getEpisimSimulationState()== EpisimSimulationState.PAUSE
					||SimStateServer.getInstance().getEpisimSimulationState()== EpisimSimulationState.PLAY
					||SimStateServer.getInstance().getEpisimSimulationState()== EpisimSimulationState.STEPWISE){
				graphics = this.getGraphics2D();
				 FontMetrics fm =graphics.getFontMetrics();
				 String simStepLabelText= "Simulation Step: " + SimStateServer.getInstance().getSimStepNumber();
				 Rectangle2D stringRect = fm.getStringBounds(simStepLabelText, graphics);
				 Color oldColor = graphics.getColor();
				 MiscalleneousGlobalParameters param = MiscalleneousGlobalParameters.getInstance();
				 Color fontColor = Color.WHITE;
				 Color backgroundColor = Color.BLACK;
				 if(param instanceof MiscalleneousGlobalParameters3D && ((MiscalleneousGlobalParameters3D)param).getOptimizedGraphics()){			
					 backgroundColor = Optimized3DVisualization.backgroundColor;
					 fontColor = Optimized3DVisualization.simulationBoxColor;
				 } 
				 graphics.setColor(backgroundColor);
				 double translationX = this.getHeight() <= this.getWidth() ? this.getHeight()/2:stringRect.getWidth();
				 int labelPosX = (int)((this.getWidth() / 2)-translationX);
				 graphics.fillRect(labelPosX, (LABEL_MARGIN_TOP+3) - (int)stringRect.getHeight(), (int)stringRect.getWidth(), (int)stringRect.getHeight());
				 graphics.setColor(fontColor);
				 graphics.drawString(simStepLabelText, labelPosX, LABEL_MARGIN_TOP);
				 graphics.setColor(oldColor);
			  	 graphics.flush(true);
			}
	    }
    }

}
