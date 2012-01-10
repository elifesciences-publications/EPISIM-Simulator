package sim.app.episim.visualization;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;

import binloc.ProjectLocator;

import com.keypoint.PngEncoder;

import episimbiomechanics.vertexbased.EpisimVertexBasedModelConnector;
import episimexceptions.ModelCompatibilityException;

import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.gui.ExtendedFileChooser;
import sim.app.episim.model.visualization.CellEllipse;
import sim.app.episim.tissue.TissueController;
import sim.app.episim.util.ExtendedColorWell;
import sim.app.episim.util.ExtendedLabelledList;
import sim.util.gui.ColorWell;
import sim.util.gui.LabelledListHack;



public class TestVisualizationMain {
	
	public static final String BACKGROUND_COLOR_PROP ="Background";
	public static final String BASALLAYER_COLOR_PROP ="BasalLayer";
	public static final String OUTERSURFACE_COLOR_PROP ="OuterSurface";
	public static final String CELL_COLOR_PROP ="Cell";
	public static final String CELLMEMBRANE_COLOR_PROP ="CellMembrane";
	public static final String CELLCENTER_COLOR_PROP ="CellCenter";
	
	private JFrame mainFrame;
	private JLabel loadedFileLabel;
	private TestCanvas canvas;
	
	
	
	private boolean tissueImportMode = false;
	private File actImportedTissuePath = null;
	
	
	private static final String loadedFileLabelText = "    Loaded File: ";
	
	private JDialog colorChooseDialog;
	
	
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
		mainFrame.setIconImage(new ImageIcon(TestVisualizationMain.class.getResource("icon.gif")).getImage());
		
		//Menü
		JMenuBar menuBar = new JMenuBar();
		mainFrame.setJMenuBar(menuBar);
		
		JMenu menu = new JMenu("Menu");
		JMenuItem loadFileMenuItem = new JMenuItem("Load Tissue File");
		final JMenuItem changeColorsMenuItem = new JMenuItem("Change Colors");
		changeColorsMenuItem.setEnabled(false);
		final JMenuItem resetColorsMenuItem = new JMenuItem("Reset Default Colors");
		resetColorsMenuItem.setEnabled(false);
		final JButton saveImageButton = new JButton(new ImageIcon(TestVisualizationMain.class.getResource("Camera.png")));
		saveImageButton.setEnabled(false);
		menu.add(loadFileMenuItem);
		menu.add(changeColorsMenuItem);
		menu.add(resetColorsMenuItem);
		menuBar.add(menu);
		menuBar.add(saveImageButton);
		loadedFileLabel = new JLabel(loadedFileLabelText);
		menuBar.add(loadedFileLabel);
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
	         	canvas.setImportedTissueVisualizationMode(true);
	         	canvas.addImportedCells(TissueController.getInstance().getImportedCells());	         
	         	
	         	loadedFileLabel.setText(loadedFileLabelText + actImportedTissuePath.getAbsolutePath());
	         	int width = (int)(TissueController.getInstance().getTissueBorder().getWidthInPixels()+60);
	         	int height = (int)(TissueController.getInstance().getTissueBorder().getHeightInPixels()+110);
	         	
