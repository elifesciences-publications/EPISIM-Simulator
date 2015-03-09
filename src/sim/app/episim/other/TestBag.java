package sim.app.episim.other;

import sim.util.Bag;


public class TestBag {

	
	public void test(){
		Bag b = new Bag();
		
		System.out.println(b.numObjs==0);
	}
	
	
	public static void main(String[] args) {

		new TestBag().test();

	}

}
