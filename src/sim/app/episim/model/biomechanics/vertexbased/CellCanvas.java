package sim.app.episim.model.biomechanics.vertexbased;

import java.awt.BasicStroke;
import java.awt.Graphics2D;


public class CellCanvas {
	
	private int x = 0;
	private int y = 0;
	private int width = 0;
	private int height = 0;
	
	public CellCanvas(int x, int y, int width, int height){
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public void drawCanvasBorder(Graphics2D graphics){
		Stroke oldStroke = g
		g.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
	}
	

}
