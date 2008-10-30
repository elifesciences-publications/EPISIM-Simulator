package sim.app.episim.datamonitoring.calc;


import java.util.Map;
import org.jfree.data.xy.XYSeries;
import episiminterfaces.CalculationHandler;
import sim.app.episim.*;
import sim.app.episim.util.GenericBag;

public class CalculationController {
	
	private static CalculationController instance;
	
	private GradientCalculator chartGradientCalculator;
	private ACMVCalculator acmvCalculator;
	private OneCellCalculator oneCellCalculator;
	private CalculationController(){
		chartGradientCalculator = new GradientCalculator();
		acmvCalculator = new ACMVCalculator();
		oneCellCalculator = new OneCellCalculator();
	}
	
	
	public static synchronized CalculationController getInstance(){
		if(instance==null) instance = new CalculationController();
		return instance;
	}
	
	public void registerCells(GenericBag<CellType> allCells){
		if(allCells == null) throw new IllegalArgumentException("CalculationController: the cells bag must not be null!");
		chartGradientCalculator.registerCells(allCells);
		acmvCalculator.registerCells(allCells);
		oneCellCalculator.registerCells(allCells);
	}
	
	public void registerForChartCalculationGradient(CalculationHandler handler, XYSeries series){
		chartGradientCalculator.registerForChartGradientCalculation(handler, series);
	}
	
	public double calculateACMV(CalculationHandler handler){
		return acmvCalculator.calculateMeanValue(handler);
	}
	public void calculateGradients(){
		chartGradientCalculator.calculateGradients();
	}
	
	public double calculateOneCellBaseLine(long chartId, CalculationHandler handler){
		return oneCellCalculator.calculateOneCellBaseLine(chartId, handler);
	}
	
	public void calculateOneCell(double baseLineResult){
		oneCellCalculator.calculateOneCell(baseLineResult);
	}
	
	public void calculateOneCell(){
		oneCellCalculator.calculateOneCell();
	}
	
	public void registerForOneCellCalculation(CalculationHandler handler, final XYSeries series){
	
		oneCellCalculator.registerForOneCellCalculation(handler, new OneCellTrackingDataManager<Double, Double>(){
			
			private int counter = 0;
			private boolean firstCellEver = true;
		
			
			public void addNewValue(Double key, Double value) {
				series.add(key, value);	         
         }

			public void cellHasChanged() {
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
         }			
		});
	}
	
	public void registerForOneCellCalculation(CalculationHandler handler, final Map<Double, Double> resultMap){
		
		oneCellCalculator.registerForOneCellCalculation(handler, new OneCellTrackingDataManager<Double, Double>(){
			
			private int counter = 0;
			private boolean firstCellEver = true;
		
			
			public void addNewValue(Double key, Double value) {
				
				resultMap.put(key, value);	         
         }

			public void cellHasChanged() {
				resultMap.clear();
				if(firstCellEver){
					//resultMap.setKey(((String)series.getKey()) + (" (Cell " + (counter +1)+ ")"));
					firstCellEver = false;
				}
				else
					//series.setKey(((String)series.getKey()).substring(0, ((String)series.getKey()).length()-(" (Cell " + counter +")").length()) + (" (Cell " + (counter +1)+ ")"));
				counter++;	         
         }

			public void restartSimulation() {
	        counter = 0;	         
         }			
		});
	}
	
	
	public void resetChart(){
		chartGradientCalculator = new GradientCalculator();
		oneCellCalculator = new OneCellCalculator();
	}
	
	public void restartSimulation(){
		chartGradientCalculator.restartSimulation();
		oneCellCalculator.restartSimulation();
	}
	
	public void resetDataExport(){
		
	}
	
	
	

}
