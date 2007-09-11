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
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.event.TitleChangeEvent;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.SeriesChangeEvent;
import org.jfree.data.general.SeriesChangeListener;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

import sim.app.episim.charts.parser.ParseException;
import sim.app.episim.charts.parser.TokenMgrError;
import sim.util.gui.LabelledList;
import sim.util.gui.NumberTextField;
import sim.util.media.ChartGenerator;



public class ChartCreationWizard extends JDialog {
	
	
   
   protected Box attributes = Box.createVerticalBox();
   protected ArrayList attributesList = new ArrayList();
   protected XYSeriesCollection dataset = new XYSeriesCollection();
   protected HashMap stoppables = new HashMap();
   protected JFreeChart previewChart;
   protected ChartPanel previewChartPanel;
   private JScrollPane previewChartScroll = new JScrollPane();
   
   JTextField chartTitleField;
   JTextField chartXLabel;
   JTextField chartYLabel;

   // XYSeries violate the hashing and equality testing, so they don't 
   // produce correct results in hash tables.  We need to put them in
   // a wrapper instead.
   class SeriesHolder
       {
       public SeriesHolder(XYSeries series) { this.series = series; }
       public XYSeries series;
       public boolean equals(Object o) { return ((SeriesHolder)o).series == series; }
       public int hashCode() { return System.identityHashCode(series); }
       }

   DatasetChangeEvent updateEvent;
       
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
   public int addSeries( final XYSeries series, final org.jfree.data.general.SeriesChangeListener stopper)
       {
       int i = dataset.getSeriesCount();
       dataset.addSeries(series);
       ChartSeriesAttributes csa = new ChartSeriesAttributes(previewChartPanel,i);
       attributes.add(csa);
       attributesList.add(new Object[] {csa,series});
      
       validate();
       stoppables.put( new SeriesHolder(series), stopper );
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
               
       // stop the inspector....
       Object tmpObj = stoppables.remove(new SeriesHolder(series));
       if( ( tmpObj != null ) && ( tmpObj instanceof SeriesChangeListener ) )
           ((SeriesChangeListener)tmpObj).seriesChanged(new SeriesChangeEvent(this));
                       
       dataset.removeSeries(index);
       Iterator iter = attributesList.iterator();
       while(iter.hasNext())
           {
           Object[] obj = (Object[])(iter.next());
           ChartSeriesAttributes csa = (ChartSeriesAttributes)(obj[0]);
           series = (XYSeries)(obj[1]);
           if (csa.seriesIndex == index)
               {
               attributes.remove(csa);
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
       this.setTitle(title);
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

   /** Generates a new ChartGenerator with a blank chart. */
   public ChartCreationWizard(Frame owner, String title, boolean modal){
		super(owner, title, modal);
		previewChartPanel = buildXYLineChart();
		
		getContentPane().setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		  		   
		c.anchor =GridBagConstraints.CENTER;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty =0;
		c.insets = new Insets(10,10,10,10);
		c.gridwidth = GridBagConstraints.REMAINDER;
		getContentPane().add(buildChartOptionPanel(), c);
          
      c.anchor =GridBagConstraints.WEST;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		c.weighty =0;
		c.insets = new Insets(10,10,10,10);
		c.gridwidth = GridBagConstraints.REMAINDER;
		JButton pdfButton = new JButton("Save as PDF");
		pdfButton.addActionListener(new ActionListener()
      {
         public void actionPerformed ( ActionEvent e )
         {
            FileDialog fd = new FileDialog(ChartCreationWizard.this,"Choose PDF file...", FileDialog.SAVE);
            fd.setFile(previewChart.getTitle().getText() + ".PDF");
            fd.setVisible(true);;
            String fileName = fd.getFile();
            if (fileName!=null)
            {
               Dimension dim = previewChartPanel.getPreferredSize();
               printChartToPDF( previewChart, dim.width, dim.height, fd.getDirectory() + fileName );
            } 
         }
      });
		getContentPane().add(pdfButton, c);
               
       
		c.anchor =GridBagConstraints.CENTER;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty =0;
		c.insets = new Insets(10,10,10,10);
		c.gridwidth = GridBagConstraints.REMAINDER;
		JScrollPane attributeScroll = new JScrollPane();
		attributeScroll.getViewport().setView(attributes);
		attributeScroll.setBackground(getBackground());
		attributeScroll.getViewport().setBackground(getBackground());
		getContentPane().add(attributeScroll, c);
      
		c.anchor =GridBagConstraints.CENTER;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty =1;
		c.insets = new Insets(10,10,10,10);
		c.gridwidth = GridBagConstraints.REMAINDER;
		
		previewChartScroll.getViewport().setView(previewChartPanel);
		previewChartScroll.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Preview"), 
                                                                      BorderFactory.createEmptyBorder(5,5,5,5)));
		previewChartScroll.setMinimumSize(new Dimension(0,0));
	   getContentPane().add(previewChartScroll, c);
       
      setSize(500, 400);
 		validate();
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

       
   
   static final Color CLEAR = new Color(0,0,0,0);
   class ChartSeriesAttributes extends LabelledList
       {
       static final float DASH = 6;
       static final float DOT = 1;
       static final float SPACE = 3;
       static final float SKIP = DASH;
       
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
       Color strokeColor = Color.black;
       //Color fillColor = CLEAR;
   
       void setIndex(int i) { seriesIndex = i; }
   
       public XYSeries getSeries()
           {
           return dataset.getSeries(seriesIndex);
           }
       
       public XYPlot getPlot()
           {
           return panel.getChart().getXYPlot();
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
                                                            new Boolean(check.isSelected()));  // why in the WORLD is it Boolean?
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
                   getSeries().setKey(name);
                   panel.repaint();
                   }
               });
           addLabelled("Series",nameF);
           ColorWell well = new ColorWell()
               {
               public void setColor(Color c) 
                   {
                   strokeColor = c;
                   rebuildGraphicsDefinitions();
                   }
               };
           strokeColor = (Color)(getPlot().getRenderer().getSeriesPaint(index));
           well.setBackground(strokeColor);
           well.setForeground(strokeColor);
           addLabelled("Line",well);
           //well = new ColorWell()
           //    {
           //    public void setColor(Color c) 
           //        {
           //        fillColor = c;
           //        rebuildGraphicsDefinitions();
           //        }
           //    };
           //fillColor = (Color)(getPlot().getRenderer().getSeriesPaint(index));
           //well.setBackground(fillColor);
           //well.setForeground(fillColor);
           //addLabelled("Fill",well);
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
//           new String[] { "Solid", "Big Dash", "Dash w/Big Skip", "Dash", "Dash Dash Dot", "Dash Dot", "Dash Dot Dot", "Dot", "Dot w/Big Skip" }))));
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
           Box b = new Box(BoxLayout.X_AXIS);
           b.add(removeButton);
           b.add(Box.createGlue());
           add(b);

