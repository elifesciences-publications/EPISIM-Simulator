package sim.app.episim.datamonitoring.charts;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.NumberFormatter;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.LineBorder;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.event.TitleChangeEvent;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

import episiminterfaces.EpisimChart;
import episiminterfaces.EpisimChartSeries;

import sim.app.episim.CellType;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.datamonitoring.ExpressionEditor;
import sim.app.episim.datamonitoring.ExpressionCheckerController;
import sim.app.episim.datamonitoring.parser.ParseException;
import sim.app.episim.datamonitoring.parser.TokenMgrError;
import sim.app.episim.gui.ExtendedFileChooser;
import sim.app.episim.util.Names;
import sim.app.episim.util.ObjectManipulations;
import sim.app.episim.util.TissueCellDataFieldsInspector;
import sim.util.gui.ColorWell;
import sim.util.gui.LabelledList;
import sim.util.gui.NumberTextField;
import sim.util.media.ChartGenerator;
import sim.util.media.chart.TimeSeriesChartGenerator;



public class ChartCreationWizard extends JDialog {
	
	
   private TissueCellDataFieldsInspector cellDataFieldsInspector;
   
   private EpisimChart episimChart;
   private boolean okButtonPressed = false;
   protected ArrayList<Object[]> attributesList = new ArrayList<Object[]>();
   
   private JFreeChart previewChart;
   private ChartPanel previewChartPanel;
   private XYSeriesCollection dataset = new XYSeriesCollection();
   
   private  DatasetChangeEvent updateEvent;
   
 
   private Map<String, CellType> cellTypesMap;
   private Map<Integer, Long> seriesIdMap;
   private JTextField chartTitleField;
   private JTextField chartXLabel;
   private JTextField chartYLabel;
   private JTextField baselineField;
   private JTextField pngPathField;
   private JButton changePngPathButton;
   private JCheckBox legendCheck;
   private JCheckBox pngCheck;
   
   private NumberTextField pngFrequencyInSimulationSteps;
   private NumberTextField chartFrequencyInSimulationSteps;
   private JLabel pngFrequencyLabel;
   private JLabel chartFrequencyLabel;
   
   private JPanel seriesPanel;
   private JPanel propertiesPanel;
	private JSplitPane mainSplit;
   private JComboBox seriesCombo;
   private DefaultComboBoxModel comboModel;
   private JCheckBox aliasCheck;
   private JButton baselineButton;
   private CardLayout seriesCards;
   
   private final String DEFAULTSERIENAME = "Chart Series ";
 
   private final int WIDTH = 1200;
   private final int HEIGHT = 650;
   
   
   private String[] baselineExpression;

