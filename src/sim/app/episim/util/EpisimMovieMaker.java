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


public class EpisimMovieMaker extends MovieMaker {
	
	Frame parentForDialogs;
   Object encoder;
   Class encoderClass;
   boolean isRunning;

	private boolean consoleMode = false;
	
	public EpisimMovieMaker(Frame parent) {
	   super(parent);
	   
	   consoleMode = EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CONSOLEMODE_PROP).equals(EpisimProperties.ON_CONSOLEMODE_VAL);
	   
	   // TODO Auto-generated constructor stub
   }
	
  
   public synchronized boolean start(BufferedImage typicalImage)
   {
       return start(typicalImage, 10f);
   }
       
   
   public synchronized boolean start(BufferedImage typicalImage, float fps){
   	if(!consoleMode) return super.start(typicalImage, fps);
   	else{
   		if (isRunning) return false;
   	   
         int encodeFormatIndex = 0;
         
         try
             {
             // get the list of supported formats
             Object[] f = (Object[]) encoderClass.
                 getMethod("getEncodingFormats", new Class[] {Float.TYPE, BufferedImage.class}).
                 invoke(null, new Object[] { new Float(fps), typicalImage });
             if (f==null) return false;
             
                      
             
            
            
                      
             // now choose the same one as before but with the fps
             // And we hope that the same encoding formats show up with the different framerate's query--
             // this should always be true
             
             // end dan mods
             
            
                 //                encoder = new sim.util.media.MovieEncoder(fps,  // frames per second
                 //                                        new File(fd.getDirectory(), ensureFileEndsWith(fd.getFile(),".mov")),
                 //                                        typicalImage,
                 //                                        (javax.media.Format)f[encodeFormatIndex]);
                 encoder = encoderClass.getConstructor(new Class[]{
                         Float.TYPE, 
                         File.class, 
                         BufferedImage.class, 
                         Class.forName("javax.media.Format")
                         }).
                     newInstance(new Object[]{new Float(fps), 
                                              getMovieFile(),
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
   
   private File getMovieFile(){
   	String path = EpisimProperties.getProperty(EpisimProperties.MOVIE_PATH_PROP);
   	File f = new File(path);
   	if(!f.exists() || !f.isDirectory()) throw new PropertyException("Property -  " + EpisimProperties.MOVIE_PATH_PROP +": " + f.getAbsolutePath() + " is not an (existing) directory!");
   	GregorianCalendar cal = new GregorianCalendar();
   	cal.setTime(new Date());
   	File moviefile = new File(f.getAbsolutePath()+System.getProperty("file.separator")
   										+ cal.get(Calendar.YEAR)+"_"
   										+ cal.get(Calendar.MONTH)+ "_"
   										+ cal.get(Calendar.DAY_OF_MONTH)+ "_"
   										+ cal.get(Calendar.HOUR_OF_DAY)+ "_"
   										+ cal.get(Calendar.MINUTE)+ "_"
   										+ cal.get(Calendar.SECOND)+ "_"
   										+ "EpisimMovie.mov");
   	int index = 1;
   	while(moviefile.exists()){
   		moviefile = new File(f.getAbsolutePath()+System.getProperty("file.separator")
					+ cal.get(Calendar.YEAR)+"_"
					+ cal.get(Calendar.MONTH)+ "_"
					+ cal.get(Calendar.DAY_OF_MONTH)+ "_"
					+ cal.get(Calendar.HOUR_OF_DAY)+ "_"
					+ cal.get(Calendar.MINUTE)+ "_"
					+ cal.get(Calendar.SECOND)+ "_"
					+ "EpisimMovie_"+(index++)+".mov");
   	}
   	return moviefile;
   }

}
