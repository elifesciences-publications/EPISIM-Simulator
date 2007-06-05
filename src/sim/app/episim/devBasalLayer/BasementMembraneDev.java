package sim.app.episim.devBasalLayer;


import java.awt.Shape;
import java.awt.geom.GeneralPath;


public class BasementMembraneDev {
	private static int basalY=80;          // y coordinate at which undulations start, the base line    
	private static int basalPeriod=70;      // width of an undulation at the foot
	
	
	
	
	private static  double width =140-2;
	private BasementMembraneDev(){
	 	
	}
	
	
	public static double getWidth(){
		return width;
	}
	
	public static double lowerBound(double x)
	 {
	     // y = a * e ^ (-b * x * x) Gaussche Glockenkurve
	     double p=basalPeriod;        
	     double partition=x-(int)(x/p)*p - p/2; // alle 10 einen buckel 5=10/2        
	     double v=Math.exp(-partition*partition/150);
	     //System.out.println("x:"+x+" p:"+partition+" v:"+v+" Av:"+basalAmplitude*v);
	     return basalY+40*v;        
	 }
	
	
}