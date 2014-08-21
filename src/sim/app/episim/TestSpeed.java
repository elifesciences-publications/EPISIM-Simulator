package sim.app.episim;


public class TestSpeed {
	
	
	private void calculateA(){
		double iterationNo = 0;
		double NUMBER_OF_ITERATIONS = 1;
		double newConcentration = 0;
		double dt = 1;
		double decayConst = 0.1;
		double maxConcentration = 100;
		double minConcentration = 100;
		
		for(int yPos = 0; yPos < 1000; yPos++){
			for(int xPos = 0; xPos < 1000; xPos++){					
				if(iterationNo==(NUMBER_OF_ITERATIONS-1))newConcentration -= dt*(decayConst*newConcentration);
				if(newConcentration > maxConcentration) newConcentration = maxConcentration;
				if(newConcentration < minConcentration) newConcentration = minConcentration;					
			}
		}
	}
	private void calculateB(){
		double iterationNo = 0;
		double NUMBER_OF_ITERATIONS = 1;
		double newConcentration = 0;
		double dt = 1;
		double decayConst = 0.1;
		double maxConcentration = 100;
		double minConcentration = 100;
		
		for(int yPos = 0; yPos < 1000; yPos++){
			for(int xPos = 0; xPos < 1000; xPos++){					
				newConcentration -= iterationNo==(NUMBER_OF_ITERATIONS-1)?dt*(decayConst*newConcentration):0;
				newConcentration = Math.max(minConcentration, Math.min(newConcentration, maxConcentration));	
			}
		}
	}
	
	
	
	public static void main(String[] args){
		TestSpeed ts = new TestSpeed();
		long start = System.currentTimeMillis();
		for(int i = 0; i < 100000; i++)ts.calculateA();
		long end = System.currentTimeMillis();
		
		System.out.println("Dauer A: "+ (end-start));
		
		start = System.currentTimeMillis();
		for(int i = 0; i < 100000; i++)ts.calculateB();
		end = System.currentTimeMillis();
		
		System.out.println("Dauer B: "+ (end-start));
	}

}
