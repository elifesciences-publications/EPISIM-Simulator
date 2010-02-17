package sim.app.episim.biomechanics;

public abstract class Calculators {
	
	public static final int STARTX = 100;
	public static final int STARTY = 100;
	public static final int SIDELENGTH = 20;
	public static final int SIDELENGTHHALF = SIDELENGTH/2;
	
	
	
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

}
