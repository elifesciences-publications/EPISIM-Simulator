package sim.app.episim.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jfree.chart.util.ParamChecks;
import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.statistics.SimpleHistogramBin;
import org.jfree.data.statistics.SimpleHistogramDataset;
import org.jfree.util.ObjectUtilities;


public class OutlierSimpleHistogramDataset extends SimpleHistogramDataset {
	

	/**
    * 
    */
   private static final long serialVersionUID = 8658737983379269327L;
   private List bins;
   
   /** The series key. */
   private Comparable key;

  

   /**
    * A flag that controls whether or not the bin count is divided by the
    * bin size.
    */
   private boolean adjustForBinSize;
   
	public OutlierSimpleHistogramDataset(Comparable key){
		super(key);
		ParamChecks.nullNotPermitted(key, "key");
      this.key = key;
      this.bins = new ArrayList();
      this.adjustForBinSize = true;
	}
	public double getStartXValue(int series, int item) {
      SimpleHistogramBin bin = (SimpleHistogramBin) this.bins.get(item);
      if(item==0) return bin.getUpperBound()-1;
      else if(item==(bins.size()-1)) return bin.getLowerBound()+1;
      else return bin.getLowerBound();
  }
  public double getEndXValue(int series, int item) {
      SimpleHistogramBin bin = (SimpleHistogramBin) this.bins.get(item);
      if(item==0) return bin.getUpperBound()-1;
      else if(item==(bins.size()-1)) return bin.getLowerBound()+1;
      else return bin.getLowerBound();
  }
  
  /**
   * Returns a flag that controls whether or not the bin count is divided by
   * the bin size in the {@link #getXValue(int, int)} method.
   *
   * @return A boolean.
   *
   * @see #setAdjustForBinSize(boolean)
   */
  public boolean getAdjustForBinSize() {
      return this.adjustForBinSize;
  }

  /**
   * Sets the flag that controls whether or not the bin count is divided by
   * the bin size in the {@link #getYValue(int, int)} method, and sends a
   * {@link DatasetChangeEvent} to all registered listeners.
   *
   * @param adjust  the flag.
   *
   * @see #getAdjustForBinSize()
   */
  public void setAdjustForBinSize(boolean adjust) {
      this.adjustForBinSize = adjust;
      notifyListeners(new DatasetChangeEvent(this, this));
  }

  /**
   * Returns the number of series in the dataset (always 1 for this dataset).
   *
   * @return The series count.
   */
  @Override
  public int getSeriesCount() {
      return 1;
  }

  /**
   * Returns the key for a series.  Since this dataset only stores a single
   * series, the <code>series</code> argument is ignored.
   *
   * @param series  the series (zero-based index, ignored in this dataset).
   *
   * @return The key for the series.
   */
  @Override
  public Comparable getSeriesKey(int series) {
      return this.key;
  }

  /**
   * Returns the order of the domain (or X) values returned by the dataset.
   *
   * @return The order (never <code>null</code>).
   */
  @Override
  public DomainOrder getDomainOrder() {
      return DomainOrder.ASCENDING;
  }

  /**
   * Returns the number of items in a series.  Since this dataset only stores
   * a single series, the <code>series</code> argument is ignored.
   *
   * @param series  the series index (zero-based, ignored in this dataset).
   *
   * @return The item count.
   */
  @Override
  public int getItemCount(int series) {
      return this.bins.size();
  }

  /**
   * Adds a bin to the dataset.  An exception is thrown if the bin overlaps
   * with any existing bin in the dataset.
   *
   * @param bin  the bin (<code>null</code> not permitted).
   *
   * @see #removeAllBins()
   */
  public void addBin(SimpleHistogramBin bin) {
      // check that the new bin doesn't overlap with any existing bin
      Iterator iterator = this.bins.iterator();
      while (iterator.hasNext()) {
          SimpleHistogramBin existingBin
                  = (SimpleHistogramBin) iterator.next();
          if (bin.overlapsWith(existingBin)) {
              throw new RuntimeException("Overlapping bin");
          }
      }
      this.bins.add(bin);
      Collections.sort(this.bins);
  }

  /**
   * Adds an observation to the dataset (by incrementing the item count for
   * the appropriate bin).  A runtime exception is thrown if the value does
   * not fit into any bin.
   *
   * @param value  the value.
   */
  public void addObservation(double value) {
      addObservation(value, true);
  }

  /**
   * Adds an observation to the dataset (by incrementing the item count for
   * the appropriate bin).  A runtime exception is thrown if the value does
   * not fit into any bin.
   *
   * @param value  the value.
   * @param notify  send {@link DatasetChangeEvent} to listeners?
   */
  public void addObservation(double value, boolean notify) {
      boolean placed = false;
      Iterator iterator = this.bins.iterator();
      while (iterator.hasNext() && !placed) {
          SimpleHistogramBin bin = (SimpleHistogramBin) iterator.next();
          if (bin.accepts(value)) {
              bin.setItemCount(bin.getItemCount() + 1);
              placed = true;
          }
      }
      if (!placed) {
          throw new RuntimeException("No bin.");
      }
      if (notify) {
          notifyListeners(new DatasetChangeEvent(this, this));
      }
  }

  /**
   * Adds a set of values to the dataset and sends a
   * {@link DatasetChangeEvent} to all registered listeners.
   *
   * @param values  the values (<code>null</code> not permitted).
   *
   * @see #clearObservations()
   */
  public void addObservations(double[] values) {
      for (int i = 0; i < values.length; i++) {
          addObservation(values[i], false);
      }
      notifyListeners(new DatasetChangeEvent(this, this));
  }

