package sim.app.episim.biomechanics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

import com.lowagie.tools.plugins.SelectedPages;

import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.biomechanics.simanneal.VertexForcesMinimizerSimAnneal;

import ec.util.MersenneTwisterFast;


public class TestVisualizationBiomechanics {
	
	private enum SimState{SIMSTART, SIMSTOP;}
	private JFrame frame;
	private JPanel visualizationPanel;
	private CellPolygon[] cells;
	private Thread simulationThread;
	private SimState simulationState = null;
	
	private int indexOfSelectedCell = 4;
	
	private ConjugateGradientOptimizer conGradientOptimizer;
   private VertexForcesMinimizerSimAnneal simAnnealOptimizer;
   private MersenneTwisterFast rand = new ec.util.MersenneTwisterFast(System.currentTimeMillis());
	
	public TestVisualizationBiomechanics(){
		try{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e){			
			e.printStackTrace();
		}
		//testCellAreaCalculation();
		cells = Calculators.getSquareVertex(100, 100, 50, 6);
		
		cells = Calculators.getStandardCellArray(3, 3);
		
		
		conGradientOptimizer = new ConjugateGradientOptimizer();
	   simAnnealOptimizer = new VertexForcesMinimizerSimAnneal();
		
		
		
		
		frame = new JFrame("Biomechanics Testvisualization");
		frame.setSize(500, 500);
		frame.setPreferredSize(new Dimension(500, 500));
		frame.getContentPane().setLayout(new BorderLayout());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		visualizationPanel = new JPanel(){
			public void paint(Graphics g){ 
				super.paint(g);
				drawVisualization((Graphics2D) g );}
		};
		visualizationPanel.setDoubleBuffered(true);
		visualizationPanel.setBackground(Color.WHITE);
		visualizationPanel.setMinimumSize(new Dimension(500, 500));
		visualizationPanel.setSize(new Dimension(500, 500));
		visualizationPanel.setPreferredSize(new Dimension(500, 500));
		frame.getContentPane().add(visualizationPanel, BorderLayout.CENTER);
		
		visualizationPanel.setBorder(BorderFactory.createLoweredBevelBorder());
		((JPanel)frame.getContentPane()).setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
		
		JPanel buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(2,5,2,5));
		final JButton startStopButton = new JButton("start");
		startStopButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {

	        if(simulationState== null || simulationState == SimState.SIMSTOP){
	      	  startStopButton.setText("stop");
	      	  setSimulationState(SimState.SIMSTART);
	        }
	        else{
	      	  startStopButton.setText("start");
	      	  setSimulationState(SimState.SIMSTOP);
	        }
	         
         }});
		buttonPanel.add(startStopButton);
		frame.getContentPane().add(buttonPanel, BorderLayout.NORTH);
		
		centerMe(frame);
		frame.pack();
		frame.setVisible(true);
	
		
		
		
		
	}
	private void setSimulationState(SimState state){
		if(state == SimState.SIMSTART){
		
		simulationState = SimState.SIMSTART;
		simulationThread = new Thread(new Runnable(){ 
			
			
			
			public void run() { 
	
			cells[indexOfSelectedCell].setSelected(true);
			while(simulationState == SimState.SIMSTART){
				try{
					checkCellDivision();
					relaxAllVertices();					
					visualizationPanel.repaint(); 
	            Thread.sleep(1);
            }
            catch (InterruptedException e){
	            ExceptionDisplayer.getInstance().displayException(e);
            }	
				
			}
			
		
			} });
		
	   	simulationThread.start();
		}
		else if(state == SimState.SIMSTOP){
			simulationState = SimState.SIMSTOP;
			
		}
	        
        
			
		
	}
	
	private void checkCellDivision(){
		if(cells[indexOfSelectedCell].canDivide()){
			CellPolygon daughterCell = cells[indexOfSelectedCell].cellDivision();
			if(daughterCell != null){
				CellPolygon[] newCellArray = new CellPolygon[cells.length+1];
				System.arraycopy(cells, 0, newCellArray, 0, cells.length);
				newCellArray[cells.length] = daughterCell;
				cells= newCellArray;
				cells[indexOfSelectedCell].setSelected(false);
				indexOfSelectedCell =Calculators.randomlySelectCell(cells);
			}
		}
		else cells[indexOfSelectedCell].grow(10);
	}
	
	private void relaxAllVertices(){
		
		int randomStartIndexCells =  rand.nextInt(cells.length);
		CellPolygon polygon = null;
		for(int n = 0; n < cells.length; n++){
			polygon = cells[((n+randomStartIndexCells)% cells.length)];
			//	System.out.println("Cell No. "+ polygon.getId() + " Size before: " +polygon.getCurrentArea());
			 Vertex[] cellVertices =	polygon.getSortedVertices();
			 int randomStartIndexVertices = rand.nextInt(cellVertices.length);
			
			for(int i = 0; i < cellVertices.length; i++){
				Vertex v = cellVertices[((i+randomStartIndexVertices)% cellVertices.length)];
				if(!v.isWasAlreadyCalculated()){					
					conGradientOptimizer.relaxVertex(v);
					v.commitNewValues();
					//	minimizer.relaxForcesActingOnVertex(v);
					v.setWasAlreadyCalculated(true);
				}				
			}
			polygon.commitNewVertexValues();
			polygon.checkForT1Transition();
		}
		
		resetCalculationStatusOfAllCells();		
	}
	
	private void resetCalculationStatusOfAllCells(){
		for(CellPolygon actPolygon: cells){
			actPolygon.resetCalculationStatusOfAllVertices();			
			//if(actPolygon.isSelected())System.out.println("Cell No. "+ actPolygon.getId() + " Size after: " +actPolygon.getCurrentArea() + "(selected) Difference: " + (actPolygon.getCurrentArea() - actPolygon.getPreferredArea()));
		}
	}
	
	
	private void testCellAreaCalculation(){
		CellPolygon c = new CellPolygon(100, 100);
		c.addVertex(new Vertex(90, 90));
		c.addVertex(new Vertex(110, 90));
		c.addVertex(new Vertex(110, 110));
		c.addVertex(new Vertex(90, 110));
		System.out.println("Fl�che Quadradt soll 400 ist: " +Calculators.getCellArea(c));
		System.out.println("Umfang Quadradt soll 80 ist: " +Calculators.getCellPerimeter(c));
	}
	
	private void drawVisualization(Graphics2D g){		
		if(cells!= null) for(CellPolygon cellPol : cells) drawCellPolygon(g, cellPol, true);
		//drawErrorManhattanVersusEuclideanDistance(g);
	}
	
	private void drawErrorManhattanVersusEuclideanDistance(Graphics2D g){
		double radius = 100;
		double x = 200, y = 200;
		drawPoint(g, x, y, 3, Color.red);		
		
		double y1_new = 0, y2_new = 0;
		//Euclidean Distance of 100 around x, y;
		for(double i = x-radius; i <= x+radius; i+=0.01){
			y1_new = y + Math.sqrt(Math.pow(radius, 2) - Math.pow((i-x),2));
			y2_new = y - Math.sqrt(Math.pow(radius, 2) - Math.pow((i-x),2));
			drawPoint(g, i, y1_new, 1, Color.blue);
			drawPoint(g, i, y2_new, 1, Color.blue);
		}
		
		//Projection of the Error of the Manhattan Distance
		y1_new = 0;
		y2_new = 0;
		double x_new_circle = 0, y_new_circle = 0;
		double radius_new = 0;
		for(double alpha=0; alpha < 2*Math.PI; alpha += 0.001){
			x_new_circle = x + radius*Math.cos(alpha);
			y_new_circle = y + radius*Math.sin(alpha);		
			
			radius_new = Math.abs(x_new_circle-x) + Math.abs(y_new_circle - y);			
			drawPoint(g, (x + radius_new*Math.cos(alpha)), (y + radius_new*Math.sin(alpha)), 1, Color.red);
			
		}
	}
	
	private void drawCellPolygon(Graphics2D g, CellPolygon cell, boolean showCellAreaAndPerimeter){
		if(cell != null){
			//drawPoint(g, cell.getX(), cell.getY(), 2, Color.BLUE);
			Polygon p = new Polygon();
			
		
		//	cell.sortVerticesWithGrahamScan();
		
			
			for(Vertex v : cell.getSortedVertices()){	
				p.addPoint(v.getIntX(), v.getIntY());
				
			}
		//	g.drawString(""+ Math.round(Calculators.getCellArea(cell))*0.2 + ", " + Math.round(Calculators.getCellPerimeter(cell))*0.2, cell.getX()-10, cell.getY());
			
			
			if(cell.isSelected()){
				Color oldColor = g.getColor();
				g.setColor(Color.RED);
				g.fillPolygon(p);
				g.setColor(oldColor);
			}
			g.drawPolygon(p);
			
			
			
			for(Vertex v : cell.getUnsortedVertices()){	
				drawVertex(g, v, false);				
			}
			
			//drawVertex(g,Calculators.getCellCenter(cell),false);
		}
	}
	
	private void drawVertex(Graphics2D g, Vertex vertex, boolean showVertexId){
		if(vertex != null){
			if(showVertexId)g.drawString(""+ vertex.getId(), vertex.getIntX(), vertex.getIntY()-4);			
			if(vertex.isNew()) drawPoint(g, vertex.getIntX(), vertex.getIntY(), 3, Color.YELLOW);
			else drawPoint(g, vertex.getIntX(), vertex.getIntY(), 3, Color.BLUE);
		}
	}
	
	private void centerMe(JFrame frame){
		if(frame != null){
			Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
			frame.setLocation(((int)((screenDim.getWidth() /2) - (frame.getPreferredSize().getWidth()/2))), 
			((int)((screenDim.getHeight() /2) - (frame.getPreferredSize().getHeight()/2))));
		}
	}
	
	private void drawPoint(Graphics2D g, double x, double y, double size, Color c){
		if(x> 0 || y > 0){
			if(size % 2 != 0) size -= 1;
			Color oldColor = g.getColor();
			g.setColor(c);
			g.fillRect((int)(x-(size/2)), (int)(y-(size/2)), (int)(size+1), (int)(size+1));
			g.setColor(oldColor);
		}
	}

	
	public static void main(String[] args) {
		new TestVisualizationBiomechanics();
	}
	

}
