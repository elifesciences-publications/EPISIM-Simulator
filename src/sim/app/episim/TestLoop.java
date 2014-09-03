package sim.app.episim;

import sim.app.episim.util.Loop;


public class TestLoop {

	
	public int sum(int i){
		return i+i+1;
	}
	
	public void start(){
		for(int i= 0; i < 10000000; i++){
			Loop.withIndex(0, 400, new Loop.Each() {
	         public void run(int n) {
	         	sum(n);
	         }
	      });
			if((i%1000)==0) System.out.println("i: "+i);
		}         
	}
	public static void main(String[] args) {

		new TestLoop().start();

	}

}