           rebuildGraphicsDefinitions();
           }
       }
   
   static
       {
       // quaquaify
       try
           {
           String version = System.getProperty("java.version");
           // both of the following will generate exceptions if the class doesn't exist, so we're okay
           // trying to put them in the UIManager here
           if (version.startsWith("1.3"))
               {
               // broken
               //UIManager.put("ColorChooserUI", 
               //      Class.forName("ch.randelshofer.quaqua.Quaqua13ColorChooserUI").getName());
               }
           else // hope there's no one using 1.2! 
               UIManager.put("ColorChooserUI", 
                             Class.forName("ch.randelshofer.quaqua.Quaqua14ColorChooserUI").getName());
           }
       catch (Exception e) { }
       }
       
   class ColorWell extends JButton
       {
       public ColorWell()
           {
           super(" ");
           // quaquaify
           putClientProperty("Quaqua.Button.style","colorWell");
           setOpaque(true);

           addActionListener(new ActionListener()
               {
               public void actionPerformed(ActionEvent e)
                   {
                   Color c = JColorChooser.showDialog(null,
                                                      "Choose Color", getBackground());
                   if (c!=null)
                       {
                       setBackground(c);
                       setForeground(c);
                       setColor(c);
                       }
                   }
               });
           setBackground(Color.black);
           setForeground(Color.black);
           setBorder(new EmptyBorder(0,0,0,0));
           }
       public void setColor(Color c) { }
       }


	
	private Map<String, ChartMonitoredCellType> cellTypesMap;
	
	
	
	
	public void createNewChart(Map<String, ChartMonitoredCellType> cellTypes){
		if(cellTypes != null){
			this.cellTypesMap = cellTypes;
			

		

			repaint();
			centerMe();
			setVisible(true);
		}
		
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
		globalAttributes.add(list);
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

		final JCheckBox legendCheck = new JCheckBox();
		legendCheck.setSelected(false);
		ItemListener il = new ItemListener() {

			public void itemStateChanged(ItemEvent e) {

				if(e.getStateChange() == ItemEvent.SELECTED){
					org.jfree.chart.title.LegendTitle title = new org.jfree.chart.title.LegendTitle(
							(XYItemRenderer) (previewChart.getXYPlot().getRenderer()));
					title.setLegendItemGraphicPadding(new org.jfree.ui.RectangleInsets(0, 8, 0, 4));
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
		

		optionsPanel.add(list);
		return optionsPanel;
	}
   }


