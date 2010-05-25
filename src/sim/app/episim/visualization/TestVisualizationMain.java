package sim.app.episim.visualization;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import sim.app.episim.gui.ExtendedFileChooser;
import sim.app.episim.tissue.TissueController;


public class TestVisualizationMain {
	
	private JFrame mainFrame;
	
	private TestCanvas canvas;
	
	
	public TestVisualizationMain(){
		
		
		try{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e){
			
			e.printStackTrace();
		}	
		 
				
		mainFrame = new JFrame();
		canvas = new TestCanvas();
				
		initCanvas();
				
		mainFrame.setTitle("Episim Tissue / Cell Visualization");
		
		//Men�
		JMenuBar menuBar = new JMenuBar();
		mainFrame.setJMenuBar(menuBar);
		
		JMenu menu = new JMenu("File");
		JMenuItem loadFileMenuItem = new JMenuItem("Load Tissue File");
		
		menu.add(loadFileMenuItem);
		
		menuBar.add(menu);
		
		final ExtendedFileChooser xmlChooser = new ExtendedFileChooser("xml");
		
		loadFileMenuItem.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {

	         xmlChooser.setDialogTitle("Load Tissue File");
	         xmlChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	         
	         if(JFileChooser.APPROVE_OPTION== xmlChooser.showOpenDialog(mainFrame)){
	         	TissueController.getInstance().loadTissue(xmlChooser.getSelectedFile());
	         	canvas.addImportedCells(TissueController.getInstance().getImportedCells());
	         	mainFrame.setSize(new Dimension(TissueController.getInstance().getTissueWidth()+30, TissueController.getInstance().getTissueHeight()+30));
	         	mainFrame.repaint();
	         }
	         	
	         
         }
			
		});
		
		mainFrame.getContentPane().setLayout(new BorderLayout(0,0));
		
		mainFrame.setPreferredSize(new Dimension((int) java.awt.Toolkit.getDefaultToolkit().getScreenSize().getWidth()/2,
				(int) java.awt.Toolkit.getDefaultToolkit().getScreenSize().getHeight()/2));
		
		
		mainFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		
		mainFrame.getContentPane().add(canvas, BorderLayout.CENTER);
		
		centerMe(mainFrame);
		mainFrame.pack();
		mainFrame.setVisible(true);
		
		
	}
	
	public static void main(String[] args) {

		new TestVisualizationMain();

	}
	
	
	private void initCanvas(){
		canvas.addMouseListener(new MouseAdapter(){
			
			 public void mouseClicked(MouseEvent e){
				if(e.getButton() == MouseEvent.BUTTON1)canvas.drawCellEllipse(e.getX(), e.getY(), 100, 45, Color.BLUE);
				else 
				 if(e.getButton() == MouseEvent.BUTTON3){
					CellEllipse cell = canvas.pickCellEllipse(e.getX(), e.getY());
					if(cell != null){
						JOptionPane.showMessageDialog(mainFrame, "The RGB value of the selected Cell is: ("+ cell.getColor().getRed()+ ", " + cell.getColor().getGreen() + ", " + cell.getColor().getBlue()+")", "Cell-Info", JOptionPane.INFORMATION_MESSAGE);
					}
				}
			 }
			 
			 public void mousePressed(MouseEvent e){
				 
				 canvas.pickCellEllipse(e.getX(), e.getY());
			 }
			 
			 public void mouseReleased(MouseEvent e){
				 canvas.releaseCellEllipse();
			 }
		});
		
		canvas.addMouseMotionListener(new MouseAdapter(){
				 
			 public void mouseDragged(MouseEvent e){
				 
				 canvas.dragCellEllipse(e.getX(), e.getY());
			 }
					 
		});
		
	}
	
	private void centerMe(JFrame frame){
		if(frame != null){
			Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
			frame.setLocation(((int)((screenDim.getWidth() /2) - (frame.getPreferredSize().getWidth()/2))), 
			((int)((screenDim.getHeight() /2) - (frame.getPreferredSize().getHeight()/2))));
		}
	}

	
	
}
