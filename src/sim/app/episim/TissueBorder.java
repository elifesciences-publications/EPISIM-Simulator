package sim.app.episim;


import java.awt.Shape;
import java.awt.geom.GeneralPath;

import episiminterfaces.EpisimMechanicalModelGlobalParameters;

import sim.app.episim.model.BioChemicalModelController;
import sim.app.episim.model.ModelController;


public class TissueBorder {
	private static int basalY=80;          // y coordinate at which undulations start, the base line    
	private static int basalPeriod=70;      // width of an undulation at the foot
	
	
	private static  EpisimMechanicalModelGlobalParameters globalParameters;  
	
	
	private TissueBorder(){
		globalParameters = ModelController.getInstance().getBioMechanicalModelController().getEpisimMechanicalModelGlobalParameters();  
		
	}
	
	
	public static double getWidth(){
		return globalParameters.getWidth()-2;
	}
	
	public static double lowerBound(double x)
	 {
	
		
		// y = a * e ^ (-b * x * x) Gaussche Glockenkurve
	     double p=basalPeriod; 
	     
	     double partition=x-(int)(x/p)*p - p/2; // alle 10 einen buckel 5=10/2        
	     double v=Math.exp(-partition*partition/globalParameters.getBasalOpening_µm());
	     //System.out.println("x:"+x+" p:"+partition+" v:"+v+" Av:"+basalAmplitude*v);
	     return basalY+globalParameters.getBasalAmplitude_µm()*v;        
	 }

}
