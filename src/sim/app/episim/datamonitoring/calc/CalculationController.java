package sim.app.episim.datamonitoring.calc;


import org.jfree.data.xy.XYSeries;
import episiminterfaces.CalculationHandler;
import sim.app.episim.*;
import sim.app.episim.util.GenericBag;

public class CalculationController {
	
	private static CalculationController instance;
	
	private GradientCalculator chartGradientCalculator;
	private ACMVCalculator acmvCalculator;
	private CalculationController(){
		chartGradientCalculator = new GradientCalculator();
	}
	
	
	public static synchronized CalculationController getInstance(){
		if(instance==null) instance = new CalculationController();
		return instance;
	}
	
	public void registerCells(GenericBag<CellType> allCells){
		if(allCells == null) throw new IllegalArgumentException("CalculationController: the cells bag must not be null!");
		chartGradientCalculator.registerCells(allCells);
	}
	
	public void registerForGradientCalculationGradient(CalculationHandler handler, XYSeries series){
		chartGradientCalculator.registerForGradientCalculationGradient(handler, series);
	}
	
	public double calculateACMV(CalculationHandler handler){
		return acmvCalculator.calculateMeanValue(handler);
	}
	public void calculateGradients(){
		chartGradientCalculator.calculateGradients();
	}
	
	public void resetChart(){
		chartGradientCalculator = new GradientCalculator();
	}
	
	public void resetDataExport(){
		
	}
	
	
	

}
