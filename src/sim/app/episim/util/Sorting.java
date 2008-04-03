package sim.app.episim.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jfree.data.xy.XYSeries;


public class Sorting {

	
	public static void sortMapValuesIntoXYSeries(Map<Double, Double> values, XYSeries series){
		List<Double> resultListY = new LinkedList<Double>();
		resultListY.addAll(values.keySet());
		Collections.sort(resultListY, new Comparator<Double>(){
			public int compare(Double o1, Double o2) {
				return o1.compareTo(o2);
				}
			});
		for(double actY: resultListY)
		series.add(actY, values.get(actY));
 		
	}
}
