package sim.app.episim.datamonitoring.charts;

import java.awt.BorderLayout;
import java.awt.Font;
import java.util.List;
import java.util.Random;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jzy3d.chart.Chart;
import org.jzy3d.chart.controllers.mouse.camera.AWTCameraMouseController;
import org.jzy3d.chart.factories.AWTChartComponentFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.ColorMapRainbow;
import org.jzy3d.chart.Settings;
import org.jzy3d.maths.BoundingBox3d;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.maths.Range;
import org.jzy3d.maths.Scale;
import org.jzy3d.plot2d.primitive.ColorbarImageGenerator;
import org.jzy3d.plot3d.builder.Builder;
import org.jzy3d.plot3d.builder.Mapper;
import org.jzy3d.plot3d.builder.concrete.OrthonormalGrid;
import org.jzy3d.plot3d.primitives.AbstractDrawable;
import org.jzy3d.plot3d.primitives.Point;
import org.jzy3d.plot3d.primitives.Polygon;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.primitives.axes.layout.renderers.ScientificNotationTickRenderer;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import org.jzy3d.plot3d.rendering.legends.ILegend;
import org.jzy3d.plot3d.rendering.legends.colorbars.AWTColorbarLegend;
import org.jzy3d.plot3d.rendering.legends.colorbars.IColorbarLegend;
import org.jzy3d.plot3d.rendering.view.Renderer2d;
import org.jzy3d.plot3d.rendering.view.modes.ViewBoundMode;
import org.jzy3d.chart.ChartLauncher;

import com.jogamp.newt.event.MouseEvent;

import sim.app.episim.EpisimProperties;
import sim.app.episim.datamonitoring.charts.build.ChartSourceBuilder;
import sim.app.episim.datamonitoring.charts.io.PNGPrinter;
import sim.app.episim.model.controller.ExtraCellularDiffusionController;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.controller.TissueController;
import sim.app.episim.model.controller.ExtraCellularDiffusionController.DiffusionFieldCrossSectionMode;
import sim.app.episim.model.diffusion.ExtraCellularDiffusionField2D;
import sim.app.episim.model.diffusion.ExtraCellularDiffusionField3D;
import sim.app.episim.util.EnhancedSteppable;
import sim.engine.SimState;
import sim.field.grid.DoubleGrid2D;
import episiminterfaces.EpisimDiffusionFieldConfiguration;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters.ModelDimensionality;
import episiminterfaces.monitoring.EpisimDiffFieldChart;


public class DiffusionChartGUI {
	
	private EpisimDiffFieldChart diffChartConfig;
	private EpisimDiffusionFieldConfiguration ecDiffFieldConfig;
	
	private Chart chart;
	private Shape surface;
	private Mapper mapper;	
	
	public DiffusionChartGUI(EpisimDiffFieldChart diffChart){
		if(diffChart != null){
			this.diffChartConfig = diffChart;
			this.ecDiffFieldConfig = ModelController.getInstance().getExtraCellularDiffusionController().getEpisimExtraCellularDiffusionFieldsConfiguration(diffChart.getDiffusionFieldName());
			if(this.ecDiffFieldConfig != null){
				buildChart();
			}
		}
	}
	//TODO: implements distinction between 2D and 3D
	private void buildChart(){
		
		final Random rand = new Random();
		mapper = getDataMapper();
		
		double widthInMikron = TissueController.getInstance().getTissueBorder().getWidthInMikron();
		double heightInMikron = TissueController.getInstance().getTissueBorder().getHeightInMikron();
		int width = (int) (widthInMikron / this.ecDiffFieldConfig.getLatticeSiteSizeInMikron());
		int height =(int) (heightInMikron / this.ecDiffFieldConfig.getLatticeSiteSizeInMikron());
		
		int xyDimensions = width > height ? width : height;
		
		double rangeDouble = widthInMikron > heightInMikron ? widthInMikron : heightInMikron;
		
		Range range = new Range(0, rangeDouble);
		int steps   = xyDimensions > 100 ? 100 : xyDimensions;
		//if(steps > 75) steps = 75;
		// Create the object to represent the function over the given range.
		surface = (Shape)Builder.buildOrthonormal(new OrthonormalGrid(range, steps, range, steps), mapper);
	
		//surface.setColorMapper(new ColorMapper(new ColorMapRainbow(), surface.getBounds().getZmin(), surface.getBounds().getZmax(), new Color(1,1,1,.5f)));
		surface.setColorMapper(new ColorMapper(new ColorMapRainbow(), 
				                             0, (this.ecDiffFieldConfig.getMaximumConcentration() < Double.POSITIVE_INFINITY?(float)this.ecDiffFieldConfig.getMaximumConcentration():1000000f)
				                             , new Color(1,1,1,.5f)));
		surface.setFaceDisplayed(true);
		surface.setWireframeDisplayed(true);
		surface.setWireframeColor(new Color(40,40,40));
		
		// Create a chart 
		chart = AWTChartComponentFactory.chart(new Quality(true, false, true, false, false, false, true),"awt");

		chart.getView().setBoundManual(new BoundingBox3d(0, (float)rangeDouble, 0, (float)rangeDouble, 0, this.ecDiffFieldConfig.getMaximumConcentration() < Double.POSITIVE_INFINITY?(float)this.ecDiffFieldConfig.getMaximumConcentration():1000000f));
		
		ILegend legend = new AWTColorbarLegend(surface, chart.getView().getAxe().getLayout().getZTickProvider(), chart.getView().getAxe().getLayout().getZTickRenderer());		
		
		surface.setLegend(legend);		
		chart.getScene().getGraph().add(surface);

		chart.getAxeLayout().setXAxeLabel( "X (µm)" );
		chart.getAxeLayout().setYAxeLabel( "Y (µm)" );
		chart.getAxeLayout().setZAxeLabel( "Z" );
		

	//	chart.getAxeLayout().setZTickRenderer( new ScientificNotationTickRenderer(2) );
		
		Settings.getInstance().setHardwareAccelerated(true);
		AWTCameraMouseController mouse   = new AWTCameraMouseController();
		
		chart.addController(mouse);			
		
		chart.render();
	}
	
