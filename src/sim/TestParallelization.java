package sim;

import java.awt.Color;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import sim.field.grid.IntGrid2D;


public class TestParallelization {
	public void start(){
		final IntGrid2D field = new IntGrid2D(10000,10000);
		long start = System.currentTimeMillis();
		for(int y = 0; y < field.getHeight(); y++){
			for(int x = 0; x < field.getWidth(); x++){
				field.field[x][y]= Color.BLACK.getRGB();
			}
		}
		long end = System.currentTimeMillis();
		System.out.println("Dauer 1: "+(end-start)+ " ms");
		
		final IntGrid2D field2 = new IntGrid2D(10000,10000);
		ExecutorService exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		long start2 = System.currentTimeMillis();
		try {	
			for(int y = 0; y < field2.getHeight(); y++){
				final int y1=y;
				
					 exec.submit(new Runnable() {
			            
			            public void run() {
			            	for(int x = 0; x < field2.getWidth(); x++){			   					
			            		field2.field[x][y1]= Color.BLACK.getRGB();
			            	}
			            }
			        });
					
				
			}	    
		} finally {
		    exec.shutdown();
		}
		long end2= System.currentTimeMillis();
		System.out.println("Dauer 2: "+(end2-start2)+ " ms");
		final IntGrid2D field3 = new IntGrid2D(10000,10000);
		long start3 = System.currentTimeMillis();
		 Loop.withIndex(0, field3.getHeight(), new Loop.Each() {
          public void run(int i) {
         	 for(int x = 0; x < field3.getWidth(); x++){			   					
          		field3.field[x][i]= Color.BLACK.getRGB();
          	}
          }
      });
		 long end3= System.currentTimeMillis();
			System.out.println("Dauer 3: "+(end3-start3)+ " ms");
	}
	
	public static void main(String[] args){
		new TestParallelization().start();
	}
}
