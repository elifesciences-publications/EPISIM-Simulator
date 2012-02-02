package sim.app.episim.model.biomechanics.vertexbased.test;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import sim.app.episim.EpisimProperties;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.model.biomechanics.vertexbased.VertexBasedMechanicalModelGP;
import sim.app.episim.model.biomechanics.vertexbased.VertexBasedModelController;
import sim.app.episim.model.biomechanics.vertexbased.calc.CellPolygonCalculator;
import sim.app.episim.model.biomechanics.vertexbased.geom.CellPolygon;
import sim.app.episim.model.biomechanics.vertexbased.geom.CellPolygonNetworkBuilder;
import sim.app.episim.model.biomechanics.vertexbased.geom.ContinuousVertexField;
import sim.app.episim.model.biomechanics.vertexbased.geom.Line;
import sim.app.episim.model.biomechanics.vertexbased.geom.Vertex;
import sim.app.episim.model.biomechanics.vertexbased.test.TestVisualizationPanel.TestVisualizationPanelPaintListener;
import sim.app.episim.model.biomechanics.vertexbased.util.CellCanvas;
import sim.app.episim.model.biomechanics.vertexbased.util.ColorRegistry;
import sim.app.episim.model.biomechanics.vertexbased.util.GlobalBiomechanicalStatistics;
import sim.app.episim.model.biomechanics.vertexbased.util.GlobalBiomechanicalStatistics.GBSValue;
import sim.app.episim.model.controller.BiomechanicalModelController;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.controller.ModelParameterModifier;
import sim.app.episim.tissue.TissueController;
import sim.app.episim.util.EpisimMovieMaker;


import ec.util.MersenneTwisterFast;
import episimbiomechanics.vertexbased.EpisimVertexBasedMC;
import episimexceptions.ModelCompatibilityException;
import episiminterfaces.CellPolygonProliferationSuccessListener;


public class TestVisualizationBiomechanics implements CellPolygonProliferationSuccessListener, TestVisualizationPanelPaintListener{
	
	public static final int ASSUMED_PROLIFERATION_CYCLE = 120;
	
	private final int CANVAS_ANCHOR_X = 40;
	private final int CANVAS_ANCHOR_Y = 40;
	
	private long simStepNo = 0;
	
	public enum VisualizationUnit{
		PROLIFERATINGCELLS("Proliferation Cells"),
		VERTICES("Vertices"),
		ATTACHED_VERTICES("Attached Vertices"),
		INTRUDER_VERTICES("Intruder Vertices"),
		CORRUPTLINES("Corrupt Lines"),
		TWOCELLLINES("Two Cell Lines"),
		OUTERLINES("Outer Lines"),
		INTERSECTINGCELLLINES("Intersecting Cell Lines"),
		CONTACTBASALLAYER("Contact Basallayer"),
		NEIGHBOURCONTACTBASALLAYER("Neighbour Contact Basal Layer");
		
		private String name;
		private VisualizationUnit(String name){
			this.name = name;
		}
		public String toString(){ return name; }
	}
	
	
	private enum SimState{SIMSTART, SIMSTOP;}
	private JFrame frame;
	private TestVisualizationPanel visualizationPanel;
	private CellPolygon[] cells;
	private Thread simulationThread;
	private SimState simulationState = null;
	private JButton startStopButton;
	
	private BufferedWriter csvWriter;
	
   private MersenneTwisterFast rand = new ec.util.MersenneTwisterFast(System.currentTimeMillis());
	
   private int numberOfCellDivisions = 0;
   
   private final int maxNumberOfCellDivisions;
   
   private final boolean autostart;
   
   

   private int lastSimStepNumberVideoFrameWasWritten = 0;
   private EpisimMovieMaker episimMovieMaker = null;
   private boolean headlessMode = false;
    
   private HashMap<VisualizationUnit, Boolean> visualizationConfigurationMap;   
   public static final boolean LOAD_STANDARD_MEMBRANE = true;   
   private CellCanvas cellCanvas;
   
   
   
   public TestVisualizationBiomechanics(boolean autoStart){
   	this(autoStart, null, null, Integer.MAX_VALUE, false);
   }
      
   public TestVisualizationBiomechanics(boolean autoStart, int numberOfCellDivisions){
   	this(autoStart, null, null, numberOfCellDivisions, false);
   }   
   
