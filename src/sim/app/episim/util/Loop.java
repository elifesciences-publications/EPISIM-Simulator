package sim.app.episim.util;

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

   private static final int CPUs = EpisimProperties.getProperty(EpisimProperties.SIMULATION_PARALLELIZATION_THREAD_NO) == null ? 
   														Runtime.getRuntime().availableProcessors() : Math.abs(Integer.parseInt(EpisimProperties.getProperty(EpisimProperties.SIMULATION_PARALLELIZATION_THREAD_NO)));
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
                   for (int i=lo; i<hi; i++)
                       body.run(i);
                   latch.countDown();
               }
           });
       }
       try {
      	// System.out.println("\nStarted Waiting");
      //	 long startTime = System.currentTimeMillis();
          	latch.await(10l, TimeUnit.SECONDS);
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

   /*public static void main(String [] argv) {
       Loop.withIndex(0, 9, new Loop.Each() {
           public void run(int i) {
               System.out.println(i*10);
           }
       });
   }*/
}