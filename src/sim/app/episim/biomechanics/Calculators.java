package sim.app.episim.biomechanics;

import java.awt.geom.Line2D;
import java.util.Random;

public abstract class Calculators {
	
	public static final int STARTX = 100;
	public static final int STARTY = 100;
	public static final int SIDELENGTH = 20;
	public static final int SIDELENGTHHALF = SIDELENGTH/2;
	private static Random rand = new Random(100);
	
	
	public static Cell[] getStandardCellArray(int rows, int columns){
		int height = Math.round((float) Math.sqrt(Math.pow(SIDELENGTH, 2)-Math.pow(SIDELENGTH/2, 2)));
		
		Cell[] cells = getCells(rows, columns, height);
		Vertex[][] vertices = getVertices(rows, columns, height);
		
		for(int rowNo = 0; rowNo < vertices.length; rowNo++){
			for(int columnNo = 0; columnNo < vertices[rowNo].length; columnNo++){
				for(int cellNo = 0; cellNo <  cells.length; cellNo++){
					if(vertices[rowNo][columnNo] != null && cells[cellNo] != null
						&&	distance(cells[cellNo].getX(), cells[cellNo].getY(), vertices[rowNo][columnNo].getIntX(), vertices[rowNo][columnNo].getIntY()) <= SIDELENGTH)
						cells[cellNo].addVertex(vertices[rowNo][columnNo]);
				}
			}
		}	
		
		return cells;		
	}
	
	private static double distance(int x1, int y1, int x2, int y2){	
		return distance((double) x1, (double) y1, (double) x2, (double) y2);		
	}
	
	private static double distance(double x1, double y1, double x2, double y2){	
		return Math.sqrt(Math.pow(x1-x2, 2)+Math.pow(y1-y2, 2));		
	}
	
	private static Vertex[][] getVertices(int rows, int columns, int height){
		
		Vertex[][] vertices = new Vertex[2*(rows+1)][(columns+1)];
		
		int startX = STARTX;
		int startY = STARTY - SIDELENGTH;
		
		//first row Vertices
		for(int i = 0; i < columns; i++) vertices[0][i] = new Vertex(startX + i*2*height, startY);
		
		// in between two rows of vertices with the same startX are calculated
		for(int i = 1;  i <= rows; i++){
			if((i%2)!= 0) startX -=height;
			else startX += height;
			
			startY += SIDELENGTHHALF;
			for(int n = 0; n <= columns; n++) vertices[((2*i)-1)][n] = new Vertex(startX+ n*2*height, startY);
			startY += SIDELENGTH;
			for(int n = 0; n <= columns; n++) vertices[(2*i)][n] = new Vertex(startX+ n*2*height, startY);
		}
		
		
		//last row Vertices
		startY += SIDELENGTHHALF;
		startX += height;
		for(int i = 0; i < columns; i++) vertices[vertices.length-1][i] = new Vertex(startX + i*2*height, startY);
		
		return vertices;
	}	
	
	private static Cell[] getCells(int rows, int columns, int height){
		Cell[] cells = new Cell[rows*columns];
		int cellIndex = 0;
		for(int i = 0; i < rows; i++){
			for(int n = 0; n < columns; n++){
				if(((i+1)%2) == 1)cells[cellIndex++] = new Cell(STARTX + 2*n*height, STARTY + i*(SIDELENGTH+SIDELENGTHHALF));
				else cells[cellIndex++] = new Cell(STARTX + (2*n + 1)*height, STARTY + i*(SIDELENGTH+SIDELENGTHHALF));
			}
		}
		return cells;
	}
	
	public static double getCellArea(Cell cell){
		double areaTrapeze = 0;
		int n = cell.getVertices().length;
		Vertex[] vertices = cell.getVertices();
		for(int i = 0; i < n; i++){
			areaTrapeze += ((vertices[(i%n)].getDoubleX() - vertices[((i+1)%n)].getDoubleX())*(vertices[(i%n)].getDoubleY() + vertices[((i+1)%n)].getDoubleY()));
		}
		
		return (Math.abs(areaTrapeze) / 2);
	}
	
