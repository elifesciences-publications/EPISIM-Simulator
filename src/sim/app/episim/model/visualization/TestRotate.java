package sim.app.episim.model.visualization;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;

import javax.swing.JFrame;
import javax.swing.JPanel;


public class TestRotate {
	
	public class CustomPanel extends JPanel{
		public void paint(Graphics g){
			Graphics2D graphics = (Graphics2D)g;
			
			int x = 50;
			int y = 50;
			int major = 100;
			int minor = major / 2;
			
			graphics.fillRect(x+(major/2)-1, y+(minor/2)-1, 2, 2);
			
			Ellipse2D ell = new Ellipse2D.Double(x, y, major, minor);
			
			graphics.draw(ell);
			AffineTransform trans = new AffineTransform();
			trans.rotate(Math.toRadians(-45), x+(major/2), y+(minor/2));
			graphics.draw(trans.createTransformedShape(new Ellipse2D.Double(x, y, major, minor)));
			
		}
	}
	public void showFrame(){
		JFrame frame = new JFrame();
		frame.getContentPane().add(new CustomPanel());
		frame.setSize(300, 300);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public static void main(String[] args){
		(new TestRotate()).showFrame();
	}

}
