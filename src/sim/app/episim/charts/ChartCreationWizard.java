package sim.app.episim.charts;

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
import sim.app.episim.charts.parser.ParseException;
import sim.app.episim.charts.parser.TokenMgrError;
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
   protected ArrayList attributesList = new ArrayList();
   
   private JFreeChart previewChart;
   private ChartPanel previewChartPanel;
   private XYSeriesCollection dataset = new XYSeriesCollection();
   
   private  DatasetChangeEvent updateEvent;
   
 
   private Map<String, CellType> cellTypesMap;
   private JTextField chartTitleField;
   private JTextField chartXLabel;
   private JTextField chartYLabel;
   private JTextField baselineField;
   private JCheckBox legendCheck;
   private JCheckBox pdfCheck;
   
   private NumberTextField frequencyInSimulationSteps;
   private JLabel frequencyLabel;
   
   private JPanel seriesPanel;
   private JPanel propertiesPanel;
	private JSplitPane mainSplit;
   private JComboBox seriesCombo;
   private DefaultComboBoxModel comboModel;
   private JCheckBox aliasCheck;
   
   private final String DEFAULTSERIENAME = "Chart Series ";
 
   private final int WIDTH = 1200;
   private final int HEIGHT = 600;
   
   private String[] baselineExpression;

   /** Generates a new ChartGenerator with a blank chart. */
   public ChartCreationWizard(Frame owner, String title, boolean modal, TissueCellDataFieldsInspector cellDataFieldsInspector){
		super(owner, title, modal);
		
		this.cellDataFieldsInspector= cellDataFieldsInspector;
		if(cellDataFieldsInspector == null) throw new IllegalArgumentException("TissueCellDataFieldsInspector was null !");
		
		this.episimChart = new EpisimChartImpl(ChartController.getInstance().getNextChartId(), this.cellDataFieldsInspector.getTissueTypesMap(),
   			this.cellDataFieldsInspector.getCellTypesMap());
		
		
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
   public int addSeries()
       {
   	 int i = dataset.getSeriesCount();
   	 EpisimChartSeries episimChartSeries = new EpisimChartSeriesImpl((long)i);
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
       return i;
   }
   
   
   private void addSeries(EpisimChartSeries chartSeries){
   	
  	 
  	 XYSeries series = new XYSeries(chartSeries.getName(), false );
  	 addRandomValues(series);
      dataset.addSeries(series);
      
      
      previewChart.getXYPlot().getRenderer().setSeriesPaint((int)chartSeries.getId(), chartSeries.getColor());
      ChartSeriesAttributes csa = new ChartSeriesAttributes(previewChartPanel,(int)chartSeries.getId());
      csa.setDash(chartSeries.getDash());
      csa.setExpression(chartSeries.getExpression());
      csa.setStretch((float)chartSeries.getStretch());
      csa.setThickness((float)chartSeries.getThickness());
      seriesPanel.add(csa, ""+chartSeries.getId());
      attributesList.add(new Object[] {csa,series});
      comboModel.addElement(chartSeries.getName());
      
      validate();
      
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
   	 this.episimChart.removeChartSeries(index);
       XYSeries series = dataset.getSeries(index);
               
       dataset.removeSeries(index);
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
			this.episimChart = chart.clone();
			
			this.chartTitleField.setText(chart.getTitle());
			this.setTitle(chart.getTitle());
			
			this.chartXLabel.setText(chart.getXLabel());
			this.setDomainAxisLabel(chart.getXLabel());
			
			this.chartYLabel.setText(chart.getYLabel());
			this.setRangeAxisLabel(chart.getYLabel());
			
			this.baselineExpression = chart.getBaselineExpression();
			if(chart.getBaselineExpression() != null && chart.getBaselineExpression()[0] != null)
				this.baselineField.setText(chart.getBaselineExpression()[0]);
			
			this.legendCheck.setSelected(chart.isLegendVisible());
			
			
			this.aliasCheck.setSelected(chart.isAntialiasingEnabled());
			
			
			this.pdfCheck.setSelected(chart.isPDFPrintingEnabled());
			if(chart.isPDFPrintingEnabled())frequencyInSimulationSteps.setEnabled(true);
			
			this.frequencyInSimulationSteps.setValue(chart.getPDFPrintingFrequency());
			
			for(EpisimChartSeries chartSeries: chart.getEpisimChartSeries()) addSeries(chartSeries);
			
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
				boolean errorFound = false;
				if(episimChart.getBaselineExpression() == null || episimChart.getBaselineExpression()[0] == null || episimChart.getBaselineExpression()[1] == null
						|| episimChart.getBaselineExpression()[0].trim().equals("") || episimChart.getBaselineExpression()[1].trim().equals("")){
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
	
	private boolean hasEverySeriesAnExpression(){
		for(EpisimChartSeries chartSeries:this.episimChart.getEpisimChartSeries()){
			if(chartSeries.getExpression() == null) return false;
			if(chartSeries.getExpression().length < 2) return false;
			if(chartSeries.getExpression()[0] == null) return false;
			if(chartSeries.getExpression()[1] == null) return false;
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
		final JButton baselineButton = new JButton("Add Baseline Expression");
      baselineField = new JTextField("");
      baselineButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {

	         ChartExpressionEditor editor = new ChartExpressionEditor(
	         		((Frame)ChartCreationWizard.this.getOwner()), "Baseline Expression Editor", true, cellDataFieldsInspector);
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
	
      pdfCheck = new JCheckBox();
      pdfCheck.setSelected(false);
      episimChart.setPDFPrintingEnabled(false);
		pdfCheck.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				if(ChartCreationWizard.this.isVisible()){
					if(e.getStateChange() == ItemEvent.SELECTED){
						episimChart.setPDFPrintingEnabled(true);
						FileDialog fd = new FileDialog(ChartCreationWizard.this,"Choose PDF Printing Path", FileDialog.SAVE);
		            if(episimChart.getPDFPrintingPath() == null)fd.setFile(previewChart.getTitle().getText() + ".pdf");
		            else{
		            	fd.setDirectory(episimChart.getPDFPrintingPath().getPath());
		            	fd.setFile(episimChart.getPDFPrintingPath().getName());
		            }
		            fd.setVisible(true);
		            
		            String fileName = fd.getFile();
		            if (fileName!=null)
		            {
		            	episimChart.setPDFPrintingPath(new File(fd.getFile()));
		            	frequencyInSimulationSteps.setEnabled(true);
		            	frequencyLabel.setEnabled(true);
		            	//  	Dimension dim = previewChartPanel.getPreferredSize();
		            	//   printChartToPDF( previewChart, dim.width, dim.height, fd.getDirectory() + fileName );
		            }
		            else{
		            	pdfCheck.setSelected(false);
		            	episimChart.setPDFPrintingEnabled(false);
		            }
		           }
					else{
						episimChart.setPDFPrintingEnabled(false);
						frequencyInSimulationSteps.setEnabled(false);
						frequencyLabel.setEnabled(false);
					}
				}
			}
		});
		list.add(new JLabel("Save as PDF"), pdfCheck);
		
		frequencyInSimulationSteps = new NumberTextField(1,false){
			public double newValue(double newValue)
	      {
				 newValue = Math.round(newValue);;
				episimChart.setPDFPrintingFrequency((int) newValue);
	        return newValue;
	      }
		};
		frequencyInSimulationSteps.addKeyListener(new KeyAdapter() {

			public void keyPressed(KeyEvent keyEvent) {

				if(keyEvent.getKeyCode() == KeyEvent.VK_ENTER){
					episimChart.setPDFPrintingFrequency((int)frequencyInSimulationSteps.getValue());
				}
				else if(keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE)
					frequencyInSimulationSteps.setValue(frequencyInSimulationSteps.getValue());
			}
		});
		frequencyInSimulationSteps.addFocusListener(new FocusAdapter() {

			public void focusLost(FocusEvent e) {

				episimChart.setPDFPrintingFrequency((int)frequencyInSimulationSteps.getValue());
			}
		});
		frequencyInSimulationSteps.setEnabled(false);
		frequencyLabel = new JLabel("Frequency in Simulation Steps: ");
		frequencyLabel.setEnabled(false);
		
		list.add(frequencyLabel, frequencyInSimulationSteps);
		
		optionsPanel.add(list, BorderLayout.CENTER);
		
		return optionsPanel;
	}
	
	private void setAntiAliasEnabled(boolean val){
		previewChart.setAntiAlias(val);
		episimChart.setAntialiasingEnabled(val);
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
   
   public void setIndex(int i) { seriesIndex = i; }

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
               episimChart.getEpisimChartSeries(index).setName(name);
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
           episimChart.getEpisimChartSeries(index).setColor(c);
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
               if(index >= 0)episimChart.getEpisimChartSeries(index).setThickness(thickness);
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
               if(index >= 0) episimChart.getEpisimChartSeries(index).setDash(dash);
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
               	episimChart.getEpisimChartSeries(index).setStretch(stretch);
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
       
       
       final JButton formulaButton = new JButton("Add Expression");
       formulaField = new JTextField("");
       formulaButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {

	         ChartExpressionEditor editor = new ChartExpressionEditor(
	         		((Frame)ChartCreationWizard.this.getOwner()), "Series Expression Editor: " + ((String) seriesCombo.getSelectedItem()), true, cellDataFieldsInspector);
	         expression =editor.getExpression(expression);
	         if(expression != null && expression[0] != null && expression[1] != null){
	         	formulaButton.setText("Edit Expression");
	         	formulaField.setText(expression[0]);
	         	int index = seriesCombo.getSelectedIndex();
               episimChart.getEpisimChartSeries(index).setExpression(expression);
	         }
	         
         }
      	 
       });
       formulaField.setEditable(false);
       add(formulaButton, formulaField);
       
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