	public static double getCellPerimeter(Cell cell){
		double cellPerimeter = 0;
		int n = cell.getVertices().length;
		Vertex[] vertices = cell.getVertices();
		for(int i = 0; i < n; i++){
			cellPerimeter += distance(vertices[(i%n)].getDoubleX(), vertices[(i%n)].getDoubleY(), vertices[((i+1)%n)].getDoubleX(), vertices[((i+1)%n)].getDoubleY());
		}
		return cellPerimeter;
	}

	public static void randomlySelectCell(Cell[] cells){
		//for(Cell c :cells) c.setSelected(false);
		
		int cellIndex =rand.nextInt(cells.length);
		if(!cells[cellIndex].isSelected()){
			addNewVertices(cells[cellIndex]);
			cells[cellIndex].setSelected(true);
		}
		
	}
	
	public static Vertex getCellCenter(Cell cell){
		Vertex[] vertices = cell.getVertices();
		double cumulativeX = 0, cumulativeY = 0;
		for(Vertex v : vertices){
			cumulativeX += v.getDoubleX();
			cumulativeY += v.getDoubleY();
		}
		return new Vertex(cumulativeX/vertices.length, cumulativeY/vertices.length);
	}
	
	
	public static void addNewVertices(Cell cell){
		//calculate the maximum distance of a cell's vertex to the cell's center vertex
		Vertex center = getCellCenter(cell);
		double maxDistance = 0;
		double actDist = 0;
		for(Vertex v: cell.getVertices()){
			actDist= center.edist(v);
			if(actDist > maxDistance) maxDistance = actDist;
		}
		
		//calculate point with random angle on the circle with cell center as center and maxDistance as radius
		double randAngleInRadians = Math.toRadians(rand.nextInt(180));
		
		Vertex vOnCircle = new Vertex((center.getDoubleX() +maxDistance*Math.cos(randAngleInRadians)), (center.getDoubleY()+maxDistance*Math.sin(randAngleInRadians)));
		
		//Calculate Intersection between the line cellcenter-vOnCircle and all sides of cell
		Vertex[] cellVertices = cell.getVertices();
		for(int i = 0; i < cellVertices.length; i++){
			Vertex v_s =getIntersectionOfLines(cellVertices[i], cellVertices[(i+1)%cellVertices.length], center, vOnCircle);
			if(v_s != null){ 
				v_s.isNew = true;
				cell.addVertex(v_s);
			}
		}
		
	}
	/**
	 * 
	 * @param v1 first point line one (first cell vertex)
	 * @param v2 second point line one (second cellvertex)
	 * @param v3 first point line two (cell center)
	 * @param v4 second point line two( point with max distance on circle)
	 * @return intersection point, returns null if there is no intersection
	 */
	public static Vertex getIntersectionOfLines(Vertex v1, Vertex v2, Vertex v3, Vertex v4){
		
		double denominator =  ((v4.getDoubleY() -v3.getDoubleY())*(v2.getDoubleX()-v1.getDoubleX())) - ((v4.getDoubleX()-v3.getDoubleX())*(v2.getDoubleY()-v1.getDoubleY()));
		
		if(denominator != 0){
			double u_a = (((v4.getDoubleX()-v3.getDoubleX())*(v1.getDoubleY()-v3.getDoubleY()))-((v4.getDoubleY()-v3.getDoubleY())*(v1.getDoubleX()-v3.getDoubleX()))) / denominator;			
			
			//only if u_a is between 0 and 1  the intersection point lies on the line segment described by the two cell vertices v1 and v2
			if(u_a >= 0 && u_a <= 1){
				double x_s = v1.getDoubleX() + u_a*(v2.getDoubleX()-v1.getDoubleX());
				double y_s = v1.getDoubleY() + u_a*(v2.getDoubleY()-v1.getDoubleY());
				return new Vertex(x_s, y_s);
			}
		}
		
		return null;
	}
	
	
}