	public TestVisualizationBiomechanics(boolean autoStart, String moviePath, String csvPath, int numberOfCellDivisions, boolean headlessMode){	
      
		visualizationConfigurationMap = new HashMap<VisualizationUnit, Boolean>();		
		for(VisualizationUnit unit : VisualizationUnit.values()) visualizationConfigurationMap.put(unit, false);		
		
		this.maxNumberOfCellDivisions = numberOfCellDivisions;
		this.autostart = autoStart;
		this.headlessMode = headlessMode;
		
		if(moviePath != null) EpisimProperties.setProperty(EpisimProperties.MOVIE_PATH_PROP, moviePath);
		if(csvPath != null) createCsvWriter(csvPath);	
		
		//cells = CellPolygonNetworkBuilder.getSquareVertex(100, 100, 50, 6);
		
		//cells = CellPolygonNetworkBuilder.getStandardCellArray(1, 1, cellPolygonCalculator);
		//cells = CellPolygonNetworkBuilder.getStandardThreeCellArray(cellPolygonCalculator);
		
		if(LOAD_STANDARD_MEMBRANE)configureStandardMembrane();
		cells = CellPolygonNetworkBuilder.getStandardMembraneCellArray();
		VertexBasedModelController.getInstance().setCellPolygonArrayInCalculator(cells);
		ContinuousVertexField.initializeContinousVertexField(500, 420);
		cellCanvas = new CellCanvas(CANVAS_ANCHOR_X, CANVAS_ANCHOR_Y, 500, 420);
		
		for(CellPolygon pol: cells){ 
			pol.addProliferationAndApoptosisListener(this);
		}
		
		visualizationPanel = new TestVisualizationPanel();
		visualizationPanel.addTestVisualizationPanelPaintListener(this);
		visualizationPanel.setDoubleBuffered(true);
		visualizationPanel.setBackground(ColorRegistry.BACKGROUND_COLOR);
		visualizationPanel.setMinimumSize(new Dimension(500, 500));
		visualizationPanel.setSize(new Dimension(500, 500));
		visualizationPanel.setPreferredSize(new Dimension(500, 500));
		if(!headlessMode){
			try{
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			}
			catch (Exception e){			
				e.printStackTrace();
			}
			
			
			frame = new JFrame("Biomechanics Testvisualization");
			frame.setSize(850, 600);
			frame.setPreferredSize(new Dimension(850, 600));
			frame.getContentPane().setLayout(new BorderLayout());
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.getContentPane().add(visualizationPanel, BorderLayout.CENTER);			
			visualizationPanel.setBorder(BorderFactory.createLoweredBevelBorder());
			((JPanel)frame.getContentPane()).setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
			frame.add(buildVisualizationConfigPanel(), BorderLayout.EAST);
		}
		JPanel buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(2,5,2,5));
		startStopButton = new JButton("start");
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
		
		if(autostart){
			startStopButton.setText("stop");
    	   setSimulationState(SimState.SIMSTART);
		}
		
