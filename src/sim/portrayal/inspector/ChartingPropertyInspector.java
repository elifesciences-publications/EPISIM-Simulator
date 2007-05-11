/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package sim.portrayal.inspector;
import java.awt.*;
import java.awt.event.*;
import sim.util.*;
import sim.display.*;
import sim.engine.*;
import javax.swing.*;
import sim.util.gui.*;
import sim.util.media.*;
import org.jfree.data.xy.*;
import org.jfree.data.general.*;

public class ChartingPropertyInspector extends PropertyInspector
    {
    XYSeries chartSeries = null;
    XYSeries aggregateSeries = new XYSeries("ChartingPropertyInspector.temp", false);
    ChartGenerator generator;

    double lastTime  = Schedule.BEFORE_SIMULATION;

    public static String name() { return "Chart"; }
    public static Class[] types() 
        {
        return new Class[]
                {
                Number.class, Boolean.TYPE, Byte.TYPE, Short.TYPE,
                Integer.TYPE, Long.TYPE, Float.TYPE,
                Double.TYPE, Valuable.class
                };
        }

    public ChartingPropertyInspector(Properties properties, int index, Frame parent, final GUIState simulation)
        {
        super(properties,index,parent,simulation);
        chartSeries = new XYSeries( properties.getName(index), false );
        generator = chartToUse( properties.getName(index), parent, simulation );
        attributes = (GlobalAttributes)(generator.getGlobalAttribute(0));  // so we share timer information
        validInspector = (generator!=null);
        }

    protected void setStopper(final Stoppable stopper)
        {
        super.setStopper(stopper);
                
        // add our series
        generator.addSeries(chartSeries, new SeriesChangeListener()
            {
            public void seriesChanged(SeriesChangeEvent event) { stopper.stop(); }
            });
        }
                
    ChartGenerator chartToUse( final String sName, Frame parent, final GUIState simulation )
        {
        Bag charts = new Bag();
        if( simulation.guiObjects != null )
            for( int i = 0 ; i < simulation.guiObjects.numObjs ; i++ )
                if( simulation.guiObjects.objs[i] instanceof ChartGenerator )
                    charts.add( simulation.guiObjects.objs[i] );
        if( charts.numObjs == 0 )
            return createNewChart(simulation);

        // init the dialog panel
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());

        String[] chartNames = new String[ charts.numObjs + 1 ];

        chartNames[0] = "[Create a New Chart]";
        for( int i = 0 ; i < charts.numObjs ; i++ )
            chartNames[i+1] = ((ChartGenerator)(charts.objs[i])).getTitle();

        // add widgets
        JPanel panel2 = new JPanel();
        panel2.setLayout(new BorderLayout());
        panel2.setBorder(new javax.swing.border.TitledBorder("Plot on Chart..."));
        JComboBox encoding = new JComboBox(chartNames);
        panel2.add(encoding, BorderLayout.CENTER);
        p.add(panel2, BorderLayout.SOUTH);
                
        // ask
        if(JOptionPane.showConfirmDialog(parent, p,"Create a New Chart...",
                                         JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION)
            return null;

        if( encoding.getSelectedIndex() == 0 )
            return createNewChart(simulation);
        else
            return (ChartGenerator)(charts.objs[encoding.getSelectedIndex()-1]);
        }
        
                                        
    static final int AGGREGATIONMETHOD_CURRENT = 0;
    static final int AGGREGATIONMETHOD_MAX = 1;
    static final int AGGREGATIONMETHOD_MIN = 2;
    static final int AGGREGATIONMETHOD_MEAN = 3;
        
    static final int REDRAW_ALWAYS = 0;
    static final int REDRAW_TENTH_SEC = 1;
    static final int REDRAW_HALF_SEC = 2;
    static final int REDRAW_ONE_SEC = 3;
    static final int REDRAW_TWO_SECS = 4;
    static final int REDRAW_FIVE_SECS = 5;
    static final int REDRAW_TEN_SECS = 6;
    static final int REDRAW_DONT = 7;
    GlobalAttributes attributes;

    class GlobalAttributes extends JPanel
        {
        public long interval = 1;
        public int aggregationMethod = AGGREGATIONMETHOD_CURRENT;
        public int redraw = REDRAW_HALF_SEC;

        public GlobalAttributes()
            {
            setLayout(new BorderLayout());
            LabelledList list = new LabelledList("Add Data...");
            add(list,BorderLayout.CENTER);
                        
            NumberTextField stepsField = new NumberTextField(1,true)
                {
                public double newValue(double value)
                    {
                    value = (long)value;
                    if (value <= 0) return currentValue;
                    else 
                        {
                        interval = (long)value;
                        return value;
                        }
                    }
                };
                                        
            list.addLabelled("Every",stepsField);
            list.addLabelled("",new JLabel("...Timesteps"));

            String[] optionsLabel = { "Current", "Maximum", "Minimum", "Mean" };
            final JComboBox optionsBox = new JComboBox(optionsLabel);
            optionsBox.setSelectedIndex(aggregationMethod);
            optionsBox.addActionListener(
                new ActionListener()
                    {
                    public void actionPerformed(ActionEvent e)
                        {
                        aggregationMethod = optionsBox.getSelectedIndex();
                        }
                    });
            list.addLabelled("Using", optionsBox);
                        
            optionsLabel = new String[]{ "When Adding Data", "Every 0.1 Seconds", "Every 0.5 Seconds", 
                                         "Every Second", "Every 2 Seconds", "Every 5 Seconds", "Every 10 Seconds", "Never" };
            final JComboBox optionsBox2 = new JComboBox(optionsLabel);
            optionsBox2.setSelectedIndex(redraw);
            optionsBox2.addActionListener(
                new ActionListener()
                    {
                    public void actionPerformed(ActionEvent e)
                        {
                        redraw = optionsBox2.getSelectedIndex();
                        generator.update();  // keep up-to-date
                        }
                    });
            list.addLabelled("Redraw", optionsBox2);
            }

        Thread timer = null;
        public void startTimer(final long milliseconds)
            {
            if (timer == null)
                {
                timer= sim.util.Utilities.doLater(milliseconds, new Runnable()
                    {
                    public void run()
                        {
                        if (generator!=null)
                            {
                            generator.update();  // keep up-to-date
                            }
                        // this is in the Swing thread, so it's okay
                        timer = null;
                        }
                    });
                }
            }
        }

    ChartGenerator createNewChart( final GUIState simulation)
        {
        generator = new ChartGenerator()
            {
            public void quit()
                {
                super.quit();
                Stoppable stopper = getStopper();
                if (stopper!=null) stopper.stop();

                // remove the chart from the GUIState's guiObjects
                if( simulation.guiObjects != null )
                    simulation.guiObjects.remove(this);
                }
            };
                        
        attributes = new GlobalAttributes();
        generator.addGlobalAttribute(attributes);
                
        // set up the simulation -- need a new name other than guiObjects: and it should be
        // a HashMap rather than a Bag.
        if( simulation.guiObjects == null )
            simulation.guiObjects = new Bag();
        simulation.guiObjects.add( generator );
        final JFrame f = generator.createFrame(simulation);
        WindowListener wl = new WindowListener()
            {
            public void windowActivated(WindowEvent e) {}
            public void windowClosed(WindowEvent e) {}
            public void windowClosing(WindowEvent e) { generator.quit(); }
            public void windowDeactivated(WindowEvent e) {}
            public void windowDeiconified(WindowEvent e) {}
            public void windowIconified(WindowEvent e) {}
            public void windowOpened(WindowEvent e) {}
            };
        f.addWindowListener(wl);
        f.setVisible(true);;

        return generator;
        }

    // Utility method.  Returns a filename guaranteed to end with the given ending.
    static String ensureFileEndsWith(String filename, String ending)
        {
        // do we end with the string?
        if (filename.regionMatches(false,filename.length()-ending.length(),ending,0,ending.length()))
            return filename;
        else return filename + ending;
        }

    protected double valueFor(Object o)
        {
        if (o instanceof java.lang.Number)  // compiler complains unless I include the full classname!!! Huh?
            return ((Number)o).doubleValue();
        else if (o instanceof Valuable)
            return ((Valuable)o).doubleValue();
        else if (o instanceof Boolean)
            return ((Boolean)o).booleanValue() ? 1 : 0;
        else return Double.NaN;  // unknown
        }

    public void updateInspector()
        {
        double time = simulation.state.schedule.time();
        if (lastTime < time)
            {
            lastTime = time;
            double d = 0;

            // FIRST, load the aggregate series with the items
            aggregateSeries.add(time, d = valueFor(properties.getValue(index)), false);
            int len = aggregateSeries.getItemCount();
                        
            // SECOND, determine if it's time to dump stuff into the main series
            long interval = attributes.interval;
            double intervalMark = time % interval;
            if (!
                // I think these are the three cases for when we may need to update because
                // we've exceeded the next interval
                (intervalMark == 0 || 
                 (time - lastTime >= interval) ||
                 lastTime % interval > intervalMark))
                return;  // not yet
                        
            // THIRD determine how and when to dump stuff into the main series
            double y = 0;  // make compiler happy
            double temp;
            switch(attributes.aggregationMethod)
                {
                case AGGREGATIONMETHOD_CURRENT:  // in this case the aggregateSeries is sort of worthless
                    chartSeries.add(time, d, false);
                    break;
                case AGGREGATIONMETHOD_MAX:
                    double maxX = 0;
                    for(int i=0;i<len;i++)
                        {
                        XYDataItem item = (XYDataItem)(aggregateSeries.getDataItem(i));
                        y = item.getY().doubleValue();
                        temp = item.getX().doubleValue();
                        if( maxX < temp || i==0) maxX = temp;
                        }
                    chartSeries.add( maxX, y, false );
                    break;
                case AGGREGATIONMETHOD_MIN:
                    double minX = 0;
                    for(int i=0;i<len;i++)
                        {
                        XYDataItem item = (XYDataItem)(aggregateSeries.getDataItem(i));
                        y = item.getY().doubleValue();
                        temp = item.getX().doubleValue();
                        if( minX > temp || i==0) minX = temp;
                        }
                    chartSeries.add( minX, y, false );
                    break;
                case AGGREGATIONMETHOD_MEAN:
                    double sumX = 0;
                    int n = 0;
                    for(int i=0;i<len;i++)
                        {
                        XYDataItem item = (XYDataItem)(aggregateSeries.getDataItem(i));
                        y = item.getY().doubleValue();
                        sumX += item.getX().doubleValue();
                        n++;
                        }
                    if (n == 0)
                        System.err.println( "No element????" );
                    else chartSeries.add(sumX / n, y, false);
                    break;
                default:
                    System.err.println( "There are only four aggregation method implemented" );
                }
            aggregateSeries.clear();

            // FOURTH, determine if it's time to redraw
            // at present we will always redraw when there's new data dumped in -- but
            // later we should add the option to redraw every N seconds.
            switch(attributes.redraw) 
                {
                case REDRAW_ALWAYS:  // do it now
                    generator.update();
                    break;
                case REDRAW_TENTH_SEC:
                    attributes.startTimer(100);
                    break;
                case REDRAW_HALF_SEC:
                    attributes.startTimer(500);
                    break;
                case REDRAW_ONE_SEC:
                    attributes.startTimer(1000);
                    break;
                case REDRAW_TWO_SECS:
                    attributes.startTimer(2000);
                    break;
                case REDRAW_FIVE_SECS:
                    attributes.startTimer(5000);
                    break;
                case REDRAW_TEN_SECS:
                    attributes.startTimer(10000);
                    break;
                case REDRAW_DONT:  // do nothing
                    break;
                default:
                    System.err.println("Unknown redraw time specified");
                }
            }
        }
    
    public boolean shouldCreateFrame()
        {
        return false;
        }

    }
