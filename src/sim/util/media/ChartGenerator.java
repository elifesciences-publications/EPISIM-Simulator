/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package sim.util.media;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.io.*;

// From MASON (cs.gmu.edu/~eclab/projects/mason/)
import sim.util.gui.LabelledList;
import sim.util.gui.NumberTextField;

// From JFreeChart (jfreechart.org)
import org.jfree.data.xy.*;
import org.jfree.chart.*;
import org.jfree.chart.event.*;
import org.jfree.chart.plot.*;
import org.jfree.data.general.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.general.*;

// from iText (www.lowagie.com/iText/)
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

/* Example:

import sim.util.media.*;
import org.jfree.data.xy.*;
c = new ChartGenerator();
x = new XYSeries("Snails");
c.addSeries(x);
f = c.createFrame();
f.setVisible(true);;
x.add(10,10);
x.add(20,20);
x.add(30,10);
x.add(40,10);
x.add(50,20);
x.add(60,10);
x.add(70,10);
x.add(80,20);
x.add(90,10);
x.add(100,10);
x.add(110,20);
x.add(120,10);
y = new XYSeries("Slugs");
c.addSeries(y);
y.add(10,12);
y.add(15,3);
y.add(20,6);
*/

/**
   ChartGenerator is a JPanel which displays a time-series chart using the JFreeChart library.
   The facility allows multiple time series to be displayed at one time, to be exported to PDF,
   and to be dynamically added and removed.
*/