		buttonPanel.add(startStopButton);
		if(!headlessMode){
			frame.getContentPane().add(buttonPanel, BorderLayout.NORTH);
			
			JPanel mousePositionPanel = new JPanel(new FlowLayout());
			final JLabel positionLabel= new JLabel("mouse position: ");
			mousePositionPanel.add(positionLabel);
			visualizationPanel.addMouseMotionListener(new MouseMotionListener(){

				public void mouseDragged(MouseEvent e) {
					mouseMoved(e);
            }
				public void mouseMoved(MouseEvent e) {           
	            positionLabel.setText("mouse position: ("+ e.getX() + ", " + e.getY()+")");
            }});
			frame.getContentPane().add(mousePositionPanel, BorderLayout.SOUTH);
			centerMe(frame);
			frame.pack();
			frame.setResizable(false);
			frame.setVisible(true);	
		}		
	}
	
	
	private JPanel buildVisualizationConfigPanel(){
		JPanel configPanel = new JPanel(new GridLayout(VisualizationUnit.values().length, 1, 5, 5));
		for(VisualizationUnit unit : VisualizationUnit.values()){
			final VisualizationUnit actUnit = unit;
			final JCheckBox check =  new JCheckBox(actUnit.toString());
			check.setSelected(visualizationConfigurationMap.get(actUnit));
			check.addChangeListener(new ChangeListener(){

				public void stateChanged(ChangeEvent e) {
					visualizationConfigurationMap.put(actUnit, check.isSelected());         
            }});
			configPanel.add(check);
		}
		
		JPanel configWrapperPanel = new JPanel();
		configWrapperPanel.setPreferredSize(new Dimension(250, 500));
		configWrapperPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Visualization Configuration"), BorderFactory.createEmptyBorder(5,5,5,5)));
		configWrapperPanel.add(configPanel);
		return configWrapperPanel;
	}
	
	private void configureStandardMembrane(){
		ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().setBasalAmplitude_mikron(250);
		ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().setWidthInMikron(500);
		ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().setHeightInMikron(500);
		ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().setBasalOpening_mikron(12000);
		TissueController.getInstance().getTissueBorder().setBasalPeriodInMikron(550);
		TissueController.getInstance().getTissueBorder().setStartXOfStandardMembraneInMikron(0);
		TissueController.getInstance().getTissueBorder().setUndulationBaseLineInMikron(250);
		TissueController.getInstance().getTissueBorder().loadStandardMembrane();
	}	
	
	private void setSimulationState(SimState state){
		if(state == SimState.SIMSTART){				
				if(EpisimProperties.getProperty(EpisimProperties.MOVIE_PATH_PROP) != null){
					this.episimMovieMaker = new EpisimMovieMaker(null);
					 Graphics g = visualizationPanel.getGraphics();
			       final BufferedImage typicalImage = visualizationPanel.paint(true,false);
			       if(g != null)g.dispose();
			               
			       if (!episimMovieMaker.start(typicalImage)){
			      	 episimMovieMaker = null;  // failed
			       }
			       else episimMovieMaker.add(typicalImage);
				}
				
				
			simulationState = SimState.SIMSTART;
			simulationThread = new Thread(new Runnable(){	
				
				
				public void run() { 
		
				//  cells[cells.length/2].proliferate();
					 for(CellPolygon cell : cells){cell.proliferate();}
				  
				while(simulationState == SimState.SIMSTART){
					
					try{
						int randomStartIndexCells =  rand.nextInt(cells.length);
						CellPolygon polygon = null;
						
						List<CellPolygon> cellsList = Arrays.asList(cells);
						Collections.shuffle(cellsList);
						cells = cellsList.toArray(new CellPolygon[cellsList.size()]);
						for(int n = 0; n < cells.length; n++){
							polygon = cells[((n+randomStartIndexCells)% cells.length)];
							//	System.out.println("Cell No. "+ polygon.getId() + " Size before: " +polygon.getCurrentArea());
							polygon.step(simStepNo);							
						}
						GlobalBiomechanicalStatistics.getInstance().step(null); 
						simStepNo++;					
						
						if(!headlessMode) visualizationPanel.repaint();
						else paintToMovie();
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
			if(episimMovieMaker != null){
				if (!episimMovieMaker.stop())
		      {		           
		           ExceptionDisplayer.getInstance().displayException(new Exception("Your movie did not write to disk\ndue to a spurious JMF movie generation bug."));		             
		      }
		      episimMovieMaker = null;
			}
			if(headlessMode){ 
				System.exit(0);
				if(csvWriter != null){
					try{
	               csvWriter.close();
               }
               catch (IOException e){
	               ExceptionDisplayer.getInstance().displayException(e);
               }
				}
			}
			simStepNo = 0;
		}
	        
        
			
		
	}
	
	
	
	
	
	
	
	
	
	private void drawVisualization(Graphics2D g){
		cellCanvas.drawCanvasBorder(g);
		g.setColor(Color.BLACK);
		Color oldColor = g.getColor();
		Stroke oldStroke = g.getStroke();
		g.setColor(ColorRegistry.BASAL_LAYER_COLOR);
		g.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		AffineTransform transform = new AffineTransform();
		transform.translate(CANVAS_ANCHOR_X, -CANVAS_ANCHOR_Y);
		GeneralPath borderPath = TissueController.getInstance().getTissueBorder().getBasalLayerDrawPolygon();
		borderPath.transform(transform);
		g.draw(borderPath);
		g.setColor(oldColor);
		g.setStroke(oldStroke);
		CellPolygonCalculator cellPolygonCalculator = VertexBasedModelController.getInstance().getCellPolygonCalculator();
		if(cells!= null){
			for(CellPolygon cellPol : cells) drawCellPolygon(g, cellPol);
			if(visualizationConfigurationMap.get(VisualizationUnit.CORRUPTLINES)){
				for(Line corrLine : cellPolygonCalculator.getAllCorruptLinesOfVertexNetwork()) cellCanvas.highlightLine(g, corrLine, Color.MAGENTA);
			}
			if(visualizationConfigurationMap.get(VisualizationUnit.OUTERLINES)){
				for(Line outerLine : cellPolygonCalculator.getAllOuterLinesOfVertexNetwork()) cellCanvas.highlightLine(g, outerLine, ColorRegistry.CELL_BORDER_COLOR);
			}				
			if(visualizationConfigurationMap.get(VisualizationUnit.TWOCELLLINES)){
				for(Line line : cellPolygonCalculator.getAllLinesBelongingToOnlyTwoCellsOfVertexNetwork()){ 
					cellCanvas.highlightLine(g, line, Color.YELLOW);
				}
			}
			if(visualizationConfigurationMap.get(VisualizationUnit.INTERSECTINGCELLLINES)){			
				for(Line line : cellPolygonCalculator.getAllIntersectingLines()){					
					cellCanvas.highlightLine(g, line, Color.GREEN);
				}
			}			
			for(CellPolygon cellPol : cells){
				for(Vertex v : cellPol.getUnsortedVertices()){	
					drawVertex(g, v, null);				
				}
			}			
		}		
	}	
	
	
	private void drawErrorManhattanVersusEuclideanDistance(Graphics2D g){
		/*double radius = 100;
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
		}*/
	}
	
	private void drawCellPolygon(Graphics2D g, CellPolygon cell){
		if(cell != null){
			
			if(visualizationConfigurationMap.get(VisualizationUnit.PROLIFERATINGCELLS)){
				if(cell.isProliferating()){
					cellCanvas.drawCellPolygon(g, cell, null, ColorRegistry.CELL_FILL_COLOR_PROLIFERATING);
					return;
				}
			}
			if(visualizationConfigurationMap.get(VisualizationUnit.CONTACTBASALLAYER)){
				if(cell.hasContactToBasalLayer()){
					cellCanvas.drawCellPolygon(g, cell, null, ColorRegistry.CELL_FILL_COLOR_ATTACHED_BASALLAYER);
					return;
				}
			}
			if(visualizationConfigurationMap.get(VisualizationUnit.NEIGHBOURCONTACTBASALLAYER)){
				if(cell.hasContactToCellThatIsAttachedToBasalLayer()){
					cellCanvas.drawCellPolygon(g, cell, null, ColorRegistry.CELL_FILL_COLOR_NEIGHBOUR_ATTACHED_BASALLAYER);
					return;
				}
			}			
			
			if(cell.isDying()){
				cellCanvas.drawCellPolygon(g, cell, null, Color.RED);
				return;
			}
			
			cellCanvas.drawCellPolygon(g, cell, null, null);
		}
	}	
	
	private void drawVertex(Graphics2D g, Vertex vertex, Color color){
		if(vertex != null){			
			if(visualizationConfigurationMap.get(VisualizationUnit.VERTICES)) 
				cellCanvas.drawVertex(g, vertex, color==null? vertex.getVertexColor():color);
			if(vertex.isNew()) 
				cellCanvas.drawVertex(g, vertex, Color.YELLOW);
			if(visualizationConfigurationMap.get(VisualizationUnit.ATTACHED_VERTICES) && vertex.isAttachedToBasalLayer()) 
				cellCanvas.drawVertex(g, vertex, ColorRegistry.VERTEX_ATTACHED_TO_BASALLAYER);
			if(vertex.isIntruderVertex()) 
				cellCanvas.drawVertex(g, vertex, Color.MAGENTA);			
		}
	}
	
	private void centerMe(JFrame frame){
		if(frame != null){
			Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
			frame.setLocation(((int)((screenDim.getWidth() /2) - (frame.getPreferredSize().getWidth()/2))), 
			((int)((screenDim.getHeight() /2) - (frame.getPreferredSize().getHeight()/2))));
		}
	}	
	
	public void proliferationCompleted(CellPolygon oldCell, CellPolygon newCell){
		if(newCell != null){
			CellPolygon[] newCellArray = new CellPolygon[cells.length+1];
			System.arraycopy(cells, 0, newCellArray, 0, cells.length);
			newCellArray[cells.length] = newCell;
			cells= newCellArray;
			VertexBasedModelController.getInstance().setCellPolygonArrayInCalculator(cells);
			
			newCell.addProliferationAndApoptosisListener(this);			
			
			if(csvWriter != null){				
	    	  	try{
	            csvWriter.write(GlobalBiomechanicalStatistics.getInstance().getCSVFileData(cells));
	            csvWriter.flush();
            }
            catch(IOException e){
            	ExceptionDisplayer.getInstance().displayException(e);
            }
			}			
			
			this.numberOfCellDivisions++;
				 
			VertexBasedModelController.getInstance().getCellPolygonCalculator().randomlySelectCellForProliferation();
		
			if(numberOfCellDivisions >= this.maxNumberOfCellDivisions 
					|| GlobalBiomechanicalStatistics.getInstance().get(GBSValue.SIM_STEP_NUMBER) > ((((double)ASSUMED_PROLIFERATION_CYCLE)+10)*((double)maxNumberOfCellDivisions))){
				startStopButton.setText("start");
				setSimulationState(SimState.SIMSTOP);
			}			
		}
	   
   }
	public void maturationCompleted(CellPolygon pol) {
		int proliferatingCells = 0;
		for(CellPolygon cellPol : this.cells){ 
			if(cellPol.isProliferating()) proliferatingCells++;
		}		
		if(proliferatingCells < (this.cells.length / 3)) VertexBasedModelController.getInstance().getCellPolygonCalculator().randomlySelectCellForProliferation();	   
   }	
	
	public void apoptosisCompleted(CellPolygon pol) {
	   if(pol!= null){
	   	ArrayList<CellPolygon> cellList = new ArrayList<CellPolygon>();
	   	for(int i = 0; i < cells.length; i++) cellList.add(cells[i]);
	   	cellList.remove(pol);
	   	cells = new CellPolygon[cellList.size()];
	   	cellList.toArray(cells);
	   	pol.removeProliferationAndApoptosisListener(this);
	   	VertexBasedModelController.getInstance().setCellPolygonArrayInCalculator(cells);
	   }	   
   }	
	
	private void createCsvWriter(String path){
		try{
			csvWriter = new BufferedWriter(new FileWriter(path, true));
			csvWriter.write(GlobalBiomechanicalStatistics.getInstance().getCSVFileColumnHeader());
         csvWriter.flush();
      }
		catch (IOException e){
			ExceptionDisplayer.getInstance().displayException(e);
		}
	}
	
	
	
	public static void main(String[] args) {
		String moviePath = null;
		String csvPath = null;
		int maxNumberOfProliferation = Integer.MAX_VALUE;
		boolean headless = false;
		
		try{
	      ModelController.getInstance().getBioMechanicalModelController().loadModelFile((new EpisimVertexBasedMC()).getBiomechanicalModelId());
      }
      catch (ModelCompatibilityException e1){
	     e1.printStackTrace();
      }
      
      VertexBasedMechanicalModelGP globalParameters = null;
      
      if(ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters() != null
      	&& ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters() instanceof VertexBasedMechanicalModelGP){
      	globalParameters = (VertexBasedMechanicalModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
      }
      
      
      
		
		if(args != null && args.length >= 0){
			EpisimProperties.setProperty(EpisimProperties.SIMULATOR_CONSOLE_INPUT_PROP, EpisimProperties.ON);
			ModelParameterModifier modifier = new ModelParameterModifier();
			for(int i = 0; i < args.length; i++){
				if(args[i] != null && (i+1)<args.length){					
					if(args[i].equals("-mp")) moviePath = args[i+1];
					else if(args[i].equals("-fps")) EpisimProperties.setProperty(EpisimProperties.FRAMES_PER_SECOND_PROP, args[i+1]);
					else if(args[i].equals("-id")) EpisimProperties.setProperty(EpisimProperties.SIMULATOR_SIMULATION_RUN_ID, args[i+1]);
					else if(args[i].equals("-mnp")) maxNumberOfProliferation = Integer.parseInt(args[i+1]);
					else if(args[i].equals("-p")) modifier.setGlobalModelPropertiesToValuesInPropertiesFile(globalParameters, new File(args[i+1]));
					else if(args[i].equals("-csv")) csvPath = args[i+1];				
				}
				if(args[i] != null && args[i].equals("-headless")) headless=true; 
			}						
		}		
		new TestVisualizationBiomechanics(false, moviePath, csvPath, maxNumberOfProliferation, headless);
	}


	public void paintWasCalled(Graphics2D graphics) {
		drawVisualization(graphics);
		paintToMovie();
   }
	public void paintToMovieBufferWasCalled(Graphics2D graphics) {
		drawVisualization(graphics);		
   }	
	
	public void paintToMovie()
   {       
       if (episimMovieMaker != null && GlobalBiomechanicalStatistics.getInstance().get(GBSValue.SIM_STEP_NUMBER) > lastSimStepNumberVideoFrameWasWritten)
       {
      	 episimMovieMaker.add(visualizationPanel.paint(true,false));
          lastSimStepNumberVideoFrameWasWritten = (int)GlobalBiomechanicalStatistics.getInstance().get(GBSValue.SIM_STEP_NUMBER);
       }     
   }

	
}
