package sim.app.episim.other;

import java.util.Arrays;

import sim.app.episim.util.GenericBag;
import ec.util.MersenneTwisterFast;


public class TestDiff {

	private static double D = 0.1;
	private static double TJ = 0.075;
	private static double LIP = 0.000098;
	private static double TJ_MAX = 200;
	private static double LIP_MAX = 200;
	public static MersenneTwisterFast random = new MersenneTwisterFast(23);
	
	public void start1(int iterations){
		double cellCa = 50;
		double cellTJ = 200;
		double cellLip = 200;
		double[] neighboursCa = new double [] {50,100,100};
		double[] neighboursTJ = new double [] {200,200,200};
		double[] neighboursLip = new double [] {200,0,0};
		for(int i = 0; i < iterations; i++){
			for(int n = 0; n< neighboursCa.length;n++){
				double delta = (cellCa-neighboursCa[n])*D*(1-((neighboursTJ[n]+cellTJ)/(2*TJ_MAX))*(1-TJ))*(1-((neighboursLip[n]+cellLip)/(2*LIP_MAX))*(1-LIP));
				cellCa -= delta;
				neighboursCa[n] += delta;				
			}			
		}
		System.out.println("--- Result ---");
		System.out.println("Cell: " + cellCa);
		for(int n = 0; n< neighboursCa.length;n++){
			System.out.println("Neighbours "+(n+1)+": "+neighboursCa[n]);
		}
	}
	/*public void start1a(int iterations){
		double cell = 200;
		GenericBag<Double> neighboursBag = new GenericBag<Double>();
		neighboursBag.add(100d);
		neighboursBag.add(100d);
		neighboursBag.add(100d);
		neighboursBag.add(100d);
		neighboursBag.add(100d);
		neighboursBag.add(100d);
		for(int i = 0; i < iterations; i++){
			neighboursBag.shuffle(random);
			for(int n = 0; n< neighboursBag.size();n++){
				System.out.println("Cell " + n+": "+ cell);
				double delta = (cell-neighboursBag.get(n))*D;
				cell -= delta;
				neighboursBag.set(n, (neighboursBag.get(n)+delta));				
			}			
		}
		System.out.println("--- Variante 1a ---");
		System.out.println("Cell: " + cell);
		for(int n = 0; n< neighboursBag.size();n++){
			System.out.println("Neighbours "+(n+1)+": "+neighboursBag.get(n));
		}
	}*/
	
	
	
	public void start2(int iterations){
		double cell = 100;
		double[] neighbours = new double [] {0, 111,112, 40};
		for(int i = 0; i < iterations; i++){
			double sum = 0;
			for(int n = 0; n< neighbours.length;n++) sum+=neighbours[n];
			double delta = sum - neighbours.length*cell;
			cell+=(delta*D);
			for(int n = 0; n< neighbours.length;n++) neighbours[n]-= ((delta/neighbours.length)*D);			
		}
		System.out.println("--- Variante 1 ---");
		System.out.println("Cell: "+cell);
		for(int n = 0; n< neighbours.length;n++){
			System.out.println("Neighbours "+(n+1)+": "+neighbours[n]);
		}
	}
/*	public void start2(int iterations){
		double cell = 100;
		//double[] neighbours = new double [] {8, 50};
		GenericBag<Double> neighbours = new GenericBag<Double>();
		neighbours.add(200d);
		neighbours.add(200d);
		neighbours.add(200d);
		neighbours.add(200d);
		for(int i = 0; i < iterations; i++){
			neighbours.shuffle(random);
			for(int n = 0; n< neighbours.size();n++){
				double delta = (cell-neighbours.get(n))*D;
				cell -= delta;
				neighbours.set(n, (neighbours.get(n)+ delta));				
			}
			neighbours.shuffle(random);
			for(int n = 0; n< neighbours.size();n++){
				double delta = (cell-neighbours.get(n))*D;
				cell -= delta;
				neighbours.set(n, (neighbours.get(n)+ delta));				
			}
					
		}
		System.out.println("\n\n--- Variante 2 ---");
		System.out.println("Cell: "+cell);
		for(int n = 0; n< neighbours.size();n++){
			System.out.println("Neighbours: "+neighbours.get(n));
		}
	}*/
	
	public static void main(String[] args) {

		new TestDiff().start1(1);
		//new TestDiff().start1a(3);
	//	new TestDiff().start2(1);

	}

}
