package sendreceive;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.util.HashSet;

import javax.swing.JFrame;
import javax.swing.JPanel;

import sim.SimStateServer;


public class TestFrame {
	
	private final HashSet<Shape> shapes = new HashSet<Shape>();
	private JPanel drawPanel;
	public TestFrame(){
		
		drawPanel = new JPanel(){
			public void paint(Graphics g){
				super.paint(g);
				Graphics2D g2D = (Graphics2D) g;
				Shape[] shapeArray = shapes.toArray(new Shape[shapes.size()]);
				for(int i = 0; i < shapeArray.length; i++){
					Shape shape = shapeArray[i];
					//AffineTransform trans = new AffineTransform();
				//	trans.setToScale(0.25, 0.25);
				//	shape = trans.createTransformedShape(shape);
					g2D.draw(shape);
					
				}
				
			}
		};
		drawPanel.setDoubleBuffered(true);
		drawPanel.setBackground(Color.WHITE);
		JFrame frame = new JFrame();
		frame.getContentPane().add(drawPanel, BorderLayout.CENTER);
		frame.setSize(900, 900);
		frame.setVisible(true);
	}
	
	
	
	long lastSimStep = 0;
	public void paintShape(Shape shape){
		long simStep = SimStateServer.getInstance().getSimStepNumber();
		if(simStep > lastSimStep){
			shapes.clear();
			lastSimStep = simStep;
		}
		shapes.add(shape);
		
	}

}
