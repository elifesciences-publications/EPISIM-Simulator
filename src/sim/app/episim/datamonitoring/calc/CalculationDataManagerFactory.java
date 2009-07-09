package sim.app.episim.datamonitoring.calc;

import java.util.Map;

import org.jfree.data.xy.XYSeries;

import episiminterfaces.calc.CalculationHandler;
import episiminterfaces.calc.CalculationAlgorithm.CalculationAlgorithmType;

public abstract class CalculationDataManagerFactory {
	
	
	
	protected static CalculationDataManager<Double, Double> createCalculationDataManager(final CalculationHandler handler, final XYSeries series, final boolean xAxisLogarithmic, final boolean yAxisLogarithmic){
		if(series != null){
			final long id = handler.getID();
			CalculationAlgorithmType calType = getAlgorithmType(handler);
			if(calType != null){//TODO: hier Unterscheidung für die verschiedenen CalculationAlgorithms einfügen
				return new CalculationDataManager<Double, Double>(){					
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
		
					public void observedEntityHasChanged() {
						series.clear();
						if(firstCellEver){
							series.setKey(((String)series.getKey()) + (" (Cell " + (counter +1)+ ")"));
							firstCellEver = false;					
						}
						else
							series.setKey(((String)series.getKey()).substring(0, ((String)series.getKey()).length()-(" (Cell " + counter +")").length()) + (" (Cell " + (counter +1)+ ")"));
						counter++;	         
		         }
		
					public void restartSimulation() {
			        counter = 0;
			        series.clear();
		         }
	
					public long getID() { return id; }
	
					public boolean isXScaleLogarithmic() { return xAxisLogarithmic; }
					public boolean isYScaleLogarithmic() { return yAxisLogarithmic; }						            			
				};
			}
		}
		return null;
	}
	
	protected static CalculationDataManager<Double, Double> createCalculationDataManager(CalculationHandler handler, final Map<Double, Double> data){
		if(data != null){
			final long id = handler.getID();
			CalculationAlgorithmType calType = getAlgorithmType(handler);
			if(calType != null){//TODO: hier Unterscheidung für die verschiedenen CalculationAlgorithms einfügen
			return new CalculationDataManager<Double, Double>(){
								
				
				public void addNewValue(Double key, Double value) {
					
					data.put(key, value);	         
	         }
	
				public void observedEntityHasChanged() {
					
					data.put(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
					
	         }
	
				public void restartSimulation() {}

				public long getID(){ return id;}
				public boolean isXScaleLogarithmic() { return false;}
				public boolean isYScaleLogarithmic() { return false;}			
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
