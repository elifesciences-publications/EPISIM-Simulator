package sim.app.episim.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.jfree.data.statistics.SimpleHistogramBin;
import org.jfree.data.statistics.SimpleHistogramDataset;

import sim.app.episim.datamonitoring.dataexport.ValueMapListener;

import episimexceptions.MethodNotImplementedException;
import episiminterfaces.calc.EntityChangeEvent;


public class ObservedDataCollection<T>{
	
	private interface ListenerAction<T>{
		void performListenerAction(ValueMapListener<T> listener);
	}
	
	public enum ObservedDataCollectionType {ONEDIMTYPE, TWODIMTYPE, MULTIDIMTYPE, HISTOGRAMTYPE}
	private HashMap<T, T> map = null;
	private ArrayList<T> list = null;
	private ArrayList<Vector<T>> multiDimList = null;
	
	private Set<ValueMapListener<T>> valueMapListenerSet;
	private ObservedDataCollectionType type;
	private long simStep = -1;
	
	private SimpleHistogramDataset histogramDataset;
	private SimpleHistogramBin[] histogramBins;
	
	public ObservedDataCollection(ObservedDataCollectionType _type){
		this.type = _type;
		if(this.type == ObservedDataCollectionType.ONEDIMTYPE) list = new ArrayList<T>();
		else if(this.type == ObservedDataCollectionType.TWODIMTYPE) map = new HashMap<T,T>();
		else if(this.type == ObservedDataCollectionType.MULTIDIMTYPE) multiDimList = new ArrayList<Vector<T>>();
		valueMapListenerSet = new HashSet<ValueMapListener<T>>();
	}
	public ObservedDataCollection(double minValue, double maxValue, int numberOfBins){
		this.type = ObservedDataCollectionType.HISTOGRAMTYPE;
		valueMapListenerSet = new HashSet<ValueMapListener<T>>();
		histogramDataset = new SimpleHistogramDataset(""+System.currentTimeMillis());
		histogramDataset.setAdjustForBinSize(false);
		this.histogramBins = buildBins(minValue, maxValue, numberOfBins);
		for(SimpleHistogramBin bin : histogramBins) histogramDataset.addBin(bin);
	}

	public ObservedDataCollectionType getType(){ return this.type;}
	
	public void clear() {
		if(this.type == ObservedDataCollectionType.ONEDIMTYPE && list != null) list.clear();
		else if(this.type == ObservedDataCollectionType.TWODIMTYPE && map != null) map.clear();
		else if(this.type == ObservedDataCollectionType.MULTIDIMTYPE && multiDimList != null) multiDimList.clear();
		else if(this.type == ObservedDataCollectionType.HISTOGRAMTYPE && histogramDataset != null) histogramDataset.clearObservations();
		simStep = -1;
   }

	

	public void add(final T value1, final T value2) {
		if(this.type == ObservedDataCollectionType.TWODIMTYPE){		
			notifyAllListeners(new ListenerAction<T>(){
	
				public void performListenerAction(ValueMapListener<T> listener) {
	
		         listener.valueAdded(value1, value2);
		         
	         }});
			
			
		   map.put(value1, value2);
		}
		else throw new MethodNotImplementedException("Oberserved Data Collection is of 1 dim type. Please use method add(final T value) or add(final Vector<T> value) instead!");
   }
	public void add(final T value) {
		if(this.type == ObservedDataCollectionType.ONEDIMTYPE){		
			notifyAllListeners(new ListenerAction<T>(){
	
				public void performListenerAction(ValueMapListener<T> listener) {
	
		         listener.valueAdded(value);
		         
	         }});
			
			
		   list.add(value);
		}
		else if(this.type == ObservedDataCollectionType.HISTOGRAMTYPE){
			if(value instanceof Number) histogramDataset.addObservation((Double)value);
			notifyAllListeners(new ListenerAction<T>(){
	
				public void performListenerAction(ValueMapListener<T> listener) {
	
		         listener.valueAdded(value);
		         
	         }});
			
			
		   
		}
		else throw new MethodNotImplementedException("Oberserved Data Collection is of 2 dim type. Please use method add(final T value1, final T value2) or add(final Vector<T> value) instead!");
   }
	public void add(final Vector<T> value) {
		if(this.type == ObservedDataCollectionType.MULTIDIMTYPE){		
			notifyAllListeners(new ListenerAction<T>(){
	
				public void performListenerAction(ValueMapListener<T> listener) {
	
		         listener.valueAdded(value);
		         
	         }});
			
			
		   multiDimList.add(value);
		}
		else throw new MethodNotImplementedException("Oberserved Data Collection is of multi dim type. Please use method add(final T value1, final T value2) or add(final T value) instead!");
   }
	
	
	
	public void observedDataSourceHasChanged(EntityChangeEvent event){
		for(ValueMapListener<T> actListener : valueMapListenerSet) actListener.observedDataSourceChanged(event);
	}

	public int size() {   
		if(this.type == ObservedDataCollectionType.ONEDIMTYPE) return list.size();
		else if(this.type == ObservedDataCollectionType.TWODIMTYPE) return map.size();
		else if(this.type == ObservedDataCollectionType.MULTIDIMTYPE) return multiDimList.size();
		else if(this.type == ObservedDataCollectionType.HISTOGRAMTYPE) return histogramDataset.getSeriesCount();
		
		return 0;
   }

	public SimpleHistogramBin[] getHistogramBins(){
		return this.histogramBins;
	}
	
	
	public void addValueMapListener(ValueMapListener<T> valueMapListener){
		this.valueMapListenerSet.add(valueMapListener);
	}
	
	private void notifyAllListeners(ListenerAction<T> listenerAction){
		for(ValueMapListener<T> valueMapListener: valueMapListenerSet) listenerAction.performListenerAction(valueMapListener);
	}


	 public long getSimStep(){ return simStep; }
	 public void setSimStep(long step){
		 if(step > this.simStep){
			 for(ValueMapListener<T> actListener : valueMapListenerSet){ 
				 actListener.simStepChanged(step);
			 }
			 this.simStep = step;
		 }
	 }
	 
	 private SimpleHistogramBin[] buildBins(double minValue, double maxValue, int numberOfBins){
	     if(minValue > maxValue){
	       double tmp = minValue;
	       minValue = maxValue;
	       maxValue = tmp;
	     }	
	     if(minValue == maxValue)maxValue = (minValue + 1);
	     if(numberOfBins < 0)numberOfBins = Math.abs(numberOfBins);
	     if(numberOfBins == 0)numberOfBins = 1;
	     double binSize = (Math.abs(maxValue - minValue)+1) / ((double)numberOfBins);
	     SimpleHistogramBin[]  bins = new SimpleHistogramBin[numberOfBins];				
	     for(int i = 0; i < numberOfBins; i ++){
	       bins[i] = new SimpleHistogramBin((minValue + i*binSize), (minValue + (i+1)*binSize), true, false);
	     }		
	     return bins;
	   }

}
