package sim.app.episim.visualization;


/*
* IntersectionDemo.java
*/
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
public class IntersectionDemo extends JFrame {
    private Grafik graf;
    public IntersectionDemo() {
        super("Intersection Demo");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(310,250);
        setLocationRelativeTo(null);
        graf = new Grafik();
        add(graf);
    }
    public static void main(String args[]) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new IntersectionDemo().setVisible(true);               
            }
        });
    }
}
class Grafik extends JPanel{
    private Shape s1, s2;//the two ellipses
    private int xM, yM;//mouse position
    private int[] iP = new int[8];//intersection points
    
    private Area resultingArea;
    
    public Grafik(){
        addMouseMotionListener(new MouseMotionListener() {
            public void mouseDragged(MouseEvent e) {
            }
            public void mouseMoved(MouseEvent e) {
                xM = e.getX();
                yM = e.getY();
                repaint();
            }
        });
        s1 = new Ellipse2D.Double(100,10,20,40);
        s2 = new Ellipse2D.Double(100,30,20,40);
        Area a1 = new Area(s1);
        Area a2 = new Area(s2);
        a1.intersect(a2);
        resultingArea = a1;
        if(!a1.isEmpty()){
	        PathIterator it = a1.getPathIterator(null);
	        double[] d = new double[6];
	        double xOLD = 0;
	        double yOLD = 0;
	        int i = 0;
	        int numberOfIntersectionPoints = 0;
	        boolean newIteration = true;
	        while(newIteration){
	            int type = it.currentSegment(d);
	            
	            switch(type){
	            
	           case PathIterator.SEG_CUBICTO:
	            	
	           case PathIterator.SEG_LINETO:{
	                if(Math.round(d[0]) == xOLD && Math.round(d[1]) == yOLD){
	                    iP[i++] = (int) xOLD;
	                    iP[i++] = (int) yOLD;
	                    
	                    numberOfIntersectionPoints++;
	                }
	                xOLD = Math.round(d[4]);
	                yOLD = Math.round(d[5]);
	                
	            }
	           	break;
	            case PathIterator.SEG_CLOSE:{
	            	if(i % 2 != 0){
	            		iP[i++] = (int) Math.round(d[4]);
	            		iP[i++] = (int) Math.round(d[5]);
	            		
	            	}
	            	newIteration=false;
	            }
	            }
	            it.next();
	            
	        }
        }
    }
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.draw(s1);
        g2.draw(s2);
        //g2.draw(resultingArea);
        g2.drawString("Mouse: "+xM+", "+yM, 5, 11);
        for (int i = 0; i < 4; i++)
            g2.drawString(iP[i*2]+", "+iP[i*2+1], iP[i*2], iP[i*2+1]);
    }
}
