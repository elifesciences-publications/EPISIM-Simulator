package sim.app.episim.datamonitoring.calc;

import java.util.Map;

import org.jfree.data.statistics.SimpleHistogramDataset;
import org.jfree.data.xy.XYSeries;

import sim.app.episim.datamonitoring.dataexport.ObservedDataCollection;
import sim.app.episim.datamonitoring.dataexport.ObservedDataCollection.ObservedDataCollectionType;

import episimexceptions.MethodNotImplementedException;
import episiminterfaces.calc.CalculationHandler;
import episiminterfaces.calc.EntityChangeEvent;
import episiminterfaces.calc.CalculationAlgorithm.CalculationAlgorithmType;
import episiminterfaces.calc.EntityChangeEvent.EntityChangeEventType;
import sim.app.episim.datamonitoring.calc.CalculationDataManager.CalculationDataManagerType;


public abstract class CalculationDataManagerFactory {
	
	
	
	protected static CalculationDataManager<Double> createCalculationDataManager(final CalculationHandler handler, final XYSeries series, final boolean xAxisLogarithmic, final boolean yAxisLogarithmic){
		if(series != null){
			final long id = handler.getID();
			CalculationAlgorithmType calType = getAlgorithmType(handler);
			
			if(calType != null){//TODO: hier Unterscheidung für die verschiedenen CalculationAlgorithms einfügen
				return new CalculationDataManager<Double>(){
					private CalculationDataManagerType type = CalculationDataManagerType.TWODIMTYPE;
					private int counter = 0;
					private boolean firstCellEver = true;					
					
					public void addNewValue(Double key, Double value) {
						if(xAxisLogarithmic && !yAxisLogarithmic){ 
							if(key > 0)series.add(key, value);	         
						}
						else if(!xAxisLogarithmic && yAxisLogarithmic){ 
							if(value > 0)series.add(key, value);	         
						}
						else if(xAxisLogarithmic && yAxisLogarithmic){
							if(value > 0 && key > 0)series.add(key, value);
						}
						else{ series.add(key, value); }
		         }
		
					public void observedEntityHasChanged(EntityChangeEvent event) {						
						
						if(event.getEventType() == EntityChangeEventType.CELLCHANGE){
							series.clear();
							if(firstCellEver){
								series.setKey(((String)series.getKey()) + (" (Cell " + (counter +1)+ ")"));
								firstCellEver = false;					
							}
							else
								series.setKey(((String)series.getKey()).substring(0, ((String)series.getKey()).length()-(" (Cell " + counter +")").length()) + (" (Cell " + (counter +1)+ ")"));
							counter++;	  
						}
						else if(event.getEventType() == EntityChangeEventType.SIMULATIONSTEPCHANGE){							
							series.clear();							
						}
		         }
		
					public void reset() {
			        counter = 0;
			        series.clear();
		         }
					
					public CalculationDataManagerType getCalculationDataManagerType(){ return type; } 
					
					public long getID() { return id; }
	
					public boolean isXScaleLogarithmic() { return xAxisLogarithmic; }
					public boolean isYScaleLogarithmic() { return yAxisLogarithmic; }

					public void addNewValue(Double xValue) {

						throw new MethodNotImplementedException("Method: addNewValue(Double xValue) is not implemented. Please use the method addNewValue(Double key, Double value) instead!");
	               
               }						            			
				};
			}
		}
		return null;
	}
	
