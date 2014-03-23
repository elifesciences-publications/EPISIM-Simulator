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

import java.awt.geom.Rectangle2D;

import java.io.File;
import java.io.FileOutputStream;

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


import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
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

import episiminterfaces.calc.CalculationAlgorithm;
import episiminterfaces.calc.CalculationAlgorithmConfigurator;
import episiminterfaces.calc.CalculationAlgorithmDescriptor;
import episiminterfaces.monitoring.EpisimChart;
import episiminterfaces.monitoring.EpisimChartSeries;

import sim.app.episim.AbstractCell;

import sim.app.episim.datamonitoring.DataEvaluationWizard;


import sim.app.episim.datamonitoring.calc.CalculationAlgorithmServer;
import sim.app.episim.datamonitoring.calc.CalculationController;

import sim.app.episim.gui.ExtendedFileChooser;
import sim.app.episim.util.ExtendedColorWell;
import sim.app.episim.util.Names;
import sim.app.episim.util.ObjectManipulations;
import sim.app.episim.util.TissueCellDataFieldsInspector;
import sim.util.gui.ColorWell;
import sim.util.gui.LabelledList;
import sim.util.gui.NumberTextField;
import episiminterfaces.calc.CalculationAlgorithm.CalculationAlgorithmType;


public class ChartCreationWizard extends JDialog {
	
	
   private TissueCellDataFieldsInspector cellDataFieldsInspector;
   
   private EpisimChart episimChart;
   private boolean okButtonPressed = false;
   protected ArrayList<Object[]> attributesList = new ArrayList<Object[]>();
   
   private JFreeChart previewChart;
   private ChartPanel previewChartPanel;
   private XYSeriesCollection dataset = new XYSeriesCollection();
   
   private  DatasetChangeEvent updateEvent;
   
 
   private Map<String, AbstractCell> cellTypesMap;
   private Map<Integer, Long> seriesIdMap;
   private JTextField chartTitleField;
   private JTextField chartXLabel;
   private JTextField chartYLabel;
   private JTextField baselineField;
   private JTextField pngPathField;
   private JButton changePngPathButton;
   private JCheckBox legendCheck;
   private JCheckBox xAxisLogarithmicCheck;
   private JCheckBox yAxisLogarithmicCheck;
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
   
   
   private CalculationAlgorithmConfigurator baselineCalculationAlgorithmConfigurator = null;
      
