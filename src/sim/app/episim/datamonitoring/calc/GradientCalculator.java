package sim.app.episim.datamonitoring.calc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.jfree.data.xy.XYSeries;

import episimexceptions.CellNotValidException;
import episiminterfaces.CalculationHandler;
import episiminterfaces.EpisimCellDiffModel;
import sim.app.episim.*;
import sim.app.episim.datamonitoring.GlobalStatistics;
import sim.app.episim.util.Sorting;

public class GradientCalculator extends AbstractCommonCalculator {
	
	private ArrayList<CalculationHandler> calculationHandlers;
	private ArrayList<Map<Double, Double>> resultMaps;
	private ArrayList<XYSeries> xySeries;
	
	
	protected GradientCalculator(){
		this.calculationHandlers = new ArrayList<CalculationHandler>();
		this.resultMaps = new ArrayList<Map<Double, Double>>();
		this.xySeries = new ArrayList<XYSeries>();
	}
	public void restartSimulation(){
		for(Map<Double, Double> map : this.resultMaps) map.clear();
		for(XYSeries series : this.xySeries) series.clear();
	}
	
	public void registerForChartGradientCalculation(CalculationHandler handler, XYSeries series){
		if(handler == null || series == null) throw new IllegalArgumentException("GradientCalculator: CalculationHandler or XYSeries must not be null!");
		this.calculationHandlers.add(handler);
		this.xySeries.add(series);
		this.resultMaps.add(new HashMap<Double, Double>());
	}
	
	
	public void calculateGradients(){
		for(Map<Double, Double> map : this.resultMaps) map.clear();
		for(XYSeries series : this.xySeries) series.clear();
		double result = 0;
		for(CellType actCell: allCells){
			EpisimCellDiffModel cellDiff = actCell.getEpisimCellDiffModelObject();
			if(cellDiff.getX() >= GlobalStatistics.getInstance().getGradientMinX()
					&& cellDiff.getX() <= GlobalStatistics.getInstance().getGradientMaxX()
					&& cellDiff.getY() >= GlobalStatistics.getInstance().getGradientMinY()
					&& cellDiff.getY() <= GlobalStatistics.getInstance().getGradientMaxY()){
				for(int i = 0; i < calculationHandlers.size(); i++){
					try{
						result = calculationHandlers.get(i).calculate(actCell);
						resultMaps.get(i).put(actCell.getEpisimCellDiffModelObject().getY(), result);
					}
					catch (CellNotValidException e){
						//Exception is Ignored
					}
					
				}
			}
		}
		
		for(int i = 0; i < resultMaps.size(); i++){
			Sorting.sortMapValuesIntoXYSeries(resultMaps.get(i), xySeries.get(i));
		}
	}
}