  /**
   * Removes all current observation data and sends a
   * {@link DatasetChangeEvent} to all registered listeners.
   *
   * @since 1.0.6
   *
   * @see #addObservations(double[])
   * @see #removeAllBins()
   */
  public void clearObservations() {
      Iterator iterator = this.bins.iterator();
      while (iterator.hasNext()) {
          SimpleHistogramBin bin = (SimpleHistogramBin) iterator.next();
          bin.setItemCount(0);
      }
      notifyListeners(new DatasetChangeEvent(this, this));
  }

  /**
   * Removes all bins and sends a {@link DatasetChangeEvent} to all
   * registered listeners.
   *
   * @since 1.0.6
   *
   * @see #addBin(SimpleHistogramBin)
   */
  public void removeAllBins() {
      this.bins = new ArrayList();
      notifyListeners(new DatasetChangeEvent(this, this));
  }

  /**
   * Returns the x-value for an item within a series.  The x-values may or
   * may not be returned in ascending order, that is up to the class
   * implementing the interface.
   *
   * @param series  the series index (zero-based).
   * @param item  the item index (zero-based).
   *
   * @return The x-value (never <code>null</code>).
   */
  @Override
  public Number getX(int series, int item) {
      return new Double(getXValue(series, item));
  }

  /**
   * Returns the x-value (as a double primitive) for an item within a series.
   *
   * @param series  the series index (zero-based).
   * @param item  the item index (zero-based).
   *
   * @return The x-value.
   */
  @Override
  public double getXValue(int series, int item) {
      SimpleHistogramBin bin = (SimpleHistogramBin) this.bins.get(item);
      return (bin.getLowerBound() + bin.getUpperBound()) / 2.0;
  }

  /**
   * Returns the y-value for an item within a series.
   *
   * @param series  the series index (zero-based).
   * @param item  the item index (zero-based).
   *
   * @return The y-value (possibly <code>null</code>).
   */
  @Override
  public Number getY(int series, int item) {
      return new Double(getYValue(series, item));
  }

  /**
   * Returns the y-value (as a double primitive) for an item within a series.
   *
   * @param series  the series index (zero-based).
   * @param item  the item index (zero-based).
   *
   * @return The y-value.
   *
   * @see #getAdjustForBinSize()
   */
  @Override
  public double getYValue(int series, int item) {
      SimpleHistogramBin bin = (SimpleHistogramBin) this.bins.get(item);
      if (this.adjustForBinSize) {
          return bin.getItemCount()
                 / (bin.getUpperBound() - bin.getLowerBound());
      }
      else {
          return bin.getItemCount();
      }
  }

  /**
   * Returns the starting X value for the specified series and item.
   *
   * @param series  the series index (zero-based).
   * @param item  the item index (zero-based).
   *
   * @return The value.
   */
  @Override
  public Number getStartX(int series, int item) {
      return new Double(getStartXValue(series, item));
  }

 
  /**
   * Returns the ending X value for the specified series and item.
   *
   * @param series  the series index (zero-based).
   * @param item  the item index (zero-based).
   *
   * @return The value.
   */
  @Override
  public Number getEndX(int series, int item) {
      return new Double(getEndXValue(series, item));
  }

  /**
   * Returns the starting Y value for the specified series and item.
   *
   * @param series  the series index (zero-based).
   * @param item  the item index (zero-based).
   *
   * @return The value.
   */
  @Override
  public Number getStartY(int series, int item) {
      return getY(series, item);
  }

  /**
   * Returns the start y-value (as a double primitive) for an item within a
   * series.
   *
   * @param series  the series index (zero-based).
   * @param item  the item index (zero-based).
   *
   * @return The start y-value.
   */
  @Override
  public double getStartYValue(int series, int item) {
      return getYValue(series, item);
  }

  /**
   * Returns the ending Y value for the specified series and item.
   *
   * @param series  the series index (zero-based).
   * @param item  the item index (zero-based).
   *
   * @return The value.
   */
  @Override
  public Number getEndY(int series, int item) {
      return getY(series, item);
  }

  /**
   * Returns the end y-value (as a double primitive) for an item within a
   * series.
   *
   * @param series  the series index (zero-based).
   * @param item  the item index (zero-based).
   *
   * @return The end y-value.
   */
  @Override
  public double getEndYValue(int series, int item) {
      return getYValue(series, item);
  }

  /**
   * Compares the dataset for equality with an arbitrary object.
   *
   * @param obj  the object (<code>null</code> permitted).
   *
   * @return A boolean.
   */
  @Override
  public boolean equals(Object obj) {
      if (obj == this) {
          return true;
      }
      if (!(obj instanceof OutlierSimpleHistogramDataset)) {
          return false;
      }
      OutlierSimpleHistogramDataset that = (OutlierSimpleHistogramDataset) obj;
      if (!this.key.equals(that.key)) {
          return false;
      }
      if (this.adjustForBinSize != that.adjustForBinSize) {
          return false;
      }
      if (!this.bins.equals(that.bins)) {
          return false;
      }
      return true;
  }

  /**
   * Returns a clone of the dataset.
   *
   * @return A clone.
   *
   * @throws CloneNotSupportedException not thrown by this class, but maybe
   *         by subclasses (if any).
   */
  @Override
  public Object clone() throws CloneNotSupportedException {
	  OutlierSimpleHistogramDataset clone = (OutlierSimpleHistogramDataset) super.clone();
      clone.bins = (List) ObjectUtilities.deepClone(this.bins);
      return clone;
  }
}
