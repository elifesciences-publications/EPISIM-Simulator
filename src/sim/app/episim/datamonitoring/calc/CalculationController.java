package sim.app.episim.datamonitoring.calc;


import java.util.Map;
import org.jfree.data.xy.XYSeries;

import episiminterfaces.calc.CalculationAlgorithmConfigurator;
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
	/*	chartGradientCalculator.registerCells(allCells);
		acmvCalculator.registerCells(allCells);
		oneCellCalculator.registerCells(allCells);*/
	}
		
	public void registerAtCalculationAlgorithm(CalculationHandler handler, final XYSeries series, final boolean xAxisLogarithmic, final boolean yAxisLogarithmic){
		CalculationDataManager manager = CalculationDataManagerFactory.createCalculationDataManager(getIDForCalculationHandler(), handler, series, xAxisLogarithmic, yAxisLogarithmic);
		writeParameters(handler);
	}
	
	public void registerAtCalculationalgorithm(CalculationHandler handler, final Map<Double, Double> resultMap){
		CalculationDataManager manager = CalculationDataManagerFactory.createCalculationDataManager(getIDForCalculationHandler(), handler, resultMap);
		writeParameters(handler);
	}
	
	private void writeParameters(CalculationHandler handler){
		if(handler.getParameters() != null){
			System.out.println("The parameter values are:");
			for(String key: handler.getParameters().keySet()){
				System.out.println("Key: " + key + "Value: " + handler.getParameters().get(key));
			}
		}
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
	
	public int getIDForCalculationHandler(){ return CalculationHandlerRegistry.getInstance().getNextCalculationHandlerID(); }
	
	public void resetDataExport(){
		
	}
	
	
	

}
