package sim.util.gui;

import java.awt.Frame;


public class MovieMakerHack extends MovieMaker{
	 public MovieMakerHack(Frame parent){
		 super(parent);
	 }
	 
	 public Class getEncoderClass(){
		 return encoderClass;
	 }
	 
	 public boolean isRunning(){ return isRunning; }
	 public void setIsRunning(boolean val){ isRunning = val; }
	 public Object getEncoder(){ return encoder; }
	 public void setEncoder(Object enc){ encoder = enc; }

}
