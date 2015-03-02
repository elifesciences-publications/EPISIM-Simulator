package sim.app.episim.model.biomechanics.vertexbased2d.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;

import sim.app.episim.model.biomechanics.vertexbased2d.geom.CellPolygon;
import sim.app.episim.model.biomechanics.vertexbased2d.geom.ContinuousVertexField;
import sim.app.episim.model.biomechanics.vertexbased2d.geom.Line;
import sim.app.episim.model.biomechanics.vertexbased2d.geom.Vertex;


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
			g.fillRect(x-(size/2), (height-(y-(size/2))), size+1, size+1);
			g.setColor(oldColor);
		}
	}
	
	public void highlightLine(Graphics2D g, Line line, Color c){
		Color oldColor = g.getColor();
		Stroke oldStroke = g.getStroke();
		g.setColor(c);
		g.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g.drawLine(x + line.getV1().getIntX(), y + (height-line.getV1().getIntY()), x + line.getV2().getIntX(), y + (height-line.getV2().getIntY()));
		g.setColor(oldColor);
		g.setStroke(oldStroke);
	}
	
	public void drawCellPolygon(Graphics2D g, CellPolygon cell, Color borderColor, Color fillColor){
		if(cell != null){			
			Polygon p = new Polygon();			
			Vertex[] sortedVertices = ContinuousVertexField.getInstance().getMinDistanceTransformedVertexArrayMajorityQuadrantReferenceSigned(cell.getSortedVertices());		
			for(Vertex v : sortedVertices){	
				p.addPoint(v.getIntX() + x, y+(height-v.getIntY()));				
			}			
			
			Color oldColor = g.getColor();
			if(fillColor == null) fillColor = ColorRegistry.CELL_FILL_COLOR;
			if(borderColor == null) borderColor = ColorRegistry.CELL_BORDER_COLOR;
			g.setColor(fillColor);
			g.fillPolygon(p);
			g.setColor(borderColor);
			g.drawPolygon(p);
			g.setColor(oldColor);		
		/*	for(Vertex v : sortedVertices){	
				drawPoint(g, v.getIntX()+x, v.getIntY()+y, 3, Color.BLUE);			
			}			*/
		}
	}
	
	public Polygon getDrawablePolygon(CellPolygon cell){
		return getDrawablePolygon(cell,0, 0);
	}
	
	public Polygon getDrawablePolygon(CellPolygon cell, double deltaX, double deltaY){
		Polygon p = new Polygon();			
		Vertex[] sortedVertices = ContinuousVertexField.getInstance().getMinDistanceTransformedVertexArrayMajorityQuadrantReferenceSigned(cell.getSortedVertices());		
		for(Vertex v : sortedVertices){	
			p.addPoint(v.getIntX() + x + ((int)deltaX),  (y + ((int)deltaY))+(height-v.getIntY()));			
		}
		return p;
	}	
	
	public void drawVertex(Graphics2D g, Vertex vertex, Color color){
		if(vertex != null){
			drawPoint(g, vertex.getIntX()+x, y+(height-vertex.getIntY()), 3, color == null ? Color.BLUE : color);						
		}
	}
	
	public void drawBigVertex(Graphics2D g, Vertex vertex){
		if(vertex != null){
			drawPoint(g, vertex.getIntX()+x, y+(height-vertex.getIntY()), 10, Color.BLUE);						
		}
	}
}
