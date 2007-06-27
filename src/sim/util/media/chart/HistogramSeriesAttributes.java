/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package sim.util.media.chart;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.event.*;
import java.util.*;
import sim.util.gui.*;

// From JFreeChart (jfreechart.org)
import org.jfree.data.xy.*;
import org.jfree.chart.*;
import org.jfree.chart.event.*;
import org.jfree.chart.plot.*;
import org.jfree.data.general.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.general.*;

/** A SeriesAttributes used for user control of histogram series created with HistogramGenerator.
    Unfortunately JFreeChart doesn't have nearly
    as well-designed a histogram chart facility as its time series charts.  There is no HistogramSeries object to
    encapsulate a series, and no facilities for deleting or moving series relative to one another.  
*/

public class HistogramSeriesAttributes extends SeriesAttributes
    {
    /** Border thickness */
    float thickness;
    /** The margin: the percentage of available space that a histogram bar will actually take up. 
        Turned off by default. */
    float margin;
    /** The color of the histogram bar. */
    Color fillColor;
    /** The color of the histogram bar border. */
    Color strokeColor;
    /** The opacity of the histogram bar.  Sadly this must be separate than the color because
        Sun doesn't have a proper color selector.  */
    double fillOpacity;
    /** The opacity of the histogram bar border.  Sadly this must be separate than the color because
        Sun doesn't have a proper color selector.  */
    double lineOpacity;
    /** Whether or not to include the margin as a GUI option.  */
    boolean includeMargin;
                
    /** Produces a HistogramSeriesAttributes object with the given generator, series name, series index,
        and desire to display margin options. */
    public HistogramSeriesAttributes(ChartGenerator generator, String name, int index, boolean includeMargin)
        { 
        super(generator, name, index);
        this.includeMargin = includeMargin;
        }

    public void setSeriesName(String val) 
        {
        setName(val);
        ((HistogramGenerator)generator).updateName(seriesIndex,val,false);
        }
                        
    public String getSeriesName() { return getName(); }

    public void rebuildGraphicsDefinitions()
        {
        XYBarRenderer renderer = (XYBarRenderer)(getPlot().getRenderer());
            
        if (thickness == 0.0)
            renderer.setDrawBarOutline(false);
        else
            {
            renderer.setSeriesOutlineStroke(getSeriesIndex(),
                                            new BasicStroke(thickness));
            renderer.setDrawBarOutline(true);
            }

        renderer.setSeriesPaint(getSeriesIndex(),reviseColor(fillColor, fillOpacity));
        renderer.setSeriesOutlinePaint(getSeriesIndex(),reviseColor(strokeColor, lineOpacity));
        if (includeMargin) renderer.setMargin(margin);
        repaint();
        }
        
    public void buildAttributes()
        {
        // The following three variables aren't defined until AFTER construction if
        // you just define them above.  So we define them below here instead.
        thickness = 2.0f;
        margin = 0.5f;
        fillOpacity = 1.0;
        lineOpacity = 1.0;

        NumberTextField numbins = new NumberTextField("", ((HistogramGenerator)generator).getNumBins(seriesIndex),true)
            {
            public double newValue(double newValue) 
                {
                newValue = (int)newValue;
                if (newValue < 1) 
                    newValue = currentValue;
                ((HistogramGenerator)generator).updateSeries(seriesIndex, (int)newValue, false);
                rebuildGraphicsDefinitions();  // forces a repaint
                return newValue;
                }
            };
        addLabelled("Bins",numbins);

        fillColor = (Color)(getPlot().getRenderer().getSeriesPaint(getSeriesIndex()));
        ColorWell well = new ColorWell(fillColor)
            {
            public Color changeColor(Color c) 
                {
                fillColor = c;
                rebuildGraphicsDefinitions();
                return c;
                }
            };

        addLabelled("Fill",well);

        NumberTextField fo = new NumberTextField("Opacity ", fillOpacity,1.0,0.125)
            {
            public double newValue(double newValue) 
                {
                if (newValue < 0.0 || newValue > 1.0) 
                    newValue = currentValue;
                fillOpacity = (float)newValue;
                rebuildGraphicsDefinitions();
                return newValue;
                }
            };
        addLabelled("",fo);

        strokeColor = Color.black; //(Color)(getPlot().getRenderer().getSeriesOutlinePaint(getSeriesIndex()));
        well = new ColorWell(strokeColor)
            {
            public Color changeColor(Color c) 
                {
                strokeColor = c;
                rebuildGraphicsDefinitions();
                return c;
                }
            };

        addLabelled("Line",well);

        NumberTextField lo = new NumberTextField("Opacity ", lineOpacity,1.0,0.125)
            {
            public double newValue(double newValue) 
                {
                if (newValue < 0.0 || newValue > 1.0) 
                    newValue = currentValue;
                lineOpacity = (float)newValue;
                rebuildGraphicsDefinitions();
                return newValue;
                }
            };
        addLabelled("",lo);

        NumberTextField thickitude = new NumberTextField(thickness,false)
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
                        
        if (includeMargin)
            {
            NumberTextField space = new NumberTextField(0.5,1.0,0.125)
                {
                public double newValue(double newValue) 
                    {
                    if (newValue < 0.0 || newValue > 1.0) 
                        newValue = currentValue;
                    margin = (float)newValue;
                    rebuildGraphicsDefinitions();
                    return newValue;
                    }
                };
            addLabelled("Space",space);
            }
        }
    }
