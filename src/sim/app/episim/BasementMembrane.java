package sim.app.episim;


import java.awt.Shape;
import java.awt.geom.GeneralPath;


public class BasementMembrane {
	private static int basalY=80;          // y coordinate at which undulations start, the base line    
	private static int basalPeriod=70;      // width of an undulation at the foot
	
	
	
	
	private static  double width =BioChemicalModelController.getInstance().getDoubleField("width")-2;
	private BasementMembrane(){
	 	
	}
	
	
	public static double getWidth(){
		return width;
	}
	
	public static double lowerBound(double x)
	 {
	     // y = a * e ^ (-b * x * x) Gaussche Glockenkurve
	     double p=basalPeriod;        
	     double partition=x-(int)(x/p)*p - p/2; // alle 10 einen buckel 5=10/2        
	     double v=Math.exp(-partition*partition/BioChemicalModelController.getInstance().getIntField("basalOpening_µm"));
	     //System.out.println("x:"+x+" p:"+partition+" v:"+v+" Av:"+basalAmplitude*v);
	     return basalY+BioChemicalModelController.getInstance().getIntField("basalAmplitude_µm")*v;        
	 }

}