	protected static CalculationDataManager<Double> createCalculationDataManager(final CalculationHandler handler, final SimpleHistogramDataset dataSet, final boolean xAxisLogarithmic, final boolean yAxisLogarithmic){
		if(dataSet != null){
			final long id = handler.getID();
			CalculationAlgorithmType calType = getAlgorithmType(handler);
			if(calType != null){//TODO: hier Unterscheidung für die verschiedenen CalculationAlgorithms einfügen
				return new CalculationDataManager<Double>(){
					private CalculationDataManagerType type = CalculationDataManagerType.ONEDIMTYPE;
					private int counter = 0;
					private boolean firstCellEver = true;					
					
					public void addNewValue(Double key, Double value) {
						throw new MethodNotImplementedException("Method: addNewValue(Double key, Double value) is not implemented. Please use the method addNewValue(Double xValue) instead!");
						
		         }
		
					public void observedEntityHasChanged(EntityChangeEvent event) {
						if(event.getEventType() == EntityChangeEventType.SIMULATIONSTEPCHANGE){
							
							dataSet.clearObservations();
						}
		         }
		
					public void reset() {
						
			        counter = 0;
			        dataSet.clearObservations();
		         }
	
					public long getID() { return id; }
	
					public boolean isXScaleLogarithmic() { return xAxisLogarithmic; }
					public boolean isYScaleLogarithmic() { return yAxisLogarithmic; }
					public CalculationDataManagerType getCalculationDataManagerType(){ return type; } 
					
					public void addNewValue(Double xValue) {
						counter++;
						if(yAxisLogarithmic){ 
							if(xValue > 0)dataSet.addObservation(xValue);	         
						}
						
						else{ dataSet.addObservation(xValue); }
	               
               }						            			
				};
			}
		}
		return null;
	}	
	
	
	
	protected static CalculationDataManager<Double> createCalculationDataManager(CalculationHandler handler, final ObservedDataCollection<Double> data){
		if(data != null){
			final long id = handler.getID();
			CalculationAlgorithmType calType = getAlgorithmType(handler);
			if(calType != null){//TODO: hier Unterscheidung für die verschiedenen CalculationAlgorithms einfügen
			return new CalculationDataManager<Double>(){
								
				private CalculationDataManagerType type;
				
				{
					if(data.getType() == ObservedDataCollectionType.ONEDIMTYPE) type = CalculationDataManagerType.ONEDIMTYPE;
					else if(data.getType() == ObservedDataCollectionType.TWODIMTYPE) type = CalculationDataManagerType.TWODIMTYPE;
				}
				
				public void addNewValue(Double value1, Double value2) {
					if(data.getType() == ObservedDataCollectionType.TWODIMTYPE)	data.add(value1, value2);
					else if(data.getType() == ObservedDataCollectionType.ONEDIMTYPE)
						throw new MethodNotImplementedException("Method: addNewValue(Double value1, Double value2) is not implemented. Please use the method addNewValue(Double value) instead!");
	         }
	
				public void observedEntityHasChanged(EntityChangeEvent event) {
					if(event.getEventType() == EntityChangeEventType.CELLCHANGE){
						if(data.getType() == ObservedDataCollectionType.ONEDIMTYPE)	data.add(Double.NEGATIVE_INFINITY);
						else if(data.getType() == ObservedDataCollectionType.TWODIMTYPE) data.add(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
					}
					
	         }
	
				public void reset() { data.clear();}

				public long getID(){ return id;}
				public boolean isXScaleLogarithmic() { return false;}
				public boolean isYScaleLogarithmic() { return false;}
				public CalculationDataManagerType getCalculationDataManagerType(){ return type; } 
				
				public void addNewValue(Double value) {
					if(data.getType() == ObservedDataCollectionType.ONEDIMTYPE)	data.add(value);
					else if(data.getType() == ObservedDataCollectionType.TWODIMTYPE)
						throw new MethodNotImplementedException("Method: addNewValue(Double value) is not implemented. Please use the method addNewValue(Double value1, Double value2) instead!");
            }			
			};
			}
		}
		return null;
	}
	
	private static CalculationAlgorithmType getAlgorithmType(CalculationHandler handler){
		if(handler != null){
			return CalculationAlgorithmServer.getInstance().getCalculationAlgorithmDescriptor(handler.getCalculationAlgorithmID()).getType();
		}
		return null;
	}

}
