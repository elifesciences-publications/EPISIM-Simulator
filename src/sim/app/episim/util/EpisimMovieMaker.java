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
import sim.app.episim.ExceptionDisplayer;
import sim.util.Utilities;
import sim.util.WordWrap;
import sim.util.gui.MovieMaker;


public class EpisimMovieMaker{
	
	Frame parentForDialogs;
   Object encoder;
   Class encoderClass;
   boolean isRunning;
   
   private MovieMaker movieMaker;

	private boolean consoleMode = false;
	
	public EpisimMovieMaker(Frame parent) {
		 movieMaker = new MovieMaker(parent);	
		 this.parentForDialogs = parent;
       try
           {
           encoderClass = Class.forName("sim.util.media.MovieEncoder");
           }
       catch (Throwable e) { encoderClass = null; }  // JMF's not installed
	   
	   consoleMode = EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CONSOLE_INPUT_PROP).equals(EpisimProperties.ON_CONSOLE_INPUT_VAL);
	   
	   // TODO Auto-generated constructor stub
   }
	
  
   public synchronized boolean start(BufferedImage typicalImage)
   {
       return start(typicalImage, 10f);
   }
       
   
   public synchronized boolean start(BufferedImage typicalImage, float fps){
   	if(!consoleMode) return movieMaker.start(typicalImage, fps);
   	else{
   		if (isRunning) return false;
   	   
         int encodeFormatIndex = 0;
         
         try
             {
         	
         	 if(EpisimProperties.getProperty(EpisimProperties.FRAMES_PER_SECOND_PROP)!= null) fps = Float.parseFloat(EpisimProperties.getProperty(EpisimProperties.FRAMES_PER_SECOND_PROP));	
         	
             // get the list of supported formats
             Object[] f = (Object[]) encoderClass.
                 getMethod("getEncodingFormats", new Class[] {Float.TYPE, BufferedImage.class}).
                 invoke(null, new Object[] { new Float(fps), typicalImage });
             if (f==null) return false;
             encoder = encoderClass.getConstructor(new Class[]{
                         Float.TYPE, 
                         File.class, 
                         BufferedImage.class, 
                         Class.forName("javax.media.Format")
                         }).
                     newInstance(new Object[]{new Float(fps), 
                                              EpisimProperties.getFileForPathOfAProperty(EpisimProperties.MOVIE_PATH_PROP, "EpisimMovie", "mov"),
                                              typicalImage,
                                              f[encodeFormatIndex]});
                 
             }
         catch (Throwable e) // (NoClassDefFoundError e)  // uh oh, JMF's not installed
             {
             ExceptionDisplayer.getInstance().displayException(e);
             encoder = null;
             isRunning = false;
             return false;
             }
             
         isRunning = true;
         return true;
    
   	}
   }
   
   
   /** Add an image to the movie stream.  Do this only after starting. */
   public synchronized boolean add(BufferedImage image)
   {
   	if(!consoleMode) return movieMaker.add(image);
   	else{
	       if (!isRunning) return false;
	       //              ((sim.util.media.MovieEncoder)encoder).add(image);
	               {
	               try  // NOT LIKELY TO HAPPEN
	                   {
	                   encoderClass.getMethod("add", new Class[]{BufferedImage.class}).
	                       invoke(encoder, new Object[]{image});
	                   }
	               catch(Exception ex)
	                   {
	                   ex.printStackTrace();
	                   return false;
	                   }
	               }
	       return true;
   	}
      }
   
   /** End the movie stream, finish up writing to disk, and clean up. */
   public synchronized boolean stop()
   {
   	if(!consoleMode) return movieMaker.stop();
   	else{
	       boolean success = true;
	       if (!isRunning) return false;  // not running -- why stop?
	       try
	           {
	           //            ((sim.util.media.MovieEncoder)encoder).stop();
	           success = ((Boolean)(encoderClass.getMethod("stop", new Class[0]).invoke(encoder, new Object[0]))).booleanValue();
	           }
	       catch(Exception ex)  // NOT LIKELY TO HAPPEN
	           {
	           ex.printStackTrace();
	           return false;
	           }
	       isRunning = false;
	       return success;
   	}
   }
   
   
   
   

}
