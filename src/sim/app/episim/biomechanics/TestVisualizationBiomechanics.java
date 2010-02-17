package sim.app.episim.biomechanics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;


public class TestVisualizationBiomechanics {
	
	JFrame frame;
	JPanel visualizationPanel;
	
	public TestVisualizationBiomechanics(){
		try{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e){			
			e.printStackTrace();
		}		 
		
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
		centerMe(frame);
		frame.pack();
		frame.setVisible(true);
	}
	
	
	
	
	public void drawVisualization(Graphics2D g){		
		for(Cell cell :Calculators.getStandardCellArray(1, 3)) drawCell(g, cell);						
	}
	
	private void drawCell(Graphics2D g, Cell cell){
		if(cell != null){
			//drawPoint(g, cell.getX(), cell.getY(), 2, Color.BLUE);
			Polygon p = new Polygon();
			
		/*	System.out.print("Vertex-Ids bevor: ");
			for(Vertex v : cell.getVertices()) System.out.print(v.getId()+ " ");
			System.out.println();*/
			cell.sortVertices();
		/*	System.out.print("Vertex-Ids nach: ");
			for(Vertex v : cell.getVertices()) System.out.print(v.getId()+ " ");
			System.out.println();*/
			for(Vertex v : cell.getVertices()){ 
				drawVertex(g, v);
				p.addPoint(v.getIntX(), v.getIntY());
			}
			//g.drawPolygon(p);
		}
	}
	
	private void drawVertex(Graphics2D g, Vertex vertex){
		if(vertex != null){
			g.drawString(""+ vertex.getId(), vertex.getIntX(), vertex.getIntY()-4);
			System.out.println(vertex.getId());
			drawPoint(g, vertex.getIntX(), vertex.getIntY(), 2, Color.BLUE);
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