	         	mainFrame.setSize(new Dimension(width,height));
	         	mainFrame.setPreferredSize(new Dimension(width,height));
	         	centerMe(mainFrame);
	         	mainFrame.repaint();
	         	changeColorsMenuItem.setEnabled(true);
	         	resetColorsMenuItem.setEnabled(true);
	         	saveImageButton.setEnabled(true);
	         }
	         	
	         
         }
			
		});
		
		saveImageButton.addActionListener(new ActionListener(){

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
		
		changeColorsMenuItem.addActionListener(new ActionListener(){			
         public void actionPerformed(ActionEvent e) {
         	loadColorConfigProperties();
      		createColorChooseDialog();
	         colorChooseDialog.pack();
	         colorChooseDialog.setPreferredSize(new Dimension(200,colorChooseDialog.getHeight()));
	         colorChooseDialog.setSize(new Dimension(200,colorChooseDialog.getHeight()));
	         centerMe(colorChooseDialog);
	         colorChooseDialog.setVisible(true);
	         
         }});
		
		resetColorsMenuItem.addActionListener(new ActionListener(){			
         public void actionPerformed(ActionEvent e) {
	        canvas.resetColors();
	        canvas.repaint();
	        storeColorConfigProperties();	         
         }});
		
		mainFrame.getContentPane().setLayout(new BorderLayout(0,0));
		
		mainFrame.setPreferredSize(new Dimension((int) java.awt.Toolkit.getDefaultToolkit().getScreenSize().getWidth()/2,
				(int) java.awt.Toolkit.getDefaultToolkit().getScreenSize().getHeight()/2));
		
		
		mainFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		
		mainFrame.getContentPane().add(canvas, BorderLayout.CENTER);
		
		
		
		
		
		loadColorConfigProperties();
		createColorChooseDialog();
		
		centerMe(mainFrame);
		mainFrame.pack();
		mainFrame.setVisible(true);
		
		
	}
	
	public static void main(String[] args) {

		new TestVisualizationMain();

	}
	
	
	private void createColorChooseDialog(){
		colorChooseDialog = new JDialog(this.mainFrame, "Change Colors");
		colorChooseDialog.setModal(true);
		colorChooseDialog.setResizable(true);
		JPanel mainPanel = new JPanel(new BorderLayout());
		ExtendedLabelledList list = new ExtendedLabelledList("Colors");
		list.setInsets(new Insets(3,3,3,3));
		ExtendedColorWell colorwellBackground = new ExtendedColorWell(colorChooseDialog, canvas.getBackgroundColor())
       {
			 public Color changeColor(Color c) 
          {
				 canvas.setBackgroundColor(c);
				 canvas.repaint();
				 return c;
          }
       };
       
       ExtendedColorWell colorwellOuterSurface = new ExtendedColorWell(colorChooseDialog, canvas.getOuterSurfaceColor())
       {
			 public Color changeColor(Color c) 
          {
				 canvas.setOuterSurfaceColor(c);
				 canvas.repaint();
				 return c;
          }
       };
       ExtendedColorWell colorwellBasementMembrane = new ExtendedColorWell(colorChooseDialog, canvas.getBasementMembraneColor())
       {
			 public Color changeColor(Color c) 
          {
				 canvas.setBasementMembraneColor(c);
				 canvas.repaint();
				 return c;
          }
       };
       ExtendedColorWell colorwellCell = new ExtendedColorWell(colorChooseDialog, canvas.getCellColor())
       {
			 public Color changeColor(Color c) 
          {
				 canvas.setCellColor(c);
				 canvas.repaint();
				 return c;
          }
       };
       ExtendedColorWell colorwellCellMembrane = new ExtendedColorWell(colorChooseDialog, canvas.getCellMembraneColor())
       {
			 public Color changeColor(Color c) 
          {
				 canvas.setCellMembraneColor(c);
				 canvas.repaint();
				 return c;
          }
       };
       ExtendedColorWell colorwellCellCenter = new ExtendedColorWell(colorChooseDialog, canvas.getCellCenterColor())
       {
			 public Color changeColor(Color c) 
          {
				 canvas.setCellCenterColor(c);
				 canvas.repaint();
				 return c;
          }
       };
       
       list.add(new JLabel("Background"), colorwellBackground);
       list.add(new JLabel("Basal Layer"), colorwellBasementMembrane);
       list.add(new JLabel("Outer Surface"),  colorwellOuterSurface);
       list.add(new JLabel("Cell"),  colorwellCell);
       list.add(new JLabel("Cell Membrane"),  colorwellCellMembrane);
       list.add(new JLabel("Cell Center"),  colorwellCellCenter);
       
       
       JButton okButton = new JButton("Close and Save");
       okButton.addActionListener(new ActionListener(){			
         public void actionPerformed(ActionEvent e) {

         	storeColorConfigProperties();
         	colorChooseDialog.setVisible(false);
	         
         }});
       
       JPanel buttonPanel = new JPanel(new GridBagLayout());
       GridBagConstraints c = new GridBagConstraints();
       c.fill = GridBagConstraints.NONE;
       c.anchor = GridBagConstraints.CENTER;
       c.insets = new Insets(10,5,0,5);
       buttonPanel.add(okButton, c);
       mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
       mainPanel.add(list, BorderLayout.CENTER);
       mainPanel.add(buttonPanel, BorderLayout.SOUTH);
       colorChooseDialog.getContentPane().add(mainPanel, BorderLayout.CENTER);
	}
	
	
	private void initCanvas(){
		canvas.addMouseListener(new MouseAdapter(){
			
			 public void mouseClicked(MouseEvent e){
				if(e.getButton() == MouseEvent.BUTTON1 && !tissueImportMode){
				//	canvas.drawCellEllipse(e.getX(), e.getY(), 100, 45, Color.BLUE);
					//canvas.drawCellPolygon(e.getX(), e.getY());
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
	
	private void centerMe(Window frame){
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

	private void loadColorConfigProperties(){
		Properties properties;
		
		properties = new Properties();
		FileInputStream stream;
      try{
	      stream = new FileInputStream(ProjectLocator.getPathOf("config").getAbsolutePath().concat(System.getProperty("file.separator")).concat("cellvisualization.properties"));
         properties.load(stream);
         stream.close();
      }
      catch (IOException e1){
	      ExceptionDisplayer.getInstance().displayException(e1);
      }
      catch (URISyntaxException e2){
      	ExceptionDisplayer.getInstance().displayException(e2);
      }
      canvas.setBackgroundColor(getColorForColorString(properties.getProperty(BACKGROUND_COLOR_PROP)));
      canvas.setBasementMembraneColor(getColorForColorString(properties.getProperty(BASALLAYER_COLOR_PROP)));
      canvas.setOuterSurfaceColor(getColorForColorString(properties.getProperty(OUTERSURFACE_COLOR_PROP)));
      canvas.setCellColor(getColorForColorString(properties.getProperty(CELL_COLOR_PROP)));
      canvas.setCellMembraneColor(getColorForColorString(properties.getProperty(CELLMEMBRANE_COLOR_PROP)));
      canvas.setCellCenterColor(getColorForColorString(properties.getProperty(CELLCENTER_COLOR_PROP)));
	}
	
	private void storeColorConfigProperties(){
		Properties properties = new Properties();
		
		properties.setProperty(BACKGROUND_COLOR_PROP, getColorStringForColor(canvas.getBackgroundColor()));
		properties.setProperty(BASALLAYER_COLOR_PROP, getColorStringForColor(canvas.getBasementMembraneColor()));
		properties.setProperty(OUTERSURFACE_COLOR_PROP, getColorStringForColor(canvas.getOuterSurfaceColor()));
		properties.setProperty(CELL_COLOR_PROP, getColorStringForColor(canvas.getCellColor()));
		properties.setProperty(CELLMEMBRANE_COLOR_PROP, getColorStringForColor(canvas.getCellMembraneColor()));
		properties.setProperty(CELLCENTER_COLOR_PROP, getColorStringForColor(canvas.getCellCenterColor()));
		
		FileOutputStream stream;
      try{
	      stream = new FileOutputStream(ProjectLocator.getPathOf("config").getAbsolutePath().concat(System.getProperty("file.separator")).concat("cellvisualization.properties"));
         properties.store(stream, "");
         stream.close();
      }
      catch (IOException e1){
	      ExceptionDisplayer.getInstance().displayException(e1);
      }
      catch (URISyntaxException e2){
      	ExceptionDisplayer.getInstance().displayException(e2);
      }
      canvas.setBackground(getColorForColorString(properties.getProperty(BACKGROUND_COLOR_PROP)));
      canvas.setBasementMembraneColor(getColorForColorString(properties.getProperty(BASALLAYER_COLOR_PROP)));
      canvas.setOuterSurfaceColor(getColorForColorString(properties.getProperty(OUTERSURFACE_COLOR_PROP)));
      canvas.setCellColor(getColorForColorString(properties.getProperty(CELL_COLOR_PROP)));
      canvas.setCellMembraneColor(getColorForColorString(properties.getProperty(CELLMEMBRANE_COLOR_PROP)));
      canvas.setCellCenterColor(getColorForColorString(properties.getProperty(CELLCENTER_COLOR_PROP)));
	}

	private String getColorStringForColor(Color color){
		if(color != null){
			StringBuffer colorString = new StringBuffer();
			colorString.append(color.getRed());
			colorString.append(",");
			colorString.append(color.getGreen());
			colorString.append(",");
			colorString.append(color.getBlue());
			return colorString.toString();
		}
		return "0,0,0";
	}
	
	private Color getColorForColorString(String colorString){
		if(colorString != null && colorString.trim().length() > 0){
			String[] rgb = colorString.split(",");
			if(rgb.length == 3){
				try{
					int r = Integer.parseInt(rgb[0]);
					int g = Integer.parseInt(rgb[1]);
					int b = Integer.parseInt(rgb[2]);
					return new Color(r,g,b);
				}
				catch(NumberFormatException e){
					return Color.BLACK;
				}				
			}
		}		
		return Color.BLACK;
	}
	
}
