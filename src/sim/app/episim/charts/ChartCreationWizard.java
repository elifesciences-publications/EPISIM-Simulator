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
import org.jfree.data.general.SeriesChangeEvent;
import org.jfree.data.general.SeriesChangeListener;
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

import sim.app.episim.CellType;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.charts.build.EpisimChart;
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
   
   protected ArrayList attributesList = new ArrayList();
   protected XYSeriesCollection dataset = new XYSeriesCollection();
   
   protected JFreeChart previewChart;
   protected ChartPanel previewChartPanel;
   
   private  DatasetChangeEvent updateEvent;
   
 
   private Map<String, CellType> cellTypesMap;
   private JTextField chartTitleField;
   private JTextField chartXLabel;
   private JTextField chartYLabel;
   private NumberTextField frequencyInSimulationSteps;
   private JLabel frequencyLabel;
   
   private JPanel seriesPanel;
   private JPanel propertiesPanel;
	private JSplitPane mainSplit;
   private JComboBox seriesCombo;
   private DefaultComboBoxModel comboModel;
   
   private final String DEFAULTSERIENAME = "Chart Series ";
 
   private final int WIDTH = 1200;
   private final int HEIGHT = 600;
   
   private String[] baselineExpression;

   /** Generates a new ChartGenerator with a blank chart. */
   public ChartCreationWizard(Frame owner, String title, boolean modal){
		super(owner, title, modal);
		
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
   	 XYSeries series = new XYSeries(DEFAULTSERIENAME+ (i+1), false );
   	 addRandomValues(series);
       dataset.addSeries(series);
       previewChart.getXYPlot().getRenderer().setSeriesPaint(i, Color.BLACK);
       ChartSeriesAttributes csa = new ChartSeriesAttributes(previewChartPanel,i);
      
       seriesPanel.add(csa, ""+i);
       attributesList.add(new Object[] {csa,series});
       
       validate();
       
       return i;
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
	
	public void createNewChart(TissueCellDataFieldsInspector cellDataFieldsInspector){
		
			this.cellDataFieldsInspector = cellDataFieldsInspector;
			
			repaint();
			centerMe();
			setVisible(true);
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
      final JTextField baselineField = new JTextField("");
      baselineButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {

	         ChartExpressionEditor editor = new ChartExpressionEditor(
	         		((Frame)ChartCreationWizard.this.getOwner()), "Baseline Expression Editor", true, cellDataFieldsInspector);
	         baselineExpression =editor.getExpression(baselineExpression);
	         if(baselineExpression != null && baselineExpression[0] != null && baselineExpression[1] != null){
	         	baselineButton.setText("Edit Baseline Expression");
	         	baselineField.setText(baselineExpression[0]);
	         }
	         
        }
     	 
      });
      baselineField.setEditable(false);
      list.add(baselineButton, baselineField);
		
		
		
		final JCheckBox legendCheck = new JCheckBox();
		legendCheck.setSelected(false);
		ItemListener il = new ItemListener() {

			public void itemStateChanged(ItemEvent e) {

				if(e.getStateChange() == ItemEvent.SELECTED){
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
					previewChart.removeLegend();
				}
			}
		};
		legendCheck.addItemListener(il);
		list.add(new JLabel("Legend"), legendCheck);

		final JCheckBox aliasCheck = new JCheckBox();
		aliasCheck.setSelected(previewChart.getAntiAlias());
		il = new ItemListener() {

			public void itemStateChanged(ItemEvent e) {

				previewChart.setAntiAlias(e.getStateChange() == ItemEvent.SELECTED);
			}
		};
		aliasCheck.addItemListener(il);
		list.add(new JLabel("Antialias"), aliasCheck);
	
      final JCheckBox pdfCheck = new JCheckBox();
      pdfCheck.setSelected(false);
		pdfCheck.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {

				if(e.getStateChange() == ItemEvent.SELECTED){
					
					FileDialog fd = new FileDialog(ChartCreationWizard.this,"Choose PDF file...", FileDialog.SAVE);
	            fd.setFile(previewChart.getTitle().getText() + ".PDF");
	            fd.setVisible(true);;
	            String fileName = fd.getFile();
	            if (fileName!=null)
	            {
	            	frequencyInSimulationSteps.setEnabled(true);
	            	frequencyLabel.setEnabled(true);
	            	Dimension dim = previewChartPanel.getPreferredSize();
	               printChartToPDF( previewChart, dim.width, dim.height, fd.getDirectory() + fileName );
	            }
	            else pdfCheck.setSelected(false);
	           }
				else{
					
					frequencyInSimulationSteps.setEnabled(false);
					frequencyLabel.setEnabled(false);
				}
			}
		});
		list.add(new JLabel("Save as PDF"), pdfCheck);
		
		frequencyInSimulationSteps = new NumberTextField(1,false){
			public double newValue(double newValue)
	      {
	        return Math.round(newValue);
	      }
		};
		frequencyInSimulationSteps.setEnabled(false);
		frequencyLabel = new JLabel("Frequency in Simulation Steps: ");
		frequencyLabel.setEnabled(false);
		
		list.add(frequencyLabel, frequencyInSimulationSteps);
		
		optionsPanel.add(list, BorderLayout.CENTER);
		
		return optionsPanel;
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

   float stretch = 1.0f;
   float thickness = 2.0f;
   float[] dash = dashes[0];
   int seriesIndex;
   ChartPanel panel;
   String name;
   Color strokeColor;
   //Color fillColor = CLEAR;

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
       final JTextField nameF = new JTextField(name);
       nameF.addActionListener(new ActionListener()
           {
           public void actionPerformed(ActionEvent e)
               {
               name = nameF.getText();
               setBorderTitle(name);
               getSeries().setKey(name);
               int index = seriesCombo.getSelectedIndex();
               comboModel.removeElementAt(index);
               comboModel.insertElementAt(name, index);
               seriesCombo.setSelectedIndex(index);
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
       ColorWell well = new ColorWell(strokeColor)
       {
       public Color changeColor(Color c) 
           {
           ChartSeriesAttributes.this.strokeColor = c;
           rebuildGraphicsDefinitions();
           return c;
           }
       };
      
       addLabelled("Line",well);
      
       NumberTextField thickitude = new NumberTextField(2.0,true)
           {
           public double newValue(double newValue) 
               {
               if (newValue < 0.0) 
                   newValue = currentValue;
               thickness = (float)newValue;
               rebuildGraphicsDefinitions();
               return newValue;
               }
           };
       addLabelled("Width",thickitude);
       final JComboBox list = new JComboBox();
       list.setEditable(false);
       list.setModel(new DefaultComboBoxModel(new Vector(Arrays.asList(
                                                             new String[] { "Solid", "__  __  __", "_  _  _  _", "_ _ _ _ _", "_ _ . _ _ .", "_ . _ . _ .", "_ . . _ . .", ". . . . . . .", ".  .  .  .  ." }))));
       list.setSelectedIndex(0);
       list.addActionListener(new ActionListener()
           {
           public void actionPerformed ( ActionEvent e )
               {
               dash = dashes[list.getSelectedIndex()];
               rebuildGraphicsDefinitions();
               }
           });
       addLabelled("Dash",list);
       NumberTextField stretchField = new NumberTextField(1.0,true)
           {
           public double newValue(double newValue) 
               {
               if (newValue < 0.0) 
                   newValue = currentValue;
               stretch = (float)newValue;
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
       final JTextField formulaField = new JTextField("");
       formulaButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {

	         ChartExpressionEditor editor = new ChartExpressionEditor(
	         		((Frame)ChartCreationWizard.this.getOwner()), "Series Expression Editor: " + ((String) seriesCombo.getSelectedItem()), true, cellDataFieldsInspector);
	         expression =editor.getExpression(expression);
	         if(expression != null && expression[0] != null && expression[1] != null){
	         	formulaButton.setText("Edit Expression");
	         	formulaField.setText(expression[0]);
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
   }
   
   }


