package sim.app.episim.model.biomechanics.vertexbased;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;


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
		Stroke oldStroke = graphics.getStroke();
		Color oldColor = graphics.getColor();
		graphics.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		graphics.drawRect(x, y, width, height);
		graphics.setColor(oldColor);
		graphics.setStroke(oldStroke);
	}
	
	private void drawPoint(Graphics2D g, int x, int y, int size, Color c){
		if(x> 0 || y > 0){
			if(size % 2 != 0) size -= 1;
			Color oldColor = g.getColor();
			g.setColor(c);
			g.fillRect(x-(size/2), y-(size/2), size+1, size+1);
			g.setColor(oldColor);
		}
	}
	
	public void drawCellPolygon(Graphics2D g, CellPolygon cell){
		if(cell != null){
			
			Polygon p = new Polygon();
			
			
			Vertex[] sortedVertices = ContinuousVertexField.getInstance().getMinDistanceTransformedVertexArrayMajorityQuadrantReferenceSigned(cell.getSortedVertices());
		
			for(Vertex v : sortedVertices){	
				p.addPoint(v.getIntX() + x, v.getIntY()+y);				
			}
		
			
			
			Color oldColor = g.getColor();
			g.setColor(cell.getFillColor());
			g.fillPolygon(p);
			g.setColor(oldColor);
			g.drawPolygon(p);
			
			
			
		for(Vertex v : sortedVertices){	
			drawPoint(g, v.getIntX()+x, v.getIntY()+y, 3, Color.BLUE);			
		}
			
			
		}
	}
	
	

}
