package sim.app.episim.util;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import sim.app.episim.EpisimProperties;
import sim.app.episim.ExceptionDisplayer;


public class Loop {
   public interface Each {
       void run(int i);
   }
   
   public interface Increment {
      void run(double i);
  }

   private static final double CPUs_d = EpisimProperties.getProperty(EpisimProperties.SIMULATION_PARALLELIZATION_THREAD_NO) == null ? 
   														Runtime.getRuntime().availableProcessors() : Math.abs(Double.parseDouble(EpisimProperties.getProperty(EpisimProperties.SIMULATION_PARALLELIZATION_THREAD_NO)));
   private static final int CPUs = (int)CPUs_d;														
   static{
   	System.out.println("Number of used CPUs in class Loop: "+CPUs);
   }
   public static void withIndex(int start, int stop, final Each body) {
       int chunksize = (stop - start + CPUs - 1) / CPUs;
       int loops = (stop - start + chunksize - 1) / chunksize;
       ExecutorService executor = Executors.newFixedThreadPool(CPUs);
       final CountDownLatch latch = new CountDownLatch(loops);
       for (int i=start; i<stop;) {
           final int lo = i;
           i += chunksize;
           final int hi = (i<stop) ? i : stop;
           executor.submit(new Runnable() {
               public void run() {
               	try{
                   for (int i=lo; i<hi; i++){
                       body.run(i);
                   }
               	}
               	catch(Exception e){
               		e.printStackTrace();
               	}
               	catch(Error er){
               		er.printStackTrace();
               	}
               	finally{
                   latch.countDown();
               	}
               }
           });
       }
       try {
      	// System.out.println("\nStarted Waiting");
      //	 long startTime = System.currentTimeMillis();
          	//latch.await(10l, TimeUnit.SECONDS);
          	latch.await();
        //  long endTime = System.currentTimeMillis();
        //  long waitingTime = ((endTime-startTime)/1000);
        //  System.out.println("Stopped Waiting after: "+((endTime-startTime)/1000)+ " seconds");
        //  if(waitingTime >8){
        // 	 System.out.println("It seems to freeze");
         // }
        
       } catch (InterruptedException e) {
      	 ExceptionDisplayer.getInstance().displayException(e);
       }
       executor.shutdown();
   }
   public static void withIndexAndIncrement(double start, double stop, final double increment, boolean equalsHigh, final Increment body) {
      int chunksize =(int)(((stop - start) + (CPUs_d - 1d)*increment) / (CPUs_d*increment));
      BigDecimal chunkSizeReal = BigDecimal.valueOf(chunksize).multiply(BigDecimal.valueOf(increment));
      int loops = (int)Math.ceil(((stop - start)) / chunkSizeReal.doubleValue());
    //  System.out.println("Loops: "+loops);
     
   //   System.out.println("Chunk Size Real: "+chunkSizeReal);
      ExecutorService executor = Executors.newFixedThreadPool(CPUs);
      final CountDownLatch latch = new CountDownLatch(loops);
      for (BigDecimal i=BigDecimal.valueOf(start); i.doubleValue()<BigDecimal.valueOf(stop).doubleValue();) {
          final BigDecimal lo = i;
          i=i.add(chunkSizeReal);
          final BigDecimal hi = BigDecimal.valueOf((i.doubleValue()<BigDecimal.valueOf(stop).doubleValue()) ? i.doubleValue() : BigDecimal.valueOf(stop).doubleValue());
  //        System.out.println( "Low: "+lo+ "   High: "+hi);
          if(equalsHigh && hi.equals(BigDecimal.valueOf(stop))){
         	 executor.submit(new Runnable() {
	              public void run() {
	              	try{
	                  for (BigDecimal i=lo; i.doubleValue()<=hi.doubleValue(); ){
	                      body.run(i.doubleValue());
	                      i=i.add(BigDecimal.valueOf(increment));
	                  }
	              	}
	              	catch(Exception e){
	              		e.printStackTrace();
	              	}
	              	catch(Error er){
	              		er.printStackTrace();
	              	}
	              	finally{
	                  latch.countDown();
	              	}
	              }
	          });
          }
	       else{
	          executor.submit(new Runnable() {
	              public void run() {
	              	try{
	                  for (BigDecimal i=lo; i.doubleValue()<hi.doubleValue(); ){
	                      body.run(i.doubleValue());
	                      i=i.add(BigDecimal.valueOf(increment));
	                  }
	              	}
	              	catch(Exception e){
	              		e.printStackTrace();
	              	}
	              	catch(Error er){
	              		er.printStackTrace();
	              	}
	              	finally{
	                  latch.countDown();
	              	}
	              }
	          });
      	}
      }
      try {
     	// System.out.println("\nStarted Waiting");
     //	 long startTime = System.currentTimeMillis();
         	//latch.await(10l, TimeUnit.SECONDS);
         	latch.await();
       //  long endTime = System.currentTimeMillis();
       //  long waitingTime = ((endTime-startTime)/1000);
       //  System.out.println("Stopped Waiting after: "+((endTime-startTime)/1000)+ " seconds");
       //  if(waitingTime >8){
       // 	 System.out.println("It seems to freeze");
        // }
       
      } catch (InterruptedException e) {
     	 ExceptionDisplayer.getInstance().displayException(e);
      }
      executor.shutdown();
  }

/*  private static double counter = 0;
   public static synchronized void incrementCounter(double increment){
   	counter+=increment;
   }
   public static void main(String [] argv) {
       Loop.withIndexAndIncrement(0, 10, 1, true, new Loop.Increment() {
           public void run(double i) {
               incrementCounter(1);
           }
       });
       System.out.println("Counter: "+counter);
   }*/
}