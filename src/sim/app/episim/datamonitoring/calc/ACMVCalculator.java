package sim.app.episim.datamonitoring.calc;

import java.util.HashMap;

import org.jfree.data.xy.XYSeries;

import sim.app.episim.CellType;
import sim.app.episim.datamonitoring.GlobalStatistics;

import episimexceptions.CellNotValidException;
import episiminterfaces.CalculationHandler;
import episiminterfaces.EpisimCellDiffModel;


public class ACMVCalculator extends AbstractCommonCalculator{
	
	public ACMVCalculator(){}
	
	public double calculateMeanValue(CalculationHandler handler) {

		double sum = 0;
		int counter = 0;
		double result = 0;
		for(CellType actCell : allCells){

			try{
				result = handler.calculate(actCell);
				sum += result;
				counter++;
			}
			catch (CellNotValidException e){
				//Exception is ignored
			}

			System.out.println(counter == allCells.size());

		}
		return (sum / counter);
	}
}
