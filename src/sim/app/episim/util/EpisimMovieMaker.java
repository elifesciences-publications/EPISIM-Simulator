package sim.app.episim.util;

import java.awt.BorderLayout;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import episimexceptions.PropertyException;

import sim.app.episim.EpisimProperties;
import sim.app.episim.EpisimExceptionHandler;
import sim.app.episim.ModeServer;
import sim.util.gui.MovieMakerHack;
import sim.util.gui.Utilities;
import sim.util.gui.MovieMaker;


public class EpisimMovieMaker{
	
	private int frames_per_file = 300;
	
	Frame parentForDialogs;
  
  
   
   private MovieMakerHack movieMaker;
   

	
	
	public int frameCounter = 0;
	private int partCounter = 1;
	private float fps = 10;
	private File moviePath;
	
	private Object[] encodingFormats;
	
	public EpisimMovieMaker(Frame parent) {
		 movieMaker = new MovieMakerHack(parent);	
		 this.parentForDialogs = parent;
		 if(EpisimProperties.getProperty(EpisimProperties.MOVIE_FRAMES_PER_FILE) != null){
			 try{
				 int fno = Integer.parseInt(EpisimProperties.getProperty(EpisimProperties.MOVIE_FRAMES_PER_FILE));
				 this.frames_per_file = fno;
			 }
			 catch(NumberFormatException e) { /*Ignore this exception*/ }
		 }
      /* try
           {
           encoderClass = Class.forName("sim.util.media.MovieEncoder");
           }
       catch (Throwable e) { encoderClass = null; }  // JMF's not installed*/
	   
	  
	   
	  
   }
	
  
   public synchronized boolean start(BufferedImage typicalImage)
   {
       return start(typicalImage, 10f);
   }
       
   
   public synchronized boolean start(BufferedImage typicalImage, float fps){
   	
   		if (movieMaker.isRunning()) return false;
   	   
         int encodeFormatIndex = 0;
         
         try
             {
         	
         	 if(EpisimProperties.getProperty(EpisimProperties.MOVIE_FRAMES_PER_SECOND_PROP)!= null){ 
         		 fps = Float.parseFloat(EpisimProperties.getProperty(EpisimProperties.MOVIE_FRAMES_PER_SECOND_PROP));
         		 this.fps = fps;
         	 }
         	
             // get the list of supported formats
             encodingFormats = (Object[]) movieMaker.getEncoderClass().
                 getMethod("getEncodingFormats", new Class[] {Float.TYPE, BufferedImage.class}).
                 invoke(null, new Object[] { new Float(fps), typicalImage });
             if (encodingFormats==null) return false;
             
             //handle the case that there are only three available formats under linux conditions
             else{
            	 if(encodingFormats.length >=2){
            		 
            			 encodeFormatIndex=1;
            		
            	 }
            	 
             }
             
             encodingFormats = (Object[])movieMaker.getEncoderClass().
             getMethod("getEncodingFormats", new Class[] {Float.TYPE, BufferedImage.class}).
             invoke(null, new Object[] { new Float(fps), typicalImage });
             
             moviePath = EpisimProperties.getFileForPathOfAProperty(EpisimProperties.MOVIE_PATH_PROP, "EpisimMovie", "mov");
             
             Object encoder = movieMaker.getEncoderClass().getConstructor(new Class[]{
                         Float.TYPE, 
                         File.class, 
                         BufferedImage.class, 
                         Class.forName("javax.media.Format")
                         }).
                     newInstance(new Object[]{new Float(fps), 
                                              moviePath,
                                              typicalImage,
                                              encodingFormats[encodeFormatIndex]});
                 
             movieMaker.setEncoder(encoder);
             }
         catch (Throwable e) // (NoClassDefFoundError e)  // uh oh, JMF's not installed
             {
             EpisimExceptionHandler.getInstance().displayException(e);
             movieMaker.setEncoder(null);
             movieMaker.setIsRunning(false);
             return false;
             }
             
         movieMaker.setIsRunning(true);
         return true;
    
   	
   }
   
   private synchronized boolean changeFile(BufferedImage typicalImage){
   	
   	
   	
   		
   	
   		if (!movieMaker.isRunning()) return false;
   	   
   		stop();
   		
         int encodeFormatIndex = 0;
         
         try
             {
         	       
             if (encodingFormats==null) return false;
             if(encodingFormats.length >=2){
         		 
      			 encodeFormatIndex=1;
      		
             }
             Object encoder = movieMaker.getEncoderClass().getConstructor(new Class[]{
                         Float.TYPE, 
                         File.class, 
                         BufferedImage.class, 
                         Class.forName("javax.media.Format")
                         }).
                     newInstance(new Object[]{new Float(fps), 
                                             getNewFileNameAfterFileSwitch(),
                                              typicalImage,
                                              encodingFormats[encodeFormatIndex]});
                 
             movieMaker.setEncoder(encoder);
             System.gc();
             }
         catch (Throwable e) // (NoClassDefFoundError e)  // uh oh, JMF's not installed
             {
             EpisimExceptionHandler.getInstance().displayException(e);
             movieMaker.setEncoder(null);
             movieMaker.setIsRunning(false);
             return false;
             }
             
         movieMaker.setIsRunning(true);
         return true;  	
   }
   
   
   /** Add an image to the movie stream.  Do this only after starting. */
   public synchronized boolean add(BufferedImage image)
   {
   	frameCounter++;
   	if((frameCounter % frames_per_file) == 0) changeFile(image);
   	
   	  if (!movieMaker.isRunning()) return false;
	       //              ((sim.util.media.MovieEncoder)encoder).add(image);
	               {
	               try  // NOT LIKELY TO HAPPEN
	                   {
	               		movieMaker.getEncoderClass().getMethod("add", new Class[]{BufferedImage.class}).
	                       invoke(movieMaker.getEncoder(), new Object[]{image});
	                   }
	               catch(Exception ex)
	                   {
	                   ex.printStackTrace();
	                   return false;
	                   }
	               }
	       return true;
   	
   }
   
   /** End the movie stream, finish up writing to disk, and clean up. */
   public synchronized boolean stop()
   {
   	return movieMaker.stop();
   }
   
   
   private File getNewFileNameAfterFileSwitch(){
   	if(this.moviePath != null){
   		partCounter++;
   		if(partCounter > 1){
   			
   			String path = moviePath.getAbsolutePath();
   			if(path.endsWith(".mov")) path = path.substring(0, path.length()-4) + "_part" + partCounter +".mov";
   			return new File(path);
   		}
   		else return moviePath;
   		
   	}   	
   	return null;
   }
   

}