	private Mapper getDataMapper(){
		Mapper dataMapper= null;
		if(ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().getModelDimensionality() == ModelDimensionality.TWO_DIMENSIONAL){
			final double height = TissueController.getInstance().getTissueBorder().getHeightInMikron();
			dataMapper = new Mapper(){
				public double f(double x, double y) {
					ExtraCellularDiffusionField2D field2D = (ExtraCellularDiffusionField2D)ModelController.getInstance().getExtraCellularDiffusionController().getExtraCellularDiffusionField(ecDiffFieldConfig.getDiffusionFieldName());
					y =  height - y;
					if(y < 0) return 0;					
					return field2D != null ? field2D.getConcentration(x, y, false) : 0;
				}
			};
		}
		else if(ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().getModelDimensionality() == ModelDimensionality.THREE_DIMENSIONAL){
			
			final ExtraCellularDiffusionController controller = ModelController.getInstance().getExtraCellularDiffusionController();
			dataMapper = new Mapper(){
				public double f(double x, double y) {
					ExtraCellularDiffusionField3D field3D = (ExtraCellularDiffusionField3D)ModelController.getInstance().getExtraCellularDiffusionController().getExtraCellularDiffusionField(ecDiffFieldConfig.getDiffusionFieldName());
					DiffusionFieldCrossSectionMode actCrossSectionMode = controller.getSelectedDiffusionFieldCrossSectionMode();
					double actCrossSectionTranslationCoordinate = controller.getDiffusionFieldCrossSectionCoordinate();				
					if(actCrossSectionMode == DiffusionFieldCrossSectionMode.X_Y_PLANE){
						return field3D != null ? field3D.getConcentration(x, y, actCrossSectionTranslationCoordinate) : 0;						
					}
					if(actCrossSectionMode == DiffusionFieldCrossSectionMode.X_Z_PLANE){						
						return field3D != null ? field3D.getConcentration(x, actCrossSectionTranslationCoordinate, y) : 0;						
					}
					if(actCrossSectionMode == DiffusionFieldCrossSectionMode.Y_Z_PLANE){					
						return field3D != null ? field3D.getConcentration(actCrossSectionTranslationCoordinate,y, x) : 0;						
					}				
					return 0;
				}
			};
		}		
		return dataMapper;
	}
	
	
	
	public EnhancedSteppable getChartSteppable(){
		if(diffChartConfig != null && this.ecDiffFieldConfig != null){
			return new EnhancedSteppable(){
				public void step(SimState state) {
					
					remap(surface, mapper);
					SwingUtilities.invokeLater(new Runnable(){
						public void run() {

							chart.render();
	                  
                  }});
					
				} 
				public double getInterval() {
		
								return EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CHARTUPDATEFREQ)== null
										|| Integer.parseInt(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CHARTUPDATEFREQ)) <= 0 ? diffChartConfig.getChartUpdatingFrequency() :
											Integer.parseInt(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CHARTUPDATEFREQ));
					
				}
			};
		}
		return new EnhancedSteppable(){public void step(SimState state) {} public double getInterval() {return 10000;}};
	}
	
	public EnhancedSteppable getChartPNGSteppable(){
		
		if(diffChartConfig.isPNGPrintingEnabled()){
			return new EnhancedSteppable(){
				public void step(SimState state) {					
					if(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CHARTPNGPRINTPATH) != null){
						 		PNGPrinter.getInstance().printChartAsPng(diffChartConfig.getId(), null, 
						 				(diffChartConfig.getChartTitle() == null || diffChartConfig.getChartTitle().length()==0 ? "EpisimChartPNG":diffChartConfig.getChartTitle()), 
						 				chart, state);
					}
					else{					
						PNGPrinter.getInstance().printChartAsPng(diffChartConfig.getId(), diffChartConfig.getPNGPrintingPath(), diffChartConfig.getChartTitle(), chart, state);						
					}			
								
				} 
				public double getInterval() {
					if(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CHARTPNGPRINTPATH) != null){
								return EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CHARTPNGPRINTFREQ)== null
										|| Integer.parseInt(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CHARTPNGPRINTFREQ)) <= 0 ? diffChartConfig.getPNGPrintingFrequency() :
											Integer.parseInt(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CHARTPNGPRINTFREQ));
					}
					else{
						return diffChartConfig.getPNGPrintingFrequency();
					}					
			  }
			};
		}
		
		return null;
	}
	
	public JPanel getChartPanel(){
		if(diffChartConfig != null && this.ecDiffFieldConfig != null){
			JPanel panel = new JPanel(new BorderLayout());
			
			JPanel titlePanel = new JPanel();
			JLabel chartTitle = new JLabel(diffChartConfig.getChartTitle());
			chartTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
			titlePanel.add(chartTitle);
			titlePanel.setBackground(java.awt.Color.WHITE);
			panel.add(titlePanel, BorderLayout.NORTH);
			panel.add(((java.awt.Component)chart.getCanvas()), BorderLayout.CENTER);
			panel.setName(diffChartConfig.getChartTitle());
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
