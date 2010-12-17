package sim.app.episim.model.biomechanics.vertexbased;

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
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

import sim.app.episim.EpisimProperties;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.datamonitoring.GlobalStatistics;
import sim.app.episim.model.biomechanics.vertexbased.GlobalBiomechanicalStatistics.GBSValue;
import sim.app.episim.model.biomechanics.vertexbased.TestVisualizationPanel.TestVisualizationPanelPaintListener;
import sim.app.episim.model.biomechanics.vertexbased.simanneal.VertexForcesMinimizerSimAnneal;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.controller.ModelParameterModifier;
import sim.app.episim.nogui.NoGUIDisplay2D;
import sim.app.episim.tissue.TissueBorder;
import sim.app.episim.tissue.TissueController;
import sim.app.episim.util.EpisimMovieMaker;
import sim.engine.Schedule;

import ec.util.MersenneTwisterFast;
import episiminterfaces.CellPolygonProliferationSuccessListener;


public class TestVisualizationBiomechanics implements CellPolygonProliferationSuccessListener, TestVisualizationPanelPaintListener{
	
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
   private CellPolygonCalculator cellPolygonCalculator;
   public TestVisualizationBiomechanics(boolean autoStart){
   	this(autoStart, null, null, Integer.MAX_VALUE, false);
   }
   
   
   public TestVisualizationBiomechanics(boolean autoStart, int numberOfCellDivisions){
   	this(autoStart, null, null, numberOfCellDivisions, false);
   }
   
   
	public TestVisualizationBiomechanics(boolean autoStart, String moviePath, String csvPath, int numberOfCellDivisions, boolean headlessMode){
		
		
		this.maxNumberOfCellDivisions = numberOfCellDivisions;
		this.autostart = autoStart;
		this.headlessMode = headlessMode;
		
		if(moviePath != null) EpisimProperties.setProperty(EpisimProperties.MOVIE_PATH_PROP, moviePath);
		if(csvPath != null) createCsvWriter(csvPath);	
	
		
		//cells = CellPolygonNetworkBuilder.getSquareVertex(100, 100, 50, 6);
		cellPolygonCalculator = new CellPolygonCalculator(new CellPolygon[]{});
		cells = CellPolygonNetworkBuilder.getStandardCellArray(1, 1, cellPolygonCalculator);
		
		configureStandardMembrane();
		
		for(CellPolygon pol: cells){ 
			pol.addProliferationSuccessListener(this);
		}
		
		visualizationPanel = new TestVisualizationPanel();
		visualizationPanel.addTestVisualizationPanelPaintListener(this);
		visualizationPanel.setDoubleBuffered(true);
		visualizationPanel.setBackground(Color.WHITE);
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
			frame.setSize(600, 600);
			frame.setPreferredSize(new Dimension(600, 600));
			frame.getContentPane().setLayout(new BorderLayout());
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.getContentPane().add(visualizationPanel, BorderLayout.CENTER);			
			visualizationPanel.setBorder(BorderFactory.createLoweredBevelBorder());
			((JPanel)frame.getContentPane()).setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
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
			
			centerMe(frame);
			frame.pack();
			frame.setVisible(true);	
		}
		
	}
	
	private void configureStandardMembrane(){
		ModelController.getInstance().getBioMechanicalModelController().getEpisimMechanicalModelGlobalParameters().setBasalAmplitude_µm(250);
		ModelController.getInstance().getBioMechanicalModelController().getEpisimMechanicalModelGlobalParameters().setWidth(500);
		ModelController.getInstance().getBioMechanicalModelController().getEpisimMechanicalModelGlobalParameters().setBasalOpening_µm(12000);
		TissueController.getInstance().getTissueBorder().setBasalPeriod(550);
		TissueController.getInstance().getTissueBorder().setStartXOfStandardMembrane(30);
		TissueController.getInstance().getTissueBorder().setUndulationBaseLine(200);
		TissueController.getInstance().getTissueBorder().loadStandardMebrane();
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
		
				cells[cells.length/2].proliferate();
				while(simulationState == SimState.SIMSTART){
					
				//	try{
						int randomStartIndexCells =  rand.nextInt(cells.length);
						CellPolygon polygon = null;
						for(int n = 0; n < cells.length; n++){
							polygon = cells[((n+randomStartIndexCells)% cells.length)];
							//	System.out.println("Cell No. "+ polygon.getId() + " Size before: " +polygon.getCurrentArea());
							polygon.step(null);							
						}
						GlobalBiomechanicalStatistics.getInstance().step(null); 
						resetCalculationStatusOfAllCells();	
						
						
						
						if(!headlessMode) visualizationPanel.repaint();
						else paintToMovie();
		      /*      Thread.sleep(1);
	            }
	            catch (InterruptedException e){
		            ExceptionDisplayer.getInstance().displayException(e);
	            }*/	
					
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
		}
	        
        
			
		
	}
	
	
	
	
	private void resetCalculationStatusOfAllCells(){
		for(CellPolygon actPolygon: cells){
			actPolygon.resetCalculationStatusOfAllVertices();			
			//if(actPolygon.isSelected())System.out.println("Cell No. "+ actPolygon.getId() + " Size after: " +actPolygon.getCurrentArea() + "(selected) Difference: " + (actPolygon.getCurrentArea() - actPolygon.getPreferredArea()));
		}
	}
	
	
	
	
	private void drawVisualization(Graphics2D g){
		g.setColor(Color.BLACK);
		if(cells!= null) for(CellPolygon cellPol : cells) drawCellPolygon(g, cellPol, true);
		
		
		g.draw(TissueController.getInstance().getTissueBorder().getFullContourDrawPolygon());
		
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
			
			for(Vertex v : cell.getSortedVertices()){	
				p.addPoint(v.getIntX(), v.getIntY());
				
			}			
			
			if(cell.isProliferating()){
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

	
	
	public void proliferationCompleted(CellPolygon pol) {

		if(pol != null){
			CellPolygon[] newCellArray = new CellPolygon[cells.length+1];
			System.arraycopy(cells, 0, newCellArray, 0, cells.length);
			newCellArray[cells.length] = pol;
			cells= newCellArray;
			cellPolygonCalculator.setCellPolygons(cells);
			pol.addProliferationSuccessListener(this);
			cellPolygonCalculator.randomlySelectCellForProliferation();
			
			
			if(csvWriter != null){
				
				
	    	  	try{
	            csvWriter.write(GlobalBiomechanicalStatistics.getInstance().getCSVFileData(cells));
	            csvWriter.flush();
            }
            catch (IOException e){
	          ExceptionDisplayer.getInstance().displayException(e);
            }
			}
			
			
			this.numberOfCellDivisions++;
			if(numberOfCellDivisions >= this.maxNumberOfCellDivisions){
				startStopButton.setText("start");
				setSimulationState(SimState.SIMSTOP);
			}			
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
		
		if(args != null && args.length >= 0){
			EpisimProperties.setProperty(EpisimProperties.SIMULATOR_CONSOLE_INPUT_PROP, EpisimProperties.ON_CONSOLE_INPUT_VAL);
			ModelParameterModifier modifier = new ModelParameterModifier();
			for(int i = 0; i < args.length; i++){
				if(args[i] != null && (i+1)<args.length){					
					if(args[i].equals("-mp")) moviePath = args[i+1];
					else if(args[i].equals("-fps")) EpisimProperties.setProperty(EpisimProperties.FRAMES_PER_SECOND_PROP, args[i+1]);
					else if(args[i].equals("-id")) EpisimProperties.setProperty(EpisimProperties.SIMULATOR_SIMULATION_RUN_ID, args[i+1]);
					else if(args[i].equals("-mnp")) maxNumberOfProliferation = Integer.parseInt(args[i+1]);
					else if(args[i].equals("-p")) modifier.setGlobalModelPropertiesToValuesInPropertiesFile(VertexBasedMechanicalModelGlobalParameters.getInstance(), new File(args[i+1]));
					else if(args[i].equals("-csv")) csvPath = args[i+1];
				
				}
				if(args[i] != null && args[i].equals("-headless")) headless=true; 
			}
						
		}		
		new TestVisualizationBiomechanics(true, moviePath, csvPath, maxNumberOfProliferation, headless);
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
