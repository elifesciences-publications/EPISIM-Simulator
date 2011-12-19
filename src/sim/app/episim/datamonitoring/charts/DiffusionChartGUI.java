package sim.app.episim.datamonitoring.charts;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jzy3d.chart.Chart;
import org.jzy3d.chart.controllers.mouse.ChartMouseController;
import org.jzy3d.chart.controllers.thread.ChartThreadController;
import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.ColorMapRainbow;
import org.jzy3d.global.Settings;

import org.jzy3d.maths.BoundingBox3d;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.maths.Range;
import org.jzy3d.plot2d.primitive.ColorbarImageGenerator;
import org.jzy3d.plot3d.builder.Builder;
import org.jzy3d.plot3d.builder.Mapper;
import org.jzy3d.plot3d.builder.concrete.OrthonormalGrid;
import org.jzy3d.plot3d.primitives.AbstractDrawable;
import org.jzy3d.plot3d.primitives.Point;
import org.jzy3d.plot3d.primitives.Polygon;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import org.jzy3d.plot3d.rendering.legends.colorbars.ColorbarLegend;
import org.jzy3d.plot3d.rendering.view.Renderer2d;
import org.jzy3d.plot3d.rendering.view.modes.ViewBoundMode;
import org.jzy3d.ui.ChartLauncher;

import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.diffusion.ExtraCellularDiffusionField;
import sim.app.episim.tissue.TissueController;
import sim.app.episim.util.EnhancedSteppable;
import sim.engine.SimState;
import episiminterfaces.EpisimDiffusionFieldConfiguration;
import episiminterfaces.monitoring.EpisimDiffFieldChart;


public class DiffusionChartGUI {
	
	private EpisimDiffFieldChart diffChartConfig;
	private EpisimDiffusionFieldConfiguration ecDiffFieldConfig;
	
	protected Chart chart;
	protected Shape surface;
	protected Mapper mapper;
	
	
	public DiffusionChartGUI(EpisimDiffFieldChart diffChart){
		if(diffChart != null){
			this.diffChartConfig = diffChart;
			this.ecDiffFieldConfig = ModelController.getInstance().getExtraCellularDiffusionController().getEpisimExtraCellularDiffusionFieldsConfiguration(diffChart.getDiffusionFieldName());
			if(this.ecDiffFieldConfig != null){
				buildChart();
			}
		}
	}
	
	private void buildChart(){
		
		final Random rand = new Random();
		mapper = new Mapper(){
			public double f(double x, double y) {
				ExtraCellularDiffusionField field= ModelController.getInstance().getExtraCellularDiffusionController().getExtraCellularDiffusionField(ecDiffFieldConfig.getDiffusionFieldName());
				return field != null ? field.getConcentration(x, y) : 0;
			}
		};
		
		double widthInMikron = TissueController.getInstance().getTissueBorder().getWidthInMikron();
		double heightInMikron = TissueController.getInstance().getTissueBorder().getHeightInMikron();
		int width = (int) (widthInMikron / this.ecDiffFieldConfig.getLatticeSiteSizeInMikron());
		int height =(int) (heightInMikron / this.ecDiffFieldConfig.getLatticeSiteSizeInMikron());
		
		Range range = new Range(0, widthInMikron > heightInMikron ? widthInMikron : heightInMikron);
		int steps   = width > height ? width : height;
		if(steps > 75) steps = 75;
		// Create the object to represent the function over the given range.
		surface = (Shape)Builder.buildOrthonormal(new OrthonormalGrid(range, steps, range, steps), mapper);
	
		//surface.setColorMapper(new ColorMapper(new ColorMapRainbow(), surface.getBounds().getZmin(), surface.getBounds().getZmax(), new Color(1,1,1,.5f)));
		surface.setColorMapper(new ColorMapper(new ColorMapRainbow(), 0, 255, new Color(1,1,1,.5f)));
		surface.setFaceDisplayed(true);
		surface.setWireframeDisplayed(true);
		surface.setWireframeColor(Color.BLACK);
		
		// Create a chart 
		chart = new Chart(Quality.Intermediate,"swing");
		//chart.getView().getAxe().setScale(null).getBoxBounds().setZmax(this.ecDiffFieldConfig.getMaximumConcentration() < Double.POSITIVE_INFINITY?(float)this.ecDiffFieldConfig.getMaximumConcentration():1000000f);
		//chart.getView().getAxe().getBoxBounds().setZmin((float)this.ecDiffFieldConfig.getMinimumConcentration());
		ColorbarLegend legend = new ColorbarLegend(surface, chart.getView().getAxe().getLayout().getZTickProvider(), chart.getView().getAxe().getLayout().getZTickRenderer());		
		surface.setLegend(legend);
		chart.getScene().getGraph().add(surface);
		chart.getView().setBoundManual(new BoundingBox3d(0, 150, 0, 150, 0, 255));
		
		
	/*	chart.addRenderer(new Renderer2d(){
			public void paint(Graphics g) {
				Graphics2D g2d = (Graphics2D)g;
				g2d.setColor(java.awt.Color.BLACK);
				g2d.drawString(diffChartConfig.getChartTitle(), 50, 50);
			}
		});*/
		
		Settings.getInstance().setHardwareAccelerated(true);
		ChartMouseController mouse   = new ChartMouseController();
		
		chart.addController(mouse);
		
		
	//	ChartThreadController thread = new ChartThreadController();
	//	mouse.addSlaveThreadController(thread);
	//	chart.addController(thread);
	//	thread.start();
		
		// trigger screenshot on 's' letter
		chart.getCanvas().addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
				switch(e.getKeyChar()){
	    		case 's':
	    			/*try {
						ChartLauncher.screenshot(chart, "./data/screenshots/"+title+".png");
					} catch (IOException e1) {
						e1.printStackTrace();
					}*/
	    		default:
	    			break;
	    		}
			}
			@Override
			public void keyReleased(KeyEvent e) {
			}
			@Override
			public void keyPressed(KeyEvent e) {
			}
		});
		//chart.render();
	}
	
	public EnhancedSteppable getChartSteppable(){
		if(diffChartConfig != null && this.ecDiffFieldConfig != null){
			return new EnhancedSteppable(){
				public void step(SimState state) {
					
					remap(surface, mapper);
					SwingUtilities.invokeLater(new Runnable(){
						@Override
                  public void run() {

							chart.render();
	                  
                  }});
					
				} 
				public double getInterval() {return diffChartConfig.getChartUpdatingFrequency();}
			};
		}
		return new EnhancedSteppable(){public void step(SimState state) {} public double getInterval() {return 10000;}};
	}
	
	public JPanel getChartPanel(){
		if(diffChartConfig != null && this.ecDiffFieldConfig != null){
			JPanel panel = new JPanel(new BorderLayout());
			panel.add(((JComponent)chart.getCanvas()), BorderLayout.CENTER);
		
			return panel;
			
		}
		else return new JPanel();
	}
	
	private void remap(Shape shape, Mapper mapper){
		List<AbstractDrawable> polygons = shape.getDrawables();		
		for(AbstractDrawable d: polygons){
			if(d instanceof Polygon){
				Polygon p = (Polygon) d;				
				for(int i=0; i<p.size(); i++){
					Point pt = p.get(i);
					Coord3d c = pt.xyz;
					c.z = (float) mapper.f(c.x, c.y);
				}
			}
		}
	}

}