   /** Generates a new ChartGenerator with a blank chart. */
   public ChartCreationWizard(Frame owner, String title, boolean modal, TissueCellDataFieldsInspector cellDataFieldsInspector){
		super(owner, title, modal);
		
		this.cellDataFieldsInspector= cellDataFieldsInspector;
		if(cellDataFieldsInspector == null) throw new IllegalArgumentException("TissueCellDataFieldsInspector was null !");
		
		this.episimChart = new EpisimChartImpl(ChartController.getInstance().getNextChartId());
		
		
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		previewChartPanel = buildXYLineChart();
		
		propertiesPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		  		   
		c.anchor =GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.weighty =0;
		c.insets = new Insets(10,10,10,10);
		c.gridwidth = GridBagConstraints.REMAINDER;
		propertiesPanel.add(buildChartOptionPanel(), c);
         
		JPanel seriesMainPanel = new JPanel(new GridBagLayout());
		seriesMainPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Chart Series"),
				                                                       BorderFactory.createEmptyBorder(5, 5, 5, 5)));
				c.anchor =GridBagConstraints.WEST;
				c.fill = GridBagConstraints.HORIZONTAL;
				c.weightx = 0.3;
				c.weighty =0;
				c.insets = new Insets(5,5,5,5);
				c.gridwidth = GridBagConstraints.RELATIVE;
				
				comboModel = new DefaultComboBoxModel();
				seriesCombo = new JComboBox(comboModel);
				seriesCombo.addItemListener(new ItemListener(){
					public void itemStateChanged(ItemEvent evt) {
					    CardLayout cl = (CardLayout)(seriesPanel.getLayout());
					    cl.show(seriesPanel, ""+ seriesCombo.getSelectedIndex());
					}
				});
				
				seriesMainPanel.add(seriesCombo, c);
				
			
				c.gridwidth = GridBagConstraints.REMAINDER;
				c.fill = GridBagConstraints.NONE;
				c.weightx = 0.0;
				JButton addSeriesButton = new JButton("Add Chart Series");
				addSeriesButton.addActionListener(new ActionListener(){

					public void actionPerformed(ActionEvent e) {
						int index =addSeries();
	               	comboModel.addElement(DEFAULTSERIENAME + (index+1));
	               	seriesCombo.setSelectedIndex(index);
               }
					
				});
				seriesMainPanel.add(addSeriesButton, c);
				
				c.anchor =GridBagConstraints.CENTER;
				c.fill = GridBagConstraints.BOTH;
				c.weightx = 1;
				c.weighty =1;
				c.insets = new Insets(5,5,5,5);
				c.gridwidth = GridBagConstraints.REMAINDER;
									
				seriesCards = new CardLayout();
				seriesPanel = new JPanel(new CardLayout());
				seriesMainPanel.add(seriesPanel, c);
		
		
		
		c.anchor =GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.weighty =0;
		c.insets = new Insets(10,10,10,10);
		c.gridwidth = GridBagConstraints.REMAINDER;
		
			
		propertiesPanel.add(seriesMainPanel, c);
		
		previewChartPanel.setPreferredSize(new Dimension(getPreferredSize().width,	(int)(getPreferredSize().height*0.7)));
		previewChartPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Preview"), 
                                                                      BorderFactory.createEmptyBorder(5,5,5,5)));
		
		JPanel layoutCorrectingPanel = new JPanel(new BorderLayout());
		layoutCorrectingPanel.add(propertiesPanel,BorderLayout.NORTH);
		mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true,
		new JScrollPane(layoutCorrectingPanel), previewChartPanel);
      mainSplit.setOneTouchExpandable(false);
      mainSplit.setDividerLocation(((int)WIDTH /2));
	  

      
      getContentPane().add(mainSplit, BorderLayout.CENTER);
      getContentPane().add(buildOKCancelButtonPanel(), BorderLayout.SOUTH);
      setSize(WIDTH, HEIGHT);
 		validate();
   }
   
   
  
   /** Informs the chart of changes to the contents of its series. */
   public void update()
       {
       if (updateEvent == null)
           updateEvent = new DatasetChangeEvent(previewChart.getPlot(), null);
       previewChart.getPlot().datasetChanged(updateEvent);
       }

   /** Adds a series, plus a (possibly null) SeriesChangeListener which will receive a <i>single</i>
       event if/when the series is deleted from the chart by the user.
       Returns the series index number. */
   private int addSeries()
       {
   	 int i = dataset.getSeriesCount();
   	 EpisimChartSeries episimChartSeries = new EpisimChartSeriesImpl(System.currentTimeMillis());
   	 
   	 episimChartSeries.setName(DEFAULTSERIENAME+ (i+1));
   	 XYSeries series = new XYSeries(DEFAULTSERIENAME+ (i+1), false );
   	 addRandomValues(series);
       dataset.addSeries(series);
       episimChartSeries.setColor(Color.BLACK);
       
       previewChart.getXYPlot().getRenderer().setSeriesPaint(i, Color.BLACK);
       ChartSeriesAttributes csa = new ChartSeriesAttributes(previewChartPanel,i);
       episimChartSeries.setDash(csa.dash);
       episimChartSeries.setExpression(null);
       episimChartSeries.setStretch(csa.stretch);
       episimChartSeries.setThickness(csa.thickness);
       seriesPanel.add(csa, ""+i);
       attributesList.add(new Object[] {csa,series});
       
       validate();
       this.episimChart.addEpisimChartSeries(episimChartSeries);
       rebuildSeriesIdMap();
       return i;
   }
   
   private void rebuildSeriesIdMap(){
   	seriesIdMap = new HashMap<Integer, Long>();
   	int i = 0;
   	for(EpisimChartSeries actSeries :this.episimChart.getEpisimChartSeries()){
   		seriesIdMap.put(i, actSeries.getId());
   		i++;
   	}
   }
   
   private void addSeries(int index, EpisimChartSeries chartSeries){
   	
  	 
  	 XYSeries series = new XYSeries(chartSeries.getName(), false );
  	 addRandomValues(series);
      dataset.addSeries(series);
      
      
      previewChart.getXYPlot().getRenderer().setSeriesPaint(index, chartSeries.getColor());
      ChartSeriesAttributes csa = new ChartSeriesAttributes(previewChartPanel, index);
      csa.setDash(chartSeries.getDash());
      csa.setExpression(chartSeries.getExpression());
      csa.getFormulaButton().setText("Edit Expression");
      csa.setStretch((float)chartSeries.getStretch());
      csa.setThickness((float)chartSeries.getThickness());
      seriesPanel.add(csa, ""+index);
      attributesList.add(new Object[] {csa,series});
      comboModel.addElement(chartSeries.getName());
      
      validate();
      rebuildSeriesIdMap();
   }
   
   /** Returns the series at the given index. */
   public XYSeries getSeries(int index)
       {
       return dataset.getSeries(index);
       }
       
   /** Returns the number of series. */
   public int getSeriesCount()
       {
       return dataset.getSeriesCount();
       }
       
   /* Removes the series at the given index and returns it. */
   public XYSeries removeSeries(int index)
       {
   	
   	 this.episimChart.removeChartSeries(seriesIdMap.get(index));
       XYSeries series = dataset.getSeries(index);
               
       dataset.removeSeries(index);
       rebuildSeriesIdMap();
       Iterator iter = attributesList.iterator();
       while(iter.hasNext())
           {
           Object[] obj = (Object[])(iter.next());
           ChartSeriesAttributes csa = (ChartSeriesAttributes)(obj[0]);
           series = (XYSeries)(obj[1]);
           if (csa.seriesIndex == index)
               {
         	   
         	    seriesPanel.remove(index);
         	    comboModel.removeElementAt(index);
         	    
         	    Component[] comps = seriesPanel.getComponents();
         	    seriesPanel.removeAll();
         	    for(int i= 0; i < comps.length; i++) seriesPanel.add(comps[i], "" + i);
         	    
         	    seriesCards.show(seriesPanel, "" + (index-1));
         	    seriesPanel.validate();
         	    seriesPanel.repaint();
               iter.remove();
               }
           else if (csa.seriesIndex > index)  // must reduce
               {
               csa.seriesIndex--;
               csa.rebuildGraphicsDefinitions();
               }
           }
       validate();
       return series;
       }
               
   public void removeAllSeries()
       {
       for(int x = dataset.getSeriesCount()-1 ; x>=0 ; x--)
           removeSeries(x);
       }
       
   public void quit()
       {
       removeAllSeries();
       }
   
   private void addRandomValues(XYSeries series){
   	Random rand = new Random();
   	for(int i = 0; i< 100; i += 10){
   		series.add(i, rand.nextInt(100));
   	}
   }
   
   /* Constructs an XYLineChart.  Ultimately we might allow various charts; but we need to also set the
      chart's antialiasing, titles, etc. to reflect the current desired information. */
   private ChartPanel buildXYLineChart()
       {
 	
       previewChart = ChartFactory.createXYLineChart("Untitled Chart","Untitled X Axis","Untitled Y Axis",dataset,
                                              PlotOrientation.VERTICAL, false, true, false);
       ((XYLineAndShapeRenderer)(((XYPlot)(previewChart.getPlot())).getRenderer())).setDrawSeriesLineAsPath(true);

       previewChart.setAntiAlias(false);
       ChartPanel chartPanel = new ChartPanel(previewChart, true);
       chartPanel.setPreferredSize(new java.awt.Dimension(640,480));
       chartPanel.setMinimumDrawHeight(10);
       chartPanel.setMaximumDrawHeight(2000);
       chartPanel.setMinimumDrawWidth(20);
       chartPanel.setMaximumDrawWidth(2000);
       
       return chartPanel;
    }
  
               
   public void setTitle(String title)
       {
   	 episimChart.setTitle(title);	
       previewChart.setTitle(title);
       previewChart.titleChanged(new TitleChangeEvent(new org.jfree.chart.title.TextTitle(title)));
       super.setTitle("Chart Creation Wizard: "+ title);
       chartTitleField.setText(title);
       }

   public String getTitle()
       {
       return previewChart.getTitle().getText();
       }
               
   public void setRangeAxisLabel(String val)
       {
   	 episimChart.setYLabel(val);
       XYPlot xyplot = (XYPlot)(previewChart.getPlot());
       xyplot.getRangeAxis().setLabel(val);
       xyplot.axisChanged(new AxisChangeEvent(xyplot.getRangeAxis()));
       chartYLabel.setText(val);
       }
               
   public String getRangeAxisLabel()
       {
       return ((XYPlot)(previewChart.getPlot())).getRangeAxis().getLabel();
       }
               
   public void setDomainAxisLabel(String val)
       {
   	episimChart.setXLabel(val);
       XYPlot xyplot = (XYPlot)(previewChart.getPlot());
       xyplot.getDomainAxis().setLabel(chartXLabel.getText());
       xyplot.axisChanged(new AxisChangeEvent(xyplot.getDomainAxis()));
       chartXLabel.setText(val);
       }
               
   public String getDomainAxisLabel()
   {
       return ((XYPlot)(previewChart.getPlot())).getDomainAxis().getLabel();
   
   }
   void printChartToPDF( JFreeChart chart, int width, int height, String fileName )
       {
       try
           {
           Document document = new Document(new com.lowagie.text.Rectangle(width,height));
           PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fileName));
           document.addAuthor("Epidermis Simulator");
           document.open();
           PdfContentByte cb = writer.getDirectContent();
           PdfTemplate tp = cb.createTemplate(width, height); 
           Graphics2D g2 = tp.createGraphics(width, height, new DefaultFontMapper());
           Rectangle2D rectangle2D = new Rectangle2D.Double(0, 0, width, height); 
           chart.draw(g2, rectangle2D);
           g2.dispose();
           cb.addTemplate(tp, 0, 0);
           document.close();
           }
       catch( Exception e )
           {
           e.printStackTrace();
           }
       }
	
	public void showWizard(){
		
			
			showWizard(null);
	}
	
	
	private void restoreChartValues(EpisimChart chart){
		if(chart != null){
			chart.getEpisimChartSeries().size();
			this.episimChart = ObjectManipulations.cloneObject(chart);
			
			this.chartTitleField.setText(chart.getTitle());
			this.setTitle(chart.getTitle());
			
			this.chartXLabel.setText(chart.getXLabel());
			this.setDomainAxisLabel(chart.getXLabel());
			
			this.chartYLabel.setText(chart.getYLabel());
			this.setRangeAxisLabel(chart.getYLabel());
			
			this.baselineExpression = chart.getBaselineExpression();
			if(chart.getBaselineExpression() != null && chart.getBaselineExpression()[0] != null){
				this.baselineField.setText(chart.getBaselineExpression()[0]);
				this.baselineButton.setText("Edit Baseline Expression");
			}
			this.legendCheck.setSelected(chart.isLegendVisible());
			
			
			this.aliasCheck.setSelected(chart.isAntialiasingEnabled());
			
			
			this.pngCheck.setSelected(chart.isPNGPrintingEnabled());
			if(chart.isPNGPrintingEnabled()){
				pngFrequencyInSimulationSteps.setEnabled(true);
				this.changePngPathButton.setEnabled(true);
				this.pngPathField.setText(chart.getPNGPrintingPath().getAbsolutePath());
				pngPathField.setEnabled(true);
			}
			else{
				pngFrequencyInSimulationSteps.setEnabled(false);
				this.changePngPathButton.setEnabled(false);
				pngPathField.setEnabled(false);
			}
			
			this.pngFrequencyInSimulationSteps.setValue(chart.getPNGPrintingFrequency());
			this.chartFrequencyInSimulationSteps.setValue(chart.getChartUpdatingFrequency());
			int i = 0;
			for(EpisimChartSeries chartSeries: chart.getEpisimChartSeries()){ 
				addSeries(i, chartSeries);
				i++;
			}
			
		}
		
	}
	
	
		
	public void showWizard(EpisimChart chart){
		if(chart != null) restoreChartValues(chart);
		repaint();
		centerMe();
		setVisible(true);
	}
	
	public EpisimChart getEpisimChart(){
		if(this.okButtonPressed){
			this.episimChart.getRequiredClasses().clear();
			for(Class<?> actClass: this.cellDataFieldsInspector.getRequiredClasses()){
				this.episimChart.addRequiredClass(actClass);
			}
			return this.episimChart;
		}
		return null;
	}
	private JPanel buildOKCancelButtonPanel() {

		JPanel bPanel = new JPanel(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();

		c.anchor = GridBagConstraints.EAST;
		c.fill = GridBagConstraints.NONE;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 1;
		c.insets = new Insets(10, 10, 10, 10);
		c.gridwidth = 1;

		JButton okButton = new JButton("  OK  ");
		okButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				okButtonPressed();
			}
		});
		bPanel.add(okButton, c);

		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.NONE;
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 1;
		c.insets = new Insets(10, 10, 10, 10);
		c.gridwidth = 1;
		c.gridwidth = 1;

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				ChartCreationWizard.this.okButtonPressed = false;
				ChartCreationWizard.this.setVisible(false);
				ChartCreationWizard.this.dispose();
			}
		});
		bPanel.add(cancelButton, c);

		return bPanel;

	}
	
	private void okButtonPressed(){
		boolean errorFound = false;
		if(episimChart.getBaselineExpression() == null || episimChart.getBaselineExpression()[0] == null || episimChart.getBaselineExpression()[1] == null
				|| episimChart.getBaselineExpression()[0].trim().equals("") || episimChart.getBaselineExpression()[1].trim().equals("")){
			errorFound = true;
			JOptionPane.showMessageDialog(ChartCreationWizard.this, "Please enter valid Baseline-Expression!", "Error", JOptionPane.ERROR_MESSAGE);
		}
		else{
			try{
				if(!episimChart.getBaselineExpression()[0].trim().equals(Names.GRADBASELINE))
					ExpressionCheckerController.getInstance().checkDataMonitoringExpression(episimChart.getBaselineExpression()[0], this.cellDataFieldsInspector);
			}
			catch (Exception e1){
				
			   ExceptionDisplayer.getInstance().displayException(e1);
			}
			
		}
		if(episimChart.getTitle() == null || episimChart.getTitle().trim().equals("")){
			errorFound = true;
			JOptionPane.showMessageDialog(ChartCreationWizard.this, "Please enter valid Title!", "Error", JOptionPane.ERROR_MESSAGE);
		
		}
		if(episimChart.getEpisimChartSeries().size() == 0){
			errorFound = true;
			JOptionPane.showMessageDialog(ChartCreationWizard.this, "Please add at least one Chart-Series!", "Error", JOptionPane.ERROR_MESSAGE);
		}
		if(!errorFound){ 
			errorFound = !hasEverySeriesAnExpression();
			if(errorFound)
				JOptionPane.showMessageDialog(ChartCreationWizard.this, "Not every Chart-Series has an Expression!", "Error", JOptionPane.ERROR_MESSAGE);
		}
		if(!errorFound){ 
			ChartCreationWizard.this.okButtonPressed = true;
			ChartCreationWizard.this.setVisible(false);
			ChartCreationWizard.this.dispose();
		}
	}
	
	private boolean hasEverySeriesAnExpression(){
		for(EpisimChartSeries chartSeries:this.episimChart.getEpisimChartSeries()){
			if(chartSeries.getExpression() == null
			 || chartSeries.getExpression().length < 2
			 || chartSeries.getExpression()[0] == null
			 || chartSeries.getExpression()[1] == null
			 || chartSeries.getExpression()[0].trim().equals("")
			 || chartSeries.getExpression()[1].trim().equals("")) return false;
			else{
				try{
					ExpressionCheckerController.getInstance().checkDataMonitoringExpression(chartSeries.getExpression()[0], this.cellDataFieldsInspector);
				}
				catch (Exception e1){
					ExceptionDisplayer.getInstance().displayException(e1);
				}
			}
		}
		
		return true;
	}
	
	private void centerMe(){
		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(((int)(screenDim.getWidth() /2) - (this.getWidth()/2)), 
		((int)(screenDim.getHeight() /2) - (this.getHeight()/2)));
	}
	
	private JPanel buildChartOptionPanel() {

		JPanel optionsPanel = new JPanel(new BorderLayout());
		Box globalAttributes = Box.createVerticalBox();
		chartTitleField = new JTextField();
		chartTitleField.setText(previewChart.getTitle().getText());
		episimChart.setTitle(chartTitleField.getText());
		chartTitleField.addKeyListener(new KeyAdapter() {

			public void keyPressed(KeyEvent keyEvent) {

				if(keyEvent.getKeyCode() == KeyEvent.VK_ENTER){
					setTitle(chartTitleField.getText());
				}
				else if(keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE)
					chartTitleField.setText(getTitle());
			}
		});
		chartTitleField.addFocusListener(new FocusAdapter() {

			public void focusLost(FocusEvent e) {

				setTitle(chartTitleField.getText());
			}
		});

		LabelledList list = new LabelledList("Chart");
		
		list.add(new JLabel("Title"), chartTitleField);

		chartXLabel = new JTextField();
		chartXLabel.setText(((XYPlot) (previewChart.getPlot())).getDomainAxis().getLabel());
		episimChart.setXLabel(chartXLabel.getText());
		chartXLabel.addKeyListener(new KeyAdapter() {

			public void keyPressed(KeyEvent keyEvent) {

				if(keyEvent.getKeyCode() == KeyEvent.VK_ENTER){
					setDomainAxisLabel(chartXLabel.getText());
				}
				else if(keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE)
					chartXLabel.setText(getDomainAxisLabel());
			}
		});
		chartXLabel.addFocusListener(new FocusAdapter() {

			public void focusLost(FocusEvent e) {

				setDomainAxisLabel(chartXLabel.getText());
			}
		});

		list.add(new JLabel("X Label"), chartXLabel);

		chartYLabel = new JTextField();
		chartYLabel.setText(((XYPlot) (previewChart.getPlot())).getRangeAxis().getLabel());
		episimChart.setYLabel(chartYLabel.getText());
		chartYLabel.addKeyListener(new KeyAdapter() {

			public void keyPressed(KeyEvent keyEvent) {

				if(keyEvent.getKeyCode() == KeyEvent.VK_ENTER){
					setRangeAxisLabel(chartYLabel.getText());
				}
				else if(keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE)
					chartYLabel.setText(getRangeAxisLabel());
			}
		});
		chartYLabel.addFocusListener(new FocusAdapter() {

			public void focusLost(FocusEvent e) {
				
				setRangeAxisLabel(chartYLabel.getText());
			}
		});
		list.add(new JLabel("Y Label"), chartYLabel);

		baselineExpression = new String[2];
		baselineButton = new JButton("Add Baseline Expression");
      baselineField = new JTextField("");
      baselineButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {

	         ExpressionEditor editor = new ExpressionEditor(
	         		((Frame)ChartCreationWizard.this.getOwner()), "Baseline Expression Editor", true, cellDataFieldsInspector, Names.CHARTBASELINEEXPRESSIONEDITORROLE);
	         baselineExpression =editor.getExpression(baselineExpression);
	         if(baselineExpression != null && baselineExpression[0] != null && baselineExpression[1] != null){
	         	baselineButton.setText("Edit Baseline Expression");
	         	baselineField.setText(baselineExpression[0]);
	         	episimChart.setBaselineExpression(baselineExpression);
	         }
	         
        }
     	 
      });
      baselineField.setEditable(false);
      list.add(baselineButton, baselineField);
      
      chartFrequencyLabel = new JLabel("Chart Updating Frequency in Simulation Steps: ");
		
		chartFrequencyInSimulationSteps = new NumberTextField(100,false){
			public double newValue(double newValue)
	      {
				 newValue = Math.round(newValue);;
				episimChart.setChartUpdatingFrequency((int) newValue);
	        return newValue;
	      }
		};
		chartFrequencyInSimulationSteps.addKeyListener(new KeyAdapter() {

			public void keyPressed(KeyEvent keyEvent) {

				if(keyEvent.getKeyCode() == KeyEvent.VK_ENTER){
					episimChart.setChartUpdatingFrequency((int)chartFrequencyInSimulationSteps.getValue());
				}
				else if(keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE)
					chartFrequencyInSimulationSteps.setValue(chartFrequencyInSimulationSteps.getValue());
			}
		});
		chartFrequencyInSimulationSteps.addFocusListener(new FocusAdapter() {

			public void focusLost(FocusEvent e) {

				episimChart.setChartUpdatingFrequency((int)chartFrequencyInSimulationSteps.getValue());
			}
		});
		
		
		list.add(chartFrequencyLabel, chartFrequencyInSimulationSteps);
		
		
		legendCheck = new JCheckBox();
		legendCheck.setSelected(false);
		episimChart.setLegendVisible(false);
		legendCheck.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {

				setChartLegendVisible(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		list.add(new JLabel("Legend"), legendCheck);

		aliasCheck = new JCheckBox();
		aliasCheck.setSelected(previewChart.getAntiAlias());
		episimChart.setAntialiasingEnabled(previewChart.getAntiAlias());
		aliasCheck.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {

				setAntiAliasEnabled(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		list.add(new JLabel("Antialias"), aliasCheck);
	
      pngCheck = new JCheckBox();
      pngCheck.setSelected(false);
      episimChart.setPNGPrintingEnabled(false);
		pngCheck.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				if(ChartCreationWizard.this.isVisible()){
					if(e.getStateChange() == ItemEvent.SELECTED){
						
						selectPNGPath(false);
						
		          }
					else{
						episimChart.setPNGPrintingEnabled(false);
						pngFrequencyInSimulationSteps.setEnabled(false);
						pngFrequencyLabel.setEnabled(false);
						pngPathField.setEnabled(false);
						changePngPathButton.setEnabled(false);
					}
				}
			}
		});
		JPanel labelCheckPanel = new JPanel(new BorderLayout(5,5));
		labelCheckPanel.add(new JLabel("Save as PNG"), BorderLayout.WEST);
		labelCheckPanel.add(pngCheck, BorderLayout.EAST);
		JPanel fieldButtonPanel = new JPanel(new BorderLayout(5,5));
		this.pngPathField = new JTextField();
		this.pngPathField.setEditable(false);
		pngPathField.setEnabled(false);
		this.changePngPathButton = new JButton("Change");
		this.changePngPathButton.setEnabled(false);
		this.changePngPathButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				selectPNGPath(true);	         
         }});
		fieldButtonPanel.add(this.pngPathField, BorderLayout.CENTER);
		fieldButtonPanel.add(this.changePngPathButton, BorderLayout.EAST);
		list.add(labelCheckPanel, fieldButtonPanel);
		
		pngFrequencyInSimulationSteps = new NumberTextField(100,false){
			public double newValue(double newValue)
	      {
				 newValue = Math.round(newValue);;
				episimChart.setPNGPrintingFrequency((int) newValue);
	        return newValue;
	      }
		};
		pngFrequencyInSimulationSteps.addKeyListener(new KeyAdapter() {

			public void keyPressed(KeyEvent keyEvent) {

				if(keyEvent.getKeyCode() == KeyEvent.VK_ENTER){
					episimChart.setPNGPrintingFrequency((int)pngFrequencyInSimulationSteps.getValue());
				}
				else if(keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE)
					pngFrequencyInSimulationSteps.setValue(pngFrequencyInSimulationSteps.getValue());
			}
		});
		pngFrequencyInSimulationSteps.addFocusListener(new FocusAdapter() {

			public void focusLost(FocusEvent e) {

				episimChart.setPNGPrintingFrequency((int)pngFrequencyInSimulationSteps.getValue());
			}
		});
		pngFrequencyInSimulationSteps.setEnabled(false);
		pngFrequencyLabel = new JLabel("PDF Printing Frequency in Simulation Steps: ");
		pngFrequencyLabel.setEnabled(false);
		
		
		
		list.add(pngFrequencyLabel, pngFrequencyInSimulationSteps);
		
		optionsPanel.add(list, BorderLayout.CENTER);
		
		return optionsPanel;
	}
	
	private void setAntiAliasEnabled(boolean val){
		previewChart.setAntiAlias(val);
		episimChart.setAntialiasingEnabled(val);
	}
	
	private void selectPNGPath(boolean buttonCall){
		
		
		
		ExtendedFileChooser fileChooser = new ExtendedFileChooser(".png");
		fileChooser.setDialogTitle("Choose ONG Printing Path");
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
      fileChooser.setCurrentDirectory(episimChart.getPNGPrintingPath());
      fileChooser.setSelectedFile(episimChart.getPNGPrintingPath());
      
      File selectedPath = null;
      if (JFileChooser.APPROVE_OPTION == fileChooser.showSaveDialog(ChartCreationWizard.this) && 
      		(selectedPath = fileChooser.getSelectedFile()) != null)
      {
      	episimChart.setPNGPrintingEnabled(true);
      	episimChart.setPNGPrintingPath(selectedPath);
      	pngFrequencyInSimulationSteps.setEnabled(true);
      	pngFrequencyLabel.setEnabled(true);
      	this.pngPathField.setText(selectedPath.getAbsolutePath());
      	this.changePngPathButton.setEnabled(true);
      	pngPathField.setEnabled(true);
      	//  	Dimension dim = previewChartPanel.getPreferredSize();
      	//   printChartToPDF( previewChart, dim.width, dim.height, fd.getDirectory() + fileName );
      }
      else{
      	if(!buttonCall){
      	pngCheck.setSelected(false);
      	episimChart.setPNGPrintingEnabled(false);
      	pngFrequencyInSimulationSteps.setEnabled(false);
      	this.changePngPathButton.setEnabled(false);
      	pngPathField.setEnabled(false);
      	}
      }
	}
	
	private void setChartLegendVisible(boolean val){
		if(val){
			episimChart.setLegendVisible(true);
			LegendTitle title = new LegendTitle(
					(XYItemRenderer) (previewChart.getXYPlot().getRenderer()));
			title.setLegendItemGraphicPadding(new org.jfree.ui.RectangleInsets(0, 8, 0, 4));
			title.setLegendItemGraphicAnchor(RectangleAnchor.BOTTOM);
			
			
         title.setMargin(new RectangleInsets(1.0, 1.0, 1.0, 1.0));
			title.setFrame(new LineBorder());
			title.setBackgroundPaint(Color.white);
			title.setPosition(RectangleEdge.BOTTOM);
		
			
			previewChart.addLegend(title);
			
		}
		else{
			episimChart.setLegendVisible(false);
			previewChart.removeLegend();
		}
	}
     
   private class ChartSeriesAttributes extends LabelledList
   {
   static final float DASH = 6;
   static final float DOT = 1;
   static final float SPACE = 3;
   static final float SKIP = DASH;
   
   private String[] expression = new String[2];
   
   public final float[][] dashes = 
       { 
       { DASH, 0.0f }, // --------
           { DASH * 2, SKIP }, 
           { DASH, SKIP } , // -  -  -  
           { DASH, SPACE } , // - - - -
           { DASH, SPACE, DASH, SPACE, DOT, SPACE },  // - - . - - . 
           { DASH, SPACE, DOT, SPACE, }, // - . - .
           { DASH, SPACE, DOT, SPACE, DOT, SPACE },  // - . . - . . 
           { DOT, SPACE }, // . . . .
           { DOT, SKIP }   // .  .  .  .  
       };

   private float stretch = 1.0f;
   private float thickness = 2.0f;
   private float[] dash = dashes[0];
   private int seriesIndex;
   private ChartPanel panel;
   private String name;
   private Color strokeColor;
   
   
   //Color fillColor = CLEAR;
   
   private JTextField nameF;
   private ColorWell colorwell;
   private NumberTextField thickitude;
   private JComboBox dashCombo;
   private NumberTextField stretchField; 
   private JTextField formulaField;
   private JButton formulaButton;
   
   public void setIndex(int i) { seriesIndex = i; }
   
   public JButton getFormulaButton(){ return this.formulaButton; }

   public XYSeries getSeries()
   {
       return dataset.getSeries(seriesIndex);
   }
   
   public XYPlot getPlot()
   {
       return panel.getChart().getXYPlot();
   }
  
   
   public void setBorderTitle(String title){
   
      if (title != null) setBorder(new javax.swing.border.TitledBorder(title));
      
   }
   
   public void rebuildGraphicsDefinitions()
       {
       float[] newDash = new float[dash.length];
       for(int x=0;x<dash.length;x++)
           newDash[x] = dash[x] * stretch * thickness;  // include thickness so we dont' get overlaps -- will this confuse users?
           
       XYItemRenderer renderer = (XYItemRenderer)(getPlot().getRenderer());
       
       renderer.setSeriesStroke(seriesIndex,
                                new BasicStroke(thickness, BasicStroke.CAP_ROUND, 
                                                BasicStroke.JOIN_ROUND,0,newDash,0));

       renderer.setSeriesPaint(seriesIndex,strokeColor);
       //renderer.setSeriesOutlinePaint(seriesIndex,strokeColor);
       repaint();
       }
   
   public ChartSeriesAttributes(ChartPanel pan, int index)
   {
       super("" + dataset.getSeries(index).getKey());  //((XYSeriesCollection)(pan.getChart().getXYPlot().getDataset())).getSeries(index).getKey());
       
       panel = pan;
       seriesIndex = index;
       final JCheckBox check = new JCheckBox();
       check.setSelected(true);
       check.addActionListener(new ActionListener()
           {
           public void actionPerformed(ActionEvent e)
               {
         	  	
               getPlot().getRenderer().setSeriesVisible(seriesIndex,
                                                        new Boolean(check.isSelected()));  
               }
           });
       
       addLabelled("Show", check);

       name = "" + getSeries().getKey();
       nameF = new JTextField(name);
       nameF.addActionListener(new ActionListener()
           {
           public void actionPerformed(ActionEvent e)
               {
               name = nameF.getText();
               
               setBorderTitle(name);
               getSeries().setKey(name);
               int index = seriesCombo.getSelectedIndex();
              if(index > -1){
               episimChart.getEpisimChartSeries(seriesIdMap.get(index)).setName(name);
               comboModel.removeElementAt(index);
               comboModel.insertElementAt(name, index);
               seriesCombo.setSelectedIndex(index);
              }
               panel.repaint();
               rebuildGraphicsDefinitions();
               }
           });
       nameF.addFocusListener(new FocusAdapter(){
      	 public void focusLost(FocusEvent e){
      		 
      		 for(ActionListener actList: nameF.getActionListeners()){
      			 actList.actionPerformed(new ActionEvent(nameF, 0, ""));
      		 }
      	 }
       });
       addLabelled("Name",nameF);
       
       strokeColor = (Color)(getPlot().getRenderer().getSeriesPaint(index));
       
       colorwell = new ColorWell(strokeColor)
       {
       public Color changeColor(Color c) 
           {
           ChartSeriesAttributes.this.strokeColor = c;
           int index = seriesCombo.getSelectedIndex();
           if(index > -1){
           episimChart.getEpisimChartSeries(seriesIdMap.get(index)).setColor(c);
           }
           rebuildGraphicsDefinitions();
           return c;
           }
       };
      
       addLabelled("Line",colorwell);
      
       thickitude = new NumberTextField(2.0,true)
       {
           public double newValue(double newValue) 
           {
               if (newValue < 0.0) 
                   newValue = currentValue;
               thickness = (float)newValue;
               int index = seriesCombo.getSelectedIndex();
               if(index >= 0)episimChart.getEpisimChartSeries(seriesIdMap.get(index)).setThickness(thickness);
               rebuildGraphicsDefinitions();
               return newValue;
            }
       };
       addLabelled("Width",thickitude);
       dashCombo = new JComboBox();
       dashCombo.setEditable(false);
       dashCombo.setModel(new DefaultComboBoxModel(new Vector(Arrays.asList(
                                                             new String[] { "Solid", "__  __  __", "_  _  _  _", "_ _ _ _ _", "_ _ . _ _ .", "_ . _ . _ .", "_ . . _ . .", ". . . . . . .", ".  .  .  .  ." }))));
       dashCombo.setSelectedIndex(0);
       dashCombo.addActionListener(new ActionListener()
           {
           public void actionPerformed ( ActionEvent e )
           {
               dash = dashes[dashCombo.getSelectedIndex()];
               int index = seriesCombo.getSelectedIndex();
               if(index >= 0) episimChart.getEpisimChartSeries(seriesIdMap.get(index)).setDash(dash);
               rebuildGraphicsDefinitions();
           }
           });
       addLabelled("Dash",dashCombo);
       stretchField = new NumberTextField(1.0,true)
           {
           public double newValue(double newValue) 
               {
               if (newValue < 0.0) 
                   newValue = currentValue;
               stretch = (float)newValue;
               int index = seriesCombo.getSelectedIndex();
               if(index > -1){
               	episimChart.getEpisimChartSeries(seriesIdMap.get(index)).setStretch(stretch);
               }
               rebuildGraphicsDefinitions();
               return newValue;
               }
           };
       addLabelled("Stretch",stretchField);
       JButton removeButton = new JButton("Remove");
       removeButton.addActionListener(new ActionListener()
           {
           public void actionPerformed ( ActionEvent e )
               {
               if (JOptionPane.showOptionDialog(
                       null,"Remove the Series " + name + "?","Confirm",
                       JOptionPane.YES_NO_OPTION,
                       JOptionPane.QUESTION_MESSAGE,null,
                       new Object[] { "Remove", "Cancel" },
                       null) == 0)  // remove
              	 ChartCreationWizard.this.removeSeries(seriesIndex);
               }
           });
       
       
       formulaButton = new JButton("Add Expression");
       formulaField = new JTextField("");
       formulaButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {

	         ExpressionEditor editor = new ExpressionEditor(
	         		((Frame)ChartCreationWizard.this.getOwner()), "Series Expression Editor: " + ((String) seriesCombo.getSelectedItem()), true, cellDataFieldsInspector, Names.CHARTSERIESEXPRESSIONEDITORROLE);
	         expression =editor.getExpression(expression);
	         if(expression != null && expression[0] != null && expression[1] != null){
	         	formulaButton.setText("Edit Expression");
	         	formulaField.setText(expression[0]);
	         	int index = seriesCombo.getSelectedIndex();
               episimChart.getEpisimChartSeries(seriesIdMap.get(index)).setExpression(expression);
	         }
	         
         }
      	 
       });
       formulaField.setEditable(false);
       JPanel fieldButtonPanel = new JPanel(new BorderLayout(5,0));
		fieldButtonPanel.add(formulaField, BorderLayout.CENTER);
		fieldButtonPanel.add(formulaButton, BorderLayout.EAST);
		add(new JLabel("Expression:"), fieldButtonPanel);
       
       Box b = new Box(BoxLayout.X_AXIS);
       b.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
       b.add(removeButton);
       b.add(Box.createGlue());
       add(b);

       rebuildGraphicsDefinitions();
       }

	
   public String[] getExpression() {
   
   	return expression;
   }

	
   public void setExpression(String[] expression) {
   	if(expression != null &&expression.length >= 2 && expression[0] != null && expression[1] != null){
   		this.expression = expression;
   		this.formulaField.setText(expression[0]);
   	}
   	
   }

	
   public float getStretch() {
   
   	return stretch;
   }

	
   public void setStretch(float stretch) {
   
   	this.stretch = stretch;
   	this.stretchField.setValue(stretch);
   }

	
   public float getThickness() {
   
   	return thickness;
   }

	
   public void setThickness(float thickness) {
   
   	this.thickness = thickness;
   	this.thickitude.newValue(thickness);
   }

	
   public float[] getDash() {
   
   	return dash;
   }

	
   public void setDash(float[] dash) {
   
   	this.dash = dash;
   	for(int i = 0; i< dashes.length; i++){
   		if(Arrays.equals(dash, dashes[i])){
   			this.dashCombo.setSelectedIndex(i);
   			break;
   		}
   	}
   }

	
   public int getSeriesIndex() {
   
   	return seriesIndex;
   }

	
   public void setSeriesIndex(int seriesIndex) {
   
   	this.seriesIndex = seriesIndex;
   }

	
   public ChartPanel getPanel() {
   
   	return panel;
   }

	
   public void setPanel(ChartPanel panel) {
   
   	this.panel = panel;
   }

	
   public String getName() {
   
   	return name;
   }

	
   public void setName(String name) {
   
   	this.name = name;
   	this.nameF.setText(name);
   }

	
   public Color getStrokeColor() {
   
   	return strokeColor;
   }

	
   public void setStrokeColor(Color strokeColor) {
   
   	this.strokeColor = strokeColor;
   	this.colorwell.setColor(strokeColor);
   }
   
   
   
  
   }
   

	

	}