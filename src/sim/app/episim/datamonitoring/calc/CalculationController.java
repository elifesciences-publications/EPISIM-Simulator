package sim.app.episim.datamonitoring.calc;


import java.util.Map;
import org.jfree.data.xy.XYSeries;

import episiminterfaces.calc.CalculationAlgorithmConfigurator;
import episiminterfaces.calc.CalculationCallBack;
import episiminterfaces.calc.CalculationHandler;
import sim.app.episim.*;
import sim.app.episim.util.GenericBag;
import sim.app.episim.util.TissueCellDataFieldsInspector;

public class CalculationController {
	
	private static CalculationController instance;
	
	
	private CalculationController(){
		
	}
	
	
	public static synchronized CalculationController getInstance(){
		if(instance==null) instance = new CalculationController();
		return instance;
	}
	
	public void registerCells(GenericBag<CellType> allCells){
		if(allCells == null) throw new IllegalArgumentException("CalculationController: the cells bag must not be null!");
		System.out.println("register all cells was called");
	}
		
	public CalculationCallBack registerAtCalculationAlgorithm(CalculationHandler handler, final XYSeries series, final boolean xAxisLogarithmic, final boolean yAxisLogarithmic){
		CalculationDataManager<Double, Double> manager = null;
		if(series != null) manager = CalculationDataManagerFactory.createCalculationDataManager(handler, series, xAxisLogarithmic, yAxisLogarithmic);
			
		return CalculationHandlerAndDataManagerRegistry.getInstance().registerCalculationHanderAndDataManager(handler, manager);
	}
	
	public CalculationCallBack registerAtCalculationalgorithm(CalculationHandler handler, final Map<Double, Double> resultMap){
			
		return CalculationHandlerAndDataManagerRegistry.getInstance().registerCalculationHanderAndDataManager(handler, CalculationDataManagerFactory.createCalculationDataManager(handler, resultMap));
	}
		
	public boolean isValidCalculationAlgorithmConfiguration(CalculationAlgorithmConfigurator config, boolean validateExpression, TissueCellDataFieldsInspector inspector){
		return CalculationAlgorithmConfiguratorChecker.isValidCalculationAlgorithmConfiguration(config, validateExpression, inspector);
	}
	
	public void resetChart(){
		/*chartGradientCalculator = new GradientCalculator();
		oneCellCalculator = new OneCellCalculator();*/
	}
	
	public void restartSimulation(){
		/*chartGradientCalculator.restartSimulation();
		oneCellCalculator.restartSimulation();*/
	}
	
	
	
	public void resetDataExport(){
		
	}
	
	
	

}
