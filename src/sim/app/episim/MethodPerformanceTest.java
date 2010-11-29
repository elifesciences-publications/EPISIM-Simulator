package sim.app.episim;


public class MethodPerformanceTest {
	
	
	private double calculate1(double a, double b){
		double result = a*b;
		result += calculate2(a,b);
		result += calculate3(a,b);
		return result;
	}
	
	private double calculate2(double a, double b){
		return a+b;
	}
	
	private double calculate3(double a, double b){
		return a-b;
	}
	
	public void start(){
		long start1 = System.currentTimeMillis();
		for(int i = 0; i < 10000000; i++){
			double result = calculate1(1,2);
		}
		long end1= System.currentTimeMillis();
		long start2 = System.currentTimeMillis();
		for(int i = 0; i < 10000000; i++){
			double result = 1*2;
			result += (1+2);
			result += (1-2);
		}
		long end2= System.currentTimeMillis();
		
		System.out.println("Performance with methods: " + (end1-start1) + " milliseconds");
		System.out.println("Performance without methods: " + (end2-start2) + " milliseconds");
		
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		MethodPerformanceTest test = new MethodPerformanceTest();
		test.start();		

	}

}
