package sim.app.episim.datamonitoring.calc;


import java.util.Map;
import java.util.concurrent.Semaphore;

import org.jfree.data.statistics.SimpleHistogramDataset;
import org.jfree.data.xy.XYSeries;

import episiminterfaces.calc.CalculationAlgorithmConfigurator;
import episiminterfaces.calc.CalculationCallBack;
import episiminterfaces.calc.CalculationHandler;
import sim.app.episim.*;
import sim.app.episim.model.AbstractCell;
import sim.app.episim.util.ClassLoaderChangeListener;
import sim.app.episim.util.GenericBag;
import sim.app.episim.util.GlobalClassLoader;
import sim.app.episim.util.ObservedDataCollection;
import sim.app.episim.util.TissueCellDataFieldsInspector;

public class CalculationController implements ClassLoaderChangeListener{
	
	private static CalculationController instance;
	
	private static Semaphore sem = new Semaphore(1);
	
	private CalculationController(){
		GlobalClassLoader.getInstance().addClassLoaderChangeListener(this);
	}	
	
	public static CalculationController getInstance(){
		if(instance==null){
			try{
	         sem.acquire();
	         instance = new CalculationController();				
				sem.release();
         }
         catch (InterruptedException e){
	        EpisimExceptionHandler.getInstance().displayException(e);
         }
				
		}
		return instance;
	}
	
	public void registerCells(GenericBag<AbstractCell> allCells){
		if(allCells == null) throw new IllegalArgumentException("CalculationController: the cells bag must not be null!");
		CalculationAlgorithmServer.getInstance().registerCellsAtCalculationAlgorithms(allCells);
	}
		
	public CalculationCallBack registerAtCalculationAlgorithm(CalculationHandler handler, final XYSeries series, final boolean xAxisLogarithmic, final boolean yAxisLogarithmic){
		CalculationDataManager<Double> manager = null;
		if(series != null) manager = CalculationDataManagerFactory.createCalculationDataManager(handler, series, xAxisLogarithmic, yAxisLogarithmic);
			
		return CalculationHandlerAndDataManagerRegistry.getInstance().registerCalculationHandlerAndDataManager(handler, manager);
	}
	
	public CalculationCallBack registerAtCalculationAlgorithm(CalculationHandler handler, final SimpleHistogramDataset dataset, final boolean xAxisLogarithmic, final boolean yAxisLogarithmic){
		CalculationDataManager<Double> manager = null;
		if(dataset != null) manager = CalculationDataManagerFactory.createCalculationDataManager(handler, dataset, xAxisLogarithmic, yAxisLogarithmic);
			
		return CalculationHandlerAndDataManagerRegistry.getInstance().registerCalculationHandlerAndDataManager(handler, manager);
	}
	
	public CalculationCallBack registerAtCalculationAlgorithm(CalculationHandler handler, final ObservedDataCollection<Double> resultMap){
			
		return CalculationHandlerAndDataManagerRegistry.getInstance().registerCalculationHandlerAndDataManager(handler, CalculationDataManagerFactory.createCalculationDataManager(handler, resultMap));
	}
		
	public boolean isValidCalculationAlgorithmConfiguration(CalculationAlgorithmConfigurator config, boolean validateExpression, TissueCellDataFieldsInspector inspector){
		return CalculationAlgorithmConfiguratorChecker.isValidCalculationAlgorithmConfiguration(config, validateExpression, inspector);
	}
	
	
	
	
	public void reset(){
	
		CalculationAlgorithmServer.getInstance().sendResetMessageToCalculationAlgorithms();
	}
	
	public void restartSimulation(){
		CalculationAlgorithmServer.getInstance().sendRestartSimulationMessageToCalculationAlgorithms();
		CalculationHandlerAndDataManagerRegistry.getInstance().resetDataManager();
	}

	
   public void classLoaderHasChanged() {
   	instance = null;	   
   }
}