   private boolean isDirty = false;
   private boolean baselineEnabled = true;
   

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
					    isDirty = true;
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
	               	comboModel.addElement(new ChartSeriesName(DEFAULTSERIENAME + (index+1)));
	               	seriesCombo.setSelectedIndex(index);
	               	isDirty = true;
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
				seriesPanel = new JPanel(seriesCards);
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
       episimChartSeries.setCalculationAlgorithmConfigurator(null);
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
      csa.setCalculationAlgorithmConfigurator(chartSeries.getCalculationAlgorithmConfigurator());
      csa.getFormulaButton().setText("Edit Expression");
      csa.setStretch((float)chartSeries.getStretch());
      csa.setThickness((float)chartSeries.getThickness());
      seriesPanel.add(csa, ""+index);
      attributesList.add(new Object[] {csa,series});
      comboModel.addElement(new ChartSeriesName(chartSeries.getName()));
      
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
   	int randomValue = 0;
   	for(int i = 1; i<= 101; i += 10){
   		randomValue = rand.nextInt(100);
   		if(randomValue == 0) randomValue = 1;
   		series.add(i, randomValue);
   	}
   }
   
   /* Constructs an XYLineChart.  Ultimately we might allow various charts; but we need to also set the
      chart's antialiasing, titles, etc. to reflect the current desired information. */
   private ChartPanel buildXYLineChart()
       {
 	
       previewChart = ChartFactory.createXYLineChart("Untitled Chart","Untitled X Axis","Untitled Y Axis",dataset,
                                              PlotOrientation.VERTICAL, false, true, false);
       ((XYLineAndShapeRenderer)(((XYPlot)(previewChart.getPlot())).getRenderer())).setDrawSeriesLineAsPath(true);
       XYPlot xyPlot =(XYPlot)previewChart.getPlot();
       xyPlot.setBackgroundPaint(Color.WHITE);
 		xyPlot.setDomainGridlinePaint(Color.LIGHT_GRAY);
 		xyPlot.setRangeGridlinePaint(Color.LIGHT_GRAY);
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
  
	
	public void showWizard(){
		
		isDirty = false;
			showWizard(null);
	}
	
	
	private void restoreChartValues(EpisimChart chart){
		if(chart != null){
			chart.getEpisimChartSeries().size();
			this.episimChart = chart.clone();
			
			this.chartTitleField.setText(episimChart.getTitle());
			this.setTitle(episimChart.getTitle());
			
			this.chartXLabel.setText(episimChart.getXLabel());
			this.setDomainAxisLabel(episimChart.getXLabel());
			
			this.chartYLabel.setText(episimChart.getYLabel());
			this.setRangeAxisLabel(episimChart.getYLabel());
			
			this.baselineCalculationAlgorithmConfigurator = episimChart.getBaselineCalculationAlgorithmConfigurator();
			if(chart.getBaselineCalculationAlgorithmConfigurator() != null){
				this.baselineField.setText(CalculationAlgorithmServer.getInstance().getCalculationAlgorithmDescriptor(this.baselineCalculationAlgorithmConfigurator.getCalculationAlgorithmID()).getName());
				this.baselineButton.setText("Edit Baseline Expression");
			}
			this.legendCheck.setSelected(episimChart.isLegendVisible());
			
			
			this.xAxisLogarithmicCheck.setSelected(episimChart.isXAxisLogarithmic());
			this.setXAxisLogarithmic(episimChart.isXAxisLogarithmic());
			
			this.yAxisLogarithmicCheck.setSelected(episimChart.isYAxisLogarithmic());
			this.setYAxisLogarithmic(episimChart.isYAxisLogarithmic());
			
			this.aliasCheck.setSelected(episimChart.isAntialiasingEnabled());
			this.setAntiAliasEnabled(episimChart.isAntialiasingEnabled());
			
			this.pngCheck.setSelected(episimChart.isPNGPrintingEnabled());
			if(episimChart.isPNGPrintingEnabled()){
				pngFrequencyInSimulationSteps.setEnabled(true);
				this.changePngPathButton.setEnabled(true);
				this.pngPathField.setText(episimChart.getPNGPrintingPath().getAbsolutePath());
				pngPathField.setEnabled(true);
			}
			else{
				pngFrequencyInSimulationSteps.setEnabled(false);
				this.changePngPathButton.setEnabled(false);
				pngPathField.setEnabled(false);
			}
			
			this.pngFrequencyInSimulationSteps.setValue(episimChart.getPNGPrintingFrequency());
			this.chartFrequencyInSimulationSteps.setValue(episimChart.getChartUpdatingFrequency());
			int i = 0;
			for(EpisimChartSeries chartSeries: episimChart.getEpisimChartSeries()){ 
				addSeries(i, chartSeries);
				i++;
			}
			if(!checkIfBaseLineShouldBeEnabled()) deactivateBaseLine();
			if(!checkIfLogarithmicAxisShouldBeEnabled()) deactivateLogarithmicAxis();
			this.isDirty = false;
		}
		
	}	
		
	public void showWizard(EpisimChart chart){
		isDirty = false;
		if(chart != null) restoreChartValues(chart);
		repaint();
		centerMe();
		setVisible(true);
	}
	
	public EpisimChart getEpisimChart(){
		if(this.okButtonPressed){
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
		if(baselineEnabled && !CalculationController.getInstance().isValidCalculationAlgorithmConfiguration(episimChart.getBaselineCalculationAlgorithmConfigurator(), true, this.cellDataFieldsInspector)){
			errorFound = true;
			JOptionPane.showMessageDialog(ChartCreationWizard.this, "Please enter valid Baseline-Expression!", "Error", JOptionPane.ERROR_MESSAGE);
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
			errorFound = !hasEverySeriesAnConfigurator();
			if(errorFound)
				JOptionPane.showMessageDialog(ChartCreationWizard.this, "Not every Chart-Series has an Expression!", "Error", JOptionPane.ERROR_MESSAGE);
		}
		if(!errorFound){
			episimChart.setIsDirty(isDirty);
			ChartCreationWizard.this.okButtonPressed = true;
			ChartCreationWizard.this.setVisible(false);
			ChartCreationWizard.this.dispose();
		}
	}	
	private boolean hasEverySeriesAnConfigurator(){
		for(EpisimChartSeries chartSeries:this.episimChart.getEpisimChartSeries()){
			if(!CalculationController.getInstance().isValidCalculationAlgorithmConfiguration(chartSeries.getCalculationAlgorithmConfigurator(), true, this.cellDataFieldsInspector)) return false;
					
		}
		
		return true;
	}
	
	private void centerMe(){
		if(this.getParent() == null){
			Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
			this.setLocation(((int)(screenDim.getWidth() /2) - (this.getWidth()/2)), 
			((int)(screenDim.getHeight() /2) - (this.getHeight()/2)));
		}
		else{
			Dimension parentDim = this.getParent().getSize();
			this.setLocation(((int)(this.getParent().getLocation().getX()+((parentDim.getWidth() /2) - (this.getWidth()/2)))), 
			((int)(this.getParent().getLocation().getY()+((parentDim.getHeight() /2) - (this.getHeight()/2)))));
		}
	}
	
	private JPanel buildChartOptionPanel() {

		JPanel optionsPanel = new JPanel(new BorderLayout());
		Box globalAttributes = Box.createVerticalBox();
		chartTitleField = new JTextField();
		chartTitleField.setText(previewChart.getTitle().getText());
		episimChart.setTitle(chartTitleField.getText());
		chartTitleField.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent keyEvent) {
				isDirty = true;
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
				isDirty = true;
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
				isDirty = true;
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

		baselineCalculationAlgorithmConfigurator = null;
		baselineButton = new JButton("Add Baseline Expression");
      baselineField = new JTextField("");
      baselineButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				isDirty = true;
				
				
				Set<CalculationAlgorithmType> allowedTypes = new HashSet<CalculationAlgorithmType>();
				if(areAllChartSeriesOfType(CalculationAlgorithmType.ONEDIMRESULT)) allowedTypes.add(CalculationAlgorithmType.ONEDIMRESULT);
				
				
		         DataEvaluationWizard editor = new DataEvaluationWizard(
		         		((Frame)ChartCreationWizard.this.getOwner()), "Baseline Expression Editor", true, cellDataFieldsInspector, allowedTypes);
		         baselineCalculationAlgorithmConfigurator =editor.getCalculationAlgorithmConfigurator(baselineCalculationAlgorithmConfigurator);
		         if(baselineCalculationAlgorithmConfigurator != null){
		         	baselineButton.setText("Edit Baseline Expression");
		         	baselineField.setText(CalculationAlgorithmServer.getInstance().getCalculationAlgorithmDescriptor(baselineCalculationAlgorithmConfigurator.getCalculationAlgorithmID()).getName());
		         	episimChart.setBaselineCalculationAlgorithmConfigurator(baselineCalculationAlgorithmConfigurator);
		         	episimChart.setRequiredClassesForBaseline(cellDataFieldsInspector.getRequiredClasses());
		         }
				
        }
     	 
      });
      baselineField.setEditable(false);
      list.add(baselineButton, baselineField);
      
      chartFrequencyLabel = new JLabel("Chart Updating Frequency in Simulation Steps: ");
		
		chartFrequencyInSimulationSteps = new NumberTextField(100,false){
			public double newValue(double newValue)
	      {
				isDirty = true; 
				newValue = Math.round(newValue);
				if(newValue <= 0) newValue=1;
				episimChart.setChartUpdatingFrequency((int) newValue);
	        return newValue;
	      }
		};
		chartFrequencyInSimulationSteps.addKeyListener(new KeyAdapter() {

			public void keyPressed(KeyEvent keyEvent) {
				isDirty = true;
				if(keyEvent.getKeyCode() == KeyEvent.VK_ENTER){
					chartFrequencyInSimulationSteps.newValue(chartFrequencyInSimulationSteps.getValue());
				}
				else if(keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE)
					chartFrequencyInSimulationSteps.newValue(chartFrequencyInSimulationSteps.getValue());
			}
		});
		chartFrequencyInSimulationSteps.addFocusListener(new FocusAdapter() {

			public void focusLost(FocusEvent e) {

				chartFrequencyInSimulationSteps.newValue(chartFrequencyInSimulationSteps.getValue());
			}
		});
		
		
		list.add(chartFrequencyLabel, chartFrequencyInSimulationSteps);
		
		xAxisLogarithmicCheck = new JCheckBox();
		xAxisLogarithmicCheck.setSelected(false);
		episimChart.setXAxisLogarithmic(false);
		xAxisLogarithmicCheck.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				isDirty = true;
				setXAxisLogarithmic(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		list.add(new JLabel("Logarithmic X Axis"), xAxisLogarithmicCheck);
		
		yAxisLogarithmicCheck = new JCheckBox();
		yAxisLogarithmicCheck.setSelected(false);
		episimChart.setYAxisLogarithmic(false);
		yAxisLogarithmicCheck.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				isDirty = true;
				setYAxisLogarithmic(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		list.add(new JLabel("Logarithmic Y Axis"), yAxisLogarithmicCheck);
		
		
		legendCheck = new JCheckBox();
		legendCheck.setSelected(false);
		episimChart.setLegendVisible(false);
		legendCheck.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				isDirty = true;
				setChartLegendVisible(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		list.add(new JLabel("Legend"), legendCheck);		

		aliasCheck = new JCheckBox();
		aliasCheck.setSelected(previewChart.getAntiAlias());
		episimChart.setAntialiasingEnabled(previewChart.getAntiAlias());
		aliasCheck.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				isDirty = true;
				setAntiAliasEnabled(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		list.add(new JLabel("Antialias"), aliasCheck);
	
      pngCheck = new JCheckBox();
      pngCheck.setSelected(false);
      episimChart.setPNGPrintingEnabled(false);
		pngCheck.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				isDirty = true;
				if(ChartCreationWizard.this.isVisible()){
					if(e.getStateChange() == ItemEvent.SELECTED){
						
						if(!episimChart.isPNGPrintingEnabled())selectPNGPath(false);
						
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
				isDirty = true;
				selectPNGPath(true);	         
         }});
		fieldButtonPanel.add(this.pngPathField, BorderLayout.CENTER);
		fieldButtonPanel.add(this.changePngPathButton, BorderLayout.EAST);
		list.add(labelCheckPanel, fieldButtonPanel);
		
		pngFrequencyInSimulationSteps = new NumberTextField(100,false){
			public double newValue(double newValue)
	      {
				isDirty = true;
				newValue = Math.round(newValue);
				if(newValue <= 0) newValue=1;
				episimChart.setPNGPrintingFrequency((int) newValue);
	        return newValue;
	      }
		};
		pngFrequencyInSimulationSteps.addKeyListener(new KeyAdapter() {

			public void keyPressed(KeyEvent keyEvent) {
				isDirty = true;
				if(keyEvent.getKeyCode() == KeyEvent.VK_ENTER){
					
					pngFrequencyInSimulationSteps.newValue(pngFrequencyInSimulationSteps.getValue());
				}
				else if(keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE)
					pngFrequencyInSimulationSteps.newValue(pngFrequencyInSimulationSteps.getValue());
			}
		});
		pngFrequencyInSimulationSteps.addFocusListener(new FocusAdapter() {

			public void focusLost(FocusEvent e) {

				pngFrequencyInSimulationSteps.newValue(pngFrequencyInSimulationSteps.getValue());
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
		fileChooser.setDialogTitle("Choose PNG Printing Path");
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
      	pngCheck.setSelected(true);
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
	
	private void setXAxisLogarithmic(boolean val){
		if(val){
			episimChart.setXAxisLogarithmic(true);
			ValueAxis  xAxis = previewChart.getXYPlot().getDomainAxis();
			previewChart.getXYPlot().setDomainAxis(new LogarithmicAxis(xAxis.getLabel()));
			
		}
		else{
			episimChart.setXAxisLogarithmic(false);
			ValueAxis  xAxis = previewChart.getXYPlot().getDomainAxis();
			previewChart.getXYPlot().setDomainAxis(new NumberAxis(xAxis.getLabel()));
		}
	}
	
	private void setYAxisLogarithmic(boolean val){
		if(val){
			episimChart.setYAxisLogarithmic(true);
			ValueAxis  yAxis = previewChart.getXYPlot().getRangeAxis();
			previewChart.getXYPlot().setRangeAxis(new LogarithmicAxis(yAxis.getLabel()));
			
		}
		else{
			episimChart.setYAxisLogarithmic(false);
			ValueAxis  yAxis = previewChart.getXYPlot().getRangeAxis();
			previewChart.getXYPlot().setRangeAxis(new NumberAxis(yAxis.getLabel()));
		}
	}
	
	
   private void deactivateBaseLine(){
   	baselineEnabled = false;
   	baselineButton.setText("Add Baseline Expression");
   	this.baselineButton.setEnabled(false);
   	this.baselineField.setText("");
   	episimChart.setBaselineCalculationAlgorithmConfigurator(null);
   	episimChart.setRequiredClassesForBaseline(new HashSet<Class<?>>());
   	this.baselineCalculationAlgorithmConfigurator = null;
   }
   
   private void activateBaseline(){
   	baselineEnabled = true;
   	this.baselineButton.setEnabled(true);
   }
   
   private void activateLogarithmicAxis(){
   	if(xAxisLogarithmicCheck != null){ 
			xAxisLogarithmicCheck.setEnabled(true);
		}
		if(yAxisLogarithmicCheck != null){ 
			yAxisLogarithmicCheck.setEnabled(true);
		}
   }
   private void deactivateLogarithmicAxis(){
   	if(xAxisLogarithmicCheck != null){ 
			xAxisLogarithmicCheck.setSelected(false);
			xAxisLogarithmicCheck.setEnabled(false);
		}
		if(yAxisLogarithmicCheck != null){ 
			yAxisLogarithmicCheck.setSelected(false);
			yAxisLogarithmicCheck.setEnabled(false);
		}
   }
	
   private boolean checkSeriesCalculationConfiguratorCompatibility(long seriesID, CalculationAlgorithmConfigurator config){
   	boolean deactivateBaseLine = false;
   	Set<CalculationAlgorithmType> typesToLookFor = new HashSet<CalculationAlgorithmType>();
   	for(CalculationAlgorithmType type : CalculationAlgorithmType.values()) typesToLookFor.add(type);
   	
   	CalculationAlgorithmDescriptor descr = CalculationAlgorithmServer.getInstance().getCalculationAlgorithmDescriptor(config.getCalculationAlgorithmID());
   	typesToLookFor.remove(descr.getType());
   	
   	Set<EpisimChartSeries> incompatibleSeries = lookForAlgorithmTypeInSeries(typesToLookFor);
   	
   
   	
   	if(descr.getType() == CalculationAlgorithmType.ONEDIMRESULT){
   		activateLogarithmicAxis();
      }
   	else{
   		if(descr.getType() == CalculationAlgorithmType.HISTOGRAMRESULT){
   			deactivateLogarithmicAxis();
   		}
   		deactivateBaseLine = true;
   	}
   	
   	for(EpisimChartSeries series: incompatibleSeries){
   		if(series.getId() == seriesID) incompatibleSeries.remove(series);
   	}
   	
   	if(!incompatibleSeries.isEmpty() || (deactivateBaseLine && this.baselineCalculationAlgorithmConfigurator != null)){
   		StringBuffer message = new StringBuffer();
   		message.append("If you add this Chart Series the following will be deleted: \n");
   		for(EpisimChartSeries series: incompatibleSeries){
   			 
   			message.append(series.getName());
   			message.append(" (Type: ");
   			message.append(CalculationAlgorithmServer.getInstance().getCalculationAlgorithmDescriptor(series.getCalculationAlgorithmConfigurator().getCalculationAlgorithmID()).getType().toString());
   			message.append(")\n");
   		}
   		if(deactivateBaseLine && this.baselineCalculationAlgorithmConfigurator != null){
   			message.append("Baseline ");
   			message.append(" (Type: ");
   			message.append(CalculationAlgorithmServer.getInstance().getCalculationAlgorithmDescriptor(this.baselineCalculationAlgorithmConfigurator.getCalculationAlgorithmID()).getType().toString());
   			message.append(")\n");
   		}
   		
   		
   		
   		message.append("\nDo you want to add the new Chart Series?");
   		
   		int result =JOptionPane.showConfirmDialog(this, message.toString(), "Incompatible Chart Series found", JOptionPane.YES_NO_OPTION);
   		
   		if(result == JOptionPane.YES_OPTION){
   			
   			for(EpisimChartSeries series: incompatibleSeries){
   				if(series.getId() != seriesID)removeSeries(getIndexOfChartSeries(series));
   			}
   			if(deactivateBaseLine){
   				deactivateBaseLine();
   			}
   			
   			return true;
   		}
   		else return false;
   		
   	}
   	   return true;	
   }
   
   private boolean checkIfBaseLineShouldBeEnabled(){
   		
   	
   	return (areAllChartSeriesOfType(CalculationAlgorithmType.ONEDIMRESULT));
   }
   
   private boolean checkIfLogarithmicAxisShouldBeEnabled(){
   		
   	
   	return !(isThereChartSeriesOfType(CalculationAlgorithmType.HISTOGRAMRESULT));
   }
   
   private boolean areAllChartSeriesOfType(CalculationAlgorithmType type){
   	boolean areAllOfType = true;
   	for(EpisimChartSeries series: this.episimChart.getEpisimChartSeries()){
   		if(series.getCalculationAlgorithmConfigurator() != null){
   			if(CalculationAlgorithmServer.getInstance().getCalculationAlgorithmDescriptor(series.getCalculationAlgorithmConfigurator().getCalculationAlgorithmID()).getType() != type){
   				areAllOfType = false;
   				break;
   			}
   		}
   	}
   	return areAllOfType;
   }
   
   private boolean isThereChartSeriesOfType(CalculationAlgorithmType type){
   	
   	for(EpisimChartSeries series: this.episimChart.getEpisimChartSeries()){
   		if(series.getCalculationAlgorithmConfigurator() != null){
   			if(CalculationAlgorithmServer.getInstance().getCalculationAlgorithmDescriptor(series.getCalculationAlgorithmConfigurator().getCalculationAlgorithmID()).getType() == type){
   				return true;
   			}
   		}
   	}
   	return false;
   }
   
   private Set<EpisimChartSeries> lookForAlgorithmTypeInSeries(Set<CalculationAlgorithmType> typesToLookFor){
   	Set<EpisimChartSeries> foundSeries = new HashSet<EpisimChartSeries>();
   	
   	for(EpisimChartSeries series: this.episimChart.getEpisimChartSeries()){
   		if(series.getCalculationAlgorithmConfigurator() != null){
	   		CalculationAlgorithmDescriptor descr = CalculationAlgorithmServer.getInstance().getCalculationAlgorithmDescriptor(series.getCalculationAlgorithmConfigurator().getCalculationAlgorithmID());
	   		if(typesToLookFor.contains(descr.getType())) foundSeries.add(series);
   		}
   	}
   	return foundSeries;
   }
   
   private int getIndexOfChartSeries(EpisimChartSeries series){
   	long id = series.getId();
   	for(int index : this.seriesIdMap.keySet()){
   		if(this.seriesIdMap.get(index) == id) return index;
   	}
   	return -1;
   }
   
   private class ChartSeriesAttributes extends LabelledList
   {
   static final float DASH = 6;
   static final float DOT = 1;
   static final float SPACE = 3;
   static final float SKIP = DASH;
   
   private CalculationAlgorithmConfigurator calculationConfig = null;
  
   
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
   private ExtendedColorWell colorwell;
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
         	  isDirty = true;
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
         	  isDirty = true;
               name = nameF.getText();
               
               setBorderTitle(name);
               getSeries().setKey(name);
               int index = seriesCombo.getSelectedIndex();
              if(index > -1){
               episimChart.getEpisimChartSeries(seriesIdMap.get(index)).setName(name);
               comboModel.removeElementAt(index);
               comboModel.insertElementAt(new ChartSeriesName(name), index);
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
       
       colorwell = new ExtendedColorWell(ChartCreationWizard.this, strokeColor)
       {
       public Color changeColor(Color c) 
           {
      	 	isDirty = true;
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
         	  isDirty = true; 
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
         	  isDirty = true; 
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
         	  isDirty = true;
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
         	  isDirty = true;
               if (JOptionPane.showOptionDialog(
                       null,"Remove the Series " + name + "?","Confirm",
                       JOptionPane.YES_NO_OPTION,
                       JOptionPane.QUESTION_MESSAGE,null,
                       new Object[] { "Remove", "Cancel" },
                       null) == 0)  // remove
              	 ChartCreationWizard.this.removeSeries(seriesIndex);
               if(checkIfBaseLineShouldBeEnabled()) activateBaseline();
               if(checkIfLogarithmicAxisShouldBeEnabled()) activateLogarithmicAxis();
               
               
               }
           });
       
       
       formulaButton = new JButton("Add Expression");
       formulaField = new JTextField("");
       formulaButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				isDirty = true;
				Set<CalculationAlgorithmType> allowedTypes = new HashSet<CalculationAlgorithmType>();
				allowedTypes.addAll(Arrays.asList(CalculationAlgorithmType.values()));
				allowedTypes.remove(CalculationAlgorithmType.MULTIDIMDATASERIESRESULT);
			
	         DataEvaluationWizard editor = new DataEvaluationWizard(
	         		((Frame)ChartCreationWizard.this.getOwner()), "Series Calculation Algorithm Wizard: " + ((ChartSeriesName) seriesCombo.getSelectedItem()).toString(), true, cellDataFieldsInspector, allowedTypes);
	         calculationConfig =editor.getCalculationAlgorithmConfigurator(calculationConfig);
	         
	         
	         
	         if(CalculationController.getInstance().isValidCalculationAlgorithmConfiguration(calculationConfig, false, cellDataFieldsInspector)){
	         	if(checkSeriesCalculationConfiguratorCompatibility(seriesIdMap.get(seriesCombo.getSelectedIndex()), calculationConfig)){
	         		
		         	formulaButton.setText("Edit Expression");
		         	formulaField.setText(CalculationAlgorithmServer.getInstance().getCalculationAlgorithmDescriptor(calculationConfig.getCalculationAlgorithmID()).getName());
		         	int index = seriesCombo.getSelectedIndex();
	               if(episimChart.getEpisimChartSeries(seriesIdMap.get(index)) != null){
	               	EpisimChartSeries series = episimChart.getEpisimChartSeries(seriesIdMap.get(index));
	               	series.setCalculationAlgorithmConfigurator(calculationConfig);
	               	series.setRequiredClasses(cellDataFieldsInspector.getRequiredClasses());
	               }
	               if(checkIfBaseLineShouldBeEnabled()) activateBaseline();
	               else deactivateBaseLine();
	               if(checkIfLogarithmicAxisShouldBeEnabled()) activateLogarithmicAxis();
	               else deactivateLogarithmicAxis();
	         	}
	         	else calculationConfig = null;
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

	
   public CalculationAlgorithmConfigurator getCalculationAlgorithmConfigurator() {
   
   	return calculationConfig;
   }

	
   public void setCalculationAlgorithmConfigurator(CalculationAlgorithmConfigurator configurator) {
   	if(CalculationController.getInstance().isValidCalculationAlgorithmConfiguration(configurator, false, cellDataFieldsInspector)){
   		this.calculationConfig = configurator;
   		this.formulaField.setText(CalculationAlgorithmServer.getInstance().getCalculationAlgorithmDescriptor(configurator.getCalculationAlgorithmID()).getName());
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

	
   
   private class ChartSeriesName{
   	private String name;
   	protected ChartSeriesName(String name){ this.name = name;}
   	public String toString(){ return this.name; }
   }
   
   
	

	}