public class ChartGenerator extends JPanel
    {
    protected Box globalAttributes = Box.createVerticalBox();
    protected Box attributes = Box.createVerticalBox();
    protected ArrayList attributesList = new ArrayList();
    protected XYSeriesCollection dataset = new XYSeriesCollection();
    protected HashMap stoppables = new HashMap();
    protected JFreeChart chart;
    protected ChartPanel chartPanel;
    JScrollPane chartHolder = new JScrollPane();
    JFrame frame;
    JTextField titleField;
    JTextField xLabel;
    JTextField yLabel;

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
            updateEvent = new DatasetChangeEvent(chart.getPlot(), null);
        chart.getPlot().datasetChanged(updateEvent);
        }

    /** Adds a series, plus a (possibly null) SeriesChangeListener which will receive a <i>single</i>
        event if/when the series is deleted from the chart by the user.
        Returns the series index number. */
    public int addSeries( final XYSeries series, final org.jfree.data.general.SeriesChangeListener stopper)
        {
        int i = dataset.getSeriesCount();
        dataset.addSeries(series);
        ChartSeriesAttributes csa = new ChartSeriesAttributes(chartPanel,i);
        attributes.add(csa);
        attributesList.add(new Object[] {csa,series});
        revalidate();
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
        revalidate();
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
    void buildXYLineChart()
        {
        chart = ChartFactory.createXYLineChart("Untitled Chart","Untitled X Axis","Untitled Y Axis",dataset,
                                               PlotOrientation.VERTICAL, false, true, false);
        ((XYLineAndShapeRenderer)(((XYPlot)(chart.getPlot())).getRenderer())).setDrawSeriesLineAsPath(true);

        chart.setAntiAlias(false);
        chartPanel = new ChartPanel(chart, true);
        chartPanel.setPreferredSize(new java.awt.Dimension(640,480));
        chartPanel.setMinimumDrawHeight(10);
        chartPanel.setMaximumDrawHeight(2000);
        chartPanel.setMinimumDrawWidth(20);
        chartPanel.setMaximumDrawWidth(2000);
        chartHolder.getViewport().setView(chartPanel);
        }
    
    /** Adds a global attribute panel to the frame */
    public void addGlobalAttribute(Component component)
        {
        globalAttributes.add(component);
        }

    /** Returns global attribute panel of the given index. */
    public Component getGlobalAttribute(int index)
        {
        // at present we have a PDF button and a chart global panel --
        // then the global attributes start
        return globalAttributes.getComponent(index+2);
        }

    /** Returns the number of global attribute panels. */
    public int getGlobalAttributeCount()
        {
        // at present we have a PDF button and a chart global panel --
        // then the global attributes start
        return globalAttributes.getComponentCount()-2;
        }

    /** Remooves the global attribute at the given index and returns it. */
    public Component removeGlobalAttribute(int index)
        {
        Component component = getGlobalAttribute(index);
        globalAttributes.remove(index);
        return component;
        }
                
    public void setTitle(String title)
        {
        chart.setTitle(title);
        chart.titleChanged(new TitleChangeEvent(new org.jfree.chart.title.TextTitle(title)));
        if (frame!=null) frame.setTitle(title);
        titleField.setText(title);
        }

    public String getTitle()
        {
        return chart.getTitle().getText();
        }
                
    public void setRangeAxisLabel(String val)
        {
        XYPlot xyplot = (XYPlot)(chart.getPlot());
        xyplot.getRangeAxis().setLabel(val);
        xyplot.axisChanged(new AxisChangeEvent(xyplot.getRangeAxis()));
        yLabel.setText(val);
        }
                
    public String getRangeAxisLabel()
        {
        return ((XYPlot)(chart.getPlot())).getRangeAxis().getLabel();
        }
                
    public void setDomainAxisLabel(String val)
        {
        XYPlot xyplot = (XYPlot)(chart.getPlot());
        xyplot.getDomainAxis().setLabel(xLabel.getText());
        xyplot.axisChanged(new AxisChangeEvent(xyplot.getDomainAxis()));
        xLabel.setText(val);
        }
                
    public String getDomainAxisLabel()
        {
        return ((XYPlot)(chart.getPlot())).getDomainAxis().getLabel();
        }

    /** Generates a new ChartGenerator with a blank chart. */
    public ChartGenerator()
        {
        // create the chart
        buildXYLineChart();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
        split.setBorder(new EmptyBorder(0,0,0,0));
        JScrollPane scroll = new JScrollPane();
        scroll.getViewport().setView(attributes);
        scroll.setBackground(getBackground());
        scroll.getViewport().setBackground(getBackground());
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        KeyListener listener;

        titleField = new JTextField();
        titleField.setText(chart.getTitle().getText());
        titleField.addKeyListener(new KeyListener()
            {
            public void keyReleased(KeyEvent keyEvent) {}
            public void keyTyped(KeyEvent keyEvent) {}
            public void keyPressed(KeyEvent keyEvent)
                {
                if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER)
                    {
                    setTitle(titleField.getText());
                    }
                else if (keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE)
                    titleField.setText(getTitle());
                }
            });
        titleField.addFocusListener(new FocusAdapter()
            {
            public void focusLost ( FocusEvent e )
                {
                setTitle(titleField.getText());
                }
            });


        LabelledList list = new LabelledList("Chart");
        globalAttributes.add(list);
        list.add(new JLabel("Title"), titleField);

        xLabel = new JTextField();
        xLabel.setText(((XYPlot)(chart.getPlot())).getDomainAxis().getLabel());
        xLabel.addKeyListener(new KeyListener()
            {
            public void keyReleased(KeyEvent keyEvent) {}
            public void keyTyped(KeyEvent keyEvent) {}
            public void keyPressed(KeyEvent keyEvent)
                {
                if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER)
                    {
                    setDomainAxisLabel(xLabel.getText());
                    }
                else if (keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE)
                    xLabel.setText(getDomainAxisLabel());
                }
            });
        xLabel.addFocusListener(new FocusAdapter()
            {
            public void focusLost ( FocusEvent e )
                {
                setDomainAxisLabel(xLabel.getText());
                }
            });

        list.add(new JLabel("X Label"), xLabel);

        yLabel = new JTextField();
        yLabel.setText(((XYPlot)(chart.getPlot())).getRangeAxis().getLabel());
        yLabel.addKeyListener(new KeyListener()
            {
            public void keyReleased(KeyEvent keyEvent) {}
            public void keyTyped(KeyEvent keyEvent) {}
            public void keyPressed(KeyEvent keyEvent)
                {
                if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER)
                    {
                    setRangeAxisLabel(yLabel.getText());
                    }
                else if (keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE)
                    yLabel.setText(getRangeAxisLabel());
                }
            });
        yLabel.addFocusListener(new FocusAdapter()
            {
            public void focusLost ( FocusEvent e )
                {
                setRangeAxisLabel(yLabel.getText());
                }
            });
        list.add(new JLabel("Y Label"), yLabel);

        final JCheckBox legendCheck = new JCheckBox();
        legendCheck.setSelected(false);
        ItemListener il = new ItemListener()
            {
            public void itemStateChanged(ItemEvent e)
                {
                if (e.getStateChange() == ItemEvent.SELECTED)
                    {
                    org.jfree.chart.title.LegendTitle title = new org.jfree.chart.title.LegendTitle(
                        (XYItemRenderer)(chart.getXYPlot().getRenderer()));
                    title.setLegendItemGraphicPadding(new org.jfree.ui.RectangleInsets(0,8,0,4));
                    chart.addLegend(title);
                    }
                else
                    {
                    chart.removeLegend();
                    }
                }
            };
        legendCheck.addItemListener(il);
        list.add(new JLabel("Legend"), legendCheck);

        final JCheckBox aliasCheck = new JCheckBox();
        aliasCheck.setSelected(chart.getAntiAlias());
        il = new ItemListener()
            {
            public void itemStateChanged(ItemEvent e)
                {
                chart.setAntiAlias( e.getStateChange() == ItemEvent.SELECTED );
                }
            };
        aliasCheck.addItemListener(il);
        list.add(new JLabel("Antialias"), aliasCheck);
        JLabel j = new JLabel("Right-Click on Chart for More...");
        j.setFont(j.getFont().deriveFont(10.0f).deriveFont(java.awt.Font.ITALIC));
        list.add(j);

        JPanel pdfButtonPanel = new JPanel();
        pdfButtonPanel.setLayout(new BorderLayout());
        JButton pdfButton = new JButton( "Print to PDF" );
        pdfButtonPanel.add(pdfButton,BorderLayout.WEST);
        pdfButton.addActionListener(new ActionListener()
            {
            public void actionPerformed ( ActionEvent e )
                {
                FileDialog fd = new FileDialog(frame,"Choose PDF file...", FileDialog.SAVE);
                fd.setFile(chart.getTitle().getText() + ".PDF");
                fd.setVisible(true);;
                String fileName = fd.getFile();
                if (fileName!=null)
                    {
                    Dimension dim = chartPanel.getPreferredSize();
                    printChartToPDF( chart, dim.width, dim.height, fd.getDirectory() + fileName );
                    } 
                }
            });
        globalAttributes.add(pdfButtonPanel);
                
        // we add into an outer box so we can later on add more global attributes
        // as the user instructs and still have glue be last
        Box outerAttributes = Box.createVerticalBox();
        outerAttributes.add(globalAttributes);
        outerAttributes.add(Box.createGlue());

        p.add(outerAttributes,BorderLayout.NORTH);
        p.add(scroll,BorderLayout.CENTER);
        p.setMinimumSize(new Dimension(0,0));
        p.setPreferredSize(new Dimension(200,0));
        split.setLeftComponent(p);
        chartHolder.setMinimumSize(new Dimension(0,0));
        split.setRightComponent(chartHolder);
        setLayout(new BorderLayout());
        add(split,BorderLayout.CENTER);
        }
    
    public JFrame createFrame( final sim.display.GUIState state )
        {
        frame = new JFrame()
            {
            public void dispose()
                {
                quit();
                super.dispose();
                }
            };
            
        // these bugs are tickled by our constant redraw requests.
        frame.addComponentListener(new ComponentAdapter()
            {
            // Bug in MacOS X Java 1.3.1 requires that we force a repaint.
            public void componentResized (ComponentEvent e) 
                {
                // Utilities.doEnsuredRepaint(ChartGenerator.this);
                }
            });

        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(this,BorderLayout.CENTER);
        frame.setResizable(true);
        frame.pack();

        frame.setTitle(chart.getTitle().getText());
        return frame;
        }
        
    void printChartToPDF( JFreeChart chart, int width, int height, String fileName )
        {
        try
            {
            Document document = new Document(new com.lowagie.text.Rectangle(width,height));
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.addAuthor("MASON");
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
//            new String[] { "Solid", "Big Dash", "Dash w/Big Skip", "Dash", "Dash Dash Dot", "Dash Dot", "Dash Dot Dot", "Dot", "Dot w/Big Skip" }))));
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
                        ChartGenerator.this.removeSeries(seriesIndex);
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

    }
