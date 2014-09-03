package sim.app.episim;

import sim.app.episim.util.GenericBag;
import ec.util.MersenneTwisterFast;


public class TestDiff {

	private static double D = 0.2;
	
	public static MersenneTwisterFast random = new MersenneTwisterFast(23);
	
	public void start1(int iterations){
		double cell = 100;
		double[] neighbours = new double [] {0, 111,112, 40};
		for(int i = 0; i < iterations; i++){
			for(int n = 0; n< neighbours.length;n++){
				double delta = (cell-neighbours[n])*D;
				cell -= delta;
				neighbours[n] += delta;
			}
			
		}
		System.out.println("--- Variante 1 ---");
		System.out.println("Cell: "+cell);
		for(int n = 0; n< neighbours.length;n++){
			System.out.println("Neighbours "+(n+1)+": "+neighbours[n]);
		}
	}
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
		new TestDiff().start2(1);

	}

}
