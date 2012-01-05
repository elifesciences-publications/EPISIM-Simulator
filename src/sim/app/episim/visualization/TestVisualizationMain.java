package sim.app.episim.visualization;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import com.keypoint.PngEncoder;

import episimbiomechanics.vertexbased.EpisimVertexBasedModelConnector;
import episimexceptions.ModelCompatibilityException;

import sim.app.episim.gui.ExtendedFileChooser;
import sim.app.episim.gui.ImageLoader;
import sim.app.episim.model.biomechanics.vertexbased.util.CellCanvas;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.visualization.CellEllipse;
import sim.app.episim.tissue.TissueController;



public class TestVisualizationMain {
	
	private JFrame mainFrame;
	
	private TestCanvas canvas;
	
	
	
	private boolean tissueImportMode = false;
	private File actImportedTissuePath = null;
	
	public TestVisualizationMain(){
	/*	try{
	      ModelController.getInstance().getBioMechanicalModelController().loadModelFile((new EpisimVertexBasedModelConnector()).getBiomechanicalModelId());
      }
      catch (ModelCompatibilityException e1){
	     e1.printStackTrace();
      }*/
		
		try{
			
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			
		}
		catch (Exception e){
			
			e.printStackTrace();
		}	
		 
				
		mainFrame = new JFrame();
		canvas = new TestCanvas();
				
		initCanvas();
				
		mainFrame.setTitle("EPISIM Simulator - Tissue Visualization");
		mainFrame.setIconImage(new ImageIcon(ImageLoader.class.getResource("icon.gif")).getImage());
		
		//Menü
		JMenuBar menuBar = new JMenuBar();
		mainFrame.setJMenuBar(menuBar);
		
		JMenu menu = new JMenu("File");
		JMenuItem loadFileMenuItem = new JMenuItem("Load Tissue File");
		final JMenuItem saveImageMenuItem = new JMenuItem("Save Image");
		saveImageMenuItem.setEnabled(false);
		menu.add(loadFileMenuItem);
		menu.add(saveImageMenuItem);
		menuBar.add(menu);
		
		final ExtendedFileChooser xmlChooser = new ExtendedFileChooser("xml");
		
		loadFileMenuItem.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {

	         xmlChooser.setDialogTitle("Load Tissue File");
	         xmlChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	         
	         if(JFileChooser.APPROVE_OPTION== xmlChooser.showOpenDialog(mainFrame)){
	         	actImportedTissuePath = xmlChooser.getSelectedFile();
	         	TissueController.getInstance().loadTissue(actImportedTissuePath);
	         	tissueImportMode = true;
	         	canvas.clearPanel();
	         	
	         	canvas.addImportedCells(TissueController.getInstance().getImportedCells());	         
	         	canvas.setImportedTissueVisualizationMode(true);
	         	
	         	int width = (int)(TissueController.getInstance().getTissueBorder().getWidthInPixels()+60);
	         	int height = (int)(TissueController.getInstance().getTissueBorder().getHeightInPixels()+100);
	         	
	         	mainFrame.setSize(new Dimension(width,height));
	         	mainFrame.setPreferredSize(new Dimension(width,height));
	         	centerMe(mainFrame);
	         	mainFrame.repaint();
	         	
	         	saveImageMenuItem.setEnabled(true);
	         }
	         	
	         
         }
			
		});
		
		saveImageMenuItem.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				if(actImportedTissuePath != null){
					File imagePath = new File(actImportedTissuePath.getAbsolutePath().substring(0, actImportedTissuePath.getAbsolutePath().length()-3) +"png");
					try{
	               savePNGImageOfCellCanvas(imagePath);
               }
               catch (IOException e1){
	               // TODO Auto-generated catch block
	               e1.printStackTrace();
               }
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
				if(e.getButton() == MouseEvent.BUTTON1 && !tissueImportMode){
				//	canvas.drawCellEllipse(e.getX(), e.getY(), 100, 45, Color.BLUE);
					canvas.drawCellPolygon(e.getX(), e.getY());
					//canvas.drawBigVertex(e.getX(), e.getY());
				}
				else 
				 if(e.getButton() == MouseEvent.BUTTON3){
					CellEllipse cell = canvas.pickCellEllipse(e.getX(), e.getY());
					if(cell != null){
						JOptionPane.showMessageDialog(mainFrame, "The ID of the selected Cell is: "+ cell.getId(), "Cell-Info", JOptionPane.INFORMATION_MESSAGE);
					}
				}
			 }
			 
			 public void mousePressed(MouseEvent e){
				 
				 canvas.pickCellEllipse(e.getX(), e.getY());
				// canvas.pickCellPolygon(e.getX(), e.getY());
				// canvas.pickBigVertex(e.getX(), e.getY());
			 }
			 
			 public void mouseReleased(MouseEvent e){
				 canvas.releaseCellEllipse();
				// canvas.releaseCellPolygon();
				 //canvas.releaseBigVertex();
			 }
		});
		
		canvas.addMouseMotionListener(new MouseAdapter(){
				 
			 public void mouseDragged(MouseEvent e){
				 
				 canvas.dragCellEllipse(e.getX(), e.getY());
				// canvas.dragCellPolygon(e.getX(), e.getY());
				// canvas.dragBigVertex(e.getX(), e.getY());
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
	

	private void savePNGImageOfCellCanvas(File output) throws IOException {
	    output.delete();
	    
	    BufferedImage image = new BufferedImage(this.canvas.getWidth(), this.canvas.getHeight(), BufferedImage.TYPE_INT_RGB);
	    this.canvas.paintAll(image.getGraphics());
	    PngEncoder enc = new PngEncoder(image);
	    enc.setXDpi(300);
	    enc.setYDpi(300);
	    FileOutputStream fileOut = new FileOutputStream(output);
	    fileOut.write(enc.pngEncode());
	    fileOut.flush();
	    fileOut.close(); 
	 }



	
	
}
