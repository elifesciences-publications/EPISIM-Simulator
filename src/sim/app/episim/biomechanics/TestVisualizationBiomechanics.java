package sim.app.episim.biomechanics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
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
	
	
	
	
	public void drawVisualization(Graphics2D g ){
		
	}
	
	private void centerMe(JFrame frame){
		if(frame != null){
			Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
			frame.setLocation(((int)((screenDim.getWidth() /2) - (frame.getPreferredSize().getWidth()/2))), 
			((int)((screenDim.getHeight() /2) - (frame.getPreferredSize().getHeight()/2))));
		}
	}	

	
	public static void main(String[] args) {
		new TestVisualizationBiomechanics();
	}
	

}
